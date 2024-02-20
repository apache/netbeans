#Signature file v4.1
#Version 1.61.0

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

CLSS public abstract interface java.beans.VetoableChangeListener
intf java.util.EventListener
meth public abstract void vetoableChange(java.beans.PropertyChangeEvent) throws java.beans.PropertyVetoException

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

CLSS public abstract interface java.lang.annotation.Annotation
meth public abstract boolean equals(java.lang.Object)
meth public abstract int hashCode()
meth public abstract java.lang.Class<? extends java.lang.annotation.Annotation> annotationType()
meth public abstract java.lang.String toString()

CLSS public abstract interface !annotation java.lang.annotation.Documented
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation java.lang.annotation.Retention
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.annotation.RetentionPolicy value()

CLSS public abstract interface !annotation java.lang.annotation.Target
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.annotation.ElementType[] value()

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

CLSS public javax.swing.JScrollPane
cons public init()
cons public init(int,int)
cons public init(java.awt.Component)
cons public init(java.awt.Component,int,int)
fld protected int horizontalScrollBarPolicy
fld protected int verticalScrollBarPolicy
fld protected java.awt.Component lowerLeft
fld protected java.awt.Component lowerRight
fld protected java.awt.Component upperLeft
fld protected java.awt.Component upperRight
fld protected javax.swing.JScrollBar horizontalScrollBar
fld protected javax.swing.JScrollBar verticalScrollBar
fld protected javax.swing.JViewport columnHeader
fld protected javax.swing.JViewport rowHeader
fld protected javax.swing.JViewport viewport
innr protected AccessibleJScrollPane
innr protected ScrollBar
intf javax.accessibility.Accessible
intf javax.swing.ScrollPaneConstants
meth protected java.lang.String paramString()
meth protected javax.swing.JViewport createViewport()
meth public boolean isValidateRoot()
meth public boolean isWheelScrollingEnabled()
meth public int getHorizontalScrollBarPolicy()
meth public int getVerticalScrollBarPolicy()
meth public java.awt.Component getCorner(java.lang.String)
meth public java.awt.Rectangle getViewportBorderBounds()
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.JScrollBar createHorizontalScrollBar()
meth public javax.swing.JScrollBar createVerticalScrollBar()
meth public javax.swing.JScrollBar getHorizontalScrollBar()
meth public javax.swing.JScrollBar getVerticalScrollBar()
meth public javax.swing.JViewport getColumnHeader()
meth public javax.swing.JViewport getRowHeader()
meth public javax.swing.JViewport getViewport()
meth public javax.swing.border.Border getViewportBorder()
meth public javax.swing.plaf.ScrollPaneUI getUI()
meth public void setColumnHeader(javax.swing.JViewport)
meth public void setColumnHeaderView(java.awt.Component)
meth public void setComponentOrientation(java.awt.ComponentOrientation)
meth public void setCorner(java.lang.String,java.awt.Component)
meth public void setHorizontalScrollBar(javax.swing.JScrollBar)
meth public void setHorizontalScrollBarPolicy(int)
meth public void setLayout(java.awt.LayoutManager)
meth public void setRowHeader(javax.swing.JViewport)
meth public void setRowHeaderView(java.awt.Component)
meth public void setUI(javax.swing.plaf.ScrollPaneUI)
meth public void setVerticalScrollBar(javax.swing.JScrollBar)
meth public void setVerticalScrollBarPolicy(int)
meth public void setViewport(javax.swing.JViewport)
meth public void setViewportBorder(javax.swing.border.Border)
meth public void setViewportView(java.awt.Component)
meth public void setWheelScrollingEnabled(boolean)
meth public void updateUI()
supr javax.swing.JComponent

CLSS public abstract interface javax.swing.ScrollPaneConstants
fld public final static int HORIZONTAL_SCROLLBAR_ALWAYS = 32
fld public final static int HORIZONTAL_SCROLLBAR_AS_NEEDED = 30
fld public final static int HORIZONTAL_SCROLLBAR_NEVER = 31
fld public final static int VERTICAL_SCROLLBAR_ALWAYS = 22
fld public final static int VERTICAL_SCROLLBAR_AS_NEEDED = 20
fld public final static int VERTICAL_SCROLLBAR_NEVER = 21
fld public final static java.lang.String COLUMN_HEADER = "COLUMN_HEADER"
fld public final static java.lang.String HORIZONTAL_SCROLLBAR = "HORIZONTAL_SCROLLBAR"
fld public final static java.lang.String HORIZONTAL_SCROLLBAR_POLICY = "HORIZONTAL_SCROLLBAR_POLICY"
fld public final static java.lang.String LOWER_LEADING_CORNER = "LOWER_LEADING_CORNER"
fld public final static java.lang.String LOWER_LEFT_CORNER = "LOWER_LEFT_CORNER"
fld public final static java.lang.String LOWER_RIGHT_CORNER = "LOWER_RIGHT_CORNER"
fld public final static java.lang.String LOWER_TRAILING_CORNER = "LOWER_TRAILING_CORNER"
fld public final static java.lang.String ROW_HEADER = "ROW_HEADER"
fld public final static java.lang.String UPPER_LEADING_CORNER = "UPPER_LEADING_CORNER"
fld public final static java.lang.String UPPER_LEFT_CORNER = "UPPER_LEFT_CORNER"
fld public final static java.lang.String UPPER_RIGHT_CORNER = "UPPER_RIGHT_CORNER"
fld public final static java.lang.String UPPER_TRAILING_CORNER = "UPPER_TRAILING_CORNER"
fld public final static java.lang.String VERTICAL_SCROLLBAR = "VERTICAL_SCROLLBAR"
fld public final static java.lang.String VERTICAL_SCROLLBAR_POLICY = "VERTICAL_SCROLLBAR_POLICY"
fld public final static java.lang.String VIEWPORT = "VIEWPORT"

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

CLSS public abstract interface org.netbeans.api.actions.Editable
meth public abstract void edit()

CLSS public abstract interface org.netbeans.api.actions.Openable
meth public abstract void open()

CLSS public abstract interface org.netbeans.api.actions.Printable
meth public abstract void print()

CLSS public abstract interface org.netbeans.core.spi.multiview.CloseOperationHandler
meth public abstract boolean resolveCloseOperation(org.netbeans.core.spi.multiview.CloseOperationState[])

CLSS public abstract interface org.netbeans.core.spi.multiview.MultiViewDescription
 anno 0 org.netbeans.spi.editor.mimelookup.MimeLocation(java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass=class org.netbeans.spi.editor.mimelookup.InstanceProvider, java.lang.String subfolderName="MultiView")
meth public abstract int getPersistenceType()
meth public abstract java.awt.Image getIcon()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String preferredID()
meth public abstract org.netbeans.core.spi.multiview.MultiViewElement createElement()
meth public abstract org.openide.util.HelpCtx getHelpCtx()

CLSS public abstract interface org.netbeans.core.spi.multiview.MultiViewElement
innr public abstract interface static !annotation Registration
meth public abstract javax.swing.Action[] getActions()
meth public abstract javax.swing.JComponent getToolbarRepresentation()
meth public abstract javax.swing.JComponent getVisualRepresentation()
meth public abstract org.netbeans.core.spi.multiview.CloseOperationState canCloseElement()
meth public abstract org.openide.awt.UndoRedo getUndoRedo()
meth public abstract org.openide.util.Lookup getLookup()
meth public abstract void componentActivated()
meth public abstract void componentClosed()
meth public abstract void componentDeactivated()
meth public abstract void componentHidden()
meth public abstract void componentOpened()
meth public abstract void componentShowing()
meth public abstract void setMultiViewCallback(org.netbeans.core.spi.multiview.MultiViewElementCallback)

CLSS public abstract org.netbeans.modules.xml.multiview.AbstractMultiViewElement
cons protected init()
cons protected init(org.netbeans.modules.xml.multiview.XmlMultiViewDataObject)
fld protected org.netbeans.core.spi.multiview.MultiViewElementCallback callback
fld protected org.netbeans.modules.xml.multiview.XmlMultiViewDataObject dObj
intf java.io.Serializable
intf org.netbeans.core.spi.multiview.MultiViewElement
meth public javax.swing.Action[] getActions()
meth public org.netbeans.core.spi.multiview.CloseOperationState canCloseElement()
meth public org.openide.awt.UndoRedo getUndoRedo()
meth public void componentClosed()
meth public void componentOpened()
meth public void setMultiViewCallback(org.netbeans.core.spi.multiview.MultiViewElementCallback)
supr java.lang.Object
hfds LOGGER,serialVersionUID
hcls DiscardAction,SaveAction

CLSS public abstract org.netbeans.modules.xml.multiview.DesignMultiViewDesc
cons public init()
cons public init(org.netbeans.modules.xml.multiview.XmlMultiViewDataObject,java.lang.String)
intf java.io.Serializable
intf org.netbeans.core.spi.multiview.MultiViewDescription
meth protected org.netbeans.modules.xml.multiview.XmlMultiViewDataObject getDataObject()
meth public abstract java.awt.Image getIcon()
meth public abstract java.lang.String preferredID()
meth public abstract org.netbeans.core.spi.multiview.MultiViewElement createElement()
meth public int getPersistenceType()
meth public java.lang.String getDisplayName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr java.lang.Object
hfds dObj,name,serialVersionUID

