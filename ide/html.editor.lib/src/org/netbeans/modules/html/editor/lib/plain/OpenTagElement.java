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

import java.util.*;
import org.netbeans.modules.html.editor.lib.api.elements.*;
import org.netbeans.modules.web.common.api.LexerUtils;

/**
 *
 * @author marekfukala
 */
public class OpenTagElement extends AttributelessOpenTagElement implements OpenTag {

    private List<Attribute> attribs;

    public OpenTagElement(CharSequence document, int from, short length,
            byte nameLen,
            List<Attribute> attribs,
            boolean isEmpty) {
        super(document, from, length, nameLen, isEmpty);
        this.attribs = attribs;
    }

    @Override
    public Collection<Attribute> attributes() {
        return attribs == null ? Collections.EMPTY_LIST : attribs;
    }

    @Override
    public Collection<Attribute> attributes(AttributeFilter filter) {
        Collection<Attribute> filtered = new ArrayList<>(attributes().size() / 2);
        for (Attribute attr : attributes()) {
            if (filter.accepts(attr)) {
                filtered.add(attr);
            }
        }
        return filtered;
    }

    @Override
    public Attribute getAttribute(String name) {
        return getAttribute(name, true);
    }

    public Attribute getAttribute(String name, boolean ignoreCase) {
        for (Attribute ta : attributes()) {
            if (LexerUtils.equals(ta.name(), name, ignoreCase, false)) {
                return ta;
            }
        }
        return null;
    }

  
}
