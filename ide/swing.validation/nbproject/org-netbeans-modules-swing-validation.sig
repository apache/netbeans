#Signature file v4.1
#Version 1.55

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

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable

CLSS public java.lang.IllegalArgumentException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.RuntimeException

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

CLSS public final org.netbeans.api.validation.adapters.DialogBuilder
cons public init(java.lang.Class<?>)
cons public init(java.lang.String)
innr public final static !enum ButtonSet
innr public final static !enum DialogType
meth public !varargs org.netbeans.api.validation.adapters.DialogBuilder setClosingOptions(java.lang.Object[])
meth public !varargs org.netbeans.api.validation.adapters.DialogBuilder setOptions(java.lang.Object[])
meth public boolean showDialog(java.lang.Object)
meth public java.lang.Object showDialog()
meth public org.netbeans.api.validation.adapters.DialogBuilder setActionListener(java.awt.event.ActionListener)
meth public org.netbeans.api.validation.adapters.DialogBuilder setAdditionalButtons(java.lang.Object[])
meth public org.netbeans.api.validation.adapters.DialogBuilder setButtonSet(org.netbeans.api.validation.adapters.DialogBuilder$ButtonSet)
meth public org.netbeans.api.validation.adapters.DialogBuilder setContent(java.lang.Object)
meth public org.netbeans.api.validation.adapters.DialogBuilder setDefaultButton(java.lang.Object)
meth public org.netbeans.api.validation.adapters.DialogBuilder setDialogType(org.netbeans.api.validation.adapters.DialogBuilder$DialogType)
meth public org.netbeans.api.validation.adapters.DialogBuilder setHelpContext(org.openide.util.HelpCtx)
meth public org.netbeans.api.validation.adapters.DialogBuilder setModal(boolean)
meth public org.netbeans.api.validation.adapters.DialogBuilder setTitle(java.lang.String)
meth public org.netbeans.api.validation.adapters.DialogBuilder setValidationGroup(org.netbeans.validation.api.ui.swing.SwingValidationGroup)
supr java.lang.Object
hfds additionalOptions,al,closingOptions,ctx,defaultOption,dialogType,message,modal,optionType,options,title,token,validationGroup

CLSS public final static !enum org.netbeans.api.validation.adapters.DialogBuilder$ButtonSet
 outer org.netbeans.api.validation.adapters.DialogBuilder
fld public final static org.netbeans.api.validation.adapters.DialogBuilder$ButtonSet CLOSE
fld public final static org.netbeans.api.validation.adapters.DialogBuilder$ButtonSet OK_CANCEL
fld public final static org.netbeans.api.validation.adapters.DialogBuilder$ButtonSet YES_NO
fld public final static org.netbeans.api.validation.adapters.DialogBuilder$ButtonSet YES_NO_CANCEL
meth public static org.netbeans.api.validation.adapters.DialogBuilder$ButtonSet valueOf(java.lang.String)
meth public static org.netbeans.api.validation.adapters.DialogBuilder$ButtonSet[] values()
supr java.lang.Enum<org.netbeans.api.validation.adapters.DialogBuilder$ButtonSet>

CLSS public final static !enum org.netbeans.api.validation.adapters.DialogBuilder$DialogType
 outer org.netbeans.api.validation.adapters.DialogBuilder
fld public final static org.netbeans.api.validation.adapters.DialogBuilder$DialogType ERROR
fld public final static org.netbeans.api.validation.adapters.DialogBuilder$DialogType INFO
fld public final static org.netbeans.api.validation.adapters.DialogBuilder$DialogType PLAIN
fld public final static org.netbeans.api.validation.adapters.DialogBuilder$DialogType QUESTION
fld public final static org.netbeans.api.validation.adapters.DialogBuilder$DialogType WARNING
meth public static org.netbeans.api.validation.adapters.DialogBuilder$DialogType valueOf(java.lang.String)
meth public static org.netbeans.api.validation.adapters.DialogBuilder$DialogType[] values()
supr java.lang.Enum<org.netbeans.api.validation.adapters.DialogBuilder$DialogType>

