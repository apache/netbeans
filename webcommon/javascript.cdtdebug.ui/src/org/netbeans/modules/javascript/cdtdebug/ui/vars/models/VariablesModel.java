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

package org.netbeans.modules.javascript.cdtdebug.ui.vars.models;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.lib.chrome_devtools_protocol.debugger.CallFrame;
import org.netbeans.lib.chrome_devtools_protocol.debugger.EvaluateOnCallFrameRequest;
import org.netbeans.lib.chrome_devtools_protocol.debugger.Scope;
import org.netbeans.lib.chrome_devtools_protocol.debugger.SetVariableValueRequest;
import org.netbeans.lib.chrome_devtools_protocol.runtime.CallArgument;
import org.netbeans.lib.chrome_devtools_protocol.runtime.GetPropertiesRequest;
import org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject;
import org.netbeans.modules.javascript.cdtdebug.CDTDebugger;
import org.netbeans.modules.javascript.cdtdebug.CDTDebuggerEngineProvider;
import org.netbeans.modules.javascript.cdtdebug.vars.CDTEvaluator;
import org.netbeans.modules.javascript.cdtdebug.vars.Variable;
import org.netbeans.modules.javascript2.debug.ui.models.ViewModelSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.DebuggerServiceRegistrations;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.TableHTMLModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;

import static java.util.Arrays.asList;


