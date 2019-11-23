#Signature file v4.1
#Version 1.14

CLSS public java.awt.EventQueue
cons public init()
meth protected void dispatchEvent(java.awt.AWTEvent)
meth protected void pop()
meth public java.awt.AWTEvent getNextEvent() throws java.lang.InterruptedException
meth public java.awt.AWTEvent peekEvent()
meth public java.awt.AWTEvent peekEvent(int)
meth public java.awt.SecondaryLoop createSecondaryLoop()
meth public static boolean isDispatchThread()
meth public static java.awt.AWTEvent getCurrentEvent()
meth public static long getMostRecentEventTime()
meth public static void invokeAndWait(java.lang.Runnable) throws java.lang.InterruptedException,java.lang.reflect.InvocationTargetException
meth public static void invokeLater(java.lang.Runnable)
meth public void postEvent(java.awt.AWTEvent)
meth public void push(java.awt.EventQueue)
supr java.lang.Object
hfds CACHE_LENGTH,DRAG,HIGH_PRIORITY,LOW_PRIORITY,MOVE,NORM_PRIORITY,NUM_PRIORITIES,PAINT,PEER,ULTIMATE_PRIORITY,UPDATE,appContext,classLoader,currentEvent,dispatchThread,dummyRunnable,eventLog,fwDispatcher,javaSecurityAccess,mostRecentEventTime,mostRecentKeyEventTime,name,nextQueue,previousQueue,pushPopCond,pushPopLock,queues,threadGroup,threadInitNumber,waitForID
hcls FwSecondaryLoopWrapper

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Cloneable

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable
hfds serialVersionUID

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

CLSS public abstract interface java.lang.Runnable
 anno 0 java.lang.FunctionalInterface()
meth public abstract void run()

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
hfds CAUSE_CAPTION,EMPTY_THROWABLE_ARRAY,NULL_CAUSE_MESSAGE,SELF_SUPPRESSION_MESSAGE,SUPPRESSED_CAPTION,SUPPRESSED_SENTINEL,UNASSIGNED_STACK,backtrace,cause,detailMessage,serialVersionUID,stackTrace,suppressedExceptions
hcls PrintStreamOrWriter,SentinelHolder,WrappedPrintStream,WrappedPrintWriter

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
hfds MAX_ARRAY_SIZE

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
hcls Itr,ListItr

CLSS public abstract java.util.AbstractSequentialList<%0 extends java.lang.Object>
cons protected init()
meth public abstract java.util.ListIterator<{java.util.AbstractSequentialList%0}> listIterator(int)
meth public boolean addAll(int,java.util.Collection<? extends {java.util.AbstractSequentialList%0}>)
meth public java.util.Iterator<{java.util.AbstractSequentialList%0}> iterator()
meth public void add(int,{java.util.AbstractSequentialList%0})
meth public {java.util.AbstractSequentialList%0} get(int)
meth public {java.util.AbstractSequentialList%0} remove(int)
meth public {java.util.AbstractSequentialList%0} set(int,{java.util.AbstractSequentialList%0})
supr java.util.AbstractList<{java.util.AbstractSequentialList%0}>

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

CLSS public abstract interface java.util.Deque<%0 extends java.lang.Object>
intf java.util.Queue<{java.util.Deque%0}>
meth public abstract boolean add({java.util.Deque%0})
meth public abstract boolean contains(java.lang.Object)
meth public abstract boolean offer({java.util.Deque%0})
meth public abstract boolean offerFirst({java.util.Deque%0})
meth public abstract boolean offerLast({java.util.Deque%0})
meth public abstract boolean remove(java.lang.Object)
meth public abstract boolean removeFirstOccurrence(java.lang.Object)
meth public abstract boolean removeLastOccurrence(java.lang.Object)
meth public abstract int size()
meth public abstract java.util.Iterator<{java.util.Deque%0}> descendingIterator()
meth public abstract java.util.Iterator<{java.util.Deque%0}> iterator()
meth public abstract void addFirst({java.util.Deque%0})
meth public abstract void addLast({java.util.Deque%0})
meth public abstract void push({java.util.Deque%0})
meth public abstract {java.util.Deque%0} element()
meth public abstract {java.util.Deque%0} getFirst()
meth public abstract {java.util.Deque%0} getLast()
meth public abstract {java.util.Deque%0} peek()
meth public abstract {java.util.Deque%0} peekFirst()
meth public abstract {java.util.Deque%0} peekLast()
meth public abstract {java.util.Deque%0} poll()
meth public abstract {java.util.Deque%0} pollFirst()
meth public abstract {java.util.Deque%0} pollLast()
meth public abstract {java.util.Deque%0} pop()
meth public abstract {java.util.Deque%0} remove()
meth public abstract {java.util.Deque%0} removeFirst()
meth public abstract {java.util.Deque%0} removeLast()

