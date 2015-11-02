package org.black.kotlin.model;

import com.intellij.codeInsight.NullableNotNullManager;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiModifierListOwner;
import java.util.List;
import org.netbeans.api.project.Project;

/**
 *
 * @author Александр
 */
public class KotlinNullableNotNullManager extends NullableNotNullManager {
    
    Project javaProject;
    
    public KotlinNullableNotNullManager(Project javaProject){
        this.javaProject = javaProject;
        setNotNulls("NotNull");
        setNullables("Nullable");
    }
    
    @Override
    public boolean hasHardcodedContracts(PsiElement element){
        return false;
    }
    
    @Override
    public boolean isNotNull(PsiModifierListOwner owner, boolean checkBases){
        List<String> notNullAnnotations = getNotNulls();
        PsiAnnotation[] annotations = owner.getModifierList().getAnnotations();
        for (PsiAnnotation an : annotations){
            if (notNullAnnotations.contains(an.getQualifiedName())){
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean isNullable(PsiModifierListOwner owner, boolean checkBases){
        return !isNotNull(owner,checkBases);
    }
    
}
