#Signature file v4.1
#Version 1.59

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

CLSS public abstract interface org.eclipse.core.runtime.IProgressMonitor
fld public final static int UNKNOWN = -1
meth public abstract boolean isCanceled()
meth public abstract void beginTask(java.lang.String,int)
meth public abstract void done()
meth public abstract void internalWorked(double)
meth public abstract void setCanceled(boolean)
meth public abstract void setTaskName(java.lang.String)
meth public abstract void subTask(java.lang.String)
meth public abstract void worked(int)

CLSS public abstract org.netbeans.modules.mylyn.util.AbstractNbTaskWrapper
cons public init(org.netbeans.modules.mylyn.util.NbTask)
fld public final static java.lang.String NEW_ATTACHMENT_ATTRIBUTE_ID = "nb.attachments.new"
meth protected abstract boolean synchronizeTask()
meth protected abstract java.lang.String getSummary(org.eclipse.mylyn.tasks.core.data.TaskData)
meth protected abstract void attributeChanged(org.netbeans.modules.mylyn.util.NbTaskDataModel$NbTaskDataModelEvent,org.netbeans.modules.mylyn.util.NbTaskDataModel)
meth protected abstract void modelSaved(org.netbeans.modules.mylyn.util.NbTaskDataModel)
meth protected abstract void repositoryTaskDataLoaded(org.eclipse.mylyn.tasks.core.data.TaskData)
meth protected abstract void taskDataUpdated()
meth protected abstract void taskDeleted(org.netbeans.modules.mylyn.util.NbTask)
meth protected abstract void taskModified(boolean)
meth protected final boolean editorOpened()
meth protected final boolean hasUnsavedPrivateTaskAttributes()
meth protected final boolean isSeen()
meth protected final boolean saveChanges()
meth protected final boolean setNewAttachments(java.util.List<org.netbeans.modules.bugtracking.commons.AttachmentsPanel$AttachmentInfo>)
meth protected final boolean updateModel()
meth protected final java.lang.String getDueDisplayString()
meth protected final java.lang.String getEstimateDisplayString()
meth protected final java.lang.String getScheduleDisplayString()
meth protected final java.util.List<org.netbeans.modules.bugtracking.commons.AttachmentsPanel$AttachmentInfo> getNewAttachments()
meth protected final org.eclipse.mylyn.tasks.core.data.TaskData getRepositoryTaskData()
meth protected final org.netbeans.modules.mylyn.util.NbTask getNbTask()
meth protected final org.netbeans.modules.mylyn.util.NbTask$SynchronizationState getSynchronizationState()
meth protected final org.netbeans.modules.mylyn.util.NbTaskDataModel getModel()
meth protected final void deleteTask()
meth protected final void editorClosed()
meth protected final void fireChanged()
meth protected final void fireDataChanged()
meth protected final void fireScheduleChanged()
meth protected final void fireStatusChanged()
meth protected final void markNewRead()
meth protected final void runWithModelLoaded(java.lang.Runnable)
meth protected final void setDueDate(java.util.Date,boolean)
meth protected final void setEstimate(int,boolean)
meth protected final void setPrivateNotes(java.lang.String)
meth protected final void setScheduleDate(org.netbeans.modules.bugtracking.spi.IssueScheduleInfo,boolean)
meth protected final void taskSubmitted(org.netbeans.modules.mylyn.util.NbTask)
meth protected static boolean attachmentAttributesDiffer(org.eclipse.mylyn.tasks.core.data.TaskAttribute,org.eclipse.mylyn.tasks.core.data.TaskAttribute)
meth public final boolean cancelChanges()
meth public final boolean hasLocalEdits()
meth public final boolean hasUnsavedChanges()
meth public final boolean isFinished()
meth public final boolean isMarkedNewUnread()
meth public final boolean isNew()
meth public final int getEstimate()
meth public final int getPersistentEstimate()
meth public final java.lang.String getID()
meth public final java.lang.String getPrivateNotes()
meth public final java.lang.String getSummary()
meth public final java.util.Date getCreatedDate()
meth public final java.util.Date getDueDate()
meth public final java.util.Date getLastModifyDate()
meth public final java.util.Date getPersistentDueDate()
meth public final long getCreated()
meth public final long getLastModify()
meth public final org.netbeans.modules.bugtracking.spi.IssueScheduleInfo getPersistentScheduleInfo()
meth public final org.netbeans.modules.bugtracking.spi.IssueStatusProvider$Status getStatus()
meth public final org.netbeans.modules.mylyn.util.NbDateRange getScheduleDate()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void clearUnsavedChanges()
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void setUpToDate(boolean,boolean)
meth public static java.lang.String getID(org.netbeans.modules.mylyn.util.NbTask)
supr java.lang.Object
hfds ATTR_NEW_UNREAD,LOG,MODEL_LOCK,NB_NEW_ATTACHMENT_ATTR_ID,NB_NEW_ATTACHMENT_CONTENT_TYPE_ATTR_ID,NB_NEW_ATTACHMENT_DESC_ATTR_ID,NB_NEW_ATTACHMENT_FILE_ATTR_ID,NB_NEW_ATTACHMENT_PATCH_ATTR_ID,RP,dueDate,dueDateModified,estimate,list,model,privateNotes,readPending,repositoryDataRef,repositoryTaskDataLoaderTask,scheduleDate,scheduleDateModified,support,task,taskDataListener,taskListener
hcls TaskDataListenerImpl,TaskListenerImpl

