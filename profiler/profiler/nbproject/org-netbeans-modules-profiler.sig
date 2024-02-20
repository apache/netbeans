#Signature file v4.1
#Version 3.52

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

CLSS public java.beans.FeatureDescriptor
cons public init()
meth public boolean isExpert()
meth public boolean isHidden()
meth public boolean isPreferred()
meth public java.lang.Object getValue(java.lang.String)
meth public java.lang.String getDisplayName()
meth public java.lang.String getName()
meth public java.lang.String getShortDescription()
meth public java.lang.String toString()
meth public java.util.Enumeration<java.lang.String> attributeNames()
meth public void setDisplayName(java.lang.String)
meth public void setExpert(boolean)
meth public void setHidden(boolean)
meth public void setName(java.lang.String)
meth public void setPreferred(boolean)
meth public void setShortDescription(java.lang.String)
meth public void setValue(java.lang.String,java.lang.Object)
supr java.lang.Object

CLSS public abstract interface java.io.Externalizable
intf java.io.Serializable
meth public abstract void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public abstract void writeExternal(java.io.ObjectOutput) throws java.io.IOException

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

CLSS public abstract javax.swing.AbstractListModel<%0 extends java.lang.Object>
cons public init()
fld protected javax.swing.event.EventListenerList listenerList
intf java.io.Serializable
intf javax.swing.ListModel<{javax.swing.AbstractListModel%0}>
meth protected void fireContentsChanged(java.lang.Object,int,int)
meth protected void fireIntervalAdded(java.lang.Object,int,int)
meth protected void fireIntervalRemoved(java.lang.Object,int,int)
meth public <%0 extends java.util.EventListener> {%%0}[] getListeners(java.lang.Class<{%%0}>)
meth public javax.swing.event.ListDataListener[] getListDataListeners()
meth public void addListDataListener(javax.swing.event.ListDataListener)
meth public void removeListDataListener(javax.swing.event.ListDataListener)
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

CLSS public javax.swing.JEditorPane
cons public init()
cons public init(java.lang.String) throws java.io.IOException
cons public init(java.lang.String,java.lang.String)
cons public init(java.net.URL) throws java.io.IOException
fld public final static java.lang.String HONOR_DISPLAY_PROPERTIES = "JEditorPane.honorDisplayProperties"
fld public final static java.lang.String W3C_LENGTH_UNITS = "JEditorPane.w3cLengthUnits"
innr protected AccessibleJEditorPane
innr protected AccessibleJEditorPaneHTML
innr protected JEditorPaneAccessibleHypertextSupport
meth protected java.io.InputStream getStream(java.net.URL) throws java.io.IOException
meth protected java.lang.String paramString()
meth protected javax.swing.text.EditorKit createDefaultEditorKit()
meth public boolean getScrollableTracksViewportHeight()
meth public boolean getScrollableTracksViewportWidth()
meth public final java.lang.String getContentType()
meth public final void setContentType(java.lang.String)
meth public java.awt.Dimension getPreferredSize()
meth public java.lang.String getText()
meth public java.lang.String getUIClassID()
meth public java.net.URL getPage()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.event.HyperlinkListener[] getHyperlinkListeners()
meth public javax.swing.text.EditorKit getEditorKit()
meth public javax.swing.text.EditorKit getEditorKitForContentType(java.lang.String)
meth public static java.lang.String getEditorKitClassNameForContentType(java.lang.String)
meth public static javax.swing.text.EditorKit createEditorKitForContentType(java.lang.String)
meth public static void registerEditorKitForContentType(java.lang.String,java.lang.String)
meth public static void registerEditorKitForContentType(java.lang.String,java.lang.String,java.lang.ClassLoader)
meth public void addHyperlinkListener(javax.swing.event.HyperlinkListener)
meth public void fireHyperlinkUpdate(javax.swing.event.HyperlinkEvent)
meth public void read(java.io.InputStream,java.lang.Object) throws java.io.IOException
meth public void removeHyperlinkListener(javax.swing.event.HyperlinkListener)
meth public void replaceSelection(java.lang.String)
meth public void scrollToReference(java.lang.String)
meth public void setEditorKit(javax.swing.text.EditorKit)
meth public void setEditorKitForContentType(java.lang.String,javax.swing.text.EditorKit)
meth public void setPage(java.lang.String) throws java.io.IOException
meth public void setPage(java.net.URL) throws java.io.IOException
meth public void setText(java.lang.String)
supr javax.swing.text.JTextComponent

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

CLSS public abstract interface javax.swing.Scrollable
meth public abstract boolean getScrollableTracksViewportHeight()
meth public abstract boolean getScrollableTracksViewportWidth()
meth public abstract int getScrollableBlockIncrement(java.awt.Rectangle,int,int)
meth public abstract int getScrollableUnitIncrement(java.awt.Rectangle,int,int)
meth public abstract java.awt.Dimension getPreferredScrollableViewportSize()

CLSS public abstract interface javax.swing.event.HyperlinkListener
intf java.util.EventListener
meth public abstract void hyperlinkUpdate(javax.swing.event.HyperlinkEvent)

CLSS public abstract interface javax.swing.event.ListSelectionListener
intf java.util.EventListener
meth public abstract void valueChanged(javax.swing.event.ListSelectionEvent)

CLSS public abstract javax.swing.text.JTextComponent
cons public init()
fld public final static java.lang.String DEFAULT_KEYMAP = "default"
fld public final static java.lang.String FOCUS_ACCELERATOR_KEY = "focusAcceleratorKey"
innr public AccessibleJTextComponent
innr public final static DropLocation
innr public static KeyBinding
intf javax.accessibility.Accessible
intf javax.swing.Scrollable
meth protected boolean saveComposedText(int)
meth protected java.lang.String paramString()
meth protected void fireCaretUpdate(javax.swing.event.CaretEvent)
meth protected void processInputMethodEvent(java.awt.event.InputMethodEvent)
meth protected void restoreComposedText()
meth public boolean getDragEnabled()
meth public boolean getScrollableTracksViewportHeight()
meth public boolean getScrollableTracksViewportWidth()
meth public boolean isEditable()
meth public boolean print() throws java.awt.print.PrinterException
meth public boolean print(java.text.MessageFormat,java.text.MessageFormat) throws java.awt.print.PrinterException
meth public boolean print(java.text.MessageFormat,java.text.MessageFormat,boolean,javax.print.PrintService,javax.print.attribute.PrintRequestAttributeSet,boolean) throws java.awt.print.PrinterException
meth public char getFocusAccelerator()
meth public final javax.swing.DropMode getDropMode()
meth public final javax.swing.text.JTextComponent$DropLocation getDropLocation()
meth public final void setDropMode(javax.swing.DropMode)
meth public int getCaretPosition()
meth public int getScrollableBlockIncrement(java.awt.Rectangle,int,int)
meth public int getScrollableUnitIncrement(java.awt.Rectangle,int,int)
meth public int getSelectionEnd()
meth public int getSelectionStart()
meth public int viewToModel(java.awt.Point)
meth public java.awt.Color getCaretColor()
meth public java.awt.Color getDisabledTextColor()
meth public java.awt.Color getSelectedTextColor()
meth public java.awt.Color getSelectionColor()
meth public java.awt.Dimension getPreferredScrollableViewportSize()
meth public java.awt.Insets getMargin()
meth public java.awt.Rectangle modelToView(int) throws javax.swing.text.BadLocationException
meth public java.awt.im.InputMethodRequests getInputMethodRequests()
meth public java.awt.print.Printable getPrintable(java.text.MessageFormat,java.text.MessageFormat)
meth public java.lang.String getSelectedText()
meth public java.lang.String getText()
meth public java.lang.String getText(int,int) throws javax.swing.text.BadLocationException
meth public java.lang.String getToolTipText(java.awt.event.MouseEvent)
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.Action[] getActions()
meth public javax.swing.event.CaretListener[] getCaretListeners()
meth public javax.swing.plaf.TextUI getUI()
meth public javax.swing.text.Caret getCaret()
meth public javax.swing.text.Document getDocument()
meth public javax.swing.text.Highlighter getHighlighter()
meth public javax.swing.text.Keymap getKeymap()
meth public javax.swing.text.NavigationFilter getNavigationFilter()
meth public static javax.swing.text.Keymap addKeymap(java.lang.String,javax.swing.text.Keymap)
meth public static javax.swing.text.Keymap getKeymap(java.lang.String)
meth public static javax.swing.text.Keymap removeKeymap(java.lang.String)
meth public static void loadKeymap(javax.swing.text.Keymap,javax.swing.text.JTextComponent$KeyBinding[],javax.swing.Action[])
meth public void addCaretListener(javax.swing.event.CaretListener)
meth public void addInputMethodListener(java.awt.event.InputMethodListener)
meth public void copy()
meth public void cut()
meth public void moveCaretPosition(int)
meth public void paste()
meth public void read(java.io.Reader,java.lang.Object) throws java.io.IOException
meth public void removeCaretListener(javax.swing.event.CaretListener)
meth public void removeNotify()
meth public void replaceSelection(java.lang.String)
meth public void select(int,int)
meth public void selectAll()
meth public void setCaret(javax.swing.text.Caret)
meth public void setCaretColor(java.awt.Color)
meth public void setCaretPosition(int)
meth public void setComponentOrientation(java.awt.ComponentOrientation)
meth public void setDisabledTextColor(java.awt.Color)
meth public void setDocument(javax.swing.text.Document)
meth public void setDragEnabled(boolean)
meth public void setEditable(boolean)
meth public void setFocusAccelerator(char)
meth public void setHighlighter(javax.swing.text.Highlighter)
meth public void setKeymap(javax.swing.text.Keymap)
meth public void setMargin(java.awt.Insets)
meth public void setNavigationFilter(javax.swing.text.NavigationFilter)
meth public void setSelectedTextColor(java.awt.Color)
meth public void setSelectionColor(java.awt.Color)
meth public void setSelectionEnd(int)
meth public void setSelectionStart(int)
meth public void setText(java.lang.String)
meth public void setUI(javax.swing.plaf.TextUI)
meth public void updateUI()
meth public void write(java.io.Writer) throws java.io.IOException
supr javax.swing.JComponent

CLSS public abstract interface org.netbeans.api.actions.Openable
meth public abstract void open()

