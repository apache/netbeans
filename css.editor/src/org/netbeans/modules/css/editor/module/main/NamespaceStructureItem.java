/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
