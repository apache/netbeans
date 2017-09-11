/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
                            ZipFile zf = new ZipFile(zip);
                            try {
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
                            } finally {
                                zf.close();
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
