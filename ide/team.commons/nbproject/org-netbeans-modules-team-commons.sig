#Signature file v4.1
#Version 1.73

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

CLSS public abstract interface java.awt.ItemSelectable
meth public abstract java.lang.Object[] getSelectedObjects()
meth public abstract void addItemListener(java.awt.event.ItemListener)
meth public abstract void removeItemListener(java.awt.event.ItemListener)

CLSS public abstract interface java.awt.MenuContainer
meth public abstract boolean postEvent(java.awt.Event)
 anno 0 java.lang.Deprecated()
meth public abstract java.awt.Font getFont()
meth public abstract void remove(java.awt.MenuComponent)

CLSS public abstract interface java.awt.event.ActionListener
intf java.util.EventListener
meth public abstract void actionPerformed(java.awt.event.ActionEvent)

CLSS public abstract interface java.awt.event.FocusListener
intf java.util.EventListener
meth public abstract void focusGained(java.awt.event.FocusEvent)
meth public abstract void focusLost(java.awt.event.FocusEvent)

CLSS public abstract interface java.awt.event.MouseListener
intf java.util.EventListener
meth public abstract void mouseClicked(java.awt.event.MouseEvent)
meth public abstract void mouseEntered(java.awt.event.MouseEvent)
meth public abstract void mouseExited(java.awt.event.MouseEvent)
meth public abstract void mousePressed(java.awt.event.MouseEvent)
meth public abstract void mouseReleased(java.awt.event.MouseEvent)

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

CLSS public abstract interface javax.accessibility.Accessible
meth public abstract javax.accessibility.AccessibleContext getAccessibleContext()

CLSS public abstract javax.swing.AbstractButton
cons public init()
fld protected java.awt.event.ActionListener actionListener
fld protected java.awt.event.ItemListener itemListener
fld protected javax.swing.ButtonModel model
fld protected javax.swing.event.ChangeEvent changeEvent
fld protected javax.swing.event.ChangeListener changeListener
fld public final static java.lang.String BORDER_PAINTED_CHANGED_PROPERTY = "borderPainted"
fld public final static java.lang.String CONTENT_AREA_FILLED_CHANGED_PROPERTY = "contentAreaFilled"
fld public final static java.lang.String DISABLED_ICON_CHANGED_PROPERTY = "disabledIcon"
fld public final static java.lang.String DISABLED_SELECTED_ICON_CHANGED_PROPERTY = "disabledSelectedIcon"
fld public final static java.lang.String FOCUS_PAINTED_CHANGED_PROPERTY = "focusPainted"
fld public final static java.lang.String HORIZONTAL_ALIGNMENT_CHANGED_PROPERTY = "horizontalAlignment"
fld public final static java.lang.String HORIZONTAL_TEXT_POSITION_CHANGED_PROPERTY = "horizontalTextPosition"
fld public final static java.lang.String ICON_CHANGED_PROPERTY = "icon"
fld public final static java.lang.String MARGIN_CHANGED_PROPERTY = "margin"
fld public final static java.lang.String MNEMONIC_CHANGED_PROPERTY = "mnemonic"
fld public final static java.lang.String MODEL_CHANGED_PROPERTY = "model"
fld public final static java.lang.String PRESSED_ICON_CHANGED_PROPERTY = "pressedIcon"
fld public final static java.lang.String ROLLOVER_ENABLED_CHANGED_PROPERTY = "rolloverEnabled"
fld public final static java.lang.String ROLLOVER_ICON_CHANGED_PROPERTY = "rolloverIcon"
fld public final static java.lang.String ROLLOVER_SELECTED_ICON_CHANGED_PROPERTY = "rolloverSelectedIcon"
fld public final static java.lang.String SELECTED_ICON_CHANGED_PROPERTY = "selectedIcon"
fld public final static java.lang.String TEXT_CHANGED_PROPERTY = "text"
fld public final static java.lang.String VERTICAL_ALIGNMENT_CHANGED_PROPERTY = "verticalAlignment"
fld public final static java.lang.String VERTICAL_TEXT_POSITION_CHANGED_PROPERTY = "verticalTextPosition"
innr protected ButtonChangeListener
innr protected abstract AccessibleAbstractButton
intf java.awt.ItemSelectable
intf javax.swing.SwingConstants
meth protected int checkHorizontalKey(int,java.lang.String)
meth protected int checkVerticalKey(int,java.lang.String)
meth protected java.awt.event.ActionListener createActionListener()
meth protected java.awt.event.ItemListener createItemListener()
meth protected java.beans.PropertyChangeListener createActionPropertyChangeListener(javax.swing.Action)
meth protected java.lang.String paramString()
meth protected javax.swing.event.ChangeListener createChangeListener()
meth protected void actionPropertyChanged(javax.swing.Action,java.lang.String)
meth protected void addImpl(java.awt.Component,java.lang.Object,int)
meth protected void configurePropertiesFromAction(javax.swing.Action)
meth protected void fireActionPerformed(java.awt.event.ActionEvent)
meth protected void fireItemStateChanged(java.awt.event.ItemEvent)
meth protected void fireStateChanged()
meth protected void init(java.lang.String,javax.swing.Icon)
meth protected void paintBorder(java.awt.Graphics)
meth public boolean getHideActionText()
meth public boolean imageUpdate(java.awt.Image,int,int,int,int,int)
meth public boolean isBorderPainted()
meth public boolean isContentAreaFilled()
meth public boolean isFocusPainted()
meth public boolean isRolloverEnabled()
meth public boolean isSelected()
meth public int getDisplayedMnemonicIndex()
meth public int getHorizontalAlignment()
meth public int getHorizontalTextPosition()
meth public int getIconTextGap()
meth public int getMnemonic()
meth public int getVerticalAlignment()
meth public int getVerticalTextPosition()
meth public java.awt.Insets getMargin()
meth public java.awt.event.ActionListener[] getActionListeners()
meth public java.awt.event.ItemListener[] getItemListeners()
meth public java.lang.Object[] getSelectedObjects()
meth public java.lang.String getActionCommand()
meth public java.lang.String getLabel()
 anno 0 java.lang.Deprecated()