CLSS public org.netbeans.modules.xml.multiview.EncodingHelper
cons public init()
fld public final static java.lang.String DEFAULT_ENCODING = "UTF-8"
meth public boolean isValidEncoding(java.lang.String)
meth public java.lang.String detectEncoding(byte[]) throws java.io.IOException
meth public java.lang.String detectEncoding(java.io.InputStream) throws java.io.IOException
meth public java.lang.String detectEncoding(javax.swing.text.StyledDocument) throws java.io.IOException
meth public java.lang.String getEncoding()
meth public java.lang.String setDefaultEncoding(java.lang.String)
meth public java.lang.String setEncoding(java.lang.String)
meth public void resetEncoding()
supr java.lang.Object
hfds encoding

CLSS public org.netbeans.modules.xml.multiview.Error
cons public init(int,int,java.lang.String,javax.swing.JComponent)
cons public init(int,int,java.lang.String,org.netbeans.modules.xml.multiview.Error$ErrorLocation)
cons public init(int,java.lang.String,javax.swing.JComponent)
cons public init(int,java.lang.String,org.netbeans.modules.xml.multiview.Error$ErrorLocation)
fld public final static int DUPLICATE_VALUE_MESSAGE = 3
fld public final static int ERROR_MESSAGE = 0
fld public final static int MISSING_VALUE_MESSAGE = 2
fld public final static int TYPE_FATAL = 0
fld public final static int TYPE_WARNING = 1
fld public final static int WARNING_MESSAGE = 1
innr public static ErrorLocation
meth public boolean isEditError()
meth public int getErrorType()
meth public int getSeverityLevel()
meth public java.lang.String getErrorMessage()
meth public javax.swing.JComponent getFocusableComponent()
meth public org.netbeans.modules.xml.multiview.Error$ErrorLocation getErrorLocation()
supr java.lang.Object
hfds errorLocation,errorMessage,errorType,focusableComponent,severityLevel

CLSS public static org.netbeans.modules.xml.multiview.Error$ErrorLocation
 outer org.netbeans.modules.xml.multiview.Error
cons public init(java.lang.Object,java.lang.String)
meth public java.lang.Object getKey()
meth public java.lang.String getComponentId()
supr java.lang.Object
hfds componentId,key

CLSS public abstract org.netbeans.modules.xml.multiview.ItemCheckBoxHelper
cons public init(org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer,javax.swing.JCheckBox)
intf java.awt.event.ActionListener
intf org.netbeans.modules.xml.multiview.Refreshable
meth public abstract boolean getItemValue()
meth public abstract void setItemValue(boolean)
meth public boolean getValue()
meth public final void actionPerformed(java.awt.event.ActionEvent)
meth public javax.swing.JCheckBox getCheckBox()
meth public void refresh()
meth public void setValue(boolean)
supr java.lang.Object
hfds checkBox,synchronizer

CLSS public abstract org.netbeans.modules.xml.multiview.ItemComboBoxHelper
cons public init(org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer,javax.swing.JComboBox)
intf java.awt.event.ActionListener
intf org.netbeans.modules.xml.multiview.Refreshable
meth public abstract java.lang.String getItemValue()
meth public abstract void setItemValue(java.lang.String)
meth public final void actionPerformed(java.awt.event.ActionEvent)
meth public java.lang.String getValue()
meth public javax.swing.JComboBox getComboBox()
meth public void refresh()
meth public void setValue(java.lang.String)
supr java.lang.Object
hfds comboBox,synchronizer

CLSS public org.netbeans.modules.xml.multiview.ItemEditorHelper
cons public init(javax.swing.text.JTextComponent)
cons public init(javax.swing.text.JTextComponent,org.netbeans.modules.xml.multiview.ItemEditorHelper$ItemEditorModel)
innr public abstract static ItemEditorModel
intf org.netbeans.modules.xml.multiview.Refreshable
meth public java.lang.String getEditorText()
meth public org.netbeans.modules.xml.multiview.ItemEditorHelper$ItemEditorModel getModel()
meth public void refresh()
supr java.lang.Object
hfds doc,editorComponent,model
hcls ItemDocument

CLSS public abstract static org.netbeans.modules.xml.multiview.ItemEditorHelper$ItemEditorModel
 outer org.netbeans.modules.xml.multiview.ItemEditorHelper
cons public init()
meth public abstract boolean setItemValue(java.lang.String)
meth public abstract java.lang.String getItemValue()
meth public abstract void documentUpdated()
meth public final java.lang.String getEditorText()
meth public final javax.swing.text.JTextComponent getEditorComponent()
supr java.lang.Object
hfds itemEditorHelper

CLSS public abstract org.netbeans.modules.xml.multiview.ItemOptionHelper
cons public init(org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer,javax.swing.ButtonGroup)
intf java.awt.event.ActionListener
intf org.netbeans.modules.xml.multiview.Refreshable
meth public abstract java.lang.String getItemValue()
meth public abstract void setItemValue(java.lang.String)
meth public final void actionPerformed(java.awt.event.ActionEvent)
meth public java.lang.String getOption()
meth public void refresh()
meth public void setOption(java.lang.String)
supr java.lang.Object
hfds buttons,synchronizer,unmatchedOption

CLSS public abstract interface org.netbeans.modules.xml.multiview.Refreshable
fld public final static java.lang.String PROPERTY_FIXED_VALUE = "prop_fixed_value"
meth public abstract void refresh()

CLSS public org.netbeans.modules.xml.multiview.SectionNode
cons protected init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,org.openide.nodes.Children,java.lang.Object,java.lang.String,java.lang.String)
fld protected boolean helpProvider
fld protected final java.lang.Object key
meth protected org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel createNodeInnerPanel()
meth protected org.netbeans.modules.xml.multiview.ui.SectionNodePanel createSectionNodePanel()
meth public boolean canDestroy()
meth public boolean equals(java.lang.Object)
meth public boolean isExpanded()
meth public final void dataModelPropertyChange(java.lang.Object,java.lang.String,java.lang.Object,java.lang.Object)
meth public int hashCode()
meth public java.lang.Object getKey()
meth public java.lang.String getIconBase()
meth public org.netbeans.modules.xml.multiview.SectionNode getNodeForElement(java.lang.Object)
meth public org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel createInnerPanel()
meth public org.netbeans.modules.xml.multiview.ui.SectionNodePanel getSectionNodePanel()
meth public org.netbeans.modules.xml.multiview.ui.SectionNodeView getSectionNodeView()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void addChild(org.netbeans.modules.xml.multiview.SectionNode)
meth public void populateBoxPanel()
meth public void populateBoxPanel(org.netbeans.modules.xml.multiview.ui.BoxPanel)
meth public void refreshSubtree()
meth public void setExpanded(boolean)
supr org.openide.nodes.AbstractNode
hfds expanded,iconBase,sectionNodeView,sectionPanel

CLSS public abstract org.netbeans.modules.xml.multiview.ToolBarMultiViewElement
cons public init(org.netbeans.modules.xml.multiview.XmlMultiViewDataObject)
meth protected void setVisualEditor(org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor)
meth public abstract org.netbeans.modules.xml.multiview.ui.SectionView getSectionView()
meth public javax.swing.JComponent getToolbarRepresentation()
meth public javax.swing.JComponent getVisualRepresentation()
meth public org.netbeans.core.spi.multiview.CloseOperationState canCloseElement()
meth public org.openide.util.Lookup getLookup()
meth public void componentActivated()
meth public void componentClosed()
meth public void componentDeactivated()
meth public void componentHidden()
meth public void componentOpened()
meth public void componentShowing()
supr org.netbeans.modules.xml.multiview.AbstractMultiViewElement
hfds editor,listener

CLSS public abstract org.netbeans.modules.xml.multiview.TreePanelMultiViewElement
cons public init(org.netbeans.modules.xml.multiview.XmlMultiViewDataObject)
meth protected void setVisualEditor(org.netbeans.modules.xml.multiview.ui.TreePanelDesignEditor)
meth public javax.swing.JComponent getToolbarRepresentation()
meth public javax.swing.JComponent getVisualRepresentation()
meth public org.netbeans.core.spi.multiview.CloseOperationState canCloseElement()
meth public org.openide.util.Lookup getLookup()
meth public void componentActivated()
meth public void componentClosed()
meth public void componentDeactivated()
meth public void componentHidden()
meth public void componentOpened()
meth public void componentShowing()
supr org.netbeans.modules.xml.multiview.AbstractMultiViewElement
hfds editor,listener

CLSS public org.netbeans.modules.xml.multiview.Utils
cons public init()
meth public static boolean replaceDocument(javax.swing.text.StyledDocument,java.lang.String)
meth public static void focusNextComponent(java.awt.Component)
meth public static void makeTextAreaLikeTextField(javax.swing.JTextArea,javax.swing.JTextField)
meth public static void runInAwtDispatchThread(java.lang.Runnable)
meth public static void scrollToVisible(javax.swing.JComponent)
meth public static void waitFinished(org.openide.util.RequestProcessor$Task)
supr java.lang.Object
hfds WAIT_FINISHED_TIMEOUT

