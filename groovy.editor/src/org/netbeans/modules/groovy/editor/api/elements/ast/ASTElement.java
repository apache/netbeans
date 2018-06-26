/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.groovy.editor.api.elements.ast;

import groovyjarjarasm.asm.Opcodes;
import java.util.*;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.groovy.editor.api.ASTUtils;
import org.netbeans.modules.groovy.editor.api.elements.GroovyElement;

/**
 *
 * @author Martin Adamek
 */
public abstract class ASTElement extends GroovyElement {

    protected final ASTNode node;
    protected final List<ASTElement> children;
    protected Set<Modifier> modifiers;


    public ASTElement(ASTNode node) {
        this(node, null);
    }

    public ASTElement(ASTNode node, String in) {
        this(node, in, null);
    }

    public ASTElement(ASTNode node, String in, String name) {
        super(in, name);
        this.node = node;
        this.children = new ArrayList<>();
    }

    public ASTNode getNode() {
        return node;
    }

    public List<ASTElement> getChildren() {
        return children;
    }

    public void addChild(ASTElement child) {
        children.add(child);
    }

    @Override
    public Set<Modifier> getModifiers() {
        if (modifiers == null) {
            int flags = -1;
            if (node instanceof FieldNode) {
                flags = ((FieldNode) node).getModifiers();
            } else if (node instanceof MethodNode) {
                flags = ((MethodNode) node).getModifiers();
            }
            if (flags != -1) {
                Set<Modifier> result = EnumSet.noneOf(Modifier.class);
                if ((flags & Opcodes.ACC_PUBLIC) != 0) {
                    result.add(Modifier.PUBLIC);
                }
                if ((flags & Opcodes.ACC_PROTECTED) != 0) {
                    result.add(Modifier.PROTECTED);
                }
                if ((flags & Opcodes.ACC_PRIVATE) != 0) {
                    result.add(Modifier.PRIVATE);
                }
                if ((flags & Opcodes.ACC_STATIC) != 0) {
                    result.add(Modifier.STATIC);
                }
                modifiers = result;
            } else {
                modifiers = Collections.<Modifier>emptySet();
            }
        }

        return modifiers;
    }

    @Override
    public boolean signatureEquals(final ElementHandle handle) {
        if (handle instanceof ASTElement) {
            return this.equals(handle);
        }
        return false;
    }

    @Override
    public OffsetRange getOffsetRange(ParserResult result) {
        BaseDocument doc = (BaseDocument) result.getSnapshot().getSource().getDocument(false);
        int lineNumber = node.getLineNumber();
        int columnNumber = node.getColumnNumber();
        int start = ASTUtils.getOffset(doc, lineNumber, columnNumber);

        return new OffsetRange(start, start);
    }

    public static ASTElement create(ASTNode node) {
        if (node instanceof MethodNode) {
            return new ASTMethod(node);
        }
        return null;
    }

    @Override
    public String toString() {
        return getKind() + "<" + getName() + ">";
    }
}
