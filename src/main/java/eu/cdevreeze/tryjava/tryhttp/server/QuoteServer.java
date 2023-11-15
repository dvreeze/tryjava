/*
 * Copyright 2023-2024 Chris de Vreeze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.cdevreeze.tryjava.tryhttp.server;

import com.sun.net.httpserver.*;
import eu.cdevreeze.tryjava.tryhttp.model.Quote;
import io.vavr.collection.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * HTTP server returning random quotes, as a program.
 *
 * @author Chris de Vreeze
 */
public class QuoteServer {

    private static class QuoteHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            var randomNumber = Double.valueOf(java.lang.Math.random() * 100).intValue() % QuoteCollection.allQuotes.size();
            var handler = getQuoteHandler(randomNumber);

            handler.handle(exchange);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(QuoteServer.class);

    private static final int port = Integer.parseInt(System.getProperty("QuoteServer.port", "8081"));

    public static void main(String[] args) throws IOException {
        var httpServer = HttpServer.create(new InetSocketAddress("localhost", port), 0);

        httpServer.createContext("/quote-of-the-day", new QuoteHandler());
        httpServer.setExecutor(null);
        httpServer.start();

        logger.atInfo().setMessage("HTTP server started at port {}").addArgument(String.valueOf(port)).log();
    }

    private static HttpHandler getQuoteHandler(int quoteIdx) {
        return HttpHandlers.of(
                200,
                Headers.of("Content-Type", "text/plain; charset=utf-8"),
                quoteToString(QuoteCollection.allQuotes.get(quoteIdx))
        );
    }

    private static String quoteToString(Quote quote) {
        var lines = Vector.of(
                String.format("Quote by %s:%n", quote.attributedTo()),
                quote.text(),
                String.format("%nSubject(s): %s", quote.subjects().mkString(", ")));
        return lines.mkString(String.format("%n"));
    }
}
