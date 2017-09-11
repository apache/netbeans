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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.java.j2seproject.copylibstask;

import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.PathTokenizer;
import org.apache.tools.ant.Task;

import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.ZipFileSet;

/**
 * Ant task that copies multiple files specified by one property (separated by ';').
 * It merely delegates to copy task for every file in the files attribute.
 * The files attribute is parsed as a Path structure.
 */
public class CopyFiles extends Task {

    /**
     * Destination directory to which files should be copied. Mandatory.
     */
    private File todir;

    /**
     * Special case for WAR file deployed within EAR: if file contains any files matching
     * META-INF/*.tld or META-INF/tlds/*.tld then jar file will be
     * copied to <code>${iftldtodir}/lib</code> instead of todir and classes folder will
     * be copied to <code>${iftldtodir}/classes</code>. See issue #58167 for more details.
     */
    private File iftldtodir;

    /**
     * Files to copy; can be Ant path property with entries separated by ":"
     * or ";". Mandatory.
     */
    private String files;

    /**
     * Name of property to set. Value is list of file names (no path;
     * separated by space) which were copied to todir folder. If a file was
     * copied to iftldtodir folder it is not included in the list. If file
     * was actaully folder then "." will be used instead. The value is used
     * for example when WAR is build from several JARs and these need to be
     * listed in WAR's manifest in classpath entry.
     */
    private String manifestproperty;

    @Override
    public void execute() throws BuildException {
        if (files == null) {
            throw new BuildException ("files must be set.");
        }
        if (todir == null) {
            throw new BuildException ("todir must be set.");
        }
        boolean folderAdded = false;
        StringBuilder sb = new StringBuilder();
        PathTokenizer tokenizer = new PathTokenizer (getFiles ());
        while (tokenizer.hasMoreTokens ()) {
            File f = getProject().resolveFile(tokenizer.nextToken());
            File toDirectory = todir;
            if (iftldtodir != null && containsTLD(f)) {
                if (f.isFile()) {
                    toDirectory = new File(iftldtodir, "lib"); // NOI18N
                } else {
                    toDirectory = new File(iftldtodir, "classes"); // NOI18N
                }
            } else {
                if (sb.length() > 0) {
                    sb.append(" "); // NOI18N
                }
                if (f.isFile()) {
                    sb.append(f.getName());
                } else {
                    // for folder add "." once
                    if (!folderAdded) {
                        sb.append("."); // NOI18N
                        folderAdded = true;
                    }
                }
            }
            Copy cp = (Copy) getProject ().createTask ("copy"); // NOI18N
            cp.setTodir (toDirectory);
            if (f.isDirectory ()) {
                FileSet fset = new FileSet ();
                fset.setDir (f);
                cp.addFileset (fset);
            } else {
                cp.setFile (f);
            }
            cp.execute ();
        }
        if (manifestproperty != null) {
            getProject().setProperty(manifestproperty, sb.toString());
        }
    }

    private boolean containsTLD(File f) {
        FileSet fs;
        if (f.isFile()) {
            ZipFileSet zpf = new ZipFileSet();
            zpf.setSrc(f);
            fs = zpf;
        } else {
            fs = new FileSet();
            fs.setDir(f);
        }
	// #187624 - web-fragment.xml must stay under WEB-INF/lib in EAR app
	// TODO: 'iftldtodir' attribute should be renamed to some more general name
        fs.setIncludes("META-INF/**/*.tld,META-INF/web-fragment.xml"); // NOI18N
        DirectoryScanner ds = fs.getDirectoryScanner(getProject());
        ds.scan();
        return ds.getIncludedFilesCount() > 0;
    }

    public String getFiles() {
        return this.files;
    }

    public void setFiles (String files) {
        assert files != null;
        this.files = files;
    }

    public File getToDir() {
        return this.todir;
    }

    public void setToDir (File todir) {
        assert todir != null;
        this.todir = todir;
    }

    public File getIfTLDToDir() {
        return this.iftldtodir;
    }

    public void setIfTLDToDir(File iftldtodir) {
        this.iftldtodir = iftldtodir;
    }

    public String getManifestProperty() {
        return manifestproperty;
    }

    public void setManifestProperty(String manifestproperty) {
        this.manifestproperty = manifestproperty;
    }

}
