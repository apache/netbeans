#Signature file v4.1
#Version 1.76

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

CLSS public abstract interface java.beans.PropertyChangeListener
intf java.util.EventListener
meth public abstract void propertyChange(java.beans.PropertyChangeEvent)

CLSS public abstract interface java.beans.PropertyEditor
meth public abstract boolean isPaintable()
meth public abstract boolean supportsCustomEditor()
meth public abstract java.awt.Component getCustomEditor()
meth public abstract java.lang.Object getValue()
meth public abstract java.lang.String getAsText()
meth public abstract java.lang.String getJavaInitializationString()
meth public abstract java.lang.String[] getTags()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void paintValue(java.awt.Graphics,java.awt.Rectangle)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setAsText(java.lang.String)
meth public abstract void setValue(java.lang.Object)

CLSS public java.beans.PropertyEditorSupport
cons public init()
cons public init(java.lang.Object)
intf java.beans.PropertyEditor
meth public boolean isPaintable()
meth public boolean supportsCustomEditor()
meth public java.awt.Component getCustomEditor()
meth public java.lang.Object getSource()
meth public java.lang.Object getValue()
meth public java.lang.String getAsText()
meth public java.lang.String getJavaInitializationString()
meth public java.lang.String[] getTags()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void firePropertyChange()
meth public void paintValue(java.awt.Graphics,java.awt.Rectangle)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setAsText(java.lang.String)
meth public void setSource(java.lang.Object)
meth public void setValue(java.lang.Object)
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

CLSS public org.netbeans.modules.i18n.EmptyPropertyPanel
cons public init()
meth public java.lang.String getText()
meth public void setBundleText(java.lang.String)
supr javax.swing.JPanel
hfds theLabel

CLSS public final org.netbeans.modules.i18n.FactoryRegistry
meth public static boolean hasFactory(java.lang.Class)
meth public static org.netbeans.modules.i18n.I18nSupport$Factory getFactory(java.lang.Class)
supr java.lang.Object
hfds cache,ncache,result

CLSS public org.netbeans.modules.i18n.FileSelector
cons public init(org.openide.filesystems.FileObject,org.openide.loaders.DataObject)
cons public init(org.openide.filesystems.FileObject,org.openide.loaders.DataObject,org.openide.filesystems.FileObject)
intf java.beans.PropertyChangeListener
intf org.openide.explorer.ExplorerManager$Provider
meth public java.awt.Dialog getDialog(java.lang.String,java.awt.event.ActionListener)
meth public org.openide.explorer.ExplorerManager getExplorerManager()
meth public org.openide.loaders.DataObject getSelectedDataObject()
meth public void addNotify()
meth public void preselectDefaultBundle(org.openide.filesystems.FileObject)
meth public void preselectFile(org.openide.filesystems.FileObject)
meth public void propertyChange(java.beans.PropertyChangeEvent)
supr javax.swing.JPanel
hfds DEFAULT_BUNDLE_NAME,PROPERTIES_EXT,cancelButton,confirmed,fileNameTextField,manager,newButton,okButton,selectedDataObject,selectedFolder,template

CLSS public org.netbeans.modules.i18n.FilteredNode
cons public init(org.openide.nodes.Node,org.netbeans.modules.i18n.FilteredNode$NodeFilter)
cons public init(org.openide.nodes.Node,org.netbeans.modules.i18n.FilteredNode$NodeFilter,java.lang.String)
innr public abstract interface static NodeFilter
innr public static FilteredChildren
meth public boolean canRename()
meth public java.lang.String getDisplayName()
meth public org.openide.nodes.Node cloneNode()
supr org.openide.nodes.FilterNode
hfds filter,newName

CLSS public static org.netbeans.modules.i18n.FilteredNode$FilteredChildren
 outer org.netbeans.modules.i18n.FilteredNode
cons public init(org.openide.nodes.Node,org.netbeans.modules.i18n.FilteredNode$NodeFilter)
meth protected org.openide.nodes.Node copyNode(org.openide.nodes.Node)
meth protected org.openide.nodes.Node[] createNodes(org.openide.nodes.Node)
supr org.openide.nodes.FilterNode$Children
hfds filter

CLSS public abstract interface static org.netbeans.modules.i18n.FilteredNode$NodeFilter
 outer org.netbeans.modules.i18n.FilteredNode
meth public abstract boolean acceptNode(org.openide.nodes.Node)

CLSS public org.netbeans.modules.i18n.HardCodedString
cons public init(java.lang.String,javax.swing.text.Position,javax.swing.text.Position)
meth public int getLength()
meth public java.lang.String getText()
meth public javax.swing.text.Position getEndPosition()
meth public javax.swing.text.Position getStartPosition()
supr java.lang.Object
hfds endPosition,startPosition,text

CLSS public org.netbeans.modules.i18n.HelpStringCustomEditor
cons public init(java.lang.String,java.util.List,java.util.List,java.lang.String,java.lang.String)
innr public static InitCodeEditor
innr public static ReplaceCodeEditor
meth public java.lang.Object getPropertyValue()
supr javax.swing.JPanel
hfds combo,comboLabel,list,listLabel,scrollPane
hcls StringEditor

CLSS public static org.netbeans.modules.i18n.HelpStringCustomEditor$InitCodeEditor
 outer org.netbeans.modules.i18n.HelpStringCustomEditor
