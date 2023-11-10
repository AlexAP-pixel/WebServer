/*
 *  MIT License
 *
 *  Copyright (c) 2019 Michael Pogrebinsky - Distributed Systems & Cloud Computing with Java
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpExchange;

public class WebServer {
    private static final String TASK_ENDPOINT = "/task";
    private static final String STATUS_ENDPOINT = "/status";
    private static final String SEARCH_TOKEN_ENDPOINT = "/searchtoken";

    private final int port;
    private HttpServer server;

    public static void main(String[] args) {
        int serverPort = 8080;
        if (args.length == 1) {
            serverPort = Integer.parseInt(args[0]);
        }

        WebServer webServer = new WebServer(serverPort);
        webServer.startServer();
	System.out.println("servidor escuchando en el puerto "+ serverPort);
    }

    public WebServer(int port) {
        this.port = port;
    }

    public void startServer() {
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        HttpContext statusContext = server.createContext(STATUS_ENDPOINT);
        HttpContext taskContext = server.createContext(TASK_ENDPOINT);
        HttpContext searchTokenContext = server.createContext(SEARCH_TOKEN_ENDPOINT);

        statusContext.setHandler(this::handleStatusCheckRequest);
        taskContext.setHandler(this::handleTaskRequest);
        searchTokenContext.setHandler(this::handleSearchTokenRequest);

        server.setExecutor(null);
        server.start();
    }

    private void handleTaskRequest(HttpExchange exchange) throws IOException {
        // Obtiene los encabezados y verifica si está en modo de depuración
        Headers headers = exchange.getRequestHeaders();
        boolean isDebugMode = headers.containsKey("X-Debug") && headers.get("X-Debug").get(0).equalsIgnoreCase("true");

        long startTime = System.nanoTime();

        // Realiza el procesamiento aquí

        long finishTime = System.nanoTime();
        long elapsedTimeInNanos = finishTime - startTime;

        if (isDebugMode) { // Si está en modo de depuración, agrega el encabezado X-Debug-Info
            long elapsedTimeInSeconds = elapsedTimeInNanos / 1_000_000_000; // 1 segundo = 1,000,000,000 nanosegundos
            long elapsedTimeInMilliseconds = (elapsedTimeInNanos % 1_000_000_000) / 1_000_000;

            String debugMessage = String.format("La operación tomó %d nanosegundos = %d segundos con %d milisegundos.",
                    elapsedTimeInNanos, elapsedTimeInSeconds, elapsedTimeInMilliseconds);

            exchange.getResponseHeaders().put("X-Debug-Info", Arrays.asList(debugMessage));
        }

        // Realiza el envío de la respuesta aquí

        exchange.close();
    }

    private void handleStatusCheckRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.close();
            return;
        }

        String responseMessage = "El servidor está vivo\n";
        sendResponse(responseMessage.getBytes(), exchange);
    }

    private void handleSearchTokenRequest(HttpExchange exchange) throws IOException {
        Headers headers = exchange.getRequestHeaders();
        boolean isDebugMode = headers.containsKey("X-Debug") && headers.get("X-Debug").get(0).equalsIgnoreCase("true");
          
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
	    System.out.println ("Datos recibidos: " + requestBody);
        String[] requestData = requestBody.split(",");
        int numTokens = 175760;
        String subcadena = "IPN";
	    long startTime = System.nanoTime();
        int ocurrencias = 0;
        StringBuilder cadenota = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < numTokens; i++) {
            char[] palabra = new char[3];
            for (int j = 0; j < 3; j++) {
                palabra[j] = (char) ('a' + random.nextInt(26));
            }
            cadenota.append(palabra).append(' ');
        }
	    String cadenaTokens = cadenota.toString().trim().toLowerCase();

      

        int index = cadenota.indexOf(subcadena);
        while (index != -1) {
            ocurrencias++;
            index = cadenota.indexOf(subcadena, index + 1);
              
        }

        long finishTime = System.nanoTime();

        if (isDebugMode) {
            long elapsedTimeInNanos = finishTime - startTime;
            long elapsedTimeInSeconds = elapsedTimeInNanos / 1_000_000_000;
            long elapsedTimeInMilliseconds = (elapsedTimeInNanos % 1_000_000_000) / 1_000_000;

            String debugMessage = String.format("La busqueda tomo %d nanosegundos = %d segundos con %d milisegundos.",elapsedTimeInNanos, elapsedTimeInSeconds, elapsedTimeInMilliseconds);

            exchange.getResponseHeaders().put("X-Debug-Info", Arrays.asList(debugMessage));
        }
	    
        String jsonResponse = String.format ("{\"ocurrencias\": %d}", ocurrencias);

        sendResponse(jsonResponse.getBytes(), exchange);
    }

    private void sendResponse(byte[] responseBytes, HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(responseBytes);
        outputStream.flush();
        outputStream.close();
        exchange.close();
    }
}