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

package org.netbeans.modules.maven.navigator;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.xml.namespace.QName;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.maven.model.pom.Activation;
import org.netbeans.modules.maven.model.pom.ActivationCustom;
import org.netbeans.modules.maven.model.pom.ActivationFile;
import org.netbeans.modules.maven.model.pom.ActivationOS;
import org.netbeans.modules.maven.model.pom.ActivationProperty;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.BuildBase;
import org.netbeans.modules.maven.model.pom.CiManagement;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.Contributor;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.DependencyContainer;
import org.netbeans.modules.maven.model.pom.DependencyManagement;
import org.netbeans.modules.maven.model.pom.DeploymentRepository;
import org.netbeans.modules.maven.model.pom.Developer;
import org.netbeans.modules.maven.model.pom.DistributionManagement;
import org.netbeans.modules.maven.model.pom.Exclusion;
import org.netbeans.modules.maven.model.pom.Extension;
import org.netbeans.modules.maven.model.pom.IssueManagement;
import org.netbeans.modules.maven.model.pom.License;
import org.netbeans.modules.maven.model.pom.MailingList;
import org.netbeans.modules.maven.model.pom.ModelList;
import org.netbeans.modules.maven.model.pom.Notifier;
import org.netbeans.modules.maven.model.pom.Organization;
import org.netbeans.modules.maven.model.pom.POMComponent;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMQName;
import org.netbeans.modules.maven.model.pom.POMQNames;
import org.netbeans.modules.maven.model.pom.Parent;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.PluginExecution;
import org.netbeans.modules.maven.model.pom.PluginManagement;
import org.netbeans.modules.maven.model.pom.Prerequisites;
import org.netbeans.modules.maven.model.pom.Profile;
import org.netbeans.modules.maven.model.pom.Project;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.modules.maven.model.pom.ReportPlugin;
import org.netbeans.modules.maven.model.pom.ReportSet;
import org.netbeans.modules.maven.model.pom.Reporting;
import org.netbeans.modules.maven.model.pom.Repository;
import org.netbeans.modules.maven.model.pom.RepositoryPolicy;
import org.netbeans.modules.maven.model.pom.Resource;
import org.netbeans.modules.maven.model.pom.Scm;
import org.netbeans.modules.maven.model.pom.Site;
import org.netbeans.modules.maven.model.pom.StringList;
import static org.netbeans.modules.maven.navigator.Bundle.*;
import org.netbeans.modules.xml.xam.Model;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author mkleint
 */
public class POMModelVisitor implements org.netbeans.modules.maven.model.pom.POMComponentVisitor {
    private static final @StaticResource String VALUE = "org/netbeans/modules/maven/navigator/value.png";
    private static final @StaticResource String VALUE2 = "org/netbeans/modules/maven/navigator/value2.png";
    private static final @StaticResource String VALUE3 = "org/netbeans/modules/maven/navigator/value3.png";
    private static final @StaticResource String VALUE4 = "org/netbeans/modules/maven/navigator/value4.png";

    private static final Logger LOG = Logger.getLogger(POMModelVisitor.class.getName());
    private Map<String, POMCutHolder> childs = new LinkedHashMap<String, POMCutHolder>();
    private int count = 0;
    private POMModelPanel.Configuration configuration;
    private POMCutHolder parent;

    POMModelVisitor(POMCutHolder parent, POMModelPanel.Configuration configuration) {
        this.parent = parent;
        this.configuration = configuration;
    }

    POMCutHolder[] getChildValues() {
        List<POMCutHolder> toRet = new ArrayList<POMCutHolder>();
        toRet.addAll(childs.values());
        return toRet.toArray(new POMCutHolder[0]);
    }

    @Override
    @Messages({"MODEL_VERSION=Model Version", "GROUPID=GroupId", "ARTIFACTID=ArtifactId", 
        "PACKAGING=Packaging", "NAME=Name", "VERSION=Version", "DESCRIPTION=Description", 
        "URL=Url", "PREREQUISITES=Prerequisites", "ISSUEMANAGEMENT=IssueManagement", "CIMANAGEMENT=CiManagement", 
        "INCEPTION_YEAR=Inception Year", "MAILING_LISTS=Mailing Lists", "MAILING_LIST=Mailing List", 
        "DEVELOPERS=Developers", "DEVELOPER=Developer", "CONTRIBUTORS=Contributors", "CONTRIBUTOR=Contributor",
        "LICENSES=Licenses", "LICENSE=License", "SCM=Scm", "ORGANIZATION=Organization", "BUILD=Build",
        "PROFILES=Profiles", "PROFILE=Profile", "REPOSITORIES=Repositories", "REPOSITORY=Repository", 
        "PLUGIN_REPOSITORIES=Plugin Repositories", "REPORTING=Reporting", "DEPENDENCY_MANAGEMENT=Dependency Management", 
        "DISTRIBUTION_MANAGEMENT=Distribution Management", "PROPERTIES=Properties", "MODULES=Modules", "MODULE=Module"})
    public void visit(Project target) {
        Project t = target;
        if (t != null && (!t.isInDocumentModel() || !t.getModel().getState().equals(Model.State.VALID))) {
            POMModel mdl = t.getModel();
            if (!mdl.getState().equals(Model.State.VALID)) {
                try {
                    mdl.sync();
                } catch (IOException ex) {
                    LOG.log(Level.INFO, null, ex);
                }
            }
            t = t.getModel().getProject();
        }
        //ordered by appearance in pom schema..
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.MODELVERSION, MODEL_VERSION(), t != null ? t.getModelVersion() : null);
        checkChildString(names.GROUPID, GROUPID(), t != null ? t.getGroupId() : null);
        checkChildString(names.ARTIFACTID, ARTIFACTID(), t != null ? t.getArtifactId() : null);
        if (count == 0 && t != null && t.getPackaging() != null) {
            checkChildString(names.PACKAGING, PACKAGING(), t.getPackaging());
        }
        checkChildString(names.NAME, NAME(), t != null ? t.getName() : null);
        checkChildString(names.VERSION, VERSION(), t != null ? t.getVersion() : null);
        checkChildString(names.DESCRIPTION, DESCRIPTION(), t != null ? t.getDescription() : null);
        checkChildString(names.URL, URL(), t != null ? t.getURL() : null);
        checkChildObject(names.PREREQUISITES, Prerequisites.class, PREREQUISITES(), t != null ? t.getPrerequisites() : null);
        checkChildObject(names.ISSUEMANAGEMENT, IssueManagement.class, ISSUEMANAGEMENT(), t != null ? t.getIssueManagement() : null);
        checkChildObject(names.CIMANAGEMENT, CiManagement.class, CIMANAGEMENT(), t != null ? t.getCiManagement() : null);
        checkChildString(names.INCEPTIONYEAR, INCEPTION_YEAR(), t != null ? t.getInceptionYear() : null);
        this.<MailingList>checkListObject(names.MAILINGLISTS, names.MAILINGLIST,
                MailingList.class, MAILING_LISTS(),
                t != null ? t.getMailingLists() : null,
                new IdentityKeyGenerator<MailingList>() {
                    @Override
                    public String createName(MailingList c) {
                        return c.getName() != null ? c.getName() : MAILING_LIST();
                    }
                });
        this.<Developer>checkListObject(names.DEVELOPERS, names.DEVELOPER,
                Developer.class, DEVELOPERS(),
                t != null ? t.getDevelopers() : null,
                new IdentityKeyGenerator<Developer>() {
                    @Override
                    public String createName(Developer c) {
                        return c.getId() != null ? c.getId() : DEVELOPER();
                    }
                });
        this.<Contributor>checkListObject(names.CONTRIBUTORS, names.CONTRIBUTOR,
                Contributor.class, CONTRIBUTORS(),
                t != null ? t.getContributors() : null,
                new IdentityKeyGenerator<Contributor>() {
                    @Override
                    public String createName(Contributor c) {
                        return c.getName() != null ? c.getName() : CONTRIBUTOR();
                    }
                });
        this.<License>checkListObject(names.LICENSES, names.LICENSE,
                License.class, LICENSES(),
                t != null ? t.getLicenses() : null,
                new IdentityKeyGenerator<License>() {
                    @Override
                    public String createName(License c) {
                        return c.getName() != null ? c.getName() : LICENSE();
                    }
                });
        checkChildObject(names.SCM, Scm.class, SCM(), t != null ? t.getScm() : null);
        checkChildObject(names.ORGANIZATION, Organization.class, ORGANIZATION(), t != null ? t.getOrganization() : null);
        checkChildObject(names.BUILD, Build.class, BUILD(), t != null ? t.getBuild() : null);
        this.<Profile>checkListObject(names.PROFILES, names.PROFILE,
                Profile.class, PROFILES(),
                t != null ? t.getProfiles() : null,
                new KeyGenerator<Profile>() {
                    @Override
                    public Object generate(Profile c) {
                        return c.getId();
                    }
                    @Override
                    public String createName(Profile c) {
                        return c.getId() != null ? c.getId() : PROFILE();
                    }
                });
        this.<Repository>checkListObject(names.REPOSITORIES, names.REPOSITORY,
                Repository.class, REPOSITORIES(),
                t != null ? t.getRepositories() : null,
                new KeyGenerator<Repository>() {
                    @Override
                    public Object generate(Repository c) {
                        return c.getId();
                    }
                    @Override
                    public String createName(Repository c) {
                        return c.getId() != null ? c.getId() : REPOSITORY();
                    }
                });
        this.<Repository>checkListObject(names.PLUGINREPOSITORIES, names.PLUGINREPOSITORY,
                Repository.class, PLUGIN_REPOSITORIES(),
                t != null ? t.getPluginRepositories() : null,
                new KeyGenerator<Repository>() {
                    @Override
                    public Object generate(Repository c) {
                        return c.getId();
                    }
                    @Override
                    public String createName(Repository c) {
                        return c.getId() != null ? c.getId() : REPOSITORY();
                    }
                });
        checkDependencies(t);
        checkChildObject(names.REPORTING, Reporting.class, REPORTING(), t != null ? t.getReporting() : null);
        checkChildObject(names.DEPENDENCYMANAGEMENT, DependencyManagement.class, DEPENDENCY_MANAGEMENT(), t != null ? t.getDependencyManagement() : null);
        checkChildObject(names.DISTRIBUTIONMANAGEMENT, DistributionManagement.class, DISTRIBUTION_MANAGEMENT(), t != null ? t.getDistributionManagement() : null);
        checkChildObject(names.PROPERTIES, Properties.class, PROPERTIES(), t != null ? t.getProperties() : null);

