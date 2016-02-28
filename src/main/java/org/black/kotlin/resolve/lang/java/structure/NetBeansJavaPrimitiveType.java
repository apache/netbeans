package org.black.kotlin.resolve.lang.java.structure;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.builtins.PrimitiveType;
import org.jetbrains.kotlin.load.java.structure.JavaPrimitiveType;
import org.jetbrains.kotlin.resolve.jvm.JvmPrimitiveType;

/**
 *
 * @author Александр
 */
public class NetBeansJavaPrimitiveType extends NetBeansJavaType<TypeMirror> implements JavaPrimitiveType {
    
    public NetBeansJavaPrimitiveType(TypeMirror typeBinding){
        super(typeBinding);
    }
    
    @Override
    @Nullable
    public PrimitiveType getType(){
        String text = getBinding().getKind().name();//maybe it should not be simple TO FIX IN FUTURE
        return "void".equals(text) ? null : JvmPrimitiveType.get(text).getPrimitiveType();
    }
    
}
