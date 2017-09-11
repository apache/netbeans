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
////        for( String name : distinct ) {
////            System.out.println( name );
////        }
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

}
