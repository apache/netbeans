#Signature file v4.1
#Version 1.88

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.CharSequence
meth public abstract char charAt(int)
meth public abstract int length()
meth public abstract java.lang.CharSequence subSequence(int,int)
meth public abstract java.lang.String toString()
meth public java.util.stream.IntStream chars()
meth public java.util.stream.IntStream codePoints()

CLSS public abstract interface java.lang.Cloneable

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

CLSS public abstract java.util.AbstractList<%0 extends java.lang.Object>
cons protected init()
fld protected int modCount
intf java.util.List<{java.util.AbstractList%0}>
meth protected void removeRange(int,int)
meth public abstract {java.util.AbstractList%0} get(int)
meth public boolean add({java.util.AbstractList%0})
meth public boolean addAll(int,java.util.Collection<? extends {java.util.AbstractList%0}>)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public int indexOf(java.lang.Object)
meth public int lastIndexOf(java.lang.Object)
meth public java.util.Iterator<{java.util.AbstractList%0}> iterator()
meth public java.util.List<{java.util.AbstractList%0}> subList(int,int)
meth public java.util.ListIterator<{java.util.AbstractList%0}> listIterator()
meth public java.util.ListIterator<{java.util.AbstractList%0}> listIterator(int)
meth public void add(int,{java.util.AbstractList%0})
meth public void clear()
meth public {java.util.AbstractList%0} remove(int)
meth public {java.util.AbstractList%0} set(int,{java.util.AbstractList%0})
supr java.util.AbstractCollection<{java.util.AbstractList%0}>

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

CLSS public abstract interface java.util.List<%0 extends java.lang.Object>
intf java.util.Collection<{java.util.List%0}>
meth public abstract <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public abstract boolean add({java.util.List%0})
meth public abstract boolean addAll(int,java.util.Collection<? extends {java.util.List%0}>)
meth public abstract boolean addAll(java.util.Collection<? extends {java.util.List%0}>)
meth public abstract boolean contains(java.lang.Object)
meth public abstract boolean containsAll(java.util.Collection<?>)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isEmpty()
meth public abstract boolean remove(java.lang.Object)
meth public abstract boolean removeAll(java.util.Collection<?>)
meth public abstract boolean retainAll(java.util.Collection<?>)
meth public abstract int hashCode()
meth public abstract int indexOf(java.lang.Object)
meth public abstract int lastIndexOf(java.lang.Object)
meth public abstract int size()
meth public abstract java.lang.Object[] toArray()
meth public abstract java.util.Iterator<{java.util.List%0}> iterator()
meth public abstract java.util.List<{java.util.List%0}> subList(int,int)
meth public abstract java.util.ListIterator<{java.util.List%0}> listIterator()
meth public abstract java.util.ListIterator<{java.util.List%0}> listIterator(int)
meth public abstract void add(int,{java.util.List%0})
meth public abstract void clear()
meth public abstract {java.util.List%0} get(int)
meth public abstract {java.util.List%0} remove(int)
meth public abstract {java.util.List%0} set(int,{java.util.List%0})
meth public java.util.Spliterator<{java.util.List%0}> spliterator()
meth public void replaceAll(java.util.function.UnaryOperator<{java.util.List%0}>)
meth public void sort(java.util.Comparator<? super {java.util.List%0}>)

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