CLSS public final org.netbeans.api.validation.adapters.DialogDescriptorAdapter
cons public init(org.openide.DialogDescriptor)
intf org.netbeans.validation.api.ui.ValidationUI
meth public void clearProblem()
meth public void showProblem(org.netbeans.validation.api.Problem)
supr java.lang.Object
hfds d

CLSS public final org.netbeans.api.validation.adapters.NotificationLineSupportAdapter
cons public init(org.openide.NotificationLineSupport)
intf org.netbeans.validation.api.ui.ValidationUI
meth public void clearProblem()
meth public void showProblem(org.netbeans.validation.api.Problem)
supr java.lang.Object
hfds nls

CLSS public org.netbeans.api.validation.adapters.WizardDescriptorAdapter
cons public init(org.openide.WizardDescriptor)
cons public init(org.openide.WizardDescriptor,org.netbeans.api.validation.adapters.WizardDescriptorAdapter$Type)
innr public final static !enum Type
intf org.netbeans.validation.api.ui.ValidationUI
meth public void clearProblem()
meth public void showProblem(org.netbeans.validation.api.Problem)
supr java.lang.Object
hfds type,wiz

CLSS public final static !enum org.netbeans.api.validation.adapters.WizardDescriptorAdapter$Type
 outer org.netbeans.api.validation.adapters.WizardDescriptorAdapter
fld public final static org.netbeans.api.validation.adapters.WizardDescriptorAdapter$Type MESSAGE
fld public final static org.netbeans.api.validation.adapters.WizardDescriptorAdapter$Type VALID
fld public final static org.netbeans.api.validation.adapters.WizardDescriptorAdapter$Type VALID_AND_MESSAGE
meth public static org.netbeans.api.validation.adapters.WizardDescriptorAdapter$Type valueOf(java.lang.String)
meth public static org.netbeans.api.validation.adapters.WizardDescriptorAdapter$Type[] values()
supr java.lang.Enum<org.netbeans.api.validation.adapters.WizardDescriptorAdapter$Type>

CLSS public abstract org.netbeans.validation.api.AbstractValidator<%0 extends java.lang.Object>
cons protected init(java.lang.Class<{org.netbeans.validation.api.AbstractValidator%0}>)
intf org.netbeans.validation.api.Validator<{org.netbeans.validation.api.AbstractValidator%0}>
meth public final java.lang.Class<{org.netbeans.validation.api.AbstractValidator%0}> modelType()
supr java.lang.Object
hfds type

CLSS public org.netbeans.validation.api.InvalidInputException
cons public init(java.lang.String,org.netbeans.validation.api.Problems)
cons public init(java.lang.String,org.netbeans.validation.api.Validating)
cons public init(org.netbeans.validation.api.Problems)
cons public init(org.netbeans.validation.api.Validating)
intf java.lang.Iterable<org.netbeans.validation.api.Problem>
meth public java.lang.String getMessage()
meth public java.util.Iterator<org.netbeans.validation.api.Problem> iterator()
meth public org.netbeans.validation.api.Problems getProblems()
meth public static void throwIfNecessary(java.lang.String,org.netbeans.validation.api.Validating)
meth public static void throwIfNecessary(org.netbeans.validation.api.Validating)
supr java.lang.IllegalArgumentException
hfds problems

CLSS public final org.netbeans.validation.api.Problem
cons public init(java.lang.String,org.netbeans.validation.api.Severity)
intf java.lang.Comparable<org.netbeans.validation.api.Problem>
meth public boolean equals(java.lang.Object)
meth public boolean isFatal()
meth public int compareTo(org.netbeans.validation.api.Problem)
meth public int hashCode()
meth public java.lang.String getMessage()
meth public java.lang.String toString()
meth public org.netbeans.validation.api.Severity severity()
meth public static org.netbeans.validation.api.Problem worst(org.netbeans.validation.api.Problem,org.netbeans.validation.api.Problem)
supr java.lang.Object
hfds message,severity

