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

import java.util.*;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.*;
import java.util.jar.*;
import java.io.*;
import java.util.zip.ZipEntry;

/** Create XML files corresponding to the set of known modules
 * without actually running the IDE.
 * @author Jesse Glick
 */
public class CreateModuleXML extends Task {

    private final List<FileSet> enabled = new ArrayList<>(1);
    private final List<FileSet> disabled = new ArrayList<>(1);
    private final List<FileSet> autoload = new ArrayList<>(1);
    private final List<FileSet> eager = new ArrayList<>(1);
    private final List<FileSet> hidden = new ArrayList<>(1);
    private Integer level;
    private boolean checkBundle = true;

    /** Add a set of module JARs that should be enabled.
     */
    public void addEnabled(FileSet fs) {
        enabled.add(fs);
    }
    
    /** Add a set of module JARs that should be disabled.
     */
    public void addDisabled(FileSet fs) {
        disabled.add(fs);
    }
    
    /** Add a set of module JARs that should be autoloads.
     */
    public void addAutoload(FileSet fs) {
        autoload.add(fs);
    }
    
    /** Add a set of module JARs that should be eager modules.
     */
    public void addEager(FileSet fs) {
        eager.add(fs);
    }
    
    /** Add a set of module JARs that should be hidden.
     */
    public void addHidden(FileSet fs) {
        hidden.add(fs);
    }
    
    private File xmldir = null;
    
    /** Set the modules directory where XML will be stored.
     * Normally the system/Modules/ directory in an installation.
     */
    public void setXmldir(File f) {
        xmldir = f;
    }

    private File trackingdir;
    /** Directory where to generate the update tracking file. Does not need
     * to be specified.
     */
    public void setUpdateTrackingRoot(File f) {
        trackingdir = f;
    }

    private boolean failOnMissingManifest = true;
    /** By default true. Set to false if JAR files without proper manifest
     * shall be ignored.
     */
    public void setFailOnMissingManifest(boolean fail) {
        failOnMissingManifest = fail;
    }
    public void setStartLevel(String level) {
        if (level.isEmpty()) {
            this.level = null;
        } else {
            this.level = Integer.parseInt(level);
        }
    }
    
    public void setStrictCheck(boolean check) {
        this.checkBundle = check;
    }
    
    private List<String> enabledNames = new ArrayList<>(50);
    private List<String> disabledNames = new ArrayList<>(10);
    private List<String> autoloadNames = new ArrayList<>(10);
    private List<String> eagerNames = new ArrayList<>(10);
    private List<String> hiddenNames = new ArrayList<>(10);
    private String hiddenList;

    /**
     * In addition to &gt;hidden&lt; nested fileset, list of hidden modules can
     * be specified as comma-separated list in attribute 'hidden'.
     * @param hiddenList
     */
    public void setHidden(String hiddenList) {
        // Only hidden parameters can be specified as list of absolute file paths,
        // enabled, eager and autoload modules need relative path to create proper module xml
        this.hiddenList = hiddenList;
    }

    @Override
    public void execute() throws BuildException {
        if (xmldir == null) throw new BuildException("Must set xmldir attribute", getLocation());
        if (!xmldir.exists ()) {
            if (!xmldir.mkdirs()) throw new BuildException("Cannot create directory " + xmldir, getLocation());
        }
        if (enabled.isEmpty() && disabled.isEmpty() && autoload.isEmpty() && eager.isEmpty() && hidden.isEmpty()
                && hiddenList == null) {
            log("Warning: <createmodulexml> with no modules listed", Project.MSG_WARN);
        }
        for (FileSet fs : enabled) {
            scanModules(fs, true, false, false, false, enabledNames);
        }
        for (FileSet fs : disabled) {
            scanModules(fs, false, false, false, false, disabledNames);
        }
        for (FileSet fs : autoload) {
            scanModules(fs, false, true, false, false, autoloadNames);
        }
        for (FileSet fs : eager) {
            scanModules(fs, false, false, true, false, eagerNames);
        }
        for (FileSet fs : hidden) {
            scanModules(fs, false, false, false, true, hiddenNames);
        }
        if (hiddenList != null && hiddenList.length() > 0)
            scanModulesInList(hiddenList, false, false, false, true, hiddenNames);
        Collections.sort(enabledNames);
        Collections.sort(disabledNames);
        Collections.sort(autoloadNames);
        Collections.sort(eagerNames);
        Collections.sort(hiddenNames);
        if (!enabledNames.isEmpty()) {
            log("Enabled modules: " + enabledNames, Project.MSG_VERBOSE);
        }
        if (!disabledNames.isEmpty()) {
            log("Disabled modules: " + disabledNames, Project.MSG_VERBOSE);
        }
        if (!autoloadNames.isEmpty()) {
            log("Autoload modules: " + autoloadNames, Project.MSG_VERBOSE);
        }
        if (!eagerNames.isEmpty()) {
            log("Eager modules: " + eagerNames, Project.MSG_VERBOSE);
        }
        if (!hiddenNames.isEmpty()) {
            log("Hidden modules: " + hiddenNames, Project.MSG_VERBOSE);
        }
    }
    
    private void scanModules(FileSet fs, boolean isEnabled, boolean isAutoload, boolean isEager, boolean isHidden, List<String> names) throws BuildException {
        FileScanner scan = fs.getDirectoryScanner(getProject());
        File dir = scan.getBasedir();
        for (String kid : scan.getIncludedFiles()) {
            File module = new File(dir, kid);
            scanOneModule(module, kid, isEnabled, isAutoload, isEager, isHidden, names);
        }
    }