        //only show modules in current project, no point in showing overrides
        List<String> modules;
        if (count == 0 && t != null && t.getModules() != null) {
            modules = t.getModules();
        } else {
            modules = null;
        }
        checkStringListObject(names.MODULES, names.MODULE, MODULES(), MODULE(), modules);

        count++;
    }

    @Override
    public void visit(Parent target) {
    }

    @Override
    public void visit(Organization target) {
        Organization t = target;
        assert t != null ? t.isInDocumentModel() : true;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.NAME, NAME(), t != null ? t.getName() : null);
        checkChildString(names.URL, URL(), t != null ? t.getUrl() : null);

        count++;
    }

    @Override
    @Messages({"SNAPSHOT_REPOSITORY=Snapshot Repository", "SITE=Site", "DOWNLOAD_URL=Download Url"})
    public void visit(DistributionManagement target) {
        DistributionManagement t = target;
        assert t != null ? t.isInDocumentModel() : true;        
        POMQNames names = parent.getPOMQNames();
        checkChildObject(names.DIST_REPOSITORY, DeploymentRepository.class, REPOSITORY(), t != null ? t.getRepository() : null);
        checkChildObject(names.DIST_SNAPSHOTREPOSITORY, DeploymentRepository.class, SNAPSHOT_REPOSITORY(), t != null ? t.getSnapshotRepository() : null);
        checkChildObject(names.SITE, Site.class, SITE(), t != null ? t.getSite() : null);
        checkChildString(names.DOWNLOADURL, DOWNLOAD_URL(), t != null ? t.getDownloadUrl() : null);

        count++;
    }

    @Override
    @Messages("ID=Id")
    public void visit(Site target) {
        Site t = target;
        assert t != null ? t.isInDocumentModel() : true;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.ID, ID(), t != null ? t.getId() : null);
        checkChildString(names.NAME, NAME(), t != null ? t.getName() : null);
        checkChildString(names.URL, URL(), t != null ? t.getUrl() : null);

        count++;
    }

    @Override
    @Messages("LAYOUT=Layout")
    public void visit(DeploymentRepository target) {
        DeploymentRepository t = target;
        assert t != null ? t.isInDocumentModel() : true;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.ID, ID(), t != null ? t.getId() : null);
        checkChildString(names.NAME, NAME(), t != null ? t.getName() : null);
        checkChildString(names.URL, URL(), t != null ? t.getUrl() : null);
        checkChildString(names.LAYOUT, LAYOUT(), t != null ? t.getLayout() : null);

        count++;
    }

    @Override
    @Messages("MAVEN=Maven")
    public void visit(Prerequisites target) {
        Prerequisites t = target;
         assert t != null ? t.isInDocumentModel() : true;
       POMQNames names = parent.getPOMQNames();
        checkChildString(names.MAVEN, MAVEN(), t != null ? t.getMaven() : null);

        count++;
    }

    @Override
    @Messages({"EMAIL=Email", "ORGANIZATION_URL=Organization Url", "TIMEZONE=Timezone"})
    public void visit(Contributor target) {
        Contributor t = target;
        assert t != null ? t.isInDocumentModel() : true;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.NAME, NAME(), t != null ? t.getName() : null);
        checkChildString(names.EMAIL, EMAIL(), t != null ? t.getEmail() : null);
        checkChildString(names.URL, URL(), t != null ? t.getUrl() : null);
        checkChildString(names.ORGANIZATION, ORGANIZATION(), t != null ? t.getOrganization() : null);
        checkChildString(names.ORGANIZATIONURL, ORGANIZATION_URL(), t != null ? t.getOrganizationUrl() : null);
        checkChildString(names.TIMEZONE, TIMEZONE(), t != null ? t.getTimezone() : null);

        count++;
    }

    @Override
    @Messages({"CONNECTION=Connection", "DEVELOPER_CONNECTION=Developer Connection", "TAG=Tag"})
    public void visit(Scm target) {
        Scm t = target;
        assert t != null ? t.isInDocumentModel() : true;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.CONNECTION, CONNECTION(), t != null ? t.getConnection() : null);
        checkChildString(names.DEVELOPERCONNECTION, DEVELOPER_CONNECTION(), t != null ? t.getDeveloperConnection() : null);
        checkChildString(names.TAG, TAG(), t != null ? t.getTag() : null);
        checkChildString(names.URL, URL(), t != null ? t.getUrl() : null);

        count++;
    }

    @Override
    @Messages("SYSTEM=System")
    public void visit(IssueManagement target) {
        IssueManagement t = target;
        assert t != null ? t.isInDocumentModel() : true;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.SYSTEM, SYSTEM(), t != null ? t.getSystem() : null);
        checkChildString(names.URL, URL(), t != null ? t.getUrl() : null);

        count++;
    }

    @Override
    public void visit(CiManagement target) {
        CiManagement t = target;
        assert t != null ? t.isInDocumentModel() : true;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.SYSTEM, SYSTEM(), t != null ? t.getSystem() : null);
        checkChildString(names.URL, URL(), t != null ? t.getUrl() : null);

        count++;
    }

    @Override
    public void visit(Notifier target) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    @Messages({"RELEASES=Releases", "SNAPSHOTS=Snapshots"})
    public void visit(Repository target) {
        Repository t = target;
        assert t != null ? t.isInDocumentModel() : true;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.ID, ID(), t != null ? t.getId() : null);
        checkChildString(names.NAME, NAME(), t != null ? t.getName() : null);
        checkChildString(names.URL, URL(), t != null ? t.getUrl() : null);
        checkChildString(names.LAYOUT, LAYOUT(), t != null ? t.getLayout() : null);
        checkChildObject(names.RELEASES, RepositoryPolicy.class, RELEASES(), t != null ? t.getReleases() : null);
        checkChildObject(names.SNAPSHOTS, RepositoryPolicy.class, SNAPSHOTS(), t != null ? t.getSnapshots() : null);

        count++;
    }

    @Override
    @Messages({"ENABLED=Enabled", "UPDATE_POLICY=Update Policy", "CHECKSUM_POLICY=Checksum Policy"})
    public void visit(RepositoryPolicy target) {
        RepositoryPolicy t = target;
        assert t != null ? t.isInDocumentModel() : true;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.ENABLED, ENABLED(), t != null ? (t.isEnabled() != null ? t.isEnabled().toString() : null) : null);
        checkChildString(names.UPDATEPOLICY, UPDATE_POLICY(), t != null ? t.getUpdatePolicy() : null);
        checkChildString(names.CHECKSUMPOLICY, CHECKSUM_POLICY(), t != null ? t.getChecksumPolicy() : null);

        count++;
    }

    @Override
    @Messages("ACTIVATION=Activation")
    public void visit(Profile target) {
        Profile t = target;
        assert t != null ? t.isInDocumentModel() : true;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.ID, ID(), t != null ? t.getId() : null);
        checkChildObject(names.ACTIVATION, Activation.class, ACTIVATION(), t != null ? t.getActivation() : null);
        checkChildObject(names.BUILD, BuildBase.class, BUILD(), t != null ? t.getBuildBase() : null);
        this.<Repository>checkListObject(names.REPOSITORIES, names.REPOSITORY,
                Repository.class, REPOSITORIES(),
                t != null ? t.getRepositories() : null,
                new KeyGenerator<Repository>() {
                    @Override
                    public Object generate(Repository c) {
                        return c.getId();
                    }
                    @Override
                    public String createName(Repository c) {
                        return c.getId() != null ? c.getId() : REPOSITORY();
                    }
                });
        this.<Repository>checkListObject(names.PLUGINREPOSITORIES, names.PLUGINREPOSITORY,
                Repository.class, PLUGIN_REPOSITORIES(),
                t != null ? t.getPluginRepositories() : null,
                new KeyGenerator<Repository>() {
                    @Override
                    public Object generate(Repository c) {
                        return c.getId();
                    }
                    @Override
                    public String createName(Repository c) {
                        return c.getId() != null ? c.getId() : REPOSITORY();
                    }
                });
        checkDependencies(t);
        checkChildObject(names.REPORTING, Reporting.class, REPORTING(), t != null ? t.getReporting() : null);
        checkChildObject(names.DEPENDENCYMANAGEMENT, DependencyManagement.class, DEPENDENCY_MANAGEMENT(), t != null ? t.getDependencyManagement() : null);
        checkChildObject(names.DISTRIBUTIONMANAGEMENT, DistributionManagement.class, DISTRIBUTION_MANAGEMENT(), t != null ? t.getDistributionManagement() : null);
        checkChildObject(names.PROPERTIES, Properties.class, PROPERTIES(), t != null ? t.getProperties() : null);

        count++;
    }

    @Override
    public void visit(BuildBase target) {
        BuildBase t = target;
        assert t != null ? t.isInDocumentModel() : true;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.DEFAULTGOAL, DEFAULT_GOAL(), t != null ? t.getDefaultGoal() : null);
        this.<Resource>checkListObject(names.RESOURCES, names.RESOURCE,
                Resource.class, RESOURCES(),
                t != null ? t.getResources() : null,
                new IdentityKeyGenerator<Resource>() {
                    @Override
                    public String createName(Resource c) {
                        return c.getDirectory() != null ? c.getDirectory() : RESOURCE();
                    }
                });
        this.<Resource>checkListObject(names.TESTRESOURCES, names.TESTRESOURCE,
                Resource.class, TEST_RESOURCES(),
                t != null ? t.getTestResources() : null,
                new IdentityKeyGenerator<Resource>() {
                    @Override
                    public String createName(Resource c) {
                        return c.getDirectory() != null ? c.getDirectory() : TEST_RESOURCE();
                    }
                });
        checkChildString(names.DIRECTORY, DIRECTORY(), t != null ? t.getDirectory() : null);
        checkChildString(names.FINALNAME, FINAL_NAME(), t != null ? t.getFinalName() : null);
        //TODO filters
        checkChildObject(names.PLUGINMANAGEMENT, PluginManagement.class, PLUGIN_MANAGEMENT(), t != null ? t.getPluginManagement() : null);
        this.<Plugin>checkListObject(names.PLUGINS, names.PLUGIN,
                Plugin.class, PLUGINS(),
                t != null ? t.getPlugins() : null,
                new KeyGenerator<Plugin>() {
                    @Override
                    public Object generate(Plugin c) {
                        String gr = c.getGroupId();
                        if (gr == null) {
                            gr = "org.apache.maven.plugins"; //NOI18N
                        }
                        return gr + ":" + c.getArtifactId(); //NOI18N
                    }
                    @Override
                    public String createName(Plugin c) {
                        return c.getArtifactId() != null ? c.getArtifactId() : PLUGIN();
                    }
                });

        count++;
    }

    @Override
    @Messages({"EXECUTIONS=Executions", "EXECUTION=Execution", "GOALS=Goals", "INHERITED=Inherited", "CONFIGURATION=Configuration", "GOAL=Goal"})
    public void visit(Plugin target) {
        Plugin t = target;
        assert t != null ? t.isInDocumentModel() : true;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.GROUPID, GROUPID(), t != null ? t.getGroupId() : null);
        checkChildString(names.ARTIFACTID, ARTIFACTID(), t != null ? t.getArtifactId() : null);
        checkChildString(names.VERSION, VERSION(), t != null ? t.getVersion() : null);
        checkChildString(names.EXTENSIONS, EXTENSIONS(), t != null ? (t.isExtensions() != null ? t.isExtensions().toString() : null) : null);
        this.<PluginExecution>checkListObject(names.EXECUTIONS, names.EXECUTION,
                PluginExecution.class, EXECUTIONS(),
                t != null ? t.getExecutions() : null,
                new KeyGenerator<PluginExecution>() {
                    @Override
                    public Object generate(PluginExecution c) {
                        return c.getId(); //NOI18N
                    }
                    @Override
                    public String createName(PluginExecution c) {
                        return c.getId() != null ? c.getId() : EXECUTION();
                    }
                });
        checkDependencies(t);
        checkStringListObject(names.GOALS, names.GOAL, GOALS(), GOAL(), t != null ? t.getGoals() : null);
        checkChildString(names.INHERITED, INHERITED(), t != null ? (t.isInherited() != null ? t.isInherited().toString() : null) : null);
        checkChildObject(names.CONFIGURATION, Configuration.class, CONFIGURATION(), t != null ? t.getConfiguration() : null);

        count++;
    }

    @Override
    @Messages({"TYPE=Type", "CLASSIFIER=Classifier", "SCOPE=Scope", "EXCLUSIONS=Exclusions", "EXCLUSION=Exclusion"})
    public void visit(Dependency target) {
        Dependency t = target;
        assert t != null ? t.isInDocumentModel() : true;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.GROUPID, GROUPID(), t != null ? t.getGroupId() : null);
        checkChildString(names.ARTIFACTID, ARTIFACTID(), t != null ? t.getArtifactId() : null);
        checkChildString(names.VERSION, VERSION(), t != null ? t.getVersion() : null);
        checkChildString(names.TYPE, TYPE(), t != null ? t.getType() : null);
        checkChildString(names.CLASSIFIER, CLASSIFIER(), t != null ? t.getClassifier() : null);
        checkChildString(names.SCOPE, SCOPE(), t != null ? t.getScope() : null);

        this.<Exclusion>checkListObject(names.EXCLUSIONS, names.EXCLUSION,
                Exclusion.class, EXCLUSIONS(),
                t != null ? t.getExclusions() : null,
                new IdentityKeyGenerator<Exclusion>() {
                    @Override
                    public String createName(Exclusion c) {
                        return c.getArtifactId() != null ? c.getArtifactId() : EXCLUSION();
                    }
                });

        count++;
    }

    @Override
    public void visit(Exclusion target) {
        Exclusion t = target;
        assert t != null ? t.isInDocumentModel() : true;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.GROUPID, GROUPID(), t != null ? t.getGroupId() : null);
        checkChildString(names.ARTIFACTID, ARTIFACTID(), t != null ? t.getArtifactId() : null);

        count++;
    }

    @Override
    @Messages("PHASE=Phase")
    public void visit(PluginExecution target) {
        PluginExecution t = target;
        assert t != null ? t.isInDocumentModel() : true;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.ID, ID(), t != null ? t.getId() : null);
        checkChildString(names.PHASE, PHASE(), t != null ? t.getPhase() : null);
        checkChildString(names.INHERITED, INHERITED(), t != null ? (t.isInherited() != null ? t.isInherited().toString() : null) : null);
        checkChildObject(names.CONFIGURATION, Configuration.class, CONFIGURATION(), t != null ? t.getConfiguration() : null);

        count++;
    }

    @Override
    @Messages({"TARGET_PATH=Target Path", "INCLUDES=Includes", "INCLUDE=Include", "EXCLUDES=Excludes", "EXCLUDE=Exclude"})
    public void visit(Resource target) {
        Resource t = target;
        assert t != null ? t.isInDocumentModel() : true;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.TARGETPATH, TARGET_PATH(), t != null ? t.getTargetPath() : null);
        //TODO filtering
        checkChildString(names.DIRECTORY, DIRECTORY(), t != null ? t.getDirectory() : null);
        checkStringListObject(names.INCLUDES, names.INCLUDE, INCLUDES(), INCLUDE(), t != null ? t.getIncludes() : null);
        checkStringListObject(names.EXCLUDES, names.EXCLUDE, EXCLUDES(), EXCLUDE(), t != null ? t.getExcludes() : null);

        count++;
    }

    @Override
    public void visit(PluginManagement target) {
        PluginManagement t = target;
        assert t != null ? t.isInDocumentModel() : true;
        POMQNames names = parent.getPOMQNames();
        this.<Plugin>checkListObject(names.PLUGINS, names.PLUGIN,
                Plugin.class, PLUGINS(),
                t != null ? t.getPlugins() : null,
                new KeyGenerator<Plugin>() {
                    @Override
                    public Object generate(Plugin c) {
                        String gr = c.getGroupId();
                        if (gr == null) {
                            gr = "org.apache.maven.plugins"; //NOI18N
                        }
                        return gr + ":" + c.getArtifactId(); //NOI18N
                    }
                    @Override
                    public String createName(Plugin c) {
                        return c.getArtifactId() != null ? c.getArtifactId() : PLUGIN();
                    }
                });

        count++;
    }

    @Override
    @Messages({"EXCLUDE_DEFAULTS=Exclude Defaults", "OUTPUT_DIRECTORY=Output Directory", "REPORT_PLUGINS=Report Plugins", "REPORT_PLUGIN=Report Plugin"})
    public void visit(Reporting target) {
        Reporting t = target;
        assert t != null ? t.isInDocumentModel() : true;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.EXCLUDEDEFAULTS, EXCLUDE_DEFAULTS(), t != null ? (t.isExcludeDefaults() != null ? t.isExcludeDefaults().toString() : null) : null);
        checkChildString(names.OUTPUTDIRECTORY, OUTPUT_DIRECTORY(), t != null ? t.getOutputDirectory() : null);
        this.<ReportPlugin>checkListObject(names.REPORTPLUGINS, names.REPORTPLUGIN,
                ReportPlugin.class, REPORT_PLUGINS(),
                t != null ? t.getReportPlugins() : null,
                new KeyGenerator<ReportPlugin>() {
                    @Override
                    public Object generate(ReportPlugin c) {
                        return c.getGroupId() + ":" + c.getArtifactId(); //NOI18N
                    }
                    @Override
                    public String createName(ReportPlugin c) {
                        return c.getArtifactId() != null ? c.getArtifactId() : REPORT_PLUGIN();
                    }
                });

        count++;
    }

    @Override
    @Messages({"REPORTSETS=ReportSets", "REPORTSET=ReportSet"})
    public void visit(ReportPlugin target) {
        ReportPlugin t = target;
        assert t != null ? t.isInDocumentModel() : true;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.GROUPID, GROUPID(), t != null ? t.getGroupId() : null);
        checkChildString(names.ARTIFACTID, ARTIFACTID(), t != null ? t.getArtifactId() : null);
        checkChildString(names.VERSION, VERSION(), t != null ? t.getVersion() : null);
        checkChildString(names.INHERITED, INHERITED(), t != null ? (t.isInherited() != null ? t.isInherited().toString() : null) : null);
        checkChildObject(names.CONFIGURATION, Configuration.class, CONFIGURATION(), t != null ? t.getConfiguration() : null);
        this.<ReportSet>checkListObject(names.REPORTSETS, names.REPORTSET,
                ReportSet.class, REPORTSETS(),
                t != null ? t.getReportSets() : null,
                new KeyGenerator<ReportSet>() {
                    @Override
                    public Object generate(ReportSet c) {
                        return c.getId(); //NOI18N
                    }
                    @Override
                    public String createName(ReportSet c) {
                        return c.getId() != null ? c.getId() : REPORTSET();
                    }
                });

        count++;
    }

    @Override
    @Messages({"REPORTS=Reports", "REPORT=Report"})
    public void visit(ReportSet target) {
        ReportSet t = target;
        assert t != null ? t.isInDocumentModel() : true;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.ID, ID(), t != null ? t.getId() : null);
        checkChildObject(names.CONFIGURATION, Configuration.class, CONFIGURATION(), t != null ? t.getConfiguration() : null);
        checkChildString(names.INHERITED, INHERITED(), t != null ? (t.isInherited() != null ? t.isInherited().toString() : null) : null);
        checkStringListObject(names.REPORTS, names.REPORT, REPORTS(), REPORT(), t != null ? t.getReports() : null);

        count++;
    }

    @Override
    @Messages({"OPERATING_SYSTEM=Operating System", "PROPERTY=Property", "FILE=File", "CUSTOM=Custom"})
    public void visit(Activation target) {
        Activation t = target;
        assert t != null ? t.isInDocumentModel() : true;
        POMQNames names = parent.getPOMQNames();
        checkChildObject(names.ACTIVATIONOS, ActivationOS.class, OPERATING_SYSTEM(), t != null ? t.getActivationOS() : null);
        checkChildObject(names.ACTIVATIONPROPERTY, ActivationProperty.class, PROPERTY(), t != null ? t.getActivationProperty() : null);
        checkChildObject(names.ACTIVATIONFILE, ActivationFile.class, FILE(), t != null ? t.getActivationFile() : null);
        checkChildObject(names.ACTIVATIONCUSTOM, ActivationCustom.class, CUSTOM(), t != null ? t.getActivationCustom() : null);

        count++;
    }

    @Override
    public void visit(ActivationProperty target) {
    }

    @Override
    public void visit(ActivationOS target) {
    }

    @Override
    public void visit(ActivationFile target) {
    }

    @Override
    public void visit(ActivationCustom target) {
    }

    @Override
    public void visit(DependencyManagement target) {
        DependencyManagement t = target;
        assert t != null ? t.isInDocumentModel() : true;
        checkDependencies(t);

        count++;
    }

    @Override
    @Messages({"EXTENSIONS=Extensions", "DEFAULT_GOAL=Default Goal", "RESOURCES=Resources", "RESOURCE=Resource", "TEST_RESOURCES=Test Resources", "TEST_RESOURCE=Test Resource", "DIRECTORY=Directory", "FINAL_NAME=Final Name", "PLUGIN_MANAGEMENT=Plugin Management", "PLUGINS=Plugins", "PLUGIN=Plugin", "SOURCE_DIRECTORY=Source Directory", "TEST_SOURCE_DIRECTORY=Test Source Directory", "TEST_OUTPUT_DIRECTORY=Test Output Directory", "EXTENSION=Extension"})
    public void visit(Build target) {
        Build t = target;
        assert t != null ? t.isInDocumentModel() : true;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.SOURCEDIRECTORY, SOURCE_DIRECTORY(), t != null ? t.getSourceDirectory() : null);
        //just ignore script directory
        checkChildString(names.TESTSOURCEDIRECTORY, TEST_SOURCE_DIRECTORY(), t != null ? t.getTestSourceDirectory() : null);
        checkChildString(names.OUTPUTDIRECTORY, OUTPUT_DIRECTORY(), t != null ? t.getOutputDirectory() : null);
        checkChildString(names.TESTOUTPUTDIRECTORY, TEST_OUTPUT_DIRECTORY(), t != null ? t.getTestOutputDirectory() : null);
        this.<Extension>checkListObject(names.EXTENSIONS, names.EXTENSION,
                Extension.class, EXTENSIONS(),
                t != null ? t.getExtensions() : null,
                new KeyGenerator<Extension>() {
                    @Override
                    public Object generate(Extension c) {
                        String gr = c.getGroupId();
                        return gr + ":" + c.getArtifactId(); //NOI18N
                    }
                    @Override
                    public String createName(Extension c) {
                        return c.getArtifactId() != null ? c.getArtifactId() : EXTENSION();
                    }
                });
        checkChildString(names.DEFAULTGOAL, DEFAULT_GOAL(), t != null ? t.getDefaultGoal() : null);
        this.<Resource>checkListObject(names.RESOURCES, names.RESOURCE,
                Resource.class, RESOURCES(),
                t != null ? t.getResources() : null,
                new IdentityKeyGenerator<Resource>() {
                    @Override
                    public String createName(Resource c) {
                        return c.getDirectory() != null ? c.getDirectory() : RESOURCE();
                    }
                });
        this.<Resource>checkListObject(names.TESTRESOURCES, names.TESTRESOURCE,
                Resource.class, TEST_RESOURCES(),
                t != null ? t.getTestResources() : null,
                new IdentityKeyGenerator<Resource>() {
                    @Override
                    public String createName(Resource c) {
                        return c.getDirectory() != null ? c.getDirectory() : TEST_RESOURCE();
                    }
                });
        checkChildString(names.DIRECTORY, DIRECTORY(), t != null ? t.getDirectory() : null);
        checkChildString(names.FINALNAME, FINAL_NAME(), t != null ? t.getFinalName() : null);
        //TODO filters
        checkChildObject(names.PLUGINMANAGEMENT, PluginManagement.class, PLUGIN_MANAGEMENT(), t != null ? t.getPluginManagement() : null);
        this.<Plugin>checkListObject(names.PLUGINS, names.PLUGIN,
                Plugin.class, PLUGINS(),
                t != null ? t.getPlugins() : null,
                new KeyGenerator<Plugin>() {
                    @Override
                    public Object generate(Plugin c) {
                        String gr = c.getGroupId();
                        if (gr == null) {
                            gr = "org.apache.maven.plugins"; //NOI18N
                        }
                        return gr + ":" + c.getArtifactId(); //NOI18N
                    }
                    @Override
                    public String createName(Plugin c) {
                        return c.getArtifactId() != null ? c.getArtifactId() : PLUGIN();
                    }
                });

        count++;
    }

    @Override
    public void visit(Extension target) {
        Extension t = target;
        assert t != null ? t.isInDocumentModel() : true;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.GROUPID, GROUPID(), t != null ? t.getGroupId() : null);
        checkChildString(names.ARTIFACTID, ARTIFACTID(), t != null ? t.getArtifactId() : null);
        checkChildString(names.VERSION, VERSION(), t != null ? t.getVersion() : null);
    }

    @Override
    public void visit(License target) {
        License t = target;
        assert t != null ? t.isInDocumentModel() : true;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.NAME, NAME(), t != null ? t.getName() : null);
        checkChildString(names.URL, URL(), t != null ? t.getUrl() : null);
        count++;
    }

    @Override
    @Messages({"SUBSCRIBE=Subscribe", "UNSUBSCRIBE=Unsubscribe", "POST=Post", "ARCHIVE=Archive"})
    public void visit(MailingList target) {
        MailingList t = target;
        assert t != null ? t.isInDocumentModel() : true;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.NAME, NAME(), t != null ? t.getName() : null);
        checkChildString(names.SUBSCRIBE, SUBSCRIBE(), t != null ? t.getSubscribe() : null);
        checkChildString(names.UNSUBSCRIBE, UNSUBSCRIBE(), t != null ? t.getUnsubscribe() : null);
        checkChildString(names.POST, POST(), t != null ? t.getPost() : null);
        checkChildString(names.ARCHIVE, ARCHIVE(), t != null ? t.getArchive() : null);
        count++;
    }

    @Override
    public void visit(Developer target) {
        Developer t = target;
        assert t != null ? t.isInDocumentModel() : true;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.ID, ID(), t != null ? t.getId() : null);
        checkChildString(names.NAME, NAME(), t != null ? t.getName() : null);
        checkChildString(names.EMAIL, EMAIL(), t != null ? t.getEmail() : null);
        checkChildString(names.URL, URL(), t != null ? t.getUrl() : null);
        checkChildString(names.ORGANIZATION, ORGANIZATION(), t != null ? t.getOrganization() : null);
        checkChildString(names.ORGANIZATIONURL, ORGANIZATION_URL(), t != null ? t.getOrganizationUrl() : null);
        checkChildString(names.TIMEZONE, TIMEZONE(), t != null ? t.getTimezone() : null);

        count++;
    }

    @Override
    public void visit(POMExtensibilityElement target) {
        POMExtensibilityElement t = target;
        assert t != null ? t.isInDocumentModel() : true;
        if (t != null) {
            doVisit(t.getAnyElements());
        }

        count++;
        
        for (POMCutHolder prop : childs.values()) {
            growToSize(count, prop);
        }
    }

    @Override
    public void visit(ModelList target) {
    }

    @Override
    public void visit(Configuration target) {
        Configuration t = target;
        assert t != null ? t.isInDocumentModel() : true;
        if (t != null) {
            doVisit(t.getConfigurationElements());
        }

        count++;

        for (POMCutHolder prop : childs.values()) {
            growToSize(count, prop);
        }

    }

    private void doVisit(List<POMExtensibilityElement> elems) {
        //#211429
        Set<String> shortvalues = new HashSet<String>();
        Set<String> duplicateValues = new HashSet<String>();
        for (POMExtensibilityElement el : elems) {
            String shortKey = el.getQName().getLocalPart();
            if (!shortvalues.add(shortKey)) {
                duplicateValues.add(shortKey);
            }
        } 
        
        for (POMExtensibilityElement el : elems) {
            List<POMExtensibilityElement> any = el.getAnyElements();
            String key = el.getQName().getLocalPart();  
            if (duplicateValues.contains(key)) {
                key = el.getQName().getLocalPart() + "=" + el.getElementText();
            }
            if (any != null && !any.isEmpty()) {
                POMCutHolder nd = childs.get(key);
                if (nd == null) {
                    nd = new SingleObjectCH(parent, el.getQName(), el.getQName().getLocalPart(), POMExtensibilityElement.class, configuration);
                    childs.put(key, nd);
                }
                fillValues(count, nd, el);
            } else {
                POMCutHolder nd = childs.get(key);
                if (nd == null) {
                    nd = new SingleFieldCH(parent, el.getQName(), el.getQName().getLocalPart());
                    childs.put(key, nd);
                }
                fillValues(count, nd, el.getElementText());
            }
        }

    }

    @Override
    public void visit(Properties target) {
        Properties t = target;
        assert t != null ? t.isInDocumentModel() : true;
        if (t != null) {
            Map<String, String> props = t.getProperties();
            for (Map.Entry<String, String> ent : props.entrySet()) {
                POMCutHolder nd = childs.get(ent.getKey());
                if (nd == null) {
                    nd = new SingleFieldCH(parent, ent.getKey());
                    childs.put(ent.getKey(), nd);
                }
                fillValues(count, nd, ent.getValue());
            }
        }

        count++;
        
        for (POMCutHolder prop : childs.values()) {
            growToSize(count, prop);
        }


    }

    @Override
    public void visit(StringList target) {
    }


    @SuppressWarnings("unchecked")
    private void checkChildString(POMQName qname, String displayName, String value) {
        POMCutHolder nd = childs.get(qname.getName());
        if (nd == null) {
            nd = new SingleFieldCH(parent, qname, displayName);
            childs.put(qname.getName(), nd);
        }
        fillValues(count, nd, value);
    }

    private void checkChildObject(POMQName qname, Class type, String displayName, POMComponent value) {
        POMCutHolder  nd = childs.get(qname.getName());
        if (nd == null) {
            nd = new SingleObjectCH(parent, qname, displayName, type, configuration);
            childs.put(qname.getName(), nd);
        }
        fillValues(count, nd, value);
    }


    private <T extends POMComponent> void checkListObject(POMQName qname, POMQName childName, Class type, String displayName, List<T> values, KeyGenerator<T> keygen) {
        POMCutHolder nd = childs.get(qname.getName());
        if (nd == null) {
            nd = new ListObjectCH<T>(parent, qname, childName, type, keygen, displayName, configuration);
            childs.put(qname.getName(), nd);
        }
        fillValues(count, nd, values);
    }

    private void checkStringListObject(POMQName qname, POMQName childName, String displayName, String childDisplayName, List<String> values) {
        POMCutHolder nd = childs.get(qname.getName());
        if (nd == null) {
            nd =  new ListStringCH(parent, qname, childName, displayName, childDisplayName, configuration);
            childs.put(qname.getName(), nd);
        }
        fillValues(count, nd, values);
    }



    private static void fillValues(int current, POMCutHolder cutHolder, Object value) {
        growToSize(current, cutHolder);
        cutHolder.addCut(value);
    }

    private static void growToSize(int count, POMCutHolder cutHolder) {
        while (cutHolder.getCutsSize() < count) {
            cutHolder.addCut(null);
        }
    }

    @Messages({"DEPENDENCIES=Dependencies", "DEPENDENCY=Dependency"})
    private void checkDependencies(DependencyContainer container) {
        POMQNames names = parent.getPOMQNames();
        this.<Dependency>checkListObject(names.DEPENDENCIES, names.DEPENDENCY,
                Dependency.class, DEPENDENCIES(),
                container != null ? container.getDependencies() : null,
                new KeyGenerator<Dependency>() {
                    @Override
                    public Object generate(Dependency c) {
                        return c.getGroupId() + ":" + c.getArtifactId(); //NOI18N
                    }
                    @Override
                    public String createName(Dependency c) {
                        return c.getArtifactId() != null ? c.getArtifactId() : DEPENDENCY();
                    }
                });

    }


    private interface KeyGenerator<T extends POMComponent> {
        Object generate(T c);

        String createName(T c);
    }

    private abstract class IdentityKeyGenerator<T extends POMComponent> implements  KeyGenerator<T> {
        @Override
        public Object generate(T c) {
            return c;
        }

    }

    abstract static class POMCutHolder {
        private List cuts = new ArrayList();

        POMCutHolder parent;
        private POMModel[] models;
        private POMQNames names;

        protected POMCutHolder(POMModel[] source, POMQNames names) {
            models = source;
            this.names = names;
        }

        protected POMCutHolder(POMCutHolder parent) {
            this.parent = parent;
        }

        public POMModel[] getSource() {
            if (models != null) {
                return models;
            }
            if (parent != null) {
                return parent.getSource();
            }
            throw new IllegalStateException();
        }

        public POMQNames getPOMQNames() {
            if (names != null) {
                return names;
            }
            if (parent != null) {
                return parent.getPOMQNames();
            }
            throw new IllegalStateException();
        }



        Object[] getCutValues() {
            return cuts.toArray();
        }

        String[] getCutValuesAsString() {
            String[] toRet = new String[cuts.size()];
            int i = 0;
            for (Object cut : cuts) {
                toRet[i] = (cut != null ? cut.toString() : null);
                i++;
            }
            return toRet;
        }

        @SuppressWarnings("unchecked")
        void addCut(Object obj) {
            cuts.add(obj);
        }

        int getCutsSize() {
            return cuts.size();
        }

        abstract Node createNode();
    }

    private static class SingleFieldCH extends POMCutHolder {
        private Object qname;
        private String display;

        private SingleFieldCH(POMCutHolder parent, POMQName qname, String displayName) {
            super(parent);
            this.qname = qname;
            this.display = displayName;
        }

        private SingleFieldCH(POMCutHolder parent, QName qname, String displayName) {
            super(parent);
            this.qname = qname;
            this.display = displayName;
        }

        private SingleFieldCH(POMCutHolder parent, String displayName) {
            super(parent);
            this.qname = displayName;
            this.display = displayName;
        }


        @Override
        Node createNode() {
            return new SingleFieldNode(Lookups.fixed(this, qname), display);
        }
    }

    static class SingleObjectCH extends POMCutHolder {
        private Object qname;
        private String display;
        private Class type;
        private POMModelPanel.Configuration configuration;

        SingleObjectCH(POMModel[] models, POMQNames names, POMQName qname, Class type, POMModelPanel.Configuration config) {
            super(models, names);
            this.qname = qname;
            this.display = "root"; //NOI18N
            this.type = type;
            this.configuration = config;
        }

        private SingleObjectCH(POMCutHolder parent, POMQName qname, String displayName, Class type, POMModelPanel.Configuration config) {
            super(parent);
            this.qname = qname;
            this.display = displayName;
            this.type = type;
            this.configuration = config;
        }

        private SingleObjectCH(POMCutHolder parent, QName qname, String displayName, Class type, POMModelPanel.Configuration config) {
            super(parent);
            this.qname = qname;
            this.display = displayName;
            this.type = type;
            this.configuration = config;
        }

        private SingleObjectCH(POMCutHolder parent, POMQName qname, String displayName) {
            super(parent);
            this.qname = qname;
            this.display = displayName;
        }


        @Override
        Node createNode() {
            if (type == null) {
                return new ObjectNode(Lookups.fixed(this, qname), Children.LEAF, display);
            }
            return new ObjectNode(Lookups.fixed(this, qname), new PomChildren(this, getPOMQNames(), type, configuration), display);
        }
    }

    static class ListObjectCH<T extends POMComponent> extends POMCutHolder {
        private final POMQName qname;
        private final POMQName childName;
        private final String displayName;
        private final Class type;
        private final KeyGenerator<T> keygen;
        private final POMModelPanel.Configuration configuration;

        private ListObjectCH(POMCutHolder parent, POMQName qname, POMQName childName, Class type, KeyGenerator<T> keygen, String displayName, POMModelPanel.Configuration configuration) {
            super(parent);
            this.qname = qname;
            this.childName = childName;
            this.displayName = displayName;
            this.type = type;
            this.keygen = keygen;
            this.configuration = configuration;
        }

        Class getListClass() {
            return type;
        }

        @Override
        Node createNode() {
            return new ListNode(Lookups.fixed(this, qname), new PomListChildren<T>(this, getPOMQNames(), type, keygen, configuration, childName), displayName);
        }

    }

    private static class ListStringCH extends POMCutHolder {
        private POMQName qname;
        private String display;
        private POMQName childName;
        private POMModelPanel.Configuration configuration;
        private final String childDisplayName;

        private ListStringCH(POMCutHolder parent, POMQName qname, POMQName childName, String displayName, String childDisplayName, POMModelPanel.Configuration configuration) {
            super(parent);
            this.qname = qname;
            this.display = displayName;
            this.childName = childName;
            this.childDisplayName = childDisplayName;
            this.configuration = configuration;
        }

        @Override
        Node createNode() {
            return new ListNode(Lookups.fixed(this, qname), new PomStringListChildren(this, childName, childDisplayName), display);
        }
    }


    private static final Image[] ICONS = new Image[] {
        ImageUtilities.loadImage(VALUE), // NOI18N
        ImageUtilities.loadImage(VALUE2), // NOI18N
        ImageUtilities.loadImage(VALUE3), // NOI18N
        ImageUtilities.loadImage(VALUE4), // NOI18N
    };

    private static Image getIconForCutHolder(POMCutHolder holder) {
        int level = POMModelPanel.currentValueDepth(holder.getCutValues());
        if (level >= 0 && level < ICONS.length) {
            return ICONS[level];
        }
        return ICONS[ICONS.length - 1];
    }


    private static class SingleFieldNode extends AbstractNode {

        private String key;
        private SingleFieldNode(Lookup lkp, String key) {
            super(Children.LEAF, lkp);
            setName(key);
            this.key = key;
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[] {
                new SelectAction(this)
            };
        }

        @Override
        public Action getPreferredAction() {
            return new SelectAction(this, 0);
        }


        @Override
        @Messages({"TOOLTIP_Defined_in=Value is defined in the following POM files:", "TOOLTIP_ArtifactId=POM ArtifactId", "TOOLTIP_Value=Value", "UNDEFINED=&lt;Undefined&gt;"})
        public String getShortDescription() {
            StringBuilder buff = new StringBuilder();
            buff.append("<html>").append(TOOLTIP_Defined_in()).append("<p><table><thead><tr><th>")
                    .append(TOOLTIP_ArtifactId()).append("</th><th>")
                    .append(TOOLTIP_Value())
                    .append("</th></tr></thead><tbody>"); //NOI18N
            String[] values = getLookup().lookup(POMCutHolder.class).getCutValuesAsString();
            POMModel[] mdls = getLookup().lookup(POMCutHolder.class).getSource();
            if (values.length == mdls.length) {
            int index = 0;
            for (POMModel mdl : mdls) {
                String artifact = mdl.getProject().getArtifactId();
                buff.append("<tr><td>"); //NOI18N
                buff.append(artifact != null ? artifact : "project");
                buff.append("</td><td>"); //NOI18N
                buff.append(values[index] != null ? values[index] : UNDEFINED());
                buff.append("</td></tr>");//NOI18N
                index++;
            }
            } else {
                LOG.log(Level.WARNING, "#180901: {0} length does not match {1} length", new Object[] {Arrays.toString(values), Arrays.toString(mdls)});
            }
            buff.append("</tbody></table>");//NOI18N

            return buff.toString();
        }


        @Override
        public String getHtmlDisplayName() {
            String[] values = getLookup().lookup(POMCutHolder.class).getCutValuesAsString();

            String dispVal = POMModelPanel.getValidValue(values);
            if (dispVal == null) {
                dispVal = UNDEFINED();
            }
            boolean override = POMModelPanel.overridesParentValue(values);
            String overrideStart = override ? "<b>" : ""; //NOI18N
            String overrideEnd = override ? "</b>" : ""; //NOI18N
            boolean inherited = !POMModelPanel.isValueDefinedInCurrent(values);
            String inheritedStart = inherited ? "<i>" : ""; //NOI18N
            String inheritedEnd = inherited ? "</i>" : ""; //NOI18N

            String message = "<html>" + //NOI18N
                    inheritedStart + overrideStart +
                    key + " : " + dispVal + //NOI18N
                    overrideEnd + inheritedEnd;
            return message;
        }

        @Override
        public Image getIcon(int type) {
             return getIconForCutHolder(getLookup().lookup(POMCutHolder.class));
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

    }

    private static class ObjectNode extends AbstractNode {

        private String key;
        private ObjectNode(Lookup lkp, Children children, String key) {
            super( children, lkp);
            setName(key);
            this.key = key;
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[] {
                new SelectAction(this)
            };
        }

        @Override
        public Action getPreferredAction() {
            return new SelectAction(this, 0);
        }


        @Override
        public String getHtmlDisplayName() {
            Object[] values = getLookup().lookup(POMCutHolder.class).getCutValues();
            String dispVal = POMModelPanel.definesValue(values) ? "" : UNDEFINED();
            boolean override = POMModelPanel.overridesParentValue(values);
            String overrideStart = override ? "<b>" : ""; //NOI18N
            String overrideEnd = override ? "</b>" : ""; //NOI18N
            boolean inherited = !POMModelPanel.isValueDefinedInCurrent(values);
            String inheritedStart = inherited ? "<i>" : ""; //NOI18N
            String inheritedEnd = inherited ? "</i>" : ""; //NOI18N

            String message = "<html>" + //NOI18N
                    inheritedStart + overrideStart +
                    key + " " + dispVal + //NOI18N
                    overrideEnd + inheritedEnd;

            return message;
        }

        @Override
        public Image getIcon(int type) {
             return getIconForCutHolder(getLookup().lookup(POMCutHolder.class));
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        @Messages({"TOOLTIP_IS_DEFINED=Is Defined?", "TOOLTIP_YES=Yes", "TOOLTIP_NO=No"})
        public String getShortDescription() {
            Object[] values = getLookup().lookup(POMCutHolder.class).getCutValues();
            POMModel[] mdls = getLookup().lookup(POMCutHolder.class).getSource();
            StringBuilder buff = new StringBuilder();
            int index = 0;
            buff.append("<html>").
                    append(TOOLTIP_Defined_in()).append("<p><table><thead><tr><th>").
                    append(TOOLTIP_ArtifactId()).append("</th><th>").
                    append(TOOLTIP_IS_DEFINED()).append("</th></tr></thead><tbody>"); //NOI18N
            for (POMModel mdl : mdls) {
                String artifact = mdl.getProject().getArtifactId();
                buff.append("<tr><td>"); //NOI18N
                buff.append(artifact != null ? artifact : "project");
                buff.append("</td><td>"); //NOI18N
                buff.append(values[index] != null ? TOOLTIP_YES() : TOOLTIP_NO());
                buff.append("</td></tr>");//NOI18N
                index++;
            }
            buff.append("</tbody></table>");//NOI18N

            return buff.toString();
        }


    }

    private static class ListNode extends AbstractNode {

        private String key;

        private ListNode(Lookup lkp, Children childs, String name) {
            super(childs , lkp);
            setName(name);
            this.key = name;
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[] {
                new SelectAction(this)
            };
        }

        @Override
        public Action getPreferredAction() {
            return new SelectAction(this, 0);
        }


        @Override
        public String getHtmlDisplayName() {
            //TODO - this needs different markings..
            Object[] values = getLookup().lookup(POMCutHolder.class).getCutValues();

            String dispVal = POMModelPanel.definesValue(values) ? "" : UNDEFINED();
            boolean override = POMModelPanel.overridesParentValue(values);
            String overrideStart = override ? "<b>" : "";
            String overrideEnd = override ? "</b>" : "";
            boolean inherited = !POMModelPanel.isValueDefinedInCurrent(values) && POMModelPanel.definesValue(values);
            String inheritedStart = inherited ? "<i>" : "";
            String inheritedEnd = inherited ? "</i>" : "";
            String message = "<html>" +
                    inheritedStart + overrideStart +
                    key + " " + dispVal +
                    overrideEnd + inheritedEnd;
            return message;
        }

        @Override
        public String getShortDescription() {
            Object[] values = getLookup().lookup(POMCutHolder.class).getCutValues();
            POMModel[] mdls = getLookup().lookup(POMCutHolder.class).getSource();
            StringBuilder buff = new StringBuilder();
            int index = 0;
            buff.append("<html>").
                    append(TOOLTIP_Defined_in()).append("<p><table><thead><tr><th>").
                    append(TOOLTIP_ArtifactId()).append("</th><th>").
                    append(TOOLTIP_IS_DEFINED()).append("</th></tr></thead><tbody>"); //NOI18N
            for (POMModel mdl : mdls) {
                String artifact = mdl.getProject().getArtifactId();
                buff.append("<tr><td>"); //NOI18N
                buff.append(artifact != null ? artifact : "project");
                buff.append("</td><td>"); //NOI18N
                buff.append(values[index] != null ? TOOLTIP_YES() : TOOLTIP_NO());
                buff.append("</td></tr>");//NOI18N
                index++;
            }
            buff.append("</tbody></table>");//NOI18N

            return buff.toString();
        }


        @Override
        public Image getIcon(int type) {
             return getIconForCutHolder(getLookup().lookup(POMCutHolder.class));
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

    }

    static class PomChildren extends Children.Keys<POMCutHolder> implements PropertyChangeListener {
        private POMCutHolder parentHolder;
        private POMQNames names;
        private POMModelVisitor visitor;
        private Class type;
        private POMModelPanel.Configuration configuration;
        private List<POMCutHolder> children;

        @Override
        public Object clone() {
            return new PomChildren(parentHolder, names, type, configuration);
        }

        public PomChildren(POMCutHolder parent, POMQNames names, Class type, POMModelPanel.Configuration config) {
            this.parentHolder = parent;
            this.names = names;
            this.type = type;
            this.configuration = config;
        }

        private void reshow() {
            List<POMCutHolder> childs = children;
            if (childs != null) {
                for (POMCutHolder h : childs) {
                    if (!POMModelPanel.definesValue(h.getCutValues())) {
                        refreshKey(h);
                    }
                }
            }
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            final List<POMCutHolder> newKeys =
                    rescan(new POMModelVisitor(parentHolder, configuration));
            setKeys(newKeys);                        
            configuration.addPropertyChangeListener(this);
        }

        @Override
        protected void removeNotify() {
            super.removeNotify();
            children = null;
            configuration.removePropertyChangeListener(this);

        }

        private List<POMCutHolder> rescan(POMModelVisitor visitor) {
            try {
                Method m = POMModelVisitor.class.getMethod("visit", type); //NOI18N
                POMModel[] models = parentHolder.getSource();
                Object[] cuts = parentHolder.getCutValues();
                for (int i = 0; i < cuts.length; i++) {
                    Object cut = cuts[i];
                    // prevent deadlock 185923
                    if (cut != null && !type.isInstance(cut)) {
                        LOG.log(Level.WARNING, "#185428: {0} is not assignable to {1}", new Object[] {cut, type});
                        continue;
                    }
                    synchronized (i < models.length ? models[i] : /*#192042*/new Object()) {
                        m.invoke(visitor, cut);
                    }
                }
            } catch (Exception x) {
                LOG.log(Level.WARNING, null, x);
            }
            children = Arrays.asList(visitor.getChildValues());
            return children;
        }

        @Override
        protected Node[] createNodes(POMCutHolder childkey) {
            if (configuration.isFilterUndefined() && !POMModelPanel.definesValue(childkey.getCutValues())) {
                return new Node[0];
            }
            return new Node[] {childkey.createNode()};
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            reshow();
        }


    }

    static class PomListChildren<T extends POMComponent> extends Children.Keys<Lookup> implements PropertyChangeListener {
        private final Object[] one = new Object[] {new Object()};
        private final POMCutHolder holder;
        private final POMQNames names;
        private final Class type;
        private final KeyGenerator<T> keyGenerator;
        private final POMQName childName;
        private final POMModelPanel.Configuration configuration;
        private List<Lookup> keys;
        public PomListChildren(POMCutHolder holder, POMQNames names, Class type, KeyGenerator<T> generator, POMModelPanel.Configuration configuration, POMQName childName) {          
            this.holder = holder;
            this.names = names;
            this.type = type;
            this.keyGenerator = generator;
            this.childName = childName;
            this.configuration = configuration;
            this.configuration.addPropertyChangeListener(WeakListeners.propertyChange(this, this.configuration));
        }

        @Override
        protected void addNotify() {
            super.addNotify(); //To change body of generated methods, choose Tools | Templates.
            setKeysImpl();
            
        }

        private void setKeysImpl() {
            List<Lookup> toSet = new ArrayList<Lookup>();
            LinkedHashMap<Object, List<T>> cut = new LinkedHashMap<Object, List<T>>();

            int level = 0;
            for (Object comp : holder.getCutValues()) {
                if (comp == null) {
                    level++;
                    continue;
                }
                @SuppressWarnings("unchecked")
                List<T> lst = (List<T>) comp;
                for (T c : lst) {
                    if (c.getModel() == null) {
                        LOG.log(Level.WARNING, "#177548: null model for {0}", c);
                        continue;
                    }
                    Object keyGen = keyGenerator.generate(c);
                    List<T> currentCut = cut.get(keyGen);
                    if (currentCut == null) {
                        currentCut = new ArrayList<T>();
                        cut.put(keyGen, currentCut);
                    }
                    fillValues(level, currentCut, c);
                }
                level++;
            }
            for (List<T> lst : cut.values()) {
                T topMost = null;
                for (T c : lst) {
                    if (topMost == null) {
                        topMost = c;
                    }
                }

                String itemName = keyGenerator.createName(topMost);
                POMCutHolder cutHolder = new SingleObjectCH(holder, childName, itemName, type, configuration);
                for (T c : lst) {
                    cutHolder.addCut(c);
                }
                growToSize(holder.getCutsSize(), cutHolder);

                toSet.add(Lookups.fixed(cutHolder, childName, new PomChildren(cutHolder, names, type, configuration), itemName));
            }
            
            this.keys = toSet; //keys is the unsorted, natural order stuff..
            if (configuration.isSortLists()) {
                toSet = new ArrayList<>(keys);
                toSet.sort(lkpComparator);
            }
            setKeys(toSet);
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (POMModelPanel.Configuration.PROP_SORT_LISTS.equals(evt.getPropertyName())) {
                resort();
            }
        }

        public void resort() {
            if (keys != null) {
                if (configuration.isSortLists()) {
                    ArrayList<Lookup> toSet = new ArrayList<>(keys);
                    toSet.sort(lkpComparator);
                    setKeys(toSet);
                } else {
                    setKeys(keys);
                }
            }
        }
        
        private final Comparator<Lookup> lkpComparator = new Comparator<Lookup>() {

                    @Override
                    public int compare(Lookup o1, Lookup o2) {
                        String s1 = o1.lookup(String.class);
                        String s2 = o2.lookup(String.class);
                        return s1.compareTo(s2);
                    }
                };

        @Override
        protected Node[] createNodes(Lookup key) {
            POMCutHolder cutHolder = key.lookup(POMCutHolder.class);
            assert cutHolder != null;
            POMQName chldName = key.lookup(POMQName.class);
            assert chldName != null;
            PomChildren children = key.lookup(PomChildren.class);
            assert children != null;
            String itemName = key.lookup(String.class);
            assert itemName != null;
            return new Node[] {new ObjectNode(Lookups.fixed(cutHolder, chldName), (PomChildren)children.clone(), itemName)};
        }

        private void fillValues(int current, List<T> list, T value) {
            while (list.size() < current) {
                list.add(null);
            }
            list.add(value);
        }
    }

    static class PomStringListChildren extends Children.Keys<Object> {
        private final Object[] one = new Object[] {new Object()};
        private final POMCutHolder holder;
        private final POMQName childName;
        private final String displayName;

        public PomStringListChildren(POMCutHolder holder, POMQName childName, String displayName) {
            setKeys(one);
            this.holder = holder;
            this.childName = childName;
            this.displayName = displayName;
        }

        public void reshow() {
            this.refreshKey(one);
        }

        @Override
        protected Node[] createNodes(Object key) {
            List<Node> toRet = new ArrayList<Node>();
            LinkedHashMap<String, List<String>> cut = new LinkedHashMap<String, List<String>>();

            int level = 0;
            for (Object comp : holder.getCutValues()) {
                if (comp == null) {
                    level++;
                    continue;
                }
                @SuppressWarnings("unchecked")
                List<String> lst = (List<String>) comp;
                for (String c : lst) {
                    List<String> currentCut = cut.get(c);
                    if (currentCut == null) {
                        currentCut = new ArrayList<String>();
                        cut.put(c, currentCut);
                    }
                    fillValues(level, currentCut, c);
                }
                level++;
            }
            for (List<String> lst : cut.values()) {
                String topMost = null;
                for (String c : lst) {
                    if (topMost == null) {
                        topMost = c;
                    }
                }
                POMCutHolder cutHolder = new SingleObjectCH(holder, childName, topMost);
                for (String c : lst) {
                    cutHolder.addCut(c);
                }
                growToSize(holder.getCutsSize(), cutHolder);
                toRet.add(new SingleFieldNode(Lookups.fixed(cutHolder, childName), displayName));
            }

            return toRet.toArray(new Node[0]);
        }

        private void fillValues(int current, List<String> list, String value) {
            while (list.size() < current) {
                list.add(null);
            }
            list.add(value);
        }

    }


    static class SelectAction extends AbstractAction implements Presenter.Popup {
        private final Node node;
        private int layer = -1;

        SelectAction(Node node) {
            this.node = node;
        }

        SelectAction(Node node, int layer) {
            this.node = node;
            this.layer = layer;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            if (layer != -1) {
                POMModelPanel.selectByNode(node, layer);
            }
        }

        @Override
        @Messages({"ACT_Show=Show in POM", 
            "# {0} - artifactid of a project",
            "ACT_Current=Current: {0}",
            "# {0} - artifactid of a project",
            "ACT_PARENT=Parent: {0}"})
        public JMenuItem getPopupPresenter() {
            JMenu menu = new JMenu();
            menu.setText(ACT_Show());
            POMCutHolder pch = node.getLookup().lookup(POMCutHolder.class);
            POMModel[] mdls = pch.getSource();
            Object[] val = pch.getCutValues();
            int index = 0;
            for (POMModel mdl : mdls) {
                String artifact = mdl.getProject().getArtifactId();
                JMenuItem item = new JMenuItem();
                item.setAction(new SelectAction(node, index));
                if (index == 0) {
                    item.setText(ACT_Current(artifact != null ? artifact : "project"));
                } else {
                    item.setText(ACT_PARENT(artifact != null ? artifact : "project"));
                }
                item.setEnabled(/* #199345 */index < val.length && val[index] != null);
                menu.add(item);
                index++;
            }
            return menu;
        }

    }
   
}
