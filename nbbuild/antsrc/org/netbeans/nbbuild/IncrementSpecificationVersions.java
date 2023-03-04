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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Increments specification versions of all specified modules,
 * in the trunk or in a branch, in a regulated manner.
 * @author Jesse Glick
 */
public final class IncrementSpecificationVersions extends Task {
    
    private File nbroot;
    private List<String> modules;
    private int stickyLevel = -1;
    
    public IncrementSpecificationVersions() {}
    
    public void setNbroot(File f) {
        nbroot = f;
    }
    
    public void setModules(String m) {
        modules = new ArrayList<>();
        for (Object o : Collections.list(new StringTokenizer(m, ", "))) {
            modules.add((String) o);
        }
    }
    
    public void setBranch(boolean b) {
        setStickyLevel(b ? 2 : 1);
    }

    /** Number of digits from the begining that are supposed to
     * stay the same
     */
    public void setStickyLevel(int stickyLevel) {
        if (this.stickyLevel != -1) {
            throw new BuildException("Only one stickyLevel or branch attribute can be used!");
        }

        this.stickyLevel = stickyLevel;
    }

    public void execute() throws BuildException {
        if (nbroot == null || modules == null) {
            throw new BuildException("Missing params 'nbroot' or 'modules'", getLocation());
        }
        MODULE: for (String module : modules) {
            File dir = new File(nbroot, module.replace('/', File.separatorChar));
            if (!dir.isDirectory()) {
                log("No such directory " + dir + "; skipping", Project.MSG_WARN);
                continue;
            }
            try {
                File pp = new File(dir, "nbproject" + File.separatorChar + "project.properties");
                if (pp.isFile()) {
                    String[] lines = gulp(pp, "ISO-8859-1");
                    for (int i = 0; i < lines.length; i++) {
                        Matcher m1 = Pattern.compile("(spec\\.version\\.base=)(.+)").matcher(lines[i]);
                        if (m1.matches()) {
                            String old = m1.group(2);
                            String nue = increment(old, stickyLevel, false);
                            if (nue != null) {
                                lines[i] = m1.group(1) + nue;
                                spit(pp, "ISO-8859-1", lines);
                                log("Incrementing " + old + " -> " + nue + " in " + pp);
                            } else {
                                log(pp + ":" + (i + 1) + ": Unsupported old version number " + old + " (must be x.y.0 in trunk or x.y.z in branch); skipping", Project.MSG_WARN);
                            }
                            continue MODULE;
                        }
                    }
                } else {
                    if (!new File(dir, "nbproject" + File.separatorChar + "project.xml").isFile()) {
                        log("No such file " + pp + "; unprojectized module?", Project.MSG_WARN);
                    }
                }
                File mf = new File(dir, "manifest.mf");
                if (mf.isFile()) {
                    String[] lines = gulp(mf, "UTF-8");
                    for (int i = 0; i < lines.length; i++) {
                        Matcher m1 = Pattern.compile("(OpenIDE-Module-Specification-Version: )(.+)").matcher(lines[i]);
                        if (m1.matches()) {
                            String old = m1.group(2);
                                String nue = increment(old, stickyLevel, true);
                            if (nue != null) {
                                lines[i] = m1.group(1) + nue;
                                spit(mf, "UTF-8", lines);
                                log("Incrementing " + old + " -> " + nue + " in " + mf);
                            } else {
                                log(mf + ":" + (i + 1) + ": Unsupported old version number " + old + " (must be x.y in trunk or x.y.z in branch); skipping", Project.MSG_WARN);
                            }
                            continue MODULE;
                        }
                    }
                } else {
                    log("No such file " + mf + "; not a real module?", Project.MSG_WARN);
                }
                log("Could not find any specification version in " + dir + "; skipping", Project.MSG_WARN);
            } catch (IOException e) {
                throw new BuildException("While processing " + dir + ": " + e, e, getLocation());
            }
        }
    }

