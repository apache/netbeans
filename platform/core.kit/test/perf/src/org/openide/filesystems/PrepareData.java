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
