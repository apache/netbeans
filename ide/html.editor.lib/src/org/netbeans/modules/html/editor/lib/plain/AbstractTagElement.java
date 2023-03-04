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

import org.openide.util.CharSequences;

/**
 *
 * @author marekfukala
 */
public abstract class AbstractTagElement extends AbstractElement {

    private byte nameLen;
    
    public AbstractTagElement(CharSequence doc, int offset, short length, byte nameLen) {
        super(doc, offset, length);
        this.nameLen = nameLen;
    }

    protected abstract int fromToNamePositionDiff();

    public CharSequence name() {
        int nameOffset = from() + fromToNamePositionDiff();
        return source().subSequence(nameOffset, nameOffset + nameLen );
    }

    public CharSequence namespacePrefix() {
        int colonIndex = CharSequences.indexOf(name(), ":");
        return colonIndex == -1 ? null : name().subSequence(0, colonIndex);

    }

    public CharSequence unqualifiedName() {
        int colonIndex = CharSequences.indexOf(name(), ":");
        return colonIndex == -1 ? name() : name().subSequence(colonIndex + 1, name().length());
    }
}
