#Signature file v4.1
#Version 6.63

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

CLSS public abstract interface java.awt.event.FocusListener
intf java.util.EventListener
meth public abstract void focusGained(java.awt.event.FocusEvent)
meth public abstract void focusLost(java.awt.event.FocusEvent)

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

CLSS public abstract interface java.beans.PropertyChangeListener
intf java.util.EventListener
meth public abstract void propertyChange(java.beans.PropertyChangeEvent)

CLSS public abstract interface java.beans.VetoableChangeListener
intf java.util.EventListener
meth public abstract void vetoableChange(java.beans.PropertyChangeEvent) throws java.beans.PropertyVetoException

CLSS public abstract interface java.io.Externalizable
intf java.io.Serializable
meth public abstract void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public abstract void writeExternal(java.io.ObjectOutput) throws java.io.IOException

CLSS public java.io.IOException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

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

CLSS public java.lang.RuntimeException
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

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

CLSS public abstract java.text.Format
cons protected init()
innr public static Field
intf java.io.Serializable
intf java.lang.Cloneable
meth public abstract java.lang.Object parseObject(java.lang.String,java.text.ParsePosition)
meth public abstract java.lang.StringBuffer format(java.lang.Object,java.lang.StringBuffer,java.text.FieldPosition)
meth public final java.lang.String format(java.lang.Object)
meth public java.lang.Object clone()
meth public java.lang.Object parseObject(java.lang.String) throws java.text.ParseException
meth public java.text.AttributedCharacterIterator formatToCharacterIterator(java.lang.Object)
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

CLSS public abstract interface java.util.EventListener

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object

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

CLSS public abstract interface java.util.concurrent.Executor
meth public abstract void execute(java.lang.Runnable)

CLSS public abstract interface java.util.concurrent.ExecutorService
intf java.util.concurrent.Executor
meth public abstract <%0 extends java.lang.Object> java.util.List<java.util.concurrent.Future<{%%0}>> invokeAll(java.util.Collection<? extends java.util.concurrent.Callable<{%%0}>>) throws java.lang.InterruptedException
meth public abstract <%0 extends java.lang.Object> java.util.List<java.util.concurrent.Future<{%%0}>> invokeAll(java.util.Collection<? extends java.util.concurrent.Callable<{%%0}>>,long,java.util.concurrent.TimeUnit) throws java.lang.InterruptedException
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> submit(java.lang.Runnable,{%%0})
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> submit(java.util.concurrent.Callable<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} invokeAny(java.util.Collection<? extends java.util.concurrent.Callable<{%%0}>>) throws java.lang.InterruptedException,java.util.concurrent.ExecutionException
meth public abstract <%0 extends java.lang.Object> {%%0} invokeAny(java.util.Collection<? extends java.util.concurrent.Callable<{%%0}>>,long,java.util.concurrent.TimeUnit) throws java.lang.InterruptedException,java.util.concurrent.ExecutionException,java.util.concurrent.TimeoutException
meth public abstract boolean awaitTermination(long,java.util.concurrent.TimeUnit) throws java.lang.InterruptedException
meth public abstract boolean isShutdown()
meth public abstract boolean isTerminated()
meth public abstract java.util.List<java.lang.Runnable> shutdownNow()
meth public abstract java.util.concurrent.Future<?> submit(java.lang.Runnable)
meth public abstract void shutdown()

CLSS public abstract interface java.util.concurrent.ScheduledExecutorService
intf java.util.concurrent.ExecutorService
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.ScheduledFuture<{%%0}> schedule(java.util.concurrent.Callable<{%%0}>,long,java.util.concurrent.TimeUnit)
meth public abstract java.util.concurrent.ScheduledFuture<?> schedule(java.lang.Runnable,long,java.util.concurrent.TimeUnit)
meth public abstract java.util.concurrent.ScheduledFuture<?> scheduleAtFixedRate(java.lang.Runnable,long,long,java.util.concurrent.TimeUnit)
meth public abstract java.util.concurrent.ScheduledFuture<?> scheduleWithFixedDelay(java.lang.Runnable,long,long,java.util.concurrent.TimeUnit)

CLSS public abstract interface javax.accessibility.Accessible
meth public abstract javax.accessibility.AccessibleContext getAccessibleContext()

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

CLSS public abstract interface javax.swing.Icon
meth public abstract int getIconHeight()
meth public abstract int getIconWidth()
meth public abstract void paintIcon(java.awt.Component,java.awt.Graphics,int,int)

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

CLSS public abstract interface javax.swing.event.ChangeListener
intf java.util.EventListener
meth public abstract void stateChanged(javax.swing.event.ChangeEvent)

CLSS public abstract interface javax.swing.event.DocumentListener
intf java.util.EventListener
meth public abstract void changedUpdate(javax.swing.event.DocumentEvent)
meth public abstract void insertUpdate(javax.swing.event.DocumentEvent)
meth public abstract void removeUpdate(javax.swing.event.DocumentEvent)

CLSS public org.openide.explorer.ExplorerActions
 anno 0 java.lang.Deprecated()
cons public init()
meth public final boolean isConfirmDelete()
meth public final void setConfirmDelete(boolean)
meth public void attach(org.openide.explorer.ExplorerManager)
meth public void detach()
supr java.lang.Object
hfds actionStateUpdater,attachPerformers,confirmDelete,copy,copyActionPerformer,cut,cutActionPerformer,delete,deleteActionPerformer,manager,paste,pasteActionPerformer
hcls ActionStateUpdater,CopyCutActionPerformer,DeleteActionPerformer,FixIssue29405Timer,MultiPasteType,OwnPaste

