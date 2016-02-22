package org.black.kotlin.resolve.lang.java.structure;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;

/**
 *
 * @author Александр
 */
public class NetBeansJavaElementFactory {

    private NetBeansJavaElementFactory(){}
    
    private interface Factory<Binding, Java> {
        @NotNull
        Java create(@NotNull Binding binding);
    }
    
    private static class Factories {
        private static final Factory<Element, JavaAnnotation> ANNOTATIONS =
                new Factory<Element, JavaAnnotation>() {
            @Override
            public JavaAnnotation create(Element binding) {
                return new NetBeansJavaAnnotation(binding);
            }
        };
    }

    @NotNull
    private static <Binding, Java> List<Java> convert(@NotNull Binding[] elements, 
            @NotNull Factory<Binding, Java> factory){
        if (elements.length == 0)
            return Collections.emptyList();
        List<Java> result = Lists.newArrayList();
        for (Binding element : elements){
            result.add(factory.create(element));
        }
        return result;
    }
    
    
    @NotNull
    public static List<JavaAnnotation> annotations(@NotNull Element[] annotations){
        return convert(annotations, Factories.ANNOTATIONS);
    }
    
}
