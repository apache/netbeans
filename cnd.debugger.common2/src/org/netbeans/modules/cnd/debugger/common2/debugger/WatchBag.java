/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