cons public init()
intf org.openide.explorer.propertysheet.ExPropertyEditor
meth public boolean isEditable()
meth public boolean supportsCustomEditor()
meth public java.awt.Component getCustomEditor()
meth public java.lang.String getJavaInitializationString()
meth public void attachEnv(org.openide.explorer.propertysheet.PropertyEnv)
meth public void setAsText(java.lang.String)
supr java.beans.PropertyEditorSupport

CLSS public static org.netbeans.modules.i18n.HelpStringCustomEditor$ReplaceCodeEditor
 outer org.netbeans.modules.i18n.HelpStringCustomEditor
cons public init()
intf org.openide.explorer.propertysheet.ExPropertyEditor
meth public boolean isEditable()
meth public boolean supportsCustomEditor()
meth public java.awt.Component getCustomEditor()
meth public java.lang.String getJavaInitializationString()
meth public void attachEnv(org.openide.explorer.propertysheet.PropertyEnv)
meth public void setAsText(java.lang.String)
supr java.beans.PropertyEditorSupport

CLSS public org.netbeans.modules.i18n.I18nAction
cons public init()
meth protected boolean asynchronous()
meth protected boolean enable(org.openide.nodes.Node[])
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.NodeAction
hfds serialVersionUID

CLSS public org.netbeans.modules.i18n.I18nManager
meth public static org.netbeans.modules.i18n.I18nManager getDefault()
meth public void cancel()
meth public void internationalize(org.openide.loaders.DataObject)
supr java.lang.Object
hfds LOG,caretWRef,dialogWRef,hcString,i18nPanelWRef,manager,replaceCount,support

CLSS public org.netbeans.modules.i18n.I18nOptions
fld public final static java.lang.String PROP_I18N_REGULAR_EXPRESSION = "i18nRegularExpression"
fld public final static java.lang.String PROP_INIT_JAVA_CODE = "initJavaCode"
fld public final static java.lang.String PROP_LAST_RESOURCE2 = "lastResource2"
fld public final static java.lang.String PROP_REGULAR_EXPRESSION = "regularExpression"
fld public final static java.lang.String PROP_REPLACE_JAVA_CODE = "replaceJavaCode"
fld public final static java.lang.String PROP_REPLACE_RESOURCE_VALUE = "replaceResourceValue"
meth public boolean isReplaceResourceValue()
meth public java.lang.String getI18nRegularExpression()
meth public java.lang.String getInitJavaCode()
meth public java.lang.String getRegularExpression()
meth public java.lang.String getReplaceJavaCode()
meth public org.openide.loaders.DataObject getLastResource2()
 anno 0 java.lang.Deprecated()
meth public org.openide.loaders.DataObject getLastResource2(org.openide.loaders.DataObject)
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static org.netbeans.modules.i18n.I18nOptions getDefault()
meth public void setI18nRegularExpression(java.lang.String)
meth public void setInitJavaCode(java.lang.String)
meth public void setLastResource2(org.openide.loaders.DataObject)
meth public void setRegularExpression(java.lang.String)
meth public void setReplaceJavaCode(java.lang.String)
meth public void setReplaceResourceValue(boolean)
supr java.lang.Object
hfds INSTANCE,LOG

CLSS public org.netbeans.modules.i18n.I18nPanel
cons public init(org.netbeans.modules.i18n.PropertyPanel,boolean,org.netbeans.api.project.Project,org.openide.filesystems.FileObject)
cons public init(org.netbeans.modules.i18n.PropertyPanel,org.netbeans.api.project.Project,org.openide.filesystems.FileObject)
meth public org.netbeans.modules.i18n.I18nString getI18nString()
meth public org.openide.filesystems.FileObject getFile()
meth public void addNotify()
meth public void setDefaultResource(org.openide.loaders.DataObject)
meth public void setFile(org.openide.filesystems.FileObject)
meth public void setI18nString(org.netbeans.modules.i18n.I18nString)
meth public void showBundleMessage(java.lang.String)
meth public void showPropertyPanel()
supr javax.swing.JPanel
hfds ALL_BUTTONS,CANCEL_BUTTON,CONTENT_FORM,CONTENT_MESG,HELP_BUTTON,IGNORE_BUTTON,INFO_BUTTON,NO_BUTTONS,REPLACE_BUTTON,SKIP_BUTTON,bundle,cancelButton,cardLayout,contentsPanelPlaceholder,contentsShown,emptyPanel,file,helpButton,i18nString,ignoreButton,infoButton,project,propListener,propertyPanel,replaceButton,serialVersionUID,skipButton,withButtons

