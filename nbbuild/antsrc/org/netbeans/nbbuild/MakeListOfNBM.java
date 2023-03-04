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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.zip.CRC32;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

/**
 * Create an update tracking file automatically.
 * @author  Michal Zlamal
 */
public class MakeListOfNBM extends Task {
    File outputFile = null;
    String moduleName = null;
    boolean pok = true;
    FileSet fs = null;
    private ArrayList<String> locales;
    private ArrayList<String> brandings;

    public MakeListOfNBM() {
        // initialize locales and brandings lists, add empty value at beginning
        locales = new ArrayList<>();
        locales.add("");
        brandings = new ArrayList<>();
        brandings.add("");
    }

    /** Sets the directory used to create the NBM list file */
    public void setOutputfiledir(File s) {
        outputFile = s;
        log("Setting outputfile to " + s, Project.MSG_DEBUG);
    }

    public FileSet createFileSet() {
        return (fs = new FileSet());
    }

    /** Sets the module file */
    public void setModule(String s) {
        moduleName = s;
        log("Setting moduleName to " + s, Project.MSG_DEBUG);
    }

    public void setTargetName(String t) {
        pok = false;
        log("<"+this.getTaskName()+"> attribute targetname has been DEPRECATED");
    }

    /** Sets the list of locales in multilanguage build */
    public void setLocales (String s) {
        for (String st : s.split("[, ]+")) {
            locales.add(st);
        }
    }

    public List<String> getLocales () {
        return this.locales;
    }

    /** Sets the list of brandings in multilanguage build */
    public void setBrandings (String s) {
        if (s.startsWith("${")) return;
        StringTokenizer st = new StringTokenizer(s,", ");
        while (st.hasMoreTokens()) {
            brandings.add(st.nextToken());
        }
    }

    public List<String> getBrandings () {
        return this.brandings;
    }

    @Override
    public void execute () throws BuildException {
        if (!pok) throw new BuildException("Use the fileset to specify the content of the NBM");
        if ( outputFile == null ) throw new BuildException( "You have to specify output directoty" );
        if ( moduleName == null ) throw new BuildException( "You have to specify the main module's file" );
        if ( fs == null ) throw new BuildException( "You have to specify the fileset of files included in this module" );

        UpdateTracking track = new UpdateTracking( outputFile.getAbsolutePath() );
        Attributes attr;
        JarFile jar = null;
        File module = new File( outputFile, moduleName );
        try {
            jar = new JarFile(module);
            attr = jar.getManifest().getMainAttributes();
        } catch (IOException ex) {
            throw new BuildException("Can't get manifest attributes for module jar file "+module.getAbsolutePath(), ex, getLocation());
        } finally {
            try {
                if (jar != null) jar.close();
            } catch( IOException ex1 ) {
                String exmsg = ex1.getMessage();
                if (exmsg == null) exmsg = "Unknown error";
                log("Caught I/O Exception (msg:\""+exmsg+"\") when trying to close jar file "+module.getAbsolutePath(),Project.MSG_WARN);
                }
            }

        boolean[] osgi = new boolean[1];
        String codename = JarWithModuleAttributes.extractCodeName(attr, osgi);
        if (codename == null) {
            throw new BuildException("Manifest in jar file "+module.getAbsolutePath()+" does not contain OpenIDE-Module", getLocation());
        }
        String cnb = codename.replaceFirst("/\\d+$", "");

        log("Generating Auto Update information for " + cnb);

        String versionTag = osgi[0] ? "Bundle-Version" : "OpenIDE-Module-Specification-Version"; // NOI18N
        String versionSpecNum = attr.getValue(versionTag);
        if (versionSpecNum == null) {
            log("Manifest in jar file "+module.getAbsolutePath()+" does not contain tag " + versionTag);
            versionSpecNum = "0";
        }

        UpdateTracking.Version version = track.addNewModuleVersion( codename, versionSpecNum );

        fs.createInclude().setName("config" + File.separator + "Modules" + File.separator + track.getTrackingFileName()); //NOI18N
        
        updateFileSetForLorB (fs);
        // get directory scanner for "default" files
        DirectoryScanner ds = fs.getDirectoryScanner( this.getProject() );
        ds.scan();

        // check if we need also localized and branded files
        String lmnl = this.getProject().getProperty("locmakenbm.locales"); // NOI18N
        String lmnb = this.getProject().getProperty("locmakenbm.brands"); // NOI18N
        
        if ((!(lmnl == null)) && (!(lmnl.trim().equals("")))) { // NOI18N
            // property locmakenbm.locales is set, let's update the included fileset for locales
            // defined in that property

            java.util.StringTokenizer tokenizer = new StringTokenizer( lmnl, ", ") ; //NOI18N
            int cntTok = tokenizer.countTokens();
            String[] lmnLocales = new String[cntTok];
            for (int j=0; j < cntTok; j++) {
                String s = tokenizer.nextToken();
                lmnLocales[j] = s;
                log("  lmnLocales[j] == "+lmnLocales[j], Project.MSG_DEBUG); // NOI18N
            }

            // handle brandings
            String[] lmnBrands = null;
            if ((!(lmnb == null)) && (!(lmnb.trim().equals("")))) { // NOI18N
                tokenizer = new StringTokenizer( lmnb, ", ") ; //NOI18N
                cntTok = tokenizer.countTokens();
                lmnBrands = new String[cntTok];
                for (int j=0; j < cntTok; j++) {
                    String s = tokenizer.nextToken();
                    lmnBrands[j] = s;
                    log("  lmnBrands[j] == "+lmnBrands[j], Project.MSG_DEBUG); // NOI18N
                }
            }

            // update fileset for localized/branded files
        
            String[] englishFiles = ds.getIncludedFiles();
            int sepPos, extPos;
            String dirName, fname, filename, fext, newinc, ei_codename;
            String moduleJar = null;
            boolean skipLocaleDir = false;
            for (int k=0; k < englishFiles.length; k++) {
                // skip records for already localized/branded files
                if ((englishFiles[k].lastIndexOf("/locale/") >= 0) || // NOI18N
                     (englishFiles[k].lastIndexOf(File.separator+"locale"+File.separator) >= 0)) {  // NOI18N
                    skipLocaleDir=true;
                } else {
                    skipLocaleDir=false;
                }
                log("Examining file " + englishFiles[k], Project.MSG_DEBUG);
                sepPos = englishFiles[k].lastIndexOf(File.separator);
                if (sepPos < 0) {
                    dirName = ""; //NOI18N
                    filename = englishFiles[k];
                } else {
                    dirName = englishFiles[k].substring(0,sepPos);
                    filename = englishFiles[k].substring(sepPos+File.separator.length());
                }
                extPos = filename.lastIndexOf('.'); //NOI18N
                if (extPos < 0) {
                    fname = filename;
                    fext = ""; //NOI18N
                } else {
                    fname = filename.substring(0, extPos);
                    fext = filename.substring(extPos);
                }
                for (int j=0; j < lmnLocales.length; j++) {
                    // localized files
                    if (skipLocaleDir) {
                    	newinc = dirName + File.separator + fname + "_"+lmnLocales[j]+"*" + fext; //NOI18N
                    } else {
                        newinc = dirName + File.separator + "locale" + File.separator + fname + "_"+lmnLocales[j]+"*" + fext; //NOI18N
                    }
                    log("  adding include mask \""+newinc+"\"", Project.MSG_DEBUG);
                    fs.setIncludes( newinc );
                    // localized & branded files
                    if (!(lmnBrands == null)) {
                    	for (int i=0; i < lmnBrands.length; i++) {
                            if (skipLocaleDir) {
                    	        newinc = dirName + File.separator + fname + "_"+lmnBrands[i]+"_"+lmnLocales[j]+"*" + fext; //NOI18N
                            } else {
                    	        newinc = dirName + File.separator + "locale" + File.separator + fname + "_"+lmnBrands[i]+"_"+lmnLocales[j]+"*" + fext; //NOI18N
                            }
                            log("  adding include mask \""+newinc+"\"", Project.MSG_DEBUG);
                            fs.setIncludes( newinc );
                        }
                    }
                }
            }
            // update directory scanner
            ds = fs.getDirectoryScanner(this.getProject());
            ds.scan();
        }

        String include[] = ds.getIncludedFiles();
        log("Including files " + Arrays.toString(include), Project.MSG_VERBOSE);
        for( int j=0; j < include.length; j++ ){
            String path = include[j].replace(File.separatorChar, '/');
            if (osgi[0] && !path.equals(moduleName) &&
                    !path.equals("config/Modules/" + cnb.replace('.', '-') + ".xml")) {
                throw new BuildException("Cannot include other files with an OSGi bundle: " + path, getLocation());
            }
            try {
                File inFile = new File( ds.getBasedir(), include[j] );
                CRC32 crc = UpdateTracking.crcForFile(inFile);
                String abs = inFile.getAbsolutePath();
                String prefix = ds.getBasedir().getAbsolutePath() + File.separatorChar;
                if (! abs.startsWith(prefix)) throw new IllegalStateException(abs);
                version.addFileWithCrc(abs.substring(prefix.length()).replace(File.separatorChar, '/'), Long.toString( crc.getValue() ) );
            } catch (IOException ex) {
                log( ex.toString() );
            }
        }
        track.write();
    }

