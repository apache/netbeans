#Signature file v4.1
#Version 1.99

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

CLSS public abstract interface javax.swing.ListModel<%0 extends java.lang.Object>
meth public abstract int getSize()
meth public abstract void addListDataListener(javax.swing.event.ListDataListener)
meth public abstract void removeListDataListener(javax.swing.event.ListDataListener)
meth public abstract {javax.swing.ListModel%0} getElementAt(int)

CLSS public abstract interface javax.swing.event.DocumentListener
intf java.util.EventListener
meth public abstract void changedUpdate(javax.swing.event.DocumentEvent)
meth public abstract void insertUpdate(javax.swing.event.DocumentEvent)
meth public abstract void removeUpdate(javax.swing.event.DocumentEvent)

CLSS public abstract org.netbeans.modules.apisupport.project.api.BasicVisualPanel
cons protected init(org.openide.WizardDescriptor)
meth protected final void markInvalid()
meth protected final void markValid()
meth protected final void setError(java.lang.String)
meth protected final void setInfo(java.lang.String,boolean)
meth protected final void setWarning(java.lang.String)
meth protected final void setWarning(java.lang.String,boolean)
meth public final org.openide.WizardDescriptor getSettings()
supr javax.swing.JPanel
hfds settings

CLSS public abstract org.netbeans.modules.apisupport.project.api.BasicWizardPanel
cons protected init(org.openide.WizardDescriptor)
intf java.beans.PropertyChangeListener
intf org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor>
meth protected org.openide.WizardDescriptor getSettings()
meth protected void fireChange()
meth public boolean isValid()
meth public org.openide.util.HelpCtx getHelp()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void readSettings(org.openide.WizardDescriptor)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void setSettings(org.openide.WizardDescriptor)
meth public void storeSettings(org.openide.WizardDescriptor)
supr java.lang.Object
hfds changeSupport,settings,valid

CLSS public org.netbeans.modules.apisupport.project.api.BrandingUtils
meth public static void openBrandingEditor(java.lang.String,org.netbeans.api.project.Project,org.netbeans.modules.apisupport.project.spi.BrandingModel)
supr java.lang.Object
hfds project2dialog

CLSS public final org.netbeans.modules.apisupport.project.api.EditableManifest
cons public init()
cons public init(java.io.InputStream) throws java.io.IOException
meth public java.lang.String getAttribute(java.lang.String,java.lang.String)
meth public java.util.Set<java.lang.String> getAttributeNames(java.lang.String)
meth public java.util.Set<java.lang.String> getSectionNames()
meth public void addSection(java.lang.String)
meth public void removeAttribute(java.lang.String,java.lang.String)
meth public void removeSection(java.lang.String)
meth public void setAttribute(java.lang.String,java.lang.String,java.lang.String)
meth public void write(java.io.OutputStream) throws java.io.IOException
supr java.lang.Object
hfds MANIFEST_VERSION,MANIFEST_VERSION_VALUE,RET,mainSection,sections
hcls Line,Section

