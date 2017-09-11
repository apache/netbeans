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

import java.io.File;
import java.io.FilenameFilter;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
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

            for (Iterator it = absPath.iterator(); it.hasNext();) {
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