CLSS public abstract org.netbeans.modules.xml.multiview.XmlMultiViewDataObject
cons public init(org.openide.filesystems.FileObject,org.openide.loaders.MultiFileLoader) throws org.openide.loaders.DataObjectExistsException
fld protected org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport editorSupport
fld public final static java.lang.String PROPERTY_DATA_MODIFIED = "data modified"
fld public final static java.lang.String PROPERTY_DATA_UPDATED = "data changed"
fld public final static java.lang.String PROP_DOCUMENT_VALID = "document_valid"
fld public final static java.lang.String PROP_SAX_ERROR = "sax_error"
innr public DataCache
intf org.openide.nodes.CookieSet$Factory
meth protected abstract java.lang.String getPrefixMark()
meth protected boolean verifyDocumentBeforeClose()
meth protected int getXMLMultiViewIndex()
meth protected java.awt.Image getXmlViewIcon()
meth protected java.lang.String getEditorMimeType()
meth protected org.netbeans.core.spi.multiview.MultiViewElement getActiveMultiViewElement()
meth protected org.netbeans.modules.xml.multiview.DesignMultiViewDesc[] getMultiViewDesc()
meth protected org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport getEditorSupport()
meth protected org.openide.cookies.EditorCookie createEditorCookie()
meth protected org.openide.filesystems.FileObject handleRename(java.lang.String) throws java.io.IOException
meth protected void setSaxError(org.xml.sax.SAXException)
meth public boolean canClose()
meth public org.netbeans.core.api.multiview.MultiViewPerspective getSelectedPerspective()
meth public org.netbeans.modules.xml.multiview.EncodingHelper getEncodingHelper()
meth public org.netbeans.modules.xml.multiview.XmlMultiViewDataObject$DataCache getDataCache()
meth public org.openide.filesystems.FileLock waitForLock() throws java.io.IOException
meth public org.openide.filesystems.FileLock waitForLock(long) throws java.io.IOException
meth public org.openide.nodes.Node$Cookie createCookie(java.lang.Class)
meth public org.openide.util.Lookup getLookup()
meth public org.xml.sax.SAXException getSaxError()
meth public void goToXmlView()
meth public void openView(int)
meth public void setLastOpenView(int)
meth public void setModified(boolean)
meth public void showElement(java.lang.Object)
supr org.openide.loaders.MultiDataObject
hfds activeMVElement,dataCache,encodingHelper,lockReference,saveCookie,saxError,timeStamp

CLSS public org.netbeans.modules.xml.multiview.XmlMultiViewDataObject$DataCache
 outer org.netbeans.modules.xml.multiview.XmlMultiViewDataObject
cons public init(org.netbeans.modules.xml.multiview.XmlMultiViewDataObject)
meth public byte[] getData()
meth public java.io.InputStream createInputStream()
meth public java.io.OutputStream createOutputStream() throws java.io.IOException
meth public java.io.OutputStream createOutputStream(org.openide.filesystems.FileLock,boolean) throws java.io.IOException
meth public java.io.Reader createReader() throws java.io.IOException
meth public java.io.Writer createWriter() throws java.io.IOException
meth public java.io.Writer createWriter(org.openide.filesystems.FileLock,boolean) throws java.io.IOException
meth public java.lang.String getStringData()
meth public long getTimeStamp()
meth public org.openide.filesystems.FileLock lock() throws java.io.IOException
meth public void loadData()
meth public void loadData(org.openide.filesystems.FileObject,org.openide.filesystems.FileLock) throws java.io.IOException
meth public void reloadData() throws java.io.IOException
meth public void resetFileTime()
meth public void saveData(org.openide.filesystems.FileLock)
meth public void setData(org.openide.filesystems.FileLock,byte[],boolean) throws java.io.IOException
meth public void setData(org.openide.filesystems.FileLock,java.lang.String,boolean) throws java.io.IOException
meth public void testLock(org.openide.filesystems.FileLock) throws java.io.IOException
supr java.lang.Object
hfds buffer,fileTime

CLSS public abstract org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer
cons public init(org.netbeans.modules.xml.multiview.XmlMultiViewDataObject,int)
fld protected final org.openide.util.RequestProcessor requestProcessor
innr public Transaction
meth protected abstract boolean mayUpdateData(boolean)
meth protected abstract java.lang.Object getModel()
meth protected abstract void reloadModelFromData()
meth protected abstract void updateDataFromModel(java.lang.Object,org.openide.filesystems.FileLock,boolean)
meth protected void dataModified(long)
meth protected void dataUpdated(long)
meth protected void reloadModel()
meth public final org.openide.util.RequestProcessor$Task getReloadTask()
meth public final void requestUpdateData()
meth public org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer$Transaction openTransaction()
meth public org.openide.filesystems.FileLock takeLock() throws java.io.IOException
meth public void updateData(org.openide.filesystems.FileLock,boolean)
supr java.lang.Object
hfds dataCache,dataObject,finishUpdateTask,reloadTask,reloading,timeStamp,updateDelay,updateLock,updateTask,updating

CLSS public org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer$Transaction
 outer org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer
meth public void commit() throws java.io.IOException
meth public void rollback()
supr java.lang.Object
hfds lock

CLSS public org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport
cons public init()
cons public init(org.netbeans.modules.xml.multiview.XmlMultiViewDataObject)
innr public static MyCloseHandler
innr public static XmlEnv
intf java.io.Serializable
intf org.openide.cookies.EditCookie
intf org.openide.cookies.EditorCookie$Observable
intf org.openide.cookies.OpenCookie
intf org.openide.cookies.PrintCookie
meth protected boolean asynchronousOpen()
meth protected java.lang.String messageHtmlName()
meth protected java.lang.String messageName()
meth protected java.lang.String messageSave()
meth protected java.lang.String messageToolTip()
meth protected org.openide.text.CloneableEditor createCloneableEditor()
meth protected org.openide.util.Task reloadDocument()
meth protected org.openide.windows.CloneableTopComponent createCloneableTopComponent()
meth protected void loadFromStreamToKit(javax.swing.text.StyledDocument,java.io.InputStream,javax.swing.text.EditorKit) throws java.io.IOException,javax.swing.text.BadLocationException
meth protected void notifyClosed()
meth protected void saveFromKitToStream(javax.swing.text.StyledDocument,javax.swing.text.EditorKit,java.io.OutputStream) throws java.io.IOException,javax.swing.text.BadLocationException
meth public java.io.InputStream getInputStream() throws java.io.IOException
meth public javax.swing.text.StyledDocument openDocument() throws java.io.IOException
meth public org.netbeans.core.spi.multiview.MultiViewDescription[] getMultiViewDescriptions()
meth public org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport$XmlEnv getXmlEnv()
meth public org.openide.windows.TopComponent getMVTC()
meth public void edit()
meth public void open()
meth public void saveDocument() throws java.io.IOException
meth public void setSuppressXmlView(boolean)
meth public void updateDisplayName()
supr org.openide.text.DataEditorSupport
hfds PROPERTY_MODIFICATION_LISTENER,dObj,docListener,documentSynchronizer,lastOpenView,loading,multiViewDescriptions,mvtc,saveLock,suppressXmlView,topComponentsListener,xmlMultiViewIndex
hcls DocumentSynchronizer,TopComponentsListener,XmlCloneableEditor,XmlViewDesc

CLSS public static org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport$MyCloseHandler
 outer org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport
cons public init(org.netbeans.modules.xml.multiview.XmlMultiViewDataObject)
intf java.io.Serializable
intf org.netbeans.core.spi.multiview.CloseOperationHandler
meth public boolean resolveCloseOperation(org.netbeans.core.spi.multiview.CloseOperationState[])
supr java.lang.Object
hfds dObj,serialVersionUID

CLSS public static org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport$XmlEnv
 outer org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport
cons public init(org.netbeans.modules.xml.multiview.XmlMultiViewDataObject)
meth protected java.io.OutputStream getFileOutputStream() throws java.io.IOException
meth protected org.openide.filesystems.FileLock takeLock() throws java.io.IOException
meth protected org.openide.filesystems.FileObject getFile()
meth public boolean isModified()
meth public java.io.InputStream inputStream() throws java.io.IOException
meth public java.io.OutputStream outputStream() throws java.io.IOException
meth public org.openide.windows.CloneableOpenSupport findCloneableOpenSupport()
supr org.openide.text.DataEditorSupport$Env
hfds serialVersionUID,xmlMultiViewDataObject

CLSS public org.netbeans.modules.xml.multiview.XmlMultiViewElement
cons public init()
cons public init(org.netbeans.modules.xml.multiview.XmlMultiViewDataObject)
intf java.io.Serializable
meth public javax.swing.JComponent getToolbarRepresentation()
meth public javax.swing.JComponent getVisualRepresentation()
meth public org.openide.util.Lookup getLookup()
meth public void componentActivated()
meth public void componentClosed()
meth public void componentDeactivated()
meth public void componentHidden()
meth public void componentOpened()
meth public void componentShowing()
supr org.netbeans.modules.xml.multiview.AbstractMultiViewElement
hfds INIT_RP,initializer,serialVersionUID,toolbar,xmlEditor
hcls ToolbarInitializer

CLSS public abstract interface org.netbeans.modules.xml.multiview.cookies.ErrorLocator
meth public abstract javax.swing.JComponent getErrorComponent(java.lang.String)

CLSS public abstract interface org.netbeans.modules.xml.multiview.cookies.LinkCookie
meth public abstract void linkButtonPressed(java.lang.Object,java.lang.String)

