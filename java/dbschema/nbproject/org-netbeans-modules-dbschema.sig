#Signature file v4.1
#Version 1.64.0

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

CLSS public abstract interface java.io.DataInput
meth public abstract boolean readBoolean() throws java.io.IOException
meth public abstract byte readByte() throws java.io.IOException
meth public abstract char readChar() throws java.io.IOException
meth public abstract double readDouble() throws java.io.IOException
meth public abstract float readFloat() throws java.io.IOException
meth public abstract int readInt() throws java.io.IOException
meth public abstract int readUnsignedByte() throws java.io.IOException
meth public abstract int readUnsignedShort() throws java.io.IOException
meth public abstract int skipBytes(int) throws java.io.IOException
meth public abstract java.lang.String readLine() throws java.io.IOException
meth public abstract java.lang.String readUTF() throws java.io.IOException
meth public abstract long readLong() throws java.io.IOException
meth public abstract short readShort() throws java.io.IOException
meth public abstract void readFully(byte[]) throws java.io.IOException
meth public abstract void readFully(byte[],int,int) throws java.io.IOException

CLSS public java.io.DataInputStream
cons public init(java.io.InputStream)
intf java.io.DataInput
meth public final boolean readBoolean() throws java.io.IOException
meth public final byte readByte() throws java.io.IOException
meth public final char readChar() throws java.io.IOException
meth public final double readDouble() throws java.io.IOException
meth public final float readFloat() throws java.io.IOException
meth public final int read(byte[]) throws java.io.IOException
meth public final int read(byte[],int,int) throws java.io.IOException
meth public final int readInt() throws java.io.IOException
meth public final int readUnsignedByte() throws java.io.IOException
meth public final int readUnsignedShort() throws java.io.IOException
meth public final int skipBytes(int) throws java.io.IOException
meth public final java.lang.String readLine() throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public final java.lang.String readUTF() throws java.io.IOException
meth public final long readLong() throws java.io.IOException
meth public final short readShort() throws java.io.IOException
meth public final static java.lang.String readUTF(java.io.DataInput) throws java.io.IOException
meth public final void readFully(byte[]) throws java.io.IOException
meth public final void readFully(byte[],int,int) throws java.io.IOException
supr java.io.FilterInputStream

CLSS public abstract interface java.io.DataOutput
meth public abstract void write(byte[]) throws java.io.IOException
meth public abstract void write(byte[],int,int) throws java.io.IOException
meth public abstract void write(int) throws java.io.IOException
meth public abstract void writeBoolean(boolean) throws java.io.IOException
meth public abstract void writeByte(int) throws java.io.IOException
meth public abstract void writeBytes(java.lang.String) throws java.io.IOException
meth public abstract void writeChar(int) throws java.io.IOException
meth public abstract void writeChars(java.lang.String) throws java.io.IOException
meth public abstract void writeDouble(double) throws java.io.IOException
meth public abstract void writeFloat(float) throws java.io.IOException
meth public abstract void writeInt(int) throws java.io.IOException
meth public abstract void writeLong(long) throws java.io.IOException
meth public abstract void writeShort(int) throws java.io.IOException
meth public abstract void writeUTF(java.lang.String) throws java.io.IOException

CLSS public java.io.DataOutputStream
cons public init(java.io.OutputStream)
fld protected int written
intf java.io.DataOutput
meth public final int size()
meth public final void writeBoolean(boolean) throws java.io.IOException
meth public final void writeByte(int) throws java.io.IOException
meth public final void writeBytes(java.lang.String) throws java.io.IOException
meth public final void writeChar(int) throws java.io.IOException
meth public final void writeChars(java.lang.String) throws java.io.IOException
meth public final void writeDouble(double) throws java.io.IOException
meth public final void writeFloat(float) throws java.io.IOException
meth public final void writeInt(int) throws java.io.IOException
meth public final void writeLong(long) throws java.io.IOException
meth public final void writeShort(int) throws java.io.IOException
meth public final void writeUTF(java.lang.String) throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void write(byte[],int,int) throws java.io.IOException
meth public void write(int) throws java.io.IOException
supr java.io.FilterOutputStream

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

CLSS public java.io.FilterOutputStream
cons public init(java.io.OutputStream)
fld protected java.io.OutputStream out
meth public void close() throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void write(byte[]) throws java.io.IOException
meth public void write(byte[],int,int) throws java.io.IOException
meth public void write(int) throws java.io.IOException
supr java.io.OutputStream

CLSS public abstract interface java.io.Flushable
meth public abstract void flush() throws java.io.IOException

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

CLSS public abstract interface java.io.ObjectInput
intf java.io.DataInput
intf java.lang.AutoCloseable
meth public abstract int available() throws java.io.IOException
meth public abstract int read() throws java.io.IOException
meth public abstract int read(byte[]) throws java.io.IOException
meth public abstract int read(byte[],int,int) throws java.io.IOException
meth public abstract java.lang.Object readObject() throws java.io.IOException,java.lang.ClassNotFoundException
meth public abstract long skip(long) throws java.io.IOException
meth public abstract void close() throws java.io.IOException

CLSS public abstract interface java.io.ObjectOutput
intf java.io.DataOutput
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException
meth public abstract void flush() throws java.io.IOException
meth public abstract void write(byte[]) throws java.io.IOException
meth public abstract void write(byte[],int,int) throws java.io.IOException
meth public abstract void write(int) throws java.io.IOException
meth public abstract void writeObject(java.lang.Object) throws java.io.IOException

CLSS public abstract java.io.OutputStream
cons public init()
intf java.io.Closeable
intf java.io.Flushable
meth public abstract void write(int) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void write(byte[]) throws java.io.IOException
meth public void write(byte[],int,int) throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.AutoCloseable
meth public abstract void close() throws java.lang.Exception

CLSS public abstract interface java.lang.Cloneable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

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

CLSS public abstract interface java.util.EventListener

CLSS public abstract interface javax.accessibility.Accessible
meth public abstract javax.accessibility.AccessibleContext getAccessibleContext()

CLSS public abstract javax.swing.AbstractListModel<%0 extends java.lang.Object>
cons public init()
fld protected javax.swing.event.EventListenerList listenerList
intf java.io.Serializable
intf javax.swing.ListModel<{javax.swing.AbstractListModel%0}>
meth protected void fireContentsChanged(java.lang.Object,int,int)
meth protected void fireIntervalAdded(java.lang.Object,int,int)
meth protected void fireIntervalRemoved(java.lang.Object,int,int)
meth public <%0 extends java.util.EventListener> {%%0}[] getListeners(java.lang.Class<{%%0}>)
meth public javax.swing.event.ListDataListener[] getListDataListeners()
meth public void addListDataListener(javax.swing.event.ListDataListener)
meth public void removeListDataListener(javax.swing.event.ListDataListener)
supr java.lang.Object

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

CLSS public abstract interface javax.swing.ListModel<%0 extends java.lang.Object>
meth public abstract int getSize()
meth public abstract void addListDataListener(javax.swing.event.ListDataListener)
meth public abstract void removeListDataListener(javax.swing.event.ListDataListener)
meth public abstract {javax.swing.ListModel%0} getElementAt(int)

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

CLSS public abstract interface javax.swing.event.ListDataListener
intf java.util.EventListener
meth public abstract void contentsChanged(javax.swing.event.ListDataEvent)
meth public abstract void intervalAdded(javax.swing.event.ListDataEvent)
meth public abstract void intervalRemoved(javax.swing.event.ListDataEvent)

CLSS public org.netbeans.modules.dbschema.ColumnElement
cons public init()
cons public init(org.netbeans.modules.dbschema.ColumnElement$Impl,org.netbeans.modules.dbschema.TableElement)
innr public abstract interface static Impl
meth public boolean equals(java.lang.Object)
meth public boolean isAutoIncrement()
meth public boolean isBlobType()
meth public boolean isCharacterType()
meth public boolean isNullable()
meth public boolean isNumericType()
meth public int getType()
meth public java.lang.Integer getLength()
meth public java.lang.Integer getPrecision()
meth public java.lang.Integer getScale()
meth public java.lang.Object clone()
meth public java.lang.String toString()
meth public void setLength(java.lang.Integer) throws org.netbeans.modules.dbschema.DBException
meth public void setNullable(boolean) throws org.netbeans.modules.dbschema.DBException
meth public void setPrecision(java.lang.Integer) throws org.netbeans.modules.dbschema.DBException
meth public void setScale(java.lang.Integer) throws org.netbeans.modules.dbschema.DBException
meth public void setType(int) throws org.netbeans.modules.dbschema.DBException
supr org.netbeans.modules.dbschema.DBMemberElement
hcls Memory

CLSS public abstract interface static org.netbeans.modules.dbschema.ColumnElement$Impl
 outer org.netbeans.modules.dbschema.ColumnElement
intf org.netbeans.modules.dbschema.DBMemberElement$Impl
meth public abstract boolean isAutoIncrement()
meth public abstract boolean isNullable()
meth public abstract int getType()
meth public abstract java.lang.Integer getLength()
meth public abstract java.lang.Integer getPrecision()
meth public abstract java.lang.Integer getScale()
meth public abstract void setLength(java.lang.Integer) throws org.netbeans.modules.dbschema.DBException
meth public abstract void setNullable(boolean) throws org.netbeans.modules.dbschema.DBException
meth public abstract void setPrecision(java.lang.Integer) throws org.netbeans.modules.dbschema.DBException
meth public abstract void setScale(java.lang.Integer) throws org.netbeans.modules.dbschema.DBException
meth public abstract void setType(int) throws org.netbeans.modules.dbschema.DBException

CLSS public abstract interface org.netbeans.modules.dbschema.ColumnElementHolder
meth public abstract org.netbeans.modules.dbschema.ColumnElement getColumn(org.netbeans.modules.dbschema.DBIdentifier)
meth public abstract org.netbeans.modules.dbschema.ColumnElement[] getColumns()
meth public abstract void addColumn(org.netbeans.modules.dbschema.ColumnElement) throws java.lang.Exception
meth public abstract void addColumns(org.netbeans.modules.dbschema.ColumnElement[]) throws java.lang.Exception
meth public abstract void removeColumn(org.netbeans.modules.dbschema.ColumnElement) throws java.lang.Exception
meth public abstract void removeColumns(org.netbeans.modules.dbschema.ColumnElement[]) throws java.lang.Exception
meth public abstract void setColumns(org.netbeans.modules.dbschema.ColumnElement[]) throws java.lang.Exception

CLSS public final org.netbeans.modules.dbschema.ColumnPairElement
cons public init()
cons public init(org.netbeans.modules.dbschema.ColumnElement,org.netbeans.modules.dbschema.ColumnElement,org.netbeans.modules.dbschema.TableElement)
cons public init(org.netbeans.modules.dbschema.ColumnPairElement$Impl,org.netbeans.modules.dbschema.ColumnElement,org.netbeans.modules.dbschema.ColumnElement,org.netbeans.modules.dbschema.TableElement)
innr public abstract interface static Impl
meth public final org.netbeans.modules.dbschema.ColumnElement getLocalColumn()
meth public final org.netbeans.modules.dbschema.ColumnElement getReferencedColumn()
meth public final void setLocalColumn(org.netbeans.modules.dbschema.ColumnElement)
meth public final void setReferencedColumn(org.netbeans.modules.dbschema.ColumnElement)
meth public org.netbeans.modules.dbschema.DBIdentifier getName()
supr org.netbeans.modules.dbschema.DBMemberElement
hfds _localColumn,_referencedColumn
hcls Memory

CLSS public abstract interface static org.netbeans.modules.dbschema.ColumnPairElement$Impl
 outer org.netbeans.modules.dbschema.ColumnPairElement
intf org.netbeans.modules.dbschema.DBMemberElement$Impl

CLSS public abstract interface org.netbeans.modules.dbschema.ColumnPairElementHolder
meth public abstract org.netbeans.modules.dbschema.ColumnPairElement getColumnPair(org.netbeans.modules.dbschema.DBIdentifier)
meth public abstract org.netbeans.modules.dbschema.ColumnPairElement[] getColumnPairs()
meth public abstract void addColumnPair(org.netbeans.modules.dbschema.ColumnPairElement) throws java.lang.Exception
meth public abstract void addColumnPairs(org.netbeans.modules.dbschema.ColumnPairElement[]) throws java.lang.Exception
meth public abstract void removeColumnPair(org.netbeans.modules.dbschema.ColumnPairElement) throws java.lang.Exception
meth public abstract void removeColumnPairs(org.netbeans.modules.dbschema.ColumnPairElement[]) throws java.lang.Exception
meth public abstract void setColumnPairs(org.netbeans.modules.dbschema.ColumnPairElement[]) throws java.lang.Exception