CLSS public abstract org.netbeans.modules.mylyn.util.BugtrackingCommand
cons public init()
meth public abstract void execute() throws java.io.IOException,org.eclipse.core.runtime.CoreException
meth public boolean hasFailed()
meth public java.lang.String getErrorMessage()
meth public void cancel()
meth public void setErrorMessage(java.lang.String)
meth public void setFailed(boolean)
supr java.lang.Object
hfds errorMessage,failed

CLSS public org.netbeans.modules.mylyn.util.CancelableProgressMonitor
cons public init()
intf org.eclipse.core.runtime.IProgressMonitor
meth public boolean isCanceled()
meth public void beginTask(java.lang.String,int)
meth public void done()
meth public void internalWorked(double)
meth public void setCanceled(boolean)
meth public void setTaskName(java.lang.String)
meth public void subTask(java.lang.String)
meth public void worked(int)
supr java.lang.Object
hfds canceled

CLSS public org.netbeans.modules.mylyn.util.GetAttachmentCommand
cons public init(org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector,org.eclipse.mylyn.tasks.core.TaskRepository,org.eclipse.mylyn.tasks.core.ITask,org.eclipse.mylyn.tasks.core.data.TaskAttribute,java.io.OutputStream)
meth public java.lang.String toString()
meth public void execute() throws java.io.IOException,org.eclipse.core.runtime.CoreException
supr org.netbeans.modules.mylyn.util.BugtrackingCommand
hfds os,repositoryConnector,stringValue,ta,task,taskRepository

CLSS public org.netbeans.modules.mylyn.util.GetMultiTaskDataCommand
cons public init(org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector,org.eclipse.mylyn.tasks.core.TaskRepository,org.eclipse.mylyn.tasks.core.data.TaskDataCollector,java.util.Set<java.lang.String>)
meth public java.lang.String toString()
meth public void execute() throws org.eclipse.core.runtime.CoreException
supr org.netbeans.modules.mylyn.util.BugtrackingCommand
hfds collector,ids,repositoryConnector,taskRepository

CLSS public org.netbeans.modules.mylyn.util.GetTaskDataCommand
cons public init(org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector,org.eclipse.mylyn.tasks.core.TaskRepository,java.lang.String)
meth public java.lang.String toString()
meth public org.eclipse.mylyn.tasks.core.data.TaskData getTaskData()
meth public void execute() throws java.io.IOException,org.eclipse.core.runtime.CoreException
supr org.netbeans.modules.mylyn.util.BugtrackingCommand
hfds id,repositoryConnector,taskData,taskRepository