CLSS public final org.netbeans.modules.apisupport.project.api.LayerHandle
cons public init(org.netbeans.api.project.Project,org.openide.filesystems.FileObject)
meth public boolean isAutosave()
meth public java.lang.String newLayerPath()
meth public java.lang.String toString()
meth public org.openide.filesystems.FileObject getLayerFile()
meth public org.openide.filesystems.FileSystem explicitLayer(boolean)
meth public org.openide.filesystems.FileSystem layer(boolean)
meth public static org.netbeans.modules.apisupport.project.api.LayerHandle forProject(org.netbeans.api.project.Project)
meth public static org.openide.filesystems.FileObject createLayer(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public void save() throws java.io.IOException
meth public void setAutosave(boolean)
supr java.lang.Object
hfds NO_ACTIONS,autosave,cookie,fs,layerHandleCache,layerXML,project,ref
hcls DualLayers,HandleRef,SingleLayer

CLSS public final org.netbeans.modules.apisupport.project.api.ManifestManager
fld public final static java.lang.String AUTO_UPDATE_SHOW_IN_CLIENT = "AutoUpdate-Show-In-Client"
fld public final static java.lang.String BUNDLE_EXPORT_PACKAGE = "Export-Package"
fld public final static java.lang.String BUNDLE_IMPORT_PACKAGE = "Import-Package"
fld public final static java.lang.String BUNDLE_LOCALIZATION = "Bundle-Localization"
fld public final static java.lang.String BUNDLE_REQUIRE_BUNDLE = "Require-Bundle"
fld public final static java.lang.String BUNDLE_SYMBOLIC_NAME = "Bundle-SymbolicName"
fld public final static java.lang.String BUNDLE_VERSION = "Bundle-Version"
fld public final static java.lang.String CLASS_PATH = "Class-Path"
fld public final static java.lang.String GENERATED_LAYER_PATH = "META-INF/generated-layer.xml"
fld public final static java.lang.String OPENIDE_MODULE = "OpenIDE-Module"
fld public final static java.lang.String OPENIDE_MODULE_FRIENDS = "OpenIDE-Module-Friends"
fld public final static java.lang.String OPENIDE_MODULE_IMPLEMENTATION_VERSION = "OpenIDE-Module-Implementation-Version"
fld public final static java.lang.String OPENIDE_MODULE_LAYER = "OpenIDE-Module-Layer"
fld public final static java.lang.String OPENIDE_MODULE_LOCALIZING_BUNDLE = "OpenIDE-Module-Localizing-Bundle"
fld public final static java.lang.String OPENIDE_MODULE_MODULE_DEPENDENCIES = "OpenIDE-Module-Module-Dependencies"
fld public final static java.lang.String OPENIDE_MODULE_NEEDS = "OpenIDE-Module-Needs"
fld public final static java.lang.String OPENIDE_MODULE_PROVIDES = "OpenIDE-Module-Provides"
fld public final static java.lang.String OPENIDE_MODULE_PUBLIC_PACKAGES = "OpenIDE-Module-Public-Packages"
fld public final static java.lang.String OPENIDE_MODULE_REQUIRES = "OpenIDE-Module-Requires"
fld public final static java.lang.String OPENIDE_MODULE_SPECIFICATION_VERSION = "OpenIDE-Module-Specification-Version"
fld public final static org.netbeans.modules.apisupport.project.api.ManifestManager NULL_INSTANCE
fld public final static org.netbeans.modules.apisupport.project.api.ManifestManager$PackageExport[] EMPTY_EXPORTED_PACKAGES
innr public final static PackageExport
meth public boolean isDeprecated()
meth public java.lang.Boolean getAutoUpdateShowInClient()
meth public java.lang.String getClassPath()
meth public java.lang.String getCodeNameBase()
meth public java.lang.String getGeneratedLayer()
meth public java.lang.String getImplementationVersion()
meth public java.lang.String getLayer()
meth public java.lang.String getLocalizingBundle()
meth public java.lang.String getProvidedTokensString()
meth public java.lang.String getReleaseVersion()
meth public java.lang.String getSpecificationVersion()
meth public java.lang.String[] getFriends()
meth public java.lang.String[] getNeededTokens()
meth public java.lang.String[] getProvidedTokens()
meth public java.lang.String[] getRequiredTokens()
meth public java.util.Set<org.openide.modules.Dependency> getModuleDependencies()
meth public org.netbeans.modules.apisupport.project.api.ManifestManager$PackageExport[] getPublicPackages()
meth public static org.netbeans.modules.apisupport.project.api.ManifestManager getInstance(java.io.File,boolean)
meth public static org.netbeans.modules.apisupport.project.api.ManifestManager getInstance(java.util.jar.Manifest,boolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.apisupport.project.api.ManifestManager getInstance(java.util.jar.Manifest,boolean,boolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.apisupport.project.api.ManifestManager getInstanceFromJAR(java.io.File)
meth public static org.netbeans.modules.apisupport.project.api.ManifestManager getInstanceFromJAR(java.io.File,boolean)
supr java.lang.Object
hfds JAVA_PLATFORM_PACKAGES,LOG,autoUpdateShowInClient,classPath,codeNameBase,deprecated,friendNames,generatedLayer,implementationVersion,layer,localizingBundle,moduleDependencies,neededTokens,provTokens,provTokensString,publicPackages,releaseVersion,requiredTokens,specificationVersion

CLSS public final static org.netbeans.modules.apisupport.project.api.ManifestManager$PackageExport
 outer org.netbeans.modules.apisupport.project.api.ManifestManager
cons public init(java.lang.String,boolean)
meth public boolean isRecursive()
meth public java.lang.String getPackage()
meth public java.lang.String toString()
supr java.lang.Object
hfds pkg,recursive

CLSS public final org.netbeans.modules.apisupport.project.api.NodeFactoryUtils
meth public static java.lang.String computeAnnotatedHtmlDisplayName(java.lang.String,java.util.Set<? extends org.openide.filesystems.FileObject>)
meth public static org.openide.nodes.Node createLayersNode(org.netbeans.api.project.Project)
meth public static org.openide.nodes.Node createSpecialFileNode(org.openide.nodes.Node,java.lang.String)
supr java.lang.Object
hcls SpecialFileNode

CLSS public final org.netbeans.modules.apisupport.project.api.UIUtil
fld public final static java.lang.String LIBRARIES_ICON = "org/netbeans/modules/apisupport/project/api/libraries.gif"
fld public final static java.lang.String TEMPLATE_ACTION_ID = "newAction"
fld public final static java.lang.String TEMPLATE_CATEGORY = "nbm-specific"
fld public final static java.lang.String TEMPLATE_FOLDER = "NetBeansModuleDevelopment"
fld public final static java.lang.String TEMPLATE_WINDOW_ID = "newWindow"
fld public final static java.lang.String WAIT_VALUE
innr public abstract interface static WaitingModel
innr public abstract static DocumentAdapter
meth public static boolean hasOnlyValue(javax.swing.ListModel,java.lang.Object)
meth public static boolean isWaitModel(javax.swing.ListModel)
meth public static javax.swing.ComboBoxModel createComboWaitModel()
meth public static javax.swing.JFileChooser getIconFileChooser()
meth public static javax.swing.ListModel createListWaitModel()
supr java.lang.Object
hfds iconChooser
hcls IconFileChooser,IconFilter

CLSS public abstract static org.netbeans.modules.apisupport.project.api.UIUtil$DocumentAdapter
 outer org.netbeans.modules.apisupport.project.api.UIUtil
cons public init()
intf javax.swing.event.DocumentListener
meth public void changedUpdate(javax.swing.event.DocumentEvent)
meth public void removeUpdate(javax.swing.event.DocumentEvent)
supr java.lang.Object

CLSS public abstract interface static org.netbeans.modules.apisupport.project.api.UIUtil$WaitingModel
 outer org.netbeans.modules.apisupport.project.api.UIUtil
intf javax.swing.ListModel
meth public abstract boolean isWaiting()

CLSS public final org.netbeans.modules.apisupport.project.api.Util
fld public final static org.openide.ErrorManager err
meth public static java.util.Comparator<org.netbeans.api.project.Project> projectDisplayNameComparator()
meth public static java.util.jar.Manifest getManifest(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.modules.apisupport.project.api.EditableManifest loadManifest(org.openide.filesystems.FileObject) throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.project.support.ant.EditableProperties loadProperties(org.openide.filesystems.FileObject) throws java.io.IOException
meth public static org.openide.filesystems.FileObject getResource(org.netbeans.api.project.Project,java.lang.String)
meth public static org.openide.filesystems.FileObject getResourceDirectory(org.netbeans.api.project.Project)
meth public static void storeManifest(org.openide.filesystems.FileObject,org.netbeans.modules.apisupport.project.api.EditableManifest) throws java.io.IOException
meth public static void storeProperties(org.openide.filesystems.FileObject,org.netbeans.spi.project.support.ant.EditableProperties) throws java.io.IOException
supr java.lang.Object

CLSS public abstract org.netbeans.modules.apisupport.project.spi.BrandingModel
cons protected init()
fld protected java.util.Locale locale
meth protected abstract boolean isBrandingEnabledRefresh()
meth protected abstract java.io.File getProjectDirectoryFile()
meth protected abstract java.lang.String loadName()
meth protected abstract java.lang.String loadTitle()
meth protected abstract org.netbeans.modules.apisupport.project.spi.BrandingSupport createBranding() throws java.io.IOException
meth protected final java.lang.String getIconLocation()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth protected java.lang.String getSimpleName()
meth public abstract org.netbeans.api.project.Project getProject()
meth public abstract void updateProjectInternationalizationLocales()
meth public final boolean isBrandingEnabled()
meth public final boolean isBundleBranded(java.lang.String,java.lang.String)
meth public final boolean isBundleLocallyBranded(java.lang.String,java.lang.String)
meth public final boolean isKeyBranded(java.lang.String,java.lang.String,java.lang.String)
meth public final boolean isKeyLocallyBranded(java.lang.String,java.lang.String,java.lang.String)
meth public final java.lang.String getKeyValue(java.lang.String,java.lang.String,java.lang.String)
meth public final java.lang.String getLocalizedKeyValue(java.lang.String,java.lang.String,java.lang.String)
meth public final java.lang.String getName()
meth public final java.lang.String getTitle()
meth public final java.net.URL getIconSource(int)
meth public final java.util.Set<java.io.File> getBrandableJars()
meth public final org.netbeans.modules.apisupport.project.spi.BrandingSupport$BrandedFile getSplash()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final org.netbeans.modules.apisupport.project.spi.BrandingSupport$BundleKey getGeneralBundleKeyForModification(java.lang.String,java.lang.String,java.lang.String)
meth public final org.netbeans.modules.apisupport.project.spi.BrandingSupport$BundleKey getGeneralLocalizedBundleKeyForModification(java.lang.String,java.lang.String,java.lang.String)
meth public final org.netbeans.modules.apisupport.project.spi.BrandingSupport$BundleKey getSplashHeight()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final org.netbeans.modules.apisupport.project.spi.BrandingSupport$BundleKey getSplashProgressBarBounds()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final org.netbeans.modules.apisupport.project.spi.BrandingSupport$BundleKey getSplashProgressBarColor()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final org.netbeans.modules.apisupport.project.spi.BrandingSupport$BundleKey getSplashProgressBarCornerColor()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final org.netbeans.modules.apisupport.project.spi.BrandingSupport$BundleKey getSplashProgressBarEdgeColor()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final org.netbeans.modules.apisupport.project.spi.BrandingSupport$BundleKey getSplashRunningTextBounds()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final org.netbeans.modules.apisupport.project.spi.BrandingSupport$BundleKey getSplashRunningTextColor()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final org.netbeans.modules.apisupport.project.spi.BrandingSupport$BundleKey getSplashRunningTextFontSize()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final org.netbeans.modules.apisupport.project.spi.BrandingSupport$BundleKey getSplashShowProgressBar()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final org.netbeans.modules.apisupport.project.spi.BrandingSupport$BundleKey getSplashWidth()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final org.netbeans.modules.apisupport.project.spi.BrandingSupport$BundleKey getWsEnableAutoSlideInMinimizedMode()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final org.netbeans.modules.apisupport.project.spi.BrandingSupport$BundleKey getWsEnableClosingEditors()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final org.netbeans.modules.apisupport.project.spi.BrandingSupport$BundleKey getWsEnableClosingViews()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final org.netbeans.modules.apisupport.project.spi.BrandingSupport$BundleKey getWsEnableDragAndDrop()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final org.netbeans.modules.apisupport.project.spi.BrandingSupport$BundleKey getWsEnableEditorModeDnD()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final org.netbeans.modules.apisupport.project.spi.BrandingSupport$BundleKey getWsEnableEditorModeUndocking()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final org.netbeans.modules.apisupport.project.spi.BrandingSupport$BundleKey getWsEnableFloating()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final org.netbeans.modules.apisupport.project.spi.BrandingSupport$BundleKey getWsEnableMaximization()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final org.netbeans.modules.apisupport.project.spi.BrandingSupport$BundleKey getWsEnableMinimumSize()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final org.netbeans.modules.apisupport.project.spi.BrandingSupport$BundleKey getWsEnableModeClosing()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final org.netbeans.modules.apisupport.project.spi.BrandingSupport$BundleKey getWsEnableModeSliding()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final org.netbeans.modules.apisupport.project.spi.BrandingSupport$BundleKey getWsEnableResizing()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final org.netbeans.modules.apisupport.project.spi.BrandingSupport$BundleKey getWsEnableSliding()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final org.netbeans.modules.apisupport.project.spi.BrandingSupport$BundleKey getWsEnableViewModeDnD()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final org.netbeans.modules.apisupport.project.spi.BrandingSupport$BundleKey getWsEnableViewModeUndocking()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final void addChangeListener(javax.swing.event.ChangeListener)
meth public final void addModifiedGeneralBundleKey(org.netbeans.modules.apisupport.project.spi.BrandingSupport$BundleKey)
meth public final void addModifiedInternationalizedBundleKey(org.netbeans.modules.apisupport.project.spi.BrandingSupport$BundleKey)
meth public final void brandingEnabledRefresh()
meth public final void initName(boolean)
meth public final void initTitle(boolean)
meth public final void removeChangeListener(javax.swing.event.ChangeListener)
meth public java.util.Locale getLocale()
meth public void doSave()
meth public void init()
meth public void refreshLocalizedBundles(java.util.Locale)
meth public void reloadProperties()
meth public void setBrandingEnabled(boolean)
meth public void setIconSource(int,java.net.URL)
meth public void setName(java.lang.String)
meth public void setTitle(java.lang.String)
meth public void store() throws java.io.IOException
supr java.lang.Object
hfds branding,brandingEnabled,changeSupport,currentVersion,generalResourceBundleKeys,icon1024,icon16,icon256,icon32,icon48,icon512,internationalizedResourceBundleKeys,mainWindowTitle,mainWindowTitleNoProject,name,productInformation,splash,splashHeight,splashKeys,splashProgressBarBounds,splashProgressBarColor,splashProgressBarCornerColor,splashProgressBarEdgeColor,splashRunningTextBounds,splashRunningTextColor,splashRunningTextFontSize,splashShowProgressBar,splashWidth,splashWindowTitle,title,winsysKeys,wsEnableAutoSlideInMinimizedMode,wsEnableClosingEditors,wsEnableClosingViews,wsEnableDragAndDrop,wsEnableEditorModeDnD,wsEnableEditorModeUndocking,wsEnableFloating,wsEnableMaximization,wsEnableMinimumSize,wsEnableModeClosing,wsEnableModeSliding,wsEnableResizing,wsEnableSliding,wsEnableViewModeDnD,wsEnableViewModeUndocking

CLSS public abstract org.netbeans.modules.apisupport.project.spi.BrandingSupport
cons protected init(org.netbeans.api.project.Project,java.lang.String)
fld protected java.util.Locale locale
innr protected abstract interface static BrandableModule
innr public final BrandedFile
innr public final BundleKey
meth protected abstract java.util.Map<java.lang.String,java.lang.String> localizingBundle(org.netbeans.modules.apisupport.project.spi.BrandingSupport$BrandableModule)
meth protected abstract java.util.Set<java.io.File> getBrandableJars()
meth protected abstract java.util.Set<org.netbeans.modules.apisupport.project.spi.BrandingSupport$BrandableModule> loadModules() throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth protected abstract org.netbeans.modules.apisupport.project.spi.BrandingSupport$BrandableModule findBrandableModule(java.lang.String)
supr java.lang.Object
hfds BUNDLE_NAME_PREFIX,BUNDLE_NAME_SUFFIX,LOCK,brandedBundleKeys,brandedFiles,brandedModules,brandingDir,brandingPath,cacheLoaded,isCached,localizedBrandedBundleKeys,project

CLSS protected abstract interface static org.netbeans.modules.apisupport.project.spi.BrandingSupport$BrandableModule
 outer org.netbeans.modules.apisupport.project.spi.BrandingSupport
meth public abstract java.io.File getJarLocation()
meth public abstract java.lang.String getCodeNameBase()
meth public abstract java.lang.String getRelativePath()

CLSS public final org.netbeans.modules.apisupport.project.spi.BrandingSupport$BrandedFile
 outer org.netbeans.modules.apisupport.project.spi.BrandingSupport
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.net.URL getBrandingSource()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public void setBrandingSource(java.net.URL)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds brandingSource,entryPath,modified,moduleEntry

CLSS public final org.netbeans.modules.apisupport.project.spi.BrandingSupport$BundleKey
 outer org.netbeans.modules.apisupport.project.spi.BrandingSupport
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public void setValue(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds brandingBundle,key,modified,moduleEntry,value

CLSS public abstract interface org.netbeans.modules.apisupport.project.spi.ExecProject
meth public abstract !varargs org.openide.util.Task execute(java.lang.String[]) throws java.io.IOException

CLSS public org.netbeans.modules.apisupport.project.spi.LayerUtil
fld public final static java.lang.String HIDDEN = "_hidden"
fld public final static java.lang.String LAYER_ICON = "org/netbeans/modules/apisupport/project/spi/layerObject.gif"
meth public static java.lang.String findGeneratedName(org.openide.filesystems.FileObject,java.lang.String)
meth public static java.lang.String generateBundleKeyForFile(java.lang.String)
meth public static java.lang.String getAnnotatedName(org.openide.filesystems.FileObject)
meth public static java.util.List<java.net.URL> layersOf(java.io.File) throws java.io.IOException
meth public static org.openide.filesystems.FileSystem mergeFilesystems(org.openide.filesystems.FileSystem,java.util.Collection<org.openide.filesystems.FileSystem>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds XML_LIKE_TYPES

CLSS public abstract interface org.netbeans.modules.apisupport.project.spi.NbModuleProvider
innr public final static ModuleDependency
meth public abstract boolean hasDependency(java.lang.String) throws java.io.IOException
meth public abstract java.io.File getClassesDirectory()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.io.File getModuleJarLocation()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getCodeNameBase()
meth public abstract java.lang.String getProjectFilePath()
meth public abstract java.lang.String getReleaseDirectoryPath()
meth public abstract java.lang.String getResourceDirectoryPath(boolean)
meth public abstract java.lang.String getSourceDirectoryPath()
meth public abstract java.lang.String getSpecVersion()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getTestSourceDirectoryPath()
meth public abstract org.openide.filesystems.FileObject getManifestFile()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.openide.filesystems.FileObject getReleaseDirectory() throws java.io.IOException
meth public abstract org.openide.filesystems.FileObject getSourceDirectory()
meth public abstract org.openide.filesystems.FileSystem getEffectiveSystemFilesystem() throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.openide.modules.SpecificationVersion getDependencyVersion(java.lang.String) throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract void addDependencies(org.netbeans.modules.apisupport.project.spi.NbModuleProvider$ModuleDependency[]) throws java.io.IOException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void addModulesToTargetPlatform(org.netbeans.modules.apisupport.project.spi.NbModuleProvider$ModuleDependency[]) throws java.io.IOException
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public final static org.netbeans.modules.apisupport.project.spi.NbModuleProvider$ModuleDependency
 outer org.netbeans.modules.apisupport.project.spi.NbModuleProvider
cons public init(java.lang.String,java.lang.String,org.openide.modules.SpecificationVersion,boolean)
cons public init(java.lang.String,java.lang.String,org.openide.modules.SpecificationVersion,boolean,java.lang.String)
meth public boolean isTestDependency()
meth public boolean isUseInCompiler()
meth public java.lang.String getClusterName()
meth public java.lang.String getCodeNameBase()
meth public java.lang.String getReleaseVersion()
meth public org.openide.modules.SpecificationVersion getVersion()
meth public void setTestDependency(boolean)
supr java.lang.Object
hfds clusterName,codeNameBase,releaseVersion,testDependency,useInCompiler,version

CLSS public abstract interface org.netbeans.modules.apisupport.project.spi.NbProjectProvider
meth public abstract boolean isNbPlatformApplication()
meth public abstract boolean isSuiteComponent()

CLSS public final org.netbeans.modules.apisupport.project.spi.NbRefactoringContext
cons public init(org.openide.filesystems.FileObject,java.lang.String,java.lang.String)
meth public java.lang.String getNewPackagePath()
meth public java.lang.String getOldPackagePath()
meth public org.openide.filesystems.FileObject getFileToRefactored()
supr java.lang.Object
hfds fileToRefactored,newPackagePath,oldPackagePath

CLSS public abstract interface org.netbeans.modules.apisupport.project.spi.NbRefactoringProvider
innr public abstract static ProjectFileRefactoring
meth public abstract java.util.List<org.netbeans.modules.apisupport.project.spi.NbRefactoringProvider$ProjectFileRefactoring> getProjectFilesRefactoring(org.netbeans.modules.apisupport.project.spi.NbRefactoringContext)

CLSS public abstract static org.netbeans.modules.apisupport.project.spi.NbRefactoringProvider$ProjectFileRefactoring
 outer org.netbeans.modules.apisupport.project.spi.NbRefactoringProvider
cons public init(org.openide.filesystems.FileObject)
meth public abstract java.lang.String getDisplayText()
meth public abstract void performChange()
meth public org.openide.filesystems.FileObject getParentFile()
supr java.lang.Object
hfds parentFile

CLSS public abstract interface org.netbeans.modules.apisupport.project.spi.PlatformJarProvider
meth public abstract java.util.Set<java.io.File> getPlatformJars() throws java.io.IOException

CLSS public org.openide.DialogDescriptor
cons public init(java.lang.Object,java.lang.String)
cons public init(java.lang.Object,java.lang.String,boolean,int,java.lang.Object,int,org.openide.util.HelpCtx,java.awt.event.ActionListener)
cons public init(java.lang.Object,java.lang.String,boolean,int,java.lang.Object,java.awt.event.ActionListener)
cons public init(java.lang.Object,java.lang.String,boolean,java.awt.event.ActionListener)
cons public init(java.lang.Object,java.lang.String,boolean,java.lang.Object[],java.lang.Object,int,org.openide.util.HelpCtx,java.awt.event.ActionListener)
cons public init(java.lang.Object,java.lang.String,boolean,java.lang.Object[],java.lang.Object,int,org.openide.util.HelpCtx,java.awt.event.ActionListener,boolean)
fld public final static int BOTTOM_ALIGN = 0
fld public final static int DEFAULT_ALIGN = 0
fld public final static int RIGHT_ALIGN = 1
fld public final static java.lang.String PROP_BUTTON_LISTENER = "buttonListener"
fld public final static java.lang.String PROP_CLOSING_OPTIONS = "closingOptions"
fld public final static java.lang.String PROP_HELP_CTX = "helpCtx"
fld public final static java.lang.String PROP_LEAF = "leaf"
fld public final static java.lang.String PROP_MODAL = "modal"
fld public final static java.lang.String PROP_OPTIONS_ALIGN = "optionsAlign"
intf org.openide.util.HelpCtx$Provider
meth public boolean isLeaf()
meth public boolean isModal()
meth public int getOptionsAlign()
meth public java.awt.event.ActionListener getButtonListener()
meth public java.lang.Object[] getClosingOptions()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void setButtonListener(java.awt.event.ActionListener)
meth public void setClosingOptions(java.lang.Object[])
meth public void setHelpCtx(org.openide.util.HelpCtx)
meth public void setLeaf(boolean)
meth public void setModal(boolean)
meth public void setOptionsAlign(int)
supr org.openide.NotifyDescriptor
hfds DEFAULT_CLOSING_OPTIONS,buttonListener,closingOptions,helpCtx,leaf,modal,optionsAlign

CLSS public org.openide.NotifyDescriptor
cons public init(java.lang.Object,java.lang.String,int,int,java.lang.Object[],java.lang.Object)
fld public final static int DEFAULT_OPTION = -1
fld public final static int ERROR_MESSAGE = 0
fld public final static int INFORMATION_MESSAGE = 1
fld public final static int OK_CANCEL_OPTION = 2
fld public final static int PLAIN_MESSAGE = -1
fld public final static int QUESTION_MESSAGE = 3
fld public final static int WARNING_MESSAGE = 2
fld public final static int YES_NO_CANCEL_OPTION = 1
fld public final static int YES_NO_OPTION = 0
fld public final static java.lang.Object CANCEL_OPTION
fld public final static java.lang.Object CLOSED_OPTION
fld public final static java.lang.Object NO_OPTION
fld public final static java.lang.Object OK_OPTION
fld public final static java.lang.Object YES_OPTION
fld public final static java.lang.String PROP_DETAIL = "detail"
fld public final static java.lang.String PROP_ERROR_NOTIFICATION = "errorNotification"
fld public final static java.lang.String PROP_INFO_NOTIFICATION = "infoNotification"
fld public final static java.lang.String PROP_MESSAGE = "message"
fld public final static java.lang.String PROP_MESSAGE_TYPE = "messageType"
fld public final static java.lang.String PROP_NO_DEFAULT_CLOSE = "noDefaultClose"
fld public final static java.lang.String PROP_OPTIONS = "options"
fld public final static java.lang.String PROP_OPTION_TYPE = "optionType"
fld public final static java.lang.String PROP_TITLE = "title"
fld public final static java.lang.String PROP_VALID = "valid"
fld public final static java.lang.String PROP_VALUE = "value"
fld public final static java.lang.String PROP_WARNING_NOTIFICATION = "warningNotification"
innr public final static ComposedInput
innr public final static Exception
innr public final static PasswordLine
innr public final static QuickPick
innr public static Confirmation
innr public static InputLine
innr public static Message
meth protected static java.lang.String getTitleForType(int)
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void initialize()
meth public boolean isNoDefaultClose()
meth public final boolean isValid()
meth public final org.openide.NotificationLineSupport createNotificationLineSupport()
meth public final org.openide.NotificationLineSupport getNotificationLineSupport()
meth public final void setValid(boolean)
meth public int getMessageType()
meth public int getOptionType()
meth public java.lang.Object getDefaultValue()
meth public java.lang.Object getMessage()
meth public java.lang.Object getValue()
meth public java.lang.Object[] getAdditionalOptions()
meth public java.lang.Object[] getOptions()
meth public java.lang.String getTitle()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setAdditionalOptions(java.lang.Object[])
meth public void setMessage(java.lang.Object)
meth public void setMessageType(int)
meth public void setNoDefaultClose(boolean)
meth public void setOptionType(int)
meth public void setOptions(java.lang.Object[])
meth public void setTitle(java.lang.String)
meth public void setValue(java.lang.Object)
supr java.lang.Object
hfds MAXIMUM_TEXT_WIDTH,SIZE_PREFERRED_HEIGHT,SIZE_PREFERRED_WIDTH,adOptions,changeSupport,defaultValue,errMsg,infoMsg,message,messageType,noDefaultClose,notificationLineSupport,optionType,options,title,valid,value,warnMsg

CLSS public org.openide.WizardDescriptor
cons protected init()
cons public <%0 extends java.lang.Object> init(org.openide.WizardDescriptor$Iterator<{%%0}>,{%%0})
cons public <%0 extends java.lang.Object> init(org.openide.WizardDescriptor$Panel<{%%0}>[],{%%0})
cons public init(org.openide.WizardDescriptor$Iterator<org.openide.WizardDescriptor>)
cons public init(org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor>[])
fld public final static java.lang.Object FINISH_OPTION
fld public final static java.lang.Object NEXT_OPTION
fld public final static java.lang.Object PREVIOUS_OPTION
fld public final static java.lang.String PROP_AUTO_WIZARD_STYLE = "WizardPanel_autoWizardStyle"
fld public final static java.lang.String PROP_CONTENT_BACK_COLOR = "WizardPanel_contentBackColor"
fld public final static java.lang.String PROP_CONTENT_DATA = "WizardPanel_contentData"
fld public final static java.lang.String PROP_CONTENT_DISPLAYED = "WizardPanel_contentDisplayed"
fld public final static java.lang.String PROP_CONTENT_FOREGROUND_COLOR = "WizardPanel_contentForegroundColor"
fld public final static java.lang.String PROP_CONTENT_NUMBERED = "WizardPanel_contentNumbered"
fld public final static java.lang.String PROP_CONTENT_SELECTED_INDEX = "WizardPanel_contentSelectedIndex"
fld public final static java.lang.String PROP_ERROR_MESSAGE = "WizardPanel_errorMessage"
fld public final static java.lang.String PROP_HELP_DISPLAYED = "WizardPanel_helpDisplayed"
fld public final static java.lang.String PROP_HELP_URL = "WizardPanel_helpURL"
fld public final static java.lang.String PROP_IMAGE = "WizardPanel_image"
fld public final static java.lang.String PROP_IMAGE_ALIGNMENT = "WizardPanel_imageAlignment"
fld public final static java.lang.String PROP_INFO_MESSAGE = "WizardPanel_infoMessage"
fld public final static java.lang.String PROP_LEFT_DIMENSION = "WizardPanel_leftDimension"
fld public final static java.lang.String PROP_WARNING_MESSAGE = "WizardPanel_warningMessage"
innr public abstract interface static AsynchronousInstantiatingIterator
innr public abstract interface static AsynchronousValidatingPanel
innr public abstract interface static BackgroundInstantiatingIterator
innr public abstract interface static ExtendedAsynchronousValidatingPanel
innr public abstract interface static FinishPanel
innr public abstract interface static FinishablePanel
innr public abstract interface static InstantiatingIterator
innr public abstract interface static Iterator
innr public abstract interface static Panel
innr public abstract interface static ProgressInstantiatingIterator
innr public abstract interface static ValidatingPanel
innr public static ArrayIterator
meth protected void initialize()
meth protected void updateState()
meth public final <%0 extends java.lang.Object> void setPanelsAndSettings(org.openide.WizardDescriptor$Iterator<{%%0}>,{%%0})
meth public final void doCancelClick()
meth public final void doFinishClick()
meth public final void doNextClick()
meth public final void doPreviousClick()
meth public final void setPanels(org.openide.WizardDescriptor$Iterator)
 anno 0 java.lang.Deprecated()
meth public java.lang.Object getProperty(java.lang.String)
meth public java.lang.Object getValue()
meth public java.text.MessageFormat getTitleFormat()
meth public java.util.Map<java.lang.String,java.lang.Object> getProperties()
meth public java.util.Set getInstantiatedObjects()
meth public void putProperty(java.lang.String,java.lang.Object)
meth public void setAdditionalOptions(java.lang.Object[])
meth public void setClosingOptions(java.lang.Object[])
meth public void setHelpCtx(org.openide.util.HelpCtx)
meth public void setOptions(java.lang.Object[])
meth public void setTitleFormat(java.text.MessageFormat)
meth public void setValue(java.lang.Object)
supr org.openide.DialogDescriptor
hfds ASYNCHRONOUS_JOBS_RP,CLOSE_PREVENTER,PROGRESS_BAR_DISPLAY_NAME,addedWindowListener,autoWizardStyle,backgroundValidationTask,baseListener,bundle,cancelButton,changeStateInProgress,contentBackColor,contentData,contentForegroundColor,contentSelectedIndex,currentPanelWasChangedWhileStoreSettings,data,err,escapeActionListener,finishButton,finishOption,handle,helpURL,image,imageAlignment,init,initialized,isWizardWideHelpSet,logged,newObjects,nextButton,previousButton,propListener,properties,titleFormat,validationRuns,waitingComponent,weakCancelButtonListener,weakChangeListener,weakFinishButtonListener,weakNextButtonListener,weakPreviousButtonListener,weakPropertyChangeListener,wizardPanel
hcls BoundedHtmlBrowser,EmptyPanel,FinishAction,FixedHeightLabel,FixedHeightPane,ImagedPanel,Listener,PropL,SettingsAndIterator,WizardPanel,WrappedCellRenderer

CLSS public abstract interface static org.openide.WizardDescriptor$Panel<%0 extends java.lang.Object>
 outer org.openide.WizardDescriptor
meth public abstract boolean isValid()
meth public abstract java.awt.Component getComponent()
meth public abstract org.openide.util.HelpCtx getHelp()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void readSettings({org.openide.WizardDescriptor$Panel%0})
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public abstract void storeSettings({org.openide.WizardDescriptor$Panel%0})

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

