package org.black.kotlin.structurescanner;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiModifierList;
import edu.emory.mathcs.backport.java.util.Collections;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtDeclaration;
import org.jetbrains.kotlin.psi.KtNamedFunction;
import org.jetbrains.kotlin.psi.KtProperty;
import org.jetbrains.kotlin.psi.KtSuperTypeListEntry;
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
public class KotlinClassStructureItem implements StructureItem {

    private final KtClass psiElement;
    private final boolean isLeaf;
    
    public KotlinClassStructureItem(KtClass psiElement, boolean isLeaf) {
        this.psiElement = psiElement;
        this.isLeaf = isLeaf;
    }
    
    @Override
    public String getName() {
        String className = psiElement.getName();
        StringBuilder superType = new StringBuilder("");
        for (KtSuperTypeListEntry entry : psiElement.getSuperTypeListEntries()) {
            superType.append(entry.getText()).append(",");
        }
        if (superType.length() > 0){
            superType.deleteCharAt(superType.length()-1);
        }
        
        return className + "::" + superType.toString();
    }

    @Override
    public String getSortText() {
        return psiElement.getName();
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
        return ElementKind.CLASS;
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
        List<StructureItem> nestedItems = new ArrayList<StructureItem>();
        
        List<KtDeclaration> declarations = psiElement.getDeclarations();
        for (KtDeclaration declaration : declarations) {
            if (declaration instanceof KtClass){
                nestedItems.add(new KotlinClassStructureItem((KtClass) declaration, true));
            } else if (declaration instanceof KtNamedFunction) {
                nestedItems.add(new KotlinFunctionStructureItem((KtNamedFunction) declaration, true));
            } else if (declaration instanceof KtProperty) {
                nestedItems.add(new KotlinPropertyStructureItem((KtProperty) declaration, true));
            }
        }
        
        return nestedItems;
    }

    @Override
    public long getPosition() {
        return psiElement.getTextRange().getStartOffset();
    }

    @Override
    public long getEndPosition() {
        return psiElement.getTextRange().getEndOffset();
    }

    @Override
    public ImageIcon getCustomIcon() {
        return new ImageIcon(ImageUtilities.loadImage("org/black/kotlin/completionIcons/class.png"));
    }
    
}
    