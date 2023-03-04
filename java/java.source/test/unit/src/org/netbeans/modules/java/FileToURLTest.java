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
package org.netbeans.modules.java;

import org.netbeans.junit.NbTestCase;

import java.io.File;
import java.net.URI;
import java.net.MalformedURLException;
import org.openide.util.Utilities;

/**
 * @author Rastislav Komara (<a href="mailto:moonko@netbeans.orgm">RKo</a>)
 * @todo documentation
 */
public class FileToURLTest extends NbTestCase {
    /**
     * Constructs a test case with the given name.
     *
     * @param name name of the testcase
     */
    public FileToURLTest() {
        super(FileToURLTest.class.getSimpleName());
    }

    public void testFileToURL() throws Exception{
        File file = new File("C:/DataReporter/lib/javahelp/xearhelp.jar!/Command bar.html");
        URI uri1;
        try {
            uri1 = URI.create(file.toURL().toExternalForm());
        } catch (Exception e) {
//            fail(e.getMessage());
            e.printStackTrace(getLog());
            uri1 = null;
        }
        URI uri2;
        try {
            uri2 = URI.create(Utilities.toURI(file).toURL().toExternalForm());
        } catch (Exception e) {
//            fail(e.getMessage());
            e.printStackTrace(getLog());
            uri2 = null;
        }
        URI uri3;
        try {
            uri3 = Utilities.toURI(file);
        } catch (Exception e) {
            //            fail(e.getMessage());
            e.printStackTrace(getLog());
            uri3 = null;
        }
        print("File to URL to external form: "); println(file.toURL().toExternalForm());
        print("URI1: "); println(uri1);
        print("URI2: "); println(uri2);
        print("URI3: "); println(uri3);
//        assertEquals("URI2 and URI3 aren't same.", uri2, uri3);
//        assertEquals("URI1 and URI3 aren't same.", uri1, uri3);
//        assertEquals("URI1 and URI2 aren't same.", uri1, uri2);

    }

    private void print(Object s) {
        System.out.print(s);
    }

    private void println(Object s) {
        System.out.println(s);
    }
}
