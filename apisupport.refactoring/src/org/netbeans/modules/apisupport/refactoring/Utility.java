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

package org.netbeans.modules.apisupport.refactoring;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.netbeans.modules.refactoring.api.Problem;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;


public class Utility {
    
    private static final ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.modules.apisupport.refactoring");   // NOI18N
    
    /** Creates a new instance of Utility */
    private Utility() {
    }

    
    public static Problem addProblemsToEnd(Problem where, Problem what) {
        Problem whereCopy = where;
        err.log("Where: " + where);
        err.log("What: " + what);
        if (what != null) {
            if (where == null) {
                whereCopy = what;
            } else {
                while (where.getNext() != null) {
                    where = where.getNext();
                }
                err.log("Last where: " + where);
                while (what != null) {
                    Problem elem = what;
                    err.log("Elem: " + elem);
                    where.setNext(elem);
                    where = where.getNext();
                    what = what.getNext();
                }
            }
        }
        err.log("wherecopy return: " + whereCopy);
        return whereCopy;
    } 
    
    /**
     * Creates full class name from package name and simple class name
     * @param pkg package name
     * @param simpleName simple class name
     * @return full class name
     */
    public static String getClassName(String pkg, final String simpleName) {
        return (pkg == null || pkg.length() == 0 ? "" : pkg + ".") + simpleName; // NOI18N
    }
    
    static void writeFileFromString(FileObject fileObject, String content) {
        if (content == null) {
            return;
        }
        try {
            OutputStream os = fileObject.getOutputStream();
            try {
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8")); // NOI18N
                writer.print(content);
                writer.flush();
            } finally {
                os.close();
            }
        } catch (IOException exc) {
            //TODO
        }
    }
    
    static String readFileIntoString(FileObject fileObject) {
        BufferedReader reader = null;
        String content = null;
        try {
            StringWriter writer =new StringWriter();
            reader = new BufferedReader(new InputStreamReader(fileObject.getInputStream(), "UTF-8")); // NOI18N
            int chr = reader.read();
            while (chr != -1) {
                writer.write(chr);
                chr = reader.read();
            }
            content = writer.toString();
        } catch (IOException exc) {
            //TODO
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException x) {
                    // ignore
                }
            }
        }
        return content;
    }
    
}
