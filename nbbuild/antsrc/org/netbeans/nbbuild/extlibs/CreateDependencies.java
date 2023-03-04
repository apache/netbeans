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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.Resource;

/**
 * Creates a list of external binaries and their licenses.
 */
public class CreateDependencies extends Task {

    private String refid;

    public void setRefid(String refid) {
        this.refid = refid;
    }
    
    private File dependencies;
    public void setDependencies(File dependencies) {
        this.dependencies = dependencies;
    }
    
    private boolean sourceDependencies;
    
    public void setSourceDependencies(boolean sourceDependencies) {
        this.sourceDependencies = sourceDependencies;
    }

    public @Override void execute() throws BuildException {
        try {
            Map<String,Map<String,String>> binary2License = findBinary2LicenseHeaderMapping();
            Properties licenseNames = new Properties();
            try (InputStream in = new FileInputStream(new File(getProject().getProperty("nb_all"), "nbbuild/licenses/names.properties"))) {
                licenseNames.load(in);
            }
            try (OutputStream os = new FileOutputStream(dependencies)) {
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
                pw.println("This project's dependencies");
                pw.println();

                Map<String, Set<String>> origin2TextsRuntime = new TreeMap<>();
                Map<String, Set<String>> origin2TextsCompileTime = new TreeMap<>();

                for (Map.Entry<String,Map<String,String>> entry : binary2License.entrySet()) {
                    Map<String,String> headers = entry.getValue();
                    String origin = getMaybeMissing(headers, "Origin");
                    Map<String, Set<String>> origin2Texts = "compile-time".equals(headers.get("Type")) ? origin2TextsCompileTime : origin2TextsRuntime;
                    StringBuilder text = new StringBuilder();
                    text.append(getMaybeMissing(headers, "Name"));
                    text.append(":");
                    if (headers.containsKey("Description")) {
                        text.append(" ");
                        text.append(headers.get("Description"));
                    }
                    if (headers.containsKey("URL")) {
                        text.append(" (");
                        text.append(headers.get("URL"));
                        text.append(")");
                    }
                    String licCode = getMaybeMissing(headers, "License");
                    String licName = licenseNames.getProperty(licCode);
                    String licDesc = licName != null ? licName : licCode;
                    text.append("\nLicense: " + licDesc);
                    if (headers.containsKey("Comment")) {
                        text.append("\nComment: " + headers.get("Comment"));
                    }
                    origin2Texts.computeIfAbsent(origin, o -> new TreeSet<>()).add(text.toString());
                }

                Map<String, Map<String, Set<String>>> dependenciesDesc = new LinkedHashMap<>();
                dependenciesDesc.put("Runtime dependencies:", origin2TextsRuntime);
                if (sourceDependencies) {
                    dependenciesDesc.put("Compile time dependencies:", origin2TextsCompileTime);
                }

                for (Entry<String, Map<String, Set<String>>> entry : dependenciesDesc.entrySet()) {
                    if (entry.getValue().isEmpty())
                        continue;
                    pw.println();
                    pw.println(entry.getKey());
                    char[] eq = new char[entry.getKey().length()];
                    Arrays.fill(eq, '=');
                    pw.println(new String(eq));
                    pw.println();

                    for (Entry<String, Set<String>> org2Lines : entry.getValue().entrySet()) {
                        pw.println("From: " + org2Lines.getKey());
                        for (String line : org2Lines.getValue()) {
                            pw.print("  - ");
                            pw.println(line.replace("\n", "\n    "));
                        }
                        pw.println();
                    }
                }
                pw.flush();
                pw.close();
            }
            log(dependencies + ": written");
        } catch (IOException x) {
            throw new BuildException(x, getLocation());
        }
    }
    private String getMaybeMissing(Map<String,String> headers, String headerName) {
        if (headers.containsKey(headerName)) {
            return headers.get(headerName);
        } else {
            return "<unknown>";
        }
    }

    private Map<String,Map<String,String>> findBinary2LicenseHeaderMapping() throws IOException {
        DirSet sources = getProject().getReference(refid);
        Map<String,Map<String,String>> file2LicenseHeaders = new HashMap<>();
        for (Resource r : sources) {
            processModule(r.getName(), file2LicenseHeaders);
        }
        return file2LicenseHeaders;
    }

    private void processModule(String module, Map<String, Map<String, String>> file2LicenseHeaders) throws IOException {
        File d = new File(new File(getProject().getProperty("nb_all"), module), "external");
        Set<String> hgFiles = VerifyLibsAndLicenses.findHgControlledFiles(d);
        Map<String,Map<String,String>> binary2License = CreateLicenseSummary.findBinary2LicenseHeaderMapping(hgFiles, d);
        for (String n : hgFiles) {
            if (!n.endsWith(".jar") && !n.endsWith(".zip") && !n.endsWith(".xml") &&
                    !n.endsWith(".js") && !n.endsWith(".dylib")) {
                continue;
            }
            Map<String,String> headers = binary2License.get(n);
            if (headers == null) {
                continue;
            }
            file2LicenseHeaders.put(n, headers); //TODO: check unique!
        }
    }

}
