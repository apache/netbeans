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

package org.netbeans.modules.cnd.debugger.dbx;

import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.InstructionBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.LineBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.FallbackBreakpointType;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.FallbackBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.InstructionBreakpointType;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.FunctionBreakpointType;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.FunctionBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.LineBreakpointType;
import org.netbeans.modules.cnd.debugger.dbx.breakpoints.types.LoadObjBreakpoint;
import org.netbeans.modules.cnd.debugger.dbx.breakpoints.types.TimerBreakpoint;
import org.netbeans.modules.cnd.debugger.dbx.breakpoints.types.FaultBreakpointType;
import org.netbeans.modules.cnd.debugger.dbx.breakpoints.types.ClassMethodBreakpointType;
import org.netbeans.modules.cnd.debugger.dbx.breakpoints.types.AccessBreakpoint;
import org.netbeans.modules.cnd.debugger.dbx.breakpoints.types.DebuggerBreakpointType;
import org.netbeans.modules.cnd.debugger.dbx.breakpoints.types.SignalBreakpointType;
import org.netbeans.modules.cnd.debugger.dbx.breakpoints.types.InfileBreakpoint;
import org.netbeans.modules.cnd.debugger.dbx.breakpoints.types.SignalBreakpoint;
import org.netbeans.modules.cnd.debugger.dbx.breakpoints.types.ObjectBreakpoint;
import org.netbeans.modules.cnd.debugger.dbx.breakpoints.types.AccessBreakpointType;
import org.netbeans.modules.cnd.debugger.dbx.breakpoints.types.ProcessBreakpointType;
import org.netbeans.modules.cnd.debugger.dbx.breakpoints.types.InfileBreakpointType;
import org.netbeans.modules.cnd.debugger.dbx.breakpoints.types.CondBreakpointType;
import org.netbeans.modules.cnd.debugger.dbx.breakpoints.types.CondBreakpoint;
import org.netbeans.modules.cnd.debugger.dbx.breakpoints.types.ObjectBreakpointType;
import org.netbeans.modules.cnd.debugger.dbx.breakpoints.types.TimerBreakpointType;
import org.netbeans.modules.cnd.debugger.dbx.breakpoints.types.DebuggerBreakpoint;
import org.netbeans.modules.cnd.debugger.dbx.breakpoints.types.FaultBreakpoint;
import org.netbeans.modules.cnd.debugger.dbx.breakpoints.types.ProcessBreakpoint;
import org.netbeans.modules.cnd.debugger.dbx.breakpoints.types.LoadObjBreakpointType;
import org.netbeans.modules.cnd.debugger.dbx.breakpoints.types.ClassMethodBreakpoint;
import org.openide.ErrorManager;

import com.sun.tools.swdev.glue.dbx.*;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;

import org.netbeans.modules.cnd.debugger.common2.values.AccessBA;
import org.netbeans.modules.cnd.debugger.common2.values.Action;
import org.netbeans.modules.cnd.debugger.common2.values.DebuggerEvent;
import org.netbeans.modules.cnd.debugger.common2.values.DlEvent;
import org.netbeans.modules.cnd.debugger.common2.values.ExceptionSpec;
import org.netbeans.modules.cnd.debugger.common2.values.FunctionSubEvent;
import org.netbeans.modules.cnd.debugger.common2.values.ProcessEvent;
import org.netbeans.modules.cnd.debugger.common2.values.SysCallEE;

import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.Handler;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.HandlerCommand;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.HandlerExpert;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.NativeBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.NativeBreakpointType;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.ExceptionBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.ExceptionBreakpointType;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.SysCallBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.SysCallBreakpointType;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.VariableBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.VariableBreakpointType;

public class DbxHandlerExpert implements HandlerExpert {
    private final DbxDebuggerImpl debugger;

    public DbxHandlerExpert(DbxDebuggerImpl debugger) {
        this.debugger = debugger;
    }

    Handler newHandler(NativeDebugger debugger,
		       GPDbxHandler newGpHandler,
		       NativeBreakpoint breakpoint,
		       boolean isFallback) {
	if (breakpoint == null) {
	    breakpoint = createBreakpoint(newGpHandler,isFallback);
	} else {
	    assert ! breakpoint.hasHandler();
	}
	update(breakpoint, newGpHandler);
	Handler handler = new Handler(debugger, breakpoint);
	setGenericProperties(handler, newGpHandler);
	return handler;
    }

    Handler replaceHandler(Handler originalHandler, GPDbxHandler newData) {
	NativeBreakpoint breakpoint = originalHandler.breakpoint();
	update(breakpoint, newData);
	setGenericProperties(originalHandler, newData);
	return originalHandler;
    }

    public ReplacementPolicy replacementPolicy() {
	return ReplacementPolicy.INPLACE;
    }

    // interface HandlerExpert
    public Handler childHandler(NativeBreakpoint bpt) {
	NativeBreakpoint breakpoint = bpt;
	if (bpt.isToplevel()) {
	    breakpoint = bpt.makeSubBreakpointCopy();
	} else if (bpt.isSubBreakpoint()) {
	    breakpoint = bpt;
	} else {
	    assert false : "childHandler(): cannot clone from a midlevel bpt";
	}
	Handler handler = new Handler(debugger, breakpoint);
	return handler;
    }


    // interface HandlerExpert
    public HandlerCommand commandFormNew(NativeBreakpoint b) {
	HandlerJig hj = new HandlerJig(debugger, b);
	HandlerCommand cmd = hj.generate();
	return cmd;
    }


    // interface HandlerExpera
    public HandlerCommand commandFormCustomize(NativeBreakpoint editedBreakpoint, 
		                       NativeBreakpoint targetBreakpoint) {

	HandlerJig hj = new HandlerJig(debugger, editedBreakpoint);

	if (targetBreakpoint.isBroken()) {
	    // If we're repairing a broken breakpoint there's no
	    // counterpart in dbx so we don't need to tack on a -replace
	    // In-effect we'll be creating a new handler

	} else {
	    // figure id of bpt to replace.
	    int id = targetBreakpoint.getId();

	    // tack on a -replace
	    hj.replace(id);
	}

	return hj.generate();
    }

    /**
     * Create a NativeBreakpoint from a GPDbxHandler
     *
     * was: IpeHandler.createIBE()
     */

