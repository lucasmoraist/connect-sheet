package com.lmdeveloper.connect_sheet.infra.google.sheets;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.lmdeveloper.connect_sheet.infra.google.GoogleAuthorizeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Service
public class SheetsServiceUtil {

    private static final String APPLICATION_NAME = "Connect Sheet";
    private static final Logger log = LoggerFactory.getLogger(SheetsServiceUtil.class);

    public static Sheets getSheetsService() throws IOException, GeneralSecurityException {
        Credential credential = GoogleAuthorizeUtil.authorize();
        log.info("Autorizando aplicação");

        log.info("Criando serviço de planilhas");
        return new Sheets
                .Builder(GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(), credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
