#Signature file v4.1
#Version 1.40.0

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation

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

CLSS public abstract org.netbeans.modules.java.hints.spi.AbstractHint
 anno 0 java.lang.Deprecated()
cons public !varargs init(boolean,boolean,org.netbeans.modules.java.hints.spi.AbstractHint$HintSeverity,java.lang.String[])
innr public final static !enum HintSeverity
intf org.netbeans.modules.java.hints.spi.TreeRule
meth public abstract java.lang.String getDescription()
meth public final boolean isEnabled()
meth public final org.netbeans.modules.java.hints.spi.AbstractHint$HintSeverity getSeverity()
meth public java.util.prefs.Preferences getPreferences(java.lang.String)
meth public javax.swing.JComponent getCustomizer(java.util.prefs.Preferences)
supr java.lang.Object
hfds enableDefault,severityDefault,showInTaskListDefault,suppressBy
hcls HintAccessorImpl

CLSS public final static !enum org.netbeans.modules.java.hints.spi.AbstractHint$HintSeverity
 outer org.netbeans.modules.java.hints.spi.AbstractHint
fld public final static org.netbeans.modules.java.hints.spi.AbstractHint$HintSeverity CURRENT_LINE_WARNING
fld public final static org.netbeans.modules.java.hints.spi.AbstractHint$HintSeverity ERROR
fld public final static org.netbeans.modules.java.hints.spi.AbstractHint$HintSeverity WARNING
meth public org.netbeans.spi.editor.hints.Severity toEditorSeverity()
meth public org.netbeans.spi.editor.hints.Severity toOfficialSeverity()
meth public static org.netbeans.modules.java.hints.spi.AbstractHint$HintSeverity valueOf(java.lang.String)
meth public static org.netbeans.modules.java.hints.spi.AbstractHint$HintSeverity[] values()
supr java.lang.Enum<org.netbeans.modules.java.hints.spi.AbstractHint$HintSeverity>
hfds editorSeverity

CLSS public abstract interface org.netbeans.modules.java.hints.spi.ErrorRule<%0 extends java.lang.Object>
innr public final static Data
intf org.netbeans.modules.java.hints.spi.Rule
meth public abstract java.util.List<org.netbeans.spi.editor.hints.Fix> run(org.netbeans.api.java.source.CompilationInfo,java.lang.String,int,com.sun.source.util.TreePath,org.netbeans.modules.java.hints.spi.ErrorRule$Data<{org.netbeans.modules.java.hints.spi.ErrorRule%0}>)
meth public abstract java.util.Set<java.lang.String> getCodes()

CLSS public final static org.netbeans.modules.java.hints.spi.ErrorRule$Data<%0 extends java.lang.Object>
 outer org.netbeans.modules.java.hints.spi.ErrorRule
cons public init()
meth public void setData({org.netbeans.modules.java.hints.spi.ErrorRule$Data%0})
meth public {org.netbeans.modules.java.hints.spi.ErrorRule$Data%0} getData()
supr java.lang.Object
hfds o

CLSS public abstract interface org.netbeans.modules.java.hints.spi.Rule
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getId()
meth public abstract void cancel()

CLSS public abstract interface org.netbeans.modules.java.hints.spi.TreeRule
intf org.netbeans.modules.java.hints.spi.Rule
meth public abstract java.util.List<org.netbeans.spi.editor.hints.ErrorDescription> run(org.netbeans.api.java.source.CompilationInfo,com.sun.source.util.TreePath)
meth public abstract java.util.Set<com.sun.source.tree.Tree$Kind> getTreeKinds()

CLSS public abstract interface org.netbeans.modules.java.hints.spi.preview.PreviewEnabler
innr public abstract interface static Factory
meth public abstract void enablePreview(java.lang.String) throws java.lang.Exception
meth public boolean canChangeSourceLevel()

CLSS public abstract interface static org.netbeans.modules.java.hints.spi.preview.PreviewEnabler$Factory
 outer org.netbeans.modules.java.hints.spi.preview.PreviewEnabler
meth public abstract org.netbeans.modules.java.hints.spi.preview.PreviewEnabler enablerFor(org.openide.filesystems.FileObject)

CLSS public final org.netbeans.modules.java.hints.spi.support.FixFactory
meth public !varargs static java.util.List<org.netbeans.spi.editor.hints.Fix> createSuppressWarnings(org.netbeans.api.java.source.CompilationInfo,com.sun.source.util.TreePath,java.lang.String[])
meth public !varargs static org.netbeans.spi.editor.hints.Fix createSuppressWarningsFix(org.netbeans.api.java.source.CompilationInfo,com.sun.source.util.TreePath,java.lang.String[])
meth public final static org.netbeans.spi.editor.hints.Fix addModifiersFix(org.netbeans.api.java.source.CompilationInfo,com.sun.source.util.TreePath,java.util.Set<javax.lang.model.element.Modifier>,java.lang.String)
meth public final static org.netbeans.spi.editor.hints.Fix changeModifiersFix(org.netbeans.api.java.source.CompilationInfo,com.sun.source.util.TreePath,java.util.Set<javax.lang.model.element.Modifier>,java.util.Set<javax.lang.model.element.Modifier>,java.lang.String)
meth public final static org.netbeans.spi.editor.hints.Fix removeModifiersFix(org.netbeans.api.java.source.CompilationInfo,com.sun.source.util.TreePath,java.util.Set<javax.lang.model.element.Modifier>,java.lang.String)
meth public static boolean isSuppressWarningsFix(org.netbeans.spi.editor.hints.Fix)
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds DECLARATION

