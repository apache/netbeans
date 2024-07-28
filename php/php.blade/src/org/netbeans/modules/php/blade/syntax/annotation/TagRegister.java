package org.netbeans.modules.php.blade.syntax.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author bhaidu
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PACKAGE})
public @interface TagRegister {
    public Tag[] value();
}
