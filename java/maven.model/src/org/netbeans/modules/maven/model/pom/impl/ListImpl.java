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

import java.util.List;
import org.netbeans.modules.maven.model.pom.*;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class ListImpl<T extends POMComponent> extends POMComponentImpl implements ModelList<T> {
    protected POMQName childname;
    private Class<T> clazz;

    protected ListImpl(POMModel model, Element element, POMQName childs, Class<T> clazz) {
        super(model, element);
        this.childname = childs;
        this.clazz = clazz;
    }
    

    @Override
    public Class<T> getListClass() {
        return clazz;
    }

    // attributes

    // child elements

    // child elements
    @Override
    public List<T> getListChildren() {
        return getChildren(clazz);
    }

    @Override
    public void addListChild(T child) {
        appendChild(childname.getQName().getLocalPart(), child);
    }

    @Override
    public void removeListChild(T child) {
        removeChild(childname.getQName().getLocalPart(), child);
    }



    // child elements
    @Override
    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
    }

}
