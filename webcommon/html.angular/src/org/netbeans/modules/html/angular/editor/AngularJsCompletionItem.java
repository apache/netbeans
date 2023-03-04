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
package org.netbeans.modules.html.angular.editor;

import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Petr Pisl
 */
public class AngularJsCompletionItem implements CompletionProposal {

    private static final String ANGULAR_NAME = "Angular Js";
    private static ImageIcon angularIcon = null;

    private final int anchorOffset;
    private final ElementHandle element;

    public AngularJsCompletionItem(final ElementHandle element, final int anchorOffset) {
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
    public ElementKind getKind() {
        return element.getKind();
    }

    @Override
    public ImageIcon getIcon() {
        if (angularIcon == null) {
            angularIcon = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/html/angular/resources/AngularJS_icon_16.png")); //NOI18N
        }
        return angularIcon;
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
        return 20;
    }

    @Override
    public String getCustomInsertTemplate() {
        return null;
    }

    @Override
    public String getLhsHtml(HtmlFormatter formatter) {
        return getName();
    }

    @Override
    public String getRhsHtml(HtmlFormatter formatter) {
        return ANGULAR_NAME;
    }

    public static class AngularFOCompletionItem extends AngularJsCompletionItem {

        private final FileObject fo;

        public AngularFOCompletionItem(ElementHandle element, int anchorOffset, FileObject fo) {
            super(element, anchorOffset);
            this.fo = fo;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            if (fo != null) {
                return fo.getNameExt();
            } else {
                return super.getRhsHtml(formatter);
            }
        }

    }

}
