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

package org.netbeans.installer.infra.utils.style.ant;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.netbeans.installer.infra.utils.style.BasicStyleCheckerEngine;

/**
 *
 * @author ks152834
 */
public class BasicStyleCheckerTask extends Task {
    private List<FileSet> filesets;
    
    public BasicStyleCheckerTask() {
        this.filesets = new LinkedList<FileSet>();
    }
    
    public void addFileSet(FileSet fileset) {
        filesets.add(fileset);
    }

    @Override
    public void execute() throws BuildException {
        BasicStyleCheckerEngine engine = new BasicStyleCheckerEngine();
        
        try {
            for (FileSet fileset: filesets) {
                final DirectoryScanner scanner =
                        fileset.getDirectoryScanner(getProject());
                
                for (String filename: scanner.getIncludedFiles()) {
                    final File file =
                            new File(fileset.getDir(getProject()), filename);
                    
                    System.out.println(file.getCanonicalPath());
                    engine.check(file);
                }
            }
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
}