CLSS public abstract org.netbeans.modules.dbschema.DBElement
cons protected init(org.netbeans.modules.dbschema.DBElement$Impl)
cons public init()
innr public abstract interface static Impl
intf java.lang.Comparable
intf org.netbeans.modules.dbschema.DBElementProperties
meth public boolean equals(java.lang.Object)
meth public final org.netbeans.modules.dbschema.DBElement$Impl getElementImpl()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void setName(org.netbeans.modules.dbschema.DBIdentifier) throws org.netbeans.modules.dbschema.DBException
meth public int compareTo(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public org.netbeans.modules.dbschema.DBIdentifier getName()
meth public void setElementImpl(org.netbeans.modules.dbschema.DBElement$Impl)
supr java.lang.Object
hfds impl
hcls Memory

CLSS public abstract interface static org.netbeans.modules.dbschema.DBElement$Impl
 outer org.netbeans.modules.dbschema.DBElement
fld public final static int ADD = 1
fld public final static int REMOVE = -1
fld public final static int SET = 0
meth public abstract org.netbeans.modules.dbschema.DBIdentifier getName()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void attachToElement(org.netbeans.modules.dbschema.DBElement)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setName(org.netbeans.modules.dbschema.DBIdentifier) throws org.netbeans.modules.dbschema.DBException

CLSS public abstract interface org.netbeans.modules.dbschema.DBElementProperties
fld public final static java.lang.String PROP_CATALOG = "catalog"
fld public final static java.lang.String PROP_COLUMNS = "columns"
fld public final static java.lang.String PROP_COLUMN_PAIRS = "columnPairs"
fld public final static java.lang.String PROP_INDEXES = "indexes"
fld public final static java.lang.String PROP_KEYS = "keys"
fld public final static java.lang.String PROP_LENGTH = "length"
fld public final static java.lang.String PROP_LOCAL_COLUMN = "localColumn"
fld public final static java.lang.String PROP_NAME = "name"
fld public final static java.lang.String PROP_NULLABLE = "nullable"
fld public final static java.lang.String PROP_PK = "primaryKey"
fld public final static java.lang.String PROP_PRECISION = "precision"
fld public final static java.lang.String PROP_REFERENCED_COLUMN = "referencedColumn"
fld public final static java.lang.String PROP_SCALE = "scale"
fld public final static java.lang.String PROP_SCHEMA = "schema"
fld public final static java.lang.String PROP_STATUS = "status"
fld public final static java.lang.String PROP_TABLES = "tables"
fld public final static java.lang.String PROP_TABLE_OR_VIEW = "tableOrView"
fld public final static java.lang.String PROP_TYPE = "type"
fld public final static java.lang.String PROP_UNIQUE = "unique"

CLSS public org.netbeans.modules.dbschema.DBElementProvider
cons public init()
cons public init(org.netbeans.modules.dbschema.DBElement)
intf org.openide.nodes.Node$Cookie
meth public org.netbeans.modules.dbschema.DBElement getDBElement()
supr java.lang.Object
hfds element

CLSS public org.netbeans.modules.dbschema.DBException
cons public init()
cons public init(java.lang.String)
supr java.lang.Exception

CLSS public final org.netbeans.modules.dbschema.DBIdentifier
cons public init()
meth public boolean compareTo(org.netbeans.modules.dbschema.DBIdentifier,boolean)
meth public java.lang.String getFullName()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public static org.netbeans.modules.dbschema.DBIdentifier create(java.lang.String)
meth public void setFullName(java.lang.String)
meth public void setName(java.lang.String)
supr java.lang.Object
hfds fullName,name

CLSS public abstract org.netbeans.modules.dbschema.DBMemberElement
cons protected init()
cons protected init(org.netbeans.modules.dbschema.DBMemberElement$Impl,org.netbeans.modules.dbschema.TableElement)
innr public abstract interface static Impl
intf java.lang.Cloneable
meth public org.netbeans.modules.dbschema.TableElement getDeclaringTable()
meth public void setDeclaringTable(org.netbeans.modules.dbschema.TableElement)
supr org.netbeans.modules.dbschema.DBElement
hfds declaringTable
hcls Memory

CLSS public abstract interface static org.netbeans.modules.dbschema.DBMemberElement$Impl
 outer org.netbeans.modules.dbschema.DBMemberElement
intf org.netbeans.modules.dbschema.DBElement$Impl

CLSS public final org.netbeans.modules.dbschema.ForeignKeyElement
cons public init()
cons public init(org.netbeans.modules.dbschema.ForeignKeyElement$Impl,org.netbeans.modules.dbschema.TableElement)
innr public abstract interface static Impl
intf org.netbeans.modules.dbschema.ColumnPairElementHolder
intf org.netbeans.modules.dbschema.ReferenceKey
meth public java.lang.String getKeyName()
meth public org.netbeans.modules.dbschema.ColumnElement getColumn(org.netbeans.modules.dbschema.DBIdentifier)
meth public org.netbeans.modules.dbschema.ColumnElement[] getColumns()
meth public org.netbeans.modules.dbschema.ColumnElement[] getLocalColumns()
meth public org.netbeans.modules.dbschema.ColumnElement[] getReferencedColumns()
meth public org.netbeans.modules.dbschema.ColumnPairElement getColumnPair(org.netbeans.modules.dbschema.DBIdentifier)
meth public org.netbeans.modules.dbschema.ColumnPairElement[] getColumnPairs()
meth public org.netbeans.modules.dbschema.TableElement getReferencedTable()
meth public void addColumn(org.netbeans.modules.dbschema.ColumnElement)
meth public void addColumnPair(org.netbeans.modules.dbschema.ColumnPairElement) throws org.netbeans.modules.dbschema.DBException
meth public void addColumnPairs(org.netbeans.modules.dbschema.ColumnPairElement[]) throws org.netbeans.modules.dbschema.DBException
meth public void addColumns(org.netbeans.modules.dbschema.ColumnElement[])
meth public void removeColumn(org.netbeans.modules.dbschema.ColumnElement)
meth public void removeColumnPair(org.netbeans.modules.dbschema.ColumnPairElement) throws org.netbeans.modules.dbschema.DBException
meth public void removeColumnPairs(org.netbeans.modules.dbschema.ColumnPairElement[]) throws org.netbeans.modules.dbschema.DBException
meth public void removeColumns(org.netbeans.modules.dbschema.ColumnElement[])
meth public void setColumnPairs(org.netbeans.modules.dbschema.ColumnPairElement[]) throws org.netbeans.modules.dbschema.DBException
meth public void setColumns(org.netbeans.modules.dbschema.ColumnElement[])
meth public void setKeyName(java.lang.String) throws org.netbeans.modules.dbschema.DBException
supr org.netbeans.modules.dbschema.KeyElement
hcls Memory

CLSS public abstract interface static org.netbeans.modules.dbschema.ForeignKeyElement$Impl
 outer org.netbeans.modules.dbschema.ForeignKeyElement
intf org.netbeans.modules.dbschema.KeyElement$Impl
meth public abstract org.netbeans.modules.dbschema.ColumnPairElement getColumnPair(org.netbeans.modules.dbschema.DBIdentifier)
meth public abstract org.netbeans.modules.dbschema.ColumnPairElement[] getColumnPairs()
meth public abstract void changeColumnPairs(org.netbeans.modules.dbschema.ColumnPairElement[],int) throws org.netbeans.modules.dbschema.DBException

CLSS public final org.netbeans.modules.dbschema.IndexElement
cons public init()
cons public init(org.netbeans.modules.dbschema.IndexElement$Impl,org.netbeans.modules.dbschema.TableElement)
innr public abstract interface static Impl
intf org.netbeans.modules.dbschema.ColumnElementHolder
meth public boolean isUnique()
meth public org.netbeans.modules.dbschema.ColumnElement getColumn(org.netbeans.modules.dbschema.DBIdentifier)
meth public org.netbeans.modules.dbschema.ColumnElement[] getColumns()
meth public void addColumn(org.netbeans.modules.dbschema.ColumnElement) throws org.netbeans.modules.dbschema.DBException
meth public void addColumns(org.netbeans.modules.dbschema.ColumnElement[]) throws org.netbeans.modules.dbschema.DBException
meth public void removeColumn(org.netbeans.modules.dbschema.ColumnElement) throws org.netbeans.modules.dbschema.DBException
meth public void removeColumns(org.netbeans.modules.dbschema.ColumnElement[]) throws org.netbeans.modules.dbschema.DBException
meth public void setColumns(org.netbeans.modules.dbschema.ColumnElement[]) throws org.netbeans.modules.dbschema.DBException
meth public void setUnique(boolean) throws org.netbeans.modules.dbschema.DBException
supr org.netbeans.modules.dbschema.DBMemberElement
hcls Memory

CLSS public abstract interface static org.netbeans.modules.dbschema.IndexElement$Impl
 outer org.netbeans.modules.dbschema.IndexElement
intf org.netbeans.modules.dbschema.DBMemberElement$Impl
meth public abstract boolean isUnique()
meth public abstract org.netbeans.modules.dbschema.ColumnElement getColumn(org.netbeans.modules.dbschema.DBIdentifier)
meth public abstract org.netbeans.modules.dbschema.ColumnElement[] getColumns()
meth public abstract void changeColumns(org.netbeans.modules.dbschema.ColumnElement[],int) throws org.netbeans.modules.dbschema.DBException
meth public abstract void setUnique(boolean) throws org.netbeans.modules.dbschema.DBException

CLSS public abstract org.netbeans.modules.dbschema.KeyElement
cons public init()
cons public init(org.netbeans.modules.dbschema.KeyElement$Impl,org.netbeans.modules.dbschema.TableElement)
innr public abstract interface static Impl
intf org.netbeans.modules.dbschema.ColumnElementHolder
meth public org.netbeans.modules.dbschema.ColumnElement getColumn(org.netbeans.modules.dbschema.DBIdentifier)
meth public org.netbeans.modules.dbschema.ColumnElement[] getColumns()
meth public void addColumn(org.netbeans.modules.dbschema.ColumnElement) throws org.netbeans.modules.dbschema.DBException
meth public void addColumns(org.netbeans.modules.dbschema.ColumnElement[]) throws org.netbeans.modules.dbschema.DBException
meth public void removeColumn(org.netbeans.modules.dbschema.ColumnElement) throws org.netbeans.modules.dbschema.DBException
meth public void removeColumns(org.netbeans.modules.dbschema.ColumnElement[]) throws org.netbeans.modules.dbschema.DBException
meth public void setColumns(org.netbeans.modules.dbschema.ColumnElement[]) throws org.netbeans.modules.dbschema.DBException
supr org.netbeans.modules.dbschema.DBMemberElement
hcls Memory

CLSS public abstract interface static org.netbeans.modules.dbschema.KeyElement$Impl
 outer org.netbeans.modules.dbschema.KeyElement
intf org.netbeans.modules.dbschema.DBMemberElement$Impl
meth public abstract org.netbeans.modules.dbschema.ColumnElement getColumn(org.netbeans.modules.dbschema.DBIdentifier)
meth public abstract org.netbeans.modules.dbschema.ColumnElement[] getColumns()
meth public abstract void changeColumns(org.netbeans.modules.dbschema.ColumnElement[],int) throws org.netbeans.modules.dbschema.DBException

CLSS public abstract interface org.netbeans.modules.dbschema.ReferenceKey
intf org.netbeans.modules.dbschema.ColumnPairElementHolder
meth public abstract java.lang.String getKeyName()
meth public abstract org.netbeans.modules.dbschema.ColumnElement[] getLocalColumns()
meth public abstract org.netbeans.modules.dbschema.ColumnElement[] getReferencedColumns()
meth public abstract org.netbeans.modules.dbschema.TableElement getDeclaringTable()
meth public abstract org.netbeans.modules.dbschema.TableElement getReferencedTable()
meth public abstract void setDeclaringTable(org.netbeans.modules.dbschema.TableElement)
meth public abstract void setKeyName(java.lang.String) throws java.lang.Exception

CLSS public org.netbeans.modules.dbschema.SchemaElement
cons public init()
cons public init(org.netbeans.modules.dbschema.SchemaElement$Impl)
fld protected static java.util.Map<java.lang.String,org.netbeans.modules.dbschema.SchemaElement> schemaCache
fld public final static int CURRENT_VERSION_NO = 2
fld public final static int STATUS_ERROR = 1
fld public final static int STATUS_NOT = 0
fld public final static int STATUS_OK = 3
fld public final static int STATUS_PARTIAL = 2
innr public abstract interface static Impl
intf org.openide.nodes.Node$Cookie
meth protected static org.netbeans.modules.dbschema.SchemaElement getLastSchema()
meth protected static void setLastSchema(org.netbeans.modules.dbschema.SchemaElement)
meth public boolean isCompatibleVersion()
meth public int getStatus()
meth public int getVersionNo()
meth public java.lang.String getDatabaseProductName()
meth public java.lang.String getDatabaseProductVersion()
meth public java.lang.String getDriver()
meth public java.lang.String getDriverName()
meth public java.lang.String getDriverVersion()
meth public java.lang.String getUrl()
meth public java.lang.String getUsername()
meth public org.netbeans.modules.dbschema.DBIdentifier getCatalog()
meth public org.netbeans.modules.dbschema.DBIdentifier getSchema()
meth public org.netbeans.modules.dbschema.TableElement getTable(org.netbeans.modules.dbschema.DBIdentifier)
meth public org.netbeans.modules.dbschema.TableElement[] getTables()
meth public static org.netbeans.modules.dbschema.SchemaElement forName(java.lang.String)
meth public static org.netbeans.modules.dbschema.SchemaElement forName(java.lang.String,java.lang.Object)
meth public static void addToCache(org.netbeans.modules.dbschema.SchemaElement)
meth public static void removeFromCache(java.lang.String)
meth public void addTable(org.netbeans.modules.dbschema.TableElement) throws org.netbeans.modules.dbschema.DBException
meth public void addTables(org.netbeans.modules.dbschema.TableElement[]) throws org.netbeans.modules.dbschema.DBException
meth public void removeTable(org.netbeans.modules.dbschema.TableElement) throws org.netbeans.modules.dbschema.DBException
meth public void removeTables(org.netbeans.modules.dbschema.TableElement[]) throws org.netbeans.modules.dbschema.DBException
meth public void save(java.io.OutputStream)
meth public void save(java.lang.String)
meth public void setCatalog(org.netbeans.modules.dbschema.DBIdentifier) throws org.netbeans.modules.dbschema.DBException
meth public void setDatabaseProductName(java.lang.String) throws org.netbeans.modules.dbschema.DBException
meth public void setDatabaseProductVersion(java.lang.String) throws org.netbeans.modules.dbschema.DBException
meth public void setDriver(java.lang.String)
meth public void setDriverName(java.lang.String) throws org.netbeans.modules.dbschema.DBException
meth public void setDriverVersion(java.lang.String) throws org.netbeans.modules.dbschema.DBException
meth public void setSchema(org.netbeans.modules.dbschema.DBIdentifier) throws org.netbeans.modules.dbschema.DBException
meth public void setTables(org.netbeans.modules.dbschema.TableElement[]) throws org.netbeans.modules.dbschema.DBException
meth public void setUrl(java.lang.String) throws org.netbeans.modules.dbschema.DBException
meth public void setUsername(java.lang.String) throws org.netbeans.modules.dbschema.DBException
meth public void setVersionNo(int)
supr org.netbeans.modules.dbschema.DBElement
hfds lastSchema,versionNo
hcls Memory

CLSS public abstract interface static org.netbeans.modules.dbschema.SchemaElement$Impl
 outer org.netbeans.modules.dbschema.SchemaElement
intf org.netbeans.modules.dbschema.DBElement$Impl
meth public abstract int getStatus()
meth public abstract java.lang.String getDatabaseProductName()
meth public abstract java.lang.String getDatabaseProductVersion()
meth public abstract java.lang.String getDriver()
meth public abstract java.lang.String getDriverName()
meth public abstract java.lang.String getDriverVersion()
meth public abstract java.lang.String getUrl()
meth public abstract java.lang.String getUsername()
meth public abstract org.netbeans.modules.dbschema.DBIdentifier getCatalog()
meth public abstract org.netbeans.modules.dbschema.DBIdentifier getSchema()
meth public abstract org.netbeans.modules.dbschema.TableElement getTable(org.netbeans.modules.dbschema.DBIdentifier)
meth public abstract org.netbeans.modules.dbschema.TableElement[] getTables()
meth public abstract void changeTables(org.netbeans.modules.dbschema.TableElement[],int) throws org.netbeans.modules.dbschema.DBException
meth public abstract void setCatalog(org.netbeans.modules.dbschema.DBIdentifier) throws org.netbeans.modules.dbschema.DBException
meth public abstract void setDatabaseProductName(java.lang.String) throws org.netbeans.modules.dbschema.DBException
meth public abstract void setDatabaseProductVersion(java.lang.String) throws org.netbeans.modules.dbschema.DBException
meth public abstract void setDriver(java.lang.String)
meth public abstract void setDriverName(java.lang.String) throws org.netbeans.modules.dbschema.DBException
meth public abstract void setDriverVersion(java.lang.String) throws org.netbeans.modules.dbschema.DBException
meth public abstract void setSchema(org.netbeans.modules.dbschema.DBIdentifier) throws org.netbeans.modules.dbschema.DBException
meth public abstract void setUrl(java.lang.String) throws org.netbeans.modules.dbschema.DBException
meth public abstract void setUsername(java.lang.String) throws org.netbeans.modules.dbschema.DBException

CLSS public org.netbeans.modules.dbschema.SchemaElementUtil
cons public init()
meth public static org.netbeans.modules.dbschema.SchemaElement forName(java.lang.String,java.lang.Object)
meth public static org.netbeans.modules.dbschema.SchemaElement forName(org.openide.filesystems.FileObject)
supr java.lang.Object
hfds schemaFO

CLSS public final org.netbeans.modules.dbschema.TableElement
cons public init()
cons public init(org.netbeans.modules.dbschema.TableElement$Impl,org.netbeans.modules.dbschema.SchemaElement)
fld public final static boolean TABLE = true
fld public final static boolean VIEW = false
innr public abstract interface static Impl
intf org.netbeans.modules.dbschema.ColumnElementHolder
intf org.netbeans.modules.dbschema.ColumnPairElementHolder
meth public boolean isTable()
meth public boolean isTableOrView()
meth public boolean isView()
meth public final org.netbeans.modules.dbschema.SchemaElement getDeclaringSchema()
meth public final void setDeclaringSchema(org.netbeans.modules.dbschema.SchemaElement)
meth public java.lang.String toString()
meth public org.netbeans.modules.dbschema.ColumnElement getColumn(org.netbeans.modules.dbschema.DBIdentifier)
meth public org.netbeans.modules.dbschema.ColumnElement[] getColumns()
meth public org.netbeans.modules.dbschema.ColumnPairElement getColumnPair(org.netbeans.modules.dbschema.DBIdentifier)
meth public org.netbeans.modules.dbschema.ColumnPairElement[] getColumnPairs()
meth public org.netbeans.modules.dbschema.DBMemberElement getMember(org.netbeans.modules.dbschema.DBIdentifier)
meth public org.netbeans.modules.dbschema.ForeignKeyElement getForeignKey(org.netbeans.modules.dbschema.DBIdentifier)
meth public org.netbeans.modules.dbschema.ForeignKeyElement[] getForeignKeys()
meth public org.netbeans.modules.dbschema.IndexElement getIndex(org.netbeans.modules.dbschema.DBIdentifier)
meth public org.netbeans.modules.dbschema.IndexElement[] getIndexes()
meth public org.netbeans.modules.dbschema.KeyElement getKey(org.netbeans.modules.dbschema.DBIdentifier)
meth public org.netbeans.modules.dbschema.KeyElement[] getKeys()
meth public org.netbeans.modules.dbschema.UniqueKeyElement getPrimaryKey()
meth public org.netbeans.modules.dbschema.UniqueKeyElement getUniqueKey(org.netbeans.modules.dbschema.DBIdentifier)
meth public org.netbeans.modules.dbschema.UniqueKeyElement[] getUniqueKeys()
meth public static org.netbeans.modules.dbschema.TableElement forName(java.lang.String)
meth public static org.netbeans.modules.dbschema.TableElement forName(java.lang.String,org.netbeans.modules.dbschema.SchemaElement)
meth public void addColumn(org.netbeans.modules.dbschema.ColumnElement) throws org.netbeans.modules.dbschema.DBException
meth public void addColumnPair(org.netbeans.modules.dbschema.ColumnPairElement) throws org.netbeans.modules.dbschema.DBException
meth public void addColumnPairs(org.netbeans.modules.dbschema.ColumnPairElement[]) throws org.netbeans.modules.dbschema.DBException
meth public void addColumns(org.netbeans.modules.dbschema.ColumnElement[]) throws org.netbeans.modules.dbschema.DBException
meth public void addIndex(org.netbeans.modules.dbschema.IndexElement) throws org.netbeans.modules.dbschema.DBException
meth public void addIndexes(org.netbeans.modules.dbschema.IndexElement[]) throws org.netbeans.modules.dbschema.DBException
meth public void addKey(org.netbeans.modules.dbschema.KeyElement) throws org.netbeans.modules.dbschema.DBException
meth public void addKeys(org.netbeans.modules.dbschema.KeyElement[]) throws org.netbeans.modules.dbschema.DBException
meth public void removeColumn(org.netbeans.modules.dbschema.ColumnElement) throws org.netbeans.modules.dbschema.DBException
meth public void removeColumnPair(org.netbeans.modules.dbschema.ColumnPairElement) throws org.netbeans.modules.dbschema.DBException
meth public void removeColumnPairs(org.netbeans.modules.dbschema.ColumnPairElement[]) throws org.netbeans.modules.dbschema.DBException
meth public void removeColumns(org.netbeans.modules.dbschema.ColumnElement[]) throws org.netbeans.modules.dbschema.DBException
meth public void removeIndex(org.netbeans.modules.dbschema.IndexElement) throws org.netbeans.modules.dbschema.DBException
meth public void removeIndexes(org.netbeans.modules.dbschema.IndexElement[]) throws org.netbeans.modules.dbschema.DBException
meth public void removeKey(org.netbeans.modules.dbschema.KeyElement) throws org.netbeans.modules.dbschema.DBException
meth public void removeKeys(org.netbeans.modules.dbschema.KeyElement[]) throws org.netbeans.modules.dbschema.DBException
meth public void setColumnPairs(org.netbeans.modules.dbschema.ColumnPairElement[]) throws org.netbeans.modules.dbschema.DBException
meth public void setColumns(org.netbeans.modules.dbschema.ColumnElement[]) throws org.netbeans.modules.dbschema.DBException
meth public void setIndexes(org.netbeans.modules.dbschema.IndexElement[]) throws org.netbeans.modules.dbschema.DBException
meth public void setKeys(org.netbeans.modules.dbschema.KeyElement[]) throws org.netbeans.modules.dbschema.DBException
meth public void setTableOrView(boolean) throws org.netbeans.modules.dbschema.DBException
supr org.netbeans.modules.dbschema.DBElement
hfds declaringSchema
hcls Memory

CLSS public abstract interface static org.netbeans.modules.dbschema.TableElement$Impl
 outer org.netbeans.modules.dbschema.TableElement
intf org.netbeans.modules.dbschema.DBElement$Impl
meth public abstract boolean isTableOrView()
meth public abstract org.netbeans.modules.dbschema.ColumnElement getColumn(org.netbeans.modules.dbschema.DBIdentifier)
meth public abstract org.netbeans.modules.dbschema.ColumnElement[] getColumns()
meth public abstract org.netbeans.modules.dbschema.ColumnPairElement getColumnPair(org.netbeans.modules.dbschema.DBIdentifier)
meth public abstract org.netbeans.modules.dbschema.ColumnPairElement[] getColumnPairs()
meth public abstract org.netbeans.modules.dbschema.IndexElement getIndex(org.netbeans.modules.dbschema.DBIdentifier)
meth public abstract org.netbeans.modules.dbschema.IndexElement[] getIndexes()
meth public abstract org.netbeans.modules.dbschema.KeyElement getKey(org.netbeans.modules.dbschema.DBIdentifier)
meth public abstract org.netbeans.modules.dbschema.KeyElement[] getKeys()
meth public abstract void changeColumnPairs(org.netbeans.modules.dbschema.ColumnPairElement[],int) throws org.netbeans.modules.dbschema.DBException
meth public abstract void changeColumns(org.netbeans.modules.dbschema.ColumnElement[],int) throws org.netbeans.modules.dbschema.DBException
meth public abstract void changeIndexes(org.netbeans.modules.dbschema.IndexElement[],int) throws org.netbeans.modules.dbschema.DBException
meth public abstract void changeKeys(org.netbeans.modules.dbschema.KeyElement[],int) throws org.netbeans.modules.dbschema.DBException
meth public abstract void setTableOrView(boolean) throws org.netbeans.modules.dbschema.DBException

CLSS public final org.netbeans.modules.dbschema.UniqueKeyElement
cons public init()
cons public init(org.netbeans.modules.dbschema.UniqueKeyElement$Impl,org.netbeans.modules.dbschema.TableElement,org.netbeans.modules.dbschema.IndexElement)
innr public abstract interface static Impl
meth public boolean isPrimaryKey()
meth public org.netbeans.modules.dbschema.IndexElement getAssociatedIndex()
meth public void setAssociatedIndex(org.netbeans.modules.dbschema.IndexElement) throws org.netbeans.modules.dbschema.DBException
meth public void setPrimaryKey(boolean) throws org.netbeans.modules.dbschema.DBException
supr org.netbeans.modules.dbschema.KeyElement
hfds _associatedIndex
hcls Memory

CLSS public abstract interface static org.netbeans.modules.dbschema.UniqueKeyElement$Impl
 outer org.netbeans.modules.dbschema.UniqueKeyElement
intf org.netbeans.modules.dbschema.KeyElement$Impl
meth public abstract boolean isPrimaryKey()
meth public abstract void setPrimaryKey(boolean) throws org.netbeans.modules.dbschema.DBException

CLSS public org.netbeans.modules.dbschema.jdbcimpl.CaptureSchemaAction
cons public init()
meth protected java.lang.String iconResource()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void performAction()
supr org.openide.util.actions.CallableSystemAction
hfds bundle

CLSS public org.netbeans.modules.dbschema.jdbcimpl.ColumnElementImpl
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String,boolean,java.lang.String,java.lang.String)
fld protected boolean _isAutoIncrement
fld protected boolean _isNullable
fld protected int _type
fld protected java.lang.Integer _length
fld protected java.lang.Integer _precision
fld protected java.lang.Integer _scale
intf org.netbeans.modules.dbschema.ColumnElement$Impl
meth public boolean isAutoIncrement()
meth public boolean isNullable()
meth public int getType()
meth public java.lang.Integer getLength()
meth public java.lang.Integer getPrecision()
meth public java.lang.Integer getScale()
meth public void setLength(java.lang.Integer) throws org.netbeans.modules.dbschema.DBException
meth public void setNullable(boolean) throws org.netbeans.modules.dbschema.DBException
meth public void setPrecision(java.lang.Integer) throws org.netbeans.modules.dbschema.DBException
meth public void setScale(java.lang.Integer) throws org.netbeans.modules.dbschema.DBException
meth public void setType(int) throws org.netbeans.modules.dbschema.DBException
supr org.netbeans.modules.dbschema.jdbcimpl.DBMemberElementImpl

