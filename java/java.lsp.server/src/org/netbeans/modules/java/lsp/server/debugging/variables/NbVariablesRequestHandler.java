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
package org.netbeans.modules.java.lsp.server.debugging.variables;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.lsp4j.debug.SetVariableArguments;
import org.eclipse.lsp4j.debug.SetVariableResponse;
import org.eclipse.lsp4j.debug.Variable;
import org.eclipse.lsp4j.debug.VariablesArguments;
import org.eclipse.lsp4j.debug.VariablesResponse;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;
import org.netbeans.api.debugger.Session;

import org.netbeans.modules.java.lsp.server.debugging.DebugAdapterContext;
import org.netbeans.modules.java.lsp.server.debugging.NbProtocolServer;
import org.netbeans.modules.java.lsp.server.debugging.NbScope;
import org.netbeans.modules.java.lsp.server.debugging.launch.NbDebugSession;
import org.netbeans.modules.java.lsp.server.debugging.utils.ErrorUtilities;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.RequestProcessor;

/**
 *
 * @author martin
 */
public final class NbVariablesRequestHandler {

    private static final Logger LOGGER = Logger.getLogger(NbProtocolServer.class.getName());
    private static final Level LOGLEVEL = Level.FINE;

    private static final String LOCALS_VIEW_NAME = "LocalsView";
    private static final String LOCALS_VALUE_COLUMN_ID = "LocalsValue";
    private static final String LOCALS_TO_STRING_COLUMN_ID = "LocalsToString";
    private static final String LOCALS_TYPE_COLUMN_ID = "LocalsType";

    private final ViewModel.Provider localsModelProvider;
    private final RequestProcessor variablesRP = new RequestProcessor(NbVariablesRequestHandler.class.getName(), 2);

    public NbVariablesRequestHandler() {
        this.localsModelProvider = new ViewModel.Provider(LOCALS_VIEW_NAME);
    }

    public CompletableFuture<VariablesResponse> variables(VariablesArguments arguments, DebugAdapterContext context) {
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.log(LOGLEVEL, "variables() START");
            long t1 = System.nanoTime();
            VariablesResponse response = new VariablesResponse();
            Object container = context.getThreadsProvider().getThreadObjects().getObject(arguments.getVariablesReference());
            if (container == null) {
                // Nothing, or an old container
                response.setVariables(new Variable[0]);
            } else {
                Session session = context.getDebugSession().getSession();
                Models.CompoundModel localsModel = localsModelProvider.getModel(session);
                int threadId;
                if (container instanceof NbScope) {
                    threadId = ((NbScope) container).getFrame().getThreadId();
                    container = localsModel.getRoot();
                } else {
                    threadId = context.getThreadsProvider().getThreadObjects().findObjectThread(arguments.getVariablesReference());
                }
                List<Variable> list = new ArrayList<>();
                try {
                    Object[] children;
                    int count = arguments.getCount() != null ? arguments.getCount() : 0;
                    if (count > 0) {
                        int start = arguments.getStart() != null ? arguments.getStart() : 0;
                        children = localsModel.getChildren(container, start, start + count);
                    } else {
                        children = localsModel.getChildren(container, 0, Integer.MAX_VALUE);
                    }
                    for (Object child : children) {
                        String name = localsModel.getDisplayName(child);
                        String value;
                        try {
                            value = String.valueOf(localsModel.getValueAt(child, LOCALS_TO_STRING_COLUMN_ID));
                        } catch (UnknownTypeException ex) {
                            value = String.valueOf(localsModel.getValueAt(child, LOCALS_VALUE_COLUMN_ID));
                        }
                        String type = String.valueOf(localsModel.getValueAt(child, LOCALS_TYPE_COLUMN_ID));
                        Variable variable = new Variable();
                        variable.setName(name);
                        variable.setValue(value);
                        variable.setType(type);
                        if (!localsModel.isLeaf(child)) {
                            int id = context.getThreadsProvider().getThreadObjects().addObject(threadId, child);
                            variable.setVariablesReference(id);
                        }
                        list.add(variable);
                    }
                } catch (UnknownTypeException e) {
                    throw ErrorUtilities.createResponseErrorException(e.getMessage(), ResponseErrorCode.InternalError);
                }
                response.setVariables(list.toArray(new Variable[0]));
            }
            long t2 = System.nanoTime();
            LOGGER.log(LOGLEVEL, "variables() END after {0} ns", (t2 - t1));
            return response;
        }, variablesRP);
    }

    public CompletableFuture<SetVariableResponse> setVariable(SetVariableArguments args, DebugAdapterContext context) {
        CompletableFuture<SetVariableResponse> future = new CompletableFuture<>();
        if (StringUtils.isBlank(args.getValue())) {
            ErrorUtilities.completeExceptionally(future,
                "SetVariablesRequest: property 'value' is missing, null, or empty",
                ResponseErrorCode.InvalidParams);
            return future;
        } else if (args.getVariablesReference() == -1) {
            ErrorUtilities.completeExceptionally(future,
                "SetVariablesRequest: property 'variablesReference' is missing, null, or empty",
                ResponseErrorCode.InvalidParams);
            return future;
        } else if (StringUtils.isBlank(args.getName())) {
            ErrorUtilities.completeExceptionally(future,
                "SetVariablesRequest: property 'name' is missing, null, or empty",
                ResponseErrorCode.InvalidParams);
            return future;
        }

        Object container = context.getThreadsProvider().getThreadObjects().getObject(args.getVariablesReference());
        // container is null means the stack frame is continued by user manually.
        if (container == null) {
            ErrorUtilities.completeExceptionally(future,
                "Failed to set variable. Reason: Cannot set value because the thread is resumed.",
                ResponseErrorCode.InternalError);
            return future;
        }

        Session session = context.getDebugSession().getSession();
        Models.CompoundModel localsModel = localsModelProvider.getModel(session);

        int threadId;
        if (container instanceof NbScope) {
            threadId = ((NbScope) container).getFrame().getThreadId();
            container = localsModel.getRoot();
        } else {
            threadId = context.getThreadsProvider().getThreadObjects().findObjectThread(args.getVariablesReference());
        }
        String varName = args.getName();
        // We need to search for varName in the container:
        try {
            Object[] children = localsModel.getChildren(container, 0, Integer.MAX_VALUE);
            Object varChild = null;
            for (Object child : children) {
                String name = localsModel.getDisplayName(child);
                if (varName.equals(name)) {
                    varChild = child;
                    break;
                }
            }
            if (varChild != null) {
                localsModel.setValueAt(varChild, LOCALS_VALUE_COLUMN_ID, args.getValue());
                String value = String.valueOf(localsModel.getValueAt(varChild, LOCALS_TO_STRING_COLUMN_ID));
                String type = String.valueOf(localsModel.getValueAt(varChild, LOCALS_TYPE_COLUMN_ID));
                int id = context.getThreadsProvider().getThreadObjects().addObject(threadId, varChild);
                SetVariableResponse response = new SetVariableResponse();
                response.setType(type);
                response.setValue(value);
                response.setVariablesReference(id);
                response.setIndexedVariables(0);
                future.complete(response);
            } else {
                ErrorUtilities.completeExceptionally(future,
                    String.format("SetVariableRequest: Variable %s cannot be found.", varName),
                    ResponseErrorCode.InternalError);
            }
        } catch (UnknownTypeException e) {
            ErrorUtilities.completeExceptionally(future, e.getMessage(), ResponseErrorCode.InternalError);
        }
        return future;
    }
}
