#Signature file v4.1
#Version 1.57

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

CLSS public abstract interface java.awt.datatransfer.Transferable
meth public abstract boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
meth public abstract java.awt.datatransfer.DataFlavor[] getTransferDataFlavors()
meth public abstract java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException

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

CLSS public abstract interface java.io.Serializable

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

CLSS public javax.swing.TransferHandler
cons protected init()
cons public init(java.lang.String)
fld public final static int COPY = 1
fld public final static int COPY_OR_MOVE = 3
fld public final static int LINK = 1073741824
fld public final static int MOVE = 2
fld public final static int NONE = 0
innr public final static TransferSupport
innr public static DropLocation
intf java.io.Serializable
meth protected java.awt.datatransfer.Transferable createTransferable(javax.swing.JComponent)
meth protected void exportDone(javax.swing.JComponent,java.awt.datatransfer.Transferable,int)
meth public boolean canImport(javax.swing.JComponent,java.awt.datatransfer.DataFlavor[])
meth public boolean canImport(javax.swing.TransferHandler$TransferSupport)
meth public boolean importData(javax.swing.JComponent,java.awt.datatransfer.Transferable)
meth public boolean importData(javax.swing.TransferHandler$TransferSupport)
meth public int getSourceActions(javax.swing.JComponent)
meth public java.awt.Image getDragImage()
meth public java.awt.Point getDragImageOffset()
meth public javax.swing.Icon getVisualRepresentation(java.awt.datatransfer.Transferable)
meth public static javax.swing.Action getCopyAction()
meth public static javax.swing.Action getCutAction()
meth public static javax.swing.Action getPasteAction()
meth public void exportAsDrag(javax.swing.JComponent,java.awt.event.InputEvent,int)
meth public void exportToClipboard(javax.swing.JComponent,java.awt.datatransfer.Clipboard,int)
meth public void setDragImage(java.awt.Image)
meth public void setDragImageOffset(java.awt.Point)
supr java.lang.Object
hcls HasGetTransferHandler

CLSS public abstract interface javax.swing.event.CellEditorListener
intf java.util.EventListener
meth public abstract void editingCanceled(javax.swing.event.ChangeEvent)
meth public abstract void editingStopped(javax.swing.event.ChangeEvent)

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

CLSS public abstract interface javax.swing.event.TableModelListener
intf java.util.EventListener
meth public abstract void tableChanged(javax.swing.event.TableModelEvent)

CLSS public abstract interface javax.swing.event.TreeWillExpandListener
intf java.util.EventListener
meth public abstract void treeWillCollapse(javax.swing.event.TreeExpansionEvent) throws javax.swing.tree.ExpandVetoException
meth public abstract void treeWillExpand(javax.swing.event.TreeExpansionEvent) throws javax.swing.tree.ExpandVetoException

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

