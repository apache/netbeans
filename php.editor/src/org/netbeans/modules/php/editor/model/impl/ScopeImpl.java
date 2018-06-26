/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
