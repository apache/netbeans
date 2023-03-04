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
package org.netbeans.modules.web.core.syntax.parser;

import java.util.Collections;
import java.util.List;

public abstract class JspSyntaxElement {

    public enum Kind {

        TEXT, COMMENT,/* EL,*/ SCRIPTING, ERROR, ENDTAG, OPENTAG, DIRECTIVE;
    }
    private CharSequence source;
    private int from, to;

    public JspSyntaxElement(CharSequence source, int from, int to) {
        this.source = source;
        this.from = from;
        this.to = to;
    }

    public int from() {
        return from;
    }

    public int to() {
        return to;
    }

    public CharSequence text() {
        return source.subSequence(from, to);
    }

    @Override
    public String toString() {
        return "JspSyntaxElement[" + from() + " - " + to() + ", " + kind() + "]"; //NOI18N
    }
    
    public abstract Kind kind();

    public static class Comment extends JspSyntaxElement {

        public Comment(CharSequence source, int from, int to) {
            super(source, from, to);
        }

        @Override
        public Kind kind() {
            return Kind.COMMENT;
        }
    }


//    public static class ExpressionLanguage extends JspSyntaxElement {
//
//        public ExpressionLanguage(CharSequence source, int from, int to) {
//            super(source, from, to);
//        }
//
//        @Override
//        public Kind kind() {
//            return Kind.EL;
//        }
//    }

    public static class SharedTextElement extends JspSyntaxElement {

        private static final String TO_STRING = "<n/a>"; //NOI18N

        public SharedTextElement() {
            super(null, 0, 0);
        }

        @Override
        public CharSequence text() {
            return TO_STRING;
        }

        @Override
        public Kind kind() {
            return Kind.TEXT;
        }

        @Override
        public int from() {
            assert false;
            return super.from();
        }

        @Override
        public int to() {
            assert false;
            return super.to();
        }

        @Override
        public String toString() {
            return "JspSyntaxElement[SHARED_TEXT_ELEMENT]"; //NOI18N
        }


    }

    public static class Scripting extends JspSyntaxElement {

        public Scripting(CharSequence source, int from, int to) {
            super(source, from, to);
        }

        @Override
        public Kind kind() {
            return Kind.SCRIPTING;
        }
    }

    public static class Error extends JspSyntaxElement {

        public Error(CharSequence source, int from, int to) {
            super(source, from, to);
        }

        @Override
        public Kind kind() {
            return Kind.ERROR;
        }
    }

    public abstract static class Named extends JspSyntaxElement {

        protected String name;

        public Named(CharSequence source, int from, int to, String name) {
            super(source, from, to);
            this.name = name;
        }

        public String name() {
            return name;
        }
    }

    public static class EndTag extends Named {

        public EndTag(CharSequence source, int from, int to, String name) {
            super(source, from, to, name);
        }

        @Override
        public Kind kind() {
            return Kind.ENDTAG;
        }
    }

    public abstract static class AttributedTagLikeElement extends Named {

        private List<Attribute> attribs;

        public AttributedTagLikeElement(CharSequence source, int from, int length, String name, List attribs) {
            super(source, from, length, name);
            this.attribs = attribs;
        }

        public List<Attribute> attributes() {
            return attribs == null ? Collections.EMPTY_LIST : attribs;
        }

        public Attribute getAttribute(String name) {
            return getAttribute(name, true);
        }

        public Attribute getAttribute(String name, boolean ignoreCase) {
            for (Attribute ta : attributes()) {
                if (ta.getName().equals(name)) {
                    return ta;
                }
            }
            return null;
        }
    }

    public abstract static class Tag extends AttributedTagLikeElement {

        private boolean empty;

        public Tag(CharSequence source, int from, int length, String name, List attribs, boolean isEmpty) {
            super(source, from, length, name, attribs);
            this.empty = isEmpty;
        }
        
        public boolean isEmpty() {
            return empty;
        }

    }

    public static class OpenTag extends Tag {

        public OpenTag(CharSequence source, int from, int length, String name, List<Attribute> attribs, boolean isEmpty) {
            super(source, from, length, name, attribs, isEmpty);
        }

        @Override
        public Kind kind() {
            return Kind.OPENTAG;
        }
    }

    public static class Directive extends AttributedTagLikeElement {

        public Directive(CharSequence source, int from, int length, String name, List<Attribute> attribs) {
            super(source, from, length, name, attribs);
        }

        @Override
        public Kind kind() {
            return Kind.DIRECTIVE;
        }
    }

    public static class Attribute {

        private String name, value;
        private int nameOffset, valueOffset, valueLength;

        Attribute(String name, String value, int nameOffset, int valueOffset, int valueLength) {
            this.name = name;
            this.value = value;
            this.nameOffset = nameOffset;
            this.valueOffset = valueOffset;
            this.valueLength = valueLength;
        }

        public String getName() {
            return name;
        }

        void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public int getValueLength() {
            return valueLength;
        }

        void setValue(String value) {
            this.value = value;
        }

        public int getNameOffset() {
            return nameOffset;
        }

        void setNameOffset(int ofs) {
            this.nameOffset = ofs;
        }

        public int getValueOffset() {
            return valueOffset;
        }

        void setValueOffset(int ofs) {
            this.valueOffset = ofs;
        }

        @Override
        public String toString() {
            return "TagAttribute[name=" + getName() + "; value=" + getValue() + "; nameOffset=" + getNameOffset() + "; valueOffset=" + getValueOffset() + "]"; //NOI18N
        }

        //backward compatibility
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Attribute)) {
                return false;
            } else {
                return getName().equals(((Attribute) o).getName());
            }
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
            return hash;
        }
    }
}
