#Signature file v4.1
#Version 1.60

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

CLSS public abstract org.netbeans.lib.terminalemulator.AbstractInterp
cons protected init(org.netbeans.lib.terminalemulator.Ops)
fld public org.netbeans.lib.terminalemulator.AbstractInterp$State state
fld public org.netbeans.lib.terminalemulator.Ops ops
innr protected abstract interface static Actor
innr protected static State
intf org.netbeans.lib.terminalemulator.Interp
meth protected boolean noNumber()
meth protected boolean pushNumber()
meth protected final void sendChars(java.awt.event.KeyEvent,java.lang.String)
meth protected int nNumbers()
meth protected int numberAt(int)
meth protected void remember_digit(char)
meth protected void resetNumber()
meth public void reset()
supr java.lang.Object
hfds MAX_NUMBERS,number,numberx

CLSS protected abstract interface static org.netbeans.lib.terminalemulator.AbstractInterp$Actor
 outer org.netbeans.lib.terminalemulator.AbstractInterp
meth public abstract java.lang.String action(org.netbeans.lib.terminalemulator.AbstractInterp,char)

CLSS protected static org.netbeans.lib.terminalemulator.AbstractInterp$State
 outer org.netbeans.lib.terminalemulator.AbstractInterp
cons public init(java.lang.String)
meth public java.lang.String name()
meth public void setAction(char,org.netbeans.lib.terminalemulator.AbstractInterp$State,org.netbeans.lib.terminalemulator.AbstractInterp$Actor)
meth public void setRegular(org.netbeans.lib.terminalemulator.AbstractInterp$State,org.netbeans.lib.terminalemulator.AbstractInterp$Actor)
supr java.lang.Object
hfds act_error,action,action_regular,name
hcls Action

CLSS public org.netbeans.lib.terminalemulator.ActiveRegion
fld public org.netbeans.lib.terminalemulator.Coord begin
fld public org.netbeans.lib.terminalemulator.Coord end
meth public boolean isFeedbackEnabled()
meth public boolean isFeedbackViaParent()
meth public boolean isLink()
meth public boolean isSelectable()
meth public java.lang.Object getUserObject()
meth public org.netbeans.lib.terminalemulator.ActiveRegion firstChild()
meth public org.netbeans.lib.terminalemulator.ActiveRegion getNextSibling()
meth public org.netbeans.lib.terminalemulator.ActiveRegion getPreviousSibling()
meth public org.netbeans.lib.terminalemulator.ActiveRegion lastChild()
meth public org.netbeans.lib.terminalemulator.ActiveRegion parent()
meth public org.netbeans.lib.terminalemulator.Extent getExtent()
meth public void setFeedbackEnabled(boolean)
meth public void setFeedbackViaParent(boolean)
meth public void setLink(boolean)
meth public void setSelectable(boolean)
meth public void setUserObject(java.lang.Object)
supr java.lang.Object
hfds children,feedback_enabled,feedback_via_parent,has_end,link,nested,parent,parentAttrs,selectable,user_object

CLSS public org.netbeans.lib.terminalemulator.ActiveTerm
cons public init()
meth protected void hyperlink(java.lang.String,java.lang.String)
meth public org.netbeans.lib.terminalemulator.ActiveRegion beginRegion(boolean)
meth public org.netbeans.lib.terminalemulator.ActiveRegion findRegion(org.netbeans.lib.terminalemulator.Coord)
meth public void cancelRegion()
meth public void clear()
meth public void clearHistoryNoRefresh()
meth public void endRegion()
meth public void hilite(org.netbeans.lib.terminalemulator.ActiveRegion)
meth public void hilite(org.netbeans.lib.terminalemulator.Coord,org.netbeans.lib.terminalemulator.Coord)
meth public void setActionListener(org.netbeans.lib.terminalemulator.ActiveTermListener)
supr org.netbeans.lib.terminalemulator.StreamTerm
hfds at_listener,currentCursor,last_begin,last_end,pointerCursor,regularCursor,rm

CLSS public abstract interface org.netbeans.lib.terminalemulator.ActiveTermListener
meth public abstract void action(org.netbeans.lib.terminalemulator.ActiveRegion,java.awt.event.InputEvent)

CLSS public final org.netbeans.lib.terminalemulator.Coord
cons public init()
cons public init(org.netbeans.lib.terminalemulator.Coord)
fld public int col
fld public int row
intf java.lang.Comparable<org.netbeans.lib.terminalemulator.Coord>
meth public int compareTo(org.netbeans.lib.terminalemulator.Coord)
meth public java.lang.Object clone()
meth public java.lang.String toString()
meth public static org.netbeans.lib.terminalemulator.Coord make(int,int)
meth public void copyFrom(org.netbeans.lib.terminalemulator.Coord)
supr java.lang.Object