CLSS public abstract interface org.netbeans.lib.profiler.client.AppStatusHandler
innr public abstract interface static AsyncDialog
innr public abstract interface static ServerCommandHandler
meth public abstract boolean confirmWaitForConnectionReply()
meth public abstract org.netbeans.lib.profiler.client.AppStatusHandler$AsyncDialog getAsyncDialogInstance(java.lang.String,boolean,java.lang.Runnable)
meth public abstract void displayError(java.lang.String)
meth public abstract void displayErrorAndWaitForConfirm(java.lang.String)
meth public abstract void displayErrorWithDetailsAndWaitForConfirm(java.lang.String,java.lang.String)
meth public abstract void displayNotification(java.lang.String)
meth public abstract void displayNotificationAndWaitForConfirm(java.lang.String)
meth public abstract void displayNotificationWithDetailsAndWaitForConfirm(java.lang.String,java.lang.String)
meth public abstract void displayWarning(java.lang.String)
meth public abstract void displayWarningAndWaitForConfirm(java.lang.String)
meth public abstract void handleShutdown()
meth public abstract void pauseLiveUpdates()
meth public abstract void resultsAvailable()
meth public abstract void resumeLiveUpdates()
meth public abstract void takeSnapshot()

CLSS public abstract interface static org.netbeans.lib.profiler.client.AppStatusHandler$AsyncDialog
 outer org.netbeans.lib.profiler.client.AppStatusHandler
meth public abstract void close()
meth public abstract void display()

CLSS public abstract org.netbeans.lib.profiler.common.Profiler
cons public init()
fld public final static int ERROR = 16
fld public final static int EXCEPTION = 8
fld public final static int INFORMATIONAL = 1
fld public final static int MODE_ATTACH = 0
fld public final static int MODE_PROFILE = 1
fld public final static int PROFILING_INACTIVE = 1
fld public final static int PROFILING_IN_TRANSITION = 128
fld public final static int PROFILING_PAUSED = 8
fld public final static int PROFILING_RUNNING = 4
fld public final static int PROFILING_STARTED = 2
fld public final static int PROFILING_STOPPED = 16
fld public final static int USER = 4
fld public final static int WARNING = 2
meth protected final void fireInstrumentationChanged(int,int)
meth protected final void fireLockContentionMonitoringChange()
meth protected final void fireProfilingStateChange(int,int)
meth protected final void fireServerStateChanged(int,int)
meth protected final void fireThreadsMonitoringChange()
meth public abstract boolean attachToApp(org.netbeans.lib.profiler.common.ProfilingSettings,org.netbeans.lib.profiler.common.AttachSettings)
meth public abstract boolean connectToStartedApp(org.netbeans.lib.profiler.common.ProfilingSettings,org.netbeans.lib.profiler.common.SessionSettings)
meth public abstract boolean getLockContentionMonitoringEnabled()
meth public abstract boolean getThreadsMonitoringEnabled()
meth public abstract boolean modifyAvailable()
meth public abstract boolean profileClass(org.netbeans.lib.profiler.common.ProfilingSettings,org.netbeans.lib.profiler.common.SessionSettings)
meth public abstract boolean rerunAvailable()
meth public abstract boolean runCalibration(boolean,java.lang.String,java.lang.String,int)
meth public abstract boolean shutdownBlockedAgent(java.lang.String,int,int)
meth public abstract int getAgentState(java.lang.String,int,int)
meth public abstract int getPlatformArchitecture(java.lang.String)
meth public abstract int getProfilingMode()
meth public abstract int getProfilingState()
meth public abstract int getServerProgress()
meth public abstract int getServerState()
meth public abstract java.lang.String getLibsDir()
meth public abstract java.lang.String getPlatformJDKVersion(java.lang.String)
meth public abstract java.lang.String getPlatformJavaFile(java.lang.String)
meth public abstract org.netbeans.lib.profiler.TargetAppRunner getTargetAppRunner()
meth public abstract org.netbeans.lib.profiler.common.GlobalProfilingSettings getGlobalProfilingSettings()
meth public abstract org.netbeans.lib.profiler.common.ProfilingSettings getLastProfilingSettings()
meth public abstract org.netbeans.lib.profiler.common.SessionSettings getCurrentSessionSettings()
meth public abstract org.netbeans.lib.profiler.results.monitor.VMTelemetryDataManager getVMTelemetryManager()
meth public abstract org.netbeans.lib.profiler.results.threads.ThreadsDataManager getThreadsManager()
meth public abstract void detachFromApp()
meth public abstract void instrumentSelectedRoots(org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection[]) throws java.io.IOException,java.lang.ClassNotFoundException,org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated,org.netbeans.lib.profiler.instrumentation.BadLocationException,org.netbeans.lib.profiler.instrumentation.InstrumentationException
meth public abstract void log(int,java.lang.String)
meth public abstract void modifyCurrentProfiling(org.netbeans.lib.profiler.common.ProfilingSettings)
meth public abstract void notifyException(int,java.lang.Exception)
meth public abstract void openJavaSource(java.lang.String,java.lang.String,java.lang.String)
meth public abstract void rerunLastProfiling()
meth public abstract void setLockContentionMonitoringEnabled(boolean)
meth public abstract void setThreadsMonitoringEnabled(boolean)
meth public abstract void stopApp()
meth public boolean prepareInstrumentation(org.netbeans.lib.profiler.common.ProfilingSettings) throws java.io.IOException,java.lang.ClassNotFoundException,org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated,org.netbeans.lib.profiler.instrumentation.BadLocationException,org.netbeans.lib.profiler.instrumentation.InstrumentationException
meth public final boolean profilingInProgress()
meth public final void addProfilingStateListener(org.netbeans.lib.profiler.common.event.ProfilingStateListener)
meth public final void removeProfilingStateListener(org.netbeans.lib.profiler.common.event.ProfilingStateListener)
meth public static org.netbeans.lib.profiler.common.Profiler getDefault()
meth public static void debug(java.lang.Exception)
meth public static void debug(java.lang.String)
supr java.lang.Object
hfds DEBUG,currentProfilingState,defaultProfiler,profilingStateListeners

CLSS public abstract interface org.netbeans.lib.profiler.results.CCTProvider
innr public abstract interface static Listener
meth public abstract void addListener(org.netbeans.lib.profiler.results.CCTProvider$Listener)
meth public abstract void removeAllListeners()
meth public abstract void removeListener(org.netbeans.lib.profiler.results.CCTProvider$Listener)

CLSS public abstract interface static org.netbeans.lib.profiler.results.CCTProvider$Listener
 outer org.netbeans.lib.profiler.results.CCTProvider
meth public abstract void cctEstablished(org.netbeans.lib.profiler.results.RuntimeCCTNode,boolean)
meth public abstract void cctReset()

CLSS public abstract interface org.netbeans.lib.profiler.results.cpu.CPUCCTProvider
innr public abstract interface static Listener
intf org.netbeans.lib.profiler.results.CCTProvider
meth public abstract org.netbeans.lib.profiler.results.cpu.CPUCCTContainer[] createPresentationCCTs(org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot)

CLSS public abstract interface static org.netbeans.lib.profiler.results.cpu.CPUCCTProvider$Listener
 outer org.netbeans.lib.profiler.results.cpu.CPUCCTProvider
intf org.netbeans.lib.profiler.results.CCTProvider$Listener

CLSS public abstract interface org.netbeans.lib.profiler.results.memory.MemoryCCTProvider
innr public abstract interface static Listener
innr public static ObjectNumbersContainer
intf org.netbeans.lib.profiler.results.CCTProvider
meth public abstract boolean classMarkedUnprofiled(int)
meth public abstract int getCurrentEpoch()
meth public abstract int getNProfiledClasses()
meth public abstract long[] getAllocObjectNumbers()
meth public abstract long[] getObjectsSizePerClass()
meth public abstract org.netbeans.lib.profiler.results.memory.MemoryCCTProvider$ObjectNumbersContainer getLivenessObjectNumbers()
meth public abstract org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode createPresentationCCT(int,boolean) throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public abstract org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode[] getStacksForClasses()
meth public abstract void beginTrans(boolean)
meth public abstract void endTrans()
meth public abstract void markClassUnprofiled(int)
meth public abstract void updateInternals()

CLSS public abstract interface static org.netbeans.lib.profiler.results.memory.MemoryCCTProvider$Listener
 outer org.netbeans.lib.profiler.results.memory.MemoryCCTProvider
intf org.netbeans.lib.profiler.results.CCTProvider$Listener

CLSS public abstract org.netbeans.lib.profiler.ui.SwingWorker
cons public init()
cons public init(boolean)
cons public init(boolean,java.util.concurrent.Semaphore)
cons public init(java.util.concurrent.Semaphore)
meth protected abstract void doInBackground()
meth protected final boolean isCancelled()
meth protected int getWarmup()
meth protected void cancelled()
meth protected void done()
meth protected void nonResponding()
meth protected void postRunnable(java.lang.Runnable)
meth public final void cancel()
meth public void execute()
supr java.lang.Object
hfds cancelFlag,primed,taskService,throughputSemaphore,useEQ,warmupLock,warmupService,warmupTimer,workerRunning

CLSS public org.netbeans.lib.profiler.ui.components.HTMLLabel
cons public init()
cons public init(java.lang.String)
intf javax.swing.event.HyperlinkListener
meth protected void showURL(java.net.URL)
meth public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent)
meth public void setBackground(java.awt.Color)
meth public void setForeground(java.awt.Color)
meth public void setHorizontalAlignment(int)
meth public void setOpaque(boolean)
meth public void setText(java.lang.String)
supr javax.swing.JEditorPane
hfds halign,txt

CLSS public org.netbeans.modules.profiler.HeapDumpWatch
cons public init()
meth public static java.lang.String getHeapDumpPath(org.openide.util.Lookup$Provider)
meth public void onShutdown()
meth public void onStartup(org.netbeans.lib.profiler.common.ProfilingSettings,org.openide.util.Lookup$Provider)
supr org.netbeans.modules.profiler.spi.SessionListener$Adapter
hfds LOG,listener,monitoredPath
hcls HeapDumpFolderListener

