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

/*
 * "watchBag.java"
 */

package org.netbeans.modules.cnd.debugger.common2.debugger;

import java.util.ArrayList;
import org.netbeans.api.debugger.Watch;
import org.netbeans.spi.debugger.ui.EditorPin;
import org.openide.filesystems.FileObject;

public class WatchBag {
    private ArrayList<NativeWatch> watches = new ArrayList<NativeWatch>();

    private ModelChangeDelegator watchUpdater() {
	return NativeDebuggerManager.get().watchUpdater();
    }

    public NativeWatch[] getWatches() {
	NativeWatch[] wa = new NativeWatch[watches.size()];
	return watches.toArray(wa);
    }

    public synchronized WatchVariable[] watchesFor(NativeDebugger debugger) {
	ArrayList<WatchVariable> ws = new ArrayList<WatchVariable>();
	for (NativeWatch w : watches) {
	    WatchVariable dw = w.findByDebugger(debugger);
            if (dw == null) {
                continue;
            }             
            if (WatchModel.showPinnedWatches.isShowPinnedWatches()) {
                ws.add(dw);
            } else {
                boolean isPinWatch = w.watch().getPin() != null;
                if (!isPinWatch) {
                    ws.add(dw);
                }
            }
            
	}
	return ws.toArray(new WatchVariable[ws.size()]);
    }
    
    public synchronized boolean hasPinnedWatches() {
        for (NativeWatch nativeWatch : watches) {
            final Watch watch = nativeWatch.watch();
            Watch.Pin pin = watch.getPin();
            if (pin instanceof EditorPin) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean hasPin(NativeWatch nativeWatch) {
        final Watch watch = nativeWatch.watch();
        Watch.Pin pin = watch.getPin();
        return (pin instanceof EditorPin); 
    }
    
    public static boolean isPinOpened(NativeWatch nativeWatch) {
        final Watch watch = nativeWatch.watch();
        Watch.Pin pin = watch.getPin();
        if (pin instanceof EditorPin) {
            EditorPin editorPin = (EditorPin)pin;
            FileObject editorFO = EditorContextBridge.getCurrentFileObject();
            if (editorFO != null &&  editorFO.equals(editorPin.getFile())) {
                //the editor is opened for the pinned watch
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if we have watches in the editor for the fileToOpen
     * @param fileToOpen if <code>null</code>  will check with the EditorContextBridge.getCurrentFileObject()
     * @return 
     */
    public synchronized boolean hasPinnedWatchesOpened(final FileObject fileToOpen) {
        FileObject editorFO = EditorContextBridge.getCurrentFileObject();
        if (fileToOpen != null && fileToOpen.isValid()) {
            editorFO = fileToOpen;
        }
        for (NativeWatch nativeWatch : watches) {
            final Watch watch = nativeWatch.watch();
            Watch.Pin pin = watch.getPin();
            if (pin instanceof EditorPin) {
                EditorPin editorPin = (EditorPin)pin;                
                if (editorFO != null &&  editorFO.equals(editorPin.getFile())) {
                    //the editor is opened for the pinned watch
                    return true;
                }
            }
        }
        return false;
    }    
    
    public void postDeleteAllWatches() {

	// Use an array because
	// 	for (WatchVariable w : subWatches)
	// will run into ConcurrentModificationException's
	
	NativeDebugger debugger = NativeDebuggerManager.get().currentNativeDebugger();

	for (NativeWatch w : getWatches()) {
	    WatchVariable dw = w.findByDebugger(debugger);
	    if (dw != null) {
		w.postDelete(false);
            }
	}
	    
    }

    /**
     * Called back when we restore a bag from XML.
     * At that point there may be no debugger or updater.
     * All such restored watches get re-add'ed later on so only need to put
     * them on the list.
     */
    public final synchronized void restore(NativeWatch newWatch) {
	assert !watches.contains(newWatch) :
	       "WB.restore(): watch added redundantly"; // NOI18N
	// LATER newWatch.restored();
	watches.add(newWatch);
	// OLD manager().addWatch(newWatch);
	newWatch.setUpdater(watchUpdater());
    }

    public synchronized void add(NativeWatch newWatch) {
	assert !watches.contains(newWatch) :
	       "WB.add(): watch added redundantly"; // NOI18N
	watches.add(newWatch);
	// OLD manager().addWatch(newWatch);
	newWatch.setUpdater(watchUpdater());
	watchUpdater().treeChanged();      // causes a pull
    }

    public synchronized void remove(NativeWatch oldWatch) {
	if (oldWatch == null)
	    return;

	oldWatch.cleanup();
	boolean removed = watches.remove(oldWatch);
	assert removed :
	       "WB.remove(): watch to be removed not in bag"; // NOI18N
	assert !watches.contains(oldWatch) :
	       "WB.remove(): watch still there after removal"; // NOI18N
	// OLD manager().removeWatch(oldWatch);
        oldWatch.postDelete(false);
	watchUpdater().treeChanged();      // causes a pull
    }
}
