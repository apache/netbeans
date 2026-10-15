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
package org.netbeans.modules.java.lsp.server.progress;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.lsp4j.ProgressParams;
import org.eclipse.lsp4j.WorkDoneProgressBegin;
import org.eclipse.lsp4j.WorkDoneProgressEnd;
import org.eclipse.lsp4j.WorkDoneProgressNotification;
import org.eclipse.lsp4j.WorkDoneProgressReport;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.progress.spi.Controller;
import org.netbeans.modules.progress.spi.InternalHandle;
import org.netbeans.modules.progress.spi.ProgressEvent;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author sdedic
 */
public final class LspInternalHandle extends InternalHandle {
    private static final Logger LOG = Logger.getLogger(LspInternalHandle.class.getName());
    
    private final NbCodeLanguageClient  lspClient;
    private final OperationContext opContext;
    private final Function<InternalHandle, Controller> controllerProvider;
    private final Lookup operationLookup;
    /**
     * Code origin may help to map the Handle to an operation.
     */
    private final StackTraceElement[] creatorTrace;
    
    private CompletableFuture<Either<String, Integer>> tokenPromise;
    private int reportedPercentage;
    
    /**
     * Set from the START event handler. Workaround for NETBEANS-5167 if start event is skipped.
     */
    private boolean started;
    
    private boolean explicitCancelRequest;
    
    private static Field controllerField;
    
    public LspInternalHandle(OperationContext opContext, 
            NbCodeLanguageClient  lspClient, Function<InternalHandle, Controller> controllerProvider,
            String displayName, Cancellable cancel, boolean userInitiated) {
        super(displayName, cancel, userInitiated);
        this.creatorTrace = new Throwable().getStackTrace();
        this.lspClient = lspClient;
        this.opContext = opContext;
        this.controllerProvider = controllerProvider;
        this.operationLookup = Lookup.getDefault();
        if (opContext != null) {
            opContext.internalHandleCreated(this);
        }
    }

    public StackTraceElement[] getCreatorTrace() {
        return creatorTrace;
    }

    public Lookup getOperationLookup() {
        return operationLookup;
    }

    public void forceRequestCancel() {
        explicitCancelRequest = true;
        requestCancel();
    }

    @Override
    public boolean isAllowCancel() {
        return super.isAllowCancel() && (explicitCancelRequest || !opContext.isDisableCancels());
    }

    public OperationContext getContext() {
        return opContext;
    }
    
    private synchronized Controller findController() {
        if (controllerField == null) {
            try {
                controllerField = InternalHandle.class.getDeclaredField("controller");
                controllerField.setAccessible(true);
            } catch (NoSuchFieldException | SecurityException ex) {
                throw new IllegalStateException();
            }
        }
        try {
            return (Controller)controllerField.get(this);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    @Override
    public synchronized void start(String message, int workunits, long estimate) {
        Controller attached = findController();
        if (attached == null) {
            setController(controllerProvider.apply(this));
        }
        super.start(message, workunits, estimate);
    }

    @Override
    public void requestView() {
        // no op
    }

    @Override
    public boolean isCustomPlaced() {
        return false;
    }

    @Override
    public boolean isAllowView() {
        // by default not supported.
        return false;
    }
    
    String id() {
        return Integer.toHexString(System.identityHashCode(this));
    }
    
    void sendStartMessage(ProgressEvent e) {
        WorkDoneProgressBegin start  = new WorkDoneProgressBegin();
        boolean determinate = getTotalUnits() > 0;
        start.setCancellable(isAllowCancel());
        start.setTitle(getDisplayName());
        start.setMessage(e.getMessage());
        if (determinate) {
            double percent = e.getPercentageDone();
            if (percent != -1) {
                start.setPercentage(cleverFloor(percent));
            } else {
                start.setPercentage(0);
            }
        }
        LOG.log(Level.FINE, "Starting progress: {0}", this);
        started = true;
        notify(start);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LspProgress@").append(id()).append("[");
        sb.append("display: ").append(getDisplayName()).
            append(", total: ").append(getTotalUnits()).
            append(", percent: ").append(String.format("%3.2f", getPercentageDone())).
            append(", state: ").append(getState());
        sb.append("]");
        return sb.toString();
    }
    
    static int cleverFloor(double percent) {
        return (int)(percent > 90.0 ? Math.floor(percent) : Math.round(percent));
    }
    
    void sendProgress(ProgressEvent e) {
        if (!started) {
            sendStartMessage(e);
            return;
        }
        WorkDoneProgressReport report = new WorkDoneProgressReport();

        double percent = e.getPercentageDone();
        if (percent != -1) {
            report.setPercentage(cleverFloor(percent));
        }
        report.setMessage(e.getMessage());
        report.setCancellable(isAllowCancel());
        notify(report);
    }
    
    Either<String, Integer> token() {
        if (tokenPromise == null || tokenPromise.isCompletedExceptionally()) {
            return null;
        } else {
            return tokenPromise.getNow(null);
        }
    }
    
    void sendFinish(ProgressEvent e) {
        WorkDoneProgressEnd end = new WorkDoneProgressEnd();
        end.setMessage(e.getMessage());
        notify(end);
        opContext.removeHandle(token(), this);
    }
    
    CompletableFuture<Either<String, Integer>> findProgressToken() {
        if (tokenPromise != null) {
            return tokenPromise;
        }
        return tokenPromise = opContext.acquireOrObtainToken(this);
    }

    void notify(WorkDoneProgressNotification msg) {
        findProgressToken().thenAccept(token -> {
            LOG.log(Level.FINER, () ->
                    MessageFormat.format("Sending progress {0}, msg: {1}",
                        id(), msg
                    )
            );
            ProgressParams param = new ProgressParams(token, Either.forLeft(msg));
            lspClient.notifyProgress(param);
        });
    }
    
}
