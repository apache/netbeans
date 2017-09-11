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

package org.netbeans.projectopener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Milan Kubec
 */
public class ArgsHandler {
    
    // Arguments will be:
    // projecturl ... project(s) zip file URL, HTTP protocol, REQUIRED parameter
    // minversion ... minumum version of NetBeans that could open the project(s)
    // mainproject ... path (in the zip file) to the project folder that will be opened as main
    
    private String args[];
    private Map argMap = new HashMap();
    private List addArgs = new ArrayList();
    
    public ArgsHandler(String[] args) {
        this.args = args;
        int index = 0;
        while (index < args.length) {
            String arg = args[index];
            if (arg.startsWith("-")) {
                String argName = arg.substring(1);
                if ("projecturl".equals(argName) || "minversion".equals(argName) || "mainproject".equals(argName)) {
                    if (args.length >= index + 2) {
                        String argVal = args[++index];
                        if (!argVal.startsWith("-")) {
                            argMap.put(argName, argVal);
                        } else {
                            // arg is missing value
                            argMap.put(argName, null);
                        }
                    } else {
                        // arg is missing value
                        argMap.put(argName, null);
                        index++;
                    }
                } else {
                    // unknown args beginning with '-'
                    addArgs.add(argName);
                    index++;
                }
            } else {
                // there are some args that do not begin with '-'
                index++;
            }
        }
    }
    
    public String getArgValue(String argName) {
        return (String) argMap.get(argName);
    }
    
    public List getAdditionalArgs() {
        return addArgs;
    }
    
    public String getAllArgs() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i] + " ");
        }
        return sb.toString().trim();
    }
    
}
