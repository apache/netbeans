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
import java.io.File;
import java.net.URL;
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
    
}
