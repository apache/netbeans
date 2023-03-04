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
package org.netbeans.modules.maven.model.settings.impl;

import org.netbeans.modules.maven.model.settings.Activation;
import org.netbeans.modules.maven.model.settings.ModelList;
import org.netbeans.modules.maven.model.settings.Profile;
import org.netbeans.modules.maven.model.settings.Properties;
import org.netbeans.modules.maven.model.settings.Repository;
import org.netbeans.modules.maven.model.settings.SettingsComponent;
import org.netbeans.modules.maven.model.settings.SettingsComponentVisitor;
import org.netbeans.modules.maven.model.settings.SettingsModel;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class ProfileImpl extends SettingsComponentImpl implements Profile {

    private static final Class<? extends SettingsComponent>[] ORDER = new Class[] {
        Activation.class,
        RepositoryImpl.RepoList.class,
        RepositoryImpl.PluginRepoList.class,
        Properties.class
    };

    public ProfileImpl(SettingsModel model, Element element) {
        super(model, element);
    }
    
    public ProfileImpl(SettingsModel model) {
        this(model, createElementNS(model, model.getSettingsQNames().PROFILE));
    }

    // attributes

    // child elements
    @Override
    public Activation getActivation() {
        return getChild(Activation.class);
    }

    @Override
    public void setActivation(Activation activation) {
        setChild(Activation.class, getModel().getSettingsQNames().ACTIVATION.getName(), activation,
                getClassesBefore(ORDER, Activation.class));
    }

    @Override
    public String getId() {
        return getChildElementText(getModel().getSettingsQNames().ID.getQName());
    }

    @Override
    public void setId(String id) {
        setChildElementText(getModel().getSettingsQNames().ID.getName(), id,
                getModel().getSettingsQNames().ID.getQName());
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
                    getModel().getSettingsQNames().REPOSITORIES.getName(),
                    getModel().getFactory().create(this, getModel().getSettingsQNames().REPOSITORIES.getQName()),
                    getClassesBefore(ORDER, RepositoryImpl.RepoList.class));
            childs = getChild(RepositoryImpl.RepoList.class);
            assert childs != null;
        }
        childs.addListChild(repo);
    }

    @Override
    public void removeRepository(Repository repo) {
        ModelList<Repository> childs = getChild(RepositoryImpl.RepoList.class);
        if (childs != null) {
            childs.removeListChild(repo);
        }
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
                    getModel().getSettingsQNames().PLUGINREPOSITORIES.getName(),
                    getModel().getFactory().create(this, getModel().getSettingsQNames().PLUGINREPOSITORIES.getQName()),
                    getClassesBefore(ORDER, RepositoryImpl.PluginRepoList.class));
            childs = getChild(RepositoryImpl.PluginRepoList.class);
            assert childs != null;
        }
        childs.addListChild(repo);
    }

    @Override
    public void removePluginRepository(Repository repo) {
        ModelList<Repository> childs = getChild(RepositoryImpl.PluginRepoList.class);
        if (childs != null) {
            childs.removeListChild(repo);
        }
    }



    @Override
    public Properties getProperties() {
        return getChild(Properties.class);
    }

    @Override
    public void setProperties(Properties props) {
        setChild(Properties.class, getModel().getSettingsQNames().PROPERTIES.getName(), props,
                getClassesBefore(ORDER, Properties.class));
    }


    @Override
    public void accept(SettingsComponentVisitor visitor) {
        visitor.visit(this);
    }


    public static class List extends ListImpl<Profile> {
        public List(SettingsModel model, Element element) {
            super(model, element, model.getSettingsQNames().PROFILE, Profile.class);
        }

        public List(SettingsModel model) {
            this(model, createElementNS(model, model.getSettingsQNames().PROFILES));
        }
    }


}
