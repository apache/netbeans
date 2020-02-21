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

package org.netbeans.modules.cnd.debugger.common2.debugger.actions;

import java.util.Set;
import java.util.Collection;
import java.util.Collections;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.DisassemblyUtils;
import org.openide.filesystems.FileObject;

import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;


import org.netbeans.api.debugger.ActionsManager;


import org.netbeans.modules.cnd.debugger.common2.debugger.State;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.EditorContextBridge;
import org.netbeans.modules.cnd.debugger.common2.debugger.RoutingToken;
import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.Disassembly;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.Handler;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.NativeBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.BreakpointBag;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.InstructionBreakpoint;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.spi.debugger.ActionsProvider.Registration;
import org.openide.filesystems.FileStateInvalidException;


/*
 * ToggleBreakpointActionProvider
 *
 * Modelled on
 *	org.netbeans.modules.debugger.jpda.ui.actions.ToggleBreakpointAction
 * was: ToggleBreakpointSupport (or ToggleLinePerformer?)
 */
@Registration(actions={"toggleBreakpoint"}, activateForMIMETypes={"text/x-c++", "text/x-c", "text/x-h", "text/x-c/text/x-h", "text/x-fortran", "text/x-asm"})
public class ToggleBreakpointActionProvider extends NativeActionsProvider implements PropertyChangeListener {
    
    public ToggleBreakpointActionProvider() {
	super(null);
        EditorContextBridge.addPropertyChangeListener(this);
    }

//    public ToggleBreakpointActionProvider(ContextProvider ctxProvider) {
//	super(ctxProvider);
//	setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, true);
//    }

    /* LATER
    public String getName() {
	// SHOULD use getText() (aka getString())
	return Catalog.get("ToggleBreakpointAction");
    }
    */

    /* interface ActionsProvider */
    @Override
    public Set getActions() {
	return Collections.singleton(ActionsManager.ACTION_TOGGLE_BREAKPOINT);
    }

    private boolean checkTarget() {
	if (! NativeDebuggerManager.isPerTargetBpts())
	    return true;	// we always accept them for global bpts

	NativeDebugger debugger = getDebugger();

	if (debugger == null || !debugger.state().isLoaded) {
	    NativeDebuggerManager.errorLoadBeforeBpt();
	    return false;
	} else {
	    return true;
	}

    }

    /* interface ActionsProvider */
    @Override
    public void doAction(Object action) {
        FileObject currentFileObject = EditorContextBridge.getCurrentFileObject();
        String fileName = currentFileObject.getPath();
        if (fileName.trim().equals("")) {
            return;
        }
	int lineNo = EditorContextBridge.getCurrentLineNumber();

	if (ignoreJava && fileName.endsWith(".java")) { // NOI18N
	    // Ignore toggles in .java files because if the jpda debugger
	    // is on we'll get two breakpoints.
	    return;
	}

	if (!checkTarget())
	    return;
        
        String currentURL = EditorContextBridge.getCurrentURL();
        String address = null;
        NativeBreakpoint bpt = null;
        BreakpointBag bb = NativeDebuggerManager.get().breakpointBag();
        NativeDebugger debugger = getDebugger();
        if (Disassembly.isDisasm(currentURL)) {
            address = DisassemblyUtils.getLineAddress(lineNo);
            if (address == null) {
                return;
            }
            for (NativeBreakpoint breakpoint : bb.getBreakpoints()) {
                if (breakpoint instanceof InstructionBreakpoint) {
                    if (address.equals(((InstructionBreakpoint)breakpoint).getAddress())) {
                        bpt = breakpoint;
                        break;
                    }
                }
            }
        } else {
            bpt = bb.locateBreakpointAt(fileName, lineNo, debugger);
        }

	if (bpt != null) {
	    // toggle off
	    bpt.dispose();
            return;
	} 
        
        // prevent double click on not yet handled previous file:line breakpoint
        if (debugger != null && debugger.bm().hasBreakpointJobAt(fileName, lineNo)) {
            // there is no way to dispose a pending breakpoint
            return;
        }
                
        // create a new breakpoint
        else if (address != null) {
            bpt = NativeBreakpoint.newInstructionBreakpoint(address);
        } else {
            try {
                bpt = NativeBreakpoint.newLineBreakpoint(fileName, lineNo, currentFileObject.getFileSystem());
            } catch (FileStateInvalidException ex) {
                // just do not create
            }
        }
            
        // toggle on
        if (bpt != null) {
            int routingToken = RoutingToken.BREAKPOINTS.getUniqueRoutingTokenInt();
            Handler.postNewHandler(debugger, bpt, routingToken);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        int lnum = EditorContextBridge.getCurrentLineNumber();
        String mimeType = EditorContextBridge.getCurrentMIMEType();
	boolean isValid = (MIMENames.isFortranOrHeaderOrCppOrC(mimeType)
                || mimeType.equals(MIMENames.ASM_MIME_TYPE))
                        && lnum > 0;
        setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, isValid);
//        if (debugger != null && debugger.getState() == GdbDebugger.State.EXITED) {
//            destroy();
//        }
    }

    /* interface NativeActionsProvider */
    @Override
    public void update(State state) {
	// always enabled
    }

    //
    // Ignore java is jpda debugger is loaded.
    //
    // We ...
    // - At this classes load time check for jpdas presense.
    // - Setup a Lookup.Result to learn of module loading and unloading
    //   and check for jpdas presense on eache vent.
    // - If the module is loaded listen to it's enable property getting 
    //   enabled or disabled.

    private static boolean ignoreJava = false;

    private static final String jpdaModuleName =
	"org.netbeans.modules.debugger.jpda";		// NOI18N

    /**
     * See if the jpda debugger is loaded or not and set 'ignoreJava'.
     */
    private static void checkForJpdaDebugger() {
	if (Log.Action.jpdaWatcher)
	    System.out.printf("checkForJpdaDebugger #######################\n"); // NOI18N

	Collection<? extends ModuleInfo> moduleInfos =
	    Lookup.getDefault().lookupAll(ModuleInfo.class);

	ignoreJava = false;
	String jdbx = System.getProperty("spro.jdbx");

	for (ModuleInfo moduleInfo : moduleInfos) {
	    if (moduleInfo.getCodeNameBase().equals(jpdaModuleName)) {
		if (moduleInfo.isEnabled() &&  jdbx != null && !jdbx.equals("on")) { // NOI18N
		    ignoreJava = true;
		}

		// Arrange to get notified when the module is enabled
		// or disabled.

		moduleInfo.addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
		    public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals(ModuleInfo.PROP_ENABLED )) {
			    if ( ((boolean) (Boolean) evt.getNewValue()) == true ) {
				ignoreJava = true;
			    } else {
				ignoreJava = false;
			    }
			    if (Log.Action.jpdaWatcher)
				System.out.printf("\tignoreJava -> %s\n", ignoreJava); // NOI18N
			}
		    }
		} );
	    }
	}

	if (Log.Action.jpdaWatcher)
	    System.out.printf("\tignoreJava = %s\n", ignoreJava); // NOI18N
    }

    private static final Lookup.Result<ModuleInfo> result;
    static {
	// Initial check

	checkForJpdaDebugger();

	// Arrange to get notified when modules get loaded or unloaded

	result = Lookup.getDefault().lookupResult(ModuleInfo.class);
	result.addLookupListener(new LookupListener() {
            @Override
	    public void resultChanged(LookupEvent event) {
		checkForJpdaDebugger();
	    }
	});
    }
}
