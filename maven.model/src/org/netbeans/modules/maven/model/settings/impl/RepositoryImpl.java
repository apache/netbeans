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
