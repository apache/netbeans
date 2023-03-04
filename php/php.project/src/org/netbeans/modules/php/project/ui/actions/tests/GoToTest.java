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

package org.netbeans.modules.php.project.ui.actions.tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.PhpProjectValidator;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
import org.netbeans.modules.php.project.ui.customizer.CompositePanelProviderImpl;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.netbeans.modules.php.spi.testing.locate.Locations;
import org.netbeans.modules.php.spi.testing.PhpTestingProvider;
import org.netbeans.spi.gototest.TestLocator;
import org.openide.filesystems.FileObject;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Support for PHP Unit.
 * @author Tomas Mysik
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.gototest.TestLocator.class)
public class GoToTest implements TestLocator {
    private static final Logger LOGGER = Logger.getLogger(GoToTest.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(GoToTest.class.getName(), 2);

    private static class FileObjectComparator implements Comparator<FileObject> {
        @Override
        public int compare(FileObject fo1, FileObject fo2) {
            return fo1.getPath().compareTo(fo2.getPath());
        }
    }
    static final Comparator<FileObject> FILE_OBJECT_COMAPARTOR = new FileObjectComparator();

    public GoToTest() {
    }

    @Override
    public boolean appliesTo(FileObject fo) {
        return FileUtils.isPhpFile(fo);
    }

    @Override
    public boolean asynchronous() {
        return true;
    }

    @Override
    public void findOpposite(final FileObject fo, final int caretOffset, final LocationListener callback) {
        RP.post(new Runnable() {
            @Override
            public void run() {
                callback.foundLocation(fo, findOpposite0(fo));
            }
        });
    }

    @Override
    public LocationResult findOpposite(FileObject fo, int caretOffset) {
        throw new UnsupportedOperationException("Go To Test is asynchronous");
    }

    private LocationResult findOpposite0(FileObject fo) {
        PhpProject project = PhpProjectUtils.getPhpProject(fo);
        if (project == null) {
            // XXX what to do now??
            LOGGER.log(Level.INFO, "PHP project was not found for file {0}", fo);
            return null;
        }
        if (PhpProjectValidator.isFatallyBroken(project)) {
            UiUtils.warnBrokenProject(project.getPhpModule());
            return null;
        }

        if (CommandUtils.isUnderTests(project, fo, false)) {
            return findSource(project, fo);
        } else if (CommandUtils.isUnderSources(project, fo)) {
            return findTest(project, fo);
        }
        return null;
    }

    @Override
    public FileType getFileType(FileObject fo) {
        PhpProject project = PhpProjectUtils.getPhpProject(fo);
        if (project == null || PhpProjectValidator.isFatallyBroken(project)) {
            LOGGER.log(Level.INFO, "PHP project was not found for file {0}", fo);
            return FileType.NEITHER;
        }

        if (CommandUtils.isUnderTests(project, fo, false)) {
            List<PhpTestingProvider> testingProviders = project.getTestingProviders();
            if (testingProviders.isEmpty()) {
                // return test file type => customizer will be opened on action
                return FileType.TEST;
            }
            PhpModule phpModule = project.getPhpModule();
            for (PhpTestingProvider testingProvider : testingProviders) {
                if (testingProvider.isTestFile(phpModule, fo)) {
                    return FileType.TEST;
                }
            }
        } else if (CommandUtils.isUnderSources(project, fo)) {
            return FileType.TESTED;
        }
        return FileType.NEITHER;
    }

    private LocationResult findSource(PhpProject project, FileObject testFo) {
        return findFile(project, testFo, false);
    }

    public static LocationResult findTest(PhpProject project, FileObject srcFo) {
        return findFile(project, srcFo, true);
    }

    private static LocationResult findFile(PhpProject project, FileObject file, boolean searchTest) {
        List<FileObject> sourceRoots = searchTest ? getTests(project) : getSources(project);
        if (sourceRoots.isEmpty()) {
            return null;
        }
        List<PhpTestingProvider> testingProviders = project.getTestingProviders();
        if (testingProviders.isEmpty()) {
            PhpProjectUtils.openCustomizer(project, CompositePanelProviderImpl.TESTING);
            return null;
        }
        Map<FileObject, Locations.Offset> phpFiles = new HashMap<>();
        PhpModule phpModule = project.getPhpModule();
        for (PhpTestingProvider testingProvider : testingProviders) {
            org.netbeans.modules.php.spi.testing.locate.TestLocator testLocator = testingProvider.getTestLocator(phpModule);
            Set<Locations.Offset> result;
            if (searchTest) {
                result = testLocator.findTests(file);
            } else {
                result = testLocator.findSources(file);
            }
            for (Locations.Offset offset : result) {
                FileObject fo = offset.getFile();
                if (phpFiles.get(fo) == null) {
                    phpFiles.put(fo, offset);
                }
            }
        }
        if (phpFiles.isEmpty()) {
            return new LocationResult(NbBundle.getMessage(GoToTest.class, searchTest ? "MSG_TestNotFound" : "MSG_SrcNotFound", file.getNameExt()));
        }
        if (phpFiles.size() == 1) {
            Locations.Offset source = phpFiles.values().iterator().next();
            return new LocationResult(source.getFile(), source.getOffset());
        }
        List<FileObject> files = new ArrayList<>(phpFiles.size());
        for (Locations.Offset location : phpFiles.values()) {
            files.add(location.getFile());
        }
        files.sort(FILE_OBJECT_COMAPARTOR);
        final List<FileObject> sourceRootsCopy = new CopyOnWriteArrayList<>(sourceRoots);
        final List<FileObject> filesCopy = new CopyOnWriteArrayList<>(files);
        FileObject selected = Mutex.EVENT.readAccess(new Mutex.Action<FileObject>() {
            @Override
            public FileObject run() {
                return SelectFilePanel.open(sourceRootsCopy, filesCopy);
            }
        });
        if (selected != null) {
            int offset = -1;
            for (Locations.Offset location : phpFiles.values()) {
                if (selected.equals(location.getFile())) {
                    offset = location.getOffset();
                    break;
                }
            }
            return new LocationResult(selected, offset);
        }
        return null;
    }

    private static List<FileObject> getSources(PhpProject project) {
        FileObject sources = ProjectPropertiesSupport.getSourcesDirectory(project);
        assert sources != null : "Project sources must be found for " + project;
        return Collections.singletonList(sources);
    }

    private static List<FileObject> getTests(PhpProject project) {
        return ProjectPropertiesSupport.getTestDirectories(project, false);
    }

}
