#Signature file v4.1
#Version 2.108

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

CLSS public javax.swing.JTabbedPane
cons public init()
cons public init(int)
cons public init(int,int)
fld protected int tabPlacement
fld protected javax.swing.SingleSelectionModel model
fld protected javax.swing.event.ChangeEvent changeEvent
fld protected javax.swing.event.ChangeListener changeListener
fld public final static int SCROLL_TAB_LAYOUT = 1
fld public final static int WRAP_TAB_LAYOUT = 0
innr protected AccessibleJTabbedPane
innr protected ModelListener
intf java.io.Serializable
intf javax.accessibility.Accessible
intf javax.swing.SwingConstants
meth protected java.lang.String paramString()
meth protected javax.swing.event.ChangeListener createChangeListener()
meth protected void fireStateChanged()
meth public boolean isEnabledAt(int)
meth public int getDisplayedMnemonicIndexAt(int)
meth public int getMnemonicAt(int)
meth public int getSelectedIndex()
meth public int getTabCount()
meth public int getTabLayoutPolicy()
meth public int getTabPlacement()
meth public int getTabRunCount()
meth public int indexAtLocation(int,int)
meth public int indexOfComponent(java.awt.Component)
meth public int indexOfTab(java.lang.String)
meth public int indexOfTab(javax.swing.Icon)
meth public int indexOfTabComponent(java.awt.Component)
meth public java.awt.Color getBackgroundAt(int)
meth public java.awt.Color getForegroundAt(int)
meth public java.awt.Component add(java.awt.Component)
meth public java.awt.Component add(java.awt.Component,int)
meth public java.awt.Component add(java.lang.String,java.awt.Component)
meth public java.awt.Component getComponentAt(int)
meth public java.awt.Component getSelectedComponent()
meth public java.awt.Component getTabComponentAt(int)
meth public java.awt.Rectangle getBoundsAt(int)
meth public java.lang.String getTitleAt(int)
meth public java.lang.String getToolTipText(java.awt.event.MouseEvent)
meth public java.lang.String getToolTipTextAt(int)
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.Icon getDisabledIconAt(int)
meth public javax.swing.Icon getIconAt(int)
meth public javax.swing.SingleSelectionModel getModel()
meth public javax.swing.event.ChangeListener[] getChangeListeners()
meth public javax.swing.plaf.TabbedPaneUI getUI()
meth public void add(java.awt.Component,java.lang.Object)
meth public void add(java.awt.Component,java.lang.Object,int)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void addTab(java.lang.String,java.awt.Component)
meth public void addTab(java.lang.String,javax.swing.Icon,java.awt.Component)
meth public void addTab(java.lang.String,javax.swing.Icon,java.awt.Component,java.lang.String)
meth public void insertTab(java.lang.String,javax.swing.Icon,java.awt.Component,java.lang.String,int)
meth public void remove(int)
meth public void remove(java.awt.Component)
meth public void removeAll()
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void removeTabAt(int)
meth public void setBackgroundAt(int,java.awt.Color)
meth public void setComponentAt(int,java.awt.Component)
meth public void setDisabledIconAt(int,javax.swing.Icon)
meth public void setDisplayedMnemonicIndexAt(int,int)
meth public void setEnabledAt(int,boolean)
meth public void setForegroundAt(int,java.awt.Color)
meth public void setIconAt(int,javax.swing.Icon)
meth public void setMnemonicAt(int,int)
meth public void setModel(javax.swing.SingleSelectionModel)
meth public void setSelectedComponent(java.awt.Component)
meth public void setSelectedIndex(int)
meth public void setTabComponentAt(int,java.awt.Component)
meth public void setTabLayoutPolicy(int)
meth public void setTabPlacement(int)
meth public void setTitleAt(int,java.lang.String)
meth public void setToolTipTextAt(int,java.lang.String)
meth public void setUI(javax.swing.plaf.TabbedPaneUI)
meth public void updateUI()
supr javax.swing.JComponent

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

