package org.netbeans.test.java.editor.formatting;

import javax.lang.model.element.Modifier;

/**
  * Comment
 * @author jp159440
 */
public @interface testReformatAnnotation {

    //line comment
    String name()  default "n/a";

    
    
    int size();

    /**
        * Comment     
     */
  Modifier modifier()  default Modifier.ABSTRACT;
    
    
    
Deprecated d();

}
