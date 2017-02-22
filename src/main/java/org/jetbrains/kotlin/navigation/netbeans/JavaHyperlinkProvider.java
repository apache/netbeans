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
package org.jetbrains.kotlin.navigation.netbeans;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.Set;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import kotlin.Pair;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.jetbrains.kotlin.psi.KtFile;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.Project;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

@MimeRegistration(mimeType = "text/x-java", service = HyperlinkProviderExt.class)
public final class JavaHyperlinkProvider implements HyperlinkProviderExt {
    
    private Class clazz = null;
    private Object object = null;
    
    public JavaHyperlinkProvider() {
        ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
        try {
            clazz = Class.forName(
                    "org.netbeans.modules.java.editor.hyperlink.JavaHyperlinkProvider",
                    true, loader);
            Constructor constructor = clazz.getConstructor();
            object = constructor.newInstance();
        } catch (ReflectiveOperationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public Set<HyperlinkType> getSupportedHyperlinkTypes() {
        return EnumSet.of(HyperlinkType.GO_TO_DECLARATION, HyperlinkType.ALT_HYPERLINK);
    }

    private int[] getIdentifierSpan(Document doc, int offset, HyperlinkType type) {
        if (clazz == null || object == null) {
            return new int[0];
        }
        
        try {
            Method method = clazz.getMethod("getHyperlinkSpan", Document.class, int.class, HyperlinkType.class);
            return (int[]) method.invoke(object, doc, offset, type);
        } catch (ReflectiveOperationException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return new int[0];
    }
    
    private String tooltipText(Document doc, int offset, HyperlinkType type) {
        if (clazz == null || object == null) {
            return null;
        }
        
        try {
            Method method = clazz.getMethod("getTooltipText", Document.class, int.class, HyperlinkType.class);
            return (String) method.invoke(object, doc, offset, type);
        } catch (ReflectiveOperationException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return null;
    }
    
    private void clickAction(Document doc, int offset, HyperlinkType type) {
        if (clazz == null || object == null) {
            return;
        }
        
        try {
            Method method = clazz.getMethod("performClickAction", Document.class, int.class, HyperlinkType.class);
            method.invoke(object, doc, offset, type);
        } catch (ReflectiveOperationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    @Override
    public boolean isHyperlinkPoint(Document doc, int offset, HyperlinkType type) {
        return getHyperlinkSpan(doc, offset, type) != null;
    }

    @Override
    public int[] getHyperlinkSpan(Document doc, int offset, HyperlinkType type) {
        return getIdentifierSpan(doc, offset, null);
    }

    @Override
    public void performClickAction(Document doc, int offset, HyperlinkType type) {
        switch (type) {
            case GO_TO_DECLARATION:
                ElementHandle element = FromJavaToKotlinNavigationUtilsKt.getElement(doc, offset);
                FileObject file = ProjectUtils.getFileObjectForDocument(doc);
                Project project = ProjectUtils.getKotlinProjectForFileObject(file);
                Pair<KtFile, Integer> pair = FromJavaToKotlinNavigationUtilsKt.findKotlinFileToNavigate(element, project, doc);
                
                if (pair == null) {
                    clickAction(doc, offset, type);
                    break;
                }
                
                KtFile ktFile = pair.getFirst();
                int offsetToOpen = pair.getSecond();
                
                if (ktFile != null) {
                    String filePath = ktFile.getVirtualFile().getPath();
                    FileObject fileToOpen = FileUtil.toFileObject(new File(filePath));
                    try {
                        StyledDocument docToOpen = ProjectUtils.getDocumentFromFileObject(fileToOpen);
                        OpenDeclarationKt.openFileAtOffset(docToOpen, offsetToOpen);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else clickAction(doc, offset, type);
                break;
            case ALT_HYPERLINK:
                clickAction(doc, offset, type);
                break;
        }
    }

    @Override
    public String getTooltipText(Document doc, int offset, HyperlinkType type) {
        return tooltipText(doc, offset, null);
    }

}
