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

package org.netbeans.modules.java.api.common.queries;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.modules.java.api.common.Roots;
import org.openide.util.Mutex;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.util.CommonModuleUtils;
import org.netbeans.modules.java.api.common.impl.RootsAccessor;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.project.SourceGroupModifierImplementation;
import org.netbeans.spi.project.SourceGroupRelativeModifierImplementation;
import org.netbeans.spi.project.support.GenericSources;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 * Implementation of {@link Sources} interface.
 */
final class SourcesImpl implements Sources, SourceGroupModifierImplementation, SourceGroupRelativeModifierImplementation, PropertyChangeListener, ChangeListener  {

    @StaticResource
    private static final String MODULE_ICON = "org/netbeans/modules/java/api/common/project/ui/resources/module.png"; //NOI18N

    private final Project project;
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final List<? extends Roots> roots;
    private boolean dirty;
    private final Map<String,SourceGroup[]> cachedGroups = new ConcurrentHashMap<String,SourceGroup[]>();
    private long eventId;
    private Sources delegate;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private SourceGroupModifierImplementation sgmi;
    private final FireAction fireTask = new FireAction();

    @SuppressWarnings("LeakingThisInConstructor")
    SourcesImpl(Project project, AntProjectHelper helper, PropertyEvaluator evaluator,
                Roots... roots) {
        this.project = project;
        this.helper = helper;
        this.evaluator = evaluator;
        this.roots = Collections.unmodifiableList(Arrays.asList(roots));
        for (Roots r : this.roots) {
            r.addPropertyChangeListener(WeakListeners.propertyChange(this, r));
        }
        final SourcesHelper sh = initSources();
        assert sh != null;
        sgmi = sh.createSourceGroupModifierImplementation();
        delegate = sh.createSources(); // have to register external build roots eagerly
    }

    /**
     * Returns an array of SourceGroup of given type. It delegates to {@link SourcesHelper}.
     * This method firstly acquire the {@link ProjectManager#mutex} in read mode then it enters
     * into the synchronized block to ensure that just one instance of the {@link SourcesHelper}
     * is created. These instance is cleared also in the synchronized block by the
     * {@link J2SESources#fireChange} method.
     */
    @Override
    public SourceGroup[] getSourceGroups(final String type) {
        final SourceGroup[] _cachedGroups = this.cachedGroups.get(type);
        if (_cachedGroups != null) {
            return _cachedGroups;
        }
        return ProjectManager.mutex().readAccess(new Mutex.Action<SourceGroup[]>() {
            public SourceGroup[] run() {
                Sources _delegate;
                long myEventId;
                synchronized (SourcesImpl.this) {
                    if (dirty) {
                        delegate.removeChangeListener(SourcesImpl.this);
                        SourcesHelper sh = initSources();
                        sgmi = sh.createSourceGroupModifierImplementation();
                        delegate = sh.createSources();
                        delegate.addChangeListener(SourcesImpl.this);
                        dirty = false;
                    }
                    _delegate = delegate;
                    myEventId = ++eventId;
                }
                SourceGroup[] groups = _delegate.getSourceGroups(type);
                if (type.equals(Sources.TYPE_GENERIC)) {
                    FileObject libLoc = getSharedLibraryFolderLocation();
                    if (libLoc != null) {
                        //#204232 only return as separate source group if not inside the default project one.
                        boolean isIncluded = false;
                        for (SourceGroup sg : groups) {
                            if (FileUtil.isParentOf(sg.getRootFolder(), libLoc)) {
                                isIncluded = true;
                                break;
                            }
                        }
                        if (!isIncluded) {
                        SourceGroup[] grps = new SourceGroup[groups.length + 1];
                        System.arraycopy(groups, 0, grps, 0, groups.length);
                        grps[grps.length - 1] = GenericSources.group(project, libLoc,
                                "sharedlibraries", // NOI18N
                                NbBundle.getMessage(SourcesImpl.class, "LibrarySourceGroup_DisplayName"),
                                null, null);
                        groups = grps;
                        }
                    }
                }
                synchronized (SourcesImpl.this) {
                    if (myEventId == eventId) {
                        SourcesImpl.this.cachedGroups.put(type, groups);
                    }
                }
                return groups;
            }
        });
    }

    @Override
    public SourceGroupModifierImplementation relativeTo(SourceGroup existingGroup, String... projectPart) {
        if (sgmi instanceof SourceGroupRelativeModifierImplementation) {
            return ((SourceGroupRelativeModifierImplementation)sgmi).relativeTo(existingGroup, projectPart);
        } else {
            return this;
        }
    }

    @Override
    public SourceGroup createSourceGroup(String type, String hint) {
        return sgmi.createSourceGroup(type, hint);
    }

    @Override
    public boolean canCreateSourceGroup(String type, String hint) {
        return sgmi.canCreateSourceGroup(type, hint);
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
        changeSupport.addChangeListener(changeListener);
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        changeSupport.removeChangeListener(changeListener);
    }

    private FileObject getSharedLibraryFolderLocation() {
        String libLoc = helper.getLibrariesLocation();
        if (libLoc != null) {
            String libLocEval = evaluator.evaluate(libLoc);
            if (libLocEval != null) {
                final File file = helper.resolveFile(libLocEval);
                FileObject libLocFO = FileUtil.toFileObject(file);
                if (libLocFO != null) {
                    //#126366 this can happen when people checkout the project but not the libraries description
                    //that is located outside the project
                    FileObject libLocParent = libLocFO.getParent();
                    return libLocParent;
                }
            }
        }
        return null;
    }
    
