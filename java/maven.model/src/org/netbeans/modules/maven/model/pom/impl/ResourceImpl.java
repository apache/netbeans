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
import org.netbeans.modules.maven.model.util.ModelImplUtils;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class ResourceImpl extends POMComponentImpl implements Resource {

    private static final List<Class<? extends POMComponent>> ORDER;
    static {
        ORDER = new ArrayList<Class<? extends POMComponent>>();
        ORDER.add(POMExtensibilityElement.class);
        ORDER.add(StringListImpl.class); //resources
    }

    public ResourceImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public ResourceImpl(POMModel model, boolean testResource) {
        this(model, createElementNS(model, testResource ? model.getPOMQNames().TESTRESOURCE : model.getPOMQNames().RESOURCE));
    }

    // attributes

    // child elements
    @Override
    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
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
    public String getTargetPath() {
        return getChildElementText(getModel().getPOMQNames().TARGETPATH.getQName());
    }

    @Override
    public void setTargetPath(String path) {
        setChildElementText(getModel().getPOMQNames().TARGETPATH.getName(), path,
                getModel().getPOMQNames().TARGETPATH.getQName());
    }

    @Override
    public Boolean isFiltering() {
        String str = getChildElementText(getModel().getPOMQNames().FILTERING.getQName());
        if (str != null) {
            return Boolean.valueOf(str);
        }
        return null;
    }

    @Override
    public void setFiltering(Boolean filtering) {
        setChildElementText(getModel().getPOMQNames().FILTERING.getName(),
                filtering == null ? null : filtering.toString(),
                getModel().getPOMQNames().FILTERING.getQName());
    }

    @Override
    public List<String> getIncludes() {
        List<StringList> lists = getChildren(StringList.class);
        for (StringList list : lists) {
            if (ModelImplUtils.hasName(list, getModel().getPOMQNames().INCLUDES.getName())) {
                return list.getListChildren();
            }
        }
        return null;
    }

    @Override
    public void addInclude(String include) {
        List<StringList> lists = getChildren(StringList.class);
        for (StringList list : lists) {
            if (ModelImplUtils.hasName(list, getModel().getPOMQNames().INCLUDES.getName())) {
                list.addListChild(include);
                return;
            }
        }
        setChild(StringListImpl.class,
                 getModel().getPOMQNames().INCLUDES.getName(),
                 getModel().getFactory().create(this, getModel().getPOMQNames().INCLUDES.getQName()),
                 getClassesBefore(ORDER, StringListImpl.class));
        lists = getChildren(StringList.class);
        for (StringList list : lists) {
            if (ModelImplUtils.hasName(list, getModel().getPOMQNames().INCLUDES.getName())) {
                list.addListChild(include);
                return;
            }
        }
    }

    @Override
    public void removeInclude(String include) {
        List<StringList> lists = getChildren(StringList.class);
        for (StringList list : lists) {
            if (ModelImplUtils.hasName(list, getModel().getPOMQNames().INCLUDES.getName())) {
                list.removeListChild(include);
                return;
            }
        }
    }

    @Override
    public List<String> getExcludes() {
        List<StringList> lists = getChildren(StringList.class);
        for (StringList list : lists) {
            if (ModelImplUtils.hasName(list, getModel().getPOMQNames().EXCLUDES.getName())) {
                return list.getListChildren();
            }
        }
        return null;
    }

    @Override
    public void addExclude(String exclude) {
        List<StringList> lists = getChildren(StringList.class);
        for (StringList list : lists) {
            if (ModelImplUtils.hasName(list, getModel().getPOMQNames().EXCLUDES.getName())) {
                list.addListChild(exclude);
                return;
            }
        }
        setChild(StringListImpl.class,
                 getModel().getPOMQNames().EXCLUDES.getName(),
                 getModel().getFactory().create(this, getModel().getPOMQNames().EXCLUDES.getQName()),
                 getClassesBefore(ORDER, StringListImpl.class));
        lists = getChildren(StringList.class);
        for (StringList list : lists) {
            if (ModelImplUtils.hasName(list, getModel().getPOMQNames().EXCLUDES.getName())) {
                list.addListChild(exclude);
                return;
            }
        }
    }

    @Override
    public void removeExclude(String exclude) {
        List<StringList> lists = getChildren(StringList.class);
        for (StringList list : lists) {
            if (ModelImplUtils.hasName(list, getModel().getPOMQNames().EXCLUDES.getName())) {
                list.removeListChild(exclude);
                return;
            }
        }
    }
    
    public static class ResList extends ListImpl<Resource> {
        public ResList(POMModel model, Element element) {
            super(model, element, model.getPOMQNames().RESOURCE, Resource.class);
        }

        public ResList(POMModel model) {
            this(model, createElementNS(model, model.getPOMQNames().RESOURCES));
        }
    }

    public static class TestResList extends ListImpl<Resource> {
        public TestResList(POMModel model, Element element) {
            super(model, element, model.getPOMQNames().TESTRESOURCE, Resource.class);
        }

        public TestResList(POMModel model) {
            this(model, createElementNS(model, model.getPOMQNames().TESTRESOURCES));
        }
    }

}
