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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.php.dbgp.breakpoints;

import org.netbeans.modules.php.dbgp.DebugSession;
import org.netbeans.modules.php.dbgp.packets.BrkpntSetCommand;

/**
 * Represent breakpoint for method call and return types.
 *
 * @author ads
 *
 */
public class FunctionBreakpoint extends AbstractBreakpoint {
    private final Type myType;
    private final String myFunctionName;

    public enum Type {
        CALL(BrkpntSetCommand.Types.CALL),
        RETURN(BrkpntSetCommand.Types.RETURN);

        Type(BrkpntSetCommand.Types type) {
            myType = type;
        }

        public BrkpntSetCommand.Types getType() {
            return myType;
        }

        public static Type forType(BrkpntSetCommand.Types type) {
            Type[] types = Type.values();
            for (Type typ : types) {
                if (type == typ.getType()) {
                    return typ;
                }
            }
            return null;
        }

        public static Type forString(String str) {
            for (Type type : Type.values()) {
                if (type.toString().equals(str)) {
                    return type;
                }
            }
            return null;
        }
        private final BrkpntSetCommand.Types myType;

    }

    public FunctionBreakpoint(Type type, String functionName) {
        myType = type;
        myFunctionName = functionName;
    }

    public Type getType() {
        return myType;
    }

    public String getFunction() {
        return myFunctionName;
    }

    @Override
    public boolean isSessionRelated(DebugSession session) {
        return true;
    }

}
