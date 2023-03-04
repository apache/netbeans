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

import java.util.Collections;
import org.netbeans.modules.maven.model.settings.Repository;
import org.netbeans.modules.maven.model.settings.RepositoryPolicy;
import org.netbeans.modules.maven.model.settings.SettingsComponent;
import org.netbeans.modules.maven.model.settings.SettingsComponentVisitor;
import org.netbeans.modules.maven.model.settings.SettingsModel;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class RepositoryImpl extends SettingsComponentImpl implements Repository {

    public RepositoryImpl(SettingsModel model, Element element) {
        super(model, element);
    }
    
    public RepositoryImpl(SettingsModel model, boolean pluginRepo) {
        this(model, createElementNS(model,
                pluginRepo ? model.getSettingsQNames().PLUGINREPOSITORY : model.getSettingsQNames().REPOSITORY));
    }

    // attributes

    // child elements
    @Override
    public RepositoryPolicy getReleases() {
        return getChild(RepositoryPolicy.class);
    }

    @Override
    public void setReleases(RepositoryPolicy releases) {
        setChild(RepositoryPolicy.class, getModel().getSettingsQNames().RELEASES.getName(), releases,
                Collections.<Class<? extends SettingsComponent>>emptyList());
    }

    @Override
    public RepositoryPolicy getSnapshots() {
        return getChild(RepositoryPolicy.class);
    }

    @Override
    public void setSnapshots(RepositoryPolicy snapshots) {
        setChild(RepositoryPolicy.class, getModel().getSettingsQNames().SNAPSHOTS.getName(), snapshots,
                Collections.<Class<? extends SettingsComponent>>emptyList());
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
    public String getName() {
        return getChildElementText(getModel().getSettingsQNames().NAME.getQName());
    }

    @Override
    public void setName(String name) {
        setChildElementText(getModel().getSettingsQNames().NAME.getName(), name,
                getModel().getSettingsQNames().NAME.getQName());
    }

    @Override
    public String getUrl() {
        return getChildElementText(getModel().getSettingsQNames().URL.getQName());
    }

    @Override
    public void setUrl(String url) {
        setChildElementText(getModel().getSettingsQNames().URL.getName(), url,
                getModel().getSettingsQNames().URL.getQName());
    }

    @Override
    public String getLayout() {
        return getChildElementText(getModel().getSettingsQNames().LAYOUT.getQName());
    }

    @Override
    public void setLayout(String layout) {
        setChildElementText(getModel().getSettingsQNames().LAYOUT.getName(), layout,
                getModel().getSettingsQNames().LAYOUT.getQName());
    }

    public static class RepoList extends ListImpl<Repository> {
        public RepoList(SettingsModel model, Element element) {
            super(model, element, model.getSettingsQNames().REPOSITORY, Repository.class);
        }

        public RepoList(SettingsModel model) {
            this(model, createElementNS(model, model.getSettingsQNames().REPOSITORIES));
        }
    }

    @Override
    public void accept(SettingsComponentVisitor visitor) {
        visitor.visit(this);
    }

    public static class PluginRepoList extends ListImpl<Repository> {
        public PluginRepoList(SettingsModel model, Element element) {
            super(model, element, model.getSettingsQNames().PLUGINREPOSITORY, Repository.class);
        }

        public PluginRepoList(SettingsModel model) {
            this(model, createElementNS(model, model.getSettingsQNames().PLUGINREPOSITORIES));
        }
    }

}