CLSS public abstract interface org.netbeans.modules.xml.multiview.cookies.SectionFocusCookie
meth public abstract boolean focusSection(org.netbeans.modules.xml.multiview.ui.NodeSectionPanel)

CLSS public abstract org.netbeans.modules.xml.multiview.ui.AbstractDesignEditor
cons public init()
cons public init(org.netbeans.modules.xml.multiview.ui.PanelView)
fld protected final static long serialVersionUID = 1
fld protected javax.swing.Action helpAction
fld protected javax.swing.JComponent structureView
fld protected org.netbeans.modules.xml.multiview.ui.PanelView contentView
fld protected static java.lang.String iconURL
fld public final static java.lang.String PROPERTY_FLUSH_DATA = "Flush Data"
intf org.openide.explorer.ExplorerManager$Provider
meth public abstract javax.swing.JComponent createStructureComponent()
meth public abstract org.netbeans.modules.xml.multiview.ui.ErrorPanel getErrorPanel()
meth public javax.swing.JComponent getStructureView()
meth public org.netbeans.modules.xml.multiview.ui.PanelView getContentView()
meth public org.openide.explorer.ExplorerManager getExplorerManager()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void componentActivated()
meth public void componentClosed()
meth public void componentDeactivated()
meth public void componentHidden()
meth public void componentOpened()
meth public void componentShowing()
meth public void fireVetoableChange(java.lang.String,java.lang.Object,java.lang.Object) throws java.beans.PropertyVetoException
meth public void open()
meth public void setContentView(org.netbeans.modules.xml.multiview.ui.PanelView)
meth public void setRootContext(org.openide.nodes.Node)
supr org.openide.windows.TopComponent
hfds ACTION_INVOKE_HELP,manager
hcls HelpAction,NodeSelectedListener

CLSS public org.netbeans.modules.xml.multiview.ui.BoxPanel
cons public init(org.netbeans.modules.xml.multiview.ui.SectionNodeView)
meth protected void signalUIChange()
meth public javax.swing.JComponent getErrorComponent(java.lang.String)
meth public void linkButtonPressed(java.lang.Object,java.lang.String)
meth public void setComponents(java.awt.Component[])
meth public void setValue(javax.swing.JComponent,java.lang.Object)
supr org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel

CLSS public org.netbeans.modules.xml.multiview.ui.ConfirmDialog
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
supr org.openide.DialogDescriptor

CLSS public abstract interface org.netbeans.modules.xml.multiview.ui.ContainerPanel
meth public abstract org.netbeans.modules.xml.multiview.ui.NodeSectionPanel getSection(org.openide.nodes.Node)
meth public abstract org.openide.nodes.Node getRoot()
meth public abstract void addSection(org.netbeans.modules.xml.multiview.ui.NodeSectionPanel)
meth public abstract void removeSection(org.netbeans.modules.xml.multiview.ui.NodeSectionPanel)

CLSS public org.netbeans.modules.xml.multiview.ui.DefaultTablePanel
cons public init(javax.swing.table.AbstractTableModel)
cons public init(javax.swing.table.AbstractTableModel,boolean)
fld protected javax.swing.JButton addButton
fld protected javax.swing.JButton editButton
fld protected javax.swing.JButton moveDownButton
fld protected javax.swing.JButton moveUpButton
fld protected javax.swing.JButton removeButton
fld protected javax.swing.JButton sourceButton
meth public boolean isReordable()
meth public javax.swing.JTable getTable()
meth public javax.swing.table.AbstractTableModel getModel()
meth public void setButtons(boolean,boolean,boolean)
meth public void setButtons(boolean,boolean,boolean,boolean,boolean,boolean)
meth public void setSelectedRow(int)
meth public void setTitle(java.lang.String)
supr javax.swing.JPanel
hfds buttonPanel,jPanel1,jTable1,model,reordable

CLSS public abstract org.netbeans.modules.xml.multiview.ui.EditDialog
cons public init(javax.swing.JPanel,java.lang.String)
cons public init(javax.swing.JPanel,java.lang.String,boolean)
innr public static DocListener
meth protected abstract java.lang.String validate()
meth public final javax.swing.JPanel getDialogPanel()
meth public final void checkValues()
supr org.openide.DialogDescriptor
hfds panel,statusLine

CLSS public static org.netbeans.modules.xml.multiview.ui.EditDialog$DocListener
 outer org.netbeans.modules.xml.multiview.ui.EditDialog
cons public init(org.netbeans.modules.xml.multiview.ui.EditDialog)
intf javax.swing.event.DocumentListener
meth public void changedUpdate(javax.swing.event.DocumentEvent)
meth public void insertUpdate(javax.swing.event.DocumentEvent)
meth public void removeUpdate(javax.swing.event.DocumentEvent)
supr java.lang.Object
hfds dialog

CLSS public org.netbeans.modules.xml.multiview.ui.ErrorPanel
cons public init(org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor)
meth public java.lang.String getErrorMessage()
meth public org.netbeans.modules.xml.multiview.Error getError()
meth public void clearError()
meth public void setError(org.netbeans.modules.xml.multiview.Error)
supr javax.swing.JPanel
hfds error,errorLabel,errorMessage
hcls ErrorLabel

CLSS public abstract interface org.netbeans.modules.xml.multiview.ui.InnerPanelFactory
meth public abstract org.netbeans.modules.xml.multiview.ui.SectionInnerPanel createInnerPanel(java.lang.Object)

CLSS public org.netbeans.modules.xml.multiview.ui.LinkButton
cons public init(org.netbeans.modules.xml.multiview.cookies.LinkCookie,java.lang.Object,java.lang.String)
innr public static LinkAction
meth public static void initLinkButton(javax.swing.AbstractButton,org.netbeans.modules.xml.multiview.cookies.LinkCookie,java.lang.Object,java.lang.String)
meth public void setText(java.lang.String)
supr javax.swing.JButton

CLSS public static org.netbeans.modules.xml.multiview.ui.LinkButton$LinkAction
 outer org.netbeans.modules.xml.multiview.ui.LinkButton
cons public init(org.netbeans.modules.xml.multiview.cookies.LinkCookie,java.lang.Object,java.lang.String)
meth public void actionPerformed(java.awt.event.ActionEvent)
supr javax.swing.AbstractAction
hfds ddBean,ddProperty,panel

CLSS public abstract interface org.netbeans.modules.xml.multiview.ui.NodeSectionPanel
meth public abstract boolean isActive()
meth public abstract int getIndex()
meth public abstract org.openide.nodes.Node getNode()
meth public abstract void open()
meth public abstract void scroll()
meth public abstract void setActive(boolean)
meth public abstract void setIndex(int)

CLSS public abstract org.netbeans.modules.xml.multiview.ui.PanelView
cons public init()
meth protected abstract org.netbeans.modules.xml.multiview.Error validateView()
meth protected boolean selectionAccept(org.openide.nodes.Node[])
meth public abstract void showSelection(org.openide.nodes.Node[])
meth public boolean canClose()
meth public boolean isSectionHeaderClicked()
meth public boolean setManagerExploredContextAndSelection(org.openide.nodes.Node,org.openide.nodes.Node[])
meth public boolean setManagerSelection(org.openide.nodes.Node[])
meth public final void checkValidity()
meth public org.netbeans.modules.xml.multiview.ui.ErrorPanel getErrorPanel()
meth public org.openide.explorer.ExplorerManager getExplorerManager()
meth public org.openide.nodes.Node getRoot()
meth public void addNotify()
meth public void initComponents()
meth public void open()
meth public void setPopupAllowed(boolean)
meth public void setRoot(org.openide.nodes.Node)
meth public void setSectionHeaderClicked(boolean)
supr javax.swing.JPanel
hfds LOGGER,errorPanel,manager,popupListener,root,sectionHeaderClicked,wlpc,wlvc
hcls PopupAdapter

CLSS public org.netbeans.modules.xml.multiview.ui.RefreshDialog
cons public init(org.netbeans.modules.xml.multiview.ui.ErrorPanel)
cons public init(org.netbeans.modules.xml.multiview.ui.ErrorPanel,java.lang.String)
fld public final static java.lang.Integer OPTION_FIX
fld public final static java.lang.Integer OPTION_REFRESH
meth public java.lang.Object getValue()
supr org.openide.DialogDescriptor
hfds OPTIONS
hcls DialogListener

CLSS public org.netbeans.modules.xml.multiview.ui.RefreshSaveDialog
cons public init(org.netbeans.modules.xml.multiview.ui.ErrorPanel)
cons public init(org.netbeans.modules.xml.multiview.ui.ErrorPanel,java.lang.String)
fld public final static java.lang.Integer OPTION_FIX
fld public final static java.lang.Integer OPTION_REFRESH
fld public final static java.lang.Integer OPTION_SAVE
meth public java.lang.Object getValue()
supr org.openide.DialogDescriptor
hfds OPTIONS
hcls DialogListener

