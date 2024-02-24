#Signature file v4.1
#Version 1.58.0

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

CLSS public abstract interface java.beans.PropertyEditor
meth public abstract boolean isPaintable()
meth public abstract boolean supportsCustomEditor()
meth public abstract java.awt.Component getCustomEditor()
meth public abstract java.lang.Object getValue()
meth public abstract java.lang.String getAsText()
meth public abstract java.lang.String getJavaInitializationString()
meth public abstract java.lang.String[] getTags()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void paintValue(java.awt.Graphics,java.awt.Rectangle)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setAsText(java.lang.String)
meth public abstract void setValue(java.lang.Object)

CLSS public java.beans.PropertyEditorSupport
cons public init()
cons public init(java.lang.Object)
intf java.beans.PropertyEditor
meth public boolean isPaintable()
meth public boolean supportsCustomEditor()
meth public java.awt.Component getCustomEditor()
meth public java.lang.Object getSource()
meth public java.lang.Object getValue()
meth public java.lang.String getAsText()
meth public java.lang.String getJavaInitializationString()
meth public java.lang.String[] getTags()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void firePropertyChange()
meth public void paintValue(java.awt.Graphics,java.awt.Rectangle)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setAsText(java.lang.String)
meth public void setSource(java.lang.Object)
meth public void setValue(java.lang.Object)
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

CLSS public abstract interface java.lang.Cloneable

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
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

CLSS public abstract java.net.Authenticator
cons public init()
innr public final static !enum RequestorType
meth protected final int getRequestingPort()
meth protected final java.lang.String getRequestingHost()
meth protected final java.lang.String getRequestingPrompt()
meth protected final java.lang.String getRequestingProtocol()
meth protected final java.lang.String getRequestingScheme()
meth protected final java.net.InetAddress getRequestingSite()
meth protected java.net.Authenticator$RequestorType getRequestorType()
meth protected java.net.PasswordAuthentication getPasswordAuthentication()
meth protected java.net.URL getRequestingURL()
meth public static java.net.PasswordAuthentication requestPasswordAuthentication(java.lang.String,java.net.InetAddress,int,java.lang.String,java.lang.String,java.lang.String)
meth public static java.net.PasswordAuthentication requestPasswordAuthentication(java.lang.String,java.net.InetAddress,int,java.lang.String,java.lang.String,java.lang.String,java.net.URL,java.net.Authenticator$RequestorType)
meth public static java.net.PasswordAuthentication requestPasswordAuthentication(java.net.InetAddress,int,java.lang.String,java.lang.String,java.lang.String)
meth public static void setDefault(java.net.Authenticator)
supr java.lang.Object

CLSS public abstract java.net.ProxySelector
cons public init()
meth public abstract java.util.List<java.net.Proxy> select(java.net.URI)
meth public abstract void connectFailed(java.net.URI,java.net.SocketAddress,java.io.IOException)
meth public static java.net.ProxySelector getDefault()
meth public static void setDefault(java.net.ProxySelector)
supr java.lang.Object

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

CLSS public abstract interface javax.enterprise.deploy.spi.DeploymentConfiguration
meth public abstract javax.enterprise.deploy.model.DeployableObject getDeployableObject()
meth public abstract javax.enterprise.deploy.spi.DConfigBeanRoot getDConfigBeanRoot(javax.enterprise.deploy.model.DDBeanRoot) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException
meth public abstract javax.enterprise.deploy.spi.DConfigBeanRoot restoreDConfigBean(java.io.InputStream,javax.enterprise.deploy.model.DDBeanRoot) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException
meth public abstract void removeDConfigBean(javax.enterprise.deploy.spi.DConfigBeanRoot) throws javax.enterprise.deploy.spi.exceptions.BeanNotFoundException
meth public abstract void restore(java.io.InputStream) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException
meth public abstract void save(java.io.OutputStream) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException
meth public abstract void saveDConfigBean(java.io.OutputStream,javax.enterprise.deploy.spi.DConfigBeanRoot) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException

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

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.common.api.Datasource
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getDriverClassName()
meth public abstract java.lang.String getJndiName()
meth public abstract java.lang.String getPassword()
meth public abstract java.lang.String getUrl()
meth public abstract java.lang.String getUsername()

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.common.api.MessageDestination
innr public final static !enum Type
meth public abstract java.lang.String getName()
meth public abstract org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type getType()

CLSS public org.netbeans.modules.j2ee.sun.api.Asenv
cons public init(java.io.File)
fld public final static java.lang.String AS_DEF_DOMAINS_PATH = "AS_DEF_DOMAINS_PATH"
fld public final static java.lang.String AS_HADB = "AS_HADB"
fld public final static java.lang.String AS_JAVA = "AS_JAVA"
fld public final static java.lang.String AS_NS_BIN = "AS_NSS_BIN"
meth public java.lang.String get(java.lang.String)
supr java.lang.Object
hfds props

CLSS public abstract interface org.netbeans.modules.j2ee.sun.api.CmpMappingProvider
meth public abstract boolean removeMappingForCmp(org.netbeans.modules.j2ee.sun.dd.api.cmp.SunCmpMappings,java.lang.String)
meth public abstract boolean removeMappingForCmpField(org.netbeans.modules.j2ee.sun.dd.api.cmp.SunCmpMappings,java.lang.String,java.lang.String)
meth public abstract boolean renameMappingForCmp(org.netbeans.modules.j2ee.sun.dd.api.cmp.SunCmpMappings,java.lang.String,java.lang.String)
meth public abstract boolean renameMappingForCmpField(org.netbeans.modules.j2ee.sun.dd.api.cmp.SunCmpMappings,java.lang.String,java.lang.String,java.lang.String)
meth public abstract void mapCmpBeans(org.openide.filesystems.FileObject,org.netbeans.modules.j2ee.deployment.common.api.OriginalCMPMapping[],org.netbeans.modules.j2ee.sun.dd.api.cmp.SunCmpMappings)

CLSS public org.netbeans.modules.j2ee.sun.api.ExtendedClassLoader
cons public init() throws java.net.MalformedURLException
cons public init(java.lang.ClassLoader) throws java.net.MalformedURLException
meth protected java.security.PermissionCollection getPermissions(java.security.CodeSource)
meth public void addURL(java.io.File) throws java.net.MalformedURLException
supr java.net.URLClassLoader

