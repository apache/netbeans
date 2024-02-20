#Signature file v4.1
#Version 7.68

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

CLSS public abstract interface java.beans.Customizer
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setObject(java.lang.Object)

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

CLSS public abstract interface java.util.EventListener

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
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

CLSS public abstract interface javax.swing.RootPaneContainer
meth public abstract java.awt.Component getGlassPane()
meth public abstract java.awt.Container getContentPane()
meth public abstract javax.swing.JLayeredPane getLayeredPane()
meth public abstract javax.swing.JRootPane getRootPane()
meth public abstract void setContentPane(java.awt.Container)
meth public abstract void setGlassPane(java.awt.Component)
meth public abstract void setLayeredPane(javax.swing.JLayeredPane)

CLSS public abstract interface javax.swing.WindowConstants
fld public final static int DISPOSE_ON_CLOSE = 2
fld public final static int DO_NOTHING_ON_CLOSE = 0
fld public final static int EXIT_ON_CLOSE = 3
fld public final static int HIDE_ON_CLOSE = 1

CLSS public abstract interface org.netbeans.api.actions.Closable
meth public abstract boolean close()

CLSS public abstract interface org.netbeans.api.actions.Editable
meth public abstract void edit()

CLSS public abstract interface org.netbeans.api.actions.Openable
meth public abstract void open()

CLSS public abstract interface org.netbeans.api.actions.Printable
meth public abstract void print()

CLSS public abstract interface org.netbeans.api.actions.Savable
fld public final static org.openide.util.Lookup REGISTRY
meth public abstract java.lang.String toString()
meth public abstract void save() throws java.io.IOException

CLSS public abstract interface org.netbeans.api.actions.Viewable
meth public abstract void view()

CLSS public abstract interface org.openide.cookies.CloseCookie
intf org.netbeans.api.actions.Closable
intf org.openide.nodes.Node$Cookie

CLSS public abstract interface org.openide.cookies.ConnectionCookie
 anno 0 java.lang.Deprecated()
innr public abstract interface static Listener
innr public abstract interface static Type
innr public static Event
intf org.openide.nodes.Node$Cookie
meth public abstract java.util.Set<? extends org.openide.cookies.ConnectionCookie$Type> getTypes()
meth public abstract void register(org.openide.cookies.ConnectionCookie$Type,org.openide.nodes.Node) throws java.io.IOException
meth public abstract void unregister(org.openide.cookies.ConnectionCookie$Type,org.openide.nodes.Node) throws java.io.IOException

CLSS public static org.openide.cookies.ConnectionCookie$Event
 outer org.openide.cookies.ConnectionCookie
cons public init(org.openide.nodes.Node,org.openide.cookies.ConnectionCookie$Type)
meth public org.openide.cookies.ConnectionCookie$Type getType()
meth public org.openide.nodes.Node getNode()
supr java.util.EventObject
hfds serialVersionUID,type

CLSS public abstract interface static org.openide.cookies.ConnectionCookie$Listener
 outer org.openide.cookies.ConnectionCookie
intf java.util.EventListener
intf org.openide.nodes.Node$Cookie
meth public abstract void notify(org.openide.cookies.ConnectionCookie$Event)

CLSS public abstract interface static org.openide.cookies.ConnectionCookie$Type
 outer org.openide.cookies.ConnectionCookie
intf java.io.Serializable
meth public abstract boolean isPersistent()
meth public abstract boolean overlaps(org.openide.cookies.ConnectionCookie$Type)
meth public abstract java.lang.Class<?> getEventClass()

CLSS public abstract interface org.openide.cookies.EditCookie
intf org.netbeans.api.actions.Editable
intf org.openide.nodes.Node$Cookie

CLSS public abstract interface org.openide.cookies.FilterCookie
 anno 0 java.lang.Deprecated()
intf org.openide.nodes.Node$Cookie
meth public abstract java.lang.Class getFilterClass()
meth public abstract java.lang.Object getFilter()
meth public abstract void setFilter(java.lang.Object)

CLSS public abstract interface org.openide.cookies.InstanceCookie
innr public abstract interface static Of
intf org.openide.nodes.Node$Cookie
meth public abstract java.lang.Class<?> instanceClass() throws java.io.IOException,java.lang.ClassNotFoundException
meth public abstract java.lang.Object instanceCreate() throws java.io.IOException,java.lang.ClassNotFoundException
meth public abstract java.lang.String instanceName()

CLSS public abstract interface static org.openide.cookies.InstanceCookie$Of
 outer org.openide.cookies.InstanceCookie
intf org.openide.cookies.InstanceCookie
meth public abstract boolean instanceOf(java.lang.Class<?>)

CLSS public abstract interface org.openide.cookies.OpenCookie
intf org.netbeans.api.actions.Openable
intf org.openide.nodes.Node$Cookie

CLSS public abstract interface org.openide.cookies.PrintCookie
intf org.netbeans.api.actions.Printable
intf org.openide.nodes.Node$Cookie

