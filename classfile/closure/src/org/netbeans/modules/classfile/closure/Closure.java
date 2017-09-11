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
 * Software is Sun Microsystems, Inc. Portions Copyright 2000-2001 Sun
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
 *
 */
/*
 * Contributor(s): Thomas Ball
 */

package org.netbeans.modules.classfile.closure;

import org.netbeans.modules.classfile.*;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Closure:  report all classes which this file references in one
 * way or another.  Note: this utility won't find classes which are
 * dynamically loaded.
 *
 * @author Thomas Ball
 */
public class Closure {
    Set<String> closure;
    ClassLoader loader;
    
    Closure(ClassLoader loader) {
        this.loader = loader;
    }

    void buildClosure(String[] classes, boolean includeJDK, boolean useFileNames)
      throws IOException {
        if (closure != null)
            return;
        closure = new HashSet<String>();
        Set<String> visited = new HashSet<String>();
        Stack<ClassName> stk = new Stack<ClassName>();
        for (String cls : classes) {
            ClassName thisCN = ClassName.getClassName(cls.replace('.', '/'));
            stk.push(thisCN);
            visited.add(thisCN.getExternalName());

            while (!stk.empty()) {
                // Add class to closure.
                ClassName cn = stk.pop();
                String classFile = cn.getType() + ".class";
                InputStream is = loader.getResourceAsStream(classFile);
                if (is == null) {
                    System.err.println("couldn't find class: " + 
                                       cn.getExternalName());
                    continue;
                }
                ClassFile cfile = new ClassFile(is);
                if (useFileNames)
                    closure.add(classFile);
                else
                    closure.add(cfile.getName().getExternalName());

                ConstantPool pool = cfile.getConstantPool();
                Iterator refs = pool.getAllClassNames().iterator();
                while (refs.hasNext()) {
                    ClassName cnRef = (ClassName)refs.next();
                    String cname = cnRef.getExternalName();
                    if (cname.indexOf('[') != -1) {
                        // skip arrays
                    } else if (!includeJDK && 
                               (cname.startsWith("java.") || 
                                cname.startsWith("javax.") ||
                                cname.startsWith("sun.") ||
                                cname.startsWith("com.sun.corba") ||
                                cname.startsWith("com.sun.image") ||
                                cname.startsWith("com.sun.java.swing") ||
                                cname.startsWith("com.sun.naming") ||
                                cname.startsWith("com.sun.security"))) {
                        // if directed, skip JDK references
                    } else {
                        boolean isNew = visited.add(cname);
                        if (isNew)
                            stk.push(cnRef);
                    }
                }
            }
        }
    }

    void dumpClosure(PrintStream out) {
        Iterator iter = new TreeSet(closure).iterator();
        while (iter.hasNext())
            out.println((String)iter.next());
    }

    Iterator dependencies() {
        return closure.iterator();
    }

    /**
     * An error routine which displays the command line usage
     * before exiting.
     */
    public static void usage() {
        System.err.println(
            "usage:  java Closure [-includejdk] [-filenames] <class> [ <class> ...]");
        System.exit(1);
    }

    public static void main(String[] args) {
        if (args.length == 0)
            usage();

        boolean includeJDK = false;
        boolean useFilenames = false;
        String classpath = null;
        int i = 0;
        while (i < args.length) {
            if (args[i].equals("-includejdk"))
                includeJDK = true;
            else if (args[i].equals("-filenames"))
                useFilenames = true;
            else if (args[i].equals("-classpath") && i+1 < args.length)
                classpath = args[++i];
            else if (args[i].charAt(0) == '-')
                usage();
            else 
                break;
            i++;
        }
        
        if (i == args.length)
            usage();
        else {
            try {
                String[] classes = new String[args.length - i];
                System.arraycopy(args, i, classes, 0, classes.length);
                Closure c = new Closure(createLoader(classpath));
                c.buildClosure(classes, includeJDK, useFilenames);
                c.dumpClosure(System.out);
            } catch (IOException e) {
                System.err.println(e.toString());
            }
        }
    }
    
    private static ClassLoader createLoader(String classpath) throws IOException {
        if (classpath == null)
            return Closure.class.getClassLoader();
        
        try {
            String[] paths = classpath.split(File.pathSeparator);
            URL[] urls = new URL[paths.length];
            for (int i = 0; i < paths.length; i++) {
                urls[i] = new File(paths[i]).toURL();
            }
            return new URLClassLoader(urls);
        } catch (Exception ex) {
            throw new IOException(ex.toString());
        }
    }
}
