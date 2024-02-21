#Signature file v4.1
#Version 1.53

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

CLSS public org.netbeans.modules.websvc.saas.codegen.Constants
cons public init()
fld public final static java.lang.String ARRAY_LIST_TYPE = "java.util.ArrayList"
fld public final static java.lang.String AUTH = "auth"
fld public final static java.lang.String CALLBACK = "callback"
fld public final static java.lang.String COLLECTIONS_TYPE = "java.util.Collections"
fld public final static java.lang.String COLLECTION_TYPE = "java.util.Collection"
fld public final static java.lang.String CONSUME_MIME = "javax.ws.rs.ConsumeMime"
fld public final static java.lang.String CONSUME_MIME_ANNOTATION = "ConsumeMime"
fld public final static java.lang.String CONTENT_TYPE = "Content-Type"
fld public final static java.lang.String DEFAULT_VALUE = "javax.ws.rs.DefaultValue"
fld public final static java.lang.String DEFAULT_VALUE_ANNOTATION = "DefaultValue"
fld public final static java.lang.String DELETE = "javax.ws.rs.DELETE"
fld public final static java.lang.String DELETE_ANNOTATION = "DELETE"
fld public final static java.lang.String ENTITY_MANAGER_FACTORY = "javax.persistence.EntityManagerFactory"
fld public final static java.lang.String ENTITY_MANAGER_TYPE = "javax.persistence.EntityManager"
fld public final static java.lang.String ENTITY_TRANSACTION = "javax.persistence.EntityTransaction"
fld public final static java.lang.String ENTITY_TYPE = "javax.ws.rs.Entity"
fld public final static java.lang.String GET = "javax.ws.rs.GET"
fld public final static java.lang.String GET_ANNOTATION = "GET"
fld public final static java.lang.String HEAD = "javax.ws.rs.HEAD"
fld public final static java.lang.String HEADER_PARAMS = "headerParams"
fld public final static java.lang.String HEAD_ANNOTATION = "HEAD"
fld public final static java.lang.String HTTP_CONTEXT = "javax.ws.rs.core.Context"
fld public final static java.lang.String HTTP_CONTEXT_ANNOTATION = "Context"
fld public final static java.lang.String HTTP_RESOURCE_ANNOTATION = "Resource"
fld public final static java.lang.String HTTP_RESPONSE = "javax.ws.rs.core.Response"
fld public final static java.lang.String HTTP_SERVLET_PACKAGE = "javax.servlet.http."
fld public final static java.lang.String HTTP_SERVLET_REQUEST_CLASS = "HttpServletRequest"
fld public final static java.lang.String HTTP_SERVLET_REQUEST_VARIABLE = "request"
fld public final static java.lang.String HTTP_SERVLET_RESPONSE_CLASS = "HttpServletResponse"
fld public final static java.lang.String HTTP_SERVLET_RESPONSE_VARIABLE = "response"
fld public final static java.lang.String JAVA_ANNOTATION_PACKAGE = "javax.annotation."
fld public final static java.lang.String JAVA_ANNOTATION_RESOURCE = "Resource"
fld public final static java.lang.String JAVA_EXT = "java"
fld public final static java.lang.String LOGIN = "login"
fld public final static java.lang.String NO_RESULT_EXCEPTION = "javax.persistence.NoResultException"
fld public final static java.lang.String PASSWORD = "password"
fld public final static java.lang.String PATH = "javax.ws.rs.Path"
fld public final static java.lang.String PATH_ANNOTATION = "Path"
fld public final static java.lang.String PATH_PARAMS = "pathParams"
fld public final static java.lang.String PERSISTENCE = "javax.persistence.Persistence"
fld public final static java.lang.String PHP_EXT = "php"
fld public final static java.lang.String POST = "javax.ws.rs.POST"
fld public final static java.lang.String POST_ANNOTATION = "POST"
fld public final static java.lang.String PRODUCE_MIME = "javax.ws.rs.ProduceMime"
fld public final static java.lang.String PRODUCE_MIME_ANNOTATION = "ProduceMime"
fld public final static java.lang.String PUT = "javax.ws.rs.PUT"
fld public final static java.lang.String PUT_ANNOTATION = "PUT"
fld public final static java.lang.String PUT_POST_CONTENT = "content"
fld public final static java.lang.String QUERY_PARAM = "javax.ws.rs.QueryParam"
fld public final static java.lang.String QUERY_PARAMS = "queryParams"
fld public final static java.lang.String QUERY_PARAM_ANNOTATION = "QueryParam"
fld public final static java.lang.String QUERY_TYPE = "javax.persistence.Query"
fld public final static java.lang.String RESPONSE_BUILDER = "javax.ws.rs.core.Response.Builder"
fld public final static java.lang.String REST_API_PACKAGE = "javax.ws.rs."
fld public final static java.lang.String REST_CONNECTION = "RestConnection"
fld public final static java.lang.String REST_JMAKI_DIR = "resources"
fld public final static java.lang.String REST_STUBS_DIR = "rest"
fld public final static java.lang.String SERVICE_AUTHENTICATOR = "Authenticator"
fld public final static java.lang.String SERVICE_AUTHORIZATION_FRAME = "AuthorizationFrame"
fld public final static java.lang.String UNSUPPORTED_DROP = "WARN_UnsupportedDropTarget"
fld public final static java.lang.String URI_INFO = "javax.ws.rs.core.UriInfo"
fld public final static java.lang.String URI_PARAM = "javax.ws.rs.PathParam"
fld public final static java.lang.String URI_PARAM_ANNOTATION = "PathParam"
fld public final static java.lang.String URI_TYPE = "java.net.URI"
fld public final static java.lang.String VOID = "void"
fld public final static java.lang.String WEB_APPLICATION_EXCEPTION = "javax.ws.rs.WebApplicationException"
fld public final static java.lang.String XML_ATTRIBUTE = "javax.xml.bind.annotation.XmlAttribute"
fld public final static java.lang.String XML_ATTRIBUTE_ANNOTATION = "XmlAttribute"
fld public final static java.lang.String XML_ELEMENT = "javax.xml.bind.annotation.XmlElement"
fld public final static java.lang.String XML_ELEMENT_ANNOTATION = "XmlElement"
fld public final static java.lang.String XML_ROOT_ELEMENT = "javax.xml.bind.annotation.XmlRootElement"
fld public final static java.lang.String XML_ROOT_ELEMENT_ANNOTATION = "XmlRootElement"
fld public final static java.lang.String XML_TRANSIENT = "javax.xml.bind.annotation.XmlTransient"
fld public final static java.lang.String XML_TRANSIENT_ANNOTATION = "XmlTransient"
innr public final static !enum DropFileType
innr public final static !enum HttpMethodType
innr public final static !enum MimeType
innr public final static !enum SaasAuthenticationType
supr java.lang.Object

CLSS public final static !enum org.netbeans.modules.websvc.saas.codegen.Constants$DropFileType
 outer org.netbeans.modules.websvc.saas.codegen.Constants
fld public final static org.netbeans.modules.websvc.saas.codegen.Constants$DropFileType JAVA_CLIENT
fld public final static org.netbeans.modules.websvc.saas.codegen.Constants$DropFileType JSP
fld public final static org.netbeans.modules.websvc.saas.codegen.Constants$DropFileType PHP
fld public final static org.netbeans.modules.websvc.saas.codegen.Constants$DropFileType RESOURCE
fld public final static org.netbeans.modules.websvc.saas.codegen.Constants$DropFileType SERVLET
meth public java.lang.String getPrintWriterType()
meth public java.lang.String prefix()
meth public java.lang.String value()
meth public static org.netbeans.modules.websvc.saas.codegen.Constants$DropFileType valueOf(java.lang.String)
meth public static org.netbeans.modules.websvc.saas.codegen.Constants$DropFileType[] values()
supr java.lang.Enum<org.netbeans.modules.websvc.saas.codegen.Constants$DropFileType>
hfds prefix,printWriterType