CLSS public org.netbeans.lib.terminalemulator.Extent
cons public init(org.netbeans.lib.terminalemulator.Coord,org.netbeans.lib.terminalemulator.Coord)
fld public org.netbeans.lib.terminalemulator.Coord begin
fld public org.netbeans.lib.terminalemulator.Coord end
meth public boolean intersects(int,int)
meth public java.lang.String toString()
meth public org.netbeans.lib.terminalemulator.Extent order()
supr java.lang.Object

CLSS public abstract interface org.netbeans.lib.terminalemulator.Interp
meth public abstract char mapACS(char)
meth public abstract java.lang.String name()
meth public abstract void keyPressed(java.awt.event.KeyEvent)
meth public abstract void processChar(char)
meth public abstract void softReset()

CLSS public org.netbeans.lib.terminalemulator.InterpANSI
cons protected init(org.netbeans.lib.terminalemulator.Ops,org.netbeans.lib.terminalemulator.InterpANSI$InterpTypeANSI)
cons public init(org.netbeans.lib.terminalemulator.Ops)
innr protected static Ascii
innr protected static InterpTypeANSI
innr protected static InterpTypeProtoANSI
meth protected boolean dispatchAttr(org.netbeans.lib.terminalemulator.AbstractInterp,int)
meth public char mapACS(char)
meth public java.lang.String name()
meth public void keyPressed(java.awt.event.KeyEvent)
meth public void reset()
supr org.netbeans.lib.terminalemulator.InterpDumb
hfds type,type_singleton

CLSS protected static org.netbeans.lib.terminalemulator.InterpANSI$InterpTypeANSI
 outer org.netbeans.lib.terminalemulator.InterpANSI
cons protected init()
fld protected final org.netbeans.lib.terminalemulator.AbstractInterp$Actor act_push_number
fld protected final org.netbeans.lib.terminalemulator.AbstractInterp$Actor act_remember1
fld protected final org.netbeans.lib.terminalemulator.AbstractInterp$Actor act_remember_digit
fld protected final org.netbeans.lib.terminalemulator.AbstractInterp$Actor act_reset_number
fld protected final org.netbeans.lib.terminalemulator.AbstractInterp$Actor act_setg
fld protected final org.netbeans.lib.terminalemulator.AbstractInterp$State st_esc
fld protected final org.netbeans.lib.terminalemulator.AbstractInterp$State st_esc_lb
fld protected final org.netbeans.lib.terminalemulator.AbstractInterp$State st_esc_setg
supr org.netbeans.lib.terminalemulator.InterpDumb$InterpTypeDumb

CLSS public org.netbeans.lib.terminalemulator.InterpDumb
cons protected init(org.netbeans.lib.terminalemulator.Ops,org.netbeans.lib.terminalemulator.InterpDumb$InterpTypeDumb)
cons public init(org.netbeans.lib.terminalemulator.Ops)
innr protected static InterpTypeDumb
meth protected org.netbeans.lib.terminalemulator.AbstractInterp$State pop_state()
meth protected void pop_all_states()
meth protected void push_state(org.netbeans.lib.terminalemulator.AbstractInterp$State)
meth public char mapACS(char)
meth public java.lang.String name()
meth public void keyPressed(java.awt.event.KeyEvent)
meth public void processChar(char)
meth public void reset()
meth public void softReset()
supr org.netbeans.lib.terminalemulator.AbstractInterp
hfds ctlSequence,stack,type,type_singleton

CLSS protected static org.netbeans.lib.terminalemulator.InterpDumb$InterpTypeDumb
 outer org.netbeans.lib.terminalemulator.InterpDumb
cons protected init()
fld protected final org.netbeans.lib.terminalemulator.AbstractInterp$Actor act_beL
fld protected final org.netbeans.lib.terminalemulator.AbstractInterp$Actor act_bs
fld protected final org.netbeans.lib.terminalemulator.AbstractInterp$Actor act_cr
fld protected final org.netbeans.lib.terminalemulator.AbstractInterp$Actor act_err
fld protected final org.netbeans.lib.terminalemulator.AbstractInterp$Actor act_lf
fld protected final org.netbeans.lib.terminalemulator.AbstractInterp$Actor act_nop
fld protected final org.netbeans.lib.terminalemulator.AbstractInterp$Actor act_pause
fld protected final org.netbeans.lib.terminalemulator.AbstractInterp$Actor act_regular
fld protected final org.netbeans.lib.terminalemulator.AbstractInterp$Actor act_tab
fld public final org.netbeans.lib.terminalemulator.AbstractInterp$State st_base
supr java.lang.Object
hcls ACT_BEL,ACT_BS,ACT_CR,ACT_ERR,ACT_LF,ACT_NOP,ACT_PAUSE,ACT_REGULAR,ACT_TAB

