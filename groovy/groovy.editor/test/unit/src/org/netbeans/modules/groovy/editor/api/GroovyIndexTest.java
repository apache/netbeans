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

package org.netbeans.modules.groovy.editor.api;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.groovy.editor.api.elements.index.IndexedClass;
import org.netbeans.modules.groovy.editor.api.elements.index.IndexedMethod;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Janicek <mjanicek@netbeans.org>
 */
public class GroovyIndexTest extends GroovyTestBase {

    public GroovyIndexTest(String testName) {
        super(testName);
    }

    public void testGetMethods() throws Exception {
        GroovyIndex index = initIndex();

        Set<IndexedMethod> methods = index.getMethods("m", "demo.GroovyClass1", QuerySupport.Kind.PREFIX);
        assertEquals(3, methods.size());

        methods = index.getMethods(".*", "demo.GroovyClass1", QuerySupport.Kind.REGEXP);
        assertEquals(6, methods.size());
    }

    public void testGetClasses() throws Exception {
        GroovyIndex index = initIndex();

        Set<IndexedClass> classes = index.getAllClasses();
        assertEquals(5, classes.size());
    }

    public void testMatchCamelCase() throws Exception {
        assertTrue(GroovyIndex.matchCamelCase("RegExPars", "RegExParser", false));
        assertFalse(GroovyIndex.matchCamelCase("REGExPars", "RegExParser", false));
        assertTrue(GroovyIndex.matchCamelCase("REGExPars", "RegExParser", true));
        assertTrue(GroovyIndex.matchCamelCase("MarkerPattern", "markerPattern", true));
        assertFalse(GroovyIndex.matchCamelCase("MarkerPattern", "markerPattern", false));
        assertTrue(GroovyIndex.matchCamelCase("markerPattern", "markerPattern", false));
        assertFalse(GroovyIndex.matchCamelCase("maRKerPattern", "markerPattern", false));
        assertTrue(GroovyIndex.matchCamelCase("maRKerPattern", "markerPattern", true));
        assertFalse(GroovyIndex.matchCamelCase("markeRPattern", "markerPattern", false));
        assertFalse(GroovyIndex.matchCamelCase("markerRPattern", "markerPattern", true));
        assertTrue(GroovyIndex.matchCamelCase("markeRPattern", "markerPattern", true));
        assertTrue(GroovyIndex.matchCamelCase("kNaD", "koleckoNaDruhou", true));
        assertTrue(GroovyIndex.matchCamelCase("kNaD", "koleckoNaDruhou", false));
        assertTrue(GroovyIndex.matchCamelCase("WVSN", "WithVeryStrangeName", true));
        assertTrue(GroovyIndex.matchCamelCase("WVSN", "WithVeryStrangeName", false));
        assertFalse(GroovyIndex.matchCamelCase("WVSNE", "WithVeryStrangeName", false));
        assertTrue(GroovyIndex.matchCamelCase("WVSNE", "WithVeryStrangeName", true));
    }
    
    
    private GroovyIndex initIndex() throws Exception {
        indexFile(getTestPath());
        return GroovyIndex.get(createSourceClassPathsForTest());
    }

    private List<FileObject> createSourceClassPathsForTest() {
        List<FileObject> list = new ArrayList<FileObject>();

        File folder = new File(getDataDir(), getTestFolderPath());
        list.add(FileUtil.toFileObject(folder));

        return list;
    }

    private String getTestFolderPath() {
        return "testfiles/index/" + getTestName(); //NOI18N
    }

    private String getTestPath() {
        return getTestFolderPath() + "/" + getTestName() + ".groovy"; //NOI18N
    }

    private String getTestName() {
        String name = getName();
        int indexOf = name.indexOf("_");
        if (indexOf != -1) {
            name = name.substring(0, indexOf);
        }
        return name;
    }
}