CLSS public final static !enum org.netbeans.modules.websvc.saas.codegen.Constants$HttpMethodType
 outer org.netbeans.modules.websvc.saas.codegen.Constants
fld public final static org.netbeans.modules.websvc.saas.codegen.Constants$HttpMethodType DELETE
fld public final static org.netbeans.modules.websvc.saas.codegen.Constants$HttpMethodType GET
fld public final static org.netbeans.modules.websvc.saas.codegen.Constants$HttpMethodType HEAD
fld public final static org.netbeans.modules.websvc.saas.codegen.Constants$HttpMethodType POST
fld public final static org.netbeans.modules.websvc.saas.codegen.Constants$HttpMethodType PUT
meth public java.lang.String getAnnotationType()
meth public java.lang.String prefix()
meth public java.lang.String value()
meth public static org.netbeans.modules.websvc.saas.codegen.Constants$HttpMethodType valueOf(java.lang.String)
meth public static org.netbeans.modules.websvc.saas.codegen.Constants$HttpMethodType[] values()
supr java.lang.Enum<org.netbeans.modules.websvc.saas.codegen.Constants$HttpMethodType>
hfds annotationType,prefix

CLSS public final static !enum org.netbeans.modules.websvc.saas.codegen.Constants$MimeType
 outer org.netbeans.modules.websvc.saas.codegen.Constants
fld public final static org.netbeans.modules.websvc.saas.codegen.Constants$MimeType HTML
fld public final static org.netbeans.modules.websvc.saas.codegen.Constants$MimeType JSON
fld public final static org.netbeans.modules.websvc.saas.codegen.Constants$MimeType TEXT
fld public final static org.netbeans.modules.websvc.saas.codegen.Constants$MimeType XML
meth public java.lang.String suffix()
meth public java.lang.String toString()
meth public java.lang.String value()
meth public static org.netbeans.modules.websvc.saas.codegen.Constants$MimeType find(java.lang.String)
meth public static org.netbeans.modules.websvc.saas.codegen.Constants$MimeType valueOf(java.lang.String)
meth public static org.netbeans.modules.websvc.saas.codegen.Constants$MimeType[] values()
supr java.lang.Enum<org.netbeans.modules.websvc.saas.codegen.Constants$MimeType>
hfds suffix,value

CLSS public final static !enum org.netbeans.modules.websvc.saas.codegen.Constants$SaasAuthenticationType
 outer org.netbeans.modules.websvc.saas.codegen.Constants
fld public final static org.netbeans.modules.websvc.saas.codegen.Constants$SaasAuthenticationType API_KEY
fld public final static org.netbeans.modules.websvc.saas.codegen.Constants$SaasAuthenticationType CUSTOM
fld public final static org.netbeans.modules.websvc.saas.codegen.Constants$SaasAuthenticationType HTTP_BASIC
fld public final static org.netbeans.modules.websvc.saas.codegen.Constants$SaasAuthenticationType PLAIN
fld public final static org.netbeans.modules.websvc.saas.codegen.Constants$SaasAuthenticationType SESSION_KEY
fld public final static org.netbeans.modules.websvc.saas.codegen.Constants$SaasAuthenticationType SIGNED_URL
meth public java.lang.String getClassIdentifier()
meth public java.lang.String value()
meth public static org.netbeans.modules.websvc.saas.codegen.Constants$SaasAuthenticationType valueOf(java.lang.String)
meth public static org.netbeans.modules.websvc.saas.codegen.Constants$SaasAuthenticationType[] values()
supr java.lang.Enum<org.netbeans.modules.websvc.saas.codegen.Constants$SaasAuthenticationType>
hfds classId,value

