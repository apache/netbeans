
package org.netbeans.modules.javafx2.editor.actions;

import java.util.HashMap;
import java.util.Map;

public class FieldsWithTypeParams {

    class SimpleTypeParam<T> {
        // something
    }
    
    class BoundParam<T extends Comparable<?> & Runnable> {
        
    }
    
    class NestedTypeParam<K, V, T extends Map<K, V>> {
        
    }
    
    class ExtendsTyped<T, V> extends HashMap<T, V> {
        
    }
    
    NestedTypeParam<?, ?, Map<?, ?>> field;
}
