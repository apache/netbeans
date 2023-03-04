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
package org.openide.filesystems.xmlfs;

import java.io.*;
import java.net.URLClassLoader;
import java.net.URL;

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.filesystems.localfs.LocalFSTest;
import org.openide.filesystems.data.SerialData;

/**
 * Tests an XMLFileSystem zipped in a jar.
 */
public class XMLinJarFSTest extends XMLFSTest {

    URLClassLoader cloader;

    /** Creates new XMLFSGenerator */
    public XMLinJarFSTest(String name) {
        super(name);
    }

    /** Set up given number of FileObjects */
    protected FileObject[] setUpFileObjects(int foCount) throws Exception {
        tmp = createTempFolder();
        destFolder = LocalFSTest.createFiles(foCount, 0, tmp);
        File xmlbase = generateXMLFile(destFolder, new ResourceComposer(LocalFSTest.RES_NAME, LocalFSTest.RES_EXT, foCount, 0));
        File jar = Utilities.createJar(tmp, "jarxmlfs.jar");
        cloader = new URLClassLoader(new URL[] { jar.toURL() });
        URL res = cloader.findResource(PACKAGE + xmlbase.getName());
        xmlfs = new XMLFileSystem();
        xmlfs.setXmlUrl(res, false);

        FileObject pkg = xmlfs.findResource(PACKAGE);
        return pkg.getChildren();
    }

    /*
    public static void main(String[] args) throws Exception {
        XMLinJarFSTest a = new XMLinJarFSTest("test");
        FileObject[] fos = a.setUpFileObjects(78);
        for (int i = 0; i < fos.length; i++) {
            System.out.println(fos[i]);
        }
    }
     */
}
