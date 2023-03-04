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
public class ReportPluginImpl extends VersionablePOMComponentImpl implements ReportPlugin {

    public ReportPluginImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public ReportPluginImpl(POMModel model) {
        this(model, createElementNS(model, model.getPOMQNames().REPORTPLUGIN));
    }

    // attributes

    // child elements
    @Override
    public java.util.List<ReportSet> getReportSets() {
        ModelList<ReportSet> childs = getChild(ReportSetImpl.List.class);
        if (childs != null) {
            return childs.getListChildren();
        }
        return null;
    }

    @Override
    public void addReportSet(ReportSet reportSet) {
        ModelList<ReportSet> childs = getChild(ReportSetImpl.List.class);
        if (childs == null) {
            setChild(ReportSetImpl.List.class,
                    getModel().getPOMQNames().REPORTSETS.getName(),
                    getModel().getFactory().create(this, getModel().getPOMQNames().REPORTSETS.getQName()),
                    Collections.<Class<? extends POMComponent>>emptySet());
            childs = getChild(ReportSetImpl.List.class);
            assert childs != null;
        }
        childs.addListChild(reportSet);
    }

    @Override
    public void removeReportSet(ReportSet reportSet) {
        ModelList<ReportSet> childs = getChild(ReportSetImpl.List.class);
        if (childs != null) {
            childs.removeListChild(reportSet);
        }
    }


    @Override
    public Boolean isInherited() {
        String str = getChildElementText(getModel().getPOMQNames().INHERITED.getQName());
        if (str != null) {
            return Boolean.valueOf(str);
        }
        return null;
    }

    @Override
    public void setInherited(Boolean inherited) {
        setChildElementText(getModel().getPOMQNames().INHERITED.getName(),
                inherited == null ? null : inherited.toString(),
                getModel().getPOMQNames().INHERITED.getQName());
    }

    @Override
    public Configuration getConfiguration() {
        return getChild(Configuration.class);
    }

    @Override
    public void setConfiguration(Configuration config) {
        java.util.List<Class<? extends POMComponent>> empty = Collections.emptyList();
        setChild(Configuration.class, getModel().getPOMQNames().CONFIGURATION.getName(), config, empty);
    }

    @Override
    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
    }

    
    public static class List extends ListImpl<ReportPlugin> {
        public List(POMModel model, Element element) {
            super(model, element, model.getPOMQNames().REPORTPLUGIN, ReportPlugin.class);
        }

        public List(POMModel model) {
            this(model, createElementNS(model, model.getPOMQNames().REPORTPLUGINS));
        }
    }

}