    private SourcesHelper initSources() {
        final SourcesHelper sourcesHelper = new SourcesHelper(project, helper, evaluator);   //Safe to pass APH
        for (Roots r : roots) {
            if (RootsAccessor.getInstance().isSourceRoot(r)) {
                registerSources(sourcesHelper, r);
            } else {
                registerNonSources(sourcesHelper, r);
            }
        }
        sourcesHelper.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT, false);
        return sourcesHelper;
    }

    @NbBundle.Messages({
            "# {0} - module name",
            "# {1} - folder name",
            "# {2} - source root name",
            "FMT_ModularSourceRootWithName={1} ({0} - {2})",
            "# {0} - module name",
            "# {1} - folder name",
            "FMT_ModularSourceRootNoName={1} ({0})"
    })
    private void registerSources(SourcesHelper sourcesHelper, Roots roots) {
        final String hint = RootsAccessor.getInstance().getHint(roots);
        final String type = RootsAccessor.getInstance().getType(roots);
        final String includes = RootsAccessor.getInstance().supportIncludes(roots) ? "${" + ProjectProperties.INCLUDES + "}" : null; // NOI18N
        final String excludes = RootsAccessor.getInstance().supportIncludes(roots) ? "${" + ProjectProperties.EXCLUDES + "}" : null; // NOI18N
        final String[] rootPathPropNames = RootsAccessor.getInstance().getRootPathProperties(roots);
        final String[] propNames = roots.getRootProperties();
        final String[] displayNames = roots.getRootDisplayNames();
        
        for (int i = 0; i < propNames.length; i++) {
            final String prop = propNames[i];
            final String pathProp =  rootPathPropNames[i];

            final List<String> locations;
            final List<String> names;
            final List<String[]> parts;
            
            if (pathProp == null || JavaProjectConstants.SOURCES_TYPE_MODULES.equals(type)) {
                locations = Collections.singletonList("${" + prop + "}"); // NOI18N
                names = Collections.singletonList(displayNames[i]);
                parts = Collections.singletonList(null);
            } else {
                locations = new ArrayList<>();
                names = new ArrayList<>();
                parts = new ArrayList<>();
                final String pathToModules = evaluator.getProperty(prop);
                if (pathToModules != null) {
                    final File file = helper.resolveFile(pathToModules);
                    if (file.isDirectory()) {
                        final Collection<? extends String> spVariants = Arrays.stream(PropertyUtils.tokenizePath(evaluator.getProperty(pathProp)))
                                .map((p) -> CommonModuleUtils.parseSourcePathVariants(p))
                                .flatMap((lv) -> lv.stream())
                                .collect(Collectors.toList());
                        for (File f : file.listFiles()) {
                            if (f.isDirectory()) {
                                for (String variant : spVariants) {
                                    final String resolvedSrcPath = String.format(
                                            "%s/%s/%s", //NOI18N
                                            pathToModules,
                                            f.getName(),
                                            variant);
                                    String[] v = variant.split("/");
                                    String[] p = new String[v.length + 2];
                                    // the most important is the module name
                                    p[0] = f.getName();
                                    // 2nd project part is the [resolved] path to modules; usually src.dir and test.dir point to the same location,
                                    // but if they do not, the 'same location' gets a preference.
                                    p[1] = pathToModules;
                                    System.arraycopy(v, 0, p, 2, v.length);
                                    locations.add(resolvedSrcPath); //Todo: Should be unevaluated
                                    parts.add(p);
                                    String dispName = displayNames[i];
                                    if (dispName == null || dispName.isEmpty()) {
                                        names.add(Bundle.FMT_ModularSourceRootNoName(f.getName(), pathToModules));
                                    } else {
                                        names.add(Bundle.FMT_ModularSourceRootWithName(f.getName(), variant, dispName));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            assert locations.size() == names.size();
            assert locations.size() == parts.size();
            Iterator<String[]> partIt = parts.iterator();
            for (Iterator<String> locationIt = locations.iterator(), nameIt = names.iterator(); locationIt.hasNext() && nameIt.hasNext();) {
                final SourcesHelper.SourceRootConfig cfg = sourcesHelper.sourceRoot(locationIt.next());
                cfg.displayName(nameIt.next());
                if (includes != null) {
                    cfg.includes(includes);
                }
                if (excludes != null) {
                    cfg.excludes(excludes);
                }
                if (hint != null) {
                    cfg.hint(hint);
                }
                cfg.inParts(partIt.next());
                cfg.add();  // principal root
                if (type != null) {
                    cfg.type(type).add();    // typed root
                }
            }
        }
    }

    private void registerNonSources(final SourcesHelper sourcesHelper, final Roots nonSources) {
        for (String nonSourceRootProp : nonSources.getRootProperties()) {
            sourcesHelper.addNonSourceRoot(String.format("${%s}", nonSourceRootProp));
        }
    }

    private void fireChange() {
        synchronized (this) {
            cachedGroups.clear();   //threading: CHM.clear is not atomic, the getSourceGroup may return staled data which is not a problem in this case.
            dirty = true;
        }
        ProjectManager.mutex().postReadRequest(fireTask.activate());
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        // was listening to PROP_ROOT_PROPERTIES, changed to PROP_ROOTS in #143633 as changes
        // from SourceGroupModifierImplementation need refresh too
        if (SourceRoots.PROP_ROOTS.equals(propName)) {
            this.fireChange();
        }
    }

    public void stateChanged (ChangeEvent event) {
        this.fireChange();
    }


    private class FireAction implements Runnable {

        private AtomicBoolean fire = new AtomicBoolean();

        public void run() {
            if (fire.getAndSet(false)) {
                changeSupport.fireChange();
            }
        }

        FireAction activate() {
            this.fire.set(true);
            return this;
        }
    };
}
