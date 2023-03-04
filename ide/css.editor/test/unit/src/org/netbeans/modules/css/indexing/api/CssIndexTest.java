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

package org.netbeans.modules.css.indexing.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.editor.ProjectTestBase;
import org.netbeans.modules.css.refactoring.api.RefactoringElementType;
import org.openide.filesystems.FileObject;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static junit.framework.TestCase.assertNotNull;

/**
 *
 * @author mfukala@netbeans.org
 */
public class CssIndexTest extends ProjectTestBase {

    public CssIndexTest(String name) {
        super(name, "testProject");
    }

    public void testEncodeValueForRegexp() {
        assertEquals("", CssIndex.encodeValueForRegexp(""));
        assertEquals("a", CssIndex.encodeValueForRegexp("a"));
        assertEquals("\\\\", CssIndex.encodeValueForRegexp("\\"));
        assertEquals("a\\*b", CssIndex.encodeValueForRegexp("a*b"));
        assertEquals("a\\\\b", CssIndex.encodeValueForRegexp("a\\b"));
        assertEquals("\\.\\^\\$\\[\\]\\{\\}\\(\\)", CssIndex.encodeValueForRegexp(".^$[]{}()"));
    }

    public void testCreateImpliedFileName() {
        //no change
        assertEquals("index.scss", CssIndex.createImpliedFileName("index.scss", null, false));

        //imply underscope, keep ext
        assertEquals("_index.scss", CssIndex.createImpliedFileName("index.scss", null, true));

        //add just ext
        assertEquals("index.scss", CssIndex.createImpliedFileName("index", "scss", false));

        //mix
        assertEquals("_index.scss", CssIndex.createImpliedFileName("index", "scss", true));
        assertEquals("folder/index.scss", CssIndex.createImpliedFileName("folder/index", "scss", false));
        assertEquals("folder/_index.scss", CssIndex.createImpliedFileName("folder/index", "scss", true));
        assertEquals("folder1/folder2/index.scss", CssIndex.createImpliedFileName("folder1/folder2/index", "scss", false));
        assertEquals("folder1/folder2/_index.scss", CssIndex.createImpliedFileName("folder1/folder2/index", "scss", true));
        assertEquals("/folder/_index.scss", CssIndex.createImpliedFileName("/folder/index", "scss", true));
        assertEquals("/_index.scss", CssIndex.createImpliedFileName("/index", "scss", true));

        //extension exists but not scss or sass
        assertEquals("_pa.rtial.scss", CssIndex.createImpliedFileName("pa.rtial", "scss", true));
        assertEquals("folder/_pa.rtial.scss", CssIndex.createImpliedFileName("folder/pa.rtial", "scss", true));
        assertEquals("folder/_pa.rtial.scss", CssIndex.createImpliedFileName("folder/pa.rtial.scss", null, true));
        assertEquals("folder/pa.rtial", CssIndex.createImpliedFileName("folder/pa.rtial", null, false));
    }

    private CssIndex getTestCssIndex() throws IOException {
        FileObject test1Css = getTestFile(getSourcesFolderName() + "/test1.css");
        Project project = FileOwnerQuery.getOwner(test1Css);
        assertNotNull(project);

        CssIndex index = CssIndex.create(project);
        assertNotNull(index);

        return index;
    }

    public void testFindByTypeAndName() throws IOException {
        FileObject test2Css = getTestFile(getSourcesFolderName() + "/test2.css");

        Collection<FileObject> fileObjects = getTestCssIndex().find(RefactoringElementType.ID, "dummyId3");

        assertNotNull(fileObjects);
        assertEquals(1, fileObjects.size());
        assertEquals(test2Css, fileObjects.iterator().next());
    }