    private static NativeBreakpoint createBreakpoint(GPDbxHandler h,
						     boolean isFallback) {
	NativeBreakpointType type = null;

        if (h.eventspec == null) {
            ErrorManager.getDefault().log("empty eventspec");	// NOI18N

	} else if (isFallback) {
	    type = new FallbackBreakpointType();
        } else if (h.eventspec.startsWith("at ")) {     // NOI18N
            if (modifierIsSetIn("-instr", h))           // NOI18N
                type = new InstructionBreakpointType();
            else
                type = LineBreakpointType.getDefault();
        } else if (h.eventspec.startsWith("in ")) { // NOI18N
            type = new FunctionBreakpointType();
        } else if (h.eventspec.startsWith("returns")) { // NOI18N
            type = new FunctionBreakpointType();
        } else if (h.eventspec.startsWith("infunction ")) { // NOI18N
            type = new FunctionBreakpointType();
        } else if (h.eventspec.startsWith("modify ")) { // NOI18N
            type = new AccessBreakpointType();
        } else if (h.eventspec.startsWith("access ")) { // NOI18N
            type = new AccessBreakpointType();
        } else if (h.eventspec.startsWith("inmember ")) { // NOI18N
            type = new ClassMethodBreakpointType();
        } else if (h.eventspec.startsWith("inclass ")) { // NOI18N
            type = new ClassMethodBreakpointType();
        } else if (h.eventspec.startsWith("cond ")) { // NOI18N
            type = new CondBreakpointType();
        } else if (h.eventspec.startsWith("throw")) { // NOI18N
            type = new ExceptionBreakpointType();
        } else if (h.eventspec.startsWith("fault ")) { // NOI18N
            type = new FaultBreakpointType();
        } else if (h.eventspec.startsWith("dlopen")) { // NOI18N
            type = new LoadObjBreakpointType();
        } else if (h.eventspec.startsWith("dlclose")) { // NOI18N
            type = new LoadObjBreakpointType();
        } else if (h.eventspec.startsWith("inobject")) { // NOI18N
            type = new ObjectBreakpointType() ;
        } else if (h.eventspec.startsWith("attach")) { // NOI18N
            type = new DebuggerBreakpointType();
        } else if (h.eventspec.startsWith("detach")) { // NOI18N
            type = new DebuggerBreakpointType();
        } else if (h.eventspec.startsWith("exit")) { // NOI18N
            type = new ProcessBreakpointType();
        } else if (h.eventspec.equals("sync")) { // NOI18N
            type = new ProcessBreakpointType();
        } else if (h.eventspec.equals("syncrtld")) { // NOI18N
            type = new ProcessBreakpointType();
        } else if (h.eventspec.equals("next")) { // NOI18N
            type = new ProcessBreakpointType();
        } else if (h.eventspec.equals("step")) { // NOI18N
            type = new ProcessBreakpointType();
        } else if (h.eventspec.equals("stop")) { // NOI18N
            type = new ProcessBreakpointType();
        } else if (h.eventspec.equals("lastrites")) { // NOI18N
            type = new ProcessBreakpointType();
        } else if (h.eventspec.equals("proc_gone")) { // NOI18N
            type = new ProcessBreakpointType();
        } else if (h.eventspec.startsWith("lwp_exit")) { // NOI18N
            type = new ProcessBreakpointType();
        } else if (h.eventspec.startsWith("sig ")) { // NOI18N
            type = new SignalBreakpointType();
        } else if (h.eventspec.startsWith("sysin")) { // NOI18N
            type = new SysCallBreakpointType();
        } else if (h.eventspec.startsWith("sysout")) { // NOI18N
            type = new SysCallBreakpointType();
        } else if (h.eventspec.startsWith("change ")) { // NOI18N
            type = new VariableBreakpointType();
        } else if (h.eventspec.startsWith("infile ")) { // NOI18N
            type = new InfileBreakpointType();
        } else if (h.eventspec.startsWith("timer ")) { // NOI18N
            type = new TimerBreakpointType();
        } else {
	    type = new FallbackBreakpointType();
        }

	NativeBreakpoint newBreakpoint = null;
	if (type != null)
	    newBreakpoint = type.newInstance(NativeBreakpoint.SUBBREAKPOINT);

        return newBreakpoint;
    }

    /*
     * was: IpeHandler.refresh()
     * was: Handler.update()
     */
    private void update(NativeBreakpoint breakpoint, GPDbxHandler gpHandler) {
	try {

	    breakpoint.removeAnnotations();
	    setGenericProperties(breakpoint, gpHandler);
	    setSpecificProperties(breakpoint, gpHandler);

	    // Now done in update:
	    // breakpoint().updateAnnotations();
	} catch (Exception x) {
	    ErrorManager.getDefault().notify(x);
	}
    }


    private static boolean modifierIsSetIn(String name, GPDbxHandler h) {
	for (int i = 0; i < h.nmodifiers; i++) {
	    if (name.equals(h.modifiers[i])) { // NOI18N
		return true;
	    }
	}
	return false;
    }

    private boolean modifierIsSet(GPDbxHandler h, String name) {
	return modifierIsSetIn(name, h);
    }

    private String getModifierValue(GPDbxHandler h, String modifier) {
	for (int i = 0; i < h.nmodifiers; i++) {
	    if (h.modifiers[i].startsWith(modifier + " ")) {	// NOI18N
		return h.modifiers[i].substring(modifier.length()+1);
	    }
	}
	return null;
    }

    private static boolean qmodifierIsSetIn(String name, GPDbxHandler h) {
	for (int i = 0; i < h.nqmodifiers; i++) {
	    if (name.equals(h.qmodifiers[i])) { // NOI18N
		return true;
	    }
	}
	return false;
    }

    private boolean qmodifierIsSet(GPDbxHandler h, String name) {
	return qmodifierIsSetIn(name, h);
    }

    private String getQmodifierValue(GPDbxHandler h, String qmodifier) {
	for (int i = 0; i < h.nqmodifiers; i++) {
	    if (h.qmodifiers[i].startsWith(qmodifier + " ")) {	// NOI18N
		return h.qmodifiers[i].substring(qmodifier.length()+1);
	    }
	}
	return null;
    }


    /**
     * Set all generic properties (those derived from eventspec modifiers)
     * of the given 'Handler' based on the given 'GPDbxHandler'.
     */

    private void setGenericProperties(Handler handler, GPDbxHandler h) {
	/* OLD
	4940627, 6826328
	if (modifierIsSet(h, "-disable")) {		// NOI18N
	    h.enabled = false;
	} else {
	    h.enabled = true;
	}
	*/
	handler.setError(null);
	handler.setEnabled(h.enabled);

	handler.setId(h.id);
    }

