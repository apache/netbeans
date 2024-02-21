#Signature file v4.1
#Version 2.35

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

CLSS public final org.netbeans.modules.gsf.testrunner.api.CommonUtils
cons public init()
fld public final static java.lang.String ANT_PROJECT_TYPE = "ant"
fld public final static java.lang.String JUNIT_TF = "junit"
fld public final static java.lang.String MAVEN_PROJECT_TYPE = "maven"
fld public final static java.lang.String TESTNG_TF = "testng"
meth public java.lang.String getTestingFramework()
meth public org.netbeans.spi.project.ActionProvider getActionProvider(org.openide.filesystems.FileObject)
meth public static org.netbeans.modules.gsf.testrunner.api.CommonUtils getInstance()
meth public void setTestingFramework(java.lang.String)
supr java.lang.Object
hfds instanceRef,testingFramework

CLSS public abstract org.netbeans.modules.gsf.testrunner.api.CoreManager
cons public init()
innr public abstract interface static !annotation Registration
meth public abstract void displayOutput(org.netbeans.modules.gsf.testrunner.api.TestSession,java.lang.String,boolean)
meth public abstract void displayReport(org.netbeans.modules.gsf.testrunner.api.TestSession,org.netbeans.modules.gsf.testrunner.api.Report)
meth public abstract void displayReport(org.netbeans.modules.gsf.testrunner.api.TestSession,org.netbeans.modules.gsf.testrunner.api.Report,boolean)
meth public abstract void displaySuiteRunning(org.netbeans.modules.gsf.testrunner.api.TestSession,java.lang.String)
meth public abstract void displaySuiteRunning(org.netbeans.modules.gsf.testrunner.api.TestSession,org.netbeans.modules.gsf.testrunner.api.TestSuite)
meth public abstract void sessionFinished(org.netbeans.modules.gsf.testrunner.api.TestSession)
meth public abstract void testStarted(org.netbeans.modules.gsf.testrunner.api.TestSession)
meth public void registerNodeFactory()
supr java.lang.Object

CLSS public abstract interface static !annotation org.netbeans.modules.gsf.testrunner.api.CoreManager$Registration
 outer org.netbeans.modules.gsf.testrunner.api.CoreManager
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String projectType()
meth public abstract java.lang.String testingFramework()

CLSS public final org.netbeans.modules.gsf.testrunner.api.NamedObject
cons public init(java.lang.Object,java.lang.String)
fld public java.lang.Object object
fld public java.lang.String name
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final org.netbeans.modules.gsf.testrunner.api.OutputLine
cons public init(java.lang.String,boolean)
meth public boolean isError()
meth public java.lang.String getLine()
supr java.lang.Object
hfds error,line

CLSS public final org.netbeans.modules.gsf.testrunner.api.Report
cons public init(java.lang.String,org.netbeans.api.project.Project)
meth public boolean containsFailed()
meth public boolean isAborted()
meth public boolean isCompleted()
meth public boolean isSkipped()
meth public int getAborted()
meth public int getDetectedPassedTests()
meth public int getErrors()
meth public int getFailures()
meth public int getPassed()
meth public int getPassedWithErrors()
meth public int getPending()
meth public int getSkipped()
meth public int getStatusMask()
meth public int getTotalTests()
meth public java.lang.String getSuiteClassName()
meth public java.util.Collection<org.netbeans.modules.gsf.testrunner.api.Testcase> getTests()
meth public long getElapsedTimeMillis()
meth public org.netbeans.api.extexecution.print.LineConvertors$FileLocator getFileLocator()
meth public org.netbeans.api.project.Project getProject()
meth public org.netbeans.modules.gsf.testrunner.api.Status getStatus()
meth public void reportTest(org.netbeans.modules.gsf.testrunner.api.Testcase)
meth public void setAborted(boolean)
meth public void setAborted(int)
meth public void setCompleted(boolean)
meth public void setDetectedPassedTests(int)
meth public void setElapsedTimeMillis(long)
meth public void setErrors(int)
meth public void setFailures(int)
meth public void setFileLocator(org.netbeans.api.extexecution.print.LineConvertors$FileLocator)
meth public void setPassed(int)
meth public void setPassedWithErrors(int)
meth public void setPending(int)
meth public void setProject(org.netbeans.api.project.Project)
meth public void setSkipped(boolean)
meth public void setSkipped(int)
meth public void setSuiteClassName(java.lang.String)
meth public void setTests(java.util.Collection<org.netbeans.modules.gsf.testrunner.api.Testcase>)
meth public void setTotalTests(int)
meth public void update(org.netbeans.modules.gsf.testrunner.api.Report)
supr java.lang.Object
hfds aborted,abortedNum,completed,detectedPassedTests,elapsedTimeMillis,errors,failures,fileLocator,passed,passedWithErrors,pending,project,projectURI,skipped,skippedNum,suiteClassName,tests,totalTests

