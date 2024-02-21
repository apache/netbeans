#Signature file v4.1
#Version 1.83

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

CLSS public abstract interface java.awt.event.ItemListener
intf java.util.EventListener
meth public abstract void itemStateChanged(java.awt.event.ItemEvent)

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

CLSS public abstract interface java.beans.PropertyChangeListener
intf java.util.EventListener
meth public abstract void propertyChange(java.beans.PropertyChangeEvent)

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation

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

CLSS public abstract interface java.util.prefs.NodeChangeListener
intf java.util.EventListener
meth public abstract void childAdded(java.util.prefs.NodeChangeEvent)
meth public abstract void childRemoved(java.util.prefs.NodeChangeEvent)

CLSS public abstract interface java.util.prefs.PreferenceChangeListener
 anno 0 java.lang.FunctionalInterface()
intf java.util.EventListener
meth public abstract void preferenceChange(java.util.prefs.PreferenceChangeEvent)

CLSS public abstract java.util.prefs.Preferences
cons protected init()
fld public final static int MAX_KEY_LENGTH = 80
fld public final static int MAX_NAME_LENGTH = 80
fld public final static int MAX_VALUE_LENGTH = 8192
meth public abstract boolean getBoolean(java.lang.String,boolean)
meth public abstract boolean isUserNode()
meth public abstract boolean nodeExists(java.lang.String) throws java.util.prefs.BackingStoreException
meth public abstract byte[] getByteArray(java.lang.String,byte[])
meth public abstract double getDouble(java.lang.String,double)
meth public abstract float getFloat(java.lang.String,float)
meth public abstract int getInt(java.lang.String,int)
meth public abstract java.lang.String absolutePath()
meth public abstract java.lang.String get(java.lang.String,java.lang.String)
meth public abstract java.lang.String name()
meth public abstract java.lang.String toString()
meth public abstract java.lang.String[] childrenNames() throws java.util.prefs.BackingStoreException
meth public abstract java.lang.String[] keys() throws java.util.prefs.BackingStoreException
meth public abstract java.util.prefs.Preferences node(java.lang.String)
meth public abstract java.util.prefs.Preferences parent()
meth public abstract long getLong(java.lang.String,long)
meth public abstract void addNodeChangeListener(java.util.prefs.NodeChangeListener)
meth public abstract void addPreferenceChangeListener(java.util.prefs.PreferenceChangeListener)
meth public abstract void clear() throws java.util.prefs.BackingStoreException
meth public abstract void exportNode(java.io.OutputStream) throws java.io.IOException,java.util.prefs.BackingStoreException
meth public abstract void exportSubtree(java.io.OutputStream) throws java.io.IOException,java.util.prefs.BackingStoreException
meth public abstract void flush() throws java.util.prefs.BackingStoreException
meth public abstract void put(java.lang.String,java.lang.String)
meth public abstract void putBoolean(java.lang.String,boolean)
meth public abstract void putByteArray(java.lang.String,byte[])
meth public abstract void putDouble(java.lang.String,double)
meth public abstract void putFloat(java.lang.String,float)
meth public abstract void putInt(java.lang.String,int)
meth public abstract void putLong(java.lang.String,long)
meth public abstract void remove(java.lang.String)
meth public abstract void removeNode() throws java.util.prefs.BackingStoreException
meth public abstract void removeNodeChangeListener(java.util.prefs.NodeChangeListener)
meth public abstract void removePreferenceChangeListener(java.util.prefs.PreferenceChangeListener)
meth public abstract void sync() throws java.util.prefs.BackingStoreException
meth public static java.util.prefs.Preferences systemNodeForPackage(java.lang.Class<?>)
meth public static java.util.prefs.Preferences systemRoot()
meth public static java.util.prefs.Preferences userNodeForPackage(java.lang.Class<?>)
meth public static java.util.prefs.Preferences userRoot()
meth public static void importPreferences(java.io.InputStream) throws java.io.IOException,java.util.prefs.InvalidPreferencesFormatException
supr java.lang.Object

