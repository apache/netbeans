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

package org.netbeans.modules.debugger.jpda.jsui.frames.models;

import org.netbeans.modules.debugger.jpda.js.frames.JSStackFrame;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.modules.debugger.jpda.js.JSUtils;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.ExtendedNodeModelFilter;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.datatransfer.PasteType;

/**
 *
 * @author Martin
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession/DebuggingView",
                             types=ExtendedNodeModelFilter.class,
                             position=22000)
public class DebuggingJSNodeModel implements ExtendedNodeModelFilter {
    
    private static final String SCRIPT_CLASS_PREFIX = "Script$";
    private static final String SCRIPT_CLASS_IN_HTML = ">Script$";

    private final List<ModelListener> listeners = new ArrayList<>();
    private ModelListener listenerToOriginal;
    private final Object listenerToOriginalLock = new Object();
    
    @Override
    public boolean canRename(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return original.canRename(node);
    }

    @Override
    public boolean canCopy(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return original.canCopy(node);
    }

    @Override
    public boolean canCut(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return original.canCut(node);
    }

    @Override
    public Transferable clipboardCopy(ExtendedNodeModel original, Object node) throws IOException, UnknownTypeException {
        return original.clipboardCopy(node);
    }

    @Override
    public Transferable clipboardCut(ExtendedNodeModel original, Object node) throws IOException, UnknownTypeException {
        return original.clipboardCut(node);
    }

    @Override
    public PasteType[] getPasteTypes(ExtendedNodeModel original, Object node, Transferable t) throws UnknownTypeException {
        return original.getPasteTypes(node, t);
    }

    @Override
    public void setName(ExtendedNodeModel original, Object node, String name) throws UnknownTypeException {
        original.setName(node, name);
    }

    @Override
    public String getIconBaseWithExtension(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        if (node instanceof JSStackFrame) {
            node = ((JSStackFrame) node).getJavaFrame();
        }
        return original.getIconBaseWithExtension(node);
    }

    @Override
    public String getDisplayName(NodeModel original, Object node) throws UnknownTypeException {
        String descr;
        if (node instanceof JSStackFrame) {
            synchronized (listenerToOriginalLock) {
                if (listenerToOriginal == null) {
                    listenerToOriginal = new OriginalModelListener();
                    original.addModelListener(listenerToOriginal);
                }
            }
            CallStackFrame javaFrame = ((JSStackFrame) node).getJavaFrame();
            descr = original.getDisplayName(javaFrame);
            //System.out.println("ORIG descr = '"+descr+"'");
            String nashornScriptClass = JSUtils.NASHORN_SCRIPT_JDK;
            int i = descr.indexOf(nashornScriptClass);
            if (i < 0) {
                // if Legacy JDK not found, search for external Nashorn
                nashornScriptClass = JSUtils.NASHORN_SCRIPT_EXT;
                i = descr.indexOf(nashornScriptClass);
            }
            int i2 = 0;
            if (i < 0) {
                if (descr.startsWith(SCRIPT_CLASS_PREFIX)) {
                    i = 0;
                    i2 = SCRIPT_CLASS_PREFIX.length();
                } else {
                    i = descr.indexOf(SCRIPT_CLASS_IN_HTML);
                    if (i > 0) {
                        i2 = i + SCRIPT_CLASS_IN_HTML.length();
                        i++;
                    }
                }
            } else {
                i2 = i + nashornScriptClass.length();
            }
            if (i >= 0) {
                descr = descr.substring(0, i) + descr.substring(i2);
            } else {
                if (descr.startsWith("<html>")) {
                    int end = descr.indexOf("</");
                    int begin = descr.lastIndexOf('>', end);
                    begin++;
                    descr = descr.substring(0, begin)+
                            stripParentPath(descr.substring(begin, end))+
                            descr.substring(end);
                } else {
                    descr = stripParentPath(descr);
                }
            }
            //System.out.println(" => descr = '"+descr+"'");
        } else {
            descr = original.getDisplayName(node);
        }
        return descr;
    }
    
    private static String stripParentPath(String descr) {
        int slash = descr.lastIndexOf(File.separatorChar);
        if (File.separatorChar != '/') {
            int slash2 = descr.lastIndexOf('/');
            slash = Math.max(slash, slash2);
        }
        if (slash > 0) {
            descr = descr.substring(slash+1);
        }
        return descr;
    }

    @Override
    public String getIconBase(NodeModel original, Object node) throws UnknownTypeException {
        if (node instanceof JSStackFrame) {
            return original.getIconBase(((JSStackFrame) node).getJavaFrame());
        } else {
            return original.getIconBase(node);
        }
    }

    @Override
    public String getShortDescription(NodeModel original, Object node) throws UnknownTypeException {
        if (node instanceof JSStackFrame) {
            return original.getShortDescription(((JSStackFrame) node).getJavaFrame());
        } else {
            return original.getShortDescription(node);
        }
    }

    @Override
    public void addModelListener(ModelListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    @Override
    public void removeModelListener(ModelListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    private void fireModelEvent(ModelEvent event) {
        List<ModelListener> ls;
        synchronized (listeners) {
            ls = new ArrayList<>(listeners);
        }
        for (ModelListener ml : ls) {
            ml.modelChanged (event);
        }
    }
    
    private class OriginalModelListener implements ModelListener {

        @Override
        public void modelChanged(ModelEvent event) {
            if (event instanceof ModelEvent.NodeChanged) {
                ModelEvent.NodeChanged nch = (ModelEvent.NodeChanged) event;
                Object node = nch.getNode();
                if (node instanceof CallStackFrame) {
                    JSStackFrame jsFrame = JSStackFrame.getExisting((CallStackFrame) node);
                    if (jsFrame != null) {
                        event = new ModelEvent.NodeChanged(DebuggingJSNodeModel.this, jsFrame, nch.getChange());
                        fireModelEvent(event);
                    }
                }
            } else if (event instanceof ModelEvent.TableValueChanged) {
                ModelEvent.TableValueChanged tch = (ModelEvent.TableValueChanged) event;
                Object node = tch.getNode();
                if (node instanceof CallStackFrame) {
                    JSStackFrame jsFrame = JSStackFrame.getExisting((CallStackFrame) node);
                    if (jsFrame != null) {
                        event = new ModelEvent.TableValueChanged(DebuggingJSNodeModel.this, jsFrame, tch.getColumnID(), tch.getChange());
                        fireModelEvent(event);
                    }
                }
            }
            
        }
        
    }
    
}
