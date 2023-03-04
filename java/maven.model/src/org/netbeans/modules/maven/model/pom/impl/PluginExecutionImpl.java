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
public class PluginExecutionImpl extends IdPOMComponentImpl implements PluginExecution {

    private static final java.util.List<Class<? extends POMComponent>> ORDER;
    static {
        ORDER = new ArrayList<Class<? extends POMComponent>>();
        ORDER.add(StringList.class); //goals
        ORDER.add(Configuration.class);
    }

    public PluginExecutionImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public PluginExecutionImpl(POMModel model) {
        this(model, createElementNS(model, model.getPOMQNames().EXECUTION));
    }

    // attributes

    // child elements
    @Override
    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getPhase() {
        return getChildElementText(getModel().getPOMQNames().PHASE.getQName());
    }

    @Override
    public void setPhase(String phase) {
        setChildElementText(getModel().getPOMQNames().PHASE.getName(), phase,
                getModel().getPOMQNames().PHASE.getQName());
    }

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
    public java.util.List<String> getGoals() {
        java.util.List<StringList> lists = getChildren(StringList.class);
        for (StringList list : lists) {
            if (getModel().getPOMQNames().GOALS.getName().equals(list.getPeer().getLocalName())) {
                return list.getListChildren();
            }
        }
        return null;
    }

    @Override
    public void addGoal(String goal) {
        java.util.List<StringList> lists = getChildren(StringList.class);
        for (StringList list : lists) {
            if (getModel().getPOMQNames().GOALS.getName().equals(list.getPeer().getLocalName())) {
                list.addListChild(goal);
                return;
            }
        }
        setChild(StringListImpl.class,
                 getModel().getPOMQNames().GOALS.getName(),
                 getModel().getFactory().create(this, getModel().getPOMQNames().GOALS.getQName()),
                 getClassesBefore(ORDER, StringListImpl.class));
        lists = getChildren(StringList.class);
        for (StringList list : lists) {
            if (getModel().getPOMQNames().GOALS.getName().equals(list.getPeer().getLocalName())) {
                list.addListChild(goal);
                return;
            }
        }
    }

    @Override
    public void removeGoal(String goal) {
        java.util.List<StringList> lists = getChildren(StringList.class);
        for (StringList list : lists) {
            if (getModel().getPOMQNames().GOALS.getName().equals(list.getPeer().getLocalName())) {
                list.removeListChild(goal);
                return;
            }
        }
    }

    public static class List extends ListImpl<PluginExecution> {
        public List(POMModel model, Element element) {
            super(model, element, model.getPOMQNames().EXECUTION, PluginExecution.class);
        }

        public List(POMModel model) {
            this(model, createElementNS(model, model.getPOMQNames().EXECUTIONS));
        }
    }

}
