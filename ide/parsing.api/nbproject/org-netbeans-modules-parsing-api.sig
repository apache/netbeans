#Signature file v4.1
#Version 9.30.0

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

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable

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

CLSS public java.lang.Throwable
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
intf java.io.Serializable
meth public final java.lang.Throwable[] getSuppressed()
meth public final void addSuppressed(java.lang.Throwable)
meth public java.lang.StackTraceElement[] getStackTrace()
meth public java.lang.String getLocalizedMessage()
meth public java.lang.String getMessage()
meth public java.lang.String toString()
meth public java.lang.Throwable fillInStackTrace()
meth public java.lang.Throwable getCause()
meth public java.lang.Throwable initCause(java.lang.Throwable)
meth public void printStackTrace()
meth public void printStackTrace(java.io.PrintStream)
meth public void printStackTrace(java.io.PrintWriter)
meth public void setStackTrace(java.lang.StackTraceElement[])
supr java.lang.Object

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

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final org.netbeans.modules.parsing.api.Embedding
meth public final boolean containsOriginalOffset(int)
meth public final java.lang.String getMimeType()
meth public final org.netbeans.modules.parsing.api.Snapshot getSnapshot()
meth public java.lang.String toString()
meth public static org.netbeans.modules.parsing.api.Embedding create(java.util.List<org.netbeans.modules.parsing.api.Embedding>)
supr java.lang.Object
hfds TMS_VCLV,mimePath,snapshot

CLSS public final org.netbeans.modules.parsing.api.ParserManager
meth public static boolean canBeParsed(java.lang.String)
meth public static boolean isParsing()
meth public static java.util.concurrent.Future<java.lang.Void> parseWhenScanFinished(java.lang.String,org.netbeans.modules.parsing.api.UserTask) throws org.netbeans.modules.parsing.spi.ParseException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static java.util.concurrent.Future<java.lang.Void> parseWhenScanFinished(java.util.Collection<org.netbeans.modules.parsing.api.Source>,org.netbeans.modules.parsing.api.UserTask) throws org.netbeans.modules.parsing.spi.ParseException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static void parse(java.lang.String,org.netbeans.modules.parsing.api.UserTask) throws org.netbeans.modules.parsing.spi.ParseException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static void parse(java.util.Collection<org.netbeans.modules.parsing.api.Source>,org.netbeans.modules.parsing.api.UserTask) throws org.netbeans.modules.parsing.spi.ParseException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds cachedParsers
hcls LazySnapshots,MimeTaskAction,MultiUserTaskAction,UserTaskAction

CLSS public final org.netbeans.modules.parsing.api.ResultIterator
meth public java.lang.Iterable<org.netbeans.modules.parsing.api.Embedding> getEmbeddings()
meth public org.netbeans.modules.parsing.api.ResultIterator getResultIterator(org.netbeans.modules.parsing.api.Embedding)
meth public org.netbeans.modules.parsing.api.Snapshot getSnapshot()
meth public org.netbeans.modules.parsing.spi.Parser$Result getParserResult() throws org.netbeans.modules.parsing.spi.ParseException
meth public org.netbeans.modules.parsing.spi.Parser$Result getParserResult(int) throws org.netbeans.modules.parsing.spi.ParseException
supr java.lang.Object
hfds children,embeddingToResultIterator,parser,result,sourceCache,task
hcls MyAccessor

CLSS public final org.netbeans.modules.parsing.api.Snapshot
meth public int getEmbeddedOffset(int)
meth public int getOriginalOffset(int)
meth public java.lang.CharSequence getText()
meth public java.lang.String getMimeType()
meth public java.lang.String toString()
meth public org.netbeans.api.editor.mimelookup.MimePath getMimePath()
meth public org.netbeans.api.lexer.TokenHierarchy<?> getTokenHierarchy()
meth public org.netbeans.modules.parsing.api.Embedding create(int,int,java.lang.String)
meth public org.netbeans.modules.parsing.api.Embedding create(java.lang.CharSequence,java.lang.String)
meth public org.netbeans.modules.parsing.api.Source getSource()
supr java.lang.Object
hfds LOG,currentToOriginal,lineStartOffsets,mimePath,originalToCurrent,source,text,tokenHierarchy
hcls CharSequenceView

