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
/*
 * Contributor(s): theanuradha@netbeans.org
 */
package org.netbeans.modules.maven.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.model.InputLocation;
import org.apache.maven.model.InputSource;
import org.apache.maven.model.building.ModelProblem;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.SuppressWarnings;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import static org.netbeans.modules.maven.model.Utilities.openAtPosition;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.DependencyManagement;
import org.netbeans.modules.maven.model.pom.POMComponent;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.PluginManagement;
import org.netbeans.modules.maven.model.pom.Project;
import org.netbeans.modules.maven.model.pom.Repository;
import org.netbeans.modules.maven.options.MavenSettings;
import org.netbeans.modules.maven.options.MavenVersionSettings;
import org.netbeans.modules.maven.spi.nodes.NodeUtils;
import org.openide.cookies.EditCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;

/**
 * Various maven model related utilities.
 * @author mkleint
 * @author Anuradha G
 */
public final class ModelUtils {
    private static final Logger LOG = Logger.getLogger(ModelUtils.class.getName());

    /**
     * library descriptor property containing whitespace separated list of maven coordinate values groupid:artifactId:version:[classifier:]type
     */
    public static final String LIBRARY_PROP_DEPENDENCIES = "maven-dependencies";
    /**
     * library descriptor property containing whitespace separated list of maven coordinate values repo_type:repo_url
     */
    public static final String LIBRARY_PROP_REPOSITORIES = "maven-repositories";

    private ModelUtils() {}

