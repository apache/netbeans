#Signature file v4.1
#Version 3.50

CLSS public abstract java.awt.AWTEvent
cons public init(java.awt.Event)
cons public init(java.lang.Object,int)
fld protected boolean consumed
fld protected int id
fld public final static int RESERVED_ID_MAX = 1999
fld public final static long ACTION_EVENT_MASK = 128
fld public final static long ADJUSTMENT_EVENT_MASK = 256
fld public final static long COMPONENT_EVENT_MASK = 1
fld public final static long CONTAINER_EVENT_MASK = 2
fld public final static long FOCUS_EVENT_MASK = 4
fld public final static long HIERARCHY_BOUNDS_EVENT_MASK = 65536
fld public final static long HIERARCHY_EVENT_MASK = 32768
fld public final static long INPUT_METHOD_EVENT_MASK = 2048
fld public final static long INVOCATION_EVENT_MASK = 16384
fld public final static long ITEM_EVENT_MASK = 512
fld public final static long KEY_EVENT_MASK = 8
fld public final static long MOUSE_EVENT_MASK = 16
fld public final static long MOUSE_MOTION_EVENT_MASK = 32
fld public final static long MOUSE_WHEEL_EVENT_MASK = 131072
fld public final static long PAINT_EVENT_MASK = 8192
fld public final static long TEXT_EVENT_MASK = 1024
fld public final static long WINDOW_EVENT_MASK = 64
fld public final static long WINDOW_FOCUS_EVENT_MASK = 524288
fld public final static long WINDOW_STATE_EVENT_MASK = 262144
meth protected boolean isConsumed()
meth protected void consume()
meth public int getID()
meth public java.lang.String paramString()
meth public java.lang.String toString()
meth public void setSource(java.lang.Object)
supr java.util.EventObject

CLSS public abstract interface java.awt.ActiveEvent
meth public abstract void dispatch()

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

CLSS public java.awt.Dialog
cons public init(java.awt.Dialog)
cons public init(java.awt.Dialog,java.lang.String)
cons public init(java.awt.Dialog,java.lang.String,boolean)
cons public init(java.awt.Dialog,java.lang.String,boolean,java.awt.GraphicsConfiguration)
cons public init(java.awt.Frame)
cons public init(java.awt.Frame,boolean)
cons public init(java.awt.Frame,java.lang.String)
cons public init(java.awt.Frame,java.lang.String,boolean)
cons public init(java.awt.Frame,java.lang.String,boolean,java.awt.GraphicsConfiguration)
cons public init(java.awt.Window)
cons public init(java.awt.Window,java.awt.Dialog$ModalityType)
cons public init(java.awt.Window,java.lang.String)
cons public init(java.awt.Window,java.lang.String,java.awt.Dialog$ModalityType)
cons public init(java.awt.Window,java.lang.String,java.awt.Dialog$ModalityType,java.awt.GraphicsConfiguration)
fld public final static java.awt.Dialog$ModalityType DEFAULT_MODALITY_TYPE
innr protected AccessibleAWTDialog
innr public final static !enum ModalExclusionType
innr public final static !enum ModalityType
meth protected java.lang.String paramString()
meth public boolean isModal()
meth public boolean isResizable()
meth public boolean isUndecorated()
meth public java.awt.Dialog$ModalityType getModalityType()
meth public java.lang.String getTitle()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public void addNotify()
meth public void hide()
 anno 0 java.lang.Deprecated()
meth public void setBackground(java.awt.Color)
meth public void setModal(boolean)
meth public void setModalityType(java.awt.Dialog$ModalityType)
meth public void setOpacity(float)
meth public void setResizable(boolean)
meth public void setShape(java.awt.Shape)
meth public void setTitle(java.lang.String)
meth public void setUndecorated(boolean)
meth public void setVisible(boolean)
meth public void show()
 anno 0 java.lang.Deprecated()
meth public void toBack()
supr java.awt.Window

CLSS public java.awt.Frame
cons public init()
cons public init(java.awt.GraphicsConfiguration)
cons public init(java.lang.String)
cons public init(java.lang.String,java.awt.GraphicsConfiguration)
fld public final static int CROSSHAIR_CURSOR = 1
 anno 0 java.lang.Deprecated()
fld public final static int DEFAULT_CURSOR = 0
 anno 0 java.lang.Deprecated()
fld public final static int E_RESIZE_CURSOR = 11
 anno 0 java.lang.Deprecated()
fld public final static int HAND_CURSOR = 12
 anno 0 java.lang.Deprecated()
fld public final static int ICONIFIED = 1
fld public final static int MAXIMIZED_BOTH = 6
fld public final static int MAXIMIZED_HORIZ = 2
fld public final static int MAXIMIZED_VERT = 4
fld public final static int MOVE_CURSOR = 13
 anno 0 java.lang.Deprecated()
fld public final static int NE_RESIZE_CURSOR = 7
 anno 0 java.lang.Deprecated()
fld public final static int NORMAL = 0
fld public final static int NW_RESIZE_CURSOR = 6
 anno 0 java.lang.Deprecated()
fld public final static int N_RESIZE_CURSOR = 8
 anno 0 java.lang.Deprecated()
fld public final static int SE_RESIZE_CURSOR = 5
 anno 0 java.lang.Deprecated()
fld public final static int SW_RESIZE_CURSOR = 4
 anno 0 java.lang.Deprecated()
fld public final static int S_RESIZE_CURSOR = 9
 anno 0 java.lang.Deprecated()
fld public final static int TEXT_CURSOR = 2
 anno 0 java.lang.Deprecated()
fld public final static int WAIT_CURSOR = 3
 anno 0 java.lang.Deprecated()
fld public final static int W_RESIZE_CURSOR = 10
 anno 0 java.lang.Deprecated()
innr protected AccessibleAWTFrame
intf java.awt.MenuContainer
meth protected java.lang.String paramString()
meth public boolean isResizable()
meth public boolean isUndecorated()
meth public int getCursorType()
 anno 0 java.lang.Deprecated()
meth public int getExtendedState()
meth public int getState()
meth public java.awt.Image getIconImage()
meth public java.awt.MenuBar getMenuBar()
meth public java.awt.Rectangle getMaximizedBounds()
meth public java.lang.String getTitle()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public static java.awt.Frame[] getFrames()
meth public void addNotify()
meth public void remove(java.awt.MenuComponent)
meth public void removeNotify()
meth public void setBackground(java.awt.Color)
meth public void setCursor(int)
 anno 0 java.lang.Deprecated()
meth public void setExtendedState(int)
meth public void setIconImage(java.awt.Image)
meth public void setMaximizedBounds(java.awt.Rectangle)
meth public void setMenuBar(java.awt.MenuBar)
meth public void setOpacity(float)
meth public void setResizable(boolean)
meth public void setShape(java.awt.Shape)
meth public void setState(int)
meth public void setTitle(java.lang.String)
meth public void setUndecorated(boolean)
supr java.awt.Window

CLSS public abstract interface java.awt.MenuContainer
meth public abstract boolean postEvent(java.awt.Event)
 anno 0 java.lang.Deprecated()
meth public abstract java.awt.Font getFont()
meth public abstract void remove(java.awt.MenuComponent)

CLSS public java.awt.Window
cons public init(java.awt.Frame)
cons public init(java.awt.Window)
cons public init(java.awt.Window,java.awt.GraphicsConfiguration)
innr protected AccessibleAWTWindow
innr public final static !enum Type
intf javax.accessibility.Accessible
meth protected void processEvent(java.awt.AWTEvent)
meth protected void processWindowEvent(java.awt.event.WindowEvent)
meth protected void processWindowFocusEvent(java.awt.event.WindowEvent)
meth protected void processWindowStateEvent(java.awt.event.WindowEvent)
meth public <%0 extends java.util.EventListener> {%%0}[] getListeners(java.lang.Class<{%%0}>)
meth public boolean getFocusableWindowState()
meth public boolean isActive()
meth public boolean isAlwaysOnTopSupported()
meth public boolean isAutoRequestFocus()
meth public boolean isFocused()
meth public boolean isLocationByPlatform()
meth public boolean isOpaque()
meth public boolean isShowing()
meth public boolean isValidateRoot()
meth public boolean postEvent(java.awt.Event)
 anno 0 java.lang.Deprecated()