CLSS public final org.openide.explorer.ExplorerManager
cons public init()
fld public final static java.lang.String PROP_EXPLORED_CONTEXT = "exploredContext"
fld public final static java.lang.String PROP_NODE_CHANGE = "nodeChange"
fld public final static java.lang.String PROP_ROOT_CONTEXT = "rootContext"
fld public final static java.lang.String PROP_SELECTED_NODES = "selectedNodes"
innr public abstract interface static Provider
intf java.io.Serializable
intf java.lang.Cloneable
meth public final org.openide.nodes.Node getExploredContext()
meth public final org.openide.nodes.Node getRootContext()
meth public final void setExploredContext(org.openide.nodes.Node)
meth public final void setExploredContext(org.openide.nodes.Node,org.openide.nodes.Node[])
meth public final void setExploredContextAndSelection(org.openide.nodes.Node,org.openide.nodes.Node[]) throws java.beans.PropertyVetoException
meth public final void setRootContext(org.openide.nodes.Node)
meth public final void setSelectedNodes(org.openide.nodes.Node[]) throws java.beans.PropertyVetoException
meth public org.openide.explorer.ExplorerManager clone()
meth public org.openide.nodes.Node[] getSelectedNodes()
meth public static org.openide.explorer.ExplorerManager find(java.awt.Component)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addVetoableChangeListener(java.beans.VetoableChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removeVetoableChangeListener(java.beans.VetoableChangeListener)
supr java.lang.Object
hfds LOCK,SCHEDULE_REMOVE_ASYNCH,SELECTION_SYNC_DELAY,actions,exploredContext,listener,propertySupport,rootContext,selectedNodes,selectionProcessor,selectionSyncTask,serialPersistentFields,serialVersionUID,vetoableSupport,weakListener
hcls Listener

CLSS public abstract interface static org.openide.explorer.ExplorerManager$Provider
 outer org.openide.explorer.ExplorerManager
meth public abstract org.openide.explorer.ExplorerManager getExplorerManager()

CLSS public org.openide.explorer.ExplorerPanel
 anno 0 java.lang.Deprecated()
cons public init()
cons public init(org.openide.explorer.ExplorerManager)
intf org.openide.explorer.ExplorerManager$Provider
meth protected void componentActivated()
meth protected void componentDeactivated()
meth protected void updateTitle()
meth public org.openide.explorer.ExplorerManager getExplorerManager()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static boolean isConfirmDelete()
meth public static org.openide.util.HelpCtx getHelpCtx(org.openide.nodes.Node[],org.openide.util.HelpCtx)
meth public static void setConfirmDelete(boolean)
meth public void open()
meth public void open(org.openide.windows.Workspace)
meth public void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr org.openide.windows.TopComponent
hfds INIT_DELAY,MAX_DELAY,actions,delayedSetter,formatExplorerTitle,manager,managerListener,panels,scheduleAcivatedNodes,serialVersionUID
hcls DelayedSetter,PropL

CLSS public final org.openide.explorer.ExplorerUtils
meth public static javax.swing.Action actionCopy(org.openide.explorer.ExplorerManager)
meth public static javax.swing.Action actionCut(org.openide.explorer.ExplorerManager)
meth public static javax.swing.Action actionDelete(org.openide.explorer.ExplorerManager,boolean)
meth public static javax.swing.Action actionPaste(org.openide.explorer.ExplorerManager)
meth public static org.openide.util.HelpCtx getHelpCtx(org.openide.nodes.Node[],org.openide.util.HelpCtx)
meth public static org.openide.util.Lookup createLookup(org.openide.explorer.ExplorerManager,javax.swing.ActionMap)
meth public static void activateActions(org.openide.explorer.ExplorerManager,boolean)
supr java.lang.Object

CLSS public abstract interface org.openide.explorer.ExtendedDelete
meth public abstract boolean delete(org.openide.nodes.Node[]) throws java.io.IOException

CLSS public abstract interface org.openide.filesystems.FileChangeListener
intf java.util.EventListener
meth public abstract void fileAttributeChanged(org.openide.filesystems.FileAttributeEvent)
meth public abstract void fileChanged(org.openide.filesystems.FileEvent)
meth public abstract void fileDataCreated(org.openide.filesystems.FileEvent)
meth public abstract void fileDeleted(org.openide.filesystems.FileEvent)
meth public abstract void fileFolderCreated(org.openide.filesystems.FileEvent)
meth public abstract void fileRenamed(org.openide.filesystems.FileRenameEvent)

CLSS public abstract interface org.openide.filesystems.FileStatusListener
intf java.util.EventListener
meth public abstract void annotationChanged(org.openide.filesystems.FileStatusEvent)

CLSS public abstract interface org.openide.filesystems.RepositoryListener
intf java.util.EventListener
meth public abstract void fileSystemAdded(org.openide.filesystems.RepositoryEvent)
meth public abstract void fileSystemPoolReordered(org.openide.filesystems.RepositoryReorderedEvent)
meth public abstract void fileSystemRemoved(org.openide.filesystems.RepositoryEvent)

CLSS public abstract interface org.openide.nodes.NodeListener
intf java.beans.PropertyChangeListener
meth public abstract void childrenAdded(org.openide.nodes.NodeMemberEvent)
meth public abstract void childrenRemoved(org.openide.nodes.NodeMemberEvent)
meth public abstract void childrenReordered(org.openide.nodes.NodeReorderEvent)
meth public abstract void nodeDestroyed(org.openide.nodes.NodeEvent)

CLSS public abstract interface org.openide.util.AsyncGUIJob
meth public abstract void construct()
meth public abstract void finished()

CLSS public abstract org.openide.util.BaseUtilities
fld public final static int OS_AIX = 64
fld public final static int OS_DEC = 1024
 anno 0 java.lang.Deprecated()
fld public final static int OS_FREEBSD = 131072
fld public final static int OS_HP = 32
fld public final static int OS_IRIX = 128
fld public final static int OS_LINUX = 16
fld public final static int OS_MAC = 4096
fld public final static int OS_OPENBSD = 1048576
fld public final static int OS_OS2 = 2048
fld public final static int OS_OTHER = 65536
fld public final static int OS_SOLARIS = 8
fld public final static int OS_SUNOS = 256
fld public final static int OS_TRU64 = 512
fld public final static int OS_UNIX_MASK = 1709048
 anno 0 java.lang.Deprecated()
fld public final static int OS_UNIX_OTHER = 524288
fld public final static int OS_VMS = 16384
fld public final static int OS_WIN2000 = 8192
fld public final static int OS_WIN95 = 2
fld public final static int OS_WIN98 = 4
fld public final static int OS_WINDOWS_MASK = 303111
 anno 0 java.lang.Deprecated()
fld public final static int OS_WINNT = 1
fld public final static int OS_WINVISTA = 262144
fld public final static int OS_WIN_OTHER = 32768
meth public static <%0 extends java.lang.Object> java.util.List<{%%0}> topologicalSort(java.util.Collection<? extends {%%0}>,java.util.Map<? super {%%0},? extends java.util.Collection<? extends {%%0}>>) throws org.openide.util.TopologicalSortException
meth public static boolean compareObjects(java.lang.Object,java.lang.Object)
meth public static boolean compareObjectsImpl(java.lang.Object,java.lang.Object,int)
meth public static boolean isJavaIdentifier(java.lang.String)
meth public static boolean isMac()
meth public static boolean isUnix()
meth public static boolean isWindows()
meth public static int getOperatingSystem()
meth public static java.io.File toFile(java.net.URI)
meth public static java.lang.Class<?> getObjectType(java.lang.Class<?>)
meth public static java.lang.Class<?> getPrimitiveType(java.lang.Class<?>)
meth public static java.lang.Object toPrimitiveArray(java.lang.Object[])
meth public static java.lang.Object[] toObjectArray(java.lang.Object)
meth public static java.lang.String escapeParameters(java.lang.String[])
meth public static java.lang.String getClassName(java.lang.Class<?>)
meth public static java.lang.String getShortClassName(java.lang.Class<?>)
meth public static java.lang.String pureClassName(java.lang.String)
meth public static java.lang.String translate(java.lang.String)
meth public static java.lang.String wrapString(java.lang.String,int,java.text.BreakIterator,boolean)
meth public static java.lang.String[] parseParameters(java.lang.String)
meth public static java.lang.String[] wrapStringToArray(java.lang.String,int,java.text.BreakIterator,boolean)
meth public static java.lang.ref.ReferenceQueue<java.lang.Object> activeReferenceQueue()
meth public static java.net.URI normalizeURI(java.net.URI)
meth public static java.net.URI toURI(java.io.File)
supr java.lang.Object
hfds LOG,TRANS_LOCK,operatingSystem,pathURIConsistent,transExp,transLoader
hcls RE

CLSS public abstract org.openide.util.CachedHiDPIIcon
cons protected init(int,int)
intf javax.swing.Icon
meth protected abstract java.awt.Image createAndPaintImage(java.awt.Component,java.awt.image.ColorModel,int,int,double)
meth protected final static java.awt.image.BufferedImage createBufferedImage(java.awt.image.ColorModel,int,int)
meth public final int getIconHeight()
meth public final int getIconWidth()
meth public final void paintIcon(java.awt.Component,java.awt.Graphics,int,int)
supr java.lang.Object
hfds MAX_CACHE_SIZE,cache,cacheSize,height,width
hcls CachedImageKey

CLSS public abstract interface org.openide.util.Cancellable
meth public abstract boolean cancel()

CLSS public final org.openide.util.ChangeSupport
cons public init(java.lang.Object)
meth public boolean hasListeners()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void fireChange()
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
hfds LOG,listeners,source

CLSS public final org.openide.util.CharSequences
meth public static boolean isCompact(java.lang.CharSequence)
meth public static int indexOf(java.lang.CharSequence,java.lang.CharSequence)
meth public static int indexOf(java.lang.CharSequence,java.lang.CharSequence,int)
meth public static java.lang.CharSequence create(char[],int,int)
meth public static java.lang.CharSequence create(java.lang.CharSequence)
meth public static java.lang.CharSequence empty()
meth public static java.util.Comparator<java.lang.CharSequence> comparator()
supr java.lang.Object
hfds Comparator,EMPTY,decodeTable,encodeTable
hcls ByteBasedSequence,CharBasedSequence,CharSequenceComparator,CompactCharSequence,Fixed6Bit_11_20,Fixed6Bit_1_10,Fixed6Bit_21_30,Fixed_0_7,Fixed_16_23,Fixed_8_15

CLSS public abstract interface org.openide.util.ContextAwareAction
intf javax.swing.Action
meth public abstract javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)