CLSS public org.netbeans.modules.mylyn.util.MylynSupport
meth public java.util.Collection<org.netbeans.modules.mylyn.util.NbTask> getTasks(org.eclipse.mylyn.tasks.core.IRepositoryQuery) throws org.eclipse.core.runtime.CoreException
meth public java.util.Collection<org.netbeans.modules.mylyn.util.NbTask> getTasks(org.eclipse.mylyn.tasks.core.TaskRepository) throws org.eclipse.core.runtime.CoreException
meth public java.util.Set<org.eclipse.mylyn.tasks.core.IRepositoryQuery> getRepositoryQueries(org.eclipse.mylyn.tasks.core.TaskRepository) throws org.eclipse.core.runtime.CoreException
meth public java.util.Set<org.eclipse.mylyn.tasks.core.data.TaskAttribute> countDiff(org.eclipse.mylyn.tasks.core.data.TaskData,org.eclipse.mylyn.tasks.core.data.TaskData)
meth public org.eclipse.mylyn.tasks.core.IRepositoryQuery createNewQuery(org.eclipse.mylyn.tasks.core.TaskRepository,java.lang.String) throws org.eclipse.core.runtime.CoreException
meth public org.eclipse.mylyn.tasks.core.IRepositoryQuery getRepositoryQuery(org.eclipse.mylyn.tasks.core.TaskRepository,java.lang.String) throws org.eclipse.core.runtime.CoreException
meth public org.eclipse.mylyn.tasks.core.TaskRepository getLocalTaskRepository()
meth public org.eclipse.mylyn.tasks.core.TaskRepository getTaskRepository(org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector,java.lang.String)
meth public org.netbeans.modules.mylyn.util.NbTask createSubtask(org.netbeans.modules.mylyn.util.NbTask) throws org.eclipse.core.runtime.CoreException
meth public org.netbeans.modules.mylyn.util.NbTask createTask(org.eclipse.mylyn.tasks.core.TaskRepository,org.eclipse.mylyn.tasks.core.ITaskMapping) throws org.eclipse.core.runtime.CoreException
meth public org.netbeans.modules.mylyn.util.NbTask getTask(java.lang.String,java.lang.String) throws org.eclipse.core.runtime.CoreException
meth public org.netbeans.modules.mylyn.util.UnsubmittedTasksContainer getUnsubmittedTasksContainer(org.eclipse.mylyn.tasks.core.TaskRepository) throws org.eclipse.core.runtime.CoreException
meth public org.netbeans.modules.mylyn.util.commands.CommandFactory getCommandFactory() throws org.eclipse.core.runtime.CoreException
meth public static org.netbeans.modules.mylyn.util.MylynSupport getInstance()
meth public void addQuery(org.eclipse.mylyn.tasks.core.TaskRepository,org.eclipse.mylyn.tasks.core.IRepositoryQuery) throws org.eclipse.core.runtime.CoreException
meth public void addRepositoryListener(org.eclipse.mylyn.tasks.core.IRepositoryListener)
meth public void addTaskDataListener(org.netbeans.modules.mylyn.util.TaskDataListener)
meth public void deleteQuery(org.eclipse.mylyn.tasks.core.IRepositoryQuery)
meth public void notifyCredentialsChanged(org.eclipse.mylyn.tasks.core.TaskRepository)
meth public void removeTaskDataListener(org.netbeans.modules.mylyn.util.TaskDataListener)
meth public void save() throws org.eclipse.core.runtime.CoreException
meth public void setRepositoryUrl(org.eclipse.mylyn.tasks.core.TaskRepository,java.lang.String) throws org.eclipse.core.runtime.CoreException
supr java.lang.Object
hfds ATTR_TASK_INCOMING_NEW,BACKUP_SUFFIX,LOG,PROP_REPOSITORY_CREATION_TIME,RP,dirty,factory,instance,localTaskRepository,repositoryModel,result,saveTask,synchronizationManager,taskActivityManager,taskDataListeners,taskDataManager,taskDataManagerListener,taskList,taskListInitialized,taskListListener,taskListStorageFile,taskListWriter,taskListeners,taskPerList,taskRepositoryManager,taskRepositoryManagerListener,tasks,unsubmittedTaskContainers

CLSS public org.netbeans.modules.mylyn.util.MylynUtils
cons public init()
meth public static org.eclipse.mylyn.tasks.core.TaskRepository createTaskRepository(java.lang.String,java.lang.String,java.lang.String,java.lang.String,char[],java.lang.String,char[])
meth public static void logCredentials(org.eclipse.mylyn.tasks.core.TaskRepository,java.lang.String,char[],java.lang.String)
meth public static void logCredentials(org.eclipse.mylyn.tasks.core.TaskRepository,java.lang.String,java.lang.String,java.lang.String)
meth public static void setCredentials(org.eclipse.mylyn.tasks.core.TaskRepository,java.lang.String,char[],java.lang.String,char[])
supr java.lang.Object
hfds LOG

