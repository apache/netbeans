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

package org.netbeans.modules.java.source;

import java.io.File;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.netbeans.junit.NbTestCase;


/** Makes sure the TestUtility class works as expected.
 *
 * @author Petr Hrebejk
 */
public class TestUtilTest extends NbTestCase {

    public TestUtilTest( String testName ) {
        super( testName );
    }

    protected void setUp() throws Exception {
        clearWorkDir();
    }

    public void testCopyResourceFile() throws Exception {
        
	String SAMPLE_FILE = "samples1/EmptyClass.java";
	
        File workDir = getWorkDir();
        
        TestUtil.copyFiles( workDir, SAMPLE_FILE );        
        File sf = new File( workDir, SAMPLE_FILE );
        
        assertEquals( "WorkDir must exist", true, sf.exists() );
        assertEquals( "WorkDir must be readable", true, sf.canRead() );
        assertEquals( "WorkDir must be writeable", true, sf.canWrite() );
        
	TestUtil.copyFiles( TestUtil.getJdkDir(), workDir, TestUtil.RT_JAR );        
        File rt = new File( workDir, TestUtil.RT_JAR );
        
        assertEquals( "WorkDir must exist", true, rt.exists() );
        assertEquals( "WorkDir must be readable", true, rt.canRead() );
        assertEquals( "WorkDir must be writeable", true, rt.canWrite() );
    }
    
    public void testCopySampleFile() throws Exception {
        
        File workDir = getWorkDir();
        
        TestUtil.copyFiles( workDir, "samples1/EmptyClass.java" );        
        File sample = new File( workDir, "samples1/EmptyClass.java" );
        
        assertEquals( "WorkDir must exist", true, sample.exists() );
        assertEquals( "WorkDir must be readable", true, sample.canRead() );
        assertEquals( "WorkDir must be writeable", true, sample.canWrite() );
    }
    
    public void testUnzip() throws Exception {
        
        File workDir = getWorkDir();
        
        TestUtil.copyFiles( TestUtil.getJdkDir(), workDir, TestUtil.RT_JAR );
        File rt = new File( workDir, TestUtil.RT_JAR );
        JarFile rtJar = new JarFile( rt );
        
        File dest = new File( workDir, "dest" );
        TestUtil.unzip( rtJar, dest ); // Unzip Jar file

        Set<String> entryNames = new TreeSet<String>(); 
        for( Enumeration<? extends ZipEntry> e = rtJar.entries(); e.hasMoreElements(); ) {
            addNames(  e.nextElement(), entryNames );
        }
        
        Set<String> fileNames = new TreeSet<String>();
        addNamesRecursively( dest, fileNames, dest.getPath().length() + 1 );
                
        // TestUtil.collectionDiff( fileNames, entryNames );
        
        assertEquals( "Sets should have the same size", entryNames.size(), fileNames.size() );        
        assertEquals( "Lists should be identical", entryNames, fileNames );
    }
    
    // Private methods ---------------------------------------------------------
    
    private void addNames( ZipEntry entry, Set<String> dest ) {
    
        
        String name = entry.getName();
        if ( entry.isDirectory() ) {
            name = name.substring( name.length() - 1 );
        }
        
        int index = name.indexOf( '/', 0 );        
        while( index != -1 ) {
            if ( index != 0 ) {                
                dest.add( name.substring( 0, index ) );
            }
            index = name.indexOf( '/', index + 1 );                
        }
        
        if ( !"/".equals( name ) ) {
            dest.add( name );
        }
                
    }
    
    private void addNamesRecursively( File folder, Set<String> list, int beginIndex ) {
        
        File[] files = folder.listFiles();
        for( File f : files ) {
            list.add( f.getPath().substring( beginIndex ) );
            if ( f.isDirectory() ) {
                addNamesRecursively( f, list, beginIndex );
            }
        }
                
    }
    
}
