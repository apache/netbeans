package org.black.kotlin.highlighter.semanticanalyzer;

import com.google.common.collect.Sets;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.black.kotlin.highlighter.semanticanalyzer.KotlinHighlightingAttributesGetter.KotlinHighlightingAttributes;
import org.jetbrains.kotlin.analyzer.AnalysisResult;
import org.jetbrains.kotlin.descriptors.ClassDescriptor;
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor;
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor;
import org.jetbrains.kotlin.descriptors.PropertyDescriptor;
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor;
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor;
import org.jetbrains.kotlin.descriptors.VariableDescriptor;
import org.jetbrains.kotlin.descriptors.impl.LocalVariableDescriptor;
import org.jetbrains.kotlin.psi.KtAnnotationEntry;
import org.jetbrains.kotlin.psi.KtClassOrObject;
import org.jetbrains.kotlin.psi.KtElement;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtNamedDeclaration;
import org.jetbrains.kotlin.psi.KtNamedFunction;
import org.jetbrains.kotlin.psi.KtParameter;
import org.jetbrains.kotlin.psi.KtProperty;
import org.jetbrains.kotlin.psi.KtSimpleNameExpression;
import org.jetbrains.kotlin.psi.KtSuperExpression;
import org.jetbrains.kotlin.psi.KtThisExpression;
import org.jetbrains.kotlin.psi.KtTypeParameter;
import org.jetbrains.kotlin.psi.KtValueArgumentList;
import org.jetbrains.kotlin.psi.KtVisitorVoid;
import org.jetbrains.kotlin.renderer.DescriptorRenderer;
import org.jetbrains.kotlin.resolve.BindingContext;
import org.jetbrains.kotlin.types.KotlinType;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OffsetRange;
//import static org.jetbrains.kotlin.psi.psiUtil.KtPsiUtilKt.getCalleeHighlightingRange;
import org.jetbrains.kotlin.resolve.DescriptorUtils;
/**
 *
 * @author Александр
 */
public class KotlinSemanticHighlightingVisitor extends KtVisitorVoid {
    
    private final KtFile ktFile;
    private final AnalysisResult result;
    private BindingContext bindingContext;
    private final Map<OffsetRange, Set<ColoringAttributes>> positions = 
            new HashMap<OffsetRange, Set<ColoringAttributes>>();
    
    public KotlinSemanticHighlightingVisitor(KtFile ktFile, AnalysisResult result){
        super();
        this.ktFile = ktFile;
        this.result = result;
    }
    
    public Map<OffsetRange, Set<ColoringAttributes>> computeHighlightingRanges() {
        positions.clear();
        
        bindingContext = result.getBindingContext();
        ktFile.acceptChildren(this);
        
        return positions;
    }
    
    private void highlight(KotlinHighlightingAttributes styleAttributes, TextRange range){
        OffsetRange offsetRange = new OffsetRange(range.getStartOffset(), range.getEndOffset());
        positions.put(offsetRange, Sets.newHashSet(styleAttributes.styleKey));
    }
    
    private void highlightSmartCast(TextRange range, String typeName){
        OffsetRange offsetRange = new OffsetRange(range.getStartOffset(), range.getEndOffset());
        //TODO
    }
    
    @Override
    public void visitElement(PsiElement element){
        element.acceptChildren(this);
    }
    
    @Override
    public void visitSimpleNameExpression(KtSimpleNameExpression expression){
        PsiElement parentExpression = expression.getParent();
        if (parentExpression instanceof KtThisExpression 
                || parentExpression instanceof KtSuperExpression){
            return;
        }
        
        DeclarationDescriptor target = 
                bindingContext.get(BindingContext.REFERENCE_TARGET, expression);
        if (target == null){
            return;
        }
        if (target instanceof ConstructorDescriptor){
            target = target.getContainingDeclaration();
        }
        
        KotlinType smartCast = bindingContext.get(BindingContext.SMARTCAST, expression);
        String typeName = null;
        if (smartCast != null){
            typeName = DescriptorRenderer.FQ_NAMES_IN_TYPES.renderType(smartCast);
        }
        
        if (target instanceof TypeParameterDescriptor){
            highlightTypeParameter(expression);
        } else if (target instanceof ClassDescriptor){
            highlightClassDescriptor(expression, (ClassDescriptor) target);
        } else if (target instanceof PropertyDescriptor){
            highlightProperty(expression, (PropertyDescriptor) target, typeName);
        } else if (target instanceof VariableDescriptor){
            highlightVariable(expression, target, typeName);
        }
        
        super.visitSimpleNameExpression(expression);
    }

    @Override
    public void visitTypeParameter(KtTypeParameter parameter){
        PsiElement identifier = parameter.getNameIdentifier();
        if (identifier != null){
            highlightTypeParameter(identifier);
        }
        
        super.visitTypeParameter(parameter);
    }
    
    @Override
    public void visitClassOrObject(KtClassOrObject classOrObject){
        PsiElement identifier = classOrObject.getNameIdentifier();
        ClassDescriptor classDescriptor = bindingContext.get(BindingContext.CLASS, classOrObject);
        if (identifier != null && classDescriptor != null) {
            highlightClassDescriptor(identifier, classDescriptor);
        }
        
        super.visitClassOrObject(classOrObject);
    }
    
