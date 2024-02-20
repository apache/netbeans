#Signature file v4.1
#Version 1.68

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

CLSS public abstract interface java.util.EventListener

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final org.netbeans.api.editor.fold.Fold
meth public boolean isCollapsed()
meth public int getEndOffset()
meth public int getFoldCount()
meth public int getFoldIndex(org.netbeans.api.editor.fold.Fold)
meth public int getGuardedEnd()
meth public int getGuardedStart()
meth public int getStartOffset()
meth public java.lang.String getDescription()
meth public java.lang.String toString()
meth public org.netbeans.api.editor.fold.Fold getFold(int)
meth public org.netbeans.api.editor.fold.Fold getParent()
meth public org.netbeans.api.editor.fold.FoldHierarchy getHierarchy()
meth public org.netbeans.api.editor.fold.FoldType getType()
supr java.lang.Object
hfds DEFAULT_DESCRIPTION,EMPTY_FOLD_ARRAY,FLAG_COLLAPSED,FLAG_END_DAMAGED,FLAG_START_DAMAGED,LOG,children,description,endGuardedLength,endPos,extraInfo,flags,operation,parent,rawIndex,startGuardedLength,startPos,type

CLSS public final org.netbeans.api.editor.fold.FoldHierarchy
fld public final static org.netbeans.api.editor.fold.FoldType ROOT_FOLD_TYPE
meth public boolean isActive()
meth public java.lang.String toString()
meth public javax.swing.text.JTextComponent getComponent()
meth public org.netbeans.api.editor.fold.Fold getRootFold()
meth public static org.netbeans.api.editor.fold.FoldHierarchy get(javax.swing.text.JTextComponent)
meth public void addFoldHierarchyListener(org.netbeans.api.editor.fold.FoldHierarchyListener)
meth public void collapse(java.util.Collection)
meth public void collapse(org.netbeans.api.editor.fold.Fold)
meth public void expand(java.util.Collection)
meth public void expand(org.netbeans.api.editor.fold.Fold)
meth public void lock()
meth public void removeFoldHierarchyListener(org.netbeans.api.editor.fold.FoldHierarchyListener)
meth public void render(java.lang.Runnable)
meth public void toggle(org.netbeans.api.editor.fold.Fold)
meth public void unlock()
supr java.lang.Object
hfds apiPackageAccessorRegistered,execution
hcls ApiPackageAccessorImpl

CLSS public final org.netbeans.api.editor.fold.FoldHierarchyEvent
meth public int getAddedFoldCount()
meth public int getAffectedEndOffset()
meth public int getAffectedStartOffset()
meth public int getFoldStateChangeCount()
meth public int getRemovedFoldCount()
meth public java.lang.String toString()
meth public org.netbeans.api.editor.fold.Fold getAddedFold(int)
meth public org.netbeans.api.editor.fold.Fold getRemovedFold(int)
meth public org.netbeans.api.editor.fold.FoldStateChange getFoldStateChange(int)
supr java.util.EventObject
hfds addedFolds,affectedEndOffset,affectedStartOffset,foldStateChanges,removedFolds

CLSS public abstract interface org.netbeans.api.editor.fold.FoldHierarchyListener
intf java.util.EventListener
meth public abstract void foldHierarchyChanged(org.netbeans.api.editor.fold.FoldHierarchyEvent)

CLSS public final org.netbeans.api.editor.fold.FoldStateChange
meth public boolean isCollapsedChanged()
meth public boolean isDescriptionChanged()
meth public boolean isEndOffsetChanged()
meth public boolean isStartOffsetChanged()
meth public int getOriginalEndOffset()
meth public int getOriginalStartOffset()
meth public java.lang.String toString()
meth public org.netbeans.api.editor.fold.Fold getFold()
supr java.lang.Object
hfds COLLAPSED_CHANGED_BIT,DESCRIPTION_CHANGED_BIT,END_OFFSET_CHANGED_BIT,START_OFFSET_CHANGED_BIT,fold,originalEndOffset,originalStartOffset,stateChangeBits

