#Signature file v4.1
#Version 7.93

CLSS public java.awt.Canvas
cons public init()
cons public init(java.awt.GraphicsConfiguration)
innr protected AccessibleAWTCanvas
intf javax.accessibility.Accessible
meth public java.awt.image.BufferStrategy getBufferStrategy()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public void addNotify()
meth public void createBufferStrategy(int)
meth public void createBufferStrategy(int,java.awt.BufferCapabilities) throws java.awt.AWTException
meth public void paint(java.awt.Graphics)
meth public void update(java.awt.Graphics)
supr java.awt.Component

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

CLSS public abstract interface java.text.AttributedCharacterIterator
innr public static Attribute
intf java.text.CharacterIterator
meth public abstract int getRunLimit()
meth public abstract int getRunLimit(java.text.AttributedCharacterIterator$Attribute)
meth public abstract int getRunLimit(java.util.Set<? extends java.text.AttributedCharacterIterator$Attribute>)
meth public abstract int getRunStart()
meth public abstract int getRunStart(java.text.AttributedCharacterIterator$Attribute)
meth public abstract int getRunStart(java.util.Set<? extends java.text.AttributedCharacterIterator$Attribute>)
meth public abstract java.lang.Object getAttribute(java.text.AttributedCharacterIterator$Attribute)
meth public abstract java.util.Map<java.text.AttributedCharacterIterator$Attribute,java.lang.Object> getAttributes()
meth public abstract java.util.Set<java.text.AttributedCharacterIterator$Attribute> getAllAttributeKeys()

CLSS public abstract interface java.text.CharacterIterator
fld public final static char DONE = '\uffff'
intf java.lang.Cloneable
meth public abstract char current()
meth public abstract char first()
meth public abstract char last()
meth public abstract char next()
meth public abstract char previous()
meth public abstract char setIndex(int)
meth public abstract int getBeginIndex()
meth public abstract int getEndIndex()
meth public abstract int getIndex()
meth public abstract java.lang.Object clone()

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

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public abstract interface javax.accessibility.Accessible
meth public abstract javax.accessibility.AccessibleContext getAccessibleContext()

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

CLSS public javax.swing.JList<%0 extends java.lang.Object>
cons public init()
cons public init(java.util.Vector<? extends {javax.swing.JList%0}>)
cons public init(javax.swing.ListModel<{javax.swing.JList%0}>)
cons public init({javax.swing.JList%0}[])
fld public final static int HORIZONTAL_WRAP = 2
fld public final static int VERTICAL = 0
fld public final static int VERTICAL_WRAP = 1
innr protected AccessibleJList
innr public final static DropLocation
intf javax.accessibility.Accessible
intf javax.swing.Scrollable
meth protected java.lang.String paramString()
meth protected javax.swing.ListSelectionModel createSelectionModel()
meth protected void fireSelectionValueChanged(int,int,boolean)
meth public boolean getDragEnabled()
meth public boolean getScrollableTracksViewportHeight()
meth public boolean getScrollableTracksViewportWidth()
meth public boolean getValueIsAdjusting()
meth public boolean isSelectedIndex(int)
meth public boolean isSelectionEmpty()
meth public final javax.swing.DropMode getDropMode()
meth public final javax.swing.JList$DropLocation getDropLocation()
meth public final void setDropMode(javax.swing.DropMode)
meth public int getAnchorSelectionIndex()
meth public int getFirstVisibleIndex()
meth public int getFixedCellHeight()
meth public int getFixedCellWidth()
meth public int getLastVisibleIndex()
meth public int getLayoutOrientation()
meth public int getLeadSelectionIndex()
meth public int getMaxSelectionIndex()
meth public int getMinSelectionIndex()
meth public int getNextMatch(java.lang.String,int,javax.swing.text.Position$Bias)
meth public int getScrollableBlockIncrement(java.awt.Rectangle,int,int)
meth public int getScrollableUnitIncrement(java.awt.Rectangle,int,int)
meth public int getSelectedIndex()
meth public int getSelectionMode()
meth public int getVisibleRowCount()
meth public int locationToIndex(java.awt.Point)
meth public int[] getSelectedIndices()
meth public java.awt.Color getSelectionBackground()
meth public java.awt.Color getSelectionForeground()
meth public java.awt.Dimension getPreferredScrollableViewportSize()
meth public java.awt.Point indexToLocation(int)
meth public java.awt.Rectangle getCellBounds(int,int)
meth public java.lang.Object[] getSelectedValues()
 anno 0 java.lang.Deprecated()
meth public java.lang.String getToolTipText(java.awt.event.MouseEvent)
meth public java.lang.String getUIClassID()
meth public java.util.List<{javax.swing.JList%0}> getSelectedValuesList()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.ListCellRenderer<? super {javax.swing.JList%0}> getCellRenderer()
meth public javax.swing.ListModel<{javax.swing.JList%0}> getModel()
meth public javax.swing.ListSelectionModel getSelectionModel()
meth public javax.swing.event.ListSelectionListener[] getListSelectionListeners()
meth public javax.swing.plaf.ListUI getUI()
meth public void addListSelectionListener(javax.swing.event.ListSelectionListener)
meth public void addSelectionInterval(int,int)
meth public void clearSelection()
meth public void ensureIndexIsVisible(int)
meth public void removeListSelectionListener(javax.swing.event.ListSelectionListener)
meth public void removeSelectionInterval(int,int)
meth public void setCellRenderer(javax.swing.ListCellRenderer<? super {javax.swing.JList%0}>)
meth public void setDragEnabled(boolean)
meth public void setFixedCellHeight(int)
meth public void setFixedCellWidth(int)
meth public void setLayoutOrientation(int)
meth public void setListData(java.util.Vector<? extends {javax.swing.JList%0}>)
meth public void setListData({javax.swing.JList%0}[])
meth public void setModel(javax.swing.ListModel<{javax.swing.JList%0}>)
meth public void setPrototypeCellValue({javax.swing.JList%0})
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
meth public void updateUI()
meth public {javax.swing.JList%0} getPrototypeCellValue()
meth public {javax.swing.JList%0} getSelectedValue()
supr javax.swing.JComponent

CLSS public javax.swing.JMenu
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,boolean)
cons public init(javax.swing.Action)
fld protected javax.swing.JMenu$WinListener popupListener
innr protected AccessibleJMenu
innr protected WinListener
intf javax.accessibility.Accessible
intf javax.swing.MenuElement
meth protected java.awt.Point getPopupMenuOrigin()
meth protected java.beans.PropertyChangeListener createActionChangeListener(javax.swing.JMenuItem)
meth protected java.lang.String paramString()
meth protected javax.swing.JMenu$WinListener createWinListener(javax.swing.JPopupMenu)
meth protected javax.swing.JMenuItem createActionComponent(javax.swing.Action)
meth protected void fireMenuCanceled()
meth protected void fireMenuDeselected()
meth protected void fireMenuSelected()
meth protected void processKeyEvent(java.awt.event.KeyEvent)
meth public boolean isMenuComponent(java.awt.Component)
meth public boolean isPopupMenuVisible()
meth public boolean isSelected()
meth public boolean isTearOff()
meth public boolean isTopLevelMenu()
meth public int getDelay()
meth public int getItemCount()
meth public int getMenuComponentCount()
meth public java.awt.Component add(java.awt.Component)
meth public java.awt.Component add(java.awt.Component,int)
meth public java.awt.Component getComponent()
meth public java.awt.Component getMenuComponent(int)
meth public java.awt.Component[] getMenuComponents()
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.JMenuItem add(java.lang.String)
meth public javax.swing.JMenuItem add(javax.swing.Action)
meth public javax.swing.JMenuItem add(javax.swing.JMenuItem)
meth public javax.swing.JMenuItem getItem(int)
meth public javax.swing.JMenuItem insert(javax.swing.Action,int)
meth public javax.swing.JMenuItem insert(javax.swing.JMenuItem,int)
meth public javax.swing.JPopupMenu getPopupMenu()
meth public javax.swing.MenuElement[] getSubElements()
meth public javax.swing.event.MenuListener[] getMenuListeners()
meth public void addMenuListener(javax.swing.event.MenuListener)
meth public void addSeparator()
meth public void applyComponentOrientation(java.awt.ComponentOrientation)
meth public void doClick(int)
meth public void insert(java.lang.String,int)
meth public void insertSeparator(int)
meth public void menuSelectionChanged(boolean)
meth public void remove(int)
meth public void remove(java.awt.Component)
meth public void remove(javax.swing.JMenuItem)
meth public void removeAll()
meth public void removeMenuListener(javax.swing.event.MenuListener)
meth public void setAccelerator(javax.swing.KeyStroke)
meth public void setComponentOrientation(java.awt.ComponentOrientation)
meth public void setDelay(int)
meth public void setMenuLocation(int,int)
meth public void setModel(javax.swing.ButtonModel)
meth public void setPopupMenuVisible(boolean)
meth public void setSelected(boolean)
meth public void updateUI()
supr javax.swing.JMenuItem

CLSS public javax.swing.JMenuBar
cons public init()
innr protected AccessibleJMenuBar
intf javax.accessibility.Accessible
intf javax.swing.MenuElement
meth protected boolean processKeyBinding(javax.swing.KeyStroke,java.awt.event.KeyEvent,int,boolean)
meth protected java.lang.String paramString()
meth protected void paintBorder(java.awt.Graphics)
meth public boolean isBorderPainted()
meth public boolean isSelected()
meth public int getComponentIndex(java.awt.Component)
meth public int getMenuCount()
meth public java.awt.Component getComponent()
meth public java.awt.Component getComponentAtIndex(int)
 anno 0 java.lang.Deprecated()
meth public java.awt.Insets getMargin()
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.JMenu add(javax.swing.JMenu)
meth public javax.swing.JMenu getHelpMenu()
meth public javax.swing.JMenu getMenu(int)
meth public javax.swing.MenuElement[] getSubElements()
meth public javax.swing.SingleSelectionModel getSelectionModel()
meth public javax.swing.plaf.MenuBarUI getUI()
meth public void addNotify()
meth public void menuSelectionChanged(boolean)
meth public void processKeyEvent(java.awt.event.KeyEvent,javax.swing.MenuElement[],javax.swing.MenuSelectionManager)
meth public void processMouseEvent(java.awt.event.MouseEvent,javax.swing.MenuElement[],javax.swing.MenuSelectionManager)
meth public void removeNotify()
meth public void setBorderPainted(boolean)
meth public void setHelpMenu(javax.swing.JMenu)
meth public void setMargin(java.awt.Insets)
meth public void setSelected(java.awt.Component)
meth public void setSelectionModel(javax.swing.SingleSelectionModel)
meth public void setUI(javax.swing.plaf.MenuBarUI)
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

CLSS public abstract interface javax.swing.ListCellRenderer<%0 extends java.lang.Object>
meth public abstract java.awt.Component getListCellRendererComponent(javax.swing.JList<? extends {javax.swing.ListCellRenderer%0}>,{javax.swing.ListCellRenderer%0},int,boolean,boolean)

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

CLSS public abstract interface javax.swing.event.ListDataListener
intf java.util.EventListener
meth public abstract void contentsChanged(javax.swing.event.ListDataEvent)
meth public abstract void intervalAdded(javax.swing.event.ListDataEvent)
meth public abstract void intervalRemoved(javax.swing.event.ListDataEvent)

CLSS public abstract interface javax.swing.event.UndoableEditListener
intf java.util.EventListener
meth public abstract void undoableEditHappened(javax.swing.event.UndoableEditEvent)

CLSS public abstract interface javax.swing.table.TableCellRenderer
meth public abstract java.awt.Component getTableCellRendererComponent(javax.swing.JTable,java.lang.Object,boolean,boolean,int,int)

