#Signature file v4.1
#Version 1.68.0

CLSS public abstract interface !annotation java.lang.FunctionalInterface
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
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

CLSS public abstract interface java.lang.Runnable
 anno 0 java.lang.FunctionalInterface()
meth public abstract void run()

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

CLSS public final org.netbeans.api.editor.completion.Completion
meth public static org.netbeans.api.editor.completion.Completion get()
meth public void hideAll()
meth public void hideCompletion()
meth public void hideDocumentation()
meth public void hideToolTip()
meth public void showCompletion()
meth public void showDocumentation()
meth public void showToolTip()
supr java.lang.Object
hfds singleton

CLSS public abstract interface org.netbeans.spi.editor.completion.CompletionDocumentation
meth public abstract java.lang.String getText()
meth public abstract java.net.URL getURL()
meth public abstract javax.swing.Action getGotoSourceAction()
meth public abstract org.netbeans.spi.editor.completion.CompletionDocumentation resolveLink(java.lang.String)

CLSS public abstract interface org.netbeans.spi.editor.completion.CompletionItem
meth public abstract boolean instantSubstitution(javax.swing.text.JTextComponent)
meth public abstract int getPreferredWidth(java.awt.Graphics,java.awt.Font)
meth public abstract int getSortPriority()
meth public abstract java.lang.CharSequence getInsertPrefix()
meth public abstract java.lang.CharSequence getSortText()
meth public abstract org.netbeans.spi.editor.completion.CompletionTask createDocumentationTask()
meth public abstract org.netbeans.spi.editor.completion.CompletionTask createToolTipTask()
meth public abstract void defaultAction(javax.swing.text.JTextComponent)
meth public abstract void processKeyEvent(java.awt.event.KeyEvent)
meth public abstract void render(java.awt.Graphics,java.awt.Font,java.awt.Color,java.awt.Color,int,int,boolean)
meth public boolean shouldSingleClickInvokeDefaultAction()

CLSS public abstract interface org.netbeans.spi.editor.completion.CompletionProvider
 anno 0 org.netbeans.spi.editor.mimelookup.MimeLocation(java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass=class org.netbeans.spi.editor.mimelookup.InstanceProvider, java.lang.String subfolderName="CompletionProviders")
fld public final static int COMPLETION_ALL_QUERY_TYPE = 9
fld public final static int COMPLETION_QUERY_TYPE = 1
fld public final static int DOCUMENTATION_QUERY_TYPE = 2
fld public final static int TOOLTIP_QUERY_TYPE = 4
meth public abstract int getAutoQueryTypes(javax.swing.text.JTextComponent,java.lang.String)
meth public abstract org.netbeans.spi.editor.completion.CompletionTask createTask(int,javax.swing.text.JTextComponent)

