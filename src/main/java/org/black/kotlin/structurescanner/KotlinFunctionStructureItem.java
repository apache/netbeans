package org.black.kotlin.structurescanner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import org.jetbrains.kotlin.psi.KtNamedFunction;
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
        return function.getText().split("\\{")[0].split("=")[0];
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
        return new ImageIcon(ImageUtilities.loadImage("org/black/kotlin/completionIcons/method.png"));
    }
    
}