    private void scanModulesInList(String modulesList, boolean isEnabled, boolean isAutoload,
            boolean isEager, boolean isHidden, List<String> names) throws BuildException {
        String[] modules = modulesList.split(",");
        for (String modulePath : modules) {
            File module = new File(modulePath);
            scanOneModule(module, null, isEnabled, isAutoload, isEager, isHidden, names);
        }
    }

    private void scanOneModule(
        File module, String kid, boolean isEnabled,
        boolean isAutoload, boolean isEager,
        boolean isHidden, List<String> names
    ) throws BuildException {
        UpdateTracking ut = null;
        if (trackingdir != null) {
            ut = new UpdateTracking(trackingdir.getAbsolutePath());
        }

        processModule(module, kid, isEnabled, isAutoload, isEager, isHidden, names, ut);
    }
    private void processModule(
        File module, String kid, boolean isEnabled,boolean isAutoload,
        boolean isEager, boolean isHidden,
        List<String> names, UpdateTracking ut
    ) throws BuildException {
        if (!module.exists()) {
            throw new BuildException("Module file does not exist: " + module, getLocation());
        }
        if (!module.getName().endsWith(".jar")) {
            throw new BuildException("Only *.jar may be listed, check the fileset: " + module, getLocation());
        }
        try {
            try (JarFile jar = new JarFile(module)) {
                Manifest m = jar.getManifest();
                Attributes attr = null;
                String codename = null;
                if (m != null) {
                    attr = m.getMainAttributes();
                    codename = JarWithModuleAttributes.extractCodeName(attr);
                }
                if (codename == null) {
                    if (failOnMissingManifest) {
                        throw new BuildException("Missing manifest tag OpenIDE-Module; " + module + " is not a module", getLocation());
                    } else {
                        log("No module manifest in " + module, Project.MSG_WARN);
                        return;
                    }
                }
                if (codename.endsWith(" ") || codename.endsWith("\t")) {
                    // #62887
                    throw new BuildException("Illegal trailing space in OpenIDE-Module value from " + module, getLocation());
                }
                int idx = codename.lastIndexOf('/');
                String codenamebase;
                if (idx == -1) {
                    codenamebase = codename;
                } else {
                    codenamebase = codename.substring(0, idx);
                }
                File xml = new File(xmldir, codenamebase.replace('.', '-') + ".xml");
                if (xml.exists()) {
                    // XXX should check that the old file actually matches what we would have written
                    log("Will not overwrite " + xml + "; skipping...", Project.MSG_VERBOSE);
                    return;
                }
                String displayname = attr.getValue("OpenIDE-Module-Name");
                if (displayname == null) {
                    String bundle = attr.getValue("OpenIDE-Module-Localizing-Bundle");
                    if (bundle != null) {
                        // Display name actually found in a bundle, not manifest.
                        ZipEntry entry = jar.getEntry(bundle);
                        InputStream is;
                        if (entry != null) {
                            is = jar.getInputStream(entry);
                        } else {
                            File moduleloc = new File(new File(module.getParentFile(), "locale"), module.getName());
                            if (!moduleloc.isFile()) {
                                throw new BuildException("Expecting localizing bundle: " + bundle + " in: " + module);
                            }
                            try (JarFile jarloc = new JarFile(moduleloc)) {
                                ZipEntry entry2 = jarloc.getEntry(bundle);
                                if (entry2 == null) {
                                    throw new BuildException("Expecting localizing bundle: " + bundle + " in: " + module);
                                }
                                is = jarloc.getInputStream(entry2);
                            }
                        }
                        try {
                            Properties p = new Properties();
                            p.load(is);
                            displayname = p.getProperty("OpenIDE-Module-Name");
                        } finally {
                            is.close();
                        }
                    }
                }
                if (displayname == null) {
                    displayname = codename;
                }
                names.add(displayname);
                String spec = attr.getValue("OpenIDE-Module-Specification-Version");
                UpdateTracking.Version v = null;
                if (ut != null) {
                    v = ut.addNewModuleVersion(codename, spec);
                    v.addFileForRoot(module);
                }
                if (isHidden) {
                    File h = new File(xml.getParentFile(), xml.getName() + "_hidden");
                    h.createNewFile();
                } else {
                    try (OutputStream os = new FileOutputStream(xml)) {
                        PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
                        // Please make sure formatting matches what the IDE actually spits
                        // out; it could matter.
                        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                        pw.println("<!DOCTYPE module PUBLIC \"-//NetBeans//DTD Module Status 1.0//EN\"");
                        pw.println("                        \"http://www.netbeans.org/dtds/module-status-1_0.dtd\">");
                        pw.println("<module name=\"" + codenamebase + "\">");
                        pw.println("    <param name=\"autoload\">" + isAutoload + "</param>");
                        pw.println("    <param name=\"eager\">" + isEager + "</param>");
                        if (!isAutoload && !isEager) {
                            pw.println("    <param name=\"enabled\">" + isEnabled + "</param>");
                        }
                        pw.println("    <param name=\"jar\">" + kid.replace(File.separatorChar, '/') + "</param>");
                        pw.println("    <param name=\"reloadable\">false</param>");
                        OUT: if (level != null) {
                            if (attr == null || attr.getValue("Bundle-SymbolicName") == null) {
                                if (!checkBundle) {
                                    break OUT;
                                }
                                throw new BuildException("startlevel specified, but " + module + " is not OSGi bundle!");
                            }
                            pw.println("    <param name=\"startlevel\">" + level + "</param>");
                        }
                        pw.println("</module>");
                        pw.flush();
                        pw.close();
                    }
                    if (v != null) {
                        v.addFileForRoot(xml);
                    }
                }
            }
            if (ut != null) {
                ut.write();
            }
        } catch (IOException ioe) {
            throw new BuildException("Caught while processing " + module + ": " + ioe, ioe, getLocation());
        }
    }
    
}
