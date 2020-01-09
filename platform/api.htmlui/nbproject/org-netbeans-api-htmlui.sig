#Signature file v4.1
#Version 1.13

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

CLSS public abstract interface !annotation org.netbeans.api.htmlui.HTMLComponent
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String className()
meth public abstract !hasdefault java.lang.String[] techIds()
meth public abstract java.lang.Class<?> type()
meth public abstract java.lang.String url()

CLSS public abstract interface !annotation org.netbeans.api.htmlui.HTMLDialog
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
innr public final static Builder
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String className()
meth public abstract !hasdefault java.lang.String[] techIds()
meth public abstract java.lang.String url()

CLSS public final static org.netbeans.api.htmlui.HTMLDialog$Builder
 outer org.netbeans.api.htmlui.HTMLDialog
meth public !varargs org.netbeans.api.htmlui.HTMLDialog$Builder addTechIds(java.lang.String[])
meth public <%0 extends java.lang.Object> {%%0} component(java.lang.Class<{%%0}>)
meth public java.lang.String showAndWait()
meth public org.netbeans.api.htmlui.HTMLDialog$Builder loadFinished(java.lang.Runnable)
meth public static org.netbeans.api.htmlui.HTMLDialog$Builder newDialog(java.lang.String)
supr java.lang.Object
hfds impl

CLSS public abstract interface !annotation org.netbeans.api.htmlui.OpenHTMLRegistration
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String iconBase()
meth public abstract !hasdefault java.lang.String[] techIds()
meth public abstract java.lang.String displayName()
meth public abstract java.lang.String url()

