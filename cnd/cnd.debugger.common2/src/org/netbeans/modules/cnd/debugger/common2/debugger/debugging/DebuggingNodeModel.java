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

package org.netbeans.modules.cnd.debugger.common2.debugger.debugging;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import org.netbeans.modules.cnd.debugger.common2.debugger.Frame;
import org.netbeans.modules.cnd.debugger.common2.debugger.Thread;
import org.netbeans.modules.cnd.debugger.common2.values.VariableValue;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.datatransfer.PasteType;

/**
 *
 */

public abstract class DebuggingNodeModel implements ExtendedNodeModel {

    protected DebuggingNodeModel() {
    }
    
    @Override
    public String getDisplayName(Object node) throws UnknownTypeException {
        if (node instanceof Thread) {
            Thread thread = (Thread) node;
            if (thread.isCurrent()) {
                return VariableValue.bold(thread.getName());
            } else {
                return thread.getName();
            }
        }
        if (node instanceof Frame) {
            Frame frame = (Frame) node;
            CndUtils.assertNotNull(frame.getThread(), "frame " + frame.getLocationName() + " has no parent thread");
            if (frame.isCurrent() && frame.getThread().isCurrent()) {
                return VariableValue.bold(frame.getLocationName());
            } else {
                return frame.getLocationName();
            }
        }
        return "";
    }
    
    // Thread icons
    private static final String THREAD_ICON_PATH =
	"org/netbeans/modules/cnd/debugger/common2/icons/";		// NOI18N

    private static final String THREAD_ICON_NORMAL =
	THREAD_ICON_PATH + "thread";				// NOI18N
    private static final String THREAD_ICON_EVENT =
	THREAD_ICON_PATH + "thread_hit";			// NOI18N
    private static final String THREAD_ICON_CURRENT_NORMAL =
	THREAD_ICON_PATH + "thread_current";			// NOI18N
    private static final String THREAD_ICON_CURRENT_EVENT =
	THREAD_ICON_PATH + "thread_current_hit";		// NOI18N
    
    // StackFrame icons
    private static final String FRAME_ICON_PATH =
	"org/netbeans/modules/debugger/resources";	// NOI18N
    private static final String FRAME_ICON_BASE =
	FRAME_ICON_PATH + "/callStackView/NonCurrentFrame";	// NOI18N
    private static final String FRAME_ICON_BASE_CURRENT =
	FRAME_ICON_PATH + "/callStackView/CurrentFrame";	// NOI18N

    private static final String FRAME_ICON_EMPTY =
	"org/netbeans/modules/cnd/debugger/common2/icons/empty";	// NOI18N

    private static final String FRAME_ICON_SIGNAL_HANDLER =
	"org/netbeans/modules/cnd/debugger/common2/icons/signal_handler_frame";// NOI18N

    private static final String FRAME_ICON_USER_CALL =
	"org/netbeans/modules/cnd/debugger/common2/icons/user_call_frame";// NOI18N

    @Override
    public String getIconBase(Object node) throws UnknownTypeException {
        if (node instanceof Thread) {
            Thread t = (Thread) node;
            if (t.hasEvent()) {
                if (t.isCurrent()) {
                    return THREAD_ICON_CURRENT_EVENT;
                } else {
                    return THREAD_ICON_EVENT;
                }
            } else {
                if (t.isCurrent()) {
                    return THREAD_ICON_CURRENT_NORMAL;
                } else {
                    return THREAD_ICON_NORMAL;
                }
            }
        }
        if (node instanceof Frame) {
	    Frame f = (Frame) node;
	    if (f.isCurrent() && f.getThread().isCurrent())
		return FRAME_ICON_BASE_CURRENT;
	    else if (f.isSignalHandler())
		return FRAME_ICON_SIGNAL_HANDLER;
	    else if (f.isUserCall())
		return FRAME_ICON_USER_CALL;
	    else if (f.isSpecial())
		return FRAME_ICON_EMPTY;
	    else
		return FRAME_ICON_BASE;
	}
        
        return "";
    }
    
    @Override
    public String getIconBaseWithExtension(Object node) throws UnknownTypeException {
        if (node instanceof Thread) {
            return getIconBase(node) + ".png";  //NOI18N
        }
        if (node instanceof Frame) {
            return getIconBase(node) + ".gif";  //NOI18N
        }
        
        throw new UnknownTypeException (node);
    }
    
    @Override
    public String getShortDescription(Object node) throws UnknownTypeException {
        return "";  // TODO
    }

    @Override
    public void addModelListener(ModelListener l) {
    }

    @Override
    public void removeModelListener(ModelListener l) {
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
    public boolean canRename(Object node) throws UnknownTypeException {
        return false;
    }

    @Override
    public Transferable clipboardCopy(Object node) throws IOException, UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet.");  //NOI18N
    }

    @Override
    public Transferable clipboardCut(Object node) throws IOException, UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet.");  //NOI18N
    }

    @Override
    public void setName(Object node, String name) throws UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet.");  //NOI18N
    }

    @Override
    public PasteType[] getPasteTypes(Object node, Transferable t) throws UnknownTypeException {
        return null;
    }
}
