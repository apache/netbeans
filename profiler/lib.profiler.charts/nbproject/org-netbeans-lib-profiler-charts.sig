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

CLSS public abstract interface java.awt.LayoutManager
meth public abstract java.awt.Dimension minimumLayoutSize(java.awt.Container)
meth public abstract java.awt.Dimension preferredLayoutSize(java.awt.Container)
meth public abstract void addLayoutComponent(java.lang.String,java.awt.Component)
meth public abstract void layoutContainer(java.awt.Container)
meth public abstract void removeLayoutComponent(java.awt.Component)

CLSS public abstract interface java.awt.LayoutManager2
intf java.awt.LayoutManager
meth public abstract float getLayoutAlignmentX(java.awt.Container)
meth public abstract float getLayoutAlignmentY(java.awt.Container)
meth public abstract java.awt.Dimension maximumLayoutSize(java.awt.Container)
meth public abstract void addLayoutComponent(java.awt.Component,java.lang.Object)
meth public abstract void invalidateLayout(java.awt.Container)

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

CLSS public abstract interface java.util.Iterator<%0 extends java.lang.Object>
meth public abstract boolean hasNext()
meth public abstract {java.util.Iterator%0} next()
meth public void forEachRemaining(java.util.function.Consumer<? super {java.util.Iterator%0}>)
meth public void remove()

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

CLSS public abstract interface javax.swing.border.Border
meth public abstract boolean isBorderOpaque()
meth public abstract java.awt.Insets getBorderInsets(java.awt.Component)
meth public abstract void paintBorder(java.awt.Component,java.awt.Graphics,int,int,int,int)

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

CLSS protected static org.netbeans.lib.profiler.charts.ChartComponent$Context
 outer org.netbeans.lib.profiler.charts.ChartComponent
cons public init(org.netbeans.lib.profiler.charts.ChartComponent)
intf org.netbeans.lib.profiler.charts.ChartContext
meth protected org.netbeans.lib.profiler.charts.ChartComponent getChartComponent()
meth public boolean fitsHeight()
meth public boolean fitsWidth()
meth public boolean isBottomBased()
meth public boolean isRightBased()
meth public double getDataHeight(double)
meth public double getDataWidth(double)
meth public double getDataX(double)
meth public double getDataY(double)
meth public double getReversedDataX(double)
meth public double getReversedDataY(double)
meth public double getReversedViewX(double)
meth public double getReversedViewY(double)
meth public double getViewHeight(double)
meth public double getViewWidth(double)
meth public double getViewX(double)
meth public double getViewY(double)
meth public int getViewportHeight()
meth public int getViewportWidth()
meth public long getDataHeight()
meth public long getDataOffsetX()
meth public long getDataOffsetY()
meth public long getDataWidth()
meth public long getViewHeight()
meth public long getViewWidth()
meth public long getViewportOffsetX()
meth public long getViewportOffsetY()
meth public org.netbeans.lib.profiler.charts.swing.LongRect getViewRect(org.netbeans.lib.profiler.charts.swing.LongRect)
supr java.lang.Object
hfds chart

CLSS public abstract interface org.netbeans.lib.profiler.charts.ChartConfigurationListener
innr public abstract static Adapter
meth public abstract void contentsUpdated(long,long,double,double,long,long,double,double,int,int)
meth public abstract void contentsWillBeUpdated(long,long,double,double,long,long,double,double)
meth public abstract void dataBoundsChanged(long,long,long,long,long,long,long,long)
meth public abstract void offsetChanged(long,long,long,long)
meth public abstract void scaleChanged(double,double,double,double)

CLSS public abstract static org.netbeans.lib.profiler.charts.ChartConfigurationListener$Adapter
 outer org.netbeans.lib.profiler.charts.ChartConfigurationListener
cons public init()
intf org.netbeans.lib.profiler.charts.ChartConfigurationListener
meth public void contentsUpdated(long,long,double,double,long,long,double,double,int,int)
meth public void contentsWillBeUpdated(long,long,double,double,long,long,double,double)
meth public void dataBoundsChanged(long,long,long,long,long,long,long,long)
meth public void offsetChanged(long,long,long,long)
meth public void scaleChanged(double,double,double,double)
supr java.lang.Object

CLSS public abstract interface org.netbeans.lib.profiler.charts.ChartContext
meth public abstract boolean fitsHeight()
meth public abstract boolean fitsWidth()
meth public abstract boolean isBottomBased()
meth public abstract boolean isRightBased()
meth public abstract double getDataHeight(double)
meth public abstract double getDataWidth(double)
meth public abstract double getDataX(double)
meth public abstract double getDataY(double)
meth public abstract double getReversedDataX(double)
meth public abstract double getReversedDataY(double)
meth public abstract double getReversedViewX(double)
meth public abstract double getReversedViewY(double)
meth public abstract double getViewHeight(double)
meth public abstract double getViewWidth(double)
meth public abstract double getViewX(double)
meth public abstract double getViewY(double)
meth public abstract int getViewportHeight()
meth public abstract int getViewportWidth()
meth public abstract long getDataHeight()
meth public abstract long getDataOffsetX()
meth public abstract long getDataOffsetY()
meth public abstract long getDataWidth()
meth public abstract long getViewHeight()
meth public abstract long getViewWidth()
meth public abstract long getViewportOffsetX()
meth public abstract long getViewportOffsetY()
meth public abstract org.netbeans.lib.profiler.charts.swing.LongRect getViewRect(org.netbeans.lib.profiler.charts.swing.LongRect)

