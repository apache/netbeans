#Signature file v4.1
#Version 1.82.0

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

CLSS public abstract interface java.beans.PropertyChangeListener
intf java.util.EventListener
meth public abstract void propertyChange(java.beans.PropertyChangeEvent)

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Cloneable

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation

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

CLSS public abstract interface java.util.concurrent.Callable<%0 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
meth public abstract {java.util.concurrent.Callable%0} call() throws java.lang.Exception

CLSS public abstract interface javax.accessibility.Accessible
meth public abstract javax.accessibility.AccessibleContext getAccessibleContext()

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

CLSS public abstract javax.swing.text.AbstractDocument
cons protected init(javax.swing.text.AbstractDocument$Content)
cons protected init(javax.swing.text.AbstractDocument$Content,javax.swing.text.AbstractDocument$AttributeContext)
fld protected final static java.lang.String BAD_LOCATION = "document location failure"
fld protected javax.swing.event.EventListenerList listenerList
fld public final static java.lang.String BidiElementName = "bidi level"
fld public final static java.lang.String ContentElementName = "content"
fld public final static java.lang.String ElementNameAttribute = "$ename"
fld public final static java.lang.String ParagraphElementName = "paragraph"
fld public final static java.lang.String SectionElementName = "section"
innr public BranchElement
innr public DefaultDocumentEvent
innr public LeafElement
innr public abstract AbstractElement
innr public abstract interface static AttributeContext
innr public abstract interface static Content
innr public static ElementEdit
intf java.io.Serializable
intf javax.swing.text.Document
meth protected final java.lang.Thread getCurrentWriter()
meth protected final javax.swing.text.AbstractDocument$AttributeContext getAttributeContext()
meth protected final javax.swing.text.AbstractDocument$Content getContent()
meth protected final void writeLock()
meth protected final void writeUnlock()
meth protected javax.swing.text.Element createBranchElement(javax.swing.text.Element,javax.swing.text.AttributeSet)
meth protected javax.swing.text.Element createLeafElement(javax.swing.text.Element,javax.swing.text.AttributeSet,int,int)
meth protected void fireChangedUpdate(javax.swing.event.DocumentEvent)
meth protected void fireInsertUpdate(javax.swing.event.DocumentEvent)
meth protected void fireRemoveUpdate(javax.swing.event.DocumentEvent)
meth protected void fireUndoableEditUpdate(javax.swing.event.UndoableEditEvent)
meth protected void insertUpdate(javax.swing.text.AbstractDocument$DefaultDocumentEvent,javax.swing.text.AttributeSet)
meth protected void postRemoveUpdate(javax.swing.text.AbstractDocument$DefaultDocumentEvent)
meth protected void removeUpdate(javax.swing.text.AbstractDocument$DefaultDocumentEvent)
meth public <%0 extends java.util.EventListener> {%%0}[] getListeners(java.lang.Class<{%%0}>)
meth public abstract javax.swing.text.Element getDefaultRootElement()
meth public abstract javax.swing.text.Element getParagraphElement(int)
meth public final java.lang.Object getProperty(java.lang.Object)
meth public final javax.swing.text.Position getEndPosition()
meth public final javax.swing.text.Position getStartPosition()
meth public final void putProperty(java.lang.Object,java.lang.Object)
meth public final void readLock()
meth public final void readUnlock()
meth public int getAsynchronousLoadPriority()
meth public int getLength()
meth public java.lang.String getText(int,int) throws javax.swing.text.BadLocationException
meth public java.util.Dictionary<java.lang.Object,java.lang.Object> getDocumentProperties()
meth public javax.swing.event.DocumentListener[] getDocumentListeners()
meth public javax.swing.event.UndoableEditListener[] getUndoableEditListeners()
meth public javax.swing.text.DocumentFilter getDocumentFilter()
meth public javax.swing.text.Element getBidiRootElement()
meth public javax.swing.text.Element[] getRootElements()
meth public javax.swing.text.Position createPosition(int) throws javax.swing.text.BadLocationException
meth public void addDocumentListener(javax.swing.event.DocumentListener)
meth public void addUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public void dump(java.io.PrintStream)
meth public void getText(int,int,javax.swing.text.Segment) throws javax.swing.text.BadLocationException
meth public void insertString(int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth public void remove(int,int) throws javax.swing.text.BadLocationException
meth public void removeDocumentListener(javax.swing.event.DocumentListener)
meth public void removeUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public void render(java.lang.Runnable)
meth public void replace(int,int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth public void setAsynchronousLoadPriority(int)
meth public void setDocumentFilter(javax.swing.text.DocumentFilter)
meth public void setDocumentProperties(java.util.Dictionary<java.lang.Object,java.lang.Object>)
supr java.lang.Object

CLSS public javax.swing.text.DefaultEditorKit
cons public init()
fld public final static java.lang.String EndOfLineStringProperty = "__EndOfLine__"
fld public final static java.lang.String backwardAction = "caret-backward"
fld public final static java.lang.String beepAction = "beep"
fld public final static java.lang.String beginAction = "caret-begin"
fld public final static java.lang.String beginLineAction = "caret-begin-line"
fld public final static java.lang.String beginParagraphAction = "caret-begin-paragraph"
fld public final static java.lang.String beginWordAction = "caret-begin-word"
fld public final static java.lang.String copyAction = "copy-to-clipboard"
fld public final static java.lang.String cutAction = "cut-to-clipboard"
fld public final static java.lang.String defaultKeyTypedAction = "default-typed"
fld public final static java.lang.String deleteNextCharAction = "delete-next"
fld public final static java.lang.String deleteNextWordAction = "delete-next-word"
fld public final static java.lang.String deletePrevCharAction = "delete-previous"
fld public final static java.lang.String deletePrevWordAction = "delete-previous-word"
fld public final static java.lang.String downAction = "caret-down"
fld public final static java.lang.String endAction = "caret-end"
fld public final static java.lang.String endLineAction = "caret-end-line"
fld public final static java.lang.String endParagraphAction = "caret-end-paragraph"
fld public final static java.lang.String endWordAction = "caret-end-word"
fld public final static java.lang.String forwardAction = "caret-forward"
fld public final static java.lang.String insertBreakAction = "insert-break"
fld public final static java.lang.String insertContentAction = "insert-content"
fld public final static java.lang.String insertTabAction = "insert-tab"
fld public final static java.lang.String nextWordAction = "caret-next-word"
fld public final static java.lang.String pageDownAction = "page-down"
fld public final static java.lang.String pageUpAction = "page-up"
fld public final static java.lang.String pasteAction = "paste-from-clipboard"
fld public final static java.lang.String previousWordAction = "caret-previous-word"
fld public final static java.lang.String readOnlyAction = "set-read-only"
fld public final static java.lang.String selectAllAction = "select-all"
fld public final static java.lang.String selectLineAction = "select-line"
fld public final static java.lang.String selectParagraphAction = "select-paragraph"
fld public final static java.lang.String selectWordAction = "select-word"
fld public final static java.lang.String selectionBackwardAction = "selection-backward"
fld public final static java.lang.String selectionBeginAction = "selection-begin"
fld public final static java.lang.String selectionBeginLineAction = "selection-begin-line"
fld public final static java.lang.String selectionBeginParagraphAction = "selection-begin-paragraph"
fld public final static java.lang.String selectionBeginWordAction = "selection-begin-word"
fld public final static java.lang.String selectionDownAction = "selection-down"
fld public final static java.lang.String selectionEndAction = "selection-end"
fld public final static java.lang.String selectionEndLineAction = "selection-end-line"
fld public final static java.lang.String selectionEndParagraphAction = "selection-end-paragraph"
fld public final static java.lang.String selectionEndWordAction = "selection-end-word"
fld public final static java.lang.String selectionForwardAction = "selection-forward"
fld public final static java.lang.String selectionNextWordAction = "selection-next-word"
fld public final static java.lang.String selectionPreviousWordAction = "selection-previous-word"
fld public final static java.lang.String selectionUpAction = "selection-up"
fld public final static java.lang.String upAction = "caret-up"
fld public final static java.lang.String writableAction = "set-writable"
innr public static BeepAction
innr public static CopyAction
innr public static CutAction
innr public static DefaultKeyTypedAction
innr public static InsertBreakAction
innr public static InsertContentAction
innr public static InsertTabAction
innr public static PasteAction
meth public java.lang.String getContentType()
meth public javax.swing.Action[] getActions()
meth public javax.swing.text.Caret createCaret()
meth public javax.swing.text.Document createDefaultDocument()
meth public javax.swing.text.ViewFactory getViewFactory()
meth public void read(java.io.InputStream,javax.swing.text.Document,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public void read(java.io.Reader,javax.swing.text.Document,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public void write(java.io.OutputStream,javax.swing.text.Document,int,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public void write(java.io.Writer,javax.swing.text.Document,int,int) throws java.io.IOException,javax.swing.text.BadLocationException
supr javax.swing.text.EditorKit

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

CLSS public abstract javax.swing.text.EditorKit
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public abstract java.lang.String getContentType()
meth public abstract javax.swing.Action[] getActions()
meth public abstract javax.swing.text.Caret createCaret()
meth public abstract javax.swing.text.Document createDefaultDocument()
meth public abstract javax.swing.text.ViewFactory getViewFactory()
meth public abstract void read(java.io.InputStream,javax.swing.text.Document,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public abstract void read(java.io.Reader,javax.swing.text.Document,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public abstract void write(java.io.OutputStream,javax.swing.text.Document,int,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public abstract void write(java.io.Writer,javax.swing.text.Document,int,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public java.lang.Object clone()
meth public void deinstall(javax.swing.JEditorPane)
meth public void install(javax.swing.JEditorPane)
supr java.lang.Object

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

CLSS public abstract javax.swing.text.TextAction
cons public init(java.lang.String)
meth protected final javax.swing.text.JTextComponent getFocusedComponent()
meth protected final javax.swing.text.JTextComponent getTextComponent(java.awt.event.ActionEvent)
meth public final static javax.swing.Action[] augmentList(javax.swing.Action[],javax.swing.Action[])
supr javax.swing.AbstractAction

CLSS public abstract interface org.netbeans.api.editor.document.CustomUndoDocument
meth public abstract void addUndoableEdit(javax.swing.undo.UndoableEdit)

CLSS public abstract interface org.netbeans.api.editor.document.LineDocument
intf javax.swing.text.Document
meth public abstract javax.swing.text.Element getParagraphElement(int)
meth public abstract javax.swing.text.Position createPosition(int,javax.swing.text.Position$Bias) throws javax.swing.text.BadLocationException

CLSS public abstract interface org.netbeans.editor.AtomicLockDocument
 anno 0 java.lang.Deprecated()
intf javax.swing.text.Document
meth public abstract void addAtomicLockListener(org.netbeans.editor.AtomicLockListener)
meth public abstract void atomicLock()
meth public abstract void atomicUndo()
meth public abstract void atomicUnlock()
meth public abstract void removeAtomicLockListener(org.netbeans.editor.AtomicLockListener)

CLSS public abstract org.netbeans.editor.BaseAction
cons public init()
cons public init(int)
cons public init(java.lang.String)
cons public init(java.lang.String,int)
fld protected int updateMask
fld public final static int ABBREV_RESET = 4
 anno 0 java.lang.Deprecated()
fld public final static int CLEAR_STATUS_TEXT = 32
fld public final static int MAGIC_POSITION_RESET = 2
fld public final static int NO_RECORDING = 64
fld public final static int SAVE_POSITION = 128
fld public final static int SELECTION_REMOVE = 1
fld public final static int UNDO_MERGE_RESET = 8
fld public final static int WORD_MATCH_RESET = 16
fld public final static java.lang.String ICON_RESOURCE_PROPERTY = "IconResource"
fld public final static java.lang.String LOCALE_DESC_PREFIX = "desc-"
fld public final static java.lang.String LOCALE_POPUP_PREFIX = "popup-"
fld public final static java.lang.String NO_KEYBINDING = "no-keybinding"
fld public final static java.lang.String POPUP_MENU_TEXT = "PopupMenuText"
meth protected boolean asynchonous()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth protected java.lang.Object createDefaultValue(java.lang.String)
meth protected java.lang.Object findValue(java.lang.String)
 anno 0 java.lang.Deprecated()
meth protected java.lang.Object getDefaultShortDescription()
meth protected void actionNameUpdate(java.lang.String)
meth public abstract void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
meth public final void actionPerformed(java.awt.event.ActionEvent)
meth public java.lang.Object getValue(java.lang.String)
meth public java.lang.String getPopupMenuText(javax.swing.text.JTextComponent)
meth public javax.swing.JMenuItem getPopupMenuItem(javax.swing.text.JTextComponent)
meth public void putValue(java.lang.String,java.lang.Object)
meth public void updateComponent(javax.swing.text.JTextComponent)
meth public void updateComponent(javax.swing.text.JTextComponent,int)
supr javax.swing.text.TextAction
hfds UILOG,UI_LOG_DETAILED,recording,serialVersionUID

CLSS public org.netbeans.editor.BaseDocument
cons public init(boolean,java.lang.String)
cons public init(java.lang.Class,boolean)
 anno 0 java.lang.Deprecated()
fld protected boolean inited
fld protected boolean modified
fld protected javax.swing.text.Element defaultRootElem
fld public final static java.lang.String BLOCKS_FINDER_PROP = "blocks-finder"
fld public final static java.lang.String FILE_NAME_PROP = "file-name"
fld public final static java.lang.String FORMATTER = "formatter"
fld public final static java.lang.String ID_PROP = "id"
fld public final static java.lang.String KIT_CLASS_PROP = "kit-class"
fld public final static java.lang.String LINE_BATCH_SIZE = "line-batch-size"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String LINE_LIMIT_PROP = "line-limit"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String LS_CR = "\r"
fld public final static java.lang.String LS_CRLF = "\r\n"
fld public final static java.lang.String LS_LF = "\n"
fld public final static java.lang.String MIME_TYPE_PROP = "mimeType"
fld public final static java.lang.String READ_LINE_SEPARATOR_PROP = "__EndOfLine__"
fld public final static java.lang.String STRING_BWD_FINDER_PROP = "string-bwd-finder"
fld public final static java.lang.String STRING_FINDER_PROP = "string-finder"
fld public final static java.lang.String UNDO_MANAGER_PROP = "undo-manager"
fld public final static java.lang.String WRAP_SEARCH_MARK_PROP = "wrap-search-mark"
fld public final static java.lang.String WRITE_LINE_SEPARATOR_PROP = "write-line-separator"
innr protected static LazyPropertyMap
innr public abstract interface static PropertyEvaluator
intf org.netbeans.api.editor.document.CustomUndoDocument
intf org.netbeans.api.editor.document.LineDocument
intf org.netbeans.editor.AtomicLockDocument
meth protected final int getAtomicDepth()
meth protected java.util.Dictionary createDocumentProperties(java.util.Dictionary)
meth protected org.netbeans.editor.BaseDocumentEvent createDocumentEvent(int,int,javax.swing.event.DocumentEvent$EventType)
meth protected void fireChangedUpdate(javax.swing.event.DocumentEvent)
meth protected void fireInsertUpdate(javax.swing.event.DocumentEvent)
meth protected void fireRemoveUpdate(javax.swing.event.DocumentEvent)
meth protected void fireUndoableEditUpdate(javax.swing.event.UndoableEditEvent)
meth protected void insertUpdate(javax.swing.text.AbstractDocument$DefaultDocumentEvent,javax.swing.text.AttributeSet)
meth protected void postRemoveUpdate(javax.swing.text.AbstractDocument$DefaultDocumentEvent)
meth protected void preInsertCheck(int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth protected void preInsertUpdate(javax.swing.text.AbstractDocument$DefaultDocumentEvent,javax.swing.text.AttributeSet)
meth protected void preRemoveCheck(int,int) throws javax.swing.text.BadLocationException
meth protected void removeUpdate(javax.swing.text.AbstractDocument$DefaultDocumentEvent)
meth public boolean isIdentifierPart(char)
meth public boolean isModifiable()
meth public boolean isModified()
meth public boolean isWhitespace(char)
meth public char[] getChars(int,int) throws javax.swing.text.BadLocationException
meth public char[] getChars(int[]) throws javax.swing.text.BadLocationException
meth public final boolean isAtomicLock()
meth public final java.lang.Class getKitClass()
 anno 0 java.lang.Deprecated()
meth public final void atomicLock()
 anno 0 java.lang.Deprecated()
meth public final void atomicUnlock()
 anno 0 java.lang.Deprecated()
meth public final void breakAtomicLock()
meth public final void extWriteLock()
meth public final void extWriteUnlock()
meth public int find(org.netbeans.editor.Finder,int,int) throws javax.swing.text.BadLocationException
meth public int getShiftWidth()
 anno 0 java.lang.Deprecated()
meth public int getTabSize()
meth public int processText(org.netbeans.editor.TextBatchProcessor,int,int) throws javax.swing.text.BadLocationException
meth public java.lang.String getText(int[]) throws javax.swing.text.BadLocationException
meth public java.lang.String toString()
meth public java.lang.String toStringDetail()
meth public javax.swing.text.Element getDefaultRootElement()
meth public javax.swing.text.Element getParagraphElement(int)
meth public javax.swing.text.Element[] getRootElements()
meth public javax.swing.text.Position createPosition(int,javax.swing.text.Position$Bias) throws javax.swing.text.BadLocationException
meth public org.netbeans.editor.Annotations getAnnotations()
meth public org.netbeans.editor.CharSeq getText()
meth public org.netbeans.editor.SyntaxSupport getSyntaxSupport()
 anno 0 java.lang.Deprecated()
meth public void addAtomicLockListener(org.netbeans.api.editor.document.AtomicLockListener)
meth public void addAtomicLockListener(org.netbeans.editor.AtomicLockListener)
 anno 0 java.lang.Deprecated()
meth public void addDocumentListener(javax.swing.event.DocumentListener)
meth public void addPostModificationDocumentListener(javax.swing.event.DocumentListener)
meth public void addUndoableEdit(javax.swing.undo.UndoableEdit)
meth public void addUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public void addUpdateDocumentListener(javax.swing.event.DocumentListener)
meth public void atomicUndo()
meth public void checkTrailingSpaces(int)
meth public void getChars(int,char[],int,int) throws javax.swing.text.BadLocationException
meth public void insertString(int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth public void invalidateSyntaxMarks()
meth public void print(org.netbeans.editor.PrintContainer)
meth public void print(org.netbeans.editor.PrintContainer,boolean,boolean,int,int)
meth public void print(org.netbeans.editor.PrintContainer,boolean,java.lang.Boolean,int,int)
meth public void read(java.io.Reader,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public void remove(int,int) throws javax.swing.text.BadLocationException
meth public void removeAtomicLockListener(org.netbeans.api.editor.document.AtomicLockListener)
meth public void removeAtomicLockListener(org.netbeans.editor.AtomicLockListener)
 anno 0 java.lang.Deprecated()
meth public void removeDocumentListener(javax.swing.event.DocumentListener)
meth public void removePostModificationDocumentListener(javax.swing.event.DocumentListener)
meth public void removeUpdateDocumentListener(javax.swing.event.DocumentListener)
meth public void render(java.lang.Runnable)
meth public void repaintBlock(int,int)
 anno 0 java.lang.Deprecated()
meth public void replace(int,int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth public void resetUndoMerge()
meth public void runAtomic(java.lang.Runnable)
meth public void runAtomicAsUser(java.lang.Runnable)
meth public void setPostModificationDocumentListener(javax.swing.event.DocumentListener)
 anno 0 java.lang.Deprecated()
meth public void write(java.io.Writer,int,int) throws java.io.IOException,javax.swing.text.BadLocationException
supr javax.swing.text.AbstractDocument
hfds DEACTIVATE_LEXER_THRESHOLD,EDITABLE_PROP,LAST_MODIFICATION_TIMESTAMP_PROP,LOG,LOG_LISTENER,MODIFICATION_LISTENER_PROP,SUPPORTS_MODIFICATION_LISTENER_PROP,VERSION_PROP,annotations,annotationsLock,atomicDepth,atomicEdits,atomicLockEventInstance,atomicLockListenerList,composedText,debugNoText,debugRead,debugStack,deprecatedKitClass,filterBypass,fixLineSyntaxState,identifierAcceptor,lastModifyUndoEdit,lastPositionEditedByTyping,lineRootElement,mimeType,modifiable,postModificationDepth,postModificationDocumentListener,postModificationDocumentListenerList,prefs,prefsListener,removeUpdateLineUndo,runExclusiveDepth,shiftWidth,syntaxSupport,tabSize,text,undoEditWrappers,undoMergeReset,updateDocumentListenerList,weakPrefsListener,whitespaceAcceptor
hcls Accessor,AtomicCompoundEdit,BaseDocumentServices,FilterBypassImpl,MimeTypePropertyEvaluator,OldListenerAdapter,PlainEditorKit,ServicesImpl

CLSS public org.netbeans.editor.BaseKit
cons public init()
fld public final static int MAGIC_POSITION_MAX = 2147483646
fld public final static java.lang.String DOC_REPLACE_SELECTION_PROPERTY = "doc-replace-selection-property"
fld public final static java.lang.String abbrevExpandAction = "abbrev-expand"
fld public final static java.lang.String abbrevResetAction = "abbrev-reset"
fld public final static java.lang.String adjustCaretBottomAction = "adjust-caret-bottom"
fld public final static java.lang.String adjustCaretCenterAction = "adjust-caret-center"
fld public final static java.lang.String adjustCaretTopAction = "adjust-caret-top"
fld public final static java.lang.String adjustWindowBottomAction = "adjust-window-bottom"
fld public final static java.lang.String adjustWindowCenterAction = "adjust-window-center"
fld public final static java.lang.String adjustWindowTopAction = "adjust-window-top"
fld public final static java.lang.String annotationsCyclingAction = "annotations-cycling"
fld public final static java.lang.String collapseAllFoldsAction = "collapse-all-folds"
fld public final static java.lang.String collapseFoldAction = "collapse-fold"
fld public final static java.lang.String copySelectionElseLineDownAction = "copy-selection-else-line-down"
fld public final static java.lang.String copySelectionElseLineUpAction = "copy-selection-else-line-up"
fld public final static java.lang.String cutToLineBeginAction = "cut-to-line-begin"
fld public final static java.lang.String cutToLineEndAction = "cut-to-line-end"
fld public final static java.lang.String expandAllFoldsAction = "expand-all-folds"
fld public final static java.lang.String expandFoldAction = "expand-fold"
fld public final static java.lang.String findNextAction = "find-next"
fld public final static java.lang.String findPreviousAction = "find-previous"
fld public final static java.lang.String findSelectionAction = "find-selection"
fld public final static java.lang.String firstNonWhiteAction = "first-non-white"
fld public final static java.lang.String formatAction = "format"
fld public final static java.lang.String generateGutterPopupAction = "generate-gutter-popup"
fld public final static java.lang.String indentAction = "indent"
fld public final static java.lang.String insertDateTimeAction = "insert-date-time"
fld public final static java.lang.String jumpListNextAction = "jump-list-next"
fld public final static java.lang.String jumpListNextComponentAction = "jump-list-next-component"
fld public final static java.lang.String jumpListPrevAction = "jump-list-prev"
fld public final static java.lang.String jumpListPrevComponentAction = "jump-list-prev-component"
fld public final static java.lang.String lastNonWhiteAction = "last-non-white"
fld public final static java.lang.String lineFirstColumnAction = "caret-line-first-column"
fld public final static java.lang.String macroActionPrefix = "macro-"
fld public final static java.lang.String moveSelectionElseLineDownAction = "move-selection-else-line-down"
fld public final static java.lang.String moveSelectionElseLineUpAction = "move-selection-else-line-up"
fld public final static java.lang.String pasteFormatedAction = "paste-formated"
fld public final static java.lang.String redoAction = "redo"
fld public final static java.lang.String reformatLineAction = "reformat-line"
fld public final static java.lang.String reindentLineAction = "reindent-line"
fld public final static java.lang.String removeLineAction = "remove-line"
fld public final static java.lang.String removeLineBeginAction = "remove-line-begin"
fld public final static java.lang.String removeNextWordAction = "remove-word-next"
fld public final static java.lang.String removePreviousWordAction = "remove-word-previous"
fld public final static java.lang.String removeSelectionAction = "remove-selection"
fld public final static java.lang.String removeTabAction = "remove-tab"
fld public final static java.lang.String removeTrailingSpacesAction = "remove-trailing-spaces"
fld public final static java.lang.String scrollDownAction = "scroll-down"
fld public final static java.lang.String scrollUpAction = "scroll-up"
fld public final static java.lang.String selectIdentifierAction = "select-identifier"
fld public final static java.lang.String selectNextParameterAction = "select-next-parameter"
fld public final static java.lang.String selectionFirstNonWhiteAction = "selection-first-non-white"
fld public final static java.lang.String selectionLastNonWhiteAction = "selection-last-non-white"
fld public final static java.lang.String selectionLineFirstColumnAction = "selection-line-first-column"
fld public final static java.lang.String selectionPageDownAction = "selection-page-down"
fld public final static java.lang.String selectionPageUpAction = "selection-page-up"
fld public final static java.lang.String shiftLineLeftAction = "shift-line-left"
fld public final static java.lang.String shiftLineRightAction = "shift-line-right"
fld public final static java.lang.String splitLineAction = "split-line"
fld public final static java.lang.String startMacroRecordingAction = "start-macro-recording"
fld public final static java.lang.String startNewLineAction = "start-new-line"
fld public final static java.lang.String stopMacroRecordingAction = "stop-macro-recording"
fld public final static java.lang.String switchCaseAction = "switch-case"
fld public final static java.lang.String toLowerCaseAction = "to-lower-case"
fld public final static java.lang.String toUpperCaseAction = "to-upper-case"
fld public final static java.lang.String toggleHighlightSearchAction = "toggle-highlight-search"
fld public final static java.lang.String toggleLineNumbersAction = "toggle-line-numbers"
fld public final static java.lang.String toggleTypingModeAction = "toggle-typing-mode"
fld public final static java.lang.String undoAction = "undo"
fld public final static java.lang.String wordMatchNextAction = "word-match-next"
fld public final static java.lang.String wordMatchPrevAction = "word-match-prev"
innr public static BackwardAction
innr public static BeepAction
innr public static BeginAction
innr public static BeginLineAction
innr public static BeginWordAction
innr public static CompoundAction
innr public static CopyAction
innr public static CutAction
innr public static DefaultKeyTypedAction
innr public static DeleteCharAction
innr public static DownAction
innr public static EndAction
innr public static EndLineAction
innr public static EndWordAction
innr public static ForwardAction
innr public static InsertBreakAction
innr public static InsertContentAction
innr public static InsertStringAction
innr public static InsertTabAction
innr public static KitCompoundAction
innr public static NextWordAction
innr public static PageDownAction
innr public static PageUpAction
innr public static PasteAction
innr public static PreviousWordAction
innr public static ReadOnlyAction
innr public static RemoveTrailingSpacesAction
innr public static SelectAllAction
innr public static SelectLineAction
innr public static SelectWordAction
innr public static SplitLineAction
innr public static UpAction
innr public static WritableAction
meth protected javax.swing.Action[] createActions()
meth protected javax.swing.Action[] getCustomActions()
meth protected javax.swing.Action[] getDeclaredActions()
meth protected javax.swing.Action[] getMacroActions()
 anno 0 java.lang.Deprecated()
meth protected org.netbeans.editor.BaseTextUI createTextUI()
meth protected org.netbeans.editor.EditorUI createEditorUI()
meth protected org.netbeans.editor.EditorUI createPrintEditorUI(org.netbeans.editor.BaseDocument)
 anno 0 java.lang.Deprecated()
meth protected org.netbeans.editor.EditorUI createPrintEditorUI(org.netbeans.editor.BaseDocument,boolean,boolean)
meth protected void executeDeinstallActions(javax.swing.JEditorPane)
meth protected void executeInstallActions(javax.swing.JEditorPane)
meth protected void initDocument(org.netbeans.editor.BaseDocument)
meth protected void updateActions()
meth public final javax.swing.Action[] getActions()
meth public java.lang.Object clone()
meth public java.util.List<javax.swing.Action> translateActionNameList(java.util.List<java.lang.String>)
meth public javax.swing.Action getActionByName(java.lang.String)
meth public javax.swing.text.Caret createCaret()
meth public javax.swing.text.Document createDefaultDocument()
meth public javax.swing.text.ViewFactory getViewFactory()
meth public org.netbeans.editor.MultiKeymap getKeymap()
meth public org.netbeans.editor.Syntax createFormatSyntax(javax.swing.text.Document)
 anno 0 java.lang.Deprecated()
meth public org.netbeans.editor.Syntax createSyntax(javax.swing.text.Document)
 anno 0 java.lang.Deprecated()
meth public org.netbeans.editor.SyntaxSupport createSyntaxSupport(org.netbeans.editor.BaseDocument)
 anno 0 java.lang.Deprecated()
meth public static javax.swing.Action[] mapToActions(java.util.Map)
meth public static org.netbeans.editor.BaseKit getKit(java.lang.Class)
 anno 0 java.lang.Deprecated()
meth public static void addActionsToMap(java.util.Map<java.lang.String,javax.swing.Action>,javax.swing.Action[],java.lang.String)
meth public void deinstall(javax.swing.JEditorPane)
meth public void install(javax.swing.JEditorPane)
meth public void read(java.io.Reader,javax.swing.text.Document,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public void write(java.io.Writer,javax.swing.text.Document,int,int) throws java.io.IOException,javax.swing.text.BadLocationException
supr javax.swing.text.DefaultEditorKit
hfds IN_PASTE,KEYMAPS_AND_ACTIONS_LOCK,KIT_CNT_PREALLOC,LOG,PROP_NAVIGATE_BOUNDARIES,copyActionDef,cutActionDef,deleteNextCharActionDef,deletePrevCharActionDef,insertBreakActionDef,insertTabActionDef,keyBindingsUpdaterInited,keymapTrackers,kitActionMaps,kitActions,kitKeymaps,kits,pasteActionDef,redoActionDef,removeSelectionActionDef,removeTabActionDef,searchableKit,serialVersionUID,undoActionDef
hcls ClearUIForNullKitListener,DefaultSyntax,DefaultSyntaxTokenContext,KeybindingsAndPreferencesTracker,NullTextUI,SearchableKit

CLSS public org.netbeans.editor.GuardedDocument
cons public init(java.lang.Class)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.Class,boolean,javax.swing.text.StyleContext)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String)
cons public init(java.lang.String,boolean,javax.swing.text.StyleContext)
fld protected java.lang.String normalStyleName
fld protected javax.swing.text.StyleContext styles
fld public final static java.lang.String FMT_GUARDED_INSERT_LOCALE = "FMT_guarded_insert"
fld public final static java.lang.String FMT_GUARDED_REMOVE_LOCALE = "FMT_guarded_remove"
fld public final static java.lang.String GUARDED_ATTRIBUTE = "guarded"
fld public final static javax.swing.text.SimpleAttributeSet guardedSet
fld public final static javax.swing.text.SimpleAttributeSet unguardedSet
intf javax.swing.text.StyledDocument
meth protected org.netbeans.editor.BaseDocumentEvent createDocumentEvent(int,int,javax.swing.event.DocumentEvent$EventType)
meth protected void preInsertCheck(int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth protected void preRemoveCheck(int,int) throws javax.swing.text.BadLocationException
meth public boolean isPosGuarded(int)
meth public java.awt.Color getBackground(javax.swing.text.AttributeSet)
meth public java.awt.Color getForeground(javax.swing.text.AttributeSet)
meth public java.awt.Font getFont(javax.swing.text.AttributeSet)
meth public java.lang.String toStringDetail()
meth public java.util.Enumeration getStyleNames()
meth public javax.swing.text.Element getCharacterElement(int)
meth public javax.swing.text.Style addStyle(java.lang.String,javax.swing.text.Style)
meth public javax.swing.text.Style getLogicalStyle(int)
meth public javax.swing.text.Style getStyle(java.lang.String)
meth public org.netbeans.editor.MarkBlockChain getGuardedBlockChain()
meth public void removeStyle(java.lang.String)
meth public void runAtomic(java.lang.Runnable)
meth public void runAtomicAsUser(java.lang.Runnable)
meth public void setCharacterAttributes(int,int,javax.swing.text.AttributeSet,boolean)
meth public void setLogicalStyle(int,javax.swing.text.Style)
meth public void setNormalStyleName(java.lang.String)
meth public void setParagraphAttributes(int,int,javax.swing.text.AttributeSet,boolean)
supr org.netbeans.editor.BaseDocument
hfds LOG,atomicAsUser,breakGuarded,debugAtomic,debugAtomicStack,guardedBlockChain

CLSS public org.netbeans.editor.ext.ExtKit
cons public init()
fld public final static java.lang.String TRIMMED_TEXT = "trimmed-text"
fld public final static java.lang.String allCompletionShowAction = "all-completion-show"
fld public final static java.lang.String buildPopupMenuAction = "build-popup-menu"
fld public final static java.lang.String buildToolTipAction = "build-tool-tip"
fld public final static java.lang.String codeSelectAction = "code-select"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String commentAction = "comment"
fld public final static java.lang.String completionShowAction = "completion-show"
fld public final static java.lang.String completionTooltipShowAction = "tooltip-show"
fld public final static java.lang.String documentationShowAction = "documentation-show"
fld public final static java.lang.String escapeAction = "escape"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String findAction = "find"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String gotoAction = "goto"
fld public final static java.lang.String gotoDeclarationAction = "goto-declaration"
fld public final static java.lang.String gotoHelpAction = "goto-help"
fld public final static java.lang.String gotoSourceAction = "goto-source"
fld public final static java.lang.String gotoSuperImplementationAction = "goto-super-implementation"
fld public final static java.lang.String matchBraceAction = "match-brace"
fld public final static java.lang.String replaceAction = "replace"
fld public final static java.lang.String selectionMatchBraceAction = "selection-match-brace"
fld public final static java.lang.String showPopupMenuAction = "show-popup-menu"
fld public final static java.lang.String toggleCaseIdentifierBeginAction = "toggle-case-identifier-begin"
fld public final static java.lang.String toggleCommentAction = "toggle-comment"
fld public final static java.lang.String toggleToolbarAction = "toggle-toolbar"
fld public final static java.lang.String uncommentAction = "uncomment"
innr public static AllCompletionShowAction
innr public static BuildPopupMenuAction
innr public static BuildToolTipAction
innr public static CodeSelectAction
innr public static CommentAction
innr public static CompletionShowAction
innr public static CompletionTooltipShowAction
innr public static DocumentationShowAction
innr public static EscapeAction
innr public static ExtDefaultKeyTypedAction
innr public static ExtDeleteCharAction
innr public static GotoAction
innr public static GotoDeclarationAction
innr public static MatchBraceAction
innr public static PrefixMakerAction
innr public static ShowPopupMenuAction
innr public static ToggleCaseIdentifierBeginAction
innr public static ToggleCommentAction
innr public static UncommentAction
meth protected javax.swing.Action[] createActions()
meth protected org.netbeans.editor.EditorUI createEditorUI()
meth public org.netbeans.editor.SyntaxSupport createSyntaxSupport(org.netbeans.editor.BaseDocument)
supr org.netbeans.editor.BaseKit
hfds debugPopupMenu,editorBundleHash,noExtEditorUIClass
hcls BaseKitLocalizedAction

CLSS public org.netbeans.modules.editor.NbEditorDocument
cons public init(java.lang.Class)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String)
fld public final static java.lang.String INDENT_ENGINE = "indentEngine"
intf org.openide.text.NbDocument$Annotatable
intf org.openide.text.NbDocument$CustomEditor
intf org.openide.text.NbDocument$CustomToolbar
intf org.openide.text.NbDocument$PositionBiasable
intf org.openide.text.NbDocument$Printable
intf org.openide.text.NbDocument$WriteLockable
meth protected java.util.Dictionary createDocumentProperties(java.util.Dictionary)
meth public int getShiftWidth()
meth public int getTabSize()
meth public java.awt.Component createEditor(javax.swing.JEditorPane)
meth public java.text.AttributedCharacterIterator[] createPrintIterators()
meth public javax.swing.JToolBar createToolbar(javax.swing.JEditorPane)
meth public void addAnnotation(javax.swing.text.Position,int,org.openide.text.Annotation)
meth public void removeAnnotation(org.openide.text.Annotation)
meth public void setCharacterAttributes(int,int,javax.swing.text.AttributeSet,boolean)
supr org.netbeans.editor.GuardedDocument
hfds RP,annoMap
hcls AnnotationDescDelegate,NbPrintContainer

CLSS public org.netbeans.modules.editor.NbEditorKit
cons public init()
fld public final static java.lang.String SYSTEM_ACTION_CLASS_NAME_PROPERTY = "systemActionClassName"
fld public final static java.lang.String generateFoldPopupAction = "generate-fold-popup"
fld public final static java.lang.String generateGoToPopupAction = "generate-goto-popup"
innr public NbStopMacroRecordingAction
innr public final static NbToggleLineNumbersAction
innr public static GenerateFoldPopupAction
innr public static NbBuildPopupMenuAction
innr public static NbBuildToolTipAction
innr public static NbGenerateGoToPopupAction
innr public static NbRedoAction
innr public static NbUndoAction
innr public static ToggleToolbarAction
intf java.util.concurrent.Callable
meth protected javax.swing.Action[] createActions()
meth protected javax.swing.Action[] getDeclaredActions()
meth protected org.netbeans.editor.EditorUI createEditorUI()
meth protected void addSystemActionMapping(java.lang.String,java.lang.Class)
meth protected void toolTipAnnotationsLock(javax.swing.text.Document)
meth protected void toolTipAnnotationsUnlock(javax.swing.text.Document)
meth protected void updateActions()
meth public java.lang.Object call()
meth public java.lang.String getContentType()
meth public javax.swing.text.Document createDefaultDocument()
supr org.netbeans.editor.ext.ExtKit
hfds ACTIONS_TOPCOMPONENT,ACTION_CREATEITEM,ACTION_EXTKIT_BYNAME,ACTION_FOLDER,ACTION_SEPARATOR,ACTION_SYSTEM,LOG,SEPARATOR,contentTypeTable,nbRedoActionDef,nbUndoActionDef,serialVersionUID,systemAction2editorAction
hcls LayerSubFolderMenu,PopupInitializer,SubFolderData

CLSS public org.netbeans.modules.xml.text.api.XMLFormatUtil
cons public init()
meth public static void reformat(org.netbeans.editor.BaseDocument,int,int)
supr java.lang.Object

CLSS public final org.netbeans.modules.xml.text.api.XMLTextUtils
fld public final static java.lang.String XML_MIME = "text/xml"
meth public static java.lang.String actualAttributeValue(java.lang.String)
meth public static java.lang.String replaceCharsWithEntityStrings(java.lang.String)
meth public static java.lang.String replaceEntityStringsWithChars(java.lang.String)
meth public static org.netbeans.api.lexer.Token<org.netbeans.api.xml.lexer.XMLTokenId> skipAttributeValue(org.netbeans.api.lexer.TokenSequence,char)
meth public static void reformat(org.netbeans.api.editor.document.LineDocument,int,int)
supr java.lang.Object
hfds knownEntityChars,knownEntityStrings

CLSS public abstract interface org.netbeans.modules.xml.text.api.dom.SyntaxElement
fld public final static int NODE_ERROR = -1
meth public abstract <%0 extends org.w3c.dom.Node> {%%0} getNode()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract int getElementLength()
meth public abstract int getElementOffset()
meth public abstract int getType()
meth public abstract org.netbeans.modules.xml.text.api.dom.SyntaxElement getNext()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.netbeans.modules.xml.text.api.dom.SyntaxElement getParentElement()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.netbeans.modules.xml.text.api.dom.SyntaxElement getPrevious()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface org.netbeans.modules.xml.text.api.dom.TagElement
intf org.netbeans.modules.xml.text.api.dom.SyntaxElement
meth public abstract boolean isEnd()
meth public abstract boolean isSelfClosing()
meth public abstract boolean isStart()
meth public abstract org.netbeans.modules.xml.text.api.dom.TagElement getEndTag()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.netbeans.modules.xml.text.api.dom.TagElement getStartTag()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public final org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport
innr public abstract interface static SequenceCallable
meth public !varargs org.netbeans.api.lexer.Token<org.netbeans.api.xml.lexer.XMLTokenId> skip(int,boolean,org.netbeans.api.xml.lexer.XMLTokenId[]) throws javax.swing.text.BadLocationException
meth public <%0 extends java.lang.Object> {%%0} runLocked(java.util.concurrent.Callable<{%%0}>) throws javax.swing.text.BadLocationException
 anno 0 org.netbeans.api.annotations.common.NullUnknown()
meth public <%0 extends java.lang.Object> {%%0} runWithSequence(int,org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport$SequenceCallable<{%%0}>) throws javax.swing.text.BadLocationException
 anno 0 org.netbeans.api.annotations.common.NullUnknown()
meth public <%0 extends java.lang.Object> {%%0} runWithSequence(org.netbeans.api.lexer.Token<org.netbeans.api.xml.lexer.XMLTokenId>,org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport$SequenceCallable<{%%0}>) throws javax.swing.text.BadLocationException
meth public boolean isEmptyTag(org.netbeans.modules.xml.text.api.dom.SyntaxElement)
meth public boolean isEndTag(org.netbeans.modules.xml.text.api.dom.SyntaxElement)
meth public boolean isNormalTag(org.netbeans.modules.xml.text.api.dom.SyntaxElement)
meth public boolean isStartTag(org.netbeans.modules.xml.text.api.dom.SyntaxElement)
meth public final char lastTypedChar()
meth public int getNodeOffset(org.w3c.dom.Node)
meth public java.util.List<org.netbeans.modules.xml.text.api.dom.SyntaxElement> getPathFromRoot(org.netbeans.modules.xml.text.api.dom.SyntaxElement)
meth public org.netbeans.api.editor.document.LineDocument getDocument()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.lexer.Token getNextToken(int,int[]) throws javax.swing.text.BadLocationException
meth public org.netbeans.api.lexer.Token<org.netbeans.api.xml.lexer.XMLTokenId> getAttributeToken(int)
meth public org.netbeans.api.lexer.Token<org.netbeans.api.xml.lexer.XMLTokenId> getNextToken(int) throws javax.swing.text.BadLocationException
meth public org.netbeans.api.lexer.Token<org.netbeans.api.xml.lexer.XMLTokenId> getPreviousToken(int) throws javax.swing.text.BadLocationException
meth public org.netbeans.api.lexer.Token<org.netbeans.api.xml.lexer.XMLTokenId> getPreviousToken(int,int[]) throws javax.swing.text.BadLocationException
meth public org.netbeans.api.lexer.Token<org.netbeans.api.xml.lexer.XMLTokenId> getTokenAtPosition(int,int[]) throws javax.swing.text.BadLocationException
meth public org.netbeans.api.lexer.Token<org.netbeans.api.xml.lexer.XMLTokenId> skip(int,boolean,java.util.function.BiPredicate<org.netbeans.api.lexer.TokenSequence,org.netbeans.api.lexer.Token<org.netbeans.api.xml.lexer.XMLTokenId>>) throws javax.swing.text.BadLocationException
meth public org.netbeans.modules.xml.text.api.dom.SyntaxElement getElementChain(int) throws javax.swing.text.BadLocationException
meth public org.netbeans.modules.xml.text.api.dom.SyntaxElement getSyntaxElement(org.w3c.dom.Node)
meth public static java.lang.String getAttributeValue(org.w3c.dom.Node,java.lang.String)
meth public static org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport createSyntaxSupport(javax.swing.text.Document)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport getSyntaxSupport(javax.swing.text.Document)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
supr java.lang.Object
hfds cachedSequence,document,documentMonitor,supportMap
hcls DocumentMonitor

CLSS public abstract interface static org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport$SequenceCallable<%0 extends java.lang.Object>
 outer org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport
meth public abstract {org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport$SequenceCallable%0} call(org.netbeans.api.lexer.TokenSequence) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract org.netbeans.modules.xml.text.navigator.base.AbstractXMLNavigatorContent
cons public init()
fld protected org.openide.explorer.ExplorerManager explorerManager
fld protected org.openide.explorer.view.TreeView treeView
fld public static java.lang.String ERROR_CANNOT_NAVIGATE
fld public static java.lang.String ERROR_NO_DATA_AVAILABLE
fld public static java.lang.String ERROR_TOO_LARGE_DOCUMENT
intf java.beans.PropertyChangeListener
intf org.openide.explorer.ExplorerManager$Provider
meth protected boolean isLoading()
meth public abstract void navigate(org.openide.loaders.DataObject)
meth public org.openide.explorer.ExplorerManager getExplorerManager()
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void release()
meth public void showError(java.lang.String)
meth public void showWaitNode()
meth public void showWaitPanel()
supr javax.swing.JPanel
hfds emptyPanel,msgLabel,waitIcon
hcls WaitNode

CLSS public abstract org.netbeans.modules.xml.text.navigator.base.AbstractXMLNavigatorPanel
cons public init()
fld protected final org.openide.util.LookupListener selectionListener
fld protected org.netbeans.modules.xml.text.navigator.base.AbstractXMLNavigatorContent navigator
fld protected org.openide.util.Lookup$Result selection
intf org.netbeans.spi.navigator.NavigatorPanel
meth protected abstract org.netbeans.modules.xml.text.navigator.base.AbstractXMLNavigatorContent getNavigatorUI()
meth public abstract java.lang.String getDisplayHint()
meth public abstract java.lang.String getDisplayName()
meth public javax.swing.JComponent getComponent()
meth public org.openide.util.Lookup getLookup()
meth public void navigate(java.util.Collection)
meth public void panelActivated(org.openide.util.Lookup)
meth public void panelDeactivated()
supr java.lang.Object

CLSS public org.netbeans.modules.xml.text.syntax.DTDKit
cons public init()
fld public final static java.lang.String MIME_TYPE = "application/xml-dtd"
meth public java.lang.String getContentType()
supr org.netbeans.modules.xml.text.syntax.UniKit
hfds serialVersionUID

CLSS public org.netbeans.modules.xml.text.syntax.ENTKit
cons public init()
fld public final static java.lang.String MIME_TYPE = "text/xml-external-parsed-entity"
meth public java.lang.String getContentType()
supr org.netbeans.modules.xml.text.syntax.UniKit
hfds serialVersionUID

CLSS public org.netbeans.modules.xml.text.syntax.UniKit
cons public init()
meth public org.netbeans.editor.Syntax createSyntax(javax.swing.text.Document)
meth public org.netbeans.editor.SyntaxSupport createSyntaxSupport(org.netbeans.editor.BaseDocument)
meth public void read(java.io.InputStream,javax.swing.text.Document,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public void read(java.io.Reader,javax.swing.text.Document,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public void write(java.io.OutputStream,javax.swing.text.Document,int,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public void write(java.io.Writer,javax.swing.text.Document,int,int) throws java.io.IOException,javax.swing.text.BadLocationException
supr org.netbeans.modules.editor.NbEditorKit
hfds serialVersionUID

CLSS public org.netbeans.modules.xml.text.syntax.XMLKit
cons public init()
fld public final static java.lang.String MIME_TYPE = "text/xml"
fld public final static java.lang.String xmlCommentAction = "xml-comment"
fld public final static java.lang.String xmlTestAction = "xml-dump"
fld public final static java.lang.String xmlUncommentAction = "xml-uncomment"
fld public static java.util.Map settings
innr public XMLEditorDocument
innr public abstract static XMLEditorAction
innr public static TestAction
innr public static XMLCommentAction
innr public static XMLUncommentAction
intf org.openide.util.HelpCtx$Provider
meth protected javax.swing.Action[] createActions()
meth public java.lang.String getContentType()
meth public java.util.Map getMap()
meth public javax.swing.text.Document createDefaultDocument()
meth public org.netbeans.editor.Syntax createSyntax(javax.swing.text.Document)
meth public org.netbeans.editor.SyntaxSupport createSyntaxSupport(org.netbeans.editor.BaseDocument)
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static void setMap(java.util.Map)
meth public void install(javax.swing.JEditorPane)
supr org.netbeans.modules.editor.NbEditorKit
hfds J2EE_LEXER_COLORING,serialVersionUID

CLSS public static org.netbeans.modules.xml.text.syntax.XMLKit$TestAction
 outer org.netbeans.modules.xml.text.syntax.XMLKit
cons public init()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.modules.xml.text.syntax.XMLKit$XMLEditorAction
hfds serialVersionUID

CLSS public static org.netbeans.modules.xml.text.syntax.XMLKit$XMLCommentAction
 outer org.netbeans.modules.xml.text.syntax.XMLKit
cons public init()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.modules.xml.text.syntax.XMLKit$XMLEditorAction
hfds commentEndString,commentStartString,serialVersionUID

CLSS public abstract static org.netbeans.modules.xml.text.syntax.XMLKit$XMLEditorAction
 outer org.netbeans.modules.xml.text.syntax.XMLKit
cons public init(java.lang.String)
meth protected void problem(java.lang.String)
supr org.netbeans.editor.BaseAction

CLSS public org.netbeans.modules.xml.text.syntax.XMLKit$XMLEditorDocument
 outer org.netbeans.modules.xml.text.syntax.XMLKit
cons public init(org.netbeans.modules.xml.text.syntax.XMLKit,java.lang.Class)
cons public init(org.netbeans.modules.xml.text.syntax.XMLKit,java.lang.String)
supr org.netbeans.modules.editor.NbEditorDocument

CLSS public static org.netbeans.modules.xml.text.syntax.XMLKit$XMLUncommentAction
 outer org.netbeans.modules.xml.text.syntax.XMLKit
cons public init()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.modules.xml.text.syntax.XMLKit$XMLEditorAction
hfds commentEnd,commentEndString,commentStart,commentStartString,serialVersionUID

CLSS public abstract interface org.netbeans.spi.navigator.NavigatorPanel
innr public abstract interface static !annotation Registration
innr public abstract interface static !annotation Registrations
innr public abstract interface static DynamicRegistration
meth public abstract java.lang.String getDisplayHint()
meth public abstract java.lang.String getDisplayName()
meth public abstract javax.swing.JComponent getComponent()
meth public abstract org.openide.util.Lookup getLookup()
meth public abstract void panelActivated(org.openide.util.Lookup)
meth public abstract void panelDeactivated()

CLSS public final org.openide.explorer.ExplorerManager
cons public init()
fld public final static java.lang.String PROP_EXPLORED_CONTEXT = "exploredContext"
fld public final static java.lang.String PROP_NODE_CHANGE = "nodeChange"
fld public final static java.lang.String PROP_ROOT_CONTEXT = "rootContext"
fld public final static java.lang.String PROP_SELECTED_NODES = "selectedNodes"
innr public abstract interface static Provider
intf java.io.Serializable
intf java.lang.Cloneable
meth public final org.openide.nodes.Node getExploredContext()
meth public final org.openide.nodes.Node getRootContext()
meth public final void setExploredContext(org.openide.nodes.Node)
meth public final void setExploredContext(org.openide.nodes.Node,org.openide.nodes.Node[])
meth public final void setExploredContextAndSelection(org.openide.nodes.Node,org.openide.nodes.Node[]) throws java.beans.PropertyVetoException
meth public final void setRootContext(org.openide.nodes.Node)
meth public final void setSelectedNodes(org.openide.nodes.Node[]) throws java.beans.PropertyVetoException
meth public org.openide.explorer.ExplorerManager clone()
meth public org.openide.nodes.Node[] getSelectedNodes()
meth public static org.openide.explorer.ExplorerManager find(java.awt.Component)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addVetoableChangeListener(java.beans.VetoableChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removeVetoableChangeListener(java.beans.VetoableChangeListener)
supr java.lang.Object
hfds LOCK,SCHEDULE_REMOVE_ASYNCH,SELECTION_SYNC_DELAY,actions,exploredContext,listener,propertySupport,rootContext,selectedNodes,selectionProcessor,selectionSyncTask,serialPersistentFields,serialVersionUID,vetoableSupport,weakListener
hcls Listener

CLSS public abstract interface static org.openide.explorer.ExplorerManager$Provider
 outer org.openide.explorer.ExplorerManager
meth public abstract org.openide.explorer.ExplorerManager getExplorerManager()

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