CLSS public org.netbeans.modules.dbschema.jdbcimpl.ColumnPairElementImpl
cons public init()
cons public init(java.lang.String)
intf org.netbeans.modules.dbschema.ColumnPairElement$Impl
supr org.netbeans.modules.dbschema.jdbcimpl.DBMemberElementImpl

CLSS public org.netbeans.modules.dbschema.jdbcimpl.ConnectionProvider
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws java.lang.ClassNotFoundException,java.sql.SQLException
cons public init(java.sql.Connection,java.lang.String) throws java.sql.SQLException
meth public java.lang.String getDriver()
meth public java.lang.String getSchema()
meth public java.sql.Connection getConnection()
meth public java.sql.DatabaseMetaData getDatabaseMetaData() throws java.sql.SQLException
meth public void closeConnection()
meth public void setSchema(java.lang.String)
supr java.lang.Object
hfds bundle,con,dmd,driver,password,schema,url,username

CLSS public org.netbeans.modules.dbschema.jdbcimpl.DBElementsCollection
cons public init()
fld protected static java.util.Set<java.lang.String> instances
intf org.netbeans.modules.dbschema.DBElementProperties
meth public java.lang.Object[] getTemplate()
meth public org.netbeans.modules.dbschema.DBElement find(org.netbeans.modules.dbschema.DBIdentifier)
meth public org.netbeans.modules.dbschema.DBElement[] getElements()
meth public void changeElements(java.util.List,int)
meth public void changeElements(org.netbeans.modules.dbschema.DBElement[],int)
meth public void setElements(org.netbeans.modules.dbschema.DBElement[])
meth public void setTemplate(java.lang.Object[])
supr java.lang.Object
hfds _elms,_template,owner

