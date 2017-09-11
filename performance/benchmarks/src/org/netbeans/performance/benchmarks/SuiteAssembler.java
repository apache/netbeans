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
package org.netbeans.performance.benchmarks;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Creates Suites for all given Test classes.
 */
public final class SuiteAssembler {

    private File root;
    private URLClassLoader loader;
    private File libFolder;
    private File libGenFolder;
    private String rootAsString;
    
    /** New assembler */
    public SuiteAssembler(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException();
        }
        
        root = new File(args[0]).getCanonicalFile();
        
        if (root.isFile()) {
            throw new IllegalArgumentException();
        }
        
        libFolder = new File(root, ".." + File.separatorChar + "lib");
        libGenFolder = new File(libFolder, "libgen");
        
        if (libFolder.exists()) {
            if (libFolder.isFile()) {
                throw new IllegalArgumentException();
            }
            if (libGenFolder.exists()) {
                if (libGenFolder.isFile()) {
                    throw new IllegalArgumentException();
                }
                delete(libGenFolder);
                libGenFolder.mkdirs();
            }
        } else {
            libGenFolder.mkdirs();
        }
        
        rootAsString = root.getCanonicalPath();
        loader = new URLClassLoader(new URL[] { root.toURL() });
    }
    
    /** Recursively deletes dir */
    private static void delete(File dir) throws IOException {
        File[] list = dir.listFiles();
        for (int i = 0; i < list.length; i++) {
            if (list[i].isDirectory()) {
                delete(list[i]);
            } else {
                list[i].delete();
            }
        }
        dir.delete();
    }
    
    /** Assemblies top level suite */
    public void assemblySuite() throws Exception {
        ArrayList classList = new ArrayList(50);
        processFolder(root, classList);
        createSuite(libGenFolder, classList);
        compile(libGenFolder);
    }
    
    /** */
    private void compile(File folder) throws Exception {
        File[] files = folder.listFiles();
        String[] args = new String[files.length + 3];
        args[0] = "javac";
        args[1] = "-classpath";
        args[2] = System.getProperty("java.class.path") + File.pathSeparatorChar + rootAsString;
        
        for (int i = 3; i < args.length; i++) {
            args[i] = files[i - 3].getCanonicalPath();
        }
        
        Process proc = Runtime.getRuntime().exec(args);
        proc.waitFor();
        java.io.InputStream is = proc.getErrorStream();
        
        int c;
        while ((c = is.read()) >= 0) {
            System.out.write(c);
        }
    }
    
    /** Creates a class for running tests */
    private static void createSuite(File libGenFolder, ArrayList classList) throws Exception {
        File dest = new File(libGenFolder, "GenSuite.java");
        StringBuffer buffer = new StringBuffer(3000);
        
        generateSuite(classList, buffer);
        
        Writer writer = new FileWriter(dest);
        writer.write(buffer.toString());
        writer.close();
    }
    
    /** Generates a textual representation */
    private static void generateSuite(ArrayList classList, StringBuffer buffer) {
        buffer.append("package libgen;\n\n");
        buffer.append("import org.netbeans.performance.BenchmarkSuite;\n");
        buffer.append("import junit.framework.TestCase;\n");
        buffer.append("import junit.framework.Test;\n\n");
        buffer.append("public class GenSuite extends TestCase {\n");
        buffer.append("    public GenSuite(String name) {\n");
        buffer.append("        super(name);\n");
        buffer.append("    }\n");
        buffer.append("    public static Test suite() {\n");
        buffer.append("        BenchmarkSuite suite = new BenchmarkSuite();\n");
        for (int i = 0; i < classList.size(); i++) {
            buffer.append("        suite.addBenchmarkClass(");
            buffer.append(classList.get(i).toString());
            buffer.append(".class);\n");
        }
        buffer.append("        return suite;\n");
        buffer.append("    }\n");
        buffer.append("}");
    }    
    
    /** Recursively searches for testable classes */
    private void processFolder(File srcFolder, ArrayList classList) throws Exception {
        File[] list = srcFolder.listFiles();
        String[] name = new String[1];
        
        for (int i = 0; i < list.length; i++) {
            if (list[i].isDirectory()) {
                processFolder(list[i], classList);
            } else if (isTestable(list[i], name)) {
                classList.add(name[0]);
            }
        }
    }
    
    /** Tests whether the test file contains a class that can be teste */
    private boolean isTestable(File test, String[] nameToFillIn) throws Exception {
        String name = test.getCanonicalPath();
        if (! name.endsWith(".java")) {
            return false;
        }
        
        name = name.substring(rootAsString.length(), name.length() - 5).replace(File.separatorChar, '.');
        
        if (name.charAt(0) == '.') {
            name = name.substring(1);
        }
        
        Class klass = Class.forName(name, false, loader);
        int mod = klass.getModifiers();
        
        if (Test.class.isAssignableFrom(klass) && 
            java.lang.reflect.Modifier.isPublic(mod) &&
            !java.lang.reflect.Modifier.isAbstract(mod)) {
                
                nameToFillIn[0] = name;
                return true;
        }
        
        return false;
    }
    
    //public void generateMain
    public static void main(String[] args) throws Exception {
        SuiteAssembler sassembler = new SuiteAssembler(args);
        sassembler.assemblySuite();
    }
}