CLSS public org.netbeans.modules.i18n.I18nString
cons protected init(org.netbeans.modules.i18n.I18nString)
cons protected init(org.netbeans.modules.i18n.I18nSupport)
fld protected java.lang.String comment
fld protected java.lang.String key
fld protected java.lang.String replaceFormat
fld protected java.lang.String value
fld protected org.netbeans.modules.i18n.I18nSupport support
meth protected void fillFormatMap(java.util.Map<java.lang.String,java.lang.String>)
meth public java.lang.Object clone()
meth public java.lang.String getComment()
meth public java.lang.String getKey()
meth public java.lang.String getReplaceFormat()
meth public java.lang.String getReplaceString()
meth public java.lang.String getValue()
meth public org.netbeans.modules.i18n.I18nSupport getSupport()
meth public void become(org.netbeans.modules.i18n.I18nString)
meth public void setComment(java.lang.String)
meth public void setKey(java.lang.String)
meth public void setReplaceFormat(java.lang.String)
meth public void setValue(java.lang.String)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.i18n.I18nSupport
cons public init(org.openide.loaders.DataObject)
fld protected final org.netbeans.modules.i18n.ResourceHolder resourceHolder
fld protected javax.swing.text.StyledDocument document
fld protected org.openide.loaders.DataObject sourceDataObject
innr public abstract interface static I18nFinder
innr public abstract interface static I18nReplacer
innr public abstract static Factory
meth protected abstract org.netbeans.modules.i18n.I18nSupport$I18nFinder createFinder()
meth protected abstract org.netbeans.modules.i18n.I18nSupport$I18nReplacer createReplacer()
meth protected abstract org.netbeans.modules.i18n.ResourceHolder createResourceHolder()
meth public abstract javax.swing.JPanel getInfo(org.netbeans.modules.i18n.HardCodedString)
meth public abstract org.netbeans.modules.i18n.I18nString getDefaultI18nString(org.netbeans.modules.i18n.HardCodedString)
meth public boolean hasAdditionalCustomizer()
meth public final javax.swing.text.StyledDocument getDocument()
meth public final org.netbeans.modules.i18n.I18nSupport$I18nFinder getFinder()
meth public final org.netbeans.modules.i18n.I18nSupport$I18nReplacer getReplacer()
meth public final org.openide.loaders.DataObject getSourceDataObject()
meth public javax.swing.JPanel getAdditionalCustomizer()
meth public org.netbeans.modules.i18n.I18nString getDefaultI18nString()
meth public org.netbeans.modules.i18n.PropertyPanel getPropertyPanel()
meth public org.netbeans.modules.i18n.ResourceHolder getResourceHolder()
meth public void performAdditionalChanges()
supr java.lang.Object
hfds finder,replacer

CLSS public abstract static org.netbeans.modules.i18n.I18nSupport$Factory
 outer org.netbeans.modules.i18n.I18nSupport
cons public init()
meth protected abstract org.netbeans.modules.i18n.I18nSupport createI18nSupport(org.openide.loaders.DataObject)
meth public abstract java.lang.Class getDataObjectClass()
meth public org.netbeans.modules.i18n.I18nSupport create(org.openide.loaders.DataObject) throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface static org.netbeans.modules.i18n.I18nSupport$I18nFinder
 outer org.netbeans.modules.i18n.I18nSupport
meth public abstract org.netbeans.modules.i18n.HardCodedString findNextHardCodedString()
meth public abstract org.netbeans.modules.i18n.HardCodedString findNextI18nString()
meth public abstract org.netbeans.modules.i18n.HardCodedString[] findAllHardCodedStrings()
meth public abstract org.netbeans.modules.i18n.HardCodedString[] findAllI18nStrings()

CLSS public abstract interface static org.netbeans.modules.i18n.I18nSupport$I18nReplacer
 outer org.netbeans.modules.i18n.I18nSupport
meth public abstract void replace(org.netbeans.modules.i18n.HardCodedString,org.netbeans.modules.i18n.I18nString)

CLSS public final org.netbeans.modules.i18n.I18nUtil
cons public init()
fld public final static java.lang.String HELP_ID_ADDPARAMS = "internation.addparams"
fld public final static java.lang.String HELP_ID_AUTOINSERT = "internation.autoinsert"
fld public final static java.lang.String HELP_ID_CUSTOM = "internation.custom"
fld public final static java.lang.String HELP_ID_FORMED = "internation.formed"
fld public final static java.lang.String HELP_ID_I18N = "internation.internation"
fld public final static java.lang.String HELP_ID_MANINSERT = "internation.maninsert"
fld public final static java.lang.String HELP_ID_REPLFORMAT = "internation.replformat"
fld public final static java.lang.String HELP_ID_RUNLOCALE = "internation.runlocale"
fld public final static java.lang.String HELP_ID_TESTING = "internation.testing"
fld public final static java.lang.String HELP_ID_WIZARD = "internation.wizard"
fld public final static java.lang.String PE_BUNDLE_CODE_HELP_ID = "i18n.pe.bundlestring"
fld public final static java.lang.String PE_I18N_REGEXP_HELP_ID = "i18n.pe.i18nregexp"
fld public final static java.lang.String PE_I18N_STRING_HELP_ID = "i18n.pe.i18nString"
fld public final static java.lang.String PE_REPLACE_CODE_HELP_ID = "i18n.pe.replacestring"
fld public final static java.lang.String PE_TEST_REGEXP_HELP_ID = "i18n.pe.testregexp"
meth public static boolean containsAcceptedDataObject(org.openide.loaders.DataFolder)
meth public static java.lang.String getDefaultReplaceFormat(boolean)
meth public static java.util.List<java.lang.String> getI18nRegExpItems()
meth public static java.util.List<java.lang.String> getInitFormatItems()
meth public static java.util.List<java.lang.String> getInitHelpItems()
meth public static java.util.List<java.lang.String> getRegExpItems()
meth public static java.util.List<java.lang.String> getReplaceFormatItems()
meth public static java.util.List<java.lang.String> getReplaceHelpItems()
meth public static java.util.List<org.openide.loaders.DataObject> getAcceptedDataObjects(org.openide.loaders.DataObject$Container)
meth public static java.util.ResourceBundle getBundle()
meth public static org.netbeans.modules.i18n.I18nOptions getOptions()
supr java.lang.Object
hfds DEFAULT_NETBEANS_REPLACE_FORMAT,DEFAULT_STANDARD_REPLACE_FORMAT,i18nRegExpItems,initFormatItems,initHelpItems,regExpHelpItems,regExpItems,replaceFormatItems,replaceHelpItems

