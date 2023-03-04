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
package org.netbeans.modules.gsf.testrunner.ui.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.swing.text.Position;
import org.netbeans.modules.gsf.testrunner.ui.TestMethodFinderImpl;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.spi.project.SingleMethod;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 * API to provide a list of {@link TestMethod}s found in sources under particular test roots.
 *
 * @author Dusan Balek
 * @since 1.27
 */
public final class TestMethodFinder {

    /**
     * Provides a list of {@link TestMethod}s found in sources under particular test roots.
     *
     * @param testRoots roots to search test methods for
     * @param listener a listener to inform about later changes. The listener is held weakly.
     * @return map of test source files to tests methods found
     * @since 1.27
     */
    public static Map<FileObject, Collection<TestMethodController.TestMethod>> findTestMethods(Iterable<FileObject> testRoots, BiConsumer<FileObject, Collection<TestMethodController.TestMethod>> listener) {
        TestMethodFinderImpl.INSTANCE.addListener(listener);
        Map<FileObject, Collection<TestMethodController.TestMethod>> file2TestMethods = new HashMap<>();
        for (FileObject testRoot : testRoots) {
            try {
                FileObject cacheRoot = getCacheRoot(testRoot.toURL());
                if (cacheRoot != null) {
                    Enumeration<? extends FileObject> children = cacheRoot.getChildren(true);
                    while (children.hasMoreElements()) {
                        FileObject child = children.nextElement();
                        if (child.hasExt("tests")) { //NOI18N
                            loadTestMethods(child, file2TestMethods);
                        }
                    }
                }
            } catch (IOException ex) {}
        }
        return file2TestMethods;
    }

    private static void loadTestMethods(FileObject input, Map<FileObject, Collection<TestMethodController.TestMethod>> file2TestMethods) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(input.getInputStream(), StandardCharsets.UTF_8))) {
            FileObject fo = null;
            String className = null;
            Position classPosition = null;
            Collection<TestMethodController.TestMethod> testMethods = null;
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("url: ")) { //NOI18N
                    String url = line.substring(5);
                    fo = URLMapper.findFileObject(URI.create(url).toURL());
                    if (fo == null) {
                        return;
                    }
                    testMethods = file2TestMethods.computeIfAbsent(fo, fObj -> {
                        return new ArrayList<>();
                    });
                } else if (line.startsWith("class: ")) { //NOI18N
                    String info = line.substring(7);
                    int idx = info.lastIndexOf(':');
                    className = (idx < 0 ? info : info.substring(0, idx)).trim();
                    classPosition = idx < 0 ? null : () -> Integer.parseInt(info.substring(idx + 1));
                } else if (line.startsWith("method: ") && testMethods != null && className != null) { //NOI18N
                    String info = line.substring(8);
                    int idx = info.lastIndexOf(':');
                    String name = (idx < 0 ? info : info.substring(0, idx)).trim();
                    String[] range = idx < 0 ? new String[0] : info.substring(idx + 1).split("-");
                    Position methodStart = range.length > 0 ? () -> Integer.parseInt(range[0]) : null;
                    Position methodEnd = range.length > 1 ? () -> Integer.parseInt(range[1]) : null;
                    testMethods.add(new TestMethodController.TestMethod(className, classPosition, new SingleMethod(fo, name), methodStart, null, methodEnd));
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static FileObject getCacheRoot(URL root) throws IOException {
        final FileObject dataFolder = CacheFolder.getDataFolder(root, true);
        return dataFolder != null ? FileUtil.createFolder(dataFolder, TestMethodFinderImpl.NAME + "/" + TestMethodFinderImpl.VERSION) : null; //NOI18N
    }
}
