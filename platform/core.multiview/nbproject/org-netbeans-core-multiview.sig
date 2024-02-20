#Signature file v4.1
#Version 1.67

CLSS public abstract interface java.io.Serializable

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

CLSS public final org.netbeans.core.api.multiview.MultiViewHandler
meth public org.netbeans.core.api.multiview.MultiViewPerspective getSelectedPerspective()
meth public org.netbeans.core.api.multiview.MultiViewPerspective[] getPerspectives()
meth public void addMultiViewDescription(org.netbeans.core.spi.multiview.MultiViewDescription,int)
meth public void removeMultiViewDescription(org.netbeans.core.spi.multiview.MultiViewDescription)
meth public void requestActive(org.netbeans.core.api.multiview.MultiViewPerspective)
meth public void requestVisible(org.netbeans.core.api.multiview.MultiViewPerspective)
supr java.lang.Object
hfds del

CLSS public final org.netbeans.core.api.multiview.MultiViewPerspective
meth public int getPersistenceType()
meth public java.awt.Image getIcon()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getDisplayName()
meth public java.lang.String preferredID()
meth public org.openide.util.HelpCtx getHelpCtx()
supr java.lang.Object
hfds description

CLSS public final org.netbeans.core.api.multiview.MultiViews
meth public static <%0 extends java.io.Serializable & org.openide.util.Lookup$Provider> org.openide.windows.CloneableTopComponent createCloneableMultiView(java.lang.String,{%%0})
meth public static <%0 extends java.io.Serializable & org.openide.util.Lookup$Provider> org.openide.windows.TopComponent createMultiView(java.lang.String,{%%0})
meth public static org.netbeans.core.api.multiview.MultiViewHandler findMultiViewHandler(org.openide.windows.TopComponent)
supr java.lang.Object

CLSS public abstract interface org.netbeans.core.spi.multiview.CloseOperationHandler
meth public abstract boolean resolveCloseOperation(org.netbeans.core.spi.multiview.CloseOperationState[])

CLSS public final org.netbeans.core.spi.multiview.CloseOperationState
fld public final static org.netbeans.core.spi.multiview.CloseOperationState STATE_OK
meth public boolean canClose()
meth public java.lang.String getCloseWarningID()
meth public javax.swing.Action getDiscardAction()
meth public javax.swing.Action getProceedAction()
supr java.lang.Object
hfds canClose,discardAction,id,proceedAction

CLSS public abstract interface org.netbeans.core.spi.multiview.MultiViewDescription
 anno 0 org.netbeans.spi.editor.mimelookup.MimeLocation(java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass=class org.netbeans.spi.editor.mimelookup.InstanceProvider, java.lang.String subfolderName="MultiView")
meth public abstract int getPersistenceType()
meth public abstract java.awt.Image getIcon()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String preferredID()
meth public abstract org.netbeans.core.spi.multiview.MultiViewElement createElement()
meth public abstract org.openide.util.HelpCtx getHelpCtx()

CLSS public abstract interface org.netbeans.core.spi.multiview.MultiViewElement
innr public abstract interface static !annotation Registration
meth public abstract javax.swing.Action[] getActions()
meth public abstract javax.swing.JComponent getToolbarRepresentation()
meth public abstract javax.swing.JComponent getVisualRepresentation()
meth public abstract org.netbeans.core.spi.multiview.CloseOperationState canCloseElement()
meth public abstract org.openide.awt.UndoRedo getUndoRedo()
meth public abstract org.openide.util.Lookup getLookup()
meth public abstract void componentActivated()
meth public abstract void componentClosed()
meth public abstract void componentDeactivated()
meth public abstract void componentHidden()
meth public abstract void componentOpened()
meth public abstract void componentShowing()
meth public abstract void setMultiViewCallback(org.netbeans.core.spi.multiview.MultiViewElementCallback)

CLSS public abstract interface static !annotation org.netbeans.core.spi.multiview.MultiViewElement$Registration
 outer org.netbeans.core.spi.multiview.MultiViewElement
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()
meth public abstract !hasdefault java.lang.String iconBase()
meth public abstract int persistenceType()
meth public abstract java.lang.String displayName()
meth public abstract java.lang.String preferredID()
meth public abstract java.lang.String[] mimeType()