CLSS public abstract org.netbeans.modules.i18n.InfoPanel
cons public init(org.netbeans.modules.i18n.HardCodedString,javax.swing.text.StyledDocument)
meth protected abstract void setHardCodedString(org.netbeans.modules.i18n.HardCodedString,javax.swing.text.StyledDocument)
meth protected javax.swing.JLabel getComponentLabel()
meth protected javax.swing.JLabel getPropertyLabel()
meth protected javax.swing.JTextField getComponentText()
meth protected javax.swing.JTextField getFoundInText()
meth protected javax.swing.JTextField getPropertyText()
meth protected javax.swing.JTextField getStringText()
supr javax.swing.JPanel
hfds bundle,componentLabel,componentText,foundInLabel,foundInText,propertyLabel,propertyText,stringLabel,stringText

CLSS public org.netbeans.modules.i18n.InsertI18nStringAction
cons public init()
meth protected boolean asynchronous()
meth protected boolean enable(org.openide.nodes.Node[])
meth protected int mode()
meth protected java.lang.Class[] cookieClasses()
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CookieAction
hfds dataObject,i18nPanel,position,serialVersionUID,support

CLSS public org.netbeans.modules.i18n.LocalePropertyEditor
cons public init()
meth public boolean supportsCustomEditor()
meth public java.awt.Component getCustomEditor()
meth public java.lang.Object getValue()
meth public java.lang.String getAsText()
meth public java.lang.String getJavaInitializationString()
meth public void setAsText(java.lang.String)
meth public void setValue(java.lang.Object)
supr java.beans.PropertyEditorSupport
hfds locale

CLSS public org.netbeans.modules.i18n.PropertyPanel
cons public init()
fld protected javax.swing.JButton argumentsButton
fld protected org.netbeans.modules.i18n.I18nString i18nString
fld public final static java.lang.String PROP_RESOURCE = "property_resource"
fld public final static java.lang.String PROP_STRING = "propString"
meth protected void updateReplaceText()
meth public org.openide.filesystems.FileObject getFile()
meth public void setEnabled(boolean)
meth public void setFile(org.openide.filesystems.FileObject)
meth public void setI18nString(org.netbeans.modules.i18n.I18nString)
meth public void setResource(org.openide.loaders.DataObject)
supr javax.swing.JPanel
hfds DUMMY_ACTION,browseButton,bundleNameLabel,commentLabel,commentScroll,commentText,file,innerResourceTextContent,internalTextChange,keyBundleCombo,keyLabel,replaceFormatButton,replaceFormatLabel,replaceFormatTextField,resourceText,valueLabel,valueScroll,valueText,warningLabel

CLSS public abstract org.netbeans.modules.i18n.ResourceHolder
cons public init(java.lang.Class[])
fld protected final java.lang.Class[] resourceClasses
fld protected org.openide.loaders.DataObject resource
meth protected abstract org.openide.loaders.DataObject createTemplate(java.lang.Class) throws java.io.IOException
meth public abstract java.lang.String getCommentForKey(java.lang.String)
meth public abstract java.lang.String getValueForKey(java.lang.String)
meth public abstract java.lang.String[] getAllKeys()
meth public abstract void addProperty(java.lang.Object,java.lang.Object,java.lang.String,boolean)
meth public final org.openide.loaders.DataObject getTemplate(java.lang.Class) throws java.io.IOException
meth public java.lang.Class[] getResourceClasses()
meth public java.lang.String toString()
meth public org.openide.loaders.DataObject getResource()
meth public void addProperty(java.lang.Object,java.lang.Object,java.lang.String)
meth public void setResource(org.openide.loaders.DataObject)
supr java.lang.Object

CLSS public org.netbeans.modules.i18n.SelectorUtils
cons public init()
fld public final static org.netbeans.modules.i18n.FilteredNode$NodeFilter ALL_FILTER
fld public final static org.netbeans.modules.i18n.FilteredNode$NodeFilter BUNDLES_FILTER
meth public static org.openide.loaders.DataObject instantiateTemplate(org.netbeans.api.project.Project,org.openide.filesystems.FileObject,org.openide.loaders.DataObject) throws java.io.IOException
meth public static org.openide.loaders.DataObject selectBundle(org.netbeans.api.project.Project,org.openide.filesystems.FileObject)
meth public static org.openide.loaders.DataObject selectOrCreateBundle(org.openide.filesystems.FileObject,org.openide.loaders.DataObject,org.openide.loaders.DataObject)
meth public static org.openide.nodes.Node bundlesNode(org.netbeans.api.project.Project,org.openide.filesystems.FileObject,boolean)
meth public static org.openide.nodes.Node sourcesNode(org.netbeans.api.project.Project,org.netbeans.modules.i18n.FilteredNode$NodeFilter)
supr java.lang.Object
hcls ObjectNameInputPanel

