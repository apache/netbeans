#Signature file v4.1
#Version 1.93.0

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

CLSS public abstract java.awt.event.MouseAdapter
cons public init()
intf java.awt.event.MouseListener
intf java.awt.event.MouseMotionListener
intf java.awt.event.MouseWheelListener
meth public void mouseClicked(java.awt.event.MouseEvent)
meth public void mouseDragged(java.awt.event.MouseEvent)
meth public void mouseEntered(java.awt.event.MouseEvent)
meth public void mouseExited(java.awt.event.MouseEvent)
meth public void mouseMoved(java.awt.event.MouseEvent)
meth public void mousePressed(java.awt.event.MouseEvent)
meth public void mouseReleased(java.awt.event.MouseEvent)
meth public void mouseWheelMoved(java.awt.event.MouseWheelEvent)
supr java.lang.Object

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

CLSS public abstract interface java.awt.event.MouseWheelListener
intf java.util.EventListener
meth public abstract void mouseWheelMoved(java.awt.event.MouseWheelEvent)

CLSS public abstract interface java.awt.event.WindowListener
intf java.util.EventListener
meth public abstract void windowActivated(java.awt.event.WindowEvent)
meth public abstract void windowClosed(java.awt.event.WindowEvent)
meth public abstract void windowClosing(java.awt.event.WindowEvent)
meth public abstract void windowDeactivated(java.awt.event.WindowEvent)
meth public abstract void windowDeiconified(java.awt.event.WindowEvent)
meth public abstract void windowIconified(java.awt.event.WindowEvent)
meth public abstract void windowOpened(java.awt.event.WindowEvent)

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

CLSS public java.io.FilterInputStream
cons protected init(java.io.InputStream)
fld protected volatile java.io.InputStream in
meth public boolean markSupported()
meth public int available() throws java.io.IOException
meth public int read() throws java.io.IOException
meth public int read(byte[]) throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void mark(int)
meth public void reset() throws java.io.IOException
supr java.io.InputStream

CLSS public abstract java.io.InputStream
cons public init()
intf java.io.Closeable
meth public abstract int read() throws java.io.IOException
meth public boolean markSupported()
meth public int available() throws java.io.IOException
meth public int read(byte[]) throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void mark(int)
meth public void reset() throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface java.io.Serializable

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

CLSS public abstract interface java.util.EventListener

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object

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

CLSS public abstract interface java.util.prefs.PreferenceChangeListener
 anno 0 java.lang.FunctionalInterface()
intf java.util.EventListener
meth public abstract void preferenceChange(java.util.prefs.PreferenceChangeEvent)

CLSS public abstract interface javax.accessibility.Accessible
meth public abstract javax.accessibility.AccessibleContext getAccessibleContext()

CLSS public abstract javax.net.SocketFactory
cons protected init()
meth public abstract java.net.Socket createSocket(java.lang.String,int) throws java.io.IOException
meth public abstract java.net.Socket createSocket(java.lang.String,int,java.net.InetAddress,int) throws java.io.IOException
meth public abstract java.net.Socket createSocket(java.net.InetAddress,int) throws java.io.IOException
meth public abstract java.net.Socket createSocket(java.net.InetAddress,int,java.net.InetAddress,int) throws java.io.IOException
meth public java.net.Socket createSocket() throws java.io.IOException
meth public static javax.net.SocketFactory getDefault()
supr java.lang.Object

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

CLSS public javax.swing.JButton
cons public init()
cons public init(java.lang.String)
 anno 0 java.beans.ConstructorProperties(java.lang.String[] value=["text"])
cons public init(java.lang.String,javax.swing.Icon)
cons public init(javax.swing.Action)
cons public init(javax.swing.Icon)
innr protected AccessibleJButton
intf javax.accessibility.Accessible
meth protected java.lang.String paramString()
meth public boolean isDefaultButton()
meth public boolean isDefaultCapable()
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public void removeNotify()
meth public void setDefaultCapable(boolean)
meth public void updateUI()
supr javax.swing.AbstractButton

CLSS public javax.swing.JCheckBox
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,boolean)
cons public init(java.lang.String,javax.swing.Icon)
cons public init(java.lang.String,javax.swing.Icon,boolean)
cons public init(javax.swing.Action)
cons public init(javax.swing.Icon)
cons public init(javax.swing.Icon,boolean)
fld public final static java.lang.String BORDER_PAINTED_FLAT_CHANGED_PROPERTY = "borderPaintedFlat"
innr protected AccessibleJCheckBox
intf javax.accessibility.Accessible
meth protected java.lang.String paramString()
meth public boolean isBorderPaintedFlat()
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public void setBorderPaintedFlat(boolean)
meth public void updateUI()
supr javax.swing.JToggleButton

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

CLSS public javax.swing.JFileChooser
cons public init()
cons public init(java.io.File)
cons public init(java.io.File,javax.swing.filechooser.FileSystemView)
cons public init(java.lang.String)
cons public init(java.lang.String,javax.swing.filechooser.FileSystemView)
cons public init(javax.swing.filechooser.FileSystemView)
fld protected javax.accessibility.AccessibleContext accessibleContext
fld public final static int APPROVE_OPTION = 0
fld public final static int CANCEL_OPTION = 1
fld public final static int CUSTOM_DIALOG = 2
fld public final static int DIRECTORIES_ONLY = 1
fld public final static int ERROR_OPTION = -1
fld public final static int FILES_AND_DIRECTORIES = 2
fld public final static int FILES_ONLY = 0
fld public final static int OPEN_DIALOG = 0
fld public final static int SAVE_DIALOG = 1
fld public final static java.lang.String ACCEPT_ALL_FILE_FILTER_USED_CHANGED_PROPERTY = "acceptAllFileFilterUsedChanged"
fld public final static java.lang.String ACCESSORY_CHANGED_PROPERTY = "AccessoryChangedProperty"
fld public final static java.lang.String APPROVE_BUTTON_MNEMONIC_CHANGED_PROPERTY = "ApproveButtonMnemonicChangedProperty"
fld public final static java.lang.String APPROVE_BUTTON_TEXT_CHANGED_PROPERTY = "ApproveButtonTextChangedProperty"
fld public final static java.lang.String APPROVE_BUTTON_TOOL_TIP_TEXT_CHANGED_PROPERTY = "ApproveButtonToolTipTextChangedProperty"
fld public final static java.lang.String APPROVE_SELECTION = "ApproveSelection"
fld public final static java.lang.String CANCEL_SELECTION = "CancelSelection"
fld public final static java.lang.String CHOOSABLE_FILE_FILTER_CHANGED_PROPERTY = "ChoosableFileFilterChangedProperty"
fld public final static java.lang.String CONTROL_BUTTONS_ARE_SHOWN_CHANGED_PROPERTY = "ControlButtonsAreShownChangedProperty"
fld public final static java.lang.String DIALOG_TITLE_CHANGED_PROPERTY = "DialogTitleChangedProperty"
fld public final static java.lang.String DIALOG_TYPE_CHANGED_PROPERTY = "DialogTypeChangedProperty"
fld public final static java.lang.String DIRECTORY_CHANGED_PROPERTY = "directoryChanged"
fld public final static java.lang.String FILE_FILTER_CHANGED_PROPERTY = "fileFilterChanged"
fld public final static java.lang.String FILE_HIDING_CHANGED_PROPERTY = "FileHidingChanged"
fld public final static java.lang.String FILE_SELECTION_MODE_CHANGED_PROPERTY = "fileSelectionChanged"
fld public final static java.lang.String FILE_SYSTEM_VIEW_CHANGED_PROPERTY = "FileSystemViewChanged"
fld public final static java.lang.String FILE_VIEW_CHANGED_PROPERTY = "fileViewChanged"
fld public final static java.lang.String MULTI_SELECTION_ENABLED_CHANGED_PROPERTY = "MultiSelectionEnabledChangedProperty"
fld public final static java.lang.String SELECTED_FILES_CHANGED_PROPERTY = "SelectedFilesChangedProperty"
fld public final static java.lang.String SELECTED_FILE_CHANGED_PROPERTY = "SelectedFileChangedProperty"
innr protected AccessibleJFileChooser
intf javax.accessibility.Accessible
meth protected java.lang.String paramString()
meth protected javax.swing.JDialog createDialog(java.awt.Component)
meth protected void fireActionPerformed(java.lang.String)
meth protected void setup(javax.swing.filechooser.FileSystemView)
meth public boolean accept(java.io.File)
meth public boolean getControlButtonsAreShown()
meth public boolean getDragEnabled()
meth public boolean isAcceptAllFileFilterUsed()
meth public boolean isDirectorySelectionEnabled()
meth public boolean isFileHidingEnabled()
meth public boolean isFileSelectionEnabled()
meth public boolean isMultiSelectionEnabled()
meth public boolean isTraversable(java.io.File)
meth public boolean removeChoosableFileFilter(javax.swing.filechooser.FileFilter)
meth public int getApproveButtonMnemonic()
meth public int getDialogType()
meth public int getFileSelectionMode()
meth public int showDialog(java.awt.Component,java.lang.String)
meth public int showOpenDialog(java.awt.Component)
meth public int showSaveDialog(java.awt.Component)
meth public java.awt.event.ActionListener[] getActionListeners()
meth public java.io.File getCurrentDirectory()
meth public java.io.File getSelectedFile()
meth public java.io.File[] getSelectedFiles()
meth public java.lang.String getApproveButtonText()
meth public java.lang.String getApproveButtonToolTipText()
meth public java.lang.String getDescription(java.io.File)
meth public java.lang.String getDialogTitle()
meth public java.lang.String getName(java.io.File)
meth public java.lang.String getTypeDescription(java.io.File)
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.Icon getIcon(java.io.File)
meth public javax.swing.JComponent getAccessory()
meth public javax.swing.filechooser.FileFilter getAcceptAllFileFilter()
meth public javax.swing.filechooser.FileFilter getFileFilter()
meth public javax.swing.filechooser.FileFilter[] getChoosableFileFilters()
meth public javax.swing.filechooser.FileSystemView getFileSystemView()
meth public javax.swing.filechooser.FileView getFileView()
meth public javax.swing.plaf.FileChooserUI getUI()
meth public void addActionListener(java.awt.event.ActionListener)
meth public void addChoosableFileFilter(javax.swing.filechooser.FileFilter)
meth public void approveSelection()
meth public void cancelSelection()
meth public void changeToParentDirectory()
meth public void ensureFileIsVisible(java.io.File)
meth public void removeActionListener(java.awt.event.ActionListener)
meth public void rescanCurrentDirectory()
meth public void resetChoosableFileFilters()
meth public void setAcceptAllFileFilterUsed(boolean)
meth public void setAccessory(javax.swing.JComponent)
meth public void setApproveButtonMnemonic(char)
meth public void setApproveButtonMnemonic(int)
meth public void setApproveButtonText(java.lang.String)
meth public void setApproveButtonToolTipText(java.lang.String)
meth public void setControlButtonsAreShown(boolean)
meth public void setCurrentDirectory(java.io.File)
meth public void setDialogTitle(java.lang.String)
meth public void setDialogType(int)
meth public void setDragEnabled(boolean)
meth public void setFileFilter(javax.swing.filechooser.FileFilter)
meth public void setFileHidingEnabled(boolean)
meth public void setFileSelectionMode(int)
meth public void setFileSystemView(javax.swing.filechooser.FileSystemView)
meth public void setFileView(javax.swing.filechooser.FileView)
meth public void setMultiSelectionEnabled(boolean)
meth public void setSelectedFile(java.io.File)
meth public void setSelectedFiles(java.io.File[])
meth public void updateUI()
supr javax.swing.JComponent

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

CLSS public javax.swing.JTable
cons public init()
cons public init(int,int)
cons public init(java.lang.Object[][],java.lang.Object[])
cons public init(java.util.Vector,java.util.Vector)
cons public init(javax.swing.table.TableModel)
cons public init(javax.swing.table.TableModel,javax.swing.table.TableColumnModel)
cons public init(javax.swing.table.TableModel,javax.swing.table.TableColumnModel,javax.swing.ListSelectionModel)
fld protected boolean autoCreateColumnsFromModel
fld protected boolean cellSelectionEnabled
fld protected boolean rowSelectionAllowed
fld protected boolean showHorizontalLines
fld protected boolean showVerticalLines
fld protected int autoResizeMode
fld protected int editingColumn
fld protected int editingRow
fld protected int rowHeight
fld protected int rowMargin
fld protected java.awt.Color gridColor
fld protected java.awt.Color selectionBackground
fld protected java.awt.Color selectionForeground
fld protected java.awt.Component editorComp
fld protected java.awt.Dimension preferredViewportSize
fld protected java.util.Hashtable defaultEditorsByColumnClass
fld protected java.util.Hashtable defaultRenderersByColumnClass
fld protected javax.swing.ListSelectionModel selectionModel
fld protected javax.swing.table.JTableHeader tableHeader
fld protected javax.swing.table.TableCellEditor cellEditor
fld protected javax.swing.table.TableColumnModel columnModel
fld protected javax.swing.table.TableModel dataModel
fld public final static int AUTO_RESIZE_ALL_COLUMNS = 4
fld public final static int AUTO_RESIZE_LAST_COLUMN = 3
fld public final static int AUTO_RESIZE_NEXT_COLUMN = 1
fld public final static int AUTO_RESIZE_OFF = 0
fld public final static int AUTO_RESIZE_SUBSEQUENT_COLUMNS = 2
innr protected AccessibleJTable
innr public final static !enum PrintMode
innr public final static DropLocation
intf javax.accessibility.Accessible
intf javax.swing.Scrollable
intf javax.swing.event.CellEditorListener
intf javax.swing.event.ListSelectionListener
intf javax.swing.event.RowSorterListener
intf javax.swing.event.TableColumnModelListener
intf javax.swing.event.TableModelListener
meth protected boolean processKeyBinding(javax.swing.KeyStroke,java.awt.event.KeyEvent,int,boolean)
meth protected java.lang.String paramString()
meth protected javax.swing.ListSelectionModel createDefaultSelectionModel()
meth protected javax.swing.table.JTableHeader createDefaultTableHeader()
meth protected javax.swing.table.TableColumnModel createDefaultColumnModel()
meth protected javax.swing.table.TableModel createDefaultDataModel()
meth protected void configureEnclosingScrollPane()
meth protected void createDefaultEditors()
meth protected void createDefaultRenderers()
meth protected void initializeLocalVars()
meth protected void resizeAndRepaint()
meth protected void unconfigureEnclosingScrollPane()
meth public boolean editCellAt(int,int)
meth public boolean editCellAt(int,int,java.util.EventObject)
meth public boolean getAutoCreateColumnsFromModel()
meth public boolean getAutoCreateRowSorter()
meth public boolean getCellSelectionEnabled()
meth public boolean getColumnSelectionAllowed()
meth public boolean getDragEnabled()
meth public boolean getFillsViewportHeight()
meth public boolean getRowSelectionAllowed()
meth public boolean getScrollableTracksViewportHeight()
meth public boolean getScrollableTracksViewportWidth()
meth public boolean getShowHorizontalLines()
meth public boolean getShowVerticalLines()
meth public boolean getSurrendersFocusOnKeystroke()
meth public boolean getUpdateSelectionOnSort()
meth public boolean isCellEditable(int,int)
meth public boolean isCellSelected(int,int)
meth public boolean isColumnSelected(int)
meth public boolean isEditing()
meth public boolean isRowSelected(int)
meth public boolean print() throws java.awt.print.PrinterException
meth public boolean print(javax.swing.JTable$PrintMode) throws java.awt.print.PrinterException
meth public boolean print(javax.swing.JTable$PrintMode,java.text.MessageFormat,java.text.MessageFormat) throws java.awt.print.PrinterException
meth public boolean print(javax.swing.JTable$PrintMode,java.text.MessageFormat,java.text.MessageFormat,boolean,javax.print.attribute.PrintRequestAttributeSet,boolean) throws java.awt.print.PrinterException
meth public boolean print(javax.swing.JTable$PrintMode,java.text.MessageFormat,java.text.MessageFormat,boolean,javax.print.attribute.PrintRequestAttributeSet,boolean,javax.print.PrintService) throws java.awt.print.PrinterException
meth public final javax.swing.DropMode getDropMode()
meth public final javax.swing.JTable$DropLocation getDropLocation()
meth public final void setDropMode(javax.swing.DropMode)
meth public int columnAtPoint(java.awt.Point)
meth public int convertColumnIndexToModel(int)
meth public int convertColumnIndexToView(int)
meth public int convertRowIndexToModel(int)
meth public int convertRowIndexToView(int)
meth public int getAutoResizeMode()
meth public int getColumnCount()
meth public int getEditingColumn()
meth public int getEditingRow()
meth public int getRowCount()
meth public int getRowHeight()
meth public int getRowHeight(int)
meth public int getRowMargin()
meth public int getScrollableBlockIncrement(java.awt.Rectangle,int,int)
meth public int getScrollableUnitIncrement(java.awt.Rectangle,int,int)
meth public int getSelectedColumn()
meth public int getSelectedColumnCount()
meth public int getSelectedRow()
meth public int getSelectedRowCount()
meth public int rowAtPoint(java.awt.Point)
meth public int[] getSelectedColumns()
meth public int[] getSelectedRows()
meth public java.awt.Color getGridColor()
meth public java.awt.Color getSelectionBackground()
meth public java.awt.Color getSelectionForeground()
meth public java.awt.Component getEditorComponent()
meth public java.awt.Component prepareEditor(javax.swing.table.TableCellEditor,int,int)
meth public java.awt.Component prepareRenderer(javax.swing.table.TableCellRenderer,int,int)
meth public java.awt.Dimension getIntercellSpacing()
meth public java.awt.Dimension getPreferredScrollableViewportSize()
meth public java.awt.Rectangle getCellRect(int,int,boolean)
meth public java.awt.print.Printable getPrintable(javax.swing.JTable$PrintMode,java.text.MessageFormat,java.text.MessageFormat)
meth public java.lang.Class<?> getColumnClass(int)
meth public java.lang.Object getValueAt(int,int)
meth public java.lang.String getColumnName(int)
meth public java.lang.String getToolTipText(java.awt.event.MouseEvent)
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.ListSelectionModel getSelectionModel()
meth public javax.swing.RowSorter<? extends javax.swing.table.TableModel> getRowSorter()
meth public javax.swing.plaf.TableUI getUI()
meth public javax.swing.table.JTableHeader getTableHeader()
meth public javax.swing.table.TableCellEditor getCellEditor()
meth public javax.swing.table.TableCellEditor getCellEditor(int,int)
meth public javax.swing.table.TableCellEditor getDefaultEditor(java.lang.Class<?>)
meth public javax.swing.table.TableCellRenderer getCellRenderer(int,int)
meth public javax.swing.table.TableCellRenderer getDefaultRenderer(java.lang.Class<?>)
meth public javax.swing.table.TableColumn getColumn(java.lang.Object)
meth public javax.swing.table.TableColumnModel getColumnModel()
meth public javax.swing.table.TableModel getModel()
meth public static javax.swing.JScrollPane createScrollPaneForTable(javax.swing.JTable)
 anno 0 java.lang.Deprecated()
