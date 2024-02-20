#Signature file v4.1
#Version 1.135

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

CLSS public abstract interface java.lang.Cloneable

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

CLSS public javax.swing.tree.DefaultMutableTreeNode
cons public init()
cons public init(java.lang.Object)
cons public init(java.lang.Object,boolean)
fld protected boolean allowsChildren
fld protected java.lang.Object userObject
fld protected java.util.Vector children
fld protected javax.swing.tree.MutableTreeNode parent
fld public final static java.util.Enumeration<javax.swing.tree.TreeNode> EMPTY_ENUMERATION
intf java.io.Serializable
intf java.lang.Cloneable
intf javax.swing.tree.MutableTreeNode
meth protected javax.swing.tree.TreeNode[] getPathToRoot(javax.swing.tree.TreeNode,int)
meth public boolean getAllowsChildren()
meth public boolean isLeaf()
meth public boolean isNodeAncestor(javax.swing.tree.TreeNode)
meth public boolean isNodeChild(javax.swing.tree.TreeNode)
meth public boolean isNodeDescendant(javax.swing.tree.DefaultMutableTreeNode)
meth public boolean isNodeRelated(javax.swing.tree.DefaultMutableTreeNode)
meth public boolean isNodeSibling(javax.swing.tree.TreeNode)
meth public boolean isRoot()
meth public int getChildCount()
meth public int getDepth()
meth public int getIndex(javax.swing.tree.TreeNode)
meth public int getLeafCount()
meth public int getLevel()
meth public int getSiblingCount()
meth public java.lang.Object clone()
meth public java.lang.Object getUserObject()
meth public java.lang.Object[] getUserObjectPath()
meth public java.lang.String toString()
meth public java.util.Enumeration breadthFirstEnumeration()
meth public java.util.Enumeration children()
meth public java.util.Enumeration depthFirstEnumeration()
meth public java.util.Enumeration pathFromAncestorEnumeration(javax.swing.tree.TreeNode)
meth public java.util.Enumeration postorderEnumeration()
meth public java.util.Enumeration preorderEnumeration()
meth public javax.swing.tree.DefaultMutableTreeNode getFirstLeaf()
meth public javax.swing.tree.DefaultMutableTreeNode getLastLeaf()
meth public javax.swing.tree.DefaultMutableTreeNode getNextLeaf()
meth public javax.swing.tree.DefaultMutableTreeNode getNextNode()
meth public javax.swing.tree.DefaultMutableTreeNode getNextSibling()
meth public javax.swing.tree.DefaultMutableTreeNode getPreviousLeaf()
meth public javax.swing.tree.DefaultMutableTreeNode getPreviousNode()
meth public javax.swing.tree.DefaultMutableTreeNode getPreviousSibling()
meth public javax.swing.tree.TreeNode getChildAfter(javax.swing.tree.TreeNode)
meth public javax.swing.tree.TreeNode getChildAt(int)
meth public javax.swing.tree.TreeNode getChildBefore(javax.swing.tree.TreeNode)
meth public javax.swing.tree.TreeNode getFirstChild()
meth public javax.swing.tree.TreeNode getLastChild()
meth public javax.swing.tree.TreeNode getParent()
meth public javax.swing.tree.TreeNode getRoot()
meth public javax.swing.tree.TreeNode getSharedAncestor(javax.swing.tree.DefaultMutableTreeNode)
meth public javax.swing.tree.TreeNode[] getPath()
meth public void add(javax.swing.tree.MutableTreeNode)
meth public void insert(javax.swing.tree.MutableTreeNode,int)
meth public void remove(int)
meth public void remove(javax.swing.tree.MutableTreeNode)
meth public void removeAllChildren()
meth public void removeFromParent()
meth public void setAllowsChildren(boolean)
meth public void setParent(javax.swing.tree.MutableTreeNode)
meth public void setUserObject(java.lang.Object)
supr java.lang.Object

CLSS public javax.swing.tree.DefaultTreeModel
cons public init(javax.swing.tree.TreeNode)
 anno 0 java.beans.ConstructorProperties(java.lang.String[] value=["root"])
cons public init(javax.swing.tree.TreeNode,boolean)
fld protected boolean asksAllowsChildren
fld protected javax.swing.event.EventListenerList listenerList
fld protected javax.swing.tree.TreeNode root
intf java.io.Serializable
intf javax.swing.tree.TreeModel
meth protected javax.swing.tree.TreeNode[] getPathToRoot(javax.swing.tree.TreeNode,int)
meth protected void fireTreeNodesChanged(java.lang.Object,java.lang.Object[],int[],java.lang.Object[])
meth protected void fireTreeNodesInserted(java.lang.Object,java.lang.Object[],int[],java.lang.Object[])
meth protected void fireTreeNodesRemoved(java.lang.Object,java.lang.Object[],int[],java.lang.Object[])
meth protected void fireTreeStructureChanged(java.lang.Object,java.lang.Object[],int[],java.lang.Object[])
meth public <%0 extends java.util.EventListener> {%%0}[] getListeners(java.lang.Class<{%%0}>)
meth public boolean asksAllowsChildren()
meth public boolean isLeaf(java.lang.Object)
meth public int getChildCount(java.lang.Object)
meth public int getIndexOfChild(java.lang.Object,java.lang.Object)
meth public java.lang.Object getChild(java.lang.Object,int)
meth public java.lang.Object getRoot()
meth public javax.swing.event.TreeModelListener[] getTreeModelListeners()
meth public javax.swing.tree.TreeNode[] getPathToRoot(javax.swing.tree.TreeNode)
meth public void addTreeModelListener(javax.swing.event.TreeModelListener)
meth public void insertNodeInto(javax.swing.tree.MutableTreeNode,javax.swing.tree.MutableTreeNode,int)
meth public void nodeChanged(javax.swing.tree.TreeNode)
meth public void nodeStructureChanged(javax.swing.tree.TreeNode)
meth public void nodesChanged(javax.swing.tree.TreeNode,int[])
meth public void nodesWereInserted(javax.swing.tree.TreeNode,int[])
meth public void nodesWereRemoved(javax.swing.tree.TreeNode,int[],java.lang.Object[])
meth public void reload()
meth public void reload(javax.swing.tree.TreeNode)
meth public void removeNodeFromParent(javax.swing.tree.MutableTreeNode)
meth public void removeTreeModelListener(javax.swing.event.TreeModelListener)
meth public void setAsksAllowsChildren(boolean)
meth public void setRoot(javax.swing.tree.TreeNode)
meth public void valueForPathChanged(javax.swing.tree.TreePath,java.lang.Object)
supr java.lang.Object

CLSS public abstract interface javax.swing.tree.MutableTreeNode
intf javax.swing.tree.TreeNode
meth public abstract void insert(javax.swing.tree.MutableTreeNode,int)
meth public abstract void remove(int)
meth public abstract void remove(javax.swing.tree.MutableTreeNode)
meth public abstract void removeFromParent()
meth public abstract void setParent(javax.swing.tree.MutableTreeNode)
meth public abstract void setUserObject(java.lang.Object)

CLSS public abstract interface javax.swing.tree.TreeModel
meth public abstract boolean isLeaf(java.lang.Object)
meth public abstract int getChildCount(java.lang.Object)
meth public abstract int getIndexOfChild(java.lang.Object,java.lang.Object)
meth public abstract java.lang.Object getChild(java.lang.Object,int)
meth public abstract java.lang.Object getRoot()
meth public abstract void addTreeModelListener(javax.swing.event.TreeModelListener)
meth public abstract void removeTreeModelListener(javax.swing.event.TreeModelListener)
meth public abstract void valueForPathChanged(javax.swing.tree.TreePath,java.lang.Object)

