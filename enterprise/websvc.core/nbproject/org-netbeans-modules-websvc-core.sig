#Signature file v4.1
#Version 1.67.0

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

CLSS public abstract interface java.awt.datatransfer.Transferable
meth public abstract boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
meth public abstract java.awt.datatransfer.DataFlavor[] getTransferDataFlavors()
meth public abstract java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException

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

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Cloneable

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

CLSS public abstract interface org.netbeans.api.actions.Openable
meth public abstract void open()

CLSS public abstract interface org.netbeans.modules.javaee.injection.spi.InjectionTargetQueryImplementation
meth public abstract boolean isInjectionTarget(org.netbeans.api.java.source.CompilationController,javax.lang.model.element.TypeElement)
meth public abstract boolean isStaticReferenceRequired(org.netbeans.api.java.source.CompilationController,javax.lang.model.element.TypeElement)

CLSS public abstract interface org.netbeans.modules.websvc.api.support.ConfigureHandlerCookie
intf org.openide.nodes.Node$Cookie
meth public abstract void configureHandler()

CLSS public abstract interface org.netbeans.modules.websvc.api.support.InvokeOperationCookie
innr public abstract static ClientSelectionPanel
innr public final static !enum TargetSourceType
meth public abstract org.netbeans.modules.websvc.api.support.InvokeOperationCookie$ClientSelectionPanel getDialogDescriptorPanel()
meth public abstract void invokeOperation(org.openide.util.Lookup,javax.swing.text.JTextComponent)

CLSS public abstract static org.netbeans.modules.websvc.api.support.InvokeOperationCookie$ClientSelectionPanel
 outer org.netbeans.modules.websvc.api.support.InvokeOperationCookie
cons public init()
fld public final static java.lang.String PROPERTY_SELECTION_VALID
meth protected final void setSelectionValid(boolean)
meth public abstract org.openide.util.Lookup getSelectedClient()
supr javax.swing.JPanel
hfds selectionValid

CLSS public abstract org.netbeans.modules.websvc.core.AbstractProjectWebServiceViewImpl
cons protected init(org.netbeans.api.project.Project)
intf org.netbeans.modules.websvc.core.ProjectWebServiceViewImpl
meth protected org.netbeans.api.project.Project getProject()
meth protected void fireChange(org.netbeans.modules.websvc.core.ProjectWebServiceView$ViewType)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public void addChangeListener(javax.swing.event.ChangeListener,org.netbeans.modules.websvc.core.ProjectWebServiceView$ViewType)
meth public void removeChangeListener(javax.swing.event.ChangeListener,org.netbeans.modules.websvc.core.ProjectWebServiceView$ViewType)
supr java.lang.Object
hfds clientListeners,project,serviceListeners

