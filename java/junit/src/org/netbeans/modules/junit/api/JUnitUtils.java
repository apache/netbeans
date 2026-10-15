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

package org.netbeans.modules.junit.api;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin;
import org.netbeans.modules.junit.DefaultPlugin;
import org.netbeans.modules.junit.plugin.JUnitPlugin;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.util.Utilities;

/**
 *
 * @author  Marian Petras
 */
public final class JUnitUtils {

    /** */
    private final Project project;
    /** <!-- PENDING --> */
    private boolean sourceGroupsOnly = true;
    /** <!-- PENDING --> */
    private SourceGroup[] javaSourceGroups;
    /** <!-- PENDING --> */
    private Map<SourceGroup,Object[]> sourcesToTestsMap;
    /** <!-- PENDING --> */
    private Map<FileObject,Object> foldersToSourceGroupsMap;
    
    // PENDING - should not be hard-coded:
    public static final String TESTS_ROOT_NAME = "test";               //NOI18N
    
    /**
     * <!-- PENDING -->
     */
    public JUnitUtils(Project project) {
        this.project = project;
    }
    
    /** <!-- PENDING --> */
    static FileObject findTestsRoot(Project project) {
        final SourceGroup[] sourceGroups
                = new JUnitUtils(project).getJavaSourceGroups();
        for (int i = 0; i < sourceGroups.length; i++) {
            FileObject root = sourceGroups[i].getRootFolder();
            if (root.getName().equals(TESTS_ROOT_NAME)) {
                return root;
            }
        }
        return null;
    }
    
    /** */
    static FileObject getPackageFolder(
            FileObject root,
            String pkgName) throws IOException {
        String relativePathName = pkgName.replace('.', '/');
        FileObject folder = root.getFileObject(relativePathName);
        if (folder == null) {
            folder = FileUtil.createFolder(root, relativePathName);
        }
        return folder;
    }
    
    public static void logJUnitUsage(URI projectURI) {
        DefaultPlugin.logJUnitUsage(projectURI);
    }
    
    public static boolean isInstanceOfDefaultPlugin(JUnitPlugin plugin) {
        if (plugin.getClass() != DefaultPlugin.class) {
            return true;
        }
        return false;
    }
    
    public static DataObject createSuiteTest(FileObject targetRootFolder, FileObject targetFolder, String suiteName, Map<CommonPlugin.CreateTestParam, Object> params) {
        DefaultPlugin defaultPlugin = new DefaultPlugin(null);
        
        if (!defaultPlugin.setupJUnitVersionByProject(targetFolder)) {
            return null;
        }

        /* create test class(es) for the selected source class: */
        DataObject suite = defaultPlugin.createSuiteTest(
                targetRootFolder,
                targetFolder,
                suiteName,
                JUnitTestUtil.getSettingsMap(true));
        return suite;
    }
    
    /**
     * Identifies and collects <code>SourceGroup</code>s and folders
     * of a given project which may serve as target folders for newly created
     * test classes.
     *
     * @param  project  project whose folders are to be checked
     * @param  sourceGroupsOnly  return only <code>SourceGroup</code>s
     *                           - ignore target folders not having
     *                           a corresponding <code>SourceGroup</code>
     * @return  collection which may contain <code>FileObject</code>s
     *          or <code>SourceGroup</code>s (or both);
     *          it may be empty but not <code>null</code>
     * @author  Marian Petras
     */
    public static Collection getTestTargets(Project project,
                                     final boolean sourceGroupsOnly) {
        final JUnitUtils utils = new JUnitUtils(project);
        return utils.getTestTargets(sourceGroupsOnly);
    }

    /**
     * Identifies and collects root test folders of a given project.
     * 
     * @param  project  project whose test folders are to be found
     * @return  collection of found {@code FileObject}s, possibly empty
     *          (but not {@code null})
     */
    public static Collection<FileObject> getTestFolders(Project project) {
        return new JUnitUtils(project).getTestFolders();
    }
    
