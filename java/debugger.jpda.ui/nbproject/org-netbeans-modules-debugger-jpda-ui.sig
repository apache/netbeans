#Signature file v4.1
#Version 1.77

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

CLSS public abstract interface java.io.Serializable

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

CLSS public abstract java.util.AbstractMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons protected init()
innr public static SimpleEntry
innr public static SimpleImmutableEntry
intf java.util.Map<{java.util.AbstractMap%0},{java.util.AbstractMap%1}>
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public abstract java.util.Set<java.util.Map$Entry<{java.util.AbstractMap%0},{java.util.AbstractMap%1}>> entrySet()
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public int hashCode()
meth public int size()
meth public java.lang.String toString()
meth public java.util.Collection<{java.util.AbstractMap%1}> values()
meth public java.util.Set<{java.util.AbstractMap%0}> keySet()
meth public void clear()
meth public void putAll(java.util.Map<? extends {java.util.AbstractMap%0},? extends {java.util.AbstractMap%1}>)
meth public {java.util.AbstractMap%1} get(java.lang.Object)
meth public {java.util.AbstractMap%1} put({java.util.AbstractMap%0},{java.util.AbstractMap%1})
meth public {java.util.AbstractMap%1} remove(java.lang.Object)
supr java.lang.Object

CLSS public abstract interface java.util.EventListener

CLSS public abstract interface java.util.Map<%0 extends java.lang.Object, %1 extends java.lang.Object>
innr public abstract interface static Entry
meth public abstract boolean containsKey(java.lang.Object)
meth public abstract boolean containsValue(java.lang.Object)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isEmpty()
meth public abstract int hashCode()
meth public abstract int size()
meth public abstract java.util.Collection<{java.util.Map%1}> values()
meth public abstract java.util.Set<java.util.Map$Entry<{java.util.Map%0},{java.util.Map%1}>> entrySet()
meth public abstract java.util.Set<{java.util.Map%0}> keySet()
meth public abstract void clear()
meth public abstract void putAll(java.util.Map<? extends {java.util.Map%0},? extends {java.util.Map%1}>)
meth public abstract {java.util.Map%1} get(java.lang.Object)
meth public abstract {java.util.Map%1} put({java.util.Map%0},{java.util.Map%1})
meth public abstract {java.util.Map%1} remove(java.lang.Object)
meth public boolean remove(java.lang.Object,java.lang.Object)
meth public boolean replace({java.util.Map%0},{java.util.Map%1},{java.util.Map%1})
meth public void forEach(java.util.function.BiConsumer<? super {java.util.Map%0},? super {java.util.Map%1}>)
meth public void replaceAll(java.util.function.BiFunction<? super {java.util.Map%0},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} compute({java.util.Map%0},java.util.function.BiFunction<? super {java.util.Map%0},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} computeIfAbsent({java.util.Map%0},java.util.function.Function<? super {java.util.Map%0},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} computeIfPresent({java.util.Map%0},java.util.function.BiFunction<? super {java.util.Map%0},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} getOrDefault(java.lang.Object,{java.util.Map%1})
meth public {java.util.Map%1} merge({java.util.Map%0},{java.util.Map%1},java.util.function.BiFunction<? super {java.util.Map%1},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} putIfAbsent({java.util.Map%0},{java.util.Map%1})
meth public {java.util.Map%1} replace({java.util.Map%0},{java.util.Map%1})

CLSS public abstract interface java.util.function.Supplier<%0 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
meth public abstract {java.util.function.Supplier%0} get()

CLSS public abstract interface javax.accessibility.Accessible
meth public abstract javax.accessibility.AccessibleContext getAccessibleContext()

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

CLSS public abstract interface javax.swing.border.Border
meth public abstract boolean isBorderOpaque()
meth public abstract java.awt.Insets getBorderInsets(java.awt.Component)
meth public abstract void paintBorder(java.awt.Component,java.awt.Graphics,int,int,int,int)

CLSS public abstract interface javax.swing.event.ListDataListener
intf java.util.EventListener
meth public abstract void contentsChanged(javax.swing.event.ListDataEvent)
meth public abstract void intervalAdded(javax.swing.event.ListDataEvent)
meth public abstract void intervalRemoved(javax.swing.event.ListDataEvent)

CLSS public abstract interface javax.swing.table.TableCellRenderer
meth public abstract java.awt.Component getTableCellRendererComponent(javax.swing.JTable,java.lang.Object,boolean,boolean,int,int)

CLSS public org.netbeans.api.debugger.ActionsManagerAdapter
cons public init()
intf org.netbeans.api.debugger.ActionsManagerListener
meth public void actionPerformed(java.lang.Object)
meth public void actionStateChanged(java.lang.Object,boolean)
supr java.lang.Object

CLSS public abstract interface org.netbeans.api.debugger.ActionsManagerListener
fld public final static java.lang.String PROP_ACTION_PERFORMED = "actionPerformed"
fld public final static java.lang.String PROP_ACTION_STATE_CHANGED = "actionStateChanged"
intf java.util.EventListener
meth public abstract void actionPerformed(java.lang.Object)
meth public abstract void actionStateChanged(java.lang.Object,boolean)

CLSS public org.netbeans.api.debugger.DebuggerManagerAdapter
cons public init()
intf org.netbeans.api.debugger.LazyDebuggerManagerListener
meth public java.lang.String[] getProperties()
meth public org.netbeans.api.debugger.Breakpoint[] initBreakpoints()
meth public void breakpointAdded(org.netbeans.api.debugger.Breakpoint)
meth public void breakpointRemoved(org.netbeans.api.debugger.Breakpoint)
meth public void engineAdded(org.netbeans.api.debugger.DebuggerEngine)
meth public void engineRemoved(org.netbeans.api.debugger.DebuggerEngine)
meth public void initWatches()
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void sessionAdded(org.netbeans.api.debugger.Session)
meth public void sessionRemoved(org.netbeans.api.debugger.Session)
meth public void watchAdded(org.netbeans.api.debugger.Watch)
meth public void watchRemoved(org.netbeans.api.debugger.Watch)
supr java.lang.Object

CLSS public abstract interface org.netbeans.api.debugger.DebuggerManagerListener
intf java.beans.PropertyChangeListener
meth public abstract org.netbeans.api.debugger.Breakpoint[] initBreakpoints()
meth public abstract void breakpointAdded(org.netbeans.api.debugger.Breakpoint)
meth public abstract void breakpointRemoved(org.netbeans.api.debugger.Breakpoint)
meth public abstract void engineAdded(org.netbeans.api.debugger.DebuggerEngine)
meth public abstract void engineRemoved(org.netbeans.api.debugger.DebuggerEngine)
meth public abstract void initWatches()
meth public abstract void sessionAdded(org.netbeans.api.debugger.Session)
meth public abstract void sessionRemoved(org.netbeans.api.debugger.Session)
meth public abstract void watchAdded(org.netbeans.api.debugger.Watch)
meth public abstract void watchRemoved(org.netbeans.api.debugger.Watch)

