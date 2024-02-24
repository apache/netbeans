#Signature file v4.1
#Version 1.70.0

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

CLSS public abstract org.netbeans.modules.refactoring.api.AbstractRefactoring
cons protected init(org.openide.util.Lookup)
fld public final static int INIT = 0
fld public final static int PARAMETERS_CHECK = 2
fld public final static int PREPARE = 3
fld public final static int PRE_CHECK = 1
meth public final org.netbeans.modules.refactoring.api.Context getContext()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public final org.netbeans.modules.refactoring.api.Problem checkParameters()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final org.netbeans.modules.refactoring.api.Problem fastCheckParameters()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final org.netbeans.modules.refactoring.api.Problem preCheck()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final org.netbeans.modules.refactoring.api.Problem prepare(org.netbeans.modules.refactoring.api.RefactoringSession)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public final org.openide.util.Lookup getRefactoringSource()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public final void addProgressListener(org.netbeans.modules.refactoring.api.ProgressListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public final void cancelRequest()
meth public final void removeProgressListener(org.netbeans.modules.refactoring.api.ProgressListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds PLUGIN_STEPS,cancel,currentState,filtersDescription,gbHandlers,plugins,pluginsWithProgress,progressListener,progressSupport,refactoringSource,scope
hcls ProgressL

CLSS public final org.netbeans.modules.refactoring.api.Context
meth public <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookup(org.openide.util.Lookup$Template<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>)
meth public void add(java.lang.Object)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void remove(java.lang.Class<?>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr org.openide.util.Lookup
hfds delegate,instanceContent

CLSS public final org.netbeans.modules.refactoring.api.CopyRefactoring
cons public init(org.openide.util.Lookup)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.openide.util.Lookup getTarget()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public void setTarget(org.openide.util.Lookup)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr org.netbeans.modules.refactoring.api.AbstractRefactoring
hfds target

CLSS public final org.netbeans.modules.refactoring.api.MoveRefactoring
cons public init(org.openide.util.Lookup)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.openide.util.Lookup getTarget()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public void setTarget(org.openide.util.Lookup)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr org.netbeans.modules.refactoring.api.AbstractRefactoring
hfds target

CLSS public final org.netbeans.modules.refactoring.api.MultipleCopyRefactoring
cons public init(org.openide.util.Lookup)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.openide.util.Lookup getTarget()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public void setTarget(org.openide.util.Lookup)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr org.netbeans.modules.refactoring.api.AbstractRefactoring
hfds target

CLSS public final org.netbeans.modules.refactoring.api.Problem
cons public init(boolean,java.lang.String)
 anno 2 org.netbeans.api.annotations.common.NonNull()
cons public init(boolean,java.lang.String,org.netbeans.modules.refactoring.api.ProblemDetails)
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public boolean isFatal()
meth public java.lang.String getMessage()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.refactoring.api.Problem getNext()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.refactoring.api.ProblemDetails getDetails()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public void setNext(org.netbeans.modules.refactoring.api.Problem)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds details,fatal,message,next

CLSS public final org.netbeans.modules.refactoring.api.ProblemDetails
meth public java.lang.String getDetailsHint()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public void showDetails(javax.swing.Action,org.openide.util.Cancellable)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds pdi

CLSS public final org.netbeans.modules.refactoring.api.ProgressEvent
cons public init(java.lang.Object,int)
 anno 1 org.netbeans.api.annotations.common.NonNull()
cons public init(java.lang.Object,int,int,int)
 anno 1 org.netbeans.api.annotations.common.NonNull()
fld public final static int START = 1
fld public final static int STEP = 2
fld public final static int STOP = 4
meth public int getCount()
meth public int getEventId()
meth public int getOperationType()
supr java.util.EventObject
hfds count,eventId,operationType

CLSS public abstract interface org.netbeans.modules.refactoring.api.ProgressListener
intf java.util.EventListener
meth public abstract void start(org.netbeans.modules.refactoring.api.ProgressEvent)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void step(org.netbeans.modules.refactoring.api.ProgressEvent)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void stop(org.netbeans.modules.refactoring.api.ProgressEvent)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public final org.netbeans.modules.refactoring.api.RefactoringElement
fld public final static int GUARDED = 2
fld public final static int NORMAL = 0
fld public final static int READ_ONLY = 3
fld public final static int WARNING = 1
meth public boolean equals(java.lang.Object)
meth public boolean include(org.netbeans.modules.refactoring.spi.FiltersManager)
meth public boolean isEnabled()
meth public int getStatus()
meth public int hashCode()
meth public java.lang.String getDisplayText()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getText()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.openide.filesystems.FileObject getParentFile()
meth public org.openide.text.PositionBounds getPosition()
meth public org.openide.util.Lookup getLookup()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public void openInEditor()
meth public void setEnabled(boolean)
meth public void showPreview()
supr java.lang.Object
hfds impl

CLSS public final org.netbeans.modules.refactoring.api.RefactoringSession
meth public java.util.Collection<org.netbeans.modules.refactoring.api.RefactoringElement> getRefactoringElements()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.refactoring.api.Problem doRefactoring(boolean)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.refactoring.api.Problem undoRefactoring(boolean)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.refactoring.api.RefactoringSession create(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void addProgressListener(org.netbeans.modules.refactoring.api.ProgressListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void finished()
meth public void removeProgressListener(org.netbeans.modules.refactoring.api.ProgressListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds COMMITSTEPS,bag,description,finished,internalList,progressSupport,realcommit,refactoringElements,undoManager
hcls ElementsCollection,ProgressL

CLSS public final org.netbeans.modules.refactoring.api.RenameRefactoring
cons public init(org.openide.util.Lookup)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public boolean isSearchInComments()
meth public java.lang.String getNewName()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public void setNewName(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void setSearchInComments(boolean)
supr org.netbeans.modules.refactoring.api.AbstractRefactoring
hfds newName,searchInComments

CLSS public final org.netbeans.modules.refactoring.api.SafeDeleteRefactoring
cons public init(org.openide.util.Lookup)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public boolean isCheckInComments()
meth public void setCheckInComments(boolean)
supr org.netbeans.modules.refactoring.api.AbstractRefactoring
hfds checkInComments

CLSS public final org.netbeans.modules.refactoring.api.Scope
meth public boolean isDependencies()
meth public java.util.Set<org.netbeans.api.fileinfo.NonRecursiveFolder> getFolders()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.util.Set<org.openide.filesystems.FileObject> getFiles()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.util.Set<org.openide.filesystems.FileObject> getSourceRoots()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.refactoring.api.Scope create(java.util.Collection<org.openide.filesystems.FileObject>,java.util.Collection<org.netbeans.api.fileinfo.NonRecursiveFolder>,java.util.Collection<org.openide.filesystems.FileObject>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.modules.refactoring.api.Scope create(java.util.Collection<org.openide.filesystems.FileObject>,java.util.Collection<org.netbeans.api.fileinfo.NonRecursiveFolder>,java.util.Collection<org.openide.filesystems.FileObject>,boolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds dependencies,files,folders,sourceRoots

CLSS public final org.netbeans.modules.refactoring.api.SingleCopyRefactoring
cons public init(org.openide.util.Lookup)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getNewName()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.openide.util.Lookup getTarget()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public void setNewName(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void setTarget(org.openide.util.Lookup)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr org.netbeans.modules.refactoring.api.AbstractRefactoring
hfds newName,target

CLSS public final org.netbeans.modules.refactoring.api.WhereUsedQuery
cons public init(org.openide.util.Lookup)
 anno 1 org.netbeans.api.annotations.common.NonNull()
fld public final static java.lang.String FIND_REFERENCES = "FIND_REFERENCES"
fld public final static java.lang.String SEARCH_IN_COMMENTS = "SEARCH_IN_COMMENTS"
meth public final boolean getBooleanValue(java.lang.Object)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public final void putValue(java.lang.Object,java.lang.Object)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public final void setRefactoringSource(org.openide.util.Lookup)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr org.netbeans.modules.refactoring.api.AbstractRefactoring
hfds hash

CLSS public final org.netbeans.modules.refactoring.api.ui.ExplorerContext
cons public init()
meth public boolean isDelete()
meth public java.awt.datatransfer.Transferable getTransferable()
meth public java.lang.String getNewName()
meth public org.openide.nodes.Node getTargetNode()
meth public void setDelete(boolean)
meth public void setNewName(java.lang.String)
meth public void setTargetNode(org.openide.nodes.Node)
meth public void setTransferable(java.awt.datatransfer.Transferable)
supr java.lang.Object
hfds isDelete,newName,targetNode,transferable

CLSS public final org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory
fld public final static java.awt.event.ActionEvent DEFAULT_EVENT
meth public static javax.swing.Action editorSubmenuAction()
meth public static org.openide.util.ContextAwareAction copyAction()
meth public static org.openide.util.ContextAwareAction moveAction()
meth public static org.openide.util.ContextAwareAction popupSubmenuAction()
meth public static org.openide.util.ContextAwareAction renameAction()
meth public static org.openide.util.ContextAwareAction safeDeleteAction()
meth public static org.openide.util.ContextAwareAction whereUsedAction()
supr java.lang.Object

CLSS public abstract org.netbeans.modules.refactoring.spi.BackupFacility
 anno 0 java.lang.Deprecated()
innr public abstract interface static Handle
meth public abstract !varargs org.netbeans.modules.refactoring.spi.BackupFacility$Handle backup(org.openide.filesystems.FileObject[]) throws java.io.IOException
meth public abstract void clear()
meth public final org.netbeans.modules.refactoring.spi.BackupFacility$Handle backup(java.util.Collection<? extends org.openide.filesystems.FileObject>) throws java.io.IOException
meth public static org.netbeans.modules.refactoring.spi.BackupFacility getDefault()
supr java.lang.Object
hfds defaultInstance
hcls DefaultHandle,DefaultImpl

CLSS public abstract interface static org.netbeans.modules.refactoring.spi.BackupFacility$Handle
 outer org.netbeans.modules.refactoring.spi.BackupFacility
meth public abstract void restore() throws java.io.IOException

CLSS public abstract org.netbeans.modules.refactoring.spi.FiltersManager
cons public init()
innr public abstract interface static Filterable
meth public abstract boolean isSelected(java.lang.String)
supr java.lang.Object

CLSS public abstract interface static org.netbeans.modules.refactoring.spi.FiltersManager$Filterable
 outer org.netbeans.modules.refactoring.spi.FiltersManager
meth public abstract boolean filter(org.netbeans.modules.refactoring.spi.FiltersManager)

CLSS public abstract interface org.netbeans.modules.refactoring.spi.GuardedBlockHandler
meth public abstract org.netbeans.modules.refactoring.api.Problem handleChange(org.netbeans.modules.refactoring.spi.RefactoringElementImplementation,java.util.Collection<org.netbeans.modules.refactoring.spi.RefactoringElementImplementation>,java.util.Collection<org.netbeans.modules.refactoring.spi.Transaction>)

CLSS public abstract interface org.netbeans.modules.refactoring.spi.GuardedBlockHandlerFactory
meth public abstract org.netbeans.modules.refactoring.spi.GuardedBlockHandler createInstance(org.netbeans.modules.refactoring.api.AbstractRefactoring)

CLSS public abstract interface org.netbeans.modules.refactoring.spi.ModificationResult
meth public abstract java.lang.String getResultingSource(org.openide.filesystems.FileObject) throws java.io.IOException
meth public abstract java.util.Collection<? extends java.io.File> getNewFiles()
meth public abstract java.util.Collection<? extends org.openide.filesystems.FileObject> getModifiedFileObjects()
meth public abstract void commit() throws java.io.IOException

CLSS public org.netbeans.modules.refactoring.spi.ProblemDetailsFactory
meth public static org.netbeans.modules.refactoring.api.ProblemDetails createProblemDetails(org.netbeans.modules.refactoring.spi.ProblemDetailsImplementation)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.refactoring.spi.ProblemDetailsImplementation
meth public abstract java.lang.String getDetailsHint()
meth public abstract void showDetails(javax.swing.Action,org.openide.util.Cancellable)

CLSS public abstract interface org.netbeans.modules.refactoring.spi.ProgressProvider
meth public abstract void addProgressListener(org.netbeans.modules.refactoring.api.ProgressListener)
meth public abstract void removeProgressListener(org.netbeans.modules.refactoring.api.ProgressListener)

CLSS public org.netbeans.modules.refactoring.spi.ProgressProviderAdapter
cons protected init()
intf org.netbeans.modules.refactoring.spi.ProgressProvider
meth protected final void fireProgressListenerStart(int,int)
meth protected final void fireProgressListenerStep()
meth protected final void fireProgressListenerStep(int)
meth protected final void fireProgressListenerStop()
meth public void addProgressListener(org.netbeans.modules.refactoring.api.ProgressListener)
meth public void removeProgressListener(org.netbeans.modules.refactoring.api.ProgressListener)
supr java.lang.Object
hfds progressSupport

CLSS public abstract interface org.netbeans.modules.refactoring.spi.ReadOnlyFilesHandler
meth public abstract org.netbeans.modules.refactoring.api.Problem createProblem(org.netbeans.modules.refactoring.api.RefactoringSession,java.util.Collection)

CLSS public final org.netbeans.modules.refactoring.spi.RefactoringCommit
cons public init(java.util.Collection<? extends org.netbeans.modules.refactoring.spi.ModificationResult>)
intf org.netbeans.modules.refactoring.spi.ProgressProvider
intf org.netbeans.modules.refactoring.spi.Transaction
meth public void addProgressListener(org.netbeans.modules.refactoring.api.ProgressListener)
meth public void commit()
meth public void removeProgressListener(org.netbeans.modules.refactoring.api.ProgressListener)
meth public void rollback()
supr java.lang.Object
hfds LOG,commited,ids,newFilesStored,progressSupport,results

CLSS public abstract interface org.netbeans.modules.refactoring.spi.RefactoringElementImplementation
fld public final static int GUARDED = 2
fld public final static int NORMAL = 0
fld public final static int READ_ONLY = 3
fld public final static int WARNING = 1
meth public abstract boolean isEnabled()
meth public abstract int getStatus()
meth public abstract java.lang.String getDisplayText()
meth public abstract java.lang.String getText()
meth public abstract org.openide.filesystems.FileObject getParentFile()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.openide.text.PositionBounds getPosition()
meth public abstract org.openide.util.Lookup getLookup()
meth public abstract void openInEditor()
meth public abstract void performChange()
meth public abstract void setEnabled(boolean)
meth public abstract void setStatus(int)
meth public abstract void showPreview()
meth public abstract void undoChange()

CLSS public final org.netbeans.modules.refactoring.spi.RefactoringElementsBag
meth public org.netbeans.modules.refactoring.api.Problem add(org.netbeans.modules.refactoring.api.AbstractRefactoring,org.netbeans.modules.refactoring.spi.RefactoringElementImplementation)
meth public org.netbeans.modules.refactoring.api.Problem addAll(org.netbeans.modules.refactoring.api.AbstractRefactoring,java.util.Collection<org.netbeans.modules.refactoring.spi.RefactoringElementImplementation>)
meth public org.netbeans.modules.refactoring.api.Problem addFileChange(org.netbeans.modules.refactoring.api.AbstractRefactoring,org.netbeans.modules.refactoring.spi.RefactoringElementImplementation)
meth public org.netbeans.modules.refactoring.api.RefactoringSession getSession()
meth public void registerTransaction(org.netbeans.modules.refactoring.spi.Transaction)
supr java.lang.Object
hfds commits,delegate,fileChanges,hasGuarded,hasReadOnly,readOnlyFiles,session

CLSS public abstract interface org.netbeans.modules.refactoring.spi.RefactoringPlugin
meth public abstract org.netbeans.modules.refactoring.api.Problem checkParameters()
meth public abstract org.netbeans.modules.refactoring.api.Problem fastCheckParameters()
meth public abstract org.netbeans.modules.refactoring.api.Problem preCheck()
meth public abstract org.netbeans.modules.refactoring.api.Problem prepare(org.netbeans.modules.refactoring.spi.RefactoringElementsBag)
meth public abstract void cancelRequest()

CLSS public abstract interface org.netbeans.modules.refactoring.spi.RefactoringPluginFactory
meth public abstract org.netbeans.modules.refactoring.spi.RefactoringPlugin createInstance(org.netbeans.modules.refactoring.api.AbstractRefactoring)

CLSS public abstract org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation
cons public init()
intf org.netbeans.modules.refactoring.spi.RefactoringElementImplementation
meth protected java.lang.String getNewFileContent()
meth public boolean isEnabled()
meth public int getStatus()
meth public void openInEditor()
meth public void setEnabled(boolean)
meth public void setStatus(int)
meth public void showPreview()
meth public void undoChange()
supr java.lang.Object
hfds enabled,status

CLSS public abstract interface org.netbeans.modules.refactoring.spi.Transaction
meth public abstract void commit()
meth public abstract void rollback()

CLSS public abstract org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider
cons public init()
meth public boolean canCopy(org.openide.util.Lookup)
meth public boolean canDelete(org.openide.util.Lookup)
meth public boolean canFindUsages(org.openide.util.Lookup)
meth public boolean canMove(org.openide.util.Lookup)
meth public boolean canRename(org.openide.util.Lookup)
meth public void doCopy(org.openide.util.Lookup)
meth public void doDelete(org.openide.util.Lookup)
meth public void doFindUsages(org.openide.util.Lookup)
meth public void doMove(org.openide.util.Lookup)
meth public void doRename(org.openide.util.Lookup)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel
meth public abstract java.awt.Component getComponent()
meth public abstract void initialize()

CLSS public abstract interface org.netbeans.modules.refactoring.spi.ui.ExpandableTreeElement
intf java.lang.Iterable<org.netbeans.modules.refactoring.spi.ui.TreeElement>
intf org.netbeans.modules.refactoring.spi.ui.TreeElement
intf org.openide.util.Cancellable
meth public abstract int estimateChildCount()

CLSS public final org.netbeans.modules.refactoring.spi.ui.FiltersDescription
cons public init()
innr public abstract interface static Provider
meth public boolean isEnabled(int)
meth public boolean isSelected(int)
meth public int getFilterCount()
meth public java.lang.String getKey(int)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getTooltip(int)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public javax.swing.Icon getIcon(int)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public void addFilter(java.lang.String,java.lang.String,boolean,javax.swing.Icon)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
meth public void enable(int)
meth public void enable(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void setSelected(int,boolean)
supr java.lang.Object
hfds filters
hcls FilterItem

CLSS public abstract interface static org.netbeans.modules.refactoring.spi.ui.FiltersDescription$Provider
 outer org.netbeans.modules.refactoring.spi.ui.FiltersDescription
meth public abstract void addFilters(org.netbeans.modules.refactoring.spi.ui.FiltersDescription)
meth public abstract void enableFilters(org.netbeans.modules.refactoring.spi.ui.FiltersDescription)

CLSS public abstract interface org.netbeans.modules.refactoring.spi.ui.RefactoringCustomUI
meth public abstract java.awt.Component getCustomComponent(java.util.Collection<org.netbeans.modules.refactoring.api.RefactoringElement>)
meth public abstract java.lang.String getCustomToolTip()
meth public abstract javax.swing.Icon getCustomIcon()

CLSS public abstract interface org.netbeans.modules.refactoring.spi.ui.RefactoringUI
meth public abstract boolean hasParameters()
meth public abstract boolean isQuery()
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getName()
meth public abstract org.netbeans.modules.refactoring.api.AbstractRefactoring getRefactoring()
meth public abstract org.netbeans.modules.refactoring.api.Problem checkParameters()
meth public abstract org.netbeans.modules.refactoring.api.Problem setParameters()
meth public abstract org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel getPanel(javax.swing.event.ChangeListener)
meth public abstract org.openide.util.HelpCtx getHelpCtx()

CLSS public abstract interface org.netbeans.modules.refactoring.spi.ui.RefactoringUIBypass
meth public abstract boolean isRefactoringBypassRequired()
meth public abstract void doRefactoringBypass() throws java.io.IOException

CLSS public final org.netbeans.modules.refactoring.spi.ui.ScopePanel
cons public init()
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,java.util.prefs.Preferences,java.lang.String)
cons public init(java.lang.String,java.util.prefs.Preferences,java.lang.String,javax.swing.event.ChangeListener)
meth public boolean initialize(org.openide.util.Lookup,java.util.concurrent.atomic.AtomicBoolean)
meth public org.netbeans.modules.refactoring.api.Scope getSelectedScope()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.refactoring.spi.ui.ScopeProvider getSelectedScopeProvider()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public void selectScopeById(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr javax.swing.JPanel
hfds ELLIPSIS,SCOPE_COMBOBOX_COLUMNS,btnCustomScope,id,parent,preferences,preferencesKey,scopeCombobox,scopes
hcls ScopeAction,ScopeDescriptionRenderer

CLSS public abstract org.netbeans.modules.refactoring.spi.ui.ScopeProvider
cons public init()
innr public abstract interface static !annotation Registration
innr public abstract static CustomScopeProvider
meth public abstract boolean initialize(org.openide.util.Lookup,java.util.concurrent.atomic.AtomicBoolean)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.refactoring.api.Scope getScope()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getDetail()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public javax.swing.Icon getIcon()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.refactoring.api.Problem getProblem()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
supr java.lang.Object

CLSS public abstract static org.netbeans.modules.refactoring.spi.ui.ScopeProvider$CustomScopeProvider
 outer org.netbeans.modules.refactoring.spi.ui.ScopeProvider
cons public init()
meth public abstract boolean showCustomizer()
meth public abstract void setScope(org.netbeans.modules.refactoring.api.Scope)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
supr org.netbeans.modules.refactoring.spi.ui.ScopeProvider

CLSS public abstract interface static !annotation org.netbeans.modules.refactoring.spi.ui.ScopeProvider$Registration
 outer org.netbeans.modules.refactoring.spi.ui.ScopeProvider
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()
meth public abstract !hasdefault java.lang.String iconBase()
meth public abstract java.lang.String displayName()
meth public abstract java.lang.String id()

CLSS public abstract interface !annotation org.netbeans.modules.refactoring.spi.ui.ScopeReference
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String id()
meth public abstract java.lang.String path()

CLSS public abstract interface !annotation org.netbeans.modules.refactoring.spi.ui.ScopeReferences
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract org.netbeans.modules.refactoring.spi.ui.ScopeReference[] value()

CLSS public abstract interface org.netbeans.modules.refactoring.spi.ui.TreeElement
meth public abstract java.lang.Object getUserObject()
meth public abstract java.lang.String getText(boolean)
meth public abstract javax.swing.Icon getIcon()
meth public abstract org.netbeans.modules.refactoring.spi.ui.TreeElement getParent(boolean)

CLSS public final org.netbeans.modules.refactoring.spi.ui.TreeElementFactory
meth public static org.netbeans.modules.refactoring.spi.ui.TreeElement getTreeElement(java.lang.Object)
supr java.lang.Object
hfds implementations

CLSS public abstract interface org.netbeans.modules.refactoring.spi.ui.TreeElementFactoryImplementation
meth public abstract org.netbeans.modules.refactoring.spi.ui.TreeElement getTreeElement(java.lang.Object)
meth public abstract void cleanUp()

CLSS public final org.netbeans.modules.refactoring.spi.ui.UI
innr public final static !enum Constants
meth public static boolean setComponentForRefactoringPreview(java.awt.Component)
meth public static void openRefactoringUI(org.netbeans.modules.refactoring.spi.ui.RefactoringUI)
meth public static void openRefactoringUI(org.netbeans.modules.refactoring.spi.ui.RefactoringUI,org.netbeans.modules.refactoring.api.RefactoringSession,javax.swing.Action)
meth public static void openRefactoringUI(org.netbeans.modules.refactoring.spi.ui.RefactoringUI,org.openide.windows.TopComponent)
supr java.lang.Object

CLSS public final static !enum org.netbeans.modules.refactoring.spi.ui.UI$Constants
 outer org.netbeans.modules.refactoring.spi.ui.UI
fld public final static org.netbeans.modules.refactoring.spi.ui.UI$Constants REQUEST_PREVIEW
meth public static org.netbeans.modules.refactoring.spi.ui.UI$Constants valueOf(java.lang.String)
meth public static org.netbeans.modules.refactoring.spi.ui.UI$Constants[] values()
supr java.lang.Enum<org.netbeans.modules.refactoring.spi.ui.UI$Constants>

CLSS public abstract interface org.openide.util.Cancellable
meth public abstract boolean cancel()

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