CLSS public org.netbeans.modules.xml.multiview.ui.SectionContainer
cons public init(org.netbeans.modules.xml.multiview.ui.SectionView,org.openide.nodes.Node,java.lang.String)
cons public init(org.netbeans.modules.xml.multiview.ui.SectionView,org.openide.nodes.Node,java.lang.String,boolean)
intf org.netbeans.modules.xml.multiview.ui.ContainerPanel
intf org.netbeans.modules.xml.multiview.ui.NodeSectionPanel
meth public boolean isActive()
meth public boolean isFoldable()
meth public int getIndex()
meth public javax.swing.JButton[] getHeaderButtons()
meth public org.netbeans.modules.xml.multiview.ui.NodeSectionPanel getSection(org.openide.nodes.Node)
meth public org.openide.nodes.Node getNode()
meth public org.openide.nodes.Node getRoot()
meth public void addSection(org.netbeans.modules.xml.multiview.ui.NodeSectionPanel)
meth public void addSection(org.netbeans.modules.xml.multiview.ui.NodeSectionPanel,boolean)
meth public void open()
meth public void removeSection(org.netbeans.modules.xml.multiview.ui.NodeSectionPanel)
meth public void scroll()
meth public void setActive(boolean)
meth public void setHeaderActions(javax.swing.Action[])
meth public void setIndex(int)
supr javax.swing.JPanel
hfds actionPanel,active,contentPanel,fillerEnd,fillerLine,foldButton,foldable,headerButtons,headerSeparator,index,root,sectionCount,sectionView,titleButton,titlePanel

CLSS public org.netbeans.modules.xml.multiview.ui.SectionContainerNode
cons public init(org.openide.nodes.Children)
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.nodes.AbstractNode

CLSS public abstract org.netbeans.modules.xml.multiview.ui.SectionInnerPanel
cons public init(org.netbeans.modules.xml.multiview.ui.SectionView)
intf org.netbeans.modules.xml.multiview.cookies.ErrorLocator
intf org.netbeans.modules.xml.multiview.cookies.LinkCookie
meth protected void addRefreshable(org.netbeans.modules.xml.multiview.Refreshable)
meth protected void endUIChange()
meth protected void scheduleRefreshView()
meth protected void signalUIChange()
 anno 0 java.lang.Deprecated()
meth protected void startUIChange()
meth public abstract void setValue(javax.swing.JComponent,java.lang.Object)
meth public boolean canClose()
meth public final void addImmediateModifier(javax.swing.JCheckBox)
meth public final void addImmediateModifier(javax.swing.JComboBox)
meth public final void addImmediateModifier(javax.swing.JRadioButton)
meth public final void addImmediateModifier(javax.swing.text.JTextComponent)
meth public final void addModifier(javax.swing.JCheckBox)
meth public final void addModifier(javax.swing.JComboBox)
meth public final void addModifier(javax.swing.JComboBox,boolean)
meth public final void addModifier(javax.swing.JRadioButton)
meth public final void addModifier(javax.swing.text.JTextComponent)
meth public final void addModifier(javax.swing.text.JTextComponent,boolean)
meth public final void addValidatee(javax.swing.text.JTextComponent)
meth public org.netbeans.modules.xml.multiview.ui.SectionView getSectionView()
meth public void addFocusListener(java.awt.event.FocusListener)
meth public void dataModelPropertyChange(java.lang.Object,java.lang.String,java.lang.Object,java.lang.Object)
meth public void documentChanged(javax.swing.text.JTextComponent,java.lang.String)
meth public void refreshView()
meth public void rollbackValue(javax.swing.text.JTextComponent)
supr javax.swing.JPanel
hfds REFRESH_DELAY,RP,activeListener,closing,localFocusListener,localFocusListenerInitialized,refreshTask,refreshableList,sectionView
hcls CheckBoxActionListener,CheckBoxModifyFocusListener,ComboBoxActionListener,ComboBoxModifyFocusListener,FlushActionListener,FlushFocusListener,ModifyFocusListener,RadioButtonActionListener,RadioButtonModifyFocusListener,TextListener,ValidateFocusListener

CLSS public abstract org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel
cons public init(org.netbeans.modules.xml.multiview.ui.SectionNodeView)
meth protected void signalUIChange()
meth public void focusData(java.lang.Object)
supr org.netbeans.modules.xml.multiview.ui.SectionInnerPanel

CLSS public org.netbeans.modules.xml.multiview.ui.SectionNodePanel
cons public init(org.netbeans.modules.xml.multiview.SectionNode)
cons public init(org.netbeans.modules.xml.multiview.SectionNode,boolean)
meth protected org.netbeans.modules.xml.multiview.ui.SectionInnerPanel createInnerpanel()
meth protected void closeInnerPanel()
meth protected void openInnerPanel()
meth protected void setExpandedViewMode()
meth protected void setInnerViewMode()
meth public void open()
supr org.netbeans.modules.xml.multiview.ui.SectionPanel
hfds openFirstChild

CLSS public abstract org.netbeans.modules.xml.multiview.ui.SectionNodeView
cons public init(org.netbeans.modules.xml.multiview.XmlMultiViewDataObject)
meth public abstract org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer getModelSynchronizer()
meth public org.netbeans.modules.xml.multiview.SectionNode getRootNode()
meth public org.netbeans.modules.xml.multiview.SectionNode retrieveSectionNode(org.netbeans.modules.xml.multiview.SectionNode)
meth public org.netbeans.modules.xml.multiview.XmlMultiViewDataObject getDataObject()
meth public void dataModelPropertyChange(java.lang.Object,java.lang.String,java.lang.Object,java.lang.Object)
meth public void openPanel(java.lang.Object)
meth public void openSection(org.openide.nodes.Node)
meth public void refreshView()
meth public void registerNode(org.netbeans.modules.xml.multiview.SectionNode)
meth public void scheduleRefreshView()
meth public void setRootNode(org.netbeans.modules.xml.multiview.SectionNode)
supr org.netbeans.modules.xml.multiview.ui.SectionView
hfds REFRESH_DELAY,dataObject,nodes,refreshTask,rootNode

CLSS public org.netbeans.modules.xml.multiview.ui.SectionPanel
cons public init(org.netbeans.modules.xml.multiview.ui.SectionView,org.openide.nodes.Node,java.lang.Object)
cons public init(org.netbeans.modules.xml.multiview.ui.SectionView,org.openide.nodes.Node,java.lang.Object,boolean)
cons public init(org.netbeans.modules.xml.multiview.ui.SectionView,org.openide.nodes.Node,java.lang.String,java.lang.Object)
cons public init(org.netbeans.modules.xml.multiview.ui.SectionView,org.openide.nodes.Node,java.lang.String,java.lang.Object,boolean)
cons public init(org.netbeans.modules.xml.multiview.ui.SectionView,org.openide.nodes.Node,java.lang.String,java.lang.Object,boolean,boolean)
innr public static HeaderButton
intf org.netbeans.modules.xml.multiview.cookies.ErrorLocator
intf org.netbeans.modules.xml.multiview.ui.NodeSectionPanel
meth protected javax.swing.JButton getTitleButton()
meth protected javax.swing.JComponent getFillerEnd()
meth protected javax.swing.JComponent getFillerLine()
meth protected javax.swing.JSeparator getHeaderSeparator()
meth protected javax.swing.JToggleButton getFoldButton()
meth protected org.netbeans.modules.xml.multiview.ui.SectionInnerPanel createInnerpanel()
meth protected void closeInnerPanel()
meth protected void openInnerPanel()
meth public boolean isActive()
meth public int getIndex()
meth public java.lang.Object getKey()
meth public java.lang.String getTitle()
meth public javax.swing.JComponent getErrorComponent(java.lang.String)
meth public org.netbeans.modules.xml.multiview.ui.SectionInnerPanel getInnerPanel()
meth public org.netbeans.modules.xml.multiview.ui.SectionPanel$HeaderButton[] getHeaderButtons()
meth public org.netbeans.modules.xml.multiview.ui.SectionView getSectionView()
meth public org.openide.nodes.Node getNode()
meth public void open()
meth public void scroll()
meth public void setActive(boolean)
meth public void setHeaderActions(javax.swing.Action[])
meth public void setIndex(int)
meth public void setKey(java.lang.Object)
meth public void setTitle(java.lang.String)
supr javax.swing.JPanel
hfds IMAGE_SELECTED,IMAGE_UNSELECTED,actionPanel,active,fillerEnd,fillerLine,foldButton,headerButtons,headerSeparator,index,innerPanel,key,node,sectionFocusListener,sectionView,title,titleButton,titlePanel,toolBarDesignEditor

CLSS public static org.netbeans.modules.xml.multiview.ui.SectionPanel$HeaderButton
 outer org.netbeans.modules.xml.multiview.ui.SectionPanel
cons public init(org.netbeans.modules.xml.multiview.ui.SectionPanel,javax.swing.Action)
meth public org.netbeans.modules.xml.multiview.ui.SectionPanel getSectionPanel()
supr javax.swing.JButton
hfds panel

