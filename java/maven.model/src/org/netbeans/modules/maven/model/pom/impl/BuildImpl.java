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
public class BuildImpl extends BuildBaseImpl implements Build {

    public BuildImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public BuildImpl(POMModel model) {
        this(model, createElementNS(model, model.getPOMQNames().BUILD));
    }

    @Override
    protected List<Class<? extends POMComponent>> getOrder() {
        List<Class<? extends POMComponent>> order = new ArrayList<Class<? extends POMComponent>>();
        order.add(ExtensionImpl.List.class);
        order.addAll(super.getOrder());
        return order;
    }


    // attributes

    // child elements
    @Override
    public List<Extension> getExtensions() {
        ModelList<Extension> childs = getChild(ExtensionImpl.List.class);
        if (childs != null) {
            return childs.getListChildren();
        }
        return null;
    }

    @Override
    public void addExtension(Extension extension) {
        ModelList<Extension> childs = getChild(ExtensionImpl.List.class);
        if (childs == null) {
            setChild(ExtensionImpl.List.class,
                    getModel().getPOMQNames().EXTENSIONS.getName(),
                    getModel().getFactory().create(this, getModel().getPOMQNames().EXTENSIONS.getQName()),
                    getClassesBefore(getOrder(), ExtensionImpl.List.class));
            childs = getChild(ExtensionImpl.List.class);
            assert childs != null;
        }
        childs.addListChild(extension);
    }

    @Override
    public void removeExtension(Extension extension) {
        remove(extension, getModel().getPOMQNames().EXTENSIONS.getName(), ExtensionImpl.List.class);
    }


    @Override
    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getSourceDirectory() {
        return getChildElementText(getModel().getPOMQNames().SOURCEDIRECTORY.getQName());
    }

    @Override
    public void setSourceDirectory(String directory) {
        setChildElementText(getModel().getPOMQNames().SOURCEDIRECTORY.getName(), directory,
                getModel().getPOMQNames().SOURCEDIRECTORY.getQName());
    }

    @Override
    public String getScriptSourceDirectory() {
        return getChildElementText(getModel().getPOMQNames().SCRIPTSOURCEDIRECTORY.getQName());
    }

    @Override
    public void setScriptSourceDirectory(String directory) {
        setChildElementText(getModel().getPOMQNames().SCRIPTSOURCEDIRECTORY.getName(), directory,
                getModel().getPOMQNames().SCRIPTSOURCEDIRECTORY.getQName());
    }

    @Override
    public String getTestSourceDirectory() {
        return getChildElementText(getModel().getPOMQNames().TESTSOURCEDIRECTORY.getQName());
    }

    @Override
    public void setTestSourceDirectory(String directory) {
        setChildElementText(getModel().getPOMQNames().TESTSOURCEDIRECTORY.getName(), directory,
                getModel().getPOMQNames().TESTSOURCEDIRECTORY.getQName());
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
    public String getTestOutputDirectory() {
        return getChildElementText(getModel().getPOMQNames().TESTOUTPUTDIRECTORY.getQName());
    }

    @Override
    public void setTestOutputDirectory(String directory) {
        setChildElementText(getModel().getPOMQNames().TESTOUTPUTDIRECTORY.getName(), directory,
                getModel().getPOMQNames().TESTOUTPUTDIRECTORY.getQName());
    }

}
