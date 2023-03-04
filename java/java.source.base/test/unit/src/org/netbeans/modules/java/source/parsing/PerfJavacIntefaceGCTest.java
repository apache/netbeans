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
import java.io.File;
import java.net.URL;
import junit.framework.Test;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.TestUtil;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;
/** Tests whether the JavacInterface gets GCed after some operations
 *
 * @author Petr Hrebejk
 */
public class PerfJavacIntefaceGCTest extends NbTestCase {
    
    private File workDir;
    private File rtJar;
    private ClassPath bootPath;
    private ClassPath classPath;
    private final String SOURCE =
                "package some;" +
                "import javax.swing.JTable;" +
                "import javax.swing.JLabel;" +
                "public class MemoryFile<K,V> extends JTable {" +
                "    public java.util.Map.Entry<K,V> entry;" +
                "    public JLabel label;" +
                "    public JTable table = new JTable();" +                       
                "    public MemoryFile() {}" +                       
                "}";
                
    public PerfJavacIntefaceGCTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        workDir = getWorkDir();
        TestUtil.copyFiles( workDir, TestUtil.RT_JAR, "jdk/JTable.java" );
        rtJar = new File( workDir, TestUtil.RT_JAR );
        URL url = FileUtil.getArchiveRoot (Utilities.toURI(rtJar).toURL());
        this.bootPath = ClassPathSupport.createClassPath (new URL[] {url});
        this.classPath = ClassPathSupport.createClassPath(new URL[0]);
    }

//    public void testSimple() throws Exception {
//        
//        JavacInterface ji = (JavacInterface) JavacInterface.create( bootPath, classPath, null);
//        WeakReference<JavacInterface> wr = new WeakReference<JavacInterface>( ji );
//        ji = null;
//        assertGC( "JavacInterface should be GCed", wr );
//        
//    }
//    
//    public void testAfterParse() throws Exception {
//        
//        JavacInterface ji = (JavacInterface) JavacInterface.create( bootPath, classPath, null);
//        ji.parse( FileObjects.memoryFileObject( SOURCE, "MemoryFile.java"), null );
//        WeakReference<JavacInterface> wr = new WeakReference<JavacInterface>( ji );
//        ji = null;
//        assertGC( "JavacInterface should be GCed", wr );
//        
//    }
//    
//    public void testAfterParseAndResolve() throws Exception {
//        
//        JavacInterface ji = (JavacInterface) JavacInterface.create( bootPath, classPath, null);
//        CompilationUnitTree cu = ji.parse( FileObjects.memoryFileObject( SOURCE, "MemoryFile.java"), null );
//        ji.resolveElements( cu );
//        WeakReference<JavacInterface> wr = new WeakReference<JavacInterface>( ji );
//        WeakReference<Context> ctx = new WeakReference<Context>( ji.getContext() );        
//        cu = null;
//        ji = null;
//        assertGC( "JavacInterface should be GCed", wr );
//        
//        // Visitor v = new SimpleXmlVisitor( new File( "/tmp/insane.xml" ) );
//        // ScannerUtils.scan( null, v, Collections.singleton( Context.class.getClassLoader() ), true );
//        
//        assertGC( "Context should be GCed", ctx );
//    }
//    
//    public void testAfterGetDeclaration() throws Exception {
//        
//        JavacInterface ji = (JavacInterface) JavacInterface.create( bootPath, classPath, null);
//        TypeDeclaration td  = ji.getTypeDeclaration( "java.lang.Object" );
//        WeakReference<TypeDeclaration> wr = new WeakReference<TypeDeclaration>( td );
//        WeakReference<Context> ctx = new WeakReference<Context>( ji.getContext() );
//        td = null;
//        ji = null;
//        assertGC( "Type Declaration be GCed", wr );
//        assertGC( "Context should be GCed", ctx );
//                
//    }
//    
//    
//    public void testCompilationUnitSize() throws Exception {
//        
//        JavacInterface ji = (JavacInterface) JavacInterface.create( bootPath, classPath, null);
//        CompilationUnitTree cu = ji.parse( FileObjects.memoryFileObject( SOURCE, "MemoryFile.java"), null );
//        ji.resolveElements( cu );
//        
//        assertSize( "Compilation unit should not be too big", Collections.singleton( ji ), 1600000, new Object[] { CachingArchiveProvider.getDefault() }  );
//        
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
