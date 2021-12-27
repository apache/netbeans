/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import org.netbeans.modules.html.editor.lib.api.ProblemDescription;
import org.netbeans.modules.html.editor.lib.api.elements.*;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.util.CharSequences;

/**
 *
 * @author marekfukala
 */
public class XmlSTElements {
    
    static abstract class ElementBase implements Element {

        private CharSequence source;
        private int from, to;
        private Node parent;

        public ElementBase(CharSequence source, int from, int to) {
            this.source = source;
            this.from = from;
            this.to = to;
        }

        @Override
        public int from() {
            return from;
        }

        @Override
        public int to() {
            return to;
        }

        @Override
        public CharSequence image() {
            return source.subSequence(from, to);
        }

        @Override
        public CharSequence id() {
            return null;
        }

        @Override
        public Collection<ProblemDescription> problems() {
            return Collections.emptyList();
        }

        void setParent(Node parent) {
            this.parent = parent;
        }

        @Override
        public Node parent() {
            return parent;
        }

        @Override
        public String toString() {
            return new StringBuilder()
                    .append(type().name())
                    .append("; ")
                    .append(from())
                    .append("-")
                    .append(to()).toString();
        }
    }
    
    static abstract class NamedElement extends ElementBase  implements Named {

        private CharSequence name; //we can possibly use the original source code + pointer
        
        public NamedElement(CharSequence name, CharSequence source, int from, int to) {
            super(source, from, to);
            this.name = name;
        }

        @Override
        public CharSequence name() {
            return name;
        }

        @Override
        public CharSequence namespacePrefix() {
            int colonIndex = CharSequences.indexOf(name(), ":");
            return colonIndex == -1 ? null : name().subSequence(0, colonIndex);
        }

        @Override
        public CharSequence unqualifiedName() {
            int colonIndex = CharSequences.indexOf(name(), ":");
            return colonIndex == -1 ? name() : name().subSequence(colonIndex + 1, name().length());
        }

        @Override
        public CharSequence id() {
            return name();
        }

        @Override
        public String toString() {
            return new StringBuilder().append(name()).append("(").append(type().name()).append(")").append("; ").append(from()).append("-").append(to()).toString();
        }
    }

    static class EmptyOT extends NamedElement implements OpenTag {

        private Collection<Attribute> attrs;

        public EmptyOT(Collection<Attribute> attrs, CharSequence name, CharSequence source, int from, int to) {
            super(name, source, from, to);
            this.attrs = attrs;
        }

        @Override
        public Collection<Attribute> attributes() {
            return attrs;
        }

        @Override
        public Collection<Attribute> attributes(AttributeFilter filter) {
            Collection<Attribute> filtered = new ArrayList<>(1);
            for (Attribute a : attributes()) {
                if (filter.accepts(a)) {
                    filtered.add(a);
                }
            }
            return filtered;
        }

        @Override
        public Attribute getAttribute(String name) {
            //typically very low number of attrs so the linear search doesn't hurt
            for (Attribute a : attributes()) {
                if (LexerUtils.equals(name, a.name(), true, false)) {
                    return a;
                }
            }
            return null;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public CloseTag matchingCloseTag() {
            return null;
        }

        @Override
        public int semanticEnd() {
            return to();
        }

        @Override
        public Collection<Element> children() {
            return Collections.emptyList();
        }

        @Override
        public Collection<Element> children(ElementType type) {
            return Collections.emptyList();
        }

        @Override
        public ElementType type() {
            return ElementType.OPEN_TAG;
        }

        @Override
        public Collection<Element> children(ElementFilter filter) {
            return Collections.emptyList();
        }

        @Override
        public <T extends Element> Collection<T> children(Class<T> type) {
            return Collections.emptyList();
        }
    }

    static class OT extends EmptyOT {

        private Collection<Element> children;
        private CloseTag matchingEndTag;
        private int logicalEndOffset;

        public OT(Collection<Attribute> attrs, CharSequence name, CharSequence source, int from, int to) {
            super(attrs, name, source, from, to);
            logicalEndOffset = to;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        void setMatchingEndTag(CloseTag endTag) {
            this.matchingEndTag = endTag;
        }

        @Override
        public CloseTag matchingCloseTag() {
            return matchingEndTag;
        }

        void addChild(Element child) {
            if (children == null) {
                children = new ArrayList<>(1);
            }
            children.add(child);
            ((ElementBase)child).setParent(this);
        }

        @Override
        public Collection<Element> children() {
            return children == null ? Collections.<Element>emptyList() : children;
        }

        @Override
        public Collection<Element> children(ElementType type) {
            Collection<Element> filtered = new ArrayList<>();
            for (Element e : children()) {
                if (e.type() == type) {
                    filtered.add(e);
                }
            }
            return filtered;
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

        void setLogicalEndOffset(int to) {
            this.logicalEndOffset = to;
        }

        @Override
        public int semanticEnd() {
            return logicalEndOffset;
        }
    }

    static class ET extends NamedElement implements CloseTag {

        private OpenTag matchingOpenTag;

        public ET(CharSequence name, CharSequence source, int from, int to) {
            super(name, source, from, to);
        }

        @Override
        public ElementType type() {
            return ElementType.CLOSE_TAG;
        }

        @Override
        public OpenTag matchingOpenTag() {
            return matchingOpenTag;
        }

        void setMatchingOpenTag(OpenTag openTag) {
            this.matchingOpenTag = openTag;
        }
    }
    
    public static class Text extends ElementBase {

        public Text(CharSequence source, int from, int to) {
            super(source, from, to);
        }

        @Override
        public ElementType type() {
            return ElementType.TEXT;
        }

        @Override
        public String toString() {
            return new StringBuilder()
                    .append(super.toString())
                    .append(" \"")
                    .append(image())
                    .append("\"").toString();
        }
        
        

    }

    public static class Root extends OT implements FeaturedNode {

        private String namespace;

        public Root(String namespace, CharSequence source) {
            super(Collections.<Attribute>emptyList(), "root", source, 0, source.length());
            this.namespace = namespace;
        }

        @Override
        public ElementType type() {
            return ElementType.ROOT;
        }

        @Override
        public Object getProperty(String propertyName) {
            if (propertyName.equalsIgnoreCase("namespace")) { //NOI18N
                return namespace;
            }

            return null;
        }
    }
}
