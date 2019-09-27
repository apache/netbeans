package org.netbeans.modules.java.editor.semantic.data;

import java.util.List;

public class AttributeDefaultValue {

    public static @interface StopAt {
        Class value() default StopAtCurrentClass.class;
        Class valuex() default List.class;
    }
    
    private static interface StopAtCurrentClass {}

}