meth public void addColumn(javax.swing.table.TableColumn)
meth public void addColumnSelectionInterval(int,int)
meth public void addNotify()
meth public void addRowSelectionInterval(int,int)
meth public void changeSelection(int,int,boolean,boolean)
meth public void clearSelection()
meth public void columnAdded(javax.swing.event.TableColumnModelEvent)
meth public void columnMarginChanged(javax.swing.event.ChangeEvent)
meth public void columnMoved(javax.swing.event.TableColumnModelEvent)
meth public void columnRemoved(javax.swing.event.TableColumnModelEvent)
meth public void columnSelectionChanged(javax.swing.event.ListSelectionEvent)
meth public void createDefaultColumnsFromModel()
meth public void doLayout()
meth public void editingCanceled(javax.swing.event.ChangeEvent)
meth public void editingStopped(javax.swing.event.ChangeEvent)
meth public void moveColumn(int,int)
meth public void removeColumn(javax.swing.table.TableColumn)
meth public void removeColumnSelectionInterval(int,int)
meth public void removeEditor()
meth public void removeNotify()
meth public void removeRowSelectionInterval(int,int)
meth public void selectAll()
meth public void setAutoCreateColumnsFromModel(boolean)
meth public void setAutoCreateRowSorter(boolean)
meth public void setAutoResizeMode(int)
meth public void setCellEditor(javax.swing.table.TableCellEditor)
meth public void setCellSelectionEnabled(boolean)
meth public void setColumnModel(javax.swing.table.TableColumnModel)
meth public void setColumnSelectionAllowed(boolean)
meth public void setColumnSelectionInterval(int,int)
meth public void setDefaultEditor(java.lang.Class<?>,javax.swing.table.TableCellEditor)
meth public void setDefaultRenderer(java.lang.Class<?>,javax.swing.table.TableCellRenderer)
meth public void setDragEnabled(boolean)
meth public void setEditingColumn(int)
meth public void setEditingRow(int)
meth public void setFillsViewportHeight(boolean)
meth public void setGridColor(java.awt.Color)
meth public void setIntercellSpacing(java.awt.Dimension)
meth public void setModel(javax.swing.table.TableModel)
meth public void setPreferredScrollableViewportSize(java.awt.Dimension)
meth public void setRowHeight(int)
meth public void setRowHeight(int,int)
meth public void setRowMargin(int)
meth public void setRowSelectionAllowed(boolean)
meth public void setRowSelectionInterval(int,int)
meth public void setRowSorter(javax.swing.RowSorter<? extends javax.swing.table.TableModel>)
meth public void setSelectionBackground(java.awt.Color)
meth public void setSelectionForeground(java.awt.Color)
meth public void setSelectionMode(int)
meth public void setSelectionModel(javax.swing.ListSelectionModel)
meth public void setShowGrid(boolean)
meth public void setShowHorizontalLines(boolean)
meth public void setShowVerticalLines(boolean)
meth public void setSurrendersFocusOnKeystroke(boolean)
meth public void setTableHeader(javax.swing.table.JTableHeader)
meth public void setUI(javax.swing.plaf.TableUI)
meth public void setUpdateSelectionOnSort(boolean)
meth public void setValueAt(java.lang.Object,int,int)
meth public void sizeColumnsToFit(boolean)
 anno 0 java.lang.Deprecated()
meth public void sizeColumnsToFit(int)
meth public void sorterChanged(javax.swing.event.RowSorterEvent)
meth public void tableChanged(javax.swing.event.TableModelEvent)
meth public void updateUI()
meth public void valueChanged(javax.swing.event.ListSelectionEvent)
supr javax.swing.JComponent

CLSS public javax.swing.JToggleButton
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,boolean)
cons public init(java.lang.String,javax.swing.Icon)
cons public init(java.lang.String,javax.swing.Icon,boolean)
cons public init(javax.swing.Action)
cons public init(javax.swing.Icon)
cons public init(javax.swing.Icon,boolean)
innr protected AccessibleJToggleButton
innr public static ToggleButtonModel
intf javax.accessibility.Accessible
meth protected java.lang.String paramString()
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public void updateUI()
supr javax.swing.AbstractButton

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

CLSS public abstract interface javax.swing.event.AncestorListener
intf java.util.EventListener
meth public abstract void ancestorAdded(javax.swing.event.AncestorEvent)
meth public abstract void ancestorMoved(javax.swing.event.AncestorEvent)
meth public abstract void ancestorRemoved(javax.swing.event.AncestorEvent)

CLSS public abstract interface javax.swing.event.CellEditorListener
intf java.util.EventListener
meth public abstract void editingCanceled(javax.swing.event.ChangeEvent)
meth public abstract void editingStopped(javax.swing.event.ChangeEvent)

CLSS public abstract interface javax.swing.event.ChangeListener
intf java.util.EventListener
meth public abstract void stateChanged(javax.swing.event.ChangeEvent)

CLSS public abstract interface javax.swing.event.ListSelectionListener
intf java.util.EventListener
meth public abstract void valueChanged(javax.swing.event.ListSelectionEvent)

CLSS public abstract interface javax.swing.event.RowSorterListener
intf java.util.EventListener
meth public abstract void sorterChanged(javax.swing.event.RowSorterEvent)

CLSS public abstract interface javax.swing.event.TableColumnModelListener
intf java.util.EventListener
meth public abstract void columnAdded(javax.swing.event.TableColumnModelEvent)
meth public abstract void columnMarginChanged(javax.swing.event.ChangeEvent)
meth public abstract void columnMoved(javax.swing.event.TableColumnModelEvent)
meth public abstract void columnRemoved(javax.swing.event.TableColumnModelEvent)
meth public abstract void columnSelectionChanged(javax.swing.event.ListSelectionEvent)

CLSS public javax.swing.event.TableModelEvent
cons public init(javax.swing.table.TableModel)
cons public init(javax.swing.table.TableModel,int)
cons public init(javax.swing.table.TableModel,int,int)
cons public init(javax.swing.table.TableModel,int,int,int)
cons public init(javax.swing.table.TableModel,int,int,int,int)
fld protected int column
fld protected int firstRow
fld protected int lastRow
fld protected int type
fld public final static int ALL_COLUMNS = -1
fld public final static int DELETE = -1
fld public final static int HEADER_ROW = -1
fld public final static int INSERT = 1
fld public final static int UPDATE = 0
meth public int getColumn()
meth public int getFirstRow()
meth public int getLastRow()
meth public int getType()
supr java.util.EventObject

CLSS public abstract interface javax.swing.event.TableModelListener
intf java.util.EventListener
meth public abstract void tableChanged(javax.swing.event.TableModelEvent)

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

CLSS public javax.swing.table.DefaultTableCellRenderer
cons public init()
fld protected static javax.swing.border.Border noFocusBorder
innr public static UIResource
intf java.io.Serializable
intf javax.swing.table.TableCellRenderer
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void setValue(java.lang.Object)
meth public boolean isOpaque()
meth public java.awt.Component getTableCellRendererComponent(javax.swing.JTable,java.lang.Object,boolean,boolean,int,int)
meth public void firePropertyChange(java.lang.String,boolean,boolean)
meth public void invalidate()
meth public void repaint()
meth public void repaint(java.awt.Rectangle)
meth public void repaint(long,int,int,int,int)
meth public void revalidate()
meth public void setBackground(java.awt.Color)
meth public void setForeground(java.awt.Color)
meth public void updateUI()
meth public void validate()
supr javax.swing.JLabel

CLSS public abstract interface javax.swing.table.TableCellRenderer
meth public abstract java.awt.Component getTableCellRendererComponent(javax.swing.JTable,java.lang.Object,boolean,boolean,int,int)

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

CLSS public abstract interface org.netbeans.api.actions.Savable
fld public final static org.openide.util.Lookup REGISTRY
meth public abstract java.lang.String toString()
meth public abstract void save() throws java.io.IOException

CLSS public abstract interface org.netbeans.editor.SideBarFactory
 anno 0 java.lang.Deprecated()
 anno 0 org.netbeans.spi.editor.mimelookup.MimeLocation(java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass=class org.netbeans.spi.editor.mimelookup.InstanceProvider, java.lang.String subfolderName="SideBar")
meth public abstract javax.swing.JComponent createSideBar(javax.swing.text.JTextComponent)

CLSS public abstract interface org.netbeans.modules.editor.errorstripe.privatespi.MarkProviderCreator
 anno 0 org.netbeans.spi.editor.mimelookup.MimeLocation(java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass=class org.netbeans.spi.editor.mimelookup.InstanceProvider, java.lang.String subfolderName="UpToDateStatusProvider")
meth public abstract org.netbeans.modules.editor.errorstripe.privatespi.MarkProvider createMarkProvider(javax.swing.text.JTextComponent)

CLSS public org.netbeans.modules.proxy.Base64Encoder
 anno 0 java.lang.Deprecated()
meth public static byte[] decode(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static java.lang.String encode(byte[])
 anno 0 java.lang.Deprecated()
meth public static java.lang.String encode(byte[],boolean)
supr java.lang.Object
hfds MIME_ENCODER

CLSS public org.netbeans.modules.proxy.ConnectivitySettings
cons public init()
fld public final static int CONNECTION_DIRECT = 0
fld public final static int CONNECTION_VIA_HTTPS = 2
fld public final static int CONNECTION_VIA_SOCKS = 1
meth public char[] getProxyPassword()
meth public int getConnectionType()
meth public int getKeepAliveIntervalSeconds()
meth public int getProxyPort()
meth public java.lang.String getProxyHost()
meth public java.lang.String getProxyUsername()
meth public java.lang.String toString()
meth public void setConnectionType(int)
meth public void setKeepAliveIntervalSeconds(int)
meth public void setProxy(int,java.lang.String,int,java.lang.String,char[])
meth public void setProxyHost(java.lang.String)
meth public void setProxyPassword(char[])
meth public void setProxyPort(int)
meth public void setProxyUsername(java.lang.String)
supr java.lang.Object
hfds CONNECTION_TYPE_MAX,CONNECTION_TYPE_MIN,mConnectionType,mKeepAliveIntervalSeconds,mProxyHost,mProxyPassword,mProxyPort,mProxyUsername

CLSS public org.netbeans.modules.proxy.InterruptibleInputStream
cons public init(java.io.InputStream)
meth public int read() throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
supr java.io.FilterInputStream

CLSS public org.netbeans.modules.proxy.ProxySocketFactory
meth public java.net.Socket createSocket() throws java.io.IOException
meth public java.net.Socket createSocket(java.lang.String,int) throws java.io.IOException
meth public java.net.Socket createSocket(java.lang.String,int,java.net.InetAddress,int) throws java.io.IOException
meth public java.net.Socket createSocket(java.net.InetAddress,int) throws java.io.IOException
meth public java.net.Socket createSocket(java.net.InetAddress,int,java.net.InetAddress,int) throws java.io.IOException
meth public static org.netbeans.modules.proxy.ProxySocketFactory getDefault()
supr javax.net.SocketFactory
hfds AUTH_BASIC,AUTH_NONE,CONNECT_TIMEOUT,instance,lastKnownSettings,sConnectionEstablishedPattern,sProxyAuthRequiredPattern

CLSS public abstract org.netbeans.modules.turbo.CacheIndex
cons public init()
meth protected abstract boolean isManaged(java.io.File)
meth public java.io.File[] get(java.io.File)
meth public java.io.File[] getAllValues()
meth public void add(java.io.File)
meth public void add(java.io.File,java.util.Set<java.io.File>)
supr java.lang.Object
hfds LOG,index

CLSS public abstract interface org.netbeans.modules.turbo.CustomProviders
meth public abstract java.util.Iterator providers()

CLSS public final org.netbeans.modules.turbo.Turbo
meth protected void finalize() throws java.lang.Throwable
meth protected void fireEntryChange(java.lang.Object,java.lang.String,java.lang.Object)
meth public boolean isPrepared(java.lang.Object,java.lang.String)
meth public boolean prepareEntry(java.lang.Object,java.lang.String)
meth public boolean writeEntry(java.lang.Object,java.lang.String,java.lang.Object)
meth public java.lang.Object getMonitoredKey(java.lang.Object)
meth public java.lang.Object readEntry(java.lang.Object,java.lang.String)
meth public java.lang.String toString()
meth public static org.netbeans.modules.turbo.Turbo createCustom(org.netbeans.modules.turbo.CustomProviders,int,int)
meth public static org.netbeans.modules.turbo.Turbo getDefault()
meth public void addTurboListener(org.netbeans.modules.turbo.TurboListener)
meth public void removeTurboListener(org.netbeans.modules.turbo.TurboListener)
supr java.lang.Object
hfds customProviders,defaultInstance,env,listeners,memory,preparationTask,prepareRequests,providers,statistics
hcls Environment,PreparationTask,Request

CLSS public abstract interface org.netbeans.modules.turbo.TurboListener
intf java.util.EventListener
meth public abstract void entryChanged(java.lang.Object,java.lang.String,java.lang.Object)

CLSS public abstract interface org.netbeans.modules.turbo.TurboProvider
innr public final static MemoryCache
meth public abstract boolean recognizesAttribute(java.lang.String)
meth public abstract boolean recognizesEntity(java.lang.Object)
meth public abstract boolean writeEntry(java.lang.Object,java.lang.String,java.lang.Object)
meth public abstract java.lang.Object readEntry(java.lang.Object,java.lang.String,org.netbeans.modules.turbo.TurboProvider$MemoryCache)

CLSS public final static org.netbeans.modules.turbo.TurboProvider$MemoryCache
 outer org.netbeans.modules.turbo.TurboProvider
meth public void cacheEntry(java.lang.Object,java.lang.String,java.lang.Object)
supr java.lang.Object
hfds enabled,memory,speculative

CLSS public final org.netbeans.modules.versioning.annotate.AnnotationBarManager
cons public init()
intf org.netbeans.editor.SideBarFactory
meth public javax.swing.JComponent createSideBar(javax.swing.text.JTextComponent)
meth public static boolean annotationBarVisible(javax.swing.text.JTextComponent)
meth public static org.netbeans.modules.versioning.annotate.AnnotationBar showAnnotationBar(javax.swing.text.JTextComponent)
meth public static void hideAnnotationBar(javax.swing.text.JTextComponent)
supr java.lang.Object
hfds BAR_KEY

CLSS public final org.netbeans.modules.versioning.annotate.AnnotationMarkInstaller
cons public init()
intf org.netbeans.modules.editor.errorstripe.privatespi.MarkProviderCreator
meth public org.netbeans.modules.editor.errorstripe.privatespi.MarkProvider createMarkProvider(javax.swing.text.JTextComponent)
meth public static org.netbeans.modules.editor.errorstripe.privatespi.MarkProvider getMarkProvider(javax.swing.text.JTextComponent)
supr java.lang.Object
hfds PROVIDER_KEY

CLSS public org.netbeans.modules.versioning.annotate.VcsAnnotateAction
cons public init(org.netbeans.modules.versioning.spi.VCSContext,org.netbeans.modules.versioning.annotate.VcsAnnotationsProvider)
meth public boolean isEnabled()
meth public boolean visible()
meth public void actionPerformed(java.awt.event.ActionEvent)
supr javax.swing.AbstractAction
hfds nodes,provider

CLSS public final org.netbeans.modules.versioning.annotate.VcsAnnotation
cons public init(int,java.lang.String,java.lang.String,java.util.Date,java.lang.String,java.lang.String)
meth public int getLineNumber()
meth public java.lang.String getAuthor()
meth public java.lang.String getDescription()
meth public java.lang.String getDocumentText()
meth public java.lang.String getRevision()
meth public java.util.Date getDate()
supr java.lang.Object
hfds author,date,description,documentText,lineNumber,revision

CLSS public abstract org.netbeans.modules.versioning.annotate.VcsAnnotations
cons protected init()
meth protected final void fireAnnotationsChanged()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public javax.swing.Action[] getActions(org.netbeans.modules.versioning.annotate.VcsAnnotation)
meth public org.netbeans.modules.versioning.annotate.VcsAnnotation[] getAnnotations()
supr java.lang.Object
hfds ANNOTATIONS_CHANGED,support

CLSS public abstract org.netbeans.modules.versioning.annotate.VcsAnnotationsProvider
cons public init()
meth public abstract org.netbeans.modules.versioning.annotate.VcsAnnotations getAnnotations()
supr java.lang.Object

CLSS public abstract org.netbeans.modules.versioning.diff.AbstractDiffSetup
cons public init()
meth public abstract org.netbeans.api.diff.StreamSource getFirstSource()
meth public abstract org.netbeans.api.diff.StreamSource getSecondSource()
supr java.lang.Object

CLSS public org.netbeans.modules.versioning.diff.DiffLookup
cons public init()
intf java.beans.PropertyChangeListener
meth protected void setValidatedData(java.lang.Object[])
meth protected void validateData(java.lang.Object[])
meth public void propertyChange(java.beans.PropertyChangeEvent)
supr org.netbeans.modules.versioning.util.SimpleLookup
hfds observableEditorCookie,weakList,withSaveCookie,withoutSaveCookie

CLSS public final org.netbeans.modules.versioning.diff.DiffUtils
meth public static int getMatchingLine(java.io.File,java.io.File,int) throws java.io.IOException
meth public static int getMatchingLine(java.io.Reader,java.io.Reader,int) throws java.io.IOException
meth public static java.lang.String getHtmlDisplayName(org.openide.nodes.Node,boolean,boolean)
meth public static org.netbeans.api.diff.Difference[] getDifferences(java.io.File,java.io.File) throws java.io.IOException
meth public static org.openide.cookies.EditorCookie getEditorCookie(javax.swing.text.Document)
meth public static org.openide.cookies.EditorCookie getEditorCookie(org.netbeans.api.diff.StreamSource)
meth public static org.openide.cookies.EditorCookie getEditorCookie(org.netbeans.modules.versioning.diff.AbstractDiffSetup)
meth public static org.openide.cookies.EditorCookie[] setupsToEditorCookies(org.netbeans.modules.versioning.diff.AbstractDiffSetup[])
meth public static org.openide.loaders.DataObject getDataObject(org.netbeans.api.diff.StreamSource)
meth public static org.openide.loaders.DataObject getDataObject(org.netbeans.modules.versioning.diff.AbstractDiffSetup)
meth public static org.openide.loaders.DataObject[] setupsToDataObjects(org.netbeans.modules.versioning.diff.AbstractDiffSetup[])
meth public static org.openide.loaders.DataObject[] setupsToDataObjects(org.netbeans.modules.versioning.diff.AbstractDiffSetup[],boolean)
meth public static void cleanThoseUnmodified(org.openide.cookies.EditorCookie[])
meth public static void cleanThoseWithEditorPaneOpen(org.openide.cookies.EditorCookie[])
supr java.lang.Object

CLSS public final org.netbeans.modules.versioning.diff.DiffViewModeSwitcher
cons public init()
intf javax.swing.event.ChangeListener
meth public static org.netbeans.modules.versioning.diff.DiffViewModeSwitcher get(java.lang.Object)
meth public static void release(java.lang.Object)
meth public void setupMode(org.netbeans.api.diff.DiffController)
meth public void stateChanged(javax.swing.event.ChangeEvent)
supr java.lang.Object
hfds INSTANCES,diffViewMode,handledViews

CLSS public org.netbeans.modules.versioning.diff.EditorSaveCookie
cons public init(org.openide.cookies.EditorCookie,java.lang.String)
cons public init(org.openide.cookies.EditorCookie,org.openide.filesystems.FileObject)
intf org.openide.cookies.SaveCookie
meth public java.lang.String toString()
meth public void save() throws java.io.IOException
supr java.lang.Object
hfds editorCookie,name

CLSS public org.netbeans.modules.versioning.diff.FilesModifiedConfirmation
cons public init(org.openide.cookies.SaveCookie[])
fld protected final javax.swing.JButton btnSave
fld protected final javax.swing.JButton btnSaveAll
fld protected final javax.swing.JComponent mainComponent
fld protected final javax.swing.JList list
meth protected !varargs java.lang.String getMessage(java.lang.String,java.lang.Object[])
meth protected !varargs static java.lang.String getMessage(java.lang.Class<?>,java.lang.String,java.lang.Object[])
meth protected final void closeDialog(java.lang.Object)
meth protected java.awt.Dialog createDialog(org.openide.DialogDescriptor)
meth protected java.lang.Object getMainDialogComponent()
meth protected java.lang.Object[] getAdditionalOptions()
meth protected java.lang.Object[] getDialogClosingOptions()
meth protected java.lang.Object[] getDialogOptions()
meth protected java.lang.String getDialogTitle()
meth protected java.lang.String getInitialSaveAllButtonText()
meth protected java.lang.String getInitialSaveButtonText()
meth protected java.lang.String save()
meth protected java.util.Collection<java.lang.String> saveAll()
meth protected javax.swing.JButton createSaveAllButton()
meth protected javax.swing.JButton createSaveButton()
meth protected void anotherActionPerformed(java.lang.Object)
meth protected void handleSaveAllFailed(java.util.Collection<java.lang.String>)
meth protected void handleSaveAllSucceeded()
meth protected void handleSaveFailed(java.lang.String)
meth protected void listSelectionChanged()
meth protected void pressedSave()
meth protected void pressedSaveAll()
meth protected void savedLastFile()
meth protected void showSaveError(java.lang.String)
meth protected void tuneDialogDescriptor(org.openide.DialogDescriptor)
meth public final java.lang.Object displayDialog()
supr java.lang.Object
hfds PROTOTYPE_LIST_CELL_VALUE,descriptor,dialog,listModel,listener
hcls ArrayListModel,ListCellRenderer,Listener

CLSS public org.netbeans.modules.versioning.diff.SaveBeforeClosingDiffConfirmation
meth protected java.lang.Object[] getDialogClosingOptions()
meth protected java.lang.Object[] getDialogOptions()
meth public static boolean allSaved(org.openide.cookies.SaveCookie[])
supr org.netbeans.modules.versioning.diff.FilesModifiedConfirmation
hfds btnKeepModifications

CLSS public org.netbeans.modules.versioning.diff.SaveBeforeCommitConfirmation
meth protected java.lang.String getInitialSaveAllButtonText()
meth protected javax.swing.JButton createSaveAllButton()
meth protected void handleSaveAllFailed(java.util.Collection<java.lang.String>)
meth protected void savedLastFile()
meth public static boolean allSaved(org.openide.cookies.SaveCookie[])
supr org.netbeans.modules.versioning.diff.FilesModifiedConfirmation
hfds commitText,saveAllAndCommitText

CLSS public abstract org.netbeans.modules.versioning.history.AbstractSummaryView
cons public init(org.netbeans.modules.versioning.history.AbstractSummaryView$SummaryViewMaster,java.util.List<? extends org.netbeans.modules.versioning.history.AbstractSummaryView$LogEntry>,java.util.Map<java.lang.String,org.netbeans.modules.versioning.util.VCSKenaiAccessor$KenaiUser>)
fld public final static java.lang.String PROP_REVISIONS_ADDED = "propRevisionsAdded"
innr public abstract interface static SummaryViewMaster
innr public abstract static LogEntry
intf java.awt.event.MouseListener
intf java.awt.event.MouseMotionListener
intf java.beans.PropertyChangeListener
meth protected abstract void onPopup(javax.swing.JComponent,java.awt.Point,java.lang.Object[])
meth protected final java.lang.Object[] getSelection()
meth public final void refreshView()
meth public final void requestFocusInWindow()
meth public javax.swing.JComponent getComponent()
meth public void entriesChanged(java.util.List<? extends org.netbeans.modules.versioning.history.AbstractSummaryView$LogEntry>)
meth public void mouseClicked(java.awt.event.MouseEvent)
meth public void mouseDragged(java.awt.event.MouseEvent)
meth public void mouseEntered(java.awt.event.MouseEvent)
meth public void mouseExited(java.awt.event.MouseEvent)
meth public void mouseMoved(java.awt.event.MouseEvent)
meth public void mousePressed(java.awt.event.MouseEvent)
meth public void mouseReleased(java.awt.event.MouseEvent)
meth public void propertyChange(java.beans.PropertyChangeEvent)
supr java.lang.Object
hfds LOG,NEXT_FILES_INITIAL_PAGING,expandCollapseAction,linkerSupport,list,master,resultsList,scrollPane
hcls ActionsItem,CollapseAction,EventItem,ExpandAction,ExpandCollapseAction,ExpandCollapseGeneralAction,Item,LoadingEventsItem,MoreRevisionsItem,RevisionItem,ShowAllEventsItem,ShowLessEventsItem,SummaryListModel

CLSS public abstract static org.netbeans.modules.versioning.history.AbstractSummaryView$LogEntry
 outer org.netbeans.modules.versioning.history.AbstractSummaryView
cons public init()
fld public final static java.lang.String PROP_EVENTS_CHANGED = "propEventsChanged"
innr public abstract static Event
innr public final static RevisionHighlight
meth protected abstract boolean isEventsInitialized()
meth protected abstract boolean isLessInteresting()
meth protected abstract java.util.Collection<org.netbeans.modules.versioning.history.AbstractSummaryView$LogEntry$RevisionHighlight> getRevisionHighlights()
meth protected abstract void cancelExpand()
meth protected abstract void expand()
meth protected final void eventsChanged(java.util.List<? extends org.netbeans.modules.versioning.history.AbstractSummaryView$LogEntry$Event>,java.util.List<? extends org.netbeans.modules.versioning.history.AbstractSummaryView$LogEntry$Event>)
meth public abstract boolean isVisible()
meth public abstract java.lang.String getAuthor()
meth public abstract java.lang.String getDate()
meth public abstract java.lang.String getMessage()
meth public abstract java.lang.String getRevision()
meth public abstract java.util.Collection<org.netbeans.modules.versioning.history.AbstractSummaryView$LogEntry$Event> getEvents()
meth public abstract javax.swing.Action[] getActions()
meth public final void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public java.util.Collection<org.netbeans.modules.versioning.history.AbstractSummaryView$LogEntry$Event> getDummyEvents()
supr java.lang.Object
hfds support

CLSS public abstract static org.netbeans.modules.versioning.history.AbstractSummaryView$LogEntry$Event
 outer org.netbeans.modules.versioning.history.AbstractSummaryView$LogEntry
cons public init()
meth public abstract boolean isVisibleByDefault()
meth public abstract java.lang.String getAction()
meth public abstract java.lang.String getOriginalPath()
meth public abstract java.lang.String getPath()
meth public abstract javax.swing.Action[] getUserActions()
supr java.lang.Object

CLSS public final static org.netbeans.modules.versioning.history.AbstractSummaryView$LogEntry$RevisionHighlight
 outer org.netbeans.modules.versioning.history.AbstractSummaryView$LogEntry
cons public init(int,int,java.awt.Color,java.awt.Color)
meth public int getLength()
meth public int getStart()
meth public java.awt.Color getBackground()
meth public java.awt.Color getForeground()
supr java.lang.Object
hfds background,foreground,length,start

CLSS public abstract interface static org.netbeans.modules.versioning.history.AbstractSummaryView$SummaryViewMaster
 outer org.netbeans.modules.versioning.history.AbstractSummaryView
innr public final static SearchHighlight
meth public abstract boolean hasMoreResults()
meth public abstract java.io.File[] getRoots()
meth public abstract java.util.Collection<org.netbeans.modules.versioning.history.AbstractSummaryView$SummaryViewMaster$SearchHighlight> getSearchHighlights()
meth public abstract java.util.Map<java.lang.String,java.lang.String> getActionColors()
meth public abstract javax.swing.JComponent getComponent()
meth public abstract void getMoreResults(java.beans.PropertyChangeListener,int)

CLSS public final static org.netbeans.modules.versioning.history.AbstractSummaryView$SummaryViewMaster$SearchHighlight
 outer org.netbeans.modules.versioning.history.AbstractSummaryView$SummaryViewMaster
cons public init(org.netbeans.modules.versioning.history.AbstractSummaryView$SummaryViewMaster$SearchHighlight$Kind,java.lang.String)
innr public final static !enum Kind
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getSearchText()
meth public org.netbeans.modules.versioning.history.AbstractSummaryView$SummaryViewMaster$SearchHighlight$Kind getKind()
supr java.lang.Object
hfds kind,searchText

CLSS public final static !enum org.netbeans.modules.versioning.history.AbstractSummaryView$SummaryViewMaster$SearchHighlight$Kind
 outer org.netbeans.modules.versioning.history.AbstractSummaryView$SummaryViewMaster$SearchHighlight
fld public final static org.netbeans.modules.versioning.history.AbstractSummaryView$SummaryViewMaster$SearchHighlight$Kind AUTHOR
fld public final static org.netbeans.modules.versioning.history.AbstractSummaryView$SummaryViewMaster$SearchHighlight$Kind FILE
fld public final static org.netbeans.modules.versioning.history.AbstractSummaryView$SummaryViewMaster$SearchHighlight$Kind MESSAGE
fld public final static org.netbeans.modules.versioning.history.AbstractSummaryView$SummaryViewMaster$SearchHighlight$Kind REVISION
meth public static org.netbeans.modules.versioning.history.AbstractSummaryView$SummaryViewMaster$SearchHighlight$Kind valueOf(java.lang.String)
meth public static org.netbeans.modules.versioning.history.AbstractSummaryView$SummaryViewMaster$SearchHighlight$Kind[] values()
supr java.lang.Enum<org.netbeans.modules.versioning.history.AbstractSummaryView$SummaryViewMaster$SearchHighlight$Kind>

CLSS public abstract org.netbeans.modules.versioning.history.HistoryAction
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,boolean)
meth protected abstract void perform(org.netbeans.modules.versioning.spi.VCSHistoryProvider$HistoryEntry,java.util.Set<java.io.File>)
meth protected boolean enable(org.openide.nodes.Node[])
meth protected boolean isMultipleHistory()
meth protected java.lang.String getRevisionShort()
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.NodeAction
hfds callback,context,multipleHistory,name,support
hcls HistoryEntryImpl

