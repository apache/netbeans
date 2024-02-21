#Signature file v4.1
#Version 3.74

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

CLSS public abstract interface java.awt.event.KeyListener
intf java.util.EventListener
meth public abstract void keyPressed(java.awt.event.KeyEvent)
meth public abstract void keyReleased(java.awt.event.KeyEvent)
meth public abstract void keyTyped(java.awt.event.KeyEvent)

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

CLSS public abstract interface java.io.Externalizable
intf java.io.Serializable
meth public abstract void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public abstract void writeExternal(java.io.ObjectOutput) throws java.io.IOException

CLSS public abstract interface java.io.Serializable

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

CLSS public java.lang.IllegalArgumentException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.RuntimeException

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

CLSS public abstract java.util.logging.Handler
cons protected init()
meth protected void reportError(java.lang.String,java.lang.Exception,int)
meth public abstract void close()
meth public abstract void flush()
meth public abstract void publish(java.util.logging.LogRecord)
meth public boolean isLoggable(java.util.logging.LogRecord)
meth public java.lang.String getEncoding()
meth public java.util.logging.ErrorManager getErrorManager()
meth public java.util.logging.Filter getFilter()
meth public java.util.logging.Formatter getFormatter()
meth public java.util.logging.Level getLevel()
meth public void setEncoding(java.lang.String) throws java.io.UnsupportedEncodingException
meth public void setErrorManager(java.util.logging.ErrorManager)
meth public void setFilter(java.util.logging.Filter)
meth public void setFormatter(java.util.logging.Formatter)
meth public void setLevel(java.util.logging.Level)
supr java.lang.Object

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

CLSS public abstract interface javax.swing.event.DocumentListener
intf java.util.EventListener
meth public abstract void changedUpdate(javax.swing.event.DocumentEvent)
meth public abstract void insertUpdate(javax.swing.event.DocumentEvent)
meth public abstract void removeUpdate(javax.swing.event.DocumentEvent)

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

CLSS public abstract org.netbeans.CLIHandler
cons protected init(int)
fld public final static int WHEN_BOOT = 1
fld public final static int WHEN_EXTRA = 3
fld public final static int WHEN_INIT = 2
innr public final static Args
meth protected abstract int cli(org.netbeans.CLIHandler$Args)
meth protected abstract void usage(java.io.PrintWriter)
meth protected static int notifyHandlers(org.netbeans.CLIHandler$Args,java.util.Collection<? extends org.netbeans.CLIHandler>,int,boolean,boolean)
meth protected static void showHelp(java.io.PrintWriter,java.util.Collection<? extends org.netbeans.CLIHandler>,int)
meth public static void stopServer()
supr java.lang.Object
hfds KEY_LENGTH,OUTPUT,REPLY_AVAILABLE,REPLY_DELAY,REPLY_ERROR,REPLY_EXIT,REPLY_FAIL,REPLY_OK,REPLY_READ,REPLY_VERSION,REPLY_WRITE,VERSION,doLater,secureCLIPort,server,when
hcls Execute,FileAndLock,Server,Status

CLSS public org.netbeans.beaninfo.editors.BoolEditor
cons public init()
meth protected void attachEnvImpl(org.openide.explorer.propertysheet.PropertyEnv)
meth protected void validateEnv(org.openide.explorer.propertysheet.PropertyEnv)
meth public java.lang.String getAsText()
meth public java.lang.String getJavaInitializationString()
meth public java.lang.String[] getTags()
meth public void setAsText(java.lang.String)
supr org.netbeans.beaninfo.editors.ExPropertyEditorSupport
hfds stringValues

CLSS public org.netbeans.beaninfo.editors.BooleanEditor
cons public init()
meth public java.lang.String getJavaInitializationString()
supr org.netbeans.beaninfo.editors.WrappersEditor

CLSS public org.netbeans.beaninfo.editors.ByteEditor
cons public init()
meth public java.lang.String getJavaInitializationString()
supr org.netbeans.beaninfo.editors.WrappersEditor

CLSS public org.netbeans.beaninfo.editors.CharEditor
cons public init()
intf org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor
meth public boolean hasInPlaceCustomEditor()
meth public boolean supportsEditingTaggedValues()
meth public java.awt.Component getInPlaceCustomEditor()
meth public java.lang.String getAsText()
meth public java.lang.String getJavaInitializationString()
meth public void setAsText(java.lang.String)
meth public void setValue(java.lang.Object)
supr java.beans.PropertyEditorSupport

CLSS public org.netbeans.beaninfo.editors.CharacterEditor
cons public init()
meth public java.lang.String getJavaInitializationString()
supr org.netbeans.beaninfo.editors.WrappersEditor

CLSS public org.netbeans.beaninfo.editors.ClassEditor
cons public init()
meth public java.lang.String getAsText()
meth public java.lang.String getJavaInitializationString()
meth public void setAsText(java.lang.String)
supr java.beans.PropertyEditorSupport

CLSS public final org.netbeans.beaninfo.editors.ColorEditor
cons public init()
fld public final static int AWT_PALETTE = 1
fld public final static int SWING_PALETTE = 3
fld public final static int SYSTEM_PALETTE = 2
fld public final static java.lang.String ATTR_BLUE = "blue"
fld public final static java.lang.String ATTR_GREEN = "green"
fld public final static java.lang.String ATTR_ID = "id"
fld public final static java.lang.String ATTR_PALETTE = "palette"
fld public final static java.lang.String ATTR_RED = "red"
fld public final static java.lang.String ATTR_TYPE = "type"
fld public final static java.lang.String VALUE_NULL = "null"
fld public final static java.lang.String VALUE_PALETTE = "palette"
fld public final static java.lang.String VALUE_RGB = "rgb"
fld public final static java.lang.String XML_COLOR = "Color"
intf java.beans.PropertyEditor
intf org.openide.explorer.propertysheet.editors.XMLPropertyEditor
meth public boolean isPaintable()
meth public boolean supportsCustomEditor()
meth public final static boolean gtkShouldAntialias()
 anno 0 java.lang.Deprecated()
meth public java.awt.Component getCustomEditor()
meth public java.lang.Object getValue()
meth public java.lang.String getAsText()
meth public java.lang.String getJavaInitializationString()
meth public java.lang.String[] getTags()
meth public org.w3c.dom.Node storeToXML(org.w3c.dom.Document)
meth public static javax.swing.JColorChooser getStaticChooser(org.netbeans.beaninfo.editors.ColorEditor)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void paintValue(java.awt.Graphics,java.awt.Rectangle)
meth public void readFromXML(org.w3c.dom.Node) throws java.io.IOException
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setAsText(java.lang.String)
meth public void setValue(java.lang.Object)
supr java.lang.Object
hfds GTK,awtColorNames,awtColors,awtGenerate,gtkAA,superColor,support,swingColorNames,swingColors,systemColorNames,systemColors,systemGenerate
hcls NbColorChooser,NbColorChooserPanel,SuperColor,SuperColorSelectionModel

CLSS public org.netbeans.beaninfo.editors.DateEditor
cons public init()
meth public java.lang.String getAsText()
meth public java.lang.String getJavaInitializationString()
meth public void setAsText(java.lang.String)
supr java.beans.PropertyEditorSupport
hfds fmt

