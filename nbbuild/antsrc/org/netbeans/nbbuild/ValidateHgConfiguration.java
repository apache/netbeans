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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Look for common problems in your Hg configuration and warn about them.
 */
public class ValidateHgConfiguration extends Task {

    private File root;
    /** Location of NB source root. */
    public void setRoot(File root) {
        this.root = root;
    }

    @Override
    public void execute() throws BuildException {
        File dotHg = new File(root, ".hg");
        if (!dotHg.isDirectory()) {
            log(root + " is not a Mercurial repository", Project.MSG_VERBOSE);
            return;
        }
        List<String> hgExecutable = HgExec.hgExecutable();
        try {
            List<String> commandAndArgs = new ArrayList<>(hgExecutable);
            commandAndArgs.add("--config");
            commandAndArgs.add("extensions.churn="); // added to hgext right before 1.0 release
            commandAndArgs.add("showconfig");
            Process p = new ProcessBuilder(commandAndArgs).directory(root).start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            boolean foundUserName = false, foundCrlfHook = false;
            String line;
            while ((line = r.readLine()) != null) {
                if (line.matches("hooks\\.pretxncommit\\..+=python:(hgext\\.)?win32text\\.forbidcrlf")) {
                    foundCrlfHook = true;
                } else if (line.matches("ui\\.username=(.+ <)?\\w+@netbeans\\.org>?")) {
                    foundUserName = true;
                }
            }
            if (!foundUserName) {
                log("======== WARNING ========\n" +
                    "You need to configure a Mercurial username\n" +
                    "if you intend to push changes to NetBeans repositories.\n" +
                    "Format (in ~/.hgrc or Mercurial.ini):\n" +
                    "[ui]\n" +
                    "username = Robert Q. Hacker <rhacker@netbeans.org>\n" +
                    "=========================", Project.MSG_WARN);
            }
            if (!foundCrlfHook) {
                log("======== WARNING ========\n" +
                    "You need to guard against committing carriage returns into Mercurial\n" +
                    "if you intend to push changes to NetBeans repositories.\n" +
                    "Format (in ~/.hgrc or Mercurial.ini):\n" +
                    "[hooks]\n" +
                    "pretxncommit.crlf = python:hgext.win32text.forbidcrlf\n" +
                    "=========================", Project.MSG_WARN);
            }
            r = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line = r.readLine()) != null) {
                if (line.contains("failed to import extension churn")) {
                    log("======== WARNING ========\n" +
                        "You seem to be using a version of Mercurial older than 1.0.\n" +
                        "Please upgrade as your version may have serious bugs fixed in later versions.\n" +
                        "=========================", Project.MSG_WARN);
                    }
            }
        } catch (IOException x) {
            log("Could not verify Hg configuration: " + x, Project.MSG_WARN);
        }
    }

}