    /**
     * Set all generic properties (those derived from eventspec modifiers)
     * of the given 'NativeBreakpoint' based on the given 'GPDbxHandler'.
     */

    private void setGenericProperties(NativeBreakpoint breakpoint,
				      GPDbxHandler h) {

	breakpoint.setOriginalEventspec(h.oeventspec);

	breakpoint.setCount(h.count);
	if (getModifierValue(h, "-count") != null)	// NOI18N
	    breakpoint.setCountLimit(h.count_limit, true);
	else
	    breakpoint.setCountLimit(h.count_limit, false);

	Action action = Action.STOPINSTR;
	if (modifierIsSet(h, "-instr")) { // NOI18N
	    switch (h.action) {
		case GPDbxAction.STOP:
		    action = Action.STOPINSTR;
		    break;
		case GPDbxAction.TRACE:
		    action = Action.TRACEINSTR;
		    break;
		case GPDbxAction.WHEN:
		    action = Action.WHENINSTR;
		    break;
		default:
		    action = Action.STOPINSTR;
		    break;
	    } 
	} else {
	    switch (h.action) {
		case GPDbxAction.STOP:
		    action = Action.STOP;
		    break;
		case GPDbxAction.TRACE:
		    action = Action.TRACE;
		    break;
		case GPDbxAction.WHEN:
		    action = Action.WHEN;
		    break;
		default:
		    action = Action.STOP;
		    break;
	    } 
	}
	breakpoint.setAction(action);
	breakpoint.setScript(h.body);

	String condition = getModifierValue(h, "-if");		// NOI18N
	breakpoint.setCondition(condition);
	
	String qcondition = getQmodifierValue(h, "-if");	// NOI18N
	breakpoint.setQcondition(qcondition);

	breakpoint.setWhileIn(getModifierValue(h, "-in"));	// NOI18N
	breakpoint.setQwhileIn(getQmodifierValue(h, "-in"));	// NOI18N
	
	String thread = getModifierValue(h, "-thread");		// NOI18N
	breakpoint.setThread(thread);

	String lwp = getModifierValue(h, "-lwp");		// NOI18N
	breakpoint.setLwp(lwp);

	breakpoint.setPropEnabled(h.enabled);	// 4940627, 6826328

	breakpoint.setTemp(modifierIsSet(h, "-temp"));		// NOI18N
    }

    private void NEW_setSpecificProperties(NativeBreakpoint b, GPDbxHandler h) {
	// New breakpoint.
	if (h.eventspec == null) {

	} else if (h.eventspec.startsWith("at ")) { // NOI18N

	    if (modifierIsSet(h, "-instr")) { // NOI18N
	    } else {
		LineBreakpoint lb = (LineBreakpoint) b;

		// Get filename
		int fileBegin = h.qeventspec.indexOf('"');
		fileBegin++;
		int fileEnd = h.qeventspec.indexOf('"', fileBegin);
		String filename = h.qeventspec.substring(fileBegin, fileEnd);

		int line = Integer.parseInt(h.qeventspec.substring(fileEnd+2));

		lb.setFileAndLine(filename, line);
	    }

	} else if (h.eventspec.startsWith("in ")) { // NOI18N
	    FunctionBreakpoint fb = (FunctionBreakpoint) b;
	    String function = h.eventspec.substring(3); // Skip 'in '
	    fb.setFunction(function);
	    fb.setSubEvent(FunctionSubEvent.IN);

	} else if (h.eventspec.startsWith("inclass ")) { // NOI18N
	    ClassMethodBreakpoint fb = (ClassMethodBreakpoint) b;
	    String cls = h.eventspec.substring(7); // Skip 'inclass '
	    String qcls = h.qeventspec.substring(7); // Skip 'inclass '
	    fb.setClassName(cls.trim());

	    fb.setQclassName(qcls.trim());

            if (modifierIsSet(h, "-recurse")) // NOI18N
                fb.setRecurse(true);
            else if (modifierIsSet(h, "-norecurse")) // NOI18N
                fb.setRecurse(false);
            else
                fb.setRecurse(false);   // -norecurse by default

	} else {
	    ErrorManager.getDefault().log(
		"generic breakpoint not yet implemented; h.eventspec is " + // NOI18N
		h.eventspec + " and h.qeventspec is " + h.qeventspec); // NOI18N

	}
    }