CLSS public org.netbeans.beaninfo.editors.DimensionCustomEditor
cons public init(org.netbeans.beaninfo.editors.DimensionEditor)
intf java.awt.event.KeyListener
intf java.beans.PropertyChangeListener
meth protected boolean validFor(javax.swing.JTextField)
meth protected java.util.HashMap<javax.swing.JTextField,javax.swing.JLabel> getMap()
meth protected void handleInvalid(javax.swing.JTextField)
meth protected void handleValid(javax.swing.JTextField)
meth protected void setPanel(javax.swing.JPanel)
meth protected void updateValues()
meth public java.awt.Dimension getPreferredSize()
meth public void keyPressed(java.awt.event.KeyEvent)
meth public void keyReleased(java.awt.event.KeyEvent)
meth public void keyTyped(java.awt.event.KeyEvent)
meth public void propertyChange(java.beans.PropertyChangeEvent)
supr javax.swing.JPanel
hfds editor,heightField,heightLabel,insidePanel,jLabel1,serialVersionUID,widthField,widthLabel

CLSS public org.netbeans.beaninfo.editors.DimensionEditor
cons public init()
fld public final static java.lang.String ATTR_VALUE = "value"
intf org.openide.explorer.propertysheet.ExPropertyEditor
intf org.openide.explorer.propertysheet.editors.XMLPropertyEditor
meth protected java.lang.String getXMLValueTag()
meth public boolean supportsCustomEditor()
meth public java.awt.Component getCustomEditor()
meth public java.lang.String getAsText()
meth public java.lang.String getJavaInitializationString()
meth public org.w3c.dom.Node storeToXML(org.w3c.dom.Document)
meth public void attachEnv(org.openide.explorer.propertysheet.PropertyEnv)
meth public void readFromXML(org.w3c.dom.Node) throws java.io.IOException
meth public void setAsText(java.lang.String)
supr java.beans.PropertyEditorSupport

CLSS public org.netbeans.beaninfo.editors.DoubleEditor
cons public init()
meth public java.lang.String getJavaInitializationString()
supr org.netbeans.beaninfo.editors.WrappersEditor

CLSS public abstract org.netbeans.beaninfo.editors.ExPropertyEditorSupport
cons protected init()
innr public static EnvException
intf org.openide.explorer.propertysheet.ExPropertyEditor
meth protected abstract void attachEnvImpl(org.openide.explorer.propertysheet.PropertyEnv)
meth protected abstract void validateEnv(org.openide.explorer.propertysheet.PropertyEnv)
meth protected final static java.lang.String arrToStr(java.lang.Object[])
meth public final void attachEnv(org.openide.explorer.propertysheet.PropertyEnv)
supr java.beans.PropertyEditorSupport

CLSS public static org.netbeans.beaninfo.editors.ExPropertyEditorSupport$EnvException
 outer org.netbeans.beaninfo.editors.ExPropertyEditorSupport
cons public init(java.lang.String)
supr java.lang.IllegalArgumentException

CLSS public org.netbeans.beaninfo.editors.FileArrayEditor
cons public init()
intf java.beans.PropertyChangeListener
intf org.openide.explorer.propertysheet.ExPropertyEditor
meth public boolean supportsCustomEditor()
meth public java.awt.Component getCustomEditor()
meth public java.lang.String getAsText()
meth public java.lang.String getJavaInitializationString()
meth public void attachEnv(org.openide.explorer.propertysheet.PropertyEnv)
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void setAsText(java.lang.String)
supr java.beans.PropertyEditorSupport
hfds ANCESTOR,baseDirectory,chooser,currentDirectory,directories,editable,fileFilter,fileHiding,files,mode,myPropertyFired

CLSS public org.netbeans.beaninfo.editors.FileEditor
cons public init()
intf java.beans.PropertyChangeListener
intf org.openide.explorer.propertysheet.ExPropertyEditor
meth public boolean supportsCustomEditor()
meth public java.awt.Component getCustomEditor()
meth public java.lang.String getAsText()
meth public java.lang.String getJavaInitializationString()
meth public static javax.swing.JFileChooser createHackedFileChooser()
meth public static void hackFileChooser(javax.swing.JFileChooser)
meth public void attachEnv(org.openide.explorer.propertysheet.PropertyEnv)
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void setAsText(java.lang.String)
supr java.beans.PropertyEditorSupport
hfds PROPERTY_BASE_DIR,PROPERTY_CURRENT_DIR,PROPERTY_FILE_HIDING,PROPERTY_FILTER,PROPERTY_SHOW_DIRECTORIES,PROPERTY_SHOW_FILES,baseDirectory,chooser,currentDirectory,directories,editable,env,fileFilter,fileHiding,files,lastCurrentDir,mode
hcls ButtonHider,DelegatingFileFilter,DelegatingFilenameFilter

CLSS public org.netbeans.beaninfo.editors.FloatEditor
cons public init()
meth public java.lang.String getJavaInitializationString()
supr org.netbeans.beaninfo.editors.WrappersEditor

CLSS public org.netbeans.beaninfo.editors.FontEditor
cons public init()
fld public final static java.lang.String ATTR_NAME = "name"
fld public final static java.lang.String ATTR_SIZE = "size"
fld public final static java.lang.String ATTR_STYLE = "style"
fld public final static java.lang.String XML_FONT = "Font"
intf java.beans.PropertyEditor
intf org.openide.explorer.propertysheet.editors.XMLPropertyEditor
meth public boolean isPaintable()
meth public boolean supportsCustomEditor()
meth public java.awt.Component getCustomEditor()
meth public java.lang.Object getValue()
meth public java.lang.String getAsText()
meth public java.lang.String getJavaInitializationString()
meth public java.lang.String[] getTags()
meth public org.w3c.dom.Node storeToXML(org.w3c.dom.Document)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void paintValue(java.awt.Graphics,java.awt.Rectangle)
meth public void readFromXML(org.w3c.dom.Node) throws java.io.IOException
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setAsText(java.lang.String)
meth public void setValue(java.lang.Object)
supr java.lang.Object
hfds font,fontName,fonts,sizes,styles,support
hcls FontPanel

CLSS public org.netbeans.beaninfo.editors.HtmlBrowser
cons public init()
innr public static FactoryEditor
supr java.lang.Object

CLSS public static org.netbeans.beaninfo.editors.HtmlBrowser$FactoryEditor
 outer org.netbeans.beaninfo.editors.HtmlBrowser
cons public init()
meth public java.lang.String getAsText()
meth public java.lang.String[] getTags()
meth public void setAsText(java.lang.String)
supr java.beans.PropertyEditorSupport
hfds BROWSER_FOLDER,EA_HIDDEN

CLSS public org.netbeans.beaninfo.editors.InsetsCustomEditor
cons public init(org.netbeans.beaninfo.editors.InsetsEditor,org.openide.explorer.propertysheet.PropertyEnv)
intf java.awt.event.KeyListener
intf java.beans.PropertyChangeListener
meth protected boolean validFor(javax.swing.JTextField)
meth protected java.util.HashMap<javax.swing.JTextField,javax.swing.JLabel> getMap()
meth protected void handleInvalid(javax.swing.JTextField)
meth protected void handleValid(javax.swing.JTextField)
meth protected void setPanel(javax.swing.JPanel)
meth protected void updateValues()
meth public java.awt.Dimension getPreferredSize()
meth public void keyPressed(java.awt.event.KeyEvent)
meth public void keyReleased(java.awt.event.KeyEvent)
meth public void keyTyped(java.awt.event.KeyEvent)
meth public void propertyChange(java.beans.PropertyChangeEvent)
supr javax.swing.JPanel
hfds editor,heightField,heightLabel,jPanel2,serialVersionUID,widthField,widthLabel,xField,xLabel,yField,yLabel