CLSS public abstract interface org.netbeans.modules.gsf.testrunner.api.RerunHandler
meth public abstract boolean enabled(org.netbeans.modules.gsf.testrunner.api.RerunType)
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public abstract void rerun()
meth public abstract void rerun(java.util.Set<org.netbeans.modules.gsf.testrunner.api.Testcase>)

CLSS public final !enum org.netbeans.modules.gsf.testrunner.api.RerunType
fld public final static org.netbeans.modules.gsf.testrunner.api.RerunType ALL
fld public final static org.netbeans.modules.gsf.testrunner.api.RerunType CUSTOM
meth public java.lang.String getName()
meth public static org.netbeans.modules.gsf.testrunner.api.RerunType valueOf(java.lang.String)
meth public static org.netbeans.modules.gsf.testrunner.api.RerunType[] values()
supr java.lang.Enum<org.netbeans.modules.gsf.testrunner.api.RerunType>
hfds name

CLSS public org.netbeans.modules.gsf.testrunner.api.SelfResizingPanel
cons public init()
meth protected boolean isPainted()
meth protected void adjustWindowSize()
meth protected void paintChildren(java.awt.Graphics)
meth protected void paintedFirstTime(java.awt.Graphics)
supr javax.swing.JPanel
hfds painted

CLSS public org.netbeans.modules.gsf.testrunner.api.SizeRestrictedPanel
cons public init()
cons public init(boolean,boolean)
cons public init(java.awt.LayoutManager)
cons public init(java.awt.LayoutManager,boolean,boolean)
meth public java.awt.Dimension getMaximumSize()
supr javax.swing.JPanel
hfds heightRestriction,widthRestriction

CLSS public final !enum org.netbeans.modules.gsf.testrunner.api.Status
fld public final static org.netbeans.modules.gsf.testrunner.api.Status ABORTED
fld public final static org.netbeans.modules.gsf.testrunner.api.Status ERROR
fld public final static org.netbeans.modules.gsf.testrunner.api.Status FAILED
fld public final static org.netbeans.modules.gsf.testrunner.api.Status IGNORED
fld public final static org.netbeans.modules.gsf.testrunner.api.Status PASSED
fld public final static org.netbeans.modules.gsf.testrunner.api.Status PASSEDWITHERRORS
fld public final static org.netbeans.modules.gsf.testrunner.api.Status PENDING
fld public final static org.netbeans.modules.gsf.testrunner.api.Status SKIPPED
meth public boolean isMaskApplied(int)
meth public int getBitMask()
meth public java.lang.String getHtmlDisplayColor()
meth public static boolean isAborted(org.netbeans.modules.gsf.testrunner.api.Status)
meth public static boolean isFailureOrError(org.netbeans.modules.gsf.testrunner.api.Status)
meth public static boolean isSkipped(org.netbeans.modules.gsf.testrunner.api.Status)
meth public static org.netbeans.modules.gsf.testrunner.api.Status valueOf(java.lang.String)
meth public static org.netbeans.modules.gsf.testrunner.api.Status[] values()
supr java.lang.Enum<org.netbeans.modules.gsf.testrunner.api.Status>
hfds bitMask,displayColor

