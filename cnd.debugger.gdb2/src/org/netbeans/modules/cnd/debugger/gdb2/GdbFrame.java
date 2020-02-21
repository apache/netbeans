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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIValue;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MITList;
import org.netbeans.modules.cnd.debugger.common2.debugger.Frame;
import org.netbeans.modules.cnd.debugger.common2.debugger.Thread;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIResult;

public final class GdbFrame extends Frame {
  
    private MITList MIframe;
    private final List<GdbLocal> argsArray = new ArrayList<GdbLocal>();
    private String fullname;
    
    public GdbFrame(GdbDebuggerImpl debugger, MIValue frame, 
            Thread thread) {
        this(debugger, frame, null, thread);
    }

    public GdbFrame(GdbDebuggerImpl debugger, MIValue frame, 
            MIResult frameargs, 
            Thread thread) {
	super(debugger, thread);
	if (frame == null) {
	    return;
        }

	MIframe = frame.asTuple();
	if (MIframe == null) {
	    return;
        }

        frameno = MIframe.getConstValue("level"); // NOI18N
        pc = MIframe.getConstValue("addr"); // NOI18N
        func = MIframe.getConstValue("func"); // NOI18N
        lineno = MIframe.getConstValue("line"); // NOI18N
	source = MIframe.getConstValue("file", MIframe.getConstValue("from")); // NOI18N
        fullname = MIframe.getConstValue("fullname", null); // NOI18N
        
        MITList args_list = (MITList) MIframe.valueOf("args"); // NOI18N
//	if (args_list != null && frameargs != null) {
//	    System.out.println("GdbFrame Impossible "); // NOI18N
//        }

	// handle args info
	if (frameargs != null) {
            args_list = (MITList) frameargs.value().asTuple().valueOf("args"); // NOI18N
        }

        if (args_list != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(" ("); // NOI18N
            if (debugger.getVerboseStack()) {
                int args_count = args_list.size();
                    // iterate through args list
                for (int vx=0; vx < args_count; vx++) {
                    if (vx != 0) {
                        sb.append(", "); // NOI18N
                    }                    
                    if (args_list.get(vx) instanceof MIValue) {
                        MIValue arg = (MIValue)args_list.get(vx);

                        sb.append( arg.asTuple().getConstValue("name")); // NOI18N
                        MIValue value = arg.asTuple().valueOf("value"); // NOI18N
                        if (value != null) {
                            argsArray.add(new GdbLocal(arg));
                            sb.append("="); // NOI18N
                            sb.append(value.asConst().value());
                        }
                    } else if (args_list.get(vx) instanceof MIResult) {
                        //mromashova_bz#269898: 
                        //we have the list of names, no values at all
                        //which is correct!! as we will never call -stack-list-arguments 1 
                        //to get values, will call -stack-list-arguments 0 to get args names only
                        sb.append(((MIResult)args_list.get(vx)).value().asConst().value());
                    }
                }
            }
            sb.append(")"); // NOI18N
            args = sb.toString();
	}

	range_of_hidden = false;
        current = false;
	optimized = false;
	attr_user_call = false;
	attr_sig = 0;
	attr_signame = "";
    }

    public MITList getMIframe() {
	return MIframe;
    }
    
    public String getLevel() {
        return frameno;
    }

//    public List<GdbLocal> getArgsList() {
//	return argsArray;
//    }
    
    @Override
    public String getFullPath() {
        return debugger.remoteToLocal("Gdb frame", debugger.fmap().engineToWorld(fullname)); //NOI18N
    }

    public String getEngineFullName() {
        return fullname;
    }
    
//    void varUpdated(String name, String value) {
//        for (GdbLocal var : argsArray) {
//            if (var.getName().equals(name)) {
//                var.setValue(value);
//            }
//        }
//    }
}
