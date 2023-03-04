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
package org.netbeans.modules.java.lsp.server.debugging;

import java.io.IOError;
import java.io.InputStream;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;

import org.netbeans.modules.java.lsp.server.LspSession;
import org.netbeans.modules.java.lsp.server.debugging.breakpoints.BreakpointsManager;
import org.netbeans.modules.java.lsp.server.debugging.launch.NbDebugSession;
import org.netbeans.modules.java.lsp.server.progress.LspInternalHandle;
import org.netbeans.modules.progress.spi.InternalHandle;
import org.openide.util.Pair;

public final class DebugAdapterContext {

    private final LspSession lspSession;
    private IDebugProtocolClient client;
    private NbDebugSession debugSession;
    private boolean clientLinesStartAt1 = true;
    private boolean clientColumnsStartAt1 = true;
    private final boolean debuggerLinesStartAt1 = true;
    private boolean clientPathsAreUri = false;
    private final boolean debuggerPathsAreUri = true;
    private boolean supportsRunInTerminalRequest = false;
    private boolean isAttached = false;
    private String[] sourcePaths;
    private Charset debuggeeEncoding;
    private boolean isVmStopOnEntry = false;
    private boolean isDebugMode = true;
    private InternalHandle processExecutorHandle;
    private Supplier<Writer> inputSinkProvider;

    private final AtomicInteger lastSourceReferenceId = new AtomicInteger(0);
    private final Map<Integer, Pair<URI, String>> sourcesById = new ConcurrentHashMap<>();
    private final Map<URI, Integer> sourceReferences = new ConcurrentHashMap<>();

    private final NBConfigurationSemaphore configurationSemaphore = new NBConfigurationSemaphore();
    private final NbSourceProvider sourceProvider = new NbSourceProvider(this);
    private final NbThreads threadsProvider = new NbThreads();
    private final BreakpointsManager breakpointManager = new BreakpointsManager(threadsProvider);

    DebugAdapterContext(LspSession lspSession) {
        this.lspSession = lspSession;
    }

    public LspSession getLspSession() {
        return lspSession;
    }

    public IDebugProtocolClient getClient() {
        return client;
    }

    void setClient(IDebugProtocolClient client) {
        this.client = client;
    }

    public NbDebugSession getDebugSession() {
        return debugSession;
    }

    public void setDebugSession(NbDebugSession session) {
        debugSession = session;
    }

    void setClientLinesStartAt1(Boolean clientLinesStartAt1) {
        if (clientLinesStartAt1 != null) {
            this.clientLinesStartAt1 = clientLinesStartAt1;
        }
    }

    void setClientColumnsStartAt1(Boolean clientColumnsStartAt1) {
        if (clientColumnsStartAt1 != null) {
            this.clientColumnsStartAt1 = clientColumnsStartAt1;
        }
    }

    public int getClientLine(int debuggerLine) {
        if (clientLinesStartAt1 == debuggerLinesStartAt1) {
            return debuggerLine;
        }
        if (clientLinesStartAt1) {
            return debuggerLine + 1;
        } else {
            return debuggerLine - 1;
        }
    }

    public int getDebuggerLine(int clientLine) {
        if (clientLinesStartAt1 == debuggerLinesStartAt1) {
            return clientLine;
        }
        if (debuggerLinesStartAt1) {
            return clientLine + 1;
        } else {
            return clientLine - 1;
        }
    }

    void setClientPathsAreUri(boolean clientPathsAreUri) {
        this.clientPathsAreUri = clientPathsAreUri;
    }
    
    public boolean requestProcessTermination() {
        InternalHandle ih;
        synchronized (this) {
            ih = processExecutorHandle;
        }
        if (ih != null) {
            ((LspInternalHandle)ih).forceRequestCancel();
            return true;
        } else {
            return false;
        }
    }
    
    public void setProcessExecutorHandle(InternalHandle h) {
        synchronized (this) {
            this.processExecutorHandle = h;
        }
    }

    public String getClientPath(String debuggerPath) {
        if (clientPathsAreUri == debuggerPathsAreUri) {
            return debuggerPath;
        }
        if (clientPathsAreUri) {
            return toURI(debuggerPath);
        } else {
            return toPath(debuggerPath);
        }
    }

    public String getDebuggerPath(String clientPath) {
        if (clientPathsAreUri == debuggerPathsAreUri) {
            return clientPath;
        }
        if (debuggerPathsAreUri) {
            return toURI(clientPath);
        } else {
            return toPath(clientPath);
        }
    }

    private static String toPath(String uri) {
        try {
            return Paths.get(new URI(uri)).toString();
        } catch (URISyntaxException | FileSystemNotFoundException | IllegalArgumentException | SecurityException e) {
            return null;
        }
    }

    private static String toURI(String path) {
        try {
            return Paths.get(path).toUri().toString();
        } catch (InvalidPathException | SecurityException | IOError e) {
            return null;
        }
    }

    public boolean isSupportsRunInTerminalRequest() {
        return supportsRunInTerminalRequest;
    }

    public void setSupportsRunInTerminalRequest(Boolean supportsRunInTerminalRequest) {
        if (supportsRunInTerminalRequest != null) {
            this.supportsRunInTerminalRequest = supportsRunInTerminalRequest;
        }
    }

    public boolean isAttached() {
        return isAttached;
    }

    public void setAttached(boolean isAttached) {
        this.isAttached = isAttached;
    }

    public String[] getSourcePaths() {
        return sourcePaths;
    }

    public void setSourcePaths(String[] sourcePaths) {
        this.sourcePaths = sourcePaths;
    }

    public URI getSourceUri(int sourceReference) {
        Pair<URI, String> sourceInfo = sourcesById.get(sourceReference);
        if (sourceInfo != null) {
            return sourceInfo.first();
        } else {
            return null;
        }
    }

    public String getSourceMimeType(int sourceReference) {
        Pair<URI, String> sourceInfo = sourcesById.get(sourceReference);
        if (sourceInfo != null) {
            return sourceInfo.second();
        } else {
            return null;
        }
    }

    public int createSourceReference(URI uri, String mimeType) {
        int id = sourceReferences.computeIfAbsent(uri, u -> lastSourceReferenceId.incrementAndGet());
        sourcesById.put(id, Pair.of(uri, mimeType));
        return id;
    }

    public void setDebuggeeEncoding(Charset encoding) {
        debuggeeEncoding = encoding;
    }

    public Charset getDebuggeeEncoding() {
        return debuggeeEncoding;
    }

    public boolean isVmStopOnEntry() {
        return isVmStopOnEntry;
    }

    public void setVmStopOnEntry(Boolean stopOnEntry) {
        if (stopOnEntry != null) {
            isVmStopOnEntry = stopOnEntry;
        }
    }

    public boolean isDebugMode() {
        return isDebugMode;
    }

    public void setDebugMode(boolean mode) {
        this.isDebugMode = mode;
    }

    public NBConfigurationSemaphore getConfigurationSemaphore() {
        return this.configurationSemaphore;
    }

    public NbSourceProvider getSourceProvider() {
        return this.sourceProvider;
    }

    public NbThreads getThreadsProvider() {
        return this.threadsProvider;
    }

    public BreakpointsManager getBreakpointManager() {
        return this.breakpointManager;
    }

    public Writer getInputSink() {
        return inputSinkProvider == null ? null : inputSinkProvider.get();
    }

    public void setInputSinkProvider(Supplier<Writer> inputSinkProvider) {
        this.inputSinkProvider = inputSinkProvider;
    }
}
