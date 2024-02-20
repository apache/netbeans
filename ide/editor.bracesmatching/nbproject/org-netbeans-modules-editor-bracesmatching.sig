#Signature file v4.1
#Version 1.62.0

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

CLSS public final org.netbeans.spi.editor.bracesmatching.BraceContext
meth public javax.swing.text.Position getEnd()
meth public javax.swing.text.Position getStart()
meth public org.netbeans.spi.editor.bracesmatching.BraceContext createRelated(javax.swing.text.Position,javax.swing.text.Position)
meth public org.netbeans.spi.editor.bracesmatching.BraceContext getRelated()
meth public static org.netbeans.spi.editor.bracesmatching.BraceContext create(javax.swing.text.Position,javax.swing.text.Position)
supr java.lang.Object
hfds end,related,start

CLSS public abstract interface org.netbeans.spi.editor.bracesmatching.BracesMatcher
innr public abstract interface static ContextLocator
meth public abstract int[] findMatches() throws java.lang.InterruptedException,javax.swing.text.BadLocationException
meth public abstract int[] findOrigin() throws java.lang.InterruptedException,javax.swing.text.BadLocationException

CLSS public abstract interface static org.netbeans.spi.editor.bracesmatching.BracesMatcher$ContextLocator
 outer org.netbeans.spi.editor.bracesmatching.BracesMatcher
meth public abstract org.netbeans.spi.editor.bracesmatching.BraceContext findContext(int)

CLSS public abstract interface org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory
 anno 0 org.netbeans.spi.editor.mimelookup.MimeLocation(java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass=class org.netbeans.spi.editor.mimelookup.InstanceProvider, java.lang.String subfolderName="BracesMatchers")
meth public abstract org.netbeans.spi.editor.bracesmatching.BracesMatcher createMatcher(org.netbeans.spi.editor.bracesmatching.MatcherContext)

CLSS public final org.netbeans.spi.editor.bracesmatching.MatcherContext
meth public boolean isSearchingBackward()
meth public int getLimitOffset()
meth public int getSearchLookahead()
meth public int getSearchOffset()
meth public javax.swing.text.Document getDocument()
meth public static boolean isTaskCanceled()
supr java.lang.Object
hfds backward,document,lookahead,offset
hcls SpiAccessorImpl

CLSS public final org.netbeans.spi.editor.bracesmatching.support.BracesMatcherSupport
meth public !varargs static int[] findChar(javax.swing.text.Document,int,int,char[]) throws javax.swing.text.BadLocationException
meth public !varargs static org.netbeans.spi.editor.bracesmatching.BracesMatcher characterMatcher(org.netbeans.spi.editor.bracesmatching.MatcherContext,int,int,char[])
meth public static int matchChar(javax.swing.text.Document,int,int,char,char) throws javax.swing.text.BadLocationException
meth public static org.netbeans.spi.editor.bracesmatching.BracesMatcher defaultMatcher(org.netbeans.spi.editor.bracesmatching.MatcherContext,int,int)
supr java.lang.Object
hfds DEFAULT_CHARS

CLSS public abstract interface !annotation org.netbeans.spi.editor.mimelookup.MimeLocation
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass()
meth public abstract java.lang.String subfolderName()

