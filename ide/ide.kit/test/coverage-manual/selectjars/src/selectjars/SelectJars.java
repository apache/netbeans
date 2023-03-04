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
 * SelectJars.java
 *
 * Created on May 23, 2005, 11:26 AM
 */

package selectjars;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

/**
 * @author mk97936
 */
public class SelectJars extends Task {
    
    /* Set for storing final list of included jars */
    private Set includedJars = new HashSet();
    
    /* Name of the property for storing ant reference */
    private String pathRefProp;
    
    /* Array of included packages */
    private String[] inclPackages;
    
    /* Original string with selected packages */
    private String inclPackagesOrig;
    
    /* List of all jar files to choose from */
    private List filesets = new LinkedList();
    
    /* PrintStream for printing out logging info */
    private PrintStream logStream = null;
    
    public void setPathrefprop(String s) {
        pathRefProp = s;
    }
    
    public void setInclpackages(String s) {
        inclPackagesOrig = new String(s);
        Vector vecIncluded = new Vector();
        StringTokenizer st = new StringTokenizer(s, ",");
        while (st.hasMoreTokens()) {
            String incPkg = st.nextToken();
            // cut off last two characters if ends with '.*' or one if with '.'
            if (incPkg.endsWith(".*")) {
                incPkg = incPkg.substring(0, incPkg.length() - 2);
            } else if (incPkg.endsWith(".")) {
                incPkg = incPkg.substring(0, incPkg.length() - 1);
            }
            log("Included package: " + incPkg, Project.MSG_VERBOSE);
            vecIncluded.add(incPkg);
        }
        inclPackages = (String[]) vecIncluded.toArray(new String[] {});
    }
    
    public void addFileset(FileSet fs) {
        filesets.add(fs);
    }
    
    public void setLogfile(File lgfl) {
        try {
            logStream = new PrintStream(new FileOutputStream(lgfl));
        } catch (IOException ioe) {
            throw new BuildException(ioe.getLocalizedMessage());
        }
    }
    
    public void execute() throws BuildException {
        
        Iterator it = filesets.iterator();
        while (it.hasNext()) {
            
            FileSet fs = (FileSet) it.next();
            DirectoryScanner ds = fs.getDirectoryScanner(project);
            File basedir = ds.getBasedir();
            String[] files = ds.getIncludedFiles();
            
            for (int i = 0; i < files.length; i++) {
                
                // scanning each jar file for required packages
                String jarFileName = basedir + File.separator + files[i];
                JarFile jarFile = null;
                try {
                    jarFile = new JarFile(jarFileName);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                
                log(jarFileName, Project.MSG_DEBUG);
                Enumeration entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry zipEntry = (ZipEntry) entries.nextElement();
                    if (zipEntry.isDirectory()) {
                        log(zipEntry.toString(), Project.MSG_DEBUG);
                        String pkgFromJar = zipEntry.toString().replace('/', '.');
                        // removing trailing '.' to enable compare
                        if (pkgFromJar.endsWith(".")) {
                            pkgFromJar = pkgFromJar.substring(0, pkgFromJar.length() - 1);
                        }
                        for (int j = 0; j < inclPackages.length; j++) {
                            if (inclPackages[j].compareTo(pkgFromJar) == 0) {
                                log("Selected jar: " + jarFileName , Project.MSG_VERBOSE);
                                log("    " + inclPackages[j] + " == " + pkgFromJar, Project.MSG_VERBOSE);
                                includedJars.add(jarFileName);
                            }
                        }
                    }
                }
                
            }
            
        }
        
        List listIncluded = new ArrayList(includedJars);
        Collections.sort(listIncluded);
        Iterator iter = listIncluded.iterator();
        
        log("Found " + includedJars.size() + " jar files.", Project.MSG_VERBOSE);
                
        if (logStream != null) {
            logStream.println("Instrumented Packages:");
            logStream.println(inclPackagesOrig);
            logStream.println("--------------------------------------------------------------------------------");
            logStream.println("Included JAR files:");
        }
        
        Path path = new Path(getProject());
        getProject().addReference(pathRefProp, path);
        while (iter.hasNext()) {
            String fileName = (String) iter.next();
            path.createPathElement().setLocation(new File(fileName));
            if (logStream != null) {
                logStream.println(fileName);
            }
        }
        if (logStream != null) {
            logStream.println("Found " + includedJars.size() + " jar files.");
            logStream.close();
        }
    }
    
}