    /**
     * Builds a map that containing relation between <code>SourceGroup</code>s
     * and their respective test <code>SourceGroup</code>s.
     * Each entry of the map contains a <code>SourceGroup</code> as a key
     * and an array of test <code>SourceGroup</code>s returned by
     * <code>UnitTestForSourceQuery</code> for that <code>SourceGroup</code>
     * as a value. <code>SourceGroup</code>s that have no test
     * <code>SourceGroup</code>s assigned are omitted, i.e. the resulting
     * map does not contain entries that would have empty arrays as their
     * values.
     *
     * @param  project  project whose <code>SourceGroup</code>s are to be
     *                  checked
     * @param  sourceGroupsOnly  return only <code>SourceGroup</code>s
     *                           - ignore test folders not having
     *                           a corresponding <code>SourceGroup</code>
     * @return  created map - may be empty, may be unmodifiable
     *                        never <code>null</code>
     */
    static Map getSourcesToTestsMap(Project project,
                                    final boolean sourceGroupsOnly) {
        final JUnitUtils utils = new JUnitUtils(project);
        return utils.getSourcesToTestsMap(sourceGroupsOnly);
    }
    
    /**
     * <!-- PENDING -->
     */
    public Project getProject() {
        return project;
    }

    /**
     * Identifies and collects root test folders of the project.
     * 
     * @return  collection of found {@code FileObject}s
     *          - may be empty but never {@code null}
     */
    private Collection<FileObject> getTestFolders() {
        
        /*
         * Idea:
         * 1) Get all SourceGroups
         * 2) For each SourceGroup, ask UnitTestForSourceQuery for its related
         *    test SourceGroups
         */

        /* .) get all SourceGroups: */
        final SourceGroup[] sourceGroups = getJavaSourceGroups();
        if (sourceGroups.length == 0) {
            return Collections.<FileObject>emptyList();
        }

        List<FileObject> result = null;

        /* .) for each SourceGroup, ask for test root folders: */
        for (SourceGroup sourceGroup : sourceGroups) {
            FileObject srcFolder = sourceGroup.getRootFolder();
            FileObject[] tstFoldersRaw = getTestFoldersRaw(srcFolder);
            if (tstFoldersRaw.length == 0) {
                continue;
            }

            for (FileObject tstFolder : tstFoldersRaw) {
                if (tstFolder == null) {
                    continue;
                }

                if (result == null) {
                    result = new ArrayList<FileObject>(2);
                }
                if (!result.contains(tstFolder)) {
                    result.add(tstFolder);
                }
            }
        }

        /* .) pack the results: */
        if (result == null) {
            return Collections.<FileObject>emptyList();
        } else {
            assert !result.isEmpty();
            return (result.size() == 1)
                   ? Collections.<FileObject>singleton(result.get(0))
                   : result;
        }
    }

    /**
     * Identifies and collects <code>SourceGroup</code>s and folders
     * which may serve as target folders for newly created test classes.
     *
     * @param  sourceGroupsOnly  return only <code>SourceGroup</code>s
     *                           (skip target folders without matching
     *                           <code>SourceGroup</code>)
     * @return  collection which may contain <code>FileObject</code>s
     *          or <code>SourceGroup</code>s (or both);
     *          it may be empty but not <code>null</code>
     * @see  #getTestTargets(Project, boolean)
     * @author  Marian Petras
     */
    private Collection<Object> getTestTargets(final boolean sourceGroupsOnly) {

        /*
         * Idea:
         * 1) Get all SourceGroups
         * 2) For each SourceGroup, ask UnitTestForSourceQuery for its related
         *    test SourceGroups
         *
         * Union of all SourceGroups returned by UnitTestForSourceQuery
         * are the test SourceGroups.
         */

        /* .) get all SourceGroups: */
        final SourceGroup[] sourceGroups = getJavaSourceGroups();
        if (sourceGroups.length == 0) {
            return Collections.<Object>emptyList();
        }

        /* .) */
        createFoldersToSourceGroupsMap(sourceGroups);
        Object testTargetsUnion[] = new Object[sourceGroups.length];
        int size = 0;
        for (int i = 0; i < sourceGroups.length; i++) {
            Object[] testTargets = getTestTargets(sourceGroups[i],
                                                  sourceGroupsOnly);
            size = merge(testTargets, testTargetsUnion, size);
        }

        if (size != testTargetsUnion.length) {
            testTargetsUnion = JUnitTestUtil.skipNulls(testTargetsUnion, new Object[0]);
        }

        return Collections.unmodifiableCollection(
                      Arrays.asList(testTargetsUnion));
    }
    