CLSS public abstract org.netbeans.modules.dbschema.jdbcimpl.DBMemberElementImpl
cons public init()
cons public init(java.lang.String)
fld protected org.netbeans.modules.dbschema.DBIdentifier _name
intf org.netbeans.modules.dbschema.DBElement$Impl
intf org.netbeans.modules.dbschema.DBElementProperties
intf org.netbeans.modules.dbschema.DBMemberElement$Impl
meth protected boolean comp(java.lang.Object,java.lang.Object)
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public org.netbeans.modules.dbschema.DBIdentifier getName()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void attachToElement(org.netbeans.modules.dbschema.DBElement)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setName(org.netbeans.modules.dbschema.DBIdentifier) throws org.netbeans.modules.dbschema.DBException
supr java.lang.Object

CLSS public org.netbeans.modules.dbschema.jdbcimpl.DBschemaDataLoader
cons public init()
meth protected java.lang.String actionsContext()
meth protected java.lang.String defaultDisplayName()
meth protected org.openide.loaders.MultiDataObject createMultiObject(org.openide.filesystems.FileObject) throws java.io.IOException
meth protected void initialize()
supr org.openide.loaders.UniFileLoader
hfds serialVersionUID

CLSS public org.netbeans.modules.dbschema.jdbcimpl.DBschemaDataLoaderBeanInfo
cons public init()
meth public java.awt.Image getIcon(int)
meth public java.beans.BeanInfo[] getAdditionalBeanInfo()
supr java.beans.SimpleBeanInfo

CLSS public org.netbeans.modules.dbschema.jdbcimpl.DBschemaDataNode
cons public init(org.netbeans.modules.dbschema.jdbcimpl.DBschemaDataObject)
cons public init(org.netbeans.modules.dbschema.jdbcimpl.DBschemaDataObject,org.openide.nodes.Children)
meth public javax.swing.Action getPreferredAction()
supr org.openide.loaders.DataNode

CLSS public org.netbeans.modules.dbschema.jdbcimpl.DBschemaDataObject
cons public init(org.openide.filesystems.FileObject,org.netbeans.modules.dbschema.jdbcimpl.DBschemaDataLoader) throws org.openide.loaders.DataObjectExistsException
fld protected org.netbeans.modules.dbschema.SchemaElement schemaElement
meth protected org.openide.nodes.Node createNodeDelegate()
meth public org.netbeans.modules.dbschema.SchemaElement getSchema()
meth public org.netbeans.modules.dbschema.jdbcimpl.SchemaElementImpl getSchemaElementImpl()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public org.openide.util.Lookup getLookup()
meth public void setSchema(org.netbeans.modules.dbschema.SchemaElement)
meth public void setSchemaElementImpl(org.netbeans.modules.dbschema.jdbcimpl.SchemaElementImpl)
supr org.openide.loaders.MultiDataObject
hfds schemaElementImpl

CLSS public org.netbeans.modules.dbschema.jdbcimpl.DDLBridge
cons public init(java.sql.Connection,java.lang.String,java.sql.DatabaseMetaData)
meth public org.netbeans.lib.ddl.impl.DriverSpecification getDriverSpecification()
supr java.lang.Object
hfds drvSpec

CLSS public org.netbeans.modules.dbschema.jdbcimpl.ForeignKeyElementImpl
cons public init()
cons public init(org.netbeans.modules.dbschema.jdbcimpl.TableElementImpl,java.lang.String)
intf org.netbeans.modules.dbschema.ForeignKeyElement$Impl
meth protected org.netbeans.modules.dbschema.jdbcimpl.DBElementsCollection initializeCollection()
meth public org.netbeans.modules.dbschema.ColumnElement getColumn(org.netbeans.modules.dbschema.DBIdentifier)
meth public org.netbeans.modules.dbschema.ColumnElement[] getColumns()
meth public org.netbeans.modules.dbschema.ColumnPairElement getColumnPair(org.netbeans.modules.dbschema.DBIdentifier)
meth public org.netbeans.modules.dbschema.ColumnPairElement[] getColumnPairs()
meth public void changeColumnPairs(org.netbeans.modules.dbschema.ColumnPairElement[],int) throws org.netbeans.modules.dbschema.DBException
supr org.netbeans.modules.dbschema.jdbcimpl.KeyElementImpl
hfds tei

CLSS public org.netbeans.modules.dbschema.jdbcimpl.IndexElementImpl
cons public init()
cons public init(org.netbeans.modules.dbschema.jdbcimpl.TableElementImpl,java.lang.String,boolean)
intf org.netbeans.modules.dbschema.IndexElement$Impl
meth protected void initColumns(java.util.LinkedList)
meth public boolean isUnique()
meth public org.netbeans.modules.dbschema.ColumnElement getColumn(org.netbeans.modules.dbschema.DBIdentifier)
meth public org.netbeans.modules.dbschema.ColumnElement[] getColumns()
meth public org.netbeans.modules.dbschema.jdbcimpl.DBElementsCollection getColumnCollection()
meth public void changeColumns(org.netbeans.modules.dbschema.ColumnElement[],int) throws org.netbeans.modules.dbschema.DBException
meth public void setColumnCollection(org.netbeans.modules.dbschema.jdbcimpl.DBElementsCollection)
meth public void setUnique(boolean) throws org.netbeans.modules.dbschema.DBException
supr org.netbeans.modules.dbschema.jdbcimpl.DBMemberElementImpl
hfds _unique,columns,tei

CLSS public abstract org.netbeans.modules.dbschema.jdbcimpl.KeyElementImpl
cons public init()
cons public init(java.lang.String)
intf org.netbeans.modules.dbschema.KeyElement$Impl
meth protected org.netbeans.modules.dbschema.jdbcimpl.DBElementsCollection initializeCollection()
meth public org.netbeans.modules.dbschema.ColumnElement getColumn(org.netbeans.modules.dbschema.DBIdentifier)
meth public org.netbeans.modules.dbschema.ColumnElement[] getColumns()
meth public org.netbeans.modules.dbschema.jdbcimpl.DBElementsCollection getColumnCollection()
meth public void changeColumns(org.netbeans.modules.dbschema.ColumnElement[],int) throws org.netbeans.modules.dbschema.DBException
meth public void setColumnCollection(org.netbeans.modules.dbschema.jdbcimpl.DBElementsCollection)
supr org.netbeans.modules.dbschema.jdbcimpl.DBMemberElementImpl
hfds columns

CLSS public org.netbeans.modules.dbschema.jdbcimpl.MetaDataUtil
cons public init()
meth public static boolean areViewsSupported(java.lang.String)
supr java.lang.Object

CLSS public org.netbeans.modules.dbschema.jdbcimpl.RecaptureSchemaAction
cons public init()
meth protected boolean asynchronous()
meth protected boolean enable(org.openide.nodes.Node[])
meth protected int mode()
meth protected java.lang.Class[] cookieClasses()
meth protected java.lang.String iconResource()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void performAction(org.openide.nodes.Node[])
supr org.openide.util.actions.CookieAction

