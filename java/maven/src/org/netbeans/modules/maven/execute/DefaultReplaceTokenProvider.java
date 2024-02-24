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

package org.netbeans.modules.maven.execute;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.maven.ActionProviderImpl;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.classpath.MavenSourcesImpl;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.spi.actions.ActionConvertor;
import org.netbeans.modules.maven.spi.actions.ReplaceTokenProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.SingleMethod;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 *
 * @author mkleint
 */
@ProjectServiceProvider(service={ReplaceTokenProvider.class, ActionConvertor.class}, projectType="org-netbeans-modules-maven")
public class DefaultReplaceTokenProvider implements ReplaceTokenProvider, ActionConvertor {
    private static final String ARTIFACTID = "artifactId";//NOI18N
    private static final String CLASSPATHSCOPE = "classPathScope";//NOI18N
    private static final String GROUPID = "groupId";//NOI18N
    private final Project project;
    static final String CLASSNAME = "className";//NOI18N
    static final String CLASSNAME_EXT = "classNameWithExtension";//NOI18N
    static final String PACK_CLASSNAME = "packageClassName";//NOI18N
    static final String ABSOLUTE_PATH = "absolutePathName";
    public static final String METHOD_NAME = "nb.single.run.methodName"; //NOI18N
    private static final String VARIABLE_PREFIX = "var."; //NOI18N
    // as defined in org.netbeans.modules.project.ant.VariablesModel
    public static String[] fileBasedProperties = new String[] {
        PACK_CLASSNAME, CLASSNAME, CLASSNAME_EXT, ABSOLUTE_PATH
    };
    public DefaultReplaceTokenProvider(Project prj) {
        project = prj;
    }

    private static FileObject[] extractFileObjectsfromLookup(Lookup lookup) {
        List<FileObject> files = new ArrayList<FileObject>(lookup.lookupAll(FileObject.class));
        if (files.isEmpty()) { // fallback to old nodes
            for (DataObject d : lookup.lookupAll(DataObject.class)) {
                files.add(d.getPrimaryFile());
            }
        }
        Collection<? extends SingleMethod> methods = lookup.lookupAll(SingleMethod.class);
        if (methods.size() == 1) {
            SingleMethod method = methods.iterator().next();
            files.add(method.getFile());
        }

        return files.toArray(new FileObject[0]);
    }