CLSS public final org.netbeans.modules.mylyn.util.NbDateRange
cons public init(java.util.Calendar)
cons public init(java.util.Calendar,java.util.Calendar)
cons public init(org.netbeans.modules.bugtracking.spi.IssueScheduleInfo)
intf java.lang.Comparable<org.netbeans.modules.mylyn.util.NbDateRange>
meth public boolean equals(java.lang.Object)
meth public int compareTo(org.netbeans.modules.mylyn.util.NbDateRange)
meth public int hashCode()
meth public java.util.Calendar getEndDate()
meth public java.util.Calendar getStartDate()
meth public org.netbeans.modules.bugtracking.spi.IssueScheduleInfo toSchedulingInfo()
supr java.lang.Object
hfds delegate

CLSS public final org.netbeans.modules.mylyn.util.NbTask
innr public final static !enum SynchronizationState
meth public boolean equals(java.lang.Object)
meth public boolean isCompleted()
meth public boolean isOutgoing()
meth public final java.lang.String getPrivateNotes()
meth public final java.util.Date getDueDate()
meth public final org.netbeans.modules.mylyn.util.NbDateRange getScheduleDate()
meth public final void setDueDate(java.util.Date)
meth public final void setPrivateNotes(java.lang.String)
meth public final void setScheduleDate(org.netbeans.modules.mylyn.util.NbDateRange)
meth public int getEstimate()
meth public int hashCode()
meth public java.lang.String getAttribute(java.lang.String)
meth public java.lang.String getRepositoryUrl()
meth public java.lang.String getSummary()
meth public java.lang.String getTaskId()
meth public java.lang.String getTaskKey()
meth public java.lang.String toString()
meth public java.util.Date getCreationDate()
meth public java.util.Date getModificationDate()
meth public org.netbeans.modules.mylyn.util.NbTask$SynchronizationState getSynchronizationState()
meth public org.netbeans.modules.mylyn.util.NbTaskDataModel getTaskDataModel()
meth public org.netbeans.modules.mylyn.util.NbTaskDataState getTaskDataState() throws org.eclipse.core.runtime.CoreException
meth public void addNbTaskListener(org.netbeans.modules.mylyn.util.NbTaskListener)
meth public void delete()
meth public void discardLocalEdits() throws org.eclipse.core.runtime.CoreException
meth public void markSeen(boolean)
meth public void removeNbTaskListener(org.netbeans.modules.mylyn.util.NbTaskListener)
meth public void setAttribute(java.lang.String,java.lang.String)
meth public void setEstimate(int)
meth public void setSummary(java.lang.String)
supr java.lang.Object
hfds delegate,list,listeners,syncState
hcls TaskListenerImpl

CLSS public final static !enum org.netbeans.modules.mylyn.util.NbTask$SynchronizationState
 outer org.netbeans.modules.mylyn.util.NbTask
fld public final static org.netbeans.modules.mylyn.util.NbTask$SynchronizationState CONFLICT
fld public final static org.netbeans.modules.mylyn.util.NbTask$SynchronizationState INCOMING
fld public final static org.netbeans.modules.mylyn.util.NbTask$SynchronizationState INCOMING_NEW
fld public final static org.netbeans.modules.mylyn.util.NbTask$SynchronizationState OUTGOING
fld public final static org.netbeans.modules.mylyn.util.NbTask$SynchronizationState OUTGOING_NEW
fld public final static org.netbeans.modules.mylyn.util.NbTask$SynchronizationState SYNCHRONIZED
meth public static org.netbeans.modules.mylyn.util.NbTask$SynchronizationState valueOf(java.lang.String)
meth public static org.netbeans.modules.mylyn.util.NbTask$SynchronizationState[] values()
supr java.lang.Enum<org.netbeans.modules.mylyn.util.NbTask$SynchronizationState>

