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

package org.netbeans.modules.cnd.debugger.dbx;

import org.netbeans.modules.cnd.debugger.common2.debugger.Address;
import org.netbeans.modules.cnd.debugger.common2.debugger.Location;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerImpl;
import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.BreakpointModel;
import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.DisFragModel;
import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.Disassembly;

/**
 *
 *
 */
public class DbxDisassembly extends Disassembly {
    private long address;
    
//    private final Map<Integer,String> regNames = new HashMap<Integer,String>();
//    private final Map<Integer,String> regValues = new HashMap<Integer,String>();
//    private final Set<Integer> regModified = new  HashSet<Integer>();

    public DbxDisassembly(NativeDebuggerImpl debugger, BreakpointModel breakpointModel) {
        super(debugger, breakpointModel);
    }
    
    public void update(DisFragModel model) {
        attachUpdateListener();

        DisText text = new DisText();

        for (DisFragModel.Line srcLine : model) {
            text.addLine(srcLine);
        }

        text.save();
        setText(text);
        if (text.isEmpty()) {
            reloadDis(true);
            return;
        }
    }

    public void stateUpdated() {
        reloadDis(false);
    }
    
    @Override
    protected void reload() {
        reloadDis(false);
    }

    @Override
    public void reset() {
        address = 0;
        super.reset();
    }
    
    private void reloadDis(boolean force) {
        if (!opened) {
            reloadFailed();
            return;
        }
        Location visitedLocation = getDebugger().getVisitedLocation();
        if (visitedLocation == null) {
            reloadFailed();
            return;
        }
        long curAddress = visitedLocation.pc();
        if (curAddress == 0) {
            reloadFailed();
            return;
        }
        
        if (curAddress != address) {
            requestMode = RequestMode.FILE_SRC;
        } else if (!force || requestMode == RequestMode.NONE) {
            reloadFailed();
            return;
        }

        // update if needed
        if (getAddressLine(curAddress) == -1) {
            switch (requestMode) {
                case FILE_SRC:
                    getDebugger().disController().requestDis(true);
                    requestMode = RequestMode.ADDRESS_SRC;
                    break;
                case ADDRESS_SRC:
                    String addr = Address.toHexString0x(curAddress, true);
                    getDebugger().disController().requestDis(addr, 100, true);
                    requestMode = RequestMode.NONE;
                    break;
            }
        }
        
        address = curAddress;
    }
    
    /*public String getNextAddress(String address) {
        //TODO : can use binary search
        synchronized (lines) {
            for (Iterator<Line> iter = lines.iterator(); iter.hasNext();) {
                Line line = iter.next();
                if (line.address.equals(address)) {
                    // Fix for IZ:131372 (Step Over doesn't work in Disasm)
                    // return next address only for call instructions
                    if (line.instruction.startsWith("call") && iter.hasNext()) { // NOI18N
                        return iter.next().address;
                    }
                    return "";
                }
            }
            return "";
        }
    }*/
    
    /*
     * Reads expressions like param="value"
     * in this case readValue("param") will return "value"
     */
//    private static String readValue(String name, String msg, int pos) {
//        String paramHeader = name + "=\""; // NOI18N
//        int start = msg.indexOf(paramHeader, pos);
//        if (start != -1) {
//            start += paramHeader.length();
//            int end = msg.indexOf("\"", start + 1); // NOI18N
//            if (end != -1) {
//                return msg.substring(start, end);
//            }
//        }
//        return "";
//    }
//    
//    private String getHeader() {
//        String res = NbBundle.getMessage(DbxDisassembly.class, "LBL_Disassembly_Window"); // NOI18N
//        if (functionName.length() > 0) {
//            res += "(" + functionName + ")"; // NOI18N
//        }
//        return res;
//    }
}
