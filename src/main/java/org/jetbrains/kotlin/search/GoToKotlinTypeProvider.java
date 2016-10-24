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
package org.jetbrains.kotlin.search;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.text.StyledDocument;
import kotlin.Pair;
import org.jetbrains.kotlin.navigation.NavigationUtil;
import org.jetbrains.kotlin.psi.KtDeclaration;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

public class GoToKotlinTypeProvider implements SearchProvider {

    @Override
    public void evaluate(SearchRequest request, SearchResponse response) {
        for (Project project : OpenProjects.getDefault().getOpenProjects()) {
            List<Pair<KtFile, List<KtDeclaration>>> found = KotlinTypeSearcher.INSTANCE.searchDeclaration(project, request.getText());
            if (found.isEmpty()) {
                continue;
            }
            
            for (Pair<KtFile, List<KtDeclaration>> pair : found) {
                for (KtDeclaration declaration : pair.getSecond()) {
                    String html = declaration.getName() + " (" + 
                            pair.getFirst().getPackageFqName().asString() + ")";
                    response.addResult(new OpenDeclaration(pair.getFirst(), declaration), 
                        html);
                }
            }
        }
    }
    
    private static class OpenDeclaration implements Runnable {

        private final KtFile file;
        private final KtDeclaration declaration;
        
        OpenDeclaration(KtFile file, KtDeclaration declaration) {
                this.file = file;
                this.declaration = declaration;
        }
            
        @Override
        public void run() {
            File f = new File(file.getVirtualFile().getPath());
            FileObject fo = FileUtil.toFileObject(f);
            if (fo == null) {
                return;
            }
            
            StyledDocument doc = null;
            try {
                doc = ProjectUtils.getDocumentFromFileObject(fo);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (doc == null) {
                return;
            }
            
            NavigationUtil.openFileAtOffset(doc, declaration.getTextRange().getStartOffset());
        }
            
    }

}