CLSS public abstract interface javax.accessibility.Accessible
meth public abstract javax.accessibility.AccessibleContext getAccessibleContext()

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

CLSS public abstract interface org.netbeans.api.lexer.TokenId
meth public abstract int ordinal()
meth public abstract java.lang.String name()
meth public abstract java.lang.String primaryCategory()

CLSS public abstract org.netbeans.modules.editor.settings.storage.spi.StorageFilter<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons protected init(java.lang.String)
meth protected final void notifyChanges()
meth public abstract void afterLoad(java.util.Map<{org.netbeans.modules.editor.settings.storage.spi.StorageFilter%0},{org.netbeans.modules.editor.settings.storage.spi.StorageFilter%1}>,org.netbeans.api.editor.mimelookup.MimePath,java.lang.String,boolean) throws java.io.IOException
meth public abstract void beforeSave(java.util.Map<{org.netbeans.modules.editor.settings.storage.spi.StorageFilter%0},{org.netbeans.modules.editor.settings.storage.spi.StorageFilter%1}>,org.netbeans.api.editor.mimelookup.MimePath,java.lang.String,boolean) throws java.io.IOException
supr java.lang.Object
hfds LOG,notificationCallback,storageDescriptionId

CLSS public org.netbeans.modules.options.colors.AllLanguageHierarchy
cons public init()
meth protected java.lang.String mimeType()
meth protected java.util.Collection<org.netbeans.modules.options.colors.AllLanguagesTokenId> createTokenIds()
meth protected org.netbeans.spi.lexer.Lexer<org.netbeans.modules.options.colors.AllLanguagesTokenId> createLexer(org.netbeans.spi.lexer.LexerRestartInfo<org.netbeans.modules.options.colors.AllLanguagesTokenId>)
supr org.netbeans.spi.lexer.LanguageHierarchy<org.netbeans.modules.options.colors.AllLanguagesTokenId>

CLSS public org.netbeans.modules.options.colors.AllLanguageProvider
cons public init()
meth public org.netbeans.api.lexer.Language<org.netbeans.modules.options.colors.AllLanguagesTokenId> findLanguage(java.lang.String)
meth public org.netbeans.spi.lexer.LanguageEmbedding<?> findLanguageEmbedding(org.netbeans.api.lexer.Token,org.netbeans.api.lexer.LanguagePath,org.netbeans.api.lexer.InputAttributes)
supr org.netbeans.spi.lexer.LanguageProvider

CLSS public final !enum org.netbeans.modules.options.colors.AllLanguagesTokenId
fld public final static org.netbeans.modules.options.colors.AllLanguagesTokenId CHARACTER
fld public final static org.netbeans.modules.options.colors.AllLanguagesTokenId COMMENT
fld public final static org.netbeans.modules.options.colors.AllLanguagesTokenId ERROR
fld public final static org.netbeans.modules.options.colors.AllLanguagesTokenId IDENTIFIER
fld public final static org.netbeans.modules.options.colors.AllLanguagesTokenId KEYWORD
fld public final static org.netbeans.modules.options.colors.AllLanguagesTokenId NUMBER
fld public final static org.netbeans.modules.options.colors.AllLanguagesTokenId OPERATOR
fld public final static org.netbeans.modules.options.colors.AllLanguagesTokenId SEPARATOR
fld public final static org.netbeans.modules.options.colors.AllLanguagesTokenId STRING
fld public final static org.netbeans.modules.options.colors.AllLanguagesTokenId WHITESPACE
intf org.netbeans.api.lexer.TokenId
meth public java.lang.String primaryCategory()
meth public static org.netbeans.modules.options.colors.AllLanguagesTokenId valueOf(java.lang.String)
meth public static org.netbeans.modules.options.colors.AllLanguagesTokenId[] values()
supr java.lang.Enum<org.netbeans.modules.options.colors.AllLanguagesTokenId>
hfds name

