# Connect Sheet

## Introdução

Este documento aborda o uso da API Google Sheets com Java, explicando como configurar as credenciais, criar serviços de planilhas, e realizar operações como leitura, escrita, e atualização de dados em planilhas. O exemplo prático utiliza Spring Boot e bibliotecas do Google.

---

## 1. Configurando o Projeto

### Dependências

Adicione as dependências necessárias no arquivo `pom.xml`:

```xml
<dependency>
	<groupId>com.google.api-client</groupId>
	<artifactId>google-api-client</artifactId>
	<version>2.6.0</version>
</dependency>
<dependency>
	<groupId>com.google.oauth-client</groupId>
	<artifactId>google-oauth-client-jetty</artifactId>
	<version>1.36.0</version>
</dependency>
<dependency>
	<groupId>com.google.apis</groupId>
	<artifactId>google-api-services-sheets</artifactId>
	<version>v4-rev20220927-2.0.0</version>
</dependency>
```

### Credenciais

1. Acesse o console do [Google Cloud](https://console.cloud.google.com/).
2. Crie um projeto e habilite a **Google Sheets API**.
3. Gere credenciais do tipo **OAuth 2.0 Client ID** e baixe o arquivo `credentials.json`.
4. Salve este arquivo no diretório `src/main/resources` do seu projeto.

---

## 2. Configurando a Autenticação

Crie uma classe para gerenciar a autenticação e autorização da API:

### Classe `GoogleAuthorizeUtil`

```java
@Configuration
public class GoogleAuthorizeUtil {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final int REDIRECT_PORT = 8888;

    public static Credential authorize() throws IOException, GeneralSecurityException {
        InputStream in = GoogleAuthorizeUtil.class.getResourceAsStream("/google-sheets-client-secret.json");
        if (in == null) {
            throw new FileNotFoundException("Arquivo de credenciais não encontrado");
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow
                .Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new MemoryDataStoreFactory())
                // .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(REDIRECT_PORT)
                .build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}
```

---

## 3. Criando o Serviço de Planilhas

Crie um serviço para interagir com a API Google Sheets:

### Classe `SheetsServiceUtil`

```java
@Service
public class SheetsServiceUtil {

    private static final String APPLICATION_NAME = "Connect Sheet";

    public static Sheets getSheetsService() throws IOException, GeneralSecurityException {
        Credential credential = GoogleAuthorizeUtil.authorize();

        return new Sheets
                .Builder(GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(), credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
```

---

## 4. Operações na Planilha

### Escrevendo em um Intervalo Único

```java
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
```

### Acrescentando a linha seguinte

```java
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
```

### Criando nova página

```java
@Test
@DisplayName("Criando uma nova aba na planilha")
public void case06() throws IOException {
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
}
```

### Salvando um objeto

```java
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
```

### Listando usuários de um Array de objetos
```java
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
```

---

## 5. Conclusão

Aqui você tem uma configuração completa para interagir com a API Google Sheets utilizando Java e Spring Boot. Com isso, é possível realizar diversas operações em planilhas de forma programática.

Para mais informações, consulte a [documentação oficial do Google Sheets API](https://developers.google.com/sheets/api).
