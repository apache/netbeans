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

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.junit.NbTestCase;

/**
 * Provides basic functionality for ProjectImporter tests.
 *
 * @author mkrauskopf
 */
public abstract class ProjectImporterTestCase extends NbTestCase {
    
    private static final int BUFFER = 2048;
    
    /*
     * If true a lot of information about parsed project will be written to a
     * console.
     */
    private static boolean verbose;
    
    /** Creates a new instance of ProjectImporterTestCase */
    public ProjectImporterTestCase(String name) {
        super(name);
    }
    
    protected @Override void setUp() throws Exception {
        super.setUp();
        /* comment this out to see verbose info */
        // setVerbose(true);
        clearWorkDir();
    }
    
    protected void setVerbose(boolean verbose) {
        ProjectImporterTestCase.verbose = verbose;
    }
    
    /*
     * XXX - doesn't similar method already exist somewhere in the API?
     * XXX - If not replace with JarFileSystem as hinted by Radek :)
     */
    protected File extractToWorkDir(String archiveFile) throws IOException {
        return extractToWorkDir(archiveFile, this);
    }

    public static File extractToWorkDir(String archiveFile, NbTestCase testCase) throws IOException {
        ZipInputStream zis = null;
        BufferedOutputStream dest = null;
        try {
            FileInputStream fis = new FileInputStream(new File(testCase.getDataDir(), archiveFile));
            zis = new ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry;
            while((entry = zis.getNextEntry()) != null) {
                byte data[] = new byte[BUFFER];
                File entryFile = new File(testCase.getWorkDir(), entry.getName());
                if (entry.isDirectory()) {
                    entryFile.mkdirs();
                } else {
                    FileOutputStream fos = new FileOutputStream(entryFile);
                    dest = new BufferedOutputStream(fos, BUFFER);
                    int count;
                    while ((count = zis.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, count);
                    }
                    dest.flush();
                }
            }
        } finally {
            if (zis != null) { zis.close(); }
            if (dest != null) { dest.close(); }
        }
        // return the directory (without ".zip" - convention used here)
        return new File(testCase.getWorkDir(), archiveFile.substring(0, archiveFile.length() - 4));
    }
    
}
