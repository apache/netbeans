/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