CLSS public org.netbeans.modules.i18n.Util
cons public init()
meth public static char getChar(java.lang.String)
meth public static java.lang.String getResourceName(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,char,boolean)
meth public static java.lang.String getString(java.lang.String)
meth public static org.netbeans.api.java.classpath.ClassPath getExecClassPath(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject)
meth public static org.netbeans.api.project.Project getProjectFor(org.openide.loaders.DataObject)
meth public static org.netbeans.api.project.Project getProjectFor(org.openide.nodes.Node[])
meth public static org.openide.filesystems.FileObject getResource(org.openide.filesystems.FileObject,java.lang.String)
meth public static void debug(java.lang.String,java.lang.Throwable)
meth public static void debug(java.lang.Throwable)
supr java.lang.Object

CLSS public org.netbeans.modules.i18n.java.JavaI18nFinder
cons public init(javax.swing.text.StyledDocument)
fld protected boolean i18nSearch
fld protected char[] buffer
fld protected final static int STATE_BLOCKCOMMENT = 3
fld protected final static int STATE_BLOCKCOMMENT_A_STAR = 4
fld protected final static int STATE_CHAR = 7
fld protected final static int STATE_JAVA = 0
fld protected final static int STATE_JAVA_A_SLASH = 1
fld protected final static int STATE_LINECOMMENT = 2
fld protected final static int STATE_STRING = 5
fld protected final static int STATE_STRING_A_BSLASH = 6
fld protected int currentStringEnd
fld protected int currentStringStart
fld protected int position
fld protected int state
fld protected java.lang.StringBuffer lastJavaString
fld protected javax.swing.text.Position lastPosition
fld protected javax.swing.text.StyledDocument document
fld public final java.lang.String strAndVarFound = "$strAndVarFound$"
intf org.netbeans.modules.i18n.I18nSupport$I18nFinder
meth protected boolean isSearchedString(java.lang.String,java.lang.String)
meth protected org.netbeans.modules.i18n.HardCodedString findNextString()
meth protected org.netbeans.modules.i18n.HardCodedString handleCharacter(char)
meth protected org.netbeans.modules.i18n.HardCodedString handleStateBlockComment(char)
meth protected org.netbeans.modules.i18n.HardCodedString handleStateBlockCommentAStar(char)
meth protected org.netbeans.modules.i18n.HardCodedString handleStateChar(char)
meth protected org.netbeans.modules.i18n.HardCodedString handleStateJava(char)
meth protected org.netbeans.modules.i18n.HardCodedString handleStateJavaASlash(char)
meth protected org.netbeans.modules.i18n.HardCodedString handleStateLineComment(char)
meth protected org.netbeans.modules.i18n.HardCodedString handleStateString(char)
meth protected org.netbeans.modules.i18n.HardCodedString handleStateStringABSlash(char)
meth protected org.netbeans.modules.i18n.HardCodedString handleStringWithVariable(java.lang.String,java.lang.String)
meth protected org.netbeans.modules.i18n.HardCodedString[] findAllStrings()
meth protected void handleNewLineCharacter()
meth protected void reset()
meth public org.netbeans.modules.i18n.HardCodedString findNextHardCodedString()
meth public org.netbeans.modules.i18n.HardCodedString findNextI18nString()
meth public org.netbeans.modules.i18n.HardCodedString modifyHCStringText(org.netbeans.modules.i18n.HardCodedString)
meth public org.netbeans.modules.i18n.HardCodedString[] findAllHardCodedStrings()
meth public org.netbeans.modules.i18n.HardCodedString[] findAllI18nStrings()
supr java.lang.Object
hfds concatenatedStringsFound
hcls AnnotationDetector

CLSS public org.netbeans.modules.i18n.java.JavaI18nString
cons protected init(org.netbeans.modules.i18n.java.JavaI18nString)
cons public init(org.netbeans.modules.i18n.I18nSupport)
fld protected java.lang.String[] arguments
meth protected void fillFormatMap(java.util.Map<java.lang.String,java.lang.String>)
meth public java.lang.Object clone()
meth public java.lang.String[] getArguments()
meth public void become(org.netbeans.modules.i18n.I18nString)
meth public void become(org.netbeans.modules.i18n.java.JavaI18nString)
 anno 0 java.lang.Deprecated()
meth public void setArguments(java.lang.String[])
supr org.netbeans.modules.i18n.I18nString

CLSS public org.netbeans.modules.i18n.java.JavaI18nSupport
cons public init(org.openide.loaders.DataObject)
fld protected boolean generateField
fld protected java.lang.String identifier
fld protected java.lang.String initFormat
fld protected java.util.Set<javax.lang.model.element.Modifier> modifiers
innr public static Factory
innr public static JavaI18nReplacer
meth protected org.netbeans.modules.i18n.I18nSupport$I18nFinder createFinder()
meth protected org.netbeans.modules.i18n.I18nSupport$I18nReplacer createReplacer()
meth protected org.netbeans.modules.i18n.ResourceHolder createResourceHolder()
meth public boolean hasAdditionalCustomizer()
meth public boolean isGenerateField()
meth public java.lang.String getIdentifier()
meth public java.lang.String getInitFormat()
meth public java.lang.String getInitString()
meth public java.util.Set<javax.lang.model.element.Modifier> getModifiers()
meth public javax.swing.JPanel getAdditionalCustomizer()
meth public javax.swing.JPanel getInfo(org.netbeans.modules.i18n.HardCodedString)
meth public org.netbeans.modules.i18n.I18nString getDefaultI18nString(org.netbeans.modules.i18n.HardCodedString)
meth public org.netbeans.modules.i18n.PropertyPanel getPropertyPanel()
meth public void createIdentifier()
meth public void performAdditionalChanges()
meth public void setGenerateField(boolean)
meth public void setIdentifier(java.lang.String)
meth public void setInitFormat(java.lang.String)
meth public void setModifiers(java.util.Set<javax.lang.model.element.Modifier>)
supr org.netbeans.modules.i18n.I18nSupport
hfds additionalCustomizer,hexaDigitChars,octalDigitChars
hcls AddFieldTask,JavaInfoPanel

