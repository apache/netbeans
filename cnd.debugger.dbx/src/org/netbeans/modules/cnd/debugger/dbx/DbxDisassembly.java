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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