    private void setSpecificProperties(NativeBreakpoint ibe, GPDbxHandler h) {

	assert ibe.isSubBreakpoint() :
	       "Can only set specifc properties for subbpts"; // NOI18N

	// New breakpoint.
	if (h.eventspec == null) {
	    // No event spec - how are we going to recognize
	    // this breakpoint?
	    // XXX check with Ivan if the eventspec can ever be null
	    assert false;

	} else if (ibe instanceof FallbackBreakpoint) {
	    FallbackBreakpoint fb = (FallbackBreakpoint) ibe;
	    fb.setEventspec(h.eventspec, h.qeventspec);

	} else if (h.eventspec.startsWith("at ")) { // NOI18N

	    if (modifierIsSet(h, "-instr")) { // NOI18N
		InstructionBreakpoint ib = (InstructionBreakpoint) ibe;
		String address = h.eventspec.substring(3); // Skip 'at '
		ib.setAddress(address);

	    } else {
		LineBreakpoint lb = (LineBreakpoint) ibe;

		// Get filename
		int fileBegin = h.qeventspec.indexOf('"');
		fileBegin++;
		int fileEnd = h.qeventspec.indexOf('"', fileBegin);
		String filename = h.qeventspec.substring(fileBegin, fileEnd);
		    
		int line = Integer.parseInt(h.qeventspec.substring(fileEnd+2));

		filename = debugger.remoteToLocal("at", filename); // NOI18N
		lb.setFileAndLine(filename, line);
	    }

	} else if (h.eventspec.startsWith("in ")) { // NOI18N
	    FunctionBreakpoint fb = (FunctionBreakpoint) ibe;
	    String function = h.eventspec.substring(3); // Skip 'in '
	    String qfunction = h.qeventspec.substring(3); // Skip 'in '
	    fb.setFunction(function);
	    fb.setQfunction(qfunction);
	    fb.setSubEvent(FunctionSubEvent.IN);

	} else if (h.eventspec.startsWith("returns")) { // NOI18N
	    FunctionBreakpoint fb = (FunctionBreakpoint) ibe;
	    if (h.eventspec.length() > 7) {
		// Skip 'returns '
		String function = h.eventspec.substring(8);
		String qfunction = h.qeventspec.substring(8);
		fb.setFunction(function);
		fb.setQfunction(qfunction);
	    }
	    fb.setSubEvent(FunctionSubEvent.RETURNS);

	} else if (h.eventspec.startsWith("infunction ")) { // NOI18N
	    FunctionBreakpoint fb = (FunctionBreakpoint) ibe;
	    String function = h.eventspec.substring(11); // Skip 'infunction '
	    String qfunction = h.qeventspec.substring(11); // Skip 'infunction '
	    fb.setFunction(function);
	    fb.setQfunction(qfunction);
	    fb.setSubEvent(FunctionSubEvent.INFUNCTION);
	
	} else if (h.eventspec.startsWith("access ") || h.eventspec.startsWith("modify ") ) { // NOI18N
	    AccessBreakpoint fb = (AccessBreakpoint) ibe;

	    // reset in preparation for setting
	    boolean is_modify = false;
	    fb.setRead(false);
	    if (h.eventspec.startsWith("modify ")) { // NOI18N
		is_modify = true;
	        fb.setWrite(true);
	    }
	    else
		fb.setWrite(false);
	    fb.setExecute(false);

	    int index = 7;	// start after the 'access' keyword
	    // First get access flags
	    while (true && !is_modify) {
		char c = h.eventspec.charAt(index++);
		if (c == ' ') {
		    break;
		}
		switch (c) {
		case 'a': fb.setWhen(AccessBA.AFTER); break;
		case 'b': fb.setWhen(AccessBA.BEFORE); break;
		case 'r': fb.setRead(true); break;
		case 'w': fb.setWrite(true); break;
		case 'x': fb.setExecute(true); break;
		default: break;
		}
	    }

	    int lastIndex = h.eventspec.indexOf(',', index);
	    if (lastIndex == -1) {
		lastIndex = h.eventspec.length();
	    }
	    String address = h.eventspec.substring(index, lastIndex);
	    String length = h.eventspec.substring(lastIndex+2);
	    fb.setAddress(address);
	    fb.setSize(length);
	
	} else if (h.eventspec.startsWith("inmember ")) { // NOI18N
	    ClassMethodBreakpoint fb = (ClassMethodBreakpoint) ibe;
	    String method = h.eventspec.substring(9); // Skip 'inmember '
	    String qmethod = h.qeventspec.substring(9); // Skip 'inmember '
	    fb.setMethodName(method.trim());
	    fb.setQmethodName(qmethod.trim());

	} else if (h.eventspec.startsWith("inclass ")) { // NOI18N
	    ClassMethodBreakpoint fb = (ClassMethodBreakpoint) ibe;
	    String cls = h.eventspec.substring(7); // Skip 'inclass '
	    String qcls = h.qeventspec.substring(7); // Skip 'inclass '
	    fb.setClassName(cls.trim());
	    fb.setQclassName(qcls.trim());

	    if (modifierIsSet(h, "-recurse")) // NOI18N
		fb.setRecurse(true);
	    else if (modifierIsSet(h, "-norecurse")) // NOI18N
		fb.setRecurse(false);
	    else
		fb.setRecurse(false);	// -norecurse by default

	} else if (h.eventspec.startsWith("cond ")) { // NOI18N
	    CondBreakpoint cb = (CondBreakpoint) ibe;
	    String cond = h.eventspec.substring(5); // Skip 'cond '
	    cb.setCond(cond);

	} else if (h.eventspec.startsWith("throw")) { // NOI18N
	    ExceptionBreakpoint eb = (ExceptionBreakpoint) ibe;
	    if (h.eventspec.length() > 6) {
		String exc = h.eventspec.substring(6); // Skip 'throw '
		if (exc.equals("-unhandled"))		// NOI18N
		    eb.setException(ExceptionSpec.UNCAUGHT);
		else if (exc.equals("-unexpected"))	// NOI18N
		    eb.setException(ExceptionSpec.UNEXPECTED);
		else
		    eb.setException(ExceptionSpec.byTag(exc));
	    } else {
		eb.setException(ExceptionSpec.ALL);
	    }

	} else if (h.eventspec.startsWith("fault ")) { // NOI18N
	    FaultBreakpoint fb = (FaultBreakpoint) ibe;
	    String flt = h.eventspec.substring(6); // Skip 'fault '
	    fb.setFault(flt);

	} else if (h.eventspec.startsWith("dlopen")) { // NOI18N
	    LoadObjBreakpoint lob = (LoadObjBreakpoint) ibe;
	    lob.setDlEvent(DlEvent.OPEN);
	    if (h.eventspec.length() > 6) {
		String o = h.eventspec.substring(7); // Skip 'dlopen '
		lob.setLoadObj(o);
	    } else {
		lob.setLoadObj(null);
	    }

	} else if (h.eventspec.startsWith("dlclose")) { // NOI18N
	    LoadObjBreakpoint lob = (LoadObjBreakpoint) ibe;
	    lob.setDlEvent(DlEvent.CLOSE);
	    if (h.eventspec.length() > 7) {
		String o = h.eventspec.substring(8); // Skip 'dlclose '
		lob.setLoadObj(o);
	    } else {
		lob.setLoadObj(null);
	    }

	} else if (h.eventspec.startsWith("inobject")) { // NOI18N
	    ObjectBreakpoint ob = (ObjectBreakpoint) ibe ;
	    String object = h.eventspec.substring(9); // Skip 'inobject'
	    // what we get in object is of the form:
	    // (<cast>) <original-expr> (<hex-addr>)
	    ob.setObject(object);

	    if (modifierIsSet(h, "-recurse")) // NOI18N
		ob.setRecurse(true);
	    else if (modifierIsSet(h, "-norecurse")) // NOI18N
		ob.setRecurse(false);
	    else
		ob.setRecurse(true);	// -recurse by default


	} else if (h.eventspec.startsWith("attach")) { // NOI18N
	    DebuggerBreakpoint db = (DebuggerBreakpoint) ibe;
	    db.setSubEvent(DebuggerEvent.ATTACH);

	} else if (h.eventspec.startsWith("detach")) { // NOI18N
	    DebuggerBreakpoint db = (DebuggerBreakpoint) ibe;
	    db.setSubEvent(DebuggerEvent.DETACH);


	} else if (h.eventspec.startsWith("exit")) { // NOI18N
	    ProcessBreakpoint pb = (ProcessBreakpoint) ibe;
	    // XXX exit can take an exitcode argument - we SHOULD pass that on
	    pb.setSubEvent(ProcessEvent.EXIT);
	    if (h.eventspec.length() > 5) {
		String o = h.eventspec.substring(5); // Skip 'exit '
		pb.setExitCode(o);
	    }

	} else if (h.eventspec.equals("sync")) { // NOI18N
	    ProcessBreakpoint pb = (ProcessBreakpoint) ibe;
	    pb.setSubEvent(ProcessEvent.SYNC);

	} else if (h.eventspec.equals("syncrtld")) { // NOI18N
	    ProcessBreakpoint pb = (ProcessBreakpoint) ibe;
	    pb.setSubEvent(ProcessEvent.SYNCRTLD);

	} else if (h.eventspec.equals("next")) { // NOI18N
	    ProcessBreakpoint pb = (ProcessBreakpoint) ibe;
	    pb.setSubEvent(ProcessEvent.NEXT);

	} else if (h.eventspec.equals("step")) { // NOI18N
	    ProcessBreakpoint pb = (ProcessBreakpoint) ibe;
	    pb.setSubEvent(ProcessEvent.STEP);

	} else if (h.eventspec.equals("stop")) { // NOI18N
	    ProcessBreakpoint pb = (ProcessBreakpoint) ibe;
	    pb.setSubEvent(ProcessEvent.STOP);

	} else if (h.eventspec.equals("lastrites")) { // NOI18N
	    ProcessBreakpoint pb = (ProcessBreakpoint) ibe;
	    pb.setSubEvent(ProcessEvent.LASTRITES);

	} else if (h.eventspec.equals("proc_gone")) { // NOI18N
	    ProcessBreakpoint pb = (ProcessBreakpoint) ibe;
	    pb.setSubEvent(ProcessEvent.GONE);

	} else if (h.eventspec.equals("lwp_exit")) { // NOI18N
	    ProcessBreakpoint pb = (ProcessBreakpoint) ibe;
	    pb.setSubEvent(ProcessEvent.LWP_EXIT);



	} else if (h.eventspec.startsWith("sig ")) { // NOI18N
	    SignalBreakpoint fb = (SignalBreakpoint) ibe;
	    int index = h.eventspec.indexOf(' ', 4);
	    if (index == -1) {
		// Only code, not subcode
		fb.setSignal(h.eventspec.substring(4));
		fb.setSubcode(null);
	    } else {
		fb.setSignal(h.eventspec.substring(4, index));
		fb.setSubcode(h.eventspec.substring(index+1));
	    }

	} else if (h.eventspec.startsWith("sysin")) { // NOI18N
	    SysCallBreakpoint scb = (SysCallBreakpoint) ibe;
	    scb.setEntryExit(SysCallEE.ENTRY);
	    if (h.eventspec.length() > 6) {
		String o = h.eventspec.substring(6); // Skip 'sysin '
		scb.setSysCall(o);
	    } else {
		scb.setSysCall(null);
	    }

	} else if (h.eventspec.startsWith("sysout")) { // NOI18N
	    SysCallBreakpoint scb = (SysCallBreakpoint) ibe;
	    scb.setEntryExit(SysCallEE.EXIT);
	    if (h.eventspec.length() > 7) {
		String o = h.eventspec.substring(7); // Skip 'sysout '
		scb.setSysCall(o);
	    } else {
		scb.setSysCall(null);
	    }

	} else if (h.eventspec.startsWith("change ")) { // NOI18N
	    VariableBreakpoint vb = (VariableBreakpoint) ibe;
	    vb.setVariable(h.eventspec.substring(7));	// Skip 'change '

	} else if (h.eventspec.startsWith("infile ")) { // NOI18N
	    InfileBreakpoint ifb = (InfileBreakpoint) ibe;
	    String filename = h.eventspec.substring(7);	// Skip 'infile '
	    filename = debugger.remoteToLocal("infile", filename); // NOI18N
	    ifb.setFileName(filename);

	} else if (h.eventspec.startsWith("timer ")) { // NOI18N
	    TimerBreakpoint tb = (TimerBreakpoint) ibe;
	    tb.setSeconds(Float.parseFloat(h.eventspec.substring(6))); // Skip 'timer '

	} else {
	    FallbackBreakpoint fb = (FallbackBreakpoint) ibe;
	    fb.setEventspec(h.eventspec, h.qeventspec);
	}
    }