CLSS public org.netbeans.modules.options.colors.AnnotationsPanel
cons public init()
intf java.awt.event.ActionListener
intf java.awt.event.ItemListener
intf org.netbeans.modules.options.colors.spi.FontsColorsController
meth public boolean isChanged()
meth public javax.swing.JComponent getComponent()
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void applyChanges()
meth public void cancel()
meth public void deleteProfile(java.lang.String)
meth public void itemStateChanged(java.awt.event.ItemEvent)
meth public void setCurrentProfile(java.lang.String)
meth public void update(org.netbeans.modules.options.colors.ColorModel)
supr javax.swing.JPanel
hfds cbBackground,cbEffectColor,cbEffects,cbForeground,changed,colorModel,cpCategories,currentScheme,lCategories,lCategory,lEffectColor,lEffects,lForeground,lbackground,listen,profileToDefaults,schemes,toBeSaved

CLSS public final org.netbeans.modules.options.colors.CategoryComparator
cons public init()
intf java.util.Comparator<javax.swing.text.AttributeSet>
meth public int compare(javax.swing.text.AttributeSet,javax.swing.text.AttributeSet)
supr java.lang.Object
hfds default_string

CLSS public org.netbeans.modules.options.colors.ColorComboBoxSupport
cons public init()
supr java.lang.Object
hfds cbWithInheritedColor,content

CLSS public final org.netbeans.modules.options.colors.ColorModel
cons public init()
fld public final static java.lang.String ALL_LANGUAGES
meth public boolean isCustomProfile(java.lang.String)
meth public java.awt.Component getSyntaxColoringPreviewComponent(java.lang.String)
meth public java.lang.String getCurrentProfile()
meth public java.util.Collection<javax.swing.text.AttributeSet> getAnnotations(java.lang.String)
meth public java.util.Collection<javax.swing.text.AttributeSet> getAnnotationsDefaults(java.lang.String)
meth public java.util.Collection<javax.swing.text.AttributeSet> getCategories(java.lang.String,java.lang.String)
meth public java.util.Collection<javax.swing.text.AttributeSet> getDefaults(java.lang.String,java.lang.String)
meth public java.util.Collection<javax.swing.text.AttributeSet> getHighlightingDefaults(java.lang.String)
meth public java.util.Collection<javax.swing.text.AttributeSet> getHighlightings(java.lang.String)
meth public java.util.Set<java.lang.String> getLanguages()
meth public java.util.Set<java.lang.String> getProfiles()
meth public void setAnnotations(java.lang.String,java.util.Collection<javax.swing.text.AttributeSet>)
meth public void setCategories(java.lang.String,java.lang.String,java.util.Collection<javax.swing.text.AttributeSet>)
meth public void setCurrentProfile(java.lang.String)
meth public void setHighlightings(java.lang.String,java.util.Collection<javax.swing.text.AttributeSet>)
supr java.lang.Object
hfds EMPTY_MIMEPATH,LOG,languageToMimeType
hcls Preview

CLSS public org.netbeans.modules.options.colors.FontAndColorsPanel
cons public init(java.util.Collection<? extends org.netbeans.modules.options.colors.spi.FontsColorsController>)
intf java.awt.event.ActionListener
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void removeNotify()
supr javax.swing.JPanel
hfds bDelete,bDuplicate,cbProfile,colorModel,currentProfile,lProfile,listen,panels,tpCustomizers

CLSS public final org.netbeans.modules.options.colors.FontAndColorsPanelController
cons public init()
meth public boolean isChanged()
meth public boolean isValid()
meth public javax.swing.JComponent getComponent(org.openide.util.Lookup)
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void applyChanges()
meth public void cancel()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void update()
supr org.netbeans.spi.options.OptionsPanelController
hfds component,delegates,lookupListener,lookupResult

