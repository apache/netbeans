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
        
        LinkedList<URL> arr = new LinkedList<>();
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
        try (FileInputStream is = new FileInputStream(f)) {
            readArr(arr, is);
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
        try (FileOutputStream os = new FileOutputStream(f)) {
            os.write(newArr);
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
        
        List<String> arr = new ArrayList<>();
        boolean modified = false;
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
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
        }
        
        if (modified) {
            if (arr.isEmpty()) {
                f.delete();
            } else {
                try (FileWriter w = new FileWriter(f)) {
                    for (String l : arr) {
                        w.write(l);
                        w.write("\n");
                    }
                }
            }
        }
    }
}
