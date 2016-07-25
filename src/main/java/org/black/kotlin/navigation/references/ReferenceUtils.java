package org.black.kotlin.navigation.references;

import com.google.common.collect.Lists;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.black.kotlin.builder.KotlinPsiManager;
import org.black.kotlin.resolve.KotlinAnalyzer;
import org.black.kotlin.resolve.NetBeansDescriptorUtils;
import org.black.kotlin.utils.ProjectUtils;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor;
import org.jetbrains.kotlin.descriptors.SourceElement;
import org.jetbrains.kotlin.lexer.KtTokens;
import org.jetbrains.kotlin.psi.KtAnnotatedExpression;
import org.jetbrains.kotlin.psi.KtBinaryExpression;
import org.jetbrains.kotlin.psi.KtCallExpression;
import org.jetbrains.kotlin.psi.KtConstructorDelegationReferenceExpression;
import org.jetbrains.kotlin.psi.KtDeclaration;
import org.jetbrains.kotlin.psi.KtElement;
import org.jetbrains.kotlin.psi.KtExpression;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtLabeledExpression;
import org.jetbrains.kotlin.psi.KtNameReferenceExpression;
import org.jetbrains.kotlin.psi.KtParenthesizedExpression;
import org.jetbrains.kotlin.psi.KtReferenceExpression;
import org.jetbrains.kotlin.psi.KtSimpleNameExpression;
import org.jetbrains.kotlin.psi.KtUnaryExpression;
import org.jetbrains.kotlin.resolve.BindingContext;
import org.jetbrains.kotlin.psi.psiUtil.KtPsiUtilKt;
import org.jetbrains.kotlin.resolve.source.KotlinSourceElement;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Александр
 */
public class ReferenceUtils {
    
    private static List<SourceElement> resolveToSourceElements(List<KotlinReference> refs) {
        if (refs.isEmpty()) {
            return Lists.newArrayList();
        }
        
        KtFile ktFile = refs.get(0).getReferenceExpression().getContainingKtFile();
        String path = ktFile.getVirtualFile().getCanonicalPath();
        File f = new File(path);
        FileObject file = FileUtil.toFileObject(f);
        if (file == null){
            return Lists.newArrayList();
        }
        
        Project project = ProjectUtils.getKotlinProjectForFileObject(file);
        if (project == null) {
            return Lists.newArrayList();
        }
        
        return resolveToSourceElements(refs, 
                KotlinAnalyzer.analyzeFile(project, ktFile).getAnalysisResult().getBindingContext(), project);
    }
    
    private static List<SourceElement> resolveToSourceElements(List<KotlinReference> refs, BindingContext context, Project project) {
        List<SourceElement> sourceElements = Lists.newArrayList();
        List<DeclarationDescriptor> declarationDescriptors = Lists.newArrayList();
        
        for (KotlinReference ref : refs) {
            declarationDescriptors.addAll(ref.getTargetDescriptors(context));
        }
        
        for (DeclarationDescriptor declarationDescriptor : declarationDescriptors) {
            sourceElements.addAll(NetBeansDescriptorUtils.descriptorToDeclarations(declarationDescriptor, project));
        }
        
        return sourceElements;
    }
    
    public static List<? extends SourceElement> resolveToSourceDeclaration(KtElement ktElement){
        if (ktElement instanceof KtDeclaration) {
            return Lists.newArrayList(new KotlinSourceElement(ktElement));
        } else {
            KtReferenceExpression referenceExpression = getReferenceExpression(ktElement);
            if (referenceExpression == null) {
                return Lists.newArrayList();
            }
            
            List<KotlinReference> refs = createReferences(referenceExpression);
            return resolveToSourceElements(refs);
        }
    }
    
    @Nullable
    public static KtReferenceExpression getReferenceExpression(PsiElement element) {
        return PsiTreeUtil.getNonStrictParentOfType(element, KtReferenceExpression.class);
    }
    
    public static List<KotlinReference> createReferences(KtReferenceExpression element){
        List<KotlinReference> refs = Lists.newArrayList();
        if (element instanceof KtNameReferenceExpression){
            if (((KtNameReferenceExpression) element).getReferencedNameElementType() != KtTokens.IDENTIFIER){
                return Collections.emptyList();
            }
            
            ReferenceAccess access = getReadWriteAccess(element);
            switch(access){
                case READ:
                    refs.add(new KotlinSyntheticPropertyAccessorReference.
                            Getter((KtNameReferenceExpression) element));
                    break;
                case WRITE:
                    refs.add(new KotlinSyntheticPropertyAccessorReference.
                            Setter((KtNameReferenceExpression) element));
                    break;
                case READ_WRITE:
                    refs.add(new KotlinSyntheticPropertyAccessorReference.
                            Getter((KtNameReferenceExpression) element));
                    refs.add(new KotlinSyntheticPropertyAccessorReference.
                            Setter((KtNameReferenceExpression) element));
                    break;
            }
            
        } else if (element instanceof KtSimpleNameExpression){
            refs.add(new KotlinSimpleNameReference((KtSimpleNameExpression) element));
        } else if (element instanceof KtCallExpression){
            refs.add(new KotlinInvokeFunctionReference(((KtCallExpression)element)));
        } else if (element instanceof KtConstructorDelegationReferenceExpression){
            refs.add(new KotlinConstructorDelegationReference((
                    (KtConstructorDelegationReferenceExpression) element)));
        } 
        
        return refs;
    }
    
    public static Collection<? extends DeclarationDescriptor> getReferenceTargets(KtReferenceExpression expression, 
            BindingContext context) {
        DeclarationDescriptor targetDescriptor = context.get(BindingContext.REFERENCE_TARGET, expression);
        if (targetDescriptor != null){
            return Lists.newArrayList(targetDescriptor);
        } else {
            Collection<? extends DeclarationDescriptor> refs = 
                    context.get(BindingContext.AMBIGUOUS_REFERENCE_TARGET, expression);
            if (refs == null){
                return Collections.emptyList();
            }
            return refs;
        }
    }
    
    private static ReferenceAccess getReadWriteAccess(KtExpression exp){
        KtExpression expression = KtPsiUtilKt.getQualifiedExpressionForSelectorOrThis(exp);
        
        while(true){
            PsiElement parent = expression.getParent();
            if (parent instanceof KtParenthesizedExpression ||
                    parent instanceof KtAnnotatedExpression ||
                    parent instanceof KtLabeledExpression){
                expression = (KtExpression) parent;
            } else {
                break;
            }
        }
        
        KtBinaryExpression assignment = KtPsiUtilKt.getAssignmentByLHS(expression);
        if (assignment != null){
            if (assignment.getOperationToken() == KtTokens.EQ){
                return ReferenceAccess.READ;
            } else {
                return ReferenceAccess.READ_WRITE;
            }
        }
        
        if (expression.getParent() instanceof KtUnaryExpression && expression.getParent() != null){
            IElementType operationToken = ((KtUnaryExpression) expression.getParent()).getOperationToken();
            if (operationToken == KtTokens.PLUSPLUS || 
                    operationToken == KtTokens.MINUSMINUS){
                return ReferenceAccess.READ_WRITE;
            }
        }
            
        return ReferenceAccess.READ;
        
    }
    
}
