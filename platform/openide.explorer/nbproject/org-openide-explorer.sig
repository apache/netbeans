#Signature file v4.1
#Version 6.85

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

CLSS public abstract interface java.beans.Customizer
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setObject(java.lang.Object)

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

CLSS public abstract interface java.io.Externalizable
intf java.io.Serializable
meth public abstract void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public abstract void writeExternal(java.io.ObjectOutput) throws java.io.IOException

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Cloneable

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation

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

CLSS public abstract interface javax.swing.ComboBoxModel<%0 extends java.lang.Object>
intf javax.swing.ListModel<{javax.swing.ComboBoxModel%0}>
meth public abstract java.lang.Object getSelectedItem()
meth public abstract void setSelectedItem(java.lang.Object)

CLSS public javax.swing.JComboBox<%0 extends java.lang.Object>
cons public init()
cons public init(java.util.Vector<{javax.swing.JComboBox%0}>)
cons public init(javax.swing.ComboBoxModel<{javax.swing.JComboBox%0}>)
cons public init({javax.swing.JComboBox%0}[])
fld protected boolean isEditable
fld protected boolean lightWeightPopupEnabled
fld protected int maximumRowCount
fld protected java.lang.Object selectedItemReminder
fld protected java.lang.String actionCommand
fld protected javax.swing.ComboBoxEditor editor
fld protected javax.swing.ComboBoxModel<{javax.swing.JComboBox%0}> dataModel
fld protected javax.swing.JComboBox$KeySelectionManager keySelectionManager
fld protected javax.swing.ListCellRenderer<? super {javax.swing.JComboBox%0}> renderer
innr protected AccessibleJComboBox
innr public abstract interface static KeySelectionManager
intf java.awt.ItemSelectable
intf java.awt.event.ActionListener
intf javax.accessibility.Accessible
intf javax.swing.event.ListDataListener
meth protected boolean processKeyBinding(javax.swing.KeyStroke,java.awt.event.KeyEvent,int,boolean)
meth protected java.beans.PropertyChangeListener createActionPropertyChangeListener(javax.swing.Action)
meth protected java.lang.String paramString()
meth protected javax.swing.JComboBox$KeySelectionManager createDefaultKeySelectionManager()
meth protected void actionPropertyChanged(javax.swing.Action,java.lang.String)
meth protected void configurePropertiesFromAction(javax.swing.Action)
meth protected void fireActionEvent()
meth protected void fireItemStateChanged(java.awt.event.ItemEvent)
meth protected void installAncestorListener()
meth protected void selectedItemChanged()
meth public boolean isEditable()
meth public boolean isLightWeightPopupEnabled()
meth public boolean isPopupVisible()
meth public boolean selectWithKeyChar(char)
meth public int getItemCount()
meth public int getMaximumRowCount()
meth public int getSelectedIndex()
meth public java.awt.event.ActionListener[] getActionListeners()
meth public java.awt.event.ItemListener[] getItemListeners()
meth public java.lang.Object getSelectedItem()
meth public java.lang.Object[] getSelectedObjects()
meth public java.lang.String getActionCommand()
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.Action getAction()
meth public javax.swing.ComboBoxEditor getEditor()
meth public javax.swing.ComboBoxModel<{javax.swing.JComboBox%0}> getModel()
meth public javax.swing.JComboBox$KeySelectionManager getKeySelectionManager()
meth public javax.swing.ListCellRenderer<? super {javax.swing.JComboBox%0}> getRenderer()
meth public javax.swing.event.PopupMenuListener[] getPopupMenuListeners()
meth public javax.swing.plaf.ComboBoxUI getUI()
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void addActionListener(java.awt.event.ActionListener)
meth public void addItem({javax.swing.JComboBox%0})
meth public void addItemListener(java.awt.event.ItemListener)
meth public void addPopupMenuListener(javax.swing.event.PopupMenuListener)
meth public void configureEditor(javax.swing.ComboBoxEditor,java.lang.Object)
meth public void contentsChanged(javax.swing.event.ListDataEvent)
meth public void firePopupMenuCanceled()
meth public void firePopupMenuWillBecomeInvisible()
meth public void firePopupMenuWillBecomeVisible()
meth public void hidePopup()
meth public void insertItemAt({javax.swing.JComboBox%0},int)
meth public void intervalAdded(javax.swing.event.ListDataEvent)
meth public void intervalRemoved(javax.swing.event.ListDataEvent)
meth public void processKeyEvent(java.awt.event.KeyEvent)
meth public void removeActionListener(java.awt.event.ActionListener)
meth public void removeAllItems()
meth public void removeItem(java.lang.Object)
meth public void removeItemAt(int)
meth public void removeItemListener(java.awt.event.ItemListener)
meth public void removePopupMenuListener(javax.swing.event.PopupMenuListener)
meth public void setAction(javax.swing.Action)
meth public void setActionCommand(java.lang.String)
meth public void setEditable(boolean)
meth public void setEditor(javax.swing.ComboBoxEditor)
meth public void setEnabled(boolean)
meth public void setKeySelectionManager(javax.swing.JComboBox$KeySelectionManager)
meth public void setLightWeightPopupEnabled(boolean)
meth public void setMaximumRowCount(int)
meth public void setModel(javax.swing.ComboBoxModel<{javax.swing.JComboBox%0}>)
meth public void setPopupVisible(boolean)
meth public void setPrototypeDisplayValue({javax.swing.JComboBox%0})
meth public void setRenderer(javax.swing.ListCellRenderer<? super {javax.swing.JComboBox%0}>)
meth public void setSelectedIndex(int)
meth public void setSelectedItem(java.lang.Object)
meth public void setUI(javax.swing.plaf.ComboBoxUI)
meth public void showPopup()
meth public void updateUI()
meth public {javax.swing.JComboBox%0} getItemAt(int)
meth public {javax.swing.JComboBox%0} getPrototypeDisplayValue()
supr javax.swing.JComponent

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