CLSS public org.netbeans.modules.xml.multiview.ui.SectionView
cons public init()
cons public init(org.netbeans.modules.xml.multiview.ui.InnerPanelFactory)
intf org.netbeans.modules.xml.multiview.cookies.SectionFocusCookie
intf org.netbeans.modules.xml.multiview.ui.ContainerPanel
meth protected org.netbeans.modules.xml.multiview.Error validateView()
meth protected org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor getToolBarDesignEditor()
meth protected void openSection(org.openide.nodes.Node)
meth public boolean focusSection(org.netbeans.modules.xml.multiview.ui.NodeSectionPanel)
meth public org.netbeans.modules.xml.multiview.ui.NodeSectionPanel getActivePanel()
meth public org.netbeans.modules.xml.multiview.ui.NodeSectionPanel getSection(org.openide.nodes.Node)
meth public org.netbeans.modules.xml.multiview.ui.SectionPanel findSectionPanel(java.lang.Object)
meth public void addSection(org.netbeans.modules.xml.multiview.ui.NodeSectionPanel)
meth public void addSection(org.netbeans.modules.xml.multiview.ui.NodeSectionPanel,boolean)
meth public void initComponents()
meth public void openPanel(java.lang.Object)
meth public void removeSection(org.netbeans.modules.xml.multiview.ui.NodeSectionPanel)
meth public void removeSection(org.openide.nodes.Node)
meth public void selectNode(org.openide.nodes.Node)
meth public void setActivePanel(org.netbeans.modules.xml.multiview.ui.NodeSectionPanel)
meth public void setInnerPanelFactory(org.netbeans.modules.xml.multiview.ui.InnerPanelFactory)
meth public void showSelection(org.openide.nodes.Node[])
supr org.netbeans.modules.xml.multiview.ui.PanelView
hfds activePanel,factory,filler,map,scrollPane,scrollPanel,sectionCount,sectionSelected

CLSS public org.netbeans.modules.xml.multiview.ui.SectionVisualTheme
cons public init()
meth public static java.awt.Color getContainerHeaderColor()
meth public static java.awt.Color getDocumentBackgroundColor()
meth public static java.awt.Color getErrorLabelColor()
meth public static java.awt.Color getFillerColor()
meth public static java.awt.Color getFoldLineColor()
meth public static java.awt.Color getHyperlinkColor()
meth public static java.awt.Color getHyperlinkColorFocused()
meth public static java.awt.Color getMarginColor()
meth public static java.awt.Color getSectionActiveBackgroundColor()
meth public static java.awt.Color getSectionHeaderActiveColor()
meth public static java.awt.Color getSectionHeaderColor()
meth public static java.awt.Color getSectionHeaderLineColor()
meth public static java.awt.Color getTableGridColor()
meth public static java.awt.Color getTableHeaderColor()
meth public static java.awt.Color getTextColor()
supr java.lang.Object
hfds containerHeaderColor,documentBackgroundColor,documentMarginColor,errorLabelColor,fillerColor,foldLineColor,hyperlinkColor,hyperlinkColorFocused,sectionActiveBackgroundColor,sectionHeaderActiveColor,sectionHeaderColor,sectionHeaderLineColor,tableGridColor,tableHeaderColor,textColor

CLSS public org.netbeans.modules.xml.multiview.ui.SimpleDialogPanel
cons public init(org.netbeans.modules.xml.multiview.ui.SimpleDialogPanel$DialogDescriptor)
innr public static DialogDescriptor
meth public java.lang.String[] getValues()
meth public javax.swing.JButton[] getCustomizerButtons()
meth public javax.swing.text.JTextComponent[] getTextComponents()
supr javax.swing.JPanel
hfds gridBagConstraints,jButtons,jLabels,jTextComponents

CLSS public static org.netbeans.modules.xml.multiview.ui.SimpleDialogPanel$DialogDescriptor
 outer org.netbeans.modules.xml.multiview.ui.SimpleDialogPanel
cons public init(java.lang.String[])
cons public init(java.lang.String[],boolean)
meth public boolean isAdding()
meth public boolean[] getButtons()
meth public boolean[] isTextField()
meth public char[] getMnemonics()
 anno 0 java.lang.Deprecated()
meth public int getSize()
meth public java.lang.String[] getA11yDesc()
meth public java.lang.String[] getInitValues()
meth public java.lang.String[] getLabels()
meth public void setA11yDesc(java.lang.String[])
meth public void setAdding(boolean)
meth public void setButtons(boolean[])
meth public void setInitValues(java.lang.String[])
meth public void setMnemonics(char[])
 anno 0 java.lang.Deprecated()
meth public void setSize(int)
meth public void setTextField(boolean[])
supr java.lang.Object
hfds a11yDesc,adding,buttons,includesMnemonics,initValues,labels,mnem,size,textField

CLSS public org.netbeans.modules.xml.multiview.ui.StructureTreeView
cons public init()
cons public init(javax.swing.tree.TreeCellRenderer,java.lang.String)
meth public javax.swing.JTree getTree()
meth public org.openide.explorer.view.NodeTreeModel getModel()
meth public void addNotify()
supr org.openide.explorer.view.BeanTreeView

CLSS public org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor
cons public init()
cons public init(org.netbeans.modules.xml.multiview.ui.PanelView)
fld protected javax.swing.JComponent designPanel
meth public java.lang.Object getLastActive()
meth public javax.swing.JComponent createDesignPanel()
meth public javax.swing.JComponent createStructureComponent()
meth public org.netbeans.modules.xml.multiview.Error getError()
meth public org.netbeans.modules.xml.multiview.ui.ErrorPanel getErrorPanel()
meth public void setContentView(org.netbeans.modules.xml.multiview.ui.PanelView)
meth public void setLastActive(java.lang.Object)
supr org.netbeans.modules.xml.multiview.ui.AbstractDesignEditor
hfds errorPanel,lastActive
hcls ToolBarView

CLSS public abstract interface org.netbeans.modules.xml.multiview.ui.TreeNode
meth public abstract java.lang.String getPanelId()
meth public abstract org.netbeans.modules.xml.multiview.ui.TreePanel getPanel()

CLSS public abstract interface org.netbeans.modules.xml.multiview.ui.TreePanel
meth public abstract void setModel(org.netbeans.modules.xml.multiview.ui.TreeNode)

CLSS public org.netbeans.modules.xml.multiview.ui.TreePanelDesignEditor
cons public init(org.netbeans.modules.xml.multiview.ui.PanelView)
cons public init(org.netbeans.modules.xml.multiview.ui.PanelView,int)
fld protected int panelOrientation
fld protected javax.swing.JSplitPane split
fld public final static int CONTENT_LEFT = 1
fld public final static int CONTENT_RIGHT = 0
fld public final static int DEFAULT_STRUCTURE_HEIGHT = 300
fld public final static int DEFAULT_STRUCTURE_WIDTH = 170
meth protected javax.swing.JComponent createDesignPanel()
meth protected void initComponents()
meth public javax.swing.JComponent createPropertiesComponent()
meth public javax.swing.JComponent createStructureComponent()
meth public javax.swing.JComponent getStructureView()
meth public org.netbeans.modules.xml.multiview.ui.ErrorPanel getErrorPanel()
supr org.netbeans.modules.xml.multiview.ui.AbstractDesignEditor
hfds EMPTY_INSPECTOR_ICON_BASE

CLSS public org.netbeans.modules.xml.multiview.ui.TreePanelView
cons public init()
meth protected org.netbeans.modules.xml.multiview.Error validateView()
meth protected void showPanel(org.netbeans.modules.xml.multiview.ui.TreeNode)
meth public void initComponents()
meth public void showSelection(org.openide.nodes.Node[])
supr org.netbeans.modules.xml.multiview.ui.PanelView
hfds cardLayout,cardPanel,map

CLSS public abstract interface !annotation org.netbeans.spi.editor.mimelookup.MimeLocation
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass()
meth public abstract java.lang.String subfolderName()

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

CLSS public abstract interface org.openide.cookies.EditCookie
intf org.netbeans.api.actions.Editable
intf org.openide.nodes.Node$Cookie

CLSS public abstract interface org.openide.cookies.EditorCookie
innr public abstract interface static Observable
intf org.openide.cookies.LineCookie
meth public abstract boolean close()
meth public abstract boolean isModified()
meth public abstract javax.swing.JEditorPane[] getOpenedPanes()
meth public abstract javax.swing.text.StyledDocument getDocument()
meth public abstract javax.swing.text.StyledDocument openDocument() throws java.io.IOException
meth public abstract org.openide.util.Task prepareDocument()
meth public abstract void open()
meth public abstract void saveDocument() throws java.io.IOException

CLSS public abstract interface static org.openide.cookies.EditorCookie$Observable
 outer org.openide.cookies.EditorCookie
fld public final static java.lang.String PROP_DOCUMENT = "document"
fld public final static java.lang.String PROP_MODIFIED = "modified"
fld public final static java.lang.String PROP_OPENED_PANES = "openedPanes"
fld public final static java.lang.String PROP_RELOADING = "reloading"
intf org.openide.cookies.EditorCookie
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public abstract interface org.openide.cookies.LineCookie
intf org.openide.nodes.Node$Cookie
meth public abstract org.openide.text.Line$Set getLineSet()

CLSS public abstract interface org.openide.cookies.OpenCookie
intf org.netbeans.api.actions.Openable
intf org.openide.nodes.Node$Cookie

CLSS public abstract interface org.openide.cookies.PrintCookie
intf org.netbeans.api.actions.Printable
intf org.openide.nodes.Node$Cookie

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

CLSS public org.openide.explorer.view.BeanTreeView
cons public init()
meth protected boolean selectionAccept(org.openide.nodes.Node[])
meth protected org.openide.explorer.view.NodeTreeModel createModel()
meth protected void selectionChanged(org.openide.nodes.Node[],org.openide.explorer.ExplorerManager) throws java.beans.PropertyVetoException
meth protected void showPath(javax.swing.tree.TreePath)
meth protected void showSelection(javax.swing.tree.TreePath[])
meth public boolean isEnabled()
meth public void setEnabled(boolean)
supr org.openide.explorer.view.TreeView
hfds serialVersionUID

