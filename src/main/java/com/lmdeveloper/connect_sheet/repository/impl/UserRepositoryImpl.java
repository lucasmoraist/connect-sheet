package com.lmdeveloper.connect_sheet.repository.impl;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.lmdeveloper.connect_sheet.model.User;
import com.lmdeveloper.connect_sheet.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UserRepositoryImpl implements UserRepository {

    @Autowired
    private Sheets sheetService;

    @Value("${google.sheet.spreadsheet.id}")
    private String SPREADSHEET_ID;

    private static final Logger log = LoggerFactory.getLogger(UserRepositoryImpl.class);

    /**
     * Esse mét0do salva um objeto do tipo User no Google Sheets
     * @param data objeto a ser salvo
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

        try {
            sheetService.spreadsheets().values()
                    .append(SPREADSHEET_ID, RANGE, body)
                    .setValueInputOption("RAW")
                    .setInsertDataOption("INSERT_ROWS")
                    .execute();
            log.info("User salvo com sucesso: {}", user);
        } catch (IOException e) {
            log.error("Erro ao salvar user: {}", user);
            throw new RuntimeException("Erro ao salvar user", e);
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
            List<User> users = new ArrayList<>();

            for (List<Object> row : values) {
                if (row.size() >= 3) { // Certifique-se de que a linha tem pelo menos 3 colunas (id, name, email)
                    User user = new User(
                            row.get(0).toString(), // ID
                            row.get(1).toString(), // Nome
                            row.get(2).toString()  // Email
                    );
                    users.add(user);
                }
            }

            log.info("Users encontrados: {}", users);
            return users;
        } catch (IOException e) {
            log.error("Erro ao buscar users");
            throw new RuntimeException("Erro ao buscar users", e);
        }
    }
}