CLSS public static org.netbeans.modules.i18n.java.JavaI18nSupport$Factory
 outer org.netbeans.modules.i18n.java.JavaI18nSupport
cons public init()
meth public java.lang.Class getDataObjectClass()
meth public org.netbeans.modules.i18n.I18nSupport createI18nSupport(org.openide.loaders.DataObject)
supr org.netbeans.modules.i18n.I18nSupport$Factory

CLSS public static org.netbeans.modules.i18n.java.JavaI18nSupport$JavaI18nReplacer
 outer org.netbeans.modules.i18n.java.JavaI18nSupport
cons public init()
intf org.netbeans.modules.i18n.I18nSupport$I18nReplacer
meth public void replace(org.netbeans.modules.i18n.HardCodedString,org.netbeans.modules.i18n.I18nString)
supr java.lang.Object

CLSS public org.netbeans.modules.i18n.java.JavaPropertyPanel
cons public init()
meth protected void updateReplaceText()
supr org.netbeans.modules.i18n.PropertyPanel
hfds bundle

CLSS public org.netbeans.modules.i18n.java.JavaReplacePanel
cons public init(org.netbeans.modules.i18n.java.JavaI18nSupport)
meth public void identifierTextFieldEventHandlerDelegate(java.awt.AWTEvent)
supr javax.swing.JPanel
hfds bundle,defaultRadio,fieldLabel,fieldTextField,finalCheck,generateCheck,identifierLabel,identifierTextField,initButton,initLabel,initTextField,javaI18nSupport,modifiersLabel,privateRadio,protectedRadio,publicRadio,staticCheck,transientCheck

CLSS public org.netbeans.modules.i18n.java.JavaResourceHolder
cons public init()
meth protected org.openide.loaders.DataObject createTemplate(java.lang.Class) throws java.io.IOException
meth public java.lang.Object getAllData(java.lang.String)
meth public java.lang.String findFreeKey(java.lang.String)
meth public java.lang.String getCommentForKey(java.lang.String)
meth public java.lang.String getLocalization()
meth public java.lang.String getValueForKey(java.lang.String)
meth public java.lang.String[] getAllKeys()
meth public static org.openide.loaders.DataObject getTemplate() throws java.io.IOException
meth public void addProperty(java.lang.Object,java.lang.Object,java.lang.String,boolean)
meth public void removeProperty(java.lang.Object)
meth public void setAllData(java.lang.String,java.lang.Object)
meth public void setLocalization(java.lang.String)
supr org.netbeans.modules.i18n.ResourceHolder
hfds selectedLocale

CLSS public org.netbeans.modules.i18n.java.ParamsPanel
cons public init()
innr protected ParamsListModel
meth public java.lang.String[] getArguments()
meth public void setArguments(java.lang.String[])
supr javax.swing.JPanel
hfds addParamButton,addRemovePanel,arguments,bundle,codeLabel,codePane,codeScroll,editingRow,model,paramLabel,paramsList,paramsScroll,removeParamButton,serialVersionUID

CLSS protected org.netbeans.modules.i18n.java.ParamsPanel$ParamsListModel
 outer org.netbeans.modules.i18n.java.ParamsPanel
cons public init(org.netbeans.modules.i18n.java.ParamsPanel)
meth public int getSize()
meth public java.lang.Object getElementAt(int)
meth public void fireContentsChanged(int,int)
meth public void fireIntervalAdded(int,int)
meth public void fireIntervalRemoved(int,int)
supr javax.swing.AbstractListModel
hfds serialVersionUID

