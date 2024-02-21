#Signature file v4.1
#Version 0.41

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

CLSS public final org.netbeans.modules.php.api.annotation.PhpAnnotations
fld public final static java.lang.String ANNOTATIONS_COMPLETION_TAG_PROVIDERS_PATH = "PHP/Annotations"
fld public final static java.lang.String ANNOTATIONS_LINE_PARSERS_PATH = "PHP/Annotations/Line/Parsers"
meth public static java.util.List<org.netbeans.modules.php.spi.annotation.AnnotationCompletionTagProvider> getCompletionTagProviders()
meth public static java.util.List<org.netbeans.modules.php.spi.annotation.AnnotationLineParser> getLineParsers()
meth public static void addCompletionTagProvidersListener(org.openide.util.LookupListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static void addLineParsersListener(org.openide.util.LookupListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static void removeCompletionTagProvidersListener(org.openide.util.LookupListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static void removeLineParsersListener(org.openide.util.LookupListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds COMPLETION_TAG_PROVIDERS,LINE_PARSERS

CLSS public org.netbeans.modules.php.api.annotation.util.AnnotationUtils
meth public static boolean isTypeAnnotation(java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static java.util.Map<org.netbeans.modules.csl.api.OffsetRange,java.lang.String> extractInlineAnnotations(java.lang.String,java.util.Set<java.lang.String>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static java.util.Map<org.netbeans.modules.csl.api.OffsetRange,java.lang.String> extractTypesFromParameters(java.lang.String,java.util.Set<java.lang.String>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds INLINE_TYPE_PATTERN,PARAM_TYPE_PATTERN

CLSS public org.netbeans.modules.php.spi.annotation.AnnotationCompletionTag
cons public init(java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
cons public init(java.lang.String,java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public boolean equals(java.lang.Object)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public final java.lang.String getDocumentation()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final java.lang.String getInsertTemplate()
meth public final java.lang.String getName()
meth public int hashCode()
meth public void formatParameters(org.netbeans.modules.csl.api.HtmlFormatter)
supr java.lang.Object
hfds documentation,insertTemplate,name

CLSS public abstract org.netbeans.modules.php.spi.annotation.AnnotationCompletionTagProvider
cons public init(java.lang.String,java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
innr public abstract interface static !annotation Registration
meth public abstract java.util.List<org.netbeans.modules.php.spi.annotation.AnnotationCompletionTag> getFieldAnnotations()
meth public abstract java.util.List<org.netbeans.modules.php.spi.annotation.AnnotationCompletionTag> getFunctionAnnotations()
meth public abstract java.util.List<org.netbeans.modules.php.spi.annotation.AnnotationCompletionTag> getMethodAnnotations()
meth public abstract java.util.List<org.netbeans.modules.php.spi.annotation.AnnotationCompletionTag> getTypeAnnotations()
meth public final java.lang.String getDescription()
meth public final java.lang.String getIdentifier()
meth public final java.lang.String getName()
meth public java.util.List<org.netbeans.modules.php.spi.annotation.AnnotationCompletionTag> getAnnotations()
supr java.lang.Object
hfds description,identifier,name

CLSS public abstract interface static !annotation org.netbeans.modules.php.spi.annotation.AnnotationCompletionTagProvider$Registration
 outer org.netbeans.modules.php.spi.annotation.AnnotationCompletionTagProvider
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()

CLSS public abstract interface org.netbeans.modules.php.spi.annotation.AnnotationLineParser
innr public abstract interface static !annotation Registration
meth public abstract org.netbeans.modules.php.spi.annotation.AnnotationParsedLine parse(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface static !annotation org.netbeans.modules.php.spi.annotation.AnnotationLineParser$Registration
 outer org.netbeans.modules.php.spi.annotation.AnnotationLineParser
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()

CLSS public abstract interface org.netbeans.modules.php.spi.annotation.AnnotationParsedLine
innr public final static ParsedLine
meth public abstract boolean startsWithAnnotation()
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getName()
meth public abstract java.util.Map<org.netbeans.modules.csl.api.OffsetRange,java.lang.String> getTypes()

CLSS public final static org.netbeans.modules.php.spi.annotation.AnnotationParsedLine$ParsedLine
 outer org.netbeans.modules.php.spi.annotation.AnnotationParsedLine
cons public init(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
cons public init(java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
cons public init(java.lang.String,java.util.Map<org.netbeans.modules.csl.api.OffsetRange,java.lang.String>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
cons public init(java.lang.String,java.util.Map<org.netbeans.modules.csl.api.OffsetRange,java.lang.String>,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
cons public init(java.lang.String,java.util.Map<org.netbeans.modules.csl.api.OffsetRange,java.lang.String>,java.lang.String,boolean)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
intf org.netbeans.modules.php.spi.annotation.AnnotationParsedLine
meth public boolean equals(java.lang.Object)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public boolean startsWithAnnotation()
meth public int hashCode()
meth public java.lang.String getDescription()
meth public java.lang.String getName()
meth public java.util.Map<org.netbeans.modules.csl.api.OffsetRange,java.lang.String> getTypes()
supr java.lang.Object
hfds description,name,startsWithAnnotation,types