CLSS public abstract interface javax.swing.text.Document
fld public final static java.lang.String StreamDescriptionProperty = "stream"
fld public final static java.lang.String TitleProperty = "title"
meth public abstract int getLength()
meth public abstract java.lang.Object getProperty(java.lang.Object)
meth public abstract java.lang.String getText(int,int) throws javax.swing.text.BadLocationException
meth public abstract javax.swing.text.Element getDefaultRootElement()
meth public abstract javax.swing.text.Element[] getRootElements()
meth public abstract javax.swing.text.Position createPosition(int) throws javax.swing.text.BadLocationException
meth public abstract javax.swing.text.Position getEndPosition()
meth public abstract javax.swing.text.Position getStartPosition()
meth public abstract void addDocumentListener(javax.swing.event.DocumentListener)
meth public abstract void addUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public abstract void getText(int,int,javax.swing.text.Segment) throws javax.swing.text.BadLocationException
meth public abstract void insertString(int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth public abstract void putProperty(java.lang.Object,java.lang.Object)
meth public abstract void remove(int,int) throws javax.swing.text.BadLocationException
meth public abstract void removeDocumentListener(javax.swing.event.DocumentListener)
meth public abstract void removeUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public abstract void render(java.lang.Runnable)

CLSS public abstract interface javax.swing.text.Position
innr public final static Bias
meth public abstract int getOffset()

CLSS public abstract interface javax.swing.text.StyledDocument
intf javax.swing.text.Document
meth public abstract java.awt.Color getBackground(javax.swing.text.AttributeSet)
meth public abstract java.awt.Color getForeground(javax.swing.text.AttributeSet)
meth public abstract java.awt.Font getFont(javax.swing.text.AttributeSet)
meth public abstract javax.swing.text.Element getCharacterElement(int)
meth public abstract javax.swing.text.Element getParagraphElement(int)
meth public abstract javax.swing.text.Style addStyle(java.lang.String,javax.swing.text.Style)
meth public abstract javax.swing.text.Style getLogicalStyle(int)
meth public abstract javax.swing.text.Style getStyle(java.lang.String)
meth public abstract void removeStyle(java.lang.String)
meth public abstract void setCharacterAttributes(int,int,javax.swing.text.AttributeSet,boolean)
meth public abstract void setLogicalStyle(int,javax.swing.text.Style)
meth public abstract void setParagraphAttributes(int,int,javax.swing.text.AttributeSet,boolean)

CLSS public abstract interface javax.swing.tree.TreeCellRenderer
meth public abstract java.awt.Component getTreeCellRendererComponent(javax.swing.JTree,java.lang.Object,boolean,boolean,boolean,int,boolean)

CLSS public javax.swing.undo.AbstractUndoableEdit
cons public init()
fld protected final static java.lang.String RedoName = "Redo"
fld protected final static java.lang.String UndoName = "Undo"
intf java.io.Serializable
intf javax.swing.undo.UndoableEdit
meth public boolean addEdit(javax.swing.undo.UndoableEdit)
meth public boolean canRedo()
meth public boolean canUndo()
meth public boolean isSignificant()
meth public boolean replaceEdit(javax.swing.undo.UndoableEdit)
meth public java.lang.String getPresentationName()
meth public java.lang.String getRedoPresentationName()
meth public java.lang.String getUndoPresentationName()
meth public java.lang.String toString()
meth public void die()
meth public void redo()
meth public void undo()
supr java.lang.Object

CLSS public javax.swing.undo.CompoundEdit
cons public init()
fld protected java.util.Vector<javax.swing.undo.UndoableEdit> edits
meth protected javax.swing.undo.UndoableEdit lastEdit()
meth public boolean addEdit(javax.swing.undo.UndoableEdit)
meth public boolean canRedo()
meth public boolean canUndo()
meth public boolean isInProgress()
meth public boolean isSignificant()
meth public java.lang.String getPresentationName()
meth public java.lang.String getRedoPresentationName()
meth public java.lang.String getUndoPresentationName()
meth public java.lang.String toString()
meth public void die()
meth public void end()
meth public void redo()
meth public void undo()
supr javax.swing.undo.AbstractUndoableEdit

CLSS public javax.swing.undo.UndoManager
cons public init()
intf javax.swing.event.UndoableEditListener
meth protected javax.swing.undo.UndoableEdit editToBeRedone()
meth protected javax.swing.undo.UndoableEdit editToBeUndone()
meth protected void redoTo(javax.swing.undo.UndoableEdit)
meth protected void trimEdits(int,int)
meth protected void trimForLimit()
meth protected void undoTo(javax.swing.undo.UndoableEdit)
meth public boolean addEdit(javax.swing.undo.UndoableEdit)
meth public boolean canRedo()
meth public boolean canUndo()
meth public boolean canUndoOrRedo()
meth public int getLimit()
meth public java.lang.String getRedoPresentationName()
meth public java.lang.String getUndoOrRedoPresentationName()
meth public java.lang.String getUndoPresentationName()
meth public java.lang.String toString()
meth public void discardAllEdits()
meth public void end()
meth public void redo()
meth public void setLimit(int)
meth public void undo()
meth public void undoOrRedo()
meth public void undoableEditHappened(javax.swing.event.UndoableEditEvent)
supr javax.swing.undo.CompoundEdit

CLSS public abstract interface javax.swing.undo.UndoableEdit
meth public abstract boolean addEdit(javax.swing.undo.UndoableEdit)
meth public abstract boolean canRedo()
meth public abstract boolean canUndo()
meth public abstract boolean isSignificant()
meth public abstract boolean replaceEdit(javax.swing.undo.UndoableEdit)
meth public abstract java.lang.String getPresentationName()
meth public abstract java.lang.String getRedoPresentationName()
meth public abstract java.lang.String getUndoPresentationName()
meth public abstract void die()
meth public abstract void redo()
meth public abstract void undo()

CLSS public abstract interface org.netbeans.api.actions.Closable
meth public abstract boolean close()

CLSS public abstract interface org.netbeans.api.actions.Openable
meth public abstract void open()

CLSS public abstract interface org.netbeans.api.actions.Printable
meth public abstract void print()

CLSS public abstract org.netbeans.api.templates.CreateFromTemplateHandler
cons public init()
meth protected abstract boolean accept(org.netbeans.api.templates.CreateDescriptor)
meth protected abstract java.util.List<org.openide.filesystems.FileObject> createFromTemplate(org.netbeans.api.templates.CreateDescriptor) throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth protected static java.util.List<org.openide.filesystems.FileObject> defaultCopyContents(org.netbeans.api.templates.CreateDescriptor,org.openide.filesystems.FileObject,org.openide.filesystems.FileObject) throws java.io.IOException
meth public static void copyAttributesFromTemplate(org.netbeans.api.templates.CreateFromTemplateHandler,org.openide.filesystems.FileObject,org.openide.filesystems.FileObject) throws java.io.IOException
supr java.lang.Object
hfds ATTR_TEMPLATE_PREFIX,PROP_TEMPLATE

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

CLSS public org.openide.actions.CloneViewAction
cons public init()
meth protected boolean asynchronous()
meth protected java.lang.String iconResource()
meth public java.lang.Object getActionMapKey()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CallbackSystemAction

CLSS public org.openide.actions.CloseViewAction
cons public init()
meth protected boolean asynchronous()
meth public java.lang.Object getActionMapKey()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CallbackSystemAction

CLSS public org.openide.actions.CopyAction
cons public init()
meth protected boolean asynchronous()
meth protected java.lang.String iconResource()
meth protected void initialize()
meth public java.lang.Object getActionMapKey()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CallbackSystemAction

CLSS public org.openide.actions.CustomizeAction
cons public init()
meth protected boolean asynchronous()
meth protected boolean enable(org.openide.nodes.Node[])
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.NodeAction

CLSS public org.openide.actions.CutAction
cons public init()
meth protected boolean asynchronous()
meth protected java.lang.String iconResource()
meth protected void initialize()
meth public java.lang.Object getActionMapKey()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CallbackSystemAction

CLSS public org.openide.actions.DeleteAction
cons public init()
meth protected boolean asynchronous()
meth protected java.lang.String iconResource()
meth protected void initialize()
meth public java.lang.Object getActionMapKey()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CallbackSystemAction

CLSS public org.openide.actions.EditAction
cons public init()
meth protected boolean asynchronous()
meth protected boolean surviveFocusChange()
meth protected int mode()
meth protected java.lang.Class[] cookieClasses()
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CookieAction

CLSS public org.openide.actions.FileSystemAction
cons public init()
intf org.openide.util.ContextAwareAction
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
meth public java.lang.String getName()
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem getMenuPresenter()
meth public javax.swing.JMenuItem getPopupPresenter()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void actionPerformed(java.awt.event.ActionEvent)
supr org.openide.util.actions.SystemAction
hfds NONE
hcls DelegateAction,Menu

CLSS public final org.openide.actions.FileSystemRefreshAction
cons public init()
meth protected boolean asynchronous()
meth protected int mode()
meth protected java.lang.Class<?>[] cookieClasses()
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CookieAction

CLSS public org.openide.actions.FindAction
cons public init()
meth protected boolean asynchronous()
meth protected java.lang.String iconResource()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CallbackSystemAction

CLSS public org.openide.actions.GarbageCollectAction
cons public init()
meth protected boolean asynchronous()
meth public java.awt.Component getToolbarPresenter()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void performAction()
supr org.openide.util.actions.CallableSystemAction
hfds NIMBUS_LAF,RP
hcls HeapViewWrapper

CLSS public org.openide.actions.GotoAction
cons public init()
meth protected boolean asynchronous()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CallbackSystemAction

CLSS public org.openide.actions.InstantiateAction
 anno 0 java.lang.Deprecated()
cons public init()
meth protected boolean enable(org.openide.nodes.Node[])
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static java.util.Set<org.openide.loaders.DataObject> instantiateTemplate(org.openide.loaders.DataObject) throws java.io.IOException
supr org.openide.util.actions.NodeAction
hfds serialVersionUID

CLSS public final org.openide.actions.MoveDownAction
cons public init()
meth protected boolean asynchronous()
meth protected boolean enable(org.openide.nodes.Node[])
meth protected void initialize()
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.NodeAction
hfds PROP_ORDER_LISTENER,curIndexCookie
hcls OrderingListener

CLSS public final org.openide.actions.MoveUpAction
cons public init()
meth protected boolean asynchronous()
meth protected boolean enable(org.openide.nodes.Node[])
meth protected void initialize()
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.NodeAction
hfds PROP_ORDER_LISTENER,curIndexCookie,err
hcls OrderingListener

CLSS public final org.openide.actions.NewAction
cons public init()
meth protected boolean asynchronous()
meth protected boolean enable(org.openide.nodes.Node[])
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem getMenuPresenter()
meth public javax.swing.JMenuItem getPopupPresenter()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.NodeAction
hfds model
hcls ActSubMenuModel,DelegateAction

CLSS public org.openide.actions.NewTemplateAction
cons public init()
innr public abstract interface static Cookie
meth protected boolean asynchronous()
meth protected boolean enable(org.openide.nodes.Node[])
meth protected void performAction(org.openide.nodes.Node[])
meth public java.awt.Component getToolbarPresenter()
meth public java.lang.String getName()
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem getMenuPresenter()
meth public javax.swing.JMenuItem getPopupPresenter()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static org.openide.nodes.Node getTemplateRoot()
supr org.openide.util.actions.NodeAction
hfds EMPTY_NODE_ARRAY,MAX_RECENT_ITEMS,active,privilegedListFolder,recentChanged,recentList,recentListFolder,selectedTemplate,targetFolder
hcls DataShadowFilterNode,DefaultTemplateWizard,DelegateAction,MenuWithRecent,NodeLookupListener,RootChildren,TemplateActionListener,TemplateChildren

CLSS public abstract interface static org.openide.actions.NewTemplateAction$Cookie
 outer org.openide.actions.NewTemplateAction
intf org.openide.nodes.Node$Cookie
meth public abstract org.openide.loaders.TemplateWizard getTemplateWizard()

CLSS public org.openide.actions.NextTabAction
cons public init()
meth protected boolean asynchronous()
meth protected java.lang.String iconResource()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CallbackSystemAction

CLSS public org.openide.actions.OpenAction
cons public init()
meth protected boolean asynchronous()
meth protected boolean surviveFocusChange()
meth protected int mode()
meth protected java.lang.Class[] cookieClasses()
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CookieAction

CLSS public final org.openide.actions.OpenLocalExplorerAction
cons public init()
meth protected boolean asynchronous()
meth protected boolean enable(org.openide.nodes.Node[])
meth protected java.lang.String iconResource()
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.NodeAction

CLSS public final org.openide.actions.PageSetupAction
cons public init()
meth protected boolean asynchronous()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void performAction()
supr org.openide.util.actions.CallableSystemAction

CLSS public final org.openide.actions.PasteAction
cons public init()
meth protected boolean asynchronous()
meth protected java.lang.String iconResource()
meth protected void initialize()
meth public java.lang.Object getActionMapKey()
meth public java.lang.String getName()
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem getMenuPresenter()
meth public javax.swing.JMenuItem getPopupPresenter()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public org.openide.util.datatransfer.PasteType[] getPasteTypes()
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void setPasteTypes(org.openide.util.datatransfer.PasteType[])
 anno 0 java.lang.Deprecated()
supr org.openide.util.actions.CallbackSystemAction
hfds globalModel,types
hcls ActSubMenuModel,ActionPT,DelegateAction,NodeSelector

CLSS public final org.openide.actions.PopupAction
cons public init()
meth protected boolean asynchronous()
meth protected void initialize()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CallbackSystemAction

CLSS public org.openide.actions.PreviousTabAction
cons public init()
meth protected boolean asynchronous()
meth protected java.lang.String iconResource()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CallbackSystemAction

CLSS public org.openide.actions.PrintAction
cons public init()
meth protected boolean asynchronous()
meth protected int mode()
meth protected java.lang.Class[] cookieClasses()
meth protected java.lang.String iconResource()
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CookieAction

CLSS public org.openide.actions.PropertiesAction
cons public init()
meth protected boolean asynchronous()
meth protected boolean enable(org.openide.nodes.Node[])
meth protected java.lang.String iconResource()
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem getPopupPresenter()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.NodeAction
hcls DelegateAction

CLSS public org.openide.actions.RedoAction
cons public init()
intf org.openide.util.ContextAwareAction
meth protected boolean asynchronous()
meth protected java.lang.String iconResource()
meth public boolean isEnabled()
meth public java.lang.String getName()
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void performAction()
supr org.openide.util.actions.CallableSystemAction
hfds SWING_DEFAULT_LABEL

CLSS public org.openide.actions.RenameAction
cons public init()
meth protected boolean asynchronous()
meth protected boolean enable(org.openide.nodes.Node[])
meth protected boolean surviveFocusChange()
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.NodeAction
hfds RP

CLSS public org.openide.actions.ReorderAction
cons public init()
meth protected boolean asynchronous()
meth protected boolean surviveFocusChange()
meth protected int mode()
meth protected java.lang.Class[] cookieClasses()
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CookieAction

CLSS public org.openide.actions.ReplaceAction
cons public init()
meth protected boolean asynchronous()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CallbackSystemAction

CLSS public org.openide.actions.SaveAction
cons public init()
meth protected boolean asynchronous()
meth protected int mode()
meth protected java.lang.Class[] cookieClasses()
meth protected java.lang.String iconResource()
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CookieAction
hfds dataObject,getNodeDelegate
hcls Delegate

CLSS public final org.openide.actions.SaveAllAction
cons public init()
meth protected boolean asynchronous()
meth protected java.lang.String iconResource()
meth protected void initialize()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void performAction()
supr org.openide.util.actions.CallableSystemAction
hfds RUNNING,chl,serialVersionUID
hcls ModifiedListL

CLSS public final org.openide.actions.SaveAsTemplateAction
cons public init()
meth protected boolean asynchronous()
meth protected boolean enable(org.openide.nodes.Node[])
meth protected boolean surviveFocusChange()
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public java.lang.String iconResource()
 anno 0 java.lang.Deprecated()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.NodeAction
hfds SCRIPT_ENGINE_ATTR
hcls FolderNodeAcceptor

CLSS public org.openide.actions.ToolsAction
cons public init()
innr public abstract interface static Model
intf org.openide.util.ContextAwareAction
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
meth protected void initialize()
meth public java.lang.String getName()
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem getMenuPresenter()
meth public javax.swing.JMenuItem getPopupPresenter()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static void setModel(org.openide.actions.ToolsAction$Model)
 anno 0 java.lang.Deprecated()
meth public void actionPerformed(java.awt.event.ActionEvent)
supr org.openide.util.actions.SystemAction
hfds serialVersionUID,taskGl
hcls DelegateAction,G,Inline,Popup

CLSS public abstract interface static org.openide.actions.ToolsAction$Model
 outer org.openide.actions.ToolsAction
 anno 0 java.lang.Deprecated()
meth public abstract org.openide.util.actions.SystemAction[] getActions()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public org.openide.actions.UndoAction
cons public init()
intf org.openide.util.ContextAwareAction
meth protected boolean asynchronous()
meth protected java.lang.String iconResource()
meth public boolean isEnabled()
meth public java.lang.String getName()
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void performAction()
supr org.openide.util.actions.CallableSystemAction
hfds SWING_DEFAULT_LABEL,last,listener,redoAction,undoAction
hcls Listener

CLSS public org.openide.actions.UndockAction
 anno 0 java.lang.Deprecated()
cons public init()
meth protected boolean asynchronous()
meth protected java.lang.String iconResource()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CallbackSystemAction

CLSS public org.openide.actions.ViewAction
cons public init()
meth protected boolean asynchronous()
meth protected boolean surviveFocusChange()
meth protected int mode()
meth protected java.lang.Class[] cookieClasses()
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CookieAction

CLSS public org.openide.actions.WorkspaceSwitchAction
 anno 0 java.lang.Deprecated()
cons public init()
meth public java.lang.String getName()
meth public javax.swing.JMenuItem getMenuPresenter()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void performAction()
supr org.openide.util.actions.CallableSystemAction

CLSS public abstract org.openide.awt.AcceleratorBinding
cons protected init()
meth protected abstract javax.swing.KeyStroke keyStrokeForAction(javax.swing.Action,org.openide.filesystems.FileObject)
meth public static void setAccelerator(javax.swing.Action,org.openide.filesystems.FileObject)
supr java.lang.Object
hfds ALL
hcls Iter

CLSS public abstract interface !annotation org.openide.awt.ActionID
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, FIELD, METHOD])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String category()
meth public abstract java.lang.String id()

CLSS public abstract interface !annotation org.openide.awt.ActionReference
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, FIELD, METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()
meth public abstract !hasdefault int separatorAfter()
meth public abstract !hasdefault int separatorBefore()
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault org.openide.awt.ActionID id()
meth public abstract java.lang.String path()

CLSS public abstract interface !annotation org.openide.awt.ActionReferences
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, FIELD, METHOD, PACKAGE])
intf java.lang.annotation.Annotation
meth public abstract org.openide.awt.ActionReference[] value()

CLSS public abstract interface !annotation org.openide.awt.ActionRegistration
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, FIELD, METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean asynchronous()
meth public abstract !hasdefault boolean iconInMenu()
meth public abstract !hasdefault boolean lazy()
meth public abstract !hasdefault boolean surviveFocusChange()
meth public abstract !hasdefault java.lang.String iconBase()
meth public abstract !hasdefault java.lang.String key()
meth public abstract !hasdefault java.lang.String menuText()
meth public abstract !hasdefault java.lang.String popupText()
meth public abstract !hasdefault org.openide.awt.ActionState checkedOn()
meth public abstract !hasdefault org.openide.awt.ActionState enabledOn()
meth public abstract java.lang.String displayName()

CLSS public abstract interface !annotation org.openide.awt.ActionState
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[FIELD])
fld public final static java.lang.String NON_NULL_VALUE = "#non-null"
fld public final static java.lang.String NULL_VALUE = "#null"
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean useActionInstance()
meth public abstract !hasdefault java.lang.Class listenOn()
meth public abstract !hasdefault java.lang.Class<?> type()
meth public abstract !hasdefault java.lang.String checkedValue()
meth public abstract !hasdefault java.lang.String listenOnMethod()
meth public abstract !hasdefault java.lang.String property()

CLSS public org.openide.awt.Actions
cons public init()
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String ACTION_VALUE_TOGGLE = "openide.awt.actionToggle"
fld public final static java.lang.String ACTION_VALUE_VISIBLE = "openide.awt.actionVisible"
innr public abstract interface static ButtonActionConnector
innr public abstract interface static SubMenuModel
innr public static CheckboxMenuItem
innr public static MenuItem
innr public static SubMenu
innr public static ToolbarButton
innr public static ToolbarToggleButton
meth public static java.lang.String cutAmpersand(java.lang.String)
meth public static java.lang.String findKey(org.openide.util.actions.SystemAction)
meth public static java.lang.String keyStrokeToString(javax.swing.KeyStroke)
meth public static javax.swing.Action alwaysEnabled(java.awt.event.ActionListener,java.lang.String,java.lang.String,boolean)
meth public static javax.swing.Action checkbox(java.lang.String,java.lang.String,java.lang.String,java.lang.String,boolean)
meth public static javax.swing.Action forID(java.lang.String,java.lang.String)
meth public static org.openide.util.ContextAwareAction callback(java.lang.String,javax.swing.Action,boolean,java.lang.String,java.lang.String,boolean)
meth public static org.openide.util.ContextAwareAction context(java.lang.Class<?>,boolean,boolean,org.openide.util.ContextAwareAction,java.lang.String,java.lang.String,java.lang.String,boolean)
meth public static void connect(javax.swing.AbstractButton,javax.swing.Action)
meth public static void connect(javax.swing.AbstractButton,org.openide.util.actions.BooleanStateAction)
meth public static void connect(javax.swing.AbstractButton,org.openide.util.actions.SystemAction)
 anno 0 java.lang.Deprecated()
meth public static void connect(javax.swing.JCheckBoxMenuItem,javax.swing.Action,boolean)
meth public static void connect(javax.swing.JCheckBoxMenuItem,org.openide.util.actions.BooleanStateAction,boolean)
 anno 0 java.lang.Deprecated()
meth public static void connect(javax.swing.JMenuItem,javax.swing.Action,boolean)
meth public static void connect(javax.swing.JMenuItem,org.openide.util.actions.SystemAction,boolean)
 anno 0 java.lang.Deprecated()
meth public static void setMenuText(javax.swing.AbstractButton,java.lang.String,boolean)
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds FQN,GET,IDENTIFIER
hcls BooleanButtonBridge,Bridge,ButtonActionConnectorGetter,ButtonBridge,CheckMenuBridge,ISubActionListener,MenuBridge,SubMenuBridge

CLSS public abstract interface static org.openide.awt.Actions$ButtonActionConnector
 outer org.openide.awt.Actions
meth public abstract boolean connect(javax.swing.AbstractButton,javax.swing.Action)
meth public abstract boolean connect(javax.swing.JMenuItem,javax.swing.Action,boolean)

CLSS public static org.openide.awt.Actions$CheckboxMenuItem
 outer org.openide.awt.Actions
cons public init(javax.swing.Action,boolean)
cons public init(org.openide.util.actions.BooleanStateAction,boolean)
 anno 0 java.lang.Deprecated()
supr javax.swing.JCheckBoxMenuItem
hfds serialVersionUID

CLSS public static org.openide.awt.Actions$MenuItem
 outer org.openide.awt.Actions
cons public init(javax.swing.Action,boolean)
cons public init(org.openide.util.actions.SystemAction,boolean)
intf org.openide.awt.DynamicMenuContent
meth public javax.swing.JComponent[] getMenuPresenters()
meth public javax.swing.JComponent[] synchMenuPresenters(javax.swing.JComponent[])
supr javax.swing.JMenuItem
hfds bridge,serialVersionUID

CLSS public static org.openide.awt.Actions$SubMenu
 outer org.openide.awt.Actions
 anno 0 java.lang.Deprecated()
