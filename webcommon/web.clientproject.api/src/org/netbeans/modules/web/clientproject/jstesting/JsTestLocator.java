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

package org.netbeans.modules.web.clientproject.jstesting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.web.clientproject.api.ProjectDirectoriesProvider;
import org.netbeans.modules.web.clientproject.api.WebClientProjectConstants;
import org.netbeans.spi.gototest.TestLocator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.lookup.ServiceProvider;

/**
 * Test locator for JS files. The current implementation simply searches for the same
 * path between Tests and Sources. Supported test file suffixes are:
 * <ul>
 * <li>spec</li>
 * <li>Spec</li>
 * <li>test</li>
 * <li>Test</li>
 * </ul>
 * <p>
 * <b>Currently, only HTML5 source groups are supported.</b>
 * <p>
 * If no test source groups are found, {@link ProjectDirectoriesProvider} is searched
 * in the project lookup (so user is able to specify folder with tests).
 * @see WebClientProjectConstants
 */
@ServiceProvider(service=TestLocator.class)
public final class JsTestLocator implements TestLocator {

    private static final Logger LOGGER = Logger.getLogger(JsTestLocator.class.getName());

    private static final String JS_MIME_TYPE = "text/javascript"; // NOI18N
    private static final String[] SUFFIXES = {
        "", // NOI18N
        "spec", // NOI18N
        "Spec", // NOI18N
        "test", // NOI18N
        "Test", // NOI18N
    };


    @Override
    public boolean appliesTo(FileObject fo) {
        Project project = findProject(fo);
        if (project == null) {
            LOGGER.log(Level.INFO, "Project was not found for file {0}", fo);
            return false;
        }
        if (getSourceGroupForSeleniumTests(project, fo) != null) {
            return false; // disabled for files under Selenium Tests Folder
        }
        return FileUtil.getMIMEType(fo, JS_MIME_TYPE, null) != null;
    }

    @Override
    public boolean asynchronous() {
        return true;
    }

    @Override
    public LocationResult findOpposite(FileObject fo, int caretOffset) {
        throw new UnsupportedOperationException("Go To Test is asynchronous");
    }

    @NbBundle.Messages({
        "# {0} - file name",
        "# {1} - file path",
        "JsTestLocator.not.found.source.both=Tested file not found for {0} in <siteroot>/{1} or <sources>/{1}.",
        "# {0} - file name",
        "# {1} - file path",
        "JsTestLocator.not.found.source.sources=Tested file not found for {0} in <sources>/{1}.",
        "# {0} - file name",
        "# {1} - file path",
        "JsTestLocator.not.found.source.siteroot=Tested file not found for {0} in <siteroot>/{1}.",
        "# {0} - file name",
        "# {1} - file path",
        "JsTestLocator.not.found.test=Test file not found for {0} in <tests>/{1}.",
    })
    @Override
    public void findOpposite(FileObject fo, int caretOffset, LocationListener callback) {
        Project project = findProject(fo);
        if (project == null) {
            LOGGER.log(Level.INFO, "Project was not found for file {0}", fo);
            foundLocation(null, fo, callback);
            return;
        }
        FileType fileType = getFileType(fo);
        if (fileType == FileType.TEST) {
            Pair<String, FileObject> source = findSource(project, fo);
            if (source == null) {
                foundLocation(null, fo, callback);
            } else if (source.second() != null) {
                foundLocation(new LocationResult(source.second(), -1), fo, callback);
            } else {
                String message;
                boolean hasSources = !getSourceGroupsForSources(project, WebClientProjectConstants.SOURCES_TYPE_HTML5).isEmpty();
                boolean hasSiteroot = !getSourceGroupsForSources(project, WebClientProjectConstants.SOURCES_TYPE_HTML5_SITE_ROOT).isEmpty();
                if (hasSources
                        && hasSiteroot) {
                    message = Bundle.JsTestLocator_not_found_source_both(fo.getNameExt(), getProperRelativePath(source.first()));
                } else if (hasSources) {
                    message = Bundle.JsTestLocator_not_found_source_sources(fo.getNameExt(), getProperRelativePath(source.first()));
                } else {
                    message = Bundle.JsTestLocator_not_found_source_siteroot(fo.getNameExt(), getProperRelativePath(source.first()));
                }
                foundLocation(new LocationResult(message), fo, callback);
            }
        } else if (fileType == FileType.TESTED) {
            Pair<String, FileObject> test = findTest(project, fo);
            if (test == null) {
                foundLocation(null, fo, callback);
            } else if (test.second() != null) {
                foundLocation(new LocationResult(test.second(), -1), fo, callback);
            } else {
                foundLocation(new LocationResult(Bundle.JsTestLocator_not_found_test(fo.getNameExt(), getProperRelativePath(test.first()))), fo, callback);
            }
        } else {
            foundLocation(null, fo, callback);
        }
    }

