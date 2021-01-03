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
package org.netbeans.modules.python.source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.ImageIcon;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.python.source.elements.AstElement;
import org.netbeans.modules.python.source.scopes.SymbolTable;
import org.openide.util.ImageUtilities;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.FunctionDef;

public final class PythonStructureItem extends AstElement implements StructureItem {
    private List<PythonStructureItem> children;
    private PythonStructureItem parent;

    public PythonStructureItem(SymbolTable scopes, ClassDef def) {
        this(scopes, def, def.getInternalName(), ElementKind.CLASS);
    }

    public PythonStructureItem(SymbolTable scopes, FunctionDef def) {
        this(scopes, def, def.getInternalName(), ElementKind.METHOD);
        if ("__init__".equals(name)) { // NOI18N
            kind = ElementKind.CONSTRUCTOR;
        }
    }
    
    public PythonStructureItem(SymbolTable scopes, PythonTree node, String name, ElementKind kind) {
        super(scopes, node, name, kind);
        this.node = node;
        this.name = name;
        this.kind = kind;
    }

    void add(PythonStructureItem child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
        child.parent = this;
    }

    @Override
    public String getSortText() {
        return name;
    }

    @Override
    public String getHtml(HtmlFormatter formatter) {
        formatter.appendText(name);
        if (kind == ElementKind.METHOD || kind == ElementKind.CONSTRUCTOR) {
            FunctionDef def = (FunctionDef)node;
            List<String> params = PythonAstUtils.getParameters(def);
            if (params.size() > 0) {
                boolean isFirst = true;
                formatter.appendHtml("(");
                formatter.parameters(true);
                for (String param : params) {
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        formatter.appendText(",");
                    }
                    formatter.appendText(param);
                }
                formatter.parameters(false);
                formatter.appendHtml(")");
            }
        }
        return formatter.getText();
    }

    @Override
    public ElementHandle getElementHandle() {
        return null;
    }

    @Override
    public boolean isLeaf() {
        return children == null;
    }

    @Override
    public List<? extends StructureItem> getNestedItems() {
        return children == null ? Collections.<StructureItem>emptyList() : children;
    }

    @Override
    public long getPosition() {
        return node.getCharStartIndex();
    }

    @Override
    public long getEndPosition() {
        return node.getCharStopIndex();
    }

    @Override
    public ImageIcon getCustomIcon() {
        if (kind == ElementKind.CLASS && getModifiers().contains(Modifier.PRIVATE)) {
            // GSF doesn't automatically handle icons on private classes, so I have to
            // work around that here
            return ImageUtilities.loadImageIcon("org/netbeans/modules/python/editor/resources/private-class.png", false); //NOI18N
        }

        return null;
    }

    @Override
    public Object getSignature() {
        if ((kind == ElementKind.METHOD || kind == ElementKind.CONSTRUCTOR) && parent != null &&
                parent.kind == ElementKind.CLASS) {
            return parent.name + "." + name;
        }
        return super.getSignature();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PythonStructureItem other = (PythonStructureItem)obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if (this.kind != other.kind) {
            return false;
        }
        if (this.getModifiers() != other.getModifiers() && (this.modifiers == null || !this.modifiers.equals(other.modifiers))) {
            return false;
        }

        if ((kind == ElementKind.METHOD || kind == ElementKind.CONSTRUCTOR) && node != null && other.node != null) {
            FunctionDef def = (FunctionDef)node;
            List<String> params = PythonAstUtils.getParameters(def);
            List<String> otherParams = PythonAstUtils.getParameters((FunctionDef)other.node);
            if (!params.equals((otherParams))) {
                return false;
            }
        }

//        if (this.getNestedItems().size() != other.getNestedItems().size()) {
//            return false;
//        }
//
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}
