#Signature file v4.1
#Version 0.36

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

CLSS public final org.netbeans.modules.php.api.documentation.PhpDocumentations
fld public final static java.lang.String CUSTOMIZER_IDENT = "Documentation"
fld public final static java.lang.String DOCUMENTATION_PATH = "PHP/Documentation"
meth public static java.util.List<org.netbeans.modules.php.spi.documentation.PhpDocumentationProvider> getDocumentations()
meth public static org.netbeans.spi.project.ui.support.ProjectCustomizer$CompositeCategoryProvider createCustomizer()
meth public static void addDocumentationsListener(org.openide.util.LookupListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static void removeDocumentationsListener(org.openide.util.LookupListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds DOCUMENTATIONS

CLSS public abstract org.netbeans.modules.php.spi.documentation.PhpDocumentationProvider
cons public init(java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
innr public abstract interface static !annotation Registration
meth public abstract void generateDocumentation(org.netbeans.modules.php.api.phpmodule.PhpModule)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public boolean isInPhpModule(org.netbeans.modules.php.api.phpmodule.PhpModule)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public final java.lang.String getDisplayName()
meth public final java.lang.String getName()
meth public org.netbeans.modules.php.spi.phpmodule.PhpModuleCustomizer createPhpModuleCustomizer(org.netbeans.modules.php.api.phpmodule.PhpModule)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public void notifyEnabled(org.netbeans.modules.php.api.phpmodule.PhpModule,boolean)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds displayName,name

CLSS public abstract interface static !annotation org.netbeans.modules.php.spi.documentation.PhpDocumentationProvider$Registration
 outer org.netbeans.modules.php.spi.documentation.PhpDocumentationProvider
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()

