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

package org.netbeans.modules.javascript.v8debug.ui.callstack.models;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.lang.ref.WeakReference;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.lib.v8debug.V8Frame;
import org.netbeans.lib.v8debug.V8Script;
import org.netbeans.lib.v8debug.vars.ReferencedValue;
import org.netbeans.lib.v8debug.vars.V8Function;
import org.netbeans.lib.v8debug.vars.V8Object;
import org.netbeans.lib.v8debug.vars.V8ScriptValue;
import org.netbeans.lib.v8debug.vars.V8Value;
import org.netbeans.modules.javascript.v8debug.V8Debugger;
import org.netbeans.modules.javascript.v8debug.V8DebuggerSessionProvider;
import org.netbeans.modules.javascript.v8debug.frames.CallFrame;
import org.netbeans.modules.javascript.v8debug.frames.CallStack;
import org.netbeans.modules.javascript2.debug.ui.models.ViewModelSupport;
import org.netbeans.modules.web.common.sourcemap.SourceMapsTranslator;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.DebuggerServiceRegistrations;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;

/**
 *
 * @author Martin Entlicher
 */
@DebuggerServiceRegistrations({
 @DebuggerServiceRegistration(path=V8DebuggerSessionProvider.SESSION_NAME+"/DebuggingView",
                              types={ TreeModel.class, ExtendedNodeModel.class }),
 @DebuggerServiceRegistration(path=V8DebuggerSessionProvider.SESSION_NAME+"/CallStackView",
                              types={ TreeModel.class, ExtendedNodeModel.class })
})
public class DebuggingModel extends ViewModelSupport implements TreeModel, ExtendedNodeModel {
    
    //@StaticResource(searchClasspath = true)
    private static final String ICON_CALL_STACK =
            "org/netbeans/modules/debugger/resources/threadsView/call_stack_16.png";
    //@StaticResource(searchClasspath = true)
    private static final String ICON_EMPTY =
            "org/netbeans/modules/debugger/resources/empty.gif";
    
    private static final Object DBG_RUNNING_NODE = new Object();
    
    private final V8Debugger dbg;
    
    public DebuggingModel(ContextProvider contextProvider) {
        dbg = contextProvider.lookupFirst(null, V8Debugger.class);
        V8Debugger.Listener changeListener = new ChangeListener();
        dbg.addListener(changeListener);
    }

    @Override
    public Object getRoot() {
        return ROOT;
    }

    @Override
    public Object[] getChildren(Object parent, int from, int to) throws UnknownTypeException {
        if (parent == ROOT) {
            if (dbg.isSuspended()) {
                CallStack cs = dbg.getCurrentCallStack();
                if (cs != null) {
                    return cs.getCallFrames();
                }
            } else {
                return new Object[] { DBG_RUNNING_NODE };
            }
            return EMPTY_CHILDREN;
        }
        throw new UnknownTypeException(parent);
    }

    @Override
    public boolean isLeaf(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return false;
        }
        if (node instanceof CallStack) {
            return true;
        }
        if (node == DBG_RUNNING_NODE) {
            return true;
        }
        throw new UnknownTypeException(node);
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
    }

    @Override
    public String getIconBaseWithExtension(Object node) throws UnknownTypeException {
        if (node instanceof CallFrame) {
            return ICON_CALL_STACK;
        } else if (node == DBG_RUNNING_NODE) {
            return ICON_EMPTY;
        }
        throw new UnknownTypeException(node);
    }

    @NbBundle.Messages("CTL_DebuggerRunning=Program is Running...")
    @Override
    public String getDisplayName(Object node) throws UnknownTypeException {
        if (node instanceof CallFrame) {
            CallFrame cf = (CallFrame) node;
            SourceMapsTranslator.Location translatedLocation = cf.getTranslatedLocation();
            V8Frame frame = cf.getFrame();
            String thisName = cf.getThisName();
            if ("Object".equals(thisName) || "global".equals(thisName)) {
                thisName = null;
            }
            String functionName = cf.getFunctionName();
            String scriptName;
            long line;
            long column;
            if (translatedLocation != null) {
                scriptName = getScriptName(translatedLocation.getFile());
                line = translatedLocation.getLine()+1;
                column = translatedLocation.getColumn()+1;
            } else {
                scriptName = getScriptName(cf);
                line = frame.getLine()+1;
                column = frame.getColumn()+1;
            }
            
            String text = ((thisName != null && !thisName.isEmpty()) ? thisName + '.' : "") +
                   functionName +
                   " (" + ((scriptName != null) ? scriptName : "?") +
                   ":"+line+":"+column+")";
            //text += ":"+line+":"+column;
            return text;
        } else if (node == DBG_RUNNING_NODE) {
            return Bundle.CTL_DebuggerRunning();
        }
        throw new UnknownTypeException(node);
    }
    
    static String getScriptName(CallFrame cf) {
        V8Script script = cf.getScript();
        if (script != null) {
            String scriptName = script.getName();
            if (scriptName == null) {
                return null;
            }
            int i = scriptName.lastIndexOf('/');
            if (i < 0) {
                i = scriptName.lastIndexOf('\\');
            }
            if (i > 0) {
                scriptName = scriptName.substring(i+1);
            }
            return scriptName;
        }
        return null;
    }
    
    private static String getScriptName(FileObject fo) {
        return fo.getNameExt();
    }
    
    @Override
    public String getIconBase(Object node) throws UnknownTypeException {
        throw new UnsupportedOperationException("Not to be called.");
    }

    @NbBundle.Messages("CTL_DebuggerRunningDescr=No stack trace while program is running.")
    @Override
    public String getShortDescription(Object node) throws UnknownTypeException {
        if (node instanceof CallFrame) {
            CallFrame cf = (CallFrame) node;
            V8Frame frame = cf.getFrame();
            String text = frame.getText();
            if (text != null) {
                text = text.replace("\\n", "\n");
            }
            return text;
        } else if (node == DBG_RUNNING_NODE) {
            return Bundle.CTL_DebuggerRunningDescr();
        }
        throw new UnknownTypeException(node);
    }
    
    private class ChangeListener implements V8Debugger.Listener {
        
        private WeakReference<CallFrame> lastCurrentFrame = new WeakReference<CallFrame>(null);
        
        public ChangeListener() {}

        @Override
        public void notifySuspended(boolean suspended) {
            fireChangeEvent(new ModelEvent.TreeChanged(DebuggingModel.this));
        }

        @Override
        public void notifyCurrentFrame(CallFrame cf) {
            CallFrame last = lastCurrentFrame.get();
            if (last != null) {
                fireChangeEvent(new ModelEvent.NodeChanged(DebuggingModel.this, last));
            }
            if (cf != null) {
                fireChangeEvent(new ModelEvent.NodeChanged(DebuggingModel.this, cf));
            }
            lastCurrentFrame = new WeakReference<>(cf);
        }
        
        @Override
        public void notifyFinished() {
        }
        
    }
    
}