CLSS public abstract org.netbeans.modules.versioning.history.HistoryActionVCSProxyBased
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,boolean)
meth protected abstract void perform(org.netbeans.modules.versioning.core.spi.VCSHistoryProvider$HistoryEntry,java.util.Set<org.netbeans.modules.versioning.core.api.VCSFileProxy>)
meth protected boolean enable(org.openide.nodes.Node[])
meth protected boolean isMultipleHistory()
meth protected java.lang.String getRevisionShort()
meth protected org.netbeans.modules.versioning.history.HistoryActionSupport$Callback<org.netbeans.modules.versioning.core.spi.VCSHistoryProvider$HistoryEntry> getCallback()
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
supr org.openide.util.actions.NodeAction
hfds context,multipleHistory,name,support
hcls HistoryEntryImpl

CLSS public org.netbeans.modules.versioning.history.LinkButton
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,javax.swing.Icon)
cons public init(javax.swing.Action)
cons public init(javax.swing.Icon)
intf java.awt.event.MouseListener
meth protected boolean isVisited()
meth protected void onMouseEntered(java.awt.event.MouseEvent)
meth protected void onMouseExited(java.awt.event.MouseEvent)
meth protected void paintComponent(java.awt.Graphics)
meth public void mouseClicked(java.awt.event.MouseEvent)
meth public void mouseEntered(java.awt.event.MouseEvent)
meth public void mouseExited(java.awt.event.MouseEvent)
meth public void mousePressed(java.awt.event.MouseEvent)
meth public void mouseReleased(java.awt.event.MouseEvent)
meth public void paint(java.awt.Graphics)
supr javax.swing.JButton
hfds BUTTON_FONT,LINK_COLOR,LINK_IN_FOCUS_COLOR,LINK_IN_FOCUS_STROKE,MOUSE_OVER_LINK_COLOR,VISITED_LINK_COLOR,underline

CLSS public org.netbeans.modules.versioning.history.RevisionItemCell
cons public init()
intf org.netbeans.modules.versioning.util.VCSHyperlinkSupport$BoundsTranslator
meth public javax.swing.JPanel getNorthPanel()
meth public javax.swing.JTextPane getAuthorControl()
meth public javax.swing.JTextPane getCommitMessageControl()
meth public javax.swing.JTextPane getDateControl()
meth public javax.swing.JTextPane getRevisionControl()
meth public void correctTranslation(java.awt.Container,java.awt.Rectangle)
supr javax.swing.JPanel
hfds authorControl,authorDatePanel,commitMessageControl,dateControl,northPanel,revisionControl

CLSS public org.netbeans.modules.versioning.historystore.Storage
meth public byte[] getRevisionInfo(java.lang.String)
meth public java.io.File getContent(java.lang.String,java.lang.String,java.lang.String)
meth public void setContent(java.lang.String,java.lang.String,java.io.File)
meth public void setContent(java.lang.String,java.lang.String,java.io.InputStream)
meth public void setRevisionInfo(java.lang.String,java.io.InputStream)
supr java.lang.Object
hfds DATA_FILE,FILE_FILTER,INDEFINITE_TTL,KIND_FILE_CONTENT,KIND_REVISION_INFO,PREF_KEY_TTL,REVISION_CONTENT_FN,storageAccessible,storageExists,storageFolder
hcls StoreDataFile

CLSS public org.netbeans.modules.versioning.historystore.StorageManager
meth public org.netbeans.modules.versioning.historystore.Storage getStorage(java.lang.String)
meth public static org.netbeans.modules.versioning.historystore.StorageManager getInstance()
supr java.lang.Object
hfds INSTANCE,LOG,storages

CLSS public abstract org.netbeans.modules.versioning.hooks.GitHook
cons public init()
meth public org.netbeans.modules.versioning.hooks.GitHookContext beforeCommit(org.netbeans.modules.versioning.hooks.GitHookContext) throws java.io.IOException
meth public org.netbeans.modules.versioning.hooks.GitHookContext beforePush(org.netbeans.modules.versioning.hooks.GitHookContext) throws java.io.IOException
meth public void afterCommit(org.netbeans.modules.versioning.hooks.GitHookContext)
meth public void afterCommitReplace(org.netbeans.modules.versioning.hooks.GitHookContext,org.netbeans.modules.versioning.hooks.GitHookContext,java.util.Map<java.lang.String,java.lang.String>)
meth public void afterPush(org.netbeans.modules.versioning.hooks.GitHookContext)
supr org.netbeans.modules.versioning.hooks.VCSHook<org.netbeans.modules.versioning.hooks.GitHookContext>

CLSS public org.netbeans.modules.versioning.hooks.GitHookContext
cons public !varargs init(java.io.File[],java.lang.String,org.netbeans.modules.versioning.hooks.GitHookContext$LogEntry[])
innr public static LogEntry
meth public java.lang.String getMessage()
meth public java.lang.String getWarning()
meth public org.netbeans.modules.versioning.hooks.GitHookContext$LogEntry[] getLogEntries()
meth public void setWarning(java.lang.String)
supr org.netbeans.modules.versioning.hooks.VCSHookContext
hfds logEntry,msg,warning

CLSS public static org.netbeans.modules.versioning.hooks.GitHookContext$LogEntry
 outer org.netbeans.modules.versioning.hooks.GitHookContext
cons public init(java.lang.String,java.lang.String,java.lang.String,java.util.Date)
meth public java.lang.String getAuthor()
meth public java.lang.String getChangeset()
meth public java.lang.String getMessage()
meth public java.util.Date getDate()
supr java.lang.Object
hfds author,date,message,revision

CLSS public abstract org.netbeans.modules.versioning.hooks.GitHookFactory
cons public init()
meth public abstract org.netbeans.modules.versioning.hooks.GitHook createHook()
meth public java.lang.Class<org.netbeans.modules.versioning.hooks.GitHook> getHookType()
supr org.netbeans.modules.versioning.hooks.VCSHookFactory<org.netbeans.modules.versioning.hooks.GitHook>

CLSS public abstract org.netbeans.modules.versioning.hooks.HgHook
cons public init()
meth public org.netbeans.modules.versioning.hooks.HgHookContext beforeCommit(org.netbeans.modules.versioning.hooks.HgHookContext) throws java.io.IOException
meth public org.netbeans.modules.versioning.hooks.HgHookContext beforePush(org.netbeans.modules.versioning.hooks.HgHookContext) throws java.io.IOException
meth public void afterCommit(org.netbeans.modules.versioning.hooks.HgHookContext)
meth public void afterCommitReplace(org.netbeans.modules.versioning.hooks.HgHookContext,org.netbeans.modules.versioning.hooks.HgHookContext,java.util.Map<java.lang.String,java.lang.String>)
meth public void afterPush(org.netbeans.modules.versioning.hooks.HgHookContext)
supr org.netbeans.modules.versioning.hooks.VCSHook<org.netbeans.modules.versioning.hooks.HgHookContext>

CLSS public org.netbeans.modules.versioning.hooks.HgHookContext
cons public !varargs init(java.io.File[],java.lang.String,org.netbeans.modules.versioning.hooks.HgHookContext$LogEntry[])
innr public static LogEntry
meth public java.lang.String getMessage()
meth public java.lang.String getWarning()
meth public org.netbeans.modules.versioning.hooks.HgHookContext$LogEntry[] getLogEntries()
meth public void setWarning(java.lang.String)
supr org.netbeans.modules.versioning.hooks.VCSHookContext
hfds logEntry,msg,warning

CLSS public static org.netbeans.modules.versioning.hooks.HgHookContext$LogEntry
 outer org.netbeans.modules.versioning.hooks.HgHookContext
cons public init(java.lang.String,java.lang.String,java.lang.String,java.util.Date)
meth public java.lang.String getAuthor()
meth public java.lang.String getChangeset()
meth public java.lang.String getMessage()
meth public java.util.Date getDate()
supr java.lang.Object
hfds author,date,message,revision

CLSS public abstract org.netbeans.modules.versioning.hooks.HgHookFactory
cons public init()
meth public abstract org.netbeans.modules.versioning.hooks.HgHook createHook()
meth public java.lang.Class<org.netbeans.modules.versioning.hooks.HgHook> getHookType()
supr org.netbeans.modules.versioning.hooks.VCSHookFactory<org.netbeans.modules.versioning.hooks.HgHook>

CLSS public abstract org.netbeans.modules.versioning.hooks.HgQueueHook
cons public init()
meth public org.netbeans.modules.versioning.hooks.HgQueueHookContext beforePatchFinish(org.netbeans.modules.versioning.hooks.HgQueueHookContext) throws java.io.IOException
meth public org.netbeans.modules.versioning.hooks.HgQueueHookContext beforePatchRefresh(org.netbeans.modules.versioning.hooks.HgQueueHookContext) throws java.io.IOException
meth public void afterPatchFinish(org.netbeans.modules.versioning.hooks.HgQueueHookContext)
meth public void afterPatchRefresh(org.netbeans.modules.versioning.hooks.HgQueueHookContext)
supr org.netbeans.modules.versioning.hooks.VCSHook<org.netbeans.modules.versioning.hooks.HgQueueHookContext>

CLSS public final org.netbeans.modules.versioning.hooks.HgQueueHookContext
cons public !varargs init(java.io.File[],java.lang.String,java.lang.String,org.netbeans.modules.versioning.hooks.HgHookContext$LogEntry[])
meth public java.lang.String getPatchId()
supr org.netbeans.modules.versioning.hooks.HgHookContext
hfds patchId

