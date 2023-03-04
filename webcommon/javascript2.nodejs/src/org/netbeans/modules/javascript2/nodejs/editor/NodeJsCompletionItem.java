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

package org.netbeans.modules.javascript2.nodejs.editor;

import java.awt.Color;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.web.common.ui.api.FileReferenceCompletion;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
public class NodeJsCompletionItem implements CompletionProposal {
    
    
    private final int anchorOffset;
    private final NodeJsElement element;

    public NodeJsCompletionItem(NodeJsElement element, int anchorOffset) {
        this.anchorOffset = anchorOffset;
        this.element = element;
    }

    @Override
    public int getAnchorOffset() {
        return anchorOffset;
    }

    @Override
    public ElementHandle getElement() {
        return element;
    }

    @Override
    public String getName() {
        return element.getName();
    }

    @Override
    public String getInsertPrefix() {
        return element.getName();
    }

    @Override
    public String getSortText() {
        return getName();
    }

    @Override
    public String getLhsHtml(HtmlFormatter formatter) {
        formatter.reset();
        formatter.appendText(getName());
        if (element.getKind() == ElementKind.METHOD) {
            formatter.appendText("()"); //NOI18N
        }
        return formatter.getText();
    }

    @NbBundle.Messages("NodeJsCompletionItem.lbl.nodejs.name=NodeJS") //NOI18N
    @Override
    public String getRhsHtml(HtmlFormatter formatter) {
        return Bundle.NodeJsCompletionItem_lbl_nodejs_name();
    }

    @Override
    public ElementKind getKind() {
        return element.getKind();
    }

    @Override
    public ImageIcon getIcon() {
        return null;
    }

    @Override
    public Set<Modifier> getModifiers() {
        return element.getModifiers();
    }

    @Override
    public boolean isSmart() {
        return false;
    }

    @Override
    public int getSortPrioOverride() {
        return 22;
    }

    @Override
    public String getCustomInsertTemplate() {
        if (element.getTemplate() != null) {
            return element.getName() + element.getTemplate();
        }
        return null;
    }
    
    public static class NodeJsModuleCompletionItem extends NodeJsCompletionItem {
        
        public NodeJsModuleCompletionItem(NodeJsElement element, int anchorOffset) {
            super(element, anchorOffset);
        }

        @Override
        public ImageIcon getIcon() {
            return NodeJsUtils.getNodeJsIcon();
        }
        
    }
    
    
    public static class FilenameSupport extends FileReferenceCompletion<NodeJsCompletionItem> {

        @Override
        public NodeJsCompletionItem createFileItem(FileObject file, int anchor) {
            NodeJsElement element = new NodeJsElement.NodeJsFileElement(file);
            return new NodeJsCompletionItem(element, anchor);
        }

        @Override
        public NodeJsCompletionItem createGoUpItem(int anchor, Color color, ImageIcon icon) {
            NodeJsElement element = new NodeJsElement(null, "../", null, ElementKind.FILE); //NOI18N
            return new NodeJsCompletionItem(element, anchor);
        }
    }
}
