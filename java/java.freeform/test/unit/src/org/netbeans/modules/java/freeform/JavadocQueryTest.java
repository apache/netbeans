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

package org.netbeans.modules.java.freeform;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.modules.ant.freeform.TestBase;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * Tests Javadoc reporting.
 * @author Jesse Glick
 */
public class JavadocQueryTest extends TestBase {

    public JavadocQueryTest(String name) {
        super(name);
    }

    private URL classes1Dir, classes1Jar, classes2Dir, javadoc1Dir, javadoc2Zip;
    
    protected void setUp() throws Exception {
        super.setUp();
        classes1Dir = asDir("classes1");
        classes1Jar = asJar("classes1.jar");
        classes2Dir = asDir("classes2");
        javadoc1Dir = asDir("javadoc1");
        javadoc2Zip = asJar("javadoc2.zip");
    }
    
    private URL asDir(String path) throws Exception {
        URL u = Utilities.toURI(simple2.helper().resolveFile(path)).toURL();
        String us = u.toExternalForm();
        if (us.endsWith("/")) {
            return u;
        } else {
            return new URL(us + "/");
        }
    }
    
    private URL asJar(String path) throws Exception {
        return FileUtil.getArchiveRoot(Utilities.toURI(simple2.helper().resolveFile(path)).toURL());
    }
    
    private List<URL> javadocFor(URL binary) {
        return Arrays.asList(JavadocForBinaryQuery.findJavadoc(binary).getRoots());
    }
    
    public void testFindJavadoc() throws Exception {
        List<URL> both = Arrays.asList(new URL[] {javadoc1Dir, javadoc2Zip});
        assertEquals("both Javadoc found for " + classes1Dir, both, javadocFor(classes1Dir));
        assertEquals("both Javadoc found for " + classes1Jar, both, javadocFor(classes1Jar));
        assertEquals("no Javadoc found for " + classes2Dir, Collections.EMPTY_LIST, javadocFor(classes2Dir));
    }

    // XXX testChangeFiring?
    
}