    public void testFindAll() throws IOException {
        FileObject test1Css = getTestFile(getSourcesFolderName() + "/test1.css");
        FileObject test2Css = getTestFile(getSourcesFolderName() + "/test2.css");
        FileObject testHtml = getTestFile(getSourcesFolderName() + "/test.html");

        Map<FileObject,Collection<String>> classIndex = getTestCssIndex().findAll(RefactoringElementType.CLASS);

        assertEquals(
            new HashSet<>(asList("classSelector1", "classSelector2")),
            new HashSet<>(classIndex.get(test1Css)));
        assertEquals(
            new HashSet<>(asList("anotherClassSelector", "classSelector3")),
            new HashSet<>(classIndex.get(test2Css)));
        assertEquals(
            new HashSet<>(asList("classSelector1", "classSelector3")),
            new HashSet<>(classIndex.get(testHtml)));

        Map<FileObject,Collection<String>> idIndex = getTestCssIndex().findAll(RefactoringElementType.ID);

        assertEquals(
            new HashSet<>(asList("anotherId", "dummyId")),
            new HashSet<>(idIndex.get(test1Css)));
        assertEquals(
            new HashSet<>(asList("dummyId2", "dummyId3", "yetAnotherId")),
            new HashSet<>(idIndex.get(test2Css)));
        assertEquals(
            new HashSet<>(asList("yetAnotherId")),
            new HashSet<>(idIndex.get(testHtml)));
    }

    public void testFindAllClassDeclarations() throws IOException {
        FileObject test1Css = getTestFile(getSourcesFolderName() + "/test1.css");
        FileObject test2Css = getTestFile(getSourcesFolderName() + "/test2.css");
        Set<FileObject> testCssFiles = new HashSet<>(asList(test1Css, test2Css));

        Map<FileObject,Collection<String>> allClassDeclarations = getTestCssIndex().findAllClassDeclarations();
        assertNotNull(allClassDeclarations);
        assertEquals(2, allClassDeclarations.size());
        assertEquals(testCssFiles, allClassDeclarations.keySet());

        assertEquals(new HashSet<>(asList("classSelector1", "classSelector2")), allClassDeclarations.get(test1Css));
        assertEquals(new HashSet<>(asList("anotherClassSelector","classSelector3")), allClassDeclarations.get(test2Css));
    }

    public void testFindAllIdDeclarations() throws IOException {
        FileObject test1Css = getTestFile(getSourcesFolderName() + "/test1.css");
        FileObject test2Css = getTestFile(getSourcesFolderName() + "/test2.css");
        Set<FileObject> testCssFiles = new HashSet<>(asList(test1Css, test2Css));

        Map<FileObject,Collection<String>> allIdDeclarations = getTestCssIndex().findAllIdDeclarations();
        assertNotNull(allIdDeclarations);
        assertEquals(2, allIdDeclarations.size());
        assertEquals(testCssFiles, allIdDeclarations.keySet());

        assertEquals(new HashSet<>(asList("dummyId", "anotherId")), allIdDeclarations.get(test1Css));
        assertEquals(new HashSet<>(asList("dummyId2", "dummyId3", "yetAnotherId")), allIdDeclarations.get(test2Css));
    }

    public void testFindByPrefix() throws IOException {
        FileObject test1Css = getTestFile(getSourcesFolderName() + "/test1.css");
        FileObject test2Css = getTestFile(getSourcesFolderName() + "/test2.css");
        FileObject testHtml = getTestFile(getSourcesFolderName() + "/test.html");
        Set<FileObject> testCssFiles = new HashSet<>(asList(test1Css, test2Css, testHtml));

        Map<FileObject,Collection<String>> idPrefixResult = getTestCssIndex().findByPrefix(RefactoringElementType.ID, "another");
        assertNotNull(idPrefixResult);
        assertEquals(1, idPrefixResult.size());
        assertEquals(singleton(test1Css), idPrefixResult.keySet());

        assertEquals(new HashSet<>(asList("anotherId")), idPrefixResult.get(test1Css));

        Map<FileObject,Collection<String>> classPrefixResult = getTestCssIndex().findByPrefix(RefactoringElementType.CLASS, "classSelector");
        assertNotNull(classPrefixResult);
        assertEquals(3, classPrefixResult.size());
        assertEquals(testCssFiles, classPrefixResult.keySet());

        assertEquals(new HashSet<>(asList("classSelector1", "classSelector2")), classPrefixResult.get(test1Css));
        assertEquals(new HashSet<>(asList("classSelector3")), classPrefixResult.get(test2Css));
    }

