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
package org.netbeans.modules.maven.model.pom.impl;

import java.util.ArrayList;
import org.netbeans.modules.maven.model.pom.*;
import org.netbeans.modules.maven.model.util.ModelImplUtils;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class ProfileImpl extends IdPOMComponentImpl implements Profile {

    private static final java.util.List<Class<? extends POMComponent>> ORDER;
    static {
        ORDER = new ArrayList<Class<? extends POMComponent>>();
        ORDER.add(POMExtensibilityElement.class);
        ORDER.add(Activation.class);
        ORDER.add(BuildBase.class);
        ORDER.add(StringList.class); //modules
        ORDER.add(RepositoryImpl.RepoList.class);
        ORDER.add(RepositoryImpl.PluginRepoList.class);
        ORDER.add(DependencyImpl.List.class);
        ORDER.add(Reporting.class);
        ORDER.add(DependencyManagement.class);
        ORDER.add(DistributionManagement.class);
        ORDER.add(Properties.class);
    }

    public ProfileImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public ProfileImpl(POMModel model) {
        this(model, createElementNS(model, model.getPOMQNames().PROFILE));
    }

    // attributes

    // child elements
    @Override
    public Activation getActivation() {
        return getChild(Activation.class);
    }

    @Override
    public void setActivation(Activation activation) {
        setChild(Activation.class, getModel().getPOMQNames().ACTIVATION.getName(), activation,
                getClassesBefore(ORDER, Activation.class));
    }

    @Override
    public BuildBase getBuildBase() {
        return getChild(BuildBase.class);
    }

    @Override
    public void setBuildBase(BuildBase buildBase) {
        setChild(BuildBase.class, getModel().getPOMQNames().BUILD.getName(), buildBase,
                getClassesBefore(ORDER, BuildBase.class));
    }

    @Override
    public java.util.List<Repository> getRepositories() {
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
    public java.util.List<Repository> getPluginRepositories() {
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
    public java.util.List<Dependency> getDependencies() {
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
    public Properties getProperties() {
        return getChild(Properties.class);
    }

    @Override
    public void setProperties(Properties props) {
        setChild(Properties.class, getModel().getPOMQNames().PROPERTIES.getName(), props,
                getClassesBefore(ORDER, Properties.class));
    }

    @Override
    public java.util.List<String> getModules() {
        java.util.List<StringList> lists = getChildren(StringList.class);
        for (StringList list : lists) {
            if (ModelImplUtils.hasName(list, getModel().getPOMQNames().MODULES.getName())) {
                return list.getListChildren();
            }
        }
        return null;
    }

    @Override
    public void addModule(String module) {
        java.util.List<StringList> lists = getChildren(StringList.class);
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
        java.util.List<StringList> lists = getChildren(StringList.class);
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
        java.util.List<Dependency> deps = getDependencies();
        if (deps != null) {
            for (Dependency d : deps) {
                if (groupId.equals(d.getGroupId()) && artifactId.equals(d.getArtifactId()) &&
                        (classifier == null || classifier.equals(d.getClassifier()))) {
                    return d;
                }
            }
        }
        return null;
    }

    public static class List extends ListImpl<Profile> {
        public List(POMModel model, Element element) {
            super(model, element, model.getPOMQNames().PROFILE, Profile.class);
        }

        public List(POMModel model) {
            this(model, createElementNS(model, model.getPOMQNames().PROFILES));
        }
    }


}
