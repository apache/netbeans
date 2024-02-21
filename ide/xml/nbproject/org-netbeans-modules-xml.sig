#Signature file v4.1
#Version 1.56

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

CLSS public abstract interface java.beans.BeanInfo
fld public final static int ICON_COLOR_16x16 = 1
fld public final static int ICON_COLOR_32x32 = 2
fld public final static int ICON_MONO_16x16 = 3
fld public final static int ICON_MONO_32x32 = 4
meth public abstract int getDefaultEventIndex()
meth public abstract int getDefaultPropertyIndex()
meth public abstract java.awt.Image getIcon(int)
meth public abstract java.beans.BeanDescriptor getBeanDescriptor()
meth public abstract java.beans.BeanInfo[] getAdditionalBeanInfo()
meth public abstract java.beans.EventSetDescriptor[] getEventSetDescriptors()
meth public abstract java.beans.MethodDescriptor[] getMethodDescriptors()
meth public abstract java.beans.PropertyDescriptor[] getPropertyDescriptors()

CLSS public abstract interface java.beans.Customizer
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setObject(java.lang.Object)

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

CLSS public java.beans.SimpleBeanInfo
cons public init()
intf java.beans.BeanInfo
meth public int getDefaultEventIndex()
meth public int getDefaultPropertyIndex()
meth public java.awt.Image getIcon(int)
meth public java.awt.Image loadImage(java.lang.String)
meth public java.beans.BeanDescriptor getBeanDescriptor()
meth public java.beans.BeanInfo[] getAdditionalBeanInfo()
meth public java.beans.EventSetDescriptor[] getEventSetDescriptors()
meth public java.beans.MethodDescriptor[] getMethodDescriptors()
meth public java.beans.PropertyDescriptor[] getPropertyDescriptors()
supr java.lang.Object

CLSS public abstract interface java.beans.VetoableChangeListener
intf java.util.EventListener
meth public abstract void vetoableChange(java.beans.PropertyChangeEvent) throws java.beans.PropertyVetoException

CLSS public abstract interface java.io.Externalizable
intf java.io.Serializable
meth public abstract void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public abstract void writeExternal(java.io.ObjectOutput) throws java.io.IOException

CLSS public abstract interface java.io.Serializable

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

CLSS public abstract interface javax.swing.event.ChangeListener
intf java.util.EventListener
meth public abstract void stateChanged(javax.swing.event.ChangeEvent)

CLSS public abstract interface org.netbeans.api.actions.Closable
meth public abstract boolean close()

CLSS public abstract interface org.netbeans.api.actions.Editable
meth public abstract void edit()

CLSS public abstract interface org.netbeans.api.actions.Openable
meth public abstract void open()

CLSS public abstract interface org.netbeans.api.actions.Printable
meth public abstract void print()

CLSS public abstract interface org.netbeans.api.actions.Savable
fld public final static org.openide.util.Lookup REGISTRY
meth public abstract java.lang.String toString()
meth public abstract void save() throws java.io.IOException

CLSS public abstract interface org.netbeans.api.xml.cookies.CookieObserver
meth public abstract void receive(org.netbeans.api.xml.cookies.CookieMessage)

CLSS public abstract interface org.netbeans.modules.csl.spi.CommentHandler
innr public abstract static DefaultCommentHandler
meth public abstract int[] getAdjustedBlocks(javax.swing.text.Document,int,int)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract int[] getCommentBlocks(javax.swing.text.Document,int,int)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getCommentEndDelimiter()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getCommentStartDelimiter()
 anno 0 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract static org.netbeans.modules.csl.spi.CommentHandler$DefaultCommentHandler
 outer org.netbeans.modules.csl.spi.CommentHandler
cons public init()
intf org.netbeans.modules.csl.spi.CommentHandler
meth public int[] getAdjustedBlocks(javax.swing.text.Document,int,int)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public int[] getCommentBlocks(javax.swing.text.Document,int,int)
supr java.lang.Object

CLSS public org.netbeans.modules.xml.CoreModuleInstall
cons public init()
meth public void uninstalled()
supr org.openide.modules.ModuleInstall

CLSS public final org.netbeans.modules.xml.DTDDataLoader
cons public init()
meth protected java.lang.String actionsContext()
meth protected java.lang.String defaultDisplayName()
meth protected org.openide.loaders.MultiDataObject createMultiObject(org.openide.filesystems.FileObject) throws java.io.IOException
meth protected org.openide.loaders.MultiDataObject$Entry createPrimaryEntry(org.openide.loaders.MultiDataObject,org.openide.filesystems.FileObject)
meth protected void initialize()
supr org.openide.loaders.UniFileLoader
hfds DTD_EXT,MOD_EXT,serialVersionUID

CLSS public org.netbeans.modules.xml.DTDDataLoaderBeanInfo
cons public init()
meth public java.awt.Image getIcon(int)
meth public java.beans.BeanInfo[] getAdditionalBeanInfo()
meth public java.beans.EventSetDescriptor[] getEventSetDescriptors()
meth public java.beans.MethodDescriptor[] getMethodDescriptors()
meth public java.beans.PropertyDescriptor[] getPropertyDescriptors()
supr java.beans.SimpleBeanInfo
hfds HINT_DTD_EXT,ICON_DIR_BASE,PROP_DTD_EXT

CLSS public final org.netbeans.modules.xml.DTDDataObject
cons public init(org.openide.filesystems.FileObject,org.openide.loaders.UniFileLoader) throws org.openide.loaders.DataObjectExistsException
fld public final static java.lang.String DTD_MIME_TYPE = "text/x-dtd"
innr public abstract interface static DTDCookieFactoryCreator
innr public abstract interface static DataNodeCreator
intf org.netbeans.modules.xml.XMLDataObjectLook
meth protected int associateLookup()
meth protected org.openide.nodes.Node createNodeDelegate()
meth public org.netbeans.modules.xml.cookies.DataObjectCookieManager getCookieManager()
meth public org.netbeans.modules.xml.sync.Synchronizator getSyncInterface()
meth public org.openide.nodes.Node$Cookie getCookie(java.lang.Class)
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static org.netbeans.core.spi.multiview.text.MultiViewEditorElement createMultiViewDTDElement(org.openide.util.Lookup)
supr org.openide.loaders.MultiDataObject
hfds cookieManager,refSync,serialVersionUID
hcls DTDDataNode

CLSS public abstract interface static org.netbeans.modules.xml.DTDDataObject$DTDCookieFactoryCreator
 outer org.netbeans.modules.xml.DTDDataObject
intf org.netbeans.modules.xml.cookies.CookieFactoryCreator

CLSS public abstract interface static org.netbeans.modules.xml.DTDDataObject$DataNodeCreator
 outer org.netbeans.modules.xml.DTDDataObject
meth public abstract org.openide.loaders.DataNode createDataNode(org.netbeans.modules.xml.DTDDataObject)

CLSS public final org.netbeans.modules.xml.EntityDataLoader
cons public init()
meth protected java.lang.String actionsContext()
meth protected java.lang.String defaultDisplayName()
meth protected org.openide.loaders.MultiDataObject createMultiObject(org.openide.filesystems.FileObject) throws java.io.IOException
meth protected org.openide.loaders.MultiDataObject$Entry createPrimaryEntry(org.openide.loaders.MultiDataObject,org.openide.filesystems.FileObject)
meth protected void initialize()
supr org.openide.loaders.UniFileLoader
hfds ENT_EXT,serialVersionUID

