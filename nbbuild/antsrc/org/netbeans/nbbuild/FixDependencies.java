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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.FileScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Ant;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.FileUtils;

/** Assistent in changing of build scripts.
 *
 * @author  Jaroslav Tulach
 */
public class FixDependencies extends Task {
    /** Replace*/
    private List<Replace> replaces = new ArrayList<>();
    /** files to fix */
    private FileSet set;
    /** verify target */
    private String tgt;
    /** clean target */
    private String clean;
    /** relative path from module file to build script to use for verification */
    private String ant;
    /** trip only changed */
    private boolean onlyChanged;
    /** fail on error */
    private boolean fail;
    private boolean doSanity = true;
    private boolean strip = true;
    
    /**
     * Only update dependencies for the codebase(s), which match the filter.
     */
    private Pattern moduleFilter;
    
    
    /** tasks to be executed */
    
    /** Initialize. */
    public FixDependencies() {
    }
    
    
    public Replace createReplace () {
        Replace r = new Replace ();
        replaces.add (r);
        return r;
    }
    
    public FileSet createFileset() throws BuildException {
        if (this.set != null) throw new BuildException ("Only one file set is allowed");
        this.set = new FileSet();
        return this.set;
    }

    public void setSanityCheck(boolean s) {
        doSanity = s;
    }
    
    public void setBuildTarget (String s) {
        tgt = s;
    }
    
    public void setCleanTarget (String s) {
        clean = s;
    }
    
    public void setAntFile (String r) {
        ant = r;
    }
    
    public void setStripOnlyChanged (boolean b) {
        onlyChanged = b;
    }
    
    public void setFailOnError (boolean b) {
        fail = b;
    }
    
    public void setStrip(boolean b) {
        this.strip = b;
    }
    
