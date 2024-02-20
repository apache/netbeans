#Signature file v4.1
#Version 1.31

CLSS public abstract interface !annotation java.lang.FunctionalInterface
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

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
innr public abstract interface static OnSubmit
innr public final static Builder
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String className()
meth public abstract !hasdefault java.lang.String[] resources()
meth public abstract !hasdefault java.lang.String[] techIds()
meth public abstract java.lang.String url()

CLSS public final static org.netbeans.api.htmlui.HTMLDialog$Builder
 outer org.netbeans.api.htmlui.HTMLDialog
meth public !varargs org.netbeans.api.htmlui.HTMLDialog$Builder addResources(java.lang.String[])
meth public !varargs org.netbeans.api.htmlui.HTMLDialog$Builder addTechIds(java.lang.String[])
meth public <%0 extends java.lang.Object> {%%0} component(java.lang.Class<{%%0}>)
meth public java.lang.String showAndWait()
meth public org.netbeans.api.htmlui.HTMLDialog$Builder loadFinished(java.lang.Runnable)
meth public static org.netbeans.api.htmlui.HTMLDialog$Builder newDialog(java.lang.String)
meth public void show(org.netbeans.api.htmlui.HTMLDialog$OnSubmit)
supr java.lang.Object
hfds onPageLoad,resources,techIds,url

CLSS public abstract interface static org.netbeans.api.htmlui.HTMLDialog$OnSubmit
 outer org.netbeans.api.htmlui.HTMLDialog
 anno 0 java.lang.FunctionalInterface()
meth public abstract boolean onSubmit(java.lang.String)

CLSS public abstract interface !annotation org.netbeans.api.htmlui.OpenHTMLRegistration
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String iconBase()
meth public abstract !hasdefault java.lang.String[] techIds()
meth public abstract java.lang.String displayName()
meth public abstract java.lang.String url()

CLSS public abstract interface org.netbeans.spi.htmlui.HTMLViewerSpi<%0 extends java.lang.Object, %1 extends java.lang.Object>
innr public final static Context
meth public abstract <%0 extends java.lang.Object> {%%0} component({org.netbeans.spi.htmlui.HTMLViewerSpi%0},java.lang.Class<{%%0}>)
meth public abstract java.lang.String getId({org.netbeans.spi.htmlui.HTMLViewerSpi%0},{org.netbeans.spi.htmlui.HTMLViewerSpi%1})
meth public abstract void runLater({org.netbeans.spi.htmlui.HTMLViewerSpi%0},java.lang.Runnable)
meth public abstract void setEnabled({org.netbeans.spi.htmlui.HTMLViewerSpi%0},{org.netbeans.spi.htmlui.HTMLViewerSpi%1},boolean)
meth public abstract void setText({org.netbeans.spi.htmlui.HTMLViewerSpi%0},{org.netbeans.spi.htmlui.HTMLViewerSpi%1},java.lang.String)
meth public abstract {org.netbeans.spi.htmlui.HTMLViewerSpi%0} newView(org.netbeans.spi.htmlui.HTMLViewerSpi$Context)
meth public abstract {org.netbeans.spi.htmlui.HTMLViewerSpi%1} createButton({org.netbeans.spi.htmlui.HTMLViewerSpi%0},java.lang.String)

CLSS public final static org.netbeans.spi.htmlui.HTMLViewerSpi$Context
 outer org.netbeans.spi.htmlui.HTMLViewerSpi
meth public boolean isBlocking()
meth public boolean isDialog()
meth public boolean isWindow()
meth public boolean onSubmit(java.lang.String)
meth public java.lang.ClassLoader getClassLoader()
meth public java.lang.String[] getResources()
meth public java.lang.String[] getTechIds()
meth public java.net.URL getPage()
meth public org.openide.util.Lookup onPageLoad()
supr java.lang.Object
hfds component,lifeCycleCallback,loader,onPageLoad,onSubmit,resources,techIds,url

