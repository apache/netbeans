#Signature file v4.1
#Version 1.57.0

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

CLSS public abstract interface !annotation org.netbeans.spi.java.hints.BooleanOption
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[FIELD])
intf java.lang.annotation.Annotation
meth public abstract boolean defaultValue()
meth public abstract java.lang.String displayName()
meth public abstract java.lang.String tooltip()

CLSS public abstract interface !annotation org.netbeans.spi.java.hints.ConstraintVariableType
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String type()
meth public abstract java.lang.String variable()

CLSS public abstract interface org.netbeans.spi.java.hints.CustomizerProvider
meth public abstract javax.swing.JComponent getCustomizer(java.util.prefs.Preferences)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public org.netbeans.spi.java.hints.ErrorDescriptionFactory
meth public !varargs static org.netbeans.spi.editor.hints.ErrorDescription forName(org.netbeans.spi.java.hints.HintContext,com.sun.source.tree.Tree,java.lang.String,org.netbeans.spi.editor.hints.Fix[])
meth public !varargs static org.netbeans.spi.editor.hints.ErrorDescription forName(org.netbeans.spi.java.hints.HintContext,com.sun.source.util.TreePath,java.lang.String,org.netbeans.spi.editor.hints.Fix[])
meth public !varargs static org.netbeans.spi.editor.hints.ErrorDescription forSpan(org.netbeans.spi.java.hints.HintContext,int,int,java.lang.String,org.netbeans.spi.editor.hints.Fix[])
meth public !varargs static org.netbeans.spi.editor.hints.ErrorDescription forTree(org.netbeans.spi.java.hints.HintContext,com.sun.source.tree.Tree,java.lang.String,org.netbeans.spi.editor.hints.Fix[])
meth public !varargs static org.netbeans.spi.editor.hints.ErrorDescription forTree(org.netbeans.spi.java.hints.HintContext,com.sun.source.util.TreePath,java.lang.String,org.netbeans.spi.editor.hints.Fix[])
supr java.lang.Object
hfds DECLARATION,LOG
hcls DisableConfigure,FixImpl,InspectFix,TopLevelConfigureFix

CLSS public abstract interface !annotation org.netbeans.spi.java.hints.Hint
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
innr public final static !enum Kind
innr public final static !enum Options
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean enabled()
meth public abstract !hasdefault java.lang.Class<? extends org.netbeans.spi.java.hints.CustomizerProvider> customizerProvider()
meth public abstract !hasdefault java.lang.String id()
meth public abstract !hasdefault java.lang.String minSourceVersion()
meth public abstract !hasdefault java.lang.String[] suppressWarnings()
meth public abstract !hasdefault org.netbeans.spi.editor.hints.Severity severity()
meth public abstract !hasdefault org.netbeans.spi.java.hints.Hint$Kind hintKind()
meth public abstract !hasdefault org.netbeans.spi.java.hints.Hint$Options[] options()
meth public abstract java.lang.String category()
meth public abstract java.lang.String description()
meth public abstract java.lang.String displayName()

CLSS public final static !enum org.netbeans.spi.java.hints.Hint$Kind
 outer org.netbeans.spi.java.hints.Hint
fld public final static org.netbeans.spi.java.hints.Hint$Kind ACTION
fld public final static org.netbeans.spi.java.hints.Hint$Kind INSPECTION
meth public static org.netbeans.spi.java.hints.Hint$Kind valueOf(java.lang.String)
meth public static org.netbeans.spi.java.hints.Hint$Kind[] values()
supr java.lang.Enum<org.netbeans.spi.java.hints.Hint$Kind>

CLSS public final static !enum org.netbeans.spi.java.hints.Hint$Options
 outer org.netbeans.spi.java.hints.Hint
fld public final static org.netbeans.spi.java.hints.Hint$Options HEAVY
fld public final static org.netbeans.spi.java.hints.Hint$Options NO_BATCH
fld public final static org.netbeans.spi.java.hints.Hint$Options QUERY
meth public static org.netbeans.spi.java.hints.Hint$Options valueOf(java.lang.String)
meth public static org.netbeans.spi.java.hints.Hint$Options[] values()
supr java.lang.Enum<org.netbeans.spi.java.hints.Hint$Options>