CLSS public org.netbeans.beaninfo.editors.InsetsEditor
cons public init()
fld public final static java.lang.String ATTR_VALUE = "value"
intf org.openide.explorer.propertysheet.ExPropertyEditor
intf org.openide.explorer.propertysheet.editors.XMLPropertyEditor
meth protected java.lang.String getXMLValueTag()
meth public boolean supportsCustomEditor()
meth public java.awt.Component getCustomEditor()
meth public java.lang.String getAsText()
meth public java.lang.String getJavaInitializationString()
meth public org.w3c.dom.Node storeToXML(org.w3c.dom.Document)
meth public void attachEnv(org.openide.explorer.propertysheet.PropertyEnv)
meth public void readFromXML(org.w3c.dom.Node) throws java.io.IOException
meth public void setAsText(java.lang.String)
supr java.beans.PropertyEditorSupport
hfds env

CLSS public org.netbeans.beaninfo.editors.IntEditor
cons public init()
fld public final static java.lang.String CODE_VALS = "codeValues"
fld public final static java.lang.String KEYS = "stringKeys"
fld public final static java.lang.String VALS = "intValues"
meth protected void attachEnvImpl(org.openide.explorer.propertysheet.PropertyEnv)
meth protected void validateEnv(org.openide.explorer.propertysheet.PropertyEnv)
meth public java.lang.String getAsText()
meth public java.lang.String getJavaInitializationString()
meth public java.lang.String[] getTags()
meth public void setAsText(java.lang.String)
meth public void setValue(java.lang.Object)
supr org.netbeans.beaninfo.editors.ExPropertyEditorSupport
hfds code,keys,values

CLSS public org.netbeans.beaninfo.editors.IntegerEditor
cons public init()
meth public java.lang.String getJavaInitializationString()
meth public void setAsText(java.lang.String)
supr org.netbeans.beaninfo.editors.WrappersEditor

CLSS public org.netbeans.beaninfo.editors.ListImageEditor
cons public init()
fld public final static java.lang.String PROP_DESCRIPTIONS = "descriptions"
fld public final static java.lang.String PROP_IMAGES = "images"
fld public final static java.lang.String PROP_VALUES = "values"
intf org.openide.explorer.propertysheet.ExPropertyEditor
meth public boolean isEditable()
meth public boolean isPaintable()
meth public java.lang.String getAsText()
meth public java.lang.String getJavaInitializationString()
meth public java.lang.String[] getTags()
meth public void attachEnv(org.openide.explorer.propertysheet.PropertyEnv)
meth public void paintValue(java.awt.Graphics,java.awt.Rectangle)
meth public void setAsText(java.lang.String)
supr java.beans.PropertyEditorSupport
hfds canWrite,descriptions,images,values

CLSS public org.netbeans.beaninfo.editors.LongEditor
cons public init()
meth public java.lang.String getJavaInitializationString()
supr org.netbeans.beaninfo.editors.WrappersEditor

CLSS public final org.netbeans.beaninfo.editors.ObjectEditor
cons public init()
intf org.openide.explorer.propertysheet.ExPropertyEditor
meth protected org.openide.util.Lookup lookup()
meth protected org.openide.util.Lookup$Template<java.lang.Object> template()
meth public boolean supportsCustomEditor()
meth public java.awt.Component getCustomEditor()
meth public java.lang.String getAsText()
meth public java.lang.String[] getTags()
meth public void attachEnv(org.openide.explorer.propertysheet.PropertyEnv)
meth public void setAsText(java.lang.String)
supr java.beans.PropertyEditorSupport
hfds PROP_LOOKUP,PROP_NULL,PROP_SUPERCLASS,customEditor,lookup,nullValue,template
hcls ItemRadioButton,ObjectPanel

CLSS public org.netbeans.beaninfo.editors.PointCustomEditor
cons public init(org.netbeans.beaninfo.editors.DimensionEditor,org.openide.explorer.propertysheet.PropertyEnv)
cons public init(org.netbeans.beaninfo.editors.PointEditor,org.openide.explorer.propertysheet.PropertyEnv)
intf java.awt.event.KeyListener
intf java.beans.PropertyChangeListener
meth protected boolean validFor(javax.swing.JTextField)
meth protected java.util.HashMap<javax.swing.JTextField,javax.swing.JLabel> getMap()
meth protected void handleInvalid(javax.swing.JTextField)
meth protected void handleValid(javax.swing.JTextField)
meth protected void setPanel(javax.swing.JPanel)
meth protected void updateValues()
meth public java.awt.Dimension getPreferredSize()
meth public void keyPressed(java.awt.event.KeyEvent)
meth public void keyReleased(java.awt.event.KeyEvent)
meth public void keyTyped(java.awt.event.KeyEvent)
meth public void propertyChange(java.beans.PropertyChangeEvent)
supr javax.swing.JPanel
hfds dimensionMode,editor,env,insidePanel,serialVersionUID,xField,xLabel,yField,yLabel

CLSS public org.netbeans.beaninfo.editors.PointEditor
cons public init()
fld public final static java.lang.String ATTR_VALUE = "value"
intf org.openide.explorer.propertysheet.ExPropertyEditor
intf org.openide.explorer.propertysheet.editors.XMLPropertyEditor
meth protected java.lang.String getXMLValueTag()
meth public boolean supportsCustomEditor()
meth public java.awt.Component getCustomEditor()
meth public java.lang.String getAsText()
meth public java.lang.String getJavaInitializationString()
meth public org.w3c.dom.Node storeToXML(org.w3c.dom.Document)
meth public void attachEnv(org.openide.explorer.propertysheet.PropertyEnv)
meth public void readFromXML(org.w3c.dom.Node) throws java.io.IOException
meth public void setAsText(java.lang.String)
supr java.beans.PropertyEditorSupport

CLSS public org.netbeans.beaninfo.editors.PropertiesCustomEditor
cons public init(org.netbeans.beaninfo.editors.PropertiesEditor)
intf javax.swing.event.DocumentListener
meth public java.awt.Dimension getPreferredSize()
meth public void changedUpdate(javax.swing.event.DocumentEvent)
meth public void insertUpdate(javax.swing.event.DocumentEvent)
meth public void removeUpdate(javax.swing.event.DocumentEvent)
supr javax.swing.JPanel
hfds editor,editorPane,warnings

CLSS public org.netbeans.beaninfo.editors.PropertiesEditor
cons public init()
meth public boolean supportsCustomEditor()
meth public java.awt.Component getCustomEditor()
meth public java.lang.String getAsText()
meth public java.lang.String getJavaInitializationString()
meth public void setAsText(java.lang.String)
supr java.beans.PropertyEditorSupport