CLSS public final org.netbeans.validation.api.Problems
cons public init()
intf java.lang.Iterable<org.netbeans.validation.api.Problem>
meth public final boolean hasFatal()
meth public final java.util.List<? extends org.netbeans.validation.api.Problem> allProblems()
meth public final org.netbeans.validation.api.Problem getLeadProblem()
meth public final org.netbeans.validation.api.Problems addAll(org.netbeans.validation.api.Problems)
meth public final org.netbeans.validation.api.Problems append(java.lang.String)
meth public final org.netbeans.validation.api.Problems append(java.lang.String,org.netbeans.validation.api.Severity)
meth public final org.netbeans.validation.api.Problems append(org.netbeans.validation.api.Problem)
meth public final void add(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public final void add(java.lang.String,org.netbeans.validation.api.Severity)
 anno 0 java.lang.Deprecated()
meth public final void add(org.netbeans.validation.api.Problem)
 anno 0 java.lang.Deprecated()
meth public final void putAll(org.netbeans.validation.api.Problems)
 anno 0 java.lang.Deprecated()
meth public java.lang.String toString()
meth public java.util.Iterator<org.netbeans.validation.api.Problem> iterator()
meth public static org.netbeans.validation.api.Problems create(java.lang.Class<?>,java.lang.String)
meth public static org.netbeans.validation.api.Problems create(java.lang.String)
meth public void throwIfFatalPresent()
meth public void throwIfFatalPresent(java.lang.String)
supr java.lang.Object
hfds hasFatal,problems

CLSS public final !enum org.netbeans.validation.api.Severity
fld public final static org.netbeans.validation.api.Severity FATAL
fld public final static org.netbeans.validation.api.Severity INFO
fld public final static org.netbeans.validation.api.Severity WARNING
meth public java.awt.Color color()
meth public java.awt.image.BufferedImage badge()
meth public java.awt.image.BufferedImage image()
meth public java.lang.String describeError(java.lang.String)
meth public java.lang.String toString()
meth public javax.swing.Icon icon()
meth public static org.netbeans.validation.api.Severity valueOf(java.lang.String)
meth public static org.netbeans.validation.api.Severity[] values()
supr java.lang.Enum<org.netbeans.validation.api.Severity>
hfds badge,image

CLSS public abstract interface org.netbeans.validation.api.Validating
meth public abstract boolean isValid()
meth public abstract org.netbeans.validation.api.Problems getProblems()

CLSS public abstract interface org.netbeans.validation.api.Validator<%0 extends java.lang.Object>
meth public abstract java.lang.Class<{org.netbeans.validation.api.Validator%0}> modelType()
meth public abstract void validate(org.netbeans.validation.api.Problems,java.lang.String,{org.netbeans.validation.api.Validator%0})

CLSS public org.netbeans.validation.api.ValidatorUtils
cons public init()
meth public !varargs static <%0 extends java.lang.Object> org.netbeans.validation.api.Validator<{%%0}> limitSeverity(org.netbeans.validation.api.Severity,org.netbeans.validation.api.Validator<{%%0}>[])
 anno 0 java.lang.SafeVarargs()
meth public !varargs static <%0 extends java.lang.Object> org.netbeans.validation.api.Validator<{%%0}> merge(org.netbeans.validation.api.Validator<{%%0}>[])
 anno 0 java.lang.SafeVarargs()
meth public final <%0 extends java.lang.Object, %1 extends java.lang.Object> org.netbeans.validation.api.Validator<{%%0}> as(java.lang.Class<{%%0}>,org.netbeans.validation.api.Validator<{%%1}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.netbeans.validation.api.Validator<{%%0}> cast(java.lang.Class<{%%0}>,org.netbeans.validation.api.Validator<{%%1}>)
meth public static <%0 extends java.lang.Object> org.netbeans.validation.api.Validator<{%%0}> allowNull(org.netbeans.validation.api.Validator<{%%0}>)
meth public static <%0 extends java.lang.Object> org.netbeans.validation.api.Validator<{%%0}> limitSeverity(org.netbeans.validation.api.Severity,org.netbeans.validation.api.Validator<{%%0}>)
meth public static <%0 extends java.lang.Object> org.netbeans.validation.api.Validator<{%%0}> merge(org.netbeans.validation.api.Validator<{%%0}>,org.netbeans.validation.api.Validator<{%%0}>)
supr java.lang.Object
hcls AllowNullValidator,CastValidator

CLSS public final !enum org.netbeans.validation.api.builtin.indexvalidation.IndexValidators
fld public final static org.netbeans.validation.api.builtin.indexvalidation.IndexValidators REQUIRE_SELECTION
intf org.netbeans.validation.api.Validator<java.lang.Integer[]>
meth public java.lang.Class<java.lang.Integer[]> modelType()
meth public static org.netbeans.validation.api.builtin.indexvalidation.IndexValidators valueOf(java.lang.String)
meth public static org.netbeans.validation.api.builtin.indexvalidation.IndexValidators[] values()
meth public void validate(org.netbeans.validation.api.Problems,java.lang.String,java.lang.Integer[])
supr java.lang.Enum<org.netbeans.validation.api.builtin.indexvalidation.IndexValidators>

CLSS public org.netbeans.validation.api.builtin.stringvalidation.BoundValidator
intf org.netbeans.validation.api.Validator<java.lang.String>
meth public java.lang.Class<java.lang.String> modelType()
meth public void validate(org.netbeans.validation.api.Problems,java.lang.String,java.lang.String)
supr java.lang.Object
hfds less,value

CLSS public final !enum org.netbeans.validation.api.builtin.stringvalidation.StringValidators
fld public final static org.netbeans.validation.api.builtin.stringvalidation.StringValidators CHARACTER_SET_NAME
fld public final static org.netbeans.validation.api.builtin.stringvalidation.StringValidators EMAIL_ADDRESS
fld public final static org.netbeans.validation.api.builtin.stringvalidation.StringValidators FILE_MUST_BE_DIRECTORY
fld public final static org.netbeans.validation.api.builtin.stringvalidation.StringValidators FILE_MUST_BE_FILE
fld public final static org.netbeans.validation.api.builtin.stringvalidation.StringValidators FILE_MUST_EXIST
fld public final static org.netbeans.validation.api.builtin.stringvalidation.StringValidators FILE_MUST_NOT_EXIST
fld public final static org.netbeans.validation.api.builtin.stringvalidation.StringValidators HOST_NAME
fld public final static org.netbeans.validation.api.builtin.stringvalidation.StringValidators HOST_NAME_OR_IP_ADDRESS
fld public final static org.netbeans.validation.api.builtin.stringvalidation.StringValidators IP_ADDRESS
fld public final static org.netbeans.validation.api.builtin.stringvalidation.StringValidators JAVA_PACKAGE_NAME
fld public final static org.netbeans.validation.api.builtin.stringvalidation.StringValidators MAY_NOT_END_WITH_PERIOD
fld public final static org.netbeans.validation.api.builtin.stringvalidation.StringValidators MAY_NOT_START_WITH_DIGIT
fld public final static org.netbeans.validation.api.builtin.stringvalidation.StringValidators NO_WHITESPACE
fld public final static org.netbeans.validation.api.builtin.stringvalidation.StringValidators REQUIRE_JAVA_IDENTIFIER
fld public final static org.netbeans.validation.api.builtin.stringvalidation.StringValidators REQUIRE_NON_EMPTY_STRING
fld public final static org.netbeans.validation.api.builtin.stringvalidation.StringValidators REQUIRE_NON_NEGATIVE_NUMBER
fld public final static org.netbeans.validation.api.builtin.stringvalidation.StringValidators REQUIRE_VALID_FILENAME
fld public final static org.netbeans.validation.api.builtin.stringvalidation.StringValidators REQUIRE_VALID_INTEGER
fld public final static org.netbeans.validation.api.builtin.stringvalidation.StringValidators REQUIRE_VALID_NUMBER
fld public final static org.netbeans.validation.api.builtin.stringvalidation.StringValidators URL_MUST_BE_VALID
fld public final static org.netbeans.validation.api.builtin.stringvalidation.StringValidators VALID_HEXADECIMAL_NUMBER
intf org.netbeans.validation.api.Validator<java.lang.String>
meth public !varargs static org.netbeans.validation.api.Validator<java.lang.String> splitString(java.lang.String,org.netbeans.validation.api.Validator<java.lang.String>[])
 anno 0 java.lang.SafeVarargs()
meth public !varargs static org.netbeans.validation.api.Validator<java.lang.String> trimString(org.netbeans.validation.api.Validator<java.lang.String>[])
 anno 0 java.lang.SafeVarargs()
meth public java.lang.Class<java.lang.String> modelType()
meth public org.netbeans.validation.api.Validator<java.lang.String> trim()
meth public static org.netbeans.validation.api.Validator<java.lang.String> between(int,int)
meth public static org.netbeans.validation.api.Validator<java.lang.String> disallowChars(char[])
meth public static org.netbeans.validation.api.Validator<java.lang.String> encodableInCharset(java.lang.String)
meth public static org.netbeans.validation.api.Validator<java.lang.String> forFormat(java.text.Format)
meth public static org.netbeans.validation.api.Validator<java.lang.String> greaterThan(int)
meth public static org.netbeans.validation.api.Validator<java.lang.String> lessThan(int)
meth public static org.netbeans.validation.api.Validator<java.lang.String> maxLength(int)
meth public static org.netbeans.validation.api.Validator<java.lang.String> mayNotEndWith(char)
meth public static org.netbeans.validation.api.Validator<java.lang.String> minLength(int)
meth public static org.netbeans.validation.api.Validator<java.lang.String> numberRange(java.lang.Number,java.lang.Number)
meth public static org.netbeans.validation.api.Validator<java.lang.String> regexp(java.lang.String,java.lang.String,boolean)
meth public static org.netbeans.validation.api.Validator<java.lang.String> splitString(java.lang.String,org.netbeans.validation.api.Validator<java.lang.String>)
meth public static org.netbeans.validation.api.Validator<java.lang.String> trimString(org.netbeans.validation.api.Validator<java.lang.String>)
meth public static org.netbeans.validation.api.Validator<java.lang.String> validNumber(java.util.Locale)
meth public static org.netbeans.validation.api.builtin.stringvalidation.StringValidators valueOf(java.lang.String)
meth public static org.netbeans.validation.api.builtin.stringvalidation.StringValidators[] values()
meth public void validate(org.netbeans.validation.api.Problems,java.lang.String,java.lang.String)
supr java.lang.Enum<org.netbeans.validation.api.builtin.stringvalidation.StringValidators>

CLSS public org.netbeans.validation.api.builtin.stringvalidation.ValidatorTypes
fld public static java.lang.Class<? extends org.netbeans.validation.api.Validator> HOST_NAME
fld public static java.lang.Class<? extends org.netbeans.validation.api.Validator> HOST_NAME_OR_IP_ADDRESS
fld public static java.lang.Class<? extends org.netbeans.validation.api.Validator> IP_ADDRESS
fld public static java.lang.Class<? extends org.netbeans.validation.api.Validator> MAY_NOT_START_WITH_DIGIT
fld public static java.lang.Class<? extends org.netbeans.validation.api.Validator> NO_WHITESPACE
fld public static java.lang.Class<? extends org.netbeans.validation.api.Validator> URL
supr java.lang.Object

CLSS public abstract org.netbeans.validation.api.conversion.Converter<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons protected init(java.lang.Class<{org.netbeans.validation.api.conversion.Converter%0}>,java.lang.Class<{org.netbeans.validation.api.conversion.Converter%1}>)
meth public !varargs final org.netbeans.validation.api.Validator<{org.netbeans.validation.api.conversion.Converter%1}> convert(org.netbeans.validation.api.Validator<{org.netbeans.validation.api.conversion.Converter%0}>[])
 anno 0 java.lang.SafeVarargs()
meth public abstract org.netbeans.validation.api.Validator<{org.netbeans.validation.api.conversion.Converter%1}> convert(org.netbeans.validation.api.Validator<{org.netbeans.validation.api.conversion.Converter%0}>)
meth public boolean equals(java.lang.Object)
meth public final java.lang.Class<{org.netbeans.validation.api.conversion.Converter%0}> from()
meth public final java.lang.Class<{org.netbeans.validation.api.conversion.Converter%1}> to()
meth public int hashCode()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.netbeans.validation.api.conversion.Converter<{%%0},{%%1}> find(java.lang.Class<{%%0}>,java.lang.Class<{%%1}>)
meth public static void register(org.netbeans.validation.api.conversion.Converter<?,?>)
supr java.lang.Object
hfds from,registry,to
hcls Wrap

CLSS public abstract org.netbeans.validation.api.ui.GroupValidator
cons protected init()
cons protected init(boolean)
meth protected abstract void performGroupValidation(org.netbeans.validation.api.Problems)
supr java.lang.Object
hfds isCurrentlyLeadingProblem,shallShowProblemInChildrenUIs

CLSS public org.netbeans.validation.api.ui.ValidationGroup
cons protected !varargs init(org.netbeans.validation.api.ui.GroupValidator,org.netbeans.validation.api.ui.ValidationUI[])
cons protected !varargs init(org.netbeans.validation.api.ui.ValidationUI[])
meth protected <%0 extends java.lang.Object> org.netbeans.validation.api.ui.ValidationUI decorationFor({%%0})
meth public !varargs final <%0 extends java.lang.Object, %1 extends java.lang.Object> void add({%%0},org.netbeans.validation.api.Validator<{%%1}>[])
 anno 0 java.lang.SafeVarargs()
meth public !varargs static org.netbeans.validation.api.ui.ValidationGroup create(org.netbeans.validation.api.ui.GroupValidator,org.netbeans.validation.api.ui.ValidationUI[])
meth public !varargs static org.netbeans.validation.api.ui.ValidationGroup create(org.netbeans.validation.api.ui.ValidationUI[])
meth public final <%0 extends java.lang.Object, %1 extends java.lang.Object> void add({%%0},org.netbeans.validation.api.Validator<{%%1}>)
meth public final void addItem(org.netbeans.validation.api.ui.ValidationItem,boolean)
meth public final void addUI(org.netbeans.validation.api.ui.ValidationUI)
meth public final void remove(org.netbeans.validation.api.ui.ValidationItem)
meth public final void removeUI(org.netbeans.validation.api.ui.ValidationUI)
supr org.netbeans.validation.api.ui.ValidationItem
hfds additionalGroupValidation,isAncestorToSelf,validationItems

CLSS public abstract interface org.netbeans.validation.api.ui.ValidationGroupProvider
meth public abstract org.netbeans.validation.api.ui.ValidationGroup getValidationGroup()

CLSS public abstract org.netbeans.validation.api.ui.ValidationItem
meth public final org.netbeans.validation.api.Problem performValidation()
meth public final void runWithValidationSuspended(java.lang.Runnable)
supr java.lang.Object
hfds currentLeadProblem,currentProblemInUI,multicastValidationUI,parentValidationGroup,suspendCount,uiEnabled

CLSS public abstract org.netbeans.validation.api.ui.ValidationListener<%0 extends java.lang.Object>
cons protected init(java.lang.Class<{org.netbeans.validation.api.ui.ValidationListener%0}>,org.netbeans.validation.api.ui.ValidationUI,{org.netbeans.validation.api.ui.ValidationListener%0})
intf java.util.EventListener
meth protected abstract void performValidation(org.netbeans.validation.api.Problems)
meth protected final {org.netbeans.validation.api.ui.ValidationListener%0} getTarget()
supr org.netbeans.validation.api.ui.ValidationItem
hfds target,targetType

CLSS public abstract org.netbeans.validation.api.ui.ValidationListenerFactory<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons protected init(java.lang.Class<{org.netbeans.validation.api.ui.ValidationListenerFactory%0}>,java.lang.Class<{org.netbeans.validation.api.ui.ValidationListenerFactory%1}>)
meth protected abstract org.netbeans.validation.api.ui.ValidationListener<{org.netbeans.validation.api.ui.ValidationListenerFactory%0}> createListener({org.netbeans.validation.api.ui.ValidationListenerFactory%0},org.netbeans.validation.api.ui.ValidationStrategy,org.netbeans.validation.api.ui.ValidationUI,org.netbeans.validation.api.Validator<{org.netbeans.validation.api.ui.ValidationListenerFactory%1}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.netbeans.validation.api.ui.ValidationListener<{%%0}> createValidationListener({%%0},org.netbeans.validation.api.ui.ValidationStrategy,org.netbeans.validation.api.ui.ValidationUI,org.netbeans.validation.api.Validator<{%%1}>)
supr java.lang.Object
hfds componentType,modelType
hcls Cast

CLSS public final !enum org.netbeans.validation.api.ui.ValidationStrategy
fld public final static org.netbeans.validation.api.ui.ValidationStrategy DEFAULT
fld public final static org.netbeans.validation.api.ui.ValidationStrategy INPUT_VERIFIER
fld public final static org.netbeans.validation.api.ui.ValidationStrategy ON_CHANGE_OR_ACTION
fld public final static org.netbeans.validation.api.ui.ValidationStrategy ON_FOCUS_LOSS
meth public static org.netbeans.validation.api.ui.ValidationStrategy valueOf(java.lang.String)
meth public static org.netbeans.validation.api.ui.ValidationStrategy[] values()
supr java.lang.Enum<org.netbeans.validation.api.ui.ValidationStrategy>

CLSS public abstract interface org.netbeans.validation.api.ui.ValidationUI
fld public final static org.netbeans.validation.api.ui.ValidationUI NO_OP
meth public abstract void clearProblem()
meth public abstract void showProblem(org.netbeans.validation.api.Problem)

CLSS public abstract org.netbeans.validation.api.ui.swing.AbstractValidationListener<%0 extends javax.swing.JComponent, %1 extends java.lang.Object>
cons public init(java.lang.Class<{org.netbeans.validation.api.ui.swing.AbstractValidationListener%0}>,{org.netbeans.validation.api.ui.swing.AbstractValidationListener%0},org.netbeans.validation.api.ui.ValidationUI,org.netbeans.validation.api.Validator<{org.netbeans.validation.api.ui.swing.AbstractValidationListener%1}>)
meth protected abstract {org.netbeans.validation.api.ui.swing.AbstractValidationListener%1} getModelObject({org.netbeans.validation.api.ui.swing.AbstractValidationListener%0})
meth protected final void performValidation(org.netbeans.validation.api.Problems)
meth protected java.lang.String findComponentName({org.netbeans.validation.api.ui.swing.AbstractValidationListener%0})
meth protected void onValidate({org.netbeans.validation.api.ui.swing.AbstractValidationListener%0},org.netbeans.validation.api.Problem)
supr org.netbeans.validation.api.ui.ValidationListener<{org.netbeans.validation.api.ui.swing.AbstractValidationListener%0}>
hfds validator

CLSS public abstract org.netbeans.validation.api.ui.swing.SwingComponentDecorationFactory
cons public init()
meth public abstract org.netbeans.validation.api.ui.ValidationUI decorationFor(javax.swing.JComponent)
meth public final static org.netbeans.validation.api.ui.swing.SwingComponentDecorationFactory getDefault()
meth public final static org.netbeans.validation.api.ui.swing.SwingComponentDecorationFactory getNoOpDecorationFactory()
supr java.lang.Object
hfds componentDecorator,noOpDecorationFactory

CLSS public final org.netbeans.validation.api.ui.swing.SwingValidationGroup
meth protected final <%0 extends java.lang.Object> org.netbeans.validation.api.ui.ValidationUI decorationFor({%%0})
meth public !varargs final void add(javax.swing.AbstractButton[],org.netbeans.validation.api.Validator<java.lang.Integer[]>[])
meth public !varargs final void add(javax.swing.JComboBox,org.netbeans.validation.api.Validator<java.lang.String>[])
meth public !varargs final void add(javax.swing.JList,org.netbeans.validation.api.Validator<java.lang.Integer[]>[])
meth public !varargs final void add(javax.swing.text.JTextComponent,org.netbeans.validation.api.Validator<java.lang.String>[])
 anno 0 java.lang.SafeVarargs()
meth public !varargs static org.netbeans.validation.api.ui.swing.SwingValidationGroup create(org.netbeans.validation.api.ui.GroupValidator,org.netbeans.validation.api.ui.ValidationUI[])
meth public !varargs static org.netbeans.validation.api.ui.swing.SwingValidationGroup create(org.netbeans.validation.api.ui.GroupValidator,org.netbeans.validation.api.ui.swing.SwingComponentDecorationFactory,org.netbeans.validation.api.ui.ValidationUI[])
meth public !varargs static org.netbeans.validation.api.ui.swing.SwingValidationGroup create(org.netbeans.validation.api.ui.ValidationUI[])
meth public final javax.swing.JComponent createProblemLabel()
meth public final void add(javax.swing.AbstractButton[],org.netbeans.validation.api.Validator<java.lang.Integer[]>)
meth public final void add(javax.swing.JComboBox,org.netbeans.validation.api.Validator<java.lang.String>)
meth public final void add(javax.swing.JList,org.netbeans.validation.api.Validator<java.lang.Integer[]>)
meth public final void add(javax.swing.text.JTextComponent,org.netbeans.validation.api.Validator<java.lang.String>)
meth public static java.lang.String nameForComponent(javax.swing.JComponent)
meth public static void setComponentName(javax.swing.JComponent,java.lang.String)
supr org.netbeans.validation.api.ui.ValidationGroup
hfds CLIENT_PROP_NAME,decorator

CLSS public final org.netbeans.validation.api.ui.swing.ValidationPanel
cons public init()
cons public init(org.netbeans.validation.api.ui.swing.SwingValidationGroup)
fld protected final org.netbeans.validation.api.ui.swing.SwingValidationGroup group
intf org.netbeans.validation.api.ui.ValidationGroupProvider
meth protected void addImpl(java.awt.Component,java.lang.Object,int)
meth public boolean showOkCancelDialog(java.lang.String)
meth public final boolean isFatalProblem()
meth public final org.netbeans.validation.api.Problem getProblem()
meth public final org.netbeans.validation.api.ui.ValidationGroup getValidationGroup()
meth public final void addChangeListener(javax.swing.event.ChangeListener)
meth public final void removeChangeListener(javax.swing.event.ChangeListener)
meth public final void setInnerComponent(java.awt.Component)
meth public final void setLayout(java.awt.LayoutManager)
meth public void removeDelegateValidationUI(org.netbeans.validation.api.ui.ValidationUI)
meth public void setDelegateValidationUI(org.netbeans.validation.api.ui.ValidationUI)
supr javax.swing.JPanel
hfds initialized,listeners,problem,problemLabel,vui
hcls VUI

CLSS public abstract org.netbeans.validation.spi.accessibility.GlobalValidationCallback
cons public init()
meth public abstract void onProblem(java.lang.Object,org.netbeans.validation.api.Problem)
meth public abstract void onProblemCleared(java.lang.Object,org.netbeans.validation.api.Problem)
meth public void onValidationFinished(java.lang.Object,java.lang.Object)
meth public void onValidationTrigger(java.lang.Object,java.lang.Object)
supr java.lang.Object
hfds INSTANCE,LOCK
hcls ProxyGlobalCallback

