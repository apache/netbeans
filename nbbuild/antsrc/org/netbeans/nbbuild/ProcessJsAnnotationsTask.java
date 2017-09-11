/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013-2014 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Oracle. Portions Copyright 2013-2014 Oracle. All Rights Reserved.
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
package org.netbeans.nbbuild;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.FileUtils;

public final class ProcessJsAnnotationsTask extends Task {
    private File classes;
    private Path cp;
    private File asm;
    
    public ProcessJsAnnotationsTask() {
    }
    
    public void setClasses(File f) {
        classes = f;
    }

    public void setAsm(File f) {
        asm = f;
    }
    
    public Path createClasspath() {
        if (cp == null) {
            cp = new Path(getProject());
        }
        return cp.createPath();
    }

    @Override
    public void execute() throws BuildException {
        if (classes == null) {
            throw new BuildException("root of classes must be specified!");
        }
        File master = new File(new File(classes, "META-INF"), "net.java.html.js.classes");
        if (!master.exists()) {
            return;
        }
        
        LinkedList<URL> arr = new LinkedList<URL>();
        boolean foundAsm = false;
        for (String s : cp.list()) {
            final File f = FileUtils.getFileUtils().resolveFile(getProject().getBaseDir(), s);
            if (f != null) {
                if (f.getName().contains("asm")) {
                    foundAsm = true;
                }
                try {
                    arr.add(f.toURI().toURL());
                } catch (MalformedURLException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }
        if (!foundAsm) {
            URL loc;
            if (asm == null || !asm.exists()) {
                throw new BuildException("Cannot find asm!");
            }
            try {
                loc = asm.toURI().toURL();
            } catch (MalformedURLException ex) {
                throw new BuildException(ex);
            }
            arr.addFirst(loc);
        }
        try {
            arr.addFirst(classes.toURI().toURL());
            URLClassLoader l = new URLClassLoader(arr.toArray(new URL[arr.size()]));
            processClasses(l, master, classes);
        } catch (IOException ex) {
            throw new BuildException("Problem converting JavaScriptXXX annotations", ex);
        }
    }
    
    private void processClasses(ClassLoader l, File master, File f) throws IOException {
        if (!f.exists()) {
            return;
        }
        if (f.isDirectory()) {
            boolean classes = new File(f, "net.java.html.js.classes").exists();
            File[] arr = f.listFiles();
            if (arr != null) {
                for (File file : arr) {
                    if (classes || file.isDirectory()) {
                        processClasses(l, master, file);
                    }
                }
            }
            return;
        }
        
        if (!f.getName().endsWith(".class")) {
            return;
        }
        
        byte[] arr = new byte[(int)f.length()];
        FileInputStream is = new FileInputStream(f);
        try {
            readArr(arr, is);
        } finally {
            is.close();
        }

        byte[] newArr = null;
        try {
            Class<?> fnUtils = l.loadClass("org.netbeans.html.boot.impl.FnUtils");
            Method transform = fnUtils.getMethod("transform", byte[].class, ClassLoader.class);
            
            newArr = (byte[]) transform.invoke(null, arr, l);
            if (newArr == null || newArr == arr) {
                return;
            }
            filterClass(new File(f.getParentFile(), "net.java.html.js.classes"), f.getName());
            filterClass(master, f.getName());
        } catch (Exception ex) {
            throw new BuildException("Can't process " + f, ex);
        }
        log("Processing " + f, Project.MSG_INFO);
        writeArr(f, newArr);        
    }

    private void writeArr(File f, byte[] newArr) throws IOException, FileNotFoundException {
        FileOutputStream os = new FileOutputStream(f);
        try {
            os.write(newArr);
        } finally {
            os.close();
        }
    }

    private static void readArr(byte[] arr, InputStream is) throws IOException {
        int off = 0;
        while (off< arr.length) {
            int read = is.read(arr, off, arr.length - off);
            if (read == -1) {
                break;
            }
            off += read;
        }
    }
    
    private static void filterClass(File f, String className) throws IOException {
        if (!f.exists()) {
            return;
        }
        if (className.endsWith(".class")) {
            className = className.substring(0, className.length() - 6);
        }
        
        BufferedReader r = new BufferedReader(new FileReader(f));
        List<String> arr = new ArrayList<String>();
        boolean modified = false;
        for (;;) {
            String line = r.readLine();
            if (line == null) {
                break;
            }
            if (line.endsWith(className)) {
                modified = true;
                continue;
            }
            arr.add(line);
        }
        r.close();
        
        if (modified) {
            if (arr.isEmpty()) {
                f.delete();
            } else {
                FileWriter w = new FileWriter(f);
                for (String l : arr) {
                    w.write(l);
                    w.write("\n");
                }
                w.close();
            }
        }
    }
}