meth public final boolean isAlwaysOnTop()
meth public final boolean isFocusCycleRoot()
meth public final boolean isFocusableWindow()
meth public final java.awt.Container getFocusCycleRootAncestor()
meth public final java.lang.String getWarningString()
meth public final void setAlwaysOnTop(boolean)
meth public final void setFocusCycleRoot(boolean)
meth public float getOpacity()
meth public java.awt.Color getBackground()
meth public java.awt.Component getFocusOwner()
meth public java.awt.Component getMostRecentFocusOwner()
meth public java.awt.Dialog$ModalExclusionType getModalExclusionType()
meth public java.awt.Shape getShape()
meth public java.awt.Toolkit getToolkit()
meth public java.awt.Window getOwner()
meth public java.awt.Window$Type getType()
meth public java.awt.Window[] getOwnedWindows()
meth public java.awt.event.WindowFocusListener[] getWindowFocusListeners()
meth public java.awt.event.WindowListener[] getWindowListeners()
meth public java.awt.event.WindowStateListener[] getWindowStateListeners()
meth public java.awt.im.InputContext getInputContext()
meth public java.awt.image.BufferStrategy getBufferStrategy()
meth public java.util.List<java.awt.Image> getIconImages()
meth public java.util.Locale getLocale()
meth public java.util.Set<java.awt.AWTKeyStroke> getFocusTraversalKeys(int)
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public static java.awt.Window[] getOwnerlessWindows()
meth public static java.awt.Window[] getWindows()
meth public void addNotify()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void addWindowFocusListener(java.awt.event.WindowFocusListener)
meth public void addWindowListener(java.awt.event.WindowListener)
meth public void addWindowStateListener(java.awt.event.WindowStateListener)
meth public void applyResourceBundle(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void applyResourceBundle(java.util.ResourceBundle)
 anno 0 java.lang.Deprecated()
meth public void createBufferStrategy(int)
meth public void createBufferStrategy(int,java.awt.BufferCapabilities) throws java.awt.AWTException
meth public void dispose()
meth public void hide()
 anno 0 java.lang.Deprecated()
meth public void pack()
meth public void paint(java.awt.Graphics)
meth public void removeNotify()
meth public void removeWindowFocusListener(java.awt.event.WindowFocusListener)
meth public void removeWindowListener(java.awt.event.WindowListener)
meth public void removeWindowStateListener(java.awt.event.WindowStateListener)
meth public void reshape(int,int,int,int)
 anno 0 java.lang.Deprecated()
meth public void setAutoRequestFocus(boolean)
meth public void setBackground(java.awt.Color)
meth public void setBounds(int,int,int,int)
meth public void setBounds(java.awt.Rectangle)
meth public void setCursor(java.awt.Cursor)
meth public void setFocusableWindowState(boolean)
meth public void setIconImage(java.awt.Image)
meth public void setIconImages(java.util.List<? extends java.awt.Image>)
meth public void setLocation(int,int)
meth public void setLocation(java.awt.Point)
meth public void setLocationByPlatform(boolean)
meth public void setLocationRelativeTo(java.awt.Component)
meth public void setMinimumSize(java.awt.Dimension)
meth public void setModalExclusionType(java.awt.Dialog$ModalExclusionType)
meth public void setOpacity(float)
meth public void setShape(java.awt.Shape)
meth public void setSize(int,int)
meth public void setSize(java.awt.Dimension)
meth public void setType(java.awt.Window$Type)
meth public void setVisible(boolean)
meth public void show()
 anno 0 java.lang.Deprecated()
meth public void toBack()
meth public void toFront()
supr java.awt.Container

CLSS public java.awt.event.InvocationEvent
cons protected init(java.lang.Object,int,java.lang.Runnable,java.lang.Object,boolean)
cons public init(java.lang.Object,java.lang.Runnable)
cons public init(java.lang.Object,java.lang.Runnable,java.lang.Object,boolean)
cons public init(java.lang.Object,java.lang.Runnable,java.lang.Runnable,boolean)
fld protected boolean catchExceptions
fld protected java.lang.Runnable runnable
fld protected volatile java.lang.Object notifier
fld public final static int INVOCATION_DEFAULT = 1200
fld public final static int INVOCATION_FIRST = 1200
fld public final static int INVOCATION_LAST = 1200
intf java.awt.ActiveEvent
meth public boolean isDispatched()
meth public java.lang.Exception getException()
meth public java.lang.String paramString()
meth public java.lang.Throwable getThrowable()
meth public long getWhen()
meth public void dispatch()
supr java.awt.AWTEvent

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

CLSS public abstract interface java.lang.Cloneable

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

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public abstract interface javax.accessibility.Accessible
meth public abstract javax.accessibility.AccessibleContext getAccessibleContext()

CLSS public javax.swing.JDialog
cons public init()
cons public init(java.awt.Dialog)
cons public init(java.awt.Dialog,boolean)
cons public init(java.awt.Dialog,java.lang.String)
cons public init(java.awt.Dialog,java.lang.String,boolean)
cons public init(java.awt.Dialog,java.lang.String,boolean,java.awt.GraphicsConfiguration)
cons public init(java.awt.Frame)
cons public init(java.awt.Frame,boolean)
cons public init(java.awt.Frame,java.lang.String)
cons public init(java.awt.Frame,java.lang.String,boolean)
cons public init(java.awt.Frame,java.lang.String,boolean,java.awt.GraphicsConfiguration)
cons public init(java.awt.Window)
cons public init(java.awt.Window,java.awt.Dialog$ModalityType)
cons public init(java.awt.Window,java.lang.String)
cons public init(java.awt.Window,java.lang.String,java.awt.Dialog$ModalityType)
cons public init(java.awt.Window,java.lang.String,java.awt.Dialog$ModalityType,java.awt.GraphicsConfiguration)
fld protected boolean rootPaneCheckingEnabled
fld protected javax.accessibility.AccessibleContext accessibleContext
fld protected javax.swing.JRootPane rootPane
innr protected AccessibleJDialog
intf javax.accessibility.Accessible
intf javax.swing.RootPaneContainer
intf javax.swing.WindowConstants
meth protected boolean isRootPaneCheckingEnabled()
meth protected java.lang.String paramString()
meth protected javax.swing.JRootPane createRootPane()
meth protected void addImpl(java.awt.Component,java.lang.Object,int)
meth protected void dialogInit()
meth protected void processWindowEvent(java.awt.event.WindowEvent)
meth protected void setRootPane(javax.swing.JRootPane)
meth protected void setRootPaneCheckingEnabled(boolean)
meth public int getDefaultCloseOperation()
meth public java.awt.Component getGlassPane()
meth public java.awt.Container getContentPane()
meth public java.awt.Graphics getGraphics()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.JLayeredPane getLayeredPane()
meth public javax.swing.JMenuBar getJMenuBar()
meth public javax.swing.JRootPane getRootPane()
meth public javax.swing.TransferHandler getTransferHandler()
meth public static boolean isDefaultLookAndFeelDecorated()
meth public static void setDefaultLookAndFeelDecorated(boolean)
meth public void remove(java.awt.Component)
meth public void repaint(long,int,int,int,int)
meth public void setContentPane(java.awt.Container)
meth public void setDefaultCloseOperation(int)
meth public void setGlassPane(java.awt.Component)
meth public void setJMenuBar(javax.swing.JMenuBar)
meth public void setLayeredPane(javax.swing.JLayeredPane)
meth public void setLayout(java.awt.LayoutManager)
meth public void setTransferHandler(javax.swing.TransferHandler)
meth public void update(java.awt.Graphics)
supr java.awt.Dialog

CLSS public javax.swing.JFrame
cons public init()
cons public init(java.awt.GraphicsConfiguration)
cons public init(java.lang.String)
cons public init(java.lang.String,java.awt.GraphicsConfiguration)
fld protected boolean rootPaneCheckingEnabled
fld protected javax.accessibility.AccessibleContext accessibleContext
fld protected javax.swing.JRootPane rootPane
fld public final static int EXIT_ON_CLOSE = 3
innr protected AccessibleJFrame
intf javax.accessibility.Accessible
intf javax.swing.RootPaneContainer
intf javax.swing.WindowConstants
meth protected boolean isRootPaneCheckingEnabled()
meth protected java.lang.String paramString()
meth protected javax.swing.JRootPane createRootPane()
meth protected void addImpl(java.awt.Component,java.lang.Object,int)
meth protected void frameInit()
meth protected void processWindowEvent(java.awt.event.WindowEvent)
meth protected void setRootPane(javax.swing.JRootPane)
meth protected void setRootPaneCheckingEnabled(boolean)
meth public int getDefaultCloseOperation()
meth public java.awt.Component getGlassPane()
meth public java.awt.Container getContentPane()
meth public java.awt.Graphics getGraphics()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.JLayeredPane getLayeredPane()
meth public javax.swing.JMenuBar getJMenuBar()
meth public javax.swing.JRootPane getRootPane()
meth public javax.swing.TransferHandler getTransferHandler()
meth public static boolean isDefaultLookAndFeelDecorated()
meth public static void setDefaultLookAndFeelDecorated(boolean)
meth public void remove(java.awt.Component)
meth public void repaint(long,int,int,int,int)
meth public void setContentPane(java.awt.Container)
meth public void setDefaultCloseOperation(int)
meth public void setGlassPane(java.awt.Component)
meth public void setIconImage(java.awt.Image)
meth public void setJMenuBar(javax.swing.JMenuBar)
meth public void setLayeredPane(javax.swing.JLayeredPane)
meth public void setLayout(java.awt.LayoutManager)
meth public void setTransferHandler(javax.swing.TransferHandler)
meth public void update(java.awt.Graphics)
supr java.awt.Frame

CLSS public abstract interface javax.swing.RootPaneContainer
meth public abstract java.awt.Component getGlassPane()
meth public abstract java.awt.Container getContentPane()
meth public abstract javax.swing.JLayeredPane getLayeredPane()
meth public abstract javax.swing.JRootPane getRootPane()
meth public abstract void setContentPane(java.awt.Container)
meth public abstract void setGlassPane(java.awt.Component)
meth public abstract void setLayeredPane(javax.swing.JLayeredPane)

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

CLSS public abstract interface javax.swing.WindowConstants
fld public final static int DISPOSE_ON_CLOSE = 2
fld public final static int DO_NOTHING_ON_CLOSE = 0
fld public final static int EXIT_ON_CLOSE = 3
fld public final static int HIDE_ON_CLOSE = 1

CLSS public abstract interface org.netbeans.jemmy.Action
meth public abstract java.lang.Object launch(java.lang.Object)
meth public abstract java.lang.String getDescription()

CLSS public org.netbeans.jemmy.ActionProducer
cons protected init()
cons protected init(boolean)
cons public init(org.netbeans.jemmy.Action)
cons public init(org.netbeans.jemmy.Action,boolean)
intf org.netbeans.jemmy.Action
intf org.netbeans.jemmy.Timeoutable
intf org.netbeans.jemmy.Waitable
meth public boolean getFinished()
meth public final java.lang.Object actionProduced(java.lang.Object)
meth public final void run()
meth public java.lang.Object getResult()
meth public java.lang.Object launch(java.lang.Object)
meth public java.lang.Object produceAction(java.lang.Object,java.lang.String) throws java.lang.InterruptedException
meth public java.lang.String getDescription()
meth public java.lang.Throwable getException()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public void setActionPriority(int)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
supr java.lang.Thread
hfds ACTION_TIMEOUT,action,exception,finished,needWait,output,parameter,result,timeouts,waiter

CLSS public org.netbeans.jemmy.Bundle
cons public init()
meth public java.lang.String getResource(java.lang.String)
meth public void load(java.io.InputStream) throws java.io.IOException
meth public void loadFromFile(java.lang.String) throws java.io.IOException
meth public void loadFromJar(java.lang.String,java.lang.String) throws java.io.IOException
meth public void loadFromZip(java.lang.String,java.lang.String) throws java.io.IOException
meth public void print(java.io.PrintStream)
meth public void print(java.io.PrintWriter)
supr java.lang.Object
hfds resources

CLSS public org.netbeans.jemmy.BundleManager
cons public init()
meth public int calculateResources(java.lang.String)
meth public java.lang.String getResource(java.lang.String)
meth public java.lang.String getResource(java.lang.String,java.lang.String)
meth public org.netbeans.jemmy.Bundle addBundle(org.netbeans.jemmy.Bundle,java.lang.String)
meth public org.netbeans.jemmy.Bundle getBundle(java.lang.String)
meth public org.netbeans.jemmy.Bundle load() throws java.io.IOException
meth public org.netbeans.jemmy.Bundle loadBundleFromFile(java.lang.String,java.lang.String) throws java.io.IOException
meth public org.netbeans.jemmy.Bundle loadBundleFromJar(java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
meth public org.netbeans.jemmy.Bundle loadBundleFromResource(java.lang.ClassLoader,java.lang.String,java.lang.String) throws java.io.IOException
meth public org.netbeans.jemmy.Bundle loadBundleFromStream(java.io.InputStream,java.lang.String) throws java.io.IOException
meth public org.netbeans.jemmy.Bundle loadBundleFromZip(java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
meth public org.netbeans.jemmy.Bundle removeBundle(java.lang.String)
meth public org.netbeans.jemmy.BundleManager cloneThis()
meth public void print(java.io.PrintStream)
meth public void print(java.io.PrintWriter)
supr java.lang.Object
hfds bundles

CLSS public abstract interface org.netbeans.jemmy.CharBindingMap
meth public abstract int getCharKey(char)
meth public abstract int getCharModifiers(char)

CLSS public org.netbeans.jemmy.ClassReference
cons public init(java.lang.Object)
cons public init(java.lang.String) throws java.lang.ClassNotFoundException
meth public java.lang.Class[] getClasses()
meth public java.lang.Object getField(java.lang.String) throws java.lang.IllegalAccessException,java.lang.NoSuchFieldException
meth public java.lang.Object invokeMethod(java.lang.String,java.lang.Object[],java.lang.Class[]) throws java.lang.IllegalAccessException,java.lang.NoSuchMethodException,java.lang.reflect.InvocationTargetException
meth public java.lang.Object newInstance(java.lang.Object[],java.lang.Class[]) throws java.lang.IllegalAccessException,java.lang.InstantiationException,java.lang.NoSuchMethodException,java.lang.reflect.InvocationTargetException
meth public void setField(java.lang.String,java.lang.Object) throws java.lang.IllegalAccessException,java.lang.NoSuchFieldException
meth public void startApplication() throws java.lang.NoSuchMethodException,java.lang.reflect.InvocationTargetException
meth public void startApplication(java.lang.String[]) throws java.lang.NoSuchMethodException,java.lang.reflect.InvocationTargetException
supr java.lang.Object
hfds cl,instance

CLSS public abstract interface org.netbeans.jemmy.ComponentChooser
meth public abstract boolean checkComponent(java.awt.Component)
meth public abstract java.lang.String getDescription()

CLSS public org.netbeans.jemmy.ComponentIsNotFocusedException
cons public init(java.awt.Component)
supr org.netbeans.jemmy.JemmyInputException

CLSS public org.netbeans.jemmy.ComponentIsNotVisibleException
cons public init(java.awt.Component)
supr org.netbeans.jemmy.JemmyInputException

CLSS public org.netbeans.jemmy.ComponentSearcher
cons public init(java.awt.Container)
intf org.netbeans.jemmy.Outputable
meth public java.awt.Component findComponent(org.netbeans.jemmy.ComponentChooser)
meth public java.awt.Component findComponent(org.netbeans.jemmy.ComponentChooser,int)
meth public java.awt.Component[] findComponents(org.netbeans.jemmy.ComponentChooser)
meth public org.netbeans.jemmy.TestOut getOutput()
meth public static org.netbeans.jemmy.ComponentChooser getTrueChooser(java.lang.String)
meth public void setOutput(org.netbeans.jemmy.TestOut)
supr java.lang.Object
hfds container,containerToString,ordinalIndex,out,queueTool

CLSS public org.netbeans.jemmy.DefaultCharBindingMap
cons public init()
intf org.netbeans.jemmy.CharBindingMap
meth public char[] getSupportedChars()
meth public int getCharKey(char)
meth public int getCharModifiers(char)
meth public int[] getKeyAndModifiers(char)
meth public void addChar(char,int,int)
meth public void removeChar(char)
supr java.lang.Object
hfds chars
hcls CharKey

CLSS public org.netbeans.jemmy.DialogWaiter
cons public init()
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth protected java.lang.String getActionProducedMessage(long,java.lang.Object)
meth protected java.lang.String getGoldenActionProducedMessage()
meth protected java.lang.String getGoldenTimeoutExpiredMessage()
meth protected java.lang.String getGoldenWaitingStartedMessage()
meth protected java.lang.String getTimeoutExpiredMessage(long)
meth protected java.lang.String getWaitingStartedMessage()
meth public java.awt.Dialog waitDialog(java.awt.Window,java.lang.String,boolean,boolean) throws java.lang.InterruptedException
meth public java.awt.Dialog waitDialog(java.awt.Window,java.lang.String,boolean,boolean,int) throws java.lang.InterruptedException
meth public java.awt.Dialog waitDialog(java.awt.Window,org.netbeans.jemmy.ComponentChooser) throws java.lang.InterruptedException
meth public java.awt.Dialog waitDialog(java.awt.Window,org.netbeans.jemmy.ComponentChooser,int) throws java.lang.InterruptedException
meth public java.awt.Dialog waitDialog(java.lang.String,boolean,boolean) throws java.lang.InterruptedException
meth public java.awt.Dialog waitDialog(java.lang.String,boolean,boolean,int) throws java.lang.InterruptedException
meth public java.awt.Dialog waitDialog(org.netbeans.jemmy.ComponentChooser) throws java.lang.InterruptedException
meth public java.awt.Dialog waitDialog(org.netbeans.jemmy.ComponentChooser,int) throws java.lang.InterruptedException
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public static java.awt.Dialog getDialog(java.awt.Window,java.lang.String,boolean,boolean)
meth public static java.awt.Dialog getDialog(java.awt.Window,java.lang.String,boolean,boolean,int)
meth public static java.awt.Dialog getDialog(java.awt.Window,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.Dialog getDialog(java.awt.Window,org.netbeans.jemmy.ComponentChooser,int)
meth public static java.awt.Dialog getDialog(java.lang.String,boolean,boolean)
meth public static java.awt.Dialog getDialog(java.lang.String,boolean,boolean,int)
meth public static java.awt.Dialog getDialog(org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.Dialog getDialog(org.netbeans.jemmy.ComponentChooser,int)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
supr org.netbeans.jemmy.WindowWaiter
hfds AFTER_WAIT_TIME,WAIT_TIME,output,timeouts
hcls DialogByTitleChooser,DialogSubChooser

CLSS public org.netbeans.jemmy.EventDispatcher
cons public init(java.awt.Component)
fld protected java.awt.Component component
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth protected int getAbsoluteX(int)
meth protected int getAbsoluteY(int)
meth public int getDispatchingModel()
meth public java.lang.Object getExistingField(java.lang.String)
meth public java.lang.Object getExistingField(java.lang.String,org.netbeans.jemmy.TestOut)
meth public java.lang.Object getField(java.lang.String) throws java.lang.IllegalAccessException,java.lang.NoSuchFieldException,java.lang.reflect.InvocationTargetException
meth public java.lang.Object invokeExistingMethod(java.lang.String,java.lang.Object[],java.lang.Class[])
meth public java.lang.Object invokeExistingMethod(java.lang.String,java.lang.Object[],java.lang.Class[],org.netbeans.jemmy.TestOut)
meth public java.lang.Object invokeMethod(java.lang.String,java.lang.Object[],java.lang.Class[]) throws java.lang.IllegalAccessException,java.lang.NoSuchMethodException,java.lang.reflect.InvocationTargetException
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public static java.lang.String getKeyDescription(int)
meth public static java.lang.String getModifiersString(int)
meth public static java.lang.String getMouseButtonDescription(int)
meth public static void performInit()
meth public static void waitQueueEmpty()
meth public static void waitQueueEmpty(long)
meth public static void waitQueueEmpty(long,org.netbeans.jemmy.TestOut,org.netbeans.jemmy.Timeouts)
meth public static void waitQueueEmpty(org.netbeans.jemmy.TestOut,org.netbeans.jemmy.Timeouts)
meth public void checkComponentUnderMouse(boolean)
meth public void delayRobot(long)
meth public void dispatchEvent(java.awt.AWTEvent)
meth public void dispatchKeyEvent(int,int,int)
meth public void dispatchKeyEvent(int,int,int,char)
meth public void dispatchMouseEvent(int,int,int,boolean)
meth public void dispatchMouseEvent(int,int,int,int,int,boolean)
meth public void dispatchWindowEvent(int)
meth public void robotMoveMouse(int,int)
meth public void robotPressKey(int)
meth public void robotPressKey(int,int)
meth public void robotPressMouse(int)
meth public void robotPressMouse(int,int)
meth public void robotReleaseKey(int)
meth public void robotReleaseKey(int,int)
meth public void robotReleaseMouse(int)
meth public void robotReleaseMouse(int,int)
meth public void setDispatchingModel(int)
meth public void setExistingField(java.lang.String,java.lang.Object)
meth public void setExistingField(java.lang.String,java.lang.Object,org.netbeans.jemmy.TestOut)
meth public void setField(java.lang.String,java.lang.Object) throws java.lang.IllegalAccessException,java.lang.NoSuchFieldException,java.lang.reflect.InvocationTargetException
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
meth public void waitForIdle()
supr java.lang.Object
hfds ROBOT_AUTO_DELAY,WAIT_COMPONENT_UNDER_MOUSE_TIMEOUT,WAIT_QUEUE_EMPTY_TIMEOUT,keyFields,model,motionListener,output,outsider,queueTool,reference,robotReference,timeouts
hcls Dispatcher,Getter,Invoker,MotionListener,Setter

CLSS public org.netbeans.jemmy.EventTool
cons public init()
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth public boolean checkNoEvent(long)
meth public boolean checkNoEvent(long,long)
meth public java.awt.AWTEvent waitEvent()
meth public java.awt.AWTEvent waitEvent(long)
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public static java.awt.AWTEvent getLastEvent()
meth public static java.awt.AWTEvent getLastEvent(long)
meth public static long getCurrentEventMask()
meth public static long getLastEventTime()
meth public static long getLastEventTime(long)
meth public static long getTheWholeEventMask()
meth public static void addListeners()
meth public static void addListeners(long)
meth public static void removeListeners()
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
meth public void waitNoEvent(long)
meth public void waitNoEvent(long,long)
supr java.lang.Object
hfds EVENT_CHECKING_DELTA,WAIT_EVENT_TIMEOUT,WAIT_NO_EVENT_TIMEOUT,currentEventMask,listenerSet,output,timeouts
hcls EventType,EventWaiter,ListenerSet,NoEventWaiter

CLSS public org.netbeans.jemmy.FrameWaiter
cons public init()
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth protected java.lang.String getActionProducedMessage(long,java.lang.Object)
meth protected java.lang.String getGoldenActionProducedMessage()
meth protected java.lang.String getGoldenTimeoutExpiredMessage()
meth protected java.lang.String getGoldenWaitingStartedMessage()
meth protected java.lang.String getTimeoutExpiredMessage(long)
meth protected java.lang.String getWaitingStartedMessage()
meth public java.awt.Frame waitFrame(java.lang.String,boolean,boolean) throws java.lang.InterruptedException
meth public java.awt.Frame waitFrame(java.lang.String,boolean,boolean,int) throws java.lang.InterruptedException
meth public java.awt.Frame waitFrame(org.netbeans.jemmy.ComponentChooser) throws java.lang.InterruptedException
meth public java.awt.Frame waitFrame(org.netbeans.jemmy.ComponentChooser,int) throws java.lang.InterruptedException
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public static java.awt.Frame getFrame(java.lang.String,boolean,boolean)
meth public static java.awt.Frame getFrame(java.lang.String,boolean,boolean,int)
meth public static java.awt.Frame getFrame(org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.Frame getFrame(org.netbeans.jemmy.ComponentChooser,int)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
supr org.netbeans.jemmy.WindowWaiter
hfds AFTER_WAIT_TIME,WAIT_TIME,output,timeouts
hcls FrameByTitleChooser,FrameSubChooser

CLSS public org.netbeans.jemmy.JemmyException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Object)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.String,java.lang.Throwable,java.lang.Object)
meth public java.lang.Exception getInnerException()
meth public java.lang.Object getObject()
meth public java.lang.Throwable getInnerThrowable()
meth public void printStackTrace()
meth public void printStackTrace(java.io.PrintStream)
meth public void printStackTrace(java.io.PrintWriter)
supr java.lang.RuntimeException
hfds innerException,object

CLSS public org.netbeans.jemmy.JemmyInputException
cons public init(java.awt.Component)
cons public init(java.lang.String,java.awt.Component)
meth public java.awt.Component getComponent()
supr org.netbeans.jemmy.JemmyException

CLSS public org.netbeans.jemmy.JemmyProperties
cons protected init()
fld public static int QUEUE_MODEL_MASK
fld public static int ROBOT_MODEL_MASK
fld public static int SHORTCUT_MODEL_MASK
fld public static int SMOOTH_ROBOT_MODEL_MASK
meth protected org.netbeans.jemmy.JemmyProperties cloneThis()
meth protected static org.netbeans.jemmy.JemmyProperties push(org.netbeans.jemmy.JemmyProperties)
meth public boolean contains(java.lang.String)
meth public int getDispatchingModel()
meth public int getDragAndDropStepLength()
meth public int setDispatchingModel(int)
meth public int setDragAndDropStepLength(int)
meth public java.lang.Object getProperty(java.lang.String)
meth public java.lang.Object removeProperty(java.lang.String)
meth public java.lang.Object setProperty(java.lang.String,java.lang.Object)
meth public java.lang.String getResource(java.lang.String)
meth public java.lang.String getResource(java.lang.String,java.lang.String)
meth public java.lang.String[] getKeys()
meth public long getTimeout(java.lang.String)
meth public long initTimeout(java.lang.String,long)
meth public long setTimeout(java.lang.String,long)
meth public org.netbeans.jemmy.BundleManager getBundleManager()
meth public org.netbeans.jemmy.BundleManager setBundleManager(org.netbeans.jemmy.BundleManager)
meth public org.netbeans.jemmy.CharBindingMap getCharBindingMap()
meth public org.netbeans.jemmy.CharBindingMap setCharBindingMap(org.netbeans.jemmy.CharBindingMap)
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.TestOut setOutput(org.netbeans.jemmy.TestOut)
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public org.netbeans.jemmy.Timeouts setTimeouts(org.netbeans.jemmy.Timeouts)
meth public static int getCurrentDispatchingModel()
meth public static int getCurrentDragAndDropStepLength()
meth public static int getDefaultDispatchingModel()
meth public static int setCurrentDispatchingModel(int)
meth public static int setCurrentDragAndDropStepLength(int)
meth public static java.lang.Object getCurrentProperty(java.lang.String)
meth public static java.lang.Object removeCurrentProperty(java.lang.String)
meth public static java.lang.Object setCurrentProperty(java.lang.String,java.lang.Object)
meth public static java.lang.String getBuild()
meth public static java.lang.String getCurrentResource(java.lang.String)
meth public static java.lang.String getCurrentResource(java.lang.String,java.lang.String)
meth public static java.lang.String getFullVersion()
meth public static java.lang.String getMajorVersion()
meth public static java.lang.String getMinorVersion()
meth public static java.lang.String getVersion()
meth public static java.lang.String[] getCurrentKeys()
meth public static long getCurrentTimeout(java.lang.String)
meth public static long initCurrentTimeout(java.lang.String,long)
meth public static long setCurrentTimeout(java.lang.String,long)
meth public static org.netbeans.jemmy.BundleManager getCurrentBundleManager()
meth public static org.netbeans.jemmy.BundleManager setCurrentBundleManager(org.netbeans.jemmy.BundleManager)
meth public static org.netbeans.jemmy.CharBindingMap getCurrentCharBindingMap()
meth public static org.netbeans.jemmy.CharBindingMap setCurrentCharBindingMap(org.netbeans.jemmy.CharBindingMap)
meth public static org.netbeans.jemmy.JemmyProperties getProperties()
meth public static org.netbeans.jemmy.JemmyProperties pop()
meth public static org.netbeans.jemmy.JemmyProperties push()
meth public static org.netbeans.jemmy.TestOut getCurrentOutput()
meth public static org.netbeans.jemmy.TestOut setCurrentOutput(org.netbeans.jemmy.TestOut)
meth public static org.netbeans.jemmy.Timeouts getCurrentTimeouts()
meth public static org.netbeans.jemmy.Timeouts setCurrentTimeouts(org.netbeans.jemmy.Timeouts)
meth public static void main(java.lang.String[])
meth public void copyTo(org.netbeans.jemmy.JemmyProperties)
meth public void init()
meth public void initDispatchingModel()
meth public void initDispatchingModel(boolean,boolean)
meth public void initDispatchingModel(boolean,boolean,boolean)
meth public void initDispatchingModel(boolean,boolean,boolean,boolean)
meth public void initProperties()
meth public void initProperties(java.lang.String)
supr java.lang.Object
hfds DEFAULT_DRAG_AND_DROP_STEP_LENGTH,propStack,properties

CLSS public org.netbeans.jemmy.NoComponentUnderMouseException
cons public init()
supr java.lang.RuntimeException

CLSS public org.netbeans.jemmy.ObjectBrowser
cons public init()
intf org.netbeans.jemmy.Outputable
meth public java.lang.Object getObject()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public void printClasses()
meth public void printFields()
meth public void printFull()
meth public void printMethods()
meth public void printToString()
meth public void setObject(java.lang.Object)
meth public void setOutput(org.netbeans.jemmy.TestOut)
supr java.lang.Object
hfds object,output

CLSS public abstract interface org.netbeans.jemmy.Outputable
meth public abstract org.netbeans.jemmy.TestOut getOutput()
meth public abstract void setOutput(org.netbeans.jemmy.TestOut)

CLSS public org.netbeans.jemmy.QueueTool
cons public init()
innr public abstract static QueueAction
innr public final static JemmyInvocationEvent
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth public boolean wasLockingExpired()
meth public java.lang.Object invokeAndWait(org.netbeans.jemmy.Action,java.lang.Object)
meth public java.lang.Object invokeAndWait(org.netbeans.jemmy.QueueTool$QueueAction)
meth public java.lang.Object invokeSmoothly(org.netbeans.jemmy.Action,java.lang.Object)
meth public java.lang.Object invokeSmoothly(org.netbeans.jemmy.QueueTool$QueueAction)
meth public org.netbeans.jemmy.QueueTool$QueueAction invoke(java.lang.Runnable)
meth public org.netbeans.jemmy.QueueTool$QueueAction invoke(org.netbeans.jemmy.Action,java.lang.Object)
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public static boolean checkEmpty()
meth public static boolean isDispatchThread()
meth public static java.awt.EventQueue getQueue()
meth public static void installQueue()
meth public static void postEvent(java.awt.AWTEvent)
meth public static void processEvent(java.awt.AWTEvent)
meth public static void shortcutEvent(java.awt.AWTEvent)
meth public static void uninstallQueue()
meth public void invoke(org.netbeans.jemmy.QueueTool$QueueAction)
meth public void invokeAndWait(java.lang.Runnable)
meth public void invokeSmoothly(java.lang.Runnable)
meth public void lock()
meth public void lock(long)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
meth public void unlock()
meth public void waitEmpty()
meth public void waitEmpty(long)
supr java.lang.Object
hfds INVOCATION_TIMEOUT,LOCK_TIMEOUT,MAXIMUM_LOCKING_TIME,QUEUE_CHECKING_DELTA,WAIT_QUEUE_EMPTY_TIMEOUT,jemmyQueue,lockWaiter,locker,output,timeouts
hcls ActionRunnable,EventWaiter,JemmyQueue,Locker,RunnableRunnable,StayingEmptyWaiter,UnlockPostponer

CLSS public final static org.netbeans.jemmy.QueueTool$JemmyInvocationEvent
 outer org.netbeans.jemmy.QueueTool
cons public init(java.lang.Object,java.lang.Runnable,java.lang.Object,boolean)
supr java.awt.event.InvocationEvent

CLSS public abstract static org.netbeans.jemmy.QueueTool$QueueAction
 outer org.netbeans.jemmy.QueueTool
cons public init(java.lang.String)
intf java.lang.Runnable
meth public abstract java.lang.Object launch() throws java.lang.Exception
meth public boolean getFinished()
meth public final void run()
meth public java.lang.Exception getException()
meth public java.lang.Object getResult()
meth public java.lang.String getDescription()
supr java.lang.Object
hfds description,exception,finished,result

CLSS public abstract interface org.netbeans.jemmy.Scenario
meth public abstract int runIt(java.lang.Object)

CLSS public org.netbeans.jemmy.Test
cons protected init()
cons public init(java.lang.String)
cons public init(org.netbeans.jemmy.Scenario)
fld protected org.netbeans.jemmy.TestOut output
fld protected org.netbeans.jemmy.Timeouts timeouts
fld public static int SCENARIO_EXCEPTION_STATUS
fld public static int TEST_PASSED_STATUS
fld public static int WRONG_PARAMETERS_STATUS
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Scenario
intf org.netbeans.jemmy.Timeoutable
meth protected void doSleep(long)
meth public final java.lang.Object launch(java.lang.Object)
meth public final java.lang.String getDescription()
meth public int runIt(java.lang.Object)
meth public int startTest(java.lang.Object)
meth public org.netbeans.jemmy.Scenario testForName(java.lang.String)
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public static int run(java.lang.String[])
meth public static int run(java.lang.String[],java.io.PrintStream)
meth public static int run(java.lang.String[],java.io.PrintStream,java.io.PrintStream)
meth public static int run(java.lang.String[],java.io.PrintWriter)
meth public static int run(java.lang.String[],java.io.PrintWriter,java.io.PrintWriter)
meth public static void closeDown(int)
meth public static void main(java.lang.String[])
meth public void printSynopsis()
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
supr org.netbeans.jemmy.ActionProducer
hfds TEST_FAILED_STATUS,WHOLE_TEST_TIMEOUT,scenario

CLSS public org.netbeans.jemmy.TestCompletedException
cons public init(int,java.lang.Exception)
cons public init(int,java.lang.String)
meth public int getStatus()
supr org.netbeans.jemmy.JemmyException
hfds status

CLSS public org.netbeans.jemmy.TestOut
cons public init()
cons public init(java.io.InputStream,java.io.PrintStream,java.io.PrintStream)
cons public init(java.io.InputStream,java.io.PrintStream,java.io.PrintStream,java.io.PrintStream)
cons public init(java.io.InputStream,java.io.PrintWriter,java.io.PrintWriter)
cons public init(java.io.InputStream,java.io.PrintWriter,java.io.PrintWriter,java.io.PrintWriter)
meth public boolean getAutoFlushMode()
meth public boolean setAutoFlushMode(boolean)
meth public int read() throws java.io.IOException
meth public java.io.InputStream getInput()
meth public java.io.PrintWriter getErrput()
meth public java.io.PrintWriter getGolden()
meth public java.io.PrintWriter getOutput()
meth public java.lang.String readLine() throws java.io.IOException
meth public org.netbeans.jemmy.TestOut createErrorOutput()
meth public static org.netbeans.jemmy.TestOut getNullOutput()
meth public void flush()
meth public void print(java.lang.String)
meth public void printErrLine(java.lang.String)
meth public void printError(java.lang.String)
meth public void printGolden(java.lang.String)
meth public void printLine(boolean,java.lang.String)
meth public void printLine(java.lang.String)
meth public void printStackTrace(java.lang.Throwable)
meth public void printTrace(java.lang.String)
supr java.lang.Object
hfds autoFlushMode,buffInput,errput,golden_output,input,output

CLSS public org.netbeans.jemmy.Timeout
cons public init(java.lang.String,long)
meth public boolean expired()
meth public java.lang.String getName()
meth public long getValue()
meth public void check()
meth public void sleep()
meth public void start()
supr java.lang.Object
hfds name,startTime,value

CLSS public org.netbeans.jemmy.TimeoutExpiredException
cons public init(java.lang.String)
supr org.netbeans.jemmy.JemmyException

CLSS public abstract interface org.netbeans.jemmy.Timeoutable
meth public abstract org.netbeans.jemmy.Timeouts getTimeouts()
meth public abstract void setTimeouts(org.netbeans.jemmy.Timeouts)

CLSS public org.netbeans.jemmy.Timeouts
cons public init()
meth public boolean contains(java.lang.String)
meth public long getDeltaTimeout()
meth public long getTimeout(java.lang.String)
meth public long initTimeout(java.lang.String,long)
meth public long setTimeout(java.lang.String,long)
meth public org.netbeans.jemmy.Timeout create(java.lang.String)
meth public org.netbeans.jemmy.Timeout createDelta()
meth public org.netbeans.jemmy.Timeouts cloneThis()
meth public static boolean containsDefault(java.lang.String)
meth public static double getTimeoutsScale()
meth public static long getDefault(java.lang.String)
meth public static void initDefault(java.lang.String,long)
meth public static void setDefault(java.lang.String,long)
meth public void eSleep(java.lang.String) throws java.lang.InterruptedException
meth public void load() throws java.io.IOException
meth public void load(java.io.InputStream) throws java.io.IOException
meth public void load(java.lang.String) throws java.io.IOException
meth public void loadDebugTimeouts() throws java.io.IOException
meth public void loadDefaults() throws java.io.IOException
meth public void loadDefaults(java.io.InputStream) throws java.io.IOException
meth public void loadDefaults(java.lang.String) throws java.io.IOException
meth public void print(java.io.PrintStream)
meth public void print(java.io.PrintWriter)
meth public void sleep(java.lang.String)
supr java.lang.Object
hfds DELTA_TIME,defaults,timeouts,timeoutsScale

CLSS public abstract interface org.netbeans.jemmy.Waitable
meth public abstract java.lang.Object actionProduced(java.lang.Object)
meth public abstract java.lang.String getDescription()

CLSS public org.netbeans.jemmy.Waiter
cons protected init()
cons public init(org.netbeans.jemmy.Waitable)
fld public static volatile boolean USE_GLOBAL_TIMEOUT
fld public static volatile boolean globalTimeoutExpired
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
intf org.netbeans.jemmy.Waitable
meth protected java.lang.String getActionProducedMessage(long,java.lang.Object)
meth protected java.lang.String getGoldenActionProducedMessage()
meth protected java.lang.String getGoldenTimeoutExpiredMessage()
meth protected java.lang.String getGoldenWaitingStartedMessage()
meth protected java.lang.String getTimeoutExpiredMessage(long)
meth protected java.lang.String getWaitingStartedMessage()
meth protected long timeFromStart()
meth public java.lang.Object actionProduced(java.lang.Object)
meth public java.lang.Object waitAction(java.lang.Object) throws java.lang.InterruptedException
meth public java.lang.String getDescription()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public org.netbeans.jemmy.Timeouts setTimeoutsToCloneOf(org.netbeans.jemmy.Timeouts,java.lang.String)
meth public org.netbeans.jemmy.Timeouts setTimeoutsToCloneOf(org.netbeans.jemmy.Timeouts,java.lang.String,java.lang.String)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
meth public void setWaitingTimeOrigin(java.lang.String)
supr java.lang.Object
hfds AFTER_WAIT_TIME,TIME_DELTA,WAIT_TIME,endTime,out,result,startTime,timeouts,waitable,waitingTimeOrigin

CLSS public org.netbeans.jemmy.WindowWaiter
cons public init()
fld public static boolean FIND_INVISIBLE_WINDOWS
intf org.netbeans.jemmy.Timeoutable
meth protected java.awt.Window getOwner()
meth protected java.lang.String getActionProducedMessage(long,java.lang.Object)
meth protected java.lang.String getGoldenActionProducedMessage()
meth protected java.lang.String getGoldenTimeoutExpiredMessage()
meth protected java.lang.String getGoldenWaitingStartedMessage()
meth protected java.lang.String getTimeoutExpiredMessage(long)
meth protected java.lang.String getWaitingStartedMessage()
meth protected org.netbeans.jemmy.ComponentChooser getComponentChooser()
meth protected void setComponentChooser(org.netbeans.jemmy.ComponentChooser)
meth protected void setOwner(java.awt.Window)
meth public java.awt.Window waitWindow(java.awt.Window,org.netbeans.jemmy.ComponentChooser) throws java.lang.InterruptedException
meth public java.awt.Window waitWindow(java.awt.Window,org.netbeans.jemmy.ComponentChooser,int) throws java.lang.InterruptedException
meth public java.awt.Window waitWindow(org.netbeans.jemmy.ComponentChooser) throws java.lang.InterruptedException
meth public java.awt.Window waitWindow(org.netbeans.jemmy.ComponentChooser,int) throws java.lang.InterruptedException
meth public java.lang.Object actionProduced(java.lang.Object)
meth public java.lang.String getDescription()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public static java.awt.Window getWindow(java.awt.Window,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.Window getWindow(java.awt.Window,org.netbeans.jemmy.ComponentChooser,int)
meth public static java.awt.Window getWindow(org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.Window getWindow(org.netbeans.jemmy.ComponentChooser,int)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
supr org.netbeans.jemmy.Waiter
hfds AFTER_WAIT_TIME,WAIT_TIME,chooser,index,owner,timeouts
hcls IndexChooser

CLSS public abstract org.netbeans.jemmy.accessibility.AccessibilityChooser
cons public init()
intf org.netbeans.jemmy.ComponentChooser
meth public abstract boolean checkContext(javax.accessibility.AccessibleContext)
meth public final boolean checkComponent(java.awt.Component)
supr java.lang.Object

CLSS public org.netbeans.jemmy.accessibility.AccessibleDescriptionChooser
cons public init(java.lang.String)
cons public init(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public final boolean checkContext(javax.accessibility.AccessibleContext)
meth public java.lang.String getDescription()
supr org.netbeans.jemmy.accessibility.AccessibilityChooser
hfds comparator,description

CLSS public org.netbeans.jemmy.accessibility.AccessibleNameChooser
cons public init(java.lang.String)
cons public init(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public final boolean checkContext(javax.accessibility.AccessibleContext)
meth public java.lang.String getDescription()
supr org.netbeans.jemmy.accessibility.AccessibilityChooser
hfds comparator,name

CLSS public abstract interface org.netbeans.jemmy.demo.CommentWindow
meth public abstract boolean isInterrupted()
meth public abstract boolean isStopped()
meth public abstract java.lang.String getInterruptMessage()
meth public abstract void close()
meth public abstract void nextStep(java.lang.String)
meth public abstract void setTitle(java.lang.String)
meth public abstract void showFinalComment(java.lang.String)

CLSS public org.netbeans.jemmy.demo.DefaultCommentWindow
cons public init()
cons public init(boolean)
innr public Mover
intf org.netbeans.jemmy.demo.CommentWindow
meth public boolean isInterrupted()
meth public boolean isStopped()
meth public java.lang.String getInterruptMessage()
meth public void close()
meth public void nextStep(java.lang.String)
meth public void setCommentTimeout(long)
meth public void showFinalComment(java.lang.String)
supr javax.swing.JDialog
hfds comments,contButton,continual,finishButton,finished,interrupted,nextStepButton,readCommentTimeout,stopped

CLSS public org.netbeans.jemmy.demo.DefaultCommentWindow$Mover
 outer org.netbeans.jemmy.demo.DefaultCommentWindow
cons public init(org.netbeans.jemmy.demo.DefaultCommentWindow,javax.swing.JButton)
meth public void enter()
meth public void push()
meth public void run()
supr java.lang.Thread
hfds bo,toPush

CLSS public org.netbeans.jemmy.demo.DemoInterruptedException
cons public init(java.lang.String)
meth public void printStackTrace()
meth public void printStackTrace(java.io.PrintStream)
supr org.netbeans.jemmy.TestCompletedException

CLSS public org.netbeans.jemmy.demo.Demonstrator
cons public init()
meth public static void nextStep(java.lang.String)
meth public static void setCommentWindow(org.netbeans.jemmy.demo.CommentWindow)
meth public static void setTitle(java.lang.String)
meth public static void showFinalComment(java.lang.String)
supr java.lang.Object
hfds displayer,nonDisplayer
hcls NonWindow

CLSS public org.netbeans.jemmy.drivers.APIDriverInstaller
cons public init()
cons public init(boolean)
supr org.netbeans.jemmy.drivers.ArrayDriverInstaller

CLSS public org.netbeans.jemmy.drivers.ArrayDriverInstaller
cons public init(java.lang.String[],java.lang.Object[])
intf org.netbeans.jemmy.drivers.DriverInstaller
meth public void install()
supr java.lang.Object
hfds drivers,ids

CLSS public abstract interface org.netbeans.jemmy.drivers.ButtonDriver
meth public abstract void press(org.netbeans.jemmy.operators.ComponentOperator)
meth public abstract void push(org.netbeans.jemmy.operators.ComponentOperator)
meth public abstract void release(org.netbeans.jemmy.operators.ComponentOperator)

CLSS public org.netbeans.jemmy.drivers.DefaultDriverInstaller
cons public init()
cons public init(boolean)
supr org.netbeans.jemmy.drivers.ArrayDriverInstaller

CLSS public abstract interface org.netbeans.jemmy.drivers.DescriptablePathChooser
intf org.netbeans.jemmy.drivers.PathChooser
meth public abstract java.lang.String getDescription()

CLSS public abstract interface org.netbeans.jemmy.drivers.Driver
meth public abstract java.lang.Class[] getSupported()

CLSS public abstract interface org.netbeans.jemmy.drivers.DriverInstaller
meth public abstract void install()

CLSS public org.netbeans.jemmy.drivers.DriverManager
fld public final static java.lang.String BUTTON_DRIVER_ID = "drivers.button"
fld public final static java.lang.String DRIVER_ID = "drivers."
fld public final static java.lang.String FOCUS_DRIVER_ID = "drivers.focus"
fld public final static java.lang.String FRAME_DRIVER_ID = "drivers.frame"
fld public final static java.lang.String INTERNAL_FRAME_DRIVER_ID = "drivers.internal_frame"
fld public final static java.lang.String KEY_DRIVER_ID = "drivers.key"
fld public final static java.lang.String LIST_DRIVER_ID = "drivers.list"
fld public final static java.lang.String MENU_DRIVER_ID = "drivers.menu"
fld public final static java.lang.String MOUSE_DRIVER_ID = "drivers.mouse"
fld public final static java.lang.String MULTISELLIST_DRIVER_ID = "drivers.multisellist"
fld public final static java.lang.String ORDEREDLIST_DRIVER_ID = "drivers.orderedlist"
fld public final static java.lang.String SCROLL_DRIVER_ID = "drivers.scroll"
fld public final static java.lang.String TABLE_DRIVER_ID = "drivers.table"
fld public final static java.lang.String TEXT_DRIVER_ID = "drivers.text"
fld public final static java.lang.String TREE_DRIVER_ID = "drivers.tree"
fld public final static java.lang.String WINDOW_DRIVER_ID = "drivers.window"
meth public static java.lang.Object getDriver(java.lang.String,java.lang.Class)
meth public static java.lang.Object getDriver(java.lang.String,java.lang.Class,org.netbeans.jemmy.JemmyProperties)
meth public static java.lang.Object getDriver(java.lang.String,org.netbeans.jemmy.operators.ComponentOperator)
meth public static org.netbeans.jemmy.drivers.ButtonDriver getButtonDriver(java.lang.Class)
meth public static org.netbeans.jemmy.drivers.ButtonDriver getButtonDriver(org.netbeans.jemmy.operators.ComponentOperator)
meth public static org.netbeans.jemmy.drivers.FocusDriver getFocusDriver(java.lang.Class)
meth public static org.netbeans.jemmy.drivers.FocusDriver getFocusDriver(org.netbeans.jemmy.operators.ComponentOperator)
meth public static org.netbeans.jemmy.drivers.FrameDriver getFrameDriver(java.lang.Class)
meth public static org.netbeans.jemmy.drivers.FrameDriver getFrameDriver(org.netbeans.jemmy.operators.ComponentOperator)
meth public static org.netbeans.jemmy.drivers.InternalFrameDriver getInternalFrameDriver(java.lang.Class)
meth public static org.netbeans.jemmy.drivers.InternalFrameDriver getInternalFrameDriver(org.netbeans.jemmy.operators.ComponentOperator)
meth public static org.netbeans.jemmy.drivers.KeyDriver getKeyDriver(java.lang.Class)
meth public static org.netbeans.jemmy.drivers.KeyDriver getKeyDriver(org.netbeans.jemmy.operators.ComponentOperator)
meth public static org.netbeans.jemmy.drivers.ListDriver getListDriver(java.lang.Class)
meth public static org.netbeans.jemmy.drivers.ListDriver getListDriver(org.netbeans.jemmy.operators.ComponentOperator)
meth public static org.netbeans.jemmy.drivers.MenuDriver getMenuDriver(java.lang.Class)
meth public static org.netbeans.jemmy.drivers.MenuDriver getMenuDriver(org.netbeans.jemmy.operators.ComponentOperator)
meth public static org.netbeans.jemmy.drivers.MouseDriver getMouseDriver(java.lang.Class)
meth public static org.netbeans.jemmy.drivers.MouseDriver getMouseDriver(org.netbeans.jemmy.operators.ComponentOperator)
meth public static org.netbeans.jemmy.drivers.MultiSelListDriver getMultiSelListDriver(java.lang.Class)
meth public static org.netbeans.jemmy.drivers.MultiSelListDriver getMultiSelListDriver(org.netbeans.jemmy.operators.ComponentOperator)
meth public static org.netbeans.jemmy.drivers.OrderedListDriver getOrderedListDriver(java.lang.Class)
meth public static org.netbeans.jemmy.drivers.OrderedListDriver getOrderedListDriver(org.netbeans.jemmy.operators.ComponentOperator)
meth public static org.netbeans.jemmy.drivers.ScrollDriver getScrollDriver(java.lang.Class)
meth public static org.netbeans.jemmy.drivers.ScrollDriver getScrollDriver(org.netbeans.jemmy.operators.ComponentOperator)
meth public static org.netbeans.jemmy.drivers.TableDriver getTableDriver(java.lang.Class)
meth public static org.netbeans.jemmy.drivers.TableDriver getTableDriver(org.netbeans.jemmy.operators.ComponentOperator)
meth public static org.netbeans.jemmy.drivers.TextDriver getTextDriver(java.lang.Class)
meth public static org.netbeans.jemmy.drivers.TextDriver getTextDriver(org.netbeans.jemmy.operators.ComponentOperator)
meth public static org.netbeans.jemmy.drivers.TreeDriver getTreeDriver(java.lang.Class)
meth public static org.netbeans.jemmy.drivers.TreeDriver getTreeDriver(org.netbeans.jemmy.operators.ComponentOperator)
meth public static org.netbeans.jemmy.drivers.WindowDriver getWindowDriver(java.lang.Class)
meth public static org.netbeans.jemmy.drivers.WindowDriver getWindowDriver(org.netbeans.jemmy.operators.ComponentOperator)
meth public static void removeDriver(java.lang.String,java.lang.Class)
meth public static void removeDriver(java.lang.String,java.lang.Class[])
meth public static void removeDriver(java.lang.String,java.lang.String)
meth public static void removeDriver(java.lang.String,java.lang.String[])
meth public static void removeDrivers(java.lang.String)
meth public static void setButtonDriver(org.netbeans.jemmy.drivers.ButtonDriver)
meth public static void setDriver(java.lang.String,java.lang.Object,java.lang.Class)
meth public static void setDriver(java.lang.String,java.lang.Object,java.lang.String)
meth public static void setDriver(java.lang.String,org.netbeans.jemmy.drivers.Driver)
meth public static void setDriver(java.lang.String,org.netbeans.jemmy.drivers.LightDriver)
meth public static void setFocusDriver(org.netbeans.jemmy.drivers.FocusDriver)
meth public static void setFrameDriver(org.netbeans.jemmy.drivers.FrameDriver)
meth public static void setInternalFrameDriver(org.netbeans.jemmy.drivers.InternalFrameDriver)
meth public static void setKeyDriver(org.netbeans.jemmy.drivers.KeyDriver)
meth public static void setListDriver(org.netbeans.jemmy.drivers.ListDriver)
meth public static void setMenuDriver(org.netbeans.jemmy.drivers.MenuDriver)
meth public static void setMouseDriver(org.netbeans.jemmy.drivers.MouseDriver)
meth public static void setMultiSelListDriver(org.netbeans.jemmy.drivers.MultiSelListDriver)
meth public static void setOrderedListDriver(org.netbeans.jemmy.drivers.OrderedListDriver)
meth public static void setScrollDriver(org.netbeans.jemmy.drivers.ScrollDriver)
meth public static void setTableDriver(org.netbeans.jemmy.drivers.TableDriver)
meth public static void setTextDriver(org.netbeans.jemmy.drivers.TextDriver)
meth public static void setTreeDriver(org.netbeans.jemmy.drivers.TreeDriver)
meth public static void setWindowDriver(org.netbeans.jemmy.drivers.WindowDriver)
supr java.lang.Object

CLSS public abstract interface org.netbeans.jemmy.drivers.EditorDriver
meth public abstract void enterNewValue(org.netbeans.jemmy.operators.ComponentOperator,java.lang.Object)

CLSS public abstract interface org.netbeans.jemmy.drivers.FocusDriver
meth public abstract void giveFocus(org.netbeans.jemmy.operators.ComponentOperator)

CLSS public abstract interface org.netbeans.jemmy.drivers.FrameDriver
meth public abstract void deiconify(org.netbeans.jemmy.operators.ComponentOperator)
meth public abstract void demaximize(org.netbeans.jemmy.operators.ComponentOperator)
meth public abstract void iconify(org.netbeans.jemmy.operators.ComponentOperator)
meth public abstract void maximize(org.netbeans.jemmy.operators.ComponentOperator)

CLSS public org.netbeans.jemmy.drivers.InputDriverInstaller
cons public init()
cons public init(boolean)
cons public init(boolean,boolean)
cons public init(boolean,org.netbeans.jemmy.Timeout)
cons public init(org.netbeans.jemmy.Timeout)
meth public void install()
supr java.lang.Object
hfds robotAutoDelay,smooth,useEventDrivers

CLSS public abstract interface org.netbeans.jemmy.drivers.InternalFrameDriver
meth public abstract java.awt.Component getTitlePane(org.netbeans.jemmy.operators.ComponentOperator)

CLSS public abstract interface org.netbeans.jemmy.drivers.KeyDriver
meth public abstract void pressKey(org.netbeans.jemmy.operators.ComponentOperator,int,int)
meth public abstract void pushKey(org.netbeans.jemmy.operators.ComponentOperator,int,int,org.netbeans.jemmy.Timeout)
meth public abstract void releaseKey(org.netbeans.jemmy.operators.ComponentOperator,int,int)
meth public abstract void typeKey(org.netbeans.jemmy.operators.ComponentOperator,int,char,int,org.netbeans.jemmy.Timeout)
meth public abstract void typedKey(org.netbeans.jemmy.operators.ComponentOperator,int,char,int)

CLSS public abstract interface org.netbeans.jemmy.drivers.LightDriver
meth public abstract java.lang.String[] getSupported()

CLSS public abstract org.netbeans.jemmy.drivers.LightSupportiveDriver
cons public init(java.lang.String[])
intf org.netbeans.jemmy.drivers.LightDriver
meth public java.lang.String[] getSupported()
meth public void checkSupported(org.netbeans.jemmy.operators.ComponentOperator)
supr java.lang.Object
hfds supported

CLSS public abstract interface org.netbeans.jemmy.drivers.ListDriver
meth public abstract void selectItem(org.netbeans.jemmy.operators.ComponentOperator,int)

CLSS public abstract interface org.netbeans.jemmy.drivers.MenuDriver
meth public abstract java.lang.Object pushMenu(org.netbeans.jemmy.operators.ComponentOperator,org.netbeans.jemmy.drivers.PathChooser)

CLSS public abstract interface org.netbeans.jemmy.drivers.MouseDriver
meth public abstract void clickMouse(org.netbeans.jemmy.operators.ComponentOperator,int,int,int,int,int,org.netbeans.jemmy.Timeout)
meth public abstract void dragMouse(org.netbeans.jemmy.operators.ComponentOperator,int,int,int,int)
meth public abstract void dragNDrop(org.netbeans.jemmy.operators.ComponentOperator,int,int,int,int,int,int,org.netbeans.jemmy.Timeout,org.netbeans.jemmy.Timeout)
meth public abstract void enterMouse(org.netbeans.jemmy.operators.ComponentOperator)
meth public abstract void exitMouse(org.netbeans.jemmy.operators.ComponentOperator)
meth public abstract void moveMouse(org.netbeans.jemmy.operators.ComponentOperator,int,int)
meth public abstract void pressMouse(org.netbeans.jemmy.operators.ComponentOperator,int,int,int,int)
meth public abstract void releaseMouse(org.netbeans.jemmy.operators.ComponentOperator,int,int,int,int)

CLSS public abstract interface org.netbeans.jemmy.drivers.MultiSelListDriver
intf org.netbeans.jemmy.drivers.ListDriver
meth public abstract void selectItems(org.netbeans.jemmy.operators.ComponentOperator,int[])

CLSS public abstract interface org.netbeans.jemmy.drivers.OrderedListDriver
intf org.netbeans.jemmy.drivers.MultiSelListDriver
meth public abstract void moveItem(org.netbeans.jemmy.operators.ComponentOperator,int,int)

CLSS public abstract interface org.netbeans.jemmy.drivers.PathChooser
meth public abstract boolean checkPathComponent(int,java.lang.Object)
meth public abstract int getDepth()

CLSS public abstract interface org.netbeans.jemmy.drivers.ScrollDriver
meth public abstract void scroll(org.netbeans.jemmy.operators.ComponentOperator,org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster)
meth public abstract void scrollToMaximum(org.netbeans.jemmy.operators.ComponentOperator,int)
meth public abstract void scrollToMinimum(org.netbeans.jemmy.operators.ComponentOperator,int)

CLSS public abstract org.netbeans.jemmy.drivers.SupportiveDriver
cons public init(java.lang.Class[])
intf org.netbeans.jemmy.drivers.Driver
meth public java.lang.Class[] getSupported()
meth public void checkSupported(org.netbeans.jemmy.operators.ComponentOperator)
supr java.lang.Object
hfds supported

CLSS public abstract interface org.netbeans.jemmy.drivers.TableDriver
meth public abstract void editCell(org.netbeans.jemmy.operators.ComponentOperator,int,int,java.lang.Object)
meth public abstract void selectCell(org.netbeans.jemmy.operators.ComponentOperator,int,int)

CLSS public abstract interface org.netbeans.jemmy.drivers.TextDriver
meth public abstract void changeCaretPosition(org.netbeans.jemmy.operators.ComponentOperator,int)
meth public abstract void changeText(org.netbeans.jemmy.operators.ComponentOperator,java.lang.String)
meth public abstract void clearText(org.netbeans.jemmy.operators.ComponentOperator)
meth public abstract void enterText(org.netbeans.jemmy.operators.ComponentOperator,java.lang.String)
meth public abstract void selectText(org.netbeans.jemmy.operators.ComponentOperator,int,int)
meth public abstract void typeText(org.netbeans.jemmy.operators.ComponentOperator,java.lang.String,int)

CLSS public abstract interface org.netbeans.jemmy.drivers.TreeDriver
intf org.netbeans.jemmy.drivers.MultiSelListDriver
meth public abstract void collapseItem(org.netbeans.jemmy.operators.ComponentOperator,int)
meth public abstract void editItem(org.netbeans.jemmy.operators.ComponentOperator,int,java.lang.Object,org.netbeans.jemmy.Timeout)
meth public abstract void expandItem(org.netbeans.jemmy.operators.ComponentOperator,int)
meth public abstract void startEditing(org.netbeans.jemmy.operators.ComponentOperator,int,org.netbeans.jemmy.Timeout)

CLSS public org.netbeans.jemmy.drivers.UnsupportedOperatorException
cons public init(java.lang.Class,java.lang.Class)
meth public static void checkSupported(java.lang.Class,java.lang.Class[],java.lang.Class)
meth public static void checkSupported(java.lang.Class,java.lang.String[],java.lang.Class)
supr org.netbeans.jemmy.JemmyException

CLSS public abstract interface org.netbeans.jemmy.drivers.WindowDriver
meth public abstract void activate(org.netbeans.jemmy.operators.ComponentOperator)
meth public abstract void close(org.netbeans.jemmy.operators.ComponentOperator)
meth public abstract void move(org.netbeans.jemmy.operators.ComponentOperator,int,int)
meth public abstract void requestClose(org.netbeans.jemmy.operators.ComponentOperator)
meth public abstract void requestCloseAndThenHide(org.netbeans.jemmy.operators.ComponentOperator)
meth public abstract void resize(org.netbeans.jemmy.operators.ComponentOperator,int,int)

CLSS public org.netbeans.jemmy.drivers.buttons.ButtonMouseDriver
cons public init()
intf org.netbeans.jemmy.drivers.ButtonDriver
meth public void press(org.netbeans.jemmy.operators.ComponentOperator)
meth public void push(org.netbeans.jemmy.operators.ComponentOperator)
meth public void release(org.netbeans.jemmy.operators.ComponentOperator)
supr org.netbeans.jemmy.drivers.LightSupportiveDriver

CLSS public org.netbeans.jemmy.drivers.focus.APIFocusDriver
cons public init()
intf org.netbeans.jemmy.drivers.FocusDriver
meth public void giveFocus(org.netbeans.jemmy.operators.ComponentOperator)
supr org.netbeans.jemmy.drivers.LightSupportiveDriver
hfds eDriver

CLSS public org.netbeans.jemmy.drivers.focus.MouseFocusDriver
cons public init()
intf org.netbeans.jemmy.drivers.FocusDriver
meth public void giveFocus(org.netbeans.jemmy.operators.ComponentOperator)
supr org.netbeans.jemmy.drivers.LightSupportiveDriver
hfds queueTool

CLSS public org.netbeans.jemmy.drivers.input.EventDriver
cons public init()
cons public init(java.lang.String[])
innr protected Dispatcher
meth protected void checkVisibility(java.awt.Component)
meth public void dispatchEvent(java.awt.Component,java.awt.AWTEvent)
supr org.netbeans.jemmy.drivers.LightSupportiveDriver

CLSS protected org.netbeans.jemmy.drivers.input.EventDriver$Dispatcher
 outer org.netbeans.jemmy.drivers.input.EventDriver
cons public init(org.netbeans.jemmy.drivers.input.EventDriver,java.awt.Component,java.awt.AWTEvent)
meth public java.lang.Object launch()
supr org.netbeans.jemmy.QueueTool$QueueAction
hfds component,event

CLSS public org.netbeans.jemmy.drivers.input.KeyEventDriver
cons public init()
cons public init(java.lang.String[])
intf org.netbeans.jemmy.drivers.KeyDriver
meth public void pressKey(org.netbeans.jemmy.operators.ComponentOperator,int,int)
meth public void pushKey(org.netbeans.jemmy.operators.ComponentOperator,int,int,org.netbeans.jemmy.Timeout)
meth public void releaseKey(org.netbeans.jemmy.operators.ComponentOperator,int,int)
meth public void typeKey(org.netbeans.jemmy.operators.ComponentOperator,int,char,int,org.netbeans.jemmy.Timeout)
meth public void typedKey(org.netbeans.jemmy.operators.ComponentOperator,int,char,int)
supr org.netbeans.jemmy.drivers.input.EventDriver

CLSS public org.netbeans.jemmy.drivers.input.KeyRobotDriver
cons public init(org.netbeans.jemmy.Timeout)
cons public init(org.netbeans.jemmy.Timeout,java.lang.String[])
intf org.netbeans.jemmy.drivers.KeyDriver
meth public void pressKey(org.netbeans.jemmy.operators.ComponentOperator,int,int)
meth public void pushKey(org.netbeans.jemmy.operators.ComponentOperator,int,int,org.netbeans.jemmy.Timeout)
meth public void releaseKey(org.netbeans.jemmy.operators.ComponentOperator,int,int)
meth public void typeKey(org.netbeans.jemmy.operators.ComponentOperator,int,char,int,org.netbeans.jemmy.Timeout)
meth public void typedKey(org.netbeans.jemmy.operators.ComponentOperator,int,char,int)
supr org.netbeans.jemmy.drivers.input.RobotDriver

CLSS public org.netbeans.jemmy.drivers.input.MouseEventDriver
cons public init()
cons public init(java.lang.String[])
intf org.netbeans.jemmy.drivers.MouseDriver
meth protected void dispatchEvent(java.awt.Component,int,int,int,int,int,int)
meth public void clickMouse(org.netbeans.jemmy.operators.ComponentOperator,int,int,int,int,int,org.netbeans.jemmy.Timeout)
meth public void dragMouse(org.netbeans.jemmy.operators.ComponentOperator,int,int,int,int)
meth public void dragNDrop(org.netbeans.jemmy.operators.ComponentOperator,int,int,int,int,int,int,org.netbeans.jemmy.Timeout,org.netbeans.jemmy.Timeout)
meth public void enterMouse(org.netbeans.jemmy.operators.ComponentOperator)
meth public void exitMouse(org.netbeans.jemmy.operators.ComponentOperator)
meth public void moveMouse(org.netbeans.jemmy.operators.ComponentOperator,int,int)
meth public void pressMouse(org.netbeans.jemmy.operators.ComponentOperator,int,int,int,int)
meth public void releaseMouse(org.netbeans.jemmy.operators.ComponentOperator,int,int,int,int)
supr org.netbeans.jemmy.drivers.input.EventDriver

CLSS public org.netbeans.jemmy.drivers.input.MouseRobotDriver
cons public init(org.netbeans.jemmy.Timeout)
cons public init(org.netbeans.jemmy.Timeout,boolean)
cons public init(org.netbeans.jemmy.Timeout,java.lang.String[])
cons public init(org.netbeans.jemmy.Timeout,java.lang.String[],boolean)
intf org.netbeans.jemmy.drivers.MouseDriver
meth protected int getAbsoluteX(org.netbeans.jemmy.operators.ComponentOperator,int)
meth protected int getAbsoluteY(org.netbeans.jemmy.operators.ComponentOperator,int)
meth public void clickMouse(org.netbeans.jemmy.operators.ComponentOperator,int,int,int,int,int,org.netbeans.jemmy.Timeout)
meth public void dragMouse(org.netbeans.jemmy.operators.ComponentOperator,int,int,int,int)
meth public void dragNDrop(org.netbeans.jemmy.operators.ComponentOperator,int,int,int,int,int,int,org.netbeans.jemmy.Timeout,org.netbeans.jemmy.Timeout)
meth public void enterMouse(org.netbeans.jemmy.operators.ComponentOperator)
meth public void exitMouse(org.netbeans.jemmy.operators.ComponentOperator)
meth public void moveMouse(org.netbeans.jemmy.operators.ComponentOperator,int,int)
meth public void pressMouse(org.netbeans.jemmy.operators.ComponentOperator,int,int,int,int)
meth public void releaseMouse(org.netbeans.jemmy.operators.ComponentOperator,int,int,int,int)
supr org.netbeans.jemmy.drivers.input.RobotDriver

CLSS public org.netbeans.jemmy.drivers.input.RobotDriver
cons public init(org.netbeans.jemmy.Timeout)
cons public init(org.netbeans.jemmy.Timeout,boolean)
cons public init(org.netbeans.jemmy.Timeout,java.lang.String[])
cons public init(org.netbeans.jemmy.Timeout,java.lang.String[],boolean)
fld protected org.netbeans.jemmy.ClassReference robotReference
fld protected org.netbeans.jemmy.QueueTool qtool
fld protected org.netbeans.jemmy.Timeout autoDelay
meth protected void makeAnOperation(java.lang.String,java.lang.Object[],java.lang.Class[])
meth protected void pressModifiers(int)
meth protected void releaseModifiers(int)
meth protected void synchronizeRobot()
meth public void clickMouse(int,int,int,int,int,org.netbeans.jemmy.Timeout)
meth public void dragMouse(int,int,int,int)
meth public void dragNDrop(int,int,int,int,int,int,org.netbeans.jemmy.Timeout,org.netbeans.jemmy.Timeout)
meth public void moveMouse(int,int)
meth public void pressKey(int,int)
meth public void pressMouse(int,int)
meth public void releaseKey(int,int)
meth public void releaseMouse(int,int)
supr org.netbeans.jemmy.drivers.LightSupportiveDriver
hfds CONSTANT1,CONSTANT2,haveOldPos,oldX,oldY,smooth

CLSS public org.netbeans.jemmy.drivers.lists.ChoiceDriver
cons public init()
intf org.netbeans.jemmy.drivers.ListDriver
meth public void selectItem(org.netbeans.jemmy.operators.ComponentOperator,int)
supr org.netbeans.jemmy.drivers.LightSupportiveDriver
hfds RIGHT_INDENT

CLSS public org.netbeans.jemmy.drivers.lists.JComboMouseDriver
cons public init()
intf org.netbeans.jemmy.drivers.ListDriver
meth public void selectItem(org.netbeans.jemmy.operators.ComponentOperator,int)
supr org.netbeans.jemmy.drivers.LightSupportiveDriver
hfds queueTool

CLSS public org.netbeans.jemmy.drivers.lists.JListMouseDriver
cons public init()
intf org.netbeans.jemmy.drivers.MultiSelListDriver
meth protected void clickOnItem(org.netbeans.jemmy.operators.JListOperator,int)
meth protected void clickOnItem(org.netbeans.jemmy.operators.JListOperator,int,int)
meth public void selectItem(org.netbeans.jemmy.operators.ComponentOperator,int)
meth public void selectItems(org.netbeans.jemmy.operators.ComponentOperator,int[])
supr org.netbeans.jemmy.drivers.LightSupportiveDriver
hfds queueTool

CLSS public org.netbeans.jemmy.drivers.lists.JTabAPIDriver
cons public init()
intf org.netbeans.jemmy.drivers.ListDriver
meth public void selectItem(org.netbeans.jemmy.operators.ComponentOperator,int)
supr org.netbeans.jemmy.drivers.LightSupportiveDriver
hfds queueTool

CLSS public org.netbeans.jemmy.drivers.lists.JTabMouseDriver
cons public init()
intf org.netbeans.jemmy.drivers.ListDriver
meth public void selectItem(org.netbeans.jemmy.operators.ComponentOperator,int)
supr org.netbeans.jemmy.drivers.LightSupportiveDriver
hfds queueTool

CLSS public org.netbeans.jemmy.drivers.lists.JTableHeaderDriver
cons public init()
intf org.netbeans.jemmy.drivers.OrderedListDriver
meth protected void clickOnHeader(org.netbeans.jemmy.operators.JTableHeaderOperator,int)
meth protected void clickOnHeader(org.netbeans.jemmy.operators.JTableHeaderOperator,int,int)
meth public void moveItem(org.netbeans.jemmy.operators.ComponentOperator,int,int)
meth public void selectItem(org.netbeans.jemmy.operators.ComponentOperator,int)
meth public void selectItems(org.netbeans.jemmy.operators.ComponentOperator,int[])
supr org.netbeans.jemmy.drivers.LightSupportiveDriver
hfds queueTool

CLSS public org.netbeans.jemmy.drivers.lists.ListAPIDriver
cons public init()
intf org.netbeans.jemmy.drivers.MultiSelListDriver
meth public void selectItem(org.netbeans.jemmy.operators.ComponentOperator,int)
meth public void selectItems(org.netbeans.jemmy.operators.ComponentOperator,int[])
supr org.netbeans.jemmy.drivers.LightSupportiveDriver

CLSS public org.netbeans.jemmy.drivers.lists.ListKeyboardDriver
cons public init()
intf org.netbeans.jemmy.drivers.MultiSelListDriver
meth public void selectItem(org.netbeans.jemmy.operators.ComponentOperator,int)
supr org.netbeans.jemmy.drivers.lists.ListAPIDriver

CLSS public org.netbeans.jemmy.drivers.menus.APIJMenuDriver
cons public init()
intf org.netbeans.jemmy.drivers.MenuDriver
meth protected java.lang.Object push(org.netbeans.jemmy.operators.ComponentOperator,javax.swing.JMenuBar,org.netbeans.jemmy.drivers.PathChooser,int,boolean)
meth protected void waitNoPopupMenu(org.netbeans.jemmy.operators.ComponentOperator)
supr org.netbeans.jemmy.drivers.menus.DefaultJMenuDriver

CLSS public org.netbeans.jemmy.drivers.menus.AppleMenuDriver
cons public init()
intf org.netbeans.jemmy.drivers.MenuDriver
meth public java.lang.Object pushMenu(org.netbeans.jemmy.operators.ComponentOperator,org.netbeans.jemmy.drivers.PathChooser)
supr org.netbeans.jemmy.drivers.input.RobotDriver

CLSS public org.netbeans.jemmy.drivers.menus.DefaultJMenuDriver
cons public init()
intf org.netbeans.jemmy.drivers.MenuDriver
meth protected java.lang.Object push(org.netbeans.jemmy.operators.ComponentOperator,org.netbeans.jemmy.operators.ComponentOperator,javax.swing.JMenuBar,org.netbeans.jemmy.drivers.PathChooser,int,boolean)
meth protected javax.swing.JMenuItem waitItem(org.netbeans.jemmy.operators.ComponentOperator,javax.swing.MenuElement,org.netbeans.jemmy.drivers.PathChooser,int)
meth protected javax.swing.JPopupMenu waitPopupMenu(org.netbeans.jemmy.operators.ComponentOperator)
meth public java.lang.Object pushMenu(org.netbeans.jemmy.operators.ComponentOperator,org.netbeans.jemmy.drivers.PathChooser)
meth public static java.lang.Object getSelectedElement(javax.swing.JMenuBar)
supr org.netbeans.jemmy.drivers.LightSupportiveDriver
hcls JMenuItemWaiter

CLSS public org.netbeans.jemmy.drivers.menus.QueueJMenuDriver
cons public init()
intf org.netbeans.jemmy.drivers.MenuDriver
meth public java.lang.Object pushMenu(org.netbeans.jemmy.operators.ComponentOperator,org.netbeans.jemmy.drivers.PathChooser)
supr org.netbeans.jemmy.drivers.LightSupportiveDriver
hfds queueTool
hcls OneReleaseAction,PopupMenuChooser

CLSS public abstract org.netbeans.jemmy.drivers.scrolling.AWTScrollDriver
cons public init(java.lang.String[])
meth protected abstract java.awt.Point getClickPoint(org.netbeans.jemmy.operators.ComponentOperator,int,int)
meth protected boolean canDragAndDrop(org.netbeans.jemmy.operators.ComponentOperator)
meth protected boolean canJump(org.netbeans.jemmy.operators.ComponentOperator)
meth protected boolean canPushAndWait(org.netbeans.jemmy.operators.ComponentOperator)
meth protected int getDragAndDropStepLength(org.netbeans.jemmy.operators.ComponentOperator)
meth protected java.awt.Point startDragging(org.netbeans.jemmy.operators.ComponentOperator)
meth protected org.netbeans.jemmy.Timeout getScrollDeltaTimeout(org.netbeans.jemmy.operators.ComponentOperator)
meth protected void drag(org.netbeans.jemmy.operators.ComponentOperator,java.awt.Point)
meth protected void drop(org.netbeans.jemmy.operators.ComponentOperator,java.awt.Point)
meth protected void jump(org.netbeans.jemmy.operators.ComponentOperator,org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster)
meth protected void startPushAndWait(org.netbeans.jemmy.operators.ComponentOperator,int,int)
meth protected void step(org.netbeans.jemmy.operators.ComponentOperator,org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster)
meth protected void stopPushAndWait(org.netbeans.jemmy.operators.ComponentOperator,int,int)
supr org.netbeans.jemmy.drivers.scrolling.AbstractScrollDriver
hfds queueTool

CLSS public abstract org.netbeans.jemmy.drivers.scrolling.AbstractScrollDriver
cons public init(java.lang.String[])
fld public final static int ADJUST_CLICK_COUNT = 10
fld public final static java.lang.String SCROLL_FREEZE_TIMEOUT
intf org.netbeans.jemmy.drivers.ScrollDriver
meth protected abstract boolean canDragAndDrop(org.netbeans.jemmy.operators.ComponentOperator)
meth protected abstract boolean canJump(org.netbeans.jemmy.operators.ComponentOperator)
meth protected abstract boolean canPushAndWait(org.netbeans.jemmy.operators.ComponentOperator)
meth protected abstract int getDragAndDropStepLength(org.netbeans.jemmy.operators.ComponentOperator)
meth protected abstract int position(org.netbeans.jemmy.operators.ComponentOperator,int)
meth protected abstract java.awt.Point startDragging(org.netbeans.jemmy.operators.ComponentOperator)
meth protected abstract org.netbeans.jemmy.Timeout getScrollDeltaTimeout(org.netbeans.jemmy.operators.ComponentOperator)
meth protected abstract void drag(org.netbeans.jemmy.operators.ComponentOperator,java.awt.Point)
meth protected abstract void drop(org.netbeans.jemmy.operators.ComponentOperator,java.awt.Point)
meth protected abstract void jump(org.netbeans.jemmy.operators.ComponentOperator,org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster)
meth protected abstract void startPushAndWait(org.netbeans.jemmy.operators.ComponentOperator,int,int)
meth protected abstract void step(org.netbeans.jemmy.operators.ComponentOperator,org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster)
meth protected abstract void stopPushAndWait(org.netbeans.jemmy.operators.ComponentOperator,int,int)
meth protected boolean doPushAndWait(org.netbeans.jemmy.operators.ComponentOperator,org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster,long)
meth protected void doDragAndDrop(org.netbeans.jemmy.operators.ComponentOperator,org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster)
meth protected void doJumps(org.netbeans.jemmy.operators.ComponentOperator,org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster)
meth protected void doSteps(org.netbeans.jemmy.operators.ComponentOperator,org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster)
meth public void scroll(org.netbeans.jemmy.operators.ComponentOperator,org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster)
supr org.netbeans.jemmy.drivers.LightSupportiveDriver

CLSS public org.netbeans.jemmy.drivers.scrolling.JScrollBarAPIDriver
cons public init()
meth protected boolean canDragAndDrop(org.netbeans.jemmy.operators.ComponentOperator)
meth protected boolean canJump(org.netbeans.jemmy.operators.ComponentOperator)
meth protected boolean canPushAndWait(org.netbeans.jemmy.operators.ComponentOperator)
meth protected int getDragAndDropStepLength(org.netbeans.jemmy.operators.ComponentOperator)
meth protected int position(org.netbeans.jemmy.operators.ComponentOperator,int)
meth protected java.awt.Point startDragging(org.netbeans.jemmy.operators.ComponentOperator)
meth protected org.netbeans.jemmy.Timeout getScrollDeltaTimeout(org.netbeans.jemmy.operators.ComponentOperator)
meth protected void drag(org.netbeans.jemmy.operators.ComponentOperator,java.awt.Point)
meth protected void drop(org.netbeans.jemmy.operators.ComponentOperator,java.awt.Point)
meth protected void jump(org.netbeans.jemmy.operators.ComponentOperator,org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster)
meth protected void startPushAndWait(org.netbeans.jemmy.operators.ComponentOperator,int,int)
meth protected void step(org.netbeans.jemmy.operators.ComponentOperator,org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster)
meth protected void stopPushAndWait(org.netbeans.jemmy.operators.ComponentOperator,int,int)
meth public void scrollToMaximum(org.netbeans.jemmy.operators.ComponentOperator,int)
meth public void scrollToMinimum(org.netbeans.jemmy.operators.ComponentOperator,int)
supr org.netbeans.jemmy.drivers.scrolling.AbstractScrollDriver
hfds SMALL_INCREMENT

CLSS public org.netbeans.jemmy.drivers.scrolling.JScrollBarDriver
cons public init()
meth protected boolean canDragAndDrop(org.netbeans.jemmy.operators.ComponentOperator)
meth protected boolean canJump(org.netbeans.jemmy.operators.ComponentOperator)
meth protected boolean canPushAndWait(org.netbeans.jemmy.operators.ComponentOperator)
meth protected int getDragAndDropStepLength(org.netbeans.jemmy.operators.ComponentOperator)
meth protected int position(org.netbeans.jemmy.operators.ComponentOperator,int)
meth protected java.awt.Point startDragging(org.netbeans.jemmy.operators.ComponentOperator)
meth protected org.netbeans.jemmy.Timeout getScrollDeltaTimeout(org.netbeans.jemmy.operators.ComponentOperator)
meth protected void drag(org.netbeans.jemmy.operators.ComponentOperator,java.awt.Point)
meth protected void drop(org.netbeans.jemmy.operators.ComponentOperator,java.awt.Point)
meth protected void jump(org.netbeans.jemmy.operators.ComponentOperator,org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster)
meth protected void startPushAndWait(org.netbeans.jemmy.operators.ComponentOperator,int,int)
meth protected void step(org.netbeans.jemmy.operators.ComponentOperator,org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster)
meth protected void stopPushAndWait(org.netbeans.jemmy.operators.ComponentOperator,int,int)
meth public void scrollToMaximum(org.netbeans.jemmy.operators.ComponentOperator,int)
meth public void scrollToMinimum(org.netbeans.jemmy.operators.ComponentOperator,int)
supr org.netbeans.jemmy.drivers.scrolling.AbstractScrollDriver
hfds MINIMAL_DRAGGER_SIZE,RELATIVE_DRAG_STEP_LENGTH,SMALL_INCREMENT,queueTool

CLSS public org.netbeans.jemmy.drivers.scrolling.JSliderAPIDriver
cons public init()
meth protected boolean canDragAndDrop(org.netbeans.jemmy.operators.ComponentOperator)
meth protected boolean canJump(org.netbeans.jemmy.operators.ComponentOperator)
meth protected boolean canPushAndWait(org.netbeans.jemmy.operators.ComponentOperator)
meth protected int getDragAndDropStepLength(org.netbeans.jemmy.operators.ComponentOperator)
meth protected int position(org.netbeans.jemmy.operators.ComponentOperator,int)
meth protected java.awt.Point startDragging(org.netbeans.jemmy.operators.ComponentOperator)
meth protected org.netbeans.jemmy.Timeout getScrollDeltaTimeout(org.netbeans.jemmy.operators.ComponentOperator)
meth protected void drag(org.netbeans.jemmy.operators.ComponentOperator,java.awt.Point)
meth protected void drop(org.netbeans.jemmy.operators.ComponentOperator,java.awt.Point)
meth protected void jump(org.netbeans.jemmy.operators.ComponentOperator,org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster)
meth protected void startPushAndWait(org.netbeans.jemmy.operators.ComponentOperator,int,int)
meth protected void step(org.netbeans.jemmy.operators.ComponentOperator,org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster)
meth protected void stopPushAndWait(org.netbeans.jemmy.operators.ComponentOperator,int,int)
meth public void scrollToMaximum(org.netbeans.jemmy.operators.ComponentOperator,int)
meth public void scrollToMinimum(org.netbeans.jemmy.operators.ComponentOperator,int)
supr org.netbeans.jemmy.drivers.scrolling.AbstractScrollDriver
hfds SMALL_INCREMENT

CLSS public org.netbeans.jemmy.drivers.scrolling.JSliderDriver
cons public init()
meth protected boolean canDragAndDrop(org.netbeans.jemmy.operators.ComponentOperator)
meth protected boolean canJump(org.netbeans.jemmy.operators.ComponentOperator)
meth protected boolean canPushAndWait(org.netbeans.jemmy.operators.ComponentOperator)
meth protected int getDragAndDropStepLength(org.netbeans.jemmy.operators.ComponentOperator)
meth protected int position(org.netbeans.jemmy.operators.ComponentOperator,int)
meth protected java.awt.Point startDragging(org.netbeans.jemmy.operators.ComponentOperator)
meth protected org.netbeans.jemmy.Timeout getScrollDeltaTimeout(org.netbeans.jemmy.operators.ComponentOperator)
meth protected void drag(org.netbeans.jemmy.operators.ComponentOperator,java.awt.Point)
meth protected void drop(org.netbeans.jemmy.operators.ComponentOperator,java.awt.Point)
meth protected void jump(org.netbeans.jemmy.operators.ComponentOperator,org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster)
meth protected void startPushAndWait(org.netbeans.jemmy.operators.ComponentOperator,int,int)
meth protected void step(org.netbeans.jemmy.operators.ComponentOperator,org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster)
meth protected void stopPushAndWait(org.netbeans.jemmy.operators.ComponentOperator,int,int)
meth public void scrollToMaximum(org.netbeans.jemmy.operators.ComponentOperator,int)
meth public void scrollToMinimum(org.netbeans.jemmy.operators.ComponentOperator,int)
supr org.netbeans.jemmy.drivers.scrolling.AbstractScrollDriver
hfds queueTool

CLSS public org.netbeans.jemmy.drivers.scrolling.JSpinnerDriver
cons public init()
intf org.netbeans.jemmy.drivers.ScrollDriver
meth public void scroll(org.netbeans.jemmy.operators.ComponentOperator,org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster)
meth public void scrollToMaximum(org.netbeans.jemmy.operators.ComponentOperator,int)
meth public void scrollToMinimum(org.netbeans.jemmy.operators.ComponentOperator,int)
supr org.netbeans.jemmy.drivers.LightSupportiveDriver

CLSS public org.netbeans.jemmy.drivers.scrolling.JSplitPaneDriver
cons public init()
intf org.netbeans.jemmy.drivers.ScrollDriver
meth public void scroll(org.netbeans.jemmy.operators.ComponentOperator,org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster)
meth public void scrollToMaximum(org.netbeans.jemmy.operators.ComponentOperator,int)
meth public void scrollToMinimum(org.netbeans.jemmy.operators.ComponentOperator,int)
supr org.netbeans.jemmy.drivers.LightSupportiveDriver

CLSS public org.netbeans.jemmy.drivers.scrolling.KeyboardJSliderScrollDriver
cons public init()
meth protected boolean doPushAndWait(org.netbeans.jemmy.operators.ComponentOperator,org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster,long)
meth protected void step(org.netbeans.jemmy.operators.ComponentOperator,org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster)
supr org.netbeans.jemmy.drivers.scrolling.JSliderDriver

CLSS public abstract interface org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster
fld public final static int DECREASE_SCROLL_DIRECTION = -1
fld public final static int DO_NOT_TOUCH_SCROLL_DIRECTION = 0
fld public final static int INCREASE_SCROLL_DIRECTION = 1
meth public abstract int getScrollDirection()
meth public abstract int getScrollOrientation()
meth public abstract java.lang.String getDescription()

CLSS public org.netbeans.jemmy.drivers.scrolling.ScrollPaneDriver
cons public init()
meth protected int position(org.netbeans.jemmy.operators.ComponentOperator,int)
meth protected java.awt.Point getClickPoint(org.netbeans.jemmy.operators.ComponentOperator,int,int)
meth public void scrollToMaximum(org.netbeans.jemmy.operators.ComponentOperator,int)
meth public void scrollToMinimum(org.netbeans.jemmy.operators.ComponentOperator,int)
supr org.netbeans.jemmy.drivers.scrolling.AWTScrollDriver
hfds CLICK_OFFSET

CLSS public org.netbeans.jemmy.drivers.scrolling.ScrollbarDriver
cons public init()
meth protected int position(org.netbeans.jemmy.operators.ComponentOperator,int)
meth protected java.awt.Point getClickPoint(org.netbeans.jemmy.operators.ComponentOperator,int,int)
meth public void scrollToMaximum(org.netbeans.jemmy.operators.ComponentOperator,int)
meth public void scrollToMinimum(org.netbeans.jemmy.operators.ComponentOperator,int)
supr org.netbeans.jemmy.drivers.scrolling.AWTScrollDriver
hfds CLICK_OFFSET

CLSS public org.netbeans.jemmy.drivers.tables.JTableMouseDriver
cons public init()
intf org.netbeans.jemmy.drivers.TableDriver
meth protected void clickOnCell(org.netbeans.jemmy.operators.JTableOperator,int,int,int)
meth public void editCell(org.netbeans.jemmy.operators.ComponentOperator,int,int,java.lang.Object)
meth public void selectCell(org.netbeans.jemmy.operators.ComponentOperator,int,int)
supr org.netbeans.jemmy.drivers.LightSupportiveDriver
hfds queueTool

CLSS public org.netbeans.jemmy.drivers.text.AWTTextAPIDriver
cons public init()
meth public int getCaretPosition(org.netbeans.jemmy.operators.ComponentOperator)
meth public int getSelectionEnd(org.netbeans.jemmy.operators.ComponentOperator)
meth public int getSelectionStart(org.netbeans.jemmy.operators.ComponentOperator)
meth public java.lang.String getText(org.netbeans.jemmy.operators.ComponentOperator)
supr org.netbeans.jemmy.drivers.text.TextAPIDriver

CLSS public org.netbeans.jemmy.drivers.text.AWTTextKeyboardDriver
cons public init()
meth public int getCaretPosition(org.netbeans.jemmy.operators.ComponentOperator)
meth public int getSelectionEnd(org.netbeans.jemmy.operators.ComponentOperator)
meth public int getSelectionStart(org.netbeans.jemmy.operators.ComponentOperator)
meth public java.lang.Object getKeys(org.netbeans.jemmy.operators.ComponentOperator)
meth public java.lang.String getText(org.netbeans.jemmy.operators.ComponentOperator)
meth public org.netbeans.jemmy.Timeout getBetweenTimeout(org.netbeans.jemmy.operators.ComponentOperator)
supr org.netbeans.jemmy.drivers.text.TextKeyboardDriver

CLSS public org.netbeans.jemmy.drivers.text.SwingTextAPIDriver
cons public init()
meth public int getCaretPosition(org.netbeans.jemmy.operators.ComponentOperator)
meth public int getSelectionEnd(org.netbeans.jemmy.operators.ComponentOperator)
meth public int getSelectionStart(org.netbeans.jemmy.operators.ComponentOperator)
meth public java.lang.String getText(org.netbeans.jemmy.operators.ComponentOperator)
supr org.netbeans.jemmy.drivers.text.TextAPIDriver

CLSS public org.netbeans.jemmy.drivers.text.SwingTextKeyboardDriver
cons public init()
meth public int getCaretPosition(org.netbeans.jemmy.operators.ComponentOperator)
meth public int getSelectionEnd(org.netbeans.jemmy.operators.ComponentOperator)
meth public int getSelectionStart(org.netbeans.jemmy.operators.ComponentOperator)
meth public java.lang.Object getKeys(org.netbeans.jemmy.operators.ComponentOperator)
meth public java.lang.String getText(org.netbeans.jemmy.operators.ComponentOperator)
meth public org.netbeans.jemmy.Timeout getBetweenTimeout(org.netbeans.jemmy.operators.ComponentOperator)
meth public void clearText(org.netbeans.jemmy.operators.ComponentOperator)
supr org.netbeans.jemmy.drivers.text.TextKeyboardDriver

CLSS public abstract org.netbeans.jemmy.drivers.text.TextAPIDriver
cons public init(java.lang.String[])
intf org.netbeans.jemmy.drivers.TextDriver
meth public abstract int getCaretPosition(org.netbeans.jemmy.operators.ComponentOperator)
meth public abstract int getSelectionEnd(org.netbeans.jemmy.operators.ComponentOperator)
meth public abstract int getSelectionStart(org.netbeans.jemmy.operators.ComponentOperator)
meth public abstract java.lang.String getText(org.netbeans.jemmy.operators.ComponentOperator)
meth public void changeCaretPosition(org.netbeans.jemmy.operators.ComponentOperator,int)
meth public void changeText(org.netbeans.jemmy.operators.ComponentOperator,java.lang.String)
meth public void clearText(org.netbeans.jemmy.operators.ComponentOperator)
meth public void enterText(org.netbeans.jemmy.operators.ComponentOperator,java.lang.String)
meth public void selectText(org.netbeans.jemmy.operators.ComponentOperator,int,int)
meth public void typeText(org.netbeans.jemmy.operators.ComponentOperator,java.lang.String,int)
supr org.netbeans.jemmy.drivers.LightSupportiveDriver

CLSS public abstract org.netbeans.jemmy.drivers.text.TextKeyboardDriver
cons public init(java.lang.String[])
intf org.netbeans.jemmy.drivers.TextDriver
meth protected void changeCaretPosition(org.netbeans.jemmy.operators.ComponentOperator,int,int)
meth public abstract int getCaretPosition(org.netbeans.jemmy.operators.ComponentOperator)
meth public abstract int getSelectionEnd(org.netbeans.jemmy.operators.ComponentOperator)
meth public abstract int getSelectionStart(org.netbeans.jemmy.operators.ComponentOperator)
meth public abstract java.lang.Object getKeys(org.netbeans.jemmy.operators.ComponentOperator)
meth public abstract java.lang.String getText(org.netbeans.jemmy.operators.ComponentOperator)
meth public abstract org.netbeans.jemmy.Timeout getBetweenTimeout(org.netbeans.jemmy.operators.ComponentOperator)
meth public void changeCaretPosition(org.netbeans.jemmy.operators.ComponentOperator,int)
meth public void changeText(org.netbeans.jemmy.operators.ComponentOperator,java.lang.String)
meth public void clearText(org.netbeans.jemmy.operators.ComponentOperator)
meth public void enterText(org.netbeans.jemmy.operators.ComponentOperator,java.lang.String)
meth public void selectText(org.netbeans.jemmy.operators.ComponentOperator,int,int)
meth public void typeText(org.netbeans.jemmy.operators.ComponentOperator,java.lang.String,int)
supr org.netbeans.jemmy.drivers.LightSupportiveDriver

CLSS public org.netbeans.jemmy.drivers.trees.JTreeAPIDriver
cons public init()
intf org.netbeans.jemmy.drivers.TreeDriver
meth public void collapseItem(org.netbeans.jemmy.operators.ComponentOperator,int)
meth public void editItem(org.netbeans.jemmy.operators.ComponentOperator,int,java.lang.Object,org.netbeans.jemmy.Timeout)
meth public void expandItem(org.netbeans.jemmy.operators.ComponentOperator,int)
meth public void selectItem(org.netbeans.jemmy.operators.ComponentOperator,int)
meth public void selectItems(org.netbeans.jemmy.operators.ComponentOperator,int[])
meth public void startEditing(org.netbeans.jemmy.operators.ComponentOperator,int,org.netbeans.jemmy.Timeout)
supr org.netbeans.jemmy.drivers.LightSupportiveDriver

CLSS public org.netbeans.jemmy.drivers.trees.JTreeMouseDriver
cons public init()
intf org.netbeans.jemmy.drivers.TreeDriver
meth public void collapseItem(org.netbeans.jemmy.operators.ComponentOperator,int)
meth public void editItem(org.netbeans.jemmy.operators.ComponentOperator,int,java.lang.Object,org.netbeans.jemmy.Timeout)
meth public void expandItem(org.netbeans.jemmy.operators.ComponentOperator,int)
meth public void selectItem(org.netbeans.jemmy.operators.ComponentOperator,int)
meth public void selectItems(org.netbeans.jemmy.operators.ComponentOperator,int[])
meth public void startEditing(org.netbeans.jemmy.operators.ComponentOperator,int,org.netbeans.jemmy.Timeout)
supr org.netbeans.jemmy.drivers.LightSupportiveDriver
hfds queueTool

CLSS public org.netbeans.jemmy.drivers.windows.DefaultFrameDriver
cons public init()
intf org.netbeans.jemmy.drivers.FrameDriver
meth public void deiconify(org.netbeans.jemmy.operators.ComponentOperator)
meth public void demaximize(org.netbeans.jemmy.operators.ComponentOperator)
meth public void iconify(org.netbeans.jemmy.operators.ComponentOperator)
meth public void maximize(org.netbeans.jemmy.operators.ComponentOperator)
supr org.netbeans.jemmy.drivers.LightSupportiveDriver
hfds eDriver

CLSS public org.netbeans.jemmy.drivers.windows.DefaultInternalFrameDriver
cons public init()
intf org.netbeans.jemmy.drivers.FrameDriver
intf org.netbeans.jemmy.drivers.InternalFrameDriver
intf org.netbeans.jemmy.drivers.WindowDriver
meth public java.awt.Component getTitlePane(org.netbeans.jemmy.operators.ComponentOperator)
meth public void activate(org.netbeans.jemmy.operators.ComponentOperator)
meth public void close(org.netbeans.jemmy.operators.ComponentOperator)
meth public void deiconify(org.netbeans.jemmy.operators.ComponentOperator)
meth public void demaximize(org.netbeans.jemmy.operators.ComponentOperator)
meth public void iconify(org.netbeans.jemmy.operators.ComponentOperator)
meth public void maximize(org.netbeans.jemmy.operators.ComponentOperator)
meth public void move(org.netbeans.jemmy.operators.ComponentOperator,int,int)
meth public void requestClose(org.netbeans.jemmy.operators.ComponentOperator)
meth public void requestCloseAndThenHide(org.netbeans.jemmy.operators.ComponentOperator)
meth public void resize(org.netbeans.jemmy.operators.ComponentOperator,int,int)
supr org.netbeans.jemmy.drivers.LightSupportiveDriver

CLSS public org.netbeans.jemmy.drivers.windows.DefaultWindowDriver
cons public init()
intf org.netbeans.jemmy.drivers.WindowDriver
meth public void activate(org.netbeans.jemmy.operators.ComponentOperator)
meth public void close(org.netbeans.jemmy.operators.ComponentOperator)
meth public void move(org.netbeans.jemmy.operators.ComponentOperator,int,int)
meth public void requestClose(org.netbeans.jemmy.operators.ComponentOperator)
meth public void requestCloseAndThenHide(org.netbeans.jemmy.operators.ComponentOperator)
meth public void resize(org.netbeans.jemmy.operators.ComponentOperator,int,int)
supr org.netbeans.jemmy.drivers.LightSupportiveDriver
hfds eDriver

CLSS public org.netbeans.jemmy.explorer.GUIBrowser
meth public static void main(java.lang.String[])
meth public static void showBrowser()
meth public void setStatus(java.lang.String)
meth public void show()
supr javax.swing.JFrame
hfds COMPONENTS_TAB,EVENT_TAB,IMAGE_TAB,PROPERTIES_TAB,REFLECTION_TAB,WINDOWS_TAB,dumpData,dumpWriter,exit,expandButton,mainTree,propDialog,qt,refreshDelay,root,shown,split,status,viewButton
hcls ClassModel,ClassNode,ComponentBrowser,ComponentImageProvider,ComponentModel,ComponentNode,ContainerNode,FieldNode,ImagePane,InterfaceNode,ListListener,MethodNode,MyModel,PropertyDialog,RootNode,SelectionManager,SuperClassNode,ToStringListener,WindowModel,WindowNode,WindowRenderer

CLSS public org.netbeans.jemmy.explorer.TrialListenerManager
cons public init(java.awt.Component)
intf org.netbeans.jemmy.Outputable
meth public org.netbeans.jemmy.TestOut getOutput()
meth public void addKeyListener()
meth public void addMouseListener()
meth public void addMouseMotionListener()
meth public void removeKeyListener()
meth public void removeMouseListener()
meth public void removeMouseMotionListener()
meth public void setOutput(org.netbeans.jemmy.TestOut)
supr java.lang.Object
hfds comp,kListener,mListener,mmListener,output
hcls TrialKeyListener,TrialMouseListener,TrialMouseMotionListener

CLSS public org.netbeans.jemmy.image.ColorImageComparator
cons public init(org.netbeans.jemmy.image.ColorImageComparator$ColorMap)
cons public init(org.netbeans.jemmy.image.ColorImageComparator$ColorMap,org.netbeans.jemmy.image.ColorImageComparator$ColorMap)
cons public init(org.netbeans.jemmy.image.ColorImageComparator$ColorMap,org.netbeans.jemmy.image.ColorImageComparator$ColorMap,org.netbeans.jemmy.image.ImageComparator)
cons public init(org.netbeans.jemmy.image.ColorImageComparator$ColorMap,org.netbeans.jemmy.image.ImageComparator)
innr public abstract interface static ColorMap
innr public static BackgroundColorMap
innr public static ForegroundColorMap
meth protected final boolean compareColors(int,int)
meth public boolean compare(java.awt.image.BufferedImage,java.awt.image.BufferedImage)
supr org.netbeans.jemmy.image.StrictImageComparator
hfds comparator,leftMap,rightMap

CLSS public static org.netbeans.jemmy.image.ColorImageComparator$BackgroundColorMap
 outer org.netbeans.jemmy.image.ColorImageComparator
cons public init(int)
intf org.netbeans.jemmy.image.ColorImageComparator$ColorMap
meth public int mapColor(int)
supr java.lang.Object
hfds background

CLSS public abstract interface static org.netbeans.jemmy.image.ColorImageComparator$ColorMap
 outer org.netbeans.jemmy.image.ColorImageComparator
meth public abstract int mapColor(int)

CLSS public static org.netbeans.jemmy.image.ColorImageComparator$ForegroundColorMap
 outer org.netbeans.jemmy.image.ColorImageComparator
cons public init(int)
intf org.netbeans.jemmy.image.ColorImageComparator$ColorMap
meth public int mapColor(int)
supr java.lang.Object
hfds foreground

CLSS public org.netbeans.jemmy.image.FileImageComparator
cons public init(org.netbeans.jemmy.image.ImageComparator,org.netbeans.jemmy.image.ImageLoader)
meth public boolean compare(java.awt.image.BufferedImage,java.lang.String)
meth public boolean compare(java.lang.String,java.lang.String)
supr java.lang.Object
hfds comparator,loader

CLSS public abstract interface org.netbeans.jemmy.image.ImageComparator
meth public abstract boolean compare(java.awt.image.BufferedImage,java.awt.image.BufferedImage)

CLSS public abstract interface org.netbeans.jemmy.image.ImageFinder
meth public abstract java.awt.Point findImage(java.awt.image.BufferedImage,int)

CLSS public abstract interface org.netbeans.jemmy.image.ImageLoader
meth public abstract java.awt.image.BufferedImage load(java.lang.String) throws java.io.IOException

CLSS public abstract interface org.netbeans.jemmy.image.ImageSaver
meth public abstract void save(java.awt.image.BufferedImage,java.lang.String) throws java.io.IOException

CLSS public org.netbeans.jemmy.image.ImageTool
cons public init()
meth public static java.awt.image.BufferedImage enlargeImage(java.awt.image.BufferedImage,int)
meth public static java.awt.image.BufferedImage getImage()
meth public static java.awt.image.BufferedImage getImage(java.awt.Component)
meth public static java.awt.image.BufferedImage getImage(java.awt.Rectangle)
meth public static java.awt.image.BufferedImage substractImage(java.awt.image.BufferedImage,java.awt.image.BufferedImage)
meth public static java.awt.image.BufferedImage substractImage(java.awt.image.BufferedImage,java.awt.image.BufferedImage,int,int)
meth public static java.awt.image.BufferedImage subtractImage(java.awt.image.BufferedImage,java.awt.image.BufferedImage)
meth public static java.awt.image.BufferedImage subtractImage(java.awt.image.BufferedImage,java.awt.image.BufferedImage,int,int)
supr java.lang.Object

CLSS public org.netbeans.jemmy.image.PNGImageLoader
cons public init()
intf org.netbeans.jemmy.image.ImageLoader
meth public java.awt.image.BufferedImage load(java.lang.String) throws java.io.IOException
supr java.lang.Object

CLSS public org.netbeans.jemmy.image.PNGImageSaver
cons public init()
intf org.netbeans.jemmy.image.ImageSaver
meth public void save(java.awt.image.BufferedImage,java.lang.String) throws java.io.IOException
supr java.lang.Object

CLSS public org.netbeans.jemmy.image.RoughImageComparator
cons public init(double)
intf org.netbeans.jemmy.image.ImageComparator
meth public boolean compare(java.awt.image.BufferedImage,java.awt.image.BufferedImage)
supr java.lang.Object
hfds roughness

CLSS public org.netbeans.jemmy.image.RoughImageFinder
cons public init(java.awt.image.BufferedImage,double)
intf org.netbeans.jemmy.image.ImageFinder
meth public java.awt.Point findImage(java.awt.image.BufferedImage,int)
supr java.lang.Object
hfds bigHeight,bigPixels,bigWidth,roughness

CLSS public org.netbeans.jemmy.image.StrictImageComparator
cons public init()
intf org.netbeans.jemmy.image.ImageComparator
meth protected boolean compareColors(int,int)
meth public boolean compare(java.awt.image.BufferedImage,java.awt.image.BufferedImage)
supr java.lang.Object

CLSS public org.netbeans.jemmy.image.StrictImageFinder
cons public init(java.awt.image.BufferedImage)
intf org.netbeans.jemmy.image.ImageFinder
meth public java.awt.Point findImage(java.awt.image.BufferedImage,int)
supr java.lang.Object
hfds bigHeight,bigPixels,bigWidth

CLSS public org.netbeans.jemmy.operators.AbstractButtonOperator
cons public init(javax.swing.AbstractButton)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static java.lang.String IS_SELECTED_DPROP = "Selected"
fld public final static java.lang.String TEXT_DPROP = "Text"
innr public static AbstractButtonByLabelFinder
innr public static AbstractButtonFinder
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth public boolean isBorderPainted()
meth public boolean isContentAreaFilled()
meth public boolean isFocusPainted()
meth public boolean isRolloverEnabled()
meth public boolean isSelected()
meth public int getHorizontalAlignment()
meth public int getHorizontalTextPosition()
meth public int getMnemonic()
meth public int getVerticalAlignment()
meth public int getVerticalTextPosition()
meth public java.awt.Insets getMargin()
meth public java.lang.Object[] getSelectedObjects()
meth public java.lang.String getActionCommand()
meth public java.lang.String getText()
meth public java.util.Hashtable getDump()
meth public javax.swing.ButtonModel getModel()
meth public javax.swing.Icon getDisabledIcon()
meth public javax.swing.Icon getDisabledSelectedIcon()
meth public javax.swing.Icon getIcon()
meth public javax.swing.Icon getPressedIcon()
meth public javax.swing.Icon getRolloverIcon()
meth public javax.swing.Icon getRolloverSelectedIcon()
meth public javax.swing.Icon getSelectedIcon()
meth public javax.swing.plaf.ButtonUI getUI()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public static javax.swing.AbstractButton findAbstractButton(java.awt.Container,java.lang.String,boolean,boolean)
meth public static javax.swing.AbstractButton findAbstractButton(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.AbstractButton findAbstractButton(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.AbstractButton findAbstractButton(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.AbstractButton waitAbstractButton(java.awt.Container,java.lang.String,boolean,boolean)
meth public static javax.swing.AbstractButton waitAbstractButton(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.AbstractButton waitAbstractButton(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.AbstractButton waitAbstractButton(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void addActionListener(java.awt.event.ActionListener)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void addItemListener(java.awt.event.ItemListener)
meth public void changeSelection(boolean)
meth public void changeSelectionNoBlock(boolean)
meth public void copyEnvironment(org.netbeans.jemmy.operators.Operator)
meth public void doClick()
meth public void doClick(int)
meth public void press()
meth public void push()
meth public void pushNoBlock()
meth public void release()
meth public void removeActionListener(java.awt.event.ActionListener)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void removeItemListener(java.awt.event.ItemListener)
meth public void setActionCommand(java.lang.String)
meth public void setBorderPainted(boolean)
meth public void setContentAreaFilled(boolean)
meth public void setDisabledIcon(javax.swing.Icon)
meth public void setDisabledSelectedIcon(javax.swing.Icon)
meth public void setFocusPainted(boolean)
meth public void setHorizontalAlignment(int)
meth public void setHorizontalTextPosition(int)
meth public void setIcon(javax.swing.Icon)
meth public void setMargin(java.awt.Insets)
meth public void setMnemonic(char)
meth public void setMnemonic(int)
meth public void setModel(javax.swing.ButtonModel)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setPressedIcon(javax.swing.Icon)
meth public void setRolloverEnabled(boolean)
meth public void setRolloverIcon(javax.swing.Icon)
meth public void setRolloverSelectedIcon(javax.swing.Icon)
meth public void setSelected(boolean)
meth public void setSelectedIcon(javax.swing.Icon)
meth public void setText(java.lang.String)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
meth public void setUI(javax.swing.plaf.ButtonUI)
meth public void setVerticalAlignment(int)
meth public void setVerticalTextPosition(int)
meth public void waitSelected(boolean)
meth public void waitText(java.lang.String)
supr org.netbeans.jemmy.operators.JComponentOperator
hfds PUSH_BUTTON_TIMEOUT,driver,output,timeouts

CLSS public static org.netbeans.jemmy.operators.AbstractButtonOperator$AbstractButtonByLabelFinder
 outer org.netbeans.jemmy.operators.AbstractButtonOperator
cons public init(java.lang.String)
cons public init(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
intf org.netbeans.jemmy.ComponentChooser
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
supr java.lang.Object
hfds comparator,label

CLSS public static org.netbeans.jemmy.operators.AbstractButtonOperator$AbstractButtonFinder
 outer org.netbeans.jemmy.operators.AbstractButtonOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.ButtonOperator
cons public init(java.awt.Button)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static java.lang.String TEXT_DPROP = "Label"
innr public static ButtonByLabelFinder
innr public static ButtonFinder
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth public java.lang.String getActionCommand()
meth public java.lang.String getLabel()
meth public java.util.Hashtable getDump()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public static java.awt.Button findButton(java.awt.Container,java.lang.String,boolean,boolean)
meth public static java.awt.Button findButton(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static java.awt.Button findButton(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.Button findButton(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static java.awt.Button waitButton(java.awt.Container,java.lang.String,boolean,boolean)
meth public static java.awt.Button waitButton(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static java.awt.Button waitButton(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.Button waitButton(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void addActionListener(java.awt.event.ActionListener)
meth public void copyEnvironment(org.netbeans.jemmy.operators.Operator)
meth public void press()
meth public void push()
meth public void pushNoBlock()
meth public void release()
meth public void removeActionListener(java.awt.event.ActionListener)
meth public void setActionCommand(java.lang.String)
meth public void setLabel(java.lang.String)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
supr org.netbeans.jemmy.operators.ComponentOperator
hfds PUSH_BUTTON_TIMEOUT,driver,output,timeouts

CLSS public static org.netbeans.jemmy.operators.ButtonOperator$ButtonByLabelFinder
 outer org.netbeans.jemmy.operators.ButtonOperator
cons public init(java.lang.String)
cons public init(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
intf org.netbeans.jemmy.ComponentChooser
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
supr java.lang.Object
hfds comparator,label

CLSS public static org.netbeans.jemmy.operators.ButtonOperator$ButtonFinder
 outer org.netbeans.jemmy.operators.ButtonOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.CheckboxOperator
cons public init(java.awt.Checkbox)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static java.lang.String TEXT_DPROP = "Label"
innr public static CheckboxByLabelFinder
innr public static CheckboxFinder
intf org.netbeans.jemmy.Outputable
meth public boolean getState()
meth public java.awt.CheckboxGroup getCheckboxGroup()
meth public java.lang.String getLabel()
meth public java.util.Hashtable getDump()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public static java.awt.Checkbox findCheckbox(java.awt.Container,java.lang.String,boolean,boolean)
meth public static java.awt.Checkbox findCheckbox(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static java.awt.Checkbox findCheckbox(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.Checkbox findCheckbox(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static java.awt.Checkbox waitCheckbox(java.awt.Container,java.lang.String,boolean,boolean)
meth public static java.awt.Checkbox waitCheckbox(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static java.awt.Checkbox waitCheckbox(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.Checkbox waitCheckbox(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void addItemListener(java.awt.event.ItemListener)
meth public void changeSelection(boolean)
meth public void changeSelectionNoBlock(boolean)
meth public void copyEnvironment(org.netbeans.jemmy.operators.Operator)
meth public void removeItemListener(java.awt.event.ItemListener)
meth public void setCheckboxGroup(java.awt.CheckboxGroup)
meth public void setLabel(java.lang.String)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setState(boolean)
meth public void waitSelected(boolean)
supr org.netbeans.jemmy.operators.ComponentOperator
hfds driver,output

CLSS public static org.netbeans.jemmy.operators.CheckboxOperator$CheckboxByLabelFinder
 outer org.netbeans.jemmy.operators.CheckboxOperator
cons public init(java.lang.String)
cons public init(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
intf org.netbeans.jemmy.ComponentChooser
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
supr java.lang.Object
hfds comparator,label

CLSS public static org.netbeans.jemmy.operators.CheckboxOperator$CheckboxFinder
 outer org.netbeans.jemmy.operators.CheckboxOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.ChoiceOperator
cons public init(java.awt.Choice)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static java.lang.String ITEM_PREFIX_DPROP = "Item"
fld public final static java.lang.String SELECTED_ITEM_DPROP = "Selected item"
innr public static ChoiceBySelectedItemFinder
innr public static ChoiceFinder
intf org.netbeans.jemmy.Outputable
meth public int findItemIndex(java.lang.String)
meth public int findItemIndex(java.lang.String,int)
meth public int getItemCount()
meth public int getSelectedIndex()
meth public java.lang.String getItem(int)
meth public java.lang.String getSelectedItem()
meth public java.util.Hashtable getDump()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public static java.awt.Choice findChoice(java.awt.Container,java.lang.String,boolean,boolean)
meth public static java.awt.Choice findChoice(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static java.awt.Choice findChoice(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.Choice findChoice(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static java.awt.Choice waitChoice(java.awt.Container,java.lang.String,boolean,boolean)
meth public static java.awt.Choice waitChoice(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static java.awt.Choice waitChoice(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.Choice waitChoice(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void add(java.lang.String)
meth public void addItemListener(java.awt.event.ItemListener)
meth public void addNotify()
meth public void copyEnvironment(org.netbeans.jemmy.operators.Operator)
meth public void insert(java.lang.String,int)
meth public void remove(int)
meth public void remove(java.lang.String)
meth public void removeAll()
meth public void removeItemListener(java.awt.event.ItemListener)
meth public void select(int)
meth public void selectItem(int)
meth public void selectItem(java.lang.String)
meth public void selectItem(java.lang.String,int)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setState(java.lang.String)
meth public void waitItemSelected(int)
supr org.netbeans.jemmy.operators.ComponentOperator
hfds driver,output

CLSS public static org.netbeans.jemmy.operators.ChoiceOperator$ChoiceBySelectedItemFinder
 outer org.netbeans.jemmy.operators.ChoiceOperator
cons public init(java.lang.String)
cons public init(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
intf org.netbeans.jemmy.ComponentChooser
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
supr java.lang.Object
hfds comparator,label

CLSS public static org.netbeans.jemmy.operators.ChoiceOperator$ChoiceFinder
 outer org.netbeans.jemmy.operators.ChoiceOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.ComponentOperator
cons public init(java.awt.Component)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static java.lang.String HEIGHT_DPROP = "Height"
fld public final static java.lang.String IS_SHOWING_DPROP = "Showing"
fld public final static java.lang.String IS_VISIBLE_DPROP = "Visible"
fld public final static java.lang.String NAME_DPROP = "Name:"
fld public final static java.lang.String WIDTH_DPROP = "Width"
fld public final static java.lang.String X_DPROP = "X"
fld public final static java.lang.String Y_DPROP = "Y"
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth protected static java.awt.Component waitComponent(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int,org.netbeans.jemmy.Timeouts,org.netbeans.jemmy.TestOut)
meth protected static java.awt.Component waitComponent(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
meth public boolean contains(int,int)
meth public boolean contains(java.awt.Point)
meth public boolean hasFocus()
meth public boolean imageUpdate(java.awt.Image,int,int,int,int,int)
meth public boolean isDisplayable()
meth public boolean isDoubleBuffered()
meth public boolean isEnabled()
meth public boolean isFocusTraversable()
meth public boolean isLightweight()
meth public boolean isOpaque()
meth public boolean isShowing()
meth public boolean isValid()
meth public boolean isVisible()
meth public boolean prepareImage(java.awt.Image,int,int,java.awt.image.ImageObserver)
meth public boolean prepareImage(java.awt.Image,java.awt.image.ImageObserver)
meth public float getAlignmentX()
meth public float getAlignmentY()
meth public int checkImage(java.awt.Image,int,int,java.awt.image.ImageObserver)
meth public int checkImage(java.awt.Image,java.awt.image.ImageObserver)
meth public int getCenterX()
meth public int getCenterXForClick()
meth public int getCenterY()
meth public int getCenterYForClick()
meth public int getHeight()
meth public int getWidth()
meth public int getX()
meth public int getY()
meth public java.awt.Color getBackground()
meth public java.awt.Color getForeground()
meth public java.awt.Component getComponentAt(int,int)
meth public java.awt.Component getComponentAt(java.awt.Point)
meth public java.awt.Component getSource()
meth public java.awt.ComponentOrientation getComponentOrientation()
meth public java.awt.Container getContainer(org.netbeans.jemmy.ComponentChooser)
meth public java.awt.Container getParent()
meth public java.awt.Container[] getContainers()
meth public java.awt.Cursor getCursor()
meth public java.awt.Dimension getMaximumSize()
meth public java.awt.Dimension getMinimumSize()
meth public java.awt.Dimension getPreferredSize()
meth public java.awt.Dimension getSize()
meth public java.awt.Dimension getSize(java.awt.Dimension)
meth public java.awt.Font getFont()
meth public java.awt.FontMetrics getFontMetrics(java.awt.Font)
meth public java.awt.Graphics getGraphics()
meth public java.awt.Image createImage(int,int)
meth public java.awt.Image createImage(java.awt.image.ImageProducer)
meth public java.awt.Point getLocation()
meth public java.awt.Point getLocation(java.awt.Point)
meth public java.awt.Point getLocationOnScreen()
meth public java.awt.Rectangle getBounds()
meth public java.awt.Rectangle getBounds(java.awt.Rectangle)
meth public java.awt.Toolkit getToolkit()
meth public java.awt.Window getWindow()
meth public java.awt.dnd.DropTarget getDropTarget()
meth public java.awt.im.InputContext getInputContext()
meth public java.awt.im.InputMethodRequests getInputMethodRequests()
meth public java.awt.image.ColorModel getColorModel()
meth public java.lang.Object getTreeLock()
meth public java.lang.String getName()
meth public java.util.Hashtable getDump()
meth public java.util.Locale getLocale()
meth public org.netbeans.jemmy.EventDispatcher getEventDispatcher()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public static java.awt.Component findComponent(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.Component findComponent(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static java.awt.Component waitComponent(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.Component waitComponent(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static java.awt.Component[] findComponents(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public void activateWindow()
meth public void add(java.awt.PopupMenu)
meth public void addComponentListener(java.awt.event.ComponentListener)
meth public void addFocusListener(java.awt.event.FocusListener)
meth public void addInputMethodListener(java.awt.event.InputMethodListener)
meth public void addKeyListener(java.awt.event.KeyListener)
meth public void addMouseListener(java.awt.event.MouseListener)
meth public void addMouseMotionListener(java.awt.event.MouseMotionListener)
meth public void addNotify()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void clickForPopup()
meth public void clickForPopup(int)
meth public void clickForPopup(int,int)
meth public void clickForPopup(int,int,int)
meth public void clickMouse()
meth public void clickMouse(int)
meth public void clickMouse(int,int)
meth public void clickMouse(int,int,int)
meth public void clickMouse(int,int,int,int)
meth public void clickMouse(int,int,int,int,int)
meth public void clickMouse(int,int,int,int,int,boolean)
meth public void copyEnvironment(org.netbeans.jemmy.operators.Operator)
meth public void dispatchEvent(java.awt.AWTEvent)
meth public void doLayout()
meth public void dragMouse(int,int)
meth public void dragMouse(int,int,int)
meth public void dragMouse(int,int,int,int)
meth public void dragNDrop(int,int,int,int)
meth public void dragNDrop(int,int,int,int,int)
meth public void dragNDrop(int,int,int,int,int,int)
meth public void enableInputMethods(boolean)
meth public void enterMouse()
meth public void exitMouse()
meth public void getFocus()
meth public void invalidate()
meth public void list()
meth public void list(java.io.PrintStream)
meth public void list(java.io.PrintStream,int)
meth public void list(java.io.PrintWriter)
meth public void list(java.io.PrintWriter,int)
meth public void makeComponentVisible()
meth public void moveMouse(int,int)
meth public void paint(java.awt.Graphics)
meth public void paintAll(java.awt.Graphics)
meth public void pressKey(int)
meth public void pressKey(int,int)
meth public void pressMouse()
meth public void pressMouse(int,int)
meth public void print(java.awt.Graphics)
meth public void printAll(java.awt.Graphics)
meth public void pushKey(int)
meth public void pushKey(int,int)
meth public void releaseKey(int)
meth public void releaseKey(int,int)
meth public void releaseMouse()
meth public void releaseMouse(int,int)
meth public void remove(java.awt.MenuComponent)
meth public void removeComponentListener(java.awt.event.ComponentListener)
meth public void removeFocusListener(java.awt.event.FocusListener)
meth public void removeInputMethodListener(java.awt.event.InputMethodListener)
meth public void removeKeyListener(java.awt.event.KeyListener)
meth public void removeMouseListener(java.awt.event.MouseListener)
meth public void removeMouseMotionListener(java.awt.event.MouseMotionListener)
meth public void removeNotify()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void repaint()
meth public void repaint(int,int,int,int)
meth public void repaint(long)
meth public void repaint(long,int,int,int,int)
meth public void requestFocus()
meth public void setBackground(java.awt.Color)
meth public void setBounds(int,int,int,int)
meth public void setBounds(java.awt.Rectangle)
meth public void setComponentOrientation(java.awt.ComponentOrientation)
meth public void setCursor(java.awt.Cursor)
meth public void setDropTarget(java.awt.dnd.DropTarget)
meth public void setEnabled(boolean)
meth public void setFont(java.awt.Font)
meth public void setForeground(java.awt.Color)
meth public void setLocale(java.util.Locale)
meth public void setLocation(int,int)
meth public void setLocation(java.awt.Point)
meth public void setName(java.lang.String)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setSize(int,int)
meth public void setSize(java.awt.Dimension)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
meth public void setVisible(boolean)
meth public void transferFocus()
meth public void typeKey(char)
meth public void typeKey(char,int)
meth public void typeKey(int,char,int)
meth public void typedKey(char,int)
meth public void update(java.awt.Graphics)
meth public void validate()
meth public void waitComponentEnabled() throws java.lang.InterruptedException
meth public void waitComponentShowing(boolean)
meth public void waitComponentVisible(boolean)
meth public void waitHasFocus()
meth public void wtComponentEnabled()
supr org.netbeans.jemmy.operators.Operator
hfds AFTER_DRAG_TIMEOUT,BEFORE_DRAG_TIMEOUT,MOUSE_CLICK_TIMEOUT,PUSH_KEY_TIMEOUT,WAIT_COMPONENT_ENABLED_TIMEOUT,WAIT_COMPONENT_TIMEOUT,WAIT_FOCUS_TIMEOUT,WAIT_STATE_TIMEOUT,dispatcher,fDriver,kDriver,mDriver,output,source,timeouts
hcls VisibleComponentFinder

CLSS public org.netbeans.jemmy.operators.ContainerOperator<%0 extends java.awt.Container>
cons public init(java.awt.Container)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
innr public static ContainerFinder
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth public boolean isAncestorOf(java.awt.Component)
meth public int getComponentCount()
meth public java.awt.Component add(java.awt.Component)
meth public java.awt.Component add(java.awt.Component,int)
meth public java.awt.Component add(java.lang.String,java.awt.Component)
meth public java.awt.Component findComponentAt(int,int)
meth public java.awt.Component findComponentAt(java.awt.Point)
meth public java.awt.Component findSubComponent(org.netbeans.jemmy.ComponentChooser)
meth public java.awt.Component findSubComponent(org.netbeans.jemmy.ComponentChooser,int)
meth public java.awt.Component getComponent(int)
meth public java.awt.Component waitSubComponent(org.netbeans.jemmy.ComponentChooser)
meth public java.awt.Component waitSubComponent(org.netbeans.jemmy.ComponentChooser,int)
meth public java.awt.Component[] getComponents()
meth public java.awt.Insets getInsets()
meth public java.awt.LayoutManager getLayout()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public org.netbeans.jemmy.operators.ComponentOperator createSubOperator(org.netbeans.jemmy.ComponentChooser)
meth public org.netbeans.jemmy.operators.ComponentOperator createSubOperator(org.netbeans.jemmy.ComponentChooser,int)
meth public static java.awt.Container findContainer(java.awt.Container)
meth public static java.awt.Container findContainer(java.awt.Container,int)
meth public static java.awt.Container findContainer(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.Container findContainer(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static java.awt.Container findContainerUnder(java.awt.Component)
meth public static java.awt.Container findContainerUnder(java.awt.Component,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.Container waitContainer(java.awt.Container)
meth public static java.awt.Container waitContainer(java.awt.Container,int)
meth public static java.awt.Container waitContainer(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.Container waitContainer(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void add(java.awt.Component,java.lang.Object)
meth public void add(java.awt.Component,java.lang.Object,int)
meth public void addContainerListener(java.awt.event.ContainerListener)
meth public void paintComponents(java.awt.Graphics)
meth public void printComponents(java.awt.Graphics)
meth public void remove(int)
meth public void remove(java.awt.Component)
meth public void removeAll()
meth public void removeContainerListener(java.awt.event.ContainerListener)
meth public void setLayout(java.awt.LayoutManager)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
supr org.netbeans.jemmy.operators.ComponentOperator
hfds WAIT_SUBCOMPONENT_TIMEOUT,output,searcher,timeouts

CLSS public static org.netbeans.jemmy.operators.ContainerOperator$ContainerFinder
 outer org.netbeans.jemmy.operators.ContainerOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.DialogOperator
cons public init()
cons public init(int)
cons public init(java.awt.Dialog)
cons public init(java.lang.String)
cons public init(java.lang.String,int)
cons public init(java.lang.String,int,org.netbeans.jemmy.operators.Operator)
cons public init(org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.ComponentChooser,int)
cons public init(org.netbeans.jemmy.ComponentChooser,int,org.netbeans.jemmy.operators.Operator)
cons public init(org.netbeans.jemmy.operators.WindowOperator)
cons public init(org.netbeans.jemmy.operators.WindowOperator,int)
cons public init(org.netbeans.jemmy.operators.WindowOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.WindowOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.WindowOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.WindowOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static java.lang.String IS_MODAL_DPROP = "Modal"
fld public final static java.lang.String IS_RESIZABLE_DPROP = "Resizable"
fld public final static java.lang.String TITLE_DPROP = "Title"
innr public static DialogByTitleFinder
innr public static DialogFinder
meth protected static java.awt.Dialog waitDialog(java.awt.Window,org.netbeans.jemmy.ComponentChooser,int,org.netbeans.jemmy.Timeouts,org.netbeans.jemmy.TestOut)
meth protected static java.awt.Dialog waitDialog(org.netbeans.jemmy.ComponentChooser,int,org.netbeans.jemmy.Timeouts,org.netbeans.jemmy.TestOut)
meth protected static java.awt.Dialog waitDialog(org.netbeans.jemmy.operators.WindowOperator,org.netbeans.jemmy.ComponentChooser,int)
meth public boolean isModal()
meth public boolean isResizable()
meth public java.lang.String getTitle()
meth public java.util.Hashtable getDump()
meth public void setModal(boolean)
meth public void setResizable(boolean)
meth public void setTitle(java.lang.String)
meth public void waitTitle(java.lang.String)
supr org.netbeans.jemmy.operators.WindowOperator

CLSS public static org.netbeans.jemmy.operators.DialogOperator$DialogByTitleFinder
 outer org.netbeans.jemmy.operators.DialogOperator
cons public init(java.lang.String)
cons public init(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
intf org.netbeans.jemmy.ComponentChooser
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
supr java.lang.Object
hfds comparator,title

CLSS public static org.netbeans.jemmy.operators.DialogOperator$DialogFinder
 outer org.netbeans.jemmy.operators.DialogOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.FrameOperator
cons public init()
cons public init(int)
cons public init(java.awt.Frame)
cons public init(java.lang.String)
cons public init(java.lang.String,int)
cons public init(java.lang.String,int,org.netbeans.jemmy.operators.Operator)
cons public init(org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.ComponentChooser,int)
cons public init(org.netbeans.jemmy.ComponentChooser,int,org.netbeans.jemmy.operators.Operator)
fld public final static java.lang.String IS_RESIZABLE_DPROP = "Resizable"
fld public final static java.lang.String STATE_DPROP = "State"
fld public final static java.lang.String STATE_ICONIFIED_DPROP_VALUE = "ICONIFIED"
fld public final static java.lang.String STATE_NORMAL_DPROP_VALUE = "NORMAL"
fld public final static java.lang.String TITLE_DPROP = "Title"
innr public static FrameByTitleFinder
innr public static FrameFinder
intf org.netbeans.jemmy.Outputable
meth protected static java.awt.Frame waitFrame(org.netbeans.jemmy.ComponentChooser,int,org.netbeans.jemmy.Timeouts,org.netbeans.jemmy.TestOut)
meth public boolean isResizable()
meth public int getState()
meth public java.awt.Image getIconImage()
meth public java.awt.MenuBar getMenuBar()
meth public java.lang.String getTitle()
meth public java.util.Hashtable getDump()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public void copyEnvironment(org.netbeans.jemmy.operators.Operator)
meth public void deiconify()
meth public void demaximize()
meth public void iconify()
meth public void maximize()
meth public void setIconImage(java.awt.Image)
meth public void setMenuBar(java.awt.MenuBar)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setResizable(boolean)
meth public void setState(int)
meth public void setTitle(java.lang.String)
meth public void waitState(int)
meth public void waitTitle(java.lang.String)
supr org.netbeans.jemmy.operators.WindowOperator
hfds driver,output

CLSS public static org.netbeans.jemmy.operators.FrameOperator$FrameByTitleFinder
 outer org.netbeans.jemmy.operators.FrameOperator
cons public init(java.lang.String)
cons public init(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
intf org.netbeans.jemmy.ComponentChooser
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
supr java.lang.Object
hfds comparator,title

CLSS public static org.netbeans.jemmy.operators.FrameOperator$FrameFinder
 outer org.netbeans.jemmy.operators.FrameOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.JButtonOperator
cons public init(javax.swing.JButton)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static java.lang.String IS_DEFAULT_DPROP = "Default button"
innr public static JButtonFinder
meth protected void prepareToClick()
meth public boolean isDefaultButton()
meth public boolean isDefaultCapable()
meth public java.util.Hashtable getDump()
meth public static javax.swing.JButton findJButton(java.awt.Container,java.lang.String,boolean,boolean)
meth public static javax.swing.JButton findJButton(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JButton findJButton(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JButton findJButton(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JButton waitJButton(java.awt.Container,java.lang.String,boolean,boolean)
meth public static javax.swing.JButton waitJButton(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JButton waitJButton(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JButton waitJButton(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void setDefaultCapable(boolean)
supr org.netbeans.jemmy.operators.AbstractButtonOperator

CLSS public static org.netbeans.jemmy.operators.JButtonOperator$JButtonFinder
 outer org.netbeans.jemmy.operators.JButtonOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.JCheckBoxMenuItemOperator
cons public init(javax.swing.JCheckBoxMenuItem)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
innr public static JCheckBoxMenuItemByLabelFinder
innr public static JCheckBoxMenuItemFinder
meth public boolean getState()
meth public void setState(boolean)
supr org.netbeans.jemmy.operators.JMenuItemOperator

CLSS public static org.netbeans.jemmy.operators.JCheckBoxMenuItemOperator$JCheckBoxMenuItemByLabelFinder
 outer org.netbeans.jemmy.operators.JCheckBoxMenuItemOperator
cons public init(java.lang.String)
cons public init(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
intf org.netbeans.jemmy.ComponentChooser
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
supr java.lang.Object
hfds comparator,label

CLSS public static org.netbeans.jemmy.operators.JCheckBoxMenuItemOperator$JCheckBoxMenuItemFinder
 outer org.netbeans.jemmy.operators.JCheckBoxMenuItemOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.JCheckBoxOperator
cons public init(javax.swing.JCheckBox)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
innr public static JCheckBoxFinder
meth public static javax.swing.JCheckBox findJCheckBox(java.awt.Container,java.lang.String,boolean,boolean)
meth public static javax.swing.JCheckBox findJCheckBox(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JCheckBox findJCheckBox(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JCheckBox findJCheckBox(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JCheckBox waitJCheckBox(java.awt.Container,java.lang.String,boolean,boolean)
meth public static javax.swing.JCheckBox waitJCheckBox(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JCheckBox waitJCheckBox(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JCheckBox waitJCheckBox(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
supr org.netbeans.jemmy.operators.JToggleButtonOperator

CLSS public static org.netbeans.jemmy.operators.JCheckBoxOperator$JCheckBoxFinder
 outer org.netbeans.jemmy.operators.JCheckBoxOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.JColorChooserOperator
cons public init(javax.swing.JColorChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static java.lang.String COLOR_DPROP = "Color"
fld public final static java.lang.String SELECTED_PAGE_DPROP = "Selected page"
innr public static JColorChooserFinder
intf org.netbeans.jemmy.Outputable
meth public java.awt.Color getColor()
meth public java.util.Hashtable getDump()
meth public javax.swing.JComponent getPreviewPanel()
meth public javax.swing.colorchooser.AbstractColorChooserPanel removeChooserPanel(javax.swing.colorchooser.AbstractColorChooserPanel)
meth public javax.swing.colorchooser.AbstractColorChooserPanel[] getChooserPanels()
meth public javax.swing.colorchooser.ColorSelectionModel getSelectionModel()
meth public javax.swing.plaf.ColorChooserUI getUI()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public static javax.swing.JColorChooser findJColorChooser(java.awt.Container)
meth public static javax.swing.JColorChooser findJColorChooser(java.awt.Container,int)
meth public static javax.swing.JColorChooser findJColorChooser(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JColorChooser findJColorChooser(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JColorChooser waitJColorChooser(java.awt.Container)
meth public static javax.swing.JColorChooser waitJColorChooser(java.awt.Container,int)
meth public static javax.swing.JColorChooser waitJColorChooser(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JColorChooser waitJColorChooser(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void addChooserPanel(javax.swing.colorchooser.AbstractColorChooserPanel)
meth public void enterBlue(int)
meth public void enterColor(int)
meth public void enterColor(int,int,int)
meth public void enterColor(java.awt.Color)
meth public void enterGreen(int)
meth public void enterRed(int)
meth public void setChooserPanels(javax.swing.colorchooser.AbstractColorChooserPanel[])
meth public void setColor(int)
meth public void setColor(int,int,int)
meth public void setColor(java.awt.Color)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setPreviewPanel(javax.swing.JComponent)
meth public void setSelectionModel(javax.swing.colorchooser.ColorSelectionModel)
meth public void setUI(javax.swing.plaf.ColorChooserUI)
meth public void switchToRGB()
supr org.netbeans.jemmy.operators.JComponentOperator
hfds RGB_TITLE,blue,green,output,red,tabbed

CLSS public static org.netbeans.jemmy.operators.JColorChooserOperator$JColorChooserFinder
 outer org.netbeans.jemmy.operators.JColorChooserOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.JComboBoxOperator
cons public init(javax.swing.JComboBox)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static java.lang.String ITEM_PREFIX_DPROP = "Item"
fld public final static java.lang.String TEXT_DPROP = "Text"
innr public static JComboBoxByItemFinder
innr public static JComboBoxFinder
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth public boolean isEditable()
meth public boolean isLightWeightPopupEnabled()
meth public boolean isPopupVisible()
meth public boolean selectWithKeyChar(char)
meth public int findItemIndex(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public int getItemCount()
meth public int getMaximumRowCount()
meth public int getSelectedIndex()
meth public int waitItem(int)
meth public int waitItem(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public java.lang.Object getItemAt(int)
meth public java.lang.Object getSelectedItem()
meth public java.lang.Object[] getSelectedObjects()
meth public java.lang.String getActionCommand()
meth public java.util.Hashtable getDump()
meth public javax.swing.ComboBoxEditor getEditor()
meth public javax.swing.ComboBoxModel getModel()
meth public javax.swing.JButton findJButton()
meth public javax.swing.JComboBox$KeySelectionManager getKeySelectionManager()
meth public javax.swing.JList waitList()
meth public javax.swing.JTextField findJTextField()
meth public javax.swing.ListCellRenderer getRenderer()
meth public javax.swing.plaf.ComboBoxUI getUI()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public org.netbeans.jemmy.operators.JButtonOperator getButton()
meth public org.netbeans.jemmy.operators.JTextFieldOperator getTextField()
meth public static javax.swing.JComboBox findJComboBox(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JComboBox findJComboBox(java.awt.Container,java.lang.String,boolean,boolean,int,int)
meth public static javax.swing.JComboBox findJComboBox(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JComboBox findJComboBox(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JComboBox waitJComboBox(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JComboBox waitJComboBox(java.awt.Container,java.lang.String,boolean,boolean,int,int)
meth public static javax.swing.JComboBox waitJComboBox(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JComboBox waitJComboBox(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void addActionListener(java.awt.event.ActionListener)
meth public void addItem(java.lang.Object)
meth public void addItemListener(java.awt.event.ItemListener)
meth public void clearText()
meth public void configureEditor(javax.swing.ComboBoxEditor,java.lang.Object)
meth public void contentsChanged(javax.swing.event.ListDataEvent)
meth public void copyEnvironment(org.netbeans.jemmy.operators.Operator)
meth public void enterText(java.lang.String)
meth public void hidePopup()
meth public void insertItemAt(java.lang.Object,int)
meth public void intervalAdded(javax.swing.event.ListDataEvent)
meth public void intervalRemoved(javax.swing.event.ListDataEvent)
meth public void processKeyEvent(java.awt.event.KeyEvent)
meth public void pushComboButton()
meth public void removeActionListener(java.awt.event.ActionListener)
meth public void removeAllItems()
meth public void removeItem(java.lang.Object)
meth public void removeItemAt(int)
meth public void removeItemListener(java.awt.event.ItemListener)
meth public void selectItem(int)
meth public void selectItem(java.lang.String)
meth public void selectItem(java.lang.String,boolean,boolean)
meth public void selectItem(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public void setActionCommand(java.lang.String)
meth public void setEditable(boolean)
meth public void setEditor(javax.swing.ComboBoxEditor)
meth public void setKeySelectionManager(javax.swing.JComboBox$KeySelectionManager)
meth public void setLightWeightPopupEnabled(boolean)
meth public void setMaximumRowCount(int)
meth public void setModel(javax.swing.ComboBoxModel)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setPopupVisible(boolean)
meth public void setRenderer(javax.swing.ListCellRenderer)
meth public void setSelectedIndex(int)
meth public void setSelectedItem(java.lang.Object)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
meth public void setUI(javax.swing.plaf.ComboBoxUI)
meth public void showPopup()
meth public void typeText(java.lang.String)
meth public void waitItemSelected(int)
meth public void waitItemSelected(java.lang.String)
supr org.netbeans.jemmy.operators.JComponentOperator
hfds BEFORE_SELECTING_TIMEOUT,WAIT_LIST_TIMEOUT,button,driver,output,text,timeouts
hcls ListWater,PopupWindowChooser

CLSS public static org.netbeans.jemmy.operators.JComboBoxOperator$JComboBoxByItemFinder
 outer org.netbeans.jemmy.operators.JComboBoxOperator
cons public init(java.lang.String,int)
cons public init(java.lang.String,int,org.netbeans.jemmy.operators.Operator$StringComparator)
intf org.netbeans.jemmy.ComponentChooser
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
supr java.lang.Object
hfds comparator,itemIndex,label

CLSS public static org.netbeans.jemmy.operators.JComboBoxOperator$JComboBoxFinder
 outer org.netbeans.jemmy.operators.JComboBoxOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.JComponentOperator
cons public init(javax.swing.JComponent)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static java.lang.String A11Y_DATA = "Accessible data (yes/no)"
fld public final static java.lang.String A11Y_DESCRIPTION_DPROP = "Accessible decription"
fld public final static java.lang.String A11Y_NAME_DPROP = "Accessible name"
fld public final static java.lang.String TOOLTIP_TEXT_DPROP = "Tooltip text"
innr public static JComponentByTipFinder
innr public static JComponentFinder
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth public boolean getAutoscrolls()
meth public boolean isFocusCycleRoot()
meth public boolean isManagingFocus()
meth public boolean isOptimizedDrawingEnabled()
meth public boolean isPaintingTile()
meth public boolean isRequestFocusEnabled()
meth public boolean isValidateRoot()
meth public boolean requestDefaultFocus()
meth public int getCenterXForClick()
meth public int getCenterYForClick()
meth public int getConditionForKeyStroke(javax.swing.KeyStroke)
meth public int getDebugGraphicsOptions()
meth public java.awt.Component getNextFocusableComponent()
meth public java.awt.Container getTopLevelAncestor()
meth public java.awt.Insets getInsets(java.awt.Insets)
meth public java.awt.Point getToolTipLocation(java.awt.event.MouseEvent)
meth public java.awt.Rectangle getVisibleRect()
meth public java.awt.event.ActionListener getActionForKeyStroke(javax.swing.KeyStroke)
meth public java.lang.Object getClientProperty(java.lang.Object)
meth public java.lang.String getToolTipText()
meth public java.lang.String getToolTipText(java.awt.event.MouseEvent)
meth public java.lang.String getUIClassID()
meth public java.util.Hashtable getDump()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.JRootPane getRootPane()
meth public javax.swing.JToolTip createToolTip()
meth public javax.swing.JToolTip showToolTip()
meth public javax.swing.JToolTip waitToolTip()
meth public javax.swing.KeyStroke[] getRegisteredKeyStrokes()
meth public javax.swing.border.Border getBorder()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public org.netbeans.jemmy.operators.ContainerOperator getWindowContainerOperator()
meth public static javax.swing.JComponent findJComponent(java.awt.Container,java.lang.String,boolean,boolean)
meth public static javax.swing.JComponent findJComponent(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JComponent findJComponent(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JComponent findJComponent(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JComponent waitJComponent(java.awt.Container,java.lang.String,boolean,boolean)
meth public static javax.swing.JComponent waitJComponent(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JComponent waitJComponent(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JComponent waitJComponent(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void addAncestorListener(javax.swing.event.AncestorListener)
meth public void addVetoableChangeListener(java.beans.VetoableChangeListener)
meth public void computeVisibleRect(java.awt.Rectangle)
meth public void firePropertyChange(java.lang.String,boolean,boolean)
meth public void firePropertyChange(java.lang.String,byte,byte)
meth public void firePropertyChange(java.lang.String,char,char)
meth public void firePropertyChange(java.lang.String,double,double)
meth public void firePropertyChange(java.lang.String,float,float)
meth public void firePropertyChange(java.lang.String,int,int)
meth public void firePropertyChange(java.lang.String,long,long)
meth public void firePropertyChange(java.lang.String,short,short)
meth public void grabFocus()
meth public void paintImmediately(int,int,int,int)
meth public void paintImmediately(java.awt.Rectangle)
meth public void putClientProperty(java.lang.Object,java.lang.Object)
meth public void registerKeyboardAction(java.awt.event.ActionListener,java.lang.String,javax.swing.KeyStroke,int)
meth public void registerKeyboardAction(java.awt.event.ActionListener,javax.swing.KeyStroke,int)
meth public void removeAncestorListener(javax.swing.event.AncestorListener)
meth public void removeVetoableChangeListener(java.beans.VetoableChangeListener)
meth public void repaint(java.awt.Rectangle)
meth public void resetKeyboardActions()
meth public void revalidate()
meth public void scrollRectToVisible(java.awt.Rectangle)
meth public void setAlignmentX(float)
meth public void setAlignmentY(float)
meth public void setAutoscrolls(boolean)
meth public void setBorder(javax.swing.border.Border)
meth public void setDebugGraphicsOptions(int)
meth public void setDoubleBuffered(boolean)
meth public void setMaximumSize(java.awt.Dimension)
meth public void setMinimumSize(java.awt.Dimension)
meth public void setNextFocusableComponent(java.awt.Component)
meth public void setOpaque(boolean)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setPreferredSize(java.awt.Dimension)
meth public void setRequestFocusEnabled(boolean)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
meth public void setToolTipText(java.lang.String)
meth public void unregisterKeyboardAction(javax.swing.KeyStroke)
meth public void updateUI()
supr org.netbeans.jemmy.operators.ContainerOperator
hfds SHOW_TOOL_TIP_TIMEOUT,WAIT_TOOL_TIP_TIMEOUT,output,timeouts
hcls JToolTipFinder,JToolTipWindowFinder

CLSS public static org.netbeans.jemmy.operators.JComponentOperator$JComponentByTipFinder
 outer org.netbeans.jemmy.operators.JComponentOperator
cons public init(java.lang.String)
cons public init(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
intf org.netbeans.jemmy.ComponentChooser
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
supr java.lang.Object
hfds comparator,label

CLSS public static org.netbeans.jemmy.operators.JComponentOperator$JComponentFinder
 outer org.netbeans.jemmy.operators.JComponentOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.JDialogOperator
cons public init()
cons public init(int)
cons public init(java.lang.String)
cons public init(java.lang.String,int)
cons public init(java.lang.String,int,org.netbeans.jemmy.operators.Operator)
cons public init(javax.swing.JDialog)
cons public init(org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.ComponentChooser,int)
cons public init(org.netbeans.jemmy.ComponentChooser,int,org.netbeans.jemmy.operators.Operator)
cons public init(org.netbeans.jemmy.operators.WindowOperator)
cons public init(org.netbeans.jemmy.operators.WindowOperator,int)
cons public init(org.netbeans.jemmy.operators.WindowOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.WindowOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.WindowOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.WindowOperator,org.netbeans.jemmy.ComponentChooser,int)
innr public static JDialogFinder
meth protected static javax.swing.JDialog waitJDialog(java.awt.Window,org.netbeans.jemmy.ComponentChooser,int,org.netbeans.jemmy.Timeouts,org.netbeans.jemmy.TestOut)
meth protected static javax.swing.JDialog waitJDialog(org.netbeans.jemmy.ComponentChooser,int,org.netbeans.jemmy.Timeouts,org.netbeans.jemmy.TestOut)
meth protected static javax.swing.JDialog waitJDialog(org.netbeans.jemmy.operators.WindowOperator,org.netbeans.jemmy.ComponentChooser,int)
meth public int getDefaultCloseOperation()
meth public java.awt.Component getGlassPane()
meth public java.awt.Container getContentPane()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.JLayeredPane getLayeredPane()
meth public javax.swing.JMenuBar getJMenuBar()
meth public javax.swing.JRootPane getRootPane()
meth public static java.awt.Dialog getTopModalDialog()
meth public static javax.swing.JDialog findJDialog(java.awt.Window,java.lang.String,boolean,boolean)
meth public static javax.swing.JDialog findJDialog(java.awt.Window,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JDialog findJDialog(java.awt.Window,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JDialog findJDialog(java.awt.Window,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JDialog findJDialog(java.lang.String,boolean,boolean)
meth public static javax.swing.JDialog findJDialog(java.lang.String,boolean,boolean,int)
meth public static javax.swing.JDialog findJDialog(org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JDialog findJDialog(org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JDialog waitJDialog(java.awt.Window,java.lang.String,boolean,boolean)
meth public static javax.swing.JDialog waitJDialog(java.awt.Window,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JDialog waitJDialog(java.awt.Window,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JDialog waitJDialog(java.awt.Window,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JDialog waitJDialog(java.lang.String,boolean,boolean)
meth public static javax.swing.JDialog waitJDialog(java.lang.String,boolean,boolean,int)
meth public static javax.swing.JDialog waitJDialog(org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JDialog waitJDialog(org.netbeans.jemmy.ComponentChooser,int)
meth public void setContentPane(java.awt.Container)
meth public void setDefaultCloseOperation(int)
meth public void setGlassPane(java.awt.Component)
meth public void setJMenuBar(javax.swing.JMenuBar)
meth public void setLayeredPane(javax.swing.JLayeredPane)
meth public void setLocationRelativeTo(java.awt.Component)
supr org.netbeans.jemmy.operators.DialogOperator

CLSS public static org.netbeans.jemmy.operators.JDialogOperator$JDialogFinder
 outer org.netbeans.jemmy.operators.JDialogOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.JEditorPaneOperator
cons public init(javax.swing.JEditorPane)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static java.lang.String CONTENT_TYPE_DPROP = "Content type"
innr public static JEditorPaneFinder
meth public java.lang.String getContentType()
meth public java.net.URL getPage()
meth public java.util.Hashtable getDump()
meth public javax.swing.text.EditorKit getEditorKit()
meth public javax.swing.text.EditorKit getEditorKitForContentType(java.lang.String)
meth public static javax.swing.JEditorPane findJEditorPane(java.awt.Container,java.lang.String,boolean,boolean)
meth public static javax.swing.JEditorPane findJEditorPane(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JEditorPane findJEditorPane(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JEditorPane findJEditorPane(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JEditorPane waitJEditorPane(java.awt.Container,java.lang.String,boolean,boolean)
meth public static javax.swing.JEditorPane waitJEditorPane(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JEditorPane waitJEditorPane(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JEditorPane waitJEditorPane(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void addHyperlinkListener(javax.swing.event.HyperlinkListener)
meth public void fireHyperlinkUpdate(javax.swing.event.HyperlinkEvent)
meth public void read(java.io.InputStream,java.lang.Object)
meth public void removeHyperlinkListener(javax.swing.event.HyperlinkListener)
meth public void setContentType(java.lang.String)
meth public void setEditorKit(javax.swing.text.EditorKit)
meth public void setEditorKitForContentType(java.lang.String,javax.swing.text.EditorKit)
meth public void setPage(java.lang.String)
meth public void setPage(java.net.URL)
meth public void usePageNavigationKeys(boolean)
supr org.netbeans.jemmy.operators.JTextComponentOperator

CLSS public static org.netbeans.jemmy.operators.JEditorPaneOperator$JEditorPaneFinder
 outer org.netbeans.jemmy.operators.JEditorPaneOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.JFileChooserOperator
cons public init()
cons public init(javax.swing.JFileChooser)
cons public init(org.netbeans.jemmy.operators.Operator)
innr public static JFileChooserFinder
innr public static JFileChooserJDialogFinder
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth public boolean accept(java.io.File)
meth public boolean checkFileDisplayed(java.lang.String)
meth public boolean checkFileDisplayed(java.lang.String,boolean,boolean)
meth public boolean checkFileDisplayed(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public boolean isDirectorySelectionEnabled()
meth public boolean isFileHidingEnabled()
meth public boolean isFileSelectionEnabled()
meth public boolean isMultiSelectionEnabled()
meth public boolean isTraversable(java.io.File)
meth public boolean removeChoosableFileFilter(javax.swing.filechooser.FileFilter)
meth public int getApproveButtonMnemonic()
meth public int getDialogType()
meth public int getFileCount()
meth public int getFileSelectionMode()
meth public int showDialog(java.awt.Component,java.lang.String)
meth public int showOpenDialog(java.awt.Component)
meth public int showSaveDialog(java.awt.Component)
meth public java.io.File enterSubDir(java.lang.String)
meth public java.io.File enterSubDir(java.lang.String,boolean,boolean)
meth public java.io.File enterSubDir(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public java.io.File getCurrentDirectory()
meth public java.io.File getSelectedFile()
meth public java.io.File goHome()
meth public java.io.File goUpLevel()
meth public java.io.File[] getFiles()
meth public java.io.File[] getSelectedFiles()
meth public java.lang.String getApproveButtonText()
meth public java.lang.String getApproveButtonToolTipText()
meth public java.lang.String getDescription(java.io.File)
meth public java.lang.String getDialogTitle()
meth public java.lang.String getName(java.io.File)
meth public java.lang.String getTypeDescription(java.io.File)
meth public javax.swing.Icon getIcon(java.io.File)
meth public javax.swing.JButton getApproveButton()
meth public javax.swing.JButton getCancelButton()
meth public javax.swing.JButton getHomeButton()
meth public javax.swing.JButton getUpLevelButton()
meth public javax.swing.JComboBox getFileTypesCombo()
meth public javax.swing.JComboBox getPathCombo()
meth public javax.swing.JComponent getAccessory()
meth public javax.swing.JList getFileList()
meth public javax.swing.JTextField getPathField()
meth public javax.swing.JToggleButton getDetailsToggleButton()
meth public javax.swing.JToggleButton getListToggleButton()
meth public javax.swing.filechooser.FileFilter getAcceptAllFileFilter()
meth public javax.swing.filechooser.FileFilter getFileFilter()
meth public javax.swing.filechooser.FileFilter[] getChoosableFileFilters()
meth public javax.swing.filechooser.FileSystemView getFileSystemView()
meth public javax.swing.filechooser.FileView getFileView()
meth public javax.swing.plaf.FileChooserUI getUI()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public static javax.swing.JDialog findJFileChooserDialog()
meth public static javax.swing.JDialog waitJFileChooserDialog()
meth public static javax.swing.JFileChooser findJFileChooser()
meth public static javax.swing.JFileChooser findJFileChooser(java.awt.Container)
meth public static javax.swing.JFileChooser waitJFileChooser()
meth public static javax.swing.JFileChooser waitJFileChooser(java.awt.Container)
meth public void addActionListener(java.awt.event.ActionListener)
meth public void addChoosableFileFilter(javax.swing.filechooser.FileFilter)
meth public void approve()
meth public void approveSelection()
meth public void cancel()
meth public void cancelSelection()
meth public void changeToParentDirectory()
meth public void chooseFile(java.lang.String)
meth public void clickOnFile(int,int)
meth public void clickOnFile(java.lang.String)
meth public void clickOnFile(java.lang.String,boolean,boolean)
meth public void clickOnFile(java.lang.String,boolean,boolean,int)
meth public void clickOnFile(java.lang.String,int)
meth public void clickOnFile(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public void clickOnFile(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator,int)
meth public void ensureFileIsVisible(java.io.File)
meth public void removeActionListener(java.awt.event.ActionListener)
meth public void rescanCurrentDirectory()
meth public void resetChoosableFileFilters()
meth public void selectFile(java.lang.String)
meth public void selectFile(java.lang.String,boolean,boolean)
meth public void selectFile(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public void selectFileType(java.lang.String)
meth public void selectFileType(java.lang.String,boolean,boolean)
meth public void selectFileType(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public void selectPathDirectory(java.lang.String)
meth public void selectPathDirectory(java.lang.String,boolean,boolean)
meth public void selectPathDirectory(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public void setAccessory(javax.swing.JComponent)
meth public void setApproveButtonMnemonic(char)
meth public void setApproveButtonMnemonic(int)
meth public void setApproveButtonText(java.lang.String)
meth public void setApproveButtonToolTipText(java.lang.String)
meth public void setCurrentDirectory(java.io.File)
meth public void setDialogTitle(java.lang.String)
meth public void setDialogType(int)
meth public void setFileFilter(javax.swing.filechooser.FileFilter)
meth public void setFileHidingEnabled(boolean)
meth public void setFileSelectionMode(int)
meth public void setFileSystemView(javax.swing.filechooser.FileSystemView)
meth public void setFileView(javax.swing.filechooser.FileView)
meth public void setMultiSelectionEnabled(boolean)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setSelectedFile(java.io.File)
meth public void setSelectedFiles(java.io.File[])
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
meth public void waitFileCount(int)
meth public void waitFileDisplayed(java.lang.String)
supr org.netbeans.jemmy.operators.JComponentOperator
hfds WAIT_LIST_PAINTED_TIMEOUT,innerSearcher,output,timeouts
hcls ButtonFinder

CLSS public static org.netbeans.jemmy.operators.JFileChooserOperator$JFileChooserFinder
 outer org.netbeans.jemmy.operators.JFileChooserOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public static org.netbeans.jemmy.operators.JFileChooserOperator$JFileChooserJDialogFinder
 outer org.netbeans.jemmy.operators.JFileChooserOperator
cons public init(org.netbeans.jemmy.TestOut)
intf org.netbeans.jemmy.ComponentChooser
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
supr java.lang.Object
hfds output,subChooser

CLSS public org.netbeans.jemmy.operators.JFrameOperator
cons public init()
cons public init(int)
cons public init(java.lang.String)
cons public init(java.lang.String,int)
cons public init(java.lang.String,int,org.netbeans.jemmy.operators.Operator)
cons public init(javax.swing.JFrame)
cons public init(org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.ComponentChooser,int)
cons public init(org.netbeans.jemmy.ComponentChooser,int,org.netbeans.jemmy.operators.Operator)
innr public static JFrameFinder
meth public int getDefaultCloseOperation()
meth public java.awt.Component getGlassPane()
meth public java.awt.Container getContentPane()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.JLayeredPane getLayeredPane()
meth public javax.swing.JMenuBar getJMenuBar()
meth public javax.swing.JRootPane getRootPane()
meth public static javax.swing.JFrame findJFrame(java.lang.String,boolean,boolean)
meth public static javax.swing.JFrame findJFrame(java.lang.String,boolean,boolean,int)
meth public static javax.swing.JFrame findJFrame(org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JFrame findJFrame(org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JFrame waitJFrame(java.lang.String,boolean,boolean)
meth public static javax.swing.JFrame waitJFrame(java.lang.String,boolean,boolean,int)
meth public static javax.swing.JFrame waitJFrame(org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JFrame waitJFrame(org.netbeans.jemmy.ComponentChooser,int)
meth public void setContentPane(java.awt.Container)
meth public void setDefaultCloseOperation(int)
meth public void setGlassPane(java.awt.Component)
meth public void setJMenuBar(javax.swing.JMenuBar)
meth public void setLayeredPane(javax.swing.JLayeredPane)
supr org.netbeans.jemmy.operators.FrameOperator

CLSS public static org.netbeans.jemmy.operators.JFrameOperator$JFrameFinder
 outer org.netbeans.jemmy.operators.JFrameOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.JInternalFrameOperator
cons public init(javax.swing.JInternalFrame)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld protected org.netbeans.jemmy.operators.ContainerOperator titleOperator
fld protected org.netbeans.jemmy.operators.JButtonOperator closeOper
fld protected org.netbeans.jemmy.operators.JButtonOperator maxOper
fld protected org.netbeans.jemmy.operators.JButtonOperator minOper
fld public final static java.lang.String IS_RESIZABLE_DPROP = "Resizable"
fld public final static java.lang.String IS_SELECTED_DPROP = "Selected"
fld public final static java.lang.String STATE_CLOSED_DPROP_VALUE = "CLOSED"
fld public final static java.lang.String STATE_DPROP = "State"
fld public final static java.lang.String STATE_ICONIFIED_DPROP_VALUE = "ICONIFIED"
fld public final static java.lang.String STATE_MAXIMAZED_DPROP_VALUE = "MAXIMIZED"
fld public final static java.lang.String STATE_NORMAL_DPROP_VALUE = "NORMAL"
fld public final static java.lang.String TITLE_DPROP = "Title"
innr public static JDesktopIconOperator
innr public static JInternalFrameByTitleFinder
innr public static JInternalFrameFinder
innr public static WrongInternalFrameStateException
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth protected java.awt.Container findTitlePane()
meth protected void initOperators()
meth public boolean isClosable()
meth public boolean isClosed()
meth public boolean isIcon()
meth public boolean isIconifiable()
meth public boolean isMaximizable()
meth public boolean isMaximum()
meth public boolean isResizable()
meth public boolean isSelected()
meth public int getDefaultCloseOperation()
meth public int getLayer()
meth public java.awt.Component getGlassPane()
meth public java.awt.Container getContentPane()
meth public java.lang.String getTitle()
meth public java.lang.String getWarningString()
meth public java.util.Hashtable getDump()
meth public javax.swing.Icon getFrameIcon()
meth public javax.swing.JDesktopPane getDesktopPane()
meth public javax.swing.JInternalFrame$JDesktopIcon getDesktopIcon()
meth public javax.swing.JLayeredPane getLayeredPane()
meth public javax.swing.JMenuBar getJMenuBar()
meth public javax.swing.plaf.InternalFrameUI getUI()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public org.netbeans.jemmy.operators.ContainerOperator getTitleOperator()
meth public org.netbeans.jemmy.operators.JButtonOperator getCloseButton()
meth public org.netbeans.jemmy.operators.JButtonOperator getMaximizeButton()
meth public org.netbeans.jemmy.operators.JButtonOperator getMinimizeButton()
meth public org.netbeans.jemmy.operators.JInternalFrameOperator$JDesktopIconOperator getIconOperator()
meth public static javax.swing.JInternalFrame findJInternalFrame(java.awt.Container,java.lang.String,boolean,boolean)
meth public static javax.swing.JInternalFrame findJInternalFrame(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JInternalFrame findJInternalFrame(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JInternalFrame findJInternalFrame(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JInternalFrame findJInternalFrameUnder(java.awt.Component)
meth public static javax.swing.JInternalFrame findJInternalFrameUnder(java.awt.Component,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JInternalFrame waitJInternalFrame(java.awt.Container,java.lang.String,boolean,boolean)
meth public static javax.swing.JInternalFrame waitJInternalFrame(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JInternalFrame waitJInternalFrame(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JInternalFrame waitJInternalFrame(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void activate()
meth public void addInternalFrameListener(javax.swing.event.InternalFrameListener)
meth public void close()
meth public void deiconify()
meth public void demaximize()
meth public void dispose()
meth public void iconify()
meth public void maximize()
meth public void move(int,int)
meth public void moveToBack()
meth public void moveToFront()
meth public void pack()
meth public void removeInternalFrameListener(javax.swing.event.InternalFrameListener)
meth public void resize(int,int)
meth public void scrollToFrame()
meth public void scrollToRectangle(int,int,int,int)
meth public void scrollToRectangle(java.awt.Rectangle)
meth public void setClosable(boolean)
meth public void setClosed(boolean)
meth public void setContentPane(java.awt.Container)
meth public void setDefaultCloseOperation(int)
meth public void setDesktopIcon(javax.swing.JInternalFrame$JDesktopIcon)
meth public void setFrameIcon(javax.swing.Icon)
meth public void setGlassPane(java.awt.Component)
meth public void setIcon(boolean)
meth public void setIconifiable(boolean)
meth public void setJMenuBar(javax.swing.JMenuBar)
meth public void setLayer(java.lang.Integer)
meth public void setLayeredPane(javax.swing.JLayeredPane)
meth public void setMaximizable(boolean)
meth public void setMaximum(boolean)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setResizable(boolean)
meth public void setSelected(boolean)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
meth public void setTitle(java.lang.String)
meth public void setUI(javax.swing.plaf.InternalFrameUI)
meth public void toBack()
meth public void toFront()
meth public void waitIcon(boolean)
meth public void waitMaximum(boolean)
supr org.netbeans.jemmy.operators.JComponentOperator
hfds fDriver,iDriver,iconOperator,output,timeouts,wDriver

CLSS public static org.netbeans.jemmy.operators.JInternalFrameOperator$JDesktopIconOperator
 outer org.netbeans.jemmy.operators.JInternalFrameOperator
cons public init(javax.swing.JInternalFrame$JDesktopIcon)
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth public javax.swing.JInternalFrame getInternalFrame()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public void pushButton()
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
supr org.netbeans.jemmy.operators.JComponentOperator
hfds output,timeouts

CLSS public static org.netbeans.jemmy.operators.JInternalFrameOperator$JInternalFrameByTitleFinder
 outer org.netbeans.jemmy.operators.JInternalFrameOperator
cons public init(java.lang.String)
cons public init(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
intf org.netbeans.jemmy.ComponentChooser
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
supr java.lang.Object
hfds comparator,label

CLSS public static org.netbeans.jemmy.operators.JInternalFrameOperator$JInternalFrameFinder
 outer org.netbeans.jemmy.operators.JInternalFrameOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
intf org.netbeans.jemmy.ComponentChooser
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
supr java.lang.Object
hfds sf

CLSS public static org.netbeans.jemmy.operators.JInternalFrameOperator$WrongInternalFrameStateException
 outer org.netbeans.jemmy.operators.JInternalFrameOperator
cons public init(java.lang.String,java.awt.Component)
supr org.netbeans.jemmy.JemmyInputException

CLSS public org.netbeans.jemmy.operators.JLabelOperator
cons public init(javax.swing.JLabel)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static java.lang.String TEXT_DPROP = "Text"
innr public static JLabelByLabelFinder
innr public static JLabelFinder
meth public int getDisplayedMnemonic()
meth public int getHorizontalAlignment()
meth public int getHorizontalTextPosition()
meth public int getIconTextGap()
meth public int getVerticalAlignment()
meth public int getVerticalTextPosition()
meth public java.awt.Component getLabelFor()
meth public java.lang.String getText()
meth public java.util.Hashtable getDump()
meth public javax.swing.Icon getDisabledIcon()
meth public javax.swing.Icon getIcon()
meth public javax.swing.plaf.LabelUI getUI()
meth public static javax.swing.JLabel findJLabel(java.awt.Container,java.lang.String,boolean,boolean)
meth public static javax.swing.JLabel findJLabel(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JLabel findJLabel(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JLabel findJLabel(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JLabel waitJLabel(java.awt.Container,java.lang.String,boolean,boolean)
meth public static javax.swing.JLabel waitJLabel(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JLabel waitJLabel(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JLabel waitJLabel(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void setDisabledIcon(javax.swing.Icon)
meth public void setDisplayedMnemonic(char)
meth public void setDisplayedMnemonic(int)
meth public void setHorizontalAlignment(int)
meth public void setHorizontalTextPosition(int)
meth public void setIcon(javax.swing.Icon)
meth public void setIconTextGap(int)
meth public void setLabelFor(java.awt.Component)
meth public void setText(java.lang.String)
meth public void setUI(javax.swing.plaf.LabelUI)
meth public void setVerticalAlignment(int)
meth public void setVerticalTextPosition(int)
meth public void waitText(java.lang.String)
supr org.netbeans.jemmy.operators.JComponentOperator

CLSS public static org.netbeans.jemmy.operators.JLabelOperator$JLabelByLabelFinder
 outer org.netbeans.jemmy.operators.JLabelOperator
cons public init(java.lang.String)
cons public init(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
intf org.netbeans.jemmy.ComponentChooser
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
supr java.lang.Object
hfds comparator,label

CLSS public static org.netbeans.jemmy.operators.JLabelOperator$JLabelFinder
 outer org.netbeans.jemmy.operators.JLabelOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.JListOperator
cons public init(javax.swing.JList)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static java.lang.String ITEM_PREFIX_DPROP = "Item"
fld public final static java.lang.String SELECTED_ITEM_PREFIX_DPROP = "SelectedItem"
innr public NoSuchItemException
innr public abstract interface static ListItemChooser
innr public static JListByItemFinder
innr public static JListFinder
intf org.netbeans.jemmy.Outputable
meth public boolean getScrollableTracksViewportHeight()
meth public boolean getScrollableTracksViewportWidth()
meth public boolean getValueIsAdjusting()
meth public boolean isSelectedIndex(int)
meth public boolean isSelectionEmpty()
meth public int findItemIndex(java.lang.String)
meth public int findItemIndex(java.lang.String,boolean,boolean)
meth public int findItemIndex(java.lang.String,boolean,boolean,int)
meth public int findItemIndex(java.lang.String,int)
meth public int findItemIndex(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public int findItemIndex(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator,int)
meth public int findItemIndex(org.netbeans.jemmy.ComponentChooser)
meth public int findItemIndex(org.netbeans.jemmy.ComponentChooser,int)
meth public int findItemIndex(org.netbeans.jemmy.operators.JListOperator$ListItemChooser)
meth public int findItemIndex(org.netbeans.jemmy.operators.JListOperator$ListItemChooser,int)
meth public int getAnchorSelectionIndex()
meth public int getFirstVisibleIndex()
meth public int getFixedCellHeight()
meth public int getFixedCellWidth()
meth public int getLastVisibleIndex()
meth public int getLeadSelectionIndex()
meth public int getMaxSelectionIndex()
meth public int getMinSelectionIndex()
meth public int getScrollableBlockIncrement(java.awt.Rectangle,int,int)
meth public int getScrollableUnitIncrement(java.awt.Rectangle,int,int)
meth public int getSelectedIndex()
meth public int getSelectionMode()
meth public int getVisibleRowCount()
meth public int locationToIndex(java.awt.Point)
meth public int[] getSelectedIndices()
meth public java.awt.Color getSelectionBackground()
meth public java.awt.Color getSelectionForeground()
meth public java.awt.Component getRenderedComponent(int)
meth public java.awt.Component getRenderedComponent(int,boolean,boolean)
meth public java.awt.Dimension getPreferredScrollableViewportSize()
meth public java.awt.Point getClickPoint(int)
meth public java.awt.Point indexToLocation(int)
meth public java.awt.Rectangle getCellBounds(int,int)
meth public java.lang.Object clickOnItem(int,int)
meth public java.lang.Object clickOnItem(java.lang.String)
meth public java.lang.Object clickOnItem(java.lang.String,boolean,boolean)
meth public java.lang.Object clickOnItem(java.lang.String,boolean,boolean,int)
meth public java.lang.Object clickOnItem(java.lang.String,int)
meth public java.lang.Object clickOnItem(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public java.lang.Object clickOnItem(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator,int)
meth public java.lang.Object getPrototypeCellValue()
meth public java.lang.Object getSelectedValue()
meth public java.lang.Object[] getSelectedValues()
meth public java.util.Hashtable getDump()
meth public javax.swing.ListCellRenderer getCellRenderer()
meth public javax.swing.ListModel getModel()
meth public javax.swing.ListSelectionModel getSelectionModel()
meth public javax.swing.plaf.ListUI getUI()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public static javax.swing.JList findJList(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JList findJList(java.awt.Container,java.lang.String,boolean,boolean,int,int)
meth public static javax.swing.JList findJList(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JList findJList(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JList waitJList(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JList waitJList(java.awt.Container,java.lang.String,boolean,boolean,int,int)
meth public static javax.swing.JList waitJList(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JList waitJList(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void addListSelectionListener(javax.swing.event.ListSelectionListener)
meth public void addSelectionInterval(int,int)
meth public void clearSelection()
meth public void copyEnvironment(org.netbeans.jemmy.operators.Operator)
meth public void ensureIndexIsVisible(int)
meth public void removeListSelectionListener(javax.swing.event.ListSelectionListener)
meth public void removeSelectionInterval(int,int)
meth public void scrollToItem(int)
meth public void scrollToItem(java.lang.String,boolean,boolean)
meth public void scrollToItem(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public void selectItem(int)
meth public void selectItem(java.lang.String)
meth public void selectItem(java.lang.String[])
meth public void selectItems(int[])
meth public void setCellRenderer(javax.swing.ListCellRenderer)
meth public void setFixedCellHeight(int)
meth public void setFixedCellWidth(int)
meth public void setListData(java.lang.Object[])
meth public void setListData(java.util.Vector)
meth public void setModel(javax.swing.ListModel)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setPrototypeCellValue(java.lang.Object)
meth public void setSelectedIndex(int)
meth public void setSelectedIndices(int[])
meth public void setSelectedValue(java.lang.Object,boolean)
meth public void setSelectionBackground(java.awt.Color)
meth public void setSelectionForeground(java.awt.Color)
meth public void setSelectionInterval(int,int)
meth public void setSelectionMode(int)
meth public void setSelectionModel(javax.swing.ListSelectionModel)
meth public void setUI(javax.swing.plaf.ListUI)
meth public void setValueIsAdjusting(boolean)
meth public void setVisibleRowCount(int)
meth public void waitItem(java.lang.String,int)
meth public void waitItemSelection(int,boolean)
meth public void waitItemsSelection(int[],boolean)
supr org.netbeans.jemmy.operators.JComponentOperator
hfds driver,output
hcls ByRenderedComponentListItemChooser,BySubStringListItemChooser

CLSS public static org.netbeans.jemmy.operators.JListOperator$JListByItemFinder
 outer org.netbeans.jemmy.operators.JListOperator
cons public init(java.lang.String,int)
cons public init(java.lang.String,int,org.netbeans.jemmy.operators.Operator$StringComparator)
intf org.netbeans.jemmy.ComponentChooser
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
supr java.lang.Object
hfds comparator,itemIndex,label

CLSS public static org.netbeans.jemmy.operators.JListOperator$JListFinder
 outer org.netbeans.jemmy.operators.JListOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public abstract interface static org.netbeans.jemmy.operators.JListOperator$ListItemChooser
 outer org.netbeans.jemmy.operators.JListOperator
meth public abstract boolean checkItem(org.netbeans.jemmy.operators.JListOperator,int)
meth public abstract java.lang.String getDescription()

CLSS public org.netbeans.jemmy.operators.JListOperator$NoSuchItemException
 outer org.netbeans.jemmy.operators.JListOperator
cons public init(org.netbeans.jemmy.operators.JListOperator,int)
cons public init(org.netbeans.jemmy.operators.JListOperator,java.lang.String)
supr org.netbeans.jemmy.JemmyInputException

CLSS public org.netbeans.jemmy.operators.JMenuBarOperator
cons public init(javax.swing.JMenuBar)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static java.lang.String SUBMENU_PREFIX_DPROP = "Submenu"
innr public static JMenuBarFinder
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth public boolean isBorderPainted()
meth public boolean isSelected()
meth public int getComponentIndex(java.awt.Component)
meth public int getMenuCount()
meth public java.awt.Insets getMargin()
meth public java.util.Hashtable getDump()
meth public javax.swing.JMenu add(javax.swing.JMenu)
meth public javax.swing.JMenu getHelpMenu()
meth public javax.swing.JMenu getMenu(int)
meth public javax.swing.JMenuItem pushMenu(java.lang.String)
meth public javax.swing.JMenuItem pushMenu(java.lang.String,java.lang.String)
meth public javax.swing.JMenuItem pushMenu(java.lang.String,java.lang.String,boolean,boolean)
meth public javax.swing.JMenuItem pushMenu(java.lang.String,java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public javax.swing.JMenuItem pushMenu(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public javax.swing.JMenuItem pushMenu(java.lang.String[])
meth public javax.swing.JMenuItem pushMenu(java.lang.String[],boolean,boolean)
meth public javax.swing.JMenuItem pushMenu(java.lang.String[],org.netbeans.jemmy.operators.Operator$StringComparator)
meth public javax.swing.JMenuItem pushMenu(org.netbeans.jemmy.ComponentChooser[])
meth public javax.swing.MenuElement[] getSubElements()
meth public javax.swing.SingleSelectionModel getSelectionModel()
meth public javax.swing.plaf.MenuBarUI getUI()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public org.netbeans.jemmy.operators.JMenuItemOperator showMenuItem(java.lang.String)
meth public org.netbeans.jemmy.operators.JMenuItemOperator showMenuItem(java.lang.String,java.lang.String)
meth public org.netbeans.jemmy.operators.JMenuItemOperator showMenuItem(java.lang.String,java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public org.netbeans.jemmy.operators.JMenuItemOperator showMenuItem(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public org.netbeans.jemmy.operators.JMenuItemOperator showMenuItem(java.lang.String[])
meth public org.netbeans.jemmy.operators.JMenuItemOperator showMenuItem(java.lang.String[],org.netbeans.jemmy.operators.Operator$StringComparator)
meth public org.netbeans.jemmy.operators.JMenuItemOperator showMenuItem(org.netbeans.jemmy.ComponentChooser[])
meth public org.netbeans.jemmy.operators.JMenuItemOperator[] showMenuItems(java.lang.String)
meth public org.netbeans.jemmy.operators.JMenuItemOperator[] showMenuItems(java.lang.String,java.lang.String)
meth public org.netbeans.jemmy.operators.JMenuItemOperator[] showMenuItems(java.lang.String,java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public org.netbeans.jemmy.operators.JMenuItemOperator[] showMenuItems(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public org.netbeans.jemmy.operators.JMenuItemOperator[] showMenuItems(java.lang.String[])
meth public org.netbeans.jemmy.operators.JMenuItemOperator[] showMenuItems(java.lang.String[],org.netbeans.jemmy.operators.Operator$StringComparator)
meth public org.netbeans.jemmy.operators.JMenuItemOperator[] showMenuItems(org.netbeans.jemmy.ComponentChooser[])
meth public static javax.swing.JMenuBar findJMenuBar(java.awt.Container)
meth public static javax.swing.JMenuBar findJMenuBar(javax.swing.JDialog)
meth public static javax.swing.JMenuBar findJMenuBar(javax.swing.JFrame)
meth public static javax.swing.JMenuBar waitJMenuBar(java.awt.Container)
meth public static javax.swing.JMenuBar waitJMenuBar(javax.swing.JDialog)
meth public static javax.swing.JMenuBar waitJMenuBar(javax.swing.JFrame)
meth public void closeSubmenus()
meth public void copyEnvironment(org.netbeans.jemmy.operators.Operator)
meth public void menuSelectionChanged(boolean)
meth public void processKeyEvent(java.awt.event.KeyEvent,javax.swing.MenuElement[],javax.swing.MenuSelectionManager)
meth public void processMouseEvent(java.awt.event.MouseEvent,javax.swing.MenuElement[],javax.swing.MenuSelectionManager)
meth public void pushMenuNoBlock(java.lang.String)
meth public void pushMenuNoBlock(java.lang.String,java.lang.String)
meth public void pushMenuNoBlock(java.lang.String,java.lang.String,boolean,boolean)
meth public void pushMenuNoBlock(java.lang.String,java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public void pushMenuNoBlock(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public void pushMenuNoBlock(java.lang.String[])
meth public void pushMenuNoBlock(java.lang.String[],boolean,boolean)
meth public void pushMenuNoBlock(java.lang.String[],org.netbeans.jemmy.operators.Operator$StringComparator)
meth public void pushMenuNoBlock(org.netbeans.jemmy.ComponentChooser[])
meth public void setBorderPainted(boolean)
meth public void setHelpMenu(javax.swing.JMenu)
meth public void setMargin(java.awt.Insets)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setSelected(java.awt.Component)
meth public void setSelectionModel(javax.swing.SingleSelectionModel)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
meth public void setUI(javax.swing.plaf.MenuBarUI)
supr org.netbeans.jemmy.operators.JComponentOperator
hfds driver,output,timeouts

CLSS public static org.netbeans.jemmy.operators.JMenuBarOperator$JMenuBarFinder
 outer org.netbeans.jemmy.operators.JMenuBarOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.JMenuItemOperator
cons public init(javax.swing.JMenuItem)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
innr public static JMenuItemByLabelFinder
innr public static JMenuItemFinder
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth protected void prepareToClick()
meth public boolean isArmed()
meth public java.awt.Component getComponent()
meth public java.util.Hashtable getDump()
meth public javax.swing.KeyStroke getAccelerator()
meth public javax.swing.MenuElement[] getSubElements()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public static javax.swing.JMenuItem findJMenuItem(java.awt.Container,java.lang.String,boolean,boolean)
meth public static javax.swing.JMenuItem findJMenuItem(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JMenuItem findJMenuItem(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JMenuItem findJMenuItem(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JMenuItem waitJMenuItem(java.awt.Container,java.lang.String,boolean,boolean)
meth public static javax.swing.JMenuItem waitJMenuItem(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JMenuItem waitJMenuItem(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JMenuItem waitJMenuItem(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void addMenuDragMouseListener(javax.swing.event.MenuDragMouseListener)
meth public void addMenuKeyListener(javax.swing.event.MenuKeyListener)
meth public void menuSelectionChanged(boolean)
meth public void processKeyEvent(java.awt.event.KeyEvent,javax.swing.MenuElement[],javax.swing.MenuSelectionManager)
meth public void processMenuDragMouseEvent(javax.swing.event.MenuDragMouseEvent)
meth public void processMenuKeyEvent(javax.swing.event.MenuKeyEvent)
meth public void processMouseEvent(java.awt.event.MouseEvent,javax.swing.MenuElement[],javax.swing.MenuSelectionManager)
meth public void push()
meth public void pushNoBlock()
meth public void removeMenuDragMouseListener(javax.swing.event.MenuDragMouseListener)
meth public void removeMenuKeyListener(javax.swing.event.MenuKeyListener)
meth public void setAccelerator(javax.swing.KeyStroke)
meth public void setArmed(boolean)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
meth public void setUI(javax.swing.plaf.MenuItemUI)
supr org.netbeans.jemmy.operators.AbstractButtonOperator
hfds PUSH_MENU_TIMEOUT,output,timeouts

CLSS public static org.netbeans.jemmy.operators.JMenuItemOperator$JMenuItemByLabelFinder
 outer org.netbeans.jemmy.operators.JMenuItemOperator
cons public init(java.lang.String)
cons public init(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
intf org.netbeans.jemmy.ComponentChooser
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
supr java.lang.Object
hfds comparator,label

CLSS public static org.netbeans.jemmy.operators.JMenuItemOperator$JMenuItemFinder
 outer org.netbeans.jemmy.operators.JMenuItemOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.JMenuOperator
cons public init(javax.swing.JMenu)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static java.lang.String SUBMENU_PREFIX_DPROP = "Submenu"
innr public static JMenuByLabelFinder
innr public static JMenuFinder
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth public boolean isMenuComponent(java.awt.Component)
meth public boolean isPopupMenuVisible()
meth public boolean isTearOff()
meth public boolean isTopLevelMenu()
meth public int getDelay()
meth public int getItemCount()
meth public int getMenuComponentCount()
meth public java.awt.Component getMenuComponent(int)
meth public java.awt.Component[] getMenuComponents()
meth public java.util.Hashtable getDump()
meth public javax.swing.JMenuItem add(java.lang.String)
meth public javax.swing.JMenuItem add(javax.swing.Action)
meth public javax.swing.JMenuItem add(javax.swing.JMenuItem)
meth public javax.swing.JMenuItem getItem(int)
meth public javax.swing.JMenuItem insert(javax.swing.Action,int)
meth public javax.swing.JMenuItem insert(javax.swing.JMenuItem,int)
meth public javax.swing.JMenuItem pushMenu(java.lang.String)
meth public javax.swing.JMenuItem pushMenu(java.lang.String,java.lang.String)
meth public javax.swing.JMenuItem pushMenu(java.lang.String,java.lang.String,boolean,boolean)
meth public javax.swing.JMenuItem pushMenu(java.lang.String,java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public javax.swing.JMenuItem pushMenu(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public javax.swing.JMenuItem pushMenu(java.lang.String[])
meth public javax.swing.JMenuItem pushMenu(java.lang.String[],boolean,boolean)
meth public javax.swing.JMenuItem pushMenu(java.lang.String[],org.netbeans.jemmy.operators.Operator$StringComparator)
meth public javax.swing.JMenuItem pushMenu(org.netbeans.jemmy.ComponentChooser[])
meth public javax.swing.JPopupMenu getPopupMenu()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public org.netbeans.jemmy.operators.JMenuItemOperator showMenuItem(java.lang.String)
meth public org.netbeans.jemmy.operators.JMenuItemOperator showMenuItem(java.lang.String,java.lang.String)
meth public org.netbeans.jemmy.operators.JMenuItemOperator showMenuItem(java.lang.String,java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public org.netbeans.jemmy.operators.JMenuItemOperator showMenuItem(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public org.netbeans.jemmy.operators.JMenuItemOperator showMenuItem(java.lang.String[])
meth public org.netbeans.jemmy.operators.JMenuItemOperator showMenuItem(java.lang.String[],org.netbeans.jemmy.operators.Operator$StringComparator)
meth public org.netbeans.jemmy.operators.JMenuItemOperator showMenuItem(org.netbeans.jemmy.ComponentChooser[])
meth public org.netbeans.jemmy.operators.JMenuItemOperator[] showMenuItems(java.lang.String)
meth public org.netbeans.jemmy.operators.JMenuItemOperator[] showMenuItems(java.lang.String,java.lang.String)
meth public org.netbeans.jemmy.operators.JMenuItemOperator[] showMenuItems(java.lang.String,java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public org.netbeans.jemmy.operators.JMenuItemOperator[] showMenuItems(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public org.netbeans.jemmy.operators.JMenuItemOperator[] showMenuItems(java.lang.String[])
meth public org.netbeans.jemmy.operators.JMenuItemOperator[] showMenuItems(java.lang.String[],org.netbeans.jemmy.operators.Operator$StringComparator)
meth public org.netbeans.jemmy.operators.JMenuItemOperator[] showMenuItems(org.netbeans.jemmy.ComponentChooser[])
meth public static javax.swing.JMenu findJMenu(java.awt.Container,java.lang.String,boolean,boolean)
meth public static javax.swing.JMenu findJMenu(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JMenu findJMenu(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JMenu findJMenu(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JMenu waitJMenu(java.awt.Container,java.lang.String,boolean,boolean)
meth public static javax.swing.JMenu waitJMenu(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JMenu waitJMenu(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JMenu waitJMenu(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static void performInit()
meth public void addMenuListener(javax.swing.event.MenuListener)
meth public void addSeparator()
meth public void copyEnvironment(org.netbeans.jemmy.operators.Operator)
meth public void insert(java.lang.String,int)
meth public void insertSeparator(int)
meth public void pushMenuNoBlock(java.lang.String)
meth public void pushMenuNoBlock(java.lang.String,java.lang.String)
meth public void pushMenuNoBlock(java.lang.String,java.lang.String,boolean,boolean)
meth public void pushMenuNoBlock(java.lang.String,java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public void pushMenuNoBlock(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public void pushMenuNoBlock(java.lang.String[])
meth public void pushMenuNoBlock(java.lang.String[],boolean,boolean)
meth public void pushMenuNoBlock(java.lang.String[],org.netbeans.jemmy.operators.Operator$StringComparator)
meth public void pushMenuNoBlock(org.netbeans.jemmy.ComponentChooser[])
meth public void remove(javax.swing.JMenuItem)
meth public void removeMenuListener(javax.swing.event.MenuListener)
meth public void setDelay(int)
meth public void setMenuLocation(int,int)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setPopupMenuVisible(boolean)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
supr org.netbeans.jemmy.operators.JMenuItemOperator
hfds PUSH_MENU_TIMEOUT,WAIT_BEFORE_POPUP_TIMEOUT,WAIT_POPUP_TIMEOUT,driver,output,timeouts

CLSS public static org.netbeans.jemmy.operators.JMenuOperator$JMenuByLabelFinder
 outer org.netbeans.jemmy.operators.JMenuOperator
cons public init(java.lang.String)
cons public init(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
intf org.netbeans.jemmy.ComponentChooser
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
supr java.lang.Object
hfds comparator,label

CLSS public static org.netbeans.jemmy.operators.JMenuOperator$JMenuFinder
 outer org.netbeans.jemmy.operators.JMenuOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.JPasswordFieldOperator
cons public init(javax.swing.JPasswordField)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static java.lang.String ECHO_CHAR_DPROP = "Echo char"
innr public static JPasswordFieldFinder
meth public boolean echoCharIsSet()
meth public char getEchoChar()
meth public char[] getPassword()
meth public java.util.Hashtable getDump()
meth public static javax.swing.JPasswordField findJPasswordField(java.awt.Container,java.lang.String,boolean,boolean)
meth public static javax.swing.JPasswordField findJPasswordField(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JPasswordField findJPasswordField(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JPasswordField findJPasswordField(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JPasswordField waitJPasswordField(java.awt.Container,java.lang.String,boolean,boolean)
meth public static javax.swing.JPasswordField waitJPasswordField(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JPasswordField waitJPasswordField(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JPasswordField waitJPasswordField(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void setEchoChar(char)
supr org.netbeans.jemmy.operators.JTextFieldOperator

CLSS public static org.netbeans.jemmy.operators.JPasswordFieldOperator$JPasswordFieldFinder
 outer org.netbeans.jemmy.operators.JPasswordFieldOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.JPopupMenuOperator
cons public init()
cons public init(javax.swing.JPopupMenu)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
cons public init(org.netbeans.jemmy.operators.Operator)
fld public final static java.lang.String LABEL_DPROP = "Label"
innr public static JPopupMenuFinder
innr public static JPopupWindowFinder
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth public boolean isBorderPainted()
meth public boolean isLightWeightPopupEnabled()
meth public int getComponentIndex(java.awt.Component)
meth public java.awt.Component getInvoker()
meth public java.awt.Insets getMargin()
meth public java.lang.String getLabel()
meth public java.util.Hashtable getDump()
meth public javax.swing.JMenuItem add(java.lang.String)
meth public javax.swing.JMenuItem add(javax.swing.Action)
meth public javax.swing.JMenuItem add(javax.swing.JMenuItem)
meth public javax.swing.JMenuItem pushMenu(java.lang.String)
meth public javax.swing.JMenuItem pushMenu(java.lang.String,java.lang.String)
meth public javax.swing.JMenuItem pushMenu(java.lang.String,java.lang.String,boolean,boolean)
meth public javax.swing.JMenuItem pushMenu(java.lang.String,java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public javax.swing.JMenuItem pushMenu(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public javax.swing.JMenuItem pushMenu(java.lang.String[])
meth public javax.swing.JMenuItem pushMenu(java.lang.String[],boolean,boolean)
meth public javax.swing.JMenuItem pushMenu(java.lang.String[],org.netbeans.jemmy.operators.Operator$StringComparator)
meth public javax.swing.JMenuItem pushMenu(org.netbeans.jemmy.ComponentChooser[])
meth public javax.swing.MenuElement[] getSubElements()
meth public javax.swing.SingleSelectionModel getSelectionModel()
meth public javax.swing.plaf.PopupMenuUI getUI()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public org.netbeans.jemmy.operators.JMenuItemOperator showMenuItem(java.lang.String)
meth public org.netbeans.jemmy.operators.JMenuItemOperator showMenuItem(java.lang.String,java.lang.String)
meth public org.netbeans.jemmy.operators.JMenuItemOperator showMenuItem(java.lang.String,java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public org.netbeans.jemmy.operators.JMenuItemOperator showMenuItem(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public org.netbeans.jemmy.operators.JMenuItemOperator showMenuItem(java.lang.String[])
meth public org.netbeans.jemmy.operators.JMenuItemOperator showMenuItem(java.lang.String[],org.netbeans.jemmy.operators.Operator$StringComparator)
meth public org.netbeans.jemmy.operators.JMenuItemOperator showMenuItem(org.netbeans.jemmy.ComponentChooser[])
meth public org.netbeans.jemmy.operators.JMenuItemOperator[] showMenuItems(java.lang.String)
meth public org.netbeans.jemmy.operators.JMenuItemOperator[] showMenuItems(java.lang.String,java.lang.String)
meth public org.netbeans.jemmy.operators.JMenuItemOperator[] showMenuItems(java.lang.String,java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public org.netbeans.jemmy.operators.JMenuItemOperator[] showMenuItems(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public org.netbeans.jemmy.operators.JMenuItemOperator[] showMenuItems(java.lang.String[])
meth public org.netbeans.jemmy.operators.JMenuItemOperator[] showMenuItems(java.lang.String[],org.netbeans.jemmy.operators.Operator$StringComparator)
meth public org.netbeans.jemmy.operators.JMenuItemOperator[] showMenuItems(org.netbeans.jemmy.ComponentChooser[])
meth public static java.awt.Window findJPopupWindow(org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.Window waitJPopupWindow(org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JPopupMenu callPopup(java.awt.Component)
meth public static javax.swing.JPopupMenu callPopup(java.awt.Component,int)
meth public static javax.swing.JPopupMenu callPopup(java.awt.Component,int,int)
meth public static javax.swing.JPopupMenu callPopup(java.awt.Component,int,int,int)
meth public static javax.swing.JPopupMenu callPopup(org.netbeans.jemmy.operators.ComponentOperator,int,int)
meth public static javax.swing.JPopupMenu callPopup(org.netbeans.jemmy.operators.ComponentOperator,int,int,int)
meth public static javax.swing.JPopupMenu findJPopupMenu(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JPopupMenu findJPopupMenu(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JPopupMenu waitJPopupMenu(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JPopupMenu waitJPopupMenu(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static org.netbeans.jemmy.operators.JPopupMenuOperator waitJPopupMenu(java.lang.String)
meth public static org.netbeans.jemmy.operators.JPopupMenuOperator waitJPopupMenu(org.netbeans.jemmy.ComponentChooser)
meth public void addPopupMenuListener(javax.swing.event.PopupMenuListener)
meth public void addSeparator()
meth public void copyEnvironment(org.netbeans.jemmy.operators.Operator)
meth public void insert(java.awt.Component,int)
meth public void insert(javax.swing.Action,int)
meth public void menuSelectionChanged(boolean)
meth public void pack()
meth public void processKeyEvent(java.awt.event.KeyEvent,javax.swing.MenuElement[],javax.swing.MenuSelectionManager)
meth public void processMouseEvent(java.awt.event.MouseEvent,javax.swing.MenuElement[],javax.swing.MenuSelectionManager)
meth public void pushMenuNoBlock(java.lang.String)
meth public void pushMenuNoBlock(java.lang.String,java.lang.String)
meth public void pushMenuNoBlock(java.lang.String,java.lang.String,boolean,boolean)
meth public void pushMenuNoBlock(java.lang.String,java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public void pushMenuNoBlock(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public void pushMenuNoBlock(java.lang.String[])
meth public void pushMenuNoBlock(java.lang.String[],boolean,boolean)
meth public void pushMenuNoBlock(java.lang.String[],org.netbeans.jemmy.operators.Operator$StringComparator)
meth public void pushMenuNoBlock(org.netbeans.jemmy.ComponentChooser[])
meth public void removePopupMenuListener(javax.swing.event.PopupMenuListener)
meth public void setBorderPainted(boolean)
meth public void setInvoker(java.awt.Component)
meth public void setLabel(java.lang.String)
meth public void setLightWeightPopupEnabled(boolean)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setPopupSize(int,int)
meth public void setPopupSize(java.awt.Dimension)
meth public void setSelected(java.awt.Component)
meth public void setSelectionModel(javax.swing.SingleSelectionModel)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
meth public void setUI(javax.swing.plaf.PopupMenuUI)
meth public void show(java.awt.Component,int,int)
supr org.netbeans.jemmy.operators.JComponentOperator
hfds driver,output,timeouts

CLSS public static org.netbeans.jemmy.operators.JPopupMenuOperator$JPopupMenuFinder
 outer org.netbeans.jemmy.operators.JPopupMenuOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
meth public boolean checkComponent(java.awt.Component)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public static org.netbeans.jemmy.operators.JPopupMenuOperator$JPopupWindowFinder
 outer org.netbeans.jemmy.operators.JPopupMenuOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
intf org.netbeans.jemmy.ComponentChooser
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
supr java.lang.Object
hfds ppFinder,subFinder

CLSS public org.netbeans.jemmy.operators.JProgressBarOperator
cons public init(javax.swing.JProgressBar)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static java.lang.String MAXIMUM_DPROP = "Maximum"
fld public final static java.lang.String MINIMUM_DPROP = "Minimum"
fld public final static java.lang.String VALUE_DPROP = "Value"
innr public abstract interface static ValueChooser
innr public static JProgressBarFinder
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth public boolean isBorderPainted()
meth public boolean isStringPainted()
meth public double getPercentComplete()
meth public int getMaximum()
meth public int getMinimum()
meth public int getOrientation()
meth public int getValue()
meth public java.lang.String getString()
meth public java.util.Hashtable getDump()
meth public javax.swing.BoundedRangeModel getModel()
meth public javax.swing.plaf.ProgressBarUI getUI()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public static javax.swing.JProgressBar findJProgressBar(java.awt.Container)
meth public static javax.swing.JProgressBar findJProgressBar(java.awt.Container,int)
meth public static javax.swing.JProgressBar findJProgressBar(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JProgressBar findJProgressBar(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JProgressBar waitJProgressBar(java.awt.Container)
meth public static javax.swing.JProgressBar waitJProgressBar(java.awt.Container,int)
meth public static javax.swing.JProgressBar waitJProgressBar(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JProgressBar waitJProgressBar(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void setBorderPainted(boolean)
meth public void setMaximum(int)
meth public void setMinimum(int)
meth public void setModel(javax.swing.BoundedRangeModel)
meth public void setOrientation(int)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setString(java.lang.String)
meth public void setStringPainted(boolean)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
meth public void setUI(javax.swing.plaf.ProgressBarUI)
meth public void setValue(int)
meth public void waitValue(int)
meth public void waitValue(java.lang.String)
meth public void waitValue(org.netbeans.jemmy.operators.JProgressBarOperator$ValueChooser)
supr org.netbeans.jemmy.operators.JComponentOperator
hfds WAIT_VALUE_TIMEOUT,output,timeouts

CLSS public static org.netbeans.jemmy.operators.JProgressBarOperator$JProgressBarFinder
 outer org.netbeans.jemmy.operators.JProgressBarOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public abstract interface static org.netbeans.jemmy.operators.JProgressBarOperator$ValueChooser
 outer org.netbeans.jemmy.operators.JProgressBarOperator
meth public abstract boolean checkValue(int)
meth public abstract java.lang.String getDescription()

CLSS public org.netbeans.jemmy.operators.JRadioButtonMenuItemOperator
cons public init(javax.swing.JRadioButtonMenuItem)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
innr public static JRadioButtonMenuItemByLabelFinder
innr public static JRadioButtonMenuItemFinder
supr org.netbeans.jemmy.operators.JMenuItemOperator

CLSS public static org.netbeans.jemmy.operators.JRadioButtonMenuItemOperator$JRadioButtonMenuItemByLabelFinder
 outer org.netbeans.jemmy.operators.JRadioButtonMenuItemOperator
cons public init(java.lang.String)
cons public init(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
intf org.netbeans.jemmy.ComponentChooser
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
supr java.lang.Object
hfds comparator,label

CLSS public static org.netbeans.jemmy.operators.JRadioButtonMenuItemOperator$JRadioButtonMenuItemFinder
 outer org.netbeans.jemmy.operators.JRadioButtonMenuItemOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.JRadioButtonOperator
cons public init(javax.swing.JRadioButton)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
innr public static JRadioButtonFinder
meth public static javax.swing.JRadioButton findJRadioButton(java.awt.Container,java.lang.String,boolean,boolean)
meth public static javax.swing.JRadioButton findJRadioButton(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JRadioButton findJRadioButton(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JRadioButton findJRadioButton(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JRadioButton waitJRadioButton(java.awt.Container,java.lang.String,boolean,boolean)
meth public static javax.swing.JRadioButton waitJRadioButton(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JRadioButton waitJRadioButton(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JRadioButton waitJRadioButton(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
supr org.netbeans.jemmy.operators.JToggleButtonOperator

CLSS public static org.netbeans.jemmy.operators.JRadioButtonOperator$JRadioButtonFinder
 outer org.netbeans.jemmy.operators.JRadioButtonOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.JScrollBarOperator
cons public init(javax.swing.JScrollBar)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static java.lang.String HORIZONTAL_ORIENTATION_DPROP_VALUE = "HORIZONTAL"
fld public final static java.lang.String MAXIMUM_DPROP = "Maximum"
fld public final static java.lang.String MINIMUM_DPROP = "Minimum"
fld public final static java.lang.String ORIENTATION_DPROP = "Orientation"
fld public final static java.lang.String VALUE_DPROP = "Value"
fld public final static java.lang.String VERTICAL_ORIENTATION_DPROP_VALUE = "VERTICAL"
innr public abstract interface static ScrollChecker
innr public static JScrollBarFinder
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth public boolean getValueIsAdjusting()
meth public int getBlockIncrement()
meth public int getBlockIncrement(int)
meth public int getMaximum()
meth public int getMinimum()
meth public int getOrientation()
meth public int getUnitIncrement()
meth public int getUnitIncrement(int)
meth public int getValue()
meth public int getVisibleAmount()
meth public java.util.Hashtable getDump()
meth public javax.swing.BoundedRangeModel getModel()
meth public javax.swing.plaf.ScrollBarUI getUI()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public org.netbeans.jemmy.operators.JButtonOperator getDecreaseButton()
meth public org.netbeans.jemmy.operators.JButtonOperator getIncreaseButton()
meth public static javax.swing.JScrollBar findJScrollBar(java.awt.Container)
meth public static javax.swing.JScrollBar findJScrollBar(java.awt.Container,int)
meth public static javax.swing.JScrollBar findJScrollBar(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JScrollBar findJScrollBar(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JScrollBar waitJScrollBar(java.awt.Container)
meth public static javax.swing.JScrollBar waitJScrollBar(java.awt.Container,int)
meth public static javax.swing.JScrollBar waitJScrollBar(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JScrollBar waitJScrollBar(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void addAdjustmentListener(java.awt.event.AdjustmentListener)
meth public void copyEnvironment(org.netbeans.jemmy.operators.Operator)
meth public void removeAdjustmentListener(java.awt.event.AdjustmentListener)
meth public void scroll(boolean)
meth public void scrollTo(org.netbeans.jemmy.Waitable,java.lang.Object,boolean)
meth public void scrollTo(org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster)
meth public void scrollTo(org.netbeans.jemmy.operators.JScrollBarOperator$ScrollChecker)
meth public void scrollToMaximum()
meth public void scrollToMinimum()
meth public void scrollToValue(double)
meth public void scrollToValue(int)
meth public void setBlockIncrement(int)
meth public void setMaximum(int)
meth public void setMinimum(int)
meth public void setModel(javax.swing.BoundedRangeModel)
meth public void setOrientation(int)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
meth public void setUnitIncrement(int)
meth public void setValue(int)
meth public void setValueIsAdjusting(boolean)
meth public void setValues(int,int,int,int)
meth public void setVisibleAmount(int)
supr org.netbeans.jemmy.operators.JComponentOperator
hfds BEFORE_DROP_TIMEOUT,DRAG_AND_DROP_SCROLLING_DELTA,ONE_SCROLL_CLICK_TIMEOUT,WHOLE_SCROLL_TIMEOUT,driver,maxButtOperator,minButtOperator,output,timeouts
hcls CheckerAdjustable,ValueScrollAdjuster,WaitableChecker

CLSS public static org.netbeans.jemmy.operators.JScrollBarOperator$JScrollBarFinder
 outer org.netbeans.jemmy.operators.JScrollBarOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public abstract interface static org.netbeans.jemmy.operators.JScrollBarOperator$ScrollChecker
 outer org.netbeans.jemmy.operators.JScrollBarOperator
meth public abstract int getScrollDirection(org.netbeans.jemmy.operators.JScrollBarOperator)
meth public abstract java.lang.String getDescription()

CLSS public org.netbeans.jemmy.operators.JScrollPaneOperator
cons public init(javax.swing.JScrollPane)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
innr public static JScrollPaneFinder
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth public boolean checkInside(java.awt.Component)
meth public boolean checkInside(java.awt.Component,int,int,int,int)
meth public int getHorizontalScrollBarPolicy()
meth public int getVerticalScrollBarPolicy()
meth public java.awt.Component getCorner(java.lang.String)
meth public java.awt.Rectangle getViewportBorderBounds()
meth public javax.swing.JScrollBar createHorizontalScrollBar()
meth public javax.swing.JScrollBar createVerticalScrollBar()
meth public javax.swing.JScrollBar getHorizontalScrollBar()
meth public javax.swing.JScrollBar getVerticalScrollBar()
meth public javax.swing.JViewport getColumnHeader()
meth public javax.swing.JViewport getRowHeader()
meth public javax.swing.JViewport getViewport()
meth public javax.swing.border.Border getViewportBorder()
meth public javax.swing.plaf.ScrollPaneUI getUI()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public org.netbeans.jemmy.operators.JScrollBarOperator getHScrollBarOperator()
meth public org.netbeans.jemmy.operators.JScrollBarOperator getVScrollBarOperator()
meth public static javax.swing.JScrollPane findJScrollPane(java.awt.Container)
meth public static javax.swing.JScrollPane findJScrollPane(java.awt.Container,int)
meth public static javax.swing.JScrollPane findJScrollPane(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JScrollPane findJScrollPane(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JScrollPane findJScrollPaneUnder(java.awt.Component)
meth public static javax.swing.JScrollPane findJScrollPaneUnder(java.awt.Component,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JScrollPane waitJScrollPane(java.awt.Container)
meth public static javax.swing.JScrollPane waitJScrollPane(java.awt.Container,int)
meth public static javax.swing.JScrollPane waitJScrollPane(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JScrollPane waitJScrollPane(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void scrollToBottom()
meth public void scrollToComponent(java.awt.Component)
meth public void scrollToComponentPoint(java.awt.Component,int,int)
meth public void scrollToComponentRectangle(java.awt.Component,int,int,int,int)
meth public void scrollToHorizontalValue(double)
meth public void scrollToHorizontalValue(int)
meth public void scrollToLeft()
meth public void scrollToRight()
meth public void scrollToTop()
meth public void scrollToValues(double,double)
meth public void scrollToValues(int,int)
meth public void scrollToVerticalValue(double)
meth public void scrollToVerticalValue(int)
meth public void setColumnHeader(javax.swing.JViewport)
meth public void setColumnHeaderView(java.awt.Component)
meth public void setCorner(java.lang.String,java.awt.Component)
meth public void setHorizontalScrollBar(javax.swing.JScrollBar)
meth public void setHorizontalScrollBarPolicy(int)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setRowHeader(javax.swing.JViewport)
meth public void setRowHeaderView(java.awt.Component)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
meth public void setUI(javax.swing.plaf.ScrollPaneUI)
meth public void setValues(int,int)
meth public void setVerticalScrollBar(javax.swing.JScrollBar)
meth public void setVerticalScrollBarPolicy(int)
meth public void setViewport(javax.swing.JViewport)
meth public void setViewportBorder(javax.swing.border.Border)
meth public void setViewportView(java.awt.Component)
supr org.netbeans.jemmy.operators.JComponentOperator
hfds X_POINT_RECT_SIZE,Y_POINT_RECT_SIZE,hScrollBarOper,output,timeouts,vScrollBarOper
hcls ComponentRectChecker

CLSS public static org.netbeans.jemmy.operators.JScrollPaneOperator$JScrollPaneFinder
 outer org.netbeans.jemmy.operators.JScrollPaneOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.JSliderOperator
cons public init(javax.swing.JSlider)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static int CLICK_SCROLL_MODEL = 1
fld public final static int PUSH_AND_WAIT_SCROLL_MODEL = 2
fld public final static java.lang.String HORIZONTAL_ORIENTATION_DPROP_VALUE = "HORIZONTAL"
fld public final static java.lang.String IS_INVERTED_DPROP = "Inverted"
fld public final static java.lang.String MAXIMUM_DPROP = "Maximum"
fld public final static java.lang.String MINIMUM_DPROP = "Minimum"
fld public final static java.lang.String ORIENTATION_DPROP = "Orientation"
fld public final static java.lang.String VALUE_DPROP = "Value"
fld public final static java.lang.String VERTICAL_ORIENTATION_DPROP_VALUE = "VERTICAL"
innr public static JSliderFinder
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth public boolean getInverted()
meth public boolean getPaintLabels()
meth public boolean getPaintTicks()
meth public boolean getPaintTrack()
meth public boolean getSnapToTicks()
meth public boolean getValueIsAdjusting()
meth public int getExtent()
meth public int getMajorTickSpacing()
meth public int getMaximum()
meth public int getMinimum()
meth public int getMinorTickSpacing()
meth public int getOrientation()
meth public int getScrollModel()
meth public int getValue()
meth public java.util.Dictionary getLabelTable()
meth public java.util.Hashtable createStandardLabels(int)
meth public java.util.Hashtable createStandardLabels(int,int)
meth public java.util.Hashtable getDump()
meth public javax.swing.BoundedRangeModel getModel()
meth public javax.swing.plaf.SliderUI getUI()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public static javax.swing.JSlider findJSlider(java.awt.Container)
meth public static javax.swing.JSlider findJSlider(java.awt.Container,int)
meth public static javax.swing.JSlider findJSlider(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JSlider findJSlider(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JSlider waitJSlider(java.awt.Container)
meth public static javax.swing.JSlider waitJSlider(java.awt.Container,int)
meth public static javax.swing.JSlider waitJSlider(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JSlider waitJSlider(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void scrollTo(org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster)
meth public void scrollToMaximum()
meth public void scrollToMinimum()
meth public void scrollToValue(int)
meth public void setExtent(int)
meth public void setInverted(boolean)
meth public void setLabelTable(java.util.Dictionary)
meth public void setMajorTickSpacing(int)
meth public void setMaximum(int)
meth public void setMinimum(int)
meth public void setMinorTickSpacing(int)
meth public void setModel(javax.swing.BoundedRangeModel)
meth public void setOrientation(int)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setPaintLabels(boolean)
meth public void setPaintTicks(boolean)
meth public void setPaintTrack(boolean)
meth public void setScrollModel(int)
meth public void setSnapToTicks(boolean)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
meth public void setUI(javax.swing.plaf.SliderUI)
meth public void setValue(int)
meth public void setValueIsAdjusting(boolean)
supr org.netbeans.jemmy.operators.JComponentOperator
hfds ONE_SCROLL_CLICK_TIMEOUT,SCROLLING_DELTA,WHOLE_SCROLL_TIMEOUT,driver,output,scrollModel,timeouts
hcls ValueScrollAdjuster

CLSS public static org.netbeans.jemmy.operators.JSliderOperator$JSliderFinder
 outer org.netbeans.jemmy.operators.JSliderOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.JSpinnerOperator
cons public init(javax.swing.JSpinner)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static java.lang.String VALUE_DPROP = "Value"
innr public abstract static ObjectScrollAdjuster
innr public static DateScrollAdjuster
innr public static DateSpinnerOperator
innr public static ExactScrollAdjuster
innr public static JSpinnerByTextFinder
innr public static JSpinnerFinder
innr public static ListScrollAdjuster
innr public static ListSpinnerOperator
innr public static NumberScrollAdjuster
innr public static NumberSpinnerOperator
innr public static SpinnerModelException
innr public static ToStringScrollAdjuster
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth public java.lang.Object getMaximum()
meth public java.lang.Object getMinimum()
meth public java.lang.Object getNextValue()
meth public java.lang.Object getPreviousValue()
meth public java.lang.Object getValue()
meth public java.util.Hashtable getDump()
meth public javax.swing.JComponent getEditor()
meth public javax.swing.SpinnerModel getModel()
meth public javax.swing.event.ChangeListener[] getChangeListeners()
meth public javax.swing.plaf.SpinnerUI getUI()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public org.netbeans.jemmy.operators.JButtonOperator getDecreaseOperator()
meth public org.netbeans.jemmy.operators.JButtonOperator getIncreaseOperator()
meth public org.netbeans.jemmy.operators.JSpinnerOperator$DateSpinnerOperator getDateSpinner()
meth public org.netbeans.jemmy.operators.JSpinnerOperator$ListSpinnerOperator getListSpinner()
meth public org.netbeans.jemmy.operators.JSpinnerOperator$NumberSpinnerOperator getNumberSpinner()
meth public static javax.swing.JSpinner findJSpinner(java.awt.Container)
meth public static javax.swing.JSpinner findJSpinner(java.awt.Container,int)
meth public static javax.swing.JSpinner findJSpinner(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JSpinner findJSpinner(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JSpinner waitJSpinner(java.awt.Container)
meth public static javax.swing.JSpinner waitJSpinner(java.awt.Container,int)
meth public static javax.swing.JSpinner waitJSpinner(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JSpinner waitJSpinner(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static void checkModel(org.netbeans.jemmy.operators.JSpinnerOperator,java.lang.Class)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void commitEdit()
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void scrollTo(org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster)
meth public void scrollToMaximum()
meth public void scrollToMinimum()
meth public void scrollToObject(java.lang.Object,int)
meth public void scrollToString(java.lang.String,int)
meth public void scrollToString(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator,int)
meth public void setEditor(javax.swing.JComponent)
meth public void setModel(javax.swing.SpinnerModel)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
meth public void setUI(javax.swing.plaf.SpinnerUI)
meth public void setValue(java.lang.Object)
supr org.netbeans.jemmy.operators.JComponentOperator
hfds WHOLE_SCROLL_TIMEOUT,decreaseOperator,driver,increaseOperator,output,timeouts

CLSS public static org.netbeans.jemmy.operators.JSpinnerOperator$DateScrollAdjuster
 outer org.netbeans.jemmy.operators.JSpinnerOperator
cons public init(org.netbeans.jemmy.operators.JSpinnerOperator,java.util.Date)
intf org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster
meth public int getScrollDirection()
meth public int getScrollOrientation()
meth public java.lang.String getDescription()
supr java.lang.Object
hfds date,model

CLSS public static org.netbeans.jemmy.operators.JSpinnerOperator$DateSpinnerOperator
 outer org.netbeans.jemmy.operators.JSpinnerOperator
meth public javax.swing.SpinnerDateModel getDateModel()
meth public void scrollToDate(java.util.Date)
supr org.netbeans.jemmy.operators.JSpinnerOperator

CLSS public static org.netbeans.jemmy.operators.JSpinnerOperator$ExactScrollAdjuster
 outer org.netbeans.jemmy.operators.JSpinnerOperator
cons public init(org.netbeans.jemmy.operators.JSpinnerOperator,java.lang.Object,int)
meth public boolean equals(java.lang.Object)
meth public int getScrollOrientation()
meth public java.lang.String getDescription()
supr org.netbeans.jemmy.operators.JSpinnerOperator$ObjectScrollAdjuster
hfds obj

CLSS public static org.netbeans.jemmy.operators.JSpinnerOperator$JSpinnerByTextFinder
 outer org.netbeans.jemmy.operators.JSpinnerOperator
cons public init(java.lang.String)
cons public init(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
intf org.netbeans.jemmy.ComponentChooser
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
supr java.lang.Object
hfds comparator,label

CLSS public static org.netbeans.jemmy.operators.JSpinnerOperator$JSpinnerFinder
 outer org.netbeans.jemmy.operators.JSpinnerOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public static org.netbeans.jemmy.operators.JSpinnerOperator$ListScrollAdjuster
 outer org.netbeans.jemmy.operators.JSpinnerOperator
cons public init(org.netbeans.jemmy.operators.JSpinnerOperator,int)
cons public init(org.netbeans.jemmy.operators.JSpinnerOperator,java.lang.Object)
intf org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster
meth public int getScrollDirection()
meth public int getScrollOrientation()
meth public java.lang.String getDescription()
supr java.lang.Object
hfds elements,itemIndex,model

CLSS public static org.netbeans.jemmy.operators.JSpinnerOperator$ListSpinnerOperator
 outer org.netbeans.jemmy.operators.JSpinnerOperator
meth public int findItem(java.lang.String)
meth public int findItem(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public javax.swing.SpinnerListModel getListModel()
meth public void scrollToIndex(int)
meth public void scrollToString(java.lang.String)
meth public void scrollToString(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
supr org.netbeans.jemmy.operators.JSpinnerOperator

CLSS public static org.netbeans.jemmy.operators.JSpinnerOperator$NumberScrollAdjuster
 outer org.netbeans.jemmy.operators.JSpinnerOperator
cons public init(org.netbeans.jemmy.operators.JSpinnerOperator,double)
cons public init(org.netbeans.jemmy.operators.JSpinnerOperator,java.lang.Number)
intf org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster
meth public int getScrollDirection()
meth public int getScrollOrientation()
meth public java.lang.String getDescription()
supr java.lang.Object
hfds model,value

CLSS public static org.netbeans.jemmy.operators.JSpinnerOperator$NumberSpinnerOperator
 outer org.netbeans.jemmy.operators.JSpinnerOperator
meth public javax.swing.SpinnerNumberModel getNumberModel()
meth public void scrollToValue(double)
meth public void scrollToValue(java.lang.Number)
supr org.netbeans.jemmy.operators.JSpinnerOperator

CLSS public abstract static org.netbeans.jemmy.operators.JSpinnerOperator$ObjectScrollAdjuster
 outer org.netbeans.jemmy.operators.JSpinnerOperator
cons public init(org.netbeans.jemmy.operators.JSpinnerOperator,int)
intf org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster
meth public abstract boolean equals(java.lang.Object)
meth public int getScrollDirection()
meth public int getScrollOrientation()
supr java.lang.Object
hfds direction,model

CLSS public static org.netbeans.jemmy.operators.JSpinnerOperator$SpinnerModelException
 outer org.netbeans.jemmy.operators.JSpinnerOperator
cons public init(java.lang.String,java.awt.Component)
supr org.netbeans.jemmy.JemmyException

CLSS public static org.netbeans.jemmy.operators.JSpinnerOperator$ToStringScrollAdjuster
 outer org.netbeans.jemmy.operators.JSpinnerOperator
cons public init(org.netbeans.jemmy.operators.JSpinnerOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.JSpinnerOperator,java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator,int)
meth public boolean equals(java.lang.Object)
meth public int getScrollOrientation()
meth public java.lang.String getDescription()
supr org.netbeans.jemmy.operators.JSpinnerOperator$ObjectScrollAdjuster
hfds comparator,pattern

CLSS public org.netbeans.jemmy.operators.JSplitPaneOperator
cons public init(javax.swing.JSplitPane)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static java.lang.String HORIZONTAL_ORIENTATION_DPROP_VALUE = "HORIZONTAL"
fld public final static java.lang.String IS_ONE_TOUCH_EXPANDABLE_DPROP = "One touch expandable"
fld public final static java.lang.String MAXIMUM_DPROP = "Maximum"
fld public final static java.lang.String MINIMUM_DPROP = "Minimum"
fld public final static java.lang.String ORIENTATION_DPROP = "Orientation"
fld public final static java.lang.String VALUE_DPROP = "Value"
fld public final static java.lang.String VERTICAL_ORIENTATION_DPROP_VALUE = "VERTICAL"
innr public static JSplitPaneFinder
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth public boolean isContinuousLayout()
meth public boolean isOneTouchExpandable()
meth public int getDividerLocation()
meth public int getDividerSize()
meth public int getLastDividerLocation()
meth public int getMaximumDividerLocation()
meth public int getMinimumDividerLocation()
meth public int getOrientation()
meth public java.awt.Component getBottomComponent()
meth public java.awt.Component getLeftComponent()
meth public java.awt.Component getRightComponent()
meth public java.awt.Component getTopComponent()
meth public java.util.Hashtable getDump()
meth public javax.swing.plaf.SplitPaneUI getUI()
meth public javax.swing.plaf.basic.BasicSplitPaneDivider findDivider()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public org.netbeans.jemmy.operators.ContainerOperator getDivider()
meth public static javax.swing.JSplitPane findJSplitPane(java.awt.Container)
meth public static javax.swing.JSplitPane findJSplitPane(java.awt.Container,int)
meth public static javax.swing.JSplitPane findJSplitPane(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JSplitPane findJSplitPane(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JSplitPane findJSplitPaneUnder(java.awt.Component)
meth public static javax.swing.JSplitPane findJSplitPaneUnder(java.awt.Component,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JSplitPane waitJSplitPane(java.awt.Container)
meth public static javax.swing.JSplitPane waitJSplitPane(java.awt.Container,int)
meth public static javax.swing.JSplitPane waitJSplitPane(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JSplitPane waitJSplitPane(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void copyEnvironment(org.netbeans.jemmy.operators.Operator)
meth public void expandLeft()
meth public void expandRight()
meth public void moveDivider(double)
meth public void moveDivider(int)
meth public void moveToMaximum()
meth public void moveToMinimum()
meth public void resetToPreferredSizes()
meth public void scrollTo(org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster)
meth public void setBottomComponent(java.awt.Component)
meth public void setContinuousLayout(boolean)
meth public void setDividerLocation(double)
meth public void setDividerLocation(int)
meth public void setDividerSize(int)
meth public void setLastDividerLocation(int)
meth public void setLeftComponent(java.awt.Component)
meth public void setOneTouchExpandable(boolean)
meth public void setOrientation(int)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setRightComponent(java.awt.Component)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
meth public void setTopComponent(java.awt.Component)
meth public void setUI(javax.swing.plaf.SplitPaneUI)
supr org.netbeans.jemmy.operators.JComponentOperator
hfds BETWEEN_CLICK_TIMEOUT,SCROLL_CLICK_TIMEOUT,WHOLE_SCROLL_TIMEOUT,divider,driver,output,timeouts
hcls ValueScrollAdjuster

CLSS public static org.netbeans.jemmy.operators.JSplitPaneOperator$JSplitPaneFinder
 outer org.netbeans.jemmy.operators.JSplitPaneOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.JTabbedPaneOperator
cons public init(javax.swing.JTabbedPane)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static java.lang.String PAGE_PREFIX_DPROP = "Page"
fld public final static java.lang.String SELECTED_PAGE_DPROP = "Selected"
innr public NoSuchPageException
innr public abstract interface static TabPageChooser
innr public static JTabbedPaneByItemFinder
innr public static JTabbedPaneFinder
intf org.netbeans.jemmy.Outputable
meth public boolean isEnabledAt(int)
meth public int findPage(java.lang.String)
meth public int findPage(java.lang.String,boolean,boolean)
meth public int findPage(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public int findPage(org.netbeans.jemmy.operators.JTabbedPaneOperator$TabPageChooser)
meth public int getSelectedIndex()
meth public int getTabCount()
meth public int getTabPlacement()
meth public int getTabRunCount()
meth public int indexOfComponent(java.awt.Component)
meth public int indexOfTab(java.lang.String)
meth public int indexOfTab(javax.swing.Icon)
meth public int waitPage(java.lang.String)
meth public int waitPage(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public int waitPage(org.netbeans.jemmy.operators.JTabbedPaneOperator$TabPageChooser)
meth public java.awt.Color getBackgroundAt(int)
meth public java.awt.Color getForegroundAt(int)
meth public java.awt.Component getComponentAt(int)
meth public java.awt.Component getSelectedComponent()
meth public java.awt.Component selectPage(int)
meth public java.awt.Component selectPage(java.lang.String)
meth public java.awt.Component selectPage(java.lang.String,boolean,boolean)
meth public java.awt.Component selectPage(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public java.awt.Component selectPage(org.netbeans.jemmy.operators.JTabbedPaneOperator$TabPageChooser)
meth public java.awt.Rectangle getBoundsAt(int)
meth public java.lang.String getTitleAt(int)
meth public java.util.Hashtable getDump()
meth public javax.swing.Icon getDisabledIconAt(int)
meth public javax.swing.Icon getIconAt(int)
meth public javax.swing.SingleSelectionModel getModel()
meth public javax.swing.plaf.TabbedPaneUI getUI()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public static javax.swing.JTabbedPane findJTabbedPane(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JTabbedPane findJTabbedPane(java.awt.Container,java.lang.String,boolean,boolean,int,int)
meth public static javax.swing.JTabbedPane findJTabbedPane(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JTabbedPane findJTabbedPane(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JTabbedPane findJTabbedPaneUnder(java.awt.Component)
meth public static javax.swing.JTabbedPane findJTabbedPaneUnder(java.awt.Component,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JTabbedPane waitJTabbedPane(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JTabbedPane waitJTabbedPane(java.awt.Container,java.lang.String,boolean,boolean,int,int)
meth public static javax.swing.JTabbedPane waitJTabbedPane(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JTabbedPane waitJTabbedPane(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void addTab(java.lang.String,java.awt.Component)
meth public void addTab(java.lang.String,javax.swing.Icon,java.awt.Component)
meth public void addTab(java.lang.String,javax.swing.Icon,java.awt.Component,java.lang.String)
meth public void copyEnvironment(org.netbeans.jemmy.operators.Operator)
meth public void insertTab(java.lang.String,javax.swing.Icon,java.awt.Component,java.lang.String,int)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void removeTabAt(int)
meth public void setBackgroundAt(int,java.awt.Color)
meth public void setComponentAt(int,java.awt.Component)
meth public void setDisabledIconAt(int,javax.swing.Icon)
meth public void setEnabledAt(int,boolean)
meth public void setForegroundAt(int,java.awt.Color)
meth public void setIconAt(int,javax.swing.Icon)
meth public void setModel(javax.swing.SingleSelectionModel)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setSelectedComponent(java.awt.Component)
meth public void setSelectedIndex(int)
meth public void setTabPlacement(int)
meth public void setTitleAt(int,java.lang.String)
meth public void setUI(javax.swing.plaf.TabbedPaneUI)
meth public void waitSelected(int)
meth public void waitSelected(java.lang.String)
supr org.netbeans.jemmy.operators.JComponentOperator
hfds driver,output
hcls BySubStringTabPageChooser

CLSS public static org.netbeans.jemmy.operators.JTabbedPaneOperator$JTabbedPaneByItemFinder
 outer org.netbeans.jemmy.operators.JTabbedPaneOperator
cons public init(java.lang.String,int)
cons public init(java.lang.String,int,org.netbeans.jemmy.operators.Operator$StringComparator)
intf org.netbeans.jemmy.ComponentChooser
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
supr java.lang.Object
hfds comparator,itemIndex,title

CLSS public static org.netbeans.jemmy.operators.JTabbedPaneOperator$JTabbedPaneFinder
 outer org.netbeans.jemmy.operators.JTabbedPaneOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.JTabbedPaneOperator$NoSuchPageException
 outer org.netbeans.jemmy.operators.JTabbedPaneOperator
cons public init(org.netbeans.jemmy.operators.JTabbedPaneOperator,java.lang.String)
supr org.netbeans.jemmy.JemmyInputException

CLSS public abstract interface static org.netbeans.jemmy.operators.JTabbedPaneOperator$TabPageChooser
 outer org.netbeans.jemmy.operators.JTabbedPaneOperator
meth public abstract boolean checkPage(org.netbeans.jemmy.operators.JTabbedPaneOperator,int)
meth public abstract java.lang.String getDescription()

CLSS public org.netbeans.jemmy.operators.JTableHeaderOperator
cons public init(javax.swing.table.JTableHeader)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
innr public static JTableHeaderFinder
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth public boolean getReorderingAllowed()
meth public boolean getResizingAllowed()
meth public boolean getUpdateTableInRealTime()
meth public int columnAtPoint(java.awt.Point)
meth public int getDraggedDistance()
meth public java.awt.Point getPointToClick(int)
meth public java.awt.Rectangle getHeaderRect(int)
meth public javax.swing.JTable getTable()
meth public javax.swing.plaf.TableHeaderUI getUI()
meth public javax.swing.table.TableCellRenderer getDefaultRenderer()
meth public javax.swing.table.TableColumn getDraggedColumn()
meth public javax.swing.table.TableColumn getResizingColumn()
meth public javax.swing.table.TableColumnModel getColumnModel()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public void columnAdded(javax.swing.event.TableColumnModelEvent)
meth public void columnMarginChanged(javax.swing.event.ChangeEvent)
meth public void columnMoved(javax.swing.event.TableColumnModelEvent)
meth public void columnRemoved(javax.swing.event.TableColumnModelEvent)
meth public void columnSelectionChanged(javax.swing.event.ListSelectionEvent)
meth public void copyEnvironment(org.netbeans.jemmy.operators.Operator)
meth public void moveColumn(int,int)
meth public void resizeAndRepaint()
meth public void selectColumn(int)
meth public void selectColumns(int[])
meth public void setColumnModel(javax.swing.table.TableColumnModel)
meth public void setDefaultRenderer(javax.swing.table.TableCellRenderer)
meth public void setDraggedColumn(javax.swing.table.TableColumn)
meth public void setDraggedDistance(int)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setReorderingAllowed(boolean)
meth public void setResizingAllowed(boolean)
meth public void setResizingColumn(javax.swing.table.TableColumn)
meth public void setTable(javax.swing.JTable)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
meth public void setUI(javax.swing.plaf.TableHeaderUI)
meth public void setUpdateTableInRealTime(boolean)
supr org.netbeans.jemmy.operators.JComponentOperator
hfds driver,output,timeouts

CLSS public static org.netbeans.jemmy.operators.JTableHeaderOperator$JTableHeaderFinder
 outer org.netbeans.jemmy.operators.JTableHeaderOperator
cons public init(org.netbeans.jemmy.ComponentChooser)
intf org.netbeans.jemmy.ComponentChooser
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
supr java.lang.Object
hfds subFinder

CLSS public org.netbeans.jemmy.operators.JTableOperator
cons public init(javax.swing.JTable)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int,int,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static java.lang.String CELL_PREFIX_DPROP = "Cell"
fld public final static java.lang.String COLUMN_COUNT_DPROP = "Column count"
fld public final static java.lang.String COLUMN_PREFIX_DPROP = "Column"
fld public final static java.lang.String ROW_COUNT_DPROP = "Row count"
fld public final static java.lang.String SELECTED_COLUMN_PREFIX_DPROP = "SelectedColumn"
fld public final static java.lang.String SELECTED_ROW_PREFIX_DPROP = "SelectedRow"
innr public abstract interface static TableCellChooser
innr public static JTableByCellFinder
innr public static JTableFinder
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth public boolean editCellAt(int,int)
meth public boolean editCellAt(int,int,java.util.EventObject)
meth public boolean getAutoCreateColumnsFromModel()
meth public boolean getCellSelectionEnabled()
meth public boolean getColumnSelectionAllowed()
meth public boolean getRowSelectionAllowed()
meth public boolean getScrollableTracksViewportHeight()
meth public boolean getScrollableTracksViewportWidth()
meth public boolean getShowHorizontalLines()
meth public boolean getShowVerticalLines()
meth public boolean isCellEditable(int,int)
meth public boolean isCellSelected(int,int)
meth public boolean isColumnSelected(int)
meth public boolean isEditing()
meth public boolean isRowSelected(int)
meth public int columnAtPoint(java.awt.Point)
meth public int convertColumnIndexToModel(int)
meth public int convertColumnIndexToView(int)
meth public int findCellColumn(java.lang.String)
meth public int findCellColumn(java.lang.String,boolean,boolean)
meth public int findCellColumn(java.lang.String,boolean,boolean,int)
meth public int findCellColumn(java.lang.String,int)
meth public int findCellColumn(java.lang.String,int,int)
meth public int findCellColumn(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public int findCellColumn(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator,int)
meth public int findCellColumn(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator,int,int)
meth public int findCellColumn(org.netbeans.jemmy.ComponentChooser)
meth public int findCellColumn(org.netbeans.jemmy.ComponentChooser,int)
meth public int findCellColumn(org.netbeans.jemmy.ComponentChooser,int,int)
meth public int findCellColumn(org.netbeans.jemmy.operators.JTableOperator$TableCellChooser)
meth public int findCellColumn(org.netbeans.jemmy.operators.JTableOperator$TableCellChooser,int)
meth public int findCellColumn(org.netbeans.jemmy.operators.JTableOperator$TableCellChooser,int,int)
meth public int findCellRow(java.lang.String)
meth public int findCellRow(java.lang.String,boolean,boolean)
meth public int findCellRow(java.lang.String,boolean,boolean,int)
meth public int findCellRow(java.lang.String,int)
meth public int findCellRow(java.lang.String,int,int)
meth public int findCellRow(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public int findCellRow(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator,int)
meth public int findCellRow(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator,int,int)
meth public int findCellRow(org.netbeans.jemmy.ComponentChooser)
meth public int findCellRow(org.netbeans.jemmy.ComponentChooser,int)
meth public int findCellRow(org.netbeans.jemmy.ComponentChooser,int,int)
meth public int findCellRow(org.netbeans.jemmy.operators.JTableOperator$TableCellChooser)
meth public int findCellRow(org.netbeans.jemmy.operators.JTableOperator$TableCellChooser,int)
meth public int findCellRow(org.netbeans.jemmy.operators.JTableOperator$TableCellChooser,int,int)
meth public int findColumn(java.lang.String)
meth public int findColumn(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public int getAutoResizeMode()
meth public int getColumnCount()
meth public int getEditingColumn()
meth public int getEditingRow()
meth public int getRowCount()
meth public int getRowHeight()
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
meth public java.awt.Component getRenderedComponent(int,int)
meth public java.awt.Component getRenderedComponent(int,int,boolean,boolean)
meth public java.awt.Component prepareEditor(javax.swing.table.TableCellEditor,int,int)
meth public java.awt.Component prepareRenderer(javax.swing.table.TableCellRenderer,int,int)
meth public java.awt.Component waitCellComponent(org.netbeans.jemmy.ComponentChooser,int,int)
meth public java.awt.Dimension getIntercellSpacing()
meth public java.awt.Dimension getPreferredScrollableViewportSize()
meth public java.awt.Point findCell(java.lang.String,int)
meth public java.awt.Point findCell(java.lang.String,int[],int[],int)
meth public java.awt.Point findCell(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator,int)
meth public java.awt.Point findCell(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator,int[],int[],int)
meth public java.awt.Point findCell(org.netbeans.jemmy.ComponentChooser)
meth public java.awt.Point findCell(org.netbeans.jemmy.ComponentChooser,int)
meth public java.awt.Point findCell(org.netbeans.jemmy.ComponentChooser,int[],int[],int)
meth public java.awt.Point findCell(org.netbeans.jemmy.operators.JTableOperator$TableCellChooser)
meth public java.awt.Point findCell(org.netbeans.jemmy.operators.JTableOperator$TableCellChooser,int)
meth public java.awt.Point findCell(org.netbeans.jemmy.operators.JTableOperator$TableCellChooser,int[],int[],int)
meth public java.awt.Point getPointToClick(int,int)
meth public java.awt.Rectangle getCellRect(int,int,boolean)
meth public java.lang.Class getColumnClass(int)
meth public java.lang.Object getValueAt(int,int)
meth public java.lang.String getColumnName(int)
meth public java.util.Hashtable getDump()
meth public javax.swing.JPopupMenu callPopupOnCell(int,int)
meth public javax.swing.ListSelectionModel getSelectionModel()
meth public javax.swing.plaf.TableUI getUI()
meth public javax.swing.table.JTableHeader getTableHeader()
meth public javax.swing.table.TableCellEditor getCellEditor()
meth public javax.swing.table.TableCellEditor getCellEditor(int,int)
meth public javax.swing.table.TableCellEditor getDefaultEditor(java.lang.Class)
meth public javax.swing.table.TableCellRenderer getCellRenderer(int,int)
meth public javax.swing.table.TableCellRenderer getDefaultRenderer(java.lang.Class)
meth public javax.swing.table.TableColumn getColumn(java.lang.Object)
meth public javax.swing.table.TableColumnModel getColumnModel()
meth public javax.swing.table.TableModel getModel()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public org.netbeans.jemmy.operators.JTableHeaderOperator getHeaderOperator()
meth public static javax.swing.JTable findJTable(java.awt.Container,java.lang.String,boolean,boolean,int,int)
meth public static javax.swing.JTable findJTable(java.awt.Container,java.lang.String,boolean,boolean,int,int,int)
meth public static javax.swing.JTable findJTable(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JTable findJTable(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JTable waitJTable(java.awt.Container,java.lang.String,boolean,boolean,int,int)
meth public static javax.swing.JTable waitJTable(java.awt.Container,java.lang.String,boolean,boolean,int,int,int)
meth public static javax.swing.JTable waitJTable(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JTable waitJTable(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void addColumn(javax.swing.table.TableColumn)
meth public void addColumnSelectionInterval(int,int)
meth public void addRowSelectionInterval(int,int)
meth public void changeCellObject(int,int,java.lang.Object)
meth public void changeCellText(int,int,java.lang.String)
meth public void clearSelection()
meth public void clickForEdit(int,int)
meth public void clickOnCell(int,int)
meth public void clickOnCell(int,int,int)
meth public void clickOnCell(int,int,int,int)
meth public void clickOnCell(int,int,int,int,int)
meth public void columnAdded(javax.swing.event.TableColumnModelEvent)
meth public void columnMarginChanged(javax.swing.event.ChangeEvent)
meth public void columnMoved(javax.swing.event.TableColumnModelEvent)
meth public void columnRemoved(javax.swing.event.TableColumnModelEvent)
meth public void columnSelectionChanged(javax.swing.event.ListSelectionEvent)
meth public void copyEnvironment(org.netbeans.jemmy.operators.Operator)
meth public void createDefaultColumnsFromModel()
meth public void editingCanceled(javax.swing.event.ChangeEvent)
meth public void editingStopped(javax.swing.event.ChangeEvent)
meth public void moveColumn(int,int)
meth public void removeColumn(javax.swing.table.TableColumn)
meth public void removeColumnSelectionInterval(int,int)
meth public void removeEditor()
meth public void removeRowSelectionInterval(int,int)
meth public void scrollToCell(int,int)
meth public void selectAll()
meth public void selectCell(int,int)
meth public void setAutoCreateColumnsFromModel(boolean)
meth public void setAutoResizeMode(int)
meth public void setCellEditor(javax.swing.table.TableCellEditor)
meth public void setCellSelectionEnabled(boolean)
meth public void setColumnModel(javax.swing.table.TableColumnModel)
meth public void setColumnSelectionAllowed(boolean)
meth public void setColumnSelectionInterval(int,int)
meth public void setDefaultEditor(java.lang.Class,javax.swing.table.TableCellEditor)
meth public void setDefaultRenderer(java.lang.Class,javax.swing.table.TableCellRenderer)
meth public void setEditingColumn(int)
meth public void setEditingRow(int)
meth public void setGridColor(java.awt.Color)
meth public void setIntercellSpacing(java.awt.Dimension)
meth public void setModel(javax.swing.table.TableModel)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setPreferredScrollableViewportSize(java.awt.Dimension)
meth public void setRowHeight(int)
meth public void setRowMargin(int)
meth public void setRowSelectionAllowed(boolean)
meth public void setRowSelectionInterval(int,int)
meth public void setSelectionBackground(java.awt.Color)
meth public void setSelectionForeground(java.awt.Color)
meth public void setSelectionMode(int)
meth public void setSelectionModel(javax.swing.ListSelectionModel)
meth public void setShowGrid(boolean)
meth public void setShowHorizontalLines(boolean)
meth public void setShowVerticalLines(boolean)
meth public void setTableHeader(javax.swing.table.JTableHeader)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
meth public void setUI(javax.swing.plaf.TableUI)
meth public void setValueAt(java.lang.Object,int,int)
meth public void tableChanged(javax.swing.event.TableModelEvent)
meth public void valueChanged(javax.swing.event.ListSelectionEvent)
meth public void waitCell(java.lang.String,int,int)
supr org.netbeans.jemmy.operators.JComponentOperator
hfds WAIT_EDITING_TIMEOUT,driver,output,timeouts
hcls ByRenderedComponentTableCellChooser,BySubStringTableCellChooser,CellComponentWaiter

CLSS public static org.netbeans.jemmy.operators.JTableOperator$JTableByCellFinder
 outer org.netbeans.jemmy.operators.JTableOperator
cons public init(java.lang.String,int,int)
cons public init(java.lang.String,int,int,org.netbeans.jemmy.operators.Operator$StringComparator)
intf org.netbeans.jemmy.ComponentChooser
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
supr java.lang.Object
hfds column,comparator,label,row

CLSS public static org.netbeans.jemmy.operators.JTableOperator$JTableFinder
 outer org.netbeans.jemmy.operators.JTableOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public abstract interface static org.netbeans.jemmy.operators.JTableOperator$TableCellChooser
 outer org.netbeans.jemmy.operators.JTableOperator
meth public abstract boolean checkCell(org.netbeans.jemmy.operators.JTableOperator,int,int)
meth public abstract java.lang.String getDescription()

CLSS public org.netbeans.jemmy.operators.JTextAreaOperator
cons public init(javax.swing.JTextArea)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static java.lang.String COLUMN_COUNT_DPROP = "Column count"
fld public final static java.lang.String ROW_COUNT_DPROP = "Row count"
innr public static JTextAreaFinder
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth public boolean getLineWrap()
meth public boolean getWrapStyleWord()
meth public int getColumns()
meth public int getLineCount()
meth public int getLineEndOffset(int)
meth public int getLineOfOffset(int)
meth public int getLineStartOffset(int)
meth public int getRows()
meth public int getTabSize()
meth public java.util.Hashtable getDump()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public static javax.swing.JTextArea findJTextArea(java.awt.Container,java.lang.String,boolean,boolean)
meth public static javax.swing.JTextArea findJTextArea(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JTextArea findJTextArea(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JTextArea findJTextArea(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JTextArea waitJTextArea(java.awt.Container,java.lang.String,boolean,boolean)
meth public static javax.swing.JTextArea waitJTextArea(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JTextArea waitJTextArea(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JTextArea waitJTextArea(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void append(java.lang.String)
meth public void changeCaretPosition(int,int)
meth public void changeCaretRow(int)
meth public void insert(java.lang.String,int)
meth public void replaceRange(java.lang.String,int,int)
meth public void selectLines(int,int)
meth public void selectText(int,int,int,int)
meth public void setColumns(int)
meth public void setLineWrap(boolean)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setRows(int)
meth public void setTabSize(int)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
meth public void setWrapStyleWord(boolean)
meth public void typeText(java.lang.String,int,int)
meth public void usePageNavigationKeys(boolean)
supr org.netbeans.jemmy.operators.JTextComponentOperator
hfds output,timeouts

CLSS public static org.netbeans.jemmy.operators.JTextAreaOperator$JTextAreaFinder
 outer org.netbeans.jemmy.operators.JTextAreaOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.JTextComponentOperator
cons public init(javax.swing.text.JTextComponent)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld protected int modifiersPressed
fld public final static java.lang.String IS_EDITABLE_DPROP = "Editable"
fld public final static java.lang.String SELECTED_TEXT_DPROP = "Selected text"
fld public final static java.lang.String TEXT_DPROP = "Text"
innr public NoSuchTextException
innr public abstract interface static TextChooser
innr public static JTextComponentByTextFinder
innr public static JTextComponentFinder
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth public boolean getScrollableTracksViewportHeight()
meth public boolean getScrollableTracksViewportWidth()
meth public boolean isEditable()
meth public char getFocusAccelerator()
meth public int getCaretPosition()
meth public int getPositionByText(java.lang.String)
meth public int getPositionByText(java.lang.String,int)
meth public int getPositionByText(java.lang.String,org.netbeans.jemmy.operators.JTextComponentOperator$TextChooser)
meth public int getPositionByText(java.lang.String,org.netbeans.jemmy.operators.JTextComponentOperator$TextChooser,int)
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
meth public java.awt.Rectangle modelToView(int)
meth public java.lang.String getDisplayedText()
meth public java.lang.String getSelectedText()
meth public java.lang.String getText()
meth public java.lang.String getText(int,int)
meth public java.util.Hashtable getDump()
meth public javax.swing.Action[] getActions()
meth public javax.swing.plaf.TextUI getUI()
meth public javax.swing.text.Caret getCaret()
meth public javax.swing.text.Document getDocument()
meth public javax.swing.text.Highlighter getHighlighter()
meth public javax.swing.text.Keymap getKeymap()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public static javax.swing.text.JTextComponent findJTextComponent(java.awt.Container,java.lang.String,boolean,boolean)
meth public static javax.swing.text.JTextComponent findJTextComponent(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.text.JTextComponent findJTextComponent(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.text.JTextComponent findJTextComponent(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.text.JTextComponent waitJTextComponent(java.awt.Container,java.lang.String,boolean,boolean)
meth public static javax.swing.text.JTextComponent waitJTextComponent(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.text.JTextComponent waitJTextComponent(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.text.JTextComponent waitJTextComponent(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void addCaretListener(javax.swing.event.CaretListener)
meth public void changeCaretPosition(int)
meth public void changeCaretPosition(java.lang.String,boolean)
meth public void changeCaretPosition(java.lang.String,int,boolean)
meth public void clearText()
meth public void copy()
meth public void copyEnvironment(org.netbeans.jemmy.operators.Operator)
meth public void cut()
meth public void enterText(java.lang.String)
meth public void moveCaretPosition(int)
meth public void paste()
meth public void read(java.io.Reader,java.lang.Object)
meth public void removeCaretListener(javax.swing.event.CaretListener)
meth public void replaceSelection(java.lang.String)
meth public void scrollToPosition(int)
meth public void select(int,int)
meth public void selectAll()
meth public void selectText(int,int)
meth public void selectText(java.lang.String)
meth public void selectText(java.lang.String,int)
meth public void setCaret(javax.swing.text.Caret)
meth public void setCaretColor(java.awt.Color)
meth public void setCaretPosition(int)
meth public void setDisabledTextColor(java.awt.Color)
meth public void setDocument(javax.swing.text.Document)
meth public void setEditable(boolean)
meth public void setFocusAccelerator(char)
meth public void setHighlighter(javax.swing.text.Highlighter)
meth public void setKeymap(javax.swing.text.Keymap)
meth public void setMargin(java.awt.Insets)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setSelectedTextColor(java.awt.Color)
meth public void setSelectionColor(java.awt.Color)
meth public void setSelectionEnd(int)
meth public void setSelectionStart(int)
meth public void setText(java.lang.String)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
meth public void setUI(javax.swing.plaf.TextUI)
meth public void typeText(java.lang.String)
meth public void typeText(java.lang.String,int)
meth public void waitCaretPosition(int)
meth public void waitText(java.lang.String)
meth public void waitText(java.lang.String,int)
meth public void write(java.io.Writer)
supr org.netbeans.jemmy.operators.JComponentOperator
hfds BETWEEN_KEYS_TIMEOUT,CHANGE_CARET_POSITION_TIMEOUT,PUSH_KEY_TIMEOUT,TYPE_TEXT_TIMEOUT,driver,output,timeouts

CLSS public static org.netbeans.jemmy.operators.JTextComponentOperator$JTextComponentByTextFinder
 outer org.netbeans.jemmy.operators.JTextComponentOperator
cons public init(java.lang.String)
cons public init(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
intf org.netbeans.jemmy.ComponentChooser
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
supr java.lang.Object
hfds comparator,label

CLSS public static org.netbeans.jemmy.operators.JTextComponentOperator$JTextComponentFinder
 outer org.netbeans.jemmy.operators.JTextComponentOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.JTextComponentOperator$NoSuchTextException
 outer org.netbeans.jemmy.operators.JTextComponentOperator
cons public init(org.netbeans.jemmy.operators.JTextComponentOperator,java.lang.String)
supr org.netbeans.jemmy.JemmyInputException

CLSS public abstract interface static org.netbeans.jemmy.operators.JTextComponentOperator$TextChooser
 outer org.netbeans.jemmy.operators.JTextComponentOperator
meth public abstract boolean checkPosition(javax.swing.text.Document,int)
meth public abstract java.lang.String getDescription()

CLSS public org.netbeans.jemmy.operators.JTextFieldOperator
cons public init(javax.swing.JTextField)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
innr public static JTextFieldFinder
meth public int getColumns()
meth public int getHorizontalAlignment()
meth public int getScrollOffset()
meth public javax.swing.BoundedRangeModel getHorizontalVisibility()
meth public static javax.swing.JTextField findJTextField(java.awt.Container,java.lang.String,boolean,boolean)
meth public static javax.swing.JTextField findJTextField(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JTextField findJTextField(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JTextField findJTextField(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JTextField waitJTextField(java.awt.Container,java.lang.String,boolean,boolean)
meth public static javax.swing.JTextField waitJTextField(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JTextField waitJTextField(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JTextField waitJTextField(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void addActionListener(java.awt.event.ActionListener)
meth public void postActionEvent()
meth public void removeActionListener(java.awt.event.ActionListener)
meth public void setActionCommand(java.lang.String)
meth public void setColumns(int)
meth public void setHorizontalAlignment(int)
meth public void setScrollOffset(int)
meth public void waitText(java.lang.String)
meth public void waitText(java.lang.String,int)
supr org.netbeans.jemmy.operators.JTextComponentOperator

CLSS public static org.netbeans.jemmy.operators.JTextFieldOperator$JTextFieldFinder
 outer org.netbeans.jemmy.operators.JTextFieldOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.JTextPaneOperator
cons public init(javax.swing.JTextPane)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
innr public static JTextPaneFinder
meth public javax.swing.text.AttributeSet getCharacterAttributes()
meth public javax.swing.text.AttributeSet getParagraphAttributes()
meth public javax.swing.text.MutableAttributeSet getInputAttributes()
meth public javax.swing.text.Style addStyle(java.lang.String,javax.swing.text.Style)
meth public javax.swing.text.Style getLogicalStyle()
meth public javax.swing.text.Style getStyle(java.lang.String)
meth public javax.swing.text.StyledDocument getStyledDocument()
meth public static javax.swing.JTextPane findJTextPane(java.awt.Container,java.lang.String,boolean,boolean)
meth public static javax.swing.JTextPane findJTextPane(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JTextPane findJTextPane(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JTextPane findJTextPane(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JTextPane waitJTextPane(java.awt.Container,java.lang.String,boolean,boolean)
meth public static javax.swing.JTextPane waitJTextPane(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JTextPane waitJTextPane(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JTextPane waitJTextPane(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void insertComponent(java.awt.Component)
meth public void insertIcon(javax.swing.Icon)
meth public void removeStyle(java.lang.String)
meth public void setCharacterAttributes(javax.swing.text.AttributeSet,boolean)
meth public void setLogicalStyle(javax.swing.text.Style)
meth public void setParagraphAttributes(javax.swing.text.AttributeSet,boolean)
meth public void setStyledDocument(javax.swing.text.StyledDocument)
supr org.netbeans.jemmy.operators.JEditorPaneOperator

CLSS public static org.netbeans.jemmy.operators.JTextPaneOperator$JTextPaneFinder
 outer org.netbeans.jemmy.operators.JTextPaneOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.JToggleButtonOperator
cons public init(javax.swing.JToggleButton)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
innr public static JToggleButtonFinder
meth protected void prepareToClick()
meth public static javax.swing.JToggleButton findJToggleButton(java.awt.Container,java.lang.String,boolean,boolean)
meth public static javax.swing.JToggleButton findJToggleButton(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JToggleButton findJToggleButton(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JToggleButton findJToggleButton(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JToggleButton waitJToggleButton(java.awt.Container,java.lang.String,boolean,boolean)
meth public static javax.swing.JToggleButton waitJToggleButton(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JToggleButton waitJToggleButton(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JToggleButton waitJToggleButton(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
supr org.netbeans.jemmy.operators.AbstractButtonOperator

CLSS public static org.netbeans.jemmy.operators.JToggleButtonOperator$JToggleButtonFinder
 outer org.netbeans.jemmy.operators.JToggleButtonOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.JTreeOperator
cons public init(javax.swing.JTree)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static java.lang.String NODE_PREFIX_DPROP = "Node"
fld public final static java.lang.String ROOT_DPROP = "Root"
fld public final static java.lang.String SELECTION_FIRST_DPROP = "First selected"
fld public final static java.lang.String SELECTION_LAST_DPROP = "Last selected"
innr public NoSuchPathException
innr public abstract interface static TreePathChooser
innr public abstract interface static TreeRowChooser
innr public static JTreeByItemFinder
innr public static JTreeFinder
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth public boolean getInvokesStopCellEditing()
meth public boolean getScrollableTracksViewportHeight()
meth public boolean getScrollableTracksViewportWidth()
meth public boolean getScrollsOnExpand()
meth public boolean getShowsRootHandles()
meth public boolean hasBeenExpanded(javax.swing.tree.TreePath)
meth public boolean isCollapsed(int)
meth public boolean isCollapsed(javax.swing.tree.TreePath)
meth public boolean isEditable()
meth public boolean isEditing()
meth public boolean isExpanded(int)
meth public boolean isExpanded(javax.swing.tree.TreePath)
meth public boolean isFixedRowHeight()
meth public boolean isLargeModel()
meth public boolean isPathEditable(javax.swing.tree.TreePath)
meth public boolean isPathSelected(javax.swing.tree.TreePath)
meth public boolean isRootVisible()
meth public boolean isRowSelected(int)
meth public boolean isSelectionEmpty()
meth public boolean isVisible(javax.swing.tree.TreePath)
meth public boolean stopEditing()
meth public int findRow(java.lang.String)
meth public int findRow(java.lang.String,boolean,boolean)
meth public int findRow(java.lang.String,boolean,boolean,int)
meth public int findRow(java.lang.String,int)
meth public int findRow(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public int findRow(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator,int)
meth public int findRow(org.netbeans.jemmy.ComponentChooser)
meth public int findRow(org.netbeans.jemmy.ComponentChooser,int)
meth public int findRow(org.netbeans.jemmy.operators.JTreeOperator$TreeRowChooser)
meth public int findRow(org.netbeans.jemmy.operators.JTreeOperator$TreeRowChooser,int)
meth public int getChildCount(java.lang.Object)
meth public int getChildCount(javax.swing.tree.TreePath)
meth public int getClosestRowForLocation(int,int)
meth public int getLeadSelectionRow()
meth public int getMaxSelectionRow()
meth public int getMinSelectionRow()
meth public int getRowCount()
meth public int getRowForLocation(int,int)
meth public int getRowForPath(javax.swing.tree.TreePath)
meth public int getRowHeight()
meth public int getScrollableBlockIncrement(java.awt.Rectangle,int,int)
meth public int getScrollableUnitIncrement(java.awt.Rectangle,int,int)
meth public int getSelectionCount()
meth public int getVisibleRowCount()
meth public int[] getSelectionRows()
meth public java.awt.Component getRenderedComponent(javax.swing.tree.TreePath)
meth public java.awt.Component getRenderedComponent(javax.swing.tree.TreePath,boolean,boolean,boolean)
meth public java.awt.Dimension getPreferredScrollableViewportSize()
meth public java.awt.Point getPointToClick(int)
meth public java.awt.Point getPointToClick(javax.swing.tree.TreePath)
meth public java.awt.Rectangle getPathBounds(javax.swing.tree.TreePath)
meth public java.awt.Rectangle getRowBounds(int)
meth public java.lang.Object chooseSubnode(java.lang.Object,java.lang.String)
meth public java.lang.Object chooseSubnode(java.lang.Object,java.lang.String,int)
meth public java.lang.Object chooseSubnode(java.lang.Object,java.lang.String,int,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public java.lang.Object chooseSubnode(java.lang.Object,java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public java.lang.Object getChild(java.lang.Object,int)
meth public java.lang.Object getLastSelectedPathComponent()
meth public java.lang.Object getRoot()
meth public java.lang.Object[] getChildren(java.lang.Object)
meth public java.lang.String convertValueToText(java.lang.Object,boolean,boolean,boolean,int,boolean)
meth public java.util.Enumeration getExpandedDescendants(javax.swing.tree.TreePath)
meth public java.util.Hashtable getDump()
meth public javax.swing.JPopupMenu callPopupOnPath(javax.swing.tree.TreePath)
meth public javax.swing.JPopupMenu callPopupOnPath(javax.swing.tree.TreePath,int)
meth public javax.swing.JPopupMenu callPopupOnPaths(javax.swing.tree.TreePath[])
meth public javax.swing.JPopupMenu callPopupOnPaths(javax.swing.tree.TreePath[],int)
meth public javax.swing.plaf.TreeUI getUI()
meth public javax.swing.tree.TreeCellEditor getCellEditor()
meth public javax.swing.tree.TreeCellRenderer getCellRenderer()
meth public javax.swing.tree.TreeModel getModel()
meth public javax.swing.tree.TreePath findPath(java.lang.String)
meth public javax.swing.tree.TreePath findPath(java.lang.String,java.lang.String)
meth public javax.swing.tree.TreePath findPath(java.lang.String,java.lang.String,boolean,boolean)
meth public javax.swing.tree.TreePath findPath(java.lang.String,java.lang.String,java.lang.String)
meth public javax.swing.tree.TreePath findPath(java.lang.String,java.lang.String,java.lang.String,boolean,boolean)
meth public javax.swing.tree.TreePath findPath(java.lang.String,java.lang.String,java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public javax.swing.tree.TreePath findPath(java.lang.String,java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public javax.swing.tree.TreePath findPath(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public javax.swing.tree.TreePath findPath(java.lang.String[])
meth public javax.swing.tree.TreePath findPath(java.lang.String[],boolean,boolean)
meth public javax.swing.tree.TreePath findPath(java.lang.String[],int[])
meth public javax.swing.tree.TreePath findPath(java.lang.String[],int[],boolean,boolean)
meth public javax.swing.tree.TreePath findPath(java.lang.String[],int[],org.netbeans.jemmy.operators.Operator$StringComparator)
meth public javax.swing.tree.TreePath findPath(java.lang.String[],org.netbeans.jemmy.operators.Operator$StringComparator)
meth public javax.swing.tree.TreePath findPath(org.netbeans.jemmy.operators.JTreeOperator$TreePathChooser)
meth public javax.swing.tree.TreePath getChildPath(javax.swing.tree.TreePath,int)
meth public javax.swing.tree.TreePath getClosestPathForLocation(int,int)
meth public javax.swing.tree.TreePath getEditingPath()
meth public javax.swing.tree.TreePath getLeadSelectionPath()
meth public javax.swing.tree.TreePath getPathForLocation(int,int)
meth public javax.swing.tree.TreePath getPathForRow(int)
meth public javax.swing.tree.TreePath getSelectionPath()
meth public javax.swing.tree.TreePath[] getChildPaths(javax.swing.tree.TreePath)
meth public javax.swing.tree.TreePath[] getSelectionPaths()
meth public javax.swing.tree.TreeSelectionModel getSelectionModel()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public static javax.swing.JTree findJTree(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JTree findJTree(java.awt.Container,java.lang.String,boolean,boolean,int,int)
meth public static javax.swing.JTree findJTree(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JTree findJTree(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JTree waitJTree(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JTree waitJTree(java.awt.Container,java.lang.String,boolean,boolean,int,int)
meth public static javax.swing.JTree waitJTree(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JTree waitJTree(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void addSelectionInterval(int,int)
meth public void addSelectionPath(javax.swing.tree.TreePath)
meth public void addSelectionPaths(javax.swing.tree.TreePath[])
meth public void addSelectionRow(int)
meth public void addSelectionRows(int[])
meth public void addTreeExpansionListener(javax.swing.event.TreeExpansionListener)
meth public void addTreeSelectionListener(javax.swing.event.TreeSelectionListener)
meth public void addTreeWillExpandListener(javax.swing.event.TreeWillExpandListener)
meth public void cancelEditing()
meth public void changePathObject(javax.swing.tree.TreePath,java.lang.Object)
meth public void changePathText(javax.swing.tree.TreePath,java.lang.String)
meth public void clearSelection()
meth public void clickForEdit(javax.swing.tree.TreePath)
meth public void clickOnPath(javax.swing.tree.TreePath)
meth public void clickOnPath(javax.swing.tree.TreePath,int)
meth public void clickOnPath(javax.swing.tree.TreePath,int,int)
meth public void clickOnPath(javax.swing.tree.TreePath,int,int,int)
meth public void collapsePath(javax.swing.tree.TreePath)
meth public void collapseRow(int)
meth public void copyEnvironment(org.netbeans.jemmy.operators.Operator)
meth public void doCollapsePath(javax.swing.tree.TreePath)
meth public void doCollapseRow(int)
meth public void doExpandPath(javax.swing.tree.TreePath)
meth public void doExpandRow(int)
meth public void doMakeVisible(javax.swing.tree.TreePath)
meth public void expandPath(javax.swing.tree.TreePath)
meth public void expandRow(int)
meth public void fireTreeCollapsed(javax.swing.tree.TreePath)
meth public void fireTreeExpanded(javax.swing.tree.TreePath)
meth public void fireTreeWillCollapse(javax.swing.tree.TreePath)
meth public void fireTreeWillExpand(javax.swing.tree.TreePath)
meth public void makeVisible(javax.swing.tree.TreePath)
meth public void removeSelectionInterval(int,int)
meth public void removeSelectionPath(javax.swing.tree.TreePath)
meth public void removeSelectionPaths(javax.swing.tree.TreePath[])
meth public void removeSelectionRow(int)
meth public void removeSelectionRows(int[])
meth public void removeTreeExpansionListener(javax.swing.event.TreeExpansionListener)
meth public void removeTreeSelectionListener(javax.swing.event.TreeSelectionListener)
meth public void removeTreeWillExpandListener(javax.swing.event.TreeWillExpandListener)
meth public void scrollPathToVisible(javax.swing.tree.TreePath)
meth public void scrollRowToVisible(int)
meth public void scrollToPath(javax.swing.tree.TreePath)
meth public void scrollToRow(int)
meth public void selectPath(javax.swing.tree.TreePath)
meth public void selectPaths(javax.swing.tree.TreePath[])
meth public void selectRow(int)
meth public void setCellEditor(javax.swing.tree.TreeCellEditor)
meth public void setCellRenderer(javax.swing.tree.TreeCellRenderer)
meth public void setEditable(boolean)
meth public void setInvokesStopCellEditing(boolean)
meth public void setLargeModel(boolean)
meth public void setModel(javax.swing.tree.TreeModel)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setRootVisible(boolean)
meth public void setRowHeight(int)
meth public void setScrollsOnExpand(boolean)
meth public void setSelectionInterval(int,int)
meth public void setSelectionModel(javax.swing.tree.TreeSelectionModel)
meth public void setSelectionPath(javax.swing.tree.TreePath)
meth public void setSelectionPaths(javax.swing.tree.TreePath[])
meth public void setSelectionRow(int)
meth public void setSelectionRows(int[])
meth public void setShowsRootHandles(boolean)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
meth public void setUI(javax.swing.plaf.TreeUI)
meth public void setVisibleRowCount(int)
meth public void startEditingAtPath(javax.swing.tree.TreePath)
meth public void treeDidChange()
meth public void waitCollapsed(int)
meth public void waitCollapsed(javax.swing.tree.TreePath)
meth public void waitExpanded(int)
meth public void waitExpanded(javax.swing.tree.TreePath)
meth public void waitRow(java.lang.String,int)
meth public void waitSelected(int)
meth public void waitSelected(int[])
meth public void waitSelected(javax.swing.tree.TreePath)
meth public void waitSelected(javax.swing.tree.TreePath[])
meth public void waitVisible(javax.swing.tree.TreePath)
supr org.netbeans.jemmy.operators.JComponentOperator
hfds BEFORE_EDIT_TIMEOUT,WAIT_AFTER_NODE_EXPANDED_TIMEOUT,WAIT_EDITING_TIMEOUT,WAIT_NEXT_NODE_TIMEOUT,WAIT_NODE_COLLAPSED_TIMEOUT,WAIT_NODE_EXPANDED_TIMEOUT,WAIT_NODE_VISIBLE_TIMEOUT,driver,output,timeouts
hcls ByRenderedComponentTreeRowChooser,BySubStringTreeRowChooser,StringArrayPathChooser

CLSS public static org.netbeans.jemmy.operators.JTreeOperator$JTreeByItemFinder
 outer org.netbeans.jemmy.operators.JTreeOperator
cons public init(java.lang.String,int)
cons public init(java.lang.String,int,org.netbeans.jemmy.operators.Operator$StringComparator)
intf org.netbeans.jemmy.ComponentChooser
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
supr java.lang.Object
hfds comparator,label,rowIndex

CLSS public static org.netbeans.jemmy.operators.JTreeOperator$JTreeFinder
 outer org.netbeans.jemmy.operators.JTreeOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.JTreeOperator$NoSuchPathException
 outer org.netbeans.jemmy.operators.JTreeOperator
cons public init(org.netbeans.jemmy.operators.JTreeOperator)
cons public init(org.netbeans.jemmy.operators.JTreeOperator,int)
cons public init(org.netbeans.jemmy.operators.JTreeOperator,java.lang.String[])
cons public init(org.netbeans.jemmy.operators.JTreeOperator,javax.swing.tree.TreePath)
supr org.netbeans.jemmy.JemmyInputException

CLSS public abstract interface static org.netbeans.jemmy.operators.JTreeOperator$TreePathChooser
 outer org.netbeans.jemmy.operators.JTreeOperator
meth public abstract boolean checkPath(javax.swing.tree.TreePath,int)
meth public abstract boolean hasAsParent(javax.swing.tree.TreePath,int)
meth public abstract java.lang.String getDescription()

CLSS public abstract interface static org.netbeans.jemmy.operators.JTreeOperator$TreeRowChooser
 outer org.netbeans.jemmy.operators.JTreeOperator
meth public abstract boolean checkRow(org.netbeans.jemmy.operators.JTreeOperator,int)
meth public abstract java.lang.String getDescription()

CLSS public org.netbeans.jemmy.operators.LabelOperator
cons public init(java.awt.Label)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static java.lang.String TEXT_DPROP = "Text"
innr public static LabelByLabelFinder
innr public static LabelFinder
meth public int getAlignment()
meth public java.lang.String getText()
meth public java.util.Hashtable getDump()
meth public static java.awt.Label findLabel(java.awt.Container,java.lang.String,boolean,boolean)
meth public static java.awt.Label findLabel(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static java.awt.Label findLabel(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.Label findLabel(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static java.awt.Label waitLabel(java.awt.Container,java.lang.String,boolean,boolean)
meth public static java.awt.Label waitLabel(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static java.awt.Label waitLabel(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.Label waitLabel(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void setAlignment(int)
meth public void setText(java.lang.String)
supr org.netbeans.jemmy.operators.ComponentOperator

CLSS public static org.netbeans.jemmy.operators.LabelOperator$LabelByLabelFinder
 outer org.netbeans.jemmy.operators.LabelOperator
cons public init(java.lang.String)
cons public init(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
intf org.netbeans.jemmy.ComponentChooser
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
supr java.lang.Object
hfds comparator,label

CLSS public static org.netbeans.jemmy.operators.LabelOperator$LabelFinder
 outer org.netbeans.jemmy.operators.LabelOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.ListOperator
cons public init(java.awt.List)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static java.lang.String ITEM_PREFIX_DPROP = "Item"
fld public final static java.lang.String SELECTED_ITEM_PREFIX_DPROP = "SelectedItem"
innr public static ListByItemFinder
innr public static ListFinder
intf org.netbeans.jemmy.Outputable
meth public boolean isIndexSelected(int)
meth public boolean isMultipleMode()
meth public int findItemIndex(java.lang.String)
meth public int findItemIndex(java.lang.String,int)
meth public int getItemCount()
meth public int getRows()
meth public int getSelectedIndex()
meth public int getVisibleIndex()
meth public int[] getSelectedIndexes()
meth public java.awt.Dimension getMinimumSize(int)
meth public java.awt.Dimension getPreferredSize(int)
meth public java.lang.Object[] getSelectedObjects()
meth public java.lang.String getItem(int)
meth public java.lang.String getSelectedItem()
meth public java.lang.String[] getItems()
meth public java.lang.String[] getSelectedItems()
meth public java.util.Hashtable getDump()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public static java.awt.List findList(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.List findList(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void addActionListener(java.awt.event.ActionListener)
meth public void addItemListener(java.awt.event.ItemListener)
meth public void copyEnvironment(org.netbeans.jemmy.operators.Operator)
meth public void deselect(int)
meth public void makeVisible(int)
meth public void remove(int)
meth public void remove(java.lang.String)
meth public void removeActionListener(java.awt.event.ActionListener)
meth public void removeAll()
meth public void removeItemListener(java.awt.event.ItemListener)
meth public void replaceItem(java.lang.String,int)
meth public void select(int)
meth public void selectItem(int)
meth public void selectItem(java.lang.String)
meth public void selectItem(java.lang.String,int)
meth public void selectItems(int,int)
meth public void setMultipleMode(boolean)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void waitItemSelection(int,boolean)
meth public void waitItemsSelection(int,int,boolean)
supr org.netbeans.jemmy.operators.ComponentOperator
hfds driver,output

CLSS public static org.netbeans.jemmy.operators.ListOperator$ListByItemFinder
 outer org.netbeans.jemmy.operators.ListOperator
cons public init(java.lang.String,int)
cons public init(java.lang.String,int,org.netbeans.jemmy.operators.Operator$StringComparator)
intf org.netbeans.jemmy.ComponentChooser
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
supr java.lang.Object
hfds comparator,itemIndex,label

CLSS public static org.netbeans.jemmy.operators.ListOperator$ListFinder
 outer org.netbeans.jemmy.operators.ListOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public abstract org.netbeans.jemmy.operators.Operator
cons public init()
fld public final static java.lang.String CLASS_DPROP = "Class"
fld public final static java.lang.String TO_STRING_DPROP = "toString"
innr protected abstract MapAction
innr protected abstract MapBooleanAction
innr protected abstract MapByteAction
innr protected abstract MapCharacterAction
innr protected abstract MapDoubleAction
innr protected abstract MapFloatAction
innr protected abstract MapIntegerAction
innr protected abstract MapLongAction
innr protected abstract MapVoidAction
innr protected abstract NoBlockingAction
innr public abstract interface static ComponentVisualizer
innr public abstract interface static PathParser
innr public abstract interface static StringComparator
innr public static DefaultPathParser
innr public static DefaultStringComparator
innr public static Finder
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth protected boolean runMapping(org.netbeans.jemmy.operators.Operator$MapBooleanAction)
meth protected byte runMapping(org.netbeans.jemmy.operators.Operator$MapByteAction)
meth protected char runMapping(org.netbeans.jemmy.operators.Operator$MapCharacterAction)
meth protected double runMapping(org.netbeans.jemmy.operators.Operator$MapDoubleAction)
meth protected float runMapping(org.netbeans.jemmy.operators.Operator$MapFloatAction)
meth protected int runMapping(org.netbeans.jemmy.operators.Operator$MapIntegerAction)
meth protected java.lang.Object produceTimeRestricted(org.netbeans.jemmy.Action,java.lang.Object,java.lang.String)
meth protected java.lang.Object produceTimeRestricted(org.netbeans.jemmy.Action,java.lang.String)
meth protected java.lang.Object runMapping(org.netbeans.jemmy.operators.Operator$MapAction)
meth protected java.lang.String[] addToDump(java.util.Hashtable,java.lang.String,java.lang.Object[])
meth protected java.lang.String[] addToDump(java.util.Hashtable,java.lang.String,java.lang.Object[][])
meth protected long runMapping(org.netbeans.jemmy.operators.Operator$MapLongAction)
meth protected void lockQueue()
meth protected void produceNoBlocking(org.netbeans.jemmy.operators.Operator$NoBlockingAction)
meth protected void produceNoBlocking(org.netbeans.jemmy.operators.Operator$NoBlockingAction,java.lang.Object)
meth protected void runMapping(org.netbeans.jemmy.operators.Operator$MapVoidAction)
meth protected void unlockAndThrow(java.lang.Exception)
meth protected void unlockQueue()
meth public abstract java.awt.Component getSource()
meth public boolean getVerification()
meth public boolean isCaptionEqual(java.lang.String,java.lang.String)
meth public boolean setVerification(boolean)
meth public int getCharKey(char)
meth public int getCharModifiers(char)
meth public int[] getCharsKeys(char[])
meth public int[] getCharsKeys(java.lang.String)
meth public int[] getCharsModifiers(char[])
meth public int[] getCharsModifiers(java.lang.String)
meth public java.lang.String toStringSource()
meth public java.lang.String[] getParentPath(java.lang.String[])
meth public java.lang.String[] parseString(java.lang.String)
meth public java.lang.String[] parseString(java.lang.String,java.lang.String)
meth public java.util.Hashtable getDump()
meth public org.netbeans.jemmy.CharBindingMap getCharBindingMap()
meth public org.netbeans.jemmy.ComponentChooser[] getParentPath(org.netbeans.jemmy.ComponentChooser[])
meth public org.netbeans.jemmy.JemmyProperties getProperties()
meth public org.netbeans.jemmy.JemmyProperties setProperties(org.netbeans.jemmy.JemmyProperties)
meth public org.netbeans.jemmy.QueueTool getQueueTool()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public org.netbeans.jemmy.operators.Operator$ComponentVisualizer getVisualizer()
meth public org.netbeans.jemmy.operators.Operator$PathParser getPathParser()
meth public org.netbeans.jemmy.operators.Operator$StringComparator getComparator()
meth public static boolean getDefaultVerification()
meth public static boolean isCaptionEqual(java.lang.String,java.lang.String,boolean,boolean)
meth public static boolean isCaptionEqual(java.lang.String,java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public static boolean setDefaultVerification(boolean)
meth public static int getDefaultMouseButton()
meth public static int getPopupMouseButton()
meth public static org.netbeans.jemmy.operators.ComponentOperator createOperator(java.awt.Component)
meth public static org.netbeans.jemmy.operators.Operator getEnvironmentOperator()
meth public static org.netbeans.jemmy.operators.Operator$ComponentVisualizer getDefaultComponentVisualizer()
meth public static org.netbeans.jemmy.operators.Operator$ComponentVisualizer setDefaultComponentVisualizer(org.netbeans.jemmy.operators.Operator$ComponentVisualizer)
meth public static org.netbeans.jemmy.operators.Operator$PathParser getDefaultPathParser()
meth public static org.netbeans.jemmy.operators.Operator$PathParser setDefaultPathParser(org.netbeans.jemmy.operators.Operator$PathParser)
meth public static org.netbeans.jemmy.operators.Operator$StringComparator getDefaultStringComparator()
meth public static org.netbeans.jemmy.operators.Operator$StringComparator setDefaultStringComparator(org.netbeans.jemmy.operators.Operator$StringComparator)
meth public static void addOperatorPackage(java.lang.String)
meth public void copyEnvironment(org.netbeans.jemmy.operators.Operator)
meth public void printDump()
meth public void setCharBindingMap(org.netbeans.jemmy.CharBindingMap)
meth public void setComparator(org.netbeans.jemmy.operators.Operator$StringComparator)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setPathParser(org.netbeans.jemmy.operators.Operator$PathParser)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
meth public void setVisualizer(org.netbeans.jemmy.operators.Operator$ComponentVisualizer)
meth public void waitState(org.netbeans.jemmy.ComponentChooser)
supr java.lang.Object
hfds comparator,map,operatorPkgs,output,parser,properties,queueTool,timeouts,verification,visualizer
hcls NullOperator

CLSS public abstract interface static org.netbeans.jemmy.operators.Operator$ComponentVisualizer
 outer org.netbeans.jemmy.operators.Operator
meth public abstract void makeVisible(org.netbeans.jemmy.operators.ComponentOperator)

CLSS public static org.netbeans.jemmy.operators.Operator$DefaultPathParser
 outer org.netbeans.jemmy.operators.Operator
cons public init(java.lang.String)
intf org.netbeans.jemmy.operators.Operator$PathParser
meth public java.lang.String[] parse(java.lang.String)
supr java.lang.Object
hfds separator

CLSS public static org.netbeans.jemmy.operators.Operator$DefaultStringComparator
 outer org.netbeans.jemmy.operators.Operator
cons public init(boolean,boolean)
intf org.netbeans.jemmy.operators.Operator$StringComparator
meth public boolean equals(java.lang.String,java.lang.String)
supr java.lang.Object
hfds ccs,ce

CLSS public static org.netbeans.jemmy.operators.Operator$Finder
 outer org.netbeans.jemmy.operators.Operator
cons public init(java.lang.Class)
cons public init(java.lang.Class,org.netbeans.jemmy.ComponentChooser)
intf org.netbeans.jemmy.ComponentChooser
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
supr java.lang.Object
hfds clz,subchooser

CLSS protected abstract org.netbeans.jemmy.operators.Operator$MapAction
 outer org.netbeans.jemmy.operators.Operator
cons public init(org.netbeans.jemmy.operators.Operator,java.lang.String)
meth public abstract java.lang.Object map() throws java.lang.Exception
meth public final java.lang.Object launch() throws java.lang.Exception
supr org.netbeans.jemmy.QueueTool$QueueAction

CLSS protected abstract org.netbeans.jemmy.operators.Operator$MapBooleanAction
 outer org.netbeans.jemmy.operators.Operator
cons public init(org.netbeans.jemmy.operators.Operator,java.lang.String)
meth public abstract boolean map() throws java.lang.Exception
meth public final java.lang.Object launch() throws java.lang.Exception
supr org.netbeans.jemmy.QueueTool$QueueAction

CLSS protected abstract org.netbeans.jemmy.operators.Operator$MapByteAction
 outer org.netbeans.jemmy.operators.Operator
cons public init(org.netbeans.jemmy.operators.Operator,java.lang.String)
meth public abstract byte map() throws java.lang.Exception
meth public final java.lang.Object launch() throws java.lang.Exception
supr org.netbeans.jemmy.QueueTool$QueueAction

CLSS protected abstract org.netbeans.jemmy.operators.Operator$MapCharacterAction
 outer org.netbeans.jemmy.operators.Operator
cons public init(org.netbeans.jemmy.operators.Operator,java.lang.String)
meth public abstract char map() throws java.lang.Exception
meth public final java.lang.Object launch() throws java.lang.Exception
supr org.netbeans.jemmy.QueueTool$QueueAction

CLSS protected abstract org.netbeans.jemmy.operators.Operator$MapDoubleAction
 outer org.netbeans.jemmy.operators.Operator
cons public init(org.netbeans.jemmy.operators.Operator,java.lang.String)
meth public abstract double map() throws java.lang.Exception
meth public final java.lang.Object launch() throws java.lang.Exception
supr org.netbeans.jemmy.QueueTool$QueueAction

CLSS protected abstract org.netbeans.jemmy.operators.Operator$MapFloatAction
 outer org.netbeans.jemmy.operators.Operator
cons public init(org.netbeans.jemmy.operators.Operator,java.lang.String)
meth public abstract float map() throws java.lang.Exception
meth public final java.lang.Object launch() throws java.lang.Exception
supr org.netbeans.jemmy.QueueTool$QueueAction

CLSS protected abstract org.netbeans.jemmy.operators.Operator$MapIntegerAction
 outer org.netbeans.jemmy.operators.Operator
cons public init(org.netbeans.jemmy.operators.Operator,java.lang.String)
meth public abstract int map() throws java.lang.Exception
meth public final java.lang.Object launch() throws java.lang.Exception
supr org.netbeans.jemmy.QueueTool$QueueAction

CLSS protected abstract org.netbeans.jemmy.operators.Operator$MapLongAction
 outer org.netbeans.jemmy.operators.Operator
cons public init(org.netbeans.jemmy.operators.Operator,java.lang.String)
meth public abstract long map() throws java.lang.Exception
meth public final java.lang.Object launch() throws java.lang.Exception
supr org.netbeans.jemmy.QueueTool$QueueAction

CLSS protected abstract org.netbeans.jemmy.operators.Operator$MapVoidAction
 outer org.netbeans.jemmy.operators.Operator
cons public init(org.netbeans.jemmy.operators.Operator,java.lang.String)
meth public abstract void map() throws java.lang.Exception
meth public final java.lang.Object launch() throws java.lang.Exception
supr org.netbeans.jemmy.QueueTool$QueueAction

CLSS protected abstract org.netbeans.jemmy.operators.Operator$NoBlockingAction
 outer org.netbeans.jemmy.operators.Operator
cons public init(org.netbeans.jemmy.operators.Operator,java.lang.String)
intf org.netbeans.jemmy.Action
meth protected void setException(java.lang.Exception)
meth public abstract java.lang.Object doAction(java.lang.Object)
meth public final java.lang.Object launch(java.lang.Object)
meth public java.lang.Exception getException()
meth public java.lang.String getDescription()
supr java.lang.Object
hfds description,exception

CLSS public abstract interface static org.netbeans.jemmy.operators.Operator$PathParser
 outer org.netbeans.jemmy.operators.Operator
meth public abstract java.lang.String[] parse(java.lang.String)

CLSS public abstract interface static org.netbeans.jemmy.operators.Operator$StringComparator
 outer org.netbeans.jemmy.operators.Operator
meth public abstract boolean equals(java.lang.String,java.lang.String)

CLSS public org.netbeans.jemmy.operators.ScrollPaneOperator
cons public init(java.awt.ScrollPane)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
innr public static ScrollPaneFinder
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth public boolean checkInside(java.awt.Component)
meth public boolean checkInside(java.awt.Component,int,int,int,int)
meth public boolean isScrollbarVisible(int)
meth public int getHScrollbarHeight()
meth public int getScrollbarDisplayPolicy()
meth public int getVScrollbarWidth()
meth public java.awt.Adjustable getHAdjustable()
meth public java.awt.Adjustable getVAdjustable()
meth public java.awt.Dimension getViewportSize()
meth public java.awt.Point getScrollPosition()
meth public java.lang.String paramString()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public static java.awt.ScrollPane findScrollPane(java.awt.Container)
meth public static java.awt.ScrollPane findScrollPane(java.awt.Container,int)
meth public static java.awt.ScrollPane findScrollPane(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.ScrollPane findScrollPane(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static java.awt.ScrollPane findScrollPaneUnder(java.awt.Component)
meth public static java.awt.ScrollPane findScrollPaneUnder(java.awt.Component,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.ScrollPane waitScrollPane(java.awt.Container)
meth public static java.awt.ScrollPane waitScrollPane(java.awt.Container,int)
meth public static java.awt.ScrollPane waitScrollPane(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.ScrollPane waitScrollPane(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void copyEnvironment(org.netbeans.jemmy.operators.Operator)
meth public void scrollTo(org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster)
meth public void scrollToBottom()
meth public void scrollToComponent(java.awt.Component)
meth public void scrollToComponentPoint(java.awt.Component,int,int)
meth public void scrollToComponentRectangle(java.awt.Component,int,int,int,int)
meth public void scrollToHorizontalValue(double)
meth public void scrollToHorizontalValue(int)
meth public void scrollToLeft()
meth public void scrollToRight()
meth public void scrollToTop()
meth public void scrollToValues(double,double)
meth public void scrollToValues(int,int)
meth public void scrollToVerticalValue(double)
meth public void scrollToVerticalValue(int)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setScrollPosition(int,int)
meth public void setScrollPosition(java.awt.Point)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
meth public void setValues(int,int)
supr org.netbeans.jemmy.operators.ContainerOperator
hfds X_POINT_RECT_SIZE,Y_POINT_RECT_SIZE,driver,output,timeouts
hcls ComponentRectChecker,ValueScrollAdjuster

CLSS public static org.netbeans.jemmy.operators.ScrollPaneOperator$ScrollPaneFinder
 outer org.netbeans.jemmy.operators.ScrollPaneOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.ScrollbarOperator
cons public init(java.awt.Scrollbar)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
innr public static ScrollbarFinder
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth public int getBlockIncrement()
meth public int getMaximum()
meth public int getMinimum()
meth public int getOrientation()
meth public int getUnitIncrement()
meth public int getValue()
meth public int getVisibleAmount()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public static java.awt.Scrollbar findScrollbar(java.awt.Container)
meth public static java.awt.Scrollbar findScrollbar(java.awt.Container,int)
meth public static java.awt.Scrollbar findScrollbar(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.Scrollbar findScrollbar(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static java.awt.Scrollbar waitScrollbar(java.awt.Container)
meth public static java.awt.Scrollbar waitScrollbar(java.awt.Container,int)
meth public static java.awt.Scrollbar waitScrollbar(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.Scrollbar waitScrollbar(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void addAdjustmentListener(java.awt.event.AdjustmentListener)
meth public void copyEnvironment(org.netbeans.jemmy.operators.Operator)
meth public void removeAdjustmentListener(java.awt.event.AdjustmentListener)
meth public void scrollTo(org.netbeans.jemmy.Waitable,java.lang.Object,boolean)
meth public void scrollTo(org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster)
meth public void scrollToMaximum()
meth public void scrollToMinimum()
meth public void scrollToValue(double)
meth public void scrollToValue(int)
meth public void setBlockIncrement(int)
meth public void setMaximum(int)
meth public void setMinimum(int)
meth public void setOrientation(int)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
meth public void setUnitIncrement(int)
meth public void setValue(int)
meth public void setValues(int,int,int,int)
meth public void setVisibleAmount(int)
supr org.netbeans.jemmy.operators.ComponentOperator
hfds BEFORE_DROP_TIMEOUT,DRAG_AND_DROP_SCROLLING_DELTA,ONE_SCROLL_CLICK_TIMEOUT,WHOLE_SCROLL_TIMEOUT,driver,output,timeouts
hcls ValueScrollAdjuster,WaitableChecker

CLSS public static org.netbeans.jemmy.operators.ScrollbarOperator$ScrollbarFinder
 outer org.netbeans.jemmy.operators.ScrollbarOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.TextAreaOperator
cons public init(java.awt.TextArea)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static java.lang.String TEXT_DPROP = "Text"
innr public static TextAreaByTextFinder
innr public static TextAreaFinder
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth public int getColumns()
meth public int getRows()
meth public int getScrollbarVisibility()
meth public java.awt.Dimension getMinimumSize(int,int)
meth public java.awt.Dimension getPreferredSize(int,int)
meth public java.util.Hashtable getDump()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public static java.awt.TextArea findTextArea(java.awt.Container,java.lang.String,boolean,boolean)
meth public static java.awt.TextArea findTextArea(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static java.awt.TextArea findTextArea(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.TextArea findTextArea(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static java.awt.TextArea waitTextArea(java.awt.Container,java.lang.String,boolean,boolean)
meth public static java.awt.TextArea waitTextArea(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static java.awt.TextArea waitTextArea(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.TextArea waitTextArea(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void replaceRange(java.lang.String,int,int)
meth public void setColumns(int)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setRows(int)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
supr org.netbeans.jemmy.operators.TextComponentOperator
hfds BETWEEN_KEYS_TIMEOUT,CHANGE_CARET_POSITION_TIMEOUT,PUSH_KEY_TIMEOUT,TYPE_TEXT_TIMEOUT,output,timeouts

CLSS public static org.netbeans.jemmy.operators.TextAreaOperator$TextAreaByTextFinder
 outer org.netbeans.jemmy.operators.TextAreaOperator
cons public init(java.lang.String)
cons public init(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
intf org.netbeans.jemmy.ComponentChooser
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
supr java.lang.Object
hfds comparator,label

CLSS public static org.netbeans.jemmy.operators.TextAreaOperator$TextAreaFinder
 outer org.netbeans.jemmy.operators.TextAreaOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.TextComponentOperator
cons public init(java.awt.TextComponent)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static java.lang.String TEXT_DPROP = "Text"
innr public static TextComponentByTextFinder
innr public static TextComponentFinder
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth protected org.netbeans.jemmy.drivers.TextDriver getTextDriver()
meth public boolean isEditable()
meth public int getCaretPosition()
meth public int getPositionByText(java.lang.String)
meth public int getPositionByText(java.lang.String,int)
meth public int getSelectionEnd()
meth public int getSelectionStart()
meth public java.lang.String getSelectedText()
meth public java.lang.String getText()
meth public java.util.Hashtable getDump()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public static java.awt.TextComponent findTextComponent(java.awt.Container,java.lang.String,boolean,boolean)
meth public static java.awt.TextComponent findTextComponent(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static java.awt.TextComponent findTextComponent(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.TextComponent findTextComponent(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static java.awt.TextComponent waitTextComponent(java.awt.Container,java.lang.String,boolean,boolean)
meth public static java.awt.TextComponent waitTextComponent(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static java.awt.TextComponent waitTextComponent(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.TextComponent waitTextComponent(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void addTextListener(java.awt.event.TextListener)
meth public void changeCaretPosition(int)
meth public void clearText()
meth public void copyEnvironment(org.netbeans.jemmy.operators.Operator)
meth public void enterText(java.lang.String)
meth public void removeTextListener(java.awt.event.TextListener)
meth public void select(int,int)
meth public void selectAll()
meth public void selectText(int,int)
meth public void setCaretPosition(int)
meth public void setEditable(boolean)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setSelectionEnd(int)
meth public void setSelectionStart(int)
meth public void setText(java.lang.String)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
meth public void typeText(java.lang.String)
meth public void typeText(java.lang.String,int)
supr org.netbeans.jemmy.operators.ComponentOperator
hfds BETWEEN_KEYS_TIMEOUT,CHANGE_CARET_POSITION_TIMEOUT,PUSH_KEY_TIMEOUT,TYPE_TEXT_TIMEOUT,driver,output,timeouts

CLSS public static org.netbeans.jemmy.operators.TextComponentOperator$TextComponentByTextFinder
 outer org.netbeans.jemmy.operators.TextComponentOperator
cons public init(java.lang.String)
cons public init(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
intf org.netbeans.jemmy.ComponentChooser
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
supr java.lang.Object
hfds comparator,label

CLSS public static org.netbeans.jemmy.operators.TextComponentOperator$TextComponentFinder
 outer org.netbeans.jemmy.operators.TextComponentOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.TextFieldOperator
cons public init(java.awt.TextField)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static java.lang.String TEXT_DPROP = "Text"
innr public static TextFieldByTextFinder
innr public static TextFieldFinder
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth public boolean echoCharIsSet()
meth public char getEchoChar()
meth public int getColumns()
meth public java.awt.Dimension getMinimumSize(int)
meth public java.awt.Dimension getPreferredSize(int)
meth public java.util.Hashtable getDump()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public static java.awt.TextField findTextField(java.awt.Container,java.lang.String,boolean,boolean)
meth public static java.awt.TextField findTextField(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static java.awt.TextField findTextField(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.TextField findTextField(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static java.awt.TextField waitTextField(java.awt.Container,java.lang.String,boolean,boolean)
meth public static java.awt.TextField waitTextField(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static java.awt.TextField waitTextField(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.TextField waitTextField(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void addActionListener(java.awt.event.ActionListener)
meth public void removeActionListener(java.awt.event.ActionListener)
meth public void setColumns(int)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
supr org.netbeans.jemmy.operators.TextComponentOperator
hfds BETWEEN_KEYS_TIMEOUT,CHANGE_CARET_POSITION_TIMEOUT,PUSH_KEY_TIMEOUT,TYPE_TEXT_TIMEOUT,output,timeouts

CLSS public static org.netbeans.jemmy.operators.TextFieldOperator$TextFieldByTextFinder
 outer org.netbeans.jemmy.operators.TextFieldOperator
cons public init(java.lang.String)
cons public init(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
intf org.netbeans.jemmy.ComponentChooser
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
supr java.lang.Object
hfds comparator,label

CLSS public static org.netbeans.jemmy.operators.TextFieldOperator$TextFieldFinder
 outer org.netbeans.jemmy.operators.TextFieldOperator
cons public init()
cons public init(org.netbeans.jemmy.ComponentChooser)
supr org.netbeans.jemmy.operators.Operator$Finder

CLSS public org.netbeans.jemmy.operators.WindowOperator
cons public init()
cons public init(int)
cons public init(int,org.netbeans.jemmy.operators.Operator)
cons public init(java.awt.Window)
cons public init(org.netbeans.jemmy.operators.WindowOperator)
cons public init(org.netbeans.jemmy.operators.WindowOperator,int)
cons public init(org.netbeans.jemmy.operators.WindowOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.WindowOperator,org.netbeans.jemmy.ComponentChooser,int)
intf org.netbeans.jemmy.Outputable
meth protected static java.awt.Window waitWindow(java.awt.Window,org.netbeans.jemmy.ComponentChooser,int,org.netbeans.jemmy.Timeouts,org.netbeans.jemmy.TestOut)
meth protected static java.awt.Window waitWindow(org.netbeans.jemmy.ComponentChooser,int,org.netbeans.jemmy.Timeouts,org.netbeans.jemmy.TestOut)
meth protected static java.awt.Window waitWindow(org.netbeans.jemmy.operators.WindowOperator,org.netbeans.jemmy.ComponentChooser,int)
meth public boolean isActive()
meth public boolean isFocused()
meth public java.awt.Component getFocusOwner()
meth public java.awt.Window findSubWindow(org.netbeans.jemmy.ComponentChooser)
meth public java.awt.Window findSubWindow(org.netbeans.jemmy.ComponentChooser,int)
meth public java.awt.Window getOwner()
meth public java.awt.Window waitSubWindow(org.netbeans.jemmy.ComponentChooser)
meth public java.awt.Window waitSubWindow(org.netbeans.jemmy.ComponentChooser,int)
meth public java.awt.Window[] getOwnedWindows()
meth public java.lang.String getWarningString()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public static java.awt.Window findWindow(java.awt.Window,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.Window findWindow(java.awt.Window,org.netbeans.jemmy.ComponentChooser,int)
meth public static java.awt.Window findWindow(org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.Window findWindow(org.netbeans.jemmy.ComponentChooser,int)
meth public static java.awt.Window waitWindow(java.awt.Window,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.Window waitWindow(java.awt.Window,org.netbeans.jemmy.ComponentChooser,int)
meth public static java.awt.Window waitWindow(org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.Window waitWindow(org.netbeans.jemmy.ComponentChooser,int)
meth public void activate()
meth public void addWindowListener(java.awt.event.WindowListener)
meth public void applyResourceBundle(java.lang.String)
meth public void applyResourceBundle(java.util.ResourceBundle)
meth public void close()
meth public void copyEnvironment(org.netbeans.jemmy.operators.Operator)
meth public void dispose()
meth public void move(int,int)
meth public void pack()
meth public void removeWindowListener(java.awt.event.WindowListener)
meth public void requestClose()
meth public void requestCloseAndThenHide()
meth public void resize(int,int)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void toBack()
meth public void toFront()
meth public void waitClosed()
supr org.netbeans.jemmy.operators.ContainerOperator
hfds driver,output

CLSS public abstract org.netbeans.jemmy.util.AbstractTextStyleChooser
cons public init()
intf org.netbeans.jemmy.operators.JTextComponentOperator$TextChooser
meth public abstract boolean checkElement(javax.swing.text.StyledDocument,javax.swing.text.Element,int)
meth public abstract java.lang.String getDescription()
meth public final boolean checkPosition(javax.swing.text.Document,int)
supr java.lang.Object

CLSS public org.netbeans.jemmy.util.DefaultVisualizer
cons public init()
intf java.lang.Cloneable
intf org.netbeans.jemmy.operators.Operator$ComponentVisualizer
meth protected boolean isWindowActive(org.netbeans.jemmy.operators.WindowOperator)
meth protected void activate(org.netbeans.jemmy.operators.WindowOperator)
meth protected void initInternalFrame(org.netbeans.jemmy.operators.JInternalFrameOperator)
meth protected void makeWindowActive(org.netbeans.jemmy.operators.WindowOperator)
meth protected void scroll(org.netbeans.jemmy.operators.JScrollPaneOperator,java.awt.Component)
meth protected void switchTab(org.netbeans.jemmy.operators.JTabbedPaneOperator,java.awt.Component)
meth public org.netbeans.jemmy.util.DefaultVisualizer cloneThis()
meth public void activateInternalFrame(boolean)
meth public void activateWindow(boolean)
meth public void checkForModal(boolean)
meth public void makeVisible(org.netbeans.jemmy.operators.ComponentOperator)
meth public void scroll(boolean)
meth public void switchTab(boolean)
supr java.lang.Object
hfds internalFrame,modal,scroll,switchTab,window

CLSS public abstract interface org.netbeans.jemmy.util.DumpController
meth public abstract boolean onComponentDump(java.awt.Component)
meth public abstract boolean onPropertyDump(java.awt.Component,java.lang.String,java.lang.String)

CLSS public org.netbeans.jemmy.util.Dumper
cons public init()
meth public static java.lang.String escape(java.lang.String)
meth public static void dumpAll(java.io.PrintStream)
meth public static void dumpAll(java.io.PrintStream,org.netbeans.jemmy.util.DumpController)
meth public static void dumpAll(java.io.PrintWriter)
meth public static void dumpAll(java.io.PrintWriter,org.netbeans.jemmy.util.DumpController)
meth public static void dumpAll(java.lang.String) throws java.io.FileNotFoundException
meth public static void dumpAll(java.lang.String,org.netbeans.jemmy.util.DumpController) throws java.io.FileNotFoundException
meth public static void dumpComponent(java.awt.Component,java.io.PrintStream)
meth public static void dumpComponent(java.awt.Component,java.io.PrintStream,org.netbeans.jemmy.util.DumpController)
meth public static void dumpComponent(java.awt.Component,java.io.PrintWriter)
meth public static void dumpComponent(java.awt.Component,java.io.PrintWriter,org.netbeans.jemmy.util.DumpController)
meth public static void dumpComponent(java.awt.Component,java.lang.String) throws java.io.FileNotFoundException
meth public static void dumpComponent(java.awt.Component,java.lang.String,org.netbeans.jemmy.util.DumpController) throws java.io.FileNotFoundException
meth public static void printDTD(java.io.PrintStream)
meth public static void printDTD(java.io.PrintWriter)
meth public static void printDTD(java.lang.String) throws java.io.FileNotFoundException
supr java.lang.Object
hfds tabIncrease

CLSS public org.netbeans.jemmy.util.EmptyVisualizer
cons public init()
intf org.netbeans.jemmy.operators.Operator$ComponentVisualizer
meth public void makeVisible(org.netbeans.jemmy.operators.ComponentOperator)
supr java.lang.Object

CLSS public org.netbeans.jemmy.util.MouseVisualizer
cons public init()
cons public init(int,double,int,boolean)
fld public static int BOTTOM
fld public static int LEFT
fld public static int RIGHT
fld public static int TOP
meth protected boolean isWindowActive(org.netbeans.jemmy.operators.WindowOperator)
meth protected void makeWindowActive(org.netbeans.jemmy.operators.WindowOperator)
supr org.netbeans.jemmy.util.DefaultVisualizer
hfds BEFORE_CLICK,depth,place,pointLocation

CLSS public org.netbeans.jemmy.util.NameComponentChooser
cons public init(java.lang.String)
cons public init(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
intf org.netbeans.jemmy.ComponentChooser
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
supr java.lang.Object
hfds comparator,name

CLSS public org.netbeans.jemmy.util.PNGDecoder
cons public init(java.io.InputStream)
meth public java.awt.image.BufferedImage decode() throws java.io.IOException
meth public static java.awt.image.BufferedImage decode(java.lang.String)
supr java.lang.Object
hfds in

CLSS public org.netbeans.jemmy.util.PNGEncoder
cons public init(java.io.OutputStream)
cons public init(java.io.OutputStream,byte)
fld public final static byte BW_MODE = 0
fld public final static byte COLOR_MODE = 2
fld public final static byte GREYSCALE_MODE = 1
meth public static void captureScreen(java.awt.Component,java.lang.String)
meth public static void captureScreen(java.awt.Component,java.lang.String,byte)
meth public static void captureScreen(java.awt.Rectangle,java.lang.String)
meth public static void captureScreen(java.awt.Rectangle,java.lang.String,byte)
meth public static void captureScreen(java.lang.String)
meth public static void captureScreen(java.lang.String,byte)
meth public void encode(java.awt.image.BufferedImage) throws java.io.IOException
supr java.lang.Object
hfds crc,mode,out

CLSS public org.netbeans.jemmy.util.PropChooser
cons public init(java.lang.String[],java.lang.Object[])
cons public init(java.lang.String[],java.lang.Object[][],java.lang.Class[][],java.lang.Object[])
fld protected java.lang.Class[][] classes
fld protected java.lang.Object[] results
fld protected java.lang.Object[][] params
fld protected java.lang.String[] propNames
intf org.netbeans.jemmy.ComponentChooser
intf org.netbeans.jemmy.Outputable
meth protected boolean checkProperty(java.lang.Object,java.lang.Object)
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public void setOutput(org.netbeans.jemmy.TestOut)
supr java.lang.Object
hfds output

CLSS public org.netbeans.jemmy.util.RegExComparator
cons public init()
innr public static RegExParsingException
intf org.netbeans.jemmy.operators.Operator$StringComparator
meth public boolean equals(java.lang.String,java.lang.String)
meth public boolean parse(java.lang.String,java.lang.String)
supr java.lang.Object
hfds ANY_SIMBOL,IGNORE_SIMBOL

CLSS public static org.netbeans.jemmy.util.RegExComparator$RegExParsingException
 outer org.netbeans.jemmy.util.RegExComparator
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Exception)
supr org.netbeans.jemmy.JemmyException

CLSS public org.netbeans.jemmy.util.StringPropChooser
cons public init(java.lang.String,boolean,boolean)
cons public init(java.lang.String,java.lang.Object[][],java.lang.Class[][],boolean,boolean)
cons public init(java.lang.String,java.lang.Object[][],java.lang.Class[][],org.netbeans.jemmy.operators.Operator$StringComparator)
cons public init(java.lang.String,java.lang.String,java.lang.String,boolean,boolean)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.Object[][],java.lang.Class[][],boolean,boolean)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.Object[][],java.lang.Class[][],org.netbeans.jemmy.operators.Operator$StringComparator)
cons public init(java.lang.String,java.lang.String,java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
cons public init(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
cons public init(java.lang.String[],java.lang.Object[][],java.lang.Class[][],java.lang.String[],boolean,boolean)
cons public init(java.lang.String[],java.lang.Object[][],java.lang.Class[][],java.lang.String[],org.netbeans.jemmy.operators.Operator$StringComparator)
cons public init(java.lang.String[],java.lang.String[],boolean,boolean)
cons public init(java.lang.String[],java.lang.String[],org.netbeans.jemmy.operators.Operator$StringComparator)
meth protected boolean checkProperty(java.lang.Object,java.lang.Object)
meth public java.lang.String getDescription()
supr org.netbeans.jemmy.util.PropChooser
hfds comparator

CLSS public org.netbeans.jemmy.util.TextStyleChooser
cons public init()
meth public boolean checkElement(javax.swing.text.StyledDocument,javax.swing.text.Element,int)
meth public java.lang.String getDescription()
meth public void setAlignment(int)
meth public void setBackground(java.awt.Color)
meth public void setBold(boolean)
meth public void setFontFamily(java.lang.String)
meth public void setFontSize(int)
meth public void setForeground(java.awt.Color)
meth public void setItalic(boolean)
meth public void setStrike(boolean)
meth public void setUnderstrike(boolean)
meth public void unsetAlignment()
meth public void unsetBackground()
meth public void unsetBold()
meth public void unsetFontFamily()
meth public void unsetFontSize()
meth public void unsetForeground()
meth public void unsetItalic()
meth public void unsetStrike()
meth public void unsetUnderstrike()
supr org.netbeans.jemmy.util.AbstractTextStyleChooser
hfds alignment,background,bold,fontFamily,fontSize,foreground,italic,strike,understrike

CLSS public abstract interface org.netbeans.jemmy.util.WindowJob
intf org.netbeans.jemmy.Action
intf org.netbeans.jemmy.ComponentChooser
meth public abstract boolean checkComponent(java.awt.Component)
meth public abstract java.lang.Object launch(java.lang.Object)
meth public abstract java.lang.String getDescription()

CLSS public org.netbeans.jemmy.util.WindowManager
innr public static ModalDialogChoosingJob
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public static void addJob(org.netbeans.jemmy.util.WindowJob)
meth public static void performJob(org.netbeans.jemmy.util.WindowJob)
meth public static void removeJob(org.netbeans.jemmy.util.WindowJob)
meth public void add(org.netbeans.jemmy.util.WindowJob)
meth public void remove(org.netbeans.jemmy.util.WindowJob)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
supr java.lang.Object
hfds TIME_DELTA,jobs,manager,output,timeouts
hcls JobThread

CLSS public static org.netbeans.jemmy.util.WindowManager$ModalDialogChoosingJob
 outer org.netbeans.jemmy.util.WindowManager
cons public init()
intf org.netbeans.jemmy.util.WindowJob
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.Object launch(java.lang.Object)
meth public java.lang.String getDescription()
supr java.lang.Object

