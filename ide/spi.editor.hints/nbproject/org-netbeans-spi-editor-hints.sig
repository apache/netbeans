#Signature file v4.1
#Version 1.65.0

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

CLSS public final org.netbeans.spi.editor.hints.ChangeInfo
cons public init()
cons public init(javax.swing.text.Position,javax.swing.text.Position)
cons public init(org.openide.filesystems.FileObject,javax.swing.text.Position,javax.swing.text.Position)
innr public abstract interface static Change
meth public final int size()
meth public final org.netbeans.spi.editor.hints.ChangeInfo$Change get(int)
meth public final void add(org.openide.filesystems.FileObject,javax.swing.text.Position,javax.swing.text.Position)
meth public java.lang.String toString()
supr java.lang.Object
hfds changes
hcls ChangeImpl

CLSS public abstract interface static org.netbeans.spi.editor.hints.ChangeInfo$Change
 outer org.netbeans.spi.editor.hints.ChangeInfo
meth public abstract javax.swing.text.Position getEnd()
meth public abstract javax.swing.text.Position getStart()
meth public abstract org.openide.filesystems.FileObject getFileObject()

CLSS public final org.netbeans.spi.editor.hints.Context
meth public boolean isCanceled()
meth public int getPosition()
meth public java.util.concurrent.atomic.AtomicBoolean getCancel()
supr java.lang.Object
hfds cancel,position
hcls ContextAccessorImpl

CLSS public abstract interface org.netbeans.spi.editor.hints.EnhancedFix
intf org.netbeans.spi.editor.hints.Fix
meth public abstract java.lang.CharSequence getSortText()

CLSS public final org.netbeans.spi.editor.hints.ErrorDescription
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.CharSequence getDetails()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getCustomType()
meth public java.lang.String getDescription()
meth public java.lang.String getId()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String toString()
meth public java.util.ArrayList<org.openide.text.PositionBounds> getRangeTail()
meth public org.netbeans.spi.editor.hints.LazyFixList getFixes()
meth public org.netbeans.spi.editor.hints.Severity getSeverity()
meth public org.openide.filesystems.FileObject getFile()
meth public org.openide.text.PositionBounds getRange()
supr java.lang.Object
hfds customType,description,details,file,fixes,id,severity,span,spanTail

