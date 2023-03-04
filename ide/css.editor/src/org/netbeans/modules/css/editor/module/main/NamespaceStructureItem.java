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
package org.netbeans.modules.css.editor.module.main;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.css.editor.csl.CssNodeElement;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author mfukala@netbeans.org
 */
public class NamespaceStructureItem implements StructureItem {

    private CssNodeElement handle;
    private CharSequence prefix, resource;
    
    public NamespaceStructureItem(FileObject file, Node namespaceNode) {
        this.handle = CssNodeElement.createElement(file, namespaceNode);
        
        Node prefixNode = NodeUtil.query(namespaceNode, NodeType.namespacePrefixName.name()); //NOI18N
        this.prefix = prefixNode != null ? prefixNode.image() : NbBundle.getMessage(NamespaceStructureItem.class, "default_namespace");
        
        Node resourceNode = NodeUtil.query(namespaceNode, "resourceIdentifier"); //NOI18N
        this.resource = resourceNode != null ? resourceNode.image() : "";
    }

    @Override
    public String getName() {
        return new StringBuilder().append(prefix).append(':').append(resource).toString();
    }

    @Override
    public String getSortText() {
        return getName();
    }

    @Override
    public String getHtml(HtmlFormatter formatter) {
        return new StringBuilder()
                .append("<b>") //NOI18N
                .append(prefix)
                .append("</b>") //NOI18N
                .append(' ')
                .append(resource).toString();
    }

    @Override
    public ElementHandle getElementHandle() {
        return handle;
    }

    @Override
    public ElementKind getKind() {
        return NamespacesModule.NAMESPACE_ELEMENT_KIND; 
    }

    @Override
    public Set<Modifier> getModifiers() {
        return Collections.emptySet();
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public List<? extends StructureItem> getNestedItems() {
        return Collections.emptyList();
    }

    @Override
    public long getPosition() {
        return handle.from();
    }

    @Override
    public long getEndPosition() {
        return handle.to();
    }

    @Override
    public ImageIcon getCustomIcon() {
        return null;
    }

}