CLSS public abstract interface org.openide.cookies.SaveCookie
intf org.netbeans.api.actions.Savable
intf org.openide.nodes.Node$Cookie

CLSS public abstract interface org.openide.cookies.ViewCookie
intf org.netbeans.api.actions.Viewable
intf org.openide.nodes.Node$Cookie

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

CLSS public org.openide.nodes.BeanChildren
cons public init(java.beans.beancontext.BeanContext)
cons public init(java.beans.beancontext.BeanContext,org.openide.nodes.BeanChildren$Factory)
innr public abstract interface static Factory
meth protected org.openide.nodes.Node[] createNodes(java.lang.Object)
meth protected void addNotify()
meth protected void removeNotify()
supr org.openide.nodes.Children$Keys
hfds DEFAULT_FACTORY,bean,contextL,factory,nodes2Beans
hcls BeanFactory,ContextL

CLSS public abstract interface static org.openide.nodes.BeanChildren$Factory
 outer org.openide.nodes.BeanChildren
meth public abstract org.openide.nodes.Node createNode(java.lang.Object) throws java.beans.IntrospectionException

CLSS public abstract interface !annotation org.openide.nodes.BeanInfoSearchPath
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PACKAGE])
intf java.lang.annotation.Annotation

CLSS public org.openide.nodes.BeanNode<%0 extends java.lang.Object>
cons protected init({org.openide.nodes.BeanNode%0},org.openide.nodes.Children) throws java.beans.IntrospectionException
cons protected init({org.openide.nodes.BeanNode%0},org.openide.nodes.Children,org.openide.util.Lookup) throws java.beans.IntrospectionException
cons public init({org.openide.nodes.BeanNode%0}) throws java.beans.IntrospectionException
innr public final static Descriptor
meth protected void createProperties({org.openide.nodes.BeanNode%0},java.beans.BeanInfo)
meth protected void setSynchronizeName(boolean)
meth protected {org.openide.nodes.BeanNode%0} getBean()
meth public boolean canCopy()
meth public boolean canCut()
meth public boolean canDestroy()
meth public boolean canRename()
meth public boolean hasCustomizer()
meth public java.awt.Component getCustomizer()
meth public java.awt.Image getIcon(int)
meth public java.awt.Image getOpenedIcon(int)
meth public javax.swing.Action getPreferredAction()
meth public javax.swing.Action[] getActions(boolean)
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static org.openide.nodes.BeanNode$Descriptor computeProperties(java.lang.Object,java.beans.BeanInfo)
meth public void destroy() throws java.io.IOException
meth public void setName(java.lang.String)
supr org.openide.nodes.AbstractNode
hfds ICON_BASE,bean,beanInfo,nameGetter,nameSetter,propertyChangeListener,removePCLMethod,synchronizeName
hcls PropL

CLSS public final static org.openide.nodes.BeanNode$Descriptor
 outer org.openide.nodes.BeanNode
fld public final org.openide.nodes.Node$Property[] expert
fld public final org.openide.nodes.Node$Property[] hidden
fld public final org.openide.nodes.Node$Property[] property
supr java.lang.Object

CLSS public abstract org.openide.nodes.ChildFactory<%0 extends java.lang.Object>
cons public init()
innr public abstract static Detachable
meth protected abstract boolean createKeys(java.util.List<{org.openide.nodes.ChildFactory%0}>)
meth protected final void refresh(boolean)
meth protected org.openide.nodes.Node createNodeForKey({org.openide.nodes.ChildFactory%0})
meth protected org.openide.nodes.Node createWaitNode()
meth protected org.openide.nodes.Node[] createNodesForKey({org.openide.nodes.ChildFactory%0})
supr java.lang.Object
hfds observer
hcls Observer,WaitFilterNode

CLSS public abstract static org.openide.nodes.ChildFactory$Detachable<%0 extends java.lang.Object>
 outer org.openide.nodes.ChildFactory
cons public init()
meth protected void addNotify()
meth protected void removeNotify()
supr org.openide.nodes.ChildFactory<{org.openide.nodes.ChildFactory$Detachable%0}>

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

CLSS public static org.openide.nodes.Children$Map<%0 extends java.lang.Object>
 outer org.openide.nodes.Children
cons protected init(java.util.Map<{org.openide.nodes.Children$Map%0},org.openide.nodes.Node>)
cons public init()
fld protected java.util.Map<{org.openide.nodes.Children$Map%0},org.openide.nodes.Node> nodes
meth protected final void put({org.openide.nodes.Children$Map%0},org.openide.nodes.Node)
meth protected final void putAll(java.util.Map<? extends {org.openide.nodes.Children$Map%0},? extends org.openide.nodes.Node>)
meth protected final void refresh()
meth protected final void refreshKey({org.openide.nodes.Children$Map%0})
meth protected final void removeAll(java.util.Collection<? extends {org.openide.nodes.Children$Map%0}>)
meth protected java.util.Map<{org.openide.nodes.Children$Map%0},org.openide.nodes.Node> initMap()
meth protected void remove({org.openide.nodes.Children$Map%0})
meth public boolean add(org.openide.nodes.Node[])
meth public boolean remove(org.openide.nodes.Node[])
supr org.openide.nodes.Children
hcls ME