meth public java.lang.String getText()
meth public javax.swing.Action getAction()
meth public javax.swing.ButtonModel getModel()
meth public javax.swing.Icon getDisabledIcon()
meth public javax.swing.Icon getDisabledSelectedIcon()
meth public javax.swing.Icon getIcon()
meth public javax.swing.Icon getPressedIcon()
meth public javax.swing.Icon getRolloverIcon()
meth public javax.swing.Icon getRolloverSelectedIcon()
meth public javax.swing.Icon getSelectedIcon()
meth public javax.swing.event.ChangeListener[] getChangeListeners()
meth public javax.swing.plaf.ButtonUI getUI()
meth public long getMultiClickThreshhold()
meth public void addActionListener(java.awt.event.ActionListener)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void addItemListener(java.awt.event.ItemListener)
meth public void doClick()
meth public void doClick(int)
meth public void removeActionListener(java.awt.event.ActionListener)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void removeItemListener(java.awt.event.ItemListener)
meth public void removeNotify()
meth public void setAction(javax.swing.Action)
meth public void setActionCommand(java.lang.String)
meth public void setBorderPainted(boolean)
meth public void setContentAreaFilled(boolean)
meth public void setDisabledIcon(javax.swing.Icon)
meth public void setDisabledSelectedIcon(javax.swing.Icon)
meth public void setDisplayedMnemonicIndex(int)
meth public void setEnabled(boolean)
meth public void setFocusPainted(boolean)
meth public void setHideActionText(boolean)
meth public void setHorizontalAlignment(int)
meth public void setHorizontalTextPosition(int)
meth public void setIcon(javax.swing.Icon)
meth public void setIconTextGap(int)
meth public void setLabel(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void setLayout(java.awt.LayoutManager)
meth public void setMargin(java.awt.Insets)
meth public void setMnemonic(char)
meth public void setMnemonic(int)
meth public void setModel(javax.swing.ButtonModel)
meth public void setMultiClickThreshhold(long)
meth public void setPressedIcon(javax.swing.Icon)
meth public void setRolloverEnabled(boolean)
meth public void setRolloverIcon(javax.swing.Icon)
meth public void setRolloverSelectedIcon(javax.swing.Icon)
meth public void setSelected(boolean)
meth public void setSelectedIcon(javax.swing.Icon)
meth public void setText(java.lang.String)
meth public void setUI(javax.swing.plaf.ButtonUI)
meth public void setVerticalAlignment(int)
meth public void setVerticalTextPosition(int)
meth public void updateUI()
supr javax.swing.JComponent

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

CLSS public javax.swing.DefaultListCellRenderer
cons public init()
fld protected static javax.swing.border.Border noFocusBorder
innr public static UIResource
intf java.io.Serializable
intf javax.swing.ListCellRenderer<java.lang.Object>
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public boolean isOpaque()
meth public java.awt.Component getListCellRendererComponent(javax.swing.JList<?>,java.lang.Object,int,boolean,boolean)
meth public void firePropertyChange(java.lang.String,boolean,boolean)
meth public void firePropertyChange(java.lang.String,byte,byte)
meth public void firePropertyChange(java.lang.String,char,char)
meth public void firePropertyChange(java.lang.String,double,double)
meth public void firePropertyChange(java.lang.String,float,float)
meth public void firePropertyChange(java.lang.String,int,int)
meth public void firePropertyChange(java.lang.String,long,long)
meth public void firePropertyChange(java.lang.String,short,short)
meth public void invalidate()
meth public void repaint()
meth public void repaint(java.awt.Rectangle)
meth public void repaint(long,int,int,int,int)
meth public void revalidate()
meth public void validate()
supr javax.swing.JLabel

CLSS public abstract interface javax.swing.Icon
meth public abstract int getIconHeight()
meth public abstract int getIconWidth()
meth public abstract void paintIcon(java.awt.Component,java.awt.Graphics,int,int)

CLSS public javax.swing.JButton
cons public init()
cons public init(java.lang.String)
 anno 0 java.beans.ConstructorProperties(java.lang.String[] value=["text"])
cons public init(java.lang.String,javax.swing.Icon)
cons public init(javax.swing.Action)
cons public init(javax.swing.Icon)
innr protected AccessibleJButton
intf javax.accessibility.Accessible
meth protected java.lang.String paramString()
meth public boolean isDefaultButton()
meth public boolean isDefaultCapable()
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public void removeNotify()
meth public void setDefaultCapable(boolean)
meth public void updateUI()
supr javax.swing.AbstractButton

CLSS public javax.swing.JCheckBox
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,boolean)
cons public init(java.lang.String,javax.swing.Icon)
cons public init(java.lang.String,javax.swing.Icon,boolean)
cons public init(javax.swing.Action)
cons public init(javax.swing.Icon)
cons public init(javax.swing.Icon,boolean)
fld public final static java.lang.String BORDER_PAINTED_FLAT_CHANGED_PROPERTY = "borderPaintedFlat"
innr protected AccessibleJCheckBox
intf javax.accessibility.Accessible
meth protected java.lang.String paramString()
meth public boolean isBorderPaintedFlat()
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public void setBorderPaintedFlat(boolean)
meth public void updateUI()
supr javax.swing.JToggleButton

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

CLSS public javax.swing.JList<%0 extends java.lang.Object>
cons public init()
cons public init(java.util.Vector<? extends {javax.swing.JList%0}>)
cons public init(javax.swing.ListModel<{javax.swing.JList%0}>)
cons public init({javax.swing.JList%0}[])
fld public final static int HORIZONTAL_WRAP = 2
fld public final static int VERTICAL = 0
fld public final static int VERTICAL_WRAP = 1
innr protected AccessibleJList
innr public final static DropLocation
intf javax.accessibility.Accessible
intf javax.swing.Scrollable
meth protected java.lang.String paramString()
meth protected javax.swing.ListSelectionModel createSelectionModel()
meth protected void fireSelectionValueChanged(int,int,boolean)
meth public boolean getDragEnabled()
meth public boolean getScrollableTracksViewportHeight()
meth public boolean getScrollableTracksViewportWidth()
meth public boolean getValueIsAdjusting()
meth public boolean isSelectedIndex(int)
meth public boolean isSelectionEmpty()
meth public final javax.swing.DropMode getDropMode()
meth public final javax.swing.JList$DropLocation getDropLocation()
meth public final void setDropMode(javax.swing.DropMode)
meth public int getAnchorSelectionIndex()
meth public int getFirstVisibleIndex()
meth public int getFixedCellHeight()
meth public int getFixedCellWidth()
meth public int getLastVisibleIndex()
meth public int getLayoutOrientation()
meth public int getLeadSelectionIndex()
meth public int getMaxSelectionIndex()
meth public int getMinSelectionIndex()
meth public int getNextMatch(java.lang.String,int,javax.swing.text.Position$Bias)
meth public int getScrollableBlockIncrement(java.awt.Rectangle,int,int)
meth public int getScrollableUnitIncrement(java.awt.Rectangle,int,int)
meth public int getSelectedIndex()
meth public int getSelectionMode()
meth public int getVisibleRowCount()
meth public int locationToIndex(java.awt.Point)
meth public int[] getSelectedIndices()
meth public java.awt.Color getSelectionBackground()
meth public java.awt.Color getSelectionForeground()
meth public java.awt.Dimension getPreferredScrollableViewportSize()
meth public java.awt.Point indexToLocation(int)
meth public java.awt.Rectangle getCellBounds(int,int)
meth public java.lang.Object[] getSelectedValues()
 anno 0 java.lang.Deprecated()
meth public java.lang.String getToolTipText(java.awt.event.MouseEvent)
meth public java.lang.String getUIClassID()
meth public java.util.List<{javax.swing.JList%0}> getSelectedValuesList()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.ListCellRenderer<? super {javax.swing.JList%0}> getCellRenderer()
meth public javax.swing.ListModel<{javax.swing.JList%0}> getModel()
meth public javax.swing.ListSelectionModel getSelectionModel()
meth public javax.swing.event.ListSelectionListener[] getListSelectionListeners()
meth public javax.swing.plaf.ListUI getUI()
meth public void addListSelectionListener(javax.swing.event.ListSelectionListener)
meth public void addSelectionInterval(int,int)
meth public void clearSelection()
meth public void ensureIndexIsVisible(int)
meth public void removeListSelectionListener(javax.swing.event.ListSelectionListener)
meth public void removeSelectionInterval(int,int)
meth public void setCellRenderer(javax.swing.ListCellRenderer<? super {javax.swing.JList%0}>)
meth public void setDragEnabled(boolean)
meth public void setFixedCellHeight(int)
meth public void setFixedCellWidth(int)
meth public void setLayoutOrientation(int)
meth public void setListData(java.util.Vector<? extends {javax.swing.JList%0}>)
meth public void setListData({javax.swing.JList%0}[])
meth public void setModel(javax.swing.ListModel<{javax.swing.JList%0}>)
meth public void setPrototypeCellValue({javax.swing.JList%0})
meth public void setSelectedIndex(int)
meth public void setSelectedIndices(int[])
meth public void setSelectedValue(java.lang.Object,boolean)
meth public void setSelectionBackground(java.awt.Color)
meth public void setSelectionForeground(java.awt.Color)
meth public void setSelectionInterval(int,int)
meth public void setSelectionMode(int)
meth public void setSelectionModel(javax.swing.ListSelectionModel)
meth public void setUI(javax.swing.plaf.ListUI)
meth public void setValueIsAdjusting(boolean)
meth public void setVisibleRowCount(int)
meth public void updateUI()
meth public {javax.swing.JList%0} getPrototypeCellValue()
meth public {javax.swing.JList%0} getSelectedValue()
supr javax.swing.JComponent

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

CLSS public javax.swing.JToggleButton
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,boolean)
cons public init(java.lang.String,javax.swing.Icon)
cons public init(java.lang.String,javax.swing.Icon,boolean)
cons public init(javax.swing.Action)
cons public init(javax.swing.Icon)
cons public init(javax.swing.Icon,boolean)
innr protected AccessibleJToggleButton
innr public static ToggleButtonModel
intf javax.accessibility.Accessible
meth protected java.lang.String paramString()
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public void updateUI()
supr javax.swing.AbstractButton

CLSS public abstract interface javax.swing.ListCellRenderer<%0 extends java.lang.Object>
meth public abstract java.awt.Component getListCellRendererComponent(javax.swing.JList<? extends {javax.swing.ListCellRenderer%0}>,{javax.swing.ListCellRenderer%0},int,boolean,boolean)

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

CLSS public abstract interface javax.swing.event.DocumentListener
intf java.util.EventListener
meth public abstract void changedUpdate(javax.swing.event.DocumentEvent)
meth public abstract void insertUpdate(javax.swing.event.DocumentEvent)
meth public abstract void removeUpdate(javax.swing.event.DocumentEvent)

