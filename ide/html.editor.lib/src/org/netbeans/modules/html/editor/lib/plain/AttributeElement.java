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
package org.netbeans.modules.html.editor.lib.plain;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.html.editor.lib.api.ProblemDescription;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.openide.util.CharSequences;

/**
 *
 * @author marekfukala
 */
public class AttributeElement implements Attribute {

    private CharSequence source;
    private int nameOffset;
    private short valueOffset2nameOffsetDiff;
    private short nameLen;
    private int valueLen;

    public AttributeElement(CharSequence source, int nameOffset, short nameLen) {
        this.source = source;

        this.nameOffset = nameOffset;
        this.valueOffset2nameOffsetDiff = -1;

        this.nameLen = nameLen;
        this.valueLen = -1;
    }

    public AttributeElement(CharSequence source, int nameOffset, int valueOffset, short nameLen, int valueLen) {
        this.source = source;

        this.nameOffset = nameOffset;
        this.valueOffset2nameOffsetDiff = (short) (valueOffset - nameOffset);

        this.nameLen = nameLen;
        this.valueLen = valueLen;
    }

    @Override
    public int nameOffset() {
        return nameOffset;
    }

    @Override
    public CharSequence name() {
        return source.subSequence(nameOffset, nameOffset + nameLen);
    }

    @Override
    public int valueOffset() {
        return valueOffset2nameOffsetDiff == -1 ? -1 : nameOffset + valueOffset2nameOffsetDiff;
    }

    @Override
    public CharSequence value() {
        return valueLen == -1 ? null : source.subSequence(valueOffset(), valueOffset() + valueLen);
    }

    @Override
    public boolean isValueQuoted() {
        if (value() == null) {
            return false;
        }
        if (valueLen < 2) {
            return false;
        } else {
            CharSequence value = value();
            return ((value.charAt(0) == '\'' || value.charAt(0) == '"')
                    && (value.charAt(value.length() - 1) == '\'' || value.charAt(value.length() - 1) == '"'));
        }
    }

    @Override
    public CharSequence unquotedValue() {
        if (value() == null) {
            return null;
        }
        return isValueQuoted() ? value().subSequence(1, value().length() - 1) : value();
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
    public int from() {
        return nameOffset();
    }

    @Override
    public int to() {
        return value() != null
                ? valueOffset() + valueLen
                : nameOffset() + nameLen;
    }

    @Override
    public ElementType type() {
        return ElementType.ATTRIBUTE;
    }

    @Override
    public CharSequence image() {
        return source.subSequence(from(), to());
    }

    @Override
    public CharSequence id() {
        return type().name();
    }

    @Override
    public Collection<ProblemDescription> problems() {
        return Collections.emptyList();
    }

    @Override
    public Node parent() {
        return null;
    }

    public static class AttributeElementWithJoinedValue extends AttributeElement {

        public String value;

        public AttributeElementWithJoinedValue(CharSequence source, int nameOffset, short nameLen, int valueOffset, String value) {
            super(source, nameOffset, valueOffset, nameLen, (short) value.length());
            this.value = value;
        }

        @Override
        public CharSequence value() {
            return value;
        }
    }
}