CLSS public org.netbeans.modules.profiler.LoadedSnapshot
cons public init(org.netbeans.lib.profiler.results.ResultsSnapshot,org.netbeans.lib.profiler.common.ProfilingSettings,java.io.File,org.openide.util.Lookup$Provider)
fld public final static int SNAPSHOT_TYPE_CODEFRAGMENT = 2
fld public final static int SNAPSHOT_TYPE_CPU = 1
fld public final static int SNAPSHOT_TYPE_CPU_JDBC = 32
fld public final static int SNAPSHOT_TYPE_MEMORY = 28
fld public final static int SNAPSHOT_TYPE_MEMORY_ALLOCATIONS = 4
fld public final static int SNAPSHOT_TYPE_MEMORY_LIVENESS = 8
fld public final static int SNAPSHOT_TYPE_MEMORY_SAMPLED = 16
fld public final static int SNAPSHOT_TYPE_UNKNOWN = 0
fld public final static java.lang.String PROFILER_FILE_MAGIC_STRING = "nBpRoFiLeR"
meth public boolean isSaved()
meth public int getType()
meth public java.io.File getFile()
meth public java.lang.String getUserComments()
meth public java.lang.String toString()
meth public org.netbeans.lib.profiler.common.ProfilingSettings getSettings()
meth public org.netbeans.lib.profiler.results.ResultsSnapshot getSnapshot()
meth public org.openide.util.Lookup$Provider getProject()
meth public static org.netbeans.modules.profiler.LoadedSnapshot loadSnapshot(java.io.DataInputStream) throws java.io.IOException
meth public void save(java.io.DataOutputStream) throws java.io.IOException
meth public void setFile(java.io.File)
meth public void setProject(org.openide.util.Lookup$Provider)
meth public void setSaved(boolean)
meth public void setUserComments(java.lang.String)
supr java.lang.Object
hfds LOGGER,SNAPSHOT_FILE_VERSION_MAJOR,SNAPSHOT_FILE_VERSION_MINOR,file,project,saved,settings,snapshot,userComments
hcls SamplesInputStream,SubInputStream,ThreadsSample

CLSS public abstract org.netbeans.modules.profiler.NetBeansProfiler
cons public init()
innr public final static ProgressPanel
meth protected boolean shouldOpenWindowsOnProfilingStart()
meth protected void cleanupAfterProfiling()
meth public abstract java.lang.String getLibsDir()
meth public boolean attachToApp(org.netbeans.lib.profiler.common.ProfilingSettings,org.netbeans.lib.profiler.common.AttachSettings)
meth public boolean cleanForProfilingOnPort(int)
meth public boolean connectToStartedApp(org.netbeans.lib.profiler.common.ProfilingSettings,org.netbeans.lib.profiler.common.SessionSettings)
meth public boolean connectToStartedApp(org.netbeans.lib.profiler.common.ProfilingSettings,org.netbeans.lib.profiler.common.SessionSettings,java.util.concurrent.atomic.AtomicBoolean)
meth public boolean getLockContentionMonitoringEnabled()
meth public boolean getThreadsMonitoringEnabled()
meth public boolean prepareInstrumentation(org.netbeans.lib.profiler.common.ProfilingSettings)
meth public boolean processesProfilingPoints()
meth public boolean profileClass(org.netbeans.lib.profiler.common.ProfilingSettings,org.netbeans.lib.profiler.common.SessionSettings)
meth public boolean runCalibration(boolean,java.lang.String,java.lang.String,int)
meth public boolean runConfiguredCalibration()
meth public boolean shutdownBlockedAgent(java.lang.String,int,int)
meth public boolean startEx(org.netbeans.lib.profiler.common.ProfilingSettings,org.netbeans.lib.profiler.common.SessionSettings,java.util.concurrent.atomic.AtomicBoolean)
meth public int getAgentState(java.lang.String,int,int)
meth public int getPlatformArchitecture(java.lang.String)
meth public int getProfilingMode()
meth public int getProfilingState()
meth public int getServerProgress()
meth public int getServerState()
meth public java.lang.String getPlatformJDKVersion(java.lang.String)
meth public java.lang.String getPlatformJavaFile(java.lang.String)
meth public org.netbeans.lib.profiler.TargetAppRunner getTargetAppRunner()
meth public org.netbeans.lib.profiler.common.GlobalProfilingSettings getGlobalProfilingSettings()
meth public org.netbeans.lib.profiler.common.ProfilingSettings getLastProfilingSettings()
meth public org.netbeans.lib.profiler.common.SessionSettings getCurrentSessionSettings()
meth public org.netbeans.lib.profiler.results.monitor.VMTelemetryDataManager getVMTelemetryManager()
meth public org.netbeans.lib.profiler.results.threads.ThreadsDataManager getThreadsManager()
meth public org.netbeans.lib.profiler.ui.monitor.VMTelemetryModels getVMTelemetryModels()
meth public org.openide.filesystems.FileObject getProfiledSingleFile()
meth public org.openide.util.Lookup$Provider getProfiledProject()
meth public static boolean isInitialized()
meth public static org.netbeans.modules.profiler.NetBeansProfiler getDefaultNB()
meth public void checkAndUpdateState()
meth public void detachFromApp()
meth public void instrumentSelectedRoots(org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection[]) throws java.io.IOException,java.lang.ClassNotFoundException,org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated,org.netbeans.lib.profiler.instrumentation.BadLocationException,org.netbeans.lib.profiler.instrumentation.InstrumentationException
meth public void log(int,java.lang.String)
meth public void modifyCurrentProfiling(org.netbeans.lib.profiler.common.ProfilingSettings)
meth public void notifyException(int,java.lang.Exception)
meth public void openJavaSource(java.lang.String,java.lang.String,java.lang.String)
meth public void setLockContentionMonitoringEnabled(boolean)
meth public void setProfiledProject(org.openide.util.Lookup$Provider,org.openide.filesystems.FileObject)
meth public void setThreadsMonitoringEnabled(boolean)
meth public void setupDispatcher(org.netbeans.lib.profiler.common.ProfilingSettings)
meth public void shutdown()
meth public void stopApp()
supr org.netbeans.lib.profiler.common.Profiler
hfds LOGGER,calibrating,ideSettings,initialized,lastMode,lastProfilingSettings,lastSessionSettings,lockContentionMonitoringEnabled,logMsgs,monitor,profiledProject,profiledSingleFile,profilingMode,profilingState,setupLock,sharedSettings,shouldDisplayDialog,targetAppRunner,threadsManager,threadsMonitoringEnabled,vmTelemetryManager,vmTelemetryModels,waitDialogOpen
hcls IDEAppStatusHandler

CLSS public final static org.netbeans.modules.profiler.NetBeansProfiler$ProgressPanel
 outer org.netbeans.modules.profiler.NetBeansProfiler
intf org.netbeans.lib.profiler.client.AppStatusHandler$AsyncDialog
meth public void close()
meth public void display()
supr java.lang.Object
hfds MINIMUM_WIDTH,cancelHandler,closed,dialog,message,opened,showProgress

CLSS public final org.netbeans.modules.profiler.ProfilerModule
cons public init()
fld public final static java.lang.String LIBS_DIR = "lib"
meth public boolean closing()
meth public void restored()
meth public void uninstalled()
supr org.openide.modules.ModuleInstall

CLSS public org.netbeans.modules.profiler.ProfilerTopComponent
cons public init()
fld public final static java.lang.String RECENT_FILE_KEY = "nb.recent.file.path"
meth protected java.awt.Component defaultFocusOwner()
meth protected void componentActivated()
meth protected void componentDeactivated()
meth public java.awt.Dimension getMinimumSize()
meth public void paintComponent(java.awt.Graphics)
supr org.openide.windows.TopComponent
hfds focusListener,lastFocusOwner

CLSS public final org.netbeans.modules.profiler.ProfilingMonitor
cons public init()
meth public void monitorVM(org.netbeans.lib.profiler.TargetAppRunner)
meth public void stopDisplayingVM()
meth public void stopUpdateThread()
supr java.lang.Object
hfds PROPERTY_SERVER_PROGRESS,PROPERTY_SERVER_STATE,monitorThread,propertyChangeSupport,serverProgress,serverState,updateThreadStarted
hcls UpdateThread

CLSS public abstract interface org.netbeans.modules.profiler.ResultsListener
meth public abstract void resultsAvailable()
meth public abstract void resultsReset()

