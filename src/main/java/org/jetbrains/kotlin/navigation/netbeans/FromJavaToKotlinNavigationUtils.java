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

import com.google.common.collect.Lists;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.text.Document;
import kotlin.Pair;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.jetbrains.kotlin.fileClasses.NoResolveFileClassesProvider;
import org.jetbrains.kotlin.filesystem.lightclasses.LightClassBuilderFactory;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtClassOrObject;
import org.jetbrains.kotlin.psi.KtDeclaration;
import org.jetbrains.kotlin.psi.KtElement;
import org.jetbrains.kotlin.psi.KtEnumEntry;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtNamedFunction;
import org.jetbrains.kotlin.psi.KtObjectDeclaration;
import org.jetbrains.kotlin.psi.KtPrimaryConstructor;
import org.jetbrains.kotlin.psi.KtProperty;
import org.jetbrains.kotlin.psi.KtPropertyAccessor;
import org.jetbrains.kotlin.psi.KtSecondaryConstructor;
import org.jetbrains.kotlin.psi.KtVisitorVoid;
import org.jetbrains.kotlin.resolve.lang.java.ElementHandleFieldContainingClassSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ElementHandleNameSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ElementHandleSimpleNameSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ElementSearcher;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.Project;
import org.openide.util.Exceptions;

/**
 *
 * @author Alexander.Baratynski
 */
public class FromJavaToKotlinNavigationUtils {
    