CLSS public abstract interface static java.util.Map$Entry<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer java.util.Map
meth public abstract boolean equals(java.lang.Object)
meth public abstract int hashCode()
meth public abstract {java.util.Map$Entry%0} getKey()
meth public abstract {java.util.Map$Entry%1} getValue()
meth public abstract {java.util.Map$Entry%1} setValue({java.util.Map$Entry%1})
meth public static <%0 extends java.lang.Comparable<? super {%%0}>, %1 extends java.lang.Object> java.util.Comparator<java.util.Map$Entry<{%%0},{%%1}>> comparingByKey()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Comparable<? super {%%1}>> java.util.Comparator<java.util.Map$Entry<{%%0},{%%1}>> comparingByValue()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Comparator<java.util.Map$Entry<{%%0},{%%1}>> comparingByKey(java.util.Comparator<? super {%%0}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Comparator<java.util.Map$Entry<{%%0},{%%1}>> comparingByValue(java.util.Comparator<? super {%%1}>)

CLSS public abstract interface java.util.RandomAccess

CLSS public abstract interface javax.swing.event.DocumentEvent
innr public abstract interface static ElementChange
innr public final static EventType
meth public abstract int getLength()
meth public abstract int getOffset()
meth public abstract javax.swing.event.DocumentEvent$ElementChange getChange(javax.swing.text.Element)
meth public abstract javax.swing.event.DocumentEvent$EventType getType()
meth public abstract javax.swing.text.Document getDocument()

CLSS public abstract interface static javax.swing.event.DocumentEvent$ElementChange
 outer javax.swing.event.DocumentEvent
meth public abstract int getIndex()
meth public abstract javax.swing.text.Element getElement()
meth public abstract javax.swing.text.Element[] getChildrenAdded()
meth public abstract javax.swing.text.Element[] getChildrenRemoved()

CLSS public abstract interface javax.swing.text.Element
meth public abstract boolean isLeaf()
meth public abstract int getElementCount()
meth public abstract int getElementIndex(int)
meth public abstract int getEndOffset()
meth public abstract int getStartOffset()
meth public abstract java.lang.String getName()
meth public abstract javax.swing.text.AttributeSet getAttributes()
meth public abstract javax.swing.text.Document getDocument()
meth public abstract javax.swing.text.Element getElement(int)
meth public abstract javax.swing.text.Element getParentElement()

CLSS public javax.swing.undo.AbstractUndoableEdit
cons public init()
fld protected final static java.lang.String RedoName = "Redo"
fld protected final static java.lang.String UndoName = "Undo"
intf java.io.Serializable
intf javax.swing.undo.UndoableEdit
meth public boolean addEdit(javax.swing.undo.UndoableEdit)
meth public boolean canRedo()
meth public boolean canUndo()
meth public boolean isSignificant()
meth public boolean replaceEdit(javax.swing.undo.UndoableEdit)
meth public java.lang.String getPresentationName()
meth public java.lang.String getRedoPresentationName()
meth public java.lang.String getUndoPresentationName()
meth public java.lang.String toString()
meth public void die()
meth public void redo()
meth public void undo()
supr java.lang.Object

CLSS public abstract interface javax.swing.undo.UndoableEdit
meth public abstract boolean addEdit(javax.swing.undo.UndoableEdit)
meth public abstract boolean canRedo()
meth public abstract boolean canUndo()
meth public abstract boolean isSignificant()
meth public abstract boolean replaceEdit(javax.swing.undo.UndoableEdit)
meth public abstract java.lang.String getPresentationName()
meth public abstract java.lang.String getRedoPresentationName()
meth public abstract java.lang.String getUndoPresentationName()
meth public abstract void die()
meth public abstract void redo()
meth public abstract void undo()

CLSS public abstract org.netbeans.lib.editor.util.AbstractCharSequence
cons public init()
innr public abstract static StringLike
intf java.lang.CharSequence
meth public abstract char charAt(int)
meth public abstract int length()
meth public java.lang.CharSequence subSequence(int,int)
meth public java.lang.String toString()
supr java.lang.Object

CLSS public abstract static org.netbeans.lib.editor.util.AbstractCharSequence$StringLike
 outer org.netbeans.lib.editor.util.AbstractCharSequence
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.CharSequence subSequence(int,int)
supr org.netbeans.lib.editor.util.AbstractCharSequence

CLSS public final org.netbeans.lib.editor.util.ArrayUtilities
meth public static <%0 extends java.lang.Object> java.util.List<{%%0}> unmodifiableList({%%0}[])
meth public static boolean[] booleanArray(boolean[])
meth public static boolean[] booleanArray(boolean[],int)
meth public static boolean[] booleanArray(boolean[],int,int,boolean)
meth public static boolean[] emptyBooleanArray()
meth public static char[] charArray(char[])
meth public static char[] charArray(char[],int)
meth public static char[] charArray(char[],int,int,boolean)
meth public static char[] charArray(char[],int,int,int)
meth public static char[] emptyCharArray()
meth public static int binarySearch(int[],int)
meth public static int binarySearch(int[],int,int,int)
meth public static int digitCount(int)
meth public static int[] emptyIntArray()
meth public static int[] intArray(int[])
meth public static int[] intArray(int[],int)
meth public static int[] intArray(int[],int,int,boolean)
meth public static int[] intArray(int[],int,int,int)
meth public static java.lang.String toString(int[])
meth public static java.lang.String toString(java.lang.Object[])
meth public static void appendBracketedIndex(java.lang.StringBuffer,int,int)
meth public static void appendBracketedIndex(java.lang.StringBuilder,int,int)
meth public static void appendIndex(java.lang.StringBuffer,int,int)
meth public static void appendIndex(java.lang.StringBuilder,int,int)
meth public static void appendSpaces(java.lang.StringBuffer,int)
meth public static void appendSpaces(java.lang.StringBuilder,int)
supr java.lang.Object
hfds EMPTY_BOOLEAN_ARRAY,EMPTY_CHAR_ARRAY,EMPTY_INT_ARRAY
hcls UnmodifiableList

CLSS public final org.netbeans.lib.editor.util.CharSequenceUtilities
meth public static boolean endsWith(java.lang.CharSequence,java.lang.CharSequence)
meth public static boolean equals(java.lang.CharSequence,java.lang.Object)
meth public static boolean startsWith(java.lang.CharSequence,java.lang.CharSequence)
meth public static boolean textEquals(java.lang.CharSequence,java.lang.CharSequence)
meth public static int indexOf(java.lang.CharSequence,int)
meth public static int indexOf(java.lang.CharSequence,int,int)
meth public static int indexOf(java.lang.CharSequence,java.lang.CharSequence)
meth public static int indexOf(java.lang.CharSequence,java.lang.CharSequence,int)
meth public static int lastIndexOf(java.lang.CharSequence,int)
meth public static int lastIndexOf(java.lang.CharSequence,int,int)
meth public static int lastIndexOf(java.lang.CharSequence,java.lang.CharSequence)
meth public static int lastIndexOf(java.lang.CharSequence,java.lang.CharSequence,int)
meth public static int stringLikeHashCode(java.lang.CharSequence)
meth public static java.lang.CharSequence trim(java.lang.CharSequence)
meth public static java.lang.String debugChar(char)
meth public static java.lang.String debugText(java.lang.CharSequence)
meth public static java.lang.String toString(java.lang.CharSequence)
meth public static java.lang.String toString(java.lang.CharSequence,int,int)
meth public static void append(java.lang.StringBuffer,java.lang.CharSequence)
meth public static void append(java.lang.StringBuffer,java.lang.CharSequence,int,int)
meth public static void checkIndexNonNegative(int)
meth public static void checkIndexValid(int,int)
meth public static void checkIndexesValid(int,int,int)
meth public static void checkIndexesValid(java.lang.CharSequence,int,int)
meth public static void debugChar(java.lang.StringBuffer,char)
meth public static void debugChar(java.lang.StringBuilder,char)
meth public static void debugText(java.lang.StringBuffer,java.lang.CharSequence)
meth public static void debugText(java.lang.StringBuilder,java.lang.CharSequence)
supr java.lang.Object

CLSS public org.netbeans.lib.editor.util.CharSubSequence
cons public init(java.lang.CharSequence,int,int)
innr public static StringLike
meth protected int start()
meth protected java.lang.CharSequence backingSequence()
meth public char charAt(int)
meth public int length()
meth public static void checkIndexesValid(java.lang.CharSequence,int,int)
 anno 0 java.lang.Deprecated()
supr org.netbeans.lib.editor.util.AbstractCharSequence
hfds backingSequence,length,start

CLSS public static org.netbeans.lib.editor.util.CharSubSequence$StringLike
 outer org.netbeans.lib.editor.util.CharSubSequence
cons public init(java.lang.CharSequence,int,int)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
supr org.netbeans.lib.editor.util.CharSubSequence

CLSS public final org.netbeans.lib.editor.util.CharacterConversions
fld public final static char CR = '\r'
fld public final static char LF = '\n'
fld public final static char LS = '\u2028'
fld public final static char PS = '\u2029'
meth public static java.lang.String lineFeedToLineSeparator(java.lang.CharSequence)
meth public static java.lang.String lineFeedToLineSeparator(java.lang.CharSequence,java.lang.CharSequence)
meth public static java.lang.String lineSeparatorToLineFeed(java.lang.CharSequence)
meth public static java.lang.String lineSeparatorToLineFeed(java.lang.CharSequence,java.lang.CharSequence)
supr java.lang.Object
hfds systemDefaultLineSeparator

CLSS public org.netbeans.lib.editor.util.CompactMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
cons public init(int)
innr public abstract static MapEntry
innr public static DefaultMapEntry
intf java.util.Map<{org.netbeans.lib.editor.util.CompactMap%0},{org.netbeans.lib.editor.util.CompactMap%1}>
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean isEmpty()
meth public final int size()
meth public java.lang.String toString()
meth public java.util.Collection<{org.netbeans.lib.editor.util.CompactMap%1}> values()
meth public java.util.Set<java.util.Map$Entry<{org.netbeans.lib.editor.util.CompactMap%0},{org.netbeans.lib.editor.util.CompactMap%1}>> entrySet()
meth public java.util.Set<{org.netbeans.lib.editor.util.CompactMap%0}> keySet()
meth public org.netbeans.lib.editor.util.CompactMap$MapEntry<{org.netbeans.lib.editor.util.CompactMap%0},{org.netbeans.lib.editor.util.CompactMap%1}> getFirstEntry(int)
meth public org.netbeans.lib.editor.util.CompactMap$MapEntry<{org.netbeans.lib.editor.util.CompactMap%0},{org.netbeans.lib.editor.util.CompactMap%1}> putEntry(org.netbeans.lib.editor.util.CompactMap$MapEntry<{org.netbeans.lib.editor.util.CompactMap%0},{org.netbeans.lib.editor.util.CompactMap%1}>)
meth public org.netbeans.lib.editor.util.CompactMap$MapEntry<{org.netbeans.lib.editor.util.CompactMap%0},{org.netbeans.lib.editor.util.CompactMap%1}> removeEntry(org.netbeans.lib.editor.util.CompactMap$MapEntry<{org.netbeans.lib.editor.util.CompactMap%0},{org.netbeans.lib.editor.util.CompactMap%1}>)
meth public void clear()
meth public void putAll(java.util.Map<? extends {org.netbeans.lib.editor.util.CompactMap%0},? extends {org.netbeans.lib.editor.util.CompactMap%1}>)
meth public {org.netbeans.lib.editor.util.CompactMap%1} get(java.lang.Object)
meth public {org.netbeans.lib.editor.util.CompactMap%1} put({org.netbeans.lib.editor.util.CompactMap%0},{org.netbeans.lib.editor.util.CompactMap%1})
meth public {org.netbeans.lib.editor.util.CompactMap%1} remove(java.lang.Object)
supr java.lang.Object
hfds size,table
hcls EntryIterator,EntrySet,HashIterator,KeyIterator,ValueIterator

CLSS public static org.netbeans.lib.editor.util.CompactMap$DefaultMapEntry<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer org.netbeans.lib.editor.util.CompactMap
cons public init({org.netbeans.lib.editor.util.CompactMap$DefaultMapEntry%0})
meth protected final boolean valueEquals(java.lang.Object)
meth protected final int valueHashCode()
meth public final {org.netbeans.lib.editor.util.CompactMap$DefaultMapEntry%0} getKey()
meth public final {org.netbeans.lib.editor.util.CompactMap$DefaultMapEntry%1} getValue()
meth public final {org.netbeans.lib.editor.util.CompactMap$DefaultMapEntry%1} setValue({org.netbeans.lib.editor.util.CompactMap$DefaultMapEntry%1})
meth public java.lang.String toString()
supr org.netbeans.lib.editor.util.CompactMap$MapEntry<{org.netbeans.lib.editor.util.CompactMap$DefaultMapEntry%0},{org.netbeans.lib.editor.util.CompactMap$DefaultMapEntry%1}>
hfds key,value

CLSS public abstract static org.netbeans.lib.editor.util.CompactMap$MapEntry<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer org.netbeans.lib.editor.util.CompactMap
cons public init()
intf java.util.Map$Entry<{org.netbeans.lib.editor.util.CompactMap$MapEntry%0},{org.netbeans.lib.editor.util.CompactMap$MapEntry%1}>
meth protected abstract boolean valueEquals(java.lang.Object)
meth protected abstract int valueHashCode()
meth public abstract {org.netbeans.lib.editor.util.CompactMap$MapEntry%0} getKey()
meth public abstract {org.netbeans.lib.editor.util.CompactMap$MapEntry%1} getValue()
meth public abstract {org.netbeans.lib.editor.util.CompactMap$MapEntry%1} setValue({org.netbeans.lib.editor.util.CompactMap$MapEntry%1})
meth public final boolean equals(java.lang.Object)
meth public final int hashCode()
meth public final int keyHashCode()
meth public final org.netbeans.lib.editor.util.CompactMap$MapEntry<{org.netbeans.lib.editor.util.CompactMap$MapEntry%0},{org.netbeans.lib.editor.util.CompactMap$MapEntry%1}> nextMapEntry()
supr java.lang.Object
hfds keyHashCode,nextMapEntry

CLSS public abstract org.netbeans.lib.editor.util.FlyOffsetGapList<%0 extends java.lang.Object>
cons public init()
cons public init(int)
meth protected abstract boolean isElementFlyweight({org.netbeans.lib.editor.util.FlyOffsetGapList%0})
meth protected abstract int elementLength({org.netbeans.lib.editor.util.FlyOffsetGapList%0})
meth protected abstract int elementRawOffset({org.netbeans.lib.editor.util.FlyOffsetGapList%0})
meth protected abstract void setElementRawOffset({org.netbeans.lib.editor.util.FlyOffsetGapList%0},int)
meth protected final int elementOffset(int)
meth protected final int elementOrEndOffset(int)
meth protected final int findElementIndex(int)
meth protected final int offset2Raw(int)
meth protected final int offsetGapLength()
meth protected final int offsetGapStart()
meth protected final void moveOffsetGap(int,int)
meth protected final void updateOffsetGapLength(int)
meth protected final void updateOffsetGapStart(int)
meth protected int startOffset()
meth protected java.lang.String dumpInternals()
meth protected void consistencyCheck(boolean)
meth protected void updateElementOffsetAdd({org.netbeans.lib.editor.util.FlyOffsetGapList%0})
meth protected void updateElementOffsetRemove({org.netbeans.lib.editor.util.FlyOffsetGapList%0})
meth public void defaultInsertUpdate(int,int)
meth public void defaultRemoveUpdate(int,int)
supr org.netbeans.lib.editor.util.GapList<{org.netbeans.lib.editor.util.FlyOffsetGapList%0}>
hfds offsetGapLength,offsetGapStart

CLSS public org.netbeans.lib.editor.util.GapList<%0 extends java.lang.Object>
cons public init()
cons public init(int)
cons public init(java.util.Collection<? extends {org.netbeans.lib.editor.util.GapList%0}>)
intf java.io.Serializable
intf java.lang.Cloneable
intf java.util.List<{org.netbeans.lib.editor.util.GapList%0}>
intf java.util.RandomAccess
meth protected final void consistencyError(java.lang.String)
meth protected java.lang.String dumpDetails()
meth protected java.lang.String dumpInternals()
meth protected void consistencyCheck()
meth protected void removeRange(int,int)
meth public <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public boolean add({org.netbeans.lib.editor.util.GapList%0})
meth public boolean addAll(int,java.util.Collection<? extends {org.netbeans.lib.editor.util.GapList%0}>)
meth public boolean addAll(int,java.util.Collection<? extends {org.netbeans.lib.editor.util.GapList%0}>,int,int)
meth public boolean addAll(java.util.Collection<? extends {org.netbeans.lib.editor.util.GapList%0}>)
meth public boolean addAll(java.util.Collection<? extends {org.netbeans.lib.editor.util.GapList%0}>,int,int)
meth public boolean addArray(int,java.lang.Object[])
meth public boolean addArray(int,java.lang.Object[],int,int)
meth public boolean addArray(java.lang.Object[])
meth public boolean contains(java.lang.Object)
meth public boolean isEmpty()
meth public int indexOf(java.lang.Object)
meth public int lastIndexOf(java.lang.Object)
meth public int size()
meth public java.lang.Object clone()
meth public java.lang.Object[] toArray()
meth public java.lang.String toString()
meth public org.netbeans.lib.editor.util.GapList<{org.netbeans.lib.editor.util.GapList%0}> copy()
meth public static java.lang.String dumpElements(java.util.List)
meth public void add(int,{org.netbeans.lib.editor.util.GapList%0})
meth public void clear()
meth public void copyElements(int,int,java.lang.Object[],int)
meth public void copyElements(int,int,java.util.Collection<{org.netbeans.lib.editor.util.GapList%0}>)
meth public void copyItems(int,int,java.lang.Object[],int)
 anno 0 java.lang.Deprecated()
meth public void ensureCapacity(int)
meth public void remove(int,int)
meth public void swap(int,int)
meth public void trimToSize()
meth public {org.netbeans.lib.editor.util.GapList%0} get(int)
meth public {org.netbeans.lib.editor.util.GapList%0} remove(int)
meth public {org.netbeans.lib.editor.util.GapList%0} set(int,{org.netbeans.lib.editor.util.GapList%0})
supr java.util.AbstractList<{org.netbeans.lib.editor.util.GapList%0}>
hfds elementData,gapLength,gapStart

CLSS public final org.netbeans.lib.editor.util.ListenerList<%0 extends java.util.EventListener>
cons public init()
intf java.io.Serializable
meth public int getListenerCount()
meth public java.lang.String toString()
meth public java.util.List<{org.netbeans.lib.editor.util.ListenerList%0}> getListeners()
meth public void add({org.netbeans.lib.editor.util.ListenerList%0})
meth public void remove({org.netbeans.lib.editor.util.ListenerList%0})
supr java.lang.Object
hfds EMPTY_LISTENER_ARRAY,listenersList,serialVersionUID
hcls ImmutableList

CLSS public abstract org.netbeans.lib.editor.util.OffsetGapList<%0 extends java.lang.Object>
cons public init()
cons public init(int)
meth protected abstract int elementRawOffset({org.netbeans.lib.editor.util.OffsetGapList%0})
meth protected abstract void setElementRawOffset({org.netbeans.lib.editor.util.OffsetGapList%0},int)
meth protected final int findElementIndex(int)
meth protected final int offset2raw(int)
meth protected final int offsetGapLength()
meth protected final int offsetGapStart()
meth protected final int raw2Offset(int)
meth protected final void moveOffsetGap(int,int)
meth protected final void updateOffsetGapLength(int)
meth protected final void updateOffsetGapStart(int)
meth protected int elementOffset(int)
meth protected int elementOffset({org.netbeans.lib.editor.util.OffsetGapList%0})
meth protected java.lang.String dumpInternals()
meth protected void consistencyCheck()
meth protected void updateElementOffsetAdd({org.netbeans.lib.editor.util.OffsetGapList%0})
meth protected void updateElementOffsetRemove({org.netbeans.lib.editor.util.OffsetGapList%0})
meth public void defaultInsertUpdate(int,int)
meth public void defaultRemoveUpdate(int,int)
supr org.netbeans.lib.editor.util.GapList<{org.netbeans.lib.editor.util.OffsetGapList%0}>
hfds offsetGapLength,offsetGapStart

CLSS public org.netbeans.lib.editor.util.PriorityListenerList<%0 extends java.util.EventListener>
cons public init()
intf java.io.Serializable
meth public int getListenerCount()
meth public void add({org.netbeans.lib.editor.util.PriorityListenerList%0},int)
meth public void remove({org.netbeans.lib.editor.util.PriorityListenerList%0},int)
meth public {org.netbeans.lib.editor.util.PriorityListenerList%0}[][] getListenersArray()
supr java.lang.Object
hfds EMPTY_LISTENER_ARRAY,EMPTY_LISTENER_ARRAY_ARRAY,listenerCount,listenersArray,serialVersionUID

CLSS public org.netbeans.lib.editor.util.PriorityMutex
cons public init()
meth protected boolean isPriorityThread()
meth public boolean isPriorityThreadWaiting()
meth public final java.lang.Thread getLockThread()
meth public java.lang.String toString()
meth public void lock()
meth public void unlock()
supr java.lang.Object
hfds LOG,TIMEOUTS_BEFORE_LOGGING,WAIT_TIMEOUT,lockDepth,lockThread,logLockStackTrace,waitingPriorityThread

CLSS public org.netbeans.lib.editor.util.StringEscapeUtils
cons public init()
meth public static java.lang.String escapeHtml(java.lang.String)
meth public static java.lang.String unescapeHtml(java.lang.String)
supr java.lang.Object
hcls Escape

CLSS public final org.netbeans.lib.editor.util.swing.BlockCompare
meth public boolean after()
meth public boolean before()
meth public boolean contains()
meth public boolean containsStrict()
meth public boolean emptyX()
meth public boolean emptyY()
meth public boolean equal()
meth public boolean equalEnd()
meth public boolean equalStart()
meth public boolean inside()
meth public boolean insideStrict()
meth public boolean invalidX()
meth public boolean invalidY()
meth public boolean lowerEnd()
meth public boolean lowerStart()
meth public boolean overlap()
meth public boolean overlapEnd()
meth public boolean overlapStart()
meth public java.lang.String toString()
meth public static org.netbeans.lib.editor.util.swing.BlockCompare get(int,int,int,int)
supr java.lang.Object
hfds AFTER,BEFORE,CONTAINS,EMPTY_X,EMPTY_Y,EQUAL_END,EQUAL_START,INSIDE,INVALID_X,INVALID_Y,LOWER_END,LOWER_START,OVERLAP_END,OVERLAP_START,value

CLSS public final org.netbeans.lib.editor.util.swing.DocumentListenerPriority
fld public final static org.netbeans.lib.editor.util.swing.DocumentListenerPriority AFTER_CARET_UPDATE
fld public final static org.netbeans.lib.editor.util.swing.DocumentListenerPriority CARET_UPDATE
fld public final static org.netbeans.lib.editor.util.swing.DocumentListenerPriority DEFAULT
fld public final static org.netbeans.lib.editor.util.swing.DocumentListenerPriority FIRST
fld public final static org.netbeans.lib.editor.util.swing.DocumentListenerPriority FOLD_UPDATE
fld public final static org.netbeans.lib.editor.util.swing.DocumentListenerPriority LEXER
fld public final static org.netbeans.lib.editor.util.swing.DocumentListenerPriority VIEW
meth public java.lang.String getDescription()
meth public java.lang.String toString()
supr java.lang.Object
hfds PRIORITIES,description,priority

CLSS public final org.netbeans.lib.editor.util.swing.DocumentUtilities
meth public static boolean addPriorityDocumentListener(javax.swing.text.Document,javax.swing.event.DocumentListener,org.netbeans.lib.editor.util.swing.DocumentListenerPriority)
meth public static boolean isReadLocked(javax.swing.text.Document)
meth public static boolean isTypingModification(javax.swing.event.DocumentEvent)
 anno 0 java.lang.Deprecated()
meth public static boolean isTypingModification(javax.swing.text.Document)
meth public static boolean isWriteLocked(javax.swing.text.Document)
meth public static boolean putEventPropertyIfSupported(javax.swing.event.DocumentEvent,java.lang.Object,java.lang.Object)
meth public static boolean removePriorityDocumentListener(javax.swing.text.Document,javax.swing.event.DocumentListener,org.netbeans.lib.editor.util.swing.DocumentListenerPriority)
meth public static int fixOffset(int,javax.swing.event.DocumentEvent)
meth public static int getDocumentListenerCount(javax.swing.text.Document)
meth public static java.beans.PropertyChangeListener addWeakPropertyChangeListener(javax.swing.text.Document,java.beans.PropertyChangeListener)
meth public static java.lang.CharSequence getText(javax.swing.text.Document)
meth public static java.lang.CharSequence getText(javax.swing.text.Document,int,int) throws javax.swing.text.BadLocationException
meth public static java.lang.Object getEventProperty(javax.swing.event.DocumentEvent,java.lang.Object)
meth public static java.lang.String debugOffset(javax.swing.text.Document,int)
meth public static java.lang.String getMimeType(javax.swing.text.Document)
meth public static java.lang.String getMimeType(javax.swing.text.JTextComponent)
meth public static java.lang.String getModificationText(javax.swing.event.DocumentEvent)
meth public static java.lang.StringBuilder appendEvent(java.lang.StringBuilder,javax.swing.event.DocumentEvent)
meth public static java.lang.StringBuilder appendOffset(java.lang.StringBuilder,javax.swing.text.Document,int)
meth public static javax.swing.event.DocumentListener initPriorityListening(javax.swing.text.Document)
meth public static javax.swing.text.Element getParagraphElement(javax.swing.text.Document,int)
meth public static javax.swing.text.Element getParagraphRootElement(javax.swing.text.Document)
meth public static long getDocumentTimestamp(javax.swing.text.Document)
meth public static long getDocumentVersion(javax.swing.text.Document)
meth public static void addDocumentListener(javax.swing.text.Document,javax.swing.event.DocumentListener,org.netbeans.lib.editor.util.swing.DocumentListenerPriority)
meth public static void addEventPropertyStorage(javax.swing.event.DocumentEvent)
meth public static void addPropertyChangeListener(javax.swing.text.Document,java.beans.PropertyChangeListener)
meth public static void putEventProperty(javax.swing.event.DocumentEvent,java.lang.Object,java.lang.Object)
meth public static void putEventProperty(javax.swing.event.DocumentEvent,java.util.Map$Entry)
meth public static void removeDocumentListener(javax.swing.text.Document,javax.swing.event.DocumentListener,org.netbeans.lib.editor.util.swing.DocumentListenerPriority)
meth public static void removePropertyChangeListener(javax.swing.text.Document,java.beans.PropertyChangeListener)
meth public static void setTypingModification(javax.swing.text.Document,boolean)
supr java.lang.Object
hfds LAST_MODIFICATION_TIMESTAMP_PROP,TYPING_MODIFICATION_DOCUMENT_PROPERTY,TYPING_MODIFICATION_KEY,VERSION_PROP,currWriterField,documentClassAccessors,numReadersField
hcls DocumentCharSequence,EventPropertiesElement,EventPropertiesElementChange

CLSS public final org.netbeans.lib.editor.util.swing.ElementUtilities
meth public static void updateOffsetRange(javax.swing.text.Element[],int[])
supr java.lang.Object

CLSS public abstract org.netbeans.lib.editor.util.swing.GapBranchElement
cons public init()
fld protected final static javax.swing.text.Element[] EMPTY_ELEMENT_ARRAY
innr public Edit
innr public abstract LastIndex
intf javax.swing.text.Element
meth protected void replace(int,int,javax.swing.text.Element[])
meth public boolean isLeaf()
meth public int getElementCount()
meth public int getElementIndex(int)
meth public java.lang.String toString()
meth public javax.swing.text.Element getElement(int)
meth public void copyElements(int,int,javax.swing.text.Element[],int)
supr java.lang.Object
hfds children

CLSS public org.netbeans.lib.editor.util.swing.GapBranchElement$Edit
 outer org.netbeans.lib.editor.util.swing.GapBranchElement
cons public init(org.netbeans.lib.editor.util.swing.GapBranchElement,int,javax.swing.text.Element[],javax.swing.text.Element[])
intf javax.swing.event.DocumentEvent$ElementChange
meth public int getIndex()
meth public javax.swing.text.Element getElement()
meth public javax.swing.text.Element[] getChildrenAdded()
meth public javax.swing.text.Element[] getChildrenRemoved()
meth public void redo()
meth public void undo()
supr javax.swing.undo.AbstractUndoableEdit
hfds childrenAdded,childrenRemoved,index

CLSS public abstract org.netbeans.lib.editor.util.swing.GapBranchElement$LastIndex
 outer org.netbeans.lib.editor.util.swing.GapBranchElement
cons public init(org.netbeans.lib.editor.util.swing.GapBranchElement)
meth public int getElementIndex(int)
supr java.lang.Object
hfds lastReturnedElementIndex

CLSS public org.netbeans.lib.editor.util.swing.MutablePositionRegion
cons public init(javax.swing.text.Document,int,int) throws javax.swing.text.BadLocationException
cons public init(javax.swing.text.Position,javax.swing.text.Position)
meth public final void reset(javax.swing.text.Position,javax.swing.text.Position)
meth public final void setEndPosition(javax.swing.text.Position)
meth public final void setStartPosition(javax.swing.text.Position)
supr org.netbeans.lib.editor.util.swing.PositionRegion

CLSS public final org.netbeans.lib.editor.util.swing.PositionComparator
cons public init()
fld public final static org.netbeans.lib.editor.util.swing.PositionComparator INSTANCE
intf java.util.Comparator<javax.swing.text.Position>
meth public int compare(javax.swing.text.Position,javax.swing.text.Position)
supr java.lang.Object

CLSS public org.netbeans.lib.editor.util.swing.PositionRegion
cons public init(javax.swing.text.Document,int,int) throws javax.swing.text.BadLocationException
cons public init(javax.swing.text.Position,javax.swing.text.Position)
meth public final int getEndOffset()
meth public final int getLength()
meth public final int getStartOffset()
meth public final javax.swing.text.Position getEndPosition()
meth public final javax.swing.text.Position getStartPosition()
meth public final static java.util.Comparator<org.netbeans.lib.editor.util.swing.PositionRegion> getComparator()
meth public java.lang.CharSequence getText(javax.swing.text.Document)
meth public java.lang.String getString(javax.swing.text.Document) throws javax.swing.text.BadLocationException
meth public java.lang.String toString()
meth public java.lang.String toString(javax.swing.text.Document)
meth public static boolean isRegionsSorted(java.util.List<? extends org.netbeans.lib.editor.util.swing.PositionRegion>)
meth public static javax.swing.text.Position createFixedPosition(int)
supr java.lang.Object
hfds comparator,endPosition,startPosition