CLSS public javax.swing.table.DefaultTableColumnModel
cons public init()
fld protected boolean columnSelectionAllowed
fld protected int columnMargin
fld protected int totalColumnWidth
fld protected java.util.Vector<javax.swing.table.TableColumn> tableColumns
fld protected javax.swing.ListSelectionModel selectionModel
fld protected javax.swing.event.ChangeEvent changeEvent
fld protected javax.swing.event.EventListenerList listenerList
intf java.beans.PropertyChangeListener
intf java.io.Serializable
intf javax.swing.event.ListSelectionListener
intf javax.swing.table.TableColumnModel
meth protected javax.swing.ListSelectionModel createSelectionModel()
meth protected void fireColumnAdded(javax.swing.event.TableColumnModelEvent)
meth protected void fireColumnMarginChanged()
meth protected void fireColumnMoved(javax.swing.event.TableColumnModelEvent)
meth protected void fireColumnRemoved(javax.swing.event.TableColumnModelEvent)
meth protected void fireColumnSelectionChanged(javax.swing.event.ListSelectionEvent)
meth protected void recalcWidthCache()
meth public <%0 extends java.util.EventListener> {%%0}[] getListeners(java.lang.Class<{%%0}>)
meth public boolean getColumnSelectionAllowed()
meth public int getColumnCount()
meth public int getColumnIndex(java.lang.Object)
meth public int getColumnIndexAtX(int)
meth public int getColumnMargin()
meth public int getSelectedColumnCount()
meth public int getTotalColumnWidth()
meth public int[] getSelectedColumns()
meth public java.util.Enumeration<javax.swing.table.TableColumn> getColumns()
meth public javax.swing.ListSelectionModel getSelectionModel()
meth public javax.swing.event.TableColumnModelListener[] getColumnModelListeners()
meth public javax.swing.table.TableColumn getColumn(int)
meth public void addColumn(javax.swing.table.TableColumn)
meth public void addColumnModelListener(javax.swing.event.TableColumnModelListener)
meth public void moveColumn(int,int)
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void removeColumn(javax.swing.table.TableColumn)
meth public void removeColumnModelListener(javax.swing.event.TableColumnModelListener)
meth public void setColumnMargin(int)
meth public void setColumnSelectionAllowed(boolean)
meth public void setSelectionModel(javax.swing.ListSelectionModel)
meth public void valueChanged(javax.swing.event.ListSelectionEvent)
supr java.lang.Object

CLSS public abstract interface javax.swing.table.TableCellRenderer
meth public abstract java.awt.Component getTableCellRendererComponent(javax.swing.JTable,java.lang.Object,boolean,boolean,int,int)

CLSS public javax.swing.table.TableColumn
cons public init()
cons public init(int)
cons public init(int,int)
cons public init(int,int,javax.swing.table.TableCellRenderer,javax.swing.table.TableCellEditor)
fld protected boolean isResizable
fld protected int maxWidth
fld protected int minWidth
fld protected int modelIndex
fld protected int resizedPostingDisableCount
 anno 0 java.lang.Deprecated()
fld protected int width
fld protected java.lang.Object headerValue
fld protected java.lang.Object identifier
fld protected javax.swing.table.TableCellEditor cellEditor
fld protected javax.swing.table.TableCellRenderer cellRenderer
fld protected javax.swing.table.TableCellRenderer headerRenderer
fld public final static java.lang.String CELL_RENDERER_PROPERTY = "cellRenderer"
fld public final static java.lang.String COLUMN_WIDTH_PROPERTY = "columWidth"
fld public final static java.lang.String HEADER_RENDERER_PROPERTY = "headerRenderer"
fld public final static java.lang.String HEADER_VALUE_PROPERTY = "headerValue"
intf java.io.Serializable
meth protected javax.swing.table.TableCellRenderer createDefaultHeaderRenderer()
meth public boolean getResizable()
meth public int getMaxWidth()
meth public int getMinWidth()
meth public int getModelIndex()
meth public int getPreferredWidth()
meth public int getWidth()
meth public java.beans.PropertyChangeListener[] getPropertyChangeListeners()
meth public java.lang.Object getHeaderValue()
meth public java.lang.Object getIdentifier()
meth public javax.swing.table.TableCellEditor getCellEditor()
meth public javax.swing.table.TableCellRenderer getCellRenderer()
meth public javax.swing.table.TableCellRenderer getHeaderRenderer()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void disableResizedPosting()
 anno 0 java.lang.Deprecated()
meth public void enableResizedPosting()
 anno 0 java.lang.Deprecated()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setCellEditor(javax.swing.table.TableCellEditor)
meth public void setCellRenderer(javax.swing.table.TableCellRenderer)
meth public void setHeaderRenderer(javax.swing.table.TableCellRenderer)
meth public void setHeaderValue(java.lang.Object)
meth public void setIdentifier(java.lang.Object)
meth public void setMaxWidth(int)
meth public void setMinWidth(int)
meth public void setModelIndex(int)
meth public void setPreferredWidth(int)
meth public void setResizable(boolean)
meth public void setWidth(int)
meth public void sizeWidthToFit()
supr java.lang.Object

