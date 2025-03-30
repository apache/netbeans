/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cloud.oracle.developer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.oracle.bmc.http.Priorities;
import com.oracle.bmc.http.client.HttpClient;
import com.oracle.bmc.http.client.HttpRequest;
import com.oracle.bmc.http.client.HttpResponse;
import com.oracle.bmc.http.client.Method;
import com.oracle.bmc.http.client.jersey.JerseyHttpProvider;
import com.oracle.bmc.http.signing.RequestSigningFilter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.ws.rs.core.MediaType;
import org.netbeans.modules.cloud.oracle.OCIManager;
import org.netbeans.modules.cloud.oracle.OCIProfile;
import org.netbeans.modules.cloud.oracle.assets.TempFileGenerator;
import org.netbeans.spi.lsp.CommandProvider;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Horvath
 */
@ServiceProvider(service = CommandProvider.class)
public class BearerTokenCommand implements CommandProvider {

    private static final String COMMAND_CREATE_BEARER_TOKEN = "nbls.cloud.assets.createBearerToken"; //NOI18N

    private static final String API_VERSION = "20180419"; //NOI18N
    private static String lastPath;
    private static Instant lastTaken = Instant.MIN;

    private static final RequestProcessor RP = new RequestProcessor(BearerTokenCommand.class);

    private static final Set COMMANDS = new HashSet<>(Arrays.asList(
            COMMAND_CREATE_BEARER_TOKEN
    ));

    @Override
    public Set<String> getCommands() {
        return Collections.unmodifiableSet(COMMANDS);
    }

    @Override
    public CompletableFuture<Object> runCommand(String command, List<Object> arguments) {
        final String ocirServer;
        if (arguments != null && !arguments.isEmpty()) {
            JsonPrimitive s = (JsonPrimitive) arguments.get(0);
            ocirServer = s.getAsString();
        } else {
            return null;
        }
        CompletableFuture<Object> result = new CompletableFuture<>();
        RP.post(() -> {
            OCIProfile session = OCIManager.getDefault().getActiveProfile();
            try {
                result.complete(generateBearerToken(session, ocirServer));
            } catch (URISyntaxException | InterruptedException | ExecutionException | IOException e) {
                result.completeExceptionally(e);
            }
        });
        return result;
    }

    public static synchronized String generateBearerToken(OCIProfile provider, String ocirServer) throws URISyntaxException, IOException, ExecutionException, InterruptedException  {
        if (Duration.between(lastTaken, Instant.now()).toMinutes() > 55) {
            lastPath = generateNewToken(provider, ocirServer);
            lastTaken = Instant.now();
        }
        return lastPath;
    }

    private static String generateNewToken(OCIProfile provider, String ocirServer) throws URISyntaxException, IOException, InterruptedException, ExecutionException {
        URI uri = new URI(String.format("https://%s/%s/docker/token", ocirServer, API_VERSION));

        RequestSigningFilter requestSigningFilter
                = RequestSigningFilter.fromAuthProvider(provider.getAuthenticationProvider());

        HttpClient client = JerseyHttpProvider.getInstance().newBuilder()
                .registerRequestInterceptor(Priorities.AUTHENTICATION, requestSigningFilter)
                .baseUri(uri).build();

        HttpRequest request = client.createRequest(Method.GET)
                .header("accepts", MediaType.APPLICATION_JSON); //NOI18N
        HttpResponse response = request.execute().toCompletableFuture().get();

        InputStream responseBody = response.streamBody().toCompletableFuture().get();

        BufferedReader reader = new BufferedReader(new InputStreamReader(responseBody, StandardCharsets.UTF_8));
        StringBuilder jsonBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBody.append(line);
        }
        JsonObject jsonObject = JsonParser.parseString(jsonBody.toString()).getAsJsonObject();
        String token = jsonObject.get("token").getAsString(); //NOI18N
        TempFileGenerator gen = new TempFileGenerator("token-", ".txt"); //NOI18N
        Path tempFilePath = gen.writeTextFile(token);
        return tempFilePath.toAbsolutePath().toString();
    }

}
