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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.ZipFileSet;
import org.apache.tools.ant.types.selectors.SelectorUtils;

/**
 * This task was created to create L10N kits. The xml to call this task might
 * look like:
 * <l10nTask nbmsdir="nbms" tmpdir="tmp" patternsFile="l10n.patterns"
 * kitFile="build/l10n.zip"/>
 *
 *
 * Resulting kitFile will contain the files according the patterns from
 * patternsFile
 *
 * @author Michal Zlamal
 */
public class L10nTask extends Task {

    File nbmsDir = null;
    File tmpDir = null;
    File patternsFile = null;
    File kitFile = null;
    String locales = "";
    private static String LOCALES_TOKEN = "${locales}";

    @Override
    public void execute() throws BuildException {
        LineNumberReader lnr = null;
        try {
            if (nbmsDir == null) {
                throw new BuildException("Required variable not set.  Set 'nbmsdir' in the calling build script file");
            }
            if (!nbmsDir.exists() || !nbmsDir.isDirectory()) {
                throw new BuildException("'nbmsdir' has to exist and be directory where are all NBMs stored");
            }
            if (patternsFile == null) {
                throw new BuildException("Required variable not set.  Set 'patternsFile' in the calling build script file");
            }
            if (!patternsFile.exists() || !patternsFile.isFile()) {
                throw new BuildException("'patternsFile' has to exist and be file with patterns what should be included in the kit");
            }
            if (kitFile == null) {
                throw new BuildException("Required variable not set.  Set 'kitFile' in the calling build script file");
            }

            lnr = new LineNumberReader(new FileReader(patternsFile));
            String line;
            Map<String, Set<String>> includes = new HashMap<>();
            Map<String, Set<String>> excludes = new HashMap<>();
            Set<String> excludeFiles = new HashSet<>();

            //Read all the patterns from patternsFile
            while ((line = lnr.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                if (line.startsWith("#")) {
                    continue;
                }
                if (!line.startsWith("exclude ")) {  //Include pattern

                    String[] p = line.split(":");
                    if (p.length != 2) {
                        if (line.endsWith(":")) {
                            includes.put(line.substring(0, line.length() - 1), null);
                            continue;
                        } else {
                            throw new BuildException("Wrong pattern '" + line + "' found in pattern file: " + patternsFile.getAbsolutePath());
                        }
                    }
                    Set<String> files = includes.get(p[0]);
                    if (files == null) {
                        files = new HashSet<>();
                        includes.put(p[0], files);
                    }
                    files.add(p[1]);
                } else {        //Exlude pattern

                    List<String> lines = new ArrayList<> ();
                    String lineRaw = line.substring("exclude ".length());
                    if (lineRaw.contains(LOCALES_TOKEN)) {
                        for (String locale : getLocales(locales)) {
                            if (! locale.isEmpty()) {
                                lines.add(lineRaw.replace(LOCALES_TOKEN, locale));
                            }
                        }
                        if (lines.isEmpty()) {
                            lines.add(lineRaw.replace(LOCALES_TOKEN, "*")); // NOI18N
                        }
                    } else {
                        lines.add(lineRaw); // NOI18N
                    }
                    for (String oneLine : lines) {
                        String[] p = oneLine.split(":");
                        if (p.length != 2) {
                            if (oneLine.endsWith(":")) {
                                excludes.put(oneLine.substring(0, oneLine.length() - 1), null);
                                excludeFiles.add(oneLine.substring(0, oneLine.length() - 1));
                                continue;
                            } else {
                                throw new BuildException("Wrong pattern '" + oneLine + "' found in pattern file: " + patternsFile.getAbsolutePath());
                            }
                        }
                        Set<String> files = excludes.get(p[0]);
                        if (files == null) {
                            files = new HashSet<>();
                            excludes.put(p[0], files);
                        }
                        files.add(p[1]);
                    }
                }
            }
            lnr.close();

            //Unzip all the NBMs
            DirectoryScanner ds = new DirectoryScanner();
            ds.setBasedir(nbmsDir);
            ds.setIncludes(new String[]{"**/*.nbm"});
            ds.scan();
            String[] nbms = ds.getIncludedFiles();
            Expand unzip = (Expand) getProject().createTask("unzip");
            for (String nbm : nbms) {
                File nbmFile = new File(nbmsDir, nbm);
                File nbmDir = new File(tmpDir, nbm);
                nbmDir.mkdirs();
                unzip.setSrc(nbmFile);
                unzip.setDest(nbmDir);
                unzip.execute();

                DirectoryScanner packGzDs = new DirectoryScanner();
                final String suffix = ".jar.pack.gz";
                packGzDs.setBasedir(nbmDir);
                packGzDs.setIncludes(new String[]{"**/*" + suffix});
                packGzDs.scan();
                for (String packedJar : packGzDs.getIncludedFiles()) {
                    File packedJarFile = new File(nbmDir, packedJar);
                    File unpackedJarFile = new File(nbmDir, packedJar.substring(0, packedJar.length() - suffix.length()) + ".jar");
                    log("Unpacking " + packedJar + " to " + unpackedJarFile, Project.MSG_VERBOSE);
                    AutoUpdate.unpack200(packedJarFile, unpackedJarFile);
                    packedJarFile.delete();
                }
            }
            ds.setBasedir(tmpDir);
            String[] includesKeys = includes.keySet().toArray(new String[]{""});
            String[] excludesKeys = excludes.keySet().toArray(new String[]{""});
            if (includesKeys[0] != null) {
                ds.setIncludes(includesKeys);
            }
            if (excludeFiles.size() > 0) {
                ds.setExcludes(excludeFiles.toArray(new String[]{""}));
            }

            //Go though all the found files maching the first part of the pattern
            ds.scan();

            if (kitFile.exists()) {
                kitFile.delete();
            }

            Zip zip = (Zip) getProject().createTask("zip");
            zip.setDestFile(kitFile);
            for (String filePath : ds.getIncludedFiles()) {
                String file = filePath.replace("\\", "/");
                ZipFileSet zipFileSet = new ZipFileSet();
                boolean matching = false;
                for (String include : includesKeys) {
                    if (SelectorUtils.matchPath(include, file)) {
                        Set<String> incPattern = includes.get(include);
                        if (incPattern != null) {
                            matching = true;
                            zipFileSet.appendIncludes(incPattern.toArray(new String[]{""}));
                        } else {
                            FileSet fileSet = new FileSet();
                            fileSet.setDir(tmpDir);
                            fileSet.setIncludes(file);
                            zip.addFileset(fileSet);
                        }
                    }
                }
                if (matching) {
                    for (String exclude : excludesKeys) {
                        if (SelectorUtils.matchPath(exclude, file)) {
                            Set<String> excPattern = excludes.get(exclude);
                            if (excPattern != null) {
                                zipFileSet.appendExcludes(excPattern.toArray(new String[]{""}));
                            }
                        }
                    }

                    File oneFile = new File(tmpDir, file);
                    zipFileSet.setSrc(oneFile);
                    file = file.replaceAll("org-netbeans-modules-", "");
                    file = file.replaceAll("/netbeans/modules/", "/");
                    file = file.replaceAll("\\.nbm/", "/");
                    file = file.replaceAll("\\.jar", "");
                    zipFileSet.setPrefix(file);
                    zip.addZipfileset(zipFileSet);
                }
            }
            zip.execute();
            Delete delete = (Delete) getProject().createTask("delete");
            delete.setDir(tmpDir);
            delete.execute();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(L10nTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(L10nTask.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (lnr != null) {
                    lnr.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(L10nTask.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void setNbmsdir(File nbmsDir) {
        this.nbmsDir = nbmsDir;
    }

    public void setTmpdir(File tmpDir) {
        this.tmpDir = tmpDir;
    }

    public void setPatternsFile(File patternsFile) {
        this.patternsFile = patternsFile;
    }

    public void setKitFile(File kitFile) {
        this.kitFile = kitFile;
    }

    public void setLocales(String locales) {
        this.locales = locales;
    }
    
    private static String[] getLocales(String locales) {
        StringTokenizer en = new StringTokenizer(locales, ","); // NOI18N
        String[] res = new String[en.countTokens()];
        int i = 0;
        while (en.hasMoreTokens()) {
            res[i++] = en.nextToken();
        }
        return res;
    }
}