CLSS public org.netbeans.modules.options.colors.HighlightingPanel
cons public init()
intf java.awt.event.ActionListener
intf java.awt.event.ItemListener
meth public boolean isChanged()
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void applyChanges()
meth public void cancel()
meth public void deleteProfile(java.lang.String)
meth public void itemStateChanged(java.awt.event.ItemEvent)
meth public void setCurrentProfile(java.lang.String)
meth public void update(org.netbeans.modules.options.colors.ColorModel)
supr javax.swing.JPanel
hfds cbBackground,cbEffectColor,cbEffects,cbForeground,changed,colorModel,cpCategories,currentProfile,lBackground,lCategories,lCategory,lEffectColor,lEffects,lForeground,listen,profileToCategories,profileToDefaults,toBeSaved

CLSS public org.netbeans.modules.options.colors.HighlightingPanelController
cons public init()
intf org.netbeans.modules.options.colors.spi.FontsColorsController
meth public boolean isChanged()
meth public javax.swing.JComponent getComponent()
meth public void applyChanges()
meth public void cancel()
meth public void deleteProfile(java.lang.String)
meth public void setCurrentProfile(java.lang.String)
meth public void update(org.netbeans.modules.options.colors.ColorModel)
supr java.lang.Object
hfds component

CLSS public org.netbeans.modules.options.colors.SyntaxColoringPanel
cons public init()
intf java.awt.event.ActionListener
intf java.awt.event.ItemListener
intf java.beans.PropertyChangeListener
intf org.netbeans.modules.options.colors.spi.FontsColorsController
meth public boolean isChanged()
meth public javax.swing.JComponent getComponent()
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void applyChanges()
meth public void cancel()
meth public void deleteProfile(java.lang.String)
meth public void itemStateChanged(java.awt.event.ItemEvent)
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void setCurrentProfile(java.lang.String)
meth public void update(org.netbeans.modules.options.colors.ColorModel)
supr javax.swing.JPanel
hfds bFont,blink,blinkSequence,cbBackground,cbEffectColor,cbEffects,cbForeground,cbLanguage,changed,colorModel,convertALC,currentLanguage,currentProfile,defaults,lBackground,lCategories,lCategory,lEffectColor,lEffects,lFont,lForeground,lLanguage,lPreview,languageToMimeType,listen,log,pPreview,preview,profiles,selectTask,spCategories,spPreview,task,tfFont,toBeSaved,updatecbLanguage
hcls LanguagesComparator

CLSS public abstract interface org.netbeans.modules.options.colors.spi.FontsColorsController
meth public abstract boolean isChanged()
meth public abstract javax.swing.JComponent getComponent()
meth public abstract void applyChanges()
meth public abstract void cancel()
meth public abstract void deleteProfile(java.lang.String)
meth public abstract void setCurrentProfile(java.lang.String)
meth public abstract void update(org.netbeans.modules.options.colors.ColorModel)

CLSS public final org.netbeans.modules.options.editor.spi.OptionsFilter
innr public abstract interface static Acceptor
meth public static org.netbeans.modules.options.editor.spi.OptionsFilter create(javax.swing.text.Document,java.lang.Runnable)
meth public void installFilteringModel(javax.swing.JTree,javax.swing.tree.TreeModel,org.netbeans.modules.options.editor.spi.OptionsFilter$Acceptor)
meth public void installFilteringModel(javax.swing.JTree,javax.swing.tree.TreeModel,org.netbeans.modules.options.editor.spi.OptionsFilter$Acceptor,boolean)
supr java.lang.Object
hfds doc,usedCallback
hcls FilteringTreeModel

CLSS public abstract interface static org.netbeans.modules.options.editor.spi.OptionsFilter$Acceptor
 outer org.netbeans.modules.options.editor.spi.OptionsFilter
meth public abstract boolean accept(java.lang.Object,java.lang.String)

