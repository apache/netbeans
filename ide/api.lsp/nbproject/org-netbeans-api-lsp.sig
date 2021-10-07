#Signature file v4.1
#Version 1.5

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

CLSS public org.netbeans.api.lsp.CodeAction
cons public init(java.lang.String,org.netbeans.api.lsp.Command)
cons public init(java.lang.String,org.netbeans.api.lsp.Command,org.netbeans.api.lsp.WorkspaceEdit)
cons public init(java.lang.String,org.netbeans.api.lsp.WorkspaceEdit)
meth public java.lang.String getTitle()
meth public org.netbeans.api.lsp.Command getCommand()
meth public org.netbeans.api.lsp.WorkspaceEdit getEdit()
supr java.lang.Object
hfds command,edit,title

CLSS public org.netbeans.api.lsp.Command
cons public init(java.lang.String,java.lang.String)
meth public java.lang.String getCommand()
meth public java.lang.String getTitle()
supr java.lang.Object
hfds command,title

CLSS public final org.netbeans.api.lsp.Completion
innr public final static !enum Kind
innr public final static !enum Tag
innr public final static !enum TextFormat
innr public final static !enum TriggerKind
innr public final static Context
meth public boolean isPreselect()
meth public java.lang.String getFilterText()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getInsertText()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getLabel()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getSortText()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.util.List<java.lang.Character> getCommitCharacters()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.util.List<org.netbeans.api.lsp.Completion$Tag> getTags()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.util.concurrent.CompletableFuture<java.lang.String> getDetail()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.util.concurrent.CompletableFuture<java.lang.String> getDocumentation()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.util.concurrent.CompletableFuture<java.util.List<org.netbeans.api.lsp.TextEdit>> getAdditionalTextEdits()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.api.lsp.Completion$Kind getKind()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.api.lsp.Completion$TextFormat getInsertTextFormat()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.api.lsp.TextEdit getTextEdit()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static boolean collect(javax.swing.text.Document,int,org.netbeans.api.lsp.Completion$Context,java.util.function.Consumer<org.netbeans.api.lsp.Completion>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds additionalTextEdits,commitCharacters,detail,documentation,filterText,insertText,insertTextFormat,kind,label,preselect,sortText,tags,textEdit

CLSS public final static org.netbeans.api.lsp.Completion$Context
 outer org.netbeans.api.lsp.Completion
cons public init(org.netbeans.api.lsp.Completion$TriggerKind,java.lang.Character)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public java.lang.Character getTriggerCharacter()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.api.lsp.Completion$TriggerKind getTriggerKind()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds triggerCharacter,triggerKind

CLSS public final static !enum org.netbeans.api.lsp.Completion$Kind
 outer org.netbeans.api.lsp.Completion
fld public final static org.netbeans.api.lsp.Completion$Kind Class
fld public final static org.netbeans.api.lsp.Completion$Kind Color
fld public final static org.netbeans.api.lsp.Completion$Kind Constant
fld public final static org.netbeans.api.lsp.Completion$Kind Constructor
fld public final static org.netbeans.api.lsp.Completion$Kind Enum
fld public final static org.netbeans.api.lsp.Completion$Kind EnumMember
fld public final static org.netbeans.api.lsp.Completion$Kind Event
fld public final static org.netbeans.api.lsp.Completion$Kind Field
fld public final static org.netbeans.api.lsp.Completion$Kind File
fld public final static org.netbeans.api.lsp.Completion$Kind Folder
fld public final static org.netbeans.api.lsp.Completion$Kind Function
fld public final static org.netbeans.api.lsp.Completion$Kind Interface
fld public final static org.netbeans.api.lsp.Completion$Kind Keyword
fld public final static org.netbeans.api.lsp.Completion$Kind Method
fld public final static org.netbeans.api.lsp.Completion$Kind Module
fld public final static org.netbeans.api.lsp.Completion$Kind Operator
fld public final static org.netbeans.api.lsp.Completion$Kind Property
fld public final static org.netbeans.api.lsp.Completion$Kind Reference
fld public final static org.netbeans.api.lsp.Completion$Kind Snippet
fld public final static org.netbeans.api.lsp.Completion$Kind Struct
fld public final static org.netbeans.api.lsp.Completion$Kind Text
fld public final static org.netbeans.api.lsp.Completion$Kind TypeParameter
fld public final static org.netbeans.api.lsp.Completion$Kind Unit
fld public final static org.netbeans.api.lsp.Completion$Kind Value
fld public final static org.netbeans.api.lsp.Completion$Kind Variable
meth public static org.netbeans.api.lsp.Completion$Kind valueOf(java.lang.String)
meth public static org.netbeans.api.lsp.Completion$Kind[] values()
supr java.lang.Enum<org.netbeans.api.lsp.Completion$Kind>

CLSS public final static !enum org.netbeans.api.lsp.Completion$Tag
 outer org.netbeans.api.lsp.Completion
fld public final static org.netbeans.api.lsp.Completion$Tag Deprecated
meth public static org.netbeans.api.lsp.Completion$Tag valueOf(java.lang.String)
meth public static org.netbeans.api.lsp.Completion$Tag[] values()
supr java.lang.Enum<org.netbeans.api.lsp.Completion$Tag>

CLSS public final static !enum org.netbeans.api.lsp.Completion$TextFormat
 outer org.netbeans.api.lsp.Completion
fld public final static org.netbeans.api.lsp.Completion$TextFormat PlainText
fld public final static org.netbeans.api.lsp.Completion$TextFormat Snippet
meth public static org.netbeans.api.lsp.Completion$TextFormat valueOf(java.lang.String)
meth public static org.netbeans.api.lsp.Completion$TextFormat[] values()
supr java.lang.Enum<org.netbeans.api.lsp.Completion$TextFormat>

CLSS public final static !enum org.netbeans.api.lsp.Completion$TriggerKind
 outer org.netbeans.api.lsp.Completion
fld public final static org.netbeans.api.lsp.Completion$TriggerKind Invoked
fld public final static org.netbeans.api.lsp.Completion$TriggerKind TriggerCharacter
fld public final static org.netbeans.api.lsp.Completion$TriggerKind TriggerForIncompleteCompletions
meth public static org.netbeans.api.lsp.Completion$TriggerKind valueOf(java.lang.String)
meth public static org.netbeans.api.lsp.Completion$TriggerKind[] values()
supr java.lang.Enum<org.netbeans.api.lsp.Completion$TriggerKind>

CLSS public org.netbeans.api.lsp.Diagnostic
innr public abstract interface static LazyCodeActions
innr public final static !enum Severity
innr public final static Builder
meth public java.lang.String getCode()
meth public java.lang.String getDescription()
meth public org.netbeans.api.lsp.Diagnostic$LazyCodeActions getActions()
meth public org.netbeans.api.lsp.Diagnostic$Severity getSeverity()
meth public org.netbeans.api.lsp.Position getEndPosition()
meth public org.netbeans.api.lsp.Position getStartPosition()
supr java.lang.Object
hfds actions,code,description,endPosition,severity,startPosition

CLSS public final static org.netbeans.api.lsp.Diagnostic$Builder
 outer org.netbeans.api.lsp.Diagnostic
meth public org.netbeans.api.lsp.Diagnostic build()
meth public org.netbeans.api.lsp.Diagnostic$Builder addActions(org.netbeans.api.lsp.Diagnostic$LazyCodeActions)
meth public org.netbeans.api.lsp.Diagnostic$Builder setCode(java.lang.String)
meth public org.netbeans.api.lsp.Diagnostic$Builder setSeverity(org.netbeans.api.lsp.Diagnostic$Severity)
meth public static org.netbeans.api.lsp.Diagnostic$Builder create(org.netbeans.api.lsp.Position,org.netbeans.api.lsp.Position,java.lang.String)
supr java.lang.Object
hfds actions,code,description,endPosition,severity,startPosition

CLSS public abstract interface static org.netbeans.api.lsp.Diagnostic$LazyCodeActions
 outer org.netbeans.api.lsp.Diagnostic
meth public abstract java.util.List<org.netbeans.api.lsp.CodeAction> computeCodeActions(java.util.function.Consumer<java.lang.Exception>)

CLSS public final static !enum org.netbeans.api.lsp.Diagnostic$Severity
 outer org.netbeans.api.lsp.Diagnostic
fld public final static org.netbeans.api.lsp.Diagnostic$Severity Error
fld public final static org.netbeans.api.lsp.Diagnostic$Severity Hint
fld public final static org.netbeans.api.lsp.Diagnostic$Severity Information
fld public final static org.netbeans.api.lsp.Diagnostic$Severity Warning
meth public static org.netbeans.api.lsp.Diagnostic$Severity valueOf(java.lang.String)
meth public static org.netbeans.api.lsp.Diagnostic$Severity[] values()
supr java.lang.Enum<org.netbeans.api.lsp.Diagnostic$Severity>

CLSS public final org.netbeans.api.lsp.Hover
cons public init()
meth public static java.util.concurrent.CompletableFuture<java.lang.String> getContent(javax.swing.text.Document,int)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public final org.netbeans.api.lsp.HyperlinkLocation
meth public boolean equals(java.lang.Object)
meth public int getEndOffset()
meth public int getStartOffset()
meth public int hashCode()
meth public java.lang.String toString()
meth public org.openide.filesystems.FileObject getFileObject()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static java.util.concurrent.CompletableFuture<java.util.List<org.netbeans.api.lsp.HyperlinkLocation>> resolve(javax.swing.text.Document,int)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static java.util.concurrent.CompletableFuture<java.util.List<org.netbeans.api.lsp.HyperlinkLocation>> resolveTypeDefinition(javax.swing.text.Document,int)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds endOffset,fileObject,startOffset

CLSS public abstract interface org.netbeans.api.lsp.Position
meth public abstract int getOffset()

CLSS public org.netbeans.api.lsp.ResourceOperation
cons public init()
innr public final static CreateFile
supr java.lang.Object

CLSS public final static org.netbeans.api.lsp.ResourceOperation$CreateFile
 outer org.netbeans.api.lsp.ResourceOperation
cons public init(java.lang.String)
meth public java.lang.String getNewFile()
supr org.netbeans.api.lsp.ResourceOperation
hfds newFile

CLSS public org.netbeans.api.lsp.TextDocumentEdit
cons public init(java.lang.String,java.util.List<org.netbeans.api.lsp.TextEdit>)
meth public java.lang.String getDocument()
meth public java.util.List<org.netbeans.api.lsp.TextEdit> getEdits()
supr java.lang.Object
hfds document,edits

CLSS public final org.netbeans.api.lsp.TextEdit
cons public init(int,int,java.lang.String)
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public int getEndOffset()
meth public int getStartOffset()
meth public java.lang.String getNewText()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds end,newText,start

CLSS public org.netbeans.api.lsp.WorkspaceEdit
cons public init(java.util.List<org.openide.util.Union2<org.netbeans.api.lsp.TextDocumentEdit,org.netbeans.api.lsp.ResourceOperation>>)
meth public java.util.List<org.openide.util.Union2<org.netbeans.api.lsp.TextDocumentEdit,org.netbeans.api.lsp.ResourceOperation>> getDocumentChanges()
supr java.lang.Object
hfds documentChanges

CLSS public abstract interface !annotation org.netbeans.spi.editor.mimelookup.MimeLocation
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass()
meth public abstract java.lang.String subfolderName()

CLSS public abstract interface org.netbeans.spi.lsp.CompletionCollector
 anno 0 org.netbeans.spi.editor.mimelookup.MimeLocation(java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass=class org.netbeans.spi.editor.mimelookup.InstanceProvider, java.lang.String subfolderName="CompletionCollectors")
innr public final static Builder
meth public abstract boolean collectCompletions(javax.swing.text.Document,int,org.netbeans.api.lsp.Completion$Context,java.util.function.Consumer<org.netbeans.api.lsp.Completion>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.lsp.CompletionCollector$Builder newBuilder(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public final static org.netbeans.spi.lsp.CompletionCollector$Builder
 outer org.netbeans.spi.lsp.CompletionCollector
meth public org.netbeans.api.lsp.Completion build()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.spi.lsp.CompletionCollector$Builder addCommitCharacter(char)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.spi.lsp.CompletionCollector$Builder addTag(org.netbeans.api.lsp.Completion$Tag)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.spi.lsp.CompletionCollector$Builder additionalTextEdits(java.util.List<org.netbeans.api.lsp.TextEdit>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.spi.lsp.CompletionCollector$Builder additionalTextEdits(java.util.function.Supplier<java.util.List<org.netbeans.api.lsp.TextEdit>>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.spi.lsp.CompletionCollector$Builder detail(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.spi.lsp.CompletionCollector$Builder detail(java.util.function.Supplier<java.lang.String>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.spi.lsp.CompletionCollector$Builder documentation(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.spi.lsp.CompletionCollector$Builder documentation(java.util.function.Supplier<java.lang.String>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.spi.lsp.CompletionCollector$Builder filterText(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.spi.lsp.CompletionCollector$Builder insertText(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.spi.lsp.CompletionCollector$Builder insertTextFormat(org.netbeans.api.lsp.Completion$TextFormat)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.spi.lsp.CompletionCollector$Builder kind(org.netbeans.api.lsp.Completion$Kind)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.spi.lsp.CompletionCollector$Builder label(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.spi.lsp.CompletionCollector$Builder preselect(boolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.spi.lsp.CompletionCollector$Builder sortText(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.spi.lsp.CompletionCollector$Builder textEdit(org.netbeans.api.lsp.TextEdit)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds additionalTextEdits,commitCharacters,detail,documentation,filterText,insertText,insertTextFormat,kind,label,preselect,sortText,tags,textEdit
hcls LazyCompletableFuture

CLSS public abstract interface org.netbeans.spi.lsp.ErrorProvider
innr public final static !enum Kind
innr public final static Context
meth public abstract java.util.List<? extends org.netbeans.api.lsp.Diagnostic> computeErrors(org.netbeans.spi.lsp.ErrorProvider$Context)

CLSS public final static org.netbeans.spi.lsp.ErrorProvider$Context
 outer org.netbeans.spi.lsp.ErrorProvider
cons public init(org.openide.filesystems.FileObject,int,org.netbeans.spi.lsp.ErrorProvider$Kind)
cons public init(org.openide.filesystems.FileObject,org.netbeans.spi.lsp.ErrorProvider$Kind)
meth public boolean isCancelled()
meth public int getOffset()
meth public org.netbeans.spi.lsp.ErrorProvider$Kind errorKind()
meth public org.openide.filesystems.FileObject file()
meth public void cancel()
meth public void registerCancelCallback(java.lang.Runnable)
supr java.lang.Object
hfds cancel,cancelCallbacks,errorKind,file,offset

CLSS public final static !enum org.netbeans.spi.lsp.ErrorProvider$Kind
 outer org.netbeans.spi.lsp.ErrorProvider
fld public final static org.netbeans.spi.lsp.ErrorProvider$Kind ERRORS
fld public final static org.netbeans.spi.lsp.ErrorProvider$Kind HINTS
meth public static org.netbeans.spi.lsp.ErrorProvider$Kind valueOf(java.lang.String)
meth public static org.netbeans.spi.lsp.ErrorProvider$Kind[] values()
supr java.lang.Enum<org.netbeans.spi.lsp.ErrorProvider$Kind>

CLSS public abstract interface org.netbeans.spi.lsp.HoverProvider
 anno 0 org.netbeans.spi.editor.mimelookup.MimeLocation(java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass=class org.netbeans.spi.editor.mimelookup.InstanceProvider, java.lang.String subfolderName="HoverProviders")
meth public abstract java.util.concurrent.CompletableFuture<java.lang.String> getHoverContent(javax.swing.text.Document,int)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.spi.lsp.HyperlinkLocationProvider
 anno 0 org.netbeans.spi.editor.mimelookup.MimeLocation(java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass=class org.netbeans.spi.editor.mimelookup.InstanceProvider, java.lang.String subfolderName="HyperlinkLocationProviders")
meth public abstract java.util.concurrent.CompletableFuture<org.netbeans.api.lsp.HyperlinkLocation> getHyperlinkLocation(javax.swing.text.Document,int)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.lsp.HyperlinkLocation createHyperlinkLocation(org.openide.filesystems.FileObject,int,int)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.spi.lsp.HyperlinkTypeDefLocationProvider
 anno 0 org.netbeans.spi.editor.mimelookup.MimeLocation(java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass=class org.netbeans.spi.editor.mimelookup.InstanceProvider, java.lang.String subfolderName="HyperlinkTypeDefLocationProviders")
meth public abstract java.util.concurrent.CompletableFuture<org.netbeans.api.lsp.HyperlinkLocation> getHyperlinkTypeDefLocation(javax.swing.text.Document,int)
 anno 1 org.netbeans.api.annotations.common.NonNull()

