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
package org.netbeans.modules.java.openjdk.jtreg;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.swing.text.BadLocationException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodController;
import org.netbeans.modules.gsf.testrunner.ui.spi.TestMethodFinderImplementation;
import org.netbeans.modules.java.openjdk.project.SourcesImpl;
import org.netbeans.spi.project.SingleMethod;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=TestMethodFinderImplementation.class, position=0)
public class TestMethodFinderImpl implements TestMethodFinderImplementation {

    @Override
    public void addListener(BiConsumer<FileObject, Collection<TestMethodController.TestMethod>> listener) {
        //TODO
    }

    @Override
    public Map<FileObject, Collection<TestMethodController.TestMethod>> findTestMethods(FileObject testRoot) {
        Project p = FileOwnerQuery.getOwner(testRoot);

        if (p == null) {
            return null;
        }

        boolean isJTRegTestRoot = false;
        for (SourceGroup testGroup : ProjectUtils.getSources(p).getSourceGroups(SourcesImpl.SOURCES_TYPE_JDK_PROJECT_TESTS)) {
            if (testGroup.getRootFolder() == testRoot) { //TODO: correct comparison??
                isJTRegTestRoot = true;
                break;
            }
        }

        if (isJTRegTestRoot) {
            return null;
        }

        long start = System.currentTimeMillis();
        Map<FileObject, Collection<TestMethodController.TestMethod>> result = new HashMap<>();
        Enumeration<? extends FileObject> childrenEn = testRoot.getChildren(true);

        while (childrenEn.hasMoreElements()) {
            FileObject current = childrenEn.nextElement();

            if (!current.isData()) {
                continue;
            }

            TagParser.Result tags = TagParser.parseTags(current);
            List<Tag> testTag = tags.getName2Tag().get("test");
            if (testTag != null) {
                try {
                    String fakeClassName = relativePath2FakeClassName(FileUtil.getRelativePath(testRoot, current));
                    result.put(current, List.of(new TestMethodController.TestMethod(fakeClassName, TestClassInfoTask.createPosition(null, testTag.get(0).getTagStart()), new SingleMethod(current, "@test"), TestClassInfoTask.createPosition(null, testTag.get(0).getTagStart()), TestClassInfoTask.createPosition(null, testTag.get(0).getTagStart()), TestClassInfoTask.createPosition(null, testTag.get(0).getTagStart()))));
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        long end = System.currentTimeMillis();
        System.err.println("gathering tests for: " + testRoot + " took: " + (end - start) + "ms");
        return result;
    }
    
    public static String relativePath2FakeClassName(String relativePath) {
        return relativePath.substring(0, relativePath.length() - ".java".length()).replace('/', '.');
    }
}