    /**
     * Remove common user input errors: adding a new line or semicolon
     * at the end of the input. See bug 4026788.
     * Actually, we SHOULD do more - we should remove comments, and add
     * semicolons for separated lines (unless they end with \),
     */

    static private String fixBody(String body) {
	if (body == null) {
	    return null; // Common case: avoid a lot of (harmless) work
	}

	int n = body.length(); // current character index
	StringBuffer sb = new StringBuffer(n);
	int lastImportantIndex = 0;
	boolean comment= false; // When true, we're in a line comment
	boolean escape = false; // When true, we've just seen a slash - escape nxt
	boolean quote = false; // When true, we're in a quote
	boolean needSemiColon = false; // When true, haven't seen semicolon yet
	for (int i = 0; i < n; i++) {
	    char c = body.charAt(i);
	    if (!comment) {
		if ((c == '\\') && !quote) {
		    // Skip this character
		    i++;
		} else if (c == '"') {
		    // Next character is important.
		    quote = !quote;
		    sb.append(c);
		} else if ((c == '#') && !quote) {
		    comment = true;
		    // Ignore everything until newline
		} else if (c == '\n') {
		    // pass newlines through
		    sb.append(c);
		    lastImportantIndex = sb.length();
		    needSemiColon = false;
		} else if (c == ';') {
		    if (needSemiColon) {
			sb.append(c);
			lastImportantIndex = sb.length();
		    }
		    needSemiColon = false;
		} else {
		    sb.append(c);
		    if (!(Character.isWhitespace(c) || (c == ';'))) {
			needSemiColon = true;
		    }
		}
		if (!(Character.isWhitespace(c) || (c == ';'))) {
		    lastImportantIndex = sb.length();
		}
	    } else {
		// Comment: nothing to do except get us out of comment mode
		// at the end of the line
		if (c == '\n') {
		    comment = false;
		}
	    }
	}
	sb.setLength(lastImportantIndex);
	if (needSemiColon) {
	    sb.append(';');
	}
	body = sb.toString();
	return body;
    }

    /**
     * A jig to convert NativeBreakpoint's into a string of commands
     * to be passed to dbx.
     *
     * It has a collection of slots per property of NativeBreakpoint (and
     * it's subclasses) that get filled at construction time and then 
     * concatenated using 'generate()'. This separation allows us to
     * change individual properties that we send to dbx w/o modifying
     * the NativeBreakpoint instance (which gets modified when dbx gets back to us).
     */

