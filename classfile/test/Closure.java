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

import org.netbeans.modules.classfile.*;
import java.io.*;
import java.util.*;

/**
 * Closure:  report all classes which this file references in one
 * way or another.  Note: this utility won't find classes which are
 * dynamically loaded.
 *
 * @author Thomas Ball
 */
public class Closure {
    String thisClass;
    Set<String> closure;

    Closure(String spec) {
        thisClass = spec;
    }

    void buildClosure(boolean includeJDK)
      throws IOException {
        if (closure != null)
            return;
        closure = new HashSet<String>();
        Set<String> visited = new HashSet<String>();
        Stack<ClassName> stk = new Stack<ClassName>();
        ClassName thisCN = ClassName.getClassName(thisClass.replace('.', '/'));
        stk.push(thisCN);
        visited.add(thisCN.getExternalName());

        while (!stk.empty()) {
            // Add class to closure.
            ClassName cn = (ClassName)stk.pop();
            InputStream is = findClassStream(cn.getType());
	    if (is == null) {
		System.err.println("couldn't find class: " + 
                                   cn.getExternalName());
		continue;
	    }
            ClassFile cfile = new ClassFile(is);
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

    void dumpClosure(PrintStream out) {
        Iterator iter = new TreeSet(closure).iterator();
        while (iter.hasNext())
            out.println((String)iter.next());
    }

    Iterator dependencies() {
        return closure.iterator();
    }

    private InputStream findClassStream(String className) {
        InputStream is = 
            ClassLoader.getSystemClassLoader().getResourceAsStream(className + ".class");
        return is;
    }

    /**
     * An error routine which displays the command line usage
     * before exiting.
     */
    public static void usage() {
        System.err.println(
            "usage:  java Closure [-includejdk] <class> [ <class> ...]");
        System.exit(1);
    }

    public static void main(String[] args) {
        if (args.length == 0)
            usage();

        boolean includeJDK = false;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-includejdk"))
                includeJDK = true;
            else if (args[i].charAt(0) == '-')
                usage();
            else {
                try {
                    Closure c = new Closure(args[i]);
                    c.buildClosure(includeJDK);
                    c.dumpClosure(System.out);
                } catch (IOException e) {
                    System.err.println("error accessing \"" + args[i] + 
                                       "\": " + e.toString());
                }
            }
        }
    }
}
