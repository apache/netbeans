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
