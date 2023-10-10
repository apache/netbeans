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
package org.netbeans.nbbuild;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.util.FileUtils;

/**
 * Replace paths prefixes with variables. 
 * Designed for netbeans.dest dir and test.dist.dir variables 
 */
public class ShorterPaths extends Task {

    /** dir is prefix and name is name of variable
     * <shorterpaths in="inputpropname" out="outpropNames">
     *     <replacement name="property_name" dir="directory"/>
     * </shorterpaths>
     */
    public static class Replacement {

        String name;
        File dir;
        File excluded;

        public void setName(String name) {
            this.name = name;
        }

        public void setDir(File dir) {
            this.dir = dir;
        }

        public void setExcluded(File excluded) {
            this.excluded = excluded;
        }

        @Override
        public String toString() {
            return dir + (excluded != null ? " - " + excluded : "") + " => ${" + name + "}";
        }
    }
    private List<Replacement> replacements = new LinkedList<>(); // List<Nestme>

    public Replacement createReplacement() {
        Replacement r = new Replacement();
        replacements.add(r);
        return r;
    }

    public void addReplacement(Replacement r) {
        replacements.add(r);
    }
    private Path in;

    public void setIn(Path p) {
        if (in == null) {
            in = p.createPath();
        }
        in.append(p);
    }

    public Path createIn() {
        if (in == null) {
            in = new Path(getProject());
        }
        return in;
    }

    public void setinRef(Reference r) {
        createIn().setRefid(r);
    }
    // <customtask path="foo:bar"/>
    // <customtask>
    //     <path>
    //         <pathelement location="foo"/>
    //     </path>
    // </customtask>
    // Etc.
    String out;

    public void setOut(String out) {
        this.out = out;
    }
    String extraLibs;

    public void setExtraLibs(String extraLibs) {
        this.extraLibs = extraLibs;
    }
    File extraLibsDir;

    public void setExtraLibsDir(File extraLibsDir) {
        this.extraLibsDir = extraLibsDir;
    }
    File testProperties;

    public void setTestProperties(File testProperties) {
        this.testProperties = testProperties;
    }