CLSS public abstract interface javax.swing.table.TableColumnModel
meth public abstract boolean getColumnSelectionAllowed()
meth public abstract int getColumnCount()
meth public abstract int getColumnIndex(java.lang.Object)
meth public abstract int getColumnIndexAtX(int)
meth public abstract int getColumnMargin()
meth public abstract int getSelectedColumnCount()
meth public abstract int getTotalColumnWidth()
meth public abstract int[] getSelectedColumns()
meth public abstract java.util.Enumeration<javax.swing.table.TableColumn> getColumns()
meth public abstract javax.swing.ListSelectionModel getSelectionModel()
meth public abstract javax.swing.table.TableColumn getColumn(int)
meth public abstract void addColumn(javax.swing.table.TableColumn)
meth public abstract void addColumnModelListener(javax.swing.event.TableColumnModelListener)
meth public abstract void moveColumn(int,int)
meth public abstract void removeColumn(javax.swing.table.TableColumn)
meth public abstract void removeColumnModelListener(javax.swing.event.TableColumnModelListener)
meth public abstract void setColumnMargin(int)
meth public abstract void setColumnSelectionAllowed(boolean)
meth public abstract void setSelectionModel(javax.swing.ListSelectionModel)

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

CLSS public abstract interface javax.swing.tree.TreeModel
meth public abstract boolean isLeaf(java.lang.Object)
meth public abstract int getChildCount(java.lang.Object)
meth public abstract int getIndexOfChild(java.lang.Object,java.lang.Object)
meth public abstract java.lang.Object getChild(java.lang.Object,int)
meth public abstract java.lang.Object getRoot()
meth public abstract void addTreeModelListener(javax.swing.event.TreeModelListener)
meth public abstract void removeTreeModelListener(javax.swing.event.TreeModelListener)
meth public abstract void valueForPathChanged(javax.swing.tree.TreePath,java.lang.Object)