CLSS public final org.netbeans.modules.parsing.api.Source
intf org.openide.util.Lookup$Provider
meth public java.lang.String getMimeType()
meth public java.lang.String toString()
meth public javax.swing.text.Document getDocument(boolean)
meth public org.netbeans.modules.parsing.api.Snapshot createSnapshot()
meth public org.openide.filesystems.FileObject getFileObject()
meth public org.openide.util.Lookup getLookup()
meth public static org.netbeans.modules.parsing.api.Source create(javax.swing.text.Document)
meth public static org.netbeans.modules.parsing.api.Source create(javax.swing.text.Document,org.openide.util.Lookup)
meth public static org.netbeans.modules.parsing.api.Source create(org.openide.filesystems.FileObject)
meth public static org.netbeans.modules.parsing.api.Source create(org.openide.filesystems.FileObject,org.openide.util.Lookup)
supr java.lang.Object
hfds LOG,cache,cachedParser,context,ctrl,document,eventId,fileObject,flags,listeningOnChanges,mimeType,peFwd,preferFile,schedulerEvents,sourceEnv,sourceModificationEvent,suppressListening,taskCount,unspecifiedSourceModificationEvent
hcls AComposite,ASourceModificationEvent,ExtendableSourceModificationEvent,MySourceAccessor

CLSS public abstract org.netbeans.modules.parsing.api.Task
cons public init()
supr java.lang.Object

CLSS public abstract org.netbeans.modules.parsing.api.UserTask
cons public init()
meth public abstract void run(org.netbeans.modules.parsing.api.ResultIterator) throws java.lang.Exception
supr org.netbeans.modules.parsing.api.Task

CLSS public org.netbeans.modules.parsing.spi.CursorMovedSchedulerEvent
cons protected init(java.lang.Object,int,int)
meth public int getCaretOffset()
meth public int getMarkOffset()
meth public java.lang.String toString()
supr org.netbeans.modules.parsing.spi.SchedulerEvent
hfds caretOffset,markOffset

CLSS public abstract org.netbeans.modules.parsing.spi.EmbeddingProvider
cons public init()
innr public abstract interface static !annotation Registration
meth public abstract int getPriority()
meth public abstract java.util.List<org.netbeans.modules.parsing.api.Embedding> getEmbeddings(org.netbeans.modules.parsing.api.Snapshot)
meth public final java.lang.Class<? extends org.netbeans.modules.parsing.spi.Scheduler> getSchedulerClass()
supr org.netbeans.modules.parsing.spi.SchedulerTask

CLSS public abstract interface static !annotation org.netbeans.modules.parsing.spi.EmbeddingProvider$Registration
 outer org.netbeans.modules.parsing.spi.EmbeddingProvider
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String mimeType()
meth public abstract java.lang.String targetMimeType()

CLSS public abstract org.netbeans.modules.parsing.spi.IndexingAwareParserResultTask<%0 extends org.netbeans.modules.parsing.spi.Parser$Result>
cons protected init(org.netbeans.modules.parsing.spi.TaskIndexingMode)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public final org.netbeans.modules.parsing.spi.TaskIndexingMode getIndexingMode()
supr org.netbeans.modules.parsing.spi.ParserResultTask<{org.netbeans.modules.parsing.spi.IndexingAwareParserResultTask%0}>
hfds scanMode

CLSS public final org.netbeans.modules.parsing.spi.LowMemoryWatcher
meth public boolean isLowMemory()
meth public static org.netbeans.modules.parsing.spi.LowMemoryWatcher getInstance()
meth public void free()
supr java.lang.Object
hfds delegate,instance

CLSS public org.netbeans.modules.parsing.spi.ParseException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr java.lang.Exception

