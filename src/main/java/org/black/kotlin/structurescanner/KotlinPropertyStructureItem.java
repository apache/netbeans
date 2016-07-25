package org.black.kotlin.structurescanner;

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
        return new ImageIcon(ImageUtilities.loadImage("org/black/kotlin/completionIcons/field.png"));
    }
    
}
