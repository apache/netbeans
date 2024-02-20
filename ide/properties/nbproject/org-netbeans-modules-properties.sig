#Signature file v4.1
#Version 1.78

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

CLSS public abstract interface java.util.Comparator<%0 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
meth public <%0 extends java.lang.Comparable<? super {%%0}>> java.util.Comparator<{java.util.Comparator%0}> thenComparing(java.util.function.Function<? super {java.util.Comparator%0},? extends {%%0}>)
meth public <%0 extends java.lang.Object> java.util.Comparator<{java.util.Comparator%0}> thenComparing(java.util.function.Function<? super {java.util.Comparator%0},? extends {%%0}>,java.util.Comparator<? super {%%0}>)
meth public abstract boolean equals(java.lang.Object)
meth public abstract int compare({java.util.Comparator%0},{java.util.Comparator%0})
meth public java.util.Comparator<{java.util.Comparator%0}> reversed()
meth public java.util.Comparator<{java.util.Comparator%0}> thenComparing(java.util.Comparator<? super {java.util.Comparator%0}>)
meth public java.util.Comparator<{java.util.Comparator%0}> thenComparingDouble(java.util.function.ToDoubleFunction<? super {java.util.Comparator%0}>)
meth public java.util.Comparator<{java.util.Comparator%0}> thenComparingInt(java.util.function.ToIntFunction<? super {java.util.Comparator%0}>)
meth public java.util.Comparator<{java.util.Comparator%0}> thenComparingLong(java.util.function.ToLongFunction<? super {java.util.Comparator%0}>)
meth public static <%0 extends java.lang.Comparable<? super {%%0}>> java.util.Comparator<{%%0}> naturalOrder()
meth public static <%0 extends java.lang.Comparable<? super {%%0}>> java.util.Comparator<{%%0}> reverseOrder()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Comparable<? super {%%1}>> java.util.Comparator<{%%0}> comparing(java.util.function.Function<? super {%%0},? extends {%%1}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Comparator<{%%0}> comparing(java.util.function.Function<? super {%%0},? extends {%%1}>,java.util.Comparator<? super {%%1}>)
meth public static <%0 extends java.lang.Object> java.util.Comparator<{%%0}> comparingDouble(java.util.function.ToDoubleFunction<? super {%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Comparator<{%%0}> comparingInt(java.util.function.ToIntFunction<? super {%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Comparator<{%%0}> comparingLong(java.util.function.ToLongFunction<? super {%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Comparator<{%%0}> nullsFirst(java.util.Comparator<? super {%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Comparator<{%%0}> nullsLast(java.util.Comparator<? super {%%0}>)

CLSS public abstract interface java.util.EventListener

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object

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

CLSS public abstract javax.swing.AbstractCellEditor
cons public init()
fld protected javax.swing.event.ChangeEvent changeEvent
fld protected javax.swing.event.EventListenerList listenerList
intf java.io.Serializable
intf javax.swing.CellEditor
meth protected void fireEditingCanceled()
meth protected void fireEditingStopped()
meth public boolean isCellEditable(java.util.EventObject)
meth public boolean shouldSelectCell(java.util.EventObject)
meth public boolean stopCellEditing()
meth public javax.swing.event.CellEditorListener[] getCellEditorListeners()
meth public void addCellEditorListener(javax.swing.event.CellEditorListener)
meth public void cancelCellEditing()
meth public void removeCellEditorListener(javax.swing.event.CellEditorListener)
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

CLSS public abstract interface javax.swing.CellEditor
meth public abstract boolean isCellEditable(java.util.EventObject)
meth public abstract boolean shouldSelectCell(java.util.EventObject)
meth public abstract boolean stopCellEditing()
meth public abstract java.lang.Object getCellEditorValue()
meth public abstract void addCellEditorListener(javax.swing.event.CellEditorListener)
meth public abstract void cancelCellEditing()
meth public abstract void removeCellEditorListener(javax.swing.event.CellEditorListener)

CLSS public javax.swing.DefaultCellEditor
cons public init(javax.swing.JCheckBox)
cons public init(javax.swing.JComboBox)
cons public init(javax.swing.JTextField)
 anno 0 java.beans.ConstructorProperties(java.lang.String[] value=["component"])
fld protected int clickCountToStart
fld protected javax.swing.DefaultCellEditor$EditorDelegate delegate
fld protected javax.swing.JComponent editorComponent
innr protected EditorDelegate
intf javax.swing.table.TableCellEditor
intf javax.swing.tree.TreeCellEditor
meth public boolean isCellEditable(java.util.EventObject)
meth public boolean shouldSelectCell(java.util.EventObject)
meth public boolean stopCellEditing()
meth public int getClickCountToStart()
meth public java.awt.Component getComponent()
meth public java.awt.Component getTableCellEditorComponent(javax.swing.JTable,java.lang.Object,boolean,int,int)
meth public java.awt.Component getTreeCellEditorComponent(javax.swing.JTree,java.lang.Object,boolean,boolean,boolean,int)
meth public java.lang.Object getCellEditorValue()
meth public void cancelCellEditing()
meth public void setClickCountToStart(int)
supr javax.swing.AbstractCellEditor

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

CLSS public abstract interface javax.swing.table.TableCellEditor
intf javax.swing.CellEditor
meth public abstract java.awt.Component getTableCellEditorComponent(javax.swing.JTable,java.lang.Object,boolean,int,int)

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

CLSS public abstract interface javax.swing.tree.TreeCellEditor
intf javax.swing.CellEditor
meth public abstract java.awt.Component getTreeCellEditorComponent(javax.swing.JTree,java.lang.Object,boolean,boolean,boolean,int)

CLSS public abstract interface org.netbeans.api.actions.Closable
meth public abstract boolean close()

CLSS public abstract interface org.netbeans.api.actions.Editable
meth public abstract void edit()

CLSS public abstract interface org.netbeans.api.actions.Openable
meth public abstract void open()

CLSS public abstract interface org.netbeans.api.actions.Printable
meth public abstract void print()

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

CLSS public org.netbeans.modules.properties.BundleEditPanel
cons public init(org.netbeans.modules.properties.BundleStructure,org.netbeans.modules.properties.PropertiesTableModel)
cons public init(org.netbeans.modules.properties.PropertiesDataObject,org.netbeans.modules.properties.PropertiesTableModel)
 anno 0 java.lang.Deprecated()
intf java.beans.PropertyChangeListener
meth protected void saveEditorValue(boolean)
meth protected void stopEditing()
meth public boolean requestFocusInWindow()
meth public javax.swing.JTable getTable()
meth public void propertyChange(java.beans.PropertyChangeEvent)
supr javax.swing.JPanel
hfds addButton,autoResizeCheck,buttonPanel,commentLabel,jScrollPane2,jScrollPane3,lastSelectedBundleKey,lastSelectedColumn,listener,removeButton,scrollPane,selectionUpdateDisabled,serialVersionUID,settings,structure,table,tablePanel,textComment,textValue,valueLabel,valuePanel
hcls BundleTable,ModifiedListener,TableViewColumnModel,TableViewHeaderRenderer,TableViewRenderer

CLSS public org.netbeans.modules.properties.BundleNodeCustomizer
cons public init(org.netbeans.modules.properties.PropertiesDataObject)
supr javax.swing.JPanel
hfds addLocale,jScrollPane1,localesLabel,localesList,nameLabel,nameText,propDataObject,removeLocales

CLSS public org.netbeans.modules.properties.BundleStructure
cons protected init()
cons public init(org.netbeans.modules.properties.PropertiesDataObject)
meth protected void buildKeySet()
meth public boolean getSortOrder()
meth public int getEntryCount()
meth public int getEntryIndexByFileName(java.lang.String)
meth public int getKeyCount()
meth public int getKeyIndexByName(java.lang.String)
meth public int getSortIndex()
meth public java.lang.String findFreeKey(java.lang.String)
meth public java.lang.String keyAt(int)
meth public java.lang.String[] getAllData(java.lang.String)
meth public java.lang.String[] getKeys()
meth public org.netbeans.modules.properties.Element$ItemElem getItem(int,int)
meth public org.netbeans.modules.properties.Element$ItemElem getItem(int,java.lang.String)
meth public org.netbeans.modules.properties.Element$ItemElem getItem(java.lang.String,java.lang.String)
meth public org.netbeans.modules.properties.PropertiesFileEntry getEntryByFileName(java.lang.String)
meth public org.netbeans.modules.properties.PropertiesFileEntry getNthEntry(int)
meth public void addItem(java.lang.String,java.lang.String,java.lang.String,java.lang.String,boolean)
meth public void addPropertyBundleListener(org.netbeans.modules.properties.PropertyBundleListener)
meth public void removeItem(java.lang.String)
meth public void removePropertyBundleListener(org.netbeans.modules.properties.PropertyBundleListener)
meth public void setAllData(java.lang.String,java.lang.String[])
meth public void sort(int)
supr java.lang.Object
hfds comparator,entries,keyList,obj,propBundleSupport,propListener
hcls KeyComparator

CLSS public abstract org.netbeans.modules.properties.Element
cons protected init(org.openide.text.PositionBounds)
fld protected org.openide.text.PositionBounds bounds
innr public abstract static Basic
innr public static CommentElem
innr public static ItemElem
innr public static KeyElem
innr public static ValueElem
intf java.io.Serializable
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract java.lang.String getDocumentString()
meth public final void print()
meth public java.lang.String toString()
meth public org.openide.text.PositionBounds getBounds()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds support

CLSS public abstract static org.netbeans.modules.properties.Element$Basic
 outer org.netbeans.modules.properties.Element
cons protected init(org.openide.text.PositionBounds,java.lang.String)
fld protected java.lang.String value
meth protected static void appendIsoControlChar(java.lang.StringBuilder,char)
meth public boolean equals(java.lang.Object)
meth public java.lang.String getValue()
meth public java.lang.String toString()
meth public void setValue(java.lang.String)
supr org.netbeans.modules.properties.Element
hfds hexaDigitChars

CLSS public static org.netbeans.modules.properties.Element$CommentElem
 outer org.netbeans.modules.properties.Element
cons protected init(org.openide.text.PositionBounds,java.lang.String)
meth public boolean equals(java.lang.Object)
meth public java.lang.String getDocumentString()
supr org.netbeans.modules.properties.Element$Basic
hfds serialVersionUID

CLSS public static org.netbeans.modules.properties.Element$ItemElem
 outer org.netbeans.modules.properties.Element
cons protected init(org.openide.text.PositionBounds,org.netbeans.modules.properties.Element$KeyElem,org.netbeans.modules.properties.Element$ValueElem,org.netbeans.modules.properties.Element$CommentElem)
fld public final static java.lang.String PROP_ITEM_COMMENT = "comment"
fld public final static java.lang.String PROP_ITEM_KEY = "key"
fld public final static java.lang.String PROP_ITEM_VALUE = "value"
intf org.openide.nodes.Node$Cookie
meth public boolean equals(java.lang.Object)
meth public java.lang.String getComment()
meth public java.lang.String getDocumentString()
meth public java.lang.String getKey()
meth public java.lang.String getValue()
meth public java.lang.String toString()
meth public org.netbeans.modules.properties.Element$CommentElem getCommentElem()
meth public org.netbeans.modules.properties.Element$KeyElem getKeyElem()
meth public org.netbeans.modules.properties.Element$ValueElem getValueElem()
meth public void setComment(java.lang.String)
meth public void setKey(java.lang.String)
meth public void setValue(java.lang.String)
supr org.netbeans.modules.properties.Element
hfds comment,key,parent,serialVersionUID,value

CLSS public static org.netbeans.modules.properties.Element$KeyElem
 outer org.netbeans.modules.properties.Element
cons protected init(org.openide.text.PositionBounds,java.lang.String)
meth public boolean equals(java.lang.Object)
meth public java.lang.String getDocumentString()
supr org.netbeans.modules.properties.Element$Basic
hfds serialVersionUID

CLSS public static org.netbeans.modules.properties.Element$ValueElem
 outer org.netbeans.modules.properties.Element
cons protected init(org.openide.text.PositionBounds,java.lang.String)
meth public java.lang.String getDocumentString()
supr org.netbeans.modules.properties.Element$Basic
hfds serialVersionUID

CLSS public org.netbeans.modules.properties.FileEntryNode
cons public init(org.netbeans.modules.properties.PresentableFileEntry,org.openide.nodes.Children)
meth protected org.openide.nodes.Sheet createSheet()
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public boolean canDestroy()
meth public final boolean canCopy()
meth public final boolean canCut()
meth public javax.swing.Action getPreferredAction()
meth public org.netbeans.modules.properties.PresentableFileEntry getFileEntry()
meth public org.openide.util.actions.SystemAction getDefaultAction()
 anno 0 java.lang.Deprecated()
meth public void destroy() throws java.io.IOException
meth public void setName(java.lang.String)
supr org.openide.nodes.AbstractNode
hfds entry,serialVersionUID
hcls PropL

CLSS public org.netbeans.modules.properties.FindPanel
cons public init()
meth public boolean requestFocusInWindow()
meth public javax.swing.JButton[] getButtons()
meth public javax.swing.JCheckBox getBackwardCheck()
meth public javax.swing.JCheckBox getHighlightCheck()
meth public javax.swing.JCheckBox getMatchCaseCheck()
meth public javax.swing.JCheckBox getRowCheck()
meth public javax.swing.JCheckBox getWrapCheck()
meth public javax.swing.JComboBox getComboBox()
supr javax.swing.JPanel
hfds backwardCheck,cancelButton,findButton,findCombo,findLabel,highlightCheck,matchCaseCheck,rowCheck,wrapCheck

CLSS public org.netbeans.modules.properties.FindPerformer
fld public final static java.lang.String TABLE_SEARCH_RESULT = "table.search.result"
intf java.beans.PropertyChangeListener
meth public boolean isHighlightSearch()
meth public java.lang.String getFindString()
meth public static org.netbeans.modules.properties.FindPerformer getFindPerformer(javax.swing.JTable)
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void closeFindDialog()
meth public void propertyChange(java.beans.PropertyChangeEvent)
supr javax.swing.AbstractAction
hfds backwardSearch,findDialog,findNextActionListener,findPreviousActionListener,findString,highlightSearch,history,matchCaseSearch,rowSearch,searchValues,settings,softRef,table,toggleHighlightListener,wrapSearch

CLSS public final org.netbeans.modules.properties.KeyComparator
cons public init()
intf java.util.Comparator<java.lang.String>
meth public int compare(java.lang.String,java.lang.String)
supr java.lang.Object

CLSS public org.netbeans.modules.properties.KeyNode
cons public init(org.netbeans.modules.properties.PropertiesStructure,java.lang.String)
intf java.beans.PropertyChangeListener
meth protected org.openide.nodes.Sheet createSheet()
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public boolean canDestroy()
meth public boolean hasCustomizer()
meth public final boolean canCopy()
meth public final boolean canCut()
meth public final boolean canRename()
meth public java.awt.Component getCustomizer()
meth public javax.swing.Action getPreferredAction()
meth public org.netbeans.modules.properties.Element$ItemElem getItem()
meth public void destroy() throws java.io.IOException
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void setName(java.lang.String)
supr org.openide.nodes.AbstractNode
hfds itemKey,propStructure,serialVersionUID

CLSS public org.netbeans.modules.properties.LangRenameAction
cons public init()
meth protected void performAction(org.openide.nodes.Node[])
supr org.openide.actions.RenameAction
hfds serialVersionUID

CLSS public org.netbeans.modules.properties.LocaleNodeCustomizer
cons public init(org.netbeans.modules.properties.PropertiesFileEntry)
supr javax.swing.JPanel
hfds addKeyButton,changeNameButton,entry,jPanel1,jScrollPane1,keyLabel,keyList,nameLabel,nameText,removeKeyButton

CLSS public org.netbeans.modules.properties.LocalePanel
cons public init()
cons public init(java.util.Locale)
fld public final static java.lang.String PROP_CUSTOMIZED_LOCALE = "customized_locale"
meth public java.util.Locale getLocale()
supr javax.swing.JPanel
hfds countryCombo,countryLabel,jScrollPane1,languageCombo,languageLabel,locale,localeLabel,localeText,supportedLabel,supportedList,supportedLocales,variantCombo,variantLabel
hcls NbBasicComboBoxRenderer

CLSS public abstract org.netbeans.modules.properties.PresentableFileEntry
cons public init(org.openide.loaders.MultiDataObject,org.openide.filesystems.FileObject)
intf org.openide.nodes.Node$Cookie
meth protected abstract org.openide.nodes.Node createNodeDelegate()
meth protected final org.openide.nodes.CookieSet getCookieSet()
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected final void setCookieSet(org.openide.nodes.CookieSet)
 anno 0 java.lang.Deprecated()
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public abstract boolean isCopyAllowed()
meth public abstract boolean isDeleteAllowed()
meth public abstract boolean isMoveAllowed()
meth public abstract boolean isRenameAllowed()
meth public abstract org.openide.util.HelpCtx getHelpCtx()
meth public boolean isModified()
meth public boolean isShadowAllowed()
meth public boolean isTemplate()
meth public final org.openide.loaders.DataFolder getFolder()
meth public final org.openide.nodes.Node getNodeDelegate()
meth public final void setTemplate(boolean) throws java.io.IOException
meth public java.lang.String getName()
meth public org.openide.filesystems.FileObject rename(java.lang.String) throws java.io.IOException
meth public org.openide.filesystems.FileObject renameEntry(java.lang.String) throws java.io.IOException
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void delete() throws java.io.IOException
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setModified(boolean)
supr org.openide.loaders.FileEntry
hfds changeSupport,cookieL,cookieSet,modif,nodeDelegate,nodeDelegateMutex,serialVersionUID

CLSS public final org.netbeans.modules.properties.PropertiesDataLoader
cons public init()
fld public final static char PRB_SEPARATOR_CHAR = '_'
fld public final static java.lang.String PROP_EXTENSIONS = "extensions"
meth protected java.lang.String actionsContext()
meth protected java.lang.String defaultDisplayName()
meth protected org.openide.filesystems.FileObject findPrimaryFile(org.openide.filesystems.FileObject)
meth protected org.openide.loaders.MultiDataObject createMultiObject(org.openide.filesystems.FileObject) throws java.io.IOException
meth protected org.openide.loaders.MultiDataObject$Entry createPrimaryEntry(org.openide.loaders.MultiDataObject,org.openide.filesystems.FileObject)
meth protected org.openide.loaders.MultiDataObject$Entry createSecondaryEntry(org.openide.loaders.MultiDataObject,org.openide.filesystems.FileObject)
meth public org.openide.loaders.ExtensionList getExtensions()
meth public void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void setExtensions(org.openide.loaders.ExtensionList)
meth public void writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr org.openide.loaders.MultiFileLoader
hfds PROPERTIES_EXTENSION,PROPERTIES_MIME_TYPE,encodingRef,knownCountries,knownLanguages,nestedView,serialVersionUID

CLSS public final org.netbeans.modules.properties.PropertiesDataLoaderBeanInfo
cons public init()
meth public java.awt.Image getIcon(int)
meth public java.beans.BeanInfo[] getAdditionalBeanInfo()
meth public java.beans.PropertyDescriptor[] getPropertyDescriptors()
supr java.beans.SimpleBeanInfo

CLSS public org.netbeans.modules.properties.PropertiesDataNode
cons public init(org.openide.loaders.DataObject,org.openide.nodes.Children,org.openide.util.Lookup)
meth protected org.openide.nodes.Sheet createSheet()
meth public boolean hasCustomizer()
meth public java.awt.Component getCustomizer()
meth public org.openide.util.datatransfer.NewType[] getNewTypes()
meth public void createPasteTypes(java.awt.datatransfer.Transferable,java.util.List<org.openide.util.datatransfer.PasteType>)
supr org.openide.loaders.DataNode
hfds PROPERTY_ENCODING,dataObjectListener,multiLocale
hcls EntryPasteType,NameUpdater,NewLocaleType,ProjectEncodingProperty

CLSS public final org.netbeans.modules.properties.PropertiesDataObject
cons public init(org.openide.filesystems.FileObject,org.netbeans.modules.properties.PropertiesDataLoader) throws org.openide.loaders.DataObjectExistsException
intf org.openide.nodes.CookieSet$Factory
meth protected int associateLookup()
meth protected org.netbeans.modules.properties.BundleStructure findBundleStructure()
meth protected org.netbeans.modules.properties.BundleStructure getBundleStructureOrNull()
meth protected org.openide.filesystems.FileObject handleMove(org.openide.loaders.DataFolder) throws java.io.IOException
meth protected org.openide.filesystems.FileObject handleRename(java.lang.String) throws java.io.IOException
meth protected org.openide.loaders.DataObject handleCopy(org.openide.loaders.DataFolder) throws java.io.IOException
meth protected org.openide.nodes.Node createNodeDelegate()
meth protected void handleDelete() throws java.io.IOException
meth protected void setBundleStructure(org.netbeans.modules.properties.BundleStructure)
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} createCookie(java.lang.Class<{%%0}>)
meth public org.netbeans.modules.properties.BundleStructure getBundleStructure()
meth public org.netbeans.modules.properties.PropertiesOpen getOpenSupport()
meth public static java.util.Comparator<java.lang.String> getSecondaryFilesComparator()
supr org.openide.loaders.MultiDataObject
hfds LOG,OPEN_SUPPORT_LOCK,bundleStructure,lookup,openSupport,pasteSuffix,serialVersionUID
hcls PropertiesChildren

CLSS public org.netbeans.modules.properties.PropertiesEditorSupport
cons public init(org.netbeans.modules.properties.PropertiesFileEntry)
innr public PropertiesEditAt
innr public static PropertiesEditor
intf java.io.Serializable
intf org.openide.cookies.CloseCookie
intf org.openide.cookies.EditCookie
intf org.openide.cookies.EditorCookie$Observable
intf org.openide.cookies.PrintCookie
intf org.openide.loaders.SaveAsCapable
meth protected boolean canClose()
meth protected boolean notifyModified()
meth protected java.lang.String messageHtmlName()
meth protected java.lang.String messageName()
meth protected java.lang.String messageOpened()
meth protected java.lang.String messageOpening()
meth protected java.lang.String messageSave()
meth protected java.lang.String messageToolTip()
meth protected javax.swing.text.StyledDocument createStyledDocument(javax.swing.text.EditorKit)
meth protected org.openide.awt.UndoRedo$Manager createUndoRedoManager()
meth protected org.openide.text.CloneableEditorSupport$Pane createPane()
meth protected org.openide.util.Task reloadDocument()
meth protected void initializeCloneableEditor(org.openide.text.CloneableEditor)
meth protected void loadFromStreamToKit(javax.swing.text.StyledDocument,java.io.InputStream,javax.swing.text.EditorKit) throws java.io.IOException,javax.swing.text.BadLocationException
meth protected void notifyClosed()
meth protected void notifyUnmodified()
meth protected void saveFromKitToStream(javax.swing.text.StyledDocument,javax.swing.text.EditorKit,java.io.OutputStream) throws java.io.IOException,javax.swing.text.BadLocationException
meth public boolean hasOpenedEditorComponent()
meth public boolean hasOpenedTableComponent()
meth public final org.openide.loaders.DataObject getDataObject()
meth public void open()
meth public void saveAs(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
supr org.openide.text.CloneableEditorSupport
hfds LOG,charset,fsStatusListener,myEntry,serialVersionUID
hcls Env,Environment,EnvironmentListener,FsStatusListener,PropertiesEditorLookup,SaveImpl,StampFlag,UndoRedoStampFlagManager

CLSS public org.netbeans.modules.properties.PropertiesEditorSupport$PropertiesEditAt
 outer org.netbeans.modules.properties.PropertiesEditorSupport
intf org.openide.cookies.EditCookie
meth public void edit()
meth public void setKey(java.lang.String)
supr java.lang.Object
hfds key

CLSS public static org.netbeans.modules.properties.PropertiesEditorSupport$PropertiesEditor
 outer org.netbeans.modules.properties.PropertiesEditorSupport
cons public init()
cons public init(org.openide.util.Lookup)
fld protected org.netbeans.modules.properties.PropertiesFileEntry entry
intf org.netbeans.core.spi.multiview.MultiViewElement
meth protected boolean closeLast()
meth public java.awt.Image getIcon()
meth public javax.swing.JComponent getToolbarRepresentation()
meth public javax.swing.JComponent getVisualRepresentation()
meth public org.netbeans.core.spi.multiview.CloseOperationState canCloseElement()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public org.openide.util.Lookup getLookup()
meth public void componentActivated()
meth public void componentClosed()
meth public void componentDeactivated()
meth public void componentHidden()
meth public void componentOpened()
meth public void componentShowing()
meth public void open()
meth public void requestActive()
meth public void requestVisible()
meth public void setMultiViewCallback(org.netbeans.core.spi.multiview.MultiViewElementCallback)
meth public void updateName()
supr org.openide.text.CloneableEditor
hfds bar,callback,originalLookup,peLookup,saveCookieLNode,serialVersionUID

CLSS public final org.netbeans.modules.properties.PropertiesEncoding
cons public init()
meth public java.nio.charset.Charset getEncoding(org.openide.filesystems.FileObject)
supr org.netbeans.spi.queries.FileEncodingQueryImplementation
hfds PROP_CHARSET_NAME
hcls PropCharset,PropCharsetDecoder,PropCharsetEncoder

CLSS public org.netbeans.modules.properties.PropertiesFileEntry
intf org.openide.nodes.CookieSet$Factory
meth protected org.netbeans.modules.properties.PropertiesEditorSupport getPropertiesEditor()
meth protected org.openide.nodes.Node createNodeDelegate()
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} createCookie(java.lang.Class<{%%0}>)
meth public boolean isCopyAllowed()
meth public boolean isDeleteAllowed()
meth public boolean isMoveAllowed()
meth public boolean isRenameAllowed()
meth public org.netbeans.modules.properties.StructHandler getHandler()
meth public org.openide.filesystems.FileObject copy(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public org.openide.filesystems.FileObject createFromTemplate(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public org.openide.filesystems.FileObject move(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public org.openide.filesystems.FileObject rename(java.lang.String) throws java.io.IOException
meth public org.openide.filesystems.FileObject renameEntry(java.lang.String) throws java.io.IOException
meth public org.openide.nodes.Children getChildren()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void delete() throws java.io.IOException
supr org.netbeans.modules.properties.PresentableFileEntry
hfds LOG,atomicUndoRedoFlag,basicName,editorSupport,list,propStruct,serialVersionUID,weakList
hcls PropKeysChildren

CLSS public final org.netbeans.modules.properties.PropertiesLocaleNode
cons public init(org.netbeans.modules.properties.PropertiesFileEntry)
intf org.openide.nodes.CookieSet$Factory
intf org.openide.nodes.Node$Cookie
meth protected org.openide.util.actions.SystemAction[] createActions()
meth protected void createPasteTypes(java.awt.datatransfer.Transferable,java.util.List<org.openide.util.datatransfer.PasteType>)
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} createCookie(java.lang.Class<{%%0}>)
meth public boolean canRename()
meth public boolean hasCustomizer()
meth public java.awt.Component getCustomizer()
meth public java.lang.String getHtmlDisplayName()
meth public java.lang.String getName()
meth public javax.swing.Action getPreferredAction()
meth public org.openide.util.datatransfer.NewType[] getNewTypes()
meth public void setName(java.lang.String)
supr org.netbeans.modules.properties.FileEntryNode
hfds LOCALE_ICON_BASE,fsStatusListener
hcls FSListener,KeyPasteType,NewPropertyType

CLSS public org.netbeans.modules.properties.PropertiesOpen
cons public init(org.netbeans.modules.properties.BundleStructure)
cons public init(org.netbeans.modules.properties.PropertiesDataObject)
 anno 0 java.lang.Deprecated()
fld protected org.openide.awt.UndoRedo undoRedoManager
innr public PropertiesOpenAt
innr public static PropertiesCloneableTopComponent
intf org.openide.cookies.CloseCookie
intf org.openide.cookies.OpenCookie
meth protected boolean canClose()
meth protected java.lang.String messageOpened()
meth protected java.lang.String messageOpening()
meth protected org.openide.windows.CloneableTopComponent createCloneableTopComponent()
meth protected void addDataObject(org.netbeans.modules.properties.PropertiesDataObject)
meth protected void removeModifiedListener(org.netbeans.modules.properties.PropertiesDataObject)
meth public boolean hasOpenedTableComponent()
meth public org.openide.awt.UndoRedo getUndoRedo()
supr org.openide.windows.CloneableOpenSupport
hfds LOG,UPDATE_LOCK,atomicUndoRedoFlag,bundleStructure,dataObjectList,modifL,propDataObject,weakModifiedListeners
hcls CompoundUndoRedoManager,Environment,ModifiedListener

CLSS public static org.netbeans.modules.properties.PropertiesOpen$PropertiesCloneableTopComponent
 outer org.netbeans.modules.properties.PropertiesOpen
cons public init()
cons public init(org.netbeans.modules.properties.BundleStructure)
cons public init(org.netbeans.modules.properties.PropertiesDataObject)
 anno 0 java.lang.Deprecated()
meth protected boolean closeLast()
meth protected java.lang.String preferredID()
meth protected org.openide.windows.CloneableTopComponent createClonedObject()
meth protected void componentHidden()
meth protected void dataObjectAdded(org.netbeans.modules.properties.PropertiesDataObject)
meth protected void dataObjectRemoved(org.netbeans.modules.properties.PropertiesDataObject)
meth public boolean canClose()
meth public int getPersistenceType()
meth public java.awt.Image getIcon()
meth public org.openide.awt.UndoRedo getUndoRedo()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void open()
meth public void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void requestActive()
meth public void writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr org.openide.windows.CloneableTopComponent
hfds UPDATE_LOCK,bundleStructure,dataObjectsList,nameUpdaterListener,propDataObject,serialVersionUID,weakNameUpdateListeners
hcls NameUpdater

CLSS public org.netbeans.modules.properties.PropertiesOpen$PropertiesOpenAt
 outer org.netbeans.modules.properties.PropertiesOpen
intf org.openide.cookies.OpenCookie
meth public void open()
meth public void setKey(java.lang.String)
supr java.lang.Object
hfds entry,key

CLSS public org.netbeans.modules.properties.PropertiesRequestProcessor
cons public init()
meth public static org.openide.util.RequestProcessor getInstance()
supr java.lang.Object
hfds requestProcessor

CLSS public org.netbeans.modules.properties.PropertiesStructure
cons public init(org.openide.text.PositionBounds,java.util.Map<java.lang.String,org.netbeans.modules.properties.Element$ItemElem>)
meth public boolean addItem(java.lang.String,java.lang.String,java.lang.String)
meth public boolean deleteItem(java.lang.String)
meth public boolean renameItem(java.lang.String,java.lang.String)
meth public java.lang.String getDocumentString()
meth public java.lang.String toString()
meth public java.util.Iterator<org.netbeans.modules.properties.Element$ItemElem> allItems()
meth public org.netbeans.modules.properties.Element$ItemElem getItem(java.lang.String)
meth public org.netbeans.modules.properties.StructHandler getParent()
meth public void update(org.netbeans.modules.properties.PropertiesStructure)
supr org.netbeans.modules.properties.Element
hfds handler,items,serialVersionUID

CLSS public org.netbeans.modules.properties.PropertiesTableCellEditor
cons public init(javax.swing.JTextField,javax.swing.text.JTextComponent,javax.swing.text.JTextComponent,javax.swing.JLabel,javax.swing.event.DocumentListener)
meth public java.awt.Component getTableCellEditorComponent(javax.swing.JTable,java.lang.Object,boolean,int,int)
supr javax.swing.DefaultCellEditor
hfds commentComponent,isKeyCell,listener,serialVersionUID,settings,valueComponent
hcls PropertiesEditorDelegate

CLSS public org.netbeans.modules.properties.PropertiesTableModel
cons public init(org.netbeans.modules.properties.BundleStructure)
meth public boolean isCellEditable(int,int)
meth public int getColumnCount()
meth public int getRowCount()
meth public java.lang.Class getColumnClass(int)
meth public java.lang.Object getValueAt(int,int)
meth public java.lang.String getColumnName(int)
meth public java.lang.String toString()
meth public void fireTableColumnChanged(int)
meth public void setValueAt(java.lang.Object,int,int)
supr javax.swing.table.AbstractTableModel
hfds bundleListener,serialVersionUID,structure
hcls CancelSelector,StringPair,TablePropertyBundleListener

CLSS public org.netbeans.modules.properties.PropertyBundleEvent
cons public init(java.lang.Object,int)
cons public init(java.lang.Object,java.lang.String)
cons public init(java.lang.Object,java.lang.String,java.lang.String)
fld protected int changeType
fld protected java.lang.String entryName
fld protected java.lang.String itemName
fld public final static int CHANGE_ALL = 2
fld public final static int CHANGE_FILE = 3
fld public final static int CHANGE_ITEM = 4
fld public final static int CHANGE_STRUCT = 1
meth public int getChangeType()
meth public java.lang.String getEntryName()
meth public java.lang.String getItemName()
meth public java.lang.String toString()
supr java.util.EventObject
hfds serialVersionUID

CLSS public abstract interface org.netbeans.modules.properties.PropertyBundleListener
intf java.util.EventListener
meth public abstract void bundleChanged(org.netbeans.modules.properties.PropertyBundleEvent)

CLSS public org.netbeans.modules.properties.PropertyBundleSupport
cons public init(java.lang.Object)
fld protected javax.swing.event.EventListenerList listenerList
intf java.io.Serializable
meth public void addPropertyBundleListener(org.netbeans.modules.properties.PropertyBundleListener)
meth public void fireBundleChanged(org.netbeans.modules.properties.PropertyBundleEvent)
meth public void fireBundleDataChanged()
meth public void fireBundleStructureChanged()
meth public void fireFileChanged(java.lang.String)
meth public void fireItemChanged(java.lang.String,java.lang.String)
meth public void removePropertyBundleListener(org.netbeans.modules.properties.PropertyBundleListener)
supr java.lang.Object
hfds serialVersionUID,source

CLSS public org.netbeans.modules.properties.StructHandler
cons public init(org.netbeans.modules.properties.PropertiesFileEntry)
meth public org.netbeans.modules.properties.PropertiesFileEntry getEntry()
meth public org.netbeans.modules.properties.PropertiesStructure getStructure()
supr java.lang.Object
hfds parserWRef,parsingAllowed,parsingTaskWRef,propFileEntry,propStructureSRef,serialVersionUID

CLSS public abstract org.netbeans.modules.properties.TableViewSettings
cons protected init()
fld public final static java.awt.Color HIGHLIGHT_DEFAULT_BACKGROUND
fld public final static java.awt.Color HIGHLIGHT_DEFAULT_COLOR
fld public final static java.awt.Color KEY_DEFAULT_BACKGROUND
fld public final static java.awt.Color KEY_DEFAULT_COLOR
fld public final static java.awt.Color SHADOW_DEFAULT_COLOR
fld public final static java.awt.Color VALUE_DEFAULT_BACKGROUND
fld public final static java.awt.Color VALUE_DEFAULT_COLOR
fld public final static javax.swing.KeyStroke[] FIND_NEXT_DEFAULT_KEYSTROKES
fld public final static javax.swing.KeyStroke[] FIND_PREVIOUS_DEFAULT_KEYSTROKES
fld public final static javax.swing.KeyStroke[] TOGGLE_HIGHLIGHT_DEFAULT_KEYSTROKES
meth public abstract java.awt.Color getHighlightBackground()
meth public abstract java.awt.Color getHighlightColor()
meth public abstract java.awt.Color getKeyBackground()
meth public abstract java.awt.Color getKeyColor()
meth public abstract java.awt.Color getShadowColor()
meth public abstract java.awt.Color getValueBackground()
meth public abstract java.awt.Color getValueColor()
meth public abstract java.awt.Font getFont()
meth public abstract javax.swing.KeyStroke[] getKeyStrokesFindNext()
meth public abstract javax.swing.KeyStroke[] getKeyStrokesFindPrevious()
meth public abstract javax.swing.KeyStroke[] getKeyStrokesToggleHighlight()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public static org.netbeans.modules.properties.TableViewSettings getDefault()
supr java.lang.Object
hfds delegatingSettings,registrations
hcls DelegatingSettings,HardcodedSettings

CLSS public final org.netbeans.modules.properties.Util
cons public init()
fld public final static char PRB_SEPARATOR_CHAR = '_'
fld public final static int LABEL_FIRST_PART_LENGTH = 10
fld public final static java.lang.String HELP_ID_ADDING = "propfiles.adding"
fld public final static java.lang.String HELP_ID_ADDLOCALE = "propfiles.addlocale"
fld public final static java.lang.String HELP_ID_CREATING = "propfiles.creating"
fld public final static java.lang.String HELP_ID_EDITLOCALE = "propfiles.editlocale"
fld public final static java.lang.String HELP_ID_MODIFYING = "propfiles.modifying"
fld public final static java.lang.String HELP_ID_PROPERTIES = "propfiles.prop"
meth public static java.lang.String assembleName(java.lang.String,java.lang.String)
meth public static java.lang.String getCountry(java.lang.String)
meth public static java.lang.String getLanguage(java.lang.String)
meth public static java.lang.String getLocaleLabel(org.openide.loaders.MultiDataObject$Entry)
meth public static java.lang.String getLocaleSuffix(org.openide.loaders.MultiDataObject$Entry)
meth public static java.lang.String getVariant(java.lang.String)
meth public static java.lang.String stringToKey(java.lang.String)
meth public static org.netbeans.modules.properties.PropertiesDataObject createPropertiesDataObject(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public static void createLocaleFile(org.netbeans.modules.properties.PropertiesDataObject,java.lang.String,boolean)
supr java.lang.Object

CLSS public org.netbeans.modules.properties.UtilConvert
fld public final static java.lang.String keyValueSeparators = "=: \u0009\r\n\u000c"
fld public final static java.lang.String strictKeyValueSeparators = "=:"
fld public final static java.lang.String whiteSpaceChars = " \u0009\r\n\u000c"
meth public static java.lang.String charsToUnicodes(java.lang.String)
meth public static java.lang.String charsToUnicodes(java.lang.String,boolean)
meth public static java.lang.String escapeComment(java.lang.String)
meth public static java.lang.String escapeJavaSpecialChars(java.lang.String)
meth public static java.lang.String escapeLineContinuationChar(java.lang.String)
meth public static java.lang.String escapeOutsideSpaces(java.lang.String)
meth public static java.lang.String escapePropertiesSpecialChars(java.lang.String)
meth public static java.lang.String loadConvert(java.lang.String)
meth public static java.lang.String saveConvert(java.lang.String)
meth public static java.lang.String unicodesToChars(java.lang.String)
supr java.lang.Object
hfds hexDigit,specialSaveChars

CLSS public abstract org.netbeans.spi.queries.FileEncodingQueryImplementation
cons public init()
meth protected static void throwUnknownEncoding()
meth public abstract java.nio.charset.Charset getEncoding(org.openide.filesystems.FileObject)
supr java.lang.Object

CLSS public org.openide.actions.RenameAction
cons public init()
meth protected boolean asynchronous()
meth protected boolean enable(org.openide.nodes.Node[])
meth protected boolean surviveFocusChange()
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.NodeAction
hfds RP

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

CLSS public abstract interface org.openide.loaders.SaveAsCapable
meth public abstract void saveAs(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException

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

CLSS public abstract interface static org.openide.text.CloneableEditorSupport$Pane
 outer org.openide.text.CloneableEditorSupport
meth public abstract javax.swing.JEditorPane getEditorPane()
meth public abstract org.openide.windows.CloneableTopComponent getComponent()
meth public abstract void ensureVisible()
meth public abstract void updateName()

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