cons public init(javax.swing.Action,org.openide.awt.Actions$SubMenuModel,boolean)
cons public init(org.openide.util.actions.SystemAction,org.openide.awt.Actions$SubMenuModel)
cons public init(org.openide.util.actions.SystemAction,org.openide.awt.Actions$SubMenuModel,boolean)
intf org.openide.awt.DynamicMenuContent
meth public javax.swing.JComponent[] getMenuPresenters()
meth public javax.swing.JComponent[] synchMenuPresenters(javax.swing.JComponent[])
supr org.openide.awt.JMenuPlus
hfds bridge,serialVersionUID

CLSS public abstract interface static org.openide.awt.Actions$SubMenuModel
 outer org.openide.awt.Actions
 anno 0 java.lang.Deprecated()
meth public abstract int getCount()
meth public abstract java.lang.String getLabel(int)
meth public abstract org.openide.util.HelpCtx getHelpCtx(int)
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void performActionAt(int)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public static org.openide.awt.Actions$ToolbarButton
 outer org.openide.awt.Actions
 anno 0 java.lang.Deprecated()
cons public init(javax.swing.Action)
cons public init(org.openide.util.actions.SystemAction)
meth public java.awt.Dimension getMaximumSize()
meth public java.awt.Dimension getMinimumSize()
supr org.openide.awt.ToolbarButton
hfds serialVersionUID

CLSS public static org.openide.awt.Actions$ToolbarToggleButton
 outer org.openide.awt.Actions
 anno 0 java.lang.Deprecated()
cons public init(org.openide.util.actions.BooleanStateAction)
meth public java.awt.Dimension getMaximumSize()
meth public java.awt.Dimension getMinimumSize()
supr org.openide.awt.ToolbarToggleButton
hfds serialVersionUID

CLSS public abstract interface org.openide.awt.CheckForUpdatesProvider
meth public abstract boolean notifyAvailableUpdates(boolean)
meth public abstract boolean openCheckForUpdatesWizard(boolean)
meth public abstract java.lang.String getContentDescription()

CLSS public final org.openide.awt.CloseButtonFactory
meth public static javax.swing.JButton createBigCloseButton()
meth public static javax.swing.JButton createCloseButton()
supr java.lang.Object
hfds bigCloseTabImage,bigCloseTabMouseOverImage,bigCloseTabPressedImage,closeTabImage,closeTabMouseOverImage,closeTabPressedImage

CLSS public final org.openide.awt.ColorComboBox
cons public init()
cons public init(java.awt.Color[],java.lang.String[],boolean)
meth public java.awt.Color getSelectedColor()
meth public void setModel(java.awt.Color[],java.lang.String[])
meth public void setSelectedColor(java.awt.Color)
supr javax.swing.JComboBox
hfds allowCustomColors,lastSelection

CLSS public final org.openide.awt.DropDownButtonFactory
fld public final static java.lang.String PROP_DROP_DOWN_MENU = "dropDownMenu"
meth public static javax.swing.Icon getArrowIcon(boolean)
meth public static javax.swing.JButton createDropDownButton(javax.swing.Icon,javax.swing.JPopupMenu)
meth public static javax.swing.JToggleButton createDropDownToggleButton(javax.swing.Icon,javax.swing.JPopupMenu)
supr java.lang.Object

CLSS public abstract interface org.openide.awt.DynamicMenuContent
fld public final static java.lang.String HIDE_WHEN_DISABLED = "hideWhenDisabled"
meth public abstract javax.swing.JComponent[] getMenuPresenters()
meth public abstract javax.swing.JComponent[] synchMenuPresenters(javax.swing.JComponent[])

CLSS public org.openide.awt.EqualFlowLayout
 anno 0 java.lang.Deprecated()
cons public init()
cons public init(int)
cons public init(int,int,int)
meth public java.awt.Dimension minimumLayoutSize(java.awt.Container)
meth public java.awt.Dimension preferredLayoutSize(java.awt.Container)
meth public void layoutContainer(java.awt.Container)
supr java.awt.FlowLayout
hfds serialVersionUID

CLSS public final org.openide.awt.GraphicsUtils
meth public static void configureDefaultRenderingHints(java.awt.Graphics)
supr java.lang.Object
hfds antialias,gtkAA,hintsMap

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

CLSS public abstract static org.openide.awt.HtmlBrowser$URLDisplayer
 outer org.openide.awt.HtmlBrowser
cons protected init()
meth public abstract void showURL(java.net.URL)
meth public static org.openide.awt.HtmlBrowser$URLDisplayer getDefault()
meth public void showURLExternal(java.net.URL)
supr java.lang.Object

CLSS public final org.openide.awt.HtmlRenderer
fld public final static int STYLE_CLIP = 0
fld public final static int STYLE_TRUNCATE = 1
innr public abstract interface static Renderer
meth public final static javax.swing.JLabel createLabel()
meth public final static org.openide.awt.HtmlRenderer$Renderer createRenderer()
meth public static double renderHTML(java.lang.String,java.awt.Graphics,int,int,int,int,java.awt.Font,java.awt.Color,int,boolean)
meth public static double renderPlainString(java.lang.String,java.awt.Graphics,int,int,int,int,java.awt.Font,java.awt.Color,int,boolean)
meth public static double renderString(java.lang.String,java.awt.Graphics,int,int,int,int,java.awt.Font,java.awt.Color,int,boolean)
supr java.lang.Object
hfds LOG,STRICT_HTML,STYLE_WORDWRAP,badStrings,colorStack,entities,entitySubstitutions

CLSS public abstract interface static org.openide.awt.HtmlRenderer$Renderer
 outer org.openide.awt.HtmlRenderer
intf javax.swing.ListCellRenderer
intf javax.swing.table.TableCellRenderer
intf javax.swing.tree.TreeCellRenderer
meth public abstract void reset()
meth public abstract void setCentered(boolean)
meth public abstract void setHtml(boolean)
meth public abstract void setIcon(javax.swing.Icon)
meth public abstract void setIconTextGap(int)
meth public abstract void setIndent(int)
meth public abstract void setParentFocused(boolean)
meth public abstract void setRenderStyle(int)
meth public abstract void setText(java.lang.String)

CLSS public org.openide.awt.JInlineMenu
 anno 0 java.lang.Deprecated()
cons public init()
intf org.openide.awt.DynamicMenuContent
meth public java.awt.Insets getInsets()
meth public javax.swing.JComponent[] getMenuPresenters()
meth public javax.swing.JComponent[] synchMenuPresenters(javax.swing.JComponent[])
meth public void setMenuItems(javax.swing.JMenuItem[])
supr javax.swing.JMenuItem
hfds BLANK_ICON,addedItems,items,serialVersionUID,upToDate

CLSS public org.openide.awt.JMenuPlus
 anno 0 java.lang.Deprecated()
cons public init()
cons public init(java.lang.String)
supr javax.swing.JMenu
hfds serialVersionUID

CLSS public org.openide.awt.JPopupMenuPlus
 anno 0 java.lang.Deprecated()
cons public init()
supr javax.swing.JPopupMenu

CLSS public org.openide.awt.JPopupMenuUtils
cons public init()
meth public static boolean isPopupContained(javax.swing.JPopupMenu)
meth public static java.awt.Rectangle getScreenRect()
meth public static void dynamicChange(javax.swing.JPopupMenu,boolean)
meth public static void dynamicChangeToSubmenu(javax.swing.JPopupMenu,boolean)
supr java.lang.Object
hfds problem,problemTested,reqProc,task

CLSS public org.openide.awt.ListPane
 anno 0 java.lang.Deprecated()
cons public init()
cons public init(java.lang.Object[])
cons public init(java.util.Vector)
cons public init(javax.swing.ListModel)
meth protected void paintBackground(java.awt.Graphics)
meth protected void paintComponent(java.awt.Graphics)
meth public boolean getScrollableTracksViewportHeight()
meth public boolean getScrollableTracksViewportWidth()
meth public boolean isOpaque()
meth public int getScrollableBlockIncrement(java.awt.Rectangle,int,int)
meth public int getScrollableUnitIncrement(java.awt.Rectangle,int,int)
meth public int getVisibleColumnCount()
meth public int locationToIndex(java.awt.Point)
meth public java.awt.Dimension getMinimumSize()
meth public java.awt.Dimension getPreferredScrollableViewportSize()
meth public java.awt.Dimension getPreferredSize()
meth public java.awt.Point indexToLocation(int)
meth public java.awt.Rectangle getCellBounds(int,int)
meth public void addSelectionInterval(int,int)
meth public void ensureIndexIsVisible(int)
meth public void removeSelectionInterval(int,int)
meth public void setSelectionInterval(int,int)
meth public void setVisibleColumnCount(int)
supr javax.swing.JList
hfds dataL,fixedCellHeight,fixedCellWidth,inputL,propertyL,realColumnCount,realRowCount,selectionL,serialVersionUID,updateLayoutStateNeeded,visibleColumnCount,visibleRowCount
hcls DataListener,InputListener,PropertyListener

CLSS public org.openide.awt.MenuBar
cons public init()
cons public init(org.openide.loaders.DataFolder)
intf java.io.Externalizable
meth public boolean isOpaque()
meth public int getMenuCount()
meth public void addImpl(java.awt.Component,java.lang.Object,int)
meth public void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void updateUI()
meth public void waitFinished()
meth public void writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr javax.swing.JMenuBar
hfds LOG,menuBarFolder,serialVersionUID
hcls LazyMenu,LazySeparator,MarkedKeyEvent,MenuBarFolder

CLSS public final org.openide.awt.Mnemonics
meth public static int findMnemonicAmpersand(java.lang.String)
meth public static void setLocalizedText(javax.swing.AbstractButton,java.lang.String)
meth public static void setLocalizedText(javax.swing.JLabel,java.lang.String)
supr java.lang.Object
hfds MNEMONIC_INDEX_LISTENER,PROP_DISPLAYED_MNEMONIC_INDEX,PROP_MNEMONIC,PROP_TEXT

CLSS public org.openide.awt.MouseUtils
cons public init()
innr public abstract static PopupMouseAdapter
meth public static boolean isDoubleClick(java.awt.event.MouseEvent)
meth public static boolean isLeftMouseButton(java.awt.event.MouseEvent)
 anno 0 java.lang.Deprecated()
meth public static boolean isRightMouseButton(java.awt.event.MouseEvent)
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds DOUBLE_CLICK_DELTA,tempe,temph,tempm,tempx,tempy

CLSS public abstract static org.openide.awt.MouseUtils$PopupMouseAdapter
 outer org.openide.awt.MouseUtils
cons public init()
cons public init(int)
 anno 0 java.lang.Deprecated()
fld public final static int DEFAULT_THRESHOLD = 5
 anno 0 java.lang.Deprecated()
meth protected abstract void showPopup(java.awt.event.MouseEvent)
meth public void mousePressed(java.awt.event.MouseEvent)
meth public void mouseReleased(java.awt.event.MouseEvent)
supr java.awt.event.MouseAdapter

CLSS public abstract org.openide.awt.Notification
cons public init()
meth public abstract void clear()
supr java.lang.Object

CLSS public abstract org.openide.awt.NotificationDisplayer
cons public init()
innr public final static !enum Priority
innr public final static Category
meth public abstract org.openide.awt.Notification notify(java.lang.String,javax.swing.Icon,java.lang.String,java.awt.event.ActionListener,org.openide.awt.NotificationDisplayer$Priority)
meth public abstract org.openide.awt.Notification notify(java.lang.String,javax.swing.Icon,javax.swing.JComponent,javax.swing.JComponent,org.openide.awt.NotificationDisplayer$Priority)
meth public org.openide.awt.Notification notify(java.lang.String,javax.swing.Icon,java.lang.String,java.awt.event.ActionListener)
meth public org.openide.awt.Notification notify(java.lang.String,javax.swing.Icon,java.lang.String,java.awt.event.ActionListener,org.openide.awt.NotificationDisplayer$Priority,java.lang.String)
meth public org.openide.awt.Notification notify(java.lang.String,javax.swing.Icon,java.lang.String,java.awt.event.ActionListener,org.openide.awt.NotificationDisplayer$Priority,org.openide.awt.NotificationDisplayer$Category)
meth public org.openide.awt.Notification notify(java.lang.String,javax.swing.Icon,javax.swing.JComponent,javax.swing.JComponent,org.openide.awt.NotificationDisplayer$Priority,java.lang.String)
meth public org.openide.awt.Notification notify(java.lang.String,javax.swing.Icon,javax.swing.JComponent,javax.swing.JComponent,org.openide.awt.NotificationDisplayer$Priority,org.openide.awt.NotificationDisplayer$Category)
meth public static org.openide.awt.NotificationDisplayer getDefault()
supr java.lang.Object
hcls NotificationImpl,SimpleNotificationDisplayer

CLSS public final static org.openide.awt.NotificationDisplayer$Category
 outer org.openide.awt.NotificationDisplayer
fld public final static org.openide.awt.NotificationDisplayer$Category ERROR
fld public final static org.openide.awt.NotificationDisplayer$Category INFO
fld public final static org.openide.awt.NotificationDisplayer$Category WARNING
intf java.lang.Comparable<org.openide.awt.NotificationDisplayer$Category>
meth public int compareTo(org.openide.awt.NotificationDisplayer$Category)
meth public java.lang.String getDescription()
meth public java.lang.String getDisplayName()
meth public java.lang.String getName()
meth public static java.util.List<org.openide.awt.NotificationDisplayer$Category> getCategories()
supr java.lang.Object
hfds description,displayName,index,name

CLSS public final static !enum org.openide.awt.NotificationDisplayer$Priority
 outer org.openide.awt.NotificationDisplayer
fld public final static org.openide.awt.NotificationDisplayer$Priority HIGH
fld public final static org.openide.awt.NotificationDisplayer$Priority LOW
fld public final static org.openide.awt.NotificationDisplayer$Priority NORMAL
fld public final static org.openide.awt.NotificationDisplayer$Priority SILENT
meth public javax.swing.Icon getIcon()
meth public static org.openide.awt.NotificationDisplayer$Priority valueOf(java.lang.String)
meth public static org.openide.awt.NotificationDisplayer$Priority[] values()
supr java.lang.Enum<org.openide.awt.NotificationDisplayer$Priority>
hfds icon

CLSS public org.openide.awt.QuickSearch
innr public abstract interface static Callback
meth protected void maybeShowPopup(java.awt.event.MouseEvent,java.awt.Component)
meth public boolean isAlwaysShown()
meth public boolean isEnabled()
meth public static java.lang.String findMaxPrefix(java.lang.String,java.lang.String,boolean)
meth public static org.openide.awt.QuickSearch attach(javax.swing.JComponent,java.lang.Object,org.openide.awt.QuickSearch$Callback)
meth public static org.openide.awt.QuickSearch attach(javax.swing.JComponent,java.lang.Object,org.openide.awt.QuickSearch$Callback,boolean)
meth public static org.openide.awt.QuickSearch attach(javax.swing.JComponent,java.lang.Object,org.openide.awt.QuickSearch$Callback,boolean,javax.swing.JMenu)
meth public static org.openide.awt.QuickSearch attach(javax.swing.JComponent,java.lang.Object,org.openide.awt.QuickSearch$Callback,javax.swing.JMenu)
meth public void detach()
meth public void processKeyEvent(java.awt.event.KeyEvent)
meth public void setAlwaysShown(boolean)
meth public void setEnabled(boolean)
supr java.lang.Object
hfds CLIENT_PROPERTY_KEY,ICON_FIND,ICON_FIND_WITH_MENU,alwaysShown,animationTimer,asynchronous,callback,component,constraints,enabled,hasSearchText,popupMenu,quickSearchKeyAdapter,rp,searchFieldListener,searchPanel,searchTextField
hcls AnimationTimer,LazyFire,QS_FIRE,SearchFieldListener,SearchPanel,SearchTextField

CLSS public abstract interface static org.openide.awt.QuickSearch$Callback
 outer org.openide.awt.QuickSearch
meth public abstract java.lang.String findMaxPrefix(java.lang.String)
meth public abstract void quickSearchCanceled()
meth public abstract void quickSearchConfirmed()
meth public abstract void quickSearchUpdate(java.lang.String)
meth public abstract void showNextSelection(boolean)

CLSS public org.openide.awt.SpinButton
 anno 0 java.lang.Deprecated()
cons public init()
fld protected boolean arrowsOrientation
fld protected boolean boundsIgnored
fld protected boolean orientation
fld protected boolean repeating
fld protected boolean running
fld protected boolean runningDir
fld protected int maximum
fld protected int minimum
fld protected int repeatDelay
fld protected int repeatRate
fld protected int step
fld protected int value
fld protected org.openide.awt.SpinButton$RepeatThread rt
fld public final static boolean DEFAULT_ORIENTATION = false
fld public final static int DEFAULT_MAXIMUM = 100
fld public final static int DEFAULT_MINIMUM = 0
fld public final static int DEFAULT_REPEAT_DELAY = 300
fld public final static int DEFAULT_REPEAT_RATE = 70
fld public final static int DEFAULT_STEP = 1
innr protected final RepeatThread
meth protected void repeatThreadNotify()
meth protected void switchRun(boolean)
meth public boolean getArrowsOrientation()
meth public boolean getOrientation()
meth public boolean isBoundsIgnored()
meth public boolean isRepeating()
meth public int getDelay()
meth public int getMaximum()
meth public int getMinimum()
meth public int getRate()
meth public int getStep()
meth public int getValue()
meth public java.awt.Dimension getMinimumSize()
meth public java.awt.Dimension getPreferredSize()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addSpinButtonListener(org.openide.awt.SpinButtonListener)
meth public void notifySpinButtonListenersAboutDownMove()
meth public void notifySpinButtonListenersAboutUpMove()
meth public void paint(java.awt.Graphics)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removeSpinButtonListener(org.openide.awt.SpinButtonListener)
meth public void setArrowsOrientation(boolean)
meth public void setBoundsIgnored(boolean)
meth public void setDelay(int)
meth public void setForeground(java.awt.Color)
meth public void setMaximum(int)
meth public void setMinimum(int)
meth public void setOrientation(boolean)
meth public void setRate(int)
meth public void setRepeating(boolean)
meth public void setStep(int)
meth public void setValue(int)
meth public void switchStop()
supr java.awt.Canvas
hfds SPIN_DOWN,SPIN_UP,serialVersionUID,spinButtonListeners,valueSupport