CLSS public abstract org.netbeans.modules.versioning.hooks.HgQueueHookFactory
cons public init()
meth public abstract org.netbeans.modules.versioning.hooks.HgQueueHook createHook()
meth public java.lang.Class<org.netbeans.modules.versioning.hooks.HgQueueHook> getHookType()
supr org.netbeans.modules.versioning.hooks.VCSHookFactory<org.netbeans.modules.versioning.hooks.HgQueueHook>

CLSS public abstract org.netbeans.modules.versioning.hooks.SvnHook
cons public init()
meth public org.netbeans.modules.versioning.hooks.SvnHookContext beforeCommit(org.netbeans.modules.versioning.hooks.SvnHookContext) throws java.io.IOException
meth public void afterCommit(org.netbeans.modules.versioning.hooks.SvnHookContext)
supr org.netbeans.modules.versioning.hooks.VCSHook<org.netbeans.modules.versioning.hooks.SvnHookContext>

CLSS public org.netbeans.modules.versioning.hooks.SvnHookContext
cons public init(java.io.File[],java.lang.String,java.util.List<org.netbeans.modules.versioning.hooks.SvnHookContext$LogEntry>)
innr public static LogEntry
meth public java.lang.String getMessage()
meth public java.lang.String getWarning()
meth public java.util.List<org.netbeans.modules.versioning.hooks.SvnHookContext$LogEntry> getLogEntries()
meth public void setWarning(java.lang.String)
supr org.netbeans.modules.versioning.hooks.VCSHookContext
hfds logEntries,msg,warning

CLSS public static org.netbeans.modules.versioning.hooks.SvnHookContext$LogEntry
 outer org.netbeans.modules.versioning.hooks.SvnHookContext
cons public init(java.lang.String,java.lang.String,long,java.util.Date)
meth public java.lang.String getAuthor()
meth public java.lang.String getMessage()
meth public java.util.Date getDate()
meth public long getRevision()
supr java.lang.Object
hfds author,date,message,revision

CLSS public abstract org.netbeans.modules.versioning.hooks.SvnHookFactory
cons public init()
meth public abstract org.netbeans.modules.versioning.hooks.SvnHook createHook()
meth public java.lang.Class<org.netbeans.modules.versioning.hooks.SvnHook> getHookType()
supr org.netbeans.modules.versioning.hooks.VCSHookFactory<org.netbeans.modules.versioning.hooks.SvnHook>

CLSS public abstract org.netbeans.modules.versioning.hooks.VCSHook<%0 extends org.netbeans.modules.versioning.hooks.VCSHookContext>
cons public init()
meth public abstract java.lang.String getDisplayName()
meth public abstract javax.swing.JPanel createComponent({org.netbeans.modules.versioning.hooks.VCSHook%0})
supr java.lang.Object

CLSS public org.netbeans.modules.versioning.hooks.VCSHookContext
cons public init(java.io.File[])
meth public java.io.File[] getFiles()
supr java.lang.Object
hfds files

CLSS public abstract org.netbeans.modules.versioning.hooks.VCSHookFactory<%0 extends org.netbeans.modules.versioning.hooks.VCSHook>
cons public init()
meth public abstract java.lang.Class<{org.netbeans.modules.versioning.hooks.VCSHookFactory%0}> getHookType()
meth public abstract {org.netbeans.modules.versioning.hooks.VCSHookFactory%0} createHook()
supr java.lang.Object

CLSS public org.netbeans.modules.versioning.hooks.VCSHooks
meth public <%0 extends org.netbeans.modules.versioning.hooks.VCSHook> java.util.Collection<{%%0}> getHooks(java.lang.Class<{%%0}>)
meth public static org.netbeans.modules.versioning.hooks.VCSHooks getInstance()
supr java.lang.Object
hfds hooksResult,instance

CLSS public final org.netbeans.modules.versioning.shelve.ShelveChangesActionsRegistry
innr public abstract static ShelveChangesActionProvider
meth public org.netbeans.modules.versioning.shelve.ShelveChangesActionsRegistry$ShelveChangesActionProvider getActionProvider(org.netbeans.modules.versioning.spi.VersioningSystem)
meth public static org.netbeans.modules.versioning.shelve.ShelveChangesActionsRegistry getInstance()
meth public void registerAction(org.netbeans.modules.versioning.spi.VersioningSystem,org.netbeans.modules.versioning.shelve.ShelveChangesActionsRegistry$ShelveChangesActionProvider)
supr java.lang.Object
hfds actionProviders,instance

CLSS public abstract static org.netbeans.modules.versioning.shelve.ShelveChangesActionsRegistry$ShelveChangesActionProvider
 outer org.netbeans.modules.versioning.shelve.ShelveChangesActionsRegistry
cons public init()
meth public abstract javax.swing.Action getAction()
meth public javax.swing.JComponent[] getUnshelveActions(org.netbeans.modules.versioning.spi.VCSContext,boolean)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.versioning.shelve.ShelveChangesSupport
cons public init()
meth protected abstract boolean isCanceled()
meth protected abstract void exportPatch(java.io.File,java.io.File) throws java.io.IOException
meth protected abstract void postExportCleanup()
meth public final boolean prepare(java.lang.String)
meth public final boolean prepare(javax.swing.JPanel,java.lang.String)
meth public final void shelveChanges(java.io.File[])
supr java.lang.Object
hfds PATCH_NAME_PATTERN,patchName
hcls PatchNameListener

CLSS public final org.netbeans.modules.versioning.shelve.ShelveUtils
meth public static java.util.List<javax.swing.JComponent> getShelveMenuItems(org.netbeans.modules.versioning.spi.VCSContext,org.openide.util.Lookup)
supr java.lang.Object

CLSS public org.netbeans.modules.versioning.util.AccessibleJFileChooser
cons public init(java.lang.String)
cons public init(java.lang.String,java.io.File)
meth protected javax.swing.JDialog createDialog(java.awt.Component)
supr javax.swing.JFileChooser
hfds acsd

CLSS public org.netbeans.modules.versioning.util.AutoResizingPanel
cons public init()
cons public init(boolean)
cons public init(java.awt.LayoutManager)
cons public init(java.awt.LayoutManager,boolean)
meth public java.awt.Dimension getPreferredSize()
meth public void enlargeAsNecessary()
meth public void enlargeHorizontallyAsNecessary()
meth public void enlargeVerticallyAsNecessary()
supr javax.swing.JPanel
hfds requestedSize

CLSS public final org.netbeans.modules.versioning.util.CollectionUtils
meth public !varargs static <%0 extends java.lang.Object> java.util.List<{%%0}> unmodifiableList({%%0}[])
meth public !varargs static <%0 extends java.lang.Object> java.util.Set<{%%0}> unmodifiableSet({%%0}[])
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> {%%0}[] retainAll({%%0}[],{%%1}[])
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> {%%0}[] retainAllByIdentity({%%0}[],{%%1}[])
meth public static <%0 extends java.lang.Object, %1 extends {%%0}> int findInArray({%%0}[],{%%1})
meth public static <%0 extends java.lang.Object, %1 extends {%%0}> {%%0}[] removeItem({%%0}[],{%%1})
meth public static <%0 extends java.lang.Object> {%%0}[] appendItem({%%0}[],{%%0})
meth public static <%0 extends java.lang.Object> {%%0}[] copyArray({%%0}[])
meth public static <%0 extends java.lang.Object> {%%0}[] makeArray({%%0}[],int)
meth public static <%0 extends java.lang.Object> {%%0}[] removeDuplicates({%%0}[])
meth public static <%0 extends java.lang.Object> {%%0}[] removeItem({%%0}[],int)
meth public static <%0 extends java.lang.Object> {%%0}[] removeNulls({%%0}[])
meth public static <%0 extends java.lang.Object> {%%0}[] shortenArray({%%0}[],int)
meth public static <%0 extends java.lang.Object> {%%0}[] stripTrailingNulls({%%0}[])
meth public static boolean containEqualObjects(java.lang.Object[],java.lang.Object[])
meth public static boolean containSameObjects(java.lang.Object[],java.lang.Object[])
supr java.lang.Object
hcls ArrayListIterator,Counter,IdentityComparator,UnmodifiableArraySet

CLSS public org.netbeans.modules.versioning.util.CommandReport
cons public init(java.lang.String,java.util.List)
supr javax.swing.JPanel
hfds jLabel1,jScrollPane1,text

CLSS public final org.netbeans.modules.versioning.util.DelayScanRegistry
meth public boolean isDelayed(org.openide.util.RequestProcessor$Task,java.util.logging.Logger,java.lang.String)
meth public static org.netbeans.modules.versioning.util.DelayScanRegistry getInstance()
supr java.lang.Object
hfds BLOCK_INDEFINITELY,MAX_WAITING_TIME,WAITING_PERIOD,instance,registry
hcls DelayedScan

CLSS public org.netbeans.modules.versioning.util.DelegatingUndoRedo
cons public init()
intf java.beans.PropertyChangeListener
intf javax.swing.event.ChangeListener
intf org.openide.awt.UndoRedo
meth public boolean canRedo()
meth public boolean canUndo()
meth public java.lang.String getRedoPresentationName()
meth public java.lang.String getUndoPresentationName()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void redo()
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void setDiffView(javax.swing.JComponent)
meth public void stateChanged(javax.swing.event.ChangeEvent)
meth public void undo()
supr java.lang.Object
hfds comp,delegate,listeners

CLSS public org.netbeans.modules.versioning.util.DialogBoundsPreserver
cons public init(java.util.prefs.Preferences,java.lang.String)
intf java.awt.event.WindowListener
meth public void windowActivated(java.awt.event.WindowEvent)
meth public void windowClosed(java.awt.event.WindowEvent)
meth public void windowClosing(java.awt.event.WindowEvent)
meth public void windowDeactivated(java.awt.event.WindowEvent)
meth public void windowDeiconified(java.awt.event.WindowEvent)
meth public void windowIconified(java.awt.event.WindowEvent)
meth public void windowOpened(java.awt.event.WindowEvent)
supr java.lang.Object
hfds DELIMITER,key,preferences

CLSS public org.netbeans.modules.versioning.util.ExportDiffPanel
cons public init(javax.swing.JComponent)
intf java.awt.event.ActionListener
meth public boolean isFileOutputSelected()
meth public java.lang.String getOutputFileText()
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void addBrowseActionListener(java.awt.event.ActionListener)
meth public void addNotify()
meth public void addOutputFileTextDocumentListener(javax.swing.event.DocumentListener)
meth public void setOutputFileText(java.lang.String)
supr org.netbeans.modules.versioning.util.ExportDiffSupport$AbstractExportDiffPanel
hfds asFileRadioButton,attachComponent,attachPanel,attachRadioButton,browseButton,buttonGroup1,fileTextField

CLSS public abstract org.netbeans.modules.versioning.util.ExportDiffSupport
cons public init(java.io.File[],java.util.prefs.Preferences)
innr public abstract static AbstractExportDiffPanel
innr public abstract static ExportDiffProvider
meth protected java.io.File createTempFile() throws java.io.IOException
meth protected java.io.File getTargetFile(java.io.File)
meth protected java.lang.String getMessage(java.lang.String)
meth protected javax.swing.filechooser.FileFilter getFileFilter()
meth protected org.netbeans.modules.versioning.util.ExportDiffSupport$AbstractExportDiffPanel createSimpleDialog(java.lang.String)
meth protected org.openide.DialogDescriptor getDialogDescriptor()
meth protected void createComplexDialog(org.netbeans.modules.versioning.util.ExportDiffSupport$AbstractExportDiffPanel)
meth public abstract void writeDiffFile(java.io.File)
meth public void export()
supr java.lang.Object
hfds dd,dialog,exportDiffProvider,files,panel,preferences

CLSS public abstract static org.netbeans.modules.versioning.util.ExportDiffSupport$AbstractExportDiffPanel
 outer org.netbeans.modules.versioning.util.ExportDiffSupport
cons public init()
meth public abstract java.lang.String getOutputFileText()
meth public abstract void addBrowseActionListener(java.awt.event.ActionListener)
meth public abstract void addOutputFileTextDocumentListener(javax.swing.event.DocumentListener)
meth public abstract void setOutputFileText(java.lang.String)
meth public boolean isFileOutputSelected()
supr javax.swing.JPanel

CLSS public abstract static org.netbeans.modules.versioning.util.ExportDiffSupport$ExportDiffProvider
 outer org.netbeans.modules.versioning.util.ExportDiffSupport
cons public init()
meth protected abstract void setContext(java.io.File[])
meth protected void fireDataChanged()
meth public abstract boolean isValid()
meth public abstract javax.swing.JComponent createComponent()
meth public abstract void handleDiffFile(java.io.File)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds EVENT_DATA_CHANGED,support

CLSS public org.netbeans.modules.versioning.util.FileCollection
cons public init()
meth public boolean contains(java.io.File)
meth public void add(java.io.File)
meth public void load(java.util.prefs.Preferences,java.lang.String)
meth public void remove(java.io.File)
meth public void save(java.util.prefs.Preferences,java.lang.String)
supr java.lang.Object
hfds FLAT_FOLDER_MARKER,storage

CLSS public org.netbeans.modules.versioning.util.FilePathCellRenderer
cons public init()
meth protected void paintComponent(java.awt.Graphics)
supr javax.swing.table.DefaultTableCellRenderer
hfds VISIBLE_START_CHARS

CLSS public org.netbeans.modules.versioning.util.FileSelector
cons public init(java.lang.String,java.lang.String,org.openide.util.HelpCtx,java.util.prefs.Preferences)
intf javax.swing.event.ListSelectionListener
meth public boolean show(java.io.File[])
meth public java.io.File getSelectedFile()
meth public java.lang.String getFileSelectorPreset(java.lang.String)
meth public void setFileSelectorPreset(java.lang.String,java.lang.String)
meth public void valueChanged(javax.swing.event.ListSelectionEvent)
supr javax.swing.JPanel
hfds FILE_SELECTOR_PREFIX,LOG,cancelButton,dialogDescriptor,filesList,helpCtx,jLabel1,jScrollPane1,okButton,pref,text,title

CLSS public org.netbeans.modules.versioning.util.FileUtils
meth public static byte[] getFileContentsAsByteArray(java.io.File) throws java.io.IOException
meth public static java.io.BufferedInputStream createInputStream(java.io.File) throws java.io.IOException
meth public static java.io.BufferedOutputStream createOutputStream(java.io.File) throws java.io.IOException
meth public static java.io.File createTmpFolder(java.lang.String)
meth public static java.io.File generateTemporaryFile(java.io.File,java.lang.String)
meth public static java.lang.String getExistingFilenameInParent(java.io.File)
meth public static void copyDirFiles(java.io.File,java.io.File)
meth public static void copyDirFiles(java.io.File,java.io.File,boolean)
meth public static void copyFile(java.io.File,java.io.File) throws java.io.IOException
meth public static void copyStreamToFile(java.io.InputStream,java.io.File) throws java.io.IOException
meth public static void deleteRecursively(java.io.File)
meth public static void renameFile(java.io.File,java.io.File) throws java.io.IOException
supr java.lang.Object

CLSS public org.netbeans.modules.versioning.util.IdentityHashSet<%0 extends java.lang.Object>
cons public init()
cons public init(int)
cons public init(java.util.Collection<? extends {org.netbeans.modules.versioning.util.IdentityHashSet%0}>)
cons public init({org.netbeans.modules.versioning.util.IdentityHashSet%0}[])
meth protected java.lang.Object clone()
meth public boolean add({org.netbeans.modules.versioning.util.IdentityHashSet%0})
meth public boolean addAll(java.util.Collection<? extends {org.netbeans.modules.versioning.util.IdentityHashSet%0}>)
meth public boolean addAll({org.netbeans.modules.versioning.util.IdentityHashSet%0}[])
meth public boolean contains(java.lang.Object)
meth public boolean containsAll(java.util.Collection<?>)
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public boolean remove(java.lang.Object)
meth public boolean removeAll(java.util.Collection<?>)
meth public int hashCode()
meth public int size()
meth public java.util.Iterator<{org.netbeans.modules.versioning.util.IdentityHashSet%0}> iterator()
meth public void clear()
supr java.util.AbstractSet<{org.netbeans.modules.versioning.util.IdentityHashSet%0}>
hfds DEFAULT_CAPACITY,MAXIMUM_CAPACITY,MINIMUM_CAPACITY,modCount,size,table,threshold
hcls IdentityHashSetIterator

CLSS public final org.netbeans.modules.versioning.util.IndexingBridge
innr public abstract interface static IndexingBridgeProvider
meth public !varargs <%0 extends java.lang.Object> {%%0} runWithoutIndexing(java.util.concurrent.Callable<{%%0}>,boolean,java.io.File[]) throws java.lang.Exception
meth public !varargs <%0 extends java.lang.Object> {%%0} runWithoutIndexing(java.util.concurrent.Callable<{%%0}>,java.io.File[]) throws java.lang.Exception
meth public boolean isIndexingInProgress()
meth public static org.netbeans.modules.versioning.util.IndexingBridge getInstance()
supr java.lang.Object
hfds DEFAULT_DELAY,LOG,delayBeforeRefresh,instance

CLSS public abstract interface static org.netbeans.modules.versioning.util.IndexingBridge$IndexingBridgeProvider
 outer org.netbeans.modules.versioning.util.IndexingBridge
meth public abstract !varargs <%0 extends java.lang.Object> {%%0} runWithoutIndexing(java.util.concurrent.Callable<{%%0}>,java.io.File[]) throws java.lang.Exception
meth public abstract boolean isIndexingInProgress()

CLSS public org.netbeans.modules.versioning.util.KeyringSupport
cons public init()
meth public static char[] read(java.lang.String,java.lang.String)
meth public static void save(java.lang.String,java.lang.String,char[],java.lang.String)
supr java.lang.Object
hfds LOG,NULL_HASHED_RECORDS,PRINT_PASSWORDS

CLSS public org.netbeans.modules.versioning.util.ListenersSupport
cons public init(java.lang.Object)
meth public void addListener(org.netbeans.modules.versioning.util.VersioningListener)
meth public void fireVersioningEvent(java.lang.Object)
meth public void fireVersioningEvent(java.lang.Object,java.lang.Object)
meth public void fireVersioningEvent(java.lang.Object,java.lang.Object[])
meth public void removeListener(org.netbeans.modules.versioning.util.VersioningListener)
supr java.lang.Object
hfds listeners,source

CLSS public org.netbeans.modules.versioning.util.NoContentPanel
cons public init()
cons public init(java.lang.String)
meth public void setLabel(java.lang.String)
supr javax.swing.JPanel
hfds label

CLSS public org.netbeans.modules.versioning.util.OpenInEditorAction
cons public init(java.io.File[])
meth public void actionPerformed(java.awt.event.ActionEvent)
supr javax.swing.AbstractAction
hfds files

CLSS public org.netbeans.modules.versioning.util.OpenVersioningOutputAction
cons public init()
meth public boolean isEnabled()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void actionPerformed(java.awt.event.ActionEvent)
supr org.openide.util.actions.SystemAction