CLSS public final org.netbeans.api.editor.fold.FoldTemplate
cons public init(int,int,java.lang.String)
fld public final static java.lang.String CONTENT_PLACEHOLDER
fld public final static org.netbeans.api.editor.fold.FoldTemplate DEFAULT
fld public final static org.netbeans.api.editor.fold.FoldTemplate DEFAULT_BLOCK
meth public int getGuardedEnd()
meth public int getGuardedStart()
meth public java.lang.String getDescription()
supr java.lang.Object
hfds displayText,guardedEnd,guardedStart

CLSS public final org.netbeans.api.editor.fold.FoldType
cons public init(java.lang.String)
 anno 0 java.lang.Deprecated()
fld public final static org.netbeans.api.editor.fold.FoldType CODE_BLOCK
fld public final static org.netbeans.api.editor.fold.FoldType COMMENT
fld public final static org.netbeans.api.editor.fold.FoldType DOCUMENTATION
fld public final static org.netbeans.api.editor.fold.FoldType IMPORT
fld public final static org.netbeans.api.editor.fold.FoldType INITIAL_COMMENT
fld public final static org.netbeans.api.editor.fold.FoldType MEMBER
fld public final static org.netbeans.api.editor.fold.FoldType NESTED
fld public final static org.netbeans.api.editor.fold.FoldType TAG
fld public final static org.netbeans.api.editor.fold.FoldType USER
innr public abstract interface static Domain
meth public boolean accepts(org.netbeans.api.editor.fold.FoldType)
 anno 0 java.lang.Deprecated()
meth public boolean isKindOf(org.netbeans.api.editor.fold.FoldType)
meth public java.lang.String code()
meth public java.lang.String getLabel()
meth public java.lang.String toString()
meth public org.netbeans.api.editor.fold.FoldTemplate getTemplate()
meth public org.netbeans.api.editor.fold.FoldType derive(java.lang.String,java.lang.String,org.netbeans.api.editor.fold.FoldTemplate)
meth public org.netbeans.api.editor.fold.FoldType override(java.lang.String,org.netbeans.api.editor.fold.FoldTemplate)
meth public org.netbeans.api.editor.fold.FoldType parent()
meth public static org.netbeans.api.editor.fold.FoldType create(java.lang.String,java.lang.String,org.netbeans.api.editor.fold.FoldTemplate)
supr java.lang.Object
hfds code,label,parent,template

CLSS public abstract interface static org.netbeans.api.editor.fold.FoldType$Domain
 outer org.netbeans.api.editor.fold.FoldType
meth public abstract java.util.Collection<org.netbeans.api.editor.fold.FoldType> values()
meth public abstract org.netbeans.api.editor.fold.FoldType valueOf(java.lang.String)
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public final org.netbeans.api.editor.fold.FoldUtilities
meth public static boolean containsOffset(org.netbeans.api.editor.fold.Fold,int)
meth public static boolean isAutoCollapsed(org.netbeans.api.editor.fold.FoldType,org.netbeans.api.editor.fold.FoldHierarchy)
meth public static boolean isEmpty(org.netbeans.api.editor.fold.Fold)
meth public static boolean isFoldingEnabled(java.lang.String)
meth public static boolean isRootFold(org.netbeans.api.editor.fold.Fold)
meth public static int findFoldEndIndex(org.netbeans.api.editor.fold.Fold,int)
meth public static int findFoldStartIndex(org.netbeans.api.editor.fold.Fold,int)
meth public static java.util.Iterator collapsedFoldIterator(org.netbeans.api.editor.fold.FoldHierarchy,int,int)
meth public static java.util.List childrenAsList(org.netbeans.api.editor.fold.Fold)
meth public static java.util.List childrenAsList(org.netbeans.api.editor.fold.Fold,int,int)
meth public static java.util.List find(org.netbeans.api.editor.fold.Fold,java.util.Collection)
meth public static java.util.List find(org.netbeans.api.editor.fold.Fold,org.netbeans.api.editor.fold.FoldType)
meth public static java.util.List findRecursive(org.netbeans.api.editor.fold.Fold)
meth public static java.util.List findRecursive(org.netbeans.api.editor.fold.Fold,java.util.Collection)
meth public static java.util.List findRecursive(org.netbeans.api.editor.fold.Fold,org.netbeans.api.editor.fold.FoldType)
meth public static org.netbeans.api.editor.fold.Fold findCollapsedFold(org.netbeans.api.editor.fold.FoldHierarchy,int,int)
meth public static org.netbeans.api.editor.fold.Fold findNearestFold(org.netbeans.api.editor.fold.FoldHierarchy,int)
meth public static org.netbeans.api.editor.fold.Fold findOffsetFold(org.netbeans.api.editor.fold.FoldHierarchy,int)
meth public static org.netbeans.api.editor.fold.FoldType$Domain getFoldTypes(java.lang.String)
meth public static org.netbeans.api.editor.fold.Fold[] childrenToArray(org.netbeans.api.editor.fold.Fold)
meth public static org.netbeans.api.editor.fold.Fold[] childrenToArray(org.netbeans.api.editor.fold.Fold,int,int)
meth public static void collapse(org.netbeans.api.editor.fold.FoldHierarchy,java.util.Collection)
meth public static void collapse(org.netbeans.api.editor.fold.FoldHierarchy,org.netbeans.api.editor.fold.FoldType)
meth public static void collapseAll(org.netbeans.api.editor.fold.FoldHierarchy)
meth public static void expand(org.netbeans.api.editor.fold.FoldHierarchy,java.util.Collection)
meth public static void expand(org.netbeans.api.editor.fold.FoldHierarchy,org.netbeans.api.editor.fold.FoldType)
meth public static void expandAll(org.netbeans.api.editor.fold.FoldHierarchy)
supr java.lang.Object

