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
import java.util.jar.JarFile;
import java.io.File;
import java.util.zip.ZipFile;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.TestUtil;
import org.netbeans.modules.java.source.usages.IndexUtil;

/** Tests performance of batch compilation large amount of files.
 *
 * This test will not work unless you have src.zip in the data/jdk 
 * directory.
 *
 * @author Petr Hrebejk
 */
public class PerfBatchCompilationTest extends NbTestCase {
    
    private File workDir;
    private File rtFile, srcFile;
    private File rtFolder, srcFolder;
    private File javacSrcFolder;
    private CachingArchiveProvider archiveProvider;
    
    public PerfBatchCompilationTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        File retouche = TestUtil.getDataDir().getParentFile().getParentFile().getParentFile().getParentFile();
        File javac = new File(retouche, "Jsr199");

        clearWorkDir();
        workDir = getWorkDir();
        System.out.println("Workdir " + workDir);
        TestUtil.copyFiles(TestUtil.getJdkDir(), workDir, TestUtil.RT_JAR);
        TestUtil.copyFiles(TestUtil.getJdkDir(), workDir, TestUtil.SRC_ZIP);

        rtFile = new File(workDir, TestUtil.RT_JAR);
        JarFile rtJar = new JarFile(rtFile);
        srcFile = new File(workDir, TestUtil.SRC_ZIP);
        ZipFile srcZip = new ZipFile(srcFile);



        //rtFolder = new File( workDir, "rtFolder" );
        //TestUtil.unzip( rtJar, rtFolder );

        srcFolder = new File(workDir, "src");
        TestUtil.unzip(srcZip, srcFolder);

        // Create archive provider
        archiveProvider = CachingArchiveProvider.getDefault();

        // Set up the output path
        File cacheDir = new File(workDir, "cache");
        cacheDir.mkdirs();
        IndexUtil.setCacheFolder(cacheDir);
    }
    
    public void testJdkSourceCompilationAtOnce() throws Throwable {
	
	fail( "Would throw OutOfMemory error anyway" );
	
//	System.out.println("MAX MEM " + Runtime.getRuntime().maxMemory() );
//	
//        // Build list of sources to compile
//        List<JavaFileObject> files = getFiles( setup.archiveProvider.getArchive( setup.srcFolder ) );
//
//        URL rtUrl = FileUtil.getArchiveRoot(setup.rtFile.toURI().toURL());
//        URL srcUrl = FileUtil.getArchiveRoot(setup.srcFolder.toURI().toURL());
//        
//        ClassPath bcp = ClassPathSupport.createClassPath(new URL[] {rtUrl});
//        ClassPath ccp = ClassPathSupport.createClassPath(new URL[0]);
//        ClassPath scp = ClassPathSupport.createClassPath(new URL[] {srcUrl});
//        JavacInterface javacInterface = JavacInterface.create( setup.archiveProvider, bcp, ccp, scp);
//
//        StopWatch swatch = new StopWatch();
//        swatch.start();
//        javacInterface.attrFiles( files );
//        swatch.stop( "Attributed" );
//        System.out.println("MEM eaten " + ( ( Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() ) / 1024 / 1000 ) + " MB" );
//        System.gc(); System.gc(); System.gc();
//        System.out.println("MEM eaten after gc "+ ( ( Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() ) / 1024 / 1000 ) + " MB" );
//       
    }
    
    public void testJdkSourceCompilationFileByFile() throws Throwable {

	fail( "Would throw OutOfMemory error anyway" );
		
//	System.out.println("MAX MEM " + Runtime.getRuntime().maxMemory() );
//	
//        // Build list of sources to compile
//        List<JavaFileObject> files = getFiles( setup.archiveProvider.getArchive( setup.srcFolder ) );
//
//        URL rtUrl = FileUtil.getArchiveRoot(setup.rtFile.toURI().toURL());
//        URL srcUrl = FileUtil.getArchiveRoot(setup.srcFolder.toURI().toURL());
//        
//        ClassPath bcp = ClassPathSupport.createClassPath(new URL[] {rtUrl});
//        ClassPath ccp = ClassPathSupport.createClassPath(new URL[0]);
//        ClassPath scp = ClassPathSupport.createClassPath(new URL[] {srcUrl});
//        JavacInterface javacInterface = JavacInterface.create( setup.archiveProvider, bcp, ccp, scp);
//
//        StopWatch swatch = new StopWatch();
//        swatch.start();
//	
//	List<JavaFileObject> l = new ArrayList<JavaFileObject>(1); 
//	l.add( null );
//	for( JavaFileObject jfo : files ) {
//	    l.set( 0, jfo);
//	    System.out.println("JFO " + jfo.getPath()  + jfo.getName() );
//	    javacInterface = JavacInterface.create( setup.archiveProvider, bcp, ccp, scp);
//	    javacInterface.attrFiles( l );
//	}
//        swatch.stop( "Attributed" );
//        System.out.println("MEM eaten " + ( ( Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() ) / 1024 / 1000 ) + " MB" );
//        System.gc(); System.gc(); System.gc();
//        System.out.println("MEM eaten after gc "+ ( ( Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() ) / 1024 / 1000 ) + " MB" );
//        
    }
    
    
    // Private methods ---------------------------------------------------------
    
//    private List<JavaFileObject> getFiles( Archive archive ) {
//
//	List<JavaFileObject> result = new LinkedList<JavaFileObject>();
//	
//	Factory<JavaFileObject, Archive.Entry> factory = archive.getJavaFileObjectFactory();
//	Indexed<Archive.Entry> entries = archive.getFiles();
//	for( Archive.Entry entry : entries ) {
//	    if ( entry.getName().endsWith(".java" ) ) {
//		result.add( factory.create( entry ) );
//	    }
//	} 
//        return result;	       
//    }
    
}
