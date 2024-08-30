package api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;



public class Status {
    public static String token;

    public static void main(String[] args) {
        try {
            // Request authentication
            authenticate();
            if (token != null) {
                postOrderStatus();
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

    private static void postOrderStatus() throws Exception {
        URL url = new URL("http://pruebas.loomber.mx/api/status-entrega");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", token);
        conn.setDoOutput(true);

        String jsonInputString = "{"
            + "\"orden\":\"S36834\""
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
            System.out.println("Status" + status);

            JsonNode addressNode = jsonResponse.get("direccion");
            if (addressNode != null) {
                String street = addressNode.get("calle").asText();
                String extNumber = addressNode.get("numero ext").asText();
                String numInt = addressNode.get("numero int").asText();
                String postalCode = addressNode.get("codigo postal").asText();
                String municipality = addressNode.get("municipio").asText();
                String state = addressNode.get("estado").asText();
                System.out.println("Adress: " + street + " " + extNumber + " " + numInt + ", " + postalCode + ", " + municipality + ", " + state);
            }

            JsonNode productsNode = jsonResponse.get("productos");
            if (productsNode != null && productsNode.isArray()) {
                for (JsonNode productNode : productsNode) {
                    String sku = productNode.get("sku").asText();
                    String trackingNumber = productNode.get("tracking number").asText();
                    String shippingCompany = productNode.get("empresa de envio").asText();
                    System.out.println("Product SKU: " + sku);
                    System.out.println("Tracking number: " + trackingNumber);
                    System.out.println("Shipping company: " + shippingCompany);
                }
            }
        } else {
            System.out.println("Getting order status failed. Response code: " + responseCode);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonResponse = mapper.readTree(response.toString());

            String code = jsonResponse.get("code").asText();
            String message = jsonResponse.get("message").asText();

            System.out.println("Error code: " + code);
            System.out.println("Error message: " + message);

        }
    }
}