CLSS public abstract interface org.netbeans.modules.options.editor.spi.PreferencesCustomizer
fld public final static java.lang.String TABS_AND_INDENTS_ID = "tabs-and-indents"
innr public abstract interface static Factory
innr public static CustomCustomizer
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getId()
meth public abstract javax.swing.JComponent getComponent()
meth public abstract org.openide.util.HelpCtx getHelpCtx()

CLSS public static org.netbeans.modules.options.editor.spi.PreferencesCustomizer$CustomCustomizer
 outer org.netbeans.modules.options.editor.spi.PreferencesCustomizer
cons public init()
meth public java.lang.String getSavedValue(org.netbeans.modules.options.editor.spi.PreferencesCustomizer,java.lang.String)
supr java.lang.Object

CLSS public abstract interface static org.netbeans.modules.options.editor.spi.PreferencesCustomizer$Factory
 outer org.netbeans.modules.options.editor.spi.PreferencesCustomizer
meth public abstract org.netbeans.modules.options.editor.spi.PreferencesCustomizer create(java.util.prefs.Preferences)

CLSS public abstract interface org.netbeans.modules.options.editor.spi.PreviewProvider
meth public abstract javax.swing.JComponent getPreviewComponent()
meth public abstract void refreshPreview()

CLSS public final org.netbeans.modules.options.indentation.CustomizerSelector
cons public init(org.netbeans.modules.options.indentation.CustomizerSelector$PreferencesFactory,boolean,java.lang.String)
fld public final static java.lang.String FORMATTING_CUSTOMIZERS_FOLDER = "OptionsDialog/Editor/Formatting/"
fld public final static java.lang.String PROP_CUSTOMIZER = "CustomizerSelector.PROP_CUSTOMIZER"
fld public final static java.lang.String PROP_MIMETYPE = "CustomizerSelector.PROP_MIMETYPE"
innr public abstract interface static PreferencesFactory
meth public java.lang.String getSelectedMimeType()
meth public java.util.Collection<? extends java.lang.String> getMimeTypes()
meth public java.util.List<? extends org.netbeans.modules.options.editor.spi.PreferencesCustomizer> getCustomizers(java.lang.String)
meth public java.util.prefs.Preferences getCustomizerPreferences(org.netbeans.modules.options.editor.spi.PreferencesCustomizer)
meth public org.netbeans.modules.options.editor.spi.PreferencesCustomizer getSelectedCustomizer()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setSelectedCustomizer(java.lang.String)
meth public void setSelectedMimeType(java.lang.String)
supr java.lang.Object
hfds LOG,acceptOldControllers,allCustomizers,allowedMimeTypes,c2p,mimeTypes,pcs,pf,selectedCustomizerId,selectedMimeType
hcls WrapperCustomizer,WrapperCustomizerWithPreview

CLSS public abstract interface static org.netbeans.modules.options.indentation.CustomizerSelector$PreferencesFactory
 outer org.netbeans.modules.options.indentation.CustomizerSelector
meth public abstract boolean isKeyOverridenForMimeType(java.lang.String,java.lang.String)
meth public abstract java.util.prefs.Preferences getPreferences(java.lang.String)

CLSS public final org.netbeans.modules.options.indentation.FormattingPanel
cons public init()
intf java.beans.PropertyChangeListener
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void setSelector(org.netbeans.modules.options.indentation.CustomizerSelector)
supr javax.swing.JPanel
hfds categoryCombo,categoryLabel,categoryPanel,jSplitPane1,languageCombo,languageLabel,optionsPanel,previewLabel,previewPanel,previewScrollPane,selector,storedCategory,storedMimeType,weakListener

CLSS public final org.netbeans.modules.options.indentation.FormattingPanelController
cons public init()
fld public final static java.lang.String OVERRIDE_GLOBAL_FORMATTING_OPTIONS = "FormattingPanelController.OVERRIDE_GLOBAL_FORMATTING_OPTIONS"
meth public boolean isChanged()
meth public boolean isValid()
meth public javax.swing.JComponent getComponent(org.openide.util.Lookup)
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void applyChanges()
meth public void cancel()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void update()
supr org.netbeans.spi.options.OptionsPanelController
hfds BASIC_SETTINGS_NAMES,LOG,changed,panel,pcs,pf,selector
hcls MimeLookupPreferencesFactory