CLSS protected final org.openide.awt.SpinButton$RepeatThread
 outer org.openide.awt.SpinButton
 anno 0 java.lang.Deprecated()
meth public void run()
supr java.lang.Thread
hfds finish

CLSS public org.openide.awt.SpinButtonAdapter
 anno 0 java.lang.Deprecated()
cons public init()
intf org.openide.awt.SpinButtonListener
meth public void changeValue()
meth public void moveDown()
meth public void moveUp()
supr java.lang.Object

CLSS public abstract interface org.openide.awt.SpinButtonListener
 anno 0 java.lang.Deprecated()
meth public abstract void changeValue()
meth public abstract void moveDown()
meth public abstract void moveUp()

CLSS public org.openide.awt.SplittedPanel
 anno 0 java.lang.Deprecated()
cons public init()
fld public final static int DEFAULT_SPLITTER = 0
fld public final static int EMPTY_SPLITTER = 1
fld public final static int FIRST_PREFERRED = -1
fld public final static int HORIZONTAL = 2
fld public final static int NONE = 0
fld public final static int RAISED_SPLITTER = 0
fld public final static int SECOND_PREFERRED = -2
fld public final static int VERTICAL = 1
fld public final static java.lang.Object ADD_BOTTOM
fld public final static java.lang.Object ADD_FIRST
fld public final static java.lang.Object ADD_LEFT
fld public final static java.lang.Object ADD_RIGHT
fld public final static java.lang.Object ADD_SECOND
fld public final static java.lang.Object ADD_SPLITTER
fld public final static java.lang.Object ADD_TOP
innr public abstract interface static SplitChangeListener
innr public static EmptySplitter
innr public static SplitChangeEvent
intf javax.accessibility.Accessible
meth protected void computeSizesAfterFlip()
meth protected void fireSplitChange(int,int)
meth protected void setSplitterCursor()
meth protected void updatePopupMenu()
meth protected void updateSplitting()
meth public boolean getKeepFirstSame()
meth public boolean getKeepSecondSame()
meth public boolean getPanesSwapped()
meth public boolean isContinuousLayout()
meth public boolean isSplitAbsolute()
meth public boolean isSplitDragable()
meth public boolean isSplitTypeChangeEnabled()
meth public boolean isSwapPanesEnabled()
meth public int getSplitPosition()
meth public int getSplitType()
meth public int getSplitterType()
meth public java.awt.Component getSplitterComponent()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public void addSplitChangeListener(org.openide.awt.SplittedPanel$SplitChangeListener)
meth public void removeSplitChangeListener(org.openide.awt.SplittedPanel$SplitChangeListener)
meth public void setContinuousLayout(boolean)
meth public void setKeepFirstSame(boolean)
meth public void setKeepSecondSame(boolean)
meth public void setSplitAbsolute(boolean)
meth public void setSplitDragable(boolean)
meth public void setSplitPosition(int)
meth public void setSplitType(int)
meth public void setSplitTypeChangeEnabled(boolean)
meth public void setSplitterComponent(java.awt.Component)
meth public void setSplitterType(int)
meth public void setSwapPanesEnabled(boolean)
meth public void swapPanes()
meth public void updateUI()
supr javax.swing.JComponent
hfds DEFAULT_SPLIT_TYPE,absolute,accessibleContext,continuousLayout,descriptionFormat,dragPos,dragable,drawBumps,firstComponent,horizontalCMI,keepFirstSame,keepSecondSame,listeners,mouseAdapter,nameFormat,panesSwapped,popupMenu,popupMenuEnabled,resetPosition,secondComponent,serialVersionUID,splitIsChanging,splitPosition,splitType,splitTypeChangeEnabled,splitter,splitterCMI,splitterType,swapCMI,swapPanesEnabled,verticalCMI
hcls DefaultSplitter,MouseListenerAdapter,SplitLayout

CLSS public static org.openide.awt.SplittedPanel$EmptySplitter
 outer org.openide.awt.SplittedPanel
 anno 0 java.lang.Deprecated()
cons public init()
cons public init(int)
intf javax.accessibility.Accessible
meth public java.awt.Dimension getPreferredSize()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
supr javax.swing.JComponent
hfds accessibleContext,serialVersionUID,width

CLSS public static org.openide.awt.SplittedPanel$SplitChangeEvent
 outer org.openide.awt.SplittedPanel
 anno 0 java.lang.Deprecated()
cons public init(org.openide.awt.SplittedPanel,int,int)
meth public int getNewValue()
meth public int getOldValue()
supr java.util.EventObject
hfds newValue,oldValue,serialVersionUID

CLSS public abstract interface static org.openide.awt.SplittedPanel$SplitChangeListener
 outer org.openide.awt.SplittedPanel
meth public abstract void splitChanged(org.openide.awt.SplittedPanel$SplitChangeEvent)

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

CLSS public abstract interface static org.openide.awt.StatusDisplayer$Message
 outer org.openide.awt.StatusDisplayer
meth public abstract void clear(int)

CLSS public abstract interface org.openide.awt.StatusLineElementProvider
meth public abstract java.awt.Component getStatusLineElement()

CLSS public org.openide.awt.TabbedPaneFactory
cons public init()
fld public final static java.lang.String NO_CLOSE_BUTTON = "noCloseButton"
fld public final static java.lang.String PROP_CLOSE = "close"
meth public javax.swing.JTabbedPane createTabbedPane()
meth public static javax.swing.JTabbedPane createCloseButtonTabbedPane()
meth public static org.openide.awt.TabbedPaneFactory getDefault()
supr java.lang.Object

CLSS public org.openide.awt.Toolbar
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,boolean)
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,boolean)
fld public final static int BASIC_HEIGHT = 34
 anno 0 java.lang.Deprecated()
innr public abstract interface static DnDListener
innr public static DnDEvent
meth protected void addImpl(java.awt.Component,java.lang.Object,int)
meth protected void fireDragToolbar(int,int,int)
 anno 0 java.lang.Deprecated()
meth protected void fireDropToolbar(int,int,int)
 anno 0 java.lang.Deprecated()
meth public boolean isOpaque()
meth public java.awt.Component[] getComponents()
meth public java.lang.String getDisplayName()
meth public java.lang.String getUIClassID()
meth public static int getBasicHeight()
 anno 0 java.lang.Deprecated()
meth public static int rowCount(int)
 anno 0 java.lang.Deprecated()
meth public void addNotify()
meth public void setDisplayName(java.lang.String)
meth public void setDnDListener(org.openide.awt.Toolbar$DnDListener)
 anno 0 java.lang.Deprecated()
meth public void setUI(javax.swing.plaf.ToolBarUI)
meth public void setVisible(boolean)
supr org.openide.awt.ToolbarWithOverflow
hfds LOG,backingFolder,displayName,emptyAction,emptyInsets,isFlatLaF,isMetalLaF,isWindowsLaF,label,processor,serialVersionUID
hcls DefaultIconButton,DefaultIconToggleButton,Folder

CLSS public static org.openide.awt.Toolbar$DnDEvent
 outer org.openide.awt.Toolbar
 anno 0 java.lang.Deprecated()
cons public init(org.openide.awt.Toolbar,java.lang.String,int,int,int)
fld public final static int DND_END = 2
fld public final static int DND_LINE = 3
fld public final static int DND_ONE = 1
meth public int getDX()
meth public int getDY()
meth public int getType()
meth public java.lang.String getName()
supr java.util.EventObject
hfds dx,dy,name,serialVersionUID,type

CLSS public abstract interface static org.openide.awt.Toolbar$DnDListener
 outer org.openide.awt.Toolbar
 anno 0 java.lang.Deprecated()
intf java.util.EventListener
meth public abstract void dragToolbar(org.openide.awt.Toolbar$DnDEvent)
meth public abstract void dropToolbar(org.openide.awt.Toolbar$DnDEvent)

CLSS public org.openide.awt.ToolbarButton
 anno 0 java.lang.Deprecated()
cons public init()
cons public init(javax.swing.Icon)
meth public void processMouseEvent(java.awt.event.MouseEvent)
supr javax.swing.JButton
hfds serialVersionUID

CLSS public final org.openide.awt.ToolbarPool
cons public init(org.openide.loaders.DataFolder)
fld public final static java.lang.String DEFAULT_CONFIGURATION = "Standard"
innr public abstract interface static Configuration
intf javax.accessibility.Accessible
meth public final boolean isFinished()
meth public final java.lang.String getConfiguration()
meth public final java.lang.String[] getConfigurations()
meth public final org.openide.awt.Toolbar findToolbar(java.lang.String)
meth public final org.openide.awt.Toolbar[] getToolbars()
meth public final org.openide.loaders.DataFolder getFolder()
meth public final void setConfiguration(java.lang.String)
meth public final void waitFinished()
meth public int getPreferredIconSize()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public static org.openide.awt.ToolbarPool getDefault()
meth public void setPreferredIconSize(int)
meth public void setToolbarsListener(org.openide.awt.Toolbar$DnDListener)
 anno 0 java.lang.Deprecated()
supr javax.swing.JComponent
hfds DONT_WAIT,center,defaultPool,folder,instance,listener,name,preferredIconSize,serialVersionUID,taskListener,toolbarAccessibleContext,toolbarConfigs,toolbarNames,toolbars
hcls ComponentConfiguration,Folder,PopupListener,TPTaskListener

CLSS public abstract interface static org.openide.awt.ToolbarPool$Configuration
 outer org.openide.awt.ToolbarPool
meth public abstract java.awt.Component activate()
meth public abstract java.lang.String getName()
meth public abstract javax.swing.JPopupMenu getContextMenu()

CLSS public org.openide.awt.ToolbarToggleButton
 anno 0 java.lang.Deprecated()
cons public init()
cons public init(javax.swing.Icon)
cons public init(javax.swing.Icon,boolean)
supr javax.swing.JToggleButton
hfds serialVersionUID

CLSS public org.openide.awt.ToolbarWithOverflow
cons public init()
cons public init(int)
cons public init(java.lang.String)
cons public init(java.lang.String,int)
meth public boolean isDisplayOverflowOnHover()
meth public java.awt.Dimension getPreferredSize()
meth public void addNotify()
meth public void removeAll()
meth public void removeNotify()
meth public void setDisplayOverflowOnHover(boolean)
meth public void setOrientation(int)
meth public void updateUI()
meth public void validate()
supr javax.swing.JToolBar
hfds PROP_DRAGGER,PROP_JDEV_DISABLE_OVERFLOW,PROP_PREF_ICON_SIZE,awtEventListener,componentAdapter,displayOverflowOnHover,overflowButton,overflowToolbar,popup,showingPopup
hcls SafePopupMenu,SafeToolBar,ToolbarArrowIcon

CLSS public abstract interface org.openide.awt.UndoRedo
fld public final static org.openide.awt.UndoRedo NONE
innr public abstract interface static Provider
innr public final static Empty
innr public static Manager
meth public abstract boolean canRedo()
meth public abstract boolean canUndo()
meth public abstract java.lang.String getRedoPresentationName()
meth public abstract java.lang.String getUndoPresentationName()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void redo()
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public abstract void undo()

CLSS public final static org.openide.awt.UndoRedo$Empty
 outer org.openide.awt.UndoRedo
 anno 0 java.lang.Deprecated()
cons public init()
intf org.openide.awt.UndoRedo
meth public boolean canRedo()
meth public boolean canUndo()
meth public java.lang.String getRedoPresentationName()
meth public java.lang.String getUndoPresentationName()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void redo()
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void undo()
supr java.lang.Object

CLSS public static org.openide.awt.UndoRedo$Manager
 outer org.openide.awt.UndoRedo
cons public init()
intf org.openide.awt.UndoRedo
meth protected javax.swing.undo.UndoableEdit editToBeRedone()
meth protected javax.swing.undo.UndoableEdit editToBeUndone()
meth protected javax.swing.undo.UndoableEdit lastEdit()
meth protected void redoTo(javax.swing.undo.UndoableEdit)
meth protected void trimEdits(int,int)
meth protected void trimForLimit()
meth protected void undoTo(javax.swing.undo.UndoableEdit)
meth public boolean addEdit(javax.swing.undo.UndoableEdit)
meth public boolean canRedo()
meth public boolean canUndo()
meth public boolean canUndoOrRedo()
meth public boolean isInProgress()
meth public boolean isSignificant()
meth public boolean replaceEdit(javax.swing.undo.UndoableEdit)
meth public int getLimit()
meth public java.lang.String getPresentationName()
meth public java.lang.String getRedoPresentationName()
meth public java.lang.String getUndoOrRedoPresentationName()
meth public java.lang.String getUndoPresentationName()
meth public java.lang.String toString()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void die()
meth public void discardAllEdits()
meth public void end()
meth public void redo()
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void setLimit(int)
meth public void undo()
meth public void undoOrRedo()
meth public void undoableEditHappened(javax.swing.event.UndoableEditEvent)
supr javax.swing.undo.UndoManager
hfds alive,cs,hasBeenDone,inProgress,indexOfNextAdd,limit,serialVersionUID

CLSS public abstract interface static org.openide.awt.UndoRedo$Provider
 outer org.openide.awt.UndoRedo
meth public abstract org.openide.awt.UndoRedo getUndoRedo()

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

CLSS public abstract interface org.openide.cookies.EditorCookie
innr public abstract interface static Observable
intf org.openide.cookies.LineCookie
meth public abstract boolean close()
meth public abstract boolean isModified()
meth public abstract javax.swing.JEditorPane[] getOpenedPanes()
meth public abstract javax.swing.text.StyledDocument getDocument()
meth public abstract javax.swing.text.StyledDocument openDocument() throws java.io.IOException
meth public abstract org.openide.util.Task prepareDocument()
meth public abstract void open()
meth public abstract void saveDocument() throws java.io.IOException

CLSS public abstract interface static org.openide.cookies.EditorCookie$Observable
 outer org.openide.cookies.EditorCookie
fld public final static java.lang.String PROP_DOCUMENT = "document"
fld public final static java.lang.String PROP_MODIFIED = "modified"
fld public final static java.lang.String PROP_OPENED_PANES = "openedPanes"
fld public final static java.lang.String PROP_RELOADING = "reloading"
intf org.openide.cookies.EditorCookie
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

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

CLSS public abstract interface org.openide.cookies.LineCookie
intf org.openide.nodes.Node$Cookie
meth public abstract org.openide.text.Line$Set getLineSet()

CLSS public abstract interface org.openide.cookies.OpenCookie
intf org.netbeans.api.actions.Openable
intf org.openide.nodes.Node$Cookie

CLSS public abstract interface org.openide.cookies.PrintCookie
intf org.netbeans.api.actions.Printable
intf org.openide.nodes.Node$Cookie

CLSS public abstract interface org.openide.loaders.ChangeableDataFilter
intf org.openide.loaders.DataFilter
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public org.openide.loaders.ConnectionSupport
 anno 0 java.lang.Deprecated()
cons public init(org.openide.loaders.MultiDataObject$Entry,org.openide.cookies.ConnectionCookie$Type[])
intf org.openide.cookies.ConnectionCookie
meth public java.util.List<org.openide.cookies.ConnectionCookie$Type> getRegisteredTypes()
meth public java.util.Set listenersFor(org.openide.cookies.ConnectionCookie$Type)
meth public java.util.Set<org.openide.cookies.ConnectionCookie$Type> getTypes()
meth public void fireEvent(org.openide.cookies.ConnectionCookie$Event)
meth public void register(org.openide.cookies.ConnectionCookie$Type,org.openide.nodes.Node) throws java.io.IOException
meth public void unregister(org.openide.cookies.ConnectionCookie$Type,org.openide.nodes.Node) throws java.io.IOException
supr java.lang.Object
hfds EA_LISTENERS,entry,listeners,types,typesSet
hcls Pair

CLSS public abstract interface org.openide.loaders.CreateFromTemplateAttributesProvider
 anno 0 java.lang.Deprecated()
meth public abstract java.util.Map<java.lang.String,?> attributesFor(org.openide.loaders.DataObject,org.openide.loaders.DataFolder,java.lang.String)

CLSS public abstract org.openide.loaders.CreateFromTemplateHandler
 anno 0 java.lang.Deprecated()
cons public init()
fld public final static java.lang.String FREE_FILE_EXTENSION = "freeFileExtension"
meth protected abstract boolean accept(org.openide.filesystems.FileObject)
meth protected abstract org.openide.filesystems.FileObject createFromTemplate(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,java.lang.String,java.util.Map<java.lang.String,java.lang.Object>) throws java.io.IOException
meth protected java.util.List<org.openide.filesystems.FileObject> createFromTemplate(org.netbeans.api.templates.CreateDescriptor) throws java.io.IOException
meth public boolean accept(org.netbeans.api.templates.CreateDescriptor)
supr org.netbeans.api.templates.CreateFromTemplateHandler

CLSS public abstract interface org.openide.loaders.DataFilter
fld public final static long serialVersionUID = 0
 anno 0 java.lang.Deprecated()
fld public final static org.openide.loaders.DataFilter ALL
innr public abstract interface static FileBased
intf java.io.Serializable
meth public abstract boolean acceptDataObject(org.openide.loaders.DataObject)

CLSS public abstract interface static org.openide.loaders.DataFilter$FileBased
 outer org.openide.loaders.DataFilter
