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
package org.netbeans.performance.cnd;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.zip.*;

import org.junit.Assert;

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
