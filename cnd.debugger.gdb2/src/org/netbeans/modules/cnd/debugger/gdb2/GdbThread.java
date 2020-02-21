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


package org.netbeans.modules.cnd.debugger.gdb2;

import org.netbeans.modules.cnd.debugger.common2.debugger.ModelChangeDelegator;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.Thread;
import org.netbeans.modules.cnd.debugger.common2.debugger.Frame;

public final class GdbThread extends Thread {

    private String line;
    private String file;
    private final String tid;
    private final String name;
    private String state;
    private Frame curFrame;

    GdbThread(NativeDebugger debugger, 
            ModelChangeDelegator updater, 
            String tid, 
            String name, 
            Frame[] stack,
            String state) {
	super(debugger, updater);
	this.tid = tid;
        this.name = name;
        this.state = state;
        line = "";
        file = "";
        current_function = "";
        address = "";
        setStack(stack);
    }
    
    GdbThread(NativeDebugger debugger, ModelChangeDelegator updater, String consoleLine) {
        super(debugger, updater);
        // try our best to parse the line
        
        // skip current thread symbol
        if (consoleLine.startsWith("* ")) { //NOI18N
            consoleLine = consoleLine.substring(2);
        }
        // parse id
        assert Character.isDigit(consoleLine.charAt(0)) : "invalid thread line: " + consoleLine;
        int pos = 0;
        while (Character.isDigit(consoleLine.charAt(pos))) {
            pos++;
        }
        tid = consoleLine.substring(0, pos);
        // put the rest into name
        name = consoleLine.substring(pos);
        line = "";
        file = "";
        current_function = "";
        state = "";
//        stack = new Frame[0]; // TODO if necessary use setStack(new Frame[0])
    }

    @Override
    public String getName() {
	return name;
    }

    public String getId() {
	return tid;
    }

    @Override
    public String getFile() {
	return file;
    }

    @Override
    public String getLine() {
	return line;
    }

    @Override
    public boolean hasEvent() {
	return false;
    }

    @Override
    public String getLWP() {
        return null; // Not supported
    }

    @Override
    public Integer getPriority() {
        return null; // Not supported
    }

    public Frame getCurFrame() {
        return curFrame;
    }

    @Override
    public void setStack(Frame[] stack) {
        super.setStack(stack);
        
        if (stack != null) {
        for (Frame frame : stack) {
                if (frame.isCurrent()) {
                    curFrame = frame;
                    line = frame.getLineNo();
                    file = frame.getSource();
                    current_function = frame.getFunc();
                    address = frame.getCurrentPC();
                }
            }
        }
    }

    @Override
    public String getStartFunction() {
        return null; // Not supported
    }

    @Override
    public String getStartupFlags() {
        return null; // Not supported
    }

    @Override
    public String getState() {
        return state;
    }

    @Override
    public boolean isSuspended() {
        return "stopped".equals(state); //NOI18N
    }
}