CLSS public abstract javax.swing.plaf.ComponentUI
cons public init()
meth public boolean contains(javax.swing.JComponent,int,int)
meth public int getAccessibleChildrenCount(javax.swing.JComponent)
meth public int getBaseline(javax.swing.JComponent,int,int)
meth public java.awt.Component$BaselineResizeBehavior getBaselineResizeBehavior(javax.swing.JComponent)
meth public java.awt.Dimension getMaximumSize(javax.swing.JComponent)
meth public java.awt.Dimension getMinimumSize(javax.swing.JComponent)
meth public java.awt.Dimension getPreferredSize(javax.swing.JComponent)
meth public javax.accessibility.Accessible getAccessibleChild(javax.swing.JComponent,int)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
meth public void installUI(javax.swing.JComponent)
meth public void paint(java.awt.Graphics,javax.swing.JComponent)
meth public void uninstallUI(javax.swing.JComponent)
meth public void update(java.awt.Graphics,javax.swing.JComponent)
supr java.lang.Object

CLSS public abstract javax.swing.plaf.ListUI
cons public init()
meth public abstract int locationToIndex(javax.swing.JList,java.awt.Point)
meth public abstract java.awt.Point indexToLocation(javax.swing.JList,int)
meth public abstract java.awt.Rectangle getCellBounds(javax.swing.JList,int,int)
supr javax.swing.plaf.ComponentUI

CLSS public javax.swing.plaf.basic.BasicListUI
cons public init()
fld protected final static int cellRendererChanged = 64
fld protected final static int fixedCellHeightChanged = 16
fld protected final static int fixedCellWidthChanged = 8
fld protected final static int fontChanged = 4
fld protected final static int modelChanged = 1
fld protected final static int prototypeCellValueChanged = 32
fld protected final static int selectionModelChanged = 2
fld protected int cellHeight
fld protected int cellWidth
fld protected int updateLayoutStateNeeded
fld protected int[] cellHeights
fld protected java.awt.event.FocusListener focusListener
fld protected java.beans.PropertyChangeListener propertyChangeListener
fld protected javax.swing.CellRendererPane rendererPane
fld protected javax.swing.JList list
fld protected javax.swing.event.ListDataListener listDataListener
fld protected javax.swing.event.ListSelectionListener listSelectionListener
fld protected javax.swing.event.MouseInputListener mouseInputListener
innr public FocusHandler
innr public ListDataHandler
innr public ListSelectionHandler
innr public MouseInputHandler
innr public PropertyChangeHandler
meth protected int convertRowToY(int)
meth protected int convertYToRow(int)
meth protected int getRowHeight(int)
meth protected java.awt.event.FocusListener createFocusListener()
meth protected java.beans.PropertyChangeListener createPropertyChangeListener()
meth protected javax.swing.event.ListDataListener createListDataListener()
meth protected javax.swing.event.ListSelectionListener createListSelectionListener()
meth protected javax.swing.event.MouseInputListener createMouseInputListener()
meth protected void installDefaults()
meth protected void installKeyboardActions()
meth protected void installListeners()
meth protected void maybeUpdateLayoutState()
meth protected void paintCell(java.awt.Graphics,int,java.awt.Rectangle,javax.swing.ListCellRenderer,javax.swing.ListModel,javax.swing.ListSelectionModel,int)
meth protected void selectNextIndex()
meth protected void selectPreviousIndex()
meth protected void uninstallDefaults()
meth protected void uninstallKeyboardActions()
meth protected void uninstallListeners()
meth protected void updateLayoutState()
meth public int getBaseline(javax.swing.JComponent,int,int)
meth public int locationToIndex(javax.swing.JList,java.awt.Point)
meth public java.awt.Component$BaselineResizeBehavior getBaselineResizeBehavior(javax.swing.JComponent)
meth public java.awt.Dimension getPreferredSize(javax.swing.JComponent)
meth public java.awt.Point indexToLocation(javax.swing.JList,int)
meth public java.awt.Rectangle getCellBounds(javax.swing.JList,int,int)
meth public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent)
meth public void installUI(javax.swing.JComponent)
meth public void paint(java.awt.Graphics,javax.swing.JComponent)
meth public void uninstallUI(javax.swing.JComponent)
supr javax.swing.plaf.ListUI

CLSS public org.netbeans.modules.bugtracking.commons.AttachmentPanel
cons public init(org.netbeans.modules.bugtracking.commons.AttachmentsPanel$NBBugzillaCallback)
intf java.awt.event.ActionListener
intf javax.swing.event.DocumentListener
meth public boolean isDeleted()
meth public boolean isPatch()
meth public java.io.File getFile()
meth public java.lang.String getContentType()
meth public java.lang.String getDescription()
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void changedUpdate(javax.swing.event.DocumentEvent)
meth public void insertUpdate(javax.swing.event.DocumentEvent)
meth public void removeUpdate(javax.swing.event.DocumentEvent)
supr javax.swing.JPanel
hfds PROP_DELETED,browseButton,deleteButton,descriptionField,descriptionLabel,fileField,fileTypeCombo,fileTypeLabel,nbCallback,patchChoice,patchLabel,supp,viewButton
hcls FileType

CLSS public org.netbeans.modules.bugtracking.commons.AttachmentsPanel
cons public init(javax.swing.JComponent)
innr public abstract interface static Attachment
innr public abstract interface static NBBugzillaCallback
innr public abstract static AbstractAttachment
innr public final static AttachmentInfo
meth protected void strikeThrough(javax.swing.JComponent)
meth public final void createAttachment()
meth public final void createNbLogAttachment()
meth public java.awt.Dimension getMinimumSize()
meth public java.util.List<org.netbeans.modules.bugtracking.commons.AttachmentsPanel$AttachmentInfo> getNewAttachments()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void setAttachments(java.util.List<? extends org.netbeans.modules.bugtracking.commons.AttachmentsPanel$Attachment>)
meth public void setAttachments(java.util.List<? extends org.netbeans.modules.bugtracking.commons.AttachmentsPanel$Attachment>,java.util.List<? extends org.netbeans.modules.bugtracking.commons.AttachmentsPanel$AttachmentInfo>,org.netbeans.modules.bugtracking.commons.AttachmentsPanel$NBBugzillaCallback)
supr javax.swing.JPanel
hfds LOG,attachLogFileButton,changeList,createNewButton,deletedListener,dummyAttachLabel,dummyCreateLabel,hadNoAttachments,maxMethod,newAttachments,noneLabel,parentPanel,supp
hcls CreateNewAction

CLSS public abstract static org.netbeans.modules.bugtracking.commons.AttachmentsPanel$AbstractAttachment
 outer org.netbeans.modules.bugtracking.commons.AttachmentsPanel
cons public init()
intf org.netbeans.modules.bugtracking.commons.AttachmentsPanel$Attachment
meth protected abstract java.lang.String getContentType()
meth protected abstract void getAttachementData(java.io.OutputStream)
meth public boolean canBeDeleted()
meth public boolean isDeprecated()
meth public javax.swing.Action getApplyPatchAction()
meth public javax.swing.Action getDeleteAction()
meth public javax.swing.Action getOpenAction()
meth public javax.swing.Action getOpenInStackAnalyzerAction()
meth public javax.swing.Action getSaveAction()
meth public void open()
meth public void openInStackAnalyzer()
supr java.lang.Object
hfds applyPatchAction,openAttachmentAction,openStacktraceAction,saveAttachmentAction
hcls ApplyPatchAction,OpenAttachmentAction,OpenInStackAnalyzerAction,SaveAttachmentAction

CLSS public abstract interface static org.netbeans.modules.bugtracking.commons.AttachmentsPanel$Attachment
 outer org.netbeans.modules.bugtracking.commons.AttachmentsPanel
meth public abstract boolean canBeDeleted()
meth public abstract boolean isDeprecated()
meth public abstract boolean isPatch()
meth public abstract java.lang.String getAuthor()
meth public abstract java.lang.String getAuthorName()
meth public abstract java.lang.String getDesc()
meth public abstract java.lang.String getFilename()
meth public abstract java.util.Date getDate()
meth public abstract javax.swing.Action getApplyPatchAction()
meth public abstract javax.swing.Action getDeleteAction()
meth public abstract javax.swing.Action getOpenAction()
meth public abstract javax.swing.Action getOpenInStackAnalyzerAction()
meth public abstract javax.swing.Action getSaveAction()

CLSS public final static org.netbeans.modules.bugtracking.commons.AttachmentsPanel$AttachmentInfo
 outer org.netbeans.modules.bugtracking.commons.AttachmentsPanel