CLSS public final org.openide.explorer.ExplorerManager
cons public init()
fld public final static java.lang.String PROP_EXPLORED_CONTEXT = "exploredContext"
fld public final static java.lang.String PROP_NODE_CHANGE = "nodeChange"
fld public final static java.lang.String PROP_ROOT_CONTEXT = "rootContext"
fld public final static java.lang.String PROP_SELECTED_NODES = "selectedNodes"
innr public abstract interface static Provider
intf java.io.Serializable
intf java.lang.Cloneable
meth public final org.openide.nodes.Node getExploredContext()
meth public final org.openide.nodes.Node getRootContext()
meth public final void setExploredContext(org.openide.nodes.Node)
meth public final void setExploredContext(org.openide.nodes.Node,org.openide.nodes.Node[])
meth public final void setExploredContextAndSelection(org.openide.nodes.Node,org.openide.nodes.Node[]) throws java.beans.PropertyVetoException
meth public final void setRootContext(org.openide.nodes.Node)
meth public final void setSelectedNodes(org.openide.nodes.Node[]) throws java.beans.PropertyVetoException
meth public org.openide.explorer.ExplorerManager clone()
meth public org.openide.nodes.Node[] getSelectedNodes()
meth public static org.openide.explorer.ExplorerManager find(java.awt.Component)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addVetoableChangeListener(java.beans.VetoableChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removeVetoableChangeListener(java.beans.VetoableChangeListener)
supr java.lang.Object
hfds LOCK,SCHEDULE_REMOVE_ASYNCH,SELECTION_SYNC_DELAY,actions,exploredContext,listener,propertySupport,rootContext,selectedNodes,selectionProcessor,selectionSyncTask,serialPersistentFields,serialVersionUID,vetoableSupport,weakListener
hcls Listener

CLSS public abstract interface static org.openide.explorer.ExplorerManager$Provider
 outer org.openide.explorer.ExplorerManager
meth public abstract org.openide.explorer.ExplorerManager getExplorerManager()

CLSS public abstract interface org.openide.explorer.propertysheet.ExPropertyEditor
fld public final static java.lang.String PROPERTY_HELP_ID = "helpID"
fld public final static java.lang.String PROP_VALUE_VALID = "propertyValueValid"
intf java.beans.PropertyEditor
meth public abstract void attachEnv(org.openide.explorer.propertysheet.PropertyEnv)

CLSS public abstract org.openide.nodes.Children
cons public init()
fld public final static org.openide.nodes.Children LEAF
fld public final static org.openide.util.Mutex MUTEX
innr public abstract static Keys
innr public static Array
innr public static Map
innr public static SortedArray
innr public static SortedMap
meth protected final boolean isInitialized()
meth protected final org.openide.nodes.Node getNode()
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void addNotify()
meth protected void removeNotify()
meth public abstract boolean add(org.openide.nodes.Node[])
meth public abstract boolean remove(org.openide.nodes.Node[])
meth public final int getNodesCount()
meth public final java.util.Enumeration<org.openide.nodes.Node> nodes()
meth public final java.util.List<org.openide.nodes.Node> snapshot()
meth public final org.openide.nodes.Node getNodeAt(int)
meth public final org.openide.nodes.Node[] getNodes()
meth public int getNodesCount(boolean)
meth public org.openide.nodes.Node findChild(java.lang.String)
meth public org.openide.nodes.Node[] getNodes(boolean)
meth public static <%0 extends java.lang.Object> org.openide.nodes.Children create(org.openide.nodes.ChildFactory<{%%0}>,boolean)
meth public static org.openide.nodes.Children createLazy(java.util.concurrent.Callable<org.openide.nodes.Children>)
supr java.lang.Object
hfds LOG,PR,entrySupport,lazySupport,parent
hcls Dupl,Empty,Entry,LazyChildren,ProjectManagerDeadlockDetector

CLSS public static org.openide.nodes.Children$Array
 outer org.openide.nodes.Children
cons protected init(java.util.Collection<org.openide.nodes.Node>)
cons public init()
fld protected java.util.Collection<org.openide.nodes.Node> nodes
intf java.lang.Cloneable
meth protected final void refresh()
meth protected java.util.Collection<org.openide.nodes.Node> initCollection()
meth public boolean add(org.openide.nodes.Node[])
meth public boolean remove(org.openide.nodes.Node[])
meth public java.lang.Object clone()
supr org.openide.nodes.Children
hfds COLLECTION_LOCK,nodesEntry
hcls AE

CLSS public abstract static org.openide.nodes.Children$Keys<%0 extends java.lang.Object>
 outer org.openide.nodes.Children
cons protected init(boolean)
cons public init()
meth protected abstract org.openide.nodes.Node[] createNodes({org.openide.nodes.Children$Keys%0})
meth protected final void refreshKey({org.openide.nodes.Children$Keys%0})
meth protected final void setBefore(boolean)
meth protected final void setKeys(java.util.Collection<? extends {org.openide.nodes.Children$Keys%0}>)
meth protected final void setKeys({org.openide.nodes.Children$Keys%0}[])
meth protected void destroyNodes(org.openide.nodes.Node[])
meth public boolean add(org.openide.nodes.Node[])
 anno 0 java.lang.Deprecated()
meth public boolean remove(org.openide.nodes.Node[])
 anno 0 java.lang.Deprecated()
meth public java.lang.Object clone()
supr org.openide.nodes.Children$Array
hfds before,lastRuns
hcls KE

CLSS public org.openide.nodes.FilterNode
cons public init(org.openide.nodes.Node)
cons public init(org.openide.nodes.Node,org.openide.nodes.Children)
cons public init(org.openide.nodes.Node,org.openide.nodes.Children,org.openide.util.Lookup)
fld protected final static int DELEGATE_DESTROY = 64
fld protected final static int DELEGATE_GET_ACTIONS = 128
fld protected final static int DELEGATE_GET_CONTEXT_ACTIONS = 256
fld protected final static int DELEGATE_GET_DISPLAY_NAME = 8
fld protected final static int DELEGATE_GET_NAME = 2
fld protected final static int DELEGATE_GET_SHORT_DESCRIPTION = 32
fld protected final static int DELEGATE_GET_VALUE = 1024
fld protected final static int DELEGATE_SET_DISPLAY_NAME = 4
fld protected final static int DELEGATE_SET_NAME = 1
fld protected final static int DELEGATE_SET_SHORT_DESCRIPTION = 16
fld protected final static int DELEGATE_SET_VALUE = 512
innr protected static NodeAdapter
innr protected static PropertyChangeAdapter
innr public static Children
meth protected final void changeOriginal(org.openide.nodes.Node,boolean)
meth protected final void disableDelegation(int)
meth protected final void enableDelegation(int)
meth protected java.beans.PropertyChangeListener createPropertyChangeListener()
meth protected org.openide.nodes.Node getOriginal()
meth protected org.openide.nodes.NodeListener createNodeListener()
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public boolean canCopy()
meth public boolean canCut()
meth public boolean canDestroy()
meth public boolean canRename()
meth public boolean equals(java.lang.Object)
meth public boolean hasCustomizer()
meth public int hashCode()
meth public java.awt.Component getCustomizer()
meth public java.awt.Image getIcon(int)
meth public java.awt.Image getOpenedIcon(int)
meth public java.awt.datatransfer.Transferable clipboardCopy() throws java.io.IOException
meth public java.awt.datatransfer.Transferable clipboardCut() throws java.io.IOException
meth public java.awt.datatransfer.Transferable drag() throws java.io.IOException
meth public java.lang.Object getValue(java.lang.String)
meth public java.lang.String getDisplayName()
meth public java.lang.String getHtmlDisplayName()
meth public java.lang.String getName()
meth public java.lang.String getShortDescription()
meth public javax.swing.Action getPreferredAction()
meth public javax.swing.Action[] getActions(boolean)
meth public org.openide.nodes.Node cloneNode()
meth public org.openide.nodes.Node$Handle getHandle()
meth public org.openide.nodes.Node$PropertySet[] getPropertySets()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public org.openide.util.actions.SystemAction getDefaultAction()
 anno 0 java.lang.Deprecated()
meth public org.openide.util.actions.SystemAction[] getActions()
 anno 0 java.lang.Deprecated()
meth public org.openide.util.actions.SystemAction[] getContextActions()
 anno 0 java.lang.Deprecated()
meth public org.openide.util.datatransfer.NewType[] getNewTypes()
meth public org.openide.util.datatransfer.PasteType getDropType(java.awt.datatransfer.Transferable,int,int)
meth public org.openide.util.datatransfer.PasteType[] getPasteTypes(java.awt.datatransfer.Transferable)
meth public void destroy() throws java.io.IOException
meth public void setDisplayName(java.lang.String)
meth public void setName(java.lang.String)
meth public void setShortDescription(java.lang.String)
meth public void setValue(java.lang.String,java.lang.Object)
supr org.openide.nodes.Node
hfds DELEGATE_ALL,LISTENER_LOCK,LOGGER,childrenProvided,delegateMask,hashCodeDepth,lookupProvided,nodeL,original,overridesGetDisplayNameCache,pchlAttached,propL,replaceProvidedLookupCache
hcls ChildrenAdapter,FilterHandle,FilterLookup,StackError

CLSS public static org.openide.nodes.FilterNode$Children
 outer org.openide.nodes.FilterNode
cons public init(org.openide.nodes.Node)
fld protected org.openide.nodes.Node original
intf java.lang.Cloneable
meth protected final void changeOriginal(org.openide.nodes.Node)
meth protected org.openide.nodes.Node copyNode(org.openide.nodes.Node)
meth protected org.openide.nodes.Node[] createNodes(org.openide.nodes.Node)
meth protected void addNotify()
meth protected void filterChildrenAdded(org.openide.nodes.NodeMemberEvent)
meth protected void filterChildrenRemoved(org.openide.nodes.NodeMemberEvent)
meth protected void filterChildrenReordered(org.openide.nodes.NodeReorderEvent)
meth protected void finalize()
meth protected void removeNotify()
meth public boolean add(org.openide.nodes.Node[])
 anno 0 java.lang.Deprecated()
meth public boolean remove(org.openide.nodes.Node[])
 anno 0 java.lang.Deprecated()
meth public int getNodesCount(boolean)
meth public java.lang.Object clone()
meth public org.openide.nodes.Node findChild(java.lang.String)
meth public org.openide.nodes.Node[] getNodes(boolean)
supr org.openide.nodes.Children$Keys<org.openide.nodes.Node>
hfds nodeL
hcls DefaultSupport,FilterChildrenSupport,LazySupport

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

CLSS public abstract interface org.openide.util.ContextAwareAction
intf javax.swing.Action
meth public abstract javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)

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

