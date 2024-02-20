#Signature file v4.1
#Version 1.146

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

CLSS public abstract interface javax.swing.ListCellRenderer<%0 extends java.lang.Object>
meth public abstract java.awt.Component getListCellRendererComponent(javax.swing.JList<? extends {javax.swing.ListCellRenderer%0}>,{javax.swing.ListCellRenderer%0},int,boolean,boolean)

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

CLSS public abstract interface javax.swing.event.CellEditorListener
intf java.util.EventListener
meth public abstract void editingCanceled(javax.swing.event.ChangeEvent)
meth public abstract void editingStopped(javax.swing.event.ChangeEvent)

CLSS public abstract interface javax.swing.event.ListSelectionListener
intf java.util.EventListener
meth public abstract void valueChanged(javax.swing.event.ListSelectionEvent)

CLSS public final org.netbeans.modules.java.api.common.ModuleRoots
fld public final static java.lang.String DEFAULT_MODULE_LABEL
fld public final static java.lang.String DEFAULT_TEST_MODULE_LABEL
meth public java.lang.String createInitialDisplayName(java.io.File)
meth public java.lang.String createInitialPath()
meth public java.lang.String getRootDisplayName(java.lang.String,java.lang.String)
meth public java.lang.String getRootPath(java.lang.String)
meth public java.lang.String[] getRootPathProperties()
meth public java.net.URL[] getRootURLs(boolean)
meth public static org.netbeans.modules.java.api.common.ModuleRoots create(org.netbeans.modules.java.api.common.ant.UpdateHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.spi.project.support.ant.ReferenceHelper,java.lang.String,java.lang.String,boolean,java.lang.String)
meth public void putModuleRoots(java.net.URL[],java.lang.String[])
supr org.netbeans.modules.java.api.common.SourceRoots
hfds DEFAULT_MODULE_PATH,DEFAULT_PATH_TEMPLATE,DEFAULT_TEST_MODULE_PATH