cons public init()
meth public boolean isPatch()
meth public java.io.File getFile()
meth public java.lang.String getContentType()
meth public java.lang.String getDescription()
meth public void setContentType(java.lang.String)
meth public void setDescription(java.lang.String)
meth public void setFile(java.io.File)
meth public void setIsPatch(boolean)
supr java.lang.Object
hfds contentType,description,file,isPatch

CLSS public abstract interface static org.netbeans.modules.bugtracking.commons.AttachmentsPanel$NBBugzillaCallback
 outer org.netbeans.modules.bugtracking.commons.AttachmentsPanel
meth public abstract java.lang.String getLogFileContentType()
meth public abstract java.lang.String getLogFileDescription()
meth public abstract java.lang.String getLogFilePath()
meth public abstract void showLogFile()

CLSS public org.netbeans.modules.bugtracking.commons.AutoupdatePanel
cons public init(org.netbeans.modules.bugtracking.commons.AutoupdateSupport)
supr javax.swing.JPanel
hfds dontShowCheckBox,jScrollPane1,support,txtTextArea

CLSS public final org.netbeans.modules.bugtracking.commons.AutoupdateSupport
cons public init(org.netbeans.modules.bugtracking.commons.AutoupdateSupport$Callback,java.lang.String,java.lang.String)
innr public abstract interface static Callback
meth public org.netbeans.modules.team.ide.spi.IDEServices$Plugin checkNewPluginAvailable()
meth public void checkAndNotify(java.lang.String)
supr java.lang.Object
hfds CHECK_UPDATES,LOG,callback,cnb,lastChecks,loggedUrls,pluginName

CLSS public abstract interface static org.netbeans.modules.bugtracking.commons.AutoupdateSupport$Callback
 outer org.netbeans.modules.bugtracking.commons.AutoupdateSupport
meth public abstract boolean checkIfShouldDownload(java.lang.String)
meth public abstract boolean isSupportedVersion(java.lang.String)
meth public abstract java.lang.String getServerVersion(java.lang.String)

CLSS public final org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel
cons public init()
intf java.awt.event.FocusListener
meth public boolean isExpanded()
meth public java.lang.String getLabel()
meth public javax.swing.AbstractButton getLabelComponent()
meth public void focusGained(java.awt.event.FocusEvent)
meth public void focusLost(java.awt.event.FocusEvent)
meth public void setActions(javax.swing.Action[])
meth public void setContent(javax.swing.JComponent)
meth public void setExpanded(boolean)
meth public void setLabel(java.lang.String)
supr javax.swing.JPanel
hfds actionsPanel,content,dummyContentPanel,headerPanel,isAqua,isGTK,isNimbus,jLabel1,sectionButton1
hcls ActionsBuilder

CLSS public org.netbeans.modules.bugtracking.commons.FileToRepoMappingStorage
cons public init()
meth public boolean setLooseAssociation(java.io.File,java.lang.String)
meth public java.lang.String getFirmlyAssociatedRepository(java.io.File)
meth public java.lang.String getLooselyAssociatedRepository(java.io.File)
meth public java.lang.String getRepository(java.io.File)
meth public java.util.Collection<java.lang.String> getAllFirmlyAssociatedUrls()
meth public static java.lang.String cutTrailingSlashes(java.lang.String)
meth public static org.netbeans.modules.bugtracking.commons.FileToRepoMappingStorage getInstance()
meth public void setFirmAssociation(java.io.File,java.lang.String)
supr java.lang.Object
hfds FIRM_ASSOCIATION,LOG,LOOSE_ASSOCIATION,REPOSITORY_FOR_FILE_PREFIX,instance

CLSS public final org.netbeans.modules.bugtracking.commons.HyperlinkSupport
fld public final static java.lang.String LINK_ATTRIBUTE = "attribute.simple.link"
innr public abstract interface static IssueLinker
innr public abstract interface static IssueRefProvider
innr public abstract interface static Link
meth public static org.netbeans.modules.bugtracking.commons.HyperlinkSupport getInstance()
meth public void register(java.awt.Component)
meth public void register(org.openide.windows.TopComponent,org.netbeans.modules.bugtracking.commons.HyperlinkSupport$IssueLinker)
meth public void registerLink(javax.swing.JTextPane,int[],org.netbeans.modules.bugtracking.commons.HyperlinkSupport$Link)
supr java.lang.Object
hfds REGISTER_TASK,STACKTRACE_ATTRIBUTE,TYPE_ATTRIBUTE,URL_ATTRIBUTE,instance,motionListener,mouseListener,regListener,registerTask,rp
hcls MotionListener,MouseListener,RegisterTask

CLSS public abstract interface static org.netbeans.modules.bugtracking.commons.HyperlinkSupport$IssueLinker
 outer org.netbeans.modules.bugtracking.commons.HyperlinkSupport
intf org.netbeans.modules.bugtracking.commons.HyperlinkSupport$IssueRefProvider
intf org.netbeans.modules.bugtracking.commons.HyperlinkSupport$Link

CLSS public abstract interface static org.netbeans.modules.bugtracking.commons.HyperlinkSupport$IssueRefProvider
 outer org.netbeans.modules.bugtracking.commons.HyperlinkSupport
meth public abstract int[] getIssueRefSpans(java.lang.CharSequence)

CLSS public abstract interface static org.netbeans.modules.bugtracking.commons.HyperlinkSupport$Link
 outer org.netbeans.modules.bugtracking.commons.HyperlinkSupport
meth public abstract void onClick(java.lang.String)

CLSS public org.netbeans.modules.bugtracking.commons.IssueSettingsStorage
meth public java.util.Collection<java.lang.Long> loadCollapsedCommenst(java.lang.String,java.lang.String)
meth public static org.netbeans.modules.bugtracking.commons.IssueSettingsStorage getInstance()
meth public void storeCollapsedComments(java.util.Collection<java.lang.Long>,java.lang.String,java.lang.String)
supr java.lang.Object
hfds LOG,PROP_COLLAPSED_COMMENT_PREFIX,instance,loggedUrls,storage
hcls FileLocks

CLSS public org.netbeans.modules.bugtracking.commons.JiraUpdater
meth public static boolean isJiraInstalled()
meth public static boolean notifyJiraDownload(java.lang.String)
meth public static org.netbeans.modules.bugtracking.commons.JiraUpdater getInstance()
meth public void downloadAndInstall(java.lang.String)
supr java.lang.Object
hfds JIRA_CNB,instance

CLSS public org.netbeans.modules.bugtracking.commons.LinkButton
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,javax.swing.Icon)
cons public init(javax.swing.Action)
cons public init(javax.swing.Icon)
innr public static MailtoButton
intf java.awt.event.FocusListener
intf java.awt.event.MouseListener
meth protected boolean isVisited()
meth protected void onMouseEntered(java.awt.event.MouseEvent)
meth protected void onMouseExited(java.awt.event.MouseEvent)
meth protected void paintComponent(java.awt.Graphics)
meth public void focusGained(java.awt.event.FocusEvent)
meth public void focusLost(java.awt.event.FocusEvent)
meth public void mouseClicked(java.awt.event.MouseEvent)
meth public void mouseEntered(java.awt.event.MouseEvent)
meth public void mouseExited(java.awt.event.MouseEvent)
meth public void mousePressed(java.awt.event.MouseEvent)
meth public void mouseReleased(java.awt.event.MouseEvent)
meth public void paint(java.awt.Graphics)
meth public void setColors(java.awt.Color,java.awt.Color,java.awt.Color,java.awt.Color)
supr javax.swing.JButton
hfds BUTTON_FONT,LINK_IN_FOCUS_STROKE,alternativeLinkColor,linkColor,linkInFocusColor,mouseOverLinkColor,underline,visitedLinkColor

CLSS public static org.netbeans.modules.bugtracking.commons.LinkButton$MailtoButton
 outer org.netbeans.modules.bugtracking.commons.LinkButton
