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
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
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
import org.jetbrains.kotlin.psi.KtParameter;
import org.jetbrains.kotlin.psi.KtPrimaryConstructor;
import org.jetbrains.kotlin.psi.KtProperty;
import org.jetbrains.kotlin.psi.KtPropertyAccessor;
import org.jetbrains.kotlin.psi.KtSecondaryConstructor;
import org.jetbrains.kotlin.psi.KtVisitorVoid;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.Project;
import org.openide.util.Exceptions;

/**
 *
 * @author Alexander.Baratynski
 */
public class FromJavaToKotlinNavigationUtils {
    
    public static Element getElement(Document doc, int offset) {
        JavaSource javaSource = JavaSource.forDocument(doc);
        ElementSearcher searcher = new ElementSearcher(offset);
        try {
            javaSource.runUserActionTask(searcher, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        Element element = searcher.getElement();
        
        return element;
    }
    
    public static Pair<KtFile, Integer> findKotlinFileToNavigate(Element element, Project project) {
        if (element == null) {
            return null;
        }
        List<KtFile> ktFiles = ProjectUtils.getSourceFiles(project);
        
        for (KtFile ktFile : ktFiles) {
            KtElement ktElement = findKotlinDeclaration(element, ktFile);
            if (ktElement != null) {
                int offset = ktElement.getTextOffset();
                return new Pair(ktFile, offset);
            }
        }
        
        return null;
    }
    
    private static KtElement findKotlinDeclaration(Element element, KtFile ktFile) {
        List<KtElement> result = new ArrayList<KtElement>();
        KtVisitorVoid visitor = makeVisitor(element, result);
        if (visitor != null) {
            ktFile.acceptChildren(visitor);
        }
        
        return result.isEmpty() ? null : result.get(0);
    }
    
    
    private static KtVisitorVoid makeVisitor(final Element element, final List<KtElement> result) {
        switch (element.getKind()) {
            case CLASS:
            case INTERFACE:
            case ENUM:
                return new KtAllVisitor() {  
                    @Override
                    public void visitClassOrObject(KtClassOrObject ktClassOrObject) {
                        if (ktClassOrObject.getFqName().asString().equals(((TypeElement) element).getQualifiedName().toString())) {
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
                        if (equalsNames(declaration, element) &&
                            declaration.getName().toString().equals(((VariableElement)element).getSimpleName().toString())){
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
                        Element enclosingElement = element.getEnclosingElement();

                        String fqName = ((TypeElement) enclosingElement).getQualifiedName().toString();
                        if (equalsNames(ktClass, element) &&
                                (ktClass.getFqName().asString().equals(fqName))) {
                            result.add(ktClass);
                            return;
                        }

                        ktClass.acceptChildren(this);
                    }

                    private void visitExplicitDeclaration(KtDeclaration declaration) {
                        if (equalsNames(declaration, element) &&
                                equalsDeclaringTypes(declaration, (ExecutableElement) element)) {
                            result.add(declaration);
                        }
                    }

                };
            default:
                return null;
        }
    }
    
    public static boolean equalsNames(KtElement ktElement, Element element) {
        String first = ktElement.getName();
        String second = element.getSimpleName().toString();
        
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
        List<String> signatures = Lists.newArrayList(SourceUtils.getJVMSignature(ElementHandle.create(element)));
        for (Pair<String, String> pair : ktSignatures) {
            if (signatures.contains(pair.getFirst())) {
                return true;
            }
        }
        
        return false;
    }

    public static boolean equalsDeclaringTypes(KtElement ktElement, ExecutableElement element) {
        FqName typeNameInfo = getDeclaringTypeFqName(ktElement);
        Element enclosingElement = element.getEnclosingElement();
        
        String fqName = ((TypeElement) enclosingElement).getQualifiedName().toString();
        
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
    
    private static class ElementSearcher implements CancellableTask<CompilationController>{

        private Element element;
        private final int offset;
        
        public ElementSearcher(int offset){
            this.offset = offset;
        }
        
        @Override
        public void cancel() {
        }

        @Override
        public void run(CompilationController info) throws Exception {
            TreePath treePath = info.getTreeUtilities().pathFor(offset);
            element = info.getTrees().getElement(treePath);
        }
        
        public Element getElement(){
            return element;
        }
        
    }
    
}