CLSS public abstract org.netbeans.modules.gsf.testrunner.api.TestCreatorProvider
cons public init()
fld public final static java.lang.String FRAMEWORK_JUNIT = "JUnit"
fld public final static java.lang.String FRAMEWORK_PHP = "PHP"
fld public final static java.lang.String FRAMEWORK_SELENIUM = "Selenium"
fld public final static java.lang.String FRAMEWORK_TESTNG = "TestNG"
fld public final static java.lang.String IDENTIFIER_JUNIT = "junit"
fld public final static java.lang.String IDENTIFIER_PHP = "php"
fld public final static java.lang.String IDENTIFIER_SELENIUM = "selenium"
fld public final static java.lang.String IDENTIFIER_TESTNG = "testng"
fld public final static java.lang.String INTEGRATION_TEST_CLASS_SUFFIX = "IT"
fld public final static java.lang.String TESTNG_TEST_CLASS_SUFFIX = "NG"
fld public final static java.lang.String TEST_CLASS_SUFFIX = "Test"
innr public abstract interface static !annotation Registration
innr public final static Context
meth public abstract boolean enable(org.openide.filesystems.FileObject[])
meth public abstract void createTests(org.netbeans.modules.gsf.testrunner.api.TestCreatorProvider$Context)
meth public static org.netbeans.api.project.SourceGroup getSourceGroup(org.openide.filesystems.FileObject,org.netbeans.api.project.Project)
supr java.lang.Object

CLSS public final static org.netbeans.modules.gsf.testrunner.api.TestCreatorProvider$Context
 outer org.netbeans.modules.gsf.testrunner.api.TestCreatorProvider
cons public init(org.openide.filesystems.FileObject[])
meth public boolean isIntegrationTests()
meth public boolean isSingleClass()
meth public java.lang.String getTestClassName()
meth public java.util.Map<java.lang.String,java.lang.Object> getConfigurationPanelProperties()
meth public org.openide.filesystems.FileObject getTargetFolder()
meth public org.openide.filesystems.FileObject[] getActivatedFOs()
meth public void setConfigurationPanelProperties(java.util.Map<java.lang.String,java.lang.Object>)
meth public void setIntegrationTests(boolean)
meth public void setSingleClass(boolean)
meth public void setTargetFolder(org.openide.filesystems.FileObject)
meth public void setTestClassName(java.lang.String)
supr java.lang.Object
hfds activatedFOs,configurationPanelProperties,integrationTests,singleClass,targetFolder,testClassName

CLSS public abstract interface static !annotation org.netbeans.modules.gsf.testrunner.api.TestCreatorProvider$Registration
 outer org.netbeans.modules.gsf.testrunner.api.TestCreatorProvider
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()
meth public abstract java.lang.String displayName()
meth public abstract java.lang.String identifier()

CLSS public final org.netbeans.modules.gsf.testrunner.api.TestMethodNodeAction
cons public init(org.netbeans.spi.project.ActionProvider,org.openide.util.Lookup,java.lang.String,java.lang.String)
intf javax.swing.Action
meth public boolean isEnabled()
meth public java.lang.Object getValue(java.lang.String)
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void putValue(java.lang.String,java.lang.Object)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setEnabled(boolean)
supr java.lang.Object
hfds actionProvider,command,context,name