CLSS public abstract org.netbeans.core.windows.nativeaccess.NativeWindowSystem
cons public init()
meth public abstract boolean isUndecoratedWindowAlphaSupported()
meth public abstract boolean isWindowAlphaSupported()
meth public abstract void setWindowAlpha(java.awt.Window,float)
meth public abstract void setWindowMask(java.awt.Window,java.awt.Shape)
meth public abstract void setWindowMask(java.awt.Window,javax.swing.Icon)
meth public final static org.netbeans.core.windows.nativeaccess.NativeWindowSystem getDefault()
supr java.lang.Object
hfds dummyInstance

CLSS public org.netbeans.core.windows.options.LafOptionsPanelController
cons public init()
meth protected org.netbeans.core.windows.options.LafPanel getPanel()
meth protected void changed(boolean)
meth protected void setCurrentSubcategory(java.lang.String)
meth public boolean isChanged()
meth public boolean isValid()
meth public javax.swing.JComponent getComponent(org.openide.util.Lookup)
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void applyChanges()
meth public void cancel()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void update()
supr org.netbeans.spi.options.OptionsPanelController
hfds changed,panel,pcs

CLSS public org.netbeans.core.windows.options.LafPanel
cons protected init(org.netbeans.core.windows.options.LafOptionsPanelController)
fld protected final org.netbeans.core.windows.options.LafOptionsPanelController controller
meth protected boolean store()
meth protected void load()
supr javax.swing.JPanel
hfds COLOR_MODEL_CLASS_NAME,NO_RESTART_ON_LAF_CHANGE,buttonGroup1,checkMaximizeNativeLaF,comboLaf,defaultLookAndFeelIndex,isAquaLaF,lafs,lblLaf,lblRestart,panelLaF,panelLaFCombo,prefs,restartNotification

CLSS public org.netbeans.core.windows.options.TabsOptionsPanelController
cons public init()
meth protected org.netbeans.core.windows.options.TabsPanel getPanel()
meth protected void changed(java.lang.Object,java.lang.Object)
meth public boolean isChanged()
meth public boolean isValid()
meth public javax.swing.JComponent getComponent(org.openide.util.Lookup)
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void applyChanges()
meth public void cancel()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void update()
supr org.netbeans.spi.options.OptionsPanelController
hfds changed,changedInnerTabsPanel,panel,pcs

CLSS public org.netbeans.core.windows.options.TabsPanel
cons protected init(org.netbeans.core.windows.options.TabsOptionsPanelController)
fld protected final org.netbeans.core.windows.options.TabsOptionsPanelController controller
innr public final static !enum EditorSortType
meth protected boolean store()
meth protected void initTabsPanel(javax.swing.JPanel)
meth protected void load()
supr javax.swing.JPanel
hfds buttonGroup1,checkMultiRow,defMultiRow,defTabPlacement,isAquaLaF,isCloseActivatesMostRecentDocument,isNewDocumentOpensNextToActiveTab,jLabel1,panelDocTabs,panelTabs,prefs,radioBottom,radioLeft,radioRight,radioSortFileName,radioSortFileNameWithParent,radioSortFullFilePath,radioSortNothing,radioTop,sortButtonGroup,sortTabsLabel

CLSS public final static !enum org.netbeans.core.windows.options.TabsPanel$EditorSortType
 outer org.netbeans.core.windows.options.TabsPanel
fld public final static org.netbeans.core.windows.options.TabsPanel$EditorSortType FileName
fld public final static org.netbeans.core.windows.options.TabsPanel$EditorSortType FileNameWithParent
fld public final static org.netbeans.core.windows.options.TabsPanel$EditorSortType FullFilePath
fld public final static org.netbeans.core.windows.options.TabsPanel$EditorSortType None
meth public static org.netbeans.core.windows.options.TabsPanel$EditorSortType valueOf(java.lang.String)
meth public static org.netbeans.core.windows.options.TabsPanel$EditorSortType[] values()
supr java.lang.Enum<org.netbeans.core.windows.options.TabsPanel$EditorSortType>

