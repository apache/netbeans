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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Forked from HgId to parse branch and git hash as fallback.
 */
public class GitBranchHash extends Task {

    private File file;
    /**
     * Locates the repository.
     * May be repository root, or any file or folder inside it.
     */
    public void setFile(File file) {
        this.file = file;
    }

    private String branchProperty;
    /**
     * Declares which property to define with the resulting branch info.
     */
    public void setBranchProperty(String branchProperty) {
        this.branchProperty = branchProperty;
    }
    
    private String hashProperty;
    /**
     * Declares which property to define with the resulting hash info.
     */
    public void setHashProperty(String hashProperty) {
        this.hashProperty = hashProperty;
    }


    @Override
    public void execute() throws BuildException {
        if (file == null || branchProperty == null || hashProperty == null) {
            throw new BuildException("define file, branch property and hash property");
        }
        Path headFile = null;
        Path gitDir = null;
        for (Path root = file.toPath(); root != null; root = root.getParent()) {
            Path headpath = root.resolve(".git/HEAD");
            if (Files.isRegularFile(headpath)) {
                headFile = headpath;
                gitDir = headFile.getParent();
                break;
            }
        }
        String branch = "";
        String hash = "";
        try {
            if (gitDir != null && headFile != null && Files.size(headFile) > 0l) {
                List<String> lines = Files.readAllLines(headFile);
                String line = lines.get(0);
                if (!line.contains(":") && (line.length() == 40)) {
                    //Detached HEAD
                    hash = line;
                    log("Detached HEAD please specify '" + branchProperty + "' externally.", Project.MSG_WARN);
                } else {
                    String refLink = line.substring(line.indexOf(':') + 1).trim();
                    branch = refLink.substring(refLink.lastIndexOf('/') + 1).trim();
                    Path refPath = gitDir.resolve(refLink);
                    if (Files.isRegularFile(refPath) && Files.size(refPath) > 0l) {
                        List<String> refLines = Files.readAllLines(refPath);
                        hash = refLines.stream()
                                .map(ref -> ref.trim())
                                .filter(ref -> ref.length() == 40)
                                .findFirst().orElse("");
                        if (hash.isEmpty()) {
                            log("No content in " + refPath, Project.MSG_WARN);
                        } else {
                            log("Found hash " + hash + " in " + refPath, Project.MSG_INFO);
                        }
                    } else if (Files.isRegularFile(gitDir.resolve("packed-refs"))) {
                        List<String> packedRefs = Files.readAllLines(gitDir.resolve("packed-refs"));
                        hash = packedRefs.stream()
                                .filter(ref -> ref.contains(refLink))
                                .map(ref -> ref.replace(refLink, "").trim())
                                .filter(ref -> ref.length() == 40)
                                .findFirst().orElse("");
                        if (hash.isEmpty()) {
                            log("Unable to find revision info for " + refPath + " in .git/packed-refs", Project.MSG_WARN);
                        } else {
                            log("Found hash " + hash + " for ref " + refPath + " in .git/packed-refs", Project.MSG_INFO);
                        }
                    } else {
                        log("Unable to find revision info for " + refPath, Project.MSG_WARN);
                    }
                }
            } else {
                log("No HEAD found starting from " + file, Project.MSG_WARN);
            }
        } catch (IOException ex) {
            log("Could not read " + headFile + ": " + ex, Project.MSG_WARN);
        }
        if (!branch.isEmpty()) {
            getProject().setNewProperty(branchProperty, branch);
        }
        if (!hash.isEmpty()) {
            getProject().setNewProperty(hashProperty, hash);
        }

    }

}

