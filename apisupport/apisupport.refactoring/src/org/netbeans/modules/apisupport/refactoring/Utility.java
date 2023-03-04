/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.apisupport.refactoring;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
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
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
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
            reader = new BufferedReader(new InputStreamReader(fileObject.getInputStream(), StandardCharsets.UTF_8));
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