CLSS public org.netbeans.beaninfo.editors.RectangleCustomEditor
cons public init(org.netbeans.beaninfo.editors.RectangleEditor,org.openide.explorer.propertysheet.PropertyEnv)
intf java.awt.event.KeyListener
intf java.beans.PropertyChangeListener
meth protected boolean validFor(javax.swing.JTextField)
meth protected java.util.HashMap<javax.swing.JTextField,javax.swing.JLabel> getMap()
meth protected void handleInvalid(javax.swing.JTextField)
meth protected void handleValid(javax.swing.JTextField)
meth protected void setPanel(javax.swing.JPanel)
meth protected void updateValues()
meth public java.awt.Dimension getPreferredSize()
meth public void keyPressed(java.awt.event.KeyEvent)
meth public void keyReleased(java.awt.event.KeyEvent)
meth public void keyTyped(java.awt.event.KeyEvent)
meth public void propertyChange(java.beans.PropertyChangeEvent)
supr javax.swing.JPanel
hfds editor,heightField,heightLabel,jPanel2,serialVersionUID,widthField,widthLabel,xField,xLabel,yField,yLabel

CLSS public org.netbeans.beaninfo.editors.RectangleEditor
cons public init()
fld public final static java.lang.String ATTR_VALUE = "value"
intf org.openide.explorer.propertysheet.ExPropertyEditor
intf org.openide.explorer.propertysheet.editors.XMLPropertyEditor
meth protected java.lang.String getXMLValueTag()
meth public boolean supportsCustomEditor()
meth public java.awt.Component getCustomEditor()
meth public java.lang.String getAsText()
meth public java.lang.String getJavaInitializationString()
meth public org.w3c.dom.Node storeToXML(org.w3c.dom.Document)
meth public void attachEnv(org.openide.explorer.propertysheet.PropertyEnv)
meth public void readFromXML(org.w3c.dom.Node) throws java.io.IOException
meth public void setAsText(java.lang.String)
supr java.beans.PropertyEditorSupport
hfds env

CLSS public org.netbeans.beaninfo.editors.ShortEditor
cons public init()
meth public java.lang.String getJavaInitializationString()
supr org.netbeans.beaninfo.editors.WrappersEditor

CLSS public org.netbeans.beaninfo.editors.StringArrayCustomEditor
cons public init(org.netbeans.beaninfo.editors.StringArrayCustomizable)
meth public void setEnabled(boolean)
supr javax.swing.JPanel
hfds addButton,buttonsPanel,changeButton,editPanel,editor,itemField,itemLabel,itemList,itemListLabel,itemListScroll,itemsVector,jSeparator1,moveDownButton,moveUpButton,paddingPanel,removeButton,serialVersionUID
hcls EmptyStringListCellRenderer

CLSS public abstract interface org.netbeans.beaninfo.editors.StringArrayCustomizable
meth public abstract java.lang.String[] getStringArray()
meth public abstract void setStringArray(java.lang.String[])

CLSS public org.netbeans.beaninfo.editors.StringArrayEditor
cons public init()
intf org.netbeans.beaninfo.editors.StringArrayCustomizable
intf org.openide.explorer.propertysheet.ExPropertyEditor
intf org.openide.explorer.propertysheet.editors.XMLPropertyEditor
meth protected final java.lang.String getStrings(boolean)
meth public boolean isPaintable()
meth public boolean supportsCustomEditor()
meth public java.awt.Component getCustomEditor()
meth public java.lang.Object getValue()
meth public java.lang.String getAsText()
meth public java.lang.String getJavaInitializationString()
meth public java.lang.String[] getStringArray()
meth public java.lang.String[] getTags()
meth public org.w3c.dom.Node storeToXML(org.w3c.dom.Document)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void attachEnv(org.openide.explorer.propertysheet.PropertyEnv)
meth public void paintValue(java.awt.Graphics,java.awt.Rectangle)
meth public void readFromXML(org.w3c.dom.Node) throws java.io.IOException
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setAsText(java.lang.String)
meth public void setStringArray(java.lang.String[])
meth public void setValue(java.lang.Object)
supr java.lang.Object
hfds ATTR_COUNT,ATTR_INDEX,ATTR_VALUE,XML_STRING_ARRAY,XML_STRING_ITEM,editable,separator,strings,support

CLSS public org.netbeans.beaninfo.editors.StringCustomEditor
cons public init(java.lang.String,boolean)
 anno 0 java.lang.Deprecated()
intf java.beans.PropertyChangeListener
meth public void addNotify()
meth public void propertyChange(java.beans.PropertyChangeEvent)
supr javax.swing.JPanel
hfds TOO_LARGE,editor,env,instructions,oneline,serialVersionUID,textArea,textAreaScroll

CLSS public org.netbeans.beaninfo.editors.StringEditor
cons public init()
intf org.openide.explorer.propertysheet.ExPropertyEditor
meth public boolean isEditable()
meth public boolean supportsCustomEditor()
meth public java.awt.Component getCustomEditor()
meth public java.lang.String getAsText()
meth public java.lang.String getJavaInitializationString()
meth public void attachEnv(org.openide.explorer.propertysheet.PropertyEnv)
meth public void setAsText(java.lang.String)
supr java.beans.PropertyEditorSupport
hfds customEd,editable,env,instructions,nullValue,oneline,useRaw

CLSS public org.netbeans.beaninfo.editors.URLEditor
cons public init()
fld public final static java.lang.String ATTR_VALUE = "value"
fld public final static java.lang.String XML_URL = "Url"
intf org.openide.explorer.propertysheet.editors.XMLPropertyEditor
meth public boolean supportsCustomEditor()
meth public java.lang.String getAsText()
meth public java.lang.String getJavaInitializationString()
meth public org.w3c.dom.Node storeToXML(org.w3c.dom.Document)
meth public void readFromXML(org.w3c.dom.Node) throws java.io.IOException
meth public void setAsText(java.lang.String)
supr java.beans.PropertyEditorSupport

CLSS public abstract org.netbeans.beaninfo.editors.WrappersEditor
cons public init(java.lang.Class)
fld protected java.beans.PropertyEditor pe
intf org.openide.explorer.propertysheet.ExPropertyEditor
meth public boolean isPaintable()
meth public boolean supportsCustomEditor()
meth public java.awt.Component getCustomEditor()
meth public java.lang.Object getValue()
meth public java.lang.String getAsText()
meth public java.lang.String[] getTags()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void attachEnv(org.openide.explorer.propertysheet.PropertyEnv)
meth public void paintValue(java.awt.Graphics,java.awt.Rectangle)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setAsText(java.lang.String)
meth public void setValue(java.lang.Object)
supr java.lang.Object

CLSS abstract interface org.netbeans.beaninfo.editors.package-info

CLSS public org.netbeans.core.CLIOptions2
cons public init()
intf java.lang.Runnable
meth protected int cli(org.netbeans.CLIHandler$Args)
meth protected void usage(java.io.PrintWriter)
meth public void run()
supr org.netbeans.CLIHandler
hfds EQ_TIMEOUT,INSTANCE,LOG,cnt,task
hcls EQStuck

CLSS public final org.netbeans.core.CoreBridgeImpl
cons public init()
meth protected void attachToCategory(java.lang.Object)
meth protected void loadActionSection(org.netbeans.core.startup.ManifestSection$ActionSection,boolean) throws java.lang.Exception
meth protected void loadDefaultSection(org.netbeans.core.startup.ManifestSection,org.openide.util.lookup.InstanceContent$Convertor<org.netbeans.core.startup.ManifestSection,java.lang.Object>,boolean)
meth protected void loadLoaderSection(org.netbeans.core.startup.ManifestSection$LoaderSection,boolean) throws java.lang.Exception
meth protected void loaderPoolTransaction(boolean)
meth public int cli(java.lang.String[],java.io.InputStream,java.io.OutputStream,java.io.OutputStream,java.io.File)
meth public org.openide.util.Lookup lookupCacheLoad()
meth public void initializePlaf(java.lang.Class,int,java.net.URL)
meth public void registerPropertyEditors()
meth public void setStatusText(java.lang.String)
supr org.netbeans.core.startup.CoreBridge
hfds editorsRegistered