CLSS public abstract interface javax.swing.tree.TreeNode
meth public abstract boolean getAllowsChildren()
meth public abstract boolean isLeaf()
meth public abstract int getChildCount()
meth public abstract int getIndex(javax.swing.tree.TreeNode)
meth public abstract java.util.Enumeration children()
meth public abstract javax.swing.tree.TreeNode getChildAt(int)
meth public abstract javax.swing.tree.TreeNode getParent()

CLSS public abstract org.netbeans.lib.profiler.results.CCTNode
cons public init()
innr public abstract interface static AlwaysFirst
innr public abstract interface static AlwaysLast
innr public abstract interface static FixedPosition
intf javax.swing.tree.TreeNode
meth protected void setFilteredNode()
meth public abstract int getIndexOfChild(java.lang.Object)
meth public abstract int getNChildren()
meth public abstract org.netbeans.lib.profiler.results.CCTNode getChild(int)
meth public abstract org.netbeans.lib.profiler.results.CCTNode getParent()
meth public abstract org.netbeans.lib.profiler.results.CCTNode[] getChildren()
meth public boolean getAllowsChildren()
meth public boolean isFiltered()
meth public boolean isLeaf()
meth public int getChildCount()
meth public int getIndex(javax.swing.tree.TreeNode)
meth public java.util.Enumeration<org.netbeans.lib.profiler.results.CCTNode> children()
meth public javax.swing.tree.TreeNode getChildAt(int)
meth public org.netbeans.lib.profiler.results.CCTNode createFilteredNode()
meth public void merge(org.netbeans.lib.profiler.results.CCTNode)
supr java.lang.Object
hfds filtered

CLSS public final org.netbeans.modules.profiler.api.icons.Icons
cons public init()
innr public abstract interface static Keys
meth public static java.awt.Image getImage(java.lang.String)
meth public static java.lang.String getResource(java.lang.String)
meth public static javax.swing.Icon getIcon(java.lang.String)
meth public static javax.swing.ImageIcon getImageIcon(java.lang.String)
supr java.lang.Object

CLSS public abstract interface static org.netbeans.modules.profiler.api.icons.Icons$Keys
 outer org.netbeans.modules.profiler.api.icons.Icons

CLSS public abstract org.netbeans.modules.profiler.heapwalk.AbstractController
cons public init()
meth protected abstract javax.swing.AbstractButton createControllerPresenter()
meth protected abstract javax.swing.JPanel createControllerUI()
meth public javax.swing.AbstractButton getPresenter()
meth public javax.swing.JPanel getPanel()
supr java.lang.Object
hfds controllerUI,presenter

CLSS public abstract org.netbeans.modules.profiler.heapwalk.AbstractTopLevelController
cons public init()
meth protected abstract javax.swing.AbstractButton[] createClientPresenters()
meth protected void updateClientPresentersEnabling(javax.swing.AbstractButton[])
meth public javax.swing.AbstractButton[] getClientPresenters()
supr org.netbeans.modules.profiler.heapwalk.AbstractController
hfds clientPresenters

CLSS public org.netbeans.modules.profiler.heapwalk.AnalysisController
cons public init(org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker)
intf org.netbeans.modules.profiler.heapwalk.NavigationHistoryManager$NavigationHistoryCapable
meth protected javax.swing.AbstractButton createControllerPresenter()
meth protected javax.swing.AbstractButton[] createClientPresenters()
meth protected javax.swing.JPanel createControllerUI()
meth public boolean isAnalysisRunning()
meth public java.util.List<org.netbeans.modules.profiler.heapwalk.memorylint.Rule> getRules()
meth public javax.swing.BoundedRangeModel performAnalysis(boolean[])
meth public org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker getHeapFragmentWalker()
meth public org.netbeans.modules.profiler.heapwalk.NavigationHistoryManager$Configuration getCurrentConfiguration()
meth public void cancelAnalysis()
meth public void configure(org.netbeans.modules.profiler.heapwalk.NavigationHistoryManager$Configuration)
meth public void showURL(java.net.URL)
supr org.netbeans.modules.profiler.heapwalk.AbstractTopLevelController
hfds analysisRunning,heapFragmentWalker,rules,runningMemoryLint

CLSS public org.netbeans.modules.profiler.heapwalk.ClassPresenterPanel
cons public init()
intf org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker$StateListener
meth public void refresh()
meth public void setHeapFragmentWalker(org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker)
meth public void setJavaClass(org.netbeans.lib.profiler.heap.JavaClass)
meth public void stateChanged(org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker$StateEvent)
meth public void updateActions(int)
supr javax.swing.JPanel
hfds ICON_CLASS,actionsDivider,actionsRenderer,detailsRenderer,headerRenderer,heapFragmentWalker
hcls HeaderRenderer

CLSS public org.netbeans.modules.profiler.heapwalk.ClassesController
cons public init(org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker)
innr public static Configuration
intf org.netbeans.modules.profiler.heapwalk.FieldsBrowserController$Handler
intf org.netbeans.modules.profiler.heapwalk.NavigationHistoryManager$NavigationHistoryCapable
meth protected javax.swing.AbstractButton createControllerPresenter()
meth protected javax.swing.AbstractButton[] createClientPresenters()
meth protected javax.swing.JPanel createControllerUI()
meth public org.netbeans.lib.profiler.heap.JavaClass getSelectedClass()
meth public org.netbeans.modules.profiler.heapwalk.ClassesController$Configuration getCurrentConfiguration()
meth public org.netbeans.modules.profiler.heapwalk.ClassesListController getClassesListController()
meth public org.netbeans.modules.profiler.heapwalk.FieldsBrowserController getStaticFieldsBrowserController()
meth public org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker getHeapFragmentWalker()
meth public void classSelected()
meth public void configure(org.netbeans.modules.profiler.heapwalk.NavigationHistoryManager$Configuration)
meth public void showClass(org.netbeans.lib.profiler.heap.JavaClass)
meth public void showInstance(org.netbeans.lib.profiler.heap.Instance)
supr org.netbeans.modules.profiler.heapwalk.AbstractTopLevelController
hfds classesListController,heapFragmentWalker,staticFieldsBrowserController

CLSS public static org.netbeans.modules.profiler.heapwalk.ClassesController$Configuration
 outer org.netbeans.modules.profiler.heapwalk.ClassesController
cons public init(long,java.util.List<javax.swing.tree.TreePath>,javax.swing.tree.TreePath)
meth public java.util.List<javax.swing.tree.TreePath> getExpandedStaticFields()
meth public javax.swing.tree.TreePath getSelectedStaticField()
meth public long getJavaClassID()
supr org.netbeans.modules.profiler.heapwalk.NavigationHistoryManager$Configuration
hfds expandedStaticFields,javaClassID,selectedStaticField

CLSS public org.netbeans.modules.profiler.heapwalk.ClassesListController
cons public init(org.netbeans.modules.profiler.heapwalk.ClassesController)
fld public final static int FILTER_SUBCLASS = 1001
fld public long maxDiff
fld public long minDiff
meth protected javax.swing.AbstractButton createControllerPresenter()
meth protected javax.swing.JPanel createControllerUI()
meth public boolean compareRetained()
meth public boolean isDiff()
meth public java.lang.Object[][] getData(java.lang.String[],int,boolean,boolean,int,boolean,int)
meth public java.util.List getFilteredSortedClasses(java.lang.String[],int,boolean,boolean,int,boolean)
meth public org.netbeans.lib.profiler.heap.JavaClass getSelectedClass()
meth public org.netbeans.modules.profiler.heapwalk.ClassesController getClassesController()
meth public static boolean matchesFilter(org.netbeans.lib.profiler.heap.JavaClass,java.lang.String[],int,boolean,boolean)
meth public void classSelected(org.netbeans.lib.profiler.heap.JavaClass)
meth public void compareAction()
meth public void resetDiffAction()
meth public void selectClass(org.netbeans.lib.profiler.heap.JavaClass)
meth public void setColumnVisibility(int,boolean)
meth public void updateData()
supr org.netbeans.modules.profiler.heapwalk.AbstractController
hfds classesController,compareRetained,comparingSnapshot,diffClasses,numberFormat,percentFormat,selectedClass
hcls ClassesComparator,DiffJavaClass

