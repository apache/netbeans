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
package org.netbeans.modules.javafx2.editor.parser;

import org.netbeans.modules.javafx2.editor.completion.model.FxInstance;
import org.netbeans.modules.javafx2.editor.completion.model.FxInstanceCopy;
import org.netbeans.modules.javafx2.editor.completion.model.FxTreeUtilities;
import org.netbeans.modules.javafx2.editor.completion.model.FxNewInstance;
import org.netbeans.modules.javafx2.editor.completion.model.FxNode;
import org.netbeans.modules.javafx2.editor.completion.model.FxNodeVisitor;
import org.netbeans.modules.javafx2.editor.completion.model.FxReference;
import org.netbeans.modules.javafx2.editor.completion.model.FxScriptFragment;
import org.netbeans.modules.javafx2.editor.completion.model.ImportDecl;
import org.netbeans.modules.javafx2.editor.completion.model.LanguageDecl;
import org.netbeans.modules.javafx2.editor.completion.model.MapProperty;
import org.netbeans.modules.javafx2.editor.completion.model.PropertySetter;
import org.netbeans.modules.javafx2.editor.completion.model.StaticProperty;
import org.netbeans.modules.javafx2.editor.completion.model.TextPositions;

/**
 *
 * @author sdedic
 */
public class PrintVisitor extends FxNodeVisitor.ModelTraversal {
    public StringBuilder out = new StringBuilder();
    private int indent = 0;
    private FxTreeUtilities trees;

    public PrintVisitor(FxTreeUtilities nodes) {
        this.trees = nodes;
    }

    @Override
    protected void scan(FxNode node) {
        indent += 2;
        if (node != null) {
            TextPositions pos = trees.positions(node);
            String s = String.format("[%d - %d]  ", pos.getStart(), pos.getEnd());
            out.append(s).append(PADDING, 0, 16 - s.length());
        }
        super.scan(node);
        indent -= 2;
    }

    private StringBuilder i() {
        for (int i = 0; i < indent; i++) {
            out.append(" ");
        }
        return out;
    }

    @Override
    public void visitCopy(FxInstanceCopy decl) {
        i().append(String.format("copy: source=%s, id=%s\n", decl.getBlueprintId(), decl.getId()));
        super.visitCopy(decl); 
    }

    @Override
    public void visitReference(FxReference decl) {
        i().append(String.format("reference: source=%s\n", decl.getTargetId()));
        super.visitReference(decl); 
    }

    @Override
    public void visitInstance(FxNewInstance decl) {
        i().append(String.format("instance: id=%s, className=%s\n", decl.getId(), decl.getTypeName()));
        super.visitInstance(decl);
    }

    @Override
    public void visitPropertySetter(PropertySetter p) {
        i().append(String.format("setter: name=%s, content=%s\n", p.getPropertyName(), p.getContent()));
        super.visitPropertySetter(p);
    }

    @Override
    public void visitLanguage(LanguageDecl decl) {
        i().append(String.format("language: %s\n", decl.getLanguage()));
        super.visitLanguage(decl);
    }

    @Override
    public void visitImport(ImportDecl decl) {
        i().append(String.format("import: name=%s, wildcard=%b\n", decl.getImportedName(), decl.isWildcard()));
        super.visitImport(decl);
    }

    @Override
    public void visitMapProperty(MapProperty p) {
        i().append(String.format("map: name=%s, content=%s\n", p.getPropertyName(), p.getValueMap()));
        super.visitMapProperty(p);
    }

    @Override
    public void visitStaticProperty(StaticProperty p) {
        i().append(String.format("attached: name=%s, source=%s, content=%s\n", p.getPropertyName(), p.getSourceClassName(), p.getContent()));
        super.visitStaticProperty(p);
    }
    
    public void visitScript(FxScriptFragment f) {
        i().append(String.format("script: src=%s, len=%d", f.getSourcePath(), (f.hasContent() ? f.getContent().length() : -1)));
    }
    
    private static final String PADDING = "                ";
}