CLSS public final org.netbeans.modules.profiler.ResultsManager
fld public final static java.lang.String HEAPDUMP_EXTENSION = "hprof"
fld public final static java.lang.String SNAPSHOT_EXTENSION = "nps"
fld public final static java.lang.String STACKTRACES_SNAPSHOT_EXTENSION = "npss"
innr public abstract interface static SnapshotHandle
innr public final static ResultsMonitor
meth protected void fireResultsAvailable()
meth protected void fireResultsReset()
meth protected void fireSnapshotLoaded(org.netbeans.modules.profiler.LoadedSnapshot)
meth protected void fireSnapshotRemoved(org.netbeans.modules.profiler.LoadedSnapshot)
meth protected void fireSnapshotSaved(org.netbeans.modules.profiler.LoadedSnapshot)
meth protected void fireSnapshotTaken(org.netbeans.modules.profiler.LoadedSnapshot)
meth public boolean hasSnapshotsFor(org.openide.util.Lookup$Provider)
meth public boolean resultsAvailable()
meth public boolean saveSnapshot(org.netbeans.modules.profiler.LoadedSnapshot)
meth public boolean saveSnapshot(org.netbeans.modules.profiler.LoadedSnapshot,org.openide.filesystems.FileObject)
meth public int getSnapshotType(org.openide.filesystems.FileObject)
meth public int getSnapshotsCountFor(org.openide.util.Lookup$Provider)
meth public java.lang.String getDefaultHeapDumpFileName(long)
meth public java.lang.String getDefaultSnapshotFileName(org.netbeans.modules.profiler.LoadedSnapshot)
meth public java.lang.String getHeapDumpDisplayName(java.lang.String)
meth public java.lang.String getSnapshotDisplayName(java.lang.String,int)
meth public java.lang.String getSnapshotDisplayName(org.netbeans.modules.profiler.LoadedSnapshot)
meth public org.netbeans.lib.profiler.common.ProfilingSettings getSnapshotSettings(org.openide.filesystems.FileObject)
meth public org.netbeans.lib.profiler.results.ResultsSnapshot createDiffSnapshot(org.netbeans.modules.profiler.LoadedSnapshot,org.netbeans.modules.profiler.LoadedSnapshot)
meth public org.netbeans.lib.profiler.ui.swing.ExportUtils$Exportable createSnapshotExporter(org.netbeans.modules.profiler.LoadedSnapshot)
meth public org.netbeans.lib.profiler.ui.swing.ExportUtils$Exportable createSnapshotExporter(org.netbeans.modules.profiler.ResultsManager$SnapshotHandle)
meth public org.netbeans.modules.profiler.LoadedSnapshot findLoadedSnapshot(java.io.File)
meth public org.netbeans.modules.profiler.LoadedSnapshot findLoadedSnapshot(org.netbeans.lib.profiler.results.ResultsSnapshot)
meth public org.netbeans.modules.profiler.LoadedSnapshot getSnapshotFromFileObject(org.openide.filesystems.FileObject)
meth public org.netbeans.modules.profiler.LoadedSnapshot loadSnapshot(org.openide.filesystems.FileObject)
meth public org.netbeans.modules.profiler.LoadedSnapshot prepareSnapshot()
meth public org.netbeans.modules.profiler.LoadedSnapshot prepareSnapshot(boolean)
meth public org.netbeans.modules.profiler.LoadedSnapshot takeSnapshot()
meth public org.netbeans.modules.profiler.LoadedSnapshot[] getLoadedSnapshots()
meth public org.netbeans.modules.profiler.LoadedSnapshot[] loadSnapshots(org.openide.filesystems.FileObject[])
meth public org.openide.filesystems.FileObject[] listSavedHeapdumps(org.openide.util.Lookup$Provider,java.io.File)
meth public org.openide.filesystems.FileObject[] listSavedSnapshots(org.openide.util.Lookup$Provider,java.io.File)
meth public static boolean checkHprofFile(java.io.File)
meth public static org.netbeans.modules.profiler.ResultsManager getDefault()
meth public void closeSnapshot(org.netbeans.modules.profiler.LoadedSnapshot)
meth public void compareSnapshots(org.netbeans.modules.profiler.LoadedSnapshot,org.netbeans.modules.profiler.LoadedSnapshot)
meth public void compareSnapshots(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject)
meth public void deleteSnapshot(org.openide.filesystems.FileObject)
meth public void exportSnapshots(org.openide.filesystems.FileObject[])
meth public void openSnapshot(java.io.File)
meth public void openSnapshot(org.netbeans.modules.profiler.LoadedSnapshot)
meth public void openSnapshot(org.netbeans.modules.profiler.LoadedSnapshot,int,boolean)
meth public void openSnapshot(org.openide.filesystems.FileObject)
meth public void openSnapshots(org.netbeans.modules.profiler.LoadedSnapshot[])
meth public void reset()
supr java.lang.Object
hfds HEAPDUMP_PREFIX,HPROF_HEADER,LOGGER,MINIMAL_TIMESTAMP,MIN_HPROF_SIZE,SNAPSHOT_PREFIX,exportDir,loadedSnapshots,mainWindow,resultsAvailable,resultsListeners,settingsCache,snapshotListeners,typeCache
hcls SelectedFile,Singleton

CLSS public final static org.netbeans.modules.profiler.ResultsManager$ResultsMonitor
 outer org.netbeans.modules.profiler.ResultsManager
cons public init()
intf org.netbeans.lib.profiler.results.cpu.CPUCCTProvider$Listener
intf org.netbeans.lib.profiler.results.memory.MemoryCCTProvider$Listener
meth public void cctEstablished(org.netbeans.lib.profiler.results.RuntimeCCTNode,boolean)
meth public void cctReset()
supr java.lang.Object

CLSS public abstract interface static org.netbeans.modules.profiler.ResultsManager$SnapshotHandle
 outer org.netbeans.modules.profiler.ResultsManager
meth public abstract org.netbeans.modules.profiler.LoadedSnapshot getSnapshot()

CLSS public final org.netbeans.modules.profiler.SampledCPUSnapshot
cons public init(org.openide.filesystems.FileObject) throws java.io.IOException
fld public final static java.lang.String OPEN_THREADS_URL = "file:/stackframe/"
meth public int getSamplesCount()
meth public java.lang.String getThreadDump(int) throws java.io.IOException
meth public java.util.List<java.lang.Integer> getIntervals(int,int,org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode) throws java.io.IOException
meth public long getStartTime()
meth public long getTimestamp(int) throws java.io.IOException
meth public long getValue(int,int) throws java.io.IOException
meth public org.netbeans.modules.profiler.LoadedSnapshot getCPUSnapshot(int,int) throws java.io.IOException
supr java.lang.Object
hfds builder,currentIndex,lastTimestamp,npssFile,sample,samples,samplesStream,startTime

CLSS public org.netbeans.modules.profiler.SnapshotInfoPanel
cons public init(org.netbeans.modules.profiler.LoadedSnapshot)
meth public boolean fitsVisibleArea()
meth public java.awt.image.BufferedImage getCurrentViewScreenshot(boolean)
meth public void setUserComments(java.lang.String)
meth public void updateInfo()
supr javax.swing.JPanel
hfds HELP_CTX,infoArea,infoAreaScrollPane,loadedSnapshot
hcls UserCommentsPanel

CLSS public final org.netbeans.modules.profiler.SnapshotResultsWindow
cons public init()
cons public init(org.netbeans.modules.profiler.LoadedSnapshot,int,boolean)
innr public abstract interface static FindPerformer
innr public static SnapshotListener
meth protected java.awt.Component defaultFocusOwner()
meth protected java.lang.String preferredID()
meth protected void componentClosed()
meth public boolean canClose()
meth public int getPersistenceType()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static boolean hasSnapshotWindow(org.netbeans.modules.profiler.LoadedSnapshot)
meth public static org.netbeans.modules.profiler.SnapshotResultsWindow get(org.netbeans.modules.profiler.LoadedSnapshot)
meth public static org.netbeans.modules.profiler.SnapshotResultsWindow get(org.netbeans.modules.profiler.LoadedSnapshot,int,boolean)
meth public static void closeAllWindows()
meth public static void closeWindow(org.netbeans.modules.profiler.LoadedSnapshot)
meth public void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void refreshTabName()
meth public void updateTitle()
meth public void writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr org.netbeans.modules.profiler.ProfilerTopComponent
hfds HELP_CTX_KEY_CPU,HELP_CTX_KEY_MEM,PERSISTENCE_VERSION_MAJOR,PERSISTENCE_VERSION_MINOR,WINDOW_ICON_CPU,WINDOW_ICON_FRAGMENT,WINDOW_ICON_JDBC,WINDOW_ICON_MEMORY,displayedPanel,forcedClose,helpCtx,ic,listener,savePerformer,snapshot,tabName,windowsList
hcls SavePerformer

CLSS public abstract interface static org.netbeans.modules.profiler.SnapshotResultsWindow$FindPerformer
 outer org.netbeans.modules.profiler.SnapshotResultsWindow
meth public abstract void performFind()
meth public abstract void performFindNext()
meth public abstract void performFindPrevious()

CLSS public static org.netbeans.modules.profiler.SnapshotResultsWindow$SnapshotListener
 outer org.netbeans.modules.profiler.SnapshotResultsWindow
cons public init()
intf org.netbeans.modules.profiler.SnapshotsListener
meth public void snapshotLoaded(org.netbeans.modules.profiler.LoadedSnapshot)
meth public void snapshotRemoved(org.netbeans.modules.profiler.LoadedSnapshot)
meth public void snapshotSaved(org.netbeans.modules.profiler.LoadedSnapshot)
meth public void snapshotTaken(org.netbeans.modules.profiler.LoadedSnapshot)
supr java.lang.Object
hfds registeredWindows

CLSS public abstract interface org.netbeans.modules.profiler.SnapshotsListener
meth public abstract void snapshotLoaded(org.netbeans.modules.profiler.LoadedSnapshot)
meth public abstract void snapshotRemoved(org.netbeans.modules.profiler.LoadedSnapshot)
meth public abstract void snapshotSaved(org.netbeans.modules.profiler.LoadedSnapshot)
meth public abstract void snapshotTaken(org.netbeans.modules.profiler.LoadedSnapshot)

CLSS public org.netbeans.modules.profiler.ThreadDumpWindow
cons public init()
cons public init(org.netbeans.lib.profiler.results.threads.ThreadDump)
meth protected java.awt.Component defaultFocusOwner()
meth protected java.lang.String preferredID()
meth public int getPersistenceType()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.netbeans.modules.profiler.ProfilerTopComponent
hfds HELP_CTX,HELP_CTX_KEY,a
hcls CustomHTMLDocument,CustomHtmlEditorKit

CLSS public final org.netbeans.modules.profiler.actions.AttachAction
meth public static org.netbeans.modules.profiler.actions.AttachAction getInstance()
meth public void actionPerformed(java.awt.event.ActionEvent)
supr javax.swing.AbstractAction
hcls Singleton

CLSS public org.netbeans.modules.profiler.actions.CompareSnapshotsAction
cons public init()
cons public init(org.netbeans.modules.profiler.LoadedSnapshot)
innr public abstract interface static Performer
meth public static boolean areComparableSnapshots(org.netbeans.modules.profiler.LoadedSnapshot,org.netbeans.modules.profiler.LoadedSnapshot)
meth public static boolean areComparableSnapshots(org.netbeans.modules.profiler.LoadedSnapshot,org.openide.filesystems.FileObject)
meth public static boolean areComparableSnapshots(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject)
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void setPerformer(org.netbeans.modules.profiler.actions.CompareSnapshotsAction$Performer)
supr javax.swing.AbstractAction
hfds EXTERNAL_SNAPSHOT_HELP_CTX,SECOND_SNAPSHOT_HELP_CTX,cpuIcon,externalSnapshotsSelector,fragmentIcon,memoryIcon,performer,secondSnapshotSelector,snapshot,snapshotFileChooser
hcls SelectExternalSnapshotsPanel,SelectSecondSnapshotPanel

CLSS public abstract interface static org.netbeans.modules.profiler.actions.CompareSnapshotsAction$Performer
 outer org.netbeans.modules.profiler.actions.CompareSnapshotsAction
meth public abstract void compare(org.netbeans.modules.profiler.LoadedSnapshot)

CLSS public final org.netbeans.modules.profiler.actions.GetCmdLineArgumentsAction
cons public init()
meth protected int[] enabledStates()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void performAction()
supr org.netbeans.modules.profiler.actions.ProfilingAwareAction
hfds enabledStates

