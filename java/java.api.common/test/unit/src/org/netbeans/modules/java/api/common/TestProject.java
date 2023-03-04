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
package org.netbeans.modules.java.api.common;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.ant.UpdateImplementation;
import org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.queries.QuerySupport;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation;
import org.netbeans.spi.java.queries.MultipleRootsUnitTestForSourceQueryImplementation;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation2;
import org.netbeans.spi.java.queries.UnitTestForSourceQueryImplementation;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Tomas Zezula
 */
public final class TestProject implements Project {

    public static final String PROJECT_CONFIGURATION_NAMESPACE = "urn:test";

    final Map<Class<?>,Function<TestProject, Object>> mergedFactories;
    private final UpdateHelper helper;
    private final PropertyEvaluator evaluator;
    private final AuxiliaryConfiguration aux;
    private final ReferenceHelper refHelper;
    private final SourceRoots src;
    private final SourceRoots test;
    private Lookup lookup;

    private TestProject(
            @NonNull final AntProjectHelper helper,
            @NonNull final Map<Class<?>,Function<TestProject, Object>> serviceFactories) {
        this.helper = new UpdateHelper(new UpdateImplementation() {
                @Override public boolean isCurrent() {
                    return true;
                }
                @Override public boolean canUpdate() {
                    throw new AssertionError();
                }
                @Override public void saveUpdate(EditableProperties props) throws IOException {
                    throw new AssertionError();
                }
                @Override public Element getUpdatedSharedConfigurationData() {
                    throw new AssertionError();
                }
                @Override public EditableProperties getUpdatedProjectProperties() {
                    throw new AssertionError();
                }
            }, helper);
        this.evaluator = helper.getStandardPropertyEvaluator();
        this.aux = helper.createAuxiliaryConfiguration();
        this.refHelper = new ReferenceHelper(helper, aux, evaluator);
        this.src = SourceRoots.create(this.helper, evaluator, refHelper, PROJECT_CONFIGURATION_NAMESPACE,
                    "source-roots", false, "src.{0}{1}.dir");
        this.test = SourceRoots.create(this.helper, evaluator, refHelper, PROJECT_CONFIGURATION_NAMESPACE,
                    "test-roots", false, "test.{0}{1}.dir");
        this.mergedFactories = new HashMap<>();
        merge(mergedFactories,
                serviceFactories,
                (p) -> new TestSources(this, helper, evaluator, src, test),
                Sources.class);
        merge(mergedFactories,
                serviceFactories,
                (p) -> new ClassPathProviderImpl(helper, evaluator, src, test),
                ClassPathProvider.class);
        merge(mergedFactories,
                serviceFactories,
                (p) -> QuerySupport.createCompiledSourceForBinaryQuery(helper, evaluator, src, test),
                SourceForBinaryQueryImplementation.class,
                SourceForBinaryQueryImplementation2.class);
        merge(mergedFactories,
                serviceFactories,
                (p) -> QuerySupport.createBinaryForSourceQueryImplementation(src, test, helper, evaluator),
                BinaryForSourceQueryImplementation.class);
        merge(mergedFactories,
                serviceFactories,
                (p) -> QuerySupport.createUnitTestForSourceQuery(src, test),
                UnitTestForSourceQueryImplementation.class,
                MultipleRootsUnitTestForSourceQueryImplementation.class);
        merge(mergedFactories,
                serviceFactories,
                (p) -> QuerySupport.createSourceLevelQuery2(evaluator),
                SourceLevelQueryImplementation.class,
                SourceLevelQueryImplementation2.class);
        merge(mergedFactories,
                serviceFactories,
                (p) -> QuerySupport.createFileBuiltQuery(helper, evaluator, src, test),
                FileBuiltQueryImplementation.class);
        merge(mergedFactories,
                serviceFactories,
                (p) -> QuerySupport.createFileEncodingQuery(evaluator, "encoding"),
                FileEncodingQueryImplementation.class);
    }

    @Override
    public FileObject getProjectDirectory() {
        return helper.getAntProjectHelper().getProjectDirectory();
    }

    @NonNull
    public UpdateHelper getUpdateHelper() {
        return helper;
    }

    @NonNull
    public PropertyEvaluator getEvaluator() {
        return evaluator;
    }

    @NonNull
    public ReferenceHelper getReferenceHelper() {
        return refHelper;
    }

    @NonNull
    public SourceRoots getSourceRoots() {
        return src;
    }

    public SourceRoots getTestRoots() {
        return test;
    }