cons public init(java.lang.String,java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
supr org.netbeans.modules.bugtracking.commons.LinkButton

CLSS public org.netbeans.modules.bugtracking.commons.ListValuePicker
innr public static ListValue
meth public static java.lang.String getValues(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.util.List<java.lang.String>)
meth public static java.lang.String getValues(java.lang.String,java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.bugtracking.commons.ListValuePicker$ListValue[])
supr javax.swing.JPanel
hfds messageLabel,scrollPane,valuesLabel,valuesList
hcls ListValueRenderer

CLSS public static org.netbeans.modules.bugtracking.commons.ListValuePicker$ListValue
 outer org.netbeans.modules.bugtracking.commons.ListValuePicker
cons public init(java.lang.String,java.lang.String)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
supr java.lang.Object
hfds displayValue,value

CLSS public final org.netbeans.modules.bugtracking.commons.NBBugzillaUtils
cons public init()
meth public static boolean isNbRepository(java.lang.String)
meth public static char[] getNBPassword()
meth public static java.lang.String getNBUsername()
meth public static void saveNBPassword(char[])
meth public static void saveNBUsername(java.lang.String)
supr java.lang.Object
hfds NB_BUGZILLA_PASSWORD,NB_BUGZILLA_USERNAME,netbeansUrlPattern

CLSS public org.netbeans.modules.bugtracking.commons.NoContentPanel
cons public init()
meth public void setProgressComponent(java.awt.Component)
meth public void setText(java.lang.String)
supr javax.swing.JPanel
hfds jPanel2,label,progressComponent

CLSS public final org.netbeans.modules.bugtracking.commons.PatchUtils
cons public init()
meth public static boolean isAvailable()
meth public static boolean isPatch(java.io.File) throws java.io.IOException
meth public static void applyPatch(java.io.File)
supr java.lang.Object

CLSS public org.netbeans.modules.bugtracking.commons.SaveQueryPanel
innr public abstract static QueryNameValidator
intf javax.swing.event.DocumentListener
meth public static java.lang.String show(org.netbeans.modules.bugtracking.commons.SaveQueryPanel$QueryNameValidator,org.openide.util.HelpCtx)
meth public void changedUpdate(javax.swing.event.DocumentEvent)
meth public void insertUpdate(javax.swing.event.DocumentEvent)
meth public void removeUpdate(javax.swing.event.DocumentEvent)
supr javax.swing.JPanel
hfds ERROR_COLOR,descriptor,jLabel3,ok,queryNameTextField,saveErrorLabel,validator

CLSS public abstract static org.netbeans.modules.bugtracking.commons.SaveQueryPanel$QueryNameValidator
 outer org.netbeans.modules.bugtracking.commons.SaveQueryPanel
cons public init()
meth public abstract java.lang.String isValid(java.lang.String)
supr java.lang.Object

CLSS public final org.netbeans.modules.bugtracking.commons.SectionPanel
cons public init()
meth public java.lang.String getLabel()
meth public javax.swing.JLabel getLabelComponent()
meth public void setActions(javax.swing.Action[])
meth public void setContent(javax.swing.JComponent)
meth public void setLabel(java.lang.String)
supr javax.swing.JPanel
hfds actionsPanel,content,dummyContentPanel,headerPanel,isAqua,isGTK,isNimbus,jLabel1,lblTitle
hcls ActionsBuilder

CLSS public org.netbeans.modules.bugtracking.commons.SimpleIssueFinder
meth public int[] getIssueSpans(java.lang.CharSequence)
meth public java.lang.String getIssueId(java.lang.String)
meth public static org.netbeans.modules.bugtracking.commons.SimpleIssueFinder getInstance()
supr java.lang.Object
hfds EMPTY_INT_ARR,instance
hcls Impl

CLSS public org.netbeans.modules.bugtracking.commons.TextUtils
meth public static java.lang.String decodeURL(java.lang.String)
meth public static java.lang.String encodeURL(java.lang.String)
meth public static java.lang.String escapeForHTMLLabel(java.lang.String)
meth public static java.lang.String getMD5(java.lang.String)
meth public static java.lang.String shortenText(java.lang.String,int,int)
meth public static java.lang.String trimSpecial(java.lang.String)
supr java.lang.Object

CLSS public final org.netbeans.modules.bugtracking.commons.TransparentSectionButton
cons public init()
cons public init(java.awt.event.ActionListener)
meth public java.awt.Color getForeground()
meth public java.lang.String getUIClassID()
meth public void addActionListener(java.awt.event.ActionListener)
supr javax.swing.JCheckBox
hfds al,isAqua,isGTK,isNimbus
hcls ExpandAction

CLSS public org.netbeans.modules.bugtracking.commons.UIUtils
cons public init()
innr public abstract interface static SizeController
meth public static boolean isNimbus()
meth public static int getColumnWidthInPixels(int,javax.swing.JComponent)
meth public static int getColumnWidthInPixels(java.lang.String,javax.swing.JComponent)
meth public static int getLongestWordWidth(java.lang.String,java.lang.String[],javax.swing.JComponent)
meth public static int getLongestWordWidth(java.lang.String,java.lang.String[],javax.swing.JComponent,boolean)
meth public static int getLongestWordWidth(java.lang.String,java.util.List<java.lang.String>,javax.swing.JComponent)
meth public static int getLongestWordWidth(java.lang.String,java.util.List<java.lang.String>,javax.swing.JComponent,boolean)
meth public static java.awt.Color getCollapsedPanelBackground()
meth public static java.awt.Color getLinkColor()
meth public static java.awt.Color getSectionPanelBackground()
meth public static java.awt.Color getTaskConflictColor()
meth public static java.awt.Color getTaskModifiedColor()
meth public static java.awt.Color getTaskNewColor()
meth public static java.awt.Color getTaskObsoleteColor()
meth public static java.lang.String getColorString(java.awt.Color)
meth public static org.netbeans.modules.team.ide.spi.IDEServices$DatePickerComponent createDatePickerComponent()
meth public static void fixFocusTraversalKeys(javax.swing.JComponent)
meth public static void issue163946Hack(javax.swing.JScrollPane)
meth public static void keepComponentsWidthByVisibleArea(javax.swing.JPanel,org.netbeans.modules.bugtracking.commons.UIUtils$SizeController)
meth public static void keepFocusedComponentVisible(java.awt.Component,javax.swing.JComponent)
meth public static void keepFocusedComponentVisible(javax.swing.JComponent)
meth public static void runInAWT(java.lang.Runnable)
meth public static void setWaitCursor(boolean)
supr java.lang.Object
hfds scrollingFocusListener
hcls DummyDatePickerComponent,NotShowingFieldsFocusListener

CLSS public abstract interface static org.netbeans.modules.bugtracking.commons.UIUtils$SizeController
 outer org.netbeans.modules.bugtracking.commons.UIUtils
meth public abstract void setWidth(int)

CLSS public org.netbeans.modules.bugtracking.commons.UndoRedoSupport
cons public init()
meth public org.openide.awt.UndoRedo getUndoRedo()
meth public void register(org.openide.windows.TopComponent,boolean)
supr java.lang.Object
hfds ACTION_NAME_REDO,ACTION_NAME_UNDO,DELIMITER_PATTERN,REGISTER_TASK,delegateManager,registerTask,rp,undoRedoListener
hcls CompoundUndoManager,DelegateManager,RegisterTask,UndoRedoListener

CLSS public final org.netbeans.modules.bugtracking.commons.Util
cons public init()
meth public static boolean show(javax.swing.JPanel,java.lang.String,java.lang.String)
meth public static java.io.File getLargerContext(java.io.File)
meth public static java.io.File getLargerContext(java.io.File,org.openide.filesystems.FileObject)
meth public static java.io.File getLargerContext(org.openide.filesystems.FileObject)
meth public static org.openide.filesystems.FileObject getFileOwnerDirectory(org.openide.filesystems.FileObject)
meth public static void notifyError(java.lang.String,java.lang.String)
supr java.lang.Object

CLSS public org.netbeans.modules.team.commons.ColorManager
meth public boolean isAqua()
meth public boolean isGtk()
meth public boolean isNimbus()
meth public boolean isWindows()
meth public java.awt.Color getDefaultBackground()
meth public java.awt.Color getDefaultForeground()
meth public java.awt.Color getDisabledColor()
meth public java.awt.Color getErrorColor()
meth public java.awt.Color getExpandableRootBackground()
meth public java.awt.Color getExpandableRootForeground()
meth public java.awt.Color getExpandableRootSelectedBackground()
meth public java.awt.Color getExpandableRootSelectedForeground()
meth public java.awt.Color getLinkColor()
meth public java.awt.Color getStableBuildColor()
meth public java.awt.Color getTitleBackground()
meth public java.awt.Color getTitleSelectedBackground()
meth public java.awt.Color getUnstableBuildColor()
meth public static org.netbeans.modules.team.commons.ColorManager getDefault()
meth public static org.netbeans.modules.team.commons.ColorManager getTheInstance()
supr java.lang.Object
hfds ROOT_BACKGROUND,ROOT_SELECTION_BACKGROUND,TITLE_BACKGROUND,TITLE_SELECTION_BACKGROUND,defaultBackground,defaultForeground,disabledColor,errorColor,expandableRootBackground,expandableRootForeground,expandableRootSelectedBackground,expandableRootSelectedForeground,isAqua,isGtk,isNimbus,isWindows,linkColor,stableBuildColor,theInstance,titleBackground,titleSelectedBackground,unstableBuildColor

CLSS public final org.netbeans.modules.team.commons.LogUtils
cons public init()
fld public final static java.lang.String USG_BUGTRACKING_AUTOMATIC_REFRESH = "USG_BUGTRACKING_AUTOMATIC_REFRESH"
fld public final static java.lang.String USG_BUGTRACKING_QUERY = "USG_BUGTRACKING_QUERY"
meth public static java.lang.String getBugtrackingType(java.lang.String)
meth public static java.lang.String getKnownRepositoryFor(java.lang.String)
meth public static java.lang.String getPasswordLog(char[])
meth public static void logAutoRefreshEvent(java.lang.String,java.lang.String,boolean,boolean)
meth public static void logBugtrackingUsage(java.lang.String,java.lang.String)
meth public static void logQueryEvent(java.lang.String,java.lang.String,int,boolean,boolean)
meth public static void logRepositoryUsage(java.lang.String,java.lang.String)
supr java.lang.Object
hfds METRICS_LOG,USG_ISSUE_TRACKING,USG_ISSUE_TRACKING_REPOSITORY,loggedParams

CLSS public abstract org.netbeans.modules.team.commons.treelist.AsynchronousNode<%0 extends java.lang.Object>
cons public init(boolean,org.netbeans.modules.team.commons.treelist.TreeListNode,java.lang.String)
cons public init(boolean,org.netbeans.modules.team.commons.treelist.TreeListNode,java.lang.String,javax.swing.Icon)
meth protected abstract javax.swing.JComponent createComponent({org.netbeans.modules.team.commons.treelist.AsynchronousNode%0})
meth protected abstract void configure(javax.swing.JComponent,java.awt.Color,java.awt.Color,boolean,boolean,int)
meth protected abstract {org.netbeans.modules.team.commons.treelist.AsynchronousNode%0} load()
meth protected final javax.swing.JComponent getComponent(java.awt.Color,java.awt.Color,boolean,boolean,int)
meth protected final void refresh()
meth protected java.lang.String getTitle(javax.swing.JComponent,boolean,boolean,int)
meth protected void setLoadingVisible(boolean)
supr org.netbeans.modules.team.commons.treelist.TreeListNode
hfds LOCK,RP,btnRetry,expandAfterRefresh,icon,inner,lblError,lblFill,lblIcon,lblLoading,lblTitle,loaded,loader,panel,title
hcls Loader

CLSS public abstract org.netbeans.modules.team.commons.treelist.LeafNode
cons public init(org.netbeans.modules.team.commons.treelist.TreeListNode)
meth protected final java.util.List<org.netbeans.modules.team.commons.treelist.TreeListNode> createChildren()
supr org.netbeans.modules.team.commons.treelist.TreeListNode

CLSS public org.netbeans.modules.team.commons.treelist.LinkButton
cons public init(java.lang.String,boolean,javax.swing.Action)
cons public init(java.lang.String,boolean,javax.swing.Action,boolean)
cons public init(java.lang.String,javax.swing.Action)
cons public init(java.lang.String,javax.swing.Action,boolean)
cons public init(java.lang.String,javax.swing.Icon,javax.swing.Action,boolean)
cons public init(javax.swing.Icon,javax.swing.Action)
meth public void setForeground(java.awt.Color,boolean)
supr javax.swing.JButton
hfds handlePopupEvents,underlined
hcls Model

CLSS public abstract interface org.netbeans.modules.team.commons.treelist.ListListener
meth public abstract void contentChanged(org.netbeans.modules.team.commons.treelist.ListNode)
meth public abstract void contentSizeChanged(org.netbeans.modules.team.commons.treelist.ListNode)

CLSS public abstract org.netbeans.modules.team.commons.treelist.ListNode
cons public init()
meth protected abstract javax.swing.JComponent getComponent(java.awt.Color,java.awt.Color,boolean,boolean,int)
meth protected javax.swing.Action getDefaultAction()
meth protected void attach()
meth public javax.swing.Action[] getPopupActions()
supr java.lang.Object
hfds lastRowWidth,listener,renderer

CLSS public final org.netbeans.modules.team.commons.treelist.ListRendererPanel
cons public init(org.netbeans.modules.team.commons.treelist.ListNode)
meth public java.lang.String getToolTipText(java.awt.event.MouseEvent)
meth public void configure(java.awt.Color,java.awt.Color,boolean,boolean,int,int)
supr javax.swing.JPanel
hfds node

CLSS public final org.netbeans.modules.team.commons.treelist.ProgressLabel
cons public init(java.lang.String,java.awt.Component)
cons public init(java.lang.String,org.netbeans.modules.team.commons.treelist.TreeListNode)
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public void invalidate()
meth public void repaint()
meth public void setVisible(boolean)
meth public void stop()
meth public void validate()
supr org.netbeans.modules.team.commons.treelist.TreeLabel
hfds busyIcon,refComp,refNode,t
hcls RotatingImageBusyIcon

CLSS public final org.netbeans.modules.team.commons.treelist.SelectionList
cons public init()
meth public int getVisibleRowCount()
meth public java.lang.String getToolTipText(java.awt.event.MouseEvent)
meth public void setItems(java.util.List<org.netbeans.modules.team.commons.treelist.ListNode>)
meth public void setUI(javax.swing.plaf.ListUI)
supr javax.swing.JList<org.netbeans.modules.team.commons.treelist.ListNode>
hfds ACTION_SELECT,ACTION_SHOW_POPUP,INSETS_BOTTOM,INSETS_LEFT,INSETS_RIGHT,INSETS_TOP,MAX_VISIBLE_ROWS,ROW_HEIGHT,mouseOverRow,nodeListener,renderer
hcls RendererImpl,SelectionListModel,SelectionListUI

CLSS public org.netbeans.modules.team.commons.treelist.TreeLabel
cons public init()
cons public init(java.lang.String)
supr javax.swing.JLabel

CLSS public org.netbeans.modules.team.commons.treelist.TreeList
cons public init(org.netbeans.modules.team.commons.treelist.TreeListModel)
meth public java.lang.String getToolTipText(java.awt.event.MouseEvent)
meth public void setUI(javax.swing.plaf.ListUI)
meth public void updateUI()
supr javax.swing.JList
hfds ACTION_COLLAPSE,ACTION_DEFAULT,ACTION_EXPAND,ACTION_SHOW_POPUP,INSETS_BOTTOM,INSETS_LEFT,INSETS_RIGHT,INSETS_TOP,ROW_HEIGHT,collapseAction,defaultAction,expandAction,renderer,showPopupAction
hcls CollapseAction,DefaultAction,ExpandAction,ShowPopupAction,TreeListRenderer

CLSS public abstract interface org.netbeans.modules.team.commons.treelist.TreeListListener
intf org.netbeans.modules.team.commons.treelist.ListListener
meth public abstract void childrenAdded(org.netbeans.modules.team.commons.treelist.TreeListNode)
meth public abstract void childrenRemoved(org.netbeans.modules.team.commons.treelist.TreeListNode)

CLSS public org.netbeans.modules.team.commons.treelist.TreeListModel
cons public init()
intf org.netbeans.modules.team.commons.treelist.TreeListListener
meth protected void fireContentsChanged(java.lang.Object,int,int)
meth protected void fireIntervalAdded(java.lang.Object,int,int)
meth protected void fireIntervalRemoved(java.lang.Object,int,int)
meth public int getSize()
meth public java.lang.Object getElementAt(int)
meth public java.util.List<org.netbeans.modules.team.commons.treelist.TreeListNode> getAllNodes()
meth public java.util.List<org.netbeans.modules.team.commons.treelist.TreeListNode> getRootNodes()
meth public void addModelListener(org.netbeans.modules.team.commons.treelist.TreeListModelListener)
meth public void addRoot(int,org.netbeans.modules.team.commons.treelist.TreeListNode)
meth public void childrenAdded(org.netbeans.modules.team.commons.treelist.TreeListNode)
meth public void childrenRemoved(org.netbeans.modules.team.commons.treelist.TreeListNode)
meth public void clear()
meth public void contentChanged(org.netbeans.modules.team.commons.treelist.ListNode)
meth public void contentSizeChanged(org.netbeans.modules.team.commons.treelist.ListNode)
meth public void removeModelListener(org.netbeans.modules.team.commons.treelist.TreeListModelListener)
meth public void removeRoot(org.netbeans.modules.team.commons.treelist.TreeListNode)
supr javax.swing.AbstractListModel
hfds modelListeners,nodes

CLSS public abstract interface org.netbeans.modules.team.commons.treelist.TreeListModelListener
meth public abstract void nodeExpanded(org.netbeans.modules.team.commons.treelist.TreeListNode)

CLSS public abstract org.netbeans.modules.team.commons.treelist.TreeListNode
cons public init(boolean,boolean,org.netbeans.modules.team.commons.treelist.TreeListNode)
cons public init(boolean,org.netbeans.modules.team.commons.treelist.TreeListNode)
fld public final static long TIMEOUT_INTERVAL_MILLIS
innr protected final static !enum Type
meth protected abstract java.util.List<org.netbeans.modules.team.commons.treelist.TreeListNode> createChildren()
meth protected final org.netbeans.modules.team.commons.treelist.ProgressLabel createProgressLabel()
meth protected final org.netbeans.modules.team.commons.treelist.ProgressLabel createProgressLabel(java.lang.String)
meth protected final void fireContentChanged()
meth protected final void fireContentSizeChanged()
meth protected final void refreshChildren()
meth protected org.netbeans.modules.team.commons.treelist.TreeListNode$Type getType()
meth protected static void post(java.lang.Runnable)
meth protected void attach()
meth protected void childrenLoadingFinished()
meth protected void childrenLoadingStarted()
meth protected void childrenLoadingTimedout()
meth protected void dispose()
meth public boolean getIndentChildren()
meth public boolean isRenderedWithGradient()
meth public final boolean isExpandable()
meth public final boolean isExpanded()
meth public final java.util.List<org.netbeans.modules.team.commons.treelist.TreeListNode> getChildren()
meth public final org.netbeans.modules.team.commons.treelist.TreeListNode getParent()
meth public final void setExpanded(boolean)
meth public final void setListener(org.netbeans.modules.team.commons.treelist.TreeListListener)
meth public void setIndentChildren(boolean)
supr org.netbeans.modules.team.commons.treelist.ListNode
hfds LOCK,children,expandable,expanded,indentChildren,lastRowWidth,listener,loader,parent,renderGradient,renderer,rp,type
hcls ChildrenLoader

CLSS protected final static !enum org.netbeans.modules.team.commons.treelist.TreeListNode$Type
 outer org.netbeans.modules.team.commons.treelist.TreeListNode
fld public final static org.netbeans.modules.team.commons.treelist.TreeListNode$Type CLOSED
fld public final static org.netbeans.modules.team.commons.treelist.TreeListNode$Type NORMAL
fld public final static org.netbeans.modules.team.commons.treelist.TreeListNode$Type TITLE
meth public static org.netbeans.modules.team.commons.treelist.TreeListNode$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.team.commons.treelist.TreeListNode$Type[] values()
supr java.lang.Enum<org.netbeans.modules.team.commons.treelist.TreeListNode$Type>

CLSS public org.netbeans.modules.team.commons.treelist.TreeListUI
cons public init()
meth protected javax.swing.event.MouseInputListener createMouseInputListener()
supr javax.swing.plaf.basic.BasicListUI

CLSS public abstract org.netbeans.modules.team.ide.spi.IDEProject
cons protected init(java.lang.String,javax.swing.Icon,java.net.URL)
innr public abstract interface static DeleteListener
innr public abstract interface static OpenListener
meth protected final java.util.List<org.netbeans.modules.team.ide.spi.IDEProject$DeleteListener> getDeleteListeners()
meth public boolean addDeleteListener(org.netbeans.modules.team.ide.spi.IDEProject$DeleteListener)
meth public boolean equals(java.lang.Object)
meth public boolean removeDeleteListener(org.netbeans.modules.team.ide.spi.IDEProject$DeleteListener)
meth public final void notifyDeleted()
meth public int hashCode()
meth public java.lang.String getDisplayName()
meth public java.net.URL getURL()
meth public javax.swing.Icon getIcon()
supr java.lang.Object
hfds deleteListeners,displayName,icon,url

CLSS public abstract interface static org.netbeans.modules.team.ide.spi.IDEProject$DeleteListener
 outer org.netbeans.modules.team.ide.spi.IDEProject
meth public abstract void projectDeleted(org.netbeans.modules.team.ide.spi.IDEProject)

CLSS public abstract interface static org.netbeans.modules.team.ide.spi.IDEProject$OpenListener
 outer org.netbeans.modules.team.ide.spi.IDEProject
meth public abstract void projectsOpened(org.netbeans.modules.team.ide.spi.IDEProject[])

CLSS public abstract interface org.netbeans.modules.team.ide.spi.IDEServices
innr public abstract interface static BusyIcon
innr public abstract interface static DatePickerComponent
innr public abstract interface static Plugin
meth public abstract boolean canOpenInFavorites()
meth public abstract boolean isPatch(java.io.File) throws java.io.IOException
meth public abstract boolean isPluginInstalled(java.lang.String)
meth public abstract boolean openHistory(java.lang.String,int)
meth public abstract boolean providesJumpTo()
meth public abstract boolean providesOpenDocument()
meth public abstract boolean providesOpenHistory()
meth public abstract boolean providesOpenInStackAnalyzer()
meth public abstract boolean providesPatchUtils()
meth public abstract boolean providesPluginUpdate()
meth public abstract boolean providesShutdown(boolean)
meth public abstract org.netbeans.modules.team.ide.spi.IDEServices$BusyIcon createBusyIcon()
meth public abstract org.netbeans.modules.team.ide.spi.IDEServices$DatePickerComponent createDatePicker()
meth public abstract org.netbeans.modules.team.ide.spi.IDEServices$Plugin getPluginUpdates(java.lang.String,java.lang.String)
meth public abstract void applyPatch(java.io.File)
meth public abstract void jumpTo(java.lang.String,java.lang.String)
meth public abstract void openDocument(java.lang.String,int)
meth public abstract void openInFavorites(java.io.File)
meth public abstract void openInStackAnalyzer(java.io.BufferedReader)
meth public abstract void shutdown(boolean)

CLSS public abstract interface static org.netbeans.modules.team.ide.spi.IDEServices$BusyIcon
 outer org.netbeans.modules.team.ide.spi.IDEServices
intf javax.swing.Icon
meth public abstract void tick()

CLSS public abstract interface static org.netbeans.modules.team.ide.spi.IDEServices$DatePickerComponent
 outer org.netbeans.modules.team.ide.spi.IDEServices
meth public abstract boolean allowsOpeningDaySelector()
meth public abstract boolean openDaySelector()
meth public abstract java.util.Date getDate()
meth public abstract javax.swing.JComponent getComponent()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public abstract void setDate(java.util.Date)

CLSS public abstract interface static org.netbeans.modules.team.ide.spi.IDEServices$Plugin
 outer org.netbeans.modules.team.ide.spi.IDEServices
meth public abstract boolean installOrUpdate()
meth public abstract java.lang.String getDescription()

CLSS public abstract interface org.netbeans.modules.team.ide.spi.ProjectServices
meth public abstract <%0 extends java.lang.Object> {%%0} runAfterProjectOpenFinished(java.util.concurrent.Callable<{%%0}>) throws java.lang.Exception
meth public abstract boolean openProject(java.net.URL)
meth public abstract java.io.File[] chooseProjects(java.io.File)
meth public abstract org.netbeans.modules.team.ide.spi.IDEProject getIDEProject(java.net.URL)
meth public abstract org.netbeans.modules.team.ide.spi.IDEProject[] getOpenProjects()
meth public abstract org.openide.filesystems.FileObject getFileOwnerDirectory(org.openide.filesystems.FileObject)
meth public abstract org.openide.filesystems.FileObject getMainProjectDirectory()
meth public abstract org.openide.filesystems.FileObject[] getCurrentSelection()
meth public abstract org.openide.filesystems.FileObject[] getOpenProjectsDirectories()
meth public abstract void addProjectOpenListener(org.netbeans.modules.team.ide.spi.IDEProject$OpenListener)
meth public abstract void createNewProject(java.io.File)
meth public abstract void openOtherProject(java.io.File)
meth public abstract void removeProjectOpenListener(org.netbeans.modules.team.ide.spi.IDEProject$OpenListener)
meth public abstract void reopenProjectsFromNewLocation(java.io.File[],java.io.File[])

CLSS public abstract interface org.netbeans.modules.team.ide.spi.SettingsServices
fld public final static java.lang.String ODCS_SETTINGS_ID = "Odcs"
fld public final static java.lang.String TASKS_SETTINGS_ID = "Tasks"
fld public final static java.lang.String TEAM_SETTINGS_LOCATION = "Team"
innr public final static !enum Section
meth public abstract boolean providesOpenSection(org.netbeans.modules.team.ide.spi.SettingsServices$Section)
meth public abstract void openSection(org.netbeans.modules.team.ide.spi.SettingsServices$Section)

CLSS public final static !enum org.netbeans.modules.team.ide.spi.SettingsServices$Section
 outer org.netbeans.modules.team.ide.spi.SettingsServices
fld public final static org.netbeans.modules.team.ide.spi.SettingsServices$Section ODCS
fld public final static org.netbeans.modules.team.ide.spi.SettingsServices$Section PROXY
fld public final static org.netbeans.modules.team.ide.spi.SettingsServices$Section TASKS
meth public static org.netbeans.modules.team.ide.spi.SettingsServices$Section valueOf(java.lang.String)
meth public static org.netbeans.modules.team.ide.spi.SettingsServices$Section[] values()
supr java.lang.Enum<org.netbeans.modules.team.ide.spi.SettingsServices$Section>

CLSS public abstract interface org.netbeans.modules.team.ide.spi.TeamDashboardComponentProvider
innr public abstract interface static Section
meth public abstract !varargs javax.swing.JComponent create(org.netbeans.modules.team.ide.spi.TeamDashboardComponentProvider$Section[])
meth public abstract javax.swing.JComponent createNoProjectComponent(javax.swing.Action)

CLSS public abstract interface static org.netbeans.modules.team.ide.spi.TeamDashboardComponentProvider$Section
 outer org.netbeans.modules.team.ide.spi.TeamDashboardComponentProvider
meth public abstract boolean isExpanded()
meth public abstract java.lang.String getDisplayName()
meth public abstract javax.swing.JComponent getComponent()
meth public abstract void setExpanded(boolean)

CLSS public abstract interface org.netbeans.modules.team.spi.NBRepositoryProvider<%0 extends java.lang.Object, %1 extends java.lang.Object>
meth public abstract void setIssueOwnerInfo({org.netbeans.modules.team.spi.NBRepositoryProvider%1},org.netbeans.modules.team.spi.OwnerInfo)
meth public abstract void setQueryOwnerInfo({org.netbeans.modules.team.spi.NBRepositoryProvider%0},org.netbeans.modules.team.spi.OwnerInfo)

CLSS public abstract org.netbeans.modules.team.spi.OwnerInfo
cons public init()
meth public abstract java.lang.String getOwner()
meth public abstract java.util.List<java.lang.String> getExtraData()
supr java.lang.Object

CLSS public org.netbeans.modules.team.spi.RepositoryUser
cons public init(java.lang.String,java.lang.String)
meth public java.lang.String getFullName()
meth public java.lang.String getUserName()
supr java.lang.Object
hfds fullName,userName

CLSS public final org.netbeans.modules.team.spi.RepositoryUserRenderer
cons public init()
meth public java.awt.Component getListCellRendererComponent(javax.swing.JList,java.lang.Object,int,boolean,boolean)
supr javax.swing.DefaultListCellRenderer
hfds pattern

CLSS public abstract org.netbeans.modules.team.spi.TeamAccessor
cons protected init()
fld public final static java.lang.String PROP_LOGIN = "team.login.changed"
fld public final static java.lang.String PROP_PROJETCS_CHANGED = "team.projects.changed"
meth public abstract !varargs void logTeamUsage(java.lang.Object[])
meth public abstract boolean isLoggedIn(java.lang.String)
meth public abstract boolean isNBTeamServerRegistered()
meth public abstract boolean isOwner(java.lang.String)
meth public abstract boolean showLogin()
meth public abstract java.net.PasswordAuthentication getPasswordAuthentication(java.lang.String,boolean)
meth public abstract java.util.Collection<org.netbeans.modules.team.spi.RepositoryUser> getProjectMembers(org.netbeans.modules.team.spi.TeamProject) throws java.io.IOException
meth public abstract javax.swing.JLabel createUserWidget(java.lang.String,java.lang.String,java.lang.String)
meth public abstract org.netbeans.modules.team.spi.OwnerInfo getOwnerInfo(java.io.File)
meth public abstract org.netbeans.modules.team.spi.OwnerInfo getOwnerInfo(org.openide.nodes.Node)
meth public abstract org.netbeans.modules.team.spi.TeamProject getTeamProject(java.lang.String,java.lang.String) throws java.io.IOException
meth public abstract org.netbeans.modules.team.spi.TeamProject getTeamProjectForRepository(java.lang.String) throws java.io.IOException
meth public abstract org.netbeans.modules.team.spi.TeamProject[] getDashboardProjects(boolean)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener,java.lang.String)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener,java.lang.String)
supr java.lang.Object