CLSS public org.netbeans.swing.etable.ETable
cons public init()
cons public init(int,int)
cons public init(java.lang.Object[][],java.lang.Object[])
cons public init(java.util.Vector,java.util.Vector)
cons public init(javax.swing.table.TableModel)
cons public init(javax.swing.table.TableModel,javax.swing.table.TableColumnModel)
cons public init(javax.swing.table.TableModel,javax.swing.table.TableColumnModel,javax.swing.ListSelectionModel)
fld protected int[] inverseSortingPermutation
fld protected int[] sortingPermutation
fld public final static java.lang.String PROP_QUICK_FILTER = "quickFilter"
innr public final static !enum ColumnSelection
innr public final static RowMapping
meth protected boolean acceptByQuickFilter(javax.swing.table.TableModel,int)
meth protected boolean processKeyBinding(javax.swing.KeyStroke,java.awt.event.KeyEvent,int,boolean)
meth protected javax.swing.ListSelectionModel createDefaultSelectionModel()
meth protected javax.swing.table.JTableHeader createDefaultTableHeader()
meth protected javax.swing.table.TableColumn createColumn(int)
meth protected javax.swing.table.TableColumnModel createDefaultColumnModel()
meth protected void configureEnclosingScrollPane()
meth protected void initializeLocalVars()
meth protected void sortAndFilter()
meth public boolean editCellAt(int,int,java.util.EventObject)
meth public boolean isCellEditable(int,int)
meth public boolean isColumnHidingAllowed()
meth public boolean isFullyEditable()
meth public boolean isFullyNonEditable()
meth public boolean isPopupUsedFromTheCorner()
meth public final void showColumnSelectionDialog()
meth public int convertRowIndexToModel(int)
meth public int convertRowIndexToView(int)
meth public int getQuickFilterColumn()
meth public int getRowCount()
meth public java.lang.Object getQuickFilterObject()
meth public java.lang.Object getValueAt(int,int)
meth public java.lang.Object transformValue(java.lang.Object)
meth public java.lang.String convertValueToString(java.lang.Object)
meth public java.lang.String getColumnDisplayName(java.lang.String)
meth public java.lang.String getToolTipText(java.awt.event.MouseEvent)
meth public java.lang.String getTransferDelimiter(boolean)
meth public java.lang.String[] getQuickFilterFormatStrings()
meth public javax.swing.JMenuItem getQuickFilterCompareItem(int,java.lang.Object,java.lang.String,java.lang.String,boolean,boolean)
meth public javax.swing.JMenuItem getQuickFilterEqualsItem(int,java.lang.Object,java.lang.String,java.lang.String,boolean)
meth public javax.swing.JMenuItem getQuickFilterNoFilterItem(java.lang.String)
meth public javax.swing.JMenuItem getQuickFilterPopup(int,java.lang.Object,java.lang.String)
meth public javax.swing.RowSorter<? extends javax.swing.table.TableModel> getRowSorter()
meth public org.netbeans.swing.etable.ETable$ColumnSelection getColumnSelectionOn(int)
meth public org.netbeans.swing.etable.TableColumnSelector getColumnSelector()
meth public static org.netbeans.swing.etable.TableColumnSelector getDefaultColumnSelector()
meth public static void setDefaultColumnSelector(org.netbeans.swing.etable.TableColumnSelector)
meth public void createDefaultColumnsFromModel()
meth public void displaySearchField()
meth public void doLayout()
meth public void readSettings(java.util.Properties,java.lang.String)
meth public void removeEditor()
meth public void setAutoCreateRowSorter(boolean)
meth public void setCellBackground(java.awt.Component,boolean,int,int)
meth public void setColumnHidingAllowed(boolean)
meth public void setColumnSelectionOn(int,org.netbeans.swing.etable.ETable$ColumnSelection)
meth public void setColumnSelector(org.netbeans.swing.etable.TableColumnSelector)
meth public void setColumnSorted(int,boolean,int)
meth public void setFullyEditable(boolean)
meth public void setFullyNonEditable(boolean)
meth public void setModel(javax.swing.table.TableModel)
meth public void setPopupUsedFromTheCorner(boolean)
meth public void setQuickFilter(int,java.lang.Object)
meth public void setQuickFilterFormatStrings(java.lang.String[])
meth public void setRowSorter(javax.swing.RowSorter<? extends javax.swing.table.TableModel>)
meth public void setSelectVisibleColumnsLabel(java.lang.String)
meth public void setValueAt(java.lang.Object,int,int)
meth public void tableChanged(javax.swing.event.TableModelEvent)
meth public void unsetQuickFilter()
meth public void writeSettings(java.util.Properties,java.lang.String)
supr javax.swing.JTable
hfds ACTION_FOCUS_NEXT,COMPUTING_TOOLTIP,DEFAULT,DEFAULT_COLUMNS_ICON,FULLY_EDITABLE,FULLY_NONEDITABLE,LOG,SEARCH_COLUMN,SEARCH_FIELD_PREFERRED_SIZE,SEARCH_FIELD_SPACE,columnHidingAllowed,columnSelectionMouseListener,columnSelectionOnMouseClick,columnSelectionOnMouseClickLock,columnSelector,defaultColumnSelector,editing,filteredRowCount,headerMouseListener,heightOfTextField,inEditRequest,inRemoveRequest,maxPrefix,quickFilterColumn,quickFilterFormatStrings,quickFilterObject,searchColumn,searchCombo,searchPanel,searchTextField,selectVisibleColumnsLabel,selectedColumnWhenTableChanged,selectedRowsWhenTableChanged,sortable,sortingPermutationsWhenTableChanged
hcls CTRLTabAction,CancelEditAction,ColumnSelectionMouseListener,CompareQuickFilter,EditAction,EnterAction,EqualsQuickFilter,HeaderMouseListener,NavigationAction,OriginalRowComparator,STPolicy,SearchComboListener,SearchFieldListener,SearchTextField,SelectedRows

CLSS public final static !enum org.netbeans.swing.etable.ETable$ColumnSelection
 outer org.netbeans.swing.etable.ETable