CLSS public static org.openide.nodes.Children$SortedArray
 outer org.openide.nodes.Children
cons protected init(java.util.Collection<org.openide.nodes.Node>)
cons public init()
meth public java.util.Comparator<? super org.openide.nodes.Node> getComparator()
meth public void setComparator(java.util.Comparator<? super org.openide.nodes.Node>)
supr org.openide.nodes.Children$Array
hfds comp
hcls SAE

CLSS public static org.openide.nodes.Children$SortedMap<%0 extends java.lang.Object>
 outer org.openide.nodes.Children
cons protected init(java.util.Map<{org.openide.nodes.Children$SortedMap%0},org.openide.nodes.Node>)
cons public init()
meth public java.util.Comparator<? super org.openide.nodes.Node> getComparator()
meth public void setComparator(java.util.Comparator<? super org.openide.nodes.Node>)
supr org.openide.nodes.Children$Map<{org.openide.nodes.Children$SortedMap%0}>
hfds comp
hcls SMComparator

CLSS public final org.openide.nodes.CookieSet
cons public init()
innr public abstract interface static Before
innr public abstract interface static Factory
intf org.openide.util.Lookup$Provider
meth public !varargs <%0 extends java.lang.Object> void assign(java.lang.Class<? extends {%%0}>,{%%0}[])
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public org.openide.util.Lookup getLookup()
meth public static org.openide.nodes.CookieSet createGeneric(org.openide.nodes.CookieSet$Before)
meth public void add(java.lang.Class<? extends org.openide.nodes.Node$Cookie>,org.openide.nodes.CookieSet$Factory)
meth public void add(java.lang.Class<? extends org.openide.nodes.Node$Cookie>[],org.openide.nodes.CookieSet$Factory)
meth public void add(org.openide.nodes.Node$Cookie)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void remove(java.lang.Class<? extends org.openide.nodes.Node$Cookie>,org.openide.nodes.CookieSet$Factory)
meth public void remove(java.lang.Class<? extends org.openide.nodes.Node$Cookie>[],org.openide.nodes.CookieSet$Factory)
meth public void remove(org.openide.nodes.Node$Cookie)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
hfds QUERY_MODE,cs,ic,lookup,map
hcls C,CookieEntry,CookieEntryPair,PairWrap,R

CLSS public abstract interface static org.openide.nodes.CookieSet$Before
 outer org.openide.nodes.CookieSet
meth public abstract void beforeLookup(java.lang.Class<?>)

CLSS public abstract interface static org.openide.nodes.CookieSet$Factory
 outer org.openide.nodes.CookieSet
meth public abstract <%0 extends org.openide.nodes.Node$Cookie> {%%0} createCookie(java.lang.Class<{%%0}>)

CLSS public final org.openide.nodes.DefaultHandle
intf org.openide.nodes.Node$Handle
meth public java.lang.String toString()
meth public org.openide.nodes.Node getNode() throws java.io.IOException
meth public static org.openide.nodes.DefaultHandle createHandle(org.openide.nodes.Node)
supr java.lang.Object
hfds parent,path,serialVersionUID

CLSS public abstract org.openide.nodes.DestroyableNodesFactory<%0 extends java.lang.Object>
cons public init()
meth protected void destroyNodes(org.openide.nodes.Node[])
supr org.openide.nodes.ChildFactory$Detachable<{org.openide.nodes.DestroyableNodesFactory%0}>

