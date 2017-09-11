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

package org.netbeans.modules.java.source.parsing;

import java.io.IOException;
import java.util.Arrays;
import java.util.jar.JarFile;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.util.zip.ZipFile;
import javax.tools.StandardLocation;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.TestUtil;
import org.openide.util.Utilities;

/** Base class for testing file managers. This class basically tests itself.
 *
 * @author Petr Hrebejk
 */
public class FileManagerTest extends NbTestCase {
    
    public FileManagerTest(String testName) {
        super(testName);
    }

    private File workDir;
    private File rtFile, srcFile;
    private File rtFolder, srcFolder;
    private CachingArchiveProvider archiveProvider;
    private Archive rtJarArchive;
    private Archive rtFolderArchive;
    private Archive srcZipArchive;
    private Archive srcFolderArchive;

    protected void setUp() throws Exception {
        clearWorkDir();
        workDir = getWorkDir();
        System.out.println("Workdir " + workDir);
        TestUtil.copyFiles(TestUtil.getJdkDir(), workDir, TestUtil.RT_JAR);
        TestUtil.copyFiles(TestUtil.getJdkDir(), workDir, TestUtil.SRC_ZIP);

        rtFile = new File(workDir, TestUtil.RT_JAR);
        JarFile rtJar = new JarFile(rtFile);
        srcFile = new File(workDir, TestUtil.SRC_ZIP);
        ZipFile srcZip = new ZipFile(srcFile);

        rtFolder = new File(workDir, "rtFolder");
        TestUtil.unzip(rtJar, rtFolder);

        srcFolder = new File(workDir, "src");
        TestUtil.unzip(srcZip, srcFolder);

        // Create archive provider
        archiveProvider = CachingArchiveProvider.getDefault();

        rtJarArchive = archiveProvider.getArchive(Utilities.toURI(rtFile).toURL(), true);
        rtFolderArchive = archiveProvider.getArchive(Utilities.toURI(rtFolder).toURL(), true);
        srcZipArchive = archiveProvider.getArchive(Utilities.toURI(srcFile).toURL(), true);
        srcFolderArchive = archiveProvider.getArchive(Utilities.toURI(srcFolder).toURL(), true);
    }

    protected JavaFileManagerDescripton[] getDescriptions() throws IOException {
	
	JavaFileManager tfm = createGoldenJFM( new File[] { rtFolder }, 
					       new File[] { srcFolder} );		    
	JavaFileManager gfm = createGoldenJFM( new File[] { rtFile }, 
					       new File[] { srcFile } );	
	return new JavaFileManagerDescripton[] {
	    new JavaFileManagerDescripton( tfm, gfm, srcZipArchive ),
	};
    }
    
    public static class JavaFileManagerDescripton {
	
		
	public Archive archive;
	public JavaFileManager testJFM;
	public JavaFileManager goldenJFM;
	
	public JavaFileManagerDescripton( JavaFileManager testJFM,
					  JavaFileManager goldenJFM,
					  Archive archive ) {	    
	    this.testJFM = testJFM;
	    this.goldenJFM = goldenJFM;
	    this.archive = archive;
	}
	
    }
    
    
    // Test methods ------------------------------------------------------------
//TODO: Fix me        
//    public void testList() throws Exception {
//		
//	JavaFileManagerDescripton[] jfmds = getDescriptions();
//	for ( JavaFileManagerDescripton jfmd : jfmds ) {
//            try {
//                JavaFileManager tfm = jfmd.testJFM;
//                JavaFileManager gfm = jfmd.goldenJFM;
//                Archive archive = jfmd.archive;
//
//                // Test all packages in the archive
//                for( String folder : Iterators.toIterable( archive.getFolders() ) ) {
//                    String pkg = FileObjects.convertFolder2Package( folder);
//
//                    for( JavaFileObject jfo : tfm.list(StandardLocation.CLASS_PATH, pkg, EnumSet.of( CLASS ), false ) ) {
//                        // Test that all of the JFOs are classes
//                        assertTrue( "Must be a class " + jfo.toUri(), jfo.getKind() == CLASS );
//                    }
//
//                    for( JavaFileObject jfo : tfm.list(StandardLocation.SOURCE_PATH,  pkg, EnumSet.of( SOURCE ), false  ) ) {
//                        // Test that all of the JFOs are sources
//                        assertTrue( "Must be a source " + jfo.toUri(), jfo.getKind() == SOURCE );		    
//                    }		
//
//                }
//            }finally {
//                jfmd.goldenJFM.close();
//                jfmd.testJFM.close();
//            }
//	}
//    }
//
//    public void testGetFileForInput() { 
//        JavaFileManagerDescripton[] jfmds = getDescriptions();
//	
//	for ( JavaFileManagerDescripton jfmd : jfmds ) {
//	}
//    }
//
//    public void testGetFileForOutput() throws Exception {
//        JavaFileManagerDescripton[] jfmds = getDescriptions();
//	
//	for ( JavaFileManagerDescripton jfmd : jfmds ) {
//	}
//    }
//   
//    public void testGetInputFile() {
//        JavaFileManagerDescripton[] jfmds = getDescriptions();
//	
//	for ( JavaFileManagerDescripton jfmd : jfmds ) {
//	}
//    }
//    
//  
//   NOT SURE WHAT TO TEST HERE    
//    
//    public void testSetLocation() {
//        fail("The test case is empty.");
//    }
//
//    public void testFlush() throws Exception {
//	fail("The test case is empty.");
//    }
//
//    public void testClose() throws Exception {
//        fail("The test case is empty.");
//    }
    
    // Other usefull methods ---------------------------------------------------
    
    /** Crates the default javac file managare tro have something to comare 
     * our file managers against
     */
    public static JavaFileManager createGoldenJFM( File[] classpath, File[] sourcpath ) throws IOException {
	
	JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fm = jc.getStandardFileManager (null, null, null);
	
	if ( classpath != null ) {
            fm.setLocation(StandardLocation.CLASS_PATH,Arrays.asList(classpath));
	}
	
	if ( sourcpath != null ) {
	    fm.setLocation(StandardLocation.SOURCE_PATH,Arrays.asList(sourcpath));
	}
	
	return fm;
		
    }
    
}