CLSS public org.netbeans.modules.dbschema.jdbcimpl.SchemaElementImpl
cons public init()
cons public init(org.netbeans.modules.dbschema.jdbcimpl.ConnectionProvider)
fld protected org.netbeans.modules.dbschema.DBIdentifier _name
fld public java.beans.PropertyChangeSupport propertySupport
intf org.netbeans.modules.dbschema.DBElement$Impl
intf org.netbeans.modules.dbschema.DBElementProperties
intf org.netbeans.modules.dbschema.SchemaElement$Impl
meth protected boolean comp(java.lang.Object,java.lang.Object)
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public boolean isStop()
meth public int getStatus()
meth public java.lang.String getDatabaseProductName()
meth public java.lang.String getDatabaseProductVersion()
meth public java.lang.String getDriver()
meth public java.lang.String getDriverName()
meth public java.lang.String getDriverVersion()
meth public java.lang.String getUrl()
meth public java.lang.String getUsername()
meth public org.netbeans.modules.dbschema.DBIdentifier getCatalog()
meth public org.netbeans.modules.dbschema.DBIdentifier getName()
meth public org.netbeans.modules.dbschema.DBIdentifier getSchema()
meth public org.netbeans.modules.dbschema.TableElement getTable(org.netbeans.modules.dbschema.DBIdentifier)
meth public org.netbeans.modules.dbschema.TableElement[] getTables()
meth public org.netbeans.modules.dbschema.jdbcimpl.DBElementsCollection getTableCollection()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void attachToElement(org.netbeans.modules.dbschema.DBElement)
meth public void changeTables(org.netbeans.modules.dbschema.TableElement[],int) throws org.netbeans.modules.dbschema.DBException
meth public void initTables(org.netbeans.modules.dbschema.jdbcimpl.ConnectionProvider)
meth public void initTables(org.netbeans.modules.dbschema.jdbcimpl.ConnectionProvider,java.util.LinkedList,java.util.LinkedList)
meth public void initTables(org.netbeans.modules.dbschema.jdbcimpl.ConnectionProvider,java.util.LinkedList,java.util.LinkedList,boolean)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setCatalog(org.netbeans.modules.dbschema.DBIdentifier) throws org.netbeans.modules.dbschema.DBException
meth public void setDatabaseProductName(java.lang.String) throws org.netbeans.modules.dbschema.DBException
meth public void setDatabaseProductVersion(java.lang.String) throws org.netbeans.modules.dbschema.DBException
meth public void setDriver(java.lang.String)
meth public void setDriverName(java.lang.String) throws org.netbeans.modules.dbschema.DBException
meth public void setDriverVersion(java.lang.String) throws org.netbeans.modules.dbschema.DBException
meth public void setName(org.netbeans.modules.dbschema.DBIdentifier) throws org.netbeans.modules.dbschema.DBException
meth public void setSchema(org.netbeans.modules.dbschema.DBIdentifier) throws org.netbeans.modules.dbschema.DBException
meth public void setStop(boolean)
meth public void setTableCollection(org.netbeans.modules.dbschema.jdbcimpl.DBElementsCollection)
meth public void setUrl(java.lang.String) throws org.netbeans.modules.dbschema.DBException
meth public void setUsername(java.lang.String) throws org.netbeans.modules.dbschema.DBException
supr java.lang.Object
hfds LOGGER,_catalog,_databaseProductName,_databaseProductVersion,_driver,_driverName,_driverVersion,_schema,_url,_username,catalog,dmd,progress,stop,tables

CLSS public org.netbeans.modules.dbschema.jdbcimpl.TableElementImpl
cons public init()
cons public init(java.lang.String)
fld protected org.netbeans.modules.dbschema.DBIdentifier _name
intf org.netbeans.modules.dbschema.DBElement$Impl
intf org.netbeans.modules.dbschema.DBElementProperties
intf org.netbeans.modules.dbschema.TableElement$Impl
meth protected boolean comp(java.lang.Object,java.lang.Object)
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void initColumns(org.netbeans.modules.dbschema.jdbcimpl.ConnectionProvider)
meth protected void initIndexes(org.netbeans.modules.dbschema.jdbcimpl.ConnectionProvider)
meth protected void initIndexes(org.netbeans.modules.dbschema.jdbcimpl.ConnectionProvider,java.lang.String)
meth protected void initKeys(org.netbeans.modules.dbschema.jdbcimpl.ConnectionProvider)
meth protected void initKeys(org.netbeans.modules.dbschema.jdbcimpl.ConnectionProvider,int)
meth protected void initKeys(org.netbeans.modules.dbschema.jdbcimpl.ConnectionProvider,int,java.lang.String)
meth public boolean isTableOrView()
meth public org.netbeans.modules.dbschema.ColumnElement getColumn(org.netbeans.modules.dbschema.DBIdentifier)
meth public org.netbeans.modules.dbschema.ColumnElement[] getColumns()
meth public org.netbeans.modules.dbschema.ColumnPairElement getColumnPair(org.netbeans.modules.dbschema.DBIdentifier)
meth public org.netbeans.modules.dbschema.ColumnPairElement[] getColumnPairs()
meth public org.netbeans.modules.dbschema.DBIdentifier getName()
meth public org.netbeans.modules.dbschema.IndexElement getIndex(org.netbeans.modules.dbschema.DBIdentifier)
meth public org.netbeans.modules.dbschema.IndexElement[] getIndexes()
meth public org.netbeans.modules.dbschema.KeyElement getKey(org.netbeans.modules.dbschema.DBIdentifier)
meth public org.netbeans.modules.dbschema.KeyElement[] getKeys()
meth public org.netbeans.modules.dbschema.jdbcimpl.DBElementsCollection getColumnCollection()
meth public org.netbeans.modules.dbschema.jdbcimpl.DBElementsCollection getIndexCollection()
meth public org.netbeans.modules.dbschema.jdbcimpl.DBElementsCollection getKeyCollection()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void attachToElement(org.netbeans.modules.dbschema.DBElement)
meth public void changeColumnPairs(org.netbeans.modules.dbschema.ColumnPairElement[],int) throws org.netbeans.modules.dbschema.DBException
meth public void changeColumns(org.netbeans.modules.dbschema.ColumnElement[],int) throws org.netbeans.modules.dbschema.DBException
meth public void changeIndexes(org.netbeans.modules.dbschema.IndexElement[],int) throws org.netbeans.modules.dbschema.DBException
meth public void changeKeys(org.netbeans.modules.dbschema.KeyElement[],int) throws org.netbeans.modules.dbschema.DBException
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setColumnCollection(org.netbeans.modules.dbschema.jdbcimpl.DBElementsCollection)
meth public void setIndexCollection(org.netbeans.modules.dbschema.jdbcimpl.DBElementsCollection)
meth public void setKeyCollection(org.netbeans.modules.dbschema.jdbcimpl.DBElementsCollection)
meth public void setName(org.netbeans.modules.dbschema.DBIdentifier) throws org.netbeans.modules.dbschema.DBException
meth public void setTableOrView(boolean) throws org.netbeans.modules.dbschema.DBException
supr java.lang.Object
hfds LOGGER,columnPairs,columns,indexes,isTable,keys,table

CLSS public org.netbeans.modules.dbschema.jdbcimpl.UniqueKeyElementImpl
cons public init()
cons public init(java.lang.String,boolean)
intf org.netbeans.modules.dbschema.UniqueKeyElement$Impl
meth public boolean isPrimaryKey()
meth public void setPrimaryKey(boolean) throws org.netbeans.modules.dbschema.DBException
supr org.netbeans.modules.dbschema.jdbcimpl.KeyElementImpl
hfds _primary

CLSS public org.netbeans.modules.dbschema.jdbcimpl.ViewDependency
cons public init(org.netbeans.modules.dbschema.jdbcimpl.ConnectionProvider,java.lang.String,java.lang.String) throws java.sql.SQLException
meth public java.util.LinkedList getColumns()
meth public java.util.LinkedList getTables()
meth public void constructPK()
supr java.lang.Object
hfds LOG,columns,con,dmd,tables,user,view

CLSS public org.netbeans.modules.dbschema.jdbcimpl.wizard.CaptureSchema
cons public init(org.netbeans.modules.dbschema.jdbcimpl.wizard.DBSchemaWizardData)
meth protected void start()
supr java.lang.Object
hfds bundle,data,defaultName

CLSS public org.netbeans.modules.dbschema.jdbcimpl.wizard.ChooseConnectionPanel
cons public init(java.lang.String)
meth public static org.netbeans.api.db.explorer.DatabaseConnection showChooseConnectionDialog(java.lang.String)
supr javax.swing.JPanel
hfds conn,connCombo,desc,jScrollPane1,jTextArea1,url

CLSS public org.netbeans.modules.dbschema.jdbcimpl.wizard.DBSchemaConnectionPanel
cons public init(org.netbeans.modules.dbschema.jdbcimpl.wizard.DBSchemaWizardData,java.util.ArrayList)
intf javax.swing.event.ListDataListener
meth public boolean isInputValid()
meth public void contentsChanged(javax.swing.event.ListDataEvent)
meth public void fireChange(java.lang.Object)
meth public void initData()
meth public void intervalAdded(javax.swing.event.ListDataEvent)
meth public void intervalRemoved(javax.swing.event.ListDataEvent)
supr javax.swing.JPanel
hfds bundle,data,dbNode,descriptionTextArea,drvNodes,existingConnComboBox,list,serialVersionUID

CLSS public final org.netbeans.modules.dbschema.jdbcimpl.wizard.DBSchemaConnectionWizardPanel
cons public init(org.netbeans.modules.dbschema.jdbcimpl.wizard.DBSchemaWizardData)
meth public boolean isValid()
meth public java.awt.Component getComponent()
meth public org.openide.util.HelpCtx getHelp()
supr org.netbeans.modules.dbschema.jdbcimpl.wizard.DBSchemaPanel
hfds panelUI

CLSS public abstract org.netbeans.modules.dbschema.jdbcimpl.wizard.DBSchemaPanel
cons public init()
fld protected java.util.ArrayList list
fld protected org.netbeans.modules.dbschema.jdbcimpl.wizard.DBSchemaWizardData data
intf org.openide.WizardDescriptor$Panel
meth public abstract org.openide.util.HelpCtx getHelp()
meth public java.awt.Dimension getPreferredSize()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void readSettings(java.lang.Object)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void storeSettings(java.lang.Object)
supr java.lang.Object
hfds DEFAULT_HEIGHT,DEFAULT_WIDTH,bundle

CLSS public org.netbeans.modules.dbschema.jdbcimpl.wizard.DBSchemaTablesPanel
cons public init(org.netbeans.modules.dbschema.jdbcimpl.wizard.DBSchemaWizardData,java.util.ArrayList)
intf javax.swing.event.ListDataListener
meth protected boolean init()
meth public boolean isInputValid()
meth public void contentsChanged(javax.swing.event.ListDataEvent)
meth public void fireChange(java.lang.Object)
meth public void intervalAdded(javax.swing.event.ListDataEvent)
meth public void intervalRemoved(javax.swing.event.ListDataEvent)
meth public void uninit()
supr javax.swing.JPanel
hfds LOG,LOGGER,bundle,conn,cp,data,dbconnOld,driver,jButtonAdd,jButtonAddAll,jButtonRemove,jButtonRemoveAll,jLabelAvailableTables,jLabelNote,jLabelSelectedTables,jListAvailableTables,jListSelectedTables,jPanelButtons,jScrollPaneAvailableTables,jScrollPaneSelectedTables,list,schema,tables,tablesCount,views
hcls FormListener,Handler,Parameters

CLSS public final org.netbeans.modules.dbschema.jdbcimpl.wizard.DBSchemaTablesWizardPanel
cons public init(org.netbeans.modules.dbschema.jdbcimpl.wizard.DBSchemaWizardData)
meth public boolean isValid()
meth public java.awt.Component getComponent()
meth public org.openide.util.HelpCtx getHelp()
supr org.netbeans.modules.dbschema.jdbcimpl.wizard.DBSchemaPanel
hfds panelUI

CLSS public org.netbeans.modules.dbschema.jdbcimpl.wizard.DBSchemaTargetPanel
cons public init()
meth public boolean isValid()
meth public java.awt.Component getComponent()
meth public org.netbeans.modules.dbschema.jdbcimpl.wizard.DBSchemaTargetPanel getPanel()
meth public org.openide.util.HelpCtx getHelp()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void readSettings(java.lang.Object)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void setPanel(org.openide.WizardDescriptor$Panel)
meth public void storeSettings(java.lang.Object)
supr org.netbeans.modules.dbschema.jdbcimpl.wizard.DBSchemaPanel
hfds panel

CLSS public org.netbeans.modules.dbschema.jdbcimpl.wizard.DBSchemaWizardData
cons public init()
meth public boolean isAllTables()
meth public boolean isConnected()
meth public boolean isExistingConn()
meth public java.beans.PropertyChangeSupport getPropertySupport()
meth public java.lang.String getDriver()
meth public java.lang.String getName()
meth public java.lang.String getSchema()
meth public java.util.LinkedList getTables()
meth public java.util.LinkedList getViews()
meth public java.util.Vector getSchemas()
meth public org.netbeans.api.db.explorer.DatabaseConnection getDatabaseConnection()
meth public org.netbeans.modules.dbschema.jdbcimpl.ConnectionProvider getConnectionProvider()
meth public org.openide.loaders.DataFolder getDestinationPackage()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setAllTables(boolean)
meth public void setConnected(boolean)
meth public void setConnectionProvider(org.netbeans.modules.dbschema.jdbcimpl.ConnectionProvider)
meth public void setDatabaseConnection(org.netbeans.api.db.explorer.DatabaseConnection)
meth public void setDestinationPackage(org.openide.loaders.DataFolder)
meth public void setDriver(java.lang.String)
meth public void setExistingConn(boolean)
meth public void setName(java.lang.String)
meth public void setSchema(java.lang.String)
meth public void setSchemas(java.util.Vector)
meth public void setTables(java.util.LinkedList)
meth public void setViews(java.util.LinkedList)
supr java.lang.Object
hfds all,connected,cp,dbconn,destinationPackage,driver,existingConn,name,propertySupport,schema,schemas,tables,views