CLSS public org.openide.nodes.FilterNode
cons public init(org.openide.nodes.Node)
cons public init(org.openide.nodes.Node,org.openide.nodes.Children)
cons public init(org.openide.nodes.Node,org.openide.nodes.Children,org.openide.util.Lookup)
fld protected final static int DELEGATE_DESTROY = 64
fld protected final static int DELEGATE_GET_ACTIONS = 128
fld protected final static int DELEGATE_GET_CONTEXT_ACTIONS = 256
fld protected final static int DELEGATE_GET_DISPLAY_NAME = 8
fld protected final static int DELEGATE_GET_NAME = 2
fld protected final static int DELEGATE_GET_SHORT_DESCRIPTION = 32
fld protected final static int DELEGATE_GET_VALUE = 1024
fld protected final static int DELEGATE_SET_DISPLAY_NAME = 4
fld protected final static int DELEGATE_SET_NAME = 1
fld protected final static int DELEGATE_SET_SHORT_DESCRIPTION = 16
fld protected final static int DELEGATE_SET_VALUE = 512
innr protected static NodeAdapter
innr protected static PropertyChangeAdapter
innr public static Children
meth protected final void changeOriginal(org.openide.nodes.Node,boolean)
meth protected final void disableDelegation(int)
meth protected final void enableDelegation(int)
meth protected java.beans.PropertyChangeListener createPropertyChangeListener()
meth protected org.openide.nodes.Node getOriginal()
meth protected org.openide.nodes.NodeListener createNodeListener()
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public boolean canCopy()
meth public boolean canCut()
meth public boolean canDestroy()
meth public boolean canRename()
meth public boolean equals(java.lang.Object)
meth public boolean hasCustomizer()
meth public int hashCode()
meth public java.awt.Component getCustomizer()
meth public java.awt.Image getIcon(int)
meth public java.awt.Image getOpenedIcon(int)
meth public java.awt.datatransfer.Transferable clipboardCopy() throws java.io.IOException
meth public java.awt.datatransfer.Transferable clipboardCut() throws java.io.IOException
meth public java.awt.datatransfer.Transferable drag() throws java.io.IOException
meth public java.lang.Object getValue(java.lang.String)
meth public java.lang.String getDisplayName()
meth public java.lang.String getHtmlDisplayName()
meth public java.lang.String getName()
meth public java.lang.String getShortDescription()
meth public javax.swing.Action getPreferredAction()
meth public javax.swing.Action[] getActions(boolean)
meth public org.openide.nodes.Node cloneNode()
meth public org.openide.nodes.Node$Handle getHandle()
meth public org.openide.nodes.Node$PropertySet[] getPropertySets()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public org.openide.util.actions.SystemAction getDefaultAction()
 anno 0 java.lang.Deprecated()
meth public org.openide.util.actions.SystemAction[] getActions()
 anno 0 java.lang.Deprecated()
meth public org.openide.util.actions.SystemAction[] getContextActions()
 anno 0 java.lang.Deprecated()
meth public org.openide.util.datatransfer.NewType[] getNewTypes()
meth public org.openide.util.datatransfer.PasteType getDropType(java.awt.datatransfer.Transferable,int,int)
meth public org.openide.util.datatransfer.PasteType[] getPasteTypes(java.awt.datatransfer.Transferable)
meth public void destroy() throws java.io.IOException
meth public void setDisplayName(java.lang.String)
meth public void setName(java.lang.String)
meth public void setShortDescription(java.lang.String)
meth public void setValue(java.lang.String,java.lang.Object)
supr org.openide.nodes.Node
hfds DELEGATE_ALL,LISTENER_LOCK,LOGGER,childrenProvided,delegateMask,hashCodeDepth,lookupProvided,nodeL,original,overridesGetDisplayNameCache,pchlAttached,propL,replaceProvidedLookupCache
hcls ChildrenAdapter,FilterHandle,FilterLookup,StackError

CLSS public static org.openide.nodes.FilterNode$Children
 outer org.openide.nodes.FilterNode
cons public init(org.openide.nodes.Node)
fld protected org.openide.nodes.Node original
intf java.lang.Cloneable
meth protected final void changeOriginal(org.openide.nodes.Node)
meth protected org.openide.nodes.Node copyNode(org.openide.nodes.Node)
meth protected org.openide.nodes.Node[] createNodes(org.openide.nodes.Node)
meth protected void addNotify()
meth protected void filterChildrenAdded(org.openide.nodes.NodeMemberEvent)
meth protected void filterChildrenRemoved(org.openide.nodes.NodeMemberEvent)
meth protected void filterChildrenReordered(org.openide.nodes.NodeReorderEvent)
meth protected void finalize()
meth protected void removeNotify()
meth public boolean add(org.openide.nodes.Node[])
 anno 0 java.lang.Deprecated()
meth public boolean remove(org.openide.nodes.Node[])
 anno 0 java.lang.Deprecated()
meth public int getNodesCount(boolean)
meth public java.lang.Object clone()
meth public org.openide.nodes.Node findChild(java.lang.String)
meth public org.openide.nodes.Node[] getNodes(boolean)
supr org.openide.nodes.Children$Keys<org.openide.nodes.Node>
hfds nodeL
hcls DefaultSupport,FilterChildrenSupport,LazySupport

CLSS protected static org.openide.nodes.FilterNode$NodeAdapter
 outer org.openide.nodes.FilterNode
cons public init(org.openide.nodes.FilterNode)
intf org.openide.nodes.NodeListener
meth protected void propertyChange(org.openide.nodes.FilterNode,java.beans.PropertyChangeEvent)
meth public final void nodeDestroyed(org.openide.nodes.NodeEvent)
meth public final void propertyChange(java.beans.PropertyChangeEvent)
meth public void childrenAdded(org.openide.nodes.NodeMemberEvent)
meth public void childrenRemoved(org.openide.nodes.NodeMemberEvent)
meth public void childrenReordered(org.openide.nodes.NodeReorderEvent)
supr java.lang.Object
hfds fnRef

CLSS protected static org.openide.nodes.FilterNode$PropertyChangeAdapter
 outer org.openide.nodes.FilterNode
