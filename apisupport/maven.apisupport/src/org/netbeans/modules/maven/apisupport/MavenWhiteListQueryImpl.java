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

package org.netbeans.modules.maven.apisupport;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.ElementKind;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.whitelist.WhiteListQuery;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.classpath.ProjectSourcesClassPathProvider;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.whitelist.WhiteListQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakSet;

/**
 *
 * @author mkleint
 */
@ProjectServiceProvider(service = WhiteListQueryImplementation.class, projectType="org-netbeans-modules-maven/" + NbMavenProject.TYPE_NBM)
public class MavenWhiteListQueryImpl implements WhiteListQueryImplementation {
    private final Project project;
    //per project caching, share across all project's whitelist results..
    private SoftReference<Set<String>> cachePrivatePackages;
    private SoftReference<Set<String>> cacheTransitivePackages;
    private final Object LOCK = new Object();
    private boolean isCached = false;
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final PropertyChangeListener projectListener;
    private static final RequestProcessor RP = new RequestProcessor(MavenWhiteListQueryImpl.class.getName(), 3);
    private static final Logger LOG = Logger.getLogger(MavenWhiteListQueryImpl.class.getName());

    private final Set<MavenWhiteListImplementation> results = Collections.synchronizedSet(new WeakSet<MavenWhiteListImplementation>());
    
    //TODO add static cache across projects for dependency jar's contents.
    
