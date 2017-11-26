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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Computes Mercurial changeset ID (in short format, i.e. 12 hex digits) of current working copy.
 * Compared to {@code hg id -i} or even {@code hg log -r . --template '{node|short}\n'}
 * it is very quick. Does not check for second merge parent or modification status
 * or tags. Sets property to {@code unknown-revn} if there is any problem.
 */
public class HgId extends Task {

    private File file;
    /**
     * Locates the repository.
     * May be repository root, or any file or folder inside it.
     */
    public void setFile(File file) {
        this.file = file;
    }

    private String property;
    /**
     * Declares which property to define with the resulting ID.
     */
    public void setProperty(String property) {
        this.property = property;
    }

    public @Override void execute() throws BuildException {
        if (file == null || property == null) {
            throw new BuildException("define file and property");
        }
        File dirstate = null;
        for (File root = file; root != null; root = root.getParentFile()) {
            File ds = new File(new File(root, ".hg"), "dirstate");
            if (ds.isFile()) {
                dirstate = ds;
                break;
            }
        }
        String id = "unknown-revn";
        if (dirstate != null && dirstate.length() >= 6) {
            try {
                InputStream is = new FileInputStream(dirstate);
                try {
                    byte[] data = new byte[6];
                    if (is.read(data) < 6) {
                        throw new IOException("truncated read");
                    }
                    id = String.format("%012x", new BigInteger(1, data));
                } finally {
                    is.close();
                }
            } catch (IOException x) {
                log("Could not read " + dirstate + ": " + x, Project.MSG_WARN);
            }
        } else {
            log("No dirstate found starting from " + file, Project.MSG_WARN);
        }
        assert id.length() == 12 : id;
        getProject().setNewProperty(property, id);
    }

}