CLSS public org.netbeans.lib.terminalemulator.LineDiscipline
cons public init()
meth public void flush()
meth public void putChar(char)
meth public void putChars(char[],int,int)
meth public void sendChar(char)
meth public void sendChars(char[],int,int)
supr org.netbeans.lib.terminalemulator.TermStream
hfds bs_sequence,line,put_buf,put_capacity,put_length,send_buf,send_buf_sz

CLSS public abstract interface org.netbeans.lib.terminalemulator.LogicalLineVisitor
meth public abstract boolean visit(int,org.netbeans.lib.terminalemulator.Coord,org.netbeans.lib.terminalemulator.Coord,java.lang.String)

CLSS public org.netbeans.lib.terminalemulator.NullTermStream
cons public init()
meth public void flush()
meth public void putChar(char)
meth public void putChars(char[],int,int)
meth public void sendChar(char)
meth public void sendChars(char[],int,int)
supr org.netbeans.lib.terminalemulator.TermStream

CLSS public abstract interface org.netbeans.lib.terminalemulator.Ops
meth public abstract int op_get_column()
meth public abstract int op_get_width()
meth public abstract void logCompletedSequence(java.lang.String)
meth public abstract void logUnrecognizedSequence(java.lang.String)
meth public abstract void op_ae()
meth public abstract void op_al(int)
meth public abstract void op_as()
meth public abstract void op_attr(int)
meth public abstract void op_back_space()
meth public abstract void op_bc(int)
meth public abstract void op_bel()
meth public abstract void op_carriage_return()
meth public abstract void op_cbt(int)
meth public abstract void op_cd()
meth public abstract void op_ce()
meth public abstract void op_cha(int)
meth public abstract void op_char(char)
meth public abstract void op_cht(int)
meth public abstract void op_cl()
meth public abstract void op_cm(int,int)
meth public abstract void op_cud(int)
meth public abstract void op_cursor_visible(boolean)
meth public abstract void op_cuu(int)
meth public abstract void op_cwd(java.lang.String)
meth public abstract void op_dc(int)
meth public abstract void op_dl(int)
meth public abstract void op_do(int)
meth public abstract void op_ech(int)
meth public abstract void op_ed(int)
meth public abstract void op_el(int)
meth public abstract void op_ext(java.lang.String)
meth public abstract void op_full_reset()
meth public abstract void op_glyph(int,int)
meth public abstract void op_ho()
meth public abstract void op_hyperlink(java.lang.String,java.lang.String)
meth public abstract void op_ic(int)
meth public abstract void op_icon_name(java.lang.String)
meth public abstract void op_ind(int)
meth public abstract void op_line_feed()
meth public abstract void op_margin(int,int)
meth public abstract void op_nd(int)
meth public abstract void op_pause()
meth public abstract void op_rc()
meth public abstract void op_reset_mode(int)
meth public abstract void op_reverse(boolean)
meth public abstract void op_ri(int)
meth public abstract void op_sc()
meth public abstract void op_selectGL(int)
meth public abstract void op_send_chars(java.lang.String)
meth public abstract void op_setG(int,int)
meth public abstract void op_set_mode(int)
meth public abstract void op_soft_reset()
meth public abstract void op_status_report(int)
meth public abstract void op_tab()
meth public abstract void op_time(boolean)
meth public abstract void op_up(int)
meth public abstract void op_vpa(int)
meth public abstract void op_win_title(java.lang.String)

CLSS public org.netbeans.lib.terminalemulator.RegionException
supr java.lang.Exception

CLSS public org.netbeans.lib.terminalemulator.RegionManager
cons public init()
meth public org.netbeans.lib.terminalemulator.ActiveRegion beginRegion(org.netbeans.lib.terminalemulator.Coord) throws org.netbeans.lib.terminalemulator.RegionException
meth public org.netbeans.lib.terminalemulator.ActiveRegion endRegion(org.netbeans.lib.terminalemulator.Coord) throws org.netbeans.lib.terminalemulator.RegionException
meth public org.netbeans.lib.terminalemulator.ActiveRegion findRegion(org.netbeans.lib.terminalemulator.Coord)
meth public org.netbeans.lib.terminalemulator.ActiveRegion root()
meth public void cancelRegion() throws org.netbeans.lib.terminalemulator.RegionException
meth public void reset()
supr java.lang.Object
hfds parent,region,root

