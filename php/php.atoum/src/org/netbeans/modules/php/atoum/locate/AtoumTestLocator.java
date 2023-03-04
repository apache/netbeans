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
package org.netbeans.modules.php.atoum.locate;

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
import org.netbeans.modules.php.spi.testing.locate.Locations;
import org.netbeans.modules.php.spi.testing.locate.TestLocator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Pair;

public class AtoumTestLocator implements TestLocator {

    private final PhpModule phpModule;


    public AtoumTestLocator(PhpModule phpModule) {
        assert phpModule != null;
        this.phpModule = phpModule;
    }

    @Override
    public Set<Locations.Offset> findSources(FileObject testFile) {
        assert phpModule.getSourceDirectory() != null : "Source directory must exist";
        return find(Collections.singletonList(phpModule.getSourceDirectory()), testFile);
    }

    @Override
    public Set<Locations.Offset> findTests(FileObject testedFile) {
        assert !phpModule.getTestDirectories().isEmpty() : "Test directory must exist";
        return find(phpModule.getTestDirectories(), testedFile);
    }

    private Set<Locations.Offset> find(List<FileObject> sourceRoots, FileObject file) {
        EditorSupport editorSupport = Lookup.getDefault().lookup(EditorSupport.class);
        assert editorSupport != null : "Editor support must exist";

        Set<Locations.Offset> phpFiles = new TreeSet<>(new Comparator<Locations.Offset>() {
            @Override
            public int compare(Locations.Offset o1, Locations.Offset o2) {
                return o1.getFile().getPath().compareTo(o2.getFile().getPath());
            }
        });
        List<Locations.Offset> results = new ArrayList<>();
        for (PhpClass phpClass : editorSupport.getClasses(file)) {
            results.clear();
            // prefer FQN
            for (FileObject sourceRoot : sourceRoots) {
                Collection<Pair<FileObject, Integer>> files = editorSupport.filesForClass(sourceRoot, phpClass);
                results.addAll(filterPhpFiles(sourceRoot, files));
            }
            if (!results.isEmpty()) {
                phpFiles.addAll(results);
                continue;
            }
            // #221816 - search only by class name
            for (FileObject sourceRoot : sourceRoots) {
                Collection<Pair<FileObject, Integer>> files = editorSupport.filesForClass(sourceRoot, new PhpClass(phpClass.getName(), null, -1));
                results = filterPhpFiles(sourceRoot, files);
                phpFiles.addAll(results);
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