CLSS public abstract org.netbeans.modules.websvc.saas.codegen.SaasClientAuthenticationGenerator
cons public init(org.netbeans.modules.websvc.saas.codegen.model.SaasBean,org.netbeans.api.project.Project)
meth public abstract java.lang.String getLoginBody(org.netbeans.modules.websvc.saas.codegen.model.SaasBean,java.lang.String,java.lang.String) throws java.io.IOException
meth public abstract java.lang.String getLogoutBody()
meth public abstract java.lang.String getPostAuthenticationCode()
meth public abstract java.lang.String getPreAuthenticationCode()
meth public abstract java.lang.String getSignParamUsage(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>,java.lang.String)
meth public abstract java.lang.String getTokenBody(org.netbeans.modules.websvc.saas.codegen.model.SaasBean,java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
meth public abstract void createAuthenticatorClass() throws java.io.IOException
meth public abstract void createAuthorizationClasses() throws java.io.IOException
meth public abstract void modifyAuthenticationClass() throws java.io.IOException
meth public abstract void modifyAuthenticationClass(java.lang.String,java.lang.Object[],java.lang.Object,java.lang.String,java.lang.String[],java.lang.Object[],java.lang.Object[],java.lang.String) throws java.io.IOException
meth public java.lang.String getAuthenticationProfile()
meth public java.lang.String getLoginArguments()
meth public java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> getAuthenticatorMethodParameters()
meth public org.netbeans.api.project.Project getProject()
meth public org.netbeans.modules.websvc.saas.codegen.Constants$DropFileType getDropFileType()
meth public org.netbeans.modules.websvc.saas.codegen.Constants$SaasAuthenticationType getAuthenticationType() throws java.io.IOException
meth public org.netbeans.modules.websvc.saas.codegen.model.SaasBean getBean()
meth public org.openide.filesystems.FileObject getSaasServiceFolder() throws java.io.IOException
meth public void setAuthenticationProfile(java.lang.String)
meth public void setAuthenticatorMethodParameters(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth public void setDropFileType(org.netbeans.modules.websvc.saas.codegen.Constants$DropFileType)
meth public void setLoginArguments(java.lang.String)
meth public void setSaasServiceFolder(org.openide.filesystems.FileObject) throws java.io.IOException
supr java.lang.Object
hfds authMethodParams,authProfile,bean,dropFileType,loginArgs,project,serviceFolder

CLSS public abstract org.netbeans.modules.websvc.saas.codegen.SaasClientCodeGenerationManager
cons public init()
fld public static java.util.Collection<? extends org.netbeans.modules.websvc.saas.codegen.spi.SaasClientCodeGenerationProvider> providers
meth public static boolean canAccept(org.netbeans.modules.websvc.saas.model.SaasMethod,javax.swing.text.Document)
meth public static org.netbeans.modules.websvc.saas.codegen.spi.SaasClientCodeGenerationProvider lookup(org.netbeans.modules.websvc.saas.model.SaasMethod,javax.swing.text.Document)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.websvc.saas.codegen.SaasClientCodeGenerator
cons public init()
fld public final static java.lang.String COMMENT_END_OF_HTTP_MEHTOD_GET = "TODO return proper representation object"
fld public final static java.lang.String CONVERTER_FOLDER = "converter"
fld public final static java.lang.String CONVERTER_SUFFIX = "Converter"
fld public final static java.lang.String GENERIC_REF_CONVERTER = "GenericRefConverter"
fld public final static java.lang.String GENERIC_REF_CONVERTER_TEMPLATE = "Templates/SaaSServices/RefConverter.java"
fld public final static java.lang.String INDENT = "        "
fld public final static java.lang.String INDENT_2 = "             "
fld public final static java.lang.String RESOURCE_SUFFIX = "Resource"
fld public final static java.lang.String REST_CONNECTION = "RestConnection"
fld public final static java.lang.String REST_CONNECTION_PACKAGE = "org.netbeans.saas"
fld public final static java.lang.String REST_CONNECTION_TEMPLATE = "Templates/SaaSServices/RestConnection.java"
fld public final static java.lang.String REST_RESPONSE = "RestResponse"
fld public final static java.lang.String REST_RESPONSE_TEMPLATE = "Templates/SaaSServices/RestResponse.java"
fld public final static java.lang.String SAAS_SERVICES = "SaaSServices"
fld public final static java.lang.String TEMPLATES_SAAS = "Templates/SaaSServices/"
fld public final static java.lang.String VAR_NAMES_RESULT_DECL = "RestResponse result"
intf org.netbeans.modules.websvc.saas.codegen.spi.SaasClientCodeGenerationProvider
meth protected abstract java.lang.String getCustomMethodBody() throws java.io.IOException
meth protected boolean isInBlock(javax.swing.text.Document)
meth protected int insert(java.lang.String,int,int,javax.swing.text.Document,boolean) throws javax.swing.text.BadLocationException
meth protected java.lang.Object[] getParamValues(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth protected java.lang.String findNewName(java.lang.String,java.lang.String)
meth protected java.lang.String getParameterName(org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo)
meth protected java.lang.String getParameterName(org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo,boolean,boolean)
meth protected java.lang.String getParameterName(org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo,boolean,boolean,boolean)
meth protected java.lang.String getResultPattern()
meth protected java.lang.String getVariableName(java.lang.String)
meth protected java.lang.String getVariableName(java.lang.String,boolean,boolean,boolean)
meth protected java.lang.String[] getGetParamNames(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth protected java.lang.String[] getGetParamTypes(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth protected java.lang.String[] getParamNames(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth protected java.lang.String[] getParamTypeNames(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth protected java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> renameParameterNames(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth protected javax.swing.text.Document getTargetDocument()
meth protected org.netbeans.api.progress.ProgressHandle getProgressHandle()
meth protected org.netbeans.api.project.Project getProject()
meth protected org.openide.filesystems.FileObject getTargetFile()
meth protected org.openide.filesystems.FileObject getTargetFolder()
meth protected void addVariablePattern(java.lang.String,int)
meth protected void clearVariablePatterns()
meth protected void insert(java.lang.String,boolean) throws javax.swing.text.BadLocationException
meth protected void preGenerate() throws java.io.IOException
meth protected void reformat(javax.swing.text.Document,int,int) throws javax.swing.text.BadLocationException
meth protected void updateVariableNames(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth protected void updateVariableNamesForWS(java.util.List<? extends org.netbeans.modules.websvc.jaxwsmodelapi.WSParameter>)
meth public abstract boolean canAccept(org.netbeans.modules.websvc.saas.model.SaasMethod,javax.swing.text.Document)
meth public int getEndPosition()
meth public int getPrecedence()
meth public int getStartPosition()
meth public int getTotalWorkUnits()
meth public java.lang.String getVariableDecl(org.netbeans.modules.websvc.jaxwsmodelapi.WSParameter)
meth public java.lang.String getVariableDecl(org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo)
meth public java.util.Set<org.openide.filesystems.FileObject> generate() throws java.io.IOException
meth public org.netbeans.modules.websvc.saas.codegen.Constants$DropFileType getDropFileType()
meth public org.netbeans.modules.websvc.saas.codegen.model.SaasBean getBean()
meth public void copyFile(java.lang.String,java.io.File) throws java.io.IOException
meth public void finishProgressReporting()
meth public void init(org.netbeans.modules.websvc.saas.model.SaasMethod,javax.swing.text.Document) throws java.io.IOException
meth public void initProgressReporting(org.netbeans.api.progress.ProgressHandle)
meth public void initProgressReporting(org.netbeans.api.progress.ProgressHandle,boolean)
meth public void reportProgress(java.lang.String)
meth public void setBean(org.netbeans.modules.websvc.saas.codegen.model.SaasBean)
meth public void setDropFileType(org.netbeans.modules.websvc.saas.codegen.Constants$DropFileType)
meth public void setDropLocation(javax.swing.text.JTextComponent)
meth public void setEndPosition(int)
meth public void setPrecedence(int)
meth public void setStartPosition(int)
supr java.lang.Object
hfds bean,destDir,dropFileType,end,nFinder,pHandle,precedence,project,start,targetDocument,targetFile,totalWorkUnits,workUnits

CLSS public org.netbeans.modules.websvc.saas.codegen.model.CustomClientSaasBean
cons public init(org.netbeans.modules.websvc.saas.model.CustomSaasMethod) throws java.io.IOException
cons public init(org.netbeans.modules.websvc.saas.model.CustomSaasMethod,boolean) throws java.io.IOException
fld public final static java.lang.String ARTIFACT_TYPE_LIB = "lib"
fld public final static java.lang.String ARTIFACT_TYPE_TEMPLATE = "template"
meth protected java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> initInputParameters()
meth protected static java.lang.String deriveResourceName(org.netbeans.modules.websvc.saas.model.CustomSaasMethod)
meth protected static java.lang.String deriveUriTemplate(org.netbeans.modules.websvc.saas.model.CustomSaasMethod)
meth public boolean canGenerateJAXBUnmarshaller()
meth public java.lang.String getSaasServiceMethodName()
meth public java.lang.String getUrl()
meth public java.util.Map<java.lang.String,java.lang.String> getArtifactLibs()
meth public java.util.Map<java.lang.String,java.lang.String> getArtifactTemplates(java.lang.String)
meth public org.netbeans.modules.websvc.saas.model.CustomSaasMethod getMethod()
meth public void addArtifactTemplates(java.lang.String,java.util.Map<java.lang.String,java.lang.String>)
meth public void setArtifactLibs(java.util.Map<java.lang.String,java.lang.String>)
meth public void setCanGenerateJAXBUnmarshaller(boolean)
supr org.netbeans.modules.websvc.saas.codegen.model.SaasBean
hfds canGenerateJaxb,libs,m,serviceMethodName,templates,url

CLSS public org.netbeans.modules.websvc.saas.codegen.model.GenericResourceBean
cons public init(java.lang.String,java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.websvc.saas.codegen.Constants$MimeType[],java.lang.String[],org.netbeans.modules.websvc.saas.codegen.Constants$HttpMethodType[])
cons public init(java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.websvc.saas.codegen.Constants$MimeType[],org.netbeans.modules.websvc.saas.codegen.Constants$HttpMethodType[])
fld public final static java.lang.String RESOURCE_SUFFIX = "Resource"
fld public final static org.netbeans.modules.websvc.saas.codegen.Constants$HttpMethodType[] CLIENT_CONTROL_CONTAINER_METHODS
fld public final static org.netbeans.modules.websvc.saas.codegen.Constants$HttpMethodType[] CONTAINER_METHODS
fld public final static org.netbeans.modules.websvc.saas.codegen.Constants$HttpMethodType[] ITEM_METHODS
fld public final static org.netbeans.modules.websvc.saas.codegen.Constants$HttpMethodType[] STAND_ALONE_METHODS
fld public final static org.netbeans.modules.websvc.saas.codegen.Constants$MimeType[] supportedMimeTypes
meth public boolean isGenerateUriTemplate()
meth public boolean isPrivateFieldForQueryParam()
meth public java.lang.String getName()
meth public java.lang.String getPackageName()
meth public java.lang.String getQualifiedClassName()
meth public java.lang.String getShortName()
meth public java.lang.String getUriTemplate()
meth public java.lang.String getUriWhenUsedAsSubResource()
meth public java.lang.String[] getRepresentationTypes()
meth public java.lang.String[] getUriParams()
meth public java.util.List<org.netbeans.modules.websvc.saas.codegen.model.GenericResourceBean> getSubResources()
meth public java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> filterParameters(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>,org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo$ParamFilter[])
meth public java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> filterParameters(org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo$ParamFilter[])
meth public java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> filterParametersByAuth(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth public java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> getInputParameters()
meth public java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> getQueryParameters()
meth public java.util.Set<org.netbeans.modules.websvc.saas.codegen.Constants$HttpMethodType> getMethodTypes()
meth public org.netbeans.modules.websvc.saas.codegen.Constants$HttpMethodType getHttpMethod()
meth public org.netbeans.modules.websvc.saas.codegen.Constants$MimeType[] getMimeTypes()
meth public org.netbeans.modules.websvc.saas.codegen.Constants$SaasAuthenticationType getAuthenticationType()
meth public org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication getAuthentication()
meth public static java.lang.String getDefaultRepresetationClass(org.netbeans.modules.websvc.saas.codegen.Constants$MimeType)
meth public static java.lang.String getGetMethodName(org.netbeans.modules.websvc.saas.codegen.Constants$MimeType)
meth public static java.lang.String getHttpMethodName(org.netbeans.modules.websvc.saas.codegen.Constants$HttpMethodType,org.netbeans.modules.websvc.saas.codegen.Constants$MimeType)
meth public static java.lang.String getShortName(java.lang.String)
meth public static java.lang.String[] getUriParams(java.lang.String)
meth public static org.netbeans.modules.websvc.saas.codegen.Constants$MimeType[] getSupportedMimeTypes()
meth public void addSubResource(org.netbeans.modules.websvc.saas.codegen.model.GenericResourceBean)
meth public void setGenerateUriTemplate(boolean)
meth public void setHttpMethod(org.netbeans.modules.websvc.saas.codegen.Constants$HttpMethodType)
meth public void setMethodTypes(org.netbeans.modules.websvc.saas.codegen.Constants$HttpMethodType[])
meth public void setMimeTypes(org.netbeans.modules.websvc.saas.codegen.Constants$MimeType[])
meth public void setName(java.lang.String)
meth public void setPackageName(java.lang.String)
meth public void setPrivateFieldForQueryParam(boolean)
meth public void setUriTemplate(java.lang.String)
supr java.lang.Object
hfds generateUriTemplate,httpMethod,methodTypes,mimeTypes,name,packageName,privateFieldForQueryParam,representationTypes,subResources,uriParams,uriTemplate

CLSS public org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo
cons public init(java.lang.String,java.lang.Class)
cons public init(java.lang.String,java.lang.Class,java.lang.String)
cons public init(javax.xml.namespace.QName,java.lang.Class,java.lang.String)
innr public final static !enum ParamFilter
innr public final static !enum ParamStyle
meth public boolean isApiKey()
meth public boolean isFixed()
meth public boolean isRepeating()
meth public boolean isRequired()
meth public boolean isSessionKey()
meth public java.lang.Class getType()
meth public java.lang.Object getDefaultValue()
meth public java.lang.String getFixed()
meth public java.lang.String getId()
meth public java.lang.String getName()
meth public java.lang.String getSimpleTypeName()
meth public java.lang.String getTypeName()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Option> getOption()
meth public javax.xml.namespace.QName getQName()
meth public org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo$ParamStyle getStyle()
meth public void setDefaultValue(java.lang.Object)
meth public void setFixed(java.lang.String)
meth public void setId(java.lang.String)
meth public void setIsApiKey(boolean)
meth public void setIsRepeating(boolean)
meth public void setIsRequired(boolean)
meth public void setIsSessionKey(boolean)
meth public void setOption(java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Option>)
meth public void setStyle(org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo$ParamStyle)
supr java.lang.Object
hfds defaultValue,fixed,id,isApiKey,isSessionKey,name,option,qname,repeating,required,style,type,typeName

CLSS public final static !enum org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo$ParamFilter
 outer org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo
fld public final static org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo$ParamFilter FIXED
meth public java.lang.String value()
meth public static org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo$ParamFilter valueOf(java.lang.String)
meth public static org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo$ParamFilter[] values()
meth public static org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo$ParamStyle fromValue(java.lang.String)
supr java.lang.Enum<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo$ParamFilter>
hfds value

CLSS public final static !enum org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo$ParamStyle
 outer org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo
fld public final static org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo$ParamStyle HEADER
fld public final static org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo$ParamStyle MATRIX
fld public final static org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo$ParamStyle PLAIN
fld public final static org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo$ParamStyle QUERY
fld public final static org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo$ParamStyle TEMPLATE
fld public final static org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo$ParamStyle UNKNOWN
meth public java.lang.String value()
meth public static org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo$ParamStyle fromValue(java.lang.String)
meth public static org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo$ParamStyle valueOf(java.lang.String)
meth public static org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo$ParamStyle[] values()
supr java.lang.Enum<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo$ParamStyle>
hfds value

CLSS public org.netbeans.modules.websvc.saas.codegen.model.RestClientSaasBean
cons public init(org.netbeans.modules.websvc.saas.model.WadlSaasMethod) throws java.io.IOException
cons public init(org.netbeans.modules.websvc.saas.model.WadlSaasMethod,boolean) throws java.io.IOException
fld public final static java.lang.String PROTOCOL_SEPERATOR = "://"
fld public final static java.lang.String PROTOCOL_SEPERATOR_ALT = "  "
fld public final static java.lang.String SAAS_SERVICE_TEMPLATE = "Templates/SaaSServices/SaasService"
meth protected java.lang.Object getSessionKey(org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication)
meth protected java.lang.Object getSignedUrl(org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication)
meth protected java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> initInputParameters()
meth public boolean canGenerateJAXBUnmarshaller()
meth public java.lang.String getSaasServiceMethodName()
meth public java.lang.String getSaasServiceTemplate()
meth public java.lang.String getUrl()
meth public java.util.ArrayList<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> findWadlParams(org.netbeans.modules.websvc.saas.model.WadlSaasMethod)
meth public java.util.ArrayList<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> findWadlParams(org.netbeans.modules.websvc.saas.model.wadl.Method)
meth public org.netbeans.modules.websvc.saas.model.WadlSaasMethod getMethod()
meth public static java.util.List<javax.xml.namespace.QName> findRepresentationTypes(org.netbeans.modules.websvc.saas.model.WadlSaasMethod)
meth public static java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Representation> findInputRepresentations(org.netbeans.modules.websvc.saas.model.WadlSaasMethod)
meth public static void findMediaType(org.netbeans.modules.websvc.saas.model.wadl.Request,java.util.List<java.lang.String>)
meth public static void findMediaType(org.netbeans.modules.websvc.saas.model.wadl.Response,java.util.List<org.netbeans.modules.websvc.saas.codegen.Constants$MimeType>)
meth public static void findRepresentationType(org.netbeans.modules.websvc.saas.model.wadl.Response,java.util.List<javax.xml.namespace.QName>)
meth public static void findWadlParams(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>,java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Param>)
supr org.netbeans.modules.websvc.saas.codegen.model.SaasBean
hfds m,serviceMethodName,url

CLSS public abstract org.netbeans.modules.websvc.saas.codegen.model.SaasBean
cons public init(org.netbeans.modules.websvc.saas.model.Saas,java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.websvc.saas.codegen.Constants$MimeType[],java.lang.String[],org.netbeans.modules.websvc.saas.codegen.Constants$HttpMethodType[])
fld public final static java.lang.String RESOURCE_TEMPLATE = "Templates/SaaSServices/WrapperResource.java"
innr public ApiKeyAuthentication
innr public CustomAuthentication
innr public HttpBasicAuthentication
innr public SaasAuthentication
innr public SessionKeyAuthentication
innr public SignedUrlAuthentication
innr public Time
meth protected abstract java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> initInputParameters()
meth protected java.lang.Object getSessionKey(org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication)
meth protected java.lang.Object getSignedUrl(org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication)
meth protected void setInputParameters(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth protected void setResourceClassTemplate(java.lang.String)
meth protected void setTemplateParameters(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth public boolean isDropTargetWeb()
meth public boolean isUseTemplates()
meth public java.lang.String getAuthenticatorClassName()
meth public java.lang.String getAuthorizationFrameClassName()
meth public java.lang.String getDisplayName()
meth public java.lang.String getGroupName()
meth public java.lang.String getResourceClassTemplate()
meth public java.lang.String getSaasName()
meth public java.lang.String getSaasServiceName()
meth public java.lang.String getSaasServicePackageName()
meth public java.lang.String[] getOutputTypes()
meth public java.lang.String[] getRepresentationTypes()
meth public java.lang.String[] getUriParams()
meth public java.util.List<java.lang.String> getOutputWrapperNames()
meth public java.util.List<java.lang.String> getOutputWrapperPackageNames()
meth public java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> getHeaderParameters()
meth public java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> getInputParameters()
meth public java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> getQueryParameters()
meth public java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> getTemplateParameters()
meth public org.netbeans.modules.websvc.saas.codegen.Constants$SaasAuthenticationType getAuthenticationType()
meth public org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication getAuthentication()
meth public org.netbeans.modules.websvc.saas.model.Saas getSaas()
meth public static boolean canAccept(org.netbeans.modules.websvc.saas.model.SaasMethod,java.lang.Class,org.netbeans.modules.websvc.saas.codegen.Constants$DropFileType)
meth public static java.lang.Class findJavaType(java.lang.String)
meth public static java.lang.String getProfile(org.netbeans.modules.websvc.saas.model.SaasMethod,org.netbeans.modules.websvc.saas.codegen.Constants$DropFileType)
meth public void addOutputWrapperName(java.lang.String)
meth public void addOutputWrapperPackageName(java.lang.String)
meth public void findAuthentication(org.netbeans.modules.websvc.saas.model.SaasMethod) throws java.io.IOException
meth public void findSaasMediaType(java.util.List<org.netbeans.modules.websvc.saas.codegen.Constants$MimeType>,org.netbeans.modules.websvc.saas.model.jaxb.Method$Output$Media)
meth public void findSaasParams(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>,java.util.List<org.netbeans.modules.websvc.saas.model.jaxb.Params$Param>)
meth public void setAuthentication(org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication)
meth public void setAuthenticationType(org.netbeans.modules.websvc.saas.codegen.Constants$SaasAuthenticationType)
meth public void setIsDropTargetWeb(boolean)
supr org.netbeans.modules.websvc.saas.codegen.model.GenericResourceBean
hfds auth,authType,displayName,groupName,headerParams,inputParams,isDropTargetWeb,outputWrapperNames,queryParams,resourceTemplate,saas,templateParams,wrapperPackageNames

CLSS public org.netbeans.modules.websvc.saas.codegen.model.SaasBean$ApiKeyAuthentication
 outer org.netbeans.modules.websvc.saas.codegen.model.SaasBean
cons public init(org.netbeans.modules.websvc.saas.codegen.model.SaasBean,java.lang.String)
meth public java.lang.String getApiKeyName()
supr org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication
hfds keyName

CLSS public org.netbeans.modules.websvc.saas.codegen.model.SaasBean$CustomAuthentication
 outer org.netbeans.modules.websvc.saas.codegen.model.SaasBean
cons public init(org.netbeans.modules.websvc.saas.codegen.model.SaasBean)
supr org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication

CLSS public org.netbeans.modules.websvc.saas.codegen.model.SaasBean$HttpBasicAuthentication
 outer org.netbeans.modules.websvc.saas.codegen.model.SaasBean
cons public init(org.netbeans.modules.websvc.saas.codegen.model.SaasBean,java.lang.String,java.lang.String)
meth public java.lang.String getPasswordId()
meth public java.lang.String getUserNameId()
meth public org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator getUseGenerator()
meth public org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseTemplates getUseTemplates()
meth public void setUseGenerator(org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator)
meth public void setUseTemplates(org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseTemplates)
supr org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication
hfds password,useGenerator,useTemplates,username

CLSS public org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication
 outer org.netbeans.modules.websvc.saas.codegen.model.SaasBean
cons public init(org.netbeans.modules.websvc.saas.codegen.model.SaasBean)
innr public UseGenerator
innr public UseTemplates
meth public org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator createUseGenerator()
meth public org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseTemplates createUseTemplates()
supr java.lang.Object

CLSS public org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator
 outer org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication
cons public init(org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication)
innr public Login
innr public Logout
innr public Method
innr public Token
meth public org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator$Login createLogin()
meth public org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator$Login getLogin()
meth public org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator$Logout createLogout()
meth public org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator$Logout getLogout()
meth public org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator$Method createMethod()
meth public org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator$Token createToken(java.lang.String)
meth public org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator$Token getToken()
meth public void setLogin(org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator$Login)
meth public void setLogout(org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator$Logout)
meth public void setToken(org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator$Token)
supr java.lang.Object
hfds login,logout,token

CLSS public org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator$Login
 outer org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator
cons public init(org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator)
meth public java.lang.String getSignId()
meth public java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> getParameters()
meth public org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator$Method getMethod()
meth public void setMethod(org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator$Method)
meth public void setParameters(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth public void setSignId(java.lang.String)
supr java.lang.Object
hfds method,params,signId

CLSS public org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator$Logout
 outer org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator
cons public init(org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator)
supr org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator$Login

CLSS public org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator$Method
 outer org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator
cons public init(org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator)
meth public java.lang.String getHref()
meth public java.lang.String getId()
meth public java.lang.String getName()
meth public void setHref(java.lang.String)
meth public void setId(java.lang.String)
meth public void setName(java.lang.String)
supr java.lang.Object
hfds href,id,name

CLSS public org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator$Token
 outer org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator
cons public init(org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator,java.lang.String)
innr public Prompt
meth public java.lang.String getId()
meth public org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator$Token$Prompt getPrompt()
meth public void setPrompt(org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator$Token$Prompt)
supr org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator$Login
hfds id,prompt

CLSS public org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator$Token$Prompt
 outer org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator$Token
cons public init(org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator$Token)
meth public java.lang.String getDesktopUrl()
meth public java.lang.String getSignId()
meth public java.lang.String getWebUrl()
meth public java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> getParameters()
meth public void setDesktopUrl(java.lang.String)
meth public void setParameters(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth public void setSignId(java.lang.String)
meth public void setWebUrl(java.lang.String)
supr java.lang.Object
hfds deskTopUrl,params,signId,webUrl

CLSS public org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseTemplates
 outer org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication
cons public init(org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication)
innr public Template
meth public java.util.List<org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseTemplates$Template> getTemplates()
meth public org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseTemplates$Template createTemplate(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public void setTemplates(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseTemplates$Template>)
supr java.lang.Object
hfds templates

CLSS public org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseTemplates$Template
 outer org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseTemplates
cons public init(org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseTemplates,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public java.lang.String getId()
meth public java.lang.String getType()
meth public java.lang.String getUrl()
meth public java.util.List<java.lang.String> getDropTypeList()
supr java.lang.Object
hfds dropTypeList,id,type,url

CLSS public org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SessionKeyAuthentication
 outer org.netbeans.modules.websvc.saas.codegen.model.SaasBean
cons public init(org.netbeans.modules.websvc.saas.codegen.model.SaasBean,java.lang.String,java.lang.String,java.lang.String)
meth public java.lang.String getApiKeyName()
meth public java.lang.String getSessionKeyName()
meth public java.lang.String getSigKeyName()
meth public java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> getParameters()
meth public org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator getUseGenerator()
meth public org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseTemplates getUseTemplates()
meth public void setParameters(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth public void setUseGenerator(org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator)
meth public void setUseTemplates(org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseTemplates)
supr org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication
hfds apiId,params,sessionId,sig,useGenerator,useTemplates

CLSS public org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SignedUrlAuthentication
 outer org.netbeans.modules.websvc.saas.codegen.model.SaasBean
cons public init(org.netbeans.modules.websvc.saas.codegen.model.SaasBean)
meth public java.lang.String getSigKeyName()
meth public java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> getParameters()
meth public void setParameters(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth public void setSigKeyName(java.lang.String)
supr org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication
hfds params,sig

CLSS public org.netbeans.modules.websvc.saas.codegen.model.SaasBean$Time
 outer org.netbeans.modules.websvc.saas.codegen.model.SaasBean
cons public init(org.netbeans.modules.websvc.saas.codegen.model.SaasBean)
supr java.lang.Object

CLSS public org.netbeans.modules.websvc.saas.codegen.model.SoapClientOperationInfo
cons public init(org.netbeans.modules.websvc.saas.model.WsdlSaasMethod,org.netbeans.api.project.Project)
meth public boolean isRPCEncoded()
meth public boolean needsSoapHandler()
meth public java.lang.Class getType(org.netbeans.api.project.Project,java.lang.String)
meth public java.lang.Class[] getInputParameterTypes()
meth public java.lang.String getCategoryName()
meth public java.lang.String getOperationName()
meth public java.lang.String getOutputType()
meth public java.lang.String getPortName()
meth public java.lang.String getServiceName()
meth public java.lang.String getWsdlLocation()
meth public java.lang.String getWsdlURL()
meth public java.lang.String[] getInputParameterNames()
meth public java.util.List<org.netbeans.modules.websvc.jaxwsmodelapi.WSParameter> getOutputParameters()
meth public java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> getSoapHeaderParameters()
meth public org.netbeans.api.project.Project getProject()
meth public org.netbeans.modules.websvc.jaxwsmodelapi.WSOperation getOperation()
meth public org.netbeans.modules.websvc.jaxwsmodelapi.WSPort getPort()
meth public org.netbeans.modules.websvc.jaxwsmodelapi.WSService getService()
meth public org.netbeans.modules.websvc.saas.model.WsdlSaasMethod getMethod()
meth public org.netbeans.modules.xml.wsdl.model.WSDLModel getXamWsdlModel()
meth public static java.lang.String getParamType(org.netbeans.modules.websvc.jaxwsmodelapi.WSParameter)
meth public static org.netbeans.modules.websvc.jaxwsmodelapi.WSOperation findOperationByName(org.netbeans.modules.websvc.jaxwsmodelapi.WSPort,java.lang.String)
meth public void initWsdlModelInfo()
supr java.lang.Object
hfds categoryName,headerParams,method,operation,operationName,port,portName,project,service,serviceName,webServiceData,wsdlUrl

CLSS public org.netbeans.modules.websvc.saas.codegen.model.SoapClientSaasBean
cons public init(org.netbeans.modules.websvc.saas.model.WsdlSaasMethod,org.netbeans.api.project.Project)
cons public init(org.netbeans.modules.websvc.saas.model.WsdlSaasMethod,org.netbeans.api.project.Project,org.netbeans.modules.websvc.saas.codegen.model.SoapClientOperationInfo[])
meth protected java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> initInputParameters()
meth public boolean needsHtmlRepresentation()
meth public java.lang.String getResourceClassTemplate()
meth public java.lang.String[] getOutputTypes()
meth public java.util.List<java.lang.String> getOutputWrapperNames()
meth public java.util.List<java.lang.String> getOutputWrapperPackageNames()
meth public java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> getHeaderParameters()
meth public org.netbeans.modules.websvc.saas.codegen.model.SoapClientOperationInfo lastOperationInfo()
meth public org.netbeans.modules.websvc.saas.codegen.model.SoapClientOperationInfo[] getOperationInfos()
supr org.netbeans.modules.websvc.saas.codegen.model.SaasBean
hfds jaxwsInfos,m

CLSS public abstract interface org.netbeans.modules.websvc.saas.codegen.spi.SaasClientCodeGenerationProvider
meth public abstract boolean canAccept(org.netbeans.modules.websvc.saas.model.SaasMethod,javax.swing.text.Document)
meth public abstract int getPrecedence()
meth public abstract java.util.Set<org.openide.filesystems.FileObject> generate() throws java.io.IOException
meth public abstract void init(org.netbeans.modules.websvc.saas.model.SaasMethod,javax.swing.text.Document) throws java.io.IOException

CLSS public org.netbeans.modules.websvc.saas.codegen.ui.CodeSetupPanel
cons public init(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
cons public init(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>,boolean)
meth public void setDialog(java.awt.Dialog)
supr javax.swing.JPanel
hfds dialog,inputParams,methodNameModified,paramLabel,paramScrollPane,paramTable,showParamTypes,tableModel
hcls ParamCellRenderer,ParamTable,ParamTableModel,TableKeyListener

CLSS public org.netbeans.modules.websvc.saas.codegen.ui.CustomClientEditorDrop
cons public init(org.netbeans.modules.websvc.saas.model.CustomSaasMethod)
intf org.openide.text.ActiveEditorDrop
meth public boolean handleTransfer(javax.swing.text.JTextComponent)
meth public static org.openide.filesystems.FileObject getTargetFile(javax.swing.text.JTextComponent)
supr java.lang.Object
hfds generatorTask,method,targetFO

CLSS public org.netbeans.modules.websvc.saas.codegen.ui.CustomClientFlavorProvider
cons public init()
intf org.netbeans.modules.websvc.saas.spi.ConsumerFlavorProvider
meth public java.awt.datatransfer.Transferable addDataFlavors(java.awt.datatransfer.Transferable)
supr java.lang.Object
hcls ActiveEditorDropTransferable

CLSS public org.netbeans.modules.websvc.saas.codegen.ui.ProgressDialog
cons public init(java.lang.String)
meth public org.netbeans.api.progress.ProgressHandle getProgressHandle()
meth public void close()
meth public void open()
supr java.lang.Object
hfds dialog,pHandle
hcls ProgressPanel

CLSS public org.netbeans.modules.websvc.saas.codegen.ui.RestClientEditorDrop
cons public init(org.netbeans.modules.websvc.saas.model.WadlSaasMethod)
intf org.openide.text.ActiveEditorDrop
meth public boolean handleTransfer(javax.swing.text.JTextComponent)
meth public static org.openide.filesystems.FileObject getTargetFile(javax.swing.text.Document)
supr java.lang.Object
hfds generatorTask,method,targetFO

CLSS public org.netbeans.modules.websvc.saas.codegen.ui.RestClientFlavorProvider
cons public init()
intf org.netbeans.modules.websvc.saas.spi.ConsumerFlavorProvider
meth public java.awt.datatransfer.Transferable addDataFlavors(java.awt.datatransfer.Transferable)
supr java.lang.Object
hcls ActiveEditorDropTransferable

CLSS public org.netbeans.modules.websvc.saas.codegen.ui.SoapClientEditorDrop
cons public init(org.netbeans.modules.websvc.saas.model.WsdlSaasMethod)
intf org.openide.text.ActiveEditorDrop
meth public boolean handleTransfer(javax.swing.text.JTextComponent)
meth public static org.openide.filesystems.FileObject getTargetFile(javax.swing.text.JTextComponent)
supr java.lang.Object
hfds generatorTask,method,targetFO

CLSS public org.netbeans.modules.websvc.saas.codegen.ui.SoapClientFlavorProvider
cons public init()
intf org.netbeans.modules.websvc.saas.spi.ConsumerFlavorProvider
meth public java.awt.datatransfer.Transferable addDataFlavors(java.awt.datatransfer.Transferable)
supr java.lang.Object
hcls ActiveEditorDropTransferable,ServiceActiveEditorDropTransferable

CLSS public org.netbeans.modules.websvc.saas.codegen.ui.SoapServiceClientEditorDrop
cons public init(org.netbeans.modules.websvc.saas.model.WsdlSaas)
intf org.openide.text.ActiveEditorDrop
meth public boolean handleTransfer(javax.swing.text.JTextComponent)
meth public static org.openide.filesystems.FileObject getTargetFile(javax.swing.text.JTextComponent)
supr java.lang.Object
hfds generatorTask,methods,service,targetFO

CLSS public org.netbeans.modules.websvc.saas.codegen.util.Inflector
meth public java.lang.String camelize(java.lang.String)
meth public java.lang.String camelize(java.lang.String,boolean)
meth public java.lang.String classify(java.lang.String)
meth public java.lang.String dasherize(java.lang.String)
meth public java.lang.String demodulize(java.lang.String)
meth public java.lang.String foreignKey(java.lang.String)
meth public java.lang.String foreignKey(java.lang.String,boolean)
meth public java.lang.String humanize(java.lang.String)
meth public java.lang.String ordinalize(int)
meth public java.lang.String pluralize(java.lang.String)
meth public java.lang.String singularize(java.lang.String)
meth public java.lang.String tableize(java.lang.String)
meth public java.lang.String titleize(java.lang.String)
meth public java.lang.String underscore(java.lang.String)
meth public static org.netbeans.modules.websvc.saas.codegen.util.Inflector getInstance()
meth public void addIrregular(java.lang.String,java.lang.String)
meth public void addPlural(java.lang.String,java.lang.String)
meth public void addPlural(java.lang.String,java.lang.String,boolean)
meth public void addSingular(java.lang.String,java.lang.String)
meth public void addSingular(java.lang.String,java.lang.String,boolean)
meth public void addUncountable(java.lang.String)
supr java.lang.Object
hfds instance,plurals,singulars,uncountables
hcls Replacer

CLSS public org.netbeans.modules.websvc.saas.codegen.util.UniqueVariableNameFinder
cons public init()
meth public int getPatternCount(java.lang.String)
meth public java.lang.String findNewName(java.lang.String,java.lang.String)
meth public java.lang.String getVariableCount(java.lang.String)
meth public java.lang.String getVariableDecl(org.netbeans.modules.websvc.jaxwsmodelapi.WSParameter)
meth public java.lang.String getVariableDecl(org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo)
meth public java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> renameParameterNames(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth public void addPattern(java.lang.String,int)
meth public void clearPatterns()
meth public void updateVariableDecl(java.lang.String,java.lang.String) throws javax.swing.text.BadLocationException
meth public void updateVariableDecl(java.lang.String,java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>) throws javax.swing.text.BadLocationException
meth public void updateVariableDeclForWS(java.lang.String,java.util.List<? extends org.netbeans.modules.websvc.jaxwsmodelapi.WSParameter>) throws javax.swing.text.BadLocationException
supr java.lang.Object
hfds varDeclMap

CLSS public org.netbeans.modules.websvc.saas.codegen.util.Util
cons public init()
fld public final static java.lang.String ADELETE = "@DELETE"
fld public final static java.lang.String AGET = "@GET"
fld public final static java.lang.String APATH = "@Path"
fld public final static java.lang.String APOST = "@POST"
fld public final static java.lang.String APUT = "@PUT"
fld public final static java.lang.String AT = "@"
fld public final static java.lang.String BUILD_XML_PATH = "build.xml"
fld public final static java.lang.String SCANNING_IN_PROGRESS = "ScanningInProgress"
fld public final static java.lang.String TYPE_DOC_ROOT = "doc_root"
fld public final static java.lang.String VAR_NAMES_RESULT = "result"
fld public final static java.lang.String WIZARD_PANEL_CONTENT_DATA = "WizardPanel_contentData"
fld public final static java.lang.String WIZARD_PANEL_CONTENT_SELECTED_INDEX = "WizardPanel_contentSelectedIndex"
meth public static boolean hasInputRepresentations(org.netbeans.modules.websvc.saas.codegen.model.RestClientSaasBean)
meth public static boolean isContains(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>,org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo)
meth public static boolean isContains(org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo,java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth public static boolean isJava(javax.swing.text.Document)
meth public static boolean isJsp(javax.swing.text.Document)
meth public static boolean isKeyword(java.lang.String)
meth public static boolean isPutPostFormParams(org.netbeans.modules.websvc.saas.codegen.model.RestClientSaasBean)
meth public static boolean isRPCEncoded(org.netbeans.modules.xml.wsdl.model.WSDLModel)
meth public static boolean isValidPackageName(java.lang.String)
meth public static boolean showDialog(java.lang.String,java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>,javax.swing.text.Document)
meth public static java.lang.Class getGenericRawType(java.lang.String,java.lang.ClassLoader)
meth public static java.lang.Class getPrimitiveClassType(java.lang.String)
meth public static java.lang.Class getPrimitiveType(java.lang.String)
meth public static java.lang.Class getType(org.netbeans.api.project.Project,java.lang.String)
meth public static java.lang.Object getParamValue(org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo)
meth public static java.lang.Object[] getParamValues(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth public static java.lang.String camelize(java.lang.String,boolean)
meth public static java.lang.String createPrintStatement(java.util.List<java.lang.String>,java.util.List<java.lang.String>,org.netbeans.modules.websvc.saas.codegen.Constants$DropFileType,org.netbeans.modules.websvc.saas.codegen.Constants$HttpMethodType,boolean,java.lang.String)
meth public static java.lang.String createPrintStatement(java.util.List<java.lang.String>,java.util.List<java.lang.String>,org.netbeans.modules.websvc.saas.codegen.Constants$DropFileType,org.netbeans.modules.websvc.saas.codegen.Constants$HttpMethodType,boolean,java.lang.String,java.lang.String)
meth public static java.lang.String createSessionKeyLoginBodyForWeb(org.netbeans.modules.websvc.saas.codegen.model.SaasBean,java.lang.String,java.lang.String) throws java.io.IOException
meth public static java.lang.String createSessionKeyTokenBodyForWeb(org.netbeans.modules.websvc.saas.codegen.model.SaasBean,java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
meth public static java.lang.String deriveContainerClassName(java.lang.String)
meth public static java.lang.String deriveMethodName(java.lang.String)
meth public static java.lang.String deriveResourceClassName(java.lang.String)
meth public static java.lang.String deriveResourceName(java.lang.String)
meth public static java.lang.String deriveUri(java.lang.String,java.lang.String)
meth public static java.lang.String deriveUriTemplate(java.lang.String)
meth public static java.lang.String findParamValue(org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo)
meth public static java.lang.String getAuthenticatorClassName(java.lang.String)
meth public static java.lang.String getAuthorizationFrameClassName(java.lang.String)
meth public static java.lang.String getClassName(java.lang.String)
meth public static java.lang.String getHeaderOrParameterDefinition(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>,java.lang.String,boolean)
meth public static java.lang.String getHeaderOrParameterDefinition(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>,java.lang.String,boolean,org.netbeans.modules.websvc.saas.codegen.Constants$HttpMethodType)
meth public static java.lang.String getHeaderOrParameterDefinitionPart(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>,boolean)
meth public static java.lang.String getHeaderOrParameterUsage(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth public static java.lang.String getLoginArgumentsForWeb()
meth public static java.lang.String getLoginBody(org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator$Login,org.netbeans.modules.websvc.saas.codegen.model.SaasBean,java.lang.String,java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException
meth public static java.lang.String getPackageName(java.lang.String)
meth public static java.lang.String getParameterName(java.lang.String,boolean,boolean)
meth public static java.lang.String getParameterName(org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo)
meth public static java.lang.String getParameterName(org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo,boolean,boolean)
meth public static java.lang.String getParameterName(org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo,boolean,boolean,boolean)
meth public static java.lang.String getQuotedValue(java.lang.String)
meth public static java.lang.String getServletCallbackBody(org.netbeans.modules.websvc.saas.codegen.model.SaasBean,java.lang.String) throws java.io.IOException
meth public static java.lang.String getServletLoginBody(org.netbeans.modules.websvc.saas.codegen.model.SaasBean,java.lang.String) throws java.io.IOException
meth public static java.lang.String getSessionKeyMethodName(java.lang.String)
meth public static java.lang.String getSignParamDeclaration(org.netbeans.modules.websvc.saas.codegen.model.SaasBean,java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>,java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth public static java.lang.String getSignParamUsage(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>,java.lang.String,boolean)
meth public static java.lang.String getTokenMethodName(org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator)
meth public static java.lang.String getTokenName(org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator)
meth public static java.lang.String getTokenPromptUrl(org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication$UseGenerator$Token,java.lang.String)
meth public static java.lang.String getVariableName(java.lang.String)
meth public static java.lang.String getVariableName(java.lang.String,boolean,boolean,boolean)
meth public static java.lang.String lowerFirstChar(java.lang.String)
meth public static java.lang.String normailizeName(java.lang.String)
meth public static java.lang.String pluralize(java.lang.String)
meth public static java.lang.String singularize(java.lang.String)
meth public static java.lang.String stripPackageName(java.lang.String)
meth public static java.lang.String upperFirstChar(java.lang.String)
meth public static java.lang.String[] ensureTypes(java.lang.String[])
meth public static java.lang.String[] getGetParamNames(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth public static java.lang.String[] getGetParamTypes(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth public static java.lang.String[] getParamIds(org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo,java.lang.String,boolean)
meth public static java.lang.String[] getParamNames(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth public static java.lang.String[] getParamTypeNames(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth public static java.lang.reflect.Constructor getConstructorWithStringParam(java.lang.Class)
meth public static java.lang.reflect.Method getValueOfMethod(java.lang.Class)
meth public static java.util.Collection<java.lang.String> sortKeys(java.util.Collection<java.lang.String>)
meth public static java.util.List<java.lang.String> getJaxBClassImports()
meth public static java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> filterParameters(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>,org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo$ParamFilter[])
meth public static java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> filterParametersByAuth(org.netbeans.modules.websvc.saas.codegen.Constants$SaasAuthenticationType,org.netbeans.modules.websvc.saas.codegen.model.SaasBean$SaasAuthentication,java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth public static java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> getAuthenticatorMethodParametersForWeb()
meth public static java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> getRestClientMethodParameters(org.netbeans.modules.websvc.saas.codegen.model.RestClientSaasBean)
meth public static java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> getRestClientPutPostParameters(org.netbeans.modules.websvc.saas.codegen.model.RestClientSaasBean,java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth public static java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> getServiceMethodParametersForWeb(org.netbeans.modules.websvc.saas.codegen.model.RestClientSaasBean)
meth public static javax.swing.JLabel findLabel(javax.swing.JComponent,java.lang.String)
meth public static javax.swing.text.Document getDocument(org.openide.filesystems.FileObject) throws java.io.IOException
meth public static org.netbeans.api.project.Project[] getProjectsFromLookup(org.openide.util.Lookup)
meth public static org.netbeans.modules.websvc.saas.codegen.Constants$MimeType[] deriveMimeTypes(org.netbeans.modules.websvc.saas.codegen.model.SoapClientOperationInfo[])
meth public static org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo findParameter(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>,java.lang.String)
meth public static org.openide.filesystems.FileObject findBuildXml(org.netbeans.api.project.Project)
meth public static org.openide.loaders.DataObject createDataObjectFromTemplate(java.lang.String,org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public static org.openide.loaders.DataObject createDataObjectFromTemplate(java.lang.String,org.openide.filesystems.FileObject,java.lang.String,java.util.Map<java.lang.String,java.lang.Object>) throws java.io.IOException
meth public static void changeLabelInComponent(javax.swing.JComponent,java.lang.String,java.lang.String)
meth public static void doGenerateCode(org.netbeans.modules.websvc.saas.codegen.SaasClientCodeGenerator,org.netbeans.modules.websvc.saas.codegen.ui.ProgressDialog,java.util.List<java.lang.Exception>)
meth public static void getAllComponents(java.awt.Component[],java.util.Collection)
meth public static void hideLabelAndLabelFor(javax.swing.JComponent,java.lang.String)
meth public static void showUnsupportedDropMessage(java.lang.Object[])
supr java.lang.Object
hfds keywords,primitiveClassTypes,primitiveTypes

CLSS public abstract interface org.netbeans.modules.websvc.saas.spi.ConsumerFlavorProvider
fld public final static java.awt.datatransfer.DataFlavor CUSTOM_METHOD_FLAVOR
fld public final static java.awt.datatransfer.DataFlavor CUSTOM_METHOD_NODE_FLAVOR
fld public final static java.awt.datatransfer.DataFlavor PORT_FLAVOR
fld public final static java.awt.datatransfer.DataFlavor PORT_NODE_FLAVOR
fld public final static java.awt.datatransfer.DataFlavor WADL_METHOD_FLAVOR
fld public final static java.awt.datatransfer.DataFlavor WADL_METHOD_NODE_FLAVOR
fld public final static java.awt.datatransfer.DataFlavor WSDL_METHOD_FLAVOR
fld public final static java.awt.datatransfer.DataFlavor WSDL_METHOD_NODE_FLAVOR
fld public final static java.awt.datatransfer.DataFlavor WSDL_SERVICE_FLAVOR
fld public final static java.awt.datatransfer.DataFlavor WSDL_SERVICE_NODE_FLAVOR
meth public abstract java.awt.datatransfer.Transferable addDataFlavors(java.awt.datatransfer.Transferable)

CLSS public abstract interface org.openide.text.ActiveEditorDrop
fld public final static java.awt.datatransfer.DataFlavor FLAVOR
meth public abstract boolean handleTransfer(javax.swing.text.JTextComponent)

