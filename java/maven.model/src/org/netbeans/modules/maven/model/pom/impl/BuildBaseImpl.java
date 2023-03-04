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
import java.util.List;	
import org.netbeans.modules.maven.model.pom.*;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class BuildBaseImpl extends POMComponentImpl implements BuildBase {

    public BuildBaseImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public BuildBaseImpl(POMModel model) {
        this(model, createElementNS(model, model.getPOMQNames().BUILD));
    }

    protected List<Class<? extends POMComponent>> getOrder() {
        List<Class<? extends POMComponent>> order = new ArrayList<Class<? extends POMComponent>>();
        order.add(ResourceImpl.ResList.class);
        order.add(ResourceImpl.TestResList.class);
        order.add(PluginManagement.class);
        order.add(PluginImpl.List.class);
        return order;
    }

    // attributes

    // child elements
    @Override
    public List<Resource> getResources() {
        ModelList<Resource> childs = getChild(ResourceImpl.ResList.class);
        if (childs != null) {
            return childs.getListChildren();
        }
        return null;
    }

    @Override
    public void addResource(Resource res) {
        ModelList<Resource> childs = getChild(ResourceImpl.ResList.class);
        if (childs == null) {
            setChild(ResourceImpl.ResList.class,
                    getModel().getPOMQNames().RESOURCES.getName(),
                    getModel().getFactory().create(this, getModel().getPOMQNames().RESOURCES.getQName()),
                    getClassesBefore(getOrder(), ResourceImpl.ResList.class));
            childs = getChild(ResourceImpl.ResList.class);
            assert childs != null;
        }
        childs.addListChild(res);
    }

    @Override
    public void removeResource(Resource res) {
        remove(res, getModel().getPOMQNames().RESOURCES.getName(), ResourceImpl.ResList.class);
    }

    @Override
    public List<Resource> getTestResources() {
        ModelList<Resource> childs = getChild(ResourceImpl.TestResList.class);
        if (childs != null) {
            return childs.getListChildren();
        }
        return null;
    }

    @Override
    public void addTestResource(Resource res) {
        ModelList<Resource> childs = getChild(ResourceImpl.TestResList.class);
        if (childs == null) {
            setChild(ResourceImpl.TestResList.class,
                    getModel().getPOMQNames().TESTRESOURCES.getName(),
                    getModel().getFactory().create(this, getModel().getPOMQNames().TESTRESOURCES.getQName()),
                    getClassesBefore(getOrder(), ResourceImpl.TestResList.class));
            childs = getChild(ResourceImpl.TestResList.class);
            assert childs != null;
        }
        childs.addListChild(res);
    }

    @Override
    public void removeTestResource(Resource res) {
        remove(res, getModel().getPOMQNames().TESTRESOURCES.getName(), ResourceImpl.TestResList.class);    
    }

    @Override
    public PluginManagement getPluginManagement() {
        return getChild(PluginManagement.class);
    }

    @Override
    public void setPluginManagement(PluginManagement pluginManagement) {
        setChild(PluginManagement.class, getModel().getPOMQNames().PLUGINMANAGEMENT.getName(), pluginManagement,
                getClassesBefore(getOrder(), PluginManagement.class));
    }

    @Override
    public List<Plugin> getPlugins() {
        ModelList<Plugin> childs = getChild(PluginImpl.List.class);
        if (childs != null) {
            return childs.getListChildren();
        }
        return null;
    }

    @Override
    public void addPlugin(Plugin plugin) {
        ModelList<Plugin> childs = getChild(PluginImpl.List.class);
        if (childs == null) {
            setChild(PluginImpl.List.class,
                    getModel().getPOMQNames().PLUGINS.getName(),
                    getModel().getFactory().create(this, getModel().getPOMQNames().PLUGINS.getQName()),
                    getClassesBefore(getOrder(), PluginImpl.List.class));
            childs = getChild(PluginImpl.List.class);
            assert childs != null;
        }
        childs.addListChild(plugin);
    }

    @Override
    public void removePlugin(Plugin plugin) {
        remove(plugin, getModel().getPOMQNames().PLUGINS.getName(), PluginImpl.List.class);        
    }

    @Override
    public Plugin findPluginById(String groupId, String artifactId) {
        assert groupId != null;
        assert artifactId != null;
        List<Plugin> plugs = getPlugins();
        if (plugs != null) {
            for (Plugin plug : plugs) {
                String plugGroupId = plug.getGroupId();
                if (plugGroupId == null) {
                    plugGroupId = "org.apache.maven.plugins"; //the default groupId
                }
                if (groupId.equals(plugGroupId) && artifactId.equals(plug.getArtifactId())) {
                    return plug;
                }
            }
        }
        return null;
    }

    @Override
    public String getDefaultGoal() {
        return getChildElementText(getModel().getPOMQNames().DEFAULTGOAL.getQName());
    }

    @Override
    public void setDefaultGoal(String goal) {
        setChildElementText(getModel().getPOMQNames().DEFAULTGOAL.getName(), goal,
                getModel().getPOMQNames().DEFAULTGOAL.getQName());
    }

    @Override
    public String getDirectory() {
        return getChildElementText(getModel().getPOMQNames().DIRECTORY.getQName());
    }

    @Override
    public void setDirectory(String directory) {
        setChildElementText(getModel().getPOMQNames().DIRECTORY.getName(), directory,
                getModel().getPOMQNames().DIRECTORY.getQName());
    }


    @Override
    public String getFinalName() {
        return getChildElementText(getModel().getPOMQNames().FINALNAME.getQName());
    }

    @Override
    public void setFinalName(String finalName) {
        setChildElementText(getModel().getPOMQNames().FINALNAME.getName(), finalName,
                getModel().getPOMQNames().FINALNAME.getQName());
    }

    @Override
    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
    }


}