    @Override public Map<String, String> createReplacements(String actionName, Lookup lookup) {
        FileObject[] fos = extractFileObjectsfromLookup(lookup);
        SourceGroup group = findGroup(ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA), fos);
        HashMap<String, String> replaceMap = new HashMap<String, String>();
        // read environment variables in the IDE and prefix them with "env." just in case someone uses it as variable in the action mappings
        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            replaceMap.put(MavenCommandLineExecutor.ENV_PREFIX + entry.getKey(), entry.getValue());
        }
        
        if (fos.length > 0) {
            replaceMap.put(ABSOLUTE_PATH, FileUtil.toFile(fos[0]).getAbsolutePath());
        }
        
        //read global variables defined in the IDE
        Map<String, String> vars = readVariables();
        replaceMap.putAll(vars);
        
        //read active configuration properties..
        Map<String, String> configProps = project.getLookup().lookup(M2ConfigProvider.class).getActiveConfiguration().getProperties();
        replaceMap.putAll(configProps);

        NbMavenProject prj = project.getLookup().lookup(NbMavenProject.class);
        replaceMap.put(GROUPID, prj.getMavenProject().getGroupId());
        replaceMap.put(ARTIFACTID, prj.getMavenProject().getArtifactId());

        StringBuilder packClassname = new StringBuilder();
        StringBuilder classname = new StringBuilder();
        StringBuilder classnameExt = new StringBuilder();
        if (group != null) {
            boolean first = true;
            boolean isTest = false;
            Set<String> uniqueClassNames = new HashSet<String>(fos.length);
            for (FileObject file : fos) {
                if (first) {
                    first = false;
                } else {
                    if (!isTest && !(ActionProvider.COMMAND_TEST_SINGLE.equals(actionName) ||
                                     ActionProvider.COMMAND_DEBUG_TEST_SINGLE.equals(actionName) ||
                                     ActionProvider.COMMAND_PROFILE_TEST_SINGLE.equals(actionName) ||
                                     ActionProvider.COMMAND_TEST.equals(actionName))) {
                        // Execution can not have more files separated by commas. Only test can.
                        break;
                    } else {
                        isTest = true;
                    }
                    packClassname.append(',');
                    classname.append(',');
                    classnameExt.append(',');
                }
                if (file.isFolder()) {
                    String rel = FileUtil.getRelativePath(group.getRootFolder(), file);
                    assert rel != null;
                    String pkg = rel.replace('/', '.');
                    if (!pkg.isEmpty()) {
                        packClassname.append(pkg).append(".**."); // test everything under this package recusively
                    }
                    packClassname.append("*");
                    if (ActionProvider.COMMAND_TEST_SINGLE.equals(actionName) || ActionProvider.COMMAND_DEBUG_TEST_SINGLE.equals(actionName)) {
                        packClassname.append("Test");
                    }
                    classname.append(pkg); // ?
                    classnameExt.append(pkg); // ??
                } else { // XXX do we need to limit to text/x-java? What about files of other type?
                    String relP = FileUtil.getRelativePath(group.getRootFolder(), file.getParent());
                    assert relP != null;
                    StringBuilder cn = new StringBuilder();
                    if (!relP.isEmpty()) {
                        cn.append(relP.replace('/', '.')).append('.');
                    }
                    String n = file.getName();
                    cn.append(n);
                    if (uniqueClassNames.add(cn.toString())) {
                        packClassname.append(cn);
                        classname.append(n);
                    } else {
                        packClassname.deleteCharAt(packClassname.length() - 1); // Delete the comma
                        classname.deleteCharAt(classname.length() - 1);
                    }
                    classnameExt.append(file.getNameExt());
                    if (MavenSourcesImpl.NAME_SOURCE.equals(group.getName()) &&
                        (ActionProvider.COMMAND_TEST_SINGLE.equals(actionName) ||
                         ActionProvider.COMMAND_DEBUG_TEST_SINGLE.equals(actionName) ||
                         ActionProvider.COMMAND_PROFILE_TEST_SINGLE.equals(actionName))) {
                        String fix = "Test";
                        if (classnameExt.toString().endsWith("." + file.getExt())) {
                            classnameExt.delete(classnameExt.length() - ("." + file.getExt()).length(), classnameExt.length());
                            URL[] unitRoots = UnitTestForSourceQuery.findUnitTests(group.getRootFolder());
                            if (unitRoots != null) {
                                for (URL unitRoot : unitRoots) {
                                    FileObject root = URLMapper.findFileObject(unitRoot);
                                    if (root != null) { //#237312
                                        String ngPath = relP + (relP.isEmpty() ? "" : "/") + classnameExt + "NGTest." + file.getExt();
                                        if (root.getFileObject(ngPath) != null) {
                                            fix = "NGTest";
                                            break;
                                        }
                                    }
                                }
                            }
                            classnameExt.append(fix).append(".").append(file.getExt());
                        }
                        packClassname.append(fix);
                        classname.append(fix);
                    }
                }
            }
        } else {
            // not all of the selected files are under one source root, so maybe they were
            // selected from both source and test packages and "Test Files" action was invoked on them?
            if (ActionProvider.COMMAND_TEST_SINGLE.equals(actionName) ||
                ActionProviderImpl.COMMAND_INTEGRATION_TEST_SINGLE.equals(actionName) ||
                ActionProvider.COMMAND_DEBUG_TEST_SINGLE.equals(actionName)) 
            {
                HashSet<String> test = new HashSet<String>();
                addSelectedFiles(false, fos, test);
                addSelectedFiles(true, fos, test);
                
                packClassname.append(test
                        .stream()
                        .map(String::trim)
                        .collect(Collectors.joining(","))
                );

            }
        }
        if (packClassname.length() > 0) { //#213671
            replaceMap.put(PACK_CLASSNAME, packClassname.toString());
        }
        if (classname.length() > 0) { //#213671
            replaceMap.put(CLASSNAME, classname.toString());
        }
        if (classnameExt.length() > 0) { //#213671
            replaceMap.put(CLASSNAME_EXT, classnameExt.toString());
        }

        Collection<? extends SingleMethod> methods = lookup.lookupAll(SingleMethod.class);
        if (methods.size() == 1) {
            //sort of hack to push the method name through the current apis..
            SingleMethod method = methods.iterator().next();
            replaceMap.put(METHOD_NAME, method.getMethodName());
        }

        if (group != null &&
                //TODO not nice, how to figure in a better way? by source classpath?
                (MavenSourcesImpl.NAME_TESTSOURCE.equals(group.getName()))) {
            replaceMap.put(CLASSPATHSCOPE,"test"); //NOI18N
        } else {
            replaceMap.put(CLASSPATHSCOPE,"runtime"); //NOI18N
        }
        return replaceMap;
    }

    private void addSelectedFiles(boolean testRoots, FileObject[] candidates, HashSet<String> test) {
        NbMavenProjectImpl prj = project.getLookup().lookup(NbMavenProjectImpl.class);
        if (prj != null) {
            addSelectedFilesInGivenRoot(prj.getSourceRoots(testRoots), candidates, testRoots, test);
            addSelectedFilesInGivenRoot(prj.getGeneratedSourceRoots(testRoots), candidates, testRoots, test);
        }
    }

    private void addSelectedFilesInGivenRoot(URI[] roots, FileObject[] candidates, boolean testRoots, HashSet<String> test) throws IllegalArgumentException {
        for (URI rootUri : roots) {
            FileObject root = FileUtil.toFileObject(Utilities.toFile(rootUri));
            // test if root isn't null - NbMavenProjectImpl.getSourceRoots() might return a bogus
            // non test uri in case there are only test source roots.
            // NOTE that not sure if this is generaly the right place for the fix. Even though it is
            // MavenProject which returns those uris, not sure if e.g. that behaviour wasn't somewhere on the way overriden
            // by the nb maven module ...
            if(root != null) {
                for (FileObject candidate : candidates) {
                    String relativePath = FileUtil.getRelativePath(root, candidate);
                    if (relativePath != null) {
                        if (testRoots) {
                            relativePath = relativePath.replace(".java", "").replace('/', '.'); //NOI18N
                            if (candidate.isFolder()) {
                                relativePath += relativePath.isEmpty()
                                        ? "**"
                                        : ".**";
                            }
                        } else {
                            relativePath = relativePath.replace(".java", "Test").replace('/', '.'); //NOI18N
                        }
                        test.add(relativePath);
                    }
                }
            }
        }
    }

    /** Finds the one source group, if any, which contains all of the listed files. */
    private static @CheckForNull SourceGroup findGroup(SourceGroup[] groups, FileObject[] files) {
        SourceGroup selected = null;
        for (FileObject file : files) {
            for (SourceGroup group : groups) {
                FileObject root = group.getRootFolder();
                if (file == root || FileUtil.isParentOf(root, file)) { // or group.contains(file)?
                    if (selected == null) {
                        selected = group;
                    } else if (selected != group) {
                        return null;
                    }
                }
            }
        }
        return selected;
    }

    public static Map<String, String> readVariables() {
        Map<String, String> vs = new HashMap<String, String>();
        EditableProperties ep = PropertyUtils.getGlobalProperties();
        for (Map.Entry<String, String> entry : ep.entrySet()) {
            if (entry.getKey().startsWith(VARIABLE_PREFIX)) {
                vs.put(entry.getKey().substring(VARIABLE_PREFIX.length()), FileUtil.normalizeFile(new File(entry.getValue())).getAbsolutePath());
            }
        }
        return vs;
    }