CLSS public abstract org.openide.util.actions.CookieAction
cons public init()
fld public final static int MODE_ALL = 4
fld public final static int MODE_ANY = 7
fld public final static int MODE_EXACTLY_ONE = 8
fld public final static int MODE_ONE = 1
fld public final static int MODE_SOME = 2
meth protected abstract int mode()
meth protected abstract java.lang.Class<?>[] cookieClasses()
meth protected boolean enable(org.openide.nodes.Node[])
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
supr org.openide.util.actions.NodeAction
hfds PROP_COOKIES,listener,serialVersionUID
hcls CookieDelegateAction,CookiesChangeListener

CLSS public abstract org.openide.util.actions.NodeAction
cons public init()
intf org.openide.util.ContextAwareAction
meth protected abstract boolean enable(org.openide.nodes.Node[])
meth protected abstract void performAction(org.openide.nodes.Node[])
meth protected boolean surviveFocusChange()
meth protected void addNotify()
meth protected void initialize()
meth protected void removeNotify()
meth public boolean isEnabled()
meth public final org.openide.nodes.Node[] getActivatedNodes()
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
meth public void actionPerformed(java.awt.event.ActionEvent)
 anno 0 java.lang.Deprecated()
meth public void performAction()
 anno 0 java.lang.Deprecated()
meth public void setEnabled(boolean)
supr org.openide.util.actions.CallableSystemAction
hfds PROP_HAS_LISTENERS,PROP_LAST_ENABLED,PROP_LAST_NODES,l,listeningActions,serialVersionUID
hcls DelegateAction,NodesL

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