CLSS public final org.netbeans.spi.editor.completion.CompletionResultSet
fld public final static int PRIORITY_SORT_TYPE = 0
fld public final static int TEXT_SORT_TYPE = 1
meth public boolean addAllItems(java.util.Collection<? extends org.netbeans.spi.editor.completion.CompletionItem>)
meth public boolean addItem(org.netbeans.spi.editor.completion.CompletionItem)
meth public boolean isFinished()
meth public int getSortType()
meth public void estimateItems(int,int)
meth public void finish()
meth public void setAnchorOffset(int)
meth public void setDocumentation(org.netbeans.spi.editor.completion.CompletionDocumentation)
meth public void setHasAdditionalItems(boolean)
meth public void setHasAdditionalItemsText(java.lang.String)
meth public void setTitle(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void setToolTip(javax.swing.JToolTip)
meth public void setWaitText(java.lang.String)
supr java.lang.Object
hfds impl
hcls SpiAccessor

CLSS public abstract interface org.netbeans.spi.editor.completion.CompletionTask
meth public abstract void cancel()
meth public abstract void query(org.netbeans.spi.editor.completion.CompletionResultSet)
meth public abstract void refresh(org.netbeans.spi.editor.completion.CompletionResultSet)

CLSS public abstract interface org.netbeans.spi.editor.completion.CompositeCompletionItem
intf org.netbeans.spi.editor.completion.CompletionItem
meth public abstract java.util.List<? extends org.netbeans.spi.editor.completion.CompletionItem> getSubItems()

CLSS public abstract interface org.netbeans.spi.editor.completion.LazyCompletionItem
intf org.netbeans.spi.editor.completion.CompletionItem
meth public abstract boolean accept()

CLSS public abstract org.netbeans.spi.editor.completion.support.AsyncCompletionQuery
cons public init()
meth protected abstract void query(org.netbeans.spi.editor.completion.CompletionResultSet,javax.swing.text.Document,int)
meth protected boolean canFilter(javax.swing.text.JTextComponent)
meth protected void filter(org.netbeans.spi.editor.completion.CompletionResultSet)
meth protected void preQueryUpdate(javax.swing.text.JTextComponent)
meth protected void prepareQuery(javax.swing.text.JTextComponent)
meth public final boolean isTaskCancelled()
supr java.lang.Object
hfds task

CLSS public final org.netbeans.spi.editor.completion.support.AsyncCompletionTask
cons public init(org.netbeans.spi.editor.completion.support.AsyncCompletionQuery)
cons public init(org.netbeans.spi.editor.completion.support.AsyncCompletionQuery,javax.swing.text.JTextComponent)
intf java.lang.Runnable
intf org.netbeans.spi.editor.completion.CompletionTask
meth public java.lang.String toString()
meth public void cancel()
meth public void query(org.netbeans.spi.editor.completion.CompletionResultSet)
meth public void refresh(org.netbeans.spi.editor.completion.CompletionResultSet)
meth public void run()
supr java.lang.Object
hfds RP,cancelled,component,doc,query,queryCaretOffset,queryInvoked,queryResultSet,refreshResultSet,rpTask

CLSS public final org.netbeans.spi.editor.completion.support.CompletionUtilities
innr public final static CompletionItemBuilder
innr public final static OnSelectContext
meth public static int getPreferredWidth(java.lang.String,java.lang.String,java.awt.Graphics,java.awt.Font)
meth public static org.netbeans.spi.editor.completion.support.CompletionUtilities$CompletionItemBuilder newCompletionItemBuilder(java.lang.String)
meth public static void renderHtml(javax.swing.ImageIcon,java.lang.String,java.lang.String,java.awt.Graphics,java.awt.Font,java.awt.Color,int,int,boolean)
supr java.lang.Object
hfds AFTER_ICON_GAP,AFTER_RIGHT_TEXT_GAP,BEFORE_ICON_GAP,BEFORE_RIGHT_TEXT_GAP,ICON_HEIGHT,ICON_WIDTH
hcls SpiAccessor

CLSS public final static org.netbeans.spi.editor.completion.support.CompletionUtilities$CompletionItemBuilder
 outer org.netbeans.spi.editor.completion.support.CompletionUtilities
meth public org.netbeans.spi.editor.completion.CompletionItem build()
meth public org.netbeans.spi.editor.completion.support.CompletionUtilities$CompletionItemBuilder documentationTask(java.util.function.Supplier<org.netbeans.spi.editor.completion.CompletionTask>)
meth public org.netbeans.spi.editor.completion.support.CompletionUtilities$CompletionItemBuilder endOffset(int)
meth public org.netbeans.spi.editor.completion.support.CompletionUtilities$CompletionItemBuilder iconResource(java.lang.String)
meth public org.netbeans.spi.editor.completion.support.CompletionUtilities$CompletionItemBuilder insertText(java.lang.String)
meth public org.netbeans.spi.editor.completion.support.CompletionUtilities$CompletionItemBuilder leftHtmlText(java.lang.String)
meth public org.netbeans.spi.editor.completion.support.CompletionUtilities$CompletionItemBuilder onSelect(java.util.function.Consumer<org.netbeans.spi.editor.completion.support.CompletionUtilities$OnSelectContext>)
meth public org.netbeans.spi.editor.completion.support.CompletionUtilities$CompletionItemBuilder rightHtmlText(java.lang.String)
meth public org.netbeans.spi.editor.completion.support.CompletionUtilities$CompletionItemBuilder sortPriority(int)
meth public org.netbeans.spi.editor.completion.support.CompletionUtilities$CompletionItemBuilder sortText(java.lang.CharSequence)
meth public org.netbeans.spi.editor.completion.support.CompletionUtilities$CompletionItemBuilder startOffset(int)
meth public org.netbeans.spi.editor.completion.support.CompletionUtilities$CompletionItemBuilder tooltipTask(java.util.function.Supplier<org.netbeans.spi.editor.completion.CompletionTask>)
supr java.lang.Object
hfds documentationTask,endOffset,iconResource,insertText,leftHtmlText,onSelectCallback,rightHtmlText,sortPriority,sortText,startOffset,tooltipTask

CLSS public final static org.netbeans.spi.editor.completion.support.CompletionUtilities$OnSelectContext
 outer org.netbeans.spi.editor.completion.support.CompletionUtilities
meth public boolean isOverwrite()
meth public javax.swing.text.JTextComponent getComponent()
supr java.lang.Object
hfds component,overwrite

CLSS public abstract interface !annotation org.netbeans.spi.editor.mimelookup.MimeLocation
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass()
meth public abstract java.lang.String subfolderName()

