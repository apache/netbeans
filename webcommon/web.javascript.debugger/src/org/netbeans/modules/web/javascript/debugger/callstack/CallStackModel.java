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

package org.netbeans.modules.web.javascript.debugger.callstack;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JToolTip;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript2.debug.NamesTranslator;
import org.netbeans.modules.javascript2.debug.ui.models.ViewModelSupport;
import org.netbeans.modules.web.common.api.ServerURLMapping;
import org.netbeans.modules.web.javascript.debugger.browser.ProjectContext;
import org.netbeans.modules.web.javascript.debugger.eval.VarNamesTranslatorFactory;
import org.netbeans.modules.web.webkit.debugging.api.Debugger;
import org.netbeans.modules.web.webkit.debugging.api.debugger.CallFrame;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.DebuggerServiceRegistrations;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

@NbBundle.Messages({
    "CTL_CallstackModel_Column_Name_Name=Name"
})
@DebuggerServiceRegistrations({
 @DebuggerServiceRegistration(path="javascript-debuggerengine/DebuggingView",
                              types={ TreeModel.class, NodeModel.class, TableModel.class }),
 @DebuggerServiceRegistration(path="javascript-debuggerengine/CallStackView",
                              types={ TreeModel.class, NodeModel.class, TableModel.class })
})
public final class CallStackModel extends ViewModelSupport implements TreeModel, NodeModel,
        TableModel, Debugger.Listener, PropertyChangeListener {

    //@StaticResource(searchClasspath = true)
    public static final String CALL_STACK =
            "org/netbeans/modules/debugger/resources/callStackView/NonCurrentFrame"; // NOI18N
    //@StaticResource(searchClasspath = true)
    public static final String CURRENT_CALL_STACK =
            "org/netbeans/modules/debugger/resources/callStackView/CurrentFrame"; // NOI18N
    //@StaticResource(searchClasspath = true)
    private static final String ICON_EMPTY =
            "org/netbeans/modules/debugger/resources/empty";                // NOI18N

    private static final Object DBG_RUNNING_NODE = new Object();
    
    private Debugger debugger;
    private ProjectContext pc;

    private AtomicReference<List<? extends CallFrame>> stackTrace = 
            new AtomicReference<List<? extends CallFrame>>(new ArrayList<CallFrame>());
    
    public CallStackModel(final ContextProvider contextProvider) {
        debugger = contextProvider.lookupFirst(null, Debugger.class);
        pc = contextProvider.lookupFirst(null, ProjectContext.class);
        debugger.addListener(this);
        debugger.addPropertyChangeListener(this);
        // update now:
        setStackTrace(debugger.isSuspended() ? debugger.getCurrentCallStack() : null);
    }

    private void setStackTrace(List<? extends CallFrame> stackTrace) {
        if (stackTrace == null) {
            this.stackTrace.set(null);
        } else {
            List<CallFrame> l = new ArrayList<CallFrame>();
            for (CallFrame cf : stackTrace) {
                if (cf.getScript() != null) {
                    l.add(cf);
                }
            }
            this.stackTrace.set(l);
        }
    }
    
    // TreeModel implementation ................................................

    @Override
    public Object getRoot() {
        return ROOT;
    }

    @Override
    public Object[] getChildren(Object parent, int from, int to)
            throws UnknownTypeException {
        if (parent == ROOT) {
            List<? extends CallFrame> list = stackTrace.get();
            if ( list == null ){
                return new Object[] { DBG_RUNNING_NODE };
            }
            else {
                if ( from >= list.size() ) {
                    return new Object[0];
                }
                int end = Math.min( list.size(), to);
                List<? extends CallFrame> stack = list.subList( from , end );
                return stack.toArray();
            }
        }
        
        throw new UnknownTypeException(parent);
    }

    @Override
    public boolean isLeaf(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return false;
        } else if (node instanceof CallFrame) {
            return true;
        }
        if (node == DBG_RUNNING_NODE) {
            return true;
        }
        
        throw new UnknownTypeException(node);
    }

    @Override
    public int getChildrenCount(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            List<? extends CallFrame> list = stackTrace.get();
            if ( list == null ){
                return 1;
            }
            else {
                return list.size();
            }
        }
        
        throw new UnknownTypeException(node);
    }

    @NbBundle.Messages({"LBL_AnonymousFunction=(anonymous function)",
                        "CTL_DebuggerRunning=Program is Running..."})
    @Override
    public String getDisplayName(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return Bundle.CTL_CallstackModel_Column_Name_Name();
        } else if (node instanceof CallFrame) {
            CallFrame frame = (CallFrame) node;
            String frameName = frame.getFunctionName();
            if (frameName.isEmpty()) {
                frameName = Bundle.LBL_AnonymousFunction();
            } else {
                NamesTranslator namesTranslator = VarNamesTranslatorFactory.get(frame, debugger, pc.getProject()).getNamesTranslator();
                if (namesTranslator != null) {
                    frameName = namesTranslator.translateDeclarationNodeName(frameName);
                }
            }
            if (frame == debugger.getCurrentCallFrame()) {
                return toHTML(frameName, true, false, null);
            } else {
                return frameName;
            }
        } else if (node == DBG_RUNNING_NODE) {
            return Bundle.CTL_DebuggerRunning();
        } else {
            throw new UnknownTypeException(node);
        }
    }

    @Override
    public String getIconBase(Object node) throws UnknownTypeException {
        if (node instanceof CallFrame) {
            CallFrame curStack = debugger.getCurrentCallFrame();
            if (curStack == node) {
                return CURRENT_CALL_STACK;
            } else {
                return CALL_STACK;
            }
        } else if (node == ROOT) {
            return null;
        } else if (node == DBG_RUNNING_NODE) {
            return ICON_EMPTY;
        }
        throw new UnknownTypeException(node);
    }

    @NbBundle.Messages("CTL_DebuggerRunningDescr=No stack trace while program is running.")
    @Override
    public String getShortDescription(Object node)
            throws UnknownTypeException {
        if (node instanceof CallFrame) {
            CallFrame frame = (CallFrame)node;
            return frame.getScript().getURL() + ":" + (frame.getLineNumber()+1);
        } else if (node == DBG_RUNNING_NODE) {
            return Bundle.CTL_DebuggerRunningDescr();
        }
        return null;
    }

    // TableModel implementation ...............................................

    @Override
    public Object getValueAt(Object node, String columnID)
            throws UnknownTypeException {
        if ( columnID.equals(Constants.CALL_STACK_FRAME_LOCATION_COLUMN_ID) ) {
            String file;
            CallFrame frame;
            if (node instanceof CallFrame) {
                frame = (CallFrame) node;
                file = frame.getScript().getURL();
                if (!file.isEmpty()) {
                    FileObject fo = null;
                    try {
                        URI uri = URI.create(file);
                        if (uri.isAbsolute()) {
                            URL url = uri.toURL();
                            Project project = pc.getProject();
                            if (project != null) {
                                fo = ServerURLMapping.fromServer(project, url);
                            }
                        }
                    } catch (MalformedURLException ex) {
                    } catch (IllegalArgumentException iaex) {
                        Exceptions.printStackTrace(Exceptions.attachMessage(iaex, "file = '"+file+"'"));
                    }
                    if (fo != null) {
                        file = fo.getNameExt();
                    }
                }
            } else if (node instanceof JToolTip) {
                JToolTip tooltip = (JToolTip) node;
                node = tooltip.getClientProperty("getShortDescription");    // NOI18N
                if (node instanceof CallFrame) {
                    frame = (CallFrame) node;
                    file = frame.getScript().getURL();
                    if (!file.isEmpty()) {
                        try {
                            URI uri = URI.create(file);
                            if (uri.isAbsolute()) {
                                URL url = uri.toURL();
                                Project project = pc.getProject();
                                if (project != null) {
                                    FileObject fo = ServerURLMapping.fromServer(project, url);
                                    if (fo != null) {
                                        file = FileUtil.getFileDisplayName(fo);
                                    }
                                }
                            }
                        } catch (MalformedURLException ex) {
                        } catch (IllegalArgumentException iaex) {
                            Exceptions.printStackTrace(Exceptions.attachMessage(iaex, "file = '"+file+"'"));
                        }
                    }
                } else {
                    throw new UnknownTypeException("Unknown Type Node: " + node);   // NOI18N
                }
            } else {
                throw new UnknownTypeException("Unknown Type Node: " + node);   // NOI18N
            }
            boolean current = frame == debugger.getCurrentCallFrame();
            return toHTML(file + ":" + (frame.getLineNumber()+1), current, false, null);
        } else {
            throw new UnknownTypeException("Unknown columnID: " + columnID);
        }
    }

    @Override
    public boolean isReadOnly(Object node, String columnID) throws
            UnknownTypeException {
        if ( node instanceof CallFrame ){
            if (columnID.equals(Constants.CALL_STACK_FRAME_LOCATION_COLUMN_ID)) {
                return true;
            }
        } 
        throw new UnknownTypeException(node);
    }

    @Override
    public void setValueAt(Object node, String columnID, Object value)
            throws UnknownTypeException {
        throw new UnknownTypeException(node);
    }
    
    @Override
    public void paused(List<CallFrame> callStack, String reason) {
        setStackTrace(callStack);
        refresh();
    }

    @Override
    public void resumed() {
        setStackTrace(null);
        refresh();
    }

    @Override
    public void reset() {
    }

    @Override
    public void enabled(boolean enabled) {
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if (Debugger.PROP_CURRENT_FRAME.equals(propertyName)) {
            refresh();
        }
    }
    
}