CLSS public org.netbeans.modules.profiler.heapwalk.FieldsBrowserController
cons public init(org.netbeans.modules.profiler.heapwalk.FieldsBrowserController$Handler,int)
cons public init(org.netbeans.modules.profiler.heapwalk.FieldsBrowserController$Handler,int,boolean)
fld public final static int ROOT_CLASS = 1
fld public final static int ROOT_INSTANCE = 0
fld public final static org.netbeans.modules.profiler.heapwalk.model.AbstractHeapWalkerNode EMPTY_CLASS_NODE
fld public final static org.netbeans.modules.profiler.heapwalk.model.AbstractHeapWalkerNode EMPTY_INSTANCE_NODE
innr public abstract interface static Handler
meth protected javax.swing.AbstractButton createControllerPresenter()
meth protected javax.swing.JPanel createControllerUI()
meth public int getRootMode()
meth public java.util.List<javax.swing.tree.TreePath> getExpandedPaths()
meth public javax.swing.tree.TreePath getSelectedRow()
meth public org.netbeans.modules.profiler.heapwalk.FieldsBrowserController$Handler getInstancesControllerHandler()
meth public org.netbeans.modules.profiler.heapwalk.model.HeapWalkerNode getFilteredSortedFields(java.lang.String,int,boolean)
meth public void createNavigationHistoryPoint()
meth public void navigateToClass(org.netbeans.lib.profiler.heap.JavaClass)
meth public void navigateToInstance(org.netbeans.lib.profiler.heap.Instance)
meth public void restoreState(java.util.List,javax.swing.tree.TreePath)
meth public void setInstance(org.netbeans.lib.profiler.heap.Instance)
meth public void setJavaClass(org.netbeans.lib.profiler.heap.JavaClass)
meth public void showInstance(org.netbeans.lib.profiler.heap.Instance)
meth public void showJavaClass(org.netbeans.lib.profiler.heap.JavaClass)
meth public void update()
supr org.netbeans.modules.profiler.heapwalk.AbstractController
hfds instance,instancesControllerHandler,javaClass,rootMode,showClassLoaders

CLSS public abstract interface static org.netbeans.modules.profiler.heapwalk.FieldsBrowserController$Handler
 outer org.netbeans.modules.profiler.heapwalk.FieldsBrowserController
meth public abstract org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker getHeapFragmentWalker()
meth public abstract void showClass(org.netbeans.lib.profiler.heap.JavaClass)
meth public abstract void showInstance(org.netbeans.lib.profiler.heap.Instance)

CLSS public org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker
cons public init(org.netbeans.lib.profiler.heap.Heap,org.netbeans.modules.profiler.heapwalk.HeapWalker)
cons public init(org.netbeans.lib.profiler.heap.Heap,org.netbeans.modules.profiler.heapwalk.HeapWalker,boolean)
fld public final static int RETAINED_SIZES_CANCELLED = 1
fld public final static int RETAINED_SIZES_COMPUTED = 3
fld public final static int RETAINED_SIZES_COMPUTING = 2
fld public final static int RETAINED_SIZES_UNKNOWN = 0
fld public final static int RETAINED_SIZES_UNSUPPORTED = -1
innr public abstract interface static StateListener
innr public final static StateEvent
meth public boolean isNavigationBackAvailable()
meth public boolean isNavigationForwardAvailable()
meth public final int computeRetainedSizes(boolean,boolean)
meth public final int countClassLoaders()
meth public final int getRetainedSizesStatus()
meth public final void addStateListener(org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker$StateListener)
meth public final void removeStateListener(org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker$StateListener)
meth public int getHeapSegment()
meth public java.io.File getHeapDumpFile()
meth public javax.swing.JPanel getPanel()
meth public long getTotalLiveBytes()
meth public long getTotalLiveInstances()
meth public org.netbeans.lib.profiler.heap.Heap getHeapFragment()
meth public org.netbeans.modules.profiler.heapwalk.AbstractTopLevelController getActiveController()
meth public org.netbeans.modules.profiler.heapwalk.AnalysisController getAnalysisController()
meth public org.netbeans.modules.profiler.heapwalk.ClassesController getClassesController()
meth public org.netbeans.modules.profiler.heapwalk.InstancesController getInstancesController()
meth public org.netbeans.modules.profiler.heapwalk.OQLController getOQLController()
meth public org.netbeans.modules.profiler.heapwalk.SummaryController getSummaryController()
meth public org.openide.util.Lookup$Provider getHeapDumpProject()
meth public void createNavigationHistoryPoint()
meth public void navigateBack()
meth public void navigateForward()
meth public void showInstancesForClass(org.netbeans.lib.profiler.heap.JavaClass)
meth public void switchToAnalysisView()
meth public void switchToClassesView()
meth public void switchToHistoryAnalysisView()
meth public void switchToHistoryClassesView()
meth public void switchToHistoryInstancesView()
meth public void switchToHistoryOQLView()
meth public void switchToHistorySummaryView()
meth public void switchToInstancesView()
meth public void switchToOQLView()
meth public void switchToSummaryView()
supr java.lang.Object
hfds analysisController,classLoaderCount,classesController,heapFragment,heapSegment,heapWalker,instancesController,navigationHistoryManager,oqlController,retainedSizesStatus,stateListeners,summaryController,walkerUI

CLSS public final static org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker$StateEvent
 outer org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker
meth public boolean isMasterChange()
meth public int getRetainedSizesStatus()
supr java.lang.Object
hfds masterChange,retainedSizesStatus

CLSS public abstract interface static org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker$StateListener
 outer org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker
meth public abstract void stateChanged(org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker$StateEvent)

CLSS public org.netbeans.modules.profiler.heapwalk.HeapWalker
cons public init(java.io.File) throws java.io.IOException
cons public init(org.netbeans.lib.profiler.heap.Heap)
meth public int getHeapDumpSegment()
meth public java.io.File getHeapDumpFile()
meth public java.lang.String getName()
meth public org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker getMainHeapWalker()
meth public org.openide.util.Lookup$Provider getHeapDumpProject()
meth public org.openide.windows.TopComponent getTopComponent()
meth public void open()
supr java.lang.Object
hfds heapDumpFile,heapDumpProject,heapWalkerName,heapWalkerUI,mainHeapWalker,segment

CLSS public org.netbeans.modules.profiler.heapwalk.HeapWalkerManager
meth public boolean isHeapWalkerOpened(java.io.File)
meth public static org.netbeans.modules.profiler.heapwalk.HeapWalkerManager getDefault()
meth public void closeAllHeapWalkers()
meth public void closeHeapWalker(org.netbeans.modules.profiler.heapwalk.HeapWalker)
meth public void deleteHeapDump(java.io.File)
meth public void heapWalkerClosed(org.netbeans.modules.profiler.heapwalk.HeapWalker)
meth public void openHeapWalker(java.io.File)
meth public void openHeapWalker(java.io.File,int)
meth public void openHeapWalker(org.netbeans.modules.profiler.heapwalk.HeapWalker)
meth public void openHeapWalkers(java.io.File[])
supr java.lang.Object
hfds dumpsBeingDeleted,heapWalkers,heapwalkerRp
hcls Singleton

CLSS public org.netbeans.modules.profiler.heapwalk.HintsController
cons public init(org.netbeans.modules.profiler.heapwalk.SummaryController)
meth protected javax.swing.AbstractButton createControllerPresenter()
meth protected javax.swing.JPanel createControllerUI()
meth public org.netbeans.modules.profiler.heapwalk.SummaryController getSummaryController()
meth public void computeBiggestObjects(int)
meth public void createNavigationHistoryPoint()
meth public void showURL(java.net.URL)
supr org.netbeans.modules.profiler.heapwalk.AbstractController
hfds CLASS_URL_PREFIX,INSTANCE_URL_PREFIX,summaryController

