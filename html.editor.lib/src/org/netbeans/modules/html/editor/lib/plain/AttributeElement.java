/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
