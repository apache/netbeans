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
package org.netbeans.modules.html.editor.lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import org.netbeans.modules.html.editor.lib.api.ProblemDescription;
import org.netbeans.modules.html.editor.lib.api.elements.*;

/**
 *
 * @author marekfukala
 */
public class RootNode implements FeaturedNode {

    private CharSequence source;
    private Collection<Element> children;

    public RootNode(CharSequence source) {
        this(source, new LinkedList<Element>());
    }

    public RootNode(CharSequence source, Collection<Element> children) {
        this.source = source;
        this.children = children;
    }

    @Override
    public Collection<Element> children() {
        return children;
    }

    @Override
    public Collection<Element> children(ElementType type) {
        Collection<Element> filtered = new ArrayList<>();
        for (Element child : children()) {
            if (child.type() == type) {
                filtered.add(child);
            }
        }
        return filtered;
    }

    @Override
    public Node parent() {
        return null;
    }

    @Override
    public int from() {
        return 0;
    }

    @Override
    public int to() {
        return source.length();
    }

    @Override
    public ElementType type() {
        return ElementType.ROOT;
    }

    @Override
    public CharSequence image() {
        return source;
    }

    @Override
    public Collection<ProblemDescription> problems() {
        return Collections.emptyList();
    }

    @Override
    public Object getProperty(String propertyName) {
        return null;
    }

    @Override
    public CharSequence id() {
        return type().name();
    }

    @Override
    public Collection<Element> children(ElementFilter filter) {
        Collection<Element> filtered = new ArrayList<>();
        for (Element e : children()) {
            if (filter.accepts(e)) {
                filtered.add(e);
            }
        }
        return filtered;
    }

    @Override
    public <T extends Element> Collection<T> children(Class<T> type) {
        Collection<T> filtered = new ArrayList<>();
        for (Element child : children()) {
            if (type.isAssignableFrom(child.getClass())) {
                filtered.add(type.cast(child));
            }
        }
        return filtered;
    }
}
