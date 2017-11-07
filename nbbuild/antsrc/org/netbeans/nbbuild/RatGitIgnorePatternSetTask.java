/**
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
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.PatternSet;

/**
 *
 * @author skygo
 */
public class RatGitIgnorePatternSetTask extends Task {

    /**
     * https://issues.apache.org/jira/browse/RAT-171
     *
     */
    private File rootFolder;
    private String name;

    /**
     * gitingore file
     *
     * @param rootFolder
     */
    public void setRootFolder(File rootFolder) {
        this.rootFolder = rootFolder;
    }

    /**
     * name of property to set
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void execute() throws BuildException {
        List<String> commandAndArgs = new ArrayList<>();
        commandAndArgs.add("git");
        commandAndArgs.add("ls-files");
        commandAndArgs.add("--others");
        commandAndArgs.add("--ignored");
        commandAndArgs.add("--exclude-standard");
        PatternSet ps = new PatternSet();
        try {
            Process p = new ProcessBuilder(commandAndArgs).directory(rootFolder).start();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = r.readLine()) != null) {
                    // add every non empty line to exclude 
                    if (!line.trim().isEmpty()) {
                        PatternSet.NameEntry createExclude = ps.createExclude();
                        createExclude.setName(line);
                    }
                }
            } catch (IOException ex) {
                throw new BuildException("Cannot evaluate git information", ex);
            }
        } catch (IOException ex) {
            throw new BuildException("git process issue", ex);
        }

        getProject().addReference(name, ps);
    }
}