    @Override
    public synchronized Lookup getLookup() {
        if (lookup == null) {
            final InstanceContent ic = new InstanceContent();
            ic.add(this);
            ic.add(aux);
            lookup = new AbstractLookup(ic) {
                private final Set<Class> seen = Collections.synchronizedSet(new HashSet<>());

                @Override
                protected void beforeLookup(Lookup.Template<?> template) {
                    super.beforeLookup(template);
                    final Class<?> type = template.getType();
                    if (seen.add(type)) {
                        final Function<TestProject, Object> f = mergedFactories.get(type);
                        if (f != null) {
                            final Object res = f.apply(TestProject.this);
                            if (type.isInstance(res)) {
                                ic.add(res);
                            }
                        }
                    }
                }
            };
        }
        return lookup;
    }

    @NonNull
    public static AntBasedProjectType createProjectType() {
        return createProjectType(Collections.emptyMap());
    }

    @NonNull
    public static AntBasedProjectType createProjectType(@NonNull final Map<Class<?>,Function<TestProject, Object>> serviceFactories) {
        return new TestAntBasedProjectType(serviceFactories);
    }

    @NonNull
    public static Project createProject(
            @NonNull final FileObject projectFolder,
            @NullAllowed final FileObject srcRoot,
            @NullAllowed final FileObject testRoot) {
        return createProjectImpl(projectFolder, srcRoot, testRoot, false);
    }

    @NonNull
    public static Project createMultiModuleProject(
            @NonNull final FileObject projectFolder) {
        return createProjectImpl(projectFolder, null, null, true);
    }

    @NonNull
    private static Project createProjectImpl(
            @NonNull final FileObject projectFolder,
            @NullAllowed final FileObject srcRoot,
            @NullAllowed final FileObject testRoot,
            final boolean multiModule) {
        return ProjectManager.mutex().writeAccess(() -> {
            try {
                AntProjectHelper h = ProjectGenerator.createProject(projectFolder, "test");
                EditableProperties pp = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                if (srcRoot != null) {
                    pp.setProperty("src.dir", PropertyUtils.relativizeFile(
                            FileUtil.toFile(projectFolder),
                            FileUtil.toFile(srcRoot)));
                }
                if (testRoot != null) {
                    pp.setProperty("test.src.dir", PropertyUtils.relativizeFile(
                            FileUtil.toFile(projectFolder),
                            FileUtil.toFile(testRoot)));
                }
                if (multiModule) {
                    pp.setProperty("src.dir.path", "classes");
                    pp.setProperty("test.src.dir.path", "classes");
                }
                pp.setProperty("build.dir", "build");
                pp.setProperty("build.classes.dir", "${build.dir}/classes");
                pp.setProperty("build.test.classes.dir", "${build.dir}/test/classes");
                pp.setProperty("build.generated.sources.dir", "${build.dir}/generated-sources");
                pp.setProperty("javac.classpath", "lib.jar");
                pp.setProperty("javac.test.classpath", "${javac.classpath}:junit.jar");
                pp.setProperty("run.classpath", "${javac.classpath}:${build.classes.dir}:runlib.jar");
                pp.setProperty("run.test.classpath", "${javac.test.classpath}:${build.test.classes.dir}:runlib.jar");
                pp.setProperty("dist.dir", "dist");
                pp.setProperty("dist.jar", "${dist.dir}/x.jar");
                pp.setProperty(ProjectProperties.DIST_JAVADOC_DIR, "${dist.dir}/javadoc");
                pp.setProperty("javac.source", "1.6");
                pp.setProperty("encoding", "UTF-8");
                h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, pp);
                Element data = h.getPrimaryConfigurationData(true);
                Document doc = data.getOwnerDocument();
                Element sRoot = (Element) data.appendChild(doc.createElementNS(TestProject.PROJECT_CONFIGURATION_NAMESPACE, "source-roots")).
                        appendChild(doc.createElementNS(TestProject.PROJECT_CONFIGURATION_NAMESPACE, "root"));
                sRoot.setAttribute("id", "src.dir");
                if (multiModule) {
                    sRoot.setAttribute("pathref", "src.dir.path");
                }
                Element tRoot = ((Element) data.appendChild(doc.createElementNS(TestProject.PROJECT_CONFIGURATION_NAMESPACE, "test-roots")).
                        appendChild(doc.createElementNS(TestProject.PROJECT_CONFIGURATION_NAMESPACE, "root")));
                tRoot.setAttribute("id", "test.src.dir");
                if (multiModule) {
                    tRoot.setAttribute("pathref", "test.src.dir.path");
                }
                h.putPrimaryConfigurationData(data, true);
                Project p = ProjectManager.getDefault().findProject(projectFolder);
                if (p == null) {
                    throw new IllegalStateException("No project");  //NOI18N
                }
                if (p.getClass() != TestProject.class) {
                    throw new IllegalStateException("Wrong project type");  //NOI18N
                }
                ProjectManager.getDefault().saveProject(p);
                return p;
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        });
    }