CLSS public java.util.LinkedList<%0 extends java.lang.Object>
cons public init()
cons public init(java.util.Collection<? extends {java.util.LinkedList%0}>)
intf java.io.Serializable
intf java.lang.Cloneable
intf java.util.Deque<{java.util.LinkedList%0}>
intf java.util.List<{java.util.LinkedList%0}>
meth public <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public boolean add({java.util.LinkedList%0})
meth public boolean addAll(int,java.util.Collection<? extends {java.util.LinkedList%0}>)
meth public boolean addAll(java.util.Collection<? extends {java.util.LinkedList%0}>)
meth public boolean contains(java.lang.Object)
meth public boolean offer({java.util.LinkedList%0})
meth public boolean offerFirst({java.util.LinkedList%0})
meth public boolean offerLast({java.util.LinkedList%0})
meth public boolean remove(java.lang.Object)
meth public boolean removeFirstOccurrence(java.lang.Object)
meth public boolean removeLastOccurrence(java.lang.Object)
meth public int indexOf(java.lang.Object)
meth public int lastIndexOf(java.lang.Object)
meth public int size()
meth public java.lang.Object clone()
meth public java.lang.Object[] toArray()
meth public java.util.Iterator<{java.util.LinkedList%0}> descendingIterator()
meth public java.util.ListIterator<{java.util.LinkedList%0}> listIterator(int)
meth public java.util.Spliterator<{java.util.LinkedList%0}> spliterator()
meth public void add(int,{java.util.LinkedList%0})
meth public void addFirst({java.util.LinkedList%0})
meth public void addLast({java.util.LinkedList%0})
meth public void clear()
meth public void push({java.util.LinkedList%0})
meth public {java.util.LinkedList%0} element()
meth public {java.util.LinkedList%0} get(int)
meth public {java.util.LinkedList%0} getFirst()
meth public {java.util.LinkedList%0} getLast()
meth public {java.util.LinkedList%0} peek()
meth public {java.util.LinkedList%0} peekFirst()
meth public {java.util.LinkedList%0} peekLast()
meth public {java.util.LinkedList%0} poll()
meth public {java.util.LinkedList%0} pollFirst()
meth public {java.util.LinkedList%0} pollLast()
meth public {java.util.LinkedList%0} pop()
meth public {java.util.LinkedList%0} remove()
meth public {java.util.LinkedList%0} remove(int)
meth public {java.util.LinkedList%0} removeFirst()
meth public {java.util.LinkedList%0} removeLast()
meth public {java.util.LinkedList%0} set(int,{java.util.LinkedList%0})
supr java.util.AbstractSequentialList<{java.util.LinkedList%0}>
hfds first,last,serialVersionUID,size
hcls DescendingIterator,LLSpliterator,ListItr,Node

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

CLSS public abstract interface java.util.Queue<%0 extends java.lang.Object>
intf java.util.Collection<{java.util.Queue%0}>
meth public abstract boolean add({java.util.Queue%0})
meth public abstract boolean offer({java.util.Queue%0})
meth public abstract {java.util.Queue%0} element()
meth public abstract {java.util.Queue%0} peek()
meth public abstract {java.util.Queue%0} poll()
meth public abstract {java.util.Queue%0} remove()

CLSS public javax.swing.RepaintManager
cons public init()
meth public boolean isCompletelyDirty(javax.swing.JComponent)
meth public boolean isDoubleBufferingEnabled()
meth public java.awt.Dimension getDoubleBufferMaximumSize()
meth public java.awt.Image getOffscreenBuffer(java.awt.Component,int,int)
meth public java.awt.Image getVolatileOffscreenBuffer(java.awt.Component,int,int)
meth public java.awt.Rectangle getDirtyRegion(javax.swing.JComponent)
meth public java.lang.String toString()
meth public static javax.swing.RepaintManager currentManager(java.awt.Component)
meth public static javax.swing.RepaintManager currentManager(javax.swing.JComponent)
meth public static void setCurrentManager(javax.swing.RepaintManager)
meth public void addDirtyRegion(java.applet.Applet,int,int,int,int)
meth public void addDirtyRegion(java.awt.Window,int,int,int,int)
meth public void addDirtyRegion(javax.swing.JComponent,int,int,int,int)
meth public void addInvalidComponent(javax.swing.JComponent)
meth public void markCompletelyClean(javax.swing.JComponent)
meth public void markCompletelyDirty(javax.swing.JComponent)
meth public void paintDirtyRegions()
meth public void removeInvalidComponent(javax.swing.JComponent)
meth public void setDoubleBufferMaximumSize(java.awt.Dimension)
meth public void setDoubleBufferingEnabled(boolean)
meth public void validateInvalidComponents()
supr java.lang.Object
hfds BUFFER_STRATEGY_NOT_SPECIFIED,BUFFER_STRATEGY_SPECIFIED_OFF,BUFFER_STRATEGY_SPECIFIED_ON,BUFFER_STRATEGY_TYPE,HANDLE_TOP_LEVEL_PAINT,VOLATILE_LOOP_MAX,bufferStrategyType,dirtyComponents,displayChangedHandler,doubleBufferMaxSize,doubleBufferingEnabled,hwDirtyComponents,invalidComponents,javaSecurityAccess,nativeDoubleBuffering,paintDepth,paintManager,paintThread,painting,processingRunnable,repaintListeners,repaintManagerKey,repaintRoot,runnableList,standardDoubleBuffer,tmp,tmpDirtyComponents,volatileBufferType,volatileImageBufferEnabled,volatileMap
hcls DisplayChangedHandler,DisplayChangedRunnable,DoubleBufferInfo,PaintManager,ProcessingRunnable

CLSS public junit.framework.Assert
 anno 0 java.lang.Deprecated()