CLSS public org.netbeans.modules.profiler.heapwalk.InstancesController
cons public init(org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker)
innr public static Configuration
intf org.netbeans.modules.profiler.heapwalk.FieldsBrowserController$Handler
intf org.netbeans.modules.profiler.heapwalk.NavigationHistoryManager$NavigationHistoryCapable
intf org.netbeans.modules.profiler.heapwalk.ReferencesBrowserController$Handler
meth protected javax.swing.AbstractButton createControllerPresenter()
meth protected javax.swing.AbstractButton[] createClientPresenters()
meth protected javax.swing.JPanel createControllerUI()
meth protected void updateClientPresentersEnabling(javax.swing.AbstractButton[])
meth public org.netbeans.lib.profiler.heap.Instance getSelectedInstance()
meth public org.netbeans.lib.profiler.heap.JavaClass getSelectedClass()
meth public org.netbeans.modules.profiler.heapwalk.ClassPresenterPanel getClassPresenterPanel()
meth public org.netbeans.modules.profiler.heapwalk.FieldsBrowserController getFieldsBrowserController()
meth public org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker getHeapFragmentWalker()
meth public org.netbeans.modules.profiler.heapwalk.InstancesController$Configuration getCurrentConfiguration()
meth public org.netbeans.modules.profiler.heapwalk.InstancesListController getInstancesListController()
meth public org.netbeans.modules.profiler.heapwalk.ReferencesBrowserController getReferencesBrowserController()
meth public void configure(org.netbeans.modules.profiler.heapwalk.NavigationHistoryManager$Configuration)
meth public void instanceSelected()
meth public void setClass(org.netbeans.lib.profiler.heap.JavaClass)
meth public void showClass(org.netbeans.lib.profiler.heap.JavaClass)
meth public void showInstance(org.netbeans.lib.profiler.heap.Instance)
supr org.netbeans.modules.profiler.heapwalk.AbstractTopLevelController
hfds classPresenter,fieldsBrowserController,heapFragmentWalker,instancesListController,referencesBrowserController,selectedClass

CLSS public static org.netbeans.modules.profiler.heapwalk.InstancesController$Configuration
 outer org.netbeans.modules.profiler.heapwalk.InstancesController
cons public init(long,java.util.List<javax.swing.tree.TreePath>,javax.swing.tree.TreePath,java.util.List<javax.swing.tree.TreePath>,javax.swing.tree.TreePath)
meth public java.util.List getExpandedReferences()
meth public java.util.List<javax.swing.tree.TreePath> getExpandedFields()
meth public javax.swing.tree.TreePath getSelectedField()
meth public javax.swing.tree.TreePath getSelectedReference()
meth public long getInstanceID()
supr org.netbeans.modules.profiler.heapwalk.NavigationHistoryManager$Configuration
hfds expandedFields,expandedReferences,instanceID,selectedField,selectedReference

CLSS public org.netbeans.modules.profiler.heapwalk.InstancesListController
cons public init(org.netbeans.modules.profiler.heapwalk.InstancesController)
fld public final static org.netbeans.lib.profiler.heap.Instance INSTANCE_FIRST
fld public final static org.netbeans.modules.profiler.heapwalk.model.AbstractHeapWalkerNode EMPTY_INSTANCE_NODE
fld public int containerToSelectIndex
fld public org.netbeans.lib.profiler.heap.Instance instanceToSelect
innr public InstancesListClassNode
innr public InstancesListContainerNode
innr public abstract interface static InstancesListNode
innr public static InstancesListInstanceNode
meth protected javax.swing.AbstractButton createControllerPresenter()
meth protected javax.swing.JPanel createControllerUI()
meth public org.netbeans.lib.profiler.heap.Instance getSelectedInstance()
meth public org.netbeans.modules.profiler.heapwalk.InstancesController getInstancesController()
meth public org.netbeans.modules.profiler.heapwalk.model.HeapWalkerNode getFilteredSortedInstances(java.lang.String,int,boolean)
meth public org.netbeans.modules.profiler.heapwalk.model.HeapWalkerNode getInstanceContainer(org.netbeans.lib.profiler.heap.Instance,org.netbeans.modules.profiler.heapwalk.InstancesListController$InstancesListClassNode)
meth public void instanceSelected(org.netbeans.lib.profiler.heap.Instance)
meth public void scheduleContainerSelection(int)
meth public void scheduleFirstInstanceSelection()
meth public void scheduleInstanceSelection(org.netbeans.lib.profiler.heap.Instance)
meth public void selectInstance(org.netbeans.lib.profiler.heap.Instance)
meth public void setClass(org.netbeans.lib.profiler.heap.JavaClass)
meth public void showInstance(org.netbeans.lib.profiler.heap.Instance)
meth public void update()
supr org.netbeans.modules.profiler.heapwalk.AbstractController
hfds instancesController,jClass,pathToSelect,selectedInstance
hcls InstancesComparator

CLSS public org.netbeans.modules.profiler.heapwalk.InstancesListController$InstancesListClassNode
 outer org.netbeans.modules.profiler.heapwalk.InstancesListController
cons public init(org.netbeans.modules.profiler.heapwalk.InstancesListController,org.netbeans.lib.profiler.heap.JavaClass,java.lang.String,int,boolean)
intf org.netbeans.modules.profiler.heapwalk.InstancesListController$InstancesListNode
meth protected java.lang.String computeName()
meth protected javax.swing.ImageIcon computeIcon()
meth protected org.netbeans.modules.profiler.heapwalk.model.ChildrenComputer getChildrenComputer()
meth public boolean isLeaf()
meth public java.lang.String getDetails(org.netbeans.lib.profiler.heap.Instance)
meth public java.lang.String getID()
meth public java.lang.String getReachableSize()
meth public java.lang.String getRetainedSize()
meth public java.lang.String getSize()
meth public javax.swing.tree.TreePath getInstancePath(org.netbeans.lib.profiler.heap.Instance)
meth public org.netbeans.lib.profiler.heap.GCRoot getGCRoot(org.netbeans.lib.profiler.heap.Instance)
meth public org.netbeans.lib.profiler.heap.JavaClass getJavaClassByID(long)
meth public void refreshView()
meth public void repaintView()
supr org.netbeans.modules.profiler.heapwalk.model.ClassNode$RootNode
hfds filterValue,size,sortingColumn,sortingOrder

CLSS public org.netbeans.modules.profiler.heapwalk.InstancesListController$InstancesListContainerNode
 outer org.netbeans.modules.profiler.heapwalk.InstancesListController
cons public init(org.netbeans.modules.profiler.heapwalk.InstancesListController,org.netbeans.modules.profiler.heapwalk.model.ClassNode,int,int,java.lang.String,int,boolean)
cons public init(org.netbeans.modules.profiler.heapwalk.InstancesListController,org.netbeans.modules.profiler.heapwalk.model.ClassNode,int,int,java.lang.String,int,boolean,java.util.List,int)
intf org.netbeans.modules.profiler.heapwalk.InstancesListController$InstancesListNode
meth protected java.lang.String computeName()
meth protected java.lang.String computeRetainedSize()
meth protected java.lang.String computeSize()
meth protected java.lang.String computeType()
meth protected java.lang.String computeValue()
meth protected javax.swing.Icon computeIcon()
meth protected org.netbeans.modules.profiler.heapwalk.model.ChildrenComputer getChildrenComputer()
meth protected org.netbeans.modules.profiler.heapwalk.model.HeapWalkerNode[] computeChildren()
meth public boolean isLeaf()
meth public java.lang.Object getNodeID()
meth public java.lang.String getID()
meth public java.lang.String getReachableSize()
meth public javax.swing.tree.TreePath getInstancePath(org.netbeans.lib.profiler.heap.Instance)
supr org.netbeans.modules.profiler.heapwalk.model.AbstractHeapWalkerNode
hfds endIndex,filterValue,sortingColumn,sortingOrder,startIndex

