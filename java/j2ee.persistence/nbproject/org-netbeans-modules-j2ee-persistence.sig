#Signature file v4.1
#Version 1.78.0

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

CLSS public abstract interface java.awt.event.ActionListener
intf java.util.EventListener
meth public abstract void actionPerformed(java.awt.event.ActionEvent)

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

CLSS public abstract interface java.beans.Customizer
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setObject(java.lang.Object)

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

CLSS public abstract interface java.beans.PropertyChangeListener
intf java.util.EventListener
meth public abstract void propertyChange(java.beans.PropertyChangeEvent)

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

CLSS public abstract interface java.io.Externalizable
intf java.io.Serializable
meth public abstract void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public abstract void writeExternal(java.io.ObjectOutput) throws java.io.IOException

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

CLSS public abstract interface java.util.EventListener

CLSS public abstract interface javax.accessibility.Accessible
meth public abstract javax.accessibility.AccessibleContext getAccessibleContext()

CLSS public javax.swing.DefaultListCellRenderer
cons public init()
fld protected static javax.swing.border.Border noFocusBorder
innr public static UIResource
intf java.io.Serializable
intf javax.swing.ListCellRenderer<java.lang.Object>
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public boolean isOpaque()
meth public java.awt.Component getListCellRendererComponent(javax.swing.JList<?>,java.lang.Object,int,boolean,boolean)
meth public void firePropertyChange(java.lang.String,boolean,boolean)
meth public void firePropertyChange(java.lang.String,byte,byte)
meth public void firePropertyChange(java.lang.String,char,char)
meth public void firePropertyChange(java.lang.String,double,double)
meth public void firePropertyChange(java.lang.String,float,float)
meth public void firePropertyChange(java.lang.String,int,int)
meth public void firePropertyChange(java.lang.String,long,long)
meth public void firePropertyChange(java.lang.String,short,short)
meth public void invalidate()
meth public void repaint()
meth public void repaint(java.awt.Rectangle)
meth public void repaint(long,int,int,int,int)
meth public void revalidate()
meth public void validate()
supr javax.swing.JLabel

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

CLSS public javax.swing.JLabel
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,int)
cons public init(java.lang.String,javax.swing.Icon,int)
cons public init(javax.swing.Icon)
cons public init(javax.swing.Icon,int)
fld protected java.awt.Component labelFor
innr protected AccessibleJLabel
intf javax.accessibility.Accessible
intf javax.swing.SwingConstants
meth protected int checkHorizontalKey(int,java.lang.String)
meth protected int checkVerticalKey(int,java.lang.String)
meth protected java.lang.String paramString()
meth public boolean imageUpdate(java.awt.Image,int,int,int,int,int)
meth public int getDisplayedMnemonic()
meth public int getDisplayedMnemonicIndex()
meth public int getHorizontalAlignment()
meth public int getHorizontalTextPosition()
meth public int getIconTextGap()
meth public int getVerticalAlignment()
meth public int getVerticalTextPosition()
meth public java.awt.Component getLabelFor()
meth public java.lang.String getText()
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.Icon getDisabledIcon()
meth public javax.swing.Icon getIcon()
meth public javax.swing.plaf.LabelUI getUI()
meth public void setDisabledIcon(javax.swing.Icon)
meth public void setDisplayedMnemonic(char)
meth public void setDisplayedMnemonic(int)
meth public void setDisplayedMnemonicIndex(int)
meth public void setHorizontalAlignment(int)
meth public void setHorizontalTextPosition(int)
meth public void setIcon(javax.swing.Icon)
meth public void setIconTextGap(int)
meth public void setLabelFor(java.awt.Component)
meth public void setText(java.lang.String)
meth public void setUI(javax.swing.plaf.LabelUI)
meth public void setVerticalAlignment(int)
meth public void setVerticalTextPosition(int)
meth public void updateUI()
supr javax.swing.JComponent

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

CLSS public abstract interface javax.swing.ListCellRenderer<%0 extends java.lang.Object>
meth public abstract java.awt.Component getListCellRendererComponent(javax.swing.JList<? extends {javax.swing.ListCellRenderer%0}>,{javax.swing.ListCellRenderer%0},int,boolean,boolean)

CLSS public abstract interface javax.swing.SwingConstants
fld public final static int BOTTOM = 3
fld public final static int CENTER = 0
fld public final static int EAST = 3
fld public final static int HORIZONTAL = 0
fld public final static int LEADING = 10
fld public final static int LEFT = 2
fld public final static int NEXT = 12
fld public final static int NORTH = 1
fld public final static int NORTH_EAST = 2
fld public final static int NORTH_WEST = 8
fld public final static int PREVIOUS = 13
fld public final static int RIGHT = 4
fld public final static int SOUTH = 5
fld public final static int SOUTH_EAST = 4
fld public final static int SOUTH_WEST = 6
fld public final static int TOP = 1
fld public final static int TRAILING = 11
fld public final static int VERTICAL = 1
fld public final static int WEST = 7

CLSS public abstract interface javax.swing.event.AncestorListener
intf java.util.EventListener
meth public abstract void ancestorAdded(javax.swing.event.AncestorEvent)
meth public abstract void ancestorMoved(javax.swing.event.AncestorEvent)
meth public abstract void ancestorRemoved(javax.swing.event.AncestorEvent)

CLSS public abstract interface javax.swing.event.ChangeListener
intf java.util.EventListener
meth public abstract void stateChanged(javax.swing.event.ChangeEvent)

CLSS public abstract interface javax.swing.event.DocumentListener
intf java.util.EventListener
meth public abstract void changedUpdate(javax.swing.event.DocumentEvent)
meth public abstract void insertUpdate(javax.swing.event.DocumentEvent)
meth public abstract void removeUpdate(javax.swing.event.DocumentEvent)

CLSS public abstract javax.swing.table.AbstractTableModel
cons public init()
fld protected javax.swing.event.EventListenerList listenerList
intf java.io.Serializable
intf javax.swing.table.TableModel
meth public <%0 extends java.util.EventListener> {%%0}[] getListeners(java.lang.Class<{%%0}>)
meth public boolean isCellEditable(int,int)
meth public int findColumn(java.lang.String)
meth public java.lang.Class<?> getColumnClass(int)
meth public java.lang.String getColumnName(int)
meth public javax.swing.event.TableModelListener[] getTableModelListeners()
meth public void addTableModelListener(javax.swing.event.TableModelListener)
meth public void fireTableCellUpdated(int,int)
meth public void fireTableChanged(javax.swing.event.TableModelEvent)
meth public void fireTableDataChanged()
meth public void fireTableRowsDeleted(int,int)
meth public void fireTableRowsInserted(int,int)
meth public void fireTableRowsUpdated(int,int)
meth public void fireTableStructureChanged()
meth public void removeTableModelListener(javax.swing.event.TableModelListener)
meth public void setValueAt(java.lang.Object,int,int)
supr java.lang.Object

CLSS public abstract interface javax.swing.table.TableModel
meth public abstract boolean isCellEditable(int,int)
meth public abstract int getColumnCount()
meth public abstract int getRowCount()
meth public abstract java.lang.Class<?> getColumnClass(int)
meth public abstract java.lang.Object getValueAt(int,int)
meth public abstract java.lang.String getColumnName(int)
meth public abstract void addTableModelListener(javax.swing.event.TableModelListener)
meth public abstract void removeTableModelListener(javax.swing.event.TableModelListener)
meth public abstract void setValueAt(java.lang.Object,int,int)

CLSS public abstract interface org.eclipse.persistence.jpa.jpql.tools.spi.IConstructor
meth public abstract org.eclipse.persistence.jpa.jpql.tools.spi.ITypeDeclaration[] getParameterTypes()

CLSS public abstract interface org.eclipse.persistence.jpa.jpql.tools.spi.IEmbeddable
intf org.eclipse.persistence.jpa.jpql.tools.spi.IManagedType

CLSS public abstract interface org.eclipse.persistence.jpa.jpql.tools.spi.IEntity
intf org.eclipse.persistence.jpa.jpql.tools.spi.IManagedType
meth public abstract java.lang.String getName()
meth public abstract org.eclipse.persistence.jpa.jpql.tools.spi.IQuery getNamedQuery(java.lang.String)

CLSS public abstract interface org.eclipse.persistence.jpa.jpql.tools.spi.IManagedType
intf java.lang.Comparable<org.eclipse.persistence.jpa.jpql.tools.spi.IManagedType>
meth public abstract java.lang.Iterable<org.eclipse.persistence.jpa.jpql.tools.spi.IMapping> mappings()
meth public abstract org.eclipse.persistence.jpa.jpql.tools.spi.IManagedTypeProvider getProvider()
meth public abstract org.eclipse.persistence.jpa.jpql.tools.spi.IMapping getMappingNamed(java.lang.String)
meth public abstract org.eclipse.persistence.jpa.jpql.tools.spi.IType getType()
meth public abstract void accept(org.eclipse.persistence.jpa.jpql.tools.spi.IManagedTypeVisitor)

CLSS public abstract interface org.eclipse.persistence.jpa.jpql.tools.spi.IManagedTypeProvider
meth public abstract java.lang.Iterable<org.eclipse.persistence.jpa.jpql.tools.spi.IEntity> entities()
meth public abstract java.lang.Iterable<org.eclipse.persistence.jpa.jpql.tools.spi.IManagedType> managedTypes()
meth public abstract org.eclipse.persistence.jpa.jpql.tools.spi.IEmbeddable getEmbeddable(java.lang.String)
meth public abstract org.eclipse.persistence.jpa.jpql.tools.spi.IEmbeddable getEmbeddable(org.eclipse.persistence.jpa.jpql.tools.spi.IType)
meth public abstract org.eclipse.persistence.jpa.jpql.tools.spi.IEntity getEntity(java.lang.String)
meth public abstract org.eclipse.persistence.jpa.jpql.tools.spi.IEntity getEntity(org.eclipse.persistence.jpa.jpql.tools.spi.IType)
meth public abstract org.eclipse.persistence.jpa.jpql.tools.spi.IEntity getEntityNamed(java.lang.String)
meth public abstract org.eclipse.persistence.jpa.jpql.tools.spi.IManagedType getManagedType(java.lang.String)
meth public abstract org.eclipse.persistence.jpa.jpql.tools.spi.IManagedType getManagedType(org.eclipse.persistence.jpa.jpql.tools.spi.IType)
meth public abstract org.eclipse.persistence.jpa.jpql.tools.spi.IMappedSuperclass getMappedSuperclass(java.lang.String)
meth public abstract org.eclipse.persistence.jpa.jpql.tools.spi.IMappedSuperclass getMappedSuperclass(org.eclipse.persistence.jpa.jpql.tools.spi.IType)
meth public abstract org.eclipse.persistence.jpa.jpql.tools.spi.ITypeRepository getTypeRepository()

CLSS public abstract interface org.eclipse.persistence.jpa.jpql.tools.spi.IMappedSuperclass
intf org.eclipse.persistence.jpa.jpql.tools.spi.IManagedType

CLSS public abstract interface org.eclipse.persistence.jpa.jpql.tools.spi.IMapping
intf java.lang.Comparable<org.eclipse.persistence.jpa.jpql.tools.spi.IMapping>
meth public abstract boolean hasAnnotation(java.lang.Class<? extends java.lang.annotation.Annotation>)
meth public abstract boolean isCollection()
meth public abstract boolean isEmbeddable()
meth public abstract boolean isProperty()
meth public abstract boolean isRelationship()
meth public abstract boolean isTransient()
meth public abstract int getMappingType()
meth public abstract java.lang.String getName()
meth public abstract org.eclipse.persistence.jpa.jpql.tools.spi.IManagedType getParent()
meth public abstract org.eclipse.persistence.jpa.jpql.tools.spi.IType getType()
meth public abstract org.eclipse.persistence.jpa.jpql.tools.spi.ITypeDeclaration getTypeDeclaration()

CLSS public abstract interface org.eclipse.persistence.jpa.jpql.tools.spi.IQuery
meth public abstract java.lang.String getExpression()
meth public abstract org.eclipse.persistence.jpa.jpql.tools.spi.IManagedTypeProvider getProvider()

CLSS public abstract interface org.eclipse.persistence.jpa.jpql.tools.spi.IType
fld public final static java.lang.String UNRESOLVABLE_TYPE = "UNRESOLVABLE_TYPE"
meth public abstract boolean equals(org.eclipse.persistence.jpa.jpql.tools.spi.IType)
meth public abstract boolean hasAnnotation(java.lang.Class<? extends java.lang.annotation.Annotation>)
meth public abstract boolean isAssignableTo(org.eclipse.persistence.jpa.jpql.tools.spi.IType)
meth public abstract boolean isEnum()
meth public abstract boolean isResolvable()
meth public abstract java.lang.Iterable<org.eclipse.persistence.jpa.jpql.tools.spi.IConstructor> constructors()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String[] getEnumConstants()
meth public abstract org.eclipse.persistence.jpa.jpql.tools.spi.ITypeDeclaration getTypeDeclaration()

CLSS public abstract interface org.eclipse.persistence.jpa.jpql.tools.spi.ITypeDeclaration
meth public abstract boolean isArray()
meth public abstract int getDimensionality()
meth public abstract org.eclipse.persistence.jpa.jpql.tools.spi.IType getType()
meth public abstract org.eclipse.persistence.jpa.jpql.tools.spi.ITypeDeclaration[] getTypeParameters()

CLSS public abstract interface org.eclipse.persistence.jpa.jpql.tools.spi.ITypeRepository
meth public abstract org.eclipse.persistence.jpa.jpql.tools.TypeHelper getTypeHelper()
meth public abstract org.eclipse.persistence.jpa.jpql.tools.spi.IType getEnumType(java.lang.String)
meth public abstract org.eclipse.persistence.jpa.jpql.tools.spi.IType getType(java.lang.Class<?>)
meth public abstract org.eclipse.persistence.jpa.jpql.tools.spi.IType getType(java.lang.String)

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

CLSS public final org.netbeans.modules.j2ee.persistence.action.EntityManagerGenerator
cons public init(org.openide.filesystems.FileObject,java.lang.String)
meth public org.openide.filesystems.FileObject generate(org.netbeans.modules.j2ee.persistence.action.GenerationOptions) throws java.io.IOException
meth public org.openide.filesystems.FileObject generate(org.netbeans.modules.j2ee.persistence.action.GenerationOptions,java.lang.Class<? extends org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategy>) throws java.io.IOException
supr java.lang.Object
hfds fqn,project,targetFo,targetSource

CLSS public final org.netbeans.modules.j2ee.persistence.action.GenerationOptions
cons public init()
innr public final static !enum Operation
meth public java.lang.String getAnnotation()
meth public java.lang.String getCallLines()
meth public java.lang.String getCallLines(java.lang.String,java.lang.String)
meth public java.lang.String getCallLines(java.lang.String,java.lang.String,java.lang.String)
meth public java.lang.String getMethodName()
meth public java.lang.String getParameterName()
meth public java.lang.String getParameterType()
meth public java.lang.String getQueryAttribute()
meth public java.lang.String getReturnType()
meth public java.util.Set<javax.lang.model.element.Modifier> getModifiers()
meth public org.netbeans.modules.j2ee.persistence.action.GenerationOptions$Operation getOperation()
meth public void setAnnotation(java.lang.String)
meth public void setMethodName(java.lang.String)
meth public void setModifiers(java.util.Set<javax.lang.model.element.Modifier>)
meth public void setOperation(org.netbeans.modules.j2ee.persistence.action.GenerationOptions$Operation)
meth public void setParameterName(java.lang.String)
meth public void setParameterType(java.lang.String)
meth public void setQueryAttribute(java.lang.String)
meth public void setReturnType(java.lang.String)
supr java.lang.Object
hfds annotationType,methodName,modifiers,operation,parameterName,parameterType,queryAttribute,returnType

CLSS public final static !enum org.netbeans.modules.j2ee.persistence.action.GenerationOptions$Operation
 outer org.netbeans.modules.j2ee.persistence.action.GenerationOptions
fld public final static org.netbeans.modules.j2ee.persistence.action.GenerationOptions$Operation COUNT
fld public final static org.netbeans.modules.j2ee.persistence.action.GenerationOptions$Operation FIND
fld public final static org.netbeans.modules.j2ee.persistence.action.GenerationOptions$Operation FIND_ALL
fld public final static org.netbeans.modules.j2ee.persistence.action.GenerationOptions$Operation FIND_SUBSET
fld public final static org.netbeans.modules.j2ee.persistence.action.GenerationOptions$Operation GET_EM
fld public final static org.netbeans.modules.j2ee.persistence.action.GenerationOptions$Operation MERGE
fld public final static org.netbeans.modules.j2ee.persistence.action.GenerationOptions$Operation PERSIST
fld public final static org.netbeans.modules.j2ee.persistence.action.GenerationOptions$Operation REMOVE
meth public java.lang.String getBody()
meth public java.lang.String getBody(java.lang.String)
meth public static org.netbeans.modules.j2ee.persistence.action.GenerationOptions$Operation valueOf(java.lang.String)
meth public static org.netbeans.modules.j2ee.persistence.action.GenerationOptions$Operation[] values()
supr java.lang.Enum<org.netbeans.modules.j2ee.persistence.action.GenerationOptions$Operation>
hfds JPA_VERSION_COMPARATOR,body,body2_0,body3_0

CLSS public org.netbeans.modules.j2ee.persistence.action.UseEntityManagerCodeGenerator
cons public init(org.openide.filesystems.FileObject)
innr public static Factory
intf org.netbeans.spi.editor.codegen.CodeGenerator
meth public java.lang.String getDisplayName()
meth public static com.sun.source.util.TreePath getPathElementOfKind(com.sun.source.tree.Tree$Kind,com.sun.source.util.TreePath)
meth public static com.sun.source.util.TreePath getPathElementOfKind(java.util.Set<com.sun.source.tree.Tree$Kind>,com.sun.source.util.TreePath)
meth public void invoke()
supr java.lang.Object
hfds srcFile

CLSS public static org.netbeans.modules.j2ee.persistence.action.UseEntityManagerCodeGenerator$Factory
 outer org.netbeans.modules.j2ee.persistence.action.UseEntityManagerCodeGenerator
cons public init()
intf org.netbeans.spi.editor.codegen.CodeGenerator$Factory
meth public java.util.List<? extends org.netbeans.spi.editor.codegen.CodeGenerator> create(org.openide.util.Lookup)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.j2ee.persistence.api.PersistenceEnvironment
meth public abstract java.lang.ClassLoader getProjectClassLoader(java.net.URL[])
meth public abstract java.util.List<java.net.URL> getProjectClassPath()
meth public abstract java.util.List<java.net.URL> getProjectClassPath(org.openide.filesystems.FileObject)
meth public abstract org.netbeans.api.project.Project getProject()
meth public abstract org.openide.filesystems.FileObject getLocation()

CLSS public final org.netbeans.modules.j2ee.persistence.api.entity.generator.EntitiesFromDBGenerator
cons public init(java.util.List<java.lang.String>,boolean,boolean,boolean,org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation$FetchType,org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation$CollectionType,java.lang.String,org.netbeans.api.project.SourceGroup,org.netbeans.api.db.explorer.DatabaseConnection,org.netbeans.api.project.Project,org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit)
cons public init(java.util.List<java.lang.String>,boolean,boolean,boolean,org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation$FetchType,org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation$CollectionType,java.lang.String,org.netbeans.api.project.SourceGroup,org.netbeans.api.db.explorer.DatabaseConnection,org.netbeans.api.project.Project,org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit,org.netbeans.modules.j2ee.persistence.wizard.fromdb.PersistenceGenerator)
cons public init(java.util.List<java.lang.String>,boolean,java.lang.String,org.netbeans.api.project.SourceGroup,org.netbeans.api.db.explorer.DatabaseConnection,org.netbeans.api.project.Project,org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit)
meth public java.util.Set<org.openide.filesystems.FileObject> generate(org.netbeans.api.progress.aggregate.ProgressContributor) throws java.io.IOException,java.sql.SQLException
supr java.lang.Object
hfds collectionType,connection,fetchType,fullyQualifiedTableNames,generateNamedQueries,generator,location,packageName,persistenceUnit,project,regenTableAttrs,schemaElement,tableNames

CLSS public org.netbeans.modules.j2ee.persistence.dd.JavaPersistenceQLKeywords
meth public static boolean isKeyword(java.lang.String)
supr java.lang.Object
hfds keywords

CLSS public final org.netbeans.modules.j2ee.persistence.dd.ORMMetadata
meth public boolean isScanInProgress()
meth public static org.netbeans.modules.j2ee.persistence.dd.ORMMetadata getDefault()
meth public void waitScanFinished()
supr java.lang.Object
hfds instance

CLSS public final org.netbeans.modules.j2ee.persistence.dd.PersistenceMetadata
meth public org.netbeans.modules.j2ee.persistence.dd.common.Persistence getRoot(org.openide.filesystems.FileObject) throws java.io.IOException
meth public static org.netbeans.modules.j2ee.persistence.dd.PersistenceMetadata getDefault()
meth public void refresh(org.openide.filesystems.FileObject)
supr java.lang.Object
hfds DEFAULT,ddMap

CLSS public org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils
meth public static java.lang.String getJPAVersion(org.netbeans.api.project.Project)
meth public static java.lang.String getJPAVersion(org.netbeans.api.project.libraries.Library)
meth public static org.netbeans.modules.j2ee.persistence.api.PersistenceScope[] getPersistenceScopes(org.netbeans.api.project.Project)
meth public static org.netbeans.modules.j2ee.persistence.api.PersistenceScope[] getPersistenceScopes(org.netbeans.api.project.Project,org.openide.filesystems.FileObject)
meth public static org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity getEntity(java.lang.String,org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappings)
meth public static org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappings getEntityMappings(org.openide.filesystems.FileObject)
meth public static org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit[] getPersistenceUnits(org.openide.filesystems.FileObject) throws java.io.IOException
meth public static void logUsage(java.lang.Class,java.lang.String,java.lang.Object[])
supr java.lang.Object
hfds LOG,USG_LOGGER

CLSS public org.netbeans.modules.j2ee.persistence.dd.common.JPAParseUtils
cons public init()
meth public static java.lang.String getVersion(java.io.InputStream) throws java.io.IOException,org.xml.sax.SAXException
meth public static java.lang.String getVersion(org.xml.sax.InputSource) throws java.io.IOException,org.xml.sax.SAXException
meth public static org.xml.sax.SAXParseException parse(org.openide.filesystems.FileObject) throws java.io.IOException,org.xml.sax.SAXException
meth public static org.xml.sax.SAXParseException parse(org.xml.sax.InputSource) throws java.io.IOException,org.xml.sax.SAXException
meth public static org.xml.sax.SAXParseException parse(org.xml.sax.InputSource,org.xml.sax.EntityResolver) throws java.io.IOException,org.xml.sax.SAXException
supr java.lang.Object
hfds LOGGER
hcls DDResolver,VersionHandler

CLSS public org.netbeans.modules.j2ee.persistence.dd.common.ParseUtils
cons public init()
fld public final static java.lang.String EXCEPTION_PREFIX = "version:"
meth public static java.lang.String getVersion(java.io.InputStream,org.xml.sax.helpers.DefaultHandler,org.xml.sax.EntityResolver) throws java.io.IOException,org.xml.sax.SAXException
meth public static java.lang.String getVersion(org.xml.sax.InputSource,org.xml.sax.helpers.DefaultHandler,org.xml.sax.EntityResolver) throws java.io.IOException,org.xml.sax.SAXException
meth public static org.xml.sax.SAXParseException parseDD(org.xml.sax.InputSource,org.xml.sax.EntityResolver) throws java.io.IOException,org.xml.sax.SAXException
supr java.lang.Object
hfds LOGGER
hcls ErrorHandler