CLSS public final org.netbeans.modules.mylyn.util.NbTaskDataModel
innr public abstract interface static NbTaskDataModelListener
innr public final static NbTaskDataModelEvent
meth public boolean hasBeenRead()
meth public boolean hasIncomingChanges(org.eclipse.mylyn.tasks.core.data.TaskAttribute,boolean)
meth public boolean hasOutgoingChanged()
meth public boolean hasOutgoingChanges(org.eclipse.mylyn.tasks.core.data.TaskAttribute)
meth public boolean isDirty()
meth public java.util.Set<org.eclipse.mylyn.tasks.core.data.TaskAttribute> getChangedAttributes()
meth public java.util.Set<org.eclipse.mylyn.tasks.core.data.TaskAttribute> getChangedOldAttributes()
meth public org.eclipse.mylyn.tasks.core.TaskRepository getTaskRepository()
meth public org.eclipse.mylyn.tasks.core.data.TaskData getLastReadTaskData()
meth public org.eclipse.mylyn.tasks.core.data.TaskData getLocalTaskData()
meth public org.eclipse.mylyn.tasks.core.data.TaskData getRepositoryTaskData()
meth public org.netbeans.modules.mylyn.util.NbTask getTask()
meth public void addNbTaskDataModelListener(org.netbeans.modules.mylyn.util.NbTaskDataModel$NbTaskDataModelListener)
meth public void attributeChanged(org.eclipse.mylyn.tasks.core.data.TaskAttribute)
meth public void clearUnsavedChanges()
meth public void refresh() throws org.eclipse.core.runtime.CoreException
meth public void removeNbTaskDataModelListener(org.netbeans.modules.mylyn.util.NbTaskDataModel$NbTaskDataModelListener)
meth public void save() throws org.eclipse.core.runtime.CoreException
meth public void save(org.eclipse.core.runtime.IProgressMonitor) throws org.eclipse.core.runtime.CoreException
supr java.lang.Object
hfds delegateModel,listeners,task,unsavedChangedAttributes,workingCopy

CLSS public final static org.netbeans.modules.mylyn.util.NbTaskDataModel$NbTaskDataModelEvent
 outer org.netbeans.modules.mylyn.util.NbTaskDataModel
meth public org.eclipse.mylyn.tasks.core.data.TaskAttribute getTaskAttribute()
meth public org.netbeans.modules.mylyn.util.NbTaskDataModel getModel()
supr java.util.EventObject
hfds model,modelEvent

CLSS public abstract interface static org.netbeans.modules.mylyn.util.NbTaskDataModel$NbTaskDataModelListener
 outer org.netbeans.modules.mylyn.util.NbTaskDataModel
intf java.util.EventListener
meth public abstract void attributeChanged(org.netbeans.modules.mylyn.util.NbTaskDataModel$NbTaskDataModelEvent)

CLSS public final org.netbeans.modules.mylyn.util.NbTaskDataState
meth public org.eclipse.mylyn.tasks.core.data.TaskData getLastReadData()
meth public org.eclipse.mylyn.tasks.core.data.TaskData getLocalData()
meth public org.eclipse.mylyn.tasks.core.data.TaskData getLocalEdits()
meth public org.eclipse.mylyn.tasks.core.data.TaskData getRepositoryData()
supr java.lang.Object
hfds state

CLSS public abstract interface org.netbeans.modules.mylyn.util.NbTaskListener
innr public final static TaskEvent
intf java.util.EventListener
meth public abstract void taskModified(org.netbeans.modules.mylyn.util.NbTaskListener$TaskEvent)

CLSS public final static org.netbeans.modules.mylyn.util.NbTaskListener$TaskEvent
 outer org.netbeans.modules.mylyn.util.NbTaskListener
innr public final static !enum Kind
meth public boolean taskStateChanged()
meth public org.netbeans.modules.mylyn.util.NbTask getTask()
meth public org.netbeans.modules.mylyn.util.NbTaskListener$TaskEvent$Kind getKind()
supr java.util.EventObject
hfds kind,stateChanged,task

CLSS public final static !enum org.netbeans.modules.mylyn.util.NbTaskListener$TaskEvent$Kind
 outer org.netbeans.modules.mylyn.util.NbTaskListener$TaskEvent
