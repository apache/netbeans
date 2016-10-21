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

import java.util.ArrayList;
import java.util.List;
import kotlin.Pair;
import org.jetbrains.kotlin.psi.KtDeclaration;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinTypeSearcher {
    
    public static List<Pair<KtFile, List<KtDeclaration>>> searchDeclaration(Project project, String fqName) {
        List<Pair<KtFile, List<KtDeclaration>>> found = new ArrayList<>();
        
        for (KtFile ktFile : ProjectUtils.getSourceFilesWithDependencies(project)) {
            List<KtDeclaration> declarations = findDeclarationsInFile(ktFile, fqName);
            if (!declarations.isEmpty()) {
                found.add(new Pair<>(ktFile, declarations));
            }
        }
        
        return found;
    }
    
    private static List<KtDeclaration> findDeclarationsInFile(KtFile ktFile, String fqName) {
        List<KtDeclaration> declarations = ktFile.getDeclarations();
        List<KtDeclaration> declarationsToReturn = new ArrayList<>();
        
        for (KtDeclaration declaration : declarations) {
            if (declaration.getName().contains(fqName)) {
                declarationsToReturn.add(declaration);
            }
        }
        
        return declarationsToReturn;
    }
    
}