CLSS public final org.netbeans.modules.options.indentation.FormattingSettingsFromNbPreferences
cons public init()
meth public void afterLoad(java.util.Map<java.lang.String,org.netbeans.modules.editor.settings.storage.spi.TypedValue>,org.netbeans.api.editor.mimelookup.MimePath,java.lang.String,boolean) throws java.io.IOException
meth public void beforeSave(java.util.Map<java.lang.String,org.netbeans.modules.editor.settings.storage.spi.TypedValue>,org.netbeans.api.editor.mimelookup.MimePath,java.lang.String,boolean) throws java.io.IOException
supr org.netbeans.modules.editor.settings.storage.spi.StorageFilter<java.lang.String,org.netbeans.modules.editor.settings.storage.spi.TypedValue>
hfds LOG,affectedMimeTypes

CLSS public org.netbeans.modules.options.indentation.IndentationPanel
cons public init(org.netbeans.api.editor.mimelookup.MimePath,org.netbeans.modules.options.indentation.CustomizerSelector$PreferencesFactory,java.util.prefs.Preferences,java.util.prefs.Preferences,org.netbeans.modules.options.editor.spi.PreviewProvider)
innr public final static NoPreview
innr public final static TextPreview
intf java.awt.event.ActionListener
intf java.util.prefs.PreferenceChangeListener
intf javax.swing.event.ChangeListener
meth public org.netbeans.modules.options.editor.spi.PreviewProvider getPreviewProvider()
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void preferenceChange(java.util.prefs.PreferenceChangeEvent)
meth public void stateChanged(javax.swing.event.ChangeEvent)
supr javax.swing.JPanel
hfds LOG,REFRESH_DELAY,REFRESH_PROCESSOR,allLangPrefs,cbEnableIndentation,cbExpandTabsToSpaces,cbOverrideGlobalOptions,cboLineWrap,jPanel1,lLineWrap,lNumberOfSpacesPerIndent,lRightMargin,lTabSize,mimePath,prefs,prefsFactory,preview,refreshTask,sNumberOfSpacesPerIndent,sRightMargin,sTabSize,showOverrideGlobalOptions
hcls ControlledCheckBox,ControlledComboBox,ControlledLabel,ControlledSpinner,LineWrapRenderer

CLSS public final static org.netbeans.modules.options.indentation.IndentationPanel$NoPreview
 outer org.netbeans.modules.options.indentation.IndentationPanel
cons public init()
intf org.netbeans.modules.options.editor.spi.PreviewProvider
meth public javax.swing.JComponent getPreviewComponent()
meth public void refreshPreview()
supr java.lang.Object
hfds component

CLSS public final static org.netbeans.modules.options.indentation.IndentationPanel$TextPreview
 outer org.netbeans.modules.options.indentation.IndentationPanel
cons public init(java.util.prefs.Preferences,java.lang.String,java.lang.Class,java.lang.String)
cons public init(java.util.prefs.Preferences,java.lang.String,java.lang.ClassLoader,java.lang.String) throws java.io.IOException
cons public init(java.util.prefs.Preferences,java.lang.String,java.lang.String)
cons public init(java.util.prefs.Preferences,java.lang.String,org.openide.filesystems.FileObject) throws java.io.IOException
cons public init(java.util.prefs.Preferences,org.openide.filesystems.FileObject) throws java.io.IOException
intf org.netbeans.modules.options.editor.spi.PreviewProvider
meth public javax.swing.JComponent getPreviewComponent()
meth public void refreshPreview()
supr java.lang.Object
hfds jep,mimeType,prefs,previewText

