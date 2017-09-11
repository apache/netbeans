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
package org.netbeans.performance.cnd;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.zip.*;

import junit.framework.Assert;

import org.openide.filesystems.*;
import org.openide.util.*;
import org.openide.util.lookup.*;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;


/**
 * Utilities methods.
 *
 * @author Pavel Flaska
 */
public class Utilities {

    /**
     * Prevent creation.
     */
    private Utilities() {
    }

    /**
     * Unzip the file <code>f</code> to folder <code>destDir</code>.
     *
     * @param f         file to unzip
     * @param destDir   destination directory
     */
    public static void unzip(File f, String destDir) {
        final int BUFFER = 2048;
        try {
            BufferedOutputStream dest = null;
            FileInputStream fis = new FileInputStream(f);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    File dir = new File(destDir + '/' + entry.getName());
                    dir.mkdir();
                } else {
                    int count;
                    byte contents[] = new byte[BUFFER];
                    // write the files to the disk
                    FileOutputStream fos = new FileOutputStream(destDir + "/" + entry.getName());
                    dest = new BufferedOutputStream(fos, BUFFER);
                    while ((count = zis.read(contents, 0, BUFFER)) != -1) {
                        dest.write(contents, 0, count);
                    }
                    dest.flush();
                    dest.close();
                }
            }
            zis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Open project <code>projectName</code> located in <code>dir</code>
     * directory.
     *
     * @param projectName           project name to open
     * @param dir                   project's enclosing directory
     * @return file-object          representing project
     * @throws java.io.IOException  when project cannot be opened
     */
    public static FileObject openProject(String projectName, File dir) throws IOException {
        File projectsDir = FileUtil.normalizeFile(dir);
        FileObject projectsDirFO = FileUtil.toFileObject(projectsDir);
        FileObject projdir = projectsDirFO.getFileObject(projectName);
        Project p = ProjectManager.getDefault().findProject(projdir);
        OpenProjects.getDefault().open(new Project[]{p}, false);
        if (p == null) {
            throw new IOException("Project is not opened " + projectName);
        }
        return projdir;
    }   

    public static class TestLkp extends ProxyLookup {

        private static TestLkp DEFAULT;

        public TestLkp() {
            Assert.assertNull(DEFAULT);
            DEFAULT = this;
            ClassLoader l = TestLkp.class.getClassLoader();
            this.setLookups(
                    new Lookup[] {
                        Lookups.metaInfServices(l),
                        Lookups.singleton(l)
                    }
            );
        }

        public static void setLookupsWrapper(Lookup... l) {
            DEFAULT.setLookups(l);
        }
    }

    public static interface ParameterSetter {
        void setParameters();
    }
    
    public static class MyHandler extends Handler {

        private Map<String, Long> map = new HashMap<String, Long>();

        @Override
        public void publish(LogRecord record) {
            Long data;
            if (record == null) {
                return;
            }
            for (Object o : record.getParameters()) {
                if (o instanceof Long) {
                    data = (Long) o;
                    map.put(record.getMessage(), data);
                }
            }
        }

        public Long get(String key) {
            return map.get(key);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }
}