    private String getProperRelativePath(String relPath) {
        assert relPath != null;
        String path = relPath;
        if (!path.isEmpty()) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    @Override
    public FileType getFileType(FileObject fo) {
        Project project = findProject(fo);
        if (project == null) {
            LOGGER.log(Level.INFO, "Project was not found for file {0}", fo);
            return FileType.NEITHER;
        }
        if (getSourceGroupForTests(project, fo) != null) {
            return FileType.TEST;
        }
        if (getSourceGroupForSources(project, fo) != null) {
            return FileType.TESTED;
        }
        return FileType.NEITHER;
    }

    @NbBundle.Messages({
        "# {0} - file name",
        "JsTestLocator.not.found=Test/Tested file not found for {0}.",
    })
    private void foundLocation(LocationResult locationResult, FileObject fo, LocationListener callback) {
        callback.foundLocation(fo,
                locationResult != null ? locationResult : new LocationResult(Bundle.JsTestLocator_not_found(fo.getNameExt())));
    }

    @CheckForNull
    private Project findProject(FileObject file) {
        return FileOwnerQuery.getOwner(file);
    }

    private List<SourceGroup> getSourceGroupsForSources(Project project) {
        return getSourceGroupsForSources(project, null);
    }

    private List<SourceGroup> getSourceGroupsForSources(Project project, String type) {
        Sources sources = ProjectUtils.getSources(project);
        List<SourceGroup> sourceGroups = new ArrayList<>();
        if (type == null) {
            sourceGroups.addAll(Arrays.asList(sources.getSourceGroups(WebClientProjectConstants.SOURCES_TYPE_HTML5)));
            sourceGroups.addAll(Arrays.asList(sources.getSourceGroups(WebClientProjectConstants.SOURCES_TYPE_HTML5_SITE_ROOT)));
        } else {
            sourceGroups.addAll(Arrays.asList(sources.getSourceGroups(type)));
        }
        return sourceGroups;
    }

    private List<SourceGroup> getSourceGroupsForTests(Project project) {
        return Arrays.asList(ProjectUtils.getSources(project).getSourceGroups(WebClientProjectConstants.SOURCES_TYPE_HTML5_TEST));
    }

    @CheckForNull
    private SourceGroup getSourceGroupForSources(Project project, FileObject file) {
        assert project != null;
        assert file != null;
        for (SourceGroup sourceGroup : getSourceGroupsForSources(project)) {
            if (sourceGroup.contains(file)) {
                return sourceGroup;
            }
        }
        return null;
    }

    @CheckForNull
    private SourceGroup getSourceGroupForTests(Project project, FileObject file) {
        assert project != null;
        assert file != null;
        for (SourceGroup sourceGroup : getSourceGroupsForTests(project)) {
            if (sourceGroup.contains(file)) {
                return sourceGroup;
            }
        }
        return null;
    }

    private List<SourceGroup> getSourceGroupsForSeleniumTests(Project project) {
        return Arrays.asList(ProjectUtils.getSources(project).getSourceGroups(WebClientProjectConstants.SOURCES_TYPE_HTML5_TEST_SELENIUM));
    }

    @CheckForNull
    private SourceGroup getSourceGroupForSeleniumTests(Project project, FileObject file) {
        assert project != null;
        assert file != null;
        for (SourceGroup sourceGroup : getSourceGroupsForSeleniumTests(project)) {
            if (sourceGroup.contains(file)) {
                return sourceGroup;
            }
        }
        return null;
    }

    @CheckForNull
    private Pair<String, FileObject> findSource(Project project, FileObject fo) {
        List<SourceGroup> sourceGroups = getSourceGroupsForSources(project);
        if (sourceGroups.isEmpty()) {
            return null;
        }
        SourceGroup testGroup = getSourceGroupForTests(project, fo);
        assert testGroup != null : project;
        FileObject tests = testGroup.getRootFolder();
        FileObject parent = fo.getParent();
        assert parent != null : fo;
        String parentRelativePath = FileUtil.getRelativePath(tests, parent);
        assert parentRelativePath != null : tests + " must be parent of " + parent;
        if (!parentRelativePath.isEmpty()) {
            parentRelativePath += "/"; // NOI18N
        }
        for (SourceGroup sourceGroup : sourceGroups) {
            FileObject sources = sourceGroup.getRootFolder();
            for (String suffix : SUFFIXES) {
                String name = fo.getName();
                if (!suffix.isEmpty()
                        && name.endsWith(suffix)) {
                    name = name.substring(0, name.length() - suffix.length());
                }
                String relPath = parentRelativePath + name + "." + fo.getExt(); // NOI18N
                FileObject source = sources.getFileObject(relPath);
                if (source != null
                        && source.isData()) {
                    return Pair.of(parentRelativePath, source);
                }
            }
        }
        return Pair.of(parentRelativePath, null);
    }

    @CheckForNull
    private Pair<String, FileObject> findTest(Project project, FileObject fo) {
        List<SourceGroup> testGroups = getSourceGroupsForTests(project);
        if (testGroups.isEmpty()) {
            // no tests -> try to use ProjectDirectoriesProvider to be able to select test folder
            ProjectDirectoriesProvider directoriesProvider = project.getLookup().lookup(ProjectDirectoriesProvider.class);
            if (directoriesProvider == null) {
                return null;
            }
            FileObject testDirectory = directoriesProvider.getTestDirectory(true);
            if (testDirectory == null) {
                return null;
            }
            testGroups = getSourceGroupsForTests(project);
            if (testGroups.isEmpty()) {
                // not html5 project -> not supported now, sorry
                return null;
            }
        }
        SourceGroup sourceGroup = getSourceGroupForSources(project, fo);
        assert sourceGroup != null : project;
        FileObject sources = sourceGroup.getRootFolder();
        FileObject parent = fo.getParent();
        assert parent != null : fo;
        String parentRelativePath = FileUtil.getRelativePath(sources, parent);
        assert parentRelativePath != null : sources + " must be parent of " + parent;
        if (!parentRelativePath.isEmpty()) {
            parentRelativePath += "/"; // NOI18N
        }
        for (SourceGroup testGroup : testGroups) {
            FileObject tests = testGroup.getRootFolder();
            for (String suffix : SUFFIXES) {
                String relPath = parentRelativePath + fo.getName() + suffix + "." + fo.getExt(); // NOI18N
                FileObject test = tests.getFileObject(relPath);
                if (test != null
                        && test.isData()) {
                    return Pair.of(parentRelativePath, test);
                }
            }
        }
        return Pair.of(parentRelativePath, null);
    }

}