CLSS public final org.netbeans.modules.options.indentation.IndentationPanelController
cons public init(java.util.prefs.Preferences)
cons public init(org.netbeans.api.editor.mimelookup.MimePath,org.netbeans.modules.options.indentation.CustomizerSelector$PreferencesFactory,java.util.prefs.Preferences,java.util.prefs.Preferences,org.netbeans.modules.options.editor.spi.PreferencesCustomizer)
intf org.netbeans.modules.options.editor.spi.PreferencesCustomizer
intf org.netbeans.modules.options.editor.spi.PreviewProvider
meth public java.lang.String getDisplayName()
meth public java.lang.String getId()
meth public javax.swing.JComponent getComponent()
meth public javax.swing.JComponent getPreviewComponent()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void refreshPreview()
supr java.lang.Object
hfds LOG,allLanguagesPreferences,delegate,indentationPanel,mimePath,preferences,prefsFactory

CLSS public final org.netbeans.modules.options.indentation.ProxyPreferences
 anno 0 java.lang.Deprecated()
intf java.util.prefs.NodeChangeListener
intf java.util.prefs.PreferenceChangeListener
meth public boolean getBoolean(java.lang.String,boolean)
meth public boolean isUserNode()
meth public boolean nodeExists(java.lang.String) throws java.util.prefs.BackingStoreException
meth public byte[] getByteArray(java.lang.String,byte[])
meth public double getDouble(java.lang.String,double)
meth public float getFloat(java.lang.String,float)
meth public int getInt(java.lang.String,int)
meth public java.lang.String absolutePath()
meth public java.lang.String get(java.lang.String,java.lang.String)
meth public java.lang.String name()
meth public java.lang.String toString()
meth public java.lang.String[] childrenNames() throws java.util.prefs.BackingStoreException
meth public java.lang.String[] keys() throws java.util.prefs.BackingStoreException
meth public java.util.prefs.Preferences node(java.lang.String)
meth public java.util.prefs.Preferences parent()
meth public long getLong(java.lang.String,long)
meth public static org.netbeans.modules.options.indentation.ProxyPreferences getProxyPreferences(java.lang.Object,java.util.prefs.Preferences)
meth public void addNodeChangeListener(java.util.prefs.NodeChangeListener)
meth public void addPreferenceChangeListener(java.util.prefs.PreferenceChangeListener)
meth public void childAdded(java.util.prefs.NodeChangeEvent)
meth public void childRemoved(java.util.prefs.NodeChangeEvent)
meth public void clear() throws java.util.prefs.BackingStoreException
meth public void destroy()
meth public void exportNode(java.io.OutputStream) throws java.io.IOException,java.util.prefs.BackingStoreException
meth public void exportSubtree(java.io.OutputStream) throws java.io.IOException,java.util.prefs.BackingStoreException
meth public void flush() throws java.util.prefs.BackingStoreException
meth public void preferenceChange(java.util.prefs.PreferenceChangeEvent)
meth public void put(java.lang.String,java.lang.String)
meth public void putBoolean(java.lang.String,boolean)
meth public void putByteArray(java.lang.String,byte[])
meth public void putDouble(java.lang.String,double)
meth public void putFloat(java.lang.String,float)
meth public void putInt(java.lang.String,int)
meth public void putLong(java.lang.String,long)
meth public void remove(java.lang.String)
meth public void removeNode() throws java.util.prefs.BackingStoreException
meth public void removeNodeChangeListener(java.util.prefs.NodeChangeListener)
meth public void removePreferenceChangeListener(java.util.prefs.PreferenceChangeListener)
meth public void silence()
meth public void sync() throws java.util.prefs.BackingStoreException
supr java.util.prefs.Preferences
hfds children,delegate,delegateRoot,noEvents,nodeListeners,parent,prefListeners,weakNodeListener,weakPrefListener

