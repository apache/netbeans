#Signature file v4.1
#Version 1.76

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

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Cloneable

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

CLSS public final org.netbeans.modules.extbrowser.BrowserUtils
meth public static void notifyMissingBrowser(java.lang.String)
supr java.lang.Object
hfds BROWSER_ICON

CLSS public org.netbeans.modules.extbrowser.ChromeBrowser
cons public init()
intf java.beans.PropertyChangeListener
meth protected org.openide.execution.NbProcessDescriptor defaultBrowserExecutable()
meth public java.lang.String getName()
meth public org.openide.awt.HtmlBrowser$Impl createHtmlBrowserImpl()
meth public static java.lang.Boolean isHidden()
supr org.netbeans.modules.extbrowser.ExtWebBrowser
hfds serialVersionUID

CLSS public org.netbeans.modules.extbrowser.ChromeBrowserBeanInfo
cons public init()
meth public java.awt.Image getIcon(int)
meth public java.beans.BeanDescriptor getBeanDescriptor()
meth public java.beans.BeanInfo[] getAdditionalBeanInfo()
meth public java.beans.PropertyDescriptor[] getPropertyDescriptors()
supr java.beans.SimpleBeanInfo

CLSS public org.netbeans.modules.extbrowser.ChromiumBrowser
cons public init()
intf java.beans.PropertyChangeListener
meth protected org.openide.execution.NbProcessDescriptor defaultBrowserExecutable()
meth public java.lang.String getName()
meth public org.openide.awt.HtmlBrowser$Impl createHtmlBrowserImpl()
meth public static java.lang.Boolean isHidden()
supr org.netbeans.modules.extbrowser.ExtWebBrowser
hfds CHROMIUM_PATH,CHROMIUM_PATH2,serialVersionUID

CLSS public org.netbeans.modules.extbrowser.ChromiumBrowserBeanInfo
cons public init()
meth public java.awt.Image getIcon(int)
meth public java.beans.BeanDescriptor getBeanDescriptor()
meth public java.beans.BeanInfo[] getAdditionalBeanInfo()
meth public java.beans.PropertyDescriptor[] getPropertyDescriptors()
supr java.beans.SimpleBeanInfo

CLSS public org.netbeans.modules.extbrowser.DelegatingWebBrowserImpl
cons public init()
cons public init(org.netbeans.modules.extbrowser.ExtWebBrowser)
meth protected void loadURLInBrowserInternal(java.net.URL)
meth public org.netbeans.modules.extbrowser.ExtBrowserImpl getImplementation()
supr org.netbeans.modules.extbrowser.ExtBrowserImpl
hfds ddeImpl,simpleImpl,unixImpl

CLSS public abstract org.netbeans.modules.extbrowser.ExtBrowserImpl
cons public init()
fld protected java.beans.PropertyChangeSupport pcs
fld protected java.lang.String title
fld protected org.netbeans.modules.extbrowser.ExtWebBrowser extBrowserFactory
meth protected abstract void loadURLInBrowserInternal(java.net.URL)
meth protected final void loadURLInBrowser(java.net.URL)
meth protected org.netbeans.modules.extbrowser.PrivateBrowserFamilyId detectPrivateBrowserFamilyId()
meth protected void setTitle(java.lang.String)
meth public boolean isBackward()
meth public boolean isForward()
meth public boolean isHistory()
meth public final java.awt.Component getComponent()
meth public final org.openide.util.Lookup getLookup()
meth public java.lang.String getStatusMessage()
meth public java.lang.String getTitle()
meth public java.net.URL getURL()
meth public org.netbeans.modules.extbrowser.PrivateBrowserFamilyId getPrivateBrowserFamilyId()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void backward()
meth public void forward()
meth public void reloadDocument()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setURL(java.net.URL)
meth public void showHistory()
meth public void stopLoading()
supr org.openide.awt.HtmlBrowser$Impl
hfds RP,url