CLSS public org.netbeans.modules.websvc.core.AddWsOperationHelper
cons public init(java.lang.String)
cons public init(java.lang.String,boolean)
meth protected org.netbeans.modules.j2ee.core.api.support.java.method.MethodCustomizer createDialog(org.openide.filesystems.FileObject,org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel) throws java.io.IOException
meth protected org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel getPrototypeMethod()
meth protected org.openide.filesystems.FileObject getDDFile(org.openide.filesystems.FileObject)
meth protected void okButtonPressed(org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel,org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public java.lang.String getTitle()
meth public org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel getMethodModel(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public void addMethod(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
supr java.lang.Object
hfds EMPTY_PATH,createAnnotations,method,name
hcls AddOperationTask,ResultHolder

CLSS public org.netbeans.modules.websvc.core.ClientWizardProperties
cons public init()
fld public final static java.lang.String CLIENT_STUB_TYPE = "clientStubType"
fld public final static java.lang.String JAX_RPC = "JAX-RPC Style"
fld public final static java.lang.String JAX_VERSION = "jaxVersion"
fld public final static java.lang.String JAX_WS = "JAX-WS Style"
fld public final static java.lang.String USEDISPATCH = "useDispatch"
fld public final static java.lang.String WSDL_DOWNLOAD_FILE = "wsdlDownloadedWsdl"
fld public final static java.lang.String WSDL_DOWNLOAD_SCHEMAS = "wsdlDownloadedSchemas"
fld public final static java.lang.String WSDL_DOWNLOAD_URL = "wsdlDownloadUrl"
fld public final static java.lang.String WSDL_FILE_PATH = "wsdlFilePath"
fld public final static java.lang.String WSDL_PACKAGE_NAME = "wsdlPackageName"
fld public final static java.lang.String WSDL_SOURCE = "wsdlSource"
supr java.lang.Object

CLSS public org.netbeans.modules.websvc.core.CreatorProvider
cons public init()
meth public static org.netbeans.modules.websvc.api.support.ClientCreator getClientCreator(org.netbeans.api.project.Project,org.openide.WizardDescriptor)
meth public static org.netbeans.modules.websvc.api.support.ServiceCreator getServiceCreator(org.netbeans.api.project.Project,org.openide.WizardDescriptor)
meth public static org.netbeans.modules.websvc.core.HandlerCreator getHandlerCreator(org.netbeans.api.project.Project,org.openide.WizardDescriptor)
supr java.lang.Object
hfds clientProviders,handlerProviders,serviceProviders

CLSS public abstract interface org.netbeans.modules.websvc.core.HandlerCreator
meth public abstract void createLogicalHandler() throws java.io.IOException
meth public abstract void createMessageHandler() throws java.io.IOException

CLSS public abstract interface org.netbeans.modules.websvc.core.HandlerCreatorProvider
meth public abstract org.netbeans.modules.websvc.core.HandlerCreator getHandlerCreator(org.netbeans.api.project.Project,org.openide.WizardDescriptor)

CLSS public org.netbeans.modules.websvc.core.JaxWsUtils
cons public init()
fld public final static java.lang.String HANDLER_TEMPLATE = "Templates/WebServices/MessageHandler.java"
innr public static WsImportClientFailedMessage
innr public static WsImportServiceFailedMessage
meth public static boolean addProjectReference(org.netbeans.api.project.Project,org.openide.filesystems.FileObject)
meth public static boolean addRelativeWsdlLocation(org.openide.filesystems.FileObject,java.lang.String)
meth public static boolean askForSunJaxWsConfig(org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel)
meth public static boolean hasAnnotation(javax.lang.model.element.Element,java.lang.String)
meth public static boolean hasFqn(javax.lang.model.element.AnnotationMirror,java.lang.String)
meth public static boolean isCarProject(org.netbeans.api.project.Project)
meth public static boolean isCarProject(org.netbeans.modules.websvc.core.ProjectInfo)
meth public static boolean isEjbJavaEE5orHigher(org.netbeans.api.project.Project)
meth public static boolean isEjbJavaEE5orHigher(org.netbeans.modules.websvc.core.ProjectInfo)
meth public static boolean isEjbSupported(org.netbeans.api.project.Project)
meth public static boolean isInSourceGroup(org.netbeans.api.project.Project,java.lang.String)
meth public static boolean isJavaIdentifier(java.lang.String)
meth public static boolean isJavaPackage(java.lang.String)
meth public static boolean isProjectReferenceable(org.netbeans.api.project.Project,org.netbeans.api.project.Project)
meth public static boolean isRPCEncoded(java.net.URI)
meth public static boolean isSoap12(org.openide.filesystems.FileObject)
meth public static boolean needsSoapHandler(org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel)
meth public static java.lang.String getModuleType(org.netbeans.api.project.Project)
meth public static java.lang.String getPackageName(java.lang.String)
meth public static java.lang.String[] createSteps(java.lang.String[],org.openide.WizardDescriptor$Panel[])
meth public static java.util.Map<javax.xml.namespace.QName,java.lang.String> getSoapHandlerParameterTypes(org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel)
meth public static java.util.Map<javax.xml.namespace.QName,java.lang.String> getSoapHandlerParameterTypes(org.netbeans.modules.xml.wsdl.model.PortType)
meth public static javax.lang.model.element.AnnotationMirror getAnnotation(javax.lang.model.element.Element,java.lang.String)
meth public static org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule getJ2eeModule(org.netbeans.api.project.Project)
meth public static org.netbeans.modules.websvc.api.jaxws.project.config.Service findServiceForServiceName(org.openide.filesystems.FileObject,java.lang.String)
meth public static org.openide.filesystems.FileObject createSoapHandler(org.openide.filesystems.FileObject,org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel,java.util.Map<javax.xml.namespace.QName,java.lang.Object>)
meth public static org.openide.filesystems.FileObject createSoapHandler(org.openide.filesystems.FileObject,org.netbeans.modules.xml.wsdl.model.PortType,java.util.Map<javax.xml.namespace.QName,java.lang.Object>) throws java.io.IOException
meth public static org.openide.filesystems.FileObject getSoapHandler(org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel)
meth public static org.openide.loaders.DataObject createDataObjectFromTemplate(java.lang.String,org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public static void generateJaxWsArtifacts(org.netbeans.api.project.Project,org.openide.filesystems.FileObject,java.lang.String,java.net.URL,java.lang.String,java.lang.String) throws java.lang.Exception
meth public static void generateJaxWsImplementationClass(org.netbeans.api.project.Project,org.openide.filesystems.FileObject,java.lang.String,java.net.URL,org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService,org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort,boolean,boolean) throws java.lang.Exception
meth public static void generateJaxWsImplementationClass(org.netbeans.api.project.Project,org.openide.filesystems.FileObject,java.lang.String,org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel,org.netbeans.modules.websvc.api.jaxws.project.config.Service) throws java.lang.Exception
meth public static void openFileInEditor(org.openide.loaders.DataObject)
meth public static void setSOAP12Binding(org.openide.filesystems.FileObject,boolean)
meth public static void setWebMethodAttrValue(org.openide.filesystems.FileObject,org.netbeans.api.java.source.ElementHandle<?>,java.lang.String,java.lang.String)
meth public static void setWebParamAttrValue(org.openide.filesystems.FileObject,org.netbeans.api.java.source.ElementHandle<?>,java.lang.String,java.lang.String,java.lang.String)
meth public static void setWebServiceAttrValue(org.openide.filesystems.FileObject,java.lang.String,java.lang.String)
supr java.lang.Object
hfds BINDING_TYPE_ANNOTATION,OLD_SOAP12_NAMESPACE,SOAP12_NAMESPACE,jsr109Supported,projectType

CLSS public static org.netbeans.modules.websvc.core.JaxWsUtils$WsImportClientFailedMessage
 outer org.netbeans.modules.websvc.core.JaxWsUtils
cons public init(java.lang.Throwable)
supr org.openide.NotifyDescriptor$Message

CLSS public static org.netbeans.modules.websvc.core.JaxWsUtils$WsImportServiceFailedMessage
 outer org.netbeans.modules.websvc.core.JaxWsUtils
cons public init(java.lang.Throwable)
supr org.openide.NotifyDescriptor$Message

CLSS public abstract interface org.netbeans.modules.websvc.core.MessageReceiver

CLSS public org.netbeans.modules.websvc.core.MethodGenerator
cons public init(org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel,org.openide.filesystems.FileObject)
meth public static void deleteMethod(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public static void removeMethod(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public void generateMethod(java.lang.String) throws java.io.IOException
supr java.lang.Object
hfds implClassFo,wsdlModel

CLSS public org.netbeans.modules.websvc.core.MethodVisitor
cons public init(org.netbeans.api.java.source.CompilationInfo)
meth public boolean hasPublicMethod()
meth public boolean hasWebMethod()
meth public java.util.List<javax.lang.model.element.ExecutableElement> getPublicMethods()
meth public javax.lang.model.element.ExecutableElement getMethod(java.lang.String)
supr java.lang.Object
hfds hasPublicMethod,hasWebMethod,info,method,operationName,publicMethods
hcls JavaMethodVisitor,PublicMethodVisitor,WebMethodVisitor

CLSS public org.netbeans.modules.websvc.core.ProjectClientView
cons public init()
meth public static org.openide.nodes.Node[] createClientView(org.netbeans.api.project.Project)
supr java.lang.Object
hfds clientViewProviders

CLSS public abstract interface org.netbeans.modules.websvc.core.ProjectClientViewProvider
meth public abstract org.openide.nodes.Node createClientView(org.netbeans.api.project.Project)

CLSS public org.netbeans.modules.websvc.core.ProjectInfo
cons public init(org.netbeans.api.project.Project)
fld public final static int CAR_PROJECT_TYPE = 3
fld public final static int EJB_PROJECT_TYPE = 2
fld public final static int JSE_PROJECT_TYPE = 0
fld public final static int WEB_PROJECT_TYPE = 1
meth public boolean isJsr109Supported()
meth public boolean isJsr109oldSupported()
meth public boolean isWsgenSupported()
meth public boolean isWsimportSupported()
meth public int getProjectType()
meth public org.netbeans.api.project.Project getProject()
meth public org.netbeans.modules.websvc.core.ServerType getServerType()
supr java.lang.Object
hfds jsr109Supported,jsr109oldSupported,project,projectType,serverType,wsgenSupported,wsimportSupported

CLSS public org.netbeans.modules.websvc.core.ProjectWebServiceNodeFactory
cons public init()
intf org.netbeans.spi.project.ui.support.NodeFactory
meth public org.netbeans.spi.project.ui.support.NodeList<org.netbeans.modules.websvc.core.ProjectWebServiceView$ViewType> createNodes(org.netbeans.api.project.Project)
meth public static org.netbeans.modules.websvc.core.ProjectWebServiceNodeFactory ejbproject()
meth public static org.netbeans.modules.websvc.core.ProjectWebServiceNodeFactory j2eeClientProject()
meth public static org.netbeans.modules.websvc.core.ProjectWebServiceNodeFactory j2seproject()
supr java.lang.Object
hcls Pair,WSChildrenFactory,WSRootNode,WsClientPrivilegedTemplates,WsNodeList,WsPrivilegedTemplates

CLSS public final org.netbeans.modules.websvc.core.ProjectWebServiceView
innr public final static !enum ViewType
meth public org.openide.nodes.Node[] getClientNodes(org.netbeans.api.project.Project)
meth public org.openide.nodes.Node[] getServiceNodes(org.netbeans.api.project.Project)
supr java.lang.Object
hfds clientListener,clientListeners,implementations,impls,project,serviceListener,serviceListeners
hcls ChangeListenerDelegate

CLSS public final static !enum org.netbeans.modules.websvc.core.ProjectWebServiceView$ViewType
 outer org.netbeans.modules.websvc.core.ProjectWebServiceView
fld public final static org.netbeans.modules.websvc.core.ProjectWebServiceView$ViewType CLIENT
fld public final static org.netbeans.modules.websvc.core.ProjectWebServiceView$ViewType SERVICE
meth public static org.netbeans.modules.websvc.core.ProjectWebServiceView$ViewType valueOf(java.lang.String)
meth public static org.netbeans.modules.websvc.core.ProjectWebServiceView$ViewType[] values()
supr java.lang.Enum<org.netbeans.modules.websvc.core.ProjectWebServiceView$ViewType>

CLSS public abstract interface org.netbeans.modules.websvc.core.ProjectWebServiceViewImpl
meth public abstract boolean isViewEmpty(org.netbeans.modules.websvc.core.ProjectWebServiceView$ViewType)
meth public abstract org.openide.nodes.Node[] createView(org.netbeans.modules.websvc.core.ProjectWebServiceView$ViewType)
meth public abstract void addChangeListener(javax.swing.event.ChangeListener,org.netbeans.modules.websvc.core.ProjectWebServiceView$ViewType)
meth public abstract void addNotify()
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener,org.netbeans.modules.websvc.core.ProjectWebServiceView$ViewType)
meth public abstract void removeNotify()

CLSS public abstract interface org.netbeans.modules.websvc.core.ProjectWebServiceViewProvider
meth public abstract org.netbeans.modules.websvc.core.ProjectWebServiceViewImpl createProjectWebServiceView(org.netbeans.api.project.Project)

CLSS public final !enum org.netbeans.modules.websvc.core.ServerType
fld public final static org.netbeans.modules.websvc.core.ServerType GLASSFISH
fld public final static org.netbeans.modules.websvc.core.ServerType GLASSFISH_V3
fld public final static org.netbeans.modules.websvc.core.ServerType JBOSS
fld public final static org.netbeans.modules.websvc.core.ServerType NOT_SPECIFIED
fld public final static org.netbeans.modules.websvc.core.ServerType TOMCAT
fld public final static org.netbeans.modules.websvc.core.ServerType UNKNOWN
fld public final static org.netbeans.modules.websvc.core.ServerType WEBLOGIC
fld public final static org.netbeans.modules.websvc.core.ServerType WEBSPHERE
meth public static org.netbeans.modules.websvc.core.ServerType valueOf(java.lang.String)
meth public static org.netbeans.modules.websvc.core.ServerType[] values()
supr java.lang.Enum<org.netbeans.modules.websvc.core.ServerType>

CLSS public abstract interface org.netbeans.modules.websvc.core.ServiceNodesProvider
meth public abstract org.openide.nodes.Node[] getServiceNodes(org.netbeans.api.project.Project)

CLSS public org.netbeans.modules.websvc.core.WSInjectiontargetQueryImplementation
cons public init()
intf org.netbeans.modules.javaee.injection.spi.InjectionTargetQueryImplementation
meth public boolean isInjectionTarget(org.netbeans.api.java.source.CompilationController,javax.lang.model.element.TypeElement)
meth public boolean isStaticReferenceRequired(org.netbeans.api.java.source.CompilationController,javax.lang.model.element.TypeElement)
supr java.lang.Object

CLSS public org.netbeans.modules.websvc.core.WSStackUtils
cons public init(org.netbeans.api.project.Project)
innr public final static !enum ErrorType
innr public final static !enum WizardType
innr public static ErrorMessage
meth public <%0 extends java.lang.Object> org.netbeans.modules.websvc.wsstack.api.WSStack<{%%0}> getWsStack(java.lang.Class<{%%0}>)
meth public boolean hasJAXWSLibrary()
meth public boolean isJsr109OldSupported()
meth public boolean isJsr109Supported()
meth public boolean isWsitSupported()
meth public org.netbeans.modules.websvc.core.ServerType getServerType()
meth public org.netbeans.modules.websvc.core.WSStackUtils$ErrorMessage getErrorMessage(org.netbeans.modules.websvc.core.WSStackUtils$WizardType)
meth public static org.netbeans.modules.websvc.core.ServerType getServerType(org.netbeans.api.project.Project)
supr java.lang.Object
hfds j2eePlatform,project

CLSS public static org.netbeans.modules.websvc.core.WSStackUtils$ErrorMessage
 outer org.netbeans.modules.websvc.core.WSStackUtils
cons public init(org.netbeans.modules.websvc.core.WSStackUtils$ErrorType,java.lang.String)
cons public init(org.netbeans.modules.websvc.core.WSStackUtils$ErrorType,java.lang.String,boolean)
meth public boolean isSerious()
meth public java.lang.String getText()
meth public java.lang.String getWizardMessageProperty()
meth public org.netbeans.modules.websvc.core.WSStackUtils$ErrorType getType()
supr java.lang.Object
hfds serious,text,type

CLSS public final static !enum org.netbeans.modules.websvc.core.WSStackUtils$ErrorType
 outer org.netbeans.modules.websvc.core.WSStackUtils
fld public final static org.netbeans.modules.websvc.core.WSStackUtils$ErrorType ERROR
fld public final static org.netbeans.modules.websvc.core.WSStackUtils$ErrorType INFO
fld public final static org.netbeans.modules.websvc.core.WSStackUtils$ErrorType WARNING
meth public static org.netbeans.modules.websvc.core.WSStackUtils$ErrorType valueOf(java.lang.String)
meth public static org.netbeans.modules.websvc.core.WSStackUtils$ErrorType[] values()
supr java.lang.Enum<org.netbeans.modules.websvc.core.WSStackUtils$ErrorType>

CLSS public final static !enum org.netbeans.modules.websvc.core.WSStackUtils$WizardType
 outer org.netbeans.modules.websvc.core.WSStackUtils
fld public final static org.netbeans.modules.websvc.core.WSStackUtils$WizardType WS
fld public final static org.netbeans.modules.websvc.core.WSStackUtils$WizardType WS_CLIENT
fld public final static org.netbeans.modules.websvc.core.WSStackUtils$WizardType WS_FROM_WSDL
meth public static org.netbeans.modules.websvc.core.WSStackUtils$WizardType valueOf(java.lang.String)
meth public static org.netbeans.modules.websvc.core.WSStackUtils$WizardType[] values()
supr java.lang.Enum<org.netbeans.modules.websvc.core.WSStackUtils$WizardType>

CLSS public org.netbeans.modules.websvc.core.WebServiceActionProvider
cons public init()
meth public static org.netbeans.modules.websvc.api.support.AddOperationCookie getAddOperationAction(org.openide.filesystems.FileObject)
meth public static org.netbeans.modules.websvc.api.support.InvokeOperationCookie getInvokeOperationAction(org.openide.filesystems.FileObject)
supr java.lang.Object
hfds addOperationActionProviders,invokeOperationActionProviders

CLSS public org.netbeans.modules.websvc.core.WebServiceReference
cons public init(java.net.URL,java.lang.String,java.lang.String)
meth public java.lang.String getModuleName()
meth public java.lang.String getWebServiceName()
meth public java.net.URL getWsdlURL()
supr java.lang.Object
hfds moduleName,webServiceName,wsdlURL

CLSS public org.netbeans.modules.websvc.core.WebServiceTransferable
cons public init(org.netbeans.modules.websvc.core.WebServiceReference)
fld public final static java.awt.datatransfer.DataFlavor WS_FLAVOR
meth protected java.lang.Object getData() throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
meth public boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
meth public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors()
meth public java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
supr org.openide.util.datatransfer.ExTransferable$Single
hfds ref

CLSS public abstract interface org.netbeans.modules.websvc.core.WsWsdlCookie
intf org.openide.nodes.Node$Cookie
meth public abstract java.lang.String getWsdlURL()

CLSS public org.netbeans.modules.websvc.core.WsdlRetriever
cons public init(org.netbeans.modules.websvc.core.WsdlRetriever$MessageReceiver,java.lang.String)
fld public final static int STATUS_BAD_WSDL = 6
fld public final static int STATUS_COMPLETE = 3
fld public final static int STATUS_CONNECTING = 1
fld public final static int STATUS_DOWNLOADING = 2
fld public final static int STATUS_FAILED = 4
fld public final static int STATUS_START = 0
fld public final static int STATUS_TERMINATED = 5
innr public abstract interface static MessageReceiver
innr public static SchemaInfo
intf java.lang.Runnable
meth public byte[] getWsdl()
meth public int getState()
meth public java.lang.String getWsdlFileName()
meth public java.lang.String getWsdlUrl()
meth public java.util.List<org.netbeans.modules.websvc.core.WsdlRetriever$SchemaInfo> getSchemas()
meth public static java.lang.String beautifyUrlName(java.lang.String)
meth public void run()
meth public void stopRetrieval()
supr java.lang.Object
hfds STATUS_MESSAGE,connection,in,receiver,schemas,shutdown,status,wsdlContent,wsdlFileName,wsdlUrl,wsdlUrlName
hcls Chunk,MessageSender,ServiceNameParser,WsdlInfo

CLSS public abstract interface static org.netbeans.modules.websvc.core.WsdlRetriever$MessageReceiver
 outer org.netbeans.modules.websvc.core.WsdlRetriever
meth public abstract void setWsdlDownloadMessage(java.lang.String)

CLSS public static org.netbeans.modules.websvc.core.WsdlRetriever$SchemaInfo
 outer org.netbeans.modules.websvc.core.WsdlRetriever
meth public byte[] getSchemaContent()
meth public java.lang.String getSchemaName()
supr java.lang.Object
hfds schemaContent,schemaName

CLSS public org.netbeans.modules.websvc.core.jaxws.nodes.ClientHandlerButtonListener
cons public init(org.netbeans.modules.websvc.spi.support.MessageHandlerPanel,org.netbeans.modules.websvc.api.jaxws.bindings.BindingsModel,org.netbeans.modules.websvc.api.jaxws.project.config.Client,org.openide.nodes.Node,org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel)
intf java.awt.event.ActionListener
meth public void actionPerformed(java.awt.event.ActionEvent)
supr java.lang.Object
hfds bindingsHandlerFile,bindingsModel,client,jaxWsModel,node,panel

CLSS public org.netbeans.modules.websvc.core.jaxws.nodes.HandlerButtonListener
cons public init(org.netbeans.modules.websvc.spi.support.MessageHandlerPanel,org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChains,org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,org.netbeans.modules.websvc.api.jaxws.project.config.Service,boolean)
intf java.awt.event.ActionListener
meth public void actionPerformed(java.awt.event.ActionEvent)
supr java.lang.Object
hfds chain,handlerChains,handlerFO,implBeanClass,isNew,panel,service

CLSS public org.netbeans.modules.websvc.core.jaxws.nodes.JaxWsChildren
cons public init(org.netbeans.modules.websvc.api.jaxws.project.config.Service,org.openide.filesystems.FileObject,org.openide.filesystems.FileObject)
meth protected org.openide.nodes.Node[] createNodes(java.lang.Object)
meth protected void addNotify()
meth protected void removeNotify()
supr org.openide.nodes.Children$Keys<java.lang.Object>
hfds OPERATION_ICON,OPERATION_INFO_COMPARATOR,cachedIcon,fcl,implClass,modelGenerationFinished,requestProcessor,service,srcRoot,wsdlChangeListener,wsdlModel,wsdlModeler
hcls WebOperationInfo,WebOperationInfoComparator

CLSS public org.netbeans.modules.websvc.core.jaxws.nodes.JaxWsClientChildren
cons public init(org.netbeans.modules.websvc.api.jaxws.project.config.Client,org.openide.filesystems.FileObject)
meth protected org.openide.nodes.Node[] createNodes(org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService)
meth protected void addNotify()
meth protected void removeNotify()
supr org.openide.nodes.Children$Keys<org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService>
hfds client,srcRoot,wsdlModel

CLSS public org.netbeans.modules.websvc.core.jaxws.nodes.JaxWsClientNode
cons public init(org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel,org.netbeans.modules.websvc.api.jaxws.project.config.Client,org.openide.filesystems.FileObject)
fld public final static java.lang.String CONTEXT = "context"
intf org.netbeans.modules.websvc.api.support.ConfigureHandlerCookie
intf org.netbeans.modules.websvc.jaxws.api.JaxWsRefreshCookie
intf org.openide.cookies.OpenCookie
meth public boolean canDestroy()
meth public java.awt.Image getIcon(int)
meth public java.awt.Image getOpenedIcon(int)
meth public java.lang.String getShortDescription()
meth public javax.swing.Action getPreferredAction()
meth public javax.swing.Action[] getActions(boolean)
meth public org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel getWsdlModel()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void configureHandler()
meth public void destroy() throws java.io.IOException
meth public void open()
meth public void refreshService(boolean)
supr org.openide.nodes.AbstractNode
hfds ERROR_BADGE,SERVICE_BADGE,WAITING_BADGE,cachedErrorBadge,cachedServiceBadge,cachedWaitingBadge,client,content,jaxWsModel,modelGenerationFinished,srcRoot,wsdlFileObject

CLSS public org.netbeans.modules.websvc.core.jaxws.nodes.JaxWsClientRootChildren
cons public init(org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel,org.openide.filesystems.FileObject)
meth protected org.openide.nodes.Node[] createNodes(org.netbeans.modules.websvc.api.jaxws.project.config.Client)
meth protected void addNotify()
meth protected void removeNotify()
supr org.openide.nodes.Children$Keys<org.netbeans.modules.websvc.api.jaxws.project.config.Client>
hfds JAX_WS_CLIENT_ROOT_RP,clients,jaxWsModel,listener,srcRoot,updateNodeTask
hcls JaxWsListener

CLSS public org.netbeans.modules.websvc.core.jaxws.nodes.JaxWsClientRootNode
cons public init(org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel,org.openide.filesystems.FileObject)
meth public java.awt.Image getIcon(int)
meth public java.awt.Image getOpenedIcon(int)
meth public javax.swing.Action[] getActions(boolean)
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.nodes.AbstractNode
hfds SERVICES_BADGE,cachedServicesBadge,folderIconCache,openedFolderIconCache

CLSS public org.netbeans.modules.websvc.core.jaxws.nodes.JaxWsNode
cons public init(org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel,org.netbeans.modules.websvc.api.jaxws.project.config.Service,org.openide.filesystems.FileObject,org.openide.filesystems.FileObject)
intf org.netbeans.modules.websvc.api.support.ConfigureHandlerCookie
intf org.netbeans.modules.websvc.core.WsWsdlCookie
intf org.netbeans.modules.websvc.jaxws.api.JaxWsTesterCookie
meth public boolean canCopy()
meth public boolean canCut()
meth public boolean canDestroy()
meth public java.awt.Image getIcon(int)
meth public java.awt.Image getOpenedIcon(int)
meth public java.awt.datatransfer.Transferable clipboardCopy() throws java.io.IOException
meth public java.lang.Object getValue(java.lang.String)
meth public java.lang.String getDisplayName()
meth public java.lang.String getTesterPageURL()
meth public java.lang.String getWsdlURL()
meth public javax.swing.Action getPreferredAction()
meth public javax.swing.Action[] getActions(boolean)
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void configureHandler()
meth public void destroy() throws java.io.IOException
supr org.openide.nodes.AbstractNode
hfds ERROR_BADGE,RP,SERVICE_BADGE,WAITING_BADGE,cachedErrorBadge,cachedServiceBadge,cachedWaitingBadge,content,implBeanClass,implClassModifiedTask,jaxWsModel,project,service,srcRoot
hcls RefreshServiceImpl,ServerContextInfo,ServiceInfo

CLSS public org.netbeans.modules.websvc.core.jaxws.nodes.JaxWsRootChildren
cons public init(org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel,org.openide.filesystems.FileObject[])
meth protected org.openide.nodes.Node[] createNodes(org.netbeans.modules.websvc.api.jaxws.project.config.Service)
meth protected void addNotify()
meth protected void removeNotify()
supr org.openide.nodes.Children$Keys<org.netbeans.modules.websvc.api.jaxws.project.config.Service>
hfds JAX_WS_ROOT_RP,jaxWsModel,listener,services,srcRoots,updateNodeTask
hcls JaxWsListener

CLSS public org.netbeans.modules.websvc.core.jaxws.nodes.JaxWsRootNode
cons public init(org.netbeans.api.project.Project,org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel,org.openide.filesystems.FileObject[])
intf java.beans.PropertyChangeListener
meth public java.awt.Image getIcon(int)
meth public java.awt.Image getOpenedIcon(int)
meth public javax.swing.Action[] getActions(boolean)
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void propertyChange(java.beans.PropertyChangeEvent)
supr org.openide.nodes.AbstractNode
hfds SERVICES_BADGE,cachedServicesBadge,evaluator,folderIconCache,jsr109Supported,openedFolderIconCache,project

CLSS public org.netbeans.modules.websvc.core.jaxws.nodes.OperationEditorDrop
cons public init(org.netbeans.modules.websvc.core.jaxws.nodes.OperationNode)
intf org.openide.text.ActiveEditorDrop
meth public boolean handleTransfer(javax.swing.text.JTextComponent)
supr java.lang.Object
hfds operationNode

CLSS public org.netbeans.modules.websvc.core.jaxws.nodes.OperationNode
cons public init(org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation)
meth public boolean canDestroy()
meth public java.awt.Image getIcon(int)
meth public java.awt.Image getOpenedIcon(int)
meth public java.awt.datatransfer.Transferable clipboardCopy() throws java.io.IOException
meth public javax.swing.Action[] getActions(boolean)
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.nodes.AbstractNode
hfds editorDrop,operation,srcRoot
hcls ActiveEditorDropTransferable

CLSS public org.netbeans.modules.websvc.core.jaxws.nodes.PortChildren
cons public init(org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort)
meth protected org.openide.nodes.Node[] createNodes(org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation)
meth protected void addNotify()
meth protected void removeNotify()
supr org.openide.nodes.Children$Keys<org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation>
hfds wsdlPort

CLSS public org.netbeans.modules.websvc.core.jaxws.nodes.PortNode
cons public init(org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort)
meth public boolean canDestroy()
meth public java.awt.Image getIcon(int)
meth public java.awt.Image getOpenedIcon(int)
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.nodes.AbstractNode

CLSS public org.netbeans.modules.websvc.core.jaxws.nodes.ProjectJaxWsWebServiceViewProvider
cons public init()
intf org.netbeans.modules.websvc.core.ProjectWebServiceViewProvider
meth public org.netbeans.modules.websvc.core.ProjectWebServiceViewImpl createProjectWebServiceView(org.netbeans.api.project.Project)
supr java.lang.Object

CLSS public org.netbeans.modules.websvc.core.jaxws.nodes.RefreshClientDialog
meth public static java.lang.String open(java.lang.String)
supr javax.swing.JPanel
hfds CLOSE,DOWNLOAD_WSDL_ON_REFRESH,NO_DOWNLOAD,downloadWsdlCheckBox,jLabel1,jLabel2,jTextField1,url

CLSS public org.netbeans.modules.websvc.core.jaxws.nodes.RefreshWsDialog
meth public static java.lang.String open(boolean,java.lang.String,java.lang.String)
meth public static java.lang.String openWithOKButtonOnly(boolean,java.lang.String,java.lang.String)
supr javax.swing.JPanel
hfds CLOSE,DOWNLOAD_WSDL,DO_ALL,DO_NOTHING,REGENERATE_IMPL_CLASS,downloadWsdl,downloadWsdlCheckBox,implClass,jLabel1,jLabel2,jLabel3,jTextField1,regenerateCheckBox,url

CLSS public org.netbeans.modules.websvc.core.jaxws.nodes.ServiceChildren
cons public init(org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService)
meth protected org.openide.nodes.Node[] createNodes(org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort)
meth protected void addNotify()
meth protected void removeNotify()
supr org.openide.nodes.Children$Keys<org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort>
hfds wsdlService

CLSS public org.netbeans.modules.websvc.core.jaxws.nodes.ServiceNode
cons public init(org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService)
meth public boolean canDestroy()
meth public java.awt.Image getIcon(int)
meth public java.awt.Image getOpenedIcon(int)
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.nodes.AbstractNode

CLSS public abstract interface org.netbeans.modules.websvc.core.jaxws.policies.JaxWsPoliciesCodeGenerator
meth public abstract boolean isApplicable(java.util.Set<java.lang.String>,org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform,org.openide.filesystems.FileObject)
meth public abstract com.sun.source.tree.ExpressionTree createSecurityFeatureInitializer(org.netbeans.api.java.source.WorkingCopy,org.netbeans.api.java.source.TreeMaker,java.lang.String)
meth public abstract com.sun.source.tree.Tree createSecurityFeatureType(org.netbeans.api.java.source.WorkingCopy,org.netbeans.api.java.source.TreeMaker)
meth public abstract java.lang.String generatePolicyAccessCode(java.util.Set<java.lang.String>,org.netbeans.modules.websvc.api.jaxws.project.config.Client,java.lang.StringBuilder)
meth public abstract java.util.Collection<java.lang.String> getRequiredClasses(java.lang.String)
meth public abstract void modifySecurityFeatureAttribute(com.sun.source.tree.VariableTree,org.netbeans.api.java.source.WorkingCopy,org.netbeans.api.java.source.TreeMaker,java.lang.String,java.util.Set<java.lang.String>)

CLSS public org.netbeans.modules.websvc.core.webservices.ui.DeleteWsDialog
fld public final static java.lang.String DELETE_ALL = "deleteALL"
fld public final static java.lang.String DELETE_NOTHING = "deleteNothing"
fld public final static java.lang.String DELETE_PACKAGE = "deletePackage"
fld public final static java.lang.String DELETE_WS = "deleteWebService"
fld public final static java.lang.String DELETE_WSDL = "deleteWsdl"
meth public static java.lang.String open(java.lang.String,java.lang.String,java.lang.String)
supr javax.swing.JPanel
hfds deletePackageCheckBox,deleteWsdlCheckBox,jLabel1,packageName,wsName,wsdlName

CLSS public org.netbeans.modules.websvc.core.webservices.ui.panels.ClientExplorerPanel
cons public init(org.openide.filesystems.FileObject)
meth protected boolean isClientNode(org.openide.nodes.Node)
meth protected org.openide.nodes.Node getRootContext()
meth public void addNotify()
supr org.netbeans.modules.websvc.spi.support.DefaultClientSelectionPanel
hfds explorerClientRoot,projectNodeList,projects,rootChildren
hcls NoServicesNode,ProjectNode

CLSS public org.netbeans.modules.websvc.core.webservices.ui.panels.EnterWSDLUrlPanel
cons public init(java.lang.String)
meth public java.lang.String getSelectedWSDLUrl()
supr javax.swing.JPanel
hfds defaultWSDLUrl,inputLabel,wsdlURLComboBox

CLSS public org.netbeans.modules.websvc.core.webservices.ui.panels.ProjectFileExplorer
cons public init()
intf java.beans.PropertyChangeListener
intf org.openide.explorer.ExplorerManager$Provider
meth public boolean dontCopy()
meth public boolean isOptimizedDrawingEnabled()
meth public org.openide.explorer.ExplorerManager getExplorerManager()
meth public org.openide.loaders.DataObject getSelectedFolder()
meth public void addNotify()
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void removeNotify()
meth public void setDescriptor(org.openide.DialogDescriptor)
supr javax.swing.JPanel
hfds descriptor,dontCopyCB,eventBlocker,explorerClientRoot,jLblTreeView,manager,projectNodeList,projects,rootChildren,selectedFolder,treeView

CLSS public abstract interface org.netbeans.modules.websvc.jaxws.api.JaxWsRefreshCookie
intf org.openide.nodes.Node$Cookie
meth public abstract void refreshService(boolean)

CLSS public abstract interface org.netbeans.modules.websvc.jaxws.api.JaxWsTesterCookie
intf org.openide.nodes.Node$Cookie
meth public abstract java.lang.String getTesterPageURL()

CLSS public abstract org.netbeans.modules.websvc.spi.support.DefaultClientSelectionPanel
cons public init(org.openide.filesystems.FileObject)
intf org.openide.explorer.ExplorerManager$Provider
meth protected abstract boolean isClientNode(org.openide.nodes.Node)
meth protected abstract org.openide.nodes.Node getRootContext()
meth protected org.openide.explorer.view.BeanTreeView getTreeView()
meth public final org.openide.filesystems.FileObject getTargetFile()
meth public org.openide.explorer.ExplorerManager getExplorerManager()
meth public org.openide.util.Lookup getSelectedClient()
meth public void addNotify()
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void removeNotify()
supr org.netbeans.modules.websvc.api.support.InvokeOperationCookie$ClientSelectionPanel
hfds jLblTreeView,manager,nodeChangeListener,selectedMethod,targetFile,treeView
hcls NodeChangeListener

CLSS public abstract interface org.netbeans.spi.project.ui.support.NodeFactory
innr public abstract interface static !annotation Registration
meth public abstract org.netbeans.spi.project.ui.support.NodeList<?> createNodes(org.netbeans.api.project.Project)

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

CLSS public static org.openide.NotifyDescriptor$Message
 outer org.openide.NotifyDescriptor
cons public init(java.lang.Object)
cons public init(java.lang.Object,int)
supr org.openide.NotifyDescriptor

CLSS public abstract interface org.openide.cookies.OpenCookie
intf org.netbeans.api.actions.Openable
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

CLSS public static org.openide.nodes.Children$Array
 outer org.openide.nodes.Children
cons protected init(java.util.Collection<org.openide.nodes.Node>)
cons public init()
fld protected java.util.Collection<org.openide.nodes.Node> nodes
intf java.lang.Cloneable
meth protected final void refresh()
meth protected java.util.Collection<org.openide.nodes.Node> initCollection()
meth public boolean add(org.openide.nodes.Node[])
meth public boolean remove(org.openide.nodes.Node[])
meth public java.lang.Object clone()
supr org.openide.nodes.Children
hfds COLLECTION_LOCK,nodesEntry
hcls AE

CLSS public abstract static org.openide.nodes.Children$Keys<%0 extends java.lang.Object>
 outer org.openide.nodes.Children
cons protected init(boolean)
cons public init()
meth protected abstract org.openide.nodes.Node[] createNodes({org.openide.nodes.Children$Keys%0})
meth protected final void refreshKey({org.openide.nodes.Children$Keys%0})
meth protected final void setBefore(boolean)
meth protected final void setKeys(java.util.Collection<? extends {org.openide.nodes.Children$Keys%0}>)
meth protected final void setKeys({org.openide.nodes.Children$Keys%0}[])
meth protected void destroyNodes(org.openide.nodes.Node[])
meth public boolean add(org.openide.nodes.Node[])
 anno 0 java.lang.Deprecated()
meth public boolean remove(org.openide.nodes.Node[])
 anno 0 java.lang.Deprecated()
meth public java.lang.Object clone()
supr org.openide.nodes.Children$Array
hfds before,lastRuns
hcls KE

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

CLSS public abstract interface org.openide.text.ActiveEditorDrop
fld public final static java.awt.datatransfer.DataFlavor FLAVOR
meth public abstract boolean handleTransfer(javax.swing.text.JTextComponent)

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

CLSS public org.openide.util.datatransfer.ExTransferable
fld public final static java.awt.datatransfer.DataFlavor multiFlavor
fld public final static java.awt.datatransfer.Transferable EMPTY
innr public abstract static Single
innr public static Multi
intf java.awt.datatransfer.Transferable
meth public boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
meth public final void addTransferListener(org.openide.util.datatransfer.TransferListener)
meth public final void removeTransferListener(org.openide.util.datatransfer.TransferListener)
meth public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors()
meth public java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
meth public static org.openide.util.datatransfer.ExTransferable create(java.awt.datatransfer.Transferable)
meth public void put(org.openide.util.datatransfer.ExTransferable$Single)
meth public void remove(java.awt.datatransfer.DataFlavor)
supr java.lang.Object
hfds listeners,map
hcls Empty

CLSS public abstract static org.openide.util.datatransfer.ExTransferable$Single
 outer org.openide.util.datatransfer.ExTransferable
cons public init(java.awt.datatransfer.DataFlavor)
intf java.awt.datatransfer.Transferable
meth protected abstract java.lang.Object getData() throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
meth public boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
meth public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors()
meth public java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
supr java.lang.Object
hfds flavor