CLSS public abstract interface org.openide.util.ContextGlobalProvider
meth public abstract org.openide.util.Lookup createGlobalContext()

CLSS public final org.openide.util.EditableProperties
cons public init(boolean)
intf java.lang.Cloneable
meth public java.lang.Object clone()
meth public java.lang.String get(java.lang.Object)
meth public java.lang.String getProperty(java.lang.String)
meth public java.lang.String put(java.lang.String,java.lang.String)
meth public java.lang.String setProperty(java.lang.String,java.lang.String)
meth public java.lang.String setProperty(java.lang.String,java.lang.String[])
meth public java.lang.String[] getComment(java.lang.String)
meth public java.util.Set<java.util.Map$Entry<java.lang.String,java.lang.String>> entrySet()
meth public org.openide.util.EditableProperties cloneProperties()
meth public void load(java.io.InputStream) throws java.io.IOException
meth public void setComment(java.lang.String,java.lang.String[],boolean)
meth public void store(java.io.OutputStream) throws java.io.IOException
supr java.util.AbstractMap<java.lang.String,java.lang.String>
hfds INDENT,READING_KEY_VALUE,WAITING_FOR_KEY_VALUE,alphabetize,state
hcls Item,IteratorImpl,MapEntryImpl,SetImpl,State

CLSS public final org.openide.util.Enumerations
innr public abstract interface static Processor
meth public !varargs static <%0 extends java.lang.Object> java.util.Enumeration<{%%0}> array({%%0}[])
meth public final static <%0 extends java.lang.Object> java.util.Enumeration<{%%0}> empty()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Enumeration<{%%1}> convert(java.util.Enumeration<? extends {%%0}>,org.openide.util.Enumerations$Processor<{%%0},{%%1}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Enumeration<{%%1}> filter(java.util.Enumeration<? extends {%%0}>,org.openide.util.Enumerations$Processor<{%%0},{%%1}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Enumeration<{%%1}> queue(java.util.Enumeration<? extends {%%0}>,org.openide.util.Enumerations$Processor<{%%0},{%%1}>)
meth public static <%0 extends java.lang.Object> java.util.Enumeration<{%%0}> concat(java.util.Enumeration<? extends java.util.Enumeration<? extends {%%0}>>)
meth public static <%0 extends java.lang.Object> java.util.Enumeration<{%%0}> concat(java.util.Enumeration<? extends {%%0}>,java.util.Enumeration<? extends {%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Enumeration<{%%0}> removeDuplicates(java.util.Enumeration<{%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Enumeration<{%%0}> removeNulls(java.util.Enumeration<{%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Enumeration<{%%0}> singleton({%%0})
supr java.lang.Object
hcls AltEn,FilEn,QEn,RNulls,SeqEn

CLSS public abstract interface static org.openide.util.Enumerations$Processor<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer org.openide.util.Enumerations
meth public abstract {org.openide.util.Enumerations$Processor%1} process({org.openide.util.Enumerations$Processor%0},java.util.Collection<{org.openide.util.Enumerations$Processor%0}>)

CLSS public final org.openide.util.Exceptions
meth public static <%0 extends java.lang.Throwable> {%%0} attachLocalizedMessage({%%0},java.lang.String)
meth public static <%0 extends java.lang.Throwable> {%%0} attachMessage({%%0},java.lang.String)
meth public static <%0 extends java.lang.Throwable> {%%0} attachSeverity({%%0},java.util.logging.Level)
meth public static java.lang.String findLocalizedMessage(java.lang.Throwable)
meth public static void printStackTrace(java.lang.Throwable)
supr java.lang.Object
hfds LOC_MSG_PLACEHOLDER,LOG
hcls AnnException,OwnLevel

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

CLSS public abstract interface static org.openide.util.HelpCtx$Displayer
 outer org.openide.util.HelpCtx
meth public abstract boolean display(org.openide.util.HelpCtx)

CLSS public abstract interface static org.openide.util.HelpCtx$Provider
 outer org.openide.util.HelpCtx
meth public abstract org.openide.util.HelpCtx getHelpCtx()

CLSS public abstract org.openide.util.HttpServer
 anno 0 java.lang.Deprecated()
innr public abstract interface static Impl
meth public static boolean allowAccess(java.net.InetAddress) throws java.net.UnknownHostException
 anno 0 java.lang.Deprecated()
meth public static java.net.URL getRepositoryRoot() throws java.net.MalformedURLException,java.net.UnknownHostException
 anno 0 java.lang.Deprecated()
meth public static java.net.URL getRepositoryURL(org.openide.filesystems.FileObject) throws java.net.MalformedURLException,java.net.UnknownHostException
 anno 0 java.lang.Deprecated()
meth public static java.net.URL getResourceRoot() throws java.net.MalformedURLException,java.net.UnknownHostException
 anno 0 java.lang.Deprecated()
meth public static java.net.URL getResourceURL(java.lang.String) throws java.net.MalformedURLException,java.net.UnknownHostException
 anno 0 java.lang.Deprecated()
meth public static void deregisterServer(org.openide.util.HttpServer$Impl)
 anno 0 java.lang.Deprecated()
meth public static void registerServer(org.openide.util.HttpServer$Impl)
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds registeredServer

CLSS public abstract interface static org.openide.util.HttpServer$Impl
 outer org.openide.util.HttpServer
 anno 0 java.lang.Deprecated()
meth public abstract boolean allowAccess(java.net.InetAddress) throws java.net.UnknownHostException
meth public abstract java.net.URL getRepositoryRoot() throws java.net.MalformedURLException,java.net.UnknownHostException
meth public abstract java.net.URL getRepositoryURL(org.openide.filesystems.FileObject) throws java.net.MalformedURLException,java.net.UnknownHostException
meth public abstract java.net.URL getResourceRoot() throws java.net.MalformedURLException,java.net.UnknownHostException
meth public abstract java.net.URL getResourceURL(java.lang.String) throws java.net.MalformedURLException,java.net.UnknownHostException

CLSS public final org.openide.util.ImageUtilities
fld public final static java.lang.String PROPERTY_URL = "url"
meth public final static java.awt.Image addToolTipToImage(java.awt.Image,java.lang.String)
meth public final static java.awt.Image assignToolTipToImage(java.awt.Image,java.lang.String)
meth public final static java.awt.Image icon2Image(javax.swing.Icon)
meth public final static java.awt.Image loadImage(java.lang.String)
meth public final static java.awt.Image loadImage(java.lang.String,boolean)
meth public final static java.awt.Image mergeImages(java.awt.Image,java.awt.Image,int,int)
meth public final static java.lang.String getImageToolTip(java.awt.Image)
meth public final static javax.swing.Icon image2Icon(java.awt.Image)
meth public final static javax.swing.ImageIcon loadImageIcon(java.lang.String,boolean)
meth public static java.awt.Image createDisabledImage(java.awt.Image)
meth public static java.net.URL findImageBaseURL(java.awt.Image)
meth public static javax.swing.Icon createDisabledIcon(javax.swing.Icon)
supr java.lang.Object
hfds DARK_LAF_SUFFIX,ERR,LOGGER,NO_ICON,PNG_READER,TOOLTIP_SEPAR,cache,classLoaderLoader,component,compositeCache,dummyIconComponentButton,dummyIconComponentLabel,extraInitialSlashes,imageIconFilter,imageToolTipCache,localizedCache,mediaTrackerID,svgLoaderLoader,tracker
hcls ActiveRef,CachedLookupLoader,CompositeImageKey,DisabledButtonFilter,IconImageIcon,MergedIcon,ToolTipImage,ToolTipImageKey

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

CLSS public abstract static org.openide.util.Lookup$Item<%0 extends java.lang.Object>
 outer org.openide.util.Lookup
cons public init()
meth public abstract java.lang.Class<? extends {org.openide.util.Lookup$Item%0}> getType()
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getId()
meth public abstract {org.openide.util.Lookup$Item%0} getInstance()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public abstract interface static org.openide.util.Lookup$Provider
 outer org.openide.util.Lookup
meth public abstract org.openide.util.Lookup getLookup()

CLSS public abstract static org.openide.util.Lookup$Result<%0 extends java.lang.Object>
 outer org.openide.util.Lookup
cons public init()
meth public abstract java.util.Collection<? extends {org.openide.util.Lookup$Result%0}> allInstances()
meth public abstract void addLookupListener(org.openide.util.LookupListener)
meth public abstract void removeLookupListener(org.openide.util.LookupListener)
meth public java.util.Collection<? extends org.openide.util.Lookup$Item<{org.openide.util.Lookup$Result%0}>> allItems()
meth public java.util.Set<java.lang.Class<? extends {org.openide.util.Lookup$Result%0}>> allClasses()
supr java.lang.Object

CLSS public final static org.openide.util.Lookup$Template<%0 extends java.lang.Object>
 outer org.openide.util.Lookup
cons public init()
 anno 0 java.lang.Deprecated()
cons public init(java.lang.Class<{org.openide.util.Lookup$Template%0}>)
cons public init(java.lang.Class<{org.openide.util.Lookup$Template%0}>,java.lang.String,{org.openide.util.Lookup$Template%0})
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.Class<{org.openide.util.Lookup$Template%0}> getType()
meth public java.lang.String getId()
meth public java.lang.String toString()
meth public {org.openide.util.Lookup$Template%0} getInstance()
supr java.lang.Object
hfds hashCode,id,instance,type

CLSS public final org.openide.util.LookupEvent
cons public init(org.openide.util.Lookup$Result)
supr java.util.EventObject

CLSS public abstract interface org.openide.util.LookupListener
intf java.util.EventListener
meth public abstract void resultChanged(org.openide.util.LookupEvent)

CLSS public org.openide.util.MapFormat
cons public init(java.util.Map<java.lang.String,?>)
meth protected java.lang.Object processKey(java.lang.String)
meth public boolean isExactMatch()
meth public boolean willThrowExceptionIfKeyWasNotFound()
meth public java.lang.Object parseObject(java.lang.String,java.text.ParsePosition)
meth public java.lang.String getLeftBrace()
meth public java.lang.String getRightBrace()
meth public java.lang.String parse(java.lang.String)
meth public java.lang.String processPattern(java.lang.String)
meth public java.lang.StringBuffer format(java.lang.Object,java.lang.StringBuffer,java.text.FieldPosition)
meth public java.util.Map getMap()
meth public static java.lang.String format(java.lang.String,java.util.Map)
meth public void setExactMatch(boolean)
meth public void setLeftBrace(java.lang.String)
meth public void setMap(java.util.Map<java.lang.String,?>)
meth public void setRightBrace(java.lang.String)
meth public void setThrowExceptionIfKeyWasNotFound(boolean)
supr java.text.Format
hfds BUFSIZE,argmap,arguments,exactmatch,ldel,locale,maxOffset,offsets,rdel,serialVersionUID,throwex

CLSS public final org.openide.util.Mutex
cons public init()
cons public init(java.lang.Object)
cons public init(org.openide.util.Mutex$Privileged)
cons public init(org.openide.util.Mutex$Privileged,java.util.concurrent.Executor)
cons public init(org.openide.util.spi.MutexImplementation)
fld public final static org.openide.util.Mutex EVENT
innr public abstract interface static Action
innr public abstract interface static ExceptionAction
innr public final static Privileged
meth public <%0 extends java.lang.Object> {%%0} readAccess(org.openide.util.Mutex$Action<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} readAccess(org.openide.util.Mutex$ExceptionAction<{%%0}>) throws org.openide.util.MutexException
meth public <%0 extends java.lang.Object> {%%0} writeAccess(org.openide.util.Mutex$Action<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} writeAccess(org.openide.util.Mutex$ExceptionAction<{%%0}>) throws org.openide.util.MutexException
meth public boolean isReadAccess()
meth public boolean isWriteAccess()
meth public java.lang.String toString()
meth public void postReadRequest(java.lang.Runnable)
meth public void postWriteRequest(java.lang.Runnable)
meth public void readAccess(java.lang.Runnable)
meth public void writeAccess(java.lang.Runnable)
supr java.lang.Object
hfds LOG,impl

CLSS public abstract interface static org.openide.util.Mutex$Action<%0 extends java.lang.Object>
 outer org.openide.util.Mutex
intf org.openide.util.Mutex$ExceptionAction<{org.openide.util.Mutex$Action%0}>
meth public abstract {org.openide.util.Mutex$Action%0} run()

CLSS public abstract interface static org.openide.util.Mutex$ExceptionAction<%0 extends java.lang.Object>
 outer org.openide.util.Mutex
meth public abstract {org.openide.util.Mutex$ExceptionAction%0} run() throws java.lang.Exception

CLSS public final static org.openide.util.Mutex$Privileged
 outer org.openide.util.Mutex
cons public init()
meth public boolean tryReadAccess(long)
meth public boolean tryWriteAccess(long)
meth public void enterReadAccess()
meth public void enterWriteAccess()
meth public void exitReadAccess()
meth public void exitWriteAccess()
supr java.lang.Object
hfds delegate

CLSS public org.openide.util.MutexException
cons public init(java.lang.Exception)
meth public java.lang.Exception getException()
meth public java.lang.Throwable getCause()
supr java.lang.Exception
hfds ex,serialVersionUID

CLSS public org.openide.util.NbBundle
cons public init()
 anno 0 java.lang.Deprecated()
innr public abstract interface static !annotation Messages
innr public abstract interface static ClassLoaderFinder
meth public !varargs static java.lang.String getMessage(java.lang.Class<?>,java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object[])
meth public static <%0 extends java.lang.Object> {%%0} getLocalizedValue(java.util.Map<java.lang.String,{%%0}>,java.lang.String)
meth public static <%0 extends java.lang.Object> {%%0} getLocalizedValue(java.util.Map<java.lang.String,{%%0}>,java.lang.String,java.util.Locale)
meth public static java.lang.String getBranding()
meth public static java.lang.String getLocalizedValue(java.util.jar.Attributes,java.util.jar.Attributes$Name)
meth public static java.lang.String getLocalizedValue(java.util.jar.Attributes,java.util.jar.Attributes$Name,java.util.Locale)
meth public static java.lang.String getMessage(java.lang.Class<?>,java.lang.String)
meth public static java.lang.String getMessage(java.lang.Class<?>,java.lang.String,java.lang.Object)
meth public static java.lang.String getMessage(java.lang.Class<?>,java.lang.String,java.lang.Object,java.lang.Object)
meth public static java.lang.String getMessage(java.lang.Class<?>,java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object)
meth public static java.lang.String getMessage(java.lang.Class<?>,java.lang.String,java.lang.Object[])
meth public static java.net.URL getLocalizedFile(java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static java.net.URL getLocalizedFile(java.lang.String,java.lang.String,java.util.Locale)
 anno 0 java.lang.Deprecated()
meth public static java.net.URL getLocalizedFile(java.lang.String,java.lang.String,java.util.Locale,java.lang.ClassLoader)
 anno 0 java.lang.Deprecated()
meth public static java.util.Iterator<java.lang.String> getLocalizingSuffixes()
meth public static java.util.ResourceBundle getBundle(java.lang.Class<?>)
meth public static java.util.ResourceBundle getBundle(java.lang.String)
meth public static java.util.ResourceBundle getBundle(java.lang.String,java.util.Locale)
meth public static java.util.ResourceBundle getBundle(java.lang.String,java.util.Locale,java.lang.ClassLoader)
meth public static void setBranding(java.lang.String)
meth public static void setClassLoaderFinder(org.openide.util.NbBundle$ClassLoaderFinder)
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds LOG,USE_DEBUG_LOADER,brandingToken,bundleCache,localizedFileCache,utfThenIsoCharset,utfThenIsoCharsetOnlyUTF8
hcls AttributesMap,DebugLoader,LocaleIterator,MergedBundle,PBundle,UtfThenIsoCharset

CLSS public abstract interface static org.openide.util.NbBundle$ClassLoaderFinder
 outer org.openide.util.NbBundle
 anno 0 java.lang.Deprecated()
meth public abstract java.lang.ClassLoader find()
 anno 0 java.lang.Deprecated()

CLSS public abstract interface static !annotation org.openide.util.NbBundle$Messages
 outer org.openide.util.NbBundle
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PACKAGE, TYPE, METHOD, CONSTRUCTOR, FIELD])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String[] value()

CLSS public org.openide.util.NbCollections
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> checkedMapByCopy(java.util.Map,java.lang.Class<{%%0}>,java.lang.Class<{%%1}>,boolean)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> checkedMapByFilter(java.util.Map,java.lang.Class<{%%0}>,java.lang.Class<{%%1}>,boolean)
meth public static <%0 extends java.lang.Object> java.lang.Iterable<{%%0}> iterable(java.util.Enumeration<{%%0}>)
meth public static <%0 extends java.lang.Object> java.lang.Iterable<{%%0}> iterable(java.util.Iterator<{%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Enumeration<{%%0}> checkedEnumerationByFilter(java.util.Enumeration<?>,java.lang.Class<{%%0}>,boolean)
meth public static <%0 extends java.lang.Object> java.util.Iterator<{%%0}> checkedIteratorByFilter(java.util.Iterator,java.lang.Class<{%%0}>,boolean)
meth public static <%0 extends java.lang.Object> java.util.List<{%%0}> checkedListByCopy(java.util.List,java.lang.Class<{%%0}>,boolean)
meth public static <%0 extends java.lang.Object> java.util.Set<{%%0}> checkedSetByCopy(java.util.Set,java.lang.Class<{%%0}>,boolean)
meth public static <%0 extends java.lang.Object> java.util.Set<{%%0}> checkedSetByFilter(java.util.Set,java.lang.Class<{%%0}>,boolean)
supr java.lang.Object
hfds LOG
hcls CheckedIterator,CheckedMap,CheckedSet

CLSS public final org.openide.util.NbPreferences
innr public abstract interface static Provider
meth public static java.util.prefs.Preferences forModule(java.lang.Class)
meth public static java.util.prefs.Preferences root()
supr java.lang.Object
hfds PREFS_IMPL

CLSS public abstract interface static org.openide.util.NbPreferences$Provider
 outer org.openide.util.NbPreferences
meth public abstract java.util.prefs.Preferences preferencesForModule(java.lang.Class)
meth public abstract java.util.prefs.Preferences preferencesRoot()

CLSS public final org.openide.util.NetworkSettings
cons public init()
innr public abstract static ProxyCredentialsProvider
meth public static <%0 extends java.lang.Object> {%%0} suppressAuthenticationDialog(java.util.concurrent.Callable<{%%0}>) throws java.lang.Exception
meth public static boolean isAuthenticationDialogSuppressed()
meth public static char[] getAuthenticationPassword(java.net.URI)
meth public static java.lang.String getAuthenticationUsername(java.net.URI)
meth public static java.lang.String getKeyForAuthenticationPassword(java.net.URI)
 anno 0 java.lang.Deprecated()
meth public static java.lang.String getProxyHost(java.net.URI)
meth public static java.lang.String getProxyPort(java.net.URI)
supr java.lang.Object
hfds LOGGER,PROXY_AUTHENTICATION_PASSWORD,authenticationDialogSuppressed

CLSS public abstract static org.openide.util.NetworkSettings$ProxyCredentialsProvider
 outer org.openide.util.NetworkSettings
cons public init()
meth protected abstract boolean isProxyAuthentication(java.net.URI)
meth protected abstract char[] getProxyPassword(java.net.URI)
meth protected abstract java.lang.String getProxyHost(java.net.URI)
meth protected abstract java.lang.String getProxyPort(java.net.URI)
meth protected abstract java.lang.String getProxyUserName(java.net.URI)
supr java.lang.Object

CLSS public org.openide.util.NotImplementedException
cons public init()
cons public init(java.lang.String)
supr java.lang.RuntimeException
hfds serialVersionUID

CLSS public final org.openide.util.Pair<%0 extends java.lang.Object, %1 extends java.lang.Object>
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.openide.util.Pair<{%%0},{%%1}> of({%%0},{%%1})
meth public {org.openide.util.Pair%0} first()
meth public {org.openide.util.Pair%1} second()
supr java.lang.Object
hfds first,second

CLSS public org.openide.util.Parameters
meth public static void javaIdentifier(java.lang.CharSequence,java.lang.CharSequence)
meth public static void javaIdentifierOrNull(java.lang.CharSequence,java.lang.CharSequence)
meth public static void notEmpty(java.lang.CharSequence,java.lang.CharSequence)
meth public static void notNull(java.lang.CharSequence,java.lang.Object)
meth public static void notWhitespace(java.lang.CharSequence,java.lang.CharSequence)
supr java.lang.Object

CLSS public org.openide.util.Queue<%0 extends java.lang.Object>
 anno 0 java.lang.Deprecated()
cons public init()
meth public void put({org.openide.util.Queue%0})
meth public {org.openide.util.Queue%0} get()
supr java.lang.Object
hfds queue

CLSS public final org.openide.util.RequestProcessor
cons public init()
cons public init(java.lang.Class<?>)
cons public init(java.lang.String)
cons public init(java.lang.String,int)
cons public init(java.lang.String,int,boolean)
cons public init(java.lang.String,int,boolean,boolean)
innr public final Task
intf java.util.concurrent.ScheduledExecutorService
meth public <%0 extends java.lang.Object> java.util.List<java.util.concurrent.Future<{%%0}>> invokeAll(java.util.Collection<? extends java.util.concurrent.Callable<{%%0}>>) throws java.lang.InterruptedException
meth public <%0 extends java.lang.Object> java.util.List<java.util.concurrent.Future<{%%0}>> invokeAll(java.util.Collection<? extends java.util.concurrent.Callable<{%%0}>>,long,java.util.concurrent.TimeUnit) throws java.lang.InterruptedException
meth public <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> submit(java.lang.Runnable,{%%0})
meth public <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> submit(java.util.concurrent.Callable<{%%0}>)
meth public <%0 extends java.lang.Object> java.util.concurrent.ScheduledFuture<{%%0}> schedule(java.util.concurrent.Callable<{%%0}>,long,java.util.concurrent.TimeUnit)
meth public <%0 extends java.lang.Object> {%%0} invokeAny(java.util.Collection<? extends java.util.concurrent.Callable<{%%0}>>) throws java.lang.InterruptedException,java.util.concurrent.ExecutionException
meth public <%0 extends java.lang.Object> {%%0} invokeAny(java.util.Collection<? extends java.util.concurrent.Callable<{%%0}>>,long,java.util.concurrent.TimeUnit) throws java.lang.InterruptedException,java.util.concurrent.ExecutionException,java.util.concurrent.TimeoutException
meth public boolean awaitTermination(long,java.util.concurrent.TimeUnit) throws java.lang.InterruptedException
meth public boolean isRequestProcessorThread()
meth public boolean isShutdown()
meth public boolean isTerminated()
meth public java.util.List<java.lang.Runnable> shutdownNow()
meth public java.util.concurrent.Future<?> submit(java.lang.Runnable)
meth public java.util.concurrent.ScheduledFuture<?> schedule(java.lang.Runnable,long,java.util.concurrent.TimeUnit)
meth public java.util.concurrent.ScheduledFuture<?> scheduleAtFixedRate(java.lang.Runnable,long,long,java.util.concurrent.TimeUnit)
meth public java.util.concurrent.ScheduledFuture<?> scheduleWithFixedDelay(java.lang.Runnable,long,long,java.util.concurrent.TimeUnit)
meth public org.openide.util.RequestProcessor$Task create(java.lang.Runnable)
meth public org.openide.util.RequestProcessor$Task create(java.lang.Runnable,boolean)
meth public org.openide.util.RequestProcessor$Task post(java.lang.Runnable)
meth public org.openide.util.RequestProcessor$Task post(java.lang.Runnable,int)
meth public org.openide.util.RequestProcessor$Task post(java.lang.Runnable,int,int)
meth public static org.openide.util.RequestProcessor getDefault()
meth public static org.openide.util.RequestProcessor$Task createRequest(java.lang.Runnable)
 anno 0 java.lang.Deprecated()
meth public static org.openide.util.RequestProcessor$Task postRequest(java.lang.Runnable)
 anno 0 java.lang.Deprecated()
meth public static org.openide.util.RequestProcessor$Task postRequest(java.lang.Runnable,int)
 anno 0 java.lang.Deprecated()
meth public static org.openide.util.RequestProcessor$Task postRequest(java.lang.Runnable,int,int)
 anno 0 java.lang.Deprecated()
meth public void execute(java.lang.Runnable)
meth public void shutdown()
meth public void stop()
supr java.lang.Object
hfds DEFAULT,SLOW,TOP_GROUP,UNLIMITED,counter,enableStackTraces,finishAwaitingTasks,inParallel,interruptThread,logger,name,processorLock,processors,queue,stopped,throughput,warnParallel
hcls CreatedItem,FastItem,FixedDelayTask,FixedRateTask,Item,Processor,RPFutureTask,RunnableWrapper,ScheduledRPFutureTask,SlowItem,TaskFutureWrapper,TickTac,TopLevelThreadGroup,WaitableCallable

CLSS public final org.openide.util.RequestProcessor$Task
 outer org.openide.util.RequestProcessor
intf org.openide.util.Cancellable
meth public boolean cancel()
meth public boolean waitFinished(long) throws java.lang.InterruptedException
meth public int getDelay()
meth public int getPriority()
meth public java.lang.String toString()
meth public void run()
meth public void schedule(int)
meth public void setPriority(int)
meth public void waitFinished()
supr org.openide.util.Task
hfds cancelled,item,lastThread,priority,time

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

CLSS public org.openide.util.Task
cons protected init()
cons public init(java.lang.Runnable)
fld public final static org.openide.util.Task EMPTY
intf java.lang.Runnable
meth protected final void notifyFinished()
meth protected final void notifyRunning()
meth public boolean waitFinished(long) throws java.lang.InterruptedException
meth public final boolean isFinished()
meth public java.lang.String toString()
meth public void addTaskListener(org.openide.util.TaskListener)
meth public void removeTaskListener(org.openide.util.TaskListener)
meth public void run()
meth public void waitFinished()
supr java.lang.Object
hfds LOG,RP,finished,list,overrides,run

CLSS public abstract interface org.openide.util.TaskListener
intf java.util.EventListener
meth public abstract void taskFinished(org.openide.util.Task)

CLSS public final org.openide.util.TopologicalSortException
meth public final java.util.List partialSort()
meth public final java.util.Set[] topologicalSets()
meth public final java.util.Set[] unsortableSets()
meth public final void printStackTrace(java.io.PrintStream)
meth public final void printStackTrace(java.io.PrintWriter)
meth public java.lang.String getMessage()
meth public java.lang.String toString()
supr java.lang.Exception
hfds counter,dualGraph,edges,result,vertexes
hcls Vertex

CLSS public abstract interface !annotation org.openide.util.URLStreamHandlerRegistration
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()
meth public abstract java.lang.String[] protocol()

CLSS public abstract org.openide.util.Union2<%0 extends java.lang.Object, %1 extends java.lang.Object>
intf java.io.Serializable
intf java.lang.Cloneable
meth public abstract boolean hasFirst()
meth public abstract boolean hasSecond()
meth public abstract org.openide.util.Union2<{org.openide.util.Union2%0},{org.openide.util.Union2%1}> clone()
meth public abstract {org.openide.util.Union2%0} first()
meth public abstract {org.openide.util.Union2%1} second()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.openide.util.Union2<{%%0},{%%1}> createFirst({%%0})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.openide.util.Union2<{%%0},{%%1}> createSecond({%%1})
supr java.lang.Object
hfds serialVersionUID
hcls Union2First,Union2Second

CLSS public org.openide.util.UserCancelException
cons public init()
cons public init(java.lang.String)
supr java.io.IOException
hfds serialVersionUID

CLSS public abstract org.openide.util.UserQuestionException
cons public init()
cons public init(java.lang.String)
meth public abstract void confirmed() throws java.io.IOException
supr java.io.IOException
hfds serialVersionUID

CLSS public final org.openide.util.Utilities
fld public final static int OS_AIX = 64
fld public final static int OS_DEC = 1024
 anno 0 java.lang.Deprecated()
fld public final static int OS_FREEBSD = 131072
fld public final static int OS_HP = 32
fld public final static int OS_IRIX = 128
fld public final static int OS_LINUX = 16
fld public final static int OS_MAC = 4096
fld public final static int OS_OPENBSD = 1048576
fld public final static int OS_OS2 = 2048
fld public final static int OS_OTHER = 65536
fld public final static int OS_SOLARIS = 8
fld public final static int OS_SUNOS = 256
fld public final static int OS_TRU64 = 512
fld public final static int OS_UNIX_MASK = 1709048
 anno 0 java.lang.Deprecated()
fld public final static int OS_UNIX_OTHER = 524288
fld public final static int OS_VMS = 16384
fld public final static int OS_WIN2000 = 8192
fld public final static int OS_WIN95 = 2
fld public final static int OS_WIN98 = 4
fld public final static int OS_WINDOWS_MASK = 303111
 anno 0 java.lang.Deprecated()
fld public final static int OS_WINNT = 1
fld public final static int OS_WINVISTA = 262144
fld public final static int OS_WIN_OTHER = 32768
fld public final static int TYPICAL_WINDOWS_TASKBAR_HEIGHT = 27
innr public static UnorderableException
meth public static <%0 extends java.lang.Object> java.util.List<{%%0}> topologicalSort(java.util.Collection<? extends {%%0}>,java.util.Map<? super {%%0},? extends java.util.Collection<? extends {%%0}>>) throws org.openide.util.TopologicalSortException
meth public static boolean compareObjects(java.lang.Object,java.lang.Object)
meth public static boolean compareObjectsImpl(java.lang.Object,java.lang.Object,int)
meth public static boolean isJavaIdentifier(java.lang.String)
meth public static boolean isLargeFrameIcons()
 anno 0 java.lang.Deprecated()
meth public static boolean isMac()
meth public static boolean isModalDialogOpen()
meth public static boolean isMouseKeyCode(int)
meth public static boolean isUnix()
meth public static boolean isWindows()
meth public static int arrayHashCode(java.lang.Object[])
 anno 0 java.lang.Deprecated()
meth public static int getOperatingSystem()
meth public static int mouseButtonKeyCode(int)
meth public static int mouseWheelDownKeyCode()
meth public static int mouseWheelUpKeyCode()
meth public static int showJFileChooser(javax.swing.JFileChooser,java.awt.Component,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static java.awt.Component findDialogParent()
meth public static java.awt.Component findDialogParent(java.awt.Component)
meth public static java.awt.Component getFocusTraversableComponent(java.awt.Component)
meth public static java.awt.Cursor createCustomCursor(java.awt.Component,java.awt.Image,java.lang.String)
meth public static java.awt.Cursor createProgressCursor(java.awt.Component)
meth public static java.awt.Dimension getScreenSize()
 anno 0 java.lang.Deprecated()
meth public static java.awt.Image icon2Image(javax.swing.Icon)
 anno 0 java.lang.Deprecated()
meth public static java.awt.Image loadImage(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static java.awt.Image loadImage(java.lang.String,boolean)
 anno 0 java.lang.Deprecated()
meth public static java.awt.Image mergeImages(java.awt.Image,java.awt.Image,int,int)
 anno 0 java.lang.Deprecated()
meth public static java.awt.Rectangle findCenterBounds(java.awt.Dimension)
meth public static java.awt.Rectangle getUsableScreenBounds()
meth public static java.awt.Rectangle getUsableScreenBounds(java.awt.GraphicsConfiguration)
meth public static java.beans.BeanInfo getBeanInfo(java.lang.Class<?>) throws java.beans.IntrospectionException
meth public static java.beans.BeanInfo getBeanInfo(java.lang.Class<?>,java.lang.Class<?>) throws java.beans.IntrospectionException
meth public static java.io.File toFile(java.net.URI)
meth public static java.io.File toFile(java.net.URL)
 anno 0 java.lang.Deprecated()
meth public static java.lang.Class<?> getObjectType(java.lang.Class<?>)
meth public static java.lang.Class<?> getPrimitiveType(java.lang.Class<?>)
meth public static java.lang.Object toPrimitiveArray(java.lang.Object[])
meth public static java.lang.Object[] toObjectArray(java.lang.Object)
meth public static java.lang.String escapeParameters(java.lang.String[])
meth public static java.lang.String getClassName(java.lang.Class<?>)
meth public static java.lang.String getShortClassName(java.lang.Class<?>)
meth public static java.lang.String keyToString(javax.swing.KeyStroke)
meth public static java.lang.String keyToString(javax.swing.KeyStroke,boolean)
meth public static java.lang.String pureClassName(java.lang.String)
meth public static java.lang.String replaceString(java.lang.String,java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static java.lang.String translate(java.lang.String)
meth public static java.lang.String wrapString(java.lang.String,int,boolean,boolean)
 anno 0 java.lang.Deprecated()
meth public static java.lang.String wrapString(java.lang.String,int,java.text.BreakIterator,boolean)
meth public static java.lang.String[] parseParameters(java.lang.String)
meth public static java.lang.String[] wrapStringToArray(java.lang.String,int,boolean,boolean)
 anno 0 java.lang.Deprecated()
meth public static java.lang.String[] wrapStringToArray(java.lang.String,int,java.text.BreakIterator,boolean)
meth public static java.lang.ref.ReferenceQueue<java.lang.Object> activeReferenceQueue()
meth public static java.net.URI toURI(java.io.File)
meth public static java.net.URL toURL(java.io.File) throws java.net.MalformedURLException
 anno 0 java.lang.Deprecated()
meth public static java.util.List partialSort(java.util.List,java.util.Comparator,boolean)
 anno 0 java.lang.Deprecated()
meth public static java.util.List<? extends javax.swing.Action> actionsForPath(java.lang.String)
meth public static java.util.List<? extends javax.swing.Action> actionsForPath(java.lang.String,org.openide.util.Lookup)
meth public static javax.swing.JPopupMenu actionsToPopup(javax.swing.Action[],java.awt.Component)
meth public static javax.swing.JPopupMenu actionsToPopup(javax.swing.Action[],org.openide.util.Lookup)
meth public static javax.swing.KeyStroke stringToKey(java.lang.String)
meth public static javax.swing.KeyStroke[] stringToKeys(java.lang.String)
meth public static org.openide.util.Lookup actionsGlobalContext()
meth public static void attachInitJob(java.awt.Component,org.openide.util.AsyncGUIJob)
meth public static void disabledActionBeep()
supr java.lang.Object
hfds ALT_WILDCARD_MASK,CTRL_WILDCARD_MASK,LOG,TYPICAL_MACOSX_MENU_HEIGHT,clearIntrospector,doClear,global,namesAndValues,screenBoundsCache
hcls NamesAndValues

CLSS public static org.openide.util.Utilities$UnorderableException
 outer org.openide.util.Utilities
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,java.util.Collection,java.util.Map)
cons public init(java.util.Collection,java.util.Map)
meth public java.util.Collection getUnorderable()
meth public java.util.Map getDeps()
supr java.lang.RuntimeException
hfds deps,serialVersionUID,unorderable

CLSS public abstract org.openide.util.VectorIcon
cons protected init(int,int)
intf java.io.Serializable
intf javax.swing.Icon
meth protected abstract void paintIcon(java.awt.Component,java.awt.Graphics2D,int,int,double)
meth protected final static int round(double)
meth protected final static void setAntiAliasing(java.awt.Graphics2D,boolean)
meth public final int getIconHeight()
meth public final int getIconWidth()
meth public final void paintIcon(java.awt.Component,java.awt.Graphics,int,int)
supr java.lang.Object
hfds height,width

CLSS public abstract org.openide.util.WeakListener
 anno 0 java.lang.Deprecated()
cons protected init(java.lang.Class,java.util.EventListener)
innr public final static Change
innr public final static Document
innr public final static Focus
innr public final static Node
innr public static FileChange
innr public static FileStatus
innr public static PropertyChange
innr public static Repository
innr public static VetoableChange
intf java.util.EventListener
meth protected abstract java.lang.String removeMethodName()
meth protected final java.util.EventListener get(java.util.EventObject)
meth protected final void setSource(java.lang.Object)
meth public java.lang.String toString()
meth public static java.awt.event.FocusListener focus(java.awt.event.FocusListener,java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public static java.beans.PropertyChangeListener propertyChange(java.beans.PropertyChangeListener,java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public static java.beans.VetoableChangeListener vetoableChange(java.beans.VetoableChangeListener,java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public static java.util.EventListener create(java.lang.Class,java.util.EventListener,java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public static javax.swing.event.ChangeListener change(javax.swing.event.ChangeListener,java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public static javax.swing.event.DocumentListener document(javax.swing.event.DocumentListener,java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public static org.openide.filesystems.FileChangeListener fileChange(org.openide.filesystems.FileChangeListener,java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public static org.openide.filesystems.FileStatusListener fileStatus(org.openide.filesystems.FileStatusListener,java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public static org.openide.filesystems.RepositoryListener repository(org.openide.filesystems.RepositoryListener,java.lang.Object)
meth public static org.openide.nodes.NodeListener node(org.openide.nodes.NodeListener,java.lang.Object)
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds listenerClass,ref,source
hcls ListenerReference,ProxyListener

CLSS public final static org.openide.util.WeakListener$Change
 outer org.openide.util.WeakListener
 anno 0 java.lang.Deprecated()
cons public init(javax.swing.event.ChangeListener)
intf javax.swing.event.ChangeListener
meth protected java.lang.String removeMethodName()
meth public void stateChanged(javax.swing.event.ChangeEvent)
supr org.openide.util.WeakListener

CLSS public final static org.openide.util.WeakListener$Document
 outer org.openide.util.WeakListener
 anno 0 java.lang.Deprecated()
cons public init(javax.swing.event.DocumentListener)
intf javax.swing.event.DocumentListener
meth protected java.lang.String removeMethodName()
meth public void changedUpdate(javax.swing.event.DocumentEvent)
meth public void insertUpdate(javax.swing.event.DocumentEvent)
meth public void removeUpdate(javax.swing.event.DocumentEvent)
supr org.openide.util.WeakListener

CLSS public static org.openide.util.WeakListener$FileChange
 outer org.openide.util.WeakListener
 anno 0 java.lang.Deprecated()
cons public init(org.openide.filesystems.FileChangeListener)
intf org.openide.filesystems.FileChangeListener
meth protected java.lang.String removeMethodName()
meth public void fileAttributeChanged(org.openide.filesystems.FileAttributeEvent)
meth public void fileChanged(org.openide.filesystems.FileEvent)
meth public void fileDataCreated(org.openide.filesystems.FileEvent)
meth public void fileDeleted(org.openide.filesystems.FileEvent)
meth public void fileFolderCreated(org.openide.filesystems.FileEvent)
meth public void fileRenamed(org.openide.filesystems.FileRenameEvent)
supr org.openide.util.WeakListener

CLSS public static org.openide.util.WeakListener$FileStatus
 outer org.openide.util.WeakListener
 anno 0 java.lang.Deprecated()
cons public init(org.openide.filesystems.FileStatusListener)
intf org.openide.filesystems.FileStatusListener
meth protected java.lang.String removeMethodName()
meth public void annotationChanged(org.openide.filesystems.FileStatusEvent)
supr org.openide.util.WeakListener

CLSS public final static org.openide.util.WeakListener$Focus
 outer org.openide.util.WeakListener
 anno 0 java.lang.Deprecated()
cons public init(java.awt.event.FocusListener)
intf java.awt.event.FocusListener
meth protected java.lang.String removeMethodName()
meth public void focusGained(java.awt.event.FocusEvent)
meth public void focusLost(java.awt.event.FocusEvent)
supr org.openide.util.WeakListener

CLSS public final static org.openide.util.WeakListener$Node
 outer org.openide.util.WeakListener
 anno 0 java.lang.Deprecated()
cons public init(org.openide.nodes.NodeListener)
intf org.openide.nodes.NodeListener
meth protected java.lang.String removeMethodName()
meth public void childrenAdded(org.openide.nodes.NodeMemberEvent)
meth public void childrenRemoved(org.openide.nodes.NodeMemberEvent)
meth public void childrenReordered(org.openide.nodes.NodeReorderEvent)
meth public void nodeDestroyed(org.openide.nodes.NodeEvent)
supr org.openide.util.WeakListener$PropertyChange

CLSS public static org.openide.util.WeakListener$PropertyChange
 outer org.openide.util.WeakListener
 anno 0 java.lang.Deprecated()
cons public init(java.beans.PropertyChangeListener)
intf java.beans.PropertyChangeListener
meth protected java.lang.String removeMethodName()
meth public void propertyChange(java.beans.PropertyChangeEvent)
supr org.openide.util.WeakListener

CLSS public static org.openide.util.WeakListener$Repository
 outer org.openide.util.WeakListener
 anno 0 java.lang.Deprecated()
cons public init(org.openide.filesystems.RepositoryListener)
intf org.openide.filesystems.RepositoryListener
meth protected java.lang.String removeMethodName()
meth public void fileSystemAdded(org.openide.filesystems.RepositoryEvent)
meth public void fileSystemPoolReordered(org.openide.filesystems.RepositoryReorderedEvent)
meth public void fileSystemRemoved(org.openide.filesystems.RepositoryEvent)
supr org.openide.util.WeakListener

CLSS public static org.openide.util.WeakListener$VetoableChange
 outer org.openide.util.WeakListener
 anno 0 java.lang.Deprecated()
cons public init(java.beans.VetoableChangeListener)
intf java.beans.VetoableChangeListener
meth protected java.lang.String removeMethodName()
meth public void vetoableChange(java.beans.PropertyChangeEvent) throws java.beans.PropertyVetoException
supr org.openide.util.WeakListener

CLSS public final org.openide.util.WeakListeners
meth public static <%0 extends java.util.EventListener> {%%0} create(java.lang.Class<{%%0}>,java.lang.Class<? super {%%0}>,{%%0},java.lang.Object)
meth public static <%0 extends java.util.EventListener> {%%0} create(java.lang.Class<{%%0}>,{%%0},java.lang.Object)
meth public static java.beans.PropertyChangeListener propertyChange(java.beans.PropertyChangeListener,java.lang.Object)
meth public static java.beans.PropertyChangeListener propertyChange(java.beans.PropertyChangeListener,java.lang.String,java.lang.Object)
meth public static java.beans.VetoableChangeListener vetoableChange(java.beans.VetoableChangeListener,java.lang.Object)
meth public static java.beans.VetoableChangeListener vetoableChange(java.beans.VetoableChangeListener,java.lang.String,java.lang.Object)
meth public static javax.swing.event.ChangeListener change(javax.swing.event.ChangeListener,java.lang.Object)
meth public static javax.swing.event.DocumentListener document(javax.swing.event.DocumentListener,java.lang.Object)
supr java.lang.Object

CLSS public org.openide.util.WeakSet<%0 extends java.lang.Object>
cons public init()
cons public init(int)
cons public init(int,float)
cons public init(java.util.Collection<? extends {org.openide.util.WeakSet%0}>)
intf java.io.Serializable
intf java.lang.Cloneable
meth public <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public boolean add({org.openide.util.WeakSet%0})
meth public boolean contains(java.lang.Object)
meth public boolean containsAll(java.util.Collection<?>)
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public boolean remove(java.lang.Object)
meth public boolean removeAll(java.util.Collection<?>)
meth public boolean retainAll(java.util.Collection<?>)
meth public int hashCode()
meth public int size()
meth public java.lang.Object clone()
meth public java.lang.Object[] toArray()
meth public java.lang.String toString()
meth public java.util.Iterator<{org.openide.util.WeakSet%0}> iterator()
meth public void clear()
meth public void resize(int)
meth public {org.openide.util.WeakSet%0} putIfAbsent({org.openide.util.WeakSet%0})
supr java.util.AbstractSet<{org.openide.util.WeakSet%0}>
hfds PRESENT,loadFactor,m,s,serialVersionUID
hcls SharedKeyWeakHashMap

CLSS public abstract interface !annotation org.openide.util.lookup.NamedServiceDefinition
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String position()
meth public abstract java.lang.Class<?>[] serviceType()
meth public abstract java.lang.String path()

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