CLSS public org.netbeans.spi.java.hints.HintContext
innr public final static !enum MessageKind
meth public boolean isBulkMode()
meth public boolean isCanceled()
meth public com.sun.source.util.TreePath getPath()
meth public int getCaretLocation()
meth public java.util.Map<java.lang.String,com.sun.source.util.TreePath> getVariables()
meth public java.util.Map<java.lang.String,java.lang.String> getVariableNames()
meth public java.util.Map<java.lang.String,java.util.Collection<? extends com.sun.source.util.TreePath>> getMultiVariables()
meth public java.util.Map<java.lang.String,javax.lang.model.type.TypeMirror> getConstraints()
meth public java.util.prefs.Preferences getPreferences()
meth public org.netbeans.api.java.source.CompilationInfo getInfo()
meth public org.netbeans.spi.editor.hints.Severity getSeverity()
meth public void reportMessage(org.netbeans.spi.java.hints.HintContext$MessageKind,java.lang.String)
supr java.lang.Object
hfds bulkMode,cancel,caret,constraints,info,messages,metadata,multiVariables,path,preferences,settings,severity,variableNames,variables

CLSS public final static !enum org.netbeans.spi.java.hints.HintContext$MessageKind
 outer org.netbeans.spi.java.hints.HintContext
fld public final static org.netbeans.spi.java.hints.HintContext$MessageKind ERROR
fld public final static org.netbeans.spi.java.hints.HintContext$MessageKind WARNING
meth public static org.netbeans.spi.java.hints.HintContext$MessageKind valueOf(java.lang.String)
meth public static org.netbeans.spi.java.hints.HintContext$MessageKind[] values()
supr java.lang.Enum<org.netbeans.spi.java.hints.HintContext$MessageKind>

CLSS public final !enum org.netbeans.spi.java.hints.HintSeverity
fld public final static org.netbeans.spi.java.hints.HintSeverity CURRENT_LINE_WARNING
fld public final static org.netbeans.spi.java.hints.HintSeverity ERROR
fld public final static org.netbeans.spi.java.hints.HintSeverity WARNING
meth public org.netbeans.spi.editor.hints.Severity toEditorSeverity()
meth public static org.netbeans.spi.java.hints.HintSeverity valueOf(java.lang.String)
meth public static org.netbeans.spi.java.hints.HintSeverity[] values()
supr java.lang.Enum<org.netbeans.spi.java.hints.HintSeverity>

CLSS public abstract interface !annotation org.netbeans.spi.java.hints.IntegerOption
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int defaultValue()
meth public abstract !hasdefault int maxValue()
meth public abstract !hasdefault int minValue()
meth public abstract !hasdefault int step()
meth public abstract !hasdefault java.lang.String tooltip()
meth public abstract java.lang.String displayName()

