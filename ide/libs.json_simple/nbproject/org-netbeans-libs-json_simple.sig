#Signature file v4.1
#Version 0.35

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Cloneable

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable

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

CLSS public java.util.ArrayList<%0 extends java.lang.Object>
cons public init()
cons public init(int)
cons public init(java.util.Collection<? extends {java.util.ArrayList%0}>)
intf java.io.Serializable
intf java.lang.Cloneable
intf java.util.List<{java.util.ArrayList%0}>
intf java.util.RandomAccess
meth protected void removeRange(int,int)
meth public <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public boolean add({java.util.ArrayList%0})
meth public boolean addAll(int,java.util.Collection<? extends {java.util.ArrayList%0}>)
meth public boolean addAll(java.util.Collection<? extends {java.util.ArrayList%0}>)
meth public boolean contains(java.lang.Object)
meth public boolean isEmpty()
meth public boolean remove(java.lang.Object)
meth public boolean removeAll(java.util.Collection<?>)
meth public boolean removeIf(java.util.function.Predicate<? super {java.util.ArrayList%0}>)
meth public boolean retainAll(java.util.Collection<?>)
meth public int indexOf(java.lang.Object)
meth public int lastIndexOf(java.lang.Object)
meth public int size()
meth public java.lang.Object clone()
meth public java.lang.Object[] toArray()
meth public java.util.Iterator<{java.util.ArrayList%0}> iterator()
meth public java.util.List<{java.util.ArrayList%0}> subList(int,int)
meth public java.util.ListIterator<{java.util.ArrayList%0}> listIterator()
meth public java.util.ListIterator<{java.util.ArrayList%0}> listIterator(int)
meth public java.util.Spliterator<{java.util.ArrayList%0}> spliterator()
meth public void add(int,{java.util.ArrayList%0})
meth public void clear()
meth public void ensureCapacity(int)
meth public void forEach(java.util.function.Consumer<? super {java.util.ArrayList%0}>)
meth public void replaceAll(java.util.function.UnaryOperator<{java.util.ArrayList%0}>)
meth public void sort(java.util.Comparator<? super {java.util.ArrayList%0}>)
meth public void trimToSize()
meth public {java.util.ArrayList%0} get(int)
meth public {java.util.ArrayList%0} remove(int)
meth public {java.util.ArrayList%0} set(int,{java.util.ArrayList%0})
supr java.util.AbstractList<{java.util.ArrayList%0}>

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

CLSS public abstract interface java.util.RandomAccess

CLSS public org.json.simple.ItemList
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,boolean)
meth public int size()
meth public java.lang.String get(int)
meth public java.lang.String toString()
meth public java.lang.String toString(java.lang.String)
meth public java.lang.String[] getArray()
meth public java.util.List getItems()
meth public void add(int,java.lang.String)
meth public void add(java.lang.String)
meth public void addAll(java.lang.String)
meth public void addAll(java.lang.String,java.lang.String)
meth public void addAll(java.lang.String,java.lang.String,boolean)
meth public void addAll(org.json.simple.ItemList)
meth public void clear()
meth public void reset()
meth public void setSP(java.lang.String)
meth public void split(java.lang.String,java.lang.String,java.util.List)
meth public void split(java.lang.String,java.lang.String,java.util.List,boolean)
supr java.lang.Object
hfds items,sp

CLSS public org.json.simple.JSONArray
cons public init()
intf java.util.List
intf org.json.simple.JSONAware
intf org.json.simple.JSONStreamAware
meth public java.lang.String toJSONString()
meth public java.lang.String toString()
meth public static java.lang.String toJSONString(java.util.List)
meth public static void writeJSONString(java.util.List,java.io.Writer) throws java.io.IOException
meth public void writeJSONString(java.io.Writer) throws java.io.IOException
supr java.util.ArrayList
hfds serialVersionUID

CLSS public abstract interface org.json.simple.JSONAware
meth public abstract java.lang.String toJSONString()

CLSS public org.json.simple.JSONObject
cons public init()
cons public init(java.util.Map)
intf java.util.Map
intf org.json.simple.JSONAware
intf org.json.simple.JSONStreamAware
meth public java.lang.String toJSONString()
meth public java.lang.String toString()
meth public static java.lang.String escape(java.lang.String)
meth public static java.lang.String toJSONString(java.util.Map)
meth public static java.lang.String toString(java.lang.String,java.lang.Object)
meth public static void writeJSONString(java.util.Map,java.io.Writer) throws java.io.IOException
meth public void writeJSONString(java.io.Writer) throws java.io.IOException
supr java.util.HashMap
hfds serialVersionUID

CLSS public abstract interface org.json.simple.JSONStreamAware
meth public abstract void writeJSONString(java.io.Writer) throws java.io.IOException