cons public init(org.openide.nodes.FilterNode)
intf java.beans.PropertyChangeListener
meth protected void propertyChange(org.openide.nodes.FilterNode,java.beans.PropertyChangeEvent)
meth public final void propertyChange(java.beans.PropertyChangeEvent)
supr java.lang.Object
hfds fnRef

CLSS public abstract interface org.openide.nodes.Index
innr public abstract static KeysChildren
innr public abstract static Support
innr public static ArrayChildren
intf org.openide.nodes.Node$Cookie
meth public abstract int getNodesCount()
meth public abstract int indexOf(org.openide.nodes.Node)
meth public abstract org.openide.nodes.Node[] getNodes()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void exchange(int,int)
meth public abstract void move(int,int)
meth public abstract void moveDown(int)
meth public abstract void moveUp(int)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public abstract void reorder()
meth public abstract void reorder(int[])

CLSS public static org.openide.nodes.Index$ArrayChildren
 outer org.openide.nodes.Index
cons public init()
fld protected org.openide.nodes.Index support
intf org.openide.nodes.Index
meth protected java.util.List<org.openide.nodes.Node> initCollection()
meth public int indexOf(org.openide.nodes.Node)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void exchange(int,int)
meth public void move(int,int)
meth public void moveDown(int)
meth public void moveUp(int)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void reorder()
meth public void reorder(int[])
supr org.openide.nodes.Children$Array

CLSS public abstract static org.openide.nodes.Index$KeysChildren<%0 extends java.lang.Object>
 outer org.openide.nodes.Index
cons public init(java.util.List<{org.openide.nodes.Index$KeysChildren%0}>)
fld protected final java.util.List<{org.openide.nodes.Index$KeysChildren%0}> list
meth protected java.lang.Object lock()
meth protected org.openide.nodes.Index createIndex()
meth protected void reorder(int[])
meth public final void update()
meth public org.openide.nodes.Index getIndex()
supr org.openide.nodes.Children$Keys<{org.openide.nodes.Index$KeysChildren%0}>
hfds support

CLSS public abstract static org.openide.nodes.Index$Support
 outer org.openide.nodes.Index
cons public init()
intf org.openide.nodes.Index
meth protected void fireChangeEvent(javax.swing.event.ChangeEvent)
meth public abstract int getNodesCount()
meth public abstract org.openide.nodes.Node[] getNodes()
meth public abstract void reorder(int[])
meth public int indexOf(org.openide.nodes.Node)
meth public static void showIndexedCustomizer(org.openide.nodes.Index)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void exchange(int,int)
meth public void move(int,int)
meth public void moveDown(int)
meth public void moveUp(int)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void reorder()
supr java.lang.Object
hfds listeners

CLSS public final org.openide.nodes.IndexedCustomizer
 anno 0 java.lang.Deprecated()
cons public init()
intf java.beans.Customizer
meth public boolean isImmediateReorder()
meth public java.awt.Dimension getPreferredSize()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setImmediateReorder(boolean)
meth public void setObject(java.lang.Object)
supr javax.swing.JDialog
hfds buttonClose,buttonDown,buttonUp,control,immediateReorder,index,nodeChangesL,nodes,permutation,serialVersionUID
hcls AutoscrollJList,IndexTransferable,IndexedDragSource,IndexedDropTarget,IndexedListCellRenderer

CLSS public org.openide.nodes.IndexedNode
cons protected init(org.openide.nodes.Children,org.openide.nodes.Index)
cons protected init(org.openide.nodes.Children,org.openide.nodes.Index,org.openide.util.Lookup)
cons public init()
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public boolean hasCustomizer()
meth public java.awt.Component getCustomizer()
supr org.openide.nodes.AbstractNode
hfds indexImpl

CLSS public org.openide.nodes.IndexedPropertySupport<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init(java.lang.Object,java.lang.Class<{org.openide.nodes.IndexedPropertySupport%0}>,java.lang.Class<{org.openide.nodes.IndexedPropertySupport%1}>,java.lang.reflect.Method,java.lang.reflect.Method,java.lang.reflect.Method,java.lang.reflect.Method)
fld protected java.lang.Object instance
meth public boolean canIndexedRead()
meth public boolean canIndexedWrite()
meth public boolean canRead()
meth public boolean canWrite()
meth public final void setDisplayName(java.lang.String)
meth public final void setName(java.lang.String)
meth public final void setShortDescription(java.lang.String)
meth public void setIndexedValue(int,{org.openide.nodes.IndexedPropertySupport%1}) throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
meth public void setValue({org.openide.nodes.IndexedPropertySupport%0}) throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
meth public {org.openide.nodes.IndexedPropertySupport%0} getValue() throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
meth public {org.openide.nodes.IndexedPropertySupport%1} getIndexedValue(int) throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
supr org.openide.nodes.Node$IndexedProperty<{org.openide.nodes.IndexedPropertySupport%0},{org.openide.nodes.IndexedPropertySupport%1}>
hfds getter,indexedGetter,indexedSetter,setter

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

CLSS public abstract interface static org.openide.nodes.Node$Handle
 outer org.openide.nodes.Node
