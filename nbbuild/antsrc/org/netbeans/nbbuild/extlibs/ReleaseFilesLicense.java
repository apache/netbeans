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

package org.netbeans.nbbuild.extlibs;

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
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Creates a license file for use in NBM generation.
 * The standard license file is written first.
 * Any named extra licenses, from the licenses dir, are appended.
 * Any project properties which are found beginning with "release.external." are also used;
 * the remainder of the property name should be a binary name in the project's external dir,
 * and a matching license notice is looked for in that dir.
 */
public class ReleaseFilesLicense extends Task {

    private File license;
    public void setLicense(File license) {
        this.license = license;
    }

    private File standardLicense;
    public void setStandardLicense(File standardLicense) {
        this.standardLicense = standardLicense;
    }

    private String extraLicenseFiles;
    public void setExtraLicenseFiles(String extraLicenseFiles) {
        this.extraLicenseFiles = extraLicenseFiles;
    }

    public @Override void execute() throws BuildException {
        try {
            try (OutputStream os = new FileOutputStream(license)) {
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
                pw.println("License for NetBeans module:");
                pw.println();
                append(pw, standardLicense);
                for (String name : extraLicenseFiles.split("[, ]+")) {
                    File f = getProject().resolveFile(name);
                    if (!f.isFile()) {
                        log("No such license: " + f, Project.MSG_WARN);
                        continue;
                    }
                    pw.println();
                    pw.println();
                    pw.println("==================================================");
                    pw.println("Additional license (" + f.getName() + "):");
                    pw.println();
                    try (InputStream is = new FileInputStream(f)) {
                        BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                        String line;
                        while ((line = r.readLine()) != null && line.length() > 0) {}
                        while ((line = r.readLine()) != null) {
                            pw.println(line);
                        }
                    }
                }
                Map<File,Set<String>> inferredLicenses = new TreeMap<>();
                RELEASE_FILE: for (Map.Entry<String,Object> entry : ((Map<String,Object>) getProject().getProperties()).entrySet()) {
                    String k = entry.getKey();
                    // XXX this does not work for release.../external/*; need to maybe match ^release\.(.+/)?external/
                    if (k.startsWith("release.external/")) {
                        String binary = k.substring("release.external/".length()).replaceFirst("!/.+$", "");
                        File d = getProject().resolveFile("external");
                        for (File possibleLicense : d.listFiles()) {
                            if (!possibleLicense.getName().endsWith("-license.txt")) {
                                continue;
                            }
                            try (InputStream is = new FileInputStream(possibleLicense)) {
                                BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                                String files = possibleLicense.getName().replaceFirst("-license\\.txt$", ".jar") +
                                        " " + possibleLicense.getName().replaceFirst("-license\\.txt$", ".zip");
                                String line;
                                while ((line = r.readLine()) != null && line.length() > 0) {
                                    Matcher m = Pattern.compile("Files: (.+)").matcher(line);
                                    if (m.matches()) {
                                        files = m.group(1);
                                        break;
                                    }
                                }
                                if (Arrays.asList(files.split("[, ]+")).contains(binary)) {
                                    Set<String> binaries = inferredLicenses.get(possibleLicense);
                                    if (binaries == null) {
                                        binaries = new TreeSet<>();
                                        inferredLicenses.put(possibleLicense, binaries);
                                    }
                                    binaries.add((String) entry.getValue());
                                    continue RELEASE_FILE;
                                }
                            }
                        }
                    }
                }
                for (Map.Entry<File,Set<String>> entry : inferredLicenses.entrySet()) {
                    pw.println();
                    pw.println();
                    pw.println("==================================================");
                    pw.println("Additional license (" + entry.getKey().getName() + ") associated with the following files:");
                    for (String binary : entry.getValue()) {
                        pw.println(binary);
                    }
                    pw.println();
                    try (InputStream is = new FileInputStream(entry.getKey())) {
                        BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                        String line;
                        while ((line = r.readLine()) != null && line.length() > 0) {}
                        while ((line = r.readLine()) != null) {
                            pw.println(line);
                        }
                    }
                }
                pw.flush();
            }
        } catch (IOException x) {
            throw new BuildException(x, getLocation());
        }
    }

    private void append(PrintWriter pw, File f) throws IOException {
        try (InputStream is = new FileInputStream(f)) {
            BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            while ((line = r.readLine()) != null) {
                pw.println(line);
            }
        }
    }

}