    /**
     * <!-- PENDING -->
     */
    Map<SourceGroup,Object[]> getSourcesToTestsMap() {
        if (sourcesToTestsMap == null) {
            sourcesToTestsMap = createSourcesToTestsMap(sourceGroupsOnly);
        }
        return sourcesToTestsMap;
    }
    
    /**
     * <!-- PENDING -->
     */
    public Map<SourceGroup,Object[]> getSourcesToTestsMap(final boolean sourceGroupsOnly) {
        if (sourceGroupsOnly != this.sourceGroupsOnly) {
            sourcesToTestsMap = null;
            this.sourceGroupsOnly = sourceGroupsOnly;
        }
        return getSourcesToTestsMap();
    }

    /**
     * Builds a map that containing relation between <code>SourceGroup</code>s
     * and their respective test <code>SourceGroup</code>s.
     * Each entry of the map contains a <code>SourceGroup</code> as a key
     * and an array of test <code>SourceGroup</code>s returned by
     * <code>UnitTestForSourceQuery</code> for that <code>SourceGroup</code>
     * as a value. <code>SourceGroup</code>s that have no test
     * <code>SourceGroup</code>s assigned are omitted, i.e. the resulting
     * map does not contain entries that would have empty arrays as their
     * values.
     *
     * @param  sourceGroupsOnly  return only <code>SourceGroup</code>s
     *                           - ignore test folders not having
     *                           a corresponding <code>SourceGroup</code>
     * @return  created map - may be empty, may be unmodifiable,
     *                        cannot be <code>null</code>
     */
    private Map<SourceGroup,Object[]> createSourcesToTestsMap(final boolean sourceGroupsOnly) {
        
        /*
         * Idea:
         * 1) Get all SourceGroups
         * 2) For each SourceGroup, ask UnitTestForSourceQuery for its related
         *    test SourceGroups
         */

        /* .) get all SourceGroups: */
        final SourceGroup[] sourceGroups = getJavaSourceGroups();
        if (sourceGroups.length == 0) {
            return Collections.<SourceGroup,Object[]>emptyMap();
        }

        /* .) get test SourceGroups for each SourceGroup: */
        createFoldersToSourceGroupsMap(sourceGroups);
        Object testTargetsUnion[] = new Object[sourceGroups.length];
        Map<SourceGroup,Object[]> map;
        map = new HashMap<SourceGroup,Object[]>(
                            (int) ((float) sourceGroups.length * 1.33f + 0.5f),
                            .75f);
        for (int i = 0; i < sourceGroups.length; i++) {
            Object[] testTargets = getTestTargets(sourceGroups[i],
                                                  sourceGroupsOnly);
            if (testTargets.length != 0) {
                map.put(sourceGroups[i], testTargets);
            }
        }
        if (map.isEmpty()) {
            return Collections.<SourceGroup,Object[]>emptyMap();
        }
        if (map.size() == 1) {
            Map.Entry<SourceGroup,Object[]> entry
                    = map.entrySet().iterator().next();
            return Collections.singletonMap(entry.getKey(), entry.getValue());
        }

        final int finalMapSize = map.size();
        if (finalMapSize >= (sourceGroups.length - 5)) {
            return map;
        }
        
        final Map<SourceGroup,Object[]> targetMap;
        targetMap = new HashMap<SourceGroup,Object[]>(
                                    (int) ((float) finalMapSize * 1.25f + .5f),
                                    .8f);
        targetMap.putAll(map);
        return targetMap;
    }