    @NonNull
    private static void merge(
            @NonNull final Map<Class<?>,Function<TestProject, Object>> mergedFactories,
            @NonNull final Map<Class<?>,Function<TestProject, Object>> serviceFactories,
            @NonNull final Function<TestProject, Object> defaultFactory,
            @NonNull final Class... serviceTypes) {
        Function<TestProject, Object> res = null;
        for (Class serviceType : serviceTypes) {
            final Function<TestProject, Object> factory = serviceFactories.get(serviceType);
            if (factory != null) {
                res = factory;
                break;
            }
        }
        if (res == null) {
            res = defaultFactory;
        }
        for (Class serviceType : serviceTypes) {
            mergedFactories.put(serviceType, res);
        }
    }

    private static class TestAntBasedProjectType implements AntBasedProjectType {
        private final Map<Class<?>,Function<TestProject, Object>> serviceFactories;

        TestAntBasedProjectType(@NonNull final Map<Class<?>,Function<TestProject, Object>> serviceFactories) {
            this.serviceFactories = serviceFactories;
        }

        public String getType() {
            return "test";
        }
        public Project createProject(AntProjectHelper helper) throws IOException {
            return new TestProject(helper, serviceFactories);
        }
        public String getPrimaryConfigurationDataElementName(boolean shared) {
            return "data";
        }
        public String getPrimaryConfigurationDataElementNamespace(boolean shared) {
            return PROJECT_CONFIGURATION_NAMESPACE;
        }
    }

    /**
     * Simplified copy of J2SESources.
     */
    private static class TestSources implements Sources, PropertyChangeListener, ChangeListener {

        private final Project project;
        private final AntProjectHelper helper;
        private final PropertyEvaluator evaluator;
        private final SourceRoots sourceRoots;
        private final SourceRoots testRoots;
        private SourcesHelper sourcesHelper;
        private Sources delegate;
        private final ChangeSupport changeSupport = new ChangeSupport(this);

        TestSources(Project project, AntProjectHelper helper, PropertyEvaluator evaluator, SourceRoots sourceRoots, SourceRoots testRoots) {
            this.project = project;
            this.helper = helper;
            this.evaluator = evaluator;
            this.evaluator.addPropertyChangeListener(this);
            this.sourceRoots = sourceRoots;
            this.sourceRoots.addPropertyChangeListener(this);
            this.testRoots = testRoots;
            this.testRoots.addPropertyChangeListener(this);
            initSources();
        }

        public SourceGroup[] getSourceGroups(final String type) {
            return ProjectManager.mutex().readAccess(new Mutex.Action<SourceGroup[]>() {
                public SourceGroup[] run() {
                    Sources _delegate;
                    synchronized (TestSources.this) {
                        if (delegate == null) {
                            delegate = initSources();
                            delegate.addChangeListener(TestSources.this);
                        }
                        _delegate = delegate;
                    }
                    return _delegate.getSourceGroups(type);
                }
            });
        }

        private Sources initSources() {
            sourcesHelper = new SourcesHelper(project, helper, evaluator);
            register(sourceRoots);
            register(testRoots);
            sourcesHelper.addNonSourceRoot("${build.dir}");
            return sourcesHelper.createSources();
        }

        private void register(SourceRoots roots) {
            String[] propNames = roots.getRootProperties();
            String[] rootNames = roots.getRootNames();
            for (int i = 0; i < propNames.length; i++) {
                String prop = propNames[i];
                String displayName = roots.getRootDisplayName(rootNames[i], prop);
                String loc = "${" + prop + "}";
                sourcesHelper.sourceRoot(loc).displayName(displayName).add();
                sourcesHelper.sourceRoot(loc).type(JavaProjectConstants.SOURCES_TYPE_JAVA).displayName(displayName).add();
            }
        }

        @Override
        public void addChangeListener(ChangeListener changeListener) {
            changeSupport.addChangeListener(changeListener);
        }

        @Override
        public void removeChangeListener(ChangeListener changeListener) {
            changeSupport.removeChangeListener(changeListener);
        }

        private void fireChange() {
            synchronized (this) {
                if (delegate != null) {
                    delegate.removeChangeListener(this);
                    delegate = null;
                }
            }
            changeSupport.fireChange();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if (SourceRoots.PROP_ROOT_PROPERTIES.equals(propName) || "build.dir".equals(propName)) {
                this.fireChange();
            }
        }

        @Override
        public void stateChanged(ChangeEvent event) {
            this.fireChange();
        }
    }

}