CLSS public org.netbeans.core.windows.options.WinSysOptionsPanelController
cons public init()
meth protected org.netbeans.core.windows.options.WinSysPanel getPanel()
meth protected void changed(boolean)
meth public boolean isChanged()
meth public boolean isValid()
meth public javax.swing.JComponent getComponent(org.openide.util.Lookup)
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void applyChanges()
meth public void cancel()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void update()
supr org.netbeans.spi.options.OptionsPanelController
hfds changed,panel,pcs

CLSS public org.netbeans.core.windows.options.WinSysPanel
cons protected init(org.netbeans.core.windows.options.WinSysOptionsPanelController)
fld protected final org.netbeans.core.windows.options.WinSysOptionsPanelController controller
meth protected boolean store()
meth protected void initTabsPanel(javax.swing.JPanel)
meth protected void load()
supr javax.swing.JPanel
hfds buttonGroup1,isAlphaFloating,isDragImage,isDragImageAlpha,isSnapScreenEdges,isSnapping,prefs

CLSS public abstract interface org.netbeans.core.windows.options.WinSysPrefs
fld public final static java.lang.String DND_DRAGIMAGE = "dnd.dragimage"
fld public final static java.lang.String DND_SMALLWINDOWS = "dnd.smallwindows"
fld public final static java.lang.String DND_SMALLWINDOWS_HEIGHT = "dnd.smallwindows.height"
fld public final static java.lang.String DND_SMALLWINDOWS_WIDTH = "dnd.smallwindows.width"
fld public final static java.lang.String DOCUMENT_TABS_MULTIROW = "document.tabs.multirow"
fld public final static java.lang.String DOCUMENT_TABS_PLACEMENT = "document.tabs.placement"
fld public final static java.lang.String EDITOR_CLOSE_ACTIVATES_RECENT = "editor.closing.activates.recent"
fld public final static java.lang.String EDITOR_SORT_TABS = "editor.sort.tabs"
fld public final static java.lang.String MAXIMIZE_NATIVE_LAF = "laf.maximize.native"
fld public final static java.lang.String OPEN_DOCUMENTS_NEXT_TO_ACTIVE_TAB = "editor.open.next.to.active"
fld public final static java.lang.String SNAPPING = "snapping"
fld public final static java.lang.String SNAPPING_ACTIVE_SIZE = "snapping.active.size"
fld public final static java.lang.String SNAPPING_SCREENEDGES = "snapping.screenedges"
fld public final static java.lang.String TRANSPARENCY_DRAGIMAGE = "transparency.dragimage"
fld public final static java.lang.String TRANSPARENCY_DRAGIMAGE_ALPHA = "transparency.dragimage.alpha"
fld public final static java.lang.String TRANSPARENCY_FLOATING = "transparency.floating"
fld public final static java.lang.String TRANSPARENCY_FLOATING_ALPHA = "transparency.floating.alpha"
fld public final static java.lang.String TRANSPARENCY_FLOATING_TIMEOUT = "transparency.floating.timeout"
fld public final static java.util.prefs.Preferences HANDLER

CLSS abstract interface org.netbeans.core.windows.options.package-info

CLSS public abstract interface org.netbeans.core.windows.view.ui.slides.SlideController
meth public abstract void userToggledAutoHide(int,boolean)
meth public abstract void userToggledTransparency(int)

