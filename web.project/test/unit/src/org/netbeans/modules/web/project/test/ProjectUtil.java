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

package org.netbeans.modules.web.project.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public final class ProjectUtil {

    private ProjectUtil() {
        super();
    }
    
    public static File getProjectAsFile(NbTestCase test, String projectFolderName) throws IOException {
        File f = new File(new File(test.getDataDir(), "projects"), projectFolderName);
        if (!f.exists()) {
            // maybe it's zipped
            File archive = new File(new File(test.getDataDir(), "projects"), projectFolderName + ".zip");
            if (archive.exists() && archive.isFile()) {
                unZip(archive, test.getWorkDir());
                f = new File(test.getWorkDir(), projectFolderName);
            }
        }
        NbTestCase.assertTrue("project directory has to exists: " + f, f.exists());
        return f;
    }

    private static void unZip(File archive, File destination) throws IOException {
        if (!archive.exists()) {
            throw new FileNotFoundException(archive + " does not exist.");
        }
        ZipFile zipFile = new ZipFile(archive);
        Enumeration<? extends ZipEntry> all = zipFile.entries();
        while (all.hasMoreElements()) {
            extractFile(zipFile, all.nextElement(), destination);
        }
    }

    private static void extractFile(ZipFile zipFile, ZipEntry e, File destination) throws IOException {
        String zipName = e.getName();
        if (zipName.startsWith("/")) {
            zipName = zipName.substring(1);
        }
        if (zipName.endsWith("/")) {
            return;
        }
        int ix = zipName.lastIndexOf('/');
        if (ix > 0) {
            String dirName = zipName.substring(0, ix);
            File d = new File(destination, dirName);
            if (!(d.exists() && d.isDirectory())) {
                if (!d.mkdirs()) {
                    NbTestCase.fail("Warning: unable to mkdir " + dirName);
                }
            }
        }
        FileOutputStream os = new FileOutputStream(destination.getAbsolutePath() + "/" + zipName);
        InputStream is = zipFile.getInputStream(e);
        int n = 0;
        byte[] buff = new byte[8192];
        while ((n = is.read(buff)) > 0) {
            os.write(buff, 0, n);
        }
        is.close();
        os.close();
    }    
}