CLSS public org.netbeans.lib.terminalemulator.StreamTerm
cons public init()
meth public java.io.OutputStreamWriter getOutputStreamWriter()
meth public java.io.Reader getIn()
meth public java.io.Writer getOut()
meth public void connect(java.io.OutputStream,java.io.InputStream,java.io.InputStream)
meth public void connect(java.io.OutputStream,java.io.InputStream,java.io.InputStream,java.lang.String)
meth public void disconnect(java.lang.Runnable)
supr org.netbeans.lib.terminalemulator.Term
hfds BUFSZ,connected,outputStreamWriter,pipe,stderrMonitor,stdinMonitor,stdoutMonitor,writer
hcls InputMonitor,OutputMonitor,Pipe,TermWriter

CLSS public org.netbeans.lib.terminalemulator.Term
cons public init()
fld public final static int DEBUG_INPUT = 4
fld public final static int DEBUG_KEYPASS = 64
fld public final static int DEBUG_KEYS = 2
fld public final static int DEBUG_MARGINS = 32
fld public final static int DEBUG_OPS = 1
fld public final static int DEBUG_OUTPUT = 8
fld public final static int DEBUG_WRAP = 16
innr protected AccessibleTerm
innr public static ExternalCommandsConstants
intf javax.accessibility.Accessible
meth protected boolean debugInput()
meth protected boolean debugOutput()
meth protected void hyperlink(java.lang.String,java.lang.String)
meth protected void possibly_repaint(boolean)
meth protected void repaint(boolean)
meth protected void updateTtySize()
meth public boolean getAltSendsEscape()
meth public boolean isAnchored()
meth public boolean isAutoCopy()
 anno 0 java.lang.Deprecated()
meth public boolean isClickToType()
meth public boolean isCoordVisible(org.netbeans.lib.terminalemulator.Coord)
meth public boolean isCursorVisible()
meth public boolean isFixedFont()
meth public boolean isHorizontallyScrollable()
meth public boolean isReadOnly()
meth public boolean isRefreshEnabled()
meth public boolean isReverseVideo()
meth public boolean isScrollOnInput()
meth public boolean isScrollOnOutput()
meth public boolean isSelectionXOR()
meth public boolean isSizeRounded()
meth public boolean isTrackCursor()
meth public boolean requestFocusInWindow()
meth public final boolean isSequenceLogging()
meth public final java.util.Set<java.lang.String> getCompletedSequences()
meth public final java.util.Set<java.lang.String> getUnrecognizedSequences()
meth public final void setFont(java.awt.Font)
meth public final void setRenderingHints(java.util.Map<?,?>)
meth public final void setSequenceLogging(boolean)
meth public int CoordToPosition(org.netbeans.lib.terminalemulator.Coord)
meth public int charWidth(char)
meth public int getColumns()
meth public int getCursorCol()
meth public int getCursorRow()
meth public int getHistoryBuffSize()
meth public int getHistorySize()
meth public int getRows()
meth public int getTabSize()
meth public java.awt.Color getActiveColor()
meth public java.awt.Color getHighlightColor()
meth public java.awt.Color getHighlightXORColor()
meth public java.awt.Dimension getGlyphCellSize()
meth public java.awt.Point mapToBufRowCol(java.awt.Point)
meth public java.awt.Point mapToViewRowCol(java.awt.Point)
meth public java.awt.Point toPixel(org.netbeans.lib.terminalemulator.Coord)
meth public java.lang.String getEmulation()
meth public java.lang.String getRowText(int)
meth public java.lang.String getSelectByWordDelimiters()
meth public java.lang.String getSelectedText()
meth public java.lang.String textWithin(org.netbeans.lib.terminalemulator.Coord,org.netbeans.lib.terminalemulator.Coord)
meth public java.util.HashSet<javax.swing.KeyStroke> getKeyStrokeSet()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.JComponent getCanvas()
 anno 0 java.lang.Deprecated()