CLSS public abstract org.netbeans.core.windows.view.ui.tabcontrol.AbstractTabbedImpl
cons public init()
meth protected abstract int dropIndexOfPoint(java.awt.Point)
meth protected abstract java.awt.Shape getDropIndication(org.openide.windows.TopComponent,java.awt.Point)
meth protected abstract javax.swing.SingleSelectionModel getSelectionModel()
meth protected abstract org.netbeans.swing.tabcontrol.ComponentConverter getComponentConverter()
meth protected abstract org.netbeans.swing.tabcontrol.TabDataModel getTabModel()
meth protected abstract void cancelRequestAttention(int)
meth protected abstract void requestAttention(int)
meth protected abstract void setAttentionHighlight(int,boolean)
meth protected final void fireStateChanged()
meth public boolean isBusy(org.openide.windows.TopComponent)
meth public final java.awt.Shape getIndicationForLocation(java.awt.Point,org.openide.windows.TopComponent,java.awt.Point,boolean)
meth public final java.lang.Object getConstraintForLocation(java.awt.Point,boolean)
meth public final javax.swing.Action[] getPopupActions(javax.swing.Action[],int)
meth public final org.openide.windows.TopComponent getSelectedTopComponent()
meth public final org.openide.windows.TopComponent getTopComponentAt(int)
meth public final org.openide.windows.TopComponent[] getTopComponents()
meth public final void addChangeListener(javax.swing.event.ChangeListener)
meth public final void addTopComponent(java.lang.String,javax.swing.Icon,org.openide.windows.TopComponent,java.lang.String)
meth public final void cancelRequestAttention(org.openide.windows.TopComponent)
meth public final void insertComponent(java.lang.String,javax.swing.Icon,java.awt.Component,java.lang.String,int)
meth public final void removeChangeListener(javax.swing.event.ChangeListener)
meth public final void removeComponent(java.awt.Component)
meth public final void requestAttention(org.openide.windows.TopComponent)
meth public final void setAttentionHighlight(org.openide.windows.TopComponent,boolean)
meth public final void setSelectedComponent(java.awt.Component)
meth public final void setTopComponents(org.openide.windows.TopComponent[],org.openide.windows.TopComponent)
supr org.netbeans.swing.tabcontrol.customtabs.Tabbed
hfds DEBUG,cs,tooltipListener,weakTooltipListener
hcls ToolTipListener

CLSS public org.netbeans.core.windows.view.ui.tabcontrol.JTabbedPaneAdapter
cons public init(org.netbeans.swing.tabcontrol.customtabs.TabbedType,org.netbeans.swing.tabcontrol.WinsysInfoForTabbedContainer)
intf org.netbeans.core.windows.view.ui.slides.SlideController
intf org.netbeans.swing.tabcontrol.customtabs.Tabbed$Accessor
meth public java.awt.Dimension getMinimumSize()
meth public java.awt.Shape getDropIndication(java.lang.Object,java.awt.Point)
meth public org.netbeans.swing.tabcontrol.customtabs.Tabbed getTabbed()
meth public void userToggledAutoHide(int,boolean)
meth public void userToggledTransparency(int)
supr org.netbeans.core.windows.view.ui.tabcontrol.tabbedpane.NBTabbedPane
hfds controller,tabbedImpl

CLSS public org.netbeans.core.windows.view.ui.tabcontrol.TabbedAdapter
cons public init(int,org.netbeans.swing.tabcontrol.WinsysInfoForTabbedContainer)
innr public static WinsysInfo
intf org.netbeans.core.windows.view.ui.slides.SlideController
intf org.netbeans.swing.tabcontrol.customtabs.Tabbed$Accessor
meth public org.netbeans.swing.tabcontrol.customtabs.Tabbed getTabbed()
meth public static boolean isInMaximizedMode(java.awt.Component)
meth public void addNotify()
meth public void removeNotify()
meth public void userToggledAutoHide(int,boolean)
meth public void userToggledTransparency(int)
supr org.netbeans.swing.tabcontrol.TabbedContainer
hfds tabbedImpl

CLSS public static org.netbeans.core.windows.view.ui.tabcontrol.TabbedAdapter$WinsysInfo
 outer org.netbeans.core.windows.view.ui.tabcontrol.TabbedAdapter
