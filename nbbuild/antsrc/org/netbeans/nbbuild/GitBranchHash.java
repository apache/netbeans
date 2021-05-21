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
        Path headroot = null;
        for (Path root = file.toPath(); root != null; root = root.getParent()) {
            Path headpath = root.resolve(".git/HEAD");
            if (Files.isRegularFile(headpath)) {
                headroot = headpath;
                break;
            }
        }
        String branch = "";
        String hash = "";
        try {
            if (headroot != null && Files.size(headroot) > 0l) {
                List<String> lines = Files.readAllLines(headroot);
                String line = lines.get(0);
                String revLink = line.substring(line.indexOf(':')+1).trim();
                branch = revLink.substring(revLink.lastIndexOf('/')+1).trim();
                Path revPath = headroot.getParent().resolve(revLink);
                if(Files.isRegularFile(revPath) && Files.size(revPath) > 0l) {
                    List<String> revlines = Files.readAllLines(revPath);
                    String revline = revlines.get(0);
                    if(revline.length()>=12){
                        hash = revline.trim();
                    } else {
                        log("no content in " + revPath, Project.MSG_WARN);                        
                    }
                } else {
                    log("unable to find revision info for " + revPath, Project.MSG_WARN);
                }
            } else {
                log("No HEAD found starting from " + file, Project.MSG_WARN);
            }
        } catch(IOException ex) {
            log("Could not read " + headroot + ": " + ex, Project.MSG_WARN);
        }
        if (!branch.isEmpty() && !hash.isEmpty()) {
            getProject().setNewProperty(branchProperty, branch);
            getProject().setNewProperty(hashProperty, hash);
        }
        
    }

}