//    /*
//     * copied from ActionUtils and reworked so that it checks for mimeType of files, and DOES NOT include files with suffix 'suffix'
//     */
//    private static FileObject[] findSelectedFilesByMimeType(Lookup context, FileObject dir, String mimeType, String suffix, boolean strict) {
//        if (dir != null && !dir.isFolder()) {
//            throw new IllegalArgumentException("Not a folder: " + dir); // NOI18N
//        }
//
//        List<FileObject> files = new ArrayList<FileObject>();
//        for (DataObject d : context.lookupAll(DataObject.class)) {
//            FileObject f = d.getPrimaryFile();
//            boolean matches = FileUtil.toFile(f) != null;
//            if (dir != null) {
//                matches &= (FileUtil.isParentOf(dir, f) || dir == f);
//            }
//            if (mimeType != null) {
//                matches &= f.getMIMEType().equals(mimeType);
//            }
//            if (suffix != null) {
//                matches &= !f.getNameExt().endsWith(suffix);
//            }
//            // Generally only files from one project will make sense.
//            // Currently the action UI infrastructure (PlaceHolderAction)
//            // checks for that itself. Should there be another check here?
//            if (matches) {
//                files.add(f);
//            } else if (strict) {
//                return null;
//            }
//        }
//        if (files.isEmpty()) {
//            return null;
//        }
//        return files.toArray(
//                new FileObject[files.size()]);
//    }
    
    private boolean isIntegrationTestFile(FileObject file) {
        return file.getName().endsWith("IT") || file.getName().endsWith("ITCase"); //NOI18N
    }

    private boolean isIntegrationTestTarget(Lookup lookup) {
        FileObject[] targetFiles = extractFileObjectsfromLookup(lookup);
        if (targetFiles.length > 0) {
            return Stream.of(targetFiles).allMatch(file -> isIntegrationTestFile(file));
        }
        return false;
    }
    
    @Override public String convert(String action, Lookup lookup) {
        if (SingleMethod.COMMAND_DEBUG_SINGLE_METHOD.equals(action)) {
            if (isIntegrationTestTarget(lookup)) {
                return ActionProviderImpl.COMMAND_DEBUG_INTEGRATION_TEST_SINGLE;
            }
            return ActionProvider.COMMAND_DEBUG_TEST_SINGLE;
        }
        if (SingleMethod.COMMAND_RUN_SINGLE_METHOD.equals(action)) {
            if (isIntegrationTestTarget(lookup)) {
                return ActionProviderImpl.COMMAND_INTEGRATION_TEST_SINGLE;
            }
            return ActionProvider.COMMAND_TEST_SINGLE;
        }
        if (ActionProvider.COMMAND_TEST_SINGLE.equals(action) && isIntegrationTestTarget(lookup)) {
            return ActionProviderImpl.COMMAND_INTEGRATION_TEST_SINGLE;
        }
        if (ActionProvider.COMMAND_DEBUG_TEST_SINGLE.equals(action) && isIntegrationTestTarget(lookup)) {
            return ActionProviderImpl.COMMAND_DEBUG_INTEGRATION_TEST_SINGLE;
        }
        if (ActionProvider.COMMAND_RUN_SINGLE.equals(action) ||
            ActionProvider.COMMAND_DEBUG_SINGLE.equals(action) ||
            ActionProvider.COMMAND_PROFILE_SINGLE.equals(action)) {
            FileObject[] fos = extractFileObjectsfromLookup(lookup);
            if (fos.length > 0) {
                FileObject fo = fos[0];
                if ("text/x-java".equals(fo.getMIMEType())) {//NOI18N
                    Sources srcs = ProjectUtils.getSources(project);
                    SourceGroup[] grp = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                    for (int i = 0; i < grp.length; i++) {
                        String relPath = FileUtil.getRelativePath(grp[i].getRootFolder(), fo);
                        if (relPath != null) {
                            if (SourceUtils.isMainClass(relPath.replaceFirst("[.]java$", "").replace('/', '.'), ClasspathInfo.create(fo), true)) {
                                return action + ".main";//NOI18N
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

}