CLSS public abstract org.netbeans.spi.java.hints.JavaFix
cons protected init(org.netbeans.api.java.source.CompilationInfo,com.sun.source.util.TreePath)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
cons protected init(org.netbeans.api.java.source.CompilationInfo,com.sun.source.util.TreePath,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
cons protected init(org.netbeans.api.java.source.TreePathHandle)
 anno 1 org.netbeans.api.annotations.common.NonNull()
cons protected init(org.netbeans.api.java.source.TreePathHandle,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
innr public final static TransformationContext
meth protected abstract java.lang.String getText()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth protected abstract void performRewrite(org.netbeans.spi.java.hints.JavaFix$TransformationContext) throws java.lang.Exception
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public final org.netbeans.spi.editor.hints.Fix toEditorFix()
supr java.lang.Object
hfds handle,modResult2ChangeInfo,options,sortText

CLSS public final static org.netbeans.spi.java.hints.JavaFix$TransformationContext
 outer org.netbeans.spi.java.hints.JavaFix
meth public com.sun.source.util.TreePath getPath()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.io.InputStream getResourceContent(org.openide.filesystems.FileObject) throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public java.io.OutputStream getResourceOutput(org.openide.filesystems.FileObject) throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.java.source.WorkingCopy getWorkingCopy()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds canShowUI,fileChanges,path,resourceContentChanges,workingCopy

CLSS public org.netbeans.spi.java.hints.JavaFixUtilities
cons public init()
meth public static boolean isPrimary(com.sun.source.tree.Tree)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static boolean requiresParenthesis(com.sun.source.tree.Tree,com.sun.source.tree.Tree,com.sun.source.tree.Tree)
meth public static org.netbeans.spi.editor.hints.Fix removeFromParent(org.netbeans.spi.java.hints.HintContext,java.lang.String,com.sun.source.util.TreePath)
meth public static org.netbeans.spi.editor.hints.Fix rewriteFix(org.netbeans.spi.java.hints.HintContext,java.lang.String,com.sun.source.util.TreePath,java.lang.String)
meth public static org.netbeans.spi.editor.hints.Fix safelyRemoveFromParent(org.netbeans.spi.java.hints.HintContext,java.lang.String,com.sun.source.util.TreePath)
supr java.lang.Object
hfds NUMBER_LITERAL_KINDS,OPERATOR_PRIORITIES,SPEC_VERSION
hcls IK,JavaFixRealImpl,MoveFile,RemoveFromParent,ReplaceParameters

CLSS public org.netbeans.spi.java.hints.MatcherUtilities
cons public init()
meth public static boolean matches(org.netbeans.spi.java.hints.HintContext,com.sun.source.util.TreePath,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public static boolean matches(org.netbeans.spi.java.hints.HintContext,com.sun.source.util.TreePath,java.lang.String,boolean)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public static boolean matches(org.netbeans.spi.java.hints.HintContext,com.sun.source.util.TreePath,java.lang.String,java.util.Map<java.lang.String,com.sun.source.util.TreePath>,java.util.Map<java.lang.String,java.util.Collection<? extends com.sun.source.util.TreePath>>,java.util.Map<java.lang.String,java.lang.String>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public static boolean matches(org.netbeans.spi.java.hints.HintContext,com.sun.source.util.TreePath,java.lang.String,java.util.Map<java.lang.String,com.sun.source.util.TreePath>,java.util.Map<java.lang.String,java.util.Collection<? extends com.sun.source.util.TreePath>>,java.util.Map<java.lang.String,java.lang.String>,java.util.Map<java.lang.String,javax.lang.model.type.TypeMirror>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public static boolean matches(org.netbeans.spi.java.hints.HintContext,java.util.Collection<? extends com.sun.source.util.TreePath>,java.lang.String,java.util.Map<java.lang.String,com.sun.source.util.TreePath>,java.util.Map<java.lang.String,java.util.Collection<? extends com.sun.source.util.TreePath>>,java.util.Map<java.lang.String,java.lang.String>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public abstract interface !annotation org.netbeans.spi.java.hints.TriggerOptions
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
fld public final static java.lang.String PROCESS_GUARDED = "processGuarded"
intf java.lang.annotation.Annotation
meth public abstract java.lang.String[] value()

CLSS public abstract interface !annotation org.netbeans.spi.java.hints.TriggerPattern
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault org.netbeans.spi.java.hints.ConstraintVariableType[] constraints()
meth public abstract java.lang.String value()

CLSS public abstract interface !annotation org.netbeans.spi.java.hints.TriggerPatterns
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract org.netbeans.spi.java.hints.TriggerPattern[] value()

CLSS public abstract interface !annotation org.netbeans.spi.java.hints.TriggerTreeKind
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract com.sun.source.tree.Tree$Kind[] value()

CLSS public abstract interface !annotation org.netbeans.spi.java.hints.UseOptions
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String[] value()

CLSS public final org.netbeans.spi.java.hints.support.FixFactory
meth public final static org.netbeans.spi.editor.hints.Fix addModifiersFix(org.netbeans.api.java.source.CompilationInfo,com.sun.source.util.TreePath,java.util.Set<javax.lang.model.element.Modifier>,java.lang.String)
meth public final static org.netbeans.spi.editor.hints.Fix changeModifiersFix(org.netbeans.api.java.source.CompilationInfo,com.sun.source.util.TreePath,java.util.Set<javax.lang.model.element.Modifier>,java.util.Set<javax.lang.model.element.Modifier>,java.lang.String)
meth public final static org.netbeans.spi.editor.hints.Fix removeModifiersFix(org.netbeans.api.java.source.CompilationInfo,com.sun.source.util.TreePath,java.util.Set<javax.lang.model.element.Modifier>,java.lang.String)
supr java.lang.Object
hcls ChangeModifiersFixImpl

CLSS public final org.netbeans.spi.java.hints.support.TransformationSupport
innr public abstract interface static Transformer
meth public java.util.Collection<? extends org.netbeans.api.java.source.ModificationResult> processAllProjects()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.spi.java.hints.support.TransformationSupport setCancel(java.util.concurrent.atomic.AtomicBoolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.java.hints.support.TransformationSupport create(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.java.hints.support.TransformationSupport create(java.lang.String,org.netbeans.spi.java.hints.support.TransformationSupport$Transformer)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public void transformTreePath(org.netbeans.api.java.source.WorkingCopy,com.sun.source.util.TreePath)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds cancel,jackpotPattern,transformer

CLSS public abstract interface static org.netbeans.spi.java.hints.support.TransformationSupport$Transformer
 outer org.netbeans.spi.java.hints.support.TransformationSupport
meth public abstract void transform(org.netbeans.api.java.source.WorkingCopy,org.netbeans.api.java.source.matching.Occurrence)

CLSS public abstract interface org.netbeans.spi.java.hints.unused.UsedDetector
innr public abstract interface static Factory
meth public abstract boolean isUsed(javax.lang.model.element.Element,com.sun.source.util.TreePath)

CLSS public abstract interface static org.netbeans.spi.java.hints.unused.UsedDetector$Factory
 outer org.netbeans.spi.java.hints.unused.UsedDetector
meth public abstract org.netbeans.spi.java.hints.unused.UsedDetector create(org.netbeans.api.java.source.CompilationInfo)