CLSS public org.netbeans.modules.extbrowser.ExtWebBrowser
cons public init()
cons public init(org.netbeans.modules.extbrowser.PrivateBrowserFamilyId)
fld protected final static int DEFAULT_ACTIVATE_TIMEOUT = 2000
fld protected final static int DEFAULT_OPENURL_TIMEOUT = 3000
fld protected int activateTimeout
fld protected int openurlTimeout
fld protected java.beans.PropertyChangeSupport pcs
fld protected java.lang.String ddeServer
fld protected java.lang.String name
fld protected org.openide.execution.NbProcessDescriptor browserExecutable
fld public final static java.lang.String CHROME = "CHROME"
fld public final static java.lang.String CHROMIUM = "CHROMIUM"
fld public final static java.lang.String FIREFOX = "FIREFOX"
fld public final static java.lang.String IEXPLORE = "IEXPLORE"
fld public final static java.lang.String MOZILLA = "MOZILLA"
fld public final static java.lang.String PROP_BROWSER_EXECUTABLE = "browserExecutable"
fld public final static java.lang.String PROP_DDESERVER = "dDEServer"
fld public final static java.lang.String PROP_DDE_ACTIVATE_TIMEOUT = "activateTimeout"
fld public final static java.lang.String PROP_DDE_OPENURL_TIMEOUT = "openurlTimeout"
fld public final static java.lang.String PROP_NAME = "name"
fld public final static java.lang.String PROP_PRIVATE_BROWSER_FAMILY = "privateBrowserFamilyId"
innr public static UnixBrowserFormat
intf java.beans.PropertyChangeListener
intf java.io.Serializable
intf org.openide.awt.HtmlBrowser$Factory
meth protected org.openide.execution.NbProcessDescriptor defaultBrowserExecutable()
meth public int getActivateTimeout()
meth public int getOpenurlTimeout()
meth public java.lang.String getDDEServer()
meth public java.lang.String getName()
meth public org.netbeans.modules.extbrowser.PrivateBrowserFamilyId getPrivateBrowserFamilyId()
meth public org.openide.awt.HtmlBrowser$Impl createHtmlBrowserImpl()
meth public org.openide.execution.NbProcessDescriptor getBrowserExecutable()
meth public static java.util.logging.Logger getEM()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setActivateTimeout(int)
meth public void setBrowserExecutable(org.openide.execution.NbProcessDescriptor)
meth public void setDDEServer(java.lang.String)
meth public void setName(java.lang.String)
meth public void setOpenurlTimeout(int)
meth public void useBrowserExecutableDelegate(org.netbeans.modules.extbrowser.ExtWebBrowser)
supr java.lang.Object
hfds browserExecutableDelegate,err,family,serialVersionUID

CLSS public static org.netbeans.modules.extbrowser.ExtWebBrowser$UnixBrowserFormat
 outer org.netbeans.modules.extbrowser.ExtWebBrowser
cons public init(java.lang.String)
fld public final static java.lang.String TAG_URL = "URL"
meth public static java.lang.String getHint()
supr org.openide.util.MapFormat
hfds serialVersionUID

CLSS public org.netbeans.modules.extbrowser.ExtWebBrowserBeanInfo
cons public init()
meth public java.awt.Image getIcon(int)
meth public java.beans.BeanDescriptor getBeanDescriptor()
meth public java.beans.PropertyDescriptor[] getPropertyDescriptors()
supr java.beans.SimpleBeanInfo

CLSS public org.netbeans.modules.extbrowser.FirefoxBrowser
cons public init()
meth protected org.openide.execution.NbProcessDescriptor defaultBrowserExecutable()
meth public java.lang.String getName()
meth public org.openide.awt.HtmlBrowser$Impl createHtmlBrowserImpl()
meth public static java.lang.Boolean isHidden()
supr org.netbeans.modules.extbrowser.ExtWebBrowser
hfds serialVersionUID

