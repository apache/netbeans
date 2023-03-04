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