    public void testFindClasses() throws IOException {
        FileObject test1Css = getTestFile(getSourcesFolderName() + "/test1.css");
        List<FileObject> result = new ArrayList<>(getTestCssIndex().findClasses("classSelector2"));
        assertEquals(asList(test1Css), result);
    }

    public void testFindClassDeclaration() throws IOException {
        FileObject test1Css = getTestFile(getSourcesFolderName() + "/test1.css");
        List<FileObject> result = new ArrayList<>(getTestCssIndex().findClassDeclarations("classSelector2"));
        assertEquals(asList(test1Css), result);
    }

    public void testFindClassesByPrefix() throws IOException {
        FileObject test2Css = getTestFile(getSourcesFolderName() + "/test2.css");
        Map<FileObject,Collection<String>> result = getTestCssIndex().findClassesByPrefix("another");
        assertNotNull(result);
        assertEquals(singleton(test2Css), result.keySet());
        assertEquals(asList("anotherClassSelector"), new ArrayList<>(result.get(test2Css)));
    }

    public void testFindColorsByPrefix() throws IOException {
        FileObject test2Css = getTestFile(getSourcesFolderName() + "/test2.css");
        Map<FileObject,Collection<String>> result = getTestCssIndex().findColorsByPrefix("#F1");
        assertNotNull(result);
        assertEquals(singleton(test2Css), result.keySet());
        assertEquals(asList("#F1F1F1"), new ArrayList<>(result.get(test2Css)));
    }

    public void testFindColor() throws IOException {
        FileObject test2Css = getTestFile(getSourcesFolderName() + "/test2.css");
        Collection<FileObject> result = getTestCssIndex().findColor("#F1F1F1");
        assertNotNull(result);
        assertEquals(singleton(test2Css), new HashSet<>(result));
    }

    public void testFindHtmlElement() throws IOException {
        FileObject test2Css = getTestFile(getSourcesFolderName() + "/test2.css");
        Collection<FileObject> result = getTestCssIndex().findHtmlElement("h1");
        assertNotNull(result);
        assertEquals(singleton(test2Css), new HashSet<>(result));
    }

    public void testFindIds() throws IOException {
        FileObject test1Css = getTestFile(getSourcesFolderName() + "/test1.css");
        List<FileObject> result = new ArrayList<>(getTestCssIndex().findIds("anotherId"));
        assertEquals(asList(test1Css), result);
    }

    public void testFindIdDeclaration() throws IOException {
        FileObject test1Css = getTestFile(getSourcesFolderName() + "/test1.css");
        List<FileObject> result = new ArrayList<>(getTestCssIndex().findIdDeclarations("anotherId"));
        assertEquals(asList(test1Css), result);
    }

    public void testFindIdsByPrefix() throws IOException {
        FileObject test2Css = getTestFile(getSourcesFolderName() + "/test2.css");
        FileObject testHtml = getTestFile(getSourcesFolderName() + "/test.html");
        Set<FileObject> testCssFiles = new HashSet<>(asList(test2Css, testHtml));
        Map<FileObject,Collection<String>> result = getTestCssIndex().findIdsByPrefix("yetAnother");
        assertNotNull(result);
        assertEquals(testCssFiles, result.keySet());
        assertEquals(asList("yetAnotherId"), new ArrayList<>(result.get(test2Css)));
        assertEquals(asList("yetAnotherId"), new ArrayList<>(result.get(testHtml)));
    }
}