CLSS public org.json.simple.JSONValue
cons public init()
meth public static java.lang.Object parse(java.io.Reader)
meth public static java.lang.Object parse(java.lang.String)
meth public static java.lang.Object parseWithException(java.io.Reader) throws java.io.IOException,org.json.simple.parser.ParseException
meth public static java.lang.Object parseWithException(java.lang.String) throws org.json.simple.parser.ParseException
meth public static java.lang.String escape(java.lang.String)
meth public static java.lang.String toJSONString(java.lang.Object)
meth public static void writeJSONString(java.lang.Object,java.io.Writer) throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface org.json.simple.parser.ContainerFactory
meth public abstract java.util.List creatArrayContainer()
meth public abstract java.util.Map createObjectContainer()

CLSS public abstract interface org.json.simple.parser.ContentHandler
meth public abstract boolean endArray() throws java.io.IOException,org.json.simple.parser.ParseException
meth public abstract boolean endObject() throws java.io.IOException,org.json.simple.parser.ParseException
meth public abstract boolean endObjectEntry() throws java.io.IOException,org.json.simple.parser.ParseException
meth public abstract boolean primitive(java.lang.Object) throws java.io.IOException,org.json.simple.parser.ParseException
meth public abstract boolean startArray() throws java.io.IOException,org.json.simple.parser.ParseException
meth public abstract boolean startObject() throws java.io.IOException,org.json.simple.parser.ParseException
meth public abstract boolean startObjectEntry(java.lang.String) throws java.io.IOException,org.json.simple.parser.ParseException
meth public abstract void endJSON() throws java.io.IOException,org.json.simple.parser.ParseException
meth public abstract void startJSON() throws java.io.IOException,org.json.simple.parser.ParseException

CLSS public org.json.simple.parser.JSONParser
cons public init()
fld public final static int S_END = 6
fld public final static int S_INIT = 0
fld public final static int S_IN_ARRAY = 3
fld public final static int S_IN_ERROR = -1
fld public final static int S_IN_FINISHED_VALUE = 1
fld public final static int S_IN_OBJECT = 2
fld public final static int S_IN_PAIR_VALUE = 5
fld public final static int S_PASSED_PAIR_KEY = 4
meth public int getPosition()
meth public java.lang.Object parse(java.io.Reader) throws java.io.IOException,org.json.simple.parser.ParseException
meth public java.lang.Object parse(java.io.Reader,org.json.simple.parser.ContainerFactory) throws java.io.IOException,org.json.simple.parser.ParseException
meth public java.lang.Object parse(java.lang.String) throws org.json.simple.parser.ParseException
meth public java.lang.Object parse(java.lang.String,org.json.simple.parser.ContainerFactory) throws org.json.simple.parser.ParseException
meth public void parse(java.io.Reader,org.json.simple.parser.ContentHandler) throws java.io.IOException,org.json.simple.parser.ParseException
meth public void parse(java.io.Reader,org.json.simple.parser.ContentHandler,boolean) throws java.io.IOException,org.json.simple.parser.ParseException
meth public void parse(java.lang.String,org.json.simple.parser.ContentHandler) throws org.json.simple.parser.ParseException
meth public void parse(java.lang.String,org.json.simple.parser.ContentHandler,boolean) throws org.json.simple.parser.ParseException
meth public void reset()
meth public void reset(java.io.Reader)
supr java.lang.Object
hfds handlerStatusStack,lexer,status,token

CLSS public org.json.simple.parser.ParseException
cons public init(int)
cons public init(int,int,java.lang.Object)
cons public init(int,java.lang.Object)
fld public final static int ERROR_UNEXPECTED_CHAR = 0
fld public final static int ERROR_UNEXPECTED_EXCEPTION = 2
fld public final static int ERROR_UNEXPECTED_TOKEN = 1
meth public int getErrorType()
meth public int getPosition()
meth public java.lang.Object getUnexpectedObject()
meth public java.lang.String toString()
meth public void setErrorType(int)
meth public void setPosition(int)
meth public void setUnexpectedObject(java.lang.Object)
supr java.lang.Exception
hfds errorType,position,serialVersionUID,unexpectedObject

CLSS public org.json.simple.parser.Yytoken
cons public init(int,java.lang.Object)
fld public final static int TYPE_COLON = 6
fld public final static int TYPE_COMMA = 5
fld public final static int TYPE_EOF = -1
fld public final static int TYPE_LEFT_BRACE = 1
fld public final static int TYPE_LEFT_SQUARE = 3
fld public final static int TYPE_RIGHT_BRACE = 2
fld public final static int TYPE_RIGHT_SQUARE = 4
fld public final static int TYPE_VALUE = 0
fld public int type
fld public java.lang.Object value
meth public java.lang.String toString()
supr java.lang.Object