fld public final static long serialVersionUID = -4518262478987434353
 anno 0 java.lang.Deprecated()
intf java.io.Serializable
meth public abstract org.openide.nodes.Node getNode() throws java.io.IOException

CLSS public abstract static org.openide.nodes.Node$IndexedProperty<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer org.openide.nodes.Node
cons public init(java.lang.Class<{org.openide.nodes.Node$IndexedProperty%0}>,java.lang.Class<{org.openide.nodes.Node$IndexedProperty%1}>)
meth public abstract boolean canIndexedRead()
meth public abstract boolean canIndexedWrite()
meth public abstract void setIndexedValue(int,{org.openide.nodes.Node$IndexedProperty%1}) throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
meth public abstract {org.openide.nodes.Node$IndexedProperty%1} getIndexedValue(int) throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.beans.PropertyEditor getIndexedPropertyEditor()
meth public java.lang.Class<{org.openide.nodes.Node$IndexedProperty%1}> getElementType()
supr org.openide.nodes.Node$Property<{org.openide.nodes.Node$IndexedProperty%0}>
hfds elementType

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

CLSS public abstract static org.openide.nodes.Node$PropertySet
 outer org.openide.nodes.Node
cons public init()
cons public init(java.lang.String,java.lang.String,java.lang.String)
meth public abstract org.openide.nodes.Node$Property<?>[] getProperties()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getHtmlDisplayName()
supr java.beans.FeatureDescriptor

CLSS public abstract interface org.openide.nodes.NodeAcceptor
meth public abstract boolean acceptNodes(org.openide.nodes.Node[])

CLSS public org.openide.nodes.NodeAdapter
cons public init()
intf org.openide.nodes.NodeListener
meth public void childrenAdded(org.openide.nodes.NodeMemberEvent)
meth public void childrenRemoved(org.openide.nodes.NodeMemberEvent)
meth public void childrenReordered(org.openide.nodes.NodeReorderEvent)
meth public void nodeDestroyed(org.openide.nodes.NodeEvent)
meth public void propertyChange(java.beans.PropertyChangeEvent)
supr java.lang.Object

CLSS public org.openide.nodes.NodeEvent
cons public init(org.openide.nodes.Node)
meth public final org.openide.nodes.Node getNode()
supr java.util.EventObject
hfds serialVersionUID

CLSS public abstract interface org.openide.nodes.NodeListener
intf java.beans.PropertyChangeListener
meth public abstract void childrenAdded(org.openide.nodes.NodeMemberEvent)
meth public abstract void childrenRemoved(org.openide.nodes.NodeMemberEvent)
meth public abstract void childrenReordered(org.openide.nodes.NodeReorderEvent)
meth public abstract void nodeDestroyed(org.openide.nodes.NodeEvent)

CLSS public org.openide.nodes.NodeMemberEvent
meth public final boolean isAddEvent()
meth public final java.util.List<org.openide.nodes.Node> getSnapshot()
meth public final org.openide.nodes.Node[] getDelta()
meth public int[] getDeltaIndices()
meth public java.lang.String toString()
supr org.openide.nodes.NodeEvent
hfds add,currSnapshot,delta,indices,prevSnapshot,serialVersionUID,sourceEntry

CLSS public final org.openide.nodes.NodeNotFoundException
meth public int getClosestNodeDepth()
meth public java.lang.String getMessage()
meth public java.lang.String getMissingChildName()
meth public org.openide.nodes.Node getClosestNode()
supr java.io.IOException
hfds depth,name,node,serialVersionUID

CLSS public final org.openide.nodes.NodeOp
meth public static boolean isSon(org.openide.nodes.Node,org.openide.nodes.Node)
meth public static int[] computePermutation(org.openide.nodes.Node[],org.openide.nodes.Node[])
meth public static java.lang.String[] createPath(org.openide.nodes.Node,org.openide.nodes.Node)
meth public static javax.swing.Action[] findActions(org.openide.nodes.Node[])
meth public static javax.swing.JPopupMenu findContextMenu(org.openide.nodes.Node[])
meth public static org.openide.nodes.Node findChild(org.openide.nodes.Node,java.lang.String)
meth public static org.openide.nodes.Node findPath(org.openide.nodes.Node,java.lang.String[]) throws org.openide.nodes.NodeNotFoundException
meth public static org.openide.nodes.Node findPath(org.openide.nodes.Node,java.util.Enumeration<java.lang.String>) throws org.openide.nodes.NodeNotFoundException
meth public static org.openide.nodes.Node findRoot(org.openide.nodes.Node)
meth public static org.openide.nodes.Node$Handle[] toHandles(org.openide.nodes.Node[])
meth public static org.openide.nodes.NodeListener weakNodeListener(org.openide.nodes.NodeListener,java.lang.Object)
meth public static org.openide.nodes.Node[] fromHandles(org.openide.nodes.Node$Handle[]) throws java.io.IOException
meth public static org.openide.util.actions.SystemAction[] getDefaultActions()
 anno 0 java.lang.Deprecated()
