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
public class PluginImpl extends VersionablePOMComponentImpl implements Plugin {

    private static final java.util.List<Class<? extends POMComponent>> ORDER;
    static {
        ORDER = new ArrayList<Class<? extends POMComponent>>();
        ORDER.add(POMExtensibilityElement.class);
        ORDER.add(PluginExecutionImpl.List.class);
        ORDER.add(DependencyImpl.List.class);
        ORDER.add(StringList.class); //goals
        ORDER.add(Configuration.class);
    }
    
    public PluginImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public PluginImpl(POMModel model) {
        this(model, createElementNS(model, model.getPOMQNames().PLUGIN));
    }

    // attributes

    // child elements
    @Override
    public java.util.List<PluginExecution> getExecutions() {
        ModelList<PluginExecution> childs = getChild(PluginExecutionImpl.List.class);
        if (childs != null) {
            return childs.getListChildren();
        }
        return null;
    }

    @Override
    public void addExecution(PluginExecution execution) {
        ModelList<PluginExecution> childs = getChild(PluginExecutionImpl.List.class);
        if (childs == null) {
            setChild(PluginExecutionImpl.List.class,
                    getModel().getPOMQNames().EXECUTIONS.getName(),
                    getModel().getFactory().create(this, getModel().getPOMQNames().EXECUTIONS.getQName()),
                    getClassesBefore(ORDER, PluginExecutionImpl.List.class));
            childs = getChild(PluginExecutionImpl.List.class);
            assert childs != null;
        }
        childs.addListChild(execution);
    }

    @Override
    public void removeExecution(PluginExecution execution) {
        remove(execution, getModel().getPOMQNames().EXECUTIONS.getName(), PluginExecutionImpl.List.class);
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


    @Override
    public Boolean isExtensions() {
        String str = getChildElementText(getModel().getPOMQNames().EXTENSIONS.getQName());
        if (str != null) {
            return Boolean.valueOf(str);
        }
        return null;
    }

    @Override
    public void setExtensions(Boolean extensions) {
        setChildElementText(getModel().getPOMQNames().EXTENSIONS.getName(),
                extensions == null ? null : extensions.toString(),
                getModel().getPOMQNames().EXTENSIONS.getQName());
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
    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
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
    public PluginExecution findExecutionById(String id) {
        assert id != null;
        java.util.List<PluginExecution> execs = getExecutions();
        if (execs != null) {
            for (PluginExecution e : execs) {
                if (id.equals(e.getId())) {
                    return e;
                }
            }
        }
        return null;
    }

    @Override
    public java.util.List<String> getGoals() {
        java.util.List<StringList> lists = getChildren(StringList.class);
        for (StringList list : lists) {
            if (ModelImplUtils.hasName(list, getModel().getPOMQNames().GOALS.getName())) {
                return list.getListChildren();
            }
        }
        return null;
    }

    @Override
    public void addGoal(String goal) {
        java.util.List<StringList> lists = getChildren(StringList.class);
        for (StringList list : lists) {
            if (ModelImplUtils.hasName(list, getModel().getPOMQNames().GOALS.getName())) {
                list.addListChild(goal);
                return;
            }
        }
        setChild(StringListImpl.class,
                 getModel().getPOMQNames().GOALS.getName(),
                 getModel().getFactory().create(this, getModel().getPOMQNames().GOALS.getQName()),
                 getClassesBefore(ORDER, StringList.class));
        lists = getChildren(StringList.class);
        for (StringList list : lists) {
            if (ModelImplUtils.hasName(list, getModel().getPOMQNames().GOALS.getName())) {
                list.addListChild(goal);
                return;
            }
        }
    }

    @Override
    public void removeGoal(String goal) {
        java.util.List<StringList> lists = getChildren(StringList.class);
        for (StringList list : lists) {
            if (ModelImplUtils.hasName(list, getModel().getPOMQNames().GOALS.getName())) {
                list.removeListChild(goal);
                return;
            }
        }
    }

    
    public static class List extends ListImpl<Plugin> {
        public List(POMModel model, Element element) {
            super(model, element, model.getPOMQNames().PLUGIN, Plugin.class);
        }

        public List(POMModel model) {
            this(model, createElementNS(model, model.getPOMQNames().PLUGINS));
        }
    }


}
