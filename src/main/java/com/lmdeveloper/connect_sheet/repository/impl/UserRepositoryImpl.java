package com.lmdeveloper.connect_sheet.repository.impl;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import com.lmdeveloper.connect_sheet.model.User;
import com.lmdeveloper.connect_sheet.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    @Autowired
    private Sheets sheetService;

    @Value("${google.sheets.application.id}")
    private String SPREADSHEET_ID;

    private static final Logger log = LoggerFactory.getLogger(UserRepositoryImpl.class);

    /**
     * Esse mét0do salva um objeto do tipo User no Google Sheets
     *
     * @param data  objeto a ser salvo
     * @param RANGE pode ser escrito dessa forma -> "'Página1'!A1:C1"
     * @return objeto salvo
     */
    @Override
    public User save(User data, String RANGE) {
        User user = new User(data.getId(), data.getName(), data.getEmail());
        log.info("Registrando user: {}", user);

        ValueRange body = new ValueRange()
                .setValues(List.of(List.of(user.getId(), user.getName(), user.getEmail())));
        log.info("Corpo da requisição: {}", body);

        Optional<User> userFinded = this.findById(user.getId());

        if (userFinded.isPresent()) {
            updateUser(RANGE, body);
        } else {
            appendUser(RANGE, body);
        }

        return user;
    }

    @Override
    public List<User> findAll(String RANGE) {
        try {
            ValueRange response = sheetService.spreadsheets().values()
                    .get(SPREADSHEET_ID, RANGE)
                    .execute();
            log.info("Buscando users");

            List<List<Object>> values = response.getValues();
            if (values == null || values.isEmpty()) {
                log.info("Nenhum user encontrado.");
                return Collections.emptyList();
            }

            List<User> users = new ArrayList<>();
            for (List<Object> row : values) {
                if (row.size() >= 3) {
                    users.add(new User.Builder()
                            .id(row.get(0).toString())
                            .name(row.get(1).toString())
                            .email(row.get(2).toString())
                            .build()
                    );
                }
            }

            log.info("Users encontrados: {}", users);
            return users;
        } catch (IOException e) {
            log.error("Erro ao buscar users");
            throw new RuntimeException("Erro ao buscar users", e);
        }
    }

    @Override
    public Optional<User> findById(String id) {
        try {
            ValueRange response = sheetService.spreadsheets().values()
                    .get(SPREADSHEET_ID, "Users!A1:C")
                    .execute();
            log.info("Recebendo todos os ID's");

            List<List<Object>> values = response.getValues();
            if (values != null) {
                for (List<Object> row : values) {
                    if (row.size() >= 3 && row.get(0).toString().equals(id)) {
                        User user = new User(
                                row.get(0).toString(),
                                row.get(1).toString(),
                                row.get(2).toString()
                        );
                        log.info("User encontrado: {}", user);
                        return Optional.of(user);
                    }
                }
            }
            log.info("User não encontrado: {}", id);
        } catch (IOException e) {
            log.error("Erro ao buscar user por id: {}", id);
            throw new RuntimeException("Erro ao buscar user por id", e);
        }
        return Optional.empty();
    }

    @Override
    public void deleteById(String id) {
        try {
            // Obter todos os dados da planilha
            ValueRange response = sheetService.spreadsheets().values()
                    .get(SPREADSHEET_ID, "Users!A1:C")
                    .execute();
            log.info("Recebendo todos os IDs para exclusão");

            List<List<Object>> values = response.getValues();
            if (values != null) {
                for (int i = 0; i < values.size(); i++) {
                    List<Object> row = values.get(i);
                    // Verificar se o ID corresponde
                    if (!row.isEmpty() && row.get(0).toString().equals(id)) {
                        log.info("Usuário encontrado para exclusão: {}", id);

                        // Excluir a linha correspondente
                        sheetService.spreadsheets().batchUpdate(SPREADSHEET_ID, new BatchUpdateSpreadsheetRequest()
                                .setRequests(Collections.singletonList(
                                        new Request().setDeleteDimension(
                                                new DeleteDimensionRequest()
                                                        .setRange(new DimensionRange()
                                                                .setSheetId(getSheetId("Users"))
                                                                .setDimension("ROWS")
                                                                .setStartIndex(i)
                                                                .setEndIndex(i + 1)
                                                        )
                                        )
                                ))).execute();

                        log.info("Usuário excluído com sucesso: {}", id);
                        return;
                    }
                }
            }
            log.info("Usuário não encontrado para exclusão: {}", id);
        } catch (IOException e) {
            log.error("Erro ao excluir usuário por ID: {}", id, e);
            throw new RuntimeException("Erro ao excluir usuário por ID", e);
        }
    }

    private void appendUser(String RANGE, ValueRange body) {
        try {
            sheetService.spreadsheets().values()
                    .append(SPREADSHEET_ID, RANGE, body)
                    .setValueInputOption("RAW")
                    .setInsertDataOption("INSERT_ROWS")
                    .execute();
            log.info("User salvo com sucesso");
        } catch (IOException e) {
            log.error("Erro ao salvar user", e);
            throw new RuntimeException("Erro ao salvar user", e);
        }
    }

    private void updateUser(String RANGE, ValueRange body) {
        try {
            sheetService.spreadsheets().values()
                    .update(SPREADSHEET_ID, RANGE, body)
                    .setValueInputOption("RAW")
                    .execute();
            log.info("User atualizado com sucesso");
        } catch (IOException e) {
            log.error("Erro ao atualizar user", e);
            throw new RuntimeException("Erro ao atualizar user", e);
        }
    }
    private Integer getSheetId(String sheetName) throws IOException {
        Spreadsheet spreadsheet = sheetService.spreadsheets().get(SPREADSHEET_ID).execute();
        for (Sheet sheet : spreadsheet.getSheets()) {
            if (sheet.getProperties().getTitle().equals(sheetName)) {
                return sheet.getProperties().getSheetId();
            }
        }
        throw new RuntimeException("Sheet não encontrada: " + sheetName);
    }

}