CLSS public org.netbeans.modules.dbschema.jdbcimpl.wizard.DBSchemaWizardIterator
cons public init()
intf org.openide.loaders.TemplateWizard$Iterator
meth protected void initialize()
meth public boolean hasNext()
meth public boolean hasPrevious()
meth public java.lang.String name()
meth public java.util.Set instantiate(org.openide.loaders.TemplateWizard) throws java.io.IOException
meth public org.openide.WizardDescriptor$Panel current()
meth public static org.netbeans.modules.dbschema.jdbcimpl.wizard.DBSchemaWizardIterator singleton()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void initialize(org.openide.loaders.TemplateWizard)
meth public void nextPanel()
meth public void previousPanel()
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void uninitialize(org.openide.loaders.TemplateWizard)
supr java.lang.Object
hfds LOG,PANEL_COUNT,bundle,guiInitialized,instance,myData,panelIndex,panelNames,panels,serialVersionUID,wizardInstance

CLSS public org.netbeans.modules.dbschema.jdbcimpl.wizard.ProgressFrame
cons public init()
fld public java.beans.PropertyChangeSupport propertySupport
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void dispose()
meth public void finishProgress()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setMaximum(int)
meth public void setMessage(java.lang.String)
meth public void setValue(int)
supr javax.swing.JFrame
hfds bundle,finished,msgLabel,okButton,progressComponent,progressHandle,progressPanel,workunits

CLSS public org.netbeans.modules.dbschema.jdbcimpl.wizard.ProgressPanel
cons public init()
meth public java.awt.Dimension getPreferredSize()
meth public void close()
meth public void open(javax.swing.JComponent)
meth public void setText(java.lang.String)
supr javax.swing.JPanel
hfds dialog,holder,info

CLSS public org.netbeans.modules.dbschema.jdbcimpl.wizard.RecaptureSchema
cons public init(org.openide.nodes.Node)
meth public org.netbeans.modules.dbschema.jdbcimpl.ConnectionProvider createConnectionProvider(org.netbeans.modules.dbschema.jdbcimpl.wizard.DBSchemaWizardData,org.netbeans.modules.dbschema.SchemaElement) throws java.sql.SQLException
meth public void start() throws java.lang.ClassNotFoundException,java.sql.SQLException
supr java.lang.Object
hfds LOGGER,bundle,data,dbSchemaNode,debug
hcls ConnectionHandler

CLSS public org.netbeans.modules.dbschema.jdbcimpl.wizard.SortedListModel
cons public init()
cons public init(int)
cons public init(java.util.Collection)
fld public final static java.util.Comparator DEFAULT_COMPARATOR
meth public boolean contains(java.lang.Object)
meth public boolean isEmpty()
meth public boolean remove(int)
meth public int add(java.lang.Object)
meth public int getSize()
meth public int indexOf(java.lang.Object)
meth public int remove(java.lang.Object)
meth public java.lang.Object getElementAt(int)
meth public java.lang.Object[] toArray()
meth public java.lang.Object[] toArray(java.lang.Object[])
meth public java.lang.String toString()
meth public java.util.Comparator getComparator()
meth public void clear()
meth public void setComparator(java.util.Comparator)
supr javax.swing.AbstractListModel
hfds comp,elements

CLSS public org.netbeans.modules.dbschema.migration.archiver.MapClassName
cons public init()
meth public static java.lang.String getClassNameToken(java.lang.String)
meth public static java.lang.String getRealClassName(java.lang.String)
meth public static void main(java.lang.String[])
supr java.lang.Object
hfds CURRENTPREFIX,LEGACYPREFIX

CLSS public org.netbeans.modules.dbschema.migration.archiver.XMLInputStream
cons public init(java.io.InputStream)
cons public init(java.io.InputStream,java.lang.ClassLoader)
intf java.io.ObjectInput
meth public java.lang.Object readObject() throws java.io.IOException,java.lang.ClassNotFoundException
supr java.io.DataInputStream
hfds classLoader,inStream

CLSS public org.netbeans.modules.dbschema.migration.archiver.XMLOutputStream
cons public init(java.io.OutputStream)
intf java.io.ObjectOutput
meth public void close() throws java.io.IOException
meth public void writeObject(java.lang.Object) throws java.io.IOException
supr java.io.DataOutputStream
hfds outStream

CLSS public org.netbeans.modules.dbschema.migration.archiver.deserializer.BaseSpecificXMLDeserializer
cons public init()
cons public init(java.lang.ClassLoader)
fld protected java.lang.Class ParameterClass
fld protected java.lang.Class ParameterSetMethod
fld protected java.lang.Integer State
fld protected java.util.HashMap ActiveAliasHash
fld protected java.util.Vector ParameterArray
fld protected org.netbeans.modules.dbschema.migration.archiver.deserializer.XMLDeserializer MasterDeserializer
fld public final static java.lang.String WRONG_TAG = "Saw tag {1} when {2} was expected."
fld public java.util.Vector ParameterTypeArray
intf org.netbeans.modules.dbschema.migration.archiver.deserializer.SpecificXMLDeserializer
meth public boolean useExistingAttribute(org.xml.sax.AttributeList,java.lang.String,java.lang.Object) throws org.xml.sax.SAXException
meth public java.lang.Class findClass(java.lang.String) throws java.lang.ClassNotFoundException
meth public java.lang.Object popObject()
meth public java.lang.Object topObject() throws org.xml.sax.SAXException
meth public java.lang.String lookupAlias(java.lang.String)
meth public java.lang.String unescapeName(java.lang.String)
meth public void DumpStatus()
meth public void addActiveAlias(java.lang.String,java.lang.String)
meth public void freeResources()
meth public void popState()
meth public void pushObject(java.lang.Object)
meth public void pushState(int)
meth public void setMasterDeserializer(org.netbeans.modules.dbschema.migration.archiver.deserializer.XMLDeserializer)
meth public void unexpectedTag(java.lang.String,java.lang.String,boolean) throws org.xml.sax.SAXException
meth public void validateTag(java.lang.String,java.lang.String,boolean) throws org.xml.sax.SAXException
supr org.netbeans.modules.dbschema.migration.archiver.deserializer.BaseXMLDeserializer
hfds ObjectStack,StateStack,classLoader