CLSS public final org.netbeans.modules.profiler.actions.HeapDumpAction
cons public init()
meth protected int[] enabledStates()
meth protected void updateAction()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static org.netbeans.modules.profiler.actions.HeapDumpAction getInstance()
meth public void dumpToProject()
meth public void performAction()
supr org.netbeans.modules.profiler.actions.ProfilingAwareAction
hfds ENABLED_STATES,SELECTING_TARGET_CANCELLED,heapdumpTargetSelector,snapshotDirectoryChooser
hcls ChooseHeapdumpTargetPanel,Singleton

CLSS public final org.netbeans.modules.profiler.actions.InternalStatsAction
cons public init()
meth protected int[] enabledStates()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void performAction()
supr org.netbeans.modules.profiler.actions.ProfilingAwareAction
hfds enabledStates

CLSS public final org.netbeans.modules.profiler.actions.JavaPlatformSelector
innr public static JPListModel
intf javax.swing.event.ListSelectionListener
intf org.openide.util.HelpCtx$Provider
meth public org.netbeans.modules.profiler.api.JavaPlatform selectPlatformForCalibration()
meth public org.netbeans.modules.profiler.api.JavaPlatform selectPlatformToUse()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static org.netbeans.modules.profiler.actions.JavaPlatformSelector getDefault()
meth public void valueChanged(javax.swing.event.ListSelectionEvent)
supr javax.swing.JPanel
hfds HELP_CTX,HELP_CTX_KEY,alwaysCheckBox,defaultPlatform,list,noteLabel,okButton

CLSS public static org.netbeans.modules.profiler.actions.JavaPlatformSelector$JPListModel
 outer org.netbeans.modules.profiler.actions.JavaPlatformSelector
meth public int getSize()
meth public java.lang.Object getElementAt(int)
supr javax.swing.AbstractListModel
hfds platforms

CLSS public final org.netbeans.modules.profiler.actions.LoadSnapshotAction
cons public init()
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void loadSnapshotOrHeapdump()
supr javax.swing.AbstractAction
hfds PROCESSOR_REF,importDir

CLSS public abstract org.netbeans.modules.profiler.actions.ProfilingAwareAction
cons protected init()
meth protected abstract int[] enabledStates()
meth protected boolean requiresInstrumentation()
meth protected boolean shouldBeEnabled(org.netbeans.lib.profiler.common.Profiler)
meth protected final boolean asynchronous()
meth protected void updateAction()
meth public final boolean isEnabled()
meth public final void setEnabled(boolean)
supr org.openide.util.actions.CallableSystemAction
hfds enabledSet

CLSS public final org.netbeans.modules.profiler.actions.ProfilingSupport
cons public init()
meth public static boolean checkProfilingInProgress()
supr java.lang.Object

CLSS public final org.netbeans.modules.profiler.actions.ResetResultsAction
innr public final static Listener
meth protected boolean asynchronous()
meth protected java.lang.String iconResource()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static org.netbeans.modules.profiler.actions.ResetResultsAction getInstance()
meth public void performAction()
supr org.openide.util.actions.CallableSystemAction
hfds resultListener
hcls Singleton

CLSS public final static org.netbeans.modules.profiler.actions.ResetResultsAction$Listener
 outer org.netbeans.modules.profiler.actions.ResetResultsAction
cons public init()
intf org.netbeans.modules.profiler.ResultsListener
meth public void resultsAvailable()
meth public void resultsReset()
supr org.netbeans.modules.profiler.utilities.Delegate<org.netbeans.modules.profiler.actions.ResetResultsAction>

CLSS public final org.netbeans.modules.profiler.actions.RunCalibrationAction
cons public init()
meth public void actionPerformed(java.awt.event.ActionEvent)
supr javax.swing.AbstractAction
hfds HELP_CTX
hcls CalibrationDateCellRenderer

CLSS public final org.netbeans.modules.profiler.actions.RunGCAction
cons protected init()
meth protected int[] enabledStates()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static org.netbeans.modules.profiler.actions.RunGCAction getInstance()
meth public void performAction()
supr org.netbeans.modules.profiler.actions.ProfilingAwareAction
hfds ENABLED_STATES
hcls Singleton

CLSS public final org.netbeans.modules.profiler.actions.SnapshotsWindowAction
cons public init()
meth public void actionPerformed(java.awt.event.ActionEvent)
supr javax.swing.AbstractAction

CLSS public final org.netbeans.modules.profiler.actions.StopAction
meth protected boolean shouldBeEnabled(org.netbeans.lib.profiler.common.Profiler)
meth protected int[] enabledStates()
meth protected void updateAction()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static org.netbeans.modules.profiler.actions.StopAction getInstance()
meth public void performAction()
supr org.netbeans.modules.profiler.actions.ProfilingAwareAction
hfds enabledStates,mode,taskPosted
hcls Singleton

CLSS public final org.netbeans.modules.profiler.actions.TakeSnapshotAction
cons public init()
innr public final static Listener
meth protected boolean requiresInstrumentation()
meth protected boolean shouldBeEnabled(org.netbeans.lib.profiler.common.Profiler)
meth protected int[] enabledStates()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static org.netbeans.modules.profiler.actions.TakeSnapshotAction getInstance()
meth public void performAction()
supr org.netbeans.modules.profiler.actions.ProfilingAwareAction
hfds ENABLED_STATES,listener
hcls Singleton

CLSS public final static org.netbeans.modules.profiler.actions.TakeSnapshotAction$Listener
 outer org.netbeans.modules.profiler.actions.TakeSnapshotAction
cons public init()
intf org.netbeans.modules.profiler.ResultsListener
meth public void resultsAvailable()
meth public void resultsReset()
supr org.netbeans.modules.profiler.utilities.Delegate<org.netbeans.modules.profiler.actions.TakeSnapshotAction>

CLSS public org.netbeans.modules.profiler.actions.TakeThreadDumpAction
cons public init()
meth protected int[] enabledStates()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static org.netbeans.modules.profiler.actions.TakeThreadDumpAction getInstance()
meth public void performAction()
supr org.netbeans.modules.profiler.actions.ProfilingAwareAction
hfds ENABLED_STATES
hcls Singleton

CLSS public abstract interface org.netbeans.modules.profiler.api.ProgressDisplayer
fld public final static org.netbeans.modules.profiler.api.ProgressDisplayer DEFAULT
innr public abstract interface static ProgressController
meth public abstract org.netbeans.modules.profiler.api.ProgressDisplayer showProgress(java.lang.String)
meth public abstract org.netbeans.modules.profiler.api.ProgressDisplayer showProgress(java.lang.String,java.lang.String,org.netbeans.modules.profiler.api.ProgressDisplayer$ProgressController)
meth public abstract org.netbeans.modules.profiler.api.ProgressDisplayer showProgress(java.lang.String,org.netbeans.modules.profiler.api.ProgressDisplayer$ProgressController)
meth public abstract void close()

CLSS public abstract interface org.netbeans.modules.profiler.spi.SessionListener
innr public abstract static Adapter
meth public abstract void onShutdown()
meth public abstract void onStartup(org.netbeans.lib.profiler.common.ProfilingSettings,org.openide.util.Lookup$Provider)

CLSS public abstract static org.netbeans.modules.profiler.spi.SessionListener$Adapter
 outer org.netbeans.modules.profiler.spi.SessionListener
cons public init()
intf org.netbeans.modules.profiler.spi.SessionListener
meth public void onShutdown()
meth public void onStartup(org.netbeans.lib.profiler.common.ProfilingSettings,org.openide.util.Lookup$Provider)
supr java.lang.Object

CLSS public org.netbeans.modules.profiler.ui.HprofDataObject
cons public init(org.openide.filesystems.FileObject,org.openide.loaders.MultiFileLoader) throws java.io.IOException
intf org.openide.cookies.OpenCookie
meth protected org.openide.nodes.Node createNodeDelegate()
meth public org.openide.util.Lookup getLookup()
meth public void open()
supr org.openide.loaders.MultiDataObject

CLSS public final org.netbeans.modules.profiler.ui.NBHTMLLabel
cons public init(java.lang.String)
meth protected void showURL(java.net.URL)
supr org.netbeans.lib.profiler.ui.components.HTMLLabel

CLSS public abstract org.netbeans.modules.profiler.ui.NBSwingWorker
cons public init()
cons public init(boolean)
meth protected void postRunnable(java.lang.Runnable)
supr org.netbeans.lib.profiler.ui.SwingWorker
hfds rp

CLSS public org.netbeans.modules.profiler.ui.NpsDataObject
cons public init(org.openide.filesystems.FileObject,org.openide.loaders.MultiFileLoader) throws java.io.IOException
intf org.openide.cookies.OpenCookie
meth protected org.openide.nodes.Node createNodeDelegate()
meth protected void handleDelete() throws java.io.IOException
meth public org.openide.util.Lookup getLookup()
meth public void open()
supr org.openide.loaders.MultiDataObject

CLSS public org.netbeans.modules.profiler.ui.ProfilerProgressDisplayer
fld protected final static java.lang.String CANCEL_BUTTON_TEXT
fld protected final static java.lang.String PROGRESS_STRING
intf org.netbeans.modules.profiler.api.ProgressDisplayer
meth public org.netbeans.modules.profiler.api.ProgressDisplayer showProgress(java.lang.String)
meth public org.netbeans.modules.profiler.api.ProgressDisplayer showProgress(java.lang.String,java.lang.String,org.netbeans.modules.profiler.api.ProgressDisplayer$ProgressController)
meth public org.netbeans.modules.profiler.api.ProgressDisplayer showProgress(java.lang.String,org.netbeans.modules.profiler.api.ProgressDisplayer$ProgressController)
meth public static org.netbeans.modules.profiler.ui.ProfilerProgressDisplayer getDefault()
meth public void close()
supr javax.swing.JPanel
hfds cancelButton,controller,defaultInstance,owner,progressBar,progressLabel

CLSS public abstract org.netbeans.modules.profiler.utilities.Delegate<%0 extends java.lang.Object>
cons public init()
meth protected {org.netbeans.modules.profiler.utilities.Delegate%0} getDelegate()
meth public void setDelegate({org.netbeans.modules.profiler.utilities.Delegate%0})
supr java.lang.Object
hfds delegate