CLSS public javax.swing.JMenu
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,boolean)
cons public init(javax.swing.Action)
fld protected javax.swing.JMenu$WinListener popupListener
innr protected AccessibleJMenu
innr protected WinListener
intf javax.accessibility.Accessible
intf javax.swing.MenuElement
meth protected java.awt.Point getPopupMenuOrigin()
meth protected java.beans.PropertyChangeListener createActionChangeListener(javax.swing.JMenuItem)
meth protected java.lang.String paramString()
meth protected javax.swing.JMenu$WinListener createWinListener(javax.swing.JPopupMenu)
meth protected javax.swing.JMenuItem createActionComponent(javax.swing.Action)
meth protected void fireMenuCanceled()
meth protected void fireMenuDeselected()
meth protected void fireMenuSelected()
meth protected void processKeyEvent(java.awt.event.KeyEvent)
meth public boolean isMenuComponent(java.awt.Component)
meth public boolean isPopupMenuVisible()
meth public boolean isSelected()
meth public boolean isTearOff()
meth public boolean isTopLevelMenu()
meth public int getDelay()
meth public int getItemCount()
meth public int getMenuComponentCount()
meth public java.awt.Component add(java.awt.Component)
meth public java.awt.Component add(java.awt.Component,int)
meth public java.awt.Component getComponent()
meth public java.awt.Component getMenuComponent(int)
meth public java.awt.Component[] getMenuComponents()
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.JMenuItem add(java.lang.String)
meth public javax.swing.JMenuItem add(javax.swing.Action)
meth public javax.swing.JMenuItem add(javax.swing.JMenuItem)
meth public javax.swing.JMenuItem getItem(int)
meth public javax.swing.JMenuItem insert(javax.swing.Action,int)
meth public javax.swing.JMenuItem insert(javax.swing.JMenuItem,int)
meth public javax.swing.JPopupMenu getPopupMenu()
meth public javax.swing.MenuElement[] getSubElements()
meth public javax.swing.event.MenuListener[] getMenuListeners()
meth public void addMenuListener(javax.swing.event.MenuListener)
meth public void addSeparator()
meth public void applyComponentOrientation(java.awt.ComponentOrientation)
meth public void doClick(int)
meth public void insert(java.lang.String,int)
meth public void insertSeparator(int)
meth public void menuSelectionChanged(boolean)
meth public void remove(int)
meth public void remove(java.awt.Component)
meth public void remove(javax.swing.JMenuItem)
meth public void removeAll()
meth public void removeMenuListener(javax.swing.event.MenuListener)
meth public void setAccelerator(javax.swing.KeyStroke)
meth public void setComponentOrientation(java.awt.ComponentOrientation)
meth public void setDelay(int)
meth public void setMenuLocation(int,int)
meth public void setModel(javax.swing.ButtonModel)
meth public void setPopupMenuVisible(boolean)
meth public void setSelected(boolean)
meth public void updateUI()
supr javax.swing.JMenuItem

CLSS public javax.swing.JMenuItem
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,int)
cons public init(java.lang.String,javax.swing.Icon)
cons public init(javax.swing.Action)
cons public init(javax.swing.Icon)
innr protected AccessibleJMenuItem
intf javax.accessibility.Accessible
intf javax.swing.MenuElement
meth protected java.lang.String paramString()
meth protected void actionPropertyChanged(javax.swing.Action,java.lang.String)
meth protected void configurePropertiesFromAction(javax.swing.Action)
meth protected void fireMenuDragMouseDragged(javax.swing.event.MenuDragMouseEvent)
meth protected void fireMenuDragMouseEntered(javax.swing.event.MenuDragMouseEvent)
meth protected void fireMenuDragMouseExited(javax.swing.event.MenuDragMouseEvent)
meth protected void fireMenuDragMouseReleased(javax.swing.event.MenuDragMouseEvent)
meth protected void fireMenuKeyPressed(javax.swing.event.MenuKeyEvent)
meth protected void fireMenuKeyReleased(javax.swing.event.MenuKeyEvent)
meth protected void fireMenuKeyTyped(javax.swing.event.MenuKeyEvent)
meth protected void init(java.lang.String,javax.swing.Icon)
meth public boolean isArmed()
meth public java.awt.Component getComponent()
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.KeyStroke getAccelerator()
meth public javax.swing.MenuElement[] getSubElements()
meth public javax.swing.event.MenuDragMouseListener[] getMenuDragMouseListeners()
meth public javax.swing.event.MenuKeyListener[] getMenuKeyListeners()
meth public void addMenuDragMouseListener(javax.swing.event.MenuDragMouseListener)
meth public void addMenuKeyListener(javax.swing.event.MenuKeyListener)
meth public void menuSelectionChanged(boolean)
meth public void processKeyEvent(java.awt.event.KeyEvent,javax.swing.MenuElement[],javax.swing.MenuSelectionManager)
meth public void processMenuDragMouseEvent(javax.swing.event.MenuDragMouseEvent)
meth public void processMenuKeyEvent(javax.swing.event.MenuKeyEvent)
meth public void processMouseEvent(java.awt.event.MouseEvent,javax.swing.MenuElement[],javax.swing.MenuSelectionManager)
meth public void removeMenuDragMouseListener(javax.swing.event.MenuDragMouseListener)
meth public void removeMenuKeyListener(javax.swing.event.MenuKeyListener)
meth public void setAccelerator(javax.swing.KeyStroke)
meth public void setArmed(boolean)
meth public void setEnabled(boolean)
meth public void setModel(javax.swing.ButtonModel)
meth public void setUI(javax.swing.plaf.MenuItemUI)
meth public void updateUI()
supr javax.swing.AbstractButton

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

CLSS public abstract interface javax.swing.ListCellRenderer<%0 extends java.lang.Object>
meth public abstract java.awt.Component getListCellRendererComponent(javax.swing.JList<? extends {javax.swing.ListCellRenderer%0}>,{javax.swing.ListCellRenderer%0},int,boolean,boolean)