CLSS public org.netbeans.modules.gsf.testrunner.api.TestSession
cons public init(java.lang.String,org.netbeans.api.project.Project,org.netbeans.modules.gsf.testrunner.api.TestSession$SessionType)
innr public final static !enum SessionType
innr public final static SessionResult
meth public java.lang.String getName()
meth public java.lang.String getStartingMsg()
meth public java.util.List<org.netbeans.modules.gsf.testrunner.api.Testcase> getAllTestCases()
meth public long incrementFailuresCount()
meth public org.netbeans.api.extexecution.print.LineConvertors$FileLocator getFileLocator()
meth public org.netbeans.api.project.Project getProject()
meth public org.netbeans.modules.gsf.testrunner.api.Report getReport(long)
meth public org.netbeans.modules.gsf.testrunner.api.RerunHandler getRerunHandler()
meth public org.netbeans.modules.gsf.testrunner.api.TestSession$SessionResult getSessionResult()
meth public org.netbeans.modules.gsf.testrunner.api.TestSession$SessionType getSessionType()
meth public org.netbeans.modules.gsf.testrunner.api.TestSuite getCurrentSuite()
meth public org.netbeans.modules.gsf.testrunner.api.Testcase getCurrentTestCase()
meth public void addOutput(java.lang.String)
meth public void addSuite(org.netbeans.modules.gsf.testrunner.api.TestSuite)
meth public void addTestCase(org.netbeans.modules.gsf.testrunner.api.Testcase)
meth public void finishSuite(org.netbeans.modules.gsf.testrunner.api.TestSuite)
meth public void setRerunHandler(org.netbeans.modules.gsf.testrunner.api.RerunHandler)
meth public void setStartingMsg(java.lang.String)
supr java.lang.Object
hfds failuresCount,fileLocator,name,output,project,projectURI,rerunHandler,sessionType,startingMsg,suiteIdxs,testSuites

CLSS public final static org.netbeans.modules.gsf.testrunner.api.TestSession$SessionResult
 outer org.netbeans.modules.gsf.testrunner.api.TestSession
cons public init()
meth public int getErrors()
meth public int getFailed()
meth public int getPassed()
meth public int getPassedWithErrors()
meth public int getPending()
meth public int getTotal()
meth public long getElapsedTime()
supr java.lang.Object
hfds aborted,elapsedTime,errors,failed,passed,passedWithErrors,pending,skipped

CLSS public final static !enum org.netbeans.modules.gsf.testrunner.api.TestSession$SessionType
 outer org.netbeans.modules.gsf.testrunner.api.TestSession
fld public final static org.netbeans.modules.gsf.testrunner.api.TestSession$SessionType DEBUG
fld public final static org.netbeans.modules.gsf.testrunner.api.TestSession$SessionType TEST
meth public static org.netbeans.modules.gsf.testrunner.api.TestSession$SessionType valueOf(java.lang.String)
meth public static org.netbeans.modules.gsf.testrunner.api.TestSession$SessionType[] values()
supr java.lang.Enum<org.netbeans.modules.gsf.testrunner.api.TestSession$SessionType>

CLSS public org.netbeans.modules.gsf.testrunner.api.TestSuite
cons public init(java.lang.String)
fld public final static java.lang.String ANONYMOUS_SUITE
fld public final static org.netbeans.modules.gsf.testrunner.api.TestSuite ANONYMOUS_TEST_SUITE
meth public java.lang.String getName()
meth public java.util.List<org.netbeans.modules.gsf.testrunner.api.Testcase> getTestcases()
meth public org.netbeans.modules.gsf.testrunner.api.Testcase getLastTestCase()
meth public void addTestcase(org.netbeans.modules.gsf.testrunner.api.Testcase)
supr java.lang.Object
hfds MAX_TOOLTIP_LINES,name,testcases