CLSS public org.netbeans.core.ExitDialog
cons public init()
intf java.awt.event.ActionListener
meth public java.awt.Dimension getPreferredSize()
meth public static boolean showDialog()
meth public void actionPerformed(java.awt.event.ActionEvent)
supr javax.swing.JPanel
hfds exitDialog,exitOptions,isAqua,list,listModel,result,serialVersionUID
hcls ExitDlgListCellRenderer

CLSS public org.netbeans.core.GuiRunLevel
cons public init()
intf org.netbeans.core.startup.RunLevel
meth public void run()
supr java.lang.Object
hcls InitWinSys

CLSS public org.netbeans.core.HtmlBrowserComponent
cons public init()
cons public init(boolean,boolean)
cons public init(org.openide.awt.HtmlBrowser$Factory,boolean,boolean)
innr public final static BrowserReplacer
intf java.beans.PropertyChangeListener
meth protected java.lang.Object writeReplace() throws java.io.ObjectStreamException
meth protected java.lang.String preferredID()
meth protected org.openide.awt.HtmlBrowser createBrowser(org.openide.awt.HtmlBrowser$Factory,boolean,boolean)
meth protected org.openide.windows.CloneableTopComponent createClonedObject()
meth protected void componentActivated()
meth protected void componentClosed()
meth protected void componentOpened()
meth public boolean isStatusLineVisible()
meth public boolean isToolbarVisible()
meth public final java.net.URL getDocumentURL()
meth public final void setEnableHome(boolean)
meth public final void setEnableLocation(boolean)
meth public int getPersistenceType()
meth public java.awt.Image getIcon()
meth public org.openide.awt.HtmlBrowser$Impl getBrowserImpl()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public org.openide.util.Lookup getLookup()
meth public void open()
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void recreateBrowser()
meth public void setStatusLineVisible(boolean)
meth public void setToolbarVisible(boolean)
meth public void setURL(java.lang.String)
meth public void setURL(java.net.URL)
meth public void setURLAndOpen(java.net.URL)
supr org.openide.windows.CloneableTopComponent
hfds MAX_TITLE_LENGTH,browserComponent,browserFactory,enableHome,enableLocation,proxyLookup,serialVersionUID,statusVisible,toolbarVisible,urlToLoad
hcls MyLookup

CLSS public final static org.netbeans.core.HtmlBrowserComponent$BrowserReplacer
 outer org.netbeans.core.HtmlBrowserComponent
cons public init()
cons public init(org.netbeans.core.HtmlBrowserComponent)
intf java.io.Externalizable
meth public void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr java.lang.Object
hfds bComp,serialVersionUID,statLine,toolbar,url

CLSS public org.netbeans.core.IDESettings
cons public init()
fld public final static java.lang.String PROP_EXTERNAL_WWWBROWSER = "ExternalWWWBrowser"
fld public final static java.lang.String PROP_WWWBROWSER = "WWWBrowser"
meth public static boolean isGui()
meth public static org.openide.awt.HtmlBrowser$Factory getExternalWWWBrowser()
meth public static org.openide.awt.HtmlBrowser$Factory getWWWBrowser()
meth public static void setExternalWWWBrowser(org.openide.awt.HtmlBrowser$Factory)
meth public static void setWWWBrowser(org.openide.awt.HtmlBrowser$Factory)
supr java.lang.Object

CLSS public org.netbeans.core.ModuleActions
cons public init()
meth public java.util.Collection<javax.swing.Action> getRunningActions()
meth public org.openide.util.actions.SystemAction[] getContextActions()
meth public static org.netbeans.core.ModuleActions getDefaultInstance()
meth public static void add(org.netbeans.core.startup.ManifestSection$ActionSection)
meth public static void attachTo(java.lang.Object)
meth public static void hideWaitCursor(java.lang.Object)
meth public static void remove(org.netbeans.core.startup.ManifestSection$ActionSection)
meth public static void showWaitCursor(java.lang.Object)
meth public void invokeAction(javax.swing.Action,java.awt.event.ActionEvent)
supr org.openide.actions.ActionManager
hfds array,err,glassPaneUses,map,module,runningActions

CLSS public final org.netbeans.core.NbErrorManager
cons public init()
meth public void close()
meth public void flush()
meth public void publish(java.util.logging.LogRecord)
supr java.util.logging.Handler
hcls Exc

CLSS public final org.netbeans.core.NbKeymap
cons public init()
fld public final static java.lang.String BINDING_REMOVED = "removed"
fld public final static java.lang.String SHADOW_EXT = "shadow"
innr public final static AcceleratorBindingImpl
intf java.util.Comparator<javax.swing.KeyStroke>
intf javax.swing.text.Keymap
meth public boolean isLocallyDefined(javax.swing.KeyStroke)
meth public int compare(javax.swing.KeyStroke,javax.swing.KeyStroke)
meth public java.lang.String getName()
meth public javax.swing.Action getAction(javax.swing.KeyStroke)
meth public javax.swing.Action getDefaultAction()
meth public javax.swing.Action[] getBoundActions()
meth public javax.swing.KeyStroke[] getBoundKeyStrokes()
meth public javax.swing.KeyStroke[] getKeyStrokesForAction(javax.swing.Action)
meth public javax.swing.text.Keymap getResolveParent()
meth public static javax.swing.KeyStroke[] getContext()
meth public void addActionForKeyStroke(javax.swing.KeyStroke,javax.swing.Action)
meth public void removeBindings()
meth public void removeKeyStrokeBinding(javax.swing.KeyStroke)
meth public void setDefaultAction(javax.swing.Action)
meth public void setResolveParent(javax.swing.text.Keymap)
supr java.lang.Object
hfds BROKEN,LOG,RP,action2Id,bindings,bindingsListener,context,id2Stroke,keymapListener,refreshTask
hcls Binding

CLSS public final static org.netbeans.core.NbKeymap$AcceleratorBindingImpl
 outer org.netbeans.core.NbKeymap
cons public init()
meth protected javax.swing.KeyStroke keyStrokeForAction(javax.swing.Action,org.openide.filesystems.FileObject)
supr org.openide.awt.AcceleratorBinding

CLSS public final org.netbeans.core.NbLifecycleManager
cons public init()
meth public static boolean isExiting()
meth public static void advancePolicy()
meth public void exit()
meth public void exit(int)
meth public void markForRestart()
meth public void saveAll()
supr org.openide.LifecycleManager
hfds LOG,isExitOnEventQueue,onExit,policyAdvanced,sndLoop

