#Signature file v4.1
#Version 1.32

CLSS public abstract interface java.lang.annotation.Annotation
meth public abstract boolean equals(java.lang.Object)
meth public abstract int hashCode()
meth public abstract java.lang.Class<? extends java.lang.annotation.Annotation> annotationType()
meth public abstract java.lang.String toString()

CLSS public abstract interface !annotation java.lang.annotation.Documented
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation java.lang.annotation.Retention
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.annotation.RetentionPolicy value()

CLSS public abstract interface !annotation java.lang.annotation.Target
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.annotation.ElementType[] value()

CLSS public abstract interface !annotation javax.annotation.Nonnull
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 javax.annotation.meta.TypeQualifier(java.lang.Class<?> applicableTo=class java.lang.Object)
innr public static Checker
intf java.lang.annotation.Annotation
meth public abstract !hasdefault javax.annotation.meta.When when()

CLSS public abstract interface !annotation javax.annotation.meta.TypeQualifier
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class<?> applicableTo()

CLSS public abstract interface !annotation javax.annotation.meta.TypeQualifierNickname
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation org.netbeans.api.annotations.common.CheckForNull
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
 anno 0 javax.annotation.Nonnull(javax.annotation.meta.When when=MAYBE)
 anno 0 javax.annotation.meta.TypeQualifierNickname()
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation org.netbeans.api.annotations.common.CheckReturnValue
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation org.netbeans.api.annotations.common.NonNull
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[FIELD, METHOD, PARAMETER, LOCAL_VARIABLE])
 anno 0 javax.annotation.Nonnull(javax.annotation.meta.When when=ALWAYS)
 anno 0 javax.annotation.meta.TypeQualifierNickname()
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation org.netbeans.api.annotations.common.NullAllowed
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[FIELD, PARAMETER, LOCAL_VARIABLE])
 anno 0 javax.annotation.Nonnull(javax.annotation.meta.When when=MAYBE)
 anno 0 javax.annotation.meta.TypeQualifierNickname()
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation org.netbeans.api.annotations.common.NullUnknown
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[FIELD, METHOD, PARAMETER, LOCAL_VARIABLE])
 anno 0 javax.annotation.Nonnull(javax.annotation.meta.When when=UNKNOWN)
 anno 0 javax.annotation.meta.TypeQualifierNickname()
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation org.netbeans.api.annotations.common.StaticResource
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean relative()
meth public abstract !hasdefault boolean searchClasspath()

CLSS public abstract interface !annotation org.netbeans.api.annotations.common.SuppressWarnings
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String justification()
meth public abstract !hasdefault java.lang.String[] value()

CLSS abstract interface org.netbeans.api.annotations.common.package-info

