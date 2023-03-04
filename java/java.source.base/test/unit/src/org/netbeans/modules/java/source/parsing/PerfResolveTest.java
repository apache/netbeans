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
/** Tests for basic JDK operations
 *
 * @author Petr Hrebejk
 */
public class PerfResolveTest extends NbTestCase {

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
                
    public PerfResolveTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        workDir = getWorkDir();
        TestUtil.copyFiles( workDir, TestUtil.RT_JAR, "jdk/JTable.java" );
        rtJar = new File( workDir, TestUtil.RT_JAR );
        URL url = FileUtil.getArchiveRoot (Utilities.toURI(rtJar).toURL());
        this.bootPath = ClassPathSupport.createClassPath (new URL[] {url});
        this.classPath = ClassPathSupport.createClassPath(new URL[0]);
    }

    /*
    public void testExtendsJTable() throws Exception {
        resolve( "MemoryFile.java", SOURCE );
    }
    */
    
    /*
    public void testJTable() throws Exception {        
        String source = TestUtil.fileToString( new File( workDir, "jdk/JTable.java" ) );
        resolve( "JTable.java", source );
    }
    */
    
    
    public void resolve( String fileName, String source ) throws Exception {
//        JavacInterface ji;
//        
//        StopWatch swatch = new StopWatch();
//                
//        
//        for( int i = 0; i < 10; i++ ) {
//            
//            System.out.println("---------- (" + i + ")" );
//            
//            swatch.start();
//            ji = JavacInterface.create( bootPath, classPath, null);
//            swatch.stop( "JI create done" );
//
//            swatch.start();        
//            CompilationUnitTree cu = ji.parse( FileObjects.memoryFileObject( SOURCE, "MemoryFile.java"), null ); 
//            swatch.stop( "Parsing done" );
//
//            swatch.start();
//            ji.resolveElements( cu );        
//            swatch.stop( "Resolution done " );                
//        }
        
    }

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