CLSS public abstract interface javax.swing.ListModel<%0 extends java.lang.Object>
meth public abstract int getSize()
meth public abstract void addListDataListener(javax.swing.event.ListDataListener)
meth public abstract void removeListDataListener(javax.swing.event.ListDataListener)
meth public abstract {javax.swing.ListModel%0} getElementAt(int)

CLSS public abstract interface javax.swing.MenuElement
meth public abstract java.awt.Component getComponent()
meth public abstract javax.swing.MenuElement[] getSubElements()
meth public abstract void menuSelectionChanged(boolean)
meth public abstract void processKeyEvent(java.awt.event.KeyEvent,javax.swing.MenuElement[],javax.swing.MenuSelectionManager)
meth public abstract void processMouseEvent(java.awt.event.MouseEvent,javax.swing.MenuElement[],javax.swing.MenuSelectionManager)

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

CLSS public abstract interface javax.swing.event.ListDataListener
intf java.util.EventListener
meth public abstract void contentsChanged(javax.swing.event.ListDataEvent)
meth public abstract void intervalAdded(javax.swing.event.ListDataEvent)
meth public abstract void intervalRemoved(javax.swing.event.ListDataEvent)

CLSS public abstract javax.swing.table.AbstractTableModel
cons public init()
fld protected javax.swing.event.EventListenerList listenerList
intf java.io.Serializable
intf javax.swing.table.TableModel
meth public <%0 extends java.util.EventListener> {%%0}[] getListeners(java.lang.Class<{%%0}>)
meth public boolean isCellEditable(int,int)
meth public int findColumn(java.lang.String)
meth public java.lang.Class<?> getColumnClass(int)
meth public java.lang.String getColumnName(int)
meth public javax.swing.event.TableModelListener[] getTableModelListeners()
meth public void addTableModelListener(javax.swing.event.TableModelListener)
meth public void fireTableCellUpdated(int,int)
meth public void fireTableChanged(javax.swing.event.TableModelEvent)
meth public void fireTableDataChanged()
meth public void fireTableRowsDeleted(int,int)
meth public void fireTableRowsInserted(int,int)
meth public void fireTableRowsUpdated(int,int)
meth public void fireTableStructureChanged()
meth public void removeTableModelListener(javax.swing.event.TableModelListener)
meth public void setValueAt(java.lang.Object,int,int)
supr java.lang.Object

CLSS public abstract interface javax.swing.table.TableModel
meth public abstract boolean isCellEditable(int,int)
meth public abstract int getColumnCount()
meth public abstract int getRowCount()
meth public abstract java.lang.Class<?> getColumnClass(int)
meth public abstract java.lang.Object getValueAt(int,int)
meth public abstract java.lang.String getColumnName(int)
meth public abstract void addTableModelListener(javax.swing.event.TableModelListener)
meth public abstract void removeTableModelListener(javax.swing.event.TableModelListener)
meth public abstract void setValueAt(java.lang.Object,int,int)

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

CLSS public abstract interface javax.swing.tree.TreeCellRenderer
meth public abstract java.awt.Component getTreeCellRendererComponent(javax.swing.JTree,java.lang.Object,boolean,boolean,boolean,int,boolean)

CLSS public abstract interface javax.swing.tree.TreeModel
meth public abstract boolean isLeaf(java.lang.Object)
meth public abstract int getChildCount(java.lang.Object)
meth public abstract int getIndexOfChild(java.lang.Object,java.lang.Object)
meth public abstract java.lang.Object getChild(java.lang.Object,int)
meth public abstract java.lang.Object getRoot()
meth public abstract void addTreeModelListener(javax.swing.event.TreeModelListener)
meth public abstract void removeTreeModelListener(javax.swing.event.TreeModelListener)
meth public abstract void valueForPathChanged(javax.swing.tree.TreePath,java.lang.Object)

CLSS public org.openide.awt.JMenuPlus
 anno 0 java.lang.Deprecated()
cons public init()
cons public init(java.lang.String)
supr javax.swing.JMenu
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

CLSS public final org.openide.explorer.ExplorerUtils
meth public static javax.swing.Action actionCopy(org.openide.explorer.ExplorerManager)
meth public static javax.swing.Action actionCut(org.openide.explorer.ExplorerManager)
meth public static javax.swing.Action actionDelete(org.openide.explorer.ExplorerManager,boolean)
meth public static javax.swing.Action actionPaste(org.openide.explorer.ExplorerManager)
meth public static org.openide.util.HelpCtx getHelpCtx(org.openide.nodes.Node[],org.openide.util.HelpCtx)
meth public static org.openide.util.Lookup createLookup(org.openide.explorer.ExplorerManager,javax.swing.ActionMap)
meth public static void activateActions(org.openide.explorer.ExplorerManager,boolean)
supr java.lang.Object

CLSS public abstract interface org.openide.explorer.ExtendedDelete
meth public abstract boolean delete(org.openide.nodes.Node[]) throws java.io.IOException

CLSS public org.openide.explorer.propertysheet.DefaultPropertyModel
 anno 0 java.lang.Deprecated()
cons public init(java.lang.Object,java.beans.PropertyDescriptor)
cons public init(java.lang.Object,java.lang.String)
intf java.beans.PropertyChangeListener
intf org.openide.explorer.propertysheet.ExPropertyModel
meth public java.beans.FeatureDescriptor getFeatureDescriptor()
meth public java.lang.Class getPropertyEditorClass()
meth public java.lang.Class getPropertyType()
meth public java.lang.Object getValue() throws java.lang.reflect.InvocationTargetException
meth public java.lang.Object[] getBeans()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setValue(java.lang.Object) throws java.lang.reflect.InvocationTargetException
supr java.lang.Object
hfds bean,donotfire,prop,propertyName,propertyTypeClass,readMethod,support,writeMethod