CLSS public org.netbeans.modules.gsf.testrunner.api.Testcase
cons public init(java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.gsf.testrunner.api.TestSession)
cons public init(java.lang.String,java.lang.String,org.netbeans.modules.gsf.testrunner.api.TestSession)
meth public java.lang.String getClassName()
meth public java.lang.String getDisplayName()
meth public java.lang.String getLocation()
meth public java.lang.String getName()
meth public java.lang.String getType()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.gsf.testrunner.api.OutputLine> getOutput()
meth public long getTimeMillis()
meth public org.netbeans.modules.gsf.testrunner.api.Status getStatus()
meth public org.netbeans.modules.gsf.testrunner.api.TestSession getSession()
meth public org.netbeans.modules.gsf.testrunner.api.Trouble getTrouble()
meth public void addOutputLines(java.util.List<java.lang.String>)
meth public void setClassName(java.lang.String)
meth public void setLocation(java.lang.String)
meth public void setStatus(org.netbeans.modules.gsf.testrunner.api.Status)
meth public void setTimeMillis(long)
meth public void setTrouble(org.netbeans.modules.gsf.testrunner.api.Trouble)
supr java.lang.Object
hfds className,displayName,location,name,output,session,status,timeMillis,trouble,type

CLSS public final org.netbeans.modules.gsf.testrunner.api.Trouble
cons public init(boolean)
innr public final static ComparisonFailure
meth public boolean isError()
meth public java.lang.String[] getStackTrace()
meth public org.netbeans.modules.gsf.testrunner.api.Trouble$ComparisonFailure getComparisonFailure()
meth public void setComparisonFailure(org.netbeans.modules.gsf.testrunner.api.Trouble$ComparisonFailure)
meth public void setError(boolean)
meth public void setStackTrace(java.lang.String[])
supr java.lang.Object
hfds comparisonFailure,error,stackTrace

CLSS public final static org.netbeans.modules.gsf.testrunner.api.Trouble$ComparisonFailure
 outer org.netbeans.modules.gsf.testrunner.api.Trouble
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String)
meth public java.lang.String getActual()
meth public java.lang.String getExpected()
meth public java.lang.String getMimeType()
supr java.lang.Object
hfds DEFAULT_MIME_TYPE,actual,expected,mimeType

CLSS public final org.netbeans.modules.gsf.testrunner.api.UnitTestsUsage
meth public static org.netbeans.modules.gsf.testrunner.api.UnitTestsUsage getInstance()
meth public void logUnitTestUsage(java.net.URI,java.lang.String)
supr java.lang.Object
hfds INSTANCE,projectsAlreadyLogged

CLSS public abstract org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin
cons protected init()
innr public final static !enum CreateTestParam
innr public final static Location
meth protected !varargs boolean canCreateTests(org.openide.filesystems.FileObject[])
meth protected abstract org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$Location getTestLocation(org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$Location)
meth protected abstract org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$Location getTestedLocation(org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$Location)
meth protected abstract org.openide.filesystems.FileObject[] createTests(org.openide.filesystems.FileObject[],org.openide.filesystems.FileObject,java.util.Map<org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$CreateTestParam,java.lang.Object>)
meth protected boolean createTestActionCalled(org.openide.filesystems.FileObject[])
supr java.lang.Object

CLSS public final static !enum org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$CreateTestParam
 outer org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin
fld public final static org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$CreateTestParam CLASS_NAME
fld public final static org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$CreateTestParam INC_ABSTRACT_CLASS
fld public final static org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$CreateTestParam INC_CLASS_SETUP
fld public final static org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$CreateTestParam INC_CLASS_TEAR_DOWN
fld public final static org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$CreateTestParam INC_CODE_HINT
fld public final static org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$CreateTestParam INC_EXCEPTION_CLASS
fld public final static org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$CreateTestParam INC_GENERATE_INTEGRATION_TEST
fld public final static org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$CreateTestParam INC_GENERATE_SUITE
fld public final static org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$CreateTestParam INC_JAVADOC
fld public final static org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$CreateTestParam INC_METHOD_BODIES
fld public final static org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$CreateTestParam INC_PKG_PRIVATE
fld public final static org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$CreateTestParam INC_PKG_PRIVATE_CLASS
fld public final static org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$CreateTestParam INC_PROTECTED
fld public final static org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$CreateTestParam INC_PUBLIC
fld public final static org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$CreateTestParam INC_SETUP
fld public final static org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$CreateTestParam INC_TEAR_DOWN
meth public int getIdNumber()
meth public static org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$CreateTestParam valueOf(java.lang.String)
meth public static org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$CreateTestParam[] values()
supr java.lang.Enum<org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$CreateTestParam>
hfds idNumber