fld public final static org.netbeans.swing.etable.ETable$ColumnSelection DIALOG
fld public final static org.netbeans.swing.etable.ETable$ColumnSelection NO_SELECTION
fld public final static org.netbeans.swing.etable.ETable$ColumnSelection POPUP
meth public static org.netbeans.swing.etable.ETable$ColumnSelection valueOf(java.lang.String)
meth public static org.netbeans.swing.etable.ETable$ColumnSelection[] values()
supr java.lang.Enum<org.netbeans.swing.etable.ETable$ColumnSelection>

CLSS public final static org.netbeans.swing.etable.ETable$RowMapping
 outer org.netbeans.swing.etable.ETable
cons public init(int,javax.swing.table.TableModel)
cons public init(int,javax.swing.table.TableModel,org.netbeans.swing.etable.ETable)
meth public int getModelRowIndex()
meth public java.lang.Object getModelObject(int)
meth public java.lang.Object getTransformedValue(int)
supr java.lang.Object
hfds TRANSFORMED_NONE,allTransformed,model,originalIndex,table,transformed,transformedColumn

CLSS public org.netbeans.swing.etable.ETableColumn
cons public init(int,int,javax.swing.table.TableCellRenderer,javax.swing.table.TableCellEditor,org.netbeans.swing.etable.ETable)
cons public init(int,int,org.netbeans.swing.etable.ETable)
cons public init(int,org.netbeans.swing.etable.ETable)
cons public init(org.netbeans.swing.etable.ETable)
innr public RowComparator
intf java.lang.Comparable<org.netbeans.swing.etable.ETableColumn>
meth protected java.util.Comparator<org.netbeans.swing.etable.ETable$RowMapping> getRowComparator(int,boolean)
meth protected javax.swing.table.TableCellRenderer createDefaultHeaderRenderer()
meth public boolean isAscending()
meth public boolean isHidingAllowed()
meth public boolean isSorted()
meth public boolean isSortingAllowed()
meth public int compareTo(org.netbeans.swing.etable.ETableColumn)
meth public int getSortRank()
meth public java.util.Comparator getNestedComparator()
meth public void readSettings(java.util.Properties,int,java.lang.String)
meth public void setAscending(boolean)
meth public void setCustomIcon(javax.swing.Icon)
meth public void setHeaderRenderer(javax.swing.table.TableCellRenderer)
meth public void setNestedComparator(java.util.Comparator)
meth public void setSortRank(int)
meth public void setSorted(int,boolean)
 anno 0 java.lang.Deprecated()
meth public void writeSettings(java.util.Properties,int,java.lang.String)
supr javax.swing.table.TableColumn
hfds PROP_ASCENDING,PROP_COMPARATOR,PROP_HEADER_VALUE,PROP_MODEL_INDEX,PROP_PREFERRED_WIDTH,PROP_PREFIX,PROP_SORT_RANK,PROP_WIDTH,ascending,comparator,customIcon,myHeaderRenderer,nestedComparator,sortRank,table
hcls ETableColumnHeaderRendererDelegate,FlippingComparator

CLSS public org.netbeans.swing.etable.ETableColumn$RowComparator
 outer org.netbeans.swing.etable.ETableColumn
cons public init(org.netbeans.swing.etable.ETableColumn,int)
fld protected int column
intf java.util.Comparator<org.netbeans.swing.etable.ETable$RowMapping>
meth public int compare(org.netbeans.swing.etable.ETable$RowMapping,org.netbeans.swing.etable.ETable$RowMapping)
supr java.lang.Object

