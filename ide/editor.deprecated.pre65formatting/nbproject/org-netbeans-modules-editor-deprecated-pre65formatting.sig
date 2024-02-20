#Signature file v4.1
#Version 1.54.0

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

CLSS public abstract interface java.awt.ItemSelectable
meth public abstract java.lang.Object[] getSelectedObjects()
meth public abstract void addItemListener(java.awt.event.ItemListener)
meth public abstract void removeItemListener(java.awt.event.ItemListener)

CLSS public abstract interface java.awt.MenuContainer
meth public abstract boolean postEvent(java.awt.Event)
 anno 0 java.lang.Deprecated()
meth public abstract java.awt.Font getFont()
meth public abstract void remove(java.awt.MenuComponent)

CLSS public abstract interface java.awt.event.ActionListener
intf java.util.EventListener
meth public abstract void actionPerformed(java.awt.event.ActionEvent)

CLSS public abstract interface java.awt.event.FocusListener
intf java.util.EventListener
meth public abstract void focusGained(java.awt.event.FocusEvent)
meth public abstract void focusLost(java.awt.event.FocusEvent)

CLSS public abstract interface java.awt.event.KeyListener
intf java.util.EventListener
meth public abstract void keyPressed(java.awt.event.KeyEvent)
meth public abstract void keyReleased(java.awt.event.KeyEvent)
meth public abstract void keyTyped(java.awt.event.KeyEvent)

CLSS public abstract interface java.awt.event.MouseListener
intf java.util.EventListener
meth public abstract void mouseClicked(java.awt.event.MouseEvent)
meth public abstract void mouseEntered(java.awt.event.MouseEvent)
meth public abstract void mouseExited(java.awt.event.MouseEvent)
meth public abstract void mousePressed(java.awt.event.MouseEvent)
meth public abstract void mouseReleased(java.awt.event.MouseEvent)

CLSS public abstract interface java.awt.event.MouseMotionListener
intf java.util.EventListener
meth public abstract void mouseDragged(java.awt.event.MouseEvent)
meth public abstract void mouseMoved(java.awt.event.MouseEvent)

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

CLSS public abstract interface java.beans.BeanInfo
fld public final static int ICON_COLOR_16x16 = 1
fld public final static int ICON_COLOR_32x32 = 2
fld public final static int ICON_MONO_16x16 = 3
fld public final static int ICON_MONO_32x32 = 4
meth public abstract int getDefaultEventIndex()
meth public abstract int getDefaultPropertyIndex()
meth public abstract java.awt.Image getIcon(int)
meth public abstract java.beans.BeanDescriptor getBeanDescriptor()
meth public abstract java.beans.BeanInfo[] getAdditionalBeanInfo()
meth public abstract java.beans.EventSetDescriptor[] getEventSetDescriptors()
meth public abstract java.beans.MethodDescriptor[] getMethodDescriptors()
meth public abstract java.beans.PropertyDescriptor[] getPropertyDescriptors()

CLSS public abstract interface java.beans.PropertyChangeListener
intf java.util.EventListener
meth public abstract void propertyChange(java.beans.PropertyChangeEvent)

CLSS public java.beans.SimpleBeanInfo
cons public init()
intf java.beans.BeanInfo
meth public int getDefaultEventIndex()
meth public int getDefaultPropertyIndex()
meth public java.awt.Image getIcon(int)
meth public java.awt.Image loadImage(java.lang.String)
meth public java.beans.BeanDescriptor getBeanDescriptor()
meth public java.beans.BeanInfo[] getAdditionalBeanInfo()
meth public java.beans.EventSetDescriptor[] getEventSetDescriptors()
meth public java.beans.MethodDescriptor[] getMethodDescriptors()
meth public java.beans.PropertyDescriptor[] getPropertyDescriptors()
supr java.lang.Object

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

CLSS public abstract interface java.io.Externalizable
intf java.io.Serializable
meth public abstract void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public abstract void writeExternal(java.io.ObjectOutput) throws java.io.IOException

CLSS public abstract interface java.io.Flushable
meth public abstract void flush() throws java.io.IOException

CLSS public abstract java.io.Reader
cons protected init()
cons protected init(java.lang.Object)
fld protected java.lang.Object lock
intf java.io.Closeable
intf java.lang.Readable
meth public abstract int read(char[],int,int) throws java.io.IOException
meth public abstract void close() throws java.io.IOException
meth public boolean markSupported()
meth public boolean ready() throws java.io.IOException
meth public int read() throws java.io.IOException
meth public int read(char[]) throws java.io.IOException
meth public int read(java.nio.CharBuffer) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
meth public void mark(int) throws java.io.IOException
meth public void reset() throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface java.io.Serializable

CLSS public abstract java.io.Writer
cons protected init()
cons protected init(java.lang.Object)
fld protected java.lang.Object lock
intf java.io.Closeable
intf java.io.Flushable
intf java.lang.Appendable
meth public abstract void close() throws java.io.IOException
meth public abstract void flush() throws java.io.IOException
meth public abstract void write(char[],int,int) throws java.io.IOException
meth public java.io.Writer append(char) throws java.io.IOException
meth public java.io.Writer append(java.lang.CharSequence) throws java.io.IOException
meth public java.io.Writer append(java.lang.CharSequence,int,int) throws java.io.IOException
meth public void write(char[]) throws java.io.IOException
meth public void write(int) throws java.io.IOException
meth public void write(java.lang.String) throws java.io.IOException
meth public void write(java.lang.String,int,int) throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface java.lang.Appendable
meth public abstract java.lang.Appendable append(char) throws java.io.IOException
meth public abstract java.lang.Appendable append(java.lang.CharSequence) throws java.io.IOException
meth public abstract java.lang.Appendable append(java.lang.CharSequence,int,int) throws java.io.IOException

CLSS public abstract interface java.lang.AutoCloseable
meth public abstract void close() throws java.lang.Exception

CLSS public abstract interface java.lang.Cloneable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

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

CLSS public abstract interface java.lang.Readable
meth public abstract int read(java.nio.CharBuffer) throws java.io.IOException

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

CLSS public abstract interface java.text.CharacterIterator
fld public final static char DONE = '\uffff'
intf java.lang.Cloneable
meth public abstract char current()
meth public abstract char first()
meth public abstract char last()
meth public abstract char next()
meth public abstract char previous()
meth public abstract char setIndex(int)
meth public abstract int getBeginIndex()
meth public abstract int getEndIndex()
meth public abstract int getIndex()
meth public abstract java.lang.Object clone()

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

CLSS public abstract java.util.Dictionary<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
meth public abstract boolean isEmpty()
meth public abstract int size()
meth public abstract java.util.Enumeration<{java.util.Dictionary%0}> keys()
meth public abstract java.util.Enumeration<{java.util.Dictionary%1}> elements()
meth public abstract {java.util.Dictionary%1} get(java.lang.Object)
meth public abstract {java.util.Dictionary%1} put({java.util.Dictionary%0},{java.util.Dictionary%1})
meth public abstract {java.util.Dictionary%1} remove(java.lang.Object)
supr java.lang.Object

CLSS public abstract interface java.util.EventListener

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object

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

CLSS public java.util.Hashtable<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
cons public init(int)
cons public init(int,float)
cons public init(java.util.Map<? extends {java.util.Hashtable%0},? extends {java.util.Hashtable%1}>)
intf java.io.Serializable
intf java.lang.Cloneable
intf java.util.Map<{java.util.Hashtable%0},{java.util.Hashtable%1}>
meth protected void rehash()
meth public boolean contains(java.lang.Object)
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public boolean remove(java.lang.Object,java.lang.Object)
meth public boolean replace({java.util.Hashtable%0},{java.util.Hashtable%1},{java.util.Hashtable%1})
meth public int hashCode()
meth public int size()
meth public java.lang.Object clone()
meth public java.lang.String toString()
meth public java.util.Collection<{java.util.Hashtable%1}> values()
meth public java.util.Enumeration<{java.util.Hashtable%0}> keys()
meth public java.util.Enumeration<{java.util.Hashtable%1}> elements()
meth public java.util.Set<java.util.Map$Entry<{java.util.Hashtable%0},{java.util.Hashtable%1}>> entrySet()
meth public java.util.Set<{java.util.Hashtable%0}> keySet()
meth public void clear()
meth public void forEach(java.util.function.BiConsumer<? super {java.util.Hashtable%0},? super {java.util.Hashtable%1}>)
meth public void putAll(java.util.Map<? extends {java.util.Hashtable%0},? extends {java.util.Hashtable%1}>)
meth public void replaceAll(java.util.function.BiFunction<? super {java.util.Hashtable%0},? super {java.util.Hashtable%1},? extends {java.util.Hashtable%1}>)
meth public {java.util.Hashtable%1} compute({java.util.Hashtable%0},java.util.function.BiFunction<? super {java.util.Hashtable%0},? super {java.util.Hashtable%1},? extends {java.util.Hashtable%1}>)
meth public {java.util.Hashtable%1} computeIfAbsent({java.util.Hashtable%0},java.util.function.Function<? super {java.util.Hashtable%0},? extends {java.util.Hashtable%1}>)
meth public {java.util.Hashtable%1} computeIfPresent({java.util.Hashtable%0},java.util.function.BiFunction<? super {java.util.Hashtable%0},? super {java.util.Hashtable%1},? extends {java.util.Hashtable%1}>)
meth public {java.util.Hashtable%1} get(java.lang.Object)
meth public {java.util.Hashtable%1} getOrDefault(java.lang.Object,{java.util.Hashtable%1})
meth public {java.util.Hashtable%1} merge({java.util.Hashtable%0},{java.util.Hashtable%1},java.util.function.BiFunction<? super {java.util.Hashtable%1},? super {java.util.Hashtable%1},? extends {java.util.Hashtable%1}>)
meth public {java.util.Hashtable%1} put({java.util.Hashtable%0},{java.util.Hashtable%1})
meth public {java.util.Hashtable%1} putIfAbsent({java.util.Hashtable%0},{java.util.Hashtable%1})
meth public {java.util.Hashtable%1} remove(java.lang.Object)
meth public {java.util.Hashtable%1} replace({java.util.Hashtable%0},{java.util.Hashtable%1})
supr java.util.Dictionary<{java.util.Hashtable%0},{java.util.Hashtable%1}>

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

CLSS public abstract interface java.util.concurrent.Callable<%0 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
meth public abstract {java.util.concurrent.Callable%0} call() throws java.lang.Exception

CLSS public abstract interface javax.accessibility.Accessible
meth public abstract javax.accessibility.AccessibleContext getAccessibleContext()

CLSS public abstract javax.swing.AbstractAction
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,javax.swing.Icon)
fld protected boolean enabled
fld protected javax.swing.event.SwingPropertyChangeSupport changeSupport
intf java.io.Serializable
intf java.lang.Cloneable
intf javax.swing.Action
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public boolean isEnabled()
meth public java.beans.PropertyChangeListener[] getPropertyChangeListeners()
meth public java.lang.Object getValue(java.lang.String)
meth public java.lang.Object[] getKeys()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void putValue(java.lang.String,java.lang.Object)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setEnabled(boolean)
supr java.lang.Object

CLSS public abstract javax.swing.AbstractButton
cons public init()
fld protected java.awt.event.ActionListener actionListener
fld protected java.awt.event.ItemListener itemListener
fld protected javax.swing.ButtonModel model
fld protected javax.swing.event.ChangeEvent changeEvent
fld protected javax.swing.event.ChangeListener changeListener
fld public final static java.lang.String BORDER_PAINTED_CHANGED_PROPERTY = "borderPainted"
fld public final static java.lang.String CONTENT_AREA_FILLED_CHANGED_PROPERTY = "contentAreaFilled"
fld public final static java.lang.String DISABLED_ICON_CHANGED_PROPERTY = "disabledIcon"
fld public final static java.lang.String DISABLED_SELECTED_ICON_CHANGED_PROPERTY = "disabledSelectedIcon"
fld public final static java.lang.String FOCUS_PAINTED_CHANGED_PROPERTY = "focusPainted"
fld public final static java.lang.String HORIZONTAL_ALIGNMENT_CHANGED_PROPERTY = "horizontalAlignment"
fld public final static java.lang.String HORIZONTAL_TEXT_POSITION_CHANGED_PROPERTY = "horizontalTextPosition"
fld public final static java.lang.String ICON_CHANGED_PROPERTY = "icon"
fld public final static java.lang.String MARGIN_CHANGED_PROPERTY = "margin"
fld public final static java.lang.String MNEMONIC_CHANGED_PROPERTY = "mnemonic"
fld public final static java.lang.String MODEL_CHANGED_PROPERTY = "model"
fld public final static java.lang.String PRESSED_ICON_CHANGED_PROPERTY = "pressedIcon"
fld public final static java.lang.String ROLLOVER_ENABLED_CHANGED_PROPERTY = "rolloverEnabled"
fld public final static java.lang.String ROLLOVER_ICON_CHANGED_PROPERTY = "rolloverIcon"
fld public final static java.lang.String ROLLOVER_SELECTED_ICON_CHANGED_PROPERTY = "rolloverSelectedIcon"
fld public final static java.lang.String SELECTED_ICON_CHANGED_PROPERTY = "selectedIcon"
fld public final static java.lang.String TEXT_CHANGED_PROPERTY = "text"
fld public final static java.lang.String VERTICAL_ALIGNMENT_CHANGED_PROPERTY = "verticalAlignment"
fld public final static java.lang.String VERTICAL_TEXT_POSITION_CHANGED_PROPERTY = "verticalTextPosition"
innr protected ButtonChangeListener
innr protected abstract AccessibleAbstractButton
intf java.awt.ItemSelectable
intf javax.swing.SwingConstants
meth protected int checkHorizontalKey(int,java.lang.String)
meth protected int checkVerticalKey(int,java.lang.String)
meth protected java.awt.event.ActionListener createActionListener()
meth protected java.awt.event.ItemListener createItemListener()
meth protected java.beans.PropertyChangeListener createActionPropertyChangeListener(javax.swing.Action)
meth protected java.lang.String paramString()
meth protected javax.swing.event.ChangeListener createChangeListener()
meth protected void actionPropertyChanged(javax.swing.Action,java.lang.String)
meth protected void addImpl(java.awt.Component,java.lang.Object,int)
meth protected void configurePropertiesFromAction(javax.swing.Action)
meth protected void fireActionPerformed(java.awt.event.ActionEvent)
meth protected void fireItemStateChanged(java.awt.event.ItemEvent)
meth protected void fireStateChanged()
meth protected void init(java.lang.String,javax.swing.Icon)
meth protected void paintBorder(java.awt.Graphics)
meth public boolean getHideActionText()
meth public boolean imageUpdate(java.awt.Image,int,int,int,int,int)
meth public boolean isBorderPainted()
meth public boolean isContentAreaFilled()
meth public boolean isFocusPainted()
meth public boolean isRolloverEnabled()
meth public boolean isSelected()
meth public int getDisplayedMnemonicIndex()
meth public int getHorizontalAlignment()
meth public int getHorizontalTextPosition()
meth public int getIconTextGap()
meth public int getMnemonic()
meth public int getVerticalAlignment()
meth public int getVerticalTextPosition()
meth public java.awt.Insets getMargin()
meth public java.awt.event.ActionListener[] getActionListeners()
meth public java.awt.event.ItemListener[] getItemListeners()
meth public java.lang.Object[] getSelectedObjects()
meth public java.lang.String getActionCommand()
meth public java.lang.String getLabel()
 anno 0 java.lang.Deprecated()
meth public java.lang.String getText()
meth public javax.swing.Action getAction()
meth public javax.swing.ButtonModel getModel()
meth public javax.swing.Icon getDisabledIcon()
meth public javax.swing.Icon getDisabledSelectedIcon()
meth public javax.swing.Icon getIcon()
meth public javax.swing.Icon getPressedIcon()
meth public javax.swing.Icon getRolloverIcon()
meth public javax.swing.Icon getRolloverSelectedIcon()
meth public javax.swing.Icon getSelectedIcon()
meth public javax.swing.event.ChangeListener[] getChangeListeners()
meth public javax.swing.plaf.ButtonUI getUI()
meth public long getMultiClickThreshhold()
meth public void addActionListener(java.awt.event.ActionListener)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void addItemListener(java.awt.event.ItemListener)
meth public void doClick()
meth public void doClick(int)
meth public void removeActionListener(java.awt.event.ActionListener)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void removeItemListener(java.awt.event.ItemListener)
meth public void removeNotify()
meth public void setAction(javax.swing.Action)
meth public void setActionCommand(java.lang.String)
meth public void setBorderPainted(boolean)
meth public void setContentAreaFilled(boolean)
meth public void setDisabledIcon(javax.swing.Icon)
meth public void setDisabledSelectedIcon(javax.swing.Icon)
meth public void setDisplayedMnemonicIndex(int)
meth public void setEnabled(boolean)
meth public void setFocusPainted(boolean)
meth public void setHideActionText(boolean)
meth public void setHorizontalAlignment(int)
meth public void setHorizontalTextPosition(int)
meth public void setIcon(javax.swing.Icon)
meth public void setIconTextGap(int)
meth public void setLabel(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void setLayout(java.awt.LayoutManager)
meth public void setMargin(java.awt.Insets)
meth public void setMnemonic(char)
meth public void setMnemonic(int)
meth public void setModel(javax.swing.ButtonModel)
meth public void setMultiClickThreshhold(long)
meth public void setPressedIcon(javax.swing.Icon)
meth public void setRolloverEnabled(boolean)
meth public void setRolloverIcon(javax.swing.Icon)
meth public void setRolloverSelectedIcon(javax.swing.Icon)
meth public void setSelected(boolean)
meth public void setSelectedIcon(javax.swing.Icon)
meth public void setText(java.lang.String)
meth public void setUI(javax.swing.plaf.ButtonUI)
meth public void setVerticalAlignment(int)
meth public void setVerticalTextPosition(int)
meth public void updateUI()
supr javax.swing.JComponent

CLSS public abstract interface javax.swing.Action
fld public final static java.lang.String ACCELERATOR_KEY = "AcceleratorKey"
fld public final static java.lang.String ACTION_COMMAND_KEY = "ActionCommandKey"
fld public final static java.lang.String DEFAULT = "Default"
fld public final static java.lang.String DISPLAYED_MNEMONIC_INDEX_KEY = "SwingDisplayedMnemonicIndexKey"
fld public final static java.lang.String LARGE_ICON_KEY = "SwingLargeIconKey"
fld public final static java.lang.String LONG_DESCRIPTION = "LongDescription"
fld public final static java.lang.String MNEMONIC_KEY = "MnemonicKey"
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String SELECTED_KEY = "SwingSelectedKey"
fld public final static java.lang.String SHORT_DESCRIPTION = "ShortDescription"
fld public final static java.lang.String SMALL_ICON = "SmallIcon"
intf java.awt.event.ActionListener
meth public abstract boolean isEnabled()
meth public abstract java.lang.Object getValue(java.lang.String)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void putValue(java.lang.String,java.lang.Object)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setEnabled(boolean)

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

CLSS public javax.swing.JMenu
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,boolean)
cons public init(javax.swing.Action)
fld protected javax.swing.JMenu$WinListener popupListener
innr protected AccessibleJMenu
innr protected WinListener
intf javax.accessibility.Accessible
intf javax.swing.MenuElement
meth protected java.awt.Point getPopupMenuOrigin()
meth protected java.beans.PropertyChangeListener createActionChangeListener(javax.swing.JMenuItem)
meth protected java.lang.String paramString()
meth protected javax.swing.JMenu$WinListener createWinListener(javax.swing.JPopupMenu)
meth protected javax.swing.JMenuItem createActionComponent(javax.swing.Action)
meth protected void fireMenuCanceled()
meth protected void fireMenuDeselected()
meth protected void fireMenuSelected()
meth protected void processKeyEvent(java.awt.event.KeyEvent)
meth public boolean isMenuComponent(java.awt.Component)
meth public boolean isPopupMenuVisible()
meth public boolean isSelected()
meth public boolean isTearOff()
meth public boolean isTopLevelMenu()
meth public int getDelay()
meth public int getItemCount()
meth public int getMenuComponentCount()
meth public java.awt.Component add(java.awt.Component)
meth public java.awt.Component add(java.awt.Component,int)
meth public java.awt.Component getComponent()
meth public java.awt.Component getMenuComponent(int)
meth public java.awt.Component[] getMenuComponents()
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.JMenuItem add(java.lang.String)
meth public javax.swing.JMenuItem add(javax.swing.Action)
meth public javax.swing.JMenuItem add(javax.swing.JMenuItem)
meth public javax.swing.JMenuItem getItem(int)
meth public javax.swing.JMenuItem insert(javax.swing.Action,int)
meth public javax.swing.JMenuItem insert(javax.swing.JMenuItem,int)
meth public javax.swing.JPopupMenu getPopupMenu()
meth public javax.swing.MenuElement[] getSubElements()
meth public javax.swing.event.MenuListener[] getMenuListeners()
meth public void addMenuListener(javax.swing.event.MenuListener)
meth public void addSeparator()
meth public void applyComponentOrientation(java.awt.ComponentOrientation)
meth public void doClick(int)
meth public void insert(java.lang.String,int)
meth public void insertSeparator(int)
meth public void menuSelectionChanged(boolean)
meth public void remove(int)
meth public void remove(java.awt.Component)
meth public void remove(javax.swing.JMenuItem)
meth public void removeAll()
meth public void removeMenuListener(javax.swing.event.MenuListener)
meth public void setAccelerator(javax.swing.KeyStroke)
meth public void setComponentOrientation(java.awt.ComponentOrientation)
meth public void setDelay(int)
meth public void setMenuLocation(int,int)
meth public void setModel(javax.swing.ButtonModel)
meth public void setPopupMenuVisible(boolean)
meth public void setSelected(boolean)
meth public void updateUI()
supr javax.swing.JMenuItem

CLSS public javax.swing.JMenuItem
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,int)
cons public init(java.lang.String,javax.swing.Icon)
cons public init(javax.swing.Action)
cons public init(javax.swing.Icon)
innr protected AccessibleJMenuItem
intf javax.accessibility.Accessible
intf javax.swing.MenuElement
meth protected java.lang.String paramString()
meth protected void actionPropertyChanged(javax.swing.Action,java.lang.String)
meth protected void configurePropertiesFromAction(javax.swing.Action)
meth protected void fireMenuDragMouseDragged(javax.swing.event.MenuDragMouseEvent)
meth protected void fireMenuDragMouseEntered(javax.swing.event.MenuDragMouseEvent)
meth protected void fireMenuDragMouseExited(javax.swing.event.MenuDragMouseEvent)
meth protected void fireMenuDragMouseReleased(javax.swing.event.MenuDragMouseEvent)
meth protected void fireMenuKeyPressed(javax.swing.event.MenuKeyEvent)
meth protected void fireMenuKeyReleased(javax.swing.event.MenuKeyEvent)
meth protected void fireMenuKeyTyped(javax.swing.event.MenuKeyEvent)
meth protected void init(java.lang.String,javax.swing.Icon)
meth public boolean isArmed()
meth public java.awt.Component getComponent()
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.KeyStroke getAccelerator()
meth public javax.swing.MenuElement[] getSubElements()
meth public javax.swing.event.MenuDragMouseListener[] getMenuDragMouseListeners()
meth public javax.swing.event.MenuKeyListener[] getMenuKeyListeners()
meth public void addMenuDragMouseListener(javax.swing.event.MenuDragMouseListener)
meth public void addMenuKeyListener(javax.swing.event.MenuKeyListener)
meth public void menuSelectionChanged(boolean)
meth public void processKeyEvent(java.awt.event.KeyEvent,javax.swing.MenuElement[],javax.swing.MenuSelectionManager)
meth public void processMenuDragMouseEvent(javax.swing.event.MenuDragMouseEvent)
meth public void processMenuKeyEvent(javax.swing.event.MenuKeyEvent)
meth public void processMouseEvent(java.awt.event.MouseEvent,javax.swing.MenuElement[],javax.swing.MenuSelectionManager)
meth public void removeMenuDragMouseListener(javax.swing.event.MenuDragMouseListener)
meth public void removeMenuKeyListener(javax.swing.event.MenuKeyListener)
meth public void setAccelerator(javax.swing.KeyStroke)
meth public void setArmed(boolean)
meth public void setEnabled(boolean)
meth public void setModel(javax.swing.ButtonModel)
meth public void setUI(javax.swing.plaf.MenuItemUI)
meth public void updateUI()
supr javax.swing.AbstractButton

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

CLSS public abstract interface javax.swing.MenuElement
meth public abstract java.awt.Component getComponent()
meth public abstract javax.swing.MenuElement[] getSubElements()
meth public abstract void menuSelectionChanged(boolean)
meth public abstract void processKeyEvent(java.awt.event.KeyEvent,javax.swing.MenuElement[],javax.swing.MenuSelectionManager)
meth public abstract void processMouseEvent(java.awt.event.MouseEvent,javax.swing.MenuElement[],javax.swing.MenuSelectionManager)

CLSS public abstract interface javax.swing.Scrollable
meth public abstract boolean getScrollableTracksViewportHeight()
meth public abstract boolean getScrollableTracksViewportWidth()
meth public abstract int getScrollableBlockIncrement(java.awt.Rectangle,int,int)
meth public abstract int getScrollableUnitIncrement(java.awt.Rectangle,int,int)
meth public abstract java.awt.Dimension getPreferredScrollableViewportSize()

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

CLSS public abstract interface javax.swing.event.ChangeListener
intf java.util.EventListener
meth public abstract void stateChanged(javax.swing.event.ChangeEvent)

CLSS public abstract interface javax.swing.event.DocumentEvent
innr public abstract interface static ElementChange
innr public final static EventType
meth public abstract int getLength()
meth public abstract int getOffset()
meth public abstract javax.swing.event.DocumentEvent$ElementChange getChange(javax.swing.text.Element)
meth public abstract javax.swing.event.DocumentEvent$EventType getType()
meth public abstract javax.swing.text.Document getDocument()

CLSS public abstract interface javax.swing.event.DocumentListener
intf java.util.EventListener
meth public abstract void changedUpdate(javax.swing.event.DocumentEvent)
meth public abstract void insertUpdate(javax.swing.event.DocumentEvent)
meth public abstract void removeUpdate(javax.swing.event.DocumentEvent)

CLSS public javax.swing.event.EventListenerList
cons public init()
fld protected java.lang.Object[] listenerList
intf java.io.Serializable
meth public <%0 extends java.util.EventListener> void add(java.lang.Class<{%%0}>,{%%0})
meth public <%0 extends java.util.EventListener> void remove(java.lang.Class<{%%0}>,{%%0})
meth public <%0 extends java.util.EventListener> {%%0}[] getListeners(java.lang.Class<{%%0}>)
meth public int getListenerCount()
meth public int getListenerCount(java.lang.Class<?>)
meth public java.lang.Object[] getListenerList()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public abstract javax.swing.plaf.ComponentUI
cons public init()
meth public boolean contains(javax.swing.JComponent,int,int)
meth public int getAccessibleChildrenCount(javax.swing.JComponent)
meth public int getBaseline(javax.swing.JComponent,int,int)
meth public java.awt.Component$BaselineResizeBehavior getBaselineResizeBehavior(javax.swing.JComponent)
meth public java.awt.Dimension getMaximumSize(javax.swing.JComponent)
meth public java.awt.Dimension getMinimumSize(javax.swing.JComponent)
meth public java.awt.Dimension getPreferredSize(javax.swing.JComponent)
meth public javax.accessibility.Accessible getAccessibleChild(javax.swing.JComponent,int)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
meth public void installUI(javax.swing.JComponent)
meth public void paint(java.awt.Graphics,javax.swing.JComponent)
meth public void uninstallUI(javax.swing.JComponent)
meth public void update(java.awt.Graphics,javax.swing.JComponent)
supr java.lang.Object

CLSS public abstract javax.swing.plaf.TextUI
cons public init()
meth public abstract int getNextVisualPositionFrom(javax.swing.text.JTextComponent,int,javax.swing.text.Position$Bias,int,javax.swing.text.Position$Bias[]) throws javax.swing.text.BadLocationException
meth public abstract int viewToModel(javax.swing.text.JTextComponent,java.awt.Point)
meth public abstract int viewToModel(javax.swing.text.JTextComponent,java.awt.Point,javax.swing.text.Position$Bias[])
meth public abstract java.awt.Rectangle modelToView(javax.swing.text.JTextComponent,int) throws javax.swing.text.BadLocationException
meth public abstract java.awt.Rectangle modelToView(javax.swing.text.JTextComponent,int,javax.swing.text.Position$Bias) throws javax.swing.text.BadLocationException
meth public abstract javax.swing.text.EditorKit getEditorKit(javax.swing.text.JTextComponent)
meth public abstract javax.swing.text.View getRootView(javax.swing.text.JTextComponent)
meth public abstract void damageRange(javax.swing.text.JTextComponent,int,int)
meth public abstract void damageRange(javax.swing.text.JTextComponent,int,int,javax.swing.text.Position$Bias,javax.swing.text.Position$Bias)
meth public java.lang.String getToolTipText(javax.swing.text.JTextComponent,java.awt.Point)
supr javax.swing.plaf.ComponentUI

CLSS public abstract javax.swing.plaf.basic.BasicTextUI
cons public init()
innr public static BasicCaret
innr public static BasicHighlighter
intf javax.swing.text.ViewFactory
meth protected abstract java.lang.String getPropertyPrefix()
meth protected final javax.swing.text.JTextComponent getComponent()
meth protected final void setView(javax.swing.text.View)
meth protected java.awt.Rectangle getVisibleEditorRect()
meth protected java.lang.String getKeymapName()
meth protected javax.swing.text.Caret createCaret()
meth protected javax.swing.text.Highlighter createHighlighter()
meth protected javax.swing.text.Keymap createKeymap()
meth protected void installDefaults()
meth protected void installKeyboardActions()
meth protected void installListeners()
meth protected void modelChanged()
meth protected void paintBackground(java.awt.Graphics)
meth protected void paintSafely(java.awt.Graphics)
meth protected void propertyChange(java.beans.PropertyChangeEvent)
meth protected void uninstallDefaults()
meth protected void uninstallKeyboardActions()
meth protected void uninstallListeners()
meth public final void paint(java.awt.Graphics,javax.swing.JComponent)
meth public int getNextVisualPositionFrom(javax.swing.text.JTextComponent,int,javax.swing.text.Position$Bias,int,javax.swing.text.Position$Bias[]) throws javax.swing.text.BadLocationException
meth public int viewToModel(javax.swing.text.JTextComponent,java.awt.Point)
meth public int viewToModel(javax.swing.text.JTextComponent,java.awt.Point,javax.swing.text.Position$Bias[])
meth public java.awt.Dimension getMaximumSize(javax.swing.JComponent)
meth public java.awt.Dimension getMinimumSize(javax.swing.JComponent)
meth public java.awt.Dimension getPreferredSize(javax.swing.JComponent)
meth public java.awt.Rectangle modelToView(javax.swing.text.JTextComponent,int) throws javax.swing.text.BadLocationException
meth public java.awt.Rectangle modelToView(javax.swing.text.JTextComponent,int,javax.swing.text.Position$Bias) throws javax.swing.text.BadLocationException
meth public java.lang.String getToolTipText(javax.swing.text.JTextComponent,java.awt.Point)
meth public javax.swing.text.EditorKit getEditorKit(javax.swing.text.JTextComponent)
meth public javax.swing.text.View create(javax.swing.text.Element)
meth public javax.swing.text.View create(javax.swing.text.Element,int,int)
meth public javax.swing.text.View getRootView(javax.swing.text.JTextComponent)
meth public void damageRange(javax.swing.text.JTextComponent,int,int)
meth public void damageRange(javax.swing.text.JTextComponent,int,int,javax.swing.text.Position$Bias,javax.swing.text.Position$Bias)
meth public void installUI(javax.swing.JComponent)
meth public void uninstallUI(javax.swing.JComponent)
meth public void update(java.awt.Graphics,javax.swing.JComponent)
supr javax.swing.plaf.TextUI

