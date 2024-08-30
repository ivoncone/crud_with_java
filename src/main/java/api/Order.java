package api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;



public class Order {
    public static String token;

    public static void main(String[] args) {
        try {
            // Request authentication
            authenticate();
            if (token != null) {
                postOrderData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void authenticate() throws Exception {
        URL url = new URL("http://pruebas.loomber.mx/api/auth/login/yuhu");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String jsonInputString = "{\"username\":\"jose.medina@itdevops.com.mx\", \"password\": \"12345\"}";
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("UTF-8");
            os.write(input, 0, input.length);
        }
        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                }
            }

            System.out.println("Token: " + response.toString());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonResponse = mapper.readTree(response.toString());
            token = jsonResponse.get("token").asText();
            System.out.println("Token: " + token);
        } else {
            System.out.println("Login failed. Response code: " + responseCode);
        }
    }
    
    private static void postOrderData() throws Exception {
        URL url = new URL("http://pruebas.loomber.mx/api/pre-orden");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", token);
        conn.setDoOutput(true);

        String jsonInputString = "{"
            + "\"customerOrderNumber\": \"YUHU202448\","
            +"\"cliente\": {"
            +"\"nombre\": \"Antonio Lara Escutia\","
            +"\"email\": \"antonio.lara@gmail.com\","
            +"\"celular\":\"8348532315\""
            +"},"
            +"\"address\": {"
            +"\"calle\": \"Calle\","
            + "\"numero_ext\": \"999\","
            + "\"numero_int\": \"\","
            + "\"colonia\": \"Colonia\","
            + "\"codigo_postal\": \"87000\","
            + "\"municipio\": \"Ciudad Victoria\","
            + "\"estado\": \"Tamaulipas\","
            + "\"comentario_adicionales\": \"Casa color crema\""
            + "},"
            + "\"productos\": ["
            + "{"
            + "\"sku\": \"9900073\","
            + "\"cantidad\": 1.0,"
            + "\"precio\": 380.0"
            + "}"
            + "]"
            + "}";

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("UTF-8");
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
        if (responseCode == HttpURLConnection.HTTP_OK) {
            System.out.println("Order Response: " + response.toString());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonResponse = mapper.readTree(response.toString());
            String orderNumber = jsonResponse.get("numero de orden").asText();
            String customerOrderNumber = jsonResponse.get("customerOrderNUmber").asText();
            String status = jsonResponse.get("estatus").asText();
            System.out.println("Order Number: " + orderNumber);
            System.out.println("Customer order number: " + customerOrderNumber);
            System.out.println("Status: " + status);
        } else {
            System.out.println("Post order data failed. Response code: " + responseCode);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonResponse = mapper.readTree(response.toString());

            String code = jsonResponse.get("code").asText();
            String message = jsonResponse.get("message").asText();

            System.out.println("Error code: " + code);
            System.out.println("Error message: " + message);

            JsonNode errorsNode = jsonResponse.get("errors");
            if (errorsNode != null && errorsNode.isArray()) {
                for (JsonNode errorNode : errorsNode) {
                    String field = errorNode.get("field").asText();
                    String errorMessage = errorNode.get("message").asText();
                    System.out.println("field: " + field);
                    System.out.println("Error message: " + errorMessage);
                }
                
            }
            
        }
    }
}