CLSS public abstract interface org.netbeans.modules.j2ee.sun.api.InstrumentAVK
meth public abstract boolean createAVKSupport(javax.enterprise.deploy.spi.DeploymentManager,org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider)
meth public abstract void generateReport()
meth public abstract void setAVK(boolean)
meth public abstract void setDeploymentManager(org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.api.ResourceConfiguratorInterface
meth public abstract boolean isJMSResourceDefined(java.lang.String,java.io.File)
meth public abstract java.lang.String createJDBCDataSourceForCmp(java.lang.String,java.lang.String,java.io.File)
meth public abstract java.util.HashSet getMessageDestinations(java.io.File)
meth public abstract java.util.HashSet getResources(java.io.File)
meth public abstract java.util.HashSet getServerDataSources()
meth public abstract java.util.HashSet getServerDestinations()
meth public abstract org.netbeans.modules.j2ee.deployment.common.api.Datasource createDataSource(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.io.File,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException
meth public abstract org.netbeans.modules.j2ee.deployment.common.api.MessageDestination createJMSResource(java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type,java.lang.String,java.io.File,java.lang.String)
meth public abstract void createJDBCDataSourceFromRef(java.lang.String,java.lang.String,java.io.File)
meth public abstract void createJMSResource(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.io.File,java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.api.ServerInterface
meth public abstract java.lang.Object getAttribute(javax.management.ObjectName,java.lang.String) throws java.rmi.RemoteException,javax.management.AttributeNotFoundException,javax.management.InstanceNotFoundException,javax.management.MBeanException,javax.management.ReflectionException
meth public abstract java.lang.Object getManagement()
meth public abstract java.lang.Object invoke(javax.management.ObjectName,java.lang.String,java.lang.Object[],java.lang.String[]) throws java.rmi.RemoteException,javax.management.InstanceNotFoundException,javax.management.MBeanException,javax.management.ReflectionException
meth public abstract java.lang.String getWebModuleName(java.lang.String)
meth public abstract javax.enterprise.deploy.spi.DeploymentManager getDeploymentManager()
meth public abstract javax.management.AttributeList getAttributes(javax.management.ObjectName,java.lang.String[]) throws java.rmi.RemoteException,javax.management.InstanceNotFoundException,javax.management.ReflectionException
meth public abstract javax.management.MBeanInfo getMBeanInfo(javax.management.ObjectName) throws java.rmi.RemoteException,javax.management.InstanceNotFoundException,javax.management.IntrospectionException,javax.management.ReflectionException
meth public abstract javax.management.MBeanServerConnection getMBeanServerConnection() throws java.rmi.RemoteException
meth public abstract void checkCredentials() throws java.io.IOException
meth public abstract void setAttribute(javax.management.ObjectName,javax.management.Attribute) throws java.rmi.RemoteException,javax.management.AttributeNotFoundException,javax.management.InstanceNotFoundException,javax.management.InvalidAttributeValueException,javax.management.MBeanException,javax.management.ReflectionException
meth public abstract void setDeploymentManager(javax.enterprise.deploy.spi.DeploymentManager)

CLSS public org.netbeans.modules.j2ee.sun.api.ServerLocationManager
cons public init()
fld public final static int GF_V1 = 900
fld public final static int GF_V2 = 910
fld public final static int GF_V2point1 = 911
fld public final static int GF_V2point1point1 = 912
fld public final static int SJSAS_82 = 820
fld public final static int SJSAS_90 = 900
fld public final static int SJSAS_91 = 910
fld public final static java.lang.String INSTALL_ROOT_PROP_NAME = "com.sun.aas.installRoot"
meth public static boolean hasUpdateCenter(java.io.File)
meth public static boolean isGlassFish(java.io.File)
meth public static boolean isGoodAppServerLocation(java.io.File)
meth public static boolean isJavaDBPresent(java.io.File)
meth public static int getAppServerPlatformVersion(java.io.File)
meth public static java.io.File getLatestPlatformLocation()
meth public static java.io.File getUpdateCenterLauncher(java.io.File)
meth public static java.lang.ClassLoader getNetBeansAndServerClassLoader(java.io.File)
meth public static java.lang.ClassLoader getServerOnlyClassLoader(java.io.File)
meth public static javax.enterprise.deploy.spi.factories.DeploymentFactory getDeploymentFactory(java.io.File)
supr java.lang.Object
hfds JAR_BRIGDES_DEFINITION_LAYER,fileColl,serverLocationAndClassLoaderMap
hcls CacheData,Empty

CLSS public abstract interface org.netbeans.modules.j2ee.sun.api.SimpleNodeExtensionProvider
meth public abstract org.openide.nodes.Node getExtensionNode(javax.management.MBeanServerConnection)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.api.SunDeploymentConfigurationInterface
intf javax.enterprise.deploy.spi.DeploymentConfiguration
meth public abstract java.lang.String getContextRoot()
meth public abstract java.lang.String getDeploymentModuleName()

CLSS public abstract interface org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface
intf org.openide.nodes.Node$Cookie
meth public abstract boolean grabInnerDM(java.lang.Thread,boolean)
meth public abstract boolean isDebugSharedMemory() throws java.rmi.RemoteException
meth public abstract boolean isLocal()
meth public abstract boolean isRestartNeeded()
meth public abstract boolean isRunning()
meth public abstract boolean isRunning(boolean)
meth public abstract boolean isSecure()
meth public abstract boolean isSuspended()
meth public abstract int getAppserverVersion()
meth public abstract int getPort()
meth public abstract java.io.File getPlatformRoot()
meth public abstract java.lang.String getDebugAddressValue() throws java.rmi.RemoteException
meth public abstract java.lang.String getHost()
meth public abstract java.lang.String getNonAdminPortNumber()
meth public abstract java.lang.String getPassword()
meth public abstract java.lang.String getUserName()
meth public abstract java.util.HashMap getAdminObjectResourcesFromXml()
meth public abstract java.util.HashMap getConnPoolsFromXml()
meth public abstract java.util.HashMap getSunDatasourcesFromXml()
meth public abstract org.netbeans.modules.j2ee.sun.api.CmpMappingProvider getSunCmpMapper()
meth public abstract org.netbeans.modules.j2ee.sun.api.ResourceConfiguratorInterface getResourceConfigurator()
meth public abstract org.netbeans.modules.j2ee.sun.api.ServerInterface getManagement()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void createSampleDataSourceinDomain()
meth public abstract void fixJVMDebugOptions() throws java.rmi.RemoteException
meth public abstract void refreshDeploymentManager()
meth public abstract void releaseInnerDM(java.lang.Thread)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setPassword(java.lang.String)
meth public abstract void setUserName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.api.SunServerStateInterface
meth public abstract boolean isDebugged()
meth public abstract boolean isRunning()
meth public abstract void viewLogFile()

CLSS public org.netbeans.modules.j2ee.sun.api.SunURIManager
cons public init()
fld public final static java.lang.String SUNSERVERSURI = "deployer:Sun:AppServer::"
meth public static org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties createInstanceProperties(java.io.File,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException
meth public static org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties getInstanceProperties(java.io.File,java.lang.String,int)
supr java.lang.Object

CLSS public org.netbeans.modules.j2ee.sun.api.restricted.RegistrationUtils
cons public init()
meth public static java.util.HashMap getProjectAdminObjects(java.util.HashMap,org.netbeans.modules.j2ee.sun.dd.api.serverresources.AdminObjectResource[])
meth public static java.util.HashMap getProjectConnectors(java.util.HashMap,org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorResource[])
meth public static java.util.HashMap getProjectDatasources(java.util.HashMap,org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcResource[])
meth public static java.util.HashMap getProjectMailResources(java.util.HashMap,org.netbeans.modules.j2ee.sun.dd.api.serverresources.MailResource[])
meth public static java.util.HashMap getReferringResources(java.lang.String,java.util.HashMap,org.netbeans.modules.j2ee.sun.api.ServerInterface)
meth public static java.util.HashMap getServerConnectionPools(org.netbeans.modules.j2ee.sun.api.ServerInterface,java.lang.String)
meth public static java.util.HashMap getServerResources(org.netbeans.modules.j2ee.sun.api.ServerInterface,java.lang.String)
meth public static void checkUpdateServerResources(org.netbeans.modules.j2ee.sun.api.ServerInterface,java.io.File)
meth public static void copyServerPool(java.util.HashMap,java.lang.String,java.lang.String,org.netbeans.modules.j2ee.sun.api.ServerInterface)
meth public static void deleteOldServerPool(java.lang.String,java.lang.String,org.netbeans.modules.j2ee.sun.api.ServerInterface)
meth public static void deleteServerResources(java.util.HashMap,org.netbeans.modules.j2ee.sun.api.ServerInterface,java.lang.String)
meth public static void updateExternalResource(java.util.HashMap,java.lang.String,org.netbeans.modules.j2ee.sun.api.ServerInterface)
supr java.lang.Object
hfds DAS_SERVER_NAME,DELETE_ADMINOBJECT,DELETE_CONNECTOR,DELETE_CONNECTORPOOL,DELETE_JDBC,DELETE_MAIL,DELETE_POOL,LOG,POOL_EXTENSION

CLSS public org.netbeans.modules.j2ee.sun.api.restricted.ResourceConfigurator
cons public init()
fld public final static java.lang.String JDBC_RESOURCE = "jdbc"
fld public final static java.lang.String JMS_PREFIX = "jms/"
fld public final static java.lang.String JMS_RESOURCE = "jms"
fld public final static java.lang.String __SunResourceExt = "sun-resource"
intf org.netbeans.modules.j2ee.sun.api.ResourceConfiguratorInterface
meth public boolean isJMSResourceDefined(java.lang.String,java.io.File)
meth public java.lang.String createJDBCDataSourceForCmp(java.lang.String,java.lang.String,java.io.File)
meth public java.lang.String getDerbyDatabaseName(java.lang.String)
meth public java.lang.String getDerbyPortNo(java.lang.String)
meth public java.lang.String getDerbyServerName(java.lang.String)
meth public java.util.HashSet getMessageDestinations(java.io.File)
meth public java.util.HashSet getResources(java.io.File)
meth public java.util.HashSet getServerDataSources()
meth public java.util.HashSet getServerDestinations()
meth public org.netbeans.modules.j2ee.deployment.common.api.Datasource createDataSource(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.io.File,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException
meth public org.netbeans.modules.j2ee.deployment.common.api.MessageDestination createJMSResource(java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type,java.lang.String,java.io.File,java.lang.String)
meth public static java.lang.String getDatabaseVendorName(java.lang.String,org.netbeans.modules.j2ee.sun.sunresources.beans.Wizard)
meth public static java.util.HashSet getResourcesFromFile(java.io.File)
meth public static void showInformation(java.lang.String)
meth public void createJDBCDataSourceFromRef(java.lang.String,java.lang.String,java.io.File)
meth public void createJMSResource(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.io.File,java.lang.String)
meth public void setDeploymentManager(javax.enterprise.deploy.spi.DeploymentManager)
supr java.lang.Object
hfds BLANK,DASH,DATAFILE,DOT,ILLEGAL_FILENAME_CHARS,LOG,REPLACEMENT_CHAR,bundle,currentDM

CLSS public org.netbeans.modules.j2ee.sun.api.restricted.ResourceUtils
cons public init()
fld public final static java.lang.String META_INF = "META-INF"
fld public final static java.lang.String WEB_INF = "WEB-INF"
intf org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants
meth public final static java.lang.String getJavaEEModuleConfigDir(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule)
meth public final static java.lang.String getResourcesFileModulePath(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule,org.netbeans.modules.glassfish.tooling.data.GlassFishVersion)
meth public final static java.lang.String getResourcesFileName(org.netbeans.modules.glassfish.tooling.data.GlassFishVersion)
meth public static boolean is90Server(org.netbeans.modules.j2ee.sun.api.ServerInterface)
meth public static boolean isFriendlyFilename(java.lang.String)
meth public static boolean isLegalFilename(java.lang.String)
meth public static boolean isLegalResourceName(java.lang.String)
meth public static boolean isUniqueFileName(java.lang.String,org.openide.filesystems.FileObject,java.lang.String)
meth public static java.io.File createPathForFile(java.io.File)
meth public static java.io.File getServerResourcesFile(org.openide.filesystems.FileObject,boolean)
meth public static java.lang.String createUniqueFileName(java.lang.String,org.openide.filesystems.FileObject,java.lang.String)
meth public static java.lang.String getUniqueResourceName(java.lang.String,java.util.HashMap)
meth public static java.lang.String getUniqueResourceName(java.lang.String,java.util.List)
meth public static java.lang.String makeLegalFilename(java.lang.String)
meth public static java.lang.String revertToResName(java.lang.String)
meth public static java.util.HashMap fillInPoolValues(org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface,javax.management.ObjectName,java.lang.String) throws java.lang.Exception
meth public static java.util.HashMap getConnPoolValues(java.io.File,java.lang.String)
meth public static java.util.HashSet formatXmlSunDatasources(java.util.HashMap)
meth public static java.util.HashSet getServerDataSources(javax.enterprise.deploy.spi.DeploymentManager)
meth public static java.util.HashSet getServerDestinations(javax.enterprise.deploy.spi.DeploymentManager)
meth public static java.util.List getAllResourceNames(java.io.File,java.util.List)
meth public static java.util.List getRegisteredConnectionPools(org.netbeans.modules.j2ee.sun.ide.sunresources.wizards.ResourceConfigData)
meth public static java.util.List getRegisteredJdbcResources(org.netbeans.modules.j2ee.sun.ide.sunresources.wizards.ResourceConfigData)
meth public static java.util.Map getResourceAttributeNames(javax.management.ObjectName,org.netbeans.modules.j2ee.sun.api.ServerInterface) throws java.lang.Exception
meth public static javax.management.AttributeList getResourceAttributes(org.netbeans.modules.j2ee.sun.dd.api.serverresources.AdminObjectResource)
meth public static javax.management.AttributeList getResourceAttributes(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorConnectionPool)
meth public static javax.management.AttributeList getResourceAttributes(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorResource)
meth public static javax.management.AttributeList getResourceAttributes(org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcConnectionPool,org.netbeans.modules.j2ee.sun.api.ServerInterface)
meth public static javax.management.AttributeList getResourceAttributes(org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcResource)
meth public static javax.management.AttributeList getResourceAttributes(org.netbeans.modules.j2ee.sun.dd.api.serverresources.JmsResource)
meth public static javax.management.AttributeList getResourceAttributes(org.netbeans.modules.j2ee.sun.dd.api.serverresources.MailResource)
meth public static javax.management.AttributeList getResourceAttributes(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PersistenceManagerFactoryResource)
meth public static org.netbeans.api.db.explorer.DatabaseConnection getDatabaseConnection(java.lang.String)
meth public static org.netbeans.modules.glassfish.tooling.data.GlassFishServer getGlassFishServer(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider)
meth public static org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider getJavaEEModuleProvider(org.openide.filesystems.FileObject)
meth public static org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties getTargetServer(org.openide.filesystems.FileObject)
meth public static org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources getResourcesGraph(java.io.File)
meth public static org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources getServerResourcesGraph(java.io.File)
meth public static org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources getServerResourcesGraph(org.openide.filesystems.FileObject,java.lang.String)
meth public static org.openide.filesystems.FileObject getResourceDirectory(org.openide.filesystems.FileObject)
meth public static org.openide.filesystems.FileObject setUpExists(org.openide.filesystems.FileObject)
meth public static void createFile(java.io.File,org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static void createFile(org.netbeans.modules.j2ee.sun.ide.sunresources.wizards.ResourceConfigData,org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources,java.lang.String)
meth public static void createFile(org.openide.filesystems.FileObject,org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static void createResource(java.lang.String,java.lang.Object[],org.netbeans.modules.j2ee.sun.api.ServerInterface) throws java.lang.Exception
meth public static void createSampleDataSource(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider)
meth public static void migrateResources(java.io.File,java.lang.String)
meth public static void migrateResources(org.openide.filesystems.FileObject,java.lang.String)
meth public static void register(org.netbeans.modules.j2ee.sun.dd.api.serverresources.AdminObjectResource,org.netbeans.modules.j2ee.sun.api.ServerInterface,boolean) throws java.lang.Exception
meth public static void register(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorConnectionPool,org.netbeans.modules.j2ee.sun.api.ServerInterface,boolean) throws java.lang.Exception
meth public static void register(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorResource,org.netbeans.modules.j2ee.sun.api.ServerInterface,boolean) throws java.lang.Exception
meth public static void register(org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcConnectionPool,org.netbeans.modules.j2ee.sun.api.ServerInterface,boolean) throws java.lang.Exception
meth public static void register(org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcResource,org.netbeans.modules.j2ee.sun.api.ServerInterface,boolean) throws java.lang.Exception
meth public static void register(org.netbeans.modules.j2ee.sun.dd.api.serverresources.JmsResource,org.netbeans.modules.j2ee.sun.api.ServerInterface,boolean) throws java.lang.Exception
meth public static void register(org.netbeans.modules.j2ee.sun.dd.api.serverresources.MailResource,org.netbeans.modules.j2ee.sun.api.ServerInterface,boolean) throws java.lang.Exception
meth public static void register(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PersistenceManagerFactoryResource,org.netbeans.modules.j2ee.sun.api.ServerInterface,boolean) throws java.lang.Exception
meth public static void register(org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources,org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface,boolean,java.lang.String) throws java.lang.Exception
meth public static void saveConnPoolDatatoXml(org.netbeans.modules.j2ee.sun.ide.sunresources.wizards.ResourceConfigData,java.lang.String)
meth public static void saveConnPoolDatatoXml(org.netbeans.modules.j2ee.sun.ide.sunresources.wizards.ResourceConfigData,org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources,java.lang.String)
meth public static void saveJDBCResourceDatatoXml(org.netbeans.modules.j2ee.sun.ide.sunresources.wizards.ResourceConfigData,org.netbeans.modules.j2ee.sun.ide.sunresources.wizards.ResourceConfigData,java.lang.String)
meth public static void saveJMSResourceDatatoXml(org.netbeans.modules.j2ee.sun.ide.sunresources.wizards.ResourceConfigData,java.lang.String)
meth public static void saveMailResourceDatatoXml(org.netbeans.modules.j2ee.sun.ide.sunresources.wizards.ResourceConfigData,java.lang.String)
meth public static void saveNodeToXml(org.openide.filesystems.FileObject,org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources)
meth public static void savePMFResourceDatatoXml(org.netbeans.modules.j2ee.sun.ide.sunresources.wizards.ResourceConfigData,org.netbeans.modules.j2ee.sun.ide.sunresources.wizards.ResourceConfigData,org.netbeans.modules.j2ee.sun.ide.sunresources.wizards.ResourceConfigData,java.lang.String)
meth public static void updateResourceAttributes(javax.management.ObjectName,javax.management.AttributeList,org.netbeans.modules.j2ee.sun.api.ServerInterface) throws java.lang.Exception
meth public static void updateResourceProperties(javax.management.ObjectName,java.util.Properties,org.netbeans.modules.j2ee.sun.api.ServerInterface) throws java.lang.Exception
supr java.lang.Object
hfds BLANK,DOT,ILLEGAL_FILENAME_CHARS,ILLEGAL_RESOURCE_NAME_CHARS,LOGGER,REPLACEMENT_CHAR,RESOURCE_FILES,RESOURCE_FILES_SUFFIX,SAMPLE_CONNPOOL,SAMPLE_DATASOURCE,bundle,sysConnpools,sysDatasources
hcls OldResourceFileFilter

CLSS public org.netbeans.modules.j2ee.sun.api.restricted.SunDatasource
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
intf org.netbeans.modules.j2ee.deployment.common.api.Datasource
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.io.File getResourceDir()
meth public java.lang.String getDisplayName()
meth public java.lang.String getDriverClassName()
meth public java.lang.String getJndiName()
meth public java.lang.String getPassword()
meth public java.lang.String getUrl()
meth public java.lang.String getUsername()
meth public java.lang.String toString()
meth public void setResourceDir(java.io.File)
supr java.lang.Object
hfds driverClassName,hash,jndiName,password,resourceDir,url,username

CLSS public org.netbeans.modules.j2ee.sun.api.restricted.SunMessageDestination
cons public init(java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type)
intf org.netbeans.modules.j2ee.deployment.common.api.MessageDestination
meth public java.io.File getResourceDir()
meth public java.lang.String getName()
meth public org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type getType()
meth public void setResourceDir(java.io.File)
supr java.lang.Object
hfds name,resourceDir,type

CLSS abstract interface org.netbeans.modules.j2ee.sun.api.restricted.package-info

CLSS public org.netbeans.modules.j2ee.sun.appsrvapi.PortDetector
cons public init()
meth public static boolean isSecurePort(java.lang.String,int) throws java.io.IOException
meth public static void main(java.lang.String[]) throws java.io.IOException
supr java.lang.Object
hfds PORT_CHECK_TIMEOUT

CLSS public org.netbeans.modules.j2ee.sun.ide.editors.AdminAuthenticator
cons public init()
cons public init(org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface)
meth protected java.net.PasswordAuthentication getPasswordAuthentication()
supr java.net.Authenticator
hfds TIMEOUT,displayed,lastTry,preferredSunDeploymentManagerInterface
hcls PasswordPanel

CLSS public org.netbeans.modules.j2ee.sun.ide.editors.BooleanEditor
cons public init()
meth public java.lang.String[] getTags()
supr org.netbeans.modules.j2ee.sun.ide.editors.ChoiceEditor
hfds choices

CLSS public org.netbeans.modules.j2ee.sun.ide.editors.CharsetDisplayPreferenceEditor
cons public init()
fld public static java.lang.Integer DEFAULT_PREF_VAL
meth public java.lang.Object getValue()
meth public java.lang.String getAsText()
meth public java.lang.String[] getTags()
meth public void setAsText(java.lang.String)
meth public void setValue(java.lang.Object)
supr org.netbeans.modules.j2ee.sun.ide.editors.LogLevelEditor
hfds choices,val

CLSS public abstract org.netbeans.modules.j2ee.sun.ide.editors.ChoiceEditor
cons public init()
fld public java.lang.String curr_Sel
intf org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor
meth public abstract java.lang.String[] getTags()
meth public boolean hasInPlaceCustomEditor()
meth public boolean supportsEditingTaggedValues()
meth public java.awt.Component getInPlaceCustomEditor()
meth public java.lang.Object getValue()
meth public java.lang.String getAsText()
meth public java.lang.String getJavaInitializationString()
meth public void setAsText(java.lang.String)
meth public void setValue(java.lang.Object)
supr java.beans.PropertyEditorSupport

CLSS public org.netbeans.modules.j2ee.sun.ide.editors.DataSourceTypeEditor
cons public init()
fld public java.lang.String[] tags
meth public java.lang.String[] getTags()
supr org.netbeans.modules.j2ee.sun.ide.editors.ChoiceEditor

CLSS public org.netbeans.modules.j2ee.sun.ide.editors.EditorUtils
cons public init()
meth public static boolean isValidInt0(java.lang.String)
meth public static boolean isValidLong(java.lang.String)
supr java.lang.Object

CLSS public org.netbeans.modules.j2ee.sun.ide.editors.Int0Editor
cons public init()
fld public java.lang.String prev
intf org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor
meth public boolean hasInPlaceCustomEditor()
meth public boolean supportsEditingTaggedValues()
meth public java.awt.Component getInPlaceCustomEditor()
meth public java.lang.Object getValue()
meth public java.lang.String checkValid(java.lang.String)
meth public java.lang.String getAsText()
meth public void setAsText(java.lang.String)
meth public void setValue(java.lang.Object)
supr java.beans.PropertyEditorSupport
hfds curValue,errorMessage

CLSS public org.netbeans.modules.j2ee.sun.ide.editors.IsolationLevelEditor
cons public init()
cons public init(boolean)
fld public java.lang.String[] choices
fld public java.lang.String[] choicesRuntime
meth public boolean supportsEditingTaggedValues()
meth public java.lang.String[] getTags()
supr org.netbeans.modules.j2ee.sun.ide.editors.ChoiceEditor
hfds defaultChoice,isRuntime

CLSS public org.netbeans.modules.j2ee.sun.ide.editors.IsolationLevelEditorRT
cons public init()
fld public java.lang.String[] choices
meth public boolean supportsEditingTaggedValues()
meth public java.lang.String[] getTags()
supr org.netbeans.modules.j2ee.sun.ide.editors.ChoiceEditor

CLSS public org.netbeans.modules.j2ee.sun.ide.editors.LogLevelEditor
cons public init()
fld public java.lang.String curr_Sel
fld public java.lang.String[] choices
intf org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor
meth public boolean hasInPlaceCustomEditor()
meth public boolean supportsEditingTaggedValues()
meth public java.awt.Component getInPlaceCustomEditor()
meth public java.lang.Object getValue()
meth public java.lang.String getAsText()
meth public java.lang.String getJavaInitializationString()
meth public java.lang.String[] getTags()
meth public void setAsText(java.lang.String)
meth public void setValue(java.lang.Object)
supr java.beans.PropertyEditorSupport

CLSS public org.netbeans.modules.j2ee.sun.ide.editors.LoggingLevelEditor
cons public init()
meth public java.lang.String[] getTags()
supr org.netbeans.modules.j2ee.sun.ide.editors.LogLevelEditor
hfds choices

CLSS public org.netbeans.modules.j2ee.sun.ide.editors.LongEditor
cons public init()
meth public java.lang.String checkValid(java.lang.String)
supr org.netbeans.modules.j2ee.sun.ide.editors.Int0Editor

CLSS public org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair
cons public init()
intf java.io.Serializable
meth public java.lang.String getParamDescription()
meth public java.lang.String getParamName()
meth public java.lang.String getParamValue()
meth public void setParamDescription(java.lang.String)
meth public void setParamName(java.lang.String)
meth public void setParamValue(java.lang.String)
supr java.lang.Object
hfds paramDescription,paramName,paramValue

CLSS public org.netbeans.modules.j2ee.sun.ide.editors.NameValuePairEditor
cons public init()
intf org.netbeans.modules.j2ee.sun.ide.editors.ui.DDTableModelEditor
meth public java.lang.Object getValue()
meth public javax.swing.JPanel getPanel()
meth public void setValue(java.lang.Object)
supr javax.swing.JPanel
hfds bundle,iLabel1,iLabel2,iLabel3,nameField,valueField

CLSS public org.netbeans.modules.j2ee.sun.ide.editors.NameValuePairsPropertyEditor
cons public init(java.lang.Object)
cons public init(org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair[])
fld protected static java.lang.String[] requiredToolTips
innr public ParamModel
meth protected java.lang.String getPaintableString()
meth public boolean isPaintable()
meth public boolean supportsCustomEditor()
meth public java.awt.Component getCustomEditor()
meth public java.lang.Object getValue()
meth public java.lang.String getAsText()
meth public void paintValue(java.awt.Graphics,java.awt.Rectangle)
meth public void setAsText(java.lang.String)
meth public void setValue(java.lang.Object)
supr java.beans.PropertyEditorSupport
hfds bundle,params

CLSS public org.netbeans.modules.j2ee.sun.ide.editors.NameValuePairsPropertyEditor$ParamModel
 outer org.netbeans.modules.j2ee.sun.ide.editors.NameValuePairsPropertyEditor
cons public init(org.netbeans.modules.j2ee.sun.ide.editors.NameValuePairsPropertyEditor,org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair[])
meth protected void setValueAt(java.lang.String,java.lang.Object,int)
meth public boolean isEditValid(java.lang.Object,int)
meth public int getColumnCount()
meth public java.lang.Object getValueAt(int,int)
meth public java.lang.Object makeNewElement()
meth public java.lang.Object[] getValue()
meth public java.lang.String getColumnName(int)
meth public java.lang.String getModelName()
meth public java.util.List isValueValid(java.lang.Object,int)
meth public org.netbeans.modules.j2ee.sun.ide.editors.ui.DDTableModelEditor getEditor()
supr org.netbeans.modules.j2ee.sun.ide.editors.ui.AbstractDDTableModel

CLSS public final org.netbeans.modules.j2ee.sun.ide.editors.NbProxySelector
cons public init()
meth public java.util.List<java.net.Proxy> select(java.net.URI)
meth public void connectFailed(java.net.URI,java.net.SocketAddress,java.io.IOException)
supr java.net.ProxySelector
hfds log,original,useSystemProxies
hcls ProxySettingsListener

CLSS public org.netbeans.modules.j2ee.sun.ide.editors.ProxySettings
cons public init()
fld public final static int AUTO_DETECT_PROXY = 1
fld public final static int DIRECT_CONNECTION = 0
fld public final static int MANUAL_SET_PROXY = 2
fld public final static java.lang.String DIRECT = "DIRECT"
fld public final static java.lang.String NOT_PROXY_HOSTS = "proxyNonProxyHosts"
fld public final static java.lang.String PROXY_AUTHENTICATION_PASSWORD = "proxyAuthenticationPassword"
fld public final static java.lang.String PROXY_AUTHENTICATION_USERNAME = "proxyAuthenticationUsername"
fld public final static java.lang.String PROXY_HTTPS_HOST = "proxyHttpsHost"
fld public final static java.lang.String PROXY_HTTPS_PORT = "proxyHttpsPort"
fld public final static java.lang.String PROXY_HTTP_HOST = "proxyHttpHost"
fld public final static java.lang.String PROXY_HTTP_PORT = "proxyHttpPort"
fld public final static java.lang.String PROXY_SOCKS_HOST = "proxySocksHost"
fld public final static java.lang.String PROXY_SOCKS_PORT = "proxySocksPort"
fld public final static java.lang.String PROXY_TYPE = "proxyType"
fld public final static java.lang.String USE_PROXY_ALL_PROTOCOLS = "useProxyAllProtocols"
fld public final static java.lang.String USE_PROXY_AUTHENTICATION = "useProxyAuthentication"
meth public static boolean useAuthentication()
meth public static boolean useProxyAllProtocols()
meth public static char[] getAuthenticationPassword()
meth public static int getProxyType()
meth public static java.lang.String getAuthenticationUsername()
meth public static java.lang.String getHttpHost()
meth public static java.lang.String getHttpPort()
meth public static java.lang.String getHttpsHost()
meth public static java.lang.String getHttpsPort()
meth public static java.lang.String getNonProxyHosts()
meth public static java.lang.String getSocksHost()
meth public static java.lang.String getSocksPort()
supr java.lang.Object
hfds presetNonProxyHosts
hcls SystemProxySettings

CLSS public org.netbeans.modules.j2ee.sun.ide.editors.ValidationMethodEditor
cons public init()
cons public init(java.lang.String)
fld public java.lang.String[] choices
fld public java.lang.String[] choicesTranx
fld public static java.lang.String curr_Sel
fld public static java.lang.String editorType
intf org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor
meth public boolean hasInPlaceCustomEditor()
meth public boolean supportsEditingTaggedValues()
meth public java.awt.Component getInPlaceCustomEditor()
meth public java.lang.Object getValue()
meth public java.lang.String getAsText()
meth public java.lang.String getJavaInitializationString()
meth public java.lang.String[] getTags()
meth public void setAsText(java.lang.String)
meth public void setValue(java.lang.Object)
supr java.beans.PropertyEditorSupport
hfds TRANX_SUPPORT_TYPE,VALIDATION_TYPE

CLSS public abstract org.netbeans.modules.j2ee.sun.ide.editors.ui.AbstractDDTableModel
cons protected init()
cons public init(java.lang.Object[])
fld protected java.util.Vector data
intf org.netbeans.modules.j2ee.sun.ide.editors.ui.DDTableModel
meth protected abstract void setValueAt(java.lang.String,java.lang.Object,int)
meth protected boolean valueInColumn(java.lang.Object,int,int)
meth protected void changeRefs(java.lang.Object[])
meth public boolean isEditValid(java.lang.Object,int)
meth public int getRowCount()
meth public java.lang.Class getColumnClass(int)
meth public java.lang.Object getValueAt(int)
meth public java.util.List canRemoveRow(int)
meth public void addRowAt(int,java.lang.Object,java.lang.Object)
meth public void editsCancelled()
meth public void newElementCancelled(java.lang.Object)
meth public void removeRowAt(int)
meth public void setValueAt(int,java.lang.Object)
meth public void setValueAt(java.lang.Object,int,int)
supr javax.swing.table.AbstractTableModel

CLSS public abstract interface org.netbeans.modules.j2ee.sun.ide.editors.ui.DDTableModel
intf javax.swing.table.TableModel
meth public abstract boolean isEditValid(java.lang.Object,int)
meth public abstract java.lang.Object getValueAt(int)
meth public abstract java.lang.Object makeNewElement()
meth public abstract java.lang.Object[] getValue()
meth public abstract java.lang.String getModelName()
meth public abstract java.util.List canRemoveRow(int)
meth public abstract java.util.List isValueValid(java.lang.Object,int)
meth public abstract org.netbeans.modules.j2ee.sun.ide.editors.ui.DDTableModelEditor getEditor()
meth public abstract void addRowAt(int,java.lang.Object,java.lang.Object)
meth public abstract void editsCancelled()
meth public abstract void newElementCancelled(java.lang.Object)
meth public abstract void removeRowAt(int)
meth public abstract void setValueAt(int,java.lang.Object)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.ide.editors.ui.DDTableModelEditor
meth public abstract java.lang.Object getValue()
meth public abstract javax.swing.JPanel getPanel()
meth public abstract void setValue(java.lang.Object)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.ide.j2ee.mbmapping.Constants
fld public final static java.lang.String DEBUG_OPTIONS = "debug-options"
fld public final static java.lang.String DEBUG_OPTIONS_ADDRESS = "address="
fld public final static java.lang.String DEF_DEUG_OPTIONS = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=1044"
fld public final static java.lang.String DEF_DEUG_OPTIONS_81 = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=9009"
fld public final static java.lang.String DEF_DEUG_OPTIONS_SHMEM = "-agentlib:jdwp=transport=dt_shmem,server=y,suspend=n,address="
fld public final static java.lang.String DEF_DEUG_OPTIONS_SOCKET = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=11000"
fld public final static java.lang.String ISMEM = "transport=dt_shmem"
fld public final static java.lang.String ISSOCKET = "transport=dt_socket"
fld public final static java.lang.String JAVA_HOME = "java-home"
fld public final static java.lang.String JPDA_PORT = "jpda_port_number"
fld public final static java.lang.String MAP_J2EEAPP_STANDALONE = "com.sun.appserv:type=applications,category=config"
fld public final static java.lang.String MAP_JVMOptions = "com.sun.appserv:type=java-config,config=server-config,category=config"
fld public final static java.lang.String MAP_RESOURCES = "com.sun.appserv:type=resources,category=config"
fld public final static java.lang.String OBJ_J2EE = "com.sun.appserv:j2eeType=J2EEServer,name=server,category=runtime"
fld public final static java.lang.String SHARED_MEM = "shared_memory"
fld public final static java.lang.String[] ADDITIONAL_SERVER_INFO
fld public final static java.lang.String[] CONFIG_MODULE
fld public final static java.lang.String[] JSR_SERVER_INFO

CLSS public org.netbeans.modules.j2ee.sun.ide.j2ee.mbmapping.JvmOptions
cons public init(javax.management.MBeanServerConnection)
cons public init(javax.management.ObjectName,javax.management.MBeanServerConnection)
cons public init(javax.management.ObjectName,javax.management.MBeanServerConnection,boolean)
intf org.netbeans.modules.j2ee.sun.ide.j2ee.mbmapping.Constants
meth public boolean isSharedMemory()
meth public boolean isWindows()
meth public java.lang.String getAddressValue()
meth public java.lang.String getClassPath()
meth public java.lang.String getConfigAttributeValue(java.lang.String)
meth public javax.management.AttributeList getAttributes(java.lang.String[])
meth public javax.management.MBeanInfo getMBeanInfo()
meth public javax.management.ObjectName getConfigObjectName()
meth public void setAddressValue(java.lang.String)
meth public void setAttribute(javax.management.Attribute) throws java.io.IOException,javax.management.AttributeNotFoundException,javax.management.InstanceNotFoundException,javax.management.InvalidAttributeValueException,javax.management.MBeanException,javax.management.ReflectionException
meth public void setClassPath(java.lang.String)
meth public void setDefaultTransportForDebug(java.lang.String)
supr org.netbeans.modules.j2ee.sun.ide.j2ee.mbmapping.ModuleMBean
hfds configObjName,isServerEightOne

CLSS public abstract org.netbeans.modules.j2ee.sun.ide.j2ee.mbmapping.ModuleMBean
cons public init(javax.management.MBeanServerConnection)
cons public init(javax.management.ObjectName)
cons public init(javax.management.ObjectName,javax.management.MBeanServerConnection)
fld protected javax.management.MBeanServerConnection conn
fld protected javax.management.ObjectName runtimeObjName
fld public javax.management.ObjectName configApplicationsObjName
intf org.netbeans.modules.j2ee.sun.ide.j2ee.mbmapping.Constants
meth public abstract javax.management.AttributeList getAttributes(java.lang.String[])
meth public abstract javax.management.MBeanInfo getMBeanInfo()
meth public abstract void setAttribute(javax.management.Attribute) throws java.io.IOException,javax.management.AttributeNotFoundException,javax.management.InstanceNotFoundException,javax.management.InvalidAttributeValueException,javax.management.MBeanException,javax.management.ReflectionException
meth public boolean isUserResource(javax.management.ObjectName)
meth public java.lang.Object invokeOperation(java.lang.String,java.lang.Object[],java.lang.String[])
meth public java.lang.Object start()
meth public java.lang.String getAttribute(javax.management.ObjectName,java.lang.String)
meth public java.lang.String getResourceName(java.lang.String)
meth public javax.management.MBeanAttributeInfo[] setSystemResourceNonEditable(javax.management.MBeanAttributeInfo[])
meth public javax.management.ObjectName getConfigObjectName(java.lang.String,java.lang.String)
meth public javax.management.ObjectName getRequiredObjectName(javax.management.ObjectName,javax.management.ObjectName,java.lang.String)
meth public javax.management.ObjectName getRequiredObjectName(javax.management.ObjectName,javax.management.ObjectName,javax.management.Attribute)
meth public javax.management.ObjectName setApplicationsObjectName()
meth public void restart()
meth public void setConnection(javax.management.MBeanServerConnection)
meth public void stop()
supr java.lang.Object

CLSS public org.netbeans.modules.j2ee.sun.ide.j2ee.mbmapping.ServerInfo
cons public init(javax.management.MBeanServerConnection)
intf org.netbeans.modules.j2ee.sun.ide.j2ee.mbmapping.Constants
meth public boolean isRestartRequired()
meth public java.lang.String getDomain()
meth public java.lang.String getHiddenPassword()
meth public java.lang.String getPassword()
meth public java.lang.String getPort()
meth public java.lang.String getRuntimeAttributeValue(java.lang.String)
meth public java.lang.String getUserName()
meth public javax.management.AttributeList getAttributes(java.lang.String[])
meth public javax.management.MBeanInfo getMBeanInfo()
meth public void setAttribute(javax.management.Attribute) throws java.io.IOException,javax.management.AttributeNotFoundException,javax.management.InstanceNotFoundException,javax.management.InvalidAttributeValueException,javax.management.MBeanException,javax.management.ReflectionException
meth public void setAttributes(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
supr org.netbeans.modules.j2ee.sun.ide.j2ee.mbmapping.ModuleMBean
hfds DOMAIN,PORT,domain,password,port,username

CLSS public org.netbeans.modules.j2ee.sun.ide.j2ee.mbmapping.Utils
cons public init()
meth public static boolean isUserResource(javax.management.ObjectName,javax.management.MBeanServerConnection)
meth public static java.lang.Object[] getStringParam(java.lang.String)
meth public static java.lang.String getAttribute(javax.management.ObjectName,java.lang.String,javax.management.MBeanServerConnection)
meth public static java.lang.String[] getStringSignature()
meth public static javax.management.ObjectName getRequiredObjectName(javax.management.ObjectName,javax.management.ObjectName,java.lang.String,javax.management.MBeanServerConnection)
meth public static javax.management.ObjectName getRequiredObjectName(javax.management.ObjectName,javax.management.ObjectName,javax.management.Attribute,javax.management.MBeanServerConnection)
supr java.lang.Object

CLSS public org.netbeans.modules.j2ee.sun.sunresources.beans.DatabaseUtils
meth public static java.lang.String getDSClassName(java.lang.String)
meth public static java.lang.String getDriverName(java.lang.String)
meth public static java.lang.String getUrlPrefix(java.lang.String,java.lang.String)
supr java.lang.Object
hfds cpClassMap,driverMap,dsClassMap

CLSS public org.netbeans.modules.j2ee.sun.sunresources.beans.Field
cons public init()
cons public init(int)
fld public final static java.lang.String FIELDTYPE = "FieldType"
fld public final static java.lang.String FIELD_VALUE = "FieldValue"
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String REQUIRED = "Required"
fld public final static java.lang.String TAG = "Tag"
meth public java.lang.String dumpBeanNode()
meth public java.lang.String getFieldType()
meth public java.lang.String getName()
meth public java.lang.String getRequired()
meth public org.netbeans.modules.j2ee.sun.sunresources.beans.FieldValue getFieldValue()
meth public org.netbeans.modules.j2ee.sun.sunresources.beans.Tag getTag()
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void setFieldType(java.lang.String)
meth public void setFieldValue(org.netbeans.modules.j2ee.sun.sunresources.beans.FieldValue)
meth public void setName(java.lang.String)
meth public void setRequired(java.lang.String)
meth public void setTag(org.netbeans.modules.j2ee.sun.sunresources.beans.Tag)
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.schema2beans.BaseBean
hfds comparators

CLSS public org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroup
cons public init()
cons public init(int)
fld public final static java.lang.String FIELD = "Field"
fld public final static java.lang.String NAME = "Name"
meth public int addField(org.netbeans.modules.j2ee.sun.sunresources.beans.Field)
meth public int removeField(org.netbeans.modules.j2ee.sun.sunresources.beans.Field)
meth public int sizeField()
meth public java.lang.String dumpBeanNode()
meth public java.lang.String getName()
meth public org.netbeans.modules.j2ee.sun.sunresources.beans.Field getField(int)
meth public org.netbeans.modules.j2ee.sun.sunresources.beans.Field[] getField()
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void setField(int,org.netbeans.modules.j2ee.sun.sunresources.beans.Field)
meth public void setField(org.netbeans.modules.j2ee.sun.sunresources.beans.Field[])
meth public void setName(java.lang.String)
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.schema2beans.BaseBean
hfds comparators

CLSS public org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroupHelper
cons public init()
meth public static org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroup getFieldGroup(org.netbeans.modules.j2ee.sun.sunresources.beans.Wizard,java.lang.String)
supr java.lang.Object

CLSS public org.netbeans.modules.j2ee.sun.sunresources.beans.FieldHelper
cons public init()
meth public static boolean isInt(org.netbeans.modules.j2ee.sun.sunresources.beans.Field)
meth public static boolean isList(org.netbeans.modules.j2ee.sun.sunresources.beans.Field)
meth public static boolean isTextArea(org.netbeans.modules.j2ee.sun.sunresources.beans.Field)
meth public static java.lang.String getConditionalFieldValue(org.netbeans.modules.j2ee.sun.sunresources.beans.Field,java.lang.String)
meth public static java.lang.String getDefaultValue(org.netbeans.modules.j2ee.sun.sunresources.beans.Field)
meth public static java.lang.String getFieldType(org.netbeans.modules.j2ee.sun.sunresources.beans.Field)
meth public static java.lang.String getOptionNameFromValue(org.netbeans.modules.j2ee.sun.sunresources.beans.Field,java.lang.String)
meth public static java.lang.String getReplacedConditionalFieldValue(org.netbeans.modules.j2ee.sun.sunresources.beans.Field,java.lang.String)
meth public static java.lang.String toUrl(java.lang.String)
meth public static java.lang.String[] getFieldNames(org.netbeans.modules.j2ee.sun.sunresources.beans.Wizard)
meth public static java.lang.String[] getRemainingFieldNames(org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroup,java.util.Vector)
meth public static java.lang.String[] getTags(org.netbeans.modules.j2ee.sun.sunresources.beans.Field)
meth public static org.netbeans.modules.j2ee.sun.sunresources.beans.Field getField(org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroup,java.lang.String)
supr java.lang.Object

CLSS public org.netbeans.modules.j2ee.sun.sunresources.beans.FieldValue
cons public init()
cons public init(int)
fld public final static java.lang.String DEFAULT_FIELD_VALUE = "DefaultFieldValue"
fld public final static java.lang.String OPTION_VALUE_PAIR = "OptionValuePair"
meth public int addOptionValuePair(org.netbeans.modules.j2ee.sun.sunresources.beans.OptionValuePair)
meth public int removeOptionValuePair(org.netbeans.modules.j2ee.sun.sunresources.beans.OptionValuePair)
meth public int sizeOptionValuePair()
meth public java.lang.String dumpBeanNode()
meth public java.lang.String getDefaultFieldValue()
meth public org.netbeans.modules.j2ee.sun.sunresources.beans.OptionValuePair getOptionValuePair(int)
meth public org.netbeans.modules.j2ee.sun.sunresources.beans.OptionValuePair[] getOptionValuePair()
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void setDefaultFieldValue(java.lang.String)
meth public void setOptionValuePair(int,org.netbeans.modules.j2ee.sun.sunresources.beans.OptionValuePair)
meth public void setOptionValuePair(org.netbeans.modules.j2ee.sun.sunresources.beans.OptionValuePair[])
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.schema2beans.BaseBean
hfds comparators

CLSS public org.netbeans.modules.j2ee.sun.sunresources.beans.OptionValuePair
cons public init()
cons public init(int)
fld public final static java.lang.String CONDITIONAL_VALUE = "ConditionalValue"
fld public final static java.lang.String OPTION_NAME = "OptionName"
meth public java.lang.String dumpBeanNode()
meth public java.lang.String getConditionalValue()
meth public java.lang.String getOptionName()
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void setConditionalValue(java.lang.String)
meth public void setOptionName(java.lang.String)
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.schema2beans.BaseBean
hfds comparators

CLSS public org.netbeans.modules.j2ee.sun.sunresources.beans.Tag
cons public init()
cons public init(int)
fld public final static java.lang.String TAG_ITEM = "TagItem"
meth public int addTagItem(java.lang.String)
meth public int removeTagItem(java.lang.String)
meth public int sizeTagItem()
meth public java.lang.String dumpBeanNode()
meth public java.lang.String getTagItem(int)
meth public java.lang.String[] getTagItem()
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void setTagItem(int,java.lang.String)
meth public void setTagItem(java.lang.String[])
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.schema2beans.BaseBean
hfds comparators

CLSS public org.netbeans.modules.j2ee.sun.sunresources.beans.Wizard
cons public init() throws org.netbeans.modules.schema2beans.Schema2BeansException
cons public init(int)
cons public init(org.w3c.dom.Node,int) throws org.netbeans.modules.schema2beans.Schema2BeansException
fld public final static java.lang.String FIELD_GROUP = "FieldGroup"
fld public final static java.lang.String NAME = "Name"
meth protected void initFromNode(org.w3c.dom.Node,int) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth protected void initOptions(int)
meth public int addFieldGroup(org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroup)
meth public int removeFieldGroup(org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroup)
meth public int sizeFieldGroup()
meth public java.lang.String dumpBeanNode()
meth public java.lang.String getName()
meth public org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroup getFieldGroup(int)
meth public org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroup[] getFieldGroup()
meth public static org.netbeans.modules.j2ee.sun.sunresources.beans.Wizard createGraph()
meth public static org.netbeans.modules.j2ee.sun.sunresources.beans.Wizard createGraph(java.io.InputStream) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public static org.netbeans.modules.j2ee.sun.sunresources.beans.Wizard createGraph(java.io.InputStream,boolean) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public static org.netbeans.modules.j2ee.sun.sunresources.beans.Wizard createGraph(org.w3c.dom.Node) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void setFieldGroup(int,org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroup)
meth public void setFieldGroup(org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroup[])
meth public void setName(java.lang.String)
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.schema2beans.BaseBean
hfds comparators

CLSS public abstract interface org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants
fld public final static java.lang.String MAP_RESOURCES = "com.sun.appserv:type=resources,category=config"
fld public final static java.lang.String QUEUE_PROP = "PhysicalQueue"
fld public final static java.lang.String TOPIC_PROP = "PhysicalTopic"
fld public final static java.lang.String __ADMINOBJResource = "jms/myQueue"
fld public final static java.lang.String __AdminObjPropertyName = "Name"
fld public final static java.lang.String __AdminObjResAdapterName = "res-adapter"
fld public final static java.lang.String __AllowNonComponentCallers = "allow-non-component-callers"
fld public final static java.lang.String __CNTN_FACTORY = "javax.jms.ConnectionFactory"
fld public final static java.lang.String __CONNECTORResource = "jms/myConnector"
fld public final static java.lang.String __CPDatasourceClassname = "datasource-classname-cp"
fld public final static java.lang.String __ConnPoolSuffix = "Pool"
fld public final static java.lang.String __ConnPoolSuffixJMS = "-Connection-Pool"
fld public final static java.lang.String __ConnectionPoolResource = "connectionPool"
fld public final static java.lang.String __ConnectionValidationMethod = "connection-validation-method"
fld public final static java.lang.String __ConnectorPoolConnDefName = "connection-definition-name"
fld public final static java.lang.String __ConnectorPoolResAdName = "resource-adapter-name"
fld public final static java.lang.String __CreateAdmObj = "createAdminObjectResource"
fld public final static java.lang.String __CreateCP = "createJdbcConnectionPool"
fld public final static java.lang.String __CreateConnPool = "createConnectorConnectionPool"
fld public final static java.lang.String __CreateConnector = "createConnectorResource"
fld public final static java.lang.String __CreateDS = "createJdbcResource"
fld public final static java.lang.String __CreateMail = "createMailResource"
fld public final static java.lang.String __CreatePMF = "createPersistenceManagerFactoryResource"
fld public final static java.lang.String __DatabaseName = "databaseName"
fld public final static java.lang.String __DatabaseVendor = "database-vendor"
fld public final static java.lang.String __DatasourceClassname = "datasource-classname"
fld public final static java.lang.String __Debug = "debug"
fld public final static java.lang.String __DerbyConnAttr = "connectionAttributes"
fld public final static java.lang.String __DerbyDatabaseName = "DatabaseName"
fld public final static java.lang.String __DerbyPortNumber = "PortNumber"
fld public final static java.lang.String __Description = "description"
fld public final static java.lang.String __DriverClass = "driverClass"
fld public final static java.lang.String __DynamicWizPanel = "dynamicPanel"
fld public final static java.lang.String __Enabled = "enabled"
fld public final static java.lang.String __FactoryClass = "factory-class"
fld public final static java.lang.String __FailAllConnections = "fail-all-connections"
fld public final static java.lang.String __FirstStepChoose = "Choose"
fld public final static java.lang.String __From = "from"
fld public final static java.lang.String __General = "general"
fld public final static java.lang.String __GetAdmObjResource = "getAdminObjectResource"
fld public final static java.lang.String __GetConnPoolResource = "getConnectorConnectionPool"
fld public final static java.lang.String __GetConnectorResource = "getConnectorResource"
fld public final static java.lang.String __GetJdbcConnectionPool = "getJdbcConnectionPool"
fld public final static java.lang.String __GetJdbcConnectionPoolByName = "getJdbcConnectionPoolByName"
fld public final static java.lang.String __GetJdbcResource = "getJdbcResource"
fld public final static java.lang.String __GetJdbcResourceByName = "getJdbcResourceByJndiName"
fld public final static java.lang.String __GetJmsResource = "getJmsResource"
fld public final static java.lang.String __GetMailResource = "getMailResource"
fld public final static java.lang.String __GetPMFResource = "getPersistenceManagerFactoryResource"
fld public final static java.lang.String __GetProperties = "getProperties"
fld public final static java.lang.String __Host = "host"
fld public final static java.lang.String __IdleTimeoutInSeconds = "idle-timeout-in-seconds"
fld public final static java.lang.String __InformixHostName = "IfxIFXHOST"
fld public final static java.lang.String __InformixServer = "InformixServer"
fld public final static java.lang.String __IsCPExisting = "is-cp-existing"
fld public final static java.lang.String __IsConnectionValidationRequired = "is-connection-validation-required"
fld public final static java.lang.String __IsIsolationLevelGuaranteed = "is-isolation-level-guaranteed"
fld public final static java.lang.String __IsXA = "isXA"
fld public final static java.lang.String __JDBCResource = "jdbc/myDatasource"
fld public final static java.lang.String __JMSResource = "jms/myQueue"
fld public final static java.lang.String __JavaMessageJndiName = "jndi_name"
fld public final static java.lang.String __JavaMessageResType = "res-type"
fld public final static java.lang.String __JdbcConnectionPool = "jdbc-connection-pool"
fld public final static java.lang.String __JdbcObjectType = "object-type"
fld public final static java.lang.String __JdbcResource = "jdbc-resource"
fld public final static java.lang.String __JdbcResourceJndiName = "jdbc-resource-jndi-name"
fld public final static java.lang.String __JmsResAdapter = "jmsra"
fld public final static java.lang.String __JmsResource = "jms-resource"
fld public final static java.lang.String __JndiName = "jndi-name"
fld public final static java.lang.String __MAILResource = "mail/mySession"
fld public final static java.lang.String __MailResource = "mail-resource"
fld public final static java.lang.String __MailUser = "user"
fld public final static java.lang.String __MaxPoolSize = "max-pool-size"
fld public final static java.lang.String __MaxWaitTimeInMillis = "max-wait-time-in-millis"
fld public final static java.lang.String __Name = "name"
fld public final static java.lang.String __NonTransactionalConnections = "non-transactional-connections"
fld public final static java.lang.String __NotApplicable = "NA"
fld public final static java.lang.String __Password = "Password"
fld public final static java.lang.String __PersistenceManagerFactoryResource = "persistence-manager-factory-resource"
fld public final static java.lang.String __PersistenceResource = "persistence"
fld public final static java.lang.String __PoolName = "pool-name"
fld public final static java.lang.String __PoolResizeQuantity = "pool-resize-quantity"
fld public final static java.lang.String __PortNumber = "portNumber"
fld public final static java.lang.String __Properties = "properties"
fld public final static java.lang.String __Properties2 = "properties2"
fld public final static java.lang.String __PropertiesURL = "propertiesUrl"
fld public final static java.lang.String __QUEUE = "javax.jms.Queue"
fld public final static java.lang.String __QUEUE_CNTN_FACTORY = "javax.jms.QueueConnectionFactory"
fld public final static java.lang.String __ResType = "res-type"
fld public final static java.lang.String __SID = "SID"
fld public final static java.lang.String __ServerName = "serverName"
fld public final static java.lang.String __SetProperty = "setProperty"
fld public final static java.lang.String __SteadyPoolSize = "steady-pool-size"
fld public final static java.lang.String __StoreProtocol = "store-protocol"
fld public final static java.lang.String __StoreProtocolClass = "store-protocol-class"
fld public final static java.lang.String __SunResourceExt = "sun-resource"
fld public final static java.lang.String __SunResourceFolder = "setup"
fld public final static java.lang.String __TOPIC = "javax.jms.Topic"
fld public final static java.lang.String __TOPIC_CNTN_FACTORY = "javax.jms.TopicConnectionFactory"
fld public final static java.lang.String __TransactionIsolationLevel = "transaction-isolation-level"
fld public final static java.lang.String __TransportProtocol = "transport-protocol"
fld public final static java.lang.String __TransportProtocolClass = "transport-protocol-class"
fld public final static java.lang.String __Type_ConnectionPoolDataSource = "javax.sql.ConnectionPoolDataSource"
fld public final static java.lang.String __Type_Datasource = "javax.sql.DataSource"
fld public final static java.lang.String __Type_XADatasource = "javax.sql.XADataSource"
fld public final static java.lang.String __Url = "URL"
fld public final static java.lang.String __User = "User"
fld public final static java.lang.String __ValidationTableName = "validation-table-name"
fld public final static java.lang.String __XADatasourceClassname = "datasource-classname-xa"
fld public final static java.lang.String[] Reqd_DBName
fld public final static java.lang.String[] VendorsDBNameProp
fld public final static java.lang.String[] VendorsExtraProps

CLSS public abstract org.netbeans.modules.schema2beans.BaseBean
cons public init(java.util.Vector,org.netbeans.modules.schema2beans.Version)
fld protected org.netbeans.modules.schema2beans.DOMBinding binding
fld protected org.netbeans.modules.schema2beans.GraphManager graphManager
fld public final static int MERGE_COMPARE = 4
fld public final static int MERGE_INTERSECT = 1
fld public final static int MERGE_NONE = 0
fld public final static int MERGE_UNION = 2
fld public final static int MERGE_UPDATE = 3
innr public IterateChoiceProperties
intf java.lang.Cloneable
intf org.netbeans.modules.schema2beans.Bean
meth protected boolean hasDomNode()
meth protected int addValue(org.netbeans.modules.schema2beans.BeanProp,java.lang.Object)
meth protected int removeValue(org.netbeans.modules.schema2beans.BeanProp,java.lang.Object)
meth protected java.util.Iterator beanPropsIterator()
meth protected org.netbeans.modules.schema2beans.DOMBinding domBinding()
meth protected void addKnownValue(java.lang.String,java.lang.Object)
meth protected void buildPathName(java.lang.StringBuffer)
meth protected void copyProperty(org.netbeans.modules.schema2beans.BeanProp,org.netbeans.modules.schema2beans.BaseBean,int,java.lang.Object)
meth protected void init(java.util.Vector,org.netbeans.modules.schema2beans.Version)
meth protected void initPropertyTables(int)
meth protected void removeValue(org.netbeans.modules.schema2beans.BeanProp,int)
meth protected void setDomBinding(org.netbeans.modules.schema2beans.DOMBinding)
meth protected void setGraphManager(org.netbeans.modules.schema2beans.GraphManager)
meth protected void setValue(org.netbeans.modules.schema2beans.BeanProp,int,java.lang.Object)
meth public abstract void dump(java.lang.StringBuffer,java.lang.String)
meth public boolean hasName(java.lang.String)
meth public boolean isChoiceProperty()
meth public boolean isChoiceProperty(java.lang.String)
meth public boolean isEqualTo(java.lang.Object)
meth public boolean isNull(java.lang.String)
meth public boolean isNull(java.lang.String,int)
meth public boolean isRoot()
meth public int addValue(java.lang.String,java.lang.Object)
meth public int idToIndex(java.lang.String,int)
meth public int indexOf(java.lang.String,java.lang.Object)
meth public int indexToId(java.lang.String,int)
meth public int removeValue(java.lang.String,java.lang.Object)
meth public int size(java.lang.String)
meth public java.lang.Object clone()
meth public java.lang.Object getValue(java.lang.String)
meth public java.lang.Object getValue(java.lang.String,int)
meth public java.lang.Object getValueById(java.lang.String,int)
meth public java.lang.Object[] getValues(java.lang.String)
meth public java.lang.Object[] knownValues(java.lang.String)
meth public java.lang.String _getXPathExpr()
meth public java.lang.String _getXPathExpr(java.lang.Object)
meth public java.lang.String dtdName()
meth public java.lang.String dumpBeanNode()
meth public java.lang.String dumpDomNode()
meth public java.lang.String dumpDomNode(int)
meth public java.lang.String dumpDomNode(java.lang.String,int)
meth public java.lang.String fullName()
meth public java.lang.String getAttributeValue(java.lang.String)
meth public java.lang.String getAttributeValue(java.lang.String,int,java.lang.String)
meth public java.lang.String getAttributeValue(java.lang.String,java.lang.String)
meth public java.lang.String getDefaultNamespace()
meth public java.lang.String name()
meth public java.lang.String nameChild(java.lang.Object)
meth public java.lang.String nameChild(java.lang.Object,boolean,boolean)
meth public java.lang.String nameChild(java.lang.Object,boolean,boolean,boolean)
meth public java.lang.String nameSelf()
meth public java.lang.String toString()
meth public java.lang.String[] findAttributeValue(java.lang.String,java.lang.String)
meth public java.lang.String[] findPropertyValue(java.lang.String,java.lang.Object)
meth public java.lang.String[] findValue(java.lang.Object)
meth public java.lang.String[] getAttributeNames()
meth public java.lang.String[] getAttributeNames(java.lang.String)
meth public java.util.Iterator listChoiceProperties()
meth public org.netbeans.modules.schema2beans.BaseAttribute[] listAttributes()
meth public org.netbeans.modules.schema2beans.BaseAttribute[] listAttributes(java.lang.String)
meth public org.netbeans.modules.schema2beans.BaseBean newInstance(java.lang.String)
meth public org.netbeans.modules.schema2beans.BaseBean parent()
meth public org.netbeans.modules.schema2beans.BaseBean[] childBeans(boolean)
meth public org.netbeans.modules.schema2beans.BaseProperty getProperty()
meth public org.netbeans.modules.schema2beans.BaseProperty getProperty(java.lang.String)
meth public org.netbeans.modules.schema2beans.BaseProperty[] listChoiceProperties(java.lang.String)
meth public org.netbeans.modules.schema2beans.BaseProperty[] listProperties()
meth public org.netbeans.modules.schema2beans.Bean _getParent()
meth public org.netbeans.modules.schema2beans.Bean _getRoot()
meth public org.netbeans.modules.schema2beans.Bean propertyById(java.lang.String,int)
meth public org.netbeans.modules.schema2beans.BeanProp beanProp()
meth public org.netbeans.modules.schema2beans.BeanProp beanProp(int)
meth public org.netbeans.modules.schema2beans.BeanProp beanProp(java.lang.String)
meth public org.netbeans.modules.schema2beans.BeanProp[] beanProps()
meth public org.netbeans.modules.schema2beans.GraphManager graphManager()
meth public org.w3c.dom.Comment addComment(java.lang.String)
meth public org.w3c.dom.Comment[] comments()
meth public static org.netbeans.modules.schema2beans.BaseBean createGraph(java.lang.Class,java.io.InputStream) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public static org.netbeans.modules.schema2beans.BaseBean createGraph(java.lang.Class,java.io.InputStream,boolean) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public static org.netbeans.modules.schema2beans.BaseBean createGraph(java.lang.Class,java.io.InputStream,boolean,org.xml.sax.EntityResolver) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public static org.netbeans.modules.schema2beans.BaseBean createGraph(java.lang.Class,java.io.InputStream,boolean,org.xml.sax.EntityResolver,org.xml.sax.ErrorHandler) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public void _setChanged(boolean)
meth public void addBeanComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void changeDocType(java.lang.String,java.lang.String)
meth public void childBeans(boolean,java.util.List)
meth public void createAttribute(java.lang.String,java.lang.String,int,java.lang.String[],java.lang.String)
meth public void createAttribute(java.lang.String,java.lang.String,java.lang.String,int,java.lang.String[],java.lang.String)
meth public void createBean(org.w3c.dom.Node,org.netbeans.modules.schema2beans.GraphManager)
meth public void createProperty(java.lang.String,java.lang.String,int,java.lang.Class)
meth public void createProperty(java.lang.String,java.lang.String,java.lang.Class)
meth public void createRoot(java.lang.String,java.lang.String,int,java.lang.Class)
meth public void dumpAttributes(java.lang.String,int,java.lang.StringBuffer,java.lang.String)
meth public void dumpXml()
meth public void merge(org.netbeans.modules.schema2beans.BaseBean)
meth public void merge(org.netbeans.modules.schema2beans.BaseBean,int)
meth public void mergeUpdate(org.netbeans.modules.schema2beans.BaseBean)
meth public void reindent()
meth public void reindent(java.lang.String)
meth public void removeBeanComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void removeComment(org.w3c.dom.Comment)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void removeValue(java.lang.String,int)
meth public void setAttributeValue(java.lang.String,int,java.lang.String,java.lang.String)
meth public void setAttributeValue(java.lang.String,java.lang.String)
meth public void setAttributeValue(java.lang.String,java.lang.String,java.lang.String)
meth public void setDefaultNamespace(java.lang.String)
meth public void setValue(java.lang.String,int,java.lang.Object)
meth public void setValue(java.lang.String,java.lang.Object)
meth public void setValue(java.lang.String,java.lang.Object[])
meth public void setValueById(java.lang.String,int,java.lang.Object)
meth public void write(java.io.File) throws java.io.IOException
meth public void write(java.io.OutputStream) throws java.io.IOException
meth public void write(java.io.OutputStream,java.lang.String) throws java.io.IOException,org.netbeans.modules.schema2beans.Schema2BeansException
meth public void write(java.io.Writer) throws java.io.IOException,org.netbeans.modules.schema2beans.Schema2BeansException
meth public void write(java.io.Writer,java.lang.String) throws java.io.IOException,org.netbeans.modules.schema2beans.Schema2BeansException
meth public void writeNoReindent(java.io.OutputStream) throws java.io.IOException,org.netbeans.modules.schema2beans.Schema2BeansException
meth public void writeNode(java.io.Writer) throws java.io.IOException
supr java.lang.Object
hfds attrCache,changeListeners,comparators,defaultNamespace,isRoot,propByName,propByOrder,propertyOrder

CLSS public abstract interface org.netbeans.modules.schema2beans.Bean
meth public abstract boolean hasName(java.lang.String)
meth public abstract boolean isRoot()
meth public abstract int addValue(java.lang.String,java.lang.Object)
meth public abstract int idToIndex(java.lang.String,int)
meth public abstract int indexToId(java.lang.String,int)
meth public abstract int removeValue(java.lang.String,java.lang.Object)
meth public abstract java.lang.Object getValue(java.lang.String)
meth public abstract java.lang.Object getValue(java.lang.String,int)
meth public abstract java.lang.Object getValueById(java.lang.String,int)
meth public abstract java.lang.Object[] getValues(java.lang.String)
meth public abstract java.lang.String dtdName()
meth public abstract java.lang.String name()
meth public abstract org.netbeans.modules.schema2beans.BaseProperty getProperty()
meth public abstract org.netbeans.modules.schema2beans.BaseProperty getProperty(java.lang.String)
meth public abstract org.netbeans.modules.schema2beans.BaseProperty[] listProperties()
meth public abstract org.netbeans.modules.schema2beans.Bean _getParent()
meth public abstract org.netbeans.modules.schema2beans.Bean _getRoot()
meth public abstract org.netbeans.modules.schema2beans.Bean propertyById(java.lang.String,int)
meth public abstract org.netbeans.modules.schema2beans.BeanProp beanProp(java.lang.String)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void childBeans(boolean,java.util.List)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setValue(java.lang.String,int,java.lang.Object)
meth public abstract void setValue(java.lang.String,java.lang.Object)
meth public abstract void setValueById(java.lang.String,int,java.lang.Object)

CLSS public abstract interface org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor
 anno 0 java.lang.Deprecated()
intf java.beans.PropertyEditor
meth public abstract boolean hasInPlaceCustomEditor()
meth public abstract boolean supportsEditingTaggedValues()
meth public abstract java.awt.Component getInPlaceCustomEditor()

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