CLSS public abstract org.netbeans.modules.versioning.util.OptionsPanelColorProvider
cons protected init()
innr public AnnotationFormat
meth protected abstract java.awt.Color getSavedColor(java.lang.String,java.awt.Color)
meth protected abstract java.text.MessageFormat createFormat(java.awt.Color,boolean)
meth protected abstract void saveColors(java.util.Collection<org.netbeans.modules.versioning.util.OptionsPanelColorProvider$AnnotationFormat>)
meth protected final org.netbeans.modules.versioning.util.OptionsPanelColorProvider$AnnotationFormat createAnnotationFormat(java.lang.String,java.lang.String,java.awt.Color,boolean)
meth protected final static java.lang.String to2Hex(int)
meth protected static java.lang.String getColorKey(java.lang.String)
meth protected void putColor(org.netbeans.modules.versioning.util.OptionsPanelColorProvider$AnnotationFormat)
meth public abstract java.lang.String getName()
meth public final java.util.Map<java.lang.String,java.awt.Color[]> getColors()
meth public void colorsChanged(java.util.Map<java.lang.String,java.awt.Color>)
supr java.lang.Object
hfds colorNamePrefix,colors

CLSS public org.netbeans.modules.versioning.util.OptionsPanelColorProvider$AnnotationFormat
 outer org.netbeans.modules.versioning.util.OptionsPanelColorProvider
meth public boolean isTooltip()
meth public java.awt.Color getActualColor()
meth public java.awt.Color getDefaultColor()
meth public java.lang.String getDisplayName()
meth public java.lang.String getKey()
meth public java.text.MessageFormat getFormat()
supr java.lang.Object
hfds actualColor,defaultColor,displayName,format,key,tooltip

CLSS public org.netbeans.modules.versioning.util.PlaceholderPanel
cons public init()
cons public init(boolean)
meth protected void addImpl(java.awt.Component,java.lang.Object,int)
meth public boolean isEmpty()
meth public java.awt.Dimension getMaximumSize()
meth public java.awt.Dimension getMinimumSize()
meth public java.awt.Dimension getPreferredSize()
meth public void doLayout()
meth public void remove(int)
meth public void remove(java.awt.Component)
meth public void remove(java.awt.MenuComponent)
meth public void removeAll()
meth public void setComponent(java.awt.Component)
meth public void setLayout(java.awt.LayoutManager)
supr javax.swing.JPanel
hfds MAX_DIMENSION,ZERO_DIMENSION

CLSS public final org.netbeans.modules.versioning.util.ProjectUtilities
fld public final static java.util.logging.Logger LOG
meth public static java.util.Set<? extends org.netbeans.api.project.Project> getSubProjects(org.netbeans.api.project.Project)
meth public static void addSubprojects(org.netbeans.api.project.Project,java.util.Set<org.netbeans.api.project.Project>,java.util.Map<org.netbeans.api.project.Project,java.util.Set<? extends org.netbeans.api.project.Project>>)
meth public static void newProjectWizard(java.io.File)
meth public static void openCheckedOutProjects(java.util.Map<org.netbeans.api.project.Project,java.util.Set<org.netbeans.api.project.Project>>,java.io.File)
meth public static void openClonedOutProjects(java.util.Map<org.netbeans.api.project.Project,java.util.Set<org.netbeans.api.project.Project>>,java.io.File)
meth public static void openExportedProjects(java.util.Map<org.netbeans.api.project.Project,java.util.Set<org.netbeans.api.project.Project>>,java.io.File)
meth public static void scanForProjects(org.openide.filesystems.FileObject,java.util.Map<org.netbeans.api.project.Project,java.util.Set<org.netbeans.api.project.Project>>)
meth public static void selectAndExpandProject(org.netbeans.api.project.Project)
meth public static void sortProjectsByParents(java.util.List<org.netbeans.api.project.Project>,java.util.Map<org.netbeans.api.project.Project,java.util.Set<org.netbeans.api.project.Project>>)
supr java.lang.Object
hfds ProjectTab_ID_LOGICAL

CLSS public org.netbeans.modules.versioning.util.RootsToFile
cons public init(org.netbeans.modules.versioning.util.RootsToFile$Callback,java.util.logging.Logger,int)
innr public abstract interface static Callback
meth public java.io.File getRepositoryRoot(java.io.File)
meth public void clear()
supr java.lang.Object
hfds accesCount,cachedAccesCount,callback,files,log,statisticsFrequency

CLSS public abstract interface static org.netbeans.modules.versioning.util.RootsToFile$Callback
 outer org.netbeans.modules.versioning.util.RootsToFile
meth public abstract boolean repositoryExistsFor(java.io.File)
meth public abstract java.io.File getTopmostManagedAncestor(java.io.File)

CLSS public abstract org.netbeans.modules.versioning.util.SearchHistorySupport
cons protected init(java.io.File)
fld public final static java.lang.String PROVIDED_EXTENSIONS_SEARCH_HISTORY = "ProvidedExtensions.SearchHistorySupport"
meth protected abstract boolean searchHistoryImpl(int) throws java.io.IOException
meth protected java.io.File getFile()
meth public boolean searchHistory(int) throws java.io.IOException
meth public static org.netbeans.modules.versioning.util.SearchHistorySupport getInstance(java.io.File)
supr java.lang.Object
hfds LOG,file

CLSS public org.netbeans.modules.versioning.util.SimpleLookup
cons public init()
fld protected final java.lang.Object dataSetLock
meth protected final void setDataImpl(java.lang.Object[])
meth protected java.lang.Object[] rectifyData(java.lang.Object[])
meth protected void setValidatedData(java.lang.Object[])
meth protected void validateData(java.lang.Object[])
meth public !varargs void setData(java.lang.Object[])
supr org.openide.util.lookup.AbstractLookup
hcls SimpleItem

CLSS public org.netbeans.modules.versioning.util.SortedTable
cons public init(org.netbeans.modules.versioning.util.TableSorter)
meth protected javax.swing.table.JTableHeader createDefaultTableHeader()
meth public void setTableHeader(javax.swing.table.JTableHeader)
supr javax.swing.JTable
hfds sorter
hcls TableHeader

CLSS public org.netbeans.modules.versioning.util.StringSelector
cons public init()
innr public static RecentMessageSelector
intf java.awt.event.MouseListener
meth public static java.lang.String select(java.lang.String,java.lang.String,java.util.List<java.lang.String>)
meth public void mouseClicked(java.awt.event.MouseEvent)
meth public void mouseEntered(java.awt.event.MouseEvent)
meth public void mouseExited(java.awt.event.MouseEvent)
meth public void mousePressed(java.awt.event.MouseEvent)
meth public void mouseReleased(java.awt.event.MouseEvent)
supr javax.swing.JPanel
hfds choices,jScrollPane,listValues,optionsPanel,promptLabel

CLSS public static org.netbeans.modules.versioning.util.StringSelector$RecentMessageSelector
 outer org.netbeans.modules.versioning.util.StringSelector
cons public init(java.util.prefs.Preferences)
meth public boolean isAutoFill()
meth public java.lang.String getRecentMessage(java.lang.String,java.lang.String,java.util.List<java.lang.String>)
meth public void setAutoFill(boolean)
supr org.netbeans.modules.versioning.util.StringSelector
hfds KEY,prefs

CLSS public org.netbeans.modules.versioning.util.SystemActionBridge
cons public init(javax.swing.Action,java.lang.String)
cons public init(javax.swing.Action,java.lang.String,java.lang.String)
meth public boolean isEnabled()
meth public static org.netbeans.modules.versioning.util.SystemActionBridge createAction(javax.swing.Action,java.lang.String,org.openide.util.Lookup)
meth public static org.netbeans.modules.versioning.util.SystemActionBridge createAction(javax.swing.Action,java.lang.String,org.openide.util.Lookup,java.lang.String)
meth public void actionPerformed(java.awt.event.ActionEvent)
supr javax.swing.AbstractAction
hfds UILOG,action,delegateAction

CLSS public final org.netbeans.modules.versioning.util.TableSorter
cons public init()
cons public init(javax.swing.table.TableModel)
cons public init(javax.swing.table.TableModel,javax.swing.table.JTableHeader)
fld protected javax.swing.table.TableModel tableModel
fld public final static int ASCENDING = 1
fld public final static int DESCENDING = -1
fld public final static int NOT_SORTED = 0
fld public final static java.util.Comparator COMPARABLE_COMAPRATOR
fld public final static java.util.Comparator LEXICAL_COMPARATOR
innr public static SortingSafeTableModelEvent
meth protected java.util.Comparator getComparator(int)
meth protected javax.swing.Icon getHeaderRendererIcon(int,int)
meth public boolean isCellEditable(int,int)
meth public boolean isSorting()
meth public final void setTableModel(javax.swing.table.TableModel)
meth public int getColumnCount()
meth public int getRowCount()
meth public int getSortingStatus(int)
meth public int modelIndex(int)
meth public int viewIndex(int)
meth public java.lang.Class getColumnClass(int)
meth public java.lang.Object getValueAt(int,int)
meth public java.lang.String getColumnName(int)
meth public java.util.LinkedHashMap<java.lang.Integer,java.lang.Integer> getSortingState()
meth public javax.swing.table.JTableHeader getTableHeader()
meth public javax.swing.table.TableModel getTableModel()
meth public void setColumnComparator(int,java.util.Comparator)
meth public void setColumnComparator(java.lang.Class,java.util.Comparator)
meth public void setSortingStatus(int,int)
meth public void setTableHeader(javax.swing.table.JTableHeader)
meth public void setValueAt(java.lang.Object,int,int)
supr javax.swing.table.AbstractTableModel
hfds EMPTY_DIRECTIVE,ICON_ASCENDING,ICON_DESCENDING,columnComparators,modelToView,mouseListener,sortingColumns,tableHeader,tableModelListener,viewToModel
hcls Directive,MouseHandler,Row,SortableHeaderRenderer,TableModelHandler

CLSS public static org.netbeans.modules.versioning.util.TableSorter$SortingSafeTableModelEvent
 outer org.netbeans.modules.versioning.util.TableSorter
cons public init(javax.swing.table.TableModel,int,int)
supr javax.swing.event.TableModelEvent

CLSS public org.netbeans.modules.versioning.util.TemplateSelector
cons public init(java.util.prefs.Preferences)
intf java.awt.event.ActionListener
meth public boolean isAutofill()
meth public boolean show()
 anno 0 java.lang.Deprecated()
meth public boolean show(java.lang.String)
meth public java.lang.String getTemplate()
meth public void actionPerformed(java.awt.event.ActionEvent)
supr java.lang.Object
hfds KEY_AUTO_FILL,KEY_TEMPLATE,KEY_TEMPLATE_FILE,panel,preferences

CLSS public org.netbeans.modules.versioning.util.UndoRedoSupport
meth public static org.netbeans.modules.versioning.util.UndoRedoSupport register(javax.swing.text.JTextComponent)
meth public void unregister()
supr java.lang.Object
hfds ACTION_NAME_REDO,ACTION_NAME_UNDO,DELIMITER_PATTERN,component,edit,lastLength,lastOffset,um
hcls CompoundUndoManager,MyCompoundEdit