CLSS public abstract interface org.netbeans.lib.profiler.charts.ChartDecorator
meth public abstract void paint(java.awt.Graphics2D,java.awt.Rectangle,org.netbeans.lib.profiler.charts.ChartContext)

CLSS public abstract interface org.netbeans.lib.profiler.charts.ChartItem
innr public abstract static Abstract
meth public abstract void addItemListener(org.netbeans.lib.profiler.charts.ChartItemListener)
meth public abstract void removeItemListener(org.netbeans.lib.profiler.charts.ChartItemListener)

CLSS public abstract static org.netbeans.lib.profiler.charts.ChartItem$Abstract
 outer org.netbeans.lib.profiler.charts.ChartItem
cons public init()
intf org.netbeans.lib.profiler.charts.ChartItem
meth protected void fireItemChanged(org.netbeans.lib.profiler.charts.ChartItemChange)
meth public void addItemListener(org.netbeans.lib.profiler.charts.ChartItemListener)
meth public void removeItemListener(org.netbeans.lib.profiler.charts.ChartItemListener)
supr java.lang.Object
hfds listeners

CLSS public abstract interface org.netbeans.lib.profiler.charts.ChartItemChange
innr public static Default
meth public abstract org.netbeans.lib.profiler.charts.ChartItem getItem()

CLSS public static org.netbeans.lib.profiler.charts.ChartItemChange$Default
 outer org.netbeans.lib.profiler.charts.ChartItemChange
cons public init(org.netbeans.lib.profiler.charts.ChartItem)
intf org.netbeans.lib.profiler.charts.ChartItemChange
meth public org.netbeans.lib.profiler.charts.ChartItem getItem()
supr java.lang.Object
hfds item

CLSS public abstract interface org.netbeans.lib.profiler.charts.ChartItemListener
meth public abstract void chartItemChanged(org.netbeans.lib.profiler.charts.ChartItemChange)

CLSS public abstract org.netbeans.lib.profiler.charts.ChartOverlay
cons public init()
meth protected final org.netbeans.lib.profiler.charts.ChartContext getChartContext()
supr javax.swing.JComponent
hfds context

CLSS public abstract interface org.netbeans.lib.profiler.charts.ChartSelectionListener
meth public abstract void highlightedItemsChanged(java.util.List<org.netbeans.lib.profiler.charts.ItemSelection>,java.util.List<org.netbeans.lib.profiler.charts.ItemSelection>,java.util.List<org.netbeans.lib.profiler.charts.ItemSelection>)
meth public abstract void selectedItemsChanged(java.util.List<org.netbeans.lib.profiler.charts.ItemSelection>,java.util.List<org.netbeans.lib.profiler.charts.ItemSelection>,java.util.List<org.netbeans.lib.profiler.charts.ItemSelection>)
meth public abstract void selectionBoundsChanged(java.awt.Rectangle,java.awt.Rectangle)
meth public abstract void selectionModeChanged(int,int)

CLSS public abstract interface org.netbeans.lib.profiler.charts.ChartSelectionModel
fld public final static int HOVER_DISTANCE_LIMIT_NONE = -1
fld public final static int HOVER_EACH_NEAREST = 102
fld public final static int HOVER_NEAREST = 101
fld public final static int HOVER_NONE = 100
fld public final static int SELECTION_CROSS = 3
fld public final static int SELECTION_LINE_H = 2
fld public final static int SELECTION_LINE_V = 1
fld public final static int SELECTION_NONE = 0
fld public final static int SELECTION_RECT = 4
meth public abstract int getDragMode()
meth public abstract int getHoverDistanceLimit()
meth public abstract int getHoverMode()
meth public abstract int getMoveMode()
meth public abstract int getSelectionMode()
meth public abstract java.awt.Rectangle getSelectionBounds()
meth public abstract java.util.List<org.netbeans.lib.profiler.charts.ItemSelection> getHighlightedItems()
meth public abstract java.util.List<org.netbeans.lib.profiler.charts.ItemSelection> getSelectedItems()
meth public abstract void addSelectionListener(org.netbeans.lib.profiler.charts.ChartSelectionListener)
meth public abstract void removeSelectionListener(org.netbeans.lib.profiler.charts.ChartSelectionListener)
meth public abstract void setDragMode(int)
meth public abstract void setHighlightedItems(java.util.List<org.netbeans.lib.profiler.charts.ItemSelection>)
meth public abstract void setHoverDistanceLimit(int)
meth public abstract void setHoverMode(int)
meth public abstract void setMoveMode(int)
meth public abstract void setSelectedItems(java.util.List<org.netbeans.lib.profiler.charts.ItemSelection>)
meth public abstract void setSelectionBounds(java.awt.Rectangle)

