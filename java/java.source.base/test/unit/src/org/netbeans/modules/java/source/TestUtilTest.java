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
        
        File rt = TestUtil.createRT_JAR( workDir );
        
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
        
        File rt = TestUtil.createRT_JAR( workDir );
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