    /** Does the increment of the specification version to new version.
     * @return the new version or null if the increment fails
     */
    static String increment(String old, int stickyLevel, boolean manifest) throws NumberFormatException {
        String nue = null;

        switch (stickyLevel) {
            case 1: // trunk
                if (manifest) {
                    Matcher mC = Pattern.compile("([0-9]+\\.)([0-9]+)").matcher(old);
                    Matcher mW1 = Pattern.compile("([0-9]+)").matcher(old);
                    Matcher mW2 = Pattern.compile("([0-9]+\\.)([0-9]+)\\.([0-9\\.]+)").matcher(old);
                    if (mC.matches()) {        // Correct e.g 1.0 -> 1.1
                        nue = mC.group(1) + (Integer.parseInt(mC.group(2)) + 1);
                    }
                    else if ( mW1.matches() ) { // Wrong e.g 1 -> 1.1
                        nue = mW1.group(1) + ".1";
                    }
                    else if ( mW2.matches() ) { // Wrong e.g 1.2.4.5.6.7 => 1.3
                        nue = mW2.group(1) + (Integer.parseInt(mW2.group(2)) + 1);                        
                    }
                } else {
                    Matcher mC = Pattern.compile("([0-9]+\\.)([0-9]+)(\\.0)").matcher(old);
                    Matcher mW1 = Pattern.compile("([0-9]+)").matcher(old);
                    Matcher mW2 = Pattern.compile("([0-9]+\\.)([0-9]+)").matcher(old);
                    Matcher mW3 = Pattern.compile("([0-9]+\\.)([0-9]+)\\.([0-9\\.]+)").matcher(old);
                                        
                    if (mC.matches()) {  // Correct 1.1.0 -> 1.2.0
                        nue = mC.group(1) + (Integer.parseInt(mC.group(2)) + 1) + mC.group(3);
                    }
                    else if (mW1.matches() ) { // Wrong 1 -> 2.1.0
                        nue = (Integer.parseInt(mW1.group(1)) + 1) + ".1.0";
                    }
                    else if (mW2.matches() ) { // Wrong 1.1 -> 1.3.0
                        // If we started with e.g. 1.1 prior to a release,
                        // actual versions would be e.g. 1.1.49.8.
                        // To make trunk > branch > old, need 1.3.0.49.8 > 1.2.1.49.8 > 1.1.49.8.
                        nue = mW2.group(1) + (Integer.parseInt(mW2.group(2)) + 2) + ".0";
                    }                    
                    else if (mW3.matches() ) { // Wrong 1.2.3.4.5.6 -> 1.3.0
                        nue = mW3.group(1) + (Integer.parseInt(mW3.group(2)) + 1) + ".0";
                    }
                }
                break;
            case 2: // branch
                if (manifest) {
                    Matcher mC1 = Pattern.compile("([0-9]+\\.[0-9]+\\.)([0-9]+)").matcher(old);
                    Matcher mC2 = Pattern.compile("([0-9]+\\.[0-9]+)").matcher(old);
                    Matcher mW1 = Pattern.compile("([0-9]+)").matcher(old);
                    Matcher mW2 = Pattern.compile("([0-9]+\\.[0-9]+\\.)([0-9]+)\\.([0-9\\.]+)").matcher(old);
                    if (mC1.matches()) { // Correct 1.2.3 -> 1.2.4
                        nue = mC1.group(1) + (Integer.parseInt(mC1.group(2)) + 1);
                    } 
                    else if (mC2.matches()) { // Correct 1.2 -> 1.2.1
                        nue = mC2.group(1) + ".1";
                    }
                    else if ( mW1.matches()) { // Wrong 1 -> 1.0.1
                        nue = mW1.group(1) + ".0.1";
                    }
                    else if ( mW2.matches()) { // Wrong 1.2.3.4.5.6 -> 1.2.4
                        nue = mW2.group(1) + (Integer.parseInt(mW2.group(2)) + 1);
                    }
                    
                } else {
                    Matcher mC = Pattern.compile("([0-9]+\\.[0-9]+\\.)([0-9]+)").matcher(old);
                    Matcher mW1 = Pattern.compile("([0-9]+)").matcher(old);                    
                    Matcher mW2 = Pattern.compile("([0-9]+\\.)([0-9]+)").matcher(old);
                    Matcher mW3 = Pattern.compile("([0-9]+\\.[0-9]+\\.)([0-9]+)\\.([0-9\\.]+)").matcher(old);                    
                    if (mC.matches()) { // Correct 1.2.3 -> 1.2.4
                        nue = mC.group(1) + (Integer.parseInt(mC.group(2)) + 1);
                    }
                    else if ( mW1.matches()) { // Wrong 1 -> 2.0.1
                        nue = (Integer.parseInt(mW1.group(1)) + 1) + ".0.1";
                    }
                    else if ( mW2.matches()) { // Wrong 1.2 -> 1.3.1
                        nue = mW2.group(1) + (Integer.parseInt(mW2.group(2)) + 1) + ".1";
                    }
                    else if ( mW3.matches()) { // Wrong 1.2.3.4.5.6 -> 1.2.4
                        nue = mW3.group(1) + (Integer.parseInt(mW3.group(2)) + 1);
                    }                    
                }
                break;
            default:
                if (stickyLevel < 1) {
                    throw new BuildException("Invalid sticky level: " + stickyLevel);
                }
                int[] segments = new int[stickyLevel + 1];
                StringTokenizer tok = new StringTokenizer(old, ".");
                for (int i = 0; i < segments.length && tok.hasMoreElements(); i++) {
                    segments[i] = Integer.parseInt(tok.nextToken());
                }
                segments[stickyLevel]++;
                nue = "";
                String pref = "";
                for (int i = 0; i < segments.length; i++) {
                    nue += pref;
                    nue += segments[i];
                    pref = ".";
                }
                break;
        }

        return nue;
    }

    private static String[] gulp(File file, String enc) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            BufferedReader r = new BufferedReader(new InputStreamReader(is, enc));
            List<String> l = new ArrayList<>();
            String line;
            while ((line = r.readLine()) != null) {
                l.add(line);
            }
            return l.toArray(new String[l.size()]);
        }
    }
    
    private static void spit(File file, String enc, String[] lines) throws IOException {
        try (OutputStream os = new FileOutputStream(file)) {
            PrintWriter w = new PrintWriter(new OutputStreamWriter(os, enc));
            for (String line : lines) {
                w.println(line);
            }
            w.flush();
        }
    }

}