CLSS public abstract org.openide.explorer.view.TreeView
cons public init()
cons public init(boolean,boolean)
fld protected javax.swing.JTree tree
meth protected abstract boolean selectionAccept(org.openide.nodes.Node[])
meth protected abstract org.openide.explorer.view.NodeTreeModel createModel()
meth protected abstract void selectionChanged(org.openide.nodes.Node[],org.openide.explorer.ExplorerManager) throws java.beans.PropertyVetoException
meth protected abstract void showPath(javax.swing.tree.TreePath)
meth protected abstract void showSelection(javax.swing.tree.TreePath[])
meth protected boolean useExploredContextMenu()
meth public boolean getScrollsOnExpand()
meth public boolean isDefaultActionEnabled()
meth public boolean isDragSource()
meth public boolean isDropTarget()
meth public boolean isExpanded(org.openide.nodes.Node)
meth public boolean isPopupAllowed()
meth public boolean isQuickSearchAllowed()
meth public boolean isRootVisible()
meth public boolean requestFocusInWindow()
meth public int getAllowedDragActions()
meth public int getAllowedDropActions()
meth public int getSelectionMode()
meth public java.awt.Insets getInsets()
meth public void add(java.awt.Component,java.lang.Object)
meth public void addNotify()
meth public void collapseNode(org.openide.nodes.Node)
meth public void expandAll()
meth public void expandNode(org.openide.nodes.Node)
meth public void remove(java.awt.Component)
meth public void removeNotify()
meth public void requestFocus()
meth public void setAllowedDragActions(int)
meth public void setAllowedDropActions(int)
meth public void setAutoWaitCursor(boolean)
meth public void setDefaultActionAllowed(boolean)
meth public void setDragSource(boolean)
meth public void setDropTarget(boolean)
meth public void setPopupAllowed(boolean)
meth public void setQuickSearchAllowed(boolean)
meth public void setRootVisible(boolean)
meth public void setScrollsOnExpand(boolean)
meth public void setSelectionMode(int)
meth public void setUseSubstringInQuickSearch(boolean)
 anno 0 java.lang.Deprecated()
meth public void updateUI()
meth public void validate()
supr javax.swing.JScrollPane
hfds LOG,MIN_TREEVIEW_HEIGHT,MIN_TREEVIEW_WIDTH,TIME_TO_COLLAPSE,allowedDragActions,allowedDropActions,autoWaitCursor,defaultActionEnabled,defaultActionListener,dragActive,dragSupport,dropActive,dropSupport,dropTargetPopupAllowed,lastSearchField,manager,managerListener,origSelectionPaths,popupListener,qs,removedNodeWasSelected,searchConstraints,searchPanel,serialVersionUID,treeModel,visHolder,wlpc,wlvc
hcls CursorR,DummyTransferHandler,ExplorerScrollPaneLayout,ExplorerTree,PopupAdapter,PopupSupport,TreePropertyListener,VisualizerHolder

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

CLSS public abstract org.openide.loaders.OpenSupport
cons protected init(org.openide.loaders.MultiDataObject$Entry,org.openide.loaders.OpenSupport$Env)
cons public init(org.openide.loaders.MultiDataObject$Entry)
fld protected org.openide.loaders.MultiDataObject$Entry entry
innr public static Env
meth protected java.lang.String messageOpened()
meth protected java.lang.String messageOpening()
supr org.openide.windows.CloneableOpenSupport
hcls FileSystemNameListener,Listener

CLSS public static org.openide.loaders.OpenSupport$Env
 outer org.openide.loaders.OpenSupport
cons public init(org.openide.loaders.DataObject)
intf java.beans.PropertyChangeListener
intf java.beans.VetoableChangeListener
intf java.io.Serializable
intf org.openide.windows.CloneableOpenSupport$Env
meth protected final org.openide.loaders.DataObject getDataObject()
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void fireVetoableChange(java.lang.String,java.lang.Object,java.lang.Object) throws java.beans.PropertyVetoException
meth public boolean isModified()
meth public boolean isValid()
meth public java.lang.String toString()
meth public org.openide.windows.CloneableOpenSupport findCloneableOpenSupport()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addVetoableChangeListener(java.beans.VetoableChangeListener)
meth public void markModified() throws java.io.IOException
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removeVetoableChangeListener(java.beans.VetoableChangeListener)
meth public void unmarkModified()
meth public void vetoableChange(java.beans.PropertyChangeEvent) throws java.beans.PropertyVetoException
supr java.lang.Object
hfds LOCK_SUPPORT,fsListenerMap,obj,propSupp,serialVersionUID,vetoSupp

CLSS public org.openide.nodes.AbstractNode
cons public init(org.openide.nodes.Children)
cons public init(org.openide.nodes.Children,org.openide.util.Lookup)
fld protected java.text.MessageFormat displayFormat
fld protected org.openide.util.actions.SystemAction[] systemActions
 anno 0 java.lang.Deprecated()
meth protected final org.openide.nodes.CookieSet getCookieSet()
meth protected final org.openide.nodes.Sheet getSheet()
meth protected final void setCookieSet(org.openide.nodes.CookieSet)
 anno 0 java.lang.Deprecated()
meth protected final void setSheet(org.openide.nodes.Sheet)
meth protected org.openide.nodes.Sheet createSheet()
meth protected org.openide.util.actions.SystemAction[] createActions()
 anno 0 java.lang.Deprecated()
meth protected void createPasteTypes(java.awt.datatransfer.Transferable,java.util.List<org.openide.util.datatransfer.PasteType>)
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public boolean canCopy()
meth public boolean canCut()
meth public boolean canDestroy()
meth public boolean canRename()
meth public boolean hasCustomizer()
meth public final org.openide.util.datatransfer.PasteType[] getPasteTypes(java.awt.datatransfer.Transferable)
meth public final void setIconBaseWithExtension(java.lang.String)
meth public java.awt.Component getCustomizer()
meth public java.awt.Image getIcon(int)
meth public java.awt.Image getOpenedIcon(int)
meth public java.awt.datatransfer.Transferable clipboardCopy() throws java.io.IOException
meth public java.awt.datatransfer.Transferable clipboardCut() throws java.io.IOException
meth public java.awt.datatransfer.Transferable drag() throws java.io.IOException
meth public javax.swing.Action getPreferredAction()
meth public org.openide.nodes.Node cloneNode()
meth public org.openide.nodes.Node$Handle getHandle()
meth public org.openide.nodes.Node$PropertySet[] getPropertySets()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public org.openide.util.actions.SystemAction getDefaultAction()
 anno 0 java.lang.Deprecated()
meth public org.openide.util.actions.SystemAction[] getActions()
 anno 0 java.lang.Deprecated()
meth public org.openide.util.datatransfer.NewType[] getNewTypes()
meth public org.openide.util.datatransfer.PasteType getDropType(java.awt.datatransfer.Transferable,int,int)
meth public void setDefaultAction(org.openide.util.actions.SystemAction)
 anno 0 java.lang.Deprecated()