intf org.openide.loaders.DataFilter
meth public abstract boolean acceptFileObject(org.openide.filesystems.FileObject)

CLSS public org.openide.loaders.DataFolder
cons protected init(org.openide.filesystems.FileObject,org.openide.loaders.DataLoader) throws org.openide.loaders.DataObjectExistsException
 anno 0 java.lang.Deprecated()
cons protected init(org.openide.filesystems.FileObject,org.openide.loaders.MultiFileLoader) throws org.openide.loaders.DataObjectExistsException
cons public init(org.openide.filesystems.FileObject) throws org.openide.loaders.DataObjectExistsException
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String PROP_CHILDREN = "children"
fld public final static java.lang.String PROP_ORDER = "order"
fld public final static java.lang.String PROP_SORT_MODE = "sortMode"
fld public final static java.lang.String SET_SORTING = "sorting"
innr public FolderNode
innr public abstract static SortMode
innr public static Index
intf org.openide.loaders.DataObject$Container
meth protected org.openide.filesystems.FileObject handleMove(org.openide.loaders.DataFolder) throws java.io.IOException
meth protected org.openide.filesystems.FileObject handleRename(java.lang.String) throws java.io.IOException
meth protected org.openide.loaders.DataObject handleCopy(org.openide.loaders.DataFolder) throws java.io.IOException
meth protected org.openide.loaders.DataObject handleCreateFromTemplate(org.openide.loaders.DataFolder,java.lang.String) throws java.io.IOException
meth protected org.openide.loaders.DataShadow handleCreateShadow(org.openide.loaders.DataFolder) throws java.io.IOException
meth protected org.openide.nodes.Node createNodeDelegate()
meth protected void handleDelete() throws java.io.IOException
meth public boolean isCopyAllowed()
meth public boolean isDeleteAllowed()
meth public boolean isMoveAllowed()
meth public boolean isRenameAllowed()
meth public final org.openide.loaders.DataFolder$SortMode getSortMode()
meth public final void setOrder(org.openide.loaders.DataObject[]) throws java.io.IOException
meth public final void setSortMode(org.openide.loaders.DataFolder$SortMode) throws java.io.IOException
meth public java.lang.String getName()
meth public java.util.Enumeration<org.openide.loaders.DataObject> children()
meth public java.util.Enumeration<org.openide.loaders.DataObject> children(boolean)
meth public org.openide.loaders.DataObject[] getChildren()
meth public org.openide.nodes.Children createNodeChildren(org.openide.loaders.DataFilter)
meth public org.openide.util.HelpCtx getHelpCtx()
meth public org.openide.util.Lookup getLookup()
meth public static org.openide.loaders.DataFolder create(org.openide.loaders.DataFolder,java.lang.String) throws java.io.IOException
meth public static org.openide.loaders.DataFolder findFolder(org.openide.filesystems.FileObject)
meth public static org.openide.loaders.DataObject$Container findContainer(org.openide.filesystems.FileObject)
supr org.openide.loaders.MultiDataObject
hfds EA_ORDER,EA_SORT_MODE,FOLDER_ICON_BASE,IMGS,KEEP_ALIVE,ROOT_SHADOW_NAME,dataTransferSupport,list,pcl,serialVersionUID,uriListDataFlavor
hcls ClonedFilter,ClonedFilterHandle,ListPCL,NewFolder,Paste

CLSS public org.openide.loaders.DataFolder$FolderNode
 outer org.openide.loaders.DataFolder
cons protected init(org.openide.loaders.DataFolder)
cons public init(org.openide.loaders.DataFolder,org.openide.nodes.Children)
meth protected org.openide.nodes.Sheet createSheet()
meth protected void createPasteTypes(java.awt.datatransfer.Transferable,java.util.List<org.openide.util.datatransfer.PasteType>)
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public java.awt.Image getIcon(int)
meth public java.awt.Image getOpenedIcon(int)
meth public javax.swing.Action getPreferredAction()
meth public org.openide.util.datatransfer.NewType[] getNewTypes()
meth public void setName(java.lang.String)
supr org.openide.loaders.DataNode

CLSS public static org.openide.loaders.DataFolder$Index
 outer org.openide.loaders.DataFolder
cons public init(org.openide.loaders.DataFolder)
 anno 0 java.lang.Deprecated()
cons public init(org.openide.loaders.DataFolder,org.openide.nodes.Node)
meth public int getNodesCount()
meth public org.openide.nodes.Node[] getNodes()
meth public void reorder()
meth public void reorder(int[])
supr org.openide.nodes.Index$Support
hfds df,listener,node
hcls Listener

CLSS public abstract static org.openide.loaders.DataFolder$SortMode
 outer org.openide.loaders.DataFolder
cons public init()
fld public final static org.openide.loaders.DataFolder$SortMode CLASS
fld public final static org.openide.loaders.DataFolder$SortMode EXTENSIONS
fld public final static org.openide.loaders.DataFolder$SortMode FOLDER_NAMES
fld public final static org.openide.loaders.DataFolder$SortMode LAST_MODIFIED
fld public final static org.openide.loaders.DataFolder$SortMode NAMES
fld public final static org.openide.loaders.DataFolder$SortMode NATURAL
fld public final static org.openide.loaders.DataFolder$SortMode NONE
fld public final static org.openide.loaders.DataFolder$SortMode SIZE
intf java.util.Comparator<org.openide.loaders.DataObject>
supr java.lang.Object

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

CLSS public abstract interface static org.openide.loaders.DataLoader$RecognizedFiles
 outer org.openide.loaders.DataLoader
meth public abstract void markRecognized(org.openide.filesystems.FileObject)

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

CLSS public abstract interface static org.openide.loaders.DataObject$Container
 outer org.openide.loaders.DataObject
fld public final static java.lang.String PROP_CHILDREN = "children"
intf org.openide.nodes.Node$Cookie
meth public abstract org.openide.loaders.DataObject[] getChildren()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public abstract interface static org.openide.loaders.DataObject$Factory
 outer org.openide.loaders.DataObject
meth public abstract org.openide.loaders.DataObject findDataObject(org.openide.filesystems.FileObject,java.util.Set<? super org.openide.filesystems.FileObject>) throws java.io.IOException

CLSS public abstract interface static !annotation org.openide.loaders.DataObject$Registration
 outer org.openide.loaders.DataObject
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()
meth public abstract !hasdefault java.lang.String displayName()
meth public abstract !hasdefault java.lang.String iconBase()
meth public abstract java.lang.String mimeType()

CLSS public abstract interface static !annotation org.openide.loaders.DataObject$Registrations
 outer org.openide.loaders.DataObject
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract org.openide.loaders.DataObject$Registration[] value()

CLSS public final static org.openide.loaders.DataObject$Registry
 outer org.openide.loaders.DataObject
meth public java.util.Set<org.openide.loaders.DataObject> getModifiedSet()
meth public org.openide.loaders.DataObject[] getModified()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object

CLSS public org.openide.loaders.DataObjectExistsException
cons public init(org.openide.loaders.DataObject)
meth public java.lang.Throwable fillInStackTrace()
meth public org.openide.loaders.DataObject getDataObject()
supr java.io.IOException
hfds obj,serialVersionUID

CLSS public org.openide.loaders.DataObjectNotFoundException
cons public init(org.openide.filesystems.FileObject)
meth public org.openide.filesystems.FileObject getFileObject()
supr java.io.IOException
hfds obj,serialVersionUID

CLSS public org.openide.loaders.DataShadow
cons protected init(org.openide.filesystems.FileObject,org.openide.loaders.DataObject,org.openide.loaders.DataLoader) throws org.openide.loaders.DataObjectExistsException
 anno 0 java.lang.Deprecated()
cons protected init(org.openide.filesystems.FileObject,org.openide.loaders.DataObject,org.openide.loaders.MultiFileLoader) throws org.openide.loaders.DataObjectExistsException
innr protected static ShadowNode
intf org.openide.loaders.DataObject$Container
meth protected org.openide.filesystems.FileObject handleMove(org.openide.loaders.DataFolder) throws java.io.IOException
meth protected org.openide.loaders.DataObject handleCopy(org.openide.loaders.DataFolder) throws java.io.IOException
meth protected org.openide.loaders.DataShadow handleCreateShadow(org.openide.loaders.DataFolder) throws java.io.IOException
meth protected org.openide.nodes.Node createNodeDelegate()
meth protected static org.openide.loaders.DataObject deserialize(org.openide.filesystems.FileObject) throws java.io.IOException
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public boolean isCopyAllowed()
meth public boolean isDeleteAllowed()
meth public boolean isMoveAllowed()
meth public boolean isRenameAllowed()
meth public org.openide.loaders.DataObject getOriginal()
meth public org.openide.loaders.DataObject[] getChildren()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public org.openide.util.Lookup getLookup()
meth public static org.openide.filesystems.FileObject findOriginal(org.openide.filesystems.FileObject) throws java.io.IOException
meth public static org.openide.loaders.DataShadow create(org.openide.loaders.DataFolder,java.lang.String,org.openide.loaders.DataObject) throws java.io.IOException
meth public static org.openide.loaders.DataShadow create(org.openide.loaders.DataFolder,java.lang.String,org.openide.loaders.DataObject,java.lang.String) throws java.io.IOException
meth public static org.openide.loaders.DataShadow create(org.openide.loaders.DataFolder,org.openide.loaders.DataObject) throws java.io.IOException
meth public void refresh()
supr org.openide.loaders.MultiDataObject
hfds MUTEX,RP,SFS_NAME,SHADOW_EXTENSION,allDataShadows,lastTask,lookup,nodes,origL,original,serialVersionUID
hcls CreateShadow,DSLookup,DSWeakReference,OrigL

CLSS protected static org.openide.loaders.DataShadow$ShadowNode
 outer org.openide.loaders.DataShadow
cons public init(org.openide.loaders.DataShadow)
meth protected org.openide.nodes.NodeListener createNodeListener()
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public boolean canDestroy()
meth public boolean equals(java.lang.Object)
meth public final boolean canCopy()
meth public final boolean canCut()
meth public final boolean canRename()
meth public int hashCode()
meth public java.awt.Image getIcon(int)
meth public java.awt.Image getOpenedIcon(int)
meth public java.awt.datatransfer.Transferable clipboardCopy() throws java.io.IOException
meth public java.awt.datatransfer.Transferable clipboardCut() throws java.io.IOException
meth public java.awt.datatransfer.Transferable drag() throws java.io.IOException
meth public java.lang.String getDisplayName()
meth public java.lang.String getHtmlDisplayName()
meth public java.lang.String getName()
meth public java.lang.String getShortDescription()
meth public org.openide.nodes.Node cloneNode()
meth public org.openide.nodes.Node$PropertySet[] getPropertySets()
meth public void destroy() throws java.io.IOException
meth public void setName(java.lang.String)
supr org.openide.nodes.FilterNode
hfds ATTR_USEOWNNAME,descriptionFormat,format,obj,sheet
hcls Name,PropL

CLSS public final org.openide.loaders.Environment
innr public abstract interface static Provider
meth public static javax.naming.Context findSettingsContext(org.openide.loaders.DataObject)
 anno 0 java.lang.Deprecated()
meth public static org.openide.util.Lookup find(org.openide.loaders.DataObject)
supr java.lang.Object
hfds result

CLSS public abstract interface static org.openide.loaders.Environment$Provider
 outer org.openide.loaders.Environment
meth public abstract org.openide.util.Lookup getEnvironment(org.openide.loaders.DataObject)

CLSS public org.openide.loaders.ExtensionList
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public boolean isRegistered(java.lang.String)
meth public boolean isRegistered(org.openide.filesystems.FileObject)
meth public int hashCode()
meth public java.lang.Object clone()
meth public java.lang.String toString()
meth public java.util.Enumeration<java.lang.String> extensions()
meth public java.util.Enumeration<java.lang.String> mimeTypes()
meth public void addExtension(java.lang.String)
meth public void addMimeType(java.lang.String)
meth public void removeExtension(java.lang.String)
meth public void removeMimeType(java.lang.String)
supr java.lang.Object
hfds CASE_INSENSITIVE,list,mimeTypes,serialVersionUID