CLSS public final static org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$Location
 outer org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin
cons public init(org.openide.filesystems.FileObject)
meth public org.openide.filesystems.FileObject getFileObject()
supr java.lang.Object
hfds fileObject

CLSS public abstract org.netbeans.modules.gsf.testrunner.plugin.CommonSettingsProvider
cons public init()
meth public abstract boolean isBodyComments()
meth public abstract boolean isBodyContent()
meth public abstract boolean isGenerateAbstractImpl()
meth public abstract boolean isGenerateClassSetUp()
meth public abstract boolean isGenerateClassTearDown()
meth public abstract boolean isGenerateExceptionClasses()
meth public abstract boolean isGenerateIntegrationTests()
meth public abstract boolean isGenerateSetUp()
meth public abstract boolean isGenerateSuiteClasses()
meth public abstract boolean isGenerateTearDown()
meth public abstract boolean isIncludePackagePrivateClasses()
meth public abstract boolean isJavaDoc()
meth public abstract boolean isMembersPackage()
meth public abstract boolean isMembersProtected()
meth public abstract boolean isMembersPublic()
meth public abstract void setBodyComments(boolean)
meth public abstract void setBodyContent(boolean)
meth public abstract void setGenerateAbstractImpl(boolean)
meth public abstract void setGenerateClassSetUp(boolean)
meth public abstract void setGenerateClassTearDown(boolean)
meth public abstract void setGenerateExceptionClasses(boolean)
meth public abstract void setGenerateIntegrationTests(boolean)
meth public abstract void setGenerateSetUp(boolean)
meth public abstract void setGenerateSuiteClasses(boolean)
meth public abstract void setGenerateTearDown(boolean)
meth public abstract void setIncludePackagePrivateClasses(boolean)
meth public abstract void setJavaDoc(boolean)
meth public abstract void setMembersPackage(boolean)
meth public abstract void setMembersProtected(boolean)
meth public abstract void setMembersPublic(boolean)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.gsf.testrunner.plugin.CommonTestUtilProvider
cons public init()
meth public abstract java.lang.Object[] getTestTargets(org.openide.filesystems.FileObject)
meth public abstract java.util.Map<org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$CreateTestParam,java.lang.Object> getSettingsMap(boolean)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.gsf.testrunner.plugin.GuiUtilsProvider
cons public init()
meth public abstract java.lang.String getCheckboxText(java.lang.String)
meth public abstract java.lang.String getJunitFramework()
meth public abstract java.lang.String getMessageFor(java.lang.String)
meth public abstract java.lang.String getTestngFramework()
meth public abstract java.util.ResourceBundle getBundle()
meth public abstract javax.swing.JCheckBox[] createCheckBoxes(java.lang.String[])
meth public abstract javax.swing.JComponent createChkBoxGroup(java.lang.String,javax.swing.JCheckBox[])
meth public abstract javax.swing.text.JTextComponent createMultilineLabel(java.lang.String,java.awt.Color)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.gsf.testrunner.plugin.RootsProvider
cons public init()
meth public abstract java.lang.String getProjectTestsHint()
meth public abstract java.lang.String getSourceRootType()
meth public abstract java.lang.String getTestsRootName()
meth public abstract java.net.URL[] findSourceRoots(org.openide.filesystems.FileObject)
meth public abstract java.net.URL[] findTestRoots(org.openide.filesystems.FileObject)
supr java.lang.Object