CLSS public abstract org.netbeans.modules.java.api.common.Roots
meth public !varargs static org.netbeans.modules.java.api.common.Roots nonSourceRoots(java.lang.String[])
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String[] getRootDisplayNames()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String[] getRootProperties()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.java.api.common.Roots propertyBased(java.lang.String[],java.lang.String[],boolean,java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds hint,isSourceRoot,support,supportIncludes,type
hcls MyAccessor,NonSourceRoots,PropSourceRoots

CLSS public org.netbeans.modules.java.api.common.SourceRoots
fld public final static java.lang.String DEFAULT_SOURCE_LABEL
fld public final static java.lang.String DEFAULT_TEST_LABEL
fld public final static java.lang.String PROP_ROOTS
fld public final static java.lang.String PROP_ROOT_PROPERTIES
meth public boolean isTest()
meth public java.lang.String createInitialDisplayName(java.io.File)
meth public java.lang.String getRootDisplayName(java.lang.String,java.lang.String)
meth public java.lang.String[] getRootDisplayNames()
meth public java.lang.String[] getRootNames()
meth public java.lang.String[] getRootProperties()
meth public java.net.URL[] getRootURLs()
meth public java.net.URL[] getRootURLs(boolean)
meth public org.openide.filesystems.FileObject[] getRoots()
meth public static org.netbeans.modules.java.api.common.SourceRoots create(org.netbeans.modules.java.api.common.ant.UpdateHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.spi.project.support.ant.ReferenceHelper,java.lang.String,java.lang.String,boolean,java.lang.String)
meth public void putRoots(java.net.URL[],java.lang.String[])
supr org.netbeans.modules.java.api.common.Roots
hfds LOG,REF_PREFIX,elementName,evaluator,helper,isTest,listener,newRootNameTemplate,projectConfigurationNamespace,projectDir,refHelper,sourceRootNames,sourceRootPathProperties,sourceRootProperties,sourceRootURLs,sourceRoots
hcls ProjectMetadataListener,RootInfo

CLSS public abstract interface org.netbeans.modules.java.api.common.ant.PackageModifierImplementation
meth public abstract void exportPackageAction(java.util.Collection<java.lang.String>,boolean)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public final org.netbeans.modules.java.api.common.ant.UpdateHelper
cons public init(org.netbeans.modules.java.api.common.ant.UpdateImplementation,org.netbeans.spi.project.support.ant.AntProjectHelper)
meth public boolean isCurrent()
meth public boolean requestUpdate() throws java.io.IOException
meth public org.netbeans.spi.project.support.ant.AntProjectHelper getAntProjectHelper()
meth public org.netbeans.spi.project.support.ant.EditableProperties getProperties(java.lang.String)
meth public org.w3c.dom.Element getPrimaryConfigurationData(boolean)
meth public void putPrimaryConfigurationData(org.w3c.dom.Element,boolean)
meth public void putProperties(java.lang.String,org.netbeans.spi.project.support.ant.EditableProperties)
supr java.lang.Object
hfds helper,updateProject

CLSS public abstract interface org.netbeans.modules.java.api.common.ant.UpdateImplementation
meth public abstract boolean canUpdate()
meth public abstract boolean isCurrent()
meth public abstract org.netbeans.spi.project.support.ant.EditableProperties getUpdatedProjectProperties()
meth public abstract org.w3c.dom.Element getUpdatedSharedConfigurationData()
meth public abstract void saveUpdate(org.netbeans.spi.project.support.ant.EditableProperties) throws java.io.IOException

CLSS public org.netbeans.modules.java.api.common.applet.AppletSupport
fld public static java.lang.Boolean unitTestingSupport_isApplet
meth public static boolean isApplet(org.openide.filesystems.FileObject)
meth public static java.net.URL generateHtmlFileURL(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,java.lang.String)
meth public static java.net.URL getHTMLPageURL(org.openide.filesystems.FileObject,java.lang.String)
meth public static org.openide.filesystems.FileObject generateSecurityPolicy(org.openide.filesystems.FileObject)
supr java.lang.Object
hfds CLASS_EXT,HTML_EXT,JDK_15,POLICY_FILE_EXT,POLICY_FILE_NAME

CLSS public abstract org.netbeans.modules.java.api.common.classpath.AbstractClassPathProvider
cons protected init()
innr public abstract interface static ClassPathsChangeListener
innr public final static ClassPathsChangeEvent
intf org.netbeans.spi.java.classpath.ClassPathProvider
meth protected final void fireClassPathsChange(java.util.Collection<? extends java.lang.String>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String[] getPropertyName(org.netbeans.api.project.SourceGroup,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.api.java.classpath.ClassPath[] getProjectClassPaths(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public final void addClassPathsChangeListener(org.netbeans.modules.java.api.common.classpath.AbstractClassPathProvider$ClassPathsChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public final void removeClassPathsChangeListener(org.netbeans.modules.java.api.common.classpath.AbstractClassPathProvider$ClassPathsChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds listeners

CLSS public final static org.netbeans.modules.java.api.common.classpath.AbstractClassPathProvider$ClassPathsChangeEvent
 outer org.netbeans.modules.java.api.common.classpath.AbstractClassPathProvider
meth public java.util.Collection<? extends java.lang.String> getChangedClassPathTypes()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.util.EventObject
hfds classPathTypes

CLSS public abstract interface static org.netbeans.modules.java.api.common.classpath.AbstractClassPathProvider$ClassPathsChangeListener
 outer org.netbeans.modules.java.api.common.classpath.AbstractClassPathProvider
intf java.util.EventListener
meth public abstract void classPathsChange(org.netbeans.modules.java.api.common.classpath.AbstractClassPathProvider$ClassPathsChangeEvent)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public final org.netbeans.modules.java.api.common.classpath.ClassPathExtender
 anno 0 java.lang.Deprecated()
cons public init(org.netbeans.modules.java.api.common.classpath.ClassPathModifier,java.lang.String,java.lang.String)
intf org.netbeans.spi.java.project.classpath.ProjectClassPathExtender
meth public boolean addAntArtifact(org.netbeans.api.project.ant.AntArtifact,java.net.URI) throws java.io.IOException
meth public boolean addAntArtifacts(java.lang.String,org.netbeans.api.project.ant.AntArtifact[],java.net.URI[],java.lang.String) throws java.io.IOException
meth public boolean addArchiveFile(org.openide.filesystems.FileObject) throws java.io.IOException
meth public boolean addArchiveFiles(java.lang.String,org.openide.filesystems.FileObject[],java.lang.String) throws java.io.IOException
meth public boolean addLibraries(java.lang.String,org.netbeans.api.project.libraries.Library[],java.lang.String) throws java.io.IOException
meth public boolean addLibrary(org.netbeans.api.project.libraries.Library) throws java.io.IOException
supr java.lang.Object
hfds classPathProperty,delegate,elementName

CLSS public final org.netbeans.modules.java.api.common.classpath.ClassPathModifier
cons public init(org.netbeans.api.project.Project,org.netbeans.modules.java.api.common.ant.UpdateHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.spi.project.support.ant.ReferenceHelper,org.netbeans.modules.java.api.common.classpath.ClassPathSupport$Callback,org.netbeans.modules.java.api.common.classpath.ClassPathModifier$Callback,org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport$Callback)
fld public final static int ADD = 1
fld public final static int ADD_NO_HEURISTICS = 3
fld public final static int REMOVE = 2
innr public abstract interface static Callback
meth public boolean addAntArtifacts(org.netbeans.api.project.ant.AntArtifact[],java.net.URI[],org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth public boolean addLibraries(org.netbeans.api.project.libraries.Library[],org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth public boolean addRoots(java.net.URI[],org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth public boolean addRoots(java.net.URI[],org.netbeans.api.project.SourceGroup,java.lang.String,int) throws java.io.IOException
meth public boolean addRoots(java.net.URL[],java.lang.String) throws java.io.IOException
meth public boolean addRoots(java.net.URL[],org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth public boolean removeAntArtifacts(org.netbeans.api.project.ant.AntArtifact[],java.net.URI[],org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth public boolean removeLibraries(org.netbeans.api.project.libraries.Library[],org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth public boolean removeRoots(java.net.URI[],org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth public boolean removeRoots(java.net.URL[],org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth public java.lang.String[] getExtensibleClassPathTypes(org.netbeans.api.project.SourceGroup)
meth public org.netbeans.api.project.SourceGroup[] getExtensibleSourceGroups()
meth public org.netbeans.modules.java.api.common.classpath.ClassPathSupport getClassPathSupport()
supr org.netbeans.spi.java.project.classpath.ProjectClassPathModifierImplementation
hfds LOG,cpModifierCallback,cpUiSupportCallback,cs,eval,project,refHelper,updateHelper

CLSS public abstract interface static org.netbeans.modules.java.api.common.classpath.ClassPathModifier$Callback
 outer org.netbeans.modules.java.api.common.classpath.ClassPathModifier
meth public abstract java.lang.String getClassPathProperty(org.netbeans.api.project.SourceGroup,java.lang.String)
meth public abstract java.lang.String getElementName(java.lang.String)

CLSS public org.netbeans.modules.java.api.common.classpath.ClassPathModifierSupport
cons public init()
fld public final static int ADD = 1
fld public final static int ADD_NO_HEURISTICS = 3
fld public final static int REMOVE = 2
meth public static boolean handleAntArtifacts(org.netbeans.api.project.Project,org.netbeans.modules.java.api.common.ant.UpdateHelper,org.netbeans.modules.java.api.common.classpath.ClassPathSupport,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.spi.project.support.ant.ReferenceHelper,org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport$Callback,org.netbeans.api.project.ant.AntArtifact[],java.net.URI[],java.lang.String,java.lang.String,int) throws java.io.IOException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 10 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NullAllowed()
 anno 7 org.netbeans.api.annotations.common.NonNull()
 anno 8 org.netbeans.api.annotations.common.NonNull()
 anno 9 org.netbeans.api.annotations.common.NonNull()
meth public static boolean handleAntArtifacts(org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.modules.java.api.common.classpath.ClassPathSupport,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport$Callback,org.netbeans.api.project.ant.AntArtifact[],java.net.URI[],java.lang.String,java.lang.String,int) throws java.io.IOException
 anno 0 java.lang.Deprecated()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
 anno 6 org.netbeans.api.annotations.common.NonNull()
 anno 7 org.netbeans.api.annotations.common.NonNull()
 anno 8 org.netbeans.api.annotations.common.NonNull()
 anno 9 org.netbeans.api.annotations.common.NullAllowed()
meth public static boolean handleLibraries(org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.modules.java.api.common.classpath.ClassPathSupport,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport$Callback,org.netbeans.spi.project.support.ant.ReferenceHelper,org.netbeans.api.project.libraries.Library[],java.lang.String,java.lang.String,int) throws java.io.IOException
meth public static boolean handleLibraryClassPathItems(org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.modules.java.api.common.classpath.ClassPathSupport,java.util.List<org.netbeans.modules.java.api.common.classpath.ClassPathSupport$Item>,java.lang.String,java.lang.String,int,boolean) throws java.io.IOException
meth public static boolean handleRoots(org.netbeans.api.project.Project,org.netbeans.modules.java.api.common.ant.UpdateHelper,org.netbeans.modules.java.api.common.classpath.ClassPathSupport,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.spi.project.support.ant.ReferenceHelper,org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport$Callback,java.net.URI[],java.lang.String,java.lang.String,int) throws java.io.IOException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NullAllowed()
 anno 7 org.netbeans.api.annotations.common.NonNull()
 anno 8 org.netbeans.api.annotations.common.NonNull()
 anno 9 org.netbeans.api.annotations.common.NullAllowed()
meth public static boolean handleRoots(org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.modules.java.api.common.classpath.ClassPathSupport,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport$Callback,java.net.URI[],java.lang.String,java.lang.String,int) throws java.io.IOException
 anno 0 java.lang.Deprecated()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
 anno 6 org.netbeans.api.annotations.common.NonNull()
 anno 7 org.netbeans.api.annotations.common.NonNull()
 anno 8 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds LOG
hcls Accessor,ClassPathPackageAccessorImpl

CLSS public final org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl
cons public init(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots)
cons public init(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots,java.lang.String,java.lang.String,java.lang.String,java.lang.String[],java.lang.String[],java.lang.String[],java.lang.String[])
cons public init(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots,java.lang.String,java.lang.String,java.lang.String,java.lang.String[],java.lang.String[],java.lang.String[],java.lang.String[],java.lang.String[])
cons public init(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots,java.lang.String,java.lang.String,java.lang.String,java.lang.String[],java.lang.String[],java.lang.String[],java.lang.String[],java.lang.String[],java.lang.String[])
innr public final static Builder
meth public java.lang.String[] getPropertyName(org.netbeans.api.project.SourceGroup,java.lang.String)
meth public java.lang.String[] getPropertyName(org.netbeans.modules.java.api.common.SourceRoots,java.lang.String)
meth public org.netbeans.api.java.classpath.ClassPath findClassPath(org.openide.filesystems.FileObject,java.lang.String)
meth public org.netbeans.api.java.classpath.ClassPath getProjectSourcesClassPath(java.lang.String)
meth public org.netbeans.api.java.classpath.ClassPath[] getProjectClassPaths(java.lang.String)
supr org.netbeans.modules.java.api.common.classpath.AbstractClassPathProvider
hfds buildClassesDir,buildGeneratedDir,buildTestClassesDir,cache,dirCache,distJar,endorsedClasspath,evaluator,helper,javacClasspath,javacSource,javacTestClasspath,moduleExecutePath,modulePath,platform,processorClasspath,processorModulePath,processorTestClasspath,processorTestModulepath,project,projectDirectory,runClasspath,runTestClasspath,sourceRoots,testModuleExecutePath,testModulePath,testSourceRoots
hcls Filter,SourceLevelSelector

CLSS public final static org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl$Builder
 outer org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl
meth public !varargs org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl$Builder setBootClasspathProperties(java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl build()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl$Builder setBuildClassesDirProperty(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl$Builder setBuildTestClassesDirProperty(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl$Builder setDistJarProperty(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl$Builder setEndorsedClasspathProperties(java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl$Builder setJavacClassPathProperties(java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl$Builder setJavacSourceProperty(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl$Builder setJavacTestClasspathProperties(java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl$Builder setModulepathProperties(java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl$Builder setPlatformType(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl$Builder setProcessorModulepathProperties(java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl$Builder setProcessorPathProperties(java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl$Builder setProject(org.netbeans.api.project.Project)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl$Builder setRunClasspathProperties(java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl$Builder setRunModulepathProperties(java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl$Builder setRunTestClasspathProperties(java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl$Builder setRunTestModulepathProperties(java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl$Builder setTestModulepathProperties(java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl$Builder create(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds DEFAULT_BUILD_CLASSES_DIR,DEFAULT_BUILD_TEST_CLASSES_DIR,DEFAULT_DIST_JAR,DEFAULT_ENDORSED_CLASSPATH,DEFAULT_JAVAC_CLASS_PATH,DEFAULT_JAVAC_SOURCE,DEFAULT_JAVAC_TEST_CLASS_PATH,DEFAULT_MODULE_EXECUTE_PATH,DEFAULT_MODULE_PATH,DEFAULT_PROCESSOR_MODULE_PATH,DEFAULT_PROCESSOR_PATH,DEFAULT_RUN_CLASS_PATH,DEFAULT_RUN_TEST_CLASS_PATH,DEFAULT_TEST_MODULE_EXECUTE_PATH,DEFAULT_TEST_MODULE_PATH,bootClasspathProperties,buildClassesDir,buildTestClassesDir,distJar,endorsedClasspath,evaluator,helper,javacClasspath,javacSource,javacTestClasspath,moduleExecutePath,modulePath,platformType,processorModulePath,processorPath,project,runClasspath,runTestClasspath,sourceRoots,testModuleExecutePath,testModulePath,testSourceRoots

CLSS public final org.netbeans.modules.java.api.common.classpath.ClassPathSupport
cons public init(org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.spi.project.support.ant.ReferenceHelper,org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.modules.java.api.common.ant.UpdateHelper,org.netbeans.modules.java.api.common.classpath.ClassPathSupport$Callback)
fld public final static java.lang.String ENDORSED = "classpath/endorsed"
innr public abstract interface static Callback
innr public static Item
meth public java.lang.String getLibraryReference(org.netbeans.modules.java.api.common.classpath.ClassPathSupport$Item)
meth public java.lang.String getLibraryReference(org.netbeans.modules.java.api.common.classpath.ClassPathSupport$Item,java.lang.String)
meth public java.lang.String[] encodeToStrings(java.util.List<org.netbeans.modules.java.api.common.classpath.ClassPathSupport$Item>)
meth public java.lang.String[] encodeToStrings(java.util.List<org.netbeans.modules.java.api.common.classpath.ClassPathSupport$Item>,java.lang.String)
meth public java.lang.String[] encodeToStrings(java.util.List<org.netbeans.modules.java.api.common.classpath.ClassPathSupport$Item>,java.lang.String,java.lang.String)
meth public java.util.Iterator<org.netbeans.modules.java.api.common.classpath.ClassPathSupport$Item> itemsIterator(java.lang.String)
meth public java.util.Iterator<org.netbeans.modules.java.api.common.classpath.ClassPathSupport$Item> itemsIterator(java.lang.String,java.lang.String)
meth public java.util.List<org.netbeans.modules.java.api.common.classpath.ClassPathSupport$Item> itemsList(java.lang.String)
meth public java.util.List<org.netbeans.modules.java.api.common.classpath.ClassPathSupport$Item> itemsList(java.lang.String,java.lang.String)
meth public static boolean isVariableBasedReference(java.lang.String)
meth public static java.lang.String getLibraryNameFromReference(java.lang.String)
supr java.lang.Object
hfds ANT_ARTIFACT_PREFIX,LIBRARY_PREFIX,LIBRARY_SUFFIX,antArtifactPrefix,antProjectHelper,callback,evaluator,referenceHelper,updateHelper,wellKnownPaths
hcls RelativePath

CLSS public abstract interface static org.netbeans.modules.java.api.common.classpath.ClassPathSupport$Callback
 outer org.netbeans.modules.java.api.common.classpath.ClassPathSupport
meth public abstract void readAdditionalProperties(java.util.List<org.netbeans.modules.java.api.common.classpath.ClassPathSupport$Item>,java.lang.String)
meth public abstract void storeAdditionalProperties(java.util.List<org.netbeans.modules.java.api.common.classpath.ClassPathSupport$Item>,java.lang.String)

CLSS public static org.netbeans.modules.java.api.common.classpath.ClassPathSupport$Item
 outer org.netbeans.modules.java.api.common.classpath.ClassPathSupport
fld public final static int TYPE_ARTIFACT = 2
fld public final static int TYPE_CLASSPATH = 3
fld public final static int TYPE_JAR = 0
fld public final static int TYPE_LIBRARY = 1
meth public boolean canEdit()
meth public boolean equals(java.lang.Object)
meth public boolean isBroken()
meth public int getType()
meth public int hashCode()
meth public java.io.File getResolvedFile()
meth public java.lang.String getAdditionalProperty(java.lang.String)
meth public java.lang.String getFilePath()
meth public java.lang.String getJavadocFilePath()
meth public java.lang.String getReference()
meth public java.lang.String getSourceFilePath()
meth public java.lang.String getVariableBasedProperty()
meth public java.lang.String toString()
meth public java.net.URI getArtifactURI()
meth public org.netbeans.api.project.ant.AntArtifact getArtifact()
meth public org.netbeans.api.project.libraries.Library getLibrary()
meth public static org.netbeans.modules.java.api.common.classpath.ClassPathSupport$Item create(java.lang.String)
meth public static org.netbeans.modules.java.api.common.classpath.ClassPathSupport$Item create(java.lang.String,java.io.File,java.lang.String,java.lang.String)
meth public static org.netbeans.modules.java.api.common.classpath.ClassPathSupport$Item create(org.netbeans.api.project.ant.AntArtifact,java.net.URI,java.lang.String)
meth public static org.netbeans.modules.java.api.common.classpath.ClassPathSupport$Item create(org.netbeans.api.project.libraries.Library,java.lang.String)
meth public static org.netbeans.modules.java.api.common.classpath.ClassPathSupport$Item createBroken(int,java.lang.String)
meth public static org.netbeans.modules.java.api.common.classpath.ClassPathSupport$Item createBroken(java.lang.String,java.io.File,java.lang.String)
meth public void initSourceAndJavadoc(org.netbeans.spi.project.support.ant.AntProjectHelper)
meth public void reassignLibraryManager(org.netbeans.api.project.libraries.LibraryManager)
meth public void removeSourceAndJavadoc(org.netbeans.modules.java.api.common.ant.UpdateHelper)
meth public void saveSourceAndJavadoc(org.netbeans.spi.project.support.ant.ReferenceHelper,org.netbeans.modules.java.api.common.ant.UpdateHelper)
meth public void setAdditionalProperty(java.lang.String,java.lang.String)
meth public void setJavadocFilePath(java.lang.String)
meth public void setReference(java.lang.String)
meth public void setSourceFilePath(java.lang.String)
meth public void updateJarReference(org.netbeans.spi.project.support.ant.AntProjectHelper)
supr java.lang.Object
hfds JAVADOC_START,REF_START,REF_START_INDEX,SOURCE_START,additionalProperties,artifactURI,broken,initialJavadocFilePath,initialSourceFilePath,javadocFilePath,libraryName,object,property,sourceFilePath,type,variableBasedProperty

CLSS public final org.netbeans.modules.java.api.common.classpath.ClassPathSupportFactory
meth public static org.netbeans.spi.java.classpath.ClassPathImplementation createBootClassPathImplementation(org.netbeans.spi.project.support.ant.PropertyEvaluator)
meth public static org.netbeans.spi.java.classpath.ClassPathImplementation createBootClassPathImplementation(org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.api.java.classpath.ClassPath)
meth public static org.netbeans.spi.java.classpath.ClassPathImplementation createBootClassPathImplementation(org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.api.java.classpath.ClassPath,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.spi.java.classpath.ClassPathImplementation createBootClassPathImplementation(org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.api.project.Project,org.netbeans.api.java.classpath.ClassPath,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.spi.java.classpath.ClassPathImplementation createModuleInfoBasedPath(org.netbeans.api.java.classpath.ClassPath,org.netbeans.api.java.classpath.ClassPath,org.netbeans.api.java.classpath.ClassPath,org.netbeans.api.java.classpath.ClassPath,org.netbeans.api.java.classpath.ClassPath,java.util.function.Function<java.net.URL,java.lang.Boolean>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
 anno 6 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.spi.java.classpath.ClassPathImplementation createSourcePathImplementation(org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator)
supr java.lang.Object

CLSS public final org.netbeans.modules.java.api.common.classpath.MultiModuleClassPathProvider
innr public final static Builder
meth public java.lang.String[] getPropertyName(org.netbeans.api.project.SourceGroup,java.lang.String)
meth public org.netbeans.api.java.classpath.ClassPath findClassPath(org.openide.filesystems.FileObject,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.java.classpath.ClassPath getProjectSourcesClassPath(java.lang.String)
meth public org.netbeans.api.java.classpath.ClassPath[] getProjectClassPaths(java.lang.String)
supr org.netbeans.modules.java.api.common.classpath.AbstractClassPathProvider
hfds INTERNAL_MODULE_BINARIES_PATH,LOG,buildModulesDirProperty,dirCache,eval,executeClassPath,executeModulePath,helper,javacClassPath,modSensitivePrjPathFcts,modulePath,platformType,processorClassPath,processorModulePath,projectDirectory,sourceCache,testCache,testExecuteClassPath,testExecuteModulePath,testJavacClassPath,testModulePath,testProcessorClassPath,testProcessorModulePath,urlCache
hcls Cache,Filter,Location,Owner,TranslateBuildModules

CLSS public final static org.netbeans.modules.java.api.common.classpath.MultiModuleClassPathProvider$Builder
 outer org.netbeans.modules.java.api.common.classpath.MultiModuleClassPathProvider
meth public org.netbeans.modules.java.api.common.classpath.MultiModuleClassPathProvider build()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.classpath.MultiModuleClassPathProvider$Builder setBuildModulesDirProperty(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.classpath.MultiModuleClassPathProvider$Builder setJavacClasspathProperties(java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.classpath.MultiModuleClassPathProvider$Builder setModulepathProperties(java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.classpath.MultiModuleClassPathProvider$Builder setPlatformType(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.classpath.MultiModuleClassPathProvider$Builder setProcessorClasspathProperties(java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.classpath.MultiModuleClassPathProvider$Builder setProcessorModulepathProperties(java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.classpath.MultiModuleClassPathProvider$Builder setRunClasspathProperties(java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.classpath.MultiModuleClassPathProvider$Builder setRunModulepathProperties(java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.classpath.MultiModuleClassPathProvider$Builder setTestJavacClasspathProperties(java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.classpath.MultiModuleClassPathProvider$Builder setTestModulepathProperties(java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.classpath.MultiModuleClassPathProvider$Builder setTestProcessorClasspathProperties(java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.classpath.MultiModuleClassPathProvider$Builder setTestProcessorModulepathProperties(java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.classpath.MultiModuleClassPathProvider$Builder setTestRunClasspathProperties(java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.classpath.MultiModuleClassPathProvider$Builder setTestRunModulepathProperties(java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.java.api.common.classpath.MultiModuleClassPathProvider$Builder newInstance(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds buildModulesDirProperty,eval,executeClassPath,executeModulePath,helper,javacClassPath,modulePath,modules,platformType,processorClassPath,processorModulePath,testExecuteClassPath,testExecuteModulePath,testJavacClassPath,testModulePath,testModules,testProcessorClassPath,testProcessorModulePath

CLSS public final org.netbeans.modules.java.api.common.problems.ProjectProblemsProviders
meth public static org.netbeans.spi.project.ui.ProjectProblemsProvider createMissingModuleProjectProblemsProvider(org.netbeans.api.project.Project)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public abstract org.netbeans.modules.java.api.common.project.BaseActionProvider
cons public init(org.netbeans.api.project.Project,org.netbeans.modules.java.api.common.ant.UpdateHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.modules.java.api.common.project.BaseActionProvider$Callback)
fld public final static java.lang.String AUTOMATIC_BUILD_TAG = ".netbeans_automatic_build"
fld public final static java.lang.String BUILD_SCRIPT = "buildfile"
fld public final static java.lang.String PROPERTY_RUN_SINGLE_ON_SERVER = "run.single.on.server"
innr public abstract interface static Callback
innr public abstract interface static Callback2
innr public abstract interface static Callback3
innr public final static CallbackImpl
intf org.netbeans.spi.project.ActionProvider
meth protected abstract boolean isCompileOnSaveEnabled()
meth protected abstract java.lang.String[] getActionsDisabledForQuickRun()
meth protected abstract java.lang.String[] getPlatformSensitiveActions()
meth protected abstract java.util.Set<java.lang.String> getJavaModelActions()
meth protected abstract java.util.Set<java.lang.String> getScanSensitiveActions()
meth protected boolean handleJavaClass(java.util.Properties,org.openide.filesystems.FileObject,java.lang.String,java.util.List<java.lang.String>)
meth protected boolean isCompileOnSaveUpdate()
meth protected boolean isServerExecution()
meth protected boolean showMainClassSelector()
meth protected final org.netbeans.api.project.Project getProject()
meth protected final org.openide.filesystems.FileObject findBuildXml()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth protected java.lang.String getProjectMainClass(boolean)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth protected org.netbeans.api.java.platform.JavaPlatform getProjectPlatform()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth protected org.netbeans.modules.java.api.common.ant.UpdateHelper getUpdateHelper()
meth protected org.netbeans.modules.java.api.common.project.BaseActionProvider$Callback getCallback()
meth protected org.netbeans.spi.project.support.ant.AntProjectHelper getAntProjectHelper()
meth protected org.netbeans.spi.project.support.ant.PropertyEvaluator getEvaluator()
meth protected void setServerExecution(boolean)
meth protected void updateJavaRunnerClasspath(java.lang.String,java.util.Map<java.lang.String,java.lang.Object>)
meth public abstract java.util.Map<java.lang.String,java.lang.String[]> getCommands()
meth public boolean isActionEnabled(java.lang.String,org.openide.util.Lookup)
meth public java.lang.String[] getTargetNames(java.lang.String,org.openide.util.Lookup,java.util.Properties)
meth public java.lang.String[] getTargetNames(java.lang.String,org.openide.util.Lookup,java.util.Properties,boolean)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static java.lang.String getBuildXmlName(org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.PropertyEvaluator)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.openide.filesystems.FileObject getBuildXml(org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.PropertyEvaluator)
meth public void invokeAction(java.lang.String,org.openide.util.Lookup)
meth public void startFSListener()
supr java.lang.Object
hfds LOG,antProjectHelper,buildXMLName,callback,classpaths,delegate,evaluator,listeners,project,projectSourceRoots,projectTestRoots,serverExecution,updateHelper,userPropertiesPolicy
hcls BrokenAPIActionDecorator,CustomRunner,EventAdaptor,ServerExecutionAwareAction

CLSS public abstract interface static org.netbeans.modules.java.api.common.project.BaseActionProvider$Callback
 outer org.netbeans.modules.java.api.common.project.BaseActionProvider
meth public abstract org.netbeans.api.java.classpath.ClassPath findClassPath(org.openide.filesystems.FileObject,java.lang.String)
meth public abstract org.netbeans.api.java.classpath.ClassPath getProjectSourcesClassPath(java.lang.String)

CLSS public abstract interface static org.netbeans.modules.java.api.common.project.BaseActionProvider$Callback2
 outer org.netbeans.modules.java.api.common.project.BaseActionProvider
intf org.netbeans.modules.java.api.common.project.BaseActionProvider$Callback
meth public abstract void antTargetInvocationFailed(java.lang.String,org.openide.util.Lookup)
meth public abstract void antTargetInvocationFinished(java.lang.String,org.openide.util.Lookup,int)
meth public abstract void antTargetInvocationStarted(java.lang.String,org.openide.util.Lookup)

CLSS public abstract interface static org.netbeans.modules.java.api.common.project.BaseActionProvider$Callback3
 outer org.netbeans.modules.java.api.common.project.BaseActionProvider
intf org.netbeans.modules.java.api.common.project.BaseActionProvider$Callback2
meth public abstract java.util.Map<java.lang.String,java.lang.String> createAdditionalProperties(java.lang.String,org.openide.util.Lookup)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.Set<java.lang.String> createConcealedProperties(java.lang.String,org.openide.util.Lookup)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()

CLSS public final static org.netbeans.modules.java.api.common.project.BaseActionProvider$CallbackImpl
 outer org.netbeans.modules.java.api.common.project.BaseActionProvider
cons public init(org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl)
intf org.netbeans.modules.java.api.common.project.BaseActionProvider$Callback
meth public org.netbeans.api.java.classpath.ClassPath findClassPath(org.openide.filesystems.FileObject,java.lang.String)
meth public org.netbeans.api.java.classpath.ClassPath getProjectSourcesClassPath(java.lang.String)
supr java.lang.Object
hfds cp

CLSS public final org.netbeans.modules.java.api.common.project.JavaActionProvider
innr public abstract interface static Action
innr public abstract interface static AntTargetInvocationListener
innr public abstract static ScriptAction
innr public final static !enum CompileOnSaveOperation
innr public final static Builder
innr public final static Context
intf org.netbeans.spi.project.ActionProvider
meth public boolean isActionEnabled(java.lang.String,org.openide.util.Lookup)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String[] getSupportedActions()
meth public void addAntTargetInvocationListener(org.netbeans.modules.java.api.common.project.JavaActionProvider$AntTargetInvocationListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void invokeAction(java.lang.String,org.openide.util.Lookup)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public void removeAntTargetInvocationListener(org.netbeans.modules.java.api.common.project.JavaActionProvider$AntTargetInvocationListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds EMPTY,LOG,additionalPropertiesProvider,classpaths,concealedPropertiesProvider,cosOpsProvider,eval,jpp,listeners,prj,supportedActions,unitTestingSupport_fixClasses,updateHelper,userPropertiesPolicy
hcls BaseRunSingleAction,BaseScriptAction,SimpleAction

CLSS public abstract interface static org.netbeans.modules.java.api.common.project.JavaActionProvider$Action
 outer org.netbeans.modules.java.api.common.project.JavaActionProvider
meth public abstract boolean isEnabled(org.netbeans.modules.java.api.common.project.JavaActionProvider$Context)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getCommand()
meth public abstract void invoke(org.netbeans.modules.java.api.common.project.JavaActionProvider$Context)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface static org.netbeans.modules.java.api.common.project.JavaActionProvider$AntTargetInvocationListener
 outer org.netbeans.modules.java.api.common.project.JavaActionProvider
intf java.util.EventListener
meth public abstract void antTargetInvocationFailed(java.lang.String,org.openide.util.Lookup)
meth public abstract void antTargetInvocationFinished(java.lang.String,org.openide.util.Lookup,int)
meth public abstract void antTargetInvocationStarted(java.lang.String,org.openide.util.Lookup)

CLSS public final static org.netbeans.modules.java.api.common.project.JavaActionProvider$Builder
 outer org.netbeans.modules.java.api.common.project.JavaActionProvider
innr public abstract interface static CustomFileExecutor
meth public !varargs org.netbeans.modules.java.api.common.project.JavaActionProvider$ScriptAction createDefaultScriptAction(java.lang.String,boolean,boolean,boolean,java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
meth public !varargs org.netbeans.modules.java.api.common.project.JavaActionProvider$ScriptAction createSimpleScriptAction(java.lang.String,boolean,boolean,boolean,boolean,java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.JavaActionProvider build()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.JavaActionProvider$Action createProjectOperation(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.JavaActionProvider$Builder addAction(org.netbeans.modules.java.api.common.project.JavaActionProvider$Action)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.JavaActionProvider$Builder setActivePlatformProperty(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.JavaActionProvider$Builder setActivePlatformProvider(java.util.function.Supplier<? extends org.netbeans.api.java.platform.JavaPlatform>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.JavaActionProvider$Builder setAdditionalPropertiesProvider(java.util.function.BiFunction<java.lang.String,org.openide.util.Lookup,java.util.Map<java.lang.String,java.lang.String>>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.JavaActionProvider$Builder setCompileOnSaveOperationsProvider(java.util.function.Supplier<? extends java.util.Set<? extends org.netbeans.modules.java.api.common.project.JavaActionProvider$CompileOnSaveOperation>>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.JavaActionProvider$Builder setConcealedPropertiesProvider(java.util.function.BiFunction<java.lang.String,org.openide.util.Lookup,java.util.Set<java.lang.String>>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.JavaActionProvider$Builder setProjectMainClassProvider(java.util.function.Function<java.lang.Boolean,java.lang.String>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.JavaActionProvider$Builder setProjectMainClassSelector(java.util.function.Supplier<java.lang.Boolean>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.JavaActionProvider$ScriptAction createDefaultScriptAction(java.lang.String,boolean,boolean,boolean,java.util.function.Supplier<? extends java.lang.String[]>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.JavaActionProvider$ScriptAction createDefaultScriptAction(java.lang.String,boolean,boolean,boolean,java.util.function.Supplier<? extends java.lang.String[]>,org.netbeans.modules.java.api.common.project.JavaActionProvider$Builder$CustomFileExecutor)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.modules.java.api.common.project.JavaActionProvider$ScriptAction createSimpleScriptAction(java.lang.String,boolean,boolean,boolean,boolean,java.util.function.Predicate<org.netbeans.modules.java.api.common.project.JavaActionProvider$Context>,java.util.function.Supplier<? extends java.lang.String[]>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NonNull()
 anno 7 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.java.api.common.project.JavaActionProvider$Builder newInstance(org.netbeans.api.project.Project,org.netbeans.modules.java.api.common.ant.UpdateHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots,java.util.function.Function<java.lang.String,org.netbeans.api.java.classpath.ClassPath>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds actions,additionalPropertiesProvider,classpaths,concealedPropertiesProvider,cosOpsProvider,evaluator,jpp,mainClassServices,mfs,project,sourceRoots,testRoots,updateHelper

CLSS public abstract interface static org.netbeans.modules.java.api.common.project.JavaActionProvider$Builder$CustomFileExecutor
 outer org.netbeans.modules.java.api.common.project.JavaActionProvider$Builder
meth public abstract java.lang.String[] getTargetNames(org.openide.filesystems.FileObject,org.netbeans.modules.java.api.common.project.JavaActionProvider$Context)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.JavaActionProvider$ScriptAction$Result performCompileOnSave(org.netbeans.modules.java.api.common.project.JavaActionProvider$Context,java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()

CLSS public final static !enum org.netbeans.modules.java.api.common.project.JavaActionProvider$CompileOnSaveOperation
 outer org.netbeans.modules.java.api.common.project.JavaActionProvider
fld public final static org.netbeans.modules.java.api.common.project.JavaActionProvider$CompileOnSaveOperation EXECUTE
fld public final static org.netbeans.modules.java.api.common.project.JavaActionProvider$CompileOnSaveOperation UPDATE
meth public static org.netbeans.modules.java.api.common.project.JavaActionProvider$CompileOnSaveOperation valueOf(java.lang.String)
meth public static org.netbeans.modules.java.api.common.project.JavaActionProvider$CompileOnSaveOperation[] values()
supr java.lang.Enum<org.netbeans.modules.java.api.common.project.JavaActionProvider$CompileOnSaveOperation>

CLSS public final static org.netbeans.modules.java.api.common.project.JavaActionProvider$Context
 outer org.netbeans.modules.java.api.common.project.JavaActionProvider
meth public boolean doJavaChecks()
meth public java.lang.String getCommand()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getProperty(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public java.util.Set<? extends org.netbeans.modules.java.api.common.project.JavaActionProvider$CompileOnSaveOperation> getCompileOnSaveOperations()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.java.classpath.ClassPath getProjectClassPath(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.java.platform.JavaPlatform getActiveJavaPlatform()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.api.project.Project getProject()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.ant.UpdateHelper getUpdateHelper()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.spi.project.support.ant.PropertyEvaluator getPropertyEvaluator()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.openide.util.Lookup getActiveLookup()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public void addConcealedProperty(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void setProperty(java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds additionalPropertiesProvider,classpaths,command,concealedProperties,concealedPropertiesProvider,cosOpsCache,cosOpsProvider,doJavaChecks,eval,jpp,listeners,lkp,project,properties,updateHelper,userPropertiesPolicy

CLSS public abstract static org.netbeans.modules.java.api.common.project.JavaActionProvider$ScriptAction
 outer org.netbeans.modules.java.api.common.project.JavaActionProvider
cons protected init(java.lang.String,java.lang.String,boolean,boolean,boolean,boolean)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
innr public final static Result
intf org.netbeans.modules.java.api.common.project.JavaActionProvider$Action
meth public abstract java.lang.String[] getTargetNames(org.netbeans.modules.java.api.common.project.JavaActionProvider$Context)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public boolean isEnabled(org.netbeans.modules.java.api.common.project.JavaActionProvider$Context)
meth public final java.lang.String getCommand()
meth public final void invoke(org.netbeans.modules.java.api.common.project.JavaActionProvider$Context)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.JavaActionProvider$ScriptAction$Result performCompileOnSave(org.netbeans.modules.java.api.common.project.JavaActionProvider$Context,java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds actionFlags,command,cosInterceptor,displayName

CLSS public final static org.netbeans.modules.java.api.common.project.JavaActionProvider$ScriptAction$Result
 outer org.netbeans.modules.java.api.common.project.JavaActionProvider$ScriptAction
meth public static org.netbeans.modules.java.api.common.project.JavaActionProvider$ScriptAction$Result abort()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.java.api.common.project.JavaActionProvider$ScriptAction$Result follow()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.java.api.common.project.JavaActionProvider$ScriptAction$Result success(org.openide.execution.ExecutorTask)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds ABORT,FOLLOW,task

CLSS public final org.netbeans.modules.java.api.common.project.MultiModuleActionProviderBuilder
meth public org.netbeans.modules.java.api.common.project.JavaActionProvider build()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.MultiModuleActionProviderBuilder setCompileOnSaveOperationsProvider(java.util.function.Supplier<? extends java.util.Set<? extends org.netbeans.modules.java.api.common.project.JavaActionProvider$CompileOnSaveOperation>>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.java.api.common.project.MultiModuleActionProviderBuilder newInstance(org.netbeans.api.project.Project,org.netbeans.modules.java.api.common.ant.UpdateHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.classpath.AbstractClassPathProvider)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds builder,evaluator

CLSS public org.netbeans.modules.java.api.common.project.ProjectConfigurations
fld public final static java.lang.String CONFIG_PROPS_PATH = "nbproject/private/config.properties"
innr public final static Configuration
innr public final static ConfigurationProviderBuilder
meth public !varargs static org.netbeans.spi.project.support.ant.PropertyEvaluator createPropertyEvaluator(org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyProvider[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.java.api.common.project.ProjectConfigurations$ConfigurationProviderBuilder createConfigurationProviderBuilder(org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.ant.UpdateHelper)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds LOGGER
hcls ConfigPropertyProvider,ConfigurationProviderImpl

CLSS public final static org.netbeans.modules.java.api.common.project.ProjectConfigurations$Configuration
 outer org.netbeans.modules.java.api.common.project.ProjectConfigurations
intf org.netbeans.spi.project.ProjectConfiguration
meth public boolean equals(java.lang.Object)
meth public boolean isDefault()
meth public int hashCode()
meth public java.lang.String getDisplayName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getName()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String toString()
supr java.lang.Object
hfds displayName,name

CLSS public final static org.netbeans.modules.java.api.common.project.ProjectConfigurations$ConfigurationProviderBuilder
 outer org.netbeans.modules.java.api.common.project.ProjectConfigurations
meth public !varargs org.netbeans.modules.java.api.common.project.ProjectConfigurations$ConfigurationProviderBuilder addConfigurationsAffectActions(java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.ProjectConfigurations$ConfigurationProviderBuilder setCustomizerAction(java.lang.Runnable)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.spi.project.ProjectConfigurationProvider<? extends org.netbeans.spi.project.ProjectConfiguration> build()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds configurationsAffectActions,customizerAction,eval,project,updateHelper

CLSS public final org.netbeans.modules.java.api.common.project.ProjectHooks
innr public final static ProjectOpenedHookBuilder
innr public final static ProjectXmlSavedHookBuilder
meth public static org.netbeans.modules.java.api.common.project.ProjectHooks$ProjectOpenedHookBuilder createProjectOpenedHookBuilder(org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.ant.UpdateHelper,org.netbeans.spi.project.support.ant.GeneratedFilesHelper,org.netbeans.modules.java.api.common.classpath.AbstractClassPathProvider)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.java.api.common.project.ProjectHooks$ProjectXmlSavedHookBuilder createProjectXmlSavedHookBuilder(org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.ant.UpdateHelper,org.netbeans.spi.project.support.ant.GeneratedFilesHelper)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds LOG
hcls ProjectOpenedHookImpl,ProjectXmlSavedHookImpl

CLSS public final static org.netbeans.modules.java.api.common.project.ProjectHooks$ProjectOpenedHookBuilder
 outer org.netbeans.modules.java.api.common.project.ProjectHooks
meth public org.netbeans.modules.java.api.common.project.ProjectHooks$ProjectOpenedHookBuilder addClassPathType(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.ProjectHooks$ProjectOpenedHookBuilder addClosePostAction(java.lang.Runnable)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.ProjectHooks$ProjectOpenedHookBuilder addClosePreAction(java.lang.Runnable)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.ProjectHooks$ProjectOpenedHookBuilder addOpenPostAction(java.lang.Runnable)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.ProjectHooks$ProjectOpenedHookBuilder addOpenPreAction(java.lang.Runnable)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.ProjectHooks$ProjectOpenedHookBuilder setBuildImplTemplate(java.net.URL)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.ProjectHooks$ProjectOpenedHookBuilder setBuildScriptProperty(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.ProjectHooks$ProjectOpenedHookBuilder setBuildTemplate(java.net.URL)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.spi.project.ui.ProjectOpenedHook build()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds buildImplTemplate,buildScriptProperty,buildTemplate,classPathTypes,cpProvider,eval,genFilesHelper,postClose,postOpen,preClose,preOpen,project,updateHelper

CLSS public final static org.netbeans.modules.java.api.common.project.ProjectHooks$ProjectXmlSavedHookBuilder
 outer org.netbeans.modules.java.api.common.project.ProjectHooks
meth public org.netbeans.modules.java.api.common.project.ProjectHooks$ProjectXmlSavedHookBuilder addPostAction(java.lang.Runnable)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.ProjectHooks$ProjectXmlSavedHookBuilder addPreAction(java.lang.Runnable)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.ProjectHooks$ProjectXmlSavedHookBuilder setBuildImplTemplate(java.net.URL)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.ProjectHooks$ProjectXmlSavedHookBuilder setBuildScriptProperty(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.ProjectHooks$ProjectXmlSavedHookBuilder setBuildTemplate(java.net.URL)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.ProjectHooks$ProjectXmlSavedHookBuilder setOverrideModifiedBuildImplPredicate(java.util.concurrent.Callable<java.lang.Boolean>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.spi.project.support.ant.ProjectXmlSavedHook build()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds buildImplTemplate,buildScriptProperty,buildTemplate,eval,genFilesHelper,overridePredicate,postActions,preActions,updateHelper

CLSS public final org.netbeans.modules.java.api.common.project.ProjectOperations
innr public abstract interface static Callback
innr public final static ProjectOperationsBuilder
meth public static org.netbeans.modules.java.api.common.project.ProjectOperations$ProjectOperationsBuilder createBuilder(org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.ant.UpdateHelper,org.netbeans.spi.project.support.ant.ReferenceHelper,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds LOG,TARGET_CLEAN
hcls Operations

CLSS public abstract interface static org.netbeans.modules.java.api.common.project.ProjectOperations$Callback
 outer org.netbeans.modules.java.api.common.project.ProjectOperations
innr public final static !enum Operation
meth public abstract void afterOperation(org.netbeans.modules.java.api.common.project.ProjectOperations$Callback$Operation,java.lang.String,org.openide.util.Pair<java.io.File,org.netbeans.api.project.Project>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract void beforeOperation(org.netbeans.modules.java.api.common.project.ProjectOperations$Callback$Operation)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public final static !enum org.netbeans.modules.java.api.common.project.ProjectOperations$Callback$Operation
 outer org.netbeans.modules.java.api.common.project.ProjectOperations$Callback
fld public final static org.netbeans.modules.java.api.common.project.ProjectOperations$Callback$Operation COPY
fld public final static org.netbeans.modules.java.api.common.project.ProjectOperations$Callback$Operation DELETE
fld public final static org.netbeans.modules.java.api.common.project.ProjectOperations$Callback$Operation MOVE
fld public final static org.netbeans.modules.java.api.common.project.ProjectOperations$Callback$Operation RENAME
meth public static org.netbeans.modules.java.api.common.project.ProjectOperations$Callback$Operation valueOf(java.lang.String)
meth public static org.netbeans.modules.java.api.common.project.ProjectOperations$Callback$Operation[] values()
supr java.lang.Enum<org.netbeans.modules.java.api.common.project.ProjectOperations$Callback$Operation>

CLSS public final static org.netbeans.modules.java.api.common.project.ProjectOperations$ProjectOperationsBuilder
 outer org.netbeans.modules.java.api.common.project.ProjectOperations
meth public !varargs org.netbeans.modules.java.api.common.project.ProjectOperations$ProjectOperationsBuilder addCleanTargets(java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public !varargs org.netbeans.modules.java.api.common.project.ProjectOperations$ProjectOperationsBuilder addDataFiles(java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public !varargs org.netbeans.modules.java.api.common.project.ProjectOperations$ProjectOperationsBuilder addMetadataFiles(java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public !varargs org.netbeans.modules.java.api.common.project.ProjectOperations$ProjectOperationsBuilder addPreservedPrivateProperties(java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.ProjectOperations$ProjectOperationsBuilder addUpdatedNameProperty(java.lang.String,java.lang.String,boolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.modules.java.api.common.project.ProjectOperations$ProjectOperationsBuilder setBuildScriptProperty(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.ProjectOperations$ProjectOperationsBuilder setCallback(org.netbeans.modules.java.api.common.project.ProjectOperations$Callback)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.spi.project.DataFilesProviderImplementation build()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds additionalDataFiles,additionalMetadataFiles,buildScriptProperty,callback,cleanTargets,eval,helper,privateProps,project,refHelper,sources,tests,updatedProps

CLSS public abstract interface org.netbeans.modules.java.api.common.project.ProjectPlatformProvider
fld public final static java.lang.String PROP_PROJECT_PLATFORM = "projectPlatform"
meth public abstract org.netbeans.api.java.platform.JavaPlatform getProjectPlatform()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void setProjectPlatform(org.netbeans.api.java.platform.JavaPlatform) throws java.io.IOException
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public final org.netbeans.modules.java.api.common.project.ProjectProperties
cons public init()
fld public final static java.lang.String ANNOTATION_PROCESSING_ENABLED = "annotation.processing.enabled"
fld public final static java.lang.String ANNOTATION_PROCESSING_ENABLED_IN_EDITOR = "annotation.processing.enabled.in.editor"
fld public final static java.lang.String ANNOTATION_PROCESSING_PROCESSORS_LIST = "annotation.processing.processors.list"
fld public final static java.lang.String ANNOTATION_PROCESSING_PROCESSOR_OPTIONS = "annotation.processing.processor.options"
fld public final static java.lang.String ANNOTATION_PROCESSING_RUN_ALL_PROCESSORS = "annotation.processing.run.all.processors"
fld public final static java.lang.String ANNOTATION_PROCESSING_SOURCE_OUTPUT = "annotation.processing.source.output"
fld public final static java.lang.String ANT_ARTIFACT_PREFIX = "${reference."
fld public final static java.lang.String APPLICATION_ARGS = "application.args"
fld public final static java.lang.String BUILD_CLASSES_DIR = "build.classes.dir"
fld public final static java.lang.String BUILD_CLASSES_EXCLUDES = "build.classes.excludes"
fld public final static java.lang.String BUILD_DIR = "build.dir"
fld public final static java.lang.String BUILD_GENERATED_SOURCES_DIR = "build.generated.sources.dir"
fld public final static java.lang.String BUILD_MODULES_DIR = "build.modules.dir"
fld public final static java.lang.String BUILD_SCRIPT = "buildfile"
fld public final static java.lang.String BUILD_TEST_CLASSES_DIR = "build.test.classes.dir"
fld public final static java.lang.String BUILD_TEST_MODULES_DIR = "build.test.modules.dir"
fld public final static java.lang.String BUILD_TEST_RESULTS_DIR = "build.test.results.dir"
fld public final static java.lang.String COMPILE_ON_SAVE = "compile.on.save"
fld public final static java.lang.String COMPILE_ON_SAVE_UNSUPPORTED_PREFIX = "compile.on.save.unsupported"
fld public final static java.lang.String DEBUG_CLASSPATH = "debug.classpath"
fld public final static java.lang.String DEBUG_MODULEPATH = "debug.modulepath"
fld public final static java.lang.String DEBUG_TEST_CLASSPATH = "debug.test.classpath"
fld public final static java.lang.String DEBUG_TEST_MODULEPATH = "debug.test.modulepath"
fld public final static java.lang.String DIST_ARCHIVE_EXCLUDES = "dist.archive.excludes"
fld public final static java.lang.String DIST_DIR = "dist.dir"
fld public final static java.lang.String DIST_JAR = "dist.jar"
fld public final static java.lang.String DIST_JAVADOC_DIR = "dist.javadoc.dir"
fld public final static java.lang.String DIST_JLINK_DIR = "dist.jlink.dir"
fld public final static java.lang.String DIST_JLINK_OUTPUT = "dist.jlink.output"
fld public final static java.lang.String DO_DEPEND = "do.depend"
fld public final static java.lang.String DO_JAR = "do.jar"
fld public final static java.lang.String DO_JLINK = "do.jlink"
fld public final static java.lang.String ENDORSED_CLASSPATH = "endorsed.classpath"
fld public final static java.lang.String EXCLUDES = "excludes"
fld public final static java.lang.String INCLUDES = "includes"
fld public final static java.lang.String JAR_COMPRESS = "jar.compress"
fld public final static java.lang.String JAVAC_CLASSPATH = "javac.classpath"
fld public final static java.lang.String JAVAC_COMPILERARGS = "javac.compilerargs"
fld public final static java.lang.String JAVAC_DEBUG = "javac.debug"
fld public final static java.lang.String JAVAC_DEPRECATION = "javac.deprecation"
fld public final static java.lang.String JAVAC_MODULEPATH = "javac.modulepath"
fld public final static java.lang.String JAVAC_PROCESSORMODULEPATH = "javac.processormodulepath"
fld public final static java.lang.String JAVAC_PROCESSORPATH = "javac.processorpath"
fld public final static java.lang.String JAVAC_PROFILE = "javac.profile"
fld public final static java.lang.String JAVAC_SOURCE = "javac.source"
fld public final static java.lang.String JAVAC_TARGET = "javac.target"
fld public final static java.lang.String JAVAC_TEST_CLASSPATH = "javac.test.classpath"
fld public final static java.lang.String JAVAC_TEST_COMPILERARGS = "javac.test.compilerargs"
fld public final static java.lang.String JAVAC_TEST_MODULEPATH = "javac.test.modulepath"
fld public final static java.lang.String JAVADOC_ADDITIONALPARAM = "javadoc.additionalparam"
fld public final static java.lang.String JAVADOC_AUTHOR = "javadoc.author"
fld public final static java.lang.String JAVADOC_ENCODING = "javadoc.encoding"
fld public final static java.lang.String JAVADOC_HTML5 = "javadoc.html5"
fld public final static java.lang.String JAVADOC_NO_INDEX = "javadoc.noindex"
fld public final static java.lang.String JAVADOC_NO_NAVBAR = "javadoc.nonavbar"
fld public final static java.lang.String JAVADOC_NO_TREE = "javadoc.notree"
fld public final static java.lang.String JAVADOC_PREVIEW = "javadoc.preview"
fld public final static java.lang.String JAVADOC_PRIVATE = "javadoc.private"
fld public final static java.lang.String JAVADOC_SPLIT_INDEX = "javadoc.splitindex"
fld public final static java.lang.String JAVADOC_USE = "javadoc.use"
fld public final static java.lang.String JAVADOC_VERSION = "javadoc.version"
fld public final static java.lang.String JAVADOC_WINDOW_TITLE = "javadoc.windowtitle"
fld public final static java.lang.String JLINK_ADDITIONALMODULES = "jlink.additionalmodules"
fld public final static java.lang.String JLINK_ADDITIONALPARAM = "jlink.additionalparam"
fld public final static java.lang.String JLINK_LAUNCHER = "jlink.launcher"
fld public final static java.lang.String JLINK_LAUNCHER_NAME = "jlink.launcher.name"
fld public final static java.lang.String JLINK_STRIP = "jlink.strip"
fld public final static java.lang.String LICENSE_NAME = "project.license"
fld public final static java.lang.String LICENSE_PATH = "project.licensePath"
fld public final static java.lang.String MAIN_CLASS = "main.class"
fld public final static java.lang.String MANIFEST_FILE = "manifest.file"
fld public final static java.lang.String NO_DEPENDENCIES = "no.dependencies"
fld public final static java.lang.String PLATFORM_ACTIVE = "platform.active"
fld public final static java.lang.String PROP_PROJECT_CONFIGURATION_CONFIG = "config"
fld public final static java.lang.String RUNTIME_ENCODING = "runtime.encoding"
fld public final static java.lang.String RUN_CLASSPATH = "run.classpath"
fld public final static java.lang.String RUN_JVM_ARGS = "run.jvmargs"
fld public final static java.lang.String RUN_MODULEPATH = "run.modulepath"
fld public final static java.lang.String RUN_TEST_CLASSPATH = "run.test.classpath"
fld public final static java.lang.String RUN_TEST_MODULEPATH = "run.test.modulepath"
fld public final static java.lang.String RUN_WORK_DIR = "work.dir"
fld public final static java.lang.String SOURCE_ENCODING = "source.encoding"
fld public final static java.lang.String SYSTEM_PROPERTIES_RUN_PREFIX = "run-sys-prop."
fld public final static java.lang.String SYSTEM_PROPERTIES_TEST_PREFIX = "test-sys-prop."
fld public final static java.lang.String TRACK_FILE_CHANGES = "track.file.changes"
fld public final static java.lang.String[] WELL_KNOWN_PATHS
fld public static javax.swing.ImageIcon ICON_ARTIFACT
fld public static javax.swing.ImageIcon ICON_BROKEN_BADGE
fld public static javax.swing.ImageIcon ICON_CLASSPATH
fld public static javax.swing.ImageIcon ICON_JAR
fld public static javax.swing.ImageIcon ICON_JAVADOC_BADGE
fld public static javax.swing.ImageIcon ICON_LIBRARY
fld public static javax.swing.ImageIcon ICON_SOURCE_BADGE
supr java.lang.Object
hfds RESOURCE_ICON_ARTIFACT,RESOURCE_ICON_BROKEN_BADGE,RESOURCE_ICON_CLASSPATH,RESOURCE_ICON_JAR,RESOURCE_ICON_JAVADOC_BADGE,RESOURCE_ICON_LIBRARY,RESOURCE_ICON_SOURCE_BADGE,RUN_JVM_ARGS_IDE

CLSS public abstract interface org.netbeans.modules.java.api.common.project.PropertyEvaluatorProvider
meth public abstract org.netbeans.spi.project.support.ant.PropertyEvaluator getPropertyEvaluator()
 anno 0 org.netbeans.api.annotations.common.NonNull()

CLSS public final org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport
innr public abstract interface static Callback
meth public static boolean canEdit(javax.swing.ListSelectionModel,javax.swing.DefaultListModel)
meth public static boolean canMoveDown(javax.swing.ListSelectionModel,int)
meth public static boolean canMoveUp(javax.swing.ListSelectionModel)
meth public static int[] addArtifacts(javax.swing.DefaultListModel,int[],org.netbeans.modules.java.api.common.project.ui.customizer.AntArtifactItem[],org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport$Callback)
meth public static int[] addJarFiles(javax.swing.DefaultListModel,int[],java.lang.String[],java.io.File,java.lang.String[],org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport$Callback)
meth public static int[] addLibraries(javax.swing.DefaultListModel,int[],org.netbeans.api.project.libraries.Library[],java.util.Set<org.netbeans.api.project.libraries.Library>,org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport$Callback)
meth public static int[] moveDown(javax.swing.DefaultListModel,int[])
meth public static int[] moveUp(javax.swing.DefaultListModel,int[])
meth public static int[] remove(javax.swing.DefaultListModel,int[])
meth public static java.util.Iterator<org.netbeans.modules.java.api.common.classpath.ClassPathSupport$Item> getIterator(javax.swing.DefaultListModel)
meth public static java.util.List<org.netbeans.modules.java.api.common.classpath.ClassPathSupport$Item> getList(javax.swing.DefaultListModel)
meth public static javax.swing.DefaultListModel createListModel(java.util.Iterator)
meth public static void edit(javax.swing.DefaultListModel,int[],org.netbeans.spi.project.support.ant.AntProjectHelper)
supr java.lang.Object

CLSS public abstract interface static org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport$Callback
 outer org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport
meth public abstract void initItem(org.netbeans.modules.java.api.common.classpath.ClassPathSupport$Item)

CLSS public final org.netbeans.modules.java.api.common.project.ui.JavaSourceNodeFactory
cons public init()
intf org.netbeans.spi.project.ui.support.NodeFactory
meth public org.netbeans.spi.project.ui.support.NodeList createNodes(org.netbeans.api.project.Project)
supr java.lang.Object
hfds LOG
hcls GeneratedSourceGroup,SourceGroupKey,SourcesNodeList

CLSS public final org.netbeans.modules.java.api.common.project.ui.LibrariesNode
cons public init(java.lang.String,org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.ant.UpdateHelper,org.netbeans.spi.project.support.ant.ReferenceHelper,java.lang.String,java.lang.String[],java.lang.String,javax.swing.Action[],java.lang.String,org.netbeans.modules.java.api.common.classpath.ClassPathSupport,org.netbeans.modules.java.api.common.project.ui.LibrariesNode$Callback)
fld public final static org.openide.util.RequestProcessor rp
innr public abstract interface static Callback
innr public final static Builder
innr public final static Key
meth public boolean canCopy()
meth public java.awt.Image getIcon(int)
meth public java.awt.Image getOpenedIcon(int)
meth public java.lang.String getDisplayName()
meth public java.lang.String getName()
meth public javax.swing.Action[] getActions(boolean)
meth public static javax.swing.Action createAddFolderAction(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.modules.java.api.common.SourceRoots)
meth public static javax.swing.Action createAddLibraryAction(org.netbeans.spi.project.support.ant.ReferenceHelper,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.api.project.libraries.LibraryChooser$Filter)
meth public static javax.swing.Action createAddProjectAction(org.netbeans.api.project.Project,org.netbeans.modules.java.api.common.SourceRoots)
supr org.openide.nodes.AbstractNode
hfds ICON_BADGE,displayName,folderIconCache,librariesNodeActions,openedFolderIconCache
hcls AddFolderAction,AddLibraryAction,AddProjectAction,LibrariesChildren,PathFinder,RootsListener,SimpleFileFilter

CLSS public final static org.netbeans.modules.java.api.common.project.ui.LibrariesNode$Builder
 outer org.netbeans.modules.java.api.common.project.ui.LibrariesNode
cons public init(org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.ant.UpdateHelper,org.netbeans.spi.project.support.ant.ReferenceHelper,org.netbeans.modules.java.api.common.classpath.ClassPathSupport)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
meth public !varargs org.netbeans.modules.java.api.common.project.ui.LibrariesNode$Builder addClassPathIgnoreRefs(java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public !varargs org.netbeans.modules.java.api.common.project.ui.LibrariesNode$Builder addClassPathProperties(java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public !varargs org.netbeans.modules.java.api.common.project.ui.LibrariesNode$Builder addLibrariesNodeActions(javax.swing.Action[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public !varargs org.netbeans.modules.java.api.common.project.ui.LibrariesNode$Builder addModulePathIgnoreRefs(java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public !varargs org.netbeans.modules.java.api.common.project.ui.LibrariesNode$Builder addModulePathProperties(java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.ui.LibrariesNode build()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.ui.LibrariesNode$Builder setBootPath(org.netbeans.api.java.classpath.ClassPath)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.ui.LibrariesNode$Builder setExtraNodes(org.netbeans.spi.project.ui.support.NodeList<org.netbeans.modules.java.api.common.project.ui.LibrariesNode$Key>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.ui.LibrariesNode$Builder setModuleInfoBasedPath(org.netbeans.api.java.classpath.ClassPath)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.ui.LibrariesNode$Builder setModuleSourcePath(org.netbeans.api.java.classpath.ClassPath)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.ui.LibrariesNode$Builder setName(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.ui.LibrariesNode$Builder setPlatformProperty(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.ui.LibrariesNode$Builder setPlatformType(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.ui.LibrariesNode$Builder setSourcePath(org.netbeans.api.java.classpath.ClassPath)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.ui.LibrariesNode$Builder setWebModuleElementName(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds boot,classPathIgnoreRef,classPathProperties,cs,eval,extraNodes,helper,librariesNodeActions,modulePath,modulePathIgnoreRef,moduleSourcePath,name,project,refHelper,sourcePath,webModuleElementName
hcls CallBackImpl

CLSS public abstract interface static org.netbeans.modules.java.api.common.project.ui.LibrariesNode$Callback
 outer org.netbeans.modules.java.api.common.project.ui.LibrariesNode
meth public abstract java.util.List<org.netbeans.modules.java.api.common.project.ui.LibrariesNode$Key> getExtraKeys()
meth public abstract org.openide.nodes.Node[] createNodes(org.netbeans.modules.java.api.common.project.ui.LibrariesNode$Key)

CLSS public final static org.netbeans.modules.java.api.common.project.ui.LibrariesNode$Key
 outer org.netbeans.modules.java.api.common.project.ui.LibrariesNode
cons public init(java.lang.String)
meth public boolean equals(java.lang.Object)
meth public int getType()
meth public int hashCode()
meth public java.lang.String getClassPathId()
meth public java.lang.String getEntryId()
meth public java.lang.String getID()
meth public java.net.URI getArtifactLocation()
meth public org.netbeans.api.project.SourceGroup getSourceGroup()
meth public org.netbeans.api.project.ant.AntArtifact getProject()
supr java.lang.Object
hfds TYPE_FILE,TYPE_FILE_REFERENCE,TYPE_LIBRARY,TYPE_MODULE,TYPE_OTHER,TYPE_PLATFORM,TYPE_PROJECT,anID,antArtifact,classPathId,entryId,postRemoveAction,preRemoveAction,sg,shared,type,uri

CLSS public abstract interface org.netbeans.modules.java.api.common.project.ui.LogicalViewProvider2
intf org.netbeans.spi.project.ui.LogicalViewProvider
meth public abstract void testBroken()

CLSS public final org.netbeans.modules.java.api.common.project.ui.LogicalViewProviders
innr public abstract interface static CompileOnSaveBadge
innr public static LogicalViewProviderBuilder
meth public static org.netbeans.modules.java.api.common.project.ui.LogicalViewProviders$LogicalViewProviderBuilder createBuilder(org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hcls LogicalViewProviderImpl,SimpleInfo

CLSS public abstract interface static org.netbeans.modules.java.api.common.project.ui.LogicalViewProviders$CompileOnSaveBadge
 outer org.netbeans.modules.java.api.common.project.ui.LogicalViewProviders
meth public abstract boolean isBadgeVisible()
meth public abstract boolean isImportant(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public static org.netbeans.modules.java.api.common.project.ui.LogicalViewProviders$LogicalViewProviderBuilder
 outer org.netbeans.modules.java.api.common.project.ui.LogicalViewProviders
meth public org.netbeans.modules.java.api.common.project.ui.LogicalViewProvider2 build()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.ui.LogicalViewProviders$LogicalViewProviderBuilder setCompileOnSaveBadge(org.netbeans.modules.java.api.common.project.ui.LogicalViewProviders$CompileOnSaveBadge)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.ui.LogicalViewProviders$LogicalViewProviderBuilder setHelpCtx(org.openide.util.HelpCtx)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds badgeStatus,eval,helpContext,project,projectType

CLSS public final org.netbeans.modules.java.api.common.project.ui.MultiModuleNodeFactory
innr public final static Builder
intf org.netbeans.spi.project.ui.support.NodeFactory
meth public org.netbeans.spi.project.ui.support.NodeList<?> createNodes(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds LOG,RP,libsSupport,procGenSrc,sourceModules,testModules
hcls LibrariesSupport,ModuleChildren,ModuleKey,ModuleNode,Nodes,ProcessorGeneratedSources,SimpleLabelNode,TestRootNode

CLSS public final static org.netbeans.modules.java.api.common.project.ui.MultiModuleNodeFactory$Builder
 outer org.netbeans.modules.java.api.common.project.ui.MultiModuleNodeFactory
meth public !varargs org.netbeans.modules.java.api.common.project.ui.MultiModuleNodeFactory$Builder addLibrariesNodeActions(javax.swing.Action[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public !varargs org.netbeans.modules.java.api.common.project.ui.MultiModuleNodeFactory$Builder addTestLibrariesNodeActions(javax.swing.Action[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.ui.MultiModuleNodeFactory build()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.ui.MultiModuleNodeFactory$Builder addLibrariesNodes()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.ui.MultiModuleNodeFactory$Builder setAnnotationProcessorsGeneratedSourcesProperty(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.ui.MultiModuleNodeFactory$Builder setSources(org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.api.common.project.ui.MultiModuleNodeFactory$Builder setTests(org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.java.api.common.project.ui.MultiModuleNodeFactory$Builder create(org.netbeans.modules.java.api.common.ant.UpdateHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.spi.project.support.ant.ReferenceHelper)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds eval,helper,libSupport,mods,procGenSourcesProp,refHelper,testMods

CLSS public final org.netbeans.modules.java.api.common.project.ui.ProjectUISupport
meth public static java.lang.String showVMOptionCustomizer(java.awt.Window,java.lang.String) throws java.lang.Exception
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static javax.swing.AbstractAction createPreselectPropertiesAction(org.netbeans.api.project.Project,java.lang.String,java.lang.String)
meth public static org.netbeans.api.project.SourceGroup createLibrariesSourceGroup(org.openide.filesystems.FileObject,java.lang.String,javax.swing.Icon,javax.swing.Icon)
meth public static org.openide.nodes.FilterNode createFilteredLibrariesNode(org.openide.nodes.Node,org.netbeans.modules.java.api.common.ant.UpdateHelper,java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.java.api.common.classpath.ClassPathSupport,org.netbeans.spi.project.support.ant.ReferenceHelper)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
 anno 6 org.netbeans.api.annotations.common.NullAllowed()
 anno 7 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object

CLSS public final org.netbeans.modules.java.api.common.project.ui.customizer.AntArtifactItem
cons public init(org.netbeans.api.project.ant.AntArtifact,java.net.URI)
meth public java.lang.String toString()
meth public java.net.URI getArtifactURI()
meth public org.netbeans.api.project.ant.AntArtifact getArtifact()
meth public static org.netbeans.modules.java.api.common.project.ui.customizer.AntArtifactItem[] showAntArtifactItemChooser(java.lang.String[],org.netbeans.api.project.Project,java.awt.Component)
supr java.lang.Object
hfds artifact,artifactURI

CLSS public org.netbeans.modules.java.api.common.project.ui.customizer.ClassPathListCellRenderer
meth public java.awt.Component getListCellRendererComponent(javax.swing.JList,java.lang.Object,int,boolean,boolean)
meth public static javax.swing.ListCellRenderer createClassPathListRenderer(org.netbeans.spi.project.support.ant.PropertyEvaluator,org.openide.filesystems.FileObject)
meth public static javax.swing.table.TableCellRenderer createClassPathTableRenderer(org.netbeans.spi.project.support.ant.PropertyEvaluator,org.openide.filesystems.FileObject)
meth public static javax.swing.tree.TreeCellRenderer createClassPathTreeRenderer(org.netbeans.spi.project.support.ant.PropertyEvaluator,org.openide.filesystems.FileObject)
supr javax.swing.DefaultListCellRenderer
hfds FOREIGN_PLAIN_FILE_REFERENCE,ICON_BROKEN_ARTIFACT,ICON_BROKEN_JAR,ICON_BROKEN_LIBRARY,ICON_FOLDER,UNKNOWN_FILE_REFERENCE,WELL_KNOWN_PATHS_NAMES,evaluator,projectFolder
hcls ClassPathTableCellRenderer,ClassPathTreeCellRenderer

CLSS public abstract interface org.netbeans.modules.java.api.common.project.ui.customizer.CustomizerProvider2
 anno 0 java.lang.Deprecated()
intf org.netbeans.spi.project.ui.CustomizerProvider
meth public abstract void showCustomizer(java.lang.String,java.lang.String)

CLSS public abstract interface org.netbeans.modules.java.api.common.project.ui.customizer.CustomizerProvider3
intf org.netbeans.spi.project.ui.CustomizerProvider2
meth public abstract void cancelCustomizer()

CLSS public final org.netbeans.modules.java.api.common.project.ui.customizer.EditMediator
fld public final static javax.swing.filechooser.FileFilter JAR_ZIP_FILTER
innr public abstract interface static ListComponent
intf java.awt.event.ActionListener
intf javax.swing.event.ListSelectionListener
meth public static java.io.File getLastUsedClassPathFolder()
meth public static org.netbeans.api.project.libraries.LibraryChooser$Filter createLibraryFilter()
meth public static org.netbeans.modules.java.api.common.project.ui.customizer.EditMediator$ListComponent createListComponent(javax.swing.JList)
meth public static org.netbeans.modules.java.api.common.project.ui.customizer.EditMediator$ListComponent createListComponent(javax.swing.JTable,javax.swing.DefaultListModel)
meth public static void register(org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.ReferenceHelper,org.netbeans.modules.java.api.common.project.ui.customizer.EditMediator$ListComponent,javax.swing.ButtonModel,javax.swing.ButtonModel,javax.swing.ButtonModel,javax.swing.ButtonModel,javax.swing.ButtonModel,javax.swing.ButtonModel,javax.swing.ButtonModel,boolean,javax.swing.text.Document,org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport$Callback)
meth public static void register(org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.ReferenceHelper,org.netbeans.modules.java.api.common.project.ui.customizer.EditMediator$ListComponent,javax.swing.ButtonModel,javax.swing.ButtonModel,javax.swing.ButtonModel,javax.swing.ButtonModel,javax.swing.ButtonModel,javax.swing.ButtonModel,javax.swing.ButtonModel,boolean,javax.swing.text.Document,org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport$Callback,java.lang.String[],javax.swing.filechooser.FileFilter,int)
meth public static void register(org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.ReferenceHelper,org.netbeans.modules.java.api.common.project.ui.customizer.EditMediator$ListComponent,javax.swing.ButtonModel,javax.swing.ButtonModel,javax.swing.ButtonModel,javax.swing.ButtonModel,javax.swing.ButtonModel,javax.swing.ButtonModel,javax.swing.ButtonModel,javax.swing.text.Document,org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport$Callback)
meth public static void register(org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.ReferenceHelper,org.netbeans.modules.java.api.common.project.ui.customizer.EditMediator$ListComponent,javax.swing.ButtonModel,javax.swing.ButtonModel,javax.swing.ButtonModel,javax.swing.ButtonModel,javax.swing.ButtonModel,javax.swing.ButtonModel,javax.swing.ButtonModel,javax.swing.text.Document,org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport$Callback,java.lang.String[],javax.swing.filechooser.FileFilter,int)
meth public static void setLastUsedClassPathFolder(java.io.File)
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void valueChanged(javax.swing.event.ListSelectionEvent)
supr java.lang.Object
hfds DEFAULT_ANT_ARTIFACT_TYPES,LAST_USED_CP_FOLDER,RP,addAntArtifact,addJar,addLibrary,allowRemoveClassPath,antArtifactTypes,callback,edit,fileSelectionMode,filter,helper,libraryPath,list,listModel,moveDown,moveUp,project,refHelper,remove,selectionModel
hcls JListListComponent,JTableListComponent,SimpleFileFilter

CLSS public abstract interface static org.netbeans.modules.java.api.common.project.ui.customizer.EditMediator$ListComponent
 outer org.netbeans.modules.java.api.common.project.ui.customizer.EditMediator
meth public abstract int[] getSelectedIndices()
meth public abstract java.awt.Component getComponent()
meth public abstract javax.swing.DefaultListModel getModel()
meth public abstract javax.swing.ListSelectionModel getSelectionModel()
meth public abstract void setSelectedIndices(int[])

CLSS public org.netbeans.modules.java.api.common.project.ui.customizer.MainClassChooser
cons public init(java.util.Collection<org.netbeans.api.java.source.ElementHandle<javax.lang.model.element.TypeElement>>)
cons public init(java.util.Collection<org.netbeans.api.java.source.ElementHandle<javax.lang.model.element.TypeElement>>,java.lang.String)
cons public init(org.openide.filesystems.FileObject[])
cons public init(org.openide.filesystems.FileObject[],java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
cons public init(org.openide.filesystems.FileObject[],java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
fld public static java.lang.Boolean unitTestingSupport_hasMainMethodResult
meth public java.lang.String getSelectedMainClass()
meth public static boolean hasMainMethod(org.openide.filesystems.FileObject)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr javax.swing.JPanel
hfds changeListener,dialogSubtitle,jLabel1,jMainClassList,jScrollPane1,possibleMainClasses,scanningLabel
hcls MainClassComparator,MainClassRenderer,SearchTask

CLSS public org.netbeans.modules.java.api.common.project.ui.customizer.MainClassWarning
cons public init(java.lang.String,java.util.Collection<org.netbeans.api.java.source.ElementHandle<javax.lang.model.element.TypeElement>>)
cons public init(java.lang.String,org.openide.filesystems.FileObject[])
meth public java.lang.String getSelectedMainClass()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr javax.swing.JPanel
hfds jLabel1,jPanel1,mainClasses,message,sourcesRoots

CLSS public abstract interface org.netbeans.modules.java.api.common.project.ui.customizer.ProjectSharability
meth public abstract boolean isSharable()
meth public abstract void makeSharable()

CLSS public final org.netbeans.modules.java.api.common.project.ui.customizer.SourceRootsUi
innr public static EditMediator
meth public static javax.swing.table.DefaultTableModel createModel(org.netbeans.modules.java.api.common.ModuleRoots)
meth public static javax.swing.table.DefaultTableModel createModel(org.netbeans.modules.java.api.common.SourceRoots)
meth public static org.netbeans.modules.java.api.common.project.ui.customizer.SourceRootsUi$EditMediator registerEditMediator(org.netbeans.api.project.Project,org.netbeans.modules.java.api.common.SourceRoots,javax.swing.JTable,javax.swing.JButton,javax.swing.JButton,javax.swing.JButton,javax.swing.JButton)
meth public static org.netbeans.modules.java.api.common.project.ui.customizer.SourceRootsUi$EditMediator registerEditMediator(org.netbeans.api.project.Project,org.netbeans.modules.java.api.common.SourceRoots,javax.swing.JTable,javax.swing.JButton,javax.swing.JButton,javax.swing.JButton,javax.swing.JButton,javax.swing.CellEditor,boolean)
meth public static void showIllegalRootsDialog(java.util.Set)
supr java.lang.Object
hcls FileRenderer,SourceRootsModel,WarningDlg

CLSS public static org.netbeans.modules.java.api.common.project.ui.customizer.SourceRootsUi$EditMediator
 outer org.netbeans.modules.java.api.common.project.ui.customizer.SourceRootsUi
cons public init(org.netbeans.api.project.Project,org.netbeans.modules.java.api.common.SourceRoots,javax.swing.JTable,javax.swing.JButton,javax.swing.JButton,javax.swing.JButton,javax.swing.JButton,boolean)
intf java.awt.event.ActionListener
intf javax.swing.event.CellEditorListener
intf javax.swing.event.ListSelectionListener
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void editingCanceled(javax.swing.event.ChangeEvent)
meth public void editingStopped(javax.swing.event.ChangeEvent)
meth public void setRelatedEditMediator(org.netbeans.modules.java.api.common.project.ui.customizer.SourceRootsUi$EditMediator)
meth public void valueChanged(javax.swing.event.ListSelectionEvent)
supr java.lang.Object
hfds addFolderButton,downButton,emptyTableIsValid,ownedFolders,project,relatedEditMediator,removeButton,rootsList,rootsModel,sourceRoots,upButton

CLSS public final org.netbeans.modules.java.api.common.project.ui.wizards.FolderList
cons public init(java.lang.String,char,java.lang.String,java.lang.String,char,java.lang.String,char,java.lang.String)
fld public final static java.lang.String PROP_FILES = "files"
fld public final static java.lang.String PROP_LAST_USED_DIR = "lastUsedDir"
meth public java.io.File getLastUsedDir()
meth public java.io.File[] getFiles()
meth public static boolean isValidRoot(java.io.File,java.io.File[],java.io.File)
meth public static java.io.FileFilter testRootsFilter()
meth public void setEnabled(boolean)
meth public void setFiles(java.io.File[])
meth public void setLastUsedDir(java.io.File)
meth public void setProjectFolder(java.io.File)
meth public void setRelatedFolderList(org.netbeans.modules.java.api.common.project.ui.wizards.FolderList)
meth public void setRelatedFolderList(org.netbeans.modules.java.api.common.project.ui.wizards.FolderList,java.io.FileFilter)
supr javax.swing.JPanel
hfds LOG,TESTS_RE,addButton,fcMessage,jLabel1,jScrollPane1,lastUsedFolder,projectFolder,relatedFolderFilter,relatedFolderList,removeButton,roots
hcls ContextFileFilter,DNDHandle,FileListTransferable,Renderer

CLSS public org.netbeans.modules.java.api.common.queries.GenericModuleInfoAccessibilityQuery
cons public init()
intf org.netbeans.spi.java.queries.AccessibilityQueryImplementation2
meth public org.netbeans.spi.java.queries.AccessibilityQueryImplementation2$Result isPubliclyAccessible(org.openide.filesystems.FileObject)
supr java.lang.Object
hfds LOG,path2Result,sourcePath2Listener
hcls ClassPathListener,CleanPath2Result,ResultImpl,TextJFO

CLSS public abstract interface org.netbeans.modules.java.api.common.queries.MultiModuleGroupQuery
innr public final static Result
meth public abstract org.netbeans.api.project.SourceGroup[] filterModuleGroups(java.lang.String,org.netbeans.api.project.SourceGroup[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.java.api.common.queries.MultiModuleGroupQuery$Result findModuleInfo(org.netbeans.api.project.SourceGroup)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public final static org.netbeans.modules.java.api.common.queries.MultiModuleGroupQuery$Result
 outer org.netbeans.modules.java.api.common.queries.MultiModuleGroupQuery
meth public java.lang.String getModuleName()
meth public java.lang.String getPathFromModule()
supr java.lang.Object
hfds moduleName,modulePath

CLSS public final org.netbeans.modules.java.api.common.queries.QuerySupport
meth public !varargs static org.netbeans.api.project.Sources createSources(org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.Roots[])
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
meth public !varargs static org.netbeans.modules.java.api.common.queries.MultiModuleGroupQuery createMultiModuleGroupQuery(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.api.project.Sources,org.netbeans.modules.java.api.common.Roots[])
meth public !varargs static org.netbeans.spi.queries.SharabilityQueryImplementation createSharabilityQuery(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots,java.lang.String[])
 anno 0 java.lang.Deprecated()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
meth public !varargs static org.netbeans.spi.queries.SharabilityQueryImplementation2 createSharabilityQuery2(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots,java.lang.String[])
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.api.project.ProjectInformation createProjectInformation(org.netbeans.modules.java.api.common.ant.UpdateHelper,org.netbeans.api.project.Project,javax.swing.Icon)
meth public static org.netbeans.api.project.ProjectInformation createProjectInformation(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.api.project.Project,javax.swing.Icon)
meth public static org.netbeans.spi.java.queries.AccessibilityQueryImplementation2 createModuleInfoAccessibilityQuery(org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.java.queries.AccessibilityQueryImplementation2 createModuleInfoAccessibilityQuery(org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.java.queries.AnnotationProcessingQueryImplementation createAnnotationProcessingQuery(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public static org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation createBinaryForSourceQueryImplementation(org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator)
meth public static org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation createBinaryForSourceQueryImplementation(org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String[],java.lang.String[])
meth public static org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation createMultiModuleBinaryForSourceQuery(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation createAutomaticModuleNameQuery(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.SourceRoots,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation createCompilerOptionsQuery(org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation createMultiModuleUnitTestsCompilerOptionsQuery(org.netbeans.api.project.Project,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation createUnitTestsCompilerOptionsQuery(org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation createJavadocForBinaryQuery(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator)
meth public static org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation createJavadocForBinaryQuery(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String[])
meth public static org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation createMultiModuleJavadocForBinaryQuery(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.java.queries.MultipleRootsUnitTestForSourceQueryImplementation createMultiModuleUnitTestForSourceQuery(org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.java.queries.MultipleRootsUnitTestForSourceQueryImplementation createUnitTestForSourceQuery(org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots)
meth public static org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation createCompiledSourceForBinaryQuery(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots)
meth public static org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation createCompiledSourceForBinaryQuery(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots,java.lang.String[],java.lang.String[])
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation createMultiModuleSourceForBinaryQuery(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.java.queries.SourceLevelQueryImplementation createSourceLevelQuery(org.netbeans.spi.project.support.ant.PropertyEvaluator)
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.spi.java.queries.SourceLevelQueryImplementation2 createSourceLevelQuery2(org.netbeans.spi.project.support.ant.PropertyEvaluator)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.java.queries.SourceLevelQueryImplementation2 createSourceLevelQuery2(org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.project.ant.AntArtifactProvider createMultiModuleAntArtifactProvider(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.queries.FileBuiltQueryImplementation createFileBuiltQuery(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots)
meth public static org.netbeans.spi.queries.FileBuiltQueryImplementation createMultiModuleFileBuiltQuery(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.queries.FileEncodingQueryImplementation createFileEncodingQuery(org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String)
meth public static org.netbeans.spi.queries.SharabilityQueryImplementation createSharabilityQuery(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots)
 anno 0 java.lang.Deprecated()
meth public static org.openide.loaders.CreateFromTemplateAttributesProvider createTemplateAttributesProvider(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.queries.FileEncodingQueryImplementation)
supr java.lang.Object
hcls AntHelper,AntUpdateHelper

CLSS public abstract interface org.netbeans.modules.java.api.common.ui.PlatformFilter
 anno 0 java.lang.FunctionalInterface()
meth public abstract boolean accept(org.netbeans.api.java.platform.JavaPlatform)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public final org.netbeans.modules.java.api.common.ui.PlatformUiSupport
meth public static javax.swing.ComboBoxModel createPlatformComboBoxModel(java.lang.String)
meth public static javax.swing.ComboBoxModel createPlatformComboBoxModel(java.lang.String,java.util.Collection<? extends org.netbeans.modules.java.api.common.ui.PlatformFilter>)
meth public static javax.swing.ComboBoxModel createPlatformComboBoxModel(org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String,java.util.Collection<? extends org.netbeans.modules.java.api.common.ui.PlatformFilter>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
meth public static javax.swing.ComboBoxModel createProfileComboBoxModel(javax.swing.ComboBoxModel,java.lang.String,org.netbeans.api.java.queries.SourceLevelQuery$Profile)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public static javax.swing.ComboBoxModel createSourceLevelComboBoxModel(javax.swing.ComboBoxModel,java.lang.String,java.lang.String)
meth public static javax.swing.ComboBoxModel createSourceLevelComboBoxModel(javax.swing.ComboBoxModel,java.lang.String,java.lang.String,org.openide.modules.SpecificationVersion)
meth public static javax.swing.ListCellRenderer createPlatformListCellRenderer()
meth public static javax.swing.ListCellRenderer createProfileListCellRenderer()
meth public static javax.swing.ListCellRenderer createSourceLevelListCellRenderer()
meth public static org.netbeans.api.java.platform.JavaPlatform getPlatform(java.lang.Object)
meth public static org.netbeans.api.java.queries.SourceLevelQuery$Profile getProfile(java.lang.Object)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.openide.modules.SpecificationVersion getSourceLevel(java.lang.Object)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static void storePlatform(org.netbeans.spi.project.support.ant.EditableProperties,org.netbeans.modules.java.api.common.ant.UpdateHelper,java.lang.String,java.lang.Object,java.lang.Object)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
meth public static void storePlatform(org.netbeans.spi.project.support.ant.EditableProperties,org.netbeans.modules.java.api.common.ant.UpdateHelper,java.lang.String,java.lang.Object,java.lang.Object,boolean)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
meth public static void storePlatform(org.netbeans.spi.project.support.ant.EditableProperties,org.netbeans.modules.java.api.common.ant.UpdateHelper,java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,boolean)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
 anno 6 org.netbeans.api.annotations.common.NullAllowed()
meth public static void storePlatform(org.netbeans.spi.project.support.ant.EditableProperties,org.netbeans.modules.java.api.common.ant.UpdateHelper,java.lang.String,java.lang.String,org.openide.modules.SpecificationVersion)
supr java.lang.Object
hfds JDK_1_5,LOGGER
hcls PlatformComboBoxModel,PlatformKey,PlatformListCellRenderer,ProfileComboBoxModel,ProfileListCellRenderer,SourceLevelComboBoxModel,SourceLevelKey,SourceLevelListCellRenderer

CLSS public final org.netbeans.modules.java.api.common.util.CommonModuleUtils
fld public final static org.openide.modules.SpecificationVersion JDK9
meth public static java.lang.String getXModule(org.netbeans.api.java.queries.CompilerOptionsQuery$Result)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static java.util.Collection<? extends java.lang.String> parseSourcePathVariants(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static java.util.Map<java.lang.String,java.util.List<java.net.URL>> getPatches(org.netbeans.api.java.queries.CompilerOptionsQuery$Result)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static java.util.Set<java.lang.String> getAddModules(org.netbeans.api.java.queries.CompilerOptionsQuery$Result)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds ARG_ADDMODS,ARG_PATCH_MOD,ARG_XMODULE,MATCHER_PATCH,MATCHER_XMODULE

CLSS public final org.netbeans.modules.java.api.common.util.CommonProjectUtils
fld public final static java.lang.String J2SE_PLATFORM_TYPE = "j2se"
meth public static boolean hasMainMethod(org.openide.filesystems.FileObject)
meth public static boolean isMainClass(java.lang.String,org.netbeans.api.java.classpath.ClassPath,org.netbeans.api.java.classpath.ClassPath,org.netbeans.api.java.classpath.ClassPath)
meth public static java.lang.String getAntPropertyName(java.lang.String)
meth public static java.lang.String getBuildXmlName(org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public static java.util.Collection<org.netbeans.api.java.source.ElementHandle<javax.lang.model.element.TypeElement>> getMainMethods(org.openide.filesystems.FileObject)
meth public static org.netbeans.api.java.platform.JavaPlatform getActivePlatform(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.api.java.platform.JavaPlatform getActivePlatform(java.lang.String,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.spi.project.libraries.LibraryImplementation3 createJavaLibraryImplementation(java.lang.String,java.net.URL[],java.net.URL[],java.net.URL[],java.lang.String[],java.lang.String[])
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.java.classpath.ClassPathProvider
meth public abstract org.netbeans.api.java.classpath.ClassPath findClassPath(org.openide.filesystems.FileObject,java.lang.String)

CLSS public abstract interface org.netbeans.spi.java.project.classpath.ProjectClassPathExtender
 anno 0 java.lang.Deprecated()
meth public abstract boolean addAntArtifact(org.netbeans.api.project.ant.AntArtifact,java.net.URI) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public abstract boolean addArchiveFile(org.openide.filesystems.FileObject) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public abstract boolean addLibrary(org.netbeans.api.project.libraries.Library) throws java.io.IOException
 anno 0 java.lang.Deprecated()

CLSS public abstract org.netbeans.spi.java.project.classpath.ProjectClassPathModifierImplementation
cons protected init()
meth protected abstract boolean addAntArtifacts(org.netbeans.api.project.ant.AntArtifact[],java.net.URI[],org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth protected abstract boolean addLibraries(org.netbeans.api.project.libraries.Library[],org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth protected abstract boolean addRoots(java.net.URL[],org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth protected abstract boolean removeAntArtifacts(org.netbeans.api.project.ant.AntArtifact[],java.net.URI[],org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth protected abstract boolean removeLibraries(org.netbeans.api.project.libraries.Library[],org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth protected abstract boolean removeRoots(java.net.URL[],org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth protected abstract java.lang.String[] getExtensibleClassPathTypes(org.netbeans.api.project.SourceGroup)
meth protected abstract org.netbeans.api.project.SourceGroup[] getExtensibleSourceGroups()
meth protected boolean addProjects(org.netbeans.api.project.Project[],org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth protected boolean addRoots(java.net.URI[],org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth protected boolean removeRoots(java.net.URI[],org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth protected final java.lang.String performSharabilityHeuristics(java.net.URI,org.netbeans.spi.project.support.ant.AntProjectHelper) throws java.io.IOException,java.net.URISyntaxException
meth protected static java.net.URI[] convertURLsToURIs(java.net.URL[])
supr java.lang.Object
hcls Accessor

CLSS public abstract interface org.netbeans.spi.java.queries.AccessibilityQueryImplementation2
innr public abstract interface static Result
meth public abstract org.netbeans.spi.java.queries.AccessibilityQueryImplementation2$Result isPubliclyAccessible(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.spi.project.ActionProvider
fld public final static java.lang.String COMMAND_BUILD = "build"
fld public final static java.lang.String COMMAND_CLEAN = "clean"
fld public final static java.lang.String COMMAND_COMPILE_SINGLE = "compile.single"
fld public final static java.lang.String COMMAND_COPY = "copy"
fld public final static java.lang.String COMMAND_DEBUG = "debug"
fld public final static java.lang.String COMMAND_DEBUG_SINGLE = "debug.single"
fld public final static java.lang.String COMMAND_DEBUG_STEP_INTO = "debug.stepinto"
fld public final static java.lang.String COMMAND_DEBUG_TEST_SINGLE = "debug.test.single"
fld public final static java.lang.String COMMAND_DELETE = "delete"
fld public final static java.lang.String COMMAND_MOVE = "move"
fld public final static java.lang.String COMMAND_PRIME = "prime"
fld public final static java.lang.String COMMAND_PROFILE = "profile"
fld public final static java.lang.String COMMAND_PROFILE_SINGLE = "profile.single"
fld public final static java.lang.String COMMAND_PROFILE_TEST_SINGLE = "profile.test.single"
fld public final static java.lang.String COMMAND_REBUILD = "rebuild"
fld public final static java.lang.String COMMAND_RENAME = "rename"
fld public final static java.lang.String COMMAND_RUN = "run"
fld public final static java.lang.String COMMAND_RUN_SINGLE = "run.single"
fld public final static java.lang.String COMMAND_TEST = "test"
fld public final static java.lang.String COMMAND_TEST_SINGLE = "test.single"
meth public abstract boolean isActionEnabled(java.lang.String,org.openide.util.Lookup)
meth public abstract java.lang.String[] getSupportedActions()
meth public abstract void invokeAction(java.lang.String,org.openide.util.Lookup)

CLSS public abstract interface org.netbeans.spi.project.ProjectConfiguration
meth public abstract java.lang.String getDisplayName()

CLSS public abstract interface org.netbeans.spi.project.ui.CustomizerProvider
meth public abstract void showCustomizer()

CLSS public abstract interface org.netbeans.spi.project.ui.CustomizerProvider2
intf org.netbeans.spi.project.ui.CustomizerProvider
meth public abstract void showCustomizer(java.lang.String,java.lang.String)

CLSS public abstract interface org.netbeans.spi.project.ui.LogicalViewProvider
intf org.netbeans.spi.project.ui.PathFinder
meth public abstract org.openide.nodes.Node createLogicalView()

CLSS public abstract interface org.netbeans.spi.project.ui.PathFinder
meth public abstract org.openide.nodes.Node findPath(org.openide.nodes.Node,java.lang.Object)

CLSS public abstract interface org.netbeans.spi.project.ui.support.NodeFactory
innr public abstract interface static !annotation Registration
meth public abstract org.netbeans.spi.project.ui.support.NodeList<?> createNodes(org.netbeans.api.project.Project)

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

