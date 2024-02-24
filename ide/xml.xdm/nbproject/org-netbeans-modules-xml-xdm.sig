#Signature file v4.1
#Version 1.57.0

CLSS public abstract interface java.beans.PropertyChangeListener
intf java.util.EventListener
meth public abstract void propertyChange(java.beans.PropertyChangeEvent)

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Cloneable

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

CLSS public abstract interface !annotation java.lang.FunctionalInterface
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface java.lang.Iterable<%0 extends java.lang.Object>
meth public abstract java.util.Iterator<{java.lang.Iterable%0}> iterator()
meth public java.util.Spliterator<{java.lang.Iterable%0}> spliterator()
meth public void forEach(java.util.function.Consumer<? super {java.lang.Iterable%0}>)

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

CLSS public abstract java.util.AbstractCollection<%0 extends java.lang.Object>
cons protected init()
intf java.util.Collection<{java.util.AbstractCollection%0}>
meth public <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public abstract int size()
meth public abstract java.util.Iterator<{java.util.AbstractCollection%0}> iterator()
meth public boolean add({java.util.AbstractCollection%0})
meth public boolean addAll(java.util.Collection<? extends {java.util.AbstractCollection%0}>)
meth public boolean contains(java.lang.Object)
meth public boolean containsAll(java.util.Collection<?>)
meth public boolean isEmpty()
meth public boolean remove(java.lang.Object)
meth public boolean removeAll(java.util.Collection<?>)
meth public boolean retainAll(java.util.Collection<?>)
meth public java.lang.Object[] toArray()
meth public java.lang.String toString()
meth public void clear()
supr java.lang.Object

CLSS public abstract java.util.AbstractMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons protected init()
innr public static SimpleEntry
innr public static SimpleImmutableEntry
intf java.util.Map<{java.util.AbstractMap%0},{java.util.AbstractMap%1}>
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public abstract java.util.Set<java.util.Map$Entry<{java.util.AbstractMap%0},{java.util.AbstractMap%1}>> entrySet()
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public int hashCode()
meth public int size()
meth public java.lang.String toString()
meth public java.util.Collection<{java.util.AbstractMap%1}> values()
meth public java.util.Set<{java.util.AbstractMap%0}> keySet()
meth public void clear()
meth public void putAll(java.util.Map<? extends {java.util.AbstractMap%0},? extends {java.util.AbstractMap%1}>)
meth public {java.util.AbstractMap%1} get(java.lang.Object)
meth public {java.util.AbstractMap%1} put({java.util.AbstractMap%0},{java.util.AbstractMap%1})
meth public {java.util.AbstractMap%1} remove(java.lang.Object)
supr java.lang.Object

CLSS public abstract java.util.AbstractSet<%0 extends java.lang.Object>
cons protected init()
intf java.util.Set<{java.util.AbstractSet%0}>
meth public boolean equals(java.lang.Object)
meth public boolean removeAll(java.util.Collection<?>)
meth public int hashCode()
supr java.util.AbstractCollection<{java.util.AbstractSet%0}>

CLSS public abstract interface java.util.Collection<%0 extends java.lang.Object>
intf java.lang.Iterable<{java.util.Collection%0}>
meth public abstract <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public abstract boolean add({java.util.Collection%0})
meth public abstract boolean addAll(java.util.Collection<? extends {java.util.Collection%0}>)
meth public abstract boolean contains(java.lang.Object)
meth public abstract boolean containsAll(java.util.Collection<?>)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isEmpty()
meth public abstract boolean remove(java.lang.Object)
meth public abstract boolean removeAll(java.util.Collection<?>)
meth public abstract boolean retainAll(java.util.Collection<?>)
meth public abstract int hashCode()
meth public abstract int size()
meth public abstract java.lang.Object[] toArray()
meth public abstract java.util.Iterator<{java.util.Collection%0}> iterator()
meth public abstract void clear()
meth public boolean removeIf(java.util.function.Predicate<? super {java.util.Collection%0}>)
meth public java.util.Spliterator<{java.util.Collection%0}> spliterator()
meth public java.util.stream.Stream<{java.util.Collection%0}> parallelStream()
meth public java.util.stream.Stream<{java.util.Collection%0}> stream()

