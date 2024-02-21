#Signature file v4.1
#Version 0.51.0

CLSS public abstract java.awt.Component
cons protected init()
fld protected javax.accessibility.AccessibleContext accessibleContext
fld public final static float BOTTOM_ALIGNMENT = 1.0
fld public final static float CENTER_ALIGNMENT = 0.5
fld public final static float LEFT_ALIGNMENT = 0.0
fld public final static float RIGHT_ALIGNMENT = 1.0
fld public final static float TOP_ALIGNMENT = 0.0
innr protected BltBufferStrategy
innr protected FlipBufferStrategy
innr protected abstract AccessibleAWTComponent
innr public final static !enum BaselineResizeBehavior
intf java.awt.MenuContainer
intf java.awt.image.ImageObserver
intf java.io.Serializable
meth protected boolean requestFocus(boolean)
meth protected boolean requestFocusInWindow(boolean)
meth protected final void disableEvents(long)
meth protected final void enableEvents(long)
meth protected java.awt.AWTEvent coalesceEvents(java.awt.AWTEvent,java.awt.AWTEvent)
meth protected java.lang.String paramString()
meth protected void firePropertyChange(java.lang.String,boolean,boolean)
meth protected void firePropertyChange(java.lang.String,int,int)
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void processComponentEvent(java.awt.event.ComponentEvent)
meth protected void processEvent(java.awt.AWTEvent)
meth protected void processFocusEvent(java.awt.event.FocusEvent)
meth protected void processHierarchyBoundsEvent(java.awt.event.HierarchyEvent)
meth protected void processHierarchyEvent(java.awt.event.HierarchyEvent)
meth protected void processInputMethodEvent(java.awt.event.InputMethodEvent)
meth protected void processKeyEvent(java.awt.event.KeyEvent)
meth protected void processMouseEvent(java.awt.event.MouseEvent)
meth protected void processMouseMotionEvent(java.awt.event.MouseEvent)
meth protected void processMouseWheelEvent(java.awt.event.MouseWheelEvent)
meth public <%0 extends java.util.EventListener> {%%0}[] getListeners(java.lang.Class<{%%0}>)
meth public boolean action(java.awt.Event,java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public boolean areFocusTraversalKeysSet(int)
meth public boolean contains(int,int)
meth public boolean contains(java.awt.Point)
meth public boolean getFocusTraversalKeysEnabled()
meth public boolean getIgnoreRepaint()
meth public boolean gotFocus(java.awt.Event,java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public boolean handleEvent(java.awt.Event)
 anno 0 java.lang.Deprecated()
meth public boolean hasFocus()
meth public boolean imageUpdate(java.awt.Image,int,int,int,int,int)
meth public boolean inside(int,int)
 anno 0 java.lang.Deprecated()
meth public boolean isBackgroundSet()
meth public boolean isCursorSet()
meth public boolean isDisplayable()
meth public boolean isDoubleBuffered()
meth public boolean isEnabled()
meth public boolean isFocusCycleRoot(java.awt.Container)
meth public boolean isFocusOwner()
meth public boolean isFocusTraversable()
 anno 0 java.lang.Deprecated()
meth public boolean isFocusable()
meth public boolean isFontSet()
meth public boolean isForegroundSet()
meth public boolean isLightweight()
meth public boolean isMaximumSizeSet()
meth public boolean isMinimumSizeSet()
meth public boolean isOpaque()
meth public boolean isPreferredSizeSet()
meth public boolean isShowing()
meth public boolean isValid()
meth public boolean isVisible()
meth public boolean keyDown(java.awt.Event,int)
 anno 0 java.lang.Deprecated()
meth public boolean keyUp(java.awt.Event,int)
 anno 0 java.lang.Deprecated()
meth public boolean lostFocus(java.awt.Event,java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public boolean mouseDown(java.awt.Event,int,int)
 anno 0 java.lang.Deprecated()
meth public boolean mouseDrag(java.awt.Event,int,int)
 anno 0 java.lang.Deprecated()
meth public boolean mouseEnter(java.awt.Event,int,int)
 anno 0 java.lang.Deprecated()
meth public boolean mouseExit(java.awt.Event,int,int)
 anno 0 java.lang.Deprecated()
meth public boolean mouseMove(java.awt.Event,int,int)
 anno 0 java.lang.Deprecated()
meth public boolean mouseUp(java.awt.Event,int,int)
 anno 0 java.lang.Deprecated()
meth public boolean postEvent(java.awt.Event)
 anno 0 java.lang.Deprecated()
meth public boolean prepareImage(java.awt.Image,int,int,java.awt.image.ImageObserver)
meth public boolean prepareImage(java.awt.Image,java.awt.image.ImageObserver)
meth public boolean requestFocusInWindow()
meth public final java.lang.Object getTreeLock()
meth public final void dispatchEvent(java.awt.AWTEvent)
meth public float getAlignmentX()
meth public float getAlignmentY()
meth public int checkImage(java.awt.Image,int,int,java.awt.image.ImageObserver)
meth public int checkImage(java.awt.Image,java.awt.image.ImageObserver)
meth public int getBaseline(int,int)
meth public int getHeight()
meth public int getWidth()
meth public int getX()
meth public int getY()
meth public java.awt.Color getBackground()
meth public java.awt.Color getForeground()
meth public java.awt.Component getComponentAt(int,int)
meth public java.awt.Component getComponentAt(java.awt.Point)
meth public java.awt.Component locate(int,int)
 anno 0 java.lang.Deprecated()
meth public java.awt.Component$BaselineResizeBehavior getBaselineResizeBehavior()
meth public java.awt.ComponentOrientation getComponentOrientation()
meth public java.awt.Container getFocusCycleRootAncestor()
meth public java.awt.Container getParent()
meth public java.awt.Cursor getCursor()
meth public java.awt.Dimension getMaximumSize()
meth public java.awt.Dimension getMinimumSize()
meth public java.awt.Dimension getPreferredSize()
meth public java.awt.Dimension getSize()
meth public java.awt.Dimension getSize(java.awt.Dimension)
meth public java.awt.Dimension minimumSize()
 anno 0 java.lang.Deprecated()
meth public java.awt.Dimension preferredSize()
 anno 0 java.lang.Deprecated()
meth public java.awt.Dimension size()
 anno 0 java.lang.Deprecated()
meth public java.awt.Font getFont()
meth public java.awt.FontMetrics getFontMetrics(java.awt.Font)
meth public java.awt.Graphics getGraphics()
meth public java.awt.GraphicsConfiguration getGraphicsConfiguration()
meth public java.awt.Image createImage(int,int)
meth public java.awt.Image createImage(java.awt.image.ImageProducer)
meth public java.awt.Point getLocation()
meth public java.awt.Point getLocation(java.awt.Point)
meth public java.awt.Point getLocationOnScreen()
meth public java.awt.Point getMousePosition()
meth public java.awt.Point location()
 anno 0 java.lang.Deprecated()
meth public java.awt.Rectangle bounds()
 anno 0 java.lang.Deprecated()
meth public java.awt.Rectangle getBounds()
meth public java.awt.Rectangle getBounds(java.awt.Rectangle)
meth public java.awt.Toolkit getToolkit()
meth public java.awt.dnd.DropTarget getDropTarget()
meth public java.awt.event.ComponentListener[] getComponentListeners()
meth public java.awt.event.FocusListener[] getFocusListeners()
meth public java.awt.event.HierarchyBoundsListener[] getHierarchyBoundsListeners()
meth public java.awt.event.HierarchyListener[] getHierarchyListeners()
meth public java.awt.event.InputMethodListener[] getInputMethodListeners()
meth public java.awt.event.KeyListener[] getKeyListeners()
meth public java.awt.event.MouseListener[] getMouseListeners()
meth public java.awt.event.MouseMotionListener[] getMouseMotionListeners()
meth public java.awt.event.MouseWheelListener[] getMouseWheelListeners()
meth public java.awt.im.InputContext getInputContext()
meth public java.awt.im.InputMethodRequests getInputMethodRequests()
meth public java.awt.image.ColorModel getColorModel()
meth public java.awt.image.VolatileImage createVolatileImage(int,int)
meth public java.awt.image.VolatileImage createVolatileImage(int,int,java.awt.ImageCapabilities) throws java.awt.AWTException
meth public java.beans.PropertyChangeListener[] getPropertyChangeListeners()
meth public java.beans.PropertyChangeListener[] getPropertyChangeListeners(java.lang.String)
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.util.Locale getLocale()
meth public java.util.Set<java.awt.AWTKeyStroke> getFocusTraversalKeys(int)
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public void add(java.awt.PopupMenu)
meth public void addComponentListener(java.awt.event.ComponentListener)
meth public void addFocusListener(java.awt.event.FocusListener)
meth public void addHierarchyBoundsListener(java.awt.event.HierarchyBoundsListener)
meth public void addHierarchyListener(java.awt.event.HierarchyListener)
meth public void addInputMethodListener(java.awt.event.InputMethodListener)
meth public void addKeyListener(java.awt.event.KeyListener)
meth public void addMouseListener(java.awt.event.MouseListener)
meth public void addMouseMotionListener(java.awt.event.MouseMotionListener)
meth public void addMouseWheelListener(java.awt.event.MouseWheelListener)
meth public void addNotify()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void applyComponentOrientation(java.awt.ComponentOrientation)
meth public void deliverEvent(java.awt.Event)
 anno 0 java.lang.Deprecated()
meth public void disable()
 anno 0 java.lang.Deprecated()
meth public void doLayout()
meth public void enable()
 anno 0 java.lang.Deprecated()
meth public void enable(boolean)
 anno 0 java.lang.Deprecated()
meth public void enableInputMethods(boolean)
meth public void firePropertyChange(java.lang.String,byte,byte)
meth public void firePropertyChange(java.lang.String,char,char)
meth public void firePropertyChange(java.lang.String,double,double)
meth public void firePropertyChange(java.lang.String,float,float)
meth public void firePropertyChange(java.lang.String,long,long)
meth public void firePropertyChange(java.lang.String,short,short)
meth public void hide()
 anno 0 java.lang.Deprecated()
meth public void invalidate()
meth public void layout()
 anno 0 java.lang.Deprecated()
meth public void list()
meth public void list(java.io.PrintStream)
meth public void list(java.io.PrintStream,int)
meth public void list(java.io.PrintWriter)
meth public void list(java.io.PrintWriter,int)
meth public void move(int,int)
 anno 0 java.lang.Deprecated()
meth public void nextFocus()
 anno 0 java.lang.Deprecated()
meth public void paint(java.awt.Graphics)
meth public void paintAll(java.awt.Graphics)
meth public void print(java.awt.Graphics)
meth public void printAll(java.awt.Graphics)
meth public void remove(java.awt.MenuComponent)
meth public void removeComponentListener(java.awt.event.ComponentListener)
meth public void removeFocusListener(java.awt.event.FocusListener)
meth public void removeHierarchyBoundsListener(java.awt.event.HierarchyBoundsListener)
meth public void removeHierarchyListener(java.awt.event.HierarchyListener)
meth public void removeInputMethodListener(java.awt.event.InputMethodListener)
meth public void removeKeyListener(java.awt.event.KeyListener)
meth public void removeMouseListener(java.awt.event.MouseListener)
meth public void removeMouseMotionListener(java.awt.event.MouseMotionListener)
meth public void removeMouseWheelListener(java.awt.event.MouseWheelListener)
meth public void removeNotify()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void repaint()
meth public void repaint(int,int,int,int)
meth public void repaint(long)
meth public void repaint(long,int,int,int,int)
meth public void requestFocus()
meth public void reshape(int,int,int,int)
 anno 0 java.lang.Deprecated()
meth public void resize(int,int)
 anno 0 java.lang.Deprecated()
meth public void resize(java.awt.Dimension)
 anno 0 java.lang.Deprecated()
meth public void revalidate()
meth public void setBackground(java.awt.Color)
meth public void setBounds(int,int,int,int)
meth public void setBounds(java.awt.Rectangle)
meth public void setComponentOrientation(java.awt.ComponentOrientation)
meth public void setCursor(java.awt.Cursor)
meth public void setDropTarget(java.awt.dnd.DropTarget)
meth public void setEnabled(boolean)
meth public void setFocusTraversalKeys(int,java.util.Set<? extends java.awt.AWTKeyStroke>)
meth public void setFocusTraversalKeysEnabled(boolean)
meth public void setFocusable(boolean)
meth public void setFont(java.awt.Font)
meth public void setForeground(java.awt.Color)
meth public void setIgnoreRepaint(boolean)
meth public void setLocale(java.util.Locale)
meth public void setLocation(int,int)
meth public void setLocation(java.awt.Point)
meth public void setMaximumSize(java.awt.Dimension)
meth public void setMinimumSize(java.awt.Dimension)
meth public void setName(java.lang.String)
meth public void setPreferredSize(java.awt.Dimension)
meth public void setSize(int,int)
meth public void setSize(java.awt.Dimension)
meth public void setVisible(boolean)
meth public void show()
 anno 0 java.lang.Deprecated()
meth public void show(boolean)
 anno 0 java.lang.Deprecated()
meth public void transferFocus()
meth public void transferFocusBackward()
meth public void transferFocusUpCycle()
meth public void update(java.awt.Graphics)
meth public void validate()
supr java.lang.Object

CLSS public java.awt.Container
cons public init()
innr protected AccessibleAWTContainer
meth protected java.lang.String paramString()
meth protected void addImpl(java.awt.Component,java.lang.Object,int)
meth protected void processContainerEvent(java.awt.event.ContainerEvent)
meth protected void processEvent(java.awt.AWTEvent)
meth protected void validateTree()
meth public <%0 extends java.util.EventListener> {%%0}[] getListeners(java.lang.Class<{%%0}>)
meth public boolean areFocusTraversalKeysSet(int)
meth public boolean isAncestorOf(java.awt.Component)
meth public boolean isFocusCycleRoot()
meth public boolean isFocusCycleRoot(java.awt.Container)
meth public boolean isFocusTraversalPolicySet()
meth public boolean isValidateRoot()
meth public final boolean isFocusTraversalPolicyProvider()
meth public final void setFocusTraversalPolicyProvider(boolean)
meth public float getAlignmentX()
meth public float getAlignmentY()
meth public int countComponents()
 anno 0 java.lang.Deprecated()
meth public int getComponentCount()
meth public int getComponentZOrder(java.awt.Component)
meth public java.awt.Component add(java.awt.Component)
meth public java.awt.Component add(java.awt.Component,int)
meth public java.awt.Component add(java.lang.String,java.awt.Component)
meth public java.awt.Component findComponentAt(int,int)
meth public java.awt.Component findComponentAt(java.awt.Point)
meth public java.awt.Component getComponent(int)
meth public java.awt.Component getComponentAt(int,int)
meth public java.awt.Component getComponentAt(java.awt.Point)
meth public java.awt.Component locate(int,int)
 anno 0 java.lang.Deprecated()
meth public java.awt.Component[] getComponents()
meth public java.awt.Dimension getMaximumSize()
meth public java.awt.Dimension getMinimumSize()
meth public java.awt.Dimension getPreferredSize()
meth public java.awt.Dimension minimumSize()
 anno 0 java.lang.Deprecated()
meth public java.awt.Dimension preferredSize()
 anno 0 java.lang.Deprecated()
meth public java.awt.FocusTraversalPolicy getFocusTraversalPolicy()
meth public java.awt.Insets getInsets()
meth public java.awt.Insets insets()
 anno 0 java.lang.Deprecated()
meth public java.awt.LayoutManager getLayout()
meth public java.awt.Point getMousePosition(boolean)
meth public java.awt.event.ContainerListener[] getContainerListeners()
meth public java.util.Set<java.awt.AWTKeyStroke> getFocusTraversalKeys(int)
meth public void add(java.awt.Component,java.lang.Object)
meth public void add(java.awt.Component,java.lang.Object,int)
meth public void addContainerListener(java.awt.event.ContainerListener)
meth public void addNotify()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void applyComponentOrientation(java.awt.ComponentOrientation)
meth public void deliverEvent(java.awt.Event)
 anno 0 java.lang.Deprecated()
meth public void doLayout()
meth public void invalidate()
meth public void layout()
 anno 0 java.lang.Deprecated()
meth public void list(java.io.PrintStream,int)
meth public void list(java.io.PrintWriter,int)
meth public void paint(java.awt.Graphics)
meth public void paintComponents(java.awt.Graphics)
meth public void print(java.awt.Graphics)
meth public void printComponents(java.awt.Graphics)
meth public void remove(int)
meth public void remove(java.awt.Component)
meth public void removeAll()
meth public void removeContainerListener(java.awt.event.ContainerListener)
meth public void removeNotify()
meth public void setComponentZOrder(java.awt.Component,int)
meth public void setFocusCycleRoot(boolean)
meth public void setFocusTraversalKeys(int,java.util.Set<? extends java.awt.AWTKeyStroke>)
meth public void setFocusTraversalPolicy(java.awt.FocusTraversalPolicy)
meth public void setFont(java.awt.Font)
meth public void setLayout(java.awt.LayoutManager)
meth public void transferFocusDownCycle()
meth public void update(java.awt.Graphics)
meth public void validate()
supr java.awt.Component

CLSS public abstract interface java.awt.MenuContainer
meth public abstract boolean postEvent(java.awt.Event)
 anno 0 java.lang.Deprecated()
meth public abstract java.awt.Font getFont()
meth public abstract void remove(java.awt.MenuComponent)

CLSS public abstract interface java.awt.image.ImageObserver
fld public final static int ABORT = 128
fld public final static int ALLBITS = 32
fld public final static int ERROR = 64
fld public final static int FRAMEBITS = 16
fld public final static int HEIGHT = 2
fld public final static int PROPERTIES = 4
fld public final static int SOMEBITS = 8
fld public final static int WIDTH = 1
meth public abstract boolean imageUpdate(java.awt.Image,int,int,int,int,int)

CLSS public java.beans.FeatureDescriptor
cons public init()
meth public boolean isExpert()
meth public boolean isHidden()
meth public boolean isPreferred()
meth public java.lang.Object getValue(java.lang.String)
meth public java.lang.String getDisplayName()
meth public java.lang.String getName()
meth public java.lang.String getShortDescription()
meth public java.lang.String toString()
meth public java.util.Enumeration<java.lang.String> attributeNames()
meth public void setDisplayName(java.lang.String)
meth public void setExpert(boolean)
meth public void setHidden(boolean)
meth public void setName(java.lang.String)
meth public void setPreferred(boolean)
meth public void setShortDescription(java.lang.String)
meth public void setValue(java.lang.String,java.lang.Object)
supr java.lang.Object

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.AutoCloseable
meth public abstract void close() throws java.lang.Exception

CLSS public abstract java.lang.ClassLoader
cons protected init()
cons protected init(java.lang.ClassLoader)
meth protected final java.lang.Class<?> defineClass(byte[],int,int)
 anno 0 java.lang.Deprecated()
meth protected final java.lang.Class<?> defineClass(java.lang.String,byte[],int,int)
meth protected final java.lang.Class<?> defineClass(java.lang.String,byte[],int,int,java.security.ProtectionDomain)
meth protected final java.lang.Class<?> defineClass(java.lang.String,java.nio.ByteBuffer,java.security.ProtectionDomain)
meth protected final java.lang.Class<?> findLoadedClass(java.lang.String)
meth protected final java.lang.Class<?> findSystemClass(java.lang.String) throws java.lang.ClassNotFoundException
meth protected final void resolveClass(java.lang.Class<?>)
meth protected final void setSigners(java.lang.Class<?>,java.lang.Object[])
meth protected java.lang.Class<?> findClass(java.lang.String) throws java.lang.ClassNotFoundException
meth protected java.lang.Class<?> loadClass(java.lang.String,boolean) throws java.lang.ClassNotFoundException
meth protected java.lang.Object getClassLoadingLock(java.lang.String)
meth protected java.lang.Package definePackage(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.net.URL)
meth protected java.lang.Package getPackage(java.lang.String)
meth protected java.lang.Package[] getPackages()
meth protected java.lang.String findLibrary(java.lang.String)
meth protected java.net.URL findResource(java.lang.String)
meth protected java.util.Enumeration<java.net.URL> findResources(java.lang.String) throws java.io.IOException
meth protected static boolean registerAsParallelCapable()
meth public final java.lang.ClassLoader getParent()
meth public java.io.InputStream getResourceAsStream(java.lang.String)
meth public java.lang.Class<?> loadClass(java.lang.String) throws java.lang.ClassNotFoundException
meth public java.net.URL getResource(java.lang.String)
meth public java.util.Enumeration<java.net.URL> getResources(java.lang.String) throws java.io.IOException
meth public static java.io.InputStream getSystemResourceAsStream(java.lang.String)
meth public static java.lang.ClassLoader getSystemClassLoader()
meth public static java.net.URL getSystemResource(java.lang.String)
meth public static java.util.Enumeration<java.net.URL> getSystemResources(java.lang.String) throws java.io.IOException
meth public void clearAssertionStatus()
meth public void setClassAssertionStatus(java.lang.String,boolean)
meth public void setDefaultAssertionStatus(boolean)
meth public void setPackageAssertionStatus(java.lang.String,boolean)
supr java.lang.Object

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

CLSS public java.lang.Thread
cons public init()
cons public init(java.lang.Runnable)
cons public init(java.lang.Runnable,java.lang.String)
cons public init(java.lang.String)
cons public init(java.lang.ThreadGroup,java.lang.Runnable)
cons public init(java.lang.ThreadGroup,java.lang.Runnable,java.lang.String)
cons public init(java.lang.ThreadGroup,java.lang.Runnable,java.lang.String,long)
cons public init(java.lang.ThreadGroup,java.lang.String)
fld public final static int MAX_PRIORITY = 10
fld public final static int MIN_PRIORITY = 1
fld public final static int NORM_PRIORITY = 5
innr public abstract interface static UncaughtExceptionHandler
innr public final static !enum State
intf java.lang.Runnable
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public boolean isInterrupted()
meth public final boolean isAlive()
meth public final boolean isDaemon()
meth public final int getPriority()
meth public final java.lang.String getName()
meth public final java.lang.ThreadGroup getThreadGroup()
meth public final void checkAccess()
meth public final void join() throws java.lang.InterruptedException
meth public final void join(long) throws java.lang.InterruptedException
meth public final void join(long,int) throws java.lang.InterruptedException
meth public final void resume()
 anno 0 java.lang.Deprecated()
meth public final void setDaemon(boolean)
meth public final void setName(java.lang.String)
meth public final void setPriority(int)
meth public final void stop()
 anno 0 java.lang.Deprecated()
meth public final void stop(java.lang.Throwable)
 anno 0 java.lang.Deprecated()
meth public final void suspend()
 anno 0 java.lang.Deprecated()
meth public int countStackFrames()
 anno 0 java.lang.Deprecated()
meth public java.lang.ClassLoader getContextClassLoader()
meth public java.lang.StackTraceElement[] getStackTrace()
meth public java.lang.String toString()
meth public java.lang.Thread$State getState()
meth public java.lang.Thread$UncaughtExceptionHandler getUncaughtExceptionHandler()
meth public long getId()
meth public static boolean holdsLock(java.lang.Object)
meth public static boolean interrupted()
meth public static int activeCount()
meth public static int enumerate(java.lang.Thread[])
meth public static java.lang.Thread currentThread()
meth public static java.lang.Thread$UncaughtExceptionHandler getDefaultUncaughtExceptionHandler()
meth public static java.util.Map<java.lang.Thread,java.lang.StackTraceElement[]> getAllStackTraces()
meth public static void dumpStack()
meth public static void setDefaultUncaughtExceptionHandler(java.lang.Thread$UncaughtExceptionHandler)
meth public static void sleep(long) throws java.lang.InterruptedException
meth public static void sleep(long,int) throws java.lang.InterruptedException
meth public static void yield()
meth public void destroy()
 anno 0 java.lang.Deprecated()
meth public void interrupt()
meth public void run()
meth public void setContextClassLoader(java.lang.ClassLoader)
meth public void setUncaughtExceptionHandler(java.lang.Thread$UncaughtExceptionHandler)
meth public void start()
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

CLSS public java.net.URLClassLoader
cons public init(java.net.URL[])
cons public init(java.net.URL[],java.lang.ClassLoader)
cons public init(java.net.URL[],java.lang.ClassLoader,java.net.URLStreamHandlerFactory)
intf java.io.Closeable
meth protected java.lang.Class<?> findClass(java.lang.String) throws java.lang.ClassNotFoundException
meth protected java.lang.Package definePackage(java.lang.String,java.util.jar.Manifest,java.net.URL)
meth protected java.security.PermissionCollection getPermissions(java.security.CodeSource)
meth protected void addURL(java.net.URL)
meth public java.io.InputStream getResourceAsStream(java.lang.String)
meth public java.net.URL findResource(java.lang.String)
meth public java.net.URL[] getURLs()
meth public java.util.Enumeration<java.net.URL> findResources(java.lang.String) throws java.io.IOException
meth public static java.net.URLClassLoader newInstance(java.net.URL[])
meth public static java.net.URLClassLoader newInstance(java.net.URL[],java.lang.ClassLoader)
meth public void close() throws java.io.IOException
supr java.security.SecureClassLoader

CLSS public java.security.SecureClassLoader
cons protected init()
cons protected init(java.lang.ClassLoader)
meth protected final java.lang.Class<?> defineClass(java.lang.String,byte[],int,int,java.security.CodeSource)
meth protected final java.lang.Class<?> defineClass(java.lang.String,java.nio.ByteBuffer,java.security.CodeSource)
meth protected java.security.PermissionCollection getPermissions(java.security.CodeSource)
supr java.lang.ClassLoader

CLSS public abstract interface javax.accessibility.Accessible
meth public abstract javax.accessibility.AccessibleContext getAccessibleContext()

CLSS public abstract javax.swing.JComponent
cons public init()
fld protected javax.swing.event.EventListenerList listenerList
fld protected javax.swing.plaf.ComponentUI ui
fld public final static int UNDEFINED_CONDITION = -1
fld public final static int WHEN_ANCESTOR_OF_FOCUSED_COMPONENT = 1
fld public final static int WHEN_FOCUSED = 0
fld public final static int WHEN_IN_FOCUSED_WINDOW = 2
fld public final static java.lang.String TOOL_TIP_TEXT_KEY = "ToolTipText"
innr public abstract AccessibleJComponent
intf java.io.Serializable
meth protected boolean isPaintingOrigin()
meth protected boolean processKeyBinding(javax.swing.KeyStroke,java.awt.event.KeyEvent,int,boolean)
meth protected boolean requestFocusInWindow(boolean)
meth protected java.awt.Graphics getComponentGraphics(java.awt.Graphics)
meth protected java.lang.String paramString()
meth protected void fireVetoableChange(java.lang.String,java.lang.Object,java.lang.Object) throws java.beans.PropertyVetoException
meth protected void paintBorder(java.awt.Graphics)
meth protected void paintChildren(java.awt.Graphics)
meth protected void paintComponent(java.awt.Graphics)
meth protected void printBorder(java.awt.Graphics)
meth protected void printChildren(java.awt.Graphics)
meth protected void printComponent(java.awt.Graphics)
meth protected void processComponentKeyEvent(java.awt.event.KeyEvent)
meth protected void processKeyEvent(java.awt.event.KeyEvent)
meth protected void processMouseEvent(java.awt.event.MouseEvent)
meth protected void processMouseMotionEvent(java.awt.event.MouseEvent)
meth protected void setUI(javax.swing.plaf.ComponentUI)
meth public <%0 extends java.util.EventListener> {%%0}[] getListeners(java.lang.Class<{%%0}>)
meth public boolean contains(int,int)
meth public boolean getAutoscrolls()
meth public boolean getInheritsPopupMenu()
meth public boolean getVerifyInputWhenFocusTarget()
meth public boolean isDoubleBuffered()
meth public boolean isManagingFocus()
 anno 0 java.lang.Deprecated()
meth public boolean isOpaque()
meth public boolean isOptimizedDrawingEnabled()
meth public boolean isPaintingTile()
meth public boolean isRequestFocusEnabled()
meth public boolean isValidateRoot()
meth public boolean requestDefaultFocus()
 anno 0 java.lang.Deprecated()
meth public boolean requestFocus(boolean)
meth public boolean requestFocusInWindow()
meth public final boolean isPaintingForPrint()
meth public final java.lang.Object getClientProperty(java.lang.Object)
meth public final javax.swing.ActionMap getActionMap()
meth public final javax.swing.InputMap getInputMap()
meth public final javax.swing.InputMap getInputMap(int)
meth public final void putClientProperty(java.lang.Object,java.lang.Object)
meth public final void setActionMap(javax.swing.ActionMap)
meth public final void setInputMap(int,javax.swing.InputMap)
meth public float getAlignmentX()
meth public float getAlignmentY()
meth public int getBaseline(int,int)
meth public int getConditionForKeyStroke(javax.swing.KeyStroke)
meth public int getDebugGraphicsOptions()
meth public int getHeight()
meth public int getWidth()
meth public int getX()
meth public int getY()
meth public java.awt.Component getNextFocusableComponent()
 anno 0 java.lang.Deprecated()
meth public java.awt.Component$BaselineResizeBehavior getBaselineResizeBehavior()
meth public java.awt.Container getTopLevelAncestor()
meth public java.awt.Dimension getMaximumSize()
meth public java.awt.Dimension getMinimumSize()
meth public java.awt.Dimension getPreferredSize()
meth public java.awt.Dimension getSize(java.awt.Dimension)
meth public java.awt.FontMetrics getFontMetrics(java.awt.Font)
meth public java.awt.Graphics getGraphics()
meth public java.awt.Insets getInsets()
meth public java.awt.Insets getInsets(java.awt.Insets)
meth public java.awt.Point getLocation(java.awt.Point)
meth public java.awt.Point getPopupLocation(java.awt.event.MouseEvent)
meth public java.awt.Point getToolTipLocation(java.awt.event.MouseEvent)
meth public java.awt.Rectangle getBounds(java.awt.Rectangle)
meth public java.awt.Rectangle getVisibleRect()
meth public java.awt.event.ActionListener getActionForKeyStroke(javax.swing.KeyStroke)
meth public java.beans.VetoableChangeListener[] getVetoableChangeListeners()
meth public java.lang.String getToolTipText()
meth public java.lang.String getToolTipText(java.awt.event.MouseEvent)
meth public java.lang.String getUIClassID()
meth public javax.swing.InputVerifier getInputVerifier()
meth public javax.swing.JPopupMenu getComponentPopupMenu()
meth public javax.swing.JRootPane getRootPane()
meth public javax.swing.JToolTip createToolTip()
meth public javax.swing.KeyStroke[] getRegisteredKeyStrokes()
meth public javax.swing.TransferHandler getTransferHandler()
meth public javax.swing.border.Border getBorder()
meth public javax.swing.event.AncestorListener[] getAncestorListeners()
meth public static boolean isLightweightComponent(java.awt.Component)
meth public static java.util.Locale getDefaultLocale()
meth public static void setDefaultLocale(java.util.Locale)
meth public void addAncestorListener(javax.swing.event.AncestorListener)
meth public void addNotify()
meth public void addVetoableChangeListener(java.beans.VetoableChangeListener)
meth public void computeVisibleRect(java.awt.Rectangle)
meth public void disable()
 anno 0 java.lang.Deprecated()
meth public void enable()
 anno 0 java.lang.Deprecated()
meth public void firePropertyChange(java.lang.String,boolean,boolean)
meth public void firePropertyChange(java.lang.String,char,char)
meth public void firePropertyChange(java.lang.String,int,int)
meth public void grabFocus()
meth public void hide()
 anno 0 java.lang.Deprecated()
meth public void paint(java.awt.Graphics)
meth public void paintImmediately(int,int,int,int)
meth public void paintImmediately(java.awt.Rectangle)
meth public void print(java.awt.Graphics)
meth public void printAll(java.awt.Graphics)
meth public void registerKeyboardAction(java.awt.event.ActionListener,java.lang.String,javax.swing.KeyStroke,int)
meth public void registerKeyboardAction(java.awt.event.ActionListener,javax.swing.KeyStroke,int)
meth public void removeAncestorListener(javax.swing.event.AncestorListener)
meth public void removeNotify()
meth public void removeVetoableChangeListener(java.beans.VetoableChangeListener)
meth public void repaint(java.awt.Rectangle)
meth public void repaint(long,int,int,int,int)
meth public void requestFocus()
meth public void resetKeyboardActions()
meth public void reshape(int,int,int,int)
 anno 0 java.lang.Deprecated()
meth public void revalidate()
meth public void scrollRectToVisible(java.awt.Rectangle)
meth public void setAlignmentX(float)
meth public void setAlignmentY(float)
meth public void setAutoscrolls(boolean)
meth public void setBackground(java.awt.Color)
meth public void setBorder(javax.swing.border.Border)
meth public void setComponentPopupMenu(javax.swing.JPopupMenu)
meth public void setDebugGraphicsOptions(int)
meth public void setDoubleBuffered(boolean)
meth public void setEnabled(boolean)
meth public void setFocusTraversalKeys(int,java.util.Set<? extends java.awt.AWTKeyStroke>)
meth public void setFont(java.awt.Font)
meth public void setForeground(java.awt.Color)
meth public void setInheritsPopupMenu(boolean)
meth public void setInputVerifier(javax.swing.InputVerifier)
meth public void setMaximumSize(java.awt.Dimension)
meth public void setMinimumSize(java.awt.Dimension)
meth public void setNextFocusableComponent(java.awt.Component)
 anno 0 java.lang.Deprecated()
meth public void setOpaque(boolean)
meth public void setPreferredSize(java.awt.Dimension)
meth public void setRequestFocusEnabled(boolean)
meth public void setToolTipText(java.lang.String)
meth public void setTransferHandler(javax.swing.TransferHandler)
meth public void setVerifyInputWhenFocusTarget(boolean)
meth public void setVisible(boolean)
meth public void unregisterKeyboardAction(javax.swing.KeyStroke)
meth public void update(java.awt.Graphics)
meth public void updateUI()
supr java.awt.Container

CLSS public javax.swing.JPanel
cons public init()
cons public init(boolean)
cons public init(java.awt.LayoutManager)
cons public init(java.awt.LayoutManager,boolean)
innr protected AccessibleJPanel
intf javax.accessibility.Accessible
meth protected java.lang.String paramString()
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.plaf.PanelUI getUI()
meth public void setUI(javax.swing.plaf.PanelUI)
meth public void updateUI()
supr javax.swing.JComponent

CLSS public org.netbeans.modules.db.mysql.Database
cons public init(org.netbeans.modules.db.mysql.DatabaseServer,java.lang.String)
intf org.openide.nodes.Node$Cookie
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getDbName()
meth public java.lang.String getDisplayName()
meth public java.lang.String getShortDescription()
meth public org.netbeans.modules.db.mysql.DatabaseServer getServer()
supr java.lang.Object
hfds name,server

CLSS public abstract interface org.netbeans.modules.db.mysql.DatabaseServer
innr public final static !enum ServerState
intf org.openide.nodes.Node$Cookie
meth public abstract boolean databaseExists(java.lang.String) throws org.netbeans.api.db.explorer.DatabaseException
meth public abstract boolean hasConfigurationError()
meth public abstract boolean isConnected()
meth public abstract boolean isSavePassword()
meth public abstract java.awt.Image getIcon()
meth public abstract java.lang.String getAdminArgs()
meth public abstract java.lang.String getAdminPath()
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getHost()
meth public abstract java.lang.String getPassword()
meth public abstract java.lang.String getPort()
meth public abstract java.lang.String getShortDescription()
meth public abstract java.lang.String getStartArgs()
meth public abstract java.lang.String getStartPath()
meth public abstract java.lang.String getStopArgs()
meth public abstract java.lang.String getStopPath()
meth public abstract java.lang.String getURL()
meth public abstract java.lang.String getURL(java.lang.String)
meth public abstract java.lang.String getUser()
meth public abstract java.util.Collection<org.netbeans.modules.db.mysql.Database> getDatabases() throws org.netbeans.api.db.explorer.DatabaseException
meth public abstract java.util.List<org.netbeans.modules.db.mysql.DatabaseUser> getUsers() throws org.netbeans.api.db.explorer.DatabaseException
meth public abstract org.netbeans.modules.db.mysql.DatabaseServer$ServerState getState()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void checkConfiguration() throws org.netbeans.api.db.explorer.DatabaseException
meth public abstract void createDatabase(java.lang.String)
meth public abstract void disconnect()
meth public abstract void disconnectSync()
meth public abstract void dropDatabase(java.lang.String)
meth public abstract void dropDatabase(java.lang.String,boolean)
meth public abstract void grantFullDatabaseRights(java.lang.String,org.netbeans.modules.db.mysql.DatabaseUser)
meth public abstract void reconnect() throws java.util.concurrent.TimeoutException,org.netbeans.api.db.explorer.DatabaseException
meth public abstract void reconnect(long) throws java.util.concurrent.TimeoutException,org.netbeans.api.db.explorer.DatabaseException
meth public abstract void refreshDatabaseList()
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setAdminArgs(java.lang.String)
meth public abstract void setAdminPath(java.lang.String)
meth public abstract void setHost(java.lang.String)
meth public abstract void setPassword(java.lang.String)
meth public abstract void setPort(java.lang.String)
meth public abstract void setSavePassword(boolean)
meth public abstract void setStartArgs(java.lang.String)
meth public abstract void setStartPath(java.lang.String)
meth public abstract void setStopArgs(java.lang.String)
meth public abstract void setStopPath(java.lang.String)
meth public abstract void setUser(java.lang.String)
meth public abstract void start() throws org.netbeans.api.db.explorer.DatabaseException
meth public abstract void startAdmin() throws org.netbeans.api.db.explorer.DatabaseException
meth public abstract void stop() throws org.netbeans.api.db.explorer.DatabaseException
meth public abstract void validateConnection() throws org.netbeans.api.db.explorer.DatabaseException

CLSS public final static !enum org.netbeans.modules.db.mysql.DatabaseServer$ServerState
 outer org.netbeans.modules.db.mysql.DatabaseServer
fld public final static org.netbeans.modules.db.mysql.DatabaseServer$ServerState CONFIGERR
fld public final static org.netbeans.modules.db.mysql.DatabaseServer$ServerState CONNECTED
fld public final static org.netbeans.modules.db.mysql.DatabaseServer$ServerState DISCONNECTED
meth public static org.netbeans.modules.db.mysql.DatabaseServer$ServerState valueOf(java.lang.String)
meth public static org.netbeans.modules.db.mysql.DatabaseServer$ServerState[] values()
supr java.lang.Enum<org.netbeans.modules.db.mysql.DatabaseServer$ServerState>

CLSS public org.netbeans.modules.db.mysql.DatabaseServerManager
cons public init()
meth public static org.netbeans.modules.db.mysql.DatabaseServer getDatabaseServer()
supr java.lang.Object
hfds SERVER

CLSS public org.netbeans.modules.db.mysql.DatabaseUser
cons public init(java.lang.String,java.lang.String)
meth public java.lang.String getHost()
meth public java.lang.String getUser()
meth public java.lang.String toString()
supr java.lang.Object
hfds host,user

CLSS public abstract interface org.netbeans.modules.db.mysql.spi.sample.SampleProvider
fld public final static java.lang.String SAMPLE_PROVIDER_PATH = "Databases/MySQL/SampleProviders"
meth public abstract boolean supportsSample(java.lang.String)
meth public abstract java.util.List<java.lang.String> getSampleNames()
meth public abstract void create(java.lang.String,org.netbeans.api.db.explorer.DatabaseConnection) throws org.netbeans.api.db.explorer.DatabaseException

CLSS public org.netbeans.modules.db.mysql.ui.AdminPropertiesPanel
cons public init(org.netbeans.modules.db.mysql.DatabaseServer)
meth public java.lang.String getAdminArgs()
meth public java.lang.String getAdminPath()
meth public java.lang.String getStartArgs()
meth public java.lang.String getStartPath()
meth public java.lang.String getStopArgs()
meth public java.lang.String getStopPath()
meth public void setDialogDescriptor(org.openide.DialogDescriptor)
supr javax.swing.JPanel
hfds LOGGER,btnAdminBrowse,btnStartBrowse,btnStopBrowse,descriptor,labelAdmin,labelAdminArgs,labelStart,labelStartArgs,labelStartArgs1,labelStop,messageLabel,nbErrorForeground,options,recentDirectory,txtAdmin,txtAdminArgs,txtStart,txtStartArgs,txtStop,txtStopArgs

CLSS public org.netbeans.modules.db.mysql.ui.BasePropertiesPanel
cons public init(org.netbeans.modules.db.mysql.DatabaseServer)
supr javax.swing.JPanel
hfds chkSavePassword,descriptor,docListener,initMessage,labelHost,labelPassword,labelPort,labelUser,messageLabel,nbErrorForeground,options,txtHost,txtPassword,txtPort,txtUser

CLSS public org.netbeans.modules.db.mysql.ui.CreateDatabasePanel
cons public init(org.netbeans.modules.db.mysql.DatabaseServer) throws org.netbeans.api.db.explorer.DatabaseException
meth public org.netbeans.api.db.explorer.DatabaseConnection showCreateDatabaseDialog() throws org.netbeans.api.db.explorer.DatabaseException
supr javax.swing.JPanel
hfds LOGGER,cancelButton,chkGrantAccess,comboDatabaseName,comboUsers,databaseComboModel,dbconn,descriptor,dialog,messageLabel,nameLabel,okButton,progressBar,progressLabel,server
hcls DatabaseComboModel,UsersComboModel

CLSS public org.netbeans.modules.db.mysql.ui.PropertiesDialog
cons public init(org.netbeans.modules.db.mysql.DatabaseServer)
innr public final static !enum Tab
meth public boolean displayDialog()
meth public boolean displayDialog(org.netbeans.modules.db.mysql.ui.PropertiesDialog$Tab)
meth public void setErrorMessage(java.lang.String)
supr java.lang.Object
hfds HELP_CTX,LOGGER,adminPanel,basePanel,server,tabbedPane

CLSS public final static !enum org.netbeans.modules.db.mysql.ui.PropertiesDialog$Tab
 outer org.netbeans.modules.db.mysql.ui.PropertiesDialog
fld public final static org.netbeans.modules.db.mysql.ui.PropertiesDialog$Tab ADMIN
fld public final static org.netbeans.modules.db.mysql.ui.PropertiesDialog$Tab BASIC
meth public static org.netbeans.modules.db.mysql.ui.PropertiesDialog$Tab valueOf(java.lang.String)
meth public static org.netbeans.modules.db.mysql.ui.PropertiesDialog$Tab[] values()
supr java.lang.Enum<org.netbeans.modules.db.mysql.ui.PropertiesDialog$Tab>

CLSS public org.netbeans.modules.db.mysql.util.DatabaseUtils
cons public init()
fld public final static java.lang.String SQLSTATE_COMM_ERROR = "08S01"
innr public final static !enum ConnectStatus
innr public static URLParser
meth public static boolean ensureConnected(org.netbeans.api.db.explorer.DatabaseConnection)
meth public static boolean isCommunicationsException(java.sql.SQLException)
meth public static boolean isCommunicationsException(org.netbeans.api.db.explorer.DatabaseException)
meth public static boolean isEmpty(java.lang.String)
meth public static boolean isServerException(java.sql.SQLException)
meth public static java.lang.String getURL(java.lang.String,java.lang.String)
meth public static java.lang.String getURL(java.lang.String,java.lang.String,java.lang.String)
meth public static java.sql.Connection connect(java.lang.String,java.lang.String,java.lang.String) throws java.util.concurrent.TimeoutException,org.netbeans.api.db.explorer.DatabaseException
meth public static java.sql.Connection connect(java.lang.String,java.lang.String,java.lang.String,long) throws java.util.concurrent.TimeoutException,org.netbeans.api.db.explorer.DatabaseException
meth public static java.sql.Connection getConnection(org.netbeans.api.db.explorer.DatabaseConnection) throws java.sql.SQLException
meth public static java.sql.Driver getDriver() throws org.netbeans.api.db.explorer.DatabaseException
meth public static java.util.List<org.netbeans.api.db.explorer.DatabaseConnection> findDatabaseConnections(java.lang.String)
meth public static org.netbeans.api.db.explorer.DatabaseConnection findDatabaseConnection(java.lang.String,java.lang.String)
meth public static org.netbeans.api.db.explorer.JDBCDriver getJDBCDriver()
meth public static void closeConnection(java.sql.Connection)
supr java.lang.Object
hfds DFLT_CONFIG_OPTIONS,LOGGER,PROCESSOR,SQLSTATE_CLIENT_PREFIX,driver

CLSS public final static !enum org.netbeans.modules.db.mysql.util.DatabaseUtils$ConnectStatus
 outer org.netbeans.modules.db.mysql.util.DatabaseUtils
fld public final static org.netbeans.modules.db.mysql.util.DatabaseUtils$ConnectStatus CONNECT_SUCCEEDED
fld public final static org.netbeans.modules.db.mysql.util.DatabaseUtils$ConnectStatus NO_SERVER
fld public final static org.netbeans.modules.db.mysql.util.DatabaseUtils$ConnectStatus SERVER_RUNNING
meth public static org.netbeans.modules.db.mysql.util.DatabaseUtils$ConnectStatus valueOf(java.lang.String)
meth public static org.netbeans.modules.db.mysql.util.DatabaseUtils$ConnectStatus[] values()
supr java.lang.Enum<org.netbeans.modules.db.mysql.util.DatabaseUtils$ConnectStatus>

CLSS public static org.netbeans.modules.db.mysql.util.DatabaseUtils$URLParser
 outer org.netbeans.modules.db.mysql.util.DatabaseUtils
cons public init(java.lang.String)
meth public java.lang.String getHost()
meth public java.lang.String getPort()
supr java.lang.Object
hfds MYSQL_PROTOCOL,host,port,url

CLSS public org.netbeans.modules.db.mysql.util.DriverClassLoader
cons public init(org.netbeans.api.db.explorer.JDBCDriver)
meth protected java.security.PermissionCollection getPermissions(java.security.CodeSource)
meth public java.lang.String toString()
supr java.net.URLClassLoader
hfds LOGGER

CLSS public org.netbeans.modules.db.mysql.util.ExecSupport
cons public init()
innr public static OutputCopier
meth public org.openide.windows.InputOutput displayProcessOutputs(java.lang.Process) throws java.io.IOException,java.lang.InterruptedException
supr java.lang.Object
hfds LOGGER,copyMakers

CLSS public static org.netbeans.modules.db.mysql.util.ExecSupport$OutputCopier
 outer org.netbeans.modules.db.mysql.util.ExecSupport
cons public init(java.io.Reader,java.io.Writer,boolean)
meth public void interrupt()
meth public void run()
supr java.lang.Thread
hfds autoflush,done,is,os

CLSS public org.netbeans.modules.db.mysql.util.Utils
cons public init()
meth public !varargs static java.lang.String getMessage(java.lang.String,java.lang.Object[])
meth public static boolean displayConfirmDialog(java.lang.String)
meth public static boolean displayYesNoDialog(java.lang.String)
meth public static boolean isEmpty(java.lang.String)
meth public static boolean isValidDirectory(java.lang.String,boolean)
meth public static boolean isValidExecutable(java.lang.String)
meth public static boolean isValidExecutable(java.lang.String,boolean)
meth public static boolean isValidURL(java.lang.String,boolean)
meth public static boolean stringEquals(java.lang.String,java.lang.String)
meth public static java.lang.RuntimeException launderThrowable(java.lang.Throwable)
meth public static java.lang.String getHostIpAddress(java.lang.String) throws java.net.UnknownHostException
meth public static java.util.ResourceBundle getBundle()
meth public static void displayError(java.lang.String,java.lang.Exception)
meth public static void displayErrorMessage(java.lang.String)
supr java.lang.Object
hfds LOGGER

CLSS public abstract org.openide.nodes.Node
cons protected init(org.openide.nodes.Children)
cons protected init(org.openide.nodes.Children,org.openide.util.Lookup)
fld public final static java.lang.String PROP_COOKIE = "cookie"
fld public final static java.lang.String PROP_DISPLAY_NAME = "displayName"
fld public final static java.lang.String PROP_ICON = "icon"
fld public final static java.lang.String PROP_LEAF = "leaf"
fld public final static java.lang.String PROP_NAME = "name"
fld public final static java.lang.String PROP_OPENED_ICON = "openedIcon"
fld public final static java.lang.String PROP_PARENT_NODE = "parentNode"
fld public final static java.lang.String PROP_PROPERTY_SETS = "propertySets"
fld public final static java.lang.String PROP_SHORT_DESCRIPTION = "shortDescription"
fld public final static org.openide.nodes.Node EMPTY
innr public abstract interface static Cookie
innr public abstract interface static Handle
innr public abstract static IndexedProperty
innr public abstract static Property
innr public abstract static PropertySet
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.Lookup$Provider
meth protected final boolean hasPropertyChangeListener()
meth protected final void fireCookieChange()
meth protected final void fireDisplayNameChange(java.lang.String,java.lang.String)
meth protected final void fireIconChange()
meth protected final void fireNameChange(java.lang.String,java.lang.String)
meth protected final void fireNodeDestroyed()
meth protected final void fireOpenedIconChange()
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected final void firePropertySetsChange(org.openide.nodes.Node$PropertySet[],org.openide.nodes.Node$PropertySet[])
meth protected final void fireShortDescriptionChange(java.lang.String,java.lang.String)
meth protected final void setChildren(org.openide.nodes.Children)
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public abstract boolean canCopy()
meth public abstract boolean canCut()
meth public abstract boolean canDestroy()
meth public abstract boolean canRename()
meth public abstract boolean hasCustomizer()
meth public abstract java.awt.Component getCustomizer()
meth public abstract java.awt.Image getIcon(int)
meth public abstract java.awt.Image getOpenedIcon(int)
meth public abstract java.awt.datatransfer.Transferable clipboardCopy() throws java.io.IOException
meth public abstract java.awt.datatransfer.Transferable clipboardCut() throws java.io.IOException
meth public abstract java.awt.datatransfer.Transferable drag() throws java.io.IOException
meth public abstract org.openide.nodes.Node cloneNode()
meth public abstract org.openide.nodes.Node$Handle getHandle()
meth public abstract org.openide.nodes.Node$PropertySet[] getPropertySets()
meth public abstract org.openide.util.HelpCtx getHelpCtx()
meth public abstract org.openide.util.datatransfer.NewType[] getNewTypes()
meth public abstract org.openide.util.datatransfer.PasteType getDropType(java.awt.datatransfer.Transferable,int,int)
meth public abstract org.openide.util.datatransfer.PasteType[] getPasteTypes(java.awt.datatransfer.Transferable)
meth public boolean equals(java.lang.Object)
meth public final boolean isLeaf()
meth public final javax.swing.JPopupMenu getContextMenu()
meth public final org.openide.nodes.Children getChildren()
meth public final org.openide.nodes.Node getParentNode()
meth public final org.openide.util.Lookup getLookup()
meth public final void addNodeListener(org.openide.nodes.NodeListener)
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removeNodeListener(org.openide.nodes.NodeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public int hashCode()
meth public java.lang.String getHtmlDisplayName()
meth public java.lang.String toString()
meth public javax.swing.Action getPreferredAction()
meth public javax.swing.Action[] getActions(boolean)
meth public org.openide.util.actions.SystemAction getDefaultAction()
 anno 0 java.lang.Deprecated()
meth public org.openide.util.actions.SystemAction[] getActions()
 anno 0 java.lang.Deprecated()
meth public org.openide.util.actions.SystemAction[] getContextActions()
 anno 0 java.lang.Deprecated()
meth public void destroy() throws java.io.IOException
meth public void setDisplayName(java.lang.String)
meth public void setHidden(boolean)
 anno 0 java.lang.Deprecated()
meth public void setName(java.lang.String)
meth public void setShortDescription(java.lang.String)
supr java.beans.FeatureDescriptor
hfds BLOCK_EVENTS,INIT_LOCK,LOCK,TEMPL_COOKIE,err,hierarchy,listeners,lookups,parent,warnedBadProperties
hcls LookupEventList,PropertyEditorRef

CLSS public abstract interface static org.openide.nodes.Node$Cookie
 outer org.openide.nodes.Node

CLSS public final org.openide.util.HelpCtx
cons public init(java.lang.Class<?>)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String)
cons public init(java.net.URL)
 anno 0 java.lang.Deprecated()
fld public final static org.openide.util.HelpCtx DEFAULT_HELP
innr public abstract interface static Displayer
innr public abstract interface static Provider
meth public boolean display()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getHelpID()
meth public java.lang.String toString()
meth public java.net.URL getHelp()
meth public static org.openide.util.HelpCtx findHelp(java.awt.Component)
meth public static org.openide.util.HelpCtx findHelp(java.lang.Object)
meth public static void setHelpIDString(javax.swing.JComponent,java.lang.String)
supr java.lang.Object
hfds err,helpCtx,helpID

CLSS public abstract interface static org.openide.util.HelpCtx$Provider
 outer org.openide.util.HelpCtx
meth public abstract org.openide.util.HelpCtx getHelpCtx()

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