CLSS public org.netbeans.lib.profiler.charts.CompoundItemPainter
cons public init(org.netbeans.lib.profiler.charts.ItemPainter,org.netbeans.lib.profiler.charts.ItemPainter)
intf org.netbeans.lib.profiler.charts.ItemPainter
meth protected org.netbeans.lib.profiler.charts.ItemPainter getPainter1()
meth protected org.netbeans.lib.profiler.charts.ItemPainter getPainter2()
meth public boolean isAppearanceChange(org.netbeans.lib.profiler.charts.ChartItemChange)
meth public boolean isBoundsChange(org.netbeans.lib.profiler.charts.ChartItemChange)
meth public boolean supportsHovering(org.netbeans.lib.profiler.charts.ChartItem)
meth public boolean supportsSelecting(org.netbeans.lib.profiler.charts.ChartItem)
meth public org.netbeans.lib.profiler.charts.ItemSelection getClosestSelection(org.netbeans.lib.profiler.charts.ChartItem,int,int,org.netbeans.lib.profiler.charts.ChartContext)
meth public org.netbeans.lib.profiler.charts.swing.LongRect getDirtyBounds(org.netbeans.lib.profiler.charts.ChartItemChange,org.netbeans.lib.profiler.charts.ChartContext)
meth public org.netbeans.lib.profiler.charts.swing.LongRect getItemBounds(org.netbeans.lib.profiler.charts.ChartItem)
meth public org.netbeans.lib.profiler.charts.swing.LongRect getItemBounds(org.netbeans.lib.profiler.charts.ChartItem,org.netbeans.lib.profiler.charts.ChartContext)
meth public org.netbeans.lib.profiler.charts.swing.LongRect getSelectionBounds(org.netbeans.lib.profiler.charts.ItemSelection,org.netbeans.lib.profiler.charts.ChartContext)
meth public void paintItem(org.netbeans.lib.profiler.charts.ChartItem,java.util.List<org.netbeans.lib.profiler.charts.ItemSelection>,java.util.List<org.netbeans.lib.profiler.charts.ItemSelection>,java.awt.Graphics2D,java.awt.Rectangle,org.netbeans.lib.profiler.charts.ChartContext)
supr java.lang.Object
hfds painter1,painter2

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

CLSS public abstract interface org.netbeans.lib.profiler.charts.ItemSelection
fld public final static int DISTANCE_UNKNOWN = 2147483647
innr public static Default
meth public abstract int getDistance()
meth public abstract org.netbeans.lib.profiler.charts.ChartItem getItem()

CLSS public static org.netbeans.lib.profiler.charts.ItemSelection$Default
 outer org.netbeans.lib.profiler.charts.ItemSelection
cons public init(org.netbeans.lib.profiler.charts.ChartItem)
cons public init(org.netbeans.lib.profiler.charts.ChartItem,int)
intf org.netbeans.lib.profiler.charts.ItemSelection
meth public boolean equals(java.lang.Object)
meth public int getDistance()
meth public int hashCode()
meth public org.netbeans.lib.profiler.charts.ChartItem getItem()
supr java.lang.Object
hfds distance,item

CLSS public abstract interface org.netbeans.lib.profiler.charts.ItemsListener
meth public abstract void itemsAdded(java.util.List<org.netbeans.lib.profiler.charts.ChartItem>)
meth public abstract void itemsChanged(java.util.List<org.netbeans.lib.profiler.charts.ChartItemChange>)
meth public abstract void itemsRemoved(java.util.List<org.netbeans.lib.profiler.charts.ChartItem>)

CLSS public abstract interface org.netbeans.lib.profiler.charts.ItemsModel
innr public abstract static Abstract
meth public abstract int getItemsCount()
meth public abstract org.netbeans.lib.profiler.charts.ChartItem getItem(int)
meth public abstract void addItemsListener(org.netbeans.lib.profiler.charts.ItemsListener)
meth public abstract void removeItemsListener(org.netbeans.lib.profiler.charts.ItemsListener)

CLSS public abstract static org.netbeans.lib.profiler.charts.ItemsModel$Abstract
 outer org.netbeans.lib.profiler.charts.ItemsModel
cons public init()
intf org.netbeans.lib.profiler.charts.ItemsModel
meth protected void fireItemsAdded(java.util.List<org.netbeans.lib.profiler.charts.ChartItem>)
meth protected void fireItemsChanged(java.util.List<org.netbeans.lib.profiler.charts.ChartItemChange>)
meth protected void fireItemsRemoved(java.util.List<org.netbeans.lib.profiler.charts.ChartItem>)
meth public void addItemsListener(org.netbeans.lib.profiler.charts.ItemsListener)
meth public void removeItemsListener(org.netbeans.lib.profiler.charts.ItemsListener)
supr java.lang.Object
hfds listeners

CLSS public abstract interface org.netbeans.lib.profiler.charts.PaintersListener
meth public abstract void paintersChanged()
meth public abstract void paintersChanged(java.util.List<org.netbeans.lib.profiler.charts.ItemPainter>)

CLSS public abstract interface org.netbeans.lib.profiler.charts.PaintersModel
innr public abstract static Abstract
innr public static Default
meth public abstract org.netbeans.lib.profiler.charts.ItemPainter getPainter(org.netbeans.lib.profiler.charts.ChartItem)
meth public abstract void addPaintersListener(org.netbeans.lib.profiler.charts.PaintersListener)
meth public abstract void removePaintersListener(org.netbeans.lib.profiler.charts.PaintersListener)