@DebuggerServiceRegistrations({
  @DebuggerServiceRegistration(path=CDTDebuggerEngineProvider.ENGINE_NAME+"/LocalsView",
                               types={ TreeModel.class, ExtendedNodeModel.class, TableModel.class }),
  @DebuggerServiceRegistration(path=CDTDebuggerEngineProvider.ENGINE_NAME+"/ToolTipView",
                               types={ TreeModel.class, ExtendedNodeModel.class, TableModel.class })
})
public class VariablesModel extends ViewModelSupport implements TreeModel,
                                                                ExtendedNodeModel,
                                                                TableModel,
                                                                TableHTMLModel,
                                                                CDTDebugger.Listener {

    private static final Set<String> WRITABLE_SCOPES = new HashSet<>(asList("local", "closure", "catch"));

    //@StaticResource(searchClasspath = true)
    private static final String ICON_LOCAL = "org/netbeans/modules/debugger/resources/localsView/local_variable_16.png"; // NOI18N
    @StaticResource(searchClasspath = true)
    private static final String ICON_SCOPE = "org/netbeans/modules/javascript2/debug/ui/resources/global_variable_16.png"; // NOI18N

    protected final CDTDebugger dbg;
    private volatile boolean topFrameRefreshed;

    public VariablesModel(ContextProvider contextProvider) {
        dbg = contextProvider.lookupFirst(null, CDTDebugger.class);
        dbg.addListener(this);
    }

    @Override
    public Object getRoot() {
        return ROOT;
    }

    @NbBundle.Messages({"# {0} - argument number", "CTL_Argument=Argument {0}"})
    @Override
    public Object[] getChildren(Object parent, int from, int to) throws UnknownTypeException {
        if (parent == ROOT) {
            CallFrame frame = dbg.getCurrentFrame();
            if (frame == null) {
                return EMPTY_CHILDREN;
            }
            List<Scope> scopes = new ArrayList<>(frame.getScopeChain());
            if (!scopes.isEmpty()) {
                Scope topScope = scopes.remove(0);
                Object[] topChildren = getObjectChildren(topScope, "", topScope.getObject());
                Object[] result = new Object[topChildren.length + scopes.size()];
                System.arraycopy(topChildren, 0, result, 0, topChildren.length);
                for(int i = 0; i < scopes.size(); i++) {
                    result[i + topChildren.length] = scopes.get(i);
                }
                return result;
            } else {
                return EMPTY_CHILDREN;
            }
        } else if (parent instanceof Variable) {
            if(((Variable) parent).getValue().getObjectId() == null) {
                return EMPTY_CHILDREN;
            } else {
                Variable v = (Variable) parent;
                String newParentPath;
                if(v.getParentPath().isEmpty()) {
                    newParentPath = v.getName();
                } else {
                    newParentPath = v.getParentPath() + "['" + v.getName() + "']";
                }
                return getObjectChildren(v.getScope(), newParentPath, v.getValue());
            }
        } else if (parent instanceof Scope) {
            Scope s = (Scope) parent;
            return getObjectChildren(s, "", s.getObject());
        } else {
            return EMPTY_CHILDREN;
        }
    }

    protected final Object[] getObjectChildren(Scope scope, String parentObjectPath, RemoteObject remoteObject) {
        try {
            return dbg.getConnection()
                    .getRuntime()
                    .getProperties(new GetPropertiesRequest(
                            remoteObject.getObjectId(), true
                    ))
                    .toCompletableFuture()
                    .get()
                    .getResult()
                    .stream()
                    .map(pd -> new Variable(
                            scope,
                            pd.getName(),
                            parentObjectPath,
                            pd.getValue()))
                    .collect(Collectors.toList())
                    .toArray();
        } catch (InterruptedException | ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
        return new Object[0];
    }

    @Override
    public boolean isLeaf(Object node) {
        return isLeaf2(node);
    }

    public static boolean isLeaf2(Object node) {
        if (node == ROOT) {
            return false;
        } else if (node instanceof Scope) {
            return false;
        } else if (node instanceof Variable) {
            return isLeaf2(((Variable) node).getValue());
        } else if (node instanceof RemoteObject) {
            return ((RemoteObject) node).getObjectId() == null;
        } else {
            return true;
        }
    }

    @Override
    public int getChildrenCount(Object node) throws UnknownTypeException {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean canRename(Object node) throws UnknownTypeException {
        return false;
    }

    @Override
    public boolean canCopy(Object node) throws UnknownTypeException {
        return false;
    }

    @Override
    public boolean canCut(Object node) throws UnknownTypeException {
        return false;
    }

    @Override
    public Transferable clipboardCopy(Object node) throws IOException, UnknownTypeException {
        return null;
    }

    @Override
    public Transferable clipboardCut(Object node) throws IOException, UnknownTypeException {
        return null;
    }

    @Override
    public PasteType[] getPasteTypes(Object node, Transferable t) throws UnknownTypeException {
        return null;
    }

    @Override
    public void setName(Object node, String name) throws UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getIconBaseWithExtension(Object node) throws UnknownTypeException {
        if (node instanceof Scope) {
            return ICON_SCOPE;
        }
        return ICON_LOCAL;
    }

    @Override
    public String getDisplayName(Object node) throws UnknownTypeException {
        if (node instanceof Variable) {
            return ((Variable) node).getName();
        }
        if (node instanceof Scope) {
            Scope scope = (Scope) node;
            String text = scope.getName();
            if (text == null) {
                text = scope.getType();
            }
            return text + " Scope";
        }
        throw new UnknownTypeException(node);
    }

    @Override
    public String getIconBase(Object node) throws UnknownTypeException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String getShortDescription(Object node) throws UnknownTypeException {
        if (node instanceof Variable) {
            Variable var = (Variable) node;
            RemoteObject value = var.getValue();
            if (value == null) {
                return null;
            }
            String strVal = CDTEvaluator.getStringValue(value);
            return var.getName() + " = " + strVal;
        }
        return null;
    }

    @Override
    public Object getValueAt(Object node, String columnID) throws UnknownTypeException {
        if (node == ROOT) {
            return "";
        } else if (node instanceof Variable) {
            if (Constants.LOCALS_VALUE_COLUMN_ID.equals(columnID) ||
                Constants.LOCALS_TO_STRING_COLUMN_ID.equals(columnID)) {
                return CDTEvaluator.getStringValue(((Variable) node).getValue());
            } else if (Constants.LOCALS_TYPE_COLUMN_ID.equals(columnID)) {
                return CDTEvaluator.getStringType(((Variable) node).getValue());
            }
        } else if (node instanceof Scope) {
            return "";
        }
        throw new UnknownTypeException(node);
    }

    @Override
    public boolean hasHTMLValueAt(Object node, String columnID) throws UnknownTypeException {
        return Constants.LOCALS_VALUE_COLUMN_ID.equals(columnID)
                || Constants.LOCALS_TO_STRING_COLUMN_ID.equals(columnID)
                || Constants.LOCALS_TYPE_COLUMN_ID.equals(columnID);
    }

    @Override
    public String getHTMLValueAt(Object node, String columnID) throws UnknownTypeException {
        if (node == ROOT) {
            return "";
        } else if (node instanceof Variable) {
            if (Constants.LOCALS_VALUE_COLUMN_ID.equals(columnID) ||
                Constants.LOCALS_TO_STRING_COLUMN_ID.equals(columnID)) {
                return toHTML(CDTEvaluator.getStringValue(((Variable) node).getValue()));
            } else if (Constants.LOCALS_TYPE_COLUMN_ID.equals(columnID)) {
                return toHTML(CDTEvaluator.getStringType(((Variable) node).getValue()));
            }
        } else if (node instanceof Scope) {
            return "";
        }
        throw new UnknownTypeException(node);
    }

    @Override
    public boolean isReadOnly(Object node, String columnID) throws UnknownTypeException {
        if ((Constants.LOCALS_VALUE_COLUMN_ID.equals(columnID)
                || Constants.LOCALS_TO_STRING_COLUMN_ID.equals(columnID))
                && node instanceof Variable) {
            Variable v = (Variable) node;
            return !(v.getScope() != null
                    && WRITABLE_SCOPES.contains(v.getScope().getType())
                    && v.getParentPath().isEmpty());
        }
        return true;
    }

    @Override
    public void setValueAt(final Object node, String columnID, Object value) throws UnknownTypeException {
        if (Objects.equals(value, getValueAt(node, columnID))) {
            return;
        }
        if (!(value instanceof String)) {
            throw new UnknownTypeException("Accepting String values only. Not "+value);
        }
        if (Constants.LOCALS_VALUE_COLUMN_ID.equals(columnID) && node instanceof Variable) {
            final Variable var = (Variable) node;
            CallFrame cf = dbg.getCurrentFrame();
            if (cf == null) {
                return;
            }
            int scopeNumber = dbg.getCurrentFrame().getScopeChain().indexOf(var.getScope());

            if(scopeNumber < 0) {
                return;
            }

            String frameId = dbg.getCurrentFrame().getCallFrameId();

            EvaluateOnCallFrameRequest req = new EvaluateOnCallFrameRequest(frameId, (String) value);

            dbg.getConnection().getDebugger().evaluateOnCallFrame(req)
                    .thenCompose(eocfr -> {
                        if(eocfr.getExceptionDetails() != null) {
                            throw new RuntimeException(eocfr.getExceptionDetails().getException().getDescription());
                        }
                        CallArgument ca = new CallArgument();
                        ca.setObjectId(eocfr.getResult().getObjectId());
                        ca.setUnserializableValue(eocfr.getResult().getUnserializableValue());
                        ca.setValue(eocfr.getResult().getValue());
                        SetVariableValueRequest svvr = new SetVariableValueRequest();
                        svvr.setCallFrameId(frameId);
                        svvr.setNewValue(ca);
                        svvr.setScopeNumber(scopeNumber);
                        svvr.setVariableName(var.getName());
                        return dbg.getConnection()
                                .getDebugger()
                                .setVariableValue(svvr)
                                .thenApply((svvr2) -> eocfr);
                    })
                    .handle((eocfr, thr) -> {
                        if(thr instanceof CompletionException) {
                            thr = thr.getCause();
                        }
                        if (thr != null) {
                            DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(thr.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
                        } else {
                            var.setValue(eocfr.getResult());
                            fireChangeEvent(new ModelEvent.TableValueChanged(VariablesModel.this, node, null, ModelEvent.TableValueChanged.VALUE_MASK));
                        }
                        return null;
                    });
        }
    }

    @Override
    public void notifySuspended(boolean suspended) {
        refresh();
        topFrameRefreshed = suspended;
    }

    @Override
    public void notifyCurrentFrame(CallFrame cf) {
        if (cf == null) {
            return ;
        }
        if (topFrameRefreshed && cf == dbg.getCurrentCallStack().get(0)) {
            return ;
        }
        topFrameRefreshed = false;
        refresh();
    }

    @Override
    public void notifyFinished() {

    }

}