    public void setModuleFilter(String s) {
        if (s == null || "".equals(s)) {
            return;
        }
        moduleFilter = Pattern.compile(s, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public void execute () throws org.apache.tools.ant.BuildException {
        FileScanner scan = this.set.getDirectoryScanner(getProject());
        File dir = scan.getBasedir();
        for (String kid : scan.getIncludedFiles()) {
            File xml = new File(dir, kid);
            if (!xml.exists()) throw new BuildException("File does not exist: " + xml, getLocation());

            log ("Fixing " + xml, Project.MSG_INFO);

            File script = null;
            Ant task = null;
            Ant cleanTask = null;
            boolean compiled = !doSanity;
            if (ant != null && tgt != null) {
                task = (org.apache.tools.ant.taskdefs.Ant)getProject ().createTask ("ant");
                script = FileUtils.getFileUtils().resolveFile(xml, ant);
                if (!script.exists ()) {
                    String msg = "Skipping. Cannot find file " + ant + " from + " + xml;
                    if (fail) {
                        throw new BuildException (msg);
                    }
                    log(msg, Project.MSG_ERR);
                    continue;
                }
                task.setAntfile (script.getPath ());
                task.setDir (script.getParentFile ());
                task.setTarget (tgt);
                if (clean != null) {
                    cleanTask = (Ant) getProject().createTask("ant");
                    cleanTask.setAntfile (script.getPath ());
                    cleanTask.setDir (script.getParentFile ());
                    cleanTask.setTarget (clean);
                }
                try {
                    if (doSanity || strip) {
                        // before we do anything else, let's verify that we build
                        if (cleanTask != null) {
                            log ("Cleaning " + clean + " in " + script, org.apache.tools.ant.Project.MSG_INFO);
                            cleanTask.execute ();
                            compiled = false;
                        }
                        if (doSanity) {
                            log ("Sanity check executes " + tgt + " in " + script, org.apache.tools.ant.Project.MSG_INFO);
                            task.execute ();
                            compiled = true;
                        }
                    } else {
                        compiled = true; // just for the case - fix will clean up.
                    }
                } catch (BuildException ex) {
                    if (fail) {
                        throw ex;
                    }

                    log("Skipping. Could not execute " + tgt + " in " + script, org.apache.tools.ant.Project.MSG_ERR);
                    continue;
                }
            }
            
            try {
                boolean change = fix (xml, script, task, cleanTask, compiled);
                if (!strip || onlyChanged && !change) {
                    continue;
                }
                simplify (xml, script, task, cleanTask);
            } catch (IOException ex) {
                throw new BuildException (ex, getLocation ());
            }
        }
    }
    
    /** Modifies the xml file to replace dependencies wiht new ones.
     * @return true if there was a change in the file
     */
    private boolean fix (File file, File script, org.apache.tools.ant.taskdefs.Ant task, org.apache.tools.ant.taskdefs.Ant cleanTask, boolean compiled) throws IOException, BuildException {
        int s = (int)file.length ();
        byte[] data = new byte[s];
        try (InputStream is = new FileInputStream(file)) {
            if (s != is.read (data)) {
                throw new BuildException ("Cannot read " + file);
            }
        }
        
        String stream = new String (data);
        String old = stream;
        data = null;

        try {
            DEPS: for (Replace r : replaces) {
                int md = stream.indexOf("<module-dependencies");
                if (md == -1) {
                    throw new BuildException("No module dependencies in " + file);
                }

                int ed = stream.indexOf ("</module-dependencies>", md);
                ed = ed == -1 ? stream.indexOf ("<module-dependencies/>", md) : ed;
                if (ed == -1) {
                    ed = stream.length();
                }
                if (moduleFilter != null && !moduleFilter.matcher(r.codeNameBase).matches()) {
                    continue;
                }
                String alldeps = stream.substring(md, ed);
                int idx = stream.indexOf ("<code-name-base>" + r.codeNameBase + "</code-name-base>", md);
                if (idx == -1 || idx > ed) continue;

                int from = stream.lastIndexOf ("<dependency>", idx);
                if (from == -1) throw new BuildException ("No <dependency> tag before index " + idx + " in " + file);
                int after = stream.indexOf ("</dependency", idx);
                if (after == -1) throw new BuildException ("No </dependency> tag after index " + idx + " in " + file);
                after = after + "</dependency>".length ();

                String remove = stream.substring (from, after);
                if (r.addCompileTime && remove.indexOf ("compile-dependency") == -1) {
                    int fromAfter = "<dependency".length();
                    int nonSpace = findNonSpace (remove, fromAfter);
                    String spaces = remove.substring (fromAfter, nonSpace);
                    remove = remove.substring (0, fromAfter) + spaces + "<compile-dependency/>" + remove.substring (fromAfter);
                }

                StringBuffer sb = new StringBuffer ();
                sb.append (stream.substring (0, from));
                boolean prefix = false;

                StringBuffer save = new StringBuffer();

                int mods = r.modules.size();
                int changed = 0;

                // first check whether the module's own version is sufficient:
                Module triggerModule = null;
                boolean specVersionMissing = false;
                for (Module m : r.modules) {
                    if (m.codeNameBase.equals(r.codeNameBase)) {
                        log ("Checking dependency: " + r.codeNameBase, Project.MSG_INFO);
                        if (remove.contains("<implementation-version/>")) {
                            continue DEPS;
                        }

                        String b = "<specification-version>";
                        int specBeg = remove.indexOf(b);
                        int specEnd = remove.indexOf("</specification-version>");
                        if (specBeg != -1 && specEnd != -1) {
                            String v = remove.substring(specBeg + b.length(), specEnd);
                            if (olderThanOrEqual(m.specVersion, v)) {
                                continue DEPS;
                            }
                        } else {
                            log("No specification version present for dependency: " + m.codeNameBase, Project.MSG_WARN);
                            specVersionMissing = true;
                        }
                        triggerModule = m;
                        break;
                    }
                }
                // reached only if the dependencies include OLDER or NONE specification version of the r.codeNameBase
                for (Module m : r.modules) {
                    // check if the dependencies already contain the injected module; if so, rather 
                        if (m != triggerModule && alldeps.indexOf ("<code-name-base>" + m.codeNameBase + "</code-name-base>") != -1) {
                            continue;
                        }

                    if (prefix) {
                        sb.append('\n');
                        for (int i = from - 1; stream.charAt(i) == ' '; i--) {
                            sb.append(' ');
                        }
                    }

                    changed++;
                    log ("Adding dependency: " + m, Project.MSG_INFO);
                    int beg = remove.indexOf (r.codeNameBase);
                    int aft = beg + r.codeNameBase.length ();
                    sb.append (remove.substring (0, beg));
                    sb.append (m.codeNameBase);
                    String a = remove.substring (aft);
                    if (specVersionMissing) {
                        int rd = a.indexOf("<run-dependency");
                        StringBuilder rep = new StringBuilder("<run-dependency>");
                        if (m.releaseVersion != null) {
                            rep.append("<release-version>").append(m.releaseVersion).append("</release-version>");
                        }
                        if (m.specVersion != null) {
                            rep.append("<specification-version>").append(m.specVersion).append("</specification-version>");
                        }
                        if (rd != -1) {
                            int end = a.indexOf("</run-dependency>");
                            String newA = a.substring(0, rd) + rep.toString();
                            if (end != -1) {
                                newA = newA + a.substring(end);
                            } else {
                                end = a.indexOf("<run-dependency/>");
                                if (end != -1) {
                                    newA = newA + "</run-dependency>" + a.substring(end + 17);
                                }
                            }
                            a = newA;
                        } else {
                            rep.append("</run-dependency>");
                            a = a + rep.toString();
                        }
                    } else {
                        if (m.specVersion != null) {
                            a = a.replaceAll (
                                "<specification-version>[0-9\\.]*</specification-version>", 
                                "<specification-version>" + m.specVersion + "</specification-version>"
                            );
                        }
                        if (m.releaseVersion == null) {
                            a = a.replaceAll (
                                "<release-version>[0-9]*</release-version>[\n\r ]*", 
                                ""
                            );
                        }
                    }
                    sb.append (a);
                    prefix = true;
                    // check whether the dependency is sufficient, or replacements must be made
                    if (remove.contains("<compile-dependency")) {
                        save = new StringBuffer(sb.toString());
                        save.append(stream.substring(after));
                        String x = save.toString();
                        if (!old.equals (x)) {
                            FileWriter fw = new FileWriter (file);
                            fw.write (x);
                            fw.close ();

                            try {
                                if (compiled && cleanTask != null) {
                                    log ("Cleaning " + clean + " in " + script, Project.MSG_INFO);
                                    cleanTask.execute ();
                                    compiled = false;
                                }
                                if (!compiled && task != null) {
                                    log ("Executing target " + tgt + " in " + script, Project.MSG_INFO);
                                    task.execute ();
                                    log ("Dependency on " + m + " is sufficient, skipping the rest", Project.MSG_INFO);
                                    compiled = true;
                                    stream = x;
                                    continue DEPS;
                                } else {
                                    log ("Cannot verify dependency on " + m + " no build target, clean target or build script set.", Project.MSG_INFO);
                                }
                            } catch (BuildException ex) {
                                log ("Compilation failed: ", ex, Project.MSG_INFO);
                                fw = new FileWriter (file);
                                fw.write (old);
                                fw.close ();
                                if (changed == mods) {
                                    throw new BuildException("Could not fix dependencies.");
                                }
                            }
                        }
                    }
                }

                sb.append (stream.substring (after));

                stream = sb.toString ();
            }

            if (!old.equals (stream)) {
                try (FileWriter fw = new FileWriter (file)) {
                    fw.write (stream);
                }
                return true;
            } else {
                return false;
            }
        } finally {
            // leave compiled so other modules may benefit from the module-auto-deps
            if (!compiled && task != null) {
                log ("Executing target " + tgt + " in " + script, Project.MSG_INFO);
                task.execute ();
            }
        }
    } // end of fix
    
    private void simplify (
        File file, File script, org.apache.tools.ant.taskdefs.Ant task, org.apache.tools.ant.taskdefs.Ant cleanTask
    ) throws IOException, BuildException {
        if (ant == null || tgt == null) {
            return;
        }
        
        int s = (int)file.length ();
        byte[] data = new byte[s];
        try (InputStream is = new FileInputStream(file)) {
            if (s != is.read (data)) {
                throw new BuildException ("Cannot read " + file);
            }
        }
        
        String stream = new String (data);
        String old = stream;

        int first = -1;
        int last = -1;
        int begin = -1;
        StringBuffer success = new StringBuffer ();
        StringBuffer sb = new StringBuffer ();
        for (;;) {
            if (cleanTask != null) {
                log ("Cleaning " + clean + " in " + script, Project.MSG_INFO);
                cleanTask.execute ();
            }
            
            int from = stream.indexOf ("<dependency>", begin);
            if (from == -1) {
                break;
            }
            
            if (first == -1) {
                first = from;
            }
            
            int after = stream.indexOf ("</dependency", from);
            if (after == -1) throw new BuildException ("No </dependency> tag after index " + from + " in " + file);
            after = findNonSpace (stream, after + "</dependency".length ());
            
            last = after;
            begin = last;

            // write the file without the
            try (FileWriter fw = new FileWriter (file)) {
                fw.write (stream.substring (0, from) + stream.substring (after));
            }
            
            String dep = stream.substring (from, after);
            if (dep.indexOf ("compile-dependency") == -1) {
                // skip non-compile dependencies
                sb.append (stream.substring (from, after));
                continue;
            }
            if (dep.indexOf("org.netbeans.libs.javacapi") != -1) {
                // should be kept even if can compile using rt.jar version
                sb.append (stream.substring (from, after));
                continue;
            }
            
            
            int cnbBeg = dep.indexOf ("<code-name-base>");
            int cnbEnd = dep.indexOf ("</code-name-base>");
            if (cnbBeg != -1 && cnbEnd != -1) {
                dep = dep.substring (cnbBeg + "<code-name-base>".length (), cnbEnd);
            }
            

            String result;
            try {
                log ("Executing target " + tgt + " in " + script, Project.MSG_INFO);
                task.execute ();
                result = "Ok";
                success.append (dep);
                success.append ("\n");
            } catch (BuildException ex) {
                result = "Failure";
                // ok, this is needed dependency
                sb.append (stream.substring (from, after));
            }
            log ("Removing dependency " + dep + ": " + result, Project.MSG_INFO);
            
        }

        if (first != -1) {
            // write the file without the
            try (FileWriter fw = new FileWriter (file)) {
                fw.write (stream.substring (0, first) + sb.toString () + stream.substring (last));
            }
        }
        
        log ("Final verification runs " + tgt + " in " + script, Project.MSG_INFO);
        // now verify, if there is a failure then something is wrong now
        task.execute ();
        if (success.length () == 0) {
            log ("No dependencies removed from " + script);
        } else {
            log ("Removed dependencies from " + script + ":\n" + success);
        }
    } // end of simplify

    /**
     * Finds first non-whitespace after the tag. The tag may be commented out, sometimes
     * the comment appear as &lt;tagname-->. So if > appears right after tagname, consume it
     * first, then search for non-whitespaces. Otherwise, break on the nearest nonwhitespace
     * @param where
     * @param from
     * @return 
     */
    private static int findNonSpace (String where, int from) {
        if (from < where.length() && where.charAt(from) == '>') {
            from++;
        }
        while (from < where.length () && Character.isWhitespace (where.charAt (from))) {
            from++;
        }
        return from;
    }

    private static boolean olderThanOrEqual(String v1, String v2) {
        String[] arr1 = v1.split("\\.");
        String[] arr2 = v2.split("\\.");
        int min = Math.min(arr1.length, arr2.length);
        for (int i = 0; i < min; i++) {
            int i1 = Integer.parseInt(arr1[i]);
            int i2 = Integer.parseInt(arr2[i]);

            if (i1 == i2) {
                continue;
            }
            return i1 < i2;
        }
        return arr1.length <= arr2.length;
    }

    public static final class Replace extends Object {
        String codeNameBase;
        List<Module> modules = new ArrayList<>();
        boolean addCompileTime;

        public void setCodeNameBase (String s) {
            codeNameBase = s;
        }
        
        public void setAddCompileTime (boolean b) {
            addCompileTime = b;
        }
        
        public Module createModule () {
            Module m = new Module ();
            modules.add (m);
            return m;
        }

    }
            
    public static final class Module extends Object {
        String codeNameBase;
        String specVersion;
        String releaseVersion;
        
        public void setCodeNameBase (String s) {
            codeNameBase = s;
        }
        
        
        public void setSpec (String s) {
            specVersion = s;
        }
        
        public void setRelease (String r) {
            releaseVersion = r;
        }
        
        public String toString() {
            return codeNameBase + (releaseVersion != null ? releaseVersion : "") + " > " + specVersion;
        }
    }
}
