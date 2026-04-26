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
package org.netbeans.modules.php.blade.editor.navigator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.ImageIcon;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.php.blade.editor.ResourceUtilities;
import org.openide.filesystems.FileObject;

/**
 *
 * @author bogdan
 */
public abstract class DirectiveStructureItem extends BladeStructureItem {

    private String identifierArg;

    public DirectiveStructureItem(String name, FileObject source, int startOffset, int stopOffset) {
        super(name, source, startOffset, stopOffset);
    }

    public DirectiveStructureItem(String name, String identifierArg, FileObject source, int startOffset, int stopOffset) {
        super(name, source, startOffset, stopOffset);
        this.identifierArg = identifierArg;
    }

    public String getDirectiveIdentiferArg() {
        return identifierArg;
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.CLASS;
    }

    @Override
    public String getHtml(HtmlFormatter formatter) {
        formatter.appendText(getName());
        if (getDirectiveIdentiferArg() != null) {
            formatter.appendText(" "); //NOI18N
            formatter.appendHtml("<em>"); //NOI18N
            formatter.appendHtml("<font color='5b5b5b'>"); //NOI18N
            formatter.appendText(getDirectiveIdentiferArg());
            formatter.appendHtml("</font>"); //NOI18N
            formatter.appendHtml("</em>"); //NOI18N
        }
        return formatter.getText();
    }

    @Override
    public ImageIcon getCustomIcon() {
        return ResourceUtilities.loadLayoutIcon();
    }
    
    public static class DirectiveInlineStructureItem extends DirectiveStructureItem {

        public DirectiveInlineStructureItem(String name, String identifierArg, FileObject source, int startOffset, int stopOffset) {
            super(name, identifierArg, source, startOffset, stopOffset);
        }

        @Override
        public boolean isLeaf() {
            return true;
        }

        @Override
        public List<? extends StructureItem> getNestedItems() {
            return Collections.emptyList();
        }
    }
    
    public static final class DirectiveBlockStructureItem extends DirectiveStructureItem {

        private int depth;
        public final List<StructureItem> nestedItems = new ArrayList<>();

        public DirectiveBlockStructureItem(String name, FileObject source, int startOffset, int stopOffset) {
            super(name, source, startOffset, stopOffset);
        }

        public DirectiveBlockStructureItem(String name, String identifierArg, FileObject source, int startOffset, int stopOffset) {
            super(name, identifierArg, source, startOffset, stopOffset);
        }

        @Override
        public boolean isLeaf() {
            return nestedItems.isEmpty();
        }

        @Override
        public List<StructureItem> getNestedItems() {
            return nestedItems;
        }

        public void setDepth(int depth) {
            this.depth = depth;
        }

        public int getDepth() {
            return depth;
        }
    }

}
