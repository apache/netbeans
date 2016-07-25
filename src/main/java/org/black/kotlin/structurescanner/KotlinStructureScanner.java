package org.black.kotlin.structurescanner;

import com.google.common.collect.Lists;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.PsiCoreCommentImpl;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
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
    
    @Override
    public List<? extends StructureItem> scan(ParserResult info) {
        return Collections.emptyList();
    }

    @Override
    public Map<String, List<OffsetRange>> folds(ParserResult info) {
        FileObject file = info.getSnapshot().getSource().getFileObject();
        if (file == null || ProjectUtils.getKotlinProjectForFileObject(file) == null){
            return Collections.emptyMap();
        }
        
        Map<String, List<OffsetRange>> folds = new HashMap<String, List<OffsetRange>>();
        
        List<OffsetRange> comments = Lists.newArrayList();
        List<OffsetRange> imports = Lists.newArrayList();
        List<OffsetRange> functions = Lists.newArrayList();
        
        KtFile ktFile = ProjectUtils.getKtFile(file);
        
        Collection<? extends PsiElement> elements =
            PsiTreeUtil.findChildrenOfAnyType(ktFile, 
                KtImportList.class, PsiCoreCommentImpl.class, KtNamedFunction.class);
        
        for (PsiElement elem : elements){
            int start = getStartOffset(elem);
            int end = elem.getTextRange().getEndOffset();
            if (start >= end){
                continue;
            }
            
            OffsetRange range = new OffsetRange(start, end);
            
            if (elem instanceof PsiCoreCommentImpl) {
                comments.add(range);
            } else if (elem instanceof KtNamedFunction) {
                functions.add(range);
            } else if (elem instanceof KtImportList) {
                imports.add(range);
            }
        }
        
        folds.put("comments", comments);
        folds.put("codeblocks", functions);
        folds.put("imports", imports);
        
        return folds;
    }

    @Override
    public Configuration getConfiguration() {
        return new Configuration(true, true);
    }
    
    private int getStartOffset(PsiElement elem){
        int start = elem.getTextRange().getStartOffset();
        if (elem instanceof KtNamedFunction){
            String name = elem.getText().split("\\{")[0];
            return start + name.length();
        } else if (elem instanceof KtImportList){
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
