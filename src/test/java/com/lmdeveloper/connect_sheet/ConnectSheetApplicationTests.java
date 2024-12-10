package com.lmdeveloper.connect_sheet;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import com.lmdeveloper.connect_sheet.infra.google.sheets.SheetsServiceUtil;
import com.lmdeveloper.connect_sheet.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ConnectSheetApplicationTests {

    private static Sheets sheetsService;
    private static final String SPREADSHEET_ID = "1ZpcS66HCvvxsH4FlVGJ2trfc5y7GJzOc21egW3KU-ww";
    private static final Logger log = LoggerFactory.getLogger(ConnectSheetApplicationTests.class);

    @BeforeEach
    public void setup() throws GeneralSecurityException, IOException {
        log.info("Iniciando teste");

        log.info("Criando serviço de planilhas");
        sheetsService = SheetsServiceUtil.getSheetsService();
    }

    /**
     * ps: Nesse metodo caso já exista algo escrito no intervalo A1:B6, o conteúdo será substituído
     */
    @Test
    @DisplayName("Escrevendo em um único intervalo")
    public void case01() throws IOException {
        // Log de início da operação
        log.info("Escrevendo na planilha");

        // Criando o corpo da requisição com os valores a serem escritos na planilha
        ValueRange body = new ValueRange()
                .setValues(Arrays.asList(
                        Arrays.asList("Expenses January"),
                        Arrays.asList("books", "30"),
                        Arrays.asList("pens", "10"),
                        Arrays.asList("Expenses February"),
                        Arrays.asList("clothes", "20"),
                        Arrays.asList("shoes", "5")));

        // Log para indicar que a atualização está sendo feita
        log.info("Atualizando valores");

        // Atualizando a planilha no intervalo "A1" com os valores definidos acima
        UpdateValuesResponse result = sheetsService.spreadsheets().values()
                .update(SPREADSHEET_ID, "A1", body)
                .setValueInputOption("RAW") // Inserção dos valores sem formatação automática
                .execute();

        // Log de conclusão da operação
        log.info("Valores atualizados");
    }

    @Test
    @DisplayName("Escrevendo em vários intervalos")
    public void case02() throws IOException {
        // Lista para armazenar múltiplos intervalos com seus respectivos valores
        List<ValueRange> data = new ArrayList<>();

        // Adicionando dados para o intervalo "D1"
        data.add(new ValueRange()
                .setRange("D1")
                .setValues(Arrays.asList(
                        Arrays.asList("January Total", "=B2+B3"))));

        // Adicionando dados para o intervalo "D4"
        data.add(new ValueRange()
                .setRange("D4")
                .setValues(Arrays.asList(
                        Arrays.asList("February Total", "=B5+B6"))));

        // Criando o corpo da requisição de atualização em batch
        BatchUpdateValuesRequest batchBody = new BatchUpdateValuesRequest()
                .setValueInputOption("USER_ENTERED") // Permite inserir fórmulas e interpretá-las
                .setData(data);

        // Executando a atualização em batch
        BatchUpdateValuesResponse batchResult = sheetsService.spreadsheets().values()
                .batchUpdate(SPREADSHEET_ID, batchBody)
                .execute();
    }

    /**
     * ps: Nesse metodo caso exista algo escrito no intervalo A1:B6, será criada uma linha nova com o conteúdo
     */
    @Test
    @DisplayName("Acrescentando valores")
    public void case03() throws IOException {
        // Criando o corpo da requisição com os valores a serem acrescentados
        ValueRange appendBody = new ValueRange()
                .setValues(Arrays.asList(
                        Arrays.asList("Total", "=E1+E4")));

        // Acrescentando valores no final do intervalo especificado ("A1")
        AppendValuesResponse appendResult = sheetsService.spreadsheets().values()
                .append(SPREADSHEET_ID, "A1", appendBody)
                .setValueInputOption("USER_ENTERED") // Permite inserir fórmulas
                .setInsertDataOption("INSERT_ROWS") // Insere os valores como novas linhas
                .setIncludeValuesInResponse(true) // Inclui os valores atualizados na resposta
                .execute();

        // Obtendo os valores atualizados e verificando o valor esperado
        ValueRange total = appendResult.getUpdates().getUpdatedData();
        assertThat(total.getValues().get(0).get(1)).isEqualTo("65"); // Valida se o resultado da fórmula é 65
    }

    @Test
    @DisplayName("Lendo valores de uma planilha")
    public void case04() throws IOException {
        // Definindo os intervalos que serão lidos
        List<String> ranges = Arrays.asList("E1", "E4");

        // Lendo os valores dos intervalos especificados
        BatchGetValuesResponse readResult = sheetsService.spreadsheets().values()
                .batchGet(SPREADSHEET_ID)
                .setRanges(ranges)
                .execute();

        // Validando o total de janeiro (intervalo "E1")
        ValueRange januaryTotal = readResult.getValueRanges().get(0);
        assertThat(januaryTotal.getValues().get(0).get(0))
                .isEqualTo("40");

        // Validando o total de fevereiro (intervalo "E4")
        ValueRange febTotal = readResult.getValueRanges().get(1);
        assertThat(febTotal.getValues().get(0).get(0))
                .isEqualTo("25");
    }

    @Test
    @DisplayName("Criando nova planilha")
    public void case05() throws IOException {
        // Criando uma nova planilha com o título "My Spreadsheet"
        Spreadsheet spreadSheet = new Spreadsheet().setProperties(
                new SpreadsheetProperties().setTitle("My Spreadsheet"));

        // Executando a criação da planilha
        Spreadsheet result = sheetsService
                .spreadsheets()
                .create(spreadSheet).execute();

        // Verificando se o ID da planilha criada não é nulo
        assertThat(result.getSpreadsheetId()).isNotNull();
    }

    @Test
    @DisplayName("Criando uma nova aba na planilha")
    public void case06() throws IOException {
        log.info("Criando uma nova aba na planilha");

        // Configurando o nome e as propriedades da nova aba
        SheetProperties sheetProperties = new SheetProperties()
                .setTitle("Nova Página"); // Define o nome da aba

        // Criando a requisição para adicionar a aba
        AddSheetRequest addSheetRequest = new AddSheetRequest()
                .setProperties(sheetProperties);

        // Envolvendo a requisição em uma lista de requests
        Request request = new Request().setAddSheet(addSheetRequest);

        // Criando o BatchUpdateSpreadsheetRequest com a lista de requests
        BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest()
                .setRequests(Arrays.asList(request));

        // Executando o pedido para adicionar a aba
        BatchUpdateSpreadsheetResponse response = sheetsService
                .spreadsheets()
                .batchUpdate(SPREADSHEET_ID, batchUpdateRequest)
                .execute();

        // Verificando o resultado
        log.info("Aba criada com o ID: {}", response.getReplies().get(0).getAddSheet().getProperties().getSheetId());
    }

    @Test
    @DisplayName("Salvando um objeto do tipo usuário")
    public void case07() throws IOException {
        User user = new User("1", "Lucas", "lucas@lucas.com");

        // Configurando os valores a serem salvos (linha única com atributos do usuário)
        ValueRange body = new ValueRange()
                .setValues(Arrays.asList(Arrays.asList(user.getId(), user.getName(), user.getEmail())));

        // Adicionando a linha na planilha
        sheetsService.spreadsheets().values()
                .append(SPREADSHEET_ID, "Users!A1", body) // "Users" é o nome da aba; "A1" indica onde adicionar
                .setValueInputOption("RAW") // Adiciona os valores como estão
                .setInsertDataOption("INSERT_ROWS") // Insere como nova linha
                .execute();
    }

    @Test
    @DisplayName("Listando usuários da planilha em forma de array de objetos")
    public void case08() throws IOException {
        // Lendo os dados da aba "Users"
        ValueRange response = sheetsService.spreadsheets().values()
                .get(SPREADSHEET_ID, "Users!A1:C") // Intervalo de dados da aba (colunas A a C)
                .execute();

        List<List<Object>> values = response.getValues();
        List<User> users = new ArrayList<>();

        if (values == null || values.isEmpty()) {
            log.info("Nenhum usuário encontrado na planilha.");
            System.out.println(users);
        }

        // Convertendo as linhas da planilha em objetos User
        assert values != null;
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

        System.out.println(users.stream().map(
                user -> user.getId() + " " + user.getName() + " " + user.getEmail()
        ).toList());
    }


}