    /**
     * Merges a given set of <code>FileObject</code>s and
     * <code>SourceGroup</code>s to the given target set (which may contain
     * same types of elements).
     * The source set (array) is not modified during merge. The target
     * set (array) is not modified otherwise than by adding (overwriting
     * <code>null</code>s) elements from the source set or by replacing elements
     * with equivalent elements (i.e. pointing to the same folder). Elements are
     * always added after the last non-<code>null</code> element of the target
     * set. After the merge, it is guaranteed that all <code>null</code>
     * elements of the target array are located at the end. The above
     * constraints can only be fulfilled if parameter
     * <code>currTargetSetSize</code> is correct and if all <code>null</code>
     * elements of the target set are placed at the end of the array at the
     * moment this method is called. The target array must contain enough
     * <code>null</code> elements so that all elements to be added to the set
     * can fit.
     *
     * @param  setToAdd  elements to be added to the target set
     *                   - must not contain <code>null</code> elements
     * @param  targetSet  array to add elements to
     * @param  currTargetSetSize  current count of non-null elements in the
     *                            target set (<code>null</code> elements are
     *                            always at the end of the array)
     * @return  new size of the target set
     *          (number of non-<code>null</code> elements)
     */
    private static int merge(final Object[] setToAdd,
                             final Object[] targetSet,
                             final int currTargetSetSize) {
        if (setToAdd.length == 0) {
            return currTargetSetSize;
        }
        if (currTargetSetSize == 0) {
            System.arraycopy(setToAdd, 0, targetSet, 0, setToAdd.length);
            return setToAdd.length;
        }
        int targetSetSize = currTargetSetSize;
        toAdd:
        for (int i = 0; i < setToAdd.length; i++) {
            final Object objToAdd = setToAdd[i];
            for (int j = 0; j < targetSetSize; j++) {
                final Object chosen = chooseTarget(targetSet[j], objToAdd);
                if (chosen != null) {           //both point to the same folder
                    targetSet[j] = chosen;
                    continue toAdd;
                }
            }
            targetSet[targetSetSize++] = objToAdd;
        }
        return targetSetSize;
    }

    /**
     * Finds whether the given two objects defining a target folder are equal
     * or not and if so, suggests which one is preferred.
     * Each of the folder targets may be either a <code>SourceGroup<code>
     * object or a <code>FileObject</code> folder. If both targets point
     * to the same folder, one of them which is preferred is returned.
     * Otherwise <code>null</code> is returned.
     * <p>
     * If both targets are <code>SourceGroup</code>s, the first target is used.
     * If none of the targets is a <code>SourceGroup</code>, the first target is
     * used.
     * Otherwise (i.e. one target is a <code>SourceGroup</code>,
     * the other is not), the <code>SourceGroup</code> target is returned.
     *
     * @param  target1  one target
     * @param  target2  second target
     * @return  <code>null</code> if the two targets define different folders;
     *          or the preferred one (of the passed targets) if the two
     *          are equal
     */
    private static Object chooseTarget(Object target1, Object target2) {
        final boolean isGroup1 = target1 instanceof SourceGroup;
        final boolean isGroup2 = target2 instanceof SourceGroup;

        assert isGroup1 || (target1 instanceof FileObject);
        assert isGroup2 || (target2 instanceof FileObject);
        
        if (isGroup1 && isGroup2 && target1.equals(target2)) {
            return target1;
        }

        final FileObject folder1 = isGroup1
                                   ? ((SourceGroup) target1).getRootFolder()
                                   : ((FileObject) target1);
        final FileObject folder2 = isGroup2
                                   ? ((SourceGroup) target2).getRootFolder()
                                   : ((FileObject) target2);
        if (!(folder1.isFolder())) {
            throw new IllegalArgumentException("target1: not a folder");//NOI18N
        }
        if (!(folder2.isFolder())) {
            throw new IllegalArgumentException("target2: not a folder");//NOI18N
        }
        if (folder1.equals(folder2)) {
            return (isGroup1 == isGroup2) ? target1
                                          : (isGroup1 ? target1 : target2);
        }
        return null;
    }