CLSS public abstract interface java.util.Comparator<%0 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
meth public <%0 extends java.lang.Comparable<? super {%%0}>> java.util.Comparator<{java.util.Comparator%0}> thenComparing(java.util.function.Function<? super {java.util.Comparator%0},? extends {%%0}>)
meth public <%0 extends java.lang.Object> java.util.Comparator<{java.util.Comparator%0}> thenComparing(java.util.function.Function<? super {java.util.Comparator%0},? extends {%%0}>,java.util.Comparator<? super {%%0}>)
meth public abstract boolean equals(java.lang.Object)
meth public abstract int compare({java.util.Comparator%0},{java.util.Comparator%0})
meth public java.util.Comparator<{java.util.Comparator%0}> reversed()
meth public java.util.Comparator<{java.util.Comparator%0}> thenComparing(java.util.Comparator<? super {java.util.Comparator%0}>)
meth public java.util.Comparator<{java.util.Comparator%0}> thenComparingDouble(java.util.function.ToDoubleFunction<? super {java.util.Comparator%0}>)
meth public java.util.Comparator<{java.util.Comparator%0}> thenComparingInt(java.util.function.ToIntFunction<? super {java.util.Comparator%0}>)
meth public java.util.Comparator<{java.util.Comparator%0}> thenComparingLong(java.util.function.ToLongFunction<? super {java.util.Comparator%0}>)
meth public static <%0 extends java.lang.Comparable<? super {%%0}>> java.util.Comparator<{%%0}> naturalOrder()
meth public static <%0 extends java.lang.Comparable<? super {%%0}>> java.util.Comparator<{%%0}> reverseOrder()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Comparable<? super {%%1}>> java.util.Comparator<{%%0}> comparing(java.util.function.Function<? super {%%0},? extends {%%1}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Comparator<{%%0}> comparing(java.util.function.Function<? super {%%0},? extends {%%1}>,java.util.Comparator<? super {%%1}>)
meth public static <%0 extends java.lang.Object> java.util.Comparator<{%%0}> comparingDouble(java.util.function.ToDoubleFunction<? super {%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Comparator<{%%0}> comparingInt(java.util.function.ToIntFunction<? super {%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Comparator<{%%0}> comparingLong(java.util.function.ToLongFunction<? super {%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Comparator<{%%0}> nullsFirst(java.util.Comparator<? super {%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Comparator<{%%0}> nullsLast(java.util.Comparator<? super {%%0}>)

CLSS public abstract interface java.util.EventListener

CLSS public java.util.HashMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
cons public init(int)
cons public init(int,float)
cons public init(java.util.Map<? extends {java.util.HashMap%0},? extends {java.util.HashMap%1}>)
intf java.io.Serializable
intf java.lang.Cloneable
intf java.util.Map<{java.util.HashMap%0},{java.util.HashMap%1}>
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean isEmpty()
meth public boolean remove(java.lang.Object,java.lang.Object)
meth public boolean replace({java.util.HashMap%0},{java.util.HashMap%1},{java.util.HashMap%1})
meth public int size()
meth public java.lang.Object clone()
meth public java.util.Collection<{java.util.HashMap%1}> values()
meth public java.util.Set<java.util.Map$Entry<{java.util.HashMap%0},{java.util.HashMap%1}>> entrySet()
meth public java.util.Set<{java.util.HashMap%0}> keySet()
meth public void clear()
meth public void forEach(java.util.function.BiConsumer<? super {java.util.HashMap%0},? super {java.util.HashMap%1}>)
meth public void putAll(java.util.Map<? extends {java.util.HashMap%0},? extends {java.util.HashMap%1}>)
meth public void replaceAll(java.util.function.BiFunction<? super {java.util.HashMap%0},? super {java.util.HashMap%1},? extends {java.util.HashMap%1}>)
meth public {java.util.HashMap%1} compute({java.util.HashMap%0},java.util.function.BiFunction<? super {java.util.HashMap%0},? super {java.util.HashMap%1},? extends {java.util.HashMap%1}>)
meth public {java.util.HashMap%1} computeIfAbsent({java.util.HashMap%0},java.util.function.Function<? super {java.util.HashMap%0},? extends {java.util.HashMap%1}>)
meth public {java.util.HashMap%1} computeIfPresent({java.util.HashMap%0},java.util.function.BiFunction<? super {java.util.HashMap%0},? super {java.util.HashMap%1},? extends {java.util.HashMap%1}>)
meth public {java.util.HashMap%1} get(java.lang.Object)
meth public {java.util.HashMap%1} getOrDefault(java.lang.Object,{java.util.HashMap%1})
meth public {java.util.HashMap%1} merge({java.util.HashMap%0},{java.util.HashMap%1},java.util.function.BiFunction<? super {java.util.HashMap%1},? super {java.util.HashMap%1},? extends {java.util.HashMap%1}>)
meth public {java.util.HashMap%1} put({java.util.HashMap%0},{java.util.HashMap%1})
meth public {java.util.HashMap%1} putIfAbsent({java.util.HashMap%0},{java.util.HashMap%1})
meth public {java.util.HashMap%1} remove(java.lang.Object)
meth public {java.util.HashMap%1} replace({java.util.HashMap%0},{java.util.HashMap%1})
supr java.util.AbstractMap<{java.util.HashMap%0},{java.util.HashMap%1}>

CLSS public java.util.HashSet<%0 extends java.lang.Object>
cons public init()
cons public init(int)
cons public init(int,float)
cons public init(java.util.Collection<? extends {java.util.HashSet%0}>)
intf java.io.Serializable
intf java.lang.Cloneable
intf java.util.Set<{java.util.HashSet%0}>
meth public boolean add({java.util.HashSet%0})
meth public boolean contains(java.lang.Object)
meth public boolean isEmpty()
meth public boolean remove(java.lang.Object)
meth public int size()
meth public java.lang.Object clone()
meth public java.util.Iterator<{java.util.HashSet%0}> iterator()
meth public java.util.Spliterator<{java.util.HashSet%0}> spliterator()
meth public void clear()
supr java.util.AbstractSet<{java.util.HashSet%0}>

CLSS public abstract interface java.util.Map<%0 extends java.lang.Object, %1 extends java.lang.Object>
innr public abstract interface static Entry
meth public abstract boolean containsKey(java.lang.Object)
meth public abstract boolean containsValue(java.lang.Object)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isEmpty()
meth public abstract int hashCode()
meth public abstract int size()
meth public abstract java.util.Collection<{java.util.Map%1}> values()
meth public abstract java.util.Set<java.util.Map$Entry<{java.util.Map%0},{java.util.Map%1}>> entrySet()
meth public abstract java.util.Set<{java.util.Map%0}> keySet()
meth public abstract void clear()
meth public abstract void putAll(java.util.Map<? extends {java.util.Map%0},? extends {java.util.Map%1}>)
meth public abstract {java.util.Map%1} get(java.lang.Object)
meth public abstract {java.util.Map%1} put({java.util.Map%0},{java.util.Map%1})
meth public abstract {java.util.Map%1} remove(java.lang.Object)
meth public boolean remove(java.lang.Object,java.lang.Object)
meth public boolean replace({java.util.Map%0},{java.util.Map%1},{java.util.Map%1})
meth public void forEach(java.util.function.BiConsumer<? super {java.util.Map%0},? super {java.util.Map%1}>)
meth public void replaceAll(java.util.function.BiFunction<? super {java.util.Map%0},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} compute({java.util.Map%0},java.util.function.BiFunction<? super {java.util.Map%0},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} computeIfAbsent({java.util.Map%0},java.util.function.Function<? super {java.util.Map%0},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} computeIfPresent({java.util.Map%0},java.util.function.BiFunction<? super {java.util.Map%0},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} getOrDefault(java.lang.Object,{java.util.Map%1})
meth public {java.util.Map%1} merge({java.util.Map%0},{java.util.Map%1},java.util.function.BiFunction<? super {java.util.Map%1},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} putIfAbsent({java.util.Map%0},{java.util.Map%1})
meth public {java.util.Map%1} replace({java.util.Map%0},{java.util.Map%1})

CLSS public abstract interface java.util.Set<%0 extends java.lang.Object>
intf java.util.Collection<{java.util.Set%0}>
meth public abstract <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public abstract boolean add({java.util.Set%0})
meth public abstract boolean addAll(java.util.Collection<? extends {java.util.Set%0}>)
meth public abstract boolean contains(java.lang.Object)
meth public abstract boolean containsAll(java.util.Collection<?>)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isEmpty()
meth public abstract boolean remove(java.lang.Object)
meth public abstract boolean removeAll(java.util.Collection<?>)
meth public abstract boolean retainAll(java.util.Collection<?>)
meth public abstract int hashCode()
meth public abstract int size()
meth public abstract java.lang.Object[] toArray()
meth public abstract java.util.Iterator<{java.util.Set%0}> iterator()
meth public abstract void clear()
meth public java.util.Spliterator<{java.util.Set%0}> spliterator()

CLSS public abstract interface javax.xml.namespace.NamespaceContext
meth public abstract java.lang.String getNamespaceURI(java.lang.String)
meth public abstract java.lang.String getPrefix(java.lang.String)
meth public abstract java.util.Iterator getPrefixes(java.lang.String)

CLSS public abstract org.netbeans.modules.xml.xam.ModelAccess
cons public init()
meth public abstract org.netbeans.modules.xml.xam.Model$State sync() throws java.io.IOException
meth public abstract void addUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public abstract void finishUndoRedo()
meth public abstract void flush()
meth public abstract void prepareForUndoRedo()
meth public abstract void removeUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public boolean isAutoSync()
meth public long dirtyIntervalMillis()
meth public void prepareSync()
meth public void setAutoSync(boolean)
meth public void unsetDirty()
supr java.lang.Object
hfds autoSync

CLSS public abstract org.netbeans.modules.xml.xam.dom.DocumentModelAccess
cons public init()
innr public abstract interface static NodeUpdater
meth public abstract boolean areSameNodes(org.w3c.dom.Node,org.w3c.dom.Node)
meth public abstract int findPosition(org.w3c.dom.Node)
meth public abstract int getElementIndexOf(org.w3c.dom.Node,org.w3c.dom.Element)
meth public abstract java.lang.String getXPath(org.w3c.dom.Document,org.w3c.dom.Element)
meth public abstract java.lang.String getXmlFragment(org.w3c.dom.Element)
meth public abstract java.util.List<org.w3c.dom.Element> getPathFromRoot(org.w3c.dom.Document,org.w3c.dom.Element)
meth public abstract java.util.List<org.w3c.dom.Node> findNodes(org.w3c.dom.Document,java.lang.String)
meth public abstract java.util.Map<javax.xml.namespace.QName,java.lang.String> getAttributeMap(org.w3c.dom.Element)
meth public abstract org.netbeans.modules.xml.xam.dom.ElementIdentity getElementIdentity()
meth public abstract org.w3c.dom.Document getDocumentRoot()
meth public abstract org.w3c.dom.Element duplicate(org.w3c.dom.Element)
meth public abstract org.w3c.dom.Element getContainingElement(int)
meth public abstract org.w3c.dom.Node findNode(org.w3c.dom.Document,java.lang.String)
meth public abstract org.w3c.dom.Node getNewEventNode(java.beans.PropertyChangeEvent)
meth public abstract org.w3c.dom.Node getNewEventParentNode(java.beans.PropertyChangeEvent)
meth public abstract org.w3c.dom.Node getOldEventNode(java.beans.PropertyChangeEvent)
meth public abstract org.w3c.dom.Node getOldEventParentNode(java.beans.PropertyChangeEvent)
meth public abstract void addMergeEventHandler(java.beans.PropertyChangeListener)
meth public abstract void appendChild(org.w3c.dom.Node,org.w3c.dom.Node,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public abstract void insertBefore(org.w3c.dom.Node,org.w3c.dom.Node,org.w3c.dom.Node,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public abstract void removeAttribute(org.w3c.dom.Element,java.lang.String,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public abstract void removeChild(org.w3c.dom.Node,org.w3c.dom.Node,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public abstract void removeMergeEventHandler(java.beans.PropertyChangeListener)
meth public abstract void replaceChild(org.w3c.dom.Node,org.w3c.dom.Node,org.w3c.dom.Node,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public abstract void setAttribute(org.w3c.dom.Element,java.lang.String,java.lang.String,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public abstract void setPrefix(org.w3c.dom.Element,java.lang.String)
meth public abstract void setText(org.w3c.dom.Element,java.lang.String,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public abstract void setXmlFragment(org.w3c.dom.Element,java.lang.String,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater) throws java.io.IOException
meth public java.lang.String getCurrentDocumentText()
meth public java.lang.String getXmlFragmentInclusive(org.w3c.dom.Element)
meth public java.lang.String lookupNamespaceURI(org.w3c.dom.Node,java.util.List<? extends org.w3c.dom.Node>)
meth public java.lang.String normalizeUndefinedAttributeValue(java.lang.String)
meth public long dirtyIntervalMillis()
meth public org.netbeans.modules.xml.xam.dom.AbstractDocumentModel getModel()
meth public void addQNameValuedAttributes(java.util.Map<javax.xml.namespace.QName,java.util.List<javax.xml.namespace.QName>>)
meth public void removeChildren(org.w3c.dom.Node,java.util.Collection<org.w3c.dom.Node>,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public void reorderChildren(org.w3c.dom.Element,int[],org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public void setDirty()
meth public void unsetDirty()
supr org.netbeans.modules.xml.xam.ModelAccess
hfds dirtyTimeMillis

CLSS public abstract org.netbeans.modules.xml.xam.dom.DocumentModelAccess2
cons public init()
meth public abstract int findEndPosition(org.w3c.dom.Node)
supr org.netbeans.modules.xml.xam.dom.DocumentModelAccess

CLSS public abstract interface org.netbeans.modules.xml.xam.dom.ElementIdentity
meth public abstract boolean compareElement(org.w3c.dom.Element,org.w3c.dom.Element,org.w3c.dom.Document,org.w3c.dom.Document)
meth public abstract java.util.List getIdentifiers()
meth public abstract void addIdentifier(java.lang.String)

CLSS public abstract interface org.netbeans.modules.xml.xam.spi.DocumentModelAccessProvider
intf org.netbeans.modules.xml.xam.spi.ModelAccessProvider
meth public abstract javax.swing.text.Document loadSwingDocument(java.io.InputStream) throws java.io.IOException,javax.swing.text.BadLocationException
meth public abstract org.netbeans.modules.xml.xam.dom.DocumentModelAccess createModelAccess(org.netbeans.modules.xml.xam.dom.AbstractDocumentModel)

CLSS public abstract interface org.netbeans.modules.xml.xam.spi.ModelAccessProvider
meth public abstract java.lang.Object getModelSourceKey(org.netbeans.modules.xml.xam.ModelSource)

CLSS public org.netbeans.modules.xml.xdm.XDMModel
cons public init(org.netbeans.modules.xml.xam.ModelSource)
fld public final static java.lang.String DEFAULT_INDENT = "    "
fld public final static java.lang.String PROP_ADDED = "added"
fld public final static java.lang.String PROP_DELETED = "deleted"
fld public final static java.lang.String PROP_MODIFIED = "modified"
innr public final static !enum Status
meth public int getNextNodeId()
meth public java.lang.String getCurrentDocumentText()
meth public java.lang.String getIndentation()
meth public java.util.List<org.netbeans.modules.xml.xdm.nodes.Node> add(org.netbeans.modules.xml.xdm.nodes.Node,org.netbeans.modules.xml.xdm.nodes.Node,int)
meth public java.util.List<org.netbeans.modules.xml.xdm.nodes.Node> append(org.netbeans.modules.xml.xdm.nodes.Node,org.netbeans.modules.xml.xdm.nodes.Node)
meth public java.util.List<org.netbeans.modules.xml.xdm.nodes.Node> delete(org.netbeans.modules.xml.xdm.nodes.Node)
meth public java.util.List<org.netbeans.modules.xml.xdm.nodes.Node> insertBefore(org.netbeans.modules.xml.xdm.nodes.Node,org.netbeans.modules.xml.xdm.nodes.Node,org.netbeans.modules.xml.xdm.nodes.Node)
meth public java.util.List<org.netbeans.modules.xml.xdm.nodes.Node> modify(org.netbeans.modules.xml.xdm.nodes.Node,org.netbeans.modules.xml.xdm.nodes.Node)
meth public java.util.List<org.netbeans.modules.xml.xdm.nodes.Node> remove(org.netbeans.modules.xml.xdm.nodes.Node,org.netbeans.modules.xml.xdm.nodes.Node)
meth public java.util.List<org.netbeans.modules.xml.xdm.nodes.Node> removeAttribute(org.netbeans.modules.xml.xdm.nodes.Element,java.lang.String)
meth public java.util.List<org.netbeans.modules.xml.xdm.nodes.Node> removeChildNodes(org.netbeans.modules.xml.xdm.nodes.Node,java.util.Collection<org.netbeans.modules.xml.xdm.nodes.Node>)
meth public java.util.List<org.netbeans.modules.xml.xdm.nodes.Node> reorder(org.netbeans.modules.xml.xdm.nodes.Node,org.netbeans.modules.xml.xdm.nodes.Node,int)
meth public java.util.List<org.netbeans.modules.xml.xdm.nodes.Node> reorderChildren(org.netbeans.modules.xml.xdm.nodes.Node,int[])
meth public java.util.List<org.netbeans.modules.xml.xdm.nodes.Node> replaceChild(org.netbeans.modules.xml.xdm.nodes.Node,org.netbeans.modules.xml.xdm.nodes.Node,org.netbeans.modules.xml.xdm.nodes.Node)
meth public java.util.List<org.netbeans.modules.xml.xdm.nodes.Node> setAttribute(org.netbeans.modules.xml.xdm.nodes.Element,java.lang.String,java.lang.String)
meth public java.util.List<org.netbeans.modules.xml.xdm.nodes.Node> setTextValue(org.netbeans.modules.xml.xdm.nodes.Node,java.lang.String)
meth public java.util.List<org.netbeans.modules.xml.xdm.nodes.Node> setXmlFragmentText(org.netbeans.modules.xml.xdm.nodes.Element,java.lang.String) throws java.io.IOException
meth public java.util.Map<javax.xml.namespace.QName,java.util.List<javax.xml.namespace.QName>> getQNameValuedAttributes()
meth public org.netbeans.modules.xml.xam.dom.ElementIdentity getElementIdentity()
meth public org.netbeans.modules.xml.xdm.XDMModel$Status getStatus()
meth public org.netbeans.modules.xml.xdm.nodes.Document getCurrentDocument()
meth public org.netbeans.modules.xml.xdm.nodes.Document getDocument()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public void flush()
meth public void mergeDiff(java.util.List<org.netbeans.modules.xml.xdm.diff.Difference>)
meth public void prepareSync()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removeUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public void setDocument(org.netbeans.modules.xml.xdm.nodes.Document)
meth public void setElementIdentity(org.netbeans.modules.xml.xam.dom.ElementIdentity)
meth public void setIndentation(java.lang.String)
meth public void setPretty(boolean)
meth public void setQNameValuedAttributes(java.util.Map<javax.xml.namespace.QName,java.util.List<javax.xml.namespace.QName>>)
meth public void sync() throws java.io.IOException
supr java.lang.Object
hfds currentDocument,currentIndent,eID,fireUndoEvents,indentInitialized,nodeCount,parser,pcs,preparation,pretty,qnameValuedAttributesByElementMap,source,status,ues
hcls CheckIOExceptionUpdater,MutationType,Updater

CLSS public final static !enum org.netbeans.modules.xml.xdm.XDMModel$Status
 outer org.netbeans.modules.xml.xdm.XDMModel
fld public final static org.netbeans.modules.xml.xdm.XDMModel$Status BROKEN
fld public final static org.netbeans.modules.xml.xdm.XDMModel$Status PARSING
fld public final static org.netbeans.modules.xml.xdm.XDMModel$Status STABLE
fld public final static org.netbeans.modules.xml.xdm.XDMModel$Status UNPARSED
meth public static org.netbeans.modules.xml.xdm.XDMModel$Status valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.xdm.XDMModel$Status[] values()
supr java.lang.Enum<org.netbeans.modules.xml.xdm.XDMModel$Status>

CLSS public org.netbeans.modules.xml.xdm.diff.Add
cons public init(org.netbeans.modules.xml.xdm.diff.NodeInfo$NodeType,java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>,java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>,org.netbeans.modules.xml.xdm.nodes.Node,int)
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.xml.xdm.nodes.Node> getNewAncestors()
meth public org.netbeans.modules.xml.xdm.nodes.Node getNewParent()
meth public void setNewParent(org.netbeans.modules.xml.xdm.nodes.Node)
supr org.netbeans.modules.xml.xdm.diff.Difference

CLSS public org.netbeans.modules.xml.xdm.diff.Change
cons public init(org.netbeans.modules.xml.xdm.diff.NodeInfo$NodeType,java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>,java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>,org.netbeans.modules.xml.xdm.nodes.Node,org.netbeans.modules.xml.xdm.nodes.Node,int,int,java.util.List<org.netbeans.modules.xml.xdm.diff.Change$Type>)
innr public AttributeAdd
innr public AttributeChange
innr public AttributeDelete
innr public AttributeDiff
innr public final static !enum Type
meth public boolean isAttributeChanged()
meth public boolean isPositionChanged()
meth public boolean isTokenChanged()
meth public boolean isValid()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.xml.xdm.diff.Change$AttributeDiff> getAttrChanges()
meth public java.util.List<org.netbeans.modules.xml.xdm.nodes.Node> getNewAncestors()
meth public org.netbeans.modules.xml.xdm.nodes.Node getNewParent()
meth public void addAttrChanges(org.netbeans.modules.xml.xdm.diff.Change$AttributeDiff)
meth public void removeAttrChanges(org.netbeans.modules.xml.xdm.diff.Change$AttributeDiff)
meth public void setNewParent(org.netbeans.modules.xml.xdm.nodes.Node)
supr org.netbeans.modules.xml.xdm.diff.Difference
hfds attrChanges,changes

CLSS public org.netbeans.modules.xml.xdm.diff.Change$AttributeAdd
 outer org.netbeans.modules.xml.xdm.diff.Change
cons public init(org.netbeans.modules.xml.xdm.diff.Change,org.netbeans.modules.xml.xdm.nodes.Attribute,int)
supr org.netbeans.modules.xml.xdm.diff.Change$AttributeDiff

CLSS public org.netbeans.modules.xml.xdm.diff.Change$AttributeChange
 outer org.netbeans.modules.xml.xdm.diff.Change
cons public init(org.netbeans.modules.xml.xdm.diff.Change,org.netbeans.modules.xml.xdm.nodes.Attribute,org.netbeans.modules.xml.xdm.nodes.Attribute,int,int,boolean,boolean)
meth public boolean isPositionChanged()
meth public boolean isTokenChanged()
supr org.netbeans.modules.xml.xdm.diff.Change$AttributeDiff
hfds posChanged,tokenChanged

CLSS public org.netbeans.modules.xml.xdm.diff.Change$AttributeDelete
 outer org.netbeans.modules.xml.xdm.diff.Change
cons public init(org.netbeans.modules.xml.xdm.diff.Change,org.netbeans.modules.xml.xdm.nodes.Attribute,int)
supr org.netbeans.modules.xml.xdm.diff.Change$AttributeDiff

CLSS public org.netbeans.modules.xml.xdm.diff.Change$AttributeDiff
 outer org.netbeans.modules.xml.xdm.diff.Change
cons public init(org.netbeans.modules.xml.xdm.diff.Change,org.netbeans.modules.xml.xdm.nodes.Attribute,org.netbeans.modules.xml.xdm.nodes.Attribute,int,int)
meth public int getNewAttributePosition()
meth public int getOldAttributePosition()
meth public org.netbeans.modules.xml.xdm.nodes.Attribute getNewAttribute()
meth public org.netbeans.modules.xml.xdm.nodes.Attribute getOldAttribute()
supr java.lang.Object
hfds newAttr,newAttrPos,oldAttr,oldAttrPos

CLSS public final static !enum org.netbeans.modules.xml.xdm.diff.Change$Type
 outer org.netbeans.modules.xml.xdm.diff.Change
fld public final static org.netbeans.modules.xml.xdm.diff.Change$Type ATTRIBUTE
fld public final static org.netbeans.modules.xml.xdm.diff.Change$Type NO_CHANGE
fld public final static org.netbeans.modules.xml.xdm.diff.Change$Type POSITION
fld public final static org.netbeans.modules.xml.xdm.diff.Change$Type TOKEN
fld public final static org.netbeans.modules.xml.xdm.diff.Change$Type UNKNOWN
meth public static org.netbeans.modules.xml.xdm.diff.Change$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.xdm.diff.Change$Type[] values()
supr java.lang.Enum<org.netbeans.modules.xml.xdm.diff.Change$Type>
hfds name

CLSS public org.netbeans.modules.xml.xdm.diff.DefaultElementIdentity
cons public init()
intf org.netbeans.modules.xml.xam.dom.ElementIdentity
meth protected boolean compareAttr(org.w3c.dom.Element,org.w3c.dom.Element)
meth protected boolean compareElement(org.w3c.dom.Element,org.w3c.dom.Element,org.w3c.dom.Node,org.w3c.dom.Document,org.w3c.dom.Document)
meth public boolean compareElement(org.w3c.dom.Element,org.w3c.dom.Element,org.w3c.dom.Document,org.w3c.dom.Document)
meth public java.util.List getIdentifiers()
meth public void addIdentifier(java.lang.String)
meth public void clear()
supr java.lang.Object
hfds identifiers

CLSS public org.netbeans.modules.xml.xdm.diff.Delete
cons public init(org.netbeans.modules.xml.xdm.diff.NodeInfo$NodeType,java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>,java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>,org.netbeans.modules.xml.xdm.nodes.Node,int)
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.xml.xdm.nodes.Node> getNewAncestors()
meth public org.netbeans.modules.xml.xdm.nodes.Node getNewParent()
meth public void setNewParent(org.netbeans.modules.xml.xdm.nodes.Node)
supr org.netbeans.modules.xml.xdm.diff.Difference

CLSS public org.netbeans.modules.xml.xdm.diff.DiffFinder
cons protected init()
cons public init(org.netbeans.modules.xml.xam.dom.ElementIdentity)
innr public PairComparator
innr public PairComparator2
meth protected boolean checkAttributesEqual(org.netbeans.modules.xml.xdm.nodes.Element,org.netbeans.modules.xml.xdm.nodes.Element)
meth protected boolean checkTokensEqual(org.netbeans.modules.xml.xdm.nodes.Node,org.netbeans.modules.xml.xdm.nodes.Node)
meth protected boolean compareText(org.netbeans.modules.xml.xdm.nodes.Text,org.netbeans.modules.xml.xdm.nodes.Text)
meth protected boolean compareTextByValue(org.netbeans.modules.xml.xdm.nodes.Text,org.netbeans.modules.xml.xdm.nodes.Text)
meth protected boolean compareTokenEquals(java.util.List<org.netbeans.modules.xml.xdm.nodes.Token>,java.util.List<org.netbeans.modules.xml.xdm.nodes.Token>)
meth protected boolean compareWhiteSpaces(org.netbeans.modules.xml.xdm.nodes.Text,org.netbeans.modules.xml.xdm.nodes.Text)
meth protected java.util.List<org.netbeans.modules.xml.xdm.diff.Change$Type> checkChange(org.netbeans.modules.xml.xdm.nodes.Node,org.netbeans.modules.xml.xdm.nodes.Node)
meth protected java.util.List<org.netbeans.modules.xml.xdm.diff.Change$Type> checkChange(org.netbeans.modules.xml.xdm.nodes.Node,org.netbeans.modules.xml.xdm.nodes.Node,int,int)
meth protected java.util.List<org.netbeans.modules.xml.xdm.nodes.Node> getChildList(org.netbeans.modules.xml.xdm.nodes.Node)
meth protected org.netbeans.modules.xml.xdm.diff.Difference createAddEvent(java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>,org.netbeans.modules.xml.xdm.nodes.Node,int,java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>)
meth protected org.netbeans.modules.xml.xdm.diff.Difference createChangeEvent(java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>,org.netbeans.modules.xml.xdm.nodes.Node,org.netbeans.modules.xml.xdm.nodes.Node,int,int,java.util.List<org.netbeans.modules.xml.xdm.diff.Change$Type>,java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>)
meth protected org.netbeans.modules.xml.xdm.diff.Difference createDeleteEvent(java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>,org.netbeans.modules.xml.xdm.nodes.Node,int,java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>)
meth protected org.netbeans.modules.xml.xdm.nodes.Node findMatch(org.netbeans.modules.xml.xdm.nodes.Element,java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>,org.w3c.dom.Node)
meth protected org.netbeans.modules.xml.xdm.nodes.Node findMatch(org.netbeans.modules.xml.xdm.nodes.Text,java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>)
meth protected void compareChildren(java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>,java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>,java.util.List<org.netbeans.modules.xml.xdm.diff.Difference>)
meth protected void markAdd(java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>,org.netbeans.modules.xml.xdm.nodes.Node,int,int,org.netbeans.modules.xml.xdm.nodes.Node,java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>,java.util.List<org.netbeans.modules.xml.xdm.diff.Difference>)
meth protected void markChange(java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>,org.netbeans.modules.xml.xdm.nodes.Node,org.netbeans.modules.xml.xdm.nodes.Node,int,int,java.util.List<org.netbeans.modules.xml.xdm.diff.Change$Type>,java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>,java.util.List<org.netbeans.modules.xml.xdm.diff.Difference>)
meth protected void markDelete(java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>,org.netbeans.modules.xml.xdm.nodes.Node,int,org.netbeans.modules.xml.xdm.nodes.Node,java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>,java.util.List<org.netbeans.modules.xml.xdm.diff.Difference>)
meth protected void modifyPositionFromIndex(int,java.util.List<org.netbeans.modules.xml.xdm.diff.Difference>,org.netbeans.modules.xml.xdm.diff.Difference,java.util.HashMap<org.netbeans.modules.xml.xdm.diff.Difference,java.lang.Integer>)
meth public java.util.List<org.netbeans.modules.xml.xdm.diff.Difference> findDiff(org.netbeans.modules.xml.xdm.nodes.Document,org.netbeans.modules.xml.xdm.nodes.Document)
meth public java.util.List<org.netbeans.modules.xml.xdm.diff.Difference> findOptimized(java.util.List<org.netbeans.modules.xml.xdm.diff.Difference>)
meth public static boolean isPossibleWhiteSpace(java.lang.String)
meth public static boolean isWhiteSpaceOnly(org.netbeans.modules.xml.xdm.nodes.Text)
meth public static java.util.List<org.netbeans.modules.xml.xdm.diff.Difference> filterWhitespace(java.util.List<org.netbeans.modules.xml.xdm.diff.Difference>)
meth public static java.util.List<org.netbeans.modules.xml.xdm.nodes.Node> getPathToRoot(org.netbeans.modules.xml.xdm.nodes.Node)
meth public static org.netbeans.modules.xml.xdm.diff.NodeInfo$NodeType getNodeType(org.netbeans.modules.xml.xdm.nodes.Node)
supr java.lang.Object
hfds cInfo1,cInfo2,eID,newDoc,oldDoc
hcls ChildInfo,SiblingInfo

CLSS public org.netbeans.modules.xml.xdm.diff.DiffFinder$PairComparator
 outer org.netbeans.modules.xml.xdm.diff.DiffFinder
cons public init(org.netbeans.modules.xml.xdm.diff.DiffFinder)
intf java.util.Comparator
meth public int compare(java.lang.Object,java.lang.Object)
supr java.lang.Object

CLSS public org.netbeans.modules.xml.xdm.diff.DiffFinder$PairComparator2
 outer org.netbeans.modules.xml.xdm.diff.DiffFinder
cons public init(org.netbeans.modules.xml.xdm.diff.DiffFinder)
intf java.util.Comparator
meth public int compare(java.lang.Object,java.lang.Object)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.xml.xdm.diff.Difference
cons public init(org.netbeans.modules.xml.xdm.diff.NodeInfo$NodeType,java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>,java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>,org.netbeans.modules.xml.xdm.nodes.Node,org.netbeans.modules.xml.xdm.nodes.Node,int,int)
meth public abstract java.util.List<org.netbeans.modules.xml.xdm.nodes.Node> getNewAncestors()
meth public abstract org.netbeans.modules.xml.xdm.nodes.Node getNewParent()
meth public abstract void setNewParent(org.netbeans.modules.xml.xdm.nodes.Node)
meth public org.netbeans.modules.xml.xdm.diff.NodeInfo getNewNodeInfo()
meth public org.netbeans.modules.xml.xdm.diff.NodeInfo getOldNodeInfo()
meth public org.netbeans.modules.xml.xdm.diff.NodeInfo$NodeType getNodeType()
supr java.lang.Object
hfds newNodeInfo,nodeType,oldNodeInfo

CLSS public org.netbeans.modules.xml.xdm.diff.MergeDiff
cons public init()
meth public void merge(org.netbeans.modules.xml.xdm.XDMModel,java.util.List<org.netbeans.modules.xml.xdm.diff.Difference>)
supr java.lang.Object
hfds model

CLSS public org.netbeans.modules.xml.xdm.diff.NodeIdDiffFinder
cons public init()
meth protected org.netbeans.modules.xml.xdm.nodes.Node findMatch(org.netbeans.modules.xml.xdm.nodes.Element,java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>,org.w3c.dom.Node)
meth protected org.netbeans.modules.xml.xdm.nodes.Node findMatch(org.netbeans.modules.xml.xdm.nodes.Text,java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>)
supr org.netbeans.modules.xml.xdm.diff.DiffFinder

CLSS public org.netbeans.modules.xml.xdm.diff.NodeInfo
cons public init(org.netbeans.modules.xml.xdm.nodes.Node,int,java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>,java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>)
innr public final static !enum NodeType
meth public int getPosition()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.xml.xdm.nodes.Node> getNewAncestors()
meth public java.util.List<org.netbeans.modules.xml.xdm.nodes.Node> getOriginalAncestors()
meth public org.netbeans.modules.xml.xdm.nodes.Document getDocument()
meth public org.netbeans.modules.xml.xdm.nodes.Node getNewParent()
meth public org.netbeans.modules.xml.xdm.nodes.Node getNode()
meth public org.netbeans.modules.xml.xdm.nodes.Node getParent()
meth public void setNewAncestors(java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>)
meth public void setNewParent(org.netbeans.modules.xml.xdm.nodes.Node)
supr java.lang.Object
hfds ancestors1,ancestors2,n,parent2,pos,updated

CLSS public final static !enum org.netbeans.modules.xml.xdm.diff.NodeInfo$NodeType
 outer org.netbeans.modules.xml.xdm.diff.NodeInfo
fld public final static org.netbeans.modules.xml.xdm.diff.NodeInfo$NodeType ATTRIBUTE
fld public final static org.netbeans.modules.xml.xdm.diff.NodeInfo$NodeType ELEMENT
fld public final static org.netbeans.modules.xml.xdm.diff.NodeInfo$NodeType TEXT
fld public final static org.netbeans.modules.xml.xdm.diff.NodeInfo$NodeType WHITE_SPACE
meth public static org.netbeans.modules.xml.xdm.diff.NodeInfo$NodeType valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.xdm.diff.NodeInfo$NodeType[] values()
supr java.lang.Enum<org.netbeans.modules.xml.xdm.diff.NodeInfo$NodeType>

CLSS public org.netbeans.modules.xml.xdm.diff.SyncPreparation
cons public init(java.lang.Exception)
cons public init(org.netbeans.modules.xml.xdm.nodes.Document)
cons public init(org.netbeans.modules.xml.xdm.nodes.Document,java.util.List<org.netbeans.modules.xml.xdm.diff.Difference>)
meth public boolean hasErrors()
meth public java.io.IOException getError()
meth public java.util.List<org.netbeans.modules.xml.xdm.diff.Difference> getDifferences()
meth public org.netbeans.modules.xml.xdm.nodes.Document getNewDocument()
meth public org.netbeans.modules.xml.xdm.nodes.Document getOldDocument()
supr java.lang.Object
hfds diffs,error,newDoc,oldDoc

CLSS public org.netbeans.modules.xml.xdm.diff.XDMTreeDiff
cons public init(org.netbeans.modules.xml.xam.dom.ElementIdentity)
meth public java.util.List<org.netbeans.modules.xml.xdm.diff.Difference> performDiff(org.netbeans.modules.xml.xdm.XDMModel,org.netbeans.modules.xml.xdm.nodes.Document)
meth public java.util.List<org.netbeans.modules.xml.xdm.diff.Difference> performDiff(org.netbeans.modules.xml.xdm.nodes.Document,org.netbeans.modules.xml.xdm.nodes.Document)
meth public java.util.List<org.netbeans.modules.xml.xdm.diff.Difference> performDiffAndMutate(org.netbeans.modules.xml.xdm.XDMModel,org.netbeans.modules.xml.xdm.nodes.Document)
meth public static void printDocument(org.netbeans.modules.xml.xdm.nodes.Node)
supr java.lang.Object
hfds eID

CLSS public org.netbeans.modules.xml.xdm.diff.XDMUtil
cons public init()
fld public final static java.lang.String NS_PREFIX = "xmlns"
fld public final static java.lang.String SCHEMA_LOCATION = "schemaLocation"
fld public final static java.lang.String XML_PROLOG = "<?xml version=\u00221.0\u0022?>\n"
innr public XDElementIdentity
innr public XDUDiffFinder
innr public final static !enum ComparisonCriteria
meth public java.lang.String prettyPrintXML(java.lang.String,java.lang.String) throws java.io.IOException,javax.swing.text.BadLocationException
meth public java.util.List<org.netbeans.modules.xml.xdm.diff.Difference> compareXML(java.lang.String,java.lang.String,org.netbeans.modules.xml.xdm.diff.XDMUtil$ComparisonCriteria) throws java.lang.Exception
meth public java.util.List<org.netbeans.modules.xml.xdm.diff.Difference> compareXML(java.lang.String,java.lang.String,org.netbeans.modules.xml.xdm.diff.XDMUtil$ComparisonCriteria,boolean) throws java.io.IOException,javax.swing.text.BadLocationException
meth public static boolean checkPrettyText(org.netbeans.modules.xml.xdm.nodes.Node)
meth public static boolean isWhitespaceOnly(java.lang.String)
meth public static boolean removeSchemaLocationAttrDiffs(org.netbeans.modules.xml.xdm.diff.Change)
meth public static int findPosition(org.netbeans.modules.xml.xdm.nodes.Node)
meth public static void filterAttributeOrderChange(java.util.List<org.netbeans.modules.xml.xdm.diff.Difference>)
meth public static void filterSchemaLocationDiffs(java.util.List<org.netbeans.modules.xml.xdm.diff.Difference>)
meth public static void removePseudoAttrPosChanges(java.util.List<org.netbeans.modules.xml.xdm.diff.Difference>)
supr java.lang.Object
hfds fDoc,sDoc

CLSS public final static !enum org.netbeans.modules.xml.xdm.diff.XDMUtil$ComparisonCriteria
 outer org.netbeans.modules.xml.xdm.diff.XDMUtil
fld public final static org.netbeans.modules.xml.xdm.diff.XDMUtil$ComparisonCriteria EQUAL
fld public final static org.netbeans.modules.xml.xdm.diff.XDMUtil$ComparisonCriteria IDENTICAL
meth public static org.netbeans.modules.xml.xdm.diff.XDMUtil$ComparisonCriteria valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.xdm.diff.XDMUtil$ComparisonCriteria[] values()
supr java.lang.Enum<org.netbeans.modules.xml.xdm.diff.XDMUtil$ComparisonCriteria>

CLSS public org.netbeans.modules.xml.xdm.diff.XDMUtil$XDElementIdentity
 outer org.netbeans.modules.xml.xdm.diff.XDMUtil
cons public init(org.netbeans.modules.xml.xdm.diff.XDMUtil)
meth protected boolean compareElement(org.w3c.dom.Element,org.w3c.dom.Element,org.w3c.dom.Node,org.w3c.dom.Document,org.w3c.dom.Document)
supr org.netbeans.modules.xml.xdm.diff.DefaultElementIdentity

CLSS public org.netbeans.modules.xml.xdm.diff.XDMUtil$XDUDiffFinder
 outer org.netbeans.modules.xml.xdm.diff.XDMUtil
cons public init(org.netbeans.modules.xml.xdm.diff.XDMUtil,org.netbeans.modules.xml.xam.dom.ElementIdentity)
meth protected boolean checkAttributesEqual(org.netbeans.modules.xml.xdm.nodes.Element,org.netbeans.modules.xml.xdm.nodes.Element)
meth protected boolean compareTextByValue(org.netbeans.modules.xml.xdm.nodes.Text,org.netbeans.modules.xml.xdm.nodes.Text)
meth public java.util.List<org.netbeans.modules.xml.xdm.diff.Change$Type> checkChange(org.netbeans.modules.xml.xdm.nodes.Node,org.netbeans.modules.xml.xdm.nodes.Node)
supr org.netbeans.modules.xml.xdm.diff.DiffFinder

CLSS abstract interface org.netbeans.modules.xml.xdm.diff.package-info

CLSS public org.netbeans.modules.xml.xdm.nodes.Attribute
intf org.netbeans.modules.xml.xdm.nodes.Node
intf org.w3c.dom.Attr
meth protected void cloneNamespacePrefixes(java.util.Map<java.lang.Integer,java.lang.String>,java.util.Map<java.lang.String,java.lang.String>)
meth public boolean getSpecified()
meth public boolean isId()
meth public boolean isXmlnsAttribute()
meth public java.lang.String getLocalName()
meth public java.lang.String getName()
meth public java.lang.String getNodeName()
meth public java.lang.String getNodeValue()
meth public java.lang.String getPrefix()
meth public java.lang.String getValue()
meth public org.w3c.dom.Element getOwnerElement()
meth public org.w3c.dom.TypeInfo getSchemaTypeInfo()
meth public short getNodeType()
meth public void accept(org.netbeans.modules.xml.xdm.visitor.XMLNodeVisitor)
meth public void setLocalName(java.lang.String)
meth public void setName(java.lang.String)
meth public void setPrefix(java.lang.String)
meth public void setValue(java.lang.String)
supr org.netbeans.modules.xml.xdm.nodes.NodeImpl
hfds name,value

CLSS public org.netbeans.modules.xml.xdm.nodes.CData
intf org.w3c.dom.CDATASection
meth public java.lang.String getNodeName()
meth public java.lang.String getNodeValue()
meth public short getNodeType()
meth public void accept(org.netbeans.modules.xml.xdm.visitor.XMLNodeVisitor)
meth public void setData(java.lang.String)
supr org.netbeans.modules.xml.xdm.nodes.Text

CLSS public org.netbeans.modules.xml.xdm.nodes.Comment
intf org.w3c.dom.Comment
meth public java.lang.String getNodeName()
meth public java.lang.String getNodeValue()
meth public short getNodeType()
meth public void accept(org.netbeans.modules.xml.xdm.visitor.XMLNodeVisitor)
meth public void setData(java.lang.String)
supr org.netbeans.modules.xml.xdm.nodes.Text

CLSS public final org.netbeans.modules.xml.xdm.nodes.Convertors
cons public init()
fld public final static java.lang.String PROP_DOCUMENT_URL = "doc-url"
meth public final static java.lang.String iana2java(java.lang.String)
meth public final static java.lang.String java2iana(java.lang.String)
meth public static java.lang.String documentToString(javax.swing.text.Document)
meth public static java.lang.String readerToString(java.io.Reader) throws java.io.IOException
meth public static org.xml.sax.InputSource documentToInputSource(javax.swing.text.Document)
supr java.lang.Object
hcls EncodingUtil

CLSS public org.netbeans.modules.xml.xdm.nodes.Document
intf org.netbeans.modules.xml.xdm.nodes.Node
intf org.w3c.dom.Document
meth public boolean getStrictErrorChecking()
meth public boolean getXmlStandalone()
meth public java.lang.String getDocumentURI()
meth public java.lang.String getInputEncoding()
meth public java.lang.String getNamespaceURI(org.netbeans.modules.xml.xdm.nodes.Node)
meth public java.lang.String getNodeName()
meth public java.lang.String getXmlEncoding()
meth public java.lang.String getXmlVersion()
meth public org.netbeans.modules.xml.xdm.nodes.Document clone(boolean,boolean,boolean)
meth public org.w3c.dom.Attr createAttribute(java.lang.String)
meth public org.w3c.dom.Attr createAttributeNS(java.lang.String,java.lang.String)
meth public org.w3c.dom.CDATASection createCDATASection(java.lang.String)
meth public org.w3c.dom.Comment createComment(java.lang.String)
meth public org.w3c.dom.DOMConfiguration getDomConfig()
meth public org.w3c.dom.DOMImplementation getImplementation()
meth public org.w3c.dom.DocumentFragment createDocumentFragment()
meth public org.w3c.dom.DocumentType getDoctype()
meth public org.w3c.dom.Element createElement(java.lang.String)
meth public org.w3c.dom.Element createElementNS(java.lang.String,java.lang.String)
meth public org.w3c.dom.Element getDocumentElement()
meth public org.w3c.dom.Element getElementById(java.lang.String)
meth public org.w3c.dom.EntityReference createEntityReference(java.lang.String)
meth public org.w3c.dom.Node adoptNode(org.w3c.dom.Node)
meth public org.w3c.dom.Node importNode(org.w3c.dom.Node,boolean)
meth public org.w3c.dom.Node renameNode(org.w3c.dom.Node,java.lang.String,java.lang.String)
meth public org.w3c.dom.NodeList getElementsByTagName(java.lang.String)
meth public org.w3c.dom.NodeList getElementsByTagNameNS(java.lang.String,java.lang.String)
meth public org.w3c.dom.ProcessingInstruction createProcessingInstruction(java.lang.String,java.lang.String)
meth public org.w3c.dom.Text createTextNode(java.lang.String)
meth public short getNodeType()
meth public void accept(org.netbeans.modules.xml.xdm.visitor.XMLNodeVisitor)
meth public void normalizeDocument()
meth public void setDocumentURI(java.lang.String)
meth public void setStrictErrorChecking(boolean)
meth public void setXmlStandalone(boolean)
meth public void setXmlVersion(java.lang.String)
supr org.netbeans.modules.xml.xdm.nodes.NodeImpl
hfds fnv

CLSS public org.netbeans.modules.xml.xdm.nodes.Element
intf org.netbeans.modules.xml.xdm.nodes.Node
intf org.w3c.dom.Element
meth protected void cloneNamespacePrefix(java.util.Map<java.lang.Integer,java.lang.String>,java.util.Map<java.lang.String,java.lang.String>)
meth public boolean hasAttribute(java.lang.String)
meth public boolean hasAttributeNS(java.lang.String,java.lang.String)
meth public java.lang.String getAttribute(java.lang.String)
meth public java.lang.String getAttributeNS(java.lang.String,java.lang.String)
meth public java.lang.String getLocalName()
meth public java.lang.String getNodeName()
meth public java.lang.String getPrefix()
meth public java.lang.String getTagName()
meth public java.lang.String getXmlFragmentText()
meth public org.netbeans.modules.xml.xdm.nodes.Attribute getAttributeNode(java.lang.String)
meth public org.netbeans.modules.xml.xdm.nodes.Attribute getAttributeNodeNS(java.lang.String,java.lang.String)
meth public org.netbeans.modules.xml.xdm.nodes.Attribute removeAttributeNode(org.w3c.dom.Attr)
meth public org.netbeans.modules.xml.xdm.nodes.Attribute setAttributeNode(org.w3c.dom.Attr)
meth public org.netbeans.modules.xml.xdm.nodes.Element cloneNode(boolean,boolean)
meth public org.netbeans.modules.xml.xdm.nodes.Node appendChild(org.w3c.dom.Node)
meth public org.netbeans.modules.xml.xdm.nodes.Node appendChild(org.w3c.dom.Node,boolean)
meth public org.netbeans.modules.xml.xdm.nodes.Node insertBefore(org.w3c.dom.Node,org.w3c.dom.Node)
meth public org.w3c.dom.Attr setAttributeNodeNS(org.w3c.dom.Attr)
meth public org.w3c.dom.NodeList getElementsByTagName(java.lang.String)
meth public org.w3c.dom.NodeList getElementsByTagNameNS(java.lang.String,java.lang.String)
meth public org.w3c.dom.TypeInfo getSchemaTypeInfo()
meth public short getNodeType()
meth public void accept(org.netbeans.modules.xml.xdm.visitor.XMLNodeVisitor)
meth public void addAttribute(org.netbeans.modules.xml.xdm.nodes.Attribute,int)
meth public void appendAttribute(org.netbeans.modules.xml.xdm.nodes.Attribute)
meth public void removeAttribute(java.lang.String)
meth public void removeAttributeNS(java.lang.String,java.lang.String)
meth public void reorderAttribute(int[])
meth public void reorderAttribute(org.netbeans.modules.xml.xdm.nodes.Attribute,int)
meth public void replaceAttribute(org.netbeans.modules.xml.xdm.nodes.Attribute,org.netbeans.modules.xml.xdm.nodes.Attribute)
meth public void setAttribute(java.lang.String,java.lang.String)
meth public void setAttributeNS(java.lang.String,java.lang.String,java.lang.String)
meth public void setIdAttribute(java.lang.String,boolean)
meth public void setIdAttributeNS(java.lang.String,java.lang.String,boolean)
meth public void setIdAttributeNode(org.w3c.dom.Attr,boolean)
meth public void setLocalName(java.lang.String)
meth public void setPrefix(java.lang.String)
meth public void setTagName(java.lang.String)
meth public void setXmlFragmentText(java.lang.String) throws java.io.IOException
supr org.netbeans.modules.xml.xdm.nodes.NodeImpl
hfds tagName

CLSS public final org.netbeans.modules.xml.xdm.nodes.NamedNodeMapImpl
cons public init(java.util.List<org.netbeans.modules.xml.xdm.nodes.Attribute>)
fld public final static org.w3c.dom.NamedNodeMap EMPTY
intf org.w3c.dom.NamedNodeMap
meth public int getLength()
meth public org.w3c.dom.Node getNamedItem(java.lang.String)
meth public org.w3c.dom.Node getNamedItemNS(java.lang.String,java.lang.String)
meth public org.w3c.dom.Node item(int)
meth public org.w3c.dom.Node removeNamedItem(java.lang.String)
meth public org.w3c.dom.Node removeNamedItemNS(java.lang.String,java.lang.String)
meth public org.w3c.dom.Node setNamedItem(org.w3c.dom.Node)
meth public org.w3c.dom.Node setNamedItemNS(org.w3c.dom.Node)
meth public static java.lang.Object createKey(java.lang.String)
meth public static java.lang.Object createKey(java.lang.String,java.lang.String)
supr java.lang.Object
hfds attributes

CLSS public abstract interface org.netbeans.modules.xml.xdm.nodes.Node
intf org.w3c.dom.Node
meth public abstract boolean isEquivalentNode(org.netbeans.modules.xml.xdm.nodes.Node)
meth public abstract boolean isInTree()
meth public abstract int getId()
meth public abstract int getIndexOfChild(org.netbeans.modules.xml.xdm.nodes.Node)
meth public abstract java.lang.String getNamespaceURI(org.netbeans.modules.xml.xdm.nodes.Document)
meth public abstract org.netbeans.modules.xml.xdm.nodes.Node clone(boolean,boolean,boolean)
meth public abstract void accept(org.netbeans.modules.xml.xdm.visitor.XMLNodeVisitor)
meth public abstract void addedToTree(org.netbeans.modules.xml.xdm.XDMModel)

CLSS public abstract org.netbeans.modules.xml.xdm.nodes.NodeImpl
fld public final static java.lang.String XMLNS = "xmlns"
intf java.lang.Cloneable
intf org.netbeans.modules.xml.xdm.nodes.Node
meth protected java.util.List<org.netbeans.modules.xml.xdm.nodes.Attribute> getAttributesForRead()
meth protected java.util.List<org.netbeans.modules.xml.xdm.nodes.Attribute> getAttributesForWrite()
meth protected org.netbeans.modules.xml.xdm.XDMModel getModel()
meth protected void cloneNamespacePrefixes(java.util.Map<java.lang.Integer,java.lang.String>,java.util.Map<java.lang.String,java.lang.String>)
meth public abstract java.lang.String getNodeName()
meth public abstract short getNodeType()
meth public boolean hasAttributes()
meth public boolean hasChildNodes()
meth public boolean isDefaultNamespace(java.lang.String)
meth public boolean isEqualNode(org.w3c.dom.Node)
meth public boolean isEquivalentNode(org.netbeans.modules.xml.xdm.nodes.Node)
meth public boolean isSameNode(org.w3c.dom.Node)
meth public boolean isSupported(java.lang.String,java.lang.String)
meth public final boolean isInTree()
meth public final int getId()
meth public int getIndexOfChild(org.netbeans.modules.xml.xdm.nodes.Node)
meth public int hashCode()
meth public java.lang.Object getFeature(java.lang.String,java.lang.String)
meth public java.lang.Object getUserData(java.lang.String)
meth public java.lang.Object setUserData(java.lang.String,java.lang.Object,org.w3c.dom.UserDataHandler)
meth public java.lang.String getBaseURI()
meth public java.lang.String getLocalName()
meth public java.lang.String getNamespaceURI()
meth public java.lang.String getNamespaceURI(org.netbeans.modules.xml.xdm.nodes.Document)
meth public java.lang.String getNodeValue()
meth public java.lang.String getPrefix()
meth public java.lang.String getTextContent()
meth public java.lang.String lookupNamespaceURI(java.lang.String)
meth public java.lang.String lookupPrefix(java.lang.String)
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.xml.xdm.nodes.Token> getTokens()
meth public org.netbeans.modules.xml.xdm.nodes.Node appendChild(org.w3c.dom.Node)
meth public org.netbeans.modules.xml.xdm.nodes.Node clone(boolean,boolean,boolean)
meth public org.netbeans.modules.xml.xdm.nodes.Node cloneNode(boolean)
meth public org.netbeans.modules.xml.xdm.nodes.Node cloneNode(boolean,boolean)
meth public org.netbeans.modules.xml.xdm.nodes.Node cloneNode(boolean,java.util.Map<java.lang.Integer,java.lang.String>,java.util.Map<java.lang.String,java.lang.String>)
meth public org.netbeans.modules.xml.xdm.nodes.Node cloneShallowWithModelContext()
meth public org.netbeans.modules.xml.xdm.nodes.Node copy()
meth public org.netbeans.modules.xml.xdm.nodes.Node getFirstChild()
meth public org.netbeans.modules.xml.xdm.nodes.Node getLastChild()
meth public org.netbeans.modules.xml.xdm.nodes.Node getNextSibling()
meth public org.netbeans.modules.xml.xdm.nodes.Node getParentNode()
meth public org.netbeans.modules.xml.xdm.nodes.Node getPreviousSibling()
meth public org.netbeans.modules.xml.xdm.nodes.Node insertBefore(org.w3c.dom.Node,org.w3c.dom.Node)
meth public org.netbeans.modules.xml.xdm.nodes.Node removeChild(org.w3c.dom.Node)
meth public org.netbeans.modules.xml.xdm.nodes.Node reorderChild(org.w3c.dom.Node,int)
meth public org.netbeans.modules.xml.xdm.nodes.Node replaceChild(org.w3c.dom.Node,org.w3c.dom.Node)
meth public org.w3c.dom.Document getOwnerDocument()
meth public org.w3c.dom.NamedNodeMap getAttributes()
meth public org.w3c.dom.NodeList getChildNodes()
meth public short compareDocumentPosition(org.w3c.dom.Node)
meth public static java.lang.String lookupNamespace(java.lang.String,java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>)
meth public static java.lang.String lookupNamespace(org.netbeans.modules.xml.xdm.nodes.Node,java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>)
meth public static java.lang.String lookupPrefix(java.lang.String,java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>)
meth public void addedToTree(org.netbeans.modules.xml.xdm.XDMModel)
meth public void assignNodeId(int)
meth public void assignNodeIdRecursively()
meth public void copyTokens(org.netbeans.modules.xml.xdm.nodes.Node)
meth public void normalize()
meth public void reorderChildren(int[])
meth public void setNodeValue(java.lang.String)
meth public void setPrefix(java.lang.String)
meth public void setTextContent(java.lang.String)
supr java.lang.Object
hfds attributes,children,id,inTree,model,tokens
hcls UniqueId

CLSS public org.netbeans.modules.xml.xdm.nodes.Text
intf org.netbeans.modules.xml.xdm.nodes.Node
intf org.w3c.dom.Text
meth public boolean isElementContentWhitespace()
meth public int getLength()
meth public java.lang.String getData()
meth public java.lang.String getNamespaceURI()
meth public java.lang.String getNodeName()
meth public java.lang.String getNodeValue()
meth public java.lang.String getText()
meth public java.lang.String getWholeText()
meth public java.lang.String substringData(int,int)
meth public org.w3c.dom.Text replaceWholeText(java.lang.String)
meth public org.w3c.dom.Text splitText(int)
meth public short getNodeType()
meth public void accept(org.netbeans.modules.xml.xdm.visitor.XMLNodeVisitor)
meth public void appendData(java.lang.String)
meth public void deleteData(int,int)
meth public void insertData(int,java.lang.String)
meth public void replaceData(int,int,java.lang.String)
meth public void setData(java.lang.String)
meth public void setText(java.lang.String)
supr org.netbeans.modules.xml.xdm.nodes.NodeImpl

CLSS public org.netbeans.modules.xml.xdm.nodes.Token
fld public final static org.netbeans.modules.xml.xdm.nodes.Token CDATA_END
fld public final static org.netbeans.modules.xml.xdm.nodes.Token CDATA_START
fld public final static org.netbeans.modules.xml.xdm.nodes.Token COMMENT_END
fld public final static org.netbeans.modules.xml.xdm.nodes.Token COMMENT_START
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getValue()
meth public java.lang.String toString()
meth public org.netbeans.modules.xml.xdm.nodes.TokenType getType()
meth public static org.netbeans.modules.xml.xdm.nodes.Token create(java.lang.String,org.netbeans.modules.xml.xdm.nodes.TokenType)
supr java.lang.Object
hfds CLOSE_ELEMENT,EQUALS_TOKEN,SELF_CLOSE_ELEMENT,WHITESPACE_TOKEN,type,value

CLSS public final !enum org.netbeans.modules.xml.xdm.nodes.TokenType
fld public final static org.netbeans.modules.xml.xdm.nodes.TokenType TOKEN_ATTR_EQUAL
fld public final static org.netbeans.modules.xml.xdm.nodes.TokenType TOKEN_ATTR_NAME
fld public final static org.netbeans.modules.xml.xdm.nodes.TokenType TOKEN_ATTR_NS
fld public final static org.netbeans.modules.xml.xdm.nodes.TokenType TOKEN_ATTR_QUOTATION
fld public final static org.netbeans.modules.xml.xdm.nodes.TokenType TOKEN_ATTR_VAL
fld public final static org.netbeans.modules.xml.xdm.nodes.TokenType TOKEN_CDATA_VAL
fld public final static org.netbeans.modules.xml.xdm.nodes.TokenType TOKEN_CHARACTER_DATA
fld public final static org.netbeans.modules.xml.xdm.nodes.TokenType TOKEN_COMMENT
fld public final static org.netbeans.modules.xml.xdm.nodes.TokenType TOKEN_COMMENT_TAG
fld public final static org.netbeans.modules.xml.xdm.nodes.TokenType TOKEN_DEC_ATTR_NAME
fld public final static org.netbeans.modules.xml.xdm.nodes.TokenType TOKEN_DEC_ATTR_VAL
fld public final static org.netbeans.modules.xml.xdm.nodes.TokenType TOKEN_DOC_VAL
fld public final static org.netbeans.modules.xml.xdm.nodes.TokenType TOKEN_DTD_VAL
fld public final static org.netbeans.modules.xml.xdm.nodes.TokenType TOKEN_ELEMENT_END_TAG
fld public final static org.netbeans.modules.xml.xdm.nodes.TokenType TOKEN_ELEMENT_NAME
fld public final static org.netbeans.modules.xml.xdm.nodes.TokenType TOKEN_ELEMENT_START_TAG
fld public final static org.netbeans.modules.xml.xdm.nodes.TokenType TOKEN_NS
fld public final static org.netbeans.modules.xml.xdm.nodes.TokenType TOKEN_NS_SEPARATOR
fld public final static org.netbeans.modules.xml.xdm.nodes.TokenType TOKEN_PI_END_TAG
fld public final static org.netbeans.modules.xml.xdm.nodes.TokenType TOKEN_PI_NAME
fld public final static org.netbeans.modules.xml.xdm.nodes.TokenType TOKEN_PI_START_TAG
fld public final static org.netbeans.modules.xml.xdm.nodes.TokenType TOKEN_PI_VAL
fld public final static org.netbeans.modules.xml.xdm.nodes.TokenType TOKEN_WHITESPACE
meth public static org.netbeans.modules.xml.xdm.nodes.TokenType valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.xdm.nodes.TokenType[] values()
supr java.lang.Enum<org.netbeans.modules.xml.xdm.nodes.TokenType>

CLSS public org.netbeans.modules.xml.xdm.nodes.XMLSyntaxParser
cons public init()
meth public org.netbeans.modules.xml.xdm.nodes.Document parse(org.netbeans.editor.BaseDocument) throws java.io.IOException,javax.swing.text.BadLocationException
supr java.lang.Object

CLSS abstract interface org.netbeans.modules.xml.xdm.nodes.package-info

CLSS abstract interface org.netbeans.modules.xml.xdm.package-info

CLSS public org.netbeans.modules.xml.xdm.visitor.ChildVisitor
cons public init()
meth protected void visitNode(org.netbeans.modules.xml.xdm.nodes.Node)
supr org.netbeans.modules.xml.xdm.visitor.DefaultVisitor

CLSS public org.netbeans.modules.xml.xdm.visitor.CompareVisitor
cons public init()
intf org.netbeans.modules.xml.xdm.visitor.XMLNodeVisitor
meth public boolean compare(org.netbeans.modules.xml.xdm.nodes.Node,org.netbeans.modules.xml.xdm.nodes.Node)
meth public void visit(org.netbeans.modules.xml.xdm.nodes.Attribute)
meth public void visit(org.netbeans.modules.xml.xdm.nodes.Document)
meth public void visit(org.netbeans.modules.xml.xdm.nodes.Element)
meth public void visit(org.netbeans.modules.xml.xdm.nodes.Text)
supr java.lang.Object
hfds result,target

CLSS public org.netbeans.modules.xml.xdm.visitor.DefaultVisitor
cons public init()
intf org.netbeans.modules.xml.xdm.visitor.XMLNodeVisitor
meth protected void visitNode(org.netbeans.modules.xml.xdm.nodes.Node)
meth public void visit(org.netbeans.modules.xml.xdm.nodes.Attribute)
meth public void visit(org.netbeans.modules.xml.xdm.nodes.Document)
meth public void visit(org.netbeans.modules.xml.xdm.nodes.Element)
meth public void visit(org.netbeans.modules.xml.xdm.nodes.Text)
supr java.lang.Object

CLSS public org.netbeans.modules.xml.xdm.visitor.EndPositionFinderVisitor
cons public init()
intf org.netbeans.modules.xml.xdm.visitor.XMLNodeVisitor
meth public int findPosition(org.netbeans.modules.xml.xdm.nodes.Node,org.netbeans.modules.xml.xdm.nodes.Node)
meth public void reset()
meth public void visit(org.netbeans.modules.xml.xdm.nodes.Attribute)
meth public void visit(org.netbeans.modules.xml.xdm.nodes.Document)
meth public void visit(org.netbeans.modules.xml.xdm.nodes.Element)
meth public void visit(org.netbeans.modules.xml.xdm.nodes.Text)
supr java.lang.Object
hfds found,node,position

CLSS public org.netbeans.modules.xml.xdm.visitor.FindNamespaceVisitor
cons public init(org.netbeans.modules.xml.xdm.nodes.Document)
meth protected void visitNode(org.netbeans.modules.xml.xdm.nodes.Node)
meth public java.lang.String findNamespace(org.netbeans.modules.xml.xdm.nodes.Node)
meth public java.util.Map<java.lang.Integer,java.lang.String> getNamespaceMap()
supr org.netbeans.modules.xml.xdm.visitor.ChildVisitor
hfds ancestorNamespaceMaps,namespaceMap,nodeCtr,root

CLSS public org.netbeans.modules.xml.xdm.visitor.FindVisitor
cons public init()
meth protected void visitNode(org.netbeans.modules.xml.xdm.nodes.Node)
meth public org.netbeans.modules.xml.xdm.nodes.Node find(org.netbeans.modules.xml.xdm.nodes.Document,int)
meth public org.netbeans.modules.xml.xdm.nodes.Node find(org.netbeans.modules.xml.xdm.nodes.Document,org.netbeans.modules.xml.xdm.nodes.Node)
supr org.netbeans.modules.xml.xdm.visitor.ChildVisitor
hfds result,targetId

CLSS public org.netbeans.modules.xml.xdm.visitor.FlushVisitor
cons public init()
meth protected void visitNode(org.netbeans.modules.xml.xdm.nodes.Node)
meth public java.lang.String flush(org.w3c.dom.NodeList)
meth public java.lang.String flushModel(org.netbeans.modules.xml.xdm.nodes.Document)
meth public void visit(org.netbeans.modules.xml.xdm.nodes.Element)
supr org.netbeans.modules.xml.xdm.visitor.ChildVisitor
hfds buffer

CLSS public org.netbeans.modules.xml.xdm.visitor.HashNamespaceResolver
cons public init(java.util.Map<java.lang.String,java.lang.String>)
cons public init(java.util.Map<java.lang.String,java.lang.String>,java.util.Map<java.lang.String,java.lang.String>)
intf javax.xml.namespace.NamespaceContext
meth public java.lang.String getNamespaceURI(java.lang.String)
meth public java.lang.String getPrefix(java.lang.String)
meth public java.util.Iterator getPrefixes(java.lang.String)
supr java.lang.Object
hfds namespaces,prefixes

CLSS public org.netbeans.modules.xml.xdm.visitor.MergeVisitor
cons public init()
meth protected void visitNode(org.netbeans.modules.xml.xdm.nodes.Node)
meth public void merge(org.netbeans.modules.xml.xdm.XDMModel,org.netbeans.modules.xml.xdm.nodes.Document)
supr org.netbeans.modules.xml.xdm.visitor.DefaultVisitor
hfds oldtree,pathVisitor,target,xmlModel

CLSS public org.netbeans.modules.xml.xdm.visitor.NamespaceRefactorVisitor
cons public init()
 anno 0 java.lang.Deprecated()
cons public init(org.netbeans.modules.xml.xdm.XDMModel)
innr public static NamespaceCheck
meth public static boolean isDefaultPrefix(java.lang.String)
meth public static java.util.List<java.lang.String> refactorAttributeValue(org.netbeans.modules.xml.xdm.nodes.Attribute,java.lang.String,java.lang.String,java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>,org.netbeans.modules.xml.xdm.XDMModel)
meth public void refactor(org.netbeans.modules.xml.xdm.nodes.NodeImpl,java.lang.String,java.lang.String,java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>)
meth public void visit(org.netbeans.modules.xml.xdm.nodes.Attribute)
meth public void visit(org.netbeans.modules.xml.xdm.nodes.Element)
supr org.netbeans.modules.xml.xdm.visitor.ChildVisitor
hfds model,namespace,p,path,prefix,prefixesUsedByAttributesForDefaultNS

CLSS public static org.netbeans.modules.xml.xdm.visitor.NamespaceRefactorVisitor$NamespaceCheck
 outer org.netbeans.modules.xml.xdm.visitor.NamespaceRefactorVisitor
cons public init(java.lang.String,java.lang.String,org.netbeans.modules.xml.xdm.nodes.Element)
meth public java.util.List<org.netbeans.modules.xml.xdm.nodes.Attribute> getNamespaceRedeclaration()
meth public org.netbeans.modules.xml.xdm.nodes.Attribute getDuplicateDeclaration()
meth public org.netbeans.modules.xml.xdm.nodes.Attribute getPrefixRedeclaration()
supr java.lang.Object
hfds duplicate,namespaceRedeclaredAttributes,prefixRedeclaration

CLSS public org.netbeans.modules.xml.xdm.visitor.NodeByPositionVisitor
cons public init(org.netbeans.modules.xml.xdm.nodes.Node)
intf org.netbeans.modules.xml.xdm.visitor.XMLNodeVisitor
meth public org.netbeans.modules.xml.xdm.nodes.Element getContainingElement(int)
meth public org.netbeans.modules.xml.xdm.nodes.Node getContainingNode(int)
meth public void reset()
meth public void visit(org.netbeans.modules.xml.xdm.nodes.Attribute)
meth public void visit(org.netbeans.modules.xml.xdm.nodes.Document)
meth public void visit(org.netbeans.modules.xml.xdm.nodes.Element)
meth public void visit(org.netbeans.modules.xml.xdm.nodes.Text)
supr java.lang.Object
hfds currentPos,found,foundNode,position,rootNode,stack

CLSS public org.netbeans.modules.xml.xdm.visitor.PathFromRootVisitor
cons public init()
meth protected void visitNode(org.netbeans.modules.xml.xdm.nodes.Node)
meth public java.util.List<org.netbeans.modules.xml.xdm.nodes.Node> findPath(org.netbeans.modules.xml.xdm.nodes.Document,org.netbeans.modules.xml.xdm.nodes.Node)
meth public java.util.List<org.netbeans.modules.xml.xdm.nodes.Node> findPath(org.w3c.dom.Document,org.w3c.dom.Node)
meth public java.util.List<org.netbeans.modules.xml.xdm.nodes.Node> findPathToRootElement(org.w3c.dom.Element,org.w3c.dom.Node)
supr org.netbeans.modules.xml.xdm.visitor.ChildVisitor
hfds found,pathToTarget,target

CLSS public org.netbeans.modules.xml.xdm.visitor.PositionFinderVisitor
cons public init()
intf org.netbeans.modules.xml.xdm.visitor.XMLNodeVisitor
meth public int findPosition(org.netbeans.modules.xml.xdm.nodes.Node,org.netbeans.modules.xml.xdm.nodes.Node)
meth public void reset()
meth public void visit(org.netbeans.modules.xml.xdm.nodes.Attribute)
meth public void visit(org.netbeans.modules.xml.xdm.nodes.Document)
meth public void visit(org.netbeans.modules.xml.xdm.nodes.Element)
meth public void visit(org.netbeans.modules.xml.xdm.nodes.Text)
supr java.lang.Object
hfds found,node,position

CLSS public org.netbeans.modules.xml.xdm.visitor.PrintVisitor
cons public init()
meth public void visit(org.netbeans.modules.xml.xdm.nodes.Attribute)
meth public void visit(org.netbeans.modules.xml.xdm.nodes.Document)
meth public void visit(org.netbeans.modules.xml.xdm.nodes.Element)
meth public void visit(org.netbeans.modules.xml.xdm.nodes.Text)
supr org.netbeans.modules.xml.xdm.visitor.ChildVisitor

CLSS public org.netbeans.modules.xml.xdm.visitor.Utils
cons public init()
meth public static java.lang.String filterEndLines(java.lang.String)
meth public static org.netbeans.editor.BaseDocument loadDocument(java.lang.String) throws java.io.IOException
meth public static org.w3c.dom.NodeList parseFragment(java.lang.String) throws java.io.IOException
meth public static void replaceDocument(javax.swing.text.Document,java.lang.String) throws javax.swing.text.BadLocationException
meth public static void replaceDocument(javax.swing.text.Document,java.lang.String,java.lang.String) throws javax.swing.text.BadLocationException
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.xml.xdm.visitor.XMLNodeVisitor
meth public abstract void visit(org.netbeans.modules.xml.xdm.nodes.Attribute)
meth public abstract void visit(org.netbeans.modules.xml.xdm.nodes.Document)
meth public abstract void visit(org.netbeans.modules.xml.xdm.nodes.Element)
meth public abstract void visit(org.netbeans.modules.xml.xdm.nodes.Text)

CLSS public org.netbeans.modules.xml.xdm.visitor.XPathFinder
cons public init()
fld public final static java.lang.String AT = "@"
fld public final static java.lang.String BRACKET0 = "["
fld public final static java.lang.String BRACKET1 = "]"
fld public final static java.lang.String COLON = ":"
fld public final static java.lang.String SEP = "/"
fld public final static java.lang.String XPNS = "xpns"
meth protected void visitNode(org.netbeans.modules.xml.xdm.nodes.Node)
meth public boolean isReadyForEvaluation()
meth public java.util.List<org.netbeans.modules.xml.xdm.nodes.Node> findNodes(org.netbeans.modules.xml.xdm.nodes.Document,java.lang.String)
meth public javax.xml.namespace.NamespaceContext getNamespaceContext()
meth public org.netbeans.modules.xml.xdm.nodes.Node findNode(org.netbeans.modules.xml.xdm.nodes.Document,java.lang.String)
meth public static java.lang.String getXpath(org.netbeans.modules.xml.xdm.nodes.Document,org.netbeans.modules.xml.xdm.nodes.Node)
supr org.netbeans.modules.xml.xdm.visitor.ChildVisitor
hfds countNamespaceSuffix,currentParent,done,fixedUpXpath,namespaces,prefixes,tokens
hcls XPathSegment

CLSS abstract interface org.netbeans.modules.xml.xdm.visitor.package-info

CLSS public org.netbeans.modules.xml.xdm.xam.XDMAccess
cons public init(org.netbeans.modules.xml.xam.dom.AbstractDocumentModel)
innr public AttributeKeySet
innr public AttributeMap
meth public boolean areSameNodes(org.w3c.dom.Node,org.w3c.dom.Node)
meth public int findEndPosition(org.w3c.dom.Node)
meth public int findPosition(org.w3c.dom.Node)
meth public int getElementIndexOf(org.w3c.dom.Node,org.w3c.dom.Element)
meth public java.lang.String getCurrentDocumentText()
meth public java.lang.String getIndentation()
meth public java.lang.String getXPath(org.w3c.dom.Document,org.w3c.dom.Element)
meth public java.lang.String getXmlFragment(org.w3c.dom.Element)
meth public java.util.List<org.w3c.dom.Element> getPathFromRoot(org.w3c.dom.Document,org.w3c.dom.Element)
meth public java.util.List<org.w3c.dom.Node> findNodes(org.w3c.dom.Document,java.lang.String)
meth public java.util.Map<javax.xml.namespace.QName,java.lang.String> getAttributeMap(org.w3c.dom.Element)
meth public org.netbeans.modules.xml.xam.Model$State sync() throws java.io.IOException
meth public org.netbeans.modules.xml.xam.dom.AbstractDocumentModel getModel()
meth public org.netbeans.modules.xml.xam.dom.ElementIdentity getElementIdentity()
meth public org.netbeans.modules.xml.xdm.XDMModel getReferenceModel()
meth public org.netbeans.modules.xml.xdm.XDMModel getXDMModel()
meth public org.netbeans.modules.xml.xdm.nodes.Element getContainingElement(int)
meth public org.w3c.dom.Document getDocumentRoot()
meth public org.w3c.dom.Element duplicate(org.w3c.dom.Element)
meth public org.w3c.dom.Node findNode(org.w3c.dom.Document,java.lang.String)
meth public org.w3c.dom.Node getNewEventNode(java.beans.PropertyChangeEvent)
meth public org.w3c.dom.Node getNewEventParentNode(java.beans.PropertyChangeEvent)
meth public org.w3c.dom.Node getOldEventNode(java.beans.PropertyChangeEvent)
meth public org.w3c.dom.Node getOldEventParentNode(java.beans.PropertyChangeEvent)
meth public void addMergeEventHandler(java.beans.PropertyChangeListener)
meth public void addQNameValuedAttributes(java.util.Map<javax.xml.namespace.QName,java.util.List<javax.xml.namespace.QName>>)
meth public void addUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public void appendChild(org.w3c.dom.Node,org.w3c.dom.Node,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public void finishUndoRedo()
meth public void flush()
meth public void insertBefore(org.w3c.dom.Node,org.w3c.dom.Node,org.w3c.dom.Node,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public void prepareForUndoRedo()
meth public void prepareSync()
meth public void removeAttribute(org.w3c.dom.Element,java.lang.String,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public void removeChild(org.w3c.dom.Node,org.w3c.dom.Node,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public void removeChildren(org.w3c.dom.Node,java.util.Collection<org.w3c.dom.Node>,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public void removeMergeEventHandler(java.beans.PropertyChangeListener)
meth public void removeUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public void reorderChildren(org.w3c.dom.Element,int[],org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public void replaceChild(org.w3c.dom.Node,org.w3c.dom.Node,org.w3c.dom.Node,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public void setAttribute(org.w3c.dom.Element,java.lang.String,java.lang.String,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public void setIndentation(java.lang.String)
meth public void setPrefix(org.w3c.dom.Element,java.lang.String)
meth public void setText(org.w3c.dom.Element,java.lang.String,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public void setXmlFragment(org.w3c.dom.Element,java.lang.String,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater) throws java.io.IOException
supr org.netbeans.modules.xml.xam.dom.DocumentModelAccess2
hfds model,xdmListener,xdmModel

CLSS public org.netbeans.modules.xml.xdm.xam.XDMAccess$AttributeKeySet<%0 extends java.lang.Object>
 outer org.netbeans.modules.xml.xdm.xam.XDMAccess
cons public init(java.util.List<{org.netbeans.modules.xml.xdm.xam.XDMAccess$AttributeKeySet%0}>)
meth public boolean contains(java.lang.Object)
meth public boolean isEmpty()
meth public int size()
meth public java.util.Iterator iterator()
supr java.util.HashSet<{org.netbeans.modules.xml.xdm.xam.XDMAccess$AttributeKeySet%0}>
hfds keys

CLSS public org.netbeans.modules.xml.xdm.xam.XDMAccess$AttributeMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer org.netbeans.modules.xml.xdm.xam.XDMAccess
cons public init(org.netbeans.modules.xml.xdm.xam.XDMAccess)
meth public org.netbeans.modules.xml.xdm.xam.XDMAccess$AttributeKeySet<{org.netbeans.modules.xml.xdm.xam.XDMAccess$AttributeMap%0}> keySet()
supr java.util.HashMap<{org.netbeans.modules.xml.xdm.xam.XDMAccess$AttributeMap%0},{org.netbeans.modules.xml.xdm.xam.XDMAccess$AttributeMap%1}>
hfds keys

CLSS public org.netbeans.modules.xml.xdm.xam.XDMAccessProvider
cons public init()
intf org.netbeans.modules.xml.xam.spi.DocumentModelAccessProvider
meth public java.lang.Object getModelSourceKey(org.netbeans.modules.xml.xam.ModelSource)
meth public javax.swing.text.Document loadSwingDocument(java.io.InputStream) throws java.io.IOException,javax.swing.text.BadLocationException
meth public org.netbeans.modules.xml.xam.dom.DocumentModelAccess createModelAccess(org.netbeans.modules.xml.xam.dom.AbstractDocumentModel)
supr java.lang.Object

CLSS public org.netbeans.modules.xml.xdm.xam.XDMListener
cons public init(org.netbeans.modules.xml.xam.dom.AbstractDocumentModel)
intf java.beans.PropertyChangeListener
meth protected org.netbeans.modules.xml.xam.dom.ChangeInfo prepareChangeInfo(java.util.List<? extends org.netbeans.modules.xml.xdm.nodes.Node>,java.util.List<? extends org.netbeans.modules.xml.xdm.nodes.Node>)
 anno 0 java.lang.Deprecated()
meth protected void processChange(org.netbeans.modules.xml.xam.dom.ChangeInfo)
meth protected void processEvent(org.netbeans.modules.xml.xdm.nodes.Node,org.netbeans.modules.xml.xdm.diff.NodeInfo,boolean)
meth protected void processRootRelatedEvent(org.netbeans.modules.xml.xdm.nodes.Node,java.util.List<org.netbeans.modules.xml.xdm.nodes.Node>,boolean)
meth public boolean xamModelHasRoot()
meth public void propertyChange(java.beans.PropertyChangeEvent)
supr java.lang.Object
hfds inSync,model,oldDocument,syncUnits,xamModelHasRoot

CLSS abstract interface org.netbeans.modules.xml.xdm.xam.package-info

CLSS public abstract interface org.w3c.dom.Attr
intf org.w3c.dom.Node
meth public abstract boolean getSpecified()
meth public abstract boolean isId()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getValue()
meth public abstract org.w3c.dom.Element getOwnerElement()
meth public abstract org.w3c.dom.TypeInfo getSchemaTypeInfo()
meth public abstract void setValue(java.lang.String)

CLSS public abstract interface org.w3c.dom.CDATASection
intf org.w3c.dom.Text

CLSS public abstract interface org.w3c.dom.CharacterData
intf org.w3c.dom.Node
meth public abstract int getLength()
meth public abstract java.lang.String getData()
meth public abstract java.lang.String substringData(int,int)
meth public abstract void appendData(java.lang.String)
meth public abstract void deleteData(int,int)
meth public abstract void insertData(int,java.lang.String)
meth public abstract void replaceData(int,int,java.lang.String)
meth public abstract void setData(java.lang.String)

CLSS public abstract interface org.w3c.dom.Comment
intf org.w3c.dom.CharacterData

CLSS public abstract interface org.w3c.dom.Document
intf org.w3c.dom.Node
meth public abstract boolean getStrictErrorChecking()
meth public abstract boolean getXmlStandalone()
meth public abstract java.lang.String getDocumentURI()
meth public abstract java.lang.String getInputEncoding()
meth public abstract java.lang.String getXmlEncoding()
meth public abstract java.lang.String getXmlVersion()
meth public abstract org.w3c.dom.Attr createAttribute(java.lang.String)
meth public abstract org.w3c.dom.Attr createAttributeNS(java.lang.String,java.lang.String)
meth public abstract org.w3c.dom.CDATASection createCDATASection(java.lang.String)
meth public abstract org.w3c.dom.Comment createComment(java.lang.String)
meth public abstract org.w3c.dom.DOMConfiguration getDomConfig()
meth public abstract org.w3c.dom.DOMImplementation getImplementation()
meth public abstract org.w3c.dom.DocumentFragment createDocumentFragment()
meth public abstract org.w3c.dom.DocumentType getDoctype()
meth public abstract org.w3c.dom.Element createElement(java.lang.String)
meth public abstract org.w3c.dom.Element createElementNS(java.lang.String,java.lang.String)
meth public abstract org.w3c.dom.Element getDocumentElement()
meth public abstract org.w3c.dom.Element getElementById(java.lang.String)
meth public abstract org.w3c.dom.EntityReference createEntityReference(java.lang.String)
meth public abstract org.w3c.dom.Node adoptNode(org.w3c.dom.Node)
meth public abstract org.w3c.dom.Node importNode(org.w3c.dom.Node,boolean)
meth public abstract org.w3c.dom.Node renameNode(org.w3c.dom.Node,java.lang.String,java.lang.String)
meth public abstract org.w3c.dom.NodeList getElementsByTagName(java.lang.String)
meth public abstract org.w3c.dom.NodeList getElementsByTagNameNS(java.lang.String,java.lang.String)
meth public abstract org.w3c.dom.ProcessingInstruction createProcessingInstruction(java.lang.String,java.lang.String)
meth public abstract org.w3c.dom.Text createTextNode(java.lang.String)
meth public abstract void normalizeDocument()
meth public abstract void setDocumentURI(java.lang.String)
meth public abstract void setStrictErrorChecking(boolean)
meth public abstract void setXmlStandalone(boolean)
meth public abstract void setXmlVersion(java.lang.String)

CLSS public abstract interface org.w3c.dom.Element
intf org.w3c.dom.Node
meth public abstract boolean hasAttribute(java.lang.String)
meth public abstract boolean hasAttributeNS(java.lang.String,java.lang.String)
meth public abstract java.lang.String getAttribute(java.lang.String)
meth public abstract java.lang.String getAttributeNS(java.lang.String,java.lang.String)
meth public abstract java.lang.String getTagName()
meth public abstract org.w3c.dom.Attr getAttributeNode(java.lang.String)
meth public abstract org.w3c.dom.Attr getAttributeNodeNS(java.lang.String,java.lang.String)
meth public abstract org.w3c.dom.Attr removeAttributeNode(org.w3c.dom.Attr)
meth public abstract org.w3c.dom.Attr setAttributeNode(org.w3c.dom.Attr)
meth public abstract org.w3c.dom.Attr setAttributeNodeNS(org.w3c.dom.Attr)
meth public abstract org.w3c.dom.NodeList getElementsByTagName(java.lang.String)
meth public abstract org.w3c.dom.NodeList getElementsByTagNameNS(java.lang.String,java.lang.String)
meth public abstract org.w3c.dom.TypeInfo getSchemaTypeInfo()
meth public abstract void removeAttribute(java.lang.String)
meth public abstract void removeAttributeNS(java.lang.String,java.lang.String)
meth public abstract void setAttribute(java.lang.String,java.lang.String)
meth public abstract void setAttributeNS(java.lang.String,java.lang.String,java.lang.String)
meth public abstract void setIdAttribute(java.lang.String,boolean)
meth public abstract void setIdAttributeNS(java.lang.String,java.lang.String,boolean)
meth public abstract void setIdAttributeNode(org.w3c.dom.Attr,boolean)

CLSS public abstract interface org.w3c.dom.NamedNodeMap
meth public abstract int getLength()
meth public abstract org.w3c.dom.Node getNamedItem(java.lang.String)
meth public abstract org.w3c.dom.Node getNamedItemNS(java.lang.String,java.lang.String)
meth public abstract org.w3c.dom.Node item(int)
meth public abstract org.w3c.dom.Node removeNamedItem(java.lang.String)
meth public abstract org.w3c.dom.Node removeNamedItemNS(java.lang.String,java.lang.String)
meth public abstract org.w3c.dom.Node setNamedItem(org.w3c.dom.Node)
meth public abstract org.w3c.dom.Node setNamedItemNS(org.w3c.dom.Node)

CLSS public abstract interface org.w3c.dom.Node
fld public final static short ATTRIBUTE_NODE = 2
fld public final static short CDATA_SECTION_NODE = 4
fld public final static short COMMENT_NODE = 8
fld public final static short DOCUMENT_FRAGMENT_NODE = 11
fld public final static short DOCUMENT_NODE = 9
fld public final static short DOCUMENT_POSITION_CONTAINED_BY = 16
fld public final static short DOCUMENT_POSITION_CONTAINS = 8
fld public final static short DOCUMENT_POSITION_DISCONNECTED = 1
fld public final static short DOCUMENT_POSITION_FOLLOWING = 4
fld public final static short DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC = 32
fld public final static short DOCUMENT_POSITION_PRECEDING = 2
fld public final static short DOCUMENT_TYPE_NODE = 10
fld public final static short ELEMENT_NODE = 1
fld public final static short ENTITY_NODE = 6
fld public final static short ENTITY_REFERENCE_NODE = 5
fld public final static short NOTATION_NODE = 12
fld public final static short PROCESSING_INSTRUCTION_NODE = 7
fld public final static short TEXT_NODE = 3
meth public abstract boolean hasAttributes()
meth public abstract boolean hasChildNodes()
meth public abstract boolean isDefaultNamespace(java.lang.String)
meth public abstract boolean isEqualNode(org.w3c.dom.Node)
meth public abstract boolean isSameNode(org.w3c.dom.Node)
meth public abstract boolean isSupported(java.lang.String,java.lang.String)
meth public abstract java.lang.Object getFeature(java.lang.String,java.lang.String)
meth public abstract java.lang.Object getUserData(java.lang.String)
meth public abstract java.lang.Object setUserData(java.lang.String,java.lang.Object,org.w3c.dom.UserDataHandler)
meth public abstract java.lang.String getBaseURI()
meth public abstract java.lang.String getLocalName()
meth public abstract java.lang.String getNamespaceURI()
meth public abstract java.lang.String getNodeName()
meth public abstract java.lang.String getNodeValue()
meth public abstract java.lang.String getPrefix()
meth public abstract java.lang.String getTextContent()
meth public abstract java.lang.String lookupNamespaceURI(java.lang.String)
meth public abstract java.lang.String lookupPrefix(java.lang.String)
meth public abstract org.w3c.dom.Document getOwnerDocument()
meth public abstract org.w3c.dom.NamedNodeMap getAttributes()
meth public abstract org.w3c.dom.Node appendChild(org.w3c.dom.Node)
meth public abstract org.w3c.dom.Node cloneNode(boolean)
meth public abstract org.w3c.dom.Node getFirstChild()
meth public abstract org.w3c.dom.Node getLastChild()
meth public abstract org.w3c.dom.Node getNextSibling()
meth public abstract org.w3c.dom.Node getParentNode()
meth public abstract org.w3c.dom.Node getPreviousSibling()
meth public abstract org.w3c.dom.Node insertBefore(org.w3c.dom.Node,org.w3c.dom.Node)
meth public abstract org.w3c.dom.Node removeChild(org.w3c.dom.Node)
meth public abstract org.w3c.dom.Node replaceChild(org.w3c.dom.Node,org.w3c.dom.Node)
meth public abstract org.w3c.dom.NodeList getChildNodes()
meth public abstract short compareDocumentPosition(org.w3c.dom.Node)
meth public abstract short getNodeType()
meth public abstract void normalize()
meth public abstract void setNodeValue(java.lang.String)
meth public abstract void setPrefix(java.lang.String)
meth public abstract void setTextContent(java.lang.String)

CLSS public abstract interface org.w3c.dom.Text
intf org.w3c.dom.CharacterData
meth public abstract boolean isElementContentWhitespace()
meth public abstract java.lang.String getWholeText()
meth public abstract org.w3c.dom.Text replaceWholeText(java.lang.String)
meth public abstract org.w3c.dom.Text splitText(int)

