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
package org.netbeans.modules.java.lsp.server.debugging.launch;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.lsp4j.debug.OutputEventArguments;
import org.eclipse.lsp4j.debug.Source;
import org.eclipse.lsp4j.debug.TerminatedEventArguments;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;
import org.netbeans.modules.java.lsp.server.debugging.DebugAdapterContext;
import org.netbeans.modules.java.lsp.server.debugging.NbSourceProvider;
import org.netbeans.modules.java.lsp.server.debugging.utils.ErrorUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author martin
 */
public final class NbLaunchRequestHandler {

    private NbLaunchDelegate activeLaunchHandler;
    private final CompletableFuture<Boolean> waitForDebuggeeConsole = new CompletableFuture<>();

    public CompletableFuture<Void> launch(Map<String, Object> launchArguments, DebugAdapterContext context) {
        CompletableFuture<Void> resultFuture = new CompletableFuture<>();
        boolean noDebug = (Boolean) launchArguments.getOrDefault("noDebug", Boolean.FALSE);
        activeLaunchHandler = noDebug ? new NbLaunchWithoutDebuggingDelegate((daContext) -> handleTerminatedEvent(daContext))
                : new NbLaunchWithDebuggingDelegate();
        // validation
        List<String> modulePaths = (List<String>) launchArguments.getOrDefault("modulePaths", Collections.emptyList());
        List<String> classPaths = (List<String>) launchArguments.getOrDefault("classPaths", Collections.emptyList());
        if (StringUtils.isBlank((String)launchArguments.get("mainClass"))
                || modulePaths.isEmpty() && classPaths.isEmpty()) {
            ErrorUtilities.completeExceptionally(resultFuture,
                "Failed to launch debuggee VM. Missing mainClass or modulePaths/classPaths options in launch configuration.",
                ResponseErrorCode.serverErrorStart);
            return resultFuture;
        }
        if (StringUtils.isBlank((String)launchArguments.get("encoding"))) {
            context.setDebuggeeEncoding(StandardCharsets.UTF_8);
        } else {
            if (!Charset.isSupported((String)launchArguments.get("encoding"))) {
                ErrorUtilities.completeExceptionally(resultFuture,
                    "Failed to launch debuggee VM. 'encoding' options in the launch configuration is not recognized.",
                    ResponseErrorCode.serverErrorStart);
                return resultFuture;
            }
            context.setDebuggeeEncoding(Charset.forName((String)launchArguments.get("encoding")));
        }

        if (StringUtils.isBlank((String)launchArguments.get("vmArgs"))) {
            launchArguments.put("vmArgs", String.format("-Dfile.encoding=%s", context.getDebuggeeEncoding().name()));
        } else {
            // if vmArgs already has the file.encoding settings, duplicate options for jvm will not cause an error, the right most value wins
            launchArguments.put("vmArgs", String.format("%s -Dfile.encoding=%s", launchArguments.get("vmArgs"), context.getDebuggeeEncoding().name()));
        }
        context.setDebugMode(!noDebug);

        activeLaunchHandler.preLaunch(launchArguments, context);

        String filePath = (String)launchArguments.get("mainClass");
        FileObject file = filePath != null ? FileUtil.toFileObject(new File(filePath)) : null;
        if (file == null) {
            ErrorUtilities.completeExceptionally(resultFuture,
                    "Missing file: " + filePath,
                    ResponseErrorCode.serverErrorStart);
            return resultFuture;
        }
        activeLaunchHandler.nbLaunch(file, context, !noDebug, new OutputListener(context)).thenRun(() -> {
            activeLaunchHandler.postLaunch(launchArguments, context);
            resultFuture.complete(null);
        }).exceptionally(e -> {
            resultFuture.completeExceptionally(e);
            return null;
        });
        return resultFuture;
    }

    private static final Pattern STACKTRACE_PATTERN = Pattern.compile("\\s+at\\s+(([\\w$]+\\.)*[\\w$]+)\\(([\\w-$]+\\.java:\\d+)\\)");

    private static OutputEventArguments convertToOutputEventArguments(String message, String category, DebugAdapterContext context) {
        Matcher matcher = STACKTRACE_PATTERN.matcher(message);
        if (matcher.find()) {
            String methodField = matcher.group(1);
            String locationField = matcher.group(matcher.groupCount());
            String fullyQualifiedName = methodField.substring(0, methodField.lastIndexOf("."));
            String packageName = fullyQualifiedName.lastIndexOf(".") > -1 ? fullyQualifiedName.substring(0, fullyQualifiedName.lastIndexOf(".")) : "";
            String[] locations = locationField.split(":");
            String sourceName = locations[0];
            int lineNumber = Integer.parseInt(locations[1]);
            String sourcePath = StringUtils.isBlank(packageName) ? sourceName
                    : packageName.replace('.', File.separatorChar) + File.separatorChar + sourceName;
            Source source = null;
            try {
                source = NbSourceProvider.convertDebuggerSourceToClient(fullyQualifiedName, sourceName, sourcePath, context);
            } catch (URISyntaxException e) {
                // do nothing.
            }

            OutputEventArguments args = new OutputEventArguments();
            args.setCategory(category);
            args.setOutput(message);
            args.setSource(source);
            args.setLine(lineNumber);
            return args;
        }

        OutputEventArguments args = new OutputEventArguments();
        args.setCategory(category);
        args.setOutput(message);
        return args;
    }

    protected void handleTerminatedEvent(DebugAdapterContext context) {
        CompletableFuture.runAsync(() -> {
            try {
                waitForDebuggeeConsole.get(5, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                // do nothing.
            }
            context.getClient().terminated(new TerminatedEventArguments());
        });
    }

    private final class OutputListener implements Consumer<NbProcessConsole.ConsoleMessage> {

        private final DebugAdapterContext context;

        OutputListener(DebugAdapterContext context) {
            this.context = context;
        }

        @Override
        public void accept(NbProcessConsole.ConsoleMessage message) {
            if (message == null) {
                // EOF
                waitForDebuggeeConsole.complete(true);
            } else {
                OutputEventArguments outputEvent = convertToOutputEventArguments(message.output, message.category, context);
                context.getClient().output(outputEvent);
            }
        }
    }
}
