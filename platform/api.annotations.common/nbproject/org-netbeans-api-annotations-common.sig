#Signature file v4.1
#Version 1.51

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public abstract java.lang.Enum<%0 extends java.lang.Enum<{java.lang.Enum%0}>>
cons protected init(java.lang.String,int)
intf java.io.Serializable
intf java.lang.Comparable<{java.lang.Enum%0}>
meth protected final java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected final void finalize()
meth public final boolean equals(java.lang.Object)
meth public final int compareTo({java.lang.Enum%0})
meth public final int hashCode()
meth public final int ordinal()
meth public final java.lang.Class<{java.lang.Enum%0}> getDeclaringClass()
meth public final java.lang.String name()
meth public java.lang.String toString()
meth public static <%0 extends java.lang.Enum<{%%0}>> {%%0} valueOf(java.lang.Class<{%%0}>,java.lang.String)
supr java.lang.Object

CLSS public java.lang.Object
cons public init()
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void finalize() throws java.lang.Throwable
meth public boolean equals(java.lang.Object)
meth public final java.lang.Class<?> getClass()
meth public final void notify()
meth public final void notifyAll()
meth public final void wait() throws java.lang.InterruptedException
meth public final void wait(long) throws java.lang.InterruptedException
meth public final void wait(long,int) throws java.lang.InterruptedException
meth public int hashCode()
meth public java.lang.String toString()

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
intf java.lang.annotation.Annotation
meth public abstract !hasdefault javax.annotation.meta.When when()

CLSS public abstract interface !annotation javax.annotation.meta.TypeQualifier
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class<?> applicableTo()

CLSS public abstract interface !annotation javax.annotation.meta.TypeQualifierNickname
intf java.lang.annotation.Annotation

CLSS public final !enum javax.annotation.meta.When
fld public final static javax.annotation.meta.When ALWAYS
fld public final static javax.annotation.meta.When MAYBE
fld public final static javax.annotation.meta.When UNKNOWN
meth public static javax.annotation.meta.When valueOf(java.lang.String)
meth public static javax.annotation.meta.When[] values()
supr java.lang.Enum<javax.annotation.meta.When>

CLSS public abstract interface !annotation org.netbeans.api.annotations.common.CheckForNull
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
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
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation org.netbeans.api.annotations.common.NullAllowed
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[FIELD, PARAMETER, LOCAL_VARIABLE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation org.netbeans.api.annotations.common.NullUnknown
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[FIELD, METHOD, PARAMETER, LOCAL_VARIABLE])
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