    /**
     * Returns test targets for the given <code>SourceGroup</code>.
     * The test targets are folders which are searched when tests for a class
     * from the <code>SourceGroup</code> are to be found. Each of the returned
     * test targets may be either <code>SourceGroup</code> (representing
     * a folder plus additional information such as display name) or simply
     * a <code>FileObject</code> representing a folder.
     * If parameter <code>includeSourceGroups</code> is <code>false</code>,
     * only <code>SourceGroup</code>s are returned (target folders without
     * corresponding <code>SourceGroup</code>s are ignored).
     *
     * @param  sourceGroup  source group to find test targets for
     * @param  sourceGroupsOnly  skip target folders without matching
     *                           <code>SourceGroup</code>
     * @return  array which may contain <code>FileObject</code>s
     *          or <code>SourceGroup</code>s (or both);
     *          it may be empty but not <code>null</code>
     * @see  org.netbeans.modules.java.testrunner.CommonTestUtil#getFileObject2SourceGroupMap
     */
    public Object[] getTestTargets(SourceGroup sourceGroup,
                                   final boolean sourceGroupsOnly) {
        
        /* .) find test root folders: */
        final FileObject[] testFolders
                    = getTestFoldersRaw(sourceGroup.getRootFolder());
        
        if (testFolders.length == 0) {
            return new Object[0];
        }
        
        /* .) find SourceGroups corresponding to the FileObjects: */
        final Object[] targets = new Object[testFolders.length];
        for (int i = 0; i < targets.length; i++) {
            final FileObject testFolder = testFolders[i];
            if (testFolder == null) {
                continue;
            }
            Object srcGroup = foldersToSourceGroupsMap.get(testFolder);
            targets[i] = (srcGroup != null)
                         ? srcGroup
                         : sourceGroupsOnly ? null : testFolder;
        }
        return JUnitTestUtil.skipNulls(targets, new Object[0]);
    }
    
    /**
     * Returns an array of test folders corresponding to the given source
     * folder - may contain <code>null</code>s.
     *
     * @param  srcFolder  <code>FileObject</code> representing source code root,
     *                    for which test root folders should be found
     * @return  array of <code>FileObject</code>s representing test root
     *          folders, possibly with superfluous <code>null</code> elements
     * @see  #getSourceFoldersRaw
     */
    public FileObject[] getTestFoldersRaw(FileObject srcFolder) {
        return getFileObjects(UnitTestForSourceQuery.findUnitTests(srcFolder),
                              true);
    }
    
    /**
     * Returns an array of source folders corresponding to the given test
     * folder - may contain <code>null</code>s.
     *
     * @param  testFolder  <code>FileObject</code> representing source code root,
     *                    for which source root folders should be found
     * @return  array of <code>FileObject</code>s representing source root
     *          folders, possibly with superfluous <code>null</code> elements
     * @see  #getTestFoldersRaw
     */
    public FileObject[] getSourceFoldersRaw(FileObject testFolder) {
        return getFileObjects(UnitTestForSourceQuery.findSources(testFolder),
                              false);
    }
    
    /**
     * Returns <code>FileObject</code>s represented by the given URLs.
     *
     * @param  rootURLs  URLs representing <code>FileObject</code>s
     * @param  srcToTest  <code>true</code> if we are searching for test
     *                    folders, <code>false</code> if we are searching
     *                    for source folders - affects only text of warning
     *                    log messages
     * @return  array of <code>FileObject</code>s representing source root
     *          folders, possibly with superfluous <code>null</code> elements
     */
    private FileObject[] getFileObjects(final URL[] rootURLs,
                                        final boolean srcToTest) {
        if (rootURLs.length == 0) {
            return new FileObject[0];
        }
        
        FileObject[] sourceRoots = new FileObject[rootURLs.length];
        for (int i = 0; i < rootURLs.length; i++) {
            if ((sourceRoots[i] = URLMapper.findFileObject(rootURLs[i]))
                    == null) {
                final int severity = ErrorManager.INFORMATIONAL;
                if (ErrorManager.getDefault().isLoggable(severity)) {
                    ErrorManager.getDefault().log(
                            severity,
                            (srcToTest ? "Test" : "Source")             //NOI18N
                            + " directory " + rootURLs[i]               //NOI18N
                            + " declared by project "                   //NOI18N
                            + ProjectUtils.getInformation(project).getName()
                            + " does not exist.");                      //NOI18N
                }
                continue;
            }
            Project sourceRootOwner = FileOwnerQuery.getOwner(sourceRoots[i]);
            if (!project.equals(sourceRootOwner)) {
                sourceRoots[i] = null;
                
                int severity = ErrorManager.INFORMATIONAL;
                if (ErrorManager.getDefault().isNotifiable(severity)) {
                    ErrorManager.getDefault().notify(
                        severity,
                        new IllegalStateException(
                            "Malformed project: Found test root (" +    //NOI18N
                                rootURLs[i] + ')' + ' ' +
                                (sourceRootOwner == null
                                        ? "does not belong to any"      //NOI18N
                                        : "belongs to a different") +   //NOI18N
                                " project."));                          //NOI18N
                }
                continue;
            }
        }
        return sourceRoots;
    }

