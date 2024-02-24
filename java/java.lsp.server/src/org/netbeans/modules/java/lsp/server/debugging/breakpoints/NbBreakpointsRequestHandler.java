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
package org.netbeans.modules.java.lsp.server.debugging.breakpoints;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.lsp4j.debug.Breakpoint;
import org.eclipse.lsp4j.debug.SetBreakpointsArguments;
import org.eclipse.lsp4j.debug.SetBreakpointsResponse;
import org.eclipse.lsp4j.debug.SetExceptionBreakpointsArguments;
import org.eclipse.lsp4j.debug.SetExceptionBreakpointsResponse;
import org.eclipse.lsp4j.debug.Source;
import org.eclipse.lsp4j.debug.SourceBreakpoint;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;
import org.netbeans.modules.java.lsp.server.URITranslator;
import org.netbeans.modules.java.lsp.server.debugging.DebugAdapterContext;
import org.netbeans.modules.java.lsp.server.debugging.utils.ErrorUtilities;

/**
 *
 * @author martin
 */
public final class NbBreakpointsRequestHandler {

    public static final String CAUGHT_EXCEPTION_FILTER_NAME = "caught";
    public static final String UNCAUGHT_EXCEPTION_FILTER_NAME = "uncaught";

    private static final boolean IS_WINDOWS = System.getProperty("os.name", "").toLowerCase().startsWith("win");

    public CompletableFuture<SetBreakpointsResponse> setBreakpoints(SetBreakpointsArguments arguments, DebugAdapterContext context) {
        CompletableFuture<SetBreakpointsResponse> resultFuture = new CompletableFuture<>();
        if (context.getDebugSession() == null) {
            ErrorUtilities.completeExceptionally(resultFuture, "Empty debug session.", ResponseErrorCode.InvalidParams);
            return resultFuture;
        }
        Source source = arguments.getSource();
        String clientPath = source.getPath();
        if (IS_WINDOWS) {
            // Normalize the drive letter case:
            String drivePrefix = FilenameUtils.getPrefix(clientPath);
            if (drivePrefix != null && drivePrefix.length() >= 2 && Character.isLowerCase(drivePrefix.charAt(0)) && drivePrefix.charAt(1) == ':') {
                clientPath = Character.toUpperCase(clientPath.charAt(0)) + clientPath.substring(1);
            }
        }
        String sourcePath = null;
        if (clientPath != null) {
            sourcePath = context.getDebuggerPath(clientPath.trim());
        }
        if (sourcePath == null || sourcePath.isEmpty()) {
            ErrorUtilities.completeExceptionally(resultFuture,
                String.format("Failed to setBreakpoint, unresolved path '%s'.", clientPath),
                ResponseErrorCode.InvalidParams);
            return resultFuture;
        }
        sourcePath = URITranslator.getDefault().uriFromLSP(sourcePath);
        List<Breakpoint> res = new ArrayList<>();
        NbBreakpoint[] toAdds = this.convertClientBreakpointsToDebugger(source, sourcePath, arguments.getBreakpoints(), context);
        // Decode the URI if it comes encoded:
        NbBreakpoint[] added = context.getBreakpointManager().setBreakpoints(decodeURI(sourcePath), toAdds, arguments.getSourceModified());
        for (int i = 0; i < arguments.getBreakpoints().length; i++) {
            // For newly added breakpoint, should install it to debuggee first.
            if (toAdds[i] == added[i]) {
                added[i].install();
            } else {
                if (toAdds[i].getHitCount() != added[i].getHitCount()) {
                    // Update hitCount condition.
                    added[i].setHitCount(toAdds[i].getHitCount());
                }

                if (!StringUtils.equals(toAdds[i].getLogMessage(), added[i].getLogMessage())) {
                    added[i].setLogMessage(toAdds[i].getLogMessage());
                }

                if (!StringUtils.equals(toAdds[i].getCondition(), added[i].getCondition())) {
                    added[i].setCondition(toAdds[i].getCondition());
                }

            }
            res.add(added[i].convertDebuggerBreakpointToClient());
        }
        SetBreakpointsResponse response = new SetBreakpointsResponse();
        response.setBreakpoints(res.toArray(new Breakpoint[0]));
        resultFuture.complete(response);
        return resultFuture;
    }

    private static String decodeURI(String uri) {
        try {
            return URLDecoder.decode(uri, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return uri;
        }
    }

    public CompletableFuture<SetExceptionBreakpointsResponse> setExceptionBreakpoints(SetExceptionBreakpointsArguments arguments, DebugAdapterContext context) {
        CompletableFuture<SetExceptionBreakpointsResponse> resultFuture = new CompletableFuture<>();
        if (context.getDebugSession() == null) {
            ErrorUtilities.completeExceptionally(resultFuture, "Empty debug session.", ResponseErrorCode.InvalidParams);
            return resultFuture;
        }
        String[] filters = arguments.getFilters();
        boolean notifyCaught = ArrayUtils.contains(filters, CAUGHT_EXCEPTION_FILTER_NAME);
        boolean notifyUncaught = ArrayUtils.contains(filters, UNCAUGHT_EXCEPTION_FILTER_NAME);
        context.getBreakpointManager().setExceptionBreakpoints(notifyCaught, notifyUncaught);
        SetExceptionBreakpointsResponse response = new SetExceptionBreakpointsResponse();
        // TODO: response.setBreakpoints(...);
        return CompletableFuture.completedFuture(response);
    }

    private NbBreakpoint[] convertClientBreakpointsToDebugger(Source source, String sourceFile, SourceBreakpoint[] sourceBreakpoints, DebugAdapterContext context) {
        int n = sourceBreakpoints.length;
        int[] lines = new int[n];
        for (int i = 0; i < n; i++) {
            lines[i] = context.getDebuggerLine(sourceBreakpoints[i].getLine());
        }
        NbBreakpoint[] breakpoints = new NbBreakpoint[n];
        for (int i = 0; i < n; i++) {
            int hitCount = 0;
            try {
                hitCount = Integer.parseInt(sourceBreakpoints[i].getHitCondition());
            } catch (NumberFormatException e) {
                hitCount = 0; // If hitCount is not a number, ignore the hitCount.
            }
            breakpoints[i] = new NbBreakpoint(source, sourceFile, lines[i], hitCount, sourceBreakpoints[i].getCondition(), sourceBreakpoints[i].getLogMessage(), context);
        }
        return breakpoints;
    }
}
