/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
            List<String> commandAndArgs = new ArrayList<String>(hgExecutable);
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
