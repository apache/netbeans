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
package org.netbeans.modules.rust.grammar.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.rust.grammar.ast.RustAST;
import org.netbeans.modules.rust.grammar.ast.RustASTNode;
import org.openide.util.ImageUtilities;

/**
 * A StructureItem representing a function, enu, struct, or other important
 * structure in a Rust program.
 */
public final class RustStructureItem implements StructureItem {

    private static final String getIconBase(RustASTNode node) {
        switch (node.getKind()) {
            case CRATE:
            case ENUM:
                return "org/netbeans/modules/rust/grammar/structure/resources/structure-enum.png"; // NOI18N
            case FUNCTION:
                return "org/netbeans/modules/rust/grammar/structure/resources/structure-function.png"; // NOI18N
            case STRUCT:
                return "org/netbeans/modules/rust/grammar/structure/resources/structure-struct.png"; // NOI18N
            case IMPL:
            case TRAIT:
                return "org/netbeans/modules/rust/grammar/structure/resources/structure-impl.png"; // NOI18N
            case MACRO:
                return "org/netbeans/modules/rust/grammar/structure/resources/structure-macro.png"; // NOI18N
            case MODULE:
                return "org/netbeans/modules/rust/grammar/structure/resources/structure-module.png"; // NOI18N
            default:
                return "org/netbeans/modules/rust/grammar/structure/resources/structure-struct.png"; // NOI18N
        }
    }

    final RustAST ast;
    final RustASTNode node;
    private List<RustStructureItem> children;

    public RustStructureItem(RustAST ast, RustASTNode node) {
        this.ast = ast;
        this.node = node;
        this.children = null;
    }

    @Override
    public ImageIcon getCustomIcon() {
        return new ImageIcon(ImageUtilities.loadImage(getIconBase(node)));
    }

    @Override
    public long getEndPosition() {
        return node.getStop();
    }

    @Override
    public long getPosition() {
        return node.getStart();
    }

    List<RustStructureItem> getChildren() {
        // TODO: We're not thread safe. Does this matter?
        if (children == null) {
            this.children = new ArrayList<>();
        }
        return children;
    }

    @Override
    public List<? extends StructureItem> getNestedItems() {
        return getChildren();
    }

    @Override
    public boolean isLeaf() {
        return getChildren().isEmpty();
    }

    @Override
    public Set<Modifier> getModifiers() {
        return Collections.emptySet();
    }

    @Override
    public ElementKind getKind() {
        switch (node.getKind()) {
            case CRATE:
                return ElementKind.FILE;
            case ENUM:
            case IMPL:
            case STRUCT:
            case TRAIT:
                return ElementKind.CLASS;
            case FUNCTION:
                return ElementKind.METHOD;
            default:
                return ElementKind.CLASS;
        }
    }

    @Override
    public ElementHandle getElementHandle() {
        // TODO: Review we want to keep a reference to this in RustElementHandle
        return new RustElementHandle(this);
    }

    @Override
    public String getHtml(HtmlFormatter formatter) {
        formatter.appendText(getName());
        return formatter.getText();
    }

    @Override
    public String getSortText() {
        return node.getName();
    }

    @Override
    public String getName() {
        return node.getName();
    }

    @Override
    public String toString() {
        return String.format("[ITEM: %s]", node.toString());
    }

}
