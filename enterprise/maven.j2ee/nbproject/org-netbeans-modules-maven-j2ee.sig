#Signature file v4.1
#Version 1.84

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

CLSS public abstract interface org.netbeans.api.project.Sources
fld public final static java.lang.String TYPE_GENERIC = "generic"
meth public abstract org.netbeans.api.project.SourceGroup[] getSourceGroups(java.lang.String)
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public abstract interface org.netbeans.modules.j2ee.core.api.support.classpath.ContainerClassPathModifier
fld public final static java.lang.String API_ANNOTATION = "annotation"
fld public final static java.lang.String API_EJB = "ejb"
fld public final static java.lang.String API_J2EE = "j2ee-api"
fld public final static java.lang.String API_JSF = "jsf-api"
fld public final static java.lang.String API_JSP = "jsp-api"
fld public final static java.lang.String API_PERSISTENCE = "persistence"
fld public final static java.lang.String API_SERVLET = "servlet-api"
fld public final static java.lang.String API_TRANSACTION = "transaction"
meth public abstract void extendClasspath(org.openide.filesystems.FileObject,java.lang.String[])

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor
meth public abstract boolean ejbsChanged()
meth public abstract java.lang.String[] getChangedEjbs()

CLSS public final org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment
innr public abstract interface static Logger
innr public final static !enum Mode
innr public final static DeploymentException
meth public boolean canFileDeploy(java.lang.String,org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule)
meth public boolean isRunning(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public final void addInstanceListener(org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener)
meth public final void removeInstanceListener(org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener)
meth public java.lang.String deploy(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider,boolean,java.lang.String,java.lang.String,boolean) throws org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$DeploymentException
 anno 0 java.lang.Deprecated()
meth public java.lang.String deploy(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider,boolean,java.lang.String,java.lang.String,boolean,org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$Logger) throws org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$DeploymentException
 anno 0 java.lang.Deprecated()
meth public java.lang.String deploy(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider,org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$Mode,java.lang.String,java.lang.String,boolean) throws org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$DeploymentException
meth public java.lang.String deploy(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider,org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$Mode,java.lang.String,java.lang.String,boolean,org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$Logger) throws org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$DeploymentException
meth public java.lang.String deploy(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider,org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$Mode,java.lang.String,java.lang.String,boolean,org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$Logger,java.util.concurrent.Callable<java.lang.Void>) throws org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$DeploymentException
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 7 org.netbeans.api.annotations.common.NullAllowed()
meth public java.lang.String getDefaultServerInstanceID()
 anno 0 java.lang.Deprecated()
meth public java.lang.String getServerDisplayName(java.lang.String)
meth public java.lang.String getServerID(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public java.lang.String getServerInstanceDisplayName(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public java.lang.String[] getInstancesOfServer(java.lang.String)
meth public java.lang.String[] getServerIDs()
meth public java.lang.String[] getServerInstanceIDs()
meth public java.lang.String[] getServerInstanceIDs(java.lang.Object[])
 anno 0 java.lang.Deprecated()
meth public java.lang.String[] getServerInstanceIDs(java.lang.Object[],java.lang.String)
 anno 0 java.lang.Deprecated()
meth public java.lang.String[] getServerInstanceIDs(java.lang.Object[],java.lang.String,java.lang.String[])
 anno 0 java.lang.Deprecated()
meth public java.lang.String[] getServerInstanceIDs(java.util.Collection<org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type>)
meth public java.lang.String[] getServerInstanceIDs(java.util.Collection<org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type>,org.netbeans.api.j2ee.core.Profile)
meth public java.lang.String[] getServerInstanceIDs(java.util.Collection<org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type>,org.netbeans.api.j2ee.core.Profile,java.lang.String[])
meth public org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform getJ2eePlatform(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance getServerInstance(java.lang.String)
meth public static org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment getDefault()
meth public void disableCompileOnSaveSupport(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider)
meth public void enableCompileOnSaveSupport(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider)
meth public void resumeDeployOnSave(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider)
meth public void suspendDeployOnSave(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider)
meth public void undeploy(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider,boolean,org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$Logger) throws org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$DeploymentException
supr java.lang.Object
hfds FILTER_PATTERN,LOGGER,alsoStartTargets,instance

CLSS public abstract interface static org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$Logger
 outer org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment
meth public abstract void log(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter
meth public abstract boolean isManifestChanged(long)
meth public abstract org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor getEjbChanges(long)

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleBase
meth public abstract <%0 extends java.lang.Object> org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<{%%0}> getMetadataModel(java.lang.Class<{%%0}>)
meth public abstract java.io.File getDeploymentConfigurationFile(java.lang.String)
meth public abstract java.io.File getResourceDirectory()
meth public abstract java.lang.String getModuleVersion()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getUrl()
meth public abstract java.util.Iterator<org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$RootedEntry> getArchiveContents() throws java.io.IOException
meth public abstract org.openide.filesystems.FileObject getArchive() throws java.io.IOException
meth public abstract org.openide.filesystems.FileObject getContentDirectory() throws java.io.IOException
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation2
intf org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleBase
meth public abstract org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type getModuleType()

CLSS public abstract org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider
cons public init()
innr public abstract interface static ConfigSupport
innr public abstract interface static DeployOnSaveClassInterceptor
innr public abstract interface static DeployOnSaveSupport
meth protected final void fireServerChange(java.lang.String,java.lang.String)
meth public abstract java.lang.String getServerID()
meth public abstract java.lang.String getServerInstanceID()
meth public abstract org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule getJ2eeModule()
meth public abstract org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter getModuleChangeReporter()
meth public abstract void setServerInstanceID(java.lang.String)
meth public boolean hasVerifierSupport()
meth public boolean isDatasourceCreationSupported()
meth public boolean isOnlyCompileOnSaveEnabled()
meth public final org.netbeans.modules.j2ee.deployment.common.api.Datasource createDatasource(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException,org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException
meth public final org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider$ConfigSupport getConfigSupport()
meth public final org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo getServerDebugInfo()
meth public final org.openide.filesystems.FileObject[] getConfigurationFiles()
meth public final org.openide.filesystems.FileObject[] getConfigurationFiles(boolean)
meth public final void addConfigurationFilesListener(org.netbeans.modules.j2ee.deployment.devmodules.spi.ConfigurationFilesListener)
meth public final void addInstanceListener(org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener)
meth public final void removeConfigurationFilesListener(org.netbeans.modules.j2ee.deployment.devmodules.spi.ConfigurationFilesListener)
meth public final void removeInstanceListener(org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener)
meth public java.io.File[] getRequiredLibraries()
meth public java.lang.String getDeploymentName()
meth public java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.Datasource> getModuleDatasources() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
 anno 0 java.lang.Deprecated()
meth public java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.Datasource> getServerDatasources() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public org.netbeans.modules.j2ee.deployment.common.api.SourceFileMap getSourceFileMap()
meth public org.netbeans.modules.j2ee.deployment.devmodules.api.ResourceChangeReporter getResourceChangeReporter()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider$DeployOnSaveClassInterceptor getDeployOnSaveClassInterceptor()
meth public org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider$DeployOnSaveSupport getDeployOnSaveSupport()
meth public org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties getInstanceProperties()
meth public org.openide.filesystems.FileObject[] getSourceRoots()
meth public void deployDatasources() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException,org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException
 anno 0 java.lang.Deprecated()
meth public void verify(org.openide.filesystems.FileObject,java.io.OutputStream) throws org.netbeans.modules.j2ee.deployment.common.api.ValidationException
supr java.lang.Object
hfds LOGGER,configFilesListener,configSupportImpl,configSupportImplLock,listeners
hcls WarningInstanceProperties

CLSS public abstract interface static org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider$DeployOnSaveSupport
 outer org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider
meth public abstract boolean containsIdeArtifacts()
meth public abstract void addArtifactListener(org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener)
meth public abstract void removeArtifactListener(org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener)

CLSS public abstract interface org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSourcePopulator
meth public abstract void connect(javax.swing.JComboBox)

CLSS public abstract interface org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSourceProvider
meth public abstract java.util.List<org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSource> getDataSources()
meth public abstract org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSource toJPADataSource(java.lang.Object)

CLSS public abstract interface org.netbeans.modules.j2ee.persistence.spi.moduleinfo.JPAModuleInfo
fld public final static java.lang.String JPACHECKSUPPORTED = "jpaversionverification"
fld public final static java.lang.String JPAVERSIONPREFIX = "jpa"
innr public final static !enum ModuleType
meth public abstract java.lang.Boolean isJPAVersionSupported(java.lang.String)
meth public abstract java.lang.String getVersion()
meth public abstract org.netbeans.modules.j2ee.persistence.spi.moduleinfo.JPAModuleInfo$ModuleType getType()

CLSS public abstract interface org.netbeans.modules.j2ee.persistence.spi.server.ServerStatusProvider
meth public abstract boolean validServerInstancePresent()

CLSS public abstract interface org.netbeans.modules.j2ee.persistence.spi.server.ServerStatusProvider2
intf org.netbeans.modules.j2ee.persistence.spi.server.ServerStatusProvider
meth public abstract boolean selectServer()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public abstract org.netbeans.modules.java.hints.spi.AbstractHint
 anno 0 java.lang.Deprecated()
cons public !varargs init(boolean,boolean,org.netbeans.modules.java.hints.spi.AbstractHint$HintSeverity,java.lang.String[])
innr public final static !enum HintSeverity
intf org.netbeans.modules.java.hints.spi.TreeRule
meth public abstract java.lang.String getDescription()
meth public final boolean isEnabled()
meth public final org.netbeans.modules.java.hints.spi.AbstractHint$HintSeverity getSeverity()
meth public java.util.prefs.Preferences getPreferences(java.lang.String)
meth public javax.swing.JComponent getCustomizer(java.util.prefs.Preferences)
supr java.lang.Object
hfds enableDefault,severityDefault,showInTaskListDefault,suppressBy
hcls HintAccessorImpl

CLSS public abstract interface org.netbeans.modules.java.hints.spi.Rule
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getId()
meth public abstract void cancel()

CLSS public abstract interface org.netbeans.modules.java.hints.spi.TreeRule
intf org.netbeans.modules.java.hints.spi.Rule
meth public abstract java.util.List<org.netbeans.spi.editor.hints.ErrorDescription> run(org.netbeans.api.java.source.CompilationInfo,com.sun.source.util.TreePath)
meth public abstract java.util.Set<com.sun.source.tree.Tree$Kind> getTreeKinds()

CLSS public abstract interface org.netbeans.modules.javaee.project.spi.JavaEEProjectSettingsImplementation
meth public abstract java.lang.String getBrowserID()
meth public abstract java.lang.String getServerInstanceID()
meth public abstract org.netbeans.api.j2ee.core.Profile getProfile()
meth public abstract void setBrowserID(java.lang.String)
meth public abstract void setProfile(org.netbeans.api.j2ee.core.Profile)
meth public abstract void setServerInstanceID(java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.api.execute.ExecutionResultChecker
meth public abstract void executionResult(org.netbeans.modules.maven.api.execute.RunConfig,org.netbeans.modules.maven.api.execute.ExecutionContext,int)

CLSS public abstract interface org.netbeans.modules.maven.api.execute.LateBoundPrerequisitesChecker
meth public abstract boolean checkRunConfig(org.netbeans.modules.maven.api.execute.RunConfig,org.netbeans.modules.maven.api.execute.ExecutionContext)

CLSS public abstract interface org.netbeans.modules.maven.api.execute.PrerequisitesChecker
meth public abstract boolean checkRunConfig(org.netbeans.modules.maven.api.execute.RunConfig)

CLSS public abstract org.netbeans.modules.maven.j2ee.BaseEEModuleImpl
cons public init(org.netbeans.api.project.Project,org.netbeans.modules.maven.j2ee.BaseEEModuleProvider,java.lang.String,java.lang.String)
fld protected final java.lang.String ddName
fld protected final java.lang.String ddPath
fld protected final org.netbeans.api.project.Project project
fld protected final org.netbeans.modules.maven.j2ee.BaseEEModuleProvider provider
intf org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter
intf org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation2
meth protected final org.netbeans.modules.j2ee.dd.api.common.RootInterface getDeploymentDescriptor(java.lang.String)
meth protected final org.netbeans.modules.maven.api.NbMavenProject mavenproject()
meth protected final org.openide.filesystems.FileObject getArchive(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
meth public boolean isManifestChanged(long)
meth public boolean isValid()
meth public java.io.File getDDFile(java.lang.String)
meth public java.io.File getDeploymentConfigurationFile(java.lang.String)
meth public java.io.File getResourceDirectory()
meth public java.lang.String getUrl()
meth public java.util.Iterator<org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$RootedEntry> getArchiveContents() throws java.io.IOException
meth public org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor getEjbChanges(long)
meth public org.openide.filesystems.FileObject getContentDirectory() throws java.io.IOException
meth public org.openide.filesystems.FileObject getDeploymentDescriptor()
meth public org.openide.filesystems.FileObject getMetaInf()
meth public org.openide.filesystems.FileObject[] getJavaSources()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hcls ContentIterator,FSRootRE

CLSS public abstract org.netbeans.modules.maven.j2ee.BaseEEModuleProvider
cons public init(org.netbeans.api.project.Project)
fld protected java.lang.String serverInstanceID
fld protected org.netbeans.api.project.Project project
fld protected org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule j2eemodule
fld protected org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter changeReporter
fld protected org.netbeans.modules.maven.j2ee.CopyOnSave copyOnSave
meth public abstract org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation2 getModuleImpl()
meth public boolean isOnlyCompileOnSaveEnabled()
meth public java.lang.String getServerID()
meth public java.lang.String getServerInstanceID()
meth public org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule getJ2eeModule()
meth public org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter getModuleChangeReporter()
meth public org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider$DeployOnSaveSupport getDeployOnSaveSupport()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.maven.j2ee.CopyOnSave getCopyOnSaveSupport()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public void setServerInstanceID(java.lang.String)
supr org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider

CLSS public org.netbeans.modules.maven.j2ee.ContainerCPModifierImpl
cons public init(org.netbeans.api.project.Project)
intf org.netbeans.modules.j2ee.core.api.support.classpath.ContainerClassPathModifier
meth public void extendClasspath(org.openide.filesystems.FileObject,java.lang.String[])
supr java.lang.Object
hfds LOGGER,project
hcls Item

CLSS public org.netbeans.modules.maven.j2ee.CopyOnSave
cons public init(org.netbeans.api.project.Project)
intf org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider$DeployOnSaveSupport
intf org.netbeans.modules.maven.spi.cos.AdditionalDestination
meth protected final void fireArtifactChange(java.lang.Iterable<org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener$Artifact>)
meth protected java.lang.String getDestinationSubFolderName()
meth protected org.netbeans.api.project.Project getProject()
meth protected org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule getJ2eeModule()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth protected org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider getJ2eeModuleProvider()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth protected org.openide.filesystems.FileObject ensureDestinationFileExists(org.openide.filesystems.FileObject,java.lang.String,boolean) throws java.io.IOException
meth protected void copySrcToDest(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject) throws java.io.IOException
meth public boolean containsIdeArtifacts()
meth public void addArtifactListener(org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener)
meth public void cleanup()
meth public void copy(org.openide.filesystems.FileObject,java.lang.String)
meth public void delete(org.openide.filesystems.FileObject,java.lang.String)
meth public void initialize()
meth public void removeArtifactListener(org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener)
supr java.lang.Object
hfds NB_COS,listeners,project

CLSS public org.netbeans.modules.maven.j2ee.EjbChangeDescriptorImpl
cons public init()
intf org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor
meth public boolean ejbsChanged()
meth public java.lang.String[] getChangedEjbs()
supr java.lang.Object

CLSS public org.netbeans.modules.maven.j2ee.EmbeddableEJBContainerHint
cons public init()
meth public java.lang.String getDescription()
meth public java.lang.String getDisplayName()
meth public java.lang.String getId()
meth public java.util.List<org.netbeans.spi.editor.hints.ErrorDescription> run(org.netbeans.api.java.source.CompilationInfo,com.sun.source.util.TreePath)
meth public java.util.Set<com.sun.source.tree.Tree$Kind> getTreeKinds()
meth public void cancel()
supr org.netbeans.modules.java.hints.spi.AbstractHint
hfds PROP_GF_EMBEDDED_JAR,TREE_KINDS
hcls FixEjbContainerAction

CLSS public org.netbeans.modules.maven.j2ee.J2eeActions
cons public init()
meth public static org.openide.util.ContextAwareAction verifyAction()
supr java.lang.Object
hcls VerifyAction

CLSS public org.netbeans.modules.maven.j2ee.J2eeActionsProvider
cons public init()
meth protected java.io.InputStream getActionDefinitionStream()
meth public boolean isActionEnable(java.lang.String,org.netbeans.api.project.Project,org.openide.util.Lookup)
supr org.netbeans.modules.maven.spi.actions.AbstractMavenActionsProvider
hfds ACT_DEBUG,ACT_PROFILE,ACT_RUN,MAPPINGS

CLSS public org.netbeans.modules.maven.j2ee.J2eeMavenSourcesImpl
cons public init(org.netbeans.api.project.Project)
fld public final static java.lang.String TYPE_DOC_ROOT = "doc_root"
fld public final static java.lang.String TYPE_WEB_INF = "web_inf"
intf org.netbeans.api.project.Sources
intf org.netbeans.modules.maven.spi.nodes.OtherSourcesExclude
meth public java.util.Set<java.nio.file.Path> excludedFolders()
meth public org.netbeans.api.project.SourceGroup[] getSourceGroups(java.lang.String)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
hfds cs,pcl,project,webResourceRoots
hcls WebResourceGroup

CLSS public org.netbeans.modules.maven.j2ee.JPAStuffImpl
cons public init(org.netbeans.api.project.Project)
intf org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSourcePopulator
intf org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSourceProvider
intf org.netbeans.modules.j2ee.persistence.spi.moduleinfo.JPAModuleInfo
intf org.netbeans.modules.j2ee.persistence.spi.server.ServerStatusProvider2
meth public boolean selectServer()
meth public boolean validServerInstancePresent()
meth public java.lang.Boolean isJPAVersionSupported(java.lang.String)
meth public java.lang.String getVersion()
meth public java.util.List<org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSource> getDataSources()
meth public org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSource toJPADataSource(java.lang.Object)
meth public org.netbeans.modules.j2ee.persistence.spi.moduleinfo.JPAModuleInfo$ModuleType getType()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void connect(javax.swing.JComboBox)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
hfds project,support
hcls DatasourceWrapper

CLSS public org.netbeans.modules.maven.j2ee.JavaEEProjectSettingsImpl
cons public init(org.netbeans.api.project.Project)
intf org.netbeans.modules.javaee.project.spi.JavaEEProjectSettingsImplementation
meth public java.lang.String getBrowserID()
meth public java.lang.String getServerInstanceID()
meth public org.netbeans.api.j2ee.core.Profile getProfile()
meth public void setBrowserID(java.lang.String)
meth public void setProfile(org.netbeans.api.j2ee.core.Profile)
meth public void setServerInstanceID(java.lang.String)
supr java.lang.Object
hfds project

CLSS public org.netbeans.modules.maven.j2ee.JsfSupportHandleImpl
cons public init(org.netbeans.api.project.Project)
meth protected boolean isEnabled()
supr org.netbeans.modules.web.jsfapi.spi.JsfSupportHandle
hfds project

CLSS public org.netbeans.modules.maven.j2ee.MavenJavaEEConstants
fld public final static java.lang.String ACTION_PROPERTY_DEPLOY = "netbeans.deploy"
fld public final static java.lang.String ACTION_PROPERTY_DEPLOY_DEBUG_MODE = "netbeans.deploy.debugmode"
fld public final static java.lang.String ACTION_PROPERTY_DEPLOY_OPEN = "netbeans.deploy.open.in.browser"
fld public final static java.lang.String ACTION_PROPERTY_DEPLOY_PROFILE_MODE = "netbeans.deploy.profilemode"
fld public final static java.lang.String ACTION_PROPERTY_DEPLOY_REDEPLOY = "netbeans.deploy.forceRedeploy"
fld public final static java.lang.String HINT_COPY_STATIC_RESOURCES_ON_SAVE = "netbeans.copy.static.resources.on.save"
fld public final static java.lang.String HINT_DEPLOY_J2EE_SERVER = "netbeans.hint.deploy.server"
fld public final static java.lang.String HINT_DEPLOY_J2EE_SERVER_ID = "netbeans.deployment.server.id"
fld public final static java.lang.String HINT_DEPLOY_ON_SAVE = "netbeans.deploy.on.save"
fld public final static java.lang.String HINT_J2EE_VERSION = "netbeans.hint.j2eeVersion"
fld public final static java.lang.String SELECTED_BROWSER = "netbeans.selected.browser"
supr java.lang.Object

CLSS public org.netbeans.modules.maven.j2ee.MavenJsfReferenceImplementationProvider
cons public init()
intf org.netbeans.modules.web.jsfapi.spi.JsfReferenceImplementationProvider
meth public java.nio.file.Path artifactPathFor(org.netbeans.modules.web.jsfapi.api.JsfVersion)
supr java.lang.Object
hfds JSF_VERSION_MAVEN_COORDINATES_MAPPING

CLSS public org.netbeans.modules.maven.j2ee.MissingJavaEEForUnitTestExecutionHint
cons public init()
meth public java.lang.String getDescription()
meth public java.lang.String getDisplayName()
meth public java.lang.String getDisplayName(boolean)
meth public java.lang.String getId()
meth public java.util.List<org.netbeans.spi.editor.hints.ErrorDescription> run(org.netbeans.api.java.source.CompilationInfo,com.sun.source.util.TreePath)
meth public java.util.Set<com.sun.source.tree.Tree$Kind> getTreeKinds()
meth public void cancel()
supr org.netbeans.modules.java.hints.spi.AbstractHint
hfds TREE_KINDS

CLSS public org.netbeans.modules.maven.j2ee.ModuleChangeReporterImpl
cons public init()
intf org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter
meth public boolean isManifestChanged(long)
meth public org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor getEjbChanges(long)
supr java.lang.Object

CLSS public final org.netbeans.modules.maven.j2ee.OneTimeDeployment
cons public init()
meth public java.lang.String getServerInstanceId()
meth public void reset()
meth public void setServerInstanceId(java.lang.String)
supr java.lang.Object
hfds serverInstanceID

CLSS public org.netbeans.modules.maven.j2ee.ProjectHookImpl
cons public init(org.netbeans.api.project.Project)
meth protected void projectClosed()
meth protected void projectOpened()
supr org.netbeans.spi.project.ui.ProjectOpenedHook
hfds RP,lastJ2eeProvider,preferences,preferencesListener,project,refreshListener,windowSystemListener

CLSS public final org.netbeans.modules.maven.j2ee.ServiceRegistrations
meth public static org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategyResolver createEntityManagerGenerationStrategyResolver(org.netbeans.api.project.Project)
meth public static org.netbeans.modules.j2ee.persistence.spi.provider.PersistenceProviderSupplier createPersistenceProviderSupplier(org.netbeans.api.project.Project)
meth public static org.netbeans.modules.javaee.project.api.ClientSideDevelopmentSupport createClientSideSupport(org.netbeans.api.project.Project)
supr java.lang.Object

CLSS public org.netbeans.modules.maven.j2ee.SessionContent
cons public init()
meth public java.lang.String getServerInstanceId()
meth public void setServerInstanceId(java.lang.String)
supr java.lang.Object
hfds serverInstanceID

CLSS public org.netbeans.modules.maven.j2ee.execution.CoSAlternativeExecutorImpl
cons public init()
intf org.netbeans.modules.maven.spi.cos.CoSAlternativeExecutorImplementation
meth public boolean execute(org.netbeans.modules.maven.api.execute.RunConfig,org.netbeans.modules.maven.api.execute.ExecutionContext)
supr java.lang.Object

CLSS public final org.netbeans.modules.maven.j2ee.execution.DeploymentHelper
fld public final static java.lang.String CLIENTURLPART = "netbeans.deploy.clientUrlPart"
innr public final static !enum DeploymentResult
meth public static boolean isDebugMode(org.netbeans.modules.maven.api.execute.RunConfig)
meth public static boolean isProfileMode(org.netbeans.modules.maven.api.execute.RunConfig)
meth public static org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$Mode getMode(org.netbeans.modules.maven.api.execute.RunConfig)
meth public static org.netbeans.modules.maven.j2ee.execution.DeploymentHelper$DeploymentResult perform(org.netbeans.modules.maven.api.execute.RunConfig,org.netbeans.modules.maven.api.execute.ExecutionContext)
supr java.lang.Object
hfds LOGGER,MODULEURI,NB_COS

CLSS public final static !enum org.netbeans.modules.maven.j2ee.execution.DeploymentHelper$DeploymentResult
 outer org.netbeans.modules.maven.j2ee.execution.DeploymentHelper
fld public final static org.netbeans.modules.maven.j2ee.execution.DeploymentHelper$DeploymentResult CANCELED
fld public final static org.netbeans.modules.maven.j2ee.execution.DeploymentHelper$DeploymentResult FAILED
fld public final static org.netbeans.modules.maven.j2ee.execution.DeploymentHelper$DeploymentResult SUCCESSFUL
meth public static org.netbeans.modules.maven.j2ee.execution.DeploymentHelper$DeploymentResult valueOf(java.lang.String)
meth public static org.netbeans.modules.maven.j2ee.execution.DeploymentHelper$DeploymentResult[] values()
supr java.lang.Enum<org.netbeans.modules.maven.j2ee.execution.DeploymentHelper$DeploymentResult>

CLSS public org.netbeans.modules.maven.j2ee.execution.DeploymentLogger
cons public init(org.openide.windows.OutputWriter)
intf org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$Logger
meth public void log(java.lang.String)
supr java.lang.Object
hfds logger

CLSS public org.netbeans.modules.maven.j2ee.execution.ExecutionChecker
cons public init(org.netbeans.api.project.Project)
fld public final static java.lang.String CLIENTURLPART = "netbeans.deploy.clientUrlPart"
fld public final static java.lang.String DEV_NULL = "DEV-NULL"
intf org.netbeans.modules.maven.api.execute.ExecutionResultChecker
intf org.netbeans.modules.maven.api.execute.PrerequisitesChecker
meth public boolean checkRunConfig(org.netbeans.modules.maven.api.execute.RunConfig)
meth public void executionResult(org.netbeans.modules.maven.api.execute.RunConfig,org.netbeans.modules.maven.api.execute.ExecutionContext,int)
supr java.lang.Object
hfds project

CLSS public final org.netbeans.modules.maven.j2ee.execution.ExecutionConstants
fld public final static java.lang.String SKIP_BUILD = "skip.build"
fld public final static java.lang.String STANDARD_EXECUTION = "standard.execution"
supr java.lang.Object

CLSS public org.netbeans.modules.maven.j2ee.execution.PrerequisitesCheckerImpl
cons public init()
intf org.netbeans.modules.maven.api.execute.LateBoundPrerequisitesChecker
intf org.netbeans.modules.maven.api.execute.PrerequisitesChecker
meth public boolean checkRunConfig(org.netbeans.modules.maven.api.execute.RunConfig)
meth public boolean checkRunConfig(org.netbeans.modules.maven.api.execute.RunConfig,org.netbeans.modules.maven.api.execute.ExecutionContext)
supr java.lang.Object
hfds SINGLE_ACTIONS,applicableActions

CLSS public org.netbeans.modules.maven.j2ee.execution.ServerInstanceChooserPanel
cons public init(java.lang.String[])
meth public java.lang.String getChosenServerInstance()
supr javax.swing.JPanel
hfds jCBServer,jLabelTitle
hcls ServerInstanceItem

CLSS public abstract org.netbeans.modules.maven.j2ee.ui.EEIcons
cons public init()
innr public static AppClientIcons
innr public static EarIcons
innr public static EjbIcon
innr public static WarIcon
intf org.netbeans.modules.maven.spi.nodes.SpecialIcon
supr java.lang.Object

CLSS public static org.netbeans.modules.maven.j2ee.ui.EEIcons$AppClientIcons
 outer org.netbeans.modules.maven.j2ee.ui.EEIcons
cons public init()
meth public javax.swing.Icon getIcon()
supr org.netbeans.modules.maven.j2ee.ui.EEIcons

CLSS public static org.netbeans.modules.maven.j2ee.ui.EEIcons$EarIcons
 outer org.netbeans.modules.maven.j2ee.ui.EEIcons
cons public init()
meth public javax.swing.Icon getIcon()
supr org.netbeans.modules.maven.j2ee.ui.EEIcons

CLSS public static org.netbeans.modules.maven.j2ee.ui.EEIcons$EjbIcon
 outer org.netbeans.modules.maven.j2ee.ui.EEIcons
cons public init()
meth public javax.swing.Icon getIcon()
supr org.netbeans.modules.maven.j2ee.ui.EEIcons

CLSS public static org.netbeans.modules.maven.j2ee.ui.EEIcons$WarIcon
 outer org.netbeans.modules.maven.j2ee.ui.EEIcons
cons public init()
meth public javax.swing.Icon getIcon()
supr org.netbeans.modules.maven.j2ee.ui.EEIcons

CLSS public org.netbeans.modules.maven.j2ee.ui.SelectAppServerPanel
meth public static boolean showServerSelectionDialog(org.netbeans.api.project.Project,org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider,org.netbeans.modules.maven.api.execute.RunConfig)
supr javax.swing.JPanel
hfds btChange,buttonGroup1,comServer,lblProject,lblServer,nls,project,rbDontRemember,rbIgnore,rbPermanent,rbSession

CLSS public org.netbeans.modules.maven.j2ee.ui.SelectProjectPanel
cons public init(org.netbeans.api.project.Project)
supr javax.swing.JPanel
hfds listLabel,listPanel,olp,project
hcls OpenListPanel

CLSS public org.netbeans.modules.maven.j2ee.utils.LoggingUtils
meth public static void logUI(java.lang.Class,java.lang.String,java.lang.Object[])
meth public static void logUI(java.lang.Class,java.lang.String,java.lang.Object[],java.lang.String)
meth public static void logUI(java.util.ResourceBundle,java.lang.String,java.lang.Object[])
meth public static void logUI(java.util.ResourceBundle,java.lang.String,java.lang.Object[],java.lang.String)
meth public static void logUsage(java.lang.Class,java.lang.String,java.lang.Object[])
meth public static void logUsage(java.lang.Class,java.lang.String,java.lang.Object[],java.lang.String)
meth public static void logUsage(java.util.ResourceBundle,java.lang.String,java.lang.Object[])
meth public static void logUsage(java.util.ResourceBundle,java.lang.String,java.lang.Object[],java.lang.String)
supr java.lang.Object
hfds UI_LOGGER,UI_LOGGER_NAME,USG_LOGGER,USG_LOGGER_NAME

CLSS public org.netbeans.modules.maven.j2ee.utils.MavenProjectSupport
meth public static boolean isBundlePackaging(org.netbeans.api.project.Project,java.lang.String)
meth public static boolean isCopyStaticResourcesOnSave(org.netbeans.api.project.Project)
meth public static boolean isDeployOnSave(org.netbeans.api.project.Project)
meth public static boolean isWebSupported(org.netbeans.api.project.Project,java.lang.String)
meth public static java.lang.String getSettings(org.netbeans.api.project.Project,java.lang.String,boolean)
meth public static java.lang.String obtainServerID(java.lang.String)
meth public static java.lang.String obtainServerName(org.netbeans.api.project.Project)
meth public static java.lang.String readServerID(org.netbeans.api.project.Project)
meth public static java.lang.String[] obtainServerIds(org.netbeans.api.project.Project)
meth public static java.util.prefs.Preferences getPreferences(org.netbeans.api.project.Project,boolean)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static void changeServer(org.netbeans.api.project.Project,boolean)
meth public static void createWebXMLIfRequired(org.netbeans.api.project.Project)
meth public static void createWebXMLIfRequired(org.netbeans.api.project.Project,java.lang.String)
meth public static void setCopyStaticResourcesOnSave(org.netbeans.api.project.Project,java.lang.Boolean)
meth public static void setDeployOnSave(org.netbeans.api.project.Project,java.lang.Boolean)
meth public static void setServerID(org.netbeans.api.project.Project,java.lang.String)
meth public static void setSettings(org.netbeans.api.project.Project,java.lang.String,java.lang.String,boolean)
meth public static void storeSettingsToPom(org.netbeans.api.project.Project,java.lang.String,java.lang.String)
meth public static void storeSettingsToPom(org.openide.filesystems.FileObject,java.lang.String,java.lang.String)
supr java.lang.Object
hcls AddServerAction,ServerLibraryAction

CLSS public final org.netbeans.modules.maven.j2ee.utils.Server
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
fld public final static org.netbeans.modules.maven.j2ee.utils.Server NO_SERVER_SELECTED
intf java.lang.Comparable<org.netbeans.modules.maven.j2ee.utils.Server>
meth public boolean equals(java.lang.Object)
meth public int compareTo(org.netbeans.modules.maven.j2ee.utils.Server)
meth public int hashCode()
meth public java.lang.String getServerID()
meth public java.lang.String getServerInstanceID()
meth public java.lang.String toString()
supr java.lang.Object
hfds serverID,serverInstanceId

CLSS public final org.netbeans.modules.maven.j2ee.utils.ServerUtils
meth public static java.util.List<org.netbeans.modules.maven.j2ee.utils.Server> findServersFor(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type)
meth public static org.netbeans.modules.maven.j2ee.utils.Server findServer(org.netbeans.api.project.Project)
meth public static void setServer(org.netbeans.api.project.Project,org.netbeans.modules.maven.j2ee.utils.Server)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.maven.spi.actions.AbstractMavenActionsProvider
cons public init()
fld protected org.netbeans.modules.maven.execute.model.ActionToGoalMapping originalMappings
fld protected org.netbeans.modules.maven.execute.model.io.xpp3.NetbeansBuildActionXpp3Reader reader
intf org.netbeans.modules.maven.spi.actions.MavenActionsProvider
meth protected abstract java.io.InputStream getActionDefinitionStream()
meth protected boolean reloadStream()
meth protected java.io.Reader performDynamicSubstitutions(java.util.Map<java.lang.String,java.lang.String>,java.lang.String) throws java.io.IOException
meth protected static org.openide.filesystems.FileObject[] extractFileObjectsfromLookup(org.openide.util.Lookup)
meth public boolean isActionEnable(java.lang.String,org.netbeans.api.project.Project,org.openide.util.Lookup)
meth public final boolean isActionEnable(java.lang.String,java.lang.String)
meth public final org.netbeans.modules.maven.api.execute.RunConfig createConfigForDefaultAction(java.lang.String,org.netbeans.api.project.Project,org.openide.util.Lookup)
meth public java.lang.String getRawMappingsAsString()
meth public java.util.Set<java.lang.String> getSupportedDefaultActions()
meth public org.netbeans.modules.maven.execute.model.ActionToGoalMapping getRawMappings()
meth public org.netbeans.modules.maven.execute.model.NetbeansActionMapping getMappingForAction(java.lang.String,org.netbeans.api.project.Project)
meth public static java.lang.String dynamicSubstitutions(java.util.Map<java.lang.String,java.lang.String>,java.lang.String)
meth public static org.netbeans.modules.maven.spi.actions.MavenActionsProvider fromNbActions(org.netbeans.api.project.Project,java.net.URL)
supr java.lang.Object
hfds LOG,writer
hcls ResourceConfigAwareProvider

CLSS public abstract interface org.netbeans.modules.maven.spi.actions.MavenActionsProvider
meth public abstract boolean isActionEnable(java.lang.String,org.netbeans.api.project.Project,org.openide.util.Lookup)
meth public abstract java.util.Set<java.lang.String> getSupportedDefaultActions()
meth public abstract org.netbeans.modules.maven.api.execute.RunConfig createConfigForDefaultAction(java.lang.String,org.netbeans.api.project.Project,org.openide.util.Lookup)
meth public abstract org.netbeans.modules.maven.execute.model.NetbeansActionMapping getMappingForAction(java.lang.String,org.netbeans.api.project.Project)

CLSS public abstract interface org.netbeans.modules.maven.spi.cos.AdditionalDestination
meth public abstract void copy(org.openide.filesystems.FileObject,java.lang.String)
meth public abstract void delete(org.openide.filesystems.FileObject,java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.spi.cos.CoSAlternativeExecutorImplementation
meth public abstract boolean execute(org.netbeans.modules.maven.api.execute.RunConfig,org.netbeans.modules.maven.api.execute.ExecutionContext)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.modules.maven.spi.nodes.OtherSourcesExclude
meth public abstract java.util.Set<java.nio.file.Path> excludedFolders()

CLSS public abstract interface org.netbeans.modules.maven.spi.nodes.SpecialIcon
meth public abstract javax.swing.Icon getIcon()
 anno 0 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.modules.web.jsfapi.spi.JsfReferenceImplementationProvider
meth public abstract java.nio.file.Path artifactPathFor(org.netbeans.modules.web.jsfapi.api.JsfVersion)

CLSS public org.netbeans.modules.web.jsfapi.spi.JsfSupportHandle
 anno 0 java.lang.Deprecated()
cons public init()
meth protected boolean isEnabled()
supr java.lang.Object
hfds caller,instance

CLSS public abstract org.netbeans.spi.project.ui.ProjectOpenedHook
cons protected init()
meth protected abstract void projectClosed()
meth protected abstract void projectOpened()
supr java.lang.Object

