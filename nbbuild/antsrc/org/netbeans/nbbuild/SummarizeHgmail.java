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
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

/**
 * Creates summary of .hgmail coverage.
 */
public class SummarizeHgmail extends Task {

    private File output;
    /** Output file. */
    public void setOutput(File output) {
        this.output = output;
    }

    // DirSet of repo roots might be more natural, but . will not match at root
    private FileSet hgmails;
    /** Hgmail files to summarize. */
    public void addConfiguredHgmails(FileSet hgmails) {
        this.hgmails = hgmails;
    }

    public @Override void execute() throws BuildException {
        try {
            try (PrintWriter w = new PrintWriter(output)) {
                DirectoryScanner scanner = hgmails.getDirectoryScanner(getProject());
                scanner.scan();
                for (String hgmailS : new TreeSet<String>(Arrays.asList(scanner.getIncludedFiles()))) {
                    w.println("SUMMARY OF " + hgmailS);
                    File hgmail = new File(scanner.getBasedir(), hgmailS);
                    File dir = hgmail.getParentFile();
                    File[] kids = dir.listFiles(new FileFilter() {
                        public boolean accept(File f) {
                            return /* contrib/contrib is a self-link */f.exists() && !f.getName().equals(".hg") && !new File(f, ".hg").isDirectory();
                        }
                    });
                    if (kids == null) {
                        throw new BuildException(dir.getPath(), getLocation());
                    }
                    // module dir such as java.source or contrib/autosave or file such as .hgtags, to list of addressees such as core-commits@platform.netbeans.org
                    Map<String,List<String>> notifications = new TreeMap<>();
                    try (Reader r = new FileReader(hgmail)) {
                        BufferedReader br = new BufferedReader(r);
                        String line;
                        while ((line = br.readLine()) != null) {
                            if (line.matches("^[\\[#].+|\\s*")) {
                                continue;
                            }
                            String[] split = line.split("=", 2);
                            if (split.length < 2) {
                                throw new BuildException(line, getLocation());
                            }
                            for (String patt : split[1].trim().split(",")) {
                                boolean isdir = false;
                                if (patt.endsWith("/*")) {
                                    patt = patt.substring(0, patt.length() - 2);
                                    isdir = true;
                                }
                                if (patt.contains("/")) {
                                    throw new BuildException("Notifications of subdirs not yet supported: " + line, getLocation());
                                }
                                List<String> modules = new ArrayList<>();
                                if (patt.contains("*")) {
                                    // Pattern, have to search for matches.
                                    Pattern wild = Pattern.compile("\\Q" + patt.replace("*", "\\E.*\\Q") + "\\E");
                                    for (File kid : kids) {
                                        String name = kid.getName();
                                        if (wild.matcher(name).matches()) {
                                            if (kid.isDirectory() ^ isdir) {
                                                throw new BuildException("Dir pattern matched non-dir or vice-versa: " + kid + " vs. " + line, getLocation());
                                            }
                                            modules.add(name);
                                        }
                                    }
                                }
                                if (modules.isEmpty()) {
                                    // Single file (may or may not exist), or unmatched pattern; so record it.
                                    modules.add(patt);
                                }
                                String addr = split[0].trim();
                                for (String module : modules) {
                                    List<String> addressees = notifications.get(module);
                                    if (addressees == null) {
                                        addressees = new ArrayList<>();
                                        notifications.put(module, addressees);
                                    }
                                    addressees.add(addr);
                                }
                            }
                        }
                    }
                    // Now find unmentioned modules:
                    for (File kid : kids) {
                        String name = kid.getName();
                        if (!notifications.containsKey(name)) {
                            notifications.put(name, new ArrayList<>());
                        }
                    }
                    // Report:
                    for (Map.Entry<String,List<String>> entry : notifications.entrySet()) {
                        String path = entry.getKey();
                        w.println("PATH " + path);
                        if (!new File(dir, path).exists()) {
                            w.println("  NONEXISTENT");
                        }
                        List<String> addressees = entry.getValue();
                        if (addressees.isEmpty()) {
                            w.println("  UNNOTIFIED");
                        } else {
                            if (new HashSet<>(addressees).size() < addressees.size()) {
                                w.println("  DUPLICATES");
                            }
                            Collections.sort(addressees);
                            for (String addressee : addressees) {
                                w.println("  NOTIFIES " + addressee);
                            }
                        }
                    }
                    w.println();
                }
                w.flush();
            }
            log(output + ": hgmail summary written");
        } catch (IOException x) {
            throw new BuildException(x, getLocation());
        }
    }

}
