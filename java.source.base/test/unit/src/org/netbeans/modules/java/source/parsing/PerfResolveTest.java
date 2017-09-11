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

}
