#Signature file v4.1
#Version 1.71.0

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
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

CLSS public final org.netbeans.api.java.source.ui.DialogBinding
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.api.java.source.JavaSource bindComponentToFile(org.openide.filesystems.FileObject,int,int,javax.swing.text.JTextComponent)
 anno 0 java.lang.Deprecated()
supr java.lang.Object

CLSS public final org.netbeans.api.java.source.ui.ElementHeaders
fld public final static java.lang.String ANNOTATIONS = "%annotations%"
fld public final static java.lang.String EXTENDS = "%extends%"
fld public final static java.lang.String FLAGS = "%flags%"
fld public final static java.lang.String IMPLEMENTS = "%implements%"
fld public final static java.lang.String NAME = "%name%"
fld public final static java.lang.String PARAMETERS = "%parameters%"
fld public final static java.lang.String THROWS = "%throws%"
fld public final static java.lang.String TYPE = "%type%"
fld public final static java.lang.String TYPEPARAMETERS = "%typeparameters%"
meth public static int getDistance(java.lang.String,java.lang.String)
meth public static java.lang.String getHeader(com.sun.source.util.TreePath,org.netbeans.api.java.source.CompilationInfo,java.lang.String)
meth public static java.lang.String getHeader(javax.lang.model.element.Element,org.netbeans.api.java.source.CompilationInfo,java.lang.String)
meth public static java.util.concurrent.CompletableFuture<org.netbeans.api.lsp.StructureElement> resolveStructureElement(org.netbeans.api.java.source.CompilationInfo,javax.lang.model.element.Element,boolean)
meth public static org.netbeans.api.lsp.StructureElement convertElement(org.netbeans.api.java.source.CompilationInfo,javax.lang.model.element.Element,org.netbeans.api.java.source.ElementUtilities$ElementAcceptor,boolean)
meth public static org.netbeans.api.lsp.StructureElement toStructureElement(org.netbeans.api.java.source.CompilationInfo,javax.lang.model.element.Element,org.netbeans.api.java.source.ElementUtilities$ElementAcceptor)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.api.lsp.StructureElement$Kind javaKind2Structure(javax.lang.model.element.Element)
supr java.lang.Object

CLSS public org.netbeans.api.java.source.ui.ElementIcons
meth public static javax.swing.Icon getElementIcon(javax.lang.model.element.ElementKind,java.util.Collection<javax.lang.model.element.Modifier>)
meth public static javax.swing.Icon getModuleDirectiveIcon(javax.lang.model.element.ModuleElement$DirectiveKind)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds EXPORTS_ICON,OPENS_ICON,PROVIDES_ICON,REQUIRES_ICON,USES_ICON

CLSS public org.netbeans.api.java.source.ui.ElementJavadoc
meth public final static org.netbeans.api.java.source.ui.ElementJavadoc create(org.netbeans.api.java.source.CompilationInfo,javax.lang.model.element.Element)
meth public final static org.netbeans.api.java.source.ui.ElementJavadoc create(org.netbeans.api.java.source.CompilationInfo,javax.lang.model.element.Element,java.util.concurrent.Callable<java.lang.Boolean>)
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public java.net.URL getURL()
meth public java.util.concurrent.Future<java.lang.String> getTextAsync()
meth public javax.swing.Action getGotoSourceAction()
meth public org.netbeans.api.java.source.ui.ElementJavadoc resolveLink(java.lang.String)
supr java.lang.Object
hfds API,APINOTE_TAG,ASSOCIATE_JDOC,HTML_TAGS,IMPLNOTE_TAG,IMPLSPEC_TAG,LANGS,MARKUPTAG_MANDATORY_ATTRIBUTE,RP,cancel,className,content,cpInfo,docRoot,docURL,fileObject,goToSource,handle,imports,linkCounter,links,packageName
hcls JavaDocSnippetLinkTagFileObject,SourceLineCharterMapperToHtmlTag

CLSS public final org.netbeans.api.java.source.ui.ElementOpen
innr public final static Location
meth public !varargs static boolean open(org.netbeans.api.java.source.ClasspathInfo,org.netbeans.api.java.source.ElementHandle<? extends javax.lang.model.element.Element>,java.lang.String[])
meth public !varargs static java.util.concurrent.CompletableFuture<org.netbeans.api.java.source.ui.ElementOpen$Location> getLocation(org.netbeans.api.java.source.ClasspathInfo,org.netbeans.api.java.source.ElementHandle<? extends javax.lang.model.element.Element>,java.lang.String,java.lang.String[])
meth public static boolean open(org.netbeans.api.java.source.ClasspathInfo,javax.lang.model.element.Element)
meth public static boolean open(org.netbeans.api.java.source.ClasspathInfo,org.netbeans.api.java.source.ElementHandle<? extends javax.lang.model.element.Element>)
meth public static boolean open(org.openide.filesystems.FileObject,org.netbeans.api.java.source.ElementHandle<? extends javax.lang.model.element.Element>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static boolean open(org.openide.filesystems.FileObject,org.netbeans.api.java.source.TreePathHandle)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static java.util.concurrent.CompletableFuture<org.netbeans.api.java.source.ui.ElementOpen$Location> getLocation(org.netbeans.api.java.source.ClasspathInfo,org.netbeans.api.java.source.ElementHandle<? extends javax.lang.model.element.Element>,java.lang.String)
supr java.lang.Object
hfds AWT_TIMEOUT,NON_AWT_TIMEOUT,log
hcls FindDeclarationVisitor

CLSS public final static org.netbeans.api.java.source.ui.ElementOpen$Location
 outer org.netbeans.api.java.source.ui.ElementOpen
meth public int getEndOffset()
meth public int getStartOffset()
meth public org.openide.filesystems.FileObject getFileObject()
supr java.lang.Object
hfds endOffset,fileObject,startOffset

CLSS public org.netbeans.api.java.source.ui.ScanDialog
meth public static boolean runWhenScanFinished(java.lang.Runnable,java.lang.String)
supr java.lang.Object

CLSS public final org.netbeans.api.java.source.ui.TypeElementFinder
cons public init()
innr public abstract interface static Customizer
meth public static org.netbeans.api.java.source.ElementHandle<javax.lang.model.element.TypeElement> find(org.netbeans.api.java.source.ClasspathInfo,java.lang.String,org.netbeans.api.java.source.ui.TypeElementFinder$Customizer)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.api.java.source.ElementHandle<javax.lang.model.element.TypeElement> find(org.netbeans.api.java.source.ClasspathInfo,org.netbeans.api.java.source.ui.TypeElementFinder$Customizer)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object

CLSS public abstract interface static org.netbeans.api.java.source.ui.TypeElementFinder$Customizer
 outer org.netbeans.api.java.source.ui.TypeElementFinder
meth public abstract boolean accept(org.netbeans.api.java.source.ElementHandle<javax.lang.model.element.TypeElement>)
meth public abstract java.util.Set<org.netbeans.api.java.source.ElementHandle<javax.lang.model.element.TypeElement>> query(org.netbeans.api.java.source.ClasspathInfo,java.lang.String,org.netbeans.api.java.source.ClassIndex$NameKind,java.util.Set<org.netbeans.api.java.source.ClassIndex$SearchScope>)