CLSS public static org.netbeans.modules.profiler.heapwalk.InstancesListController$InstancesListInstanceNode
 outer org.netbeans.modules.profiler.heapwalk.InstancesListController
cons public init(org.netbeans.lib.profiler.heap.Instance,org.netbeans.modules.profiler.heapwalk.model.HeapWalkerNode)
intf org.netbeans.modules.profiler.heapwalk.InstancesListController$InstancesListNode
meth protected javax.swing.ImageIcon computeIcon()
meth public boolean currentlyHasChildren()
meth public boolean isLeaf()
meth public boolean isModeFields()
meth public boolean isRoot()
meth public int getIndexOfChild(java.lang.Object)
meth public int getMode()
meth public int getNChildren()
meth public java.lang.Object getNodeID()
meth public java.lang.String getDetails()
meth public java.lang.String getID()
meth public java.lang.String getName()
meth public java.lang.String getReachableSize()
meth public java.lang.String getRetainedSize()
meth public java.lang.String getSimpleType()
meth public java.lang.String getSize()
meth public java.lang.String getType()
meth public java.lang.String getValue()
meth public java.lang.String toString()
meth public javax.swing.Icon getIcon()
meth public javax.swing.tree.TreePath getInstancePath(org.netbeans.lib.profiler.heap.Instance)
meth public org.netbeans.lib.profiler.heap.Instance getInstance()
meth public org.netbeans.modules.profiler.heapwalk.model.HeapWalkerNode getChild(int)
meth public org.netbeans.modules.profiler.heapwalk.model.HeapWalkerNode getParent()
meth public org.netbeans.modules.profiler.heapwalk.model.HeapWalkerNode[] getChildren()
supr org.netbeans.modules.profiler.heapwalk.model.HeapWalkerNode
hfds details,icon,instance,name,numberFormat,parent,reachableSize,retainedSize,size

CLSS public abstract interface static org.netbeans.modules.profiler.heapwalk.InstancesListController$InstancesListNode
 outer org.netbeans.modules.profiler.heapwalk.InstancesListController
meth public abstract java.lang.String getID()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getReachableSize()
meth public abstract java.lang.String getRetainedSize()
meth public abstract java.lang.String getSize()
meth public abstract javax.swing.tree.TreePath getInstancePath(org.netbeans.lib.profiler.heap.Instance)

CLSS public org.netbeans.modules.profiler.heapwalk.LegendPanel
cons public init(boolean)
meth public void setGCRootVisible(boolean)
supr javax.swing.JPanel
hfds gcRootLegend,gcRootLegendDivider

CLSS public org.netbeans.modules.profiler.heapwalk.NavigationHistoryManager
cons public init(org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker)
innr public abstract interface static NavigationHistoryCapable
innr public static Configuration
meth public boolean isNavigationBackAvailable()
meth public boolean isNavigationForwardAvailable()
meth public void createNavigationHistoryPoint()
meth public void navigateBack()
meth public void navigateForward()
supr java.lang.Object
hfds backHistory,forwardHistory,heapFragmentWalker
hcls NavigationHistoryItem

CLSS public static org.netbeans.modules.profiler.heapwalk.NavigationHistoryManager$Configuration
 outer org.netbeans.modules.profiler.heapwalk.NavigationHistoryManager
cons public init()
supr java.lang.Object

CLSS public abstract interface static org.netbeans.modules.profiler.heapwalk.NavigationHistoryManager$NavigationHistoryCapable
 outer org.netbeans.modules.profiler.heapwalk.NavigationHistoryManager
meth public abstract org.netbeans.modules.profiler.heapwalk.NavigationHistoryManager$Configuration getCurrentConfiguration()
meth public abstract void configure(org.netbeans.modules.profiler.heapwalk.NavigationHistoryManager$Configuration)

CLSS public org.netbeans.modules.profiler.heapwalk.OQLController
cons public init(org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker)
innr public static QueryController
innr public static ResultsController
innr public static SavedController
intf org.netbeans.modules.profiler.heapwalk.NavigationHistoryManager$NavigationHistoryCapable
meth protected javax.swing.AbstractButton createControllerPresenter()
meth protected javax.swing.AbstractButton[] createClientPresenters()
meth protected javax.swing.JPanel createControllerUI()
meth public boolean isQueryRunning()
meth public org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker getHeapFragmentWalker()
meth public org.netbeans.modules.profiler.heapwalk.NavigationHistoryManager$Configuration getCurrentConfiguration()
meth public org.netbeans.modules.profiler.heapwalk.OQLController$QueryController getQueryController()
meth public org.netbeans.modules.profiler.heapwalk.OQLController$ResultsController getResultsController()
meth public org.netbeans.modules.profiler.heapwalk.OQLController$SavedController getSavedController()
meth public void cancelQuery()
meth public void configure(org.netbeans.modules.profiler.heapwalk.NavigationHistoryManager$Configuration)
meth public void executeQuery(java.lang.String)
supr org.netbeans.modules.profiler.heapwalk.AbstractTopLevelController
hfds RESULTS_LIMIT,analysisRunning,engine,heapFragmentWalker,progressUpdater,queryController,resultsController,savedController
hcls ProgressUpdater

CLSS public static org.netbeans.modules.profiler.heapwalk.OQLController$QueryController
 outer org.netbeans.modules.profiler.heapwalk.OQLController
cons public init(org.netbeans.modules.profiler.heapwalk.OQLController)
meth protected javax.swing.AbstractButton createControllerPresenter()
meth protected javax.swing.JPanel createControllerUI()
meth public org.netbeans.modules.profiler.heapwalk.OQLController getOQLController()
meth public void setQuery(java.lang.String)
supr org.netbeans.modules.profiler.heapwalk.AbstractController
hfds oqlController

CLSS public static org.netbeans.modules.profiler.heapwalk.OQLController$ResultsController
 outer org.netbeans.modules.profiler.heapwalk.OQLController
cons public init(org.netbeans.modules.profiler.heapwalk.OQLController)
meth protected javax.swing.AbstractButton createControllerPresenter()
meth protected javax.swing.JPanel createControllerUI()
meth public org.netbeans.modules.profiler.heapwalk.OQLController getOQLController()
meth public void setResult(java.lang.String)
meth public void showURL(java.net.URL)
supr org.netbeans.modules.profiler.heapwalk.AbstractController
hfds oqlController

CLSS public static org.netbeans.modules.profiler.heapwalk.OQLController$SavedController
 outer org.netbeans.modules.profiler.heapwalk.OQLController
cons public init(org.netbeans.modules.profiler.heapwalk.OQLController)
meth protected javax.swing.AbstractButton createControllerPresenter()
meth protected javax.swing.JPanel createControllerUI()
meth public org.netbeans.modules.profiler.heapwalk.OQLController getOQLController()
meth public static void loadData(org.netbeans.modules.profiler.heapwalk.OQLSupport$OQLTreeModel)
meth public static void saveData(org.netbeans.modules.profiler.heapwalk.OQLSupport$OQLTreeModel)
meth public void saveQuery(java.lang.String)
supr org.netbeans.modules.profiler.heapwalk.AbstractController
hfds oqlController

