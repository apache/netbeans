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

import com.sun.tools.javac.api.JavacTaskImpl;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import junit.framework.*;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.java.source.ElementUtils;
import org.netbeans.modules.java.source.TestUtil;
import org.netbeans.modules.java.source.indexing.TransactionContext;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author Petr Hrebejk
 */
public class ClasspathInfoTest extends NbTestCase {
    
    private File workDir;
    private File rtJar;
    private ClassPath bootPath;
    private ClassPath classPath;
    
    private final String SOURCE =
                "package some;" +
                "public class MemoryFile<K,V> extends javax.swing.JTable {" +
                "    public java.util.Map.Entry<K,V> entry;" +                       
                "}";
    
    public ClasspathInfoTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        this.clearWorkDir();
        File workDir = getWorkDir();
        File cacheFolder = new File (workDir, "cache"); //NOI18N
        cacheFolder.mkdirs();
        IndexUtil.setCacheFolder(cacheFolder);
        rtJar = FileUtil.normalizeFile(TestUtil.createRT_JAR(workDir));
        URL url = FileUtil.getArchiveRoot (Utilities.toURI(rtJar).toURL());
        this.bootPath = ClassPathSupport.createClassPath (new URL[] {url});
        this.classPath = ClassPathSupport.createClassPath(new URL[0]);
    }

    protected void tearDown() throws Exception {
        //Delete unneeded rt.jar
        rtJar.delete();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(ClasspathInfoTest.class);        
        return suite;
    }

    public void testCreate() {
        ClasspathInfo ci = ClasspathInfo.create( bootPath, classPath, null);
        assertNotNull( "Classpath Info should be created", ci );
    }
    
    @RandomlyFails
    public void testGetTypeDeclaration() throws Exception {
        ClasspathInfo ci = ClasspathInfo.create( bootPath, classPath, null);
        JavacTaskImpl jTask = JavacParser.createJavacTask(ci,  (DiagnosticListener) null, (String) null, null, null, null, null, null, Collections.emptyList());
        jTask.enter(); 
	
        List<String> notFound = new LinkedList<String>();
        JarFile jf = new JarFile( rtJar );       
        for( Enumeration entries = jf.entries(); entries.hasMoreElements(); ) {
            JarEntry je = (JarEntry)entries.nextElement();
            String jeName = je.getName();
            if ( !je.isDirectory() && jeName.endsWith( ".class" ) ) {
                String typeName = jeName.substring( 0, jeName.length() - ".class".length() );

                typeName = typeName.replace( "/", "." ); //.replace( "$", "." );
                TypeElement te = ElementUtils.getTypeElementByBinaryName(jTask, typeName );
//                assertNotNull( "Declaration for " + typeName + " should not be null.", td );
                if ( te == null ) {
                    if (!typeName.endsWith("package-info")) {
                        notFound.add( typeName );
                    }
                }
            }
        }
        
        assertTrue( "Should be empty " + notFound, notFound.isEmpty() );
        
    }    
    
    public void testGetPackageDeclaration() throws Exception {
        ClasspathInfo ci = ClasspathInfo.create( bootPath, classPath, null);
        JavaFileManager fm = ClasspathInfoAccessor.getINSTANCE().createFileManager(ci, null);
        JarFile jf = new JarFile( rtJar );
        for( Enumeration entries = jf.entries(); entries.hasMoreElements(); ) {
            JarEntry je = (JarEntry)entries.nextElement();
            String jeName = je.getName();
            if ( je.isDirectory() ) {
                String packageName = jeName.replace( "/", "." );
                if ( !fm.list( StandardLocation.PLATFORM_CLASS_PATH,packageName, EnumSet.of( JavaFileObject.Kind.CLASS ), false).iterator().hasNext() ) {
                    // empty package
                    continue;
                }
                PackageElement pd = JavacParser.createJavacTask(ci,  (DiagnosticListener) null, (String) null, null, null, null, null, null, Collections.emptyList()).getElements().getPackageElement( packageName );
                assertNotNull( "Declaration for " + packageName + " should not be null.", pd );
            }
        }
    }
    
    
    private static ClassPath createSourcePath (FileObject testBase) throws IOException {
        FileObject root = testBase.createFolder("src");        
        return ClassPathSupport.createClassPath(new FileObject[]{root});
    }
    
    private static FileObject createJavaFile (FileObject root, String path, String content) throws IOException {
        FileObject fo = FileUtil.createData(root, path);
        final FileLock lock = fo.lock();
        try {
            PrintWriter out = new PrintWriter (new OutputStreamWriter (fo.getOutputStream(lock)));
            try {
                out.print(content);
            } finally {
                out.close();
            }
        } finally {
            lock.releaseLock();
        }
        return fo;
    }
    
    private static void assertEquals (final String[] binNames,
            final Iterable<JavaFileObject> jfos, final JavaFileManager fm) {
        final Set<String> bs = new HashSet<String>();
        bs.addAll(Arrays.asList(binNames));
        for (JavaFileObject jfo : jfos) {
            final String bn = fm.inferBinaryName (StandardLocation.SOURCE_PATH, jfo);
            assertNotNull(bn);
            assertTrue(bs.remove(bn));
        }
        assertTrue(bs.isEmpty());
        
    }
    
    //TODO: the FileManager created from ClasspathInfoAccessor.getINSTANCE().createFileManager ignores the MemoryFileManager
    //disabling the test for now.
    public void DISABLEtestMemoryFileManager () throws Exception {
        final ClassPath scp = createSourcePath(FileUtil.toFileObject(this.getWorkDir()));
        createJavaFile(scp.getRoots()[0], "org/me/Lib.java", "package org.me;\n class Lib {}\n");
        TransactionContext tx = TransactionContext.beginStandardTransaction(scp.getRoots()[0].toURL(), true, ()->true, false);
        try {
            final ClasspathInfo cpInfo = ClasspathInfoAccessor.getINSTANCE().create( bootPath, ClassPath.EMPTY, classPath, ClassPath.EMPTY, ClassPath.EMPTY, scp, ClassPath.EMPTY, null, true, true, true, false, false, null);
            final JavaFileManager fm = ClasspathInfoAccessor.getINSTANCE().createFileManager(cpInfo, null);
            Iterable<JavaFileObject> jfos = fm.list(StandardLocation.SOURCE_PATH, "org.me", EnumSet.of(JavaFileObject.Kind.SOURCE), false);
            assertEquals (new String[] {"org.me.Lib"}, jfos, fm);
            ClasspathInfoAccessor.getINSTANCE().registerVirtualSource(cpInfo, FileObjects.memoryFileObject("org.me","Main.java",
            null,System.currentTimeMillis(),"package org.me;\n class Main{}\n"));
            jfos = fm.list(StandardLocation.SOURCE_PATH, "org.me", EnumSet.of(JavaFileObject.Kind.SOURCE), false);
            assertEquals (new String[] {"org.me.Lib","org.me.Main"}, jfos, fm);
            ClasspathInfoAccessor.getINSTANCE().unregisterVirtualSource(cpInfo, "org.me.Main");
            jfos = fm.list(StandardLocation.SOURCE_PATH, "org.me", EnumSet.of(JavaFileObject.Kind.SOURCE), false);
            assertEquals (new String[] {"org.me.Lib"}, jfos, fm);
        } finally {
            TransactionContext.get().commit();
        }
    }


    
}