meth public static void registerPropertyEditors()
meth public static void setDefaultActions(org.openide.util.actions.SystemAction[])
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds LOG,defaultActions

CLSS public abstract org.openide.nodes.NodeOperation
cons protected init()
meth public !varargs void showCustomEditorDialog(org.openide.nodes.Node$Property<?>,java.lang.Object[])
meth public abstract boolean customize(org.openide.nodes.Node)
meth public abstract org.openide.nodes.Node[] select(java.lang.String,java.lang.String,org.openide.nodes.Node,org.openide.nodes.NodeAcceptor,java.awt.Component) throws org.openide.util.UserCancelException
meth public abstract void explore(org.openide.nodes.Node)
meth public abstract void showProperties(org.openide.nodes.Node)
meth public abstract void showProperties(org.openide.nodes.Node[])
meth public final org.openide.nodes.Node select(java.lang.String,java.lang.String,org.openide.nodes.Node) throws org.openide.util.UserCancelException
meth public org.openide.nodes.Node[] select(java.lang.String,java.lang.String,org.openide.nodes.Node,org.openide.nodes.NodeAcceptor) throws org.openide.util.UserCancelException
meth public static org.openide.nodes.NodeOperation getDefault()
supr java.lang.Object

CLSS public final org.openide.nodes.NodeReorderEvent
meth public final java.util.List<org.openide.nodes.Node> getSnapshot()
meth public int getPermutationSize()
meth public int newIndexOf(int)
meth public int[] getPermutation()
meth public java.lang.String toString()
supr org.openide.nodes.NodeEvent
hfds currSnapshot,newIndices,serialVersionUID

CLSS public abstract org.openide.nodes.NodeTransfer
fld public final static int CLIPBOARD_COPY = 1
fld public final static int CLIPBOARD_CUT = 4
fld public final static int COPY = 1
fld public final static int DND_COPY = 1
fld public final static int DND_COPY_OR_MOVE = 3
fld public final static int DND_LINK = 1073741824
fld public final static int DND_MOVE = 2
fld public final static int DND_NONE = 0
fld public final static int DND_REFERENCE = 1073741824
fld public final static int MOVE = 6
innr public abstract interface static Paste
meth public static <%0 extends org.openide.nodes.Node$Cookie> {%%0} cookie(java.awt.datatransfer.Transferable,int,java.lang.Class<{%%0}>)
meth public static org.openide.nodes.Node node(java.awt.datatransfer.Transferable,int)
meth public static org.openide.nodes.NodeTransfer$Paste findPaste(java.awt.datatransfer.Transferable)
meth public static org.openide.nodes.Node[] nodes(java.awt.datatransfer.Transferable,int)
meth public static org.openide.util.datatransfer.ExTransferable$Single createPaste(org.openide.nodes.NodeTransfer$Paste)
meth public static org.openide.util.datatransfer.ExTransferable$Single transferable(org.openide.nodes.Node,int)
supr java.lang.Object
hfds dndMimeType,nodePasteFlavor

CLSS public abstract interface static org.openide.nodes.NodeTransfer$Paste
 outer org.openide.nodes.NodeTransfer
meth public abstract org.openide.util.datatransfer.PasteType[] types(org.openide.nodes.Node)

CLSS public abstract interface !annotation org.openide.nodes.PropertyEditorRegistration
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class<?>[] targetType()

CLSS public abstract interface !annotation org.openide.nodes.PropertyEditorSearchPath
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PACKAGE])
intf java.lang.annotation.Annotation

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

CLSS public final static org.openide.nodes.PropertySupport$Name
 outer org.openide.nodes.PropertySupport
cons public init(org.openide.nodes.Node)
cons public init(org.openide.nodes.Node,java.lang.String,java.lang.String)
meth public java.lang.String getValue() throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
meth public void setValue(java.lang.String) throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
supr org.openide.nodes.PropertySupport<java.lang.String>
hfds node

CLSS public abstract static org.openide.nodes.PropertySupport$ReadOnly<%0 extends java.lang.Object>
 outer org.openide.nodes.PropertySupport
cons public init(java.lang.String,java.lang.Class<{org.openide.nodes.PropertySupport$ReadOnly%0}>,java.lang.String,java.lang.String)
meth public void setValue({org.openide.nodes.PropertySupport$ReadOnly%0}) throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
supr org.openide.nodes.PropertySupport<{org.openide.nodes.PropertySupport$ReadOnly%0}>

CLSS public abstract static org.openide.nodes.PropertySupport$ReadWrite<%0 extends java.lang.Object>
 outer org.openide.nodes.PropertySupport
cons public init(java.lang.String,java.lang.Class<{org.openide.nodes.PropertySupport$ReadWrite%0}>,java.lang.String,java.lang.String)
supr org.openide.nodes.PropertySupport<{org.openide.nodes.PropertySupport$ReadWrite%0}>