    private static class EventspecJig {
	public String eventspec;
	public Class<?> eventClass;	// ... of 'event'

	public String keyword;	// like "in, "at"" etc.
	public String exitcode = null; // like "10" in "stop exit 10"

	// for LineBreakpoint
	public String fileLine;

	// for Infile
	public String inFile;

	// for FunctionBreakpoint
	public String function;
	public String qfunction;
	// public FunctionSubEvent sub_event;	N/A we modify the keyword

	// for ClassMethod
	public String cls;
	public String qcls;
	public String method;
	public String qmethod;
	public String recurse;	// also for Object

	// for Object
	public String object;

	// for Access
	public String mode_rwx;
	public String mode_ba;
	public String address;	// also for Instruction
	public String size;

	// for Cond
	public String cond;

	// for Timer
	public String seconds;

	// for Fault
	public String fault;

	// for Signal
	public String signal;
	public String subcode;

	// for SysCall
	public String syscall;

	// for Exception
	public String exception;

	// for Loadobject
	public String loadobj;

	// for Variable
	public String variable;

	// for Fallback
	// public String eventspec;	just reuse this.eventspec


	public EventspecJig(NativeDebugger debugger, NativeBreakpoint breakpoint) {

	    eventClass = breakpoint.getClass();	// dynamic type

	    if (eventClass == LineBreakpoint.class) {
		LineBreakpoint lb = (LineBreakpoint) breakpoint;
		String file = lb.getFileName();
		int line = lb.getLineNumber();
		keyword = " at ";	// NOI18N
		if (file != null && file.length() > 0) {
		    file = debugger.localToRemote("LineBreakpoint", file); // NOI18N
		    fileLine = "\"" + file + "\":" + line; // NOI18N

		    /*
		    if (!file.endsWith("java") && jdbxRunning) {
			    // Must prepend "native" to the action
			    // in jdbx mode when dealing with non java
			    // files
			    action = "native " + action;
		    }
		    */
		} else {
		    fileLine = "" + line; // NOI18N
		}
	    } else if (eventClass == FunctionBreakpoint.class) {
		FunctionBreakpoint fbr = (FunctionBreakpoint) breakpoint;
		FunctionSubEvent se = fbr.getSubEvent();
		if (se.equals(FunctionSubEvent.IN)) {
		    keyword = " in ";		// NOI18N
		} else if (se.equals(FunctionSubEvent.INFUNCTION)) {
		    keyword = " infunction ";	// NOI18N
		} else if (se.equals(FunctionSubEvent.RETURNS)) {
		    keyword = " returns ";	// NOI18N
		}

		function = fbr.getFunction();
		qfunction = fbr.getQfunction();

	    } else if (eventClass == ClassMethodBreakpoint.class) {
		ClassMethodBreakpoint cmb = (ClassMethodBreakpoint)breakpoint;
		cls = null;
		method = null;
		recurse = null;
		// keyword is set in the generation phase because it depends
		// on the actual disposition of 'cls' and 'method'.
		if (cmb.getClassName() == null) {
		    // stop inmethod
		    method = cmb.getMethodName();
		    qmethod = cmb.getQmethodName();
		} else if (cmb.getMethodName() == null) {
		    // stop inclass
		    cls = cmb.getClassName();
		    qcls = cmb.getQclassName();
		    if (cmb.isRecurse())
			recurse = " -recurse ";	// NOI18N
		    else
			recurse = " -norecurse ";	// NOI18N
		} else {
		    // stop in
		    cls = cmb.getClassName();
		    qcls = cmb.getQclassName();
		    method = cmb.getMethodName();
		    qmethod = cmb.getQmethodName();
		}

	    } else if (eventClass == AccessBreakpoint.class) {
		AccessBreakpoint ab = (AccessBreakpoint) breakpoint;

		mode_rwx = "";		// NOI18N
		mode_ba = "";		// NOI18N
		address = null;
		size = null;

		keyword = " access ";	// NOI18N

		if (ab.getWhen() == AccessBA.BEFORE)
		    mode_ba = "b";	// NOI18N
		else
		    mode_ba = "a";	// NOI18N

		if (ab.isRead())
		    mode_rwx += "r";	// NOI18N
		if (ab.isWrite())
		    mode_rwx += "w";	// NOI18N
		if (ab.isExecute())
		    mode_rwx += "x";	// NOI18N

		address = ab.getAddress();
		size = ab.getSize();

	    } else if (eventClass == CondBreakpoint.class) {
		CondBreakpoint cb = (CondBreakpoint) breakpoint;
		keyword = " cond ";	 // NOI18N
		cond = cb.getCond();

	    } else if (eventClass == ObjectBreakpoint.class) {
		ObjectBreakpoint ob = (ObjectBreakpoint) breakpoint;
		keyword = " inobject ";		// NOI18N
		object = ob.getObject();

		if (ob.isRecurse())
		    recurse = " -recurse ";	// NOI18N
		else
		    recurse = " -norecurse ";	// NOI18N

	    } else if (eventClass == TimerBreakpoint.class) {
		TimerBreakpoint tb = (TimerBreakpoint) breakpoint;
		keyword = " timer ";		// NOI18N
		seconds = "" + tb.getSeconds();	// NOI18N

	    } else if (eventClass == InfileBreakpoint.class) {
		InfileBreakpoint ifb = (InfileBreakpoint) breakpoint;
		keyword = " infile ";		// NOI18N
		inFile = "" + ifb.getFileName();	// NOI18N
		inFile = debugger.localToRemote("InfileBreakpoint", inFile); // NOI18N

	    } else if (eventClass == FaultBreakpoint.class) {
		FaultBreakpoint fb = (FaultBreakpoint) breakpoint;
		keyword = " fault ";		// NOI18N
		fault = fb.getFault();

	    } else if (eventClass == SignalBreakpoint.class) {
		SignalBreakpoint sb = (SignalBreakpoint) breakpoint;
		keyword = " sig ";		// NOI18N
		signal = sb.getSignal();
		subcode = sb.getSubcode();

	    } else if (eventClass == SysCallBreakpoint.class) {
		SysCallBreakpoint scb = (SysCallBreakpoint) breakpoint;
		if (scb.getEntryExit() == SysCallEE.EXIT) {
		    keyword = " sysout "; // NOI18N
		} else {
		    keyword = " sysin "; // NOI18N
		}
		syscall = scb.getSysCall();

	    } else if (eventClass == ExceptionBreakpoint.class) {
		ExceptionBreakpoint eb = (ExceptionBreakpoint) breakpoint;
		ExceptionSpec x = eb.getException();
		keyword = " throw ";		// NOI18N
		if (x == ExceptionSpec.UNCAUGHT)
		    exception = "-unhandled";	// NOI18N
		else if (x == ExceptionSpec.UNEXPECTED)
		    exception = "-unexpected";	// NOI18N
		else if (x == ExceptionSpec.ALL)
		    exception = null;
		else
		    exception = x.toString();

	    } else if (eventClass == LoadObjBreakpoint.class) {
		LoadObjBreakpoint lob = (LoadObjBreakpoint) breakpoint;
		if (lob.getDlEvent() == DlEvent.OPEN) {
		    keyword = " dlopen "; // NOI18N
		} else {
		    keyword = " dlclose "; // NOI18N
		}
		loadobj = lob.getLoadObj();

	    } else if (eventClass == VariableBreakpoint.class) {
		VariableBreakpoint vb = (VariableBreakpoint) breakpoint;
		keyword = " change ";	 // NOI18N
		variable = vb.getVariable();

	    } else if (eventClass == InstructionBreakpoint.class) {
		InstructionBreakpoint ib = (InstructionBreakpoint) breakpoint;
		keyword = " at ";	 // NOI18N
		address = ib.getAddress();

	    } else if (eventClass == DebuggerBreakpoint.class) {
		DebuggerBreakpoint db = (DebuggerBreakpoint) breakpoint;
		DebuggerEvent de = db.getSubEvent();
		if (de == DebuggerEvent.ATTACH)
		    keyword = " attach ";	 // NOI18N
		else if (de == DebuggerEvent.DETACH)
		    keyword = " detach ";	 // NOI18N

	    } else if (eventClass == ProcessBreakpoint.class) {
		ProcessBreakpoint pb = (ProcessBreakpoint) breakpoint;
		ProcessEvent pe = pb.getSubEvent();
		if (pe == ProcessEvent.EXIT) {
		    keyword = " exit ";		 // NOI18N
		    exitcode = pb.getExitCode();
		} else if (pe == ProcessEvent.NEXT)
		    keyword = " next ";		 // NOI18N
		else if (pe == ProcessEvent.STEP)
		    keyword = " step ";		 // NOI18N
		else if (pe == ProcessEvent.STOP)
		    keyword = " stop ";		 // NOI18N
		else if (pe == ProcessEvent.LASTRITES)
		    keyword = " lastrites ";		 // NOI18N
		else if (pe == ProcessEvent.GONE)
		    keyword = " proc_gone ";		 // NOI18N
		else if (pe == ProcessEvent.LWP_EXIT)
		    keyword = " lwp_exit ";		 // NOI18N
		else if (pe == ProcessEvent.SYNC)
		    keyword = " sync ";		 // NOI18N
		else if (pe == ProcessEvent.SYNCRTLD)
		    keyword = " syncrtld ";		 // NOI18N

	    } else if (eventClass == FallbackBreakpoint.class) {
		FallbackBreakpoint fb = (FallbackBreakpoint) breakpoint;
		eventspec = " " +		// NOI18N
		            fb.getEventspec();

	    } else {
		eventspec = "Unrecognized breakpoint object" +	// NOI18N
			    breakpoint.getClass().toString();
	    }
	}