CLSS public org.netbeans.modules.dbschema.migration.archiver.deserializer.BaseXMLDeserializer
cons public init()
fld protected java.lang.Object InitialObject
fld protected org.xml.sax.InputSource TheSource
fld protected org.xml.sax.Parser Parser
fld public java.lang.StringBuffer TheCharacters
fld public org.xml.sax.Locator TheLocator
intf org.netbeans.modules.dbschema.migration.archiver.deserializer.XMLDeserializer
intf org.xml.sax.DTDHandler
intf org.xml.sax.DocumentHandler
intf org.xml.sax.ErrorHandler
meth public int Begin() throws org.xml.sax.SAXException
meth public java.lang.Object XlateObject() throws java.io.IOException,org.xml.sax.SAXException
meth public java.lang.Object XlateObject(java.io.InputStream) throws java.io.IOException,org.xml.sax.SAXException
meth public java.lang.String getCharacters()
meth public void DumpStatus()
meth public void characters(char[],int,int) throws org.xml.sax.SAXException
meth public void commonErrorProcessor(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void endDocument() throws org.xml.sax.SAXException
meth public void endElement(java.lang.String) throws org.xml.sax.SAXException
meth public void error(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void fatalError(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void freeResources()
meth public void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public void notationDecl(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void setDocumentLocator(org.xml.sax.Locator)
meth public void setInitialObject(java.lang.Object)
meth public void setSource(org.xml.sax.InputSource)
meth public void startDocument() throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,org.xml.sax.AttributeList) throws org.xml.sax.SAXException
meth public void unparsedEntityDecl(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void warning(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
supr java.lang.Object

CLSS public org.netbeans.modules.dbschema.migration.archiver.deserializer.NewInstanceHelper
cons public init()
meth public static java.lang.Object newInstance(java.lang.String,java.lang.Object)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.dbschema.migration.archiver.deserializer.SpecificXMLDeserializer
meth public abstract void setMasterDeserializer(org.netbeans.modules.dbschema.migration.archiver.deserializer.XMLDeserializer)

CLSS public abstract interface org.netbeans.modules.dbschema.migration.archiver.deserializer.XMLDeserializer
meth public abstract void setInitialObject(java.lang.Object)

CLSS public org.netbeans.modules.dbschema.migration.archiver.deserializer.XMLGraphDeserializer
cons public init()
cons public init(java.lang.ClassLoader)
fld public final static int XGD_END = 99
fld public final static int XGD_NEED_ATTRIBUTE = 1
fld public final static int XGD_NEED_END_ATTR = 2
fld public final static int XGD_NEED_END_NULLVALUE = 4
fld public final static int XGD_NEED_END_OBJECT = 5
fld public final static int XGD_NEED_END_PARAM = 10
fld public final static int XGD_NEED_END_ROW = 3
fld public final static int XGD_NEED_END_ROW_ELEMENT = 14
fld public final static int XGD_NEED_OBJECT = 7
fld public final static int XGD_NEED_PARAM = 9
fld public final static int XGD_NEED_ROW = 6
fld public final static int XGD_NEED_ROW_ELEMENT = 13
fld public final static int XGD_NEED_ROW_TAG = 12
fld public final static int XGD_NEED_STRING = 11
fld public final static int XGD_NEED_VALUE = 8
intf java.io.Serializable
meth public java.lang.Class findClass(java.lang.String) throws java.lang.ClassNotFoundException
meth public java.lang.Class popRowType()
meth public java.lang.Class topRowType()
meth public java.lang.String popAttrName()
meth public java.lang.String topAttrName()
meth public void DumpStatus()
meth public void endDocument() throws org.xml.sax.SAXException
meth public void endElement(java.lang.String) throws org.xml.sax.SAXException
meth public void processObjectReference(java.lang.String) throws org.xml.sax.SAXException
meth public void pushAttrName(java.lang.String)
meth public void pushRowType(java.lang.Class)
meth public void readAttributeHeader(java.lang.String,org.xml.sax.AttributeList)
meth public void readObjectHeader(java.lang.String,org.xml.sax.AttributeList,boolean) throws org.xml.sax.SAXException
meth public void readValue(java.lang.String,org.xml.sax.AttributeList) throws org.xml.sax.SAXException
meth public void setCurrentAttribute(java.lang.Object) throws org.xml.sax.SAXException
meth public void startDocument() throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,org.xml.sax.AttributeList) throws org.xml.sax.SAXException
supr org.netbeans.modules.dbschema.migration.archiver.deserializer.BaseSpecificXMLDeserializer
hfds AttrNameStack,ObjectHash,RowCountStack,RowTypeStack,hashedClasses
hcls WrapperClassHelper

CLSS public org.netbeans.modules.dbschema.migration.archiver.serializer.XMLGraphSerializer
cons public init(java.io.OutputStream)
meth public void DumpStatus()
meth public void writeObject(java.lang.Object) throws java.io.IOException
supr java.lang.Object
hfds IDAttrib,ObjectMap,arrayTag,classAttrib,encoding,endCDATA,endEmptyTag,endEmptyTagNL,endTag,endTagNL,indent,indentChar,indentLevel,objectTag,outStream,primitiveArray,refAttrib,rowAttrib,rowClassAttrib,rowTag,sizeAttrib,startCDATA,startTag

CLSS public org.netbeans.modules.dbschema.nodes.ColumnElementNode
cons public init(org.netbeans.modules.dbschema.ColumnElement,boolean)
fld public final static java.lang.String COLUMN = "org/netbeans/modules/dbschema/resources/column"
fld public final static java.lang.String COLUMNS_CATEGORY = "org/netbeans/modules/dbschema/resources/columns"
fld public final static java.lang.String FK = "org/netbeans/modules/dbschema/resources/columnForeign"
fld public final static java.lang.String FKS_CATEGORY = "org/netbeans/modules/dbschema/resources/foreignKeys"
fld public final static java.lang.String INDEX = "org/netbeans/modules/dbschema/resources/index"
fld public final static java.lang.String INDEXES_CATEGORY = "org/netbeans/modules/dbschema/resources/indexes"
fld public final static java.lang.String SCHEMA = "org/netbeans/modules/dbschema/resources/database"
fld public final static java.lang.String TABLE = "org/netbeans/modules/dbschema/resources/table"
fld public final static java.lang.String VIEW = "org/netbeans/modules/dbschema/resources/view"
meth protected java.lang.String resolveIconBase()
meth protected org.openide.nodes.Node$Property createLengthProperty(boolean)
meth protected org.openide.nodes.Node$Property createNullableProperty(boolean)
meth protected org.openide.nodes.Node$Property createPrecisionProperty(boolean)
meth protected org.openide.nodes.Node$Property createScaleProperty(boolean)
meth protected org.openide.nodes.Node$Property createTypeProperty(boolean)
meth protected org.openide.nodes.Sheet createSheet()
supr org.netbeans.modules.dbschema.nodes.DBMemberElementNode

CLSS public org.netbeans.modules.dbschema.nodes.ColumnPairElementNode
cons public init(org.netbeans.modules.dbschema.ColumnPairElement,boolean)
fld public final static java.lang.String COLUMN = "org/netbeans/modules/dbschema/resources/column"
fld public final static java.lang.String COLUMNS_CATEGORY = "org/netbeans/modules/dbschema/resources/columns"
fld public final static java.lang.String FK = "org/netbeans/modules/dbschema/resources/columnForeign"
fld public final static java.lang.String FKS_CATEGORY = "org/netbeans/modules/dbschema/resources/foreignKeys"
fld public final static java.lang.String INDEX = "org/netbeans/modules/dbschema/resources/index"
fld public final static java.lang.String INDEXES_CATEGORY = "org/netbeans/modules/dbschema/resources/indexes"
fld public final static java.lang.String SCHEMA = "org/netbeans/modules/dbschema/resources/database"
fld public final static java.lang.String TABLE = "org/netbeans/modules/dbschema/resources/table"
fld public final static java.lang.String VIEW = "org/netbeans/modules/dbschema/resources/view"
meth protected java.lang.String resolveIconBase()
meth protected org.openide.nodes.Node$Property createLocalColumnProperty(boolean)
meth protected org.openide.nodes.Node$Property createNameProperty(boolean)
meth protected org.openide.nodes.Node$Property createReferencedColumnProperty(boolean)
meth protected org.openide.nodes.Sheet createSheet()
supr org.netbeans.modules.dbschema.nodes.DBMemberElementNode

CLSS public abstract org.netbeans.modules.dbschema.nodes.DBElementNode
cons public init(org.netbeans.modules.dbschema.DBElement,org.openide.nodes.Children,boolean)
fld protected boolean writeable
fld protected org.netbeans.modules.dbschema.DBElement element
fld public final static java.lang.String COLUMN = "org/netbeans/modules/dbschema/resources/column"
fld public final static java.lang.String COLUMNS_CATEGORY = "org/netbeans/modules/dbschema/resources/columns"
fld public final static java.lang.String FK = "org/netbeans/modules/dbschema/resources/columnForeign"
fld public final static java.lang.String FKS_CATEGORY = "org/netbeans/modules/dbschema/resources/foreignKeys"
fld public final static java.lang.String INDEX = "org/netbeans/modules/dbschema/resources/index"
fld public final static java.lang.String INDEXES_CATEGORY = "org/netbeans/modules/dbschema/resources/indexes"
fld public final static java.lang.String SCHEMA = "org/netbeans/modules/dbschema/resources/database"
fld public final static java.lang.String TABLE = "org/netbeans/modules/dbschema/resources/table"
fld public final static java.lang.String VIEW = "org/netbeans/modules/dbschema/resources/view"
intf org.netbeans.modules.dbschema.DBElementProperties
intf org.openide.nodes.Node$Cookie
meth protected abstract java.lang.String resolveIconBase()
meth protected java.lang.String[] getIconAffectingProperties()
meth protected org.openide.nodes.Node$Property createNameProperty(boolean)
meth public boolean canCopy()
meth public boolean canCut()
meth public boolean canDestroy()
meth public boolean canRename()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.awt.datatransfer.Transferable clipboardCopy() throws java.io.IOException
meth public java.awt.datatransfer.Transferable clipboardCut() throws java.io.IOException
meth public org.openide.nodes.Node$Cookie getCookie(java.lang.Class)
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.nodes.AbstractNode
hfds ICON_AFFECTING_PROPERTIES,listener
hcls ElementListener,ElementProp,ElementStringTransferable

CLSS public abstract interface org.netbeans.modules.dbschema.nodes.DBElementNodeFactory
meth public abstract org.openide.nodes.Node createColumnNode(org.netbeans.modules.dbschema.ColumnElement)
meth public abstract org.openide.nodes.Node createColumnPairNode(org.netbeans.modules.dbschema.ColumnPairElement)
meth public abstract org.openide.nodes.Node createErrorNode()
meth public abstract org.openide.nodes.Node createForeignKeyNode(org.netbeans.modules.dbschema.ForeignKeyElement)
meth public abstract org.openide.nodes.Node createIndexNode(org.netbeans.modules.dbschema.IndexElement)
meth public abstract org.openide.nodes.Node createSchemaNode(org.netbeans.modules.dbschema.SchemaElement)
meth public abstract org.openide.nodes.Node createTableNode(org.netbeans.modules.dbschema.TableElement)
meth public abstract org.openide.nodes.Node createWaitNode()

CLSS public abstract org.netbeans.modules.dbschema.nodes.DBMemberElementNode
cons public init(org.netbeans.modules.dbschema.DBMemberElement,org.openide.nodes.Children,boolean)
fld public final static java.lang.String COLUMN = "org/netbeans/modules/dbschema/resources/column"
fld public final static java.lang.String COLUMNS_CATEGORY = "org/netbeans/modules/dbschema/resources/columns"
fld public final static java.lang.String FK = "org/netbeans/modules/dbschema/resources/columnForeign"
fld public final static java.lang.String FKS_CATEGORY = "org/netbeans/modules/dbschema/resources/foreignKeys"
fld public final static java.lang.String INDEX = "org/netbeans/modules/dbschema/resources/index"
fld public final static java.lang.String INDEXES_CATEGORY = "org/netbeans/modules/dbschema/resources/indexes"
fld public final static java.lang.String SCHEMA = "org/netbeans/modules/dbschema/resources/database"
fld public final static java.lang.String TABLE = "org/netbeans/modules/dbschema/resources/table"
fld public final static java.lang.String VIEW = "org/netbeans/modules/dbschema/resources/view"
meth protected org.openide.nodes.Node$Property createNameProperty(boolean)
supr org.netbeans.modules.dbschema.nodes.DBElementNode

CLSS public org.netbeans.modules.dbschema.nodes.DefaultDBFactory
cons public init(boolean)
fld public final static java.lang.String COLUMN = "org/netbeans/modules/dbschema/resources/column"
fld public final static java.lang.String COLUMNS_CATEGORY = "org/netbeans/modules/dbschema/resources/columns"
fld public final static java.lang.String ERROR = "org/openide/src/resources/error"
fld public final static java.lang.String FK = "org/netbeans/modules/dbschema/resources/columnForeign"
fld public final static java.lang.String FKS_CATEGORY = "org/netbeans/modules/dbschema/resources/foreignKeys"
fld public final static java.lang.String INDEX = "org/netbeans/modules/dbschema/resources/index"
fld public final static java.lang.String INDEXES_CATEGORY = "org/netbeans/modules/dbschema/resources/indexes"
fld public final static java.lang.String SCHEMA = "org/netbeans/modules/dbschema/resources/database"
fld public final static java.lang.String TABLE = "org/netbeans/modules/dbschema/resources/table"
fld public final static java.lang.String VIEW = "org/netbeans/modules/dbschema/resources/view"
fld public final static java.lang.String WAIT = "org/openide/src/resources/wait"
fld public final static org.netbeans.modules.dbschema.nodes.DefaultDBFactory READ_ONLY
fld public final static org.netbeans.modules.dbschema.nodes.DefaultDBFactory READ_WRITE
intf org.netbeans.modules.dbschema.nodes.DBElementNodeFactory
meth protected final org.openide.nodes.Children createForeignKeyChildren(org.netbeans.modules.dbschema.ForeignKeyElement,org.netbeans.modules.dbschema.nodes.DBElementNodeFactory)
meth protected final org.openide.nodes.Children createIndexChildren(org.netbeans.modules.dbschema.IndexElement,org.netbeans.modules.dbschema.nodes.DBElementNodeFactory)
meth protected final org.openide.nodes.Children createSchemaChildren(org.netbeans.modules.dbschema.SchemaElement,org.netbeans.modules.dbschema.nodes.DBElementNodeFactory)
meth protected final org.openide.nodes.Children createTableChildren(org.netbeans.modules.dbschema.TableElement,org.netbeans.modules.dbschema.nodes.DBElementNodeFactory)
meth protected org.openide.nodes.Children createForeignKeyChildren(org.netbeans.modules.dbschema.ForeignKeyElement)
meth protected org.openide.nodes.Children createIndexChildren(org.netbeans.modules.dbschema.IndexElement)
meth protected org.openide.nodes.Children createSchemaChildren(org.netbeans.modules.dbschema.SchemaElement)
meth protected org.openide.nodes.Children createTableChildren(org.netbeans.modules.dbschema.TableElement)
meth public boolean isWriteable()
meth public org.openide.nodes.Node createColumnNode(org.netbeans.modules.dbschema.ColumnElement)
meth public org.openide.nodes.Node createColumnPairNode(org.netbeans.modules.dbschema.ColumnPairElement)
meth public org.openide.nodes.Node createErrorNode()
meth public org.openide.nodes.Node createForeignKeyNode(org.netbeans.modules.dbschema.ForeignKeyElement)
meth public org.openide.nodes.Node createIndexNode(org.netbeans.modules.dbschema.IndexElement)
meth public org.openide.nodes.Node createSchemaNode(org.netbeans.modules.dbschema.SchemaElement)
meth public org.openide.nodes.Node createTableNode(org.netbeans.modules.dbschema.TableElement)
meth public org.openide.nodes.Node createWaitNode()
supr java.lang.Object
hfds CATEGORY_ACTIONS,CATEGORY_ICONS,FILTERS,NAMES,_writeable
hcls ElementCategoryNode

CLSS public org.netbeans.modules.dbschema.nodes.ForeignKeyElementNode
cons public init(org.netbeans.modules.dbschema.ForeignKeyElement,org.netbeans.modules.dbschema.nodes.TableChildren,boolean)
fld public final static java.lang.String COLUMN = "org/netbeans/modules/dbschema/resources/column"
fld public final static java.lang.String COLUMNS_CATEGORY = "org/netbeans/modules/dbschema/resources/columns"
fld public final static java.lang.String FK = "org/netbeans/modules/dbschema/resources/columnForeign"
fld public final static java.lang.String FKS_CATEGORY = "org/netbeans/modules/dbschema/resources/foreignKeys"
fld public final static java.lang.String INDEX = "org/netbeans/modules/dbschema/resources/index"
fld public final static java.lang.String INDEXES_CATEGORY = "org/netbeans/modules/dbschema/resources/indexes"
fld public final static java.lang.String SCHEMA = "org/netbeans/modules/dbschema/resources/database"
fld public final static java.lang.String TABLE = "org/netbeans/modules/dbschema/resources/table"
fld public final static java.lang.String VIEW = "org/netbeans/modules/dbschema/resources/view"
meth protected java.lang.String resolveIconBase()
meth protected org.openide.nodes.Sheet createSheet()
supr org.netbeans.modules.dbschema.nodes.DBMemberElementNode

CLSS public org.netbeans.modules.dbschema.nodes.IndexElementNode
cons public init(org.netbeans.modules.dbschema.IndexElement,org.netbeans.modules.dbschema.nodes.TableChildren,boolean)
fld public final static java.lang.String COLUMN = "org/netbeans/modules/dbschema/resources/column"
fld public final static java.lang.String COLUMNS_CATEGORY = "org/netbeans/modules/dbschema/resources/columns"
fld public final static java.lang.String FK = "org/netbeans/modules/dbschema/resources/columnForeign"
fld public final static java.lang.String FKS_CATEGORY = "org/netbeans/modules/dbschema/resources/foreignKeys"
fld public final static java.lang.String INDEX = "org/netbeans/modules/dbschema/resources/index"
fld public final static java.lang.String INDEXES_CATEGORY = "org/netbeans/modules/dbschema/resources/indexes"
fld public final static java.lang.String SCHEMA = "org/netbeans/modules/dbschema/resources/database"
fld public final static java.lang.String TABLE = "org/netbeans/modules/dbschema/resources/table"
fld public final static java.lang.String VIEW = "org/netbeans/modules/dbschema/resources/view"
meth protected java.lang.String resolveIconBase()
meth protected org.openide.nodes.Node$Property createUniqueProperty(boolean)
meth protected org.openide.nodes.Sheet createSheet()
supr org.netbeans.modules.dbschema.nodes.DBMemberElementNode

CLSS public org.netbeans.modules.dbschema.nodes.SchemaChildren
cons public init(org.netbeans.modules.dbschema.SchemaElement)
cons public init(org.netbeans.modules.dbschema.nodes.DBElementNodeFactory,org.netbeans.modules.dbschema.SchemaElement)
fld protected java.util.Collection[] cpl
fld protected org.netbeans.modules.dbschema.SchemaElement element
fld protected org.netbeans.modules.dbschema.nodes.DBElementNodeFactory factory
fld protected org.netbeans.modules.dbschema.nodes.SchemaElementFilter filter
fld protected static java.util.HashMap propToFilter
meth protected int[] getOrder()
meth protected java.util.Collection getKeysOfType(int)
meth protected org.openide.nodes.Node[] createNodes(java.lang.Object)
meth protected void addNotify()
meth protected void refreshAllKeys()
meth protected void refreshKeys(int)
meth protected void removeNotify()
meth public java.lang.Class getFilterClass()
meth public java.lang.Object getFilter()
meth public void setFilter(java.lang.Object)
supr org.openide.nodes.Children$Keys
hfds nodesInited,propL,wPropL
hcls DBElementListener

CLSS public org.netbeans.modules.dbschema.nodes.SchemaElementFilter
cons public init()
fld public final static int ALL = 3
fld public final static int TABLE = 1
fld public final static int VIEW = 2
fld public final static int[] DEFAULT_ORDER
meth public boolean isAllTables()
meth public int[] getOrder()
meth public void setAllTables(boolean)
meth public void setOrder(int[])
supr java.lang.Object
hfds allTables,order

CLSS public org.netbeans.modules.dbschema.nodes.SchemaElementNode
cons public init(org.netbeans.modules.dbschema.SchemaElement,org.openide.nodes.Children,boolean)
fld public final static java.lang.String COLUMN = "org/netbeans/modules/dbschema/resources/column"
fld public final static java.lang.String COLUMNS_CATEGORY = "org/netbeans/modules/dbschema/resources/columns"
fld public final static java.lang.String FK = "org/netbeans/modules/dbschema/resources/columnForeign"
fld public final static java.lang.String FKS_CATEGORY = "org/netbeans/modules/dbschema/resources/foreignKeys"
fld public final static java.lang.String INDEX = "org/netbeans/modules/dbschema/resources/index"
fld public final static java.lang.String INDEXES_CATEGORY = "org/netbeans/modules/dbschema/resources/indexes"
fld public final static java.lang.String SCHEMA = "org/netbeans/modules/dbschema/resources/database"
fld public final static java.lang.String TABLE = "org/netbeans/modules/dbschema/resources/table"
fld public final static java.lang.String VIEW = "org/netbeans/modules/dbschema/resources/view"
meth protected java.lang.String resolveIconBase()
meth protected org.openide.nodes.Node$Property createCatalogProperty(boolean)
meth protected org.openide.nodes.Node$Property createDatabaseProductNameProperty(boolean)
meth protected org.openide.nodes.Node$Property createDatabaseProductVersionProperty(boolean)
meth protected org.openide.nodes.Node$Property createDriverNameProperty(boolean)
meth protected org.openide.nodes.Node$Property createDriverProperty(boolean)
meth protected org.openide.nodes.Node$Property createDriverVersionProperty(boolean)
meth protected org.openide.nodes.Node$Property createSchemaProperty(boolean)
meth protected org.openide.nodes.Node$Property createUrlProperty(boolean)
meth protected org.openide.nodes.Node$Property createUsernameProperty(boolean)
meth protected org.openide.nodes.Sheet createSheet()
supr org.netbeans.modules.dbschema.nodes.DBElementNode

CLSS public org.netbeans.modules.dbschema.nodes.SchemaRootChildren
cons public init(org.netbeans.modules.dbschema.nodes.DBElementNodeFactory,org.netbeans.modules.dbschema.jdbcimpl.DBschemaDataObject)
fld protected org.netbeans.modules.dbschema.SchemaElement element
fld protected org.netbeans.modules.dbschema.nodes.DBElementNodeFactory factory
meth protected org.openide.nodes.Node[] createNodes(java.lang.Object)
meth protected void addNotify()
meth protected void removeNotify()
meth public org.netbeans.modules.dbschema.SchemaElement getElement()
meth public void refreshKeys()
meth public void setElement(org.netbeans.modules.dbschema.SchemaElement)
supr org.openide.nodes.Children$Keys
hfds ERROR_KEY,NOT_KEY,RP,nodesInited,obj,parseLock,parseStatus,propL,wPropL
hcls DBElementListener

CLSS public org.netbeans.modules.dbschema.nodes.TableChildren
cons public init(org.netbeans.modules.dbschema.DBElement)
cons public init(org.netbeans.modules.dbschema.nodes.DBElementNodeFactory,org.netbeans.modules.dbschema.DBElement)
fld protected java.util.Collection[] cpl
fld protected org.netbeans.modules.dbschema.DBElement element
fld protected org.netbeans.modules.dbschema.nodes.DBElementNodeFactory factory
fld protected org.netbeans.modules.dbschema.nodes.TableElementFilter filter
fld protected static java.util.HashMap propToFilter
meth protected int[] getOrder()
meth protected java.util.Collection getKeysOfType(int)
meth protected org.openide.nodes.Node[] createNodes(java.lang.Object)
meth protected void addNotify()
meth protected void refreshAllKeys()
meth protected void refreshKeys(int)
meth protected void removeNotify()
meth public java.lang.Class getFilterClass()
meth public java.lang.Object getFilter()
meth public void setFilter(java.lang.Object)
supr org.openide.nodes.Children$Keys
hfds comparator,nodesInited,propL,wPropL
hcls DBElementListener

CLSS public org.netbeans.modules.dbschema.nodes.TableElementFilter
cons public init()
fld public final static int ALL = 63
fld public final static int COLUMN = 4
fld public final static int COLUMN_PAIR = 32
fld public final static int FK = 16
fld public final static int INDEX = 8
fld public final static int[] DEFAULT_ORDER
meth public boolean isSorted()
meth public void setSorted(boolean)
supr org.netbeans.modules.dbschema.nodes.SchemaElementFilter
hfds sorted

CLSS public org.netbeans.modules.dbschema.nodes.TableElementNode
cons public init(org.netbeans.modules.dbschema.TableElement,org.openide.nodes.Children,boolean)
fld public final static java.lang.String COLUMN = "org/netbeans/modules/dbschema/resources/column"
fld public final static java.lang.String COLUMNS_CATEGORY = "org/netbeans/modules/dbschema/resources/columns"
fld public final static java.lang.String FK = "org/netbeans/modules/dbschema/resources/columnForeign"
fld public final static java.lang.String FKS_CATEGORY = "org/netbeans/modules/dbschema/resources/foreignKeys"
fld public final static java.lang.String INDEX = "org/netbeans/modules/dbschema/resources/index"
fld public final static java.lang.String INDEXES_CATEGORY = "org/netbeans/modules/dbschema/resources/indexes"
fld public final static java.lang.String SCHEMA = "org/netbeans/modules/dbschema/resources/database"
fld public final static java.lang.String TABLE = "org/netbeans/modules/dbschema/resources/table"
fld public final static java.lang.String VIEW = "org/netbeans/modules/dbschema/resources/view"
meth protected java.lang.String resolveIconBase()
meth protected java.lang.String[] getIconAffectingProperties()
meth protected org.openide.nodes.Node$Property createNameProperty(boolean)
meth protected org.openide.nodes.Node$Property createTableOrViewProperty(boolean)
meth protected org.openide.nodes.Sheet createSheet()
supr org.netbeans.modules.dbschema.nodes.DBElementNode
hfds ICON_AFFECTING_PROPERTIES

CLSS public org.netbeans.modules.dbschema.util.IDEUtil
cons public init()
meth public static boolean isIDERunning()
supr java.lang.Object

CLSS public org.netbeans.modules.dbschema.util.NameUtil
cons public init()
fld public final static char dbElementSeparator = '.'
meth public static java.lang.String getAbsoluteMemberName(java.lang.String,java.lang.String)
meth public static java.lang.String getAbsoluteTableName(java.lang.String,java.lang.String)
meth public static java.lang.String getRelativeMemberName(java.lang.String)
meth public static java.lang.String getRelativeTableName(java.lang.String)
meth public static java.lang.String getSchemaName(java.lang.String)
meth public static java.lang.String getSchemaResourceName(java.lang.String)
meth public static java.lang.String getTableName(java.lang.String)
supr java.lang.Object
hfds columnPairSeparator

CLSS public org.netbeans.modules.dbschema.util.SQLTypeUtil
cons public init()
meth public static boolean isBlob(int)
meth public static boolean isCharacter(int)
meth public static boolean isCompatibleType(int,int)
meth public static boolean isLob(int)
meth public static boolean isNumeric(int)
meth public static java.lang.String getSqlTypeString(int)
meth public static java.lang.String getString(java.lang.String)
supr java.lang.Object

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

CLSS public abstract org.openide.nodes.Children
cons public init()
fld public final static org.openide.nodes.Children LEAF
fld public final static org.openide.util.Mutex MUTEX
innr public abstract static Keys
innr public static Array
innr public static Map
innr public static SortedArray
innr public static SortedMap
meth protected final boolean isInitialized()
meth protected final org.openide.nodes.Node getNode()
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void addNotify()
meth protected void removeNotify()
meth public abstract boolean add(org.openide.nodes.Node[])
meth public abstract boolean remove(org.openide.nodes.Node[])
meth public final int getNodesCount()
meth public final java.util.Enumeration<org.openide.nodes.Node> nodes()
meth public final java.util.List<org.openide.nodes.Node> snapshot()
meth public final org.openide.nodes.Node getNodeAt(int)
meth public final org.openide.nodes.Node[] getNodes()
meth public int getNodesCount(boolean)
meth public org.openide.nodes.Node findChild(java.lang.String)
meth public org.openide.nodes.Node[] getNodes(boolean)
meth public static <%0 extends java.lang.Object> org.openide.nodes.Children create(org.openide.nodes.ChildFactory<{%%0}>,boolean)
meth public static org.openide.nodes.Children createLazy(java.util.concurrent.Callable<org.openide.nodes.Children>)
supr java.lang.Object
hfds LOG,PR,entrySupport,lazySupport,parent
hcls Dupl,Empty,Entry,LazyChildren,ProjectManagerDeadlockDetector

CLSS public static org.openide.nodes.Children$Array
 outer org.openide.nodes.Children
cons protected init(java.util.Collection<org.openide.nodes.Node>)
cons public init()
fld protected java.util.Collection<org.openide.nodes.Node> nodes
intf java.lang.Cloneable
meth protected final void refresh()
meth protected java.util.Collection<org.openide.nodes.Node> initCollection()
meth public boolean add(org.openide.nodes.Node[])
meth public boolean remove(org.openide.nodes.Node[])
meth public java.lang.Object clone()
supr org.openide.nodes.Children
hfds COLLECTION_LOCK,nodesEntry
hcls AE

CLSS public abstract static org.openide.nodes.Children$Keys<%0 extends java.lang.Object>
 outer org.openide.nodes.Children
cons protected init(boolean)
cons public init()
meth protected abstract org.openide.nodes.Node[] createNodes({org.openide.nodes.Children$Keys%0})
meth protected final void refreshKey({org.openide.nodes.Children$Keys%0})
meth protected final void setBefore(boolean)
meth protected final void setKeys(java.util.Collection<? extends {org.openide.nodes.Children$Keys%0}>)
meth protected final void setKeys({org.openide.nodes.Children$Keys%0}[])
meth protected void destroyNodes(org.openide.nodes.Node[])
meth public boolean add(org.openide.nodes.Node[])
 anno 0 java.lang.Deprecated()
meth public boolean remove(org.openide.nodes.Node[])
 anno 0 java.lang.Deprecated()
meth public java.lang.Object clone()
supr org.openide.nodes.Children$Array
hfds before,lastRuns
hcls KE

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

CLSS public abstract interface org.xml.sax.DTDHandler
meth public abstract void notationDecl(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void unparsedEntityDecl(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.DocumentHandler
meth public abstract void characters(char[],int,int) throws org.xml.sax.SAXException
meth public abstract void endDocument() throws org.xml.sax.SAXException
meth public abstract void endElement(java.lang.String) throws org.xml.sax.SAXException
meth public abstract void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public abstract void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void setDocumentLocator(org.xml.sax.Locator)
meth public abstract void startDocument() throws org.xml.sax.SAXException
meth public abstract void startElement(java.lang.String,org.xml.sax.AttributeList) throws org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.ErrorHandler
meth public abstract void error(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public abstract void fatalError(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public abstract void warning(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException

