/*******************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.jetbrains.kotlin.structurescanner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import org.jetbrains.kotlin.psi.KtProperty;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.StructureItem;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinPropertyStructureItem implements StructureItem {

    private final KtProperty property;
    private final boolean isLeaf;
    
    public KotlinPropertyStructureItem(KtProperty property, boolean isLeaf) {
        this.property = property;
        this.isLeaf = isLeaf;
    }
    
    
    @Override
    public String getName() {
        String type = "";
        if (property.getTypeReference() != null) {
            type = property.getTypeReference().getText();
        }
        return property.getName() + " : " + type;
    }

    @Override
    public String getSortText() {
        return property.getName();
    }

    @Override
    public String getHtml(HtmlFormatter hf) {
        return getName();
    }

    @Override
    public ElementHandle getElementHandle() {
        return null;
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.PROPERTY;
    }

    @Override
    public Set<Modifier> getModifiers() {
        return new HashSet<Modifier>();
    }

    @Override
    public boolean isLeaf() {
        return isLeaf;
    }

    @Override
    public List<? extends StructureItem> getNestedItems() {
        return new ArrayList<StructureItem>();
    }

    @Override
    public long getPosition() {
        return property.getTextRange().getStartOffset();
    }

    @Override
    public long getEndPosition() {
        return property.getTextRange().getEndOffset();
    }

    @Override
    public ImageIcon getCustomIcon() {
        return new ImageIcon(ImageUtilities.loadImage("org/jetbrains/kotlin/completionIcons/field.png"));
    }
    
}