meth public javax.swing.JComponent getScreen()
meth public org.netbeans.lib.terminalemulator.Coord PositionToCoord(int)
meth public org.netbeans.lib.terminalemulator.Coord advance(org.netbeans.lib.terminalemulator.Coord)
meth public org.netbeans.lib.terminalemulator.Coord backup(org.netbeans.lib.terminalemulator.Coord)
meth public org.netbeans.lib.terminalemulator.Coord getCursorCoord()
meth public org.netbeans.lib.terminalemulator.Extent extentInLogicalLine(org.netbeans.lib.terminalemulator.Coord,int,int)
meth public org.netbeans.lib.terminalemulator.Extent getSelectionExtent()
meth public org.netbeans.lib.terminalemulator.Interp getInterp()
meth public org.netbeans.lib.terminalemulator.Ops ops()
meth public org.netbeans.lib.terminalemulator.RegionManager regionManager()
meth public org.netbeans.lib.terminalemulator.WordDelineator getWordDelineator()
meth public void addInputListener(org.netbeans.lib.terminalemulator.TermInputListener)
meth public void addListener(org.netbeans.lib.terminalemulator.TermListener)
meth public void appendText(java.lang.String,boolean)
meth public void clear()
meth public void clearHistory()
meth public void clearHistoryNoRefresh()
meth public void clearSelection()
meth public void columnLeft(int)
meth public void columnRight(int)
meth public void copy()
meth public void copyToClipboard()
meth public void copyToSelection()
meth public void fillSizeInfo(java.awt.Dimension,java.awt.Dimension)
meth public void flush()
meth public void goTo(org.netbeans.lib.terminalemulator.Coord)
 anno 0 java.lang.Deprecated()
meth public void lineDown(int)
meth public void lineUp(int)
meth public void pageDown(int)
meth public void pageLeft(int)
meth public void pageRight(int)
meth public void pageUp(int)
meth public void paste()
meth public void pasteFromClipboard()
meth public void pasteFromSelection()
meth public void possiblyNormalize(org.netbeans.lib.terminalemulator.ActiveRegion)
meth public void possiblyNormalize(org.netbeans.lib.terminalemulator.Coord)
meth public void printCounts(boolean)
meth public void printStats(java.lang.String)
meth public void pushStream(org.netbeans.lib.terminalemulator.TermStream)
meth public void putChar(char)
meth public void putChars(char[],int,int)
meth public void removeInputListener(org.netbeans.lib.terminalemulator.TermInputListener)
meth public void removeListener(org.netbeans.lib.terminalemulator.TermListener)
meth public void requestFocus()
meth public void resetStats()
meth public void reverseVisitLogicalLines(org.netbeans.lib.terminalemulator.Coord,org.netbeans.lib.terminalemulator.Coord,org.netbeans.lib.terminalemulator.LogicalLineVisitor)
meth public void setActiveColor(java.awt.Color)
meth public void setAltSendsEscape(boolean)
meth public void setAnchored(boolean)
meth public void setAttribute(int)
meth public void setAutoCopy(boolean)
 anno 0 java.lang.Deprecated()
meth public void setBackground(java.awt.Color)
meth public void setCharacterAttribute(org.netbeans.lib.terminalemulator.Coord,org.netbeans.lib.terminalemulator.Coord,int,boolean)
meth public void setClickToType(boolean)
meth public void setColumns(int)
meth public void setCursorCoord(org.netbeans.lib.terminalemulator.Coord)
meth public void setCursorVisible(boolean)
meth public void setCustomColor(int,java.awt.Color)
 anno 0 java.lang.Deprecated()
meth public void setDebugFlags(int)
meth public void setEmulation(java.lang.String)
meth public void setEnabled(boolean)
meth public void setFixedFont(boolean)
meth public void setForeground(java.awt.Color)
meth public void setGlyph(int,int)
meth public void setGlyphGutterWidth(int)
meth public void setGlyphImage(int,java.awt.Image)
meth public void setHighlightColor(java.awt.Color)
meth public void setHighlightXORColor(java.awt.Color)
meth public void setHistorySize(int)
meth public void setHorizontallyScrollable(boolean)
meth public void setInputListener(org.netbeans.lib.terminalemulator.TermInputListener)
 anno 0 java.lang.Deprecated()
meth public void setInterp(org.netbeans.lib.terminalemulator.Interp)
meth public void setKeyStrokeSet(java.util.HashSet<javax.swing.KeyStroke>)
meth public void setKeymap(javax.swing.text.Keymap,java.util.Set<java.lang.String>)
meth public void setListener(org.netbeans.lib.terminalemulator.TermListener)
 anno 0 java.lang.Deprecated()
