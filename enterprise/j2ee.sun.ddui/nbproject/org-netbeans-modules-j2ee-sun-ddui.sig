#Signature file v4.1
#Version 1.59.0

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

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

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

CLSS public abstract interface javax.accessibility.Accessible
meth public abstract javax.accessibility.AccessibleContext getAccessibleContext()

CLSS public abstract interface javax.enterprise.deploy.spi.DeploymentConfiguration
meth public abstract javax.enterprise.deploy.model.DeployableObject getDeployableObject()
meth public abstract javax.enterprise.deploy.spi.DConfigBeanRoot getDConfigBeanRoot(javax.enterprise.deploy.model.DDBeanRoot) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException
meth public abstract javax.enterprise.deploy.spi.DConfigBeanRoot restoreDConfigBean(java.io.InputStream,javax.enterprise.deploy.model.DDBeanRoot) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException
meth public abstract void removeDConfigBean(javax.enterprise.deploy.spi.DConfigBeanRoot) throws javax.enterprise.deploy.spi.exceptions.BeanNotFoundException
meth public abstract void restore(java.io.InputStream) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException
meth public abstract void save(java.io.OutputStream) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException
meth public abstract void saveDConfigBean(java.io.OutputStream,javax.enterprise.deploy.spi.DConfigBeanRoot) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException

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

CLSS public abstract org.netbeans.modules.glassfish.eecommon.api.config.GlassfishConfiguration
cons protected init(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule,org.netbeans.modules.glassfish.eecommon.api.config.J2eeModuleHelper,org.netbeans.modules.glassfish.tooling.data.GlassFishVersion) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
cons protected init(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule,org.netbeans.modules.glassfish.tooling.data.GlassFishVersion) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
cons public init()
 anno 0 java.lang.Deprecated()