CLSS public org.netbeans.modules.xml.EntityDataLoaderBeanInfo
cons public init()
meth public java.awt.Image getIcon(int)
meth public java.beans.BeanDescriptor getBeanDescriptor()
meth public java.beans.BeanInfo[] getAdditionalBeanInfo()
meth public java.beans.EventSetDescriptor[] getEventSetDescriptors()
meth public java.beans.MethodDescriptor[] getMethodDescriptors()
meth public java.beans.PropertyDescriptor[] getPropertyDescriptors()
supr java.beans.SimpleBeanInfo
hfds ICON_DIR_BASE

CLSS public final org.netbeans.modules.xml.EntityDataObject
cons public init(org.openide.filesystems.FileObject,org.openide.loaders.UniFileLoader) throws org.openide.loaders.DataObjectExistsException
fld public final static java.lang.String MIME_TYPE = "text/xml-external-parsed-entity"
intf org.netbeans.modules.xml.XMLDataObjectLook
meth protected int associateLookup()
meth protected org.openide.nodes.Node createNodeDelegate()
meth public final org.openide.util.Lookup getLookup()
meth public org.netbeans.modules.xml.cookies.DataObjectCookieManager getCookieManager()
meth public org.netbeans.modules.xml.sync.Synchronizator getSyncInterface()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static org.netbeans.core.spi.multiview.text.MultiViewEditorElement createMultiViewDTDElement(org.openide.util.Lookup)
supr org.openide.loaders.MultiDataObject
hfds cookieManager,serialVersionUID,synchronizator
hcls EntityDataNode

CLSS public org.netbeans.modules.xml.XMLDataLoader
cons public init()
innr public static XMLFileEntry
meth protected java.lang.String actionsContext()
meth protected java.lang.String defaultDisplayName()
meth protected org.openide.filesystems.FileObject findPrimaryFile(org.openide.filesystems.FileObject)
meth protected org.openide.loaders.MultiDataObject createMultiObject(org.openide.filesystems.FileObject) throws org.openide.loaders.DataObjectExistsException
meth protected org.openide.loaders.MultiDataObject$Entry createPrimaryEntry(org.openide.loaders.MultiDataObject,org.openide.filesystems.FileObject)
meth protected org.openide.loaders.MultiDataObject$Entry createSecondaryEntry(org.openide.loaders.MultiDataObject,org.openide.filesystems.FileObject)
meth protected void initialize()
supr org.openide.loaders.UniFileLoader
hfds XMLINFO_EXT,XML_EXT,serialVersionUID

CLSS public static org.netbeans.modules.xml.XMLDataLoader$XMLFileEntry
 outer org.netbeans.modules.xml.XMLDataLoader