CLSS public final org.netbeans.modules.profiler.heapwalk.OQLSupport
cons public init()
innr public abstract static OQLNode
innr public final static OQLTreeModel
innr public final static Query
innr public static OQLCategoryNode
innr public static OQLQueryNode
meth public static org.netbeans.modules.profiler.heapwalk.OQLSupport$OQLTreeModel createModel()
meth public static void loadModel(org.netbeans.modules.profiler.heapwalk.OQLSupport$OQLTreeModel)
meth public static void saveModel(org.netbeans.modules.profiler.heapwalk.OQLSupport$OQLTreeModel)
supr java.lang.Object
hfds PROP_QUERY_DESCR_KEY,PROP_QUERY_NAME_KEY,PROP_QUERY_SCRIPT_KEY,SAVED_OQL_QUERIES_FILENAME,SNAPSHOT_VERSION
hcls CustomCategoryNode,NoCustomQueriesNode,RootNode,SpecialNode

CLSS public static org.netbeans.modules.profiler.heapwalk.OQLSupport$OQLCategoryNode
 outer org.netbeans.modules.profiler.heapwalk.OQLSupport
cons public init(org.netbeans.modules.profiler.oql.repository.api.OQLQueryCategory)
meth public boolean isLeaf()
meth public boolean supportsProperties()
meth public java.lang.String getCaption()
meth public java.lang.String getDescription()
meth public java.lang.String toString()
meth public void insert(javax.swing.tree.MutableTreeNode,int)
meth public void remove(int)
supr org.netbeans.modules.profiler.heapwalk.OQLSupport$OQLNode<org.netbeans.modules.profiler.oql.repository.api.OQLQueryCategory>
hfds noQueries

CLSS public abstract static org.netbeans.modules.profiler.heapwalk.OQLSupport$OQLNode<%0 extends java.lang.Object>
 outer org.netbeans.modules.profiler.heapwalk.OQLSupport
cons public init({org.netbeans.modules.profiler.heapwalk.OQLSupport$OQLNode%0})
meth public boolean isReadOnly()
meth public boolean supportsDelete()
meth public boolean supportsOpen()
meth public boolean supportsProperties()
meth public final {org.netbeans.modules.profiler.heapwalk.OQLSupport$OQLNode%0} getUserObject()
meth public java.lang.String getCaption()
meth public java.lang.String getDescription()
supr javax.swing.tree.DefaultMutableTreeNode

CLSS public static org.netbeans.modules.profiler.heapwalk.OQLSupport$OQLQueryNode
 outer org.netbeans.modules.profiler.heapwalk.OQLSupport
cons public init(org.netbeans.modules.profiler.heapwalk.OQLSupport$Query)
meth public boolean isLeaf()
meth public boolean isReadOnly()
meth public boolean supportsDelete()
meth public boolean supportsOpen()
meth public java.lang.String getCaption()
meth public java.lang.String getDescription()
meth public java.lang.String toString()
supr org.netbeans.modules.profiler.heapwalk.OQLSupport$OQLNode<org.netbeans.modules.profiler.heapwalk.OQLSupport$Query>

CLSS public final static org.netbeans.modules.profiler.heapwalk.OQLSupport$OQLTreeModel
 outer org.netbeans.modules.profiler.heapwalk.OQLSupport
cons public init()
meth public boolean hasCustomQueries()
meth public boolean hasDefinedCategories()
meth public org.netbeans.modules.profiler.heapwalk.OQLSupport$OQLCategoryNode customCategory()
supr javax.swing.tree.DefaultTreeModel
hfds customCategory

CLSS public final static org.netbeans.modules.profiler.heapwalk.OQLSupport$Query
 outer org.netbeans.modules.profiler.heapwalk.OQLSupport
cons public init(java.lang.String,java.lang.String,java.lang.String)
cons public init(org.netbeans.modules.profiler.oql.repository.api.OQLQueryDefinition)
meth public java.lang.String getDescription()
meth public java.lang.String getName()
meth public java.lang.String getScript()
meth public java.lang.String toString()
meth public void setDescription(java.lang.String)
meth public void setName(java.lang.String)
meth public void setScript(java.lang.String)
supr java.lang.Object
hfds description,name,script

CLSS public org.netbeans.modules.profiler.heapwalk.OverviewController
cons public init(org.netbeans.modules.profiler.heapwalk.SummaryController)
fld public final static java.lang.String SHOW_NEXT_SEGMENT_URL = "file:/next"
fld public final static java.lang.String SHOW_SYSPROPS_URL = "file:/sysprops"
fld public final static java.lang.String SHOW_THREADS_URL = "file:/threads"
meth protected javax.swing.AbstractButton createControllerPresenter()
meth protected javax.swing.JPanel createControllerUI()
meth public java.lang.String computeEnvironment()
meth public java.lang.String computeSummary()
meth public java.lang.String computeSystemProperties(boolean)
meth public java.lang.String computeThreads(boolean)
meth public org.netbeans.modules.profiler.heapwalk.SummaryController getSummaryController()
meth public void showNextSegment()
meth public void showURL(java.lang.String)
supr org.netbeans.modules.profiler.heapwalk.AbstractController
hfds CLASS_URL_PREFIX,INSTANCE_URL_PREFIX,JVMTI_THREAD_STATE_ALIVE,JVMTI_THREAD_STATE_BLOCKED_ON_MONITOR_ENTER,JVMTI_THREAD_STATE_RUNNABLE,JVMTI_THREAD_STATE_TERMINATED,JVMTI_THREAD_STATE_WAITING_INDEFINITELY,JVMTI_THREAD_STATE_WAITING_WITH_TIMEOUT,LINE_PREFIX,OPEN_THREADS_URL,THREAD_URL_PREFIX,heapFragmentWalker,java_lang_Class,oome,stackTrace,summaryController,systemProperties,systemPropertiesComputed

CLSS public org.netbeans.modules.profiler.heapwalk.ReferencesBrowserController
cons public init(org.netbeans.modules.profiler.heapwalk.ReferencesBrowserController$Handler)
fld public final static org.netbeans.modules.profiler.heapwalk.model.AbstractHeapWalkerNode EMPTY_INSTANCE_NODE
innr public abstract interface static Handler
meth protected javax.swing.AbstractButton createControllerPresenter()
meth protected javax.swing.JPanel createControllerUI()
meth public java.util.List<javax.swing.tree.TreePath> getExpandedPaths()
meth public javax.swing.tree.TreePath getSelectedRow()
meth public org.netbeans.modules.profiler.heapwalk.ReferencesBrowserController$Handler getReferencesControllerHandler()
meth public org.netbeans.modules.profiler.heapwalk.model.HeapWalkerNode getFilteredSortedReferences(java.lang.String,int,boolean)
meth public void createNavigationHistoryPoint()
meth public void navigateToClass(org.netbeans.lib.profiler.heap.JavaClass)
meth public void navigateToInstance(org.netbeans.lib.profiler.heap.Instance)
meth public void navigateToNearestGCRoot(org.netbeans.modules.profiler.heapwalk.model.InstanceNode)
meth public void navigateToRootNearestGCRoot()
meth public void restoreState(java.util.List,javax.swing.tree.TreePath)
meth public void setInstance(org.netbeans.lib.profiler.heap.Instance)
meth public void showInThreads(org.netbeans.lib.profiler.heap.Instance)
meth public void showInstance(org.netbeans.lib.profiler.heap.Instance)
meth public void update()
supr org.netbeans.modules.profiler.heapwalk.AbstractController
hfds DEFAULT_HEIGHT,DEFAULT_WIDTH,instance,referencesControllerHandler

CLSS public abstract interface static org.netbeans.modules.profiler.heapwalk.ReferencesBrowserController$Handler
 outer org.netbeans.modules.profiler.heapwalk.ReferencesBrowserController
meth public abstract org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker getHeapFragmentWalker()
meth public abstract void showClass(org.netbeans.lib.profiler.heap.JavaClass)
meth public abstract void showInstance(org.netbeans.lib.profiler.heap.Instance)

