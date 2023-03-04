/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.java.source.parsing;
import java.util.jar.JarFile;
import java.io.File;
import java.util.zip.ZipFile;
import junit.framework.Test;
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
    
    public static Test suite() {
        return new NoopClass("noop");
    }
    public static class NoopClass extends NbTestCase {

        public NoopClass(String name) {
            super(name);
        }

        public void noop() {}
    }
}
