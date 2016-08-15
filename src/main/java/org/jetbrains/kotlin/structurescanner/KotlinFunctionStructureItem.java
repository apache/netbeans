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

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import org.jetbrains.kotlin.psi.KtNamedFunction;
import org.jetbrains.kotlin.psi.KtParameter;
import org.jetbrains.kotlin.psi.KtPsiUtil;
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
public class KotlinFunctionStructureItem implements StructureItem {

    private final KtNamedFunction function;
    private final boolean isLeaf;
    
    public KotlinFunctionStructureItem(KtNamedFunction function, boolean isLeaf) {
        this.function = function;
        this.isLeaf = isLeaf;
    }
    
    @Override
    public String getName() {
        StringBuilder builder = new StringBuilder();
        builder.append(function.getName()).append("(");
        List<KtParameter> valueParameters = function.getValueParameters();
        for (KtParameter param : valueParameters) {
            builder.append(param.getText()).append(",");
        }
        if (!valueParameters.isEmpty()) {
            builder.deleteCharAt(builder.length() - 1);
        }
        builder.append(")");
        PsiElement colon = function.getColon();
        if (colon == null) {
            return builder.toString();
        }
        
        PsiElement returnType = colon.getNextSibling();
        if (returnType instanceof PsiWhiteSpace) {
            returnType = returnType.getNextSibling();
        }
        builder.append(" : ").append(returnType.getText());
        
        return builder.toString();
    }

    @Override
    public String getSortText() {
        return function.getName();
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
        return ElementKind.METHOD;
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
        return function.getTextRange().getStartOffset();
    }

    @Override
    public long getEndPosition() {
        return function.getTextRange().getEndOffset();
    }

    @Override
    public ImageIcon getCustomIcon() {
        return new ImageIcon(ImageUtilities.loadImage("org/jetbrains/kotlin/completionIcons/method.png"));
    }
    
}