CLSS public abstract javax.swing.text.AbstractDocument
cons protected init(javax.swing.text.AbstractDocument$Content)
cons protected init(javax.swing.text.AbstractDocument$Content,javax.swing.text.AbstractDocument$AttributeContext)
fld protected final static java.lang.String BAD_LOCATION = "document location failure"
fld protected javax.swing.event.EventListenerList listenerList
fld public final static java.lang.String BidiElementName = "bidi level"
fld public final static java.lang.String ContentElementName = "content"
fld public final static java.lang.String ElementNameAttribute = "$ename"
fld public final static java.lang.String ParagraphElementName = "paragraph"
fld public final static java.lang.String SectionElementName = "section"
innr public BranchElement
innr public DefaultDocumentEvent
innr public LeafElement
innr public abstract AbstractElement
innr public abstract interface static AttributeContext
innr public abstract interface static Content
innr public static ElementEdit
intf java.io.Serializable
intf javax.swing.text.Document
meth protected final java.lang.Thread getCurrentWriter()
meth protected final javax.swing.text.AbstractDocument$AttributeContext getAttributeContext()
meth protected final javax.swing.text.AbstractDocument$Content getContent()
meth protected final void writeLock()
meth protected final void writeUnlock()
meth protected javax.swing.text.Element createBranchElement(javax.swing.text.Element,javax.swing.text.AttributeSet)
meth protected javax.swing.text.Element createLeafElement(javax.swing.text.Element,javax.swing.text.AttributeSet,int,int)
meth protected void fireChangedUpdate(javax.swing.event.DocumentEvent)
meth protected void fireInsertUpdate(javax.swing.event.DocumentEvent)
meth protected void fireRemoveUpdate(javax.swing.event.DocumentEvent)
meth protected void fireUndoableEditUpdate(javax.swing.event.UndoableEditEvent)
meth protected void insertUpdate(javax.swing.text.AbstractDocument$DefaultDocumentEvent,javax.swing.text.AttributeSet)
meth protected void postRemoveUpdate(javax.swing.text.AbstractDocument$DefaultDocumentEvent)
meth protected void removeUpdate(javax.swing.text.AbstractDocument$DefaultDocumentEvent)
meth public <%0 extends java.util.EventListener> {%%0}[] getListeners(java.lang.Class<{%%0}>)
meth public abstract javax.swing.text.Element getDefaultRootElement()
meth public abstract javax.swing.text.Element getParagraphElement(int)
meth public final java.lang.Object getProperty(java.lang.Object)
meth public final javax.swing.text.Position getEndPosition()
meth public final javax.swing.text.Position getStartPosition()
meth public final void putProperty(java.lang.Object,java.lang.Object)
meth public final void readLock()
meth public final void readUnlock()
meth public int getAsynchronousLoadPriority()
meth public int getLength()
meth public java.lang.String getText(int,int) throws javax.swing.text.BadLocationException
meth public java.util.Dictionary<java.lang.Object,java.lang.Object> getDocumentProperties()
meth public javax.swing.event.DocumentListener[] getDocumentListeners()
meth public javax.swing.event.UndoableEditListener[] getUndoableEditListeners()
meth public javax.swing.text.DocumentFilter getDocumentFilter()
meth public javax.swing.text.Element getBidiRootElement()
meth public javax.swing.text.Element[] getRootElements()
meth public javax.swing.text.Position createPosition(int) throws javax.swing.text.BadLocationException
meth public void addDocumentListener(javax.swing.event.DocumentListener)
meth public void addUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public void dump(java.io.PrintStream)
meth public void getText(int,int,javax.swing.text.Segment) throws javax.swing.text.BadLocationException
meth public void insertString(int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth public void remove(int,int) throws javax.swing.text.BadLocationException
meth public void removeDocumentListener(javax.swing.event.DocumentListener)
meth public void removeUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public void render(java.lang.Runnable)
meth public void replace(int,int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth public void setAsynchronousLoadPriority(int)
meth public void setDocumentFilter(javax.swing.text.DocumentFilter)
meth public void setDocumentProperties(java.util.Dictionary<java.lang.Object,java.lang.Object>)
supr java.lang.Object

CLSS public javax.swing.text.AbstractDocument$DefaultDocumentEvent
 outer javax.swing.text.AbstractDocument
cons public init(javax.swing.text.AbstractDocument,int,int,javax.swing.event.DocumentEvent$EventType)
intf javax.swing.event.DocumentEvent
meth public boolean addEdit(javax.swing.undo.UndoableEdit)
meth public boolean isSignificant()
meth public int getLength()
meth public int getOffset()
meth public java.lang.String getPresentationName()
meth public java.lang.String getRedoPresentationName()
meth public java.lang.String getUndoPresentationName()
meth public java.lang.String toString()
meth public javax.swing.event.DocumentEvent$ElementChange getChange(javax.swing.text.Element)
meth public javax.swing.event.DocumentEvent$EventType getType()
meth public javax.swing.text.Document getDocument()
meth public void redo()
meth public void undo()
supr javax.swing.undo.CompoundEdit

CLSS public javax.swing.text.BadLocationException
cons public init(java.lang.String,int)
meth public int offsetRequested()
supr java.lang.Exception

CLSS public abstract interface javax.swing.text.Caret
meth public abstract boolean isSelectionVisible()
meth public abstract boolean isVisible()
meth public abstract int getBlinkRate()
meth public abstract int getDot()
meth public abstract int getMark()
meth public abstract java.awt.Point getMagicCaretPosition()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void deinstall(javax.swing.text.JTextComponent)
meth public abstract void install(javax.swing.text.JTextComponent)
meth public abstract void moveDot(int)
meth public abstract void paint(java.awt.Graphics)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public abstract void setBlinkRate(int)
meth public abstract void setDot(int)
meth public abstract void setMagicCaretPosition(java.awt.Point)
meth public abstract void setSelectionVisible(boolean)
meth public abstract void setVisible(boolean)

CLSS public javax.swing.text.DefaultEditorKit
cons public init()
fld public final static java.lang.String EndOfLineStringProperty = "__EndOfLine__"
fld public final static java.lang.String backwardAction = "caret-backward"
fld public final static java.lang.String beepAction = "beep"
fld public final static java.lang.String beginAction = "caret-begin"
fld public final static java.lang.String beginLineAction = "caret-begin-line"
fld public final static java.lang.String beginParagraphAction = "caret-begin-paragraph"
fld public final static java.lang.String beginWordAction = "caret-begin-word"
fld public final static java.lang.String copyAction = "copy-to-clipboard"
fld public final static java.lang.String cutAction = "cut-to-clipboard"
fld public final static java.lang.String defaultKeyTypedAction = "default-typed"
fld public final static java.lang.String deleteNextCharAction = "delete-next"
fld public final static java.lang.String deleteNextWordAction = "delete-next-word"
fld public final static java.lang.String deletePrevCharAction = "delete-previous"
fld public final static java.lang.String deletePrevWordAction = "delete-previous-word"
fld public final static java.lang.String downAction = "caret-down"
fld public final static java.lang.String endAction = "caret-end"
fld public final static java.lang.String endLineAction = "caret-end-line"
fld public final static java.lang.String endParagraphAction = "caret-end-paragraph"
fld public final static java.lang.String endWordAction = "caret-end-word"
fld public final static java.lang.String forwardAction = "caret-forward"
fld public final static java.lang.String insertBreakAction = "insert-break"
fld public final static java.lang.String insertContentAction = "insert-content"
fld public final static java.lang.String insertTabAction = "insert-tab"
fld public final static java.lang.String nextWordAction = "caret-next-word"
fld public final static java.lang.String pageDownAction = "page-down"
fld public final static java.lang.String pageUpAction = "page-up"
fld public final static java.lang.String pasteAction = "paste-from-clipboard"
fld public final static java.lang.String previousWordAction = "caret-previous-word"
fld public final static java.lang.String readOnlyAction = "set-read-only"
fld public final static java.lang.String selectAllAction = "select-all"
fld public final static java.lang.String selectLineAction = "select-line"
fld public final static java.lang.String selectParagraphAction = "select-paragraph"
fld public final static java.lang.String selectWordAction = "select-word"
fld public final static java.lang.String selectionBackwardAction = "selection-backward"
fld public final static java.lang.String selectionBeginAction = "selection-begin"
fld public final static java.lang.String selectionBeginLineAction = "selection-begin-line"
fld public final static java.lang.String selectionBeginParagraphAction = "selection-begin-paragraph"
fld public final static java.lang.String selectionBeginWordAction = "selection-begin-word"
fld public final static java.lang.String selectionDownAction = "selection-down"
fld public final static java.lang.String selectionEndAction = "selection-end"
fld public final static java.lang.String selectionEndLineAction = "selection-end-line"
fld public final static java.lang.String selectionEndParagraphAction = "selection-end-paragraph"
fld public final static java.lang.String selectionEndWordAction = "selection-end-word"
fld public final static java.lang.String selectionForwardAction = "selection-forward"
fld public final static java.lang.String selectionNextWordAction = "selection-next-word"
fld public final static java.lang.String selectionPreviousWordAction = "selection-previous-word"
fld public final static java.lang.String selectionUpAction = "selection-up"
fld public final static java.lang.String upAction = "caret-up"
fld public final static java.lang.String writableAction = "set-writable"
innr public static BeepAction
innr public static CopyAction
innr public static CutAction
innr public static DefaultKeyTypedAction
innr public static InsertBreakAction
innr public static InsertContentAction
innr public static InsertTabAction
innr public static PasteAction
meth public java.lang.String getContentType()
meth public javax.swing.Action[] getActions()
meth public javax.swing.text.Caret createCaret()
meth public javax.swing.text.Document createDefaultDocument()
meth public javax.swing.text.ViewFactory getViewFactory()
meth public void read(java.io.InputStream,javax.swing.text.Document,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public void read(java.io.Reader,javax.swing.text.Document,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public void write(java.io.OutputStream,javax.swing.text.Document,int,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public void write(java.io.Writer,javax.swing.text.Document,int,int) throws java.io.IOException,javax.swing.text.BadLocationException
supr javax.swing.text.EditorKit

CLSS public abstract interface javax.swing.text.Document
fld public final static java.lang.String StreamDescriptionProperty = "stream"
fld public final static java.lang.String TitleProperty = "title"
meth public abstract int getLength()
meth public abstract java.lang.Object getProperty(java.lang.Object)
meth public abstract java.lang.String getText(int,int) throws javax.swing.text.BadLocationException
meth public abstract javax.swing.text.Element getDefaultRootElement()
meth public abstract javax.swing.text.Element[] getRootElements()
meth public abstract javax.swing.text.Position createPosition(int) throws javax.swing.text.BadLocationException
meth public abstract javax.swing.text.Position getEndPosition()
meth public abstract javax.swing.text.Position getStartPosition()
meth public abstract void addDocumentListener(javax.swing.event.DocumentListener)
meth public abstract void addUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public abstract void getText(int,int,javax.swing.text.Segment) throws javax.swing.text.BadLocationException
meth public abstract void insertString(int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth public abstract void putProperty(java.lang.Object,java.lang.Object)
meth public abstract void remove(int,int) throws javax.swing.text.BadLocationException
meth public abstract void removeDocumentListener(javax.swing.event.DocumentListener)
meth public abstract void removeUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public abstract void render(java.lang.Runnable)

CLSS public abstract javax.swing.text.EditorKit
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public abstract java.lang.String getContentType()
meth public abstract javax.swing.Action[] getActions()
meth public abstract javax.swing.text.Caret createCaret()
meth public abstract javax.swing.text.Document createDefaultDocument()
meth public abstract javax.swing.text.ViewFactory getViewFactory()
meth public abstract void read(java.io.InputStream,javax.swing.text.Document,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public abstract void read(java.io.Reader,javax.swing.text.Document,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public abstract void write(java.io.OutputStream,javax.swing.text.Document,int,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public abstract void write(java.io.Writer,javax.swing.text.Document,int,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public java.lang.Object clone()
meth public void deinstall(javax.swing.JEditorPane)
meth public void install(javax.swing.JEditorPane)
supr java.lang.Object

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

CLSS public abstract javax.swing.text.JTextComponent
cons public init()
fld public final static java.lang.String DEFAULT_KEYMAP = "default"
fld public final static java.lang.String FOCUS_ACCELERATOR_KEY = "focusAcceleratorKey"
innr public AccessibleJTextComponent
innr public final static DropLocation
innr public static KeyBinding
intf javax.accessibility.Accessible
intf javax.swing.Scrollable
meth protected boolean saveComposedText(int)
meth protected java.lang.String paramString()
meth protected void fireCaretUpdate(javax.swing.event.CaretEvent)
meth protected void processInputMethodEvent(java.awt.event.InputMethodEvent)
meth protected void restoreComposedText()
meth public boolean getDragEnabled()
meth public boolean getScrollableTracksViewportHeight()
meth public boolean getScrollableTracksViewportWidth()
meth public boolean isEditable()
meth public boolean print() throws java.awt.print.PrinterException
meth public boolean print(java.text.MessageFormat,java.text.MessageFormat) throws java.awt.print.PrinterException
meth public boolean print(java.text.MessageFormat,java.text.MessageFormat,boolean,javax.print.PrintService,javax.print.attribute.PrintRequestAttributeSet,boolean) throws java.awt.print.PrinterException
meth public char getFocusAccelerator()
meth public final javax.swing.DropMode getDropMode()
meth public final javax.swing.text.JTextComponent$DropLocation getDropLocation()
meth public final void setDropMode(javax.swing.DropMode)
meth public int getCaretPosition()
meth public int getScrollableBlockIncrement(java.awt.Rectangle,int,int)
meth public int getScrollableUnitIncrement(java.awt.Rectangle,int,int)
meth public int getSelectionEnd()
meth public int getSelectionStart()
meth public int viewToModel(java.awt.Point)
meth public java.awt.Color getCaretColor()
meth public java.awt.Color getDisabledTextColor()
meth public java.awt.Color getSelectedTextColor()
meth public java.awt.Color getSelectionColor()
meth public java.awt.Dimension getPreferredScrollableViewportSize()
meth public java.awt.Insets getMargin()
meth public java.awt.Rectangle modelToView(int) throws javax.swing.text.BadLocationException
meth public java.awt.im.InputMethodRequests getInputMethodRequests()
meth public java.awt.print.Printable getPrintable(java.text.MessageFormat,java.text.MessageFormat)
meth public java.lang.String getSelectedText()
meth public java.lang.String getText()
meth public java.lang.String getText(int,int) throws javax.swing.text.BadLocationException
meth public java.lang.String getToolTipText(java.awt.event.MouseEvent)
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.Action[] getActions()
meth public javax.swing.event.CaretListener[] getCaretListeners()
meth public javax.swing.plaf.TextUI getUI()
meth public javax.swing.text.Caret getCaret()
meth public javax.swing.text.Document getDocument()
meth public javax.swing.text.Highlighter getHighlighter()
meth public javax.swing.text.Keymap getKeymap()
meth public javax.swing.text.NavigationFilter getNavigationFilter()
meth public static javax.swing.text.Keymap addKeymap(java.lang.String,javax.swing.text.Keymap)
meth public static javax.swing.text.Keymap getKeymap(java.lang.String)
meth public static javax.swing.text.Keymap removeKeymap(java.lang.String)
meth public static void loadKeymap(javax.swing.text.Keymap,javax.swing.text.JTextComponent$KeyBinding[],javax.swing.Action[])
meth public void addCaretListener(javax.swing.event.CaretListener)
meth public void addInputMethodListener(java.awt.event.InputMethodListener)
meth public void copy()
meth public void cut()
meth public void moveCaretPosition(int)
meth public void paste()
meth public void read(java.io.Reader,java.lang.Object) throws java.io.IOException
meth public void removeCaretListener(javax.swing.event.CaretListener)
meth public void removeNotify()
meth public void replaceSelection(java.lang.String)
meth public void select(int,int)
meth public void selectAll()
meth public void setCaret(javax.swing.text.Caret)
meth public void setCaretColor(java.awt.Color)
meth public void setCaretPosition(int)
meth public void setComponentOrientation(java.awt.ComponentOrientation)
meth public void setDisabledTextColor(java.awt.Color)
meth public void setDocument(javax.swing.text.Document)
meth public void setDragEnabled(boolean)
meth public void setEditable(boolean)
meth public void setFocusAccelerator(char)
meth public void setHighlighter(javax.swing.text.Highlighter)
meth public void setKeymap(javax.swing.text.Keymap)
meth public void setMargin(java.awt.Insets)
meth public void setNavigationFilter(javax.swing.text.NavigationFilter)
meth public void setSelectedTextColor(java.awt.Color)
meth public void setSelectionColor(java.awt.Color)
meth public void setSelectionEnd(int)
meth public void setSelectionStart(int)
meth public void setText(java.lang.String)
meth public void setUI(javax.swing.plaf.TextUI)
meth public void updateUI()
meth public void write(java.io.Writer) throws java.io.IOException
supr javax.swing.JComponent

CLSS public static javax.swing.text.JTextComponent$KeyBinding
 outer javax.swing.text.JTextComponent
cons public init(javax.swing.KeyStroke,java.lang.String)
fld public java.lang.String actionName
fld public javax.swing.KeyStroke key
supr java.lang.Object

CLSS public abstract interface javax.swing.text.Keymap
meth public abstract boolean isLocallyDefined(javax.swing.KeyStroke)
meth public abstract java.lang.String getName()
meth public abstract javax.swing.Action getAction(javax.swing.KeyStroke)
meth public abstract javax.swing.Action getDefaultAction()
meth public abstract javax.swing.Action[] getBoundActions()
meth public abstract javax.swing.KeyStroke[] getBoundKeyStrokes()
meth public abstract javax.swing.KeyStroke[] getKeyStrokesForAction(javax.swing.Action)
meth public abstract javax.swing.text.Keymap getResolveParent()
meth public abstract void addActionForKeyStroke(javax.swing.KeyStroke,javax.swing.Action)
meth public abstract void removeBindings()
meth public abstract void removeKeyStrokeBinding(javax.swing.KeyStroke)
meth public abstract void setDefaultAction(javax.swing.Action)
meth public abstract void setResolveParent(javax.swing.text.Keymap)

CLSS public abstract interface javax.swing.text.StyledDocument
intf javax.swing.text.Document
meth public abstract java.awt.Color getBackground(javax.swing.text.AttributeSet)
meth public abstract java.awt.Color getForeground(javax.swing.text.AttributeSet)
meth public abstract java.awt.Font getFont(javax.swing.text.AttributeSet)
meth public abstract javax.swing.text.Element getCharacterElement(int)
meth public abstract javax.swing.text.Element getParagraphElement(int)
meth public abstract javax.swing.text.Style addStyle(java.lang.String,javax.swing.text.Style)
meth public abstract javax.swing.text.Style getLogicalStyle(int)
meth public abstract javax.swing.text.Style getStyle(java.lang.String)
meth public abstract void removeStyle(java.lang.String)
meth public abstract void setCharacterAttributes(int,int,javax.swing.text.AttributeSet,boolean)
meth public abstract void setLogicalStyle(int,javax.swing.text.Style)
meth public abstract void setParagraphAttributes(int,int,javax.swing.text.AttributeSet,boolean)

CLSS public abstract javax.swing.text.TextAction
cons public init(java.lang.String)
meth protected final javax.swing.text.JTextComponent getFocusedComponent()
meth protected final javax.swing.text.JTextComponent getTextComponent(java.awt.event.ActionEvent)
meth public final static javax.swing.Action[] augmentList(javax.swing.Action[],javax.swing.Action[])
supr javax.swing.AbstractAction

CLSS public abstract javax.swing.text.View
cons public init(javax.swing.text.Element)
fld public final static int BadBreakWeight = 0
fld public final static int ExcellentBreakWeight = 2000
fld public final static int ForcedBreakWeight = 3000
fld public final static int GoodBreakWeight = 1000
fld public final static int X_AXIS = 0
fld public final static int Y_AXIS = 1
intf javax.swing.SwingConstants
meth protected boolean updateChildren(javax.swing.event.DocumentEvent$ElementChange,javax.swing.event.DocumentEvent,javax.swing.text.ViewFactory)
meth protected void forwardUpdate(javax.swing.event.DocumentEvent$ElementChange,javax.swing.event.DocumentEvent,java.awt.Shape,javax.swing.text.ViewFactory)
meth protected void forwardUpdateToView(javax.swing.text.View,javax.swing.event.DocumentEvent,java.awt.Shape,javax.swing.text.ViewFactory)
meth protected void updateLayout(javax.swing.event.DocumentEvent$ElementChange,javax.swing.event.DocumentEvent,java.awt.Shape)
meth public abstract float getPreferredSpan(int)
meth public abstract int viewToModel(float,float,java.awt.Shape,javax.swing.text.Position$Bias[])
meth public abstract java.awt.Shape modelToView(int,java.awt.Shape,javax.swing.text.Position$Bias) throws javax.swing.text.BadLocationException
meth public abstract void paint(java.awt.Graphics,java.awt.Shape)
meth public boolean isVisible()
meth public float getAlignment(int)
meth public float getMaximumSpan(int)
meth public float getMinimumSpan(int)
meth public int getBreakWeight(int,float,float)
meth public int getEndOffset()
meth public int getNextVisualPositionFrom(int,javax.swing.text.Position$Bias,java.awt.Shape,int,javax.swing.text.Position$Bias[]) throws javax.swing.text.BadLocationException
meth public int getResizeWeight(int)
meth public int getStartOffset()
meth public int getViewCount()
meth public int getViewIndex(float,float,java.awt.Shape)
meth public int getViewIndex(int,javax.swing.text.Position$Bias)
meth public int viewToModel(float,float,java.awt.Shape)
 anno 0 java.lang.Deprecated()
meth public java.awt.Container getContainer()
meth public java.awt.Graphics getGraphics()
meth public java.awt.Shape getChildAllocation(int,java.awt.Shape)
meth public java.awt.Shape modelToView(int,java.awt.Shape) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
meth public java.awt.Shape modelToView(int,javax.swing.text.Position$Bias,int,javax.swing.text.Position$Bias,java.awt.Shape) throws javax.swing.text.BadLocationException
meth public java.lang.String getToolTipText(float,float,java.awt.Shape)
meth public javax.swing.text.AttributeSet getAttributes()
meth public javax.swing.text.Document getDocument()
meth public javax.swing.text.Element getElement()
meth public javax.swing.text.View breakView(int,int,float,float)
meth public javax.swing.text.View createFragment(int,int)
meth public javax.swing.text.View getParent()
meth public javax.swing.text.View getView(int)
meth public javax.swing.text.ViewFactory getViewFactory()
meth public void append(javax.swing.text.View)
meth public void changedUpdate(javax.swing.event.DocumentEvent,java.awt.Shape,javax.swing.text.ViewFactory)
meth public void insert(int,javax.swing.text.View)
meth public void insertUpdate(javax.swing.event.DocumentEvent,java.awt.Shape,javax.swing.text.ViewFactory)
meth public void preferenceChanged(javax.swing.text.View,boolean,boolean)
meth public void remove(int)
meth public void removeAll()
meth public void removeUpdate(javax.swing.event.DocumentEvent,java.awt.Shape,javax.swing.text.ViewFactory)
meth public void replace(int,int,javax.swing.text.View[])
meth public void setParent(javax.swing.text.View)
meth public void setSize(float,float)
supr java.lang.Object

CLSS public abstract interface javax.swing.text.ViewFactory
meth public abstract javax.swing.text.View create(javax.swing.text.Element)

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

CLSS public javax.swing.undo.CompoundEdit
cons public init()
fld protected java.util.Vector<javax.swing.undo.UndoableEdit> edits
meth protected javax.swing.undo.UndoableEdit lastEdit()
meth public boolean addEdit(javax.swing.undo.UndoableEdit)
meth public boolean canRedo()
meth public boolean canUndo()
meth public boolean isInProgress()
meth public boolean isSignificant()
meth public java.lang.String getPresentationName()
meth public java.lang.String getRedoPresentationName()
meth public java.lang.String getUndoPresentationName()
meth public java.lang.String toString()
meth public void die()
meth public void end()
meth public void redo()
meth public void undo()
supr javax.swing.undo.AbstractUndoableEdit

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

CLSS public org.netbeans.api.editor.document.AtomicLockEvent
cons public init(javax.swing.text.Document)
meth public javax.swing.text.Document getDocument()
meth public org.netbeans.api.editor.document.AtomicLockDocument getAtomicLock()
supr java.util.EventObject

CLSS public abstract interface org.netbeans.api.editor.document.CustomUndoDocument
meth public abstract void addUndoableEdit(javax.swing.undo.UndoableEdit)

CLSS public abstract interface org.netbeans.api.editor.document.LineDocument
intf javax.swing.text.Document
meth public abstract javax.swing.text.Element getParagraphElement(int)
meth public abstract javax.swing.text.Position createPosition(int,javax.swing.text.Position$Bias) throws javax.swing.text.BadLocationException

CLSS public abstract interface org.netbeans.api.editor.fold.FoldHierarchyListener
intf java.util.EventListener
meth public abstract void foldHierarchyChanged(org.netbeans.api.editor.fold.FoldHierarchyEvent)

CLSS public org.netbeans.editor.Abbrev
 anno 0 java.lang.Deprecated()
cons public init(org.netbeans.editor.EditorUI,boolean,boolean)
fld protected org.netbeans.editor.EditorUI editorUI
intf java.beans.PropertyChangeListener
meth protected boolean doExpansion(int,java.lang.String,java.awt.event.ActionEvent) throws javax.swing.text.BadLocationException
meth public boolean checkAndExpand(char,java.awt.event.ActionEvent) throws javax.swing.text.BadLocationException
meth public boolean checkReset(char)
meth public boolean expandString(char,java.lang.String,java.awt.event.ActionEvent) throws javax.swing.text.BadLocationException
meth public boolean expandString(java.lang.String,java.awt.event.ActionEvent) throws javax.swing.text.BadLocationException
meth public java.lang.Object translateAbbrev(java.lang.String)
meth public java.lang.String getAbbrevString()
meth public java.lang.String getExpandString()
meth public java.lang.String getExpandString(char)
meth public java.util.Map getAbbrevMap()
meth public static boolean isAbbrevDisabled(javax.swing.text.JTextComponent)
meth public void addChar(char)
meth public void checkAndExpand(java.awt.event.ActionEvent) throws javax.swing.text.BadLocationException
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void reset()
supr java.lang.Object
hfds abbrevMap,abbrevSB,addTypedAcceptor,checkDocText,checkTextDelimiter,doExpandAcceptor,resetAcceptor

CLSS public abstract interface org.netbeans.editor.Acceptor
meth public abstract boolean accept(char)

CLSS public org.netbeans.editor.AcceptorFactory
cons public init()
fld public final static org.netbeans.editor.Acceptor FALSE
fld public final static org.netbeans.editor.Acceptor JAVA_IDENTIFIER
fld public final static org.netbeans.editor.Acceptor LETTER_DIGIT
fld public final static org.netbeans.editor.Acceptor NL
fld public final static org.netbeans.editor.Acceptor NON_JAVA_IDENTIFIER
fld public final static org.netbeans.editor.Acceptor SPACE_NL
fld public final static org.netbeans.editor.Acceptor TRUE
fld public final static org.netbeans.editor.Acceptor WHITESPACE
supr java.lang.Object
hcls Char,Fixed,TwoChar

CLSS public org.netbeans.editor.ActionFactory
innr public static AbbrevExpandAction
innr public static AbbrevResetAction
innr public static AdjustCaretAction
innr public static AdjustWindowAction
innr public static AnnotationsCyclingAction
innr public static ChangeCaseAction
innr public static CollapseAllFolds
innr public static CollapseFold
innr public static CopySelectionElseLineDownAction
innr public static CopySelectionElseLineUpAction
innr public static CutToLineBeginOrEndAction
innr public static DumpViewHierarchyAction
innr public static ExpandAllFolds
innr public static ExpandFold
innr public static FirstNonWhiteAction
innr public static FormatAction
innr public static GenerateGutterPopupAction
innr public static InsertDateTimeAction
innr public static JumpListNextAction
innr public static JumpListNextComponentAction
innr public static JumpListPrevAction
innr public static JumpListPrevComponentAction
innr public static LastNonWhiteAction
innr public static MoveSelectionElseLineDownAction
innr public static MoveSelectionElseLineUpAction
innr public static RedoAction
innr public static ReindentLineAction
innr public static RemoveLineAction
innr public static RemoveLineBeginAction
innr public static RemoveSelectionAction
innr public static RemoveTabAction
innr public static RemoveWordNextAction
innr public static RemoveWordPreviousAction
innr public static RunMacroAction
innr public static ScrollDownAction
innr public static ScrollUpAction
innr public static SelectIdentifierAction
innr public static SelectNextParameterAction
innr public static ShiftLineAction
innr public static StartMacroRecordingAction
innr public static StartNewLine
innr public static StopMacroRecordingAction
innr public static ToggleLineNumbersAction
innr public static ToggleRectangularSelectionAction
innr public static ToggleTypingModeAction
innr public static UndoAction
innr public static WordMatchAction
supr java.lang.Object
hfds LOG
hcls DeprecatedFoldAction

CLSS public static org.netbeans.editor.ActionFactory$AbbrevExpandAction
 outer org.netbeans.editor.ActionFactory
 anno 0 java.lang.Deprecated()
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$AbbrevResetAction
 outer org.netbeans.editor.ActionFactory
 anno 0 java.lang.Deprecated()
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$AdjustCaretAction
 outer org.netbeans.editor.ActionFactory
cons public init(int)
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public static org.netbeans.editor.ActionFactory$AdjustCaretAction createAdjustBottom()
meth public static org.netbeans.editor.ActionFactory$AdjustCaretAction createAdjustCenter()
meth public static org.netbeans.editor.ActionFactory$AdjustCaretAction createAdjustTop()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds percentFromWindowTop,serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$AdjustWindowAction
 outer org.netbeans.editor.ActionFactory
cons public init(int)
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public static org.netbeans.editor.ActionFactory$AdjustWindowAction createAdjustBottom()
meth public static org.netbeans.editor.ActionFactory$AdjustWindowAction createAdjustCenter()
meth public static org.netbeans.editor.ActionFactory$AdjustWindowAction createAdjustTop()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds percentFromWindowTop,serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$AnnotationsCyclingAction
 outer org.netbeans.editor.ActionFactory
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction

CLSS public static org.netbeans.editor.ActionFactory$ChangeCaseAction
 outer org.netbeans.editor.ActionFactory
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public static org.netbeans.editor.ActionFactory$ChangeCaseAction createSwitchCase()
meth public static org.netbeans.editor.ActionFactory$ChangeCaseAction createToLowerCase()
meth public static org.netbeans.editor.ActionFactory$ChangeCaseAction createToUpperCase()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds changeCaseMode,serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$CollapseAllFolds
 outer org.netbeans.editor.ActionFactory
 anno 0 java.lang.Deprecated()
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction

CLSS public static org.netbeans.editor.ActionFactory$CollapseFold
 outer org.netbeans.editor.ActionFactory
 anno 0 java.lang.Deprecated()
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction

CLSS public static org.netbeans.editor.ActionFactory$CopySelectionElseLineDownAction
 outer org.netbeans.editor.ActionFactory
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$CopySelectionElseLineUpAction
 outer org.netbeans.editor.ActionFactory
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$CutToLineBeginOrEndAction
 outer org.netbeans.editor.ActionFactory
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction

CLSS public static org.netbeans.editor.ActionFactory$DumpViewHierarchyAction
 outer org.netbeans.editor.ActionFactory
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction

CLSS public static org.netbeans.editor.ActionFactory$ExpandAllFolds
 outer org.netbeans.editor.ActionFactory
 anno 0 java.lang.Deprecated()
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction

CLSS public static org.netbeans.editor.ActionFactory$ExpandFold
 outer org.netbeans.editor.ActionFactory
 anno 0 java.lang.Deprecated()
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction

CLSS public static org.netbeans.editor.ActionFactory$FirstNonWhiteAction
 outer org.netbeans.editor.ActionFactory
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$FormatAction
 outer org.netbeans.editor.ActionFactory
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth protected void actionNameUpdate(java.lang.String)
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds indentOnly,serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$GenerateGutterPopupAction
 outer org.netbeans.editor.ActionFactory
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public javax.swing.JMenuItem getPopupMenuItem(javax.swing.text.JTextComponent)
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$InsertDateTimeAction
 outer org.netbeans.editor.ActionFactory
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$JumpListNextAction
 outer org.netbeans.editor.ActionFactory
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds pcl,serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$JumpListNextComponentAction
 outer org.netbeans.editor.ActionFactory
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$JumpListPrevAction
 outer org.netbeans.editor.ActionFactory
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds pcl,serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$JumpListPrevComponentAction
 outer org.netbeans.editor.ActionFactory
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$LastNonWhiteAction
 outer org.netbeans.editor.ActionFactory
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$MoveSelectionElseLineDownAction
 outer org.netbeans.editor.ActionFactory
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$MoveSelectionElseLineUpAction
 outer org.netbeans.editor.ActionFactory
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$RedoAction
 outer org.netbeans.editor.ActionFactory
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$ReindentLineAction
 outer org.netbeans.editor.ActionFactory
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth protected void actionNameUpdate(java.lang.String)
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds reindent,serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$RemoveLineAction
 outer org.netbeans.editor.ActionFactory
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$RemoveLineBeginAction
 outer org.netbeans.editor.ActionFactory
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$RemoveSelectionAction
 outer org.netbeans.editor.ActionFactory
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$RemoveTabAction
 outer org.netbeans.editor.ActionFactory
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$RemoveWordNextAction
 outer org.netbeans.editor.ActionFactory
 anno 0 java.lang.Deprecated()
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction

CLSS public static org.netbeans.editor.ActionFactory$RemoveWordPreviousAction
 outer org.netbeans.editor.ActionFactory
 anno 0 java.lang.Deprecated()
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction

CLSS public static org.netbeans.editor.ActionFactory$RunMacroAction
 outer org.netbeans.editor.ActionFactory
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String)
meth protected void error(javax.swing.text.JTextComponent,java.lang.String)
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds macroName,runningActions,serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$ScrollDownAction
 outer org.netbeans.editor.ActionFactory
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction

CLSS public static org.netbeans.editor.ActionFactory$ScrollUpAction
 outer org.netbeans.editor.ActionFactory
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction

CLSS public static org.netbeans.editor.ActionFactory$SelectIdentifierAction
 outer org.netbeans.editor.ActionFactory
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$SelectNextParameterAction
 outer org.netbeans.editor.ActionFactory
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$ShiftLineAction
 outer org.netbeans.editor.ActionFactory
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth protected void actionNameUpdate(java.lang.String)
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$StartMacroRecordingAction
 outer org.netbeans.editor.ActionFactory
 anno 0 java.lang.Deprecated()
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$StartNewLine
 outer org.netbeans.editor.ActionFactory
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction

CLSS public static org.netbeans.editor.ActionFactory$StopMacroRecordingAction
 outer org.netbeans.editor.ActionFactory
 anno 0 java.lang.Deprecated()
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth protected org.netbeans.editor.MacroDialogSupport getMacroDialogSupport(java.lang.Class)
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$ToggleLineNumbersAction
 outer org.netbeans.editor.ActionFactory
 anno 0 java.lang.Deprecated()
cons public init()
meth protected boolean isLineNumbersVisible()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth protected void toggleLineNumbers()
meth public javax.swing.JMenuItem getPopupMenuItem(javax.swing.text.JTextComponent)
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds item,serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$ToggleRectangularSelectionAction
 outer org.netbeans.editor.ActionFactory
cons public init()
intf java.beans.PropertyChangeListener
intf javax.swing.event.DocumentListener
intf org.openide.util.ContextAwareAction
intf org.openide.util.actions.Presenter$Toolbar
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public java.awt.Component getToolbarPresenter()
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
meth public void changedUpdate(javax.swing.event.DocumentEvent)
meth public void insertUpdate(javax.swing.event.DocumentEvent)
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void removeUpdate(javax.swing.event.DocumentEvent)
supr org.netbeans.editor.BaseAction
hfds paneRef,serialVersionUID,toggleButtonRef

CLSS public static org.netbeans.editor.ActionFactory$ToggleTypingModeAction
 outer org.netbeans.editor.ActionFactory
 anno 0 java.lang.Deprecated()
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$UndoAction
 outer org.netbeans.editor.ActionFactory
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$WordMatchAction
 outer org.netbeans.editor.ActionFactory
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth protected void actionNameUpdate(java.lang.String)
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds matchNext,serialVersionUID

CLSS public abstract interface org.netbeans.editor.AdjustFinder
intf org.netbeans.editor.Finder
meth public abstract int adjustLimitPos(org.netbeans.editor.BaseDocument,int)
meth public abstract int adjustStartPos(org.netbeans.editor.BaseDocument,int)

CLSS public org.netbeans.editor.Analyzer
fld public final static char[] EMPTY_CHAR_ARRAY
meth public static boolean blocksHit(int[],int,int)
meth public static boolean endsWith(char[],char[])
meth public static boolean equals(java.lang.String,char[])
meth public static boolean equals(java.lang.String,char[],int,int)
meth public static boolean isSpace(char[],int,int)
meth public static boolean isSpace(java.lang.String)
meth public static boolean isWhitespace(char[],int,int)
meth public static boolean startsWith(char[],char[])
meth public static char[] concat(char[],char[])
meth public static char[] createSpacesBuffer(int)
meth public static char[] createWhiteSpaceFillBuffer(int,int,int)
 anno 0 java.lang.Deprecated()
meth public static char[] createWhitespaceFillBuffer(int,int,int)
meth public static char[] extract(char[],int,int)
meth public static char[] getSpacesBuffer(int)
meth public static char[] getTabsBuffer(int)
meth public static char[] loadFile(java.lang.String) throws java.io.IOException
meth public static int blocksIndex(int[],int,int)
meth public static int convertLFToLS(char[],int,char[],java.lang.String)
meth public static int convertLSToLF(char[],int)
meth public static int findFirstLFOffset(char[],int,int)
meth public static int findFirstLFOffset(java.lang.String)
meth public static int findFirstNonSpace(char[],int,int)
meth public static int findFirstNonTab(char[],int,int)
meth public static int findFirstNonWhite(char[],int,int)
meth public static int findFirstTab(char[],int,int)
meth public static int findFirstTabOrLF(char[],int,int)
meth public static int findLastNonWhite(char[],int,int)
meth public static int getColumn(char[],int,int,int,int)
meth public static int getLFCount(char[])
meth public static int getLFCount(char[],int,int)
meth public static int getLFCount(java.lang.String)
meth public static java.lang.Object getPlatformLS()
meth public static java.lang.String convertLSToLF(java.lang.String)
meth public static java.lang.String getIndentString(int,boolean,int)
meth public static java.lang.String getSpacesString(int)
meth public static java.lang.String getWhitespaceString(int,int,boolean,int)
meth public static java.lang.String removeSpaces(java.lang.String)
meth public static java.lang.String testLS(char[],int)
meth public static void initialRead(org.netbeans.editor.BaseDocument,java.io.Reader,boolean) throws java.io.IOException
meth public static void reverse(char[],int)
supr java.lang.Object
hfds MAX_CACHED_SPACES_STRING_LENGTH,platformLS,spacesBuffer,spacesStrings,tabsBuffer

CLSS public abstract org.netbeans.editor.AnnotationDesc
cons public init(int,int)
fld public final static java.lang.String PROP_ANNOTATION_TYPE = "annotationType"
fld public final static java.lang.String PROP_MOVE_TO_FRONT = "moveToFront"
fld public final static java.lang.String PROP_SHORT_DESCRIPTION = "shortDescription"
intf java.lang.Comparable<org.netbeans.editor.AnnotationDesc>
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract int getLine()
meth public abstract int getOffset()
meth public abstract java.lang.String getAnnotationType()
meth public abstract java.lang.String getShortDescription()
meth public boolean isDefaultGlyph()
meth public boolean isVisible()
meth public boolean isWholeLine()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public int compareTo(org.netbeans.editor.AnnotationDesc)
meth public int getLength()
meth public int getOrderNumber()
meth public java.awt.Image getGlyph()
meth public java.lang.String toString()
meth public javax.swing.Action[] getActions()
meth public org.netbeans.editor.AnnotationType getAnnotationTypeInstance()
meth public org.netbeans.editor.Coloring getColoring()
meth public void updateAnnotationType()
supr java.lang.Object
hfds counter,length,mark,order,support,type

CLSS public org.netbeans.editor.AnnotationType
cons public init()
fld public final static java.lang.String PROP_ACTIONS = "actions"
fld public final static java.lang.String PROP_ACTIONS_FOLDER = "actionsFolder"
fld public final static java.lang.String PROP_BROWSEABLE = "browseable"
fld public final static java.lang.String PROP_COMBINATIONS = "combinations"
fld public final static java.lang.String PROP_COMBINATION_MINIMUM_OPTIONALS = "combinationMinimumOptionals"
fld public final static java.lang.String PROP_COMBINATION_ORDER = "combinationOrder"
fld public final static java.lang.String PROP_COMBINATION_TOOLTIP_TEXT_KEY = "tooltipTextKey"
fld public final static java.lang.String PROP_CONTENT_TYPE = "contenttype"
fld public final static java.lang.String PROP_CUSTOM_SIDEBAR_COLOR = "customSidebarColor"
fld public final static java.lang.String PROP_DESCRIPTION = "description"
fld public final static java.lang.String PROP_DESCRIPTION_KEY = "desciptionKey"
fld public final static java.lang.String PROP_FILE = "file"
fld public final static java.lang.String PROP_FOREGROUND_COLOR = "foreground"
fld public final static java.lang.String PROP_GLYPH_URL = "glyph"
fld public final static java.lang.String PROP_HIGHLIGHT_COLOR = "highlight"
fld public final static java.lang.String PROP_INHERIT_FOREGROUND_COLOR = "inheritForegroundColor"
fld public final static java.lang.String PROP_LOCALIZING_BUNDLE = "bundle"
fld public final static java.lang.String PROP_NAME = "name"
fld public final static java.lang.String PROP_PRIORITY = "priority"
fld public final static java.lang.String PROP_SEVERITY = "severity"
fld public final static java.lang.String PROP_TOOLTIP_TEXT = "tooltipText"
fld public final static java.lang.String PROP_USE_CUSTOM_SIDEBAR_COLOR = "useCustomSidebarColor"
fld public final static java.lang.String PROP_USE_HIGHLIGHT_COLOR = "useHighlightColor"
fld public final static java.lang.String PROP_USE_WAVEUNDERLINE_COLOR = "useWaveUnderlineColor"
fld public final static java.lang.String PROP_VISIBLE = "visible"
fld public final static java.lang.String PROP_WAVEUNDERLINE_COLOR = "waveunderline"
fld public final static java.lang.String PROP_WHOLE_LINE = "wholeline"
innr public final static CombinationMember
innr public final static Severity
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public boolean isBrowseable()
meth public boolean isDefaultGlyph()
meth public boolean isInheritForegroundColor()
meth public boolean isUseCustomSidebarColor()
meth public boolean isUseHighlightColor()
meth public boolean isUseWaveUnderlineColor()
meth public boolean isVisible()
meth public boolean isWholeLine()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public int getCombinationOrder()
meth public int getMinimumOptionals()
meth public int getPriority()
meth public java.awt.Color getCustomSidebarColor()
meth public java.awt.Color getForegroundColor()
meth public java.awt.Color getHighlight()
meth public java.awt.Color getWaveUnderlineColor()
meth public java.awt.Image getGlyphImage()
meth public java.lang.Object getProp(java.lang.String)
meth public java.lang.String getContentType()
meth public java.lang.String getDescription()
meth public java.lang.String getName()
meth public java.lang.String getTooltipText()
meth public java.lang.String toString()
meth public java.net.URL getGlyph()
meth public javax.swing.Action[] getActions()
meth public org.netbeans.editor.AnnotationType$CombinationMember[] getCombinations()
meth public org.netbeans.editor.AnnotationType$Severity getSeverity()
meth public org.netbeans.editor.Coloring getColoring()
meth public void putProp(java.lang.Object,java.lang.Object)
meth public void setActions(javax.swing.Action[])
meth public void setBrowseable(boolean)
meth public void setCombinationOrder(int)
meth public void setCombinationOrder(java.lang.String)
meth public void setCombinations(org.netbeans.editor.AnnotationType$CombinationMember[])
meth public void setContentType(java.lang.String)
meth public void setCustomSidebarColor(java.awt.Color)
meth public void setDescription(java.lang.String)
meth public void setForegroundColor(java.awt.Color)
meth public void setGlyph(java.net.URL)
meth public void setHighlight(java.awt.Color)
meth public void setInheritForegroundColor(boolean)
meth public void setMinimumOptionals(int)
meth public void setMinimumOptionals(java.lang.String)
meth public void setName(java.lang.String)
meth public void setPriority(int)
meth public void setSeverity(org.netbeans.editor.AnnotationType$Severity)
meth public void setTooltipText(java.lang.String)
meth public void setUseCustomSidebarColor(boolean)
meth public void setUseHighlightColor(boolean)
meth public void setUseWaveUnderlineColor(boolean)
meth public void setVisible(boolean)
meth public void setVisible(java.lang.String)
meth public void setWaveUnderlineColor(java.awt.Color)
meth public void setWholeLine(boolean)
meth public void setWholeLine(java.lang.String)
supr java.lang.Object
hfds LOG,col,img,properties,support

CLSS public final static org.netbeans.editor.AnnotationType$CombinationMember
 outer org.netbeans.editor.AnnotationType
cons public init(java.lang.String,boolean,boolean,int)
cons public init(java.lang.String,boolean,boolean,java.lang.String)
meth public boolean isAbsorbAll()
meth public boolean isOptional()
meth public int getMinimumCount()
meth public java.lang.String getName()
supr java.lang.Object
hfds absorbAll,minimumCount,optional,type

CLSS public final static org.netbeans.editor.AnnotationType$Severity
 outer org.netbeans.editor.AnnotationType
fld public final static org.netbeans.editor.AnnotationType$Severity STATUS_ERROR
fld public final static org.netbeans.editor.AnnotationType$Severity STATUS_NONE
fld public final static org.netbeans.editor.AnnotationType$Severity STATUS_OK
fld public final static org.netbeans.editor.AnnotationType$Severity STATUS_WARNING
intf java.lang.Comparable
meth public boolean equals(java.lang.Object)
meth public int compareTo(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public static java.awt.Color getDefaultColor(org.netbeans.editor.AnnotationType$Severity)
meth public static org.netbeans.editor.AnnotationType$Severity getCompoundStatus(org.netbeans.editor.AnnotationType$Severity,org.netbeans.editor.AnnotationType$Severity)
meth public static org.netbeans.editor.AnnotationType$Severity valueOf(java.lang.String)
supr java.lang.Object
hfds DEFAULT_STATUS_COLORS,STATUS_ERROR_NUMBER,STATUS_NAMES,STATUS_NONE_NUMBER,STATUS_OK_NUMBER,STATUS_WARNING_NUMBER,VALUES,status

CLSS public org.netbeans.editor.AnnotationTypes
fld public final static java.lang.String PROP_ANNOTATION_TYPES = "annotationTypes"
fld public final static java.lang.String PROP_BACKGROUND_DRAWING = "backgroundDrawing"
fld public final static java.lang.String PROP_BACKGROUND_GLYPH_ALPHA = "backgroundGlyphAlpha"
fld public final static java.lang.String PROP_COMBINE_GLYPHS = "combineGlyphs"
fld public final static java.lang.String PROP_GLYPHS_OVER_LINE_NUMBERS = "glyphsOverLineNumbers"
fld public final static java.lang.String PROP_SHOW_GLYPH_GUTTER = "showGlyphGutter"
innr public abstract interface static Loader
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public final org.netbeans.editor.AnnotationType getType(java.lang.String)
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removeType(java.lang.String)
meth public final void setTypes(java.util.Map)
meth public int getAnnotationTypeNamesCount()
meth public int getVisibleAnnotationTypeNamesCount()
meth public java.lang.Boolean isBackgroundDrawing()
meth public java.lang.Boolean isCombineGlyphs()
meth public java.lang.Boolean isGlyphsOverLineNumbers()
meth public java.lang.Boolean isShowGlyphGutter()
meth public java.lang.Integer getBackgroundGlyphAlpha()
meth public java.util.Iterator<java.lang.String> getAnnotationTypeNames()
meth public static java.net.URL getDefaultGlyphURL()
meth public static org.netbeans.editor.AnnotationTypes getTypes()
meth public void registerLoader(org.netbeans.editor.AnnotationTypes$Loader)
meth public void saveSetting(java.lang.String,java.lang.Object)
meth public void saveType(org.netbeans.editor.AnnotationType)
meth public void setBackgroundDrawing(java.lang.Boolean)
meth public void setBackgroundGlyphAlpha(int)
meth public void setCombineGlyphs(java.lang.Boolean)
meth public void setGlyphsOverLineNumbers(java.lang.Boolean)
meth public void setShowGlyphGutter(java.lang.Boolean)
supr java.lang.Object
hfds allTypes,annoTypes,defaultGlyphIcon,loadedSettings,loadedTypes,loader,loadingInProgress,properties,support
hcls FirePropertyChange

CLSS public abstract interface static org.netbeans.editor.AnnotationTypes$Loader
 outer org.netbeans.editor.AnnotationTypes
meth public abstract void loadSettings()
meth public abstract void loadTypes()
meth public abstract void saveSetting(java.lang.String,java.lang.Object)
meth public abstract void saveType(org.netbeans.editor.AnnotationType)

CLSS public org.netbeans.editor.Annotations
cons public init(org.netbeans.editor.BaseDocument)
fld public final static java.util.Comparator<javax.swing.JMenu> MENU_COMPARATOR
innr public abstract interface static AnnotationsListener
innr public final static MenuComparator
innr public static LineAnnotations
intf javax.swing.event.DocumentListener
meth protected org.netbeans.editor.Annotations$LineAnnotations getLineAnnotations(int)
meth protected void fireChangedAll()
meth protected void fireChangedLine(int)
meth protected void refreshLine(int)
meth public boolean isGlyphButtonColumn()
meth public boolean isGlyphColumn()
meth public int getNextLineWithAnnotation(int)
meth public int getNumberOfAnnotations(int)
meth public java.lang.String toString()
meth public javax.swing.JMenu createMenu(org.netbeans.editor.BaseKit,int)
meth public javax.swing.JPopupMenu createPopupMenu(org.netbeans.editor.BaseKit,int)
meth public org.netbeans.editor.AnnotationDesc activateNextAnnotation(int)
meth public org.netbeans.editor.AnnotationDesc getActiveAnnotation(int)
meth public org.netbeans.editor.AnnotationDesc getActiveAnnotation(org.netbeans.editor.Mark)
meth public org.netbeans.editor.AnnotationDesc getAnnotation(int,java.lang.String)
meth public org.netbeans.editor.AnnotationDesc[] getPasiveAnnotations(int)
 anno 0 java.lang.Deprecated()
meth public org.netbeans.editor.AnnotationDesc[] getPassiveAnnotations(int)
meth public org.netbeans.editor.AnnotationDesc[] getPassiveAnnotationsForLine(int)
meth public void addAnnotation(org.netbeans.editor.AnnotationDesc)
meth public void addAnnotationsListener(org.netbeans.editor.Annotations$AnnotationsListener)
meth public void changedUpdate(javax.swing.event.DocumentEvent)
meth public void frontAnnotation(org.netbeans.editor.AnnotationDesc)
meth public void insertUpdate(javax.swing.event.DocumentEvent)
meth public void removeAnnotation(org.netbeans.editor.AnnotationDesc)
meth public void removeAnnotationsListener(org.netbeans.editor.Annotations$AnnotationsListener)
meth public void removeUpdate(javax.swing.event.DocumentEvent)
supr java.lang.Object
hfds doc,glyphButtonColumn,glyphColumn,l,lastGetLineAnnotationsIdx,lastGetLineAnnotationsLine,lastGetLineAnnotationsResult,lineAnnotationsArray,lineAnnotationsByMark,listenerList,markChain,menuInitialized,weakListenerList
hcls AnnotationCombination,DelayedMenu,LineAnnotationsComparator

CLSS public abstract interface static org.netbeans.editor.Annotations$AnnotationsListener
 outer org.netbeans.editor.Annotations
intf java.util.EventListener
meth public abstract void changedAll()
meth public abstract void changedLine(int)

CLSS public static org.netbeans.editor.Annotations$LineAnnotations
 outer org.netbeans.editor.Annotations
cons protected init()
meth public boolean activate(org.netbeans.editor.AnnotationDesc)
meth public boolean isMarkStillReferenced(org.netbeans.editor.Mark)
meth public int getCount()
meth public int getLine()
meth public java.util.Iterator<org.netbeans.editor.AnnotationDesc> getAnnotations()
meth public org.netbeans.editor.AnnotationDesc activateNext()
meth public org.netbeans.editor.AnnotationDesc getActive()
meth public org.netbeans.editor.AnnotationDesc[] getPasive()
 anno 0 java.lang.Deprecated()
meth public org.netbeans.editor.AnnotationDesc[] getPassive()
meth public void addAnnotation(org.netbeans.editor.AnnotationDesc)
meth public void refreshAnnotations()
meth public void removeAnnotation(org.netbeans.editor.AnnotationDesc)
meth public void setLine(int)
supr java.lang.Object
hfds active,annos,annosVisible

CLSS public final static org.netbeans.editor.Annotations$MenuComparator
 outer org.netbeans.editor.Annotations
cons public init()
intf java.util.Comparator<javax.swing.JMenu>
meth public int compare(javax.swing.JMenu,javax.swing.JMenu)
supr java.lang.Object

CLSS public abstract interface org.netbeans.editor.AtomicLockDocument
 anno 0 java.lang.Deprecated()
intf javax.swing.text.Document
meth public abstract void addAtomicLockListener(org.netbeans.editor.AtomicLockListener)
meth public abstract void atomicLock()
meth public abstract void atomicUndo()
meth public abstract void atomicUnlock()
meth public abstract void removeAtomicLockListener(org.netbeans.editor.AtomicLockListener)

CLSS public org.netbeans.editor.AtomicLockEvent
 anno 0 java.lang.Deprecated()
supr org.netbeans.api.editor.document.AtomicLockEvent

CLSS public abstract interface org.netbeans.editor.AtomicLockListener
 anno 0 java.lang.Deprecated()
intf java.util.EventListener
meth public abstract void atomicLock(org.netbeans.editor.AtomicLockEvent)
meth public abstract void atomicUnlock(org.netbeans.editor.AtomicLockEvent)

CLSS public abstract org.netbeans.editor.BaseAction
cons public init()
cons public init(int)
cons public init(java.lang.String)
cons public init(java.lang.String,int)
fld protected int updateMask
fld public final static int ABBREV_RESET = 4
 anno 0 java.lang.Deprecated()
fld public final static int CLEAR_STATUS_TEXT = 32
fld public final static int MAGIC_POSITION_RESET = 2
fld public final static int NO_RECORDING = 64
fld public final static int SAVE_POSITION = 128
fld public final static int SELECTION_REMOVE = 1
fld public final static int UNDO_MERGE_RESET = 8
fld public final static int WORD_MATCH_RESET = 16
fld public final static java.lang.String ICON_RESOURCE_PROPERTY = "IconResource"
fld public final static java.lang.String LOCALE_DESC_PREFIX = "desc-"
fld public final static java.lang.String LOCALE_POPUP_PREFIX = "popup-"
fld public final static java.lang.String NO_KEYBINDING = "no-keybinding"
fld public final static java.lang.String POPUP_MENU_TEXT = "PopupMenuText"
meth protected boolean asynchonous()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth protected java.lang.Object createDefaultValue(java.lang.String)
meth protected java.lang.Object findValue(java.lang.String)
 anno 0 java.lang.Deprecated()
meth protected java.lang.Object getDefaultShortDescription()
meth protected void actionNameUpdate(java.lang.String)
meth public abstract void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
meth public final void actionPerformed(java.awt.event.ActionEvent)
meth public java.lang.Object getValue(java.lang.String)
meth public java.lang.String getPopupMenuText(javax.swing.text.JTextComponent)
meth public javax.swing.JMenuItem getPopupMenuItem(javax.swing.text.JTextComponent)
meth public void putValue(java.lang.String,java.lang.Object)
meth public void updateComponent(javax.swing.text.JTextComponent)
meth public void updateComponent(javax.swing.text.JTextComponent,int)
supr javax.swing.text.TextAction
hfds UILOG,UI_LOG_DETAILED,recording,serialVersionUID

CLSS public org.netbeans.editor.BaseCaret
cons public init()
fld protected char[] dotChar
fld protected java.awt.Color textBackColor
fld protected java.awt.Color textForeColor
fld protected java.awt.Font afterCaretFont
fld protected java.awt.Font beforeCaretFont
fld protected javax.swing.Timer flasher
fld protected javax.swing.event.ChangeEvent changeEvent
fld protected javax.swing.event.EventListenerList listenerList
fld protected javax.swing.text.JTextComponent component
fld public final static java.lang.String BLOCK_CARET = "block-caret"
fld public final static java.lang.String LINE_CARET = "line-caret"
fld public final static java.lang.String THICK_LINE_CARET = "thick-line-caret"
fld public final static java.lang.String THIN_LINE_CARET = "thin-line-caret"
intf java.awt.event.ActionListener
intf java.awt.event.MouseListener
intf java.awt.event.MouseMotionListener
intf java.beans.PropertyChangeListener
intf javax.swing.event.DocumentListener
intf javax.swing.text.Caret
intf org.netbeans.api.editor.fold.FoldHierarchyListener
intf org.netbeans.editor.AtomicLockListener
meth protected boolean isDragPossible(java.awt.event.MouseEvent)
meth protected int mapDragOperationFromModifiers(java.awt.event.MouseEvent)
meth protected javax.swing.JComponent getEventComponent(java.awt.event.MouseEvent)
meth protected void fireStateChanged()
meth protected void modelChanged(org.netbeans.editor.BaseDocument,org.netbeans.editor.BaseDocument)
meth protected void paintCustomCaret(java.awt.Graphics)
meth protected void setVisibleImpl(boolean)
meth protected void update(boolean)
meth public boolean equals(java.lang.Object)
meth public final boolean isSelectionVisible()
meth public final boolean isVisible()
meth public final java.awt.Point getMagicCaretPosition()
meth public final void refresh()
meth public int getBlinkRate()
meth public int getDot()
meth public int getMark()
meth public int hashCode()
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void atomicLock(org.netbeans.editor.AtomicLockEvent)
meth public void atomicUnlock(org.netbeans.editor.AtomicLockEvent)
meth public void changedUpdate(javax.swing.event.DocumentEvent)
meth public void deinstall(javax.swing.text.JTextComponent)
meth public void extendRectangularSelection(boolean,boolean)
meth public void foldHierarchyChanged(org.netbeans.api.editor.fold.FoldHierarchyEvent)
 anno 0 java.lang.Deprecated()
meth public void insertUpdate(javax.swing.event.DocumentEvent)
meth public void install(javax.swing.text.JTextComponent)
meth public void mouseClicked(java.awt.event.MouseEvent)
meth public void mouseDragged(java.awt.event.MouseEvent)
meth public void mouseEntered(java.awt.event.MouseEvent)
meth public void mouseExited(java.awt.event.MouseEvent)
meth public void mouseMoved(java.awt.event.MouseEvent)
meth public void mousePressed(java.awt.event.MouseEvent)
meth public void mouseReleased(java.awt.event.MouseEvent)
meth public void moveDot(int)
meth public void moveDot(int,java.awt.Rectangle,int)
 anno 0 java.lang.Deprecated()
meth public void paint(java.awt.Graphics)
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void refresh(boolean)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void removeUpdate(javax.swing.event.DocumentEvent)
meth public void setBlinkRate(int)
meth public void setDot(int)
meth public void setDot(int,boolean)
meth public void setDot(int,java.awt.Rectangle,int)
 anno 0 java.lang.Deprecated()
meth public void setDot(int,java.awt.Rectangle,int,boolean)
 anno 0 java.lang.Deprecated()
meth public void setMagicCaretPosition(java.awt.Point)
meth public void setSelectionVisible(boolean)
meth public void setVisible(boolean)
meth public void updateRectangularUpDownSelection()
supr java.lang.Object
hfds LOG,LOG_EDT,RECTANGULAR_SELECTION_PROPERTY,RECTANGULAR_SELECTION_REGIONS_PROPERTY,blinkVisible,caretBounds,caretPos,caretUpdatePending,caretVisible,inAtomicLock,inAtomicUnlock,italic,listenDoc,listenerImpl,magicCaretPosition,markPos,minSelectionEndOffset,minSelectionStartOffset,modified,mouseState,overwriteMode,prefs,prefsListener,rectangularSelection,rsDotRect,rsMarkRect,rsPaintRect,rsRegions,selectLineAction,selectWordAction,selectionVisible,serialVersionUID,showingTextCursor,type,typingModificationOccurred,undoOffset,updateAfterFoldHierarchyChange,weakPrefsListener,width,xPoints,yPoints
hcls ListenerImpl,MouseState

CLSS public org.netbeans.editor.BaseDocument
cons public init(boolean,java.lang.String)
cons public init(java.lang.Class,boolean)
 anno 0 java.lang.Deprecated()
fld protected boolean inited
fld protected boolean modified
fld protected javax.swing.text.Element defaultRootElem
fld public final static java.lang.String BLOCKS_FINDER_PROP = "blocks-finder"
fld public final static java.lang.String FILE_NAME_PROP = "file-name"
fld public final static java.lang.String FORMATTER = "formatter"
fld public final static java.lang.String ID_PROP = "id"
fld public final static java.lang.String KIT_CLASS_PROP = "kit-class"
fld public final static java.lang.String LINE_BATCH_SIZE = "line-batch-size"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String LINE_LIMIT_PROP = "line-limit"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String LS_CR = "\r"
fld public final static java.lang.String LS_CRLF = "\r\n"
fld public final static java.lang.String LS_LF = "\n"
fld public final static java.lang.String MIME_TYPE_PROP = "mimeType"
fld public final static java.lang.String READ_LINE_SEPARATOR_PROP = "__EndOfLine__"
fld public final static java.lang.String STRING_BWD_FINDER_PROP = "string-bwd-finder"
fld public final static java.lang.String STRING_FINDER_PROP = "string-finder"
fld public final static java.lang.String UNDO_MANAGER_PROP = "undo-manager"
fld public final static java.lang.String WRAP_SEARCH_MARK_PROP = "wrap-search-mark"
fld public final static java.lang.String WRITE_LINE_SEPARATOR_PROP = "write-line-separator"
innr protected static LazyPropertyMap
innr public abstract interface static PropertyEvaluator
intf org.netbeans.api.editor.document.CustomUndoDocument
intf org.netbeans.api.editor.document.LineDocument
intf org.netbeans.editor.AtomicLockDocument
meth protected final int getAtomicDepth()
meth protected java.util.Dictionary createDocumentProperties(java.util.Dictionary)
meth protected org.netbeans.editor.BaseDocumentEvent createDocumentEvent(int,int,javax.swing.event.DocumentEvent$EventType)
meth protected void fireChangedUpdate(javax.swing.event.DocumentEvent)
meth protected void fireInsertUpdate(javax.swing.event.DocumentEvent)
meth protected void fireRemoveUpdate(javax.swing.event.DocumentEvent)
meth protected void fireUndoableEditUpdate(javax.swing.event.UndoableEditEvent)
meth protected void insertUpdate(javax.swing.text.AbstractDocument$DefaultDocumentEvent,javax.swing.text.AttributeSet)
meth protected void postRemoveUpdate(javax.swing.text.AbstractDocument$DefaultDocumentEvent)
meth protected void preInsertCheck(int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth protected void preInsertUpdate(javax.swing.text.AbstractDocument$DefaultDocumentEvent,javax.swing.text.AttributeSet)
meth protected void preRemoveCheck(int,int) throws javax.swing.text.BadLocationException
meth protected void removeUpdate(javax.swing.text.AbstractDocument$DefaultDocumentEvent)
meth public boolean isIdentifierPart(char)
meth public boolean isModifiable()
meth public boolean isModified()
meth public boolean isWhitespace(char)
meth public char[] getChars(int,int) throws javax.swing.text.BadLocationException
meth public char[] getChars(int[]) throws javax.swing.text.BadLocationException
meth public final boolean isAtomicLock()
meth public final java.lang.Class getKitClass()
 anno 0 java.lang.Deprecated()
meth public final void atomicLock()
 anno 0 java.lang.Deprecated()
meth public final void atomicUnlock()
 anno 0 java.lang.Deprecated()
meth public final void breakAtomicLock()
meth public final void extWriteLock()
meth public final void extWriteUnlock()
meth public int find(org.netbeans.editor.Finder,int,int) throws javax.swing.text.BadLocationException
meth public int getShiftWidth()
 anno 0 java.lang.Deprecated()
meth public int getTabSize()
meth public int processText(org.netbeans.editor.TextBatchProcessor,int,int) throws javax.swing.text.BadLocationException
meth public java.lang.String getText(int[]) throws javax.swing.text.BadLocationException
meth public java.lang.String toString()
meth public java.lang.String toStringDetail()
meth public javax.swing.text.Element getDefaultRootElement()
meth public javax.swing.text.Element getParagraphElement(int)
meth public javax.swing.text.Element[] getRootElements()
meth public javax.swing.text.Position createPosition(int,javax.swing.text.Position$Bias) throws javax.swing.text.BadLocationException
meth public org.netbeans.editor.Annotations getAnnotations()
meth public org.netbeans.editor.CharSeq getText()
meth public org.netbeans.editor.SyntaxSupport getSyntaxSupport()
 anno 0 java.lang.Deprecated()
meth public void addAtomicLockListener(org.netbeans.api.editor.document.AtomicLockListener)
meth public void addAtomicLockListener(org.netbeans.editor.AtomicLockListener)
 anno 0 java.lang.Deprecated()
meth public void addDocumentListener(javax.swing.event.DocumentListener)
meth public void addPostModificationDocumentListener(javax.swing.event.DocumentListener)
meth public void addUndoableEdit(javax.swing.undo.UndoableEdit)
meth public void addUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public void addUpdateDocumentListener(javax.swing.event.DocumentListener)
meth public void atomicUndo()
meth public void checkTrailingSpaces(int)
meth public void getChars(int,char[],int,int) throws javax.swing.text.BadLocationException
meth public void insertString(int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth public void invalidateSyntaxMarks()
meth public void print(org.netbeans.editor.PrintContainer)
meth public void print(org.netbeans.editor.PrintContainer,boolean,boolean,int,int)
meth public void print(org.netbeans.editor.PrintContainer,boolean,java.lang.Boolean,int,int)
meth public void read(java.io.Reader,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public void remove(int,int) throws javax.swing.text.BadLocationException
meth public void removeAtomicLockListener(org.netbeans.api.editor.document.AtomicLockListener)
meth public void removeAtomicLockListener(org.netbeans.editor.AtomicLockListener)
 anno 0 java.lang.Deprecated()
meth public void removeDocumentListener(javax.swing.event.DocumentListener)
meth public void removePostModificationDocumentListener(javax.swing.event.DocumentListener)
meth public void removeUpdateDocumentListener(javax.swing.event.DocumentListener)
meth public void render(java.lang.Runnable)
meth public void repaintBlock(int,int)
 anno 0 java.lang.Deprecated()
meth public void replace(int,int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth public void resetUndoMerge()
meth public void runAtomic(java.lang.Runnable)
meth public void runAtomicAsUser(java.lang.Runnable)
meth public void setPostModificationDocumentListener(javax.swing.event.DocumentListener)
 anno 0 java.lang.Deprecated()
meth public void write(java.io.Writer,int,int) throws java.io.IOException,javax.swing.text.BadLocationException
supr javax.swing.text.AbstractDocument
hfds DEACTIVATE_LEXER_THRESHOLD,EDITABLE_PROP,LAST_MODIFICATION_TIMESTAMP_PROP,LOG,LOG_LISTENER,MODIFICATION_LISTENER_PROP,SUPPORTS_MODIFICATION_LISTENER_PROP,VERSION_PROP,annotations,annotationsLock,atomicDepth,atomicEdits,atomicLockEventInstance,atomicLockListenerList,composedText,debugNoText,debugRead,debugStack,deprecatedKitClass,filterBypass,fixLineSyntaxState,identifierAcceptor,lastModifyUndoEdit,lastPositionEditedByTyping,lineRootElement,mimeType,modifiable,postModificationDepth,postModificationDocumentListener,postModificationDocumentListenerList,prefs,prefsListener,removeUpdateLineUndo,runExclusiveDepth,shiftWidth,syntaxSupport,tabSize,text,undoEditWrappers,undoMergeReset,updateDocumentListenerList,weakPrefsListener,whitespaceAcceptor
hcls Accessor,AtomicCompoundEdit,BaseDocumentServices,FilterBypassImpl,MimeTypePropertyEvaluator,OldListenerAdapter,PlainEditorKit,ServicesImpl

CLSS protected static org.netbeans.editor.BaseDocument$LazyPropertyMap
 outer org.netbeans.editor.BaseDocument
cons protected init(java.util.Dictionary)
meth public java.lang.Object get(java.lang.Object)
meth public java.lang.Object put(java.lang.Object,java.lang.Object)
supr java.util.Hashtable
hfds pcs

CLSS public abstract interface static org.netbeans.editor.BaseDocument$PropertyEvaluator
 outer org.netbeans.editor.BaseDocument
meth public abstract java.lang.Object getValue()

CLSS public org.netbeans.editor.BaseDocumentEvent
cons public init(org.netbeans.editor.BaseDocument,int,int,javax.swing.event.DocumentEvent$EventType)
meth protected javax.swing.undo.UndoableEdit findEdit(java.lang.Class)
meth public boolean addEdit(javax.swing.undo.UndoableEdit)
meth public boolean canMerge(org.netbeans.editor.BaseDocumentEvent)
meth public boolean canRedo()
meth public boolean canUndo()
meth public boolean isInProgress()
meth public boolean isInRedo()
meth public boolean isInUndo()
meth public boolean replaceEdit(javax.swing.undo.UndoableEdit)
meth public char[] getChars()
 anno 0 java.lang.Deprecated()
meth public final javax.swing.text.AttributeSet getChangeAttributes()
meth public int getLFCount()
meth public int getLine()
 anno 0 java.lang.Deprecated()
meth public int getSyntaxUpdateOffset()
meth public java.lang.String getRedoPresentationName()
meth public java.lang.String getText()
meth public java.lang.String getUndoPresentationName()
meth public java.lang.String toString()
meth public javax.swing.event.DocumentEvent$ElementChange getChange(javax.swing.text.Element)
meth public void die()
meth public void end()
meth public void redo()
meth public void undo()
supr javax.swing.text.AbstractDocument$DefaultDocumentEvent
hfds alive2,attribs,changeLookup2,debugUndo,fixLineSyntaxState,hasBeenDone2,inProgress2,inRedo,inUndo,lfCount,modifyUndoEdit,previous,serialVersionUID

CLSS public abstract org.netbeans.editor.BaseElement
cons public init(org.netbeans.editor.BaseDocument,org.netbeans.editor.BaseElement,javax.swing.text.AttributeSet)
fld protected javax.swing.text.AttributeSet attrs
fld protected org.netbeans.editor.BaseDocument doc
fld protected org.netbeans.editor.BaseElement parent
fld public final static java.lang.String ElementNameAttribute = "$ename"
intf javax.swing.text.Element
meth public abstract boolean isLeaf()
meth public abstract int getElementCount()
meth public abstract int getElementIndex(int)
meth public abstract int getEndOffset()
meth public abstract int getStartOffset()
meth public abstract javax.swing.text.Element getElement(int)
meth public abstract org.netbeans.editor.Mark getEndMark()
meth public abstract org.netbeans.editor.Mark getStartMark()
meth public java.lang.String getName()
meth public javax.swing.text.AttributeSet getAttributes()
meth public javax.swing.text.Document getDocument()
meth public javax.swing.text.Element getParentElement()
supr java.lang.Object

CLSS public org.netbeans.editor.BaseImageTokenID
cons public init(java.lang.String)
cons public init(java.lang.String,int)
cons public init(java.lang.String,int,java.lang.String)
cons public init(java.lang.String,int,org.netbeans.editor.TokenCategory)
cons public init(java.lang.String,int,org.netbeans.editor.TokenCategory,java.lang.String)
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,org.netbeans.editor.TokenCategory)
cons public init(java.lang.String,org.netbeans.editor.TokenCategory,java.lang.String)
intf org.netbeans.editor.ImageTokenID
meth public java.lang.String getImage()
meth public java.lang.String toString()
supr org.netbeans.editor.BaseTokenID
hfds image

CLSS public org.netbeans.editor.BaseKit
cons public init()
fld public final static int MAGIC_POSITION_MAX = 2147483646
fld public final static java.lang.String DOC_REPLACE_SELECTION_PROPERTY = "doc-replace-selection-property"
fld public final static java.lang.String abbrevExpandAction = "abbrev-expand"
fld public final static java.lang.String abbrevResetAction = "abbrev-reset"
fld public final static java.lang.String adjustCaretBottomAction = "adjust-caret-bottom"
fld public final static java.lang.String adjustCaretCenterAction = "adjust-caret-center"
fld public final static java.lang.String adjustCaretTopAction = "adjust-caret-top"
fld public final static java.lang.String adjustWindowBottomAction = "adjust-window-bottom"
fld public final static java.lang.String adjustWindowCenterAction = "adjust-window-center"
fld public final static java.lang.String adjustWindowTopAction = "adjust-window-top"
fld public final static java.lang.String annotationsCyclingAction = "annotations-cycling"
fld public final static java.lang.String collapseAllFoldsAction = "collapse-all-folds"
fld public final static java.lang.String collapseFoldAction = "collapse-fold"
fld public final static java.lang.String copySelectionElseLineDownAction = "copy-selection-else-line-down"
fld public final static java.lang.String copySelectionElseLineUpAction = "copy-selection-else-line-up"
fld public final static java.lang.String cutToLineBeginAction = "cut-to-line-begin"
fld public final static java.lang.String cutToLineEndAction = "cut-to-line-end"
fld public final static java.lang.String expandAllFoldsAction = "expand-all-folds"
fld public final static java.lang.String expandFoldAction = "expand-fold"
fld public final static java.lang.String findNextAction = "find-next"
fld public final static java.lang.String findPreviousAction = "find-previous"
fld public final static java.lang.String findSelectionAction = "find-selection"
fld public final static java.lang.String firstNonWhiteAction = "first-non-white"
fld public final static java.lang.String formatAction = "format"
fld public final static java.lang.String generateGutterPopupAction = "generate-gutter-popup"
fld public final static java.lang.String indentAction = "indent"
fld public final static java.lang.String insertDateTimeAction = "insert-date-time"
fld public final static java.lang.String jumpListNextAction = "jump-list-next"
fld public final static java.lang.String jumpListNextComponentAction = "jump-list-next-component"
fld public final static java.lang.String jumpListPrevAction = "jump-list-prev"
fld public final static java.lang.String jumpListPrevComponentAction = "jump-list-prev-component"
fld public final static java.lang.String lastNonWhiteAction = "last-non-white"
fld public final static java.lang.String lineFirstColumnAction = "caret-line-first-column"
fld public final static java.lang.String macroActionPrefix = "macro-"
fld public final static java.lang.String moveSelectionElseLineDownAction = "move-selection-else-line-down"
fld public final static java.lang.String moveSelectionElseLineUpAction = "move-selection-else-line-up"
fld public final static java.lang.String pasteFormatedAction = "paste-formated"
fld public final static java.lang.String redoAction = "redo"
fld public final static java.lang.String reformatLineAction = "reformat-line"
fld public final static java.lang.String reindentLineAction = "reindent-line"
fld public final static java.lang.String removeLineAction = "remove-line"
fld public final static java.lang.String removeLineBeginAction = "remove-line-begin"
fld public final static java.lang.String removeNextWordAction = "remove-word-next"
fld public final static java.lang.String removePreviousWordAction = "remove-word-previous"
fld public final static java.lang.String removeSelectionAction = "remove-selection"
fld public final static java.lang.String removeTabAction = "remove-tab"
fld public final static java.lang.String removeTrailingSpacesAction = "remove-trailing-spaces"
fld public final static java.lang.String scrollDownAction = "scroll-down"
fld public final static java.lang.String scrollUpAction = "scroll-up"
fld public final static java.lang.String selectIdentifierAction = "select-identifier"
fld public final static java.lang.String selectNextParameterAction = "select-next-parameter"
fld public final static java.lang.String selectionFirstNonWhiteAction = "selection-first-non-white"
fld public final static java.lang.String selectionLastNonWhiteAction = "selection-last-non-white"
fld public final static java.lang.String selectionLineFirstColumnAction = "selection-line-first-column"
fld public final static java.lang.String selectionPageDownAction = "selection-page-down"
fld public final static java.lang.String selectionPageUpAction = "selection-page-up"
fld public final static java.lang.String shiftLineLeftAction = "shift-line-left"
fld public final static java.lang.String shiftLineRightAction = "shift-line-right"
fld public final static java.lang.String splitLineAction = "split-line"
fld public final static java.lang.String startMacroRecordingAction = "start-macro-recording"
fld public final static java.lang.String startNewLineAction = "start-new-line"
fld public final static java.lang.String stopMacroRecordingAction = "stop-macro-recording"
fld public final static java.lang.String switchCaseAction = "switch-case"
fld public final static java.lang.String toLowerCaseAction = "to-lower-case"
fld public final static java.lang.String toUpperCaseAction = "to-upper-case"
fld public final static java.lang.String toggleHighlightSearchAction = "toggle-highlight-search"
fld public final static java.lang.String toggleLineNumbersAction = "toggle-line-numbers"
fld public final static java.lang.String toggleTypingModeAction = "toggle-typing-mode"
fld public final static java.lang.String undoAction = "undo"
fld public final static java.lang.String wordMatchNextAction = "word-match-next"
fld public final static java.lang.String wordMatchPrevAction = "word-match-prev"
innr public static BackwardAction
innr public static BeepAction
innr public static BeginAction
innr public static BeginLineAction
innr public static BeginWordAction
innr public static CompoundAction
innr public static CopyAction
innr public static CutAction
innr public static DefaultKeyTypedAction
innr public static DeleteCharAction
innr public static DownAction
innr public static EndAction
innr public static EndLineAction
innr public static EndWordAction
innr public static ForwardAction
innr public static InsertBreakAction
innr public static InsertContentAction
innr public static InsertStringAction
innr public static InsertTabAction
innr public static KitCompoundAction
innr public static NextWordAction
innr public static PageDownAction
innr public static PageUpAction
innr public static PasteAction
innr public static PreviousWordAction
innr public static ReadOnlyAction
innr public static RemoveTrailingSpacesAction
innr public static SelectAllAction
innr public static SelectLineAction
innr public static SelectWordAction
innr public static SplitLineAction
innr public static UpAction
innr public static WritableAction
meth protected javax.swing.Action[] createActions()
meth protected javax.swing.Action[] getCustomActions()
meth protected javax.swing.Action[] getDeclaredActions()
meth protected javax.swing.Action[] getMacroActions()
 anno 0 java.lang.Deprecated()
meth protected org.netbeans.editor.BaseTextUI createTextUI()
meth protected org.netbeans.editor.EditorUI createEditorUI()
meth protected org.netbeans.editor.EditorUI createPrintEditorUI(org.netbeans.editor.BaseDocument)
 anno 0 java.lang.Deprecated()
meth protected org.netbeans.editor.EditorUI createPrintEditorUI(org.netbeans.editor.BaseDocument,boolean,boolean)
meth protected void executeDeinstallActions(javax.swing.JEditorPane)
meth protected void executeInstallActions(javax.swing.JEditorPane)
meth protected void initDocument(org.netbeans.editor.BaseDocument)
meth protected void updateActions()
meth public final javax.swing.Action[] getActions()
meth public java.lang.Object clone()
meth public java.util.List<javax.swing.Action> translateActionNameList(java.util.List<java.lang.String>)
meth public javax.swing.Action getActionByName(java.lang.String)
meth public javax.swing.text.Caret createCaret()
meth public javax.swing.text.Document createDefaultDocument()
meth public javax.swing.text.ViewFactory getViewFactory()
meth public org.netbeans.editor.MultiKeymap getKeymap()
meth public org.netbeans.editor.Syntax createFormatSyntax(javax.swing.text.Document)
 anno 0 java.lang.Deprecated()
meth public org.netbeans.editor.Syntax createSyntax(javax.swing.text.Document)
 anno 0 java.lang.Deprecated()
meth public org.netbeans.editor.SyntaxSupport createSyntaxSupport(org.netbeans.editor.BaseDocument)
 anno 0 java.lang.Deprecated()
meth public static javax.swing.Action[] mapToActions(java.util.Map)
meth public static org.netbeans.editor.BaseKit getKit(java.lang.Class)
 anno 0 java.lang.Deprecated()
meth public static void addActionsToMap(java.util.Map<java.lang.String,javax.swing.Action>,javax.swing.Action[],java.lang.String)
meth public void deinstall(javax.swing.JEditorPane)
meth public void install(javax.swing.JEditorPane)
meth public void read(java.io.Reader,javax.swing.text.Document,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public void write(java.io.Writer,javax.swing.text.Document,int,int) throws java.io.IOException,javax.swing.text.BadLocationException
supr javax.swing.text.DefaultEditorKit
hfds IN_PASTE,KEYMAPS_AND_ACTIONS_LOCK,KIT_CNT_PREALLOC,LOG,PROP_NAVIGATE_BOUNDARIES,copyActionDef,cutActionDef,deleteNextCharActionDef,deletePrevCharActionDef,insertBreakActionDef,insertTabActionDef,keyBindingsUpdaterInited,keymapTrackers,kitActionMaps,kitActions,kitKeymaps,kits,pasteActionDef,redoActionDef,removeSelectionActionDef,removeTabActionDef,searchableKit,serialVersionUID,undoActionDef
hcls ClearUIForNullKitListener,DefaultSyntax,DefaultSyntaxTokenContext,KeybindingsAndPreferencesTracker,NullTextUI,SearchableKit

CLSS public static org.netbeans.editor.BaseKit$BackwardAction
 outer org.netbeans.editor.BaseKit
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.BaseKit$BeepAction
 outer org.netbeans.editor.BaseKit
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.BaseKit$BeginAction
 outer org.netbeans.editor.BaseKit
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.BaseKit$BeginLineAction
 outer org.netbeans.editor.BaseKit
cons public init(boolean)
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public static org.netbeans.editor.BaseKit$BeginLineAction create()
meth public static org.netbeans.editor.BaseKit$BeginLineAction createColumnOne()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds homeKeyColumnOne,serialVersionUID

CLSS public static org.netbeans.editor.BaseKit$BeginWordAction
 outer org.netbeans.editor.BaseKit
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.BaseKit$CompoundAction
 outer org.netbeans.editor.BaseKit
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,int,javax.swing.Action[])
cons public init(java.lang.String,javax.swing.Action[])
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds actions,serialVersionUID

CLSS public static org.netbeans.editor.BaseKit$CopyAction
 outer org.netbeans.editor.BaseKit
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.BaseKit$CutAction
 outer org.netbeans.editor.BaseKit
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.BaseKit$DefaultKeyTypedAction
 outer org.netbeans.editor.BaseKit
 anno 0 java.lang.Deprecated()
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth protected void checkIndent(javax.swing.text.JTextComponent,java.lang.String)
meth protected void insertString(org.netbeans.editor.BaseDocument,int,javax.swing.text.Caret,java.lang.String,boolean) throws javax.swing.text.BadLocationException
meth protected void replaceSelection(javax.swing.text.JTextComponent,int,javax.swing.text.Caret,java.lang.String,boolean) throws javax.swing.text.BadLocationException
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.BaseKit$DeleteCharAction
 outer org.netbeans.editor.BaseKit
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,boolean)
fld protected boolean nextChar
meth protected java.lang.Class getShortDescriptionBundleClass()
meth protected void charBackspaced(org.netbeans.editor.BaseDocument,int,javax.swing.text.Caret,char) throws javax.swing.text.BadLocationException
meth protected void charDeleted(org.netbeans.editor.BaseDocument,int,javax.swing.text.Caret,char) throws javax.swing.text.BadLocationException
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds disableDeleteFromScreenMenu,serialVersionUID

CLSS public static org.netbeans.editor.BaseKit$DownAction
 outer org.netbeans.editor.BaseKit
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.BaseKit$EndAction
 outer org.netbeans.editor.BaseKit
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.BaseKit$EndLineAction
 outer org.netbeans.editor.BaseKit
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.BaseKit$EndWordAction
 outer org.netbeans.editor.BaseKit
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.BaseKit$ForwardAction
 outer org.netbeans.editor.BaseKit
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.BaseKit$InsertBreakAction
 outer org.netbeans.editor.BaseKit
 anno 0 java.lang.Deprecated()
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth protected java.lang.Object beforeBreak(javax.swing.text.JTextComponent,org.netbeans.editor.BaseDocument,javax.swing.text.Caret)
meth protected void afterBreak(javax.swing.text.JTextComponent,org.netbeans.editor.BaseDocument,javax.swing.text.Caret,java.lang.Object)
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.BaseKit$InsertContentAction
 outer org.netbeans.editor.BaseKit
 anno 0 java.lang.Deprecated()
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.BaseKit$InsertStringAction
 outer org.netbeans.editor.BaseKit
cons public init(java.lang.String,java.lang.String)
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID,text

CLSS public static org.netbeans.editor.BaseKit$InsertTabAction
 outer org.netbeans.editor.BaseKit
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.BaseKit$KitCompoundAction
 outer org.netbeans.editor.BaseKit
 anno 0 java.lang.Deprecated()
cons public init(int,java.lang.String[])
cons public init(java.lang.String,int,java.lang.String[])
cons public init(java.lang.String,java.lang.String[])
cons public init(java.lang.String[])
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds actionNames,serialVersionUID

CLSS public static org.netbeans.editor.BaseKit$NextWordAction
 outer org.netbeans.editor.BaseKit
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String)
meth protected int getNextWordOffset(javax.swing.text.JTextComponent) throws javax.swing.text.BadLocationException
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.BaseKit$PageDownAction
 outer org.netbeans.editor.BaseKit
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.BaseKit$PageUpAction
 outer org.netbeans.editor.BaseKit
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.BaseKit$PasteAction
 outer org.netbeans.editor.BaseKit
cons public init(boolean)
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.BaseKit$PreviousWordAction
 outer org.netbeans.editor.BaseKit
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String)
meth protected int getPreviousWordOffset(javax.swing.text.JTextComponent) throws javax.swing.text.BadLocationException
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.BaseKit$ReadOnlyAction
 outer org.netbeans.editor.BaseKit
 anno 0 java.lang.Deprecated()
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.BaseKit$RemoveTrailingSpacesAction
 outer org.netbeans.editor.BaseKit
cons public init()
meth protected boolean asynchonous()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction

CLSS public static org.netbeans.editor.BaseKit$SelectAllAction
 outer org.netbeans.editor.BaseKit
cons public init()
supr org.netbeans.editor.BaseKit$KitCompoundAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.BaseKit$SelectLineAction
 outer org.netbeans.editor.BaseKit
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.BaseKit$SelectWordAction
 outer org.netbeans.editor.BaseKit
cons public init()
supr org.netbeans.editor.BaseKit$KitCompoundAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.BaseKit$SplitLineAction
 outer org.netbeans.editor.BaseKit
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.BaseKit$UpAction
 outer org.netbeans.editor.BaseKit
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.BaseKit$WritableAction
 outer org.netbeans.editor.BaseKit
 anno 0 java.lang.Deprecated()
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public org.netbeans.editor.BaseTextUI
cons public init()
intf java.beans.PropertyChangeListener
intf javax.swing.event.DocumentListener
intf org.netbeans.editor.AtomicLockListener
meth protected boolean isRootViewReplaceNecessary()
meth protected java.lang.String getPropertyPrefix()
meth protected void installKeyboardActions()
meth protected void modelChanged()
meth protected void refresh()
meth protected void rootViewReplaceNotify()
meth public int getBaseX(int)
meth public int getNextVisualPositionFrom(javax.swing.text.JTextComponent,int,javax.swing.text.Position$Bias,int,javax.swing.text.Position$Bias[]) throws javax.swing.text.BadLocationException
meth public int getPosFromY(int) throws javax.swing.text.BadLocationException
meth public int getYFromPos(int) throws javax.swing.text.BadLocationException
meth public int viewToModel(javax.swing.text.JTextComponent,int,int)
meth public javax.swing.text.EditorKit getEditorKit(javax.swing.text.JTextComponent)
meth public javax.swing.text.View create(javax.swing.text.Element)
meth public javax.swing.text.View create(javax.swing.text.Element,int,int)
meth public org.netbeans.editor.EditorUI getEditorUI()
meth public static javax.swing.text.JTextComponent getFocusedComponent()
meth public void atomicLock(org.netbeans.editor.AtomicLockEvent)
meth public void atomicUnlock(org.netbeans.editor.AtomicLockEvent)
meth public void changedUpdate(javax.swing.event.DocumentEvent)
meth public void damageRange(javax.swing.text.JTextComponent,int,int,javax.swing.text.Position$Bias,javax.swing.text.Position$Bias)
meth public void insertUpdate(javax.swing.event.DocumentEvent)
meth public void installUI(javax.swing.JComponent)
meth public void invalidateStartY()
meth public void preferenceChanged(boolean,boolean)
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void removeUpdate(javax.swing.event.DocumentEvent)
meth public void uninstallUI(javax.swing.JComponent)
supr javax.swing.plaf.basic.BasicTextUI
hfds LENGTHY_ATOMIC_EDIT_THRESHOLD,LOG,PROP_DEFAULT_CARET_BLINK_RATE,atomicModCount,componentID,editorUI,gfcAction,lastDocument,needsRefresh,prefs
hcls GetFocusedComponentAction,UIWatcher

CLSS public org.netbeans.editor.BaseTokenCategory
cons public init(java.lang.String)
cons public init(java.lang.String,int)
intf org.netbeans.editor.TokenCategory
meth public int getNumericID()
meth public java.lang.String getName()
meth public java.lang.String toString()
supr java.lang.Object
hfds name,numericID

CLSS public org.netbeans.editor.BaseTokenID
cons public init(java.lang.String)
cons public init(java.lang.String,int)
cons public init(java.lang.String,int,org.netbeans.editor.TokenCategory)
cons public init(java.lang.String,org.netbeans.editor.TokenCategory)
intf org.netbeans.editor.TokenID
meth public int getNumericID()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public org.netbeans.editor.TokenCategory getCategory()
supr java.lang.Object
hfds category,name,numericID

CLSS public abstract org.netbeans.editor.BaseView
fld protected boolean packed
fld protected final static int INSETS_BOTTOM = 4
fld protected final static int INSETS_TOP = 1
fld protected final static int MAIN_AREA = 2
fld protected int helperInd
fld protected java.awt.Insets insets
meth protected abstract int getBaseX(int)
meth protected abstract int getPaintAreas(java.awt.Graphics,int,int)
meth protected abstract int getPosFromY(int)
meth protected abstract int getViewStartY(org.netbeans.editor.BaseView,int)
meth protected abstract int getYFromPos(int) throws javax.swing.text.BadLocationException
meth protected abstract void paintAreas(java.awt.Graphics,int,int,int)
meth protected int getStartY()
meth protected org.netbeans.editor.EditorUI getEditorUI()
meth protected void invalidateStartY()
meth protected void setHelperInd(int)
meth public abstract int getHeight()
meth public abstract void updateMainHeight()
meth public boolean isPacked()
meth public float getAlignment(int)
meth public float getPreferredSpan(int)
meth public java.awt.Insets getInsets()
meth public java.lang.String toString()
meth public javax.swing.text.JTextComponent getComponent()
meth public void displayHierarchy()
meth public void paint(java.awt.Graphics,java.awt.Shape)
meth public void setPacked(boolean)
supr javax.swing.text.View
hfds component,startY

CLSS public abstract interface org.netbeans.editor.CharSeq
 anno 0 java.lang.Deprecated()
meth public abstract char charAt(int)
meth public abstract int length()

CLSS public org.netbeans.editor.CodeFoldingSideBar
 anno 0 java.lang.Deprecated()
cons public init()
cons public init(javax.swing.text.JTextComponent)
fld protected java.awt.Color backColor
fld protected java.awt.Color foreColor
fld protected java.awt.Font font
fld protected java.util.List<org.netbeans.editor.CodeFoldingSideBar$Mark> visibleMarks
fld protected javax.swing.text.JTextComponent component
fld public final static int PAINT_END_MARK = 3
fld public final static int PAINT_LINE = 2
fld public final static int PAINT_MARK = 1
fld public final static int PAINT_NOOP = 0
fld public final static int SINGLE_PAINT_MARK = 4
innr public Mark
innr public PaintInfo
intf javax.accessibility.Accessible
meth protected int getMarkSize(java.awt.Graphics)
meth protected java.awt.Color getBackColor()
meth protected java.awt.Color getForeColor()
meth protected java.awt.Font getColoringFont()
meth protected java.util.List<? extends org.netbeans.editor.CodeFoldingSideBar$PaintInfo> getPaintInfo(java.awt.Rectangle) throws javax.swing.text.BadLocationException
meth protected javax.swing.text.Document getDocument()
meth protected org.netbeans.editor.EditorUI getEditorUI()
meth protected void collectPaintInfos(javax.swing.text.View,org.netbeans.api.editor.fold.Fold,java.util.Map<java.lang.Integer,org.netbeans.editor.CodeFoldingSideBar$PaintInfo>,int,int,int) throws javax.swing.text.BadLocationException
meth protected void paintComponent(java.awt.Graphics)
meth protected void performAction(org.netbeans.editor.CodeFoldingSideBar$Mark)
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public void update(java.awt.Graphics)
supr javax.swing.JComponent
hfds LINE_BOLD,LINE_DASHED,LOG,NO_MOUSE_POINT,alreadyPresent,attribs,enabled,fcsLookupResult,fcsTracker,listener,lowestAboveMouse,mouseBoundary,mousePoint,mousePointConsumed,prefs,prefsListener,topmostBelowMouse
hcls Listener

CLSS public org.netbeans.editor.CodeFoldingSideBar$Mark
 outer org.netbeans.editor.CodeFoldingSideBar
cons public init(org.netbeans.editor.CodeFoldingSideBar,int,int,int,boolean)
fld public boolean isFolded
fld public int size
fld public int x
fld public int y
supr java.lang.Object

CLSS public org.netbeans.editor.CodeFoldingSideBar$PaintInfo
 outer org.netbeans.editor.CodeFoldingSideBar
cons public init(org.netbeans.editor.CodeFoldingSideBar,int,int,int,int,boolean,int,int)
cons public init(org.netbeans.editor.CodeFoldingSideBar,int,int,int,int,int,int)
meth public boolean isCollapsed()
meth public int getInnerLevel()
meth public int getPaintHeight()
meth public int getPaintOperation()
meth public int getPaintY()
meth public java.lang.String toString()
meth public void setInnerLevel(int)
meth public void setPaintOperation(int)
supr java.lang.Object
hfds allCollapsed,endOffset,innerLevel,isCollapsed,lineIn,lineInActive,lineOut,lineOutActive,outgoingLevel,paintHeight,paintOperation,paintY,signActive,startOffset

CLSS public final org.netbeans.editor.Coloring
cons public init()
cons public init(java.awt.Font,int,java.awt.Color,java.awt.Color)
cons public init(java.awt.Font,int,java.awt.Color,java.awt.Color,java.awt.Color,java.awt.Color)
cons public init(java.awt.Font,int,java.awt.Color,java.awt.Color,java.awt.Color,java.awt.Color,java.awt.Color)
cons public init(java.awt.Font,int,java.awt.Color,java.awt.Color,java.awt.Color,java.awt.Color,java.awt.Color,java.awt.Color,java.awt.Color,java.awt.Color,java.awt.Color)
cons public init(java.awt.Font,java.awt.Color,java.awt.Color)
fld public final static int FONT_MODE_APPLY_NAME = 1
fld public final static int FONT_MODE_APPLY_SIZE = 4
fld public final static int FONT_MODE_APPLY_STYLE = 2
fld public final static int FONT_MODE_DEFAULT = 7
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public int getFontMode()
meth public int hashCode()
meth public java.awt.Color getBackColor()
meth public java.awt.Color getBottomBorderLineColor()
meth public java.awt.Color getForeColor()
meth public java.awt.Color getLeftBorderLineColor()
meth public java.awt.Color getRightBorderLineColor()
meth public java.awt.Color getStrikeThroughColor()
meth public java.awt.Color getTopBorderLineColor()
meth public java.awt.Color getUnderlineColor()
meth public java.awt.Color getWaveUnderlineColor()
meth public java.awt.Font getFont()
meth public java.lang.String toString()
meth public org.netbeans.editor.Coloring apply(org.netbeans.editor.Coloring)
meth public static org.netbeans.editor.Coloring changeBackColor(org.netbeans.editor.Coloring,java.awt.Color)
meth public static org.netbeans.editor.Coloring changeFont(org.netbeans.editor.Coloring,java.awt.Font)
meth public static org.netbeans.editor.Coloring changeFont(org.netbeans.editor.Coloring,java.awt.Font,int)
meth public static org.netbeans.editor.Coloring changeForeColor(org.netbeans.editor.Coloring,java.awt.Color)
meth public static org.netbeans.editor.Coloring fromAttributeSet(javax.swing.text.AttributeSet)
meth public void apply(javax.swing.JComponent)
supr java.lang.Object
hfds backColor,backColorCache,bottomBorderLineColor,cacheLock,colorings,font,fontAndForeColorCache,fontMode,foreColor,leftBorderLineColor,rightBorderLineColor,serialVersionUID,strikeThroughColor,topBorderLineColor,underlineColor,waveUnderlineColor
hcls Accessor

CLSS public org.netbeans.editor.DelegateAction
cons public init()
cons public init(javax.swing.Action)
fld protected javax.swing.Action delegate
meth protected final javax.swing.Action getDelegate()
meth protected void setDelegate(javax.swing.Action)
meth public java.lang.Object getValue(java.lang.String)
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void putValue(java.lang.String,java.lang.Object)
supr javax.swing.AbstractAction
hfds pcl

CLSS public org.netbeans.editor.DialogSupport
 anno 0 java.lang.Deprecated()
innr public abstract interface static DialogFactory
meth public static java.awt.Dialog createDialog(java.lang.String,javax.swing.JPanel,boolean,javax.swing.JButton[],boolean,int,int,java.awt.event.ActionListener)
meth public static void setDialogFactory(org.netbeans.editor.DialogSupport$DialogFactory)
supr java.lang.Object
hcls Wrapper

CLSS public abstract interface static org.netbeans.editor.DialogSupport$DialogFactory
 outer org.netbeans.editor.DialogSupport
meth public abstract java.awt.Dialog createDialog(java.lang.String,javax.swing.JPanel,boolean,javax.swing.JButton[],boolean,int,int,java.awt.event.ActionListener)

CLSS public org.netbeans.editor.DocumentUtilities
meth public static int getGapStart(javax.swing.text.Document)
meth public static void copyText(javax.swing.text.Document,int,int,char[],int) throws javax.swing.text.BadLocationException
supr java.lang.Object

CLSS public org.netbeans.editor.EditorDebug
 anno 0 java.lang.Deprecated()
meth public static java.lang.String debugArray(int[])
meth public static java.lang.String debugArray(java.lang.Object[])
meth public static java.lang.String debugBlocks(org.netbeans.editor.BaseDocument,int[])
meth public static java.lang.String debugChar(char)
meth public static java.lang.String debugChars(char[])
meth public static java.lang.String debugChars(char[],int,int)
meth public static java.lang.String debugChars(javax.swing.text.Segment)
meth public static java.lang.String debugIterator(java.util.Iterator)
meth public static java.lang.String debugList(java.util.List)
meth public static java.lang.String debugPairs(int[])
meth public static java.lang.String debugString(java.lang.String)
meth public static void checkSettings(java.lang.Class) throws java.lang.Exception
meth public static void dumpPlanes(org.netbeans.editor.BaseDocument)
meth public static void dumpSyntaxMarks(org.netbeans.editor.BaseDocument)
meth public static void test(javax.swing.text.JTextComponent)
supr java.lang.Object

CLSS public org.netbeans.editor.EditorState
 anno 0 java.lang.Deprecated()
meth public static java.lang.Object get(java.lang.Object)
meth public static java.util.HashMap getStateObject()
meth public static void put(java.lang.Object,java.lang.Object)
meth public static void setStateObject(java.util.HashMap)
supr java.lang.Object
hfds state

CLSS public org.netbeans.editor.EditorUI
cons public init()
cons public init(org.netbeans.editor.BaseDocument)
cons public init(org.netbeans.editor.BaseDocument,boolean,boolean)
fld public final static int SCROLL_DEFAULT = 0
fld public final static int SCROLL_FIND = 3
fld public final static int SCROLL_MOVE = 1
fld public final static int SCROLL_SMALLEST = 2
fld public final static java.awt.Insets defaultLineNumberMargin
fld public final static java.lang.String COMPONENT_PROPERTY = "component"
fld public final static java.lang.String LINE_HEIGHT_CHANGED_PROP = "line-height-changed-prop"
fld public final static java.lang.String OVERWRITE_MODE_PROPERTY = "overwriteMode"
fld public final static java.lang.String TAB_SIZE_CHANGED_PROP = "tab-size-changed-prop"
intf java.awt.event.MouseListener
intf java.beans.PropertyChangeListener
intf javax.swing.event.ChangeListener
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected int textLimitWidth()
meth protected java.util.Map createColoringMap()
 anno 0 java.lang.Deprecated()
meth protected javax.swing.JComponent createExtComponent()
meth protected javax.swing.JToolBar createToolBarComponent()
meth protected static java.util.Map<java.lang.String,org.netbeans.editor.Coloring> getSharedColoringMap(java.lang.Class)
 anno 0 java.lang.Deprecated()
meth protected void initGlyphCorner(javax.swing.JScrollPane)
meth protected void installUI(javax.swing.text.JTextComponent)
meth protected void modelChanged(org.netbeans.editor.BaseDocument,org.netbeans.editor.BaseDocument)
meth protected void paint(java.awt.Graphics)
meth protected void setGlyphGutter(org.netbeans.editor.GlyphGutter)
meth protected void settingsChangeImpl(java.lang.String)
meth protected void uninstallUI(javax.swing.text.JTextComponent)
meth protected void update(java.awt.Graphics)
meth protected void updateScrollPaneCornerColor()
meth public boolean hasExtComponent()
meth public boolean isGlyphGutterVisible()
meth public boolean isLineNumberEnabled()
meth public boolean updateVirtualHeight(int)
 anno 0 java.lang.Deprecated()
meth public boolean updateVirtualWidth(int)
 anno 0 java.lang.Deprecated()
meth public final int getSideBarWidth()
meth public final javax.swing.text.JTextComponent getComponent()
meth public final org.netbeans.editor.BaseDocument getDocument()
meth public final org.netbeans.editor.GlyphGutter getGlyphGutter()
meth public int getLineAscent()
meth public int getLineHeight()
meth public int getLineNumberDigitWidth()
meth public java.awt.Insets getLineNumberMargin()
meth public java.awt.Insets getTextMargin()
meth public java.awt.Rectangle getExtentBounds()
meth public java.awt.Rectangle getExtentBounds(java.awt.Rectangle)
meth public java.lang.Object getComponentLock()
meth public java.lang.Object getProperty(java.lang.Object)
meth public java.util.Map<java.lang.String,org.netbeans.editor.Coloring> getColoringMap()
 anno 0 java.lang.Deprecated()
meth public javax.swing.JComponent getExtComponent()
meth public javax.swing.JPopupMenu getPopupMenu()
meth public javax.swing.JToolBar getToolBarComponent()
meth public org.netbeans.api.editor.StickyWindowSupport getStickyWindowSupport()
meth public org.netbeans.editor.Abbrev getAbbrev()
 anno 0 java.lang.Deprecated()
meth public org.netbeans.editor.Coloring getColoring(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public org.netbeans.editor.Coloring getDefaultColoring()
 anno 0 java.lang.Deprecated()
meth public org.netbeans.editor.PopupManager getPopupManager()
meth public org.netbeans.editor.StatusBar getStatusBar()
meth public org.netbeans.editor.WordMatch getWordMatch()
meth public org.netbeans.editor.ext.ToolTipSupport getToolTipSupport()
meth public static java.awt.Frame getParentFrame(java.awt.Component)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void adjustCaret(int)
meth public void adjustWindow(int)
meth public void caretMoveDot(int,java.awt.Rectangle,int)
 anno 0 java.lang.Deprecated()
meth public void caretSetDot(int,java.awt.Rectangle,int)
 anno 0 java.lang.Deprecated()
meth public void hidePopupMenu()
meth public void mouseClicked(java.awt.event.MouseEvent)
meth public void mouseEntered(java.awt.event.MouseEvent)
meth public void mouseExited(java.awt.event.MouseEvent)
meth public void mousePressed(java.awt.event.MouseEvent)
meth public void mouseReleased(java.awt.event.MouseEvent)
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void putProperty(java.lang.Object,java.lang.Object)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void repaint(int)
meth public void repaint(int,int)
meth public void repaintBlock(int,int) throws javax.swing.text.BadLocationException
meth public void repaintOffset(int) throws javax.swing.text.BadLocationException
meth public void scrollRectToVisible(java.awt.Rectangle,int)
 anno 0 java.lang.Deprecated()
meth public void setLineNumberEnabled(boolean)
meth public void setPopupMenu(javax.swing.JPopupMenu)
meth public void showPopupMenu(int,int)
meth public void stateChanged(javax.swing.event.ChangeEvent)
meth public void updateLineNumberWidth(int)
meth public void updateTextMargin()
supr java.lang.Object
hfds DEFAULT_INSETS,LOG,NULL_INSETS,abbrev,coloringMap,component,componentLock,defaultSpaceWidth,disableLineNumbers,drawLayerList,extComponent,focusL,glyphCorner,glyphGutter,highlightSearch,isPasteActionInited,lineAscent,lineHeight,lineHeightCorrection,lineNumberDigitWidth,lineNumberEnabled,lineNumberMaxDigitCount,lineNumberVisible,lineNumberVisibleSetting,lineNumberWidth,listener,popupManager,popupMenu,popupMenuEnabled,prefs,printDoc,propertyChangeSupport,props,renderingHints,scrollFindInsets,scrollJumpInsets,statusBar,stickyWindowSupport,textLeftMarginWidth,textLimitLineVisible,textLimitWidth,textMargin,toolBarComponent,toolTipSupport,weakPrefsListener,wordMatch
hcls Accessor,ComponentLock,Listener

CLSS public abstract interface org.netbeans.editor.Finder
meth public abstract boolean isFound()
meth public abstract int find(int,char[],int,int,int,int)
meth public abstract void reset()

CLSS public org.netbeans.editor.FinderFactory
cons public init()
innr public abstract interface static BlocksFinder
innr public abstract interface static StringFinder
innr public abstract static AbstractBlocksFinder
innr public abstract static AbstractFinder
innr public abstract static GenericBwdFinder
innr public abstract static GenericFinder
innr public abstract static GenericFwdFinder
innr public final static FalseBlocksFinder
innr public final static PosVisColFwdFinder
innr public final static StringBlocksFinder
innr public final static StringFwdFinder
innr public final static VisColPosFwdFinder
innr public final static WholeWordsBlocksFinder
innr public final static WholeWordsBwdFinder
innr public final static WholeWordsFwdFinder
innr public static AcceptorBwdFinder
innr public static AcceptorFwdFinder
innr public static CharArrayBwdFinder
innr public static CharArrayFwdFinder
innr public static CharBwdFinder
innr public static CharFwdFinder
innr public static FalseFinder
innr public static NextWordFwdFinder
innr public static NonWhiteBwdFinder
innr public static NonWhiteFwdFinder
innr public static PreviousWordBwdFinder
innr public static StringBwdFinder
innr public static TrueFinder
innr public static WhiteBwdFinder
innr public static WhiteFwdFinder
supr java.lang.Object

CLSS public abstract static org.netbeans.editor.FinderFactory$AbstractBlocksFinder
 outer org.netbeans.editor.FinderFactory
cons public init()
intf org.netbeans.editor.FinderFactory$BlocksFinder
meth protected void addBlock(int,int)
meth protected void closeBlocks()
meth public int[] getBlocks()
meth public java.lang.String debugBlocks()
meth public void reset()
meth public void setBlocks(int[])
supr org.netbeans.editor.FinderFactory$AbstractFinder
hfds EMPTY_INT_ARRAY,blocks,blocksInd,closed

CLSS public abstract static org.netbeans.editor.FinderFactory$AbstractFinder
 outer org.netbeans.editor.FinderFactory
cons public init()
fld protected boolean found
intf org.netbeans.editor.Finder
meth public final boolean isFound()
meth public void reset()
supr java.lang.Object

CLSS public static org.netbeans.editor.FinderFactory$AcceptorBwdFinder
 outer org.netbeans.editor.FinderFactory
cons public init(org.netbeans.editor.Acceptor)
meth protected int scan(char,boolean)
supr org.netbeans.editor.FinderFactory$GenericBwdFinder
hfds a

CLSS public static org.netbeans.editor.FinderFactory$AcceptorFwdFinder
 outer org.netbeans.editor.FinderFactory
cons public init(org.netbeans.editor.Acceptor)
meth protected int scan(char,boolean)
supr org.netbeans.editor.FinderFactory$GenericFwdFinder
hfds a

CLSS public abstract interface static org.netbeans.editor.FinderFactory$BlocksFinder
 outer org.netbeans.editor.FinderFactory
intf org.netbeans.editor.Finder
meth public abstract int[] getBlocks()
meth public abstract void setBlocks(int[])

CLSS public static org.netbeans.editor.FinderFactory$CharArrayBwdFinder
 outer org.netbeans.editor.FinderFactory
cons public init(char[])
meth protected int scan(char,boolean)
meth public char getFoundChar()
supr org.netbeans.editor.FinderFactory$GenericBwdFinder
hfds foundChar,searchChars

CLSS public static org.netbeans.editor.FinderFactory$CharArrayFwdFinder
 outer org.netbeans.editor.FinderFactory
cons public init(char[])
meth protected int scan(char,boolean)
meth public char getFoundChar()
supr org.netbeans.editor.FinderFactory$GenericFwdFinder
hfds foundChar,searchChars

CLSS public static org.netbeans.editor.FinderFactory$CharBwdFinder
 outer org.netbeans.editor.FinderFactory
cons public init(char)
meth protected int scan(char,boolean)
supr org.netbeans.editor.FinderFactory$GenericBwdFinder
hfds searchChar

CLSS public static org.netbeans.editor.FinderFactory$CharFwdFinder
 outer org.netbeans.editor.FinderFactory
cons public init(char)
meth protected int scan(char,boolean)
supr org.netbeans.editor.FinderFactory$GenericFwdFinder
hfds searchChar

CLSS public final static org.netbeans.editor.FinderFactory$FalseBlocksFinder
 outer org.netbeans.editor.FinderFactory
cons public init()
meth public int find(int,char[],int,int,int,int)
supr org.netbeans.editor.FinderFactory$AbstractBlocksFinder

CLSS public static org.netbeans.editor.FinderFactory$FalseFinder
 outer org.netbeans.editor.FinderFactory
cons public init()
intf org.netbeans.editor.FinderFactory$StringFinder
meth public int find(int,char[],int,int,int,int)
meth public int getFoundLength()
supr org.netbeans.editor.FinderFactory$AbstractFinder

CLSS public abstract static org.netbeans.editor.FinderFactory$GenericBwdFinder
 outer org.netbeans.editor.FinderFactory
cons public init()
meth protected abstract int scan(char,boolean)
meth public final int find(int,char[],int,int,int,int)
supr org.netbeans.editor.FinderFactory$AbstractFinder

CLSS public abstract static org.netbeans.editor.FinderFactory$GenericFinder
 outer org.netbeans.editor.FinderFactory
cons public init()
fld protected boolean forward
meth protected abstract int scan(char,boolean)
meth public boolean isForward()
meth public final int find(int,char[],int,int,int,int)
supr org.netbeans.editor.FinderFactory$AbstractFinder

CLSS public abstract static org.netbeans.editor.FinderFactory$GenericFwdFinder
 outer org.netbeans.editor.FinderFactory
cons public init()
meth protected abstract int scan(char,boolean)
meth public final int find(int,char[],int,int,int,int)
supr org.netbeans.editor.FinderFactory$AbstractFinder

CLSS public static org.netbeans.editor.FinderFactory$NextWordFwdFinder
 outer org.netbeans.editor.FinderFactory
cons public init(org.netbeans.editor.BaseDocument,boolean,boolean)
meth protected int scan(char,boolean)
meth public void reset()
supr org.netbeans.editor.FinderFactory$GenericFwdFinder
hfds doc,firstChar,inIdentifier,inPunct,inWhitespace,stopOnEOL,stopOnWhitespace

CLSS public static org.netbeans.editor.FinderFactory$NonWhiteBwdFinder
 outer org.netbeans.editor.FinderFactory
cons public init(org.netbeans.editor.BaseDocument)
meth protected int scan(char,boolean)
meth public char getFoundChar()
supr org.netbeans.editor.FinderFactory$GenericBwdFinder
hfds doc,foundChar

CLSS public static org.netbeans.editor.FinderFactory$NonWhiteFwdFinder
 outer org.netbeans.editor.FinderFactory
cons public init(org.netbeans.editor.BaseDocument)
meth protected int scan(char,boolean)
meth public char getFoundChar()
supr org.netbeans.editor.FinderFactory$GenericFwdFinder
hfds doc,foundChar

CLSS public final static org.netbeans.editor.FinderFactory$PosVisColFwdFinder
 outer org.netbeans.editor.FinderFactory
cons public init()
meth public int find(int,char[],int,int,int,int)
meth public int getVisCol()
meth public void reset()
meth public void setTabSize(int)
supr org.netbeans.editor.FinderFactory$AbstractFinder
hfds tabSize,visCol

CLSS public static org.netbeans.editor.FinderFactory$PreviousWordBwdFinder
 outer org.netbeans.editor.FinderFactory
cons public init(org.netbeans.editor.BaseDocument,boolean,boolean)
meth protected int scan(char,boolean)
meth public void reset()
supr org.netbeans.editor.FinderFactory$GenericBwdFinder
hfds doc,firstChar,inIdentifier,inPunct,stopOnEOL,stopOnWhitespace

CLSS public final static org.netbeans.editor.FinderFactory$StringBlocksFinder
 outer org.netbeans.editor.FinderFactory
cons public init(java.lang.String,boolean)
meth public int find(int,char[],int,int,int,int)
meth public void reset()
supr org.netbeans.editor.FinderFactory$AbstractBlocksFinder
hfds chars,matchCase,stringInd

CLSS public static org.netbeans.editor.FinderFactory$StringBwdFinder
 outer org.netbeans.editor.FinderFactory
cons public init(java.lang.String,boolean)
intf org.netbeans.editor.FinderFactory$StringFinder
meth protected int scan(char,boolean)
meth public int getFoundLength()
meth public void reset()
supr org.netbeans.editor.FinderFactory$GenericBwdFinder
hfds chars,endInd,matchCase,stringInd

CLSS public abstract interface static org.netbeans.editor.FinderFactory$StringFinder
 outer org.netbeans.editor.FinderFactory
intf org.netbeans.editor.Finder
meth public abstract int getFoundLength()

CLSS public final static org.netbeans.editor.FinderFactory$StringFwdFinder
 outer org.netbeans.editor.FinderFactory
cons public init(java.lang.String,boolean)
intf org.netbeans.editor.FinderFactory$StringFinder
meth protected int scan(char,boolean)
meth public int getFoundLength()
meth public void reset()
supr org.netbeans.editor.FinderFactory$GenericFwdFinder
hfds chars,matchCase,stringInd

CLSS public static org.netbeans.editor.FinderFactory$TrueFinder
 outer org.netbeans.editor.FinderFactory
cons public init()
meth public int find(int,char[],int,int,int,int)
supr org.netbeans.editor.FinderFactory$AbstractFinder

CLSS public final static org.netbeans.editor.FinderFactory$VisColPosFwdFinder
 outer org.netbeans.editor.FinderFactory
cons public init()
meth public int find(int,char[],int,int,int,int)
meth public void reset()
meth public void setTabSize(int)
meth public void setVisCol(int)
supr org.netbeans.editor.FinderFactory$AbstractFinder
hfds curVisCol,editorUI,tabSize,visCol

CLSS public static org.netbeans.editor.FinderFactory$WhiteBwdFinder
 outer org.netbeans.editor.FinderFactory
cons public init(org.netbeans.editor.BaseDocument)
meth protected int scan(char,boolean)
meth public char getFoundChar()
supr org.netbeans.editor.FinderFactory$GenericBwdFinder
hfds doc,foundChar

CLSS public static org.netbeans.editor.FinderFactory$WhiteFwdFinder
 outer org.netbeans.editor.FinderFactory
cons public init(org.netbeans.editor.BaseDocument)
meth protected int scan(char,boolean)
meth public char getFoundChar()
supr org.netbeans.editor.FinderFactory$GenericFwdFinder
hfds doc,foundChar

CLSS public final static org.netbeans.editor.FinderFactory$WholeWordsBlocksFinder
 outer org.netbeans.editor.FinderFactory
cons public init(org.netbeans.editor.BaseDocument,java.lang.String,boolean)
meth public int find(int,char[],int,int,int,int)
meth public void reset()
supr org.netbeans.editor.FinderFactory$AbstractBlocksFinder
hfds chars,doc,firstCharWordPart,insideWord,matchCase,stringInd,wordFound

CLSS public final static org.netbeans.editor.FinderFactory$WholeWordsBwdFinder
 outer org.netbeans.editor.FinderFactory
cons public init(org.netbeans.editor.BaseDocument,java.lang.String,boolean)
intf org.netbeans.editor.FinderFactory$StringFinder
meth protected int scan(char,boolean)
meth public int getFoundLength()
meth public void reset()
supr org.netbeans.editor.FinderFactory$GenericBwdFinder
hfds chars,doc,endInd,insideWord,lastCharWordPart,matchCase,stringInd,wordFound

CLSS public final static org.netbeans.editor.FinderFactory$WholeWordsFwdFinder
 outer org.netbeans.editor.FinderFactory
cons public init(org.netbeans.editor.BaseDocument,java.lang.String,boolean)
intf org.netbeans.editor.FinderFactory$StringFinder
meth protected int scan(char,boolean)
meth public int getFoundLength()
meth public void reset()
supr org.netbeans.editor.FinderFactory$GenericFwdFinder
hfds chars,doc,firstCharWordPart,insideWord,matchCase,stringInd,wordFound

CLSS public org.netbeans.editor.FoldingToolTip
 anno 0 java.lang.Deprecated()
cons public init(javax.swing.text.View,org.netbeans.editor.EditorUI)
fld public final static int BORDER_WIDTH = 2
meth protected void paintComponent(java.awt.Graphics)
meth public java.awt.Dimension getPreferredSize()
meth public void setSize(int,int)
meth public void setSize(java.awt.Dimension)
supr javax.swing.JPanel
hfds editorUI,view

CLSS public org.netbeans.editor.FontMetricsCache
cons public init()
innr public abstract interface static Info
meth public static java.awt.FontMetrics getFontMetrics(java.awt.Font,java.awt.Component)
meth public static java.awt.FontMetrics getFontMetrics(java.awt.Font,java.awt.Graphics)
meth public static org.netbeans.editor.FontMetricsCache$Info getInfo(java.awt.Font)
meth public static void clear()
supr java.lang.Object
hfds font2FM,font2Info
hcls InfoImpl

CLSS public abstract interface static org.netbeans.editor.FontMetricsCache$Info
 outer org.netbeans.editor.FontMetricsCache
meth public abstract float getStrikethroughOffset(java.awt.Component)
meth public abstract float getStrikethroughOffset(java.awt.Graphics)
meth public abstract float getStrikethroughThickness(java.awt.Component)
meth public abstract float getStrikethroughThickness(java.awt.Graphics)
meth public abstract float getUnderlineOffset(java.awt.Component)
meth public abstract float getUnderlineOffset(java.awt.Graphics)
meth public abstract float getUnderlineThickness(java.awt.Component)
meth public abstract float getUnderlineThickness(java.awt.Graphics)
meth public abstract int getSpaceWidth(java.awt.Component)
meth public abstract int getSpaceWidth(java.awt.Graphics)

CLSS public org.netbeans.editor.Formatter
 anno 0 java.lang.Deprecated()
cons public init(java.lang.Class)
meth public boolean expandTabs()
meth public int getShiftWidth()
meth public int getSpacesPerTab()
meth public int getTabSize()
meth public int indentLine(javax.swing.text.Document,int)
meth public int indentNewLine(javax.swing.text.Document,int)
meth public int reformat(org.netbeans.editor.BaseDocument,int,int) throws javax.swing.text.BadLocationException
meth public java.io.Writer createWriter(javax.swing.text.Document,int,java.io.Writer)
meth public java.lang.Class getKitClass()
meth public java.lang.String getIndentString(int)
meth public java.lang.String getIndentString(org.netbeans.editor.BaseDocument,int)
meth public static org.netbeans.editor.Formatter getFormatter(java.lang.Class)
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.editor.Formatter getFormatter(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static void setFormatter(java.lang.Class,org.netbeans.editor.Formatter)
 anno 0 java.lang.Deprecated()
meth public void changeBlockIndent(org.netbeans.editor.BaseDocument,int,int,int) throws javax.swing.text.BadLocationException
meth public void changeRowIndent(org.netbeans.editor.BaseDocument,int,int) throws javax.swing.text.BadLocationException
meth public void indentLock()
meth public void indentUnlock()
meth public void insertTabString(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
meth public void reformatLock()
meth public void reformatUnlock()
meth public void setExpandTabs(boolean)
meth public void setShiftWidth(int)
meth public void setSpacesPerTab(int)
meth public void setTabSize(int)
meth public void shiftLine(org.netbeans.editor.BaseDocument,int,boolean) throws javax.swing.text.BadLocationException
supr java.lang.Object
hfds ISC_MAX_INDENT_SIZE,ISC_MAX_TAB_SIZE,customExpandTabs,customShiftWidth,customSpacesPerTab,customTabSize,expandTabs,indentStringCache,indentUtilsClassRef,inited,kitClass,kitClass2Formatter,mimePath2Formatter,noIndentUtils,prefs,prefsListener,shiftWidth,spacesPerTab,tabSize

CLSS public org.netbeans.editor.GapObjectArray
cons public init()
cons public init(java.lang.Object[],int)
innr public abstract interface static RemoveUpdater
intf org.netbeans.editor.ObjectArray
intf org.netbeans.editor.ObjectArray$CopyItems
meth protected void movedAboveGapUpdate(java.lang.Object[],int,int)
meth protected void movedBelowGapUpdate(java.lang.Object[],int,int)
meth protected void unoptimizedRemove(int,int,org.netbeans.editor.GapObjectArray$RemoveUpdater)
meth public int getItemCount()
meth public java.lang.Object getItem(int)
meth public java.lang.String toStringDetail()
meth public void compact()
meth public void copyItems(int,int,java.lang.Object[],int)
meth public void insertAll(int,java.lang.Object[])
meth public void insertAll(int,java.lang.Object[],int,int)
meth public void insertItem(int,java.lang.Object)
meth public void remove(int,int)
meth public void remove(int,int,org.netbeans.editor.GapObjectArray$RemoveUpdater)
meth public void replace(int,int,java.lang.Object[])
supr java.lang.Object
hfds EMPTY_ARRAY,array,gapLength,gapStart

CLSS public abstract interface static org.netbeans.editor.GapObjectArray$RemoveUpdater
 outer org.netbeans.editor.GapObjectArray
meth public abstract void removeUpdate(java.lang.Object)

CLSS public abstract interface org.netbeans.editor.GapStart
 anno 0 java.lang.Deprecated()
meth public abstract int getGapStart()

CLSS public org.netbeans.editor.GlyphGutter
cons public init()
cons public init(org.netbeans.editor.EditorUI)
intf javax.accessibility.Accessible
intf org.netbeans.editor.Annotations$AnnotationsListener
intf org.netbeans.editor.SideBarFactory
meth protected int getDigitCount(int)
meth protected int getHeightDimension()
meth protected int getLineCount()
meth protected int getLineNumberWidth()
meth protected int getWidthDimension()
meth protected void checkSize()
meth protected void init()
meth protected void resize()
meth public java.lang.String getToolTipText(java.awt.event.MouseEvent)
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.JComponent createSideBar(javax.swing.text.JTextComponent)
meth public void changedAll()
meth public void changedLine(int)
meth public void paintComponent(java.awt.Graphics)
meth public void update()
supr javax.swing.JComponent
hfds DEFAULT_GUTTER_LINE,ENLARGE_GUTTER_HEIGHT,LOG,REPAINT_TASK_DELAY,TEXT_ZOOM_PROPERTY,annoTypesListener,annos,backgroundColor,cachedCountOfAnnos,cachedCountOfAnnosForLine,coloringMap,coloringMapListener,drawOverLineNumbers,editorUI,editorUIListener,font,foreColor,glyphButtonWidth,glyphGutterWidth,glyphWidth,gutterButton,gutterMouseListener,highestLineNumber,init,leftGap,prefs,prefsListener,repaintTask,rightGap,showLineNumbers,toRepaint,toRepaintLock
hcls EditorUIListener,GutterMouseListener

CLSS public org.netbeans.editor.GuardedDocument
cons public init(java.lang.Class)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.Class,boolean,javax.swing.text.StyleContext)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String)
cons public init(java.lang.String,boolean,javax.swing.text.StyleContext)
fld protected java.lang.String normalStyleName
fld protected javax.swing.text.StyleContext styles
fld public final static java.lang.String FMT_GUARDED_INSERT_LOCALE = "FMT_guarded_insert"
fld public final static java.lang.String FMT_GUARDED_REMOVE_LOCALE = "FMT_guarded_remove"
fld public final static java.lang.String GUARDED_ATTRIBUTE = "guarded"
fld public final static javax.swing.text.SimpleAttributeSet guardedSet
fld public final static javax.swing.text.SimpleAttributeSet unguardedSet
intf javax.swing.text.StyledDocument
meth protected org.netbeans.editor.BaseDocumentEvent createDocumentEvent(int,int,javax.swing.event.DocumentEvent$EventType)
meth protected void preInsertCheck(int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth protected void preRemoveCheck(int,int) throws javax.swing.text.BadLocationException
meth public boolean isPosGuarded(int)
meth public java.awt.Color getBackground(javax.swing.text.AttributeSet)
meth public java.awt.Color getForeground(javax.swing.text.AttributeSet)
meth public java.awt.Font getFont(javax.swing.text.AttributeSet)
meth public java.lang.String toStringDetail()
meth public java.util.Enumeration getStyleNames()
meth public javax.swing.text.Element getCharacterElement(int)
meth public javax.swing.text.Style addStyle(java.lang.String,javax.swing.text.Style)
meth public javax.swing.text.Style getLogicalStyle(int)
meth public javax.swing.text.Style getStyle(java.lang.String)
meth public org.netbeans.editor.MarkBlockChain getGuardedBlockChain()
meth public void removeStyle(java.lang.String)
meth public void runAtomic(java.lang.Runnable)
meth public void runAtomicAsUser(java.lang.Runnable)
meth public void setCharacterAttributes(int,int,javax.swing.text.AttributeSet,boolean)
meth public void setLogicalStyle(int,javax.swing.text.Style)
meth public void setNormalStyleName(java.lang.String)
meth public void setParagraphAttributes(int,int,javax.swing.text.AttributeSet,boolean)
supr org.netbeans.editor.BaseDocument
hfds LOG,atomicAsUser,breakGuarded,debugAtomic,debugAtomicStack,guardedBlockChain

CLSS public org.netbeans.editor.GuardedDocumentEvent
cons public init(org.netbeans.editor.GuardedDocument,int,int,javax.swing.event.DocumentEvent$EventType)
meth public void redo()
meth public void undo()
supr org.netbeans.editor.BaseDocumentEvent
hfds serialVersionUID

CLSS public org.netbeans.editor.GuardedException
cons public init(java.lang.String,int)
supr javax.swing.text.BadLocationException
hfds serialVersionUID

CLSS public abstract interface org.netbeans.editor.ImageTokenID
intf org.netbeans.editor.TokenID
meth public abstract java.lang.String getImage()

CLSS public abstract org.netbeans.editor.ImplementationProvider
 anno 0 java.lang.Deprecated()
cons public init()
meth public abstract java.util.ResourceBundle getResourceBundle(java.lang.String)
meth public abstract javax.swing.Action[] getGlyphGutterActions(javax.swing.text.JTextComponent)
meth public boolean activateComponent(javax.swing.text.JTextComponent)
meth public static org.netbeans.editor.ImplementationProvider getDefault()
meth public static void registerDefault(org.netbeans.editor.ImplementationProvider)
supr java.lang.Object
hfds PROVIDER
hcls ProviderBridge,Wrapper

CLSS public org.netbeans.editor.InvalidMarkException
supr java.lang.Exception
hfds serialVersionUID

CLSS public final org.netbeans.editor.JumpList
innr public final static Entry
meth public static boolean hasNext()
meth public static boolean hasPrev()
meth public static java.lang.String dump()
 anno 0 java.lang.Deprecated()
meth public static void addEntry(javax.swing.text.JTextComponent,int)
meth public static void checkAddEntry()
meth public static void checkAddEntry(javax.swing.text.JTextComponent)
meth public static void checkAddEntry(javax.swing.text.JTextComponent,int)
 anno 0 java.lang.Deprecated()
meth public static void jumpNext(javax.swing.text.JTextComponent)
meth public static void jumpNextComponent(javax.swing.text.JTextComponent)
meth public static void jumpPrev(javax.swing.text.JTextComponent)
meth public static void jumpPrevComponent(javax.swing.text.JTextComponent)
supr java.lang.Object
hfds LOG,listener,support

CLSS public final static org.netbeans.editor.JumpList$Entry
 outer org.netbeans.editor.JumpList
meth public boolean setDot()
meth public int getPosition()
meth public javax.swing.text.JTextComponent getComponent()
supr java.lang.Object

CLSS public org.netbeans.editor.KeySequenceInputPanel
cons public init()
fld public final static java.lang.String PROP_KEYSEQUENCE = "keySequence"
fld public javax.swing.JLabel keySequenceLabel
fld public javax.swing.JTextArea collisionLabel
fld public javax.swing.JTextField keySequenceInputField
meth public java.awt.Dimension getPreferredSize()
meth public javax.swing.KeyStroke[] getKeySequence()
meth public void clear()
meth public void requestFocus()
meth public void setInfoText(java.lang.String)
supr javax.swing.JPanel
hfds bundle,strokes,text

CLSS public org.netbeans.editor.LeafElement
cons public init(org.netbeans.editor.BaseDocument,org.netbeans.editor.BaseElement,javax.swing.text.AttributeSet,int,int,boolean,boolean)
fld protected boolean bol
fld protected boolean eol
fld protected org.netbeans.editor.Mark endMark
fld protected org.netbeans.editor.Mark startMark
meth protected void finalize() throws java.lang.Throwable
meth public boolean isLeaf()
meth public final boolean isBOL()
meth public final boolean isEOL()
meth public final int getEndOffset()
meth public final int getStartOffset()
meth public final org.netbeans.editor.Mark getEndMark()
meth public final org.netbeans.editor.Mark getStartMark()
meth public int getElementCount()
meth public int getElementIndex(int)
meth public java.lang.String toString()
meth public javax.swing.text.Element getElement(int)
supr org.netbeans.editor.BaseElement

CLSS public org.netbeans.editor.LeafView
cons public init(javax.swing.text.Element)
fld protected int mainHeight
meth protected int getPaintAreas(java.awt.Graphics,int,int)
meth protected int getPosFromY(int)
meth protected int getViewStartY(org.netbeans.editor.BaseView,int)
meth protected int getYFromPos(int) throws javax.swing.text.BadLocationException
meth protected void paintAreas(java.awt.Graphics,int,int,int)
meth public final int getViewCount()
meth public final javax.swing.text.View getView(int)
meth public int getBaseX(int)
meth public int getHeight()
meth public int getNextVisualPositionFrom(int,javax.swing.text.Position$Bias,java.awt.Shape,int,javax.swing.text.Position$Bias[]) throws javax.swing.text.BadLocationException
meth public int viewToModel(float,float,java.awt.Shape,javax.swing.text.Position$Bias[])
meth public java.awt.Shape modelToView(int,java.awt.Shape,javax.swing.text.Position$Bias) throws javax.swing.text.BadLocationException
meth public java.awt.Shape modelToView(int,javax.swing.text.Position$Bias,int,javax.swing.text.Position$Bias,java.awt.Shape) throws javax.swing.text.BadLocationException
meth public void changedUpdate(javax.swing.event.DocumentEvent,java.awt.Shape,javax.swing.text.ViewFactory)
meth public void insertUpdate(javax.swing.event.DocumentEvent,java.awt.Shape,javax.swing.text.ViewFactory)
meth public void removeUpdate(javax.swing.event.DocumentEvent,java.awt.Shape,javax.swing.text.ViewFactory)
meth public void setParent(javax.swing.text.View)
meth public void updateMainHeight()
supr org.netbeans.editor.BaseView
hfds modelToViewDG,viewToModelDG
hcls ModelToViewDG,ViewToModelDG

CLSS public org.netbeans.editor.LineSeparatorConversion
 anno 0 java.lang.Deprecated()
fld public final static char LS = '\u2028'
fld public final static char PS = '\u2029'
fld public final static java.lang.String LS_LS
fld public final static java.lang.String LS_PS
innr public static FromLineFeed
innr public static InitialSeparatorReader
innr public static ToLineFeed
meth public static java.lang.String convertFromLineFeed(java.lang.String,java.lang.String)
meth public static java.lang.String convertToLineFeed(java.lang.String)
meth public static void convertFromLineFeed(java.lang.String,int,int,java.lang.String,java.lang.StringBuffer)
meth public static void convertToLineFeed(java.lang.String,int,int,java.lang.StringBuffer)
supr java.lang.Object
hfds DEFAULT_CONVERSION_BUFFER_SIZE

CLSS public static org.netbeans.editor.LineSeparatorConversion$FromLineFeed
 outer org.netbeans.editor.LineSeparatorConversion
cons public init(char[],int,int,java.lang.String)
cons public init(char[],int,int,java.lang.String,int)
cons public init(java.lang.String,int,int,java.lang.String)
cons public init(java.lang.String,int,int,java.lang.String,int)
meth public javax.swing.text.Segment nextConverted()
supr java.lang.Object
hfds charArrayOrSequence,convertedText,endOffset,lineFeedReplace,offset

CLSS public static org.netbeans.editor.LineSeparatorConversion$InitialSeparatorReader
 outer org.netbeans.editor.LineSeparatorConversion
cons public init(java.io.Reader)
meth public int read() throws java.io.IOException
meth public int read(char[],int,int) throws java.io.IOException
meth public java.lang.String getInitialSeparator()
meth public void close() throws java.io.IOException
supr java.io.Reader
hfds AFTER_CR_STATUS,CRLF_SEPARATOR,CR_SEPARATOR,INITIAL_STATUS,LF_SEPARATOR,LS_SEPARATOR,PS_SEPARATOR,delegate,status

CLSS public static org.netbeans.editor.LineSeparatorConversion$ToLineFeed
 outer org.netbeans.editor.LineSeparatorConversion
cons public init(java.io.Reader)
cons public init(java.io.Reader,int)
meth public boolean isReadWholeBuffer()
meth public javax.swing.text.Segment nextConverted() throws java.io.IOException
supr java.lang.Object
hfds convertedText,lastCharCR,readWholeBuffer,reader

CLSS public org.netbeans.editor.LocaleSupport
 anno 0 java.lang.Deprecated()
cons public init()
innr public abstract interface static Localizer
meth public static char getChar(java.lang.String,char)
meth public static java.lang.String getString(java.lang.String)
meth public static java.lang.String getString(java.lang.String,java.lang.String)
meth public static void addLocalizer(org.netbeans.editor.LocaleSupport$Localizer)
meth public static void removeLocalizer(org.netbeans.editor.LocaleSupport$Localizer)
supr java.lang.Object
hfds debug,localizers

CLSS public abstract interface static org.netbeans.editor.LocaleSupport$Localizer
 outer org.netbeans.editor.LocaleSupport
meth public abstract java.lang.String getString(java.lang.String)

CLSS public org.netbeans.editor.MacroDialogSupport
 anno 0 java.lang.Deprecated()
cons public init(java.lang.Class)
intf java.awt.event.ActionListener
meth protected int showConfirmDialog(java.lang.String)
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void setBody(java.lang.String)
meth public void showMacroDialog()
supr java.lang.Object
hfds cancelButton,kitClass,macroDialog,okButton,panel

CLSS public org.netbeans.editor.MacroSavePanel
 anno 0 java.lang.Deprecated()
cons public init(java.lang.Class)
fld public javax.swing.JButton addButton
fld public javax.swing.JButton removeButton
fld public javax.swing.JLabel bindingLabel
fld public javax.swing.JLabel macroLabel
fld public javax.swing.JLabel nameLabel
fld public javax.swing.JList bindingList
fld public javax.swing.JPanel bindingPanel
fld public javax.swing.JPanel macroPanel
fld public javax.swing.JScrollPane bindingScrollPane
fld public javax.swing.JTextField macroField
fld public javax.swing.JTextField nameField
meth public java.awt.Dimension getPreferredSize()
meth public java.lang.String getMacroBody()
meth public java.lang.String getMacroName()
meth public java.util.List getKeySequences()
meth public void popupNotify()
meth public void setKeySequences(java.util.List)
meth public void setMacroBody(java.lang.String)
meth public void setMacroName(java.lang.String)
supr javax.swing.JPanel
hfds bindings,bundle,kitClass
hcls KeySequenceCellRenderer,KeySequenceRequester

CLSS public org.netbeans.editor.Mark
 anno 0 java.lang.Deprecated()
cons public init()
cons public init(boolean)
cons public init(javax.swing.text.Position$Bias)
meth protected void removeUpdateAction(int,int)
meth public final boolean getBackwardBias()
meth public final boolean getInsertAfter()
meth public final boolean isValid()
meth public final int compare(int) throws org.netbeans.editor.InvalidMarkException
meth public final int getLine() throws org.netbeans.editor.InvalidMarkException
meth public final int getOffset() throws org.netbeans.editor.InvalidMarkException
meth public final javax.swing.text.Position$Bias getBias()
meth public final void dispose()
meth public final void remove() throws org.netbeans.editor.InvalidMarkException
meth public java.lang.String toString()
supr java.lang.Object
hfds MARK_COMPARATOR,bias,doc,pos
hcls MarkComparator

CLSS public org.netbeans.editor.MarkBlock
cons public init(org.netbeans.editor.BaseDocument,int,int) throws javax.swing.text.BadLocationException
cons public init(org.netbeans.editor.BaseDocument,org.netbeans.editor.Mark,org.netbeans.editor.Mark)
cons public init(org.netbeans.editor.BaseDocument,org.netbeans.editor.Mark,org.netbeans.editor.Mark,int,int) throws javax.swing.text.BadLocationException
fld protected org.netbeans.editor.BaseDocument doc
fld protected org.netbeans.editor.MarkBlock next
fld protected org.netbeans.editor.MarkBlock prev
fld public final static int AFTER = 128
fld public final static int BEFORE = 64
fld public final static int CONTINUE = 2
fld public final static int CONTINUE_BEGIN = 66
fld public final static int CONTINUE_END = 130
fld public final static int EMPTY = 4
fld public final static int EXTEND = 17
fld public final static int EXTEND_BEGIN = 273
fld public final static int EXTEND_END = 529
fld public final static int IGNORE_EMPTY = -13
fld public final static int INCLUDE = 785
fld public final static int INNER = 4129
fld public final static int INSIDE = 33
fld public final static int INSIDE_BEGIN = 1057
fld public final static int INSIDE_END = 2081
fld public final static int INVALID = 0
fld public final static int OVERLAP = 1
fld public final static int OVERLAP_BEGIN = 257
fld public final static int OVERLAP_END = 513
fld public final static int SAME = 3105
fld public final static int THIS_EMPTY = 8
fld public org.netbeans.editor.Mark endMark
fld public org.netbeans.editor.Mark startMark
meth protected void finalize() throws java.lang.Throwable
meth public boolean checkReverse()
meth public boolean extend(int,int,boolean) throws javax.swing.text.BadLocationException
meth public boolean extend(org.netbeans.editor.MarkBlock,boolean)
meth public boolean isReverse()
meth public final org.netbeans.editor.MarkBlock getNext()
meth public final org.netbeans.editor.MarkBlock getPrev()
meth public final void setNext(org.netbeans.editor.MarkBlock)
meth public final void setPrev(org.netbeans.editor.MarkBlock)
meth public int compare(int,int)
meth public int extendEnd(int) throws javax.swing.text.BadLocationException
meth public int extendStart(int) throws javax.swing.text.BadLocationException
meth public int getEndOffset()
meth public int getStartOffset()
meth public int shrink(int,int) throws javax.swing.text.BadLocationException
meth public java.lang.String toString()
meth public java.lang.String toStringChain()
meth public javax.swing.text.Document getDocument()
meth public org.netbeans.editor.MarkBlock addChain(org.netbeans.editor.MarkBlock)
meth public org.netbeans.editor.MarkBlock insertChain(org.netbeans.editor.MarkBlock)
meth public org.netbeans.editor.MarkBlock removeChain()
meth public static java.lang.String debugRelation(int)
meth public void reverse()
meth public void setNextChain(org.netbeans.editor.MarkBlock)
meth public void setPrevChain(org.netbeans.editor.MarkBlock)
supr java.lang.Object

CLSS public org.netbeans.editor.MarkBlockChain
cons public init(org.netbeans.editor.BaseDocument)
fld protected org.netbeans.editor.BaseDocument doc
fld protected org.netbeans.editor.MarkBlock chain
fld protected org.netbeans.editor.MarkBlock currentBlock
fld public final static java.lang.String PROP_BLOCKS_CHANGED = "MarkBlockChain.PROP_BLOCKS_CHANGED"
meth protected org.netbeans.editor.Mark createBlockEndMark()
meth protected org.netbeans.editor.Mark createBlockStartMark()
meth protected org.netbeans.editor.MarkBlock checkedRemove(org.netbeans.editor.MarkBlock)
meth protected org.netbeans.editor.MarkBlock createBlock(int,int) throws javax.swing.text.BadLocationException
meth public final org.netbeans.editor.MarkBlock getChain()
meth public int adjustToBlockEnd(int)
meth public int adjustToNextBlockStart(int)
meth public int compareBlock(int,int)
meth public java.lang.String toString()
meth public void addBlock(int,int,boolean)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removeBlock(int,int)
meth public void removeEmptyBlocks()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds PCS

CLSS public org.netbeans.editor.MarkFactory
innr public static ContextMark
innr public static SyntaxMark
supr java.lang.Object

CLSS public static org.netbeans.editor.MarkFactory$ContextMark
 outer org.netbeans.editor.MarkFactory
cons public init(boolean)
cons public init(boolean,boolean)
cons public init(javax.swing.text.Position$Bias,boolean)
supr org.netbeans.editor.Mark

CLSS public static org.netbeans.editor.MarkFactory$SyntaxMark
 outer org.netbeans.editor.MarkFactory
 anno 0 java.lang.Deprecated()
cons public init()
meth protected void removeUpdateAction(int,int)
meth public org.netbeans.editor.Syntax$StateInfo getStateInfo()
meth public org.netbeans.editor.TokenItem getTokenItem()
meth public void updateStateInfo(org.netbeans.editor.Syntax)
supr org.netbeans.editor.Mark
hfds stateInfo,tokenItem

CLSS public abstract interface org.netbeans.editor.MimeTypeInitializer
meth public abstract void init(java.lang.String)

CLSS public org.netbeans.editor.MultiKeyBinding
cons public init()
cons public init(javax.swing.KeyStroke,java.lang.String)
cons public init(javax.swing.KeyStroke[],java.lang.String)
cons public init(javax.swing.text.JTextComponent$KeyBinding)
fld public javax.swing.KeyStroke[] keys
intf java.io.Externalizable
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public static void updateKeyBindings(javax.swing.text.JTextComponent$KeyBinding[],javax.swing.text.JTextComponent$KeyBinding[])
meth public void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr javax.swing.text.JTextComponent$KeyBinding
hfds serialVersionUID

CLSS public org.netbeans.editor.MultiKeymap
cons public init(java.lang.String)
fld public final static javax.swing.Action BEEP_ACTION
fld public final static javax.swing.Action EMPTY_ACTION
intf javax.swing.text.Keymap
meth public boolean isLocallyDefined(javax.swing.KeyStroke)
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public javax.swing.Action getAction(javax.swing.KeyStroke)
meth public javax.swing.Action getDefaultAction()
meth public javax.swing.Action[] getBoundActions()
meth public javax.swing.KeyStroke[] getBoundKeyStrokes()
meth public javax.swing.KeyStroke[] getKeyStrokesForAction(javax.swing.Action)
meth public javax.swing.text.Keymap getResolveParent()
meth public void addActionForKeyStroke(javax.swing.KeyStroke,javax.swing.Action)
meth public void load(javax.swing.text.JTextComponent$KeyBinding[],java.util.Map)
meth public void load(javax.swing.text.JTextComponent$KeyBinding[],javax.swing.Action[])
meth public void removeBindings()
meth public void removeKeyStrokeBinding(javax.swing.KeyStroke)
meth public void resetContext()
meth public void setContextKeyNotFoundAction(javax.swing.Action)
meth public void setDefaultAction(javax.swing.Action)
meth public void setResolveParent(javax.swing.text.Keymap)
supr java.lang.Object
hfds LOG,compatibleIgnoreNextTyped,context,contextKeyNotFoundAction,contextKeys,delegate,ignoreNextTyped
hcls KeymapSetContextAction

CLSS public abstract interface org.netbeans.editor.ObjectArray
innr public abstract interface static CopyItems
innr public abstract interface static Modification
meth public abstract int getItemCount()
meth public abstract java.lang.Object getItem(int)

CLSS public abstract interface static org.netbeans.editor.ObjectArray$CopyItems
 outer org.netbeans.editor.ObjectArray
meth public abstract void copyItems(int,int,java.lang.Object[],int)

CLSS public abstract interface static org.netbeans.editor.ObjectArray$Modification
 outer org.netbeans.editor.ObjectArray
meth public abstract int getIndex()
meth public abstract int getRemovedItemsCount()
meth public abstract java.lang.Object[] getAddedItems()
meth public abstract org.netbeans.editor.ObjectArray getArray()

CLSS public org.netbeans.editor.ObjectArrayUtilities
meth public static int binarySearch(org.netbeans.editor.ObjectArray,java.lang.Object)
meth public static int binarySearch(org.netbeans.editor.ObjectArray,java.lang.Object,java.util.Comparator)
meth public static int findIndex(org.netbeans.editor.ObjectArray,java.lang.Object)
meth public static int findIndex(org.netbeans.editor.ObjectArray,java.lang.Object,java.util.Comparator)
meth public static java.lang.Object[] toArray(org.netbeans.editor.ObjectArray)
meth public static java.lang.Object[] toArray(org.netbeans.editor.ObjectArray,int,int)
meth public static org.netbeans.editor.ObjectArray$Modification mergeModifications(org.netbeans.editor.ObjectArray$Modification,org.netbeans.editor.ObjectArray$Modification)
meth public static org.netbeans.editor.ObjectArray$Modification mergeModifications(org.netbeans.editor.ObjectArray$Modification[])
meth public static void copyItems(org.netbeans.editor.ObjectArray,int,int,java.lang.Object[],int)
meth public static void copyItems(org.netbeans.editor.ObjectArray,java.lang.Object[])
meth public static void reverse(java.lang.Object[])
supr java.lang.Object

CLSS public org.netbeans.editor.PopupManager
cons public init(javax.swing.text.JTextComponent)
fld public final static org.netbeans.editor.PopupManager$HorizontalBounds ScrollBarBounds
fld public final static org.netbeans.editor.PopupManager$HorizontalBounds ViewPortBounds
fld public final static org.netbeans.editor.PopupManager$Placement Above
fld public final static org.netbeans.editor.PopupManager$Placement AbovePreferred
fld public final static org.netbeans.editor.PopupManager$Placement Below
fld public final static org.netbeans.editor.PopupManager$Placement BelowPreferred
fld public final static org.netbeans.editor.PopupManager$Placement FixedPoint
fld public final static org.netbeans.editor.PopupManager$Placement Largest
innr public final static HorizontalBounds
innr public final static Placement
meth protected static java.awt.Rectangle computeBounds(javax.swing.JComponent,int,int,java.awt.Rectangle,org.netbeans.editor.PopupManager$Placement)
meth protected static java.awt.Rectangle computeBounds(javax.swing.JComponent,int,int,java.awt.Rectangle,org.netbeans.editor.PopupManager$Placement,org.netbeans.editor.PopupManager$HorizontalBounds)
meth protected static java.awt.Rectangle computeBounds(javax.swing.JComponent,javax.swing.JComponent,java.awt.Rectangle,org.netbeans.editor.PopupManager$Placement)
meth protected static java.awt.Rectangle computeBounds(javax.swing.JComponent,javax.swing.JComponent,java.awt.Rectangle,org.netbeans.editor.PopupManager$Placement,org.netbeans.editor.PopupManager$HorizontalBounds)
meth public javax.swing.JComponent get()
meth public void install(javax.swing.JComponent)
meth public void install(javax.swing.JComponent,java.awt.Rectangle,org.netbeans.editor.PopupManager$Placement)
meth public void install(javax.swing.JComponent,java.awt.Rectangle,org.netbeans.editor.PopupManager$Placement,org.netbeans.editor.PopupManager$HorizontalBounds)
meth public void install(javax.swing.JComponent,java.awt.Rectangle,org.netbeans.editor.PopupManager$Placement,org.netbeans.editor.PopupManager$HorizontalBounds,int,int)
meth public void uninstall(javax.swing.JComponent)
supr java.lang.Object
hfds LOG,SUPPRESS_POPUP_KEYBOARD_FORWARDING_CLIENT_PROPERTY_KEY,componentListener,keyListener,popup,textComponent
hcls PopupKeyListener,TextComponentListener

CLSS public final static org.netbeans.editor.PopupManager$HorizontalBounds
 outer org.netbeans.editor.PopupManager
meth public java.lang.String toString()
supr java.lang.Object
hfds representation

CLSS public final static org.netbeans.editor.PopupManager$Placement
 outer org.netbeans.editor.PopupManager
meth public java.lang.String toString()
supr java.lang.Object
hfds representation

CLSS public abstract interface org.netbeans.editor.PrintContainer
meth public abstract boolean initEmptyLines()
meth public abstract void add(char[],java.awt.Font,java.awt.Color,java.awt.Color)
meth public abstract void eol()

CLSS public org.netbeans.editor.Registry
 anno 0 java.lang.Deprecated()
cons public init()
meth public static java.lang.String registryToString()
meth public static java.util.Iterator<? extends javax.swing.text.Document> getDocumentIterator()
meth public static java.util.Iterator<? extends javax.swing.text.JTextComponent> getComponentIterator()
meth public static javax.swing.text.JTextComponent getLeastActiveComponent()
meth public static javax.swing.text.JTextComponent getMostActiveComponent()
meth public static org.netbeans.editor.BaseDocument getLeastActiveDocument()
meth public static org.netbeans.editor.BaseDocument getMostActiveDocument()
meth public static void addChangeListener(javax.swing.event.ChangeListener)
meth public static void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
hfds editorRegistryListener,listenerList

CLSS public org.netbeans.editor.SegmentCache
 anno 0 java.lang.Deprecated()
cons public init()
meth public javax.swing.text.Segment getSegment()
meth public static org.netbeans.editor.SegmentCache getSharedInstance()
meth public void releaseSegment(javax.swing.text.Segment)
supr java.lang.Object
hfds SHARED

CLSS public abstract interface org.netbeans.editor.SideBarFactory
 anno 0 java.lang.Deprecated()
 anno 0 org.netbeans.spi.editor.mimelookup.MimeLocation(java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass=class org.netbeans.spi.editor.mimelookup.InstanceProvider, java.lang.String subfolderName="SideBar")
meth public abstract javax.swing.JComponent createSideBar(javax.swing.text.JTextComponent)

CLSS public org.netbeans.editor.StatusBar
cons public init(org.netbeans.editor.EditorUI)
fld protected org.netbeans.editor.EditorUI editorUI
fld public final static java.lang.String CELL_MAIN = "main"
fld public final static java.lang.String CELL_POSITION = "position"
fld public final static java.lang.String CELL_TYPING_MODE = "typing-mode"
fld public final static java.lang.String INSERT_LOCALE = "status-bar-insert"
fld public final static java.lang.String OVERWRITE_LOCALE = "status-bar-overwrite"
innr public final static StatusBarFactory
intf java.beans.PropertyChangeListener
intf javax.swing.event.DocumentListener
meth protected javax.swing.JPanel createPanel()
meth protected void initPanel()
meth public boolean isVisible()
meth public final javax.swing.JPanel getPanel()
meth public int getCellCount()
meth public java.lang.String getText(java.lang.String)
meth public javax.swing.JLabel addCell(int,java.lang.String,java.lang.String[])
meth public javax.swing.JLabel addCell(java.lang.String,java.lang.String[])
meth public javax.swing.JLabel getCellByName(java.lang.String)
meth public static void setGlobalCell(java.lang.String,javax.swing.JLabel)
meth public void addCustomCell(int,javax.swing.JLabel)
meth public void changedUpdate(javax.swing.event.DocumentEvent)
meth public void insertUpdate(javax.swing.event.DocumentEvent)
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void removeUpdate(javax.swing.event.DocumentEvent)
meth public void setBoldText(java.lang.String,java.lang.String)
meth public void setText(java.lang.String,int)
meth public void setText(java.lang.String,java.lang.String)
meth public void setText(java.lang.String,java.lang.String,org.netbeans.editor.Coloring)
meth public void setText(java.lang.String,java.lang.String,org.netbeans.editor.Coloring,int)
meth public void setVisible(boolean)
meth public void updateGlobal()
supr java.lang.Object
hfds CARET_OFFSET_LOG,CELL_BORDER,NULL_INSETS,POS_MAX_STRINGS,POS_MAX_STRINGS_OFFSET,caret,caretDelay,caretL,caretPositionLocaleString,cellList,cellName2GlobalCell,insText,insertModeLocaleString,overwriteModeDisplayed,overwriteModeLocaleString,ovrText,panel,prefs,prefsListener,serialVersionUID,visible,weakListener
hcls CaretListener,Cell

CLSS public final static org.netbeans.editor.StatusBar$StatusBarFactory
 outer org.netbeans.editor.StatusBar
cons public init()
intf org.netbeans.editor.SideBarFactory
meth public javax.swing.JComponent createSideBar(javax.swing.text.JTextComponent)
supr java.lang.Object

CLSS public org.netbeans.editor.StringMap
cons public init()
cons public init(int)
cons public init(int,float)
cons public init(java.util.Map)
meth public boolean containsKey(char[],int,int)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.Object get(char[],int,int)
meth public java.lang.Object remove(char[],int,int)
supr java.util.HashMap
hfds serialVersionUID,testChars,testLen,testOffset

CLSS public org.netbeans.editor.Syntax
cons public init()
fld protected boolean lastBuffer
fld protected char[] buffer
fld protected int offset
fld protected int state
fld protected int stopOffset
fld protected int stopPosition
fld protected int tokenLength
fld protected int tokenOffset
fld protected org.netbeans.editor.TokenContextPath tokenContextPath
fld protected org.netbeans.editor.TokenID supposedTokenID
fld public final static int DIFFERENT_STATE = 1
fld public final static int EQUAL_STATE = 0
fld public final static int INIT = -1
innr public abstract interface static StateInfo
innr public static BaseStateInfo
meth protected org.netbeans.editor.TokenID parseToken()
meth public char[] getBuffer()
meth public int compareState(org.netbeans.editor.Syntax$StateInfo)
meth public int getOffset()
meth public int getPreScan()
meth public int getTokenLength()
meth public int getTokenOffset()
meth public java.lang.String getStateName(int)
meth public java.lang.String toString()
meth public org.netbeans.editor.Syntax$StateInfo createStateInfo()
meth public org.netbeans.editor.TokenContextPath getTokenContextPath()
meth public org.netbeans.editor.TokenID getSupposedTokenID()
meth public org.netbeans.editor.TokenID nextToken()
meth public void load(org.netbeans.editor.Syntax$StateInfo,char[],int,int,boolean,int)
meth public void loadInitState()
meth public void loadState(org.netbeans.editor.Syntax$StateInfo)
meth public void relocate(char[],int,int,boolean,int)
meth public void reset()
meth public void storeState(org.netbeans.editor.Syntax$StateInfo)
supr java.lang.Object

CLSS public static org.netbeans.editor.Syntax$BaseStateInfo
 outer org.netbeans.editor.Syntax
cons public init()
intf org.netbeans.editor.Syntax$StateInfo
meth public int getPreScan()
meth public int getState()
meth public java.lang.String toString()
meth public java.lang.String toString(org.netbeans.editor.Syntax)
meth public void setPreScan(int)
meth public void setState(int)
supr java.lang.Object
hfds preScan,state

CLSS public abstract interface static org.netbeans.editor.Syntax$StateInfo
 outer org.netbeans.editor.Syntax
meth public abstract int getPreScan()
meth public abstract int getState()
meth public abstract void setPreScan(int)
meth public abstract void setState(int)

CLSS public org.netbeans.editor.SyntaxDebug
cons public init(org.netbeans.editor.Syntax)
fld public final static java.lang.String NO_STATE_ASSIGNED = "NO STATE ASSIGNED"
fld public final static java.lang.String NULL_STATE = "NULL STATE"
fld public final static java.lang.String NULL_SYNTAX_MARK = "NULL SYNTAX MARK"
fld public org.netbeans.editor.Syntax syntax
meth public int debugScan()
meth public int parseFile(java.lang.String) throws java.io.IOException
supr java.lang.Object

CLSS public org.netbeans.editor.SyntaxSupport
cons public init(org.netbeans.editor.BaseDocument)
fld protected boolean tokenNumericIDsValid
meth protected boolean isAbbrevDisabled(int)
meth protected org.netbeans.editor.SyntaxSupport createSyntaxSupport(java.lang.Class)
meth public boolean isIdentifier(java.lang.String)
meth public final org.netbeans.editor.BaseDocument getDocument()
meth public int findInsideBlocks(org.netbeans.editor.Finder,int,int,int[]) throws javax.swing.text.BadLocationException
meth public int findOutsideBlocks(org.netbeans.editor.Finder,int,int,int[]) throws javax.swing.text.BadLocationException
meth public int[] getTokenBlocks(int,int,org.netbeans.editor.TokenID[]) throws javax.swing.text.BadLocationException
meth public org.netbeans.editor.SyntaxSupport get(java.lang.Class)
meth public org.netbeans.editor.TokenItem getTokenChain(int) throws javax.swing.text.BadLocationException
meth public void initSyntax(org.netbeans.editor.Syntax,int,int,boolean,boolean) throws javax.swing.text.BadLocationException
meth public void tokenizeText(org.netbeans.editor.TokenProcessor,int,int,boolean) throws javax.swing.text.BadLocationException
meth public void tokenizeText(org.netbeans.editor.TokenProcessor,java.lang.String)
supr java.lang.Object
hfds EMPTY_INT_ARRAY,MATCH_ARRAY_CACHE_SIZE,doc,lastMatchArrays,lastTokenIDArrays,supMap,tokenBlocks

CLSS public abstract org.netbeans.editor.SyntaxUpdateTokens
cons public init()
innr public TokenInfo
meth public abstract java.util.List syntaxUpdateEnd()
meth public abstract void syntaxUpdateStart()
meth public abstract void syntaxUpdateToken(org.netbeans.editor.TokenID,org.netbeans.editor.TokenContextPath,int,int)
meth public static java.util.List getTokenInfoList(javax.swing.event.DocumentEvent)
meth public static java.util.List getTokenInfoList(javax.swing.text.Document)
supr java.lang.Object
hcls AllTokensProcessor

CLSS public org.netbeans.editor.SyntaxUpdateTokens$TokenInfo
 outer org.netbeans.editor.SyntaxUpdateTokens
cons public init(org.netbeans.editor.SyntaxUpdateTokens,org.netbeans.editor.TokenID,org.netbeans.editor.TokenContextPath,int,int)
meth public final int getOffset()
meth public final org.netbeans.editor.TokenContextPath getContextPath()
meth public final org.netbeans.editor.TokenID getID()
meth public int getLength()
meth public java.lang.String toString()
supr java.lang.Object
hfds contextPath,id,length,offset

CLSS public abstract interface org.netbeans.editor.TextBatchProcessor
meth public abstract int processTextBatch(org.netbeans.editor.BaseDocument,int,int,boolean)

CLSS public abstract interface org.netbeans.editor.TokenCategory
meth public abstract int getNumericID()
meth public abstract java.lang.String getName()

CLSS public org.netbeans.editor.TokenContext
cons public init(java.lang.String)
cons public init(java.lang.String,org.netbeans.editor.TokenContext[])
meth protected void addDeclaredTokenIDs() throws java.lang.IllegalAccessException
meth protected void addTokenID(org.netbeans.editor.TokenID)
meth public java.lang.String getNamePrefix()
meth public org.netbeans.editor.TokenCategory[] getTokenCategories()
meth public org.netbeans.editor.TokenContextPath getContextPath()
meth public org.netbeans.editor.TokenContextPath getContextPath(org.netbeans.editor.TokenContextPath)
meth public org.netbeans.editor.TokenContextPath[] getAllContextPaths()
meth public org.netbeans.editor.TokenContext[] getChildren()
meth public org.netbeans.editor.TokenID[] getTokenIDs()
supr java.lang.Object
hfds EMPTY_CHILDREN,allContextPaths,children,contextPath,lastContextPathPair,namePrefix,pathCache,tokenCategories,tokenCategoryList,tokenIDList,tokenIDs

CLSS public final org.netbeans.editor.TokenContextPath
meth public boolean contains(org.netbeans.editor.TokenContextPath)
meth public int length()
meth public java.lang.String getFullTokenName(org.netbeans.editor.TokenCategory)
meth public java.lang.String getNamePrefix()
meth public java.lang.String toString()
meth public org.netbeans.editor.TokenContextPath getBase()
meth public org.netbeans.editor.TokenContextPath getParent()
meth public org.netbeans.editor.TokenContextPath replaceStart(org.netbeans.editor.TokenContextPath)
meth public org.netbeans.editor.TokenContext[] getContexts()
supr java.lang.Object
hfds base,contexts,namePrefix,parent,registry,replaceStartCache,tokenNameCache
hcls ArrayMatcher

CLSS public abstract interface org.netbeans.editor.TokenID
intf org.netbeans.editor.TokenCategory
meth public abstract org.netbeans.editor.TokenCategory getCategory()

CLSS public abstract interface org.netbeans.editor.TokenItem
innr public abstract static AbstractItem
innr public static FilterItem
meth public abstract int getOffset()
meth public abstract java.lang.String getImage()
meth public abstract org.netbeans.editor.TokenContextPath getTokenContextPath()
meth public abstract org.netbeans.editor.TokenID getTokenID()
meth public abstract org.netbeans.editor.TokenItem getNext()
meth public abstract org.netbeans.editor.TokenItem getPrevious()

CLSS public abstract static org.netbeans.editor.TokenItem$AbstractItem
 outer org.netbeans.editor.TokenItem
cons public init(org.netbeans.editor.TokenID,org.netbeans.editor.TokenContextPath,int,java.lang.String)
intf org.netbeans.editor.TokenItem
meth public int getOffset()
meth public java.lang.String getImage()
meth public java.lang.String toString()
meth public org.netbeans.editor.TokenContextPath getTokenContextPath()
meth public org.netbeans.editor.TokenID getTokenID()
supr java.lang.Object
hfds image,offset,tokenContextPath,tokenID

CLSS public static org.netbeans.editor.TokenItem$FilterItem
 outer org.netbeans.editor.TokenItem
cons public init(org.netbeans.editor.TokenItem)
fld protected org.netbeans.editor.TokenItem delegate
intf org.netbeans.editor.TokenItem
meth public int getOffset()
meth public java.lang.String getImage()
meth public java.lang.String toString()
meth public org.netbeans.editor.TokenContextPath getTokenContextPath()
meth public org.netbeans.editor.TokenID getTokenID()
meth public org.netbeans.editor.TokenItem getNext()
meth public org.netbeans.editor.TokenItem getPrevious()
supr java.lang.Object

CLSS public abstract interface org.netbeans.editor.TokenProcessor
meth public abstract boolean token(org.netbeans.editor.TokenID,org.netbeans.editor.TokenContextPath,int,int)
meth public abstract int eot(int)
meth public abstract void nextBuffer(char[],int,int,int,int,boolean)

CLSS public org.netbeans.editor.Utilities
fld public final static int CASE_LOWER = 1
fld public final static int CASE_SWITCH = 2
fld public final static int CASE_UPPER = 0
meth public static <%0 extends java.lang.Object> {%%0} runWithOnSaveTasksDisabled(org.openide.util.Mutex$Action<{%%0}>)
meth public static boolean changeCase(org.netbeans.editor.BaseDocument,int,int,int) throws javax.swing.text.BadLocationException
meth public static boolean isRowEmpty(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
meth public static boolean isRowWhite(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
meth public static boolean isSelectionShowing(javax.swing.text.Caret)
meth public static boolean isSelectionShowing(javax.swing.text.JTextComponent)
meth public static int getFirstNonEmptyRow(org.netbeans.editor.BaseDocument,int,boolean) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
meth public static int getFirstNonWhiteBwd(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
meth public static int getFirstNonWhiteBwd(org.netbeans.editor.BaseDocument,int,int) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
meth public static int getFirstNonWhiteFwd(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
meth public static int getFirstNonWhiteFwd(org.netbeans.editor.BaseDocument,int,int) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
meth public static int getFirstNonWhiteRow(org.netbeans.editor.BaseDocument,int,boolean) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
meth public static int getFirstWhiteBwd(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
meth public static int getFirstWhiteBwd(org.netbeans.editor.BaseDocument,int,int) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
meth public static int getFirstWhiteFwd(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
meth public static int getFirstWhiteFwd(org.netbeans.editor.BaseDocument,int,int) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
meth public static int getLineOffset(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
meth public static int getNextTabColumn(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
meth public static int getNextWord(javax.swing.text.JTextComponent,int) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
meth public static int getNextWord(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
meth public static int getPositionAbove(javax.swing.text.JTextComponent,int,int) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
meth public static int getPositionBelow(javax.swing.text.JTextComponent,int,int) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
meth public static int getPreviousWord(javax.swing.text.JTextComponent,int) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
meth public static int getPreviousWord(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
meth public static int getRowCount(org.netbeans.editor.BaseDocument)
 anno 0 java.lang.Deprecated()
meth public static int getRowCount(org.netbeans.editor.BaseDocument,int,int) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
meth public static int getRowEnd(javax.swing.text.JTextComponent,int) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
meth public static int getRowEnd(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
meth public static int getRowFirstNonWhite(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
meth public static int getRowIndent(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
meth public static int getRowIndent(org.netbeans.editor.BaseDocument,int,boolean) throws javax.swing.text.BadLocationException
meth public static int getRowLastNonWhite(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
meth public static int getRowStart(javax.swing.text.JTextComponent,int) throws javax.swing.text.BadLocationException
meth public static int getRowStart(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
meth public static int getRowStart(org.netbeans.editor.BaseDocument,int,int) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
meth public static int getRowStartFromLineOffset(org.netbeans.editor.BaseDocument,int)
 anno 0 java.lang.Deprecated()
meth public static int getVisualColumn(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
meth public static int getVisualColumn(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
meth public static int getWordEnd(javax.swing.text.JTextComponent,int) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
meth public static int getWordEnd(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
meth public static int getWordStart(javax.swing.text.JTextComponent,int) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
meth public static int getWordStart(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
meth public static int reformat(org.netbeans.editor.BaseDocument,int,int) throws javax.swing.text.BadLocationException
meth public static int[] getIdentifierBlock(javax.swing.text.JTextComponent,int) throws javax.swing.text.BadLocationException
meth public static int[] getIdentifierBlock(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
meth public static int[] getSelectionOrIdentifierBlock(javax.swing.text.JTextComponent)
meth public static int[] getSelectionOrIdentifierBlock(javax.swing.text.JTextComponent,int) throws javax.swing.text.BadLocationException
meth public static java.lang.Class getKitClass(javax.swing.text.JTextComponent)
meth public static java.lang.String debugDocument(javax.swing.text.Document)
meth public static java.lang.String debugPosition(org.netbeans.editor.BaseDocument,int)
meth public static java.lang.String debugPosition(org.netbeans.editor.BaseDocument,int,java.lang.String)
meth public static java.lang.String getIdentifier(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
meth public static java.lang.String getIdentifierBefore(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
meth public static java.lang.String getSelectionOrIdentifier(javax.swing.text.JTextComponent)
meth public static java.lang.String getSelectionOrIdentifier(javax.swing.text.JTextComponent,int) throws javax.swing.text.BadLocationException
meth public static java.lang.String getStatusText(javax.swing.text.JTextComponent)
meth public static java.lang.String getTabInsertString(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
meth public static java.lang.String getWord(javax.swing.text.JTextComponent,int) throws javax.swing.text.BadLocationException
meth public static java.lang.String getWord(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
meth public static java.lang.String keySequenceToString(javax.swing.KeyStroke[])
meth public static java.lang.String keyStrokeToString(javax.swing.KeyStroke)
meth public static java.lang.String offsetToLineColumnString(org.netbeans.editor.BaseDocument,int)
meth public static javax.swing.JComponent[] createSingleLineEditor(java.lang.String)
meth public static javax.swing.text.JTextComponent getFocusedComponent()
meth public static javax.swing.text.JTextComponent getLastActiveComponent()
meth public static javax.swing.text.View getDocumentView(javax.swing.text.JTextComponent)
meth public static javax.swing.text.View getRootView(javax.swing.text.JTextComponent,java.lang.Class)
meth public static org.netbeans.editor.BaseDocument getDocument(javax.swing.text.JTextComponent)
meth public static org.netbeans.editor.BaseKit getKit(javax.swing.text.JTextComponent)
meth public static org.netbeans.editor.EditorUI getEditorUI(javax.swing.text.JTextComponent)
meth public static org.netbeans.editor.SyntaxSupport getSyntaxSupport(javax.swing.text.JTextComponent)
meth public static void annotateLoggable(java.lang.Throwable)
 anno 0 java.lang.Deprecated()
meth public static void clearStatusText(javax.swing.text.JTextComponent)
meth public static void insertMark(org.netbeans.editor.BaseDocument,org.netbeans.editor.Mark,int) throws javax.swing.text.BadLocationException,org.netbeans.editor.InvalidMarkException
meth public static void moveMark(org.netbeans.editor.BaseDocument,org.netbeans.editor.Mark,int) throws javax.swing.text.BadLocationException,org.netbeans.editor.InvalidMarkException
meth public static void performAction(javax.swing.Action,java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
meth public static void reformatLine(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
meth public static void requestFocus(javax.swing.text.JTextComponent)
meth public static void returnFocus()
meth public static void runInEventDispatchThread(java.lang.Runnable)
meth public static void runViewHierarchyTransaction(javax.swing.text.JTextComponent,boolean,java.lang.Runnable)
meth public static void setStatusBoldText(javax.swing.text.JTextComponent,java.lang.String)
meth public static void setStatusText(javax.swing.text.JTextComponent,java.lang.String)
meth public static void setStatusText(javax.swing.text.JTextComponent,java.lang.String,int)
meth public static void setStatusText(javax.swing.text.JTextComponent,java.lang.String,org.netbeans.editor.Coloring)
supr java.lang.Object
hfds NO_ACTION,WRONG_POSITION_LOCALE,discardBias,focusedComponentAction,logger
hcls DelegatingBorder,ManageViewPositionListener

CLSS public org.netbeans.editor.WeakEventListenerList
cons public init()
meth public java.lang.Object[] getListenerList()
meth public java.lang.String toString()
meth public java.util.EventListener[] getListeners(java.lang.Class)
meth public void add(java.lang.Class,java.util.EventListener)
meth public void remove(java.lang.Class,java.util.EventListener)
supr javax.swing.event.EventListenerList
hfds listenerSize

CLSS public org.netbeans.editor.WeakPropertyChangeSupport
cons public init()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void firePropertyChange(java.lang.Object,java.lang.String,java.lang.Object,java.lang.Object)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds interestNames,listeners

CLSS public org.netbeans.editor.WeakTimerListener
cons public init(java.awt.event.ActionListener)
cons public init(java.awt.event.ActionListener,boolean)
intf java.awt.event.ActionListener
meth public void actionPerformed(java.awt.event.ActionEvent)
supr java.lang.Object
hfds ref,stopTimer

CLSS public org.netbeans.editor.WordMatch
cons public init(org.netbeans.editor.EditorUI)
intf java.beans.PropertyChangeListener
meth public int find(int,char[],int,int,int,int)
meth public java.lang.String getMatchWord(int,boolean)
meth public java.lang.String getPreviousWord()
meth public java.lang.String toString()
meth public void clear()
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void reset()
supr org.netbeans.editor.FinderFactory$AbstractFinder
hfds NULL_DOC,baseWord,editorUI,forwardSearch,lastWord,matchCase,matchOneChar,maxSearchLen,prefs,prefsListener,previousWord,realMatchCase,smartCase,startDoc,staticWordsDocs,weakListener,word,wordInfoList,wordLen,wordsIndex,wordsMap,wrapSearch
hcls WordInfo

CLSS public abstract org.netbeans.editor.ext.AbstractFormatLayer
cons public init(java.lang.String)
intf org.netbeans.editor.ext.FormatLayer
meth protected org.netbeans.editor.ext.FormatSupport createFormatSupport(org.netbeans.editor.ext.FormatWriter)
meth public java.lang.String getName()
supr java.lang.Object
hfds name

CLSS public org.netbeans.editor.ext.CharacterArrayIterator
cons public init(char[],int,int)
intf java.text.CharacterIterator
meth public char current()
meth public char first()
meth public char last()
meth public char next()
meth public char previous()
meth public char setIndex(int)
meth public int getBeginIndex()
meth public int getEndIndex()
meth public int getIndex()
meth public java.lang.Object clone()
supr java.lang.Object
hfds beginIndex,chars,endIndex,index

CLSS public abstract interface org.netbeans.editor.ext.DataAccessor
meth public abstract int getFileLength()
meth public abstract long getFilePointer() throws java.io.IOException
meth public abstract void append(byte[],int,int) throws java.io.IOException
meth public abstract void close() throws java.io.IOException
meth public abstract void open(boolean) throws java.io.IOException
meth public abstract void read(byte[],int,int) throws java.io.IOException
meth public abstract void resetFile() throws java.io.IOException
meth public abstract void seek(long) throws java.io.IOException

CLSS public org.netbeans.editor.ext.ExtCaret
cons public init()
meth protected void updateMatchBrace()
 anno 0 java.lang.Deprecated()
meth public void requestMatchBraceUpdateSync()
 anno 0 java.lang.Deprecated()
supr org.netbeans.editor.BaseCaret
hfds serialVersionUID

CLSS public org.netbeans.editor.ext.ExtFinderFactory
cons public init()
innr public abstract static LineBlocksFinder
innr public abstract static LineBwdFinder
innr public abstract static LineFwdFinder
supr java.lang.Object

CLSS public abstract static org.netbeans.editor.ext.ExtFinderFactory$LineBlocksFinder
 outer org.netbeans.editor.ext.ExtFinderFactory
cons public init()
meth protected abstract int lineFound(char[],int,int,int,int)
meth public int adjustLimitPos(org.netbeans.editor.BaseDocument,int)
meth public int adjustStartPos(org.netbeans.editor.BaseDocument,int)
meth public int find(int,char[],int,int,int,int)
supr org.netbeans.editor.FinderFactory$AbstractBlocksFinder
hfds origLimitPos,origStartPos

CLSS public abstract static org.netbeans.editor.ext.ExtFinderFactory$LineBwdFinder
 outer org.netbeans.editor.ext.ExtFinderFactory
cons public init()
meth protected abstract int lineFound(char[],int,int,int,int)
meth public int adjustLimitPos(org.netbeans.editor.BaseDocument,int)
meth public int adjustStartPos(org.netbeans.editor.BaseDocument,int)
meth public int find(int,char[],int,int,int,int)
supr org.netbeans.editor.FinderFactory$AbstractFinder
hfds origLimitPos,origStartPos

CLSS public abstract static org.netbeans.editor.ext.ExtFinderFactory$LineFwdFinder
 outer org.netbeans.editor.ext.ExtFinderFactory
cons public init()
meth protected abstract int lineFound(char[],int,int,int,int)
meth public int adjustLimitPos(org.netbeans.editor.BaseDocument,int)
meth public int adjustStartPos(org.netbeans.editor.BaseDocument,int)
meth public int find(int,char[],int,int,int,int)
supr org.netbeans.editor.FinderFactory$AbstractFinder
hfds origLimitPos,origStartPos

CLSS public org.netbeans.editor.ext.ExtFormatSupport
cons public init(org.netbeans.editor.ext.FormatWriter)
meth public boolean isComment(org.netbeans.editor.TokenItem,int)
meth public boolean isComment(org.netbeans.editor.ext.FormatTokenPosition)
meth public boolean isImportant(org.netbeans.editor.TokenItem,int)
meth public boolean isImportant(org.netbeans.editor.ext.FormatTokenPosition)
meth public boolean isLineStart(org.netbeans.editor.ext.FormatTokenPosition)
meth public boolean isNewLine(org.netbeans.editor.ext.FormatTokenPosition)
meth public char getChar(org.netbeans.editor.ext.FormatTokenPosition)
meth public int findLineDistance(org.netbeans.editor.ext.FormatTokenPosition,org.netbeans.editor.ext.FormatTokenPosition)
meth public int getIndex(org.netbeans.editor.TokenItem,org.netbeans.editor.TokenID[])
meth public org.netbeans.editor.TokenItem findAnyToken(org.netbeans.editor.TokenItem,org.netbeans.editor.TokenItem,org.netbeans.editor.TokenID[],org.netbeans.editor.TokenContextPath,boolean)
meth public org.netbeans.editor.TokenItem findImportantToken(org.netbeans.editor.TokenItem,org.netbeans.editor.TokenItem,boolean)
meth public org.netbeans.editor.TokenItem findMatchingToken(org.netbeans.editor.TokenItem,org.netbeans.editor.TokenItem,org.netbeans.editor.ImageTokenID,boolean)
meth public org.netbeans.editor.TokenItem findMatchingToken(org.netbeans.editor.TokenItem,org.netbeans.editor.TokenItem,org.netbeans.editor.TokenID,java.lang.String,boolean)
meth public org.netbeans.editor.TokenItem findToken(org.netbeans.editor.TokenItem,org.netbeans.editor.TokenItem,org.netbeans.editor.TokenID,org.netbeans.editor.TokenContextPath,java.lang.String,boolean)
meth public org.netbeans.editor.TokenItem insertImageToken(org.netbeans.editor.TokenItem,org.netbeans.editor.ImageTokenID,org.netbeans.editor.TokenContextPath)
meth public org.netbeans.editor.ext.FormatTokenPosition findImportant(org.netbeans.editor.ext.FormatTokenPosition,org.netbeans.editor.ext.FormatTokenPosition,boolean,boolean)
meth public org.netbeans.editor.ext.FormatTokenPosition findLineEndNonImportant(org.netbeans.editor.ext.FormatTokenPosition)
meth public org.netbeans.editor.ext.FormatTokenPosition findLineFirstImportant(org.netbeans.editor.ext.FormatTokenPosition)
meth public org.netbeans.editor.ext.FormatTokenPosition removeLineEndWhitespace(org.netbeans.editor.ext.FormatTokenPosition)
supr org.netbeans.editor.ext.FormatSupport

CLSS public org.netbeans.editor.ext.ExtFormatter
 anno 0 java.lang.Deprecated()
cons public init(java.lang.Class)
innr public static Simple
intf org.netbeans.editor.ext.FormatLayer
meth protected boolean acceptSyntax(org.netbeans.editor.Syntax)
meth protected boolean hasTextBefore(javax.swing.text.JTextComponent,java.lang.String)
meth protected int getEOLOffset(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
meth protected void initFormatLayers()
meth public boolean isSimple()
meth public boolean replaceFormatLayer(java.lang.String,org.netbeans.editor.ext.FormatLayer)
meth public int indentLine(javax.swing.text.Document,int)
meth public int indentNewLine(javax.swing.text.Document,int)
meth public int reformat(org.netbeans.editor.BaseDocument,int,int) throws javax.swing.text.BadLocationException
meth public int[] getReformatBlock(javax.swing.text.JTextComponent,java.lang.String)
meth public java.io.Writer createWriter(javax.swing.text.Document,int,java.io.Writer)
meth public java.io.Writer reformat(org.netbeans.editor.BaseDocument,int,int,boolean) throws java.io.IOException,javax.swing.text.BadLocationException
meth public java.lang.Object getSettingValue(java.lang.String)
meth public java.lang.String getName()
meth public java.util.Iterator formatLayerIterator()
meth public void addFormatLayer(org.netbeans.editor.ext.FormatLayer)
meth public void format(org.netbeans.editor.ext.FormatWriter)
meth public void removeFormatLayer(java.lang.String)
meth public void setSettingValue(java.lang.String,java.lang.Object)
supr org.netbeans.editor.Formatter
hfds NULL_VALUE,formatLayerList,indentHotCharsAcceptor,mimeType,prefs,prefsListener,reindentWithTextBefore,settingsMap

CLSS public static org.netbeans.editor.ext.ExtFormatter$Simple
 outer org.netbeans.editor.ext.ExtFormatter
cons public init(java.lang.Class)
meth protected int getEOLOffset(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
meth public boolean isSimple()
supr org.netbeans.editor.ext.ExtFormatter

CLSS public org.netbeans.editor.ext.ExtKit
cons public init()
fld public final static java.lang.String TRIMMED_TEXT = "trimmed-text"
fld public final static java.lang.String allCompletionShowAction = "all-completion-show"
fld public final static java.lang.String buildPopupMenuAction = "build-popup-menu"
fld public final static java.lang.String buildToolTipAction = "build-tool-tip"
fld public final static java.lang.String codeSelectAction = "code-select"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String commentAction = "comment"
fld public final static java.lang.String completionShowAction = "completion-show"
fld public final static java.lang.String completionTooltipShowAction = "tooltip-show"
fld public final static java.lang.String documentationShowAction = "documentation-show"
fld public final static java.lang.String escapeAction = "escape"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String findAction = "find"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String gotoAction = "goto"
fld public final static java.lang.String gotoDeclarationAction = "goto-declaration"
fld public final static java.lang.String gotoHelpAction = "goto-help"
fld public final static java.lang.String gotoSourceAction = "goto-source"
fld public final static java.lang.String gotoSuperImplementationAction = "goto-super-implementation"
fld public final static java.lang.String matchBraceAction = "match-brace"
fld public final static java.lang.String replaceAction = "replace"
fld public final static java.lang.String selectionMatchBraceAction = "selection-match-brace"
fld public final static java.lang.String showPopupMenuAction = "show-popup-menu"
fld public final static java.lang.String toggleCaseIdentifierBeginAction = "toggle-case-identifier-begin"
fld public final static java.lang.String toggleCommentAction = "toggle-comment"
fld public final static java.lang.String toggleToolbarAction = "toggle-toolbar"
fld public final static java.lang.String uncommentAction = "uncomment"
innr public static AllCompletionShowAction
innr public static BuildPopupMenuAction
innr public static BuildToolTipAction
innr public static CodeSelectAction
innr public static CommentAction
innr public static CompletionShowAction
innr public static CompletionTooltipShowAction
innr public static DocumentationShowAction
innr public static EscapeAction
innr public static ExtDefaultKeyTypedAction
innr public static ExtDeleteCharAction
innr public static GotoAction
innr public static GotoDeclarationAction
innr public static MatchBraceAction
innr public static PrefixMakerAction
innr public static ShowPopupMenuAction
innr public static ToggleCaseIdentifierBeginAction
innr public static ToggleCommentAction
innr public static UncommentAction
meth protected javax.swing.Action[] createActions()
meth protected org.netbeans.editor.EditorUI createEditorUI()
meth public org.netbeans.editor.SyntaxSupport createSyntaxSupport(org.netbeans.editor.BaseDocument)
supr org.netbeans.editor.BaseKit
hfds debugPopupMenu,editorBundleHash,noExtEditorUIClass
hcls BaseKitLocalizedAction

CLSS public static org.netbeans.editor.ext.ExtKit$AllCompletionShowAction
 outer org.netbeans.editor.ext.ExtKit
 anno 0 java.lang.Deprecated()
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction

CLSS public static org.netbeans.editor.ext.ExtKit$BuildPopupMenuAction
 outer org.netbeans.editor.ext.ExtKit
cons public init()
cons public init(java.util.Map)
meth protected final void debugPopupMenuItem(javax.swing.JMenuItem,javax.swing.Action)
meth protected java.lang.Class getShortDescriptionBundleClass()
meth protected java.lang.String getItemText(javax.swing.text.JTextComponent,java.lang.String,javax.swing.Action)
meth protected javax.swing.JPopupMenu buildPopupMenu(javax.swing.text.JTextComponent)
meth protected javax.swing.JPopupMenu createPopupMenu(javax.swing.text.JTextComponent)
meth protected void addAction(javax.swing.text.JTextComponent,javax.swing.JPopupMenu,java.lang.String)
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ext.ExtKit$BuildToolTipAction
 outer org.netbeans.editor.ext.ExtKit
cons public init()
cons public init(java.util.Map)
meth protected java.lang.String buildText(javax.swing.text.JTextComponent)
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ext.ExtKit$CodeSelectAction
 outer org.netbeans.editor.ext.ExtKit
 anno 0 java.lang.Deprecated()
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ext.ExtKit$CommentAction
 outer org.netbeans.editor.ext.ExtKit
cons public init(java.lang.String)
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds delegateAction,serialVersionUID

CLSS public static org.netbeans.editor.ext.ExtKit$CompletionShowAction
 outer org.netbeans.editor.ext.ExtKit
 anno 0 java.lang.Deprecated()
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ext.ExtKit$CompletionTooltipShowAction
 outer org.netbeans.editor.ext.ExtKit
 anno 0 java.lang.Deprecated()
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction

CLSS public static org.netbeans.editor.ext.ExtKit$DocumentationShowAction
 outer org.netbeans.editor.ext.ExtKit
 anno 0 java.lang.Deprecated()
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction

CLSS public static org.netbeans.editor.ext.ExtKit$EscapeAction
 outer org.netbeans.editor.ext.ExtKit
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction

CLSS public static org.netbeans.editor.ext.ExtKit$ExtDefaultKeyTypedAction
 outer org.netbeans.editor.ext.ExtKit
 anno 0 java.lang.Deprecated()
cons public init()
meth protected void checkCompletion(javax.swing.text.JTextComponent,java.lang.String)
meth protected void checkIndentHotChars(javax.swing.text.JTextComponent,java.lang.String)
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseKit$DefaultKeyTypedAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ext.ExtKit$ExtDeleteCharAction
 outer org.netbeans.editor.ext.ExtKit
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,boolean)
supr org.netbeans.editor.BaseKit$DeleteCharAction

CLSS public static org.netbeans.editor.ext.ExtKit$GotoAction
 outer org.netbeans.editor.ext.ExtKit
cons public init()
meth protected int getOffsetFromLine(org.netbeans.editor.BaseDocument,int)
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ext.ExtKit$GotoDeclarationAction
 outer org.netbeans.editor.ext.ExtKit
 anno 0 java.lang.Deprecated()
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public boolean gotoDeclaration(javax.swing.text.JTextComponent)
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ext.ExtKit$MatchBraceAction
 outer org.netbeans.editor.ext.ExtKit
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,boolean)
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ext.ExtKit$PrefixMakerAction
 outer org.netbeans.editor.ext.ExtKit
cons public init(java.lang.String,java.lang.String,java.lang.String[])
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds prefix,prefixGroup,serialVersionUID

CLSS public static org.netbeans.editor.ext.ExtKit$ShowPopupMenuAction
 outer org.netbeans.editor.ext.ExtKit
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ext.ExtKit$ToggleCaseIdentifierBeginAction
 outer org.netbeans.editor.ext.ExtKit
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ext.ExtKit$ToggleCommentAction
 outer org.netbeans.editor.ext.ExtKit
cons public init(java.lang.String)
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds lineCommentString,lineCommentStringLen,serialVersionUID

CLSS public static org.netbeans.editor.ext.ExtKit$UncommentAction
 outer org.netbeans.editor.ext.ExtKit
cons public init(java.lang.String)
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds delegateAction,serialVersionUID

CLSS public org.netbeans.editor.ext.ExtSyntaxSupport
cons public init(org.netbeans.editor.BaseDocument)
fld public final static int COMPLETION_CANCEL = 1
fld public final static int COMPLETION_HIDE = 4
fld public final static int COMPLETION_POPUP = 0
fld public final static int COMPLETION_POST_REFRESH = 3
fld public final static int COMPLETION_REFRESH = 2
innr public BracketFinder
innr public abstract interface static DeclarationTokenProcessor
innr public abstract interface static VariableMapTokenProcessor
meth protected int getMethodStartPosition(int)
meth protected java.util.Map buildGlobalVariableMap(int)
meth protected java.util.Map buildLocalVariableMap(int)
meth protected org.netbeans.editor.TokenID[] getBracketSkipTokens()
meth protected org.netbeans.editor.ext.ExtSyntaxSupport$BracketFinder getMatchingBracketFinder(char)
meth protected org.netbeans.editor.ext.ExtSyntaxSupport$DeclarationTokenProcessor createDeclarationTokenProcessor(java.lang.String,int,int)
meth protected org.netbeans.editor.ext.ExtSyntaxSupport$VariableMapTokenProcessor createVariableMapTokenProcessor(int,int)
meth protected void documentModified(javax.swing.event.DocumentEvent)
meth public boolean isCommentOrWhitespace(int,int) throws javax.swing.text.BadLocationException
meth public boolean isRowValid(int) throws javax.swing.text.BadLocationException
meth public boolean isWhitespaceToken(org.netbeans.editor.TokenID,char[],int,int)
meth public int checkCompletion(javax.swing.text.JTextComponent,java.lang.String,boolean)
meth public int findDeclarationPosition(java.lang.String,int)
meth public int findGlobalDeclarationPosition(java.lang.String,int)
meth public int findLocalDeclarationPosition(java.lang.String,int)
meth public int getRowLastValidChar(int) throws javax.swing.text.BadLocationException
meth public int[] findMatchingBlock(int,boolean) throws javax.swing.text.BadLocationException
meth public int[] getCommentBlocks(int,int) throws javax.swing.text.BadLocationException
meth public int[] getFunctionBlock(int) throws javax.swing.text.BadLocationException
meth public int[] getFunctionBlock(int[]) throws javax.swing.text.BadLocationException
meth public java.lang.Object findType(java.lang.String,int)
meth public java.util.Map getGlobalVariableMap(int)
meth public java.util.Map getLocalVariableMap(int)
meth public org.netbeans.editor.TokenID getTokenID(int) throws javax.swing.text.BadLocationException
meth public org.netbeans.editor.TokenID[] getCommentTokens()
meth public org.netbeans.editor.TokenItem getTokenChain(int,int) throws javax.swing.text.BadLocationException
supr org.netbeans.editor.SyntaxSupport
hfds EMPTY_TOKEN_ID_ARRAY,docL,globalVarMaps,localVarMaps
hcls CommentOrWhitespaceTP,FirstTokenTP,TokenItemTP

CLSS public org.netbeans.editor.ext.ExtSyntaxSupport$BracketFinder
 outer org.netbeans.editor.ext.ExtSyntaxSupport
cons public init(org.netbeans.editor.ext.ExtSyntaxSupport,char)
fld protected char bracketChar
fld protected char matchChar
fld protected int moveCount
meth protected boolean updateStatus()
meth protected int scan(char,boolean)
supr org.netbeans.editor.FinderFactory$GenericFinder
hfds depth

CLSS public abstract interface static org.netbeans.editor.ext.ExtSyntaxSupport$DeclarationTokenProcessor
 outer org.netbeans.editor.ext.ExtSyntaxSupport
intf org.netbeans.editor.TokenProcessor
meth public abstract int getDeclarationPosition()

CLSS public abstract interface static org.netbeans.editor.ext.ExtSyntaxSupport$VariableMapTokenProcessor
 outer org.netbeans.editor.ext.ExtSyntaxSupport
intf org.netbeans.editor.TokenProcessor
meth public abstract java.util.Map getVariableMap()

CLSS public org.netbeans.editor.ext.FileAccessor
cons public init(java.io.File)
intf org.netbeans.editor.ext.DataAccessor
meth public int getFileLength()
meth public java.lang.String toString()
meth public long getFilePointer() throws java.io.IOException
meth public void append(byte[],int,int) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void open(boolean) throws java.io.IOException
meth public void read(byte[],int,int) throws java.io.IOException
meth public void resetFile() throws java.io.IOException
meth public void seek(long) throws java.io.IOException
supr java.lang.Object
hfds f,file

CLSS public org.netbeans.editor.ext.FileStorage
cons public init(java.lang.String)
cons public init(java.lang.String,org.netbeans.editor.ext.StringCache)
cons public init(org.netbeans.editor.ext.DataAccessor,org.netbeans.editor.ext.StringCache)
fld protected boolean opened
fld protected boolean openedForWrite
fld protected byte[] bytes
fld protected int offset
fld protected org.netbeans.editor.ext.DataAccessor da
fld public boolean fileNotFound
meth protected void checkBytesSize(int)
meth public final void lockFile()
meth public final void unlockFile()
meth public int getFileLength() throws java.io.IOException
meth public int getFilePointer() throws java.io.IOException
meth public int getInteger()
meth public int getOffset()
meth public java.lang.String getFileName()
meth public java.lang.String getString()
meth public java.lang.String toString()
meth public void close() throws java.io.IOException
meth public void open(boolean) throws java.io.IOException
meth public void putInteger(int)
meth public void putString(java.lang.String)
meth public void read(int) throws java.io.IOException
meth public void resetBytes()
meth public void resetFile() throws java.io.IOException
meth public void seek(int) throws java.io.IOException
meth public void setOffset(int)
meth public void setVersion(int)
meth public void write() throws java.io.IOException
supr java.lang.Object
hfds BIT5,BIT6,BIT7,BYTES_INCREMENT,EMPTY_BYTES,MAX_STRING,WRITE_LOCK_MISSING,chars,currentLock,lockDeep,strCache,version

CLSS public abstract interface org.netbeans.editor.ext.FormatLayer
meth public abstract java.lang.String getName()
meth public abstract void format(org.netbeans.editor.ext.FormatWriter)

CLSS public org.netbeans.editor.ext.FormatSupport
cons public init(org.netbeans.editor.ext.FormatWriter)
meth public boolean canInsertToken(org.netbeans.editor.TokenItem)
meth public boolean canModifyWhitespace(org.netbeans.editor.TokenItem)
meth public boolean canRemoveToken(org.netbeans.editor.TokenItem)
meth public boolean canReplaceToken(org.netbeans.editor.TokenItem)
meth public boolean expandTabs()
meth public boolean getSettingBoolean(java.lang.String,boolean)
meth public boolean getSettingBoolean(java.lang.String,java.lang.Boolean)
meth public boolean isAfter(org.netbeans.editor.TokenItem,org.netbeans.editor.TokenItem)
meth public boolean isAfter(org.netbeans.editor.ext.FormatTokenPosition,org.netbeans.editor.ext.FormatTokenPosition)
meth public boolean isChainStartPosition(org.netbeans.editor.ext.FormatTokenPosition)
meth public boolean isLineEmpty(org.netbeans.editor.ext.FormatTokenPosition)
meth public boolean isLineWhite(org.netbeans.editor.ext.FormatTokenPosition)
meth public boolean isRestartFormat()
meth public boolean isWhitespace(org.netbeans.editor.TokenItem,int)
meth public boolean isWhitespace(org.netbeans.editor.ext.FormatTokenPosition)
meth public boolean tokenEquals(org.netbeans.editor.TokenItem,org.netbeans.editor.TokenID)
meth public boolean tokenEquals(org.netbeans.editor.TokenItem,org.netbeans.editor.TokenID,org.netbeans.editor.TokenContextPath)
meth public boolean tokenEquals(org.netbeans.editor.TokenItem,org.netbeans.editor.TokenID,org.netbeans.editor.TokenContextPath,java.lang.String)
meth public final boolean isIndentOnly()
meth public int getIndentShift()
meth public int getLineIndent(org.netbeans.editor.ext.FormatTokenPosition,boolean)
meth public int getSettingInteger(java.lang.String,int)
meth public int getSettingInteger(java.lang.String,java.lang.Integer)
meth public int getShiftWidth()
meth public int getSpacesPerTab()
meth public int getTabSize()
meth public int getVisualColumnOffset(org.netbeans.editor.ext.FormatTokenPosition)
meth public java.lang.Object getSettingValue(java.lang.String)
meth public java.lang.Object getSettingValue(java.lang.String,java.lang.Object)
meth public java.lang.String chainToString(org.netbeans.editor.TokenItem)
meth public java.lang.String chainToString(org.netbeans.editor.TokenItem,int)
meth public java.lang.String getIndentString(int)
meth public org.netbeans.editor.TokenContextPath getValidWhitespaceTokenContextPath()
meth public org.netbeans.editor.TokenContextPath getWhitespaceTokenContextPath()
meth public org.netbeans.editor.TokenID getValidWhitespaceTokenID()
meth public org.netbeans.editor.TokenID getWhitespaceTokenID()
meth public org.netbeans.editor.TokenItem findFirstToken(org.netbeans.editor.TokenItem)
meth public org.netbeans.editor.TokenItem findNonEmptyToken(org.netbeans.editor.TokenItem,boolean)
meth public org.netbeans.editor.TokenItem getLastToken()
meth public org.netbeans.editor.TokenItem getPreviousToken(org.netbeans.editor.TokenItem)
meth public org.netbeans.editor.TokenItem insertToken(org.netbeans.editor.TokenItem,org.netbeans.editor.TokenID,org.netbeans.editor.TokenContextPath,java.lang.String)
meth public org.netbeans.editor.TokenItem splitEnd(org.netbeans.editor.TokenItem,int,org.netbeans.editor.TokenID,org.netbeans.editor.TokenContextPath)
meth public org.netbeans.editor.TokenItem splitStart(org.netbeans.editor.TokenItem,int,org.netbeans.editor.TokenID,org.netbeans.editor.TokenContextPath)
meth public org.netbeans.editor.ext.FormatTokenPosition changeLineIndent(org.netbeans.editor.ext.FormatTokenPosition,int)
meth public org.netbeans.editor.ext.FormatTokenPosition findLineEnd(org.netbeans.editor.ext.FormatTokenPosition)
meth public org.netbeans.editor.ext.FormatTokenPosition findLineEndWhitespace(org.netbeans.editor.ext.FormatTokenPosition)
meth public org.netbeans.editor.ext.FormatTokenPosition findLineFirstNonWhitespace(org.netbeans.editor.ext.FormatTokenPosition)
meth public org.netbeans.editor.ext.FormatTokenPosition findLineStart(org.netbeans.editor.ext.FormatTokenPosition)
meth public org.netbeans.editor.ext.FormatTokenPosition findNextEOL(org.netbeans.editor.ext.FormatTokenPosition)
meth public org.netbeans.editor.ext.FormatTokenPosition findNonWhitespace(org.netbeans.editor.ext.FormatTokenPosition,org.netbeans.editor.ext.FormatTokenPosition,boolean,boolean)
meth public org.netbeans.editor.ext.FormatTokenPosition findPreviousEOL(org.netbeans.editor.ext.FormatTokenPosition)
meth public org.netbeans.editor.ext.FormatTokenPosition getFormatStartPosition()
meth public org.netbeans.editor.ext.FormatTokenPosition getLastPosition()
meth public org.netbeans.editor.ext.FormatTokenPosition getNextPosition(org.netbeans.editor.TokenItem,int)
meth public org.netbeans.editor.ext.FormatTokenPosition getNextPosition(org.netbeans.editor.TokenItem,int,javax.swing.text.Position$Bias)
meth public org.netbeans.editor.ext.FormatTokenPosition getNextPosition(org.netbeans.editor.ext.FormatTokenPosition)
meth public org.netbeans.editor.ext.FormatTokenPosition getNextPosition(org.netbeans.editor.ext.FormatTokenPosition,javax.swing.text.Position$Bias)
meth public org.netbeans.editor.ext.FormatTokenPosition getPosition(org.netbeans.editor.TokenItem,int)
meth public org.netbeans.editor.ext.FormatTokenPosition getPosition(org.netbeans.editor.TokenItem,int,javax.swing.text.Position$Bias)
meth public org.netbeans.editor.ext.FormatTokenPosition getPreviousPosition(org.netbeans.editor.TokenItem,int)
meth public org.netbeans.editor.ext.FormatTokenPosition getPreviousPosition(org.netbeans.editor.TokenItem,int,javax.swing.text.Position$Bias)
meth public org.netbeans.editor.ext.FormatTokenPosition getPreviousPosition(org.netbeans.editor.ext.FormatTokenPosition)
meth public org.netbeans.editor.ext.FormatTokenPosition getPreviousPosition(org.netbeans.editor.ext.FormatTokenPosition,javax.swing.text.Position$Bias)
meth public org.netbeans.editor.ext.FormatTokenPosition getTextStartPosition()
meth public org.netbeans.editor.ext.FormatWriter getFormatWriter()
meth public void insertSpaces(org.netbeans.editor.TokenItem,int)
meth public void insertString(org.netbeans.editor.TokenItem,int,java.lang.String)
meth public void insertString(org.netbeans.editor.ext.FormatTokenPosition,java.lang.String)
meth public void remove(org.netbeans.editor.TokenItem,int,int)
meth public void remove(org.netbeans.editor.ext.FormatTokenPosition,int)
meth public void removeToken(org.netbeans.editor.TokenItem)
meth public void removeTokenChain(org.netbeans.editor.TokenItem,org.netbeans.editor.TokenItem)
meth public void replaceToken(org.netbeans.editor.TokenItem,org.netbeans.editor.TokenID,org.netbeans.editor.TokenContextPath,java.lang.String)
meth public void setIndentShift(int)
meth public void setRestartFormat(boolean)
supr java.lang.Object
hfds formatWriter

CLSS public abstract interface org.netbeans.editor.ext.FormatTokenPosition
meth public abstract int getOffset()
meth public abstract javax.swing.text.Position$Bias getBias()
meth public abstract org.netbeans.editor.TokenItem getToken()

CLSS public final org.netbeans.editor.ext.FormatWriter
fld public final static boolean debug
fld public final static boolean debugModify
meth public boolean canInsertToken(org.netbeans.editor.TokenItem)
meth public boolean canModifyToken(org.netbeans.editor.TokenItem,int)
meth public boolean canRemoveToken(org.netbeans.editor.TokenItem)
meth public boolean canSplitEnd(org.netbeans.editor.TokenItem,int)
meth public boolean canSplitStart(org.netbeans.editor.TokenItem,int)
meth public boolean isAfter(org.netbeans.editor.TokenItem,org.netbeans.editor.TokenItem)
meth public boolean isAfter(org.netbeans.editor.ext.FormatTokenPosition,org.netbeans.editor.ext.FormatTokenPosition)
meth public boolean isChainModified()
meth public boolean isChainStartPosition(org.netbeans.editor.ext.FormatTokenPosition)
meth public boolean isRestartFormat()
meth public final boolean isIndentOnly()
meth public final int getOffset()
meth public final javax.swing.text.Document getDocument()
meth public final org.netbeans.editor.ext.ExtFormatter getFormatter()
meth public int getIndentShift()
meth public java.lang.String chainToString(org.netbeans.editor.TokenItem)
meth public java.lang.String chainToString(org.netbeans.editor.TokenItem,int)
meth public org.netbeans.editor.TokenItem findFirstToken(org.netbeans.editor.TokenItem)
meth public org.netbeans.editor.TokenItem findNonEmptyToken(org.netbeans.editor.TokenItem,boolean)
meth public org.netbeans.editor.TokenItem getLastToken()
meth public org.netbeans.editor.TokenItem insertToken(org.netbeans.editor.TokenItem,org.netbeans.editor.TokenID,org.netbeans.editor.TokenContextPath,java.lang.String)
meth public org.netbeans.editor.TokenItem splitEnd(org.netbeans.editor.TokenItem,int,org.netbeans.editor.TokenID,org.netbeans.editor.TokenContextPath)
meth public org.netbeans.editor.TokenItem splitStart(org.netbeans.editor.TokenItem,int,org.netbeans.editor.TokenID,org.netbeans.editor.TokenContextPath)
meth public org.netbeans.editor.ext.FormatTokenPosition getFormatStartPosition()
meth public org.netbeans.editor.ext.FormatTokenPosition getPosition(org.netbeans.editor.TokenItem,int,javax.swing.text.Position$Bias)
meth public org.netbeans.editor.ext.FormatTokenPosition getTextStartPosition()
meth public void checkChain()
meth public void close() throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void insertString(org.netbeans.editor.TokenItem,int,java.lang.String)
meth public void remove(org.netbeans.editor.TokenItem,int,int)
meth public void removeToken(org.netbeans.editor.TokenItem)
meth public void setChainModified(boolean)
meth public void setIndentOnly(boolean)
meth public void setIndentShift(int)
meth public void setRestartFormat(boolean)
meth public void write(char[],int,int) throws java.io.IOException
meth public void write(char[],int,int,int[],javax.swing.text.Position$Bias[]) throws java.io.IOException
supr java.io.Writer
hfds EMPTY_BUFFER,buffer,bufferSize,chainModified,doc,firstFlush,formatStartPosition,formatter,ftps,indentOnly,indentShift,lastFlush,lastToken,offset,offsetPreScan,reformatting,restartFormat,simple,syntax,textStartPosition,underWriter
hcls ExtTokenItem,FilterDocumentItem,FormatTokenItem

CLSS public org.netbeans.editor.ext.GotoDialogPanel
cons public init()
fld protected javax.swing.JComboBox gotoCombo
fld protected javax.swing.JLabel gotoLabel
intf java.awt.event.FocusListener
meth protected void updateCombo(java.util.List)
meth public java.lang.String getValue()
meth public javax.swing.JComboBox getGotoCombo()
meth public void focusGained(java.awt.event.FocusEvent)
meth public void focusLost(java.awt.event.FocusEvent)
meth public void popupNotify(org.netbeans.editor.ext.KeyEventBlocker)
meth public void updateHistory()
supr javax.swing.JPanel
hfds HISTORY_KEY,MAX_ITEMS,blocker,bundle,dontFire,serialVersionUID

CLSS public org.netbeans.editor.ext.GotoDialogSupport
cons public init()
intf java.awt.event.ActionListener
meth protected boolean performGoto()
meth protected final java.lang.String getGotoValueText()
meth protected java.awt.Dialog createGotoDialog()
meth protected void disposeGotoDialog()
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void showGotoDialog(org.netbeans.editor.ext.KeyEventBlocker)
supr java.lang.Object
hfds BOUNDS_KEY,gotoButtons,gotoDialog,gotoPanel

CLSS public org.netbeans.editor.ext.KeyEventBlocker
cons public init(javax.swing.text.JTextComponent,boolean)
intf java.awt.event.KeyListener
meth public void keyPressed(java.awt.event.KeyEvent)
meth public void keyReleased(java.awt.event.KeyEvent)
meth public void keyTyped(java.awt.event.KeyEvent)
meth public void stopBlocking()
meth public void stopBlocking(boolean)
supr java.lang.Object
hfds blockedEvents,component,debugBlockEvent,discardKeyTyped

CLSS public org.netbeans.editor.ext.KeywordMatchGenerator
cons public init()
fld public final static java.lang.String IGNORE_CASE = "-i"
fld public final static java.lang.String USE_STRING = "-s"
meth protected boolean upperCaseKeyConstants()
meth protected java.lang.String getCurrentChar()
meth protected java.lang.String getKwdConstant(java.lang.String)
meth protected java.lang.String getKwdConstantPrefix()
meth protected void appendString(java.lang.String)
meth protected void finishScan()
meth protected void initScan(java.lang.String)
meth public static void main(java.lang.String[])
meth public void addOption(java.lang.String)
meth public void scan()
supr java.lang.Object
hfds DEFAULT_METHOD_NAME,OPTION_LIST,UNKNOWN_OPTION,USAGE,kwdConstants,kwds,maxKwdLen,options

CLSS public org.netbeans.editor.ext.MultiSyntax
cons public init()
innr public static MultiStateInfo
meth protected void registerSyntax(org.netbeans.editor.Syntax)
meth public int compareState(org.netbeans.editor.Syntax$StateInfo)
meth public org.netbeans.editor.Syntax$StateInfo createStateInfo()
meth public void load(org.netbeans.editor.Syntax$StateInfo,char[],int,int,boolean,int)
meth public void loadInitState()
meth public void storeState(org.netbeans.editor.Syntax$StateInfo)
supr org.netbeans.editor.Syntax
hfds slaveSyntaxChain,slaveSyntaxChainEnd
hcls SyntaxInfo

CLSS public static org.netbeans.editor.ext.MultiSyntax$MultiStateInfo
 outer org.netbeans.editor.ext.MultiSyntax
cons public init()
supr org.netbeans.editor.Syntax$BaseStateInfo
hfds stateInfoChain
hcls ChainItem

CLSS public org.netbeans.editor.ext.StringCache
cons public init()
cons public init(int)
cons public init(int,int)
fld public int statHits
fld public int statQueries
meth public java.lang.String getString(char[],int,int)
meth public java.lang.String toString()
meth public void putSurviveString(java.lang.String)
supr java.lang.Object
hfds DEFAULT_INITIAL_CAPACITY,DEFAULT_MAX_SIZE,chain,endChain,freeEntry,maxSize,size,strMap
hcls Entry

CLSS public org.netbeans.editor.ext.ToolTipSupport
fld public final static int DISMISS_DELAY = 60000
fld public final static int FLAGS_HEAVYWEIGHT_TOOLTIP = 4
fld public final static int FLAGS_LIGHTWEIGHT_TOOLTIP = 3
fld public final static int FLAG_HIDE_ON_MOUSE_MOVE = 1
fld public final static int FLAG_HIDE_ON_TIMER = 2
fld public final static int FLAG_PERMANENT = 4
fld public final static int INITIAL_DELAY = 200
fld public final static int STATUS_COMPONENT_VISIBLE = 3
fld public final static int STATUS_HIDDEN = 0
fld public final static int STATUS_TEXT_VISIBLE = 2
fld public final static int STATUS_VISIBILITY_ENABLED = 1
fld public final static java.lang.String PROP_DISMISS_DELAY = "dismissDelay"
fld public final static java.lang.String PROP_ENABLED = "enabled"
fld public final static java.lang.String PROP_INITIAL_DELAY = "initialDelay"
fld public final static java.lang.String PROP_STATUS = "status"
fld public final static java.lang.String PROP_TOOL_TIP = "toolTip"
fld public final static java.lang.String PROP_TOOL_TIP_TEXT = "toolTipText"
meth protected javax.swing.JComponent createDefaultToolTip()
meth protected void componentToolTipTextChanged(java.beans.PropertyChangeEvent)
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void updateToolTip()
meth public boolean isEnabled()
meth public boolean isToolTipVisible()
meth public final int getStatus()
meth public final java.awt.event.MouseEvent getLastMouseEvent()
meth public final javax.swing.JComponent getToolTip()
meth public final void setToolTipVisible(boolean,boolean)
meth public int getDismissDelay()
meth public int getInitialDelay()
meth public java.lang.String getIdentifierUnderCursor()
meth public java.lang.String getToolTipText()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setDismissDelay(int)
meth public void setEnabled(boolean)
meth public void setInitialDelay(int)
meth public void setToolTip(javax.swing.JComponent)
meth public void setToolTip(javax.swing.JComponent,org.netbeans.editor.PopupManager$HorizontalBounds,java.awt.Point,int,int,int)
meth public void setToolTip(javax.swing.JComponent,org.netbeans.editor.PopupManager$HorizontalBounds,org.netbeans.editor.PopupManager$Placement)
meth public void setToolTip(javax.swing.JComponent,org.netbeans.editor.PopupManager$HorizontalBounds,org.netbeans.editor.PopupManager$Placement,int,int)
meth public void setToolTip(javax.swing.JComponent,org.netbeans.editor.PopupManager$HorizontalBounds,org.netbeans.editor.PopupManager$Placement,int,int,int)
meth public void setToolTipText(java.lang.String)
meth public void setToolTipVisible(boolean)
supr java.lang.Object
hfds ELIPSIS,HIDE_ACTION,HTML_PREFIX_LOWERCASE,HTML_PREFIX_UPPERCASE,LAST_TOOLTIP_POSITION,LOG,MOUSE_EXTRA_HEIGHT,MOUSE_LISTENER,MOUSE_MOVE_IGNORED_AREA,NO_ACTION,NO_OP_MOUSE_LISTENER,SUPPRESS_POPUP_KEYBOARD_FORWARDING_CLIENT_PROPERTY_KEY,UI_PREFIX,enabled,enterTimer,exitTimer,extEditorUI,flags,glyphListenerAdded,horizontalAdjustment,horizontalBounds,lastMouseEvent,listener,pcs,placement,status,toolTip,toolTipText,tooltipFromView,verticalAdjustment
hcls Accessor,Listener

CLSS public org.netbeans.modules.editor.EditorModule
cons public init()
meth public void restored()
meth public void uninstalled()
supr org.openide.modules.ModuleInstall
hfds LOG,debug,topComponentRegistryListener
hcls DebugHashtable,HackMap

CLSS public org.netbeans.modules.editor.EditorWarmUpTask
cons public init()
intf java.lang.Runnable
meth public void run()
supr java.lang.Object

CLSS public org.netbeans.modules.editor.ExportHtmlAction
cons public init()
meth protected final boolean asynchronous()
meth protected final int mode()
meth protected final java.lang.Class[] cookieClasses()
meth protected final void performAction(org.openide.nodes.Node[])
meth public final java.lang.String getName()
meth public final org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CookieAction
hfds CHARSET,FOLDER_NAME_HIST,HTML_EXT,OPEN_HTML_HIST,SELECTION_HIST,SHOW_LINES_HIST,dlg
hcls HtmlOrDirFilter,Presenter

CLSS public abstract org.netbeans.modules.editor.FormatterIndentEngine
 anno 0 java.lang.Deprecated()
cons public init()
fld public final static java.lang.String EXPAND_TABS_PROP = "expandTabs"
fld public final static java.lang.String SPACES_PER_TAB_PROP = "spacesPerTab"
meth protected abstract org.netbeans.editor.ext.ExtFormatter createFormatter()
meth protected boolean acceptMimeType(java.lang.String)
meth public boolean isExpandTabs()
meth public int getSpacesPerTab()
meth public int indentLine(javax.swing.text.Document,int)
meth public int indentNewLine(javax.swing.text.Document,int)
meth public java.io.Writer createWriter(javax.swing.text.Document,int,java.io.Writer)
meth public java.lang.Object getValue(java.lang.String)
meth public java.lang.String[] getAcceptedMimeTypes()
meth public org.netbeans.editor.ext.ExtFormatter getFormatter()
meth public void setAcceptedMimeTypes(java.lang.String[])
meth public void setExpandTabs(boolean)
meth public void setSpacesPerTab(int)
meth public void setValue(java.lang.String,java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public void setValue(java.lang.String,java.lang.Object,java.lang.String)
supr org.openide.text.IndentEngine
hfds acceptedMimeTypes,formatter,serialPersistentFields,serialVersionUID

CLSS public abstract org.netbeans.modules.editor.FormatterIndentEngineBeanInfo
cons public init()
cons public init(java.lang.String)
meth protected abstract java.lang.Class getBeanClass()
meth protected java.beans.PropertyDescriptor createPropertyDescriptor(java.lang.String)
meth protected java.beans.PropertyDescriptor getPropertyDescriptor(java.lang.String)
meth protected java.lang.String getString(java.lang.String)
meth protected java.lang.String[] createPropertyNames()
meth protected java.lang.String[] getPropertyNames()
meth protected void setExpert(java.lang.String[])
meth protected void setHidden(java.lang.String[])
meth protected void setPropertyEditor(java.lang.String,java.lang.Class)
meth protected void updatePropertyDescriptors()
meth public java.awt.Image getIcon(int)
meth public java.beans.PropertyDescriptor[] getPropertyDescriptors()
supr java.beans.SimpleBeanInfo
hfds icon,icon32,iconPrefix,propertyDescriptors,propertyNames

CLSS public org.netbeans.modules.editor.HtmlPrintContainer
cons public init()
intf org.netbeans.editor.PrintContainer
meth public final boolean initEmptyLines()
meth public final java.lang.String end()
meth public final void add(char[],java.awt.Font,java.awt.Color,java.awt.Color)
meth public final void begin(org.openide.filesystems.FileObject,java.awt.Font,java.awt.Color,java.awt.Color,java.awt.Color,java.awt.Color,java.lang.Class,java.lang.String)
meth public final void eol()
meth public void addLines(java.util.List<java.text.AttributedCharacterIterator>)
supr java.lang.Object
hfds DOCTYPE,DOT,EOL,ESC_AMP,ESC_APOS,ESC_GT,ESC_LT,ESC_QUOT,FF_MONOSPACE,FF_SANSSERIF,FF_SERIF,STYLE_PREFIX,ST_BEGIN,ST_BGCOLOR,ST_BODY,ST_BOLD,ST_COLOR,ST_END,ST_FONT_FAMILY,ST_ITALIC,ST_PRE,ST_SEPARATOR,ST_SIZE,ST_TABLE,T_BLOCK_E,T_BLOCK_S,T_BODY_E,T_BODY_S,T_CHARSET,T_COMMENT_E,T_COMMENT_S,T_HEAD_E,T_HEAD_S,T_HTML_E,T_HTML_S,T_NAME_TABLE,T_PRE_E,T_PRE_S,T_STYLE_E,T_STYLE_S,T_TITLE,WS,ZERO,boolHolder,buffer,charset,defaultBackgroundColor,defaultFont,defaultForegroundColor,fileName,headerBackgroundColor,headerForegroundColor,shortFileName,styles,syntaxColoring
hcls Styles

CLSS public org.netbeans.modules.editor.IndentEngineFormatter
cons public init(java.lang.Class,org.openide.text.IndentEngine)
meth public int indentLine(javax.swing.text.Document,int)
meth public int indentNewLine(javax.swing.text.Document,int)
meth public java.io.Writer createWriter(javax.swing.text.Document,int,java.io.Writer)
meth public org.openide.text.IndentEngine getIndentEngine()
supr org.netbeans.editor.Formatter
hfds indentEngine

CLSS public abstract org.netbeans.modules.editor.MainMenuAction
cons public init()
cons public init(boolean,javax.swing.Icon)
fld public boolean menuInitialized
fld public final static javax.swing.Icon BLANK_ICON
innr public final static CommentAction
innr public final static FindNextAction
innr public final static FindPreviousAction
innr public final static FindSelectionAction
innr public final static FormatAction
innr public final static JumpBackAction
innr public final static JumpForwardAction
innr public final static PasteFormattedAction
innr public final static RemoveTrailingSpacesAction
innr public final static SelectAllAction
innr public final static SelectIdentifierAction
innr public final static ShiftLineLeftAction
innr public final static ShiftLineRightAction
innr public final static StartMacroRecordingAction
innr public final static StopMacroRecordingAction
innr public final static ToggleCommentAction
innr public final static UncommentAction
innr public final static WordMatchNextAction
innr public final static WordMatchPrevAction
innr public static GoToDeclarationAction
innr public static GoToSourceAction
innr public static GoToSuperAction
innr public static ShowLineNumbersAction
innr public static ShowToolBarAction
intf javax.swing.event.ChangeListener
intf org.openide.util.LookupListener
intf org.openide.util.actions.Presenter$Menu
meth protected abstract java.lang.String getActionName()
meth protected abstract java.lang.String getMenuItemText()
meth protected final javax.swing.ActionMap getContextActionMap()
meth protected final void postSetMenu()
meth protected javax.swing.Action getGlobalKitAction()
meth protected javax.swing.KeyStroke getDefaultAccelerator()
meth protected static javax.swing.Action getActionByName(java.lang.String)
meth protected static void addAccelerators(javax.swing.Action,javax.swing.JMenuItem,javax.swing.text.JTextComponent)
meth protected void setMenu()
meth public boolean isEnabled()
meth public java.lang.String getName()
meth public javax.swing.JMenuItem getMenuPresenter()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void resultChanged(org.openide.util.LookupEvent)
meth public void stateChanged(javax.swing.event.ChangeEvent)
supr java.lang.Object
hfds IS_SET_POST_SET_MENU_LISTENER,RP,forceIcon,forcedIcon,globalActionMap,kbs,menuPresenter

CLSS public final static org.netbeans.modules.editor.MainMenuAction$CommentAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public final static org.netbeans.modules.editor.MainMenuAction$FindNextAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public final static org.netbeans.modules.editor.MainMenuAction$FindPreviousAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public final static org.netbeans.modules.editor.MainMenuAction$FindSelectionAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
meth public boolean isEnabled()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public final static org.netbeans.modules.editor.MainMenuAction$FormatAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
meth protected javax.swing.Action getGlobalKitAction()
meth protected javax.swing.KeyStroke getDefaultAccelerator()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public static org.netbeans.modules.editor.MainMenuAction$GoToDeclarationAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public static org.netbeans.modules.editor.MainMenuAction$GoToSourceAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public static org.netbeans.modules.editor.MainMenuAction$GoToSuperAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public final static org.netbeans.modules.editor.MainMenuAction$JumpBackAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public final static org.netbeans.modules.editor.MainMenuAction$JumpForwardAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public final static org.netbeans.modules.editor.MainMenuAction$PasteFormattedAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public final static org.netbeans.modules.editor.MainMenuAction$RemoveTrailingSpacesAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public final static org.netbeans.modules.editor.MainMenuAction$SelectAllAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public final static org.netbeans.modules.editor.MainMenuAction$SelectIdentifierAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public final static org.netbeans.modules.editor.MainMenuAction$ShiftLineLeftAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public final static org.netbeans.modules.editor.MainMenuAction$ShiftLineRightAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public static org.netbeans.modules.editor.MainMenuAction$ShowLineNumbersAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
meth protected javax.swing.Action getGlobalKitAction()
meth protected void setMenu()
meth public java.lang.String getName()
meth public javax.swing.JMenuItem getMenuPresenter()
supr org.netbeans.modules.editor.MainMenuAction
hfds SHOW_LINE_MENU,delegate

CLSS public static org.netbeans.modules.editor.MainMenuAction$ShowToolBarAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
meth protected javax.swing.Action getGlobalKitAction()
meth protected void setMenu()
meth public javax.swing.JMenuItem getMenuPresenter()
supr org.netbeans.modules.editor.MainMenuAction
hfds SHOW_TOOLBAR_MENU,delegate

CLSS public final static org.netbeans.modules.editor.MainMenuAction$StartMacroRecordingAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public final static org.netbeans.modules.editor.MainMenuAction$StopMacroRecordingAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public final static org.netbeans.modules.editor.MainMenuAction$ToggleCommentAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public final static org.netbeans.modules.editor.MainMenuAction$UncommentAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public final static org.netbeans.modules.editor.MainMenuAction$WordMatchNextAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public final static org.netbeans.modules.editor.MainMenuAction$WordMatchPrevAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public org.netbeans.modules.editor.NbCodeFoldingAction
cons public init()
innr public CodeFoldsMenu
intf org.openide.util.actions.Presenter$Menu
meth public boolean isEnabled()
meth public final org.openide.util.HelpCtx getHelpCtx()
meth public java.lang.String getName()
meth public javax.swing.JMenuItem getMenuPresenter()
meth public void actionPerformed(java.awt.event.ActionEvent)
supr java.lang.Object

CLSS public org.netbeans.modules.editor.NbCodeFoldingAction$CodeFoldsMenu
 outer org.netbeans.modules.editor.NbCodeFoldingAction
cons public init(org.netbeans.modules.editor.NbCodeFoldingAction)
cons public init(org.netbeans.modules.editor.NbCodeFoldingAction,java.lang.String)
intf org.openide.awt.DynamicMenuContent
meth public javax.swing.JComponent[] getMenuPresenters()
meth public javax.swing.JComponent[] synchMenuPresenters(javax.swing.JComponent[])
meth public javax.swing.JPopupMenu getPopupMenu()
supr javax.swing.JMenu

CLSS public org.netbeans.modules.editor.NbDialogSupport
 anno 0 java.lang.Deprecated()
cons public init()
intf org.netbeans.editor.DialogSupport$DialogFactory
supr org.netbeans.modules.editor.impl.NbDialogFactory

CLSS public org.netbeans.modules.editor.NbEditorDocument
cons public init(java.lang.Class)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String)
fld public final static java.lang.String INDENT_ENGINE = "indentEngine"
intf org.openide.text.NbDocument$Annotatable
intf org.openide.text.NbDocument$CustomEditor
intf org.openide.text.NbDocument$CustomToolbar
intf org.openide.text.NbDocument$PositionBiasable
intf org.openide.text.NbDocument$Printable
intf org.openide.text.NbDocument$WriteLockable
meth protected java.util.Dictionary createDocumentProperties(java.util.Dictionary)
meth public int getShiftWidth()
meth public int getTabSize()
meth public java.awt.Component createEditor(javax.swing.JEditorPane)
meth public java.text.AttributedCharacterIterator[] createPrintIterators()
meth public javax.swing.JToolBar createToolbar(javax.swing.JEditorPane)
meth public void addAnnotation(javax.swing.text.Position,int,org.openide.text.Annotation)
meth public void removeAnnotation(org.openide.text.Annotation)
meth public void setCharacterAttributes(int,int,javax.swing.text.AttributeSet,boolean)
supr org.netbeans.editor.GuardedDocument
hfds RP,annoMap
hcls AnnotationDescDelegate,NbPrintContainer

CLSS public org.netbeans.modules.editor.NbEditorKit
cons public init()
fld public final static java.lang.String SYSTEM_ACTION_CLASS_NAME_PROPERTY = "systemActionClassName"
fld public final static java.lang.String generateFoldPopupAction = "generate-fold-popup"
fld public final static java.lang.String generateGoToPopupAction = "generate-goto-popup"
innr public NbStopMacroRecordingAction
innr public final static NbToggleLineNumbersAction
innr public static GenerateFoldPopupAction
innr public static NbBuildPopupMenuAction
innr public static NbBuildToolTipAction
innr public static NbGenerateGoToPopupAction
innr public static NbRedoAction
innr public static NbUndoAction
innr public static ToggleToolbarAction
intf java.util.concurrent.Callable
meth protected javax.swing.Action[] createActions()
meth protected javax.swing.Action[] getDeclaredActions()
meth protected org.netbeans.editor.EditorUI createEditorUI()
meth protected void addSystemActionMapping(java.lang.String,java.lang.Class)
meth protected void toolTipAnnotationsLock(javax.swing.text.Document)
meth protected void toolTipAnnotationsUnlock(javax.swing.text.Document)
meth protected void updateActions()
meth public java.lang.Object call()
meth public java.lang.String getContentType()
meth public javax.swing.text.Document createDefaultDocument()
supr org.netbeans.editor.ext.ExtKit
hfds ACTIONS_TOPCOMPONENT,ACTION_CREATEITEM,ACTION_EXTKIT_BYNAME,ACTION_FOLDER,ACTION_SEPARATOR,ACTION_SYSTEM,LOG,SEPARATOR,contentTypeTable,nbRedoActionDef,nbUndoActionDef,serialVersionUID,systemAction2editorAction
hcls LayerSubFolderMenu,PopupInitializer,SubFolderData

CLSS public static org.netbeans.modules.editor.NbEditorKit$GenerateFoldPopupAction
 outer org.netbeans.modules.editor.NbEditorKit
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth protected java.lang.String getItemText(javax.swing.text.JTextComponent,java.lang.String,javax.swing.Action)
meth protected void addAction(javax.swing.text.JTextComponent,javax.swing.JMenu,java.lang.String)
meth protected void addAdditionalItems(javax.swing.text.JTextComponent,javax.swing.JMenu)
meth protected void setAddSeparatorBeforeNextAction(boolean)
meth public javax.swing.JMenuItem getPopupMenuItem(javax.swing.text.JTextComponent)
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds addSeparatorBeforeNextAction

CLSS public static org.netbeans.modules.editor.NbEditorKit$NbBuildPopupMenuAction
 outer org.netbeans.modules.editor.NbEditorKit
cons public init()
cons public init(java.util.Map)
meth protected javax.swing.JPopupMenu buildPopupMenu(javax.swing.text.JTextComponent)
meth protected javax.swing.JPopupMenu createPopupMenu(javax.swing.text.JTextComponent)
meth protected void addAction(javax.swing.text.JTextComponent,javax.swing.JPopupMenu,java.lang.String)
meth protected void addAction(javax.swing.text.JTextComponent,javax.swing.JPopupMenu,javax.swing.Action)
supr org.netbeans.editor.ext.ExtKit$BuildPopupMenuAction
hfds serialVersionUID

CLSS public static org.netbeans.modules.editor.NbEditorKit$NbBuildToolTipAction
 outer org.netbeans.modules.editor.NbEditorKit
cons public init()
cons public init(java.util.Map)
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.ext.ExtKit$BuildToolTipAction

CLSS public static org.netbeans.modules.editor.NbEditorKit$NbGenerateGoToPopupAction
 outer org.netbeans.modules.editor.NbEditorKit
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction

CLSS public static org.netbeans.modules.editor.NbEditorKit$NbRedoAction
 outer org.netbeans.modules.editor.NbEditorKit
cons public init()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.ActionFactory$RedoAction

CLSS public org.netbeans.modules.editor.NbEditorKit$NbStopMacroRecordingAction
 outer org.netbeans.modules.editor.NbEditorKit
 anno 0 java.lang.Deprecated()
cons public init(org.netbeans.modules.editor.NbEditorKit)
meth protected org.netbeans.editor.MacroDialogSupport getMacroDialogSupport(java.lang.Class)
supr org.netbeans.editor.ActionFactory$StopMacroRecordingAction

CLSS public final static org.netbeans.modules.editor.NbEditorKit$NbToggleLineNumbersAction
 outer org.netbeans.modules.editor.NbEditorKit
 anno 0 java.lang.Deprecated()
cons public init()
meth protected boolean isLineNumbersVisible()
meth protected void toggleLineNumbers()
supr org.netbeans.editor.ActionFactory$ToggleLineNumbersAction

CLSS public static org.netbeans.modules.editor.NbEditorKit$NbUndoAction
 outer org.netbeans.modules.editor.NbEditorKit
cons public init()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.ActionFactory$UndoAction

CLSS public static org.netbeans.modules.editor.NbEditorKit$ToggleToolbarAction
 outer org.netbeans.modules.editor.NbEditorKit
 anno 0 java.lang.Deprecated()
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public javax.swing.JMenuItem getPopupMenuItem(javax.swing.text.JTextComponent)
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction

CLSS public org.netbeans.modules.editor.NbEditorUI
cons public init()
innr public final SystemActionUpdater
meth protected int textLimitWidth()
meth protected javax.swing.JComponent createExtComponent()
meth protected javax.swing.JToolBar createToolBarComponent()
meth protected org.netbeans.modules.editor.NbEditorUI$SystemActionUpdater createSystemActionUpdater(java.lang.String,boolean,boolean)
 anno 0 java.lang.Deprecated()
meth protected void attachSystemActionPerformer(java.lang.String)
meth protected void installUI(javax.swing.text.JTextComponent)
meth protected void uninstallUI(javax.swing.text.JTextComponent)
meth public boolean isLineNumberEnabled()
meth public void setLineNumberEnabled(boolean)
supr org.netbeans.editor.EditorUI
hfds TASK,WORKER,attached,focusL,listener,lock,objectsToRefresh
hcls EnabledPropertySyncListener,LayeredEditorPane,SideBarsListener,SystemActionPerformer

CLSS public final org.netbeans.modules.editor.NbEditorUI$SystemActionUpdater
 outer org.netbeans.modules.editor.NbEditorUI
 anno 0 java.lang.Deprecated()
intf java.beans.PropertyChangeListener
intf org.openide.util.actions.ActionPerformer
meth protected void finalize() throws java.lang.Throwable
meth public void editorActivated()
meth public void editorDeactivated()
meth public void performAction(org.openide.util.actions.SystemAction)
meth public void propertyChange(java.beans.PropertyChangeEvent)
supr java.lang.Object
hfds editorAction,editorActionName,enabledPropertySyncL,listeningOnTCRegistry,syncEnabling,systemAction,updatePerformer

CLSS public org.netbeans.modules.editor.NbEditorUtilities
cons public init()
meth public static boolean isDocumentActive(javax.swing.text.Document)
meth public static int[] getIdentifierAndMethodBlock(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
meth public static java.lang.String getMimeType(javax.swing.text.Document)
meth public static java.lang.String getMimeType(javax.swing.text.JTextComponent)
meth public static java.lang.String[] mergeStringArrays(java.lang.String[],java.lang.String[])
meth public static org.openide.filesystems.FileObject getFileObject(javax.swing.text.Document)
meth public static org.openide.loaders.DataObject getDataObject(javax.swing.text.Document)
meth public static org.openide.text.Line getLine(javax.swing.text.Document,int,boolean)
meth public static org.openide.text.Line getLine(javax.swing.text.JTextComponent,boolean)
meth public static org.openide.text.Line getLine(org.netbeans.editor.BaseDocument,int,boolean)
 anno 0 java.lang.Deprecated()
meth public static org.openide.windows.TopComponent getOuterTopComponent(javax.swing.text.JTextComponent)
meth public static org.openide.windows.TopComponent getTopComponent(javax.swing.text.JTextComponent)
meth public static void addJumpListEntry(org.openide.loaders.DataObject)
meth public static void invalidArgument(java.lang.String)
supr java.lang.Object

CLSS public org.netbeans.modules.editor.NbImplementationProvider
 anno 0 java.lang.Deprecated()
cons public init()
fld public final static java.lang.String GLYPH_GUTTER_ACTIONS_FOLDER_NAME = "GlyphGutterActions"
meth public boolean activateComponent(javax.swing.text.JTextComponent)
meth public java.util.ResourceBundle getResourceBundle(java.lang.String)
meth public javax.swing.Action[] getGlyphGutterActions(javax.swing.text.JTextComponent)
supr org.netbeans.editor.ImplementationProvider
hfds provider

CLSS public org.netbeans.modules.editor.NbLocalizer
cons public init(java.lang.Class)
intf org.netbeans.editor.LocaleSupport$Localizer
meth public java.lang.String getString(java.lang.String)
meth public java.lang.String toString()
supr java.lang.Object
hfds bundleClass

CLSS public org.netbeans.modules.editor.NbToolTip
supr org.openide.filesystems.FileChangeAdapter
hfds LOG,lastRequestId,lastToolTipTask,mime2tip,mimeType,tipAnnotations,toolTipRP
hcls Request

CLSS public org.netbeans.modules.editor.SimpleIndentEngine
cons public init()
meth protected boolean acceptMimeType(java.lang.String)
meth protected org.netbeans.editor.ext.ExtFormatter createFormatter()
supr org.netbeans.modules.editor.FormatterIndentEngine
hfds serialVersionUID

CLSS public org.netbeans.modules.editor.SimpleIndentEngineBeanInfo
cons public init()
meth protected java.lang.Class getBeanClass()
meth protected java.lang.String getString(java.lang.String)
meth public java.beans.BeanDescriptor getBeanDescriptor()
supr org.netbeans.modules.editor.FormatterIndentEngineBeanInfo
hfds beanDescriptor

CLSS public org.netbeans.modules.editor.impl.NbDialogFactory
cons public init()
intf org.netbeans.modules.editor.lib2.DialogFactory
meth public java.awt.Dialog createDialog(java.lang.String,javax.swing.JPanel,boolean,javax.swing.JButton[],boolean,int,int,java.awt.event.ActionListener)
supr java.lang.Object
hfds HELP_ID_JavaFastImportPanel,HELP_ID_MacroSavePanel,HELP_ID_ScrollCompletionPane,helpIDs

CLSS public abstract interface org.netbeans.modules.editor.lib2.DialogFactory
meth public abstract java.awt.Dialog createDialog(java.lang.String,javax.swing.JPanel,boolean,javax.swing.JButton[],boolean,int,int,java.awt.event.ActionListener)

CLSS public abstract interface !annotation org.netbeans.spi.editor.mimelookup.MimeLocation
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass()
meth public abstract java.lang.String subfolderName()

CLSS public abstract org.openide.ServiceType
 anno 0 java.lang.Deprecated()
cons public init()
fld public final static java.lang.String PROP_NAME = "name"
innr public abstract static Registry
innr public final static Handle
intf java.io.Serializable
intf org.openide.util.HelpCtx$Provider
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
 anno 0 java.lang.Deprecated()
meth protected java.lang.String displayName()
meth public abstract org.openide.util.HelpCtx getHelpCtx()
meth public final org.openide.ServiceType createClone()
 anno 0 java.lang.Deprecated()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public java.lang.String getName()
meth public void setName(java.lang.String)
supr java.lang.Object
hfds err,name,serialVersionUID,supp

CLSS public abstract interface org.openide.awt.DynamicMenuContent
fld public final static java.lang.String HIDE_WHEN_DISABLED = "hideWhenDisabled"
meth public abstract javax.swing.JComponent[] getMenuPresenters()
meth public abstract javax.swing.JComponent[] synchMenuPresenters(javax.swing.JComponent[])

CLSS public org.openide.filesystems.FileChangeAdapter
cons public init()
intf org.openide.filesystems.FileChangeListener
meth public void fileAttributeChanged(org.openide.filesystems.FileAttributeEvent)
meth public void fileChanged(org.openide.filesystems.FileEvent)
meth public void fileDataCreated(org.openide.filesystems.FileEvent)
meth public void fileDeleted(org.openide.filesystems.FileEvent)
meth public void fileFolderCreated(org.openide.filesystems.FileEvent)
meth public void fileRenamed(org.openide.filesystems.FileRenameEvent)
supr java.lang.Object

CLSS public abstract interface org.openide.filesystems.FileChangeListener
intf java.util.EventListener
meth public abstract void fileAttributeChanged(org.openide.filesystems.FileAttributeEvent)
meth public abstract void fileChanged(org.openide.filesystems.FileEvent)
meth public abstract void fileDataCreated(org.openide.filesystems.FileEvent)
meth public abstract void fileDeleted(org.openide.filesystems.FileEvent)
meth public abstract void fileFolderCreated(org.openide.filesystems.FileEvent)
meth public abstract void fileRenamed(org.openide.filesystems.FileRenameEvent)

CLSS public org.openide.modules.ModuleInstall
cons public init()
meth protected boolean clearSharedData()
meth public boolean closing()
meth public void close()
meth public void installed()
 anno 0 java.lang.Deprecated()
meth public void restored()
meth public void uninstalled()
meth public void updated(int,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void validate()
supr org.openide.util.SharedClassObject
hfds serialVersionUID

CLSS public abstract org.openide.text.IndentEngine
 anno 0 java.lang.Deprecated()
cons public init()
meth protected boolean acceptMimeType(java.lang.String)
meth public abstract int indentLine(javax.swing.text.Document,int)
meth public abstract int indentNewLine(javax.swing.text.Document,int)
meth public abstract java.io.Writer createWriter(javax.swing.text.Document,int,java.io.Writer)
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static java.util.Enumeration<? extends org.openide.text.IndentEngine> indentEngines()
meth public static org.openide.text.IndentEngine find(java.lang.String)
meth public static org.openide.text.IndentEngine find(javax.swing.text.Document)
meth public static org.openide.text.IndentEngine getDefault()
meth public static void register(java.lang.String,org.openide.text.IndentEngine)
 anno 0 java.lang.Deprecated()
supr org.openide.ServiceType
hfds INSTANCE,map,serialVersionUID
hcls Default

CLSS public final org.openide.text.NbDocument
fld public final static java.lang.Object GUARDED
fld public final static java.lang.String BREAKPOINT_STYLE_NAME = "NbBreakpointStyle"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String CURRENT_STYLE_NAME = "NbCurrentStyle"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String ERROR_STYLE_NAME = "NbErrorStyle"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String NORMAL_STYLE_NAME = "NbNormalStyle"
 anno 0 java.lang.Deprecated()
innr public abstract interface static Annotatable
innr public abstract interface static CustomEditor
innr public abstract interface static CustomToolbar
innr public abstract interface static PositionBiasable
innr public abstract interface static Printable
innr public abstract interface static WriteLockable
meth public static <%0 extends javax.swing.undo.UndoableEdit> {%%0} getEditToBeRedoneOfType(org.openide.cookies.EditorCookie,java.lang.Class<{%%0}>)
meth public static <%0 extends javax.swing.undo.UndoableEdit> {%%0} getEditToBeUndoneOfType(org.openide.cookies.EditorCookie,java.lang.Class<{%%0}>)
meth public static boolean openDocument(org.openide.util.Lookup$Provider,int,int,org.openide.text.Line$ShowOpenType,org.openide.text.Line$ShowVisibilityType)
meth public static boolean openDocument(org.openide.util.Lookup$Provider,int,org.openide.text.Line$ShowOpenType,org.openide.text.Line$ShowVisibilityType)
meth public static int findLineColumn(javax.swing.text.StyledDocument,int)
meth public static int findLineNumber(javax.swing.text.StyledDocument,int)
meth public static int findLineOffset(javax.swing.text.StyledDocument,int)
meth public static java.lang.Object findPageable(javax.swing.text.StyledDocument)
meth public static javax.swing.JEditorPane findRecentEditorPane(org.openide.cookies.EditorCookie)
meth public static javax.swing.text.Element findLineRootElement(javax.swing.text.StyledDocument)
meth public static javax.swing.text.Position createPosition(javax.swing.text.Document,int,javax.swing.text.Position$Bias) throws javax.swing.text.BadLocationException
meth public static javax.swing.text.StyledDocument getDocument(org.openide.util.Lookup$Provider)
meth public static void addAnnotation(javax.swing.text.StyledDocument,javax.swing.text.Position,int,org.openide.text.Annotation)
meth public static void insertGuarded(javax.swing.text.StyledDocument,int,java.lang.String) throws javax.swing.text.BadLocationException
meth public static void markBreakpoint(javax.swing.text.StyledDocument,int)
 anno 0 java.lang.Deprecated()
meth public static void markCurrent(javax.swing.text.StyledDocument,int)
 anno 0 java.lang.Deprecated()
meth public static void markError(javax.swing.text.StyledDocument,int)
 anno 0 java.lang.Deprecated()
meth public static void markGuarded(javax.swing.text.StyledDocument,int,int)
meth public static void markNormal(javax.swing.text.StyledDocument,int)
 anno 0 java.lang.Deprecated()
meth public static void removeAnnotation(javax.swing.text.StyledDocument,org.openide.text.Annotation)
meth public static void runAtomic(javax.swing.text.StyledDocument,java.lang.Runnable)
meth public static void runAtomicAsUser(javax.swing.text.StyledDocument,java.lang.Runnable) throws javax.swing.text.BadLocationException
meth public static void unmarkGuarded(javax.swing.text.StyledDocument,int,int)
supr java.lang.Object
hfds ATTR_ADD,ATTR_REMOVE
hcls DocumentRenderer

CLSS public abstract interface static org.openide.text.NbDocument$Annotatable
 outer org.openide.text.NbDocument
intf javax.swing.text.Document
meth public abstract void addAnnotation(javax.swing.text.Position,int,org.openide.text.Annotation)
meth public abstract void removeAnnotation(org.openide.text.Annotation)

CLSS public abstract interface static org.openide.text.NbDocument$CustomEditor
 outer org.openide.text.NbDocument
intf javax.swing.text.Document
meth public abstract java.awt.Component createEditor(javax.swing.JEditorPane)

CLSS public abstract interface static org.openide.text.NbDocument$CustomToolbar
 outer org.openide.text.NbDocument
intf javax.swing.text.Document
meth public abstract javax.swing.JToolBar createToolbar(javax.swing.JEditorPane)

CLSS public abstract interface static org.openide.text.NbDocument$PositionBiasable
 outer org.openide.text.NbDocument
intf javax.swing.text.Document
meth public abstract javax.swing.text.Position createPosition(int,javax.swing.text.Position$Bias) throws javax.swing.text.BadLocationException

CLSS public abstract interface static org.openide.text.NbDocument$Printable
 outer org.openide.text.NbDocument
intf javax.swing.text.Document
meth public abstract java.text.AttributedCharacterIterator[] createPrintIterators()

CLSS public abstract interface static org.openide.text.NbDocument$WriteLockable
 outer org.openide.text.NbDocument
intf javax.swing.text.Document
meth public abstract void runAtomic(java.lang.Runnable)
meth public abstract void runAtomicAsUser(java.lang.Runnable) throws javax.swing.text.BadLocationException

CLSS public abstract interface org.openide.util.ContextAwareAction
intf javax.swing.Action
meth public abstract javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)

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

CLSS public abstract interface org.openide.util.LookupListener
intf java.util.EventListener
meth public abstract void resultChanged(org.openide.util.LookupEvent)

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

CLSS public abstract interface org.openide.util.actions.ActionPerformer
 anno 0 java.lang.Deprecated()
meth public abstract void performAction(org.openide.util.actions.SystemAction)

CLSS public abstract org.openide.util.actions.CallableSystemAction
cons public init()
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected boolean asynchronous()
meth public abstract void performAction()
meth public java.awt.Component getToolbarPresenter()
meth public javax.swing.JMenuItem getMenuPresenter()
meth public javax.swing.JMenuItem getPopupPresenter()
meth public void actionPerformed(java.awt.event.ActionEvent)
supr org.openide.util.actions.SystemAction
hfds DEFAULT_ASYNCH,serialVersionUID,warnedAsynchronousActions

CLSS public abstract org.openide.util.actions.CookieAction
cons public init()
fld public final static int MODE_ALL = 4
fld public final static int MODE_ANY = 7
fld public final static int MODE_EXACTLY_ONE = 8
fld public final static int MODE_ONE = 1
fld public final static int MODE_SOME = 2
meth protected abstract int mode()
meth protected abstract java.lang.Class<?>[] cookieClasses()
meth protected boolean enable(org.openide.nodes.Node[])
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
supr org.openide.util.actions.NodeAction
hfds PROP_COOKIES,listener,serialVersionUID
hcls CookieDelegateAction,CookiesChangeListener

CLSS public abstract org.openide.util.actions.NodeAction
cons public init()
intf org.openide.util.ContextAwareAction
meth protected abstract boolean enable(org.openide.nodes.Node[])
meth protected abstract void performAction(org.openide.nodes.Node[])
meth protected boolean surviveFocusChange()
meth protected void addNotify()
meth protected void initialize()
meth protected void removeNotify()
meth public boolean isEnabled()
meth public final org.openide.nodes.Node[] getActivatedNodes()
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
meth public void actionPerformed(java.awt.event.ActionEvent)
 anno 0 java.lang.Deprecated()
meth public void performAction()
 anno 0 java.lang.Deprecated()
meth public void setEnabled(boolean)
supr org.openide.util.actions.CallableSystemAction
hfds PROP_HAS_LISTENERS,PROP_LAST_ENABLED,PROP_LAST_NODES,l,listeningActions,serialVersionUID
hcls DelegateAction,NodesL

CLSS public abstract interface org.openide.util.actions.Presenter
innr public abstract interface static Menu
innr public abstract interface static Popup
innr public abstract interface static Toolbar

CLSS public abstract interface static org.openide.util.actions.Presenter$Menu
 outer org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter
meth public abstract javax.swing.JMenuItem getMenuPresenter()

CLSS public abstract interface static org.openide.util.actions.Presenter$Popup
 outer org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter
meth public abstract javax.swing.JMenuItem getPopupPresenter()

CLSS public abstract interface static org.openide.util.actions.Presenter$Toolbar
 outer org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter
meth public abstract java.awt.Component getToolbarPresenter()

CLSS public abstract org.openide.util.actions.SystemAction
cons public init()
fld public final static java.lang.String PROP_ENABLED = "enabled"
fld public final static java.lang.String PROP_ICON = "icon"
intf javax.swing.Action
intf org.openide.util.HelpCtx$Provider
meth protected boolean clearSharedData()
meth protected java.lang.String iconResource()
meth protected void initialize()
meth public abstract java.lang.String getName()
meth public abstract org.openide.util.HelpCtx getHelpCtx()
meth public abstract void actionPerformed(java.awt.event.ActionEvent)
meth public boolean isEnabled()
meth public final java.lang.Object getValue(java.lang.String)
meth public final javax.swing.Icon getIcon()
meth public final javax.swing.Icon getIcon(boolean)
meth public final void putValue(java.lang.String,java.lang.Object)
meth public final void setIcon(javax.swing.Icon)
meth public static <%0 extends org.openide.util.actions.SystemAction> {%%0} get(java.lang.Class<{%%0}>)
meth public static javax.swing.JPopupMenu createPopupMenu(org.openide.util.actions.SystemAction[])
 anno 0 java.lang.Deprecated()
meth public static javax.swing.JToolBar createToolbarPresenter(org.openide.util.actions.SystemAction[])
meth public static org.openide.util.actions.SystemAction[] linkActions(org.openide.util.actions.SystemAction[],org.openide.util.actions.SystemAction[])
meth public void setEnabled(boolean)
supr org.openide.util.SharedClassObject
hfds LOG,PROP_ICON_TEXTUAL,relativeIconResourceClasses,serialVersionUID
hcls ComponentIcon