CLSS public abstract interface org.netbeans.modules.j2ee.persistence.dd.common.Persistence
fld public final static java.lang.String PERSISTENCE_UNIT = "PersistenceUnit"
fld public final static java.lang.String VERSION = "Version"
fld public final static java.lang.String VERSION_1_0 = "1.0"
fld public final static java.lang.String VERSION_2_0 = "2.0"
fld public final static java.lang.String VERSION_2_1 = "2.1"
fld public final static java.lang.String VERSION_2_2 = "2.2"
fld public final static java.lang.String VERSION_3_0 = "3.0"
fld public final static java.lang.String VERSION_3_1 = "3.1"
meth public abstract int addPersistenceUnit(org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit)
meth public abstract int removePersistenceUnit(org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit)
meth public abstract int sizePersistenceUnit()
meth public abstract java.lang.String getVersion()
meth public abstract org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit getPersistenceUnit(int)
meth public abstract org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit newPersistenceUnit()
meth public abstract org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit[] getPersistenceUnit()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setPersistenceUnit(int,org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit)
meth public abstract void setPersistenceUnit(org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit[])
meth public abstract void setVersion(java.lang.String)
meth public abstract void validate() throws org.netbeans.modules.schema2beans.ValidateException

CLSS public abstract interface org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit
fld public final static java.lang.String CLASS2 = "Class2"
fld public final static java.lang.String DESCRIPTION = "Description"
fld public final static java.lang.String EXCLUDE_UNLISTED_CLASSES = "ExcludeUnlistedClasses"
fld public final static java.lang.String JAKARTA_NAMESPACE = "jakarta.persistence."
fld public final static java.lang.String JAR_FILE = "JarFile"
fld public final static java.lang.String JAVAX_NAMESPACE = "javax.persistence."
fld public final static java.lang.String JTA_DATA_SOURCE = "JtaDataSource"
fld public final static java.lang.String JTA_TRANSACTIONTYPE = "JTA"
fld public final static java.lang.String MAPPING_FILE = "MappingFile"
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String NON_JTA_DATA_SOURCE = "NonJtaDataSource"
fld public final static java.lang.String PROPERTIES = "Properties"
fld public final static java.lang.String PROVIDER = "Provider"
fld public final static java.lang.String RESOURCE_LOCAL_TRANSACTIONTYPE = "RESOURCE_LOCAL"
fld public final static java.lang.String TRANSACTIONTYPE = "TransactionType"
meth public abstract boolean isExcludeUnlistedClasses()
meth public abstract int addClass2(java.lang.String)
meth public abstract int addJarFile(java.lang.String)
meth public abstract int addMappingFile(java.lang.String)
meth public abstract int removeClass2(java.lang.String)
meth public abstract int removeJarFile(java.lang.String)
meth public abstract int removeMappingFile(java.lang.String)
meth public abstract int sizeClass2()
meth public abstract int sizeJarFile()
meth public abstract int sizeMappingFile()
meth public abstract java.lang.String getClass2(int)
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getJarFile(int)
meth public abstract java.lang.String getJtaDataSource()
meth public abstract java.lang.String getMappingFile(int)
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getNonJtaDataSource()
meth public abstract java.lang.String getProvider()
meth public abstract java.lang.String getTransactionType()
meth public abstract java.lang.String[] getClass2()
meth public abstract java.lang.String[] getJarFile()
meth public abstract java.lang.String[] getMappingFile()
meth public abstract org.netbeans.modules.j2ee.persistence.dd.common.Properties getProperties()
meth public abstract org.netbeans.modules.j2ee.persistence.dd.common.Properties newProperties()
meth public abstract void setClass2(int,java.lang.String)
meth public abstract void setClass2(java.lang.String[])
meth public abstract void setDescription(java.lang.String)
meth public abstract void setExcludeUnlistedClasses(boolean)
meth public abstract void setJarFile(int,java.lang.String)
meth public abstract void setJarFile(java.lang.String[])
meth public abstract void setJtaDataSource(java.lang.String)
meth public abstract void setMappingFile(int,java.lang.String)
meth public abstract void setMappingFile(java.lang.String[])
meth public abstract void setName(java.lang.String)
meth public abstract void setNonJtaDataSource(java.lang.String)
meth public abstract void setProperties(org.netbeans.modules.j2ee.persistence.dd.common.Properties)
meth public abstract void setProvider(java.lang.String)
meth public abstract void setTransactionType(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.persistence.dd.common.Properties
meth public abstract int addProperty2(org.netbeans.modules.j2ee.persistence.dd.common.Property)
meth public abstract int removeProperty2(org.netbeans.modules.j2ee.persistence.dd.common.Property)
meth public abstract int sizeProperty2()
meth public abstract org.netbeans.modules.j2ee.persistence.dd.common.Property getProperty2(int)
meth public abstract org.netbeans.modules.j2ee.persistence.dd.common.Property newProperty()
meth public abstract org.netbeans.modules.j2ee.persistence.dd.common.Property[] getProperty2()
meth public abstract void setProperty2(int,org.netbeans.modules.j2ee.persistence.dd.common.Property)
meth public abstract void setProperty2(org.netbeans.modules.j2ee.persistence.dd.common.Property[])

CLSS public abstract interface org.netbeans.modules.j2ee.persistence.dd.common.Property
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String VALUE = "Value"
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getValue()
meth public abstract void setName(java.lang.String)
meth public abstract void setValue(java.lang.String)

CLSS public org.netbeans.modules.j2ee.persistence.entitygenerator.CMPMappingModel
cons public init()
innr public static ColumnData
innr public static JoinTableColumnMapping
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getTableName()
meth public java.util.Map<java.lang.String,java.lang.String> getCMPFieldMapping()
meth public java.util.Map<java.lang.String,java.lang.String> getJoinTableMapping()
meth public java.util.Map<java.lang.String,org.netbeans.modules.j2ee.persistence.entitygenerator.CMPMappingModel$ColumnData[]> getCmrFieldMapping()
meth public java.util.Map<java.lang.String,org.netbeans.modules.j2ee.persistence.entitygenerator.CMPMappingModel$JoinTableColumnMapping> getJoinTableColumnMppings()
meth public void setCMPFieldMapping(java.util.Map<java.lang.String,java.lang.String>)
meth public void setCmrFieldMapping(java.util.Map<java.lang.String,org.netbeans.modules.j2ee.persistence.entitygenerator.CMPMappingModel$ColumnData[]>)
meth public void setJoiTableColumnMppings(java.util.Map<java.lang.String,org.netbeans.modules.j2ee.persistence.entitygenerator.CMPMappingModel$JoinTableColumnMapping>)
meth public void setJoinTableMapping(java.util.Map<java.lang.String,java.lang.String>)
meth public void setTableName(java.lang.String)
supr java.lang.Object
hfds cmpFieldMapping,cmrFieldMapping,cmrJoinMapping,joinTableColumnMappings,tableName

CLSS public static org.netbeans.modules.j2ee.persistence.entitygenerator.CMPMappingModel$ColumnData
 outer org.netbeans.modules.j2ee.persistence.entitygenerator.CMPMappingModel
cons public init(java.lang.String,boolean)
meth public boolean equals(java.lang.Object)
meth public boolean isNullable()
meth public int hashCode()
meth public java.lang.String getColumnName()
supr java.lang.Object
hfds columnName,nullable

CLSS public static org.netbeans.modules.j2ee.persistence.entitygenerator.CMPMappingModel$JoinTableColumnMapping
 outer org.netbeans.modules.j2ee.persistence.entitygenerator.CMPMappingModel
cons public init()
meth public org.netbeans.modules.j2ee.persistence.entitygenerator.CMPMappingModel$ColumnData[] getColumns()
meth public org.netbeans.modules.j2ee.persistence.entitygenerator.CMPMappingModel$ColumnData[] getInverseColumns()
meth public org.netbeans.modules.j2ee.persistence.entitygenerator.CMPMappingModel$ColumnData[] getReferencedColumns()
meth public org.netbeans.modules.j2ee.persistence.entitygenerator.CMPMappingModel$ColumnData[] getReferencedInverseColumns()
meth public void setColumns(org.netbeans.modules.j2ee.persistence.entitygenerator.CMPMappingModel$ColumnData[])
meth public void setInverseColumns(org.netbeans.modules.j2ee.persistence.entitygenerator.CMPMappingModel$ColumnData[])
meth public void setReferencedColumns(org.netbeans.modules.j2ee.persistence.entitygenerator.CMPMappingModel$ColumnData[])
meth public void setReferencedInverseColumns(org.netbeans.modules.j2ee.persistence.entitygenerator.CMPMappingModel$ColumnData[])
supr java.lang.Object
hfds columns,inverseColumns,referencedColumns,referencedInverseColumns

CLSS public org.netbeans.modules.j2ee.persistence.entitygenerator.DbSchemaEjbGenerator
cons public init(org.netbeans.modules.j2ee.persistence.entitygenerator.GeneratedTables,org.netbeans.modules.dbschema.SchemaElement)
cons public init(org.netbeans.modules.j2ee.persistence.entitygenerator.GeneratedTables,org.netbeans.modules.dbschema.SchemaElement,org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation$CollectionType,boolean,boolean,boolean)
meth public boolean isUseDefaults()
meth public org.netbeans.modules.j2ee.persistence.entitygenerator.EntityClass[] getBeans()
meth public org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation[] getRelations()
meth public static boolean isJoinTable(org.netbeans.modules.dbschema.TableElement,java.util.Set<java.lang.String>)
meth public static java.util.Set<java.lang.String> getTablesReferecedByOtherTables(org.netbeans.modules.dbschema.SchemaElement)
meth public static java.util.Set<java.lang.String> getTablesReferencesOtherTablesWithPrimaryKeyMatch(org.netbeans.modules.dbschema.SchemaElement)
supr java.lang.Object
hfds LOGGER,beans,colectionType,genTables,generateUnresolvedRelationships,primaryKeyIsForeignKeyTables,relations,schemaElement,tablesReferecedByOtherTables,useColumNamesInRelations,useDefaults
hcls ComparableFK

CLSS public org.netbeans.modules.j2ee.persistence.entitygenerator.EntityClass
cons public init(java.lang.String,java.lang.String,java.lang.String,org.openide.filesystems.FileObject,java.lang.String,java.lang.String,org.netbeans.modules.j2ee.persistence.wizard.fromdb.UpdateType,boolean,java.util.Set<java.util.List<java.lang.String>>)
meth public boolean getUseDefaults()
meth public boolean isDerivedIdCandidate()
meth public boolean isForTable()
meth public boolean isUsePkField()
meth public java.lang.String getCatalogName()
meth public java.lang.String getClassName()
meth public java.lang.String getPackage()
meth public java.lang.String getPkFieldName()
meth public java.lang.String getSchemaName()
meth public java.lang.String getTableName()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.j2ee.persistence.entitygenerator.EntityMember> getFields()
meth public java.util.List<org.netbeans.modules.j2ee.persistence.entitygenerator.RelationshipRole> getRoles()
meth public java.util.Set<java.util.List<java.lang.String>> getUniqueConstraints()
meth public org.netbeans.modules.j2ee.persistence.entitygenerator.CMPMappingModel getCMPMapping()
meth public org.netbeans.modules.j2ee.persistence.wizard.fromdb.UpdateType getUpdateType()
meth public org.openide.filesystems.FileObject getPackageFileObject()
meth public org.openide.filesystems.FileObject getRootFolder()
meth public void addRole(org.netbeans.modules.j2ee.persistence.entitygenerator.RelationshipRole)
meth public void setDerivedIdCandidate(boolean)
meth public void setFields(java.util.List<org.netbeans.modules.j2ee.persistence.entitygenerator.EntityMember>)
meth public void setIsForTable(boolean)
meth public void setPkFieldName(java.lang.String)
meth public void usePkField(boolean)
supr java.lang.Object
hfds catalogName,className,derivedIdCandidate,fields,forTable,mappingModel,packageName,pkFieldName,roles,rootFolder,schemaName,tableName,uniqueConstraints,updateType,useDefaults,usePkField

CLSS public abstract org.netbeans.modules.j2ee.persistence.entitygenerator.EntityMember
cons public init()
meth public abstract boolean isAutoIncrement()
meth public abstract boolean isLobType()
meth public abstract boolean isNullable()
meth public abstract boolean isPrimaryKey()
meth public abstract boolean supportsFinder()
meth public abstract java.lang.Integer getLength()
meth public abstract java.lang.Integer getPrecision()
meth public abstract java.lang.Integer getScale()
meth public abstract java.lang.String getColumnName()
meth public abstract java.lang.String getTableName()
meth public abstract void setPrimaryKey(boolean,boolean)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getMemberName()
meth public java.lang.String getMemberType()
meth public static java.lang.String fixRelationshipFieldName(java.lang.String,org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation$CollectionType)
 anno 0 java.lang.Deprecated()
meth public static java.lang.String makeClassName(java.lang.String)
meth public static java.lang.String makeFieldName(java.lang.String)
meth public static java.lang.String makeRelationshipFieldName(java.lang.String,boolean)
meth public static java.lang.String makeRelationshipFieldName(java.lang.String,org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation$CollectionType,boolean)
meth public static org.netbeans.modules.j2ee.persistence.entitygenerator.EntityMember create(org.netbeans.modules.dbschema.ColumnElement)
meth public void setMemberName(java.lang.String)
meth public void setMemberType(java.lang.String)
supr java.lang.Object
hfds memberClass,memberName

CLSS public org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation
cons public init(org.netbeans.modules.j2ee.persistence.entitygenerator.RelationshipRole,org.netbeans.modules.j2ee.persistence.entitygenerator.RelationshipRole)
innr public final static !enum CollectionType
innr public final static !enum FetchType
meth public java.lang.String getRelationName()
meth public java.lang.String toString()
meth public org.netbeans.modules.j2ee.persistence.entitygenerator.RelationshipRole getRoleA()
meth public org.netbeans.modules.j2ee.persistence.entitygenerator.RelationshipRole getRoleB()
meth public org.netbeans.modules.j2ee.persistence.entitygenerator.RelationshipRole[] getRoles()
meth public void makeRoleNamesUnique()
meth public void setRelationName(java.lang.String)
meth public void setRoleA(org.netbeans.modules.j2ee.persistence.entitygenerator.RelationshipRole)
meth public void setRoleB(org.netbeans.modules.j2ee.persistence.entitygenerator.RelationshipRole)
supr java.lang.Object
hfds relationName,roles

CLSS public final static !enum org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation$CollectionType
 outer org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation
fld public final static org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation$CollectionType COLLECTION
fld public final static org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation$CollectionType LIST
fld public final static org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation$CollectionType SET
meth public java.lang.String className()
meth public java.lang.String getShortName()
meth public static org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation$CollectionType valueOf(java.lang.String)
meth public static org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation$CollectionType[] values()
supr java.lang.Enum<org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation$CollectionType>
hfds classStr

CLSS public final static !enum org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation$FetchType
 outer org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation
fld public final static org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation$FetchType DEFAULT
fld public final static org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation$FetchType EAGER
fld public final static org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation$FetchType LAZY
meth public static org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation$FetchType valueOf(java.lang.String)
meth public static org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation$FetchType[] values()
supr java.lang.Enum<org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation$FetchType>

CLSS public abstract interface org.netbeans.modules.j2ee.persistence.entitygenerator.GeneratedTables
meth public abstract java.lang.String getCatalog()
meth public abstract java.lang.String getClassName(java.lang.String)
meth public abstract java.lang.String getPackageName(java.lang.String)
meth public abstract java.lang.String getSchema()
meth public abstract java.util.Set<java.lang.String> getTableNames()
meth public abstract java.util.Set<java.util.List<java.lang.String>> getUniqueConstraints(java.lang.String)
meth public abstract org.netbeans.modules.j2ee.persistence.wizard.fromdb.UpdateType getUpdateType(java.lang.String)
meth public abstract org.openide.filesystems.FileObject getRootFolder(java.lang.String)

CLSS public org.netbeans.modules.j2ee.persistence.entitygenerator.RelationshipRole
cons public init(java.lang.String,java.lang.String,java.lang.String,boolean,boolean,boolean)
cons public init(java.lang.String,java.lang.String,java.lang.String,boolean,boolean,boolean,boolean)
cons public init(org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation)
meth public boolean isCascade()
meth public boolean isMany()
meth public boolean isOptional()
meth public boolean isToMany()
meth public java.lang.String getEntityName()
meth public java.lang.String getEntityPkgName()
meth public java.lang.String getFieldName()
meth public java.lang.String getRoleName()
meth public org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation getParent()
meth public void setCascade(boolean)
meth public void setEntityName(java.lang.String)
meth public void setEntityPkgName(java.lang.String)
meth public void setFieldName(java.lang.String)
meth public void setMany(boolean)
meth public void setParent(org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation)
meth public void setRoleName(java.lang.String)
meth public void setToMany(boolean)
supr java.lang.Object
hfds cascade,entityName,entityPkgName,fieldName,many,optional,parent,roleName,toMany

CLSS public org.netbeans.modules.j2ee.persistence.provider.DefaultProvider
cons protected init()
cons protected init(java.lang.String)
meth public java.lang.String getDisplayName()
meth public java.util.Map getDefaultVendorSpecificProperties()
meth public java.util.Map getUnresolvedVendorSpecificProperties()
supr org.netbeans.modules.j2ee.persistence.provider.Provider

CLSS public org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
meth public java.lang.String getPath()
meth public java.lang.String toString()
supr java.lang.Exception
hfds path

CLSS public abstract org.netbeans.modules.j2ee.persistence.provider.Provider
cons protected init(java.lang.String)
cons protected init(java.lang.String,java.lang.String)
fld public final static java.lang.String TABLE_GENERATION_CREATE = "tableGenerationCreate"
fld public final static java.lang.String TABLE_GENERATION_DROPCREATE = "tableGenerationDropCreate"
fld public final static java.lang.String TABLE_GENERATTION_UNKOWN = "tableGenerationUnknown"
meth protected boolean isJakartaNamespace()
meth protected java.lang.String getVersion()
meth public abstract java.lang.String getDisplayName()
meth public abstract java.util.Map getDefaultVendorSpecificProperties()
meth public abstract java.util.Map getUnresolvedVendorSpecificProperties()
meth public boolean equals(java.lang.Object)
meth public boolean isOnClassPath(org.netbeans.api.java.classpath.ClassPath)
meth public final boolean supportsTableGeneration()
meth public final java.lang.String getDefaultJtaDatasource()
meth public final java.lang.String getProviderClass()
meth public final java.util.Map<java.lang.String,java.lang.String> getConnectionPropertiesMap(org.netbeans.api.db.explorer.DatabaseConnection,java.lang.String)
meth public final org.netbeans.modules.j2ee.persistence.dd.common.Property getTableGenerationProperty(java.lang.String,java.lang.String)
meth public int hashCode()
meth public java.lang.String getAnnotationProcessor()
meth public java.lang.String getAnnotationSubPackageProperty()
meth public java.lang.String getJdbcDriver()
meth public java.lang.String getJdbcPassword()
meth public java.lang.String getJdbcUrl()
meth public java.lang.String getJdbcUsername()
meth public java.lang.String getTableGenerationCreateValue()
meth public java.lang.String getTableGenerationDropCreateValue()
meth public java.lang.String getTableGenerationPropertyName()
meth public java.lang.String toString()
meth public java.util.Set getPropertyNames()
supr java.lang.Object
hfds providerClass,vendorSpecificProperties,version

CLSS public org.netbeans.modules.j2ee.persistence.provider.ProviderUtil
fld public final static org.netbeans.modules.j2ee.persistence.provider.Provider DATANUCLEUS_PROVIDER1_0
fld public final static org.netbeans.modules.j2ee.persistence.provider.Provider DATANUCLEUS_PROVIDER2_0
fld public final static org.netbeans.modules.j2ee.persistence.provider.Provider DATANUCLEUS_PROVIDER2_1
fld public final static org.netbeans.modules.j2ee.persistence.provider.Provider DATANUCLEUS_PROVIDER2_2
fld public final static org.netbeans.modules.j2ee.persistence.provider.Provider DATANUCLEUS_PROVIDER3_0
fld public final static org.netbeans.modules.j2ee.persistence.provider.Provider DATANUCLEUS_PROVIDER3_1
fld public final static org.netbeans.modules.j2ee.persistence.provider.Provider DEFAULT_PROVIDER
fld public final static org.netbeans.modules.j2ee.persistence.provider.Provider DEFAULT_PROVIDER2_0
fld public final static org.netbeans.modules.j2ee.persistence.provider.Provider DEFAULT_PROVIDER2_1
fld public final static org.netbeans.modules.j2ee.persistence.provider.Provider DEFAULT_PROVIDER2_2
fld public final static org.netbeans.modules.j2ee.persistence.provider.Provider DEFAULT_PROVIDER3_0
fld public final static org.netbeans.modules.j2ee.persistence.provider.Provider DEFAULT_PROVIDER3_1
fld public final static org.netbeans.modules.j2ee.persistence.provider.Provider ECLIPSELINK_PROVIDER1_0
fld public final static org.netbeans.modules.j2ee.persistence.provider.Provider ECLIPSELINK_PROVIDER2_0
fld public final static org.netbeans.modules.j2ee.persistence.provider.Provider ECLIPSELINK_PROVIDER2_1
fld public final static org.netbeans.modules.j2ee.persistence.provider.Provider ECLIPSELINK_PROVIDER2_2
fld public final static org.netbeans.modules.j2ee.persistence.provider.Provider ECLIPSELINK_PROVIDER3_0
fld public final static org.netbeans.modules.j2ee.persistence.provider.Provider ECLIPSELINK_PROVIDER3_1
fld public final static org.netbeans.modules.j2ee.persistence.provider.Provider HIBERNATE_PROVIDER1_0
fld public final static org.netbeans.modules.j2ee.persistence.provider.Provider HIBERNATE_PROVIDER2_0
fld public final static org.netbeans.modules.j2ee.persistence.provider.Provider HIBERNATE_PROVIDER2_1
fld public final static org.netbeans.modules.j2ee.persistence.provider.Provider HIBERNATE_PROVIDER2_2
fld public final static org.netbeans.modules.j2ee.persistence.provider.Provider HIBERNATE_PROVIDER3_0
fld public final static org.netbeans.modules.j2ee.persistence.provider.Provider HIBERNATE_PROVIDER3_1
fld public final static org.netbeans.modules.j2ee.persistence.provider.Provider KODO_PROVIDER
fld public final static org.netbeans.modules.j2ee.persistence.provider.Provider OPENJPA_PROVIDER1_0
fld public final static org.netbeans.modules.j2ee.persistence.provider.Provider OPENJPA_PROVIDER2_0
fld public final static org.netbeans.modules.j2ee.persistence.provider.Provider OPENJPA_PROVIDER2_1
fld public final static org.netbeans.modules.j2ee.persistence.provider.Provider OPENJPA_PROVIDER2_2
fld public final static org.netbeans.modules.j2ee.persistence.provider.Provider TOPLINK_PROVIDER1_0
meth public static boolean canServerBeSelected(org.netbeans.api.project.Project)
meth public static boolean isValid(org.netbeans.modules.j2ee.persistence.unit.PUDataObject)
meth public static boolean isValidServerInstanceOrNone(org.netbeans.api.project.Project)
meth public static boolean makePortableIfPossible(org.netbeans.api.project.Project,org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit)
meth public static boolean normalizeIfPossible(org.netbeans.api.project.Project,org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit)
meth public static boolean persistenceExists(org.netbeans.api.project.Project) throws org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException
meth public static boolean persistenceExists(org.netbeans.api.project.Project,org.openide.filesystems.FileObject) throws org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException
meth public static java.lang.String getDatasourceName(org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit)
meth public static java.lang.String getVersion(org.netbeans.modules.j2ee.persistence.provider.Provider)
meth public static java.util.ArrayList<org.netbeans.modules.j2ee.persistence.provider.Provider> getProviders(org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit)
meth public static java.util.HashMap<java.lang.String,java.lang.String> getConnectionProperties(org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit)
meth public static org.netbeans.api.db.explorer.DatabaseConnection getConnection(org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit)
meth public static org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit buildPersistenceUnit(java.lang.String,org.netbeans.modules.j2ee.persistence.provider.Provider,org.netbeans.api.db.explorer.DatabaseConnection,java.lang.String)
meth public static org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit[] getPersistenceUnits(org.netbeans.modules.j2ee.persistence.unit.PUDataObject)
meth public static org.netbeans.modules.j2ee.persistence.dd.common.Property getProperty(org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit,java.lang.String)
meth public static org.netbeans.modules.j2ee.persistence.provider.Provider getProvider(java.lang.String,org.netbeans.api.project.Project)
meth public static org.netbeans.modules.j2ee.persistence.provider.Provider getProvider(org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit)
meth public static org.netbeans.modules.j2ee.persistence.provider.Provider getProvider(org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit,org.netbeans.modules.j2ee.persistence.provider.Provider[])
meth public static org.netbeans.modules.j2ee.persistence.provider.Provider[] getAllProviders()
meth public static org.netbeans.modules.j2ee.persistence.unit.PUDataObject getPUDataObject(org.netbeans.api.project.Project) throws org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException
meth public static org.netbeans.modules.j2ee.persistence.unit.PUDataObject getPUDataObject(org.netbeans.api.project.Project,java.lang.String) throws org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException
meth public static org.netbeans.modules.j2ee.persistence.unit.PUDataObject getPUDataObject(org.netbeans.api.project.Project,org.openide.filesystems.FileObject,java.lang.String) throws org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException
meth public static org.netbeans.modules.j2ee.persistence.unit.PUDataObject getPUDataObject(org.openide.filesystems.FileObject) throws org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException
meth public static org.openide.filesystems.FileObject getDDFile(org.netbeans.api.project.Project)
meth public static org.openide.filesystems.FileObject getDDFile(org.netbeans.api.project.Project,org.openide.filesystems.FileObject)
meth public static void addManagedClass(org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit,java.lang.String,org.netbeans.modules.j2ee.persistence.unit.PUDataObject)
meth public static void addPersistenceUnit(org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit,org.netbeans.api.project.Project) throws org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException
meth public static void addPersistenceUnit(org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit,org.netbeans.api.project.Project,org.openide.filesystems.FileObject) throws org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException
meth public static void migrateProperties(java.lang.String,java.lang.String,org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit)
meth public static void removeManagedClass(org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit,java.lang.String,org.netbeans.modules.j2ee.persistence.unit.PUDataObject)
meth public static void removeProviderProperties(org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit)
meth public static void renameManagedClass(org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit,java.lang.String,java.lang.String,org.netbeans.modules.j2ee.persistence.unit.PUDataObject)
meth public static void setDatabaseConnection(org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit,org.netbeans.api.db.explorer.DatabaseConnection)
meth public static void setDatabaseConnection(org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit,org.netbeans.modules.j2ee.persistence.provider.Provider,org.netbeans.api.db.explorer.DatabaseConnection)
meth public static void setProvider(org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit,org.netbeans.modules.j2ee.persistence.provider.Provider,org.netbeans.api.db.explorer.DatabaseConnection,java.lang.String)
meth public static void setTableGeneration(org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit,java.lang.String,org.netbeans.api.project.Project)
meth public static void setTableGeneration(org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit,java.lang.String,org.netbeans.modules.j2ee.persistence.provider.Provider)
supr java.lang.Object
hfds TOPLINK_PROVIDER_55_COMPATIBLE

CLSS public abstract interface org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSource
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getDriverClassName()
meth public abstract java.lang.String getJndiName()
meth public abstract java.lang.String getPassword()
meth public abstract java.lang.String getUrl()
meth public abstract java.lang.String getUsername()

CLSS public abstract interface org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSourcePopulator
meth public abstract void connect(javax.swing.JComboBox)

CLSS public abstract interface org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSourceProvider
meth public abstract java.util.List<org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSource> getDataSources()
meth public abstract org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSource toJPADataSource(java.lang.Object)

CLSS public final org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.ApplicationManagedResourceTransactionInJ2SE
cons public init()
meth protected java.lang.String getInvocationCode(org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategySupport$FieldInfo)
meth public com.sun.source.tree.ClassTree generate()
supr org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategySupport

CLSS public final org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.ApplicationManagedResourceTransactionInjectableInEJB
cons public init()
meth public com.sun.source.tree.ClassTree generate()
supr org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategySupport

CLSS public final org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.ApplicationManagedResourceTransactionInjectableInWeb
cons public init()
meth public com.sun.source.tree.ClassTree generate()
supr org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategySupport

CLSS public final org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.ApplicationManagedResourceTransactionNonInjectableInEJB
cons public init()
meth public com.sun.source.tree.ClassTree generate()
supr org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategySupport

CLSS public final org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.ApplicationManagedResourceTransactionNonInjectableInWeb
cons public init()
meth public com.sun.source.tree.ClassTree generate()
supr org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategySupport

CLSS public final org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.ContainerManagedJTAInjectableInEJB
cons public init()
meth public com.sun.source.tree.ClassTree generate()
supr org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategySupport

CLSS public final org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.ContainerManagedJTAInjectableInWeb
cons public init()
meth public com.sun.source.tree.ClassTree generate()
supr org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategySupport

CLSS public final org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.ContainerManagedJTANonInjectableInWeb
cons public init()
meth public com.sun.source.tree.ClassTree generate()
supr org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategySupport

CLSS public abstract interface org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategy
meth public abstract com.sun.source.tree.ClassTree generate()
meth public abstract void setClassTree(com.sun.source.tree.ClassTree)
meth public abstract void setGenUtils(org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils)
meth public abstract void setGenerationOptions(org.netbeans.modules.j2ee.persistence.action.GenerationOptions)
meth public abstract void setPersistenceUnit(org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit)
meth public abstract void setTreeMaker(org.netbeans.api.java.source.TreeMaker)
meth public abstract void setWorkingCopy(org.netbeans.api.java.source.WorkingCopy)

CLSS public abstract interface org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategyResolver
meth public abstract java.lang.Class<? extends org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategy> resolveStrategy(org.openide.filesystems.FileObject)

CLSS public final org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategyResolverFactory
meth public static org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategyResolver createInstance(org.netbeans.api.project.Project)
supr java.lang.Object
hcls EMGenStrategyResolverImpl

CLSS public abstract org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategySupport
cons public init()
fld protected final static java.lang.String ENTITY_MANAGER_DEFAULT_NAME = "em"
fld protected final static java.lang.String ENTITY_MANAGER_FACTORY_DEFAULT_NAME = "emf"
fld protected final static java.lang.String ENTITY_MANAGER_FACTORY_FQN = "javax.persistence.EntityManagerFactory"
fld protected final static java.lang.String ENTITY_MANAGER_FQN = "javax.persistence.EntityManager"
fld protected final static java.lang.String PERSISTENCE_CONTEXT_FQN = "javax.persistence.PersistenceContext"
fld protected final static java.lang.String PERSISTENCE_UNIT_FQN = "javax.persistence.PersistenceUnit"
fld protected final static java.lang.String POST_CONSTRUCT_FQN = "javax.annotation.PostConstruct"
fld protected final static java.lang.String PRE_DESTROY_FQN = "javax.annotation.PreDestroy"
fld protected final static java.lang.String RESOURCE_FQN = "javax.annotation.Resource"
fld protected final static java.lang.String USER_TX_FQN = "javax.transaction.UserTransaction"
innr protected final static !enum Initialization
innr protected static FieldInfo
intf org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategy
meth protected com.sun.source.tree.ClassTree createEntityManager(org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategySupport$Initialization)
meth protected com.sun.source.tree.ClassTree getClassTree()
meth protected com.sun.source.tree.ExpressionTree getTypeTree(java.lang.String)
meth protected com.sun.source.tree.Tree getReturnTypeTree()
meth protected com.sun.source.tree.Tree importFQNs(com.sun.source.tree.Tree)
meth protected com.sun.source.tree.VariableTree createEntityManagerFactory(java.lang.String)
meth protected com.sun.source.tree.VariableTree createUserTransaction()
meth protected com.sun.source.tree.VariableTree getField(java.lang.String)
meth protected int getIndexForField(com.sun.source.tree.ClassTree)
meth protected java.lang.String computeMethodName()
meth protected java.lang.String generateCallLines()
meth protected java.lang.String generateCallLines(java.lang.String)
meth protected java.lang.String getEmInitCode(org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategySupport$FieldInfo,org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategySupport$FieldInfo)
meth protected java.lang.String getPersistenceUnitName()
meth protected java.util.List<com.sun.source.tree.VariableTree> getParameterList()
meth protected javax.lang.model.element.Element getAnnotation(java.lang.String)
meth protected org.netbeans.api.java.source.TreeMaker getTreeMaker()
meth protected org.netbeans.api.java.source.WorkingCopy getWorkingCopy()
meth protected org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils getGenUtils()
meth protected org.netbeans.modules.j2ee.persistence.action.GenerationOptions getGenerationOptions()
meth protected org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit getPersistenceUnit()
meth public void setClassTree(com.sun.source.tree.ClassTree)
meth public void setGenUtils(org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils)
meth public void setGenerationOptions(org.netbeans.modules.j2ee.persistence.action.GenerationOptions)
meth public void setPersistenceUnit(org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit)
meth public void setTreeMaker(org.netbeans.api.java.source.TreeMaker)
meth public void setWorkingCopy(org.netbeans.api.java.source.WorkingCopy)
supr java.lang.Object
hfds classTree,genUtils,generationOptions,persistenceUnit,treeMaker,workingCopy

CLSS protected static org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategySupport$FieldInfo
 outer org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategySupport
meth public boolean isExisting()
meth public java.lang.String getName()
supr java.lang.Object
hfds existing,name

CLSS protected final static !enum org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategySupport$Initialization
 outer org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategySupport
fld public final static org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategySupport$Initialization EMF
fld public final static org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategySupport$Initialization INIT
fld public final static org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategySupport$Initialization INJECT
meth public static org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategySupport$Initialization valueOf(java.lang.String)
meth public static org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategySupport$Initialization[] values()
supr java.lang.Enum<org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategySupport$Initialization>

CLSS public org.netbeans.modules.j2ee.persistence.spi.jpql.Constructor
cons public init(org.eclipse.persistence.jpa.jpql.tools.spi.IType,java.lang.reflect.Constructor<?>)
cons public init(org.eclipse.persistence.jpa.jpql.tools.spi.IType,javax.lang.model.element.ExecutableElement)
intf org.eclipse.persistence.jpa.jpql.tools.spi.IConstructor
meth public org.eclipse.persistence.jpa.jpql.tools.spi.ITypeDeclaration[] getParameterTypes()
supr java.lang.Object
hfds jConstructor,nbConstructor,owner,parameterTypes

CLSS public org.netbeans.modules.j2ee.persistence.spi.jpql.Embeddable
cons public init(org.netbeans.modules.j2ee.persistence.api.metadata.orm.Embeddable,org.eclipse.persistence.jpa.jpql.tools.spi.IManagedTypeProvider)
intf org.eclipse.persistence.jpa.jpql.tools.spi.IEmbeddable
meth public java.lang.String toString()
meth public void accept(org.eclipse.persistence.jpa.jpql.tools.spi.IManagedTypeVisitor)
supr org.netbeans.modules.j2ee.persistence.spi.jpql.ManagedType

CLSS public org.netbeans.modules.j2ee.persistence.spi.jpql.Entity
cons public init(org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity,org.eclipse.persistence.jpa.jpql.tools.spi.IManagedTypeProvider)
intf org.eclipse.persistence.jpa.jpql.tools.spi.IEntity
meth public java.lang.String getName()
meth public org.eclipse.persistence.jpa.jpql.tools.spi.IQuery getNamedQuery(java.lang.String)
meth public void accept(org.eclipse.persistence.jpa.jpql.tools.spi.IManagedTypeVisitor)
supr org.netbeans.modules.j2ee.persistence.spi.jpql.ManagedType

CLSS public abstract org.netbeans.modules.j2ee.persistence.spi.jpql.ManagedType
cons public init(org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject,org.eclipse.persistence.jpa.jpql.tools.spi.IManagedTypeProvider)
intf org.eclipse.persistence.jpa.jpql.tools.spi.IManagedType
meth public int compareTo(org.eclipse.persistence.jpa.jpql.tools.spi.IManagedType)
meth public java.lang.Iterable<org.eclipse.persistence.jpa.jpql.tools.spi.IMapping> mappings()
meth public org.eclipse.persistence.jpa.jpql.tools.spi.IManagedTypeProvider getProvider()
meth public org.eclipse.persistence.jpa.jpql.tools.spi.IMapping getMappingNamed(java.lang.String)
meth public org.eclipse.persistence.jpa.jpql.tools.spi.IType getType()
supr java.lang.Object
hfds element,mappings,provider,type

CLSS public org.netbeans.modules.j2ee.persistence.spi.jpql.ManagedTypeProvider
cons public init(org.netbeans.api.project.Project,org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappings,javax.lang.model.util.Elements)
cons public init(org.netbeans.api.project.Project,org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata,javax.lang.model.util.Elements)
intf org.eclipse.persistence.jpa.jpql.tools.spi.IManagedTypeProvider
meth public boolean isValid()
meth public java.lang.Iterable<org.eclipse.persistence.jpa.jpql.tools.spi.IEntity> entities()
meth public java.lang.Iterable<org.eclipse.persistence.jpa.jpql.tools.spi.IManagedType> managedTypes()
meth public org.eclipse.persistence.jpa.jpql.tools.spi.IEmbeddable getEmbeddable(java.lang.String)
meth public org.eclipse.persistence.jpa.jpql.tools.spi.IEmbeddable getEmbeddable(org.eclipse.persistence.jpa.jpql.tools.spi.IType)
meth public org.eclipse.persistence.jpa.jpql.tools.spi.IEntity getEntity(java.lang.String)
meth public org.eclipse.persistence.jpa.jpql.tools.spi.IEntity getEntity(org.eclipse.persistence.jpa.jpql.tools.spi.IType)
meth public org.eclipse.persistence.jpa.jpql.tools.spi.IEntity getEntityNamed(java.lang.String)
meth public org.eclipse.persistence.jpa.jpql.tools.spi.IManagedType getManagedType(java.lang.String)
meth public org.eclipse.persistence.jpa.jpql.tools.spi.IManagedType getManagedType(org.eclipse.persistence.jpa.jpql.tools.spi.IType)
meth public org.eclipse.persistence.jpa.jpql.tools.spi.IMappedSuperclass getMappedSuperclass(java.lang.String)
meth public org.eclipse.persistence.jpa.jpql.tools.spi.IMappedSuperclass getMappedSuperclass(org.eclipse.persistence.jpa.jpql.tools.spi.IType)
meth public org.eclipse.persistence.jpa.jpql.tools.spi.ITypeRepository getTypeRepository()
meth public void invalidate()
supr java.lang.Object
hfds elements,embeddables,entities,mSuperclasses,managedTypes,mappings,project,typeRepository,valid

CLSS public org.netbeans.modules.j2ee.persistence.spi.jpql.MappedSuperclass
cons public init(org.netbeans.modules.j2ee.persistence.api.metadata.orm.MappedSuperclass,org.eclipse.persistence.jpa.jpql.tools.spi.IManagedTypeProvider)
intf org.eclipse.persistence.jpa.jpql.tools.spi.IMappedSuperclass
meth public void accept(org.eclipse.persistence.jpa.jpql.tools.spi.IManagedTypeVisitor)
supr org.netbeans.modules.j2ee.persistence.spi.jpql.ManagedType

CLSS public org.netbeans.modules.j2ee.persistence.spi.jpql.Mapping
cons public init(org.netbeans.modules.j2ee.persistence.spi.jpql.ManagedType,org.netbeans.modules.j2ee.persistence.spi.jpql.support.JPAAttribute)
intf org.eclipse.persistence.jpa.jpql.tools.spi.IMapping
meth public boolean hasAnnotation(java.lang.Class<? extends java.lang.annotation.Annotation>)
meth public boolean isCollection()
meth public boolean isEmbeddable()
meth public boolean isProperty()
meth public boolean isRelationship()
meth public boolean isTransient()
meth public int compareTo(org.eclipse.persistence.jpa.jpql.tools.spi.IMapping)
meth public int getMappingType()
meth public java.lang.String getName()
meth public org.eclipse.persistence.jpa.jpql.tools.spi.IManagedType getParent()
meth public org.eclipse.persistence.jpa.jpql.tools.spi.IType getType()
meth public org.eclipse.persistence.jpa.jpql.tools.spi.ITypeDeclaration getTypeDeclaration()
supr java.lang.Object
hfds attribute,mappingType,parent,type

CLSS public org.netbeans.modules.j2ee.persistence.spi.jpql.Query
cons public init(org.netbeans.modules.j2ee.persistence.api.metadata.orm.NamedQuery,java.lang.String,org.eclipse.persistence.jpa.jpql.tools.spi.IManagedTypeProvider)
cons public init(org.netbeans.modules.j2ee.persistence.api.metadata.orm.NamedQuery,org.eclipse.persistence.jpa.jpql.tools.spi.IManagedTypeProvider)
intf org.eclipse.persistence.jpa.jpql.tools.spi.IQuery
meth public java.lang.String getExpression()
meth public java.lang.String toString()
meth public org.eclipse.persistence.jpa.jpql.tools.spi.IManagedTypeProvider getProvider()
meth public org.netbeans.modules.j2ee.persistence.api.metadata.orm.NamedQuery getNamedQuery()
supr java.lang.Object
hfds provider,query,queryStr

CLSS public org.netbeans.modules.j2ee.persistence.spi.jpql.Type
cons public init(org.eclipse.persistence.jpa.jpql.tools.spi.ITypeRepository,java.lang.Class<?>)
cons public init(org.eclipse.persistence.jpa.jpql.tools.spi.ITypeRepository,javax.lang.model.element.Element)
cons public init(org.eclipse.persistence.jpa.jpql.tools.spi.ITypeRepository,org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject)
intf org.eclipse.persistence.jpa.jpql.tools.spi.IType
meth public boolean equals(java.lang.Object)
meth public boolean equals(org.eclipse.persistence.jpa.jpql.tools.spi.IType)
meth public boolean hasAnnotation(java.lang.Class<? extends java.lang.annotation.Annotation>)
meth public boolean isAssignableTo(org.eclipse.persistence.jpa.jpql.tools.spi.IType)
meth public boolean isEnum()
meth public boolean isResolvable()
meth public int hashCode()
meth public java.lang.Iterable<org.eclipse.persistence.jpa.jpql.tools.spi.IConstructor> constructors()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.lang.String[] getEnumConstants()
meth public org.eclipse.persistence.jpa.jpql.tools.spi.ITypeDeclaration getTypeDeclaration()
supr java.lang.Object
hfds constructors,element,enumConstants,po,repository,tDeclaration,type,typeName

CLSS public org.netbeans.modules.j2ee.persistence.spi.jpql.TypeDeclaration
intf org.eclipse.persistence.jpa.jpql.tools.spi.ITypeDeclaration
meth public boolean isArray()
meth public int getDimensionality()
meth public org.eclipse.persistence.jpa.jpql.tools.spi.IType getType()
meth public org.eclipse.persistence.jpa.jpql.tools.spi.ITypeDeclaration[] getTypeParameters()
supr java.lang.Object
hfds array,dimensionality,genericType,genericTypes,type,typeRepository

CLSS public org.netbeans.modules.j2ee.persistence.spi.jpql.TypeRepository
intf org.eclipse.persistence.jpa.jpql.tools.spi.ITypeRepository
meth public org.eclipse.persistence.jpa.jpql.tools.TypeHelper getTypeHelper()
meth public org.eclipse.persistence.jpa.jpql.tools.spi.IType getEnumType(java.lang.String)
meth public org.eclipse.persistence.jpa.jpql.tools.spi.IType getType(java.lang.Class<?>)
meth public org.eclipse.persistence.jpa.jpql.tools.spi.IType getType(java.lang.String)
supr java.lang.Object
hfds elements,mtp,packages,types

CLSS public abstract interface org.netbeans.modules.j2ee.persistence.spi.moduleinfo.JPAModuleInfo
fld public final static java.lang.String JPACHECKSUPPORTED = "jpaversionverification"
fld public final static java.lang.String JPAVERSIONPREFIX = "jpa"
innr public final static !enum ModuleType
meth public abstract java.lang.Boolean isJPAVersionSupported(java.lang.String)
meth public abstract java.lang.String getVersion()
meth public abstract org.netbeans.modules.j2ee.persistence.spi.moduleinfo.JPAModuleInfo$ModuleType getType()

CLSS public final static !enum org.netbeans.modules.j2ee.persistence.spi.moduleinfo.JPAModuleInfo$ModuleType
 outer org.netbeans.modules.j2ee.persistence.spi.moduleinfo.JPAModuleInfo
fld public final static org.netbeans.modules.j2ee.persistence.spi.moduleinfo.JPAModuleInfo$ModuleType EJB
fld public final static org.netbeans.modules.j2ee.persistence.spi.moduleinfo.JPAModuleInfo$ModuleType WEB
meth public static org.netbeans.modules.j2ee.persistence.spi.moduleinfo.JPAModuleInfo$ModuleType valueOf(java.lang.String)
meth public static org.netbeans.modules.j2ee.persistence.spi.moduleinfo.JPAModuleInfo$ModuleType[] values()
supr java.lang.Enum<org.netbeans.modules.j2ee.persistence.spi.moduleinfo.JPAModuleInfo$ModuleType>

CLSS public abstract interface org.netbeans.modules.j2ee.persistence.spi.provider.PersistenceProviderSupplier
meth public abstract boolean supportsDefaultProvider()
meth public abstract java.util.List<org.netbeans.modules.j2ee.persistence.provider.Provider> getSupportedProviders()

CLSS public abstract interface org.netbeans.modules.j2ee.persistence.spi.server.ServerStatusProvider
meth public abstract boolean validServerInstancePresent()

CLSS public abstract interface org.netbeans.modules.j2ee.persistence.spi.server.ServerStatusProvider2
intf org.netbeans.modules.j2ee.persistence.spi.server.ServerStatusProvider
meth public abstract boolean selectServer()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public abstract interface org.netbeans.modules.j2ee.persistence.spi.targetinfo.JPATargetInfo
innr public final static !enum TargetType
meth public abstract org.netbeans.modules.j2ee.persistence.spi.targetinfo.JPATargetInfo$TargetType getType(org.openide.filesystems.FileObject,java.lang.String)

CLSS public final static !enum org.netbeans.modules.j2ee.persistence.spi.targetinfo.JPATargetInfo$TargetType
 outer org.netbeans.modules.j2ee.persistence.spi.targetinfo.JPATargetInfo
fld public final static org.netbeans.modules.j2ee.persistence.spi.targetinfo.JPATargetInfo$TargetType ANY
fld public final static org.netbeans.modules.j2ee.persistence.spi.targetinfo.JPATargetInfo$TargetType EJB
meth public static org.netbeans.modules.j2ee.persistence.spi.targetinfo.JPATargetInfo$TargetType valueOf(java.lang.String)
meth public static org.netbeans.modules.j2ee.persistence.spi.targetinfo.JPATargetInfo$TargetType[] values()
supr java.lang.Enum<org.netbeans.modules.j2ee.persistence.spi.targetinfo.JPATargetInfo$TargetType>

CLSS public org.netbeans.modules.j2ee.persistence.unit.AddEntityDialog
cons public init()
meth public static java.util.List<java.lang.String> open(org.netbeans.modules.j2ee.persistence.api.EntityClassScope,java.util.Set<java.lang.String>)
supr java.lang.Object

CLSS public org.netbeans.modules.j2ee.persistence.unit.AddEntityPanel
meth public java.util.List<java.lang.String> getSelectedEntityClasses()
meth public static org.netbeans.modules.j2ee.persistence.unit.AddEntityPanel create(org.netbeans.modules.j2ee.persistence.api.EntityClassScope,java.util.Set<java.lang.String>)
supr javax.swing.JPanel
hfds entityList,jScrollPane1,readHelper

CLSS public org.netbeans.modules.j2ee.persistence.unit.PUDataLoader
cons public init()
fld public final static java.lang.String REQUIRED_MIME = "text/x-persistence1.0+xml"
meth protected java.lang.String actionsContext()
meth protected java.lang.String defaultDisplayName()
meth protected org.openide.loaders.MultiDataObject createMultiObject(org.openide.filesystems.FileObject) throws java.io.IOException
meth protected void initialize()
supr org.openide.loaders.UniFileLoader

CLSS public org.netbeans.modules.j2ee.persistence.unit.PUDataNode
cons public init(org.netbeans.modules.j2ee.persistence.unit.PUDataObject)
meth protected org.openide.nodes.Sheet createSheet()
supr org.openide.loaders.DataNode
hfds IMAGE_ICON_BASE

CLSS public org.netbeans.modules.j2ee.persistence.unit.PUDataObject
cons public init(org.openide.filesystems.FileObject,org.netbeans.modules.j2ee.persistence.unit.PUDataLoader) throws org.openide.loaders.DataObjectExistsException
fld protected boolean changedFromUI
fld public final static int UPDATE_DELAY = 200
fld public final static java.lang.String HELP_ID_DESIGN_PERSISTENCE_UNIT = "persistence_multiview_design_persistenceUnitNode"
fld public final static java.lang.String ICON = "org/netbeans/modules/j2ee/persistence/unit/PersistenceIcon.gif"
fld public final static java.lang.String NO_UI_PU_CLASSES_CHANGED = "non ui pu classes modified"
fld public final static java.lang.String PREFERRED_ID_DESIGN = "persistence_multiview_design"
fld public final static java.lang.String PREFERRED_ID_SOURCE = "persistence_multiview_source"
meth protected int associateLookup()
meth protected int getXMLMultiViewIndex()
meth protected java.awt.Image getXmlViewIcon()
meth protected java.lang.String getEditorMimeType()
meth protected java.lang.String getPrefixMark()
meth protected org.openide.nodes.Node createNodeDelegate()
meth public boolean addClass(org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit,java.lang.String,boolean)
meth public boolean isChangedFromUI()
meth public boolean isCopyAllowed()
meth public boolean isDeleteAllowed()
meth public boolean isMoveAllowed()
meth public boolean parseDocument()
meth public boolean viewCanBeDisplayed()
meth public org.netbeans.modules.j2ee.persistence.dd.common.Persistence getPersistence()
meth public org.netbeans.modules.xml.multiview.ToolBarMultiViewElement getActiveMultiViewElement0()
meth public static org.netbeans.modules.xml.multiview.XmlMultiViewElement createXmlMultiViewElement(org.openide.util.Lookup)
meth public void addPersistenceUnit(org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit)
meth public void modelUpdated()
meth public void modelUpdatedFromUI()
meth public void removeClass(org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit,java.lang.String,boolean)
meth public void removePersistenceUnit(org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit)
meth public void save()
meth public void setChangedFromUI(boolean)
meth public void showElement(java.lang.Object)
meth public void updateDataFromModel(org.openide.filesystems.FileLock) throws java.io.IOException
supr org.netbeans.modules.xml.multiview.XmlMultiViewDataObject
hfds DESIGN_VIEW_ID,LOG,PERSISTENCE_UNIT_ADDED_OR_REMOVED,TYPE_TOOLBAR,modelSynchronizer,persistence
hcls ModelSynchronizer

CLSS public org.netbeans.modules.j2ee.persistence.unit.PanelFactory
intf org.netbeans.modules.xml.multiview.ui.InnerPanelFactory
meth public org.netbeans.modules.xml.multiview.ui.SectionInnerPanel createInnerPanel(java.lang.Object)
supr java.lang.Object
hfds dObj,editor

CLSS public org.netbeans.modules.j2ee.persistence.unit.PersistenceCatalog
cons public init()
intf org.netbeans.modules.xml.catalog.spi.CatalogDescriptor2
intf org.netbeans.modules.xml.catalog.spi.CatalogReader
intf org.xml.sax.EntityResolver
meth public java.lang.String getDisplayName()
meth public java.lang.String getIconResource(int)
meth public java.lang.String getShortDescription()
meth public java.lang.String getSystemID(java.lang.String)
meth public java.lang.String resolvePublic(java.lang.String)
meth public java.lang.String resolveURI(java.lang.String)
meth public java.util.Iterator getPublicIDs()
meth public org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public void addCatalogListener(org.netbeans.modules.xml.catalog.spi.CatalogListener)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void refresh()
meth public void removeCatalogListener(org.netbeans.modules.xml.catalog.spi.CatalogListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds ORM_JAKARTA_NS,ORM_NS,ORM_OLD_NS,PERSISTENCE_JAKARTA_NS,PERSISTENCE_NS,PERSISTENCE_OLD_NS,RESOURCE_PATH,schemas
hcls SchemaInfo

CLSS public org.netbeans.modules.j2ee.persistence.unit.PersistenceCfgProperties
cons public init()
meth public static java.lang.Object getPossiblePropertyValue(org.netbeans.modules.j2ee.persistence.provider.Provider,java.lang.String)
meth public static java.util.List<java.lang.String> getKeys(org.netbeans.modules.j2ee.persistence.provider.Provider)
meth public static java.util.List<org.netbeans.modules.j2ee.persistence.provider.Provider> getProviders()
meth public static java.util.Map<org.netbeans.modules.j2ee.persistence.provider.Provider,java.util.Map<java.lang.String,java.lang.String[]>> getAllKeyAndValues()
supr java.lang.Object
hfds DN_CONN_POOLING,DN_TRX_ATTR,DN_TRX_ISO,DN_VALIDATION_MODE,EL_BATCHWRITER,EL_CACHE_TYPES,EL_CONTEXT_REFMODE,EL_DDL_GEN_MODE,EL_EXCLUSIVE_CON_MODE,EL_FLUSH_CLEAR_CACHE,EL_LOGGER,EL_LOGGER_LEVEL,EL_PROFILER,EL_TARGET_DATABASE,EL_TARGET_SERVER,EL_WEAVING,HIBERNATE_DIALECTS,RESOURCE_TYPE,SCHEMA_GEN_DB_OPTIONS,SCHEMA_GEN_SCRIPTS_OPTIONS,SCHEMA_GEN_SOURCE_TYPES,SHARED_CACHE_MODE,TRUE_FALSE,possiblePropertyValues
hcls KeyOrder

CLSS public org.netbeans.modules.j2ee.persistence.unit.PersistenceToolBarMVElement
cons public init(org.openide.util.Lookup)
intf java.beans.PropertyChangeListener
meth public org.netbeans.modules.xml.multiview.ui.SectionView getSectionView()
meth public void componentClosed()
meth public void componentOpened()
meth public void componentShowing()
meth public void propertyChange(java.beans.PropertyChangeEvent)
supr org.netbeans.modules.xml.multiview.ToolBarMultiViewElement
hfds addAction,comp,factory,modelListener,needInit,project,puDataObject,removeAction,repaintingTask,view
hcls AddAction,ElementLeafNode,ModelListener,PersistenceUnitNode,PersistenceView,RemoveAction

CLSS public org.netbeans.modules.j2ee.persistence.unit.PersistenceUnitPanel
cons public init(org.netbeans.modules.xml.multiview.ui.SectionView,org.netbeans.modules.j2ee.persistence.unit.PUDataObject,org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit)
meth protected void endUIChange()
meth protected void startUIChange()
meth public javax.swing.JComponent getErrorComponent(java.lang.String)
meth public void linkButtonPressed(java.lang.Object,java.lang.String)
meth public void rollbackValue(javax.swing.text.JTextComponent)
meth public void setValue(javax.swing.JComponent,java.lang.Object)
supr org.netbeans.modules.xml.multiview.ui.SectionInnerPanel
hfds RP,addClassButton,buttonGroup1,buttonGroup2,buttonGroup3,cachingStrategyLabel,cachingStrategyPanel,cachingTypes,dObj,datasourceLabel,ddAll,ddAuto,ddCallBack,ddCreate,ddDefault,ddDisableSelective,ddDropCreate,ddEnableSelective,ddNoValidation,ddNone,ddUnknown,dsCombo,entityClassesPanel,entityList,entityScrollPane,includeAllEntities,includeEntitiesLabel,isContainerManaged,jdbcComboBox,jdbcLabel,jpa2x,jtaCheckBox,libraryComboBox,libraryLabel,nameLabel,nameTextField,persistenceUnit,project,providerCombo,providerLabel,removeClassButton,tableGenerationLabel,tableGenerationPanel,validationModes,validationStrategyLabel,validationStrategyPanel

CLSS public org.netbeans.modules.j2ee.persistence.unit.PersistenceUnitPanelFactory
intf java.beans.PropertyChangeListener
intf org.netbeans.modules.xml.multiview.ui.InnerPanelFactory
meth public org.netbeans.modules.xml.multiview.ui.SectionInnerPanel createInnerPanel(java.lang.Object)
meth public void propertyChange(java.beans.PropertyChangeEvent)
supr java.lang.Object
hfds cache,dObj,editor

CLSS public org.netbeans.modules.j2ee.persistence.unit.PersistenceValidator
cons public init(org.netbeans.modules.j2ee.persistence.unit.PUDataObject)
meth protected boolean isJavaSE()
meth public java.util.List<org.netbeans.modules.xml.multiview.Error> validate()
supr java.lang.Object
hfds errors,puDataObject

CLSS public org.netbeans.modules.j2ee.persistence.unit.PropertiesPanel
cons public init(org.netbeans.modules.xml.multiview.ui.SectionView,org.netbeans.modules.j2ee.persistence.unit.PUDataObject,org.netbeans.modules.j2ee.persistence.unit.PropertiesPanel$PropertiesParamHolder)
innr public static PropertiesParamHolder
meth public javax.swing.JComponent getErrorComponent(java.lang.String)
meth public void linkButtonPressed(java.lang.Object,java.lang.String)
meth public void setValue(javax.swing.JComponent,java.lang.Object)
supr org.netbeans.modules.xml.multiview.ui.SectionInnerPanel
hfds filler

CLSS public static org.netbeans.modules.j2ee.persistence.unit.PropertiesPanel$PropertiesParamHolder
 outer org.netbeans.modules.j2ee.persistence.unit.PropertiesPanel
meth public org.netbeans.modules.j2ee.persistence.dd.common.Persistence getPersistence()
meth public org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit getPU()
meth public org.netbeans.modules.j2ee.persistence.provider.Provider getProvider()
supr java.lang.Object
hfds persistence,prov,pu

CLSS public org.netbeans.modules.j2ee.persistence.unit.PropertiesTableModel
cons public init(org.netbeans.modules.j2ee.persistence.unit.PropertiesPanel$PropertiesParamHolder)
meth public boolean isCellEditable(int,int)
meth public int getColumnCount()
meth public int getRowCount()
meth public java.lang.Object getValueAt(int,int)
meth public java.lang.String getColumnName(int)
meth public void addRow(java.lang.String,java.lang.String)
meth public void editRow(int,java.lang.String)
meth public void removeRow(int)
supr javax.swing.table.AbstractTableModel
hfds columnNames,jpa_prefix,propParam,propsData
hcls PropertyData

CLSS public org.netbeans.modules.j2ee.persistence.unit.PropertiesTablePanel
cons public init(org.netbeans.modules.j2ee.persistence.unit.PUDataObject,org.netbeans.modules.j2ee.persistence.unit.PropertiesPanel$PropertiesParamHolder,org.netbeans.modules.j2ee.persistence.unit.PropertiesTableModel)
supr org.netbeans.modules.xml.multiview.ui.DefaultTablePanel
hfds configDataObject,model,propParam
hcls PropertyPanelListner,TableActionListener

CLSS public org.netbeans.modules.j2ee.persistence.unit.PropertyPanel
cons public init(org.netbeans.modules.j2ee.persistence.unit.PropertiesPanel$PropertiesParamHolder,boolean,java.lang.String,java.lang.String)
intf java.awt.event.ActionListener
meth public java.lang.String getPropertyName()
meth public java.lang.String getPropertyValue()
meth public javax.swing.JTextField getValueComboBoxTextField()
meth public javax.swing.JTextField getValueTextField()
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void addNameComboBoxListener(java.awt.event.ActionListener)
meth public void addValueComponent(java.lang.String,java.lang.String)
supr javax.swing.JPanel
hfds nameComboBox,nameLabel,provider,valueComboBox,valueLabel,valuePanel,valueTextField

CLSS public org.netbeans.modules.j2ee.persistence.unit.Util
cons public init()
meth public static java.lang.String getResourcePath(org.netbeans.api.project.SourceGroup[],org.openide.filesystems.FileObject)
meth public static java.lang.String getResourcePath(org.netbeans.api.project.SourceGroup[],org.openide.filesystems.FileObject,char)
meth public static java.lang.String getResourcePath(org.netbeans.api.project.SourceGroup[],org.openide.filesystems.FileObject,char,boolean)
meth public static java.util.ArrayList<java.lang.String> getAvailPropNames(org.netbeans.modules.j2ee.persistence.dd.common.Persistence,org.netbeans.modules.j2ee.persistence.provider.Provider,org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit)
meth public static java.util.List<java.lang.String> getAllPropNames(org.netbeans.modules.j2ee.persistence.provider.Provider)
meth public static java.util.List<java.lang.String> getPropsNamesExceptGeneral(org.netbeans.modules.j2ee.persistence.provider.Provider)
meth public static org.netbeans.api.project.SourceGroup[] getJavaSourceGroups(org.netbeans.modules.j2ee.persistence.unit.PUDataObject) throws java.io.IOException
supr java.lang.Object

CLSS public org.netbeans.modules.j2ee.persistence.util.CustomClassLoader
cons public init(java.net.URL[],java.lang.ClassLoader)
meth protected java.lang.Class loadClass(java.lang.String,boolean) throws java.lang.ClassNotFoundException
meth protected java.security.PermissionCollection getPermissions(java.security.CodeSource)
meth public java.io.InputStream getResourceAsStream(java.lang.String)
supr java.net.URLClassLoader
hfds logger,package2File

CLSS public org.netbeans.modules.j2ee.persistence.util.EntityMethodGenerator
cons public init(org.netbeans.api.java.source.WorkingCopy,org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils,javax.lang.model.element.TypeElement)
meth public com.sun.source.tree.MethodTree createEqualsMethod(java.lang.String,java.util.List<com.sun.source.tree.VariableTree>)
meth public com.sun.source.tree.MethodTree createHashCodeMethod(java.util.List<com.sun.source.tree.VariableTree>)
meth public com.sun.source.tree.MethodTree createToStringMethod(java.lang.String,java.util.List<com.sun.source.tree.VariableTree>)
supr java.lang.Object
hfds copy,genUtils,scope

CLSS public org.netbeans.modules.j2ee.persistence.util.JPAClassPathHelper
cons public init(java.util.Set<org.netbeans.api.java.classpath.ClassPath>,java.util.Set<org.netbeans.api.java.classpath.ClassPath>,java.util.Set<org.netbeans.api.java.classpath.ClassPath>)
meth public org.netbeans.api.java.source.ClasspathInfo createClasspathInfo()
meth public org.netbeans.modules.j2ee.persistence.util.JPAClassPathHelper setModuleBootPaths(java.util.Set<org.netbeans.api.java.classpath.ClassPath>)
meth public org.netbeans.modules.j2ee.persistence.util.JPAClassPathHelper setModuleClassPaths(java.util.Set<org.netbeans.api.java.classpath.ClassPath>)
meth public org.netbeans.modules.j2ee.persistence.util.JPAClassPathHelper setModuleCompilePaths(java.util.Set<org.netbeans.api.java.classpath.ClassPath>)
meth public org.netbeans.modules.j2ee.persistence.util.JPAClassPathHelper setModuleSourcePaths(java.util.Set<org.netbeans.api.java.classpath.ClassPath>)
supr java.lang.Object
hfds boot,compile,moduleBoot,moduleClass,moduleCompile,moduleSource,source

CLSS public org.netbeans.modules.j2ee.persistence.util.MetadataModelReadHelper<%0 extends java.lang.Object, %1 extends java.lang.Object>
innr public final static !enum State
meth public org.netbeans.modules.j2ee.persistence.util.MetadataModelReadHelper$State getState()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.netbeans.modules.j2ee.persistence.util.MetadataModelReadHelper<{%%0},{%%1}> create(org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<{%%0}>,org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction<{%%0},{%%1}>)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void start()
meth public {org.netbeans.modules.j2ee.persistence.util.MetadataModelReadHelper%1} getResult() throws java.util.concurrent.ExecutionException
supr java.lang.Object
hfds RP,action,changeSupport,eventRP,executionException,model,reader,result,state
hcls Reader

CLSS public final static !enum org.netbeans.modules.j2ee.persistence.util.MetadataModelReadHelper$State
 outer org.netbeans.modules.j2ee.persistence.util.MetadataModelReadHelper
fld public final static org.netbeans.modules.j2ee.persistence.util.MetadataModelReadHelper$State FINISHED
fld public final static org.netbeans.modules.j2ee.persistence.util.MetadataModelReadHelper$State IDLE
fld public final static org.netbeans.modules.j2ee.persistence.util.MetadataModelReadHelper$State READING_MODEL
fld public final static org.netbeans.modules.j2ee.persistence.util.MetadataModelReadHelper$State WAITING_READY
meth public static org.netbeans.modules.j2ee.persistence.util.MetadataModelReadHelper$State valueOf(java.lang.String)
meth public static org.netbeans.modules.j2ee.persistence.util.MetadataModelReadHelper$State[] values()
supr java.lang.Enum<org.netbeans.modules.j2ee.persistence.util.MetadataModelReadHelper$State>

CLSS public org.netbeans.modules.j2ee.persistence.util.PersistenceEnvironmentImpl
cons public init(org.netbeans.api.project.Project)
intf org.netbeans.modules.j2ee.persistence.api.PersistenceEnvironment
meth public java.lang.ClassLoader getProjectClassLoader(java.net.URL[])
meth public java.util.List<java.net.URL> getProjectClassPath()
meth public java.util.List<java.net.URL> getProjectClassPath(org.openide.filesystems.FileObject)
meth public org.netbeans.api.project.Project getProject()
meth public org.openide.filesystems.FileObject getLocation()
supr java.lang.Object
hfds loaderRef,project

CLSS public final org.netbeans.modules.j2ee.persistence.util.PersistenceProviderComboboxHelper
cons public init(org.netbeans.api.project.Project)
innr public abstract interface static LibraryItem
meth public void connect(javax.swing.JComboBox)
supr java.lang.Object
hfds EMPTY,SEPARATOR,preferredProvider,project,providerSupplier
hcls DefaultPersistenceProviderSupplier,ManageLibrariesItem,NewPersistenceLibraryItem,PersistenceProviderCellRenderer

CLSS public abstract interface static org.netbeans.modules.j2ee.persistence.util.PersistenceProviderComboboxHelper$LibraryItem
 outer org.netbeans.modules.j2ee.persistence.util.PersistenceProviderComboboxHelper
meth public abstract java.lang.String getText()
meth public abstract void performAction()

CLSS public org.netbeans.modules.j2ee.persistence.util.SourceLevelChecker
meth public static boolean isSourceLevel14orLower(org.netbeans.api.project.Project)
meth public static java.lang.String getSourceLevel(org.netbeans.api.project.Project)
supr java.lang.Object

CLSS public org.netbeans.modules.j2ee.persistence.wizard.EntityClosure
meth public boolean isClosureEnabled()
meth public boolean isEjbModuleInvolved()
meth public boolean isModelReady()
meth public java.util.Set<java.lang.String> getAvailableEntities()
meth public java.util.Set<java.lang.String> getSelectedEntities()
meth public java.util.Set<java.lang.String> getWantedEntities()
meth public java.util.Set<org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity> getAvailableEntityInstances()
meth public static javax.swing.ComboBoxModel getAsComboModel(org.netbeans.modules.j2ee.persistence.wizard.EntityClosure)
meth public static org.netbeans.modules.j2ee.persistence.wizard.EntityClosure create(org.netbeans.modules.j2ee.persistence.api.EntityClassScope,org.netbeans.api.project.Project)
meth public void addAllEntities()
meth public void addAvaliableEntities(java.util.Set<org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity>)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void addEntities(java.util.Set<java.lang.String>)
meth public void removeAllEntities()
meth public void removeEntities(java.util.Set<java.lang.String>)
meth public void setClosureEnabled(boolean)
supr java.lang.Object
hfds LOG,availableEntities,availableEntityInstances,changeSupport,closureEnabled,ejbModuleInvolved,fqnEntityMap,fqnIdExistMap,model,modelReady,project,readHelper,referencedEntities,selectedEntities,wantedEntities
hcls EntityClosureComboBoxModel,Queue

CLSS public final org.netbeans.modules.j2ee.persistence.wizard.PersistenceClientEntitySelection
cons public init(java.lang.String,org.openide.util.HelpCtx,org.openide.WizardDescriptor)
fld public final static java.lang.String DISABLENOIDSELECTION = "disableNoIdSelection"
intf javax.swing.event.ChangeListener
intf org.openide.WizardDescriptor$FinishablePanel
intf org.openide.WizardDescriptor$Panel
meth protected final void fireChangeEvent(javax.swing.event.ChangeEvent)
meth public boolean isFinishPanel()
meth public boolean isValid()
meth public final void addChangeListener(javax.swing.event.ChangeListener)
meth public final void removeChangeListener(javax.swing.event.ChangeListener)
meth public java.awt.Component getComponent()
meth public org.openide.util.HelpCtx getHelp()
meth public void readSettings(java.lang.Object)
meth public void stateChanged(javax.swing.event.ChangeEvent)
meth public void storeSettings(java.lang.Object)
supr java.lang.Object
hfds changeSupport,component,disableNoIdSelection,helpCtx,panelName,wizardDescriptor

CLSS public org.netbeans.modules.j2ee.persistence.wizard.PersistenceClientEntitySelectionVisual
cons public init(java.lang.String,org.openide.WizardDescriptor)
cons public init(java.lang.String,org.openide.WizardDescriptor,boolean)
meth public boolean getCreatePersistenceUnit()
meth public boolean valid(org.openide.WizardDescriptor)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void read(org.openide.WizardDescriptor)
meth public void store(org.openide.WizardDescriptor)
meth public void updatePersistenceUnitButton()
supr javax.swing.JPanel
hfds ENTITY_LIST_RENDERER_AV,ENTITY_LIST_RENDERER_SEL,buttonAdd,buttonAddAll,buttonRemove,buttonRemoveAll,cbAddRelated,changeSupport,createPU,createPUCheckbox,disableNoIdSelection,entityClosure,jPanel1,jPanel2,jScrollPane1,jScrollPane2,labelAvailableEntities,labelSelectedEntities,listAvailable,listSelected,panelButtons,project,serialVersionUID,workAround1,workAround2
hcls EntityListCellRenderer,EntityListModel

CLSS public org.netbeans.modules.j2ee.persistence.wizard.Util
cons public init()
meth public static boolean createPersistenceUnitUsingWizard(org.netbeans.api.project.Project,java.lang.String) throws org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException
meth public static boolean createPersistenceUnitUsingWizard(org.netbeans.api.project.Project,java.lang.String,org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel$TableGeneration) throws org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException
meth public static boolean isContainerManaged(org.netbeans.api.project.Project)
meth public static boolean isDefaultProvider(org.netbeans.api.project.Project,org.netbeans.modules.j2ee.persistence.provider.Provider)
meth public static boolean isEjb21Module(org.netbeans.api.project.Project)
meth public static boolean isEjbModule(org.netbeans.api.project.Project)
meth public static boolean isJPAVersionSupported(org.netbeans.api.project.Project,java.lang.String)
meth public static boolean isJavaSE(org.netbeans.api.project.Project)
meth public static boolean isSupportedJavaEEVersion(org.netbeans.api.project.Project)
meth public static java.lang.String getCandidateName(org.netbeans.api.project.Project)
meth public static java.lang.String getJPAVersionSupported(org.netbeans.api.project.Project,java.lang.String)
meth public static java.lang.String getPersistenceUnitAsString(org.netbeans.api.project.Project,java.lang.String) throws java.io.IOException
meth public static java.util.ArrayList<org.netbeans.modules.j2ee.persistence.provider.Provider> getProviders(org.netbeans.api.project.Project)
meth public static java.util.Set getEnabledItems(javax.swing.JList)
meth public static java.util.Set getSelectedItems(javax.swing.JList,boolean)
meth public static javax.swing.JLabel findLabel(javax.swing.JComponent,java.lang.String)
meth public static org.netbeans.api.java.classpath.ClassPath getFullClasspath(org.openide.filesystems.FileObject)
meth public static org.netbeans.api.project.SourceGroup getClassSourceGroup(org.netbeans.api.project.Project,java.lang.String)
meth public static org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit buildPersistenceUnitUsingData(org.netbeans.api.project.Project,java.lang.String,java.lang.String,org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel$TableGeneration,org.netbeans.modules.j2ee.persistence.provider.Provider)
meth public static org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit buildPersistenceUnitUsingData(org.netbeans.api.project.Project,java.lang.String,java.lang.String,org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel$TableGeneration,org.netbeans.modules.j2ee.persistence.provider.Provider,java.lang.String)
meth public static org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit buildPersistenceUnitUsingWizard(org.netbeans.api.project.Project,java.lang.String,org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel$TableGeneration)
meth public static org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit[] getPersistenceUnits(org.netbeans.api.project.Project) throws java.io.IOException
meth public static org.netbeans.modules.j2ee.persistence.provider.Provider getDefaultProvider(org.netbeans.api.project.Project)
meth public static org.netbeans.modules.j2ee.persistence.provider.Provider getPreferredProvider(org.netbeans.api.project.Project)
meth public static void addLibraryToProject(org.netbeans.api.project.Project,org.netbeans.api.project.libraries.Library)
meth public static void addLibraryToProject(org.netbeans.api.project.Project,org.netbeans.api.project.libraries.Library,java.lang.String)
meth public static void addPersistenceUnitToProject(org.netbeans.api.project.Project)
meth public static void addPersistenceUnitToProject(org.netbeans.api.project.Project,org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit)
meth public static void addPersistenceUnitToProjectRoot(org.netbeans.api.project.Project,org.openide.filesystems.FileObject,org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit)
meth public static void changeLabelInComponent(javax.swing.JComponent,java.lang.String,java.lang.String)
meth public static void getAllComponents(java.awt.Component[],java.util.Collection)
meth public static void hideLabelAndLabelFor(javax.swing.JComponent,java.lang.String)
supr java.lang.Object
hfds JPA_VERSIONS
hcls AddLibrary,DefaultPersistenceProviderSupplier

CLSS public org.netbeans.modules.j2ee.persistence.wizard.WizardProperties
cons public init()
fld public final static java.lang.String CREATE_PERSISTENCE_UNIT = "CreatePersistenceUnit"
fld public final static java.lang.String ENTITY_CLASS = "EntityClass"
supr java.lang.Object

CLSS public org.netbeans.modules.j2ee.persistence.wizard.fromdb.DBSchemaFileList
cons public init(org.netbeans.api.project.Project,org.openide.filesystems.FileObject)
meth public java.lang.String getDisplayName(org.openide.filesystems.FileObject)
meth public java.util.List<org.openide.filesystems.FileObject> getFileList()
supr java.lang.Object
hfds dbschema2DisplayName,dbschemaList
hcls DBSchemaComparator

CLSS public org.netbeans.modules.j2ee.persistence.wizard.fromdb.DBSchemaManager
cons public init()
fld public final static java.lang.String DBSCHEMA_EXT = "dbschema"
meth public org.netbeans.modules.dbschema.SchemaElement getSchemaElement(org.netbeans.api.db.explorer.DatabaseConnection) throws java.sql.SQLException
meth public org.netbeans.modules.dbschema.SchemaElement getSchemaElement(org.openide.filesystems.FileObject)
meth public static org.openide.filesystems.FileObject updateDBSchemas(org.netbeans.modules.dbschema.SchemaElement,org.netbeans.modules.j2ee.persistence.wizard.fromdb.DBSchemaFileList,org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
supr java.lang.Object
hfds conn,exception,fileSchemaElement,oldDBConn,oldDBConnWasConnected,schemaElement,schemaFileObject

CLSS public org.netbeans.modules.j2ee.persistence.wizard.fromdb.DBSchemaTableProvider
cons public init(org.netbeans.modules.dbschema.SchemaElement,org.netbeans.modules.j2ee.persistence.wizard.fromdb.PersistenceGenerator)
cons public init(org.netbeans.modules.dbschema.SchemaElement,org.netbeans.modules.j2ee.persistence.wizard.fromdb.PersistenceGenerator,org.netbeans.api.project.Project)
intf org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableProvider
meth public java.util.Set<org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table> getTables()
supr java.lang.Object
hfds persistenceGen,project,schemaElement,tables,tablesReferecedByOtherTables
hcls DBSchemaTable

CLSS public org.netbeans.modules.j2ee.persistence.wizard.fromdb.DBSchemaUISupport
meth public static void connect(javax.swing.JComboBox,org.netbeans.modules.j2ee.persistence.wizard.fromdb.DBSchemaFileList)
supr java.lang.Object
hcls DBSchemaModel,DBSchemaRenderer

CLSS public org.netbeans.modules.j2ee.persistence.wizard.fromdb.DatabaseTablesPanel
cons public init()
innr public final static WizardPanel
intf javax.swing.event.AncestorListener
meth public java.lang.String getDatasourceName()
meth public org.netbeans.api.db.explorer.DatabaseConnection getDatabaseConnection()
meth public org.netbeans.modules.dbschema.SchemaElement getSourceSchemaElement()
meth public org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableClosure getTableClosure()
meth public org.openide.filesystems.FileObject getDBSchemaFile()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void ancestorAdded(javax.swing.event.AncestorEvent)
meth public void ancestorMoved(javax.swing.event.AncestorEvent)
meth public void ancestorRemoved(javax.swing.event.AncestorEvent)
meth public void initialize(org.netbeans.api.project.Project,org.netbeans.modules.j2ee.persistence.wizard.fromdb.DBSchemaFileList,org.netbeans.modules.j2ee.persistence.wizard.fromdb.PersistenceGenerator,org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableSource,org.openide.filesystems.FileObject)
supr javax.swing.JPanel
hfds addAllButton,addAllTypeCombo,addButton,allowUpdateRecreate,availableTablesLabel,availableTablesList,availableTablesScrollPane,buttonPanel,changeListener,changeSupport,comboPanel,datasourceLabel,datasourceLocalComboBox,datasourceLocalRadioButton,datasourceName,datasourceServerComboBox,datasourceServerRadioButton,dbconn,dbschemaComboBox,dbschemaFile,dbschemaFileList,dbschemaManager,dbschemaRadioButton,filterAvailable,filterComboTxts,persistenceGen,project,removeAllButton,removeButton,schemaSource,selectedTablesLabel,selectedTablesList,selectedTablesScrollPane,serverStatusProvider,sourceSchemaElement,sourceSchemaUpdateEnabled,tableClosure,tableClosureCheckBox,tableError,tableErrorScroll,tableSource,tablesPanel,targetFolder
hcls ItemListCellRenderer,TablesPanel

CLSS public final static org.netbeans.modules.j2ee.persistence.wizard.fromdb.DatabaseTablesPanel$WizardPanel
 outer org.netbeans.modules.j2ee.persistence.wizard.fromdb.DatabaseTablesPanel
cons public init(java.lang.String)
intf javax.swing.event.ChangeListener
intf org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor>
meth public boolean isValid()
meth public org.netbeans.modules.j2ee.persistence.wizard.fromdb.DatabaseTablesPanel getComponent()
meth public org.openide.util.HelpCtx getHelp()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void readSettings(org.openide.WizardDescriptor)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void stateChanged(javax.swing.event.ChangeEvent)
meth public void storeSettings(org.openide.WizardDescriptor)
supr java.lang.Object
hfds changeSupport,component,componentInitialized,project,title,waitingForScan,wizardDescriptor

CLSS public org.netbeans.modules.j2ee.persistence.wizard.fromdb.EmptyTableProvider
cons public init()
intf org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableProvider
meth public java.util.Set<org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table> getTables()
supr java.lang.Object

CLSS public org.netbeans.modules.j2ee.persistence.wizard.fromdb.EntityClassesPanel
innr public final static WizardPanel
meth public boolean getCreatePersistenceUnit()
meth public boolean getGenerateFinderMethods()
meth public boolean getGenerateJAXB()
meth public boolean getGenerateMappedSuperclasses()
meth public boolean getGenerateValidationConstraints()
meth public java.lang.String getPackageName()
meth public org.netbeans.api.project.SourceGroup getLocationValue()
meth public org.netbeans.modules.j2ee.persistence.wizard.fromdb.SelectedTables getSelectedTables()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void initialize(org.netbeans.modules.j2ee.persistence.wizard.fromdb.PersistenceGenerator,org.netbeans.api.project.Project,org.openide.filesystems.FileObject)
meth public void update(org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableClosure,java.lang.String)
supr javax.swing.JPanel
hfds LOGGER,allToRecreateItem,allToUpdateItem,changeSupport,classNamesLabel,classNamesScrollPane,classNamesTable,createPUCheckbox,createPUWarningLabel,generateFinderMethodsCheckBox,generateJAXBCheckBox,locationComboBox,locationLabel,mappedSuperclassCheckBox,packageComboBox,packageComboBoxEditor,packageLabel,persistenceGen,project,projectLabel,projectTextField,puRequired,selectedTables,spacerPanel,specifyNamesLabel,tableActionsButton,tableActionsPopup,tableSourceName
hcls AllToRecreateAction,AllToUpdateAction,ShyLabel

CLSS public final static org.netbeans.modules.j2ee.persistence.wizard.fromdb.EntityClassesPanel$WizardPanel
 outer org.netbeans.modules.j2ee.persistence.wizard.fromdb.EntityClassesPanel
cons public init()
cons public init(boolean)
cons public init(boolean,boolean)
cons public init(boolean,boolean,boolean)
intf javax.swing.event.ChangeListener
intf org.openide.WizardDescriptor$FinishablePanel
intf org.openide.WizardDescriptor$Panel
meth public boolean isFinishPanel()
meth public boolean isValid()
meth public org.netbeans.modules.j2ee.persistence.wizard.fromdb.EntityClassesPanel getComponent()
meth public org.openide.util.HelpCtx getHelp()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void readSettings(java.lang.Object)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void stateChanged(javax.swing.event.ChangeEvent)
meth public void storeSettings(java.lang.Object)
supr java.lang.Object
hfds JAXBRequired,changeSupport,component,componentInitialized,isFinishable,isJPA,project,providers,puRequired,wizardDescriptor

CLSS public abstract interface org.netbeans.modules.j2ee.persistence.wizard.fromdb.FacadeGenerator
fld public final static java.lang.String FACADE_SUFFIX = "Facade"
meth public abstract java.util.Set<org.openide.filesystems.FileObject> generate(org.netbeans.api.project.Project,java.util.Map<java.lang.String,java.lang.String>,org.openide.filesystems.FileObject,java.lang.String,java.lang.String,java.lang.String,boolean,boolean,boolean) throws java.io.IOException

CLSS public abstract interface org.netbeans.modules.j2ee.persistence.wizard.fromdb.FacadeGeneratorProvider
meth public abstract java.lang.String getGeneratorType()
meth public abstract org.netbeans.modules.j2ee.persistence.wizard.fromdb.FacadeGenerator createGenerator()

CLSS public org.netbeans.modules.j2ee.persistence.wizard.fromdb.JavaPersistenceGenerator
cons public init()
cons public init(org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit)
intf org.netbeans.modules.j2ee.persistence.wizard.fromdb.PersistenceGenerator
meth public java.lang.String generateEntityName(java.lang.String)
meth public java.lang.String getFQClassName(java.lang.String)
meth public java.util.Set<org.openide.filesystems.FileObject> createdObjects()
meth public void generateBeans(org.netbeans.modules.j2ee.persistence.wizard.fromdb.ProgressPanel,org.netbeans.modules.j2ee.persistence.wizard.fromdb.RelatedCMPHelper,org.openide.filesystems.FileObject,org.netbeans.api.progress.aggregate.ProgressContributor) throws java.io.IOException
meth public void init(org.openide.WizardDescriptor)
meth public void uninit()
supr java.lang.Object
hfds addToAutoDiscoveredPU,entityName2TableName,fieldAccess,genNamedParams,genSerializableEntities,getters,initProject,persistenceUnit,result,setters,variables
hcls Generator

CLSS public org.netbeans.modules.j2ee.persistence.wizard.fromdb.JavaPersistenceGeneratorProvider
cons public init()
intf org.netbeans.modules.j2ee.persistence.wizard.fromdb.PersistenceGeneratorProvider
meth public java.lang.String getGeneratorType()
meth public org.netbeans.modules.j2ee.persistence.wizard.fromdb.PersistenceGenerator createGenerator()
supr java.lang.Object

CLSS public org.netbeans.modules.j2ee.persistence.wizard.fromdb.MappingOptionsPanel
cons public init()
innr public final static WizardPanel
meth public boolean isFullyQualifiedTableName()
meth public boolean isGenerateUnresolved()
meth public boolean isRegenSchemaAttributes()
meth public boolean isUseColumnNamesInRelationships()
meth public boolean isUseDefaults()
meth public org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation$CollectionType getCollectionType()
meth public org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation$FetchType getFetchType()
meth public void initialize(org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation$CollectionType,org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation$FetchType,boolean,boolean,boolean)
supr javax.swing.JPanel
hfds collectionTypeComboBox,collectionTypeLabel,defaultsCheckBox,descLabel,fetchComboBox,fetchLabel,paddingPanel,regenTablesCheckBox,relationshipColumnNamesCheckBox,relationshipsUnresolvedCheckBox,tableNameCheckBox

CLSS public final static org.netbeans.modules.j2ee.persistence.wizard.fromdb.MappingOptionsPanel$WizardPanel
 outer org.netbeans.modules.j2ee.persistence.wizard.fromdb.MappingOptionsPanel
cons public init()
intf org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor>
meth public boolean isValid()
meth public org.netbeans.modules.j2ee.persistence.wizard.fromdb.MappingOptionsPanel getComponent()
meth public org.openide.util.HelpCtx getHelp()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void readSettings(org.openide.WizardDescriptor)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void storeSettings(org.openide.WizardDescriptor)
supr java.lang.Object
hfds component,componentInitialized,wizardDescriptor

CLSS public abstract interface org.netbeans.modules.j2ee.persistence.wizard.fromdb.PersistenceGenerator
meth public abstract java.lang.String generateEntityName(java.lang.String)
meth public abstract java.lang.String getFQClassName(java.lang.String)
meth public abstract java.util.Set<org.openide.filesystems.FileObject> createdObjects()
meth public abstract void generateBeans(org.netbeans.modules.j2ee.persistence.wizard.fromdb.ProgressPanel,org.netbeans.modules.j2ee.persistence.wizard.fromdb.RelatedCMPHelper,org.openide.filesystems.FileObject,org.netbeans.api.progress.aggregate.ProgressContributor) throws java.io.IOException
meth public abstract void init(org.openide.WizardDescriptor)
meth public abstract void uninit()

CLSS public abstract interface org.netbeans.modules.j2ee.persistence.wizard.fromdb.PersistenceGeneratorProvider
meth public abstract java.lang.String getGeneratorType()
meth public abstract org.netbeans.modules.j2ee.persistence.wizard.fromdb.PersistenceGenerator createGenerator()

CLSS public org.netbeans.modules.j2ee.persistence.wizard.fromdb.ProgressPanel
cons public init()
meth public java.awt.Dimension getPreferredSize()
meth public void close()
meth public void open(javax.swing.JComponent,java.lang.String)
meth public void setText(java.lang.String)
supr javax.swing.JPanel
hfds dialog,holder,info

CLSS public org.netbeans.modules.j2ee.persistence.wizard.fromdb.RelatedCMPHelper
cons public init(org.netbeans.api.project.Project,org.openide.filesystems.FileObject,org.netbeans.modules.j2ee.persistence.wizard.fromdb.PersistenceGenerator)
meth public boolean isCreatePU()
meth public boolean isFullyQualifiedTableNames()
meth public boolean isGenerateFinderMethods()
meth public boolean isGenerateJAXBAnnotations()
meth public boolean isGenerateMappedSuperclasses()
meth public boolean isGenerateUnresolvedRelationships()
meth public boolean isGenerateValidationConstraints()
meth public boolean isRegenTablesAttrs()
meth public boolean isUseColumnNamesInRelationships()
meth public boolean isUseDefaults()
meth public java.lang.String getPackageName()
meth public org.netbeans.api.db.explorer.DatabaseConnection getDatabaseConnection()
meth public org.netbeans.api.project.Project getProject()
meth public org.netbeans.api.project.SourceGroup getLocation()
meth public org.netbeans.modules.dbschema.SchemaElement getSchemaElement()
meth public org.netbeans.modules.j2ee.persistence.entitygenerator.EntityClass[] getBeans()
meth public org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation$CollectionType getCollectionType()
meth public org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation$FetchType getFetchType()
meth public org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation[] getRelations()
meth public org.netbeans.modules.j2ee.persistence.wizard.fromdb.DBSchemaFileList getDBSchemaFileList()
meth public org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableClosure getTableClosure()
meth public org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableSource getTableSource()
meth public org.openide.filesystems.FileObject getDBSchemaFile()
meth public void buildBeans()
meth public void setCollectionType(org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation$CollectionType)
meth public void setCreatePU(boolean)
meth public void setFetchType(org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation$FetchType)
meth public void setFullyQualifiedTableNames(boolean)
meth public void setGenerateFinderMethods(boolean)
meth public void setGenerateJAXBAnnotations(boolean)
meth public void setGenerateMappedSuperclasses(boolean)
meth public void setGenerateUnresolvedRelationships(boolean)
meth public void setGenerateValidationConstraints(boolean)
meth public void setLocation(org.netbeans.api.project.SourceGroup)
meth public void setPackageName(java.lang.String)
meth public void setRegenTablesAttrs(boolean)
meth public void setSelectedTables(org.netbeans.modules.j2ee.persistence.wizard.fromdb.SelectedTables)
meth public void setTableClosure(org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableClosure)
meth public void setTableSource(org.netbeans.modules.dbschema.SchemaElement,org.netbeans.api.db.explorer.DatabaseConnection,java.lang.String)
meth public void setTableSource(org.netbeans.modules.dbschema.SchemaElement,org.openide.filesystems.FileObject)
meth public void setUseColumnNamesInRelationships(boolean)
meth public void setUseDefaults(boolean)
supr java.lang.Object
hfds collectionType,configFilesFolder,createPU,datasourceName,dbconn,dbschemaFile,dbschemaFileList,fetchType,fullyQualifiedTableNames,generateFinderMethods,generateJAXBAnnotations,generateMappedSuperclasses,generateUnresolvedRelationships,generateValidationConstraints,generator,location,packageName,persistenceGen,persistenceUnit,project,regenTablesAttrs,schemaElement,selectedTables,tableClosure,tableSource,useColumnNamesInRelationships,useDefaults
hcls GenerateTablesImpl

CLSS public org.netbeans.modules.j2ee.persistence.wizard.fromdb.RelatedCMPWizard
cons public init(java.lang.String)
intf org.openide.loaders.TemplateWizard$Iterator
meth public boolean hasNext()
meth public boolean hasPrevious()
meth public final void initialize(org.openide.loaders.TemplateWizard)
meth public final void uninitialize(org.openide.loaders.TemplateWizard)
meth public java.lang.String name()
meth public java.util.Set<org.openide.loaders.DataObject> instantiate(org.openide.loaders.TemplateWizard) throws java.io.IOException
meth public org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor> current()
meth public static org.netbeans.modules.j2ee.persistence.wizard.fromdb.RelatedCMPWizard createForJPA()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void nextPanel()
meth public void previousPanel()
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
hfds PERSISTENCE_PROVIDERS,PROP_HELPER,RP,TYPE_JPA,currentPanel,generator,helper,panels,progressPanel,project,type,wizardDescriptor

CLSS public final org.netbeans.modules.j2ee.persistence.wizard.fromdb.SelectedTables
cons public init(org.netbeans.modules.j2ee.persistence.wizard.fromdb.PersistenceGenerator,org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableClosure,org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth public java.lang.String getClassName(org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table)
meth public java.lang.String getFirstProblemDisplayName()
meth public java.util.List<org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table> getTables()
meth public org.netbeans.modules.j2ee.persistence.wizard.fromdb.UpdateType getUpdateType(org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void ensureUniqueClassNames()
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void setClassName(org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table,java.lang.String)
meth public void setTableClosureAndTargetFolder(org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableClosure,org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth public void setTargetFolder(org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth public void setUpdateType(org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table,org.netbeans.modules.j2ee.persistence.wizard.fromdb.UpdateType)
supr java.lang.Object
hfds changeSupport,location,packageName,persistenceGen,table2ClassName,table2Problems,table2UpdateType,tableClosure,tableClosureListener,targetFolder,validatedTables
hcls Problem,TableClosureListener

CLSS public org.netbeans.modules.j2ee.persistence.wizard.fromdb.SourceGroupUISupport
meth public static void connect(javax.swing.JComboBox,org.netbeans.api.project.SourceGroup[])
supr java.lang.Object
hcls SourceGroupRenderer

CLSS public abstract org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table
cons public init(java.lang.String,java.lang.String,java.lang.String,boolean,org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table$DisabledReason)
cons public init(java.lang.String,java.lang.String,java.lang.String,boolean,org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table$DisabledReason,boolean)
innr public final static ExistingDisabledReason
innr public final static ExistingNotInSourceDisabledReason
innr public final static ExistingReadOnlyDisabledReason
innr public final static NoPrimaryKeyDisabledReason
innr public static DisabledReason
intf java.lang.Comparable<org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table>
meth public abstract java.util.Set<java.util.List<java.lang.String>> getUniqueConstraints()
meth public abstract java.util.Set<org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table> getJoinTables()
meth public abstract java.util.Set<org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table> getReferencedByTables()
meth public abstract java.util.Set<org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table> getReferencedTables()
meth public boolean equals(java.lang.Object)
meth public boolean isDisabled()
meth public boolean isJoin()
meth public boolean isTable()
meth public int compareTo(org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table)
meth public int hashCode()
meth public java.lang.String getCatalog()
meth public java.lang.String getName()
meth public java.lang.String getSchema()
meth public java.lang.String toString()
meth public org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table$DisabledReason getDisabledReason()
supr java.lang.Object
hfds catalog,disabledReason,join,name,schema,tableOrView

CLSS public static org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table$DisabledReason
 outer org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table
cons public init(java.lang.String,java.lang.String)
meth public java.lang.String getDescription()
meth public java.lang.String getDisplayName()
supr java.lang.Object
hfds description,displayName

CLSS public final static org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table$ExistingDisabledReason
 outer org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table
cons public init(java.lang.String)
meth public java.lang.String getFQClassName()
supr org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table$DisabledReason
hfds fqClassName

CLSS public final static org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table$ExistingNotInSourceDisabledReason
 outer org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table
cons public init(java.lang.String)
meth public java.lang.String getFQClassName()
supr org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table$DisabledReason
hfds fqClassName

CLSS public final static org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table$ExistingReadOnlyDisabledReason
 outer org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table
cons public init(java.lang.String)
meth public java.lang.String getFQClassName()
supr org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table$DisabledReason
hfds fqClassName

CLSS public final static org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table$NoPrimaryKeyDisabledReason
 outer org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table
cons public init()
supr org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table$DisabledReason

CLSS public org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableClosure
cons public init(org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableProvider)
meth public boolean canAddAllTables(java.util.Set<org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table>)
meth public boolean canAddSomeTables(java.util.Set<org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table>)
meth public boolean canRemoveAllTables(java.util.Set<org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table>)
meth public boolean getClosureEnabled()
meth public java.util.Set<org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table> getAvailableTables()
meth public java.util.Set<org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table> getReferencedTables()
meth public java.util.Set<org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table> getSelectedTables()
meth public void addAllTables()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void addTables(java.util.Set<org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table>)
meth public void removeAllTables()
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void removeTables(java.util.Set<org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table>)
meth public void setClosureEnabled(boolean)
supr java.lang.Object
hfds availableTables,changeSupport,closureEnabled,referencedTables,selectedTables,tables,unmodifAvailableTables,unmodifReferencedTables,unmodifSelectedTables,unmodifWantedTables,wantedTables
hcls Queue

CLSS public abstract interface org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableProvider
meth public abstract java.util.Set<org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table> getTables()

CLSS public org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableSource
cons public init(java.lang.String,org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableSource$Type)
innr public final static !enum Type
meth public java.lang.String getName()
meth public org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableSource$Type getType()
meth public static org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableSource get(org.netbeans.api.project.Project)
meth public static void put(org.netbeans.api.project.Project,org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableSource)
supr java.lang.Object
hfds PROJECT_TO_SOURCE,name,type

CLSS public final static !enum org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableSource$Type
 outer org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableSource
fld public final static org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableSource$Type CONNECTION
fld public final static org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableSource$Type DATA_SOURCE
fld public final static org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableSource$Type SCHEMA_FILE
meth public static org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableSource$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableSource$Type[] values()
supr java.lang.Enum<org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableSource$Type>

CLSS public org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableUISupport
innr public final static !enum FilterAvailable
meth public static java.util.Set<org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table> getEnabledTables(javax.swing.JList)
meth public static java.util.Set<org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table> getSelectedTables(javax.swing.JList)
meth public static java.util.Set<org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table> getSelectedTables(javax.swing.JList,boolean)
meth public static javax.swing.JList createTableList()
meth public static void connectAvailable(javax.swing.JList,org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableClosure,org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableUISupport$FilterAvailable)
meth public static void connectClassNames(javax.swing.JTable,org.netbeans.modules.j2ee.persistence.wizard.fromdb.SelectedTables)
meth public static void connectSelected(javax.swing.JList,org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableClosure)
supr java.lang.Object
hcls AvailableTableRenderer,AvailableTablesModel,ClassNamesTable,SelectedTableRenderer,SelectedTablesModel,TableClassNameRenderer,TableClassNamesModel,TableJList,TableModel

CLSS public final static !enum org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableUISupport$FilterAvailable
 outer org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableUISupport
fld public final static org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableUISupport$FilterAvailable ANY
fld public final static org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableUISupport$FilterAvailable NEW
fld public final static org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableUISupport$FilterAvailable UPDATE
meth public static org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableUISupport$FilterAvailable valueOf(java.lang.String)
meth public static org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableUISupport$FilterAvailable[] values()
supr java.lang.Enum<org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableUISupport$FilterAvailable>

CLSS public final !enum org.netbeans.modules.j2ee.persistence.wizard.fromdb.UpdateType
fld public final static org.netbeans.modules.j2ee.persistence.wizard.fromdb.UpdateType NEW
fld public final static org.netbeans.modules.j2ee.persistence.wizard.fromdb.UpdateType RECREATE
fld public final static org.netbeans.modules.j2ee.persistence.wizard.fromdb.UpdateType UPDATE
meth public java.lang.String getName()
meth public static org.netbeans.modules.j2ee.persistence.wizard.fromdb.UpdateType valueOf(java.lang.String)
meth public static org.netbeans.modules.j2ee.persistence.wizard.fromdb.UpdateType[] values()
supr java.lang.Enum<org.netbeans.modules.j2ee.persistence.wizard.fromdb.UpdateType>
hfds name

CLSS public org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerGenerator
cons public init()
meth public static java.lang.String getRefOrMergeString(javax.lang.model.element.ExecutableElement,java.lang.String)
meth public static void generateJpaController(org.netbeans.api.project.Project,java.lang.String,java.lang.String,java.lang.String,org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil$EmbeddedPkSupport) throws java.io.IOException
supr java.lang.Object

CLSS public org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerIterator
cons public init()
fld public final static java.lang.String EXCEPTION_FOLDER_NAME = "exceptions"
intf org.openide.loaders.TemplateWizard$Iterator
meth public boolean hasNext()
meth public boolean hasPrevious()
meth public java.lang.String name()
meth public java.util.Set instantiate(org.openide.loaders.TemplateWizard) throws java.io.IOException
meth public org.openide.WizardDescriptor$Panel current()
meth public static int getProgressStepCount(int)
meth public static org.openide.filesystems.FileObject[] generateJpaControllers(org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.ProgressReporter,java.util.List<java.lang.String>,org.netbeans.api.project.Project,java.lang.String,org.openide.filesystems.FileObject,org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil$EmbeddedPkSupport,boolean) throws java.io.IOException
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void initialize(org.openide.loaders.TemplateWizard)
meth public void nextPanel()
meth public void previousPanel()
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void uninitialize(org.openide.loaders.TemplateWizard)
supr java.lang.Object
hfds EXCEPTION_CLASS_NAMES,RESOURCE_FOLDER,index,panels,puPanel
hcls ValidationPanel

CLSS public org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerSetupPanelVisual
cons public init(org.openide.WizardDescriptor)
intf javax.swing.event.DocumentListener
meth public java.lang.String getPackage()
meth public org.netbeans.api.project.SourceGroup getLocationValue()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void changedUpdate(javax.swing.event.DocumentEvent)
meth public void insertUpdate(javax.swing.event.DocumentEvent)
meth public void removeUpdate(javax.swing.event.DocumentEvent)
supr javax.swing.JPanel
hfds changeSupport,jLabel4,locationComboBox,locationLabel,packageComboBox,packageComboBoxEditor,packageLabel,project,projectLabel,projectTextField,wizard

CLSS public org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil
cons public init()
fld public final static int REL_NONE = 0
fld public final static int REL_TO_MANY = 2
fld public final static int REL_TO_ONE = 1
innr public static AnnotationInfo
innr public static EmbeddedPkSupport
innr public static MethodInfo
innr public static TreeMakerUtils
innr public static TypeInfo
meth public static boolean exceptionsThrownIncludes(org.netbeans.api.java.source.WorkingCopy,java.lang.String,java.lang.String,java.util.List<java.lang.String>,java.lang.String)
meth public static boolean haveId(javax.lang.model.element.TypeElement)
meth public static boolean isAnnotatedWith(javax.lang.model.element.Element,java.lang.String)
meth public static boolean isEmbeddableClass(javax.lang.model.element.TypeElement)
meth public static boolean isFieldAccess(javax.lang.model.element.TypeElement)
meth public static boolean isFieldOptionalAndNullable(javax.lang.model.element.ExecutableElement,boolean)
meth public static boolean isGenerated(javax.lang.model.element.ExecutableElement,boolean)
meth public static int isRelationship(javax.lang.model.element.ExecutableElement,boolean)
meth public static java.lang.String fieldFromClassName(java.lang.String)
meth public static java.lang.String findAnnotationValueAsString(javax.lang.model.element.AnnotationMirror,java.lang.String)
meth public static java.lang.String getAnnotationQualifiedName(javax.lang.model.element.AnnotationMirror)
meth public static java.lang.String getProjectEncodingAsString(org.netbeans.api.project.Project,org.openide.filesystems.FileObject)
meth public static java.lang.String getPropNameFromMethod(java.lang.String)
meth public static java.lang.String readResource(java.io.InputStream,java.lang.String) throws java.io.IOException
meth public static java.lang.String simpleClassName(java.lang.String)
meth public static java.nio.charset.Charset getProjectEncoding(org.netbeans.api.project.Project,org.openide.filesystems.FileObject)
meth public static java.util.List<java.lang.String> getExceptionsThrown(org.netbeans.api.java.source.WorkingCopy,java.lang.String,java.lang.String,java.util.List<java.lang.String>)
meth public static java.util.List<javax.lang.model.element.AnnotationMirror> findNestedAnnotations(javax.lang.model.element.AnnotationMirror,java.lang.String)
meth public static javax.lang.model.element.AnnotationMirror findAnnotation(javax.lang.model.element.Element,java.lang.String)
meth public static javax.lang.model.element.ExecutableElement getIdGetter(boolean,javax.lang.model.element.TypeElement)
meth public static javax.lang.model.element.ExecutableElement getOtherSideOfRelation(org.netbeans.api.java.source.CompilationController,javax.lang.model.element.ExecutableElement,boolean)
meth public static javax.lang.model.element.ExecutableElement[] getEntityMethods(javax.lang.model.element.TypeElement)
meth public static javax.lang.model.element.TypeElement getSuperclassTypeElement(javax.lang.model.element.TypeElement)
meth public static javax.lang.model.element.VariableElement guessField(javax.lang.model.element.ExecutableElement)
meth public static javax.lang.model.element.VariableElement guessGetter(javax.lang.model.element.ExecutableElement)
meth public static javax.lang.model.type.TypeMirror stripCollection(javax.lang.model.type.TypeMirror,javax.lang.model.util.Types)
meth public static void createFile(org.openide.filesystems.FileObject,java.lang.String,java.lang.String) throws java.io.IOException
supr java.lang.Object
hcls EmbeddedPkSupportInfo

CLSS public static org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil$AnnotationInfo
 outer org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String[],java.lang.Object[])
meth public java.lang.Object[] getArgValues()
meth public java.lang.String getType()
meth public java.lang.String[] getArgNames()
supr java.lang.Object
hfds argNames,argValues,type

CLSS public static org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil$EmbeddedPkSupport
 outer org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil
cons public init()
meth public boolean getPkSetterMethodExist(javax.lang.model.element.TypeElement,javax.lang.model.element.ExecutableElement)
meth public boolean isRedundantWithPkFields(javax.lang.model.element.TypeElement,javax.lang.model.element.ExecutableElement)
meth public boolean isRedundantWithRelationshipField(javax.lang.model.element.TypeElement,javax.lang.model.element.ExecutableElement)
meth public java.lang.String getCodeToPopulatePkField(javax.lang.model.element.TypeElement,javax.lang.model.element.ExecutableElement)
meth public java.util.Set<javax.lang.model.element.ExecutableElement> getPkAccessorMethods(javax.lang.model.element.TypeElement)
supr java.lang.Object
hfds typeToInfo

CLSS public static org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil$MethodInfo
 outer org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil
cons public init(java.lang.String,int,java.lang.String,java.lang.String[],java.lang.String[],java.lang.String[],java.lang.String,org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil$AnnotationInfo[],java.lang.String)
cons public init(java.lang.String,int,org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil$TypeInfo,org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil$TypeInfo[],org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil$TypeInfo[],java.lang.String[],java.lang.String,org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil$AnnotationInfo[],java.lang.String)
meth public int getModifiers()
meth public java.lang.String getCommentText()
meth public java.lang.String getMethodBodyText()
meth public java.lang.String getName()
meth public java.lang.String[] getParameterNames()
meth public org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil$AnnotationInfo[] getAnnotations()
meth public org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil$TypeInfo getReturnType()
meth public org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil$TypeInfo[] getExceptionTypes()
meth public org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil$TypeInfo[] getParameterTypes()
supr java.lang.Object
hfds annotations,commentText,exceptionTypes,methodBodyText,modifiers,name,parameterNames,parameterTypes,returnType

CLSS public static org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil$TreeMakerUtils
 outer org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil
cons public init()
meth public static com.sun.source.tree.ClassTree addMethod(com.sun.source.tree.ClassTree,org.netbeans.api.java.source.WorkingCopy,org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil$MethodInfo)
meth public static com.sun.source.tree.ClassTree addVariable(com.sun.source.tree.ClassTree,org.netbeans.api.java.source.WorkingCopy,java.lang.String,java.lang.String,int,java.lang.Object,org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil$AnnotationInfo[])
meth public static com.sun.source.tree.ClassTree addVariable(com.sun.source.tree.ClassTree,org.netbeans.api.java.source.WorkingCopy,java.lang.String,org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil$TypeInfo,int,java.lang.Object,org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil$AnnotationInfo[])
meth public static com.sun.source.tree.ClassTree modifyDefaultConstructor(com.sun.source.tree.ClassTree,com.sun.source.tree.ClassTree,org.netbeans.api.java.source.WorkingCopy,org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil$MethodInfo)
meth public static com.sun.source.tree.CompilationUnitTree createImport(org.netbeans.api.java.source.WorkingCopy,com.sun.source.tree.CompilationUnitTree,java.lang.String)
supr java.lang.Object

CLSS public static org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil$TypeInfo
 outer org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String[])
cons public init(java.lang.String,org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil$TypeInfo[])
meth public java.lang.String getRawType()
meth public org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil$TypeInfo[] getDeclaredTypeParameters()
meth public static org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil$TypeInfo[] fromStrings(java.lang.String[])
supr java.lang.Object
hfds declaredTypeParameters,rawType

CLSS public abstract interface org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.ProgressReporter
meth public abstract void progress(java.lang.String,int)

CLSS public org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.ProgressReporterDelegate
cons public init(org.netbeans.api.progress.aggregate.ProgressContributor,org.netbeans.modules.j2ee.persistence.wizard.fromdb.ProgressPanel)
intf org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.ProgressReporter
meth public void progress(java.lang.String,int)
supr java.lang.Object
hfds contributor,panel

CLSS public org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.WizardProperties
cons public init()
fld public final static java.lang.String CONFIG_FILE = "ConfigFile"
fld public final static java.lang.String DESCRIPTION = "Description"
fld public final static java.lang.String ENTITY_CLASS = "EntityClass"
fld public final static java.lang.String JPA_CONTROLLER_PACKAGE = "JpaControllerPackage"
fld public final static java.lang.String SCOPE = "Scope"
supr java.lang.Object

CLSS public org.netbeans.modules.j2ee.persistence.wizard.library.J2SEVolumeCustomizer
intf java.beans.Customizer
meth public void addNotify()
meth public void setEnabled(boolean)
meth public void setObject(java.lang.Object)
supr javax.swing.JPanel
hfds addButton,addURLButton,content,downButton,impl,jScrollPane1,lastFolder,message,model,removeButton,upButton,volumeType
hcls ContentRenderer,SimpleFileFilter

CLSS public org.netbeans.modules.j2ee.persistence.wizard.library.PersistenceLibraryCustomizer
meth public static boolean showCustomizer()
supr java.lang.Object

CLSS public org.netbeans.modules.j2ee.persistence.wizard.library.PersistenceLibraryPanel
cons public init(org.netbeans.spi.project.libraries.LibraryImplementation)
fld public final static java.lang.String IS_VALID = "PersistenceLibraryPanel_isValid"
meth public void addNotify()
meth public void apply()
supr javax.swing.JPanel
hfds ERROR_GIF,WARNING_GIF,errorMessage,jLabel1,libImpl,libraryNameTextField,nbErrorForeground,nbWarningForeground,tabbedPane

CLSS public org.netbeans.modules.j2ee.persistence.wizard.library.PersistenceLibrarySupport
fld public final static java.lang.String LIBRARY_TYPE = "j2se"
fld public final static java.lang.String VOLUME_TYPE_CLASSPATH = "classpath"
fld public final static java.lang.String VOLUME_TYPE_JAVADOC = "javadoc"
fld public final static java.lang.String VOLUME_TYPE_SRC = "src"
meth public static boolean containsClass(org.netbeans.spi.project.libraries.LibraryImplementation,java.lang.String)
meth public static boolean containsService(org.netbeans.api.project.libraries.Library,java.lang.String)
meth public static boolean containsService(org.netbeans.spi.project.libraries.LibraryImplementation,java.lang.String)
meth public static boolean isValidLibraryJavadocRoot(java.net.URL)
meth public static java.util.List<org.netbeans.modules.j2ee.persistence.provider.Provider> getProvidersFromLibraries()
meth public static org.netbeans.api.project.libraries.Library getFirstProviderLibrary()
meth public static org.netbeans.api.project.libraries.Library getLibrary(org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit)
meth public static org.netbeans.api.project.libraries.Library getLibrary(org.netbeans.modules.j2ee.persistence.provider.Provider)
meth public static org.netbeans.modules.j2ee.persistence.wizard.library.PersistenceLibrarySupport getDefault()
meth public static void addDriver(org.netbeans.api.project.Project,org.netbeans.api.db.explorer.JDBCDriver)
meth public void addLibrary(org.netbeans.spi.project.libraries.LibraryImplementation)
supr java.lang.Object
hfds LIBRARIES_REPOSITORY,MAX_DEPTH,VOLUME_TYPES,instance,storage
hcls ProviderLibrary

CLSS public org.netbeans.modules.j2ee.persistence.wizard.unit.JdbcListCellRenderer
cons public init()
meth public java.awt.Component getListCellRendererComponent(javax.swing.JList,java.lang.Object,int,boolean,boolean)
supr javax.swing.DefaultListCellRenderer

CLSS public org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizard
cons public init()
intf org.openide.WizardDescriptor$ProgressInstantiatingIterator
meth public boolean hasNext()
meth public boolean hasPrevious()
meth public java.lang.String name()
meth public java.util.Set instantiate() throws java.io.IOException
meth public java.util.Set instantiate(org.netbeans.api.progress.ProgressHandle) throws java.io.IOException
meth public org.openide.WizardDescriptor$Panel current()
meth public static org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizard create()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void initialize(org.openide.WizardDescriptor)
meth public void nextPanel()
meth public void previousPanel()
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void uninitialize(org.openide.WizardDescriptor)
supr java.lang.Object
hfds LOG,descriptor,index,panels,project

CLSS public org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardDescriptor
cons public init(org.netbeans.api.project.Project)
intf javax.swing.event.ChangeListener
intf org.openide.WizardDescriptor$FinishablePanel
meth public boolean isContainerManaged()
meth public boolean isFinishPanel()
meth public boolean isJTA()
meth public boolean isValid()
meth public java.awt.Component getComponent()
meth public java.lang.String getDBResourceSelection()
meth public java.lang.String getDatasource()
meth public java.lang.String getNonDefaultProvider()
meth public java.lang.String getPersistenceUnitName()
meth public java.lang.String getTableGeneration()
meth public org.netbeans.api.db.explorer.DatabaseConnection getPersistenceConnection()
meth public org.netbeans.modules.j2ee.persistence.provider.Provider getSelectedProvider()
meth public org.openide.util.HelpCtx getHelp()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void readSettings(java.lang.Object)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void stateChanged(javax.swing.event.ChangeEvent)
meth public void storeSettings(java.lang.Object)
supr java.lang.Object
hfds ERROR_MSG_KEY,changeSupport,datasourcePanel,isContainerManaged,jdbcPanel,panel,project,wizardDescriptor

CLSS public abstract org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel
cons protected init(org.netbeans.api.project.Project)
fld protected final org.netbeans.api.project.Project project
fld public final static java.lang.String IS_VALID = "PersistenceUnitWizardPanel_isValid"
innr public final static !enum TableGeneration
meth public abstract boolean isValidPanel()
meth public abstract java.lang.String getPersistenceUnitName()
meth public abstract java.lang.String getTableGeneration()
meth public abstract org.netbeans.modules.j2ee.persistence.provider.Provider getSelectedProvider()
meth public abstract void setErrorMessage(java.lang.String)
meth public abstract void setPreselectedDB(java.lang.String)
meth public final boolean isNameUnique() throws org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException
supr javax.swing.JPanel
hfds LOG

CLSS public final static !enum org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel$TableGeneration
 outer org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel
fld public final static org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel$TableGeneration CREATE
fld public final static org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel$TableGeneration DROP_CREATE
fld public final static org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel$TableGeneration NONE
meth public static org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel$TableGeneration valueOf(java.lang.String)
meth public static org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel$TableGeneration[] values()
supr java.lang.Enum<org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel$TableGeneration>

CLSS public org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanelDS
cons public init(org.netbeans.api.project.Project,javax.swing.event.ChangeListener,boolean)
cons public init(org.netbeans.api.project.Project,javax.swing.event.ChangeListener,boolean,org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel$TableGeneration)
intf javax.swing.event.AncestorListener
meth public boolean isJTA()
meth public boolean isNonDefaultProviderEnabled()
meth public boolean isValidPanel()
meth public java.lang.String getDatasource()
meth public java.lang.String getNonDefaultProvider()
meth public java.lang.String getPersistenceUnitName()
meth public java.lang.String getTableGeneration()
meth public org.netbeans.modules.j2ee.persistence.provider.Provider getSelectedProvider()
meth public void ancestorAdded(javax.swing.event.AncestorEvent)
meth public void ancestorMoved(javax.swing.event.AncestorEvent)
meth public void ancestorRemoved(javax.swing.event.AncestorEvent)
meth public void setErrorMessage(java.lang.String)
meth public void setPreselectedDB(java.lang.String)
supr org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel
hfds RP,buttonGroup1,createPUWarningLabel,datasourceLabel,ddlCreate,ddlDropCreate,ddlUnkown,dsCombo,errorMessage,jLabel1,jLabel2,jtaCheckBox,persistenceProviderLabel,providerCombo,tableCreationButtonGroup,unitNameLabel,unitNameTextField,warnPanel
hcls ShyLabel,ValidationListener

CLSS public org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanelJdbc
cons public init(org.netbeans.api.project.Project,javax.swing.event.ChangeListener,boolean)
cons public init(org.netbeans.api.project.Project,javax.swing.event.ChangeListener,boolean,org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel$TableGeneration)
meth public boolean isValidPanel()
meth public java.lang.String getPersistenceUnitName()
meth public java.lang.String getTableGeneration()
meth public org.netbeans.api.db.explorer.DatabaseConnection getPersistenceConnection()
meth public org.netbeans.modules.j2ee.persistence.provider.Provider getSelectedProvider()
meth public void setErrorMessage(java.lang.String)
meth public void setPreselectedDB(java.lang.String)
supr org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel
hfds RP,buttonGroup1,createPUWarningLabel,ddlCreate,ddlDropCreate,ddlUnkown,errorMessage,jLabel1,jLabel2,jdbcCombo,jdbcLabel,libraryCombo,libraryLabel,tableCreationButtonGroup,unitNameLabel,unitNameTextField,warnPanel
hcls ShyLabel,ValidationListener

CLSS public abstract interface org.netbeans.modules.xml.catalog.spi.CatalogDescriptor2
intf org.netbeans.modules.xml.catalog.spi.CatalogDescriptorBase
meth public abstract java.lang.String getIconResource(int)

CLSS public abstract interface org.netbeans.modules.xml.catalog.spi.CatalogDescriptorBase
fld public final static java.lang.String PROP_CATALOG_DESC = "ca-desc"
fld public final static java.lang.String PROP_CATALOG_ICON = "ca-icon"
fld public final static java.lang.String PROP_CATALOG_NAME = "ca-name"
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getShortDescription()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public abstract interface org.netbeans.modules.xml.catalog.spi.CatalogReader
meth public abstract java.lang.String getSystemID(java.lang.String)
meth public abstract java.lang.String resolvePublic(java.lang.String)
meth public abstract java.lang.String resolveURI(java.lang.String)
meth public abstract java.util.Iterator getPublicIDs()
meth public abstract void addCatalogListener(org.netbeans.modules.xml.catalog.spi.CatalogListener)
meth public abstract void refresh()
meth public abstract void removeCatalogListener(org.netbeans.modules.xml.catalog.spi.CatalogListener)

CLSS public abstract org.netbeans.modules.xml.multiview.AbstractMultiViewElement
cons protected init()
cons protected init(org.netbeans.modules.xml.multiview.XmlMultiViewDataObject)
fld protected org.netbeans.core.spi.multiview.MultiViewElementCallback callback
fld protected org.netbeans.modules.xml.multiview.XmlMultiViewDataObject dObj
intf java.io.Serializable
intf org.netbeans.core.spi.multiview.MultiViewElement
meth public javax.swing.Action[] getActions()
meth public org.netbeans.core.spi.multiview.CloseOperationState canCloseElement()
meth public org.openide.awt.UndoRedo getUndoRedo()
meth public void componentClosed()
meth public void componentOpened()
meth public void setMultiViewCallback(org.netbeans.core.spi.multiview.MultiViewElementCallback)
supr java.lang.Object
hfds LOGGER,serialVersionUID
hcls DiscardAction,SaveAction

CLSS public abstract org.netbeans.modules.xml.multiview.ToolBarMultiViewElement
cons public init(org.netbeans.modules.xml.multiview.XmlMultiViewDataObject)
meth protected void setVisualEditor(org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor)
meth public abstract org.netbeans.modules.xml.multiview.ui.SectionView getSectionView()
meth public javax.swing.JComponent getToolbarRepresentation()
meth public javax.swing.JComponent getVisualRepresentation()
meth public org.netbeans.core.spi.multiview.CloseOperationState canCloseElement()
meth public org.openide.util.Lookup getLookup()
meth public void componentActivated()
meth public void componentClosed()
meth public void componentDeactivated()
meth public void componentHidden()
meth public void componentOpened()
meth public void componentShowing()
supr org.netbeans.modules.xml.multiview.AbstractMultiViewElement
hfds editor,listener

CLSS public abstract org.netbeans.modules.xml.multiview.XmlMultiViewDataObject
cons public init(org.openide.filesystems.FileObject,org.openide.loaders.MultiFileLoader) throws org.openide.loaders.DataObjectExistsException
fld protected org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport editorSupport
fld public final static java.lang.String PROPERTY_DATA_MODIFIED = "data modified"
fld public final static java.lang.String PROPERTY_DATA_UPDATED = "data changed"
fld public final static java.lang.String PROP_DOCUMENT_VALID = "document_valid"
fld public final static java.lang.String PROP_SAX_ERROR = "sax_error"
innr public DataCache
intf org.openide.nodes.CookieSet$Factory
meth protected abstract java.lang.String getPrefixMark()
meth protected boolean verifyDocumentBeforeClose()
meth protected int getXMLMultiViewIndex()
meth protected java.awt.Image getXmlViewIcon()
meth protected java.lang.String getEditorMimeType()
meth protected org.netbeans.core.spi.multiview.MultiViewElement getActiveMultiViewElement()
meth protected org.netbeans.modules.xml.multiview.DesignMultiViewDesc[] getMultiViewDesc()
meth protected org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport getEditorSupport()
meth protected org.openide.cookies.EditorCookie createEditorCookie()
meth protected org.openide.filesystems.FileObject handleRename(java.lang.String) throws java.io.IOException
meth protected void setSaxError(org.xml.sax.SAXException)
meth public boolean canClose()
meth public org.netbeans.core.api.multiview.MultiViewPerspective getSelectedPerspective()
meth public org.netbeans.modules.xml.multiview.EncodingHelper getEncodingHelper()
meth public org.netbeans.modules.xml.multiview.XmlMultiViewDataObject$DataCache getDataCache()
meth public org.openide.filesystems.FileLock waitForLock() throws java.io.IOException
meth public org.openide.filesystems.FileLock waitForLock(long) throws java.io.IOException
meth public org.openide.nodes.Node$Cookie createCookie(java.lang.Class)
meth public org.openide.util.Lookup getLookup()
meth public org.xml.sax.SAXException getSaxError()
meth public void goToXmlView()
meth public void openView(int)
meth public void setLastOpenView(int)
meth public void setModified(boolean)
meth public void showElement(java.lang.Object)
supr org.openide.loaders.MultiDataObject
hfds activeMVElement,dataCache,encodingHelper,lockReference,saveCookie,saxError,timeStamp

CLSS public abstract interface org.netbeans.modules.xml.multiview.cookies.ErrorLocator
meth public abstract javax.swing.JComponent getErrorComponent(java.lang.String)

CLSS public abstract interface org.netbeans.modules.xml.multiview.cookies.LinkCookie
meth public abstract void linkButtonPressed(java.lang.Object,java.lang.String)

CLSS public org.netbeans.modules.xml.multiview.ui.DefaultTablePanel
cons public init(javax.swing.table.AbstractTableModel)
cons public init(javax.swing.table.AbstractTableModel,boolean)
fld protected javax.swing.JButton addButton
fld protected javax.swing.JButton editButton
fld protected javax.swing.JButton moveDownButton
fld protected javax.swing.JButton moveUpButton
fld protected javax.swing.JButton removeButton
fld protected javax.swing.JButton sourceButton
meth public boolean isReordable()
meth public javax.swing.JTable getTable()
meth public javax.swing.table.AbstractTableModel getModel()
meth public void setButtons(boolean,boolean,boolean)
meth public void setButtons(boolean,boolean,boolean,boolean,boolean,boolean)
meth public void setSelectedRow(int)
meth public void setTitle(java.lang.String)
supr javax.swing.JPanel
hfds buttonPanel,jPanel1,jTable1,model,reordable

CLSS public abstract interface org.netbeans.modules.xml.multiview.ui.InnerPanelFactory
meth public abstract org.netbeans.modules.xml.multiview.ui.SectionInnerPanel createInnerPanel(java.lang.Object)

CLSS public abstract org.netbeans.modules.xml.multiview.ui.SectionInnerPanel
cons public init(org.netbeans.modules.xml.multiview.ui.SectionView)
intf org.netbeans.modules.xml.multiview.cookies.ErrorLocator
intf org.netbeans.modules.xml.multiview.cookies.LinkCookie
meth protected void addRefreshable(org.netbeans.modules.xml.multiview.Refreshable)
meth protected void endUIChange()
meth protected void scheduleRefreshView()
meth protected void signalUIChange()
 anno 0 java.lang.Deprecated()
meth protected void startUIChange()
meth public abstract void setValue(javax.swing.JComponent,java.lang.Object)
meth public boolean canClose()
meth public final void addImmediateModifier(javax.swing.JCheckBox)
meth public final void addImmediateModifier(javax.swing.JComboBox)
meth public final void addImmediateModifier(javax.swing.JRadioButton)
meth public final void addImmediateModifier(javax.swing.text.JTextComponent)
meth public final void addModifier(javax.swing.JCheckBox)
meth public final void addModifier(javax.swing.JComboBox)
meth public final void addModifier(javax.swing.JComboBox,boolean)
meth public final void addModifier(javax.swing.JRadioButton)
meth public final void addModifier(javax.swing.text.JTextComponent)
meth public final void addModifier(javax.swing.text.JTextComponent,boolean)
meth public final void addValidatee(javax.swing.text.JTextComponent)
meth public org.netbeans.modules.xml.multiview.ui.SectionView getSectionView()
meth public void addFocusListener(java.awt.event.FocusListener)
meth public void dataModelPropertyChange(java.lang.Object,java.lang.String,java.lang.Object,java.lang.Object)
meth public void documentChanged(javax.swing.text.JTextComponent,java.lang.String)
meth public void refreshView()
meth public void rollbackValue(javax.swing.text.JTextComponent)
supr javax.swing.JPanel
hfds REFRESH_DELAY,RP,activeListener,closing,localFocusListener,localFocusListenerInitialized,refreshTask,refreshableList,sectionView
hcls CheckBoxActionListener,CheckBoxModifyFocusListener,ComboBoxActionListener,ComboBoxModifyFocusListener,FlushActionListener,FlushFocusListener,ModifyFocusListener,RadioButtonActionListener,RadioButtonModifyFocusListener,TextListener,ValidateFocusListener

CLSS public abstract interface org.netbeans.spi.editor.codegen.CodeGenerator
innr public abstract interface static Factory
meth public abstract java.lang.String getDisplayName()
meth public abstract void invoke()

CLSS public abstract interface static org.netbeans.spi.editor.codegen.CodeGenerator$Factory
 outer org.netbeans.spi.editor.codegen.CodeGenerator
 anno 0 org.netbeans.spi.editor.mimelookup.MimeLocation(java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass=class org.netbeans.spi.editor.mimelookup.InstanceProvider, java.lang.String subfolderName="CodeGenerators")
meth public abstract java.util.List<? extends org.netbeans.spi.editor.codegen.CodeGenerator> create(org.openide.util.Lookup)

CLSS public abstract interface !annotation org.netbeans.spi.editor.mimelookup.MimeLocation
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass()
meth public abstract java.lang.String subfolderName()

CLSS public org.openide.DialogDescriptor
cons public init(java.lang.Object,java.lang.String)
cons public init(java.lang.Object,java.lang.String,boolean,int,java.lang.Object,int,org.openide.util.HelpCtx,java.awt.event.ActionListener)
cons public init(java.lang.Object,java.lang.String,boolean,int,java.lang.Object,java.awt.event.ActionListener)
cons public init(java.lang.Object,java.lang.String,boolean,java.awt.event.ActionListener)
cons public init(java.lang.Object,java.lang.String,boolean,java.lang.Object[],java.lang.Object,int,org.openide.util.HelpCtx,java.awt.event.ActionListener)
cons public init(java.lang.Object,java.lang.String,boolean,java.lang.Object[],java.lang.Object,int,org.openide.util.HelpCtx,java.awt.event.ActionListener,boolean)
fld public final static int BOTTOM_ALIGN = 0
fld public final static int DEFAULT_ALIGN = 0
fld public final static int RIGHT_ALIGN = 1
fld public final static java.lang.String PROP_BUTTON_LISTENER = "buttonListener"
fld public final static java.lang.String PROP_CLOSING_OPTIONS = "closingOptions"
fld public final static java.lang.String PROP_HELP_CTX = "helpCtx"
fld public final static java.lang.String PROP_LEAF = "leaf"
fld public final static java.lang.String PROP_MODAL = "modal"
fld public final static java.lang.String PROP_OPTIONS_ALIGN = "optionsAlign"
intf org.openide.util.HelpCtx$Provider
meth public boolean isLeaf()
meth public boolean isModal()
meth public int getOptionsAlign()
meth public java.awt.event.ActionListener getButtonListener()
meth public java.lang.Object[] getClosingOptions()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void setButtonListener(java.awt.event.ActionListener)
meth public void setClosingOptions(java.lang.Object[])
meth public void setHelpCtx(org.openide.util.HelpCtx)
meth public void setLeaf(boolean)
meth public void setModal(boolean)
meth public void setOptionsAlign(int)
supr org.openide.NotifyDescriptor
hfds DEFAULT_CLOSING_OPTIONS,buttonListener,closingOptions,helpCtx,leaf,modal,optionsAlign

CLSS public org.openide.NotifyDescriptor
cons public init(java.lang.Object,java.lang.String,int,int,java.lang.Object[],java.lang.Object)
fld public final static int DEFAULT_OPTION = -1
fld public final static int ERROR_MESSAGE = 0
fld public final static int INFORMATION_MESSAGE = 1
fld public final static int OK_CANCEL_OPTION = 2
fld public final static int PLAIN_MESSAGE = -1
fld public final static int QUESTION_MESSAGE = 3
fld public final static int WARNING_MESSAGE = 2
fld public final static int YES_NO_CANCEL_OPTION = 1
fld public final static int YES_NO_OPTION = 0
fld public final static java.lang.Object CANCEL_OPTION
fld public final static java.lang.Object CLOSED_OPTION
fld public final static java.lang.Object NO_OPTION
fld public final static java.lang.Object OK_OPTION
fld public final static java.lang.Object YES_OPTION
fld public final static java.lang.String PROP_DETAIL = "detail"
fld public final static java.lang.String PROP_ERROR_NOTIFICATION = "errorNotification"
fld public final static java.lang.String PROP_INFO_NOTIFICATION = "infoNotification"
fld public final static java.lang.String PROP_MESSAGE = "message"
fld public final static java.lang.String PROP_MESSAGE_TYPE = "messageType"
fld public final static java.lang.String PROP_NO_DEFAULT_CLOSE = "noDefaultClose"
fld public final static java.lang.String PROP_OPTIONS = "options"
fld public final static java.lang.String PROP_OPTION_TYPE = "optionType"
fld public final static java.lang.String PROP_TITLE = "title"
fld public final static java.lang.String PROP_VALID = "valid"
fld public final static java.lang.String PROP_VALUE = "value"
fld public final static java.lang.String PROP_WARNING_NOTIFICATION = "warningNotification"
innr public final static ComposedInput
innr public final static Exception
innr public final static PasswordLine
innr public final static QuickPick
innr public static Confirmation
innr public static InputLine
innr public static Message
meth protected static java.lang.String getTitleForType(int)
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void initialize()
meth public boolean isNoDefaultClose()
meth public final boolean isValid()
meth public final org.openide.NotificationLineSupport createNotificationLineSupport()
meth public final org.openide.NotificationLineSupport getNotificationLineSupport()
meth public final void setValid(boolean)
meth public int getMessageType()
meth public int getOptionType()
meth public java.lang.Object getDefaultValue()
meth public java.lang.Object getMessage()
meth public java.lang.Object getValue()
meth public java.lang.Object[] getAdditionalOptions()
meth public java.lang.Object[] getOptions()
meth public java.lang.String getTitle()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setAdditionalOptions(java.lang.Object[])
meth public void setMessage(java.lang.Object)
meth public void setMessageType(int)
meth public void setNoDefaultClose(boolean)
meth public void setOptionType(int)
meth public void setOptions(java.lang.Object[])
meth public void setTitle(java.lang.String)
meth public void setValue(java.lang.Object)
supr java.lang.Object
hfds MAXIMUM_TEXT_WIDTH,SIZE_PREFERRED_HEIGHT,SIZE_PREFERRED_WIDTH,adOptions,changeSupport,defaultValue,errMsg,infoMsg,message,messageType,noDefaultClose,notificationLineSupport,optionType,options,title,valid,value,warnMsg

CLSS public org.openide.WizardDescriptor
cons protected init()
cons public <%0 extends java.lang.Object> init(org.openide.WizardDescriptor$Iterator<{%%0}>,{%%0})
cons public <%0 extends java.lang.Object> init(org.openide.WizardDescriptor$Panel<{%%0}>[],{%%0})
cons public init(org.openide.WizardDescriptor$Iterator<org.openide.WizardDescriptor>)
cons public init(org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor>[])
fld public final static java.lang.Object FINISH_OPTION
fld public final static java.lang.Object NEXT_OPTION
fld public final static java.lang.Object PREVIOUS_OPTION
fld public final static java.lang.String PROP_AUTO_WIZARD_STYLE = "WizardPanel_autoWizardStyle"
fld public final static java.lang.String PROP_CONTENT_BACK_COLOR = "WizardPanel_contentBackColor"
fld public final static java.lang.String PROP_CONTENT_DATA = "WizardPanel_contentData"
fld public final static java.lang.String PROP_CONTENT_DISPLAYED = "WizardPanel_contentDisplayed"
fld public final static java.lang.String PROP_CONTENT_FOREGROUND_COLOR = "WizardPanel_contentForegroundColor"
fld public final static java.lang.String PROP_CONTENT_NUMBERED = "WizardPanel_contentNumbered"
fld public final static java.lang.String PROP_CONTENT_SELECTED_INDEX = "WizardPanel_contentSelectedIndex"
fld public final static java.lang.String PROP_ERROR_MESSAGE = "WizardPanel_errorMessage"
fld public final static java.lang.String PROP_HELP_DISPLAYED = "WizardPanel_helpDisplayed"
fld public final static java.lang.String PROP_HELP_URL = "WizardPanel_helpURL"
fld public final static java.lang.String PROP_IMAGE = "WizardPanel_image"
fld public final static java.lang.String PROP_IMAGE_ALIGNMENT = "WizardPanel_imageAlignment"
fld public final static java.lang.String PROP_INFO_MESSAGE = "WizardPanel_infoMessage"
fld public final static java.lang.String PROP_LEFT_DIMENSION = "WizardPanel_leftDimension"
fld public final static java.lang.String PROP_WARNING_MESSAGE = "WizardPanel_warningMessage"
innr public abstract interface static AsynchronousInstantiatingIterator
innr public abstract interface static AsynchronousValidatingPanel
innr public abstract interface static BackgroundInstantiatingIterator
innr public abstract interface static ExtendedAsynchronousValidatingPanel
innr public abstract interface static FinishPanel
innr public abstract interface static FinishablePanel
innr public abstract interface static InstantiatingIterator
innr public abstract interface static Iterator
innr public abstract interface static Panel
innr public abstract interface static ProgressInstantiatingIterator
innr public abstract interface static ValidatingPanel
innr public static ArrayIterator
meth protected void initialize()
meth protected void updateState()
meth public final <%0 extends java.lang.Object> void setPanelsAndSettings(org.openide.WizardDescriptor$Iterator<{%%0}>,{%%0})
meth public final void doCancelClick()
meth public final void doFinishClick()
meth public final void doNextClick()
meth public final void doPreviousClick()
meth public final void setPanels(org.openide.WizardDescriptor$Iterator)
 anno 0 java.lang.Deprecated()
meth public java.lang.Object getProperty(java.lang.String)
meth public java.lang.Object getValue()
meth public java.text.MessageFormat getTitleFormat()
meth public java.util.Map<java.lang.String,java.lang.Object> getProperties()
meth public java.util.Set getInstantiatedObjects()
meth public void putProperty(java.lang.String,java.lang.Object)
meth public void setAdditionalOptions(java.lang.Object[])
meth public void setClosingOptions(java.lang.Object[])
meth public void setHelpCtx(org.openide.util.HelpCtx)
meth public void setOptions(java.lang.Object[])
meth public void setTitleFormat(java.text.MessageFormat)
meth public void setValue(java.lang.Object)
supr org.openide.DialogDescriptor
hfds ASYNCHRONOUS_JOBS_RP,CLOSE_PREVENTER,PROGRESS_BAR_DISPLAY_NAME,addedWindowListener,autoWizardStyle,backgroundValidationTask,baseListener,bundle,cancelButton,changeStateInProgress,contentBackColor,contentData,contentForegroundColor,contentSelectedIndex,currentPanelWasChangedWhileStoreSettings,data,err,escapeActionListener,finishButton,finishOption,handle,helpURL,image,imageAlignment,init,initialized,isWizardWideHelpSet,logged,newObjects,nextButton,previousButton,propListener,properties,titleFormat,validationRuns,waitingComponent,weakCancelButtonListener,weakChangeListener,weakFinishButtonListener,weakNextButtonListener,weakPreviousButtonListener,weakPropertyChangeListener,wizardPanel
hcls BoundedHtmlBrowser,EmptyPanel,FinishAction,FixedHeightLabel,FixedHeightPane,ImagedPanel,Listener,PropL,SettingsAndIterator,WizardPanel,WrappedCellRenderer

CLSS public abstract interface static org.openide.WizardDescriptor$AsynchronousInstantiatingIterator<%0 extends java.lang.Object>
 outer org.openide.WizardDescriptor
intf org.openide.WizardDescriptor$InstantiatingIterator<{org.openide.WizardDescriptor$AsynchronousInstantiatingIterator%0}>
meth public abstract java.util.Set instantiate() throws java.io.IOException

CLSS public abstract interface static org.openide.WizardDescriptor$FinishablePanel<%0 extends java.lang.Object>
 outer org.openide.WizardDescriptor
intf org.openide.WizardDescriptor$Panel<{org.openide.WizardDescriptor$FinishablePanel%0}>
meth public abstract boolean isFinishPanel()

CLSS public abstract interface static org.openide.WizardDescriptor$InstantiatingIterator<%0 extends java.lang.Object>
 outer org.openide.WizardDescriptor
intf org.openide.WizardDescriptor$Iterator<{org.openide.WizardDescriptor$InstantiatingIterator%0}>
meth public abstract java.util.Set instantiate() throws java.io.IOException
meth public abstract void initialize(org.openide.WizardDescriptor)
meth public abstract void uninitialize(org.openide.WizardDescriptor)

CLSS public abstract interface static org.openide.WizardDescriptor$Iterator<%0 extends java.lang.Object>
 outer org.openide.WizardDescriptor
meth public abstract boolean hasNext()
meth public abstract boolean hasPrevious()
meth public abstract java.lang.String name()
meth public abstract org.openide.WizardDescriptor$Panel<{org.openide.WizardDescriptor$Iterator%0}> current()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void nextPanel()
meth public abstract void previousPanel()
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public abstract interface static org.openide.WizardDescriptor$Panel<%0 extends java.lang.Object>
 outer org.openide.WizardDescriptor
meth public abstract boolean isValid()
meth public abstract java.awt.Component getComponent()
meth public abstract org.openide.util.HelpCtx getHelp()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void readSettings({org.openide.WizardDescriptor$Panel%0})
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public abstract void storeSettings({org.openide.WizardDescriptor$Panel%0})

CLSS public abstract interface static org.openide.WizardDescriptor$ProgressInstantiatingIterator<%0 extends java.lang.Object>
 outer org.openide.WizardDescriptor
intf org.openide.WizardDescriptor$AsynchronousInstantiatingIterator<{org.openide.WizardDescriptor$ProgressInstantiatingIterator%0}>
meth public abstract java.util.Set instantiate(org.netbeans.api.progress.ProgressHandle) throws java.io.IOException

CLSS public abstract org.openide.loaders.DataLoader
cons protected init(java.lang.Class<? extends org.openide.loaders.DataObject>)
 anno 0 java.lang.Deprecated()
cons protected init(java.lang.String)
fld public final static java.lang.String PROP_ACTIONS = "actions"
fld public final static java.lang.String PROP_DISPLAY_NAME = "displayName"
innr public abstract interface static RecognizedFiles
intf org.openide.loaders.DataObject$Factory
meth protected abstract org.openide.loaders.DataObject handleFindDataObject(org.openide.filesystems.FileObject,org.openide.loaders.DataLoader$RecognizedFiles) throws java.io.IOException
meth protected boolean clearSharedData()
meth protected final void setDisplayName(java.lang.String)
meth protected java.lang.String actionsContext()
meth protected java.lang.String defaultDisplayName()
meth protected org.openide.util.actions.SystemAction[] defaultActions()
 anno 0 java.lang.Deprecated()
meth public final java.lang.Class<? extends org.openide.loaders.DataObject> getRepresentationClass()
meth public final java.lang.String getDisplayName()
meth public final java.lang.String getRepresentationClassName()
meth public final org.openide.loaders.DataObject findDataObject(org.openide.filesystems.FileObject,java.util.Set<? super org.openide.filesystems.FileObject>) throws java.io.IOException
meth public final org.openide.loaders.DataObject findDataObject(org.openide.filesystems.FileObject,org.openide.loaders.DataLoader$RecognizedFiles) throws java.io.IOException
meth public final org.openide.util.actions.SystemAction[] getActions()
meth public final void markFile(org.openide.filesystems.FileObject) throws java.io.IOException
meth public final void setActions(org.openide.util.actions.SystemAction[])
meth public static <%0 extends org.openide.loaders.DataLoader> {%%0} getLoader(java.lang.Class<{%%0}>)
meth public void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr org.openide.util.SharedClassObject
hfds ACTION_MANAGER,ERR,LOADER_VERSION,PROP_DEF_ACTIONS,PROP_REPRESENTATION_CLASS,PROP_REPRESENTATION_CLASS_NAME,serialVersionUID

CLSS public org.openide.loaders.DataNode
cons public init(org.openide.loaders.DataObject,org.openide.nodes.Children)
cons public init(org.openide.loaders.DataObject,org.openide.nodes.Children,org.openide.util.Lookup)
meth protected org.openide.nodes.Sheet createSheet()
meth protected org.openide.util.actions.SystemAction[] createActions()
 anno 0 java.lang.Deprecated()
meth protected void createPasteTypes(java.awt.datatransfer.Transferable,java.util.List<org.openide.util.datatransfer.PasteType>)
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public boolean canCopy()
meth public boolean canCut()
meth public boolean canDestroy()
meth public boolean canRename()
meth public java.awt.Image getIcon(int)
meth public java.awt.Image getOpenedIcon(int)
meth public java.awt.datatransfer.Transferable clipboardCopy() throws java.io.IOException
meth public java.awt.datatransfer.Transferable clipboardCut() throws java.io.IOException
meth public java.lang.Object getValue(java.lang.String)
meth public java.lang.String getDisplayName()
meth public java.lang.String getHtmlDisplayName()
meth public javax.swing.Action getPreferredAction()
meth public javax.swing.Action[] getActions(boolean)
meth public org.openide.loaders.DataObject getDataObject()
meth public org.openide.nodes.Node$Handle getHandle()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public org.openide.util.actions.SystemAction[] getActions()
 anno 0 java.lang.Deprecated()
meth public static boolean getShowFileExtensions()
meth public static void setShowFileExtensions(boolean)
meth public void destroy() throws java.io.IOException
meth public void setName(java.lang.String)
meth public void setName(java.lang.String,boolean)
supr org.openide.nodes.AbstractNode
hfds PROP_EXTENSION,defaultLookup,obj,propL,refreshIconNodes,refreshNameIconLock,refreshNameNodes,refreshNamesIconsRunning,refreshNamesIconsTask,serialVersionUID,showFileExtensions
hcls AllFilesProperty,ExtensionProperty,LastModifiedProperty,LazyFilesSet,NamesUpdater,ObjectHandle,PropL,SizeProperty

CLSS public abstract org.openide.loaders.DataObject
cons public init(org.openide.filesystems.FileObject,org.openide.loaders.DataLoader) throws org.openide.loaders.DataObjectExistsException
fld public final static java.lang.String PROP_COOKIE = "cookie"
fld public final static java.lang.String PROP_FILES = "files"
fld public final static java.lang.String PROP_HELP = "helpCtx"
fld public final static java.lang.String PROP_MODIFIED = "modified"
fld public final static java.lang.String PROP_NAME = "name"
fld public final static java.lang.String PROP_PRIMARY_FILE = "primaryFile"
fld public final static java.lang.String PROP_TEMPLATE = "template"
fld public final static java.lang.String PROP_VALID = "valid"
innr public abstract interface static !annotation Registration
innr public abstract interface static !annotation Registrations
innr public abstract interface static Container
innr public abstract interface static Factory
innr public final static Registry
intf java.io.Serializable
intf org.openide.nodes.Node$Cookie
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.Lookup$Provider
meth protected <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(org.openide.loaders.DataShadow,java.lang.Class<{%%0}>)
meth protected abstract org.openide.filesystems.FileObject handleMove(org.openide.loaders.DataFolder) throws java.io.IOException
meth protected abstract org.openide.filesystems.FileObject handleRename(java.lang.String) throws java.io.IOException
meth protected abstract org.openide.loaders.DataObject handleCopy(org.openide.loaders.DataFolder) throws java.io.IOException
meth protected abstract org.openide.loaders.DataObject handleCreateFromTemplate(org.openide.loaders.DataFolder,java.lang.String) throws java.io.IOException
meth protected abstract void handleDelete() throws java.io.IOException
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected final void fireVetoableChange(java.lang.String,java.lang.Object,java.lang.Object) throws java.beans.PropertyVetoException
meth protected final void markFiles() throws java.io.IOException
meth protected org.openide.filesystems.FileLock takePrimaryFileLock() throws java.io.IOException
meth protected org.openide.loaders.DataObject handleCopyRename(org.openide.loaders.DataFolder,java.lang.String,java.lang.String) throws java.io.IOException
meth protected org.openide.loaders.DataShadow handleCreateShadow(org.openide.loaders.DataFolder) throws java.io.IOException
meth protected org.openide.nodes.Node createNodeDelegate()
meth protected void dispose()
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public abstract boolean isCopyAllowed()
meth public abstract boolean isDeleteAllowed()
meth public abstract boolean isMoveAllowed()
meth public abstract boolean isRenameAllowed()
meth public abstract org.openide.util.HelpCtx getHelpCtx()
meth public boolean isModified()
meth public boolean isShadowAllowed()
meth public final boolean isTemplate()
meth public final boolean isValid()
meth public final org.openide.filesystems.FileObject getPrimaryFile()
meth public final org.openide.loaders.DataFolder getFolder()
meth public final org.openide.loaders.DataLoader getLoader()
meth public final org.openide.loaders.DataObject copy(org.openide.loaders.DataFolder) throws java.io.IOException
meth public final org.openide.loaders.DataObject createFromTemplate(org.openide.loaders.DataFolder) throws java.io.IOException
meth public final org.openide.loaders.DataObject createFromTemplate(org.openide.loaders.DataFolder,java.lang.String) throws java.io.IOException
meth public final org.openide.loaders.DataObject createFromTemplate(org.openide.loaders.DataFolder,java.lang.String,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public final org.openide.loaders.DataShadow createShadow(org.openide.loaders.DataFolder) throws java.io.IOException
meth public final org.openide.nodes.Node getNodeDelegate()
meth public final void delete() throws java.io.IOException
meth public final void move(org.openide.loaders.DataFolder) throws java.io.IOException
meth public final void rename(java.lang.String) throws java.io.IOException
meth public final void setTemplate(boolean) throws java.io.IOException
meth public java.lang.Object writeReplace()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.util.Set<org.openide.filesystems.FileObject> files()
meth public org.openide.util.Lookup getLookup()
meth public static org.openide.loaders.DataObject find(org.openide.filesystems.FileObject) throws org.openide.loaders.DataObjectNotFoundException
meth public static org.openide.loaders.DataObject$Registry getRegistry()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addVetoableChangeListener(java.beans.VetoableChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removeVetoableChangeListener(java.beans.VetoableChangeListener)
meth public void setModified(boolean)
meth public void setValid(boolean) throws java.beans.PropertyVetoException
supr java.lang.Object
hfds BEING_CREATED,EA_ASSIGNED_LOADER,EA_ASSIGNED_LOADER_MODULE,LOCK,LOG,OBJ_LOG,PROGRESS_INFO_TL,REGISTRY_INSTANCE,changeSupport,changeSupportUpdater,item,loader,modif,modified,nodeDelegate,serialVersionUID,syncModified,synchObject,vetoableChangeSupport,warnedClasses
hcls CreateAction,DOSavable,ModifiedRegistry,ProgressInfo,Replace

CLSS public abstract interface static org.openide.loaders.DataObject$Factory
 outer org.openide.loaders.DataObject
meth public abstract org.openide.loaders.DataObject findDataObject(org.openide.filesystems.FileObject,java.util.Set<? super org.openide.filesystems.FileObject>) throws java.io.IOException

CLSS public org.openide.loaders.MultiDataObject
cons public init(org.openide.filesystems.FileObject,org.openide.loaders.MultiFileLoader) throws org.openide.loaders.DataObjectExistsException
innr public abstract Entry
meth protected final org.openide.loaders.MultiDataObject$Entry registerEntry(org.openide.filesystems.FileObject)
meth protected final org.openide.nodes.CookieSet getCookieSet()
meth protected final void addSecondaryEntry(org.openide.loaders.MultiDataObject$Entry)
meth protected final void registerEditor(java.lang.String,boolean)
meth protected final void removeSecondaryEntry(org.openide.loaders.MultiDataObject$Entry)
meth protected final void setCookieSet(org.openide.nodes.CookieSet)
 anno 0 java.lang.Deprecated()
meth protected int associateLookup()
meth protected org.openide.filesystems.FileLock takePrimaryFileLock() throws java.io.IOException
meth protected org.openide.filesystems.FileObject handleMove(org.openide.loaders.DataFolder) throws java.io.IOException
meth protected org.openide.filesystems.FileObject handleRename(java.lang.String) throws java.io.IOException
meth protected org.openide.loaders.DataObject handleCopy(org.openide.loaders.DataFolder) throws java.io.IOException
meth protected org.openide.loaders.DataObject handleCopyRename(org.openide.loaders.DataFolder,java.lang.String,java.lang.String) throws java.io.IOException
meth protected org.openide.loaders.DataObject handleCreateFromTemplate(org.openide.loaders.DataFolder,java.lang.String) throws java.io.IOException
meth protected org.openide.nodes.Node createNodeDelegate()
meth protected void handleDelete() throws java.io.IOException
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public boolean isCopyAllowed()
meth public boolean isDeleteAllowed()
meth public boolean isMoveAllowed()
meth public boolean isRenameAllowed()
meth public final java.util.Set<org.openide.loaders.MultiDataObject$Entry> secondaryEntries()
meth public final org.openide.loaders.MultiDataObject$Entry findSecondaryEntry(org.openide.filesystems.FileObject)
meth public final org.openide.loaders.MultiDataObject$Entry getPrimaryEntry()
meth public final org.openide.loaders.MultiFileLoader getMultiFileLoader()
meth public java.util.Set<org.openide.filesystems.FileObject> files()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public org.openide.util.Lookup getLookup()
supr org.openide.loaders.DataObject
hfds ERR,RECOGNIZER,TEMPLATE_ATTRIBUTES,chLis,checked,cookieSet,cookieSetLock,delayProcessor,delayedPropFilesLock,delayedPropFilesTask,firingProcessor,later,primary,secondary,secondaryCreationLock,serialVersionUID
hcls ChangeAndBefore,EmptyRecognizer,EntryReplace,Pair

CLSS public abstract org.openide.loaders.MultiFileLoader
cons protected init(java.lang.Class<? extends org.openide.loaders.DataObject>)
 anno 0 java.lang.Deprecated()
cons protected init(java.lang.String)
meth protected abstract org.openide.filesystems.FileObject findPrimaryFile(org.openide.filesystems.FileObject)
meth protected abstract org.openide.loaders.MultiDataObject createMultiObject(org.openide.filesystems.FileObject) throws java.io.IOException
meth protected abstract org.openide.loaders.MultiDataObject$Entry createPrimaryEntry(org.openide.loaders.MultiDataObject,org.openide.filesystems.FileObject)
meth protected abstract org.openide.loaders.MultiDataObject$Entry createSecondaryEntry(org.openide.loaders.MultiDataObject,org.openide.filesystems.FileObject)
meth protected final org.openide.loaders.DataObject handleFindDataObject(org.openide.filesystems.FileObject,org.openide.loaders.DataLoader$RecognizedFiles) throws java.io.IOException
supr org.openide.loaders.DataLoader
hfds serialVersionUID

CLSS public org.openide.loaders.TemplateWizard
cons protected init(org.openide.loaders.TemplateWizard$Iterator)
cons public init()
innr public abstract interface static Iterator
meth protected java.util.Set<org.openide.loaders.DataObject> handleInstantiate() throws java.io.IOException
meth protected org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor> createTargetChooser()
meth protected org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor> createTemplateChooser()
meth protected org.openide.loaders.TemplateWizard$Iterator createDefaultIterator()
meth protected void initialize()
meth protected void updateState()
meth public java.lang.String getTargetName()
meth public java.text.MessageFormat getTitleFormat()
meth public java.util.Set<org.openide.loaders.DataObject> instantiate() throws java.io.IOException
meth public java.util.Set<org.openide.loaders.DataObject> instantiate(org.openide.loaders.DataObject) throws java.io.IOException
meth public java.util.Set<org.openide.loaders.DataObject> instantiate(org.openide.loaders.DataObject,org.openide.loaders.DataFolder) throws java.io.IOException
meth public org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor> targetChooser()
meth public org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor> templateChooser()
meth public org.openide.loaders.DataFolder getTargetFolder() throws java.io.IOException
meth public org.openide.loaders.DataFolder getTemplatesFolder()
meth public org.openide.loaders.DataObject getTemplate()
meth public static java.lang.String getDescriptionAsResource(org.openide.loaders.DataObject)
 anno 0 java.lang.Deprecated()
meth public static java.net.URL getDescription(org.openide.loaders.DataObject)
meth public static org.openide.loaders.TemplateWizard$Iterator getIterator(org.openide.loaders.DataObject)
meth public static void setDescription(org.openide.loaders.DataObject,java.net.URL) throws java.io.IOException
meth public static void setDescriptionAsResource(org.openide.loaders.DataObject,java.lang.String) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public static void setIterator(org.openide.loaders.DataObject,org.openide.loaders.TemplateWizard$Iterator) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public void setTargetFolder(org.openide.loaders.DataFolder)
meth public void setTargetFolderLazy(java.util.function.Supplier<org.openide.loaders.DataFolder>)
meth public void setTargetName(java.lang.String)
meth public void setTemplate(org.openide.loaders.DataObject)
meth public void setTemplatesFolder(org.openide.loaders.DataFolder)
meth public void setTitleFormat(java.text.MessageFormat)
supr org.openide.WizardDescriptor
hfds CUSTOM_DESCRIPTION,CUSTOM_ITERATOR,EA_DESCRIPTION,EA_DESC_RESOURCE,EA_ITERATOR,LOG,PREF_DIM,isInstantiating,iterator,lastComp,newObjects,pcl,progressHandle,showTargetChooser,targetChooser,targetDataFolder,targetDataFolderCreator,targetIterator,targetName,template,templateChooser,templatesFolder,titleFormatSet
hcls DefaultIterator,InstantiatingIteratorBridge

CLSS public abstract interface static org.openide.loaders.TemplateWizard$Iterator
 outer org.openide.loaders.TemplateWizard
intf java.io.Serializable
intf org.openide.WizardDescriptor$Iterator<org.openide.WizardDescriptor>
intf org.openide.nodes.Node$Cookie
meth public abstract java.util.Set<org.openide.loaders.DataObject> instantiate(org.openide.loaders.TemplateWizard) throws java.io.IOException
meth public abstract void initialize(org.openide.loaders.TemplateWizard)
meth public abstract void uninitialize(org.openide.loaders.TemplateWizard)

CLSS public abstract org.openide.loaders.UniFileLoader
cons protected init(java.lang.Class<? extends org.openide.loaders.DataObject>)
 anno 0 java.lang.Deprecated()
cons protected init(java.lang.String)
fld public final static java.lang.String PROP_EXTENSIONS = "extensions"
meth protected abstract org.openide.loaders.MultiDataObject createMultiObject(org.openide.filesystems.FileObject) throws java.io.IOException
meth protected org.openide.filesystems.FileObject findPrimaryFile(org.openide.filesystems.FileObject)
meth protected org.openide.loaders.MultiDataObject$Entry createPrimaryEntry(org.openide.loaders.MultiDataObject,org.openide.filesystems.FileObject)
meth protected org.openide.loaders.MultiDataObject$Entry createSecondaryEntry(org.openide.loaders.MultiDataObject,org.openide.filesystems.FileObject)
meth public org.openide.loaders.ExtensionList getExtensions()
meth public void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void setExtensions(org.openide.loaders.ExtensionList)
meth public void writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr org.openide.loaders.MultiFileLoader
hfds serialVersionUID

CLSS public org.openide.nodes.AbstractNode
cons public init(org.openide.nodes.Children)
cons public init(org.openide.nodes.Children,org.openide.util.Lookup)
fld protected java.text.MessageFormat displayFormat
fld protected org.openide.util.actions.SystemAction[] systemActions
 anno 0 java.lang.Deprecated()
meth protected final org.openide.nodes.CookieSet getCookieSet()
meth protected final org.openide.nodes.Sheet getSheet()
meth protected final void setCookieSet(org.openide.nodes.CookieSet)
 anno 0 java.lang.Deprecated()
meth protected final void setSheet(org.openide.nodes.Sheet)
meth protected org.openide.nodes.Sheet createSheet()
meth protected org.openide.util.actions.SystemAction[] createActions()
 anno 0 java.lang.Deprecated()
meth protected void createPasteTypes(java.awt.datatransfer.Transferable,java.util.List<org.openide.util.datatransfer.PasteType>)
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public boolean canCopy()
meth public boolean canCut()
meth public boolean canDestroy()
meth public boolean canRename()
meth public boolean hasCustomizer()
meth public final org.openide.util.datatransfer.PasteType[] getPasteTypes(java.awt.datatransfer.Transferable)
meth public final void setIconBaseWithExtension(java.lang.String)
meth public java.awt.Component getCustomizer()
meth public java.awt.Image getIcon(int)
meth public java.awt.Image getOpenedIcon(int)
meth public java.awt.datatransfer.Transferable clipboardCopy() throws java.io.IOException
meth public java.awt.datatransfer.Transferable clipboardCut() throws java.io.IOException
meth public java.awt.datatransfer.Transferable drag() throws java.io.IOException
meth public javax.swing.Action getPreferredAction()
meth public org.openide.nodes.Node cloneNode()
meth public org.openide.nodes.Node$Handle getHandle()
meth public org.openide.nodes.Node$PropertySet[] getPropertySets()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public org.openide.util.actions.SystemAction getDefaultAction()
 anno 0 java.lang.Deprecated()
meth public org.openide.util.actions.SystemAction[] getActions()
 anno 0 java.lang.Deprecated()
meth public org.openide.util.datatransfer.NewType[] getNewTypes()
meth public org.openide.util.datatransfer.PasteType getDropType(java.awt.datatransfer.Transferable,int,int)
meth public void setDefaultAction(org.openide.util.actions.SystemAction)
 anno 0 java.lang.Deprecated()
meth public void setIconBase(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void setName(java.lang.String)
supr org.openide.nodes.Node
hfds DEFAULT_ICON,DEFAULT_ICON_BASE,DEFAULT_ICON_EXTENSION,ICON_BASE,NO_NEW_TYPES,NO_PASTE_TYPES,OPENED_ICON_BASE,iconBase,iconExtension,icons,lookup,overridesGetDefaultAction,preferredAction,sheet,sheetCookieL
hcls SheetAndCookieListener

CLSS public final org.openide.nodes.CookieSet
cons public init()
innr public abstract interface static Before
innr public abstract interface static Factory
intf org.openide.util.Lookup$Provider
meth public !varargs <%0 extends java.lang.Object> void assign(java.lang.Class<? extends {%%0}>,{%%0}[])
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public org.openide.util.Lookup getLookup()
meth public static org.openide.nodes.CookieSet createGeneric(org.openide.nodes.CookieSet$Before)
meth public void add(java.lang.Class<? extends org.openide.nodes.Node$Cookie>,org.openide.nodes.CookieSet$Factory)
meth public void add(java.lang.Class<? extends org.openide.nodes.Node$Cookie>[],org.openide.nodes.CookieSet$Factory)
meth public void add(org.openide.nodes.Node$Cookie)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void remove(java.lang.Class<? extends org.openide.nodes.Node$Cookie>,org.openide.nodes.CookieSet$Factory)
meth public void remove(java.lang.Class<? extends org.openide.nodes.Node$Cookie>[],org.openide.nodes.CookieSet$Factory)
meth public void remove(org.openide.nodes.Node$Cookie)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
hfds QUERY_MODE,cs,ic,lookup,map
hcls C,CookieEntry,CookieEntryPair,PairWrap,R

CLSS public abstract interface static org.openide.nodes.CookieSet$Factory
 outer org.openide.nodes.CookieSet
meth public abstract <%0 extends org.openide.nodes.Node$Cookie> {%%0} createCookie(java.lang.Class<{%%0}>)

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

CLSS public abstract org.openide.util.SharedClassObject
cons protected init()
intf java.io.Externalizable
meth protected boolean clearSharedData()
meth protected final java.lang.Object getLock()
meth protected final java.lang.Object getProperty(java.lang.Object)
meth protected final java.lang.Object putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void finalize() throws java.lang.Throwable
meth protected java.lang.Object writeReplace()
meth protected void addNotify()
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void initialize()
meth protected void removeNotify()
meth protected void reset()
meth public final boolean equals(java.lang.Object)
meth public final int hashCode()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public static <%0 extends org.openide.util.SharedClassObject> {%%0} findObject(java.lang.Class<{%%0}>)
meth public static <%0 extends org.openide.util.SharedClassObject> {%%0} findObject(java.lang.Class<{%%0}>,boolean)
meth public void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr java.lang.Object
hfds PROP_SUPPORT,addNotifySuper,alreadyWarnedAboutDupes,dataEntry,err,first,firstTrace,inReadExternal,initializeSuper,instancesBeingCreated,lock,prematureSystemOptionMutation,removeNotifySuper,serialVersionUID,systemOption,values,waitingOnSystemOption
hcls DataEntry,SetAccessibleAction,WriteReplace

CLSS public abstract interface org.xml.sax.EntityResolver
meth public abstract org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException

