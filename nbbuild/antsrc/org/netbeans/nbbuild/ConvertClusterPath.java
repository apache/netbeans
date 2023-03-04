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

import java.io.File;
import java.io.FilenameFilter;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.util.FileUtils;

/**
 * Converts cluster.path from value specified in platform.properties
 * to value usable by harness.
 *
 * TODO - describe actual changes
 *
 * @author Richard Michalsky
 */
public class ConvertClusterPath extends Task {
    private String from;
    private String id;
    private String basedir;
    private String to;

    /**
     * Name of property to which stores
     * @param to
     */
    public void setTo(String to) {
        this.to = to;
    }

    public void setBasedir(String basedir) {
        this.basedir = basedir;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ConvertClusterPath() {

    }

    @Override
    public void execute() throws BuildException {
        try {
            if (from == null || from.length() == 0)
                throw new BuildException("From parameter not specified.", getLocation());
            if ((id == null || id.length() == 0)
                    && (to == null && to.length() == 0))
                throw new BuildException("Either 'to' or 'id' parameter for converted path must be specified.", getLocation());
            if (basedir == null || basedir.length() == 0)
                basedir = getProject().getBaseDir().getAbsolutePath();

            log("Converting cluster.path from '" + from + "' relative to '" + basedir + "'.", Project.MSG_VERBOSE);
            FileUtils fu = FileUtils.getFileUtils();
            Project fakeproj = new Project();
            fakeproj.setBasedir(basedir);
            Path absPath = new Path(fakeproj, from);
            log("Converted path: '" + absPath.toString() + "'.", Project.MSG_VERBOSE);

            // When cluster does not exist, it is either bare name or one with different number
            final Pattern pat = Pattern.compile("(?:.*[\\\\/])?([^/\\\\]*?)([0-9.]+)?[/\\\\]?$");
            Path convPath = new Path(fakeproj);

            for (Iterator<Resource> it = absPath.iterator(); it.hasNext();) {
                FileResource element = (FileResource) it.next();
                File f = element.getFile();
                String fPath = f.getAbsolutePath();
                final Matcher cm = pat.matcher(fPath);
                if (f.exists()) {
                    if (! f.isDirectory())
                        throw new BuildException("Only directories can be elements of cluster.path. Got '" + fPath + "'", getLocation());
                    convPath.createPathElement().setLocation(f);
                    continue;
                }
                if (cm.matches()) {
                    // search for corresponding numbered cluster
                    File parent = f.getParentFile();

                    if (parent != null) {
                        File[] alternate = parent.listFiles(new FilenameFilter() {
                            public boolean accept(File dir, String name) {
                                Matcher am = pat.matcher(name);
                                return am.matches() && cm.group(1).equalsIgnoreCase(am.group(1));
                            }
                        });
                        if (alternate == null) {
                            Matcher matcher = Pattern.compile("^\\$\\{nbplatform\\.(.*)\\.netbeans\\.dest\\.dir\\}$").matcher(parent.getName());
                            if (matcher.matches()) {
                                throw new BuildException("Unknown platform name '" + matcher.group(1) + "'.", getLocation());
                            } else {
                                // cannot throw BuildException here, targets like 'clean' must run with nonexistent clusters as well
                                log("Warning: parent dir '" + parent.getAbsolutePath()
                                        + "' does not exist.", Project.MSG_WARN);
                                continue;
                            }
                        }
                        if (alternate.length > 0 && alternate[0].isDirectory()) {
                            if (cm.group(2) != null) // numbered cluster in cluster.path, found one with different number, warning
                                log("Warning: cluster '" + fPath + "' not found, using '" + alternate[0].getAbsolutePath() + "' instead.", Project.MSG_WARN);
                            else // bare name used
                                log("Cluster '" + alternate[0].getAbsolutePath() + "' found matching bare name '" + fPath + "'.", Project.MSG_VERBOSE);

                            convPath.createPathElement().setLocation(alternate[0]);
                            continue;
                        }
                    }
                }
                // no alternate cluster found
                log("Warning: no numbered cluster matching bare name '" + fPath + "' found.", Project.MSG_WARN);
            }

            if (id != null && id.length() > 0)
                getProject().addReference(id, convPath);
            if (to != null && to.length() > 0)
                getProject().setProperty(to, convPath.toString());
        } catch (Exception e) {
            throw new BuildException(e, getLocation());
        }
    }


}
