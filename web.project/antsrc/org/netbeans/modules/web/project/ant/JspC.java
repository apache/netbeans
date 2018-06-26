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