cons protected init()
meth public static java.lang.String format(java.lang.String,java.lang.Object,java.lang.Object)
meth public static void assertEquals(boolean,boolean)
meth public static void assertEquals(byte,byte)
meth public static void assertEquals(char,char)
meth public static void assertEquals(double,double,double)
meth public static void assertEquals(float,float,float)
meth public static void assertEquals(int,int)
meth public static void assertEquals(java.lang.Object,java.lang.Object)
meth public static void assertEquals(java.lang.String,boolean,boolean)
meth public static void assertEquals(java.lang.String,byte,byte)
meth public static void assertEquals(java.lang.String,char,char)
meth public static void assertEquals(java.lang.String,double,double,double)
meth public static void assertEquals(java.lang.String,float,float,float)
meth public static void assertEquals(java.lang.String,int,int)
meth public static void assertEquals(java.lang.String,java.lang.Object,java.lang.Object)
meth public static void assertEquals(java.lang.String,java.lang.String)
meth public static void assertEquals(java.lang.String,java.lang.String,java.lang.String)
meth public static void assertEquals(java.lang.String,long,long)
meth public static void assertEquals(java.lang.String,short,short)
meth public static void assertEquals(long,long)
meth public static void assertEquals(short,short)
meth public static void assertFalse(boolean)
meth public static void assertFalse(java.lang.String,boolean)
meth public static void assertNotNull(java.lang.Object)
meth public static void assertNotNull(java.lang.String,java.lang.Object)
meth public static void assertNotSame(java.lang.Object,java.lang.Object)
meth public static void assertNotSame(java.lang.String,java.lang.Object,java.lang.Object)
meth public static void assertNull(java.lang.Object)
meth public static void assertNull(java.lang.String,java.lang.Object)
meth public static void assertSame(java.lang.Object,java.lang.Object)
meth public static void assertSame(java.lang.String,java.lang.Object,java.lang.Object)
meth public static void assertTrue(boolean)
meth public static void assertTrue(java.lang.String,boolean)
meth public static void fail()
meth public static void fail(java.lang.String)
meth public static void failNotEquals(java.lang.String,java.lang.Object,java.lang.Object)
meth public static void failNotSame(java.lang.String,java.lang.Object,java.lang.Object)
meth public static void failSame(java.lang.String)
supr java.lang.Object

CLSS public abstract interface junit.framework.Test
meth public abstract int countTestCases()
meth public abstract void run(junit.framework.TestResult)

CLSS public abstract junit.framework.TestCase
cons public init()
cons public init(java.lang.String)
intf junit.framework.Test
meth protected junit.framework.TestResult createResult()
meth protected void runTest() throws java.lang.Throwable
meth protected void setUp() throws java.lang.Exception
meth protected void tearDown() throws java.lang.Exception
meth public int countTestCases()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public junit.framework.TestResult run()
meth public static java.lang.String format(java.lang.String,java.lang.Object,java.lang.Object)
meth public static void assertEquals(boolean,boolean)
meth public static void assertEquals(byte,byte)
meth public static void assertEquals(char,char)
meth public static void assertEquals(double,double,double)
meth public static void assertEquals(float,float,float)
meth public static void assertEquals(int,int)
meth public static void assertEquals(java.lang.Object,java.lang.Object)
meth public static void assertEquals(java.lang.String,boolean,boolean)
meth public static void assertEquals(java.lang.String,byte,byte)
meth public static void assertEquals(java.lang.String,char,char)
meth public static void assertEquals(java.lang.String,double,double,double)
meth public static void assertEquals(java.lang.String,float,float,float)
meth public static void assertEquals(java.lang.String,int,int)
meth public static void assertEquals(java.lang.String,java.lang.Object,java.lang.Object)
meth public static void assertEquals(java.lang.String,java.lang.String)
meth public static void assertEquals(java.lang.String,java.lang.String,java.lang.String)
meth public static void assertEquals(java.lang.String,long,long)
meth public static void assertEquals(java.lang.String,short,short)
meth public static void assertEquals(long,long)
meth public static void assertEquals(short,short)
meth public static void assertFalse(boolean)
meth public static void assertFalse(java.lang.String,boolean)
meth public static void assertNotNull(java.lang.Object)
meth public static void assertNotNull(java.lang.String,java.lang.Object)
meth public static void assertNotSame(java.lang.Object,java.lang.Object)
meth public static void assertNotSame(java.lang.String,java.lang.Object,java.lang.Object)
meth public static void assertNull(java.lang.Object)
meth public static void assertNull(java.lang.String,java.lang.Object)
meth public static void assertSame(java.lang.Object,java.lang.Object)
meth public static void assertSame(java.lang.String,java.lang.Object,java.lang.Object)
meth public static void assertTrue(boolean)
meth public static void assertTrue(java.lang.String,boolean)
meth public static void fail()
meth public static void fail(java.lang.String)
meth public static void failNotEquals(java.lang.String,java.lang.Object,java.lang.Object)
meth public static void failNotSame(java.lang.String,java.lang.Object,java.lang.Object)
meth public static void failSame(java.lang.String)
meth public void run(junit.framework.TestResult)
meth public void runBare() throws java.lang.Throwable
meth public void setName(java.lang.String)
supr junit.framework.Assert
hfds fName