CLSS public abstract interface org.openide.explorer.propertysheet.ExPropertyEditor
fld public final static java.lang.String PROPERTY_HELP_ID = "helpID"
fld public final static java.lang.String PROP_VALUE_VALID = "propertyValueValid"
intf java.beans.PropertyEditor
meth public abstract void attachEnv(org.openide.explorer.propertysheet.PropertyEnv)

CLSS public abstract interface org.openide.explorer.propertysheet.ExPropertyModel
 anno 0 java.lang.Deprecated()
intf org.openide.explorer.propertysheet.PropertyModel
meth public abstract java.beans.FeatureDescriptor getFeatureDescriptor()
meth public abstract java.lang.Object[] getBeans()

CLSS public abstract interface org.openide.explorer.propertysheet.InplaceEditor
fld public final static java.lang.String COMMAND_FAILURE = "failure"
fld public final static java.lang.String COMMAND_SUCCESS = "success"
innr public abstract interface static Factory
meth public abstract boolean isKnownComponent(java.awt.Component)
meth public abstract boolean supportsTextEntry()
meth public abstract java.beans.PropertyEditor getPropertyEditor()
meth public abstract java.lang.Object getValue()
meth public abstract javax.swing.JComponent getComponent()
meth public abstract javax.swing.KeyStroke[] getKeyStrokes()
meth public abstract org.openide.explorer.propertysheet.PropertyModel getPropertyModel()
meth public abstract void addActionListener(java.awt.event.ActionListener)
meth public abstract void clear()
meth public abstract void connect(java.beans.PropertyEditor,org.openide.explorer.propertysheet.PropertyEnv)
meth public abstract void removeActionListener(java.awt.event.ActionListener)
meth public abstract void reset()
meth public abstract void setPropertyModel(org.openide.explorer.propertysheet.PropertyModel)
meth public abstract void setValue(java.lang.Object)

CLSS public abstract interface static org.openide.explorer.propertysheet.InplaceEditor$Factory
 outer org.openide.explorer.propertysheet.InplaceEditor
meth public abstract org.openide.explorer.propertysheet.InplaceEditor getInplaceEditor()

CLSS public org.openide.explorer.propertysheet.PropertyEnv
fld public final static java.lang.Object STATE_INVALID
fld public final static java.lang.Object STATE_NEEDS_VALIDATION
fld public final static java.lang.Object STATE_VALID
fld public final static java.lang.String PROP_STATE = "state"
meth public !varargs static org.openide.explorer.propertysheet.PropertyEnv create(java.beans.FeatureDescriptor,java.lang.Object[])
meth public java.beans.FeatureDescriptor getFeatureDescriptor()
meth public java.lang.Object getState()
meth public java.lang.Object[] getBeans()
meth public java.lang.String toString()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addVetoableChangeListener(java.beans.VetoableChangeListener)
meth public void registerInplaceEditorFactory(org.openide.explorer.propertysheet.InplaceEditor$Factory)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removeVetoableChangeListener(java.beans.VetoableChangeListener)
meth public void setState(java.lang.Object)
supr java.lang.Object
hfds LOG,PROP_CHANGE_IMMEDIATE,beans,change,changeImmediate,dummyDescriptor,editable,factory,featureDescriptor,state,support

CLSS public abstract interface org.openide.explorer.propertysheet.PropertyModel
fld public final static java.lang.String PROP_VALUE = "value"
meth public abstract java.lang.Class getPropertyEditorClass()
meth public abstract java.lang.Class getPropertyType()
meth public abstract java.lang.Object getValue() throws java.lang.reflect.InvocationTargetException
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setValue(java.lang.Object) throws java.lang.reflect.InvocationTargetException

CLSS public org.openide.explorer.propertysheet.PropertyPanel
cons public init()
cons public init(java.lang.Object,java.lang.String,int)
cons public init(org.openide.explorer.propertysheet.PropertyModel,int)
cons public init(org.openide.nodes.Node$Property)
cons public init(org.openide.nodes.Node$Property,int)
fld public final static int PREF_CUSTOM_EDITOR = 2
fld public final static int PREF_INPUT_STATE = 4
fld public final static int PREF_READ_ONLY = 1
fld public final static int PREF_TABLEUI = 8
fld public final static java.lang.String PROP_MODEL = "model"
fld public final static java.lang.String PROP_PREFERENCES = "preferences"
fld public final static java.lang.String PROP_PROPERTY_EDITOR = "propertyEditor"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String PROP_STATE = "state"
intf javax.accessibility.Accessible
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void processFocusEvent(java.awt.event.FocusEvent)
meth public boolean isChangeImmediate()
meth public boolean isFocusable()
meth public final java.lang.Object getState()
meth public final org.openide.nodes.Node$Property getProperty()
meth public final void setProperty(org.openide.nodes.Node$Property)
meth public int getPreferences()
meth public java.awt.Dimension getMinimumSize()
meth public java.awt.Dimension getPreferredSize()
meth public java.beans.PropertyEditor getPropertyEditor()
 anno 0 java.lang.Deprecated()
meth public java.lang.String toString()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public org.openide.explorer.propertysheet.PropertyModel getModel()
meth public void addNotify()
meth public void doLayout()
meth public void layout()
meth public void paint(java.awt.Graphics)
meth public void removeNotify()
meth public void requestFocus()
meth public void setBackground(java.awt.Color)
meth public void setChangeImmediate(boolean)
meth public void setEnabled(boolean)
meth public void setForeground(java.awt.Color)
meth public void setModel(org.openide.explorer.propertysheet.PropertyModel)
meth public void setPreferences(int)
meth public void updateValue()
supr javax.swing.JComponent
hfds beans,changeImmediate,displayer,ignoreCommit,initializing,inner,isGtk,listener,model,preferences,prop,reusableEnv,reusableModel,settingModel
hcls AccessiblePropertyPanel,BridgeAccessor,CustomEditorProxyAction,Listener

CLSS public org.openide.explorer.propertysheet.PropertySheet
cons public init()
fld protected static javax.swing.Icon iAlphaSort
 anno 0 java.lang.Deprecated()