CLSS public org.netbeans.modules.profiler.heapwalk.SummaryController
cons public init(org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker)
intf org.netbeans.modules.profiler.heapwalk.NavigationHistoryManager$NavigationHistoryCapable
meth protected javax.swing.AbstractButton createControllerPresenter()
meth protected javax.swing.AbstractButton[] createClientPresenters()
meth protected javax.swing.JPanel createControllerUI()
meth public org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker getHeapFragmentWalker()
meth public org.netbeans.modules.profiler.heapwalk.HintsController getHintsController()
meth public org.netbeans.modules.profiler.heapwalk.NavigationHistoryManager$Configuration getCurrentConfiguration()
meth public org.netbeans.modules.profiler.heapwalk.OverviewController getOverViewController()
meth public void configure(org.netbeans.modules.profiler.heapwalk.NavigationHistoryManager$Configuration)
supr org.netbeans.modules.profiler.heapwalk.AbstractTopLevelController
hfds heapFragmentWalker,hintsController,overviewController

CLSS public final org.netbeans.modules.profiler.heapwalk.details.api.DetailsSupport
cons public init()
meth public static java.lang.String getDetailsString(org.netbeans.lib.profiler.heap.Instance,org.netbeans.lib.profiler.heap.Heap)
meth public static org.netbeans.modules.profiler.heapwalk.details.spi.DetailsProvider$View getDetailsView(org.netbeans.lib.profiler.heap.Instance,org.netbeans.lib.profiler.heap.Heap)
supr java.lang.Object
hfds PROVIDERS,PROVIDERS_CACHE
hcls ProviderClassPair

CLSS public final org.netbeans.modules.profiler.heapwalk.details.api.ExportAction
cons public init(org.netbeans.modules.profiler.heapwalk.details.api.ExportAction$ExportProvider)
fld public final static int MODE_BIN = 3
fld public final static int MODE_CSV = 1
fld public final static int MODE_TXT = 2
innr public abstract interface static ExportProvider
meth public void actionPerformed(java.awt.event.ActionEvent)
supr javax.swing.AbstractAction
hfds FILE_EXTENSION_BIN,FILE_EXTENSION_CSV,FILE_EXTENSION_TXT,ICON,LOGGER,exportDir,exportProvider,exportedFileType,fileChooser
hcls FileFilterImpl,SelectedFile

CLSS public abstract interface static org.netbeans.modules.profiler.heapwalk.details.api.ExportAction$ExportProvider
 outer org.netbeans.modules.profiler.heapwalk.details.api.ExportAction
meth public abstract boolean hasBinaryData()
meth public abstract boolean hasRawData()
meth public abstract boolean hasText()
meth public abstract boolean isExportable()
meth public abstract java.lang.String getViewName()
meth public abstract void exportData(int,org.netbeans.lib.profiler.results.ExportDataDumper)

CLSS public final org.netbeans.modules.profiler.heapwalk.details.api.StringDecoder
cons public init(org.netbeans.lib.profiler.heap.Heap,byte,java.util.List<java.lang.String>)
meth public int getStringLength()
meth public java.lang.String getValueAt(int)
supr java.lang.Object
hfds HI_BYTE_SHIFT,LO_BYTE_SHIFT,coder,values

CLSS public abstract org.netbeans.modules.profiler.heapwalk.details.spi.DetailsProvider
cons public init()
innr public abstract static Basic
innr public abstract static View
meth public java.lang.String getDetailsString(java.lang.String,org.netbeans.lib.profiler.heap.Instance,org.netbeans.lib.profiler.heap.Heap)
meth public java.lang.String[] getSupportedClasses()
meth public org.netbeans.modules.profiler.heapwalk.details.spi.DetailsProvider$View getDetailsView(java.lang.String,org.netbeans.lib.profiler.heap.Instance,org.netbeans.lib.profiler.heap.Heap)
supr java.lang.Object

CLSS public abstract static org.netbeans.modules.profiler.heapwalk.details.spi.DetailsProvider$Basic
 outer org.netbeans.modules.profiler.heapwalk.details.spi.DetailsProvider
cons protected !varargs init(java.lang.String[])
cons public init()
meth public final java.lang.String[] getSupportedClasses()
supr org.netbeans.modules.profiler.heapwalk.details.spi.DetailsProvider
hfds supportedClasses

CLSS public abstract static org.netbeans.modules.profiler.heapwalk.details.spi.DetailsProvider$View
 outer org.netbeans.modules.profiler.heapwalk.details.spi.DetailsProvider
cons protected init(org.netbeans.lib.profiler.heap.Instance,org.netbeans.lib.profiler.heap.Heap)
cons protected init(org.netbeans.lib.profiler.heap.Instance,org.netbeans.lib.profiler.heap.Heap,java.awt.Component)
meth protected abstract void computeView(org.netbeans.lib.profiler.heap.Instance,org.netbeans.lib.profiler.heap.Heap)
meth protected void removed()
meth public final void addNotify()
meth public final void removeNotify()
supr javax.swing.JPanel
hfds heap,instance,workerTask

CLSS public final org.netbeans.modules.profiler.heapwalk.details.spi.DetailsUtils
cons public init()
fld public final static int MAX_ARRAY_LENGTH = 160
meth public static boolean getBooleanFieldValue(org.netbeans.lib.profiler.heap.Instance,java.lang.String,boolean)
meth public static boolean isInstanceOf(org.netbeans.lib.profiler.heap.Instance,java.lang.String)
meth public static boolean isSubclassOf(org.netbeans.lib.profiler.heap.Instance,java.lang.String)
meth public static boolean[] getBooleanArray(java.util.List<java.lang.String>)
meth public static byte getByteFieldValue(org.netbeans.lib.profiler.heap.Instance,java.lang.String,byte)
meth public static byte[] getByteArray(java.util.List<java.lang.String>)
meth public static char getCharFieldValue(org.netbeans.lib.profiler.heap.Instance,java.lang.String,char)
meth public static char[] getCharArray(java.util.List<java.lang.String>)
meth public static double getDoubleFieldValue(org.netbeans.lib.profiler.heap.Instance,java.lang.String,double)
meth public static double[] getDoubleArray(java.util.List<java.lang.String>)
meth public static float getFloatFieldValue(org.netbeans.lib.profiler.heap.Instance,java.lang.String,float)
meth public static float[] getFloatArray(java.util.List<java.lang.String>)
meth public static int getIntFieldValue(org.netbeans.lib.profiler.heap.Instance,java.lang.String,int)
meth public static int[] getIntArray(java.util.List<java.lang.String>)
meth public static java.lang.String getInstanceFieldString(org.netbeans.lib.profiler.heap.Instance,java.lang.String,org.netbeans.lib.profiler.heap.Heap)
meth public static java.lang.String getInstanceString(org.netbeans.lib.profiler.heap.Instance,org.netbeans.lib.profiler.heap.Heap)
meth public static java.lang.String getPrimitiveArrayFieldString(org.netbeans.lib.profiler.heap.Instance,java.lang.String,int,int,java.lang.String,java.lang.String)
meth public static java.lang.String getPrimitiveArrayString(org.netbeans.lib.profiler.heap.Instance,int,int,java.lang.String,java.lang.String)
meth public static java.util.List<java.lang.String> getPrimitiveArrayFieldValues(org.netbeans.lib.profiler.heap.Instance,java.lang.String)
meth public static java.util.List<java.lang.String> getPrimitiveArrayValues(org.netbeans.lib.profiler.heap.Instance)
meth public static long getLongFieldValue(org.netbeans.lib.profiler.heap.Instance,java.lang.String,long)
meth public static long[] getLongArray(java.util.List<java.lang.String>)
meth public static short getShortFieldValue(org.netbeans.lib.profiler.heap.Instance,java.lang.String,short)
meth public static short[] getShortArray(java.util.List<java.lang.String>)
supr java.lang.Object
hfds LAST_SUBCLASS_INSTANCE,SUBCLASS_CACHE