fld public final static org.netbeans.modules.mylyn.util.NbTaskListener$TaskEvent$Kind DELETED
fld public final static org.netbeans.modules.mylyn.util.NbTaskListener$TaskEvent$Kind MODIFIED
meth public static org.netbeans.modules.mylyn.util.NbTaskListener$TaskEvent$Kind valueOf(java.lang.String)
meth public static org.netbeans.modules.mylyn.util.NbTaskListener$TaskEvent$Kind[] values()
supr java.lang.Enum<org.netbeans.modules.mylyn.util.NbTaskListener$TaskEvent$Kind>

CLSS public org.netbeans.modules.mylyn.util.PerformQueryCommand
cons public init(org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector,org.eclipse.mylyn.tasks.core.TaskRepository,org.eclipse.mylyn.tasks.core.data.TaskDataCollector,org.eclipse.mylyn.tasks.core.IRepositoryQuery)
meth public java.lang.String toString()
meth public org.eclipse.core.runtime.IStatus getStatus()
meth public org.eclipse.mylyn.tasks.core.IRepositoryQuery getQuery()
meth public void execute() throws org.eclipse.core.runtime.CoreException
supr org.netbeans.modules.mylyn.util.BugtrackingCommand
hfds collector,query,repositoryConnector,status,taskRepository

CLSS public org.netbeans.modules.mylyn.util.PostAttachmentCommand
cons public init(org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector,org.eclipse.mylyn.tasks.core.TaskRepository,org.eclipse.mylyn.tasks.core.ITask,org.eclipse.mylyn.tasks.core.data.TaskAttribute,org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource,java.lang.String)
meth public java.lang.String toString()
meth public void execute() throws java.io.IOException,org.eclipse.core.runtime.CoreException
supr org.netbeans.modules.mylyn.util.BugtrackingCommand
hfds attAttribute,comment,repositoryConnector,stringValue,task,taskAttachmentSource,taskRepository

CLSS public abstract interface org.netbeans.modules.mylyn.util.RepositoryConnectorProvider
meth public abstract org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector getConnector()

CLSS public org.netbeans.modules.mylyn.util.SubmitCommand
cons public init(org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector,org.eclipse.mylyn.tasks.core.TaskRepository,org.eclipse.mylyn.tasks.core.data.TaskData)
meth public java.lang.String toString()
meth public org.eclipse.mylyn.tasks.core.RepositoryResponse getRepositoryResponse()
meth public void execute() throws java.io.IOException,org.eclipse.core.runtime.CoreException
supr org.netbeans.modules.mylyn.util.BugtrackingCommand
hfds data,repositoryConnector,rr,stringValue,taskRepository,wasNew

CLSS public abstract interface org.netbeans.modules.mylyn.util.TaskDataListener
innr public final static TaskDataEvent
intf java.util.EventListener
meth public abstract void taskDataUpdated(org.netbeans.modules.mylyn.util.TaskDataListener$TaskDataEvent)

CLSS public final static org.netbeans.modules.mylyn.util.TaskDataListener$TaskDataEvent
 outer org.netbeans.modules.mylyn.util.TaskDataListener
meth public boolean getTaskDataUpdated()
meth public org.eclipse.mylyn.tasks.core.data.TaskData getTaskData()
meth public org.netbeans.modules.mylyn.util.NbTask getTask()
supr java.util.EventObject
hfds event

CLSS public final org.netbeans.modules.mylyn.util.UnsubmittedTasksContainer
fld public final static java.lang.String EVENT_ISSUES_CHANGED = "mylyn.unsubmitted_tasks.changed"
meth public java.util.List<org.netbeans.modules.mylyn.util.NbTask> getTasks()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds LOG,list,repository,supp,support,taskList,tasks
hcls TaskListListener

CLSS public abstract org.netbeans.modules.mylyn.util.WikiPanel
cons public init()
meth public abstract java.lang.String getWikiFormatText()
meth public abstract javax.swing.JLabel getWarningLabel()
meth public abstract javax.swing.JTextPane getCodePane()
meth public abstract javax.swing.JTextPane getPreviewPane()
meth public abstract void appendCodeText(java.lang.String)
meth public abstract void clear()
meth public abstract void registerHighlights(javax.swing.JTextPane)
meth public abstract void setWikiFormatText(java.lang.String)
supr javax.swing.JPanel

