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
import org.netbeans.modules.html.editor.lib.api.elements.*;

/**
 *
 * @author marekfukala
 */
public class AttributelessOpenTagElement extends AbstractTagElement implements OpenTag {

    private boolean empty;

    public AttributelessOpenTagElement(CharSequence document, int from, short length,
            byte nameLen, boolean isEmpty) {
        super(document, from, length, nameLen);
        this.empty = isEmpty;
    }

    @Override
    public boolean isEmpty() {
        return empty;
    }

    @Override
    public ElementType type() {
        return ElementType.OPEN_TAG;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder(super.toString());
        ret.append(" - {");   // NOI18N

        ret.append("}");      //NOI18N
        if (isEmpty()) {
            ret.append(" (EMPTY TAG)"); //NOI18N
        }
        return ret.toString();
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
        return null;
    }

    @Override
    public Collection<Element> children(ElementType type) {
        return null;
    }

    @Override
    public Collection<Element> children(ElementFilter filter) {
        return null;
    }

    @Override
    public <T extends Element> Collection<T> children(Class<T> type) {
        return null;
    }

    @Override
    public Collection<ProblemDescription> problems() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Attribute> attributes() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Attribute> attributes(AttributeFilter filter) {
        return Collections.emptyList();
    }

    @Override
    public Attribute getAttribute(String name) {
        return null;
    }

    @Override
    protected int fromToNamePositionDiff() {
        return 1;
    }
}