CLSS public org.netbeans.jellytools.JellyTestCase
cons public init(java.lang.String)
fld public boolean captureScreen
fld public boolean closeAllModal
fld public boolean dumpScreen
fld public boolean waitNoEvent
meth protected !varargs static junit.framework.Test createModuleTest(java.lang.Class,java.lang.String[])
meth protected !varargs static junit.framework.Test createModuleTest(java.lang.String,java.lang.String,java.lang.Class,java.lang.String[])
meth protected void clearTestStatus()
meth protected void endTest()
meth protected void failNotify(java.lang.Throwable)
meth protected void initEnvironment()
meth protected void runTest() throws java.lang.Throwable
meth protected void startTest()
meth public !varargs void closeOpenedProjects(java.lang.Object[])
meth public !varargs void openDataProjects(java.lang.String[]) throws java.io.IOException
meth public !varargs void openProjects(java.lang.String[]) throws java.io.IOException
meth public static org.netbeans.junit.NbModuleSuite$Configuration emptyConfiguration()
meth public static void closeAllModal()
meth public void closeOpenedProjects()
meth public void fail(java.lang.Throwable)
meth public void runBare() throws java.lang.Throwable
meth public void waitScanFinished()
supr org.netbeans.junit.NbTestCase
hfds isScreenCaptured,openedProjects,testStatus

CLSS public abstract interface org.netbeans.junit.NbPerformanceTest
innr public static PerformanceData
intf org.netbeans.junit.NbTest
meth public abstract org.netbeans.junit.NbPerformanceTest$PerformanceData[] getPerformanceData()

CLSS public org.netbeans.junit.NbPerformanceTestCase
cons public init(java.lang.String)
intf org.netbeans.junit.NbPerformanceTest
meth public org.netbeans.junit.NbPerformanceTest$PerformanceData[] getPerformanceData()
meth public void reportPerformance(java.lang.String,long)
meth public void reportPerformance(java.lang.String,long,java.lang.String,int)
meth public void reportPerformance(java.lang.String,long,java.lang.String,int,long)
meth public void reportPerformance(long)
meth public void reportPerformance(long,java.lang.String)
supr org.netbeans.junit.NbTestCase
hfds data

CLSS public abstract interface org.netbeans.junit.NbTest
intf junit.framework.Test
meth public abstract boolean canRun()
meth public abstract java.lang.String getExpectedFail()
meth public abstract void setFilter(org.netbeans.junit.Filter)

CLSS public abstract org.netbeans.junit.NbTestCase
cons public init(java.lang.String)
intf org.netbeans.junit.NbTest
meth protected boolean runInEQ()
meth protected final int getTestNumber()
meth protected int timeOut()
meth protected java.lang.String logRoot()
meth protected java.util.logging.Level logLevel()
meth public boolean canRun()
meth public java.io.File getDataDir()
meth public java.io.File getGoldenFile()
meth public java.io.File getGoldenFile(java.lang.String)
meth public java.io.File getWorkDir() throws java.io.IOException
meth public java.io.PrintStream getLog()
meth public java.io.PrintStream getLog(java.lang.String)
meth public java.io.PrintStream getRef()
meth public java.lang.String getExpectedFail()
meth public java.lang.String getWorkDirPath()
meth public static int assertSize(java.lang.String,java.util.Collection<?>,int,org.netbeans.junit.MemoryFilter)
meth public static java.lang.String convertNBFSURL(java.net.URL)
 anno 0 java.lang.Deprecated()