CLSS public final org.netbeans.modules.profiler.utils.IDEUtils
cons public init()
meth public static java.lang.String getAntProfilerStartArgument110Beyond(int,int)
meth public static java.lang.String getAntProfilerStartArgument15(int,int)
meth public static java.lang.String getAntProfilerStartArgument16(int,int)
meth public static java.lang.String getAntProfilerStartArgument17(int,int)
meth public static java.lang.String getAntProfilerStartArgument18(int,int)
meth public static java.lang.String getAntProfilerStartArgument19(int,int)
supr java.lang.Object
hfds HELP_CTX

CLSS public org.netbeans.modules.profiler.utils.MainClassChooser
cons public init(org.openide.util.Lookup$Provider)
cons public init(org.openide.util.Lookup$Provider,java.lang.String)
fld public static java.lang.Boolean unitTestingSupport_hasMainMethodResult
meth public java.lang.String getSelectedMainClass()
meth public static boolean hasMainMethod(org.openide.filesystems.FileObject)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr javax.swing.JPanel
hfds changeListener,dialogSubtitle,jLabel1,jMainClassList,jScrollPane1,possibleMainClasses

CLSS public org.netbeans.modules.profiler.utils.MainClassWarning
cons public init(java.lang.String,org.openide.util.Lookup$Provider)
meth public java.lang.String getSelectedMainClass()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr javax.swing.JPanel
hfds jLabel1,jPanel1,jScrollPane1,message,project

CLSS public abstract org.netbeans.modules.profiler.v2.ProfilerFeature
cons public init()
innr public abstract static Basic
innr public abstract static Provider
innr public final static Registry
meth protected void activatedInSession()
meth protected void deactivatedInSession()
meth public abstract boolean currentSettingsValid()
meth public abstract boolean supportsConfiguration(org.openide.util.Lookup)
meth public abstract boolean supportsSettings(org.netbeans.lib.profiler.common.ProfilingSettings)
meth public abstract int getPosition()
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getName()
meth public abstract javax.swing.Icon getIcon()
meth public abstract javax.swing.JPanel getResultsUI()
meth public abstract javax.swing.JPanel getSettingsUI()
meth public abstract org.netbeans.lib.profiler.ui.components.ProfilerToolbar getToolbar()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void configure(org.openide.util.Lookup)
meth public abstract void configureSettings(org.netbeans.lib.profiler.common.ProfilingSettings)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object

CLSS public abstract static org.netbeans.modules.profiler.v2.ProfilerFeature$Basic
 outer org.netbeans.modules.profiler.v2.ProfilerFeature
cons public init(javax.swing.Icon,java.lang.String,java.lang.String,int,org.netbeans.modules.profiler.v2.ProfilerSession)
meth protected final boolean isActivated()
meth protected final int getSessionState()
meth protected final java.lang.String readFlag(java.lang.String,java.lang.String)
meth protected final org.netbeans.modules.profiler.v2.ProfilerSession getSession()
meth protected final void activatedInSession()
meth protected final void deactivatedInSession()
meth protected final void fireChange()
meth protected final void storeFlag(java.lang.String,java.lang.String)
meth protected void instrumentationChanged(int,int)
meth protected void lockContentionMonitoringChanged()
meth protected void notifyActivated()
meth protected void notifyDeactivated()
meth protected void profilingStateChanged(int,int)
meth protected void serverStateChanged(int,int)
meth protected void threadsMonitoringChanged()
meth public boolean currentSettingsValid()
meth public boolean supportsConfiguration(org.openide.util.Lookup)
meth public boolean supportsSettings(org.netbeans.lib.profiler.common.ProfilingSettings)
meth public final int getPosition()
meth public final java.lang.String getDescription()
meth public final java.lang.String getName()
meth public final javax.swing.Icon getIcon()
meth public final void addChangeListener(javax.swing.event.ChangeListener)
meth public final void removeChangeListener(javax.swing.event.ChangeListener)
meth public javax.swing.JPanel getSettingsUI()
meth public org.netbeans.lib.profiler.ui.components.ProfilerToolbar getToolbar()
meth public void configure(org.openide.util.Lookup)
supr org.netbeans.modules.profiler.v2.ProfilerFeature
hfds description,icon,isActive,listener,listeners,name,position,session

CLSS public abstract static org.netbeans.modules.profiler.v2.ProfilerFeature$Provider
 outer org.netbeans.modules.profiler.v2.ProfilerFeature
cons public init()
meth public abstract org.netbeans.modules.profiler.v2.ProfilerFeature getFeature(org.netbeans.modules.profiler.v2.ProfilerSession)
supr java.lang.Object

CLSS public final static org.netbeans.modules.profiler.v2.ProfilerFeature$Registry
 outer org.netbeans.modules.profiler.v2.ProfilerFeature
meth public static boolean hasProviders()
supr java.lang.Object
hfds HAS_PROVIDERS

CLSS public abstract org.netbeans.modules.profiler.v2.ProfilerPlugin
cons public init(java.lang.String)
innr public abstract static Provider
meth protected void sessionStarted()
meth protected void sessionStarting()
meth protected void sessionStopped()
meth protected void sessionStopping()
meth public abstract void createMenu(javax.swing.JMenu)
meth public final java.lang.String getName()
supr java.lang.Object
hfds name

CLSS public abstract static org.netbeans.modules.profiler.v2.ProfilerPlugin$Provider
 outer org.netbeans.modules.profiler.v2.ProfilerPlugin
cons public init()
meth public abstract org.netbeans.modules.profiler.v2.ProfilerPlugin createPlugin(org.openide.util.Lookup$Provider,org.netbeans.modules.profiler.v2.SessionStorage)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.profiler.v2.ProfilerSession
cons protected init(org.netbeans.lib.profiler.common.Profiler,org.openide.util.Lookup)
innr public abstract static Provider
meth protected abstract boolean isCompatibleContext(org.openide.util.Lookup)
meth protected abstract boolean modify()
meth protected abstract boolean start()
meth protected abstract boolean stop()
meth protected final org.openide.util.Lookup getContext()
meth public abstract org.openide.filesystems.FileObject getFile()
meth public abstract org.openide.util.Lookup$Provider getProject()
meth public final boolean inProgress()
meth public final boolean isAttach()
meth public final int getState()
meth public final org.netbeans.lib.profiler.common.AttachSettings getAttachSettings()
meth public final org.netbeans.lib.profiler.common.Profiler getProfiler()
meth public final org.netbeans.lib.profiler.common.ProfilingSettings getProfilingSettings()
meth public final org.netbeans.modules.profiler.v2.SessionStorage getStorage()
meth public final void addListener(org.netbeans.lib.profiler.common.event.ProfilingStateListener)
meth public final void open()
meth public final void removeListener(org.netbeans.lib.profiler.common.event.ProfilingStateListener)
meth public final void setAttach(boolean)
meth public static org.netbeans.modules.profiler.v2.ProfilerSession currentSession()
meth public static org.netbeans.modules.profiler.v2.ProfilerSession forContext(org.openide.util.Lookup)
meth public static void findAndConfigure(org.openide.util.Lookup,org.openide.util.Lookup$Provider,java.lang.String)
supr java.lang.Object
hfds CURRENT_SESSION,CURRENT_SESSION_LOCK,attachSettings,context,features,isAttach,plugins,profiler,profilingSettings,profilingStateListeners,storage,window

CLSS public abstract static org.netbeans.modules.profiler.v2.ProfilerSession$Provider
 outer org.netbeans.modules.profiler.v2.ProfilerSession
cons public init()
meth public abstract org.netbeans.modules.profiler.v2.ProfilerSession createSession(org.openide.util.Lookup)
supr java.lang.Object

CLSS public final org.netbeans.modules.profiler.v2.SessionStorage
meth public java.lang.String readFlag(java.lang.String,java.lang.String)
meth public void storeFlag(java.lang.String,java.lang.String)
supr java.lang.Object
hfds PROCESSOR,SETTINGS_FILENAME,dirty,project,properties

CLSS public final org.netbeans.modules.profiler.v2.SnapshotsWindow
meth public static org.netbeans.modules.profiler.v2.SnapshotsWindow instance()
meth public void refreshFolder(org.openide.filesystems.FileObject,boolean)
meth public void sessionActivated(org.netbeans.modules.profiler.v2.ProfilerSession)
meth public void sessionClosed(org.netbeans.modules.profiler.v2.ProfilerSession)
meth public void sessionDeactivated(org.netbeans.modules.profiler.v2.ProfilerSession)
meth public void sessionOpened(org.netbeans.modules.profiler.v2.ProfilerSession)
meth public void showStandalone()
meth public void snapshotSaved(org.netbeans.modules.profiler.LoadedSnapshot)
supr java.lang.Object
hfds INSTANCE,snapshotsListener,ui

CLSS public abstract interface org.openide.cookies.OpenCookie
intf org.netbeans.api.actions.Openable
intf org.openide.nodes.Node$Cookie

