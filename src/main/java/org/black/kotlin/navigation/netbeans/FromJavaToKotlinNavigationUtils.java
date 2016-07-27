package org.black.kotlin.navigation.netbeans;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.swing.text.Document;
import kotlin.Pair;
import org.black.kotlin.builder.KotlinPsiManager;
import org.black.kotlin.filesystem.lightclasses.LightClassBuilderFactory;
import org.black.kotlin.project.KotlinSources;
import org.black.kotlin.utils.ProjectUtils;
import org.jetbrains.kotlin.fileClasses.NoResolveFileClassesProvider;
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
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.java.GoToSupport;
import org.openide.filesystems.FileObject;
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
    
    public static KtFile findKotlinFileToNavigate(Element element, Project project) {
        List<KtFile> ktFiles = ProjectUtils.getSourceFiles(project);
        
        for (KtFile ktFile : ktFiles) {
            KtElement ktElement = findKotlinDeclaration(element, ktFile);
            if (ktElement != null) {
                return ktFile;
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
        if (element.getKind() ==  ElementKind.CLASS 
                || element.getKind() ==  ElementKind.INTERFACE
                || element.getKind() ==  ElementKind.ENUM) {
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
        } else if (element.getKind() == ElementKind.FIELD) {
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
                    if (equalsJvmSignature(declaration, element)){
                        result.add(declaration);
                    }
                }
            };
        } if (element.getKind() == ElementKind.METHOD || element.getKind() == ElementKind.CONSTRUCTOR) {
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
                    if (enclosingElement == null || !enclosingElement.getKind().isClass()
                        || !enclosingElement.getKind().isInterface()) {
                        return;
                    }
                    String fqName = ((TypeElement) enclosingElement).getQualifiedName().toString();
                    if (equalsJvmSignature(ktClass, element) && (ktClass.getFqName().asString().equals(fqName))) {
                        result.add(ktClass);
                        return;
                    }
                    
                    ktClass.acceptChildren(this);
                }
                
                private void visitExplicitDeclaration(KtDeclaration declaration) {
                    if (equalsJvmSignature(declaration, element) && equalsDeclaringTypes(declaration, (ExecutableElement) element)) {
                        result.add(declaration);
                    }
                }
                
            };
        } else return null;
    }
    
    public static boolean equalsJvmSignature(KtElement ktElement, Element element) {
        Set<Pair<String,String>> ktSignatures = ktElement.getUserData(LightClassBuilderFactory.JVM_SIGNATURE);
        if (ktSignatures == null) {
            return false;
        }
        Iterator<Pair<String, String>> it = ktSignatures.iterator();
        while (it.hasNext()) {
            Pair<String, String> pair = it.next();
            String first = pair.getFirst();
            String second = pair.getSecond();
        }
        
        String elementSignature;
        switch (element.getKind()) {
            case FIELD:
                break;
            case METHOD:
            case CONSTRUCTOR:
                break;
            default:
                elementSignature = null;
        }
        
        return true;
    }
    
    public static boolean equalsDeclaringTypes(KtElement ktElement, ExecutableElement element) {
        FqName typeNameInfo = getDeclaringTypeFqName(ktElement);
        Element enclosingElement = element.getEnclosingElement();
        if (enclosingElement == null || !enclosingElement.getKind().isClass()
                || !enclosingElement.getKind().isInterface()) {
            return false;
        }
        
        String fqName = ((TypeElement) enclosingElement).getQualifiedName().toString();
        
        return typeNameInfo.asString().equals(fqName);
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

