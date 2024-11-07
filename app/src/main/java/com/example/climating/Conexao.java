package com.example.climating;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class Conexao {
    static BufferedReader bufferedReader = null;

    public static CompletableFuture<String> pegarJson(String endereco) {
        CompletableFuture<String> future = new CompletableFuture<>();
        try {
            URL url = new URL("https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/" + endereco + "?unitGroup=metric&include=hours%2Cdays&lang=pt&key=YOUR_API_KEY&contentType=json");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            StringBuilder stringBuilder = new StringBuilder();
            bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String linha;
            while ((linha = bufferedReader.readLine()) != null) {
                stringBuilder.append(linha);
            }

            future.complete(stringBuilder.toString());
            return future;
        } catch (Exception e) {
            e.printStackTrace();
            future.complete("Erro");
            return future;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}