    @Override
    public void visitProperty(KtProperty property){
        PsiElement identifier = property.getNameIdentifier();
        if (identifier == null){
            return;
        }
        
        VariableDescriptor propertyDescriptor = bindingContext.get(BindingContext.VARIABLE, property);
        if (propertyDescriptor instanceof PropertyDescriptor){
            highlightProperty(identifier, (PropertyDescriptor) propertyDescriptor, null);
        } else {
            visitVariableDeclaration(property);
        }
        
        super.visitProperty(property);
    }
    
    @Override
    public void visitParameter(KtParameter parameter){
        PsiElement identifier = parameter.getNameIdentifier();
        if (identifier == null){
            return;
        }
        
        VariableDescriptor propertyDescriptor = bindingContext.get(BindingContext.PRIMARY_CONSTRUCTOR_PARAMETER,
                parameter);
        if (propertyDescriptor instanceof PropertyDescriptor){
            highlightProperty(identifier, (PropertyDescriptor) propertyDescriptor, null);
        } else {
            visitVariableDeclaration(parameter);
        }
        
        super.visitParameter(parameter);
    }
    
    @Override
    public void visitNamedFunction(KtNamedFunction function){
        PsiElement identifier = function.getNameIdentifier();
        if (identifier != null){
            highlight(KotlinHighlightingAttributesGetter.INSTANCE.FUNCTION_DECLARATION, identifier.getTextRange());
        }
        
        super.visitNamedFunction(function);
    }
    
    private void visitVariableDeclaration(KtNamedDeclaration declaration){
        DeclarationDescriptor declarationDescriptor = bindingContext.get(BindingContext.DECLARATION_TO_DESCRIPTOR,
                declaration);
        PsiElement identifier = declaration.getNameIdentifier();
        
        if (identifier != null && declarationDescriptor != null){
            highlightVariable(identifier, declarationDescriptor, null);
        }
    }
    
    private void highlightTypeParameter(PsiElement element) {
        highlight(KotlinHighlightingAttributesGetter.INSTANCE.TYPE_PARAMETER, element.getTextRange());
    }

    private void highlightAnnotation(PsiElement expression) {
        TextRange range = expression.getTextRange();
        KtAnnotationEntry annotationEntry = PsiTreeUtil.getParentOfType(expression, KtAnnotationEntry.class,false,KtValueArgumentList.class);
        
        if (annotationEntry != null) {
            PsiElement atSymbol = annotationEntry.getAtSymbol();
            if (atSymbol != null) {
                range = new TextRange(atSymbol.getTextRange().getStartOffset(), expression.getTextRange().getEndOffset());
            }
        }
        
        highlight(KotlinHighlightingAttributesGetter.INSTANCE.ANNOTATION, range);
    }
    
    
    private void highlightClassDescriptor(PsiElement element, ClassDescriptor target) {
        switch (target.getKind()){
            case INTERFACE:
                highlight(KotlinHighlightingAttributesGetter.INSTANCE.INTERFACE, element.getTextRange());
                break;
            case ANNOTATION_CLASS:
                highlightAnnotation(element);
                break;
            case ENUM_ENTRY:
                highlight(KotlinHighlightingAttributesGetter.INSTANCE.STATIC_FINAL_FIELD, element.getTextRange());
                break;
            case ENUM_CLASS:
                highlight(KotlinHighlightingAttributesGetter.INSTANCE.ENUM_CLASS, element.getTextRange());
                break;
            case CLASS:
            case OBJECT:
                highlight(KotlinHighlightingAttributesGetter.INSTANCE.CLASS, element.getTextRange());
                break;
        }
    }

    private void highlightProperty(PsiElement element, PropertyDescriptor descriptor, String typeName) {
        TextRange range = element.getTextRange();
        boolean mutable = descriptor.isVar();
        KotlinHighlightingAttributes attributes;
        if (DescriptorUtils.isStaticDeclaration(descriptor)){
            if (mutable){
                attributes = KotlinHighlightingAttributesGetter.INSTANCE.STATIC_FIELD;
            } else {
                attributes = KotlinHighlightingAttributesGetter.INSTANCE.STATIC_FINAL_FIELD;
            }
        } else {
            if (mutable) {
                attributes = KotlinHighlightingAttributesGetter.INSTANCE.FIELD;
            } else {
                attributes = KotlinHighlightingAttributesGetter.INSTANCE.FINAL_FIELD;
            }
        }
        
        if (typeName != null){
            highlightSmartCast(element.getTextRange(), typeName);
        }
        
        highlight(attributes, range);
    }

    private void highlightVariable(PsiElement element, DeclarationDescriptor descriptor, String typeName) {
        if (!(descriptor instanceof VariableDescriptor)){
            return;
        }
        KotlinHighlightingAttributes attributes;
        if (descriptor instanceof LocalVariableDescriptor){
            if (((LocalVariableDescriptor) descriptor).isVar()){
                attributes = KotlinHighlightingAttributesGetter.INSTANCE.LOCAL_VARIABLE;
            } else {
                attributes = KotlinHighlightingAttributesGetter.INSTANCE.LOCAL_FINAL_VARIABLE;
            }
        } else if (descriptor instanceof ValueParameterDescriptor){
            attributes = KotlinHighlightingAttributesGetter.INSTANCE.PARAMETER_VARIABLE;
        } else {
            attributes = KotlinHighlightingAttributesGetter.INSTANCE.LOCAL_VARIABLE;
//            throw new IllegalStateException("Highlight attributes error");
        }
        
        if (typeName != null){
            highlightSmartCast(element.getTextRange(), typeName);
        }
        
        highlight(attributes, element.getTextRange());
    }
    
    
}