CLSS public org.netbeans.swing.etable.ETableColumnModel
cons public init()
fld protected java.util.List<javax.swing.table.TableColumn> hiddenColumns
fld protected java.util.List<javax.swing.table.TableColumn> sortedColumns
meth public boolean isColumnHidden(javax.swing.table.TableColumn)
meth public java.util.Comparator<org.netbeans.swing.etable.ETable$RowMapping> getComparator()
meth public org.netbeans.swing.etable.TableColumnSelector$TreeNode getColumnHierarchyRoot()
meth public void clean()
meth public void clearSortedColumns()
meth public void moveColumn(int,int)
meth public void readSettings(java.util.Properties,java.lang.String,org.netbeans.swing.etable.ETable)
meth public void removeColumn(javax.swing.table.TableColumn)
meth public void setColumnHidden(javax.swing.table.TableColumn,boolean)
meth public void setColumnHierarchyRoot(org.netbeans.swing.etable.TableColumnSelector$TreeNode)
meth public void setColumnSorted(org.netbeans.swing.etable.ETableColumn,boolean,int)
meth public void writeSettings(java.util.Properties,java.lang.String)
supr javax.swing.table.DefaultTableColumnModel
hfds NUMBER_OF_COLUMNS,NUMBER_OF_HIDDEN_COLUMNS,PROP_HIDDEN_POSITION_PREFIX,PROP_HIDDEN_PREFIX,columnHierarchyRoot,hiddenColumnsPosition
hcls CompoundComparator

CLSS public org.netbeans.swing.etable.ETableTransferHandler
cons public init()
meth protected java.awt.datatransfer.Transferable createTransferable(javax.swing.JComponent)
meth public int getSourceActions(javax.swing.JComponent)
supr javax.swing.TransferHandler

CLSS public org.netbeans.swing.etable.ETableTransferable
cons public init(java.lang.String)
fld protected java.lang.String plainData
intf java.awt.datatransfer.Transferable
meth protected boolean isPlainFlavor(java.awt.datatransfer.DataFlavor)
meth protected boolean isPlainSupported()
meth protected boolean isStringFlavor(java.awt.datatransfer.DataFlavor)
meth protected java.lang.String getPlainData()
meth public boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
meth public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors()
meth public java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
supr java.lang.Object
hfds plainFlavors,stringFlavors

CLSS public abstract interface org.netbeans.swing.etable.QuickFilter
meth public abstract boolean accept(java.lang.Object)

CLSS public abstract org.netbeans.swing.etable.TableColumnSelector
cons public init()
innr public abstract interface static TreeNode
meth public abstract java.lang.String[] selectVisibleColumns(java.lang.String[],java.lang.String[])
meth public abstract java.lang.String[] selectVisibleColumns(org.netbeans.swing.etable.TableColumnSelector$TreeNode,java.lang.String[])
supr java.lang.Object

CLSS public abstract interface static org.netbeans.swing.etable.TableColumnSelector$TreeNode
 outer org.netbeans.swing.etable.TableColumnSelector
meth public abstract boolean isLeaf()
meth public abstract java.lang.String getText()
meth public abstract org.netbeans.swing.etable.TableColumnSelector$TreeNode[] getChildren()

CLSS public abstract interface org.netbeans.swing.outline.CheckRenderDataProvider
intf org.netbeans.swing.outline.RenderDataProvider
meth public abstract boolean isCheckEnabled(java.lang.Object)
meth public abstract boolean isCheckable(java.lang.Object)
meth public abstract java.lang.Boolean isSelected(java.lang.Object)
meth public abstract void setSelected(java.lang.Object,java.lang.Boolean)

CLSS public org.netbeans.swing.outline.DefaultOutlineCellRenderer
cons public init()
meth protected void setValue(java.lang.Object)
meth public final void setBorder(javax.swing.border.Border)
meth public java.awt.Component getTableCellRendererComponent(javax.swing.JTable,java.lang.Object,boolean,boolean,int,int)
meth public java.lang.String getToolTipText()
supr javax.swing.table.DefaultTableCellRenderer
hfds checkBox,expanded,expansionBorder,expansionHandleHeight,expansionHandleWidth,fakeCellRendererPane,htmlRenderer,htmlRendererClass,labelTextGap,lastRenderedValueRef,lastRendererRef,leaf,nestingDepth,showHandle,swingRendering,theCheckBox
hcls ExpansionHandleBorder,HtmlRenderer,RestrictedInsetsBorder

