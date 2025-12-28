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
package org.netbeans.modules.java.api.common;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.Utilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Represents a project module roots. It can be used to obtain module roots as Ant properties, {@link FileObject}'s
 * or {@link URL}s.
 * This class is thread safe and listens to the changes
 * in Ant project metadata (see {@link #PROP_ROOT_PROPERTIES}) as well as
 * in project properties (see {@link #PROP_ROOTS}).
 *
 * @author Dusan Balek
 */
public final class ModuleRoots extends SourceRoots {
    
    /**
     * Default label for sources node used in {@link org.netbeans.spi.project.ui.LogicalViewProvider}.
     */
    public static final String DEFAULT_MODULE_LABEL = NbBundle.getMessage(ModuleRoots.class, "NAME_module.dir");
    /**
     * Default label for sources node used in {@link org.netbeans.spi.project.ui.LogicalViewProvider}.
     */
    public static final String DEFAULT_TEST_MODULE_LABEL = NbBundle.getMessage(ModuleRoots.class, "NAME_test.module.dir");

    private static final String DEFAULT_MODULE_PATH = "classes"; //NOI18N
    private static final String DEFAULT_TEST_MODULE_PATH = "tests"; //NOI18N
    private static final String DEFAULT_PATH_TEMPLATE = "{0}{1}.path"; //NOI18N

    public static ModuleRoots create(UpdateHelper helper, PropertyEvaluator evaluator, ReferenceHelper refHelper,
            String projectConfigurationNamespace, String elementName, boolean isTest, String newRootNameTemplate) {
        Parameters.notNull("helper", helper); // NOI18N
        Parameters.notNull("evaluator", evaluator); // NOI18N
        Parameters.notNull("refHelper", refHelper); // NOI18N
        Parameters.notNull("projectConfigurationNamespace", projectConfigurationNamespace); // NOI18N
        Parameters.notNull("elementName", elementName); // NOI18N
        Parameters.notNull("newRootNameTemplate", newRootNameTemplate); // NOI18N

        return new ModuleRoots(helper, evaluator, refHelper, projectConfigurationNamespace, elementName,
                JavaProjectConstants.SOURCES_TYPE_MODULES, isTest, newRootNameTemplate);
    }
    
    private ModuleRoots(UpdateHelper helper, PropertyEvaluator evaluator, ReferenceHelper refHelper, String projectConfigurationNamespace, String elementName, String type, boolean isTest, String newRootNameTemplate) {
        super(helper, evaluator, refHelper, projectConfigurationNamespace, elementName, type, isTest, newRootNameTemplate);
    }

    @Override
    public String[] getRootPathProperties() {
        return super.getRootPathProperties();
    }

    @Override
    public URL[] getRootURLs(final boolean removeInvalidRoots) {
        synchronized (this) {
            if (sourceRootURLs != null) {
                return sourceRootURLs.toArray(new URL[0]);
            }
        }
        return ProjectManager.mutex().readAccess(() -> {
            synchronized (ModuleRoots.this) {
                // local caching
                if (sourceRootURLs == null) {
                    List<URL> result = new ArrayList<>();
                    for (String rootProp : getRootProperties()) {
                        String pathToRoot = evaluator.getProperty(rootProp);
                        if (pathToRoot != null) {
                            File file = helper.getAntProjectHelper().resolveFile(pathToRoot);
                            try {
                                URL url = Utilities.toURI(file).toURL();
                                if (!file.exists()) {
                                    url = new URL(url.toExternalForm() + "/"); // NOI18N
                                } else if (removeInvalidRoots && !file.isDirectory()) {
                                    // file cannot be a source root (archives are not supported as source roots).
                                    continue;
                                }
                                assert url.toExternalForm().endsWith("/") : "#90639 violation for " + url + "; "
                                        + file + " exists? " + file.exists() + " dir? " + file.isDirectory()
                                        + " file? " + file.isFile();
                                result.add(url);
                                listener.add(file, false);
                            } catch (MalformedURLException e) {
                                Exceptions.printStackTrace(e);
                            }
                        }
                    }
                    sourceRootURLs = Collections.unmodifiableList(result);
                }
                return sourceRootURLs.toArray(new URL[0]);
            }
        });
    }

    /**
     * Replaces the current module roots by the given ones.
     * @param roots the {@link URL}s of the new roots.
     * @param paths the paths of the new roots.
     */
    public void putModuleRoots(final URL[] roots, final String[] paths) {
        ProjectManager.mutex().writeAccess(() -> {
            final Map<URL, Pair<String, String>> oldRoots2props = getRootsToProps();
            final Map<URL, String> newRoots2paths = new HashMap<>();
            for (int i = 0; i < roots.length; i++) {
                newRoots2paths.put(roots[i], paths[i]);
            }
            final Element cfgEl = helper.getPrimaryConfigurationData(true);
            final NodeList nl = cfgEl.getElementsByTagNameNS(projectConfigurationNamespace, elementName);
            if (nl.getLength() != 1) {
                final FileObject prjDir = helper.getAntProjectHelper().getProjectDirectory();
                final FileObject projectXml = prjDir == null ?
                        null :
                        prjDir.getFileObject(AntProjectHelper.PROJECT_XML_PATH);
                String content = null;
                try {
                    content = projectXml == null ?
                            null :
                            projectXml.asText("UTF-8");      //NOI18N
                } catch (IOException e) {/*ignore*/}
                throw new IllegalArgumentException(String.format(
                        "Broken nbproject/project.xml, missing %s in %s namespace, content: %s.",   //NOI18N
                        elementName,
                        projectConfigurationNamespace,
                        content));
            }
            final Element ownerElement = (Element) nl.item(0);
            // remove all old roots
            final NodeList rootsNodes =
                    ownerElement.getElementsByTagNameNS(projectConfigurationNamespace, "root");    //NOI18N
            while (rootsNodes.getLength() > 0) {
                Element root = (Element) rootsNodes.item(0);
                ownerElement.removeChild(root);
            }
            // remove all unused root properties
            final List<URL> newRoots = Arrays.asList(roots);
            final Map<URL, Pair<String, String>> propsToRemove = new HashMap<>(oldRoots2props);
            propsToRemove.keySet().removeAll(newRoots);
            final EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            final Set<String> referencesToRemove = new HashSet<>();
            propsToRemove.values().stream().forEach(propToRemove -> {
                final String propValue = ep.getProperty(propToRemove.first());
                if (propValue != null && propValue.startsWith(REF_PREFIX)) {
                    referencesToRemove.add(propValue);
                }
                ep.remove(propToRemove.first());
                ep.remove(propToRemove.second());
            });
            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
            referencesToRemove.stream()
                    .filter(referenceToRemove -> !isUsed(referenceToRemove, ep))
                    .forEach(referenceToRemove -> {
                refHelper.destroyReference(referenceToRemove);
            });
            // add the new roots
            Document doc = ownerElement.getOwnerDocument();
            oldRoots2props.keySet().retainAll(newRoots);
            newRoots.stream().map((newRoot) -> {
                Pair<String, String> props = oldRoots2props.get(newRoot);
                if (props == null) {
                    // root is new generate property for it
                    String[] names = newRoot.getPath().split("/");  //NOI18N
                    String rootName = MessageFormat.format(
                            newRootNameTemplate, new Object[] {names[names.length - 1], ""}); // NOI18N
                    int rootIndex = 1;
                    while (ep.containsKey(rootName)) {
                        rootIndex++;
                        rootName = MessageFormat.format(
                                newRootNameTemplate, new Object[] {names[names.length - 1], rootIndex});
                    }
                    File f = FileUtil.normalizeFile(Utilities.toFile(URI.create(newRoot.toExternalForm())));
                    File projDir = FileUtil.toFile(helper.getAntProjectHelper().getProjectDirectory());
                    String path = f.getAbsolutePath();
                    String prjPath = projDir.getAbsolutePath() + File.separatorChar;
                    if (path.startsWith(prjPath)) {
                        path = path.substring(prjPath.length());
                    } else {
                        path = refHelper.createForeignFileReference(
                                f, JavaProjectConstants.SOURCES_TYPE_JAVA);
                    }
                    ep.put(rootName, path);
                    String rootPath = MessageFormat.format(DEFAULT_PATH_TEMPLATE, rootName, ""); //NOI18N
                    rootIndex = 1;
                    while (ep.containsKey(rootPath)) {
                        rootIndex++;
                        rootPath = MessageFormat.format(DEFAULT_PATH_TEMPLATE, rootName, rootIndex);
                    }
                    ep.put(rootPath, newRoots2paths.get(newRoot));
                    helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                    props = Pair.of(rootName, rootPath);
                }
                Element newRootNode = doc.createElementNS(projectConfigurationNamespace, "root"); //NOI18N
                newRootNode.setAttribute("id", props.first()); //NOI18N
                String path = props.second();
                if (path != null && path.length() > 0) {
                    newRootNode.setAttribute("pathref", path); //NOI18N
                }
                return newRootNode;
            }).forEachOrdered((newRootNode) -> {
                ownerElement.appendChild(newRootNode);
            });
            helper.putPrimaryConfigurationData(cfgEl, true);
        });
    }
    
    @Override
    public String getRootDisplayName(String rootName, String propName) {
        if (rootName == null || rootName.length() == 0) {
            // if the prop is src.dir use the default name
            if (isTest() && "test.src.dir".equals(propName)) { //NOI18N
                rootName = DEFAULT_TEST_MODULE_LABEL;
            } else if (!isTest() && "src.dir".equals(propName)) { //NOI18N
                rootName = DEFAULT_MODULE_LABEL;
            } else {
                // if the name is not given, it should be either a relative path in the project dir
                // or absolute path when the root is not under the project dir
                String propValue = evaluator.getProperty(propName);
                File sourceRoot = propValue == null ? null : helper.getAntProjectHelper().resolveFile(propValue);
                rootName = createInitialDisplayName(sourceRoot);
            }
        }
        return rootName;
    }

    @Override
    public String createInitialDisplayName(File sourceRoot) {
        String rootName;
        if (sourceRoot != null) {
            String srPath = sourceRoot.getAbsolutePath();
            String pdPath = projectDir.getAbsolutePath() + File.separatorChar;
            if (srPath.startsWith(pdPath)) {
                rootName = srPath.substring(pdPath.length());
            } else {
                rootName = sourceRoot.getAbsolutePath();
            }
        } else {
            rootName = isTest() ? DEFAULT_TEST_MODULE_LABEL : DEFAULT_MODULE_LABEL;
        }
        return rootName;
    }

    public String getRootPath(String rootPathProperty) {
        String prop = evaluator.getProperty(rootPathProperty);
        if (prop != null) {
            StringBuilder sb = new StringBuilder();
            for (String propElement : PropertyUtils.tokenizePath(prop)) {
                if (sb.length() > 0) {
                    sb.append(':');
                }
                sb.append(propElement);
            }
            return sb.toString();
        }
        return ""; //NOI18N
    }
    
    /**
     * Creates initial path of module root.
     * @return the path.
     */
    public String createInitialPath() {
        return isTest() ? DEFAULT_TEST_MODULE_PATH : DEFAULT_MODULE_PATH;
    }

    private Map<URL, Pair<String, String>> getRootsToProps() {
        return ProjectManager.mutex().readAccess(() -> {
            Map<URL, Pair<String, String>> result = new HashMap<>();
            String[] rootProperties = getRootProperties();
            String[] rootPathProperties = getRootPathProperties();
            assert rootProperties.length == rootPathProperties.length;
            for (int i = 0; i < rootProperties.length; i++) {
                String rootProp = evaluator.getProperty(rootProperties[i]);
                if (rootProp != null) {
                    File f = helper.getAntProjectHelper().resolveFile(rootProp);
                    try {
                        URL url = Utilities.toURI(f).toURL();
                        if (!f.exists()) {
                            url = new URL(url.toExternalForm() + "/"); // NOI18N
                        } else if (f.isFile()) {
                            // file cannot be a source root (archives are not supported as source roots).
                            continue;
                        }
                        assert url.toExternalForm().endsWith("/") : "#90639 violation for " + url + "; "
                                + f + " exists? " + f.exists() + " dir? " + f.isDirectory()
                                + " file? " + f.isFile();
                        result.put(url, Pair.of(rootProperties[i], rootPathProperties[i]));
                    } catch (MalformedURLException e) {
                        Exceptions.printStackTrace(e);
                    }
                }
            }
            return result;
        });
    }
}
