/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.lsp4j.debug.OutputEventArguments;
import org.eclipse.lsp4j.debug.Source;
import org.eclipse.lsp4j.debug.TerminatedEventArguments;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.lsp.server.debugging.DebugAdapterContext;
import org.netbeans.modules.java.lsp.server.debugging.NbSourceProvider;
import org.netbeans.modules.java.lsp.server.debugging.utils.ErrorUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author martin
 */
public final class NbLaunchRequestHandler {

    private NbLaunchDelegate activeLaunchHandler;

    public CompletableFuture<Void> launch(Map<String, Object> launchArguments, DebugAdapterContext context) {
        boolean isNative = "nativeimage".equals(launchArguments.get("type"));
        CompletableFuture<Void> resultFuture = new CompletableFuture<>();
        boolean noDebug = (Boolean) launchArguments.getOrDefault("noDebug", Boolean.FALSE);
        Consumer<DebugAdapterContext> terminateHandle = (daContext) -> handleTerminatedEvent(daContext);
        activeLaunchHandler = noDebug ? new NbLaunchWithoutDebuggingDelegate(terminateHandle)
                : new NbLaunchWithDebuggingDelegate(terminateHandle);
        // validation
        List<String> modulePaths = (List<String>) launchArguments.getOrDefault("modulePaths", Collections.emptyList());
        List<String> classPaths = (List<String>) launchArguments.getOrDefault("classPaths", Collections.emptyList());

        // "file" key is provided by DAP client infrastructure, sometimes in an unsuitable manner, e.g. some cryptic ID for Output window etc. 
        // the "projectFile" allows to override the infrastructure from client logic.
        String filePath = (String)launchArguments.get("file");
        String projectFilePath = (String)launchArguments.get("projectFile");
        String mainFilePath = (String)launchArguments.get("mainClass");

        if (!isNative && (StringUtils.isBlank(mainFilePath) && StringUtils.isBlank(filePath) && StringUtils.isBlank(projectFilePath)
                          || modulePaths.isEmpty() && classPaths.isEmpty())) {
            ErrorUtilities.completeExceptionally(resultFuture,
                "Failed to launch debuggee VM. Missing mainClass or modulePaths/classPaths options in launch configuration.",
                ResponseErrorCode.ServerNotInitialized);
            return resultFuture;
        }
        if (StringUtils.isBlank((String)launchArguments.get("encoding"))) {
            context.setDebuggeeEncoding(StandardCharsets.UTF_8);
        } else {
            if (!Charset.isSupported((String)launchArguments.get("encoding"))) {
                ErrorUtilities.completeExceptionally(resultFuture,
                    "Failed to launch debuggee VM. 'encoding' options in the launch configuration is not recognized.",
                    ResponseErrorCode.ServerNotInitialized);
                return resultFuture;
            }
            context.setDebuggeeEncoding(Charset.forName((String)launchArguments.get("encoding")));
        }

        if (!isNative) {
            if (StringUtils.isBlank((String)launchArguments.get("vmArgs"))) {
                launchArguments.put("vmArgs", String.format("-Dfile.encoding=%s", context.getDebuggeeEncoding().name()));
            } else {
                // if vmArgs already has the file.encoding settings, duplicate options for jvm will not cause an error, the right most value wins
                launchArguments.put("vmArgs", String.format("%s -Dfile.encoding=%s", launchArguments.get("vmArgs"), context.getDebuggeeEncoding().name()));
            }
        }
        context.setDebugMode(!noDebug);

        activeLaunchHandler.preLaunch(launchArguments, context);

        if (projectFilePath != null) {
            filePath = projectFilePath;
        }
        boolean preferProjActions = true; // True when we prefer project actions to the current (main) file actions.
        if (filePath == null || mainFilePath != null) {
            // main overides the current file
            preferProjActions = false;
            filePath = mainFilePath;
        }
        FileObject file = null;
        File nativeImageFile = null;
        if (!isNative) {
            file = getFileObject(filePath);
            if (file == null) {
                ErrorUtilities.completeExceptionally(resultFuture,
                        "Missing file: " + filePath,
                        ResponseErrorCode.ServerNotInitialized);
                return resultFuture;
            }
        } else {
            String nativeImage = (String) launchArguments.get("nativeImagePath");
            if (nativeImage == null) {
                ErrorUtilities.completeExceptionally(resultFuture,
                    "Failed to launch debuggee native image. No native image is specified.",
                    ResponseErrorCode.ServerNotInitialized);
                return resultFuture;
            }
            nativeImageFile = new File(nativeImage);
        }
        if (!isNative && !launchArguments.containsKey("sourcePaths")) {
            ClassPath sourceCP = ClassPath.getClassPath(file, ClassPath.SOURCE);
            if (sourceCP != null) {
                FileObject[] roots = sourceCP.getRoots();
                String[] sourcePaths = new String[roots.length];
                for (int i = 0; i < roots.length; i++) {
                    sourcePaths[i] = roots[i].getPath();
                }
                context.setSourcePaths(sourcePaths);
            }
        } else {
            context.setSourcePaths((String[]) launchArguments.get("sourcePaths"));
        }
        String singleMethod = (String)launchArguments.get("methodName");
        boolean testRun = (Boolean) launchArguments.getOrDefault("testRun", Boolean.FALSE);
        activeLaunchHandler.nbLaunch(file, preferProjActions, nativeImageFile, singleMethod, launchArguments, context, !noDebug, testRun, new OutputListener(context)).thenRun(() -> {
            activeLaunchHandler.postLaunch(launchArguments, context);
            resultFuture.complete(null);
        }).exceptionally(e -> {
            resultFuture.completeExceptionally(e);
            return null;
        });
        return resultFuture;
    }

    private static FileObject getFileObject(String filePath) {
        File ioFile = null;
        if (filePath != null) {
            ioFile = new File(filePath);
            if (!ioFile.exists()) {
                try {
                    URI uri = new URI(filePath);
                    ioFile = Utilities.toFile(uri);
                } catch (URISyntaxException ex) {
                    // Not a valid file
                }
            }
        }
        return ioFile != null ? FileUtil.toFileObject(ioFile) : null;
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
        // Project Action has already closed the I/O streams, and even in NetBeans IDE, the output area
        // is already inactive at this point.
        context.getClient().terminated(new TerminatedEventArguments());
    }

    private final class OutputListener implements Consumer<NbProcessConsole.ConsoleMessage> {

        private final DebugAdapterContext context;

        OutputListener(DebugAdapterContext context) {
            this.context = context;
        }

        @Override
        public void accept(NbProcessConsole.ConsoleMessage message) {
            if (message != null) {
                OutputEventArguments outputEvent = convertToOutputEventArguments(message.output, message.category, context);
                context.getClient().output(outputEvent);
            }
        }
    }
}