    public static ElementHandle getElement(Document doc, int offset) {
        JavaSource javaSource = JavaSource.forDocument(doc);
        ElementSearcher searcher = new ElementSearcher(offset);
        try {
            javaSource.runUserActionTask(searcher, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        ElementHandle element = searcher.getElement();
        
        return element;
    }
    
    public static Pair<KtFile, Integer> findKotlinFileToNavigate(ElementHandle element, Project project, Document doc) {
        if (element == null) {
            return null;
        }
        if (project == null) {
            return null;
        }
        
        List<KtFile> ktFiles = ProjectUtils.getSourceFiles(project);
        
        for (KtFile ktFile : ktFiles) {
            KtElement ktElement = findKotlinDeclaration(element, ktFile, doc);
            if (ktElement != null) {
                int offset = ktElement.getTextOffset();
                return new Pair(ktFile, offset);
            }
        }
        
        return null;
    }
    
    private static KtElement findKotlinDeclaration(ElementHandle element, KtFile ktFile, Document doc) {
        List<KtElement> result = new ArrayList<>();
        KtVisitorVoid visitor = makeVisitor(element, result, doc);
        if (visitor != null) {
            ktFile.acceptChildren(visitor);
        }
        
        return result.isEmpty() ? null : result.get(0);
    }
    
    
    private static KtVisitorVoid makeVisitor(final ElementHandle element, final List<KtElement> result, final Document doc) {
        switch (element.getKind()) {
            case CLASS:
            case INTERFACE:
            case ENUM:
                return new KtAllVisitor() {  
                    @Override
                    public void visitClassOrObject(KtClassOrObject ktClassOrObject) {
                        if (ktClassOrObject.getFqName().asString().equals(element.getQualifiedName())) {
                            result.add(ktClassOrObject);
                            return;
                        }

                        ktClassOrObject.acceptChildren(this);
                    }  
                };
            case FIELD:
                return new KtAllVisitor() {
                    @Override
                    public void visitObjectDeclaration(KtObjectDeclaration declaration) {
                        visitExplicitDeclaration(declaration);
                        declaration.acceptChildren(this);
                    }

                    @Override
                    public void visitEnumEntry(KtEnumEntry enumEntry) {
                        visitExplicitDeclaration(enumEntry);
                    }

                    @Override
                    public void visitProperty(KtProperty property) {
                        visitExplicitDeclaration(property);
                    }

                    private void visitExplicitDeclaration(KtDeclaration declaration) {
                        JavaSource javaSource = JavaSource.forDocument(doc);
                        ElementHandleNameSearcher searcher = new ElementHandleNameSearcher(element);
                        try {
                            javaSource.runUserActionTask(searcher, true);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        
                        if (equalsNames(declaration, element, doc) &&
                            declaration.getName().equals(searcher.getName().asString())){
                            result.add(declaration);
                        }
                    }
                };
            case METHOD:
            case CONSTRUCTOR:
                return new KtAllVisitor() {
                
                    @Override
                    public void visitNamedFunction(KtNamedFunction function) {
                        visitExplicitDeclaration(function);
                    }

                    @Override
                    public void visitProperty(KtProperty property) {
                        visitExplicitDeclaration(property);
                        property.acceptChildren(this);
                    }

                    @Override
                    public void visitPropertyAccessor(KtPropertyAccessor accessor) {
                        visitExplicitDeclaration(accessor);
                    }

                    @Override
                    public void visitSecondaryConstructor(KtSecondaryConstructor constructor) {
                        visitExplicitDeclaration(constructor);
                    }

                    @Override
                    public void visitPrimaryConstructor(KtPrimaryConstructor constructor) {
                        visitExplicitDeclaration(constructor);
                    }

                    @Override
                    public void visitClass(KtClass ktClass) {
                        JavaSource javaSource = JavaSource.forDocument(doc);
                        ElementHandleFieldContainingClassSearcher searcher = 
                                new ElementHandleFieldContainingClassSearcher(element);
                        try {
                            javaSource.runUserActionTask(searcher, true);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        ElementHandle containingClass = searcher.getContainingClass();
                        String fqName = containingClass.getQualifiedName();
                        if (equalsNames(ktClass, element, doc) &&
                                (ktClass.getFqName().asString().equals(fqName))) {
                            result.add(ktClass);
                            return;
                        }

                        ktClass.acceptChildren(this);
                    }

                    private void visitExplicitDeclaration(KtDeclaration declaration) {
                        if (equalsNames(declaration, element, doc) &&
                                equalsDeclaringTypes(declaration, element, doc)) {
                            result.add(declaration);
                        }
                    }

                };
            default:
                return null;
        }
    }
    
    public static boolean equalsNames(KtElement ktElement, ElementHandle element, Document doc) {
        String first = ktElement.getName();
        JavaSource javaSource = JavaSource.forDocument(doc);
        ElementHandleSimpleNameSearcher searcher = 
                new ElementHandleSimpleNameSearcher(element);
        try {
            javaSource.runUserActionTask(searcher, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        String second = searcher.getSimpleName();
        if (first == null || second == null) {
            return false;
        }
        
        if (!first.equals(second)) {
            return false;
        }
        
        Set<Pair<String,String>> ktSignatures = ktElement.getUserData(LightClassBuilderFactory.JVM_SIGNATURE);
        if (ktSignatures == null) {
            return false;
        }
        List<String> signatures = Lists.newArrayList(SourceUtils.getJVMSignature(element));
        for (Pair<String, String> pair : ktSignatures) {
            if (signatures.contains(pair.getFirst())) {
                return true;
            }
        }
        
        return false;
    }

    public static boolean equalsDeclaringTypes(KtElement ktElement, ElementHandle element, Document doc) {
        FqName typeNameInfo = getDeclaringTypeFqName(ktElement);
        JavaSource javaSource = JavaSource.forDocument(doc);
        ElementHandleFieldContainingClassSearcher searcher = 
                new ElementHandleFieldContainingClassSearcher(element);
        try {
            javaSource.runUserActionTask(searcher, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        ElementHandle containingClass = searcher.getContainingClass();
        String fqName = containingClass.getQualifiedName();
        
        return typeNameInfo.asString().equals(fqName) || typeNameInfo.asString().equals(fqName+"Kt");
    }
    
    private static FqName getDeclaringTypeFqName(KtElement ktElement) {
        PsiElement parent = PsiTreeUtil.getParentOfType(ktElement, KtClassOrObject.class, KtFile.class);
        if (parent != null) {
            return getTypeFqName(parent);
        } else return null;    
    }
    
    private static FqName getTypeFqName(PsiElement element) {
        if (element instanceof KtClassOrObject) {
            return ((KtClassOrObject) element).getFqName();
        } else if (element instanceof KtFile) {
            return NoResolveFileClassesProvider.INSTANCE.getFileClassInfo((KtFile) element).getFileClassFqName();
        } else return null;
    }
    
    private static class KtAllVisitor extends KtVisitorVoid {
        
        @Override
        public void visitElement(PsiElement element) {
            element.acceptChildren(this);
        }
        
    }
}

