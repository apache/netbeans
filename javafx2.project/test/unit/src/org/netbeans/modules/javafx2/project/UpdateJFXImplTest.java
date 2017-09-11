/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.project;

import java.io.*;
import junit.framework.Test;
import static org.junit.Assert.*;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.ide.FXProjectSupport;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Petr Somol
 */
public class UpdateJFXImplTest extends NbTestCase {
    
    /** Creates a new test. 
     * @param testName name of test
     */
    public UpdateJFXImplTest(String testName) {
        super(testName);
    }

    private static final String PROJECT_NAME = "SampleFXProject";
    private static File projectParentDir;
    private static Project project = null;
    private static J2SEPropertyEvaluator j2sePropEval = null;
    private static PropertyEvaluator evaluator = null;
    private static String JFXIMPL_DIR = "nbproject";
    private static String JFXIMPL_NAME = "jfx-impl";
    private static String JFXIMPL_BACKUP_NAME = "jfx-impl_backup";
    private static String JFXIMPL_UPDATED_README = "UPDATED.TXT";
    private static String JFXIMPL_FILE = JFXIMPL_DIR + "/" + JFXIMPL_NAME + ".xml";
    

    /** Set up. */
    protected @Override void setUp() throws IOException {
        MockLookup.setLayersAndInstances();
        clearWorkDir();
        System.out.println("FXFXFXFX  "+getName()+"  FXFXFXFX");
    }

    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.emptyConfiguration()
            .addTest(UpdateJFXImplTest.class,
                "testCreateProject",
                "testProjectInitialState",
                "testProjectInitialJFXImplState",
                "testProjectJFXImplNotUpdated",
                "testProjectJFXImplUpdated",
                "testProjectJFXImplUpdatedAgain",
                "testProjectJFXImplReplacedMissing"
            )
        .enableModules(".*").clusters(".*"));
    }

    /** Test createProject method. */
    public void testCreateProject() throws Exception {
        projectParentDir = this.getWorkDir();
        project = (Project)FXProjectSupport.createProject(projectParentDir, PROJECT_NAME);
        Project[] projects = OpenProjectList.getDefault().getOpenProjects();
        assertEquals("Only 1 project should be opened.", 1, projects.length);
        assertSame("Created project not opened.", project, projects[0]);
        j2sePropEval = project.getLookup().lookup(J2SEPropertyEvaluator.class);
        assertNotNull(j2sePropEval);
        evaluator = j2sePropEval.evaluator();
        assertNotNull(evaluator);
    }

    public void testProjectInitialState() throws Exception {
        assertNotNull(project);
        assertEquals("javafx.disable.autoupdate", JFXProjectProperties.JAVAFX_DISABLE_AUTOUPDATE);
        String noUpdate = evaluator.getProperty(JFXProjectProperties.JAVAFX_DISABLE_AUTOUPDATE);
        assertFalse(JFXProjectProperties.isTrue(noUpdate));
        assertEquals("javafx.disable.autoupdate.notification", JFXProjectProperties.JAVAFX_DISABLE_AUTOUPDATE_NOTIFICATION);
        String noNotify = evaluator.getProperty(JFXProjectProperties.JAVAFX_DISABLE_AUTOUPDATE_NOTIFICATION);
        assertFalse(JFXProjectProperties.isTrue(noNotify));
    }
    
    public void testProjectInitialJFXImplState() throws Exception {
        assertNotNull(project);
        assertNotNull(evaluator);
        
        Counts counts = countJFXImplFiles(JFXIMPL_DIR);
        // No. of jfx-impl.xml files
        assertEquals(1, counts.getFirst());
        // No. of jfx-impl_backup*.xml files
        assertEquals(0, counts.getSecond());
        // No. of UPDATED.TXT files
        assertEquals(0, counts.getThird());

        String crc = getCRC(JFXIMPL_FILE);
        assertNotNull(crc);
        System.out.println(JFXIMPL_FILE + " CRC = " + crc);
        assertTrue(JFXProjectUtils.isJfxImplCurrentVer(crc));
    }

    public void testProjectJFXImplNotUpdated() throws Exception {
        assertNotNull(project);
        assertNotNull(evaluator);
        
        Counts counts = countJFXImplFiles(JFXIMPL_DIR);
        // No. of jfx-impl.xml files
        assertEquals(1, counts.getFirst());
        // No. of jfx-impl_backup*.xml files
        assertEquals(0, counts.getSecond());
        // No. of UPDATED.TXT files
        assertEquals(0, counts.getThird());

        FileObject readmeFO = null;
        try {
            readmeFO = JFXProjectUtils.updateJfxImpl(project);
        } catch (Exception ex) {
            fail("JFXProjectUtils.updateJfxImpl threw IO exception.");
        }
        assertNull(readmeFO);

        Counts newcounts = countJFXImplFiles(JFXIMPL_DIR);
        // No. of jfx-impl.xml files
        assertEquals(1, newcounts.getFirst());
        // No. of jfx-impl_backup*.xml files
        assertEquals(0, newcounts.getSecond());
        // No. of UPDATED.TXT files
        assertEquals(0, newcounts.getThird());
        
        String crc = getCRC(JFXIMPL_FILE);
        assertNotNull(crc);
        System.out.println(JFXIMPL_FILE + " CRC = " + crc);
        assertTrue(JFXProjectUtils.isJfxImplCurrentVer(crc));
    }
    
    public void testProjectJFXImplUpdated() throws Exception {
        assertNotNull(project);
        assertNotNull(evaluator);

        Counts counts = countJFXImplFiles(JFXIMPL_DIR);
        // No. of jfx-impl.xml files
        assertEquals(1, counts.getFirst());
        // No. of jfx-impl_backup*.xml files
        assertEquals(0, counts.getSecond());
        // No. of UPDATED.TXT files
        assertEquals(0, counts.getThird());

        assertTrue(modifyFileContents(JFXIMPL_FILE));
        String crc = getCRC(JFXIMPL_FILE);
        assertNotNull(crc);
        System.out.println("Mock incorrect " + JFXIMPL_FILE + " CRC = " + crc);
        assertFalse(JFXProjectUtils.isJfxImplCurrentVer(crc));
        FileObject readmeFO = null;
        try {
            readmeFO = JFXProjectUtils.updateJfxImpl(project);
        } catch (Exception ex) {
            fail("JFXProjectUtils.updateJfxImpl threw IO exception.");
        }
        assertNotNull(readmeFO);

        Counts newcounts = countJFXImplFiles(JFXIMPL_DIR);
        // No. of jfx-impl.xml files
        assertEquals(1, newcounts.getFirst());
        // No. of jfx-impl_backup*.xml files
        assertEquals(1, newcounts.getSecond());
        // No. of UPDATED.TXT files
        assertEquals(1, newcounts.getThird());
        
        crc = getCRC(JFXIMPL_FILE);
        assertNotNull(crc);
        System.out.println("Correct " + JFXIMPL_FILE + " CRC = " + crc);
        assertTrue(JFXProjectUtils.isJfxImplCurrentVer(crc));
    }

    public void testProjectJFXImplUpdatedAgain() throws Exception {
        assertNotNull(project);
        assertNotNull(evaluator);
        
        Counts counts = countJFXImplFiles(JFXIMPL_DIR);
        // No. of jfx-impl.xml files
        assertEquals(1, counts.getFirst());
        // No. of jfx-impl_backup*.xml files
        assertEquals(1, counts.getSecond());
        // No. of UPDATED.TXT files
        assertEquals(1, counts.getThird());

        assertTrue(modifyFileContents(JFXIMPL_FILE));
        String crc = getCRC(JFXIMPL_FILE);
        assertNotNull(crc);
        System.out.println("Mock incorrect " + JFXIMPL_FILE + " CRC = " + crc);
        assertFalse(JFXProjectUtils.isJfxImplCurrentVer(crc));
        FileObject readmeFO = null;
        try {
            readmeFO = JFXProjectUtils.updateJfxImpl(project);
        } catch (Exception ex) {
            fail("JFXProjectUtils.updateJfxImpl threw IO exception.");
        }
        assertNotNull(readmeFO);

        Counts newcounts = countJFXImplFiles(JFXIMPL_DIR);
        // No. of jfx-impl.xml files
        assertEquals(1, newcounts.getFirst());
        // No. of jfx-impl_backup*.xml files
        assertEquals(2, newcounts.getSecond());
        // No. of UPDATED.TXT files
        assertEquals(1, newcounts.getThird());
        
        crc = getCRC(JFXIMPL_FILE);
        assertNotNull(crc);
        System.out.println("Correct " + JFXIMPL_FILE + " CRC = " + crc);
        assertTrue(JFXProjectUtils.isJfxImplCurrentVer(crc));
    }
    
    public void testProjectJFXImplReplacedMissing() throws Exception {
        assertNotNull(project);
        assertNotNull(evaluator);
        
        assertTrue(deleteFile(JFXIMPL_FILE));
        
        Counts counts = countJFXImplFiles(JFXIMPL_DIR);
        // No. of jfx-impl.xml files
        assertEquals(0, counts.getFirst());
        // No. of jfx-impl_backup*.xml files
        assertEquals(2, counts.getSecond());
        // No. of UPDATED.TXT files
        assertEquals(1, counts.getThird());

        FileObject readmeFO = null;
        try {
            readmeFO = JFXProjectUtils.updateJfxImpl(project);
        } catch (Exception ex) {
            fail("JFXProjectUtils.updateJfxImpl threw IO exception.");
        }
        assertNull(readmeFO);

        Counts newcounts = countJFXImplFiles(JFXIMPL_DIR);
        // No. of jfx-impl.xml files
        assertEquals(1, newcounts.getFirst());
        // No. of jfx-impl_backup*.xml files
        assertEquals(2, newcounts.getSecond());
        // No. of UPDATED.TXT files
        assertEquals(1, newcounts.getThird());
        
        String crc = getCRC(JFXIMPL_FILE);
        assertNotNull(crc);
        System.out.println("Correct " + JFXIMPL_FILE + " CRC = " + crc);
        assertTrue(JFXProjectUtils.isJfxImplCurrentVer(crc));
    }

    private String getCRC(String file) {
        assertNotNull(project);
        FileObject projDir = project.getProjectDirectory();
        FileObject jfxBuildFile = projDir.getFileObject(file);
        String computedCRC = null;
        if (jfxBuildFile != null) {
            final InputStream in;
            try {
                in = jfxBuildFile.getInputStream();
                try {
                    computedCRC = JFXProjectUtils.computeCrc32( in );
                } catch(IOException ioe) {
                    // no reaction
                } finally {
                    try {
                        in.close();
                    } catch (IOException ex) {
                        // no reaction
                    }
                }
            } catch (FileNotFoundException ex) {
                 // no reaction
            }
        }
        return computedCRC;
    }

    private boolean deleteFile(String file) {
        assertNotNull(project);
        FileObject projDir = project.getProjectDirectory();
        final FileObject deleteFile = projDir.getFileObject(file);
        boolean deleted = false;
        if (deleteFile != null) {
            try {
                deleteFile.delete();
                deleted = true;
            } catch (IOException ex) {
            }
        }
        return deleted;
    }
    
    private boolean modifyFileContents(String file) {
        assertNotNull(project);
        boolean modified = true;
        FileObject projDir = project.getProjectDirectory();
        final FileObject jfxBuildFile = projDir.getFileObject(file);
        if (jfxBuildFile != null) {
            try {
                ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                    @Override
                    public Void run() throws Exception {
                        OutputStream os = null;
                        FileLock lock = null;
                        try {
                            lock = jfxBuildFile.lock();
                            os = jfxBuildFile.getOutputStream(lock);
                            final PrintWriter out = new PrintWriter ( os );
                            try {
                                out.println("This is mock jfx-impl.xml file.");
                                out.println("Its only purpose is to yield CRC different");
                                out.println("from the current JFXProject's jfx-impl.xml.");
                            } finally {
                                if(out != null) {
                                    out.close ();
                                }
                            }
                        } finally {
                            if (lock != null) {
                                lock.releaseLock();
                            }
                            if (os != null) {
                                os.close();
                            }
                        }
                        return null;
                    }
                });
            } catch (MutexException mux) {
                modified = false;
            }
        }
        return modified;
    }
    
    private class Counts {
        
        private int first;
        private int second;
        private int third;
        
        Counts() {
            first = 0;
            second = 0;
            third = 0;
        }
        
        Counts(int f, int s, int t) {
            first = f;
            second = s;
            third = t;
        }

        public int getFirst() {
            return first;
        }
        
        public int getSecond() {
            return second;
        }

        public int getThird() {
            return third;
        }
    }

    private Counts countJFXImplFiles(String dir) {
        assertNotNull(project);
        FileObject projDir = project.getProjectDirectory();
        FileObject foDir = projDir.getFileObject(dir);
        assertTrue("Directory " + dir + " does not exist.", foDir != null);
        File fdir = FileUtil.toFile(foDir);
        assertTrue("Directory " + dir + " does not exist.", fdir.exists());
        int files = 0;
        int backups = 0;
        int readmes = 0;
        // No. of jfx-impl.xml files
        File[] fFiles = fdir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String string) {
                return string.equalsIgnoreCase(JFXIMPL_NAME + ".xml");
            }
        });
        if(fFiles != null) {
            files = fFiles.length;
        }
        // No. of jfx-impl_backup*.xml files
        File[] fBackups = fdir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String string) {
                return string.length() >= JFXIMPL_BACKUP_NAME.length() + 4
                        && string.substring(0,JFXIMPL_BACKUP_NAME.length()).equalsIgnoreCase(JFXIMPL_BACKUP_NAME) 
                        && string.substring(string.length() - 4, string.length()).equalsIgnoreCase(".xml");
            }
        });
        if(fBackups != null) {
            backups = fBackups.length;
        }
        // No. of UPDATED.TXT files
        File[] fReadmes = fdir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String string) {
                return string.equalsIgnoreCase(JFXIMPL_UPDATED_README);
            }
        });
        if(fReadmes != null) {
            readmes = fReadmes.length;
        }
        return new Counts(files, backups, readmes);
    }
    
}
