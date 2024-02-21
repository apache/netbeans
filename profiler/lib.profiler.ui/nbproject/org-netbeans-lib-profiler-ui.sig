#Signature file v4.1
#Version 1.169

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

CLSS public java.awt.FlowLayout
cons public init()
cons public init(int)
cons public init(int,int,int)
fld public final static int CENTER = 1
fld public final static int LEADING = 3
fld public final static int LEFT = 0
fld public final static int RIGHT = 2
fld public final static int TRAILING = 4
intf java.awt.LayoutManager
intf java.io.Serializable
meth public boolean getAlignOnBaseline()
meth public int getAlignment()
meth public int getHgap()
meth public int getVgap()
meth public java.awt.Dimension minimumLayoutSize(java.awt.Container)
meth public java.awt.Dimension preferredLayoutSize(java.awt.Container)
meth public java.lang.String toString()
meth public void addLayoutComponent(java.lang.String,java.awt.Component)
meth public void layoutContainer(java.awt.Container)
meth public void removeLayoutComponent(java.awt.Component)
meth public void setAlignOnBaseline(boolean)
meth public void setAlignment(int)
meth public void setHgap(int)
meth public void setVgap(int)
supr java.lang.Object

CLSS public abstract java.awt.FocusTraversalPolicy
cons public init()
meth public abstract java.awt.Component getComponentAfter(java.awt.Container,java.awt.Component)
meth public abstract java.awt.Component getComponentBefore(java.awt.Container,java.awt.Component)
meth public abstract java.awt.Component getDefaultComponent(java.awt.Container)
meth public abstract java.awt.Component getFirstComponent(java.awt.Container)
meth public abstract java.awt.Component getLastComponent(java.awt.Container)
meth public java.awt.Component getInitialComponent(java.awt.Window)
supr java.lang.Object

CLSS public abstract interface java.awt.ItemSelectable
meth public abstract java.lang.Object[] getSelectedObjects()
meth public abstract void addItemListener(java.awt.event.ItemListener)
meth public abstract void removeItemListener(java.awt.event.ItemListener)

CLSS public abstract interface java.awt.LayoutManager
meth public abstract java.awt.Dimension minimumLayoutSize(java.awt.Container)
meth public abstract java.awt.Dimension preferredLayoutSize(java.awt.Container)
meth public abstract void addLayoutComponent(java.lang.String,java.awt.Component)
meth public abstract void layoutContainer(java.awt.Container)
meth public abstract void removeLayoutComponent(java.awt.Component)

CLSS public abstract interface java.awt.MenuContainer
meth public abstract boolean postEvent(java.awt.Event)
 anno 0 java.lang.Deprecated()
meth public abstract java.awt.Font getFont()
meth public abstract void remove(java.awt.MenuComponent)

CLSS public abstract interface java.awt.event.ActionListener
intf java.util.EventListener
meth public abstract void actionPerformed(java.awt.event.ActionEvent)

CLSS public abstract interface java.awt.event.ComponentListener
intf java.util.EventListener
meth public abstract void componentHidden(java.awt.event.ComponentEvent)
meth public abstract void componentMoved(java.awt.event.ComponentEvent)
meth public abstract void componentResized(java.awt.event.ComponentEvent)
meth public abstract void componentShown(java.awt.event.ComponentEvent)

CLSS public abstract interface java.awt.event.FocusListener
intf java.util.EventListener
meth public abstract void focusGained(java.awt.event.FocusEvent)
meth public abstract void focusLost(java.awt.event.FocusEvent)

CLSS public abstract interface java.awt.event.KeyListener
intf java.util.EventListener
meth public abstract void keyPressed(java.awt.event.KeyEvent)
meth public abstract void keyReleased(java.awt.event.KeyEvent)
meth public abstract void keyTyped(java.awt.event.KeyEvent)

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

CLSS public abstract interface java.util.EventListener

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

CLSS public javax.swing.ButtonGroup
cons public init()
fld protected java.util.Vector<javax.swing.AbstractButton> buttons
intf java.io.Serializable
meth public boolean isSelected(javax.swing.ButtonModel)
meth public int getButtonCount()
meth public java.util.Enumeration<javax.swing.AbstractButton> getElements()
meth public javax.swing.ButtonModel getSelection()
meth public void add(javax.swing.AbstractButton)
meth public void clearSelection()
meth public void remove(javax.swing.AbstractButton)
meth public void setSelected(javax.swing.ButtonModel,boolean)
supr java.lang.Object

CLSS public abstract interface javax.swing.Icon
meth public abstract int getIconHeight()
meth public abstract int getIconWidth()
meth public abstract void paintIcon(java.awt.Component,java.awt.Graphics,int,int)

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

CLSS public javax.swing.JCheckBoxMenuItem
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,boolean)
cons public init(java.lang.String,javax.swing.Icon)
cons public init(java.lang.String,javax.swing.Icon,boolean)
cons public init(javax.swing.Action)
cons public init(javax.swing.Icon)
innr protected AccessibleJCheckBoxMenuItem
intf javax.accessibility.Accessible
intf javax.swing.SwingConstants
meth protected java.lang.String paramString()
meth public boolean getState()
meth public java.lang.Object[] getSelectedObjects()
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public void setState(boolean)
supr javax.swing.JMenuItem

CLSS public javax.swing.JComboBox<%0 extends java.lang.Object>
cons public init()
cons public init(java.util.Vector<{javax.swing.JComboBox%0}>)
cons public init(javax.swing.ComboBoxModel<{javax.swing.JComboBox%0}>)
cons public init({javax.swing.JComboBox%0}[])
fld protected boolean isEditable
fld protected boolean lightWeightPopupEnabled
fld protected int maximumRowCount
fld protected java.lang.Object selectedItemReminder
fld protected java.lang.String actionCommand
fld protected javax.swing.ComboBoxEditor editor
fld protected javax.swing.ComboBoxModel<{javax.swing.JComboBox%0}> dataModel
fld protected javax.swing.JComboBox$KeySelectionManager keySelectionManager
fld protected javax.swing.ListCellRenderer<? super {javax.swing.JComboBox%0}> renderer
innr protected AccessibleJComboBox
innr public abstract interface static KeySelectionManager
intf java.awt.ItemSelectable
intf java.awt.event.ActionListener
intf javax.accessibility.Accessible
intf javax.swing.event.ListDataListener
meth protected boolean processKeyBinding(javax.swing.KeyStroke,java.awt.event.KeyEvent,int,boolean)
meth protected java.beans.PropertyChangeListener createActionPropertyChangeListener(javax.swing.Action)
meth protected java.lang.String paramString()
meth protected javax.swing.JComboBox$KeySelectionManager createDefaultKeySelectionManager()
meth protected void actionPropertyChanged(javax.swing.Action,java.lang.String)
meth protected void configurePropertiesFromAction(javax.swing.Action)
meth protected void fireActionEvent()
meth protected void fireItemStateChanged(java.awt.event.ItemEvent)
meth protected void installAncestorListener()
meth protected void selectedItemChanged()
meth public boolean isEditable()
meth public boolean isLightWeightPopupEnabled()
meth public boolean isPopupVisible()
meth public boolean selectWithKeyChar(char)
meth public int getItemCount()
meth public int getMaximumRowCount()
meth public int getSelectedIndex()
meth public java.awt.event.ActionListener[] getActionListeners()
meth public java.awt.event.ItemListener[] getItemListeners()
meth public java.lang.Object getSelectedItem()
meth public java.lang.Object[] getSelectedObjects()
meth public java.lang.String getActionCommand()
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.Action getAction()
meth public javax.swing.ComboBoxEditor getEditor()
meth public javax.swing.ComboBoxModel<{javax.swing.JComboBox%0}> getModel()
meth public javax.swing.JComboBox$KeySelectionManager getKeySelectionManager()
meth public javax.swing.ListCellRenderer<? super {javax.swing.JComboBox%0}> getRenderer()
meth public javax.swing.event.PopupMenuListener[] getPopupMenuListeners()
meth public javax.swing.plaf.ComboBoxUI getUI()
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void addActionListener(java.awt.event.ActionListener)
meth public void addItem({javax.swing.JComboBox%0})
meth public void addItemListener(java.awt.event.ItemListener)
meth public void addPopupMenuListener(javax.swing.event.PopupMenuListener)
meth public void configureEditor(javax.swing.ComboBoxEditor,java.lang.Object)
meth public void contentsChanged(javax.swing.event.ListDataEvent)
meth public void firePopupMenuCanceled()
meth public void firePopupMenuWillBecomeInvisible()
meth public void firePopupMenuWillBecomeVisible()
meth public void hidePopup()
meth public void insertItemAt({javax.swing.JComboBox%0},int)
meth public void intervalAdded(javax.swing.event.ListDataEvent)
meth public void intervalRemoved(javax.swing.event.ListDataEvent)
meth public void processKeyEvent(java.awt.event.KeyEvent)
meth public void removeActionListener(java.awt.event.ActionListener)
meth public void removeAllItems()
meth public void removeItem(java.lang.Object)
meth public void removeItemAt(int)
meth public void removeItemListener(java.awt.event.ItemListener)
meth public void removePopupMenuListener(javax.swing.event.PopupMenuListener)
meth public void setAction(javax.swing.Action)
meth public void setActionCommand(java.lang.String)
meth public void setEditable(boolean)
meth public void setEditor(javax.swing.ComboBoxEditor)
meth public void setEnabled(boolean)
meth public void setKeySelectionManager(javax.swing.JComboBox$KeySelectionManager)
meth public void setLightWeightPopupEnabled(boolean)
meth public void setMaximumRowCount(int)
meth public void setModel(javax.swing.ComboBoxModel<{javax.swing.JComboBox%0}>)
meth public void setPopupVisible(boolean)
meth public void setPrototypeDisplayValue({javax.swing.JComboBox%0})
meth public void setRenderer(javax.swing.ListCellRenderer<? super {javax.swing.JComboBox%0}>)
meth public void setSelectedIndex(int)
meth public void setSelectedItem(java.lang.Object)
meth public void setUI(javax.swing.plaf.ComboBoxUI)
meth public void showPopup()
meth public void updateUI()
meth public {javax.swing.JComboBox%0} getItemAt(int)
meth public {javax.swing.JComboBox%0} getPrototypeDisplayValue()
supr javax.swing.JComponent

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

CLSS public javax.swing.JEditorPane
cons public init()
cons public init(java.lang.String) throws java.io.IOException
cons public init(java.lang.String,java.lang.String)
cons public init(java.net.URL) throws java.io.IOException
fld public final static java.lang.String HONOR_DISPLAY_PROPERTIES = "JEditorPane.honorDisplayProperties"
fld public final static java.lang.String W3C_LENGTH_UNITS = "JEditorPane.w3cLengthUnits"
innr protected AccessibleJEditorPane
innr protected AccessibleJEditorPaneHTML
innr protected JEditorPaneAccessibleHypertextSupport
meth protected java.io.InputStream getStream(java.net.URL) throws java.io.IOException
meth protected java.lang.String paramString()
meth protected javax.swing.text.EditorKit createDefaultEditorKit()
meth public boolean getScrollableTracksViewportHeight()
meth public boolean getScrollableTracksViewportWidth()
meth public final java.lang.String getContentType()
meth public final void setContentType(java.lang.String)
meth public java.awt.Dimension getPreferredSize()
meth public java.lang.String getText()
meth public java.lang.String getUIClassID()
meth public java.net.URL getPage()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.event.HyperlinkListener[] getHyperlinkListeners()
meth public javax.swing.text.EditorKit getEditorKit()
meth public javax.swing.text.EditorKit getEditorKitForContentType(java.lang.String)
meth public static java.lang.String getEditorKitClassNameForContentType(java.lang.String)
meth public static javax.swing.text.EditorKit createEditorKitForContentType(java.lang.String)
meth public static void registerEditorKitForContentType(java.lang.String,java.lang.String)
meth public static void registerEditorKitForContentType(java.lang.String,java.lang.String,java.lang.ClassLoader)
meth public void addHyperlinkListener(javax.swing.event.HyperlinkListener)
meth public void fireHyperlinkUpdate(javax.swing.event.HyperlinkEvent)
meth public void read(java.io.InputStream,java.lang.Object) throws java.io.IOException
meth public void removeHyperlinkListener(javax.swing.event.HyperlinkListener)
meth public void replaceSelection(java.lang.String)
meth public void scrollToReference(java.lang.String)
meth public void setEditorKit(javax.swing.text.EditorKit)
meth public void setEditorKitForContentType(java.lang.String,javax.swing.text.EditorKit)
meth public void setPage(java.lang.String) throws java.io.IOException
meth public void setPage(java.net.URL) throws java.io.IOException
meth public void setText(java.lang.String)
supr javax.swing.text.JTextComponent

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

CLSS public javax.swing.JMenuItem
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,int)
cons public init(java.lang.String,javax.swing.Icon)
cons public init(javax.swing.Action)
cons public init(javax.swing.Icon)
innr protected AccessibleJMenuItem
intf javax.accessibility.Accessible
intf javax.swing.MenuElement
meth protected java.lang.String paramString()
meth protected void actionPropertyChanged(javax.swing.Action,java.lang.String)
meth protected void configurePropertiesFromAction(javax.swing.Action)
meth protected void fireMenuDragMouseDragged(javax.swing.event.MenuDragMouseEvent)
meth protected void fireMenuDragMouseEntered(javax.swing.event.MenuDragMouseEvent)
meth protected void fireMenuDragMouseExited(javax.swing.event.MenuDragMouseEvent)
meth protected void fireMenuDragMouseReleased(javax.swing.event.MenuDragMouseEvent)
meth protected void fireMenuKeyPressed(javax.swing.event.MenuKeyEvent)
meth protected void fireMenuKeyReleased(javax.swing.event.MenuKeyEvent)
meth protected void fireMenuKeyTyped(javax.swing.event.MenuKeyEvent)
meth protected void init(java.lang.String,javax.swing.Icon)
meth public boolean isArmed()
meth public java.awt.Component getComponent()
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.KeyStroke getAccelerator()
meth public javax.swing.MenuElement[] getSubElements()
meth public javax.swing.event.MenuDragMouseListener[] getMenuDragMouseListeners()
meth public javax.swing.event.MenuKeyListener[] getMenuKeyListeners()
meth public void addMenuDragMouseListener(javax.swing.event.MenuDragMouseListener)
meth public void addMenuKeyListener(javax.swing.event.MenuKeyListener)
meth public void menuSelectionChanged(boolean)
meth public void processKeyEvent(java.awt.event.KeyEvent,javax.swing.MenuElement[],javax.swing.MenuSelectionManager)
meth public void processMenuDragMouseEvent(javax.swing.event.MenuDragMouseEvent)
meth public void processMenuKeyEvent(javax.swing.event.MenuKeyEvent)
meth public void processMouseEvent(java.awt.event.MouseEvent,javax.swing.MenuElement[],javax.swing.MenuSelectionManager)
meth public void removeMenuDragMouseListener(javax.swing.event.MenuDragMouseListener)
meth public void removeMenuKeyListener(javax.swing.event.MenuKeyListener)
meth public void setAccelerator(javax.swing.KeyStroke)
meth public void setArmed(boolean)
meth public void setEnabled(boolean)
meth public void setModel(javax.swing.ButtonModel)
meth public void setUI(javax.swing.plaf.MenuItemUI)
meth public void updateUI()
supr javax.swing.AbstractButton

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

CLSS public javax.swing.JPopupMenu
cons public init()
cons public init(java.lang.String)
innr protected AccessibleJPopupMenu
innr public static Separator
intf javax.accessibility.Accessible
intf javax.swing.MenuElement
meth protected java.beans.PropertyChangeListener createActionChangeListener(javax.swing.JMenuItem)
meth protected java.lang.String paramString()
meth protected javax.swing.JMenuItem createActionComponent(javax.swing.Action)
meth protected void firePopupMenuCanceled()
meth protected void firePopupMenuWillBecomeInvisible()
meth protected void firePopupMenuWillBecomeVisible()
meth protected void paintBorder(java.awt.Graphics)
meth protected void processFocusEvent(java.awt.event.FocusEvent)
meth protected void processKeyEvent(java.awt.event.KeyEvent)
meth public boolean isBorderPainted()
meth public boolean isLightWeightPopupEnabled()
meth public boolean isPopupTrigger(java.awt.event.MouseEvent)
meth public boolean isVisible()
meth public int getComponentIndex(java.awt.Component)
meth public java.awt.Component getComponent()
meth public java.awt.Component getComponentAtIndex(int)
 anno 0 java.lang.Deprecated()
meth public java.awt.Component getInvoker()
meth public java.awt.Insets getMargin()
meth public java.lang.String getLabel()
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.JMenuItem add(java.lang.String)
meth public javax.swing.JMenuItem add(javax.swing.Action)
meth public javax.swing.JMenuItem add(javax.swing.JMenuItem)
meth public javax.swing.MenuElement[] getSubElements()
meth public javax.swing.SingleSelectionModel getSelectionModel()
meth public javax.swing.event.MenuKeyListener[] getMenuKeyListeners()
meth public javax.swing.event.PopupMenuListener[] getPopupMenuListeners()
meth public javax.swing.plaf.PopupMenuUI getUI()
meth public static boolean getDefaultLightWeightPopupEnabled()
meth public static void setDefaultLightWeightPopupEnabled(boolean)
meth public void addMenuKeyListener(javax.swing.event.MenuKeyListener)
meth public void addPopupMenuListener(javax.swing.event.PopupMenuListener)
meth public void addSeparator()
meth public void insert(java.awt.Component,int)
meth public void insert(javax.swing.Action,int)
meth public void menuSelectionChanged(boolean)
meth public void pack()
meth public void processKeyEvent(java.awt.event.KeyEvent,javax.swing.MenuElement[],javax.swing.MenuSelectionManager)
meth public void processMouseEvent(java.awt.event.MouseEvent,javax.swing.MenuElement[],javax.swing.MenuSelectionManager)
meth public void remove(int)
meth public void removeMenuKeyListener(javax.swing.event.MenuKeyListener)
meth public void removePopupMenuListener(javax.swing.event.PopupMenuListener)
meth public void setBorderPainted(boolean)
meth public void setInvoker(java.awt.Component)
meth public void setLabel(java.lang.String)
meth public void setLightWeightPopupEnabled(boolean)
meth public void setLocation(int,int)
meth public void setPopupSize(int,int)
meth public void setPopupSize(java.awt.Dimension)
meth public void setSelected(java.awt.Component)
meth public void setSelectionModel(javax.swing.SingleSelectionModel)
meth public void setUI(javax.swing.plaf.PopupMenuUI)
meth public void setVisible(boolean)
meth public void show(java.awt.Component,int,int)
meth public void updateUI()
supr javax.swing.JComponent

CLSS public javax.swing.JRadioButton
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,boolean)
cons public init(java.lang.String,javax.swing.Icon)
cons public init(java.lang.String,javax.swing.Icon,boolean)
cons public init(javax.swing.Action)
cons public init(javax.swing.Icon)
cons public init(javax.swing.Icon,boolean)
innr protected AccessibleJRadioButton
intf javax.accessibility.Accessible
meth protected java.lang.String paramString()
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public void updateUI()
supr javax.swing.JToggleButton

CLSS public javax.swing.JRadioButtonMenuItem
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,boolean)
cons public init(java.lang.String,javax.swing.Icon)
cons public init(java.lang.String,javax.swing.Icon,boolean)
cons public init(javax.swing.Action)
cons public init(javax.swing.Icon)
cons public init(javax.swing.Icon,boolean)
innr protected AccessibleJRadioButtonMenuItem
intf javax.accessibility.Accessible
meth protected java.lang.String paramString()
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
supr javax.swing.JMenuItem

CLSS public javax.swing.JSpinner
cons public init()
cons public init(javax.swing.SpinnerModel)
innr protected AccessibleJSpinner
innr public static DateEditor
innr public static DefaultEditor
innr public static ListEditor
innr public static NumberEditor
intf javax.accessibility.Accessible
meth protected javax.swing.JComponent createEditor(javax.swing.SpinnerModel)
meth protected void fireStateChanged()
meth public java.lang.Object getNextValue()
meth public java.lang.Object getPreviousValue()
meth public java.lang.Object getValue()
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.JComponent getEditor()
meth public javax.swing.SpinnerModel getModel()
meth public javax.swing.event.ChangeListener[] getChangeListeners()
meth public javax.swing.plaf.SpinnerUI getUI()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void commitEdit() throws java.text.ParseException
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void setEditor(javax.swing.JComponent)
meth public void setModel(javax.swing.SpinnerModel)
meth public void setUI(javax.swing.plaf.SpinnerUI)
meth public void setValue(java.lang.Object)
meth public void updateUI()
supr javax.swing.JComponent

CLSS public javax.swing.JSplitPane
cons public init()
cons public init(int)
 anno 0 java.beans.ConstructorProperties(java.lang.String[] value=["orientation"])
cons public init(int,boolean)
cons public init(int,boolean,java.awt.Component,java.awt.Component)
cons public init(int,java.awt.Component,java.awt.Component)
fld protected boolean continuousLayout
fld protected boolean oneTouchExpandable
fld protected int dividerSize
fld protected int lastDividerLocation
fld protected int orientation
fld protected java.awt.Component leftComponent
fld protected java.awt.Component rightComponent
fld public final static int HORIZONTAL_SPLIT = 1
fld public final static int VERTICAL_SPLIT = 0
fld public final static java.lang.String BOTTOM = "bottom"
fld public final static java.lang.String CONTINUOUS_LAYOUT_PROPERTY = "continuousLayout"
fld public final static java.lang.String DIVIDER = "divider"
fld public final static java.lang.String DIVIDER_LOCATION_PROPERTY = "dividerLocation"
fld public final static java.lang.String DIVIDER_SIZE_PROPERTY = "dividerSize"
fld public final static java.lang.String LAST_DIVIDER_LOCATION_PROPERTY = "lastDividerLocation"
fld public final static java.lang.String LEFT = "left"
fld public final static java.lang.String ONE_TOUCH_EXPANDABLE_PROPERTY = "oneTouchExpandable"
fld public final static java.lang.String ORIENTATION_PROPERTY = "orientation"
fld public final static java.lang.String RESIZE_WEIGHT_PROPERTY = "resizeWeight"
fld public final static java.lang.String RIGHT = "right"
fld public final static java.lang.String TOP = "top"
innr protected AccessibleJSplitPane
intf javax.accessibility.Accessible
meth protected java.lang.String paramString()
meth protected void addImpl(java.awt.Component,java.lang.Object,int)
meth protected void paintChildren(java.awt.Graphics)
meth public boolean isContinuousLayout()
meth public boolean isOneTouchExpandable()
meth public boolean isValidateRoot()
meth public double getResizeWeight()
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
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.plaf.SplitPaneUI getUI()
meth public void remove(int)
meth public void remove(java.awt.Component)
meth public void removeAll()
meth public void resetToPreferredSizes()
meth public void setBottomComponent(java.awt.Component)
meth public void setContinuousLayout(boolean)
meth public void setDividerLocation(double)
meth public void setDividerLocation(int)
meth public void setDividerSize(int)
meth public void setLastDividerLocation(int)
meth public void setLeftComponent(java.awt.Component)
meth public void setOneTouchExpandable(boolean)
meth public void setOrientation(int)
meth public void setResizeWeight(double)
meth public void setRightComponent(java.awt.Component)
meth public void setTopComponent(java.awt.Component)
meth public void setUI(javax.swing.plaf.SplitPaneUI)
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

CLSS public javax.swing.JTextArea
cons public init()
cons public init(int,int)
cons public init(java.lang.String)
cons public init(java.lang.String,int,int)
cons public init(javax.swing.text.Document)
cons public init(javax.swing.text.Document,java.lang.String,int,int)
innr protected AccessibleJTextArea
meth protected int getColumnWidth()
meth protected int getRowHeight()
meth protected java.lang.String paramString()
meth protected javax.swing.text.Document createDefaultModel()
meth public boolean getLineWrap()
meth public boolean getScrollableTracksViewportWidth()
meth public boolean getWrapStyleWord()
meth public int getColumns()
meth public int getLineCount()
meth public int getLineEndOffset(int) throws javax.swing.text.BadLocationException
meth public int getLineOfOffset(int) throws javax.swing.text.BadLocationException
meth public int getLineStartOffset(int) throws javax.swing.text.BadLocationException
meth public int getRows()
meth public int getScrollableUnitIncrement(java.awt.Rectangle,int,int)
meth public int getTabSize()
meth public java.awt.Dimension getPreferredScrollableViewportSize()
meth public java.awt.Dimension getPreferredSize()
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public void append(java.lang.String)
meth public void insert(java.lang.String,int)
meth public void replaceRange(java.lang.String,int,int)
meth public void setColumns(int)
meth public void setFont(java.awt.Font)
meth public void setLineWrap(boolean)
meth public void setRows(int)
meth public void setTabSize(int)
meth public void setWrapStyleWord(boolean)
supr javax.swing.text.JTextComponent

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

CLSS public javax.swing.JToolBar
cons public init()
cons public init(int)
cons public init(java.lang.String)
cons public init(java.lang.String,int)
innr protected AccessibleJToolBar
innr public static Separator
intf javax.accessibility.Accessible
intf javax.swing.SwingConstants
meth protected java.beans.PropertyChangeListener createActionChangeListener(javax.swing.JButton)
meth protected java.lang.String paramString()
meth protected javax.swing.JButton createActionComponent(javax.swing.Action)
meth protected void addImpl(java.awt.Component,java.lang.Object,int)
meth protected void paintBorder(java.awt.Graphics)
meth public boolean isBorderPainted()
meth public boolean isFloatable()
meth public boolean isRollover()
meth public int getComponentIndex(java.awt.Component)
meth public int getOrientation()
meth public java.awt.Component getComponentAtIndex(int)
meth public java.awt.Insets getMargin()
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.JButton add(javax.swing.Action)
meth public javax.swing.plaf.ToolBarUI getUI()
meth public void addSeparator()
meth public void addSeparator(java.awt.Dimension)
meth public void setBorderPainted(boolean)
meth public void setFloatable(boolean)
meth public void setLayout(java.awt.LayoutManager)
meth public void setMargin(java.awt.Insets)
meth public void setOrientation(int)
meth public void setRollover(boolean)
meth public void setUI(javax.swing.plaf.ToolBarUI)
meth public void updateUI()
supr javax.swing.JComponent

CLSS public javax.swing.JTree
cons public init()
cons public init(java.lang.Object[])
cons public init(java.util.Hashtable<?,?>)
cons public init(java.util.Vector<?>)
cons public init(javax.swing.tree.TreeModel)
 anno 0 java.beans.ConstructorProperties(java.lang.String[] value=["model"])
cons public init(javax.swing.tree.TreeNode)
cons public init(javax.swing.tree.TreeNode,boolean)
fld protected boolean editable
fld protected boolean invokesStopCellEditing
fld protected boolean largeModel
fld protected boolean rootVisible
fld protected boolean scrollsOnExpand
fld protected boolean showsRootHandles
fld protected int rowHeight
fld protected int toggleClickCount
fld protected int visibleRowCount
fld protected javax.swing.JTree$TreeSelectionRedirector selectionRedirector
fld protected javax.swing.event.TreeModelListener treeModelListener
fld protected javax.swing.tree.TreeCellEditor cellEditor
fld protected javax.swing.tree.TreeCellRenderer cellRenderer
fld protected javax.swing.tree.TreeModel treeModel
fld protected javax.swing.tree.TreeSelectionModel selectionModel
fld public final static java.lang.String ANCHOR_SELECTION_PATH_PROPERTY = "anchorSelectionPath"
fld public final static java.lang.String CELL_EDITOR_PROPERTY = "cellEditor"
fld public final static java.lang.String CELL_RENDERER_PROPERTY = "cellRenderer"
fld public final static java.lang.String EDITABLE_PROPERTY = "editable"
fld public final static java.lang.String EXPANDS_SELECTED_PATHS_PROPERTY = "expandsSelectedPaths"
fld public final static java.lang.String INVOKES_STOP_CELL_EDITING_PROPERTY = "invokesStopCellEditing"
fld public final static java.lang.String LARGE_MODEL_PROPERTY = "largeModel"
fld public final static java.lang.String LEAD_SELECTION_PATH_PROPERTY = "leadSelectionPath"
fld public final static java.lang.String ROOT_VISIBLE_PROPERTY = "rootVisible"
fld public final static java.lang.String ROW_HEIGHT_PROPERTY = "rowHeight"
fld public final static java.lang.String SCROLLS_ON_EXPAND_PROPERTY = "scrollsOnExpand"
fld public final static java.lang.String SELECTION_MODEL_PROPERTY = "selectionModel"
fld public final static java.lang.String SHOWS_ROOT_HANDLES_PROPERTY = "showsRootHandles"
fld public final static java.lang.String TOGGLE_CLICK_COUNT_PROPERTY = "toggleClickCount"
fld public final static java.lang.String TREE_MODEL_PROPERTY = "model"
fld public final static java.lang.String VISIBLE_ROW_COUNT_PROPERTY = "visibleRowCount"
innr protected AccessibleJTree
innr protected TreeModelHandler
innr protected TreeSelectionRedirector
innr protected static EmptySelectionModel
innr public final static DropLocation
innr public static DynamicUtilTreeNode
intf javax.accessibility.Accessible
intf javax.swing.Scrollable
meth protected boolean removeDescendantSelectedPaths(javax.swing.tree.TreePath,boolean)
meth protected java.lang.String paramString()
meth protected java.util.Enumeration<javax.swing.tree.TreePath> getDescendantToggledPaths(javax.swing.tree.TreePath)
meth protected javax.swing.event.TreeModelListener createTreeModelListener()
meth protected javax.swing.tree.TreePath[] getPathBetweenRows(int,int)
meth protected static javax.swing.tree.TreeModel createTreeModel(java.lang.Object)
meth protected static javax.swing.tree.TreeModel getDefaultTreeModel()
meth protected void clearToggledPaths()
meth protected void fireValueChanged(javax.swing.event.TreeSelectionEvent)
meth protected void removeDescendantToggledPaths(java.util.Enumeration<javax.swing.tree.TreePath>)
meth protected void setExpandedState(javax.swing.tree.TreePath,boolean)
meth public boolean getDragEnabled()
meth public boolean getExpandsSelectedPaths()
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
meth public final javax.swing.DropMode getDropMode()
meth public final javax.swing.JTree$DropLocation getDropLocation()
meth public final void setDropMode(javax.swing.DropMode)
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
meth public int getToggleClickCount()
meth public int getVisibleRowCount()
meth public int[] getSelectionRows()
meth public java.awt.Dimension getPreferredScrollableViewportSize()
meth public java.awt.Rectangle getPathBounds(javax.swing.tree.TreePath)
meth public java.awt.Rectangle getRowBounds(int)
meth public java.lang.Object getLastSelectedPathComponent()
meth public java.lang.String convertValueToText(java.lang.Object,boolean,boolean,boolean,int,boolean)
meth public java.lang.String getToolTipText(java.awt.event.MouseEvent)
meth public java.lang.String getUIClassID()
meth public java.util.Enumeration<javax.swing.tree.TreePath> getExpandedDescendants(javax.swing.tree.TreePath)
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.event.TreeExpansionListener[] getTreeExpansionListeners()
meth public javax.swing.event.TreeSelectionListener[] getTreeSelectionListeners()
meth public javax.swing.event.TreeWillExpandListener[] getTreeWillExpandListeners()
meth public javax.swing.plaf.TreeUI getUI()
meth public javax.swing.tree.TreeCellEditor getCellEditor()
meth public javax.swing.tree.TreeCellRenderer getCellRenderer()
meth public javax.swing.tree.TreeModel getModel()
meth public javax.swing.tree.TreePath getAnchorSelectionPath()
meth public javax.swing.tree.TreePath getClosestPathForLocation(int,int)
meth public javax.swing.tree.TreePath getEditingPath()
meth public javax.swing.tree.TreePath getLeadSelectionPath()
meth public javax.swing.tree.TreePath getNextMatch(java.lang.String,int,javax.swing.text.Position$Bias)
meth public javax.swing.tree.TreePath getPathForLocation(int,int)
meth public javax.swing.tree.TreePath getPathForRow(int)
meth public javax.swing.tree.TreePath getSelectionPath()
meth public javax.swing.tree.TreePath[] getSelectionPaths()
meth public javax.swing.tree.TreeSelectionModel getSelectionModel()
meth public void addSelectionInterval(int,int)
meth public void addSelectionPath(javax.swing.tree.TreePath)
meth public void addSelectionPaths(javax.swing.tree.TreePath[])
meth public void addSelectionRow(int)
meth public void addSelectionRows(int[])
meth public void addTreeExpansionListener(javax.swing.event.TreeExpansionListener)
meth public void addTreeSelectionListener(javax.swing.event.TreeSelectionListener)
meth public void addTreeWillExpandListener(javax.swing.event.TreeWillExpandListener)
meth public void cancelEditing()
meth public void clearSelection()
meth public void collapsePath(javax.swing.tree.TreePath)
meth public void collapseRow(int)
meth public void expandPath(javax.swing.tree.TreePath)
meth public void expandRow(int)
meth public void fireTreeCollapsed(javax.swing.tree.TreePath)
meth public void fireTreeExpanded(javax.swing.tree.TreePath)
meth public void fireTreeWillCollapse(javax.swing.tree.TreePath) throws javax.swing.tree.ExpandVetoException
meth public void fireTreeWillExpand(javax.swing.tree.TreePath) throws javax.swing.tree.ExpandVetoException
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
meth public void setAnchorSelectionPath(javax.swing.tree.TreePath)
meth public void setCellEditor(javax.swing.tree.TreeCellEditor)
meth public void setCellRenderer(javax.swing.tree.TreeCellRenderer)
meth public void setDragEnabled(boolean)
meth public void setEditable(boolean)
meth public void setExpandsSelectedPaths(boolean)
meth public void setInvokesStopCellEditing(boolean)
meth public void setLargeModel(boolean)
meth public void setLeadSelectionPath(javax.swing.tree.TreePath)
meth public void setModel(javax.swing.tree.TreeModel)
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
meth public void setToggleClickCount(int)
meth public void setUI(javax.swing.plaf.TreeUI)
meth public void setVisibleRowCount(int)
meth public void startEditingAtPath(javax.swing.tree.TreePath)
meth public void treeDidChange()
meth public void updateUI()
supr javax.swing.JComponent

CLSS public abstract interface javax.swing.MenuElement
meth public abstract java.awt.Component getComponent()
meth public abstract javax.swing.MenuElement[] getSubElements()
meth public abstract void menuSelectionChanged(boolean)
meth public abstract void processKeyEvent(java.awt.event.KeyEvent,javax.swing.MenuElement[],javax.swing.MenuSelectionManager)
meth public abstract void processMouseEvent(java.awt.event.MouseEvent,javax.swing.MenuElement[],javax.swing.MenuSelectionManager)

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

CLSS public abstract javax.swing.border.AbstractBorder
cons public init()
intf java.io.Serializable
intf javax.swing.border.Border
meth public boolean isBorderOpaque()
meth public int getBaseline(java.awt.Component,int,int)
meth public java.awt.Component$BaselineResizeBehavior getBaselineResizeBehavior(java.awt.Component)
meth public java.awt.Insets getBorderInsets(java.awt.Component)
meth public java.awt.Insets getBorderInsets(java.awt.Component,java.awt.Insets)
meth public java.awt.Rectangle getInteriorRectangle(java.awt.Component,int,int,int,int)
meth public static java.awt.Rectangle getInteriorRectangle(java.awt.Component,javax.swing.border.Border,int,int,int,int)
meth public void paintBorder(java.awt.Component,java.awt.Graphics,int,int,int,int)
supr java.lang.Object

CLSS public javax.swing.border.BevelBorder
cons public init(int)
cons public init(int,java.awt.Color,java.awt.Color)
cons public init(int,java.awt.Color,java.awt.Color,java.awt.Color,java.awt.Color)
 anno 0 java.beans.ConstructorProperties(java.lang.String[] value=["bevelType", "highlightOuterColor", "highlightInnerColor", "shadowOuterColor", "shadowInnerColor"])
fld protected int bevelType
fld protected java.awt.Color highlightInner
fld protected java.awt.Color highlightOuter
fld protected java.awt.Color shadowInner
fld protected java.awt.Color shadowOuter
fld public final static int LOWERED = 1
fld public final static int RAISED = 0
meth protected void paintLoweredBevel(java.awt.Component,java.awt.Graphics,int,int,int,int)
meth protected void paintRaisedBevel(java.awt.Component,java.awt.Graphics,int,int,int,int)
meth public boolean isBorderOpaque()
meth public int getBevelType()
meth public java.awt.Color getHighlightInnerColor()
meth public java.awt.Color getHighlightInnerColor(java.awt.Component)
meth public java.awt.Color getHighlightOuterColor()
meth public java.awt.Color getHighlightOuterColor(java.awt.Component)
meth public java.awt.Color getShadowInnerColor()
meth public java.awt.Color getShadowInnerColor(java.awt.Component)
meth public java.awt.Color getShadowOuterColor()
meth public java.awt.Color getShadowOuterColor(java.awt.Component)
meth public java.awt.Insets getBorderInsets(java.awt.Component,java.awt.Insets)
meth public void paintBorder(java.awt.Component,java.awt.Graphics,int,int,int,int)
supr javax.swing.border.AbstractBorder

CLSS public abstract interface javax.swing.border.Border
meth public abstract boolean isBorderOpaque()
meth public abstract java.awt.Insets getBorderInsets(java.awt.Component)
meth public abstract void paintBorder(java.awt.Component,java.awt.Graphics,int,int,int,int)

CLSS public abstract interface javax.swing.event.AncestorListener
intf java.util.EventListener
meth public abstract void ancestorAdded(javax.swing.event.AncestorEvent)
meth public abstract void ancestorMoved(javax.swing.event.AncestorEvent)
meth public abstract void ancestorRemoved(javax.swing.event.AncestorEvent)

CLSS public abstract interface javax.swing.event.CellEditorListener
intf java.util.EventListener
meth public abstract void editingCanceled(javax.swing.event.ChangeEvent)
meth public abstract void editingStopped(javax.swing.event.ChangeEvent)

CLSS public abstract interface javax.swing.event.HyperlinkListener
intf java.util.EventListener
meth public abstract void hyperlinkUpdate(javax.swing.event.HyperlinkEvent)

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

CLSS public abstract javax.swing.filechooser.FileFilter
cons public init()
meth public abstract boolean accept(java.io.File)
meth public abstract java.lang.String getDescription()
supr java.lang.Object

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

CLSS public javax.swing.table.DefaultTableModel
cons public init()
cons public init(int,int)
cons public init(java.lang.Object[],int)
cons public init(java.lang.Object[][],java.lang.Object[])
cons public init(java.util.Vector,int)
cons public init(java.util.Vector,java.util.Vector)
fld protected java.util.Vector columnIdentifiers
fld protected java.util.Vector dataVector
intf java.io.Serializable
meth protected static java.util.Vector convertToVector(java.lang.Object[])
meth protected static java.util.Vector convertToVector(java.lang.Object[][])
meth public boolean isCellEditable(int,int)
meth public int getColumnCount()
meth public int getRowCount()
meth public java.lang.Object getValueAt(int,int)
meth public java.lang.String getColumnName(int)
meth public java.util.Vector getDataVector()
meth public void addColumn(java.lang.Object)
meth public void addColumn(java.lang.Object,java.lang.Object[])
meth public void addColumn(java.lang.Object,java.util.Vector)
meth public void addRow(java.lang.Object[])
meth public void addRow(java.util.Vector)
meth public void insertRow(int,java.lang.Object[])
meth public void insertRow(int,java.util.Vector)
meth public void moveRow(int,int,int)
meth public void newDataAvailable(javax.swing.event.TableModelEvent)
meth public void newRowsAdded(javax.swing.event.TableModelEvent)
meth public void removeRow(int)
meth public void rowsRemoved(javax.swing.event.TableModelEvent)
meth public void setColumnCount(int)
meth public void setColumnIdentifiers(java.lang.Object[])
meth public void setColumnIdentifiers(java.util.Vector)
meth public void setDataVector(java.lang.Object[][],java.lang.Object[])
meth public void setDataVector(java.util.Vector,java.util.Vector)
meth public void setNumRows(int)
meth public void setRowCount(int)
meth public void setValueAt(java.lang.Object,int,int)
supr javax.swing.table.AbstractTableModel

CLSS public abstract interface javax.swing.table.TableCellRenderer
meth public abstract java.awt.Component getTableCellRendererComponent(javax.swing.JTable,java.lang.Object,boolean,boolean,int,int)

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

CLSS public abstract interface javax.swing.text.Caret
meth public abstract boolean isSelectionVisible()
meth public abstract boolean isVisible()
meth public abstract int getBlinkRate()
meth public abstract int getDot()
meth public abstract int getMark()
meth public abstract java.awt.Point getMagicCaretPosition()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void deinstall(javax.swing.text.JTextComponent)
meth public abstract void install(javax.swing.text.JTextComponent)
meth public abstract void moveDot(int)
meth public abstract void paint(java.awt.Graphics)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public abstract void setBlinkRate(int)
meth public abstract void setDot(int)
meth public abstract void setMagicCaretPosition(java.awt.Point)
meth public abstract void setSelectionVisible(boolean)
meth public abstract void setVisible(boolean)

CLSS public abstract javax.swing.text.JTextComponent
cons public init()
fld public final static java.lang.String DEFAULT_KEYMAP = "default"
fld public final static java.lang.String FOCUS_ACCELERATOR_KEY = "focusAcceleratorKey"
innr public AccessibleJTextComponent
innr public final static DropLocation
innr public static KeyBinding
intf javax.accessibility.Accessible
intf javax.swing.Scrollable
meth protected boolean saveComposedText(int)
meth protected java.lang.String paramString()
meth protected void fireCaretUpdate(javax.swing.event.CaretEvent)
meth protected void processInputMethodEvent(java.awt.event.InputMethodEvent)
meth protected void restoreComposedText()
meth public boolean getDragEnabled()
meth public boolean getScrollableTracksViewportHeight()
meth public boolean getScrollableTracksViewportWidth()
meth public boolean isEditable()
meth public boolean print() throws java.awt.print.PrinterException
meth public boolean print(java.text.MessageFormat,java.text.MessageFormat) throws java.awt.print.PrinterException
meth public boolean print(java.text.MessageFormat,java.text.MessageFormat,boolean,javax.print.PrintService,javax.print.attribute.PrintRequestAttributeSet,boolean) throws java.awt.print.PrinterException
meth public char getFocusAccelerator()
meth public final javax.swing.DropMode getDropMode()
meth public final javax.swing.text.JTextComponent$DropLocation getDropLocation()
meth public final void setDropMode(javax.swing.DropMode)
meth public int getCaretPosition()
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
meth public java.awt.Rectangle modelToView(int) throws javax.swing.text.BadLocationException
meth public java.awt.im.InputMethodRequests getInputMethodRequests()
meth public java.awt.print.Printable getPrintable(java.text.MessageFormat,java.text.MessageFormat)
meth public java.lang.String getSelectedText()
meth public java.lang.String getText()
meth public java.lang.String getText(int,int) throws javax.swing.text.BadLocationException
meth public java.lang.String getToolTipText(java.awt.event.MouseEvent)
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.Action[] getActions()
meth public javax.swing.event.CaretListener[] getCaretListeners()
meth public javax.swing.plaf.TextUI getUI()
meth public javax.swing.text.Caret getCaret()
meth public javax.swing.text.Document getDocument()
meth public javax.swing.text.Highlighter getHighlighter()
meth public javax.swing.text.Keymap getKeymap()
meth public javax.swing.text.NavigationFilter getNavigationFilter()
meth public static javax.swing.text.Keymap addKeymap(java.lang.String,javax.swing.text.Keymap)
meth public static javax.swing.text.Keymap getKeymap(java.lang.String)
meth public static javax.swing.text.Keymap removeKeymap(java.lang.String)
meth public static void loadKeymap(javax.swing.text.Keymap,javax.swing.text.JTextComponent$KeyBinding[],javax.swing.Action[])
meth public void addCaretListener(javax.swing.event.CaretListener)
meth public void addInputMethodListener(java.awt.event.InputMethodListener)
meth public void copy()
meth public void cut()
meth public void moveCaretPosition(int)
meth public void paste()
meth public void read(java.io.Reader,java.lang.Object) throws java.io.IOException
meth public void removeCaretListener(javax.swing.event.CaretListener)
meth public void removeNotify()
meth public void replaceSelection(java.lang.String)
meth public void select(int,int)
meth public void selectAll()
meth public void setCaret(javax.swing.text.Caret)
meth public void setCaretColor(java.awt.Color)
meth public void setCaretPosition(int)
meth public void setComponentOrientation(java.awt.ComponentOrientation)
meth public void setDisabledTextColor(java.awt.Color)
meth public void setDocument(javax.swing.text.Document)
meth public void setDragEnabled(boolean)
meth public void setEditable(boolean)
meth public void setFocusAccelerator(char)
meth public void setHighlighter(javax.swing.text.Highlighter)
meth public void setKeymap(javax.swing.text.Keymap)
meth public void setMargin(java.awt.Insets)
meth public void setNavigationFilter(javax.swing.text.NavigationFilter)
meth public void setSelectedTextColor(java.awt.Color)
meth public void setSelectionColor(java.awt.Color)
meth public void setSelectionEnd(int)
meth public void setSelectionStart(int)
meth public void setText(java.lang.String)
meth public void setUI(javax.swing.plaf.TextUI)
meth public void updateUI()
meth public void write(java.io.Writer) throws java.io.IOException
supr javax.swing.JComponent

CLSS public javax.swing.tree.DefaultMutableTreeNode
cons public init()
cons public init(java.lang.Object)
cons public init(java.lang.Object,boolean)
fld protected boolean allowsChildren
fld protected java.lang.Object userObject
fld protected java.util.Vector children
fld protected javax.swing.tree.MutableTreeNode parent
fld public final static java.util.Enumeration<javax.swing.tree.TreeNode> EMPTY_ENUMERATION
intf java.io.Serializable
intf java.lang.Cloneable
intf javax.swing.tree.MutableTreeNode
meth protected javax.swing.tree.TreeNode[] getPathToRoot(javax.swing.tree.TreeNode,int)
meth public boolean getAllowsChildren()
meth public boolean isLeaf()
meth public boolean isNodeAncestor(javax.swing.tree.TreeNode)
meth public boolean isNodeChild(javax.swing.tree.TreeNode)
meth public boolean isNodeDescendant(javax.swing.tree.DefaultMutableTreeNode)
meth public boolean isNodeRelated(javax.swing.tree.DefaultMutableTreeNode)
meth public boolean isNodeSibling(javax.swing.tree.TreeNode)
meth public boolean isRoot()
meth public int getChildCount()
meth public int getDepth()
meth public int getIndex(javax.swing.tree.TreeNode)
meth public int getLeafCount()
meth public int getLevel()
meth public int getSiblingCount()
meth public java.lang.Object clone()
meth public java.lang.Object getUserObject()
meth public java.lang.Object[] getUserObjectPath()
meth public java.lang.String toString()
meth public java.util.Enumeration breadthFirstEnumeration()
meth public java.util.Enumeration children()
meth public java.util.Enumeration depthFirstEnumeration()
meth public java.util.Enumeration pathFromAncestorEnumeration(javax.swing.tree.TreeNode)
meth public java.util.Enumeration postorderEnumeration()
meth public java.util.Enumeration preorderEnumeration()
meth public javax.swing.tree.DefaultMutableTreeNode getFirstLeaf()
meth public javax.swing.tree.DefaultMutableTreeNode getLastLeaf()
meth public javax.swing.tree.DefaultMutableTreeNode getNextLeaf()
meth public javax.swing.tree.DefaultMutableTreeNode getNextNode()
meth public javax.swing.tree.DefaultMutableTreeNode getNextSibling()
meth public javax.swing.tree.DefaultMutableTreeNode getPreviousLeaf()
meth public javax.swing.tree.DefaultMutableTreeNode getPreviousNode()
meth public javax.swing.tree.DefaultMutableTreeNode getPreviousSibling()
meth public javax.swing.tree.TreeNode getChildAfter(javax.swing.tree.TreeNode)
meth public javax.swing.tree.TreeNode getChildAt(int)
meth public javax.swing.tree.TreeNode getChildBefore(javax.swing.tree.TreeNode)
meth public javax.swing.tree.TreeNode getFirstChild()
meth public javax.swing.tree.TreeNode getLastChild()
meth public javax.swing.tree.TreeNode getParent()
meth public javax.swing.tree.TreeNode getRoot()
meth public javax.swing.tree.TreeNode getSharedAncestor(javax.swing.tree.DefaultMutableTreeNode)
meth public javax.swing.tree.TreeNode[] getPath()
meth public void add(javax.swing.tree.MutableTreeNode)
meth public void insert(javax.swing.tree.MutableTreeNode,int)
meth public void remove(int)
meth public void remove(javax.swing.tree.MutableTreeNode)
meth public void removeAllChildren()
meth public void removeFromParent()
meth public void setAllowsChildren(boolean)
meth public void setParent(javax.swing.tree.MutableTreeNode)
meth public void setUserObject(java.lang.Object)
supr java.lang.Object

CLSS public abstract interface javax.swing.tree.MutableTreeNode
intf javax.swing.tree.TreeNode
meth public abstract void insert(javax.swing.tree.MutableTreeNode,int)
meth public abstract void remove(int)
meth public abstract void remove(javax.swing.tree.MutableTreeNode)
meth public abstract void removeFromParent()
meth public abstract void setParent(javax.swing.tree.MutableTreeNode)
meth public abstract void setUserObject(java.lang.Object)

CLSS public abstract interface javax.swing.tree.TreeCellRenderer
meth public abstract java.awt.Component getTreeCellRendererComponent(javax.swing.JTree,java.lang.Object,boolean,boolean,boolean,int,boolean)

CLSS public abstract interface javax.swing.tree.TreeModel
meth public abstract boolean isLeaf(java.lang.Object)
meth public abstract int getChildCount(java.lang.Object)
meth public abstract int getIndexOfChild(java.lang.Object,java.lang.Object)
meth public abstract java.lang.Object getChild(java.lang.Object,int)
meth public abstract java.lang.Object getRoot()
meth public abstract void addTreeModelListener(javax.swing.event.TreeModelListener)
meth public abstract void removeTreeModelListener(javax.swing.event.TreeModelListener)
meth public abstract void valueForPathChanged(javax.swing.tree.TreePath,java.lang.Object)

CLSS public abstract interface javax.swing.tree.TreeNode
meth public abstract boolean getAllowsChildren()
meth public abstract boolean isLeaf()
meth public abstract int getChildCount()
meth public abstract int getIndex(javax.swing.tree.TreeNode)
meth public abstract java.util.Enumeration children()
meth public abstract javax.swing.tree.TreeNode getChildAt(int)
meth public abstract javax.swing.tree.TreeNode getParent()

CLSS public org.netbeans.lib.profiler.charts.ChartComponent
cons public init()
fld protected org.netbeans.lib.profiler.charts.swing.LongRect dataBounds
innr protected static Context
meth protected final void contentsUpdated(long,long,double,double,long,long,double,double,int,int)
meth protected final void contentsWillBeUpdated(long,long,double,double,long,long,double,double)
meth protected final void dataBoundsChanged(long,long,long,long,long,long,long,long)
meth protected final void offsetChanged(long,long,long,long)
meth protected final void scaleChanged(double,double,double,double)
meth protected org.netbeans.lib.profiler.charts.ChartContext createChartContext()
meth protected org.netbeans.lib.profiler.charts.ChartContext getChartContext(org.netbeans.lib.profiler.charts.ChartItem)
meth protected void computeDataBounds()
meth protected void itemsAdded(java.util.List<org.netbeans.lib.profiler.charts.ChartItem>)
meth protected void itemsChanged(java.util.List<org.netbeans.lib.profiler.charts.ChartItemChange>)
meth protected void itemsRemoved(java.util.List<org.netbeans.lib.profiler.charts.ChartItem>)
meth protected void paintContents(java.awt.Graphics,java.awt.Rectangle)
meth protected void paintersChanged()
meth protected void paintersChanged(java.util.List<org.netbeans.lib.profiler.charts.ItemPainter>)
meth protected void reshaped(java.awt.Rectangle,java.awt.Rectangle)
meth protected void resizeChart()
meth protected void updateChart()
meth public final java.awt.RenderingHints getRenderingHints()
meth public final org.netbeans.lib.profiler.charts.ChartContext getChartContext()
meth public final org.netbeans.lib.profiler.charts.ChartSelectionModel getSelectionModel()
meth public final org.netbeans.lib.profiler.charts.ItemsModel getItemsModel()
meth public final org.netbeans.lib.profiler.charts.PaintersModel getPaintersModel()
meth public final org.netbeans.lib.profiler.charts.swing.LongRect getInitialDataBounds()
meth public final void addConfigurationListener(org.netbeans.lib.profiler.charts.ChartConfigurationListener)
meth public final void addOverlayComponent(org.netbeans.lib.profiler.charts.ChartOverlay)
meth public final void addPostDecorator(org.netbeans.lib.profiler.charts.ChartDecorator)
meth public final void addPreDecorator(org.netbeans.lib.profiler.charts.ChartDecorator)
meth public final void removeConfigurationListener(org.netbeans.lib.profiler.charts.ChartConfigurationListener)
meth public final void removeOverlayComponent(org.netbeans.lib.profiler.charts.ChartOverlay)
meth public final void removePostDecorator(org.netbeans.lib.profiler.charts.ChartDecorator)
meth public final void removePreDecorator(org.netbeans.lib.profiler.charts.ChartDecorator)
meth public final void setInitialDataBounds(org.netbeans.lib.profiler.charts.swing.LongRect)
meth public final void setItemsModel(org.netbeans.lib.profiler.charts.ItemsModel)
meth public final void setPaintersModel(org.netbeans.lib.profiler.charts.PaintersModel)
meth public final void setRenderingHints(java.awt.RenderingHints)
meth public final void setSelectionModel(org.netbeans.lib.profiler.charts.ChartSelectionModel)
meth public void setBackground(java.awt.Color)
supr org.netbeans.lib.profiler.charts.canvas.InteractiveCanvasComponent
hfds chartContext,configurationListeners,initialDataBounds,itemsListener,itemsModel,overlays,paintersListener,paintersModel,postDecorators,preDecorators,renderingHints,selectionListener,selectionModel
hcls ItemsModelListener,PaintersModelListener,SelectionListener

CLSS public abstract interface org.netbeans.lib.profiler.charts.ChartItem
innr public abstract static Abstract
meth public abstract void addItemListener(org.netbeans.lib.profiler.charts.ChartItemListener)
meth public abstract void removeItemListener(org.netbeans.lib.profiler.charts.ChartItemListener)

CLSS public abstract org.netbeans.lib.profiler.charts.ChartOverlay
cons public init()
meth protected final org.netbeans.lib.profiler.charts.ChartContext getChartContext()
supr javax.swing.JComponent
hfds context

CLSS public abstract interface org.netbeans.lib.profiler.charts.ItemPainter
meth public abstract boolean isAppearanceChange(org.netbeans.lib.profiler.charts.ChartItemChange)
meth public abstract boolean isBoundsChange(org.netbeans.lib.profiler.charts.ChartItemChange)
meth public abstract boolean supportsHovering(org.netbeans.lib.profiler.charts.ChartItem)
meth public abstract boolean supportsSelecting(org.netbeans.lib.profiler.charts.ChartItem)
meth public abstract org.netbeans.lib.profiler.charts.ItemSelection getClosestSelection(org.netbeans.lib.profiler.charts.ChartItem,int,int,org.netbeans.lib.profiler.charts.ChartContext)
meth public abstract org.netbeans.lib.profiler.charts.swing.LongRect getDirtyBounds(org.netbeans.lib.profiler.charts.ChartItemChange,org.netbeans.lib.profiler.charts.ChartContext)
meth public abstract org.netbeans.lib.profiler.charts.swing.LongRect getItemBounds(org.netbeans.lib.profiler.charts.ChartItem)
meth public abstract org.netbeans.lib.profiler.charts.swing.LongRect getItemBounds(org.netbeans.lib.profiler.charts.ChartItem,org.netbeans.lib.profiler.charts.ChartContext)
meth public abstract org.netbeans.lib.profiler.charts.swing.LongRect getSelectionBounds(org.netbeans.lib.profiler.charts.ItemSelection,org.netbeans.lib.profiler.charts.ChartContext)
meth public abstract void paintItem(org.netbeans.lib.profiler.charts.ChartItem,java.util.List<org.netbeans.lib.profiler.charts.ItemSelection>,java.util.List<org.netbeans.lib.profiler.charts.ItemSelection>,java.awt.Graphics2D,java.awt.Rectangle,org.netbeans.lib.profiler.charts.ChartContext)

CLSS public abstract org.netbeans.lib.profiler.charts.canvas.BufferedCanvasComponent
cons public init()
cons public init(int)
fld public final static int BUFFER_IMAGE = 1
fld public final static int BUFFER_NONE = 0
fld public final static int BUFFER_VOLATILE_IMAGE = 2
meth protected abstract void paintComponent(java.awt.Graphics,java.awt.Rectangle)
meth protected boolean canDirectlyAccessGraphics()
meth protected final boolean isBuffered()
meth protected final float getAccelerationPriority()
meth protected final int getBufferType()
meth protected final void invalidateImage()
meth protected final void invalidateImage(java.awt.Rectangle)
meth protected final void paintBorder(java.awt.Graphics)
meth protected final void paintChildren(java.awt.Graphics)
meth protected final void paintComponent(java.awt.Graphics)
meth protected final void releaseOffscreenImage()
meth protected final void setAccelerationPriority(float)
meth protected final void setBufferType(int)
meth protected final void weaklyReleaseOffscreenImage()
meth protected void hidden()
meth protected void reshaped(java.awt.Rectangle,java.awt.Rectangle)
meth protected void shown()
meth protected void windowDeiconified()
meth protected void windowIconified()
meth public final java.awt.Insets getInsets()
meth public final java.awt.Insets getInsets(java.awt.Insets)
meth public final void paint(java.awt.Graphics)
meth public final void repaintDirty()
meth public final void repaintDirty(java.awt.Rectangle)
meth public final void repaintDirtyAccel()
meth public final void reshape(int,int,int,int)
meth public final void setBorder(javax.swing.border.Border)
meth public final void update(java.awt.Graphics)
supr javax.swing.JComponent
hfds ACCEL_DISABLED,DEFAULT_BUFFER,ZERO_INSETS,accelerationPriority,bufferType,invalidOffscreenArea,offscreenImage,offscreenImageReference
hcls VisibilityHandler

CLSS public abstract org.netbeans.lib.profiler.charts.canvas.InteractiveCanvasComponent
cons public init()
fld public final static int ZOOM_ALL = 0
fld public final static int ZOOM_X = 1
fld public final static int ZOOM_Y = 2
meth protected void dataBoundsChanged(long,long,long,long,long,long,long,long)
meth protected void offsetChanged(long,long,long,long)
meth protected void reshaped(java.awt.Rectangle,java.awt.Rectangle)
meth protected void scaleChanged(double,double,double,double)
meth public final boolean isMousePanningEnabled()
meth public final boolean isMouseZoomingEnabled()
meth public final boolean panningPossible()
meth public final double getMouseZoomingFactor()
meth public final int getMousePanningButton()
meth public final int getZoomMode()
meth public final java.awt.Cursor getMousePanningCursor()
meth public final void attachHorizontalScrollBar(javax.swing.JScrollBar)
meth public final void attachVerticalScrollBar(javax.swing.JScrollBar)
meth public final void detachHorizontalScrollBar()
meth public final void detachVerticalScrollBar()
meth public final void disableMousePanning()
meth public final void disableMouseZooming()
meth public final void enableMousePanning()
meth public final void enableMouseZooming()
meth public final void setMousePanningButton(int)
meth public final void setMousePanningCursor(java.awt.Cursor)
meth public final void setMousePanningEnabled(boolean)
meth public final void setMouseZoomingEnabled(boolean)
meth public final void setMouseZoomingFactor(double)
meth public final void setZoomMode(int)
meth public final void zoom(int,int,double)
supr org.netbeans.lib.profiler.charts.canvas.TransformableCanvasComponent
hfds hScrollBarManager,mousePanHandler,mousePanningButton,mousePanningCursor,mouseZoomHandler,mouseZoomingFactor,vScrollBarManager,zoomMode
hcls MousePanHandler,MouseZoomHandler,ScrollBarManager

CLSS public abstract org.netbeans.lib.profiler.charts.canvas.TransformableCanvasComponent
cons public init()
meth protected abstract void paintContents(java.awt.Graphics,java.awt.Rectangle)
meth protected final boolean isHOffsetAdjusting()
meth protected final boolean isOffsetAdjusting()
meth protected final boolean isVOffsetAdjusting()
meth protected final double getDataHeight(double)
meth protected final double getDataWidth(double)
meth protected final double getDataX(double)
meth protected final double getDataY(double)
meth protected final double getReversedDataX(double)
meth protected final double getReversedDataY(double)
meth protected final double getReversedViewX(double)
meth protected final double getReversedViewY(double)
meth protected final double getViewHeight(double)
meth protected final double getViewWidth(double)
meth protected final double getViewX(double)
meth protected final double getViewY(double)
meth protected final long getMaxOffsetX()
meth protected final long getMaxOffsetY()
meth protected final void hOffsetAdjustingFinished()
meth protected final void hOffsetAdjustingStarted()
meth protected final void offsetAdjustingFinished()
meth protected final void offsetAdjustingStarted()
meth protected final void paintComponent(java.awt.Graphics,java.awt.Rectangle)
meth protected final void vOffsetAdjustingFinished()
meth protected final void vOffsetAdjustingStarted()
meth protected void contentsUpdated(long,long,double,double,long,long,double,double,int,int)
meth protected void contentsWillBeUpdated(long,long,double,double,long,long,double,double)
meth protected void dataBoundsChanged(long,long,long,long,long,long,long,long)
meth protected void offsetChanged(long,long,long,long)
meth protected void reshaped(java.awt.Rectangle,java.awt.Rectangle)
meth protected void scaleChanged(double,double,double,double)
meth public final boolean currentlyFollowingDataHeight()
meth public final boolean currentlyFollowingDataWidth()
meth public final boolean fitsHeight()
meth public final boolean fitsWidth()
meth public final boolean isBottomBased()
meth public final boolean isRightBased()
meth public final boolean tracksDataHeight()
meth public final boolean tracksDataOffsetX()
meth public final boolean tracksDataOffsetY()
meth public final boolean tracksDataWidth()
meth public final double getScaleX()
meth public final double getScaleY()
meth public final java.awt.Insets getViewInsets()
meth public final long getContentsHeight()
meth public final long getContentsWidth()
meth public final long getDataHeight()
meth public final long getDataOffsetX()
meth public final long getDataOffsetY()
meth public final long getDataWidth()
meth public final long getOffsetX()
meth public final long getOffsetY()
meth public final void setBottomBased(boolean)
meth public final void setDataBounds(long,long,long,long)
meth public final void setFitsHeight(boolean)
meth public final void setFitsWidth(boolean)
meth public final void setOffset(long,long)
meth public final void setRightBased(boolean)
meth public final void setScale(double,double)
meth public final void setTracksDataHeight(boolean)
meth public final void setTracksDataOffsetX(boolean)
meth public final void setTracksDataOffsetY(boolean)
meth public final void setTracksDataWidth(boolean)
meth public final void setViewInsets(java.awt.Insets)
supr org.netbeans.lib.profiler.charts.canvas.BufferedCanvasComponent
hfds DIAGONAL_SHIFT_ACCEL_LIMIT,SHIFT_ACCEL_LIMIT,bottomBased,contentsHeight,contentsOffsetX,contentsOffsetY,contentsWidth,dataHeight,dataOffsetX,dataOffsetY,dataWidth,dx,dy,fitsHeight,fitsWidth,hOffsetAdjustingCounter,lastOffsetX,lastOffsetY,lastScaleX,lastScaleY,maxOffsetX,maxOffsetY,offsetX,offsetY,oldScaleX,oldScaleY,pendingDataHeight,pendingDataOffsetX,pendingDataOffsetY,pendingDataWidth,rightBased,scaleX,scaleY,tracksDataHeight,tracksDataOffsetX,tracksDataOffsetY,tracksDataWidth,vOffsetAdjustingCounter,viewInsets

CLSS public abstract interface org.netbeans.lib.profiler.charts.xy.XYItem
intf org.netbeans.lib.profiler.charts.ChartItem
meth public abstract int getValuesCount()
meth public abstract long getXValue(int)
meth public abstract long getYValue(int)
meth public abstract org.netbeans.lib.profiler.charts.swing.LongRect getBounds()

CLSS public abstract interface org.netbeans.lib.profiler.charts.xy.XYItemPainter
fld public final static int TYPE_ABSOLUTE = 0
fld public final static int TYPE_RELATIVE = 1
innr public abstract static Abstract
intf org.netbeans.lib.profiler.charts.ItemPainter
meth public abstract double getItemValue(double,org.netbeans.lib.profiler.charts.xy.XYItem,org.netbeans.lib.profiler.charts.ChartContext)
meth public abstract double getItemValueScale(org.netbeans.lib.profiler.charts.xy.XYItem,org.netbeans.lib.profiler.charts.ChartContext)
meth public abstract double getItemView(double,org.netbeans.lib.profiler.charts.xy.XYItem,org.netbeans.lib.profiler.charts.ChartContext)

CLSS public abstract static org.netbeans.lib.profiler.charts.xy.XYItemPainter$Abstract
 outer org.netbeans.lib.profiler.charts.xy.XYItemPainter
cons public init()
intf org.netbeans.lib.profiler.charts.xy.XYItemPainter
meth public double getItemValue(double,org.netbeans.lib.profiler.charts.xy.XYItem,org.netbeans.lib.profiler.charts.ChartContext)
meth public double getItemValueScale(org.netbeans.lib.profiler.charts.xy.XYItem,org.netbeans.lib.profiler.charts.ChartContext)
meth public double getItemView(double,org.netbeans.lib.profiler.charts.xy.XYItem,org.netbeans.lib.profiler.charts.ChartContext)
supr java.lang.Object

CLSS public org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYChart
cons public init(org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYItemsModel,org.netbeans.lib.profiler.charts.PaintersModel)
innr protected static Context
meth protected org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYChart$Context createChartContext()
meth protected void reshaped(java.awt.Rectangle,java.awt.Rectangle)
meth public int getNearestTimestampIndex(int,int)
supr org.netbeans.lib.profiler.charts.ChartComponent
hfds VISIBLE_NONE,contentsWidthChanged,firstVisibleIndex,indexesCache,lastVisibleIndex,newBoundsWidth,newOffsetX,newScaleX,oldBoundsWidth,oldOffsetX,oldScaleX,timeline,visibleIndexesDirty
hcls VisibleBoundsListener

CLSS public abstract org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYItem
cons public init(java.lang.String)
cons public init(java.lang.String,long)
cons public init(java.lang.String,long,long)
intf org.netbeans.lib.profiler.charts.xy.XYItem
meth public abstract long getYValue(int)
meth public int getValuesCount()
meth public java.lang.String getName()
meth public long getMaxYValue()
meth public long getMinYValue()
meth public long getXValue(int)
meth public org.netbeans.lib.profiler.charts.swing.LongRect getBounds()
meth public org.netbeans.lib.profiler.charts.swing.LongRect getInitialBounds()
meth public org.netbeans.lib.profiler.charts.xy.XYItemChange valuesChanged()
meth public void addItemListener(org.netbeans.lib.profiler.charts.ChartItemListener)
meth public void removeItemListener(org.netbeans.lib.profiler.charts.ChartItemListener)
meth public void setInitialBounds(org.netbeans.lib.profiler.charts.swing.LongRect)
supr java.lang.Object
hfds bounds,initialBounds,initialMaxY,initialMinY,lastIndex,maxY,minY,name,timeline

CLSS public org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYItemPainter
cons public init(float,java.awt.Color,java.awt.Color,int,int)
fld protected final int lineWidth
fld protected final int maxValueOffset
fld protected final int type
fld protected final java.awt.Color fillColor
fld protected final java.awt.Color lineColor
fld protected final java.awt.Stroke lineStroke
meth protected void paint(org.netbeans.lib.profiler.charts.xy.XYItem,java.util.List<org.netbeans.lib.profiler.charts.ItemSelection>,java.util.List<org.netbeans.lib.profiler.charts.ItemSelection>,java.awt.Graphics2D,java.awt.Rectangle,org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYChartContext)
meth public boolean isAppearanceChange(org.netbeans.lib.profiler.charts.ChartItemChange)
meth public boolean isBoundsChange(org.netbeans.lib.profiler.charts.ChartItemChange)
meth public boolean supportsHovering(org.netbeans.lib.profiler.charts.ChartItem)
meth public boolean supportsSelecting(org.netbeans.lib.profiler.charts.ChartItem)
meth public double getItemValue(double,org.netbeans.lib.profiler.charts.xy.XYItem,org.netbeans.lib.profiler.charts.ChartContext)
meth public double getItemValueScale(org.netbeans.lib.profiler.charts.xy.XYItem,org.netbeans.lib.profiler.charts.ChartContext)
meth public double getItemView(double,org.netbeans.lib.profiler.charts.xy.XYItem,org.netbeans.lib.profiler.charts.ChartContext)
meth public org.netbeans.lib.profiler.charts.ItemSelection getClosestSelection(org.netbeans.lib.profiler.charts.ChartItem,int,int,org.netbeans.lib.profiler.charts.ChartContext)
meth public org.netbeans.lib.profiler.charts.swing.LongRect getDirtyBounds(org.netbeans.lib.profiler.charts.ChartItemChange,org.netbeans.lib.profiler.charts.ChartContext)
meth public org.netbeans.lib.profiler.charts.swing.LongRect getItemBounds(org.netbeans.lib.profiler.charts.ChartItem)
meth public org.netbeans.lib.profiler.charts.swing.LongRect getItemBounds(org.netbeans.lib.profiler.charts.ChartItem,org.netbeans.lib.profiler.charts.ChartContext)
meth public org.netbeans.lib.profiler.charts.swing.LongRect getSelectionBounds(org.netbeans.lib.profiler.charts.ItemSelection,org.netbeans.lib.profiler.charts.ChartContext)
meth public static org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYItemPainter absolutePainter(float,java.awt.Color,java.awt.Color)
meth public static org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYItemPainter relativePainter(float,java.awt.Color,java.awt.Color,int)
meth public void paintItem(org.netbeans.lib.profiler.charts.ChartItem,java.util.List<org.netbeans.lib.profiler.charts.ItemSelection>,java.util.List<org.netbeans.lib.profiler.charts.ItemSelection>,java.awt.Graphics2D,java.awt.Rectangle,org.netbeans.lib.profiler.charts.ChartContext)
supr org.netbeans.lib.profiler.charts.xy.XYItemPainter$Abstract

CLSS public org.netbeans.lib.profiler.filters.GenericFilter
cons public init()
cons public init(java.lang.String,java.lang.String,int)
cons public init(java.util.Properties,java.lang.String)
cons public init(org.netbeans.lib.profiler.filters.GenericFilter)
fld protected final static int MODE_CONTAINS = 1010
fld protected final static int MODE_ENDS_WITH = 1030
fld protected final static int MODE_EQUALS = 1000
fld protected final static int MODE_STARTS_WITH = 1020
fld public final static int TYPE_EXCLUSIVE = 20
fld public final static int TYPE_INCLUSIVE = 10
fld public final static int TYPE_NONE = 0
innr public final static InvalidFilterIdException
meth protected boolean matches(java.lang.String,java.lang.String,int)
meth protected boolean simplePasses(java.lang.String)
meth protected boolean valuesEquals(java.lang.Object)
meth protected int valuesHashCode(int)
meth protected int[] computeModes(java.lang.String[])
meth protected java.lang.String[] computeValues(java.lang.String)
meth protected void valueChanged()
meth public boolean equals(java.lang.Object)
meth public boolean isAll()
meth public boolean passes(java.lang.String)
meth public final boolean isEmpty()
meth public final int getType()
meth public final int[] getModes()
meth public final java.lang.String getName()
meth public final java.lang.String getValue()
meth public final java.lang.String[] getValues()
meth public final void setName(java.lang.String)
meth public final void setType(int)
meth public final void setValue(java.lang.String)
meth public int hashCode()
meth public java.lang.String toString()
meth public static java.lang.String[] values(java.lang.String)
meth public void copyFrom(org.netbeans.lib.profiler.filters.GenericFilter)
meth public void store(java.util.Properties,java.lang.String)
supr java.lang.Object
hfds PROP_NAME,PROP_TYPE,PROP_VALUE,isAll,isEmpty,modes,name,type,value,values

CLSS public abstract interface org.netbeans.lib.profiler.global.CommonConstants
fld public final static byte ADJUST_TIME = 5
fld public final static byte BUFFEREVENT_PROFILEPOINT_HIT = 29
fld public final static byte COMPACT_EVENT_FORMAT_BYTE_MASK = -128
fld public final static byte MARKER_ENTRY = 3
fld public final static byte MARKER_ENTRY_PARAMETERS = 35
fld public final static byte MARKER_ENTRY_UNSTAMPED = 18
fld public final static byte MARKER_EXIT = 4
fld public final static byte MARKER_EXIT_UNSTAMPED = 19
fld public final static byte METHOD_ENTRY = 6
fld public final static byte METHOD_ENTRY_COMPACT_BYTE_MASK = -128
fld public final static byte METHOD_ENTRY_MONITOR = 22
fld public final static byte METHOD_ENTRY_PARK = 26
fld public final static byte METHOD_ENTRY_SLEEP = 24
fld public final static byte METHOD_ENTRY_UNSTAMPED = 16
fld public final static byte METHOD_ENTRY_WAIT = 20
fld public final static byte METHOD_EXIT = 7
fld public final static byte METHOD_EXIT_COMPACT_BYTE_MASK = -64
fld public final static byte METHOD_EXIT_MONITOR = 23
fld public final static byte METHOD_EXIT_PARK = 27
fld public final static byte METHOD_EXIT_SLEEP = 25
fld public final static byte METHOD_EXIT_UNSTAMPED = 17
fld public final static byte METHOD_EXIT_WAIT = 21
fld public final static byte NEW_MONITOR = 28
fld public final static byte NEW_THREAD = 11
fld public final static byte OBJ_ALLOC_STACK_TRACE = 12
fld public final static byte OBJ_GC_HAPPENED = 15
fld public final static byte OBJ_LIVENESS_STACK_TRACE = 14
fld public final static byte RESET_COLLECTORS = 10
fld public final static byte ROOT_ENTRY = 1
fld public final static byte ROOT_EXIT = 2
fld public final static byte SERVLET_DO_METHOD = 30
fld public final static byte SET_FOLLOWING_EVENTS_THREAD = 13
fld public final static byte THREADS_RESUMED = 9
fld public final static byte THREADS_SUSPENDED = 8
fld public final static byte THREAD_DUMP_END = 32
fld public final static byte THREAD_DUMP_START = 31
fld public final static byte THREAD_INFO = 34
fld public final static byte THREAD_INFO_IDENTICAL = 33
fld public final static byte THREAD_STATUS_MONITOR = 3
fld public final static byte THREAD_STATUS_PARK = 5
fld public final static byte THREAD_STATUS_RUNNING = 1
fld public final static byte THREAD_STATUS_SLEEPING = 2
fld public final static byte THREAD_STATUS_UNKNOWN = -1
fld public final static byte THREAD_STATUS_WAIT = 4
fld public final static byte THREAD_STATUS_ZOMBIE = 0
fld public final static char COMPACT_EVENT_METHOD_ID_MASK = '\u3fff'
fld public final static char MAX_METHOD_ID_FOR_COMPACT_FORMAT = '\u3fff'
fld public final static char METHOD_ENTRY_COMPACT_MASK = '\u8000'
fld public final static char METHOD_EXIT_COMPACT_MASK = '\uc000'
fld public final static int AGENT_ID_ANY = -1
fld public final static int AGENT_STATE_CONNECTED = 3
fld public final static int AGENT_STATE_DIFFERENT_ID = 4
fld public final static int AGENT_STATE_NOT_RUNNING = 0
fld public final static int AGENT_STATE_OTHER_SESSION_IN_PROGRESS = 5
fld public final static int AGENT_STATE_READY_DIRECT = 2
fld public final static int AGENT_STATE_READY_DYNAMIC = 1
fld public final static int AGENT_VERSION_10_M10 = 2
fld public final static int AGENT_VERSION_10_M9 = 1
fld public final static int AGENT_VERSION_60_BETA1 = 8
fld public final static int AGENT_VERSION_60_M10 = 7
fld public final static int AGENT_VERSION_60_M5 = 3
fld public final static int AGENT_VERSION_60_M6 = 4
fld public final static int AGENT_VERSION_60_M7 = 5
fld public final static int AGENT_VERSION_60_M8 = 6
fld public final static int AGENT_VERSION_610_M2 = 11
fld public final static int AGENT_VERSION_67_BETA = 9
fld public final static int AGENT_VERSION_69 = 10
fld public final static int AGENT_VERSION_71 = 12
fld public final static int AGENT_VERSION_73 = 13
fld public final static int AGENT_VERSION_74 = 14
fld public final static int AGENT_VERSION_80 = 15
fld public final static int AGENT_VERSION_81 = 16
fld public final static int AGENT_VERSION_82 = 17
fld public final static int AGENT_VERSION_90 = 18
fld public final static int ARCH_32 = 32
fld public final static int ARCH_64 = 64
fld public final static int CPU_INSTR_FULL = 0
fld public final static int CPU_INSTR_SAMPLED = 1
fld public final static int CPU_SAMPLED = 2
fld public final static int CURRENT_AGENT_VERSION = 18
fld public final static int EVENT_BUFFER_SIZE_IN_BYTES = 1200000
fld public final static int FILTER_CONTAINS = 20
fld public final static int FILTER_ENDS_WITH = 30
fld public final static int FILTER_EQUALS = 40
fld public final static int FILTER_NONE = 0
fld public final static int FILTER_NOT_CONTAINS = 25
fld public final static int FILTER_REGEXP = 50
fld public final static int FILTER_STARTS_WITH = 10
fld public final static int INJ_CODE_REGION = 8
fld public final static int INJ_MAXNUMBER = 13
fld public final static int INJ_OBJECT_ALLOCATIONS = 9
fld public final static int INJ_OBJECT_LIVENESS = 10
fld public final static int INJ_RECURSIVE_MARKER_METHOD = 2
fld public final static int INJ_RECURSIVE_NORMAL_METHOD = 0
fld public final static int INJ_RECURSIVE_ROOT_METHOD = 1
fld public final static int INJ_RECURSIVE_SAMPLED_MARKER_METHOD = 5
fld public final static int INJ_RECURSIVE_SAMPLED_NORMAL_METHOD = 3
fld public final static int INJ_RECURSIVE_SAMPLED_ROOT_METHOD = 4
fld public final static int INJ_REFLECT_METHOD_INVOKE = 6
fld public final static int INJ_SERVLET_DO_METHOD = 7
fld public final static int INJ_STACKMAP = 11
fld public final static int INJ_THROWABLE = 12
fld public final static int INSTRSCHEME_EAGER = 2
fld public final static int INSTRSCHEME_LAZY = 1
fld public final static int INSTRSCHEME_TOTAL = 3
fld public final static int INSTR_CODE_REGION = 1
fld public final static int INSTR_MAXNUMBER = 7
fld public final static int INSTR_MEMORY_BASE = 5
fld public final static int INSTR_NONE = 0
fld public final static int INSTR_NONE_MEMORY_SAMPLING = 7
fld public final static int INSTR_NONE_SAMPLING = 2
fld public final static int INSTR_OBJECT_ALLOCATIONS = 5
fld public final static int INSTR_OBJECT_LIVENESS = 6
fld public final static int INSTR_RECURSIVE_FULL = 3
fld public final static int INSTR_RECURSIVE_SAMPLED = 4
fld public final static int JDK_110_BEYOND = 8
fld public final static int JDK_15 = 2
fld public final static int JDK_16 = 3
fld public final static int JDK_17 = 4
fld public final static int JDK_18 = 6
fld public final static int JDK_19 = 7
fld public final static int JDK_CVM = 5
fld public final static int JDK_UNSUPPORTED = -1
fld public final static int MODE_THREADS_EXACT = 2
fld public final static int MODE_THREADS_NONE = 0
fld public final static int MODE_THREADS_SAMPLING = 1
fld public final static int SERVER_INITIALIZING = 1
fld public final static int SERVER_INSTRUMENTING = 3
fld public final static int SERVER_PREPARING = 2
fld public final static int SERVER_PROGRESS_INDETERMINATE = -1
fld public final static int SERVER_PROGRESS_WORKUNITS = 100
fld public final static int SERVER_RUNNING = 0
fld public final static int SORTING_COLUMN_DEFAULT = -1
fld public final static java.awt.Color THREAD_STATUS_MONITOR_COLOR
fld public final static java.awt.Color THREAD_STATUS_PARK_COLOR
fld public final static java.awt.Color THREAD_STATUS_RUNNING_COLOR
fld public final static java.awt.Color THREAD_STATUS_SLEEPING_COLOR
fld public final static java.awt.Color THREAD_STATUS_UNKNOWN_COLOR
fld public final static java.awt.Color THREAD_STATUS_WAIT_COLOR
fld public final static java.awt.Color THREAD_STATUS_ZOMBIE_COLOR
fld public final static java.lang.String CALIBRATION_PSEUDO_CLASS_NAME = "____Profiler+Calibration+Run____"
fld public final static java.lang.String ENGINE_WARNING = "*** Profiler engine warning: "
fld public final static java.lang.String INVOKE_METHOD_NAME = "invoke"
fld public final static java.lang.String INVOKE_METHOD_SIGNATURE = "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;"
fld public final static java.lang.String JAVA_LANG_REFLECT_METHOD_DOTTED_CLASS_NAME = "java.lang.reflect.Method"
fld public final static java.lang.String JAVA_LANG_REFLECT_METHOD_SLASHED_CLASS_NAME = "java/lang/reflect/Method"
fld public final static java.lang.String JDK_110_BEYOND_STRING = "jdk110"
fld public final static java.lang.String JDK_15_STRING = "jdk15"
fld public final static java.lang.String JDK_16_STRING = "jdk16"
fld public final static java.lang.String JDK_17_STRING = "jdk17"
fld public final static java.lang.String JDK_18_STRING = "jdk18"
fld public final static java.lang.String JDK_19_STRING = "jdk19"
fld public final static java.lang.String JDK_CVM_STRING = "cvm"
fld public final static java.lang.String JDK_UNSUPPORTED_STRING = "UNSUPPORTED_JDK"
fld public final static java.lang.String NO_CLASS_NAME = "*NO_CLASS_NAME*"
fld public final static java.lang.String NO_METHOD_NAME = "*NO_METHOD_NAME*"
fld public final static java.lang.String NO_METHOD_SIGNATURE = "*NO_METHOD_SIGNATURE*"
fld public final static java.lang.String OBJECT_SLASHED_CLASS_NAME = "java/lang/Object"
fld public final static java.lang.String PLEASE_REPORT_PROBLEM = "*** Please report this problem to feedback@profiler.netbeans.org"
fld public final static java.lang.String PROFILER_DOTTED_CLASS_PREFIX = "org.netbeans.lib.profiler."
fld public final static java.lang.String PROFILER_SEPARATE_EXEC_THREAD_NAME = "*** JFluid Separate Command Execution Thread"
fld public final static java.lang.String PROFILER_SERVER_SLASHED_CLASS_PREFIX = "org/netbeans/lib/profiler/server/"
fld public final static java.lang.String PROFILER_SERVER_THREAD_NAME = "*** Profiler Agent Communication Thread"
fld public final static java.lang.String PROFILER_SPECIAL_EXEC_THREAD_NAME = "*** Profiler Agent Special Execution Thread"
fld public final static java.lang.String THREAD_STATUS_MONITOR_STRING
fld public final static java.lang.String THREAD_STATUS_PARK_STRING
fld public final static java.lang.String THREAD_STATUS_RUNNING_STRING
fld public final static java.lang.String THREAD_STATUS_SLEEPING_STRING
fld public final static java.lang.String THREAD_STATUS_UNKNOWN_STRING
fld public final static java.lang.String THREAD_STATUS_WAIT_STRING
fld public final static java.lang.String THREAD_STATUS_ZOMBIE_STRING

CLSS public abstract interface org.netbeans.lib.profiler.results.CCTProvider
innr public abstract interface static Listener
meth public abstract void addListener(org.netbeans.lib.profiler.results.CCTProvider$Listener)
meth public abstract void removeAllListeners()
meth public abstract void removeListener(org.netbeans.lib.profiler.results.CCTProvider$Listener)

CLSS public abstract interface static org.netbeans.lib.profiler.results.CCTProvider$Listener
 outer org.netbeans.lib.profiler.results.CCTProvider
meth public abstract void cctEstablished(org.netbeans.lib.profiler.results.RuntimeCCTNode,boolean)
meth public abstract void cctReset()

CLSS public abstract interface org.netbeans.lib.profiler.results.cpu.CPUCCTProvider
innr public abstract interface static Listener
intf org.netbeans.lib.profiler.results.CCTProvider
meth public abstract org.netbeans.lib.profiler.results.cpu.CPUCCTContainer[] createPresentationCCTs(org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot)

CLSS public abstract interface static org.netbeans.lib.profiler.results.cpu.CPUCCTProvider$Listener
 outer org.netbeans.lib.profiler.results.cpu.CPUCCTProvider
intf org.netbeans.lib.profiler.results.CCTProvider$Listener

CLSS public abstract interface org.netbeans.lib.profiler.results.cpu.FlatProfileProvider
meth public abstract org.netbeans.lib.profiler.results.cpu.FlatProfileContainer createFlatProfile()

CLSS public abstract interface org.netbeans.lib.profiler.results.jdbc.JdbcCCTProvider
fld public final static int SQL_CALLABLE_STATEMENT = 2
fld public final static int SQL_COMMAND_ALTER = 0
fld public final static int SQL_COMMAND_BATCH = -2
fld public final static int SQL_COMMAND_CREATE = 1
fld public final static int SQL_COMMAND_DELETE = 2
fld public final static int SQL_COMMAND_DESCRIBE = 3
fld public final static int SQL_COMMAND_INSERT = 4
fld public final static int SQL_COMMAND_OTHER = -1
fld public final static int SQL_COMMAND_SELECT = 5
fld public final static int SQL_COMMAND_SET = 6
fld public final static int SQL_COMMAND_UPDATE = 7
fld public final static int SQL_PREPARED_STATEMENT = 1
fld public final static int SQL_STATEMENT = 0
fld public final static int SQL_STATEMENT_UNKNOWN = -1
fld public final static java.lang.String CALLABLE_STATEMENT_INTERFACE
fld public final static java.lang.String CONNECTION_INTERFACE
fld public final static java.lang.String DRIVER_INTERFACE
fld public final static java.lang.String PREPARED_STATEMENT_INTERFACE
fld public final static java.lang.String STATEMENT_INTERFACE
innr public abstract interface static Listener
intf org.netbeans.lib.profiler.results.CCTProvider
intf org.netbeans.lib.profiler.results.cpu.FlatProfileProvider
meth public abstract int getCommandType(int)
meth public abstract int getSQLCommand(int)
meth public abstract java.lang.String[] getTables(int)
meth public abstract org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode[] getStacksForSelects()
meth public abstract void beginTrans(boolean)
meth public abstract void endTrans()
meth public abstract void updateInternals()

CLSS public abstract interface static org.netbeans.lib.profiler.results.jdbc.JdbcCCTProvider$Listener
 outer org.netbeans.lib.profiler.results.jdbc.JdbcCCTProvider
intf org.netbeans.lib.profiler.results.CCTProvider$Listener

CLSS public abstract interface org.netbeans.lib.profiler.results.memory.MemoryCCTProvider
innr public abstract interface static Listener
innr public static ObjectNumbersContainer
intf org.netbeans.lib.profiler.results.CCTProvider
meth public abstract boolean classMarkedUnprofiled(int)
meth public abstract int getCurrentEpoch()
meth public abstract int getNProfiledClasses()
meth public abstract long[] getAllocObjectNumbers()
meth public abstract long[] getObjectsSizePerClass()
meth public abstract org.netbeans.lib.profiler.results.memory.MemoryCCTProvider$ObjectNumbersContainer getLivenessObjectNumbers()
meth public abstract org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode createPresentationCCT(int,boolean) throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public abstract org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode[] getStacksForClasses()
meth public abstract void beginTrans(boolean)
meth public abstract void endTrans()
meth public abstract void markClassUnprofiled(int)
meth public abstract void updateInternals()

CLSS public abstract interface static org.netbeans.lib.profiler.results.memory.MemoryCCTProvider$Listener
 outer org.netbeans.lib.profiler.results.memory.MemoryCCTProvider
intf org.netbeans.lib.profiler.results.CCTProvider$Listener

CLSS public org.netbeans.lib.profiler.ui.AppearanceController
cons public init()
meth public boolean isAddToRootsVisible()
meth public int[] invisibleLivenessResultsColumns()
meth public static org.netbeans.lib.profiler.ui.AppearanceController getDefault()
meth public void customizeLiveFlatProfilePanel(javax.swing.JPanel)
meth public void customizeProfilerTableContainer(javax.swing.JPanel)
meth public void customizeThreadPanel(javax.swing.JPanel)
supr java.lang.Object
hfds DEFAULT

CLSS public final org.netbeans.lib.profiler.ui.Formatters
cons public init()
meth public static java.text.Format bytesFormat()
meth public static java.text.Format millisecondsFormat()
meth public static java.text.Format numberFormat()
meth public static java.text.Format percentFormat()
supr java.lang.Object
hfds BUNDLE,BYTES_FORMAT,MILLISECONDS_FORMAT,NUMBER_FORMAT,PERCENT_FORMAT

CLSS public abstract interface org.netbeans.lib.profiler.ui.LiveResultsPanel
meth public abstract boolean fitsVisibleArea()
meth public abstract boolean getSortingOrder()
meth public abstract boolean hasView()
meth public abstract boolean supports(int)
meth public abstract int getSortingColumn()
meth public abstract java.awt.image.BufferedImage getViewImage(boolean)
meth public abstract java.lang.String getViewName()
meth public abstract void handleRemove()
meth public abstract void handleShutdown()
meth public abstract void reset()
meth public abstract void updateLiveResults()

CLSS public abstract interface org.netbeans.lib.profiler.ui.LiveResultsWindowContributor
innr public abstract static Adapter
meth public abstract void addToCpuResults(org.netbeans.lib.profiler.ui.cpu.LiveFlatProfilePanel,org.netbeans.lib.profiler.ui.components.ProfilerToolbar,org.netbeans.lib.profiler.ProfilerClient,org.openide.util.Lookup$Provider)
meth public abstract void addToMemoryResults(org.netbeans.lib.profiler.ui.cpu.LiveFlatProfilePanel,org.netbeans.lib.profiler.ui.components.ProfilerToolbar,org.netbeans.lib.profiler.ProfilerClient,org.openide.util.Lookup$Provider)
meth public abstract void hide()
meth public abstract void refresh()
meth public abstract void reset()
meth public abstract void show()

CLSS public abstract static org.netbeans.lib.profiler.ui.LiveResultsWindowContributor$Adapter
 outer org.netbeans.lib.profiler.ui.LiveResultsWindowContributor
cons public init()
intf org.netbeans.lib.profiler.ui.LiveResultsWindowContributor
meth public void addToCpuResults(org.netbeans.lib.profiler.ui.cpu.LiveFlatProfilePanel,org.netbeans.lib.profiler.ui.components.ProfilerToolbar,org.netbeans.lib.profiler.ProfilerClient,org.openide.util.Lookup$Provider)
meth public void addToMemoryResults(org.netbeans.lib.profiler.ui.cpu.LiveFlatProfilePanel,org.netbeans.lib.profiler.ui.components.ProfilerToolbar,org.netbeans.lib.profiler.ProfilerClient,org.openide.util.Lookup$Provider)
meth public void hide()
meth public void refresh()
meth public void reset()
meth public void show()
supr java.lang.Object

CLSS public abstract org.netbeans.lib.profiler.ui.ResultsPanel
cons public init()
fld protected java.text.NumberFormat intFormat
fld protected java.text.NumberFormat percentFormat
meth protected abstract void initColumnSelectorItems()
meth protected javax.swing.JButton createHeaderPopupCornerButton(javax.swing.JPopupMenu)
meth protected javax.swing.JScrollPane createScrollPane()
meth protected javax.swing.JScrollPane createScrollPaneVerticalScrollBarAlways()
meth public abstract void prepareResults()
supr javax.swing.JPanel
hfds CORNER_BUTTON_TOOLTIP,internalCornerButtonClick,messages

CLSS public org.netbeans.lib.profiler.ui.ResultsView
cons public init()
meth public final boolean isViewEnabled(java.awt.Component)
meth public final int getSelectedViewIndex()
meth public final int getViewsCount()
meth public final java.awt.Component getSelectedView()
meth public final java.lang.String getViewName(java.awt.Component)
meth public final void addChangeListener(javax.swing.event.ChangeListener)
meth public final void addView(java.lang.String,javax.swing.Icon,java.lang.String,java.awt.Component,java.awt.Component)
meth public final void fireViewOrIndexChanged()
meth public final void removeChangeListener(javax.swing.event.ChangeListener)
meth public final void removeView(java.awt.Component)
meth public final void removeViews()
meth public final void selectNextView()
meth public final void selectPreviousView()
meth public final void selectView(int)
meth public final void selectView(java.awt.Component)
meth public final void setMainToolbar(java.awt.Component)
meth public final void setViewEnabled(java.awt.Component,boolean)
meth public final void setViewName(java.awt.Component,java.lang.String)
supr javax.swing.JPanel
hfds currentToolbar,firstDescription,firstIcon,firstName,firstView,listeners,mainToolbar,tabs,toolbars

CLSS public abstract interface org.netbeans.lib.profiler.ui.StringDecorator
fld public final static org.netbeans.lib.profiler.ui.StringDecorator DEFAULT
meth public abstract java.lang.String decorate(java.lang.String)

CLSS public abstract org.netbeans.lib.profiler.ui.SwingWorker
cons public init()
cons public init(boolean)
cons public init(boolean,java.util.concurrent.Semaphore)
cons public init(java.util.concurrent.Semaphore)
meth protected abstract void doInBackground()
meth protected final boolean isCancelled()
meth protected int getWarmup()
meth protected void cancelled()
meth protected void done()
meth protected void nonResponding()
meth protected void postRunnable(java.lang.Runnable)
meth public final void cancel()
meth public void execute()
supr java.lang.Object
hfds cancelFlag,primed,taskService,throughputSemaphore,useEQ,warmupLock,warmupService,warmupTimer,workerRunning

CLSS public abstract interface org.netbeans.lib.profiler.ui.UIConstants
fld public final static boolean SHOW_TABLE_HORIZONTAL_GRID = false
fld public final static boolean SHOW_TABLE_VERTICAL_GRID = true
fld public final static int TABLE_ROW_MARGIN = 0
fld public final static java.awt.Color TABLE_SELECTION_BACKGROUND_COLOR
fld public final static java.awt.Color TABLE_SELECTION_FOREGROUND_COLOR
fld public final static java.awt.Color TABLE_VERTICAL_GRID_COLOR
fld public final static java.lang.String PROFILER_PANELS_BACKGROUND = "ProfilerPanels.background"

CLSS public final org.netbeans.lib.profiler.ui.UIUtils
cons public init()
fld public final static float ALTERNATE_ROW_DARKER_FACTOR = 0.96
fld public final static java.lang.String PROP_AUTO_EXPANDING = "auto_expanding"
fld public final static java.lang.String PROP_EXPANSION_TRANSACTION = "expansion_transaction"
fld public static java.awt.Dimension DIMENSION_SMALLEST
meth public static boolean hasOnlyLeafs(javax.swing.JTree,java.lang.Object)
meth public static boolean isAquaLookAndFeel()
meth public static boolean isDarkResultsBackground()
meth public static boolean isGTKLookAndFeel()
meth public static boolean isMetalLookAndFeel()
meth public static boolean isNimbus()
meth public static boolean isNimbusGTKTheme()
meth public static boolean isNimbusLookAndFeel()
meth public static boolean isOracleLookAndFeel()
meth public static boolean isWindowsClassicLookAndFeel()
meth public static boolean isWindowsLookAndFeel()
meth public static boolean isWindowsModernLookAndFeel()
meth public static boolean isWindowsXPLookAndFeel()
meth public static float[] copyArray(float[])
meth public static int getDefaultRowHeight()
meth public static int getNextSubTabIndex(javax.swing.JTabbedPane,int)
meth public static int getPreviousSubTabIndex(javax.swing.JTabbedPane,int)
meth public static int[] copyArray(int[])
meth public static java.awt.Color getDarker(java.awt.Color)
meth public static java.awt.Color getDarkerLine(java.awt.Color,float)
meth public static java.awt.Color getDisabledForeground(java.awt.Color)
meth public static java.awt.Color getDisabledLineColor()
meth public static java.awt.Color getProfilerResultsBackground()
meth public static java.awt.Color getSafeColor(int,int,int)
meth public static java.awt.Color getUnfocusedSelectionBackground()
meth public static java.awt.Color getUnfocusedSelectionForeground()
meth public static java.awt.Window getParentWindow(java.awt.Component)
meth public static java.awt.image.BufferedImage createScreenshot(java.awt.Component)
meth public static javax.swing.JPanel createFillerPanel()
meth public static javax.swing.JSeparator createHorizontalLine(java.awt.Color)
meth public static javax.swing.JSeparator createHorizontalSeparator()
meth public static long[] copyArray(long[])
meth public static void addBorder(javax.swing.JComponent,javax.swing.border.Border)
meth public static void autoExpand(javax.swing.JTree,javax.swing.tree.TreePath,int,int,boolean)
meth public static void autoExpandRoot(javax.swing.JTree)
meth public static void autoExpandRoot(javax.swing.JTree,int)
meth public static void decorateProfilerPanel(javax.swing.JPanel)
meth public static void ensureMinimumSize(java.awt.Component)
meth public static void fixButtonUI(javax.swing.AbstractButton)
meth public static void makeTreeAutoExpandable(javax.swing.JTree)
meth public static void makeTreeAutoExpandable(javax.swing.JTree,boolean)
meth public static void makeTreeAutoExpandable(javax.swing.JTree,int)
meth public static void makeTreeAutoExpandable(javax.swing.JTree,int,boolean)
meth public static void runInEventDispatchThread(java.lang.Runnable)
meth public static void runInEventDispatchThreadAndWait(java.lang.Runnable)
supr java.lang.Object
hfds DARKER_CACHE,LOGGER,MAX_TREE_AUTOEXPAND_LINES,darkResultsBackground,disabledLineColor,profilerResultsBackground,toolTipValuesInitialized,unfocusedSelBg,unfocusedSelFg

CLSS public abstract org.netbeans.lib.profiler.ui.charts.AbstractBarChartModel
cons public init()
intf org.netbeans.lib.profiler.ui.charts.BarChartModel
meth protected void fireChartDataChanged()
meth public abstract int[] getYValues()
meth public abstract java.lang.String getXAxisDesc()
meth public abstract java.lang.String getYAxisDesc()
meth public abstract java.lang.String[] getXLabels()
meth public void addChartModelListener(org.netbeans.lib.profiler.ui.charts.ChartModelListener)
meth public void removeChartModelListener(org.netbeans.lib.profiler.ui.charts.ChartModelListener)
supr java.lang.Object
hfds listeners

CLSS public abstract org.netbeans.lib.profiler.ui.charts.AbstractPieChartModel
cons public init()
intf org.netbeans.lib.profiler.ui.charts.PieChartModel
meth protected void fireChartDataChanged()
meth public abstract boolean hasData()
meth public abstract double getItemValue(int)
meth public abstract double getItemValueRel(int)
meth public abstract int getItemCount()
meth public abstract java.awt.Color getItemColor(int)
meth public abstract java.lang.String getItemName(int)
meth public void addChartModelListener(org.netbeans.lib.profiler.ui.charts.ChartModelListener)
meth public void removeChartModelListener(org.netbeans.lib.profiler.ui.charts.ChartModelListener)
supr java.lang.Object
hfds listeners

CLSS public org.netbeans.lib.profiler.ui.charts.BarChart
cons public init()
intf java.awt.event.ComponentListener
intf javax.accessibility.Accessible
intf javax.swing.event.AncestorListener
intf org.netbeans.lib.profiler.ui.charts.ChartModelListener
meth protected void drawBar(java.awt.Graphics2D,int,int,int,int)
meth protected void drawChart(java.awt.Graphics2D)
meth protected void drawHorizontalAxis(java.awt.Graphics2D,java.util.List,java.lang.String[])
meth protected void drawHorizontalAxisLegendItem(java.awt.Graphics2D,int,java.lang.String)
meth protected void drawVerticalAxis(java.awt.Graphics2D,int,int[])
meth protected void drawVerticalAxisLegendItem(java.awt.Graphics2D,int,java.lang.String)
meth protected void updateOffScreenImageSize()
meth public boolean getDraw3D()
meth public int getLeftOffset()
meth public int getRightOffset()
meth public int getTopOffset()
meth public int getXSpacing()
meth public java.awt.Paint getFillPaint()
meth public java.awt.Paint getOutlinePaint()
meth public java.awt.Stroke getOutlineStroke()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public org.netbeans.lib.profiler.ui.charts.BarChartModel getModel()
meth public static void main(java.lang.String[])
meth public void ancestorAdded(javax.swing.event.AncestorEvent)
meth public void ancestorMoved(javax.swing.event.AncestorEvent)
meth public void ancestorRemoved(javax.swing.event.AncestorEvent)
meth public void chartDataChanged()
meth public void componentHidden(java.awt.event.ComponentEvent)
meth public void componentMoved(java.awt.event.ComponentEvent)
meth public void componentResized(java.awt.event.ComponentEvent)
meth public void componentShown(java.awt.event.ComponentEvent)
meth public void paintComponent(java.awt.Graphics)
meth public void setAccessibleContext(javax.accessibility.AccessibleContext)
meth public void setDraw3D(boolean)
meth public void setFillPaint(java.awt.Paint)
meth public void setFont(java.awt.Font)
meth public void setLeftOffset(int)
meth public void setModel(org.netbeans.lib.profiler.ui.charts.BarChartModel)
meth public void setOutlinePaint(java.awt.Paint)
meth public void setOutlineStroke(java.awt.Stroke)
meth public void setRightOffset(int)
meth public void setTopOffset(int)
meth public void setXSpacing(int)
supr javax.swing.JComponent
hfds accessibleContext,axisMeshPaint,axisPaint,axisStroke,draw3D,drawHeight,drawWidth,fillPaint,horizAxisHeight,horizAxisXes,horizLegendWidth,insets,leftOffset,maxHeight,model,modelIncorrect,offScreenGraphics,offScreenImage,offScreenImageInvalid,offScreenImageSizeInvalid,outlinePaint,outlineStroke,rightOffset,topOffset,vertAxisWidth,vertLegendHeight,xSpacing

CLSS public abstract interface org.netbeans.lib.profiler.ui.charts.BarChartModel
meth public abstract int[] getYValues()
meth public abstract java.lang.String getXAxisDesc()
meth public abstract java.lang.String getYAxisDesc()
meth public abstract java.lang.String[] getXLabels()
meth public abstract void addChartModelListener(org.netbeans.lib.profiler.ui.charts.ChartModelListener)
meth public abstract void removeChartModelListener(org.netbeans.lib.profiler.ui.charts.ChartModelListener)

CLSS public abstract interface org.netbeans.lib.profiler.ui.charts.ChartActionListener
meth public abstract void chartDataChanged()
meth public abstract void chartFitToWindowChanged()
meth public abstract void chartPanned()
meth public abstract void chartTrackingEndChanged()
meth public abstract void chartZoomed()

CLSS public abstract interface org.netbeans.lib.profiler.ui.charts.ChartModelListener
meth public abstract void chartDataChanged()

CLSS public org.netbeans.lib.profiler.ui.charts.DateTimeAxisUtils
cons public init()
fld public final static int MIN_TIMEMARK_STEP = 100
fld public final static java.awt.Color BASE_TIMELINE_COLOR
fld public final static java.awt.Color MAIN_TIMELINE_COLOR
fld public final static java.awt.Color TICK_TIMELINE_COLOR
meth public static double getMaximumScale(long)
meth public static java.lang.String getDaysValue(long,boolean)
meth public static java.lang.String getHoursValue(long,boolean)
meth public static java.lang.String getMillisValue(long,boolean)
meth public static java.lang.String getMillisValueFull(long)
meth public static java.lang.String getMinutesValue(long,boolean)
meth public static java.lang.String getSecondsValue(long,boolean)
meth public static java.lang.String getTimeMarkMillisString(long,long)
meth public static java.lang.String getTimeMarkNoMillisString(long,long,boolean)
meth public static java.lang.String getTimeMarkString(long,long,boolean)
meth public static long getOptimalUnits(double)
supr java.lang.Object
hfds DAYS_FORMAT,HOURS_EXT_FORMAT,HOURS_FORMAT,MILLIS_EXT_FORMAT,MILLIS_FORMAT,MILLIS_FULL_FORMAT,MILLIS_ONLY_FORMAT,MINUTES_EXT_FORMAT,MINUTES_FORMAT,SECONDS_EXT_FORMAT,SECONDS_FORMAT,TIME_FORMAT_DAYS,TIME_FORMAT_HOURS,TIME_FORMAT_MILLIS,TIME_FORMAT_MINUTES,TIME_FORMAT_SECONDS,TIME_FORMAT_UNKNOWN,daysDateFormat,hoursDateFormat,hoursDateFormatD,messages,millisDateFormat,millisDateFormatD,millisDateFormatF,minutesDateFormat,minutesDateFormatD,onlyMillisDateFormat,secondsDateFormat,secondsDateFormatD,timeUnitsFormat,timeUnitsGrid,timeUnitsToIndex

CLSS public org.netbeans.lib.profiler.ui.charts.DecimalAxisUtils
cons public init()
meth public static long getOptimalUnits(double,int)
supr java.lang.Object
hfds timeUnitsGrid

CLSS public org.netbeans.lib.profiler.ui.charts.DynamicPieChartModel
cons public init()
fld protected boolean hasData
fld protected double[] itemValues
fld protected double[] itemValuesRel
fld protected int itemCount
fld protected java.awt.Color[] itemColors
fld protected java.lang.String[] itemNames
meth public boolean hasData()
meth public boolean isSelectable(int)
meth public double getItemValue(int)
meth public double getItemValueRel(int)
meth public int getItemCount()
meth public java.awt.Color getItemColor(int)
meth public java.lang.String getItemName(int)
meth public void setItemValues(double[])
meth public void setupModel(java.lang.String[],java.awt.Color[])
supr org.netbeans.lib.profiler.ui.charts.AbstractPieChartModel

CLSS public org.netbeans.lib.profiler.ui.charts.PieChart
cons public init()
intf java.awt.event.ComponentListener
intf javax.accessibility.Accessible
intf org.netbeans.lib.profiler.ui.charts.ChartModelListener
meth protected java.awt.Color getDisabledColor(java.awt.Color)
meth protected java.awt.geom.Area drawChartPartSide(java.awt.Graphics2D,java.awt.geom.Area,double,double,double,double,java.awt.Color)
meth protected void drawChart(java.awt.Graphics2D)
meth protected void updateOffScreenImageSize()
meth public int getChartHeight()
meth public int getItemIndexAt(int,int)
meth public int getStartAngle()
meth public int[] getSelectedItems()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public org.netbeans.lib.profiler.ui.charts.PieChartModel getModel()
meth public static void main(java.lang.String[])
meth public void addSelectedItem(int)
meth public void chartDataChanged()
meth public void componentHidden(java.awt.event.ComponentEvent)
meth public void componentMoved(java.awt.event.ComponentEvent)
meth public void componentResized(java.awt.event.ComponentEvent)
meth public void componentShown(java.awt.event.ComponentEvent)
meth public void deselectAllItems()
meth public void paintComponent(java.awt.Graphics)
meth public void removeSelectedItem(int)
meth public void resetFocusedItem()
meth public void selectAllItems()
meth public void setAccessibleContext(javax.accessibility.AccessibleContext)
meth public void setChartHeight(int)
meth public void setFocusedItem(int)
meth public void setModel(org.netbeans.lib.profiler.ui.charts.PieChartModel)
meth public void setSelectedItem(int)
meth public void setStartAngle(int)
meth public void toggleItemSelection(int)
supr javax.swing.JComponent
hfds accessibleContext,arcs,bottoms,chartHeight,draw3D,drawHeight,drawWidth,evenSelectionSegmentsColor,evenSelectionSegmentsStroke,focusedItem,initialAngle,insets,model,oddSelectionSegmentColor,oddSelectionSegmentStroke,offScreenGraphics,offScreenImage,offScreenImageInvalid,offScreenImageSizeInvalid,pieArea,pieCenterY,pieHeight,selectedItems

CLSS public abstract interface org.netbeans.lib.profiler.ui.charts.PieChartModel
meth public abstract boolean hasData()
meth public abstract boolean isSelectable(int)
meth public abstract double getItemValue(int)
meth public abstract double getItemValueRel(int)
meth public abstract int getItemCount()
meth public abstract java.awt.Color getItemColor(int)
meth public abstract java.lang.String getItemName(int)
meth public abstract void addChartModelListener(org.netbeans.lib.profiler.ui.charts.ChartModelListener)
meth public abstract void removeChartModelListener(org.netbeans.lib.profiler.ui.charts.ChartModelListener)

CLSS public abstract org.netbeans.lib.profiler.ui.charts.xy.ProfilerGCXYItem
cons public init(java.lang.String)
meth public abstract long[] getGCEnds(int)
meth public abstract long[] getGCStarts(int)
meth public long getYValue(int)
supr org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYItem

CLSS public org.netbeans.lib.profiler.ui.charts.xy.ProfilerGCXYItemPainter
meth protected void paint(org.netbeans.lib.profiler.charts.xy.XYItem,java.util.List<org.netbeans.lib.profiler.charts.ItemSelection>,java.util.List<org.netbeans.lib.profiler.charts.ItemSelection>,java.awt.Graphics2D,java.awt.Rectangle,org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYChartContext)
meth public double getItemValue(double,org.netbeans.lib.profiler.charts.xy.XYItem,org.netbeans.lib.profiler.charts.ChartContext)
meth public double getItemValueScale(org.netbeans.lib.profiler.charts.xy.XYItem,org.netbeans.lib.profiler.charts.ChartContext)
meth public double getItemView(double,org.netbeans.lib.profiler.charts.xy.XYItem,org.netbeans.lib.profiler.charts.ChartContext)
meth public org.netbeans.lib.profiler.charts.swing.LongRect getDirtyBounds(org.netbeans.lib.profiler.charts.ChartItemChange,org.netbeans.lib.profiler.charts.ChartContext)
meth public org.netbeans.lib.profiler.charts.swing.LongRect getItemBounds(org.netbeans.lib.profiler.charts.ChartItem,org.netbeans.lib.profiler.charts.ChartContext)
meth public static org.netbeans.lib.profiler.ui.charts.xy.ProfilerGCXYItemPainter painter(java.awt.Color)
supr org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYItemPainter

CLSS public org.netbeans.lib.profiler.ui.charts.xy.ProfilerXYChart
cons public init(org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYItemsModel,org.netbeans.lib.profiler.charts.PaintersModel)
meth public javax.swing.Action toggleViewAction()
meth public javax.swing.Action zoomInAction()
meth public javax.swing.Action zoomOutAction()
supr org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYChart
hfds FIXED_SCALE_ICON,FIXED_SCALE_STRING,SCALE_TO_FIT_ICON,SCALE_TO_FIT_STRING,ZOOM_IN_ICON,ZOOM_IN_STRING,ZOOM_OUT_ICON,ZOOM_OUT_STRING,messages,toggleViewAction,zoomInAction,zoomOutAction
hcls ToggleViewAction,VisibleBoundsListener,ZoomInAction,ZoomOutAction

CLSS public org.netbeans.lib.profiler.ui.charts.xy.ProfilerXYItemPainter
cons public init(float,java.awt.Color,java.awt.Color,int,int)
meth public boolean supportsHovering(org.netbeans.lib.profiler.charts.ChartItem)
meth public org.netbeans.lib.profiler.charts.ItemSelection getClosestSelection(org.netbeans.lib.profiler.charts.ChartItem,int,int,org.netbeans.lib.profiler.charts.ChartContext)
meth public org.netbeans.lib.profiler.charts.swing.LongRect getSelectionBounds(org.netbeans.lib.profiler.charts.ItemSelection,org.netbeans.lib.profiler.charts.ChartContext)
meth public static org.netbeans.lib.profiler.ui.charts.xy.ProfilerXYItemPainter absolutePainter(float,java.awt.Color,java.awt.Color)
meth public static org.netbeans.lib.profiler.ui.charts.xy.ProfilerXYItemPainter relativePainter(float,java.awt.Color,java.awt.Color,int)
supr org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYItemPainter

CLSS public org.netbeans.lib.profiler.ui.charts.xy.ProfilerXYSelectionOverlay
cons public init()
meth public final void registerChart(org.netbeans.lib.profiler.charts.ChartComponent)
meth public final void unregisterChart(org.netbeans.lib.profiler.charts.ChartComponent)
meth public void paint(java.awt.Graphics)
supr org.netbeans.lib.profiler.charts.ChartOverlay
hfds chart,configurationListener,evenPerfPaint,evenPerfStroke,markPaint,markStroke,oddPerfPaint,oddPerfStroke,selectedValues,selectionExtent,selectionListener
hcls ConfigurationListener,SelectionListener

CLSS public abstract interface org.netbeans.lib.profiler.ui.charts.xy.ProfilerXYTooltipModel
meth public abstract int getExtraRowsCount()
meth public abstract int getRowsCount()
meth public abstract java.awt.Color getExtraRowColor(int)
meth public abstract java.awt.Color getRowColor(int)
meth public abstract java.lang.String getExtraRowName(int)
meth public abstract java.lang.String getExtraRowUnits(int)
meth public abstract java.lang.String getExtraRowValue(int)
meth public abstract java.lang.String getRowName(int)
meth public abstract java.lang.String getRowUnits(int)
meth public abstract java.lang.String getRowValue(int,long)
meth public abstract java.lang.String getTimeValue(long)

CLSS public org.netbeans.lib.profiler.ui.charts.xy.ProfilerXYTooltipOverlay
cons public init(org.netbeans.lib.profiler.charts.ChartComponent,org.netbeans.lib.profiler.ui.charts.xy.ProfilerXYTooltipPainter)
intf java.awt.event.ActionListener
meth public final java.awt.Point getPosition()
meth public final void setPosition(java.awt.Point)
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void paint(java.awt.Graphics)
supr org.netbeans.lib.profiler.charts.ChartOverlay
hfds ANIMATION_STEPS,TOOLTIP_MARGIN,TOOLTIP_OFFSET,TOOLTIP_RESPONSE,currentStep,mousePosition,targetPosition,timer,tooltipPainter

CLSS public org.netbeans.lib.profiler.ui.charts.xy.ProfilerXYTooltipPainter
cons public init(org.netbeans.lib.profiler.ui.charts.xy.ProfilerXYTooltipModel)
meth protected void paintComponent(java.awt.Graphics)
meth public void update(java.util.List<org.netbeans.lib.profiler.charts.ItemSelection>)
supr javax.swing.JPanel
hfds BACKGROUND_COLOR,caption,extraValuePainters,initialized,model,valuePainters

CLSS public org.netbeans.lib.profiler.ui.components.AnimatedContainer
cons public init(java.awt.Color)
fld public final static int HEIGHT = 20
fld public final static int WIDTH = 10
meth public java.awt.Component add(java.awt.Component)
meth public void lockContentResizing(boolean)
meth public void setAnimatedDimension(int)
meth public void setAnimation()
meth public void setAnimation(java.awt.Dimension)
meth public void setAnimation(javax.swing.JComponent)
meth public void setAnimation(javax.swing.JComponent,javax.swing.JComponent)
meth public void setContent(javax.swing.JComponent)
meth public void setDefaultBorder()
meth public void setFinishState()
meth public void setFocusedBorder()
meth public void setSelectedBorder()
meth public void setState(int)
meth public void setTargetContent(javax.swing.JComponent)
meth public void setTransContent(javax.swing.JComponent)
supr javax.swing.JPanel
hfds BOTH,animatedDimension,border,content,layout,origHeight,origWidth,targetContent,targetHeight,targetWidth,transContent

CLSS public org.netbeans.lib.profiler.ui.components.AnimationLayout
cons public init()
intf java.awt.LayoutManager
meth public java.awt.Dimension minimumLayoutSize(java.awt.Container)
meth public java.awt.Dimension preferredLayoutSize(java.awt.Container)
meth public java.lang.String toString()
meth public void addLayoutComponent(java.lang.String,java.awt.Component)
meth public void layoutContainer(java.awt.Container)
meth public void removeLayoutComponent(java.awt.Component)
meth public void setLockedSize(java.awt.Dimension)
supr java.lang.Object
hfds lockedSize

CLSS public org.netbeans.lib.profiler.ui.components.Animator
cons public init(org.netbeans.lib.profiler.ui.components.AnimatedContainer,org.netbeans.lib.profiler.ui.components.AnimatedContainer)
intf java.awt.event.ActionListener
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void startAnimation()
supr java.lang.Object
hfds DURATION,STEPS,cont1,cont2,stepsCounter,timer

CLSS public abstract interface org.netbeans.lib.profiler.ui.components.CellTipAware
meth public abstract java.awt.Point getCellTipLocation()
meth public abstract javax.swing.JToolTip getCellTip()
meth public abstract void processMouseEvent(java.awt.event.MouseEvent)

CLSS public org.netbeans.lib.profiler.ui.components.CellTipManager
cons public init()
intf java.awt.event.MouseListener
intf java.awt.event.MouseMotionListener
intf java.awt.event.MouseWheelListener
meth public boolean isEnabled()
meth public static org.netbeans.lib.profiler.ui.components.CellTipManager sharedInstance()
meth public void hideCellTip()
meth public void mouseClicked(java.awt.event.MouseEvent)
meth public void mouseDragged(java.awt.event.MouseEvent)
meth public void mouseEntered(java.awt.event.MouseEvent)
meth public void mouseExited(java.awt.event.MouseEvent)
meth public void mouseMoved(java.awt.event.MouseEvent)
meth public void mousePressed(java.awt.event.MouseEvent)
meth public void mouseReleased(java.awt.event.MouseEvent)
meth public void mouseWheelMoved(java.awt.event.MouseWheelEvent)
meth public void registerComponent(javax.swing.JComponent)
meth public void setEnabled(boolean)
meth public void unregisterComponent(javax.swing.JComponent)
supr java.lang.Object
hfds cellTip,cellTipComponent,cellTipPopup,cellTipWindow,enabled,heavyweightPopupClosed,internalMousePressed,moveBeforeEnterListener,popupFrameRect,popupRect,sharedInstance,universalCellTipListener
hcls MoveBeforeEnterListener,UniversalCellTipListener

CLSS public org.netbeans.lib.profiler.ui.components.CloseButton
cons protected init()
innr public abstract static Provider
innr public static Impl
meth public static javax.swing.JButton create(java.lang.Runnable)
supr java.lang.Object

CLSS public static org.netbeans.lib.profiler.ui.components.CloseButton$Impl
 outer org.netbeans.lib.profiler.ui.components.CloseButton
cons public init(java.lang.Runnable)
supr javax.swing.JButton

CLSS public abstract static org.netbeans.lib.profiler.ui.components.CloseButton$Provider
 outer org.netbeans.lib.profiler.ui.components.CloseButton
cons public init()
meth public abstract javax.swing.JButton create(java.lang.Runnable)
supr java.lang.Object

CLSS public org.netbeans.lib.profiler.ui.components.ColorIcon
cons public init()
cons public init(java.awt.Color)
cons public init(java.awt.Color,int,int)
cons public init(java.awt.Color,java.awt.Color,int,int)
intf javax.swing.Icon
meth public int getIconHeight()
meth public int getIconWidth()
meth public java.awt.Color getBorderColor()
meth public java.awt.Color getColor()
meth public void paintIcon(java.awt.Component,java.awt.Graphics,int,int)
meth public void setBorderColor(java.awt.Color)
meth public void setColor(java.awt.Color)
meth public void setIconHeight(int)
meth public void setIconWidth(int)
supr java.lang.Object
hfds borderColor,color,height,width

CLSS public org.netbeans.lib.profiler.ui.components.ComponentMorpher
cons public init(javax.swing.JComponent,javax.swing.JComponent)
cons public init(javax.swing.JComponent,javax.swing.JComponent,int,int)
innr protected AccessibleComponentMorpher
intf java.awt.event.ComponentListener
intf javax.accessibility.Accessible
meth public boolean isExpanded()
meth public boolean isMorphing()
meth public int getMorphingDelay()
meth public int getMorphingSteps()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public void componentHidden(java.awt.event.ComponentEvent)
meth public void componentMoved(java.awt.event.ComponentEvent)
meth public void componentResized(java.awt.event.ComponentEvent)
meth public void componentShown(java.awt.event.ComponentEvent)
meth public void expand()
meth public void morph()
meth public void morphingStep()
meth public void refreshLayout()
meth public void reset()
meth public void setBorder(javax.swing.border.Border)
meth public void setMorphingDelay(int)
meth public void setMorphingSteps(int)
meth public void setupMorphing()
supr javax.swing.JComponent
hfds blenderPanel,component1,component2,currentComponent,endComponent,heightDelta,isMorphing,morphingDelay,morphingStep,morphingSteps,startComponent
hcls MorpherThread

CLSS protected org.netbeans.lib.profiler.ui.components.ComponentMorpher$AccessibleComponentMorpher
 outer org.netbeans.lib.profiler.ui.components.ComponentMorpher
cons protected init(org.netbeans.lib.profiler.ui.components.ComponentMorpher)
meth public javax.accessibility.AccessibleRole getAccessibleRole()
supr javax.swing.JComponent$AccessibleJComponent

CLSS public org.netbeans.lib.profiler.ui.components.ComponentMorpher2
cons public init(javax.swing.JComponent,javax.swing.JComponent)
cons public init(javax.swing.JComponent,javax.swing.JComponent,int,int)
meth public boolean isExpanded()
meth public boolean isMorphing()
meth public int getMorphingDelay()
meth public int getMorphingSteps()
meth public void morph()
meth public void morphingStep()
meth public void refresh()
meth public void setBorder(javax.swing.border.Border)
meth public void setMorphingDelay(int)
meth public void setMorphingSteps(int)
meth public void setupMorphing()
supr javax.swing.JComponent
hfds blenderPanel,component1,component2,currentComponent,endComponent,endComponentImage,heightDelta,isMorphing,morphingDelay,morphingStep,morphingSteps,startComponent,startComponentImage
hcls MorpherThread

CLSS public org.netbeans.lib.profiler.ui.components.CustomTaskButtonBorder
cons public init(java.awt.Color,java.awt.Color)
cons public init(java.awt.Color,java.awt.Color,int)
fld public final static int BORDER_STATE_DEFAULT = 10
fld public final static int BORDER_STATE_FOCUSED = 20
fld public final static int BORDER_STATE_SELECTED = 30
meth public boolean isBorderOpaque()
meth public int getBorderState()
meth public java.awt.Color getBackgroundColor()
meth public java.awt.Color getForegroundColor()
meth public java.awt.Insets getBorderInsets(java.awt.Component)
meth public static java.awt.Insets getBorderInsets()
meth public static org.netbeans.lib.profiler.ui.components.CustomTaskButtonBorder getDefaultInstance(java.awt.Color,java.awt.Color)
meth public static org.netbeans.lib.profiler.ui.components.CustomTaskButtonBorder getFocusedInstance(java.awt.Color,java.awt.Color)
meth public static org.netbeans.lib.profiler.ui.components.CustomTaskButtonBorder getSelectedInstance(java.awt.Color,java.awt.Color)
meth public void paintBorder(java.awt.Component,java.awt.Graphics,int,int,int,int)
meth public void setBackgroundColor(java.awt.Color)
meth public void setBorderState(int)
meth public void setDefault()
meth public void setFocused()
meth public void setForegroundColor(java.awt.Color)
meth public void setSelected()
supr javax.swing.border.AbstractBorder
hfds DEFAULT_BOTTOM_DARK_CLR,DEFAULT_BOTTOM_LIGHT_CLR,DEFAULT_TOP_DARK_CLR,DEFAULT_TOP_LIGHT_CLR,FOCUSED_BOTTOM_DARK_CLR,FOCUSED_BOTTOM_LIGHT_CLR,FOCUSED_TOP_DARK_CLR,FOCUSED_TOP_LIGHT_CLR,OUTLINE_CLR,SELECTED_BOTTOM_DARK_CLR,SELECTED_BOTTOM_LIGHT_CLR,SELECTED_TOP_DARK_CLR,SELECTED_TOP_LIGHT_CLR,backgroundColor,backgroundFade,borderState,startColor,stopColor

CLSS public org.netbeans.lib.profiler.ui.components.DiscreteProgress
cons public init()
meth public int getActiveUnits()
meth public java.awt.Dimension getMaximumSize()
meth public java.awt.Dimension getMinimumSize()
meth public java.awt.Dimension getPreferredSize()
meth public static void main(java.lang.String[])
meth public void paintComponent(java.awt.Graphics)
meth public void setActiveUnits(int)
supr javax.swing.JPanel
hfds activeUnits,disabledColor,enabledColor,progressDelegate,progressDelegateModel,totalUnits,unitHeight,unitWidth

CLSS public org.netbeans.lib.profiler.ui.components.EqualFlowLayout
cons public init()
cons public init(int)
cons public init(int,int,int)
meth public java.awt.Dimension minimumLayoutSize(java.awt.Container)
meth public java.awt.Dimension preferredLayoutSize(java.awt.Container)
meth public void layoutContainer(java.awt.Container)
supr java.awt.FlowLayout
hfds serialVersionUID

CLSS public abstract org.netbeans.lib.profiler.ui.components.FilterComponent
cons protected init()
intf org.netbeans.lib.profiler.global.CommonConstants
meth public abstract int getFilterType()
meth public abstract java.lang.String getFilterValue()
meth public abstract java.lang.String getHint()
meth public abstract javax.swing.JComponent getComponent()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void addFilterType(java.lang.String,int)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public abstract void setFilter(java.lang.String,int)
meth public abstract void setFilterType(int)
meth public abstract void setFilterValue(java.lang.String)
meth public abstract void setHint(java.lang.String)
meth public static java.lang.String[] getFilterValues(java.lang.String)
meth public static org.netbeans.lib.profiler.ui.components.FilterComponent create(boolean,boolean)
supr java.lang.Object
hcls Impl

CLSS public org.netbeans.lib.profiler.ui.components.FlatToolBar
cons public init()
cons public init(int)
cons public init(int,int)
fld public final static int BUTTON_STYLE_HORIZONTAL = 1
fld public final static int BUTTON_STYLE_VERICAL = 2
innr public static FlatMarginBorder
innr public static FlatRolloverButtonBorder
meth protected javax.swing.JButton createActionComponent(javax.swing.Action)
meth protected void addImpl(java.awt.Component,java.lang.Object,int)
supr javax.swing.JToolBar
hfds buttonStyle
hcls MyToolBarUI

CLSS public static org.netbeans.lib.profiler.ui.components.FlatToolBar$FlatMarginBorder
 outer org.netbeans.lib.profiler.ui.components.FlatToolBar
cons public init()
meth public java.awt.Insets getBorderInsets(java.awt.Component)
meth public java.awt.Insets getBorderInsets(java.awt.Component,java.awt.Insets)
supr javax.swing.border.AbstractBorder

CLSS public static org.netbeans.lib.profiler.ui.components.FlatToolBar$FlatRolloverButtonBorder
 outer org.netbeans.lib.profiler.ui.components.FlatToolBar
cons public init(java.awt.Color,java.awt.Color)
cons public init(java.awt.Color,java.awt.Color,java.awt.Color)
meth public java.awt.Insets getBorderInsets(java.awt.Component)
meth public java.awt.Insets getBorderInsets(java.awt.Component,java.awt.Insets)
meth public void paintBorder(java.awt.Component,java.awt.Graphics,int,int,int,int)
supr javax.swing.border.AbstractBorder
hfds borderPainted,normal,pressed,roll

CLSS public org.netbeans.lib.profiler.ui.components.HTMLLabel
cons public init()
cons public init(java.lang.String)
intf javax.swing.event.HyperlinkListener
meth protected void showURL(java.net.URL)
meth public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent)
meth public void setBackground(java.awt.Color)
meth public void setForeground(java.awt.Color)
meth public void setHorizontalAlignment(int)
meth public void setOpaque(boolean)
meth public void setText(java.lang.String)
supr javax.swing.JEditorPane
hfds halign,txt

CLSS public org.netbeans.lib.profiler.ui.components.HTMLTextArea
cons public init()
cons public init(java.lang.String)
intf javax.swing.event.HyperlinkListener
meth protected javax.swing.JMenuItem createCopyMenuItem()
meth protected javax.swing.JMenuItem createCutMenuItem()
meth protected javax.swing.JMenuItem createDeleteMenuItem()
meth protected javax.swing.JMenuItem createPasteMenuItem()
meth protected javax.swing.JMenuItem createSelectAllMenuItem()
meth protected void populatePopup(javax.swing.JPopupMenu)
meth protected void processKeyEvent(java.awt.event.KeyEvent)
meth protected void processMouseEvent(java.awt.event.MouseEvent)
meth protected void showURL(java.net.URL)
meth protected void showURL(java.net.URL,java.awt.event.InputEvent)
meth public boolean getShowPopup()
meth public java.awt.Dimension getMaximumSize()
meth public java.awt.Dimension getMinimumSize()
meth public java.awt.Dimension getPreferredSize()
meth public java.lang.String getText()
meth public java.net.URL getActiveLink()
meth public javax.swing.text.EditorKit getEditorKitForContentType(java.lang.String)
meth public void deleteSelection()
meth public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent)
meth public void paste()
meth public void setForeground(java.awt.Color)
meth public void setOpaque(boolean)
meth public void setShowPopup(boolean)
meth public void setText(java.lang.String)
supr javax.swing.JEditorPane
hfds COPY_STRING,CUT_STRING,DELETE_STRING,PASTE_STRING,SELECT_ALL_STRING,activeLink,currentText,forceSetText,messages,pendingText,showPopup
hcls ExtendedHTMLWriter,HTMLTextAreaTransferHandler,NcrToUnicode

CLSS public org.netbeans.lib.profiler.ui.components.ImageBlenderPanel
cons public init(java.awt.Image,java.awt.Image)
cons public init(java.awt.Image,java.awt.Image,java.awt.Color,float)
meth public void setBlendAlpha(float)
supr org.netbeans.lib.profiler.ui.components.ImagePanel
hfds background,blendAlpha,image1,image2

CLSS public org.netbeans.lib.profiler.ui.components.ImagePanel
cons public init(java.awt.Image)
cons public init(java.awt.Image,int)
meth protected static java.awt.Image loadImage(java.awt.Image)
meth protected void paintComponent(java.awt.Graphics)
meth protected void setPreferredBackground()
meth public void setImage(java.awt.Image)
meth public void setImageAlign(int)
supr javax.swing.JPanel
hfds image,imageAlign,mTracker

CLSS public org.netbeans.lib.profiler.ui.components.JAntiLabel
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,int)
cons public init(java.lang.String,javax.swing.Icon,int)
cons public init(javax.swing.Icon)
cons public init(javax.swing.Icon,int)
meth public void paintComponent(java.awt.Graphics)
supr javax.swing.JLabel

CLSS public org.netbeans.lib.profiler.ui.components.JCheckTree
cons public init()
innr public abstract interface static CheckTreeListener
meth protected boolean shouldShowCellTipAt(java.awt.Point)
meth protected static javax.swing.tree.TreeModel getDefaultTreeModel()
meth protected void processCellTipMouseMove(java.awt.event.MouseEvent)
meth public java.awt.Point getCellTipLocation()
meth public static void main(java.lang.String[])
meth public void addCheckTreeListener(org.netbeans.lib.profiler.ui.components.JCheckTree$CheckTreeListener)
meth public void processMouseEvent(java.awt.event.MouseEvent)
meth public void removeCheckTreeListener(org.netbeans.lib.profiler.ui.components.JCheckTree$CheckTreeListener)
supr org.netbeans.lib.profiler.ui.components.JExtendedTree
hfds checkTreeListeners,componentListener
hcls CheckTreeUI,PrivateComponentListener

CLSS public abstract interface static org.netbeans.lib.profiler.ui.components.JCheckTree$CheckTreeListener
 outer org.netbeans.lib.profiler.ui.components.JCheckTree
meth public abstract void checkNodeToggled(javax.swing.tree.TreePath,boolean)
meth public abstract void checkTreeChanged(java.util.Collection<org.netbeans.lib.profiler.ui.components.tree.CheckTreeNode>)

CLSS public org.netbeans.lib.profiler.ui.components.JCompoundSplitPane
cons public init()
cons public init(int)
cons public init(int,boolean)
cons public init(int,boolean,java.awt.Component,java.awt.Component)
cons public init(int,java.awt.Component,java.awt.Component)
supr org.netbeans.lib.profiler.ui.components.JExtendedSplitPane
hcls DividerMouseListener

CLSS public org.netbeans.lib.profiler.ui.components.JExtendedComboBox
cons public init()
meth protected void fireItemStateChanged(java.awt.event.ItemEvent)
meth public void firePopupMenuWillBecomeInvisible()
meth public void setModel(javax.swing.ComboBoxModel)
supr javax.swing.JComboBox
hfds closingWithSeparator,lastSelectedIndex,model
hcls ExtendedComboListRenderer

CLSS public org.netbeans.lib.profiler.ui.components.JExtendedRadioButton
cons public init(java.lang.String)
cons public init(java.lang.String,javax.swing.Icon)
cons public init(javax.swing.Icon)
innr public static DoubleIcon
meth public javax.swing.Icon getExtraIcon()
meth public void setExtraIcon(javax.swing.Icon)
supr javax.swing.JRadioButton
hfds extraIcon

CLSS public static org.netbeans.lib.profiler.ui.components.JExtendedRadioButton$DoubleIcon
 outer org.netbeans.lib.profiler.ui.components.JExtendedRadioButton
cons public init(javax.swing.Icon,javax.swing.Icon,int)
intf javax.swing.Icon
meth public int getIconHeight()
meth public int getIconWidth()
meth public int getIconsGap()
meth public javax.swing.Icon getIcon1()
meth public javax.swing.Icon getIcon2()
meth public void paintIcon(java.awt.Component,java.awt.Graphics,int,int)
supr java.lang.Object
hfds icon1,icon1VertOffset,icon2,icon2HorzOffset,icon2VertOffset,iconHeight,iconWidth,iconsGap

CLSS public org.netbeans.lib.profiler.ui.components.JExtendedSpinner
cons public init()
cons public init(javax.swing.SpinnerModel)
meth public java.awt.Dimension getMinimumSize()
meth public java.awt.Dimension getPreferredSize()
meth public static int getDefaultSpinnerHeight()
meth public void fixAccessibility()
meth public void setModel(javax.swing.SpinnerModel)
supr javax.swing.JSpinner
hfds defaultSpinnerHeight

CLSS public org.netbeans.lib.profiler.ui.components.JExtendedSplitPane
cons public init()
cons public init(int)
cons public init(int,boolean)
cons public init(int,boolean,java.awt.Component,java.awt.Component)
cons public init(int,java.awt.Component,java.awt.Component)
meth public void setBottomComponent(java.awt.Component)
meth public void setDividerSize(int)
meth public void setLeftComponent(java.awt.Component)
meth public void setRightComponent(java.awt.Component)
meth public void setTopComponent(java.awt.Component)
supr javax.swing.JSplitPane
hfds dividerLocation,dividerSize,splitPaneActionListener,splitPaneComponentListener
hcls SplitPaneActionListener,SplitPaneComponentListener

CLSS public org.netbeans.lib.profiler.ui.components.JExtendedTable
cons public init(javax.swing.table.TableModel)
fld protected int lastColumn
fld protected int lastRow
fld protected java.awt.Rectangle rendererRect
fld protected javax.swing.JToolTip cellTip
intf java.awt.event.MouseListener
intf java.awt.event.MouseMotionListener
intf java.awt.event.MouseWheelListener
intf org.netbeans.lib.profiler.ui.components.CellTipAware
meth protected javax.swing.JToolTip createCellTip()
meth public boolean canFindBePerformed()
meth public boolean findFirst()
meth public boolean findNext()
meth public boolean findPrevious()
meth public boolean isFindColumnValid()
meth public boolean isFindStringDefined()
meth public int getFindColumn()
meth public java.awt.Point getCellTipLocation()
meth public java.lang.String getFindString()
meth public javax.swing.JToolTip getCellTip()
meth public void ensureRowVisible(int)
meth public void mouseClicked(java.awt.event.MouseEvent)
meth public void mouseDragged(java.awt.event.MouseEvent)
meth public void mouseEntered(java.awt.event.MouseEvent)
meth public void mouseExited(java.awt.event.MouseEvent)
meth public void mouseMoved(java.awt.event.MouseEvent)
meth public void mousePressed(java.awt.event.MouseEvent)
meth public void mouseReleased(java.awt.event.MouseEvent)
meth public void mouseWheelMoved(java.awt.event.MouseWheelEvent)
meth public void processMouseEvent(java.awt.event.MouseEvent)
meth public void selectRowByContents(java.lang.String,int,boolean)
meth public void selectRowByInstance(java.lang.Object,int,boolean)
meth public void selectRowsByInstances(java.lang.Object[],int,boolean)
meth public void setFindParameters(java.lang.String,int)
meth public void setGridColor(java.awt.Color)
supr javax.swing.JTable
hfds internalFindString,userFindColumn,userFindString

CLSS public org.netbeans.lib.profiler.ui.components.JExtendedTree
cons public init()
fld protected java.awt.Rectangle rendererRect
fld protected javax.swing.JToolTip cellTip
fld protected javax.swing.tree.TreePath lastTreePath
intf org.netbeans.lib.profiler.ui.components.CellTipAware
meth protected javax.swing.JToolTip createCellTip()
meth protected void processCellTipMouseMove(java.awt.event.MouseEvent)
meth public java.awt.Point getCellTipLocation()
meth public javax.swing.JToolTip getCellTip()
meth public void processMouseEvent(java.awt.event.MouseEvent)
supr javax.swing.JTree
hfds componentListener
hcls PrivateComponentListener

CLSS public org.netbeans.lib.profiler.ui.components.JTitledPanel
cons public init(java.lang.String)
cons public init(java.lang.String,javax.swing.Icon)
cons public init(java.lang.String,javax.swing.Icon,boolean)
fld public final static int STATE_CLOSED = 1000
fld public final static int STATE_MAXIMIZED = 1002
fld public final static int STATE_MINIMIZED = 1003
fld public final static int STATE_RESTORED = 1001
meth protected java.awt.Color getTitleBorderColor()
meth protected java.awt.Component[] getAdditionalControls()
meth public boolean areButtonsEnabled()
meth public boolean isClosed()
meth public boolean isMaximized()
meth public boolean isMinimized()
meth public boolean isRestored()
meth public int getState()
meth public java.awt.Component add(java.awt.Component)
meth public java.awt.Component add(java.awt.Component,int)
meth public java.awt.Component add(java.lang.String,java.awt.Component)
meth public java.awt.Dimension getMinimumSize()
meth public java.awt.LayoutManager getLayout()
meth public java.lang.String getTitle()
meth public javax.swing.AbstractButton getPresenter()
meth public javax.swing.Icon getIcon()
meth public javax.swing.JPanel getContentPanel()
meth public void add(java.awt.Component,java.lang.Object)
meth public void add(java.awt.Component,java.lang.Object,int)
meth public void add(java.awt.PopupMenu)
meth public void addActionListener(java.awt.event.ActionListener)
meth public void close()
meth public void maximize()
meth public void minimize()
meth public void remove(int)
meth public void remove(java.awt.Component)
meth public void remove(java.awt.MenuComponent)
meth public void removeActionListener(java.awt.event.ActionListener)
meth public void removeAll()
meth public void restore()
meth public void setButtonsEnabled(boolean)
meth public void setLayout(java.awt.LayoutManager)
supr javax.swing.JPanel
hfds actionListeners,closePanelButton,closePanelIcon,contentPanel,icon,maximizePanelButton,maximizePanelIcon,minimizePanelButton,minimizePanelIcon,presenter,restorePanelButton,restorePanelIcon,showButtons,state,title,titlePanel
hcls DoubleClickListener,ImageIconButton,Presenter,ThinBevelBorder

CLSS public org.netbeans.lib.profiler.ui.components.JTreeTable
cons public init(org.netbeans.lib.profiler.ui.components.treetable.AbstractTreeTableModel)
fld protected int lastColumn
fld protected int lastRow
fld protected java.awt.Rectangle rendererRect
fld protected javax.swing.JToolTip cellTip
fld public final static boolean SORT_ORDER_ASC = true
fld public final static boolean SORT_ORDER_DESC = false
intf java.awt.event.KeyListener
intf java.awt.event.MouseListener
intf java.awt.event.MouseMotionListener
intf java.awt.event.MouseWheelListener
intf org.netbeans.lib.profiler.ui.components.CellTipAware
meth protected javax.swing.JToolTip createCellTip()
meth public boolean canFindBePerformed()
meth public boolean findFirst()
meth public boolean findNext()
meth public boolean findPrevious()
meth public boolean getSortingOrder()
meth public boolean isFindColumnValid()
meth public boolean isFindStringDefined()
meth public boolean silentlyFindFirst()
meth public int getFindColumn()
meth public int getSortingColumn()
meth public int getTreeCellOffsetX()
meth public java.awt.Point getCellTipLocation()
meth public java.lang.String getFindString()
meth public java.util.List<javax.swing.tree.TreePath> getExpandedPaths()
meth public javax.swing.JToolTip getCellTip()
meth public javax.swing.JTree getTree()
meth public org.netbeans.lib.profiler.results.CCTNode[] getPathToRoot(org.netbeans.lib.profiler.results.CCTNode)
meth public org.netbeans.lib.profiler.ui.components.tree.EnhancedTreeCellRenderer getTreeCellRenderer()
meth public void changeRoot(org.netbeans.lib.profiler.results.CCTNode)
meth public void keyPressed(java.awt.event.KeyEvent)
meth public void keyReleased(java.awt.event.KeyEvent)
meth public void keyTyped(java.awt.event.KeyEvent)
meth public void mouseClicked(java.awt.event.MouseEvent)
meth public void mouseDragged(java.awt.event.MouseEvent)
meth public void mouseEntered(java.awt.event.MouseEvent)
meth public void mouseExited(java.awt.event.MouseEvent)
meth public void mouseMoved(java.awt.event.MouseEvent)
meth public void mousePressed(java.awt.event.MouseEvent)
meth public void mouseReleased(java.awt.event.MouseEvent)
meth public void mouseWheelMoved(java.awt.event.MouseWheelEvent)
meth public void processMouseEvent(java.awt.event.MouseEvent)
meth public void resetTreeCellOffsetX()
meth public void selectNode(org.netbeans.lib.profiler.results.CCTNode,boolean)
meth public void selectRowByContents(java.lang.String,int,boolean)
meth public void setFindParameters(java.lang.String,int)
meth public void setGridColor(java.awt.Color)
meth public void setRowHeight(int)
meth public void setSortingColumn(int)
meth public void setSortingOrder(boolean)
meth public void setTreeCellOffsetX(int)
meth public void setTreeCellRenderer(org.netbeans.lib.profiler.ui.components.tree.EnhancedTreeCellRenderer)
meth public void setup(java.util.List<javax.swing.tree.TreePath>,javax.swing.tree.TreePath)
meth public void updateTreeTable()
meth public void updateTreeTableHeader()
meth public void updateUI()
supr javax.swing.JTable
hfds ZERO_INSETS,headerListener,headerRenderer,internalFindString,isGTK,lastFocusedColumn,sortAscIcon,sortDescIcon,tableHeader,tree,treeSignExtent,treeSignRightMargin,treeTableModel,treeTableModelAdapter,userFindColumn,userFindString
hcls ListToTreeSelectionModelWrapper,TableHeaderListener,TreeTableCellRenderer

CLSS public org.netbeans.lib.profiler.ui.components.LazyComboBox<%0 extends java.lang.Object>
cons public init(org.netbeans.lib.profiler.ui.components.LazyComboBox$Populator<{org.netbeans.lib.profiler.ui.components.LazyComboBox%0}>)
innr public abstract static Populator
meth protected int getPreferredWidth()
meth protected void fireItemStateChanged(java.awt.event.ItemEvent)
meth protected void selectionChanged()
meth public final int getSelectedIndex()
meth public final java.awt.Dimension getMaximumSize()
meth public final java.awt.Dimension getMinimumSize()
meth public final java.awt.Dimension getPreferredSize()
meth public final void resetModel()
meth public final void setModel(javax.swing.ComboBoxModel<{org.netbeans.lib.profiler.ui.components.LazyComboBox%0}>)
meth public final void setSelectedItem(java.lang.Object)
supr javax.swing.JComboBox<{org.netbeans.lib.profiler.ui.components.LazyComboBox%0}>
hcls LazyComboBoxModel

CLSS public abstract static org.netbeans.lib.profiler.ui.components.LazyComboBox$Populator<%0 extends java.lang.Object>
 outer org.netbeans.lib.profiler.ui.components.LazyComboBox
cons public init()
meth protected abstract {org.netbeans.lib.profiler.ui.components.LazyComboBox$Populator%0} initial()
meth protected abstract {org.netbeans.lib.profiler.ui.components.LazyComboBox$Populator%0}[] populate()
supr java.lang.Object

CLSS public final org.netbeans.lib.profiler.ui.components.NoCaret
cons public init()
intf javax.swing.text.Caret
meth public boolean isSelectionVisible()
meth public boolean isVisible()
meth public int getBlinkRate()
meth public int getDot()
meth public int getMark()
meth public java.awt.Point getMagicCaretPosition()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void deinstall(javax.swing.text.JTextComponent)
meth public void install(javax.swing.text.JTextComponent)
meth public void moveDot(int)
meth public void paint(java.awt.Graphics)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void setBlinkRate(int)
meth public void setDot(int)
meth public void setMagicCaretPosition(java.awt.Point)
meth public void setSelectionVisible(boolean)
meth public void setVisible(boolean)
supr java.lang.Object

CLSS public abstract org.netbeans.lib.profiler.ui.components.ProfilerToolbar
cons protected init()
innr public abstract static Provider
innr public static Impl
innr public static SimpleFocusTraversalPolicy
meth public abstract int getComponentCount()
meth public abstract java.awt.Component add(java.awt.Component)
meth public abstract java.awt.Component add(java.awt.Component,int)
meth public abstract java.awt.Component add(javax.swing.Action)
meth public abstract javax.swing.JComponent getComponent()
meth public abstract void addFiller()
meth public abstract void addSeparator()
meth public abstract void addSpace(int)
meth public abstract void remove(int)
meth public abstract void remove(java.awt.Component)
meth public java.awt.Component add(org.netbeans.lib.profiler.ui.components.ProfilerToolbar)
meth public java.awt.Component add(org.netbeans.lib.profiler.ui.components.ProfilerToolbar,int)
meth public static org.netbeans.lib.profiler.ui.components.ProfilerToolbar create(boolean)
meth public void remove(org.netbeans.lib.profiler.ui.components.ProfilerToolbar)
supr java.lang.Object

CLSS public static org.netbeans.lib.profiler.ui.components.ProfilerToolbar$Impl
 outer org.netbeans.lib.profiler.ui.components.ProfilerToolbar
cons protected init(boolean)
fld protected final javax.swing.JComponent component
fld protected final javax.swing.JToolBar toolbar
meth public int getComponentCount()
meth public java.awt.Component add(java.awt.Component)
meth public java.awt.Component add(java.awt.Component,int)
meth public java.awt.Component add(javax.swing.Action)
meth public java.awt.Component add(org.netbeans.lib.profiler.ui.components.ProfilerToolbar)
meth public java.awt.Component add(org.netbeans.lib.profiler.ui.components.ProfilerToolbar,int)
meth public javax.swing.JComponent getComponent()
meth public void addFiller()
meth public void addSeparator()
meth public void addSpace(int)
meth public void remove(int)
meth public void remove(java.awt.Component)
meth public void remove(org.netbeans.lib.profiler.ui.components.ProfilerToolbar)
supr org.netbeans.lib.profiler.ui.components.ProfilerToolbar

CLSS public abstract static org.netbeans.lib.profiler.ui.components.ProfilerToolbar$Provider
 outer org.netbeans.lib.profiler.ui.components.ProfilerToolbar
cons public init()
meth public abstract org.netbeans.lib.profiler.ui.components.ProfilerToolbar create(boolean)
supr java.lang.Object

CLSS public static org.netbeans.lib.profiler.ui.components.ProfilerToolbar$SimpleFocusTraversalPolicy
 outer org.netbeans.lib.profiler.ui.components.ProfilerToolbar
cons public init()
meth protected boolean focusable(java.awt.Component)
meth protected java.awt.Container topContainer(java.awt.Container)
meth protected java.util.List<java.awt.Component> components(java.awt.Container)
meth public java.awt.Component getComponentAfter(java.awt.Container,java.awt.Component)
meth public java.awt.Component getComponentBefore(java.awt.Container,java.awt.Component)
meth public java.awt.Component getDefaultComponent(java.awt.Container)
meth public java.awt.Component getFirstComponent(java.awt.Container)
meth public java.awt.Component getLastComponent(java.awt.Container)
supr java.awt.FocusTraversalPolicy

CLSS public org.netbeans.lib.profiler.ui.components.SnippetPanel
cons public init(java.lang.String,javax.swing.JComponent)
innr public static Padding
intf java.awt.event.FocusListener
intf java.awt.event.KeyListener
intf java.awt.event.MouseListener
meth public boolean isCollapsed()
meth public java.lang.String getSnippetName()
meth public javax.swing.JComponent getContent()
meth public void focusGained(java.awt.event.FocusEvent)
meth public void focusLost(java.awt.event.FocusEvent)
meth public void keyPressed(java.awt.event.KeyEvent)
meth public void keyReleased(java.awt.event.KeyEvent)
meth public void keyTyped(java.awt.event.KeyEvent)
meth public void mouseClicked(java.awt.event.MouseEvent)
meth public void mouseEntered(java.awt.event.MouseEvent)
meth public void mouseExited(java.awt.event.MouseEvent)
meth public void mousePressed(java.awt.event.MouseEvent)
meth public void mouseReleased(java.awt.event.MouseEvent)
meth public void requestFocus()
meth public void setCollapsed(boolean)
meth public void setContent(javax.swing.JComponent)
meth public void setSnippetName(java.lang.String)
supr javax.swing.JPanel
hfds backgroundColor,collapsed,content,focusedBackgroundColor,lineColor,snippetName,title
hcls Title,TitleUI

CLSS public static org.netbeans.lib.profiler.ui.components.SnippetPanel$Padding
 outer org.netbeans.lib.profiler.ui.components.SnippetPanel
cons public init()
meth protected void paintComponent(java.awt.Graphics)
supr javax.swing.JPanel

CLSS public org.netbeans.lib.profiler.ui.components.ThinBevelBorder
cons public init(int)
cons public init(int,java.awt.Color,java.awt.Color)
meth protected void paintLoweredBevel(java.awt.Component,java.awt.Graphics,int,int,int,int)
meth protected void paintRaisedBevel(java.awt.Component,java.awt.Graphics,int,int,int,int)
supr javax.swing.border.BevelBorder

CLSS public org.netbeans.lib.profiler.ui.components.VerticalLayout
cons public init()
cons public init(int,int)
intf java.awt.LayoutManager
meth public int getHGap()
meth public int getVGap()
meth public java.awt.Dimension minimumLayoutSize(java.awt.Container)
meth public java.awt.Dimension preferredLayoutSize(java.awt.Container)
meth public java.lang.String toString()
meth public void addLayoutComponent(java.lang.String,java.awt.Component)
meth public void layoutContainer(java.awt.Container)
meth public void removeLayoutComponent(java.awt.Component)
supr java.lang.Object
hfds hgap,vgap

CLSS public org.netbeans.lib.profiler.ui.components.XPStyleBorder
cons public init(java.awt.Color,java.awt.Color)
cons public init(java.awt.Color,java.awt.Color,int)
fld public final static int BORDER_STATE_DEFAULT = 10
fld public final static int BORDER_STATE_FOCUSED = 20
fld public final static int BORDER_STATE_SELECTED = 30
meth public boolean isBorderOpaque()
meth public boolean isDefault()
meth public boolean isFocused()
meth public boolean isSelected()
meth public int getBorderState()
meth public java.awt.Color getBackgroundColor()
meth public java.awt.Color getForegroundColor()
meth public java.awt.Insets getBorderInsets(java.awt.Component)
meth public static java.awt.Insets getBorderInsets()
meth public static org.netbeans.lib.profiler.ui.components.XPStyleBorder getDefaultInstance(java.awt.Color,java.awt.Color)
meth public static org.netbeans.lib.profiler.ui.components.XPStyleBorder getFocusedInstance(java.awt.Color,java.awt.Color)
meth public static org.netbeans.lib.profiler.ui.components.XPStyleBorder getSelectedInstance(java.awt.Color,java.awt.Color)
meth public void paintBorder(java.awt.Component,java.awt.Graphics,int,int,int,int)
meth public void setBackgroundColor(java.awt.Color)
meth public void setBorderState(int)
meth public void setDefault()
meth public void setFocused()
meth public void setForegroundColor(java.awt.Color)
meth public void setSelected()
supr javax.swing.border.AbstractBorder
hfds DEFAULT_BOTTOM_DARK_CLR,DEFAULT_BOTTOM_LIGHT_CLR,DEFAULT_TOP_DARK_CLR,DEFAULT_TOP_LIGHT_CLR,FOCUSED_BOTTOM_DARK_CLR,FOCUSED_BOTTOM_LIGHT_CLR,FOCUSED_TOP_DARK_CLR,FOCUSED_TOP_LIGHT_CLR,OUTLINE_CLR,SELECTED_BOTTOM_DARK_CLR,SELECTED_BOTTOM_LIGHT_CLR,SELECTED_TOP_DARK_CLR,SELECTED_TOP_LIGHT_CLR,backgroundColor,backgroundFade,borderState,isNimbus,startColor,stopColor

CLSS public org.netbeans.lib.profiler.ui.components.table.BooleanTableCellRenderer
cons public init()
meth protected void setState(javax.swing.JTable,java.lang.Object,boolean,boolean,int,int)
meth protected void setValue(javax.swing.JTable,java.lang.Object,int,int)
meth public java.awt.Component getTableCellRendererComponentPersistent(javax.swing.JTable,java.lang.Object,boolean,boolean,int,int)
supr org.netbeans.lib.profiler.ui.components.table.EnhancedTableCellRenderer
hfds checkBox

CLSS public org.netbeans.lib.profiler.ui.components.table.ClassNameTableCellRenderer
cons public init()
cons public init(int)
meth protected void setRowForeground(java.awt.Color)
meth protected void setValue(javax.swing.JTable,java.lang.Object,int,int)
meth public java.awt.Component getTableCellRendererComponentPersistent(javax.swing.JTable,java.lang.Object,boolean,boolean,int,int)
supr org.netbeans.lib.profiler.ui.components.table.EnhancedTableCellRenderer
hfds label1,label2

CLSS public org.netbeans.lib.profiler.ui.components.table.CustomBarCellRenderer
cons public init(long,long)
fld protected double relValue
fld protected long max
fld protected long min
fld public final static java.awt.Color BAR_FOREGROUND_COLOR
meth protected double calculateViewValue(double)
meth protected double calculateViewValue(long)
meth protected void setValue(javax.swing.JTable,java.lang.Object,int,int)
meth public java.awt.Component getTableCellRendererComponentPersistent(javax.swing.JTable,java.lang.Object,boolean,boolean,int,int)
meth public void paintComponent(java.awt.Graphics)
meth public void setMaximum(long)
meth public void setMinimum(long)
meth public void setRelValue(double)
supr org.netbeans.lib.profiler.ui.components.table.EnhancedTableCellRenderer

CLSS public org.netbeans.lib.profiler.ui.components.table.CustomSortableHeaderRenderer
cons public init(javax.swing.ImageIcon,javax.swing.ImageIcon)
intf javax.swing.table.TableCellRenderer
meth public boolean getSortingOrder()
meth public int getPressedColumn()
meth public int getSortingColumn()
meth public java.awt.Component getTableCellRendererComponent(javax.swing.JTable,java.lang.Object,boolean,boolean,int,int)
meth public void reverseSortingOrder()
meth public void setPressedColumn(int)
meth public void setSortingColumn(int)
meth public void setSortingOrder(boolean)
supr java.lang.Object
hfds ascIcon,descIcon,pressedColumn,sortOrder,sortingColumn

CLSS public org.netbeans.lib.profiler.ui.components.table.DiffBarCellRenderer
cons public init(long,long)
fld public final static java.awt.Color BAR_FOREGROUND2_COLOR
meth protected double calculateViewValue(double)
meth protected double calculateViewValue(long)
meth public void paintComponent(java.awt.Graphics)
supr org.netbeans.lib.profiler.ui.components.table.CustomBarCellRenderer

CLSS public abstract org.netbeans.lib.profiler.ui.components.table.EnhancedTableCellRenderer
cons public init()
fld protected boolean supportsFocusBorder
intf org.netbeans.lib.profiler.ui.components.table.TableCellRendererPersistent
meth protected abstract void setValue(javax.swing.JTable,java.lang.Object,int,int)
meth protected void setRowBackground(java.awt.Color)
meth protected void setRowForeground(java.awt.Color)
meth protected void setState(javax.swing.JTable,java.lang.Object,boolean,boolean,int,int)
meth public abstract java.awt.Component getTableCellRendererComponentPersistent(javax.swing.JTable,java.lang.Object,boolean,boolean,int,int)
meth public boolean getSupportsFocusBorder()
meth public final java.awt.Component getTableCellRendererComponent(javax.swing.JTable,java.lang.Object,boolean,boolean,int,int)
meth public int getHorizontalAlignment()
meth public static java.awt.Color getDarker(java.awt.Color)
meth public void setBorder(javax.swing.border.Border)
meth public void setHorizontalAlignment(int)
meth public void setSupportsFocusBorder(boolean)
supr javax.swing.JPanel
hfds darkerUnselectedBackground,horizontalAlignment,originalBorder,originalBorderInsets,unselectedBackground,unselectedForeground

CLSS public org.netbeans.lib.profiler.ui.components.table.ExtendedTableModel
cons public init(org.netbeans.lib.profiler.ui.components.table.SortableTableModel)
meth public boolean getInitialSorting(int)
meth public boolean isRealColumnVisible(int)
meth public boolean[] getColumnsVisibility()
meth public int getColumnCount()
meth public int getRealColumn(int)
meth public int getRowCount()
meth public int getVirtualColumn(int)
meth public java.lang.Class getColumnClass(int)
meth public java.lang.Object getValueAt(int,int)
meth public java.lang.String getColumnName(int)
meth public java.lang.String getColumnToolTipText(int)
meth public void hideRealColumn(int)
meth public void setColumnsVisibility(boolean[])
meth public void setRealColumnVisibility(int,boolean)
meth public void showRealColumn(int)
meth public void sortByColumn(int,boolean)
supr org.netbeans.lib.profiler.ui.components.table.SortableTableModel
hfds columnsMapping,columnsVisibility,realColumnsCount,realModel,virtualColumnsCount

CLSS public org.netbeans.lib.profiler.ui.components.table.HTMLLabelTableCellRenderer
cons public init()
cons public init(int)
cons public init(int,boolean)
fld protected org.netbeans.lib.profiler.ui.components.HTMLLabel label
meth protected void handleCursor(java.awt.Cursor)
meth protected void handleLink(java.net.URL)
meth protected void setRowBackground(java.awt.Color)
meth protected void setValue(javax.swing.JTable,java.lang.Object,int,int)
meth public java.awt.Component getTableCellRendererComponentPersistent(javax.swing.JTable,java.lang.Object,boolean,boolean,int,int)
supr org.netbeans.lib.profiler.ui.components.table.EnhancedTableCellRenderer

CLSS public org.netbeans.lib.profiler.ui.components.table.JExtendedTablePanel
cons public init(org.netbeans.lib.profiler.ui.components.JExtendedTable)
fld protected javax.swing.JScrollPane extendedTableScrollPane
fld protected org.netbeans.lib.profiler.ui.components.JExtendedTable extendedTable
meth public javax.swing.JScrollPane getScrollPane()
meth public void clearBorders()
meth public void setCorner(java.lang.String,java.awt.Component)
meth public void setEnabled(boolean)
supr javax.swing.JPanel
hfds extendedTableViewport
hcls CustomExtendedTableViewport

CLSS public org.netbeans.lib.profiler.ui.components.table.LabelBracketTableCellRenderer
cons public init()
cons public init(int)
cons public init(int,java.lang.String)
meth protected void setRowForeground(java.awt.Color)
meth protected void setValue(javax.swing.JTable,java.lang.Object,int,int)
meth public java.awt.Component getTableCellRendererComponentPersistent(javax.swing.JTable,java.lang.Object,boolean,boolean,int,int)
supr org.netbeans.lib.profiler.ui.components.table.EnhancedTableCellRenderer
hfds digitsWidth,label1,label2

CLSS public org.netbeans.lib.profiler.ui.components.table.LabelTableCellRenderer
cons public init()
cons public init(int)
fld protected javax.swing.JLabel label
meth protected void setRowForeground(java.awt.Color)
meth protected void setValue(javax.swing.JTable,java.lang.Object,int,int)
meth public java.awt.Component getTableCellRendererComponentPersistent(javax.swing.JTable,java.lang.Object,boolean,boolean,int,int)
supr org.netbeans.lib.profiler.ui.components.table.EnhancedTableCellRenderer

CLSS public org.netbeans.lib.profiler.ui.components.table.MethodNameTableCellRenderer
cons public init()
meth protected void setRowForeground(java.awt.Color)
meth protected void setValue(javax.swing.JTable,java.lang.Object,int,int)
meth public java.awt.Component getTableCellRendererComponentPersistent(javax.swing.JTable,java.lang.Object,boolean,boolean,int,int)
supr org.netbeans.lib.profiler.ui.components.table.EnhancedTableCellRenderer
hfds label1,label2,label3

CLSS public abstract org.netbeans.lib.profiler.ui.components.table.SortableTableModel
cons public init()
fld public final static boolean SORT_ORDER_ASC = true
fld public final static boolean SORT_ORDER_DESC = false
meth public abstract boolean getInitialSorting(int)
meth public abstract void sortByColumn(int,boolean)
meth public boolean getSortingOrder()
meth public int getSortingColumn()
meth public java.lang.String getColumnToolTipText(int)
meth public void setInitialSorting(int,boolean)
meth public void setTable(javax.swing.JTable)
supr javax.swing.table.AbstractTableModel
hfds headerListener,headerRenderer,lastFocusedColumn,sortAscIcon,sortDescIcon,tableHeader
hcls HeaderListener

CLSS public abstract interface org.netbeans.lib.profiler.ui.components.table.TableCellRendererPersistent
intf javax.swing.table.TableCellRenderer
meth public abstract java.awt.Component getTableCellRendererComponentPersistent(javax.swing.JTable,java.lang.Object,boolean,boolean,int,int)

CLSS public org.netbeans.lib.profiler.ui.components.tree.CheckTreeCellRenderer
cons public init()
intf org.netbeans.lib.profiler.ui.components.tree.TreeCellRendererPersistent
meth public java.awt.Component getTreeCellRendererComponent(javax.swing.JTree,java.lang.Object,boolean,boolean,boolean,int,boolean)
meth public java.awt.Component getTreeCellRendererComponentPersistent(javax.swing.JTree,java.lang.Object,boolean,boolean,boolean,int,boolean)
meth public static java.awt.Dimension getCheckBoxDimension()
supr javax.swing.JPanel
hfds checkBox,checkBoxDimension,checkBoxModel,persistentRenderer,treeRenderer,treeRendererComponent

CLSS public org.netbeans.lib.profiler.ui.components.tree.CheckTreeNode
cons public init()
cons public init(java.lang.Object)
cons public init(java.lang.Object,javax.swing.Icon)
fld protected int checkState
fld public final static int STATE_CHECKED = 1
fld public final static int STATE_PARTIALLY_CHECKED = 4
fld public final static int STATE_UNCHECKED = 2
meth protected java.util.Collection setPartiallyChecked()
meth public boolean isFullyChecked()
meth public boolean isPartiallyChecked()
meth public int getCheckState()
meth public java.util.Collection setChecked(boolean)
meth public java.util.Collection toggleState()
meth public javax.swing.Icon getIcon()
supr javax.swing.tree.DefaultMutableTreeNode
hfds icon

CLSS public org.netbeans.lib.profiler.ui.components.tree.EnhancedTreeCellRenderer
cons public init()
fld protected boolean hasFocus
fld protected boolean selected
fld protected java.awt.Color backgroundNonSelectionColor
fld protected java.awt.Color backgroundSelectionColor
fld protected java.awt.Color borderSelectionColor
fld protected java.awt.Color textNonSelectionColor
fld protected java.awt.Color textSelectionColor
intf org.netbeans.lib.profiler.ui.components.tree.TreeCellRendererPersistent
meth protected java.lang.String getLabel1Text(java.lang.Object,java.lang.String)
meth protected java.lang.String getLabel2Text(java.lang.Object,java.lang.String)
meth protected java.lang.String getLabel3Text(java.lang.Object,java.lang.String)
meth protected javax.swing.Icon getClosedIcon(java.lang.Object)
meth protected javax.swing.Icon getLeafIcon(java.lang.Object)
meth protected javax.swing.Icon getOpenIcon(java.lang.Object)
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public java.awt.Color getBackgroundNonSelectionColor()
meth public java.awt.Color getBackgroundSelectionColor()
meth public java.awt.Color getBorderSelectionColor()
meth public java.awt.Color getTextNonSelectionColor()
meth public java.awt.Color getTextSelectionColor()
meth public java.awt.Component getTreeCellRendererComponent(javax.swing.JTree,java.lang.Object,boolean,boolean,boolean,int,boolean)
meth public java.awt.Component getTreeCellRendererComponentPersistent(javax.swing.JTree,java.lang.Object,boolean,boolean,boolean,int,boolean)
meth public java.awt.Dimension getMaximumSize()
meth public java.awt.Dimension getMinimumSize()
meth public java.awt.Dimension getPreferredSize()
meth public java.awt.Font getFont()
meth public java.awt.Insets getInsets()
meth public javax.swing.Icon getClosedIcon()
meth public javax.swing.Icon getLeafIcon()
meth public javax.swing.Icon getOpenIcon()
meth public void doLayout()
meth public void firePropertyChange(java.lang.String,boolean,boolean)
meth public void firePropertyChange(java.lang.String,byte,byte)
meth public void firePropertyChange(java.lang.String,char,char)
meth public void firePropertyChange(java.lang.String,double,double)
meth public void firePropertyChange(java.lang.String,float,float)
meth public void firePropertyChange(java.lang.String,int,int)
meth public void firePropertyChange(java.lang.String,long,long)
meth public void firePropertyChange(java.lang.String,short,short)
meth public void paint(java.awt.Graphics)
meth public void repaint()
meth public void repaint(java.awt.Rectangle)
meth public void repaint(long,int,int,int,int)
meth public void revalidate()
meth public void setBackground(java.awt.Color)
meth public void setBackgroundNonSelectionColor(java.awt.Color)
meth public void setBackgroundSelectionColor(java.awt.Color)
meth public void setBorderSelectionColor(java.awt.Color)
meth public void setClosedIcon(javax.swing.Icon)
meth public void setFont(java.awt.Font)
meth public void setLeafIcon(javax.swing.Icon)
meth public void setOpenIcon(javax.swing.Icon)
meth public void setTextNonSelectionColor(java.awt.Color)
meth public void setTextSelectionColor(java.awt.Color)
meth public void validate()
supr javax.swing.JPanel
hfds ZERO_INSETS,closedIcon,label1,label2,label3,leafIcon,openIcon,tree
hcls InternalLabel

CLSS public org.netbeans.lib.profiler.ui.components.tree.MethodNameTreeCellRenderer
cons public init()
meth protected java.lang.String getLabel1Text(java.lang.Object,java.lang.String)
meth protected java.lang.String getLabel2Text(java.lang.Object,java.lang.String)
meth protected java.lang.String getLabel3Text(java.lang.Object,java.lang.String)
meth protected javax.swing.Icon getClosedIcon(java.lang.Object)
meth protected javax.swing.Icon getLeafIcon(java.lang.Object)
meth protected javax.swing.Icon getOpenIcon(java.lang.Object)
meth public java.awt.Component getTreeCellRendererComponentPersistent(javax.swing.JTree,java.lang.Object,boolean,boolean,boolean,int,boolean)
supr org.netbeans.lib.profiler.ui.components.tree.EnhancedTreeCellRenderer
hfds allThreadsIcon,threadIcon

CLSS public abstract interface org.netbeans.lib.profiler.ui.components.tree.TreeCellRendererPersistent
intf javax.swing.tree.TreeCellRenderer
meth public abstract java.awt.Component getTreeCellRendererComponentPersistent(javax.swing.JTree,java.lang.Object,boolean,boolean,boolean,int,boolean)

CLSS public abstract org.netbeans.lib.profiler.ui.components.treetable.AbstractTreeTableModel
cons public init(org.netbeans.lib.profiler.results.CCTNode)
cons public init(org.netbeans.lib.profiler.results.CCTNode,boolean,int,boolean)
cons public init(org.netbeans.lib.profiler.results.CCTNode,int,boolean)
fld protected boolean initialSortingOrder
fld protected boolean supportsSorting
fld protected int initialSortingColumn
fld protected org.netbeans.lib.profiler.results.CCTNode root
intf org.netbeans.lib.profiler.ui.components.treetable.TreeTableModel
meth public abstract int getColumnCount()
meth public abstract java.lang.Class getColumnClass(int)
meth public abstract java.lang.Object getValueAt(java.lang.Object,int)
meth public abstract java.lang.String getColumnName(int)
meth public boolean getInitialSorting(int)
meth public boolean getInitialSortingOrder()
meth public boolean isCellEditable(java.lang.Object,int)
meth public boolean isLeaf(java.lang.Object)
meth public boolean supportsSorting()
meth public int getChildCount(java.lang.Object)
meth public int getIndexOfChild(java.lang.Object,java.lang.Object)
meth public int getInitialSortingColumn()
meth public java.lang.Object getChild(java.lang.Object,int)
meth public java.lang.Object getRoot()
meth public java.lang.String getColumnToolTipText(int)
meth public org.netbeans.lib.profiler.results.CCTNode[] getPathToRoot(org.netbeans.lib.profiler.results.CCTNode)
meth public void addTreeModelListener(javax.swing.event.TreeModelListener)
meth public void fireTreeNodesChanged(java.lang.Object,java.lang.Object[],int[],java.lang.Object[])
meth public void fireTreeNodesInserted(java.lang.Object,java.lang.Object[],int[],java.lang.Object[])
meth public void fireTreeNodesRemoved(java.lang.Object,java.lang.Object[],int[],java.lang.Object[])
meth public void fireTreeStructureChanged(java.lang.Object,java.lang.Object[],int[],java.lang.Object[])
meth public void removeTreeModelListener(javax.swing.event.TreeModelListener)
meth public void setRoot(java.lang.Object)
meth public void setValueAt(java.lang.Object,java.lang.Object,int)
meth public void sortByColumn(int,boolean)
meth public void valueForPathChanged(javax.swing.tree.TreePath,java.lang.Object)
supr javax.swing.table.DefaultTableModel

CLSS public org.netbeans.lib.profiler.ui.components.treetable.ExtendedTreeTableModel
cons public init(org.netbeans.lib.profiler.ui.components.treetable.AbstractTreeTableModel)
meth public boolean getInitialSorting(int)
meth public boolean getInitialSortingOrder()
meth public boolean isCellEditable(java.lang.Object,int)
meth public boolean isLeaf(java.lang.Object)
meth public boolean isRealColumnVisible(int)
meth public boolean[] getColumnsVisibility()
meth public int getColumnCount()
meth public int getInitialSortingColumn()
meth public int getRealColumn(int)
meth public int getVirtualColumn(int)
meth public java.lang.Class getColumnClass(int)
meth public java.lang.Object getRoot()
meth public java.lang.Object getValueAt(java.lang.Object,int)
meth public java.lang.String getColumnName(int)
meth public java.lang.String getColumnToolTipText(int)
meth public void hideRealColumn(int)
meth public void setColumnsVisibility(boolean[])
meth public void setRealColumnVisibility(int,boolean)
meth public void setRoot(java.lang.Object)
meth public void setValueAt(java.lang.Object,java.lang.Object,int)
meth public void showRealColumn(int)
meth public void sortByColumn(int,boolean)
supr org.netbeans.lib.profiler.ui.components.treetable.AbstractTreeTableModel
hfds columnsMapping,columnsVisibility,realColumnsCount,realModel,virtualColumnsCount

CLSS public org.netbeans.lib.profiler.ui.components.treetable.JTreeTablePanel
cons public init(org.netbeans.lib.profiler.ui.components.JTreeTable)
fld protected javax.swing.JPanel scrollBarPanel
fld protected javax.swing.JScrollBar scrollBar
fld protected javax.swing.JScrollPane treeTableScrollPane
fld protected org.netbeans.lib.profiler.ui.components.JTreeTable treeTable
meth public javax.swing.JScrollPane getScrollPane()
meth public void clearBorders()
meth public void setCorner(java.lang.String,java.awt.Component)
supr javax.swing.JPanel
hfds invisibleRowsCount,treeTableViewport
hcls CustomTreeTableViewport

CLSS public abstract interface org.netbeans.lib.profiler.ui.components.treetable.TreeTableModel
intf javax.swing.tree.TreeModel
meth public abstract boolean isCellEditable(java.lang.Object,int)
meth public abstract int getColumnCount()
meth public abstract java.lang.Class getColumnClass(int)
meth public abstract java.lang.Object getValueAt(java.lang.Object,int)
meth public abstract java.lang.String getColumnName(int)
meth public abstract void setValueAt(java.lang.Object,java.lang.Object,int)

CLSS public org.netbeans.lib.profiler.ui.components.treetable.TreeTableModelAdapter
cons public init(org.netbeans.lib.profiler.ui.components.treetable.AbstractTreeTableModel,org.netbeans.lib.profiler.ui.components.JTreeTable)
fld protected javax.swing.JTree tree
fld protected org.netbeans.lib.profiler.ui.components.JTreeTable treeTable
fld protected org.netbeans.lib.profiler.ui.components.treetable.AbstractTreeTableModel treeTableModel
meth protected java.lang.Object nodeForRow(int)
meth protected void delayedFireTableDataChanged()
meth public boolean isCellEditable(int,int)
meth public final boolean isFiringChange()
meth public int getColumnCount()
meth public int getRowCount()
meth public java.lang.Class getColumnClass(int)
meth public java.lang.Object getValueAt(int,int)
meth public java.lang.String getColumnName(int)
meth public java.util.List<javax.swing.tree.TreePath> getExpandedPaths()
meth public javax.swing.tree.TreePath getRootPath()
meth public void changeRoot(org.netbeans.lib.profiler.results.CCTNode)
meth public void expandRoot()
meth public void restoreExpandedPaths(java.util.List<javax.swing.tree.TreePath>)
meth public void setValueAt(java.lang.Object,int,int)
meth public void setup(java.util.List<javax.swing.tree.TreePath>,javax.swing.tree.TreePath)
meth public void updateTreeTable()
supr javax.swing.table.AbstractTableModel
hfds firingChange

CLSS public org.netbeans.lib.profiler.ui.cpu.CCTDisplay
cons public init(org.netbeans.lib.profiler.ui.cpu.CPUResUserActionsHandler,java.lang.Boolean)
cons public init(org.netbeans.lib.profiler.ui.cpu.CPUResUserActionsHandler,org.netbeans.lib.profiler.ui.cpu.CPUSelectionHandler,java.lang.Boolean)
fld protected boolean sortOrder
fld protected int sortingColumn
fld protected org.netbeans.lib.profiler.ui.components.FilterComponent filterComponent
fld protected org.netbeans.lib.profiler.ui.components.JTreeTable treeTable
fld protected org.netbeans.lib.profiler.ui.components.treetable.JTreeTablePanel treeTablePanel
intf org.netbeans.lib.profiler.ui.cpu.ScreenshotProvider
meth protected java.lang.Float getNodeTimeRel(long,float)
meth protected java.lang.String getNodeInvocations(int)
meth protected java.lang.String getNodeSecondaryTime(long)
meth protected java.lang.String getNodeTime(long,float)
meth protected java.lang.String getSelectedMethodName()
meth protected void enableDisablePopup(org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode)
meth protected void initColumnSelectorItems()
meth protected void initColumnsData()
meth public boolean findFirst()
meth public boolean findNext()
meth public boolean findPrevious()
meth public boolean fitsVisibleArea()
meth public boolean getSortingOrder()
meth public boolean isFilterVisible()
meth public boolean isFindStringDefined()
meth public boolean silentlyFindFirst()
meth public boolean[] getColumnsVisibility()
meth public int getCurrentThreadId()
meth public int getFilterType()
meth public int getSortingColumn()
meth public java.awt.image.BufferedImage getCurrentViewScreenshot(boolean)
meth public java.lang.Object getResultsViewReference()
meth public java.lang.String getDefaultViewName()
meth public java.lang.String getFilterValue()
meth public java.lang.String getFindString()
meth public void addResultsViewFocusListener(java.awt.event.FocusListener)
meth public void clearSelection()
meth public void exportData(int,org.netbeans.lib.profiler.results.ExportDataDumper,boolean,java.lang.String)
meth public void prepareResults()
meth public void removeResultsViewFocusListener(java.awt.event.FocusListener)
meth public void requestFocus()
meth public void reset()
meth public void setColumnsVisibility(boolean[])
meth public void setDefaultSorting()
meth public void setFilterValues(java.lang.String,int)
meth public void setFilterVisible(boolean)
meth public void setFindString(java.lang.String)
meth public void setSorting(int,boolean)
meth public void setSorting(int,boolean,boolean)
supr org.netbeans.lib.profiler.ui.cpu.SnapshotCPUResultsPanel
hfds CLASS_COLUMN_NAME,CLASS_COLUMN_TOOLTIP,CLASS_FILTER_HINT,DEBUG,FILTER_ITEM_NAME,INVOCATIONS_COLUMN_NAME,INVOCATIONS_COLUMN_TOOLTIP,METHOD_COLUMN_NAME,METHOD_COLUMN_TOOLTIP,METHOD_FILTER_HINT,PACKAGE_COLUMN_NAME,PACKAGE_COLUMN_TOOLTIP,PACKAGE_FILTER_HINT,SAMPLES_COLUMN_NAME,SAMPLES_COLUMN_TOOLTIP,TIME_COLUMN_NAME,TIME_COLUMN_TOOLTIP,TIME_CPU_COLUMN_NAME,TIME_CPU_COLUMN_TOOLTIP,TIME_REL_COLUMN_NAME,TIME_REL_COLUMN_TOOLTIP,TREETABLE_ACCESS_NAME,abstractTreeTableModel,cornerButton,enhancedTreeCellRenderer,leafIcon,messages,minNamesColumnWidth,nodeIcon,selectionHandler,treeTableModel

CLSS public org.netbeans.lib.profiler.ui.cpu.CPUJavaNameRenderer
cons public init()
cons public init(java.lang.String)
meth public void setValue(java.lang.Object,int)
supr org.netbeans.lib.profiler.ui.swing.renderer.JavaNameRenderer
hfds LEAF_ICON,LEAF_ICON_DISABLED,THREAD_ICON,THREAD_ICON_DISABLED,icon,iconDisabled

CLSS public abstract interface org.netbeans.lib.profiler.ui.cpu.CPUResUserActionsHandler
innr public static Adapter
meth public abstract void addMethodToRoots(java.lang.String,java.lang.String,java.lang.String)
meth public abstract void find(java.lang.Object,java.lang.String)
meth public abstract void showReverseCallGraph(org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot,int,int,int,int,boolean)
meth public abstract void showSourceForMethod(java.lang.String,java.lang.String,java.lang.String)
meth public abstract void showSubtreeCallGraph(org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot,org.netbeans.lib.profiler.results.CCTNode,int,int,boolean)
meth public abstract void viewChanged(int)

CLSS public static org.netbeans.lib.profiler.ui.cpu.CPUResUserActionsHandler$Adapter
 outer org.netbeans.lib.profiler.ui.cpu.CPUResUserActionsHandler
cons public init()
intf org.netbeans.lib.profiler.ui.cpu.CPUResUserActionsHandler
meth public void addMethodToRoots(java.lang.String,java.lang.String,java.lang.String)
meth public void find(java.lang.Object,java.lang.String)
meth public void showReverseCallGraph(org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot,int,int,int,int,boolean)
meth public void showSourceForMethod(java.lang.String,java.lang.String,java.lang.String)
meth public void showSubtreeCallGraph(org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot,org.netbeans.lib.profiler.results.CCTNode,int,int,boolean)
meth public void viewChanged(int)
supr java.lang.Object

CLSS public abstract org.netbeans.lib.profiler.ui.cpu.CPUResultsPanel
cons public init(org.netbeans.lib.profiler.ui.cpu.CPUResUserActionsHandler,java.lang.Boolean)
fld protected boolean[] columnsVisibility
fld protected int columnCount
fld protected int currentView
fld protected int methodId
fld protected int[] columnWidths
fld protected java.lang.String[] columnNames
fld protected java.lang.String[] columnToolTips
fld protected javax.swing.JMenuItem popupAddToRoots
fld protected javax.swing.JMenuItem popupFind
fld protected javax.swing.JMenuItem popupShowReverse
fld protected javax.swing.JMenuItem popupShowSource
fld protected javax.swing.JMenuItem popupShowSubtree
fld protected javax.swing.JPopupMenu callGraphPopupMenu
fld protected javax.swing.JPopupMenu cornerPopup
fld protected javax.swing.table.TableCellRenderer[] columnRenderers
fld protected javax.swing.tree.TreePath popupPath
fld protected org.netbeans.lib.profiler.ui.cpu.CPUResUserActionsHandler actionsHandler
intf org.netbeans.lib.profiler.global.CommonConstants
meth protected abstract boolean supportsReverseCallGraph()
meth protected abstract boolean supportsSubtreeCallGraph()
meth protected abstract java.lang.String getSelectedMethodName()
meth protected abstract java.lang.String[] getMethodClassNameAndSig(int,int)
meth protected boolean isAddToRootsAvailable()
meth protected boolean isShowSourceAvailable()
meth protected javax.swing.JPopupMenu createPopupMenu()
meth protected void performDefaultAction()
meth protected void showReverseCallGraph(int,int,int,int,boolean)
meth protected void showSourceForMethod(int)
meth protected void showSourceForMethod(javax.swing.tree.TreePath)
meth protected void showSubtreeCallGraph(org.netbeans.lib.profiler.results.CCTNode,int,int,boolean)
meth public abstract int getCurrentThreadId()
meth public abstract void reset()
meth public boolean getSortingOrder()
meth public boolean[] getColumnsVisibility()
meth public int getCurrentView()
meth public int getSortingColumn()
meth public java.lang.Boolean isSampling()
meth public javax.swing.JMenuItem getPopupFindItem()
meth public void changeView(int)
meth public void setColumnsVisibility(boolean[])
supr org.netbeans.lib.profiler.ui.ResultsPanel
hfds BACKTRACES_ITEM_NAME,GO_TO_SOURCE_ITEM_NAME,ROOT_METHODS_ITEM_NAME,SUBTREE_ITEM_NAME,messages,sampling

CLSS public abstract interface org.netbeans.lib.profiler.ui.cpu.CPUSelectionHandler
meth public abstract void methodSelected(int,int,int)

CLSS public abstract org.netbeans.lib.profiler.ui.cpu.CPUView
cons public init()
supr org.netbeans.lib.profiler.ui.results.DataView
hfds ACTION_GOTOSOURCE,ACTION_PROFILE_CLASS,ACTION_PROFILE_METHOD,COLLAPSE_ALL_ITEM,COLLAPSE_CHILDREN_ITEM,COLUMN_HITS,COLUMN_INVOCATIONS,COLUMN_NAME,COLUMN_SELECTED,COLUMN_SELFTIME,COLUMN_SELFTIME_CPU,COLUMN_TOTALTIME,COLUMN_TOTALTIME_CPU,EXPAND_MENU,EXPAND_PLAIN_ITEM,EXPAND_TOPMOST_ITEM,EXPORT_FORWARD_CALLS,EXPORT_HOTSPOTS,EXPORT_METHODS,EXPORT_REVERSE_CALLS,EXPORT_TOOLTIP,FILTER_CALLEES_SCOPE,FILTER_CALLERS_SCOPE,FILTER_SCOPE_TOOLTIP,FIND_IN_FORWARDCALLS,FIND_IN_HOTSPOTS,FIND_IN_REVERSECALLS,HIDE_THREAD_ITEM,HITS_COLUMN_TOOLTIP,INVOCATIONS_COLUMN_TOOLTIP,NAME_COLUMN_TOOLTIP,SEARCH_CALLEES_SCOPE,SEARCH_CALLERS_SCOPE,SEARCH_SCOPE_TOOLTIP,SELECTED_COLUMN_TOOLTIP,SELF_TIME_COLUMN_TOOLTIP,SELF_TIME_CPU_COLUMN_TOOLTIP,SHOW_MENU,SHOW_THREAD_ITEM,TOTAL_TIME_COLUMN_TOOLTIP,TOTAL_TIME_CPU_COLUMN_TOOLTIP,messages

CLSS public org.netbeans.lib.profiler.ui.cpu.CodeRegionLivePanel
cons public init(org.netbeans.lib.profiler.ProfilerClient)
intf org.netbeans.lib.profiler.ui.LiveResultsPanel
meth public boolean fitsVisibleArea()
meth public boolean getSortingOrder()
meth public boolean hasView()
meth public boolean supports(int)
meth public int getSortingColumn()
meth public java.awt.image.BufferedImage getViewImage(boolean)
meth public java.lang.String getTitle()
meth public java.lang.String getViewName()
meth public void handleRemove()
meth public void handleShutdown()
meth public void reset()
meth public void updateLiveResults()
supr javax.swing.JPanel
hfds ALL_REMEMBERED_MSG,AREA_ACCESS_NAME,INDIVIDUAL_TIMES_MSG,INVOCATIONS_LISTED_MSG,LAST_REMEMBERED_MSG,NO_RESULTS_REGION_MSG,NO_RESULTS_TERMINATED_MSG,PANEL_NAME,SUMMARY_TIMES_MSG,TOTAL_INVOCATIONS_MSG,messages,profilerClient,resArea

CLSS public org.netbeans.lib.profiler.ui.cpu.CodeRegionSnapshotPanel
cons public init(org.netbeans.lib.profiler.results.coderegion.CodeRegionResultsSnapshot)
meth public java.lang.String getTitle()
meth public org.netbeans.lib.profiler.results.ResultsSnapshot getSnapshot()
supr javax.swing.JPanel
hfds ALL_REMEMBERED_MSG,AREA_ACCESS_NAME,INDIVIDUAL_TIMES_MSG,INVOCATIONS_LISTED_MSG,LAST_REMEMBERED_MSG,NO_RESULTS_REGION_MSG,PANEL_NAME,SUMMARY_TIMES_MSG,TOTAL_INVOCATIONS_MSG,messages,snapshot

CLSS public org.netbeans.lib.profiler.ui.cpu.CombinedPanel
cons public init(int,java.awt.Component,java.awt.Component)
intf org.netbeans.lib.profiler.ui.cpu.ScreenshotProvider
meth public boolean fitsVisibleArea()
meth public java.awt.image.BufferedImage getCurrentViewScreenshot(boolean)
meth public java.lang.String getDefaultViewName()
meth public void exportData(int,org.netbeans.lib.profiler.results.ExportDataDumper,java.lang.String)
supr javax.swing.JSplitPane

CLSS public org.netbeans.lib.profiler.ui.cpu.DiffCCTDisplay
cons public init(org.netbeans.lib.profiler.ui.cpu.CPUResUserActionsHandler,java.lang.Boolean)
meth protected boolean supportsReverseCallGraph()
meth protected boolean supportsSubtreeCallGraph()
meth protected java.lang.Float getNodeTimeRel(long,float)
meth protected java.lang.String getNodeInvocations(int)
meth protected java.lang.String getNodeSecondaryTime(long)
meth protected java.lang.String getNodeTime(long,float)
meth protected void initColumnsData()
meth public void prepareResults()
supr org.netbeans.lib.profiler.ui.cpu.CCTDisplay

CLSS public org.netbeans.lib.profiler.ui.cpu.DiffFlatProfilePanel
cons public init(org.netbeans.lib.profiler.ui.cpu.CPUResUserActionsHandler,java.lang.Boolean)
meth protected boolean supportsReverseCallGraph()
meth protected boolean supportsSubtreeCallGraph()
meth protected java.lang.Object computeValueAt(int,int)
meth protected void initColumnsData()
meth protected void obtainResults()
supr org.netbeans.lib.profiler.ui.cpu.SnapshotFlatProfilePanel

CLSS public abstract org.netbeans.lib.profiler.ui.cpu.FlatProfilePanel
cons public init(org.netbeans.lib.profiler.ui.cpu.CPUResUserActionsHandler,java.lang.Boolean)
cons public init(org.netbeans.lib.profiler.ui.cpu.CPUResUserActionsHandler,org.netbeans.lib.profiler.ui.cpu.CPUSelectionHandler,java.lang.Boolean)
fld protected boolean collectingTwoTimeStamps
fld protected boolean sortOrder
fld protected double valueFilterValue
fld protected int filterType
fld protected int sortBy
fld protected int threadId
fld protected java.lang.String filterString
fld protected javax.swing.JScrollPane jScrollPane
fld protected org.netbeans.lib.profiler.results.cpu.FlatProfileContainer flatProfileContainer
fld protected org.netbeans.lib.profiler.ui.components.FilterComponent filterComponent
fld protected org.netbeans.lib.profiler.ui.components.JExtendedTable resTable
fld protected org.netbeans.lib.profiler.ui.components.table.CustomBarCellRenderer barRenderer
fld protected org.netbeans.lib.profiler.ui.components.table.ExtendedTableModel resTableModel
meth protected abstract void obtainResults()
meth protected java.lang.Object computeValueAt(int,int)
meth protected java.lang.String getSelectedMethodName()
meth protected void initColumnSelectorItems()
meth protected void initColumnsData()
meth protected void prepareResults(boolean)
meth protected void saveColumnsData()
meth protected void updateResults()
meth public boolean findFirst()
meth public boolean findNext()
meth public boolean findPrevious()
meth public boolean getSortingOrder()
meth public boolean isFilterVisible()
meth public boolean isFindStringDefined()
meth public boolean[] getColumnsVisibility()
meth public int getCurrentThreadId()
meth public int getFilterType()
meth public int getSortBy(int)
meth public int getSortingColumn()
meth public java.lang.Object getResultsViewReference()
meth public java.lang.String getFilterValue()
meth public java.lang.String getFindString()
meth public void addFilterListener(javax.swing.event.ChangeListener)
meth public void addResultsViewFocusListener(java.awt.event.FocusListener)
meth public void clearSelection()
meth public void prepareResults()
meth public void removeFilterListener(javax.swing.event.ChangeListener)
meth public void removeResultsViewFocusListener(java.awt.event.FocusListener)
meth public void requestFocus()
meth public void reset()
meth public void selectMethod(int)
meth public void selectMethod(java.lang.String)
meth public void setCPUSelectionHandler(org.netbeans.lib.profiler.ui.cpu.CPUSelectionHandler)
meth public void setColumnsVisibility(boolean[])
meth public void setDefaultSorting()
meth public void setFilterValues(java.lang.String,int)
meth public void setFilterVisible(boolean)
meth public void setFindString(java.lang.String)
meth public void setResultsAvailable(boolean)
meth public void setSorting(int,boolean)
meth public void setSorting(int,boolean,boolean)
supr org.netbeans.lib.profiler.ui.cpu.CPUResultsPanel
hfds CLASS_COLUMN_NAME,CLASS_COLUMN_TOOLTIP,CLASS_FILTER_HINT,FILTER_ITEM_NAME,INVOCATIONS_COLUMN_NAME,INVOCATIONS_COLUMN_TOOLTIP,METHOD_COLUMN_NAME,METHOD_COLUMN_TOOLTIP,METHOD_FILTER_HINT,NO_RELEVANT_DATA,PACKAGE_COLUMN_NAME,PACKAGE_COLUMN_TOOLTIP,PACKAGE_FILTER_HINT,SAMPLES_COLUMN_NAME,SAMPLES_COLUMN_TOOLTIP,SELFTIME_COLUMN_NAME,SELFTIME_COLUMN_TOOLTIP,SELFTIME_CPU_COLUMN_NAME,SELFTIME_CPU_COLUMN_TOOLTIP,SELFTIME_REL_COLUMN_NAME,SELFTIME_REL_COLUMN_TOOLTIP,TABLE_ACCESS_NAME,TOTALTIME_COLUMN_NAME,TOTALTIME_COLUMN_TOOLTIP,TOTALTIME_CPU_COLUMN_NAME,TOTALTIME_CPU_COLUMN_TOOLTIP,messages,minNamesColumnWidth,noDataPanel,selectionHandler,sortingColumn

CLSS public abstract org.netbeans.lib.profiler.ui.cpu.LiveCPUView
cons public init(java.util.Set<org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection>)
meth protected abstract boolean showSourceSupported()
meth protected abstract void selectForProfiling(org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection)
meth protected abstract void showSource(org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection)
meth protected boolean profileClassSupported()
meth protected boolean profileMethodSupported()
meth protected void customizeNodePopup(org.netbeans.lib.profiler.ui.results.DataView,javax.swing.JPopupMenu,java.lang.Object,org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection)
meth protected void foundInForwardCalls()
meth protected void foundInHotSpots()
meth protected void foundInReverseCalls()
meth protected void popupHidden()
meth protected void popupShowing()
meth public boolean isRefreshRunning()
meth public boolean setDiffView(boolean)
meth public long getLastUpdate()
meth public org.netbeans.lib.profiler.ui.cpu.ThreadsSelector createThreadSelector()
meth public void refreshSelection()
meth public void resetData()
meth public void setData(org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot,boolean)
meth public void setView(boolean,boolean,boolean)
meth public void showSelectionColumn()
supr javax.swing.JPanel
hfds executor,forwardCallsView,hotSpotsView,lastFocused,lastupdate,mergedThreads,refSnapshot,refreshIsRunning,reverseCallsView,sampled,selectedThreads,snapshot,threadsSelector

CLSS public org.netbeans.lib.profiler.ui.cpu.LiveCPUViewUpdater
cons public init(org.netbeans.lib.profiler.ui.cpu.LiveCPUView,org.netbeans.lib.profiler.ProfilerClient)
innr public static CCTHandler
meth public void cleanup()
meth public void setForceRefresh(boolean)
meth public void setPaused(boolean)
meth public void update() throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
supr java.lang.Object
hfds MAX_UPDATE_DIFF,MIN_UPDATE_DIFF,client,cpuView,forceRefresh,handler,paused

CLSS public static org.netbeans.lib.profiler.ui.cpu.LiveCPUViewUpdater$CCTHandler
 outer org.netbeans.lib.profiler.ui.cpu.LiveCPUViewUpdater
cons public init()
intf org.netbeans.lib.profiler.results.cpu.CPUCCTProvider$Listener
meth public final void cctEstablished(org.netbeans.lib.profiler.results.RuntimeCCTNode,boolean)
meth public final void cctReset()
meth public static org.netbeans.lib.profiler.ui.cpu.LiveCPUViewUpdater$CCTHandler registerUpdater(org.netbeans.lib.profiler.ui.cpu.LiveCPUViewUpdater)
meth public void unregisterUpdater(org.netbeans.lib.profiler.ui.cpu.LiveCPUViewUpdater)
supr java.lang.Object
hfds updaters

CLSS public org.netbeans.lib.profiler.ui.cpu.LiveFlatProfileCollectorPanel
cons public init(org.netbeans.lib.profiler.TargetAppRunner,org.netbeans.lib.profiler.ui.cpu.CPUResUserActionsHandler,org.netbeans.lib.profiler.ui.cpu.CPUSelectionHandler,boolean)
intf org.netbeans.lib.profiler.ui.LiveResultsPanel
meth protected boolean supportsReverseCallGraph()
meth protected boolean supportsSubtreeCallGraph()
meth protected java.lang.String[] getMethodClassNameAndSig(int,int)
meth protected javax.swing.JPopupMenu createPopupMenu()
meth protected void obtainResults()
meth protected void showReverseCallGraph(int,int,int,int,boolean)
meth public boolean fitsVisibleArea()
meth public boolean hasData()
meth public boolean hasView()
meth public boolean supports(int)
meth public java.awt.image.BufferedImage getViewImage(boolean)
meth public java.lang.String getViewName()
meth public org.netbeans.lib.profiler.results.cpu.FlatProfileProvider getFlatProfileProvider()
meth public void exportData(int,org.netbeans.lib.profiler.results.ExportDataDumper,java.lang.String)
meth public void handleRemove()
meth public void handleShutdown()
meth public void prepareResults()
meth public void setDataToDisplay(org.netbeans.lib.profiler.results.cpu.FlatProfileContainer)
meth public void updateLiveResults()
supr org.netbeans.lib.profiler.ui.cpu.FlatProfilePanel
hfds firstTime,popup,runner,updateResultsInProgress,updateResultsPending

CLSS public org.netbeans.lib.profiler.ui.cpu.LiveFlatProfilePanel
cons public init(org.netbeans.lib.profiler.TargetAppRunner,org.netbeans.lib.profiler.ui.cpu.CPUResUserActionsHandler,boolean)
cons public init(org.netbeans.lib.profiler.TargetAppRunner,org.netbeans.lib.profiler.ui.cpu.CPUResUserActionsHandler,java.util.Collection,boolean)
intf org.netbeans.lib.profiler.ui.LiveResultsPanel
meth public boolean fitsVisibleArea()
meth public boolean getSortingOrder()
meth public boolean hasValidDrillDown()
meth public boolean hasView()
meth public boolean supports(int)
meth public int getSortingColumn()
meth public java.awt.image.BufferedImage getViewImage(boolean)
meth public java.lang.String getViewName()
meth public void exportData(int,org.netbeans.lib.profiler.results.ExportDataDumper,java.lang.String)
meth public void handleRemove()
meth public void handleShutdown()
meth public void reset()
meth public void setAdditionalStats(java.util.Collection<org.netbeans.lib.profiler.ui.cpu.statistics.StatisticalModule>)
meth public void updateLiveResults()
supr javax.swing.JPanel
hfds NO_RESULTS_STRING,actionsHandler,fpCollectorPanel,handler,messages,noResultsPanel,resultsTable,runner,statModules

CLSS public org.netbeans.lib.profiler.ui.cpu.ReverseCallGraphPanel
cons public init(org.netbeans.lib.profiler.ui.cpu.CPUResUserActionsHandler,java.lang.Boolean)
fld protected boolean sortOrder
fld protected int selectedMethodId
fld protected int sortingColumn
fld protected int threadId
fld protected javax.swing.JButton cornerButton
fld protected org.netbeans.lib.profiler.ui.components.FilterComponent filterComponent
fld protected org.netbeans.lib.profiler.ui.components.JTreeTable treeTable
fld protected org.netbeans.lib.profiler.ui.components.treetable.JTreeTablePanel treeTablePanel
intf org.netbeans.lib.profiler.ui.cpu.ScreenshotProvider
meth protected javax.swing.JPopupMenu createPopupMenu()
meth protected void initColumnSelectorItems()
meth public boolean findFirst()
meth public boolean findNext()
meth public boolean findPrevious()
meth public boolean fitsVisibleArea()
meth public boolean getSortingOrder()
meth public boolean isFindStringDefined()
meth public int getCurrentThreadId()
meth public int getSelectedMethodId()
meth public int getSortingColumn()
meth public java.awt.image.BufferedImage getCurrentViewScreenshot(boolean)
meth public java.lang.String getDefaultViewName()
meth public java.lang.String getFindString()
meth public java.lang.String getSelectedMethodName()
meth public java.lang.String getSelectedMethodNameShort()
meth public java.lang.String getShortTitle()
meth public java.lang.String getTitle()
meth public void exportData(int,org.netbeans.lib.profiler.results.ExportDataDumper,java.lang.String)
meth public void prepareResults()
meth public void requestFocus()
meth public void reset()
meth public void setDataToDisplay(org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot,int,int)
meth public void setDefaultSorting()
meth public void setFindString(java.lang.String)
meth public void setSelectedMethodId(int)
meth public void setSorting(int,boolean)
supr org.netbeans.lib.profiler.ui.cpu.SnapshotCPUResultsPanel
hfds ADD_ROOT_METHOD_POPUP_ITEM,CLASS_COLUMN_NAME,CLASS_COLUMN_TOOLTIP,CLASS_FILTER_HINT,FILTER_ITEM_NAME,GO_TO_SOURCE_POPUP_ITEM,INVOCATIONS_COLUMN_NAME,INVOCATIONS_COLUMN_TOOLTIP,METHOD_COLUMN_NAME,METHOD_COLUMN_TOOLTIP,METHOD_FILTER_HINT,PACKAGE_COLUMN_NAME,PACKAGE_COLUMN_TOOLTIP,PACKAGE_FILTER_HINT,PANEL_TITLE,PANEL_TITLE_SHORT,SAMPLES_COLUMN_NAME,SAMPLES_COLUMN_TOOLTIP,TIME_COLUMN_NAME,TIME_COLUMN_TOOLTIP,TIME_CPU_COLUMN_NAME,TIME_CPU_COLUMN_TOOLTIP,TIME_REL_COLUMN_NAME,TIME_REL_COLUMN_TOOLTIP,TREETABLE_ACCESS_NAME,abstractTreeTableModel,enhancedTreeCellRenderer,leafIcon,messages,minNamesColumnWidth,nodeIcon,treeTableModel

CLSS public abstract interface org.netbeans.lib.profiler.ui.cpu.ScreenshotProvider
meth public abstract boolean fitsVisibleArea()
meth public abstract java.awt.image.BufferedImage getCurrentViewScreenshot(boolean)
meth public abstract java.lang.String getDefaultViewName()

CLSS public abstract org.netbeans.lib.profiler.ui.cpu.SnapshotCPUResultsPanel
cons public init(org.netbeans.lib.profiler.ui.cpu.CPUResUserActionsHandler,java.lang.Boolean)
fld protected org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot snapshot
intf org.netbeans.lib.profiler.global.CommonConstants
intf org.netbeans.lib.profiler.ui.cpu.ScreenshotProvider
meth protected boolean isCloseable()
meth protected boolean supportsReverseCallGraph()
meth protected boolean supportsSubtreeCallGraph()
meth protected java.lang.String[] getMethodClassNameAndSig(int,int)
meth protected void showReverseCallGraph(int,int,int,int,boolean)
meth protected void showSubtreeCallGraph(org.netbeans.lib.profiler.results.CCTNode,int,int,boolean)
meth public org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot getSnapshot()
meth public void setDataToDisplay(org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot,int)
supr org.netbeans.lib.profiler.ui.cpu.CPUResultsPanel

CLSS public abstract org.netbeans.lib.profiler.ui.cpu.SnapshotCPUView
cons public init(org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot,boolean,javax.swing.Action,javax.swing.Action,javax.swing.Action,org.netbeans.lib.profiler.ui.swing.ExportUtils$Exportable)
meth protected abstract boolean showSourceSupported()
meth protected abstract void selectForProfiling(org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection)
meth protected abstract void showSource(org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection)
meth protected boolean profileClassSupported()
meth protected boolean profileMethodEnabled()
meth protected boolean profileMethodSupported()
meth protected final void setSnapshot(org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot,boolean)
meth protected void customizeNodePopup(org.netbeans.lib.profiler.ui.results.DataView,javax.swing.JPopupMenu,java.lang.Object,org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection)
meth protected void foundInForwardCalls()
meth protected void foundInHotSpots()
meth protected void foundInReverseCalls()
meth public void setRefSnapshot(org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot)
supr javax.swing.JPanel
hfds AGGREGATION_CLASSES,AGGREGATION_METHODS,AGGREGATION_PACKAGES,COMPARE_SNAPSHOTS,RESET_COMPARE_SNAPSHOTS,TOOLBAR_AGGREGATION,TOOLBAR_VIEW,VIEW_FORWARD,VIEW_HOTSPOTS,VIEW_REVERSE,aggregation,compareButton,executor,forwardCallsView,hotSpotsView,lastFocused,mergedThreads,messages,refSnapshot,reverseCallsView,sampled,selectedThreads,snapshot,threadsSelector,toggles

CLSS public org.netbeans.lib.profiler.ui.cpu.SnapshotFlatProfilePanel
cons public init(org.netbeans.lib.profiler.ui.cpu.CPUResUserActionsHandler,java.lang.Boolean)
cons public init(org.netbeans.lib.profiler.ui.cpu.CPUResUserActionsHandler,org.netbeans.lib.profiler.ui.cpu.CPUSelectionHandler,java.lang.Boolean)
intf org.netbeans.lib.profiler.ui.cpu.ScreenshotProvider
meth protected boolean supportsReverseCallGraph()
meth protected boolean supportsSubtreeCallGraph()
meth protected java.lang.String[] getMethodClassNameAndSig(int,int)
meth protected void initDataUponResultsFetch()
meth protected void obtainResults()
meth protected void showReverseCallGraph(int,int,int,int,boolean)
meth protected void updateResults()
meth public boolean fitsVisibleArea()
meth public java.awt.image.BufferedImage getCurrentViewScreenshot(boolean)
meth public java.lang.String getDefaultViewName()
meth public org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot getSnapshot()
meth public void exportData(int,org.netbeans.lib.profiler.results.ExportDataDumper,boolean,java.lang.String)
meth public void setDataToDisplay(org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot,int,int)
supr org.netbeans.lib.profiler.ui.cpu.FlatProfilePanel
hfds snapshot

CLSS public org.netbeans.lib.profiler.ui.cpu.StatisticsPanel
cons public init(org.netbeans.lib.profiler.ui.components.HTMLTextArea,org.netbeans.lib.profiler.ui.charts.PieChart,java.lang.Runnable)
innr public abstract interface static Listener
meth public void addListener(org.netbeans.lib.profiler.ui.cpu.StatisticsPanel$Listener)
meth public void addSnippet(javax.swing.JComponent)
meth public void removeListener(org.netbeans.lib.profiler.ui.cpu.StatisticsPanel$Listener)
meth public void removeSnippet(javax.swing.JComponent)
supr javax.swing.JPanel
hfds SCOPE_LABEL_TEXT,container,itemPresenters,listeners,messages,navPanel,navigationBackPerformer,noSnippetsBottomFiller,pieChart,snippets,snippetsBottomFiller
hcls ChartItemPresenter,ChartPanel,ColorIcon,Container,KeyboardAwareLabel,NavPanel

CLSS public abstract interface static org.netbeans.lib.profiler.ui.cpu.StatisticsPanel$Listener
 outer org.netbeans.lib.profiler.ui.cpu.StatisticsPanel
meth public abstract void itemClicked(int)

CLSS public org.netbeans.lib.profiler.ui.cpu.SubtreeCallGraphPanel
cons public init(org.netbeans.lib.profiler.ui.cpu.CPUResUserActionsHandler,java.lang.Boolean)
fld protected boolean sortOrder
fld protected int sortingColumn
fld protected javax.swing.JButton cornerButton
fld protected org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode rootNode
fld protected org.netbeans.lib.profiler.ui.components.FilterComponent filterComponent
fld protected org.netbeans.lib.profiler.ui.components.JTreeTable treeTable
fld protected org.netbeans.lib.profiler.ui.components.treetable.JTreeTablePanel treeTablePanel
intf org.netbeans.lib.profiler.ui.cpu.ScreenshotProvider
meth protected boolean isCloseable()
meth protected void initColumnSelectorItems()
meth public boolean findFirst()
meth public boolean findNext()
meth public boolean findPrevious()
meth public boolean fitsVisibleArea()
meth public boolean getSortingOrder()
meth public boolean isFindStringDefined()
meth public int getCurrentThreadId()
meth public int getSortingColumn()
meth public java.awt.image.BufferedImage getCurrentViewScreenshot(boolean)
meth public java.lang.String getDefaultViewName()
meth public java.lang.String getFindString()
meth public java.lang.String getSelectedMethodName()
meth public java.lang.String getSelectedMethodNameShort()
meth public java.lang.String getSelectedThreadName()
meth public java.lang.String getShortTitle()
meth public java.lang.String getTitle()
meth public void exportData(int,org.netbeans.lib.profiler.results.ExportDataDumper,java.lang.String)
meth public void prepareResults()
meth public void requestFocus()
meth public void reset()
meth public void setDataToDisplay(org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot,org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode,int)
meth public void setDefaultSorting()
meth public void setFindString(java.lang.String)
meth public void setSorting(int,boolean)
supr org.netbeans.lib.profiler.ui.cpu.SnapshotCPUResultsPanel
hfds CLASS_COLUMN_NAME,CLASS_COLUMN_TOOLTIP,CLASS_FILTER_HINT,FILTER_ITEM_NAME,INVOCATIONS_COLUMN_NAME,INVOCATIONS_COLUMN_TOOLTIP,METHOD_COLUMN_NAME,METHOD_COLUMN_TOOLTIP,METHOD_FILTER_HINT,PACKAGE_COLUMN_NAME,PACKAGE_COLUMN_TOOLTIP,PACKAGE_FILTER_HINT,PANEL_TITLE,PANEL_TITLE_SHORT,SAMPLES_COLUMN_NAME,SAMPLES_COLUMN_TOOLTIP,TIME_COLUMN_NAME,TIME_COLUMN_TOOLTIP,TIME_CPU_COLUMN_NAME,TIME_CPU_COLUMN_TOOLTIP,TIME_REL_COLUMN_NAME,TIME_REL_COLUMN_TOOLTIP,TREETABLE_ACCESS_NAME,abstractTreeTableModel,enhancedTreeCellRenderer,leafIcon,messages,minNamesColumnWidth,nodeIcon,treeTableModel

CLSS public abstract org.netbeans.lib.profiler.ui.cpu.ThreadsSelector
cons public init()
meth protected abstract org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot getSnapshot()
meth protected abstract void selectionChanged(java.util.Collection<java.lang.Integer>,boolean)
meth protected void displayPopup()
meth public java.lang.String getToolTipText()
supr org.netbeans.lib.profiler.ui.swing.PopupButton
hfds ALL_THREADS,ALL_THREADS_TOOLTIP,COLUMN_SELECTED,COLUMN_SELECTED_TOOLTIP,COLUMN_THREAD,COLUMN_THREAD_TOOLTIP,FILTER_THREADS,MERGE_THREADS,MERGE_THREADS_TOOLTIP,MERGE_THREADS_TOOLTIP_DISABLED,NO_THREADS,SELECTED_THREADS,SELECTED_THREADS_ALL,SELECT_THREADS,allThreadsResetter,displayAllThreads,mergeSelectedThreads,messages,selection
hcls SelectedThreadsModel

CLSS public abstract org.netbeans.lib.profiler.ui.cpu.statistics.StatisticalModule
cons public init()
meth protected final int getSelectedMethodId()
meth protected final org.netbeans.lib.profiler.marker.Mark getSelectedMark()
meth protected void onMarkSelectionChange(org.netbeans.lib.profiler.marker.Mark,org.netbeans.lib.profiler.marker.Mark)
meth protected void onMethodSelectionChange(int,int)
meth public abstract void refresh(org.netbeans.lib.profiler.results.cpu.cct.nodes.RuntimeCPUCCTNode)
meth public final void setSelectedMark(org.netbeans.lib.profiler.marker.Mark)
meth public final void setSelectedMethodId(int)
supr javax.swing.JPanel
hfds mId,mark

CLSS public org.netbeans.lib.profiler.ui.cpu.statistics.StatisticalModuleContainer
cons public init()
intf org.netbeans.lib.profiler.results.cpu.CPUCCTProvider$Listener
meth public java.util.Collection getAllModules()
meth public void addModule(org.netbeans.lib.profiler.ui.cpu.statistics.StatisticalModule)
meth public void cctEstablished(org.netbeans.lib.profiler.results.RuntimeCCTNode,boolean)
meth public void cctReset()
meth public void removeAllModules()
meth public void removeModule(org.netbeans.lib.profiler.ui.cpu.statistics.StatisticalModule)
supr java.lang.Object
hfds modules

CLSS public org.netbeans.lib.profiler.ui.cpu.statistics.TimingData
cons public init()
meth public int getIncInv()
meth public int getOutInv()
meth public long getTime0Acc()
meth public long getTime1Acc()
meth public void addIncomming(int)
meth public void addOutgoing(int)
meth public void addTime0(long)
meth public void addTime1(long)
meth public void incrementIncomming()
meth public void incrementOutgoing()
supr java.lang.Object
hfds incInv,outInv,time0Acc,time1Acc

CLSS public final org.netbeans.lib.profiler.ui.graphs.AllocationsHistoryGraphPanel
meth protected org.netbeans.lib.profiler.ui.charts.xy.ProfilerXYTooltipModel createTooltipModel()
meth public javax.swing.Action[] getActions()
meth public static org.netbeans.lib.profiler.ui.graphs.AllocationsHistoryGraphPanel createPanel(org.netbeans.lib.profiler.ui.memory.ClassHistoryModels)
meth public void cleanup()
supr org.netbeans.lib.profiler.ui.graphs.GraphPanel
hfds chart,chartActions,models

CLSS public final org.netbeans.lib.profiler.ui.graphs.CPUGraphPanel
meth protected org.netbeans.lib.profiler.ui.charts.xy.ProfilerXYTooltipModel createTooltipModel()
meth public javax.swing.Action[] getActions()
meth public static org.netbeans.lib.profiler.ui.graphs.CPUGraphPanel createBigPanel(org.netbeans.lib.profiler.ui.monitor.VMTelemetryModels)
meth public static org.netbeans.lib.profiler.ui.graphs.CPUGraphPanel createSmallPanel(org.netbeans.lib.profiler.ui.monitor.VMTelemetryModels,javax.swing.Action)
meth public void cleanup()
supr org.netbeans.lib.profiler.ui.graphs.GraphPanel
hfds CPU_COLOR,GC_COLOR,chart,chartActions,listener,models,smallPanel

CLSS public abstract org.netbeans.lib.profiler.ui.graphs.GraphPanel
cons public init()
fld protected final static double INITIAL_CHART_SCALEX = 0.02
fld protected final static java.text.DateFormat DATE_FORMATTER
fld protected final static java.text.NumberFormat INT_FORMATTER
fld protected final static java.text.NumberFormat PERCENT_FORMATTER
fld protected final static long SMALL_CHART_FIT_TO_WINDOW_PERIOD = 180000
meth protected abstract org.netbeans.lib.profiler.ui.charts.xy.ProfilerXYTooltipModel createTooltipModel()
meth protected java.lang.String getMaxValueString(java.lang.String)
meth protected org.netbeans.lib.profiler.ui.charts.xy.ProfilerXYChart createChart(org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYItemsModel,org.netbeans.lib.profiler.charts.PaintersModel,boolean)
meth protected org.netbeans.lib.profiler.ui.charts.xy.ProfilerXYTooltipModel getTooltipModel()
meth public abstract javax.swing.Action[] getActions()
meth public abstract void cleanup()
supr javax.swing.JPanel
hfds MAX_VALUE_STRING,NO_DATA_TOOLTIP,lastTooltip,messages,smallTooltipManager,tooltipModel
hcls SmallTooltip,SmallTooltipManager

CLSS public abstract interface org.netbeans.lib.profiler.ui.graphs.GraphsUI
fld public final static float A_ALLOC_BYTES_MARKER_LINE1_WIDTH = 0.75
fld public final static float A_ALLOC_BYTES_MARKER_LINE2_WIDTH = 3.5
fld public final static float A_ALLOC_BYTES_PAINTER_LINE_WIDTH = 2.0
fld public final static float A_ALLOC_OBJECTS_MARKER_LINE1_WIDTH = 0.75
fld public final static float A_ALLOC_OBJECTS_MARKER_LINE2_WIDTH = 3.5
fld public final static float A_ALLOC_OBJECTS_PAINTER_LINE_WIDTH = 2.0
fld public final static float GC_TIME_MARKER_LINE1_WIDTH = 0.75
fld public final static float GC_TIME_MARKER_LINE2_WIDTH = 3.5
fld public final static float GC_TIME_PAINTER_LINE_WIDTH = 2.0
fld public final static float HEAP_SIZE_MARKER_LINE1_WIDTH = 0.75
fld public final static float HEAP_SIZE_MARKER_LINE2_WIDTH = 3.5
fld public final static float HEAP_SIZE_PAINTER_LINE_WIDTH = 2.0
fld public final static float LOADED_CLASSES_MARKER_LINE1_WIDTH = 0.75
fld public final static float LOADED_CLASSES_MARKER_LINE2_WIDTH = 3.5
fld public final static float LOADED_CLASSES_PAINTER_LINE_WIDTH = 2.0
fld public final static float L_ALLOC_OBJECTS_MARKER_LINE1_WIDTH = 0.75
fld public final static float L_ALLOC_OBJECTS_MARKER_LINE2_WIDTH = 3.5
fld public final static float L_ALLOC_OBJECTS_PAINTER_LINE_WIDTH = 2.0
fld public final static float L_LIVE_BYTES_MARKER_LINE1_WIDTH = 0.75
fld public final static float L_LIVE_BYTES_MARKER_LINE2_WIDTH = 3.5
fld public final static float L_LIVE_BYTES_PAINTER_LINE_WIDTH = 2.0
fld public final static float L_LIVE_OBJECTS_MARKER_LINE1_WIDTH = 0.75
fld public final static float L_LIVE_OBJECTS_MARKER_LINE2_WIDTH = 3.5
fld public final static float L_LIVE_OBJECTS_PAINTER_LINE_WIDTH = 2.0
fld public final static float SURVGEN_MARKER_LINE1_WIDTH = 0.75
fld public final static float SURVGEN_MARKER_LINE2_WIDTH = 3.5
fld public final static float SURVGEN_PAINTER_LINE_WIDTH = 2.0
fld public final static float THREADS_MARKER_LINE1_WIDTH = 0.75
fld public final static float THREADS_MARKER_LINE2_WIDTH = 3.5
fld public final static float THREADS_PAINTER_LINE_WIDTH = 2.0
fld public final static float TOOLTIP_OVERLAY_LINE_WIDTH = 2.1
fld public final static float USED_HEAP_MARKER_LINE1_WIDTH = 0.75
fld public final static float USED_HEAP_MARKER_LINE2_WIDTH = 3.5
fld public final static float USED_HEAP_PAINTER_LINE_WIDTH = 2.0
fld public final static int A_ALLOC_BYTES_MARKER_RADIUS = 5
fld public final static int A_ALLOC_OBJECTS_MARKER_RADIUS = 5
fld public final static int GC_TIME_MARKER_RADIUS = 5
fld public final static int HEAP_SIZE_MARKER_RADIUS = 5
fld public final static int LOADED_CLASSES_MARKER_RADIUS = 5
fld public final static int L_ALLOC_OBJECTS_MARKER_RADIUS = 5
fld public final static int L_LIVE_BYTES_MARKER_RADIUS = 5
fld public final static int L_LIVE_OBJECTS_MARKER_RADIUS = 5
fld public final static int SURVGEN_MARKER_RADIUS = 5
fld public final static int THREADS_MARKER_RADIUS = 5
fld public final static int USED_HEAP_MARKER_RADIUS = 5
fld public final static java.awt.Color A_ALLOC_BYTES_MARKER_FILL_COLOR
fld public final static java.awt.Color A_ALLOC_BYTES_MARKER_LINE1_COLOR
fld public final static java.awt.Color A_ALLOC_BYTES_MARKER_LINE2_COLOR
fld public final static java.awt.Color A_ALLOC_BYTES_PAINTER_FILL_COLOR
fld public final static java.awt.Color A_ALLOC_BYTES_PAINTER_LINE_COLOR
fld public final static java.awt.Color A_ALLOC_OBJECTS_MARKER_FILL_COLOR
fld public final static java.awt.Color A_ALLOC_OBJECTS_MARKER_LINE1_COLOR
fld public final static java.awt.Color A_ALLOC_OBJECTS_MARKER_LINE2_COLOR
fld public final static java.awt.Color A_ALLOC_OBJECTS_PAINTER_FILL_COLOR
fld public final static java.awt.Color A_ALLOC_OBJECTS_PAINTER_LINE_COLOR
fld public final static java.awt.Color CHART_BACKGROUND_COLOR
fld public final static java.awt.Color GC_ACTIVITY_FILL_COLOR
fld public final static java.awt.Color GC_TIME_MARKER_FILL_COLOR
fld public final static java.awt.Color GC_TIME_MARKER_LINE1_COLOR
fld public final static java.awt.Color GC_TIME_MARKER_LINE2_COLOR
fld public final static java.awt.Color GC_TIME_PAINTER_FILL_COLOR
fld public final static java.awt.Color GC_TIME_PAINTER_LINE_COLOR
fld public final static java.awt.Color HEAP_LIMIT_FILL_COLOR
fld public final static java.awt.Color HEAP_SIZE_MARKER_FILL_COLOR
fld public final static java.awt.Color HEAP_SIZE_MARKER_LINE1_COLOR
fld public final static java.awt.Color HEAP_SIZE_MARKER_LINE2_COLOR
fld public final static java.awt.Color HEAP_SIZE_PAINTER_FILL_COLOR
fld public final static java.awt.Color HEAP_SIZE_PAINTER_LINE_COLOR
fld public final static java.awt.Color LOADED_CLASSES_MARKER_FILL_COLOR
fld public final static java.awt.Color LOADED_CLASSES_MARKER_LINE1_COLOR
fld public final static java.awt.Color LOADED_CLASSES_MARKER_LINE2_COLOR
fld public final static java.awt.Color LOADED_CLASSES_PAINTER_FILL_COLOR
fld public final static java.awt.Color LOADED_CLASSES_PAINTER_LINE_COLOR
fld public final static java.awt.Color L_ALLOC_OBJECTS_MARKER_FILL_COLOR
fld public final static java.awt.Color L_ALLOC_OBJECTS_MARKER_LINE1_COLOR
fld public final static java.awt.Color L_ALLOC_OBJECTS_MARKER_LINE2_COLOR
fld public final static java.awt.Color L_ALLOC_OBJECTS_PAINTER_FILL_COLOR
fld public final static java.awt.Color L_ALLOC_OBJECTS_PAINTER_LINE_COLOR
fld public final static java.awt.Color L_LIVE_BYTES_MARKER_FILL_COLOR
fld public final static java.awt.Color L_LIVE_BYTES_MARKER_LINE1_COLOR
fld public final static java.awt.Color L_LIVE_BYTES_MARKER_LINE2_COLOR
fld public final static java.awt.Color L_LIVE_BYTES_PAINTER_FILL_COLOR
fld public final static java.awt.Color L_LIVE_BYTES_PAINTER_LINE_COLOR
fld public final static java.awt.Color L_LIVE_OBJECTS_MARKER_FILL_COLOR
fld public final static java.awt.Color L_LIVE_OBJECTS_MARKER_LINE1_COLOR
fld public final static java.awt.Color L_LIVE_OBJECTS_MARKER_LINE2_COLOR
fld public final static java.awt.Color L_LIVE_OBJECTS_PAINTER_FILL_COLOR
fld public final static java.awt.Color L_LIVE_OBJECTS_PAINTER_LINE_COLOR
fld public final static java.awt.Color PROFILER_BLUE
fld public final static java.awt.Color PROFILER_GREEN
fld public final static java.awt.Color PROFILER_RED
fld public final static java.awt.Color SMALL_LEGEND_BACKGROUND_COLOR
fld public final static java.awt.Color SMALL_LEGEND_BORDER_COLOR
fld public final static java.awt.Color SURVGEN_MARKER_FILL_COLOR
fld public final static java.awt.Color SURVGEN_MARKER_LINE1_COLOR
fld public final static java.awt.Color SURVGEN_MARKER_LINE2_COLOR
fld public final static java.awt.Color SURVGEN_PAINTER_FILL_COLOR
fld public final static java.awt.Color SURVGEN_PAINTER_LINE_COLOR
fld public final static java.awt.Color THREADS_MARKER_FILL_COLOR
fld public final static java.awt.Color THREADS_MARKER_LINE1_COLOR
fld public final static java.awt.Color THREADS_MARKER_LINE2_COLOR
fld public final static java.awt.Color THREADS_PAINTER_FILL_COLOR
fld public final static java.awt.Color THREADS_PAINTER_LINE_COLOR
fld public final static java.awt.Color TOOLTIP_OVERLAY_FILL_COLOR
fld public final static java.awt.Color TOOLTIP_OVERLAY_LINE_COLOR
fld public final static java.awt.Color USED_HEAP_MARKER_FILL_COLOR
fld public final static java.awt.Color USED_HEAP_MARKER_LINE1_COLOR
fld public final static java.awt.Color USED_HEAP_MARKER_LINE2_COLOR
fld public final static java.awt.Color USED_HEAP_PAINTER_FILL_COLOR
fld public final static java.awt.Color USED_HEAP_PAINTER_LINE_COLOR
fld public final static java.lang.String A_ALLOC_BYTES_NAME
fld public final static java.lang.String A_ALLOC_OBJECTS_NAME
fld public final static java.lang.String CPU_GC_CAPTION
fld public final static java.lang.String CPU_TIME_NAME
fld public final static java.lang.String GC_CAPTION
fld public final static java.lang.String GC_INTERVALS_NAME
fld public final static java.lang.String GC_TIME_NAME
fld public final static java.lang.String HEAP_SIZE_NAME
fld public final static java.lang.String LOADED_CLASSES_NAME
fld public final static java.lang.String L_ALLOC_OBJECTS_NAME
fld public final static java.lang.String L_LIVE_BYTES_NAME
fld public final static java.lang.String L_LIVE_OBJECTS_NAME
fld public final static java.lang.String MEMORY_CAPTION
fld public final static java.lang.String SURVGEN_NAME
fld public final static java.lang.String THREADS_CLASSES_CAPTION
fld public final static java.lang.String THREADS_NAME
fld public final static java.lang.String USED_HEAP_NAME
fld public final static java.util.ResourceBundle messages
fld public final static long A_ALLOC_BYTES_INITIAL_VALUE = 102400
fld public final static long A_ALLOC_OBJECTS_INITIAL_VALUE = 100
fld public final static long HEAP_SIZE_INITIAL_VALUE = 67108864
fld public final static long LOADED_CLASSES_INITIAL_VALUE = 732
fld public final static long L_ALLOC_OBJECTS_INITIAL_VALUE = 100
fld public final static long L_LIVE_BYTES_INITIAL_VALUE = 102400
fld public final static long L_LIVE_OBJECTS_INITIAL_VALUE = 100
fld public final static long SURVGEN_INITIAL_VALUE = 11
fld public final static long THREADS_INITIAL_VALUE = 3
fld public final static long USED_HEAP_INITIAL_VALUE = 16777216

CLSS public final org.netbeans.lib.profiler.ui.graphs.LivenessHistoryGraphPanel
meth protected org.netbeans.lib.profiler.ui.charts.xy.ProfilerXYTooltipModel createTooltipModel()
meth public javax.swing.Action[] getActions()
meth public static org.netbeans.lib.profiler.ui.graphs.LivenessHistoryGraphPanel createPanel(org.netbeans.lib.profiler.ui.memory.ClassHistoryModels)
meth public void cleanup()
supr org.netbeans.lib.profiler.ui.graphs.GraphPanel
hfds chart,chartActions,models

CLSS public final org.netbeans.lib.profiler.ui.graphs.MemoryGraphPanel
meth protected org.netbeans.lib.profiler.ui.charts.xy.ProfilerXYTooltipModel createTooltipModel()
meth public javax.swing.Action[] getActions()
meth public static org.netbeans.lib.profiler.ui.graphs.MemoryGraphPanel createBigPanel(org.netbeans.lib.profiler.ui.monitor.VMTelemetryModels)
meth public static org.netbeans.lib.profiler.ui.graphs.MemoryGraphPanel createSmallPanel(org.netbeans.lib.profiler.ui.monitor.VMTelemetryModels,javax.swing.Action)
meth public void cleanup()
supr org.netbeans.lib.profiler.ui.graphs.GraphPanel
hfds SIZE_COLOR,SIZE_GRADIENT,USED_COLOR,USED_GRADIENT,chart,chartActions,listener,models,smallPanel

CLSS public final org.netbeans.lib.profiler.ui.graphs.SurvivingGenerationsGraphPanel
meth protected org.netbeans.lib.profiler.ui.charts.xy.ProfilerXYTooltipModel createTooltipModel()
meth public javax.swing.Action[] getActions()
meth public static org.netbeans.lib.profiler.ui.graphs.SurvivingGenerationsGraphPanel createBigPanel(org.netbeans.lib.profiler.ui.monitor.VMTelemetryModels)
meth public static org.netbeans.lib.profiler.ui.graphs.SurvivingGenerationsGraphPanel createSmallPanel(org.netbeans.lib.profiler.ui.monitor.VMTelemetryModels,javax.swing.Action)
meth public void cleanup()
supr org.netbeans.lib.profiler.ui.graphs.GraphPanel
hfds GC_INTERVALS_COLOR,GENERATIONS_COLOR,chart,chartActions,listener,models,smallPanel

CLSS public final org.netbeans.lib.profiler.ui.graphs.ThreadsGraphPanel
meth protected org.netbeans.lib.profiler.ui.charts.xy.ProfilerXYTooltipModel createTooltipModel()
meth public javax.swing.Action[] getActions()
meth public static org.netbeans.lib.profiler.ui.graphs.ThreadsGraphPanel createBigPanel(org.netbeans.lib.profiler.ui.monitor.VMTelemetryModels)
meth public static org.netbeans.lib.profiler.ui.graphs.ThreadsGraphPanel createSmallPanel(org.netbeans.lib.profiler.ui.monitor.VMTelemetryModels,javax.swing.Action)
meth public void cleanup()
supr org.netbeans.lib.profiler.ui.graphs.GraphPanel
hfds CLASSES_COLOR,THREADS_COLOR,chart,chartActions,listener,models,smallPanel

CLSS public abstract org.netbeans.lib.profiler.ui.jdbc.JDBCView
cons public init()
supr org.netbeans.lib.profiler.ui.results.DataView
hfds ACTION_GOTOSOURCE,ACTION_PROFILE_CLASS,ACTION_PROFILE_METHOD,ACTION_VIEWSQLQUERY,COLLAPSE_ALL_ITEM,COLLAPSE_CHILDREN_ITEM,COLUMN_COMMANDS,COLUMN_INVOCATIONS,COLUMN_NAME,COLUMN_STATEMENTS,COLUMN_TABLES,COLUMN_TOTALTIME,COMMANDS_COLUMN_TOOLTIP,EXPAND_MENU,EXPAND_PLAIN_ITEM,EXPAND_TOPMOST_ITEM,EXPORT_LBL,EXPORT_QUERIES,EXPORT_TOOLTIP,INVOCATIONS_COLUMN_TOOLTIP,NAME_COLUMN_TOOLTIP,OTHER_COMMAND,SEARCH_CALLERS_SCOPE,SEARCH_QUERIES_SCOPE,SEARCH_SCOPE_TOOLTIP,STATEMENTS_COLUMN_TOOLTIP,STATEMENT_CALLABLE,STATEMENT_PREPARED,STATEMENT_REGULAR,TABLES_COLUMN_TOOLTIP,TOTAL_TIME_COLUMN_TOOLTIP,messages

CLSS public abstract org.netbeans.lib.profiler.ui.jdbc.LiveJDBCView
cons public init(java.util.Set<org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection>)
meth protected abstract boolean showSourceSupported()
meth protected abstract org.netbeans.lib.profiler.ProfilerClient getProfilerClient()
meth protected abstract void selectForProfiling(org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection)
meth protected abstract void showSQLQuery(java.lang.String,java.lang.String)
meth protected abstract void showSource(org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection)
meth protected boolean profileClassSupported()
meth protected boolean profileMethodSupported()
meth protected void customizeNodePopup(org.netbeans.lib.profiler.ui.results.DataView,javax.swing.JPopupMenu,java.lang.Object,org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection)
meth protected void popupHidden()
meth protected void popupShowing()
meth public boolean isRefreshRunning()
meth public boolean setDiffView(boolean)
meth public long getLastUpdate()
meth public void cleanup()
meth public void refreshSelection()
meth public void resetData()
meth public void setData(org.netbeans.lib.profiler.results.jdbc.JdbcResultsSnapshot)
meth public void setView(boolean,boolean,boolean)
meth public void showSelectionColumn()
supr javax.swing.JPanel
hfds executor,jdbcCallsView,lastFocused,lastupdate,refSnapshot,refreshIsRunning,snapshot

CLSS public org.netbeans.lib.profiler.ui.jdbc.LiveJDBCViewUpdater
cons public init(org.netbeans.lib.profiler.ui.jdbc.LiveJDBCView,org.netbeans.lib.profiler.ProfilerClient)
innr public static CCTHandler
meth public void cleanup()
meth public void setForceRefresh(boolean)
meth public void setPaused(boolean)
meth public void update() throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
supr java.lang.Object
hfds MAX_UPDATE_DIFF,MIN_UPDATE_DIFF,client,forceRefresh,handler,jdbcView,paused

CLSS public static org.netbeans.lib.profiler.ui.jdbc.LiveJDBCViewUpdater$CCTHandler
 outer org.netbeans.lib.profiler.ui.jdbc.LiveJDBCViewUpdater
cons public init()
intf org.netbeans.lib.profiler.results.jdbc.JdbcCCTProvider$Listener
meth public final void cctEstablished(org.netbeans.lib.profiler.results.RuntimeCCTNode,boolean)
meth public final void cctReset()
meth public static org.netbeans.lib.profiler.ui.jdbc.LiveJDBCViewUpdater$CCTHandler registerUpdater(org.netbeans.lib.profiler.ui.jdbc.LiveJDBCViewUpdater)
meth public void unregisterUpdater(org.netbeans.lib.profiler.ui.jdbc.LiveJDBCViewUpdater)
supr java.lang.Object
hfds updaters

CLSS public abstract org.netbeans.lib.profiler.ui.jdbc.SnapshotJDBCView
cons public init(org.netbeans.lib.profiler.results.jdbc.JdbcResultsSnapshot,javax.swing.Action,javax.swing.Action,javax.swing.Action,org.netbeans.lib.profiler.ui.swing.ExportUtils$Exportable)
meth protected abstract boolean showSourceSupported()
meth protected abstract void selectForProfiling(org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection)
meth protected abstract void showSQLQuery(java.lang.String,java.lang.String)
meth protected abstract void showSource(org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection)
meth protected final void setSnapshot(org.netbeans.lib.profiler.results.jdbc.JdbcResultsSnapshot)
meth protected void customizeNodePopup(org.netbeans.lib.profiler.ui.results.DataView,javax.swing.JPopupMenu,java.lang.Object,org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection)
meth public void setRefSnapshot(org.netbeans.lib.profiler.results.jdbc.JdbcResultsSnapshot)
supr javax.swing.JPanel
hfds COMPARE_SNAPSHOTS,RESET_COMPARE_SNAPSHOTS,compareButton,executor,jdbcCallsView,lastFocused,messages,refSnapshot,snapshot

CLSS public abstract org.netbeans.lib.profiler.ui.locks.LockContentionPanel
cons public init()
innr public final static !enum Aggregation
meth protected abstract org.netbeans.lib.profiler.ProfilerClient getProfilerClient()
meth protected boolean hasBottomFilterFindMargin()
meth protected java.awt.Component[] getSearchOptions()
meth protected org.netbeans.lib.profiler.ui.swing.ProfilerTable getResultsComponent()
meth protected org.netbeans.lib.profiler.ui.swing.SearchUtils$TreeHelper getSearchHelper()
meth public java.awt.Component getToolbar()
meth public org.netbeans.lib.profiler.ui.locks.LockContentionPanel$Aggregation getAggregation()
meth public void addExportAction(javax.swing.AbstractAction)
meth public void addLockContentionListener(java.awt.event.ActionListener)
meth public void addSaveViewAction(javax.swing.AbstractAction)
meth public void lockContentionDisabled()
meth public void lockContentionEnabled()
meth public void prepareResults()
meth public void profilingSessionFinished()
meth public void profilingSessionStarted()
meth public void refreshData() throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public void removeLockContentionListener(java.awt.event.ActionListener)
meth public void resetData()
meth public void setAggregation(org.netbeans.lib.profiler.ui.locks.LockContentionPanel$Aggregation)
meth public void setForceRefresh(boolean)
meth public void setPaused(boolean)
supr org.netbeans.lib.profiler.ui.results.DataView
hfds DISPLAY_MODE,ENABLE_LOCKS_MONITORING,ENABLE_LOCKS_MONITORING_TOOLTIP,LOCKS_THREADS_COLUMN_NAME,LOCKS_THREADS_COLUMN_TOOLTIP,MAX_UPDATE_DIFF,MIN_UPDATE_DIFF,MODE_MONITORS,MODE_THREADS,NO_PROFILING,SEARCH_MONITORS_SCOPE,SEARCH_SCOPE_TOOLTIP,SEARCH_THREADS_SCOPE,TIME_REL_COLUMN_NAME,TIME_REL_COLUMN_TOOLTIP,WAITS_COLUMN_NAME,WAITS_COLUMN_TOOLTIP,aggregation,cctListener,columnCount,columnNames,columnRenderers,columnToolTips,columnWidths,contentPanel,countsInMicrosec,enableLockContentionButton,enableLockContentionLabel1,enableLockContentionLabel2,forceRefresh,hbrTime,hbrWaits,lastupdate,messages,modeCombo,notificationPanel,paused,root,searchMonitors,searchThreads,toolbar,treeTable,treeTableModel,treeTablePanel
hcls Listener,LocksTreeTableModel

CLSS public final static !enum org.netbeans.lib.profiler.ui.locks.LockContentionPanel$Aggregation
 outer org.netbeans.lib.profiler.ui.locks.LockContentionPanel
fld public final static org.netbeans.lib.profiler.ui.locks.LockContentionPanel$Aggregation BY_MONITORS
fld public final static org.netbeans.lib.profiler.ui.locks.LockContentionPanel$Aggregation BY_THREADS
meth public static org.netbeans.lib.profiler.ui.locks.LockContentionPanel$Aggregation valueOf(java.lang.String)
meth public static org.netbeans.lib.profiler.ui.locks.LockContentionPanel$Aggregation[] values()
supr java.lang.Enum<org.netbeans.lib.profiler.ui.locks.LockContentionPanel$Aggregation>

CLSS public org.netbeans.lib.profiler.ui.locks.LockContentionRenderer
cons public init()
meth public void setValue(java.lang.Object,int)
supr org.netbeans.lib.profiler.ui.swing.renderer.NormalBoldGrayRenderer
hfds LOCK_ICON,THREAD_ICON

CLSS public org.netbeans.lib.profiler.ui.locks.LockContentionTreeCellRenderer
cons public init()
meth protected java.lang.String getLabel1Text(java.lang.Object,java.lang.String)
meth protected java.lang.String getLabel2Text(java.lang.Object,java.lang.String)
meth protected java.lang.String getLabel3Text(java.lang.Object,java.lang.String)
meth protected javax.swing.Icon getClosedIcon(java.lang.Object)
meth protected javax.swing.Icon getLeafIcon(java.lang.Object)
meth protected javax.swing.Icon getOpenIcon(java.lang.Object)
meth public java.awt.Component getTreeCellRendererComponentPersistent(javax.swing.JTree,java.lang.Object,boolean,boolean,boolean,int,boolean)
supr org.netbeans.lib.profiler.ui.components.tree.EnhancedTreeCellRenderer

CLSS public abstract interface org.netbeans.lib.profiler.ui.memory.ActionsHandler
meth public abstract void performAction(java.lang.String,java.lang.Object[])

CLSS public abstract org.netbeans.lib.profiler.ui.memory.AllocResultsPanel
cons public init(org.netbeans.lib.profiler.ui.memory.MemoryResUserActionsHandler)
fld protected int[] nTotalAllocObjects
fld protected long nTotalBytes
fld protected long nTotalClasses
fld protected long[] totalAllocObjectsSize
meth protected abstract javax.swing.JPopupMenu getPopupMenu()
meth protected boolean passesValueFilter(int)
meth protected java.lang.Object computeValueAt(int,int)
meth protected org.netbeans.lib.profiler.ui.components.JExtendedTable getResultsTable()
meth protected org.netbeans.lib.profiler.ui.components.table.CustomBarCellRenderer getBarCellRenderer()
meth protected void getResultsSortedByAllocObjNumber()
meth protected void getResultsSortedByAllocObjSize()
meth protected void getResultsSortedByClassName(boolean)
meth protected void initColumnSelectorItems()
meth protected void initColumnsData()
meth protected void performDefaultAction(int)
meth public boolean getSortingOrder()
meth public int getSortingColumn()
meth public void setDefaultSorting()
meth public void setSorting(int,boolean)
supr org.netbeans.lib.profiler.ui.memory.MemoryResultsPanel
hfds BYTES_COLUMN_NAME,BYTES_COLUMN_TOOLTIP,BYTES_REL_COLUMN_NAME,BYTES_REL_COLUMN_TOOLTIP,CLASS_COLUMN_NAME,CLASS_COLUMN_TOOLTIP,FILTER_MENU_ITEM_NAME,OBJECTS_COLUMN_NAME,OBJECTS_COLUMN_TOOLTIP,TABLE_ACCESS_NAME,initialSortingColumn,messages,minNamesColumnWidth

CLSS public abstract interface org.netbeans.lib.profiler.ui.memory.ClassHistoryActionsHandler
meth public abstract void showClassHistory(int,java.lang.String)

CLSS public final org.netbeans.lib.profiler.ui.memory.ClassHistoryModels
cons public init(org.netbeans.lib.profiler.results.memory.ClassHistoryDataManager)
meth public org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYItemsModel allocationsItemsModel()
meth public org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYItemsModel livenessItemsModel()
meth public org.netbeans.lib.profiler.results.memory.ClassHistoryDataManager getDataManager()
supr java.lang.Object
hfds allocationsItemsModel,dataManager,livenessItemsModel,timeline

CLSS public org.netbeans.lib.profiler.ui.memory.DiffAllocResultsPanel
cons public init(org.netbeans.lib.profiler.results.memory.AllocMemoryResultsSnapshot,org.netbeans.lib.profiler.ui.memory.MemoryResUserActionsHandler)
meth protected boolean passesValueFilter(int)
meth protected boolean truncateZeroItems()
meth protected java.lang.Object computeValueAt(int,int)
meth protected javax.swing.JPopupMenu getPopupMenu()
meth protected org.netbeans.lib.profiler.ui.components.table.CustomBarCellRenderer getBarCellRenderer()
meth protected void initColumnsData()
meth protected void initDataUponResultsFetch()
meth protected void performDefaultAction(int)
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void exportData(int,org.netbeans.lib.profiler.results.ExportDataDumper,java.lang.String)
supr org.netbeans.lib.profiler.ui.memory.SnapshotAllocResultsPanel
hfds GO_SOURCE_POPUP_ITEM_NAME,diff,memoryResPopupMenu,messages,popupShowSource

CLSS public org.netbeans.lib.profiler.ui.memory.DiffLivenessResultsPanel
cons public init(org.netbeans.lib.profiler.results.memory.LivenessMemoryResultsSnapshot,org.netbeans.lib.profiler.ui.memory.MemoryResUserActionsHandler,int)
meth protected boolean passesValueFilter(int)
meth protected boolean truncateZeroItems()
meth protected java.lang.Object computeValueAt(int,int)
meth protected javax.swing.JPopupMenu getPopupMenu()
meth protected org.netbeans.lib.profiler.ui.components.table.CustomBarCellRenderer getBarCellRenderer()
meth protected void initColumnsData()
meth protected void initDataUponResultsFetch()
meth protected void performDefaultAction(int)
meth public void actionPerformed(java.awt.event.ActionEvent)
supr org.netbeans.lib.profiler.ui.memory.SnapshotLivenessResultsPanel
hfds GO_SOURCE_POPUP_ITEM,diff,messages,popup,popupShowSource

CLSS public org.netbeans.lib.profiler.ui.memory.DiffSampledResultsPanel
cons public init(org.netbeans.lib.profiler.results.memory.SampledMemoryResultsSnapshot,org.netbeans.lib.profiler.ui.memory.MemoryResUserActionsHandler)
meth protected boolean passesValueFilter(int)
meth protected boolean truncateZeroItems()
meth protected java.lang.Object computeValueAt(int,int)
meth protected javax.swing.JPopupMenu getPopupMenu()
meth protected org.netbeans.lib.profiler.ui.components.table.CustomBarCellRenderer getBarCellRenderer()
meth protected void initColumnsData()
meth protected void initDataUponResultsFetch()
meth protected void performDefaultAction(int)
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void exportData(int,org.netbeans.lib.profiler.results.ExportDataDumper,java.lang.String)
supr org.netbeans.lib.profiler.ui.memory.SnapshotSampledResultsPanel
hfds GO_SOURCE_POPUP_ITEM_NAME,diff,memoryResPopupMenu,messages,popupShowSource

CLSS public org.netbeans.lib.profiler.ui.memory.LiveAllocResultsPanel
cons public init(org.netbeans.lib.profiler.TargetAppRunner,org.netbeans.lib.profiler.ui.memory.MemoryResUserActionsHandler,org.netbeans.lib.profiler.ui.memory.ClassHistoryActionsHandler,org.netbeans.lib.profiler.results.memory.ClassHistoryDataManager)
fld protected org.netbeans.lib.profiler.TargetAppRunner runner
intf java.awt.event.ActionListener
intf org.netbeans.lib.profiler.ui.LiveResultsPanel
meth protected java.lang.String getClassName(int)
meth protected java.lang.String[] getClassNames()
meth protected javax.swing.JPopupMenu getPopupMenu()
meth public boolean fitsVisibleArea()
meth public boolean hasView()
meth public boolean supports(int)
meth public java.awt.image.BufferedImage getViewImage(boolean)
meth public java.lang.String getViewName()
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void exportData(int,org.netbeans.lib.profiler.results.ExportDataDumper,java.lang.String)
meth public void fetchResultsFromTargetApp() throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public void handleRemove()
meth public void handleShutdown()
meth public void updateLiveResults()
supr org.netbeans.lib.profiler.ui.memory.AllocResultsPanel
hfds GO_SOURCE_POPUP_ITEM_NAME,LOG_CLASS_HISTORY,SHOW_STACK_TRACES_POPUP_ITEM_NAME,classHistoryManager,historyActionsHandler,memoryResPopupMenu,messages,popupShowSource,popupShowStacks,startHisto,status,updateResultsInProgress,updateResultsPending

CLSS public org.netbeans.lib.profiler.ui.memory.LiveLivenessResultsPanel
cons public init(org.netbeans.lib.profiler.TargetAppRunner,org.netbeans.lib.profiler.ui.memory.MemoryResUserActionsHandler,org.netbeans.lib.profiler.ui.memory.ClassHistoryActionsHandler,org.netbeans.lib.profiler.results.memory.ClassHistoryDataManager)
fld protected org.netbeans.lib.profiler.TargetAppRunner runner
intf java.awt.event.ActionListener
intf org.netbeans.lib.profiler.ui.LiveResultsPanel
meth protected int getPercentsTracked()
meth protected java.lang.String getClassName(int)
meth protected java.lang.String[] getClassNames()
meth protected javax.swing.JPopupMenu getPopupMenu()
meth protected void adjustFramePopupMenuTextIfNecessary()
meth protected void performDefaultAction(int)
meth public boolean fitsVisibleArea()
meth public boolean hasView()
meth public boolean supports(int)
meth public java.awt.image.BufferedImage getViewImage(boolean)
meth public java.lang.String getViewName()
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void fetchResultsFromTargetApp() throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public void handleRemove()
meth public void handleShutdown()
meth public void updateLiveResults()
supr org.netbeans.lib.profiler.ui.memory.LivenessResultsPanel
hfds GO_SOURCE_POPUP_ITEM_NAME,LOG_CLASS_HISTORY,SHOW_STACK_TRACES_POPUP_ITEM_NAME,STOP_BELOW_LINE_POPUP_ITEM_NAME,STOP_BELOW_LINE_SPEC_POPUP_ITEM_NAME,STOP_CLASS_POPUP_ITEM_NAME,STOP_CLASS_SPEC_POPUP_ITEM_NAME,classHistoryManager,historyActionsHandler,messages,popup,popupRemoveProfForClass,popupRemoveProfForClassesBelow,popupShowSource,popupShowStacks,startHisto,status,updateResultsInProgress,updateResultsPending

CLSS public abstract org.netbeans.lib.profiler.ui.memory.LiveMemoryView
cons public init(java.util.Set<org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection>)
meth protected abstract boolean showSourceSupported()
meth protected abstract void selectForProfiling(org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection)
meth protected abstract void showSource(org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection)
meth protected boolean profileClassSupported()
meth protected void customizeNodePopup(org.netbeans.lib.profiler.ui.results.DataView,javax.swing.JPopupMenu,java.lang.Object,org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection)
meth protected void popupHidden()
meth protected void popupShowing()
meth public boolean isRefreshRunning()
meth public boolean setDiffView(boolean)
meth public long getLastUpdate()
meth public void refreshSelection()
meth public void resetData()
meth public void setData(org.netbeans.lib.profiler.results.memory.MemoryResultsSnapshot,org.netbeans.lib.profiler.filters.GenericFilter)
meth public void showSelectionColumn()
supr javax.swing.JPanel
hfds dataView,filter,lastupdate,refSnapshot,refreshIsRunning,selection,snapshot

CLSS public org.netbeans.lib.profiler.ui.memory.LiveMemoryViewUpdater
cons public init(org.netbeans.lib.profiler.ui.memory.LiveMemoryView,org.netbeans.lib.profiler.ProfilerClient)
innr public final static CCTHandler
meth public void cleanup()
meth public void setForceRefresh(boolean)
meth public void setPaused(boolean)
meth public void update() throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
supr java.lang.Object
hfds MAX_UPDATE_DIFF,MIN_UPDATE_DIFF,client,forceRefresh,handler,memoryView,paused

CLSS public final static org.netbeans.lib.profiler.ui.memory.LiveMemoryViewUpdater$CCTHandler
 outer org.netbeans.lib.profiler.ui.memory.LiveMemoryViewUpdater
cons public init()
intf org.netbeans.lib.profiler.results.memory.MemoryCCTProvider$Listener
meth public static org.netbeans.lib.profiler.ui.memory.LiveMemoryViewUpdater$CCTHandler registerUpdater(org.netbeans.lib.profiler.ui.memory.LiveMemoryViewUpdater)
meth public void cctEstablished(org.netbeans.lib.profiler.results.RuntimeCCTNode,boolean)
meth public void cctReset()
meth public void unregisterUpdater(org.netbeans.lib.profiler.ui.memory.LiveMemoryViewUpdater)
supr java.lang.Object
hfds updaters

CLSS public org.netbeans.lib.profiler.ui.memory.LiveReverseMemCallGraphPanel
cons public init(org.netbeans.lib.profiler.global.ProfilingSessionStatus,org.netbeans.lib.profiler.ui.memory.MemoryResUserActionsHandler)
fld protected int classId
fld protected org.netbeans.lib.profiler.global.ProfilingSessionStatus status
fld protected org.netbeans.lib.profiler.results.memory.MemoryCCTManager callGraphManager
meth public void prepareResults()
meth public void setCallGraph(org.netbeans.lib.profiler.results.memory.MemoryCCTManager,int)
supr org.netbeans.lib.profiler.ui.memory.ReverseMemCallGraphPanel
hfds NO_STACKS_MSG,TREETABLE_ACCESS_NAME,abstractTreeTableModel,messages

CLSS public org.netbeans.lib.profiler.ui.memory.LiveSampledResultsPanel
cons public init(org.netbeans.lib.profiler.TargetAppRunner,org.netbeans.lib.profiler.ui.memory.MemoryResUserActionsHandler)
fld protected org.netbeans.lib.profiler.TargetAppRunner runner
intf java.awt.event.ActionListener
intf org.netbeans.lib.profiler.ui.LiveResultsPanel
meth protected java.lang.String getClassName(int)
meth protected java.lang.String[] getClassNames()
meth protected javax.swing.JPopupMenu getPopupMenu()
meth public boolean fitsVisibleArea()
meth public boolean hasView()
meth public boolean supports(int)
meth public java.awt.image.BufferedImage getViewImage(boolean)
meth public java.lang.String getViewName()
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void exportData(int,org.netbeans.lib.profiler.results.ExportDataDumper,java.lang.String)
meth public void fetchResultsFromTargetApp() throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public void handleRemove()
meth public void handleShutdown()
meth public void updateLiveResults()
supr org.netbeans.lib.profiler.ui.memory.SampledResultsPanel
hfds GO_SOURCE_POPUP_ITEM_NAME,classNames,memoryResPopupMenu,messages,popupShowSource,status,updateResultsInProgress,updateResultsPending

CLSS public abstract org.netbeans.lib.profiler.ui.memory.LivenessResultsPanel
cons public init(org.netbeans.lib.profiler.ui.memory.MemoryResUserActionsHandler)
fld protected float[] avgObjectAge
fld protected int nInstrClasses
fld protected int trackedAllocObjects
fld protected int trackedLiveObjects
fld protected int[] maxSurvGen
fld protected int[] nTotalAllocObjects
fld protected int[] nTrackedLiveObjects
fld protected long nTotalTracked
fld protected long nTotalTrackedBytes
fld protected long[] nTrackedAllocObjects
fld protected long[] trackedLiveObjectsSize
meth protected boolean passesValueFilter(int)
meth protected java.lang.Object computeValueAt(int,int)
meth protected org.netbeans.lib.profiler.ui.components.JExtendedTable getResultsTable()
meth protected org.netbeans.lib.profiler.ui.components.table.CustomBarCellRenderer getBarCellRenderer()
meth protected void getResultsSortedByAllocObj()
meth protected void getResultsSortedByAvgAge()
meth protected void getResultsSortedByClassName(boolean)
meth protected void getResultsSortedByLiveObjNumber()
meth protected void getResultsSortedByLiveObjSize()
meth protected void getResultsSortedBySurvGen()
meth protected void getResultsSortedByTotalAllocObj()
meth protected void initColumnSelectorItems()
meth protected void initColumnsData()
meth public boolean getSortingOrder()
meth public int getSortingColumn()
meth public void exportData(int,org.netbeans.lib.profiler.results.ExportDataDumper,java.lang.String)
meth public void setDefaultSorting()
meth public void setSorting(int,boolean)
supr org.netbeans.lib.profiler.ui.memory.MemoryResultsPanel
hfds ALLOC_OBJECTS_COLUMN_NAME,ALLOC_OBJECTS_COLUMN_TOOLTIP,AVG_AGE_COLUMN_NAME,AVG_AGE_COLUMN_TOOLTIP,CLASS_COLUMN_NAME,CLASS_COLUMN_TOOLTIP,FILTER_MENU_ITEM_NAME,LIVE_BYTES_COLUMN_NAME,LIVE_BYTES_COLUMN_TOOLTIP,LIVE_BYTES_REL_COLUMN_NAME,LIVE_BYTES_REL_COLUMN_TOOLTIP,LIVE_OBJECTS_COLUMN_NAME,LIVE_OBJECTS_COLUMN_TOOLTIP,SURVGEN_COLUMN_NAME,SURVGEN_COLUMN_TOOLTIP,TABLE_ACCESS_NAME,TOTAL_ALLOC_OBJECTS_COLUMN_NAME,TOTAL_ALLOC_OBJECTS_COLUMN_TOOLTIP,initialSortingColumn,messages,minNamesColumnWidth

CLSS public org.netbeans.lib.profiler.ui.memory.MemoryCCTTreeModel
cons public init(org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode)
intf javax.swing.tree.TreeModel
meth public boolean isLeaf(java.lang.Object)
meth public int getChildCount(java.lang.Object)
meth public int getIndexOfChild(java.lang.Object,java.lang.Object)
meth public java.lang.Object getChild(java.lang.Object,int)
meth public java.lang.Object getRoot()
meth public void addTreeModelListener(javax.swing.event.TreeModelListener)
meth public void removeTreeModelListener(javax.swing.event.TreeModelListener)
meth public void valueForPathChanged(javax.swing.tree.TreePath,java.lang.Object)
supr java.lang.Object
hfds root

CLSS public org.netbeans.lib.profiler.ui.memory.MemoryJavaNameRenderer
cons public init()
meth public void setValue(java.lang.Object,int)
supr org.netbeans.lib.profiler.ui.swing.renderer.JavaNameRenderer
hfds CLASS_ICON,REVERSE_ICON,REVERSE_ICON_DISABLED

CLSS public abstract interface org.netbeans.lib.profiler.ui.memory.MemoryResUserActionsHandler
fld public final static java.lang.String CANNOT_SHOW_PRIMITIVE_SRC_MSG
fld public final static java.lang.String CANNOT_SHOW_REFLECTION_SRC_MSG
meth public abstract void showSourceForMethod(java.lang.String,java.lang.String,java.lang.String)
meth public abstract void showStacksForClass(int,int,boolean)

CLSS public abstract org.netbeans.lib.profiler.ui.memory.MemoryResultsPanel
cons public init(org.netbeans.lib.profiler.ui.memory.MemoryResUserActionsHandler)
fld protected boolean registeredMouseListenerWithResTable
fld protected boolean sortOrder
fld protected double valueFilterValue
fld protected int clickedLine
fld protected int filterType
fld protected int nDisplayedItems
fld protected int nInfoLines
fld protected int nTrackedItems
fld protected int selectedClassId
fld protected int sortBy
fld protected int[] columnWidths
fld protected int[] sortedClassIds
fld protected java.lang.Class[] columnTypes
fld protected java.lang.String filterString
fld protected java.lang.String[] columnNames
fld protected java.lang.String[] columnToolTips
fld protected java.lang.String[] sortedClassNames
fld protected java.util.ArrayList filteredToFullIndexes
fld protected javax.swing.JButton cornerButton
fld protected javax.swing.JPopupMenu headerPopup
fld protected javax.swing.JScrollPane jScrollPane
fld protected javax.swing.table.TableCellRenderer[] columnRenderers
fld protected long maxValue
fld protected long totalAllocations
fld protected org.netbeans.lib.profiler.ui.components.FilterComponent filterComponent
fld protected org.netbeans.lib.profiler.ui.components.JExtendedTable resTable
fld protected org.netbeans.lib.profiler.ui.components.table.CustomBarCellRenderer barRenderer
fld protected org.netbeans.lib.profiler.ui.components.table.ExtendedTableModel resTableModel
fld protected org.netbeans.lib.profiler.ui.memory.MemoryResUserActionsHandler actionsHandler
meth protected abstract java.lang.String getClassName(int)
meth protected abstract java.lang.String[] getClassNames()
meth protected abstract javax.swing.JPopupMenu getPopupMenu()
meth protected abstract org.netbeans.lib.profiler.ui.components.JExtendedTable getResultsTable()
meth protected abstract void performDefaultAction(int)
meth protected boolean passesValueFilter(int)
meth protected boolean truncateZeroItems()
meth protected final void doCreateClassNamesFromScratch()
meth protected int sortResults(float[],int[][],long[][],int,int,boolean)
meth protected int sortResults(int[],int[][],long[][],float[][],int,int,boolean)
meth protected int sortResults(long[],int[][],long[][],float[][],int,int,boolean)
meth protected int sortResultsByClassName(int[][],long[][],float[][],int,boolean)
meth protected void createFilteredIndexes()
meth protected void initDataUponResultsFetch()
meth protected void showSourceForClass(int)
meth public boolean findFirst()
meth public boolean findNext()
meth public boolean findPrevious()
meth public boolean fitsVisibleArea()
meth public boolean isFindStringDefined()
meth public java.awt.image.BufferedImage getCurrentViewScreenshot(boolean)
meth public java.lang.String getFindString()
meth public void prepareResults()
meth public void requestFocus()
meth public void reset()
meth public void setFindString(java.lang.String)
meth public void updateValueFilter(double)
supr org.netbeans.lib.profiler.ui.ResultsPanel
hfds CLASS_NAME_FILTER,messages

CLSS public abstract org.netbeans.lib.profiler.ui.memory.MemoryView
cons public init()
meth protected abstract org.netbeans.lib.profiler.ui.swing.ProfilerTable getResultsComponent()
meth public abstract org.netbeans.lib.profiler.ui.swing.ExportUtils$ExportProvider[] getExportProviders()
meth public abstract void refreshSelection()
meth public abstract void resetData()
meth public abstract void setData(org.netbeans.lib.profiler.results.memory.MemoryResultsSnapshot,org.netbeans.lib.profiler.filters.GenericFilter,int)
meth public abstract void showSelectionColumn()
supr org.netbeans.lib.profiler.ui.results.DataView
hfds ACTION_GOTOSOURCE,ACTION_PROFILE_CLASS,ACTION_PROFILE_METHOD,ALLOC_COUNT_COLUMN_TOOLTIP,ALLOC_SIZE_COLUMN_TOOLTIP,AVG_AGE_COLUMN_TOOLTIP,COLLAPSE_ALL_ITEM,COLLAPSE_CHILDREN_ITEM,COLUMN_ALLOCATED_BYTES,COLUMN_ALLOCATED_OBJECTS,COLUMN_AVG_AGE,COLUMN_GENERATIONS,COLUMN_LIVE_BYTES,COLUMN_LIVE_OBJECTS,COLUMN_NAME,COLUMN_SELECTED,COLUMN_TOTAL_ALLOCATED_OBJECTS,EXPAND_MENU,EXPAND_PLAIN_ITEM,EXPAND_TOPMOST_ITEM,EXPORT_ALLOCATED,EXPORT_ALLOCATED_LIVE,EXPORT_LIVE,EXPORT_OBJECTS,EXPORT_TOOLTIP,FILTER_ALLOCATIONS_SCOPE,FILTER_CLASSES_SCOPE,FILTER_SCOPE_TOOLTIP,GENERATIONS_COLUMN_TOOLTIP,LIVE_COUNT_COLUMN_TOOLTIP,LIVE_SIZE_COLUMN_TOOLTIP,NAME_COLUMN_TOOLTIP,SEARCH_ALLOCATIONS_SCOPE,SEARCH_CLASSES_SCOPE,SEARCH_SCOPE_TOOLTIP,SELECTED_COLUMN_TOOLTIP,TOTAL_ALLOC_COUNT_COLUMN_TOOLTIP,messages

CLSS public abstract org.netbeans.lib.profiler.ui.memory.ReverseMemCallGraphPanel
cons public init(org.netbeans.lib.profiler.ui.memory.MemoryResUserActionsHandler,boolean)
fld protected boolean extendedResults
fld protected int columnCount
fld protected int minNamesColumnWidth
fld protected int[] columnWidths
fld protected java.lang.String[] columnNames
fld protected java.lang.String[] columnToolTips
fld protected javax.swing.JButton cornerButton
fld protected javax.swing.JMenuItem popupShowSource
fld protected javax.swing.JPopupMenu headerPopup
fld protected javax.swing.JPopupMenu popupMenu
fld protected javax.swing.table.TableCellRenderer[] columnRenderers
fld protected javax.swing.tree.TreePath treePath
fld protected org.netbeans.lib.profiler.ui.components.FilterComponent filterComponent
fld protected org.netbeans.lib.profiler.ui.components.JTreeTable treeTable
fld protected org.netbeans.lib.profiler.ui.components.treetable.ExtendedTreeTableModel treeTableModel
fld protected org.netbeans.lib.profiler.ui.components.treetable.JTreeTablePanel treeTablePanel
fld protected org.netbeans.lib.profiler.ui.memory.MemoryResUserActionsHandler actionsHandler
meth protected javax.swing.JPopupMenu initPopupMenu()
meth protected void initColumnSelectorItems()
meth protected void setColumnsData()
meth public boolean findFirst()
meth public boolean findNext()
meth public boolean findPrevious()
meth public boolean isFindStringDefined()
meth public java.lang.String getFindString()
meth public void requestFocus()
meth public void setFindString(java.lang.String)
supr org.netbeans.lib.profiler.ui.ResultsPanel
hfds ALLOC_OBJECTS_COLUMN_NAME,ALLOC_OBJECTS_COLUMN_TOOLTIP,AVG_AGE_COLUMN_NAME,AVG_AGE_COLUMN_TOOLTIP,BYTES_ALLOC_COLUMN_NAME,BYTES_ALLOC_COLUMN_TOOLTIP,BYTES_ALLOC_REL_COLUMN_NAME,BYTES_ALLOC_REL_COLUMN_TOOLTIP,FILTER_ITEM_NAME,GO_SOURCE_POPUP_ITEM,LIVE_BYTES_COLUMN_NAME,LIVE_BYTES_COLUMN_TOOLTIP,LIVE_BYTES_REL_COLUMN_NAME,LIVE_BYTES_REL_COLUMN_TOOLTIP,LIVE_OBJECTS_COLUMN_NAME,LIVE_OBJECTS_COLUMN_TOOLTIP,METHOD_COLUMN_NAME,METHOD_COLUMN_TOOLTIP,OBJECTS_ALLOC_COLUMN_NAME,OBJECTS_ALLOC_COLUMN_TOOLTIP,SURVGEN_COLUMN_NAME,SURVGEN_COLUMN_TOOLTIP,customBarCellRenderer,enhancedTreeCellRenderer,leafIcon,messages,nodeIcon

CLSS public abstract org.netbeans.lib.profiler.ui.memory.SampledResultsPanel
cons public init(org.netbeans.lib.profiler.ui.memory.MemoryResUserActionsHandler)
fld protected int[] nTotalLiveObjects
fld protected long nTotalClasses
fld protected long nTotalLiveBytes
fld protected long[] totalLiveObjectsSize
meth protected abstract javax.swing.JPopupMenu getPopupMenu()
meth protected boolean passesValueFilter(int)
meth protected java.lang.Object computeValueAt(int,int)
meth protected org.netbeans.lib.profiler.ui.components.JExtendedTable getResultsTable()
meth protected org.netbeans.lib.profiler.ui.components.table.CustomBarCellRenderer getBarCellRenderer()
meth protected void getResultsSortedByClassName(boolean)
meth protected void getResultsSortedByLiveObjNumber()
meth protected void getResultsSortedByLiveObjSize()
meth protected void initColumnSelectorItems()
meth protected void initColumnsData()
meth protected void performDefaultAction(int)
meth public boolean getSortingOrder()
meth public int getSortingColumn()
meth public void setDefaultSorting()
meth public void setSorting(int,boolean)
supr org.netbeans.lib.profiler.ui.memory.MemoryResultsPanel
hfds BYTES_COLUMN_NAME,BYTES_COLUMN_TOOLTIP,BYTES_REL_COLUMN_NAME,BYTES_REL_COLUMN_TOOLTIP,CLASS_COLUMN_NAME,CLASS_COLUMN_TOOLTIP,FILTER_MENU_ITEM_NAME,OBJECTS_COLUMN_NAME,OBJECTS_COLUMN_TOOLTIP,TABLE_ACCESS_NAME,initialSortingColumn,messages,minNamesColumnWidth

CLSS public org.netbeans.lib.profiler.ui.memory.SnapshotAllocResultsPanel
cons public init(org.netbeans.lib.profiler.results.memory.AllocMemoryResultsSnapshot,org.netbeans.lib.profiler.ui.memory.MemoryResUserActionsHandler)
intf java.awt.event.ActionListener
meth protected java.lang.String getClassName(int)
meth protected java.lang.String[] getClassNames()
meth protected javax.swing.JPopupMenu getPopupMenu()
meth public org.netbeans.lib.profiler.results.ResultsSnapshot getSnapshot()
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void exportData(int,org.netbeans.lib.profiler.results.ExportDataDumper,java.lang.String)
supr org.netbeans.lib.profiler.ui.memory.AllocResultsPanel
hfds GO_SOURCE_POPUP_ITEM_NAME,SHOW_STACK_TRACES_POPUP_ITEM_NAME,memoryResPopupMenu,messages,popupShowSource,popupShowStacks,snapshot

CLSS public org.netbeans.lib.profiler.ui.memory.SnapshotLivenessResultsPanel
cons public init(org.netbeans.lib.profiler.results.memory.LivenessMemoryResultsSnapshot,org.netbeans.lib.profiler.ui.memory.MemoryResUserActionsHandler,int)
intf java.awt.event.ActionListener
meth protected int getPercentsTracked()
meth protected java.lang.String getClassName(int)
meth protected java.lang.String[] getClassNames()
meth protected javax.swing.JPopupMenu getPopupMenu()
meth protected void performDefaultAction(int)
meth public void actionPerformed(java.awt.event.ActionEvent)
supr org.netbeans.lib.profiler.ui.memory.LivenessResultsPanel
hfds GO_SOURCE_POPUP_ITEM,STACK_TRACES_POPUP_ITEM,allocTrackEvery,messages,popup,popupShowSource,popupShowStacks,snapshot

CLSS public abstract org.netbeans.lib.profiler.ui.memory.SnapshotMemoryView
cons public init(org.netbeans.lib.profiler.results.memory.MemoryResultsSnapshot,org.netbeans.lib.profiler.filters.GenericFilter,javax.swing.Action,javax.swing.Action,javax.swing.Action,org.netbeans.lib.profiler.ui.swing.ExportUtils$Exportable)
meth protected abstract boolean showSourceSupported()
meth protected abstract void selectForProfiling(org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection)
meth protected abstract void showSource(org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection)
meth protected boolean profileClassSupported()
meth protected boolean profileMethodSupported()
meth protected void customizeNodePopup(org.netbeans.lib.profiler.ui.results.DataView,javax.swing.JPopupMenu,java.lang.Object,org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection)
meth public void setRefSnapshot(org.netbeans.lib.profiler.results.memory.MemoryResultsSnapshot)
supr javax.swing.JPanel
hfds COMPARE_SNAPSHOTS,RESET_COMPARE_SNAPSHOTS,aggregation,compareButton,dataView,filter,messages,refSnapshot,snapshot

CLSS public org.netbeans.lib.profiler.ui.memory.SnapshotReverseMemCallGraphPanel
cons public init(org.netbeans.lib.profiler.results.memory.MemoryResultsSnapshot,org.netbeans.lib.profiler.ui.memory.MemoryResUserActionsHandler)
fld protected int classId
meth public boolean fitsVisibleArea()
meth public boolean hasView()
meth public boolean isEmpty()
meth public java.awt.image.BufferedImage getCurrentViewScreenshot(boolean)
meth public void exportData(int,org.netbeans.lib.profiler.results.ExportDataDumper,java.lang.String)
meth public void prepareResults()
meth public void setClassId(int)
meth public void setDefaultSorting()
meth public void setSorting(int,boolean)
supr org.netbeans.lib.profiler.ui.memory.ReverseMemCallGraphPanel
hfds METHOD_NAME_FILTER,NO_STACK_TRACES_MSG,TREETABLE_ACCESS_NAME,abstractTreeTableModel,callGraphManager,initialSortingColumn,initialSortingOrder,messages,noContentPanel,snapshot

CLSS public org.netbeans.lib.profiler.ui.memory.SnapshotSampledResultsPanel
cons public init(org.netbeans.lib.profiler.results.memory.SampledMemoryResultsSnapshot,org.netbeans.lib.profiler.ui.memory.MemoryResUserActionsHandler)
intf java.awt.event.ActionListener
meth protected java.lang.String getClassName(int)
meth protected java.lang.String[] getClassNames()
meth protected javax.swing.JPopupMenu getPopupMenu()
meth public org.netbeans.lib.profiler.results.ResultsSnapshot getSnapshot()
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void exportData(int,org.netbeans.lib.profiler.results.ExportDataDumper,java.lang.String)
supr org.netbeans.lib.profiler.ui.memory.SampledResultsPanel
hfds GO_SOURCE_POPUP_ITEM_NAME,SHOW_STACK_TRACES_POPUP_ITEM_NAME,memoryResPopupMenu,messages,popupShowSource,popupShowStacks,snapshot

CLSS public org.netbeans.lib.profiler.ui.monitor.MonitorView
cons public init(org.netbeans.lib.profiler.results.monitor.VMTelemetryDataManager)
meth public void cleanup()
meth public void setupCPUView(boolean)
meth public void setupGCView(boolean)
meth public void setupMemoryView(boolean)
meth public void setupThreadsView(boolean)
supr javax.swing.JPanel
hfds cpuPanel,gcPanel,memoryPanel,models,threadsPanel
hcls GraphsLayout

CLSS public final org.netbeans.lib.profiler.ui.monitor.VMTelemetryModels
cons public init(org.netbeans.lib.profiler.results.monitor.VMTelemetryDataManager)
meth public org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYItemsModel cpuItemsModel()
meth public org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYItemsModel generationsItemsModel()
meth public org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYItemsModel memoryItemsModel()
meth public org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYItemsModel threadsItemsModel()
meth public org.netbeans.lib.profiler.results.monitor.VMTelemetryDataManager getDataManager()
meth public void cleanup()
supr java.lang.Object
hfds cpuItemsModel,dataManager,generationsItemsModel,listener,memoryItemsModel,threadsItemsModel,timeline

CLSS public org.netbeans.lib.profiler.ui.results.ColoredFilter
cons public init(java.lang.String,java.lang.String,java.awt.Color)
cons public init(java.util.Properties,java.lang.String)
cons public init(org.netbeans.lib.profiler.ui.results.ColoredFilter)
meth protected boolean valuesEquals(java.lang.Object)
meth protected int valuesHashCode(int)
meth protected java.lang.String[] computeValues(java.lang.String)
meth public boolean passes(java.lang.String)
meth public final java.awt.Color getColor()
meth public final javax.swing.Icon getIcon(int,int)
meth public final void setColor(java.awt.Color)
meth public void copyFrom(org.netbeans.lib.profiler.ui.results.ColoredFilter)
meth public void store(java.util.Properties,java.lang.String)
supr org.netbeans.lib.profiler.filters.GenericFilter
hfds PROP_COLOR,color,icon

CLSS public abstract org.netbeans.lib.profiler.ui.results.DataView
cons protected init()
innr public abstract static PopupCustomizer
meth protected abstract org.netbeans.lib.profiler.ui.swing.ProfilerTable getResultsComponent()
meth protected boolean hasBottomFilterFindMargin()
meth protected java.awt.Component[] getFilterOptions()
meth protected java.awt.Component[] getSearchOptions()
meth protected javax.swing.RowFilter getExcludesFilter()
meth protected org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection getUserValueForRow(int)
meth protected org.netbeans.lib.profiler.ui.swing.SearchUtils$TreeHelper getSearchHelper()
meth protected void addFilterFindPanel(javax.swing.JComponent)
meth protected void enableFilter()
meth protected void installDefaultAction()
meth protected void performDefaultAction(org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection)
meth public final javax.swing.JMenuItem createCopyMenuItem()
meth public final javax.swing.JMenuItem[] createCustomMenuItems(javax.swing.JComponent,java.lang.Object,org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection)
meth public void activateFilter()
meth public void activateSearch()
meth public void notifyOnFocus(java.lang.Runnable)
supr javax.swing.JPanel
hfds bottomPanel,filterPanel,searchPanel
hcls FilterFindLayout

CLSS public abstract static org.netbeans.lib.profiler.ui.results.DataView$PopupCustomizer
 outer org.netbeans.lib.profiler.ui.results.DataView
cons public init()
meth public abstract javax.swing.JMenuItem[] getMenuItems(javax.swing.JComponent,org.netbeans.lib.profiler.ui.results.DataView,java.lang.Object,org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection)
supr java.lang.Object

CLSS public final org.netbeans.lib.profiler.ui.results.PackageColorer
cons public init()
meth public static boolean hasRegisteredColors()
meth public static boolean registerColor(org.netbeans.lib.profiler.ui.results.ColoredFilter)
meth public static boolean unregisterColor(org.netbeans.lib.profiler.ui.results.ColoredFilter)
meth public static java.awt.Color getForeground(java.lang.String)
meth public static java.util.List<org.netbeans.lib.profiler.ui.results.ColoredFilter> getRegisteredColors()
meth public static void setRegisteredColors(java.util.List<org.netbeans.lib.profiler.ui.results.ColoredFilter>)
supr java.lang.Object
hfds COLORS,FILTERS_FILE,FILTERS_JAVAEE,FILTERS_JAVASE,FILTERS_JPA,FILTERS_REFLECTION,FILTERS_SERVERS,messages

CLSS public org.netbeans.lib.profiler.ui.swing.ActionPopupButton
cons public !varargs init(int,javax.swing.Action[])
cons public !varargs init(javax.swing.Action[])
meth protected void populatePopup(javax.swing.JPopupMenu)
meth public final int getSelectedIndex()
meth public final javax.swing.Action getSelectedAction()
meth public final javax.swing.Action[] getActions()
meth public final void selectAction(int)
meth public final void selectAction(javax.swing.Action)
supr org.netbeans.lib.profiler.ui.swing.PopupButton
hfds action,actions

CLSS public final org.netbeans.lib.profiler.ui.swing.ExportUtils
fld public final static org.netbeans.lib.profiler.ui.swing.ExportUtils$FormatFilter CSV_FILTER
fld public final static org.netbeans.lib.profiler.ui.swing.ExportUtils$FormatFilter HTML_FILTER
fld public final static org.netbeans.lib.profiler.ui.swing.ExportUtils$FormatFilter NPS_FILTER
fld public final static org.netbeans.lib.profiler.ui.swing.ExportUtils$FormatFilter PNG_FILTER
fld public final static org.netbeans.lib.profiler.ui.swing.ExportUtils$FormatFilter XML_FILTER
innr public abstract static AbstractNPSExportProvider
innr public abstract static BaseExportProvider
innr public abstract static ExportProvider
innr public abstract static Exportable
innr public abstract static ProfilerTableExportProvider
innr public static CSVExportProvider
innr public static FormatFilter
innr public static HTMLExportProvider
innr public static PNGExportProvider
innr public static XMLExportProvider
meth public !varargs static javax.swing.AbstractButton exportButton(java.awt.Component,java.lang.String,org.netbeans.lib.profiler.ui.swing.ExportUtils$Exportable[])
meth public static boolean exportCSV(org.netbeans.lib.profiler.ui.swing.ProfilerTable,char,java.io.File)
meth public static boolean exportHTML(org.netbeans.lib.profiler.ui.swing.ProfilerTable,java.lang.String,java.io.File)
meth public static boolean exportXML(org.netbeans.lib.profiler.ui.swing.ProfilerTable,java.lang.String,java.io.File)
meth public static java.io.File checkFileExtension(java.io.File,java.lang.String)
supr java.lang.Object
hfds BUTTON_EXPORT,CSV_FILE,FILE_FILTER_DESCR,HTML_FILE,LAST_INDENT,LAST_indent,LOGGER,MSG_CANNOT_OVERWRITE_SOURCE,MSG_EXPORT_IMAGE_FAILED,MSG_EXPORT_SNAPSHOT_FAILED,MSG_OVERWRITE_FILE,NPS_FILE,PNG_FILE,TITLE_OVERWRITE_FILE,XML_FILE,messages
hcls Exporter

CLSS public abstract static org.netbeans.lib.profiler.ui.swing.ExportUtils$AbstractNPSExportProvider
 outer org.netbeans.lib.profiler.ui.swing.ExportUtils
cons public init(java.io.File)
meth protected abstract void doExport(java.io.File)
meth public void export(java.io.File)
supr org.netbeans.lib.profiler.ui.swing.ExportUtils$BaseExportProvider
hfds sourceFile

CLSS public abstract static org.netbeans.lib.profiler.ui.swing.ExportUtils$BaseExportProvider
 outer org.netbeans.lib.profiler.ui.swing.ExportUtils
cons protected init(org.netbeans.lib.profiler.ui.swing.ExportUtils$FormatFilter)
meth public org.netbeans.lib.profiler.ui.swing.ExportUtils$FormatFilter getFormatFilter()
supr org.netbeans.lib.profiler.ui.swing.ExportUtils$ExportProvider
hfds formatFilter

CLSS public static org.netbeans.lib.profiler.ui.swing.ExportUtils$CSVExportProvider
 outer org.netbeans.lib.profiler.ui.swing.ExportUtils
cons public init(org.netbeans.lib.profiler.ui.swing.ProfilerTable)
meth protected void export(org.netbeans.lib.profiler.ui.swing.ProfilerTable,java.io.File)
supr org.netbeans.lib.profiler.ui.swing.ExportUtils$ProfilerTableExportProvider

CLSS public abstract static org.netbeans.lib.profiler.ui.swing.ExportUtils$ExportProvider
 outer org.netbeans.lib.profiler.ui.swing.ExportUtils
cons public init()
meth public abstract org.netbeans.lib.profiler.ui.swing.ExportUtils$FormatFilter getFormatFilter()
meth public abstract void export(java.io.File)
supr java.lang.Object

CLSS public abstract static org.netbeans.lib.profiler.ui.swing.ExportUtils$Exportable
 outer org.netbeans.lib.profiler.ui.swing.ExportUtils
cons public init()
meth public abstract boolean isEnabled()
meth public abstract java.lang.String getName()
meth public abstract org.netbeans.lib.profiler.ui.swing.ExportUtils$ExportProvider[] getProviders()
supr java.lang.Object

CLSS public static org.netbeans.lib.profiler.ui.swing.ExportUtils$FormatFilter
 outer org.netbeans.lib.profiler.ui.swing.ExportUtils
cons public init(java.lang.String,java.lang.String)
meth public boolean accept(java.io.File)
meth public java.lang.String getDescription()
meth public java.lang.String getExtension()
supr javax.swing.filechooser.FileFilter
hfds extension,name

CLSS public static org.netbeans.lib.profiler.ui.swing.ExportUtils$HTMLExportProvider
 outer org.netbeans.lib.profiler.ui.swing.ExportUtils
cons public init(org.netbeans.lib.profiler.ui.swing.ProfilerTable,java.lang.String)
meth protected void export(org.netbeans.lib.profiler.ui.swing.ProfilerTable,java.io.File)
supr org.netbeans.lib.profiler.ui.swing.ExportUtils$ProfilerTableExportProvider
hfds name

CLSS public static org.netbeans.lib.profiler.ui.swing.ExportUtils$PNGExportProvider
 outer org.netbeans.lib.profiler.ui.swing.ExportUtils
cons public init(java.awt.Component)
meth public void export(java.io.File)
supr org.netbeans.lib.profiler.ui.swing.ExportUtils$BaseExportProvider
hfds component

CLSS public abstract static org.netbeans.lib.profiler.ui.swing.ExportUtils$ProfilerTableExportProvider
 outer org.netbeans.lib.profiler.ui.swing.ExportUtils
cons public init(org.netbeans.lib.profiler.ui.swing.ProfilerTable,org.netbeans.lib.profiler.ui.swing.ExportUtils$FormatFilter)
meth protected abstract void export(org.netbeans.lib.profiler.ui.swing.ProfilerTable,java.io.File)
meth public void export(java.io.File)
supr org.netbeans.lib.profiler.ui.swing.ExportUtils$BaseExportProvider
hfds table

CLSS public static org.netbeans.lib.profiler.ui.swing.ExportUtils$XMLExportProvider
 outer org.netbeans.lib.profiler.ui.swing.ExportUtils
cons public init(org.netbeans.lib.profiler.ui.swing.ProfilerTable,java.lang.String)
meth protected void export(org.netbeans.lib.profiler.ui.swing.ProfilerTable,java.io.File)
supr org.netbeans.lib.profiler.ui.swing.ExportUtils$ProfilerTableExportProvider
hfds name

CLSS public final org.netbeans.lib.profiler.ui.swing.FilterUtils
fld public final static java.lang.String ACTION_FILTER
fld public final static java.lang.String FILTER_ACTION_KEY = "filter-action-key"
meth public static boolean filter(org.netbeans.lib.profiler.ui.swing.ProfilerTable,org.netbeans.lib.profiler.filters.GenericFilter,javax.swing.RowFilter)
meth public static boolean filterContains(org.netbeans.lib.profiler.ui.swing.ProfilerTable,java.lang.String)
meth public static boolean filterContains(org.netbeans.lib.profiler.ui.swing.ProfilerTable,java.lang.String,boolean,javax.swing.RowFilter)
meth public static boolean filterNotContains(org.netbeans.lib.profiler.ui.swing.ProfilerTable,java.lang.String,boolean,javax.swing.RowFilter)
meth public static boolean filterRegExp(org.netbeans.lib.profiler.ui.swing.ProfilerTable,java.lang.String,javax.swing.RowFilter)
meth public static javax.swing.JComponent createFilterPanel(org.netbeans.lib.profiler.ui.swing.ProfilerTable)
meth public static javax.swing.JComponent createFilterPanel(org.netbeans.lib.profiler.ui.swing.ProfilerTable,javax.swing.RowFilter)
meth public static javax.swing.JComponent createFilterPanel(org.netbeans.lib.profiler.ui.swing.ProfilerTable,javax.swing.RowFilter,java.awt.Component[])
supr java.lang.Object
hfds BTN_CLOSE_TOOLTIP,BTN_FILTER_TOOLTIP,BTN_MATCH_CASE_TOOLTIP,FILTER_CHANGED,FILTER_CONTAINS,FILTER_NOT_CONTAINS,FILTER_REGEXP,FILTER_TYPE,INSERT_FILTER,MSG_INVALID_REGEXP,SIDEBAR_CAPTION,messages
hcls Filter,Support

CLSS public abstract org.netbeans.lib.profiler.ui.swing.FilteringToolbar
cons public init(java.lang.String)
meth protected abstract void filterChanged()
meth public final boolean isAll()
meth public final boolean passes(java.lang.String)
meth public final org.netbeans.lib.profiler.filters.GenericFilter getFilter()
supr org.netbeans.lib.profiler.ui.swing.InvisibleToolbar
hfds filter,filterButton,hiddenComponents

CLSS public org.netbeans.lib.profiler.ui.swing.GenericToolbar
cons public init()
cons public init(int)
cons public init(java.lang.String)
cons public init(java.lang.String,int)
meth protected boolean isFocusableComponent(java.awt.Component)
meth protected void addImpl(java.awt.Component,java.lang.Object,int)
meth protected void paintComponent(java.awt.Graphics)
meth public java.awt.Dimension getPreferredSize()
meth public void addSeparator()
meth public void doLayout()
supr javax.swing.JToolBar
hfds PREFERRED_HEIGHT

CLSS public org.netbeans.lib.profiler.ui.swing.GrayLabel
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,int)
cons public init(java.lang.String,javax.swing.Icon,int)
cons public init(javax.swing.Icon)
cons public init(javax.swing.Icon,int)
meth public java.awt.Color getForeground()
meth public void setEnabled(boolean)
supr javax.swing.JLabel

CLSS public org.netbeans.lib.profiler.ui.swing.InvisibleToolbar
cons public init()
cons public init(int)
cons public init(java.lang.String)
cons public init(java.lang.String,int)
supr org.netbeans.lib.profiler.ui.swing.GenericToolbar

CLSS public org.netbeans.lib.profiler.ui.swing.MultiButtonGroup
cons public init()
meth public boolean isSelected(javax.swing.ButtonModel)
meth public void setSelected(javax.swing.ButtonModel,boolean)
supr javax.swing.ButtonGroup
hfds selected

CLSS public org.netbeans.lib.profiler.ui.swing.PopupButton
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,javax.swing.Icon)
cons public init(javax.swing.Action)
cons public init(javax.swing.Icon)
meth protected void displayPopup()
meth protected void fireActionPerformed(java.awt.event.ActionEvent)
meth protected void paintComponent(java.awt.Graphics)
meth protected void populatePopup(javax.swing.JPopupMenu)
meth public int getPopupAlign()
meth public java.awt.Dimension getMaximumSize()
meth public java.awt.Dimension getMinimumSize()
meth public java.awt.Dimension getPreferredSize()
meth public void addNotify()
meth public void setPopupAlign(int)
supr org.netbeans.lib.profiler.ui.swing.SmallButton
hfds DROPDOWN_ICON,DROPDOWN_ICON_HEIGHT,DROPDOWN_ICON_WIDTH,iconOffset,popupAlign

CLSS public final org.netbeans.lib.profiler.ui.swing.ProfilerPopup
fld public final static int RESIZE_BOTTOM = 4
fld public final static int RESIZE_LEFT = 2
fld public final static int RESIZE_NONE = 0
fld public final static int RESIZE_RIGHT = 8
fld public final static int RESIZE_TOP = 1
innr public abstract static Listener
meth public static boolean isInPopup(java.awt.Component)
meth public static org.netbeans.lib.profiler.ui.swing.ProfilerPopup create(java.awt.Component,java.awt.Component,int,int)
meth public static org.netbeans.lib.profiler.ui.swing.ProfilerPopup create(java.awt.Component,java.awt.Component,int,int,int)
meth public static org.netbeans.lib.profiler.ui.swing.ProfilerPopup create(java.awt.Component,java.awt.Component,int,int,int,org.netbeans.lib.profiler.ui.swing.ProfilerPopup$Listener)
meth public static org.netbeans.lib.profiler.ui.swing.ProfilerPopup createRelative(java.awt.Component,java.awt.Component,int)
meth public static org.netbeans.lib.profiler.ui.swing.ProfilerPopup createRelative(java.awt.Component,java.awt.Component,int,int)
meth public static org.netbeans.lib.profiler.ui.swing.ProfilerPopup createRelative(java.awt.Component,java.awt.Component,int,int,org.netbeans.lib.profiler.ui.swing.ProfilerPopup$Listener)
meth public void hide()
meth public void show()
supr java.lang.Object
hfds DEBUG,IGNORE_OWNER_TIMEOUT,RESIZE_STRIPE,content,listener,location,owner,ownerLocation,ownerRef,popupAlign,resizeMode,window
hcls PopupFocusTraversalPolicy,PopupPane

CLSS public abstract static org.netbeans.lib.profiler.ui.swing.ProfilerPopup$Listener
 outer org.netbeans.lib.profiler.ui.swing.ProfilerPopup
cons public init()
meth protected void popupHidden()
meth protected void popupShown()
supr java.lang.Object

CLSS public org.netbeans.lib.profiler.ui.swing.ProfilerPopupMenu
cons public init()
cons public init(java.lang.String)
meth protected void paintComponent(java.awt.Graphics)
meth public javax.swing.JMenuItem add(javax.swing.JMenuItem)
meth public void add(java.awt.Component,java.lang.Object)
meth public void setForceBackground(boolean)
supr javax.swing.JPopupMenu
hfds forceBackground

CLSS public org.netbeans.lib.profiler.ui.swing.ProfilerTable
cons public init(javax.swing.table.TableModel,boolean,boolean,int[])
fld public final static java.lang.String PROP_NO_HOVER = "ProfilerTableHover_NoHover"
innr public abstract interface static Tweaker
meth protected int computeColumnPreferredWidth(int,int,int,int)
meth protected javax.swing.table.JTableHeader createDefaultTableHeader()
meth protected javax.swing.table.TableColumnModel createDefaultColumnModel()
meth protected javax.swing.table.TableRowSorter createRowSorter()
meth protected static java.awt.event.MouseEvent clearClicks(java.awt.event.MouseEvent)
meth protected void configureEnclosingScrollPane()
meth protected void populatePopup(javax.swing.JPopupMenu,java.lang.Object,java.lang.Object)
meth protected void popupHidden()
meth protected void popupShowing()
meth protected void processKeyEvent(java.awt.event.KeyEvent)
meth protected void processMouseEvent(java.awt.event.MouseEvent)
meth protected void restoreSelection()
meth protected void saveSelection()
meth protected void setupAppearance()
meth protected void setupModels(boolean)
meth protected void updateColumnsPreferredWidth()
meth public boolean allowsThreeStateColumns()
meth public boolean getFiltersMode()
meth public boolean isColumnVisible(int)
meth public boolean isFocusOwner()
meth public boolean isScrollableColumn(int)
meth public final boolean isSelectionOnMiddlePress()
meth public final boolean providesPopupMenu()
meth public final boolean shadesUnfocusedSelection()
meth public final int getMainColumn()
meth public final javax.swing.JMenuItem createCopyMenuItem()
meth public final void providePopupMenu(boolean)
meth public final void setMainColumn(int)
meth public final void setSelectionOnMiddlePress(boolean)
meth public final void setShadeUnfocusedSelection(boolean)
meth public int getColumnOffset(int)
meth public int getColumnPreferredWidth(int)
meth public int getSortColumn()
meth public java.awt.Color getBackground()
meth public java.awt.Component prepareEditor(javax.swing.table.TableCellEditor,int,int)
meth public java.awt.Component prepareRenderer(javax.swing.table.TableCellRenderer,int,int)
meth public java.lang.Object getSelectedValue(int)
meth public java.lang.Object getUserValueForRow(int)
meth public java.lang.Object getValueForRow(int)
meth public java.lang.Object selectValue(java.lang.Object,int,boolean)
meth public java.lang.String getModelStringValue(int,int)
meth public java.lang.String getStringValue(int,int)
meth public java.util.List getSelectedValues(int)
meth public java.util.Set<java.lang.Integer> getScrollableColumns()
meth public javax.swing.RowFilter getRowFilter()
meth public static javax.swing.table.TableCellRenderer createTableCellRenderer(org.netbeans.lib.profiler.ui.swing.renderer.ProfilerRenderer)
meth public void addRowFilter(javax.swing.RowFilter)
meth public void createDefaultColumnsFromModel()
meth public void disableColumnSorting(int)
meth public void doLayout()
meth public void loadColumns(java.util.Properties)
meth public void performDefaultAction(java.awt.event.ActionEvent)
meth public void removeRowFilter(javax.swing.RowFilter)
meth public void saveColumns(java.util.Properties)
meth public void selectColumn(int,boolean)
meth public void selectRow(int,boolean)
meth public void setAllowsThreeStateColumns(boolean)
meth public void setColumnOffset(int,int)
meth public void setColumnRenderer(int,org.netbeans.lib.profiler.ui.swing.renderer.ProfilerRenderer)
meth public void setColumnToolTips(java.lang.String[])
meth public void setColumnVisibility(int,boolean)
meth public void setDefaultAction(javax.swing.Action)
meth public void setDefaultColumnWidth(int)
meth public void setDefaultColumnWidth(int,int)
meth public void setDefaultRenderer(java.lang.Class<?>,org.netbeans.lib.profiler.ui.swing.renderer.ProfilerRenderer)
meth public void setDefaultSortOrder(int,javax.swing.SortOrder)
meth public void setDefaultSortOrder(javax.swing.SortOrder)
meth public void setFiltersMode(boolean)
meth public void setFitWidthColumn(int)
meth public void setFixedColumnSelection(int)
meth public void setRowFilter(javax.swing.RowFilter)
meth public void setSecondarySortColumn(int)
meth public void setSelectionModel(javax.swing.ListSelectionModel)
meth public void setSortColumn(int)
meth public void setVisibleRows(int)
meth public void tableChanged(javax.swing.event.TableModelEvent)
meth public void updateColumnPreferredWidth(int)
supr javax.swing.JTable
hfds _renderer,columnWidthsValid,defaultAction,fixedSelectionColumn,hideableColums,internal,isCustomRendering,mainColumn,pressedPoint,pressedWhen,providesPopupMenu,scrollableColumns,scrolling,selection,selectionListener,selectionOnMiddlePress,shadeUnfocusedSelection
hcls ProfilerRendererWrapper,ScrollableRenderer

CLSS public abstract interface static org.netbeans.lib.profiler.ui.swing.ProfilerTable$Tweaker
 outer org.netbeans.lib.profiler.ui.swing.ProfilerTable
meth public abstract void tweak(org.netbeans.lib.profiler.ui.swing.ProfilerTable)

CLSS public org.netbeans.lib.profiler.ui.swing.ProfilerTableContainer
cons public init(org.netbeans.lib.profiler.ui.swing.ProfilerTable,boolean,org.netbeans.lib.profiler.ui.swing.ProfilerTableContainer$ColumnChangeAdapter)
innr public static ColumnChangeAdapter
meth public boolean tableNeedsScrolling()
meth public java.awt.image.BufferedImage createTableScreenshot(boolean)
supr javax.swing.JPanel
hfds PROP_COLUMN,scrollersPanel,table,tableScroll

CLSS public static org.netbeans.lib.profiler.ui.swing.ProfilerTableContainer$ColumnChangeAdapter
 outer org.netbeans.lib.profiler.ui.swing.ProfilerTableContainer
cons public init()
meth public void columnOffsetChanged(int,int,int)
meth public void columnPreferredWidthChanged(int,int,int)
meth public void columnWidthChanged(int,int,int)
supr java.lang.Object

CLSS public org.netbeans.lib.profiler.ui.swing.ProfilerTreeTable
cons public init(org.netbeans.lib.profiler.ui.swing.ProfilerTreeTableModel,boolean,boolean,int[])
innr protected static UIState
innr public abstract static NodeExpansionEvaluator
meth protected javax.swing.table.TableRowSorter createRowSorter()
meth protected org.netbeans.lib.profiler.ui.swing.ProfilerTreeTable$UIState getUIState()
meth protected void nodeCollapsed(javax.swing.tree.TreeNode)
meth protected void nodeCollapsing(javax.swing.tree.TreeNode)
meth protected void nodeExpanded(javax.swing.tree.TreeNode)
meth protected void nodeExpanding(javax.swing.tree.TreeNode)
meth protected void processKeyEvent(java.awt.event.KeyEvent)
meth protected void processMouseEvent(java.awt.event.MouseEvent)
meth protected void restoreExpandedNodes(org.netbeans.lib.profiler.ui.swing.ProfilerTreeTable$UIState)
meth protected void restoreSelectedNodes(org.netbeans.lib.profiler.ui.swing.ProfilerTreeTable$UIState)
meth protected void restoreSelection()
meth protected void saveSelection()
meth protected void willBeSorted(java.util.List<? extends javax.swing.RowSorter$SortKey>)
meth public boolean getShowsRootHandles()
meth public boolean isRootVisible()
meth public java.lang.String getStringValue(javax.swing.tree.TreeNode,int)
meth public java.util.Comparator getCurrentComparator()
meth public java.util.Enumeration<javax.swing.tree.TreePath> getExpandedNodes()
meth public javax.swing.tree.DefaultTreeModel getTreeModel()
meth public javax.swing.tree.TreeNode getValueForRow(int)
meth public javax.swing.tree.TreePath getPathForRow(int)
meth public static javax.swing.tree.TreeCellRenderer createTreeCellRenderer(org.netbeans.lib.profiler.ui.swing.renderer.ProfilerRenderer)
meth public void addRowFilter(javax.swing.RowFilter)
meth public void clearExpandedNodes(java.util.Enumeration<javax.swing.tree.TreePath>)
meth public void collapseAll()
meth public void collapseChildren(int)
meth public void collapseChildren(javax.swing.tree.TreePath)
meth public void expandFirstPath(int)
meth public void expandPath(javax.swing.tree.TreePath)
meth public void expandPlainPath(int,int)
meth public void expandRow(int)
meth public void makeTreeAutoExpandable(int)
meth public void removeRowFilter(javax.swing.RowFilter)
meth public void resetExpandedNodes()
meth public void resetPath(javax.swing.tree.TreePath)
meth public void selectPath(javax.swing.tree.TreePath,boolean)
meth public void setCellRenderer(javax.swing.tree.TreeCellRenderer)
meth public void setForgetPreviouslyExpanded(boolean)
meth public void setRootVisible(boolean)
meth public void setRowFilter(javax.swing.RowFilter)
meth public void setRowHeight(int)
meth public void setShowsRootHandles(boolean)
meth public void setTreeCellRenderer(org.netbeans.lib.profiler.ui.swing.renderer.ProfilerRenderer)
supr org.netbeans.lib.profiler.ui.swing.ProfilerTable
hfds DISABLE_TREEUI_FIX,model,tree
hcls Adapter,FilterEntry,FilteredTreeModel,ProfilerRendererWrapper,ProfilerTreeTableSorter,ProfilerTreeTableTree,SortedFilteredTreeModel,SynthLikeTreeUI,TableModelImpl,TreePathKey

CLSS public abstract static org.netbeans.lib.profiler.ui.swing.ProfilerTreeTable$NodeExpansionEvaluator
 outer org.netbeans.lib.profiler.ui.swing.ProfilerTreeTable
cons public init()
meth public abstract java.lang.Boolean hasBeenExpanded(javax.swing.tree.TreePath)
supr java.lang.Object

CLSS protected static org.netbeans.lib.profiler.ui.swing.ProfilerTreeTable$UIState
 outer org.netbeans.lib.profiler.ui.swing.ProfilerTreeTable
meth public java.util.Enumeration getExpandedPaths()
meth public javax.swing.tree.TreePath[] getSelectedPaths()
supr java.lang.Object
hfds expandedPaths,selectedPaths

CLSS public abstract interface org.netbeans.lib.profiler.ui.swing.ProfilerTreeTableModel
innr public abstract interface static Listener
innr public abstract static Abstract
innr public static Adapter
meth public abstract boolean isCellEditable(javax.swing.tree.TreeNode,int)
meth public abstract int getColumnCount()
meth public abstract java.lang.Class getColumnClass(int)
meth public abstract java.lang.Object getValueAt(javax.swing.tree.TreeNode,int)
meth public abstract java.lang.String getColumnName(int)
meth public abstract javax.swing.tree.TreeNode getRoot()
meth public abstract void addListener(org.netbeans.lib.profiler.ui.swing.ProfilerTreeTableModel$Listener)
meth public abstract void removeListener(org.netbeans.lib.profiler.ui.swing.ProfilerTreeTableModel$Listener)
meth public abstract void setValueAt(java.lang.Object,javax.swing.tree.TreeNode,int)

CLSS public abstract static org.netbeans.lib.profiler.ui.swing.ProfilerTreeTableModel$Abstract
 outer org.netbeans.lib.profiler.ui.swing.ProfilerTreeTableModel
cons public init(javax.swing.tree.TreeNode)
intf org.netbeans.lib.profiler.ui.swing.ProfilerTreeTableModel
meth protected void fireChildrenChanged(javax.swing.tree.TreeNode)
meth protected void fireDataChanged()
meth protected void fireRootChanged(javax.swing.tree.TreeNode,javax.swing.tree.TreeNode)
meth protected void fireStructureChanged()
meth public javax.swing.tree.TreeNode getRoot()
meth public void addListener(org.netbeans.lib.profiler.ui.swing.ProfilerTreeTableModel$Listener)
meth public void childrenChanged(javax.swing.tree.TreeNode)
meth public void dataChanged()
meth public void removeListener(org.netbeans.lib.profiler.ui.swing.ProfilerTreeTableModel$Listener)
meth public void setRoot(javax.swing.tree.TreeNode)
meth public void structureChanged()
supr java.lang.Object
hfds listeners,root

CLSS public static org.netbeans.lib.profiler.ui.swing.ProfilerTreeTableModel$Adapter
 outer org.netbeans.lib.profiler.ui.swing.ProfilerTreeTableModel
cons public init()
intf org.netbeans.lib.profiler.ui.swing.ProfilerTreeTableModel$Listener
meth public void childrenChanged(javax.swing.tree.TreeNode)
meth public void dataChanged()
meth public void rootChanged(javax.swing.tree.TreeNode,javax.swing.tree.TreeNode)
meth public void structureChanged()
supr java.lang.Object

CLSS public abstract interface static org.netbeans.lib.profiler.ui.swing.ProfilerTreeTableModel$Listener
 outer org.netbeans.lib.profiler.ui.swing.ProfilerTreeTableModel
meth public abstract void childrenChanged(javax.swing.tree.TreeNode)
meth public abstract void dataChanged()
meth public abstract void rootChanged(javax.swing.tree.TreeNode,javax.swing.tree.TreeNode)
meth public abstract void structureChanged()

CLSS public final org.netbeans.lib.profiler.ui.swing.SearchUtils
fld public final static java.lang.String ACTION_FIND
fld public final static java.lang.String FIND_ACTION_KEY = "find-action-key"
fld public final static java.lang.String FIND_NEXT_ACTION_KEY = "find-next-action-key"
fld public final static java.lang.String FIND_PREV_ACTION_KEY = "find-prev-action-key"
fld public final static java.lang.String FIND_SEL_ACTION_KEY = "find-sel-action-key"
innr public abstract static TreeHelper
meth public static boolean findString(org.netbeans.lib.profiler.ui.swing.ProfilerTable,java.lang.String)
meth public static boolean findString(org.netbeans.lib.profiler.ui.swing.ProfilerTable,java.lang.String,boolean,boolean)
meth public static boolean findString(org.netbeans.lib.profiler.ui.swing.ProfilerTreeTable,java.lang.String,boolean,boolean,org.netbeans.lib.profiler.ui.swing.SearchUtils$TreeHelper)
meth public static javax.swing.JComponent createSearchPanel(org.netbeans.lib.profiler.ui.swing.ProfilerTable)
meth public static javax.swing.JComponent createSearchPanel(org.netbeans.lib.profiler.ui.swing.ProfilerTable,java.awt.Component[])
meth public static javax.swing.JComponent createSearchPanel(org.netbeans.lib.profiler.ui.swing.ProfilerTreeTable,org.netbeans.lib.profiler.ui.swing.SearchUtils$TreeHelper,java.awt.Component[])
meth public static void enableSearchActions(org.netbeans.lib.profiler.ui.swing.ProfilerTable)
supr java.lang.Object
hfds BTN_CLOSE_TOOLTIP,BTN_MATCH_CASE_TOOLTIP,BTN_NEXT,BTN_NEXT_TOOLTIP,BTN_PREVIOUS,BTN_PREVIOUS_TOOLTIP,FIND_TREE_HELPER,LAST_FIND_MATCH_CASE,LAST_FIND_TEXT,MSG_NODATA,MSG_NOTFOUND,SIDEBAR_CAPTION,messages
hcls Support

CLSS public abstract static org.netbeans.lib.profiler.ui.swing.SearchUtils$TreeHelper
 outer org.netbeans.lib.profiler.ui.swing.SearchUtils
cons public init()
fld public final static int NODE_SEARCH_DOWN = 10
fld public final static int NODE_SEARCH_NEXT = 11
fld public final static int NODE_SKIP_DOWN = 100
fld public final static int NODE_SKIP_NEXT = 101
meth public abstract int getNodeType(javax.swing.tree.TreeNode)
supr java.lang.Object

CLSS public org.netbeans.lib.profiler.ui.swing.SmallButton
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,javax.swing.Icon)
cons public init(javax.swing.Action)
cons public init(javax.swing.Icon)
fld protected final static javax.swing.Icon NO_ICON
meth public java.awt.Insets getMargin()
meth public void setIcon(javax.swing.Icon)
supr javax.swing.JButton

CLSS public org.netbeans.lib.profiler.ui.swing.StayOpenPopupMenu
cons public init()
cons public init(java.lang.String)
innr public static CheckBoxItem
innr public static Item
innr public static RadioButtonItem
meth public void processKeyEvent(java.awt.event.KeyEvent,javax.swing.MenuElement[],javax.swing.MenuSelectionManager)
supr org.netbeans.lib.profiler.ui.swing.ProfilerPopupMenu
hcls StayOpen

CLSS public static org.netbeans.lib.profiler.ui.swing.StayOpenPopupMenu$CheckBoxItem
 outer org.netbeans.lib.profiler.ui.swing.StayOpenPopupMenu
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,boolean)
cons public init(java.lang.String,javax.swing.Icon)
cons public init(java.lang.String,javax.swing.Icon,boolean)
cons public init(javax.swing.Action)
cons public init(javax.swing.Icon)
intf java.awt.event.ActionListener
meth protected void processMouseEvent(java.awt.event.MouseEvent)
meth public javax.swing.JMenuItem getItem()
meth public void actionPerformed(java.awt.event.ActionEvent)
supr javax.swing.JCheckBoxMenuItem

CLSS public static org.netbeans.lib.profiler.ui.swing.StayOpenPopupMenu$Item
 outer org.netbeans.lib.profiler.ui.swing.StayOpenPopupMenu
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,int)
cons public init(java.lang.String,javax.swing.Icon)
cons public init(javax.swing.Action)
cons public init(javax.swing.Icon)
intf java.awt.event.ActionListener
meth protected void processMouseEvent(java.awt.event.MouseEvent)
meth public javax.swing.JMenuItem getItem()
meth public void actionPerformed(java.awt.event.ActionEvent)
supr javax.swing.JMenuItem

CLSS public static org.netbeans.lib.profiler.ui.swing.StayOpenPopupMenu$RadioButtonItem
 outer org.netbeans.lib.profiler.ui.swing.StayOpenPopupMenu
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,boolean)
cons public init(java.lang.String,javax.swing.Icon)
cons public init(java.lang.String,javax.swing.Icon,boolean)
cons public init(javax.swing.Action)
cons public init(javax.swing.Icon)
intf java.awt.event.ActionListener
meth protected void processMouseEvent(java.awt.event.MouseEvent)
meth public javax.swing.JMenuItem getItem()
meth public void actionPerformed(java.awt.event.ActionEvent)
supr javax.swing.JRadioButtonMenuItem

CLSS public org.netbeans.lib.profiler.ui.swing.TextArea
cons public init()
cons public init(int,int)
cons public init(java.lang.String)
cons public init(java.lang.String,int,int)
cons public init(javax.swing.text.Document)
cons public init(javax.swing.text.Document,java.lang.String,int,int)
meth protected boolean changeSize(boolean,boolean)
meth protected boolean resetSize()
meth protected final javax.swing.JMenu createResizeMenu()
meth protected void changed()
meth protected void customizePopup(javax.swing.JPopupMenu)
meth protected void processFocusEvent(java.awt.event.FocusEvent)
meth protected void processKeyEvent(java.awt.event.KeyEvent)
meth protected void processMouseEvent(java.awt.event.MouseEvent)
meth public boolean showsHint()
meth public java.lang.String getHint()
meth public void setHint(java.lang.String)
meth public void setText(java.lang.String)
supr javax.swing.JTextArea
hfds changeListener,hint,hintFg,showsHint

CLSS public org.netbeans.lib.profiler.ui.swing.renderer.BarRenderer
cons public init()
fld protected boolean renderingDiff
intf org.netbeans.lib.profiler.ui.swing.renderer.RelativeRenderer
meth public boolean isDiffMode()
meth public long getMaxValue()
meth public void paint(java.awt.Graphics)
meth public void setDiffMode(boolean)
meth public void setMaxValue(long)
meth public void setValue(java.lang.Object,int)
supr org.netbeans.lib.profiler.ui.swing.renderer.BaseRenderer
hfds BAR_RECT,COLOR_NEG,COLOR_POS,FACTOR,X_MARGIN,Y_MARGIN,maxValue,value

CLSS public org.netbeans.lib.profiler.ui.swing.renderer.BaseRenderer
cons public init()
fld protected final java.awt.Dimension size
fld protected final java.awt.Point location
intf org.netbeans.lib.profiler.ui.swing.renderer.ProfilerRenderer
meth protected final java.awt.Dimension sharedDimension(int,int)
meth protected final java.awt.Dimension sharedDimension(java.awt.Dimension)
meth protected final java.awt.Point sharedPoint(int,int)
meth protected final java.awt.Point sharedPoint(java.awt.Point)
meth protected final java.awt.Rectangle sharedRectangle(int,int,int,int)
meth protected final java.awt.Rectangle sharedRectangle(java.awt.Rectangle)
meth protected javax.accessibility.AccessibleContext createAccesibleContext()
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public boolean isEnabled()
meth public boolean isVisible()
meth public int getHeight()
meth public int getHorizontalAlignment()
meth public int getWidth()
meth public int getX()
meth public int getY()
meth public java.awt.Color getBackground()
meth public java.awt.Color getForeground()
meth public java.awt.Dimension getSize()
meth public java.awt.Insets getInsets()
meth public java.awt.Insets getInsets(java.awt.Insets)
meth public java.awt.Point getLocation()
meth public java.awt.Rectangle getBounds()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.JComponent getComponent()
meth public void firePropertyChange(java.lang.String,boolean,boolean)
meth public void firePropertyChange(java.lang.String,byte,byte)
meth public void firePropertyChange(java.lang.String,char,char)
meth public void firePropertyChange(java.lang.String,double,double)
meth public void firePropertyChange(java.lang.String,float,float)
meth public void firePropertyChange(java.lang.String,int,int)
meth public void firePropertyChange(java.lang.String,long,long)
meth public void firePropertyChange(java.lang.String,short,short)
meth public void move(int,int)
meth public void paint(java.awt.Graphics)
meth public void repaint()
meth public void repaint(java.awt.Rectangle)
meth public void repaint(long,int,int,int,int)
meth public void reshape(int,int,int,int)
meth public void revalidate()
meth public void setBackground(java.awt.Color)
meth public void setEnabled(boolean)
meth public void setForeground(java.awt.Color)
meth public void setHorizontalAlignment(int)
meth public void setSize(int,int)
meth public void setValue(java.lang.Object,int)
meth public void setVisible(boolean)
meth public void validate()
supr javax.swing.JComponent
hfds accessibleContext,alignment,background,enabled,foreground,insets,sharedDimension,sharedPoint,sharedRectangle,visible

CLSS public org.netbeans.lib.profiler.ui.swing.renderer.CheckBoxRenderer
cons public init()
fld protected final java.awt.Dimension size
fld protected final java.awt.Point location
intf org.netbeans.lib.profiler.ui.swing.renderer.ProfilerRenderer
meth protected final java.awt.Dimension sharedDimension(int,int)
meth protected final java.awt.Dimension sharedDimension(java.awt.Dimension)
meth protected final java.awt.Point sharedPoint(int,int)
meth protected final java.awt.Point sharedPoint(java.awt.Point)
meth protected final java.awt.Rectangle sharedRectangle(int,int,int,int)
meth protected final java.awt.Rectangle sharedRectangle(java.awt.Rectangle)
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public boolean isEnabled()
meth public int getHeight()
meth public int getWidth()
meth public int getX()
meth public int getY()
meth public java.awt.Color getBackground()
meth public java.awt.Color getForeground()
meth public java.awt.Dimension getSize()
meth public java.awt.Insets getInsets()
meth public java.awt.Insets getInsets(java.awt.Insets)
meth public java.awt.Point getLocation()
meth public java.awt.Rectangle getBounds()
meth public java.lang.String toString()
meth public javax.swing.JComponent getComponent()
meth public void firePropertyChange(java.lang.String,boolean,boolean)
meth public void firePropertyChange(java.lang.String,byte,byte)
meth public void firePropertyChange(java.lang.String,char,char)
meth public void firePropertyChange(java.lang.String,double,double)
meth public void firePropertyChange(java.lang.String,float,float)
meth public void firePropertyChange(java.lang.String,int,int)
meth public void firePropertyChange(java.lang.String,long,long)
meth public void firePropertyChange(java.lang.String,short,short)
meth public void move(int,int)
meth public void repaint()
meth public void repaint(java.awt.Rectangle)
meth public void repaint(long,int,int,int,int)
meth public void reshape(int,int,int,int)
meth public void revalidate()
meth public void setBackground(java.awt.Color)
meth public void setDisplayedMnemonicIndex(int)
meth public void setEnabled(boolean)
meth public void setForeground(java.awt.Color)
meth public void setSize(int,int)
meth public void setValue(java.lang.Object,int)
meth public void validate()
supr javax.swing.JCheckBox
hfds background,enabled,foreground,insets,sharedDimension,sharedPoint,sharedRectangle

CLSS public org.netbeans.lib.profiler.ui.swing.renderer.FormattedLabelRenderer
cons public init(java.text.Format)
meth protected java.lang.String getValueString(java.lang.Object,int,java.text.Format)
meth protected static java.lang.String formatImpl(java.text.Format,java.lang.Object)
meth public void setValue(java.lang.Object,int)
supr org.netbeans.lib.profiler.ui.swing.renderer.LabelRenderer
hfds format

CLSS public org.netbeans.lib.profiler.ui.swing.renderer.HideableBarRenderer
cons public init(org.netbeans.lib.profiler.ui.swing.renderer.ProfilerRenderer)
cons public init(org.netbeans.lib.profiler.ui.swing.renderer.ProfilerRenderer,int)
meth protected int renderersGap()
meth protected org.netbeans.lib.profiler.ui.swing.renderer.ProfilerRenderer[] valueRenderers()
meth public int getMaxNoBarWidth()
meth public int getNoBarWidth()
meth public int getOptimalWidth()
meth public java.awt.Dimension getPreferredSize()
meth public java.lang.String toString()
meth public void paint(java.awt.Graphics)
meth public void setMaxValue(long)
meth public void setValue(java.lang.Object,int)
supr org.netbeans.lib.profiler.ui.swing.renderer.MultiRenderer
hfds BAR_MARGIN,MAX_BAR_WIDTH,MIN_BAR_WIDTH,OPT_BAR_WIDTH,barRenderer,mainRenderer,maxRendererWidth,numberPercentRenderer,valueRenderers

CLSS public org.netbeans.lib.profiler.ui.swing.renderer.JavaNameRenderer
cons public init()
cons public init(javax.swing.Icon)
meth protected void setNormalValue(java.lang.String)
meth public void setValue(java.lang.Object,int)
supr org.netbeans.lib.profiler.ui.swing.renderer.NormalBoldGrayRenderer
hfds icon

CLSS public org.netbeans.lib.profiler.ui.swing.renderer.LabelRenderer
cons public init()
cons public init(boolean)
fld protected final java.awt.Dimension size
fld protected final java.awt.Point location
intf org.netbeans.lib.profiler.ui.swing.renderer.ProfilerRenderer
meth protected final java.awt.Dimension sharedDimension(int,int)
meth protected final java.awt.Dimension sharedDimension(java.awt.Dimension)
meth protected final java.awt.Point sharedPoint(int,int)
meth protected final java.awt.Point sharedPoint(java.awt.Point)
meth protected final java.awt.Rectangle sharedRectangle(int,int,int,int)
meth protected final java.awt.Rectangle sharedRectangle(java.awt.Rectangle)
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public boolean isEnabled()
meth public int getDisplayedMnemonicIndex()
meth public int getHeight()
meth public int getIconTextGap()
meth public int getWidth()
meth public int getX()
meth public int getY()
meth public java.awt.Color getBackground()
meth public java.awt.Color getForeground()
meth public java.awt.Dimension getPreferredSize()
meth public java.awt.Dimension getPreferredSizeImpl()
meth public java.awt.Dimension getSize()
meth public java.awt.FontMetrics getFontMetrics(java.awt.Font)
meth public java.awt.Insets getInsets()
meth public java.awt.Insets getInsets(java.awt.Insets)
meth public java.awt.Insets getMargin()
meth public java.awt.Point getLocation()
meth public java.awt.Rectangle getBounds()
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public javax.swing.Icon getIcon()
meth public javax.swing.JComponent getComponent()
meth public void changeFontSize(int)
meth public void firePropertyChange(java.lang.String,boolean,boolean)
meth public void firePropertyChange(java.lang.String,byte,byte)
meth public void firePropertyChange(java.lang.String,char,char)
meth public void firePropertyChange(java.lang.String,double,double)
meth public void firePropertyChange(java.lang.String,float,float)
meth public void firePropertyChange(java.lang.String,int,int)
meth public void firePropertyChange(java.lang.String,long,long)
meth public void firePropertyChange(java.lang.String,short,short)
meth public void move(int,int)
meth public void paint(java.awt.Graphics)
meth public void repaint()
meth public void repaint(java.awt.Rectangle)
meth public void repaint(long,int,int,int,int)
meth public void reshape(int,int,int,int)
meth public void revalidate()
meth public void setBackground(java.awt.Color)
meth public void setDisplayedMnemonic(char)
meth public void setDisplayedMnemonic(int)
meth public void setDisplayedMnemonicIndex(int)
meth public void setEnabled(boolean)
meth public void setFont(java.awt.Font)
meth public void setForeground(java.awt.Color)
meth public void setIcon(javax.swing.Icon)
meth public void setIconTextGap(int)
meth public void setMargin(int,int,int,int)
meth public void setSize(int,int)
meth public void setText(java.lang.String)
meth public void setUI(javax.swing.plaf.LabelUI)
meth public void setValue(java.lang.Object,int)
meth public void validate()
supr javax.swing.JLabel
hfds DIRTY,UI,background,enabled,fontAscent,fontMetrics,fontSizeDiff,foreground,icon,iconHeight,iconTextGap,iconWidth,insets,margin,preferredSize,sharedDimension,sharedPoint,sharedRectangle,text,textWidth
hcls LabelRendererUI

CLSS public org.netbeans.lib.profiler.ui.swing.renderer.McsTimeRenderer
cons public init()
fld protected boolean renderingDiff
intf org.netbeans.lib.profiler.ui.swing.renderer.RelativeRenderer
meth public boolean isDiffMode()
meth public void setDiffMode(boolean)
meth public void setValue(java.lang.Object,int)
supr org.netbeans.lib.profiler.ui.swing.renderer.FormattedLabelRenderer

CLSS public abstract interface org.netbeans.lib.profiler.ui.swing.renderer.Movable
meth public abstract void move(int,int)

CLSS public abstract org.netbeans.lib.profiler.ui.swing.renderer.MultiRenderer
cons public init()
intf org.netbeans.lib.profiler.ui.swing.renderer.RelativeRenderer
meth protected abstract org.netbeans.lib.profiler.ui.swing.renderer.ProfilerRenderer[] valueRenderers()
meth protected int renderersGap()
meth public boolean isDiffMode()
meth public java.awt.Dimension getPreferredSize()
meth public java.lang.String toString()
meth public void paint(java.awt.Graphics)
meth public void setBackground(java.awt.Color)
meth public void setDiffMode(boolean)
meth public void setForeground(java.awt.Color)
meth public void setOpaque(boolean)
supr org.netbeans.lib.profiler.ui.swing.renderer.BaseRenderer
hfds preferredSize

CLSS public org.netbeans.lib.profiler.ui.swing.renderer.NormalBoldGrayRenderer
cons public init()
meth protected final java.lang.String getBoldValue()
meth protected final java.lang.String getGrayValue()
meth protected final java.lang.String getNormalValue()
meth protected org.netbeans.lib.profiler.ui.swing.renderer.ProfilerRenderer[] valueRenderers()
meth protected void setBoldValue(java.lang.String)
meth protected void setCustomForeground(java.awt.Color)
meth protected void setGrayValue(java.lang.String)
meth protected void setIcon(javax.swing.Icon)
meth protected void setIconTextGap(int)
meth protected void setNormalValue(java.lang.String)
meth public javax.swing.Icon getIcon()
meth public void setReplaceableForeground(java.awt.Color)
supr org.netbeans.lib.profiler.ui.swing.renderer.MultiRenderer
hfds REPLACEABLE_FOREGROUND,boldRenderer,customForeground,grayRenderer,normalRenderer,renderers,replaceableForeground

CLSS public org.netbeans.lib.profiler.ui.swing.renderer.NumberPercentRenderer
cons public init()
cons public init(java.text.Format)
cons public init(org.netbeans.lib.profiler.ui.swing.renderer.ProfilerRenderer)
meth protected org.netbeans.lib.profiler.ui.swing.renderer.ProfilerRenderer[] valueRenderers()
meth public java.awt.Dimension getPreferredSize()
meth public java.lang.String toString()
meth public void setDiffMode(boolean)
meth public void setMaxValue(long)
meth public void setValue(java.lang.Object,int)
supr org.netbeans.lib.profiler.ui.swing.renderer.MultiRenderer
hfds percentRenderer,percentSize,renderers,valueRenderer

CLSS public org.netbeans.lib.profiler.ui.swing.renderer.NumberRenderer
cons public init()
cons public init(java.text.Format)
fld protected boolean renderingDiff
intf org.netbeans.lib.profiler.ui.swing.renderer.RelativeRenderer
meth protected java.lang.String getValueString(java.lang.Object,int,java.text.Format)
meth public boolean isDiffMode()
meth public void setDiffMode(boolean)
supr org.netbeans.lib.profiler.ui.swing.renderer.FormattedLabelRenderer
hfds outputFormat

CLSS public org.netbeans.lib.profiler.ui.swing.renderer.PercentRenderer
cons public init()
fld protected boolean renderingDiff
intf org.netbeans.lib.profiler.ui.swing.renderer.RelativeRenderer
meth protected java.lang.String getValueString(java.lang.Object,int,java.text.Format)
meth public boolean isDiffMode()
meth public long getMaxValue()
meth public void setDiffMode(boolean)
meth public void setMaxValue(long)
supr org.netbeans.lib.profiler.ui.swing.renderer.FormattedLabelRenderer
hfds NAN,NUL,maxValue

CLSS public abstract interface org.netbeans.lib.profiler.ui.swing.renderer.ProfilerRenderer
intf javax.accessibility.Accessible
intf org.netbeans.lib.profiler.ui.swing.renderer.Movable
meth public abstract int getHorizontalAlignment()
meth public abstract javax.swing.JComponent getComponent()
meth public abstract void setValue(java.lang.Object,int)

CLSS public abstract interface org.netbeans.lib.profiler.ui.swing.renderer.RelativeRenderer
meth public abstract boolean isDiffMode()
meth public abstract void setDiffMode(boolean)

CLSS public org.netbeans.lib.profiler.ui.threads.NameStateRenderer
cons public init()
meth public void setValue(java.lang.Object,int)
supr org.netbeans.lib.profiler.ui.swing.renderer.LabelRenderer
hfds STATE_ICONS_CACHE,THREAD_ICON_SIZE

CLSS public org.netbeans.lib.profiler.ui.threads.ThreadStateIcon
cons public init(int,int,int)
fld protected int height
fld protected int width
fld protected java.awt.Color threadStateColor
fld public final static int ICON_NONE = -100
intf javax.swing.Icon
meth protected java.awt.Color getThreadStateColor(int)
meth public int getIconHeight()
meth public int getIconWidth()
meth public void paintIcon(java.awt.Component,java.awt.Graphics,int,int)
supr java.lang.Object

CLSS public org.netbeans.lib.profiler.ui.threads.ThreadTimeRelRenderer
cons public init(org.netbeans.lib.profiler.results.threads.ThreadsDataManager)
meth public void setValue(java.lang.Object,int)
supr org.netbeans.lib.profiler.ui.swing.renderer.HideableBarRenderer
hfds manager

CLSS public org.netbeans.lib.profiler.ui.threads.ThreadsPanel
cons public init(org.netbeans.lib.profiler.results.threads.ThreadsDataManager,javax.swing.Action)
innr public final static !enum Filter
meth protected boolean hasBottomFilterFindMargin()
meth protected org.netbeans.lib.profiler.ui.swing.ProfilerTable getResultsComponent()
meth protected void addFilterFindPanel(javax.swing.JComponent)
meth protected void filterSelected(org.netbeans.lib.profiler.ui.threads.ThreadsPanel$Filter)
meth public boolean fitsVisibleArea()
meth public boolean hasSelectedThreads()
meth public boolean hasView()
meth public java.awt.Component getFitWidth()
meth public java.awt.Component getToolbar()
meth public java.awt.Component getZoomIn()
meth public java.awt.Component getZoomOut()
meth public java.awt.image.BufferedImage getCurrentViewScreenshot(boolean)
meth public org.netbeans.lib.profiler.ui.threads.ThreadsPanel$Filter getFilter()
meth public void addThreadsMonitoringActionListener(java.awt.event.ActionListener)
meth public void cleanup()
meth public void profilingSessionFinished()
meth public void profilingSessionStarted()
meth public void setFilter(org.netbeans.lib.profiler.ui.threads.ThreadsPanel$Filter)
meth public void showSelectedColumn()
meth public void threadsMonitoringDisabled()
meth public void threadsMonitoringEnabled()
supr org.netbeans.lib.profiler.ui.results.DataView
hfds bottomPanel,dataManager,filter,fitAction,lastTimestamp,legendPanel,listener,selected,selectedApplied,threadsTable,threadsTableContainer,timeRelRenderer,viewManager,zoomInAction,zoomOutAction
hcls ThreadsFilter

CLSS public final static !enum org.netbeans.lib.profiler.ui.threads.ThreadsPanel$Filter
 outer org.netbeans.lib.profiler.ui.threads.ThreadsPanel
fld public final static org.netbeans.lib.profiler.ui.threads.ThreadsPanel$Filter ALL
fld public final static org.netbeans.lib.profiler.ui.threads.ThreadsPanel$Filter FINISHED
fld public final static org.netbeans.lib.profiler.ui.threads.ThreadsPanel$Filter LIVE
fld public final static org.netbeans.lib.profiler.ui.threads.ThreadsPanel$Filter SELECTED
meth public static org.netbeans.lib.profiler.ui.threads.ThreadsPanel$Filter valueOf(java.lang.String)
meth public static org.netbeans.lib.profiler.ui.threads.ThreadsPanel$Filter[] values()
supr java.lang.Enum<org.netbeans.lib.profiler.ui.threads.ThreadsPanel$Filter>

CLSS public org.netbeans.lib.profiler.ui.threads.TimelineHeaderRenderer
cons public init(javax.swing.table.TableCellRenderer,int,org.netbeans.lib.profiler.ui.threads.ViewManager)
intf javax.swing.table.TableCellRenderer
meth public java.awt.Component getTableCellRendererComponent(javax.swing.JTable,java.lang.Object,boolean,boolean,int,int)
meth public void paint(java.awt.Graphics)
supr org.netbeans.lib.profiler.ui.swing.renderer.BaseRenderer
hfds Y_LAF_OFFSET,column,impl,painter,view

CLSS public org.netbeans.lib.profiler.ui.threads.TimelineRenderer
cons public init(org.netbeans.lib.profiler.ui.threads.ViewManager)
meth public java.lang.String toString()
meth public void paint(java.awt.Graphics)
meth public void setValue(java.lang.Object,int)
supr org.netbeans.lib.profiler.ui.swing.renderer.BaseRenderer
hfds BAR_MARGIN,BAR_MARGIN_X2,TICK_COLOR,rowView,view

CLSS public org.netbeans.lib.profiler.ui.threads.ViewManager
cons public init(int,org.netbeans.lib.profiler.results.threads.ThreadsDataManager)
fld public final static java.lang.String PROP_NEW_OFFSET = "newOffset"
innr public RowView
meth public boolean isFit()
meth public double getZoom()
meth public int getTimePosition(long,boolean)
meth public int getViewWidth()
meth public int setZoom(double)
meth public int zoomIn()
meth public int zoomOut()
meth public java.lang.String getTimeMarksFormat()
meth public javax.swing.Action fitAction()
meth public javax.swing.Action zoomInAction()
meth public javax.swing.Action zoomOutAction()
meth public long getFirstTimeMark(boolean)
meth public long getTimeMarksStep()
meth public org.netbeans.lib.profiler.ui.threads.ViewManager$RowView getRowView(int)
meth public void columnOffsetChanged(int,int,int)
meth public void columnPreferredWidthChanged(int,int,int)
meth public void columnWidthChanged(int,int,int)
meth public void reset()
meth public void setFit(boolean)
meth public void update()
meth public void zoomChanged(double,double)
supr org.netbeans.lib.profiler.ui.swing.ProfilerTableContainer$ColumnChangeAdapter
hfds MAX_ZOOM,MIN_TIMEMARK_STEP,MIN_VIEW,_firstTimeMark,column,data,firstTimeMark,fit,fitAction,format,lastZoom,offset,prefWidth,rowViews,timeMarksStep,width,zoom,zoomInAction,zoomOutAction
hcls Position

CLSS public org.netbeans.lib.profiler.ui.threads.ViewManager$RowView
 outer org.netbeans.lib.profiler.ui.threads.ViewManager
intf java.lang.Comparable<org.netbeans.lib.profiler.ui.threads.ViewManager$RowView>
meth public int compareTo(org.netbeans.lib.profiler.ui.threads.ViewManager$RowView)
meth public int getLastIndex()
meth public int getMaxIndex()
meth public int getMaxPosition()
meth public int getPosition(long)
meth public int getState(int)
meth public java.lang.String toString()
meth public long getTime(int)
supr java.lang.Object
hfds data,i,lastMaxIn