CLSS public org.netbeans.modules.extbrowser.FirefoxBrowserBeanInfo
cons public init()
meth public java.awt.Image getIcon(int)
meth public java.beans.BeanDescriptor getBeanDescriptor()
meth public java.beans.BeanInfo[] getAdditionalBeanInfo()
meth public java.beans.PropertyDescriptor[] getPropertyDescriptors()
supr java.beans.SimpleBeanInfo

CLSS public org.netbeans.modules.extbrowser.IExplorerBrowser
cons public init()
meth protected org.openide.execution.NbProcessDescriptor defaultBrowserExecutable()
meth public java.lang.String getName()
meth public org.openide.awt.HtmlBrowser$Impl createHtmlBrowserImpl()
meth public static java.lang.Boolean isHidden()
supr org.netbeans.modules.extbrowser.ExtWebBrowser
hfds serialVersionUID

CLSS public org.netbeans.modules.extbrowser.IExplorerBrowserBeanInfo
cons public init()
meth public java.awt.Image getIcon(int)
meth public java.beans.BeanDescriptor getBeanDescriptor()
meth public java.beans.BeanInfo[] getAdditionalBeanInfo()
meth public java.beans.PropertyDescriptor[] getPropertyDescriptors()
supr java.beans.SimpleBeanInfo

CLSS public org.netbeans.modules.extbrowser.MacBrowserImpl
cons public init(org.netbeans.modules.extbrowser.ExtWebBrowser)
meth protected org.netbeans.modules.extbrowser.PrivateBrowserFamilyId detectPrivateBrowserFamilyId()
meth protected void loadURLInBrowserInternal(java.net.URL)
supr org.netbeans.modules.extbrowser.ExtBrowserImpl

CLSS public org.netbeans.modules.extbrowser.MicrosoftEdgeBrowser
cons public init()
meth protected org.openide.execution.NbProcessDescriptor defaultBrowserExecutable()
meth public java.lang.String getName()
meth public org.openide.awt.HtmlBrowser$Impl createHtmlBrowserImpl()
meth public static java.lang.Boolean isHidden()
supr org.netbeans.modules.extbrowser.ExtWebBrowser
hfds appUserModelId,hidden,serialVersionUID

CLSS public org.netbeans.modules.extbrowser.MicrosoftEdgeBrowserBeanInfo
cons public init()
meth public java.awt.Image getIcon(int)
meth public java.beans.BeanDescriptor getBeanDescriptor()
meth public java.beans.BeanInfo[] getAdditionalBeanInfo()
meth public java.beans.PropertyDescriptor[] getPropertyDescriptors()
supr java.beans.SimpleBeanInfo

CLSS public org.netbeans.modules.extbrowser.MozillaBrowser
cons public init()
meth protected org.openide.execution.NbProcessDescriptor defaultBrowserExecutable()
meth public java.lang.String getName()
meth public org.openide.awt.HtmlBrowser$Impl createHtmlBrowserImpl()
meth public static java.lang.Boolean isHidden()
supr org.netbeans.modules.extbrowser.ExtWebBrowser
hfds serialVersionUID

CLSS public org.netbeans.modules.extbrowser.MozillaBrowserBeanInfo
cons public init()
meth public java.awt.Image getIcon(int)
meth public java.beans.BeanDescriptor getBeanDescriptor()
meth public java.beans.BeanInfo[] getAdditionalBeanInfo()
meth public java.beans.PropertyDescriptor[] getPropertyDescriptors()
supr java.beans.SimpleBeanInfo

CLSS public org.netbeans.modules.extbrowser.NbBrowserException
cons public init()
cons public init(java.lang.String)
supr java.lang.Exception
hfds serialVersionUID

