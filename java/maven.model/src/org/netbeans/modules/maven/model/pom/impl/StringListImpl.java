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
public class StringListImpl extends POMComponentImpl implements StringList {
    private POMQName childname;

    protected StringListImpl(POMModel model, Element element, POMQName childs) {
        super(model, element);
        this.childname = childs;
    }

    public StringListImpl(POMModel model, POMQName listName, POMQName childs) {
        this(model, createElementNS(model, listName), childs);
    }


    // child elements
    @Override
    public List<String> getListChildren() {
        List<POMExtensibilityElement> el = getChildren(POMExtensibilityElement.class);
        List<String> toRet = new ArrayList<String>();
        for (POMExtensibilityElement elem : el) {
            if (elem.getQName().getLocalPart().equals(childname.getQName().getLocalPart())) {
                toRet.add(elem.getElementText());
            }
        }
        return toRet.size() > 0 ? toRet : null;
    }

    @Override
    public void addListChild(String child) {
        assert child != null;
        POMExtensibilityElement el = getModel().getFactory().createPOMExtensibilityElement(childname.getQName());
        el.setElementText(child);
        appendChild(childname.getName(), el);

    }

    @Override
    public void removeListChild(String child) {
        assert child != null;
        List<POMExtensibilityElement> el = getChildren(POMExtensibilityElement.class);
        for (POMExtensibilityElement elem : el) {
            if (elem.getQName().getLocalPart().equals(childname.getQName().getLocalPart()) && child.equals(elem.getElementText())) {
                removeChild(childname.getName(), elem);
                return;
            }
        }
    }



    // child elements
    @Override
    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
    }


}