CLSS public abstract static org.netbeans.lib.profiler.charts.PaintersModel$Abstract
 outer org.netbeans.lib.profiler.charts.PaintersModel
cons public init()
intf org.netbeans.lib.profiler.charts.PaintersModel
meth protected void firePaintersChanged()
meth protected void firePaintersChanged(java.util.List<org.netbeans.lib.profiler.charts.ItemPainter>)
meth public void addPaintersListener(org.netbeans.lib.profiler.charts.PaintersListener)
meth public void removePaintersListener(org.netbeans.lib.profiler.charts.PaintersListener)
supr java.lang.Object
hfds listeners

CLSS public static org.netbeans.lib.profiler.charts.PaintersModel$Default
 outer org.netbeans.lib.profiler.charts.PaintersModel
cons public init()
cons public init(org.netbeans.lib.profiler.charts.ChartItem[],org.netbeans.lib.profiler.charts.ItemPainter[])
meth public org.netbeans.lib.profiler.charts.ItemPainter getPainter(org.netbeans.lib.profiler.charts.ChartItem)
meth public void addPainters(org.netbeans.lib.profiler.charts.ChartItem[],org.netbeans.lib.profiler.charts.ItemPainter[])
meth public void removePainters(org.netbeans.lib.profiler.charts.ChartItem[])
supr org.netbeans.lib.profiler.charts.PaintersModel$Abstract
hfds painters

CLSS public abstract interface org.netbeans.lib.profiler.charts.Timeline
meth public abstract int getTimestampsCount()
meth public abstract long getTimestamp(int)

CLSS public org.netbeans.lib.profiler.charts.axis.AxisComponent
cons public init(org.netbeans.lib.profiler.charts.ChartComponent,org.netbeans.lib.profiler.charts.axis.AxisMarksComputer,org.netbeans.lib.profiler.charts.axis.AxisMarksPainter,int,int)
fld public final static int MESH_BACKGROUND = 1
fld public final static int MESH_FOREGROUND = 2
fld public final static int NO_MESH = 0
meth protected int getAxisBasisExtent()
meth protected void paintAxis(java.awt.Graphics,java.awt.Rectangle,java.awt.Rectangle)
meth protected void paintAxisMesh(java.awt.Graphics2D,java.awt.Rectangle,java.awt.Rectangle)
meth protected void paintHorizontalAxis(java.awt.Graphics,java.awt.Rectangle,java.awt.Rectangle)
meth protected void paintHorizontalBasis(java.awt.Graphics,java.awt.Rectangle,java.awt.Rectangle)
meth protected void paintHorizontalMesh(java.awt.Graphics2D,java.awt.Rectangle,java.awt.Rectangle)
meth protected void paintHorizontalTick(java.awt.Graphics,org.netbeans.lib.profiler.charts.axis.AxisMark,int,java.awt.Rectangle,java.awt.Rectangle)
meth protected void paintVerticalAxis(java.awt.Graphics,java.awt.Rectangle,java.awt.Rectangle)
meth protected void paintVerticalBasis(java.awt.Graphics,java.awt.Rectangle,java.awt.Rectangle)
meth protected void paintVerticalMesh(java.awt.Graphics2D,java.awt.Rectangle,java.awt.Rectangle)
meth protected void paintVerticalTick(java.awt.Graphics,org.netbeans.lib.profiler.charts.axis.AxisMark,int,java.awt.Rectangle,java.awt.Rectangle)
meth public void paint(java.awt.Graphics)
supr javax.swing.JComponent
hfds chart,horizontal,location,marksComputer,marksPainter,maxExtent,meshPaint,meshStroke
hcls ChartListener

CLSS public abstract interface org.netbeans.lib.profiler.charts.axis.AxisMark
innr public abstract static Abstract
meth public abstract int getPosition()

CLSS public abstract static org.netbeans.lib.profiler.charts.axis.AxisMark$Abstract
 outer org.netbeans.lib.profiler.charts.axis.AxisMark
cons public init(int)
intf org.netbeans.lib.profiler.charts.axis.AxisMark
meth public int getPosition()
supr java.lang.Object
hfds position

CLSS public abstract interface org.netbeans.lib.profiler.charts.axis.AxisMarksComputer
fld public final static java.util.Iterator<org.netbeans.lib.profiler.charts.axis.AxisMark> EMPTY_ITERATOR
innr public abstract static Abstract
innr public abstract static AbstractIterator
meth public abstract java.util.Iterator<org.netbeans.lib.profiler.charts.axis.AxisMark> marksIterator(int,int)

CLSS public abstract static org.netbeans.lib.profiler.charts.axis.AxisMarksComputer$Abstract
 outer org.netbeans.lib.profiler.charts.axis.AxisMarksComputer
cons public init(org.netbeans.lib.profiler.charts.ChartContext,int)
fld protected final boolean horizontal
fld protected final boolean reverse
fld protected final int orientation
fld protected final org.netbeans.lib.profiler.charts.ChartContext context
intf org.netbeans.lib.profiler.charts.axis.AxisMarksComputer
meth protected boolean refreshConfiguration()
meth protected int getMinMarksDistance()
supr java.lang.Object