CLSS public org.netbeans.modules.mylyn.util.WikiUtils
meth public static java.lang.String getHtmlFormatText(java.lang.String,java.lang.String)
meth public static org.netbeans.modules.mylyn.util.WikiPanel getWikiPanel(java.lang.String,boolean,boolean)
supr java.lang.Object
hfds LOG

CLSS public final org.netbeans.modules.mylyn.util.commands.CommandFactory
meth public org.netbeans.modules.mylyn.util.commands.GetAttachmentCommand createGetAttachmentCommand(org.eclipse.mylyn.tasks.core.TaskRepository,org.netbeans.modules.mylyn.util.NbTask,org.eclipse.mylyn.tasks.core.data.TaskAttribute,java.io.OutputStream)
meth public org.netbeans.modules.mylyn.util.commands.GetRepositoryTasksCommand createGetRepositoryTasksCommand(org.eclipse.mylyn.tasks.core.TaskRepository,java.util.Set<java.lang.String>) throws org.eclipse.core.runtime.CoreException
meth public org.netbeans.modules.mylyn.util.commands.PostAttachmentCommand createPostAttachmentCommand(org.eclipse.mylyn.tasks.core.TaskRepository,org.netbeans.modules.mylyn.util.NbTask,org.eclipse.mylyn.tasks.core.data.TaskAttribute,org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource,java.lang.String)
meth public org.netbeans.modules.mylyn.util.commands.SimpleQueryCommand createSimpleQueryCommand(org.eclipse.mylyn.tasks.core.TaskRepository,org.eclipse.mylyn.tasks.core.IRepositoryQuery) throws org.eclipse.core.runtime.CoreException
meth public org.netbeans.modules.mylyn.util.commands.SubmitTaskCommand createSubmitTaskCommand(org.netbeans.modules.mylyn.util.NbTaskDataModel) throws org.eclipse.core.runtime.CoreException
meth public org.netbeans.modules.mylyn.util.commands.SynchronizeQueryCommand createSynchronizeQueriesCommand(org.eclipse.mylyn.tasks.core.TaskRepository,org.eclipse.mylyn.tasks.core.IRepositoryQuery)
meth public org.netbeans.modules.mylyn.util.commands.SynchronizeQueryCommand createSynchronizeQueriesCommand(org.eclipse.mylyn.tasks.core.TaskRepository,org.eclipse.mylyn.tasks.core.IRepositoryQuery,org.eclipse.core.runtime.IProgressMonitor)
meth public org.netbeans.modules.mylyn.util.commands.SynchronizeTasksCommand createSynchronizeTasksCommand(org.eclipse.mylyn.tasks.core.TaskRepository,java.util.Set<org.netbeans.modules.mylyn.util.NbTask>)
 anno 0 java.lang.Deprecated()
meth public org.netbeans.modules.mylyn.util.commands.SynchronizeTasksCommand createSynchronizeTasksCommand(org.eclipse.mylyn.tasks.core.TaskRepository,java.util.Set<org.netbeans.modules.mylyn.util.NbTask>,boolean)
supr java.lang.Object
hfds LOG,repositoryModel,taskDataManager,taskList,taskRepositoryManager

CLSS public org.netbeans.modules.mylyn.util.commands.GetAttachmentCommand
meth public java.lang.String toString()
meth public void execute() throws java.io.IOException,org.eclipse.core.runtime.CoreException
supr org.netbeans.modules.mylyn.util.BugtrackingCommand
hfds os,repositoryConnector,stringValue,ta,task,taskRepository

CLSS public org.netbeans.modules.mylyn.util.commands.GetRepositoryTasksCommand
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.mylyn.util.NbTask> getTasks()
meth public void cancel()
meth public void execute() throws java.io.IOException,org.eclipse.core.runtime.CoreException
supr org.netbeans.modules.mylyn.util.BugtrackingCommand
hfds connector,monitor,stringValue,taskDataManager,taskIds,taskRepository,tasks
hcls Collector

CLSS public org.netbeans.modules.mylyn.util.commands.PostAttachmentCommand
meth public java.lang.String toString()
meth public void execute() throws java.io.IOException,org.eclipse.core.runtime.CoreException
supr org.netbeans.modules.mylyn.util.BugtrackingCommand
hfds attAttribute,comment,repositoryConnector,stringValue,task,taskAttachmentSource,taskRepository