CLSS public abstract org.netbeans.api.debugger.LazyActionsManagerListener
cons public init()
innr public abstract interface static !annotation Registration
meth protected abstract void destroy()
meth public abstract java.lang.String[] getProperties()
supr org.netbeans.api.debugger.ActionsManagerAdapter
hcls ContextAware

CLSS public abstract interface org.netbeans.api.debugger.LazyDebuggerManagerListener
intf org.netbeans.api.debugger.DebuggerManagerListener
meth public abstract java.lang.String[] getProperties()

CLSS public abstract org.netbeans.api.debugger.Properties
cons public init()
innr public abstract interface static Initializer
innr public abstract interface static Reader
meth public abstract boolean getBoolean(java.lang.String,boolean)
meth public abstract byte getByte(java.lang.String,byte)
meth public abstract char getChar(java.lang.String,char)
meth public abstract double getDouble(java.lang.String,double)
meth public abstract float getFloat(java.lang.String,float)
meth public abstract int getInt(java.lang.String,int)
meth public abstract java.lang.Object getObject(java.lang.String,java.lang.Object)
meth public abstract java.lang.Object[] getArray(java.lang.String,java.lang.Object[])
meth public abstract java.lang.String getString(java.lang.String,java.lang.String)
meth public abstract java.util.Collection getCollection(java.lang.String,java.util.Collection)
meth public abstract java.util.Map getMap(java.lang.String,java.util.Map)
meth public abstract long getLong(java.lang.String,long)
meth public abstract org.netbeans.api.debugger.Properties getProperties(java.lang.String)
meth public abstract short getShort(java.lang.String,short)
meth public abstract void setArray(java.lang.String,java.lang.Object[])
meth public abstract void setBoolean(java.lang.String,boolean)
meth public abstract void setByte(java.lang.String,byte)
meth public abstract void setChar(java.lang.String,char)
meth public abstract void setCollection(java.lang.String,java.util.Collection)
meth public abstract void setDouble(java.lang.String,double)
meth public abstract void setFloat(java.lang.String,float)
meth public abstract void setInt(java.lang.String,int)
meth public abstract void setLong(java.lang.String,long)
meth public abstract void setMap(java.lang.String,java.util.Map)
meth public abstract void setObject(java.lang.String,java.lang.Object)
meth public abstract void setShort(java.lang.String,short)
meth public abstract void setString(java.lang.String,java.lang.String)
meth public static org.netbeans.api.debugger.Properties getDefault()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds LOG,defaultProperties
hcls DelegatingProperties,PrimitiveRegister,PropertiesImpl

CLSS public abstract interface static org.netbeans.api.debugger.Properties$Initializer
 outer org.netbeans.api.debugger.Properties
meth public abstract java.lang.Object getDefaultPropertyValue(java.lang.String)
meth public abstract java.lang.String[] getSupportedPropertyNames()

CLSS public abstract interface org.netbeans.api.debugger.jpda.event.JPDABreakpointListener
intf java.util.EventListener
meth public abstract void breakpointReached(org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent)

CLSS public org.netbeans.modules.debugger.jpda.ui.BreakpointOutput
cons public init(org.netbeans.spi.debugger.ContextProvider)
intf java.beans.PropertyChangeListener
intf org.netbeans.api.debugger.DebuggerManagerListener
intf org.netbeans.api.debugger.jpda.event.JPDABreakpointListener
meth protected void destroy()
meth public java.lang.String[] getProperties()
meth public org.netbeans.api.debugger.Breakpoint[] initBreakpoints()
meth public void breakpointAdded(org.netbeans.api.debugger.Breakpoint)
meth public void breakpointReached(org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent)
meth public void breakpointRemoved(org.netbeans.api.debugger.Breakpoint)
meth public void engineAdded(org.netbeans.api.debugger.DebuggerEngine)
meth public void engineRemoved(org.netbeans.api.debugger.DebuggerEngine)
meth public void initWatches()
meth public void printValidityMessage(org.netbeans.api.debugger.Breakpoint,org.netbeans.api.debugger.Breakpoint$VALIDITY,java.lang.String,int)
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void sessionAdded(org.netbeans.api.debugger.Session)
meth public void sessionRemoved(org.netbeans.api.debugger.Session)
meth public void substituteAndPrintText(java.lang.String,org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent)
meth public void watchAdded(org.netbeans.api.debugger.Watch)
meth public void watchRemoved(org.netbeans.api.debugger.Watch)
supr org.netbeans.api.debugger.LazyActionsManagerListener
hfds backslashEscapePattern,breakpointsNodeModel,classNamePattern,contextProvider,debugger,dollarEscapePattern,exceptionClassNamePattern,exceptionMessagePattern,expressionPattern,lineNumberPattern,lock,methodNamePattern,threadNamePattern,threadStartedCondition

CLSS public org.netbeans.modules.debugger.jpda.ui.ConnectPanel
cons public init()
innr public ConnectController
intf java.awt.event.ActionListener
intf org.openide.util.HelpCtx$Provider
meth public org.netbeans.spi.debugger.ui.Controller getController()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void actionPerformed(java.awt.event.ActionEvent)
supr javax.swing.JPanel
hfds RP,USG_LOGGER,cbConnectors,connectors,connectorsLoaded,controller,selectedConnector,standardCursor,tfParams,validityDocumentListener
hcls ValidityDocumentListener

CLSS public org.netbeans.modules.debugger.jpda.ui.ConnectPanel$ConnectController
 outer org.netbeans.modules.debugger.jpda.ui.ConnectPanel
cons public init(org.netbeans.modules.debugger.jpda.ui.ConnectPanel)
intf org.netbeans.spi.debugger.ui.PersistentController
meth public boolean cancel()
meth public boolean isValid()
meth public boolean load(org.netbeans.api.debugger.Properties)
meth public boolean ok()
meth public java.lang.String getDisplayName()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void save(org.netbeans.api.debugger.Properties)
supr java.lang.Object
hfds pcs,valid

CLSS public org.netbeans.modules.debugger.jpda.ui.CurrentThreadAnnotationListener
cons public init()
meth public java.lang.String[] getProperties()
meth public void propertyChange(java.beans.PropertyChangeEvent)
supr org.netbeans.api.debugger.DebuggerManagerAdapter
hfds ANNOTATION_SCHEDULE_TIME,ANNOTATION_STACK_SCHEDULE_TIME,PROP_OPERATIONS_SET,PROP_OPERATIONS_UPDATE,allThreadsAnnotator,currentDebugger,currentPC,currentPCLock,currentPCSet,currentSourcePath,currentThread,rp,sourcePathToAnnotate,stackAnnotations,stackToAnnotate,taskAnnotate,taskRemove,threadToAnnotate
hcls AllThreadsAnnotator,AnnotateCallStackTask,RemoveAnnotationsTask