cons public init(org.openide.loaders.MultiDataObject,org.openide.filesystems.FileObject)
meth protected java.text.Format createFormat(org.openide.filesystems.FileObject,java.lang.String,java.lang.String)
meth public java.io.InputStream getInputStream() throws java.io.FileNotFoundException
meth public org.openide.filesystems.FileLock takeLock() throws java.io.IOException
meth public org.openide.filesystems.FileObject createFromTemplate(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public void delete() throws java.io.IOException
supr org.openide.loaders.FileEntry$Format
hfds activeReaders,disableInputStream,serialVersionUID
hcls NotifyInputStream

CLSS public org.netbeans.modules.xml.XMLDataLoaderBeanInfo
cons public init()
meth public java.awt.Image getIcon(int)
meth public java.beans.BeanInfo[] getAdditionalBeanInfo()
meth public java.beans.EventSetDescriptor[] getEventSetDescriptors()
meth public java.beans.MethodDescriptor[] getMethodDescriptors()
meth public java.beans.PropertyDescriptor[] getPropertyDescriptors()
supr java.beans.SimpleBeanInfo
hfds HINT_EXT,ICON_DIR_BASE,PROP_EXT

CLSS public final org.netbeans.modules.xml.XMLDataObject
cons public init(org.openide.filesystems.FileObject,org.openide.loaders.MultiFileLoader) throws org.openide.loaders.DataObjectExistsException
fld public final static java.lang.String MIME_PLAIN_XML = "text/plain+xml"
fld public final static java.lang.String MIME_XSD_XML = "text/xsd+xml"
innr public abstract interface static DataNodeCreator
innr public abstract interface static XMLCookieFactoryCreator
innr public static XMLDataNode
intf java.beans.PropertyChangeListener
intf org.netbeans.modules.xml.XMLDataObjectLook
meth protected int associateLookup()
meth protected org.openide.nodes.Node createNodeDelegate()
meth public org.netbeans.modules.xml.cookies.DataObjectCookieManager getCookieManager()
meth public org.netbeans.modules.xml.sync.Synchronizator getSyncInterface()
meth public org.openide.nodes.Node$Cookie getCookie(java.lang.Class)
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static org.netbeans.core.spi.multiview.text.MultiViewEditorElement createMultiViewEditorElement(org.openide.util.Lookup)
meth public static org.netbeans.core.spi.multiview.text.MultiViewEditorElement createMultiViewXSDEditorElement(org.openide.util.Lookup)
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void setModified(boolean)
meth public void updateDocument()
supr org.openide.loaders.XMLDataObject
hfds cookieManager,editorSupportFactory,refSync,serialVersionUID
hcls ViewCookieFactory,ViewSupport

CLSS public abstract interface static org.netbeans.modules.xml.XMLDataObject$DataNodeCreator
 outer org.netbeans.modules.xml.XMLDataObject
meth public abstract org.openide.loaders.DataNode createDataNode(org.netbeans.modules.xml.XMLDataObject)

CLSS public abstract interface static org.netbeans.modules.xml.XMLDataObject$XMLCookieFactoryCreator
 outer org.netbeans.modules.xml.XMLDataObject
intf org.netbeans.modules.xml.cookies.CookieFactoryCreator

CLSS public static org.netbeans.modules.xml.XMLDataObject$XMLDataNode
 outer org.netbeans.modules.xml.XMLDataObject
cons public init(org.netbeans.modules.xml.XMLDataObject)
supr org.openide.loaders.DataNode

CLSS public abstract interface org.netbeans.modules.xml.XMLDataObjectLook
meth public abstract <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public abstract boolean isModified()
meth public abstract java.lang.String getName()
meth public abstract org.netbeans.modules.xml.cookies.DataObjectCookieManager getCookieManager()
meth public abstract org.netbeans.modules.xml.sync.Synchronizator getSyncInterface()
meth public abstract org.openide.nodes.Node getNodeDelegate()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setModified(boolean)

CLSS public org.netbeans.modules.xml.actions.CheckEntityAction
 anno 0 java.lang.Deprecated()
cons public init()
intf org.netbeans.modules.xml.actions.CollectXMLAction$XMLAction
meth protected int mode()
meth protected java.lang.Class[] cookieClasses()
meth protected java.lang.String iconResource()
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CookieAction
hfds serialVersionUID

CLSS public org.netbeans.modules.xml.actions.CollectDTDAction
cons public init()
innr public abstract interface static DTDAction
meth protected java.lang.Class getActionLookClass()
meth protected void addRegisteredAction()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.netbeans.modules.xml.actions.CollectSystemAction
hfds FOLDER_PATH_DTD_ACTIONS,serialVersionUID

CLSS public abstract interface static org.netbeans.modules.xml.actions.CollectDTDAction$DTDAction
 outer org.netbeans.modules.xml.actions.CollectDTDAction

CLSS public abstract org.netbeans.modules.xml.actions.CollectSystemAction
cons public init()
fld protected final java.util.List<java.lang.Object> registeredAction
intf org.openide.util.actions.Presenter$Popup
meth protected abstract java.lang.Class getActionLookClass()
meth protected abstract void addRegisteredAction()
meth protected java.util.Collection getPossibleActions()
meth protected void addRegisteredAction(java.lang.String)
meth public javax.swing.JMenuItem getPopupPresenter()
meth public void actionPerformed(java.awt.event.ActionEvent)
supr org.openide.util.actions.SystemAction
hfds NONE,allActionsResult,serialVersionUID
hcls Menu

CLSS public org.netbeans.modules.xml.actions.CollectXMLAction
cons public init()
innr public abstract interface static XMLAction
meth protected java.lang.Class getActionLookClass()
meth protected void addRegisteredAction()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.netbeans.modules.xml.actions.CollectSystemAction
hfds FOLDER_PATH_XML_ACTIONS,serialVersionUID

CLSS public abstract interface static org.netbeans.modules.xml.actions.CollectXMLAction$XMLAction
 outer org.netbeans.modules.xml.actions.CollectXMLAction

CLSS public final org.netbeans.modules.xml.actions.InputOutputReporter
 anno 0 java.lang.Deprecated()
cons public init()
cons public init(java.lang.String)
intf org.netbeans.api.xml.cookies.CookieObserver
meth public final void moveToFront()
meth public final void moveToFront(boolean)
meth public static void releaseAllAnnotations()
meth public void message(java.lang.String)
meth public void receive(org.netbeans.api.xml.cookies.CookieMessage)
meth public void setNode(org.openide.nodes.Node)
supr java.lang.Object
hfds FORMAT,dataObject,hyperlinks,ioName
hcls Hyperlink

CLSS public final org.netbeans.modules.xml.actions.XMLUpdateDocumentAction
cons public init()
innr public abstract interface static Performer
meth protected boolean asynchronous()
meth protected int mode()
meth protected java.lang.Class[] cookieClasses()
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CookieAction
hfds serialVersionUID

CLSS public abstract interface static org.netbeans.modules.xml.actions.XMLUpdateDocumentAction$Performer
 outer org.netbeans.modules.xml.actions.XMLUpdateDocumentAction
meth public abstract void perform(org.openide.nodes.Node)

CLSS public abstract interface org.netbeans.modules.xml.axi.visitor.AXIVisitor
meth public abstract void visit(org.netbeans.modules.xml.axi.AXIDocument)
meth public abstract void visit(org.netbeans.modules.xml.axi.AnyAttribute)
meth public abstract void visit(org.netbeans.modules.xml.axi.AnyElement)
meth public abstract void visit(org.netbeans.modules.xml.axi.Attribute)
meth public abstract void visit(org.netbeans.modules.xml.axi.Compositor)
meth public abstract void visit(org.netbeans.modules.xml.axi.ContentModel)
meth public abstract void visit(org.netbeans.modules.xml.axi.Element)
meth public abstract void visit(org.netbeans.modules.xml.axi.datatype.Datatype)

CLSS public abstract interface org.netbeans.modules.xml.axi.visitor.AXIVisitor2
intf org.netbeans.modules.xml.axi.visitor.AXIVisitor
meth public abstract void visit(org.netbeans.modules.xml.axi.SchemaReference)

CLSS public org.netbeans.modules.xml.axi.visitor.DeepAXITreeVisitor
cons public init()
meth protected boolean canVisit(org.netbeans.modules.xml.axi.AXIComponent)
meth protected void visitChildren(org.netbeans.modules.xml.axi.AXIComponent)
meth public void visit(org.netbeans.modules.xml.axi.AXIDocument)
meth public void visit(org.netbeans.modules.xml.axi.AnyAttribute)
meth public void visit(org.netbeans.modules.xml.axi.AnyElement)
meth public void visit(org.netbeans.modules.xml.axi.Attribute)
meth public void visit(org.netbeans.modules.xml.axi.Compositor)
meth public void visit(org.netbeans.modules.xml.axi.ContentModel)
meth public void visit(org.netbeans.modules.xml.axi.Element)
supr org.netbeans.modules.xml.axi.visitor.DefaultVisitor
hfds pathToRoot

CLSS public abstract org.netbeans.modules.xml.axi.visitor.DefaultVisitor
cons public init()
intf org.netbeans.modules.xml.axi.visitor.AXIVisitor2
meth public void visit(org.netbeans.modules.xml.axi.AXIDocument)
meth public void visit(org.netbeans.modules.xml.axi.AnyAttribute)
meth public void visit(org.netbeans.modules.xml.axi.AnyElement)
meth public void visit(org.netbeans.modules.xml.axi.Attribute)
meth public void visit(org.netbeans.modules.xml.axi.Compositor)
meth public void visit(org.netbeans.modules.xml.axi.ContentModel)
meth public void visit(org.netbeans.modules.xml.axi.Element)
meth public void visit(org.netbeans.modules.xml.axi.SchemaReference)
meth public void visit(org.netbeans.modules.xml.axi.datatype.Datatype)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.xml.cookies.CookieFactory
cons public init()
intf org.openide.nodes.CookieSet$Factory
meth protected abstract java.lang.Class[] supportedCookies()
meth public final void registerCookies(org.openide.nodes.CookieSet)
meth public final void unregisterCookies(org.openide.nodes.CookieSet)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.xml.cookies.CookieFactoryCreator
meth public abstract org.netbeans.modules.xml.cookies.CookieFactory createCookieFactory(org.openide.loaders.DataObject)

CLSS public final org.netbeans.modules.xml.cookies.CookieManager
cons public init(org.openide.loaders.DataObject,org.openide.nodes.CookieSet,java.lang.Class)
meth protected void addedToResult(java.util.Collection)
meth protected void removedFromResult(java.util.Collection)
supr org.netbeans.modules.xml.util.LookupManager
hfds cookieSet,dataObject,factoryMap

CLSS public abstract interface org.netbeans.modules.xml.cookies.CookieManagerCookie
intf org.openide.nodes.Node$Cookie
meth public abstract void addCookie(org.openide.nodes.Node$Cookie)
meth public abstract void removeCookie(org.openide.nodes.Node$Cookie)

CLSS public org.netbeans.modules.xml.cookies.DataObjectCookieManager
cons public init(org.openide.loaders.DataObject,org.openide.nodes.CookieSet)
meth public void addCookie(org.openide.nodes.Node$Cookie)
meth public void removeCookie(org.openide.nodes.Node$Cookie)
supr org.netbeans.modules.xml.cookies.DefaultCookieManager
hfds dobj

CLSS public org.netbeans.modules.xml.cookies.DefaultCookieManager
cons public init(org.openide.nodes.CookieSet)
intf org.netbeans.modules.xml.cookies.CookieManagerCookie
meth public void addCookie(org.openide.nodes.Node$Cookie)
meth public void removeCookie(org.openide.nodes.Node$Cookie)
supr java.lang.Object
hfds set

CLSS public abstract interface org.netbeans.modules.xml.cookies.UpdateDocumentCookie
intf org.openide.nodes.Node$Cookie
meth public abstract void updateDocumentRoot()

CLSS public final org.netbeans.modules.xml.lib.A11YUtil
meth public static java.awt.event.FocusListener getA11YJTextFieldSupport()
supr java.lang.Object
hfds flis

CLSS public final org.netbeans.modules.xml.lib.FileUtilities
cons public init()
meth public static org.openide.filesystems.FileObject createFileObject(org.openide.filesystems.FileObject,java.lang.String,boolean) throws java.io.IOException
meth public static org.openide.loaders.DataObject createDataObject(org.openide.filesystems.FileObject,java.lang.String,java.lang.String,boolean) throws java.io.IOException
supr java.lang.Object

CLSS public final org.netbeans.modules.xml.lib.GuiUtil
meth public static boolean confirmAction(java.lang.String)
meth public static void notifyError(java.lang.String)
meth public static void notifyException(java.lang.String,java.lang.Throwable)
meth public static void notifyException(java.lang.Throwable)
meth public static void notifyWarning(java.lang.String)
meth public static void performDefaultAction(org.openide.filesystems.FileObject)
meth public static void performDefaultAction(org.openide.loaders.DataObject)
meth public static void setStatusText(java.lang.String)
supr java.lang.Object

CLSS public org.netbeans.modules.xml.sync.DTDSyncSupport
cons public init(org.netbeans.modules.xml.DTDDataObject)
supr org.netbeans.modules.xml.sync.DataObjectSyncSupport

CLSS public org.netbeans.modules.xml.sync.DataObjectSyncSupport
cons public init(org.netbeans.modules.xml.XMLDataObjectLook)
intf org.netbeans.modules.xml.sync.Synchronizator
meth protected org.netbeans.modules.xml.sync.Representation[] getRepresentations()
meth public org.netbeans.modules.xml.sync.Representation getPrimaryRepresentation()
meth public void addRepresentation(org.netbeans.modules.xml.sync.Representation)
meth public void removeRepresentation(org.netbeans.modules.xml.sync.Representation)
meth public void representationChanged(java.lang.Class)
supr org.netbeans.modules.xml.sync.SyncSupport
hfds cookieMgr,reps

CLSS public org.netbeans.modules.xml.sync.EntitySyncSupport
cons public init(org.netbeans.modules.xml.EntityDataObject)
supr org.netbeans.modules.xml.sync.DataObjectSyncSupport

CLSS public org.netbeans.modules.xml.sync.FileRepresentation
cons public init(org.openide.loaders.DataObject,org.netbeans.modules.xml.sync.Synchronizator)
meth public boolean isModified()
meth public boolean represents(java.lang.Class)
meth public int level()
meth public java.lang.Class getUpdateClass()
meth public java.lang.Object getChange(java.lang.Class)
meth public java.lang.String getDisplayName()
meth public void update(java.lang.Object)
supr org.netbeans.modules.xml.sync.SyncRepresentation
hfds dataObject,lastSave
hcls FileListener

CLSS public abstract interface org.netbeans.modules.xml.sync.Representation
meth public abstract boolean isModified()
meth public abstract boolean isValid()
meth public abstract boolean represents(java.lang.Class)
meth public abstract int level()
meth public abstract java.lang.Class getUpdateClass()
meth public abstract java.lang.Object getChange(java.lang.Class)
meth public abstract java.lang.String getDisplayName()
meth public abstract void update(java.lang.Object)

CLSS public abstract org.netbeans.modules.xml.sync.SyncRepresentation
cons public init(org.netbeans.modules.xml.sync.Synchronizator)
intf org.netbeans.modules.xml.sync.Representation
meth protected final org.netbeans.modules.xml.sync.Synchronizator getSynchronizator()
meth protected final void changed(java.lang.Class)
meth public boolean isValid()
supr java.lang.Object
hfds sync

CLSS public abstract org.netbeans.modules.xml.sync.SyncSupport
cons public init(org.openide.loaders.DataObject)
fld protected final static int JUST_RESOLVING_CONFLICT = 2
fld protected final static int JUST_SYNCHRONIZING = 1
fld protected final static int NOP = 0
meth protected abstract org.netbeans.modules.xml.sync.Representation[] getRepresentations()
meth protected final org.openide.loaders.DataObject getDO()
meth protected final org.openide.nodes.Node$Cookie getCookie(java.lang.Class)
meth protected org.netbeans.modules.xml.sync.Representation selectMasterRepresentation(org.netbeans.modules.xml.sync.Representation[])
meth protected org.netbeans.modules.xml.sync.Representation[] getRepresentations(java.lang.Class)
meth protected void representationChanged(java.lang.Class)
meth public abstract void addRepresentation(org.netbeans.modules.xml.sync.Representation)
meth public abstract void removeRepresentation(org.netbeans.modules.xml.sync.Representation)
meth public boolean isInSync()
meth public void postRequest(java.lang.Runnable)
supr java.lang.Object
hfds dobj,syncOperation,syncOperationLock
hcls SyncSupportLock

CLSS public abstract interface org.netbeans.modules.xml.sync.Synchronizator
meth public abstract boolean isInSync()
meth public abstract org.netbeans.modules.xml.sync.Representation getPrimaryRepresentation()
meth public abstract void addRepresentation(org.netbeans.modules.xml.sync.Representation)
meth public abstract void postRequest(java.lang.Runnable)
meth public abstract void removeRepresentation(org.netbeans.modules.xml.sync.Representation)
meth public abstract void representationChanged(java.lang.Class)

CLSS public org.netbeans.modules.xml.sync.XMLSyncSupport
cons public init(org.netbeans.modules.xml.XMLDataObject)
supr org.netbeans.modules.xml.sync.DataObjectSyncSupport

CLSS public final org.netbeans.modules.xml.text.ComplexValueSettingsFactory
meth public static org.netbeans.editor.Acceptor getDTDAbbrevResetAcceptor()
meth public static org.netbeans.editor.Acceptor getXMLAbbrevResetAcceptor()
meth public static org.netbeans.editor.Acceptor getXMLIdentifierAcceptor()
meth public static org.openide.text.IndentEngine getDTDIndentEngine()
meth public static org.openide.text.IndentEngine getXMLIndentEngine()
supr java.lang.Object
hfds abbrevResetAcceptor,xmlIdentifierAcceptor

CLSS public org.netbeans.modules.xml.text.DTDTextRepresentation
cons public init(org.netbeans.modules.xml.text.TextEditorSupport,org.netbeans.modules.xml.sync.Synchronizator)
meth public boolean isModified()
meth public void update(java.lang.Object)
supr org.netbeans.modules.xml.text.TextRepresentation

CLSS public org.netbeans.modules.xml.text.EntityTextRepresentation
cons public init(org.netbeans.modules.xml.text.TextEditorSupport,org.netbeans.modules.xml.sync.Synchronizator)
meth public boolean isModified()
meth public void update(java.lang.Object)
supr org.netbeans.modules.xml.text.TextRepresentation

CLSS public org.netbeans.modules.xml.text.TextEditorComponent
cons public init()
cons public init(org.netbeans.modules.xml.text.TextEditorSupport)
meth protected void componentActivated()
meth protected void componentDeactivated()
meth public void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr org.openide.text.CloneableEditor
hfds caretListener,serialVersionUID,support

CLSS public org.netbeans.modules.xml.text.TextEditorSupport
cons protected init(org.netbeans.modules.xml.XMLDataObjectLook,org.netbeans.modules.xml.text.TextEditorSupport$Env,java.lang.String)
cons public init(org.netbeans.modules.xml.XMLDataObjectLook,java.lang.String)
fld public final static java.lang.String PROP_DOCUMENT_URL = "doc-url"
innr protected static Env
innr public static TextEditorSupportFactory
intf org.openide.cookies.CloseCookie
intf org.openide.cookies.EditCookie
intf org.openide.cookies.EditorCookie$Observable
intf org.openide.cookies.OpenCookie
intf org.openide.cookies.PrintCookie
meth protected boolean notifyModified()
meth protected final org.openide.text.CloneableEditor openCloneableEditor()
meth protected final org.openide.windows.CloneableTopComponent createCloneableTopComponent()
meth protected java.lang.Object getLock()
meth protected org.netbeans.modules.xml.XMLDataObjectLook getXMLDataObjectLook()
meth protected org.openide.text.CloneableEditor createCloneableEditor()
meth protected org.openide.text.CloneableEditorSupport$Pane createPane()
meth protected void notifyClosed()
meth protected void notifyUnmodified()
meth protected void saveFromKitToStream(javax.swing.text.StyledDocument,javax.swing.text.EditorKit,java.io.OutputStream) throws java.io.IOException,javax.swing.text.BadLocationException
meth protected void syncDocument(boolean)
meth public final static org.netbeans.modules.xml.text.TextEditorSupport$TextEditorSupportFactory findEditorSupportFactory(org.netbeans.modules.xml.XMLDataObjectLook,java.lang.String)
meth public void saveDocument() throws java.io.IOException
meth public void setMIMEType(java.lang.String)
supr org.openide.text.DataEditorSupport
hfds awtLock,fileEncoding,mimeType,rep,timer

CLSS protected static org.netbeans.modules.xml.text.TextEditorSupport$Env
 outer org.netbeans.modules.xml.text.TextEditorSupport
cons public init(org.netbeans.modules.xml.XMLDataObjectLook)
intf org.openide.cookies.SaveCookie
meth protected org.netbeans.modules.xml.XMLDataObjectLook getXMLDataObjectLook()
meth protected org.openide.filesystems.FileLock takeLock() throws java.io.IOException
meth protected org.openide.filesystems.FileObject getFile()
meth public org.netbeans.modules.xml.text.TextEditorSupport findTextEditorSupport()
meth public org.openide.windows.CloneableOpenSupport findCloneableOpenSupport()
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void save() throws java.io.IOException
supr org.openide.text.DataEditorSupport$Env
hfds serialVersionUID

CLSS public static org.netbeans.modules.xml.text.TextEditorSupport$TextEditorSupportFactory
 outer org.netbeans.modules.xml.text.TextEditorSupport
cons public init(org.netbeans.modules.xml.XMLDataObjectLook,java.lang.String)
intf org.openide.nodes.CookieSet$Factory
meth protected final java.lang.String getMIMEType()
meth protected final org.netbeans.modules.xml.XMLDataObjectLook getDataObject()
meth protected java.lang.Class[] supportedCookies()
meth protected org.netbeans.modules.xml.text.TextEditorSupport prepareEditor()
meth public final org.netbeans.modules.xml.text.TextEditorSupport createEditor()
meth public final org.openide.nodes.Node$Cookie createCookie(java.lang.Class)
meth public final void registerCookies(org.openide.nodes.CookieSet)
supr java.lang.Object
hfds dataObject,editorRef,mime

CLSS public abstract org.netbeans.modules.xml.text.TextRepresentation
cons public init(org.netbeans.modules.xml.text.TextEditorSupport,org.netbeans.modules.xml.sync.Synchronizator)
fld protected final org.netbeans.modules.xml.text.TextEditorSupport editor
meth public boolean represents(java.lang.Class)
meth public int level()
meth public java.lang.Class getUpdateClass()
meth public java.lang.Object getChange(java.lang.Class)
meth public java.lang.String getDisplayName()
supr org.netbeans.modules.xml.sync.SyncRepresentation

CLSS public org.netbeans.modules.xml.text.XMLTextRepresentation
cons public init(org.netbeans.modules.xml.text.TextEditorSupport,org.netbeans.modules.xml.sync.Synchronizator)
meth public boolean isModified()
meth public void update(java.lang.Object)
meth public void updateText(java.lang.Object)
supr org.netbeans.modules.xml.text.TextRepresentation

CLSS public org.netbeans.modules.xml.text.XmlCommentHandler
cons public init()
meth public java.lang.String getCommentEndDelimiter()
meth public java.lang.String getCommentStartDelimiter()
supr org.netbeans.modules.csl.spi.CommentHandler$DefaultCommentHandler
hfds COMMENT_END_DELIMITER,COMMENT_START_DELIMITER

CLSS public abstract org.netbeans.modules.xml.util.LookupManager
cons public init()
meth protected abstract void addedToResult(java.util.Collection)
meth protected abstract void removedFromResult(java.util.Collection)
meth protected final java.util.Collection getResult()
meth protected final void register(java.lang.Class)
supr java.lang.Object
hfds CHANGES,handle,handles
hcls Handle

CLSS public abstract org.netbeans.modules.xml.wizard.AbstractPanel
cons public init()
fld protected org.netbeans.modules.xml.wizard.DocumentModel model
innr public static WizardStep
intf java.beans.Customizer
meth protected abstract void initView()
meth protected abstract void updateModel()
meth protected abstract void updateView()
meth protected final org.netbeans.modules.xml.wizard.AbstractPanel$WizardStep getStep()
meth protected static boolean not(boolean)
meth protected void fireChange()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void setObject(java.lang.Object)
supr javax.swing.JPanel
hfds listeners,serialVersionUID,step

CLSS public static org.netbeans.modules.xml.wizard.AbstractPanel$WizardStep
 outer org.netbeans.modules.xml.wizard.AbstractPanel
cons public init(org.netbeans.modules.xml.wizard.AbstractPanel)
intf javax.swing.event.ChangeListener
intf org.openide.WizardDescriptor$Panel
meth protected final void fireChangeEvent()
meth protected final void setValid(boolean)
meth public boolean isValid()
meth public final org.openide.util.HelpCtx getHelp()
meth public final void addChangeListener(javax.swing.event.ChangeListener)
meth public final void removeChangeListener(javax.swing.event.ChangeListener)
meth public java.awt.Component getComponent()
meth public void readSettings(java.lang.Object)
meth public void stateChanged(javax.swing.event.ChangeEvent)
meth public void storeSettings(java.lang.Object)
supr java.lang.Object
hfds EVENT,listeners,peer,valid

CLSS public final org.netbeans.modules.xml.wizard.DocumentModel
cons public init(java.net.URL)
fld public final static int DTD = 1
fld public final static int NONE = 0
fld public final static int OTHER = 3
fld public final static int SCHEMA = 2
fld public final static java.lang.String PROP_TYPE = "type"
meth public int getType()
meth public java.lang.String getName()
meth public java.lang.String getNamespace()
meth public java.lang.String getPrefix()
meth public java.lang.String getPrimarySchema()
meth public java.lang.String getPublicID()
meth public java.lang.String getRoot()
meth public java.lang.String getSystemID()
meth public java.net.URL getTargetFolderURL()
meth public java.util.List getSchemaNodes()
meth public org.netbeans.modules.xml.wizard.XMLContentAttributes getXMLContentAttributes()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setName(java.lang.String)
meth public void setNamespace(java.lang.String)
meth public void setPrefix(java.lang.String)
meth public void setPrimarySchema(java.lang.String)
meth public void setPublicID(java.lang.String)
meth public void setRoot(java.lang.String)
meth public void setSchemaNodes(java.util.List)
meth public void setSystemID(java.lang.String)
meth public void setType(int)
meth public void setXMLContentAttributes(org.netbeans.modules.xml.wizard.XMLContentAttributes)
supr java.lang.Object
hfds contentAttr,name,namespace,prefix,primarySchemaFileName,publicID,root,schemaNodes,support,systemID,targetFolderURL,type

CLSS public final org.netbeans.modules.xml.wizard.SchemaParser
cons public init()
innr public final static SchemaInfo
meth public org.netbeans.modules.xml.wizard.SchemaParser$SchemaInfo parse(java.lang.String)
meth public org.netbeans.modules.xml.wizard.SchemaParser$SchemaInfo parse(org.xml.sax.InputSource)
meth public static java.lang.String getNamespace(org.openide.filesystems.FileObject)
meth public static org.netbeans.modules.xml.wizard.SchemaParser$SchemaInfo getRootElements(org.openide.filesystems.FileObject)
meth public void endElement(java.lang.String,java.lang.String,java.lang.String)
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
supr org.xml.sax.helpers.DefaultHandler
hfds depth,info

CLSS public final static org.netbeans.modules.xml.wizard.SchemaParser$SchemaInfo
 outer org.netbeans.modules.xml.wizard.SchemaParser
cons public init()
fld public final java.util.Set roots
fld public java.lang.String namespace
supr java.lang.Object

CLSS public org.netbeans.modules.xml.wizard.XMLContentAttributes
cons public init(java.lang.String)
meth public boolean generateOptionalAttributes()
meth public boolean generateOptionalElements()
meth public int getDepthPreferrence()
meth public int getPreferredOccurences()
meth public java.lang.String getPrefix()
meth public java.util.Map<java.lang.String,java.lang.String> getNamespaceToPrefixMap()
meth public void setDepthPreferrence(int)
meth public void setNamespaceToPrefixMap(java.util.Map<java.lang.String,java.lang.String>)
meth public void setOptionalAttributes(boolean)
meth public void setOptionalElements(boolean)
meth public void setPreferredOccurences(int)
meth public void setPrefix(java.lang.String)
supr java.lang.Object
hfds DEPTH,PREFERRED,nsToPre,optionalAttributes,optionalElements,prefix

CLSS public org.netbeans.modules.xml.wizard.XMLContentPanel
cons public init()
cons public init(boolean)
meth protected void initView()
meth protected void updateModel()
meth protected void updateView()
meth public boolean isPanelValid()
meth public java.lang.String getName()
supr org.netbeans.modules.xml.wizard.AbstractPanel
hfds attributes,depthModel,depthSpinner,elements,jLabel1,jLabel2,jLabel3,jLabel4,jLabel5,jLabel6,jLabel7,jSeparator1,jSeparator2,occurSpinner,occurencesModel,rootElementComboBox,rootModel,schemaInfo,titleLabel,visible

CLSS public org.netbeans.modules.xml.wizard.XMLGeneratorVisitor
cons public init(java.lang.String,org.netbeans.modules.xml.wizard.XMLContentAttributes,java.lang.StringBuffer)
meth protected void visitChildren(org.netbeans.modules.xml.axi.AXIComponent)
meth protected void visitChildrenForXML(org.netbeans.modules.xml.axi.AXIComponent)
meth public void generateXML(java.lang.String)
meth public void generateXML(java.lang.String,org.netbeans.modules.xml.schema.model.SchemaModel)
meth public void generateXML(org.netbeans.modules.xml.axi.Element)
meth public void visit(org.netbeans.modules.xml.axi.Element)
supr org.netbeans.modules.xml.axi.visitor.DeepAXITreeVisitor
hfds LOG,PREFIX,attrPrefix,axiModel,blockExpansion,contentAttr,counter,defaultPrefix,depth,elemPrefix,machineIncluded,namespaceToPrefix,nestingStack,parentSkippable,primaryTNS,qualifiedElem,rElement,schemaFileName,writer

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

CLSS public abstract interface org.openide.cookies.CloseCookie
intf org.netbeans.api.actions.Closable
intf org.openide.nodes.Node$Cookie

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

CLSS public abstract interface org.openide.cookies.SaveCookie
intf org.netbeans.api.actions.Savable
intf org.openide.nodes.Node$Cookie

CLSS public abstract org.openide.loaders.DataLoader
cons protected init(java.lang.Class<? extends org.openide.loaders.DataObject>)
 anno 0 java.lang.Deprecated()
cons protected init(java.lang.String)
fld public final static java.lang.String PROP_ACTIONS = "actions"
fld public final static java.lang.String PROP_DISPLAY_NAME = "displayName"
innr public abstract interface static RecognizedFiles
intf org.openide.loaders.DataObject$Factory
meth protected abstract org.openide.loaders.DataObject handleFindDataObject(org.openide.filesystems.FileObject,org.openide.loaders.DataLoader$RecognizedFiles) throws java.io.IOException
meth protected boolean clearSharedData()
meth protected final void setDisplayName(java.lang.String)
meth protected java.lang.String actionsContext()
meth protected java.lang.String defaultDisplayName()
meth protected org.openide.util.actions.SystemAction[] defaultActions()
 anno 0 java.lang.Deprecated()
meth public final java.lang.Class<? extends org.openide.loaders.DataObject> getRepresentationClass()
meth public final java.lang.String getDisplayName()
meth public final java.lang.String getRepresentationClassName()
meth public final org.openide.loaders.DataObject findDataObject(org.openide.filesystems.FileObject,java.util.Set<? super org.openide.filesystems.FileObject>) throws java.io.IOException
meth public final org.openide.loaders.DataObject findDataObject(org.openide.filesystems.FileObject,org.openide.loaders.DataLoader$RecognizedFiles) throws java.io.IOException
meth public final org.openide.util.actions.SystemAction[] getActions()
meth public final void markFile(org.openide.filesystems.FileObject) throws java.io.IOException
meth public final void setActions(org.openide.util.actions.SystemAction[])
meth public static <%0 extends org.openide.loaders.DataLoader> {%%0} getLoader(java.lang.Class<{%%0}>)
meth public void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr org.openide.util.SharedClassObject
hfds ACTION_MANAGER,ERR,LOADER_VERSION,PROP_DEF_ACTIONS,PROP_REPRESENTATION_CLASS,PROP_REPRESENTATION_CLASS_NAME,serialVersionUID

CLSS public org.openide.loaders.DataNode
cons public init(org.openide.loaders.DataObject,org.openide.nodes.Children)
cons public init(org.openide.loaders.DataObject,org.openide.nodes.Children,org.openide.util.Lookup)
meth protected org.openide.nodes.Sheet createSheet()
meth protected org.openide.util.actions.SystemAction[] createActions()
 anno 0 java.lang.Deprecated()
meth protected void createPasteTypes(java.awt.datatransfer.Transferable,java.util.List<org.openide.util.datatransfer.PasteType>)
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public boolean canCopy()
meth public boolean canCut()
meth public boolean canDestroy()
meth public boolean canRename()
meth public java.awt.Image getIcon(int)
meth public java.awt.Image getOpenedIcon(int)
meth public java.awt.datatransfer.Transferable clipboardCopy() throws java.io.IOException
meth public java.awt.datatransfer.Transferable clipboardCut() throws java.io.IOException
meth public java.lang.Object getValue(java.lang.String)
meth public java.lang.String getDisplayName()
meth public java.lang.String getHtmlDisplayName()
meth public javax.swing.Action getPreferredAction()
meth public javax.swing.Action[] getActions(boolean)
meth public org.openide.loaders.DataObject getDataObject()
meth public org.openide.nodes.Node$Handle getHandle()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public org.openide.util.actions.SystemAction[] getActions()
 anno 0 java.lang.Deprecated()
meth public static boolean getShowFileExtensions()
meth public static void setShowFileExtensions(boolean)
meth public void destroy() throws java.io.IOException
meth public void setName(java.lang.String)
meth public void setName(java.lang.String,boolean)
supr org.openide.nodes.AbstractNode
hfds PROP_EXTENSION,defaultLookup,obj,propL,refreshIconNodes,refreshNameIconLock,refreshNameNodes,refreshNamesIconsRunning,refreshNamesIconsTask,serialVersionUID,showFileExtensions
hcls AllFilesProperty,ExtensionProperty,LastModifiedProperty,LazyFilesSet,NamesUpdater,ObjectHandle,PropL,SizeProperty

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

CLSS public abstract interface static org.openide.loaders.DataObject$Factory
 outer org.openide.loaders.DataObject
meth public abstract org.openide.loaders.DataObject findDataObject(org.openide.filesystems.FileObject,java.util.Set<? super org.openide.filesystems.FileObject>) throws java.io.IOException

CLSS public org.openide.loaders.FileEntry
cons public init(org.openide.loaders.MultiDataObject,org.openide.filesystems.FileObject)
innr public abstract static Format
innr public final static Folder
innr public final static Numb
meth public org.openide.filesystems.FileObject copy(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public org.openide.filesystems.FileObject copyRename(org.openide.filesystems.FileObject,java.lang.String,java.lang.String) throws java.io.IOException
meth public org.openide.filesystems.FileObject createFromTemplate(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public org.openide.filesystems.FileObject move(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public org.openide.filesystems.FileObject rename(java.lang.String) throws java.io.IOException
meth public void delete() throws java.io.IOException
supr org.openide.loaders.MultiDataObject$Entry
hfds serialVersionUID

CLSS public abstract static org.openide.loaders.FileEntry$Format
 outer org.openide.loaders.FileEntry
cons public init(org.openide.loaders.MultiDataObject,org.openide.filesystems.FileObject)
meth protected abstract java.text.Format createFormat(org.openide.filesystems.FileObject,java.lang.String,java.lang.String)
meth public org.openide.filesystems.FileObject createFromTemplate(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
supr org.openide.loaders.FileEntry
hfds serialVersionUID

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

CLSS public abstract org.openide.loaders.MultiDataObject$Entry
 outer org.openide.loaders.MultiDataObject
cons protected init(org.openide.loaders.MultiDataObject,org.openide.filesystems.FileObject)
intf java.io.Serializable
meth protected java.lang.Object writeReplace()
meth public abstract org.openide.filesystems.FileObject copy(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public abstract org.openide.filesystems.FileObject createFromTemplate(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public abstract org.openide.filesystems.FileObject move(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public abstract org.openide.filesystems.FileObject rename(java.lang.String) throws java.io.IOException
meth public abstract void delete() throws java.io.IOException
meth public boolean equals(java.lang.Object)
meth public boolean isImportant()
meth public boolean isLocked()
meth public final org.openide.filesystems.FileObject getFile()
meth public final org.openide.loaders.MultiDataObject getDataObject()
meth public int hashCode()
meth public org.openide.filesystems.FileLock takeLock() throws java.io.IOException
meth public org.openide.filesystems.FileObject copyRename(org.openide.filesystems.FileObject,java.lang.String,java.lang.String) throws java.io.IOException
supr java.lang.Object
hfds file,lock,serialVersionUID

CLSS public abstract org.openide.loaders.MultiFileLoader
cons protected init(java.lang.Class<? extends org.openide.loaders.DataObject>)
 anno 0 java.lang.Deprecated()
cons protected init(java.lang.String)
meth protected abstract org.openide.filesystems.FileObject findPrimaryFile(org.openide.filesystems.FileObject)
meth protected abstract org.openide.loaders.MultiDataObject createMultiObject(org.openide.filesystems.FileObject) throws java.io.IOException
meth protected abstract org.openide.loaders.MultiDataObject$Entry createPrimaryEntry(org.openide.loaders.MultiDataObject,org.openide.filesystems.FileObject)
meth protected abstract org.openide.loaders.MultiDataObject$Entry createSecondaryEntry(org.openide.loaders.MultiDataObject,org.openide.filesystems.FileObject)
meth protected final org.openide.loaders.DataObject handleFindDataObject(org.openide.filesystems.FileObject,org.openide.loaders.DataLoader$RecognizedFiles) throws java.io.IOException
supr org.openide.loaders.DataLoader
hfds serialVersionUID

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

CLSS public abstract org.openide.loaders.UniFileLoader
cons protected init(java.lang.Class<? extends org.openide.loaders.DataObject>)
 anno 0 java.lang.Deprecated()
cons protected init(java.lang.String)
fld public final static java.lang.String PROP_EXTENSIONS = "extensions"
meth protected abstract org.openide.loaders.MultiDataObject createMultiObject(org.openide.filesystems.FileObject) throws java.io.IOException
meth protected org.openide.filesystems.FileObject findPrimaryFile(org.openide.filesystems.FileObject)
meth protected org.openide.loaders.MultiDataObject$Entry createPrimaryEntry(org.openide.loaders.MultiDataObject,org.openide.filesystems.FileObject)
meth protected org.openide.loaders.MultiDataObject$Entry createSecondaryEntry(org.openide.loaders.MultiDataObject,org.openide.filesystems.FileObject)
meth public org.openide.loaders.ExtensionList getExtensions()
meth public void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void setExtensions(org.openide.loaders.ExtensionList)
meth public void writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr org.openide.loaders.MultiFileLoader
hfds serialVersionUID

CLSS public org.openide.loaders.XMLDataObject
cons protected init(org.openide.filesystems.FileObject,org.openide.loaders.MultiFileLoader,boolean) throws org.openide.loaders.DataObjectExistsException
cons public init(org.openide.filesystems.FileObject,org.openide.loaders.MultiFileLoader) throws org.openide.loaders.DataObjectExistsException
fld public final static int STATUS_ERROR = 3
fld public final static int STATUS_NOT = 0
fld public final static int STATUS_OK = 1
fld public final static int STATUS_WARNING = 2
fld public final static java.lang.String MIME = "text/xml"
fld public final static java.lang.String PROP_DOCUMENT = "document"
fld public final static java.lang.String PROP_INFO = "info"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String XMLINFO_DTD_PUBLIC_ID = "-//NetBeans IDE//DTD xmlinfo//EN"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String XMLINFO_DTD_PUBLIC_ID_FORTE = "-//Forte for Java//DTD xmlinfo//EN"
 anno 0 java.lang.Deprecated()
innr public abstract interface static Processor
innr public final static Info
meth protected org.openide.cookies.EditorCookie createEditorCookie()
 anno 0 java.lang.Deprecated()
meth protected org.openide.nodes.Node createNodeDelegate()
meth protected void handleDelete() throws java.io.IOException
meth protected void updateIconBase(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public final int getStatus()
meth public final org.openide.loaders.XMLDataObject$Info getInfo()
 anno 0 java.lang.Deprecated()
meth public final org.w3c.dom.Document getDocument() throws java.io.IOException,org.xml.sax.SAXException
meth public final void setInfo(org.openide.loaders.XMLDataObject$Info) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public org.openide.util.Lookup getLookup()
meth public static boolean addEntityResolver(org.xml.sax.EntityResolver)
 anno 0 java.lang.Deprecated()
meth public static org.openide.loaders.XMLDataObject$Info getRegisteredInfo(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static org.w3c.dom.Document createDocument()
 anno 0 java.lang.Deprecated()
meth public static org.w3c.dom.Document parse(java.net.URL) throws java.io.IOException,org.xml.sax.SAXException
 anno 0 java.lang.Deprecated()
meth public static org.w3c.dom.Document parse(java.net.URL,boolean) throws java.io.IOException,org.xml.sax.SAXException
 anno 0 java.lang.Deprecated()
meth public static org.w3c.dom.Document parse(java.net.URL,org.xml.sax.ErrorHandler) throws java.io.IOException,org.xml.sax.SAXException
 anno 0 java.lang.Deprecated()
meth public static org.w3c.dom.Document parse(java.net.URL,org.xml.sax.ErrorHandler,boolean) throws java.io.IOException,org.xml.sax.SAXException
 anno 0 java.lang.Deprecated()
meth public static org.xml.sax.EntityResolver removeEntityResolver(org.xml.sax.EntityResolver)
 anno 0 java.lang.Deprecated()
meth public static org.xml.sax.InputSource createInputSource(java.net.URL) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public static org.xml.sax.Parser createParser()
 anno 0 java.lang.Deprecated()
meth public static org.xml.sax.Parser createParser(boolean)
 anno 0 java.lang.Deprecated()
meth public static void registerCatalogEntry(java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static void registerCatalogEntry(java.lang.String,java.lang.String,java.lang.ClassLoader)
 anno 0 java.lang.Deprecated()
meth public static void registerInfo(java.lang.String,org.openide.loaders.XMLDataObject$Info)
 anno 0 java.lang.Deprecated()
meth public static void write(org.w3c.dom.Document,java.io.OutputStream,java.lang.String) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public static void write(org.w3c.dom.Document,java.io.Writer) throws java.io.IOException
 anno 0 java.lang.Deprecated()
supr org.openide.loaders.MultiDataObject
hfds ERR,chainingEntityResolver,cnstr,doc,editor,emgrLock,errorHandler,infoParser,infos,serialVersionUID,status
hcls DelDoc,ErrorPrinter,ICDel,InfoLkp,Loader,NullHandler,PlainDataNode,XMLEditorSupport,XMLNode

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

CLSS public org.openide.text.CloneableEditor
cons public init()
cons public init(org.openide.text.CloneableEditorSupport)
cons public init(org.openide.text.CloneableEditorSupport,boolean)
fld protected javax.swing.JEditorPane pane
intf org.openide.text.CloneableEditorSupport$Pane
meth protected boolean closeLast()
meth protected final boolean closeLast(boolean)
meth protected final void initializeBySupport()
meth protected java.lang.Object readResolve() throws java.io.ObjectStreamException
meth protected java.lang.Object writeReplace() throws java.io.ObjectStreamException
meth protected java.lang.String preferredID()
meth protected org.openide.text.CloneableEditorSupport cloneableEditorSupport()
meth protected org.openide.windows.CloneableTopComponent createClonedObject()
meth protected void componentActivated()
meth protected void componentClosed()
meth protected void componentOpened()
meth protected void componentShowing()
meth public boolean canClose()
meth public boolean requestDefaultFocus()
 anno 0 java.lang.Deprecated()
meth public boolean requestFocusInWindow()
 anno 0 java.lang.Deprecated()
meth public int getPersistenceType()
meth public java.awt.Dimension getPreferredSize()
meth public javax.swing.Action[] getActions()
meth public javax.swing.JEditorPane getEditorPane()
meth public org.openide.awt.UndoRedo getUndoRedo()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public org.openide.windows.CloneableTopComponent getComponent()
meth public void ensureVisible()
meth public void open()
meth public void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void requestFocus()
 anno 0 java.lang.Deprecated()
meth public void updateName()
meth public void writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr org.openide.windows.CloneableTopComponent
hfds CLOSE_LAST_LOCK,HELP_ID,LOG,componentCreated,cursorPosition,customComponent,initializer,serialVersionUID,support

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

CLSS public abstract interface static org.openide.text.CloneableEditorSupport$Pane
 outer org.openide.text.CloneableEditorSupport
meth public abstract javax.swing.JEditorPane getEditorPane()
meth public abstract org.openide.windows.CloneableTopComponent getComponent()
meth public abstract void ensureVisible()
meth public abstract void updateName()

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

CLSS public abstract org.openide.windows.CloneableTopComponent
cons public init()
fld public final static org.openide.windows.CloneableTopComponent$Ref EMPTY
innr public static Ref
intf java.io.Externalizable
intf org.openide.windows.TopComponent$Cloneable
meth protected boolean closeLast()
meth protected org.openide.windows.CloneableTopComponent createClonedObject()
meth protected void componentClosed()
meth protected void componentOpened()
meth public boolean canClose()
meth public boolean canClose(org.openide.windows.Workspace,boolean)
meth public final java.lang.Object clone()
meth public final org.openide.windows.CloneableTopComponent cloneTopComponent()
meth public final org.openide.windows.CloneableTopComponent$Ref getReference()
meth public final org.openide.windows.TopComponent cloneComponent()
meth public final void setReference(org.openide.windows.CloneableTopComponent$Ref)
meth public void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr org.openide.windows.TopComponent
hfds isLastActivated,ref,serialVersionUID

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

CLSS public abstract interface static org.openide.windows.TopComponent$Cloneable
 outer org.openide.windows.TopComponent
meth public abstract org.openide.windows.TopComponent cloneComponent()

CLSS public abstract interface static !annotation org.openide.windows.TopComponent$Description
 outer org.openide.windows.TopComponent
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int persistenceType()
meth public abstract !hasdefault java.lang.String iconBase()
meth public abstract java.lang.String preferredID()

CLSS public abstract interface org.xml.sax.ContentHandler
meth public abstract void characters(char[],int,int) throws org.xml.sax.SAXException
meth public abstract void endDocument() throws org.xml.sax.SAXException
meth public abstract void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void endPrefixMapping(java.lang.String) throws org.xml.sax.SAXException
meth public abstract void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public abstract void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void setDocumentLocator(org.xml.sax.Locator)
meth public abstract void skippedEntity(java.lang.String) throws org.xml.sax.SAXException
meth public abstract void startDocument() throws org.xml.sax.SAXException
meth public abstract void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public abstract void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.DTDHandler
meth public abstract void notationDecl(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void unparsedEntityDecl(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.EntityResolver
meth public abstract org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.ErrorHandler
meth public abstract void error(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public abstract void fatalError(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public abstract void warning(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException

CLSS public org.xml.sax.helpers.DefaultHandler
cons public init()
intf org.xml.sax.ContentHandler
intf org.xml.sax.DTDHandler
intf org.xml.sax.EntityResolver
intf org.xml.sax.ErrorHandler
meth public org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public void characters(char[],int,int) throws org.xml.sax.SAXException
meth public void endDocument() throws org.xml.sax.SAXException
meth public void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void endPrefixMapping(java.lang.String) throws org.xml.sax.SAXException
meth public void error(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void fatalError(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public void notationDecl(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void setDocumentLocator(org.xml.sax.Locator)
meth public void skippedEntity(java.lang.String) throws org.xml.sax.SAXException
meth public void startDocument() throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void unparsedEntityDecl(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void warning(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
supr java.lang.Object