CLSS public abstract org.netbeans.modules.parsing.spi.Parser
cons public init()
innr public abstract static Result
innr public final static !enum CancelReason
meth public abstract org.netbeans.modules.parsing.spi.Parser$Result getResult(org.netbeans.modules.parsing.api.Task) throws org.netbeans.modules.parsing.spi.ParseException
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void parse(org.netbeans.modules.parsing.api.Snapshot,org.netbeans.modules.parsing.api.Task,org.netbeans.modules.parsing.spi.SourceModificationEvent) throws org.netbeans.modules.parsing.spi.ParseException
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public void cancel()
 anno 0 java.lang.Deprecated()
meth public void cancel(org.netbeans.modules.parsing.spi.Parser$CancelReason,org.netbeans.modules.parsing.spi.SourceModificationEvent)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hcls MyAccessor

CLSS public final static !enum org.netbeans.modules.parsing.spi.Parser$CancelReason
 outer org.netbeans.modules.parsing.spi.Parser
fld public final static org.netbeans.modules.parsing.spi.Parser$CancelReason PARSER_RESULT_TASK
fld public final static org.netbeans.modules.parsing.spi.Parser$CancelReason SOURCE_MODIFICATION_EVENT
fld public final static org.netbeans.modules.parsing.spi.Parser$CancelReason USER_TASK
meth public static org.netbeans.modules.parsing.spi.Parser$CancelReason valueOf(java.lang.String)
meth public static org.netbeans.modules.parsing.spi.Parser$CancelReason[] values()
supr java.lang.Enum<org.netbeans.modules.parsing.spi.Parser$CancelReason>

CLSS public abstract static org.netbeans.modules.parsing.spi.Parser$Result
 outer org.netbeans.modules.parsing.spi.Parser
cons protected init(org.netbeans.modules.parsing.api.Snapshot)
meth protected abstract void invalidate()
meth protected boolean processingFinished()
meth public org.netbeans.modules.parsing.api.Snapshot getSnapshot()
supr java.lang.Object
hfds snapshot

CLSS public abstract org.netbeans.modules.parsing.spi.ParserBasedEmbeddingProvider<%0 extends org.netbeans.modules.parsing.spi.Parser$Result>
cons public init()
meth public abstract int getPriority()
meth public abstract java.util.List<org.netbeans.modules.parsing.api.Embedding> getEmbeddings({org.netbeans.modules.parsing.spi.ParserBasedEmbeddingProvider%0})
supr org.netbeans.modules.parsing.spi.SchedulerTask

CLSS public abstract org.netbeans.modules.parsing.spi.ParserFactory
cons public init()
meth public abstract org.netbeans.modules.parsing.spi.Parser createParser(java.util.Collection<org.netbeans.modules.parsing.api.Snapshot>)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.parsing.spi.ParserResultTask<%0 extends org.netbeans.modules.parsing.spi.Parser$Result>
cons public init()
meth public abstract int getPriority()
meth public abstract void run({org.netbeans.modules.parsing.spi.ParserResultTask%0},org.netbeans.modules.parsing.spi.SchedulerEvent)
supr org.netbeans.modules.parsing.spi.SchedulerTask

CLSS public abstract org.netbeans.modules.parsing.spi.Scheduler
cons public init()
fld public final static int DEFAULT_REPARSE_DELAY = 500
fld public final static java.lang.Class<? extends org.netbeans.modules.parsing.spi.Scheduler> CURSOR_SENSITIVE_TASK_SCHEDULER
fld public final static java.lang.Class<? extends org.netbeans.modules.parsing.spi.Scheduler> EDITOR_SENSITIVE_TASK_SCHEDULER
fld public final static java.lang.Class<? extends org.netbeans.modules.parsing.spi.Scheduler> SELECTED_NODES_SENSITIVE_TASK_SCHEDULER
meth protected abstract org.netbeans.modules.parsing.spi.SchedulerEvent createSchedulerEvent(org.netbeans.modules.parsing.spi.SourceModificationEvent)
meth protected final org.netbeans.modules.parsing.api.Source getSource()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth protected final void schedule(org.netbeans.modules.parsing.api.Source,org.netbeans.modules.parsing.spi.SchedulerEvent)
meth protected final void schedule(org.netbeans.modules.parsing.spi.SchedulerEvent)
supr java.lang.Object
hfds LOG,ctrl,reparseDelay,requestProcessor,source,task,wlistener
hcls Accessor,Control

