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

import java.util.*;
import org.netbeans.modules.maven.model.pom.*;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class ReportingImpl extends POMComponentImpl implements Reporting {

    public ReportingImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public ReportingImpl(POMModel model) {
        this(model, createElementNS(model, model.getPOMQNames().REPORTING));
    }

    // attributes

    // child elements
    @Override
    public List<ReportPlugin> getReportPlugins() {
        ModelList<ReportPlugin> childs = getChild(ReportPluginImpl.List.class);
        if (childs != null) {
            return childs.getListChildren();
        }
        return null;
    }

    @Override
    public void addReportPlugin(ReportPlugin plugin) {
        ModelList<ReportPlugin> childs = getChild(ReportPluginImpl.List.class);
        if (childs == null) {
            setChild(ReportPluginImpl.List.class,
                    getModel().getPOMQNames().REPORTPLUGINS.getName(),
                    getModel().getFactory().create(this, getModel().getPOMQNames().REPORTPLUGINS.getQName()),
                    Collections.<Class<? extends POMComponent>>emptySet());
            childs = getChild(ReportPluginImpl.List.class);
            assert childs != null;
        }
        childs.addListChild(plugin);
    }

    @Override
    public void removeReportPlugin(ReportPlugin plugin) {
        remove(plugin, getModel().getPOMQNames().REPORTPLUGINS.getName(), ReportPluginImpl.List.class);
    }

    @Override
    public ReportPlugin findReportPluginById(String groupId, String artifactId) {
        assert groupId != null;
        assert artifactId != null;
        java.util.List<ReportPlugin> plgs = getReportPlugins();
        if (plgs != null) {
            for (ReportPlugin p : plgs) {
                if (groupId.equals(p.getGroupId()) && artifactId.equals(p.getArtifactId())) {
                    return p;
                }
            }
        }
        return null;
    }

    @Override
    public Boolean isExcludeDefaults() {
        String str = getChildElementText(getModel().getPOMQNames().EXCLUDEDEFAULTS.getQName());
        if (str != null) {
            return Boolean.valueOf(str);
        }
        return null;
    }

    @Override
    public void setExcludeDefaults(Boolean exclude) {
        setChildElementText(getModel().getPOMQNames().EXCLUDEDEFAULTS.getName(),
                exclude == null ? null : exclude.toString(),
                getModel().getPOMQNames().EXCLUDEDEFAULTS.getQName());
    }

    @Override
    public String getOutputDirectory() {
        return getChildElementText(getModel().getPOMQNames().OUTPUTDIRECTORY.getQName());
    }

    @Override
    public void setOutputDirectory(String directory) {
        setChildElementText(getModel().getPOMQNames().OUTPUTDIRECTORY.getName(), directory,
                getModel().getPOMQNames().OUTPUTDIRECTORY.getQName());
    }

    @Override
    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
    }
}