CLSS public org.netbeans.spi.editor.hints.ErrorDescriptionFactory
meth public static org.netbeans.spi.editor.hints.ErrorDescription createErrorDescription(java.lang.String,org.netbeans.spi.editor.hints.Severity,java.lang.String,java.lang.CharSequence,org.netbeans.spi.editor.hints.LazyFixList,javax.swing.text.Document,int)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.editor.hints.ErrorDescription createErrorDescription(java.lang.String,org.netbeans.spi.editor.hints.Severity,java.lang.String,java.lang.CharSequence,org.netbeans.spi.editor.hints.LazyFixList,javax.swing.text.Document,javax.swing.text.Position,javax.swing.text.Position)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NonNull()
 anno 7 org.netbeans.api.annotations.common.NonNull()
 anno 8 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.editor.hints.ErrorDescription createErrorDescription(java.lang.String,org.netbeans.spi.editor.hints.Severity,java.lang.String,java.lang.CharSequence,org.netbeans.spi.editor.hints.LazyFixList,org.openide.filesystems.FileObject,int,int)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.editor.hints.ErrorDescription createErrorDescription(java.lang.String,org.netbeans.spi.editor.hints.Severity,java.lang.String,java.lang.CharSequence,org.netbeans.spi.editor.hints.LazyFixList,org.openide.filesystems.FileObject,org.openide.text.PositionBounds)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NonNull()
 anno 7 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.editor.hints.ErrorDescription createErrorDescription(java.lang.String,org.netbeans.spi.editor.hints.Severity,java.lang.String,java.lang.String,java.lang.CharSequence,java.util.List<org.netbeans.spi.editor.hints.Fix>,javax.swing.text.Document,int[],int[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
 anno 6 org.netbeans.api.annotations.common.NonNull()
 anno 7 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.editor.hints.ErrorDescription createErrorDescription(java.lang.String,org.netbeans.spi.editor.hints.Severity,java.lang.String,java.lang.String,java.lang.CharSequence,java.util.List<org.netbeans.spi.editor.hints.Fix>,javax.swing.text.Document,javax.swing.text.Position,javax.swing.text.Position)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
 anno 6 org.netbeans.api.annotations.common.NonNull()
 anno 7 org.netbeans.api.annotations.common.NonNull()
 anno 8 org.netbeans.api.annotations.common.NonNull()
 anno 9 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.editor.hints.ErrorDescription createErrorDescription(java.lang.String,org.netbeans.spi.editor.hints.Severity,java.lang.String,java.lang.String,java.lang.CharSequence,org.netbeans.spi.editor.hints.LazyFixList,org.openide.filesystems.FileObject,int[],int[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
 anno 6 org.netbeans.api.annotations.common.NonNull()
 anno 7 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.editor.hints.ErrorDescription createErrorDescription(org.netbeans.spi.editor.hints.Severity,java.lang.String,java.util.List<org.netbeans.spi.editor.hints.Fix>,javax.swing.text.Document,int)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.editor.hints.ErrorDescription createErrorDescription(org.netbeans.spi.editor.hints.Severity,java.lang.String,java.util.List<org.netbeans.spi.editor.hints.Fix>,javax.swing.text.Document,javax.swing.text.Position,javax.swing.text.Position)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.editor.hints.ErrorDescription createErrorDescription(org.netbeans.spi.editor.hints.Severity,java.lang.String,java.util.List<org.netbeans.spi.editor.hints.Fix>,org.openide.filesystems.FileObject,int,int)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.editor.hints.ErrorDescription createErrorDescription(org.netbeans.spi.editor.hints.Severity,java.lang.String,javax.swing.text.Document,int)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.editor.hints.ErrorDescription createErrorDescription(org.netbeans.spi.editor.hints.Severity,java.lang.String,javax.swing.text.Document,javax.swing.text.Position,javax.swing.text.Position)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.editor.hints.ErrorDescription createErrorDescription(org.netbeans.spi.editor.hints.Severity,java.lang.String,org.netbeans.spi.editor.hints.LazyFixList,javax.swing.text.Document,int)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.editor.hints.ErrorDescription createErrorDescription(org.netbeans.spi.editor.hints.Severity,java.lang.String,org.netbeans.spi.editor.hints.LazyFixList,javax.swing.text.Document,javax.swing.text.Position,javax.swing.text.Position)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.editor.hints.ErrorDescription createErrorDescription(org.netbeans.spi.editor.hints.Severity,java.lang.String,org.netbeans.spi.editor.hints.LazyFixList,org.openide.filesystems.FileObject,int,int)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.editor.hints.ErrorDescription createErrorDescription(org.netbeans.spi.editor.hints.Severity,java.lang.String,org.openide.filesystems.FileObject,int,int)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.editor.hints.Fix attachSubfixes(org.netbeans.spi.editor.hints.Fix,java.lang.Iterable<? extends org.netbeans.spi.editor.hints.Fix>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.editor.hints.LazyFixList lazyListForDelegates(java.util.List<org.netbeans.spi.editor.hints.LazyFixList>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.editor.hints.LazyFixList lazyListForFixes(java.util.List<org.netbeans.spi.editor.hints.Fix>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.editor.hints.Fix
meth public abstract java.lang.String getText()
meth public abstract org.netbeans.spi.editor.hints.ChangeInfo implement() throws java.lang.Exception

CLSS public final org.netbeans.spi.editor.hints.HintsController
meth public static void setErrors(javax.swing.text.Document,java.lang.String,java.util.Collection<? extends org.netbeans.spi.editor.hints.ErrorDescription>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public static void setErrors(org.openide.filesystems.FileObject,java.lang.String,java.util.Collection<? extends org.netbeans.spi.editor.hints.ErrorDescription>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds WORKER

CLSS public abstract interface org.netbeans.spi.editor.hints.LazyFixList
fld public final static java.lang.String PROP_COMPUTED = "computed"
fld public final static java.lang.String PROP_FIXES = "fixes"
meth public abstract boolean isComputed()
meth public abstract boolean probablyContainsFixes()
meth public abstract java.util.List<org.netbeans.spi.editor.hints.Fix> getFixes()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public abstract interface org.netbeans.spi.editor.hints.PositionRefresher
meth public abstract java.util.Map<java.lang.String,java.util.List<org.netbeans.spi.editor.hints.ErrorDescription>> getErrorDescriptionsAt(org.netbeans.spi.editor.hints.Context,javax.swing.text.Document)

CLSS public final !enum org.netbeans.spi.editor.hints.Severity
fld public final static org.netbeans.spi.editor.hints.Severity ERROR
fld public final static org.netbeans.spi.editor.hints.Severity HINT
fld public final static org.netbeans.spi.editor.hints.Severity VERIFIER
fld public final static org.netbeans.spi.editor.hints.Severity WARNING
meth public static org.netbeans.spi.editor.hints.Severity valueOf(java.lang.String)
meth public static org.netbeans.spi.editor.hints.Severity[] values()
supr java.lang.Enum<org.netbeans.spi.editor.hints.Severity>
hfds displayName

CLSS public org.netbeans.spi.editor.hints.settings.FileHintPreferences
cons public init()
innr public abstract interface static GlobalHintPreferencesProvider
meth public static java.util.prefs.Preferences getFilePreferences(org.openide.filesystems.FileObject,java.lang.String)
meth public static void addChangeListener(javax.swing.event.ChangeListener)
meth public static void fireChange()
meth public static void openFilePreferences(org.openide.filesystems.FileObject,java.lang.String,java.lang.String)
meth public static void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
hfds FIRE_WORKER,cs
hcls WrapperPreferences

CLSS public abstract interface static org.netbeans.spi.editor.hints.settings.FileHintPreferences$GlobalHintPreferencesProvider
 outer org.netbeans.spi.editor.hints.settings.FileHintPreferences
meth public abstract java.util.prefs.Preferences getGlobalPreferences()