CLSS public final org.netbeans.core.spi.multiview.MultiViewElementCallback
meth public boolean isSelectedElement()
meth public javax.swing.Action[] createDefaultActions()
meth public org.openide.windows.TopComponent getTopComponent()
meth public void requestActive()
meth public void requestVisible()
meth public void updateTitle(java.lang.String)
supr java.lang.Object
hfds delegate

CLSS public final org.netbeans.core.spi.multiview.MultiViewFactory
fld public final static javax.swing.Action NOOP_CLOSE_ACTION
fld public final static org.netbeans.core.spi.multiview.MultiViewElement BLANK_ELEMENT
meth public static org.netbeans.core.spi.multiview.CloseOperationState createUnsafeCloseState(java.lang.String,javax.swing.Action,javax.swing.Action)
meth public static org.openide.windows.CloneableTopComponent createCloneableMultiView(org.netbeans.core.spi.multiview.MultiViewDescription[],org.netbeans.core.spi.multiview.MultiViewDescription)
meth public static org.openide.windows.CloneableTopComponent createCloneableMultiView(org.netbeans.core.spi.multiview.MultiViewDescription[],org.netbeans.core.spi.multiview.MultiViewDescription,org.netbeans.core.spi.multiview.CloseOperationHandler)
meth public static org.openide.windows.TopComponent createMultiView(org.netbeans.core.spi.multiview.MultiViewDescription[],org.netbeans.core.spi.multiview.MultiViewDescription)
meth public static org.openide.windows.TopComponent createMultiView(org.netbeans.core.spi.multiview.MultiViewDescription[],org.netbeans.core.spi.multiview.MultiViewDescription,org.netbeans.core.spi.multiview.CloseOperationHandler)
supr java.lang.Object
hcls Blank,DefaultCloseHandler,MapMVD,NoopAction

CLSS public abstract interface org.netbeans.core.spi.multiview.SourceViewMarker

CLSS public org.netbeans.core.spi.multiview.text.MultiViewEditorElement
cons public init(org.openide.util.Lookup)
intf java.io.Serializable
intf org.netbeans.core.spi.multiview.MultiViewElement
intf org.openide.text.CloneableEditorSupport$Pane
meth public javax.swing.Action[] getActions()
meth public javax.swing.JComponent getToolbarRepresentation()
meth public javax.swing.JComponent getVisualRepresentation()
meth public javax.swing.JEditorPane getEditorPane()
meth public org.netbeans.core.spi.multiview.CloseOperationState canCloseElement()
meth public org.openide.awt.UndoRedo getUndoRedo()
meth public org.openide.util.Lookup getLookup()
meth public org.openide.windows.CloneableTopComponent getComponent()
meth public void componentActivated()
meth public void componentClosed()
meth public void componentDeactivated()
meth public void componentHidden()
meth public void componentOpened()
meth public void componentShowing()
meth public void ensureVisible()
meth public void setMultiViewCallback(org.netbeans.core.spi.multiview.MultiViewElementCallback)
meth public void updateName()
supr java.lang.Object
hfds editor,serialVersionUID

CLSS public abstract interface !annotation org.netbeans.spi.editor.mimelookup.MimeLocation
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass()
meth public abstract java.lang.String subfolderName()