meth public void setReadOnly(boolean)
meth public void setRefreshEnabled(boolean)
meth public void setReverseVideo(boolean)
meth public void setRowGlyph(int,int,int)
meth public void setRows(int)
meth public void setRowsColumns(int,int)
meth public void setScrollOnInput(boolean)
meth public void setScrollOnOutput(boolean)
meth public void setSelectByWordDelimiters(java.lang.String)
meth public void setSelectionExtent(org.netbeans.lib.terminalemulator.Extent)
meth public void setSelectionXOR(boolean)
meth public void setSizeRounded(boolean)
meth public void setTabSize(int)
meth public void setText(java.lang.String)
meth public void setTrackCursor(boolean)
meth public void setWordDelineator(org.netbeans.lib.terminalemulator.WordDelineator)
meth public void visitLogicalLines(org.netbeans.lib.terminalemulator.Coord,org.netbeans.lib.terminalemulator.Coord,org.netbeans.lib.terminalemulator.LogicalLineVisitor)
supr javax.swing.JComponent
hfds CULL_FREQUENCY,DEFAULT_DEBUG,DO_MARGINS,ESC,MODULO,TRANSPARENT,accessible_context,active_color,actual_background,actual_foreground,allowedActions,altSendsEscape,anchored,base_stream,bot_margin,buf,charsInPrehistory,check_selection,click_to_type,completedSequences,cull_count,cursor_visible,dce_end,debug,debug_gutter_width,default_word_delineator,delimiters,drag_point,dte_end,firsta,fixedFont,glyph_gutter_width,glyph_images,has_focus,history_size,horizontally_scrollable,hscroll_bar,hscroll_count,hscroll_wrapper,input_listeners,interp,keymap,keystroke_set,lastMemUse,left_down_point,listeners,metrics,n_linefeeds,n_paint,n_putchar,n_putchars,n_repaint,newp,old_extent,old_rows,onMac,ops,palette,passOn,read_only,refresh_enabled,region_manager,renderingHints,reverse_video,screen,scroll_on_input,scroll_on_output,scroller,scrolling_direction,sel,selection_xor,sequenceLogging,size_rounded,st,systemClipboard,systemSelection,tab_size,top_margin,totcols,track_cursor,unrecognizedSequences,vscroll_bar,word_delineator,xferBuf
hcls BaseTermStream,MemUse,MouseWheelHandler,OpsImpl,ScrollWrapper,Scroller

CLSS protected org.netbeans.lib.terminalemulator.Term$AccessibleTerm
 outer org.netbeans.lib.terminalemulator.Term
cons protected init(org.netbeans.lib.terminalemulator.Term)
meth public javax.accessibility.AccessibleRole getAccessibleRole()
meth public void setAccessibleName(java.lang.String)
supr javax.swing.JComponent$AccessibleJComponent

CLSS public static org.netbeans.lib.terminalemulator.Term$ExternalCommandsConstants
 outer org.netbeans.lib.terminalemulator.Term
cons public init()
fld public final static java.lang.String COMMAND_PREFIX = "ext[::] "
fld public final static java.lang.String EXECUTION_ENV_PROPERTY_KEY = "ExecutionEnvironment_KEY"
fld public final static java.lang.String IDE_OPEN = "ideopen"
supr java.lang.Object

CLSS public org.netbeans.lib.terminalemulator.TermAdapter
cons public init()
intf org.netbeans.lib.terminalemulator.TermListener
meth public void cwdChanged(java.lang.String)
meth public void externalToolCalled(java.lang.String)
meth public void sizeChanged(java.awt.Dimension,java.awt.Dimension)
meth public void titleChanged(java.lang.String)
supr java.lang.Object

CLSS public abstract interface org.netbeans.lib.terminalemulator.TermInputListener
meth public abstract void sendChar(char)
meth public abstract void sendChars(char[],int,int)

CLSS public abstract interface org.netbeans.lib.terminalemulator.TermListener
meth public abstract void cwdChanged(java.lang.String)
meth public abstract void externalToolCalled(java.lang.String)
meth public abstract void sizeChanged(java.awt.Dimension,java.awt.Dimension)
meth public abstract void titleChanged(java.lang.String)

CLSS public abstract org.netbeans.lib.terminalemulator.TermStream
cons public init()
fld protected org.netbeans.lib.terminalemulator.TermStream toDCE
fld protected org.netbeans.lib.terminalemulator.TermStream toDTE
meth protected org.netbeans.lib.terminalemulator.Term getTerm()
meth public abstract void flush()
meth public abstract void putChar(char)
meth public abstract void putChars(char[],int,int)
meth public abstract void sendChar(char)
meth public abstract void sendChars(char[],int,int)
supr java.lang.Object
hfds term

