/**
 * *****************************************************************************
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
 ******************************************************************************
 */
package org.jetbrains.kotlin.navigation.netbeans;

import com.google.common.collect.Sets;
import com.intellij.psi.PsiElement;
import java.util.Set;
import javax.swing.text.Document;
import kotlin.Pair;
import org.jetbrains.kotlin.navigation.NavigationUtilKt;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.jetbrains.kotlin.psi.KtReferenceExpression;
import org.jetbrains.kotlin.navigation.references.ReferenceUtilsKt;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.project.Project;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.openide.filesystems.FileObject;

@MimeRegistration(mimeType = "text/x-kt", service = HyperlinkProviderExt.class)
public class KotlinHyperlinkProvider implements HyperlinkProviderExt {

    private PsiElement psi = null;
    private KtReferenceExpression referenceExpression = null;
    private Pair<Document, Integer> navigationCache = null;

    @Override
    public boolean isHyperlinkPoint(Document doc, int offset, HyperlinkType type) {
        psi = NavigationUtilKt.getReferenceExpression(doc, offset);
        if (psi == null) return false;
        
        referenceExpression = ReferenceUtilsKt.getReferenceExpression(psi);
        return referenceExpression != null;
        
    }

    @Override
    public int[] getHyperlinkSpan(Document doc, int offset, HyperlinkType type) {
        if (isHyperlinkPoint(doc, offset, type)) {
            Pair<Integer, Integer> span = NavigationUtilKt.getSpan(psi);
            if (span == null) {
                return null;
            }
            return new int[]{span.getFirst(), span.getSecond()};
        }
        return null;
    }

    @Override
    public void performClickAction(Document doc, int offset, HyperlinkType type) {
        if (referenceExpression == null) {
            return;
        }

        FileObject file = ProjectUtils.getFileObjectForDocument(doc);
        if (file == null) {
            return;
        }

        Project project = getProjectForNavigation(file);
        if (project == null) {
            return;
        }

        navigationCache = OpenDeclarationKt.navigate(referenceExpression, project, file);
    }

    @Override
    public Set<HyperlinkType> getSupportedHyperlinkTypes() {
        return Sets.newHashSet(HyperlinkType.GO_TO_DECLARATION);
    }

    @Override
    public String getTooltipText(Document doc, int offset, HyperlinkType type) {
        return HoverUtilsKt.getToolTip(referenceExpression, doc, offset);
    }
    
    private Project getProjectForNavigation(FileObject fo) {
        Project project = ProjectUtils.getKotlinProjectForFileObject(fo);
        if (project != null) {
            return project;
        }
        return ProjectUtils.getValidProject();
    }

    public Pair<Document, Integer> getNavigationCache() {
        return navigationCache;
    }

}