CLSS public org.netbeans.modules.mylyn.util.commands.SimpleQueryCommand
meth public java.lang.String toString()
meth public java.util.Collection<org.netbeans.modules.mylyn.util.NbTask> getTasks()
meth public org.eclipse.core.runtime.IStatus getStatus()
meth public void cancel()
meth public void execute() throws org.eclipse.core.runtime.CoreException
supr org.netbeans.modules.mylyn.util.BugtrackingCommand
hfds monitor,query,repositoryConnector,status,taskDataManager,taskRepository,tasks

CLSS public org.netbeans.modules.mylyn.util.commands.SubmitTaskCommand
meth public java.lang.String toString()
meth public org.eclipse.mylyn.tasks.core.RepositoryResponse getRepositoryResponse()
meth public org.netbeans.modules.mylyn.util.NbTask getSubmittedTask()
meth public void cancel()
meth public void execute() throws java.io.IOException,org.eclipse.core.runtime.CoreException
supr org.netbeans.modules.mylyn.util.BugtrackingCommand
hfds changedOldAttributes,monitor,repositoryConnector,rr,stringValue,submitJobListener,submittedTask,task,taskData,taskDataManager,taskRepository
hcls MylynSubmitTaskJob

CLSS public org.netbeans.modules.mylyn.util.commands.SynchronizeQueryCommand
innr public abstract interface static CommandProgressListener
meth public java.lang.String toString()
meth public org.eclipse.core.runtime.IStatus getStatus()
meth public void addCommandProgressListener(org.netbeans.modules.mylyn.util.commands.SynchronizeQueryCommand$CommandProgressListener)
meth public void cancel()
meth public void execute() throws java.io.IOException,org.eclipse.core.runtime.CoreException
meth public void removeCommandProgressListener(org.netbeans.modules.mylyn.util.commands.SynchronizeQueryCommand$CommandProgressListener)
supr org.netbeans.modules.mylyn.util.BugtrackingCommand
hfds listeners,monitor,query,repositoryConnector,repositoryModel,status,stringValue,taskDataManager,taskList,taskRepository

CLSS public abstract interface static org.netbeans.modules.mylyn.util.commands.SynchronizeQueryCommand$CommandProgressListener
 outer org.netbeans.modules.mylyn.util.commands.SynchronizeQueryCommand
intf java.util.EventListener
meth public abstract void queryRefreshStarted(java.util.Collection<org.netbeans.modules.mylyn.util.NbTask>)
meth public abstract void taskAdded(org.netbeans.modules.mylyn.util.NbTask)
meth public abstract void taskRemoved(org.netbeans.modules.mylyn.util.NbTask)
meth public abstract void taskSynchronized(org.netbeans.modules.mylyn.util.NbTask)
meth public abstract void tasksRefreshStarted(java.util.Collection<org.netbeans.modules.mylyn.util.NbTask>)

CLSS public org.netbeans.modules.mylyn.util.commands.SynchronizeTasksCommand
meth public java.lang.String toString()
meth public void cancel()
meth public void execute() throws java.io.IOException,org.eclipse.core.runtime.CoreException
supr org.netbeans.modules.mylyn.util.BugtrackingCommand
hfds monitor,repositoryConnector,repositoryModel,stringValue,taskDataManager,taskList,taskRepository,tasks,user

CLSS public abstract org.netbeans.modules.mylyn.util.localtasks.AbstractLocalTask
cons public init(org.netbeans.modules.mylyn.util.NbTask)
meth public final boolean finish()
meth public final boolean reopen()
supr org.netbeans.modules.mylyn.util.AbstractNbTaskWrapper

CLSS public final org.netbeans.modules.mylyn.util.localtasks.IssueField
fld public final static org.netbeans.modules.mylyn.util.localtasks.IssueField ATTACHMENTS
fld public final static org.netbeans.modules.mylyn.util.localtasks.IssueField REFERENCES
fld public final static org.netbeans.modules.mylyn.util.localtasks.IssueField SUMMARY
meth public java.lang.String getDisplayName()
meth public java.lang.String getKey()
supr java.lang.Object
hfds displayNameKey,key

