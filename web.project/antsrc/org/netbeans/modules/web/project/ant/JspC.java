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
package org.netbeans.modules.web.project.ant;

import java.util.ArrayList;
import org.apache.jasper.JasperException;
import org.apache.jasper.compiler.Localizer;

/**
 * Ant task that extends org.apache.jasper.JspC and dumps smap for easier error reporting.
 *
 * @author Petr Jiricka
 */
public class JspC extends org.apache.jasper.JspC {

    private static final String SOURCE_VM = "-compilerSourceVM"; // NOI18N
    private static final String TARGET_VM = "-compilerTargetVM"; // NOI18N
    private static final String JAVA_ENCODING = "-javaEncoding"; // NOI18N
    private static final String SCHEMAS = "-schemas"; // NOI18N
    private static final String DTDS = "-dtds"; // NOI18N
    private static final String SYS_CLASSPATH = "-sysClasspath"; // NOI18N

    public static void main(String arg[]) {
        if (arg.length == 0) {
            System.out.println(Localizer.getMessage("jspc.usage")); // NOI18N
        } else {
            try {
                JspC jspc = new JspC();
                ArrayList args = new ArrayList();
                for (int i = 0; i < arg.length; i++) {
                    String oldArg = arg[i];
                    if (oldArg.contains(TARGET_VM)) {
                        String version = oldArg.substring(TARGET_VM.length()).trim();
                        version = adjustVersion(version);
                        jspc.setCompilerTargetVM(version);
                    } else if (oldArg.contains(SOURCE_VM)) {
                        String version = oldArg.substring(SOURCE_VM.length()).trim();
                        version = adjustVersion(version);
                        jspc.setCompilerSourceVM(version);
                    } else if (oldArg.contains(JAVA_ENCODING)) {
                        String javaEncoding = oldArg.substring(JAVA_ENCODING.length()).trim();
                        jspc.setJavaEncoding(javaEncoding);
                    } else if (oldArg.contains(SCHEMAS)) {
                        String schemas = oldArg.substring(SCHEMAS.length()).trim();
                        JspC.setSchemaResourcePrefix(schemas);
                    } else if (oldArg.contains(DTDS)) {
                        String dtds = oldArg.substring(DTDS.length()).trim();
                        JspC.setDtdResourcePrefix(dtds);
                    } else if (oldArg.contains(SYS_CLASSPATH)) {
                        String sysClassPath = oldArg.substring(SYS_CLASSPATH.length()).trim();
                        jspc.setSystemClassPath(sysClassPath);
                    } else {
                        args.add(oldArg);
                    }
                }

                String[] newArgs = new String[args.size()];
                newArgs = (String[]) args.toArray(newArgs);
                jspc.setArgs(newArgs);
                jspc.execute();
            } catch (JasperException je) {
                System.err.println(je);
                //System.err.println(je.getMessage());
                System.exit(1);
            }
        }
    }

    // #135568
    private static String adjustVersion(String version) {
        if ("1.6".equals(version)) { // NOI18N
            return "1.5"; // NOI18N
        }
        return version;
    }

    public boolean isSmapSuppressed() {
        return false;
    }

    public boolean isSmapDumped() {
        return true;
    }

    public boolean getMappedFile() {
        return true;
    }
    
}
