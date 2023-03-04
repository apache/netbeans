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
package org.netbeans.modules.php.editor.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.openide.filesystems.FileObject;
import org.openide.util.Union2;

/**
 * @author Radek Matous
 */
abstract class ScopeImpl extends ModelElementImpl implements Scope {

    private OffsetRange blockRange = null;
    //@GuardedBy("this")
    private List<ModelElementImpl> elements = null;

    //new contructors
    ScopeImpl(Scope inScope, ASTNodeInfo info, PhpModifiers modifiers, Block block, boolean isDeprecated) {
        super(inScope, info, modifiers, isDeprecated);
        if (block != null) {
            this.blockRange = new OffsetRange(block.getStartOffset(), block.getEndOffset());
        }
    }

    ScopeImpl(Scope inScope, PhpElement element, PhpElementKind kind) {
        super(inScope, element, kind);
    }
    //old contructors
    ScopeImpl(Scope inScope, String name, Union2<String/*url*/, FileObject> file,
            OffsetRange offsetRange, PhpElementKind kind, boolean isDeprecated) {
        super(inScope, name, file, offsetRange, kind, isDeprecated);
        assert isScopeKind(kind) : kind.toString();
    }

    ScopeImpl(Scope inScope, String name, Union2<String/*url*/, FileObject> file,
            OffsetRange offsetRange, PhpElementKind kind,
            PhpModifiers modifier, boolean isDeprecated) {
        super(inScope, name, file, offsetRange, kind, modifier, isDeprecated);
        assert isScopeKind(kind) : kind.toString();
    }

    private static boolean isScopeKind(PhpElementKind kind) {
        boolean result;
        switch (kind) {
            case PROGRAM:
            case NAMESPACE_DECLARATION:
            case INDEX:
            case CLASS:
            case FUNCTION:
            case IFACE:
            case METHOD:
            case VARIABLE:
            case FIELD:
            case USE_STATEMENT:
            case GROUP_USE_STATEMENT:
            case TRAIT:
            case ENUM:
                result = true;
                break;
            default:
                result = false;
        }
        return result;
    }

    @Override
    public synchronized List<? extends ModelElementImpl> getElements() {
        return elements == null ? Collections.EMPTY_LIST : new ArrayList<>(elements);
    }

    synchronized void addElement(ModelElementImpl element) {
        if (elements == null) {
            elements = new ArrayList<>();
        }
        elements.add(element);
    }

    @SuppressWarnings("unchecked")
    static <T extends ModelElement> Collection<? extends T> filter(final Collection<? extends ModelElement> original,
            final ElementFilter<T> filter) {
        Set<T> retval = new HashSet<>();
        for (ModelElement baseElement : original) {
            boolean accepted = filter.isAccepted(baseElement);
            if (accepted) {
                retval.add((T) baseElement);
            }
        }
        return retval;
    }

    interface ElementFilter<T extends ModelElement> {
        boolean isAccepted(ModelElement element);
    }

    void setBlockRange(ASTNode program) {
        this.blockRange = new OffsetRange(program.getStartOffset(), program.getEndOffset());
    }

    @Override
    public OffsetRange getBlockRange() {
        //assert blockRange != null;
        return blockRange;
    }
}