CLSS public final org.netbeans.modules.versioning.util.Utils
meth public !varargs static void setAcceleratorBindings(java.lang.String,javax.swing.Action[])
meth public static boolean isAncestorOrEqual(java.io.File,java.io.File)
meth public static boolean isFileContentText(java.io.File)
meth public static boolean isForbiddenFolder(java.io.File)
meth public static boolean isForbiddenFolder(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static boolean isForbiddenFolder(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public static boolean isFromMultiFileDataObject(org.netbeans.modules.versioning.spi.VCSContext)
meth public static boolean isScanForbidden(java.io.File)
 anno 0 java.lang.Deprecated()
meth public static boolean shareCommonDataObject(java.io.File[])
meth public static java.awt.Point getPositionForPopup(javax.swing.JList)
meth public static java.awt.Point getPositionForPopup(javax.swing.JTable)
meth public static java.awt.Point getPositionForPopup(javax.swing.JTree)
meth public static java.io.File getCommonParent(java.io.File,java.io.File)
meth public static java.io.File getProjectFile(org.netbeans.api.project.Project)
meth public static java.io.File getProjectFile(org.netbeans.modules.versioning.spi.VCSContext)
meth public static java.io.File getTempFolder()
meth public static java.io.File getTempFolder(boolean)
meth public static java.io.File[] getProjectRootFiles(org.netbeans.api.project.Project)
meth public static java.io.File[][] splitFlatOthers(java.io.File[])
meth public static java.io.Reader createReader(java.io.File) throws java.io.FileNotFoundException
meth public static java.io.Reader createReader(org.openide.filesystems.FileObject) throws java.io.FileNotFoundException
meth public static java.io.Reader getDocumentReader(javax.swing.text.Document)
meth public static java.lang.Integer getPriority(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static java.lang.String getActionName(java.lang.Class,java.lang.String,org.netbeans.modules.versioning.spi.VCSContext)
meth public static java.lang.String getContextDisplayName(org.netbeans.modules.versioning.spi.VCSContext)
meth public static java.lang.String getHash(java.lang.String,byte[]) throws java.security.NoSuchAlgorithmException
meth public static java.lang.String getLineEnding(org.openide.filesystems.FileObject,org.openide.filesystems.FileLock)
meth public static java.lang.String getStackTrace()
meth public static java.lang.String skipUnsupportedVariables(java.lang.String,java.lang.String[])
meth public static java.lang.String wordWrap(java.lang.String,int)
meth public static java.nio.charset.Charset getAssociatedEncoding(org.openide.filesystems.FileObject)
meth public static java.util.List<java.lang.String> getStringList(java.util.prefs.Preferences,java.lang.String)
meth public static java.util.Set<java.io.File> flattenFiles(java.io.File[],java.util.Collection<java.io.File>)
meth public static java.util.Set<java.io.File> getAllDataObjectFiles(java.io.File)
meth public static java.util.Set<java.io.File> getOpenFiles()
meth public static javax.swing.Action getAcceleratedAction(java.lang.String)
meth public static javax.swing.JMenuItem toMenuItem(javax.swing.Action)
meth public static org.netbeans.api.project.Project getProject(java.io.File[])
meth public static org.netbeans.api.project.Project getProject(org.netbeans.modules.versioning.spi.VCSContext)
meth public static org.netbeans.modules.versioning.spi.VersioningSystem[] getOwners(org.netbeans.modules.versioning.spi.VCSContext)
meth public static org.openide.text.CloneableEditorSupport openFile(org.openide.filesystems.FileObject,java.lang.String)
meth public static org.openide.util.RequestProcessor$Task createTask(java.lang.Runnable)
meth public static void addFolderToLog(java.io.File)
meth public static void associateEncoding(java.io.File,java.io.File)
meth public static void associateEncoding(java.io.File,java.nio.charset.Charset)
meth public static void associateEncoding(org.openide.filesystems.FileObject,java.nio.charset.Charset)
meth public static void associateEncoding(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject)
meth public static void copyStreamsCloseAll(java.io.OutputStream,java.io.InputStream) throws java.io.IOException
meth public static void copyStreamsCloseAll(java.io.Writer,java.io.Reader) throws java.io.IOException
meth public static void deleteRecursively(java.io.File)
meth public static void insert(java.util.prefs.Preferences,java.lang.String,java.lang.String,int)
meth public static void logError(java.lang.Object,java.lang.Throwable)
meth public static void logFine(java.lang.Object,java.lang.Exception)
meth public static void logInfo(java.lang.Class,java.lang.Throwable)
meth public static void logVCSActionEvent(java.lang.String)
meth public static void logVCSClientEvent(java.lang.String,java.lang.String)
meth public static void logVCSCommandUsageEvent(java.lang.String,long,long,java.lang.String,boolean)
meth public static void logVCSExternalRepository(java.lang.String,java.lang.String)
meth public static void logVCSKenaiUsage(java.lang.String,java.lang.String)
meth public static void logWarn(java.lang.Class,java.lang.Throwable)
meth public static void logWarn(java.lang.Object,java.lang.Throwable)
meth public static void openFile(java.io.File)
meth public static void post(java.lang.Runnable)
meth public static void post(java.lang.Runnable,int)
meth public static void postParallel(java.lang.Runnable,int)
meth public static void put(java.util.prefs.Preferences,java.lang.String,java.util.List<java.lang.String>)
meth public static void removeFromArray(java.util.prefs.Preferences,java.lang.String,java.lang.String)
meth public static void removeFromArray(java.util.prefs.Preferences,java.lang.String,java.util.List<java.lang.String>)
meth public static void setReadOnly(java.io.File,boolean)
meth public static void setWaitCursor(boolean)
supr java.lang.Object
hfds ENCODING_LOCK,LOG,METRICS_LOG,UIGESTURES_LOG,fileToCharset,fileToFileObject,foldersToCheck,forbiddenFolders,kenaiAccessor,loggedRoots,loggingTask,metrics,tempDir,vcsBlockingRequestProcessor,vcsParallelRequestProcessor,vcsRequestProcessor
hcls LogTask,ViewCES,ViewEnv

CLSS public abstract org.netbeans.modules.versioning.util.VCSBugtrackingAccessor
cons public init()
meth public abstract void setFirmAssociations(java.io.File[],java.lang.String)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.versioning.util.VCSHyperlinkProvider
cons public init()
meth public int[] getSpans(java.lang.String)
meth public java.lang.String getTooltip(java.lang.String,int,int)
meth public void onClick(java.io.File,java.lang.String,int,int)
supr java.lang.Object

CLSS public org.netbeans.modules.versioning.util.VCSHyperlinkSupport
cons public init()
innr public abstract interface static BoundsTranslator
innr public abstract static Hyperlink
innr public abstract static StyledDocumentHyperlink
innr public static AuthorLinker
innr public static IssueLinker
meth public <%0 extends org.netbeans.modules.versioning.util.VCSHyperlinkSupport$Hyperlink> void remove(java.lang.Class<{%%0}>,java.lang.String)
meth public <%0 extends org.netbeans.modules.versioning.util.VCSHyperlinkSupport$Hyperlink> {%%0} getLinker(java.lang.Class<{%%0}>,int)
meth public <%0 extends org.netbeans.modules.versioning.util.VCSHyperlinkSupport$Hyperlink> {%%0} getLinker(java.lang.Class<{%%0}>,java.lang.String)
meth public boolean mouseClicked(java.awt.Point,int)
meth public boolean mouseClicked(java.awt.Point,java.lang.String)
meth public boolean mouseMoved(java.awt.Point,javax.swing.JComponent,int)
meth public boolean mouseMoved(java.awt.Point,javax.swing.JComponent,java.lang.String)
meth public void add(org.netbeans.modules.versioning.util.VCSHyperlinkSupport$Hyperlink,int)
meth public void add(org.netbeans.modules.versioning.util.VCSHyperlinkSupport$Hyperlink,java.lang.String)
meth public void computeBounds(javax.swing.JTextPane,int)
meth public void computeBounds(javax.swing.JTextPane,java.lang.String)
meth public void remove(org.netbeans.modules.versioning.util.VCSHyperlinkSupport$Hyperlink,java.lang.String)
supr java.lang.Object
hfds LOG,linkers

CLSS public static org.netbeans.modules.versioning.util.VCSHyperlinkSupport$AuthorLinker
 outer org.netbeans.modules.versioning.util.VCSHyperlinkSupport
cons public init(org.netbeans.modules.versioning.util.VCSKenaiAccessor$KenaiUser,javax.swing.text.Style,javax.swing.text.StyledDocument,java.lang.String) throws javax.swing.text.BadLocationException
cons public init(org.netbeans.modules.versioning.util.VCSKenaiAccessor$KenaiUser,javax.swing.text.Style,javax.swing.text.StyledDocument,java.lang.String,java.lang.String) throws javax.swing.text.BadLocationException
meth public boolean mouseClicked(java.awt.Point)
meth public boolean mouseMoved(java.awt.Point,javax.swing.JComponent)
meth public void computeBounds(javax.swing.JTextPane)
meth public void computeBounds(javax.swing.JTextPane,org.netbeans.modules.versioning.util.VCSHyperlinkSupport$BoundsTranslator)
meth public void insertString(javax.swing.text.StyledDocument,javax.swing.text.Style) throws javax.swing.text.BadLocationException
supr org.netbeans.modules.versioning.util.VCSHyperlinkSupport$StyledDocumentHyperlink
hfds AUTHOR_ICON_STYLE,author,authorStyle,bounds,docend,docstart,insertToChat,kenaiUser

CLSS public abstract interface static org.netbeans.modules.versioning.util.VCSHyperlinkSupport$BoundsTranslator
 outer org.netbeans.modules.versioning.util.VCSHyperlinkSupport
meth public abstract void correctTranslation(java.awt.Container,java.awt.Rectangle)

CLSS public abstract static org.netbeans.modules.versioning.util.VCSHyperlinkSupport$Hyperlink
 outer org.netbeans.modules.versioning.util.VCSHyperlinkSupport
cons public init()
meth public abstract boolean mouseClicked(java.awt.Point)
meth public abstract boolean mouseMoved(java.awt.Point,javax.swing.JComponent)
meth public abstract void computeBounds(javax.swing.JTextPane)
supr java.lang.Object

CLSS public static org.netbeans.modules.versioning.util.VCSHyperlinkSupport$IssueLinker
 outer org.netbeans.modules.versioning.util.VCSHyperlinkSupport
meth public boolean mouseClicked(java.awt.Point)
meth public boolean mouseMoved(java.awt.Point,javax.swing.JComponent)
meth public static org.netbeans.modules.versioning.util.VCSHyperlinkSupport$IssueLinker create(org.netbeans.modules.versioning.util.VCSHyperlinkProvider,javax.swing.text.Style,java.io.File,javax.swing.text.StyledDocument,java.lang.String)
meth public void computeBounds(javax.swing.JTextPane)
meth public void computeBounds(javax.swing.JTextPane,org.netbeans.modules.versioning.util.VCSHyperlinkSupport$BoundsTranslator)
meth public void insertString(javax.swing.text.StyledDocument,javax.swing.text.Style) throws javax.swing.text.BadLocationException
supr org.netbeans.modules.versioning.util.VCSHyperlinkSupport$StyledDocumentHyperlink
hfds bounds,docend,docstart,end,hp,issueHyperlinkStyle,length,root,start,text

CLSS public abstract static org.netbeans.modules.versioning.util.VCSHyperlinkSupport$StyledDocumentHyperlink
 outer org.netbeans.modules.versioning.util.VCSHyperlinkSupport
cons public init()
meth public abstract void insertString(javax.swing.text.StyledDocument,javax.swing.text.Style) throws javax.swing.text.BadLocationException
supr org.netbeans.modules.versioning.util.VCSHyperlinkSupport$Hyperlink

CLSS public abstract org.netbeans.modules.versioning.util.VCSKenaiAccessor
cons public init()
fld protected final static java.util.logging.Logger LOG
fld public final static java.lang.String PROP_KENAI_VCS_NOTIFICATION = "kenai.vcs.notification"
innr public abstract static KenaiNotificationListener
innr public abstract static KenaiUser
innr public abstract static VCSKenaiModification
innr public abstract static VCSKenaiNotification
innr public final static !enum RepositoryActivity
innr public final static !enum Service
meth public abstract boolean isAuthorized(java.lang.String,org.netbeans.modules.versioning.util.VCSKenaiAccessor$RepositoryActivity)
meth public abstract boolean isKenai(java.lang.String)
meth public abstract boolean isLogged(java.lang.String)
meth public abstract boolean isUserOnline(java.lang.String)
meth public abstract boolean showLogin()
meth public abstract java.lang.String getRevisionUrl(java.lang.String,java.lang.String)
meth public abstract java.net.PasswordAuthentication getPasswordAuthentication(java.lang.String)
meth public abstract java.net.PasswordAuthentication getPasswordAuthentication(java.lang.String,boolean)
meth public abstract org.netbeans.modules.versioning.util.VCSKenaiAccessor$KenaiUser forName(java.lang.String)
meth public abstract org.netbeans.modules.versioning.util.VCSKenaiAccessor$KenaiUser forName(java.lang.String,java.lang.String)
meth public abstract void addVCSNoficationListener(java.beans.PropertyChangeListener)
meth public abstract void logVcsUsage(java.lang.String,java.lang.String)
meth public abstract void removeVCSNoficationListener(java.beans.PropertyChangeListener)
supr java.lang.Object

CLSS public abstract static org.netbeans.modules.versioning.util.VCSKenaiAccessor$KenaiNotificationListener
 outer org.netbeans.modules.versioning.util.VCSKenaiAccessor
cons public init()
fld protected static java.util.logging.Logger LOG
intf java.beans.PropertyChangeListener
meth protected abstract void handleVCSNotification(org.netbeans.modules.versioning.util.VCSKenaiAccessor$VCSKenaiNotification)
meth public void propertyChange(java.beans.PropertyChangeEvent)
supr org.netbeans.modules.versioning.util.VCSNotificationDisplayer
hfds rp

CLSS public abstract static org.netbeans.modules.versioning.util.VCSKenaiAccessor$KenaiUser
 outer org.netbeans.modules.versioning.util.VCSKenaiAccessor
cons public init()
meth public abstract boolean isOnline()
meth public abstract java.lang.String getUser()
meth public abstract javax.swing.Icon getIcon()
meth public abstract javax.swing.JLabel createUserWidget()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void startChat()
meth public abstract void startChat(java.lang.String)
meth public static java.lang.String getChatLink(org.openide.filesystems.FileObject,int)
supr java.lang.Object

CLSS public final static !enum org.netbeans.modules.versioning.util.VCSKenaiAccessor$RepositoryActivity
 outer org.netbeans.modules.versioning.util.VCSKenaiAccessor
fld public final static org.netbeans.modules.versioning.util.VCSKenaiAccessor$RepositoryActivity READ
fld public final static org.netbeans.modules.versioning.util.VCSKenaiAccessor$RepositoryActivity WRITE
meth public static org.netbeans.modules.versioning.util.VCSKenaiAccessor$RepositoryActivity valueOf(java.lang.String)
meth public static org.netbeans.modules.versioning.util.VCSKenaiAccessor$RepositoryActivity[] values()
supr java.lang.Enum<org.netbeans.modules.versioning.util.VCSKenaiAccessor$RepositoryActivity>

CLSS public final static !enum org.netbeans.modules.versioning.util.VCSKenaiAccessor$Service
 outer org.netbeans.modules.versioning.util.VCSKenaiAccessor
fld public final static org.netbeans.modules.versioning.util.VCSKenaiAccessor$Service UNKNOWN
fld public final static org.netbeans.modules.versioning.util.VCSKenaiAccessor$Service VCS_HG
fld public final static org.netbeans.modules.versioning.util.VCSKenaiAccessor$Service VCS_SVN
meth public static org.netbeans.modules.versioning.util.VCSKenaiAccessor$Service valueOf(java.lang.String)
meth public static org.netbeans.modules.versioning.util.VCSKenaiAccessor$Service[] values()
supr java.lang.Enum<org.netbeans.modules.versioning.util.VCSKenaiAccessor$Service>

CLSS public abstract static org.netbeans.modules.versioning.util.VCSKenaiAccessor$VCSKenaiModification
 outer org.netbeans.modules.versioning.util.VCSKenaiAccessor
cons public init()
innr public final static !enum Type
meth public abstract java.lang.String getId()
meth public abstract java.lang.String getResource()
meth public abstract org.netbeans.modules.versioning.util.VCSKenaiAccessor$VCSKenaiModification$Type getType()
supr java.lang.Object

CLSS public final static !enum org.netbeans.modules.versioning.util.VCSKenaiAccessor$VCSKenaiModification$Type
 outer org.netbeans.modules.versioning.util.VCSKenaiAccessor$VCSKenaiModification
fld public final static org.netbeans.modules.versioning.util.VCSKenaiAccessor$VCSKenaiModification$Type CHANGE
fld public final static org.netbeans.modules.versioning.util.VCSKenaiAccessor$VCSKenaiModification$Type DELETE
fld public final static org.netbeans.modules.versioning.util.VCSKenaiAccessor$VCSKenaiModification$Type NEW
meth public static org.netbeans.modules.versioning.util.VCSKenaiAccessor$VCSKenaiModification$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.versioning.util.VCSKenaiAccessor$VCSKenaiModification$Type[] values()
supr java.lang.Enum<org.netbeans.modules.versioning.util.VCSKenaiAccessor$VCSKenaiModification$Type>

CLSS public abstract static org.netbeans.modules.versioning.util.VCSKenaiAccessor$VCSKenaiNotification
 outer org.netbeans.modules.versioning.util.VCSKenaiAccessor
cons public init()
meth public abstract java.io.File getProjectDirectory()
meth public abstract java.lang.String getAuthor()
meth public abstract java.net.URI getUri()
meth public abstract java.util.Date getStamp()
meth public abstract java.util.List<org.netbeans.modules.versioning.util.VCSKenaiAccessor$VCSKenaiModification> getModifications()
meth public abstract org.netbeans.modules.versioning.util.VCSKenaiAccessor$Service getService()
supr java.lang.Object

CLSS public abstract org.netbeans.modules.versioning.util.VCSNotificationDisplayer
cons public init()
meth protected abstract void setupPane(javax.swing.JTextPane,java.io.File[],java.io.File,java.lang.String,java.lang.String)
meth protected java.lang.String getFileNames(java.io.File[])
meth protected java.lang.String trim(java.lang.String)
meth protected void notifyFileChange(java.io.File[],java.io.File,java.lang.String,java.lang.String)
supr java.lang.Object
hfds NOTIFICATION_ICON_PATH

CLSS public abstract interface org.netbeans.modules.versioning.util.VCSOptionsKeywordsProvider
meth public abstract boolean acceptKeywords(java.util.List<java.lang.String>)

CLSS public org.netbeans.modules.versioning.util.VcsAdvancedOptions
cons public init()
fld public final static java.lang.String ID = "Versioning"
meth protected void setCurrentSubcategory(java.lang.String)
meth public boolean isChanged()
meth public boolean isValid()
meth public javax.swing.JComponent getComponent(org.openide.util.Lookup)
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void applyChanges()
meth public void cancel()
meth public void handleSuccessfulSearch(java.lang.String,java.util.List<java.lang.String>)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void update()
supr org.netbeans.spi.options.OptionsPanelController
hfds categoryToController,initialized,panel

CLSS public org.netbeans.modules.versioning.util.VcsAdvancedOptionsPanel
cons public init()
meth public void addPanel(java.lang.String,javax.swing.JComponent)
supr javax.swing.JPanel
hfds containerPanel,jLabel1,jScrollPane1,versioningPanels,versioningSystemsList

CLSS public org.netbeans.modules.versioning.util.VersioningEvent
cons public init(java.lang.Object,java.lang.Object,java.lang.Object[])
meth public java.lang.Object getId()
meth public java.lang.Object[] getParams()
supr java.util.EventObject
hfds id,params

CLSS public org.netbeans.modules.versioning.util.VersioningInfo
cons public init()
meth public static void show(java.util.HashMap<java.io.File,java.util.Map<java.lang.String,java.lang.String>>)
supr java.lang.Object
hcls VersioningInfoNode,VersioningInfoProperty

CLSS public abstract interface org.netbeans.modules.versioning.util.VersioningListener
intf java.util.EventListener
meth public abstract void versioningEvent(org.netbeans.modules.versioning.util.VersioningEvent)

CLSS public final org.netbeans.modules.versioning.util.VersioningOutputManager
meth public static org.netbeans.modules.versioning.util.VersioningOutputManager getInstance()
meth public void addComponent(java.lang.String,javax.swing.JComponent)
supr java.lang.Object
hfds instance

CLSS public org.netbeans.modules.versioning.util.VersioningOutputTopComponent
cons public init()
intf java.beans.PropertyChangeListener
intf java.io.Externalizable
meth protected java.lang.String preferredID()
meth public int getPersistenceType()
meth public java.lang.Object readResolve()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static org.netbeans.modules.versioning.util.VersioningOutputTopComponent getInstance()
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr org.openide.windows.TopComponent
hfds components,instance,nextTabAction,prevTabAction,serialVersionUID,tabbedPane

CLSS public org.netbeans.modules.versioning.util.VerticallyNonResizingPanel
cons public init()
meth public java.awt.Dimension getMaximumSize()
supr javax.swing.JPanel

CLSS public org.netbeans.modules.versioning.util.WideButton
cons public init()
meth public java.awt.Dimension getPreferredSize()
supr javax.swing.JButton

CLSS public org.netbeans.modules.versioning.util.common.CommitMessageMouseAdapter
cons public init()
meth public void mousePressed(java.awt.event.MouseEvent)
supr java.awt.event.MouseAdapter
hfds popupBuilder

CLSS public abstract org.netbeans.modules.versioning.util.common.FileTreeView<%0 extends org.netbeans.modules.versioning.util.status.VCSStatusNode>
cons public init()
fld protected final org.openide.explorer.view.OutlineView view
innr protected abstract AbstractRenderDataProvider
intf java.awt.event.MouseListener
intf java.beans.PropertyChangeListener
intf javax.swing.event.AncestorListener
intf org.netbeans.modules.versioning.util.common.FileViewComponent<{org.netbeans.modules.versioning.util.common.FileTreeView%0}>
meth protected abstract javax.swing.JPopupMenu getPopup()
meth protected abstract void nodeSelected({org.netbeans.modules.versioning.util.common.FileTreeView%0})
meth protected abstract void setDefaultColumnSizes()
meth protected final org.openide.nodes.Node createFilterNode({org.netbeans.modules.versioning.util.common.FileTreeView%0})
meth protected {org.netbeans.modules.versioning.util.common.FileTreeView%0} convertToAcceptedNode(org.openide.nodes.Node)
meth public boolean hasNextNode({org.netbeans.modules.versioning.util.common.FileTreeView%0})
meth public boolean hasPreviousNode({org.netbeans.modules.versioning.util.common.FileTreeView%0})
meth public final java.util.List<org.openide.nodes.Node> getSelectedNodes()
meth public final void focus()
meth public int getPreferredHeaderHeight()
meth public int getPreferredHeight()
meth public java.lang.Object prepareModel({org.netbeans.modules.versioning.util.common.FileTreeView%0}[])
meth public javax.swing.JComponent getComponent()
meth public void ancestorAdded(javax.swing.event.AncestorEvent)
meth public void ancestorMoved(javax.swing.event.AncestorEvent)
meth public void ancestorRemoved(javax.swing.event.AncestorEvent)
meth public void mouseClicked(java.awt.event.MouseEvent)
meth public void mouseEntered(java.awt.event.MouseEvent)
meth public void mouseExited(java.awt.event.MouseEvent)
meth public void mousePressed(java.awt.event.MouseEvent)
meth public void mouseReleased(java.awt.event.MouseEvent)
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void setModel({org.netbeans.modules.versioning.util.common.FileTreeView%0}[],org.openide.cookies.EditorCookie[],java.lang.Object)
meth public void setSelectedNode({org.netbeans.modules.versioning.util.common.FileTreeView%0})
meth public {org.netbeans.modules.versioning.util.common.FileTreeView%0} getNextNode({org.netbeans.modules.versioning.util.common.FileTreeView%0})
meth public {org.netbeans.modules.versioning.util.common.FileTreeView%0} getNodeAtPosition(int)
meth public {org.netbeans.modules.versioning.util.common.FileTreeView%0} getPreviousNode({org.netbeans.modules.versioning.util.common.FileTreeView%0})
meth public {org.netbeans.modules.versioning.util.common.FileTreeView%0} getSelectedNode()
meth public {org.netbeans.modules.versioning.util.common.FileTreeView%0}[] getNeighbouringNodes({org.netbeans.modules.versioning.util.common.FileTreeView%0},int)
supr java.lang.Object
hfds FOLDER_ICON,ICON_KEY_UIMANAGER,ICON_KEY_UIMANAGER_NB,PATH_SEPARATOR_REGEXP,displayed,editorCookies,em,internalTraverse,nodeMapping,nodes,viewComponent
hcls NodeChildren,NodeData,PathComparator,RepositoryRootNode,RootNode,RootNodeChildren,TreeFilterNode,TreeViewChildren,ViewContainer

CLSS protected abstract org.netbeans.modules.versioning.util.common.FileTreeView$AbstractRenderDataProvider
 outer org.netbeans.modules.versioning.util.common.FileTreeView
cons protected init(org.netbeans.modules.versioning.util.common.FileTreeView)
intf org.netbeans.swing.outline.RenderDataProvider
meth protected abstract java.lang.String annotateName({org.netbeans.modules.versioning.util.common.FileTreeView%0},java.lang.String)
meth public boolean isHtmlDisplayName(java.lang.Object)
meth public java.awt.Color getBackground(java.lang.Object)
meth public java.awt.Color getForeground(java.lang.Object)
meth public java.lang.String getDisplayName(java.lang.Object)
meth public java.lang.String getTooltipText(java.lang.Object)
meth public javax.swing.Icon getIcon(java.lang.Object)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.versioning.util.common.FileViewComponent<%0 extends org.openide.nodes.Node>
meth public abstract boolean hasNextNode({org.netbeans.modules.versioning.util.common.FileViewComponent%0})
meth public abstract boolean hasPreviousNode({org.netbeans.modules.versioning.util.common.FileViewComponent%0})
meth public abstract int getPreferredHeaderHeight()
meth public abstract int getPreferredHeight()
meth public abstract java.lang.Object prepareModel({org.netbeans.modules.versioning.util.common.FileViewComponent%0}[])
meth public abstract javax.swing.JComponent getComponent()
meth public abstract void focus()
meth public abstract void setModel({org.netbeans.modules.versioning.util.common.FileViewComponent%0}[],org.openide.cookies.EditorCookie[],java.lang.Object)
meth public abstract void setSelectedNode({org.netbeans.modules.versioning.util.common.FileViewComponent%0})
meth public abstract {org.netbeans.modules.versioning.util.common.FileViewComponent%0} getNextNode({org.netbeans.modules.versioning.util.common.FileViewComponent%0})
meth public abstract {org.netbeans.modules.versioning.util.common.FileViewComponent%0} getNodeAtPosition(int)
meth public abstract {org.netbeans.modules.versioning.util.common.FileViewComponent%0} getPreviousNode({org.netbeans.modules.versioning.util.common.FileViewComponent%0})
meth public abstract {org.netbeans.modules.versioning.util.common.FileViewComponent%0} getSelectedNode()
meth public abstract {org.netbeans.modules.versioning.util.common.FileViewComponent%0}[] getNeighbouringNodes({org.netbeans.modules.versioning.util.common.FileViewComponent%0},int)

CLSS public final org.netbeans.modules.versioning.util.common.SectionButton
cons public init()
cons public init(java.awt.event.ActionListener)
meth public java.awt.Color getBackground()
meth public java.awt.Color getForeground()
meth public java.lang.String getUIClassID()
meth public void addActionListener(java.awt.event.ActionListener)
supr javax.swing.JCheckBox
hfds al,isAqua,isGTK,isNimbus
hcls ExpandAction

CLSS public abstract org.netbeans.modules.versioning.util.common.VCSCommitDiffProvider<%0 extends org.netbeans.modules.versioning.util.common.VCSFileNode>
cons public init()
meth protected abstract javax.swing.JComponent createDiffComponent(java.io.File)
meth protected java.util.Set<java.io.File> getModifiedFiles()
meth protected javax.swing.JComponent getDiffComponent({org.netbeans.modules.versioning.util.common.VCSCommitDiffProvider%0}[])
meth protected org.openide.cookies.EditorCookie[] getEditorCookies()
meth protected org.openide.cookies.SaveCookie[] getSaveCookies()
meth protected void selectFile(java.io.File)
supr java.lang.Object
hfds displayedDiffs

CLSS public abstract org.netbeans.modules.versioning.util.common.VCSCommitFilter
cons public init(boolean)
meth protected void setSelected(boolean)
meth public abstract java.lang.String getID()
meth public abstract java.lang.String getTooltip()
meth public abstract javax.swing.Icon getIcon()
meth public boolean isSelected()
supr java.lang.Object
hfds selected

CLSS public abstract org.netbeans.modules.versioning.util.common.VCSCommitOptions
fld public final static org.netbeans.modules.versioning.util.common.VCSCommitOptions COMMIT
fld public final static org.netbeans.modules.versioning.util.common.VCSCommitOptions COMMIT_REMOVE
fld public final static org.netbeans.modules.versioning.util.common.VCSCommitOptions EXCLUDE
innr public static Add
innr public static Commit
meth public java.lang.String toString()
supr java.lang.Object
hfds label

CLSS public static org.netbeans.modules.versioning.util.common.VCSCommitOptions$Add
 outer org.netbeans.modules.versioning.util.common.VCSCommitOptions
cons public init(java.lang.String)
supr org.netbeans.modules.versioning.util.common.VCSCommitOptions

CLSS public static org.netbeans.modules.versioning.util.common.VCSCommitOptions$Commit
 outer org.netbeans.modules.versioning.util.common.VCSCommitOptions
cons public init(java.lang.String)
supr org.netbeans.modules.versioning.util.common.VCSCommitOptions

CLSS public abstract org.netbeans.modules.versioning.util.common.VCSCommitPanel<%0 extends org.netbeans.modules.versioning.util.common.VCSFileNode>
cons public init(org.netbeans.modules.versioning.util.common.VCSCommitTable,org.netbeans.modules.versioning.util.common.VCSCommitParameters,java.util.prefs.Preferences,java.util.Collection<? extends org.netbeans.modules.versioning.hooks.VCSHook>,org.netbeans.modules.versioning.hooks.VCSHookContext,java.util.List<org.netbeans.modules.versioning.util.common.VCSCommitFilter>,org.netbeans.modules.versioning.util.common.VCSCommitDiffProvider)
fld public final static java.lang.String PROP_COMMIT_EXCLUSIONS = "commitExclusions"
intf java.beans.PropertyChangeListener
intf java.util.prefs.PreferenceChangeListener
intf javax.swing.event.ChangeListener
intf javax.swing.event.TableModelListener
meth protected abstract void computeNodes()
meth protected boolean isCommitButtonEnabled()
meth protected boolean processKeyBinding(javax.swing.KeyStroke,java.awt.event.KeyEvent,int,boolean)
meth protected void enableCommitButton(boolean)
meth protected void setupProgress(java.lang.String,javax.swing.JComponent)
meth protected void showProgress()
meth protected void stopProgress()
meth public boolean open(org.netbeans.modules.versioning.spi.VCSContext,org.openide.util.HelpCtx)
meth public boolean open(org.netbeans.modules.versioning.spi.VCSContext,org.openide.util.HelpCtx,java.lang.String)
meth public org.netbeans.modules.versioning.util.common.VCSCommitFilter getSelectedFilter()
meth public org.netbeans.modules.versioning.util.common.VCSCommitParameters getParameters()
meth public org.netbeans.modules.versioning.util.common.VCSCommitTable<{org.netbeans.modules.versioning.util.common.VCSCommitPanel%0}> getCommitTable()
meth public void addNotify()
meth public void addVersioningListener(org.netbeans.modules.versioning.util.VersioningListener)
meth public void preferenceChange(java.util.prefs.PreferenceChangeEvent)
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void removeNotify()
meth public void removeVersioningListener(org.netbeans.modules.versioning.util.VersioningListener)
meth public void setErrorLabel(java.lang.String)
meth public void stateChanged(javax.swing.event.ChangeEvent)
meth public void tableChanged(javax.swing.event.TableModelEvent)
supr org.netbeans.modules.versioning.util.AutoResizingPanel
hfds ERROR_COLOR,EVENT_SETTINGS_CHANGED,basePanel,cancelButton,commitButton,commitTable,diffProvider,errorLabel,filters,listenerSupport,modifier,parameters,parametersPane1,preferences,progressPanel,tabbedPane

CLSS public org.netbeans.modules.versioning.util.common.VCSCommitPanelModifier
cons public init()
innr public final static !enum BundleMessage
meth public java.lang.String getMessage(org.netbeans.modules.versioning.util.common.VCSCommitPanelModifier$BundleMessage)
meth public org.netbeans.modules.versioning.util.common.VCSCommitOptions getExcludedOption()
supr java.lang.Object

CLSS public final static !enum org.netbeans.modules.versioning.util.common.VCSCommitPanelModifier$BundleMessage
 outer org.netbeans.modules.versioning.util.common.VCSCommitPanelModifier
fld public final static org.netbeans.modules.versioning.util.common.VCSCommitPanelModifier$BundleMessage COMMIT_BUTTON_ACCESSIBLE_DESCRIPTION
fld public final static org.netbeans.modules.versioning.util.common.VCSCommitPanelModifier$BundleMessage COMMIT_BUTTON_ACCESSIBLE_NAME
fld public final static org.netbeans.modules.versioning.util.common.VCSCommitPanelModifier$BundleMessage COMMIT_BUTTON_LABEL
fld public final static org.netbeans.modules.versioning.util.common.VCSCommitPanelModifier$BundleMessage FILE_PANEL_TITLE
fld public final static org.netbeans.modules.versioning.util.common.VCSCommitPanelModifier$BundleMessage FILE_TABLE_ACCESSIBLE_DESCRIPTION
fld public final static org.netbeans.modules.versioning.util.common.VCSCommitPanelModifier$BundleMessage FILE_TABLE_ACCESSIBLE_NAME
fld public final static org.netbeans.modules.versioning.util.common.VCSCommitPanelModifier$BundleMessage FILE_TABLE_EXCLUDE_ACTION_NAME
fld public final static org.netbeans.modules.versioning.util.common.VCSCommitPanelModifier$BundleMessage FILE_TABLE_HEADER_ACTION
fld public final static org.netbeans.modules.versioning.util.common.VCSCommitPanelModifier$BundleMessage FILE_TABLE_HEADER_ACTION_DESC
fld public final static org.netbeans.modules.versioning.util.common.VCSCommitPanelModifier$BundleMessage FILE_TABLE_HEADER_COMMIT
fld public final static org.netbeans.modules.versioning.util.common.VCSCommitPanelModifier$BundleMessage FILE_TABLE_HEADER_COMMIT_DESC
fld public final static org.netbeans.modules.versioning.util.common.VCSCommitPanelModifier$BundleMessage FILE_TABLE_INCLUDE_ACTION_NAME
fld public final static org.netbeans.modules.versioning.util.common.VCSCommitPanelModifier$BundleMessage MESSAGE_FINISHING_FROM_DIFF
fld public final static org.netbeans.modules.versioning.util.common.VCSCommitPanelModifier$BundleMessage MESSAGE_FINISHING_FROM_DIFF_TITLE
fld public final static org.netbeans.modules.versioning.util.common.VCSCommitPanelModifier$BundleMessage MESSAGE_NO_FILES
fld public final static org.netbeans.modules.versioning.util.common.VCSCommitPanelModifier$BundleMessage PANEL_ACCESSIBLE_DESCRIPTION
fld public final static org.netbeans.modules.versioning.util.common.VCSCommitPanelModifier$BundleMessage PANEL_ACCESSIBLE_NAME
fld public final static org.netbeans.modules.versioning.util.common.VCSCommitPanelModifier$BundleMessage TABS_MAIN_NAME
meth public java.lang.String toString()
meth public static org.netbeans.modules.versioning.util.common.VCSCommitPanelModifier$BundleMessage valueOf(java.lang.String)
meth public static org.netbeans.modules.versioning.util.common.VCSCommitPanelModifier$BundleMessage[] values()
supr java.lang.Enum<org.netbeans.modules.versioning.util.common.VCSCommitPanelModifier$BundleMessage>
hfds message

CLSS public abstract org.netbeans.modules.versioning.util.common.VCSCommitParameters
cons public init(java.util.prefs.Preferences)
innr public static DefaultCommitParameters
meth protected java.awt.Component makeVerticalStrut(javax.swing.JComponent,javax.swing.JComponent,javax.swing.LayoutStyle$ComponentPlacement,javax.swing.JPanel)
meth protected java.lang.String getLastCanceledCommitMessage()
meth protected java.util.prefs.Preferences getPreferences()
meth protected javax.swing.JLabel getMessagesTemplateLink(javax.swing.JTextArea,java.lang.String)
meth protected javax.swing.JLabel getRecentMessagesLink(javax.swing.JTextArea)
meth protected static java.awt.Component makeHorizontalStrut(javax.swing.JComponent,javax.swing.JComponent,javax.swing.LayoutStyle$ComponentPlacement,javax.swing.JPanel)
meth protected static java.util.List<java.lang.String> getRecentCommitMessages(java.util.prefs.Preferences)
meth protected static javax.swing.JLabel createMessagesTemplateLink(javax.swing.JTextArea,java.util.prefs.Preferences,java.lang.String)
meth protected void fireChange()
meth public abstract boolean isCommitable()
meth public abstract java.lang.String getErrorMessage()
meth public abstract javax.swing.JPanel getPanel()
meth public static javax.swing.JLabel createRecentMessagesLink(javax.swing.JTextArea,java.util.prefs.Preferences)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
hfds LAST_COMMIT_MESSAGE,PARAMETERS_CHANGED_PROPERTY,RECENT_COMMIT_MESSAGES,changeSupport,preferences,recentLink,templateLink

CLSS public static org.netbeans.modules.versioning.util.common.VCSCommitParameters$DefaultCommitParameters
 outer org.netbeans.modules.versioning.util.common.VCSCommitParameters
cons public init(java.util.prefs.Preferences)
cons public init(java.util.prefs.Preferences,java.lang.String)
meth protected javax.swing.JPanel createPanel()
meth public boolean isCommitable()
meth public java.lang.String getCommitMessage()
meth public java.lang.String getErrorMessage()
meth public javax.swing.JPanel getPanel()
meth public void storeCommitMessage()
supr org.netbeans.modules.versioning.util.common.VCSCommitParameters
hfds commitMessage,panel
hcls ParametersPanel

CLSS public org.netbeans.modules.versioning.util.common.VCSCommitTable<%0 extends org.netbeans.modules.versioning.util.common.VCSFileNode>
cons public init(org.netbeans.modules.versioning.util.common.VCSCommitTableModel<{org.netbeans.modules.versioning.util.common.VCSCommitTable%0}>)
cons public init(org.netbeans.modules.versioning.util.common.VCSCommitTableModel<{org.netbeans.modules.versioning.util.common.VCSCommitTable%0}>,boolean)
intf java.awt.event.MouseListener
intf javax.swing.event.AncestorListener
intf javax.swing.event.TableModelListener
meth public boolean containsCommitable()
meth public java.lang.String getErrorMessage()
meth public java.util.List<{org.netbeans.modules.versioning.util.common.VCSCommitTable%0}> getCommitFiles()
meth public javax.swing.JComponent getComponent()
meth public javax.swing.table.TableModel getTableModel()
meth public org.netbeans.modules.versioning.util.TableSorter getSorter()
meth public void ancestorAdded(javax.swing.event.AncestorEvent)
meth public void ancestorMoved(javax.swing.event.AncestorEvent)
meth public void ancestorRemoved(javax.swing.event.AncestorEvent)
meth public void mouseClicked(java.awt.event.MouseEvent)
meth public void mouseEntered(java.awt.event.MouseEvent)
meth public void mouseExited(java.awt.event.MouseEvent)
meth public void mousePressed(java.awt.event.MouseEvent)
meth public void mouseReleased(java.awt.event.MouseEvent)
meth public void setCommitPanel(org.netbeans.modules.versioning.util.common.VCSCommitPanel)
meth public void setModifiedFiles(java.util.Set<java.io.File>)
meth public void setNodes({org.netbeans.modules.versioning.util.common.VCSCommitTable%0}[])
meth public void setRootFile(java.lang.String,java.lang.String)
meth public void tableChanged(javax.swing.event.TableModelEvent)
supr java.lang.Object
hfds columns,commitPanel,component,editable,errroMessage,modifiedFiles,modifier,sortByColumns,sorter,table,tableModel
hcls CheckboxCellEditor,CheckboxCellRenderer,CommitStringsCellRenderer,FileNameComparator,StatusComparator

CLSS public org.netbeans.modules.versioning.util.common.VCSCommitTableModel<%0 extends org.netbeans.modules.versioning.util.common.VCSFileNode>
cons public init()
cons public init(org.netbeans.modules.versioning.util.common.VCSCommitPanelModifier)
fld public final static java.lang.String COLUMN_NAME_ACTION = "action"
fld public final static java.lang.String COLUMN_NAME_BRANCH = "branch"
fld public final static java.lang.String COLUMN_NAME_COMMIT = "commit"
fld public final static java.lang.String COLUMN_NAME_NAME = "name"
fld public final static java.lang.String COLUMN_NAME_PATH = "path"
fld public final static java.lang.String COLUMN_NAME_STATUS = "status"
fld public static java.lang.String[] COMMIT_COLUMNS
meth protected final void setColumns(java.lang.String[])
meth protected void setIncluded(int[],boolean)
meth protected void setNodes({org.netbeans.modules.versioning.util.common.VCSCommitTableModel%0}[])
meth protected void setRootFile(java.lang.String,java.lang.String)
meth protected {org.netbeans.modules.versioning.util.common.VCSCommitTableModel%0}[] getNodes()
meth public boolean isCellEditable(int,int)
meth public int getColumnCount()
meth public int getRowCount()
meth public java.lang.Class getColumnClass(int)
meth public java.lang.Object getValueAt(int,int)
meth public java.lang.String getColumnName(int)
meth public java.util.List<{org.netbeans.modules.versioning.util.common.VCSCommitTableModel%0}> getCommitFiles()
meth public org.netbeans.modules.versioning.util.common.VCSCommitOptions getOption(int)
meth public void setValueAt(java.lang.Object,int,int)
meth public {org.netbeans.modules.versioning.util.common.VCSCommitTableModel%0} getNode(int)
supr javax.swing.table.AbstractTableModel
hfds columnLabels,columns,modifier,nodes,rootFile
hcls RootFile

CLSS public abstract org.netbeans.modules.versioning.util.common.VCSFileInformation
cons public init()
meth public abstract int getComparableStatus()
meth public abstract java.lang.String annotateNameHtml(java.lang.String)
meth public abstract java.lang.String getStatusText()
meth public java.awt.Color getAnnotatedColor()
supr java.lang.Object
hcls ByImportanceComparator

CLSS public abstract org.netbeans.modules.versioning.util.common.VCSFileNode<%0 extends org.netbeans.modules.versioning.util.common.VCSFileInformation>
cons public init(java.io.File,java.io.File)
meth public abstract org.netbeans.modules.versioning.util.common.VCSCommitOptions getDefaultCommitOption(boolean)
meth public abstract {org.netbeans.modules.versioning.util.common.VCSFileNode%0} getInformation()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.io.File getFile()
meth public java.io.File getRoot()
meth public java.lang.Object[] getLookupObjects()
meth public java.lang.String getName()
meth public java.lang.String getRelativePath()
meth public java.lang.String getStatusText()
meth public org.netbeans.modules.versioning.util.common.VCSCommitOptions getCommitOptions()
meth public org.openide.filesystems.FileObject getFileObject()
supr java.lang.Object
hfds commitOption,file,root,shortPath

CLSS public abstract org.netbeans.modules.versioning.util.status.VCSStatusNode<%0 extends org.netbeans.modules.versioning.util.common.VCSFileNode>
cons protected init({org.netbeans.modules.versioning.util.status.VCSStatusNode%0})
cons protected init({org.netbeans.modules.versioning.util.status.VCSStatusNode%0},org.openide.util.Lookup)
fld protected final org.netbeans.modules.versioning.util.status.VCSStatusNode$NameProperty nameProperty
fld protected final org.netbeans.modules.versioning.util.status.VCSStatusNode$PathProperty pathProperty
fld protected final org.netbeans.modules.versioning.util.status.VCSStatusNode$StatusProperty statusProperty
fld protected final {org.netbeans.modules.versioning.util.status.VCSStatusNode%0} node
innr protected abstract static NodeProperty
innr public static NameProperty
innr public static PathProperty
innr public static StatusProperty
meth public abstract void refresh()
meth public final {org.netbeans.modules.versioning.util.status.VCSStatusNode%0} getFileNode()
meth public java.awt.Color getAnnotatedFontColor()
meth public java.io.File getFile()
meth public java.lang.String getHtmlDisplayName()
meth public java.lang.String getName()
meth public javax.swing.Action getNodeAction()
supr org.openide.nodes.AbstractNode
hfds zeros
hcls NodePropertyEditor

CLSS public static org.netbeans.modules.versioning.util.status.VCSStatusNode$NameProperty
 outer org.netbeans.modules.versioning.util.status.VCSStatusNode
cons public init(org.netbeans.modules.versioning.util.status.VCSStatusNode)
fld public final static java.lang.String DESCRIPTION
fld public final static java.lang.String DISPLAY_NAME
fld public final static java.lang.String NAME = "name"
meth public java.lang.String getValue()
supr org.netbeans.modules.versioning.util.status.VCSStatusNode$NodeProperty<java.lang.String>
hfds fileNode

CLSS protected abstract static org.netbeans.modules.versioning.util.status.VCSStatusNode$NodeProperty<%0 extends java.lang.Object>
 outer org.netbeans.modules.versioning.util.status.VCSStatusNode
cons protected init(java.lang.String,java.lang.Class<{org.netbeans.modules.versioning.util.status.VCSStatusNode$NodeProperty%0}>,java.lang.String,java.lang.String)
meth public abstract {org.netbeans.modules.versioning.util.status.VCSStatusNode$NodeProperty%0} getValue()
meth public java.beans.PropertyEditor getPropertyEditor()
meth public java.lang.String toString()
supr org.openide.nodes.PropertySupport$ReadOnly<{org.netbeans.modules.versioning.util.status.VCSStatusNode$NodeProperty%0}>

CLSS public static org.netbeans.modules.versioning.util.status.VCSStatusNode$PathProperty
 outer org.netbeans.modules.versioning.util.status.VCSStatusNode
cons public init(org.netbeans.modules.versioning.util.status.VCSStatusNode)
fld public final static java.lang.String DESCRIPTION
fld public final static java.lang.String DISPLAY_NAME
fld public final static java.lang.String NAME = "path"
meth public java.lang.String getValue()
supr org.netbeans.modules.versioning.util.status.VCSStatusNode$NodeProperty<java.lang.String>
hfds shortPath

CLSS public static org.netbeans.modules.versioning.util.status.VCSStatusNode$StatusProperty
 outer org.netbeans.modules.versioning.util.status.VCSStatusNode
cons public init(org.netbeans.modules.versioning.util.status.VCSStatusNode)
fld public final static java.lang.String DESCRIPTION
fld public final static java.lang.String DISPLAY_NAME
fld public final static java.lang.String NAME = "status"
meth public java.lang.String getValue()
supr org.netbeans.modules.versioning.util.status.VCSStatusNode$NodeProperty<java.lang.String>
hfds fileNode

CLSS public abstract org.netbeans.modules.versioning.util.status.VCSStatusTable<%0 extends org.netbeans.modules.versioning.util.status.VCSStatusNode>
cons public init(org.netbeans.modules.versioning.util.status.VCSStatusTableModel<{org.netbeans.modules.versioning.util.status.VCSStatusTable%0}>)
fld protected final org.netbeans.modules.versioning.util.status.VCSStatusTableModel<{org.netbeans.modules.versioning.util.status.VCSStatusTable%0}> tableModel
fld protected final static java.util.Comparator NodeComparator
fld public final static java.lang.String PROP_SELECTED_FILES = "selectedFiles"
innr protected static ColumnDescriptor
intf java.awt.event.MouseListener
intf javax.swing.event.ListSelectionListener
intf org.netbeans.modules.versioning.util.common.FileViewComponent<{org.netbeans.modules.versioning.util.status.VCSStatusTable%0}>
meth protected abstract javax.swing.JPopupMenu getPopup()
meth protected abstract void setModelProperties()
meth protected final javax.swing.JTable getTable()
meth protected final void setDefaultRenderer(javax.swing.table.TableCellRenderer)
meth protected final {org.netbeans.modules.versioning.util.status.VCSStatusTable%0}[] getSelectedNodes()
meth protected void mouseClicked(org.netbeans.modules.versioning.util.status.VCSStatusNode)
meth public boolean hasNextNode({org.netbeans.modules.versioning.util.status.VCSStatusTable%0})
meth public boolean hasPreviousNode({org.netbeans.modules.versioning.util.status.VCSStatusTable%0})
meth public final java.io.File[] getSelectedFiles()
meth public final javax.swing.JComponent getComponent()
meth public final void focus()
meth public final void setSelectedNodes(java.io.File[])
meth public int getPreferredHeaderHeight()
meth public int getPreferredHeight()
meth public java.io.File getNeighbouringFile(java.io.File,int)
meth public java.io.File getNextFile(java.io.File)
meth public java.io.File getPrevFile(java.io.File)
meth public java.lang.Object prepareModel({org.netbeans.modules.versioning.util.status.VCSStatusTable%0}[])
meth public java.util.Map<java.io.File,{org.netbeans.modules.versioning.util.status.VCSStatusTable%0}> getNodes()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void mouseClicked(java.awt.event.MouseEvent)
meth public void mouseEntered(java.awt.event.MouseEvent)
meth public void mouseExited(java.awt.event.MouseEvent)
meth public void mousePressed(java.awt.event.MouseEvent)
meth public void mouseReleased(java.awt.event.MouseEvent)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setModel({org.netbeans.modules.versioning.util.status.VCSStatusTable%0}[],org.openide.cookies.EditorCookie[],java.lang.Object)
meth public void setNodes({org.netbeans.modules.versioning.util.status.VCSStatusTable%0}[])
meth public void setSelectedNode({org.netbeans.modules.versioning.util.status.VCSStatusTable%0})
meth public void updateNodes(java.util.List<{org.netbeans.modules.versioning.util.status.VCSStatusTable%0}>,java.util.List<{org.netbeans.modules.versioning.util.status.VCSStatusTable%0}>,java.util.List<{org.netbeans.modules.versioning.util.status.VCSStatusTable%0}>)
meth public void valueChanged(javax.swing.event.ListSelectionEvent)
meth public {org.netbeans.modules.versioning.util.status.VCSStatusTable%0} getNextNode({org.netbeans.modules.versioning.util.status.VCSStatusTable%0})
meth public {org.netbeans.modules.versioning.util.status.VCSStatusTable%0} getNodeAtPosition(int)
meth public {org.netbeans.modules.versioning.util.status.VCSStatusTable%0} getPreviousNode({org.netbeans.modules.versioning.util.status.VCSStatusTable%0})
meth public {org.netbeans.modules.versioning.util.status.VCSStatusTable%0} getSelectedNode()
meth public {org.netbeans.modules.versioning.util.status.VCSStatusTable%0}[] getNeighbouringNodes({org.netbeans.modules.versioning.util.status.VCSStatusTable%0},int)
supr java.lang.Object
hfds LOG,component,support,table

CLSS protected static org.netbeans.modules.versioning.util.status.VCSStatusTable$ColumnDescriptor<%0 extends java.lang.Object>
 outer org.netbeans.modules.versioning.util.status.VCSStatusTable
cons public init(java.lang.String,java.lang.Class<{org.netbeans.modules.versioning.util.status.VCSStatusTable$ColumnDescriptor%0}>,java.lang.String,java.lang.String)
meth public {org.netbeans.modules.versioning.util.status.VCSStatusTable$ColumnDescriptor%0} getValue() throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
supr org.openide.nodes.PropertySupport$ReadOnly<{org.netbeans.modules.versioning.util.status.VCSStatusTable$ColumnDescriptor%0}>

CLSS public org.netbeans.modules.versioning.util.status.VCSStatusTableModel<%0 extends org.netbeans.modules.versioning.util.status.VCSStatusNode>
cons public init({org.netbeans.modules.versioning.util.status.VCSStatusTableModel%0}[])
meth public final java.lang.Class<{org.netbeans.modules.versioning.util.status.VCSStatusTableModel%0}> getItemClass()
meth public final void setNodes(org.openide.nodes.Node[])
meth public final void setNodes({org.netbeans.modules.versioning.util.status.VCSStatusTableModel%0}[])
meth public void add(java.util.List<{org.netbeans.modules.versioning.util.status.VCSStatusTableModel%0}>)
meth public void remove(java.util.List<{org.netbeans.modules.versioning.util.status.VCSStatusTableModel%0}>)
meth public {org.netbeans.modules.versioning.util.status.VCSStatusTableModel%0} getNode(int)
meth public {org.netbeans.modules.versioning.util.status.VCSStatusTableModel%0}[] getNodes()
supr org.openide.explorer.view.NodeTableModel
hfds nodes

CLSS public abstract interface !annotation org.netbeans.spi.editor.mimelookup.MimeLocation
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass()
meth public abstract java.lang.String subfolderName()

CLSS public abstract org.netbeans.spi.options.OptionsPanelController
cons public init()
fld public final static java.lang.String PROP_CHANGED = "changed"
fld public final static java.lang.String PROP_HELP_CTX = "helpCtx"
fld public final static java.lang.String PROP_VALID = "valid"
innr public abstract interface static !annotation ContainerRegistration
innr public abstract interface static !annotation Keywords
innr public abstract interface static !annotation KeywordsRegistration
innr public abstract interface static !annotation SubRegistration
innr public abstract interface static !annotation TopLevelRegistration
meth protected void setCurrentSubcategory(java.lang.String)
meth public abstract boolean isChanged()
meth public abstract boolean isValid()
meth public abstract javax.swing.JComponent getComponent(org.openide.util.Lookup)
meth public abstract org.openide.util.HelpCtx getHelpCtx()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void applyChanges()
meth public abstract void cancel()
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void update()
meth public final static org.netbeans.spi.options.OptionsPanelController createAdvanced(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public final void setSubcategory(java.lang.String)
meth public org.openide.util.Lookup getLookup()
meth public void handleSuccessfulSearch(java.lang.String,java.util.List<java.lang.String>)
supr java.lang.Object

CLSS public abstract interface org.netbeans.swing.outline.RenderDataProvider
meth public abstract boolean isHtmlDisplayName(java.lang.Object)
meth public abstract java.awt.Color getBackground(java.lang.Object)
meth public abstract java.awt.Color getForeground(java.lang.Object)
meth public abstract java.lang.String getDisplayName(java.lang.Object)
meth public abstract java.lang.String getTooltipText(java.lang.Object)
meth public abstract javax.swing.Icon getIcon(java.lang.Object)

CLSS public abstract interface org.openide.awt.UndoRedo
fld public final static org.openide.awt.UndoRedo NONE
innr public abstract interface static Provider
innr public final static Empty
innr public static Manager
meth public abstract boolean canRedo()
meth public abstract boolean canUndo()
meth public abstract java.lang.String getRedoPresentationName()
meth public abstract java.lang.String getUndoPresentationName()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void redo()
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public abstract void undo()

CLSS public abstract interface org.openide.cookies.SaveCookie
intf org.netbeans.api.actions.Savable
intf org.openide.nodes.Node$Cookie

CLSS public org.openide.explorer.view.NodeTableModel
cons public init()
meth protected org.openide.nodes.Node$Property getPropertyFor(org.openide.nodes.Node,org.openide.nodes.Node$Property)
meth public boolean isCellEditable(int,int)
meth public int getColumnCount()
meth public int getRowCount()
meth public java.lang.Class getColumnClass(int)
meth public java.lang.Object getValueAt(int,int)
meth public java.lang.String getColumnName(int)
meth public void setNodes(org.openide.nodes.Node[])
meth public void setProperties(org.openide.nodes.Node$Property[])
supr javax.swing.table.AbstractTableModel
hfds ATTR_COMPARABLE_COLUMN,ATTR_DESCENDING_ORDER,ATTR_DISPLAY_NAME_WITH_MNEMONIC,ATTR_INVISIBLE,ATTR_MNEMONIC_CHAR,ATTR_ORDER_NUMBER,ATTR_SORTING_COLUMN,ATTR_TREE_COLUMN,allPropertyColumns,existsComparableColumn,nodeRows,pcl,propertyColumns,sortColumn,treeColumnProperty
hcls ArrayColumn

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

CLSS public abstract static org.openide.nodes.Node$Property<%0 extends java.lang.Object>
 outer org.openide.nodes.Node
cons public init(java.lang.Class<{org.openide.nodes.Node$Property%0}>)
meth public abstract boolean canRead()
meth public abstract boolean canWrite()
meth public abstract void setValue({org.openide.nodes.Node$Property%0}) throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
meth public abstract {org.openide.nodes.Node$Property%0} getValue() throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
meth public boolean equals(java.lang.Object)
meth public boolean isDefaultValue()
meth public boolean supportsDefaultValue()
meth public int hashCode()
meth public java.beans.PropertyEditor getPropertyEditor()
meth public java.lang.Class<{org.openide.nodes.Node$Property%0}> getValueType()
meth public java.lang.String getHtmlDisplayName()
meth public void restoreDefaultValue() throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
supr java.beans.FeatureDescriptor
hfds edRef,type,warnedNames

CLSS public abstract org.openide.nodes.PropertySupport<%0 extends java.lang.Object>
cons public init(java.lang.String,java.lang.Class<{org.openide.nodes.PropertySupport%0}>,java.lang.String,java.lang.String,boolean,boolean)
innr public abstract static ReadOnly
innr public abstract static ReadWrite
innr public abstract static WriteOnly
innr public final static Name
innr public static Reflection
meth public boolean canRead()
meth public boolean canWrite()
meth public final org.openide.nodes.PropertySupport<{org.openide.nodes.PropertySupport%0}> withDisplayName(java.lang.String)
meth public final org.openide.nodes.PropertySupport<{org.openide.nodes.PropertySupport%0}> withShortDescription(java.lang.String)
meth public static <%0 extends java.lang.Object> org.openide.nodes.PropertySupport<{%%0}> readOnly(java.lang.String,java.lang.Class<{%%0}>,java.util.function.Supplier<{%%0}>)
meth public static <%0 extends java.lang.Object> org.openide.nodes.PropertySupport<{%%0}> readWrite(java.lang.String,java.lang.Class<{%%0}>,java.util.function.Supplier<{%%0}>,java.util.function.Consumer<{%%0}>)
meth public static <%0 extends java.lang.Object> org.openide.nodes.PropertySupport<{%%0}> writeOnly(java.lang.String,java.lang.Class<{%%0}>,java.util.function.Consumer<{%%0}>)
supr org.openide.nodes.Node$Property<{org.openide.nodes.PropertySupport%0}>
hfds canR,canW
hcls FunctionalProperty

CLSS public abstract static org.openide.nodes.PropertySupport$ReadOnly<%0 extends java.lang.Object>
 outer org.openide.nodes.PropertySupport
cons public init(java.lang.String,java.lang.Class<{org.openide.nodes.PropertySupport$ReadOnly%0}>,java.lang.String,java.lang.String)
meth public void setValue({org.openide.nodes.PropertySupport$ReadOnly%0}) throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
supr org.openide.nodes.PropertySupport<{org.openide.nodes.PropertySupport$ReadOnly%0}>

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

CLSS public org.openide.util.lookup.AbstractLookup
cons protected init()
cons public init(org.openide.util.lookup.AbstractLookup$Content)
innr public abstract static Pair
innr public static Content
intf java.io.Serializable
meth protected final void addPair(org.openide.util.lookup.AbstractLookup$Pair<?>)
meth protected final void addPair(org.openide.util.lookup.AbstractLookup$Pair<?>,java.util.concurrent.Executor)
meth protected final void removePair(org.openide.util.lookup.AbstractLookup$Pair<?>)
meth protected final void removePair(org.openide.util.lookup.AbstractLookup$Pair<?>,java.util.concurrent.Executor)
meth protected final void setPairs(java.util.Collection<? extends org.openide.util.lookup.AbstractLookup$Pair>)
meth protected final void setPairs(java.util.Collection<? extends org.openide.util.lookup.AbstractLookup$Pair>,java.util.concurrent.Executor)
meth protected void beforeLookup(org.openide.util.Lookup$Template<?>)
meth protected void initialize()
meth public final <%0 extends java.lang.Object> org.openide.util.Lookup$Item<{%%0}> lookupItem(org.openide.util.Lookup$Template<{%%0}>)
meth public final <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookup(org.openide.util.Lookup$Template<{%%0}>)
meth public final <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>)
meth public java.lang.String toString()
supr org.openide.util.Lookup
hfds LOG,count,serialVersionUID,tree,treeLock
hcls CycleError,ISE,Info,NotifyListeners,R,ReferenceIterator,ReferenceToResult,Storage

CLSS public org.openide.windows.TopComponent
cons public init()
cons public init(org.openide.util.Lookup)
fld public final static int CLOSE_EACH = 0
 anno 0 java.lang.Deprecated()
fld public final static int CLOSE_LAST = 1
 anno 0 java.lang.Deprecated()
fld public final static int PERSISTENCE_ALWAYS = 0
fld public final static int PERSISTENCE_NEVER = 2
fld public final static int PERSISTENCE_ONLY_OPENED = 1
fld public final static java.lang.String PROP_CLOSING_DISABLED = "netbeans.winsys.tc.closing_disabled"
fld public final static java.lang.String PROP_DND_COPY_DISABLED = "netbeans.winsys.tc.draganddrop_copy_disabled"
fld public final static java.lang.String PROP_DRAGGING_DISABLED = "netbeans.winsys.tc.dragging_disabled"
fld public final static java.lang.String PROP_KEEP_PREFERRED_SIZE_WHEN_SLIDED_IN = "netbeans.winsys.tc.keep_preferred_size_when_slided_in"
fld public final static java.lang.String PROP_MAXIMIZATION_DISABLED = "netbeans.winsys.tc.maximization_disabled"
fld public final static java.lang.String PROP_SLIDING_DISABLED = "netbeans.winsys.tc.sliding_disabled"
fld public final static java.lang.String PROP_UNDOCKING_DISABLED = "netbeans.winsys.tc.undocking_disabled"
innr public abstract interface static !annotation Description
innr public abstract interface static !annotation OpenActionRegistration
innr public abstract interface static !annotation Registration
innr public abstract interface static Cloneable
innr public abstract interface static Registry
innr public final static SubComponent
innr public static NodeName
intf java.io.Externalizable
intf javax.accessibility.Accessible
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.Lookup$Provider
meth protected boolean processKeyBinding(javax.swing.KeyStroke,java.awt.event.KeyEvent,int,boolean)
meth protected final void associateLookup(org.openide.util.Lookup)
meth protected java.lang.Object writeReplace() throws java.io.ObjectStreamException
meth protected java.lang.String preferredID()
meth protected void closeNotify()
 anno 0 java.lang.Deprecated()
meth protected void componentActivated()
meth protected void componentClosed()
meth protected void componentDeactivated()
meth protected void componentHidden()
meth protected void componentOpened()
meth protected void componentShowing()
meth protected void openNotify()
 anno 0 java.lang.Deprecated()
meth public boolean canClose()
meth public boolean canClose(org.openide.windows.Workspace,boolean)
 anno 0 java.lang.Deprecated()
meth public boolean requestFocusInWindow()
meth public final boolean close()
meth public final boolean close(org.openide.windows.Workspace)
 anno 0 java.lang.Deprecated()
meth public final boolean isOpened()
meth public final boolean isOpened(org.openide.windows.Workspace)
 anno 0 java.lang.Deprecated()
meth public final int getCloseOperation()
 anno 0 java.lang.Deprecated()
meth public final int getTabPosition()
meth public final org.openide.nodes.Node[] getActivatedNodes()
meth public final static org.openide.windows.TopComponent$Registry getRegistry()
meth public final void cancelRequestAttention()
meth public final void makeBusy(boolean)
meth public final void openAtTabPosition(int)
meth public final void requestAttention(boolean)
meth public final void setActivatedNodes(org.openide.nodes.Node[])
meth public final void setAttentionHighlight(boolean)
meth public final void setCloseOperation(int)
 anno 0 java.lang.Deprecated()
meth public int getPersistenceType()
meth public java.awt.Image getIcon()
meth public java.lang.String getDisplayName()
meth public java.lang.String getHtmlDisplayName()
meth public java.lang.String getShortName()
meth public java.util.List<org.openide.windows.Mode> availableModes(java.util.List<org.openide.windows.Mode>)
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.Action[] getActions()
meth public org.openide.awt.UndoRedo getUndoRedo()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public org.openide.util.Lookup getLookup()
meth public org.openide.util.actions.SystemAction[] getSystemActions()
 anno 0 java.lang.Deprecated()
meth public org.openide.windows.TopComponent$SubComponent[] getSubComponents()
meth public static javax.swing.Action openAction(org.openide.windows.TopComponent,java.lang.String,java.lang.String,boolean)
meth public void addNotify()
meth public void open()
meth public void open(org.openide.windows.Workspace)
 anno 0 java.lang.Deprecated()
meth public void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void requestActive()
meth public void requestFocus()
meth public void requestVisible()
meth public void setDisplayName(java.lang.String)
meth public void setHtmlDisplayName(java.lang.String)
meth public void setIcon(java.awt.Image)
meth public void setName(java.lang.String)
meth public void setToolTipText(java.lang.String)
meth public void toFront()
meth public void writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr javax.swing.JComponent
hfds LOG,MODE_ID_PREFERENCES_KEY_INFIX,UILOG,activatedNodes,attentionGetter,closeOperation,defaultLookupLock,defaultLookupRef,displayName,htmlDisplayName,icon,modeName,nodeName,serialVersion,serialVersionUID,warnedClasses,warnedTCPIClasses
hcls AttentionGetter,CloneWindowAction,CloseWindowAction,Replacer,SynchronizeNodes