CLSS public org.netbeans.modules.parsing.spi.SchedulerEvent
cons protected init(java.lang.Object)
meth public java.lang.String toString()
supr java.util.EventObject

CLSS public abstract org.netbeans.modules.parsing.spi.SchedulerTask
meth public abstract int getPriority()
meth public abstract java.lang.Class<? extends org.netbeans.modules.parsing.spi.Scheduler> getSchedulerClass()
meth public abstract void cancel()
supr org.netbeans.modules.parsing.api.Task

CLSS public org.netbeans.modules.parsing.spi.SourceModificationEvent
cons protected init(java.lang.Object)
 anno 0 java.lang.Deprecated()
cons protected init(java.lang.Object,boolean)
innr public static Composite
meth public boolean sourceChanged()
meth public int getAffectedEndOffset()
meth public int getAffectedStartOffset()
meth public java.lang.String toString()
meth public org.netbeans.modules.parsing.api.Source getModifiedSource()
supr java.util.EventObject
hfds sourceChanged

CLSS public static org.netbeans.modules.parsing.spi.SourceModificationEvent$Composite
 outer org.netbeans.modules.parsing.spi.SourceModificationEvent
cons public init(org.netbeans.modules.parsing.spi.SourceModificationEvent,org.netbeans.modules.parsing.spi.SourceModificationEvent)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.parsing.spi.SourceModificationEvent getReadEvent()
meth public org.netbeans.modules.parsing.spi.SourceModificationEvent getWriteEvent()
supr org.netbeans.modules.parsing.spi.SourceModificationEvent
hfds read,write

CLSS public abstract org.netbeans.modules.parsing.spi.TaskFactory
cons public init()
meth public abstract java.util.Collection<? extends org.netbeans.modules.parsing.spi.SchedulerTask> create(org.netbeans.modules.parsing.api.Snapshot)
supr java.lang.Object

CLSS public final !enum org.netbeans.modules.parsing.spi.TaskIndexingMode
fld public final static org.netbeans.modules.parsing.spi.TaskIndexingMode ALLOWED_DURING_SCAN
fld public final static org.netbeans.modules.parsing.spi.TaskIndexingMode DISALLOWED_DURING_SCAN
meth public static org.netbeans.modules.parsing.spi.TaskIndexingMode valueOf(java.lang.String)
meth public static org.netbeans.modules.parsing.spi.TaskIndexingMode[] values()
supr java.lang.Enum<org.netbeans.modules.parsing.spi.TaskIndexingMode>

CLSS public final org.netbeans.modules.parsing.spi.support.CancelSupport
meth public boolean isCancelled()
meth public static org.netbeans.modules.parsing.spi.support.CancelSupport create(org.netbeans.modules.parsing.spi.SchedulerTask)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds owner

CLSS public abstract org.openide.util.Lookup
cons public init()
fld public final static org.openide.util.Lookup EMPTY
innr public abstract interface static Provider
innr public abstract static Item
innr public abstract static Result
innr public final static Template
meth public <%0 extends java.lang.Object> java.util.Collection<? extends {%%0}> lookupAll(java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> org.openide.util.Lookup$Item<{%%0}> lookupItem(org.openide.util.Lookup$Template<{%%0}>)
meth public <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookupResult(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookup(org.openide.util.Lookup$Template<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>)
meth public static org.openide.util.Lookup getDefault()
supr java.lang.Object
hfds LOG,defaultLookup,defaultLookupProvider
hcls DefLookup,Empty

CLSS public abstract interface static org.openide.util.Lookup$Provider
 outer org.openide.util.Lookup
meth public abstract org.openide.util.Lookup getLookup()