CLSS public abstract org.netbeans.modules.profiler.heapwalk.model.AbstractHeapWalkerNode
cons public init(org.netbeans.modules.profiler.heapwalk.model.HeapWalkerNode)
cons public init(org.netbeans.modules.profiler.heapwalk.model.HeapWalkerNode,int)
meth protected abstract java.lang.String computeName()
meth protected abstract java.lang.String computeRetainedSize()
meth protected abstract java.lang.String computeSize()
meth protected abstract java.lang.String computeType()
meth protected abstract java.lang.String computeValue()
meth protected abstract javax.swing.Icon computeIcon()
meth protected org.netbeans.modules.profiler.heapwalk.model.ChildrenComputer getChildrenComputer()
meth protected org.netbeans.modules.profiler.heapwalk.model.HeapWalkerNode[] computeChildren()
meth protected void setChildren(org.netbeans.modules.profiler.heapwalk.model.HeapWalkerNode[])
meth public boolean currentlyHasChildren()
meth public boolean equals(java.lang.Object)
meth public boolean isLeaf()
meth public boolean isRoot()
meth public final boolean isModeFields()
meth public int getIndexOfChild(java.lang.Object)
meth public int getMode()
meth public int getNChildren()
meth public int hashCode()
meth public java.lang.Object getNodeID()
meth public java.lang.String getDetails()
meth public java.lang.String getName()
meth public java.lang.String getRetainedSize()
meth public java.lang.String getSimpleType()
meth public java.lang.String getSize()
meth public java.lang.String getType()
meth public java.lang.String getValue()
meth public java.lang.String toString()
meth public javax.swing.Icon getIcon()
meth public org.netbeans.modules.profiler.heapwalk.model.HeapWalkerNode getChild(int)
meth public org.netbeans.modules.profiler.heapwalk.model.HeapWalkerNode getParent()
meth public org.netbeans.modules.profiler.heapwalk.model.HeapWalkerNode[] getChildren()
meth public void setParent(org.netbeans.modules.profiler.heapwalk.model.HeapWalkerNode)
supr org.netbeans.modules.profiler.heapwalk.model.HeapWalkerNode
hfds children,icon,indexes,mode,name,parent,retainedSize,size,type,value

CLSS public org.netbeans.modules.profiler.heapwalk.model.ClassNode
cons public init(org.netbeans.lib.profiler.heap.JavaClass,java.lang.String,org.netbeans.modules.profiler.heapwalk.model.HeapWalkerNode)
cons public init(org.netbeans.lib.profiler.heap.JavaClass,java.lang.String,org.netbeans.modules.profiler.heapwalk.model.HeapWalkerNode,int)
innr public abstract static RootNode
meth protected java.lang.String computeName()
meth protected java.lang.String computeRetainedSize()
meth protected java.lang.String computeSize()
meth protected java.lang.String computeType()
meth protected java.lang.String computeValue()
meth protected javax.swing.ImageIcon computeIcon()
meth protected org.netbeans.modules.profiler.heapwalk.model.ChildrenComputer getChildrenComputer()
meth public boolean isLeaf()
meth public java.lang.Object getNodeID()
meth public org.netbeans.lib.profiler.heap.JavaClass getJavaClass()
supr org.netbeans.modules.profiler.heapwalk.model.AbstractHeapWalkerNode
hfds javaClass,name,numberFormat

CLSS public abstract static org.netbeans.modules.profiler.heapwalk.model.ClassNode$RootNode
 outer org.netbeans.modules.profiler.heapwalk.model.ClassNode
cons public init(org.netbeans.lib.profiler.heap.JavaClass,java.lang.String,org.netbeans.modules.profiler.heapwalk.model.HeapWalkerNode)
cons public init(org.netbeans.lib.profiler.heap.JavaClass,java.lang.String,org.netbeans.modules.profiler.heapwalk.model.HeapWalkerNode,int)
intf org.netbeans.modules.profiler.heapwalk.model.RootNode
meth public abstract void refreshView()
supr org.netbeans.modules.profiler.heapwalk.model.ClassNode

CLSS public abstract org.netbeans.modules.profiler.heapwalk.model.HeapWalkerNode
cons public init()
fld public final static int MODE_FIELDS = 1
fld public final static int MODE_FIELDS_NO_CLASSLOADER = 3
fld public final static int MODE_REFERENCES = 2
meth public abstract boolean currentlyHasChildren()
meth public abstract boolean isModeFields()
meth public abstract boolean isRoot()
meth public abstract int getMode()
meth public abstract java.lang.Object getNodeID()
meth public abstract java.lang.String getDetails()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getRetainedSize()
meth public abstract java.lang.String getSimpleType()
meth public abstract java.lang.String getSize()
meth public abstract java.lang.String getType()
meth public abstract java.lang.String getValue()
meth public abstract javax.swing.Icon getIcon()
meth public abstract org.netbeans.modules.profiler.heapwalk.model.HeapWalkerNode getChild(int)
meth public abstract org.netbeans.modules.profiler.heapwalk.model.HeapWalkerNode getParent()
meth public abstract org.netbeans.modules.profiler.heapwalk.model.HeapWalkerNode[] getChildren()
meth public static javax.swing.tree.TreePath fromNode(javax.swing.tree.TreeNode)
meth public static javax.swing.tree.TreePath fromNode(javax.swing.tree.TreeNode,javax.swing.tree.TreeNode)
supr org.netbeans.lib.profiler.results.CCTNode

CLSS public abstract interface org.netbeans.modules.profiler.heapwalk.model.RootNode
fld public final static int BROWSER_FIELDS = 1
fld public final static int BROWSER_REFERENCES = 2
meth public abstract java.lang.String getDetails(org.netbeans.lib.profiler.heap.Instance)
meth public abstract org.netbeans.lib.profiler.heap.GCRoot getGCRoot(org.netbeans.lib.profiler.heap.Instance)
meth public abstract org.netbeans.lib.profiler.heap.JavaClass getJavaClassByID(long)
meth public abstract void refreshView()
meth public abstract void repaintView()

CLSS public abstract interface org.netbeans.modules.profiler.heapwalk.ui.icons.HeapWalkerIcons
fld public final static java.lang.String BIGGEST_OBJECTS = "HeapWalkerIcons.BiggestObjects"
fld public final static java.lang.String CLASSES = "HeapWalkerIcons.Classes"
fld public final static java.lang.String DATA = "HeapWalkerIcons.Data"
fld public final static java.lang.String GC_ROOT = "HeapWalkerIcons.GcRoot"
fld public final static java.lang.String GC_ROOTS = "HeapWalkerIcons.GcRoots"
fld public final static java.lang.String INCOMING_REFERENCES = "HeapWalkerIcons.IncomingReferences"
fld public final static java.lang.String INSTANCES = "HeapWalkerIcons.Instances"
fld public final static java.lang.String LOOP = "HeapWalkerIcons.Loop"
fld public final static java.lang.String MEMORY_LINT = "HeapWalkerIcons.MemoryLint"
fld public final static java.lang.String OQL_CONSOLE = "HeapWalkerIcons.OQLConsole"
fld public final static java.lang.String PROGRESS = "HeapWalkerIcons.Progress"
fld public final static java.lang.String PROPERTIES = "HeapWalkerIcons.Properties"
fld public final static java.lang.String RULES = "HeapWalkerIcons.Rules"
fld public final static java.lang.String SAVED_OQL_QUERIES = "HeapWalkerIcons.SavedOqlQueries"
fld public final static java.lang.String STATIC = "HeapWalkerIcons.Static"
fld public final static java.lang.String SYSTEM_INFO = "HeapWalkerIcons.SystemInfo"
fld public final static java.lang.String WINDOW = "HeapWalkerIcons.Window"
intf org.netbeans.modules.profiler.api.icons.Icons$Keys