    @Override
    public void execute() throws BuildException {
        // TODO code here what the task actually does:
        String paths[] = in.list();
        StringBuffer nbLibBuff = new StringBuffer();
//        Path nbLibPath = new Path(getProject());
        StringBuffer externalLibBuf = new StringBuffer();
        try {
            for (int i = 0; i < paths.length; i++) {
                String path = paths[i];
                File file = new File(path);
                // check if file exists
                if (file.exists()) {
                    // add it on classpath
                    path = file.getCanonicalPath();
                    simplyPath(path, externalLibBuf, nbLibBuff);
                } else {
                    log("Path element " + file + " doesn't exist.", Project.MSG_VERBOSE);
                }
            }
            if (out != null) {
                define(out, nbLibBuff.toString());
            }
            if (this.extraLibs != null) {
                define(extraLibs, externalLibBuf.toString());
            }

            if (testProperties != null) {
                // create properties file
                try (PrintWriter pw = new PrintWriter(testProperties)) {
                    // copy extra unit.test.properties
                    Map<String,Object> properties = getProject().getProperties();
                    StringBuffer outProp = new StringBuffer();
                    for (Map.Entry<String, Object> it : properties.entrySet()) {
                        String name = it.getKey();

                        if (name.matches("test-(unit|qa-functional)-sys-prop\\..+")) {
                            if (name.equals("test-unit-sys-prop.xtest.data")) {
                                // ignore overring xtest.data.dir, data.zip placed to standard location
                                continue;
                            }
                            //
                            outProp.setLength(0);
                            for (String path : tokenizePath(it.getValue().toString())) {
                                replacePath(path, outProp);
                            }
                            pw.println(name.replaceFirst("^test-(unit|qa-functional)-sys-prop\\.", "test-sys-prop.") + "=" + outProp);
                        } else if (name.startsWith("test.config")) {
                            pw.println(name + "=" + it.getValue());
                        } else if ("requires.nb.javac".equals(name)) {
                            pw.println(name + "=" + it.getValue());
                        }
                    }
                    pw.println("extra.test.libs=" + externalLibBuf.toString());
                    pw.println("test.run.cp=" + nbLibBuff.toString());
                }
            }
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
    }

    /** Replace absolute path with ${a.prop}/relpath and concatenate resulting
     * path to nbLibBuff. If replacement does not exist copy file on the path
     * to extra lib folder and add the path to externalLibBuf.
     * @param path path to be replaced
     * @param externalLibBuf extra lib paths buffer
     * @param nbLibBuff nb lib paths buffer
     * @throws IOException 
     */
    private void simplyPath(String path, final StringBuffer externalLibBuf, final StringBuffer nbLibBuff) throws IOException {
        boolean bAppend = false;
        File file = new File(path);
        if (file.exists()) {
            // file exists, try to to replace the path with ${a.prop}/relpath
            //
            path = file.getAbsolutePath();
            for (Replacement repl : replacements) {
                String dirCan = repl.dir.getCanonicalPath();
                if (path.startsWith(dirCan) && (repl.excluded == null || !path.startsWith(repl.excluded.getCanonicalPath()))) {
                    if (nbLibBuff.length() > 0) {
                        nbLibBuff.append(":\\\n");
                    }

                    nbLibBuff.append("${").append(repl.name).append("}");
                    // postfix + unify file separators to '/'
                    nbLibBuff.append(path.substring(dirCan.length()).replace(File.separatorChar, '/'));
                    bAppend = true;
                    break;
                }
            }
            if (!bAppend) {
                String fName = copyExtraLib(file);
                if (fName != null) {
                    if (externalLibBuf.length() > 0) {
                        externalLibBuf.append(":\\\n");
                    }
                    externalLibBuf.append("${extra.test.libs.dir}/").append(fName);
                }
            }

        } else {
            if (nbLibBuff.length() > 0) {
                nbLibBuff.append(":\\\n");
            }
            nbLibBuff.append(path);
        }

    }

    /** Replace absolute path with ${a.prop}/relpath and concatenate resulting
     * path to pathsBuff.
     * @param path path to be replaced by one of registered replacements
     * @param pathsBuff buffer containing whole path
     */
    private void replacePath(String path, final StringBuffer pathsBuff) throws IOException {
        File file = new File(path);
        if (file.exists()) {
            path = file.getAbsolutePath();
        }
        boolean replacementFound = false;
        if (pathsBuff.length() > 0) {
            pathsBuff.append(":\\\n");
        }
        for (Replacement repl : replacements) {
            String dirCan = repl.dir.getCanonicalPath();
            if (path.startsWith(dirCan)) {
                pathsBuff.append("${").append(repl.name).append("}");
                // postfix + unify file separators to '/'
                pathsBuff.append(path.substring(dirCan.length()).replace(File.separatorChar, '/'));
                replacementFound = true;
                break;
            }
        }
        if (!replacementFound) {
            // append without property replacement
            pathsBuff.append(path.replace(File.separatorChar, '/'));
        }
    }

    private void define(String prop, String val) {
        log("Setting " + prop + "=" + val, Project.MSG_VERBOSE);
        String old = getProject().getProperty(prop);
        if (old != null && !old.equals(val)) {
            getProject().log("Warning: " + prop + " was already set to " + old, Project.MSG_WARN);
        }
        getProject().setNewProperty(prop, val);
    }

    private String copyExtraLib(File file) throws IOException {
        if (extraLibsDir == null || !extraLibsDir.isDirectory() || !file.isFile()) {
            return null;
        }
        File copy = new File(extraLibsDir, file.getName());
        boolean wasCopied = copyMissing(file, copy);
        // copy Class-Path extensions if available
        if (wasCopied && file.getName().endsWith(".jar")) {
            String cp;
            try {
                try (JarFile jf = new JarFile(file)) {
                    Manifest manifest = jf.getManifest();
                    cp = manifest != null ? manifest.getMainAttributes().getValue(Name.CLASS_PATH) : null;
                }
            } catch (IOException x) {
                log("Could not parse " + file + " for Class-Path", Project.MSG_WARN);
                cp = null;
            }
            if (cp != null) {
                for (String ext : cp.split(" ")) {
                    // copy CP extension with relative path to keep link dependency from manifest
                    copyMissing(new File(file.getParentFile(), ext), new File(extraLibsDir, ext));
                }
            }
        }
        return copy.getName();
    }
    
    /** Copies source file to target location only if it is missing there.
     * @param file source file
     * @param copy target file
     * @return true if file was successfully copied, false if source is the same
     * as target
     * @throws IOException if target file exists and it is not the same as 
     * source file
     */
    private boolean copyMissing(File file, File copy) throws IOException {
        if (FileUtils.getFileUtils().contentEquals(file, copy)) {
            return false;
        } else if (copy.isFile()) {
            // Could try to copy to a different name, but this is probably something that should be fixed anyway:
            throw new IOException(file + " is not the same as " + copy + "; will not overwrite");
        }
        log("Copying " + file + " to extralibs despite " + replacements);
        FileUtils.getFileUtils().copyFile(file, copy);
        return true;
    }

    /**
     * Split an Ant-style path specification into components.
     * Tokenizes on <code>:</code> and <code>;</code>, paying
     * attention to DOS-style components such as <samp>C:\FOO</samp>.
     * Also removes any empty components.
     * Copied from org.netbeans.spi.project.support.ant.PropertyUtils.
     * @param path an Ant-style path (elements arbitrary) using DOS or Unix separators
     * @return a tokenization of that path into components
     */
    public static String[] tokenizePath(String path) {
        List<String> l = new ArrayList<>();
        StringTokenizer tok = new StringTokenizer(path, ":;", true); // NOI18N
        char dosHack = '\0';
        char lastDelim = '\0';
        int delimCount = 0;
        while (tok.hasMoreTokens()) {
            String s = tok.nextToken();
            if (s.length() == 0) {
                // Strip empty components.
                continue;
            }
            if (s.length() == 1) {
                char c = s.charAt(0);
                if (c == ':' || c == ';') {
                    // Just a delimiter.
                    lastDelim = c;
                    delimCount++;
                    continue;
                }
            }
            if (dosHack != '\0') {
                // #50679 - "C:/something" is also accepted as DOS path
                if (lastDelim == ':' && delimCount == 1 && (s.charAt(0) == '\\' || s.charAt(0) == '/')) {
                    // We had a single letter followed by ':' now followed by \something or /something
                    s = "" + dosHack + ':' + s;
                    // and use the new token with the drive prefix...
                } else {
                    // Something else, leave alone.
                    l.add(Character.toString(dosHack));
                    // and continue with this token too...
                }
                dosHack = '\0';
            }
            // Reset count of # of delimiters in a row.
            delimCount = 0;
            if (s.length() == 1) {
                char c = s.charAt(0);
                if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                    // Probably a DOS drive letter. Leave it with the next component.
                    dosHack = c;
                    continue;
                }
            }
            l.add(s);
        }
        if (dosHack != '\0') {
            //the dosHack was the last letter in the input string (not followed by the ':')
            //so obviously not a drive letter.
            //Fix for issue #57304
            l.add(Character.toString(dosHack));
        }
        return l.toArray(new String[l.size()]);
    }
}