	/**
	 * what we get in object address is of the forms:
	 *	<original-expr>				# NewBreakpoint dialog
	 *	(<cast>) <original-expr> (<hex-addr>)	# dbx callback
	 *
	 * Need to send the following to dbx because <original-expr> might
	 * not be in scope anymore and is unlikley to be parsable. Even
	 * a fully qualified version won't do if the expression is "local".
	 *	(<cast>) <hex-addr>
	 *
	 * We do simple pattern-matching to detect the dbx "canonical" form,
	 * but hat can easily be foiled by users, with for example
	 * "(Node *) foo(0x444)"
	 * So ... SHOULD devise a better way of handling all this.
	 * Even better SHOULD have some facilities to easily track objects
	 * from when they are constructed to when they are destroyed ...
	 */
	private String massageObjectAddress(String oa) {
	    int castx = oa.indexOf(')');
	    int addrx = oa.lastIndexOf('(');
	    if (castx != -1 && addrx != -1) {
		// canonical form from dbx

		// keep ()'s:
		String castPart = oa.substring(0, castx+1);

		// drop ()'s:
		String addrPart = oa.substring(addrx+1, oa.length()-1);

		return castPart + " " + addrPart; // NOI18N

	    } else {
		// raw user entry form from NewBreakpoint dialog.
		return oa;
	    }
	}