CLSS public final org.netbeans.modules.team.spi.TeamAccessorUtils
cons public init()
fld public final static java.lang.String ALL_ISSUES_QUERY_DISPLAY_NAME
fld public final static java.lang.String MINE_ISSUES_QUERY_DISPLAY_NAME
fld public final static java.lang.String MY_ISSUES_QUERY_DISPLAY_NAME
fld public final static java.lang.String OPEN_ISSUES_QUERY_DISPLAY_NAME
fld public final static java.lang.String RECENT_ISSUES_QUERY_DISPLAY_NAME
fld public final static java.lang.String RELATED_ISSUES_QUERY_DISPLAY_NAME
meth public !varargs static void logTeamUsage(java.lang.String,java.lang.Object[])
meth public static boolean isLoggedIn(java.lang.String)
meth public static boolean isLoggedIn(java.net.URL)
meth public static boolean showLogin(java.lang.String)
meth public static java.lang.String getChatLink(java.lang.String)
meth public static java.net.PasswordAuthentication getPasswordAuthentication(java.lang.String,boolean)
meth public static java.util.Collection<org.netbeans.modules.team.spi.RepositoryUser> getProjectMembers(org.netbeans.modules.team.spi.TeamProject)
meth public static javax.swing.JLabel createUserWidget(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public static org.netbeans.modules.team.spi.OwnerInfo getOwnerInfo(java.io.File)
meth public static org.netbeans.modules.team.spi.OwnerInfo getOwnerInfo(org.openide.nodes.Node)
meth public static org.netbeans.modules.team.spi.TeamAccessor getTeamAccessor(java.lang.String)
meth public static org.netbeans.modules.team.spi.TeamAccessor[] getTeamAccessors()
meth public static org.netbeans.modules.team.spi.TeamProject getTeamProject(java.lang.String,java.lang.String)
meth public static org.netbeans.modules.team.spi.TeamProject getTeamProjectForRepository(java.lang.String) throws java.io.IOException
meth public static org.netbeans.modules.team.spi.TeamProject getTeamProjectForRepository(java.lang.String,boolean) throws java.io.IOException
meth public static org.netbeans.modules.team.spi.TeamProject[] getDashboardProjects(boolean)
supr java.lang.Object
hfds teamAccessors

CLSS public abstract interface org.netbeans.modules.team.spi.TeamBugtrackingConnector
fld public final static java.lang.String TEAM_PROJECT_NAME = "team.project.name"
innr public final static !enum BugtrackingType
meth public abstract java.lang.String findNBRepository()
meth public abstract org.netbeans.modules.team.spi.TeamBugtrackingConnector$BugtrackingType getType()

CLSS public final static !enum org.netbeans.modules.team.spi.TeamBugtrackingConnector$BugtrackingType
 outer org.netbeans.modules.team.spi.TeamBugtrackingConnector
fld public final static org.netbeans.modules.team.spi.TeamBugtrackingConnector$BugtrackingType BUGZILLA
fld public final static org.netbeans.modules.team.spi.TeamBugtrackingConnector$BugtrackingType JIRA
fld public final static org.netbeans.modules.team.spi.TeamBugtrackingConnector$BugtrackingType ODCS
meth public static org.netbeans.modules.team.spi.TeamBugtrackingConnector$BugtrackingType valueOf(java.lang.String)
meth public static org.netbeans.modules.team.spi.TeamBugtrackingConnector$BugtrackingType[] values()
supr java.lang.Enum<org.netbeans.modules.team.spi.TeamBugtrackingConnector$BugtrackingType>

CLSS public abstract org.netbeans.modules.team.spi.TeamProject
cons public init()
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getFeatureLocation()
meth public abstract java.lang.String getHost()
meth public abstract java.lang.String getName()
meth public abstract java.net.URL getWebLocation()
meth public abstract org.netbeans.modules.team.spi.TeamBugtrackingConnector$BugtrackingType getType()
supr java.lang.Object

