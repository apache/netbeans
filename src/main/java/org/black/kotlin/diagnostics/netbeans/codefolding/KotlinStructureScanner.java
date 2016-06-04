package org.black.kotlin.diagnostics.netbeans.codefolding;

import com.google.common.collect.Lists;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.PsiCoreCommentImpl;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.ParserResult;
import org.openide.filesystems.FileObject;

import org.black.kotlin.utils.ProjectUtils;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtImportList;
import org.jetbrains.kotlin.psi.KtNamedFunction;

/**
 *
 * @author Александр
 */
public class KotlinStructureScanner implements StructureScanner{
    
    private final String COMMENT_FOLD = "/*...*/";
    private final String IMPORT_LIST_FOLD = "...";
    private final String FUNCTION_FOLD = "{...}";
    
    private final List<StructureItem> items = Lists.newArrayList();
    
    @Override
    public List<? extends StructureItem> scan(ParserResult info) {
        checkCode(info);
        return items;
    }

    @Override
    public Map<String, List<OffsetRange>> folds(ParserResult info) {
        return Collections.emptyMap();
    }

    @Override
    public Configuration getConfiguration() {
        return null;
    }
    
    private void checkCode(ParserResult info){
        FileObject file = info.getSnapshot().getSource().getFileObject();
        if (file == null || ProjectUtils.getKotlinProjectForFileObject(file) == null){
            return;
        }
        KtFile ktFile = ProjectUtils.getKtFile(file);
        
        Collection<? extends PsiElement> elements =
            PsiTreeUtil.findChildrenOfAnyType(ktFile, 
                KtImportList.class, PsiCoreCommentImpl.class, KtNamedFunction.class);
        
        for (PsiElement elem : elements){
            String type = getFoldType(elem);
            int start = getStartOffset(elem, type);
            int end = elem.getTextRange().getEndOffset();
            if (start >= end){
                continue;
            }
            OffsetRange range = new OffsetRange(start, end);
            StructureItem item = new KotlinStructureItem(type, range);
            
            if (!items.contains(item)){
                items.add(item);
            }
            
        }
        
    }
    
    private String getFoldType(PsiElement element){
        if (element instanceof KtImportList){
            return IMPORT_LIST_FOLD;
        } else if (element instanceof PsiCoreCommentImpl){
            return COMMENT_FOLD;
        } else if (element instanceof KtNamedFunction){
            return FUNCTION_FOLD;
        } else return "";
    }
    
    private int getStartOffset(PsiElement elem, String type){
        int start = elem.getTextRange().getStartOffset();
        if (type.equals(FUNCTION_FOLD)){
            String name = elem.getText().split("\\{")[0];
            return start + name.length();
        } else if (type.equals(IMPORT_LIST_FOLD)){
            return start + "import ".length();
        } else {
            return start;
        }
    }
    
    class KotlinStructureItem implements StructureItem{

        private final String name;
        private final OffsetRange offsetRange;
        
        public KotlinStructureItem(String name, OffsetRange offsetRange){
            this.name = name;
            this.offsetRange = offsetRange;
        }
        
        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getSortText() {
            return name;
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
            return name;
        }

        @Override
        public ElementHandle getElementHandle() {
            return null;
        }

        @Override
        public ElementKind getKind() {
            return null;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return null;
        }

        @Override
        public boolean isLeaf() {
            return false;
        }

        @Override
        public List<? extends StructureItem> getNestedItems() {
            return null;
        }

        @Override
        public long getPosition() {
            return offsetRange.getStart();
        }

        @Override
        public long getEndPosition() {
            return offsetRange.getEnd();
        }

        @Override
        public ImageIcon getCustomIcon() {
            return null;
        }
        
    }
    
}
