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
