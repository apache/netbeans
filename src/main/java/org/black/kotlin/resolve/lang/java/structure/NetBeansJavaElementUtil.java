package org.black.kotlin.resolve.lang.java.structure;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.name.ClassId;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.name.Name;
/**
 *
 * @author Александр
 */
public class NetBeansJavaElementUtil {

    @Nullable
    public static ClassId computeClassId(Class classBinding){
        Class container = classBinding.getDeclaringClass();
        if (container != null){
            ClassId parentClassId = computeClassId(container);
            return parentClassId == null ? null : parentClassId.createNestedClassId(Name.identifier(classBinding.getName()));
        }
        
        String fqName = classBinding.getCanonicalName(); //Not sure
        return fqName == null ? null : ClassId.topLevel(new FqName(fqName));
    }
    
}
