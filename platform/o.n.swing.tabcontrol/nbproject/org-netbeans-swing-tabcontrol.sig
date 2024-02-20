#Signature file v4.1
#Version 1.80

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

CLSS protected abstract java.awt.Component$AccessibleAWTComponent
 outer java.awt.Component
cons protected init(java.awt.Component)
fld protected java.awt.event.ComponentListener accessibleAWTComponentHandler
fld protected java.awt.event.FocusListener accessibleAWTFocusHandler
innr protected AccessibleAWTComponentHandler
innr protected AccessibleAWTFocusHandler
intf java.io.Serializable
intf javax.accessibility.AccessibleComponent
meth public boolean contains(java.awt.Point)
meth public boolean isEnabled()
meth public boolean isFocusTraversable()
meth public boolean isShowing()
meth public boolean isVisible()
meth public int getAccessibleChildrenCount()
meth public int getAccessibleIndexInParent()
meth public java.awt.Color getBackground()
meth public java.awt.Color getForeground()
meth public java.awt.Cursor getCursor()
meth public java.awt.Dimension getSize()
meth public java.awt.Font getFont()
meth public java.awt.FontMetrics getFontMetrics(java.awt.Font)
meth public java.awt.Point getLocation()
meth public java.awt.Point getLocationOnScreen()
meth public java.awt.Rectangle getBounds()
meth public java.lang.String getAccessibleDescription()
meth public java.lang.String getAccessibleName()
meth public java.util.Locale getLocale()
meth public javax.accessibility.Accessible getAccessibleAt(java.awt.Point)
meth public javax.accessibility.Accessible getAccessibleChild(int)
meth public javax.accessibility.Accessible getAccessibleParent()
meth public javax.accessibility.AccessibleComponent getAccessibleComponent()
meth public javax.accessibility.AccessibleRole getAccessibleRole()
meth public javax.accessibility.AccessibleStateSet getAccessibleStateSet()
meth public void addFocusListener(java.awt.event.FocusListener)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removeFocusListener(java.awt.event.FocusListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void requestFocus()
meth public void setBackground(java.awt.Color)
meth public void setBounds(java.awt.Rectangle)
meth public void setCursor(java.awt.Cursor)
meth public void setEnabled(boolean)
meth public void setFont(java.awt.Font)
meth public void setForeground(java.awt.Color)
meth public void setLocation(java.awt.Point)
meth public void setSize(java.awt.Dimension)
meth public void setVisible(boolean)
supr javax.accessibility.AccessibleContext

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

CLSS protected java.awt.Container$AccessibleAWTContainer
 outer java.awt.Container
cons protected init(java.awt.Container)
fld protected java.awt.event.ContainerListener accessibleContainerHandler
innr protected AccessibleContainerHandler
meth public int getAccessibleChildrenCount()
meth public javax.accessibility.Accessible getAccessibleAt(java.awt.Point)
meth public javax.accessibility.Accessible getAccessibleChild(int)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.awt.Component$AccessibleAWTComponent

CLSS public abstract interface java.awt.ItemSelectable
meth public abstract java.lang.Object[] getSelectedObjects()
meth public abstract void addItemListener(java.awt.event.ItemListener)
meth public abstract void removeItemListener(java.awt.event.ItemListener)

CLSS public abstract interface java.awt.MenuContainer
meth public abstract boolean postEvent(java.awt.Event)
 anno 0 java.lang.Deprecated()
meth public abstract java.awt.Font getFont()
meth public abstract void remove(java.awt.MenuComponent)

CLSS public java.awt.Polygon
cons public init()
cons public init(int[],int[],int)
fld protected java.awt.Rectangle bounds
fld public int npoints
fld public int[] xpoints
fld public int[] ypoints
intf java.awt.Shape
intf java.io.Serializable
meth public boolean contains(double,double)
meth public boolean contains(double,double,double,double)
meth public boolean contains(int,int)
meth public boolean contains(java.awt.Point)
meth public boolean contains(java.awt.geom.Point2D)
meth public boolean contains(java.awt.geom.Rectangle2D)
meth public boolean inside(int,int)
 anno 0 java.lang.Deprecated()
meth public boolean intersects(double,double,double,double)
meth public boolean intersects(java.awt.geom.Rectangle2D)
meth public java.awt.Rectangle getBoundingBox()
 anno 0 java.lang.Deprecated()
meth public java.awt.Rectangle getBounds()
meth public java.awt.geom.PathIterator getPathIterator(java.awt.geom.AffineTransform)
meth public java.awt.geom.PathIterator getPathIterator(java.awt.geom.AffineTransform,double)
meth public java.awt.geom.Rectangle2D getBounds2D()
meth public void addPoint(int,int)
meth public void invalidate()
meth public void reset()
meth public void translate(int,int)
supr java.lang.Object

CLSS public abstract interface java.awt.Shape
meth public abstract boolean contains(double,double)
meth public abstract boolean contains(double,double,double,double)
meth public abstract boolean contains(java.awt.geom.Point2D)
meth public abstract boolean contains(java.awt.geom.Rectangle2D)
meth public abstract boolean intersects(double,double,double,double)
meth public abstract boolean intersects(java.awt.geom.Rectangle2D)
meth public abstract java.awt.Rectangle getBounds()
meth public abstract java.awt.geom.PathIterator getPathIterator(java.awt.geom.AffineTransform)
meth public abstract java.awt.geom.PathIterator getPathIterator(java.awt.geom.AffineTransform,double)
meth public abstract java.awt.geom.Rectangle2D getBounds2D()

CLSS public abstract interface java.awt.dnd.Autoscroll
meth public abstract java.awt.Insets getAutoscrollInsets()
meth public abstract void autoscroll(java.awt.Point)

CLSS public java.awt.event.ActionEvent
cons public init(java.lang.Object,int,java.lang.String)
cons public init(java.lang.Object,int,java.lang.String,int)
cons public init(java.lang.Object,int,java.lang.String,long,int)
fld public final static int ACTION_FIRST = 1001
fld public final static int ACTION_LAST = 1001
fld public final static int ACTION_PERFORMED = 1001
fld public final static int ALT_MASK = 8
fld public final static int CTRL_MASK = 2
fld public final static int META_MASK = 4
fld public final static int SHIFT_MASK = 1
meth public int getModifiers()
meth public java.lang.String getActionCommand()
meth public java.lang.String paramString()
meth public long getWhen()
supr java.awt.AWTEvent

CLSS public abstract interface java.awt.event.ActionListener
intf java.util.EventListener
meth public abstract void actionPerformed(java.awt.event.ActionEvent)

CLSS public abstract java.awt.event.ComponentAdapter
cons public init()
intf java.awt.event.ComponentListener
meth public void componentHidden(java.awt.event.ComponentEvent)
meth public void componentMoved(java.awt.event.ComponentEvent)
meth public void componentResized(java.awt.event.ComponentEvent)
meth public void componentShown(java.awt.event.ComponentEvent)
supr java.lang.Object

CLSS public abstract interface java.awt.event.ComponentListener
intf java.util.EventListener
meth public abstract void componentHidden(java.awt.event.ComponentEvent)
meth public abstract void componentMoved(java.awt.event.ComponentEvent)
meth public abstract void componentResized(java.awt.event.ComponentEvent)
meth public abstract void componentShown(java.awt.event.ComponentEvent)

CLSS public abstract interface java.awt.event.HierarchyListener
intf java.util.EventListener
meth public abstract void hierarchyChanged(java.awt.event.HierarchyEvent)

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

CLSS public abstract interface javax.accessibility.AccessibleComponent
meth public abstract boolean contains(java.awt.Point)
meth public abstract boolean isEnabled()
meth public abstract boolean isFocusTraversable()
meth public abstract boolean isShowing()
meth public abstract boolean isVisible()
meth public abstract java.awt.Color getBackground()
meth public abstract java.awt.Color getForeground()
meth public abstract java.awt.Cursor getCursor()
meth public abstract java.awt.Dimension getSize()
meth public abstract java.awt.Font getFont()
meth public abstract java.awt.FontMetrics getFontMetrics(java.awt.Font)
meth public abstract java.awt.Point getLocation()
meth public abstract java.awt.Point getLocationOnScreen()
meth public abstract java.awt.Rectangle getBounds()
meth public abstract javax.accessibility.Accessible getAccessibleAt(java.awt.Point)
meth public abstract void addFocusListener(java.awt.event.FocusListener)
meth public abstract void removeFocusListener(java.awt.event.FocusListener)
meth public abstract void requestFocus()
meth public abstract void setBackground(java.awt.Color)
meth public abstract void setBounds(java.awt.Rectangle)
meth public abstract void setCursor(java.awt.Cursor)
meth public abstract void setEnabled(boolean)
meth public abstract void setFont(java.awt.Font)
meth public abstract void setForeground(java.awt.Color)
meth public abstract void setLocation(java.awt.Point)
meth public abstract void setSize(java.awt.Dimension)
meth public abstract void setVisible(boolean)

CLSS public abstract javax.accessibility.AccessibleContext
cons public init()
fld protected java.lang.String accessibleDescription
fld protected java.lang.String accessibleName
fld protected javax.accessibility.Accessible accessibleParent
fld public final static java.lang.String ACCESSIBLE_ACTION_PROPERTY = "accessibleActionProperty"
fld public final static java.lang.String ACCESSIBLE_ACTIVE_DESCENDANT_PROPERTY = "AccessibleActiveDescendant"
fld public final static java.lang.String ACCESSIBLE_CARET_PROPERTY = "AccessibleCaret"
fld public final static java.lang.String ACCESSIBLE_CHILD_PROPERTY = "AccessibleChild"
fld public final static java.lang.String ACCESSIBLE_COMPONENT_BOUNDS_CHANGED = "accessibleComponentBoundsChanged"
fld public final static java.lang.String ACCESSIBLE_DESCRIPTION_PROPERTY = "AccessibleDescription"
fld public final static java.lang.String ACCESSIBLE_HYPERTEXT_OFFSET = "AccessibleHypertextOffset"
fld public final static java.lang.String ACCESSIBLE_INVALIDATE_CHILDREN = "accessibleInvalidateChildren"
fld public final static java.lang.String ACCESSIBLE_NAME_PROPERTY = "AccessibleName"
fld public final static java.lang.String ACCESSIBLE_SELECTION_PROPERTY = "AccessibleSelection"
fld public final static java.lang.String ACCESSIBLE_STATE_PROPERTY = "AccessibleState"
fld public final static java.lang.String ACCESSIBLE_TABLE_CAPTION_CHANGED = "accessibleTableCaptionChanged"
fld public final static java.lang.String ACCESSIBLE_TABLE_COLUMN_DESCRIPTION_CHANGED = "accessibleTableColumnDescriptionChanged"
fld public final static java.lang.String ACCESSIBLE_TABLE_COLUMN_HEADER_CHANGED = "accessibleTableColumnHeaderChanged"
fld public final static java.lang.String ACCESSIBLE_TABLE_MODEL_CHANGED = "accessibleTableModelChanged"
fld public final static java.lang.String ACCESSIBLE_TABLE_ROW_DESCRIPTION_CHANGED = "accessibleTableRowDescriptionChanged"
fld public final static java.lang.String ACCESSIBLE_TABLE_ROW_HEADER_CHANGED = "accessibleTableRowHeaderChanged"
fld public final static java.lang.String ACCESSIBLE_TABLE_SUMMARY_CHANGED = "accessibleTableSummaryChanged"
fld public final static java.lang.String ACCESSIBLE_TEXT_ATTRIBUTES_CHANGED = "accessibleTextAttributesChanged"
fld public final static java.lang.String ACCESSIBLE_TEXT_PROPERTY = "AccessibleText"
fld public final static java.lang.String ACCESSIBLE_VALUE_PROPERTY = "AccessibleValue"
fld public final static java.lang.String ACCESSIBLE_VISIBLE_DATA_PROPERTY = "AccessibleVisibleData"
meth public abstract int getAccessibleChildrenCount()
meth public abstract int getAccessibleIndexInParent()
meth public abstract java.util.Locale getLocale()
meth public abstract javax.accessibility.Accessible getAccessibleChild(int)
meth public abstract javax.accessibility.AccessibleRole getAccessibleRole()
meth public abstract javax.accessibility.AccessibleStateSet getAccessibleStateSet()
meth public java.lang.String getAccessibleDescription()
meth public java.lang.String getAccessibleName()
meth public javax.accessibility.Accessible getAccessibleParent()
meth public javax.accessibility.AccessibleAction getAccessibleAction()
meth public javax.accessibility.AccessibleComponent getAccessibleComponent()
meth public javax.accessibility.AccessibleEditableText getAccessibleEditableText()
meth public javax.accessibility.AccessibleIcon[] getAccessibleIcon()
meth public javax.accessibility.AccessibleRelationSet getAccessibleRelationSet()
meth public javax.accessibility.AccessibleSelection getAccessibleSelection()
meth public javax.accessibility.AccessibleTable getAccessibleTable()
meth public javax.accessibility.AccessibleText getAccessibleText()
meth public javax.accessibility.AccessibleValue getAccessibleValue()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setAccessibleDescription(java.lang.String)
meth public void setAccessibleName(java.lang.String)
meth public void setAccessibleParent(javax.accessibility.Accessible)
supr java.lang.Object

CLSS public abstract interface javax.accessibility.AccessibleExtendedComponent
intf javax.accessibility.AccessibleComponent
meth public abstract java.lang.String getTitledBorderText()
meth public abstract java.lang.String getToolTipText()
meth public abstract javax.accessibility.AccessibleKeyBinding getAccessibleKeyBinding()

CLSS public abstract interface javax.accessibility.AccessibleSelection
meth public abstract boolean isAccessibleChildSelected(int)
meth public abstract int getAccessibleSelectionCount()
meth public abstract javax.accessibility.Accessible getAccessibleSelection(int)
meth public abstract void addAccessibleSelection(int)
meth public abstract void clearAccessibleSelection()
meth public abstract void removeAccessibleSelection(int)
meth public abstract void selectAllAccessibleSelection()

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

CLSS public abstract javax.swing.JComponent$AccessibleJComponent
 outer javax.swing.JComponent
cons protected init(javax.swing.JComponent)
fld protected java.awt.event.FocusListener accessibleFocusHandler
 anno 0 java.lang.Deprecated()
innr protected AccessibleContainerHandler
innr protected AccessibleFocusHandler
intf javax.accessibility.AccessibleExtendedComponent
meth protected java.lang.String getBorderTitle(javax.swing.border.Border)
meth public int getAccessibleChildrenCount()
meth public java.lang.String getAccessibleDescription()
meth public java.lang.String getAccessibleName()
meth public java.lang.String getTitledBorderText()
meth public java.lang.String getToolTipText()
meth public javax.accessibility.Accessible getAccessibleChild(int)
meth public javax.accessibility.AccessibleKeyBinding getAccessibleKeyBinding()
meth public javax.accessibility.AccessibleRole getAccessibleRole()
meth public javax.accessibility.AccessibleStateSet getAccessibleStateSet()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.awt.Container$AccessibleAWTContainer

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

CLSS public abstract interface javax.swing.border.Border
meth public abstract boolean isBorderOpaque()
meth public abstract java.awt.Insets getBorderInsets(java.awt.Component)
meth public abstract void paintBorder(java.awt.Component,java.awt.Graphics,int,int,int,int)

CLSS public abstract interface javax.swing.event.CellEditorListener
intf java.util.EventListener
meth public abstract void editingCanceled(javax.swing.event.ChangeEvent)
meth public abstract void editingStopped(javax.swing.event.ChangeEvent)

CLSS public abstract interface javax.swing.event.ChangeListener
intf java.util.EventListener
meth public abstract void stateChanged(javax.swing.event.ChangeEvent)

CLSS public javax.swing.event.ListDataEvent
cons public init(java.lang.Object,int,int,int)
fld public final static int CONTENTS_CHANGED = 0
fld public final static int INTERVAL_ADDED = 1
fld public final static int INTERVAL_REMOVED = 2
meth public int getIndex0()
meth public int getIndex1()
meth public int getType()
meth public java.lang.String toString()
supr java.util.EventObject

CLSS public abstract interface javax.swing.event.ListDataListener
intf java.util.EventListener
meth public abstract void contentsChanged(javax.swing.event.ListDataEvent)
meth public abstract void intervalAdded(javax.swing.event.ListDataEvent)
meth public abstract void intervalRemoved(javax.swing.event.ListDataEvent)

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

CLSS public abstract javax.swing.plaf.ButtonUI
cons public init()
supr javax.swing.plaf.ComponentUI

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

CLSS public javax.swing.plaf.basic.BasicButtonUI
cons public init()
fld protected int defaultTextIconGap
fld protected int defaultTextShiftOffset
meth protected int getTextShiftOffset()
meth protected java.lang.String getPropertyPrefix()
meth protected javax.swing.plaf.basic.BasicButtonListener createButtonListener(javax.swing.AbstractButton)
meth protected void clearTextShiftOffset()
meth protected void installDefaults(javax.swing.AbstractButton)
meth protected void installKeyboardActions(javax.swing.AbstractButton)
meth protected void installListeners(javax.swing.AbstractButton)
meth protected void paintButtonPressed(java.awt.Graphics,javax.swing.AbstractButton)
meth protected void paintFocus(java.awt.Graphics,javax.swing.AbstractButton,java.awt.Rectangle,java.awt.Rectangle,java.awt.Rectangle)
meth protected void paintIcon(java.awt.Graphics,javax.swing.JComponent,java.awt.Rectangle)
meth protected void paintText(java.awt.Graphics,javax.swing.AbstractButton,java.awt.Rectangle,java.lang.String)
meth protected void paintText(java.awt.Graphics,javax.swing.JComponent,java.awt.Rectangle,java.lang.String)
meth protected void setTextShiftOffset()
meth protected void uninstallDefaults(javax.swing.AbstractButton)
meth protected void uninstallKeyboardActions(javax.swing.AbstractButton)
meth protected void uninstallListeners(javax.swing.AbstractButton)
meth public int getBaseline(javax.swing.JComponent,int,int)
meth public int getDefaultTextIconGap(javax.swing.AbstractButton)
meth public java.awt.Component$BaselineResizeBehavior getBaselineResizeBehavior(javax.swing.JComponent)
meth public java.awt.Dimension getMaximumSize(javax.swing.JComponent)
meth public java.awt.Dimension getMinimumSize(javax.swing.JComponent)
meth public java.awt.Dimension getPreferredSize(javax.swing.JComponent)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
meth public void installUI(javax.swing.JComponent)
meth public void paint(java.awt.Graphics,javax.swing.JComponent)
meth public void uninstallUI(javax.swing.JComponent)
supr javax.swing.plaf.ButtonUI

CLSS public javax.swing.plaf.basic.BasicToggleButtonUI
cons public init()
meth protected int getTextShiftOffset()
meth protected java.lang.String getPropertyPrefix()
meth protected void paintIcon(java.awt.Graphics,javax.swing.AbstractButton,java.awt.Rectangle)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
meth public void paint(java.awt.Graphics,javax.swing.JComponent)
supr javax.swing.plaf.basic.BasicButtonUI

CLSS public org.netbeans.swing.popupswitcher.SwitcherTable
cons public init(org.netbeans.swing.popupswitcher.SwitcherTableItem[])
cons public init(org.netbeans.swing.popupswitcher.SwitcherTableItem[],int)
meth public int getLastValidRow()
meth public java.awt.Color getBackground()
meth public java.awt.Color getForeground()
meth public java.awt.Color getSelectionBackground()
meth public java.awt.Color getSelectionForeground()
meth public java.awt.Component prepareRenderer(javax.swing.table.TableCellRenderer,int,int)
meth public java.awt.Dimension getPreferredSize()
meth public org.netbeans.swing.popupswitcher.SwitcherTableItem getSelectedItem()
meth public void changeSelection(int,int,boolean,boolean)
meth public void paint(java.awt.Graphics)
meth public void setFont(java.awt.Font)
meth public void setSwitcherItems(org.netbeans.swing.popupswitcher.SwitcherTableItem[],int)
meth public void updateUI()
supr javax.swing.JTable
hfds TABNAMES_HTML,background,ctx,foreground,needCalcRowHeight,nullIcon,prefSize,rendererBorder,selBackground,selForeground,showIcons
hcls NullIcon

CLSS public org.netbeans.swing.popupswitcher.SwitcherTableItem
cons public init(org.netbeans.swing.popupswitcher.SwitcherTableItem$Activatable,java.lang.String)
cons public init(org.netbeans.swing.popupswitcher.SwitcherTableItem$Activatable,java.lang.String,java.lang.String,javax.swing.Icon,boolean)
cons public init(org.netbeans.swing.popupswitcher.SwitcherTableItem$Activatable,java.lang.String,java.lang.String,javax.swing.Icon,boolean,java.lang.String)
cons public init(org.netbeans.swing.popupswitcher.SwitcherTableItem$Activatable,java.lang.String,javax.swing.Icon)
innr public abstract interface static Activatable
intf java.lang.Comparable
meth public boolean equals(java.lang.Object)
meth public boolean isActive()
meth public int compareTo(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getDescription()
meth public java.lang.String getHtmlName()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public javax.swing.Icon getIcon()
meth public org.netbeans.swing.popupswitcher.SwitcherTableItem$Activatable getActivatable()
meth public void activate()
supr java.lang.Object
hfds activatable,active,description,htmlName,icon,name

CLSS public abstract interface static org.netbeans.swing.popupswitcher.SwitcherTableItem$Activatable
 outer org.netbeans.swing.popupswitcher.SwitcherTableItem
meth public abstract void activate()

CLSS public abstract interface org.netbeans.swing.tabcontrol.ComponentConverter
fld public final static org.netbeans.swing.tabcontrol.ComponentConverter DEFAULT
innr public final static Fixed
meth public abstract java.awt.Component getComponent(org.netbeans.swing.tabcontrol.TabData)

CLSS public final static org.netbeans.swing.tabcontrol.ComponentConverter$Fixed
 outer org.netbeans.swing.tabcontrol.ComponentConverter
cons public init(java.awt.Component)
intf org.netbeans.swing.tabcontrol.ComponentConverter
meth public java.awt.Component getComponent(org.netbeans.swing.tabcontrol.TabData)
supr java.lang.Object
hfds component

CLSS public org.netbeans.swing.tabcontrol.DefaultTabDataModel
cons public init()
cons public init(org.netbeans.swing.tabcontrol.TabData[])
intf org.netbeans.swing.tabcontrol.TabDataModel
meth public int indexOf(org.netbeans.swing.tabcontrol.TabData)
meth public int size()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.swing.tabcontrol.TabData> getTabs()
meth public org.netbeans.swing.tabcontrol.TabData getTab(int)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void addComplexListDataListener(org.netbeans.swing.tabcontrol.event.ComplexListDataListener)
meth public void addTab(int,org.netbeans.swing.tabcontrol.TabData)
meth public void addTabs(int,org.netbeans.swing.tabcontrol.TabData[])
meth public void addTabs(int[],org.netbeans.swing.tabcontrol.TabData[])
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void removeComplexListDataListener(org.netbeans.swing.tabcontrol.event.ComplexListDataListener)
meth public void removeTab(int)
meth public void removeTabs(int,int)
meth public void removeTabs(int[])
meth public void setIcon(int,javax.swing.Icon)
meth public void setIcon(int[],javax.swing.Icon[])
meth public void setIconsAndText(int[],java.lang.String[],javax.swing.Icon[])
meth public void setTab(int,org.netbeans.swing.tabcontrol.TabData)
meth public void setTabs(org.netbeans.swing.tabcontrol.TabData[])
meth public void setText(int,java.lang.String)
meth public void setText(int[],java.lang.String[])
meth public void setToolTipTextAt(int,java.lang.String)
supr java.lang.Object
hfds LOCK,cs,list,listenerList
hcls L

CLSS public abstract interface org.netbeans.swing.tabcontrol.LocationInformer
 anno 0 java.lang.Deprecated()
meth public abstract java.lang.Object getOrientation(java.awt.Component)

CLSS public abstract interface org.netbeans.swing.tabcontrol.SlideBarDataModel
fld public final static int EAST = 1
fld public final static int NORTH = 4
fld public final static int SOUTH = 3
fld public final static int WEST = 2
innr public static Impl
intf org.netbeans.swing.tabcontrol.TabDataModel
meth public abstract int getOrientation()
meth public abstract void setOrientation(int)

CLSS public static org.netbeans.swing.tabcontrol.SlideBarDataModel$Impl
 outer org.netbeans.swing.tabcontrol.SlideBarDataModel
cons public init()
intf org.netbeans.swing.tabcontrol.SlideBarDataModel
meth public int getOrientation()
meth public void setOrientation(int)
supr org.netbeans.swing.tabcontrol.DefaultTabDataModel
hfds orientation

CLSS public final org.netbeans.swing.tabcontrol.SlidingButton
cons public init(org.netbeans.swing.tabcontrol.TabData,int)
fld public final static java.lang.String UI_CLASS_ID = "SlidingButtonUI"
meth public boolean isBlinking()
meth public final boolean isBlinkState()
meth public final java.awt.Color getBackground()
meth public int getOrientation()
meth public java.lang.String getToolTipText()
meth public java.lang.String getUIClassID()
meth public org.netbeans.swing.tabcontrol.TabData getData()
meth public void addNotify()
meth public void removeNotify()
meth public void setBlinking(boolean)
meth public void updateUI()
supr javax.swing.JToggleButton
hfds blinkState,blinkTimer,data,isAqua,orientation
hcls BlinkListener

CLSS public org.netbeans.swing.tabcontrol.SlidingButtonUI
cons protected init()
meth protected java.lang.String getPropertyPrefix()
meth protected void paintBackground(java.awt.Graphics2D,javax.swing.AbstractButton)
meth public java.awt.Dimension getMaximumSize(javax.swing.JComponent)
meth public java.awt.Dimension getMinimumSize(javax.swing.JComponent)
meth public java.awt.Dimension getPreferredSize(javax.swing.JComponent)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
meth public void paint(java.awt.Graphics,javax.swing.JComponent)
supr javax.swing.plaf.basic.BasicToggleButtonUI
hfds INSTANCE

CLSS public final org.netbeans.swing.tabcontrol.TabData
cons public init(java.lang.Object,javax.swing.Icon,java.lang.String,java.lang.String)
intf java.lang.Comparable
meth public boolean equals(java.lang.Object)
meth public int compareTo(java.lang.Object)
meth public int hashCode()
meth public java.awt.Component getComponent()
meth public java.lang.Object getUserObject()
meth public java.lang.String getText()
meth public java.lang.String getTooltip()
meth public java.lang.String toString()
meth public javax.swing.Icon getIcon()
supr java.lang.Object
hfds NO_ICON,icon,tip,txt,userObject

CLSS public abstract interface org.netbeans.swing.tabcontrol.TabDataModel
meth public abstract int indexOf(org.netbeans.swing.tabcontrol.TabData)
meth public abstract int size()
meth public abstract java.util.List<org.netbeans.swing.tabcontrol.TabData> getTabs()
meth public abstract org.netbeans.swing.tabcontrol.TabData getTab(int)
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void addComplexListDataListener(org.netbeans.swing.tabcontrol.event.ComplexListDataListener)
meth public abstract void addTab(int,org.netbeans.swing.tabcontrol.TabData)
meth public abstract void addTabs(int,org.netbeans.swing.tabcontrol.TabData[])
meth public abstract void addTabs(int[],org.netbeans.swing.tabcontrol.TabData[])
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeComplexListDataListener(org.netbeans.swing.tabcontrol.event.ComplexListDataListener)
meth public abstract void removeTab(int)
meth public abstract void removeTabs(int,int)
meth public abstract void removeTabs(int[])
meth public abstract void setIcon(int,javax.swing.Icon)
meth public abstract void setIcon(int[],javax.swing.Icon[])
meth public abstract void setIconsAndText(int[],java.lang.String[],javax.swing.Icon[])
meth public abstract void setTab(int,org.netbeans.swing.tabcontrol.TabData)
meth public abstract void setTabs(org.netbeans.swing.tabcontrol.TabData[])
meth public abstract void setText(int,java.lang.String)
meth public abstract void setText(int[],java.lang.String[])

CLSS public final org.netbeans.swing.tabcontrol.TabDisplayer
cons public init()
cons public init(org.netbeans.swing.tabcontrol.TabDataModel,int)
cons public init(org.netbeans.swing.tabcontrol.TabDataModel,int,org.netbeans.swing.tabcontrol.LocationInformer)
 anno 0 java.lang.Deprecated()
cons public init(org.netbeans.swing.tabcontrol.TabDataModel,int,org.netbeans.swing.tabcontrol.WinsysInfoForTabbed)
 anno 0 java.lang.Deprecated()
cons public init(org.netbeans.swing.tabcontrol.TabDataModel,int,org.netbeans.swing.tabcontrol.WinsysInfoForTabbedContainer)
fld public final static int TYPE_EDITOR = 1
fld public final static int TYPE_SLIDING = 2
fld public final static int TYPE_TOOLBAR = 3
fld public final static int TYPE_VIEW = 0
fld public final static java.lang.Object ORIENTATION_CENTER
fld public final static java.lang.Object ORIENTATION_EAST
fld public final static java.lang.Object ORIENTATION_INVISIBLE
fld public final static java.lang.Object ORIENTATION_NORTH
fld public final static java.lang.Object ORIENTATION_SOUTH
fld public final static java.lang.Object ORIENTATION_WEST
fld public final static java.lang.String COMMAND_CLOSE = "close"
fld public final static java.lang.String COMMAND_CLOSE_ALL = "closeAll"
fld public final static java.lang.String COMMAND_CLOSE_ALL_BUT_THIS = "closeAllButThis"
fld public final static java.lang.String COMMAND_CLOSE_GROUP = "closeGroup"
fld public final static java.lang.String COMMAND_DISABLE_AUTO_HIDE = "disableAutoHide"
fld public final static java.lang.String COMMAND_ENABLE_AUTO_HIDE = "enableAutoHide"
fld public final static java.lang.String COMMAND_MAXIMIZE = "maximize"
fld public final static java.lang.String COMMAND_MINIMIZE_GROUP = "minimizeGroup"
fld public final static java.lang.String COMMAND_POPUP_REQUEST = "popup"
fld public final static java.lang.String COMMAND_RESTORE_GROUP = "restoreGroup"
fld public final static java.lang.String COMMAND_SELECT = "select"
fld public final static java.lang.String EDITOR_TAB_DISPLAYER_UI_CLASS_ID = "EditorTabDisplayerUI"
fld public final static java.lang.String PROP_ACTIVE = "active"
fld public final static java.lang.String PROP_ORIENTATION = "orientation"
fld public final static java.lang.String SLIDING_TAB_DISPLAYER_UI_CLASS_ID = "SlidingTabDisplayerUI"
fld public final static java.lang.String TOOLBAR_TAB_DISPLAYER_UI_CLASS_ID = "ToolbarTabDisplayerUI"
fld public final static java.lang.String VIEW_TAB_DISPLAYER_UI_CLASS_ID = "ViewTabDisplayerUI"
innr protected AccessibleTabDisplayer
intf java.awt.dnd.Autoscroll
intf javax.accessibility.Accessible
meth protected final void postActionEvent(org.netbeans.swing.tabcontrol.event.TabActionEvent)
meth public final boolean isActive()
meth public final boolean isShowCloseButton()
meth public final boolean requestAttention(org.netbeans.swing.tabcontrol.TabData)
meth public final int getType()
meth public final java.awt.Dimension getMinimumSize()
meth public final java.awt.Dimension getPreferredSize()
meth public final java.awt.Font getFont()
meth public final java.awt.Image getDragImage(int)
 anno 0 java.lang.Deprecated()
meth public final java.awt.Rectangle getTabRect(int,java.awt.Rectangle)
meth public final java.lang.String getToolTipText(java.awt.event.MouseEvent)
meth public final org.netbeans.swing.tabcontrol.TabDataModel getModel()
meth public final org.netbeans.swing.tabcontrol.TabDisplayerUI getUI()
meth public final void addActionListener(java.awt.event.ActionListener)
meth public final void cancelRequestAttention(int)
meth public final void makeTabVisible(int)
meth public final void removeActionListener(java.awt.event.ActionListener)
meth public final void requestAttention(int)
meth public final void setActive(boolean)
meth public final void setAttentionHighlight(int,boolean)
meth public final void setShowCloseButton(boolean)
meth public final void updateUI()
meth public int tabForCoordinate(java.awt.Point)
meth public java.awt.Insets getAutoscrollInsets()
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.SingleSelectionModel getSelectionModel()
meth public org.netbeans.swing.tabcontrol.ComponentConverter getComponentConverter()
meth public org.netbeans.swing.tabcontrol.LocationInformer getLocationInformer()
 anno 0 java.lang.Deprecated()
meth public org.netbeans.swing.tabcontrol.WinsysInfoForTabbed getWinsysInfo()
 anno 0 java.lang.Deprecated()
meth public org.netbeans.swing.tabcontrol.WinsysInfoForTabbedContainer getContainerWinsysInfo()
meth public void autoscroll(java.awt.Point)
meth public void registerShortcuts(javax.swing.JComponent)
meth public void setComponentConverter(org.netbeans.swing.tabcontrol.ComponentConverter)
meth public void unregisterShortcuts(javax.swing.JComponent)
supr javax.swing.JComponent
hfds actionListenerList,active,componentConverter,containerWinsysInfo,initialized,locationInformer,model,sel,showClose,type,winsysInfo

CLSS protected org.netbeans.swing.tabcontrol.TabDisplayer$AccessibleTabDisplayer
 outer org.netbeans.swing.tabcontrol.TabDisplayer
cons public init(org.netbeans.swing.tabcontrol.TabDisplayer)
intf javax.accessibility.AccessibleSelection
intf javax.swing.event.ChangeListener
meth public boolean isAccessibleChildSelected(int)
meth public int getAccessibleChildrenCount()
meth public int getAccessibleSelectionCount()
meth public javax.accessibility.Accessible getAccessibleAt(java.awt.Point)
meth public javax.accessibility.Accessible getAccessibleChild(int)
meth public javax.accessibility.Accessible getAccessibleSelection(int)
meth public javax.accessibility.AccessibleRole getAccessibleRole()
meth public javax.accessibility.AccessibleSelection getAccessibleSelection()
meth public void addAccessibleSelection(int)
meth public void clearAccessibleSelection()
meth public void removeAccessibleSelection(int)
meth public void selectAllAccessibleSelection()
meth public void stateChanged(javax.swing.event.ChangeEvent)
supr javax.swing.JComponent$AccessibleJComponent

CLSS public abstract org.netbeans.swing.tabcontrol.TabDisplayerUI
cons protected init(org.netbeans.swing.tabcontrol.TabDisplayer)
fld protected final org.netbeans.swing.tabcontrol.TabDisplayer displayer
fld protected javax.swing.SingleSelectionModel selectionModel
meth protected abstract javax.swing.SingleSelectionModel createSelectionModel()
meth protected abstract void cancelRequestAttention(int)
meth protected abstract void requestAttention(int)
meth protected final boolean shouldPerformAction(java.lang.String,int,java.awt.event.MouseEvent)
meth protected final boolean shouldPerformAction(org.netbeans.swing.tabcontrol.event.TabActionEvent)
meth protected java.awt.Font getTxtFont()
meth protected void setAttentionHighlight(int,boolean)
meth public abstract int dropIndexOfPoint(java.awt.Point)
meth public abstract int tabForCoordinate(java.awt.Point)
meth public abstract java.awt.Polygon getExactTabIndication(int)
meth public abstract java.awt.Polygon getInsertTabIndication(int)
meth public abstract java.awt.Rectangle getTabRect(int,java.awt.Rectangle)
meth public abstract void registerShortcuts(javax.swing.JComponent)
meth public abstract void unregisterShortcuts(javax.swing.JComponent)
meth public final boolean isTabBusy(int)
meth public java.awt.Image createImageOfTab(int)
meth public java.awt.Insets getAutoscrollInsets()
meth public javax.swing.Icon getButtonIcon(int,int)
meth public void autoscroll(java.awt.Point)
meth public void installUI(javax.swing.JComponent)
meth public void makeTabVisible(int)
meth public void postTabAction(org.netbeans.swing.tabcontrol.event.TabActionEvent)
meth public void uninstallUI(javax.swing.JComponent)
supr javax.swing.plaf.ComponentUI
hfds buttonIconPaths

CLSS public org.netbeans.swing.tabcontrol.TabListPopupAction
cons public init(org.netbeans.swing.tabcontrol.TabDisplayer)
meth public void actionPerformed(java.awt.event.ActionEvent)
supr javax.swing.AbstractAction
hfds displayer

CLSS public org.netbeans.swing.tabcontrol.TabbedContainer
cons public init()
cons public init(int)
cons public init(org.netbeans.swing.tabcontrol.TabDataModel)
cons public init(org.netbeans.swing.tabcontrol.TabDataModel,int)
cons public init(org.netbeans.swing.tabcontrol.TabDataModel,int,org.netbeans.swing.tabcontrol.LocationInformer)
 anno 0 java.lang.Deprecated()
cons public init(org.netbeans.swing.tabcontrol.TabDataModel,int,org.netbeans.swing.tabcontrol.WinsysInfoForTabbed)
 anno 0 java.lang.Deprecated()
cons public init(org.netbeans.swing.tabcontrol.TabDataModel,int,org.netbeans.swing.tabcontrol.WinsysInfoForTabbedContainer)
fld protected static int DEFAULT_CONTENT_POLICY
fld public final static int CONTENT_POLICY_ADD_ALL = 1
fld public final static int CONTENT_POLICY_ADD_ONLY_SELECTED = 3
fld public final static int CONTENT_POLICY_ADD_ON_FIRST_USE = 2
fld public final static int TYPE_EDITOR = 1
fld public final static int TYPE_SLIDING = 2
fld public final static int TYPE_TOOLBAR = 3
fld public final static int TYPE_VIEW = 0
fld public final static java.lang.String COMMAND_CLOSE = "close"
fld public final static java.lang.String COMMAND_CLOSE_ALL = "closeAll"
fld public final static java.lang.String COMMAND_CLOSE_ALL_BUT_THIS = "closeAllButThis"
fld public final static java.lang.String COMMAND_CLOSE_GROUP = "closeGroup"
fld public final static java.lang.String COMMAND_DISABLE_AUTO_HIDE = "disableAutoHide"
fld public final static java.lang.String COMMAND_ENABLE_AUTO_HIDE = "enableAutoHide"
fld public final static java.lang.String COMMAND_MAXIMIZE = "maximize"
fld public final static java.lang.String COMMAND_MINIMIZE_GROUP = "minimizeGroup"
fld public final static java.lang.String COMMAND_POPUP_REQUEST = "popup"
fld public final static java.lang.String COMMAND_RESTORE_GROUP = "restoreGroup"
fld public final static java.lang.String COMMAND_SELECT = "select"
fld public final static java.lang.String COMMAND_TOGGLE_TRANSPARENCY = "toggleTransparency"
fld public final static java.lang.String PROP_ACTIVE = "active"
fld public final static java.lang.String PROP_MANAGE_TAB_POSITION = "manageTabPosition"
fld public final static java.lang.String TABBED_CONTAINER_UI_CLASS_ID = "TabbedContainerUI"
intf javax.accessibility.Accessible
meth protected final void postActionEvent(org.netbeans.swing.tabcontrol.event.TabActionEvent)
meth public boolean isPaintingOrigin()
meth public boolean isTransparent()
meth public boolean isValidateRoot()
meth public final boolean isActive()
meth public final boolean isShowCloseButton()
meth public final boolean requestAttention(org.netbeans.swing.tabcontrol.TabData)
meth public final int getType()
meth public final java.awt.Rectangle getTabRect(int,java.awt.Rectangle)
meth public final javax.swing.SingleSelectionModel getSelectionModel()
meth public final org.netbeans.swing.tabcontrol.ComponentConverter getComponentConverter()
meth public final org.netbeans.swing.tabcontrol.TabDataModel getModel()
meth public final void addActionListener(java.awt.event.ActionListener)
meth public final void cancelRequestAttention(int)
meth public final void cancelRequestAttention(org.netbeans.swing.tabcontrol.TabData)
meth public final void removeActionListener(java.awt.event.ActionListener)
meth public final void requestAttention(int)
meth public final void setActive(boolean)
meth public final void setAttentionHighlight(int,boolean)
meth public final void setAttentionHighlight(org.netbeans.swing.tabcontrol.TabData,boolean)
meth public final void setComponentConverter(org.netbeans.swing.tabcontrol.ComponentConverter)
meth public final void setContentPolicy(int)
meth public final void setShowCloseButton(boolean)
meth public int dropIndexOfPoint(java.awt.Point)
meth public int getContentPolicy()
meth public int getTabCount()
meth public int indexOf(java.awt.Component)
meth public int tabForCoordinate(java.awt.Point)
meth public java.awt.Image createImageOfTab(int)
meth public java.awt.Shape getDropIndication(java.lang.Object,java.awt.Point)
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public org.netbeans.swing.tabcontrol.LocationInformer getLocationInformer()
 anno 0 java.lang.Deprecated()
meth public org.netbeans.swing.tabcontrol.TabbedContainerUI getUI()
meth public org.netbeans.swing.tabcontrol.WinsysInfoForTabbed getWinsysInfo()
 anno 0 java.lang.Deprecated()
meth public org.netbeans.swing.tabcontrol.WinsysInfoForTabbedContainer getContainerWinsysInfo()
meth public void addNotify()
meth public void paint(java.awt.Graphics)
meth public void removeNotify()
meth public void setIconAt(int,javax.swing.Icon)
meth public void setTitleAt(int,java.lang.String)
meth public void setToolTipTextAt(int,java.lang.String)
meth public void setTransparent(boolean)
meth public void updateUI()
supr javax.swing.JComponent
hfds ALPHA_TRESHOLD,actionListenerList,active,awtListener,containerWinsysInfo,contentPolicy,converter,currentAlpha,inTransparentMode,initialized,locationInformer,model,type,winsysInfo

CLSS public abstract org.netbeans.swing.tabcontrol.TabbedContainerUI
cons public init(org.netbeans.swing.tabcontrol.TabbedContainer)
fld protected org.netbeans.swing.tabcontrol.TabbedContainer container
meth protected abstract void cancelRequestAttention(int)
meth protected abstract void requestAttention(int)
meth protected boolean uichange()
meth protected final boolean shouldPerformAction(java.lang.String,int,java.awt.event.MouseEvent)
meth protected void setAttentionHighlight(int,boolean)
meth public abstract boolean isShowCloseButton()
meth public abstract int dropIndexOfPoint(java.awt.Point)
meth public abstract int tabForCoordinate(java.awt.Point)
meth public abstract java.awt.Image createImageOfTab(int)
meth public abstract java.awt.Polygon getExactTabIndication(int)
meth public abstract java.awt.Polygon getInsertTabIndication(int)
meth public abstract java.awt.Rectangle getContentArea()
meth public abstract java.awt.Rectangle getTabRect(int,java.awt.Rectangle)
meth public abstract java.awt.Rectangle getTabsArea()
meth public abstract javax.swing.SingleSelectionModel getSelectionModel()
meth public abstract void makeTabVisible(int)
meth public abstract void setShowCloseButton(boolean)
meth public void installUI(javax.swing.JComponent)
supr javax.swing.plaf.ComponentUI

CLSS public abstract interface org.netbeans.swing.tabcontrol.WinsysInfoForTabbed
meth public abstract boolean inMaximizedMode(java.awt.Component)
meth public abstract java.lang.Object getOrientation(java.awt.Component)

CLSS public abstract org.netbeans.swing.tabcontrol.WinsysInfoForTabbedContainer
cons public init()
intf org.netbeans.swing.tabcontrol.WinsysInfoForTabbed
meth public boolean isModeSlidingEnabled()
meth public boolean isSlidedOutContainer()
meth public boolean isTopComponentBusy(org.openide.windows.TopComponent)
meth public boolean isTopComponentClosingEnabled()
meth public boolean isTopComponentClosingEnabled(org.openide.windows.TopComponent)
meth public boolean isTopComponentMaximizationEnabled()
meth public boolean isTopComponentMaximizationEnabled(org.openide.windows.TopComponent)
meth public boolean isTopComponentSlidingEnabled()
meth public boolean isTopComponentSlidingEnabled(org.openide.windows.TopComponent)
meth public static org.netbeans.swing.tabcontrol.WinsysInfoForTabbedContainer getDefault(org.netbeans.swing.tabcontrol.WinsysInfoForTabbed)
supr java.lang.Object
hcls DefaultWinsysInfoForTabbedContainer

CLSS public abstract org.netbeans.swing.tabcontrol.customtabs.Tabbed
cons public init()
innr public abstract interface static Accessor
meth public abstract boolean isTransparent()
meth public abstract int getTabCount()
meth public abstract int indexOf(java.awt.Component)
meth public abstract int tabForCoordinate(java.awt.Point)
meth public abstract java.awt.Component getComponent()
meth public abstract java.awt.Image createImageOfTab(int)
meth public abstract java.awt.Rectangle getTabBounds(int)
meth public abstract java.awt.Rectangle getTabsArea()
meth public abstract java.awt.Shape getIndicationForLocation(java.awt.Point,org.openide.windows.TopComponent,java.awt.Point,boolean)
meth public abstract java.lang.Object getConstraintForLocation(java.awt.Point,boolean)
meth public abstract javax.swing.Action[] getPopupActions(javax.swing.Action[],int)
meth public abstract org.openide.windows.TopComponent getSelectedTopComponent()
meth public abstract org.openide.windows.TopComponent getTopComponentAt(int)
meth public abstract org.openide.windows.TopComponent[] getTopComponents()
meth public abstract void addActionListener(java.awt.event.ActionListener)
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void addTopComponent(java.lang.String,javax.swing.Icon,org.openide.windows.TopComponent,java.lang.String)
meth public abstract void cancelRequestAttention(org.openide.windows.TopComponent)
meth public abstract void insertComponent(java.lang.String,javax.swing.Icon,java.awt.Component,java.lang.String,int)
meth public abstract void removeActionListener(java.awt.event.ActionListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeComponent(java.awt.Component)
meth public abstract void requestAttention(org.openide.windows.TopComponent)
meth public abstract void setActive(boolean)
meth public abstract void setIconAt(int,javax.swing.Icon)
meth public abstract void setSelectedComponent(java.awt.Component)
meth public abstract void setTitleAt(int,java.lang.String)
meth public abstract void setToolTipTextAt(int,java.lang.String)
meth public abstract void setTopComponents(org.openide.windows.TopComponent[],org.openide.windows.TopComponent)
meth public abstract void setTransparent(boolean)
meth public boolean isBusy(org.openide.windows.TopComponent)
meth public void makeBusy(org.openide.windows.TopComponent,boolean)
meth public void setAttentionHighlight(org.openide.windows.TopComponent,boolean)
supr java.lang.Object

CLSS public abstract interface static org.netbeans.swing.tabcontrol.customtabs.Tabbed$Accessor
 outer org.netbeans.swing.tabcontrol.customtabs.Tabbed
meth public abstract org.netbeans.swing.tabcontrol.customtabs.Tabbed getTabbed()

CLSS public abstract interface org.netbeans.swing.tabcontrol.customtabs.TabbedComponentFactory
meth public abstract org.netbeans.swing.tabcontrol.customtabs.Tabbed createTabbedComponent(org.netbeans.swing.tabcontrol.customtabs.TabbedType,org.netbeans.swing.tabcontrol.WinsysInfoForTabbedContainer)

CLSS public abstract !enum org.netbeans.swing.tabcontrol.customtabs.TabbedType
fld public final static org.netbeans.swing.tabcontrol.customtabs.TabbedType EDITOR
fld public final static org.netbeans.swing.tabcontrol.customtabs.TabbedType SLIDING
fld public final static org.netbeans.swing.tabcontrol.customtabs.TabbedType TOOLBAR
fld public final static org.netbeans.swing.tabcontrol.customtabs.TabbedType VIEW
meth public abstract int toInt()
meth public static org.netbeans.swing.tabcontrol.customtabs.TabbedType valueOf(java.lang.String)
meth public static org.netbeans.swing.tabcontrol.customtabs.TabbedType[] values()
supr java.lang.Enum<org.netbeans.swing.tabcontrol.customtabs.TabbedType>

CLSS public final org.netbeans.swing.tabcontrol.event.ArrayDiff
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public java.util.Set<java.lang.Integer> getAddedIndices()
meth public java.util.Set<java.lang.Integer> getChangedIndices()
meth public java.util.Set<java.lang.Integer> getDeletedIndices()
meth public java.util.Set<java.lang.Integer> getMovedIndices()
meth public org.netbeans.swing.tabcontrol.TabData[] getNewData()
meth public org.netbeans.swing.tabcontrol.TabData[] getOldData()
meth public static org.netbeans.swing.tabcontrol.event.ArrayDiff createDiff(org.netbeans.swing.tabcontrol.TabData[],org.netbeans.swing.tabcontrol.TabData[])
supr java.lang.Object
hfds added,deleted,nue,old

CLSS public org.netbeans.swing.tabcontrol.event.ComplexListDataEvent
cons public init(java.lang.Object,int,int,int)
cons public init(java.lang.Object,int,int,int,boolean)
cons public init(java.lang.Object,int,int,int,boolean,boolean)
cons public init(java.lang.Object,int,int[],boolean)
fld public final static int ITEMS_ADDED = 3
fld public final static int ITEMS_REMOVED = 4
meth public boolean isTextChanged()
meth public boolean isUserObjectChanged()
meth public int[] getIndices()
meth public java.lang.String toString()
meth public org.netbeans.swing.tabcontrol.TabData[] getAffectedItems()
meth public void setAffectedItems(org.netbeans.swing.tabcontrol.TabData[])
supr javax.swing.event.ListDataEvent
hfds ITEMS_CHANGED,LAST,affectedItems,componentChanged,indices,textChanged

CLSS public abstract interface org.netbeans.swing.tabcontrol.event.ComplexListDataListener
intf javax.swing.event.ListDataListener
meth public abstract void indicesAdded(org.netbeans.swing.tabcontrol.event.ComplexListDataEvent)
meth public abstract void indicesChanged(org.netbeans.swing.tabcontrol.event.ComplexListDataEvent)
meth public abstract void indicesRemoved(org.netbeans.swing.tabcontrol.event.ComplexListDataEvent)

CLSS public final org.netbeans.swing.tabcontrol.event.TabActionEvent
cons public init(java.lang.Object,java.lang.String,int)
cons public init(java.lang.Object,java.lang.String,int,java.awt.event.MouseEvent)
meth public boolean isConsumed()
meth public int getTabIndex()
meth public java.awt.event.MouseEvent getMouseEvent()
meth public java.lang.String getGroupName()
meth public java.lang.String toString()
meth public void consume()
meth public void setGroupName(java.lang.String)
meth public void setSource(java.lang.Object)
supr java.awt.event.ActionEvent
hfds groupName,mouseEvent,tabIndex

CLSS public final org.netbeans.swing.tabcontrol.event.VeryComplexListDataEvent
cons public init(java.lang.Object,org.netbeans.swing.tabcontrol.TabData[],org.netbeans.swing.tabcontrol.TabData[])
meth public java.lang.String toString()
meth public org.netbeans.swing.tabcontrol.event.ArrayDiff getDiff()
supr org.netbeans.swing.tabcontrol.event.ComplexListDataEvent
hfds nue,old

CLSS public abstract org.netbeans.swing.tabcontrol.plaf.AbstractTabCellRenderer
cons public init(org.netbeans.swing.tabcontrol.plaf.TabPainter,java.awt.Dimension)
cons public init(org.netbeans.swing.tabcontrol.plaf.TabPainter,org.netbeans.swing.tabcontrol.plaf.TabPainter,org.netbeans.swing.tabcontrol.plaf.TabPainter,java.awt.Dimension)
intf org.netbeans.swing.tabcontrol.plaf.TabCellRenderer
meth protected boolean inCloseButton()
meth protected final boolean isActive()
meth protected final boolean isArmed()
meth protected final boolean isAttention()
meth protected final boolean isBusy()
meth protected final boolean isClipLeft()
meth protected final boolean isClipRight()
meth protected final boolean isHighlight()
meth protected final boolean isLeftmost()
meth protected final boolean isNextTabArmed()
meth protected final boolean isNextTabSelected()
meth protected final boolean isPressed()
meth protected final boolean isPreviousTabSelected()
meth protected final boolean isRightmost()
meth protected final boolean isSelected()
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected final void setState(int)
meth protected int getCaptionYAdjustment()
meth protected int getCaptionYPosition(java.awt.Graphics)
meth protected int getIconYAdjustment()
meth protected int stateChanged(int,int)
meth protected void paintIconAndText(java.awt.Graphics)
meth public final boolean isShowCloseButton()
meth public final int getState()
meth public final javax.swing.JComponent getRendererComponent(org.netbeans.swing.tabcontrol.TabData,java.awt.Rectangle,int)
meth public final void addContainerListener(java.awt.event.ContainerListener)
meth public final void addHierarchyBoundsListener(java.awt.event.HierarchyBoundsListener)
meth public final void addHierarchyListener(java.awt.event.HierarchyListener)
meth public final void setShowCloseButton(boolean)
meth public int getPixelsToAddToSelection()
meth public java.awt.Color getSelectedActivatedBackground()
meth public java.awt.Color getSelectedActivatedForeground()
meth public java.awt.Color getSelectedBackground()
meth public java.awt.Color getSelectedForeground()
meth public java.awt.Dimension getPadding()
meth public java.awt.Polygon getTabShape(int,java.awt.Rectangle)
meth public java.lang.String getCommandAtPoint(java.awt.Point,int,java.awt.Rectangle)
meth public java.lang.String getCommandAtPoint(java.awt.Point,int,java.awt.Rectangle,int,int,int)
meth public void paintComponent(java.awt.Graphics)
meth public void repaint()
meth public void repaint(long)
meth public void repaint(long,int,int,int,int)
meth public void revalidate()
meth public void validate()
supr javax.swing.JLabel
hfds leftBorder,normalBorder,padding,rightBorder,scratch,showClose,state

CLSS public abstract org.netbeans.swing.tabcontrol.plaf.AbstractTabDisplayerUI
cons public init(org.netbeans.swing.tabcontrol.TabDisplayer)
fld protected java.awt.event.ComponentListener componentListener
fld protected java.awt.event.HierarchyListener hierarchyListener
fld protected java.awt.event.MouseListener mouseListener
fld protected java.beans.PropertyChangeListener propertyChangeListener
fld protected javax.swing.event.ChangeListener selectionListener
fld protected org.netbeans.swing.tabcontrol.plaf.AbstractTabDisplayerUI$ModelListener modelListener
fld protected org.netbeans.swing.tabcontrol.plaf.TabLayoutModel layoutModel
innr protected DisplayerHierarchyListener
innr protected DisplayerPropertyChangeListener
innr protected ModelListener
meth protected abstract java.awt.event.MouseListener createMouseListener()
meth protected abstract javax.swing.event.ChangeListener createSelectionListener()
meth protected abstract org.netbeans.swing.tabcontrol.plaf.TabLayoutModel createLayoutModel()
meth protected final void installListeners()
meth protected final void uninstallListeners()
meth protected java.awt.Font createFont()
meth protected java.awt.Point toDropPoint(java.awt.Point)
meth protected java.awt.event.ComponentListener createComponentListener()
meth protected java.awt.event.HierarchyListener createHierarchyListener()
meth protected java.beans.PropertyChangeListener createPropertyChangeListener()
meth protected javax.swing.SingleSelectionModel createSelectionModel()
meth protected org.netbeans.swing.tabcontrol.plaf.AbstractTabDisplayerUI$ModelListener createModelListener()
meth protected void install()
meth protected void modelChanged()
meth protected void uninstall()
meth public final void installUI(javax.swing.JComponent)
meth public final void uninstallUI(javax.swing.JComponent)
meth public int dropIndexOfPoint(java.awt.Point)
meth public void registerShortcuts(javax.swing.JComponent)
meth public void unregisterShortcuts(javax.swing.JComponent)
supr org.netbeans.swing.tabcontrol.TabDisplayerUI
hfds scratchPoint

CLSS protected org.netbeans.swing.tabcontrol.plaf.AbstractTabDisplayerUI$DisplayerHierarchyListener
 outer org.netbeans.swing.tabcontrol.plaf.AbstractTabDisplayerUI
cons public init(org.netbeans.swing.tabcontrol.plaf.AbstractTabDisplayerUI)
intf java.awt.event.HierarchyListener
meth public void hierarchyChanged(java.awt.event.HierarchyEvent)
supr java.lang.Object

CLSS protected org.netbeans.swing.tabcontrol.plaf.AbstractTabDisplayerUI$DisplayerPropertyChangeListener
 outer org.netbeans.swing.tabcontrol.plaf.AbstractTabDisplayerUI
cons protected init(org.netbeans.swing.tabcontrol.plaf.AbstractTabDisplayerUI)
intf java.beans.PropertyChangeListener
meth protected void activationChanged()
meth public void propertyChange(java.beans.PropertyChangeEvent)
supr java.lang.Object

CLSS protected org.netbeans.swing.tabcontrol.plaf.AbstractTabDisplayerUI$ModelListener
 outer org.netbeans.swing.tabcontrol.plaf.AbstractTabDisplayerUI
cons protected init(org.netbeans.swing.tabcontrol.plaf.AbstractTabDisplayerUI)
intf javax.swing.event.ChangeListener
intf org.netbeans.swing.tabcontrol.event.ComplexListDataListener
meth public final void stateChanged(javax.swing.event.ChangeEvent)
meth public void contentsChanged(javax.swing.event.ListDataEvent)
meth public void indicesAdded(org.netbeans.swing.tabcontrol.event.ComplexListDataEvent)
meth public void indicesChanged(org.netbeans.swing.tabcontrol.event.ComplexListDataEvent)
meth public void indicesRemoved(org.netbeans.swing.tabcontrol.event.ComplexListDataEvent)
meth public void intervalAdded(javax.swing.event.ListDataEvent)
meth public void intervalRemoved(javax.swing.event.ListDataEvent)
supr java.lang.Object
hfds checkVisible

CLSS public abstract org.netbeans.swing.tabcontrol.plaf.AbstractViewTabDisplayerUI
cons public init(org.netbeans.swing.tabcontrol.TabDisplayer)
fld protected final org.netbeans.swing.tabcontrol.plaf.TabState tabState
fld protected org.netbeans.swing.tabcontrol.plaf.AbstractViewTabDisplayerUI$Controller controller
innr protected Controller
meth protected abstract void paintTabBackground(java.awt.Graphics,int,int,int,int,int)
meth protected abstract void paintTabBorder(java.awt.Graphics,int,int,int,int,int)
meth protected abstract void paintTabContent(java.awt.Graphics,int,java.lang.String,int,int,int,int)
meth protected boolean isAttention(int)
meth protected boolean isHighlight(int)
meth protected final boolean isActive()
meth protected final boolean isFocused(int)
meth protected final boolean isSelected(int)
meth protected final java.awt.FontMetrics getTxtFontMetrics()
meth protected final javax.swing.SingleSelectionModel createSelectionModel()
meth protected final javax.swing.SingleSelectionModel getSelectionModel()
meth protected final org.netbeans.swing.tabcontrol.TabDataModel getDataModel()
meth protected final org.netbeans.swing.tabcontrol.TabDisplayer getDisplayer()
meth protected int createRepaintPolicy()
meth protected java.awt.Component getControlButtons()
meth protected java.awt.Font getTxtFont()
meth protected org.netbeans.swing.tabcontrol.plaf.AbstractViewTabDisplayerUI$Controller createController()
meth protected org.netbeans.swing.tabcontrol.plaf.TabLayoutModel createLayoutModel()
meth protected void cancelRequestAttention(int)
meth protected void installControlButtons()
meth protected void paintDisplayerBackground(java.awt.Graphics,javax.swing.JComponent)
meth protected void requestAttention(int)
meth protected void setAttentionHighlight(int,boolean)
meth public final org.netbeans.swing.tabcontrol.plaf.TabLayoutModel getLayoutModel()
meth public int dropIndexOfPoint(java.awt.Point)
meth public int tabForCoordinate(java.awt.Point)
meth public java.awt.Dimension getMinimumSize(javax.swing.JComponent)
meth public java.awt.Image createImageOfTab(int)
meth public java.awt.Polygon getExactTabIndication(int)
meth public java.awt.Polygon getInsertTabIndication(int)
meth public java.awt.Rectangle getTabRect(int,java.awt.Rectangle)
meth public org.netbeans.swing.tabcontrol.plaf.AbstractViewTabDisplayerUI$Controller getController()
meth public void installUI(javax.swing.JComponent)
meth public void paint(java.awt.Graphics,javax.swing.JComponent)
meth public void registerShortcuts(javax.swing.JComponent)
meth public void uninstallUI(javax.swing.JComponent)
meth public void unregisterShortcuts(javax.swing.JComponent)
supr org.netbeans.swing.tabcontrol.TabDisplayerUI
hfds ICON_X_PAD,PIN_ACTION,TRANSPARENCY_ACTION,btnAutoHidePin,btnClose,btnMinimizeMode,controlButtons,dataModel,fm,layoutModel,pinAction,txtFont
hcls PinAction,PinButtonLayout,ViewTabState

CLSS protected org.netbeans.swing.tabcontrol.plaf.AbstractViewTabDisplayerUI$Controller
 outer org.netbeans.swing.tabcontrol.plaf.AbstractViewTabDisplayerUI
cons protected init(org.netbeans.swing.tabcontrol.plaf.AbstractViewTabDisplayerUI)
intf java.awt.event.MouseMotionListener
intf java.beans.PropertyChangeListener
intf javax.swing.event.ChangeListener
intf org.netbeans.swing.tabcontrol.event.ComplexListDataListener
meth protected boolean shouldReact(java.awt.event.MouseEvent)
meth public boolean inControlButtonsRect(java.awt.Point)
meth public void contentsChanged(javax.swing.event.ListDataEvent)
meth public void indicesAdded(org.netbeans.swing.tabcontrol.event.ComplexListDataEvent)
meth public void indicesChanged(org.netbeans.swing.tabcontrol.event.ComplexListDataEvent)
meth public void indicesRemoved(org.netbeans.swing.tabcontrol.event.ComplexListDataEvent)
meth public void intervalAdded(javax.swing.event.ListDataEvent)
meth public void intervalRemoved(javax.swing.event.ListDataEvent)
meth public void mouseClicked(java.awt.event.MouseEvent)
meth public void mouseDragged(java.awt.event.MouseEvent)
meth public void mouseMoved(java.awt.event.MouseEvent)
meth public void mousePressed(java.awt.event.MouseEvent)
meth public void mouseReleased(java.awt.event.MouseEvent)
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void stateChanged(javax.swing.event.ChangeEvent)
supr java.awt.event.MouseAdapter
hfds selectionChanged

CLSS public org.netbeans.swing.tabcontrol.plaf.AquaEditorTabDisplayerUI
cons public init(org.netbeans.swing.tabcontrol.TabDisplayer)
meth protected boolean isAntialiased()
meth protected int createRepaintPolicy()
meth protected java.awt.Font createFont()
meth protected java.awt.Font getTxtFont()
meth protected org.netbeans.swing.tabcontrol.plaf.TabCellRenderer createDefaultRenderer()
meth protected void paintAfterTabs(java.awt.Graphics)
meth protected void paintBackground(java.awt.Graphics)
meth public java.awt.Dimension getPreferredSize(javax.swing.JComponent)
meth public javax.swing.Icon getButtonIcon(int,int)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
supr org.netbeans.swing.tabcontrol.plaf.BasicScrollingTabDisplayerUI
hfds buttonIconPaths,txtFont

CLSS public org.netbeans.swing.tabcontrol.plaf.AquaSlidingButtonUI
meth protected void paintIcon(java.awt.Graphics,javax.swing.AbstractButton,java.awt.Rectangle)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
meth public void installUI(javax.swing.JComponent)
supr org.netbeans.swing.tabcontrol.SlidingButtonUI
hfds AQUA_INSTANCE

CLSS public final org.netbeans.swing.tabcontrol.plaf.AquaVectorEditorTabDisplayerUI
cons public init(org.netbeans.swing.tabcontrol.TabDisplayer)
meth protected org.netbeans.swing.tabcontrol.plaf.TabCellRenderer createDefaultRenderer()
meth public javax.swing.Icon getButtonIcon(int,int)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
supr org.netbeans.swing.tabcontrol.plaf.AquaEditorTabDisplayerUI

CLSS public final org.netbeans.swing.tabcontrol.plaf.AquaVectorViewTabDisplayerUI
meth public javax.swing.Icon getButtonIcon(int,int)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
supr org.netbeans.swing.tabcontrol.plaf.AquaViewTabDisplayerUI

CLSS public org.netbeans.swing.tabcontrol.plaf.AquaViewTabDisplayerUI
cons protected init(org.netbeans.swing.tabcontrol.TabDisplayer)
meth protected org.netbeans.swing.tabcontrol.plaf.AbstractViewTabDisplayerUI$Controller createController()
meth protected void paintTabBackground(java.awt.Graphics,int,int,int,int,int)
meth protected void paintTabBorder(java.awt.Graphics,int,int,int,int,int)
meth protected void paintTabContent(java.awt.Graphics,int,java.lang.String,int,int,int,int)
meth public java.awt.Dimension getPreferredSize(javax.swing.JComponent)
meth public javax.swing.Icon getButtonIcon(int,int)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
supr org.netbeans.swing.tabcontrol.plaf.AbstractViewTabDisplayerUI
hfds ICON_X_PAD,TXT_X_PAD,buttonIconPaths,prefSize
hcls OwnController

CLSS public abstract org.netbeans.swing.tabcontrol.plaf.BasicScrollingTabDisplayerUI
cons public init(org.netbeans.swing.tabcontrol.TabDisplayer)
innr protected ScrollingDisplayerComponentListener
innr protected ScrollingHierarchyListener
innr protected ScrollingTabState
meth protected final int getFirstVisibleTab()
meth protected final int getLastVisibleTab()
meth protected final int getTabsAreaWidth()
meth protected final org.netbeans.swing.tabcontrol.plaf.ScrollingTabLayoutModel scroll()
meth protected java.awt.Component getControlButtons()
meth protected java.awt.LayoutManager createLayout()
meth protected java.awt.Rectangle getControlButtonsRectangle(java.awt.Container)
meth protected java.awt.event.ComponentListener createComponentListener()
meth protected java.awt.event.HierarchyListener createHierarchyListener()
meth protected org.netbeans.swing.tabcontrol.plaf.TabLayoutModel createLayoutModel()
meth protected org.netbeans.swing.tabcontrol.plaf.TabState createTabState()
meth protected void install()
meth protected void installControlButtons()
meth protected void modelChanged()
meth protected void processMouseWheelEvent(java.awt.event.MouseWheelEvent)
meth protected void uninstall()
meth public java.awt.Dimension getMinimumSize(javax.swing.JComponent)
meth public java.awt.Insets getAutoscrollInsets()
meth public java.awt.Insets getTabAreaInsets()
meth public static java.awt.Graphics2D getOffscreenGraphics()
meth public static java.awt.Graphics2D getOffscreenGraphics(javax.swing.JComponent)
meth public void autoscroll(java.awt.Point)
meth public void makeTabVisible(int)
supr org.netbeans.swing.tabcontrol.plaf.BasicTabDisplayerUI
hfds autoscroll,btnDropDown,btnMaximizeRestore,btnScrollLeft,btnScrollRight,cachedScratchGraphics,controlButtons,lastKnownModelSize,scratch
hcls Autoscroller,ScratchGraphics,WCLayout

CLSS protected org.netbeans.swing.tabcontrol.plaf.BasicScrollingTabDisplayerUI$ScrollingDisplayerComponentListener
 outer org.netbeans.swing.tabcontrol.plaf.BasicScrollingTabDisplayerUI
cons protected init(org.netbeans.swing.tabcontrol.plaf.BasicScrollingTabDisplayerUI)
meth public void componentResized(java.awt.event.ComponentEvent)
supr java.awt.event.ComponentAdapter

CLSS protected org.netbeans.swing.tabcontrol.plaf.BasicScrollingTabDisplayerUI$ScrollingHierarchyListener
 outer org.netbeans.swing.tabcontrol.plaf.BasicScrollingTabDisplayerUI
cons protected init(org.netbeans.swing.tabcontrol.plaf.BasicScrollingTabDisplayerUI)
meth public void hierarchyChanged(java.awt.event.HierarchyEvent)
supr org.netbeans.swing.tabcontrol.plaf.AbstractTabDisplayerUI$DisplayerHierarchyListener

CLSS protected org.netbeans.swing.tabcontrol.plaf.BasicScrollingTabDisplayerUI$ScrollingTabState
 outer org.netbeans.swing.tabcontrol.plaf.BasicScrollingTabDisplayerUI
cons protected init(org.netbeans.swing.tabcontrol.plaf.BasicScrollingTabDisplayerUI)
meth public int getState(int)
supr org.netbeans.swing.tabcontrol.plaf.BasicTabDisplayerUI$BasicTabState

CLSS public final org.netbeans.swing.tabcontrol.plaf.BasicSlidingTabDisplayerUI
cons public init(org.netbeans.swing.tabcontrol.TabDisplayer)
innr protected final SlidingPropertyChangeListener
innr public final IndexButton
meth protected java.awt.Font createFont()
meth protected java.awt.event.MouseListener createMouseListener()
meth protected javax.swing.event.ChangeListener createSelectionListener()
meth protected org.netbeans.swing.tabcontrol.plaf.TabLayoutModel createLayoutModel()
meth protected void install()
meth protected void modelChanged()
meth protected void uninstall()
meth public int tabForCoordinate(java.awt.Point)
meth public java.awt.Dimension getMinimumSize(javax.swing.JComponent)
meth public java.awt.Dimension getPreferredSize(javax.swing.JComponent)
meth public java.awt.Image createImageOfTab(int)
meth public java.awt.Polygon getExactTabIndication(int)
meth public java.awt.Polygon getInsertTabIndication(int)
meth public java.awt.Rectangle getTabRect(int,java.awt.Rectangle)
meth public javax.swing.Icon getButtonIcon(int,int)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
meth public void cancelRequestAttention(int)
meth public void requestAttention(int)
supr org.netbeans.swing.tabcontrol.plaf.AbstractTabDisplayerUI
hfds BUTTON_COMPARATOR,buttonCount,scratch
hcls IndexButtonComparator,OrientedLayoutManager

CLSS public final org.netbeans.swing.tabcontrol.plaf.BasicSlidingTabDisplayerUI$IndexButton
 outer org.netbeans.swing.tabcontrol.plaf.BasicSlidingTabDisplayerUI
cons public init(org.netbeans.swing.tabcontrol.plaf.BasicSlidingTabDisplayerUI,int)
fld public final static java.lang.String UI_KEY = "IndexButtonUI"
intf java.awt.event.ActionListener
meth public boolean isActive()
meth public final void actionPerformed(java.awt.event.ActionEvent)
meth public int getIndex()
meth public java.lang.Object getOrientation()
meth public java.lang.String getText()
meth public java.lang.String getToolTipText()
meth public java.lang.String getUIClassID()
meth public javax.swing.Icon getIcon()
meth public void addNotify()
meth public void removeNotify()
meth public void updateUI()
supr javax.swing.JToggleButton
hfds index,lastKnownIcon,lastKnownText

CLSS protected final org.netbeans.swing.tabcontrol.plaf.BasicSlidingTabDisplayerUI$SlidingPropertyChangeListener
 outer org.netbeans.swing.tabcontrol.plaf.BasicSlidingTabDisplayerUI
cons protected init(org.netbeans.swing.tabcontrol.plaf.BasicSlidingTabDisplayerUI)
meth public void propertyChange(java.beans.PropertyChangeEvent)
supr org.netbeans.swing.tabcontrol.plaf.AbstractTabDisplayerUI$DisplayerPropertyChangeListener

CLSS public abstract org.netbeans.swing.tabcontrol.plaf.BasicTabDisplayerUI
cons public init(org.netbeans.swing.tabcontrol.TabDisplayer)
fld protected int repaintPolicy
fld protected org.netbeans.swing.tabcontrol.plaf.TabCellRenderer defaultRenderer
fld protected org.netbeans.swing.tabcontrol.plaf.TabState tabState
innr protected BasicDisplayerMouseListener
innr protected BasicModelListener
innr protected BasicSelectionListener
innr protected BasicTabState
meth protected abstract org.netbeans.swing.tabcontrol.plaf.TabCellRenderer createDefaultRenderer()
meth protected boolean isAntialiased()
 anno 0 java.lang.Deprecated()
meth protected final java.awt.Point getLastKnownMouseLocation()
meth protected final void cancelRequestAttention(int)
meth protected final void getTabsVisibleArea(java.awt.Rectangle)
meth protected final void requestAttention(int)
meth protected final void setAttentionHighlight(int,boolean)
meth protected int createRepaintPolicy()
meth protected int getFirstVisibleTab()
meth protected int getLastVisibleTab()
meth protected java.awt.Rectangle getTabRectForRepaint(int,java.awt.Rectangle)
meth protected java.awt.event.MouseListener createMouseListener()
meth protected java.beans.PropertyChangeListener createPropertyChangeListener()
meth protected javax.swing.event.ChangeListener createSelectionListener()
meth protected org.netbeans.swing.tabcontrol.plaf.AbstractTabDisplayerUI$ModelListener createModelListener()
meth protected org.netbeans.swing.tabcontrol.plaf.TabState createTabState()
meth protected void install()
meth protected void modelChanged()
meth protected void paintAfterTabs(java.awt.Graphics)
meth protected void paintBackground(java.awt.Graphics)
meth protected void processMouseWheelEvent(java.awt.event.MouseWheelEvent)
meth protected void uninstall()
meth public abstract java.awt.Insets getTabAreaInsets()
meth public final void paint(java.awt.Graphics,javax.swing.JComponent)
meth public int dropIndexOfPoint(java.awt.Point)
meth public int tabForCoordinate(java.awt.Point)
meth public java.awt.Image createImageOfTab(int)
meth public java.awt.Polygon getExactTabIndication(int)
meth public java.awt.Polygon getInsertTabIndication(int)
meth public java.awt.Rectangle getTabRect(int,java.awt.Rectangle)
meth public org.netbeans.swing.tabcontrol.plaf.TabCellRenderer getTabCellRenderer(int)
supr org.netbeans.swing.tabcontrol.plaf.AbstractTabDisplayerUI
hfds lastKnownMouseLocation,pixelsToAdd,scratch,scratch2,scratch3,swingpainting
hcls BasicDisplayerPropertyChangeListener

CLSS protected org.netbeans.swing.tabcontrol.plaf.BasicTabDisplayerUI$BasicDisplayerMouseListener
 outer org.netbeans.swing.tabcontrol.plaf.BasicTabDisplayerUI
cons protected init(org.netbeans.swing.tabcontrol.plaf.BasicTabDisplayerUI)
intf java.awt.event.MouseListener
intf java.awt.event.MouseMotionListener
intf java.awt.event.MouseWheelListener
meth public final void mouseWheelMoved(java.awt.event.MouseWheelEvent)
meth public void mouseClicked(java.awt.event.MouseEvent)
meth public void mouseDragged(java.awt.event.MouseEvent)
meth public void mouseEntered(java.awt.event.MouseEvent)
meth public void mouseExited(java.awt.event.MouseEvent)
meth public void mouseMoved(java.awt.event.MouseEvent)
meth public void mousePressed(java.awt.event.MouseEvent)
meth public void mouseReleased(java.awt.event.MouseEvent)
supr java.lang.Object
hfds lastPressedTab,pressTime

CLSS protected org.netbeans.swing.tabcontrol.plaf.BasicTabDisplayerUI$BasicModelListener
 outer org.netbeans.swing.tabcontrol.plaf.BasicTabDisplayerUI
cons protected init(org.netbeans.swing.tabcontrol.plaf.BasicTabDisplayerUI)
meth public void contentsChanged(javax.swing.event.ListDataEvent)
meth public void indicesAdded(org.netbeans.swing.tabcontrol.event.ComplexListDataEvent)
meth public void indicesChanged(org.netbeans.swing.tabcontrol.event.ComplexListDataEvent)
meth public void indicesRemoved(org.netbeans.swing.tabcontrol.event.ComplexListDataEvent)
meth public void intervalAdded(javax.swing.event.ListDataEvent)
meth public void intervalRemoved(javax.swing.event.ListDataEvent)
supr org.netbeans.swing.tabcontrol.plaf.AbstractTabDisplayerUI$ModelListener

CLSS protected org.netbeans.swing.tabcontrol.plaf.BasicTabDisplayerUI$BasicSelectionListener
 outer org.netbeans.swing.tabcontrol.plaf.BasicTabDisplayerUI
cons protected init(org.netbeans.swing.tabcontrol.plaf.BasicTabDisplayerUI)
intf javax.swing.event.ChangeListener
meth public void stateChanged(javax.swing.event.ChangeEvent)
supr java.lang.Object

CLSS protected org.netbeans.swing.tabcontrol.plaf.BasicTabDisplayerUI$BasicTabState
 outer org.netbeans.swing.tabcontrol.plaf.BasicTabDisplayerUI
cons protected init(org.netbeans.swing.tabcontrol.plaf.BasicTabDisplayerUI)
meth protected void repaintAllTabs()
meth protected void repaintTab(int)
meth public int getRepaintPolicy(int)
meth public int getState(int)
supr org.netbeans.swing.tabcontrol.plaf.TabState

CLSS public abstract org.netbeans.swing.tabcontrol.plaf.BusyTabsSupport
cons public init()
innr public final static DefaultBusyTabsSupport
meth protected abstract int getRepaintTimerIntervalMillis()
meth protected abstract void tick()
meth public abstract javax.swing.Icon getBusyIcon(boolean)
meth public final void install(org.netbeans.swing.tabcontrol.customtabs.Tabbed,org.netbeans.swing.tabcontrol.TabDataModel)
meth public final void makeTabBusy(org.netbeans.swing.tabcontrol.customtabs.Tabbed,int,boolean)
meth public final void uninstall(org.netbeans.swing.tabcontrol.customtabs.Tabbed,org.netbeans.swing.tabcontrol.TabDataModel)
meth public static org.netbeans.swing.tabcontrol.plaf.BusyTabsSupport getDefault()
supr java.lang.Object
hfds animationTimer,busyContainers,containers,modelListener

CLSS public final static org.netbeans.swing.tabcontrol.plaf.BusyTabsSupport$DefaultBusyTabsSupport
 outer org.netbeans.swing.tabcontrol.plaf.BusyTabsSupport
cons public init()
meth protected int getRepaintTimerIntervalMillis()
meth protected void tick()
meth public javax.swing.Icon getBusyIcon(boolean)
supr org.netbeans.swing.tabcontrol.plaf.BusyTabsSupport
hfds busyIconDefault,busyIconSelected

CLSS public org.netbeans.swing.tabcontrol.plaf.ChicletWrapper
 anno 0 java.lang.Deprecated()
cons public init()
intf java.lang.Runnable
meth public java.lang.Long hash()
meth public void draw(java.awt.Graphics)
meth public void run()
meth public void setAllowVertical(boolean)
meth public void setArcs(float,float,float,float)
meth public void setBounds(int,int,int,int)
meth public void setNotch(boolean,boolean)
meth public void setState(int)
supr java.lang.Object
hfds allowVertical,arcs,bounds,cache,chiclet,drawCount,leftNotch,rightNotch,state
hcls CacheEntry

CLSS public final org.netbeans.swing.tabcontrol.plaf.DefaultTabLayoutModel
cons public init(org.netbeans.swing.tabcontrol.TabDataModel,javax.swing.JComponent)
fld protected int padX
fld protected int padY
fld protected int textHeight
fld protected javax.swing.JComponent renderTarget
fld protected org.netbeans.swing.tabcontrol.TabDataModel model
intf org.netbeans.swing.tabcontrol.plaf.TabLayoutModel
meth protected int iconHeight(int)
meth protected int iconWidth(int)
meth protected int textHeight(int,javax.swing.JComponent)
meth protected int textWidth(int)
meth public int dropIndexOfPoint(int,int)
meth public int getH(int)
meth public int getW(int)
meth public int getX(int)
meth public int getY(int)
meth public int indexOfPoint(int,int)
meth public void setPadding(java.awt.Dimension)
supr java.lang.Object

CLSS public org.netbeans.swing.tabcontrol.plaf.DefaultTabbedContainerUI
cons public init(org.netbeans.swing.tabcontrol.TabbedContainer)
fld protected java.awt.LayoutManager contentDisplayerLayout
fld protected java.awt.event.ComponentListener componentListener
fld protected java.beans.PropertyChangeListener propertyChangeListener
fld protected javax.swing.JComponent contentDisplayer
fld protected javax.swing.event.ChangeListener selectionListener
fld protected org.netbeans.swing.tabcontrol.TabDisplayer tabDisplayer
fld protected org.netbeans.swing.tabcontrol.event.ComplexListDataListener modelListener
fld protected org.netbeans.swing.tabcontrol.plaf.FxProvider slideEffectManager
fld public final static java.lang.String KEY_EDITOR_CONTENT_BORDER = "TabbedContainer.editor.contentBorder"
fld public final static java.lang.String KEY_EDITOR_OUTER_BORDER = "TabbedContainer.editor.outerBorder"
fld public final static java.lang.String KEY_EDITOR_TABS_BORDER = "TabbedContainer.editor.tabsBorder"
fld public final static java.lang.String KEY_SLIDING_CONTENT_BORDER = "TabbedContainer.sliding.contentBorder"
fld public final static java.lang.String KEY_SLIDING_OUTER_BORDER = "TabbedContainer.sliding.outerBorder"
fld public final static java.lang.String KEY_SLIDING_TABS_BORDER = "TabbedContainer.sliding.tabsBorder"
fld public final static java.lang.String KEY_TOOLBAR_CONTENT_BORDER = "TabbedContainer.toolbar.contentBorder"
fld public final static java.lang.String KEY_TOOLBAR_OUTER_BORDER = "TabbedContainer.toolbar.outerBorder"
fld public final static java.lang.String KEY_TOOLBAR_TABS_BORDER = "TabbedContainer.toolbar.tabsBorder"
fld public final static java.lang.String KEY_VIEW_CONTENT_BORDER = "TabbedContainer.view.contentBorder"
fld public final static java.lang.String KEY_VIEW_OUTER_BORDER = "TabbedContainer.view.outerBorder"
fld public final static java.lang.String KEY_VIEW_TABS_BORDER = "TabbedContainer.view.tabsBorder"
innr protected ContainerComponentListener
innr protected ContainerPropertyChangeListener
innr protected ModelListener
innr protected SelectionListener
meth protected boolean uichange()
meth protected final java.awt.Component toComp(org.netbeans.swing.tabcontrol.TabData)
meth protected final void showComponentWithFxProvider(java.awt.Component)
meth protected final void updateOrientation()
meth protected java.awt.Component showComponent(java.awt.Component)
meth protected java.awt.LayoutManager createContentDisplayerLayout()
meth protected java.awt.LayoutManager createLayout()
meth protected java.awt.event.ComponentListener createComponentListener()
meth protected java.beans.PropertyChangeListener createPropertyChangeListener()
meth protected javax.swing.JPanel createContentDisplayer()
meth protected javax.swing.event.ChangeListener createSelectionListener()
meth protected org.netbeans.swing.tabcontrol.TabDisplayer createTabDisplayer()
meth protected org.netbeans.swing.tabcontrol.event.ComplexListDataListener createModelListener()
meth protected org.netbeans.swing.tabcontrol.plaf.FxProvider createFxProvider()
meth protected void attachModelAndSelectionListeners()
meth protected void cancelRequestAttention(int)
meth protected void detachModelAndSelectionListeners()
meth protected void ensureSelectedComponentIsShowing()
meth protected void initDisplayer()
meth protected void install()
meth protected void installBorders()
meth protected void installContentDisplayer()
meth protected void installListeners()
meth protected void installTabDisplayer()
meth protected void requestAttention(int)
meth protected void setAttentionHighlight(int,boolean)
meth protected void uninstall()
meth protected void uninstallDisplayers()
meth protected void uninstallListeners()
meth public boolean isShowCloseButton()
meth public final void installUI(javax.swing.JComponent)
meth public final void uninstallUI(javax.swing.JComponent)
meth public int dropIndexOfPoint(java.awt.Point)
meth public int tabForCoordinate(java.awt.Point)
meth public java.awt.Image createImageOfTab(int)
meth public java.awt.Polygon getExactTabIndication(int)
meth public java.awt.Polygon getInsertTabIndication(int)
meth public java.awt.Rectangle getContentArea()
meth public java.awt.Rectangle getTabRect(int,java.awt.Rectangle)
meth public java.awt.Rectangle getTabsArea()
meth public java.awt.Rectangle getTabsArea(java.awt.Rectangle)
meth public javax.swing.SingleSelectionModel getSelectionModel()
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
meth public void makeTabVisible(int)
meth public void setShowCloseButton(boolean)
supr org.netbeans.swing.tabcontrol.TabbedContainerUI
hfds ADD_TO_GLASSPANE,EFFECTS_EVERYWHERE,INCREMENT,NO_EFFECTS,NO_SCALE,SYNCHRONOUS_PAINTING,TIMER,USE_ALPHA,USE_SWINGPAINTING,actionListener,bug4924561knownShowing,forward,hierarchyListener,scratchPoint
hcls ContainerHierarchyListener,DefaultWindowBorder,DisplayerActionListener,ForwardingMouseListener,ImageSlideFxProvider,LiveComponentSlideFxProvider,NoOpFxProvider,SlidingTabsLayout,ToolbarTabsLayout

CLSS protected org.netbeans.swing.tabcontrol.plaf.DefaultTabbedContainerUI$ContainerComponentListener
 outer org.netbeans.swing.tabcontrol.plaf.DefaultTabbedContainerUI
cons public init(org.netbeans.swing.tabcontrol.plaf.DefaultTabbedContainerUI)
meth public void componentMoved(java.awt.event.ComponentEvent)
meth public void componentResized(java.awt.event.ComponentEvent)
supr java.awt.event.ComponentAdapter

CLSS protected org.netbeans.swing.tabcontrol.plaf.DefaultTabbedContainerUI$ContainerPropertyChangeListener
 outer org.netbeans.swing.tabcontrol.plaf.DefaultTabbedContainerUI
cons protected init(org.netbeans.swing.tabcontrol.plaf.DefaultTabbedContainerUI)
intf java.beans.PropertyChangeListener
meth public void propertyChange(java.beans.PropertyChangeEvent)
supr java.lang.Object

CLSS protected org.netbeans.swing.tabcontrol.plaf.DefaultTabbedContainerUI$ModelListener
 outer org.netbeans.swing.tabcontrol.plaf.DefaultTabbedContainerUI
cons public init(org.netbeans.swing.tabcontrol.plaf.DefaultTabbedContainerUI)
intf org.netbeans.swing.tabcontrol.event.ComplexListDataListener
meth public void contentsChanged(javax.swing.event.ListDataEvent)
meth public void indicesAdded(org.netbeans.swing.tabcontrol.event.ComplexListDataEvent)
meth public void indicesChanged(org.netbeans.swing.tabcontrol.event.ComplexListDataEvent)
meth public void indicesRemoved(org.netbeans.swing.tabcontrol.event.ComplexListDataEvent)
meth public void intervalAdded(javax.swing.event.ListDataEvent)
meth public void intervalRemoved(javax.swing.event.ListDataEvent)
supr java.lang.Object

CLSS protected org.netbeans.swing.tabcontrol.plaf.DefaultTabbedContainerUI$SelectionListener
 outer org.netbeans.swing.tabcontrol.plaf.DefaultTabbedContainerUI
cons public init(org.netbeans.swing.tabcontrol.plaf.DefaultTabbedContainerUI)
intf javax.swing.event.ChangeListener
meth public void stateChanged(javax.swing.event.ChangeEvent)
supr java.lang.Object

CLSS public final org.netbeans.swing.tabcontrol.plaf.EqualPolygon
cons public init()
cons public init(int[],int[])
cons public init(int[],int[],int)
cons public init(java.awt.Polygon)
cons public init(java.awt.Rectangle)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.Object clone()
meth public java.lang.String toString()
meth public void lineTo(int,int)
meth public void moveTo(int,int)
supr java.awt.Polygon
hfds comparator
hcls PointsComparator

CLSS public abstract org.netbeans.swing.tabcontrol.plaf.FxProvider
cons public init()
fld protected java.lang.Object orientation
fld protected javax.swing.JComponent comp
fld protected javax.swing.JRootPane root
meth protected abstract void doFinish()
meth protected abstract void doStart()
meth public abstract void cleanup()
meth public final boolean isRunning()
meth public final void abort()
meth public final void finish()
meth public final void start(javax.swing.JComponent,javax.swing.JRootPane,java.lang.Object)
supr java.lang.Object
hfds running

CLSS public final org.netbeans.swing.tabcontrol.plaf.GtkEditorTabDisplayerUI
cons public init(org.netbeans.swing.tabcontrol.TabDisplayer)
meth protected java.awt.Rectangle getControlButtonsRectangle(java.awt.Container)
meth protected org.netbeans.swing.tabcontrol.plaf.TabCellRenderer createDefaultRenderer()
meth protected void paintAfterTabs(java.awt.Graphics)
meth public java.awt.Dimension getPreferredSize(javax.swing.JComponent)
meth public java.awt.Insets getTabAreaInsets()
meth public java.awt.Rectangle getTabRect(int,java.awt.Rectangle)
meth public javax.swing.Icon getButtonIcon(int,int)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
meth public void install()
supr org.netbeans.swing.tabcontrol.plaf.BasicScrollingTabDisplayerUI
hfds buttonIconPaths

CLSS public org.netbeans.swing.tabcontrol.plaf.GtkSlidingButtonUI
fld protected javax.swing.JToggleButton hiddenToggle
meth protected void paintBackground(java.awt.Graphics2D,javax.swing.AbstractButton)
meth protected void paintButtonPressed(java.awt.Graphics,javax.swing.AbstractButton)
meth protected void uninstallDefaults(javax.swing.AbstractButton)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
meth public void installDefaults(javax.swing.AbstractButton)
meth public void paint(java.awt.Graphics,javax.swing.JComponent)
supr org.netbeans.swing.tabcontrol.SlidingButtonUI
hfds INSTANCE,defaults_initialized

CLSS public final org.netbeans.swing.tabcontrol.plaf.GtkViewTabDisplayerUI
meth protected java.awt.Font getTxtFont()
meth protected void paintOverallBorder(java.awt.Graphics,javax.swing.JComponent)
meth protected void paintTabBackground(java.awt.Graphics,int,int,int,int,int)
meth protected void paintTabBorder(java.awt.Graphics,int,int,int,int,int)
meth protected void paintTabContent(java.awt.Graphics,int,java.lang.String,int,int,int,int)
meth public java.awt.Dimension getPreferredSize(javax.swing.JComponent)
meth public javax.swing.Icon getButtonIcon(int,int)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
meth public void paint(java.awt.Graphics,javax.swing.JComponent)
supr org.netbeans.swing.tabcontrol.plaf.AbstractViewTabDisplayerUI
hfds BUMP_WIDTH,BUMP_X_PAD,ICON_X_PAD,LOG,TXT_X_PAD,TXT_Y_PAD,buttonIconPaths,dummyTab,prefSize,tempRect

CLSS public final org.netbeans.swing.tabcontrol.plaf.MetalEditorTabDisplayerUI
cons public init(org.netbeans.swing.tabcontrol.TabDisplayer)
meth protected int createRepaintPolicy()
meth protected java.awt.Rectangle getControlButtonsRectangle(java.awt.Container)
meth protected org.netbeans.swing.tabcontrol.plaf.TabCellRenderer createDefaultRenderer()
meth protected void paintAfterTabs(java.awt.Graphics)
meth public java.awt.Dimension getMinimumSize(javax.swing.JComponent)
meth public java.awt.Dimension getPreferredSize(javax.swing.JComponent)
meth public java.awt.Insets getTabAreaInsets()
meth public javax.swing.Icon getButtonIcon(int,int)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
meth public void install()
supr org.netbeans.swing.tabcontrol.plaf.BasicScrollingTabDisplayerUI
hfds buttonIconPaths,scratch

CLSS public org.netbeans.swing.tabcontrol.plaf.MetalSlidingButtonUI
fld protected javax.swing.JToggleButton hiddenToggle
meth protected void paintBackground(java.awt.Graphics2D,javax.swing.AbstractButton)
meth protected void paintButtonPressed(java.awt.Graphics,javax.swing.AbstractButton)
meth protected void uninstallDefaults(javax.swing.AbstractButton)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
meth public void installDefaults(javax.swing.AbstractButton)
meth public void paint(java.awt.Graphics,javax.swing.JComponent)
supr org.netbeans.swing.tabcontrol.SlidingButtonUI
hfds INSTANCE,defaults_initialized

CLSS public final org.netbeans.swing.tabcontrol.plaf.MetalViewTabDisplayerUI
meth protected void paintTabBackground(java.awt.Graphics,int,int,int,int,int)
meth protected void paintTabBorder(java.awt.Graphics,int,int,int,int,int)
meth protected void paintTabContent(java.awt.Graphics,int,java.lang.String,int,int,int,int)
meth public java.awt.Dimension getPreferredSize(javax.swing.JComponent)
meth public javax.swing.Icon getButtonIcon(int,int)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
meth public void paint(java.awt.Graphics,javax.swing.JComponent)
supr org.netbeans.swing.tabcontrol.plaf.AbstractViewTabDisplayerUI
hfds BUMP_X_PAD,BUMP_Y_PAD,ICON_X_LEFT_PAD,ICON_X_RIGHT_PAD,TXT_X_PAD,actBgColor,borderHighlight,borderShadow,buttonIconPaths,inactBgColor,prefSize,tempRect

CLSS public final org.netbeans.swing.tabcontrol.plaf.NimbusEditorTabDisplayerUI
cons public init(org.netbeans.swing.tabcontrol.TabDisplayer)
meth protected java.awt.Rectangle getControlButtonsRectangle(java.awt.Container)
meth protected org.netbeans.swing.tabcontrol.plaf.TabCellRenderer createDefaultRenderer()
meth protected void paintAfterTabs(java.awt.Graphics)
meth protected void paintBackground(java.awt.Graphics)
meth public java.awt.Dimension getPreferredSize(javax.swing.JComponent)
meth public java.awt.Insets getTabAreaInsets()
meth public java.awt.Rectangle getTabRect(int,java.awt.Rectangle)
meth public javax.swing.Icon getButtonIcon(int,int)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
meth public void install()
supr org.netbeans.swing.tabcontrol.plaf.BasicScrollingTabDisplayerUI
hfds buttonIconPaths

CLSS public org.netbeans.swing.tabcontrol.plaf.NimbusSlidingButtonUI
fld protected javax.swing.JToggleButton hiddenToggle
meth protected void paintBackground(java.awt.Graphics2D,javax.swing.AbstractButton)
meth protected void paintButtonPressed(java.awt.Graphics,javax.swing.AbstractButton)
meth protected void uninstallDefaults(javax.swing.AbstractButton)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
meth public void installDefaults(javax.swing.AbstractButton)
meth public void paint(java.awt.Graphics,javax.swing.JComponent)
supr org.netbeans.swing.tabcontrol.SlidingButtonUI
hfds INSTANCE,defaults_initialized

CLSS public final org.netbeans.swing.tabcontrol.plaf.NimbusViewTabDisplayerUI
meth protected java.awt.Font getTxtFont()
meth protected void paintDisplayerBackground(java.awt.Graphics,javax.swing.JComponent)
meth protected void paintOverallBorder(java.awt.Graphics,javax.swing.JComponent)
meth protected void paintTabBackground(java.awt.Graphics,int,int,int,int,int)
meth protected void paintTabBorder(java.awt.Graphics,int,int,int,int,int)
meth protected void paintTabContent(java.awt.Graphics,int,java.lang.String,int,int,int,int)
meth public java.awt.Dimension getPreferredSize(javax.swing.JComponent)
meth public javax.swing.Icon getButtonIcon(int,int)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
meth public void paint(java.awt.Graphics,javax.swing.JComponent)
supr org.netbeans.swing.tabcontrol.plaf.AbstractViewTabDisplayerUI
hfds BUMP_WIDTH,BUMP_X_PAD,ICON_X_PAD,TXT_X_PAD,TXT_Y_PAD,buttonIconPaths,prefSize,tempRect

CLSS public final org.netbeans.swing.tabcontrol.plaf.ScrollingTabLayoutModel
cons public init(org.netbeans.swing.tabcontrol.plaf.TabLayoutModel,javax.swing.SingleSelectionModel,org.netbeans.swing.tabcontrol.TabDataModel)
cons public init(org.netbeans.swing.tabcontrol.plaf.TabLayoutModel,javax.swing.SingleSelectionModel,org.netbeans.swing.tabcontrol.TabDataModel,int)
intf org.netbeans.swing.tabcontrol.plaf.TabLayoutModel
meth public boolean isLastTabClipped()
meth public boolean makeVisible(int,int)
meth public int countVisibleTabs(int)
meth public int dropIndexOfPoint(int,int)
meth public int getFirstVisibleTab(int)
meth public int getH(int)
meth public int getLastVisibleTab(int)
meth public int getOffset()
meth public int getPixelsToAddToSelection()
meth public int getW(int)
meth public int getX(int)
meth public int getY(int)
meth public int indexOfPoint(int,int)
meth public int setOffset(int)
meth public javax.swing.Action getBackwardAction()
meth public javax.swing.Action getForwardAction()
meth public void clearCachedData()
meth public void setMinimumXposition(int)
meth public void setPadding(java.awt.Dimension)
meth public void setPixelsToAddToSelection(int)
meth public void setWidth(int)
supr java.lang.Object
hfds bAction,changed,fAction,firstVisibleTab,lastTabClipped,lastVisibleTab,makeVisibleTab,mdl,minimumXposition,offset,pixelsToAddToSelection,recentlyResized,sel,width,widths,wrapped
hcls BackwardAction,ForwardAction

CLSS public org.netbeans.swing.tabcontrol.plaf.SlidingTabDisplayerButtonUI
cons protected init()
innr public final static Aqua
meth protected final void paintIconAndText(java.awt.Graphics2D,org.netbeans.swing.tabcontrol.plaf.BasicSlidingTabDisplayerUI$IndexButton,java.lang.Object)
meth protected void installBorder(javax.swing.AbstractButton)
meth protected void paintBackground(java.awt.Graphics2D,org.netbeans.swing.tabcontrol.plaf.BasicSlidingTabDisplayerUI$IndexButton)
meth public final void paint(java.awt.Graphics,javax.swing.JComponent)
meth public java.awt.Dimension getMaximumSize(javax.swing.JComponent)
meth public java.awt.Dimension getMinimumSize(javax.swing.JComponent)
meth public java.awt.Dimension getPreferredSize(javax.swing.JComponent)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
meth public void installDefaults(javax.swing.AbstractButton)
meth public void installUI(javax.swing.JComponent)
meth public void uninstallUI(javax.swing.JComponent)
supr javax.swing.plaf.basic.BasicToggleButtonUI
hfds AQUA_INSTANCE,INSTANCE

CLSS public final static org.netbeans.swing.tabcontrol.plaf.SlidingTabDisplayerButtonUI$Aqua
 outer org.netbeans.swing.tabcontrol.plaf.SlidingTabDisplayerButtonUI
 anno 0 java.lang.Deprecated()
cons public init()
meth protected void installBorder(javax.swing.AbstractButton)
meth protected void paintBackground(java.awt.Graphics2D,org.netbeans.swing.tabcontrol.plaf.BasicSlidingTabDisplayerUI$IndexButton)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
supr org.netbeans.swing.tabcontrol.plaf.SlidingTabDisplayerButtonUI

CLSS public abstract interface org.netbeans.swing.tabcontrol.plaf.TabCellRenderer
meth public abstract boolean isShowCloseButton()
meth public abstract int getPixelsToAddToSelection()
meth public abstract java.awt.Color getSelectedActivatedBackground()
meth public abstract java.awt.Color getSelectedBackground()
meth public abstract java.awt.Dimension getPadding()
meth public abstract java.awt.Polygon getTabShape(int,java.awt.Rectangle)
meth public abstract java.lang.String getCommandAtPoint(java.awt.Point,int,java.awt.Rectangle)
meth public abstract java.lang.String getCommandAtPoint(java.awt.Point,int,java.awt.Rectangle,int,int,int)
meth public abstract javax.swing.JComponent getRendererComponent(org.netbeans.swing.tabcontrol.TabData,java.awt.Rectangle,int)
meth public abstract void setShowCloseButton(boolean)

CLSS public abstract org.netbeans.swing.tabcontrol.plaf.TabControlButton
fld protected final org.netbeans.swing.tabcontrol.TabDisplayer displayer
fld public final static int ID_CLOSE_BUTTON = 1
fld public final static int ID_DROP_DOWN_BUTTON = 8
fld public final static int ID_MAXIMIZE_BUTTON = 3
fld public final static int ID_PIN_BUTTON = 2
fld public final static int ID_RESTORE_BUTTON = 4
fld public final static int ID_RESTORE_GROUP_BUTTON = 11
fld public final static int ID_SCROLL_LEFT_BUTTON = 9
fld public final static int ID_SCROLL_RIGHT_BUTTON = 10
fld public final static int ID_SLIDE_DOWN_BUTTON = 7
fld public final static int ID_SLIDE_GROUP_BUTTON = 12
fld public final static int ID_SLIDE_LEFT_BUTTON = 5
fld public final static int ID_SLIDE_RIGHT_BUTTON = 6
fld public final static int STATE_DEFAULT = 0
fld public final static int STATE_DISABLED = 2
fld public final static int STATE_PRESSED = 1
fld public final static int STATE_ROLLOVER = 3
meth protected abstract java.lang.String getTabActionCommand(java.awt.event.ActionEvent)
meth protected int getButtonId()
meth protected org.netbeans.swing.tabcontrol.TabDisplayer getTabDisplayer()
meth protected org.netbeans.swing.tabcontrol.event.TabActionEvent createTabActionEvent(java.awt.event.ActionEvent)
meth protected void configureButton()
meth protected void fireActionPerformed(java.awt.event.ActionEvent)
meth public javax.swing.Icon getDisabledIcon()
meth public javax.swing.Icon getDisabledSelectedIcon()
meth public javax.swing.Icon getIcon()
meth public javax.swing.Icon getPressedIcon()
meth public javax.swing.Icon getRolloverIcon()
meth public javax.swing.Icon getRolloverSelectedIcon()
meth public void updateUI()
supr javax.swing.JButton
hfds buttonId,showBorder,superConstructorsCompleted

CLSS public org.netbeans.swing.tabcontrol.plaf.TabControlButtonFactory
meth public static javax.swing.Icon getIcon(java.lang.String)
meth public static org.netbeans.swing.tabcontrol.plaf.TabControlButton createCloseButton(org.netbeans.swing.tabcontrol.TabDisplayer)
meth public static org.netbeans.swing.tabcontrol.plaf.TabControlButton createCloseGroupButton(org.netbeans.swing.tabcontrol.TabDisplayer)
meth public static org.netbeans.swing.tabcontrol.plaf.TabControlButton createDropDownButton(org.netbeans.swing.tabcontrol.TabDisplayer,boolean)
meth public static org.netbeans.swing.tabcontrol.plaf.TabControlButton createMaximizeRestoreButton(org.netbeans.swing.tabcontrol.TabDisplayer,boolean)
meth public static org.netbeans.swing.tabcontrol.plaf.TabControlButton createRestoreGroupButton(org.netbeans.swing.tabcontrol.TabDisplayer,java.lang.String)
meth public static org.netbeans.swing.tabcontrol.plaf.TabControlButton createScrollLeftButton(org.netbeans.swing.tabcontrol.TabDisplayer,javax.swing.Action,boolean)
meth public static org.netbeans.swing.tabcontrol.plaf.TabControlButton createScrollRightButton(org.netbeans.swing.tabcontrol.TabDisplayer,javax.swing.Action,boolean)
meth public static org.netbeans.swing.tabcontrol.plaf.TabControlButton createSlideGroupButton(org.netbeans.swing.tabcontrol.TabDisplayer)
meth public static org.netbeans.swing.tabcontrol.plaf.TabControlButton createSlidePinButton(org.netbeans.swing.tabcontrol.TabDisplayer)
supr java.lang.Object
hcls CloseButton,CloseGroupButton,DropDownButton,MaximizeRestoreButton,RestoreGroupButton,SlideGroupButton,SlidePinButton,TimerButton

CLSS public abstract interface org.netbeans.swing.tabcontrol.plaf.TabLayoutModel
meth public abstract int dropIndexOfPoint(int,int)
meth public abstract int getH(int)
meth public abstract int getW(int)
meth public abstract int getX(int)
meth public abstract int getY(int)
meth public abstract int indexOfPoint(int,int)
meth public abstract void setPadding(java.awt.Dimension)

CLSS public abstract interface org.netbeans.swing.tabcontrol.plaf.TabPainter
intf javax.swing.border.Border
meth public abstract boolean supportsCloseButton(javax.swing.JComponent)
meth public abstract java.awt.Polygon getInteriorPolygon(java.awt.Component)
meth public abstract void getCloseButtonRectangle(javax.swing.JComponent,java.awt.Rectangle,java.awt.Rectangle)
meth public abstract void paintInterior(java.awt.Graphics,java.awt.Component)

CLSS public abstract org.netbeans.swing.tabcontrol.plaf.TabState
cons public init()
fld public final static int ACTIVE = 32
fld public final static int AFTER_SELECTED = 2048
fld public final static int ALL_TABS = 2147483647
fld public final static int ARMED = 4
fld public final static int ATTENTION = 16384
fld public final static int BEFORE_ARMED = 32768
fld public final static int BEFORE_SELECTED = 1024
fld public final static int BUSY = 65536
fld public final static int CHANGE_NONE_TO_TAB = 3
fld public final static int CHANGE_TAB_TO_NONE = 2
fld public final static int CHANGE_TAB_TO_SELF = 4
fld public final static int CHANGE_TAB_TO_TAB = 1
fld public final static int CLIP_LEFT = 2
fld public final static int CLIP_RIGHT = 1
fld public final static int CLOSE_BUTTON_ARMED = 512
fld public final static int HIGHLIGHT = 131072
fld public final static int LEFTMOST = 128
fld public final static int MOUSE_IN_TABS_AREA = 4096
fld public final static int MOUSE_PRESSED_IN_CLOSE_BUTTON = 8192
fld public final static int NOT_ONSCREEN = 64
fld public final static int NO_CHANGE = 0
fld public final static int PRESSED = 8
fld public final static int REPAINT_ALL_ON_MOUSE_ENTER_TABS_AREA = 3
fld public final static int REPAINT_ALL_TABS_ON_ACTIVATION_CHANGE = 32
fld public final static int REPAINT_ALL_TABS_ON_SELECTION_CHANGE = 128
fld public final static int REPAINT_ON_CLOSE_BUTTON_PRESSED = 256
fld public final static int REPAINT_ON_MOUSE_ENTER_CLOSE_BUTTON = 4
fld public final static int REPAINT_ON_MOUSE_ENTER_TAB = 1
fld public final static int REPAINT_ON_MOUSE_PRESSED = 8
fld public final static int REPAINT_ON_SELECTION_CHANGE = 64
fld public final static int REPAINT_SELECTION_ON_ACTIVATION_CHANGE = 16
fld public final static int RIGHTMOST = 256
fld public final static int SELECTED = 16
fld public static int STATE_LAST
meth protected abstract void repaintAllTabs()
meth protected abstract void repaintTab(int)
meth protected void change(int,int,int,int)
meth protected void maybeRepaint(int,int)
meth protected void possibleChange(boolean,boolean,int)
meth protected void possibleChange(int,int,int)
meth public abstract int getRepaintPolicy(int)
meth public final boolean setActive(boolean)
meth public final boolean setMouseInTabsArea(boolean)
meth public final int setCloseButtonContainsMouse(int)
meth public final int setContainsMouse(int)
meth public final int setMousePressedInCloseButton(int)
meth public final int setPressed(int)
meth public final int setSelected(int)
meth public final void addAlarmTab(int)
meth public final void addHighlightTab(int)
meth public final void removeAlarmTab(int)
meth public final void removeHighlightTab(int)
meth public int getState(int)
meth public java.lang.String toString()
meth public void clearTransientStates()
supr java.lang.Object
hfds active,alarmTabs,alarmTimer,attentionToggle,closeButtonContainsMouseIndex,containsMouseIndex,curr,highlightTabs,lastAffected,lastChange,lastChangeType,mouseInTabsArea,mousePressedInCloseButtonIndex,pressedIndex,prev,selectedIndex

CLSS public org.netbeans.swing.tabcontrol.plaf.ToolbarTabDisplayerUI
cons public init(org.netbeans.swing.tabcontrol.TabDisplayer)
innr public final IndexButton
meth protected java.awt.event.MouseListener createMouseListener()
meth protected javax.swing.event.ChangeListener createSelectionListener()
meth protected org.netbeans.swing.tabcontrol.plaf.TabLayoutModel createLayoutModel()
meth protected void install()
meth protected void modelChanged()
meth public int tabForCoordinate(java.awt.Point)
meth public java.awt.Dimension getMinimumSize(javax.swing.JComponent)
meth public java.awt.Dimension getPreferredSize(javax.swing.JComponent)
meth public java.awt.Polygon getExactTabIndication(int)
meth public java.awt.Polygon getInsertTabIndication(int)
meth public java.awt.Rectangle getTabRect(int,java.awt.Rectangle)
meth public javax.swing.Icon getButtonIcon(int,int)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
meth public void cancelRequestAttention(int)
meth public void requestAttention(int)
supr org.netbeans.swing.tabcontrol.plaf.AbstractTabDisplayerUI
hfds ascent,bg,buttonBorder,fontHeight,isAqua,isMac,toolbar
hcls AutoGridLayout,TabToolbar

CLSS public final org.netbeans.swing.tabcontrol.plaf.ToolbarTabDisplayerUI$IndexButton
 outer org.netbeans.swing.tabcontrol.plaf.ToolbarTabDisplayerUI
cons public init(org.netbeans.swing.tabcontrol.plaf.ToolbarTabDisplayerUI)
intf java.awt.event.ActionListener
meth public boolean isActive()
meth public final void actionPerformed(java.awt.event.ActionEvent)
meth public int getIndex()
meth public java.awt.Dimension getPreferredSize()
meth public java.lang.String doGetText()
meth public java.lang.String getText()
meth public java.lang.String getToolTipText()
meth public javax.swing.Icon getIcon()
meth public void addNotify()
meth public void paintComponent(java.awt.Graphics)
meth public void removeNotify()
supr javax.swing.JToggleButton
hfds lastKnownText

CLSS public final org.netbeans.swing.tabcontrol.plaf.WinClassicEditorTabDisplayerUI
cons public init(org.netbeans.swing.tabcontrol.TabDisplayer)
meth protected java.awt.Rectangle getControlButtonsRectangle(java.awt.Container)
meth protected org.netbeans.swing.tabcontrol.plaf.TabCellRenderer createDefaultRenderer()
meth protected void paintAfterTabs(java.awt.Graphics)
meth public java.awt.Dimension getPreferredSize(javax.swing.JComponent)
meth public java.awt.Insets getTabAreaInsets()
meth public java.awt.Rectangle getTabRect(int,java.awt.Rectangle)
meth public javax.swing.Icon getButtonIcon(int,int)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
meth public void install()
supr org.netbeans.swing.tabcontrol.plaf.BasicScrollingTabDisplayerUI
hfds buttonIconPaths,isGenericUI,scratch5

CLSS public final org.netbeans.swing.tabcontrol.plaf.WinClassicViewTabDisplayerUI
meth protected java.awt.Font getTxtFont()
meth protected void paintOverallBorder(java.awt.Graphics,javax.swing.JComponent)
meth protected void paintTabBackground(java.awt.Graphics,int,int,int,int,int)
meth protected void paintTabBorder(java.awt.Graphics,int,int,int,int,int)
meth protected void paintTabContent(java.awt.Graphics,int,java.lang.String,int,int,int,int)
meth public java.awt.Dimension getPreferredSize(javax.swing.JComponent)
meth public javax.swing.Icon getButtonIcon(int,int)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
meth public void paint(java.awt.Graphics,javax.swing.JComponent)
supr org.netbeans.swing.tabcontrol.plaf.AbstractViewTabDisplayerUI
hfds BUMP_WIDTH,BUMP_X_PAD,GTK_TABBED_PANE_BACKGROUND_1,ICON_X_PAD,TXT_X_PAD,TXT_Y_PAD,buttonIconPaths,isGenericUI,prefSize,tempRect

CLSS public org.netbeans.swing.tabcontrol.plaf.WinFlatEditorTabDisplayerUI
cons public init(org.netbeans.swing.tabcontrol.TabDisplayer)
meth protected java.awt.Rectangle getControlButtonsRectangle(java.awt.Container)
meth protected org.netbeans.swing.tabcontrol.plaf.TabCellRenderer createDefaultRenderer()
meth public java.awt.Dimension getPreferredSize(javax.swing.JComponent)
meth public java.awt.Insets getTabAreaInsets()
meth public javax.swing.Icon getButtonIcon(int,int)
meth public org.netbeans.swing.tabcontrol.plaf.TabCellRenderer getTabCellRenderer(int)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
meth public void paintBackground(java.awt.Graphics)
supr org.netbeans.swing.tabcontrol.plaf.BasicScrollingTabDisplayerUI
hfds ICON_X_PAD,activeBackground,background,contentBorderColor,tabInsets,unscaledBorders

CLSS public org.netbeans.swing.tabcontrol.plaf.WinFlatViewTabDisplayerUI
cons public init(org.netbeans.swing.tabcontrol.TabDisplayer)
meth protected java.awt.Font getTxtFont()
meth protected org.netbeans.swing.tabcontrol.plaf.AbstractViewTabDisplayerUI$Controller createController()
meth protected void paintDisplayerBackground(java.awt.Graphics,javax.swing.JComponent)
meth protected void paintTabBackground(java.awt.Graphics,int,int,int,int,int)
meth protected void paintTabBorder(java.awt.Graphics,int,int,int,int,int)
meth protected void paintTabContent(java.awt.Graphics,int,java.lang.String,int,int,int,int)
meth public java.awt.Dimension getPreferredSize(javax.swing.JComponent)
meth public javax.swing.Icon getButtonIcon(int,int)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
meth public void installUI(javax.swing.JComponent)
meth public void postTabAction(org.netbeans.swing.tabcontrol.event.TabActionEvent)
supr org.netbeans.swing.tabcontrol.plaf.AbstractViewTabDisplayerUI
hfds ICON_X_PAD,activeBackground,activeForeground,attentionBackground,attentionForeground,background,colorsReady,contentBorderColor,font,foreground,hoverBackground,hoverForeground,inactiveUnderlineColor,selectedBackground,selectedForeground,showSelectedTabBorder,showTabSeparators,tabInsets,tabSeparatorColor,underlineAtTop,underlineColor,underlineHeight,unscaledBorders,unselectedHoverBackground
hcls OwnController

CLSS public final org.netbeans.swing.tabcontrol.plaf.WinVistaEditorTabDisplayerUI
cons public init(org.netbeans.swing.tabcontrol.TabDisplayer)
meth protected java.awt.Rectangle getTabRectForRepaint(int,java.awt.Rectangle)
meth protected org.netbeans.swing.tabcontrol.plaf.TabCellRenderer createDefaultRenderer()
meth protected void paintAfterTabs(java.awt.Graphics)
meth public java.awt.Dimension getPreferredSize(javax.swing.JComponent)
meth public javax.swing.Icon getButtonIcon(int,int)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
meth public void paintBackground(java.awt.Graphics)
supr org.netbeans.swing.tabcontrol.plaf.BasicScrollingTabDisplayerUI

CLSS public org.netbeans.swing.tabcontrol.plaf.WinVistaSlidingButtonUI
cons public init()
meth protected void installBorder(javax.swing.AbstractButton)
supr org.netbeans.swing.tabcontrol.plaf.WinXPSlidingButtonUI

CLSS public final org.netbeans.swing.tabcontrol.plaf.WinVistaViewTabDisplayerUI
meth protected java.awt.Font getTxtFont()
meth protected org.netbeans.swing.tabcontrol.plaf.AbstractViewTabDisplayerUI$Controller createController()
meth protected void paintTabBackground(java.awt.Graphics,int,int,int,int,int)
meth protected void paintTabBorder(java.awt.Graphics,int,int,int,int,int)
meth protected void paintTabContent(java.awt.Graphics,int,java.lang.String,int,int,int,int)
meth public java.awt.Dimension getPreferredSize(javax.swing.JComponent)
meth public javax.swing.Icon getButtonIcon(int,int)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
meth public void installUI(javax.swing.JComponent)
meth public void postTabAction(org.netbeans.swing.tabcontrol.event.TabActionEvent)
supr org.netbeans.swing.tabcontrol.plaf.AbstractViewTabDisplayerUI

CLSS public final org.netbeans.swing.tabcontrol.plaf.WinXPEditorTabDisplayerUI
cons public init(org.netbeans.swing.tabcontrol.TabDisplayer)
meth protected java.awt.Font createFont()
meth protected org.netbeans.swing.tabcontrol.plaf.TabCellRenderer createDefaultRenderer()
meth protected void paintAfterTabs(java.awt.Graphics)
meth public java.awt.Dimension getPreferredSize(javax.swing.JComponent)
meth public javax.swing.Icon getButtonIcon(int,int)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
meth public void paintBackground(java.awt.Graphics)
supr org.netbeans.swing.tabcontrol.plaf.BasicScrollingTabDisplayerUI
hfds buttonIconPaths,scratch5

CLSS public org.netbeans.swing.tabcontrol.plaf.WinXPSlidingButtonUI
fld protected javax.swing.JToggleButton hiddenToggle
meth protected void installBorder(javax.swing.AbstractButton)
meth protected void paintBackground(java.awt.Graphics2D,javax.swing.AbstractButton)
meth protected void paintButtonPressed(java.awt.Graphics,javax.swing.AbstractButton)
meth protected void uninstallDefaults(javax.swing.AbstractButton)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
meth public void installDefaults(javax.swing.AbstractButton)
meth public void paint(java.awt.Graphics,javax.swing.JComponent)
supr org.netbeans.swing.tabcontrol.plaf.WindowsSlidingButtonUI
hfds INSTANCE,defaults_initialized

CLSS public final org.netbeans.swing.tabcontrol.plaf.WinXPViewTabDisplayerUI
meth protected java.awt.Font getTxtFont()
meth protected org.netbeans.swing.tabcontrol.plaf.AbstractViewTabDisplayerUI$Controller createController()
meth protected void paintDisplayerBackground(java.awt.Graphics,javax.swing.JComponent)
meth protected void paintTabBackground(java.awt.Graphics,int,int,int,int,int)
meth protected void paintTabBorder(java.awt.Graphics,int,int,int,int,int)
meth protected void paintTabContent(java.awt.Graphics,int,java.lang.String,int,int,int,int)
meth public java.awt.Dimension getPreferredSize(javax.swing.JComponent)
meth public javax.swing.Icon getButtonIcon(int,int)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
meth public void installUI(javax.swing.JComponent)
meth public void postTabAction(org.netbeans.swing.tabcontrol.event.TabActionEvent)
supr org.netbeans.swing.tabcontrol.plaf.AbstractViewTabDisplayerUI
hfds BUMP_X_PAD,BUMP_Y_PAD_BOTTOM,BUMP_Y_PAD_UPPER,HIGHLIGHTED_RAISE,ICON_X_PAD,TXT_X_PAD,TXT_Y_PAD,bgFillC,borderC,bottomBorderC,buttonIconPaths,colorsReady,focusFillBrightC,focusFillDarkC,prefSize,selBorderC,selFillC,tempRect,txtC,unselFillBrightC,unselFillDarkC
hcls OwnController

CLSS public org.netbeans.swing.tabcontrol.plaf.Windows8EditorTabDisplayerUI
cons public init(org.netbeans.swing.tabcontrol.TabDisplayer)
meth protected java.awt.Rectangle getTabRectForRepaint(int,java.awt.Rectangle)
meth protected org.netbeans.swing.tabcontrol.plaf.TabCellRenderer createDefaultRenderer()
meth protected void paintAfterTabs(java.awt.Graphics)
meth public java.awt.Dimension getPreferredSize(javax.swing.JComponent)
meth public javax.swing.Icon getButtonIcon(int,int)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
meth public void paintBackground(java.awt.Graphics)
supr org.netbeans.swing.tabcontrol.plaf.BasicScrollingTabDisplayerUI
hfds buttonIconPaths

CLSS public final org.netbeans.swing.tabcontrol.plaf.Windows8VectorEditorTabDisplayerUI
cons public init(org.netbeans.swing.tabcontrol.TabDisplayer)
meth protected org.netbeans.swing.tabcontrol.plaf.TabCellRenderer createDefaultRenderer()
meth public javax.swing.Icon getButtonIcon(int,int)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
supr org.netbeans.swing.tabcontrol.plaf.Windows8EditorTabDisplayerUI

CLSS public final org.netbeans.swing.tabcontrol.plaf.Windows8VectorViewTabDisplayerUI
meth public javax.swing.Icon getButtonIcon(int,int)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
supr org.netbeans.swing.tabcontrol.plaf.Windows8ViewTabDisplayerUI

CLSS public org.netbeans.swing.tabcontrol.plaf.Windows8ViewTabDisplayerUI
cons protected init(org.netbeans.swing.tabcontrol.TabDisplayer)
meth protected java.awt.Font getTxtFont()
meth protected org.netbeans.swing.tabcontrol.plaf.AbstractViewTabDisplayerUI$Controller createController()
meth protected void paintTabBackground(java.awt.Graphics,int,int,int,int,int)
meth protected void paintTabBorder(java.awt.Graphics,int,int,int,int,int)
meth protected void paintTabContent(java.awt.Graphics,int,java.lang.String,int,int,int,int)
meth public java.awt.Dimension getPreferredSize(javax.swing.JComponent)
meth public javax.swing.Icon getButtonIcon(int,int)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
meth public void installUI(javax.swing.JComponent)
meth public void postTabAction(org.netbeans.swing.tabcontrol.event.TabActionEvent)
supr org.netbeans.swing.tabcontrol.plaf.AbstractViewTabDisplayerUI
hfds attentionFillLowerC,attentionFillUpperC,buttonIconPaths,colorsReady,focusFillLowerC,focusFillUpperC,mouseOverFillLowerC,mouseOverFillUpperC,selFillC,unselFillLowerC,unselFillUpperC

CLSS public org.netbeans.swing.tabcontrol.plaf.WindowsSlidingButtonUI
cons protected init()
fld protected java.awt.Color focusColor
fld protected static int dashedRectGapHeight
fld protected static int dashedRectGapWidth
fld protected static int dashedRectGapX
fld protected static int dashedRectGapY
meth protected java.awt.Color getFocusColor()
meth protected void installBorder(javax.swing.AbstractButton)
meth protected void paintBackground(java.awt.Graphics2D,javax.swing.AbstractButton)
meth protected void paintButtonPressed(java.awt.Graphics,javax.swing.AbstractButton)
meth protected void paintFocus(java.awt.Graphics,javax.swing.AbstractButton,java.awt.Rectangle,java.awt.Rectangle,java.awt.Rectangle)
meth protected void uninstallDefaults(javax.swing.AbstractButton)
meth public java.awt.Dimension getPreferredSize(javax.swing.JComponent)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
meth public void installDefaults(javax.swing.AbstractButton)
supr org.netbeans.swing.tabcontrol.SlidingButtonUI
hfds INSTANCE,defaults_initialized