CLSS public org.netbeans.swing.outline.DefaultOutlineModel
cons protected init(javax.swing.tree.TreeModel,javax.swing.table.TableModel,boolean,java.lang.String)
cons protected init(javax.swing.tree.TreeModel,org.netbeans.swing.outline.RowModel,boolean,java.lang.String)
intf org.netbeans.swing.outline.OutlineModel
meth protected void setTreeValueAt(java.lang.Object,int)
meth public boolean areMoreEventsPending()
meth public boolean isCellEditable(int,int)
meth public boolean isLargeModel()
meth public final boolean isLeaf(java.lang.Object)
meth public final int getChildCount(java.lang.Object)
meth public final int getColumnCount()
meth public final int getIndexOfChild(java.lang.Object,java.lang.Object)
meth public final int getRowCount()
meth public final java.lang.Class getColumnClass(int)
meth public final java.lang.Object getChild(java.lang.Object,int)
meth public final java.lang.Object getRoot()
meth public final java.lang.Object getValueAt(int,int)
meth public final javax.swing.tree.AbstractLayoutCache getLayout()
meth public final org.netbeans.swing.outline.TreePathSupport getTreePathSupport()
meth public final void addTableModelListener(javax.swing.event.TableModelListener)
meth public final void addTreeModelListener(javax.swing.event.TreeModelListener)
meth public final void removeTableModelListener(javax.swing.event.TableModelListener)
meth public final void removeTreeModelListener(javax.swing.event.TreeModelListener)
meth public final void setValueAt(java.lang.Object,int,int)
meth public final void valueForPathChanged(javax.swing.tree.TreePath,java.lang.Object)
meth public java.lang.String getColumnName(int)
meth public static org.netbeans.swing.outline.OutlineModel createOutlineModel(javax.swing.tree.TreeModel,org.netbeans.swing.outline.RowModel)
meth public static org.netbeans.swing.outline.OutlineModel createOutlineModel(javax.swing.tree.TreeModel,org.netbeans.swing.outline.RowModel,boolean)
meth public static org.netbeans.swing.outline.OutlineModel createOutlineModel(javax.swing.tree.TreeModel,org.netbeans.swing.outline.RowModel,boolean,java.lang.String)
meth public void setNodesColumnLabel(java.lang.String)
supr java.lang.Object
hfds NODES_CHANGED,NODES_INSERTED,NODES_REMOVED,STRUCTURE_CHANGED,broadcaster,layout,nodesColumnLabel,tableModel,treeModel,treePathSupport

CLSS public abstract interface org.netbeans.swing.outline.ExtTreeWillExpandListener
intf javax.swing.event.TreeWillExpandListener
meth public abstract void treeExpansionVetoed(javax.swing.event.TreeExpansionEvent,javax.swing.tree.ExpandVetoException)

CLSS public org.netbeans.swing.outline.Outline
cons public init()
cons public init(org.netbeans.swing.outline.OutlineModel)
innr protected OutlineColumn
meth protected boolean processKeyBinding(javax.swing.KeyStroke,java.awt.event.KeyEvent,int,boolean)
meth protected final boolean checkAt(int,int,java.awt.event.MouseEvent)
meth protected javax.swing.table.TableColumn createColumn(int)
meth protected void configureTreeCellEditor(java.awt.Component,int,int)
meth protected void processMouseEvent(java.awt.event.MouseEvent)
meth protected void sortAndFilter()
meth public boolean editCellAt(int,int,java.util.EventObject)
meth public boolean isExpanded(javax.swing.tree.TreePath)
meth public boolean isRootVisible()
meth public boolean isVisible(javax.swing.tree.TreePath)
meth public final javax.swing.tree.AbstractLayoutCache getLayoutCache()
meth public java.awt.Rectangle getPathBounds(javax.swing.tree.TreePath)
meth public java.lang.String getToolTipText(java.awt.event.MouseEvent)
meth public javax.swing.JToolTip createToolTip()
meth public javax.swing.table.TableCellRenderer getCellRenderer(int,int)
meth public javax.swing.tree.TreePath getClosestPathForLocation(int,int)
meth public org.netbeans.swing.outline.OutlineModel getOutlineModel()
meth public org.netbeans.swing.outline.RenderDataProvider getRenderDataProvider()
meth public void addNotify()
meth public void changeSelection(int,int,boolean,boolean)
meth public void collapsePath(javax.swing.tree.TreePath)
meth public void expandPath(javax.swing.tree.TreePath)
meth public void setModel(javax.swing.table.TableModel)
meth public void setRenderDataProvider(org.netbeans.swing.outline.RenderDataProvider)
meth public void setRootVisible(boolean)
meth public void setRowHeight(int)
meth public void setRowHeight(int,int)
meth public void tableChanged(javax.swing.event.TableModelEvent)
supr org.netbeans.swing.etable.ETable
hfds MAX_TOOLTIP_LENGTH,cachedRootVisible,componentListener,initialized,lastEditPosition,lastProcessedKeyStroke,renderDataProvider,rowHeightIsSet,selectedRow,selectionDisabled,tempSortMap,tempSortMapLock,toolTip
hcls ExpandAction,ND,SizeManager,TreeCellEditorBorder