CLSS public final org.netbeans.api.editor.fold.FoldingSupport
meth public static org.netbeans.spi.editor.fold.ContentReader contentReader(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public static org.netbeans.spi.editor.fold.ContentReader$Factory contentReaderFactory(java.util.Map)
meth public static org.netbeans.spi.editor.fold.FoldManager userFoldManager(java.lang.String)
meth public static org.netbeans.spi.editor.fold.FoldManagerFactory userFoldManagerFactory(java.util.Map)
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.editor.fold.ContentReader
innr public abstract interface static Factory
meth public abstract java.lang.CharSequence read(javax.swing.text.Document,org.netbeans.api.editor.fold.Fold,org.netbeans.api.editor.fold.FoldTemplate) throws javax.swing.text.BadLocationException

CLSS public abstract interface static org.netbeans.spi.editor.fold.ContentReader$Factory
 outer org.netbeans.spi.editor.fold.ContentReader
 anno 0 org.netbeans.spi.editor.mimelookup.MimeLocation(java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass=class org.netbeans.spi.editor.mimelookup.InstanceProvider, java.lang.String subfolderName="FoldManager")
meth public abstract org.netbeans.spi.editor.fold.ContentReader createReader(org.netbeans.api.editor.fold.FoldType)

CLSS public abstract interface org.netbeans.spi.editor.fold.FoldHierarchyMonitor
meth public abstract void foldsAttached(org.netbeans.api.editor.fold.FoldHierarchy)

CLSS public final org.netbeans.spi.editor.fold.FoldHierarchyTransaction
meth public void commit()
supr java.lang.Object
hfds impl

CLSS public final org.netbeans.spi.editor.fold.FoldInfo
meth public int getEnd()
meth public int getStart()
meth public java.lang.Boolean getCollapsed()
meth public java.lang.Object getExtraInfo()
meth public java.lang.String getDescriptionOverride()
meth public java.lang.String toString()
meth public org.netbeans.api.editor.fold.FoldTemplate getTemplate()
meth public org.netbeans.api.editor.fold.FoldType getType()
meth public org.netbeans.spi.editor.fold.FoldInfo attach(java.lang.Object)
meth public org.netbeans.spi.editor.fold.FoldInfo collapsed(boolean)
meth public org.netbeans.spi.editor.fold.FoldInfo withDescription(java.lang.String)
meth public org.netbeans.spi.editor.fold.FoldInfo withTemplate(org.netbeans.api.editor.fold.FoldTemplate)
meth public static org.netbeans.spi.editor.fold.FoldInfo range(int,int,org.netbeans.api.editor.fold.FoldType)
supr java.lang.Object
hfds collapsed,description,end,extraInfo,start,template,type

CLSS public abstract interface org.netbeans.spi.editor.fold.FoldManager
meth public abstract void changedUpdate(javax.swing.event.DocumentEvent,org.netbeans.spi.editor.fold.FoldHierarchyTransaction)
meth public abstract void expandNotify(org.netbeans.api.editor.fold.Fold)
meth public abstract void init(org.netbeans.spi.editor.fold.FoldOperation)
meth public abstract void initFolds(org.netbeans.spi.editor.fold.FoldHierarchyTransaction)
meth public abstract void insertUpdate(javax.swing.event.DocumentEvent,org.netbeans.spi.editor.fold.FoldHierarchyTransaction)
meth public abstract void release()
meth public abstract void removeDamagedNotify(org.netbeans.api.editor.fold.Fold)
meth public abstract void removeEmptyNotify(org.netbeans.api.editor.fold.Fold)
meth public abstract void removeUpdate(javax.swing.event.DocumentEvent,org.netbeans.spi.editor.fold.FoldHierarchyTransaction)

CLSS public abstract interface org.netbeans.spi.editor.fold.FoldManagerFactory
 anno 0 org.netbeans.spi.editor.mimelookup.MimeLocation(java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass=class org.netbeans.spi.editor.mimelookup.InstanceProvider, java.lang.String subfolderName="FoldManager")
meth public abstract org.netbeans.spi.editor.fold.FoldManager createFoldManager()

CLSS public final org.netbeans.spi.editor.fold.FoldOperation
meth public boolean isAddedOrBlocked(org.netbeans.api.editor.fold.Fold)
meth public boolean isBlocked(org.netbeans.api.editor.fold.Fold)
meth public boolean isEndDamaged(org.netbeans.api.editor.fold.Fold)
meth public boolean isReleased()
meth public boolean isStartDamaged(org.netbeans.api.editor.fold.Fold)
meth public boolean owns(org.netbeans.api.editor.fold.Fold)
meth public java.lang.Object getExtraInfo(org.netbeans.api.editor.fold.Fold)
meth public java.util.Iterator<org.netbeans.api.editor.fold.Fold> foldIterator()
meth public java.util.Map<org.netbeans.spi.editor.fold.FoldInfo,org.netbeans.api.editor.fold.Fold> update(java.util.Collection<org.netbeans.spi.editor.fold.FoldInfo>,java.util.Collection<org.netbeans.api.editor.fold.Fold>,java.util.Collection<org.netbeans.spi.editor.fold.FoldInfo>) throws javax.swing.text.BadLocationException
meth public org.netbeans.api.editor.fold.Fold addToHierarchy(org.netbeans.api.editor.fold.FoldType,int,int,java.lang.Boolean,org.netbeans.api.editor.fold.FoldTemplate,java.lang.String,java.lang.Object,org.netbeans.spi.editor.fold.FoldHierarchyTransaction) throws javax.swing.text.BadLocationException
meth public org.netbeans.api.editor.fold.Fold addToHierarchy(org.netbeans.api.editor.fold.FoldType,java.lang.String,boolean,int,int,int,int,java.lang.Object,org.netbeans.spi.editor.fold.FoldHierarchyTransaction) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
meth public org.netbeans.api.editor.fold.FoldHierarchy getHierarchy()
meth public org.netbeans.spi.editor.fold.FoldHierarchyTransaction openTransaction()
meth public static boolean isBoundsValid(int,int,int,int)
meth public void removeFromHierarchy(org.netbeans.api.editor.fold.Fold,org.netbeans.spi.editor.fold.FoldHierarchyTransaction)
supr java.lang.Object
hfds impl,spiPackageAccessorRegistered
hcls SpiPackageAccessorImpl

CLSS public abstract interface org.netbeans.spi.editor.fold.FoldTypeProvider
 anno 0 org.netbeans.spi.editor.mimelookup.MimeLocation(java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass=class org.netbeans.spi.editor.mimelookup.InstanceProvider, java.lang.String subfolderName="FoldManager")
meth public abstract boolean inheritable()
meth public abstract java.util.Collection getValues(java.lang.Class)

CLSS public abstract interface !annotation org.netbeans.spi.editor.mimelookup.MimeLocation
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass()
meth public abstract java.lang.String subfolderName()

