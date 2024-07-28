package org.netbeans.modules.php.blade.syntax.annotation;

/**
 *
 * @author bhaidu
 */
public @interface Tag {
    String openTag();
    String closeTag() default "";
    String description() default "";
    int position() default 0;
}