CLSS public static org.openide.nodes.PropertySupport$Reflection<%0 extends java.lang.Object>
 outer org.openide.nodes.PropertySupport
cons public init(java.lang.Object,java.lang.Class<{org.openide.nodes.PropertySupport$Reflection%0}>,java.lang.String) throws java.lang.NoSuchMethodException
cons public init(java.lang.Object,java.lang.Class<{org.openide.nodes.PropertySupport$Reflection%0}>,java.lang.String,java.lang.String) throws java.lang.NoSuchMethodException
cons public init(java.lang.Object,java.lang.Class<{org.openide.nodes.PropertySupport$Reflection%0}>,java.lang.reflect.Method,java.lang.reflect.Method)
fld protected java.lang.Object instance
meth public boolean canRead()
meth public boolean canWrite()
meth public java.beans.PropertyEditor getPropertyEditor()
meth public void setPropertyEditorClass(java.lang.Class<? extends java.beans.PropertyEditor>)
meth public void setValue({org.openide.nodes.PropertySupport$Reflection%0}) throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
meth public {org.openide.nodes.PropertySupport$Reflection%0} getValue() throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
supr org.openide.nodes.Node$Property<{org.openide.nodes.PropertySupport$Reflection%0}>
hfds getter,propertyEditorClass,setter

CLSS public abstract static org.openide.nodes.PropertySupport$WriteOnly<%0 extends java.lang.Object>
 outer org.openide.nodes.PropertySupport
cons public init(java.lang.String,java.lang.Class<{org.openide.nodes.PropertySupport$WriteOnly%0}>,java.lang.String,java.lang.String)
meth public {org.openide.nodes.PropertySupport$WriteOnly%0} getValue() throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
supr org.openide.nodes.PropertySupport<{org.openide.nodes.PropertySupport$WriteOnly%0}>

CLSS public final org.openide.nodes.Sheet
cons public init()
fld public final static java.lang.String EXPERT = "expert"
fld public final static java.lang.String PROPERTIES = "properties"
innr public final static Set
meth public final org.openide.nodes.Node$PropertySet[] toArray()
meth public org.openide.nodes.Sheet cloneSheet()
meth public org.openide.nodes.Sheet$Set get(java.lang.String)
meth public org.openide.nodes.Sheet$Set put(org.openide.nodes.Sheet$Set)
meth public org.openide.nodes.Sheet$Set remove(java.lang.String)
meth public static org.openide.nodes.Sheet createDefault()
meth public static org.openide.nodes.Sheet$Set createExpertSet()
meth public static org.openide.nodes.Sheet$Set createPropertiesSet()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds array,propL,sets,supp

CLSS public final static org.openide.nodes.Sheet$Set
 outer org.openide.nodes.Sheet
cons public init()
meth public org.openide.nodes.Node$Property<?> get(java.lang.String)
meth public org.openide.nodes.Node$Property<?> put(org.openide.nodes.Node$Property<?>)
meth public org.openide.nodes.Node$Property<?> remove(java.lang.String)
meth public org.openide.nodes.Node$Property<?>[] getProperties()
meth public org.openide.nodes.Sheet$Set cloneSet()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void put(org.openide.nodes.Node$Property<?>[])
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr org.openide.nodes.Node$PropertySet
hfds array,props,supp

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

CLSS public abstract org.openide.util.actions.ActionInvoker
cons protected init()
meth protected abstract void invokeAction(javax.swing.Action,java.awt.event.ActionEvent)
meth public static void invokeAction(javax.swing.Action,java.awt.event.ActionEvent,boolean,java.lang.Runnable)
supr java.lang.Object
hfds RP
hcls ActionRunnable

CLSS public abstract interface org.openide.util.actions.ActionPerformer
 anno 0 java.lang.Deprecated()
meth public abstract void performAction(org.openide.util.actions.SystemAction)

CLSS public abstract org.openide.util.actions.ActionPresenterProvider
cons protected init()
meth public abstract java.awt.Component createToolbarPresenter(javax.swing.Action)
meth public abstract java.awt.Component[] convertComponents(java.awt.Component)
meth public abstract javax.swing.JMenuItem createMenuPresenter(javax.swing.Action)
meth public abstract javax.swing.JMenuItem createPopupPresenter(javax.swing.Action)
meth public abstract javax.swing.JPopupMenu createEmptyPopup()
meth public static org.openide.util.actions.ActionPresenterProvider getDefault()
supr java.lang.Object
hcls Default

CLSS public abstract org.openide.util.actions.BooleanStateAction
 anno 0 java.lang.Deprecated()
cons public init()
fld public final static java.lang.String PROP_BOOLEAN_STATE = "booleanState"
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected void initialize()
meth public boolean getBooleanState()
meth public java.awt.Component getToolbarPresenter()
meth public javax.swing.JMenuItem getMenuPresenter()
meth public javax.swing.JMenuItem getPopupPresenter()
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void setBooleanState(boolean)
supr org.openide.util.actions.SystemAction
hfds serialVersionUID

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

