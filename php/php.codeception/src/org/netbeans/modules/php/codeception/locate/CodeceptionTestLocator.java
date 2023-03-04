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
package org.netbeans.modules.php.codeception.locate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.modules.php.api.editor.EditorSupport;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.codeception.commands.Codecept;
import org.netbeans.modules.php.spi.testing.locate.Locations;
import org.netbeans.modules.php.spi.testing.locate.TestLocator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Pair;

public final class CodeceptionTestLocator implements TestLocator {

    private final PhpModule phpModule;


    public CodeceptionTestLocator(PhpModule phpModule) {
        assert phpModule != null;
        this.phpModule = phpModule;
    }

    @Override
    public Set<Locations.Offset> findSources(FileObject testFile) {
        assert phpModule.getSourceDirectory() != null : "Source directory must exist";
        return find(Collections.singletonList(phpModule.getSourceDirectory()), testFile, false);
    }

    @Override
    public Set<Locations.Offset> findTests(FileObject testedFile) {
        assert !phpModule.getTestDirectories().isEmpty() : "Test directories must exist";
        return find(phpModule.getTestDirectories(), testedFile, true);
    }

    private Set<Locations.Offset> find(List<FileObject> sourceRoots, FileObject file, boolean searchTest) {
        EditorSupport editorSupport = Lookup.getDefault().lookup(EditorSupport.class);
        assert editorSupport != null : "Editor support must exist";

        Set<Locations.Offset> phpFiles = new TreeSet<>(new Comparator<Locations.Offset>() {
            @Override
            public int compare(Locations.Offset o1, Locations.Offset o2) {
                return o1.getFile().getPath().compareTo(o2.getFile().getPath());
            }
        });
        for (PhpClass phpClass : editorSupport.getClasses(file)) {
            //        name,   FQ name
            List<Pair<String, String>> classes = new ArrayList<>();
            if (searchTest) {
                // FooTest
                classes.add(Pair.of(Codecept.makeTestClass(phpClass.getName()), Codecept.makeTestClass(phpClass.getFullyQualifiedName())));
                // FooCest
                classes.add(Pair.of(Codecept.makeCestClass(phpClass.getName()), Codecept.makeCestClass(phpClass.getFullyQualifiedName())));
                // FooCept
                classes.add(Pair.of(Codecept.makeCeptClass(phpClass.getName()), Codecept.makeCeptClass(phpClass.getFullyQualifiedName())));
            } else {
                if (!Codecept.isCodeceptionTestClass(phpClass.getName())) {
                    continue;
                }
                String fullyQualifiedName = phpClass.getFullyQualifiedName();
                assert fullyQualifiedName != null : "No FQN for php class: " + phpClass.getName();
                classes.add(Pair.of(Codecept.getTestedClass(phpClass.getName()), Codecept.getTestedClass(fullyQualifiedName)));
            }

            List<Locations.Offset> results = new ArrayList<>();
            for (Pair<String, String> namePair : classes) {
                results.clear();
                // prefer FQN
                for (FileObject sourceRoot : sourceRoots) {
                    Collection<Pair<FileObject, Integer>> files = editorSupport.filesForClass(sourceRoot, new PhpClass(namePair.first(), namePair.second(), -1));
                    results.addAll(filterPhpFiles(sourceRoot, files));
                }
                if (!results.isEmpty()) {
                    phpFiles.addAll(results);
                    continue;
                }

                // search only by class name
                for (FileObject sourceRoot : sourceRoots) {
                    Collection<Pair<FileObject, Integer>> files = editorSupport.filesForClass(sourceRoot, new PhpClass(namePair.first(), null, -1));
                    results = filterPhpFiles(sourceRoot, files);
                    phpFiles.addAll(results);
                }
            }
        }
        return phpFiles;
    }

    private List<Locations.Offset> filterPhpFiles(FileObject sourceRoot, Collection<Pair<FileObject, Integer>> files) {
        List<Locations.Offset> results = new ArrayList<>(files.size());
        for (Pair<FileObject, Integer> pair : files) {
            FileObject fileObject = pair.first();
            if (FileUtils.isPhpFile(fileObject)
                    && FileUtil.isParentOf(sourceRoot, fileObject)) {
                results.add(new Locations.Offset(fileObject, pair.second()));
            }
        }
        return results;
    }

}