fld protected static javax.swing.Icon iCustomize
 anno 0 java.lang.Deprecated()
fld protected static javax.swing.Icon iDisplayWritableOnly
 anno 0 java.lang.Deprecated()
fld protected static javax.swing.Icon iNoSort
 anno 0 java.lang.Deprecated()
fld protected static javax.swing.Icon iTypeSort
 anno 0 java.lang.Deprecated()
fld public final static int ALWAYS_AS_STRING = 1
 anno 0 java.lang.Deprecated()
fld public final static int PAINTING_PREFERRED = 3
 anno 0 java.lang.Deprecated()
fld public final static int SORTED_BY_NAMES = 1
fld public final static int SORTED_BY_TYPES = 2
 anno 0 java.lang.Deprecated()
fld public final static int STRING_PREFERRED = 2
 anno 0 java.lang.Deprecated()
fld public final static int UNSORTED = 0
fld public final static java.lang.String PROPERTY_CURRENT_PAGE = "currentPage"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String PROPERTY_DISABLED_PROPERTY_COLOR = "disabledPropertyColor"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String PROPERTY_DISPLAY_WRITABLE_ONLY = "displayWritableOnly"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String PROPERTY_PLASTIC = "plastic"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String PROPERTY_PROPERTY_PAINTING_STYLE = "propertyPaintingStyle"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String PROPERTY_SORTING_MODE = "sortingMode"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String PROPERTY_VALUE_COLOR = "valueColor"
 anno 0 java.lang.Deprecated()
meth protected final boolean isExpanded(java.beans.FeatureDescriptor)
meth protected final java.beans.FeatureDescriptor getSelection()
meth protected final void toggleExpanded(java.beans.FeatureDescriptor)
meth protected javax.swing.JPopupMenu createPopupMenu()
meth public boolean getDisplayWritableOnly()
 anno 0 java.lang.Deprecated()
meth public boolean getPlastic()
 anno 0 java.lang.Deprecated()
