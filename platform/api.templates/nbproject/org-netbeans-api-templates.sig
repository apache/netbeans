#Signature file v4.1
#Version 1.31

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

CLSS public final org.netbeans.api.templates.CreateDescriptor
fld public final static java.lang.String FREE_FILE_EXTENSION = "freeFileExtension"
fld public final static java.lang.String PREFORMATTED_TEMPLATE = "org-netbeans-modules-java-preformattedSource"
meth public <%0 extends java.lang.Object> {%%0} getValue(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public boolean hasFreeExtension()
meth public boolean isPreformatted()
meth public java.lang.String getName()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getProposedName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.util.Locale getLocale()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.util.Map<java.lang.String,java.lang.Object> getParameters()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.openide.filesystems.FileObject getTarget()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.openide.filesystems.FileObject getTemplate()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.openide.util.Lookup getLookup()
supr java.lang.Object
hfds contextLookup,freeExtension,locale,name,parameters,preformatted,proposedName,target,template

CLSS public abstract interface org.netbeans.api.templates.CreateFromTemplateAttributes
meth public abstract java.util.Map<java.lang.String,?> attributesFor(org.netbeans.api.templates.CreateDescriptor)

CLSS public abstract interface org.netbeans.api.templates.CreateFromTemplateDecorator
meth public abstract boolean accept(org.netbeans.api.templates.CreateDescriptor)
meth public abstract boolean isAfterCreation()
meth public abstract boolean isBeforeCreation()
meth public abstract java.util.List<org.openide.filesystems.FileObject> decorate(org.netbeans.api.templates.CreateDescriptor,java.util.List<org.openide.filesystems.FileObject>) throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract org.netbeans.api.templates.CreateFromTemplateHandler
cons public init()
meth protected abstract boolean accept(org.netbeans.api.templates.CreateDescriptor)
meth protected abstract java.util.List<org.openide.filesystems.FileObject> createFromTemplate(org.netbeans.api.templates.CreateDescriptor) throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth protected static java.util.List<org.openide.filesystems.FileObject> defaultCopyContents(org.netbeans.api.templates.CreateDescriptor,org.openide.filesystems.FileObject,org.openide.filesystems.FileObject) throws java.io.IOException
meth public static void copyAttributesFromTemplate(org.netbeans.api.templates.CreateFromTemplateHandler,org.openide.filesystems.FileObject,org.openide.filesystems.FileObject) throws java.io.IOException
supr java.lang.Object
hfds ATTR_TEMPLATE_PREFIX,PROP_TEMPLATE

CLSS public final org.netbeans.api.templates.FileBuilder
cons public init(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
fld public final static java.lang.String ATTR_TEMPLATE_HANDLER = "template.createTemplateHandler"
fld public final static java.lang.String ATTR_TEMPLATE_MERGE_FOLDERS = "template.mergeFolders"
fld public final static java.lang.String ATTR_TEMPLATE_OPEN_FILE = "template.openFile"
innr public final static !enum Mode
meth public java.util.List<org.openide.filesystems.FileObject> build() throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.api.templates.CreateDescriptor createDescriptor(boolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.templates.FileBuilder defaultMode(org.netbeans.api.templates.FileBuilder$Mode)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.templates.FileBuilder name(java.lang.String)
meth public org.netbeans.api.templates.FileBuilder param(java.lang.String,java.lang.Object)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.templates.FileBuilder useFormat(java.text.Format)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.templates.FileBuilder useLocale(java.util.Locale)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.templates.FileBuilder useLookup(org.openide.util.Lookup)
meth public org.netbeans.api.templates.FileBuilder withParameters(java.util.Map<java.lang.String,?>)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.api.templates.FileBuilder fromDescriptor(org.netbeans.api.templates.CreateDescriptor)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.openide.filesystems.FileObject createFromTemplate(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,java.lang.String,java.util.Map<java.lang.String,?>,org.netbeans.api.templates.FileBuilder$Mode) throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds defaultMode,descriptor,format

CLSS public final static !enum org.netbeans.api.templates.FileBuilder$Mode
 outer org.netbeans.api.templates.FileBuilder
fld public final static org.netbeans.api.templates.FileBuilder$Mode COPY
fld public final static org.netbeans.api.templates.FileBuilder$Mode FAIL
fld public final static org.netbeans.api.templates.FileBuilder$Mode FORMAT
meth public static org.netbeans.api.templates.FileBuilder$Mode valueOf(java.lang.String)
meth public static org.netbeans.api.templates.FileBuilder$Mode[] values()
supr java.lang.Enum<org.netbeans.api.templates.FileBuilder$Mode>

CLSS public abstract interface !annotation org.netbeans.api.templates.TemplateRegistration
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD, PACKAGE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean requireProject()
meth public abstract !hasdefault int position()
meth public abstract !hasdefault java.lang.Class<? extends org.netbeans.api.templates.CreateFromTemplateHandler> createHandlerClass()
meth public abstract !hasdefault java.lang.String description()
meth public abstract !hasdefault java.lang.String displayName()
meth public abstract !hasdefault java.lang.String iconBase()
meth public abstract !hasdefault java.lang.String id()
meth public abstract !hasdefault java.lang.String page()
meth public abstract !hasdefault java.lang.String scriptEngine()
meth public abstract !hasdefault java.lang.String targetName()
meth public abstract !hasdefault java.lang.String[] category()
meth public abstract !hasdefault java.lang.String[] content()
meth public abstract !hasdefault java.lang.String[] techIds()
meth public abstract java.lang.String folder()

CLSS public abstract interface !annotation org.netbeans.api.templates.TemplateRegistrations
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD, PACKAGE])
intf java.lang.annotation.Annotation
meth public abstract org.netbeans.api.templates.TemplateRegistration[] value()

