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
package org.netbeans.modules.java.lsp.server.debugging.attach;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector.Argument;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.debug.TerminatedEventArguments;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.AttachingDICookie;
import org.netbeans.api.debugger.jpda.DebuggerStartException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.java.lsp.server.debugging.DebugAdapterContext;
import org.netbeans.modules.java.lsp.server.debugging.launch.NbDebugSession;
import org.netbeans.modules.java.lsp.server.debugging.utils.ErrorUtilities;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Martin Entlicher
 */
public final class NbAttachRequestHandler {

    private static final String CONNECTOR_ARG_PID = "pid";          // NOI18N
    private static final String CONNECTOR_ARG_HOST = "hostname";    // NOI18N
    private static final String CONNECTOR_ARG_PORT = "port";        // NOI18N
    private static final String CONNECTOR_ARG_NAME = "name";        // NOI18N

    private static final Map<String, String> ATTR_CONFIG_TO_CONNECTOR = Stream.of(new String[][] {
        { ConfigurationAttributes.PROCESS_ARG_PID, CONNECTOR_ARG_PID },
        { ConfigurationAttributes.SOCKET_ARG_HOST, CONNECTOR_ARG_HOST },
        { ConfigurationAttributes.SOCKET_ARG_PORT, CONNECTOR_ARG_PORT },
        { ConfigurationAttributes.SHMEM_ARG_NAME, CONNECTOR_ARG_NAME },
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    private static final RequestProcessor RP = new RequestProcessor(AttachConfigurations.class);

    public CompletableFuture<Void> attach(Map<String, Object> attachArguments, DebugAdapterContext context) {
        boolean isNative = "nativeimage".equals(attachArguments.get("type"));   // NOI18N
        if (isNative) {
            return attachToNative(attachArguments, context);
        } else {
            return attachToJVM(attachArguments, context);
        }
    }

    private CompletableFuture<Void> attachToNative(Map<String, Object> attachArguments, DebugAdapterContext context) {
        CompletableFuture<Void> resultFuture = new CompletableFuture<>();
        // TODO
        ErrorUtilities.completeExceptionally(resultFuture,
                "Attach to native image is not implemented yet", ResponseErrorCode.serverErrorStart);
        return resultFuture;
    }

    @Messages({"# {0} - connector name", "MSG_InvalidConnector=Invalid connector name: {0}"})
    private CompletableFuture<Void> attachToJVM(Map<String, Object> attachArguments, DebugAdapterContext context) {
        CompletableFuture<Void> resultFuture = new CompletableFuture<>();
        ConfigurationAttributes configurationAttributes = AttachConfigurations.get().findConfiguration(attachArguments);
        if (configurationAttributes != null) {
            AttachingConnector connector = configurationAttributes.getConnector();
            RP.post(() -> attachTo(connector, attachArguments, context, resultFuture));
        } else {
            context.setDebugMode(true);
            String name = (String) attachArguments.get("name");     // NOI18N
            ErrorUtilities.completeExceptionally(resultFuture,
                    Bundle.MSG_InvalidConnector(name),
                    ResponseErrorCode.serverErrorStart);
        }
        return resultFuture;
    }

    @Messages({"# {0} - argument name", "# {1} - value", "MSG_ConnectorInvalidValue=Invalid value of {0}: {1}"})
    private void attachTo(AttachingConnector connector, Map<String, Object> arguments, DebugAdapterContext context, CompletableFuture<Void> resultFuture) {
        Map<String, Argument> args = connector.defaultArguments();
        for (String argName : arguments.keySet()) {
            String argNameTranslated = ATTR_CONFIG_TO_CONNECTOR.getOrDefault(argName, argName);
            Argument arg = args.get(argNameTranslated);
            if (arg == null) {
                continue;
            }
            String value = arguments.get(argName).toString();
            if (!arg.isValid(value)) {
                ErrorUtilities.completeExceptionally(resultFuture,
                    Bundle.MSG_ConnectorInvalidValue(argName, value),
                    ResponseErrorCode.serverErrorStart);
                return ;
            }
            arg.setValue(value);
        }
        AttachingDICookie attachingCookie = AttachingDICookie.create(connector, args);
        resultFuture.complete(null);
        startAttaching(attachingCookie, context);
    }

    @Messages("MSG_FailedToAttach=Failed to attach.")
    private void startAttaching(AttachingDICookie attachingCookie, DebugAdapterContext context) {
        DebuggerEngine[] es = DebuggerManager.getDebuggerManager ().startDebugging(
            DebuggerInfo.create(AttachingDICookie.ID, new Object [] { attachingCookie })
        );
        if (es.length > 0) {
            JPDADebugger debugger = es[0].lookupFirst(null, JPDADebugger.class);
            if (debugger != null) {
                Session session = es[0].lookupFirst(null, Session.class);
                NbDebugSession debugSession = new NbDebugSession(session);
                context.setDebugSession(debugSession);
                AtomicBoolean finished = new AtomicBoolean(false);
                debugger.addPropertyChangeListener(JPDADebugger.PROP_STATE, new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        int newState = (int) evt.getNewValue();
                        if (newState == JPDADebugger.STATE_DISCONNECTED) {
                            if (!finished.getAndSet(true)) {
                                notifyTerminated(context);
                            }
                        }
                    }
                });
                boolean success = false;
                try {
                    debugger.waitRunning();
                    success = debugger.getState() != JPDADebugger.STATE_DISCONNECTED;
                } catch (DebuggerStartException ex) {
                    notifyErrorMessage(context, ex.getLocalizedMessage());
                }
                if (!success) {
                    if (!finished.getAndSet(true)) {
                        notifyTerminated(context);
                    }
                } else {
                    context.getClient().initialized();
                }
                return ;
            }
        }
        notifyErrorMessage(context, Bundle.MSG_FailedToAttach());
        notifyTerminated(context);
    }

    private void notifyErrorMessage(DebugAdapterContext context, String message) {
        MessageParams params = new MessageParams();
        params.setMessage(message);
        params.setType(MessageType.Error);
        context.getLspSession().getLookup().lookup(NbCodeLanguageClient.class).showMessage(params);
    }

    private void notifyTerminated(DebugAdapterContext context) {
        context.getClient().terminated(new TerminatedEventArguments());
    }
}