CLSS public org.netbeans.modules.extbrowser.NbDdeBrowserImpl
cons public init(org.netbeans.modules.extbrowser.ExtWebBrowser)
meth protected org.netbeans.modules.extbrowser.PrivateBrowserFamilyId detectPrivateBrowserFamilyId()
meth protected void loadURLInBrowserInternal(java.net.URL)
meth public int getActivateTimeout()
meth public int getOpenUrlTimeout()
meth public static java.lang.String getBrowserPath(java.lang.String) throws org.netbeans.modules.extbrowser.NbBrowserException
meth public static java.lang.String getDefaultOpenCommand() throws org.netbeans.modules.extbrowser.NbBrowserException
supr org.netbeans.modules.extbrowser.ExtBrowserImpl
hfds EXTBROWSER_DLL,EXTBROWSER_DLL_64BIT,WWW_ACTIVATE,WWW_OPEN_URL,nativeRunnable,nativeThread
hcls DisplayTask,URLDisplayer

CLSS public final !enum org.netbeans.modules.extbrowser.PrivateBrowserFamilyId
fld public final static org.netbeans.modules.extbrowser.PrivateBrowserFamilyId CHROME
fld public final static org.netbeans.modules.extbrowser.PrivateBrowserFamilyId CHROMIUM
fld public final static org.netbeans.modules.extbrowser.PrivateBrowserFamilyId EDGE
fld public final static org.netbeans.modules.extbrowser.PrivateBrowserFamilyId FIREFOX
fld public final static org.netbeans.modules.extbrowser.PrivateBrowserFamilyId IE
fld public final static org.netbeans.modules.extbrowser.PrivateBrowserFamilyId MOZILLA
fld public final static org.netbeans.modules.extbrowser.PrivateBrowserFamilyId OPERA
fld public final static org.netbeans.modules.extbrowser.PrivateBrowserFamilyId SAFARI
fld public final static org.netbeans.modules.extbrowser.PrivateBrowserFamilyId UNKNOWN
meth public static org.netbeans.modules.extbrowser.PrivateBrowserFamilyId valueOf(java.lang.String)
meth public static org.netbeans.modules.extbrowser.PrivateBrowserFamilyId[] values()
supr java.lang.Enum<org.netbeans.modules.extbrowser.PrivateBrowserFamilyId>

CLSS public org.netbeans.modules.extbrowser.SafariBrowser
cons public init()
meth protected org.openide.execution.NbProcessDescriptor defaultBrowserExecutable()
meth public java.lang.String getName()
meth public org.openide.awt.HtmlBrowser$Impl createHtmlBrowserImpl()
meth public static java.lang.Boolean isHidden()
supr org.netbeans.modules.extbrowser.ExtWebBrowser
hfds serialVersionUID

CLSS public org.netbeans.modules.extbrowser.SafariBrowserBeanInfo
cons public init()
meth public java.awt.Image getIcon(int)
meth public java.beans.BeanDescriptor getBeanDescriptor()
meth public java.beans.BeanInfo[] getAdditionalBeanInfo()
meth public java.beans.PropertyDescriptor[] getPropertyDescriptors()
supr java.beans.SimpleBeanInfo

CLSS public org.netbeans.modules.extbrowser.SimpleExtBrowser
cons public init()
innr public static BrowserFormat
meth public java.lang.String getName()
meth public org.openide.awt.HtmlBrowser$Impl createHtmlBrowserImpl()
meth public static java.lang.Boolean isHidden()
supr org.netbeans.modules.extbrowser.ExtWebBrowser
hfds serialVersionUID

CLSS public static org.netbeans.modules.extbrowser.SimpleExtBrowser$BrowserFormat
 outer org.netbeans.modules.extbrowser.SimpleExtBrowser
cons public init(java.lang.String)
fld public final static java.lang.String TAG_URL = "URL"
supr org.openide.util.MapFormat
hfds serialVersionUID

CLSS public org.netbeans.modules.extbrowser.SimpleExtBrowserBeanInfo
cons public init()
meth public java.awt.Image getIcon(int)
meth public java.beans.BeanDescriptor getBeanDescriptor()
meth public java.beans.PropertyDescriptor[] getPropertyDescriptors()
supr java.beans.SimpleBeanInfo

