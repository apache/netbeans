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
