#Signature file v4.1
#Version 1.26

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

CLSS public abstract interface java.beans.Customizer
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setObject(java.lang.Object)

CLSS public abstract interface java.beans.PropertyChangeListener
intf java.util.EventListener
meth public abstract void propertyChange(java.beans.PropertyChangeEvent)

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

CLSS public abstract interface java.util.EventListener

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

CLSS public abstract interface org.netbeans.modules.javascript2.debug.EditorLineHandler
fld public final static java.lang.String PROP_LINE_NUMBER = "lineNumber"
meth public abstract int getLineNumber()
meth public abstract java.net.URL getURL()
meth public abstract org.openide.filesystems.FileObject getFileObject()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void dispose()
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setLineNumber(int)

CLSS public final org.netbeans.modules.javascript2.debug.ui.EditorContextSetter
innr public abstract interface static EditorContextProvider
meth public static void setContext(javax.swing.JEditorPane,org.netbeans.modules.javascript2.debug.ui.EditorContextSetter$EditorContextProvider,java.lang.Runnable)
supr java.lang.Object
hfds RP

CLSS public abstract interface static org.netbeans.modules.javascript2.debug.ui.EditorContextSetter$EditorContextProvider
 outer org.netbeans.modules.javascript2.debug.ui.EditorContextSetter
meth public abstract org.openide.text.Line$Part getContext()

CLSS public org.netbeans.modules.javascript2.debug.ui.JSUtils
cons public init()
fld public final static java.lang.String JS_MIME_TYPE = "text/javascript"
meth public static java.lang.String getFileName(org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint)
meth public static org.netbeans.modules.javascript2.debug.EditorLineHandler createLineHandler(org.openide.text.Line)
meth public static org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint createLineBreakpoint(org.openide.text.Line)
meth public static org.openide.cookies.LineCookie getLineCookie(org.openide.filesystems.FileObject)
meth public static org.openide.text.Line getCurrentLine()
meth public static org.openide.text.Line getLine(java.lang.String,int)
meth public static org.openide.text.Line getLine(org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint)
meth public static org.openide.text.Line getLine(org.openide.filesystems.FileObject,int)
supr java.lang.Object
hfds LOG

CLSS public abstract interface org.netbeans.modules.javascript2.debug.ui.TextLineHandler
intf org.netbeans.modules.javascript2.debug.EditorLineHandler
meth public abstract org.openide.text.Line getLine()

CLSS public abstract interface org.netbeans.modules.javascript2.debug.ui.breakpoints.ControllerProvider
meth public abstract org.netbeans.spi.debugger.ui.Controller getController()

CLSS public org.netbeans.modules.javascript2.debug.ui.breakpoints.JSLineBreakpointCustomizer
cons public init()
intf java.beans.Customizer
intf org.netbeans.spi.debugger.ui.Controller
meth public boolean cancel()
meth public boolean ok()
meth public static org.netbeans.modules.javascript2.debug.ui.breakpoints.JSLineBreakpointCustomizerPanel getCustomizerComponent(org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint)
meth public void setObject(java.lang.Object)
supr javax.swing.JPanel
hfds b,c

CLSS public org.netbeans.modules.javascript2.debug.ui.breakpoints.JSLineBreakpointCustomizerPanel
cons public init()
cons public init(org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint)
intf org.netbeans.modules.javascript2.debug.ui.breakpoints.ControllerProvider
intf org.openide.util.HelpCtx$Provider
meth protected org.netbeans.spi.debugger.ui.Controller createController()
meth protected org.openide.util.RequestProcessor getUpdateRP()
meth public org.netbeans.spi.debugger.ui.Controller getController()
meth public org.openide.util.HelpCtx getHelpCtx()
supr javax.swing.JPanel
hfds MAX_SAVED_CONDITIONS,RP,conditionCheckBox,conditionComboBox,controller,createBreakpoint,fileLabel,fileTextField,jSeparator1,lb,lineLabel,lineTextField
hcls CustomizerController

CLSS public org.netbeans.modules.javascript2.debug.ui.breakpoints.JSLineBreakpointType
cons public init()
meth public boolean isDefault()
meth public java.lang.String getCategoryDisplayName()
meth public java.lang.String getTypeDisplayName()
meth public javax.swing.JComponent getCustomizer()
meth public org.netbeans.spi.debugger.ui.Controller getController()
supr org.netbeans.spi.debugger.ui.BreakpointType
hfds cust