CLSS public final org.netbeans.core.NbLoaderPool
cons public init()
intf java.beans.PropertyChangeListener
intf java.lang.Runnable
intf org.openide.util.LookupListener
intf org.openide.util.TaskListener
meth protected java.util.Enumeration<org.openide.loaders.DataLoader> loaders()
meth public static boolean remove(org.openide.loaders.DataLoader,org.netbeans.core.NbLoaderPool)
meth public static org.netbeans.core.NbLoaderPool getNbLoaderPool()
meth public static void add(org.netbeans.core.startup.ManifestSection$LoaderSection) throws java.lang.Exception
meth public static void beginUpdates()
meth public static void endUpdates()
meth public static void load() throws java.io.IOException
meth public static void store() throws java.io.IOException
meth public static void waitFinished()
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void resultChanged(org.openide.util.LookupEvent)
meth public void run()
meth public void taskFinished(org.openide.util.Task)
supr org.openide.loaders.DataLoaderPool
hfds IN_TEST,LOADER_POOL_NAME,declarativeResolvers,err,fireTask,installAfters,installBefores,installationFinished,listener,listenersRegistered,loaders,loadersArray,mimeResolvers,modifiedLoaders,names2Loaders,repNames2Loaders,rp,serialVersionUID,updatingBatch,updatingBatchUsed

CLSS public final org.netbeans.core.NbStatusDisplayer
cons public init()
meth public java.lang.String getStatusText()
meth public org.openide.awt.StatusDisplayer$Message setStatusText(java.lang.String,int)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void setStatusText(java.lang.String)
supr org.openide.awt.StatusDisplayer
hfds RP,SURVIVING_TIME,cs,messages
hcls MessageImpl

CLSS public final org.netbeans.core.NbURLDisplayer
cons public init()
meth public void showURL(java.net.URL)
meth public void showURLExternal(java.net.URL)
supr org.openide.awt.HtmlBrowser$URLDisplayer
hfds RP,htmlViewer
hcls DesktopBrowser,NbBrowser

CLSS public final org.netbeans.core.NotifyExcPanel
intf java.awt.event.ActionListener
meth public void actionPerformed(java.awt.event.ActionEvent)
supr javax.swing.JPanel
hfds AUTO_FOCUS,INSTANCE,MAX_STORED_EXCEPTIONS,SIZE_PREFERRED_HEIGHT,SIZE_PREFERRED_WIDTH,current,descriptor,details,dialog,exceptions,extraH,extraW,lastBounds,next,output,previous,serialVersionUID,showDetails
hcls ArrayListPos,ExceptionFlasher

CLSS public org.netbeans.core.ProxySettings
cons public init()
fld public final static int AUTO_DETECT_PAC = 3
fld public final static int AUTO_DETECT_PROXY = 1
fld public final static int DIRECT_CONNECTION = 0
fld public final static int MANUAL_SET_PAC = 4
fld public final static int MANUAL_SET_PROXY = 2
fld public final static java.lang.String DIRECT = "DIRECT"
fld public final static java.lang.String HTTP_CONNECTION_TEST_URL = "https://netbeans.apache.org"
fld public final static java.lang.String NOT_PROXY_HOSTS = "proxyNonProxyHosts"
fld public final static java.lang.String PAC = "PAC"
fld public final static java.lang.String PROXY_AUTHENTICATION_PASSWORD = "proxyAuthenticationPassword"
fld public final static java.lang.String PROXY_AUTHENTICATION_USERNAME = "proxyAuthenticationUsername"
fld public final static java.lang.String PROXY_HTTPS_HOST = "proxyHttpsHost"
fld public final static java.lang.String PROXY_HTTPS_PORT = "proxyHttpsPort"
fld public final static java.lang.String PROXY_HTTP_HOST = "proxyHttpHost"
fld public final static java.lang.String PROXY_HTTP_PORT = "proxyHttpPort"
fld public final static java.lang.String PROXY_SOCKS_HOST = "proxySocksHost"
fld public final static java.lang.String PROXY_SOCKS_PORT = "proxySocksPort"
fld public final static java.lang.String PROXY_TYPE = "proxyType"
fld public final static java.lang.String SYSTEM_NON_PROXY_HOSTS = "systemProxyNonProxyHosts"
fld public final static java.lang.String SYSTEM_PAC = "systemPAC"
fld public final static java.lang.String SYSTEM_PROXY_HTTPS_HOST = "systemProxyHttpsHost"
fld public final static java.lang.String SYSTEM_PROXY_HTTPS_PORT = "systemProxyHttpsPort"
fld public final static java.lang.String SYSTEM_PROXY_HTTP_HOST = "systemProxyHttpHost"
fld public final static java.lang.String SYSTEM_PROXY_HTTP_PORT = "systemProxyHttpPort"
fld public final static java.lang.String SYSTEM_PROXY_SOCKS_HOST = "systemProxySocksHost"
fld public final static java.lang.String SYSTEM_PROXY_SOCKS_PORT = "systemProxySocksPort"
fld public final static java.lang.String TEST_SYSTEM_PROXY_HTTP_HOST = "testSystemProxyHttpHost"
fld public final static java.lang.String TEST_SYSTEM_PROXY_HTTP_PORT = "testSystemProxyHttpPort"
fld public final static java.lang.String USE_PROXY_ALL_PROTOCOLS = "useProxyAllProtocols"
fld public final static java.lang.String USE_PROXY_AUTHENTICATION = "useProxyAuthentication"
innr public abstract static Reloader
innr public static NbProxyCredentialsProvider
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
meth public static java.lang.String getSystemHttpHost()
meth public static java.lang.String getSystemHttpPort()
meth public static java.lang.String getSystemHttpsHost()
meth public static java.lang.String getSystemHttpsPort()
meth public static java.lang.String getSystemNonProxyHosts()
meth public static java.lang.String getSystemPac()
meth public static java.lang.String getSystemSocksHost()
meth public static java.lang.String getSystemSocksPort()
meth public static java.lang.String getTestSystemHttpHost()
meth public static java.lang.String getTestSystemHttpPort()
meth public static java.lang.String normalizeProxyHost(java.lang.String)
meth public static void addPreferenceChangeListener(java.util.prefs.PreferenceChangeListener)
meth public static void reload()
meth public static void removePreferenceChangeListener(java.util.prefs.PreferenceChangeListener)
meth public static void setAuthenticationPassword(char[])
supr java.lang.Object
hfds LOGGER,presetNonProxyHosts

CLSS public static org.netbeans.core.ProxySettings$NbProxyCredentialsProvider
 outer org.netbeans.core.ProxySettings
cons public init()
meth protected boolean isProxyAuthentication(java.net.URI)
meth protected char[] getProxyPassword(java.net.URI)
meth protected java.lang.String getProxyUserName(java.net.URI)
meth public java.lang.String getProxyHost(java.net.URI)
meth public java.lang.String getProxyPort(java.net.URI)
supr org.openide.util.NetworkSettings$ProxyCredentialsProvider

CLSS public abstract static org.netbeans.core.ProxySettings$Reloader
 outer org.netbeans.core.ProxySettings
cons public init()
meth public abstract void reload()
supr java.lang.Object

CLSS public final org.netbeans.core.Services
 anno 0 java.lang.Deprecated()
cons public init()
intf org.openide.util.LookupListener
meth public <%0 extends org.openide.ServiceType> java.util.Enumeration<{%%0}> services(java.lang.Class<{%%0}>)
meth public java.util.Enumeration<org.openide.ServiceType> services()
meth public java.util.List<org.openide.ServiceType> getServiceTypes()
meth public org.openide.ServiceType find(java.lang.Class)
meth public org.openide.ServiceType find(java.lang.String)
meth public static org.netbeans.core.Services getDefault()
meth public void resultChanged(org.openide.util.LookupEvent)
meth public void setServiceTypes(java.util.List)
supr org.openide.ServiceType$Registry
hfds allTypes,name2Service,serialVersionUID

