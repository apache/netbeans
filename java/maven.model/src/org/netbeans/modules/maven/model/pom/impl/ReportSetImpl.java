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
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class ReportSetImpl extends IdPOMComponentImpl implements ReportSet {

    private static final java.util.List<Class<? extends POMComponent>> ORDER;
    static {
        ORDER = new ArrayList<Class<? extends POMComponent>>();
        ORDER.add(Configuration.class);
        ORDER.add(StringList.class); //reports
    }

    public ReportSetImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public ReportSetImpl(POMModel model) {
        this(model, createElementNS(model, model.getPOMQNames().REPORTSET));
    }

    // attributes


    @Override
    public Boolean isInherited() {
        String str = getChildElementText(getModel().getPOMQNames().INHERITED.getQName());
        if (str != null) {
            return Boolean.valueOf(str);
        }
        return Boolean.TRUE;
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
        setChild(Configuration.class, getModel().getPOMQNames().CONFIGURATION.getName(), config,
                getClassesBefore(ORDER, Configuration.class));
    }

    @Override
    public java.util.List<String> getReports() {
        java.util.List<StringList> lists = getChildren(StringList.class);
        for (StringList list : lists) {
            if (getModel().getPOMQNames().REPORTS.getName().equals(list.getPeer().getLocalName())) {
                return list.getListChildren();
            }
        }
        return null;
    }

    @Override
    public void addReport(String report) {
        java.util.List<StringList> lists = getChildren(StringList.class);
        for (StringList list : lists) {
            if (getModel().getPOMQNames().REPORTS.getName().equals(list.getPeer().getLocalName())) {
                list.addListChild(report);
                return;
            }
        }
        setChild(StringListImpl.class,
                 getModel().getPOMQNames().REPORTS.getName(),
                 getModel().getFactory().create(this, getModel().getPOMQNames().REPORTS.getQName()),
                 getClassesBefore(ORDER, StringListImpl.class));
        lists = getChildren(StringList.class);
        for (StringList list : lists) {
            if (getModel().getPOMQNames().REPORTS.getName().equals(list.getPeer().getLocalName())) {
                list.addListChild(report);
                return;
            }
        }
    }

    @Override
    public void removeReport(String report) {
        java.util.List<StringList> lists = getChildren(StringList.class);
        for (StringList list : lists) {
            if (getModel().getPOMQNames().REPORTS.getName().equals(list.getPeer().getLocalName())) {
                list.removeListChild(report);
                return;
            }
        }
    }

    // child elements
    @Override
    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
    }

    public static class List extends ListImpl<ReportSet> {
        public List(POMModel model, Element element) {
            super(model, element, model.getPOMQNames().REPORTSET, ReportSet.class);
        }

        public List(POMModel model) {
            this(model, createElementNS(model, model.getPOMQNames().REPORTSETS));
        }
    }


}