CLSS public abstract org.openide.text.CloneableEditorSupport
cons public init(org.openide.text.CloneableEditorSupport$Env)
cons public init(org.openide.text.CloneableEditorSupport$Env,org.openide.util.Lookup)
fld public final static java.lang.String EDITOR_MODE = "editor"
fld public final static javax.swing.undo.UndoableEdit BEGIN_COMMIT_GROUP
fld public final static javax.swing.undo.UndoableEdit END_COMMIT_GROUP
fld public final static javax.swing.undo.UndoableEdit MARK_COMMIT_GROUP
innr public abstract interface static Env
innr public abstract interface static Pane
meth protected abstract java.lang.String messageName()
meth protected abstract java.lang.String messageSave()
meth protected abstract java.lang.String messageToolTip()
meth protected boolean asynchronousOpen()
meth protected boolean canClose()
meth protected boolean close(boolean)
meth protected boolean notifyModified()
meth protected final org.openide.awt.UndoRedo$Manager getUndoRedo()
meth protected final org.openide.text.CloneableEditorSupport$Pane openAt(org.openide.text.PositionRef,int)
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected java.awt.Component wrapEditorComponent(java.awt.Component)
meth protected java.lang.String documentID()
meth protected java.lang.String messageHtmlName()
meth protected java.lang.String messageLine(org.openide.text.Line)
meth protected javax.swing.text.EditorKit createEditorKit()
meth protected javax.swing.text.StyledDocument createStyledDocument(javax.swing.text.EditorKit)
meth protected org.openide.awt.UndoRedo$Manager createUndoRedoManager()
meth protected org.openide.text.CloneableEditor createCloneableEditor()
meth protected org.openide.text.CloneableEditorSupport$Pane createPane()
meth protected org.openide.util.Task reloadDocument()
meth protected org.openide.windows.CloneableTopComponent createCloneableTopComponent()
meth protected void afterRedirect(org.openide.windows.CloneableOpenSupport)
meth protected void initializeCloneableEditor(org.openide.text.CloneableEditor)
meth protected void loadFromStreamToKit(javax.swing.text.StyledDocument,java.io.InputStream,javax.swing.text.EditorKit) throws java.io.IOException,javax.swing.text.BadLocationException
meth protected void notifyClosed()
meth protected void notifyUnmodified()
meth protected void saveFromKitToStream(javax.swing.text.StyledDocument,javax.swing.text.EditorKit,java.io.OutputStream) throws java.io.IOException,javax.swing.text.BadLocationException
meth protected void updateTitles()
meth public boolean isDocumentLoaded()
meth public boolean isModified()
meth public final org.openide.text.PositionRef createPositionRef(int,javax.swing.text.Position$Bias)
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public java.io.InputStream getInputStream() throws java.io.IOException
meth public java.lang.String toString()
meth public javax.swing.JEditorPane[] getOpenedPanes()
meth public javax.swing.text.StyledDocument getDocument()
meth public javax.swing.text.StyledDocument openDocument() throws java.io.IOException
meth public org.openide.text.Line$Set getLineSet()
meth public org.openide.util.Task prepareDocument()
meth public static javax.swing.text.EditorKit getEditorKit(java.lang.String)
meth public void addChangeListener(javax.swing.event.ChangeListener)
 anno 0 java.lang.Deprecated()
meth public void open()
meth public void print()
meth public void removeChangeListener(javax.swing.event.ChangeListener)
 anno 0 java.lang.Deprecated()
meth public void saveDocument() throws java.io.IOException
meth public void setMIMEType(java.lang.String)
supr org.openide.windows.CloneableOpenSupport
hfds ERR,LOCAL_CLOSE_DOCUMENT,LOCK_PRINTING,PROP_PANE,alreadyModified,annotationsLoaded,checkModificationLock,docFilter,inUserQuestionExceptionHandler,isSaving,kit,lastReusable,lastSaveTime,lastSelected,lineSet,lineSetLineVector,listener,listeners,listeningOnEnv,lookup,mimeType,openClose,positionManager,preventModification,printing,propertyChangeSupport,reloadDialogOpened,undoRedo,warnedClasses
hcls DocFilter,Listener,PlainEditorKit

CLSS public abstract interface static org.openide.text.CloneableEditorSupport$Pane
 outer org.openide.text.CloneableEditorSupport
meth public abstract javax.swing.JEditorPane getEditorPane()
meth public abstract org.openide.windows.CloneableTopComponent getComponent()
meth public abstract void ensureVisible()
meth public abstract void updateName()

CLSS public abstract org.openide.windows.CloneableOpenSupport
cons public init(org.openide.windows.CloneableOpenSupport$Env)
fld protected org.openide.windows.CloneableOpenSupport$Env env
fld protected org.openide.windows.CloneableTopComponent$Ref allEditors
innr public abstract interface static Env
meth protected abstract java.lang.String messageOpened()
meth protected abstract java.lang.String messageOpening()
meth protected abstract org.openide.windows.CloneableTopComponent createCloneableTopComponent()
meth protected boolean canClose()
meth protected boolean close(boolean)
meth protected final org.openide.windows.CloneableTopComponent openCloneableTopComponent()
meth protected void afterRedirect(org.openide.windows.CloneableOpenSupport)
meth public boolean close()
meth public void edit()
meth public void open()
meth public void view()
supr java.lang.Object
hfds container
hcls Listener

