/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.java.source;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import junit.framework.*;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.classfile.ClassFile;

/** Tests for basic JDK operations
 *
 * @author Petr Hrebejk
 */
public class BasicPerformanceTest extends TestCase {
    
                
    public BasicPerformanceTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }
    
    
//    public void testClassfileSizes() throws Exception {
//        
//        StopWatch swatch = new StopWatch();
//                
//        JarFile jar = TestUtil.getTestJarFile();
//        
//        int size = 0;
//        
//        
//        ArrayList classfiles = new ArrayList();
//        for( Enumeration e = jar.entries(); e.hasMoreElements(); ) {
//            ZipEntry ze = (ZipEntry)e.nextElement();
//            if ( !ze.isDirectory()  )  {
//                try { 
//                    ClassFile cf = new ClassFile( jar.getInputStream( ze ) );
//                    // size += ScannerUtils.sizeOf( cf );
//                    classfiles.add( cf );
//                }
//                catch( IOException ex ) {
//                    // Ignore
//                }
//                
//            }
//        }
//
//        swatch.stop( "Got classes " + size );
//        
//        
//    }
//    
//    public void testNameCounts() throws Exception {
//        
//        JarFile jar = TestUtil.getTestJarFile();
//        
//        StopWatch swatch = new StopWatch();
//        Enumeration<JarEntry> entries = jar.entries();
//        ArrayList<String> names = new ArrayList<String>(); 
//        while( entries.hasMoreElements() ) {
//            names.add( entries.nextElement().getName() );
//        }        
//        swatch.stop( "Got enries " + names.size()  );
//        
//        HashSet<String> distinct = new HashSet<String>(); 
//        swatch.start();
//        for( String name : names ) {
//            distinct.addAll( parseName( name ) );
//        }
//        swatch.stop( "Got distinct strings " + distinct.size()  );
//        
// //        for( String name : distinct ) {
// //            System.out.println( name );
// //        }
//        
//    }
//    
//    private List<String> parseName( String str ) {
//
//        List result = new ArrayList();
//        
//        StringTokenizer strtok = new StringTokenizer( str, "/$" );
//        while( strtok.hasMoreTokens() ) {
//            result.add( strtok.nextToken() );
//        }
//                    
//        return result;
//    }
//    
//    public void testGetJarEntries() throws Exception {
//        
//        JarFile jar = TestUtil.getTestJarFile();
//        
//        StopWatch swatch = new StopWatch();
//        Enumeration<JarEntry> entries = jar.entries();
//        ArrayList<String> names = new ArrayList<String>(); 
//        while( entries.hasMoreElements() ) {
//            names.add( entries.nextElement().getName() );
//        }
//        swatch.stop( "Got enries" );
//                
//        swatch.start();
//        ArrayList<String> al = new ArrayList<String>( names ); 
//        Collections.sort( al );
//        swatch.stop( "sort using array list" );
//        
//        swatch.start();
//        TreeSet<String> ts = new TreeSet<String>( names ); 
//        while( entries.hasMoreElements() ) {
//            ts.add( entries.nextElement().getName() );
//        }
//        swatch.stop( "sort using tree set" );
//        
//        swatch.start();
//        
//        /*
//        HashMap<String> packages = new HashSet<String>();
//        for( String name : names ) {            
//            String pn = name.substring( 0, name.lastIndexOf( "/" ) ); // .replace( '/', '.' );
//            if ( !packages.contains( pn ) ) {
//                packages.add( pn );
//            }
//            
//        }
//        swatch.stop( "got packages" );
//        */
//        
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