CLSS public org.netbeans.lib.terminalemulator.WordDelineator
meth protected int charClass(char)
meth protected int findLeft(java.lang.StringBuffer,int,boolean)
meth protected int findRight(java.lang.StringBuffer,int,boolean)
meth public static org.netbeans.lib.terminalemulator.WordDelineator createCustomDelineator(java.lang.String)
meth public static org.netbeans.lib.terminalemulator.WordDelineator createNewlineDelineator()
supr java.lang.Object
hfds delimiters

CLSS public org.netbeans.lib.terminalemulator.support.ColorComboBox
cons public init()
fld public final static java.lang.String PROP_COLOR = "color"
supr java.lang.Object
hfds content
hcls ComboBoxListener

CLSS public org.netbeans.lib.terminalemulator.support.DefaultFindState
cons public init(org.netbeans.lib.terminalemulator.Term)
intf org.netbeans.lib.terminalemulator.support.FindState
meth public boolean isVisible()
meth public java.lang.String getPattern()
meth public org.netbeans.lib.terminalemulator.support.FindState$Status getStatus()
meth public void next()
meth public void prev()
meth public void setPattern(java.lang.String)
meth public void setVisible(boolean)
supr java.lang.Object
hfds backwardVisitor,direction,extent,forwardVisitor,found,pattern,status,tentative,term,visible
hcls Direction

CLSS public final org.netbeans.lib.terminalemulator.support.FindBar
cons public init(org.netbeans.lib.terminalemulator.support.FindBar$Owner)
innr public abstract interface static Owner
meth public org.netbeans.lib.terminalemulator.support.FindState getState()
meth public void requestTextFocus()
meth public void setState(org.netbeans.lib.terminalemulator.support.FindState)
supr javax.swing.JPanel
hfds BUTTON_INSETS,closeAction,errorLabel,findText,nextAction,originalColor,owner,prevAction,state,updating
hcls CloseAction,NextAction,PrevAction

CLSS public abstract interface static org.netbeans.lib.terminalemulator.support.FindBar$Owner
 outer org.netbeans.lib.terminalemulator.support.FindBar
meth public abstract void close(org.netbeans.lib.terminalemulator.support.FindBar)

CLSS public abstract interface org.netbeans.lib.terminalemulator.support.FindState
innr public final static !enum Status
meth public abstract boolean isVisible()
meth public abstract java.lang.String getPattern()
meth public abstract org.netbeans.lib.terminalemulator.support.FindState$Status getStatus()
meth public abstract void next()
meth public abstract void prev()
meth public abstract void setPattern(java.lang.String)
meth public abstract void setVisible(boolean)

CLSS public final static !enum org.netbeans.lib.terminalemulator.support.FindState$Status
 outer org.netbeans.lib.terminalemulator.support.FindState
fld public final static org.netbeans.lib.terminalemulator.support.FindState$Status EMPTYPATTERN
fld public final static org.netbeans.lib.terminalemulator.support.FindState$Status NOTFOUND
fld public final static org.netbeans.lib.terminalemulator.support.FindState$Status OK
fld public final static org.netbeans.lib.terminalemulator.support.FindState$Status WILLWRAP
meth public static org.netbeans.lib.terminalemulator.support.FindState$Status valueOf(java.lang.String)
meth public static org.netbeans.lib.terminalemulator.support.FindState$Status[] values()
supr java.lang.Enum<org.netbeans.lib.terminalemulator.support.FindState$Status>

CLSS public abstract org.netbeans.lib.terminalemulator.support.LineFilter
cons public init()
innr protected abstract interface static LineSink
meth protected final java.lang.String hyperlink(java.lang.String,java.lang.String)
meth public abstract void processLine(java.lang.String,org.netbeans.lib.terminalemulator.support.LineFilter$LineSink)
meth public static void pushInto(org.netbeans.lib.terminalemulator.support.LineFilter,org.netbeans.lib.terminalemulator.Term,int)
supr java.lang.Object
hcls LineProcessorBridge

CLSS protected abstract interface static org.netbeans.lib.terminalemulator.support.LineFilter$LineSink
 outer org.netbeans.lib.terminalemulator.support.LineFilter
meth public abstract void forwardLine(java.lang.String)