CLSS public abstract static org.netbeans.lib.profiler.charts.axis.AxisMarksComputer$AbstractIterator
 outer org.netbeans.lib.profiler.charts.axis.AxisMarksComputer
cons public init()
intf java.util.Iterator<org.netbeans.lib.profiler.charts.axis.AxisMark>
meth public void remove()
supr java.lang.Object

CLSS public abstract interface org.netbeans.lib.profiler.charts.axis.AxisMarksPainter
innr public abstract static Abstract
meth public abstract java.awt.Component getPainter(org.netbeans.lib.profiler.charts.axis.AxisMark)

CLSS public abstract static org.netbeans.lib.profiler.charts.axis.AxisMarksPainter$Abstract
 outer org.netbeans.lib.profiler.charts.axis.AxisMarksPainter
cons public init()
intf org.netbeans.lib.profiler.charts.axis.AxisMarksPainter
meth protected abstract java.lang.String formatMark(org.netbeans.lib.profiler.charts.axis.AxisMark)
meth public java.awt.Component getPainter(org.netbeans.lib.profiler.charts.axis.AxisMark)
supr javax.swing.JLabel

CLSS public org.netbeans.lib.profiler.charts.axis.BytesAxisUtils
cons public init()
fld public final static java.lang.String UNITS_B
fld public final static java.lang.String UNITS_GB
fld public final static java.lang.String UNITS_KB
fld public final static java.lang.String UNITS_MB
fld public final static java.lang.String UNITS_PB
fld public final static java.lang.String UNITS_TB
fld public final static java.lang.String[] radixUnits
fld public final static long[] bytesUnitsGrid
meth public static java.lang.String formatBytes(org.netbeans.lib.profiler.charts.axis.BytesMark)
meth public static java.lang.String getRadixUnits(org.netbeans.lib.profiler.charts.axis.BytesMark)
meth public static long[] getBytesUnits(double,int)
supr java.lang.Object
hfds FORMAT,SIZE_FORMAT,messages

CLSS public org.netbeans.lib.profiler.charts.axis.BytesMark
cons public init(long,int,int)
meth public int getRadix()
supr org.netbeans.lib.profiler.charts.axis.LongMark
hfds radix

CLSS public org.netbeans.lib.profiler.charts.axis.BytesMarksPainter
cons public init()
meth protected java.lang.String formatMark(org.netbeans.lib.profiler.charts.axis.AxisMark)
supr org.netbeans.lib.profiler.charts.axis.AxisMarksPainter$Abstract

CLSS public org.netbeans.lib.profiler.charts.axis.DecimalAxisUtils
cons public init()
fld public final static long[] decimalUnitsGrid
meth public static long getDecimalUnits(double,int)
supr java.lang.Object

CLSS public org.netbeans.lib.profiler.charts.axis.LongMark
cons public init(long,int)
meth public long getValue()
supr org.netbeans.lib.profiler.charts.axis.AxisMark$Abstract
hfds value

CLSS public org.netbeans.lib.profiler.charts.axis.PercentLongMarksPainter
cons public init(long,long)
fld protected final long maxValue
fld protected final long minValue
fld protected java.text.NumberFormat format
meth protected java.lang.String formatMark(org.netbeans.lib.profiler.charts.axis.AxisMark)
supr org.netbeans.lib.profiler.charts.axis.AxisMarksPainter$Abstract

CLSS public org.netbeans.lib.profiler.charts.axis.SimpleLongMarksPainter
cons public init()
meth protected java.lang.String formatMark(org.netbeans.lib.profiler.charts.axis.AxisMark)
supr org.netbeans.lib.profiler.charts.axis.AxisMarksPainter$Abstract
hfds FORMAT

CLSS public org.netbeans.lib.profiler.charts.axis.TimeAxisUtils
cons public init()
fld public final static int DAY_NEEDED = 1
fld public final static int MONTH_NEEDED = 2
fld public final static int NTHNG_NEEDED = 0
fld public final static int STEP_DAY = 16
fld public final static int STEP_HOUR = 8
fld public final static int STEP_MIN = 4
fld public final static int STEP_MONTH = 64
fld public final static int STEP_MSEC = 1
fld public final static int STEP_SEC = 2
fld public final static int STEP_WEEK = 32
fld public final static int STEP_YEAR = 128
fld public final static int YEAR_NEEDED = 4
fld public final static java.lang.String DATE_MONTH
fld public final static java.lang.String DATE_SINGLEYEAR = "yyyy"
fld public final static java.lang.String DATE_WEEKDAY = "EEEE"
fld public final static java.lang.String DATE_WEEKDAY_SHORT = "EEE"
fld public final static java.lang.String DATE_YEAR
fld public final static java.lang.String DATE_YEARMONTH = "MMMM"
fld public final static java.lang.String TIME_DATE_FORMAT = "{0}, {1}"
fld public final static java.lang.String TIME_MIN
fld public final static java.lang.String TIME_MSEC
fld public final static java.lang.String TIME_SEC
fld public final static long[] timeUnitsGrid
meth public static int getRangeFlag(long,long)
meth public static int getStepFlag(long)
meth public static java.lang.String formatTime(java.lang.Long,java.lang.String)
meth public static java.lang.String formatTime(org.netbeans.lib.profiler.charts.axis.TimeMark)
meth public static java.lang.String getFormatString(long,long,long)
meth public static long getTimeUnits(double,int)
supr java.lang.Object
hfds FORMATS,PATTERN_CHARS,c1,c2