CLSS public final org.netbeans.core.UIExceptions
meth public static void annotateUser(java.lang.Throwable,java.lang.String,java.lang.String,java.lang.Throwable,java.util.Date)
supr java.lang.Object
hcls AnnException,OwnLevel

CLSS public abstract interface org.netbeans.core.WindowSystem
meth public abstract void hide()
meth public abstract void init()
meth public abstract void load()
meth public abstract void save()
meth public abstract void show()

CLSS public org.netbeans.core.actions.AboutAction
cons public init()
meth protected boolean asynchronous()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void performAction()
supr org.openide.util.actions.CallableSystemAction

CLSS public org.netbeans.core.actions.HTMLViewAction
cons public init()
meth protected boolean asynchronous()
meth protected java.lang.String iconResource()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void performAction()
supr org.openide.util.actions.CallableSystemAction

CLSS public org.netbeans.core.actions.JumpNextAction
cons public init()
meth protected boolean asynchronous()
meth protected java.lang.String iconResource()
meth protected void initialize()
meth public java.lang.Object getActionMapKey()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void setActionPerformer(org.openide.util.actions.ActionPerformer)
supr org.openide.util.actions.CallbackSystemAction

CLSS public org.netbeans.core.actions.JumpPrevAction
cons public init()
meth protected boolean asynchronous()
meth protected java.lang.String iconResource()
meth protected void initialize()
meth public java.lang.Object getActionMapKey()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void setActionPerformer(org.openide.util.actions.ActionPerformer)
supr org.openide.util.actions.CallbackSystemAction

CLSS public org.netbeans.core.actions.LogAction
cons public init()
intf java.awt.event.ActionListener
meth public void actionPerformed(java.awt.event.ActionEvent)
supr java.lang.Object

CLSS public org.netbeans.core.actions.LogViewerSupport
cons public init(java.io.File,java.lang.String)
intf java.lang.Runnable
meth public void run()
meth public void showLogViewer() throws java.io.IOException
meth public void stopUpdatingLogViewer()
supr java.lang.Object
hfds RP,fileName,filestream,ins,io,ioName,lines,ring,shouldStop,task
hcls Ring

CLSS public org.netbeans.core.actions.NextViewCallbackAction
cons public init()
meth protected boolean asynchronous()
meth public java.lang.Object getActionMapKey()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CallbackSystemAction

CLSS public org.netbeans.core.actions.PreviousViewCallbackAction
cons public init()
meth protected boolean asynchronous()
meth public java.lang.Object getActionMapKey()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CallbackSystemAction

CLSS public org.netbeans.core.actions.SystemExit
cons public init()
intf java.lang.Runnable
meth protected boolean asynchronous()
meth protected void initialize()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void performAction()
meth public void run()
supr org.openide.util.actions.CallableSystemAction
hfds RP,serialVersionUID

CLSS public abstract org.netbeans.core.startup.CoreBridge
cons public init()
meth protected abstract void attachToCategory(java.lang.Object)
meth protected abstract void loadActionSection(org.netbeans.core.startup.ManifestSection$ActionSection,boolean) throws java.lang.Exception
meth protected abstract void loadDefaultSection(org.netbeans.core.startup.ManifestSection,org.openide.util.lookup.InstanceContent$Convertor<org.netbeans.core.startup.ManifestSection,java.lang.Object>,boolean)
meth protected abstract void loadLoaderSection(org.netbeans.core.startup.ManifestSection$LoaderSection,boolean) throws java.lang.Exception
meth protected abstract void loaderPoolTransaction(boolean)
meth public abstract int cli(java.lang.String[],java.io.InputStream,java.io.OutputStream,java.io.OutputStream,java.io.File)
meth public abstract org.openide.util.Lookup lookupCacheLoad()
meth public abstract void initializePlaf(java.lang.Class,int,java.net.URL)
meth public abstract void registerPropertyEditors()
meth public abstract void setStatusText(java.lang.String)
meth public static org.netbeans.core.startup.CoreBridge getDefault()
meth public static void defineOsTokens(java.util.Collection<? super java.lang.String>)
supr java.lang.Object
hcls FakeBridge

CLSS public abstract interface org.netbeans.core.startup.RunLevel
meth public abstract void run()

CLSS public abstract org.openide.LifecycleManager
cons protected init()
meth public abstract void exit()
meth public abstract void saveAll()
meth public static org.openide.LifecycleManager getDefault()
meth public void exit(int)
meth public void markForRestart()
supr java.lang.Object
hcls Trivial

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

CLSS public abstract static org.openide.ServiceType$Registry
 outer org.openide.ServiceType
 anno 0 java.lang.Deprecated()
cons public init()
intf java.io.Serializable
meth public <%0 extends org.openide.ServiceType> java.util.Enumeration<{%%0}> services(java.lang.Class<{%%0}>)
meth public abstract java.util.Enumeration<org.openide.ServiceType> services()
meth public abstract java.util.List getServiceTypes()
meth public abstract void setServiceTypes(java.util.List)
 anno 0 java.lang.Deprecated()
meth public org.openide.ServiceType find(java.lang.Class)
 anno 0 java.lang.Deprecated()
meth public org.openide.ServiceType find(java.lang.String)
supr java.lang.Object
hfds serialVersionUID

CLSS public abstract org.openide.actions.ActionManager
cons public init()
fld public final static java.lang.String PROP_CONTEXT_ACTIONS = "contextActions"
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract org.openide.util.actions.SystemAction[] getContextActions()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public static org.openide.actions.ActionManager getDefault()
meth public void invokeAction(javax.swing.Action,java.awt.event.ActionEvent)
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds supp
hcls Trivial

CLSS public abstract org.openide.awt.AcceleratorBinding
cons protected init()
meth protected abstract javax.swing.KeyStroke keyStrokeForAction(javax.swing.Action,org.openide.filesystems.FileObject)
meth public static void setAccelerator(javax.swing.Action,org.openide.filesystems.FileObject)
supr java.lang.Object
hfds ALL
hcls Iter

CLSS public org.openide.awt.HtmlBrowser
cons public init()
cons public init(boolean,boolean)
cons public init(org.openide.awt.HtmlBrowser$Factory,boolean,boolean)
cons public init(org.openide.awt.HtmlBrowser$Factory,boolean,boolean,java.awt.Component)
fld public final static int DEFAULT_HEIGHT = 600
fld public final static int DEFAULT_WIDTH = 400
innr public abstract interface static Factory
innr public abstract static Impl
innr public abstract static URLDisplayer
meth public boolean isStatusLineVisible()
meth public boolean isToolbarVisible()
meth public boolean requestFocusInWindow()
meth public final java.awt.Component getBrowserComponent()
meth public final java.net.URL getDocumentURL()
meth public final org.openide.awt.HtmlBrowser$Impl getBrowserImpl()
meth public final void setEnableHome(boolean)
meth public final void setEnableLocation(boolean)
meth public java.awt.Dimension getPreferredSize()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public static java.lang.String getHomePage()
meth public static void setFactory(org.openide.awt.HtmlBrowser$Factory)
 anno 0 java.lang.Deprecated()