    /**
     */
    public static FileObject[] skipNulls(final FileObject[] fileObjs) {
        if (fileObjs.length == 0) {
            return fileObjs;
        }
        
        int nullsCount = 0;
        for (int i = 0; i < fileObjs.length; i++) {
            if (fileObjs[i] == null) {
                nullsCount++;
            }
        }
        
        if (nullsCount == 0) {
            return fileObjs;
        }
        if (nullsCount == fileObjs.length) {
            return new FileObject[0];
        }
        
        final FileObject[] fileObjsNew
                = new FileObject[fileObjs.length - nullsCount];
        int index = 0, indexNew = 0;
        while (indexNew < fileObjsNew.length) {
            FileObject fileObj = fileObjs[index++];
            if (fileObj != null) {
                fileObjsNew[indexNew++] = fileObj;
            }
        }
        return fileObjsNew;
    }

    /**
     * Creates a map mapping folders to source groups.
     * For a folder as a key, the map returns the source group having that
     * folder as a root. The created map is stored to variable
     * {@link #foldersToSourceGroupsMap}.
     *
     * @param  sourceGroup  source group to create a map from
     * @author  Marian Petras
     */
    private void createFoldersToSourceGroupsMap(
            final SourceGroup[] sourceGroups) {
        Map<FileObject,Object> result;

        if (sourceGroups.length == 0) {
            result = Collections.<FileObject,Object>emptyMap();
        } else {
            result = new HashMap<FileObject,Object>(2 * sourceGroups.length,
                                                    .5f);
            for (SourceGroup sourceGroup : sourceGroups) {
                result.put(sourceGroup.getRootFolder(), sourceGroup);
            }
        }

        foldersToSourceGroupsMap = result;
    }
    
    /**
     * <!-- PENDING -->
     */
    public SourceGroup[] getJavaSourceGroups() {
        if (javaSourceGroups == null) {
            javaSourceGroups = ProjectUtils.getSources(project).getSourceGroups(
                                        JavaProjectConstants.SOURCES_TYPE_JAVA);
        }
        return javaSourceGroups;
    }
    
    /**
     * Finds a <code>SourceGroup</code> having the specified root folder.
     * If there are more <code>SourceGroup</code>s matching, the first one
     * (according to the order of elements in the array) is returned.
     *
     * @param  sourceGroups  source groups to test
     * @param  rootFolder  root folder of a source group to be found
     * @return  the found <code>SourceGroup</code>;
     *          or <code>null</code> if no matching <code>SourceGroup</code>
     *          was found
     */
    private static SourceGroup findSourceGroup(SourceGroup[] sourceGroups,
                                               FileObject rootFolder) {
        for (int i = 0; i < sourceGroups.length; i++) {
            if (sourceGroups[i].getRootFolder().equals(rootFolder)) {
                return sourceGroups[i];
            }
        }
        return (SourceGroup) null;
    }
    
    public static boolean isValidClassName(String className) {
        if (className.length() == 0) {
            return false;
        }
        char[] chars = className.toCharArray();
        int segmentStart = 0;
        int i;
        for (i = 0; i < chars.length; i++) {
            if (chars[i] == '.') {
                if (i == segmentStart) {
                    return false;         //empty segment
                }
                if (!Utilities.isJavaIdentifier(
                        className.substring(segmentStart, i))) {
                    return false;         //illegal name of the segment
                }
                segmentStart = i + 1;
            }
        }
        if (i == segmentStart) {
            return false;                 //empty last segment
        }
        if (!Utilities.isJavaIdentifier(
                className.substring(segmentStart, chars.length))) {
            return false;                 //illegal name of the last segment
        }
        return true;
    }
        
}
