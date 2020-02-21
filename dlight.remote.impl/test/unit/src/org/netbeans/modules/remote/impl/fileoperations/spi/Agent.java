/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.remote.impl.fileoperations.spi;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;

/**
 *
 */
public class Agent {
    public static void main(String[] args){
        if (args.length < 1) {
            System.err.println("Not enough parameters."); // NOI18N
            System.err.println("Usage:"); // NOI18N
            System.err.println("java org.netbeans.modules.remote.impl.fileoperations.Agent fileName"); // NOI18N
            return;
        }
        String filePath = args[0];
        File file = new File(filePath);
        System.out.println("exists="+file.exists());
        System.out.println("canWrite="+file.canWrite());
        System.out.println("isDirectory="+file.isDirectory());
        System.out.println("isFile="+file.isFile());
        System.out.println("getName="+file.getName());
        System.out.println("getAbsolutePath="+file.getAbsolutePath());
        System.out.println("getParent="+file.getParent());
        String[] list = file.list();
        if (list == null) {
            System.out.println("list="+list);
        } else {
            StringBuilder buf = new StringBuilder("list=");
            for(String s : list) {
                buf.append(s);
                buf.append(';');
            }
            System.out.println(buf.toString());
        }
    }
    
    private final ExecutionEnvironment execEnv;
    private final String pathToClass;
    
    public Agent(ExecutionEnvironment execEnv, String pathToClass) {
        this.execEnv = execEnv;
        this.pathToClass = pathToClass;
    }
    
    public Map<String, Object> execute(String filePath) {
        ExitStatus res = ProcessUtils.executeInDir(pathToClass, execEnv, "java", Agent.class.getName(), filePath);
        if (res.exitCode == 0) {
            String[] split = res.getOutputString().split("\n");
            Map<String, Object> map = new HashMap<>();
            for(String s : split) {
                String key = s.substring(0, s.indexOf('='));
                String value = s.substring(s.indexOf('=')+1);
                if ("exists".equals(key) || "canWrite".equals(key) ||
                    "isDirectory".equals(key) || "isFile".equals(key)) {
                    map.put(key, value.equals("true"));
                } else if ("getName".equals(key) || "getAbsolutePath".equals(key) ||
                           "getParent".equals(key)) {
                    map.put(key, value);
                } else if ("list".equals(key)) {
                    if ("null".equals(value)) {
                        map.put(key, null);
                    } else {
                        if (value.indexOf(';') < 0) {
                            map.put(key, new String[0]);
                        } else {
                            map.put(key, value.split(";"));
                        }
                    }
                }
            }
            return map;
        } else {
            System.err.println("Cannot run java on "+execEnv.getDisplayName());
            System.err.println(res.getErrorString());
            return null;
        }
    }
}