CLSS public org.netbeans.lib.profiler.charts.axis.TimeMark
cons public init(long,int,java.lang.String)
meth public java.lang.String getFormat()
supr org.netbeans.lib.profiler.charts.axis.LongMark
hfds format

CLSS public org.netbeans.lib.profiler.charts.axis.TimeMarksPainter
cons public init()
meth protected java.lang.String formatMark(org.netbeans.lib.profiler.charts.axis.AxisMark)
supr org.netbeans.lib.profiler.charts.axis.AxisMarksPainter$Abstract

CLSS public org.netbeans.lib.profiler.charts.axis.TimelineMarksComputer
cons public init(org.netbeans.lib.profiler.charts.Timeline,org.netbeans.lib.profiler.charts.ChartContext,int)
meth protected boolean refreshConfiguration()
meth protected int getMinMarksDistance()
meth public java.util.Iterator<org.netbeans.lib.profiler.charts.axis.AxisMark> marksIterator(int,int)
supr org.netbeans.lib.profiler.charts.axis.AxisMarksComputer$Abstract
hfds firstTimestamp,lastTimestamp,scale,step,timeline

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

CLSS public org.netbeans.lib.profiler.charts.swing.CrossBorderLayout
cons public init()
intf java.awt.LayoutManager2
meth public float getLayoutAlignmentX(java.awt.Container)
meth public float getLayoutAlignmentY(java.awt.Container)
meth public java.awt.Component getLayoutComponent(int)
meth public java.awt.Dimension maximumLayoutSize(java.awt.Container)
meth public java.awt.Dimension minimumLayoutSize(java.awt.Container)
meth public java.awt.Dimension preferredLayoutSize(java.awt.Container)
meth public java.lang.Object getConstraints(int)
meth public java.lang.Object getConstraints(java.awt.Component)
meth public void addLayoutComponent(java.awt.Component,java.lang.Object)
meth public void addLayoutComponent(java.lang.String,java.awt.Component)
meth public void invalidateLayout(java.awt.Container)
meth public void layoutContainer(java.awt.Container)
meth public void removeLayoutComponent(java.awt.Component)
supr java.lang.Object
hfds NONE,center,east,map,north,south,west

CLSS public final org.netbeans.lib.profiler.charts.swing.LongRect
cons public init()
cons public init(long,long,long,long)
cons public init(org.netbeans.lib.profiler.charts.swing.LongRect)
fld public long height
fld public long width
fld public long x
fld public long y
meth public java.lang.String toString()
meth public static boolean contains(org.netbeans.lib.profiler.charts.swing.LongRect,org.netbeans.lib.profiler.charts.swing.LongRect)
meth public static boolean equals(org.netbeans.lib.profiler.charts.swing.LongRect,org.netbeans.lib.profiler.charts.swing.LongRect)
meth public static boolean isClear(org.netbeans.lib.profiler.charts.swing.LongRect)
meth public static boolean isEmpty(org.netbeans.lib.profiler.charts.swing.LongRect)
meth public static boolean touches(org.netbeans.lib.profiler.charts.swing.LongRect,org.netbeans.lib.profiler.charts.swing.LongRect)
meth public static void add(org.netbeans.lib.profiler.charts.swing.LongRect,long,long)
meth public static void add(org.netbeans.lib.profiler.charts.swing.LongRect,org.netbeans.lib.profiler.charts.swing.LongRect)
meth public static void addBorder(org.netbeans.lib.profiler.charts.swing.LongRect,long)
meth public static void clear(org.netbeans.lib.profiler.charts.swing.LongRect)
meth public static void set(org.netbeans.lib.profiler.charts.swing.LongRect,long,long,long,long)
meth public static void set(org.netbeans.lib.profiler.charts.swing.LongRect,org.netbeans.lib.profiler.charts.swing.LongRect)
supr java.lang.Object

CLSS public org.netbeans.lib.profiler.charts.swing.RoundBorder
cons public init(float,java.awt.Color,java.awt.Color,int,int)
meth public java.awt.Insets getBorderInsets(java.awt.Component)
meth public java.awt.Insets getBorderInsets(java.awt.Component,java.awt.Insets)
meth public void paintBorder(java.awt.Component,java.awt.Graphics,int,int,int,int)
supr javax.swing.border.AbstractBorder
hfds arcRadius,borderExtent,borderStroke,borderStrokeWidth,fillColor,forceSpeed,halfBorderStrokeWidth,inset,lineColor

CLSS public final org.netbeans.lib.profiler.charts.swing.Utils
cons public init()
fld public final static int VALUE_OUT_OF_RANGE_NEG = -2147483648
fld public final static int VALUE_OUT_OF_RANGE_POS = 2147483647
meth public final static int checkedInt(double)
meth public final static java.awt.Rectangle checkedRectangle(org.netbeans.lib.profiler.charts.swing.LongRect)
meth public static boolean forceSpeed()
meth public static float getStrokeWidth(java.awt.Stroke)
meth public static java.awt.Color checkedColor(java.awt.Color)
meth public static java.awt.Color getSystemSelection()
meth public static java.awt.RenderingHints checkedRenderingHints(java.awt.RenderingHints)
supr java.lang.Object
hfds FORCE_SPEED,forceSpeed