    public MavenWhiteListQueryImpl(Project prj) {
        project = prj;
        projectListener = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                //TODO listen just on changes of classpath??
                if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                    synchronized (LOCK) {
                        Set<String> oldPrivate = cachePrivatePackages != null ? cachePrivatePackages.get() : null;
                        if (oldPrivate == null) {
                            oldPrivate = Collections.emptySet();
                        }
                        Set<String> oldTransitive = cacheTransitivePackages != null ? cacheTransitivePackages.get() : null;
                        if (oldTransitive == null) {
                            oldTransitive = Collections.emptySet();
                        }
                        isCached = false;
                        cacheOrLoad();
                        Set<String> newPrivate = cachePrivatePackages != null ? cachePrivatePackages.get() : null;
                        if (newPrivate == null) {
                            newPrivate = Collections.emptySet();
                        }
                        Set<String> newTransitive = cacheTransitivePackages != null ? cacheTransitivePackages.get() : null;
                        if (newTransitive == null) {
                            newTransitive = Collections.emptySet();
                        }
                        Set<String> oldNotNew1 = new HashSet<>(oldPrivate);
                        oldNotNew1.removeAll(newPrivate);
                        Set<String> newNotOld1 = new HashSet<>(newPrivate);
                        newNotOld1.removeAll(oldPrivate);
                        Set<String> oldNotNew2 = new HashSet<>(oldTransitive);
                        oldNotNew2.removeAll(newTransitive);
                        Set<String> newNotOld2 = new HashSet<>(newTransitive);
                        newNotOld2.removeAll(oldTransitive);
                        
                        boolean privateChanged = !oldNotNew1.isEmpty() || !newNotOld1.isEmpty();
                        boolean transitiveChanged = !oldNotNew2.isEmpty() || !newNotOld2.isEmpty();
                        if (privateChanged || transitiveChanged) {
                            ClassPath[] cps = project.getLookup().lookup(ProjectSourcesClassPathProvider.class).getProjectClassPaths(ClassPath.SOURCE);
                            Set<FileObject> fos = new HashSet<FileObject>();
                            for (ClassPath cp : cps) {
                                fos.addAll(Arrays.asList(cp.getRoots()));
                            }
                            LOG.log(Level.INFO, "Refreshing indexes for {0} because {1}{2} changed.", new Object[]{project.getProjectDirectory(), privateChanged ? "accessible private packages, " : "", transitiveChanged ? "accessible transitive packages " : ""});
                            LOG.log(Level.FINE, "changes in private1-{0}", Arrays.toString(oldNotNew1.toArray()));
                            LOG.log(Level.FINE, "changes in private2-{0}", Arrays.toString(newNotOld1.toArray()));
                            LOG.log(Level.FINE, "changes in transitive1-{0}", Arrays.toString(oldNotNew2.toArray()));
                            LOG.log(Level.FINE, "changes in transitive2-{0}", Arrays.toString(newNotOld2.toArray()));
                            IndexingManager.getDefault().refreshAllIndices(fos.toArray(new FileObject[0]));
                        }
                    }
                }
            }
        };
    }
    
    @Override
    public WhiteListImplementation getWhiteList(FileObject file) {
        NbMavenProject mvn = project.getLookup().lookup(NbMavenProject.class);
        assert mvn != null;
        AuxiliaryProperties props = project.getLookup().lookup(AuxiliaryProperties.class);
        String disable = props.get("netbeans.hint.disable.whitelist", true);
        if (disable != null) {
            return null;
        }
        ProjectSourcesClassPathProvider prov = project.getLookup().lookup(ProjectSourcesClassPathProvider.class);
        assert prov != null;
        ClassPath sourceCp = prov.getProjectSourcesClassPath(ClassPath.SOURCE);
        //does not apply to test sources.
        if (!sourceCp.contains(file)) {
            return null;
        }     
        
        if (initialized.compareAndSet(false, true)) {
            //TODO listen to classpath changes only?
            mvn.addPropertyChangeListener(projectListener);
        }
        
        Tuple res = cacheOrLoad();
        
        MavenWhiteListImplementation val = new MavenWhiteListImplementation(res.privatePackages, res.transitivePackages);
        results.add(val);
        //System.out.println("added to results =" + results.size());
        return val;
    }
    
    private static final WhiteListQuery.RuleDescription PRIVATE_RD = new WhiteListQuery.RuleDescription("private", "Module dependency's private package referenced", null);
    private static final WhiteListQuery.RuleDescription TRANSITIVE_RD = new WhiteListQuery.RuleDescription("transitive", "Package from transitive module dependency referenced, declare a direct dependency to fix.", null);
    private static final WhiteListQuery.Result OK = new WhiteListQuery.Result();
    

    private Set<String> getAllPackages(FileObject root) {       
        Set<String> toRet = new HashSet<String>();
        processFolder(root, root, toRet);
        toRet.remove("");
        return toRet;
    }
    
    private void processFolder(FileObject root, FileObject folder, Set<String> foundPackages) {
        Enumeration<? extends FileObject> it = folder.getData(false);
        while (it.hasMoreElements()) {
            FileObject fileObject = it.nextElement();
            if (fileObject.hasExt("class")) {
                foundPackages.add(folder.getPath().replace('/', '.'));
                break;
            }
        }
        it = folder.getFolders(false);
        while (it.hasMoreElements()) {
            FileObject fileObject = it.nextElement();
            processFolder(root, fileObject, foundPackages);
        }
    }
    
    public static boolean isUseOSGiDependencies(Project project) {
        String useOsgiString = PluginBackwardPropertyUtils.getPluginProperty(project, "useOSGiDependencies", null, null);
        return  useOsgiString != null ? Boolean.parseBoolean(useOsgiString) : false;
    }
    

    private Tuple calculateLists() {
        //System.out.println("calculate for project=" + project.getProjectDirectory());
        boolean useOsgi = isUseOSGiDependencies(project);
        List<NBMWrapper> nbms = new ArrayList<NBMWrapper>();
        List<OSGIWrapper> osgis = new ArrayList<OSGIWrapper>();
        List<Wrapper> directCPs = new ArrayList<Wrapper>();
        List<Wrapper> unknown = new ArrayList<Wrapper>();
        NbMavenProject mvn = project.getLookup().lookup(NbMavenProject.class);
        
        MavenProject mp = mvn.getMavenProject();
        final Set<String> privatePackages = new HashSet<String>();
        final Set<String> transitivePackages = new HashSet<String>();
                
        for (Artifact a : mp.getCompileArtifacts()) {
            if (a.getFile() != null) {
                FileObject fo = FileUtil.toFileObject(a.getFile());
                if (fo != null && FileUtil.isArchiveFile(fo)) {
                    FileObject root = FileUtil.getArchiveRoot(fo);
                    Manifest mf = getManifest(root);

                    if (mf != null && mf.getMainAttributes() != null) {
                        Attributes attrs = mf.getMainAttributes();
                        String osgiexport = attrs.getValue("Export-Package");
                        String osgiprivate = attrs.getValue("Private-Package");
                        String nbmexport = attrs.getValue("OpenIDE-Module-Public-Packages");
                        Set<String> allpackages = getAllPackages(root);
                        if (nbmexport != null) {
                            String nbmMaven = attrs.getValue("Maven-Class-Path"); //modules built with maven with external libs
                            String friends = attrs.getValue("OpenIDE-Module-Friends");
                            nbms.add(new NBMWrapper(a, allpackages, nbmexport.equals("-") ? null : StringUtils.split(nbmexport, ","),
                                    friends != null ? StringUtils.split(friends, ",") : null,
                                    nbmMaven != null ? StringUtils.split(nbmMaven, " ") : null));
                        } else if (useOsgi && osgiexport != null) {
                            //TODO
                        } else {
                            if (a.getDependencyTrail() != null && a.getDependencyTrail().size() > 2) {
                                unknown.add(new Wrapper(a, allpackages));
                            } else {
                                //direct dependencies are part of the module's CP entirely..
                                directCPs.add(new Wrapper(a, allpackages));
                            }
                        }
                    }
                }
            }
        }
        
        List<ExplicitDependency> explicits = PluginBackwardPropertyUtils.getPluginPropertyBuildable(project, null, new ExplicitBuilder());
//        String codenamebase = PluginPropertyUtils.getPluginProperty(project, MavenNbModuleImpl.GROUPID_MOJO, MavenNbModuleImpl.NBM_PLUGIN, "codeNameBase", null, null);
//        if (codenamebase == null) {
//            codenamebase = mp.getGroupId() + "." + mp.getArtifactId();
//        }
        //these two are here to remove duplicates, if a package is both private (in one module) and public (in another module)
        // consider the package public for our purposes. better a false negative than false positive here..
        Set<String> nonPrivatePackages = new HashSet<String>();
        Set<String> nonTransitivePackages = new HashSet<String>();
        //direct cp is always visible..
        for (Wrapper dir : directCPs) {
            nonTransitivePackages.addAll(dir.allPackages);
            nonPrivatePackages.addAll(dir.allPackages);
        }
        directCPs.clear();
        for (NBMWrapper nbm : nbms ) {
            Set<String> allPackages = new HashSet<String>(nbm.allPackages);
            //merge unknowns into their respective wrapper modules..
            if (nbm.hasMavenCPDefined()) {
                Iterator<Wrapper> it = unknown.iterator();
                while (it.hasNext()) {
                    Wrapper wrapper = it.next();
                    if (nbm.hasOnClassPath(wrapper.art)) {
                        nbm.wrappedLibs.add(wrapper.art); //TODO do we want to modify the nbm wrapper  at this point?
                        allPackages.addAll(wrapper.allPackages);
                        //it.remove(); cannot remove, sometimes multiple nbms reference the same jar, and some could make it's packages public and some could make them private..
                    }
                }
            }
            
            if (nbm.art.getDependencyTrail() != null && nbm.art.getDependencyTrail().size() > 2) {
                //transitive dependency - TODO 
                transitivePackages.addAll(allPackages);
            } else {
                nonTransitivePackages.addAll(allPackages);
            }
            
            //we need to check the explicit dependencies for implementation deps, in that case all packages are public
            if (explicits != null) {
                for (ExplicitDependency ex : explicits) {
                    if (ex.matches(nbm.art) && ex.isImplementationDependency()) {
                        //we got impl dep, none of the packages are private.
                        nonPrivatePackages.addAll(allPackages);
                    }
                }
            }
            
            for (String p : allPackages) {
                if (nbm.isPublicPackage(p)) {
                    nonPrivatePackages.add(p);
                } else {
                    privatePackages.add(p);
                }
            }
        }
        nbms.clear();
        
        //now remove all packages from bootclasspath that clash with private/transitive packages..
        // happens for javax.swing for example which is part of the tabcontrol module
        ClassPath boot = project.getLookup().lookup(ProjectSourcesClassPathProvider.class).getProjectSourcesClassPath(ClassPath.BOOT);
        Set<String> bootCP = new HashSet<String>();
        for (FileObject fo : boot.getRoots()) {
            bootCP.addAll(getAllPackages(fo));
        }
        transitivePackages.removeAll(bootCP);
        privatePackages.removeAll(bootCP);
        
        //remove all duplicates. only keep the privates we are 100% positive about..
        transitivePackages.removeAll(nonTransitivePackages);
        privatePackages.removeAll(nonPrivatePackages);
        return new Tuple(privatePackages, transitivePackages);
    }

    private void fireChangeAllExistingResults(final Set<String> privatePackages, final Set<String> transitivePackages) {
        assert Thread.holdsLock(LOCK);
        final Set<MavenWhiteListImplementation> set;
        synchronized (results) {
            set = new HashSet<>(results);
        }
        RP.post(new Runnable() {
            @Override
            public void run() {
                for (MavenWhiteListImplementation res : set) {
                    if (res != null) {
                        res.changeData(privatePackages, transitivePackages);
                    }
                }
            }
        });
    }

    private Tuple cacheOrLoad() {
        //compute the effective, known "private" packages that should not be accessible from the file.

        synchronized (LOCK) {
            if (isCached) {
                Set<String> set1 = cachePrivatePackages != null ? cachePrivatePackages.get() : null;
                Set<String> set2 = cacheTransitivePackages != null ? cacheTransitivePackages.get() : null;
                if (set1 != null && set2 != null) {
                    return new Tuple(set1, set2);
                }
            }
            Tuple tup = calculateLists();
            cachePrivatePackages = new SoftReference<Set<String>>(tup.privatePackages);
            cacheTransitivePackages = new SoftReference<Set<String>>(tup.transitivePackages);
            isCached = true;
            fireChangeAllExistingResults(tup.privatePackages, tup.transitivePackages);
            return tup;
        }
    }

    private Manifest getManifest(FileObject root) {
        FileObject manifestFo = root.getFileObject("META-INF/MANIFEST.MF");
        if (manifestFo != null) {
            try (InputStream is = manifestFo.getInputStream()) {
                Manifest manifest = new Manifest(is);
                return manifest;
            } catch (IOException ex) {
                //Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }
    
    private static class MavenWhiteListImplementation implements WhiteListImplementation {
        private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
        @NonNull
        private Set<String> privatePackages;
        @NonNull
        private Set<String> transitivePackages;
        private final Object IMPL_LOCK = new Object();

        private MavenWhiteListImplementation(@NonNull Set<String> privatePackages, @NonNull Set<String> transitivePackages) {
            this.privatePackages = privatePackages;
            this.transitivePackages = transitivePackages;
        }

        @Override
        public WhiteListQuery.Result check(ElementHandle<?> element, WhiteListQuery.Operation operation) {
            if (!operation.equals(WhiteListQuery.Operation.USAGE)) {
                return OK;
            }
            List<WhiteListQuery.RuleDescription> rds = new ArrayList<WhiteListQuery.RuleDescription>();
            if (element.getKind() == ElementKind.CLASS || element.getKind() == ElementKind.INTERFACE) {
                String qn = element.getQualifiedName();
                if (qn.lastIndexOf('.') > 0) {
                    String pack = qn.substring(0, qn.lastIndexOf("."));
                    synchronized (IMPL_LOCK) {
                        if (privatePackages.contains(pack)) {
                            rds.add(PRIVATE_RD);
                        }
                        if (transitivePackages.contains(pack)) {
                            rds.add(TRANSITIVE_RD);
                        }
                    }
                    if (!rds.isEmpty()) {
                        return new WhiteListQuery.Result(rds);
                    }
                }
            }
            return OK;
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            synchronized (listeners) {
                listeners.add(listener);
            }
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            synchronized (listeners) {
                listeners.remove(listener);
            }
        }
        
        public void changeData(@NonNull Set<String> privatePackages, @NonNull Set<String> transitivePackages) {
            synchronized (IMPL_LOCK) {
                this.privatePackages = privatePackages;
                this.transitivePackages = transitivePackages;
            }
            ArrayList<ChangeListener> changes = new ArrayList<ChangeListener>();
            synchronized (listeners) {
                changes.addAll(listeners);
            }
            for (ChangeListener change : changes) {
                change.stateChanged(new ChangeEvent(this));
            }
        }
        
    }
    
    private static class Tuple {
        Set<String> privatePackages;
        Set<String> transitivePackages;

        public Tuple(Set<String> privatePackages, Set<String> transitivePackages) {
            this.privatePackages = privatePackages;
            this.transitivePackages = transitivePackages;
        }

        
    }


    private static class Wrapper {
        final Artifact art;
        final Set<String> allPackages;

        public Wrapper(Artifact art, Set<String> allPackages) {
            this.art = art;
            this.allPackages = allPackages;
        }
        
    }
    
    private static class OSGIWrapper extends Wrapper {
        final String[] exports;

        public OSGIWrapper( Artifact art, Set<String> allPackages, String[] exports) {
            super(art, allPackages);
            this.exports = exports;
        }
        
    }
    
    private static class NBMWrapper extends Wrapper {
        final String[] publicPackages;
        final List<Artifact> wrappedLibs = new ArrayList<Artifact>();
        boolean isImplementationDependency;
        final List<String> friends;
        final List<String> mavenCP;
        private final Set<String> eqPublic = new HashSet<String>();
        private final Set<String> subPublic = new HashSet<String>();

        public NBMWrapper(Artifact art, Set<String> allPackages, String[] publicPackages, String[] friends, String[] mavenCP) {
            super(art, allPackages);
            this.friends = friends != null ? Arrays.asList(friends) : Collections.<String>emptyList();
            this.mavenCP = mavenCP != null ? Arrays.asList(mavenCP) : Collections.<String>emptyList();
            Set<String> packs = new HashSet<String>();
            if (publicPackages == null) {
                //no public packages.
            } else {
                for (String pub : publicPackages) {
                    pub = pub.trim();
                    packs.add(pub);
                    if (pub.endsWith(".**")) {
                        String sub = pub.substring(0, pub.length() - ".**".length());
                        subPublic.add(sub);
                    } else if (pub.endsWith(".*")) {
                        String eq = pub.substring(0, pub.length() - ".*".length());
                        eqPublic.add(eq);
                    }
                }
            }
            this.publicPackages = packs.toArray(new String[0]);
        }

        boolean isFriend(String codenamebase) {
            return friends.contains(codenamebase);
        }
        
        boolean hasFriendAPI() {
            return !friends.isEmpty();
        }
        
        boolean hasOnClassPath(Artifact art) {
            //construct ID as we do in NetbeansManifestUpdateMojo
            String id = art.getGroupId() + ":" + art.getArtifactId() + ":" + art.getBaseVersion() + (art.getClassifier() != null ? ":" + art.getClassifier() : "");
             return mavenCP.contains(id);
        }
        
        boolean hasMavenCPDefined() {
            return !mavenCP.isEmpty();
        }
        
        boolean isPublicPackage(String pack) {
            if (eqPublic.contains(pack)) {
                return true;
            }
            for (String suString : subPublic) {
                if (pack.startsWith(suString)) {
                    return true;
                }
            }
            return false;
        }
        
    }
    
    //model for http://mojo.codehaus.org/nbm-maven/nbm-maven-plugin/manifest-mojo.html#moduleDependencies
    private static class ExplicitDependency {
        String id;
        String explicit;
        String type;
        
        boolean matches(Artifact art) {
            return id != null && id.equals(art.getGroupId() + ":" +  art.getArtifactId());
        }
        
        boolean isImplementationDependency() {
            return (explicit != null && explicit.contains("=")) || ("impl".equals(type));
        }
    }
    
    private static class ExplicitBuilder implements PluginPropertyUtils.ConfigurationBuilder<List<ExplicitDependency>> {

        @Override
        public List<ExplicitDependency> build(Xpp3Dom configRoot, ExpressionEvaluator eval) {
            if (configRoot != null) {
                Xpp3Dom list = configRoot.getChild("moduleDependencies");
                if (list != null) {
                    List<ExplicitDependency> toRet = new ArrayList<ExplicitDependency>();
                    Xpp3Dom[] childs = list.getChildren("moduleDependency");
                    for (Xpp3Dom ch : childs) {
                        Xpp3Dom idDom = ch.getChild("id"); //NOI18N
                        Xpp3Dom typeDom = ch.getChild("type"); //NOI18N
                        Xpp3Dom explicitDom = ch.getChild("explicitValue"); //NOI18N
                        if (idDom != null && (typeDom != null || explicitDom != null)) {
                            String id = idDom.getValue();
                            String type = typeDom != null ? typeDom.getValue() : null;
                            String explicit = explicitDom != null ? explicitDom.getValue() : null;
                            if (id != null && (type != null || explicit != null)) {
                                try {
                                    Object evaluated = eval.evaluate(id);
                                    if (evaluated != null) {
                                        id = evaluated.toString();
                                    }
                                } catch (ExpressionEvaluationException ex) {
                                }
                                if (type != null) {
                                    try {
                                        Object evaluated = eval.evaluate(type);
                                        if (evaluated != null) {
                                            type = evaluated.toString();
                                        }
                                    } catch (ExpressionEvaluationException ex) {
                                    }
                                }
                                if (explicit != null) {
                                    try {
                                        Object evaluated = eval.evaluate(explicit);
                                        if (evaluated != null) {
                                            explicit = evaluated.toString();
                                        }
                                    } catch (ExpressionEvaluationException ex) {
                                    }
                                }
                                ExplicitDependency ed = new ExplicitDependency();
                                ed.id = id;
                                ed.type = type;
                                ed.explicit = explicit;
                                toRet.add(ed);
                            }
                        }
                    }
                    return toRet;
                }
            }
            return null;
        }
        
    }
}