CLSS public org.openide.loaders.FileEntry
cons public init(org.openide.loaders.MultiDataObject,org.openide.filesystems.FileObject)
innr public abstract static Format
innr public final static Folder
innr public final static Numb
meth public org.openide.filesystems.FileObject copy(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public org.openide.filesystems.FileObject copyRename(org.openide.filesystems.FileObject,java.lang.String,java.lang.String) throws java.io.IOException
meth public org.openide.filesystems.FileObject createFromTemplate(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public org.openide.filesystems.FileObject move(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public org.openide.filesystems.FileObject rename(java.lang.String) throws java.io.IOException
meth public void delete() throws java.io.IOException
supr org.openide.loaders.MultiDataObject$Entry
hfds serialVersionUID

CLSS public final static org.openide.loaders.FileEntry$Folder
 outer org.openide.loaders.FileEntry
cons public init(org.openide.loaders.MultiDataObject,org.openide.filesystems.FileObject)
meth public org.openide.filesystems.FileObject copy(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public org.openide.filesystems.FileObject createFromTemplate(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public org.openide.filesystems.FileObject move(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public org.openide.filesystems.FileObject rename(java.lang.String) throws java.io.IOException
meth public void delete() throws java.io.IOException
supr org.openide.loaders.MultiDataObject$Entry

CLSS public abstract static org.openide.loaders.FileEntry$Format
 outer org.openide.loaders.FileEntry
cons public init(org.openide.loaders.MultiDataObject,org.openide.filesystems.FileObject)
meth protected abstract java.text.Format createFormat(org.openide.filesystems.FileObject,java.lang.String,java.lang.String)
meth public org.openide.filesystems.FileObject createFromTemplate(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
supr org.openide.loaders.FileEntry
hfds serialVersionUID

CLSS public final static org.openide.loaders.FileEntry$Numb
 outer org.openide.loaders.FileEntry
cons public init(org.openide.loaders.MultiDataObject,org.openide.filesystems.FileObject)
meth public boolean isImportant()
meth public org.openide.filesystems.FileObject copy(org.openide.filesystems.FileObject,java.lang.String)
meth public org.openide.filesystems.FileObject createFromTemplate(org.openide.filesystems.FileObject,java.lang.String)
meth public org.openide.filesystems.FileObject move(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public org.openide.filesystems.FileObject rename(java.lang.String) throws java.io.IOException
meth public void delete() throws java.io.IOException
supr org.openide.loaders.MultiDataObject$Entry
hfds serialVersionUID

CLSS public abstract org.openide.loaders.FolderInstance
cons public init(org.openide.loaders.DataFolder)
cons public init(org.openide.loaders.DataObject$Container)
fld protected org.openide.loaders.DataFolder folder
intf org.openide.cookies.InstanceCookie
meth protected abstract java.lang.Object createInstance(org.openide.cookies.InstanceCookie[]) throws java.io.IOException,java.lang.ClassNotFoundException
meth protected java.lang.Object instanceForCookie(org.openide.loaders.DataObject,org.openide.cookies.InstanceCookie) throws java.io.IOException,java.lang.ClassNotFoundException
meth protected org.openide.cookies.InstanceCookie acceptContainer(org.openide.loaders.DataObject$Container)
meth protected org.openide.cookies.InstanceCookie acceptCookie(org.openide.cookies.InstanceCookie) throws java.io.IOException,java.lang.ClassNotFoundException
meth protected org.openide.cookies.InstanceCookie acceptDataObject(org.openide.loaders.DataObject)
meth protected org.openide.cookies.InstanceCookie acceptFolder(org.openide.loaders.DataFolder)
meth protected org.openide.util.Task postCreationTask(java.lang.Runnable)
meth public final void instanceFinished()
meth public java.lang.Class<?> instanceClass() throws java.io.IOException,java.lang.ClassNotFoundException
meth public java.lang.Object instanceCreate() throws java.io.IOException,java.lang.ClassNotFoundException
meth public java.lang.String instanceName()
meth public java.lang.String toString()
meth public void recreate()
meth public void run()
meth public void waitFinished()
supr org.openide.util.Task
hfds CURRENT,LAST_CURRENT,PROCESSOR,container,creationSequence,creationTask,err,listener,map,object,precreateInstances,recognizingTask,waitFor
hcls HoldInstance,Listener

CLSS public org.openide.loaders.FolderLookup
 anno 0 java.lang.Deprecated()
cons public init(org.openide.loaders.DataObject$Container)
cons public init(org.openide.loaders.DataObject$Container,java.lang.String)
meth protected final java.lang.Object createInstance(org.openide.cookies.InstanceCookie[]) throws java.io.IOException,java.lang.ClassNotFoundException
meth protected final org.openide.util.Task postCreationTask(java.lang.Runnable)
meth protected java.lang.Object instanceForCookie(org.openide.loaders.DataObject,org.openide.cookies.InstanceCookie) throws java.io.IOException,java.lang.ClassNotFoundException
meth protected org.openide.cookies.InstanceCookie acceptContainer(org.openide.loaders.DataObject$Container)
meth protected org.openide.cookies.InstanceCookie acceptFolder(org.openide.loaders.DataFolder)
meth public final java.lang.Class<?> instanceClass()
meth public final org.openide.util.Lookup getLookup()
supr org.openide.loaders.FolderInstance
hfds LOCK,isRoot,lookup,notified,rootName
hcls Dispatch,FolderLookupData,ICItem,ProxyLkp

CLSS public abstract interface org.openide.loaders.FolderRenameHandler
meth public abstract void handleRename(org.openide.loaders.DataFolder,java.lang.String)

CLSS public org.openide.loaders.InstanceDataObject
cons public init(org.openide.filesystems.FileObject,org.openide.loaders.MultiFileLoader) throws org.openide.loaders.DataObjectExistsException
fld public final static java.lang.String INSTANCE = "instance"
intf org.openide.cookies.InstanceCookie$Of
meth protected org.openide.filesystems.FileObject handleRename(java.lang.String) throws java.io.IOException
meth protected org.openide.loaders.DataObject handleCopy(org.openide.loaders.DataFolder) throws java.io.IOException
meth protected org.openide.loaders.DataObject handleCreateFromTemplate(org.openide.loaders.DataFolder,java.lang.String) throws java.io.IOException
meth protected org.openide.nodes.Node createNodeDelegate()
meth protected void dispose()
meth protected void handleDelete() throws java.io.IOException
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public boolean instanceOf(java.lang.Class<?>)
meth public java.lang.Class<?> instanceClass() throws java.io.IOException,java.lang.ClassNotFoundException
meth public java.lang.Object instanceCreate() throws java.io.IOException,java.lang.ClassNotFoundException
meth public java.lang.String getName()
meth public java.lang.String instanceName()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public org.openide.util.Lookup getLookup()
meth public static boolean remove(org.openide.loaders.DataFolder,java.lang.String,java.lang.Class<?>)
meth public static boolean remove(org.openide.loaders.DataFolder,java.lang.String,java.lang.String)
meth public static org.openide.loaders.InstanceDataObject create(org.openide.loaders.DataFolder,java.lang.String,java.lang.Class<?>) throws java.io.IOException
meth public static org.openide.loaders.InstanceDataObject create(org.openide.loaders.DataFolder,java.lang.String,java.lang.Object,org.openide.modules.ModuleInfo) throws java.io.IOException
meth public static org.openide.loaders.InstanceDataObject create(org.openide.loaders.DataFolder,java.lang.String,java.lang.Object,org.openide.modules.ModuleInfo,boolean) throws java.io.IOException
meth public static org.openide.loaders.InstanceDataObject create(org.openide.loaders.DataFolder,java.lang.String,java.lang.String) throws java.io.IOException
meth public static org.openide.loaders.InstanceDataObject find(org.openide.loaders.DataFolder,java.lang.String,java.lang.Class<?>)
meth public static org.openide.loaders.InstanceDataObject find(org.openide.loaders.DataFolder,java.lang.String,java.lang.String)
supr org.openide.loaders.MultiDataObject
hfds CLOSE,EA_INSTANCE_CLASS,EA_INSTANCE_CREATE,EA_INSTANCE_OF,EA_NAME,EA_PROVIDER_PATH,EA_SUBCLASSES,ICON_NAME,IDO_LOCK,INIT_LOOKUP,MAX_FILENAME_LENGTH,OPEN,PROCESSOR,SAVE_DELAY,SER_EXT,XML_EXT,cookieResult,cookiesLkp,cookiesLsnr,createdIDOs,err,fileLock,lkp,nameCache,nodeLsnr,nodeResult,savingCanceled,ser,serialVersionUID,un,warnedAboutBrackets
hcls CookieAdjustingFilter,Creator,Creator2,FileObjectContext,Ser,UnrecognizedSettingNode,UpdatableNode,WriterProvider

CLSS public org.openide.loaders.InstanceSupport
cons public init(org.openide.loaders.MultiDataObject$Entry)
innr public static Instance
intf org.openide.cookies.InstanceCookie$Of
meth protected java.lang.ClassLoader createClassLoader()
meth public boolean instanceOf(java.lang.Class<?>)
meth public boolean isApplet()
 anno 0 java.lang.Deprecated()
meth public boolean isExecutable()
 anno 0 java.lang.Deprecated()
meth public boolean isInterface()
 anno 0 java.lang.Deprecated()
meth public boolean isJavaBean()
 anno 0 java.lang.Deprecated()
meth public java.lang.Class<?> instanceClass() throws java.io.IOException,java.lang.ClassNotFoundException
meth public java.lang.Object instanceCreate() throws java.io.IOException,java.lang.ClassNotFoundException
meth public java.lang.String instanceName()
meth public java.lang.String toString()
meth public org.openide.filesystems.FileObject instanceOrigin()
meth public static org.openide.util.HelpCtx findHelp(org.openide.cookies.InstanceCookie)
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds applet,bean,clazz,clazzException,entry,writeRepl
hcls ClassEx

CLSS public static org.openide.loaders.InstanceSupport$Instance
 outer org.openide.loaders.InstanceSupport
cons public init(java.lang.Object)
intf org.openide.cookies.InstanceCookie$Of
meth public boolean instanceOf(java.lang.Class<?>)
meth public java.lang.Class<?> instanceClass()
meth public java.lang.Object instanceCreate()
meth public java.lang.String instanceName()
supr java.lang.Object
hfds obj

CLSS public abstract org.openide.loaders.LoaderTransfer
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
meth public static org.openide.loaders.DataObject getDataObject(java.awt.datatransfer.Transferable,int)
meth public static org.openide.loaders.DataObject[] getDataObjects(java.awt.datatransfer.Transferable,int)
meth public static org.openide.util.datatransfer.ExTransferable$Single transferable(org.openide.loaders.DataObject,int)
supr java.lang.Object
hfds dndMimeType

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

CLSS public abstract org.openide.loaders.MultiDataObject$Entry
 outer org.openide.loaders.MultiDataObject
cons protected init(org.openide.loaders.MultiDataObject,org.openide.filesystems.FileObject)
intf java.io.Serializable
meth protected java.lang.Object writeReplace()
meth public abstract org.openide.filesystems.FileObject copy(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public abstract org.openide.filesystems.FileObject createFromTemplate(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public abstract org.openide.filesystems.FileObject move(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public abstract org.openide.filesystems.FileObject rename(java.lang.String) throws java.io.IOException
meth public abstract void delete() throws java.io.IOException
meth public boolean equals(java.lang.Object)
meth public boolean isImportant()
meth public boolean isLocked()
meth public final org.openide.filesystems.FileObject getFile()
meth public final org.openide.loaders.MultiDataObject getDataObject()
meth public int hashCode()
meth public org.openide.filesystems.FileLock takeLock() throws java.io.IOException
meth public org.openide.filesystems.FileObject copyRename(org.openide.filesystems.FileObject,java.lang.String,java.lang.String) throws java.io.IOException
supr java.lang.Object
hfds file,lock,serialVersionUID

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

CLSS public abstract org.openide.loaders.OpenSupport
cons protected init(org.openide.loaders.MultiDataObject$Entry,org.openide.loaders.OpenSupport$Env)
cons public init(org.openide.loaders.MultiDataObject$Entry)
fld protected org.openide.loaders.MultiDataObject$Entry entry
innr public static Env
meth protected java.lang.String messageOpened()
meth protected java.lang.String messageOpening()
supr org.openide.windows.CloneableOpenSupport
hcls FileSystemNameListener,Listener

CLSS public static org.openide.loaders.OpenSupport$Env
 outer org.openide.loaders.OpenSupport
cons public init(org.openide.loaders.DataObject)
intf java.beans.PropertyChangeListener
intf java.beans.VetoableChangeListener
intf java.io.Serializable
intf org.openide.windows.CloneableOpenSupport$Env
meth protected final org.openide.loaders.DataObject getDataObject()
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void fireVetoableChange(java.lang.String,java.lang.Object,java.lang.Object) throws java.beans.PropertyVetoException
meth public boolean isModified()
meth public boolean isValid()
meth public java.lang.String toString()
meth public org.openide.windows.CloneableOpenSupport findCloneableOpenSupport()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addVetoableChangeListener(java.beans.VetoableChangeListener)
meth public void markModified() throws java.io.IOException
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removeVetoableChangeListener(java.beans.VetoableChangeListener)
meth public void unmarkModified()
meth public void vetoableChange(java.beans.PropertyChangeEvent) throws java.beans.PropertyVetoException
supr java.lang.Object
hfds LOCK_SUPPORT,fsListenerMap,obj,propSupp,serialVersionUID,vetoSupp

CLSS public org.openide.loaders.OperationAdapter
cons public init()
intf org.openide.loaders.OperationListener
meth public void operationCopy(org.openide.loaders.OperationEvent$Copy)
meth public void operationCreateFromTemplate(org.openide.loaders.OperationEvent$Copy)
meth public void operationCreateShadow(org.openide.loaders.OperationEvent$Copy)
meth public void operationDelete(org.openide.loaders.OperationEvent)
meth public void operationMove(org.openide.loaders.OperationEvent$Move)
meth public void operationPostCreate(org.openide.loaders.OperationEvent)
meth public void operationRename(org.openide.loaders.OperationEvent$Rename)
supr java.lang.Object

CLSS public org.openide.loaders.OperationEvent
innr public final static Copy
innr public final static Move
innr public final static Rename
meth public java.lang.String toString()
meth public org.openide.loaders.DataObject getObject()
supr java.util.EventObject
hfds COPY,CREATE,DELETE,MOVE,RENAME,SHADOW,TEMPL,obj,pl,serialVersionUID

CLSS public final static org.openide.loaders.OperationEvent$Copy
 outer org.openide.loaders.OperationEvent
meth public org.openide.loaders.DataObject getOriginalDataObject()
supr org.openide.loaders.OperationEvent
hfds orig,serialVersionUID

CLSS public final static org.openide.loaders.OperationEvent$Move
 outer org.openide.loaders.OperationEvent
meth public org.openide.filesystems.FileObject getOriginalPrimaryFile()
supr org.openide.loaders.OperationEvent
hfds file,serialVersionUID

CLSS public final static org.openide.loaders.OperationEvent$Rename
 outer org.openide.loaders.OperationEvent
meth public java.lang.String getOriginalName()
supr org.openide.loaders.OperationEvent
hfds name,serialVersionUID

CLSS public abstract interface org.openide.loaders.OperationListener
intf java.util.EventListener
meth public abstract void operationCopy(org.openide.loaders.OperationEvent$Copy)
meth public abstract void operationCreateFromTemplate(org.openide.loaders.OperationEvent$Copy)
meth public abstract void operationCreateShadow(org.openide.loaders.OperationEvent$Copy)
meth public abstract void operationDelete(org.openide.loaders.OperationEvent)
meth public abstract void operationMove(org.openide.loaders.OperationEvent$Move)
meth public abstract void operationPostCreate(org.openide.loaders.OperationEvent)
meth public abstract void operationRename(org.openide.loaders.OperationEvent$Rename)

CLSS public abstract org.openide.loaders.RepositoryNodeFactory
 anno 0 java.lang.Deprecated()
cons protected init()
meth public abstract org.openide.nodes.Node repository(org.openide.loaders.DataFilter)
meth public static org.openide.loaders.RepositoryNodeFactory getDefault()
supr java.lang.Object
hcls Trivial

CLSS public abstract interface org.openide.loaders.SaveAsCapable
meth public abstract void saveAs(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException

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

CLSS public org.openide.loaders.XMLDataObject
cons protected init(org.openide.filesystems.FileObject,org.openide.loaders.MultiFileLoader,boolean) throws org.openide.loaders.DataObjectExistsException
cons public init(org.openide.filesystems.FileObject,org.openide.loaders.MultiFileLoader) throws org.openide.loaders.DataObjectExistsException
fld public final static int STATUS_ERROR = 3
fld public final static int STATUS_NOT = 0
fld public final static int STATUS_OK = 1
fld public final static int STATUS_WARNING = 2
fld public final static java.lang.String MIME = "text/xml"
fld public final static java.lang.String PROP_DOCUMENT = "document"
fld public final static java.lang.String PROP_INFO = "info"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String XMLINFO_DTD_PUBLIC_ID = "-//NetBeans IDE//DTD xmlinfo//EN"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String XMLINFO_DTD_PUBLIC_ID_FORTE = "-//Forte for Java//DTD xmlinfo//EN"
 anno 0 java.lang.Deprecated()
innr public abstract interface static Processor
innr public final static Info
meth protected org.openide.cookies.EditorCookie createEditorCookie()
 anno 0 java.lang.Deprecated()
meth protected org.openide.nodes.Node createNodeDelegate()
meth protected void handleDelete() throws java.io.IOException
meth protected void updateIconBase(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public final int getStatus()
meth public final org.openide.loaders.XMLDataObject$Info getInfo()
 anno 0 java.lang.Deprecated()
meth public final org.w3c.dom.Document getDocument() throws java.io.IOException,org.xml.sax.SAXException
meth public final void setInfo(org.openide.loaders.XMLDataObject$Info) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public org.openide.util.Lookup getLookup()
meth public static boolean addEntityResolver(org.xml.sax.EntityResolver)
 anno 0 java.lang.Deprecated()
meth public static org.openide.loaders.XMLDataObject$Info getRegisteredInfo(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static org.w3c.dom.Document createDocument()
 anno 0 java.lang.Deprecated()
meth public static org.w3c.dom.Document parse(java.net.URL) throws java.io.IOException,org.xml.sax.SAXException
 anno 0 java.lang.Deprecated()
meth public static org.w3c.dom.Document parse(java.net.URL,boolean) throws java.io.IOException,org.xml.sax.SAXException
 anno 0 java.lang.Deprecated()
meth public static org.w3c.dom.Document parse(java.net.URL,org.xml.sax.ErrorHandler) throws java.io.IOException,org.xml.sax.SAXException
 anno 0 java.lang.Deprecated()
meth public static org.w3c.dom.Document parse(java.net.URL,org.xml.sax.ErrorHandler,boolean) throws java.io.IOException,org.xml.sax.SAXException
 anno 0 java.lang.Deprecated()
meth public static org.xml.sax.EntityResolver removeEntityResolver(org.xml.sax.EntityResolver)
 anno 0 java.lang.Deprecated()
meth public static org.xml.sax.InputSource createInputSource(java.net.URL) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public static org.xml.sax.Parser createParser()
 anno 0 java.lang.Deprecated()
meth public static org.xml.sax.Parser createParser(boolean)
 anno 0 java.lang.Deprecated()
meth public static void registerCatalogEntry(java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static void registerCatalogEntry(java.lang.String,java.lang.String,java.lang.ClassLoader)
 anno 0 java.lang.Deprecated()
meth public static void registerInfo(java.lang.String,org.openide.loaders.XMLDataObject$Info)
 anno 0 java.lang.Deprecated()
meth public static void write(org.w3c.dom.Document,java.io.OutputStream,java.lang.String) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public static void write(org.w3c.dom.Document,java.io.Writer) throws java.io.IOException
 anno 0 java.lang.Deprecated()
supr org.openide.loaders.MultiDataObject
hfds ERR,chainingEntityResolver,cnstr,doc,editor,emgrLock,errorHandler,infoParser,infos,serialVersionUID,status
hcls DelDoc,ErrorPrinter,ICDel,InfoLkp,Loader,NullHandler,PlainDataNode,XMLEditorSupport,XMLNode

CLSS public final static org.openide.loaders.XMLDataObject$Info
 outer org.openide.loaders.XMLDataObject
 anno 0 java.lang.Deprecated()
cons public init()
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public boolean removeProcessorClass(java.lang.Class<?>)
meth public java.lang.Object clone()
meth public java.lang.String getIconBase()
meth public java.util.Iterator<java.lang.Class<?>> processorClasses()
meth public void addProcessorClass(java.lang.Class<?>)
meth public void setIconBase(java.lang.String)
meth public void write(java.io.Writer) throws java.io.IOException
supr java.lang.Object
hfds iconBase,processors

CLSS public abstract interface static org.openide.loaders.XMLDataObject$Processor
 outer org.openide.loaders.XMLDataObject
 anno 0 java.lang.Deprecated()
intf org.openide.nodes.Node$Cookie
meth public abstract void attachTo(org.openide.loaders.XMLDataObject)

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

CLSS public abstract interface org.openide.text.ActiveEditorDrop
fld public final static java.awt.datatransfer.DataFlavor FLAVOR
meth public abstract boolean handleTransfer(javax.swing.text.JTextComponent)

CLSS public abstract org.openide.text.Annotatable
cons public init()
fld public final static java.lang.String PROP_ANNOTATION_COUNT = "annotationCount"
fld public final static java.lang.String PROP_DELETED = "deleted"
fld public final static java.lang.String PROP_TEXT = "text"
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void addAnnotation(org.openide.text.Annotation)
meth protected void removeAnnotation(org.openide.text.Annotation)
meth public abstract java.lang.String getText()
meth public final boolean isDeleted()
meth public final int getAnnotationCount()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds attachedAnnotations,deleted,propertyChangeSupport

CLSS public abstract org.openide.text.Annotation
cons public init()
fld public final static java.lang.String PROP_ANNOTATION_TYPE = "annotationType"
fld public final static java.lang.String PROP_MOVE_TO_FRONT = "moveToFront"
fld public final static java.lang.String PROP_SHORT_DESCRIPTION = "shortDescription"
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void notifyAttached(org.openide.text.Annotatable)
meth protected void notifyDetached(org.openide.text.Annotatable)
meth public abstract java.lang.String getAnnotationType()
meth public abstract java.lang.String getShortDescription()
meth public final org.openide.text.Annotatable getAttachedAnnotatable()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void attach(org.openide.text.Annotatable)
meth public final void detach()
meth public final void moveToFront()
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds attached,inDocument,support

CLSS public abstract interface org.openide.text.AnnotationProvider
meth public abstract void annotate(org.openide.text.Line$Set,org.openide.util.Lookup)

CLSS public org.openide.text.AttributedCharacters
cons public init()
fld protected char[] chars
fld protected int current
fld protected int[] runLimit
fld protected int[] runStart
fld protected java.awt.Color[] colors
fld protected java.awt.Font[] fonts
innr public static AttributedCharacterIteratorImpl
meth public java.text.AttributedCharacterIterator iterator()
meth public void append(char,java.awt.Font,java.awt.Color)
meth public void append(char[],java.awt.Font,java.awt.Color)
supr java.lang.Object

CLSS public static org.openide.text.AttributedCharacters$AttributedCharacterIteratorImpl
 outer org.openide.text.AttributedCharacters
cons public init(char[],java.awt.Font[],java.awt.Color[],int[],int[])
fld protected char[] chars
fld protected int current
fld protected int[] runLimit
fld protected int[] runStart
fld protected java.awt.Color[] colors
fld protected java.awt.Font[] fonts
fld protected java.util.Set<java.text.AttributedCharacterIterator$Attribute> singleton
intf java.text.AttributedCharacterIterator
meth public char current()
meth public char first()
meth public char last()
meth public char next()
meth public char previous()
meth public char setIndex(int)
meth public int getBeginIndex()
meth public int getEndIndex()
meth public int getIndex()
meth public int getRunLimit()
meth public int getRunLimit(java.text.AttributedCharacterIterator$Attribute)
meth public int getRunLimit(java.util.Set<? extends java.text.AttributedCharacterIterator$Attribute>)
meth public int getRunStart()
meth public int getRunStart(java.text.AttributedCharacterIterator$Attribute)
meth public int getRunStart(java.util.Set<? extends java.text.AttributedCharacterIterator$Attribute>)
meth public java.lang.Object clone()
meth public java.lang.Object getAttribute(java.text.AttributedCharacterIterator$Attribute)
meth public java.util.Map<java.text.AttributedCharacterIterator$Attribute,java.lang.Object> getAttributes()
meth public java.util.Set<java.text.AttributedCharacterIterator$Attribute> getAllAttributeKeys()
supr java.lang.Object

CLSS public org.openide.text.CloneableEditor
cons public init()
cons public init(org.openide.text.CloneableEditorSupport)
cons public init(org.openide.text.CloneableEditorSupport,boolean)
fld protected javax.swing.JEditorPane pane
intf org.openide.text.CloneableEditorSupport$Pane
meth protected boolean closeLast()
meth protected final boolean closeLast(boolean)
meth protected final void initializeBySupport()
meth protected java.lang.Object readResolve() throws java.io.ObjectStreamException
meth protected java.lang.Object writeReplace() throws java.io.ObjectStreamException
meth protected java.lang.String preferredID()
meth protected org.openide.text.CloneableEditorSupport cloneableEditorSupport()
meth protected org.openide.windows.CloneableTopComponent createClonedObject()
meth protected void componentActivated()
meth protected void componentClosed()
meth protected void componentOpened()
meth protected void componentShowing()
meth public boolean canClose()
meth public boolean requestDefaultFocus()
 anno 0 java.lang.Deprecated()
meth public boolean requestFocusInWindow()
 anno 0 java.lang.Deprecated()
meth public int getPersistenceType()
meth public java.awt.Dimension getPreferredSize()
meth public javax.swing.Action[] getActions()
meth public javax.swing.JEditorPane getEditorPane()
meth public org.openide.awt.UndoRedo getUndoRedo()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public org.openide.windows.CloneableTopComponent getComponent()
meth public void ensureVisible()
meth public void open()
meth public void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void requestFocus()
 anno 0 java.lang.Deprecated()
meth public void updateName()
meth public void writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr org.openide.windows.CloneableTopComponent
hfds CLOSE_LAST_LOCK,HELP_ID,LOG,componentCreated,cursorPosition,customComponent,initializer,serialVersionUID,support

CLSS public abstract org.openide.text.CloneableEditorSupport
cons public init(org.openide.text.CloneableEditorSupport$Env)
cons public init(org.openide.text.CloneableEditorSupport$Env,org.openide.util.Lookup)
fld public final static java.lang.String EDITOR_MODE = "editor"
fld public final static javax.swing.undo.UndoableEdit BEGIN_COMMIT_GROUP
fld public final static javax.swing.undo.UndoableEdit END_COMMIT_GROUP
fld public final static javax.swing.undo.UndoableEdit MARK_COMMIT_GROUP
innr public abstract interface static Env
innr public abstract interface static Pane
meth protected abstract java.lang.String messageName()
meth protected abstract java.lang.String messageSave()
meth protected abstract java.lang.String messageToolTip()
meth protected boolean asynchronousOpen()
meth protected boolean canClose()
meth protected boolean close(boolean)
meth protected boolean notifyModified()
meth protected final org.openide.awt.UndoRedo$Manager getUndoRedo()
meth protected final org.openide.text.CloneableEditorSupport$Pane openAt(org.openide.text.PositionRef,int)
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected java.awt.Component wrapEditorComponent(java.awt.Component)
meth protected java.lang.String documentID()
meth protected java.lang.String messageHtmlName()
meth protected java.lang.String messageLine(org.openide.text.Line)
meth protected javax.swing.text.EditorKit createEditorKit()
meth protected javax.swing.text.StyledDocument createStyledDocument(javax.swing.text.EditorKit)
meth protected org.openide.awt.UndoRedo$Manager createUndoRedoManager()
meth protected org.openide.text.CloneableEditor createCloneableEditor()
meth protected org.openide.text.CloneableEditorSupport$Pane createPane()
meth protected org.openide.util.Task reloadDocument()
meth protected org.openide.windows.CloneableTopComponent createCloneableTopComponent()
meth protected void afterRedirect(org.openide.windows.CloneableOpenSupport)
meth protected void initializeCloneableEditor(org.openide.text.CloneableEditor)
meth protected void loadFromStreamToKit(javax.swing.text.StyledDocument,java.io.InputStream,javax.swing.text.EditorKit) throws java.io.IOException,javax.swing.text.BadLocationException
meth protected void notifyClosed()
meth protected void notifyUnmodified()
meth protected void saveFromKitToStream(javax.swing.text.StyledDocument,javax.swing.text.EditorKit,java.io.OutputStream) throws java.io.IOException,javax.swing.text.BadLocationException
meth protected void updateTitles()
meth public boolean isDocumentLoaded()
meth public boolean isModified()
meth public final org.openide.text.PositionRef createPositionRef(int,javax.swing.text.Position$Bias)
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public java.io.InputStream getInputStream() throws java.io.IOException
meth public java.lang.String toString()
meth public javax.swing.JEditorPane[] getOpenedPanes()
meth public javax.swing.text.StyledDocument getDocument()
meth public javax.swing.text.StyledDocument openDocument() throws java.io.IOException
meth public org.openide.text.Line$Set getLineSet()
meth public org.openide.util.Task prepareDocument()
meth public static javax.swing.text.EditorKit getEditorKit(java.lang.String)
meth public void addChangeListener(javax.swing.event.ChangeListener)
 anno 0 java.lang.Deprecated()
meth public void open()
meth public void print()
meth public void removeChangeListener(javax.swing.event.ChangeListener)
 anno 0 java.lang.Deprecated()
meth public void saveDocument() throws java.io.IOException
meth public void setMIMEType(java.lang.String)
supr org.openide.windows.CloneableOpenSupport
hfds ERR,LOCAL_CLOSE_DOCUMENT,LOCK_PRINTING,PROP_PANE,alreadyModified,annotationsLoaded,checkModificationLock,docFilter,inUserQuestionExceptionHandler,isSaving,kit,lastReusable,lastSaveTime,lastSelected,lineSet,lineSetLineVector,listener,listeners,listeningOnEnv,lookup,mimeType,openClose,positionManager,preventModification,printing,propertyChangeSupport,reloadDialogOpened,undoRedo,warnedClasses
hcls DocFilter,Listener,PlainEditorKit

CLSS public abstract interface static org.openide.text.CloneableEditorSupport$Env
 outer org.openide.text.CloneableEditorSupport
fld public final static java.lang.String PROP_TIME = "time"
intf org.openide.windows.CloneableOpenSupport$Env
meth public abstract java.io.InputStream inputStream() throws java.io.IOException
meth public abstract java.io.OutputStream outputStream() throws java.io.IOException
meth public abstract java.lang.String getMimeType()
meth public abstract java.util.Date getTime()

CLSS public abstract interface static org.openide.text.CloneableEditorSupport$Pane
 outer org.openide.text.CloneableEditorSupport
meth public abstract javax.swing.JEditorPane getEditorPane()
meth public abstract org.openide.windows.CloneableTopComponent getComponent()
meth public abstract void ensureVisible()
meth public abstract void updateName()

CLSS public abstract org.openide.text.CloneableEditorSupportRedirector
cons public init()
meth protected abstract org.openide.text.CloneableEditorSupport redirect(org.openide.util.Lookup)
supr java.lang.Object
hfds CHECKED

CLSS public org.openide.text.DataEditorSupport
cons public init(org.openide.loaders.DataObject,org.openide.text.CloneableEditorSupport$Env)
cons public init(org.openide.loaders.DataObject,org.openide.util.Lookup,org.openide.text.CloneableEditorSupport$Env)
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
innr public abstract static Env
meth protected boolean canClose()
meth protected java.lang.String documentID()
meth protected java.lang.String messageHtmlName()
meth protected java.lang.String messageLine(org.openide.text.Line)
meth protected java.lang.String messageName()
meth protected java.lang.String messageOpened()
meth protected java.lang.String messageOpening()
meth protected java.lang.String messageSave()
meth protected java.lang.String messageToolTip()
meth protected javax.swing.text.StyledDocument createStyledDocument(javax.swing.text.EditorKit)
meth protected void initializeCloneableEditor(org.openide.text.CloneableEditor)
meth protected void loadFromStreamToKit(javax.swing.text.StyledDocument,java.io.InputStream,javax.swing.text.EditorKit) throws java.io.IOException,javax.swing.text.BadLocationException
meth protected void notifyClosed()
meth protected void saveFromKitToStream(javax.swing.text.StyledDocument,javax.swing.text.EditorKit,java.io.OutputStream) throws java.io.IOException,javax.swing.text.BadLocationException
meth public final org.openide.loaders.DataObject getDataObject()
meth public javax.swing.text.StyledDocument openDocument() throws java.io.IOException
meth public static java.lang.String annotateName(java.lang.String,boolean,boolean,boolean)
meth public static java.lang.String toolTip(org.openide.filesystems.FileObject,boolean,boolean)
meth public static org.openide.loaders.DataObject findDataObject(org.openide.text.Line)
meth public static org.openide.text.CloneableEditorSupport create(org.openide.loaders.DataObject,org.openide.loaders.MultiDataObject$Entry,org.openide.nodes.CookieSet)
meth public static org.openide.text.CloneableEditorSupport create(org.openide.loaders.DataObject,org.openide.loaders.MultiDataObject$Entry,org.openide.nodes.CookieSet,java.util.concurrent.Callable<org.openide.text.CloneableEditorSupport$Pane>)
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
meth public void saveAs(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public void saveDocument() throws java.io.IOException
supr org.openide.text.CloneableEditorSupport
hfds ERR,TABNAMES_HTML,cacheCounter,charsets,nodeL,obj,warnedEncodingFiles
hcls DOEnvLookup,DataNodeListener,EnvListener,SaveImpl

CLSS public abstract static org.openide.text.DataEditorSupport$Env
 outer org.openide.text.DataEditorSupport
cons public init(org.openide.loaders.DataObject)
intf org.openide.text.CloneableEditorSupport$Env
meth protected abstract org.openide.filesystems.FileLock takeLock() throws java.io.IOException
meth protected abstract org.openide.filesystems.FileObject getFile()
meth protected final void changeFile()
meth public java.io.InputStream inputStream() throws java.io.IOException
meth public java.io.OutputStream outputStream() throws java.io.IOException
meth public java.lang.String getMimeType()
meth public java.util.Date getTime()
meth public void markModified() throws java.io.IOException
meth public void unmarkModified()
supr org.openide.loaders.OpenSupport$Env
hfds BIG_FILE_THRESHOLD_MB,action,canWrite,fileLock,fileObject,sentBigFileInfo,serialVersionUID,warnedFiles
hcls ME,SaveAsCapableImpl

CLSS public abstract org.openide.text.DocumentLine
cons public init(org.openide.util.Lookup,org.openide.text.PositionRef)
fld protected org.openide.text.PositionRef pos
innr public abstract static Set
meth protected void addAnnotation(org.openide.text.Annotation)
meth protected void removeAnnotation(org.openide.text.Annotation)
meth public abstract void show(int,int)
 anno 0 java.lang.Deprecated()
meth public boolean equals(java.lang.Object)
meth public boolean isBreakpoint()
 anno 0 java.lang.Deprecated()
meth public int getLineNumber()
meth public int hashCode()
meth public java.lang.String getText()
meth public void markCurrentLine()
 anno 0 java.lang.Deprecated()
meth public void markError()
 anno 0 java.lang.Deprecated()
meth public void setBreakpoint(boolean)
 anno 0 java.lang.Deprecated()
meth public void unmarkCurrentLine()
 anno 0 java.lang.Deprecated()
meth public void unmarkError()
 anno 0 java.lang.Deprecated()
supr org.openide.text.Line
hfds assigned,breakpoint,current,dlEqualsCounter,docL,error,lineParts,listener,serialVersionUID
hcls FindAnnotationPosition,LR,Part

CLSS public abstract static org.openide.text.DocumentLine$Set
 outer org.openide.text.DocumentLine
cons public init(javax.swing.text.StyledDocument)
meth protected abstract org.openide.text.Line createLine(int)
meth public int getOriginalLineNumber(org.openide.text.Line)
meth public java.util.List<? extends org.openide.text.Line> getLines()
meth public org.openide.text.Line getCurrent(int)
meth public org.openide.text.Line getOriginal(int)
supr org.openide.text.Line$Set
hfds list,listener
hcls OffsetLineCreator

CLSS public org.openide.text.EditorSupport
 anno 0 java.lang.Deprecated()
cons public init(org.openide.loaders.MultiDataObject$Entry)
fld protected java.lang.String modifiedAppendix
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String EDITOR_MODE = "editor"
 anno 0 java.lang.Deprecated()
innr public static Editor
intf org.openide.cookies.CloseCookie
intf org.openide.cookies.EditorCookie$Observable
intf org.openide.cookies.OpenCookie
intf org.openide.cookies.PrintCookie
meth protected boolean canClose()
meth protected boolean close(boolean)
meth protected boolean notifyModified()
meth protected java.lang.String messageName()
meth protected java.lang.String messageOpened()
meth protected java.lang.String messageOpening()
meth protected java.lang.String messageSave()
meth protected java.lang.String messageToolTip()
meth protected javax.swing.text.EditorKit createEditorKit()
meth protected org.openide.awt.UndoRedo$Manager createUndoRedoManager()
meth protected org.openide.loaders.MultiDataObject findDataObject()
meth protected org.openide.text.EditorSupport$Editor openAt(org.openide.text.PositionRef)
meth protected org.openide.util.Task reloadDocumentTask()
meth protected org.openide.windows.CloneableTopComponent createCloneableTopComponent()
meth protected void loadFromStreamToKit(javax.swing.text.StyledDocument,java.io.InputStream,javax.swing.text.EditorKit) throws java.io.IOException,javax.swing.text.BadLocationException
meth protected void notifyClosed()
meth protected void notifyUnmodified()
meth protected void reloadDocument()
meth protected void saveFromKitToStream(javax.swing.text.StyledDocument,javax.swing.text.EditorKit,java.io.OutputStream) throws java.io.IOException,javax.swing.text.BadLocationException
meth protected void updateTitles()
meth public boolean close()
meth public boolean isDocumentLoaded()
meth public boolean isModified()
meth public final org.openide.text.PositionRef createPositionRef(int,javax.swing.text.Position$Bias)
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public javax.swing.JEditorPane[] getOpenedPanes()
meth public javax.swing.text.StyledDocument getDocument()
meth public javax.swing.text.StyledDocument openDocument() throws java.io.IOException
meth public org.openide.text.Line$Set getLineSet()
meth public org.openide.util.Task prepareDocument()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void open()
meth public void print()
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void saveDocument() throws java.io.IOException
meth public void setActions(org.openide.util.actions.SystemAction[])
 anno 0 java.lang.Deprecated()
meth public void setMIMEType(java.lang.String)
meth public void setModificationListening(boolean)
supr org.openide.loaders.OpenSupport
hfds del,listenToModifs
hcls Del,DelEnv,EntryEnv

CLSS public static org.openide.text.EditorSupport$Editor
 outer org.openide.text.EditorSupport
cons public init()
cons public init(org.openide.loaders.DataObject)
cons public init(org.openide.loaders.DataObject,org.openide.text.EditorSupport)
fld protected org.openide.loaders.DataObject obj
meth public void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
supr org.openide.text.CloneableEditor
hfds serialVersionUID

CLSS public org.openide.text.FilterDocument
cons public init(javax.swing.text.Document)
fld protected javax.swing.text.Document original
intf javax.swing.text.StyledDocument
meth public int getLength()
meth public java.awt.Color getBackground(javax.swing.text.AttributeSet)
meth public java.awt.Color getForeground(javax.swing.text.AttributeSet)
meth public java.awt.Font getFont(javax.swing.text.AttributeSet)
meth public java.lang.Object getProperty(java.lang.Object)
meth public java.lang.String getText(int,int) throws javax.swing.text.BadLocationException
meth public javax.swing.text.Element getCharacterElement(int)
meth public javax.swing.text.Element getDefaultRootElement()
meth public javax.swing.text.Element getParagraphElement(int)
meth public javax.swing.text.Element[] getRootElements()
meth public javax.swing.text.Position createPosition(int) throws javax.swing.text.BadLocationException
meth public javax.swing.text.Position getEndPosition()
meth public javax.swing.text.Position getStartPosition()
meth public javax.swing.text.Style addStyle(java.lang.String,javax.swing.text.Style)
meth public javax.swing.text.Style getLogicalStyle(int)
meth public javax.swing.text.Style getStyle(java.lang.String)
meth public void addDocumentListener(javax.swing.event.DocumentListener)
meth public void addUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public void getText(int,int,javax.swing.text.Segment) throws javax.swing.text.BadLocationException
meth public void insertString(int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth public void putProperty(java.lang.Object,java.lang.Object)
meth public void remove(int,int) throws javax.swing.text.BadLocationException
meth public void removeDocumentListener(javax.swing.event.DocumentListener)
meth public void removeStyle(java.lang.String)
meth public void removeUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public void render(java.lang.Runnable)
meth public void setCharacterAttributes(int,int,javax.swing.text.AttributeSet,boolean)
meth public void setLogicalStyle(int,javax.swing.text.Style)
meth public void setParagraphAttributes(int,int,javax.swing.text.AttributeSet,boolean)
supr java.lang.Object
hfds leaf

CLSS public org.openide.text.FilterStyledDocument
cons public init(javax.swing.text.StyledDocument)
meth public java.awt.Color getBackground(javax.swing.text.AttributeSet)
meth public java.awt.Color getForeground(javax.swing.text.AttributeSet)
meth public java.awt.Font getFont(javax.swing.text.AttributeSet)
meth public javax.swing.text.Element getCharacterElement(int)
meth public javax.swing.text.Element getParagraphElement(int)
meth public javax.swing.text.Style addStyle(java.lang.String,javax.swing.text.Style)
meth public javax.swing.text.Style getLogicalStyle(int)
meth public javax.swing.text.Style getStyle(java.lang.String)
meth public void removeStyle(java.lang.String)
meth public void setCharacterAttributes(int,int,javax.swing.text.AttributeSet,boolean)
meth public void setLogicalStyle(int,javax.swing.text.Style)
meth public void setParagraphAttributes(int,int,javax.swing.text.AttributeSet,boolean)
supr org.openide.text.FilterDocument

CLSS public abstract org.openide.text.IndentEngine
 anno 0 java.lang.Deprecated()
cons public init()
meth protected boolean acceptMimeType(java.lang.String)
meth public abstract int indentLine(javax.swing.text.Document,int)
meth public abstract int indentNewLine(javax.swing.text.Document,int)
meth public abstract java.io.Writer createWriter(javax.swing.text.Document,int,java.io.Writer)
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static java.util.Enumeration<? extends org.openide.text.IndentEngine> indentEngines()
meth public static org.openide.text.IndentEngine find(java.lang.String)
meth public static org.openide.text.IndentEngine find(javax.swing.text.Document)
meth public static org.openide.text.IndentEngine getDefault()
meth public static void register(java.lang.String,org.openide.text.IndentEngine)
 anno 0 java.lang.Deprecated()
supr org.openide.ServiceType
hfds INSTANCE,map,serialVersionUID
hcls Default

CLSS public abstract org.openide.text.Line
cons public init(java.lang.Object)
cons public init(org.openide.util.Lookup)
fld public final static int SHOW_GOTO = 2
 anno 0 java.lang.Deprecated()
fld public final static int SHOW_REUSE = 4
 anno 0 java.lang.Deprecated()
fld public final static int SHOW_REUSE_NEW = 5
 anno 0 java.lang.Deprecated()
fld public final static int SHOW_SHOW = 1
 anno 0 java.lang.Deprecated()
fld public final static int SHOW_TOFRONT = 3
 anno 0 java.lang.Deprecated()
fld public final static int SHOW_TRY_SHOW = 0
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String PROP_LINE_NUMBER = "lineNumber"
innr public abstract static Part
innr public abstract static Set
innr public final static !enum ShowOpenType
innr public final static !enum ShowVisibilityType
intf java.io.Serializable
meth public abstract boolean isBreakpoint()
 anno 0 java.lang.Deprecated()
meth public abstract int getLineNumber()
meth public abstract void markCurrentLine()
 anno 0 java.lang.Deprecated()
meth public abstract void markError()
 anno 0 java.lang.Deprecated()
meth public abstract void setBreakpoint(boolean)
 anno 0 java.lang.Deprecated()
meth public abstract void show(int,int)
 anno 0 java.lang.Deprecated()
meth public abstract void unmarkCurrentLine()
 anno 0 java.lang.Deprecated()
meth public abstract void unmarkError()
 anno 0 java.lang.Deprecated()
meth public boolean canBeMarkedCurrent(int,org.openide.text.Line)
 anno 0 java.lang.Deprecated()
meth public final org.openide.util.Lookup getLookup()
meth public java.lang.String getDisplayName()
meth public java.lang.String getText()
meth public org.openide.text.Line$Part createPart(int,int)
meth public void show(int)
 anno 0 java.lang.Deprecated()
meth public void show(org.openide.text.Line$ShowOpenType,org.openide.text.Line$ShowVisibilityType)
meth public void show(org.openide.text.Line$ShowOpenType,org.openide.text.Line$ShowVisibilityType,int)
supr org.openide.text.Annotatable
hfds LOG,dataObject,nullPart,serialVersionUID
hcls NullPart

CLSS public abstract static org.openide.text.Line$Part
 outer org.openide.text.Line
cons public init()
fld public final static java.lang.String PROP_COLUMN = "column"
fld public final static java.lang.String PROP_LENGTH = "length"
fld public final static java.lang.String PROP_LINE = "line"
meth public abstract int getColumn()
meth public abstract int getLength()
meth public abstract org.openide.text.Line getLine()
supr org.openide.text.Annotatable

CLSS public abstract static org.openide.text.Line$Set
 outer org.openide.text.Line
cons public init()
meth public abstract java.util.List<? extends org.openide.text.Line> getLines()
meth public abstract org.openide.text.Line getCurrent(int)
meth public abstract org.openide.text.Line getOriginal(int)
meth public final java.util.Date getDate()
meth public int getOriginalLineNumber(org.openide.text.Line)
supr java.lang.Object
hfds date,lineVector

CLSS public final static !enum org.openide.text.Line$ShowOpenType
 outer org.openide.text.Line
fld public final static org.openide.text.Line$ShowOpenType NONE
fld public final static org.openide.text.Line$ShowOpenType OPEN
fld public final static org.openide.text.Line$ShowOpenType REUSE
fld public final static org.openide.text.Line$ShowOpenType REUSE_NEW
meth public static org.openide.text.Line$ShowOpenType valueOf(java.lang.String)
meth public static org.openide.text.Line$ShowOpenType[] values()
supr java.lang.Enum<org.openide.text.Line$ShowOpenType>

CLSS public final static !enum org.openide.text.Line$ShowVisibilityType
 outer org.openide.text.Line
fld public final static org.openide.text.Line$ShowVisibilityType FOCUS
fld public final static org.openide.text.Line$ShowVisibilityType FRONT
fld public final static org.openide.text.Line$ShowVisibilityType NONE
meth public static org.openide.text.Line$ShowVisibilityType valueOf(java.lang.String)
meth public static org.openide.text.Line$ShowVisibilityType[] values()
supr java.lang.Enum<org.openide.text.Line$ShowVisibilityType>

CLSS public final org.openide.text.NbDocument
fld public final static java.lang.Object GUARDED
fld public final static java.lang.String BREAKPOINT_STYLE_NAME = "NbBreakpointStyle"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String CURRENT_STYLE_NAME = "NbCurrentStyle"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String ERROR_STYLE_NAME = "NbErrorStyle"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String NORMAL_STYLE_NAME = "NbNormalStyle"
 anno 0 java.lang.Deprecated()
innr public abstract interface static Annotatable
innr public abstract interface static CustomEditor
innr public abstract interface static CustomToolbar
innr public abstract interface static PositionBiasable
innr public abstract interface static Printable
innr public abstract interface static WriteLockable
meth public static <%0 extends javax.swing.undo.UndoableEdit> {%%0} getEditToBeRedoneOfType(org.openide.cookies.EditorCookie,java.lang.Class<{%%0}>)
meth public static <%0 extends javax.swing.undo.UndoableEdit> {%%0} getEditToBeUndoneOfType(org.openide.cookies.EditorCookie,java.lang.Class<{%%0}>)
meth public static boolean openDocument(org.openide.util.Lookup$Provider,int,int,org.openide.text.Line$ShowOpenType,org.openide.text.Line$ShowVisibilityType)
meth public static boolean openDocument(org.openide.util.Lookup$Provider,int,org.openide.text.Line$ShowOpenType,org.openide.text.Line$ShowVisibilityType)
meth public static int findLineColumn(javax.swing.text.StyledDocument,int)
meth public static int findLineNumber(javax.swing.text.StyledDocument,int)
meth public static int findLineOffset(javax.swing.text.StyledDocument,int)
meth public static java.lang.Object findPageable(javax.swing.text.StyledDocument)
meth public static javax.swing.JEditorPane findRecentEditorPane(org.openide.cookies.EditorCookie)
meth public static javax.swing.text.Element findLineRootElement(javax.swing.text.StyledDocument)
meth public static javax.swing.text.Position createPosition(javax.swing.text.Document,int,javax.swing.text.Position$Bias) throws javax.swing.text.BadLocationException
meth public static javax.swing.text.StyledDocument getDocument(org.openide.util.Lookup$Provider)
meth public static void addAnnotation(javax.swing.text.StyledDocument,javax.swing.text.Position,int,org.openide.text.Annotation)
meth public static void insertGuarded(javax.swing.text.StyledDocument,int,java.lang.String) throws javax.swing.text.BadLocationException
meth public static void markBreakpoint(javax.swing.text.StyledDocument,int)
 anno 0 java.lang.Deprecated()
meth public static void markCurrent(javax.swing.text.StyledDocument,int)
 anno 0 java.lang.Deprecated()
meth public static void markError(javax.swing.text.StyledDocument,int)
 anno 0 java.lang.Deprecated()
meth public static void markGuarded(javax.swing.text.StyledDocument,int,int)
meth public static void markNormal(javax.swing.text.StyledDocument,int)
 anno 0 java.lang.Deprecated()
meth public static void removeAnnotation(javax.swing.text.StyledDocument,org.openide.text.Annotation)
meth public static void runAtomic(javax.swing.text.StyledDocument,java.lang.Runnable)
meth public static void runAtomicAsUser(javax.swing.text.StyledDocument,java.lang.Runnable) throws javax.swing.text.BadLocationException
meth public static void unmarkGuarded(javax.swing.text.StyledDocument,int,int)
supr java.lang.Object
hfds ATTR_ADD,ATTR_REMOVE
hcls DocumentRenderer

CLSS public abstract interface static org.openide.text.NbDocument$Annotatable
 outer org.openide.text.NbDocument
intf javax.swing.text.Document
meth public abstract void addAnnotation(javax.swing.text.Position,int,org.openide.text.Annotation)
meth public abstract void removeAnnotation(org.openide.text.Annotation)

CLSS public abstract interface static org.openide.text.NbDocument$CustomEditor
 outer org.openide.text.NbDocument
intf javax.swing.text.Document
meth public abstract java.awt.Component createEditor(javax.swing.JEditorPane)

CLSS public abstract interface static org.openide.text.NbDocument$CustomToolbar
 outer org.openide.text.NbDocument
intf javax.swing.text.Document
meth public abstract javax.swing.JToolBar createToolbar(javax.swing.JEditorPane)

CLSS public abstract interface static org.openide.text.NbDocument$PositionBiasable
 outer org.openide.text.NbDocument
intf javax.swing.text.Document
meth public abstract javax.swing.text.Position createPosition(int,javax.swing.text.Position$Bias) throws javax.swing.text.BadLocationException

CLSS public abstract interface static org.openide.text.NbDocument$Printable
 outer org.openide.text.NbDocument
intf javax.swing.text.Document
meth public abstract java.text.AttributedCharacterIterator[] createPrintIterators()

CLSS public abstract interface static org.openide.text.NbDocument$WriteLockable
 outer org.openide.text.NbDocument
intf javax.swing.text.Document
meth public abstract void runAtomic(java.lang.Runnable)
meth public abstract void runAtomicAsUser(java.lang.Runnable) throws javax.swing.text.BadLocationException

CLSS public final org.openide.text.PositionBounds
cons public init(org.openide.text.PositionRef,org.openide.text.PositionRef)
intf java.io.Serializable
meth public java.lang.String getText() throws java.io.IOException,javax.swing.text.BadLocationException
meth public java.lang.String toString()
meth public org.openide.text.PositionBounds insertAfter(java.lang.String) throws java.io.IOException,javax.swing.text.BadLocationException
meth public org.openide.text.PositionRef getBegin()
meth public org.openide.text.PositionRef getEnd()
meth public void setText(java.lang.String) throws java.io.IOException,javax.swing.text.BadLocationException
supr java.lang.Object
hfds begin,end,serialVersionUID

CLSS public final org.openide.text.PositionRef
intf java.io.Serializable
intf javax.swing.text.Position
meth public int getColumn() throws java.io.IOException
meth public int getLine() throws java.io.IOException
meth public int getOffset()
meth public java.lang.String toString()
meth public javax.swing.text.Position getPosition() throws java.io.IOException
meth public javax.swing.text.Position$Bias getPositionBias()
meth public org.openide.text.CloneableEditorSupport getCloneableEditorSupport()
supr java.lang.Object
hfds LOG,insertAfter,kind,manager,serialVersionUID
hcls Manager

CLSS public final org.openide.text.PrintPreferences
innr public final static !enum Alignment
meth public static boolean getWrap()
meth public static float getLineAscentCorrection()
meth public static java.awt.Font getFooterFont()
meth public static java.awt.Font getHeaderFont()
meth public static java.awt.print.PageFormat getPageFormat(java.awt.print.PrinterJob)
meth public static java.lang.String getFooterFormat()
meth public static java.lang.String getHeaderFormat()
meth public static org.openide.text.PrintPreferences$Alignment getFooterAlignment()
meth public static org.openide.text.PrintPreferences$Alignment getHeaderAlignment()
meth public static void setFooterAlignment(org.openide.text.PrintPreferences$Alignment)
meth public static void setFooterFont(java.awt.Font)
meth public static void setFooterFormat(java.lang.String)
meth public static void setHeaderAlignment(org.openide.text.PrintPreferences$Alignment)
meth public static void setHeaderFont(java.awt.Font)
meth public static void setHeaderFormat(java.lang.String)
meth public static void setLineAscentCorrection(float)
meth public static void setPageFormat(java.awt.print.PageFormat)
meth public static void setWrap(boolean)
supr java.lang.Object
hfds DEFAULT_FONT_NAME,DEFAULT_FONT_SIZE,DEFAULT_FONT_STYLE,INSTANCE,PROP_FOOTER_ALIGNMENT,PROP_FOOTER_FONT_NAME,PROP_FOOTER_FONT_SIZE,PROP_FOOTER_FONT_STYLE,PROP_FOOTER_FORMAT,PROP_HEADER_ALIGNMENT,PROP_HEADER_FONT_NAME,PROP_HEADER_FONT_SIZE,PROP_HEADER_FONT_STYLE,PROP_HEADER_FORMAT,PROP_LINE_ASCENT_CORRECTION,PROP_PAGE_HEIGHT,PROP_PAGE_IMAGEABLEAREA_HEIGHT,PROP_PAGE_IMAGEABLEAREA_WIDTH,PROP_PAGE_IMAGEABLEAREA_X,PROP_PAGE_IMAGEABLEAREA_Y,PROP_PAGE_ORIENTATION,PROP_PAGE_WIDTH,PROP_WRAP

CLSS public final static !enum org.openide.text.PrintPreferences$Alignment
 outer org.openide.text.PrintPreferences
fld public final static org.openide.text.PrintPreferences$Alignment CENTER
fld public final static org.openide.text.PrintPreferences$Alignment LEFT
fld public final static org.openide.text.PrintPreferences$Alignment RIGHT
meth public static org.openide.text.PrintPreferences$Alignment valueOf(java.lang.String)
meth public static org.openide.text.PrintPreferences$Alignment[] values()
supr java.lang.Enum<org.openide.text.PrintPreferences$Alignment>

CLSS public org.openide.text.StableCompoundEdit
cons public init()
intf javax.swing.undo.UndoableEdit
meth protected javax.swing.undo.UndoableEdit lastEdit()
meth public boolean addEdit(javax.swing.undo.UndoableEdit)
meth public boolean canRedo()
meth public boolean canUndo()
meth public boolean isInProgress()
meth public boolean isSignificant()
meth public boolean replaceEdit(javax.swing.undo.UndoableEdit)
meth public final java.util.List<javax.swing.undo.UndoableEdit> getEdits()
meth public java.lang.String getPresentationName()
meth public java.lang.String getRedoPresentationName()
meth public java.lang.String getUndoPresentationName()
meth public java.lang.String toString()
meth public void die()
meth public void end()
meth public void redo()
meth public void undo()
supr java.lang.Object
hfds ALIVE,HAS_BEEN_DONE,IN_PROGRESS,edits,statusBits

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

CLSS public abstract org.openide.windows.CloneableOpenSupport
cons public init(org.openide.windows.CloneableOpenSupport$Env)
fld protected org.openide.windows.CloneableOpenSupport$Env env
fld protected org.openide.windows.CloneableTopComponent$Ref allEditors
innr public abstract interface static Env
meth protected abstract java.lang.String messageOpened()
meth protected abstract java.lang.String messageOpening()
meth protected abstract org.openide.windows.CloneableTopComponent createCloneableTopComponent()
meth protected boolean canClose()
meth protected boolean close(boolean)
meth protected final org.openide.windows.CloneableTopComponent openCloneableTopComponent()
meth protected void afterRedirect(org.openide.windows.CloneableOpenSupport)
meth public boolean close()
meth public void edit()
meth public void open()
meth public void view()
supr java.lang.Object
hfds container
hcls Listener

CLSS public abstract interface static org.openide.windows.CloneableOpenSupport$Env
 outer org.openide.windows.CloneableOpenSupport
fld public final static java.lang.String PROP_MODIFIED = "modified"
fld public final static java.lang.String PROP_VALID = "valid"
intf java.io.Serializable
meth public abstract boolean isModified()
meth public abstract boolean isValid()
meth public abstract org.openide.windows.CloneableOpenSupport findCloneableOpenSupport()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void addVetoableChangeListener(java.beans.VetoableChangeListener)
meth public abstract void markModified() throws java.io.IOException
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removeVetoableChangeListener(java.beans.VetoableChangeListener)
meth public abstract void unmarkModified()

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