CLSS public org.netbeans.lib.profiler.charts.xy.BytesXYItemMarksComputer
cons public init(org.netbeans.lib.profiler.charts.xy.XYItem,org.netbeans.lib.profiler.charts.xy.XYItemPainter,org.netbeans.lib.profiler.charts.ChartContext,int)
meth protected boolean refreshConfiguration()
meth public java.util.Iterator<org.netbeans.lib.profiler.charts.axis.AxisMark> marksIterator(int,int)
supr org.netbeans.lib.profiler.charts.xy.XYItemMarksComputer
hfds radix,scale,step

CLSS public org.netbeans.lib.profiler.charts.xy.CompoundXYItemPainter
cons public init(org.netbeans.lib.profiler.charts.xy.XYItemPainter,org.netbeans.lib.profiler.charts.xy.XYItemPainter)
intf org.netbeans.lib.profiler.charts.xy.XYItemPainter
meth protected org.netbeans.lib.profiler.charts.xy.XYItemPainter getPainter1()
meth protected org.netbeans.lib.profiler.charts.xy.XYItemPainter getPainter2()
meth public double getItemValue(double,org.netbeans.lib.profiler.charts.xy.XYItem,org.netbeans.lib.profiler.charts.ChartContext)
meth public double getItemValueScale(org.netbeans.lib.profiler.charts.xy.XYItem,org.netbeans.lib.profiler.charts.ChartContext)
meth public double getItemView(double,org.netbeans.lib.profiler.charts.xy.XYItem,org.netbeans.lib.profiler.charts.ChartContext)
supr org.netbeans.lib.profiler.charts.CompoundItemPainter

CLSS public org.netbeans.lib.profiler.charts.xy.DecimalXYItemMarksComputer
cons public init(org.netbeans.lib.profiler.charts.xy.XYItem,org.netbeans.lib.profiler.charts.xy.XYItemPainter,org.netbeans.lib.profiler.charts.ChartContext,int)
meth protected boolean refreshConfiguration()
meth public java.util.Iterator<org.netbeans.lib.profiler.charts.axis.AxisMark> marksIterator(int,int)
supr org.netbeans.lib.profiler.charts.xy.XYItemMarksComputer
hfds scale,step

CLSS public abstract interface org.netbeans.lib.profiler.charts.xy.XYItem
intf org.netbeans.lib.profiler.charts.ChartItem
meth public abstract int getValuesCount()
meth public abstract long getXValue(int)
meth public abstract long getYValue(int)
meth public abstract org.netbeans.lib.profiler.charts.swing.LongRect getBounds()

CLSS public abstract interface org.netbeans.lib.profiler.charts.xy.XYItemChange
innr public static Default
intf org.netbeans.lib.profiler.charts.ChartItemChange
meth public abstract int[] getValuesIndexes()
meth public abstract org.netbeans.lib.profiler.charts.swing.LongRect getDirtyValuesBounds()
meth public abstract org.netbeans.lib.profiler.charts.swing.LongRect getNewValuesBounds()
meth public abstract org.netbeans.lib.profiler.charts.swing.LongRect getOldValuesBounds()
meth public abstract org.netbeans.lib.profiler.charts.xy.XYItem getItem()

CLSS public static org.netbeans.lib.profiler.charts.xy.XYItemChange$Default
 outer org.netbeans.lib.profiler.charts.xy.XYItemChange
cons public init(org.netbeans.lib.profiler.charts.xy.XYItem,int[],org.netbeans.lib.profiler.charts.swing.LongRect,org.netbeans.lib.profiler.charts.swing.LongRect,org.netbeans.lib.profiler.charts.swing.LongRect)
intf org.netbeans.lib.profiler.charts.xy.XYItemChange
meth public int[] getValuesIndexes()
meth public org.netbeans.lib.profiler.charts.swing.LongRect getDirtyValuesBounds()
meth public org.netbeans.lib.profiler.charts.swing.LongRect getNewValuesBounds()
meth public org.netbeans.lib.profiler.charts.swing.LongRect getOldValuesBounds()
meth public org.netbeans.lib.profiler.charts.xy.XYItem getItem()
supr org.netbeans.lib.profiler.charts.ChartItemChange$Default
hfds dirtyBounds,newBounds,oldBounds,valuesIndexes

CLSS public abstract org.netbeans.lib.profiler.charts.xy.XYItemMarksComputer
cons public init(org.netbeans.lib.profiler.charts.xy.XYItem,org.netbeans.lib.profiler.charts.xy.XYItemPainter,org.netbeans.lib.profiler.charts.ChartContext,int)
fld protected final org.netbeans.lib.profiler.charts.xy.XYItem item
fld protected final org.netbeans.lib.profiler.charts.xy.XYItemPainter painter
supr org.netbeans.lib.profiler.charts.axis.AxisMarksComputer$Abstract

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

CLSS public abstract interface org.netbeans.lib.profiler.charts.xy.XYItemSelection
innr public static Default
intf org.netbeans.lib.profiler.charts.ItemSelection
meth public abstract int getValueIndex()
meth public abstract org.netbeans.lib.profiler.charts.xy.XYItem getItem()