CLSS public abstract org.netbeans.spi.lexer.LanguageHierarchy<%0 extends org.netbeans.api.lexer.TokenId>
cons public init()
meth protected abstract java.lang.String mimeType()
meth protected abstract java.util.Collection<{org.netbeans.spi.lexer.LanguageHierarchy%0}> createTokenIds()
meth protected abstract org.netbeans.spi.lexer.Lexer<{org.netbeans.spi.lexer.LanguageHierarchy%0}> createLexer(org.netbeans.spi.lexer.LexerRestartInfo<{org.netbeans.spi.lexer.LanguageHierarchy%0}>)
meth protected boolean isRetainTokenText({org.netbeans.spi.lexer.LanguageHierarchy%0})
meth protected java.util.Map<java.lang.String,java.util.Collection<{org.netbeans.spi.lexer.LanguageHierarchy%0}>> createTokenCategories()
meth protected org.netbeans.spi.lexer.EmbeddingPresence embeddingPresence({org.netbeans.spi.lexer.LanguageHierarchy%0})
meth protected org.netbeans.spi.lexer.LanguageEmbedding<?> embedding(org.netbeans.api.lexer.Token<{org.netbeans.spi.lexer.LanguageHierarchy%0}>,org.netbeans.api.lexer.LanguagePath,org.netbeans.api.lexer.InputAttributes)
meth protected org.netbeans.spi.lexer.TokenValidator<{org.netbeans.spi.lexer.LanguageHierarchy%0}> createTokenValidator({org.netbeans.spi.lexer.LanguageHierarchy%0})
meth public final boolean equals(java.lang.Object)
meth public final int hashCode()
meth public final org.netbeans.api.lexer.Language<{org.netbeans.spi.lexer.LanguageHierarchy%0}> language()
meth public java.lang.String toString()
meth public static org.netbeans.api.lexer.TokenId newId(java.lang.String,int)
meth public static org.netbeans.api.lexer.TokenId newId(java.lang.String,int,java.lang.String)
supr java.lang.Object
hfds language
hcls Accessor

CLSS public abstract org.netbeans.spi.lexer.LanguageProvider
cons protected init()
fld public final static java.lang.String PROP_EMBEDDED_LANGUAGE = "LanguageProvider.PROP_EMBEDDED_LANGUAGE"
fld public final static java.lang.String PROP_LANGUAGE = "LanguageProvider.PROP_LANGUAGE"
meth protected final void firePropertyChange(java.lang.String)
meth public abstract org.netbeans.api.lexer.Language<?> findLanguage(java.lang.String)
meth public abstract org.netbeans.spi.lexer.LanguageEmbedding<?> findLanguageEmbedding(org.netbeans.api.lexer.Token<?>,org.netbeans.api.lexer.LanguagePath,org.netbeans.api.lexer.InputAttributes)
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds pcs

CLSS public abstract org.netbeans.spi.options.OptionsPanelController
cons public init()
fld public final static java.lang.String PROP_CHANGED = "changed"
fld public final static java.lang.String PROP_HELP_CTX = "helpCtx"
fld public final static java.lang.String PROP_VALID = "valid"
innr public abstract interface static !annotation ContainerRegistration
innr public abstract interface static !annotation Keywords
innr public abstract interface static !annotation KeywordsRegistration
innr public abstract interface static !annotation SubRegistration
innr public abstract interface static !annotation TopLevelRegistration
meth protected void setCurrentSubcategory(java.lang.String)
meth public abstract boolean isChanged()
meth public abstract boolean isValid()
meth public abstract javax.swing.JComponent getComponent(org.openide.util.Lookup)
meth public abstract org.openide.util.HelpCtx getHelpCtx()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void applyChanges()
meth public abstract void cancel()
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void update()
meth public final static org.netbeans.spi.options.OptionsPanelController createAdvanced(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public final void setSubcategory(java.lang.String)
meth public org.openide.util.Lookup getLookup()
meth public void handleSuccessfulSearch(java.lang.String,java.util.List<java.lang.String>)
supr java.lang.Object