CLSS public final org.netbeans.lib.terminalemulator.support.TermOptions
fld public final static int MAX_FONT_SIZE = 48
fld public final static int MAX_HISTORY_SIZE = 50000
fld public final static int MAX_TAB_SIZE = 16
fld public final static int MIN_FONT_SIZE = 8
fld public final static int MIN_HISTORY_SIZE = 0
fld public final static int MIN_TAB_SIZE = 1
meth public boolean getAltSendsEscape()
meth public boolean getClickToType()
meth public boolean getIgnoreKeymap()
meth public boolean getLineWrap()
meth public boolean getScrollOnInput()
meth public boolean getScrollOnOutput()
meth public boolean isDirty()
meth public final void resetToDefault()
meth public int getFontSize()
meth public int getHistorySize()
meth public int getTabSize()
meth public java.awt.Color getBackground()
meth public java.awt.Color getForeground()
meth public java.awt.Color getSelectionBackground()
meth public java.awt.Font getFont()
meth public java.lang.String getSelectByWordDelimiters()
meth public org.netbeans.lib.terminalemulator.support.TermOptions makeCopy()
meth public static org.netbeans.lib.terminalemulator.support.TermOptions getDefault(java.util.prefs.Preferences)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void assign(org.netbeans.lib.terminalemulator.support.TermOptions)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setAltSendsEscape(boolean)
meth public void setBackground(java.awt.Color)
meth public void setClickToType(boolean)
meth public void setFont(java.awt.Font)
meth public void setFontSize(int)
meth public void setForeground(java.awt.Color)
meth public void setHistorySize(int)
meth public void setIgnoreKeymap(boolean)
meth public void setLineWrap(boolean)
meth public void setScrollOnInput(boolean)
meth public void setScrollOnOutput(boolean)
meth public void setSelectByWordDelimiters(java.lang.String)
meth public void setSelectionBackground(java.awt.Color)
meth public void setTabSize(int)
meth public void storeTo(java.util.prefs.Preferences)
supr java.lang.Object
hfds DEFAULT,PREFIX,PROP_ALT_SENDS_ESCAPE,PROP_BACKGROUND,PROP_CLICK_TO_TYPE,PROP_FONT_FAMILY,PROP_FONT_SIZE,PROP_FONT_STYLE,PROP_FOREGROUND,PROP_HISTORY_SIZE,PROP_IGNORE_KEYMAP,PROP_LINE_WRAP,PROP_SCROLL_ON_INPUT,PROP_SCROLL_ON_OUTPUT,PROP_SELECTION_BACKGROUND,PROP_SELECT_BY_WORD_DELIMITERS,PROP_TAB_SIZE,altSendsEscape,altSendsEscapeDefault,background,backgroundDefault,clickToType,clickToTypeDefault,dirty,font,fontDefault,fontSize,fontSizeDefault,foreground,foregroundDefault,historySize,historySizeDefault,ignoreKeymap,ignoreKeymapDefault,lineWrap,lineWrapDefault,pcs,preferences,scrollOnInput,scrollOnInputDefault,scrollOnOutput,scrollOnOutputDefault,selectByWordDelimiters,selectByWordDelimitersDefault,selectionBackground,selectionBackgroundDefault,tabSize,tabSizeDefault

CLSS public final org.netbeans.lib.terminalemulator.support.TermOptionsPanel
cons public init()
meth public void setTermOptions(org.netbeans.lib.terminalemulator.support.TermOptions)
supr javax.swing.JPanel
hfds CTL_ClickToType,CTL_Ellipsis,CTL_Restore,CTL_ScrollOnInput,CTL_ScrollOnOutput,LBL_BackgroundColor,LBL_Font,LBL_FontSize,LBL_ForegroundColor,LBL_HistorySize,LBL_IgnoreKeymap,LBL_Options,LBL_Preview,LBL_SelectionBackgroundColor,LBL_TabSize,LBL_WrapLines,MNM_BackgroundColor,MNM_ClickToType,MNM_Font,MNM_FontSize,MNM_ForegroundColor,MNM_HistorySize,MNM_IgnoreKeymap,MNM_Preview,MNM_Restore,MNM_ScrollOnInput,MNM_ScrollOnOutput,MNM_SelectionBackgroundColor,MNM_TabSize,MNM_WrapLines,backgroundComboBox,backgroundLabel,clickToTypeCheckBox,descriptionLabel,fontButton,fontLabel,fontSizeLabel,fontSizeSpinner,fontText,foregroundComboBox,foregroundLabel,historySizeLabel,historySizeSpinner,ignoreKeymapCheckBox,inApplyingModel,lineWrapCheckBox,previewLabel,previewPanel,propertyListener,restoreButton,scrollOnInputCheckBox,scrollOnOutputCheckBox,selectionComboBox,selectionLabel,tabSizeLabel,tabSizeSpinner,term,termOptions