	public String generate() {
	    if (eventspec == null) {
		if (eventClass == LineBreakpoint.class) {
		    eventspec = keyword + fileLine;

		} else if (eventClass == FunctionBreakpoint.class) {
		    if (qfunction != null)
		        eventspec = keyword + qfunction;
		    else if (function != null)
			eventspec = keyword + function;

		} else if (eventClass == ClassMethodBreakpoint.class) {
		    if (cls == null) {
			// stop in method
			keyword = " inmember ";	// NOI18N
		    } else if (method == null) {
			// stop in class
			keyword = " inclass ";	// NOI18N
		    } else {
			// stop in
			keyword = " in ";		// NOI18N
		    }

		    eventspec = keyword;
		    if (qcls != null) {
			eventspec += qcls;
			if (method != null)
			    eventspec += "::";	// NOI18N
		    } else if (cls != null) {
			eventspec += cls;
			if (method != null)
			    eventspec += "::";	// NOI18N
		    }

		    if (qmethod != null)
			eventspec += qmethod;
		    else if (method != null)
			eventspec += method;
		    
		    if (recurse != null)
			eventspec += recurse;

		} else if (eventClass == AccessBreakpoint.class) {
		    eventspec = keyword;
		    if (mode_rwx != null)
			eventspec += mode_rwx;
		    if (mode_ba != null)
			eventspec += mode_ba;
		    eventspec += " ";	// NOI18N
		    eventspec += address;
		    if (size != null)
			eventspec += ", " + size;	// NOI18N
		} else if (eventClass == CondBreakpoint.class) {
		    eventspec = keyword;
		    if (cond != null)
			eventspec += cond;

		} else if (eventClass == ObjectBreakpoint.class) {
		    eventspec = keyword;
		    if (object != null) {
			// what we get in object is of the form:
			// (<cast>) <original-expr> (<hex-addr>)
			// need to send the following to dbx because
			// <original-expr> might not be in scope anymore.

			eventspec += massageObjectAddress(object);
		    }
		    if (recurse != null)
			eventspec += recurse;

		} else if (eventClass == TimerBreakpoint.class) {
		    eventspec = keyword;
		    if (seconds != null)
			eventspec += seconds;

		} else if (eventClass == InfileBreakpoint.class) {
		    eventspec = keyword;
		    if (inFile != null)
			eventspec += inFile;

		} else if (eventClass == FaultBreakpoint.class) {
		    eventspec = keyword;
		    if (fault != null)
			eventspec += fault;

		} else if (eventClass == SignalBreakpoint.class) {
		    eventspec = keyword;
		    if (signal != null)
			eventspec += signal;
		    if (subcode != null)
			eventspec += " " + subcode;	// NOI18N

		} else if (eventClass == SysCallBreakpoint.class) {
		    eventspec = keyword;
		    if (syscall != null)
			eventspec += syscall;

		} else if (eventClass == ExceptionBreakpoint.class) {
		    eventspec = keyword;
		    if (exception != null)
			eventspec += exception;

		} else if (eventClass == LoadObjBreakpoint.class) {
		    eventspec = keyword;
		    if (loadobj != null)
			eventspec += loadobj;

		} else if (eventClass == VariableBreakpoint.class) {
		    eventspec = keyword;
		    if (variable != null)
			eventspec += variable;

		} else if (eventClass == InstructionBreakpoint.class) {
		    eventspec = keyword;
		    if (address != null)
			eventspec += address;

		} else if (eventClass == DebuggerBreakpoint.class) {
		    eventspec = keyword;

		} else if (eventClass == ProcessBreakpoint.class) {
		    eventspec = keyword;
		    if (exitcode != null)
			eventspec += exitcode;

		} else if (eventClass == FallbackBreakpoint.class) {
		    // eventspec = eventspec;

		} else {
		    eventspec = "<EventspecJig.generate(): bad cls " + eventClass + ">"; // NOI18N
		}
	    }
	    return eventspec;
	}
    }

    private static class HandlerJig {
	public String action;

	public EventspecJig eventspec;

	public String replace;

	public boolean disable;
	public boolean temp;
	public String count;
	public String whileIn;
	public String qwhileIn;
	public String condition;
	public String qcondition;
	public String lwp;
	public String thread;

	public String body;

	public HandlerJig(NativeDebugger debugger, NativeBreakpoint breakpoint) {
	    Action act = breakpoint.getAction();
	    action = actionFor(act,
			       breakpoint instanceof InstructionBreakpoint);
	    eventspec = new EventspecJig(debugger, breakpoint);

	    if (breakpoint.getAction() == Action.WHEN ||
		breakpoint.getAction() == Action.WHENINSTR) {

		body = breakpoint.getScript();
	    }

	    temp = breakpoint.getTemp();
	    disable = !breakpoint.isPropEnabled();
	    whileIn = trim(breakpoint.getWhileIn());
	    qwhileIn = trim(breakpoint.getQwhileIn());
	    condition = trim(breakpoint.getCondition());
	    qcondition = trim(breakpoint.getQcondition());
	    lwp = trim(breakpoint.getLwp());
	    thread = trim(breakpoint.getThread());
	    if (breakpoint.hasCountLimit()) {
		long countLimit = breakpoint.getCountLimit();
		// mimic CountLimit.possiblySetToCurrentCount
		if (countLimit == -1)
		    count = "infinity";			// NOI18N
		else if (countLimit == -2)
		    count = "" + breakpoint.getCount();	// NOI18N
		else
		    count = "" + countLimit;		// NOI18N
	    }
	}

	/**
	 * Utility to convert "empty" Strings to nulls so testing is more
	 * straightforward down the line.
	 */
	public String trim(String str) {
	    if (str == null)
		return null;
	    str = str.trim();
	    if (str.length() == 0)
		return null;
	    return str;
	}


	public void replace(int id) {
	    replace = "" + id;	// NOI18N
	}

	public HandlerCommand generate() {
	    String cmd = action + eventspec.generate();

	    if (replace != null)
		cmd += " -replace " + replace;	// NOI18N

	    if (disable)
		cmd += " -disable ";		// NOI18N
	    if (temp)
		cmd += " -temp ";		// NOI18N
	    if (qwhileIn != null)
		cmd += " -in " + qwhileIn;	// NOI18N
	    else if (whileIn != null)
		 cmd += " -in " + whileIn;	// NOI18N
	    // we don't want full qualified name for condition
	    if (condition != null)
		cmd += " -if " + condition;	// NOI18N
	    else if (qcondition != null)
		 cmd += " -if " + qcondition;// NOI18N
	    if (lwp != null)
		cmd += " -lwp " + lwp;		// NOI18N
	    if (thread != null)
		cmd += " -thread " + thread;	// NOI18N


	    // Note the special treatment in HandlerJig.<init>().
	    if (count != null)
		cmd += " -count " + count;	// NOI18N

	    if (body != null) {
		cmd += " { " + fixBody(body) + " } ";	// NOI18N
	    }

	    return HandlerCommand.makeCommand(cmd);
	}
    }


    static String actionFor(Action act, boolean alwaysInstr) {
	if (alwaysInstr) {
	    if (act == Action.STOPINSTR)
		return "stopi";	// NOI18N
	    else if (act == Action.TRACE)
		return "tracei";	// NOI18N
	    else if (act == Action.TRACEINSTR)
		return "tracei";	// NOI18N
	    else if (act == Action.WHEN)
		return "wheni";	// NOI18N
	    else if (act == Action.WHENINSTR)
		return "wheni";	// NOI18N
	    else if (act == Action.STOP)
		return "stopi";	// NOI18N
	    else
		return "stopi";	// NOI18N
	} else {
	    if (act == Action.STOPINSTR)
		return "stopi";	// NOI18N
	    else if (act == Action.TRACE)
		return "trace";	// NOI18N
	    else if (act == Action.TRACEINSTR)
		return "tracei";	// NOI18N
	    else if (act == Action.WHEN)
		return "when";	// NOI18N
	    else if (act == Action.WHENINSTR)
		return "wheni";	// NOI18N
	    else if (act == Action.STOP)
		return "stop";	// NOI18N
	    else
		return "stopi";	// NOI18N
	}
    }


}
