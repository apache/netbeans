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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.util.FileUtils;

/**
 * Looks for project properties whose name start with "release." for files to copy.
 * If the remainder of the property name matches a file (relative to basedir),
 * it is copied to the location in the cluster given by the value.
 * Example: release.external/something.jar=modules/ext/something.jar
 * You can also use ZIP entries on the left side, e.g.
 * release.external/release.zip!/something.jar=modules/ext/something.jar
 */
public class ReleaseFilesCopy extends Task {

    private File cluster;
    public void setCluster(File cluster) {
        this.cluster = cluster;
    }

    public @Override void execute() throws BuildException {
        for (Map.Entry<String,Object> entry : ((Map<String,Object>) getProject().getProperties()).entrySet()) {
            String k = entry.getKey();
            if (k.startsWith("release.") && !k.matches("release\\.(files|files\\.extra|dir)")) {
                File to = FileUtils.getFileUtils().resolveFile(cluster, (String) entry.getValue());
                String fromString = k.substring(8);
                int bangSlash = fromString.indexOf("!/");
                if (bangSlash == -1) {
                    File from = getProject().resolveFile(fromString);
                    if (from.isFile()) {
                        log("Copying " + from + " to " + to);
                        to.getParentFile().mkdirs();
                        try {
                            FileUtils.getFileUtils().copyFile(from, to);
                        } catch (IOException x) {
                            throw new BuildException("Could not copy " + from + ": " + x, x, getLocation());
                        }
                    } else {
                        throw new BuildException("Could not find file " + from + " to copy", getLocation());
                    }
                } else {
                    File zip = getProject().resolveFile(fromString.substring(0, bangSlash));
                    if (zip.isFile()) {
                        try {
                            try (ZipFile zf = new ZipFile(zip)) {
                                String path = fromString.substring(bangSlash + 2);
                                log("Copying " + path + " in " + zip + " to " + to);
                                ZipEntry ze = zf.getEntry(path);
                                if (ze == null) {
                                    throw new BuildException("No such entry " + path + " in " + zip, getLocation());
                                }
                                InputStream is = zf.getInputStream(ze);
                                to.getParentFile().mkdirs();
                                OutputStream os = new FileOutputStream(to);
                                try {
                                    byte[] buf = new byte[4096];
                                    int read;
                                    while ((read = is.read(buf)) != -1) {
                                        os.write(buf, 0, read);
                                    }
                                } finally {
                                    os.close();
                                }
                            }
                        } catch (IOException x) {
                            throw new BuildException("Could not extract " + zip + ": " + x, x, getLocation());
                        }
                    } else {
                        throw new BuildException("Could not find file " + zip + " to extract", getLocation());
                    }
                }
            }
        }
    }

}
