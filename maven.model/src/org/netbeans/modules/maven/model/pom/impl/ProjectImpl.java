/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.model.pom.impl;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.maven.model.pom.*;
import org.netbeans.modules.maven.model.util.ModelImplUtils;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class ProjectImpl extends VersionablePOMComponentImpl implements Project {

    private static final List<Class<? extends POMComponent>> ORDER;
    static {
        ORDER = new ArrayList<Class<? extends POMComponent>>();
        ORDER.add(Parent.class);
        ORDER.add(POMExtensibilityElement.class);
        ORDER.add(Prerequisites.class);
        ORDER.add(IssueManagement.class);
        ORDER.add(CiManagement.class);
        ORDER.add(MailingListImpl.List.class);
        ORDER.add(DeveloperImpl.List.class);
        ORDER.add(ContributorImpl.List.class);
        ORDER.add(LicenseImpl.List.class);
        ORDER.add(Scm.class);
        ORDER.add(Organization.class);
        ORDER.add(Build.class);
        ORDER.add(ProfileImpl.List.class);
        ORDER.add(StringListImpl.class); //modules
        ORDER.add(RepositoryImpl.RepoList.class);
        ORDER.add(RepositoryImpl.PluginRepoList.class);
        ORDER.add(DependencyImpl.List.class);
        ORDER.add(Reporting.class);
        ORDER.add(DependencyManagement.class);
        ORDER.add(DistributionManagement.class);
        ORDER.add(Properties.class);
    }

    public ProjectImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public ProjectImpl(POMModel model) {
        this(model, createElementNS(model, model.getPOMQNames().PROJECT));
    }

    // attributes

    // child elements
    @Override
    public Parent getPomParent() {
        return getChild(Parent.class);
    }

    @Override
    public void setPomParent(Parent parent) {
        setChild(Parent.class, getModel().getPOMQNames().PARENT.getName(), parent,
                getClassesBefore(ORDER, Parent.class));
    }

    @Override
    public Prerequisites getPrerequisites() {
        return getChild(Prerequisites.class);
    }

    @Override
    public void setPrerequisites(Prerequisites prerequisites) {
        setChild(Prerequisites.class, getModel().getPOMQNames().PREREQUISITES.getName(), prerequisites,
                getClassesBefore(ORDER, Prerequisites.class));
    }

    @Override
    public IssueManagement getIssueManagement() {
        return getChild(IssueManagement.class);
    }

    @Override
    public void setIssueManagement(IssueManagement issueManagement) {
        setChild(IssueManagement.class, getModel().getPOMQNames().ISSUEMANAGEMENT.getName(), issueManagement,
                getClassesBefore(ORDER, IssueManagement.class));
    }

    @Override
    public CiManagement getCiManagement() {
        return getChild(CiManagement.class);
    }

    @Override
    public void setCiManagement(CiManagement ciManagement) {
        setChild(CiManagement.class, getModel().getPOMQNames().CIMANAGEMENT.getName(), ciManagement,
                getClassesBefore(ORDER, CiManagement.class));
    }

    @Override
    public List<MailingList> getMailingLists() {
        ModelList<MailingList> childs = getChild(MailingListImpl.List.class);
        if (childs != null) {
            return childs.getListChildren();
        }
        return null;
    }

    @Override
    public void addMailingList(MailingList mailingList) {
        ModelList<MailingList> childs = getChild(MailingListImpl.List.class);
        if (childs == null) {
            setChild(MailingListImpl.List.class,
                    getModel().getPOMQNames().MAILINGLISTS.getName(),
                    getModel().getFactory().create(this, getModel().getPOMQNames().MAILINGLISTS.getQName()),
                    getClassesBefore(ORDER, MailingListImpl.List.class));
            childs = getChild(MailingListImpl.List.class);
            assert childs != null;
        }
        childs.addListChild(mailingList);
    }

    @Override
    public void removeMailingList(MailingList mailingList) {
        remove(mailingList, getModel().getPOMQNames().MAILINGLISTS.getName(), MailingListImpl.List.class);
    }

    @Override
    public List<Developer> getDevelopers() {
        ModelList<Developer> childs = getChild(DeveloperImpl.List.class);
        if (childs != null) {
            return childs.getListChildren();
        }
        return null;
    }

    @Override
    public void addDeveloper(Developer dev) {
        ModelList<Developer> childs = getChild(DeveloperImpl.List.class);
        if (childs == null) {
            setChild(DeveloperImpl.List.class,
                    getModel().getPOMQNames().DEVELOPERS.getName(),
                    getModel().getFactory().create(this, getModel().getPOMQNames().DEVELOPERS.getQName()),
                    getClassesBefore(ORDER, DeveloperImpl.List.class));
            childs = getChild(DeveloperImpl.List.class);
            assert childs != null;
        }
        childs.addListChild(dev);
    }

    @Override
    public void removeDeveloper(Developer dev) {
        remove(dev, getModel().getPOMQNames().DEVELOPERS.getName(), DeveloperImpl.List.class);
    }

    @Override
    public List<Contributor> getContributors() {
        ModelList<Contributor> childs = getChild(ContributorImpl.List.class);
        if (childs != null) {
            return childs.getListChildren();
        }
        return null;
    }

    @Override
    public void addContributor(Contributor contributor) {
        ModelList<Contributor> childs = getChild(ContributorImpl.List.class);
        if (childs == null) {
            setChild(ContributorImpl.List.class,
                    getModel().getPOMQNames().CONTRIBUTORS.getName(),
                    getModel().getFactory().create(this, getModel().getPOMQNames().CONTRIBUTORS.getQName()),
                    getClassesBefore(ORDER, ContributorImpl.List.class));
            childs = getChild(ContributorImpl.List.class);
            assert childs != null;
        }
        childs.addListChild(contributor);
    }

    @Override
    public void removeContributor(Contributor contributor) {
        remove(contributor, getModel().getPOMQNames().CONTRIBUTORS.getName(), ContributorImpl.List.class);
    }

    @Override
    public List<License> getLicenses() {
        ModelList<License> childs = getChild(LicenseImpl.List.class);
        if (childs != null) {
            return childs.getListChildren();
        }
        return null;
    }

    @Override
    public void addLicense(License license) {
        ModelList<License> childs = getChild(LicenseImpl.List.class);
        if (childs == null) {
            setChild(LicenseImpl.List.class,
                    getModel().getPOMQNames().LICENSES.getName(),
                    getModel().getFactory().create(this, getModel().getPOMQNames().LICENSES.getQName()),
                    getClassesBefore(ORDER, LicenseImpl.List.class));
            childs = getChild(LicenseImpl.List.class);
            assert childs != null;
        }
        childs.addListChild(license);
    }

    @Override
    public void removeLicense(License lic) {
        remove(lic, getModel().getPOMQNames().LICENSES.getName(), LicenseImpl.List.class);
    }

    @Override
    public Scm getScm() {
        return getChild(Scm.class);
    }

    @Override
    public void setScm(Scm scm) {
        setChild(Scm.class, getModel().getPOMQNames().SCM.getName(), scm,
                getClassesBefore(ORDER, Scm.class));
    }

    @Override
    public Organization getOrganization() {
        return getChild(Organization.class);
    }

    @Override
    public void setOrganization(Organization organization) {
        setChild(Organization.class, getModel().getPOMQNames().ORGANIZATION.getName(), organization,
                getClassesBefore(ORDER, Organization.class));
    }

    @Override
    public Build getBuild() {
        return getChild(Build.class);
    }

    @Override
    public void setBuild(Build build) {
        setChild(Build.class, getModel().getPOMQNames().BUILD.getName(), build,
                getClassesBefore(ORDER, Build.class));
    }

    @Override
    public List<Profile> getProfiles() {
        ModelList<Profile> childs = getChild(ProfileImpl.List.class);
        if (childs != null) {
            return childs.getListChildren();
        }
        return null;
    }

    @Override
    public void addProfile(Profile profile) {
        ModelList<Profile> childs = getChild(ProfileImpl.List.class);
        if (childs == null) {
            setChild(ProfileImpl.List.class,
                    getModel().getPOMQNames().PROFILES.getName(),
                    getModel().getFactory().create(this, getModel().getPOMQNames().PROFILES.getQName()),
                    getClassesBefore(ORDER, ProfileImpl.List.class));
            childs = getChild(ProfileImpl.List.class);
            assert childs != null;
        }
        childs.addListChild(profile);
    }

    @Override
    public void removeProfile(Profile profile) {
        remove(profile, getModel().getPOMQNames().PROFILES.getName(), ProfileImpl.List.class);
    }


    @Override
    public List<Repository> getRepositories() {
        ModelList<Repository> childs = getChild(RepositoryImpl.RepoList.class);
        if (childs != null) {
            return childs.getListChildren();
        }
        return null;
    }

    @Override
    public void addRepository(Repository repo) {
        ModelList<Repository> childs = getChild(RepositoryImpl.RepoList.class);
        if (childs == null) {
            setChild(RepositoryImpl.RepoList.class,
                    getModel().getPOMQNames().REPOSITORIES.getName(),
                    getModel().getFactory().create(this, getModel().getPOMQNames().REPOSITORIES.getQName()),
                    getClassesBefore(ORDER, RepositoryImpl.RepoList.class));
            childs = getChild(RepositoryImpl.RepoList.class);
            assert childs != null;
        }
        childs.addListChild(repo);
    }

    @Override
    public void removeRepository(Repository repo) {
        remove(repo, getModel().getPOMQNames().REPOSITORIES.getName(), RepositoryImpl.RepoList.class);
    }

    @Override
    public List<Repository> getPluginRepositories() {
        ModelList<Repository> childs = getChild(RepositoryImpl.PluginRepoList.class);
        if (childs != null) {
            return childs.getListChildren();
        }
        return null;
    }

    @Override
    public void addPluginRepository(Repository repo) {
        ModelList<Repository> childs = getChild(RepositoryImpl.PluginRepoList.class);
        if (childs == null) {
            setChild(RepositoryImpl.PluginRepoList.class,
                    getModel().getPOMQNames().PLUGINREPOSITORIES.getName(),
                    getModel().getFactory().create(this, getModel().getPOMQNames().PLUGINREPOSITORIES.getQName()),
                    getClassesBefore(ORDER, RepositoryImpl.PluginRepoList.class));
            childs = getChild(RepositoryImpl.PluginRepoList.class);
            assert childs != null;
        }
        childs.addListChild(repo);
    }

    @Override
    public void removePluginRepository(Repository repo) {
        remove(repo, getModel().getPOMQNames().PLUGINREPOSITORIES.getName(), RepositoryImpl.PluginRepoList.class);
    }
    


    @Override
    public List<Dependency> getDependencies() {
        ModelList<Dependency> childs = getChild(DependencyImpl.List.class);
        if (childs != null) {
            return childs.getListChildren();
        }
        return null;
    }

    @Override
    public void addDependency(Dependency dep) {
        ModelList<Dependency> childs = getChild(DependencyImpl.List.class);
        if (childs == null) {
            setChild(DependencyImpl.List.class,
                    getModel().getPOMQNames().DEPENDENCIES.getName(),
                    getModel().getFactory().create(this, getModel().getPOMQNames().DEPENDENCIES.getQName()),
                    getClassesBefore(ORDER, DependencyImpl.List.class));
            childs = getChild(DependencyImpl.List.class);
            assert childs != null;
        }
        childs.addListChild(dep);
    }

    @Override
    public void removeDependency(Dependency dep) {
         remove(dep, getModel().getPOMQNames().DEPENDENCIES.getName(), DependencyImpl.List.class);
    }

    @Override
    public Reporting getReporting() {
        return getChild(Reporting.class);
    }

    @Override
    public void setReporting(Reporting reporting) {
        setChild(Reporting.class, getModel().getPOMQNames().REPORTING.getName(), reporting,
                getClassesBefore(ORDER, Reporting.class));
    }

    @Override
    public DependencyManagement getDependencyManagement() {
        return getChild(DependencyManagement.class);
    }

    @Override
    public void setDependencyManagement(DependencyManagement dependencyManagement) {
        setChild(DependencyManagement.class, getModel().getPOMQNames().DEPENDENCYMANAGEMENT.getName(), dependencyManagement,
                getClassesBefore(ORDER, DependencyManagement.class));
    }

    @Override
    public DistributionManagement getDistributionManagement() {
        return getChild(DistributionManagement.class);
    }

    @Override
    public void setDistributionManagement(DistributionManagement distributionManagement) {
        setChild(DistributionManagement.class, getModel().getPOMQNames().DISTRIBUTIONMANAGEMENT.getName(), distributionManagement,
                getClassesBefore(ORDER, DistributionManagement.class));
    }

    @Override
    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getModelVersion() {
        return getChildElementText(getModel().getPOMQNames().MODELVERSION.getQName());
    }

    @Override
    public String getPackaging() {
        String packaging = getChildElementText(getModel().getPOMQNames().PACKAGING.getQName());
        return packaging != null ? packaging : "jar";
    }

    @Override
    public void setPackaging(String pack) {
        setChildElementText(getModel().getPOMQNames().PACKAGING.getName(), pack,
                getModel().getPOMQNames().PACKAGING.getQName());
    }

    @Override
    public String getName() {
        return getChildElementText(getModel().getPOMQNames().NAME.getQName());
    }

    @Override
    public void setName(String name) {
        setChildElementText(getModel().getPOMQNames().NAME.getName(), name,
                getModel().getPOMQNames().NAME.getQName());
    }

    @Override
    public String getDescription() {
        return getChildElementText(getModel().getPOMQNames().DESCRIPTION.getQName());
    }

    @Override
    public void setDescription(String description) {
        setChildElementText(getModel().getPOMQNames().DESCRIPTION.getName(), description,
                getModel().getPOMQNames().DESCRIPTION.getQName());
    }

    @Override
    public String getURL() {
        return getChildElementText(getModel().getPOMQNames().URL.getQName());
    }

    @Override
    public void setURL(String url) {
        setChildElementText(getModel().getPOMQNames().URL.getName(), url,
                getModel().getPOMQNames().URL.getQName());
    }

    @Override
    public String getInceptionYear() {
        return getChildElementText(getModel().getPOMQNames().INCEPTIONYEAR.getQName());
    }

    @Override
    public void setInceptionYear(String inceptionYear) {
        setChildElementText(getModel().getPOMQNames().INCEPTIONYEAR.getName(), inceptionYear,
                getModel().getPOMQNames().INCEPTIONYEAR.getQName());
    }

    @Override
    public Properties getProperties() {
        return getChild(Properties.class);
    }

    @Override
    public void setProperties(Properties props) {
        setChild(Reporting.class, getModel().getPOMQNames().PROPERTIES.getName(), props,
                getClassesBefore(ORDER, Properties.class));
    }

    @Override
    public List<String> getModules() {
        List<StringList> lists = getChildren(StringList.class);
        for (StringList list : lists) {
            if (ModelImplUtils.hasName(list, getModel().getPOMQNames().MODULES.getName())) {
                return list.getListChildren();
            }
        }
        return null;
    }

    @Override
    public void addModule(String module) {
        List<StringList> lists = getChildren(StringList.class);
        for (StringList list : lists) {
            if (ModelImplUtils.hasName(list, getModel().getPOMQNames().MODULES.getName())) {
                list.addListChild(module);
                return;
            }
        }
        setChild(StringListImpl.class,
                 getModel().getPOMQNames().MODULES.getName(),
                 getModel().getFactory().create(this, getModel().getPOMQNames().MODULES.getQName()),
                 getClassesBefore(ORDER, StringListImpl.class));
        lists = getChildren(StringList.class);
        for (StringList list : lists) {
            if (ModelImplUtils.hasName(list, getModel().getPOMQNames().MODULES.getName())) {
                list.addListChild(module);
                return;
            }
        }
    }

    @Override
    public void removeModule(String module) {
        List<StringList> lists = getChildren(StringList.class);
        for (StringList list : lists) {
            if (ModelImplUtils.hasName(list, getModel().getPOMQNames().MODULES.getName())) {
                list.removeListChild(module);
                return;
            }
        }
    }

    @Override
    public Dependency findDependencyById(String groupId, String artifactId, String classifier) {
        assert groupId != null;
        assert artifactId != null;
        List<Dependency> deps = getDependencies();
        if (deps != null) {
            for (Dependency d : deps) {
                String realGroupId = d.getGroupId();
                if ("${project.groupId}".equals(realGroupId)) {
                    realGroupId = getGroupId();
                    if (realGroupId == null) {
                        Parent parent = getPomParent();
                        if (parent != null) {
                            realGroupId = parent.getGroupId();
                        }
                    }
                }
                if (groupId.equals(realGroupId) && artifactId.equals(d.getArtifactId()) &&
                        (classifier == null || classifier.equals(d.getClassifier()))) {
                    return d;
                }
            }
        }
        return null;
    }

    @Override
    public Profile findProfileById(String id) {
        assert id != null;
        List<Profile> profiles = getProfiles();
        if (profiles != null) {
            for (Profile p : profiles) {
                if (id.equals(p.getId())) {
                    return p;
                }
            }
        }
        return null;
    }

    public static class PList extends DependencyImpl.List {

        public PList(POMModel model, Element element) {
            super(model, element);
        }

        public PList(POMModel model) {
            super(model);
        }

        @Override
        protected DependencyContainer getDependencyContainer() {
            return getModel().getProject();
        }

    }
}
