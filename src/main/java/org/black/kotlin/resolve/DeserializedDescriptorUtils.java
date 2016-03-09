package org.black.kotlin.resolve;

import java.util.Iterator;
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor;
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedCallableMemberDescriptor;
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedClassDescriptor;
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor;
import org.jetbrains.kotlin.resolve.DescriptorUtils;
import org.jetbrains.kotlin.descriptors.ClassDescriptor;
import static org.jetbrains.kotlin.resolve.descriptorUtil.DescriptorUtilsKt.getParentsWithSelf;

public class DeserializedDescriptorUtils {
    
    public static boolean isDeserialized(DeclarationDescriptor descriptor){
        return descriptor instanceof DeserializedCallableMemberDescriptor ||
                descriptor instanceof DeserializedClassDescriptor;
    }

    public static DeclarationDescriptor getContainingClassOrPackage(DeclarationDescriptor descriptor) {
        DeclarationDescriptor decDes;
        Iterator<DeclarationDescriptor> it = getParentsWithSelf(descriptor).iterator();
        if (it.hasNext()){
            decDes = it.next();
            if ((decDes instanceof ClassDescriptor && DescriptorUtils.isTopLevelDeclaration(decDes)) 
                    || decDes instanceof PackageFragmentDescriptor ){
                return decDes;
            }
        } 
        return null;
    }
    
}
