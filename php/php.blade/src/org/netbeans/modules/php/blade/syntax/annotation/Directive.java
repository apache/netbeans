package org.netbeans.modules.php.blade.syntax.annotation;

/**
 *
 * @author bhaidu
 */
public @interface Directive {
    String name();
    boolean params() default false;
    String endtag() default "";
    String[] endTags() default {};
    String description() default "";
    String category() default "";
    String since() default "";
}
