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

package org.openide.filesystems;

import java.io.*;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;


public class PrepareData {

    private static void prepareLocalFilesystem( File root, String name, int count, String prefix ) throws IOException {
        File subRoot = new File( root, name );
        subRoot.mkdir();
        for( int i=0; i<count; i++ ) {
            new File( subRoot, prefix + i ).createNewFile();
        }
    }

    private static void prepareJarFilesystem( File root, String name, int count, String prefix ) throws IOException {
        File jar = new File( root, name );
        FileOutputStream stream = new FileOutputStream( jar );
        JarOutputStream jarStream = new JarOutputStream( stream );
        for( int i=0; i<count; i++ ) {
            jarStream.putNextEntry( new ZipEntry( prefix + i ) );
            jarStream.closeEntry();
        }
        jarStream.close();
    }

    private static void prepareXmlFilesystem( File root, String name, int count, String prefix ) throws IOException {
        File jar = new File( root, name );
        FileOutputStream stream = new FileOutputStream( jar );
        OutputStreamWriter writer = new OutputStreamWriter( stream, "UTF8" );
        PrintWriter print = new PrintWriter( writer );
        print.println(
            "<?xml version=\"1.0\"?>" +
            "<!DOCTYPE filesystem PUBLIC " +
            "\"-//NetBeans//DTD Filesystem 1.0//EN\" " + 
            "\"http://www.netbeans.org/dtds/filesystem-1_0.dtd\">\n" +
            "<filesystem>"
        );

        for( int i=0; i<count; i++ ) {
            print.println( "<file name=\"" + prefix + i + "\"/>" );
        }
        print.println( "</filesystem>" );
        print.flush();
        print.close();
    }


    public static void main( String[] args ) throws IOException {
        File data = new File( "data" );
        prepareLocalFilesystem( data, "10", 10, "f" );
        prepareLocalFilesystem( data, "100", 100, "f" );
        prepareLocalFilesystem( data, "1000", 1000, "f" );
        prepareJarFilesystem( data, "flat-10.jar", 10, "f" );
        prepareJarFilesystem( data, "flat-100.jar", 100, "f" );
        prepareJarFilesystem( data, "flat-1000.jar", 1000, "f" );
        prepareXmlFilesystem( data, "flat-10.xml", 10, "f" );
        prepareXmlFilesystem( data, "flat-100.xml", 100, "f" );
        prepareXmlFilesystem( data, "flat-1000.xml", 1000, "f" );
    }

}