CLSS public org.netbeans.modules.javascript2.debug.ui.breakpoints.ToggleBreakpointActionProvider
cons public init()
intf java.beans.PropertyChangeListener
meth public java.util.Set getActions()
meth public void doAction(java.lang.Object)
meth public void propertyChange(java.beans.PropertyChangeEvent)
supr org.netbeans.spi.debugger.ActionsProviderSupport

CLSS public abstract org.netbeans.modules.javascript2.debug.ui.models.ViewModelSupport
cons protected init()
fld protected final static java.lang.Object[] EMPTY_CHILDREN
meth protected final void fireChangeEvent(org.netbeans.spi.viewmodel.ModelEvent)
meth protected final void fireChangeEvents(java.util.Collection<org.netbeans.spi.viewmodel.ModelEvent>)
meth protected final void fireChangeEvents(org.netbeans.spi.viewmodel.ModelEvent[])
meth protected final void refresh()
meth public final void addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public final void removeModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public static java.lang.String toHTML(java.lang.String)
meth public static java.lang.String toHTML(java.lang.String,boolean,boolean,java.awt.Color)
supr java.lang.Object
hfds myListeners

CLSS public org.netbeans.modules.javascript2.debug.ui.models.breakpoints.BreakpointNodeActionProvider
cons public init()
intf org.netbeans.spi.viewmodel.NodeActionsProviderFilter
meth public javax.swing.Action[] getActions(org.netbeans.spi.viewmodel.NodeActionsProvider,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public void performDefaultAction(org.netbeans.spi.viewmodel.NodeActionsProvider,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
supr java.lang.Object
hfds CUSTOMIZE_ACTION,GO_TO_SOURCE_ACTION
hcls CustomizePerformer,GoToSourcePerformer

CLSS public org.netbeans.modules.javascript2.debug.ui.models.breakpoints.BreakpointNodeModel
cons public init()
fld public final static java.lang.String CURRENT_LINE_BREAKPOINT = "org/netbeans/modules/debugger/resources/breakpointsView/BreakpointHit.gif"
fld public final static java.lang.String CURRENT_LINE_CONDITIONAL_BREAKPOINT = "org/netbeans/modules/debugger/resources/breakpointsView/ConditionalBreakpointHit.gif"
fld public final static java.lang.String DEACTIVATED_DISABLED_LINE_BREAKPOINT = "org/netbeans/modules/debugger/resources/editor/DisabledBreakpoint_stroke.png"
fld public final static java.lang.String DEACTIVATED_LINE_BREAKPOINT = "org/netbeans/modules/debugger/resources/editor/Breakpoint_stroke.png"
fld public final static java.lang.String DISABLED_CURRENT_LINE_BREAKPOINT = "org/netbeans/modules/debugger/resources/breakpointsView/DisabledBreakpointHit.gif"
fld public final static java.lang.String DISABLED_LINE_BREAKPOINT = "org/netbeans/modules/debugger/resources/breakpointsView/DisabledBreakpoint.gif"
fld public final static java.lang.String DISABLED_LINE_CONDITIONAL_BREAKPOINT = "org/netbeans/modules/debugger/resources/breakpointsView/DisabledConditionalBreakpoint.gif"
fld public final static java.lang.String LINE_BREAKPOINT = "org/netbeans/modules/debugger/resources/breakpointsView/Breakpoint.gif"
fld public final static java.lang.String LINE_CONDITIONAL_BREAKPOINT = "org/netbeans/modules/debugger/resources/breakpointsView/ConditionalBreakpoint.gif"
intf org.netbeans.spi.viewmodel.ExtendedNodeModel
meth public boolean canCopy(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public boolean canCut(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public boolean canRename(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.awt.datatransfer.Transferable clipboardCopy(java.lang.Object) throws java.io.IOException,org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.awt.datatransfer.Transferable clipboardCut(java.lang.Object) throws java.io.IOException,org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.String getDisplayName(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.String getIconBase(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.String getIconBaseWithExtension(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.String getShortDescription(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public org.openide.util.datatransfer.PasteType[] getPasteTypes(java.lang.Object,java.awt.datatransfer.Transferable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public void addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public void removeModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public void setName(java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
supr java.lang.Object
hfds listeners

CLSS public abstract org.netbeans.modules.javascript2.debug.ui.tooltip.AbstractJSToolTipAnnotation
cons public init()
meth protected abstract org.openide.util.Pair<java.lang.String,java.lang.Object> evaluate(java.lang.String,org.netbeans.api.debugger.DebuggerEngine)
meth protected abstract void handleToolTipClose(org.netbeans.api.debugger.DebuggerEngine,org.netbeans.editor.ext.ToolTipSupport)
meth public java.lang.String getAnnotationType()
meth public java.lang.String getShortDescription()
supr org.openide.text.Annotation
hfds JS_KEYWORDS,MAX_TOOLTIP_TEXT,RP

CLSS public abstract org.netbeans.spi.debugger.ActionsProvider
cons public init()
innr public abstract interface static !annotation Registration
innr public abstract interface static !annotation Registrations
meth public abstract boolean isEnabled(java.lang.Object)
meth public abstract java.util.Set getActions()
meth public abstract void addActionsProviderListener(org.netbeans.spi.debugger.ActionsProviderListener)
meth public abstract void doAction(java.lang.Object)
meth public abstract void removeActionsProviderListener(org.netbeans.spi.debugger.ActionsProviderListener)
meth public void postAction(java.lang.Object,java.lang.Runnable)
supr java.lang.Object
hfds debuggerActionsRP
hcls ContextAware

CLSS public abstract org.netbeans.spi.debugger.ActionsProviderSupport
cons public init()
meth protected final void setEnabled(java.lang.Object,boolean)
meth protected void fireActionStateChanged(java.lang.Object,boolean)
meth public abstract void doAction(java.lang.Object)
meth public boolean isEnabled(java.lang.Object)
meth public final void addActionsProviderListener(org.netbeans.spi.debugger.ActionsProviderListener)
meth public final void removeActionsProviderListener(org.netbeans.spi.debugger.ActionsProviderListener)
supr org.netbeans.spi.debugger.ActionsProvider
hfds enabled,listeners

CLSS public abstract org.netbeans.spi.debugger.ui.BreakpointType
cons public init()
innr public abstract interface static !annotation Registration
meth public abstract boolean isDefault()
meth public abstract java.lang.String getCategoryDisplayName()
meth public abstract javax.swing.JComponent getCustomizer()
meth public java.lang.String getTypeDisplayName()
meth public org.netbeans.spi.debugger.ui.Controller getController()
supr java.lang.Object
hcls ContextAware

CLSS public abstract interface org.netbeans.spi.debugger.ui.Controller
fld public final static java.lang.String PROP_VALID = "valid"
meth public abstract boolean cancel()
meth public abstract boolean isValid()
meth public abstract boolean ok()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public abstract interface org.netbeans.spi.viewmodel.ExtendedNodeModel
intf org.netbeans.spi.viewmodel.NodeModel
meth public abstract boolean canCopy(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract boolean canCut(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract boolean canRename(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.awt.datatransfer.Transferable clipboardCopy(java.lang.Object) throws java.io.IOException,org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.awt.datatransfer.Transferable clipboardCut(java.lang.Object) throws java.io.IOException,org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String getIconBaseWithExtension(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract org.openide.util.datatransfer.PasteType[] getPasteTypes(java.lang.Object,java.awt.datatransfer.Transferable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void setName(java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException

CLSS public abstract interface org.netbeans.spi.viewmodel.Model

CLSS public abstract interface org.netbeans.spi.viewmodel.NodeActionsProviderFilter
intf org.netbeans.spi.viewmodel.Model
meth public abstract javax.swing.Action[] getActions(org.netbeans.spi.viewmodel.NodeActionsProvider,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void performDefaultAction(org.netbeans.spi.viewmodel.NodeActionsProvider,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException

CLSS public abstract interface org.netbeans.spi.viewmodel.NodeModel
intf org.netbeans.spi.viewmodel.Model
meth public abstract java.lang.String getDisplayName(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String getIconBase(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String getShortDescription(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void removeModelListener(org.netbeans.spi.viewmodel.ModelListener)

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