meth public static void assertFile(java.io.File,java.io.File)
meth public static void assertFile(java.io.File,java.io.File,java.io.File)
meth public static void assertFile(java.io.File,java.io.File,java.io.File,org.netbeans.junit.diff.Diff)
meth public static void assertFile(java.lang.String,java.io.File,java.io.File,java.io.File)
meth public static void assertFile(java.lang.String,java.io.File,java.io.File,java.io.File,org.netbeans.junit.diff.Diff)
meth public static void assertFile(java.lang.String,java.lang.String)
meth public static void assertFile(java.lang.String,java.lang.String,java.lang.String)
meth public static void assertFile(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public static void assertFile(java.lang.String,java.lang.String,java.lang.String,java.lang.String,org.netbeans.junit.diff.Diff)
meth public static void assertFile(java.lang.String,java.lang.String,java.lang.String,org.netbeans.junit.diff.Diff)
meth public static void assertGC(java.lang.String,java.lang.ref.Reference<?>)
meth public static void assertGC(java.lang.String,java.lang.ref.Reference<?>,java.util.Set<?>)
meth public static void assertSize(java.lang.String,int,java.lang.Object)
meth public static void assertSize(java.lang.String,java.util.Collection<?>,int)
meth public static void assertSize(java.lang.String,java.util.Collection<?>,int,java.lang.Object[])
meth public static void failByBug(int)
meth public static void failByBug(int,java.lang.String)
meth public void clearWorkDir() throws java.io.IOException
meth public void compareReferenceFiles()
meth public void compareReferenceFiles(java.lang.String,java.lang.String,java.lang.String)
meth public void log(java.lang.String)
meth public void log(java.lang.String,java.lang.String)
meth public void ref(java.lang.String)
meth public void run(junit.framework.TestResult)
meth public void runBare() throws java.lang.Throwable
meth public void setFilter(org.netbeans.junit.Filter)
supr junit.framework.TestCase
hfds DEFAULT_TIME_OUT_CALLED,filter,lastTestMethod,logStreamTable,radix,systemOutPSWrapper,time,usedPaths,vmDeadline,workDirPath
hcls WFOS

CLSS public org.netbeans.modules.performance.guitracker.ActionTracker
fld public final static int TRACK_APPLICATION_MESSAGE = 50
fld public final static int TRACK_COMPONENT_HIDE = 3103
fld public final static int TRACK_COMPONENT_RESIZE = 3101
fld public final static int TRACK_COMPONENT_SHOW = 3102
fld public final static int TRACK_CONFIG_APPLICATION_MESSAGE = 51
fld public final static int TRACK_DIALOG_HIDE = 2103
fld public final static int TRACK_DIALOG_RESIZE = 2101
fld public final static int TRACK_DIALOG_SHOW = 2102
fld public final static int TRACK_FOCUS_GAINED = 80
fld public final static int TRACK_FOCUS_LOST = 81
fld public final static int TRACK_FRAME_HIDE = 1103
fld public final static int TRACK_FRAME_RESIZE = 1101
fld public final static int TRACK_FRAME_SHOW = 1102
fld public final static int TRACK_INVOCATION = 82
fld public final static int TRACK_KEY_PRESS = 20
fld public final static int TRACK_KEY_RELEASE = 21
fld public final static int TRACK_MOUSE_DRAGGED = 12
fld public final static int TRACK_MOUSE_MOVED = 13
fld public final static int TRACK_MOUSE_PRESS = 10
fld public final static int TRACK_MOUSE_RELEASE = 11
fld public final static int TRACK_OPEN_AFTER_TRACE_MESSAGE = 54
fld public final static int TRACK_OPEN_BEFORE_TRACE_MESSAGE = 53
fld public final static int TRACK_PAINT = 2
fld public final static int TRACK_START = 1
fld public final static int TRACK_TRACE_MESSAGE = 52
fld public final static int TRACK_UNKNOWN = 83
fld public final static java.lang.String ATTR_MEASURED = "measured"
fld public final static java.lang.String ATTR_NAME = "name"
fld public final static java.lang.String ATTR_START = "start"
fld public final static java.lang.String ATTR_TIME = "time"
fld public final static java.lang.String ATTR_TIME_DIFF_DRAG = "diffdrag"
fld public final static java.lang.String ATTR_TIME_DIFF_START = "diff"
fld public final static java.lang.String ATTR_TYPE = "type"
fld public final static java.lang.String TN_EVENT = "event"
fld public final static java.lang.String TN_EVENT_LIST = "event-list"
fld public final static java.lang.String TN_ROOT_ELEMENT = "action-tracking"
innr public final EventList
innr public final Tuple
meth public boolean isRecording()
meth public java.util.LinkedList<org.netbeans.modules.performance.guitracker.ActionTracker$EventList> getEventLists()
meth public long getAWTEventListengingMask()
meth public org.netbeans.modules.performance.guitracker.ActionTracker$EventList getCurrentEvents()
meth public static java.lang.String getNameForCode(int)
meth public static java.lang.String logComponentAndItsParents(java.awt.Component)
meth public static org.netbeans.modules.performance.guitracker.ActionTracker getInstance()
meth public void add(int,java.lang.String)
meth public void add(int,java.lang.String,boolean)
meth public void add(int,java.lang.String,long)
meth public void add(java.awt.AWTEvent)
meth public void add(org.netbeans.modules.performance.guitracker.ActionTracker$Tuple)
meth public void connectToAWT(boolean)
meth public void exportAsXML() throws javax.xml.parsers.ParserConfigurationException,javax.xml.transform.TransformerException
meth public void exportAsXML(java.io.PrintStream) throws javax.xml.parsers.ParserConfigurationException,javax.xml.transform.TransformerException
meth public void exportAsXML(org.w3c.dom.Document,java.io.PrintStream) throws javax.xml.parsers.ParserConfigurationException,javax.xml.transform.TransformerException
meth public void forgetAllEvents()
meth public void forgetCurrentEvents()
meth public void scenarioFinished()
meth public void setAWTEventListeningMask(long)
meth public void setExportXMLWhenScenarioFinished(boolean)
meth public void setOutputFileName(java.lang.String)
meth public void setXslLocation(java.lang.String)
meth public void startNewEventList(java.lang.String)
meth public void startRecording()
meth public void stopRecording()
supr java.lang.Object
hfds TRACK_COMPONENT,TRACK_DIALOG,TRACK_FRAME,actionTrackerXslLocation,allowRecording,awt_event_mask,awt_listener,connected,currentEvents,dbfactory,dbld,default_awt_event_mask,eventLists,exportXmlWhenScenarioFinished,fnActionOutput,instance,interactive,tfactory
hcls OurAWTEventListener

CLSS public final org.netbeans.modules.performance.guitracker.ActionTracker$EventList
 outer org.netbeans.modules.performance.guitracker.ActionTracker
cons public init(org.netbeans.modules.performance.guitracker.ActionTracker,java.lang.String)
meth public boolean add(org.netbeans.modules.performance.guitracker.ActionTracker$Tuple)
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public long getStartMillis()
meth public void start()
supr java.util.LinkedList<org.netbeans.modules.performance.guitracker.ActionTracker$Tuple>
hfds name,startMillies

CLSS public final org.netbeans.modules.performance.guitracker.ActionTracker$Tuple
 outer org.netbeans.modules.performance.guitracker.ActionTracker
cons public init(org.netbeans.modules.performance.guitracker.ActionTracker,int,java.lang.String,long)
cons public init(org.netbeans.modules.performance.guitracker.ActionTracker,int,java.lang.String,long,long)
cons public init(org.netbeans.modules.performance.guitracker.ActionTracker,int,java.lang.String,long,long,boolean)
meth public boolean equals(java.lang.Object)
meth public boolean getMeasured()
meth public int getCode()
meth public java.lang.String getCodeName()
meth public java.lang.String getMeasurementThreadName()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public long getTimeDifference()
meth public long getTimeMillis()
meth public void setMeasured(boolean)
supr java.lang.Object
hfds code,diffies,measured,measurementThreadName,millies,name

CLSS public org.netbeans.modules.performance.guitracker.LoggingEventQueue
cons public init(org.netbeans.modules.performance.guitracker.ActionTracker)
meth public boolean isEnabled()
meth public void postEvent(java.awt.AWTEvent)
meth public void push(java.awt.EventQueue)
meth public void setEnabled(boolean)
supr java.awt.EventQueue
hfds orig,popMethod,tr

CLSS public org.netbeans.modules.performance.guitracker.LoggingRepaintManager
cons public init(org.netbeans.modules.performance.guitracker.ActionTracker)
fld public final static org.netbeans.modules.performance.guitracker.LoggingRepaintManager$RegionFilter EDITOR_FILTER
fld public final static org.netbeans.modules.performance.guitracker.LoggingRepaintManager$RegionFilter EXPLORER_FILTER
fld public final static org.netbeans.modules.performance.guitracker.LoggingRepaintManager$RegionFilter IGNORE_DIFF_SIDEBAR_FILTER
fld public final static org.netbeans.modules.performance.guitracker.LoggingRepaintManager$RegionFilter IGNORE_EXPLORER_TREE_FILTER
fld public final static org.netbeans.modules.performance.guitracker.LoggingRepaintManager$RegionFilter IGNORE_STATUS_LINE_FILTER
innr public abstract interface static RegionFilter
meth public boolean acceptedByRegionFilters(javax.swing.JComponent)
meth public boolean isEnabled()
meth public long waitNoPaintEvent(long)
meth public static java.lang.String getContainersChain(java.awt.Container)
meth public static java.lang.String logComponent(java.awt.Component)
meth public static java.lang.String logContainerAndItsParents(java.awt.Container)
meth public static long measureStartup()
meth public void addDirtyRegion(javax.swing.JComponent,int,int,int,int)
meth public void addRegionFilter(org.netbeans.modules.performance.guitracker.LoggingRepaintManager$RegionFilter)
meth public void paintDirtyRegions()
meth public void removeRegionFilter(org.netbeans.modules.performance.guitracker.LoggingRepaintManager$RegionFilter)
meth public void resetRegionFilters()
meth public void setEnabled(boolean)
supr javax.swing.RepaintManager
hfds DEBUG_MODE,MAX_TIMEOUT,OS_NAME,hasDirtyMatches,lastPaint,orig,regionFilters,tr

CLSS public abstract interface static org.netbeans.modules.performance.guitracker.LoggingRepaintManager$RegionFilter
 outer org.netbeans.modules.performance.guitracker.LoggingRepaintManager
meth public abstract boolean accept(javax.swing.JComponent)
meth public abstract java.lang.String getFilterName()

CLSS public org.netbeans.modules.performance.guitracker.Main
cons public init()
meth public static void main(java.lang.String[])
supr java.lang.Object

CLSS public org.netbeans.modules.performance.utilities.CommonUtilities
cons public init()
fld public final static java.lang.String SOURCE_PACKAGES = "Source Packages"
fld public final static java.lang.String TEST_PACKAGES = "Test Packages"
meth protected static void waitForProjectCreation(int,boolean)
meth public static java.lang.String createproject(java.lang.String,java.lang.String,boolean)
meth public static java.lang.String getProjectsDir()
meth public static java.lang.String getTempDir()
meth public static java.lang.String getTimeIndex()
meth public static java.lang.String jEditProjectOpen()
meth public static org.netbeans.jellytools.EditorOperator editFile(java.lang.String,java.lang.String,java.lang.String)
meth public static org.netbeans.jellytools.EditorOperator openFile(java.lang.String,java.lang.String,java.lang.String,boolean)
meth public static org.netbeans.jellytools.EditorOperator openFile(org.netbeans.jellytools.nodes.Node,java.lang.String,boolean)
meth public static org.netbeans.jellytools.modules.form.FormDesignerOperator openSmallFormFile()
meth public static org.netbeans.jellytools.nodes.Node getApplicationServerNode()
meth public static org.netbeans.jellytools.nodes.Node getTomcatServerNode()
meth public static org.netbeans.jellytools.nodes.Node startTomcatServer()
meth public static org.netbeans.jellytools.nodes.Node stopTomcatServer()
meth public static void actionOnProject(java.lang.String,java.lang.String)
meth public static void addApplicationServer()
meth public static void addTomcatServer()
meth public static void buildProject(java.lang.String)
meth public static void cleanTempDir() throws java.io.IOException
meth public static void closeAllDocuments()
meth public static void closeBluePrints()
meth public static void closeLog()
meth public static void closeMemoryToolbar()
meth public static void copyFile(java.io.File,java.io.File) throws java.io.IOException
meth public static void debugProject(java.lang.String)
meth public static void deleteFile(java.io.File) throws java.io.IOException
meth public static void deleteProject(java.lang.String)
meth public static void deleteProject(java.lang.String,boolean)
meth public static void deployProject(java.lang.String)
meth public static void initLog(org.netbeans.modules.performance.utilities.PerformanceTestCase)
meth public static void insertToFile(java.lang.String,int,java.lang.String,boolean)
meth public static void installPlugin(java.lang.String)
meth public static void killDebugOnProject(java.lang.String)
meth public static void killRunOnProject(java.lang.String)
meth public static void maximizeWholeNetbeansWindow()
meth public static void openFiles(java.lang.String,java.lang.String[][])
meth public static void processUnitTestsResults(java.lang.String,java.lang.String,org.netbeans.junit.NbPerformanceTest$PerformanceData)
meth public static void processUnitTestsResults(java.lang.String,org.netbeans.junit.NbPerformanceTest$PerformanceData)
meth public static void runProject(java.lang.String)
meth public static void setSpellcheckerEnabled(boolean)
meth public static void testProject(java.lang.String)
meth public static void verifyProject(java.lang.String)
meth public static void waitForPendingBackgroundTasks()
meth public static void waitProjectOpenedScanFinished(java.lang.String)
meth public static void waitScanFinished()
meth public static void workarroundMainMenuRolledUp()
meth public static void xmlTestResults(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,long,long[],int)
supr java.lang.Object
hfds allPerfDoc,db,dbf,perfDataTag,projectsDir,tempDir,test,testResultsTag,testSuiteTag,testTag

CLSS public org.netbeans.modules.performance.utilities.LoggingScanClasspath
cons public init()
innr public PerformanceScanData
meth public static java.util.ArrayList<org.netbeans.modules.performance.utilities.LoggingScanClasspath$PerformanceScanData> getData()
meth public static void printMeasuredValues(java.io.PrintStream)
meth public void reportScanOfFile(java.lang.String,java.lang.Long)
supr java.lang.Object
hfds data

CLSS public org.netbeans.modules.performance.utilities.LoggingScanClasspath$PerformanceScanData
 outer org.netbeans.modules.performance.utilities.LoggingScanClasspath
cons public init(org.netbeans.modules.performance.utilities.LoggingScanClasspath)
cons public init(org.netbeans.modules.performance.utilities.LoggingScanClasspath,java.lang.String,java.lang.Long)
cons public init(org.netbeans.modules.performance.utilities.LoggingScanClasspath,java.lang.String,java.lang.Long,java.lang.String)
meth public java.lang.String getFullyQualifiedName()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public long getValue()
meth public void setFullyQualifiedName(java.lang.String)
meth public void setName(java.lang.String)
meth public void setValue(long)
supr java.lang.Object
hfds fullyQualifiedName,name,value

CLSS public org.netbeans.modules.performance.utilities.MeasureStartupTimeTestCase
cons public init(java.lang.String)
fld protected final static java.lang.String MAC = "mac"
fld protected final static java.lang.String UNIX = "unix"
fld protected final static java.lang.String UNKNOWN = "unknown"
fld protected final static java.lang.String WINDOWS = "windows"
fld protected final static java.lang.String[][] STARTUP_DATA
fld protected final static java.lang.String[][] SUPPORTED_PLATFORMS
fld protected final static long UNKNOWN_TIME = -1
fld protected static int repeat
fld protected static int repeatNewUserdir
fld public final static java.lang.String separator
innr protected static CantParseMeasuredValuesException
innr public static ThreadReader
meth protected java.io.File getMeasureFile(int) throws java.io.IOException
meth protected java.io.File getMeasureFile(int,int) throws java.io.IOException
meth protected java.io.File getUserdirFile() throws java.io.IOException
meth protected java.io.File getUserdirFile(int) throws java.io.IOException
meth protected long runIDEandMeasureStartup(java.lang.String,java.io.File,java.io.File,long) throws java.io.IOException
meth protected static java.io.File getIdeHome() throws java.io.IOException
meth protected static java.lang.String getPlatform()
meth protected static java.util.HashMap<java.lang.String,java.lang.Long> parseMeasuredValues(java.io.File) throws org.netbeans.modules.performance.utilities.MeasureStartupTimeTestCase$CantParseMeasuredValuesException
meth protected static long runIDE(java.io.File,java.io.File,java.io.File,long) throws java.io.IOException
meth protected void measureComplexStartupTime(java.lang.String) throws java.io.IOException
meth public static void createStatusFile() throws java.io.IOException
supr org.netbeans.junit.NbPerformanceTestCase
hcls StatusFile

CLSS protected static org.netbeans.modules.performance.utilities.MeasureStartupTimeTestCase$CantParseMeasuredValuesException
 outer org.netbeans.modules.performance.utilities.MeasureStartupTimeTestCase
cons public init(java.lang.Throwable)
supr java.lang.Exception

CLSS public static org.netbeans.modules.performance.utilities.MeasureStartupTimeTestCase$ThreadReader
 outer org.netbeans.modules.performance.utilities.MeasureStartupTimeTestCase
cons public init(java.io.InputStream,java.io.PrintStream)
intf java.lang.Runnable
meth public void run()
meth public void stop()
supr java.lang.Object
hfds br,out,s,thread

CLSS public abstract org.netbeans.modules.performance.utilities.MemoryFootprintTestCase
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
fld protected java.lang.String prefix
innr public final MeasuredMemoryValue
meth public void initialize()
meth public void testMeasureMemoryFootprint()
supr org.netbeans.modules.performance.utilities.PerformanceTestCase
hfds MAC,SUPPORTED_PLATFORMS,UNIX,UNKNOWN,WINDOWS,pid,platform

CLSS public final org.netbeans.modules.performance.utilities.MemoryFootprintTestCase$MeasuredMemoryValue
 outer org.netbeans.modules.performance.utilities.MemoryFootprintTestCase
cons public init(org.netbeans.modules.performance.utilities.MemoryFootprintTestCase)
meth public void reportPerformanceData(java.lang.String,int)
supr java.lang.Object
hfds classes_loaded,classes_unloaded,heap_commited,heap_used,permGen_commited,permGen_used,rss,vsz

CLSS public abstract org.netbeans.modules.performance.utilities.PerformanceTestCase
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
fld protected final static int MY_EVENT_NOT_AVAILABLE = -10
fld protected final static long UI_RESPONSE = 100
fld protected final static long WINDOW_OPEN = 1000
fld protected int MY_END_EVENT
fld protected int MY_START_EVENT
fld protected int track_mouse_event
fld protected java.util.HashMap<java.lang.String,java.lang.String> renamedTestCaseName
fld protected org.netbeans.jemmy.operators.ComponentOperator testedComponentOperator
fld protected static int repeat
fld protected static int repeat_memory
fld public boolean useTwoOrderTypes
fld public double HEURISTIC_FACTOR
fld public final static java.lang.String OPEN_AFTER = "OPEN - after"
fld public final static java.lang.String OPEN_BEFORE = "OPEN - before"
fld public int MAX_ITERATION
fld public int WAIT_AFTER_CLOSE
fld public int WAIT_AFTER_OPEN
fld public int WAIT_AFTER_PREPARE
fld public int WAIT_PAINT
fld public int iteration
fld public long expectedTime
intf org.netbeans.junit.NbPerformanceTest
meth protected org.netbeans.modules.performance.guitracker.LoggingRepaintManager repaintManager()
meth protected void addEditorPhaseHandler()
meth protected void disableEditorCaretBlinking()
meth protected void getScreenshot(java.lang.String)
meth protected void getScreenshotOfMeasuredIDEInTimeOfMeasurement(int)
meth protected void initialize()
meth protected void logMemoryUsage()
meth protected void removeEditorPhaseHandler()
meth protected void reportReference(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void restoreEditorCaretBlinking()
meth protected void runTestGC(java.lang.Object) throws java.lang.Exception
meth protected void shutdown()
meth protected void waitNoEvent(long)
meth public abstract org.netbeans.jemmy.operators.ComponentOperator open()
meth public abstract void prepare()
meth public java.lang.String getName()
meth public java.lang.String getPerformanceName()
meth public java.lang.String setPerformanceName()
meth public long getMeasuredTime()
meth public org.netbeans.junit.NbPerformanceTest$PerformanceData[] getPerformanceData()
meth public static void closeAllDialogs()
meth public static void disablePHPReadmeHTML()
meth public static void disableStatusBarEffects()
meth public static void prepareForMeasurements()
meth public void checkScanFinished()
meth public void close()
meth public void compare(java.lang.String,long[])
meth public void doMeasurement()
meth public void dumpLog()
meth public void measureMemoryUsage()
meth public void measureTime()
meth public void reportPerformance(java.lang.String,long,java.lang.String,int)
meth public void reportPerformance(java.lang.String,long,java.lang.String,int,long)
meth public void runGC(int)
meth public void setTestCaseName(java.lang.String,java.lang.String)
meth public void setUp()
meth public void tearDown()
supr org.netbeans.modules.performance.utilities.PerformanceTestCase2
hfds CARET_BLINK_RATE_KEY,DEFAULT_REFS_GROUP,LOG,caretBlinkingDisabled,data,defaultCaretBlinkRate,logMemory,phaseHandler,profile,rm,tr,tracedRefs
hcls PhaseHandler,Profile

CLSS public org.netbeans.modules.performance.utilities.PerformanceTestCase2
cons public init(java.lang.String)
meth public !varargs void openDataProjects(java.lang.String[]) throws java.io.IOException
supr org.netbeans.jellytools.JellyTestCase

CLSS public abstract org.netbeans.modules.performance.utilities.ValidatePopupMenuOnNodes
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
fld protected org.netbeans.jellytools.nodes.Node dataObjectNode
meth public org.netbeans.jemmy.operators.ComponentOperator open()
meth public void close()
meth public void prepare()
supr org.netbeans.modules.performance.utilities.PerformanceTestCase