CLSS public org.netbeans.modules.extbrowser.SimpleExtBrowserImpl
cons public init(org.netbeans.modules.extbrowser.ExtWebBrowser)
meth protected void loadURLInBrowserInternal(java.net.URL)
supr org.netbeans.modules.extbrowser.ExtBrowserImpl

CLSS public org.netbeans.modules.extbrowser.SystemDefaultBrowser
cons public init()
meth protected org.openide.execution.NbProcessDescriptor defaultBrowserExecutable()
meth public java.lang.String getName()
meth public org.netbeans.modules.extbrowser.PrivateBrowserFamilyId getPrivateBrowserFamilyId()
meth public org.openide.awt.HtmlBrowser$Impl createHtmlBrowserImpl()
meth public static java.lang.Boolean isHidden()
meth public void setName(java.lang.String)
supr org.netbeans.modules.extbrowser.ExtWebBrowser
hfds RP,USE_JDK_BROWSER,detected,logger,serialVersionUID
hcls JdkBrowserImpl

CLSS public org.netbeans.modules.extbrowser.SystemDefaultBrowserBeanInfo
cons public init()
meth public java.awt.Image getIcon(int)
meth public java.beans.BeanDescriptor getBeanDescriptor()
meth public java.beans.PropertyDescriptor[] getPropertyDescriptors()
supr java.beans.SimpleBeanInfo

CLSS public org.netbeans.modules.extbrowser.URLUtil
cons public init()
meth public static boolean browserHandlesJarURLs(java.lang.String)
meth public static java.net.URL createExternalURL(java.net.URL,boolean)
supr java.lang.Object
hfds result

CLSS public org.netbeans.modules.extbrowser.UnixBrowserImpl
cons public init()
cons public init(org.netbeans.modules.extbrowser.ExtWebBrowser)
fld protected final static int CMD_TIMEOUT = 6
meth protected static org.openide.execution.NbProcessDescriptor createPatchedExecutable(org.openide.execution.NbProcessDescriptor)
meth protected void loadURLInBrowserInternal(java.net.URL)
supr org.netbeans.modules.extbrowser.ExtBrowserImpl
hfds RP
hcls Status

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

CLSS public abstract interface static org.openide.awt.HtmlBrowser$Factory
 outer org.openide.awt.HtmlBrowser
meth public abstract org.openide.awt.HtmlBrowser$Impl createHtmlBrowserImpl()

CLSS public abstract static org.openide.awt.HtmlBrowser$Impl
 outer org.openide.awt.HtmlBrowser
cons public init()
fld public final static java.lang.String PROP_BACKWARD = "backward"
fld public final static java.lang.String PROP_BROWSER_WAS_CLOSED = "browser.was.closed"
fld public final static java.lang.String PROP_FORWARD = "forward"
fld public final static java.lang.String PROP_HISTORY = "history"
fld public final static java.lang.String PROP_LOADING = "loading"
fld public final static java.lang.String PROP_STATUS_MESSAGE = "statusMessage"
fld public final static java.lang.String PROP_TITLE = "title"
fld public final static java.lang.String PROP_URL = "url"
meth public abstract boolean isBackward()
meth public abstract boolean isForward()
meth public abstract boolean isHistory()
meth public abstract java.awt.Component getComponent()
meth public abstract java.lang.String getStatusMessage()
meth public abstract java.lang.String getTitle()
meth public abstract java.net.URL getURL()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void backward()
meth public abstract void forward()
meth public abstract void reloadDocument()
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setURL(java.net.URL)
meth public abstract void showHistory()
meth public abstract void stopLoading()
meth public java.lang.String getLocation()
meth public org.openide.util.Lookup getLookup()
meth public void dispose()
meth public void setLocation(java.lang.String)
supr java.lang.Object
hfds serialVersionUID

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

