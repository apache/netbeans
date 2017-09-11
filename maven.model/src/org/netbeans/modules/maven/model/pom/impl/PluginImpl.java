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