CLSS public abstract org.openide.loaders.DataObject
cons public init(org.openide.filesystems.FileObject,org.openide.loaders.DataLoader) throws org.openide.loaders.DataObjectExistsException
fld public final static java.lang.String PROP_COOKIE = "cookie"
fld public final static java.lang.String PROP_FILES = "files"
fld public final static java.lang.String PROP_HELP = "helpCtx"
fld public final static java.lang.String PROP_MODIFIED = "modified"
fld public final static java.lang.String PROP_NAME = "name"
fld public final static java.lang.String PROP_PRIMARY_FILE = "primaryFile"
fld public final static java.lang.String PROP_TEMPLATE = "template"
fld public final static java.lang.String PROP_VALID = "valid"
innr public abstract interface static !annotation Registration
innr public abstract interface static !annotation Registrations
innr public abstract interface static Container
innr public abstract interface static Factory
innr public final static Registry
intf java.io.Serializable
intf org.openide.nodes.Node$Cookie
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.Lookup$Provider
meth protected <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(org.openide.loaders.DataShadow,java.lang.Class<{%%0}>)
meth protected abstract org.openide.filesystems.FileObject handleMove(org.openide.loaders.DataFolder) throws java.io.IOException
meth protected abstract org.openide.filesystems.FileObject handleRename(java.lang.String) throws java.io.IOException
meth protected abstract org.openide.loaders.DataObject handleCopy(org.openide.loaders.DataFolder) throws java.io.IOException
meth protected abstract org.openide.loaders.DataObject handleCreateFromTemplate(org.openide.loaders.DataFolder,java.lang.String) throws java.io.IOException
meth protected abstract void handleDelete() throws java.io.IOException
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected final void fireVetoableChange(java.lang.String,java.lang.Object,java.lang.Object) throws java.beans.PropertyVetoException
meth protected final void markFiles() throws java.io.IOException
meth protected org.openide.filesystems.FileLock takePrimaryFileLock() throws java.io.IOException
meth protected org.openide.loaders.DataObject handleCopyRename(org.openide.loaders.DataFolder,java.lang.String,java.lang.String) throws java.io.IOException
meth protected org.openide.loaders.DataShadow handleCreateShadow(org.openide.loaders.DataFolder) throws java.io.IOException
meth protected org.openide.nodes.Node createNodeDelegate()
meth protected void dispose()
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public abstract boolean isCopyAllowed()
meth public abstract boolean isDeleteAllowed()
meth public abstract boolean isMoveAllowed()
meth public abstract boolean isRenameAllowed()
meth public abstract org.openide.util.HelpCtx getHelpCtx()
meth public boolean isModified()
meth public boolean isShadowAllowed()
meth public final boolean isTemplate()
meth public final boolean isValid()
meth public final org.openide.filesystems.FileObject getPrimaryFile()
meth public final org.openide.loaders.DataFolder getFolder()
meth public final org.openide.loaders.DataLoader getLoader()
meth public final org.openide.loaders.DataObject copy(org.openide.loaders.DataFolder) throws java.io.IOException
meth public final org.openide.loaders.DataObject createFromTemplate(org.openide.loaders.DataFolder) throws java.io.IOException
meth public final org.openide.loaders.DataObject createFromTemplate(org.openide.loaders.DataFolder,java.lang.String) throws java.io.IOException
meth public final org.openide.loaders.DataObject createFromTemplate(org.openide.loaders.DataFolder,java.lang.String,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public final org.openide.loaders.DataShadow createShadow(org.openide.loaders.DataFolder) throws java.io.IOException
meth public final org.openide.nodes.Node getNodeDelegate()
meth public final void delete() throws java.io.IOException
meth public final void move(org.openide.loaders.DataFolder) throws java.io.IOException
meth public final void rename(java.lang.String) throws java.io.IOException
meth public final void setTemplate(boolean) throws java.io.IOException
meth public java.lang.Object writeReplace()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.util.Set<org.openide.filesystems.FileObject> files()
meth public org.openide.util.Lookup getLookup()
meth public static org.openide.loaders.DataObject find(org.openide.filesystems.FileObject) throws org.openide.loaders.DataObjectNotFoundException
meth public static org.openide.loaders.DataObject$Registry getRegistry()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addVetoableChangeListener(java.beans.VetoableChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removeVetoableChangeListener(java.beans.VetoableChangeListener)
meth public void setModified(boolean)
meth public void setValid(boolean) throws java.beans.PropertyVetoException
supr java.lang.Object
hfds BEING_CREATED,EA_ASSIGNED_LOADER,EA_ASSIGNED_LOADER_MODULE,LOCK,LOG,OBJ_LOG,PROGRESS_INFO_TL,REGISTRY_INSTANCE,changeSupport,changeSupportUpdater,item,loader,modif,modified,nodeDelegate,serialVersionUID,syncModified,synchObject,vetoableChangeSupport,warnedClasses
hcls CreateAction,DOSavable,ModifiedRegistry,ProgressInfo,Replace

CLSS public org.openide.loaders.MultiDataObject
cons public init(org.openide.filesystems.FileObject,org.openide.loaders.MultiFileLoader) throws org.openide.loaders.DataObjectExistsException
innr public abstract Entry
meth protected final org.openide.loaders.MultiDataObject$Entry registerEntry(org.openide.filesystems.FileObject)
meth protected final org.openide.nodes.CookieSet getCookieSet()
meth protected final void addSecondaryEntry(org.openide.loaders.MultiDataObject$Entry)
meth protected final void registerEditor(java.lang.String,boolean)
meth protected final void removeSecondaryEntry(org.openide.loaders.MultiDataObject$Entry)
meth protected final void setCookieSet(org.openide.nodes.CookieSet)
 anno 0 java.lang.Deprecated()
meth protected int associateLookup()
meth protected org.openide.filesystems.FileLock takePrimaryFileLock() throws java.io.IOException
meth protected org.openide.filesystems.FileObject handleMove(org.openide.loaders.DataFolder) throws java.io.IOException
meth protected org.openide.filesystems.FileObject handleRename(java.lang.String) throws java.io.IOException
meth protected org.openide.loaders.DataObject handleCopy(org.openide.loaders.DataFolder) throws java.io.IOException
meth protected org.openide.loaders.DataObject handleCopyRename(org.openide.loaders.DataFolder,java.lang.String,java.lang.String) throws java.io.IOException
meth protected org.openide.loaders.DataObject handleCreateFromTemplate(org.openide.loaders.DataFolder,java.lang.String) throws java.io.IOException
meth protected org.openide.nodes.Node createNodeDelegate()
meth protected void handleDelete() throws java.io.IOException
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public boolean isCopyAllowed()
meth public boolean isDeleteAllowed()
meth public boolean isMoveAllowed()
meth public boolean isRenameAllowed()
meth public final java.util.Set<org.openide.loaders.MultiDataObject$Entry> secondaryEntries()
meth public final org.openide.loaders.MultiDataObject$Entry findSecondaryEntry(org.openide.filesystems.FileObject)
meth public final org.openide.loaders.MultiDataObject$Entry getPrimaryEntry()
meth public final org.openide.loaders.MultiFileLoader getMultiFileLoader()
meth public java.util.Set<org.openide.filesystems.FileObject> files()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public org.openide.util.Lookup getLookup()
supr org.openide.loaders.DataObject
hfds ERR,RECOGNIZER,TEMPLATE_ATTRIBUTES,chLis,checked,cookieSet,cookieSetLock,delayProcessor,delayedPropFilesLock,delayedPropFilesTask,firingProcessor,later,primary,secondary,secondaryCreationLock,serialVersionUID
hcls ChangeAndBefore,EmptyRecognizer,EntryReplace,Pair

CLSS public org.openide.modules.ModuleInstall
cons public init()
meth protected boolean clearSharedData()
meth public boolean closing()
meth public void close()
meth public void installed()
 anno 0 java.lang.Deprecated()
meth public void restored()
meth public void uninstalled()
meth public void updated(int,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void validate()
supr org.openide.util.SharedClassObject
hfds serialVersionUID

CLSS public abstract org.openide.nodes.Node
cons protected init(org.openide.nodes.Children)
cons protected init(org.openide.nodes.Children,org.openide.util.Lookup)
fld public final static java.lang.String PROP_COOKIE = "cookie"
fld public final static java.lang.String PROP_DISPLAY_NAME = "displayName"
fld public final static java.lang.String PROP_ICON = "icon"
fld public final static java.lang.String PROP_LEAF = "leaf"
fld public final static java.lang.String PROP_NAME = "name"
fld public final static java.lang.String PROP_OPENED_ICON = "openedIcon"
fld public final static java.lang.String PROP_PARENT_NODE = "parentNode"
fld public final static java.lang.String PROP_PROPERTY_SETS = "propertySets"
fld public final static java.lang.String PROP_SHORT_DESCRIPTION = "shortDescription"
fld public final static org.openide.nodes.Node EMPTY
innr public abstract interface static Cookie
innr public abstract interface static Handle
innr public abstract static IndexedProperty
innr public abstract static Property
innr public abstract static PropertySet
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.Lookup$Provider
meth protected final boolean hasPropertyChangeListener()
meth protected final void fireCookieChange()
meth protected final void fireDisplayNameChange(java.lang.String,java.lang.String)
meth protected final void fireIconChange()
meth protected final void fireNameChange(java.lang.String,java.lang.String)
meth protected final void fireNodeDestroyed()
meth protected final void fireOpenedIconChange()
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected final void firePropertySetsChange(org.openide.nodes.Node$PropertySet[],org.openide.nodes.Node$PropertySet[])
meth protected final void fireShortDescriptionChange(java.lang.String,java.lang.String)
meth protected final void setChildren(org.openide.nodes.Children)
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public abstract boolean canCopy()
meth public abstract boolean canCut()
meth public abstract boolean canDestroy()
meth public abstract boolean canRename()
meth public abstract boolean hasCustomizer()
meth public abstract java.awt.Component getCustomizer()
meth public abstract java.awt.Image getIcon(int)
meth public abstract java.awt.Image getOpenedIcon(int)
meth public abstract java.awt.datatransfer.Transferable clipboardCopy() throws java.io.IOException
meth public abstract java.awt.datatransfer.Transferable clipboardCut() throws java.io.IOException
meth public abstract java.awt.datatransfer.Transferable drag() throws java.io.IOException
meth public abstract org.openide.nodes.Node cloneNode()
meth public abstract org.openide.nodes.Node$Handle getHandle()
meth public abstract org.openide.nodes.Node$PropertySet[] getPropertySets()
meth public abstract org.openide.util.HelpCtx getHelpCtx()
meth public abstract org.openide.util.datatransfer.NewType[] getNewTypes()
meth public abstract org.openide.util.datatransfer.PasteType getDropType(java.awt.datatransfer.Transferable,int,int)
meth public abstract org.openide.util.datatransfer.PasteType[] getPasteTypes(java.awt.datatransfer.Transferable)
meth public boolean equals(java.lang.Object)
meth public final boolean isLeaf()
meth public final javax.swing.JPopupMenu getContextMenu()
meth public final org.openide.nodes.Children getChildren()
meth public final org.openide.nodes.Node getParentNode()
meth public final org.openide.util.Lookup getLookup()
meth public final void addNodeListener(org.openide.nodes.NodeListener)
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removeNodeListener(org.openide.nodes.NodeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public int hashCode()
meth public java.lang.String getHtmlDisplayName()
meth public java.lang.String toString()
meth public javax.swing.Action getPreferredAction()
meth public javax.swing.Action[] getActions(boolean)
meth public org.openide.util.actions.SystemAction getDefaultAction()
 anno 0 java.lang.Deprecated()
meth public org.openide.util.actions.SystemAction[] getActions()
 anno 0 java.lang.Deprecated()
meth public org.openide.util.actions.SystemAction[] getContextActions()
 anno 0 java.lang.Deprecated()
meth public void destroy() throws java.io.IOException
meth public void setDisplayName(java.lang.String)
meth public void setHidden(boolean)
 anno 0 java.lang.Deprecated()
meth public void setName(java.lang.String)
meth public void setShortDescription(java.lang.String)
supr java.beans.FeatureDescriptor
hfds BLOCK_EVENTS,INIT_LOCK,LOCK,TEMPL_COOKIE,err,hierarchy,listeners,lookups,parent,warnedBadProperties
hcls LookupEventList,PropertyEditorRef

CLSS public abstract interface static org.openide.nodes.Node$Cookie
 outer org.openide.nodes.Node

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

CLSS public abstract org.openide.util.Lookup
cons public init()
fld public final static org.openide.util.Lookup EMPTY
innr public abstract interface static Provider
innr public abstract static Item
innr public abstract static Result
innr public final static Template
meth public <%0 extends java.lang.Object> java.util.Collection<? extends {%%0}> lookupAll(java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> org.openide.util.Lookup$Item<{%%0}> lookupItem(org.openide.util.Lookup$Template<{%%0}>)
meth public <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookupResult(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookup(org.openide.util.Lookup$Template<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>)
meth public static org.openide.util.Lookup getDefault()
supr java.lang.Object
hfds LOG,defaultLookup,defaultLookupProvider
hcls DefLookup,Empty

CLSS public abstract interface static org.openide.util.Lookup$Provider
 outer org.openide.util.Lookup
meth public abstract org.openide.util.Lookup getLookup()

CLSS public abstract org.openide.util.SharedClassObject
cons protected init()
intf java.io.Externalizable
meth protected boolean clearSharedData()
meth protected final java.lang.Object getLock()
meth protected final java.lang.Object getProperty(java.lang.Object)
meth protected final java.lang.Object putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void finalize() throws java.lang.Throwable
meth protected java.lang.Object writeReplace()
meth protected void addNotify()
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void initialize()
meth protected void removeNotify()
meth protected void reset()
meth public final boolean equals(java.lang.Object)
meth public final int hashCode()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public static <%0 extends org.openide.util.SharedClassObject> {%%0} findObject(java.lang.Class<{%%0}>)
meth public static <%0 extends org.openide.util.SharedClassObject> {%%0} findObject(java.lang.Class<{%%0}>,boolean)
meth public void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr java.lang.Object
hfds PROP_SUPPORT,addNotifySuper,alreadyWarnedAboutDupes,dataEntry,err,first,firstTrace,inReadExternal,initializeSuper,instancesBeingCreated,lock,prematureSystemOptionMutation,removeNotifySuper,serialVersionUID,systemOption,values,waitingOnSystemOption
hcls DataEntry,SetAccessibleAction,WriteReplace

CLSS public abstract org.openide.util.actions.CallableSystemAction
cons public init()
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected boolean asynchronous()
meth public abstract void performAction()
meth public java.awt.Component getToolbarPresenter()
meth public javax.swing.JMenuItem getMenuPresenter()
meth public javax.swing.JMenuItem getPopupPresenter()
meth public void actionPerformed(java.awt.event.ActionEvent)
supr org.openide.util.actions.SystemAction
hfds DEFAULT_ASYNCH,serialVersionUID,warnedAsynchronousActions

CLSS public abstract interface org.openide.util.actions.Presenter
innr public abstract interface static Menu
innr public abstract interface static Popup
innr public abstract interface static Toolbar

CLSS public abstract interface static org.openide.util.actions.Presenter$Menu
 outer org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter
meth public abstract javax.swing.JMenuItem getMenuPresenter()

CLSS public abstract interface static org.openide.util.actions.Presenter$Popup
 outer org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter
meth public abstract javax.swing.JMenuItem getPopupPresenter()

CLSS public abstract interface static org.openide.util.actions.Presenter$Toolbar
 outer org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter
meth public abstract java.awt.Component getToolbarPresenter()

CLSS public abstract org.openide.util.actions.SystemAction
cons public init()
fld public final static java.lang.String PROP_ENABLED = "enabled"
fld public final static java.lang.String PROP_ICON = "icon"
intf javax.swing.Action
intf org.openide.util.HelpCtx$Provider
meth protected boolean clearSharedData()
meth protected java.lang.String iconResource()
meth protected void initialize()
meth public abstract java.lang.String getName()
meth public abstract org.openide.util.HelpCtx getHelpCtx()
meth public abstract void actionPerformed(java.awt.event.ActionEvent)
meth public boolean isEnabled()
meth public final java.lang.Object getValue(java.lang.String)
meth public final javax.swing.Icon getIcon()
meth public final javax.swing.Icon getIcon(boolean)
meth public final void putValue(java.lang.String,java.lang.Object)
meth public final void setIcon(javax.swing.Icon)
meth public static <%0 extends org.openide.util.actions.SystemAction> {%%0} get(java.lang.Class<{%%0}>)
meth public static javax.swing.JPopupMenu createPopupMenu(org.openide.util.actions.SystemAction[])
 anno 0 java.lang.Deprecated()
meth public static javax.swing.JToolBar createToolbarPresenter(org.openide.util.actions.SystemAction[])
meth public static org.openide.util.actions.SystemAction[] linkActions(org.openide.util.actions.SystemAction[],org.openide.util.actions.SystemAction[])
meth public void setEnabled(boolean)
supr org.openide.util.SharedClassObject
hfds LOG,PROP_ICON_TEXTUAL,relativeIconResourceClasses,serialVersionUID
hcls ComponentIcon

CLSS public org.openide.windows.TopComponent
cons public init()
cons public init(org.openide.util.Lookup)
fld public final static int CLOSE_EACH = 0
 anno 0 java.lang.Deprecated()
fld public final static int CLOSE_LAST = 1
 anno 0 java.lang.Deprecated()
fld public final static int PERSISTENCE_ALWAYS = 0
fld public final static int PERSISTENCE_NEVER = 2
fld public final static int PERSISTENCE_ONLY_OPENED = 1
fld public final static java.lang.String PROP_CLOSING_DISABLED = "netbeans.winsys.tc.closing_disabled"
fld public final static java.lang.String PROP_DND_COPY_DISABLED = "netbeans.winsys.tc.draganddrop_copy_disabled"
fld public final static java.lang.String PROP_DRAGGING_DISABLED = "netbeans.winsys.tc.dragging_disabled"
fld public final static java.lang.String PROP_KEEP_PREFERRED_SIZE_WHEN_SLIDED_IN = "netbeans.winsys.tc.keep_preferred_size_when_slided_in"
fld public final static java.lang.String PROP_MAXIMIZATION_DISABLED = "netbeans.winsys.tc.maximization_disabled"
fld public final static java.lang.String PROP_SLIDING_DISABLED = "netbeans.winsys.tc.sliding_disabled"
fld public final static java.lang.String PROP_UNDOCKING_DISABLED = "netbeans.winsys.tc.undocking_disabled"
innr public abstract interface static !annotation Description
innr public abstract interface static !annotation OpenActionRegistration
innr public abstract interface static !annotation Registration
innr public abstract interface static Cloneable
innr public abstract interface static Registry
innr public final static SubComponent
innr public static NodeName
intf java.io.Externalizable
intf javax.accessibility.Accessible
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.Lookup$Provider
meth protected boolean processKeyBinding(javax.swing.KeyStroke,java.awt.event.KeyEvent,int,boolean)
meth protected final void associateLookup(org.openide.util.Lookup)
meth protected java.lang.Object writeReplace() throws java.io.ObjectStreamException
meth protected java.lang.String preferredID()
meth protected void closeNotify()
 anno 0 java.lang.Deprecated()
meth protected void componentActivated()
meth protected void componentClosed()
meth protected void componentDeactivated()
meth protected void componentHidden()
meth protected void componentOpened()
meth protected void componentShowing()
meth protected void openNotify()
 anno 0 java.lang.Deprecated()
meth public boolean canClose()
meth public boolean canClose(org.openide.windows.Workspace,boolean)
 anno 0 java.lang.Deprecated()
meth public boolean requestFocusInWindow()
meth public final boolean close()
meth public final boolean close(org.openide.windows.Workspace)
 anno 0 java.lang.Deprecated()
meth public final boolean isOpened()
meth public final boolean isOpened(org.openide.windows.Workspace)
 anno 0 java.lang.Deprecated()
meth public final int getCloseOperation()
 anno 0 java.lang.Deprecated()
meth public final int getTabPosition()
meth public final org.openide.nodes.Node[] getActivatedNodes()
meth public final static org.openide.windows.TopComponent$Registry getRegistry()
meth public final void cancelRequestAttention()
meth public final void makeBusy(boolean)
meth public final void openAtTabPosition(int)
meth public final void requestAttention(boolean)
meth public final void setActivatedNodes(org.openide.nodes.Node[])
meth public final void setAttentionHighlight(boolean)
meth public final void setCloseOperation(int)
 anno 0 java.lang.Deprecated()
meth public int getPersistenceType()
meth public java.awt.Image getIcon()
meth public java.lang.String getDisplayName()
meth public java.lang.String getHtmlDisplayName()
meth public java.lang.String getShortName()
meth public java.util.List<org.openide.windows.Mode> availableModes(java.util.List<org.openide.windows.Mode>)
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.Action[] getActions()
meth public org.openide.awt.UndoRedo getUndoRedo()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public org.openide.util.Lookup getLookup()
meth public org.openide.util.actions.SystemAction[] getSystemActions()
 anno 0 java.lang.Deprecated()
meth public org.openide.windows.TopComponent$SubComponent[] getSubComponents()
meth public static javax.swing.Action openAction(org.openide.windows.TopComponent,java.lang.String,java.lang.String,boolean)
meth public void addNotify()
meth public void open()
meth public void open(org.openide.windows.Workspace)
 anno 0 java.lang.Deprecated()
meth public void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void requestActive()
meth public void requestFocus()
meth public void requestVisible()
meth public void setDisplayName(java.lang.String)
meth public void setHtmlDisplayName(java.lang.String)
meth public void setIcon(java.awt.Image)
meth public void setName(java.lang.String)
meth public void setToolTipText(java.lang.String)
meth public void toFront()
meth public void writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr javax.swing.JComponent
hfds LOG,MODE_ID_PREFERENCES_KEY_INFIX,UILOG,activatedNodes,attentionGetter,closeOperation,defaultLookupLock,defaultLookupRef,displayName,htmlDisplayName,icon,modeName,nodeName,serialVersion,serialVersionUID,warnedClasses,warnedTCPIClasses
hcls AttentionGetter,CloneWindowAction,CloseWindowAction,Replacer,SynchronizeNodes