meth public void setIconBase(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void setName(java.lang.String)
supr org.openide.nodes.Node
hfds DEFAULT_ICON,DEFAULT_ICON_BASE,DEFAULT_ICON_EXTENSION,ICON_BASE,NO_NEW_TYPES,NO_PASTE_TYPES,OPENED_ICON_BASE,iconBase,iconExtension,icons,lookup,overridesGetDefaultAction,preferredAction,sheet,sheetCookieL
hcls SheetAndCookieListener

CLSS public final org.openide.nodes.CookieSet
cons public init()
innr public abstract interface static Before
innr public abstract interface static Factory
intf org.openide.util.Lookup$Provider
meth public !varargs <%0 extends java.lang.Object> void assign(java.lang.Class<? extends {%%0}>,{%%0}[])
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public org.openide.util.Lookup getLookup()
meth public static org.openide.nodes.CookieSet createGeneric(org.openide.nodes.CookieSet$Before)
meth public void add(java.lang.Class<? extends org.openide.nodes.Node$Cookie>,org.openide.nodes.CookieSet$Factory)
meth public void add(java.lang.Class<? extends org.openide.nodes.Node$Cookie>[],org.openide.nodes.CookieSet$Factory)
meth public void add(org.openide.nodes.Node$Cookie)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void remove(java.lang.Class<? extends org.openide.nodes.Node$Cookie>,org.openide.nodes.CookieSet$Factory)
meth public void remove(java.lang.Class<? extends org.openide.nodes.Node$Cookie>[],org.openide.nodes.CookieSet$Factory)
meth public void remove(org.openide.nodes.Node$Cookie)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
hfds QUERY_MODE,cs,ic,lookup,map
hcls C,CookieEntry,CookieEntryPair,PairWrap,R

CLSS public abstract interface static org.openide.nodes.CookieSet$Factory
 outer org.openide.nodes.CookieSet
meth public abstract <%0 extends org.openide.nodes.Node$Cookie> {%%0} createCookie(java.lang.Class<{%%0}>)

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

CLSS public abstract org.openide.text.CloneableEditorSupport
cons public init(org.openide.text.CloneableEditorSupport$Env)
cons public init(org.openide.text.CloneableEditorSupport$Env,org.openide.util.Lookup)
fld public final static java.lang.String EDITOR_MODE = "editor"
fld public final static javax.swing.undo.UndoableEdit BEGIN_COMMIT_GROUP
fld public final static javax.swing.undo.UndoableEdit END_COMMIT_GROUP
fld public final static javax.swing.undo.UndoableEdit MARK_COMMIT_GROUP
innr public abstract interface static Env
innr public abstract interface static Pane
meth protected abstract java.lang.String messageName()
meth protected abstract java.lang.String messageSave()
meth protected abstract java.lang.String messageToolTip()
meth protected boolean asynchronousOpen()
meth protected boolean canClose()
meth protected boolean close(boolean)
meth protected boolean notifyModified()
meth protected final org.openide.awt.UndoRedo$Manager getUndoRedo()
meth protected final org.openide.text.CloneableEditorSupport$Pane openAt(org.openide.text.PositionRef,int)
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected java.awt.Component wrapEditorComponent(java.awt.Component)
meth protected java.lang.String documentID()
meth protected java.lang.String messageHtmlName()
meth protected java.lang.String messageLine(org.openide.text.Line)
meth protected javax.swing.text.EditorKit createEditorKit()
meth protected javax.swing.text.StyledDocument createStyledDocument(javax.swing.text.EditorKit)
meth protected org.openide.awt.UndoRedo$Manager createUndoRedoManager()
meth protected org.openide.text.CloneableEditor createCloneableEditor()
meth protected org.openide.text.CloneableEditorSupport$Pane createPane()
meth protected org.openide.util.Task reloadDocument()
meth protected org.openide.windows.CloneableTopComponent createCloneableTopComponent()
meth protected void afterRedirect(org.openide.windows.CloneableOpenSupport)
meth protected void initializeCloneableEditor(org.openide.text.CloneableEditor)
meth protected void loadFromStreamToKit(javax.swing.text.StyledDocument,java.io.InputStream,javax.swing.text.EditorKit) throws java.io.IOException,javax.swing.text.BadLocationException
meth protected void notifyClosed()
meth protected void notifyUnmodified()
meth protected void saveFromKitToStream(javax.swing.text.StyledDocument,javax.swing.text.EditorKit,java.io.OutputStream) throws java.io.IOException,javax.swing.text.BadLocationException
meth protected void updateTitles()
meth public boolean isDocumentLoaded()
meth public boolean isModified()
meth public final org.openide.text.PositionRef createPositionRef(int,javax.swing.text.Position$Bias)
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public java.io.InputStream getInputStream() throws java.io.IOException
meth public java.lang.String toString()
meth public javax.swing.JEditorPane[] getOpenedPanes()
meth public javax.swing.text.StyledDocument getDocument()
meth public javax.swing.text.StyledDocument openDocument() throws java.io.IOException
meth public org.openide.text.Line$Set getLineSet()
meth public org.openide.util.Task prepareDocument()
meth public static javax.swing.text.EditorKit getEditorKit(java.lang.String)
meth public void addChangeListener(javax.swing.event.ChangeListener)
 anno 0 java.lang.Deprecated()
meth public void open()
meth public void print()
meth public void removeChangeListener(javax.swing.event.ChangeListener)
 anno 0 java.lang.Deprecated()
meth public void saveDocument() throws java.io.IOException
meth public void setMIMEType(java.lang.String)
supr org.openide.windows.CloneableOpenSupport
hfds ERR,LOCAL_CLOSE_DOCUMENT,LOCK_PRINTING,PROP_PANE,alreadyModified,annotationsLoaded,checkModificationLock,docFilter,inUserQuestionExceptionHandler,isSaving,kit,lastReusable,lastSaveTime,lastSelected,lineSet,lineSetLineVector,listener,listeners,listeningOnEnv,lookup,mimeType,openClose,positionManager,preventModification,printing,propertyChangeSupport,reloadDialogOpened,undoRedo,warnedClasses
hcls DocFilter,Listener,PlainEditorKit

CLSS public abstract interface static org.openide.text.CloneableEditorSupport$Env
 outer org.openide.text.CloneableEditorSupport
fld public final static java.lang.String PROP_TIME = "time"
intf org.openide.windows.CloneableOpenSupport$Env
meth public abstract java.io.InputStream inputStream() throws java.io.IOException
meth public abstract java.io.OutputStream outputStream() throws java.io.IOException
meth public abstract java.lang.String getMimeType()
meth public abstract java.util.Date getTime()

CLSS public org.openide.text.DataEditorSupport
cons public init(org.openide.loaders.DataObject,org.openide.text.CloneableEditorSupport$Env)
cons public init(org.openide.loaders.DataObject,org.openide.util.Lookup,org.openide.text.CloneableEditorSupport$Env)
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
innr public abstract static Env
meth protected boolean canClose()
meth protected java.lang.String documentID()
meth protected java.lang.String messageHtmlName()
meth protected java.lang.String messageLine(org.openide.text.Line)
meth protected java.lang.String messageName()
meth protected java.lang.String messageOpened()
meth protected java.lang.String messageOpening()
meth protected java.lang.String messageSave()
meth protected java.lang.String messageToolTip()
meth protected javax.swing.text.StyledDocument createStyledDocument(javax.swing.text.EditorKit)
meth protected void initializeCloneableEditor(org.openide.text.CloneableEditor)
meth protected void loadFromStreamToKit(javax.swing.text.StyledDocument,java.io.InputStream,javax.swing.text.EditorKit) throws java.io.IOException,javax.swing.text.BadLocationException
meth protected void notifyClosed()
meth protected void saveFromKitToStream(javax.swing.text.StyledDocument,javax.swing.text.EditorKit,java.io.OutputStream) throws java.io.IOException,javax.swing.text.BadLocationException
meth public final org.openide.loaders.DataObject getDataObject()
meth public javax.swing.text.StyledDocument openDocument() throws java.io.IOException
meth public static java.lang.String annotateName(java.lang.String,boolean,boolean,boolean)
meth public static java.lang.String toolTip(org.openide.filesystems.FileObject,boolean,boolean)
meth public static org.openide.loaders.DataObject findDataObject(org.openide.text.Line)
meth public static org.openide.text.CloneableEditorSupport create(org.openide.loaders.DataObject,org.openide.loaders.MultiDataObject$Entry,org.openide.nodes.CookieSet)
meth public static org.openide.text.CloneableEditorSupport create(org.openide.loaders.DataObject,org.openide.loaders.MultiDataObject$Entry,org.openide.nodes.CookieSet,java.util.concurrent.Callable<org.openide.text.CloneableEditorSupport$Pane>)
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
meth public void saveAs(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public void saveDocument() throws java.io.IOException
supr org.openide.text.CloneableEditorSupport
hfds ERR,TABNAMES_HTML,cacheCounter,charsets,nodeL,obj,warnedEncodingFiles
hcls DOEnvLookup,DataNodeListener,EnvListener,SaveImpl

CLSS public abstract static org.openide.text.DataEditorSupport$Env
 outer org.openide.text.DataEditorSupport
cons public init(org.openide.loaders.DataObject)
intf org.openide.text.CloneableEditorSupport$Env
meth protected abstract org.openide.filesystems.FileLock takeLock() throws java.io.IOException
meth protected abstract org.openide.filesystems.FileObject getFile()
meth protected final void changeFile()
meth public java.io.InputStream inputStream() throws java.io.IOException
meth public java.io.OutputStream outputStream() throws java.io.IOException
meth public java.lang.String getMimeType()
meth public java.util.Date getTime()
meth public void markModified() throws java.io.IOException
meth public void unmarkModified()
supr org.openide.loaders.OpenSupport$Env
hfds BIG_FILE_THRESHOLD_MB,action,canWrite,fileLock,fileObject,sentBigFileInfo,serialVersionUID,warnedFiles
hcls ME,SaveAsCapableImpl

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

CLSS public abstract org.openide.windows.CloneableOpenSupport
cons public init(org.openide.windows.CloneableOpenSupport$Env)
fld protected org.openide.windows.CloneableOpenSupport$Env env
fld protected org.openide.windows.CloneableTopComponent$Ref allEditors
innr public abstract interface static Env
meth protected abstract java.lang.String messageOpened()
meth protected abstract java.lang.String messageOpening()
meth protected abstract org.openide.windows.CloneableTopComponent createCloneableTopComponent()
meth protected boolean canClose()
meth protected boolean close(boolean)
meth protected final org.openide.windows.CloneableTopComponent openCloneableTopComponent()
meth protected void afterRedirect(org.openide.windows.CloneableOpenSupport)
meth public boolean close()
meth public void edit()
meth public void open()
meth public void view()
supr java.lang.Object
hfds container
hcls Listener

CLSS public abstract interface static org.openide.windows.CloneableOpenSupport$Env
 outer org.openide.windows.CloneableOpenSupport
fld public final static java.lang.String PROP_MODIFIED = "modified"
fld public final static java.lang.String PROP_VALID = "valid"
intf java.io.Serializable
meth public abstract boolean isModified()
meth public abstract boolean isValid()
meth public abstract org.openide.windows.CloneableOpenSupport findCloneableOpenSupport()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void addVetoableChangeListener(java.beans.VetoableChangeListener)
meth public abstract void markModified() throws java.io.IOException
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removeVetoableChangeListener(java.beans.VetoableChangeListener)
meth public abstract void unmarkModified()

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