meth public boolean isQuickSearchAllowed()
meth public boolean requestFocusInWindow()
meth public boolean setCurrentPage(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public final void setDescriptionAreaVisible(boolean)
meth public final void setPopupEnabled(boolean)
meth public int getCurrentPage()
 anno 0 java.lang.Deprecated()
meth public int getPropertyPaintingStyle()
 anno 0 java.lang.Deprecated()
meth public int getSortingMode()
meth public java.awt.Color getDisabledPropertyColor()
 anno 0 java.lang.Deprecated()
meth public java.awt.Color getValueColor()
 anno 0 java.lang.Deprecated()
meth public void addNotify()
meth public void firePropertyChange(java.lang.String,boolean,boolean)
meth public void removeNotify()
meth public void requestFocus()
meth public void setCurrentPage(int)
 anno 0 java.lang.Deprecated()
meth public void setDisabledPropertyColor(java.awt.Color)
 anno 0 java.lang.Deprecated()
meth public void setDisplayWritableOnly(boolean)
 anno 0 java.lang.Deprecated()
meth public void setNodes(org.openide.nodes.Node[])
meth public void setPlastic(boolean)
 anno 0 java.lang.Deprecated()
meth public void setPropertyPaintingStyle(int)
 anno 0 java.lang.Deprecated()
meth public void setQuickSearchAllowed(boolean)
meth public void setSortingMode(int) throws java.beans.PropertyVetoException
meth public void setValueColor(java.awt.Color)
 anno 0 java.lang.Deprecated()
meth public void updateUI()
supr javax.swing.JPanel
hfds ACTION_INVOKE_HELP,ACTION_INVOKE_POPUP,INIT_DELAY,MAX_DELAY,RP,forceTabs,helpAction,helperNodes,initTask,neverTabs,pclistener,popupEnabled,psheet,scheduleTask,serialVersionUID,showDesc,sortingMode,storedNode,table
hcls HelpAction,MutableAction,SheetPCListener,TabInfo,TabSelectionListener

CLSS public org.openide.explorer.propertysheet.PropertySheetView
cons public init()
meth public void addNotify()
meth public void removeNotify()
supr org.openide.explorer.propertysheet.PropertySheet
hfds explorerManager,guiInitialized,managerListener,serialVersionUID
hcls PropertyIL

CLSS public abstract interface org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor
 anno 0 java.lang.Deprecated()
meth public abstract java.lang.Object getPropertyValue()

CLSS public abstract interface org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor
 anno 0 java.lang.Deprecated()
intf java.beans.PropertyEditor
meth public abstract boolean hasInPlaceCustomEditor()
meth public abstract boolean supportsEditingTaggedValues()
meth public abstract java.awt.Component getInPlaceCustomEditor()

CLSS public abstract interface org.openide.explorer.propertysheet.editors.NodeCustomizer
 anno 0 java.lang.Deprecated()
intf java.beans.Customizer
meth public abstract void attach(org.openide.nodes.Node)

CLSS public abstract interface org.openide.explorer.propertysheet.editors.NodePropertyEditor
 anno 0 java.lang.Deprecated()
intf java.beans.PropertyEditor
meth public abstract void attach(org.openide.nodes.Node[])

CLSS public abstract interface org.openide.explorer.propertysheet.editors.XMLPropertyEditor
intf java.beans.PropertyEditor
meth public abstract org.w3c.dom.Node storeToXML(org.w3c.dom.Document)
meth public abstract void readFromXML(org.w3c.dom.Node) throws java.io.IOException

CLSS abstract interface org.openide.explorer.propertysheet.editors.package-info

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

CLSS public abstract interface org.openide.explorer.view.CheckableNode
meth public abstract boolean isCheckEnabled()
meth public abstract boolean isCheckable()
meth public abstract java.lang.Boolean isSelected()
meth public abstract void setSelected(java.lang.Boolean)

CLSS public org.openide.explorer.view.ChoiceView
cons public init()
intf java.io.Externalizable
meth protected org.openide.explorer.view.NodeListModel createModel()
meth public boolean getShowExploredContext()
meth public void addNotify()
meth public void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void removeNotify()
meth public void setShowExploredContext(boolean)
meth public void writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr javax.swing.JComboBox
hfds iListener,manager,model,serialVersionUID,showExploredContext
hcls PropertyIL

CLSS public org.openide.explorer.view.ContextTreeView
cons public init()
meth protected boolean selectionAccept(org.openide.nodes.Node[])
meth protected boolean useExploredContextMenu()
meth protected org.openide.explorer.view.NodeTreeModel createModel()
meth protected void selectionChanged(org.openide.nodes.Node[],org.openide.explorer.ExplorerManager) throws java.beans.PropertyVetoException
meth protected void showPath(javax.swing.tree.TreePath)
meth protected void showSelection(javax.swing.tree.TreePath[])
supr org.openide.explorer.view.TreeView
hfds LOG,serialVersionUID
hcls NodeContextModel

CLSS public org.openide.explorer.view.IconView
cons public init()
intf java.io.Externalizable
meth protected javax.swing.JList createList()
supr org.openide.explorer.view.ListView
hfds serialVersionUID

CLSS public org.openide.explorer.view.ListTableView
 anno 0 java.lang.Deprecated()
cons public init()
cons public init(org.openide.explorer.view.NodeTableModel)
meth public final int getListPreferredWidth()
meth public final int getTableAutoResizeMode()
meth public final int getTableColumnPreferredWidth(int)
meth public final void setTableAutoResizeMode(int)
meth public final void setTableColumnPreferredWidth(int,int)
meth public java.awt.Dimension getPreferredSize()
meth public void addNotify()
meth public void removeNotify()
meth public void setListPreferredWidth(int)
meth public void setPreferredSize(java.awt.Dimension)
meth public void setProperties(org.openide.nodes.Node$Property[])
supr org.openide.explorer.view.ListView
hfds controlledTableView,listener,manager,pchl,prefSize,table,tableChanging
hcls Listener

CLSS public org.openide.explorer.view.ListView
cons public init()
fld protected javax.swing.JList list
fld protected org.openide.explorer.view.NodeListModel model
intf java.io.Externalizable
meth protected boolean selectionAccept(org.openide.nodes.Node[])
meth protected javax.swing.JList createList()
meth protected org.openide.explorer.view.NodeListModel createModel()
meth protected void selectionChanged(org.openide.nodes.Node[],org.openide.explorer.ExplorerManager) throws java.beans.PropertyVetoException
meth protected void showSelection(int[])
meth protected void validateTree()
meth public boolean isDragSource()
meth public boolean isDropTarget()
meth public boolean isPopupAllowed()
meth public boolean isShowParentNode()
meth public boolean isTraversalAllowed()
meth public boolean requestFocusInWindow()
meth public int getAllowedDragActions()
meth public int getAllowedDropActions()
meth public int getSelectionMode()
meth public java.awt.Dimension getPreferredSize()
meth public java.awt.event.ActionListener getDefaultProcessor()
meth public void addNotify()
meth public void doLayout()
meth public void paint(java.awt.Graphics)
meth public void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void removeNotify()
meth public void requestFocus()
meth public void setAllowedDragActions(int)
meth public void setAllowedDropActions(int)
meth public void setDefaultProcessor(java.awt.event.ActionListener)
meth public void setDragSource(boolean)
meth public void setDropTarget(boolean)
meth public void setPopupAllowed(boolean)
meth public void setSelectionMode(int)
meth public void setShowParentNode(boolean)
meth public void setTraversalAllowed(boolean)
meth public void writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr javax.swing.JScrollPane
hfds allowedDragActions,allowedDropActions,defaultProcessor,dragActive,dragSupport,dropActive,dropSupport,listenerActive,manager,managerListener,popupAllowed,popupSupport,serialVersionUID,showParentNode,traversalAllowed,wlpc,wlvc
hcls EnterAction,GoUpAction,GuardedActions,Listener,NbList,PopupSupport

CLSS public org.openide.explorer.view.MenuView
cons public init()
innr public abstract interface static Acceptor
innr public static Menu
innr public static MenuItem
meth public void addNotify()
meth public void removeNotify()
supr javax.swing.JPanel
hfds DEFAULT_LISTENER,current,explorerManager,listener,root,serialVersionUID
hcls AcceptorProxy,Listener

CLSS public abstract interface static org.openide.explorer.view.MenuView$Acceptor
 outer org.openide.explorer.view.MenuView
 anno 0 java.lang.Deprecated()
meth public abstract boolean accept(org.openide.nodes.Node)
 anno 0 java.lang.Deprecated()

CLSS public static org.openide.explorer.view.MenuView$Menu
 outer org.openide.explorer.view.MenuView
cons public init(org.openide.nodes.Node)
cons public init(org.openide.nodes.Node,org.openide.explorer.view.MenuView$Acceptor)
 anno 0 java.lang.Deprecated()
cons public init(org.openide.nodes.Node,org.openide.explorer.view.MenuView$Acceptor,boolean)
 anno 0 java.lang.Deprecated()
cons public init(org.openide.nodes.Node,org.openide.nodes.NodeAcceptor)
cons public init(org.openide.nodes.Node,org.openide.nodes.NodeAcceptor,boolean)
fld protected org.openide.nodes.Node node
fld protected org.openide.nodes.NodeAcceptor action
meth protected javax.swing.JMenuItem createMenuItem(org.openide.nodes.Node)
meth public javax.swing.JPopupMenu getPopupMenu()
meth public void processMouseEvent(java.awt.event.MouseEvent,javax.swing.MenuElement[],javax.swing.MenuSelectionManager)
supr org.openide.awt.JMenuPlus
hfds filled,serialVersionUID
hcls Helper

CLSS public static org.openide.explorer.view.MenuView$MenuItem
 outer org.openide.explorer.view.MenuView
cons public init(org.openide.nodes.Node)
cons public init(org.openide.nodes.Node,org.openide.explorer.view.MenuView$Acceptor)
 anno 0 java.lang.Deprecated()
cons public init(org.openide.nodes.Node,org.openide.explorer.view.MenuView$Acceptor,boolean)
 anno 0 java.lang.Deprecated()
cons public init(org.openide.nodes.Node,org.openide.nodes.NodeAcceptor)
cons public init(org.openide.nodes.Node,org.openide.nodes.NodeAcceptor,boolean)
fld protected org.openide.nodes.Node node
fld protected org.openide.nodes.NodeAcceptor action
intf org.openide.util.HelpCtx$Provider
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void doClick(int)
supr javax.swing.JMenuItem
hfds serialVersionUID

CLSS public org.openide.explorer.view.NodeListModel
cons public init()
cons public init(org.openide.nodes.Node)
intf javax.swing.ComboBoxModel
meth public int getDepth()
meth public int getIndex(java.lang.Object)
meth public int getSize()
meth public java.lang.Object getElementAt(int)
meth public java.lang.Object getSelectedItem()
meth public void setDepth(int)
meth public void setNode(org.openide.nodes.Node)
meth public void setSelectedItem(java.lang.Object)
supr javax.swing.AbstractListModel
hfds childrenCount,depth,listener,parent,selectedObject,serialVersionUID,showParent,size
hcls Info,Listener

CLSS public org.openide.explorer.view.NodePopupFactory
cons public init()
meth public javax.swing.JPopupMenu createPopupMenu(int,int,org.openide.nodes.Node[],java.awt.Component)
meth public void setShowQuickFilter(boolean)
supr java.lang.Object
hfds showQuickFilter

CLSS public org.openide.explorer.view.NodeRenderer
cons public init()
cons public init(boolean)
 anno 0 java.lang.Deprecated()
intf javax.swing.ListCellRenderer
intf javax.swing.tree.TreeCellRenderer
meth public final boolean isShowIcons()
meth public final void setShowIcons(boolean)
meth public java.awt.Component getListCellRendererComponent(javax.swing.JList,java.lang.Object,int,boolean,boolean)
meth public java.awt.Component getTreeCellRendererComponent(javax.swing.JTree,java.lang.Object,boolean,boolean,boolean,int,boolean)
meth public static org.openide.explorer.view.NodeRenderer sharedInstance()
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds bigIcons,draggedOver,instance,labelGap,renderer,showIcons

CLSS public org.openide.explorer.view.NodeTableModel
cons public init()
meth protected org.openide.nodes.Node$Property getPropertyFor(org.openide.nodes.Node,org.openide.nodes.Node$Property)
meth public boolean isCellEditable(int,int)
meth public int getColumnCount()
meth public int getRowCount()
meth public java.lang.Class getColumnClass(int)
meth public java.lang.Object getValueAt(int,int)
meth public java.lang.String getColumnName(int)
meth public void setNodes(org.openide.nodes.Node[])
meth public void setProperties(org.openide.nodes.Node$Property[])
supr javax.swing.table.AbstractTableModel
hfds ATTR_COMPARABLE_COLUMN,ATTR_DESCENDING_ORDER,ATTR_DISPLAY_NAME_WITH_MNEMONIC,ATTR_INVISIBLE,ATTR_MNEMONIC_CHAR,ATTR_ORDER_NUMBER,ATTR_SORTING_COLUMN,ATTR_TREE_COLUMN,allPropertyColumns,existsComparableColumn,nodeRows,pcl,propertyColumns,sortColumn,treeColumnProperty
hcls ArrayColumn

CLSS public org.openide.explorer.view.NodeTreeModel
cons public init()
cons public init(org.openide.nodes.Node)
meth public void setNode(org.openide.nodes.Node)
meth public void valueForPathChanged(javax.swing.tree.TreePath,java.lang.Object)
supr javax.swing.tree.DefaultTreeModel
hfds LOG,listener,serialVersionUID,views
hcls Listener,TreeModelEventImpl

CLSS public org.openide.explorer.view.OutlineView
cons public init()
cons public init(java.lang.String)
meth protected int getAllowedDropActions(java.awt.datatransfer.Transferable)
meth protected org.netbeans.swing.outline.OutlineModel createOutlineModel(org.openide.explorer.view.NodeTreeModel,org.netbeans.swing.outline.RowModel,java.lang.String)
meth public !varargs final void setPropertyColumns(java.lang.String[])
meth public boolean isDefaultActionAllowed()
meth public boolean isDragSource()
meth public boolean isDropTarget()
meth public boolean isExpanded(org.openide.nodes.Node)
meth public boolean isPopupAllowed()
meth public boolean isQuickSearchAllowed()
meth public boolean requestFocusInWindow()
meth public final boolean isShowNodeIcons()
meth public final boolean removePropertyColumn(java.lang.String)
meth public final void addPropertyColumn(java.lang.String,java.lang.String)
meth public final void addPropertyColumn(java.lang.String,java.lang.String,java.lang.String)
meth public final void setPropertyColumnAttribute(java.lang.String,java.lang.String,java.lang.Object)
meth public final void setPropertyColumnDescription(java.lang.String,java.lang.String)
meth public final void setShowNodeIcons(boolean)
meth public int getAllowedDragActions()
meth public int getAllowedDropActions()
meth public int getHorizontalScrollBarPolicy()
meth public int getTreeHorizontalScrollBarPolicy()
meth public java.awt.Insets getInsets()
meth public org.netbeans.swing.outline.Outline getOutline()
meth public org.openide.explorer.view.NodePopupFactory getNodePopupFactory()
meth public void add(java.awt.Component,java.lang.Object)
meth public void addNotify()
meth public void addTreeExpansionListener(javax.swing.event.TreeExpansionListener)
meth public void collapseNode(org.openide.nodes.Node)
meth public void expandNode(org.openide.nodes.Node)
meth public void readSettings(java.util.Properties,java.lang.String)
meth public void remove(java.awt.Component)
meth public void removeNotify()
meth public void removeTreeExpansionListener(javax.swing.event.TreeExpansionListener)
meth public void requestFocus()
meth public void setAllowedDragActions(int)
meth public void setAllowedDropActions(int)
meth public void setDefaultActionAllowed(boolean)
meth public void setDragSource(boolean)
meth public void setDropTarget(boolean)
meth public void setNodePopupFactory(org.openide.explorer.view.NodePopupFactory)
meth public void setPopupAllowed(boolean)
meth public void setProperties(org.openide.nodes.Node$Property[])
 anno 0 java.lang.Deprecated()
meth public void setQuickSearchAllowed(boolean)
meth public void setQuickSearchTableFilter(org.openide.explorer.view.QuickSearchTableFilter,boolean)
meth public void setTreeHorizontalScrollBarPolicy(int)
meth public void setTreeSortable(boolean)
meth public void writeSettings(java.util.Properties,java.lang.String)
supr javax.swing.JScrollPane
hfds REVALIDATING_RP,TREE_HORIZONTAL_SCROLLBAR,allowedDragActions,allowedDropActions,defaultTreeActionListener,dragActive,dragSupport,dropActive,dropSupport,dropTargetPopupAllowed,hScrollBar,horizontalScrollBarIsNeeded,isTreeHScrollBar,listener,logger,manager,managerListener,managerLock,model,nodeRenderer,outline,popupFactory,popupListener,qsKeyListener,quickSearch,rowModel,searchConstraints,searchPanel,selection,treeHorizontalScrollBarPolicy,treeModel,wlpc,wlvc
hcls DefaultTreeAction,NodeOutlineModel,OutlinePopupFactory,OutlineScrollLayout,OutlineViewOutline,PopupAction,PopupAdapter,PrototypeProperty,ScrollListener,Selection,TableSelectionListener

CLSS public abstract interface org.openide.explorer.view.QuickSearchTableFilter
meth public abstract java.lang.String getStringValueAt(int,int)

CLSS public org.openide.explorer.view.TableView
cons public init()
cons public init(org.openide.explorer.view.NodeTableModel)
meth public boolean isDragSource()
meth public boolean isDropTarget()
meth public boolean isPopupAllowed()
meth public boolean requestFocusInWindow()
meth public int getAllowedDragActions()
meth public int getAllowedDropActions()
meth public org.netbeans.swing.etable.ETable getTable()
meth public org.openide.explorer.view.NodePopupFactory getNodePopupFactory()
meth public void addNotify()
meth public void readSettings(java.util.Properties,java.lang.String)
meth public void removeNotify()
meth public void requestFocus()
meth public void setAllowedDragActions(int)
meth public void setAllowedDropActions(int)
meth public void setDragSource(boolean)
meth public void setDropTarget(boolean)
meth public void setNodePopupFactory(org.openide.explorer.view.NodePopupFactory)
meth public void setPopupAllowed(boolean)
meth public void writeSettings(java.util.Properties,java.lang.String)
supr javax.swing.JScrollPane
hfds allowedDragActions,allowedDropActions,dragActive,dragSupport,dropActive,dropSupport,dropTargetPopupAllowed,manager,managerListener,popupFactory,popupListener,table,wlpc,wlvc
hcls PopupAction,PopupAdapter,TableSelectionListener,TableViewETable

CLSS public org.openide.explorer.view.TreeTableView
cons public init()
cons public init(org.openide.explorer.view.NodeTableModel)
fld protected javax.swing.JTable treeTable
meth protected org.openide.explorer.view.NodeTreeModel createModel()
meth protected void showSelection(javax.swing.tree.TreePath[])
meth public boolean isExpanded(org.openide.nodes.Node)
meth public boolean isQuickSearchAllowed()
meth public boolean requestFocusInWindow()
meth public final int getTableAutoResizeMode()
meth public final int getTableColumnPreferredWidth(int)
meth public final int getTreePreferredWidth()
meth public final void setTableAutoResizeMode(int)
meth public final void setTableColumnPreferredWidth(int,int)
meth public final void setTreePreferredWidth(int)
meth public java.awt.Insets getInsets()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public void add(java.awt.Component,java.lang.Object)
meth public void addMouseListener(java.awt.event.MouseListener)
meth public void addNotify()
meth public void collapseNode(org.openide.nodes.Node)
meth public void expandNode(org.openide.nodes.Node)
meth public void remove(java.awt.Component)
meth public void removeMouseListener(java.awt.event.MouseListener)
meth public void removeNotify()
meth public void requestFocus()
meth public void setDefaultActionAllowed(boolean)
meth public void setDragSource(boolean)
meth public void setDropTarget(boolean)
meth public void setHorizontalScrollBarPolicy(int)
meth public void setPopupAllowed(boolean)
meth public void setProperties(org.openide.nodes.Node$Property[])
meth public void setQuickSearchAllowed(boolean)
meth public void setRowHeader(javax.swing.JViewport)
meth public void setSelectionMode(int)
meth public void setVerticalScrollBarPolicy(int)
supr org.openide.explorer.view.BeanTreeView
hfds COLUMNS_ICON,SORT_ASC_ICON,SORT_DESC_ICON,accessContext,allowHideColumns,allowSortingByColumn,colsButton,defaultHeaderRenderer,defaultTreeActionListener,hScrollBar,hideHScrollBar,listener,quickSearch,scrollPane,searchConstraints,searchpanel,sortedNodeTreeModel,tableModel,tableMouseListener,treeColumnProperty,treeColumnWidth,treeTableParent
hcls AccessibleTreeTableView,CompoundScrollPane,DefaultTreeAction,ScrollListener,SearchScrollPaneLayout,SortedNodeTreeModel,SortingHeaderRenderer,TreeColumnProperty

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

CLSS public org.openide.explorer.view.Visualizer
meth public static javax.swing.tree.TreeNode findVisualizer(org.openide.nodes.Node)
meth public static org.openide.nodes.Node findNode(java.lang.Object)
supr java.lang.Object

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