meth public static void setHomePage(java.lang.String)
meth public void requestFocus()
meth public void setStatusLineVisible(boolean)
meth public void setToolbarVisible(boolean)
meth public void setURL(java.lang.String)
meth public void setURL(java.net.URL)
supr javax.swing.JPanel
hfds bBack,bForward,bReload,bStop,browserComponent,browserFactory,browserImpl,browserListener,extraToolbar,head,homePage,ignoreChangeInLocationField,lStatusLine,rp,serialVersionUID,statusLineVisible,toolbarVisible,txtLocation
hcls AccessibleHtmlBrowser,BrowserListener,TrivialURLDisplayer

CLSS public abstract static org.openide.awt.HtmlBrowser$URLDisplayer
 outer org.openide.awt.HtmlBrowser
cons protected init()
meth public abstract void showURL(java.net.URL)
meth public static org.openide.awt.HtmlBrowser$URLDisplayer getDefault()
meth public void showURLExternal(java.net.URL)
supr java.lang.Object

CLSS public abstract org.openide.awt.StatusDisplayer
cons protected init()
fld public final static int IMPORTANCE_ANNOTATION = 1000
fld public final static int IMPORTANCE_ERROR_HIGHLIGHT = 700
fld public final static int IMPORTANCE_FIND_OR_REPLACE = 800
fld public final static int IMPORTANCE_INCREMENTAL_FIND = 900
innr public abstract interface static Message
meth public abstract java.lang.String getStatusText()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public abstract void setStatusText(java.lang.String)
meth public org.openide.awt.StatusDisplayer$Message setStatusText(java.lang.String,int)
meth public static org.openide.awt.StatusDisplayer getDefault()
supr java.lang.Object
hfds INSTANCE
hcls Trivial

CLSS public abstract interface org.openide.explorer.propertysheet.ExPropertyEditor
fld public final static java.lang.String PROPERTY_HELP_ID = "helpID"
fld public final static java.lang.String PROP_VALUE_VALID = "propertyValueValid"
intf java.beans.PropertyEditor
meth public abstract void attachEnv(org.openide.explorer.propertysheet.PropertyEnv)

CLSS public abstract interface org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor
 anno 0 java.lang.Deprecated()
intf java.beans.PropertyEditor
meth public abstract boolean hasInPlaceCustomEditor()
meth public abstract boolean supportsEditingTaggedValues()
meth public abstract java.awt.Component getInPlaceCustomEditor()

CLSS public abstract interface org.openide.explorer.propertysheet.editors.XMLPropertyEditor
intf java.beans.PropertyEditor
meth public abstract org.w3c.dom.Node storeToXML(org.w3c.dom.Document)
meth public abstract void readFromXML(org.w3c.dom.Node) throws java.io.IOException

CLSS public abstract org.openide.loaders.DataLoaderPool
cons protected init()
cons protected init(org.openide.loaders.DataLoader)
intf java.io.Serializable
meth protected abstract java.util.Enumeration<? extends org.openide.loaders.DataLoader> loaders()
meth protected final void fireChangeEvent(javax.swing.event.ChangeEvent)
meth public final java.util.Enumeration<org.openide.loaders.DataLoader> allLoaders()
meth public final java.util.Enumeration<org.openide.loaders.DataLoader> producersOf(java.lang.Class<? extends org.openide.loaders.DataObject>)
meth public final org.openide.loaders.DataLoader firstProducerOf(java.lang.Class<? extends org.openide.loaders.DataObject>)
meth public final void addChangeListener(javax.swing.event.ChangeListener)
meth public final void addOperationListener(org.openide.loaders.OperationListener)
meth public final void removeChangeListener(javax.swing.event.ChangeListener)
meth public final void removeOperationListener(org.openide.loaders.OperationListener)
meth public org.openide.loaders.DataLoader[] toArray()
meth public org.openide.loaders.DataObject findDataObject(org.openide.filesystems.FileObject) throws java.io.IOException
meth public org.openide.loaders.DataObject findDataObject(org.openide.filesystems.FileObject,org.openide.loaders.DataLoader$RecognizedFiles) throws java.io.IOException
meth public static <%0 extends org.openide.loaders.DataObject> org.openide.loaders.DataObject$Factory factory(java.lang.Class<{%%0}>,java.lang.String,java.awt.Image)
meth public static org.openide.loaders.DataLoader getPreferredLoader(org.openide.filesystems.FileObject)
meth public static org.openide.loaders.DataLoaderPool getDefault()
meth public static org.openide.loaders.OperationListener createWeakOperationListener(org.openide.loaders.OperationListener,java.lang.Object)
meth public static void setPreferredLoader(org.openide.filesystems.FileObject,org.openide.loaders.DataLoader) throws java.io.IOException
supr java.lang.Object
hfds DEFAULT,allLoaders,cntchanges,defaultLoaders,emptyDataLoaderRecognized,listeners,loaderArray,prefLoaders,preferredLoader,serialVersionUID,systemLoaders
hcls DefaultLoader,DefaultPool,EmptyDataLoaderRecognized,FolderLoader,InstanceLoader,InstanceLoaderSystem,ShadowLoader

CLSS public abstract interface !annotation org.openide.nodes.PropertyEditorRegistration
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class<?>[] targetType()

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

CLSS public abstract interface org.openide.util.LookupListener
intf java.util.EventListener
meth public abstract void resultChanged(org.openide.util.LookupEvent)

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

CLSS public abstract interface org.openide.util.TaskListener
intf java.util.EventListener
meth public abstract void taskFinished(org.openide.util.Task)

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

CLSS public abstract org.openide.util.actions.CallbackSystemAction
cons public init()
intf org.openide.util.ContextAwareAction
meth protected void initialize()
meth public boolean getSurviveFocusChange()
meth public java.lang.Object getActionMapKey()
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
meth public org.openide.util.actions.ActionPerformer getActionPerformer()
 anno 0 java.lang.Deprecated()
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void performAction()
 anno 0 java.lang.Deprecated()
meth public void setActionPerformer(org.openide.util.actions.ActionPerformer)
 anno 0 java.lang.Deprecated()
meth public void setSurviveFocusChange(boolean)
supr org.openide.util.actions.CallableSystemAction
hfds LISTENER,PROP_ACTION_PERFORMER,err,notSurviving,serialVersionUID,surviving
hcls ActionDelegateListener,DelegateAction,GlobalManager,WeakAction

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

CLSS public abstract org.openide.windows.CloneableTopComponent
cons public init()
fld public final static org.openide.windows.CloneableTopComponent$Ref EMPTY
innr public static Ref
intf java.io.Externalizable
intf org.openide.windows.TopComponent$Cloneable
meth protected boolean closeLast()
meth protected org.openide.windows.CloneableTopComponent createClonedObject()
meth protected void componentClosed()
meth protected void componentOpened()
meth public boolean canClose()
meth public boolean canClose(org.openide.windows.Workspace,boolean)
meth public final java.lang.Object clone()
meth public final org.openide.windows.CloneableTopComponent cloneTopComponent()
meth public final org.openide.windows.CloneableTopComponent$Ref getReference()
meth public final org.openide.windows.TopComponent cloneComponent()
meth public final void setReference(org.openide.windows.CloneableTopComponent$Ref)
meth public void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr org.openide.windows.TopComponent
hfds isLastActivated,ref,serialVersionUID

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

CLSS public abstract interface static org.openide.windows.TopComponent$Cloneable
 outer org.openide.windows.TopComponent
meth public abstract org.openide.windows.TopComponent cloneComponent()

