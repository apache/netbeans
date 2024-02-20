#Signature file v4.1
#Version 1.110.0

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

CLSS public abstract interface !annotation java.lang.FunctionalInterface
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
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

CLSS public abstract interface java.lang.Runnable
 anno 0 java.lang.FunctionalInterface()
meth public abstract void run()

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

CLSS public abstract interface java.util.concurrent.Callable<%0 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
meth public abstract {java.util.concurrent.Callable%0} call() throws java.lang.Exception

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

CLSS public abstract interface javax.swing.MenuElement
meth public abstract java.awt.Component getComponent()
meth public abstract javax.swing.MenuElement[] getSubElements()
meth public abstract void menuSelectionChanged(boolean)
meth public abstract void processKeyEvent(java.awt.event.KeyEvent,javax.swing.MenuElement[],javax.swing.MenuSelectionManager)
meth public abstract void processMouseEvent(java.awt.event.MouseEvent,javax.swing.MenuElement[],javax.swing.MenuSelectionManager)

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

CLSS public abstract interface javax.swing.event.ChangeListener
intf java.util.EventListener
meth public abstract void stateChanged(javax.swing.event.ChangeEvent)

CLSS public abstract javax.swing.text.AbstractDocument
cons protected init(javax.swing.text.AbstractDocument$Content)
cons protected init(javax.swing.text.AbstractDocument$Content,javax.swing.text.AbstractDocument$AttributeContext)
fld protected final static java.lang.String BAD_LOCATION = "document location failure"
fld protected javax.swing.event.EventListenerList listenerList
fld public final static java.lang.String BidiElementName = "bidi level"
fld public final static java.lang.String ContentElementName = "content"
fld public final static java.lang.String ElementNameAttribute = "$ename"
fld public final static java.lang.String ParagraphElementName = "paragraph"
fld public final static java.lang.String SectionElementName = "section"
innr public BranchElement
innr public DefaultDocumentEvent
innr public LeafElement
innr public abstract AbstractElement
innr public abstract interface static AttributeContext
innr public abstract interface static Content
innr public static ElementEdit
intf java.io.Serializable
intf javax.swing.text.Document
meth protected final java.lang.Thread getCurrentWriter()
meth protected final javax.swing.text.AbstractDocument$AttributeContext getAttributeContext()
meth protected final javax.swing.text.AbstractDocument$Content getContent()
meth protected final void writeLock()
meth protected final void writeUnlock()
meth protected javax.swing.text.Element createBranchElement(javax.swing.text.Element,javax.swing.text.AttributeSet)
meth protected javax.swing.text.Element createLeafElement(javax.swing.text.Element,javax.swing.text.AttributeSet,int,int)
meth protected void fireChangedUpdate(javax.swing.event.DocumentEvent)
meth protected void fireInsertUpdate(javax.swing.event.DocumentEvent)
meth protected void fireRemoveUpdate(javax.swing.event.DocumentEvent)
meth protected void fireUndoableEditUpdate(javax.swing.event.UndoableEditEvent)
meth protected void insertUpdate(javax.swing.text.AbstractDocument$DefaultDocumentEvent,javax.swing.text.AttributeSet)
meth protected void postRemoveUpdate(javax.swing.text.AbstractDocument$DefaultDocumentEvent)
meth protected void removeUpdate(javax.swing.text.AbstractDocument$DefaultDocumentEvent)
meth public <%0 extends java.util.EventListener> {%%0}[] getListeners(java.lang.Class<{%%0}>)
meth public abstract javax.swing.text.Element getDefaultRootElement()
meth public abstract javax.swing.text.Element getParagraphElement(int)
meth public final java.lang.Object getProperty(java.lang.Object)
meth public final javax.swing.text.Position getEndPosition()
meth public final javax.swing.text.Position getStartPosition()
meth public final void putProperty(java.lang.Object,java.lang.Object)
meth public final void readLock()
meth public final void readUnlock()
meth public int getAsynchronousLoadPriority()
meth public int getLength()
meth public java.lang.String getText(int,int) throws javax.swing.text.BadLocationException
meth public java.util.Dictionary<java.lang.Object,java.lang.Object> getDocumentProperties()
meth public javax.swing.event.DocumentListener[] getDocumentListeners()
meth public javax.swing.event.UndoableEditListener[] getUndoableEditListeners()
meth public javax.swing.text.DocumentFilter getDocumentFilter()
meth public javax.swing.text.Element getBidiRootElement()
meth public javax.swing.text.Element[] getRootElements()
meth public javax.swing.text.Position createPosition(int) throws javax.swing.text.BadLocationException
meth public void addDocumentListener(javax.swing.event.DocumentListener)
meth public void addUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public void dump(java.io.PrintStream)
meth public void getText(int,int,javax.swing.text.Segment) throws javax.swing.text.BadLocationException
meth public void insertString(int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth public void remove(int,int) throws javax.swing.text.BadLocationException
meth public void removeDocumentListener(javax.swing.event.DocumentListener)
meth public void removeUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public void render(java.lang.Runnable)
meth public void replace(int,int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth public void setAsynchronousLoadPriority(int)
meth public void setDocumentFilter(javax.swing.text.DocumentFilter)
meth public void setDocumentProperties(java.util.Dictionary<java.lang.Object,java.lang.Object>)
supr java.lang.Object

CLSS public javax.swing.text.DefaultEditorKit
cons public init()
fld public final static java.lang.String EndOfLineStringProperty = "__EndOfLine__"
fld public final static java.lang.String backwardAction = "caret-backward"
fld public final static java.lang.String beepAction = "beep"
fld public final static java.lang.String beginAction = "caret-begin"
fld public final static java.lang.String beginLineAction = "caret-begin-line"
fld public final static java.lang.String beginParagraphAction = "caret-begin-paragraph"
fld public final static java.lang.String beginWordAction = "caret-begin-word"
fld public final static java.lang.String copyAction = "copy-to-clipboard"
fld public final static java.lang.String cutAction = "cut-to-clipboard"
fld public final static java.lang.String defaultKeyTypedAction = "default-typed"
fld public final static java.lang.String deleteNextCharAction = "delete-next"
fld public final static java.lang.String deleteNextWordAction = "delete-next-word"
fld public final static java.lang.String deletePrevCharAction = "delete-previous"
fld public final static java.lang.String deletePrevWordAction = "delete-previous-word"
fld public final static java.lang.String downAction = "caret-down"
fld public final static java.lang.String endAction = "caret-end"
fld public final static java.lang.String endLineAction = "caret-end-line"
fld public final static java.lang.String endParagraphAction = "caret-end-paragraph"
fld public final static java.lang.String endWordAction = "caret-end-word"
fld public final static java.lang.String forwardAction = "caret-forward"
fld public final static java.lang.String insertBreakAction = "insert-break"
fld public final static java.lang.String insertContentAction = "insert-content"
fld public final static java.lang.String insertTabAction = "insert-tab"
fld public final static java.lang.String nextWordAction = "caret-next-word"
fld public final static java.lang.String pageDownAction = "page-down"
fld public final static java.lang.String pageUpAction = "page-up"
fld public final static java.lang.String pasteAction = "paste-from-clipboard"
fld public final static java.lang.String previousWordAction = "caret-previous-word"
fld public final static java.lang.String readOnlyAction = "set-read-only"
fld public final static java.lang.String selectAllAction = "select-all"
fld public final static java.lang.String selectLineAction = "select-line"
fld public final static java.lang.String selectParagraphAction = "select-paragraph"
fld public final static java.lang.String selectWordAction = "select-word"
fld public final static java.lang.String selectionBackwardAction = "selection-backward"
fld public final static java.lang.String selectionBeginAction = "selection-begin"
fld public final static java.lang.String selectionBeginLineAction = "selection-begin-line"
fld public final static java.lang.String selectionBeginParagraphAction = "selection-begin-paragraph"
fld public final static java.lang.String selectionBeginWordAction = "selection-begin-word"
fld public final static java.lang.String selectionDownAction = "selection-down"
fld public final static java.lang.String selectionEndAction = "selection-end"
fld public final static java.lang.String selectionEndLineAction = "selection-end-line"
fld public final static java.lang.String selectionEndParagraphAction = "selection-end-paragraph"
fld public final static java.lang.String selectionEndWordAction = "selection-end-word"
fld public final static java.lang.String selectionForwardAction = "selection-forward"
fld public final static java.lang.String selectionNextWordAction = "selection-next-word"
fld public final static java.lang.String selectionPreviousWordAction = "selection-previous-word"
fld public final static java.lang.String selectionUpAction = "selection-up"
fld public final static java.lang.String upAction = "caret-up"
fld public final static java.lang.String writableAction = "set-writable"
innr public static BeepAction
innr public static CopyAction
innr public static CutAction
innr public static DefaultKeyTypedAction
innr public static InsertBreakAction
innr public static InsertContentAction
innr public static InsertTabAction
innr public static PasteAction
meth public java.lang.String getContentType()
meth public javax.swing.Action[] getActions()
meth public javax.swing.text.Caret createCaret()
meth public javax.swing.text.Document createDefaultDocument()
meth public javax.swing.text.ViewFactory getViewFactory()
meth public void read(java.io.InputStream,javax.swing.text.Document,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public void read(java.io.Reader,javax.swing.text.Document,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public void write(java.io.OutputStream,javax.swing.text.Document,int,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public void write(java.io.Writer,javax.swing.text.Document,int,int) throws java.io.IOException,javax.swing.text.BadLocationException
supr javax.swing.text.EditorKit

CLSS public abstract interface javax.swing.text.Document
fld public final static java.lang.String StreamDescriptionProperty = "stream"
fld public final static java.lang.String TitleProperty = "title"
meth public abstract int getLength()
meth public abstract java.lang.Object getProperty(java.lang.Object)
meth public abstract java.lang.String getText(int,int) throws javax.swing.text.BadLocationException
meth public abstract javax.swing.text.Element getDefaultRootElement()
meth public abstract javax.swing.text.Element[] getRootElements()
meth public abstract javax.swing.text.Position createPosition(int) throws javax.swing.text.BadLocationException
meth public abstract javax.swing.text.Position getEndPosition()
meth public abstract javax.swing.text.Position getStartPosition()
meth public abstract void addDocumentListener(javax.swing.event.DocumentListener)
meth public abstract void addUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public abstract void getText(int,int,javax.swing.text.Segment) throws javax.swing.text.BadLocationException
meth public abstract void insertString(int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth public abstract void putProperty(java.lang.Object,java.lang.Object)
meth public abstract void remove(int,int) throws javax.swing.text.BadLocationException
meth public abstract void removeDocumentListener(javax.swing.event.DocumentListener)
meth public abstract void removeUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public abstract void render(java.lang.Runnable)

CLSS public abstract javax.swing.text.EditorKit
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public abstract java.lang.String getContentType()
meth public abstract javax.swing.Action[] getActions()
meth public abstract javax.swing.text.Caret createCaret()
meth public abstract javax.swing.text.Document createDefaultDocument()
meth public abstract javax.swing.text.ViewFactory getViewFactory()
meth public abstract void read(java.io.InputStream,javax.swing.text.Document,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public abstract void read(java.io.Reader,javax.swing.text.Document,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public abstract void write(java.io.OutputStream,javax.swing.text.Document,int,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public abstract void write(java.io.Writer,javax.swing.text.Document,int,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public java.lang.Object clone()
meth public void deinstall(javax.swing.JEditorPane)
meth public void install(javax.swing.JEditorPane)
supr java.lang.Object

CLSS public abstract interface javax.swing.text.StyledDocument
intf javax.swing.text.Document
meth public abstract java.awt.Color getBackground(javax.swing.text.AttributeSet)
meth public abstract java.awt.Color getForeground(javax.swing.text.AttributeSet)
meth public abstract java.awt.Font getFont(javax.swing.text.AttributeSet)
meth public abstract javax.swing.text.Element getCharacterElement(int)
meth public abstract javax.swing.text.Element getParagraphElement(int)
meth public abstract javax.swing.text.Style addStyle(java.lang.String,javax.swing.text.Style)
meth public abstract javax.swing.text.Style getLogicalStyle(int)
meth public abstract javax.swing.text.Style getStyle(java.lang.String)
meth public abstract void removeStyle(java.lang.String)
meth public abstract void setCharacterAttributes(int,int,javax.swing.text.AttributeSet,boolean)
meth public abstract void setLogicalStyle(int,javax.swing.text.Style)
meth public abstract void setParagraphAttributes(int,int,javax.swing.text.AttributeSet,boolean)

CLSS public abstract javax.swing.text.TextAction
cons public init(java.lang.String)
meth protected final javax.swing.text.JTextComponent getFocusedComponent()
meth protected final javax.swing.text.JTextComponent getTextComponent(java.awt.event.ActionEvent)
meth public final static javax.swing.Action[] augmentList(javax.swing.Action[],javax.swing.Action[])
supr javax.swing.AbstractAction

CLSS public abstract interface org.netbeans.api.editor.document.CustomUndoDocument
meth public abstract void addUndoableEdit(javax.swing.undo.UndoableEdit)

CLSS public abstract interface org.netbeans.api.editor.document.LineDocument
intf javax.swing.text.Document
meth public abstract javax.swing.text.Element getParagraphElement(int)
meth public abstract javax.swing.text.Position createPosition(int,javax.swing.text.Position$Bias) throws javax.swing.text.BadLocationException

CLSS public org.netbeans.editor.ActionFactory
innr public static AbbrevExpandAction
innr public static AbbrevResetAction
innr public static AdjustCaretAction
innr public static AdjustWindowAction
innr public static AnnotationsCyclingAction
innr public static ChangeCaseAction
innr public static CollapseAllFolds
innr public static CollapseFold
innr public static CopySelectionElseLineDownAction
innr public static CopySelectionElseLineUpAction
innr public static CutToLineBeginOrEndAction
innr public static DumpViewHierarchyAction
innr public static ExpandAllFolds
innr public static ExpandFold
innr public static FirstNonWhiteAction
innr public static FormatAction
innr public static GenerateGutterPopupAction
innr public static InsertDateTimeAction
innr public static JumpListNextAction
innr public static JumpListNextComponentAction
innr public static JumpListPrevAction
innr public static JumpListPrevComponentAction
innr public static LastNonWhiteAction
innr public static MoveSelectionElseLineDownAction
innr public static MoveSelectionElseLineUpAction
innr public static RedoAction
innr public static ReindentLineAction
innr public static RemoveLineAction
innr public static RemoveLineBeginAction
innr public static RemoveSelectionAction
innr public static RemoveTabAction
innr public static RemoveWordNextAction
innr public static RemoveWordPreviousAction
innr public static RunMacroAction
innr public static ScrollDownAction
innr public static ScrollUpAction
innr public static SelectIdentifierAction
innr public static SelectNextParameterAction
innr public static ShiftLineAction
innr public static StartMacroRecordingAction
innr public static StartNewLine
innr public static StopMacroRecordingAction
innr public static ToggleLineNumbersAction
innr public static ToggleRectangularSelectionAction
innr public static ToggleTypingModeAction
innr public static UndoAction
innr public static WordMatchAction
supr java.lang.Object
hfds LOG
hcls DeprecatedFoldAction

CLSS public static org.netbeans.editor.ActionFactory$RedoAction
 outer org.netbeans.editor.ActionFactory
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$StopMacroRecordingAction
 outer org.netbeans.editor.ActionFactory
 anno 0 java.lang.Deprecated()
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth protected org.netbeans.editor.MacroDialogSupport getMacroDialogSupport(java.lang.Class)
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$ToggleLineNumbersAction
 outer org.netbeans.editor.ActionFactory
 anno 0 java.lang.Deprecated()
cons public init()
meth protected boolean isLineNumbersVisible()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth protected void toggleLineNumbers()
meth public javax.swing.JMenuItem getPopupMenuItem(javax.swing.text.JTextComponent)
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds item,serialVersionUID

CLSS public static org.netbeans.editor.ActionFactory$UndoAction
 outer org.netbeans.editor.ActionFactory
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public abstract interface org.netbeans.editor.AtomicLockDocument
 anno 0 java.lang.Deprecated()
intf javax.swing.text.Document
meth public abstract void addAtomicLockListener(org.netbeans.editor.AtomicLockListener)
meth public abstract void atomicLock()
meth public abstract void atomicUndo()
meth public abstract void atomicUnlock()
meth public abstract void removeAtomicLockListener(org.netbeans.editor.AtomicLockListener)

CLSS public abstract org.netbeans.editor.BaseAction
cons public init()
cons public init(int)
cons public init(java.lang.String)
cons public init(java.lang.String,int)
fld protected int updateMask
fld public final static int ABBREV_RESET = 4
 anno 0 java.lang.Deprecated()
fld public final static int CLEAR_STATUS_TEXT = 32
fld public final static int MAGIC_POSITION_RESET = 2
fld public final static int NO_RECORDING = 64
fld public final static int SAVE_POSITION = 128
fld public final static int SELECTION_REMOVE = 1
fld public final static int UNDO_MERGE_RESET = 8
fld public final static int WORD_MATCH_RESET = 16
fld public final static java.lang.String ICON_RESOURCE_PROPERTY = "IconResource"
fld public final static java.lang.String LOCALE_DESC_PREFIX = "desc-"
fld public final static java.lang.String LOCALE_POPUP_PREFIX = "popup-"
fld public final static java.lang.String NO_KEYBINDING = "no-keybinding"
fld public final static java.lang.String POPUP_MENU_TEXT = "PopupMenuText"
meth protected boolean asynchonous()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth protected java.lang.Object createDefaultValue(java.lang.String)
meth protected java.lang.Object findValue(java.lang.String)
 anno 0 java.lang.Deprecated()
meth protected java.lang.Object getDefaultShortDescription()
meth protected void actionNameUpdate(java.lang.String)
meth public abstract void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
meth public final void actionPerformed(java.awt.event.ActionEvent)
meth public java.lang.Object getValue(java.lang.String)
meth public java.lang.String getPopupMenuText(javax.swing.text.JTextComponent)
meth public javax.swing.JMenuItem getPopupMenuItem(javax.swing.text.JTextComponent)
meth public void putValue(java.lang.String,java.lang.Object)
meth public void updateComponent(javax.swing.text.JTextComponent)
meth public void updateComponent(javax.swing.text.JTextComponent,int)
supr javax.swing.text.TextAction
hfds UILOG,UI_LOG_DETAILED,recording,serialVersionUID

CLSS public org.netbeans.editor.BaseDocument
cons public init(boolean,java.lang.String)
cons public init(java.lang.Class,boolean)
 anno 0 java.lang.Deprecated()
fld protected boolean inited
fld protected boolean modified
fld protected javax.swing.text.Element defaultRootElem
fld public final static java.lang.String BLOCKS_FINDER_PROP = "blocks-finder"
fld public final static java.lang.String FILE_NAME_PROP = "file-name"
fld public final static java.lang.String FORMATTER = "formatter"
fld public final static java.lang.String ID_PROP = "id"
fld public final static java.lang.String KIT_CLASS_PROP = "kit-class"
fld public final static java.lang.String LINE_BATCH_SIZE = "line-batch-size"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String LINE_LIMIT_PROP = "line-limit"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String LS_CR = "\r"
fld public final static java.lang.String LS_CRLF = "\r\n"
fld public final static java.lang.String LS_LF = "\n"
fld public final static java.lang.String MIME_TYPE_PROP = "mimeType"
fld public final static java.lang.String READ_LINE_SEPARATOR_PROP = "__EndOfLine__"
fld public final static java.lang.String STRING_BWD_FINDER_PROP = "string-bwd-finder"
fld public final static java.lang.String STRING_FINDER_PROP = "string-finder"
fld public final static java.lang.String UNDO_MANAGER_PROP = "undo-manager"
fld public final static java.lang.String WRAP_SEARCH_MARK_PROP = "wrap-search-mark"
fld public final static java.lang.String WRITE_LINE_SEPARATOR_PROP = "write-line-separator"
innr protected static LazyPropertyMap
innr public abstract interface static PropertyEvaluator
intf org.netbeans.api.editor.document.CustomUndoDocument
intf org.netbeans.api.editor.document.LineDocument
intf org.netbeans.editor.AtomicLockDocument
meth protected final int getAtomicDepth()
meth protected java.util.Dictionary createDocumentProperties(java.util.Dictionary)
meth protected org.netbeans.editor.BaseDocumentEvent createDocumentEvent(int,int,javax.swing.event.DocumentEvent$EventType)
meth protected void fireChangedUpdate(javax.swing.event.DocumentEvent)
meth protected void fireInsertUpdate(javax.swing.event.DocumentEvent)
meth protected void fireRemoveUpdate(javax.swing.event.DocumentEvent)
meth protected void fireUndoableEditUpdate(javax.swing.event.UndoableEditEvent)
meth protected void insertUpdate(javax.swing.text.AbstractDocument$DefaultDocumentEvent,javax.swing.text.AttributeSet)
meth protected void postRemoveUpdate(javax.swing.text.AbstractDocument$DefaultDocumentEvent)
meth protected void preInsertCheck(int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth protected void preInsertUpdate(javax.swing.text.AbstractDocument$DefaultDocumentEvent,javax.swing.text.AttributeSet)
meth protected void preRemoveCheck(int,int) throws javax.swing.text.BadLocationException
meth protected void removeUpdate(javax.swing.text.AbstractDocument$DefaultDocumentEvent)
meth public boolean isIdentifierPart(char)
meth public boolean isModifiable()
meth public boolean isModified()
meth public boolean isWhitespace(char)
meth public char[] getChars(int,int) throws javax.swing.text.BadLocationException
meth public char[] getChars(int[]) throws javax.swing.text.BadLocationException
meth public final boolean isAtomicLock()
meth public final java.lang.Class getKitClass()
 anno 0 java.lang.Deprecated()
meth public final void atomicLock()
 anno 0 java.lang.Deprecated()
meth public final void atomicUnlock()
 anno 0 java.lang.Deprecated()
meth public final void breakAtomicLock()
meth public final void extWriteLock()
meth public final void extWriteUnlock()
meth public int find(org.netbeans.editor.Finder,int,int) throws javax.swing.text.BadLocationException
meth public int getShiftWidth()
 anno 0 java.lang.Deprecated()
meth public int getTabSize()
meth public int processText(org.netbeans.editor.TextBatchProcessor,int,int) throws javax.swing.text.BadLocationException
meth public java.lang.String getText(int[]) throws javax.swing.text.BadLocationException
meth public java.lang.String toString()
meth public java.lang.String toStringDetail()
meth public javax.swing.text.Element getDefaultRootElement()
meth public javax.swing.text.Element getParagraphElement(int)
meth public javax.swing.text.Element[] getRootElements()
meth public javax.swing.text.Position createPosition(int,javax.swing.text.Position$Bias) throws javax.swing.text.BadLocationException
meth public org.netbeans.editor.Annotations getAnnotations()
meth public org.netbeans.editor.CharSeq getText()
meth public org.netbeans.editor.SyntaxSupport getSyntaxSupport()
 anno 0 java.lang.Deprecated()
meth public void addAtomicLockListener(org.netbeans.api.editor.document.AtomicLockListener)
meth public void addAtomicLockListener(org.netbeans.editor.AtomicLockListener)
 anno 0 java.lang.Deprecated()
meth public void addDocumentListener(javax.swing.event.DocumentListener)
meth public void addPostModificationDocumentListener(javax.swing.event.DocumentListener)
meth public void addUndoableEdit(javax.swing.undo.UndoableEdit)
meth public void addUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public void addUpdateDocumentListener(javax.swing.event.DocumentListener)
meth public void atomicUndo()
meth public void checkTrailingSpaces(int)
meth public void getChars(int,char[],int,int) throws javax.swing.text.BadLocationException
meth public void insertString(int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth public void invalidateSyntaxMarks()
meth public void print(org.netbeans.editor.PrintContainer)
meth public void print(org.netbeans.editor.PrintContainer,boolean,boolean,int,int)
meth public void print(org.netbeans.editor.PrintContainer,boolean,java.lang.Boolean,int,int)
meth public void read(java.io.Reader,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public void remove(int,int) throws javax.swing.text.BadLocationException
meth public void removeAtomicLockListener(org.netbeans.api.editor.document.AtomicLockListener)
meth public void removeAtomicLockListener(org.netbeans.editor.AtomicLockListener)
 anno 0 java.lang.Deprecated()
meth public void removeDocumentListener(javax.swing.event.DocumentListener)
meth public void removePostModificationDocumentListener(javax.swing.event.DocumentListener)
meth public void removeUpdateDocumentListener(javax.swing.event.DocumentListener)
meth public void render(java.lang.Runnable)
meth public void repaintBlock(int,int)
 anno 0 java.lang.Deprecated()
meth public void replace(int,int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth public void resetUndoMerge()
meth public void runAtomic(java.lang.Runnable)
meth public void runAtomicAsUser(java.lang.Runnable)
meth public void setPostModificationDocumentListener(javax.swing.event.DocumentListener)
 anno 0 java.lang.Deprecated()
meth public void write(java.io.Writer,int,int) throws java.io.IOException,javax.swing.text.BadLocationException
supr javax.swing.text.AbstractDocument
hfds DEACTIVATE_LEXER_THRESHOLD,EDITABLE_PROP,LAST_MODIFICATION_TIMESTAMP_PROP,LOG,LOG_LISTENER,MODIFICATION_LISTENER_PROP,SUPPORTS_MODIFICATION_LISTENER_PROP,VERSION_PROP,annotations,annotationsLock,atomicDepth,atomicEdits,atomicLockEventInstance,atomicLockListenerList,composedText,debugNoText,debugRead,debugStack,deprecatedKitClass,filterBypass,fixLineSyntaxState,identifierAcceptor,lastModifyUndoEdit,lastPositionEditedByTyping,lineRootElement,mimeType,modifiable,postModificationDepth,postModificationDocumentListener,postModificationDocumentListenerList,prefs,prefsListener,removeUpdateLineUndo,runExclusiveDepth,shiftWidth,syntaxSupport,tabSize,text,undoEditWrappers,undoMergeReset,updateDocumentListenerList,weakPrefsListener,whitespaceAcceptor
hcls Accessor,AtomicCompoundEdit,BaseDocumentServices,FilterBypassImpl,MimeTypePropertyEvaluator,OldListenerAdapter,PlainEditorKit,ServicesImpl

CLSS public org.netbeans.editor.BaseKit
cons public init()
fld public final static int MAGIC_POSITION_MAX = 2147483646
fld public final static java.lang.String DOC_REPLACE_SELECTION_PROPERTY = "doc-replace-selection-property"
fld public final static java.lang.String abbrevExpandAction = "abbrev-expand"
fld public final static java.lang.String abbrevResetAction = "abbrev-reset"
fld public final static java.lang.String adjustCaretBottomAction = "adjust-caret-bottom"
fld public final static java.lang.String adjustCaretCenterAction = "adjust-caret-center"
fld public final static java.lang.String adjustCaretTopAction = "adjust-caret-top"
fld public final static java.lang.String adjustWindowBottomAction = "adjust-window-bottom"
fld public final static java.lang.String adjustWindowCenterAction = "adjust-window-center"
fld public final static java.lang.String adjustWindowTopAction = "adjust-window-top"
fld public final static java.lang.String annotationsCyclingAction = "annotations-cycling"
fld public final static java.lang.String collapseAllFoldsAction = "collapse-all-folds"
fld public final static java.lang.String collapseFoldAction = "collapse-fold"
fld public final static java.lang.String copySelectionElseLineDownAction = "copy-selection-else-line-down"
fld public final static java.lang.String copySelectionElseLineUpAction = "copy-selection-else-line-up"
fld public final static java.lang.String cutToLineBeginAction = "cut-to-line-begin"
fld public final static java.lang.String cutToLineEndAction = "cut-to-line-end"
fld public final static java.lang.String expandAllFoldsAction = "expand-all-folds"
fld public final static java.lang.String expandFoldAction = "expand-fold"
fld public final static java.lang.String findNextAction = "find-next"
fld public final static java.lang.String findPreviousAction = "find-previous"
fld public final static java.lang.String findSelectionAction = "find-selection"
fld public final static java.lang.String firstNonWhiteAction = "first-non-white"
fld public final static java.lang.String formatAction = "format"
fld public final static java.lang.String generateGutterPopupAction = "generate-gutter-popup"
fld public final static java.lang.String indentAction = "indent"
fld public final static java.lang.String insertDateTimeAction = "insert-date-time"
fld public final static java.lang.String jumpListNextAction = "jump-list-next"
fld public final static java.lang.String jumpListNextComponentAction = "jump-list-next-component"
fld public final static java.lang.String jumpListPrevAction = "jump-list-prev"
fld public final static java.lang.String jumpListPrevComponentAction = "jump-list-prev-component"
fld public final static java.lang.String lastNonWhiteAction = "last-non-white"
fld public final static java.lang.String lineFirstColumnAction = "caret-line-first-column"
fld public final static java.lang.String macroActionPrefix = "macro-"
fld public final static java.lang.String moveSelectionElseLineDownAction = "move-selection-else-line-down"
fld public final static java.lang.String moveSelectionElseLineUpAction = "move-selection-else-line-up"
fld public final static java.lang.String pasteFormatedAction = "paste-formated"
fld public final static java.lang.String redoAction = "redo"
fld public final static java.lang.String reformatLineAction = "reformat-line"
fld public final static java.lang.String reindentLineAction = "reindent-line"
fld public final static java.lang.String removeLineAction = "remove-line"
fld public final static java.lang.String removeLineBeginAction = "remove-line-begin"
fld public final static java.lang.String removeNextWordAction = "remove-word-next"
fld public final static java.lang.String removePreviousWordAction = "remove-word-previous"
fld public final static java.lang.String removeSelectionAction = "remove-selection"
fld public final static java.lang.String removeTabAction = "remove-tab"
fld public final static java.lang.String removeTrailingSpacesAction = "remove-trailing-spaces"
fld public final static java.lang.String scrollDownAction = "scroll-down"
fld public final static java.lang.String scrollUpAction = "scroll-up"
fld public final static java.lang.String selectIdentifierAction = "select-identifier"
fld public final static java.lang.String selectNextParameterAction = "select-next-parameter"
fld public final static java.lang.String selectionFirstNonWhiteAction = "selection-first-non-white"
fld public final static java.lang.String selectionLastNonWhiteAction = "selection-last-non-white"
fld public final static java.lang.String selectionLineFirstColumnAction = "selection-line-first-column"
fld public final static java.lang.String selectionPageDownAction = "selection-page-down"
fld public final static java.lang.String selectionPageUpAction = "selection-page-up"
fld public final static java.lang.String shiftLineLeftAction = "shift-line-left"
fld public final static java.lang.String shiftLineRightAction = "shift-line-right"
fld public final static java.lang.String splitLineAction = "split-line"
fld public final static java.lang.String startMacroRecordingAction = "start-macro-recording"
fld public final static java.lang.String startNewLineAction = "start-new-line"
fld public final static java.lang.String stopMacroRecordingAction = "stop-macro-recording"
fld public final static java.lang.String switchCaseAction = "switch-case"
fld public final static java.lang.String toLowerCaseAction = "to-lower-case"
fld public final static java.lang.String toUpperCaseAction = "to-upper-case"
fld public final static java.lang.String toggleHighlightSearchAction = "toggle-highlight-search"
fld public final static java.lang.String toggleLineNumbersAction = "toggle-line-numbers"
fld public final static java.lang.String toggleTypingModeAction = "toggle-typing-mode"
fld public final static java.lang.String undoAction = "undo"
fld public final static java.lang.String wordMatchNextAction = "word-match-next"
fld public final static java.lang.String wordMatchPrevAction = "word-match-prev"
innr public static BackwardAction
innr public static BeepAction
innr public static BeginAction
innr public static BeginLineAction
innr public static BeginWordAction
innr public static CompoundAction
innr public static CopyAction
innr public static CutAction
innr public static DefaultKeyTypedAction
innr public static DeleteCharAction
innr public static DownAction
innr public static EndAction
innr public static EndLineAction
innr public static EndWordAction
innr public static ForwardAction
innr public static InsertBreakAction
innr public static InsertContentAction
innr public static InsertStringAction
innr public static InsertTabAction
innr public static KitCompoundAction
innr public static NextWordAction
innr public static PageDownAction
innr public static PageUpAction
innr public static PasteAction
innr public static PreviousWordAction
innr public static ReadOnlyAction
innr public static RemoveTrailingSpacesAction
innr public static SelectAllAction
innr public static SelectLineAction
innr public static SelectWordAction
innr public static SplitLineAction
innr public static UpAction
innr public static WritableAction
meth protected javax.swing.Action[] createActions()
meth protected javax.swing.Action[] getCustomActions()
meth protected javax.swing.Action[] getDeclaredActions()
meth protected javax.swing.Action[] getMacroActions()
 anno 0 java.lang.Deprecated()
meth protected org.netbeans.editor.BaseTextUI createTextUI()
meth protected org.netbeans.editor.EditorUI createEditorUI()
meth protected org.netbeans.editor.EditorUI createPrintEditorUI(org.netbeans.editor.BaseDocument)
 anno 0 java.lang.Deprecated()
meth protected org.netbeans.editor.EditorUI createPrintEditorUI(org.netbeans.editor.BaseDocument,boolean,boolean)
meth protected void executeDeinstallActions(javax.swing.JEditorPane)
meth protected void executeInstallActions(javax.swing.JEditorPane)
meth protected void initDocument(org.netbeans.editor.BaseDocument)
meth protected void updateActions()
meth public final javax.swing.Action[] getActions()
meth public java.lang.Object clone()
meth public java.util.List<javax.swing.Action> translateActionNameList(java.util.List<java.lang.String>)
meth public javax.swing.Action getActionByName(java.lang.String)
meth public javax.swing.text.Caret createCaret()
meth public javax.swing.text.Document createDefaultDocument()
meth public javax.swing.text.ViewFactory getViewFactory()
meth public org.netbeans.editor.MultiKeymap getKeymap()
meth public org.netbeans.editor.Syntax createFormatSyntax(javax.swing.text.Document)
 anno 0 java.lang.Deprecated()
meth public org.netbeans.editor.Syntax createSyntax(javax.swing.text.Document)
 anno 0 java.lang.Deprecated()
meth public org.netbeans.editor.SyntaxSupport createSyntaxSupport(org.netbeans.editor.BaseDocument)
 anno 0 java.lang.Deprecated()
meth public static javax.swing.Action[] mapToActions(java.util.Map)
meth public static org.netbeans.editor.BaseKit getKit(java.lang.Class)
 anno 0 java.lang.Deprecated()
meth public static void addActionsToMap(java.util.Map<java.lang.String,javax.swing.Action>,javax.swing.Action[],java.lang.String)
meth public void deinstall(javax.swing.JEditorPane)
meth public void install(javax.swing.JEditorPane)
meth public void read(java.io.Reader,javax.swing.text.Document,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public void write(java.io.Writer,javax.swing.text.Document,int,int) throws java.io.IOException,javax.swing.text.BadLocationException
supr javax.swing.text.DefaultEditorKit
hfds IN_PASTE,KEYMAPS_AND_ACTIONS_LOCK,KIT_CNT_PREALLOC,LOG,PROP_NAVIGATE_BOUNDARIES,copyActionDef,cutActionDef,deleteNextCharActionDef,deletePrevCharActionDef,insertBreakActionDef,insertTabActionDef,keyBindingsUpdaterInited,keymapTrackers,kitActionMaps,kitActions,kitKeymaps,kits,pasteActionDef,redoActionDef,removeSelectionActionDef,removeTabActionDef,searchableKit,serialVersionUID,undoActionDef
hcls ClearUIForNullKitListener,DefaultSyntax,DefaultSyntaxTokenContext,KeybindingsAndPreferencesTracker,NullTextUI,SearchableKit

CLSS public org.netbeans.editor.DialogSupport
 anno 0 java.lang.Deprecated()
innr public abstract interface static DialogFactory
meth public static java.awt.Dialog createDialog(java.lang.String,javax.swing.JPanel,boolean,javax.swing.JButton[],boolean,int,int,java.awt.event.ActionListener)
meth public static void setDialogFactory(org.netbeans.editor.DialogSupport$DialogFactory)
supr java.lang.Object
hcls Wrapper

CLSS public abstract interface static org.netbeans.editor.DialogSupport$DialogFactory
 outer org.netbeans.editor.DialogSupport
meth public abstract java.awt.Dialog createDialog(java.lang.String,javax.swing.JPanel,boolean,javax.swing.JButton[],boolean,int,int,java.awt.event.ActionListener)

CLSS public org.netbeans.editor.EditorUI
cons public init()
cons public init(org.netbeans.editor.BaseDocument)
cons public init(org.netbeans.editor.BaseDocument,boolean,boolean)
fld public final static int SCROLL_DEFAULT = 0
fld public final static int SCROLL_FIND = 3
fld public final static int SCROLL_MOVE = 1
fld public final static int SCROLL_SMALLEST = 2
fld public final static java.awt.Insets defaultLineNumberMargin
fld public final static java.lang.String COMPONENT_PROPERTY = "component"
fld public final static java.lang.String LINE_HEIGHT_CHANGED_PROP = "line-height-changed-prop"
fld public final static java.lang.String OVERWRITE_MODE_PROPERTY = "overwriteMode"
fld public final static java.lang.String TAB_SIZE_CHANGED_PROP = "tab-size-changed-prop"
intf java.awt.event.MouseListener
intf java.beans.PropertyChangeListener
intf javax.swing.event.ChangeListener
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected int textLimitWidth()
meth protected java.util.Map createColoringMap()
 anno 0 java.lang.Deprecated()
meth protected javax.swing.JComponent createExtComponent()
meth protected javax.swing.JToolBar createToolBarComponent()
meth protected static java.util.Map<java.lang.String,org.netbeans.editor.Coloring> getSharedColoringMap(java.lang.Class)
 anno 0 java.lang.Deprecated()
meth protected void initGlyphCorner(javax.swing.JScrollPane)
meth protected void installUI(javax.swing.text.JTextComponent)
meth protected void modelChanged(org.netbeans.editor.BaseDocument,org.netbeans.editor.BaseDocument)
meth protected void paint(java.awt.Graphics)
meth protected void setGlyphGutter(org.netbeans.editor.GlyphGutter)
meth protected void settingsChangeImpl(java.lang.String)
meth protected void uninstallUI(javax.swing.text.JTextComponent)
meth protected void update(java.awt.Graphics)
meth protected void updateScrollPaneCornerColor()
meth public boolean hasExtComponent()
meth public boolean isGlyphGutterVisible()
meth public boolean isLineNumberEnabled()
meth public boolean updateVirtualHeight(int)
 anno 0 java.lang.Deprecated()
meth public boolean updateVirtualWidth(int)
 anno 0 java.lang.Deprecated()
meth public final int getSideBarWidth()
meth public final javax.swing.text.JTextComponent getComponent()
meth public final org.netbeans.editor.BaseDocument getDocument()
meth public final org.netbeans.editor.GlyphGutter getGlyphGutter()
meth public int getLineAscent()
meth public int getLineHeight()
meth public int getLineNumberDigitWidth()
meth public java.awt.Insets getLineNumberMargin()
meth public java.awt.Insets getTextMargin()
meth public java.awt.Rectangle getExtentBounds()
meth public java.awt.Rectangle getExtentBounds(java.awt.Rectangle)
meth public java.lang.Object getComponentLock()
meth public java.lang.Object getProperty(java.lang.Object)
meth public java.util.Map<java.lang.String,org.netbeans.editor.Coloring> getColoringMap()
 anno 0 java.lang.Deprecated()
meth public javax.swing.JComponent getExtComponent()
meth public javax.swing.JPopupMenu getPopupMenu()
meth public javax.swing.JToolBar getToolBarComponent()
meth public org.netbeans.api.editor.StickyWindowSupport getStickyWindowSupport()
meth public org.netbeans.editor.Abbrev getAbbrev()
 anno 0 java.lang.Deprecated()
meth public org.netbeans.editor.Coloring getColoring(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public org.netbeans.editor.Coloring getDefaultColoring()
 anno 0 java.lang.Deprecated()
meth public org.netbeans.editor.PopupManager getPopupManager()
meth public org.netbeans.editor.StatusBar getStatusBar()
meth public org.netbeans.editor.WordMatch getWordMatch()
meth public org.netbeans.editor.ext.ToolTipSupport getToolTipSupport()
meth public static java.awt.Frame getParentFrame(java.awt.Component)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void adjustCaret(int)
meth public void adjustWindow(int)
meth public void caretMoveDot(int,java.awt.Rectangle,int)
 anno 0 java.lang.Deprecated()
meth public void caretSetDot(int,java.awt.Rectangle,int)
 anno 0 java.lang.Deprecated()
meth public void hidePopupMenu()
meth public void mouseClicked(java.awt.event.MouseEvent)
meth public void mouseEntered(java.awt.event.MouseEvent)
meth public void mouseExited(java.awt.event.MouseEvent)
meth public void mousePressed(java.awt.event.MouseEvent)
meth public void mouseReleased(java.awt.event.MouseEvent)
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void putProperty(java.lang.Object,java.lang.Object)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void repaint(int)
meth public void repaint(int,int)
meth public void repaintBlock(int,int) throws javax.swing.text.BadLocationException
meth public void repaintOffset(int) throws javax.swing.text.BadLocationException
meth public void scrollRectToVisible(java.awt.Rectangle,int)
 anno 0 java.lang.Deprecated()
meth public void setLineNumberEnabled(boolean)
meth public void setPopupMenu(javax.swing.JPopupMenu)
meth public void showPopupMenu(int,int)
meth public void stateChanged(javax.swing.event.ChangeEvent)
meth public void updateLineNumberWidth(int)
meth public void updateTextMargin()
supr java.lang.Object
hfds DEFAULT_INSETS,LOG,NULL_INSETS,abbrev,coloringMap,component,componentLock,defaultSpaceWidth,disableLineNumbers,drawLayerList,extComponent,focusL,glyphCorner,glyphGutter,highlightSearch,isPasteActionInited,lineAscent,lineHeight,lineHeightCorrection,lineNumberDigitWidth,lineNumberEnabled,lineNumberMaxDigitCount,lineNumberVisible,lineNumberVisibleSetting,lineNumberWidth,listener,popupManager,popupMenu,popupMenuEnabled,prefs,printDoc,propertyChangeSupport,props,renderingHints,scrollFindInsets,scrollJumpInsets,statusBar,stickyWindowSupport,textLeftMarginWidth,textLimitLineVisible,textLimitWidth,textMargin,toolBarComponent,toolTipSupport,weakPrefsListener,wordMatch
hcls Accessor,ComponentLock,Listener

CLSS public org.netbeans.editor.GuardedDocument
cons public init(java.lang.Class)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.Class,boolean,javax.swing.text.StyleContext)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String)
cons public init(java.lang.String,boolean,javax.swing.text.StyleContext)
fld protected java.lang.String normalStyleName
fld protected javax.swing.text.StyleContext styles
fld public final static java.lang.String FMT_GUARDED_INSERT_LOCALE = "FMT_guarded_insert"
fld public final static java.lang.String FMT_GUARDED_REMOVE_LOCALE = "FMT_guarded_remove"
fld public final static java.lang.String GUARDED_ATTRIBUTE = "guarded"
fld public final static javax.swing.text.SimpleAttributeSet guardedSet
fld public final static javax.swing.text.SimpleAttributeSet unguardedSet
intf javax.swing.text.StyledDocument
meth protected org.netbeans.editor.BaseDocumentEvent createDocumentEvent(int,int,javax.swing.event.DocumentEvent$EventType)
meth protected void preInsertCheck(int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth protected void preRemoveCheck(int,int) throws javax.swing.text.BadLocationException
meth public boolean isPosGuarded(int)
meth public java.awt.Color getBackground(javax.swing.text.AttributeSet)
meth public java.awt.Color getForeground(javax.swing.text.AttributeSet)
meth public java.awt.Font getFont(javax.swing.text.AttributeSet)
meth public java.lang.String toStringDetail()
meth public java.util.Enumeration getStyleNames()
meth public javax.swing.text.Element getCharacterElement(int)
meth public javax.swing.text.Style addStyle(java.lang.String,javax.swing.text.Style)
meth public javax.swing.text.Style getLogicalStyle(int)
meth public javax.swing.text.Style getStyle(java.lang.String)
meth public org.netbeans.editor.MarkBlockChain getGuardedBlockChain()
meth public void removeStyle(java.lang.String)
meth public void runAtomic(java.lang.Runnable)
meth public void runAtomicAsUser(java.lang.Runnable)
meth public void setCharacterAttributes(int,int,javax.swing.text.AttributeSet,boolean)
meth public void setLogicalStyle(int,javax.swing.text.Style)
meth public void setNormalStyleName(java.lang.String)
meth public void setParagraphAttributes(int,int,javax.swing.text.AttributeSet,boolean)
supr org.netbeans.editor.BaseDocument
hfds LOG,atomicAsUser,breakGuarded,debugAtomic,debugAtomicStack,guardedBlockChain

CLSS public abstract org.netbeans.editor.ImplementationProvider
 anno 0 java.lang.Deprecated()
cons public init()
meth public abstract java.util.ResourceBundle getResourceBundle(java.lang.String)
meth public abstract javax.swing.Action[] getGlyphGutterActions(javax.swing.text.JTextComponent)
meth public boolean activateComponent(javax.swing.text.JTextComponent)
meth public static org.netbeans.editor.ImplementationProvider getDefault()
meth public static void registerDefault(org.netbeans.editor.ImplementationProvider)
supr java.lang.Object
hfds PROVIDER
hcls ProviderBridge,Wrapper

CLSS public org.netbeans.editor.LocaleSupport
 anno 0 java.lang.Deprecated()
cons public init()
innr public abstract interface static Localizer
meth public static char getChar(java.lang.String,char)
meth public static java.lang.String getString(java.lang.String)
meth public static java.lang.String getString(java.lang.String,java.lang.String)
meth public static void addLocalizer(org.netbeans.editor.LocaleSupport$Localizer)
meth public static void removeLocalizer(org.netbeans.editor.LocaleSupport$Localizer)
supr java.lang.Object
hfds debug,localizers

CLSS public abstract interface static org.netbeans.editor.LocaleSupport$Localizer
 outer org.netbeans.editor.LocaleSupport
meth public abstract java.lang.String getString(java.lang.String)

CLSS public abstract interface org.netbeans.editor.PrintContainer
meth public abstract boolean initEmptyLines()
meth public abstract void add(char[],java.awt.Font,java.awt.Color,java.awt.Color)
meth public abstract void eol()

CLSS public org.netbeans.editor.ext.ExtKit
cons public init()
fld public final static java.lang.String TRIMMED_TEXT = "trimmed-text"
fld public final static java.lang.String allCompletionShowAction = "all-completion-show"
fld public final static java.lang.String buildPopupMenuAction = "build-popup-menu"
fld public final static java.lang.String buildToolTipAction = "build-tool-tip"
fld public final static java.lang.String codeSelectAction = "code-select"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String commentAction = "comment"
fld public final static java.lang.String completionShowAction = "completion-show"
fld public final static java.lang.String completionTooltipShowAction = "tooltip-show"
fld public final static java.lang.String documentationShowAction = "documentation-show"
fld public final static java.lang.String escapeAction = "escape"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String findAction = "find"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String gotoAction = "goto"
fld public final static java.lang.String gotoDeclarationAction = "goto-declaration"
fld public final static java.lang.String gotoHelpAction = "goto-help"
fld public final static java.lang.String gotoSourceAction = "goto-source"
fld public final static java.lang.String gotoSuperImplementationAction = "goto-super-implementation"
fld public final static java.lang.String matchBraceAction = "match-brace"
fld public final static java.lang.String replaceAction = "replace"
fld public final static java.lang.String selectionMatchBraceAction = "selection-match-brace"
fld public final static java.lang.String showPopupMenuAction = "show-popup-menu"
fld public final static java.lang.String toggleCaseIdentifierBeginAction = "toggle-case-identifier-begin"
fld public final static java.lang.String toggleCommentAction = "toggle-comment"
fld public final static java.lang.String toggleToolbarAction = "toggle-toolbar"
fld public final static java.lang.String uncommentAction = "uncomment"
innr public static AllCompletionShowAction
innr public static BuildPopupMenuAction
innr public static BuildToolTipAction
innr public static CodeSelectAction
innr public static CommentAction
innr public static CompletionShowAction
innr public static CompletionTooltipShowAction
innr public static DocumentationShowAction
innr public static EscapeAction
innr public static ExtDefaultKeyTypedAction
innr public static ExtDeleteCharAction
innr public static GotoAction
innr public static GotoDeclarationAction
innr public static MatchBraceAction
innr public static PrefixMakerAction
innr public static ShowPopupMenuAction
innr public static ToggleCaseIdentifierBeginAction
innr public static ToggleCommentAction
innr public static UncommentAction
meth protected javax.swing.Action[] createActions()
meth protected org.netbeans.editor.EditorUI createEditorUI()
meth public org.netbeans.editor.SyntaxSupport createSyntaxSupport(org.netbeans.editor.BaseDocument)
supr org.netbeans.editor.BaseKit
hfds debugPopupMenu,editorBundleHash,noExtEditorUIClass
hcls BaseKitLocalizedAction

CLSS public static org.netbeans.editor.ext.ExtKit$BuildPopupMenuAction
 outer org.netbeans.editor.ext.ExtKit
cons public init()
cons public init(java.util.Map)
meth protected final void debugPopupMenuItem(javax.swing.JMenuItem,javax.swing.Action)
meth protected java.lang.Class getShortDescriptionBundleClass()
meth protected java.lang.String getItemText(javax.swing.text.JTextComponent,java.lang.String,javax.swing.Action)
meth protected javax.swing.JPopupMenu buildPopupMenu(javax.swing.text.JTextComponent)
meth protected javax.swing.JPopupMenu createPopupMenu(javax.swing.text.JTextComponent)
meth protected void addAction(javax.swing.text.JTextComponent,javax.swing.JPopupMenu,java.lang.String)
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public static org.netbeans.editor.ext.ExtKit$BuildToolTipAction
 outer org.netbeans.editor.ext.ExtKit
cons public init()
cons public init(java.util.Map)
meth protected java.lang.String buildText(javax.swing.text.JTextComponent)
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public org.netbeans.modules.editor.EditorModule
cons public init()
meth public void restored()
meth public void uninstalled()
supr org.openide.modules.ModuleInstall
hfds LOG,debug,topComponentRegistryListener
hcls DebugHashtable,HackMap

CLSS public org.netbeans.modules.editor.EditorWarmUpTask
cons public init()
intf java.lang.Runnable
meth public void run()
supr java.lang.Object

CLSS public org.netbeans.modules.editor.ExportHtmlAction
cons public init()
meth protected final boolean asynchronous()
meth protected final int mode()
meth protected final java.lang.Class[] cookieClasses()
meth protected final void performAction(org.openide.nodes.Node[])
meth public final java.lang.String getName()
meth public final org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CookieAction
hfds CHARSET,FOLDER_NAME_HIST,HTML_EXT,OPEN_HTML_HIST,SELECTION_HIST,SHOW_LINES_HIST,dlg
hcls HtmlOrDirFilter,Presenter

CLSS public org.netbeans.modules.editor.HtmlPrintContainer
cons public init()
intf org.netbeans.editor.PrintContainer
meth public final boolean initEmptyLines()
meth public final java.lang.String end()
meth public final void add(char[],java.awt.Font,java.awt.Color,java.awt.Color)
meth public final void begin(org.openide.filesystems.FileObject,java.awt.Font,java.awt.Color,java.awt.Color,java.awt.Color,java.awt.Color,java.lang.Class,java.lang.String)
meth public final void eol()
meth public void addLines(java.util.List<java.text.AttributedCharacterIterator>)
supr java.lang.Object
hfds DOCTYPE,DOT,EOL,ESC_AMP,ESC_APOS,ESC_GT,ESC_LT,ESC_QUOT,FF_MONOSPACE,FF_SANSSERIF,FF_SERIF,STYLE_PREFIX,ST_BEGIN,ST_BGCOLOR,ST_BODY,ST_BOLD,ST_COLOR,ST_END,ST_FONT_FAMILY,ST_ITALIC,ST_PRE,ST_SEPARATOR,ST_SIZE,ST_TABLE,T_BLOCK_E,T_BLOCK_S,T_BODY_E,T_BODY_S,T_CHARSET,T_COMMENT_E,T_COMMENT_S,T_HEAD_E,T_HEAD_S,T_HTML_E,T_HTML_S,T_NAME_TABLE,T_PRE_E,T_PRE_S,T_STYLE_E,T_STYLE_S,T_TITLE,WS,ZERO,boolHolder,buffer,charset,defaultBackgroundColor,defaultFont,defaultForegroundColor,fileName,headerBackgroundColor,headerForegroundColor,shortFileName,styles,syntaxColoring
hcls Styles

CLSS public abstract org.netbeans.modules.editor.MainMenuAction
cons public init()
cons public init(boolean,javax.swing.Icon)
fld public boolean menuInitialized
fld public final static javax.swing.Icon BLANK_ICON
innr public final static CommentAction
innr public final static FindNextAction
innr public final static FindPreviousAction
innr public final static FindSelectionAction
innr public final static FormatAction
innr public final static JumpBackAction
innr public final static JumpForwardAction
innr public final static PasteFormattedAction
innr public final static RemoveTrailingSpacesAction
innr public final static SelectAllAction
innr public final static SelectIdentifierAction
innr public final static ShiftLineLeftAction
innr public final static ShiftLineRightAction
innr public final static StartMacroRecordingAction
innr public final static StopMacroRecordingAction
innr public final static ToggleCommentAction
innr public final static UncommentAction
innr public final static WordMatchNextAction
innr public final static WordMatchPrevAction
innr public static GoToDeclarationAction
innr public static GoToSourceAction
innr public static GoToSuperAction
innr public static ShowLineNumbersAction
innr public static ShowToolBarAction
intf javax.swing.event.ChangeListener
intf org.openide.util.LookupListener
intf org.openide.util.actions.Presenter$Menu
meth protected abstract java.lang.String getActionName()
meth protected abstract java.lang.String getMenuItemText()
meth protected final javax.swing.ActionMap getContextActionMap()
meth protected final void postSetMenu()
meth protected javax.swing.Action getGlobalKitAction()
meth protected javax.swing.KeyStroke getDefaultAccelerator()
meth protected static javax.swing.Action getActionByName(java.lang.String)
meth protected static void addAccelerators(javax.swing.Action,javax.swing.JMenuItem,javax.swing.text.JTextComponent)
meth protected void setMenu()
meth public boolean isEnabled()
meth public java.lang.String getName()
meth public javax.swing.JMenuItem getMenuPresenter()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void resultChanged(org.openide.util.LookupEvent)
meth public void stateChanged(javax.swing.event.ChangeEvent)
supr java.lang.Object
hfds IS_SET_POST_SET_MENU_LISTENER,RP,forceIcon,forcedIcon,globalActionMap,kbs,menuPresenter

CLSS public final static org.netbeans.modules.editor.MainMenuAction$CommentAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public final static org.netbeans.modules.editor.MainMenuAction$FindNextAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public final static org.netbeans.modules.editor.MainMenuAction$FindPreviousAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public final static org.netbeans.modules.editor.MainMenuAction$FindSelectionAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
meth public boolean isEnabled()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public final static org.netbeans.modules.editor.MainMenuAction$FormatAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
meth protected javax.swing.Action getGlobalKitAction()
meth protected javax.swing.KeyStroke getDefaultAccelerator()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public static org.netbeans.modules.editor.MainMenuAction$GoToDeclarationAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public static org.netbeans.modules.editor.MainMenuAction$GoToSourceAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public static org.netbeans.modules.editor.MainMenuAction$GoToSuperAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public final static org.netbeans.modules.editor.MainMenuAction$JumpBackAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public final static org.netbeans.modules.editor.MainMenuAction$JumpForwardAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public final static org.netbeans.modules.editor.MainMenuAction$PasteFormattedAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public final static org.netbeans.modules.editor.MainMenuAction$RemoveTrailingSpacesAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public final static org.netbeans.modules.editor.MainMenuAction$SelectAllAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public final static org.netbeans.modules.editor.MainMenuAction$SelectIdentifierAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public final static org.netbeans.modules.editor.MainMenuAction$ShiftLineLeftAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public final static org.netbeans.modules.editor.MainMenuAction$ShiftLineRightAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public static org.netbeans.modules.editor.MainMenuAction$ShowLineNumbersAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
meth protected javax.swing.Action getGlobalKitAction()
meth protected void setMenu()
meth public java.lang.String getName()
meth public javax.swing.JMenuItem getMenuPresenter()
supr org.netbeans.modules.editor.MainMenuAction
hfds SHOW_LINE_MENU,delegate

CLSS public static org.netbeans.modules.editor.MainMenuAction$ShowToolBarAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
meth protected javax.swing.Action getGlobalKitAction()
meth protected void setMenu()
meth public javax.swing.JMenuItem getMenuPresenter()
supr org.netbeans.modules.editor.MainMenuAction
hfds SHOW_TOOLBAR_MENU,delegate

CLSS public final static org.netbeans.modules.editor.MainMenuAction$StartMacroRecordingAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public final static org.netbeans.modules.editor.MainMenuAction$StopMacroRecordingAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public final static org.netbeans.modules.editor.MainMenuAction$ToggleCommentAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public final static org.netbeans.modules.editor.MainMenuAction$UncommentAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public final static org.netbeans.modules.editor.MainMenuAction$WordMatchNextAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public final static org.netbeans.modules.editor.MainMenuAction$WordMatchPrevAction
 outer org.netbeans.modules.editor.MainMenuAction
cons public init()
meth protected java.lang.String getActionName()
meth protected java.lang.String getMenuItemText()
supr org.netbeans.modules.editor.MainMenuAction

CLSS public org.netbeans.modules.editor.NbCodeFoldingAction
cons public init()
innr public CodeFoldsMenu
intf org.openide.util.actions.Presenter$Menu
meth public boolean isEnabled()
meth public final org.openide.util.HelpCtx getHelpCtx()
meth public java.lang.String getName()
meth public javax.swing.JMenuItem getMenuPresenter()
meth public void actionPerformed(java.awt.event.ActionEvent)
supr java.lang.Object

CLSS public org.netbeans.modules.editor.NbCodeFoldingAction$CodeFoldsMenu
 outer org.netbeans.modules.editor.NbCodeFoldingAction
cons public init(org.netbeans.modules.editor.NbCodeFoldingAction)
cons public init(org.netbeans.modules.editor.NbCodeFoldingAction,java.lang.String)
intf org.openide.awt.DynamicMenuContent
meth public javax.swing.JComponent[] getMenuPresenters()
meth public javax.swing.JComponent[] synchMenuPresenters(javax.swing.JComponent[])
meth public javax.swing.JPopupMenu getPopupMenu()
supr javax.swing.JMenu

CLSS public org.netbeans.modules.editor.NbDialogSupport
 anno 0 java.lang.Deprecated()
cons public init()
intf org.netbeans.editor.DialogSupport$DialogFactory
supr org.netbeans.modules.editor.impl.NbDialogFactory

CLSS public org.netbeans.modules.editor.NbEditorDocument
cons public init(java.lang.Class)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String)
fld public final static java.lang.String INDENT_ENGINE = "indentEngine"
intf org.openide.text.NbDocument$Annotatable
intf org.openide.text.NbDocument$CustomEditor
intf org.openide.text.NbDocument$CustomToolbar
intf org.openide.text.NbDocument$PositionBiasable
intf org.openide.text.NbDocument$Printable
intf org.openide.text.NbDocument$WriteLockable
meth protected java.util.Dictionary createDocumentProperties(java.util.Dictionary)
meth public int getShiftWidth()
meth public int getTabSize()
meth public java.awt.Component createEditor(javax.swing.JEditorPane)
meth public java.text.AttributedCharacterIterator[] createPrintIterators()
meth public javax.swing.JToolBar createToolbar(javax.swing.JEditorPane)
meth public void addAnnotation(javax.swing.text.Position,int,org.openide.text.Annotation)
meth public void removeAnnotation(org.openide.text.Annotation)
meth public void setCharacterAttributes(int,int,javax.swing.text.AttributeSet,boolean)
supr org.netbeans.editor.GuardedDocument
hfds RP,annoMap
hcls AnnotationDescDelegate,NbPrintContainer

CLSS public org.netbeans.modules.editor.NbEditorKit
cons public init()
fld public final static java.lang.String SYSTEM_ACTION_CLASS_NAME_PROPERTY = "systemActionClassName"
fld public final static java.lang.String generateFoldPopupAction = "generate-fold-popup"
fld public final static java.lang.String generateGoToPopupAction = "generate-goto-popup"
innr public NbStopMacroRecordingAction
innr public final static NbToggleLineNumbersAction
innr public static GenerateFoldPopupAction
innr public static NbBuildPopupMenuAction
innr public static NbBuildToolTipAction
innr public static NbGenerateGoToPopupAction
innr public static NbRedoAction
innr public static NbUndoAction
innr public static ToggleToolbarAction
intf java.util.concurrent.Callable
meth protected javax.swing.Action[] createActions()
meth protected javax.swing.Action[] getDeclaredActions()
meth protected org.netbeans.editor.EditorUI createEditorUI()
meth protected void addSystemActionMapping(java.lang.String,java.lang.Class)
meth protected void toolTipAnnotationsLock(javax.swing.text.Document)
meth protected void toolTipAnnotationsUnlock(javax.swing.text.Document)
meth protected void updateActions()
meth public java.lang.Object call()
meth public java.lang.String getContentType()
meth public javax.swing.text.Document createDefaultDocument()
supr org.netbeans.editor.ext.ExtKit
hfds ACTIONS_TOPCOMPONENT,ACTION_CREATEITEM,ACTION_EXTKIT_BYNAME,ACTION_FOLDER,ACTION_SEPARATOR,ACTION_SYSTEM,LOG,SEPARATOR,contentTypeTable,nbRedoActionDef,nbUndoActionDef,serialVersionUID,systemAction2editorAction
hcls LayerSubFolderMenu,PopupInitializer,SubFolderData

CLSS public static org.netbeans.modules.editor.NbEditorKit$GenerateFoldPopupAction
 outer org.netbeans.modules.editor.NbEditorKit
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth protected java.lang.String getItemText(javax.swing.text.JTextComponent,java.lang.String,javax.swing.Action)
meth protected void addAction(javax.swing.text.JTextComponent,javax.swing.JMenu,java.lang.String)
meth protected void addAdditionalItems(javax.swing.text.JTextComponent,javax.swing.JMenu)
meth protected void setAddSeparatorBeforeNextAction(boolean)
meth public javax.swing.JMenuItem getPopupMenuItem(javax.swing.text.JTextComponent)
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds addSeparatorBeforeNextAction

CLSS public static org.netbeans.modules.editor.NbEditorKit$NbBuildPopupMenuAction
 outer org.netbeans.modules.editor.NbEditorKit
cons public init()
cons public init(java.util.Map)
meth protected javax.swing.JPopupMenu buildPopupMenu(javax.swing.text.JTextComponent)
meth protected javax.swing.JPopupMenu createPopupMenu(javax.swing.text.JTextComponent)
meth protected void addAction(javax.swing.text.JTextComponent,javax.swing.JPopupMenu,java.lang.String)
meth protected void addAction(javax.swing.text.JTextComponent,javax.swing.JPopupMenu,javax.swing.Action)
supr org.netbeans.editor.ext.ExtKit$BuildPopupMenuAction
hfds serialVersionUID

CLSS public static org.netbeans.modules.editor.NbEditorKit$NbBuildToolTipAction
 outer org.netbeans.modules.editor.NbEditorKit
cons public init()
cons public init(java.util.Map)
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.ext.ExtKit$BuildToolTipAction

CLSS public static org.netbeans.modules.editor.NbEditorKit$NbGenerateGoToPopupAction
 outer org.netbeans.modules.editor.NbEditorKit
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction

CLSS public static org.netbeans.modules.editor.NbEditorKit$NbRedoAction
 outer org.netbeans.modules.editor.NbEditorKit
cons public init()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.ActionFactory$RedoAction

CLSS public org.netbeans.modules.editor.NbEditorKit$NbStopMacroRecordingAction
 outer org.netbeans.modules.editor.NbEditorKit
 anno 0 java.lang.Deprecated()
cons public init(org.netbeans.modules.editor.NbEditorKit)
meth protected org.netbeans.editor.MacroDialogSupport getMacroDialogSupport(java.lang.Class)
supr org.netbeans.editor.ActionFactory$StopMacroRecordingAction

CLSS public final static org.netbeans.modules.editor.NbEditorKit$NbToggleLineNumbersAction
 outer org.netbeans.modules.editor.NbEditorKit
 anno 0 java.lang.Deprecated()
cons public init()
meth protected boolean isLineNumbersVisible()
meth protected void toggleLineNumbers()
supr org.netbeans.editor.ActionFactory$ToggleLineNumbersAction

CLSS public static org.netbeans.modules.editor.NbEditorKit$NbUndoAction
 outer org.netbeans.modules.editor.NbEditorKit
cons public init()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.ActionFactory$UndoAction

CLSS public static org.netbeans.modules.editor.NbEditorKit$ToggleToolbarAction
 outer org.netbeans.modules.editor.NbEditorKit
 anno 0 java.lang.Deprecated()
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public javax.swing.JMenuItem getPopupMenuItem(javax.swing.text.JTextComponent)
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction

CLSS public org.netbeans.modules.editor.NbEditorUI
cons public init()
innr public final SystemActionUpdater
meth protected int textLimitWidth()
meth protected javax.swing.JComponent createExtComponent()
meth protected javax.swing.JToolBar createToolBarComponent()
meth protected org.netbeans.modules.editor.NbEditorUI$SystemActionUpdater createSystemActionUpdater(java.lang.String,boolean,boolean)
 anno 0 java.lang.Deprecated()
meth protected void attachSystemActionPerformer(java.lang.String)
meth protected void installUI(javax.swing.text.JTextComponent)
meth protected void uninstallUI(javax.swing.text.JTextComponent)
meth public boolean isLineNumberEnabled()
meth public void setLineNumberEnabled(boolean)
supr org.netbeans.editor.EditorUI
hfds TASK,WORKER,attached,focusL,listener,lock,objectsToRefresh
hcls EnabledPropertySyncListener,LayeredEditorPane,SideBarsListener,SystemActionPerformer

CLSS public final org.netbeans.modules.editor.NbEditorUI$SystemActionUpdater
 outer org.netbeans.modules.editor.NbEditorUI
 anno 0 java.lang.Deprecated()
intf java.beans.PropertyChangeListener
intf org.openide.util.actions.ActionPerformer
meth protected void finalize() throws java.lang.Throwable
meth public void editorActivated()
meth public void editorDeactivated()
meth public void performAction(org.openide.util.actions.SystemAction)
meth public void propertyChange(java.beans.PropertyChangeEvent)
supr java.lang.Object
hfds editorAction,editorActionName,enabledPropertySyncL,listeningOnTCRegistry,syncEnabling,systemAction,updatePerformer

CLSS public org.netbeans.modules.editor.NbEditorUtilities
cons public init()
meth public static boolean isDocumentActive(javax.swing.text.Document)
meth public static int[] getIdentifierAndMethodBlock(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
meth public static java.lang.String getMimeType(javax.swing.text.Document)
meth public static java.lang.String getMimeType(javax.swing.text.JTextComponent)
meth public static java.lang.String[] mergeStringArrays(java.lang.String[],java.lang.String[])
meth public static org.openide.filesystems.FileObject getFileObject(javax.swing.text.Document)
meth public static org.openide.loaders.DataObject getDataObject(javax.swing.text.Document)
meth public static org.openide.text.Line getLine(javax.swing.text.Document,int,boolean)
meth public static org.openide.text.Line getLine(javax.swing.text.JTextComponent,boolean)
meth public static org.openide.text.Line getLine(org.netbeans.editor.BaseDocument,int,boolean)
 anno 0 java.lang.Deprecated()
meth public static org.openide.windows.TopComponent getOuterTopComponent(javax.swing.text.JTextComponent)
meth public static org.openide.windows.TopComponent getTopComponent(javax.swing.text.JTextComponent)
meth public static void addJumpListEntry(org.openide.loaders.DataObject)
meth public static void invalidArgument(java.lang.String)
supr java.lang.Object

CLSS public org.netbeans.modules.editor.NbImplementationProvider
 anno 0 java.lang.Deprecated()
cons public init()
fld public final static java.lang.String GLYPH_GUTTER_ACTIONS_FOLDER_NAME = "GlyphGutterActions"
meth public boolean activateComponent(javax.swing.text.JTextComponent)
meth public java.util.ResourceBundle getResourceBundle(java.lang.String)
meth public javax.swing.Action[] getGlyphGutterActions(javax.swing.text.JTextComponent)
supr org.netbeans.editor.ImplementationProvider
hfds provider

CLSS public org.netbeans.modules.editor.NbLocalizer
cons public init(java.lang.Class)
intf org.netbeans.editor.LocaleSupport$Localizer
meth public java.lang.String getString(java.lang.String)
meth public java.lang.String toString()
supr java.lang.Object
hfds bundleClass

CLSS public org.netbeans.modules.editor.NbToolTip
supr org.openide.filesystems.FileChangeAdapter
hfds LOG,lastRequestId,lastToolTipTask,mime2tip,mimeType,tipAnnotations,toolTipRP
hcls Request

CLSS public org.netbeans.modules.editor.impl.NbDialogFactory
cons public init()
intf org.netbeans.modules.editor.lib2.DialogFactory
meth public java.awt.Dialog createDialog(java.lang.String,javax.swing.JPanel,boolean,javax.swing.JButton[],boolean,int,int,java.awt.event.ActionListener)
supr java.lang.Object
hfds HELP_ID_JavaFastImportPanel,HELP_ID_MacroSavePanel,HELP_ID_ScrollCompletionPane,helpIDs

CLSS public abstract interface org.netbeans.modules.editor.lib2.DialogFactory
meth public abstract java.awt.Dialog createDialog(java.lang.String,javax.swing.JPanel,boolean,javax.swing.JButton[],boolean,int,int,java.awt.event.ActionListener)

CLSS public org.netbeans.modules.editor.options.AnnotationTypeActionsFolder
meth protected java.lang.Object createInstance(org.openide.cookies.InstanceCookie[]) throws java.io.IOException,java.lang.ClassNotFoundException
meth public static boolean readActions(org.netbeans.editor.AnnotationType,java.lang.String)
supr org.openide.loaders.FolderInstance
hfds FOLDER,type

CLSS public org.netbeans.modules.editor.options.AnnotationTypeOptions
cons public init(org.netbeans.editor.AnnotationType)
meth public boolean isInheritForegroundColor()
meth public boolean isUseHighlightColor()
meth public boolean isUseWaveUnderlineColor()
meth public boolean isVisible()
meth public boolean isWholeLine()
meth public java.awt.Color getForegroundColor()
meth public java.awt.Color getHighlightColor()
meth public java.awt.Color getWaveUnderlineColor()
meth public void setForegroundColor(java.awt.Color)
meth public void setHighlightColor(java.awt.Color)
meth public void setInheritForegroundColor(boolean)
meth public void setUseHighlightColor(boolean)
meth public void setUseWaveUnderlineColor(boolean)
meth public void setWaveUnderlineColor(java.awt.Color)
supr java.lang.Object
hfds delegate

CLSS public org.netbeans.modules.editor.options.AnnotationTypeOptionsBeanInfo
cons public init()
meth public java.beans.PropertyDescriptor[] getPropertyDescriptors()
supr java.beans.SimpleBeanInfo

CLSS public org.netbeans.modules.editor.options.AnnotationTypeProcessor
cons public init()
intf org.openide.cookies.InstanceCookie
intf org.openide.loaders.XMLDataObject$Processor
meth public java.lang.Class instanceClass()
meth public java.lang.Object instanceCreate() throws java.io.IOException,java.lang.ClassNotFoundException
meth public java.lang.String instanceName()
meth public void attachTo(org.openide.filesystems.FileObject)
meth public void attachTo(org.openide.loaders.XMLDataObject)
supr java.lang.Object
hfds ATTR_ACTION_NAME,ATTR_BROWSEABLE,ATTR_COMBINATION_MIN_OPTIONALS,ATTR_COMBINATION_ORDER,ATTR_COMBINATION_TIPTEXT_KEY,ATTR_COMBINE_ABSORBALL,ATTR_COMBINE_ANNOTATIONTYPE,ATTR_COMBINE_MIN,ATTR_COMBINE_OPTIONAL,ATTR_CUSTOM_SIDEBAR_COLOR,ATTR_INHERIT_FOREGROUND_COLOR,ATTR_PRIORITY,ATTR_SEVERITY,ATTR_TYPE_ACTIONS,ATTR_TYPE_CONTENTTYPE,ATTR_TYPE_DESCRIPTION_KEY,ATTR_TYPE_FOREGROUND,ATTR_TYPE_GLYPH,ATTR_TYPE_HIGHLIGHT,ATTR_TYPE_LOCALIZING_BUNDLE,ATTR_TYPE_NAME,ATTR_TYPE_TYPE,ATTR_TYPE_VISIBLE,ATTR_TYPE_WAVEUNDERLINE,ATTR_USE_CUSTOM_SIDEBAR_COLOR,ATTR_USE_HIHGLIGHT_COLOR,ATTR_USE_WAVE_UNDERLINE_COLOR,DTD_PUBLIC_ID,DTD_PUBLIC_ID11,DTD_SYSTEM_ID,DTD_SYSTEM_ID11,LOG,TAG_COMBINATION,TAG_COMBINE,TAG_TYPE,annotationType,xmlDataObject
hcls Handler

CLSS public org.netbeans.modules.editor.options.AnnotationTypesFolder
meth protected java.lang.Object createInstance(org.openide.cookies.InstanceCookie[]) throws java.io.IOException,java.lang.ClassNotFoundException
meth public static org.netbeans.modules.editor.options.AnnotationTypesFolder getAnnotationTypesFolder()
meth public void saveAnnotationType(org.netbeans.editor.AnnotationType)
supr org.openide.loaders.FolderInstance
hfds FOLDER,annotationTypes,folder

CLSS public org.netbeans.modules.editor.options.AnnotationTypesNode
cons public init()
meth protected org.openide.nodes.Sheet createSheet()
meth protected org.openide.util.actions.SystemAction[] createActions()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.nodes.AbstractNode
hfds HELP_ID,ICON_BASE
hcls AnnotationTypesSubnodes

CLSS public abstract interface org.openide.awt.DynamicMenuContent
fld public final static java.lang.String HIDE_WHEN_DISABLED = "hideWhenDisabled"
meth public abstract javax.swing.JComponent[] getMenuPresenters()
meth public abstract javax.swing.JComponent[] synchMenuPresenters(javax.swing.JComponent[])

CLSS public abstract interface org.openide.cookies.InstanceCookie
innr public abstract interface static Of
intf org.openide.nodes.Node$Cookie
meth public abstract java.lang.Class<?> instanceClass() throws java.io.IOException,java.lang.ClassNotFoundException
meth public abstract java.lang.Object instanceCreate() throws java.io.IOException,java.lang.ClassNotFoundException
meth public abstract java.lang.String instanceName()

CLSS public org.openide.filesystems.FileChangeAdapter
cons public init()
intf org.openide.filesystems.FileChangeListener
meth public void fileAttributeChanged(org.openide.filesystems.FileAttributeEvent)
meth public void fileChanged(org.openide.filesystems.FileEvent)
meth public void fileDataCreated(org.openide.filesystems.FileEvent)
meth public void fileDeleted(org.openide.filesystems.FileEvent)
meth public void fileFolderCreated(org.openide.filesystems.FileEvent)
meth public void fileRenamed(org.openide.filesystems.FileRenameEvent)
supr java.lang.Object

CLSS public abstract interface org.openide.filesystems.FileChangeListener
intf java.util.EventListener
meth public abstract void fileAttributeChanged(org.openide.filesystems.FileAttributeEvent)
meth public abstract void fileChanged(org.openide.filesystems.FileEvent)
meth public abstract void fileDataCreated(org.openide.filesystems.FileEvent)
meth public abstract void fileDeleted(org.openide.filesystems.FileEvent)
meth public abstract void fileFolderCreated(org.openide.filesystems.FileEvent)
meth public abstract void fileRenamed(org.openide.filesystems.FileRenameEvent)

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

CLSS public abstract org.openide.loaders.FolderInstance
cons public init(org.openide.loaders.DataFolder)
cons public init(org.openide.loaders.DataObject$Container)
fld protected org.openide.loaders.DataFolder folder
intf org.openide.cookies.InstanceCookie
meth protected abstract java.lang.Object createInstance(org.openide.cookies.InstanceCookie[]) throws java.io.IOException,java.lang.ClassNotFoundException
meth protected java.lang.Object instanceForCookie(org.openide.loaders.DataObject,org.openide.cookies.InstanceCookie) throws java.io.IOException,java.lang.ClassNotFoundException
meth protected org.openide.cookies.InstanceCookie acceptContainer(org.openide.loaders.DataObject$Container)
meth protected org.openide.cookies.InstanceCookie acceptCookie(org.openide.cookies.InstanceCookie) throws java.io.IOException,java.lang.ClassNotFoundException
meth protected org.openide.cookies.InstanceCookie acceptDataObject(org.openide.loaders.DataObject)
meth protected org.openide.cookies.InstanceCookie acceptFolder(org.openide.loaders.DataFolder)
meth protected org.openide.util.Task postCreationTask(java.lang.Runnable)
meth public final void instanceFinished()
meth public java.lang.Class<?> instanceClass() throws java.io.IOException,java.lang.ClassNotFoundException
meth public java.lang.Object instanceCreate() throws java.io.IOException,java.lang.ClassNotFoundException
meth public java.lang.String instanceName()
meth public java.lang.String toString()
meth public void recreate()
meth public void run()
meth public void waitFinished()
supr org.openide.util.Task
hfds CURRENT,LAST_CURRENT,PROCESSOR,container,creationSequence,creationTask,err,listener,map,object,precreateInstances,recognizingTask,waitFor
hcls HoldInstance,Listener

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

CLSS public abstract interface static org.openide.loaders.XMLDataObject$Processor
 outer org.openide.loaders.XMLDataObject
 anno 0 java.lang.Deprecated()
intf org.openide.nodes.Node$Cookie
meth public abstract void attachTo(org.openide.loaders.XMLDataObject)

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

CLSS public final org.openide.text.NbDocument
fld public final static java.lang.Object GUARDED
fld public final static java.lang.String BREAKPOINT_STYLE_NAME = "NbBreakpointStyle"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String CURRENT_STYLE_NAME = "NbCurrentStyle"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String ERROR_STYLE_NAME = "NbErrorStyle"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String NORMAL_STYLE_NAME = "NbNormalStyle"
 anno 0 java.lang.Deprecated()
innr public abstract interface static Annotatable
innr public abstract interface static CustomEditor
innr public abstract interface static CustomToolbar
innr public abstract interface static PositionBiasable
innr public abstract interface static Printable
innr public abstract interface static WriteLockable
meth public static <%0 extends javax.swing.undo.UndoableEdit> {%%0} getEditToBeRedoneOfType(org.openide.cookies.EditorCookie,java.lang.Class<{%%0}>)
meth public static <%0 extends javax.swing.undo.UndoableEdit> {%%0} getEditToBeUndoneOfType(org.openide.cookies.EditorCookie,java.lang.Class<{%%0}>)
meth public static boolean openDocument(org.openide.util.Lookup$Provider,int,int,org.openide.text.Line$ShowOpenType,org.openide.text.Line$ShowVisibilityType)
meth public static boolean openDocument(org.openide.util.Lookup$Provider,int,org.openide.text.Line$ShowOpenType,org.openide.text.Line$ShowVisibilityType)
meth public static int findLineColumn(javax.swing.text.StyledDocument,int)
meth public static int findLineNumber(javax.swing.text.StyledDocument,int)
meth public static int findLineOffset(javax.swing.text.StyledDocument,int)
meth public static java.lang.Object findPageable(javax.swing.text.StyledDocument)
meth public static javax.swing.JEditorPane findRecentEditorPane(org.openide.cookies.EditorCookie)
meth public static javax.swing.text.Element findLineRootElement(javax.swing.text.StyledDocument)
meth public static javax.swing.text.Position createPosition(javax.swing.text.Document,int,javax.swing.text.Position$Bias) throws javax.swing.text.BadLocationException
meth public static javax.swing.text.StyledDocument getDocument(org.openide.util.Lookup$Provider)
meth public static void addAnnotation(javax.swing.text.StyledDocument,javax.swing.text.Position,int,org.openide.text.Annotation)
meth public static void insertGuarded(javax.swing.text.StyledDocument,int,java.lang.String) throws javax.swing.text.BadLocationException
meth public static void markBreakpoint(javax.swing.text.StyledDocument,int)
 anno 0 java.lang.Deprecated()
meth public static void markCurrent(javax.swing.text.StyledDocument,int)
 anno 0 java.lang.Deprecated()
meth public static void markError(javax.swing.text.StyledDocument,int)
 anno 0 java.lang.Deprecated()
meth public static void markGuarded(javax.swing.text.StyledDocument,int,int)
meth public static void markNormal(javax.swing.text.StyledDocument,int)
 anno 0 java.lang.Deprecated()
meth public static void removeAnnotation(javax.swing.text.StyledDocument,org.openide.text.Annotation)
meth public static void runAtomic(javax.swing.text.StyledDocument,java.lang.Runnable)
meth public static void runAtomicAsUser(javax.swing.text.StyledDocument,java.lang.Runnable) throws javax.swing.text.BadLocationException
meth public static void unmarkGuarded(javax.swing.text.StyledDocument,int,int)
supr java.lang.Object
hfds ATTR_ADD,ATTR_REMOVE
hcls DocumentRenderer

CLSS public abstract interface static org.openide.text.NbDocument$Annotatable
 outer org.openide.text.NbDocument
intf javax.swing.text.Document
meth public abstract void addAnnotation(javax.swing.text.Position,int,org.openide.text.Annotation)
meth public abstract void removeAnnotation(org.openide.text.Annotation)

CLSS public abstract interface static org.openide.text.NbDocument$CustomEditor
 outer org.openide.text.NbDocument
intf javax.swing.text.Document
meth public abstract java.awt.Component createEditor(javax.swing.JEditorPane)

CLSS public abstract interface static org.openide.text.NbDocument$CustomToolbar
 outer org.openide.text.NbDocument
intf javax.swing.text.Document
meth public abstract javax.swing.JToolBar createToolbar(javax.swing.JEditorPane)

CLSS public abstract interface static org.openide.text.NbDocument$PositionBiasable
 outer org.openide.text.NbDocument
intf javax.swing.text.Document
meth public abstract javax.swing.text.Position createPosition(int,javax.swing.text.Position$Bias) throws javax.swing.text.BadLocationException

CLSS public abstract interface static org.openide.text.NbDocument$Printable
 outer org.openide.text.NbDocument
intf javax.swing.text.Document
meth public abstract java.text.AttributedCharacterIterator[] createPrintIterators()

CLSS public abstract interface static org.openide.text.NbDocument$WriteLockable
 outer org.openide.text.NbDocument
intf javax.swing.text.Document
meth public abstract void runAtomic(java.lang.Runnable)
meth public abstract void runAtomicAsUser(java.lang.Runnable) throws javax.swing.text.BadLocationException

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

CLSS public abstract interface org.openide.util.LookupListener
intf java.util.EventListener
meth public abstract void resultChanged(org.openide.util.LookupEvent)

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

CLSS public org.openide.util.Task
cons protected init()
cons public init(java.lang.Runnable)
fld public final static org.openide.util.Task EMPTY
intf java.lang.Runnable
meth protected final void notifyFinished()
meth protected final void notifyRunning()
meth public boolean waitFinished(long) throws java.lang.InterruptedException
meth public final boolean isFinished()
meth public java.lang.String toString()
meth public void addTaskListener(org.openide.util.TaskListener)
meth public void removeTaskListener(org.openide.util.TaskListener)
meth public void run()
meth public void waitFinished()
supr java.lang.Object
hfds LOG,RP,finished,list,overrides,run

CLSS public abstract interface org.openide.util.actions.ActionPerformer
 anno 0 java.lang.Deprecated()
meth public abstract void performAction(org.openide.util.actions.SystemAction)

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