CLSS protected org.netbeans.swing.outline.Outline$OutlineColumn
 outer org.netbeans.swing.outline.Outline
cons public init(org.netbeans.swing.outline.Outline,int)
meth protected java.util.Comparator<org.netbeans.swing.etable.ETable$RowMapping> getRowComparator(int,boolean)
meth public boolean isHidingAllowed()
meth public boolean isSortingAllowed()
supr org.netbeans.swing.etable.ETableColumn
hcls OutlineRowComparator

CLSS public abstract interface org.netbeans.swing.outline.OutlineModel
intf javax.swing.table.TableModel
intf javax.swing.tree.TreeModel
meth public abstract boolean isLargeModel()
meth public abstract javax.swing.tree.AbstractLayoutCache getLayout()
meth public abstract org.netbeans.swing.outline.TreePathSupport getTreePathSupport()

CLSS public abstract interface org.netbeans.swing.outline.RenderDataProvider
meth public abstract boolean isHtmlDisplayName(java.lang.Object)
meth public abstract java.awt.Color getBackground(java.lang.Object)
meth public abstract java.awt.Color getForeground(java.lang.Object)
meth public abstract java.lang.String getDisplayName(java.lang.Object)
meth public abstract java.lang.String getTooltipText(java.lang.Object)
meth public abstract javax.swing.Icon getIcon(java.lang.Object)

CLSS public abstract interface org.netbeans.swing.outline.RowModel
meth public abstract boolean isCellEditable(java.lang.Object,int)
meth public abstract int getColumnCount()
meth public abstract java.lang.Class getColumnClass(int)
meth public abstract java.lang.Object getValueFor(java.lang.Object,int)
meth public abstract java.lang.String getColumnName(int)
meth public abstract void setValueFor(java.lang.Object,int,java.lang.Object)

CLSS public final org.netbeans.swing.outline.TreePathSupport
cons public init(org.netbeans.swing.outline.OutlineModel,javax.swing.tree.AbstractLayoutCache)
meth public boolean hasBeenExpanded(javax.swing.tree.TreePath)
meth public boolean isExpanded(javax.swing.tree.TreePath)
meth public boolean isVisible(javax.swing.tree.TreePath)
meth public javax.swing.tree.TreePath[] getExpandedDescendants(javax.swing.tree.TreePath)
meth public void addTreeExpansionListener(javax.swing.event.TreeExpansionListener)
meth public void addTreeWillExpandListener(javax.swing.event.TreeWillExpandListener)
meth public void clear()
meth public void collapsePath(javax.swing.tree.TreePath)
meth public void expandPath(javax.swing.tree.TreePath)
meth public void removePath(javax.swing.tree.TreePath)
meth public void removeTreeExpansionListener(javax.swing.event.TreeExpansionListener)
meth public void removeTreeWillExpandListener(javax.swing.event.TreeWillExpandListener)
supr java.lang.Object
hfds eListeners,layout,weListeners