cons public init(int)
meth public boolean inMaximizedMode(java.awt.Component)
meth public boolean isModeSlidingEnabled()
meth public boolean isTopComponentBusy(org.openide.windows.TopComponent)
meth public boolean isTopComponentClosingEnabled()
meth public boolean isTopComponentClosingEnabled(org.openide.windows.TopComponent)
meth public boolean isTopComponentMaximizationEnabled()
meth public boolean isTopComponentMaximizationEnabled(org.openide.windows.TopComponent)
meth public boolean isTopComponentSlidingEnabled()
meth public boolean isTopComponentSlidingEnabled(org.openide.windows.TopComponent)
meth public java.lang.Object getOrientation(java.awt.Component)
supr org.netbeans.swing.tabcontrol.WinsysInfoForTabbedContainer
hfds containerType

CLSS public final org.netbeans.core.windows.view.ui.tabcontrol.Utilities
meth public static boolean isEditorTopComponentClosingEnabled()
meth public static void resetTabbedContainers()
supr java.lang.Object

CLSS public org.netbeans.core.windows.view.ui.tabcontrol.tabbedpane.NBTabbedPane
cons public init(org.netbeans.swing.tabcontrol.TabDataModel,org.netbeans.swing.tabcontrol.customtabs.TabbedType,org.netbeans.swing.tabcontrol.WinsysInfoForTabbedContainer)
fld protected final org.netbeans.swing.tabcontrol.customtabs.TabbedType type
meth protected final void postActionEvent(org.netbeans.swing.tabcontrol.event.TabActionEvent)
meth public final org.netbeans.swing.tabcontrol.ComponentConverter getComponentConverter()
meth public final org.netbeans.swing.tabcontrol.customtabs.TabbedType getType()
meth public final void addActionListener(java.awt.event.ActionListener)
meth public final void cancelRequestAttention(int)
meth public final void removeActionListener(java.awt.event.ActionListener)
meth public final void requestAttention(int)
meth public final void setActive(boolean)
meth public final void setAttentionHighlight(int,boolean)
meth public final void setComponentConverter(org.netbeans.swing.tabcontrol.ComponentConverter)
meth public int dropIndexOfPoint(java.awt.Point)
meth public int indexOf(java.awt.Component)
meth public int tabForCoordinate(java.awt.Point)
meth public java.awt.Image createImageOfTab(int)
meth public org.netbeans.swing.tabcontrol.TabDataModel getDataModel()
meth public org.netbeans.swing.tabcontrol.WinsysInfoForTabbedContainer getWinsysInfoForTabbedContainer()
meth public void startBlinking(int,java.awt.Color,java.awt.Color)
meth public void stopBlinking()
supr javax.swing.JTabbedPane
hfds _background,_foreground,_savedBackground,_savedForeground,_tabIndex,actionListenerList,active,blinks,converter,count,dataModel,timer,winsysInfo

CLSS public abstract org.netbeans.spi.options.OptionsPanelController
cons public init()
fld public final static java.lang.String PROP_CHANGED = "changed"
fld public final static java.lang.String PROP_HELP_CTX = "helpCtx"
fld public final static java.lang.String PROP_VALID = "valid"
innr public abstract interface static !annotation ContainerRegistration
innr public abstract interface static !annotation Keywords
innr public abstract interface static !annotation KeywordsRegistration
innr public abstract interface static !annotation SubRegistration
innr public abstract interface static !annotation TopLevelRegistration
meth protected void setCurrentSubcategory(java.lang.String)
meth public abstract boolean isChanged()
meth public abstract boolean isValid()
meth public abstract javax.swing.JComponent getComponent(org.openide.util.Lookup)
meth public abstract org.openide.util.HelpCtx getHelpCtx()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void applyChanges()
meth public abstract void cancel()
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void update()
meth public final static org.netbeans.spi.options.OptionsPanelController createAdvanced(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public final void setSubcategory(java.lang.String)
meth public org.openide.util.Lookup getLookup()
meth public void handleSuccessfulSearch(java.lang.String,java.util.List<java.lang.String>)
supr java.lang.Object

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

