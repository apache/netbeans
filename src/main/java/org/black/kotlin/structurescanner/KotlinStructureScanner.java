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
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.ParserResult;
import org.openide.filesystems.FileObject;

import org.black.kotlin.utils.ProjectUtils;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtDeclaration;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtImportList;
import org.jetbrains.kotlin.psi.KtNamedFunction;
import org.jetbrains.kotlin.psi.KtProperty;

/**
 *
 * @author Александр
 */
public class KotlinStructureScanner implements StructureScanner{
    
    @Override
    public List<? extends StructureItem> scan(ParserResult info) {
        FileObject file = info.getSnapshot().getSource().getFileObject();
        if (file == null || ProjectUtils.getKotlinProjectForFileObject(file) == null){
            return Collections.emptyList();
        }
        
        List<StructureItem> items = Lists.newArrayList();
        
        KtFile ktFile = ProjectUtils.getKtFile(file);
        List<KtDeclaration> declarations = ktFile.getDeclarations();
        
        for (KtDeclaration declaration : declarations) {
            if (declaration instanceof KtClass){
                items.add(new KotlinClassStructureItem((KtClass) declaration, false));
            } else if (declaration instanceof KtNamedFunction) {
                items.add(new KotlinFunctionStructureItem((KtNamedFunction) declaration, false));
            } else if (declaration instanceof KtProperty) {
                items.add(new KotlinPropertyStructureItem((KtProperty) declaration, false));
            }
        }
        
        return items;
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
    
    
}