fld protected final java.io.File primarySunDD
fld protected final java.io.File secondarySunDD
fld protected final org.netbeans.modules.glassfish.eecommon.api.config.J2eeModuleHelper moduleHelper
fld protected final org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule module
fld protected org.netbeans.modules.glassfish.eecommon.api.config.DescriptorListener descriptorListener
fld protected org.netbeans.modules.glassfish.tooling.data.GlassFishVersion version
innr public final static !enum ChangeOperation
intf org.netbeans.modules.j2ee.deployment.plugins.spi.config.ContextRootConfiguration
intf org.netbeans.modules.j2ee.deployment.plugins.spi.config.DatasourceConfiguration
intf org.netbeans.modules.j2ee.deployment.plugins.spi.config.EjbResourceConfiguration
intf org.netbeans.modules.j2ee.deployment.plugins.spi.config.MessageDestinationConfiguration
meth protected <%0 extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean> {%%0} findNamedBean(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean,java.lang.String,java.lang.String,java.lang.String)
meth protected org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider getProvider(java.io.File)
meth protected org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion getInstalledAppServerVersion(java.io.File)
meth protected org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion getTargetAppServerVersion()
meth protected org.netbeans.modules.j2ee.sun.dd.api.RootInterface getSunDDRoot(boolean) throws java.io.IOException
meth protected org.openide.filesystems.FileObject getSunDD(java.io.File,boolean) throws java.io.IOException
meth protected void createDefaultSunDD(java.io.File) throws java.io.IOException
meth protected void displayError(java.lang.Exception,java.lang.String)
meth protected void handleEventRelatedException(java.lang.Exception)
meth protected void handleEventRelatedIOException(java.io.IOException)
meth public <%0 extends java.lang.Object> org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<{%%0}> getMetadataModel(java.lang.Class<{%%0}>)
meth public abstract boolean supportsCreateDatasource()
meth public abstract boolean supportsCreateMessageDestination()
meth public abstract java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.Datasource> getDatasources() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.MessageDestination> getMessageDestinations() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract org.netbeans.modules.j2ee.deployment.common.api.Datasource createDatasource(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException,org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException
meth public abstract org.netbeans.modules.j2ee.deployment.common.api.MessageDestination createMessageDestination(java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public final org.netbeans.modules.j2ee.dd.api.common.RootInterface getStandardRootDD()
meth public final org.netbeans.modules.j2ee.dd.api.webservices.Webservices getWebServicesRootDD()
meth public final static org.openide.util.Pair<java.io.File,java.lang.Boolean> getExistingResourceFile(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule,org.netbeans.modules.glassfish.tooling.data.GlassFishVersion)
meth public final static org.openide.util.Pair<java.io.File,java.lang.Boolean> getNewResourceFile(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule,org.netbeans.modules.glassfish.tooling.data.GlassFishVersion)
meth public java.lang.String findDatasourceJndiName(java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public java.lang.String findDatasourceJndiNameForEjb(java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public java.lang.String findJndiNameForEjb(java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public java.lang.String findMessageDestinationName(java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public java.lang.String getContextRoot() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public org.netbeans.modules.glassfish.eecommon.api.config.J2EEBaseVersion getJ2eeVersion()
meth public org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule getJ2eeModule()
meth public org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion getAppServerVersion()
meth public org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion getMaxASVersion()
meth public org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion getMinASVersion()
meth public org.netbeans.modules.j2ee.sun.dd.api.RootInterface getSunDDRoot(java.io.File,boolean) throws java.io.IOException
meth public static org.netbeans.modules.glassfish.eecommon.api.config.GlassfishConfiguration getConfiguration(java.io.File)
meth public static void addConfiguration(java.io.File,org.netbeans.modules.glassfish.eecommon.api.config.GlassfishConfiguration)
meth public static void removeConfiguration(java.io.File)
meth public void bindDatasourceReference(java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public void bindDatasourceReferenceForEjb(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public void bindEjbReference(java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public void bindEjbReferenceForEjb(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public void bindMdbToMessageDestination(java.lang.String,java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public void bindMessageDestinationReference(java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public void bindMessageDestinationReferenceForEjb(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public void dispose()
meth public void saveConfiguration(java.io.OutputStream) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public void setAppServerVersion(org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
meth public void setContextRoot(java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
supr java.lang.Object
hfds LOGGER,RESOURCE_FILES,RESOURCE_FILES_SUFFIX,RP,appServerVersion,configurationMap,configurationMonitor,defaultcr,deferredAppServerChange,maxASVersion,minASVersion,sunServerIds

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.config.ContextRootConfiguration
meth public abstract java.lang.String getContextRoot() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void setContextRoot(java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.config.DatasourceConfiguration
meth public abstract boolean supportsCreateDatasource()
meth public abstract java.lang.String findDatasourceJndiName(java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract java.lang.String findDatasourceJndiNameForEjb(java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.Datasource> getDatasources() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract org.netbeans.modules.j2ee.deployment.common.api.Datasource createDatasource(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException,org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException
meth public abstract void bindDatasourceReference(java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void bindDatasourceReferenceForEjb(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.config.EjbResourceConfiguration
meth public abstract java.lang.String findJndiNameForEjb(java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void bindEjbReference(java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void bindEjbReferenceForEjb(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.config.MessageDestinationConfiguration
meth public abstract boolean supportsCreateMessageDestination()
meth public abstract java.lang.String findMessageDestinationName(java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.MessageDestination> getMessageDestinations() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract org.netbeans.modules.j2ee.deployment.common.api.MessageDestination createMessageDestination(java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void bindMdbToMessageDestination(java.lang.String,java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void bindMessageDestinationReference(java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void bindMessageDestinationReferenceForEjb(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException

CLSS public abstract interface org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction<%0 extends java.lang.Object, %1 extends java.lang.Object>
meth public abstract {org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction%1} run({org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction%0}) throws java.lang.Exception

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.impl.common.DDProviderDataObject
meth public abstract java.io.Reader createReader() throws java.io.IOException
meth public abstract org.openide.filesystems.FileLock getDataLock()
meth public abstract void writeModel(org.netbeans.modules.j2ee.sun.dd.api.RootInterface) throws java.io.IOException
meth public abstract void writeModel(org.netbeans.modules.j2ee.sun.dd.api.RootInterface,org.openide.filesystems.FileLock)

CLSS public abstract org.netbeans.modules.j2ee.sun.ddloaders.DDMultiViewDataObject
cons public init(org.openide.filesystems.FileObject,org.openide.loaders.MultiFileLoader) throws org.openide.loaders.DataObjectExistsException
intf org.netbeans.modules.j2ee.sun.dd.impl.common.DDProviderDataObject
intf org.netbeans.modules.j2ee.sun.ddloaders.api.TransactionSupport
meth protected abstract boolean isDocumentParseable()
meth protected abstract boolean isModelCreated()
meth protected abstract org.netbeans.modules.j2ee.sun.dd.api.RootInterface getDDModel()
meth protected abstract void parseDocument() throws java.io.IOException
meth protected abstract void validateDocument() throws java.io.IOException
meth protected java.lang.String generateDocumentFromModel(org.netbeans.modules.j2ee.sun.dd.api.RootInterface)
meth public java.io.InputStream createInputStream()
meth public java.io.Reader createReader() throws java.io.IOException
meth public org.netbeans.modules.j2ee.sun.ddloaders.api.Transaction openTransaction()
meth public org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer getModelSynchronizer()
meth public org.openide.filesystems.FileLock getDataLock()
meth public void checkParseable()
meth public void modelUpdatedFromUI()
meth public void notifyError(java.lang.Exception)
meth public void writeModel(org.netbeans.modules.j2ee.sun.dd.api.RootInterface) throws java.io.IOException
meth public void writeModel(org.netbeans.modules.j2ee.sun.dd.api.RootInterface,org.openide.filesystems.FileLock)
supr org.netbeans.modules.xml.multiview.XmlMultiViewDataObject
hfds HANDLE_UNPARSABLE_TIMEOUT,modelSynchronizer,transactionReference
hcls ModelSynchronizer

CLSS public final org.netbeans.modules.j2ee.sun.ddloaders.DDType
fld public final static java.lang.String APP_CLI_MIME_TYPE = "text/x-dd-sun-app-client+xml"
fld public final static java.lang.String APP_MIME_TYPE = "text/x-dd-sun-application+xml"
fld public final static java.lang.String CMP_MIME_TYPE = "text/x-sun-cmp-mapping+xml"
fld public final static java.lang.String EJB_MIME_TYPE = "text/x-dd-sun-ejb-jar+xml"
fld public final static java.lang.String RSRC_MIME_TYPE = "text/x-sun-resource+xml"
fld public final static java.lang.String WEB_MIME_TYPE = "text/x-dd-sun-web+xml"
fld public final static org.netbeans.modules.j2ee.sun.ddloaders.DDType DD_GF_APPLICATION
fld public final static org.netbeans.modules.j2ee.sun.ddloaders.DDType DD_GF_APP_CLIENT
fld public final static org.netbeans.modules.j2ee.sun.ddloaders.DDType DD_GF_EJB_JAR
fld public final static org.netbeans.modules.j2ee.sun.ddloaders.DDType DD_GF_RESOURCE
fld public final static org.netbeans.modules.j2ee.sun.ddloaders.DDType DD_GF_WEB_APP
fld public final static org.netbeans.modules.j2ee.sun.ddloaders.DDType DD_SUN_APPLICATION
fld public final static org.netbeans.modules.j2ee.sun.ddloaders.DDType DD_SUN_APP_CLIENT
fld public final static org.netbeans.modules.j2ee.sun.ddloaders.DDType DD_SUN_CMP_MAPPINGS
fld public final static org.netbeans.modules.j2ee.sun.ddloaders.DDType DD_SUN_EJB_JAR
fld public final static org.netbeans.modules.j2ee.sun.ddloaders.DDType DD_SUN_RESOURCE
fld public final static org.netbeans.modules.j2ee.sun.ddloaders.DDType DD_SUN_WEB_APP
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getDescriptorFileName()
meth public org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type getEditorModuleType()
meth public static org.netbeans.modules.j2ee.sun.ddloaders.DDType getDDType(java.lang.String)
supr java.lang.Object
hfds APP_CLI_MIME_TYPE_SUFFIX,APP_MIME_TYPE_SUFFIX,CMP_MIME_TYPE_SUFFIX,EJB_MIME_TYPE_SUFFIX,GLASSFISH_MIME_TYPE_PREFIX,IPLANET_MIME_TYPE_PREFIX,NAME_GFAPPCLIENT,NAME_GFAPPLICATION,NAME_GFEJBJAR,NAME_GFRESOURCE,NAME_GFWEBAPP,NAME_SUNAPPCLIENT,NAME_SUNAPPLICATION,NAME_SUNCMPMAPPING,NAME_SUNEJBJAR,NAME_SUNRESOURCE,NAME_SUNWEBAPP,RSRC_MIME_TYPE_SUFFIX,SUN_MIME_TYPE_PREFIX,WEB_MIME_TYPE_SUFFIX,descriptorName,fileToMimeSuffixMap,fileToTypeMap,moduleType

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataLoader
cons public init()
cons public init(java.lang.String)
meth protected java.lang.String actionsContext()
meth protected java.lang.String defaultDisplayName()
meth protected org.openide.filesystems.FileObject findPrimaryFile(org.openide.filesystems.FileObject)
meth protected org.openide.loaders.MultiDataObject createMultiObject(org.openide.filesystems.FileObject) throws java.io.IOException
meth protected void initialize()
supr org.openide.loaders.UniFileLoader
hfds SUPPORTED_MIME_TYPES,serialVersionUID

CLSS public final org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataLoaderBeanInfo
cons public init()
meth public java.awt.Image getIcon(int)
meth public java.beans.BeanInfo[] getAdditionalBeanInfo()
meth public java.beans.PropertyDescriptor[] getPropertyDescriptors()
supr java.beans.SimpleBeanInfo

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataNode
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject)
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject,org.openide.nodes.Children)
fld public final static java.lang.String PROPERTY_DOCUMENT_TYPE = "documentType"
meth protected org.openide.nodes.Sheet createSheet()
meth public java.awt.Image getIcon(int)
meth public java.lang.String getShortDescription()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.loaders.DataNode
hfds SUN_DD,dataObject,ddListener

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject
cons public init(org.openide.filesystems.FileObject,org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataLoader) throws org.openide.loaders.DataObjectExistsException
fld public final static java.lang.String PROP_DOCUMENT_DTD = "documentDTD"
meth protected boolean isModelCreated()
meth protected int getXMLMultiViewIndex()
meth protected java.lang.String getEditorMimeType()
meth protected java.lang.String getIconBaseForInvalidDocument()
meth protected java.lang.String getIconBaseForValidDocument()
meth protected java.lang.String getPrefixMark()
meth protected org.netbeans.modules.j2ee.sun.dd.api.RootInterface getDDModel()
meth protected org.openide.loaders.DataObject handleCopy(org.openide.loaders.DataFolder) throws java.io.IOException
meth protected org.openide.nodes.Node createNodeDelegate()
meth protected void parseDocument() throws java.io.IOException
meth protected void validateDocument() throws java.io.IOException
meth public boolean isDocumentParseable()
meth public org.netbeans.modules.glassfish.eecommon.api.config.J2EEBaseVersion getJ2eeModuleVersion()
meth public org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type getModuleType()
meth public org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion getASDDVersion()
meth public org.netbeans.modules.j2ee.sun.dd.api.RootInterface getDDRoot()
meth public org.netbeans.modules.xml.multiview.ToolBarMultiViewElement getActiveMVElement()
meth public org.openide.filesystems.FileObject getProjectDirectory()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static org.netbeans.core.spi.multiview.MultiViewElement createAppCliOverviewViewElement(org.openide.util.Lookup)
meth public static org.netbeans.core.spi.multiview.MultiViewElement createEjbMultiViewElement(org.openide.util.Lookup)
meth public static org.netbeans.core.spi.multiview.MultiViewElement createEjbOveriewMultiViewElement(org.openide.util.Lookup)
meth public static org.netbeans.core.spi.multiview.MultiViewElement createEnvMultiViewEnvironmentElement(org.openide.util.Lookup)
meth public static org.netbeans.core.spi.multiview.MultiViewElement createJmsMultiViewElement(org.openide.util.Lookup)
meth public static org.netbeans.core.spi.multiview.MultiViewElement createSecurityMultiViewSecurityElement(org.openide.util.Lookup)
meth public static org.netbeans.core.spi.multiview.MultiViewElement createServicesMultiViewElement(org.openide.util.Lookup)
meth public static org.netbeans.core.spi.multiview.MultiViewElement createServletsMultiViewElement(org.openide.util.Lookup)
meth public static org.netbeans.core.spi.multiview.MultiViewElement createWebOverviewMultiViewElement(org.openide.util.Lookup)
meth public static org.netbeans.modules.xml.multiview.XmlMultiViewElement createXmlMultiViewElement(org.openide.util.Lookup)
meth public void showElement(java.lang.Object)
supr org.netbeans.modules.j2ee.sun.ddloaders.DDMultiViewDataObject
hfds ddRootChangeListener,ddRootProxy,descriptorType,proxyMonitor,serialVersionUID,xmlIndex
hcls SunDDPropertyChangeListener

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.Utils
fld public final static java.lang.String ICON_BASE_DD_INVALID = "org/netbeans/modules/j2ee/ddloaders/resources/DDInvalidIcon"
fld public final static java.lang.String ICON_BASE_DD_VALID = "org/netbeans/modules/j2ee/ddloaders/resources/DDValidIcon"
fld public final static java.lang.String ICON_BASE_ERROR_BADGE = "org/netbeans/modules/j2ee/sun/ddloaders/resources/ErrorBadge"
meth public static boolean booleanValueOf(java.lang.String)
meth public static boolean notEmpty(java.lang.String)
meth public static boolean strEmpty(java.lang.String)
meth public static boolean strEquals(java.lang.String,java.lang.String)
meth public static boolean strEquivalent(java.lang.String,java.lang.String)
meth public static int strCompareTo(java.lang.String,java.lang.String)
meth public static java.lang.String getBeanDisplayName(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean,java.lang.String)
supr java.lang.Object
hfds booleanStrings

CLSS public abstract interface org.netbeans.modules.j2ee.sun.ddloaders.api.Transaction
meth public abstract void commit() throws java.io.IOException
meth public abstract void rollback()

CLSS public abstract interface org.netbeans.modules.j2ee.sun.ddloaders.api.TransactionSupport
intf org.openide.nodes.Node$Cookie
meth public abstract org.netbeans.modules.j2ee.sun.ddloaders.api.Transaction openTransaction()

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.BaseSectionNode
cons public init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,java.lang.Object,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion,java.lang.String,java.lang.String)
cons public init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,org.openide.nodes.Children,java.lang.Object,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion,java.lang.String,java.lang.String)
fld protected final org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion version
fld public final static java.lang.String ICON_BASE_EJB_REF_NODE = "org/netbeans/modules/j2ee/sun/ddloaders/resources/EjbRefIcon"
fld public final static java.lang.String ICON_BASE_ENDPOINT_NODE = "org/netbeans/modules/j2ee/sun/ddloaders/resources/WebServiceEndpointIcon"
fld public final static java.lang.String ICON_BASE_MESSAGE_DESTINATION_NODE = "org/netbeans/modules/j2ee/sun/ddloaders/resources/MessageDestinationIcon"
fld public final static java.lang.String ICON_BASE_MESSAGE_DESTINATION_REF_NODE = "org/netbeans/modules/j2ee/sun/ddloaders/resources/MessageDestinationRefIcon"
fld public final static java.lang.String ICON_BASE_MISC_NODE = "org/netbeans/modules/j2ee/sun/ddloaders/resources/MiscNodeIcon"
fld public final static java.lang.String ICON_BASE_PORT_INFO_NODE = "org/netbeans/modules/j2ee/sun/ddloaders/resources/ServiceRefIcon"
fld public final static java.lang.String ICON_BASE_REFERENCES_NODE = "org/netbeans/modules/j2ee/sun/ddloaders/resources/ReferencesIcon"
fld public final static java.lang.String ICON_BASE_RESOURCE_ENV_REF_NODE = "org/netbeans/modules/j2ee/sun/ddloaders/resources/ResourceEnvRefIcon"
fld public final static java.lang.String ICON_BASE_RESOURCE_REF_NODE = "org/netbeans/modules/j2ee/sun/ddloaders/resources/ResourceRefIcon"
fld public final static java.lang.String ICON_BASE_SECURITY_ROLE_MAPPING_NODE = "org/netbeans/modules/j2ee/sun/ddloaders/resources/SecurityRoleMappingIcon"
fld public final static java.lang.String ICON_BASE_SERVICE_REF_NODE = "org/netbeans/modules/j2ee/sun/ddloaders/resources/ServiceRefIcon"
fld public final static java.lang.String ICON_BASE_SERVLET_NODE = "org/netbeans/modules/j2ee/sun/ddloaders/resources/ServletIcon"
fld public final static java.lang.String ICON_EJB_ENTITY_NODE = "org/netbeans/modules/j2ee/sun/ddloaders/resources/EntityBean"
fld public final static java.lang.String ICON_EJB_GROUP_NODE = "org/netbeans/modules/j2ee/sun/ddloaders/resources/EjbGroupIcon"
fld public final static java.lang.String ICON_EJB_MDB_NODE = "org/netbeans/modules/j2ee/sun/ddloaders/resources/MessageBean"
fld public final static java.lang.String ICON_EJB_SESSION_NODE = "org/netbeans/modules/j2ee/sun/ddloaders/resources/SessionBean"
meth protected javax.swing.JButton getTitleButton(org.netbeans.modules.xml.multiview.ui.SectionNodePanel)
meth protected void disableTitleButtonFocusListener(org.netbeans.modules.xml.multiview.ui.SectionNodePanel)
meth protected void setHeaderSeparatorVisibility(org.netbeans.modules.xml.multiview.ui.SectionNodePanel,boolean)
meth public org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel createInnerPanel()
meth public org.netbeans.modules.xml.multiview.ui.SectionNodePanel getSectionNodePanel()
meth public void refreshSubtree()
supr org.netbeans.modules.xml.multiview.SectionNode

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.BaseSectionNodeInnerPanel
cons public init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
fld protected final boolean as80FeaturesVisible
fld protected final boolean as81FeaturesVisible
fld protected final boolean as90FeaturesVisible
fld protected final org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion version
fld public final java.util.ResourceBundle commonBundle
fld public final java.util.ResourceBundle customizerBundle
meth protected int getScaledMaxWidth()
meth public java.awt.Dimension getMaximumSize()
meth public java.awt.Dimension getPreferredSize()
meth public javax.swing.JComponent getErrorComponent(java.lang.String)
meth public void linkButtonPressed(java.lang.Object,java.lang.String)
meth public void setValue(javax.swing.JComponent,java.lang.Object)
supr org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel
hfds scaledMaxWidth

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.CustomSectionNodePanel
cons public init(org.netbeans.modules.xml.multiview.SectionNode)
fld public final static int MAX_WIDTH = 600
meth public java.awt.Dimension getPreferredSize()
meth public void setTitleIcon(java.lang.String)
supr org.netbeans.modules.xml.multiview.ui.SectionNodePanel

CLSS public abstract org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDMultiViewElement
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject)
fld protected org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject dataObject
fld protected org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor comp
meth protected abstract org.netbeans.modules.xml.multiview.ui.SectionView createView()
meth public org.netbeans.modules.xml.multiview.ui.SectionView getSectionView()
meth public void componentShowing()
supr org.netbeans.modules.xml.multiview.ToolBarMultiViewElement
hfds serialVersionUID,view

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDSectionNodeView
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject)
fld protected org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion version
fld protected org.netbeans.modules.j2ee.sun.dd.api.RootInterface rootDD
meth public org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer getModelSynchronizer()
meth public void dataModelPropertyChange(java.lang.Object,java.lang.String,java.lang.Object,java.lang.Object)
meth public void refreshView()
meth public void scheduleRefreshView()
meth public void setChildren(java.util.LinkedList<org.netbeans.modules.xml.multiview.SectionNode>)
meth public void setChildren(org.netbeans.modules.xml.multiview.SectionNode[])
supr org.netbeans.modules.xml.multiview.ui.SectionNodeView
hfds DD_REFRESH_DELAY,ddRefreshTask

CLSS public abstract org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDTextFieldEditorModel
cons public init(org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer,java.lang.String)
cons public init(org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer,java.lang.String,java.lang.String)
meth protected abstract org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean getBean()
meth protected java.lang.String getValue()
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean getBean(boolean)
meth protected void setValue(java.lang.String)
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.TextItemEditorModel
hfds attrProperty,nameProperty

CLSS public abstract org.netbeans.modules.j2ee.sun.ddloaders.multiview.MappingComboBoxHelper
cons public init(org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer,javax.swing.JComboBox)
intf java.awt.event.ActionListener
intf org.netbeans.modules.xml.multiview.Refreshable
meth public abstract org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.TextMapping getItemValue()
meth public abstract void setItemValue(org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.TextMapping)
meth public final void actionPerformed(java.awt.event.ActionEvent)
meth public javax.swing.JComboBox getComboBox()
meth public org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.TextMapping getValue()
meth public void refresh()
meth public void setValue(org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.TextMapping)
supr java.lang.Object
hfds comboBox,synchronizer

CLSS public abstract org.netbeans.modules.j2ee.sun.ddloaders.multiview.TextItemEditorModel
cons protected init(org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer,boolean)
cons protected init(org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer,boolean,boolean)
fld protected org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer synchronizer
meth protected abstract java.lang.String getValue()
meth protected abstract void setValue(java.lang.String)
meth protected boolean validate(java.lang.String)
meth public final boolean setItemValue(java.lang.String)
meth public final java.lang.String getItemValue()
meth public void documentUpdated()
supr org.netbeans.modules.xml.multiview.ItemEditorHelper$ItemEditorModel
hfds emptyAllowed,emptyIsNull

CLSS public abstract org.netbeans.modules.j2ee.sun.ddloaders.multiview.appclient.SunAppClientBaseView
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject)
fld protected final org.netbeans.modules.j2ee.sun.dd.api.client.SunApplicationClient sunAppClient
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDSectionNodeView

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.appclient.SunAppClientJWSNode
meth protected org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel createNodeInnerPanel()
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.BaseSectionNode

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.appclient.SunAppClientOverviewMultiViewElement
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject)
meth protected org.netbeans.modules.xml.multiview.ui.SectionView createView()
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDMultiViewElement
hfds serialVersionUID

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.appclient.SunAppClientView
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject)
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.appclient.SunAppClientBaseView

CLSS public abstract interface org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.BeanResolver
meth public abstract java.lang.String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean)
meth public abstract java.lang.String getBeanName(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth public abstract java.lang.String getStandardBeanNameProperty()
meth public abstract java.lang.String getSunBeanNameProperty()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean createBean()
meth public abstract void setBeanName(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean,java.lang.String)

CLSS public abstract org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader
cons public init(java.lang.String)
fld protected java.lang.String propertyName
innr public AppClientCommonReader
innr public CommonReader
innr public EjbJarCommonReader
innr public WebAppCommonReader
innr public WebservicesCommonReader
meth protected abstract java.util.Map<java.lang.String,java.lang.Object> genProperties(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean[])
meth protected org.netbeans.modules.j2ee.dd.api.common.CommonDDBean findEjbByName(org.netbeans.modules.j2ee.dd.api.ejb.EjbJar,java.lang.String)
meth protected org.netbeans.modules.j2ee.dd.api.common.CommonDDBean findEjbByName(org.netbeans.modules.j2ee.dd.api.ejb.Ejb[],java.lang.String)
meth protected org.netbeans.modules.j2ee.dd.api.common.CommonDDBean normalizeParent(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean)
meth protected void addMapString(java.util.Map<java.lang.String,java.lang.Object>,java.lang.String,java.lang.String)
meth public java.util.Map<java.lang.String,java.lang.Object> readAnnotations(org.openide.loaders.DataObject)
meth public java.util.Map<java.lang.String,java.lang.Object> readAppClientMetadata(org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<org.netbeans.modules.j2ee.dd.api.client.AppClientMetadata>) throws java.io.IOException
meth public java.util.Map<java.lang.String,java.lang.Object> readDescriptor(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean)
meth public java.util.Map<java.lang.String,java.lang.Object> readEjbJarMetadata(org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata>) throws java.io.IOException
meth public java.util.Map<java.lang.String,java.lang.Object> readWebAppMetadata(org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata>) throws java.io.IOException
meth public java.util.Map<java.lang.String,java.lang.Object> readWebservicesMetadata(org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata>) throws java.io.IOException
supr java.lang.Object
hfds methodMap

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader$AppClientCommonReader
 outer org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader)
intf org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction<org.netbeans.modules.j2ee.dd.api.client.AppClientMetadata,java.util.Map<java.lang.String,java.lang.Object>>
meth public java.util.Map<java.lang.String,java.lang.Object> run(org.netbeans.modules.j2ee.dd.api.client.AppClientMetadata) throws java.lang.Exception
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader$CommonReader

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader$CommonReader
 outer org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader)
meth public java.util.Map<java.lang.String,java.lang.Object> genCommonProperties(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean)
supr java.lang.Object

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader$EjbJarCommonReader
 outer org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader)
intf org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction<org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata,java.util.Map<java.lang.String,java.lang.Object>>
meth public java.util.Map<java.lang.String,java.lang.Object> run(org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata) throws java.lang.Exception
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader$CommonReader

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader$WebAppCommonReader
 outer org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader)
intf org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction<org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata,java.util.Map<java.lang.String,java.lang.Object>>
meth public java.util.Map<java.lang.String,java.lang.Object> run(org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata) throws java.lang.Exception
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader$CommonReader

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader$WebservicesCommonReader
 outer org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader)
intf org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction<org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata,java.util.Map<java.lang.String,java.lang.Object>>
meth public java.util.Map<java.lang.String,java.lang.Object> run(org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata) throws java.lang.Exception
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader$CommonReader

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.BeanResolver,org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean,java.util.Map<java.lang.String,java.lang.Object>,java.util.Map<java.lang.String,java.lang.Object>)
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.BeanResolver,org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean,java.util.Map<java.lang.String,java.lang.Object>,java.util.Map<java.lang.String,java.lang.Object>,boolean)
fld protected boolean virtual
fld protected final java.util.Map<java.lang.String,java.lang.Object> annotationMap
fld protected final java.util.Map<java.lang.String,java.lang.Object> standardMap
fld protected final org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean sunBean
fld protected final org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.BeanResolver resolver
fld public final static java.lang.String PROP_DESTINATION_TYPE = "DestinationType"
fld public final static java.lang.String PROP_EJB_LINK = "EjbLink"
fld public final static java.lang.String PROP_EJB_REF = "EjbRef"
fld public final static java.lang.String PROP_MSGDEST = "MessageDestination"
fld public final static java.lang.String PROP_MSGDEST_REF = "MessageDestinationRef"
fld public final static java.lang.String PROP_NAME = "Name"
fld public final static java.lang.String PROP_PERSISTENCE_TYPE = "PersistenceType"
fld public final static java.lang.String PROP_PORTCOMPONENT = "PortComponent"
fld public final static java.lang.String PROP_PORTCOMPONENT_LINK = "PortComponentLink"
fld public final static java.lang.String PROP_PORTCOMPONENT_REF = "PortComponentRef"
fld public final static java.lang.String PROP_RESOURCE_ENV_REF = "ResourceEnvRef"
fld public final static java.lang.String PROP_RESOURCE_REF = "ResourceRef"
fld public final static java.lang.String PROP_ROLE_NAME = "RoleName"
fld public final static java.lang.String PROP_RUNAS_ROLE = "RunAsRole"
fld public final static java.lang.String PROP_SECURITY_ROLE = "SecurityRole"
fld public final static java.lang.String PROP_SEI = "EndpointInterface"
fld public final static java.lang.String PROP_SERVICE_REF = "ServiceRef"
fld public final static java.lang.String PROP_SERVLET_LINK = "ServletLink"
fld public final static java.lang.String PROP_SESSION_TYPE = "SessionType"
fld public final static java.lang.String PROP_WEBSERVICE_DESC = "WebserviceDescription"
fld public final static java.lang.String PROP_WSDLPORT = "WsdlPort"
intf java.lang.Comparable<org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding>
meth public boolean equals(java.lang.Object)
meth public boolean hasAnnotationBinding()
meth public boolean hasStandardDDBinding()
meth public boolean isAnnotated()
meth public boolean isBound()
meth public boolean isVirtual()
meth public int compareTo(org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding)
meth public int hashCode()
meth public java.lang.Object getProperty(java.lang.String)
meth public java.lang.String getBeanName()
meth public java.lang.String getBindingName()
meth public java.lang.String toString()
meth public org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean getSunBean()
meth public org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding rebind(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth public void clearVirtual()
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DescriptorReader
meth public abstract java.util.Map<java.lang.String,java.lang.Object> readAnnotations()
meth public abstract java.util.Map<java.lang.String,java.lang.Object> readDescriptor()

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.EjbRefGroupNode
cons public init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean addBean(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean addNewBean()
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean[] getBeansFromModel()
meth protected org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader getModelReader()
meth protected org.netbeans.modules.xml.multiview.SectionNode createNode(org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding)
meth protected void removeBean(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth public java.lang.String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean)
meth public java.lang.String getBeanName(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth public java.lang.String getStandardBeanNameProperty()
meth public java.lang.String getSunBeanNameProperty()
meth public org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean createBean()
meth public void setBeanName(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean,java.lang.String)
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanGroupNode

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.EjbRefMetadataReader
cons public init(java.lang.String)
meth protected org.netbeans.modules.j2ee.dd.api.common.CommonDDBean normalizeParent(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean)
meth public java.util.Map<java.lang.String,java.lang.Object> genProperties(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean[])
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader
hfds parentName

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.EjbRefNode
cons public init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
meth protected org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel createNodeInnerPanel()
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanNode

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.EnvironmentMultiViewElement
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject)
meth protected org.netbeans.modules.xml.multiview.ui.SectionView createView()
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDMultiViewElement
hfds serialVersionUID

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.EnvironmentView
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject)
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDSectionNodeView

CLSS public abstract org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanGroupNode
cons public init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean,java.lang.String,java.lang.Class,java.lang.String,java.lang.String,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
fld protected final static java.lang.String PFX_DESTINATION = "destination"
fld protected final static java.lang.String PFX_DESTINATION_REF = "destination_ref"
fld protected final static java.lang.String PFX_EJB = "ejb"
fld protected final static java.lang.String PFX_EJB_REF = "ejb_ref"
fld protected final static java.lang.String PFX_ENDPOINT = "endpoint"
fld protected final static java.lang.String PFX_RESOURCE_ENV_REF = "resource_env_ref"
fld protected final static java.lang.String PFX_RESOURCE_REF = "resource_ref"
fld protected final static java.lang.String PFX_ROLE = "role"
fld protected final static java.lang.String PFX_SERVICE = "service"
fld protected final static java.lang.String PFX_SERVICE_REF = "service_ref"
fld protected final static java.lang.String PFX_SERVLET = "servlet"
fld protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean commonDD
fld public final static java.lang.String STANDARD_EJB_NAME = "EjbName"
fld public final static java.lang.String STANDARD_EJB_REF_NAME = "EjbRefName"
fld public final static java.lang.String STANDARD_MSGDEST_NAME = "MessageDestinationName"
fld public final static java.lang.String STANDARD_MSGDEST_REF_NAME = "MessageDestinationRefName"
fld public final static java.lang.String STANDARD_PORTCOMPONENT_NAME = "WebserviceDescriptionName"
fld public final static java.lang.String STANDARD_PORTCOMPONENT_REF_NAME = "PortComponentRef"
fld public final static java.lang.String STANDARD_RESOURCE_ENV_REF_NAME = "ResourceEnvRefName"
fld public final static java.lang.String STANDARD_RES_REF_NAME = "ResRefName"
fld public final static java.lang.String STANDARD_ROLE_NAME = "RoleName"
fld public final static java.lang.String STANDARD_SERVICE_REF_NAME = "ServiceRefName"
fld public final static java.lang.String STANDARD_SERVLET_NAME = "ServletName"
fld public final static java.lang.String STANDARD_WEBSERVICE_DESC_NAME = "WebserviceDescriptionName"
innr public final AddBeanAction
innr public static NamedChildren
intf org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.BeanResolver
intf org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DescriptorReader
meth protected <%0 extends java.lang.Object> org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<{%%0}> getMetadataModel(java.lang.Class<{%%0}>)
meth protected abstract org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean addBean(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth protected abstract org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean addNewBean()
meth protected abstract org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean[] getBeansFromModel()
meth protected abstract org.netbeans.modules.xml.multiview.SectionNode createNode(org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding)
meth protected abstract void removeBean(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth protected boolean isEventSource(java.lang.Object)
meth protected boolean setChecking(boolean)
meth protected java.lang.String getParentNodeName()
meth protected java.util.SortedSet<org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding> computeBindingSet()
meth protected java.util.SortedSet<org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding> computeBindingSet(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean[],java.util.Map<java.lang.String,java.lang.Object>,java.util.Map<java.lang.String,java.lang.Object>)
meth protected org.netbeans.modules.j2ee.dd.api.common.RootInterface getStandardRootDD()
meth protected org.netbeans.modules.j2ee.dd.api.common.RootInterface getWebServicesRootDD()
meth protected org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader getModelReader()
meth protected org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanGroupNode getParentGroupNode()
meth protected org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel createNodeInnerPanel()
meth protected org.netbeans.modules.xml.multiview.ui.SectionNodePanel createSectionNodePanel()
meth protected void check(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean,java.util.SortedSet<org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding>)
meth protected void enableAddAction(java.lang.String)
meth public java.lang.String getNewBeanId(java.lang.String)
meth public java.util.Map<java.lang.String,java.lang.Object> readAnnotations()
meth public java.util.Map<java.lang.String,java.lang.Object> readDescriptor()
meth public org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel createInnerPanel()
meth public org.netbeans.modules.xml.multiview.ui.SectionNodePanel getSectionNodePanel()
meth public void checkChildren(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.BaseSectionNode
hfds addBeanAction,beanClass,beanNameProperty,checking,doCheck,newBeanId,processor

CLSS public final org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanGroupNode$AddBeanAction
 outer org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanGroupNode
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanGroupNode,java.lang.String)
meth public void actionPerformed(java.awt.event.ActionEvent)
supr javax.swing.AbstractAction

CLSS public static org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanGroupNode$NamedChildren
 outer org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanGroupNode
cons public init()
intf java.util.Comparator<org.openide.nodes.Node>
meth public boolean add(org.openide.nodes.Node[])
meth public boolean remove(org.openide.nodes.Node[])
meth public int compare(org.openide.nodes.Node,org.openide.nodes.Node)
meth public org.openide.nodes.Node findChild(java.lang.String)
supr org.openide.nodes.Children$SortedMap<org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding>

CLSS public abstract org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanNode
cons protected init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding,java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
cons protected init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding,java.lang.String,java.lang.String,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
meth protected abstract org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel createNodeInnerPanel()
meth protected java.lang.String generateTitle()
meth protected org.netbeans.modules.xml.multiview.ui.SectionNodePanel createSectionNodePanel()
meth protected void enableRemoveAction()
meth public boolean addVirtualBean()
meth public org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding getBinding()
meth public org.netbeans.modules.xml.multiview.ui.SectionNodePanel getSectionNodePanel()
meth public void updateIcon()
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.BaseSectionNode
hfds BOUND_ANNOTATION_ICON,BOUND_STANDARD_ICON,UNBOUND_DD_ICON,VIRTUAL_DD_ICON,beanNameProperty,binding,customSectionNodePanel,removeBeanAction
hcls RemoveBeanAction

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.PortComponentRefMetadataReader
cons public init(java.lang.String,java.lang.String)
meth protected org.netbeans.modules.j2ee.dd.api.common.CommonDDBean findServiceRefByName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean,java.lang.String)
meth protected org.netbeans.modules.j2ee.dd.api.common.CommonDDBean findServiceRefByName(org.netbeans.modules.j2ee.dd.api.common.ServiceRef[],java.lang.String)
meth protected org.netbeans.modules.j2ee.dd.api.common.CommonDDBean normalizeParent(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean)
meth public java.util.Map<java.lang.String,java.lang.Object> genProperties(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean[])
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader
hfds ejbName,serviceRefName

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.PortInfoGroupNode
cons public init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
meth protected org.netbeans.modules.j2ee.dd.api.common.CommonDDBean[] getStandardBeansFromModel()
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean addBean(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean addNewBean()
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean[] getBeansFromModel()
meth protected org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader getModelReader()
meth protected org.netbeans.modules.xml.multiview.SectionNode createNode(org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding)
meth protected void removeBean(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth public java.lang.String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean)
meth public java.lang.String getBeanName(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth public java.lang.String getStandardBeanNameProperty()
meth public java.lang.String getSunBeanNameProperty()
meth public org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean createBean()
meth public void setBeanName(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean,java.lang.String)
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanGroupNode
hfds serviceRef

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.PortInfoNode
cons public init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
meth protected java.lang.String generateTitle()
meth protected org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel createNodeInnerPanel()
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanNode

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.ResourceEnvRefGroupNode
cons public init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean addBean(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean addNewBean()
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean[] getBeansFromModel()
meth protected org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader getModelReader()
meth protected org.netbeans.modules.xml.multiview.SectionNode createNode(org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding)
meth protected void removeBean(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth public java.lang.String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean)
meth public java.lang.String getBeanName(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth public java.lang.String getStandardBeanNameProperty()
meth public java.lang.String getSunBeanNameProperty()
meth public org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean createBean()
meth public void setBeanName(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean,java.lang.String)
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanGroupNode

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.ResourceEnvRefMetadataReader
cons public init(java.lang.String)
meth protected org.netbeans.modules.j2ee.dd.api.common.CommonDDBean normalizeParent(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean)
meth public java.util.Map<java.lang.String,java.lang.Object> genProperties(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean[])
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader
hfds parentName

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.ResourceEnvRefNode
cons public init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
meth protected org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel createNodeInnerPanel()
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanNode

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.ResourceRefGroupNode
cons public init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean addBean(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean addNewBean()
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean[] getBeansFromModel()
meth protected org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader getModelReader()
meth protected org.netbeans.modules.xml.multiview.SectionNode createNode(org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding)
meth protected void removeBean(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth public java.lang.String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean)
meth public java.lang.String getBeanName(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth public java.lang.String getStandardBeanNameProperty()
meth public java.lang.String getSunBeanNameProperty()
meth public org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean createBean()
meth public void setBeanName(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean,java.lang.String)
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanGroupNode

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.ResourceRefMetadataReader
cons public init(java.lang.String)
meth protected org.netbeans.modules.j2ee.dd.api.common.CommonDDBean normalizeParent(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean)
meth public java.util.Map<java.lang.String,java.lang.Object> genProperties(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean[])
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader
hfds parentName

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.ResourceRefNode
cons public init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
meth protected org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel createNodeInnerPanel()
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanNode

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.SecurityRoleMappingGroupNode
cons public init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,org.netbeans.modules.j2ee.sun.dd.api.RootInterface,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean addBean(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean addNewBean()
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean[] getBeansFromModel()
meth protected org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader getModelReader()
meth protected org.netbeans.modules.xml.multiview.SectionNode createNode(org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding)
meth protected void removeBean(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth public java.lang.String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean)
meth public java.lang.String getBeanName(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth public java.lang.String getStandardBeanNameProperty()
meth public java.lang.String getSunBeanNameProperty()
meth public org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean createBean()
meth public void setBeanName(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean,java.lang.String)
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanGroupNode

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.SecurityRoleMappingMultiViewElement
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject)
meth protected org.netbeans.modules.xml.multiview.ui.SectionView createView()
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDMultiViewElement
hfds serialVersionUID

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.SecurityRoleMappingNode
cons public init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
meth protected org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel createNodeInnerPanel()
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanNode

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.SecurityRoleMappingView
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject)
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDSectionNodeView
hfds securityRoleCache

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.SecurityRoleMetadataReader
cons public init()
meth protected org.netbeans.modules.j2ee.dd.api.common.CommonDDBean normalizeParent(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean)
meth public java.util.Map<java.lang.String,java.lang.Object> genProperties(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean[])
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.ServiceRefGroupNode
cons public init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean addBean(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean addNewBean()
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean[] getBeansFromModel()
meth protected org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader getModelReader()
meth protected org.netbeans.modules.xml.multiview.SectionNode createNode(org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding)
meth protected void removeBean(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth public java.lang.String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean)
meth public java.lang.String getBeanName(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth public java.lang.String getStandardBeanNameProperty()
meth public java.lang.String getSunBeanNameProperty()
meth public org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean createBean()
meth public void setBeanName(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean,java.lang.String)
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanGroupNode

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.ServiceRefMetadataReader
cons public init(java.lang.String)
meth protected org.netbeans.modules.j2ee.dd.api.common.CommonDDBean normalizeParent(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean)
meth public java.util.Map<java.lang.String,java.lang.Object> genProperties(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean[])
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader
hfds parentName

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.ServiceRefMultiViewElement
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject)
meth protected org.netbeans.modules.xml.multiview.ui.SectionView createView()
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDMultiViewElement
hfds serialVersionUID

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.ServiceRefNode
cons public init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
meth protected org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel createNodeInnerPanel()
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanNode

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.ServiceRefView
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject)
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDSectionNodeView
hfds serviceRefCache

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.ejb.EjbGroupNode
cons public init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
meth protected boolean isEventSource(java.lang.Object)
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean addBean(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean addNewBean()
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean[] getBeansFromModel()
meth protected org.netbeans.modules.xml.multiview.SectionNode createNode(org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding)
meth protected void removeBean(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth public java.lang.String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean)
meth public java.lang.String getBeanName(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth public java.lang.String getStandardBeanNameProperty()
meth public java.lang.String getSunBeanNameProperty()
meth public java.util.Map<java.lang.String,java.lang.Object> readAnnotations()
meth public java.util.Map<java.lang.String,java.lang.Object> readDescriptor()
meth public org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean createBean()
meth public void setBeanName(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean,java.lang.String)
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanGroupNode
hfds ejbFactory,sunEjbJar

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.ejb.EjbMetadataReader
cons public init()
intf org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction<org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata,java.util.Map<java.lang.String,java.lang.Object>>
meth public java.util.Map<java.lang.String,java.lang.Object> run(org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata) throws java.lang.Exception
meth public static java.util.Map<java.lang.String,java.lang.Object> readDescriptor(org.netbeans.modules.j2ee.dd.api.ejb.EjbJar)
supr java.lang.Object

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.ejb.EjbMultiViewElement
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject)
meth protected org.netbeans.modules.xml.multiview.ui.SectionView createView()
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDMultiViewElement
hfds serialVersionUID

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.ejb.EjbNode
cons public init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
meth protected org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel createNodeInnerPanel()
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanNode

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.ejb.EjbView
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject)
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.ejb.SunEjbJarBaseView
hfds ejbCache

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.ejb.ReferencesNode
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.BaseSectionNode

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.ejb.SunEjbJarBaseView
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject)
fld protected final org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar sunEjbJar
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDSectionNodeView

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.ejb.SunEjbJarDetailsNode
meth protected org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel createNodeInnerPanel()
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.BaseSectionNode

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.ejb.SunEjbJarDetailsPanel
cons public init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.BaseSectionNodeInnerPanel
hfds nameLabel,nameText,sunEjbJar
hcls NameEditorModel

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.ejb.SunEjbJarView
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject)
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.ejb.SunEjbJarBaseView

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.ejb.SunEjbOverviewMultiViewElement
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject)
meth protected org.netbeans.modules.xml.multiview.ui.SectionView createView()
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDMultiViewElement
hfds serialVersionUID

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.jms.JmsMappingView
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject)
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDSectionNodeView
hfds securityRoleCache

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.jms.JmsMultiViewElement
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject)
meth protected org.netbeans.modules.xml.multiview.ui.SectionView createView()
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDMultiViewElement
hfds serialVersionUID

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.jms.MessageDestinationGroupNode
cons public init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,org.netbeans.modules.j2ee.sun.dd.api.RootInterface,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
meth protected boolean isEventSource(java.lang.Object)
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean addBean(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean addNewBean()
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean[] getBeansFromModel()
meth protected org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader getModelReader()
meth protected org.netbeans.modules.xml.multiview.SectionNode createNode(org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding)
meth protected void removeBean(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth public java.lang.String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean)
meth public java.lang.String getBeanName(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth public java.lang.String getStandardBeanNameProperty()
meth public java.lang.String getSunBeanNameProperty()
meth public org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean createBean()
meth public void setBeanName(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean,java.lang.String)
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanGroupNode
hfds ejbJarMesgDestFactory,sunAppClient,sunEjbJar,sunWebApp

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.jms.MessageDestinationMetadataReader
cons public init()
meth protected org.netbeans.modules.j2ee.dd.api.common.CommonDDBean normalizeParent(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean)
meth public java.util.Map<java.lang.String,java.lang.Object> genProperties(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean[])
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.jms.MessageDestinationNode
cons public init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
meth protected org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel createNodeInnerPanel()
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanNode

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.jms.MessageDestinationRefGroupNode
cons public init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean addBean(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean addNewBean()
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean[] getBeansFromModel()
meth protected org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader getModelReader()
meth protected org.netbeans.modules.xml.multiview.SectionNode createNode(org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding)
meth protected void removeBean(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth public java.lang.String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean)
meth public java.lang.String getBeanName(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth public java.lang.String getStandardBeanNameProperty()
meth public java.lang.String getSunBeanNameProperty()
meth public org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean createBean()
meth public void setBeanName(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean,java.lang.String)
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanGroupNode

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.jms.MessageDestinationRefMetadataReader
cons public init(java.lang.String)
meth protected org.netbeans.modules.j2ee.dd.api.common.CommonDDBean normalizeParent(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean)
meth public java.util.Map<java.lang.String,java.lang.Object> genProperties(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean[])
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader
hfds parentName

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.jms.MessageDestinationRefNode
cons public init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
meth protected org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel createNodeInnerPanel()
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanNode

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.web.ServletGroupNode
cons public init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean addBean(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean addNewBean()
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean[] getBeansFromModel()
meth protected org.netbeans.modules.xml.multiview.SectionNode createNode(org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding)
meth protected void removeBean(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth public java.lang.String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean)
meth public java.lang.String getBeanName(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth public java.lang.String getStandardBeanNameProperty()
meth public java.lang.String getSunBeanNameProperty()
meth public java.util.Map<java.lang.String,java.lang.Object> readAnnotations()
meth public java.util.Map<java.lang.String,java.lang.Object> readDescriptor()
meth public org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean createBean()
meth public void setBeanName(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean,java.lang.String)
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanGroupNode
hfds sunWebApp

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.web.ServletMetadataReader
cons public init()
intf org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction<org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata,java.util.Map<java.lang.String,java.lang.Object>>
meth public java.util.Map<java.lang.String,java.lang.Object> run(org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata) throws java.lang.Exception
meth public static java.util.Map<java.lang.String,java.lang.Object> readDescriptor(org.netbeans.modules.j2ee.dd.api.web.WebApp)
supr java.lang.Object

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.web.ServletMultiViewElement
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject)
meth protected org.netbeans.modules.xml.multiview.ui.SectionView createView()
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDMultiViewElement
hfds serialVersionUID

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.web.ServletNode
cons public init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
meth protected org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel createNodeInnerPanel()
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanNode

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.web.ServletView
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject)
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.web.SunWebBaseView
hfds servletCache

CLSS public abstract org.netbeans.modules.j2ee.sun.ddloaders.multiview.web.SunWebBaseView
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject)
fld protected final org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp sunWebApp
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDSectionNodeView

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.web.SunWebClassLoaderNode
meth protected org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel createNodeInnerPanel()
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.BaseSectionNode

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.web.SunWebClassLoaderPanel
cons public init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.BaseSectionNodeInnerPanel
hfds jChkClassLoader,jChkDelegate,jLblDelegate,jLblDynamicReloadInterval,jLblExtraClassPath,jTxtDynamicReloadInterval,jTxtExtraClassPath,sunWebApp,webappBundle
hcls ClassLoaderCheckboxHelper,DelegateCheckboxHelper,DynamicReloadIntervalEditorModel,ExtraClasspathEditorModel

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.web.SunWebDetailsNode
meth protected org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel createNodeInnerPanel()
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.BaseSectionNode

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.web.SunWebDetailsPanel
cons public init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.BaseSectionNodeInnerPanel
hfds jLblContextRoot,jLblErrorUrl,jLblHttpservletSecurityProvider,jTxtContextRoot,jTxtErrorUrl,jTxtHttpservletSecurityProvider,sunWebApp
hcls ContextRootEditorModel,ErrorUrlEditorModel,HttpServletSecurityEditorModel

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.web.SunWebJspConfigPropertyNode
cons public init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
meth protected org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel createNodeInnerPanel()
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.BaseSectionNode
hcls JspConfigPropertyFactory

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.web.SunWebOverviewMultiViewElement
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject)
meth protected org.netbeans.modules.xml.multiview.ui.SectionView createView()
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDMultiViewElement
hfds serialVersionUID

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.web.SunWebView
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject)
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.web.SunWebBaseView

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.webservice.EndpointGroupNode
cons public init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean addBean(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean addNewBean()
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean[] getBeansFromModel()
meth protected org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader getModelReader()
meth protected org.netbeans.modules.xml.multiview.SectionNode createNode(org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding)
meth protected void removeBean(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth public java.lang.String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean)
meth public java.lang.String getBeanName(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth public java.lang.String getStandardBeanNameProperty()
meth public java.lang.String getSunBeanNameProperty()
meth public java.util.Map<java.lang.String,java.lang.Object> readDescriptor()
meth public org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean createBean()
meth public void setBeanName(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean,java.lang.String)
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanGroupNode

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.webservice.EndpointNode
cons public init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
meth protected org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel createNodeInnerPanel()
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanNode

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.webservice.PortComponentMetadataReader
cons public init(java.lang.String)
meth protected org.netbeans.modules.j2ee.dd.api.common.CommonDDBean normalizeParent(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean)
meth public java.util.Map<java.lang.String,java.lang.Object> genProperties(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean[])
meth public java.util.Map<java.lang.String,java.lang.Object> readAnnotations(org.openide.loaders.DataObject)
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader
hfds parentName

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.webservice.WebServiceGroupNode
cons public init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
meth protected boolean isEventSource(java.lang.Object)
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean addBean(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean addNewBean()
meth protected org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean[] getBeansFromModel()
meth protected org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader getModelReader()
meth protected org.netbeans.modules.xml.multiview.SectionNode createNode(org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding)
meth protected void removeBean(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth public java.lang.String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean)
meth public java.lang.String getBeanName(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean)
meth public java.lang.String getStandardBeanNameProperty()
meth public java.lang.String getSunBeanNameProperty()
meth public java.util.Map<java.lang.String,java.lang.Object> readDescriptor()
meth public org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean createBean()
meth public void setBeanName(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean,java.lang.String)
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanGroupNode
hfds ejbWebserviceDescFactory,sunEjbJar,sunWebApp

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.webservice.WebServiceMetadataReader
cons public init()
meth public java.util.Map<java.lang.String,java.lang.Object> genProperties(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean[])
meth public java.util.Map<java.lang.String,java.lang.Object> readAnnotations(org.openide.loaders.DataObject)
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.webservice.WebServiceMultiViewElement
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject)
meth protected org.netbeans.modules.xml.multiview.ui.SectionView createView()
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDMultiViewElement
hfds serialVersionUID

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.webservice.WebServiceNode
cons public init(org.netbeans.modules.xml.multiview.ui.SectionNodeView,org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
meth protected org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel createNodeInnerPanel()
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanNode

CLSS public org.netbeans.modules.j2ee.sun.ddloaders.multiview.webservice.WebServiceView
cons public init(org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject)
supr org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDSectionNodeView
hfds webServiceCache

CLSS public org.netbeans.modules.j2ee.sun.share.CharsetMapping
cons public init(java.nio.charset.Charset)
cons public init(java.nio.charset.Charset,boolean)
cons public init(java.nio.charset.Charset,java.lang.String)
fld public final static java.lang.Integer CHARSET_ALIAS_ASIDE
fld public final static java.lang.Integer CHARSET_ALIAS_SELECTION
fld public final static java.lang.Integer CHARSET_CANONICAL
fld public final static java.lang.String CHARSET_DISPLAY_TYPE = "CharsetDisplayType"
intf java.lang.Comparable
meth public boolean equals(java.lang.Object)
meth public int compareTo(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getAlias()
meth public java.lang.String toString()
meth public java.nio.charset.Charset getCharset()
meth public static java.lang.Integer getDisplayOption()
meth public static java.util.SortedMap getSortedAvailableCharsetMappings()
meth public static org.netbeans.modules.j2ee.sun.share.CharsetMapping getCharsetMapping(java.lang.String)
meth public static org.netbeans.modules.j2ee.sun.share.CharsetMapping getCharsetMapping(java.nio.charset.Charset)
meth public static void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public static void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public static void setDisplayOption(java.lang.Integer)
meth public void updateDisplayText()
supr java.lang.Object
hfds bundle,charset,chosenAlias,displayOption,displayText,propSupport,showAliases,sortedAliasCharsetMappings,sortedCanonicalCharsetMappings,textOutOfDate,useAliases

CLSS public abstract interface org.netbeans.modules.j2ee.sun.share.Constants
fld public final static java.lang.String EMPTY_STRING = ""
fld public final static java.lang.String USER_DATA_CHANGED = "UserDataChanged"
fld public final static java.util.logging.Logger jsr88Logger

CLSS public final org.netbeans.modules.j2ee.sun.share.PrincipalNameMapping
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getClassName()
meth public java.lang.String getPrincipalName()
meth public java.lang.String toString()
supr java.lang.Object
hfds className,principalName

CLSS public org.netbeans.modules.j2ee.sun.share.SecurityMasterListModel
fld public final static java.lang.String DUPLICATE_GROUP
fld public final static java.lang.String DUPLICATE_PRINCIPAL
fld public final static java.lang.String[] GROUP_COLUMN_NAMES
fld public final static java.lang.String[] PRINCIPAL_COLUMN_NAMES
meth public boolean contains(java.lang.Object)
meth public boolean removeElement(java.lang.Object)
meth public boolean replaceElement(java.lang.Object,java.lang.Object)
meth public int getColumnCount()
meth public int getRowCount()
meth public java.lang.Object getRow(int)
meth public java.lang.Object getValueAt(int,int)
meth public java.lang.String getColumnName(int)
meth public java.lang.String getDuplicateErrorMessage(java.lang.String)
meth public static org.netbeans.modules.j2ee.sun.share.SecurityMasterListModel getGroupMasterModel()
meth public static org.netbeans.modules.j2ee.sun.share.SecurityMasterListModel getPrincipalMasterModel()
meth public void addElement(java.lang.Object)
meth public void removeElementAt(int)
meth public void removeElements(int[])
supr javax.swing.table.AbstractTableModel
hfds columnCount,columnNames,duplicateErrorPattern,groupMaster,masterList,principalMaster

CLSS public org.netbeans.modules.j2ee.sun.share.configbean.CmpListenerSupport
cons public init()
supr java.lang.Object
hcls CmpEntityVisitor,CmpFieldNameVisitor,CmpFieldVisitor,CmpNameVisitorFactory,EntityVisitor

CLSS public org.netbeans.modules.j2ee.sun.share.configbean.SunONEDeploymentConfiguration
cons public init(javax.enterprise.deploy.model.DeployableObject)
 anno 0 java.lang.Deprecated()
cons public init(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
 anno 0 java.lang.Deprecated()
cons public init(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule,java.lang.String,org.netbeans.modules.glassfish.tooling.data.GlassFishVersion) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
cons public init(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule,org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
 anno 0 java.lang.Deprecated()
cons public init(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule,org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface,org.netbeans.modules.glassfish.tooling.data.GlassFishVersion) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
intf javax.enterprise.deploy.spi.DeploymentConfiguration
meth public boolean supportsCreateDatasource()
meth public boolean supportsCreateMessageDestination()
meth public java.lang.String getDeploymentModuleName()
meth public java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.Datasource> getDatasources()
meth public java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.MessageDestination> getMessageDestinations() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public javax.enterprise.deploy.model.DeployableObject getDeployableObject()
meth public javax.enterprise.deploy.spi.DConfigBeanRoot getDConfigBeanRoot(javax.enterprise.deploy.model.DDBeanRoot) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException
meth public javax.enterprise.deploy.spi.DConfigBeanRoot restoreDConfigBean(java.io.InputStream,javax.enterprise.deploy.model.DDBeanRoot) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException
meth public org.netbeans.modules.j2ee.deployment.common.api.Datasource createDatasource(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException,org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException
meth public org.netbeans.modules.j2ee.deployment.common.api.MessageDestination createMessageDestination(java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public void removeDConfigBean(javax.enterprise.deploy.spi.DConfigBeanRoot) throws javax.enterprise.deploy.spi.exceptions.BeanNotFoundException
meth public void restore(java.io.InputStream) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException
meth public void save(java.io.OutputStream) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException
meth public void saveDConfigBean(java.io.OutputStream,javax.enterprise.deploy.spi.DConfigBeanRoot) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException
meth public void setCMPResource(java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public void setDeploymentModuleName(java.lang.String)
meth public void setMappingInfo(org.netbeans.modules.j2ee.deployment.common.api.OriginalCMPMapping[]) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
supr org.netbeans.modules.glassfish.eecommon.api.config.GlassfishConfiguration
hfds deploymentModuleName,resourceProcessor,sdmi

CLSS public org.netbeans.modules.j2ee.sun.share.configbean.Utils
intf org.netbeans.modules.j2ee.sun.share.Constants
meth public static boolean booleanValueOf(java.lang.String)
meth public static boolean containsWhitespace(java.lang.String)
meth public static boolean hasTrailingSlash(java.lang.String)
meth public static boolean interpretCheckboxState(java.awt.event.ItemEvent)
meth public static boolean isJavaClass(java.lang.String)
meth public static boolean isJavaIdentifier(java.lang.String)
meth public static boolean isJavaPackage(java.lang.String)
meth public static boolean notEmpty(java.lang.String)
meth public static boolean strEmpty(java.lang.String)
meth public static boolean strEquals(java.lang.String,java.lang.String)
meth public static boolean strEquivalent(java.lang.String,java.lang.String)
meth public static int strCompareTo(java.lang.String,java.lang.String)
meth public static java.lang.String encodeUrlField(java.lang.String)
meth public static java.net.URL getResourceURL(java.lang.String,java.lang.Class)
meth public static void invokeHelp(java.lang.String)
meth public static void invokeHelp(org.openide.util.HelpCtx)
supr java.lang.Object
hfds booleanStrings

CLSS public org.netbeans.modules.j2ee.sun.share.plan.DeploymentPlan
cons public init() throws org.netbeans.modules.schema2beans.Schema2BeansException
cons public init(int)
cons public init(org.w3c.dom.Node,int) throws org.netbeans.modules.schema2beans.Schema2BeansException
fld public final static java.lang.String FILE_ENTRY = "FileEntry"
meth protected void initFromNode(org.w3c.dom.Node,int) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth protected void initOptions(int)
meth public int addFileEntry(org.netbeans.modules.j2ee.sun.share.plan.FileEntry) throws java.beans.PropertyVetoException
meth public int removeFileEntry(org.netbeans.modules.j2ee.sun.share.plan.FileEntry) throws java.beans.PropertyVetoException
meth public int sizeFileEntry()
meth public java.lang.String _getSchemaLocation()
meth public java.lang.String dumpBeanNode()
meth public org.netbeans.modules.j2ee.sun.share.plan.FileEntry getFileEntry(int)
meth public org.netbeans.modules.j2ee.sun.share.plan.FileEntry newFileEntry()
meth public org.netbeans.modules.j2ee.sun.share.plan.FileEntry[] getFileEntry()
meth public static org.netbeans.modules.j2ee.sun.share.plan.DeploymentPlan createGraph()
meth public static org.netbeans.modules.j2ee.sun.share.plan.DeploymentPlan createGraph(java.io.InputStream) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public static org.netbeans.modules.j2ee.sun.share.plan.DeploymentPlan createGraph(java.io.InputStream,boolean) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public static org.netbeans.modules.j2ee.sun.share.plan.DeploymentPlan createGraph(org.w3c.dom.Node) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void _setSchemaLocation(java.lang.String)
meth public void addVetoableChangeListener(java.lang.String,java.beans.VetoableChangeListener)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void removeVetoableChangeListener(java.lang.String,java.beans.VetoableChangeListener)
meth public void setFileEntry(int,org.netbeans.modules.j2ee.sun.share.plan.FileEntry)
meth public void setFileEntry(org.netbeans.modules.j2ee.sun.share.plan.FileEntry[])
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.schema2beans.BaseBean
hfds comparators,runtimeVersion

CLSS public org.netbeans.modules.j2ee.sun.share.plan.FileEntry
cons public init()
cons public init(int)
fld public final static java.lang.String CONTENT = "Content"
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String URI = "Uri"
meth public java.lang.String dumpBeanNode()
meth public java.lang.String getContent()
meth public java.lang.String getName()
meth public java.lang.String getUri()
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void addVetoableChangeListener(java.lang.String,java.beans.VetoableChangeListener)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void removeVetoableChangeListener(java.lang.String,java.beans.VetoableChangeListener)
meth public void setContent(java.lang.String) throws java.beans.PropertyVetoException
meth public void setName(java.lang.String) throws java.beans.PropertyVetoException
meth public void setUri(java.lang.String) throws java.beans.PropertyVetoException
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.schema2beans.BaseBean
hfds comparators,runtimeVersion

CLSS public org.netbeans.modules.j2ee.sun.share.plan.Util
meth public static void convert(java.io.InputStream,java.util.jar.JarOutputStream) throws java.io.IOException
supr java.lang.Object

CLSS public abstract org.netbeans.modules.schema2beans.BaseBean
cons public init(java.util.Vector,org.netbeans.modules.schema2beans.Version)
fld protected org.netbeans.modules.schema2beans.DOMBinding binding
fld protected org.netbeans.modules.schema2beans.GraphManager graphManager
fld public final static int MERGE_COMPARE = 4
fld public final static int MERGE_INTERSECT = 1
fld public final static int MERGE_NONE = 0
fld public final static int MERGE_UNION = 2
fld public final static int MERGE_UPDATE = 3
innr public IterateChoiceProperties
intf java.lang.Cloneable
intf org.netbeans.modules.schema2beans.Bean
meth protected boolean hasDomNode()
meth protected int addValue(org.netbeans.modules.schema2beans.BeanProp,java.lang.Object)
meth protected int removeValue(org.netbeans.modules.schema2beans.BeanProp,java.lang.Object)
meth protected java.util.Iterator beanPropsIterator()
meth protected org.netbeans.modules.schema2beans.DOMBinding domBinding()
meth protected void addKnownValue(java.lang.String,java.lang.Object)
meth protected void buildPathName(java.lang.StringBuffer)
meth protected void copyProperty(org.netbeans.modules.schema2beans.BeanProp,org.netbeans.modules.schema2beans.BaseBean,int,java.lang.Object)
meth protected void init(java.util.Vector,org.netbeans.modules.schema2beans.Version)
meth protected void initPropertyTables(int)
meth protected void removeValue(org.netbeans.modules.schema2beans.BeanProp,int)
meth protected void setDomBinding(org.netbeans.modules.schema2beans.DOMBinding)
meth protected void setGraphManager(org.netbeans.modules.schema2beans.GraphManager)
meth protected void setValue(org.netbeans.modules.schema2beans.BeanProp,int,java.lang.Object)
meth public abstract void dump(java.lang.StringBuffer,java.lang.String)
meth public boolean hasName(java.lang.String)
meth public boolean isChoiceProperty()
meth public boolean isChoiceProperty(java.lang.String)
meth public boolean isEqualTo(java.lang.Object)
meth public boolean isNull(java.lang.String)
meth public boolean isNull(java.lang.String,int)
meth public boolean isRoot()
meth public int addValue(java.lang.String,java.lang.Object)
meth public int idToIndex(java.lang.String,int)
meth public int indexOf(java.lang.String,java.lang.Object)
meth public int indexToId(java.lang.String,int)
meth public int removeValue(java.lang.String,java.lang.Object)
meth public int size(java.lang.String)
meth public java.lang.Object clone()
meth public java.lang.Object getValue(java.lang.String)
meth public java.lang.Object getValue(java.lang.String,int)
meth public java.lang.Object getValueById(java.lang.String,int)
meth public java.lang.Object[] getValues(java.lang.String)
meth public java.lang.Object[] knownValues(java.lang.String)
meth public java.lang.String _getXPathExpr()
meth public java.lang.String _getXPathExpr(java.lang.Object)
meth public java.lang.String dtdName()
meth public java.lang.String dumpBeanNode()
meth public java.lang.String dumpDomNode()
meth public java.lang.String dumpDomNode(int)
meth public java.lang.String dumpDomNode(java.lang.String,int)
meth public java.lang.String fullName()
meth public java.lang.String getAttributeValue(java.lang.String)
meth public java.lang.String getAttributeValue(java.lang.String,int,java.lang.String)
meth public java.lang.String getAttributeValue(java.lang.String,java.lang.String)
meth public java.lang.String getDefaultNamespace()
meth public java.lang.String name()
meth public java.lang.String nameChild(java.lang.Object)
meth public java.lang.String nameChild(java.lang.Object,boolean,boolean)
meth public java.lang.String nameChild(java.lang.Object,boolean,boolean,boolean)
meth public java.lang.String nameSelf()
meth public java.lang.String toString()
meth public java.lang.String[] findAttributeValue(java.lang.String,java.lang.String)
meth public java.lang.String[] findPropertyValue(java.lang.String,java.lang.Object)
meth public java.lang.String[] findValue(java.lang.Object)
meth public java.lang.String[] getAttributeNames()
meth public java.lang.String[] getAttributeNames(java.lang.String)
meth public java.util.Iterator listChoiceProperties()
meth public org.netbeans.modules.schema2beans.BaseAttribute[] listAttributes()
meth public org.netbeans.modules.schema2beans.BaseAttribute[] listAttributes(java.lang.String)
meth public org.netbeans.modules.schema2beans.BaseBean newInstance(java.lang.String)
meth public org.netbeans.modules.schema2beans.BaseBean parent()
meth public org.netbeans.modules.schema2beans.BaseBean[] childBeans(boolean)
meth public org.netbeans.modules.schema2beans.BaseProperty getProperty()
meth public org.netbeans.modules.schema2beans.BaseProperty getProperty(java.lang.String)
meth public org.netbeans.modules.schema2beans.BaseProperty[] listChoiceProperties(java.lang.String)
meth public org.netbeans.modules.schema2beans.BaseProperty[] listProperties()
meth public org.netbeans.modules.schema2beans.Bean _getParent()
meth public org.netbeans.modules.schema2beans.Bean _getRoot()
meth public org.netbeans.modules.schema2beans.Bean propertyById(java.lang.String,int)
meth public org.netbeans.modules.schema2beans.BeanProp beanProp()
meth public org.netbeans.modules.schema2beans.BeanProp beanProp(int)
meth public org.netbeans.modules.schema2beans.BeanProp beanProp(java.lang.String)
meth public org.netbeans.modules.schema2beans.BeanProp[] beanProps()
meth public org.netbeans.modules.schema2beans.GraphManager graphManager()
meth public org.w3c.dom.Comment addComment(java.lang.String)
meth public org.w3c.dom.Comment[] comments()
meth public static org.netbeans.modules.schema2beans.BaseBean createGraph(java.lang.Class,java.io.InputStream) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public static org.netbeans.modules.schema2beans.BaseBean createGraph(java.lang.Class,java.io.InputStream,boolean) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public static org.netbeans.modules.schema2beans.BaseBean createGraph(java.lang.Class,java.io.InputStream,boolean,org.xml.sax.EntityResolver) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public static org.netbeans.modules.schema2beans.BaseBean createGraph(java.lang.Class,java.io.InputStream,boolean,org.xml.sax.EntityResolver,org.xml.sax.ErrorHandler) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public void _setChanged(boolean)
meth public void addBeanComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void changeDocType(java.lang.String,java.lang.String)
meth public void childBeans(boolean,java.util.List)
meth public void createAttribute(java.lang.String,java.lang.String,int,java.lang.String[],java.lang.String)
meth public void createAttribute(java.lang.String,java.lang.String,java.lang.String,int,java.lang.String[],java.lang.String)
meth public void createBean(org.w3c.dom.Node,org.netbeans.modules.schema2beans.GraphManager)
meth public void createProperty(java.lang.String,java.lang.String,int,java.lang.Class)
meth public void createProperty(java.lang.String,java.lang.String,java.lang.Class)
meth public void createRoot(java.lang.String,java.lang.String,int,java.lang.Class)
meth public void dumpAttributes(java.lang.String,int,java.lang.StringBuffer,java.lang.String)
meth public void dumpXml()
meth public void merge(org.netbeans.modules.schema2beans.BaseBean)
meth public void merge(org.netbeans.modules.schema2beans.BaseBean,int)
meth public void mergeUpdate(org.netbeans.modules.schema2beans.BaseBean)
meth public void reindent()
meth public void reindent(java.lang.String)
meth public void removeBeanComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void removeComment(org.w3c.dom.Comment)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void removeValue(java.lang.String,int)
meth public void setAttributeValue(java.lang.String,int,java.lang.String,java.lang.String)
meth public void setAttributeValue(java.lang.String,java.lang.String)
meth public void setAttributeValue(java.lang.String,java.lang.String,java.lang.String)
meth public void setDefaultNamespace(java.lang.String)
meth public void setValue(java.lang.String,int,java.lang.Object)
meth public void setValue(java.lang.String,java.lang.Object)
meth public void setValue(java.lang.String,java.lang.Object[])
meth public void setValueById(java.lang.String,int,java.lang.Object)
meth public void write(java.io.File) throws java.io.IOException
meth public void write(java.io.OutputStream) throws java.io.IOException
meth public void write(java.io.OutputStream,java.lang.String) throws java.io.IOException,org.netbeans.modules.schema2beans.Schema2BeansException
meth public void write(java.io.Writer) throws java.io.IOException,org.netbeans.modules.schema2beans.Schema2BeansException
meth public void write(java.io.Writer,java.lang.String) throws java.io.IOException,org.netbeans.modules.schema2beans.Schema2BeansException
meth public void writeNoReindent(java.io.OutputStream) throws java.io.IOException,org.netbeans.modules.schema2beans.Schema2BeansException
meth public void writeNode(java.io.Writer) throws java.io.IOException
supr java.lang.Object
hfds attrCache,changeListeners,comparators,defaultNamespace,isRoot,propByName,propByOrder,propertyOrder

CLSS public abstract interface org.netbeans.modules.schema2beans.Bean
meth public abstract boolean hasName(java.lang.String)
meth public abstract boolean isRoot()
meth public abstract int addValue(java.lang.String,java.lang.Object)
meth public abstract int idToIndex(java.lang.String,int)
meth public abstract int indexToId(java.lang.String,int)
meth public abstract int removeValue(java.lang.String,java.lang.Object)
meth public abstract java.lang.Object getValue(java.lang.String)
meth public abstract java.lang.Object getValue(java.lang.String,int)
meth public abstract java.lang.Object getValueById(java.lang.String,int)
meth public abstract java.lang.Object[] getValues(java.lang.String)
meth public abstract java.lang.String dtdName()
meth public abstract java.lang.String name()
meth public abstract org.netbeans.modules.schema2beans.BaseProperty getProperty()
meth public abstract org.netbeans.modules.schema2beans.BaseProperty getProperty(java.lang.String)
meth public abstract org.netbeans.modules.schema2beans.BaseProperty[] listProperties()
meth public abstract org.netbeans.modules.schema2beans.Bean _getParent()
meth public abstract org.netbeans.modules.schema2beans.Bean _getRoot()
meth public abstract org.netbeans.modules.schema2beans.Bean propertyById(java.lang.String,int)
meth public abstract org.netbeans.modules.schema2beans.BeanProp beanProp(java.lang.String)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void childBeans(boolean,java.util.List)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setValue(java.lang.String,int,java.lang.Object)
meth public abstract void setValue(java.lang.String,java.lang.Object)
meth public abstract void setValueById(java.lang.String,int,java.lang.Object)

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

CLSS public abstract interface org.netbeans.modules.xml.multiview.cookies.ErrorLocator
meth public abstract javax.swing.JComponent getErrorComponent(java.lang.String)

CLSS public abstract interface org.netbeans.modules.xml.multiview.cookies.LinkCookie
meth public abstract void linkButtonPressed(java.lang.Object,java.lang.String)

CLSS public abstract interface org.netbeans.modules.xml.multiview.cookies.SectionFocusCookie
meth public abstract boolean focusSection(org.netbeans.modules.xml.multiview.ui.NodeSectionPanel)

CLSS public abstract interface org.netbeans.modules.xml.multiview.ui.ContainerPanel
meth public abstract org.netbeans.modules.xml.multiview.ui.NodeSectionPanel getSection(org.openide.nodes.Node)
meth public abstract org.openide.nodes.Node getRoot()
meth public abstract void addSection(org.netbeans.modules.xml.multiview.ui.NodeSectionPanel)
meth public abstract void removeSection(org.netbeans.modules.xml.multiview.ui.NodeSectionPanel)

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

CLSS public static org.openide.nodes.Children$Map<%0 extends java.lang.Object>
 outer org.openide.nodes.Children
cons protected init(java.util.Map<{org.openide.nodes.Children$Map%0},org.openide.nodes.Node>)
cons public init()
fld protected java.util.Map<{org.openide.nodes.Children$Map%0},org.openide.nodes.Node> nodes
meth protected final void put({org.openide.nodes.Children$Map%0},org.openide.nodes.Node)
meth protected final void putAll(java.util.Map<? extends {org.openide.nodes.Children$Map%0},? extends org.openide.nodes.Node>)
meth protected final void refresh()
meth protected final void refreshKey({org.openide.nodes.Children$Map%0})
meth protected final void removeAll(java.util.Collection<? extends {org.openide.nodes.Children$Map%0}>)
meth protected java.util.Map<{org.openide.nodes.Children$Map%0},org.openide.nodes.Node> initMap()
meth protected void remove({org.openide.nodes.Children$Map%0})
meth public boolean add(org.openide.nodes.Node[])
meth public boolean remove(org.openide.nodes.Node[])
supr org.openide.nodes.Children
hcls ME

CLSS public static org.openide.nodes.Children$SortedMap<%0 extends java.lang.Object>
 outer org.openide.nodes.Children
cons protected init(java.util.Map<{org.openide.nodes.Children$SortedMap%0},org.openide.nodes.Node>)
cons public init()
meth public java.util.Comparator<? super org.openide.nodes.Node> getComparator()
meth public void setComparator(java.util.Comparator<? super org.openide.nodes.Node>)
supr org.openide.nodes.Children$Map<{org.openide.nodes.Children$SortedMap%0}>
hfds comp
hcls SMComparator

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