    /**
     * 
     * @param pom       FileObject that represents POM
     * @param group     
     * @param artifact
     * @param version
     * @param type
     * @param scope
     * @param classifier
     * @param acceptNull accept null values to scope,type and classifier.
     *                   If true null values will remove corresponding tag.
     */
    public static void addDependency(FileObject pom,
            final String group,
            final String artifact,
            final String version,
            final String type,
            final String scope,
            final String classifier, final boolean acceptNull)
    {
        ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
            private static final String BUNDLE_TYPE = "bundle"; //NOI18N
            @Override
            public void performOperation(POMModel model) {
                Dependency dep = checkModelDependency(model, group, artifact, true);
                dep.setVersion(version);
                if (acceptNull || scope != null) {
                    dep.setScope(scope);
                }
                if (acceptNull || (type != null && !BUNDLE_TYPE.equals(type))) {
                    dep.setType(type);
                }
                if (acceptNull || classifier != null) {
                    dep.setClassifier(classifier);
                }
            }
        };
        Utilities.performPOMModelOperations(pom, List.of(operation));
    }

    public static Dependency checkModelDependency(POMModel pom, String groupId, String artifactId, boolean add) {
        Project mdl = pom.getProject();
        Dependency ret = mdl.findDependencyById(groupId, artifactId, null);
        Dependency managed = null;
        if (ret == null || ret.getVersion() == null) {
            //check dependency management section as well..
            DependencyManagement mng = mdl.getDependencyManagement();
            if (mng != null) {
                managed = mng.findDependencyById(groupId, artifactId, null);
            }
        }
        if (add && ret == null) {
            ret = mdl.getModel().getFactory().createDependency();
            ret.setGroupId(groupId);
            ret.setArtifactId(artifactId);
            mdl.addDependency(ret);
        }
        // if managed dependency section is present, return that one for editing..
        return managed == null ? ret : managed;
    }


    public static boolean hasModelDependency(POMModel mdl, String groupid, String artifactid) {
        return checkModelDependency(mdl, groupid, artifactid, false) != null;
    }
    
    /**
     * Opens a pom file at location defined in InputLocation parameter
     * @since 2.77
     * @param location 
     */
    public static void openAtSource(InputLocation location) {
        InputSource source = location.getSource();
        if (source != null && source.getLocation() != null) {
            FileObject fobj = FileUtilities.convertStringToFileObject(source.getLocation());
            if (fobj != null) {
                try {
                    DataObject dobj = DataObject.find(NodeUtils.readOnlyLocalRepositoryFile(fobj));
                    EditCookie edit = dobj.getLookup().lookup(EditCookie.class);
                    if (edit != null) {
                        edit.edit();
                    }
                    LineCookie lc = dobj.getLookup().lookup(LineCookie.class);
                    lc.getLineSet().getOriginal(location.getLineNumber() - 1).show(Line.ShowOpenType.REUSE, Line.ShowVisibilityType.FOCUS, location.getColumnNumber() - 1);
                } catch (DataObjectNotFoundException ex) {
                    LOG.log(Level.FINE, "dataobject not found", ex);
                }
            }
        }
    }    

    /**
     * Opens pom at a plugin with the given groupId and artifactId.
     * 
     * @param model the model to open
     * @param groupId the plugin groupId
     * @param artifactId the plugin artifactId
     */
    public static void openAtPlugin(POMModel model, String groupId, String artifactId) {
        int pos = -1;
        org.netbeans.modules.maven.model.pom.Project p = model.getProject();
        Build bld = p.getBuild();
        if (bld != null) {
            Plugin plg = bld.findPluginById(groupId, artifactId);
            if (plg != null) {
                pos = plg.findPosition();
            }
        }    

        if(pos == -1) {
            pos = p.findPosition();
        }

        if(pos == -1) {
            return;
        }        
        openAtPosition(model, pos);
    }
    
    public static void updatePluginVersion(String groupId, String artifactId, String version, org.netbeans.modules.maven.model.pom.Project prj) {
        Build bld = prj.getBuild();
        boolean setInPM = false;
        boolean setInPlgs = false;

        if (bld != null) {
            PluginManagement pm = bld.getPluginManagement();
            if (pm != null) {
                Plugin p = pm.findPluginById(groupId, artifactId);
                if (p != null) {
                    p.setVersion(version);
                    setInPM = true;
                }
            }
            Plugin p = bld.findPluginById(groupId, artifactId);
            if (p != null) {
                if (p.getVersion() != null) {
                    p.setVersion(version);
                    setInPlgs = true;
                } else {
                    if (!setInPM) {
                        p.setVersion(version);
                        setInPlgs = true;
                    }
                }
            }
        }
        if (!setInPM && !setInPlgs) {
            if (bld == null) {
                bld = prj.getModel().getFactory().createBuild();
                prj.setBuild(bld);
            }
            Plugin p = prj.getModel().getFactory().createPlugin();
            p.setGroupId(groupId);
            p.setArtifactId(artifactId);
            p.setVersion(version);
            bld.addPlugin(p);
        }
    }
    
    /**
     *
     * @param mdl
     * @param url of the repository
     * @return null if repository with given url exists, otherwise a returned newly created item.
     */
    public static Repository addModelRepository(MavenProject project, POMModel mdl, String url) {
        if (url.contains(RepositorySystem.DEFAULT_REMOTE_REPO_URL) || /* #212336 */url.contains("https://repo1.maven.org/maven2")) {
            return null;
        }
        List<Repository> repos = mdl.getProject().getRepositories();
        if (repos != null) {
            for (Repository r : repos) {
                if (url.equals(r.getUrl())) {
                    //already in model..either in pom.xml or added in this session.
                    return null;
                }
            }
        }
        
        List<org.apache.maven.model.Repository> reps = project.getRepositories();
        org.apache.maven.model.Repository prjret = null;
        Repository ret = null;
        if (reps != null) {
            for (org.apache.maven.model.Repository re : reps) {
                if (url.equals(re.getUrl())) {
                    prjret = re;
                    break;
                }
            }
        }
        if (prjret == null) {
            ret = mdl.getFactory().createRepository();
            ret.setUrl(url);
            ret.setId(url);
            mdl.getProject().addRepository(ret);
        }
        return ret;
    }

    public static boolean checkByCLIMavenValidationLevel(ModelProblem problem) {
        // XXX HACK - this should be properly solved by upgrading the embeded maven
        String version = MavenSettings.getCommandLineMavenVersion();        
        try {
            if ( version != null && !"".equals(version.trim()) && 
                 new DefaultArtifactVersion(version).compareTo(new DefaultArtifactVersion("3.2.1")) > 0) 
            {
                if ( (problem.getMessage().startsWith("'dependencies.dependency.exclusions.exclusion.groupId' for ") ||
                      problem.getMessage().startsWith("'dependencies.dependency.exclusions.exclusion.artifactId' for "))
                        && problem.getMessage().contains(" with value '*' does not match a valid id pattern")) 
                {
                    return false;
                }
            }
        } catch (Throwable e) {
            // ignore and be optimistic about the hint
            LOG.log(Level.INFO, version, e);
        }
        return true;
    }
    
    /**
     * Sets the Java source and target level of a project (will set release if previously set).
     * Use {@link PluginPropertyUtils#getPluginProperty(org.netbeans.api.project.Project,String,String,String,String,String)} first
     * ({@link Constants#GROUP_APACHE_PLUGINS}, {@link Constants#PLUGIN_COMPILER}, {@link Constants#SOURCE_PARAM}, {@code "compile"})
     * to make sure that the current level is actually not what you want.
     * 
     * Please Note: This method will not take existing properties into account (maven.compiler.source, maven.compiler.target or maven.compiler.release),
     * it is only updating the plugin configuration itself.
     * @param mdl a POM model
     * @param sourceLevel the desired source level
     * @since 2.19
     */
    public static void setSourceLevel(POMModel mdl, String sourceLevel) {
        Plugin old = null;
        Plugin plugin;
        Build bld = mdl.getProject().getBuild();
        if (bld != null) {
            old = bld.findPluginById(Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER);
        } else {
            mdl.getProject().setBuild(mdl.getFactory().createBuild());
        }
        if (old != null) {
            plugin = old;
        } else {
            plugin = mdl.getFactory().createPlugin();
            plugin.setGroupId(Constants.GROUP_APACHE_PLUGINS);
            plugin.setArtifactId(Constants.PLUGIN_COMPILER);
            plugin.setVersion(MavenVersionSettings.getDefault().getVersion(Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER));
            mdl.getProject().getBuild().addPlugin(plugin);
        }
        Configuration conf = plugin.getConfiguration();
        if (conf == null) {
            conf = mdl.getFactory().createConfiguration();
            plugin.setConfiguration(conf);
        }
        if (conf.getSimpleParameter(Constants.RELEASE_PARAM) != null) {
            conf.setSimpleParameter(Constants.RELEASE_PARAM, sourceLevel);
            conf.setSimpleParameter(Constants.SOURCE_PARAM, null);
            conf.setSimpleParameter(Constants.TARGET_PARAM, null);
        } else {
            conf.setSimpleParameter(Constants.SOURCE_PARAM, sourceLevel);
            conf.setSimpleParameter(Constants.TARGET_PARAM, sourceLevel);
        }
        
    }

    /**
     * Returns child element of given parent, specified by its local name.
     * Creates such child in case it doesn't exist.
     *
     * @param parent parent element
     * @param localQName local name of the child
     * @param pomModel whole pom model
     * @return existing or newly created child
     */
    public static POMExtensibilityElement getOrCreateChild (POMComponent parent, String localQName, POMModel pomModel) {
        POMExtensibilityElement result = null;
        for (POMExtensibilityElement el : parent.getExtensibilityElements()) {
            if (localQName.equals(el.getQName().getLocalPart())) {
                result = el;
                break;
            }
        }

        if (result == null) {
            result = pomModel.getFactory().
                    createPOMExtensibilityElement(new QName(localQName));
            parent.addExtensibilityElement(result);
        }

        return result;
    }

    private static final String PROBABLE_ROOTS
            = "maven2|" // mainly for Central
            + "maven[.]repo|" // often used for Eclipse repos
            + "content/(?:groups|repositories|shadows)/[^/]+|" // Nexus
            + ".+(?=/(?:javax|org|net|com)/)"; // common groupId starters
    /**
     * 1 - root
     * 2 - groupId as slashes
     * 3 - artifactId
     * 4 - version
     */
    private static Pattern DEFAULT = Pattern.compile("(.+://[^/]+/(?:(?:.+/)?(?:" + PROBABLE_ROOTS + ")/)?)(.+)/([^/]+)/([^/]+)/\\3-\\4[.]pom");
    /**
     * 1 - root
     * 2 - groupId
     * 3 - artifactId
     * 4 - version
     */
    private static Pattern LEGACY = Pattern.compile("(.+/)([^/]+)/poms/([a-zA-Z0-9_]+[a-zA-Z_-]+)-([0-9].+)[.]pom");

    /** Returns a library descriptor corresponding to the given library,
     * or null if not recognized successfully.
     *
     * @param pom library to check
     * @return LibraryDescriptor corresponding to the library, or null if the pom URL format is not recognized.
     * @deprecated  in favor of <code>checkLibraries(Library)</code>
     */
    @SuppressWarnings("SBSC_USE_STRINGBUFFER_CONCATENATION")
    @Deprecated
    public static LibraryDescriptor checkLibrary(URL pom) {
        String pomS;
        try {
            pomS = new URL(pom.getProtocol(), pom.getHost(), pom.getPort(), pom.getFile()).toString(); // strip ref
        } catch (MalformedURLException x) {
            pomS = pom.toString();
        }
        Matcher m1 = LEGACY.matcher(pomS);
        if (m1.matches()) {
            return new LibraryDescriptor("legacy", m1.group(1), m1.group(2), m1.group(3), m1.group(4), pom.getRef(), "jar");
        }
        Matcher m2 = DEFAULT.matcher(pomS);
        if (m2.matches()) {
            return new LibraryDescriptor("default", m2.group(1), m2.group(2).replace('/', '.'), m2.group(3), m2.group(4), pom.getRef(), "jar");
        }
        return null;
    }
    
    /**
     * return a descriptor for entire <code>Library</code> containing recognized dependencies and repositories,
     * both in old (volume) and new (properties) format
     * @param library
     * @return 
     */
    public static Descriptor checkLibraries(Library library) {
        return checkLibraries(library.getProperties());
    }
    //for tests
    static Descriptor checkLibraries(Map<String, String> properties) {
        List<LibraryDescriptor> libs = new ArrayList<>();
        List<RepositoryDescriptor> reps = new ArrayList<>();
                
        String dependencies = properties.get(LIBRARY_PROP_DEPENDENCIES);
        if (dependencies != null) {
            //http://www.netbeans.org/ns/library-declaration/3
            for (String dep : dependencies.split("([\\s])+")) {
                String[] v = dep.trim().split(":");
                if (v.length < 4) {
                    //TODO log.
                    continue;
                }
                String grid = v[0];
                String art = v[1];
                String ver = v[2];
                String type = v.length == 4 ? v[3] : v[4];
                String classifier = v.length >= 5 ? v[3] : null;
                libs.add(new LibraryDescriptor(null, null, grid, art, ver, classifier, type));
            }
            String repositories = properties.get(LIBRARY_PROP_REPOSITORIES);
            if (repositories != null) {
                for (String r : repositories.split("([\\s])+")) {
                    String[] rep = r.split(":", 2);
                    if (rep.length < 2) {
                        //TODO log.
                        continue;
                    }
                    reps.add(new RepositoryDescriptor(rep[0], rep[1]));
                }
            }
        }        
        return new Descriptor(libs, reps);
    }
    
    public static final class Descriptor {
        final List<LibraryDescriptor> dependencies;
        final List<RepositoryDescriptor> repositories;

        Descriptor(@NonNull List<LibraryDescriptor> dependencies, @NonNull List<RepositoryDescriptor> repositories) {
            this.dependencies = dependencies;
            this.repositories = repositories;
        }

        public List<LibraryDescriptor> getDependencies() {
            return dependencies;
        }

        public List<RepositoryDescriptor> getRepositories() {
            return repositories;
        }
    }
    
    public static final class RepositoryDescriptor {

        private final String repoType /* default/legacy */, repoRoot;

        RepositoryDescriptor(String repoType, String repoRoot) {
            this.repoType = repoType;
            this.repoRoot = repoRoot;
        }

        public String getRepoType() {
            return repoType;
        }

        public String getRepoRoot() {
            return repoRoot;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 37 * hash + (this.repoType != null ? this.repoType.hashCode() : 0);
            hash = 37 * hash + (this.repoRoot != null ? this.repoRoot.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final RepositoryDescriptor other = (RepositoryDescriptor) obj;
            if ((this.repoType == null) ? (other.repoType != null) : !this.repoType.equals(other.repoType)) {
                return false;
            }
            if ((this.repoRoot == null) ? (other.repoRoot != null) : !this.repoRoot.equals(other.repoRoot)) {
                return false;
            }
            return true;
        }
        
    }
    

    public static final class LibraryDescriptor {
        private final String repoType /* default/legacy */, repoRoot, groupId, artifactId,
                version, classifier /* optional, not part of path, but url's ref */, type;

        LibraryDescriptor(String repoType, String repoRoot, String groupId, String artifactId, String version, String classifier, String type) {
            this.repoType = repoType;
            this.repoRoot = repoRoot;
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.classifier = classifier;
            this.type = type;
        }

        public String getArtifactId() {
            return artifactId;
        }

        /** May return null. */
        public String getClassifier() {
            return classifier;
        }

        public String getGroupId() {
            return groupId;
        }

        @Deprecated
        public String getRepoRoot() {
            return repoRoot;
        }
        @Deprecated
        public String getRepoType() {
            return repoType;
        }

        public String getVersion() {
            return version;
        }
        
        public String getType() {
            return type;
        }

        @Override public String toString() {
            return "LibraryDescriptor{" + "repoType=" + repoType + ", repoRoot=" + repoRoot + ", groupId=" + groupId + ", artifactId=" + artifactId + ", version=" + version + ", classifier=" + classifier + ", type=" + type + '}';
        }

    }

}