    private void updateFileSetForLorB(FileSet fs) {
        if ((locales.size() == 1) && (brandings.size() == 1)) return;
        // update the fileset only if we have got at least one locale or one branding
        DirectoryScanner ds = fs.getDirectoryScanner();
        String[] included = ds.getIncludedFiles();
        ArrayList<String> newIncludes = new ArrayList<>();
        String dirName; String filename; String fname; String fext; String newinc;
        
        for (String include : included) {
            include = include.replace(File.separatorChar, '/');
            int sepPos = include.lastIndexOf('/');
            if (sepPos < 0) {
                dirName = ""; //NOI18N
                filename = include;
            } else {
                dirName = include.substring(0,sepPos);
                filename = include.substring(sepPos+1);
            }
            int extPos = filename.lastIndexOf('.'); //NOI18N
            if (extPos < 0) {
                fname = filename;
                fext = ""; //NOI18N
            } else {
                fname = filename.substring(0, extPos);
                fext = filename.substring(extPos);
            }
            for (String branding : brandings) {
                for (String loc : locales) {
                    newinc = dirName + "/locale/" + fname;
                    if (branding.length()>0) newinc += "_" + branding;
                    if (loc.length()>0) newinc += "_" + loc;
                    newinc+=fext;
                    if (newinc.startsWith("/")) newinc = newinc.substring(1); //avoid root referring masks on unix boxes
                    newIncludes.add(newinc);
                    log("Added include mask: "+newinc,Project.MSG_VERBOSE);
                    if (dirName.length() == 0) {
                        // if file is located in root of the cluster, add also
                        // a mask without "locale/" subdirectory
                        newinc = fname;
                        if (branding.length()>0) newinc += "_" + branding;
                        if (loc.length()>0) newinc += "_" + loc;
                        newinc+=fext;
                        newIncludes.add(newinc);
                        log("Added cluster-root include mask: "+newinc,Project.MSG_VERBOSE);
                    }
                }
            }
        }
        for (String inc : newIncludes) {
            fs.setIncludes(inc);
        }
    }
}