CLSS public static org.netbeans.lib.profiler.charts.xy.XYItemSelection$Default
 outer org.netbeans.lib.profiler.charts.xy.XYItemSelection
cons public init(org.netbeans.lib.profiler.charts.xy.XYItem,int)
cons public init(org.netbeans.lib.profiler.charts.xy.XYItem,int,int)
intf org.netbeans.lib.profiler.charts.xy.XYItemSelection
meth public boolean equals(java.lang.Object)
meth public int getValueIndex()
meth public int hashCode()
meth public org.netbeans.lib.profiler.charts.xy.XYItem getItem()
supr org.netbeans.lib.profiler.charts.ItemSelection$Default
hfds valueIndex

CLSS public org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYChart
cons public init(org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYItemsModel,org.netbeans.lib.profiler.charts.PaintersModel)
innr protected static Context
meth protected org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYChart$Context createChartContext()
meth protected void reshaped(java.awt.Rectangle,java.awt.Rectangle)
meth public int getNearestTimestampIndex(int,int)
supr org.netbeans.lib.profiler.charts.ChartComponent
hfds VISIBLE_NONE,contentsWidthChanged,firstVisibleIndex,indexesCache,lastVisibleIndex,newBoundsWidth,newOffsetX,newScaleX,oldBoundsWidth,oldOffsetX,oldScaleX,timeline,visibleIndexesDirty
hcls VisibleBoundsListener

CLSS protected static org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYChart$Context
 outer org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYChart
cons protected init(org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYChart)
intf org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYChartContext
meth protected org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYChart getChartComponent()
meth public int getNearestTimestampIndex(int,int)
meth public int[][] getVisibleBounds(java.awt.Rectangle)
supr org.netbeans.lib.profiler.charts.ChartComponent$Context

CLSS public abstract interface org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYChartContext
intf org.netbeans.lib.profiler.charts.ChartContext
meth public abstract int getNearestTimestampIndex(int,int)
meth public abstract int[][] getVisibleBounds(java.awt.Rectangle)

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

CLSS public org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYItemMarker
cons public init(int,float,java.awt.Color,float,java.awt.Color,java.awt.Color,int,int)
fld protected final int decorationRadius
fld protected final int line1Width
fld protected final int line2Width
fld protected final int markRadius
fld protected final int maxValueOffset
fld protected final int type
fld protected final java.awt.Color fillColor
fld protected final java.awt.Color line1Color
fld protected final java.awt.Color line2Color
fld protected final java.awt.Stroke line1Stroke
fld protected final java.awt.Stroke line2Stroke
meth public boolean isAppearanceChange(org.netbeans.lib.profiler.charts.ChartItemChange)
meth public boolean isBoundsChange(org.netbeans.lib.profiler.charts.ChartItemChange)
meth public boolean supportsHovering(org.netbeans.lib.profiler.charts.ChartItem)
meth public boolean supportsSelecting(org.netbeans.lib.profiler.charts.ChartItem)
meth public org.netbeans.lib.profiler.charts.swing.LongRect getDirtyBounds(org.netbeans.lib.profiler.charts.ChartItemChange,org.netbeans.lib.profiler.charts.ChartContext)
meth public org.netbeans.lib.profiler.charts.swing.LongRect getItemBounds(org.netbeans.lib.profiler.charts.ChartItem)
meth public org.netbeans.lib.profiler.charts.swing.LongRect getItemBounds(org.netbeans.lib.profiler.charts.ChartItem,org.netbeans.lib.profiler.charts.ChartContext)
meth public org.netbeans.lib.profiler.charts.swing.LongRect getSelectionBounds(org.netbeans.lib.profiler.charts.ItemSelection,org.netbeans.lib.profiler.charts.ChartContext)
meth public org.netbeans.lib.profiler.charts.xy.XYItemSelection getClosestSelection(org.netbeans.lib.profiler.charts.ChartItem,int,int,org.netbeans.lib.profiler.charts.ChartContext)
meth public static org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYItemMarker absolutePainter(int,float,java.awt.Color,float,java.awt.Color,java.awt.Color)
meth public static org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYItemMarker relativePainter(int,float,java.awt.Color,float,java.awt.Color,java.awt.Color,int)
meth public void paintItem(org.netbeans.lib.profiler.charts.ChartItem,java.util.List<org.netbeans.lib.profiler.charts.ItemSelection>,java.util.List<org.netbeans.lib.profiler.charts.ItemSelection>,java.awt.Graphics2D,java.awt.Rectangle,org.netbeans.lib.profiler.charts.ChartContext)
supr org.netbeans.lib.profiler.charts.xy.XYItemPainter$Abstract

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

CLSS public org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYItemsModel
cons public init(org.netbeans.lib.profiler.charts.Timeline)
cons public init(org.netbeans.lib.profiler.charts.Timeline,org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYItem[])
meth public final int getItemsCount()
meth public final org.netbeans.lib.profiler.charts.Timeline getTimeline()
meth public final org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYItem getItem(int)
meth public final void valuesAdded()
meth public final void valuesReset()
meth public void addItems(org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYItem[])
meth public void removeItems(org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYItem[])
supr org.netbeans.lib.profiler.charts.ItemsModel$Abstract
hfds items,timeline