CLSS public org.netbeans.modules.debugger.jpda.ui.EditorContextBridge
cons public init()
fld public final static java.lang.String CLASS = "class"
fld public final static java.lang.String FIELD = "field"
fld public final static java.lang.String LINE = "line"
fld public final static java.lang.String METHOD = "method"
meth public static java.lang.String getCurrentClassDeclaration()
meth public static java.lang.String getCurrentMethodSignature()
meth public static java.lang.String getDefaultType()
meth public static java.lang.String getFileName(org.netbeans.api.debugger.jpda.LineBreakpoint)
meth public static java.lang.String getMostRecentClassName()
meth public static java.lang.String getMostRecentFieldName()
meth public static java.lang.String getMostRecentMethodName()
meth public static java.lang.String getMostRecentMethodSignature()
meth public static java.lang.String getRelativePath(java.lang.String)
meth public static java.lang.String getRelativePath(org.netbeans.api.debugger.jpda.CallStackFrame,java.lang.String)
meth public static java.lang.String getRelativePath(org.netbeans.api.debugger.jpda.JPDAThread,java.lang.String)
meth public static org.netbeans.spi.debugger.jpda.EditorContext getContext()
supr java.lang.Object
hfds context
hcls CompoundAnnotation,CompoundContextProvider

CLSS public org.netbeans.modules.debugger.jpda.ui.FixedWatchesManager
cons public init(org.netbeans.spi.debugger.ContextProvider)
fld public final static java.lang.String FIXED_WATCH = "org/netbeans/modules/debugger/resources/watchesView/watch_type3_16.png"
intf org.netbeans.spi.viewmodel.ExtendedNodeModelFilter
intf org.netbeans.spi.viewmodel.NodeActionsProviderFilter
intf org.netbeans.spi.viewmodel.TableModelFilter
intf org.netbeans.spi.viewmodel.TreeModelFilter
meth public boolean canCopy(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public boolean canCut(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public boolean canRename(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public boolean isLeaf(org.netbeans.spi.viewmodel.TreeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public boolean isReadOnly(org.netbeans.spi.viewmodel.TableModel,java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public int getChildrenCount(org.netbeans.spi.viewmodel.TreeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.awt.datatransfer.Transferable clipboardCopy(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object) throws java.io.IOException,org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.awt.datatransfer.Transferable clipboardCut(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object) throws java.io.IOException,org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.Object getRoot(org.netbeans.spi.viewmodel.TreeModel)
meth public java.lang.Object getValueAt(org.netbeans.spi.viewmodel.TableModel,java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.Object[] getChildren(org.netbeans.spi.viewmodel.TreeModel,java.lang.Object,int,int) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.String getDisplayName(org.netbeans.spi.viewmodel.NodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.String getIconBase(org.netbeans.spi.viewmodel.NodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.String getIconBaseWithExtension(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.String getShortDescription(org.netbeans.spi.viewmodel.NodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public javax.swing.Action[] getActions(org.netbeans.spi.viewmodel.NodeActionsProvider,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public org.openide.util.datatransfer.PasteType[] getPasteTypes(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object,java.awt.datatransfer.Transferable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public void addFixedWatch(java.lang.String,org.netbeans.api.debugger.jpda.Variable)
meth public void addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public void deleteAllFixedWatches()
meth public void performDefaultAction(org.netbeans.spi.viewmodel.NodeActionsProvider,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public void removeModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public void setName(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public void setValueAt(org.netbeans.spi.viewmodel.TableModel,java.lang.Object,java.lang.String,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
supr java.lang.Object
hfds CREATE_FIXED_WATCH_ACTION,DELETE_ACTION,contextProvider,fixedWatches,listeners
hcls KeyWrapper

CLSS public org.netbeans.modules.debugger.jpda.ui.JPDAAttachType
cons public init()
meth public javax.swing.JComponent getCustomizer()
meth public org.netbeans.spi.debugger.ui.Controller getController()
supr org.netbeans.spi.debugger.ui.AttachType
hfds customizerRef

CLSS public org.netbeans.modules.debugger.jpda.ui.JPDACodeEvaluator
cons public init(org.netbeans.spi.debugger.ContextProvider)
meth public boolean canEvaluate()
meth public java.lang.String getExpression()
meth public java.util.List<java.lang.String> getExpressionsHistory()
meth public org.openide.util.RequestProcessor getRequestProcessor()
meth public void evaluate(java.lang.String)
meth public void setupContext(javax.swing.JEditorPane,java.lang.Runnable)
supr org.netbeans.spi.debugger.ui.CodeEvaluator$EvaluatorService
hfds LOG,debugger,debuggerListener,evalTask,expression,historyPersistence,result,rp
hcls DebuggerChangeListener,EvaluateTask

CLSS public org.netbeans.modules.debugger.jpda.ui.JavaUtils
cons public init()
meth public static java.util.concurrent.Future<java.lang.Void> runWhenScanFinishedReallyLazy(org.netbeans.api.java.source.JavaSource,org.netbeans.api.java.source.Task<org.netbeans.api.java.source.CompilationController>,boolean) throws java.io.IOException
supr java.lang.Object
hfds ASYNC_WAIT_TIME,scanningProcessor
hcls ScanRunnable

CLSS public final org.netbeans.modules.debugger.jpda.ui.MultilinePanel
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
supr javax.swing.JPanel

CLSS public org.netbeans.modules.debugger.jpda.ui.SmartSteppingImpl
cons public init()
intf java.beans.PropertyChangeListener
meth public boolean stopHere(org.netbeans.spi.debugger.ContextProvider,org.netbeans.api.debugger.jpda.JPDAThread,org.netbeans.api.debugger.jpda.SmartSteppingFilter)
meth public void initFilter(org.netbeans.api.debugger.jpda.SmartSteppingFilter)
meth public void propertyChange(java.beans.PropertyChangeEvent)
supr org.netbeans.spi.debugger.jpda.SmartSteppingCallback
hfds engineContext,exclusionPatterns,smartSteppingFilter

CLSS public org.netbeans.modules.debugger.jpda.ui.SourcePath
cons public init(org.netbeans.spi.debugger.ContextProvider)
meth public boolean sourceAvailable(java.lang.String,boolean)
meth public boolean sourceAvailable(org.netbeans.api.debugger.jpda.CallStackFrame,java.lang.String)
meth public boolean sourceAvailable(org.netbeans.api.debugger.jpda.Field)
meth public boolean sourceAvailable(org.netbeans.api.debugger.jpda.JPDAThread,java.lang.String,boolean)
meth public java.lang.Object annotate(org.netbeans.api.debugger.jpda.CallStackFrame,java.lang.String)
meth public java.lang.Object annotate(org.netbeans.api.debugger.jpda.JPDAThread,java.lang.String,java.lang.String,int)
meth public java.lang.Object annotate(org.netbeans.api.debugger.jpda.JPDAThread,java.lang.String,java.lang.String,int,boolean)
meth public java.lang.String getRelativePath(java.lang.String,char,boolean)
meth public java.lang.String getURL(org.netbeans.api.debugger.jpda.CallStackFrame,java.lang.String)
meth public java.lang.String getURL(org.netbeans.api.debugger.jpda.CallStackFrame,java.lang.String,java.lang.String[])
meth public java.lang.String getURL(org.netbeans.api.debugger.jpda.JPDAThread,java.lang.String)
meth public java.lang.String[] getAdditionalSourceRoots()
meth public java.lang.String[] getOriginalSourceRoots()
meth public java.lang.String[] getSourceRoots()
meth public static java.lang.String convertClassNameToRelativePath(java.lang.String)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void reorderOriginalSourceRoots(int[])
meth public void setSourceRoots(java.lang.String[],java.lang.String[])
meth public void showSource(org.netbeans.api.debugger.jpda.CallStackFrame,java.lang.String)
meth public void showSource(org.netbeans.api.debugger.jpda.Field)
meth public void showSource(org.netbeans.api.debugger.jpda.Field,boolean)
meth public void showSource(org.netbeans.api.debugger.jpda.JPDAThread,java.lang.String)
supr java.lang.Object
hfds contextProvider,debugger,sourcePathProvider
hcls CompoundAnnotation,CompoundContextProvider

CLSS public org.netbeans.modules.debugger.jpda.ui.WatchPanel
cons public init(java.lang.String)
innr public final static DelegatingBorder
meth public java.lang.String getExpression()
meth public javax.swing.JComponent getPanel()
meth public static void setupContext(javax.swing.JEditorPane,java.lang.Runnable)
meth public static void setupContext(javax.swing.JEditorPane,java.lang.String,int,int)
meth public static void setupContext(javax.swing.JEditorPane,java.lang.String,int,int,org.netbeans.api.debugger.jpda.JPDADebugger)
supr java.lang.Object
hfds editorPane,expression,logger,panel
hcls Context,MyTrees,MyWrapperFactory

CLSS public final static org.netbeans.modules.debugger.jpda.ui.WatchPanel$DelegatingBorder
 outer org.netbeans.modules.debugger.jpda.ui.WatchPanel
cons public init(javax.swing.border.Border,java.awt.Insets)
intf javax.swing.border.Border
meth public boolean isBorderOpaque()
meth public java.awt.Insets getBorderInsets(java.awt.Component)
meth public java.awt.Insets getInsets()
meth public void paintBorder(java.awt.Component,java.awt.Graphics,int,int,int,int)
meth public void setInsets(java.awt.Insets)
supr java.lang.Object
hfds delegate,insets

CLSS public org.netbeans.modules.debugger.jpda.ui.breakpoints.ActionsPanel
cons public init(org.netbeans.api.debugger.jpda.JPDABreakpoint)
meth public void ok()
supr javax.swing.JPanel
hfds DEFAULT_SUSPEND_ACTION,NONE_BREAKPOINT_GROUP,breakpoint,cbSuspend,checkBoxPanel,checkedSuspendAction,defaultActionCheckBox,defaultSuspendAction,disableGroupCheckBox,disableGroupComboBox,disableGroupLabel,enableGroupCheckBox,enableGroupComboBox,enableGroupLabel,jLabel1,jLabel2,preferences,tfPrintText
hcls NoneBreakpointGroup

CLSS public org.netbeans.modules.debugger.jpda.ui.breakpoints.ClassBreakpointPanel
cons public init()
cons public init(org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint)
intf org.netbeans.spi.debugger.ui.Controller
intf org.openide.util.HelpCtx$Provider
meth public boolean cancel()
meth public boolean ok()
meth public org.openide.util.HelpCtx getHelpCtx()
supr javax.swing.JPanel
hfds HELP_ID,actionsPanel,breakpoint,cPanel,cbBreakpointType,conditionsPanel,createBreakpoint,epClassName,jLabel3,jLabel4,jPanel1,pActions,pSettings,spClassName

CLSS public org.netbeans.modules.debugger.jpda.ui.breakpoints.ClassBreakpointType
cons public init()
meth public boolean isDefault()
meth public java.lang.String getCategoryDisplayName()
meth public java.lang.String getTypeDisplayName()
meth public javax.swing.JComponent getCustomizer()
supr org.netbeans.spi.debugger.ui.BreakpointType

CLSS public org.netbeans.modules.debugger.jpda.ui.breakpoints.ConditionsPanel
cons public init(java.lang.String)
meth public int getHitCount()
meth public java.lang.String getCondition()
meth public java.lang.String valiadateMsg()
meth public java.lang.String[] getClassExcludeFilter()
meth public java.lang.String[] getClassMatchFilter()
meth public org.netbeans.api.debugger.Breakpoint$HIT_COUNT_FILTERING_STYLE getHitCountFilteringStyle()
meth public void setClassExcludeFilter(java.lang.String[])
meth public void setClassMatchFilter(java.lang.String[])
meth public void setCondition(java.lang.String)
meth public void setHitCount(int)
meth public void setHitCountFilteringStyle(org.netbeans.api.debugger.Breakpoint$HIT_COUNT_FILTERING_STYLE)
meth public void setupConditionPaneContext()
meth public void setupConditionPaneContext(java.lang.String,int)
meth public void showClassFilter(boolean)
meth public void showCondition(boolean)
meth public void showExclusionClassFilter(boolean)
supr javax.swing.JPanel
hfds MAX_SAVED_CONDITIONS,cbHitStyle,cbWhenHitCount,classExcludeFilterCheckBox,classExcludeFilterLabel,classExcludeFilterTextField,classFilterCheckBox,classIncludeFilterLabel,classIncludeFilterTextField,conditionCheckBox,conditionComboBox,panelHitCountFilter,spCondition,tfCondition,tfConditionFieldForUI,tfHitCountFilter
hcls ConditionComboBoxEditor

CLSS public abstract interface org.netbeans.modules.debugger.jpda.ui.breakpoints.ControllerProvider
meth public abstract org.netbeans.spi.debugger.ui.Controller getController()

CLSS public org.netbeans.modules.debugger.jpda.ui.breakpoints.ExceptionBreakpointPanel
cons public init()
cons public init(org.netbeans.api.debugger.jpda.ExceptionBreakpoint)
intf org.netbeans.spi.debugger.ui.Controller
intf org.openide.util.HelpCtx$Provider
meth public boolean cancel()
meth public boolean ok()
meth public org.openide.util.HelpCtx getHelpCtx()
supr javax.swing.JPanel
hfds HELP_ID,actionsPanel,breakpoint,cPanel,cbBreakpointType,conditionsPanel,createBreakpoint,epExceptionClassName,jLabel3,jLabel4,jPanel1,pActions,pSettings,spExceptionClassName

CLSS public org.netbeans.modules.debugger.jpda.ui.breakpoints.ExceptionBreakpointType
cons public init()
meth public boolean isDefault()
meth public java.lang.String getCategoryDisplayName()
meth public java.lang.String getTypeDisplayName()
meth public javax.swing.JComponent getCustomizer()
supr org.netbeans.spi.debugger.ui.BreakpointType

CLSS public org.netbeans.modules.debugger.jpda.ui.breakpoints.FieldBreakpointPanel
cons public init()
cons public init(org.netbeans.api.debugger.jpda.FieldBreakpoint)
intf org.netbeans.spi.debugger.ui.Controller
intf org.openide.util.HelpCtx$Provider
meth public boolean cancel()
meth public boolean ok()
meth public org.openide.util.HelpCtx getHelpCtx()
supr javax.swing.JPanel
hfds HELP_ID,actionsPanel,breakpoint,cPanel,cbBreakpointType,conditionsPanel,createBreakpoint,epClassName,epFieldName,jLabel1,jLabel3,jLabel4,jPanel1,pActions,pSettings,spClassName,spFieldName

CLSS public org.netbeans.modules.debugger.jpda.ui.breakpoints.FieldBreakpointType
cons public init()
meth public boolean isDefault()
meth public java.lang.String getCategoryDisplayName()
meth public java.lang.String getTypeDisplayName()
meth public javax.swing.JComponent getCustomizer()
supr org.netbeans.spi.debugger.ui.BreakpointType

CLSS public org.netbeans.modules.debugger.jpda.ui.breakpoints.FileMoveBreakpointsHandler
cons public init()
intf org.netbeans.api.debugger.LazyDebuggerManagerListener
meth public java.lang.String[] getProperties()
meth public org.netbeans.api.debugger.Breakpoint[] initBreakpoints()
meth public void breakpointAdded(org.netbeans.api.debugger.Breakpoint)
meth public void breakpointRemoved(org.netbeans.api.debugger.Breakpoint)
meth public void engineAdded(org.netbeans.api.debugger.DebuggerEngine)
meth public void engineRemoved(org.netbeans.api.debugger.DebuggerEngine)
meth public void initWatches()
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void sessionAdded(org.netbeans.api.debugger.Session)
meth public void sessionRemoved(org.netbeans.api.debugger.Session)
meth public void watchAdded(org.netbeans.api.debugger.Watch)
meth public void watchRemoved(org.netbeans.api.debugger.Watch)
supr java.lang.Object
hfds LOG,handlerMap,preferedHandler
hcls BreakpointHandler

CLSS public org.netbeans.modules.debugger.jpda.ui.breakpoints.JPDABreakpointCustomizer
cons public init()
intf java.beans.Customizer
intf org.netbeans.spi.debugger.ui.Controller
meth public boolean cancel()
meth public boolean ok()
meth public void setObject(java.lang.Object)
supr javax.swing.JPanel
hfds b,c

CLSS public org.netbeans.modules.debugger.jpda.ui.breakpoints.LineBreakpointPanel
cons public init()
cons public init(org.netbeans.api.debugger.jpda.LineBreakpoint)
cons public init(org.netbeans.api.debugger.jpda.LineBreakpoint,boolean)
intf org.netbeans.modules.debugger.jpda.ui.breakpoints.ControllerProvider
intf org.openide.util.HelpCtx$Provider
meth public org.netbeans.spi.debugger.ui.Controller getController()
meth public org.openide.util.HelpCtx getHelpCtx()
supr javax.swing.JPanel
hfds HELP_ID,actionsPanel,breakpoint,cPanel,conditionsPanel,controller,createBreakpoint,fileURL,jLabel1,jLabel3,jPanel1,logger,pActions,pSettings,tfFileName,tfLineNumber,validityDocumentListener
hcls LBController,ValidityDocumentListener

CLSS public org.netbeans.modules.debugger.jpda.ui.breakpoints.LineBreakpointType
cons public init()
meth public boolean isDefault()
meth public java.lang.String getCategoryDisplayName()
meth public java.lang.String getTypeDisplayName()
meth public javax.swing.JComponent getCustomizer()
meth public org.netbeans.spi.debugger.ui.Controller getController()
supr org.netbeans.spi.debugger.ui.BreakpointType
hfds customizerRef

CLSS public org.netbeans.modules.debugger.jpda.ui.breakpoints.MethodBreakpointPanel
cons public init()
cons public init(org.netbeans.api.debugger.jpda.MethodBreakpoint)
intf org.netbeans.spi.debugger.ui.Controller
intf org.openide.util.HelpCtx$Provider
meth public boolean cancel()
meth public boolean ok()
meth public org.openide.util.HelpCtx getHelpCtx()
supr javax.swing.JPanel
hfds HELP_ID,actionsPanel,breakpoint,cPanel,cbAllMethods,cbBreakpointType,conditionsPanel,createBreakpoint,epClassName,epMethodName,jLabel1,jLabel3,jPanel1,pActions,pSettings,panelClassName,spClassName,spMethodName,stopOnLabel

CLSS public org.netbeans.modules.debugger.jpda.ui.breakpoints.MethodBreakpointType
cons public init()
meth public boolean isDefault()
meth public java.lang.String getCategoryDisplayName()
meth public java.lang.String getTypeDisplayName()
meth public javax.swing.JComponent getCustomizer()
supr org.netbeans.spi.debugger.ui.BreakpointType

CLSS public org.netbeans.modules.debugger.jpda.ui.breakpoints.OutlineComboBox
cons public init()
innr public abstract interface static Expandable
innr public abstract interface static PopupMenuItem
meth public void setItems(java.lang.Object[])
meth public void setPopupVisible(boolean)
supr javax.swing.JComboBox
hfds areExpandables,expandedItems,expansionHandleWidth,keepPopupVisible,popupList
hcls OutlineComboBoxModel,OutlineComboBoxRenderer

CLSS public abstract interface static org.netbeans.modules.debugger.jpda.ui.breakpoints.OutlineComboBox$Expandable
 outer org.netbeans.modules.debugger.jpda.ui.breakpoints.OutlineComboBox
meth public abstract boolean isExpanded()
meth public abstract java.lang.Object[] getItems()
meth public abstract void setExpanded(boolean)

CLSS public abstract interface static org.netbeans.modules.debugger.jpda.ui.breakpoints.OutlineComboBox$PopupMenuItem
 outer org.netbeans.modules.debugger.jpda.ui.breakpoints.OutlineComboBox
meth public abstract java.lang.String toPopupMenuString()

CLSS public org.netbeans.modules.debugger.jpda.ui.breakpoints.ThreadBreakpointPanel
cons public init()
cons public init(org.netbeans.api.debugger.jpda.ThreadBreakpoint)
intf org.netbeans.spi.debugger.ui.Controller
intf org.openide.util.HelpCtx$Provider
meth public boolean cancel()
meth public boolean ok()
meth public org.openide.util.HelpCtx getHelpCtx()
supr javax.swing.JPanel
hfds HELP_ID,actionsPanel,breakpoint,cPanel,cbBreakpointType,conditionsPanel,createBreakpoint,jLabel4,jPanel1,pActions,pSettings

CLSS public org.netbeans.modules.debugger.jpda.ui.breakpoints.ThreadBreakpointType
cons public init()
meth public boolean isDefault()
meth public java.lang.String getCategoryDisplayName()
meth public java.lang.String getTypeDisplayName()
meth public javax.swing.JComponent getCustomizer()
supr org.netbeans.spi.debugger.ui.BreakpointType

CLSS public org.netbeans.modules.debugger.jpda.ui.debugging.DebuggingViewSupportImpl
cons public init(org.netbeans.spi.debugger.ContextProvider)
meth protected int getFrameCount(org.netbeans.modules.debugger.jpda.ui.debugging.JPDADVThread)
meth protected java.util.List<org.netbeans.spi.debugger.ui.DebuggingView$DVFilter> getFilters()
meth protected java.util.List<org.netbeans.spi.debugger.ui.DebuggingView$DVFrame> getFrames(org.netbeans.modules.debugger.jpda.ui.debugging.JPDADVThread,int,int)
meth public java.awt.Image getIcon(org.netbeans.spi.debugger.ui.DebuggingView$DVThread)
meth public java.lang.String getDisplayName(org.netbeans.spi.debugger.ui.DebuggingView$DVThread)
meth public java.util.List<org.netbeans.spi.debugger.ui.DebuggingView$DVThread> getAllThreads()
meth public java.util.Set<org.netbeans.spi.debugger.ui.DebuggingView$Deadlock> getDeadlocks()
meth public org.netbeans.api.debugger.Session getSession()
meth public org.netbeans.modules.debugger.jpda.ui.debugging.JPDADVThread get(org.netbeans.api.debugger.jpda.JPDAThread)
meth public org.netbeans.modules.debugger.jpda.ui.debugging.JPDADVThreadGroup get(org.netbeans.api.debugger.jpda.JPDAThreadGroup)
meth public org.netbeans.modules.debugger.jpda.ui.debugging.JPDADVThreadGroup[] get(org.netbeans.api.debugger.jpda.JPDAThreadGroup[])
meth public org.netbeans.modules.debugger.jpda.ui.debugging.JPDADVThread[] get(org.netbeans.api.debugger.jpda.JPDAThread[])
meth public org.netbeans.spi.debugger.ui.DebuggingView$DVSupport$STATE getState()
meth public org.netbeans.spi.debugger.ui.DebuggingView$DVThread getCurrentThread()
meth public static java.util.prefs.Preferences getFilterPreferences()
meth public void resume()
supr org.netbeans.spi.debugger.ui.DebuggingView$DVSupport
hfds debugger,preferences,threadGroupsMap,threadsMap
hcls ChangeListener

CLSS public final org.netbeans.modules.debugger.jpda.ui.debugging.JPDADVFrame
intf org.netbeans.spi.debugger.ui.DebuggingView$DVFrame
meth public int getColumn()
meth public int getLine()
meth public java.lang.String getName()
meth public java.net.URI getSourceURI()
meth public org.netbeans.api.debugger.jpda.CallStackFrame getCallStackFrame()
meth public org.netbeans.spi.debugger.ui.DebuggingView$DVThread getThread()
meth public void makeCurrent()
meth public void popOff() throws org.netbeans.spi.debugger.ui.DebuggingView$PopException
supr java.lang.Object
hfds stackFrame,thread

CLSS public final org.netbeans.modules.debugger.jpda.ui.debugging.JPDADVThread
intf java.util.function.Supplier<org.netbeans.api.debugger.jpda.JPDAThread>
intf org.netbeans.modules.debugger.jpda.util.WeakCacheMap$KeyedValue<org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl>
intf org.netbeans.spi.debugger.ui.DebuggingView$DVThread
meth public boolean isInStep()
meth public boolean isSuspended()
meth public int getFrameCount()
meth public java.lang.String getName()
meth public java.util.List<org.netbeans.spi.debugger.ui.DebuggingView$DVFrame> getFrames()
meth public java.util.List<org.netbeans.spi.debugger.ui.DebuggingView$DVFrame> getFrames(int,int)
meth public java.util.List<org.netbeans.spi.debugger.ui.DebuggingView$DVThread> getLockerThreads()
meth public org.netbeans.api.debugger.Breakpoint getCurrentBreakpoint()
meth public org.netbeans.api.debugger.jpda.JPDAThread get()
meth public org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl getKey()
meth public org.netbeans.spi.debugger.ui.DebuggingView$DVSupport getDVSupport()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void makeCurrent()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void resume()
meth public void resumeBlockingThreads()
meth public void suspend()
supr java.lang.Object
hfds dvSupport,proxyListener,t
hcls PropertyChangeProxyListener,ThreadListDelegate

CLSS public org.netbeans.modules.debugger.jpda.ui.debugging.JPDADVThreadGroup
intf org.netbeans.modules.debugger.jpda.util.WeakCacheMap$KeyedValue<org.netbeans.modules.debugger.jpda.models.JPDAThreadGroupImpl>
intf org.netbeans.spi.debugger.ui.DebuggingView$DVThreadGroup
meth public java.lang.String getName()
meth public org.netbeans.modules.debugger.jpda.models.JPDAThreadGroupImpl getKey()
meth public org.netbeans.spi.debugger.ui.DebuggingView$DVThreadGroup getParentThreadGroup()
meth public org.netbeans.spi.debugger.ui.DebuggingView$DVThreadGroup[] getThreadGroups()
meth public org.netbeans.spi.debugger.ui.DebuggingView$DVThread[] getThreads()
supr java.lang.Object
hfds dvSupport,tg

CLSS public org.netbeans.modules.debugger.jpda.ui.options.DisablingCellRenderer
cons public init(javax.swing.table.TableCellRenderer,javax.swing.JTable)
cons public init(javax.swing.table.TableCellRenderer,javax.swing.JTable,java.awt.Color)
intf javax.swing.table.TableCellRenderer
meth public java.awt.Component getTableCellRendererComponent(javax.swing.JTable,java.lang.Object,boolean,boolean,int,int)
meth public static void apply(javax.swing.JTable)
meth public static void apply(javax.swing.JTable,java.awt.Color)
supr java.lang.Object
hfds background,r,t

CLSS public final org.netbeans.modules.debugger.jpda.ui.options.JavaDebuggerOptionsPanelController
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
hfds changed,panel,pcs

CLSS public org.netbeans.modules.debugger.jpda.ui.options.OptionsInitializer
cons public init()
intf org.netbeans.api.debugger.Properties$Initializer
meth public java.lang.Object getDefaultPropertyValue(java.lang.String)
meth public java.lang.String[] getSupportedPropertyNames()
supr java.lang.Object
hfds CLASS_FILTERS_ALL,CLASS_FILTERS_ENABLED

CLSS public abstract org.netbeans.modules.debugger.jpda.ui.options.StorablePanel
cons public init()
innr public abstract interface static Provider
meth public abstract boolean isChanged()
meth public abstract void load()
meth public abstract void store()
supr javax.swing.JPanel

CLSS public abstract interface static org.netbeans.modules.debugger.jpda.ui.options.StorablePanel$Provider
 outer org.netbeans.modules.debugger.jpda.ui.options.StorablePanel
meth public abstract java.lang.String getPanelName()
meth public abstract org.netbeans.modules.debugger.jpda.ui.options.StorablePanel getPanel()

CLSS public org.netbeans.modules.debugger.jpda.ui.options.VariableFormatterEditPanel
cons public init()
meth public boolean checkValidInput()
meth public void load(org.netbeans.modules.debugger.jpda.expr.formatters.VariablesFormatter)
meth public void store(org.netbeans.modules.debugger.jpda.expr.formatters.VariablesFormatter)
supr javax.swing.JPanel
hfds addVarButton,childrenButtonGroup,childrenCodeEditorPane,childrenCodeRadioButton,childrenCodeScrollPane,childrenFormatCheckBox,childrenVariablesRadioButton,childrenVariablesTable,classTypesLabel,classTypesTextField,continualValidityChecks,formatterNames,jPanel1,jPanel2,jPanel3,jScrollPane1,moveDownVarButton,moveUpVarButton,nameLabel,nameTextField,removeVarButton,subtypesCheckBox,tableColumnNames,testChildrenCheckBox,testChildrenEditorPane,testChildrenScrollPane,validityDescriptor,validityNotificationSupport,valueEditorPane,valueFormatCheckBox,valueScrollPane

CLSS public final org.netbeans.modules.debugger.jpda.util.WeakCacheMap<%0 extends java.lang.Object, %1 extends org.netbeans.modules.debugger.jpda.util.WeakCacheMap$KeyedValue<{org.netbeans.modules.debugger.jpda.util.WeakCacheMap%0}>>
cons public init()
innr public abstract interface static KeyedValue
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean isEmpty()
meth public int size()
meth public java.util.Set<java.util.Map$Entry<{org.netbeans.modules.debugger.jpda.util.WeakCacheMap%0},{org.netbeans.modules.debugger.jpda.util.WeakCacheMap%1}>> entrySet()
meth public void clear()
meth public {org.netbeans.modules.debugger.jpda.util.WeakCacheMap%1} get(java.lang.Object)
meth public {org.netbeans.modules.debugger.jpda.util.WeakCacheMap%1} put({org.netbeans.modules.debugger.jpda.util.WeakCacheMap%0},{org.netbeans.modules.debugger.jpda.util.WeakCacheMap%1})
meth public {org.netbeans.modules.debugger.jpda.util.WeakCacheMap%1} remove(java.lang.Object)
supr java.util.AbstractMap<{org.netbeans.modules.debugger.jpda.util.WeakCacheMap%0},{org.netbeans.modules.debugger.jpda.util.WeakCacheMap%1}>
hfds cache

CLSS public abstract interface static org.netbeans.modules.debugger.jpda.util.WeakCacheMap$KeyedValue<%0 extends java.lang.Object>
 outer org.netbeans.modules.debugger.jpda.util.WeakCacheMap
meth public abstract {org.netbeans.modules.debugger.jpda.util.WeakCacheMap$KeyedValue%0} getKey()

CLSS public abstract org.netbeans.spi.debugger.jpda.SmartSteppingCallback
cons public init()
innr public abstract interface static !annotation Registration
innr public final static StopOrStep
meth public abstract boolean stopHere(org.netbeans.spi.debugger.ContextProvider,org.netbeans.api.debugger.jpda.JPDAThread,org.netbeans.api.debugger.jpda.SmartSteppingFilter)
meth public abstract void initFilter(org.netbeans.api.debugger.jpda.SmartSteppingFilter)
meth public org.netbeans.spi.debugger.jpda.SmartSteppingCallback$StopOrStep stopAt(org.netbeans.spi.debugger.ContextProvider,org.netbeans.api.debugger.jpda.CallStackFrame,org.netbeans.api.debugger.jpda.SmartSteppingFilter)
supr java.lang.Object
hcls ContextAware

CLSS public abstract org.netbeans.spi.debugger.ui.AttachType
cons public init()
innr public abstract interface static !annotation Registration
meth public abstract javax.swing.JComponent getCustomizer()
meth public java.lang.String getTypeDisplayName()
meth public org.netbeans.spi.debugger.ui.Controller getController()
supr java.lang.Object
hcls ContextAware

CLSS public abstract org.netbeans.spi.debugger.ui.BreakpointType
cons public init()
innr public abstract interface static !annotation Registration
meth public abstract boolean isDefault()
meth public abstract java.lang.String getCategoryDisplayName()
meth public abstract javax.swing.JComponent getCustomizer()
meth public java.lang.String getTypeDisplayName()
meth public org.netbeans.spi.debugger.ui.Controller getController()
supr java.lang.Object
hcls ContextAware

CLSS public final org.netbeans.spi.debugger.ui.CodeEvaluator
innr public abstract static EvaluatorService
innr public final static DefaultExpressionsHistoryPersistence
innr public final static Result
meth public static org.netbeans.spi.debugger.ui.CodeEvaluator getDefault()
meth public void open()
meth public void requestFocus()
meth public void setExpression(java.lang.String)
supr java.lang.Object
hfds INSTANCE

CLSS public abstract static org.netbeans.spi.debugger.ui.CodeEvaluator$EvaluatorService
 outer org.netbeans.spi.debugger.ui.CodeEvaluator
cons public init()
fld public final static java.lang.String PROP_CAN_EVALUATE = "canEvaluate"
fld public final static java.lang.String PROP_EXPRESSIONS_HISTORY = "expressionsHistory"
innr public abstract interface static !annotation Registration
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract boolean canEvaluate()
meth public abstract java.util.List<java.lang.String> getExpressionsHistory()
meth public abstract void evaluate(java.lang.String)
meth public abstract void setupContext(javax.swing.JEditorPane,java.lang.Runnable)
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds pchs

CLSS public abstract interface org.netbeans.spi.debugger.ui.Controller
fld public final static java.lang.String PROP_VALID = "valid"
meth public abstract boolean cancel()
meth public abstract boolean isValid()
meth public abstract boolean ok()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public final org.netbeans.spi.debugger.ui.DebuggingView
innr public abstract interface static DVFrame
innr public abstract interface static DVThread
innr public abstract interface static DVThreadGroup
innr public abstract static DVSupport
innr public final static DVFilter
innr public final static Deadlock
innr public final static PopException
meth public org.openide.windows.TopComponent getViewTC()
meth public static org.netbeans.spi.debugger.ui.DebuggingView getDefault()
supr java.lang.Object
hfds INSTANCE,dvcRef

CLSS public abstract interface static org.netbeans.spi.debugger.ui.DebuggingView$DVFrame
 outer org.netbeans.spi.debugger.ui.DebuggingView
meth public abstract int getColumn()
meth public abstract int getLine()
meth public abstract java.lang.String getName()
meth public abstract java.net.URI getSourceURI()
meth public abstract org.netbeans.spi.debugger.ui.DebuggingView$DVThread getThread()
meth public abstract void makeCurrent()
meth public java.lang.String getSourceMimeType()
meth public void popOff() throws org.netbeans.spi.debugger.ui.DebuggingView$PopException

CLSS public abstract static org.netbeans.spi.debugger.ui.DebuggingView$DVSupport
 outer org.netbeans.spi.debugger.ui.DebuggingView
cons protected init()
fld public final static java.lang.String PROP_CURRENT_THREAD = "currentThread"
fld public final static java.lang.String PROP_DEADLOCK = "deadlock"
fld public final static java.lang.String PROP_STATE = "state"
fld public final static java.lang.String PROP_THREAD_DIED = "threadDied"
fld public final static java.lang.String PROP_THREAD_GROUP_ADDED = "threadGroupAdded"
fld public final static java.lang.String PROP_THREAD_RESUMED = "threadResumed"
fld public final static java.lang.String PROP_THREAD_STARTED = "threadStarted"
fld public final static java.lang.String PROP_THREAD_SUSPENDED = "threadSuspended"
innr public abstract interface static !annotation Registration
innr public final static !enum STATE
meth protected abstract java.util.List<org.netbeans.spi.debugger.ui.DebuggingView$DVFilter> getFilters()
meth protected final org.netbeans.spi.debugger.ui.DebuggingView$Deadlock createDeadlock(java.util.Collection<org.netbeans.spi.debugger.ui.DebuggingView$DVThread>)
meth protected final void firePropertyChange(java.beans.PropertyChangeEvent)
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract java.awt.Image getIcon(org.netbeans.spi.debugger.ui.DebuggingView$DVThread)
meth public abstract java.lang.String getDisplayName(org.netbeans.spi.debugger.ui.DebuggingView$DVThread)
meth public abstract java.util.List<org.netbeans.spi.debugger.ui.DebuggingView$DVThread> getAllThreads()
meth public abstract java.util.Set<org.netbeans.spi.debugger.ui.DebuggingView$Deadlock> getDeadlocks()
meth public abstract org.netbeans.api.debugger.Session getSession()
meth public abstract org.netbeans.spi.debugger.ui.DebuggingView$DVSupport$STATE getState()
meth public abstract org.netbeans.spi.debugger.ui.DebuggingView$DVThread getCurrentThread()
meth public abstract void resume()
meth public final javax.swing.Action[] getFilterActions()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public java.lang.String getDisplayName(org.netbeans.spi.debugger.ui.DebuggingView$DVFrame)
supr java.lang.Object
hfds pcs

CLSS public abstract interface static org.netbeans.spi.debugger.ui.DebuggingView$DVThread
 outer org.netbeans.spi.debugger.ui.DebuggingView
fld public final static java.lang.String PROP_BREAKPOINT = "currentBreakpoint"
fld public final static java.lang.String PROP_LOCKER_THREADS = "lockerThreads"
fld public final static java.lang.String PROP_SUSPENDED = "suspended"
meth public abstract boolean isInStep()
meth public abstract boolean isSuspended()
meth public abstract java.lang.String getName()
meth public abstract java.util.List<org.netbeans.spi.debugger.ui.DebuggingView$DVThread> getLockerThreads()
meth public abstract org.netbeans.api.debugger.Breakpoint getCurrentBreakpoint()
meth public abstract org.netbeans.spi.debugger.ui.DebuggingView$DVSupport getDVSupport()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void makeCurrent()
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void resume()
meth public abstract void resumeBlockingThreads()
meth public abstract void suspend()
meth public int getFrameCount()
meth public java.util.List<org.netbeans.spi.debugger.ui.DebuggingView$DVFrame> getFrames()
meth public java.util.List<org.netbeans.spi.debugger.ui.DebuggingView$DVFrame> getFrames(int,int)

CLSS public abstract interface static org.netbeans.spi.debugger.ui.DebuggingView$DVThreadGroup
 outer org.netbeans.spi.debugger.ui.DebuggingView
meth public abstract java.lang.String getName()
meth public abstract org.netbeans.spi.debugger.ui.DebuggingView$DVThreadGroup getParentThreadGroup()
meth public abstract org.netbeans.spi.debugger.ui.DebuggingView$DVThreadGroup[] getThreadGroups()
meth public abstract org.netbeans.spi.debugger.ui.DebuggingView$DVThread[] getThreads()

CLSS public abstract interface org.netbeans.spi.debugger.ui.PersistentController
intf org.netbeans.spi.debugger.ui.Controller
meth public abstract boolean load(org.netbeans.api.debugger.Properties)
meth public abstract java.lang.String getDisplayName()
meth public abstract void save(org.netbeans.api.debugger.Properties)

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

CLSS public abstract interface org.netbeans.spi.viewmodel.ExtendedNodeModelFilter
intf org.netbeans.spi.viewmodel.NodeModelFilter
meth public abstract boolean canCopy(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract boolean canCut(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract boolean canRename(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.awt.datatransfer.Transferable clipboardCopy(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object) throws java.io.IOException,org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.awt.datatransfer.Transferable clipboardCut(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object) throws java.io.IOException,org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String getIconBaseWithExtension(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract org.openide.util.datatransfer.PasteType[] getPasteTypes(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object,java.awt.datatransfer.Transferable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void setName(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException

CLSS public abstract interface org.netbeans.spi.viewmodel.Model

CLSS public abstract interface org.netbeans.spi.viewmodel.NodeActionsProviderFilter
intf org.netbeans.spi.viewmodel.Model
meth public abstract javax.swing.Action[] getActions(org.netbeans.spi.viewmodel.NodeActionsProvider,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void performDefaultAction(org.netbeans.spi.viewmodel.NodeActionsProvider,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException

CLSS public abstract interface org.netbeans.spi.viewmodel.NodeModelFilter
intf org.netbeans.spi.viewmodel.Model
meth public abstract java.lang.String getDisplayName(org.netbeans.spi.viewmodel.NodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String getIconBase(org.netbeans.spi.viewmodel.NodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String getShortDescription(org.netbeans.spi.viewmodel.NodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void removeModelListener(org.netbeans.spi.viewmodel.ModelListener)

CLSS public abstract interface org.netbeans.spi.viewmodel.TableModelFilter
intf org.netbeans.spi.viewmodel.Model
meth public abstract boolean isReadOnly(org.netbeans.spi.viewmodel.TableModel,java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.Object getValueAt(org.netbeans.spi.viewmodel.TableModel,java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void removeModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void setValueAt(org.netbeans.spi.viewmodel.TableModel,java.lang.Object,java.lang.String,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException

CLSS public abstract interface org.netbeans.spi.viewmodel.TreeModelFilter
intf org.netbeans.spi.viewmodel.Model
meth public abstract boolean isLeaf(org.netbeans.spi.viewmodel.TreeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract int getChildrenCount(org.netbeans.spi.viewmodel.TreeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.Object getRoot(org.netbeans.spi.viewmodel.TreeModel)
meth public abstract java.lang.Object[] getChildren(org.netbeans.spi.viewmodel.TreeModel,java.lang.Object,int,int) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void removeModelListener(org.netbeans.spi.viewmodel.ModelListener)

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

