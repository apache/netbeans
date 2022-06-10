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

package org.netbeans.modules.javascript.cdtdebug.ui.callstack.models;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import org.netbeans.lib.chrome_devtools_protocol.debugger.CallFrame;
import org.netbeans.lib.chrome_devtools_protocol.debugger.Location;
import org.netbeans.modules.javascript.cdtdebug.CDTDebugger;
import org.netbeans.modules.javascript.cdtdebug.CDTDebuggerSessionProvider;
import org.netbeans.modules.javascript.cdtdebug.CDTScript;

import org.netbeans.modules.javascript2.debug.ui.models.ViewModelSupport;
import org.netbeans.modules.web.common.sourcemap.SourceMapsTranslator;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.DebuggerServiceRegistrations;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;

@DebuggerServiceRegistrations({
 @DebuggerServiceRegistration(path=CDTDebuggerSessionProvider.SESSION_NAME+"/DebuggingView",
                              types={ TreeModel.class, ExtendedNodeModel.class }),
 @DebuggerServiceRegistration(path=CDTDebuggerSessionProvider.SESSION_NAME+"/CallStackView",
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

    private final CDTDebugger dbg;

    public DebuggingModel(ContextProvider contextProvider) {
        dbg = contextProvider.lookupFirst(null, CDTDebugger.class);
        CDTDebugger.Listener changeListener = new ChangeListener();
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
                List<CallFrame> cs = dbg.getCurrentCallStack();
                if (cs != null) {
                    return cs.toArray();
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
        if (node instanceof CallFrame) {
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
            CallFrame frame = (CallFrame) node;
            Location locationRemote = frame.getLocation();
            FileObject locationFile = dbg.getScriptsHandler()
                    .getFile(locationRemote.getScriptId());

            int locationLine = frame.getLocation().getLineNumber();
            int locationColumn = frame.getLocation().getColumnNumber() != null ? frame.getLocation().getColumnNumber() : 0;

            SourceMapsTranslator.Location translatedLocation = dbg
                    .getScriptsHandler()
                    .getSourceMapsTranslator()
                    .getSourceLocation(new SourceMapsTranslator.Location(locationFile, locationLine, locationColumn));

            String thisName = frame.getThisObject().getClassName();
            if ("Object".equals(thisName) || "global".equals(thisName)) {
                thisName = null;
            }
            String functionName = frame.getFunctionName();
            String scriptName;
            long line;
            long column;
            if (translatedLocation != null) {
                scriptName = getScriptName(translatedLocation.getFile());
                line = translatedLocation.getLine()+1;
                column = translatedLocation.getColumn()+1;
            } else {
                scriptName = getScriptName(dbg, frame);
                line = locationLine+1;
                column = locationColumn+1;
            }

            String text = ((thisName != null && !thisName.isEmpty()) ? thisName + '.' : "") +
                   functionName +
                   " (" + ((scriptName != null) ? scriptName : "?") +
                   ":"+line+":"+column+")";

            if(dbg.getCurrentFrame() == frame) {
                return toHTML(text, true, false, null);
            } else {
                return text;
            }
        } else if (node == DBG_RUNNING_NODE) {
            return Bundle.CTL_DebuggerRunning();
        }
        throw new UnknownTypeException(node);
    }

    static String getScriptName(CDTDebugger dbg, CallFrame cf) {
        CDTScript script = dbg.getScriptsHandler()
                    .getScript(cf.getLocation().getScriptId());
        if (script != null) {
            String scriptName = script.getUrl().toString();
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
            CallFrame frame = (CallFrame) node;
            String text = frame.getFunctionName();
            if (text != null) {
                text = text.replace("\\n", "\n");
            }
            return text;
        } else if (node == DBG_RUNNING_NODE) {
            return Bundle.CTL_DebuggerRunningDescr();
        }
        throw new UnknownTypeException(node);
    }

    private class ChangeListener implements CDTDebugger.Listener {

        private WeakReference<CallFrame> lastCurrentFrame = new WeakReference<>(null);

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
