#Signature file v4.1
#Version 2.162.0

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

CLSS public abstract interface java.lang.Cloneable

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

CLSS public javax.swing.JTable
cons public init()
cons public init(int,int)
cons public init(java.lang.Object[][],java.lang.Object[])
cons public init(java.util.Vector,java.util.Vector)
cons public init(javax.swing.table.TableModel)
cons public init(javax.swing.table.TableModel,javax.swing.table.TableColumnModel)
cons public init(javax.swing.table.TableModel,javax.swing.table.TableColumnModel,javax.swing.ListSelectionModel)
fld protected boolean autoCreateColumnsFromModel
fld protected boolean cellSelectionEnabled
fld protected boolean rowSelectionAllowed
fld protected boolean showHorizontalLines
fld protected boolean showVerticalLines
fld protected int autoResizeMode
fld protected int editingColumn
fld protected int editingRow
fld protected int rowHeight
fld protected int rowMargin
fld protected java.awt.Color gridColor
fld protected java.awt.Color selectionBackground
fld protected java.awt.Color selectionForeground
fld protected java.awt.Component editorComp
fld protected java.awt.Dimension preferredViewportSize
fld protected java.util.Hashtable defaultEditorsByColumnClass
fld protected java.util.Hashtable defaultRenderersByColumnClass
fld protected javax.swing.ListSelectionModel selectionModel
fld protected javax.swing.table.JTableHeader tableHeader
fld protected javax.swing.table.TableCellEditor cellEditor
fld protected javax.swing.table.TableColumnModel columnModel
fld protected javax.swing.table.TableModel dataModel
fld public final static int AUTO_RESIZE_ALL_COLUMNS = 4
fld public final static int AUTO_RESIZE_LAST_COLUMN = 3
fld public final static int AUTO_RESIZE_NEXT_COLUMN = 1
fld public final static int AUTO_RESIZE_OFF = 0
fld public final static int AUTO_RESIZE_SUBSEQUENT_COLUMNS = 2
innr protected AccessibleJTable
innr public final static !enum PrintMode
innr public final static DropLocation
intf javax.accessibility.Accessible
intf javax.swing.Scrollable
intf javax.swing.event.CellEditorListener
intf javax.swing.event.ListSelectionListener
intf javax.swing.event.RowSorterListener
intf javax.swing.event.TableColumnModelListener
intf javax.swing.event.TableModelListener
meth protected boolean processKeyBinding(javax.swing.KeyStroke,java.awt.event.KeyEvent,int,boolean)
meth protected java.lang.String paramString()
meth protected javax.swing.ListSelectionModel createDefaultSelectionModel()
meth protected javax.swing.table.JTableHeader createDefaultTableHeader()
meth protected javax.swing.table.TableColumnModel createDefaultColumnModel()
meth protected javax.swing.table.TableModel createDefaultDataModel()
meth protected void configureEnclosingScrollPane()
meth protected void createDefaultEditors()
meth protected void createDefaultRenderers()
meth protected void initializeLocalVars()
meth protected void resizeAndRepaint()
meth protected void unconfigureEnclosingScrollPane()
meth public boolean editCellAt(int,int)
meth public boolean editCellAt(int,int,java.util.EventObject)
meth public boolean getAutoCreateColumnsFromModel()
meth public boolean getAutoCreateRowSorter()
meth public boolean getCellSelectionEnabled()
meth public boolean getColumnSelectionAllowed()
meth public boolean getDragEnabled()
meth public boolean getFillsViewportHeight()
meth public boolean getRowSelectionAllowed()
meth public boolean getScrollableTracksViewportHeight()
meth public boolean getScrollableTracksViewportWidth()
meth public boolean getShowHorizontalLines()
meth public boolean getShowVerticalLines()
meth public boolean getSurrendersFocusOnKeystroke()
meth public boolean getUpdateSelectionOnSort()
meth public boolean isCellEditable(int,int)
meth public boolean isCellSelected(int,int)
meth public boolean isColumnSelected(int)
meth public boolean isEditing()
meth public boolean isRowSelected(int)
meth public boolean print() throws java.awt.print.PrinterException
meth public boolean print(javax.swing.JTable$PrintMode) throws java.awt.print.PrinterException
meth public boolean print(javax.swing.JTable$PrintMode,java.text.MessageFormat,java.text.MessageFormat) throws java.awt.print.PrinterException
meth public boolean print(javax.swing.JTable$PrintMode,java.text.MessageFormat,java.text.MessageFormat,boolean,javax.print.attribute.PrintRequestAttributeSet,boolean) throws java.awt.print.PrinterException
meth public boolean print(javax.swing.JTable$PrintMode,java.text.MessageFormat,java.text.MessageFormat,boolean,javax.print.attribute.PrintRequestAttributeSet,boolean,javax.print.PrintService) throws java.awt.print.PrinterException
meth public final javax.swing.DropMode getDropMode()
meth public final javax.swing.JTable$DropLocation getDropLocation()
meth public final void setDropMode(javax.swing.DropMode)
meth public int columnAtPoint(java.awt.Point)
meth public int convertColumnIndexToModel(int)
meth public int convertColumnIndexToView(int)
meth public int convertRowIndexToModel(int)
meth public int convertRowIndexToView(int)
meth public int getAutoResizeMode()
meth public int getColumnCount()
meth public int getEditingColumn()
meth public int getEditingRow()
meth public int getRowCount()
meth public int getRowHeight()
meth public int getRowHeight(int)
meth public int getRowMargin()
meth public int getScrollableBlockIncrement(java.awt.Rectangle,int,int)
meth public int getScrollableUnitIncrement(java.awt.Rectangle,int,int)
meth public int getSelectedColumn()
meth public int getSelectedColumnCount()
meth public int getSelectedRow()
meth public int getSelectedRowCount()
meth public int rowAtPoint(java.awt.Point)
meth public int[] getSelectedColumns()
meth public int[] getSelectedRows()
meth public java.awt.Color getGridColor()
meth public java.awt.Color getSelectionBackground()
meth public java.awt.Color getSelectionForeground()
meth public java.awt.Component getEditorComponent()
meth public java.awt.Component prepareEditor(javax.swing.table.TableCellEditor,int,int)
meth public java.awt.Component prepareRenderer(javax.swing.table.TableCellRenderer,int,int)
meth public java.awt.Dimension getIntercellSpacing()
meth public java.awt.Dimension getPreferredScrollableViewportSize()
meth public java.awt.Rectangle getCellRect(int,int,boolean)
meth public java.awt.print.Printable getPrintable(javax.swing.JTable$PrintMode,java.text.MessageFormat,java.text.MessageFormat)
meth public java.lang.Class<?> getColumnClass(int)
meth public java.lang.Object getValueAt(int,int)
meth public java.lang.String getColumnName(int)
meth public java.lang.String getToolTipText(java.awt.event.MouseEvent)
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.ListSelectionModel getSelectionModel()
meth public javax.swing.RowSorter<? extends javax.swing.table.TableModel> getRowSorter()
meth public javax.swing.plaf.TableUI getUI()
meth public javax.swing.table.JTableHeader getTableHeader()
meth public javax.swing.table.TableCellEditor getCellEditor()
meth public javax.swing.table.TableCellEditor getCellEditor(int,int)
meth public javax.swing.table.TableCellEditor getDefaultEditor(java.lang.Class<?>)
meth public javax.swing.table.TableCellRenderer getCellRenderer(int,int)
meth public javax.swing.table.TableCellRenderer getDefaultRenderer(java.lang.Class<?>)
meth public javax.swing.table.TableColumn getColumn(java.lang.Object)
meth public javax.swing.table.TableColumnModel getColumnModel()
meth public javax.swing.table.TableModel getModel()
meth public static javax.swing.JScrollPane createScrollPaneForTable(javax.swing.JTable)
 anno 0 java.lang.Deprecated()
meth public void addColumn(javax.swing.table.TableColumn)
meth public void addColumnSelectionInterval(int,int)
meth public void addNotify()
meth public void addRowSelectionInterval(int,int)
meth public void changeSelection(int,int,boolean,boolean)
meth public void clearSelection()
meth public void columnAdded(javax.swing.event.TableColumnModelEvent)
meth public void columnMarginChanged(javax.swing.event.ChangeEvent)
meth public void columnMoved(javax.swing.event.TableColumnModelEvent)
meth public void columnRemoved(javax.swing.event.TableColumnModelEvent)
meth public void columnSelectionChanged(javax.swing.event.ListSelectionEvent)
meth public void createDefaultColumnsFromModel()
meth public void doLayout()
meth public void editingCanceled(javax.swing.event.ChangeEvent)
meth public void editingStopped(javax.swing.event.ChangeEvent)
meth public void moveColumn(int,int)
meth public void removeColumn(javax.swing.table.TableColumn)
meth public void removeColumnSelectionInterval(int,int)
meth public void removeEditor()
meth public void removeNotify()
meth public void removeRowSelectionInterval(int,int)
meth public void selectAll()
meth public void setAutoCreateColumnsFromModel(boolean)
meth public void setAutoCreateRowSorter(boolean)
meth public void setAutoResizeMode(int)
meth public void setCellEditor(javax.swing.table.TableCellEditor)
meth public void setCellSelectionEnabled(boolean)
meth public void setColumnModel(javax.swing.table.TableColumnModel)
meth public void setColumnSelectionAllowed(boolean)
meth public void setColumnSelectionInterval(int,int)
meth public void setDefaultEditor(java.lang.Class<?>,javax.swing.table.TableCellEditor)
meth public void setDefaultRenderer(java.lang.Class<?>,javax.swing.table.TableCellRenderer)
meth public void setDragEnabled(boolean)
meth public void setEditingColumn(int)
meth public void setEditingRow(int)
meth public void setFillsViewportHeight(boolean)
meth public void setGridColor(java.awt.Color)
meth public void setIntercellSpacing(java.awt.Dimension)
meth public void setModel(javax.swing.table.TableModel)
meth public void setPreferredScrollableViewportSize(java.awt.Dimension)
meth public void setRowHeight(int)
meth public void setRowHeight(int,int)
meth public void setRowMargin(int)
meth public void setRowSelectionAllowed(boolean)
meth public void setRowSelectionInterval(int,int)
meth public void setRowSorter(javax.swing.RowSorter<? extends javax.swing.table.TableModel>)
meth public void setSelectionBackground(java.awt.Color)
meth public void setSelectionForeground(java.awt.Color)
meth public void setSelectionMode(int)
meth public void setSelectionModel(javax.swing.ListSelectionModel)
meth public void setShowGrid(boolean)
meth public void setShowHorizontalLines(boolean)
meth public void setShowVerticalLines(boolean)
meth public void setSurrendersFocusOnKeystroke(boolean)
meth public void setTableHeader(javax.swing.table.JTableHeader)
meth public void setUI(javax.swing.plaf.TableUI)
meth public void setUpdateSelectionOnSort(boolean)
meth public void setValueAt(java.lang.Object,int,int)
meth public void sizeColumnsToFit(boolean)
 anno 0 java.lang.Deprecated()
meth public void sizeColumnsToFit(int)
meth public void sorterChanged(javax.swing.event.RowSorterEvent)
meth public void tableChanged(javax.swing.event.TableModelEvent)
meth public void updateUI()
meth public void valueChanged(javax.swing.event.ListSelectionEvent)
supr javax.swing.JComponent

CLSS public abstract interface javax.swing.Scrollable
meth public abstract boolean getScrollableTracksViewportHeight()
meth public abstract boolean getScrollableTracksViewportWidth()
meth public abstract int getScrollableBlockIncrement(java.awt.Rectangle,int,int)
meth public abstract int getScrollableUnitIncrement(java.awt.Rectangle,int,int)
meth public abstract java.awt.Dimension getPreferredScrollableViewportSize()

CLSS public abstract interface javax.swing.event.AncestorListener
intf java.util.EventListener
meth public abstract void ancestorAdded(javax.swing.event.AncestorEvent)
meth public abstract void ancestorMoved(javax.swing.event.AncestorEvent)
meth public abstract void ancestorRemoved(javax.swing.event.AncestorEvent)

CLSS public abstract interface javax.swing.event.CellEditorListener
intf java.util.EventListener
meth public abstract void editingCanceled(javax.swing.event.ChangeEvent)
meth public abstract void editingStopped(javax.swing.event.ChangeEvent)

CLSS public abstract interface javax.swing.event.DocumentListener
intf java.util.EventListener
meth public abstract void changedUpdate(javax.swing.event.DocumentEvent)
meth public abstract void insertUpdate(javax.swing.event.DocumentEvent)
meth public abstract void removeUpdate(javax.swing.event.DocumentEvent)

CLSS public abstract interface javax.swing.event.ListSelectionListener
intf java.util.EventListener
meth public abstract void valueChanged(javax.swing.event.ListSelectionEvent)

CLSS public abstract interface javax.swing.event.RowSorterListener
intf java.util.EventListener
meth public abstract void sorterChanged(javax.swing.event.RowSorterEvent)

CLSS public abstract interface javax.swing.event.TableColumnModelListener
intf java.util.EventListener
meth public abstract void columnAdded(javax.swing.event.TableColumnModelEvent)
meth public abstract void columnMarginChanged(javax.swing.event.ChangeEvent)
meth public abstract void columnMoved(javax.swing.event.TableColumnModelEvent)
meth public abstract void columnRemoved(javax.swing.event.TableColumnModelEvent)
meth public abstract void columnSelectionChanged(javax.swing.event.ListSelectionEvent)

CLSS public abstract interface javax.swing.event.TableModelListener
intf java.util.EventListener
meth public abstract void tableChanged(javax.swing.event.TableModelEvent)

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

CLSS public org.netbeans.modules.maven.api.CommonArtifactActions
meth public static javax.swing.Action createFindUsages(org.apache.maven.artifact.Artifact)
meth public static javax.swing.Action createLibraryAction(org.openide.util.Lookup)
meth public static javax.swing.Action createScmCheckoutAction(org.openide.util.Lookup)
meth public static javax.swing.Action createViewArtifactDetails(org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
meth public static javax.swing.Action createViewJavadocAction(org.apache.maven.artifact.Artifact)
supr java.lang.Object
hcls ShowArtifactAction

CLSS public abstract interface org.netbeans.modules.maven.api.Constants
fld public final static java.lang.String ACTION_PROPERTY_JPDAATTACH = "jpda.attach"
fld public final static java.lang.String ACTION_PROPERTY_JPDAATTACH_ADDRESS = "jpda.attach.address"
fld public final static java.lang.String ACTION_PROPERTY_JPDAATTACH_PORT = "jpda.attach.port"
fld public final static java.lang.String ACTION_PROPERTY_JPDAATTACH_TRANSPORT = "jpda.attach.transport"
fld public final static java.lang.String ACTION_PROPERTY_JPDAATTACH_TRIGGER = "jpda.attach.trigger"
fld public final static java.lang.String ACTION_PROPERTY_JPDALISTEN = "jpda.listen"
fld public final static java.lang.String ENCODING_PARAM = "encoding"
fld public final static java.lang.String ENCODING_PROP = "project.build.sourceEncoding"
fld public final static java.lang.String GROUP_APACHE_PLUGINS = "org.apache.maven.plugins"
fld public final static java.lang.String HINT_CHECKSTYLE_FORMATTING = "netbeans.checkstyle.format"
fld public final static java.lang.String HINT_COMPILE_ON_SAVE = "netbeans.compile.on.save"
fld public final static java.lang.String HINT_DISPLAY_NAME = "netbeans.hint.displayName"
fld public final static java.lang.String HINT_JDK_PLATFORM = "netbeans.hint.jdkPlatform"
fld public final static java.lang.String HINT_LICENSE = "netbeans.hint.license"
fld public final static java.lang.String HINT_LICENSE_PATH = "netbeans.hint.licensePath"
fld public final static java.lang.String HINT_PACKAGING = "netbeans.hint.packaging"
fld public final static java.lang.String PLUGIN_CHECKSTYLE = "maven-checkstyle-plugin"
fld public final static java.lang.String PLUGIN_COMPILER = "maven-compiler-plugin"
fld public final static java.lang.String PLUGIN_COMPILER_VERSION_SUPPORTING_JDK9 = "3.6"
fld public final static java.lang.String PLUGIN_EAR = "maven-ear-plugin"
fld public final static java.lang.String PLUGIN_EJB = "maven-ejb-plugin"
fld public final static java.lang.String PLUGIN_FAILSAFE = "maven-failsafe-plugin"
fld public final static java.lang.String PLUGIN_JAR = "maven-jar-plugin"
fld public final static java.lang.String PLUGIN_RESOURCES = "maven-resources-plugin"
fld public final static java.lang.String PLUGIN_SITE = "maven-site-plugin"
fld public final static java.lang.String PLUGIN_SUREFIRE = "maven-surefire-plugin"
fld public final static java.lang.String PLUGIN_WAR = "maven-war-plugin"
fld public final static java.lang.String POM_MIME_TYPE = "text/x-maven-pom+xml"
fld public final static java.lang.String RELEASE_PARAM = "release"
fld public final static java.lang.String SOURCE_PARAM = "source"
fld public final static java.lang.String TARGET_PARAM = "target"
fld public final static java.util.List<java.lang.String> CLEAN_PHASES
fld public final static java.util.List<java.lang.String> DEFAULT_PHASES

CLSS public final org.netbeans.modules.maven.api.FileUtilities
meth public static java.io.File convertArtifactToLocalRepositoryFile(org.apache.maven.artifact.Artifact)
meth public static java.io.File convertStringToFile(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NullUnknown()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public static java.io.File getUserSettingsFile(boolean)
meth public static java.io.File resolveFilePath(java.io.File,java.lang.String)
meth public static java.lang.String getRelativePath(java.io.File,java.io.File)
meth public static java.lang.String relativizeFile(java.io.File,java.io.File)
meth public static java.net.URI convertStringToUri(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NullUnknown()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public static java.net.URI getDirURI(java.io.File,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static java.net.URI getDirURI(org.openide.filesystems.FileObject,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static java.util.SortedSet<java.lang.String> getBasePackageNames(org.netbeans.api.project.Project)
meth public static java.util.SortedSet<java.lang.String> getPackageNames(org.netbeans.api.project.Project)
meth public static org.openide.filesystems.FileObject convertStringToFileObject(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.openide.filesystems.FileObject convertURItoFileObject(java.net.URI)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds RELATIVE_SLASH_SEPARATED_PATH
hcls NameObtainer

CLSS public abstract interface org.netbeans.modules.maven.api.MavenConfiguration
intf org.netbeans.spi.project.ProjectConfiguration
meth public abstract java.util.List<java.lang.String> getActivatedProfiles()
meth public abstract java.util.Map<java.lang.String,java.lang.String> getProperties()

CLSS public org.netbeans.modules.maven.api.MavenValidators
meth public static org.netbeans.validation.api.Validator<java.lang.String> createArtifactIdValidators()
meth public static org.netbeans.validation.api.Validator<java.lang.String> createGroupIdValidators()
meth public static org.netbeans.validation.api.Validator<java.lang.String> createVersionValidators()
supr java.lang.Object

CLSS public final org.netbeans.modules.maven.api.ModelUtils
fld public final static java.lang.String LIBRARY_PROP_DEPENDENCIES = "maven-dependencies"
fld public final static java.lang.String LIBRARY_PROP_REPOSITORIES = "maven-repositories"
innr public final static Descriptor
innr public final static LibraryDescriptor
innr public final static RepositoryDescriptor
meth public static boolean checkByCLIMavenValidationLevel(org.apache.maven.model.building.ModelProblem)
meth public static boolean hasModelDependency(org.netbeans.modules.maven.model.pom.POMModel,java.lang.String,java.lang.String)
meth public static org.netbeans.modules.maven.api.ModelUtils$Descriptor checkLibraries(org.netbeans.api.project.libraries.Library)
meth public static org.netbeans.modules.maven.api.ModelUtils$LibraryDescriptor checkLibrary(java.net.URL)
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.modules.maven.model.pom.Dependency checkModelDependency(org.netbeans.modules.maven.model.pom.POMModel,java.lang.String,java.lang.String,boolean)
meth public static org.netbeans.modules.maven.model.pom.POMExtensibilityElement getOrCreateChild(org.netbeans.modules.maven.model.pom.POMComponent,java.lang.String,org.netbeans.modules.maven.model.pom.POMModel)
meth public static org.netbeans.modules.maven.model.pom.Repository addModelRepository(org.apache.maven.project.MavenProject,org.netbeans.modules.maven.model.pom.POMModel,java.lang.String)
meth public static void addDependency(org.openide.filesystems.FileObject,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,boolean)
meth public static void openAtPlugin(org.netbeans.modules.maven.model.pom.POMModel,java.lang.String,java.lang.String)
meth public static void openAtSource(org.apache.maven.model.InputLocation)
meth public static void setSourceLevel(org.netbeans.modules.maven.model.pom.POMModel,java.lang.String)
meth public static void updatePluginVersion(java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.maven.model.pom.Project)
supr java.lang.Object
hfds DEFAULT,LEGACY,LOG,PROBABLE_ROOTS

CLSS public final static org.netbeans.modules.maven.api.ModelUtils$Descriptor
 outer org.netbeans.modules.maven.api.ModelUtils
meth public java.util.List<org.netbeans.modules.maven.api.ModelUtils$LibraryDescriptor> getDependencies()
meth public java.util.List<org.netbeans.modules.maven.api.ModelUtils$RepositoryDescriptor> getRepositories()
supr java.lang.Object
hfds dependencies,repositories

CLSS public final static org.netbeans.modules.maven.api.ModelUtils$LibraryDescriptor
 outer org.netbeans.modules.maven.api.ModelUtils
meth public java.lang.String getArtifactId()
meth public java.lang.String getClassifier()
meth public java.lang.String getGroupId()
meth public java.lang.String getRepoRoot()
 anno 0 java.lang.Deprecated()
meth public java.lang.String getRepoType()
 anno 0 java.lang.Deprecated()
meth public java.lang.String getType()
meth public java.lang.String getVersion()
meth public java.lang.String toString()
supr java.lang.Object
hfds artifactId,classifier,groupId,repoRoot,repoType,type,version

CLSS public final static org.netbeans.modules.maven.api.ModelUtils$RepositoryDescriptor
 outer org.netbeans.modules.maven.api.ModelUtils
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getRepoRoot()
meth public java.lang.String getRepoType()
supr java.lang.Object
hfds repoRoot,repoType

CLSS public final org.netbeans.modules.maven.api.ModuleInfoUtils
cons public init()
meth public static boolean checkModuleInfoAndCompilerFit(org.netbeans.api.project.Project)
meth public static boolean hasModuleInfo(org.netbeans.modules.maven.api.NbMavenProject)
meth public static void addRequires(org.netbeans.modules.maven.api.NbMavenProject,java.util.Collection<? extends org.apache.maven.artifact.Artifact>)
supr java.lang.Object
hfds MODULE_INFO

CLSS public final org.netbeans.modules.maven.api.NbMavenProject
fld public final static java.lang.String PROP_PROJECT = "MavenProject"
fld public final static java.lang.String PROP_RESOURCE = "RESOURCES"
fld public final static java.lang.String TYPE = "org-netbeans-modules-maven"
fld public final static java.lang.String TYPE_APPCLIENT = "app-client"
fld public final static java.lang.String TYPE_EAR = "ear"
fld public final static java.lang.String TYPE_EJB = "ejb"
fld public final static java.lang.String TYPE_JAR = "jar"
fld public final static java.lang.String TYPE_NBM = "nbm"
fld public final static java.lang.String TYPE_NBM_APPLICATION = "nbm-application"
fld public final static java.lang.String TYPE_OSGI = "bundle"
fld public final static java.lang.String TYPE_POM = "pom"
fld public final static java.lang.String TYPE_WAR = "war"
meth public boolean isMavenProjectLoaded()
meth public boolean isUnloadable()
meth public java.io.File getOutputDirectory(boolean)
meth public java.lang.String getPackagingType()
meth public java.lang.String toString()
meth public java.net.URI getEarAppDirectory()
meth public java.net.URI getWebAppDirectory()
meth public java.net.URI[] getResources(boolean)
meth public org.apache.maven.model.Model getRawModel() throws org.apache.maven.model.building.ModelBuildingException
meth public org.apache.maven.project.MavenProject getEvaluatedProject(org.netbeans.api.project.ProjectActionContext)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.apache.maven.project.MavenProject getMavenProject()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.apache.maven.project.MavenProject loadAlternateMavenProject(org.netbeans.modules.maven.embedder.MavenEmbedder,java.util.List<java.lang.String>,java.util.Properties)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static boolean isErrorPlaceholder(org.apache.maven.project.MavenProject)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static boolean isIncomplete(org.apache.maven.project.MavenProject)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.apache.maven.project.MavenProject getPartialProject(org.apache.maven.project.MavenProject)
meth public static void addPropertyChangeListener(org.netbeans.api.project.Project,java.beans.PropertyChangeListener)
meth public static void fireMavenProjectReload(org.netbeans.api.project.Project)
meth public static void removePropertyChangeListener(org.netbeans.api.project.Project,java.beans.PropertyChangeListener)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addWatchedPath(java.lang.String)
meth public void addWatchedPath(java.net.URI)
meth public void downloadDependencyAndJavadocSource()
 anno 0 java.lang.Deprecated()
meth public void downloadDependencyAndJavadocSource(boolean)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removeWatchedPath(java.lang.String)
meth public void removeWatchedPath(java.net.URI)
meth public void synchronousDependencyDownload()
meth public void triggerDependencyDownload()
meth public void triggerSourceJavadocDownload(boolean)
supr java.lang.Object
hfds BINARYRP,LOG,NONBINARYRP,files,listener,project,support,task
hcls AccessorImpl,FCHSL

CLSS public org.netbeans.modules.maven.api.PluginPropertyUtils
innr public abstract interface static ConfigurationBuilder
innr public final static PluginConfigPathParams
meth public static <%0 extends java.lang.Object> {%%0} getPluginPropertyBuildable(org.apache.maven.project.MavenProject,java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.maven.api.PluginPropertyUtils$ConfigurationBuilder<{%%0}>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
 anno 5 org.netbeans.api.annotations.common.NonNull()
meth public static <%0 extends java.lang.Object> {%%0} getPluginPropertyBuildable(org.netbeans.api.project.Project,java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.maven.api.PluginPropertyUtils$ConfigurationBuilder<{%%0}>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
 anno 5 org.netbeans.api.annotations.common.NonNull()
meth public static java.lang.String getPluginProperty(org.apache.maven.project.MavenProject,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
meth public static java.lang.String getPluginProperty(org.apache.maven.project.MavenProject,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
 anno 6 org.netbeans.api.annotations.common.NullAllowed()
meth public static java.lang.String getPluginProperty(org.netbeans.api.project.Project,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
meth public static java.lang.String getPluginProperty(org.netbeans.api.project.Project,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
 anno 6 org.netbeans.api.annotations.common.NullAllowed()
meth public static java.lang.String getPluginVersion(org.apache.maven.project.MavenProject,java.lang.String,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public static java.lang.String getReportPluginProperty(org.apache.maven.project.MavenProject,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
meth public static java.lang.String getReportPluginProperty(org.netbeans.api.project.Project,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
meth public static java.lang.String getReportPluginVersion(org.apache.maven.project.MavenProject,java.lang.String,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public static java.lang.String[] getPluginPropertyList(org.apache.maven.project.MavenProject,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NullAllowed()
meth public static java.lang.String[] getPluginPropertyList(org.netbeans.api.project.Project,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NullAllowed()
meth public static java.lang.String[] getReportPluginPropertyList(org.apache.maven.project.MavenProject,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NullAllowed()
meth public static java.lang.String[] getReportPluginPropertyList(org.netbeans.api.project.Project,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NullAllowed()
meth public static java.util.List<org.apache.maven.artifact.Artifact> getPluginPathProperty(org.netbeans.api.project.Project,org.netbeans.modules.maven.api.PluginPropertyUtils$PluginConfigPathParams,boolean,java.util.List<org.apache.maven.artifact.resolver.ArtifactResolutionException>)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
meth public static java.util.Properties getPluginPropertyParameter(org.apache.maven.project.MavenProject,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
meth public static java.util.Properties getPluginPropertyParameter(org.netbeans.api.project.Project,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator createEvaluator(org.apache.maven.project.MavenProject)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator createEvaluator(org.netbeans.api.project.Project)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds CONTEXT_EXPRESSION_EVALUATOR,DUMMY_EVALUATOR,LIFECYCLE_PLUGINS
hcls DependencyListBuilder,ExternalDefaultBuilder

CLSS public abstract interface static org.netbeans.modules.maven.api.PluginPropertyUtils$ConfigurationBuilder<%0 extends java.lang.Object>
 outer org.netbeans.modules.maven.api.PluginPropertyUtils
meth public abstract {org.netbeans.modules.maven.api.PluginPropertyUtils$ConfigurationBuilder%0} build(org.codehaus.plexus.util.xml.Xpp3Dom,org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator)

CLSS public final static org.netbeans.modules.maven.api.PluginPropertyUtils$PluginConfigPathParams
 outer org.netbeans.modules.maven.api.PluginPropertyUtils
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public java.lang.String getArtifactType()
meth public java.lang.String getDefaultScope()
meth public java.lang.String getGoal()
meth public java.lang.String getPathItemName()
meth public java.lang.String getPathProperty()
meth public java.lang.String getPluginArtifactId()
meth public java.lang.String getPluginGroupId()
meth public void setArtifactType(java.lang.String)
meth public void setDefaultScope(java.lang.String)
meth public void setGoal(java.lang.String)
supr java.lang.Object
hfds artifactType,defaultScope,goal,pathItemName,pathProperty,pluginArtifactId,pluginGroupId

CLSS public abstract interface org.netbeans.modules.maven.api.ProjectProfileHandler
meth public abstract java.util.List<java.lang.String> getActiveProfiles(boolean)
meth public abstract java.util.List<java.lang.String> getAllProfiles()
meth public abstract java.util.List<java.lang.String> getMergedActiveProfiles(boolean)
meth public abstract void disableProfile(java.lang.String,boolean)
meth public abstract void enableProfile(java.lang.String,boolean)

CLSS public final org.netbeans.modules.maven.api.archetype.Archetype
cons public init()
cons public init(boolean)
fld public final boolean deletable
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getArtifactId()
meth public java.lang.String getDescription()
meth public java.lang.String getGroupId()
meth public java.lang.String getName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getRepository()
meth public java.lang.String getVersion()
meth public java.lang.String toString()
meth public java.util.Map<java.lang.String,java.lang.String> loadRequiredProperties()
meth public org.apache.maven.artifact.Artifact getArtifact()
meth public org.apache.maven.artifact.Artifact getPomArtifact()
meth public void resolveArtifacts(org.netbeans.api.progress.aggregate.AggregateProgressHandle) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException
meth public void setArtifactId(java.lang.String)
meth public void setDescription(java.lang.String)
meth public void setGroupId(java.lang.String)
meth public void setName(java.lang.String)
meth public void setRepository(java.lang.String)
meth public void setVersion(java.lang.String)
supr java.lang.Object
hfds LOG,artifact,artifactId,description,groupId,name,pomArtifact,repository,version

CLSS public abstract interface org.netbeans.modules.maven.api.archetype.ArchetypeProvider
meth public abstract java.util.List<org.netbeans.modules.maven.api.archetype.Archetype> getArchetypes()

CLSS public org.netbeans.modules.maven.api.archetype.ArchetypeWizards
fld public final static java.lang.String TEMPLATE_FOLDER = "Project/Maven2"
meth public static java.util.Set<org.openide.filesystems.FileObject> openProjects(java.io.File,java.io.File) throws java.io.IOException
meth public static org.netbeans.modules.maven.model.ModelOperation<org.netbeans.modules.maven.model.pom.POMModel> addDependencyOperation(org.netbeans.modules.maven.api.archetype.ProjectInfo,java.lang.String)
meth public static org.openide.WizardDescriptor$InstantiatingIterator<?> definedArchetype(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.openide.WizardDescriptor$InstantiatingIterator<?> definedArchetype(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.openide.WizardDescriptor$InstantiatingIterator<?> definedArchetype(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.util.Map<java.lang.String,java.lang.String>)
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor> basicWizardPanel(org.netbeans.validation.api.ui.ValidationGroup,boolean,org.netbeans.modules.maven.api.archetype.Archetype)
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public static void createFromArchetype(java.io.File,org.netbeans.modules.maven.api.archetype.ProjectInfo,org.netbeans.modules.maven.api.archetype.Archetype,java.util.Map<java.lang.String,java.lang.String>,boolean) throws java.io.IOException
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
meth public static void logUsage(java.lang.String,java.lang.String,java.lang.String)
supr java.lang.Object

CLSS public final org.netbeans.modules.maven.api.archetype.ProjectInfo
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
fld public final java.lang.String artifactId
fld public final java.lang.String groupId
fld public final java.lang.String packageName
fld public final java.lang.String version
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.maven.api.classpath.DependencyProjectsProvider
innr public final static Pair
meth public abstract java.util.Set<org.netbeans.modules.maven.api.classpath.DependencyProjectsProvider$Pair> getDependencyProjects()

CLSS public final static org.netbeans.modules.maven.api.classpath.DependencyProjectsProvider$Pair
 outer org.netbeans.modules.maven.api.classpath.DependencyProjectsProvider
cons public init(org.netbeans.api.project.Project,org.apache.maven.artifact.Artifact)
meth public boolean isIncludedAtCompile()
meth public boolean isIncludedAtRuntime()
meth public boolean isIncludedAtTests()
meth public org.apache.maven.artifact.Artifact getArtifact()
meth public org.netbeans.api.project.Project getProject()
supr java.lang.Object
hfds artifact,project

CLSS public abstract interface org.netbeans.modules.maven.api.classpath.ProjectSourcesClassPathProvider
meth public abstract org.netbeans.api.java.classpath.ClassPath getProjectSourcesClassPath(java.lang.String)
meth public abstract org.netbeans.api.java.classpath.ClassPath[] getProjectClassPaths(java.lang.String)

CLSS public final org.netbeans.modules.maven.api.customizer.ModelHandle
fld public final static java.lang.String PANEL_BASIC = "BASIC"
fld public final static java.lang.String PANEL_COMPILE = "COMPILE"
fld public final static java.lang.String PANEL_CONFIGURATION = "CONFIGURATION"
fld public final static java.lang.String PANEL_LIBRARIES = "LIBRARIES"
fld public final static java.lang.String PANEL_MAPPING = "MAPPING"
fld public final static java.lang.String PANEL_RUN = "RUN"
fld public final static java.lang.String PANEL_SOURCES = "SOURCES"
innr public static Configuration
meth public boolean isModified(java.lang.Object)
meth public java.lang.String getRawAuxiliaryProperty(java.lang.String,boolean)
meth public java.util.List<org.netbeans.modules.maven.api.customizer.ModelHandle$Configuration> getConfigurations()
meth public org.apache.maven.project.MavenProject getProject()
meth public org.netbeans.modules.maven.api.customizer.ModelHandle$Configuration getActiveConfiguration()
meth public org.netbeans.modules.maven.execute.model.ActionToGoalMapping getActionMappings()
meth public org.netbeans.modules.maven.execute.model.ActionToGoalMapping getActionMappings(org.netbeans.modules.maven.api.customizer.ModelHandle$Configuration)
meth public org.netbeans.modules.maven.model.pom.POMModel getPOMModel()
meth public static org.netbeans.modules.maven.api.customizer.ModelHandle$Configuration createCustomConfiguration(java.lang.String)
meth public static org.netbeans.modules.maven.api.customizer.ModelHandle$Configuration createDefaultConfiguration()
meth public static org.netbeans.modules.maven.api.customizer.ModelHandle$Configuration createProfileConfiguration(java.lang.String)
meth public static org.netbeans.modules.maven.execute.model.NetbeansActionMapping getDefaultMapping(java.lang.String,org.netbeans.api.project.Project)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.maven.execute.model.NetbeansActionMapping getMapping(java.lang.String,org.netbeans.api.project.Project,org.netbeans.spi.project.ProjectConfiguration)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static void putMapping(org.netbeans.modules.maven.execute.model.NetbeansActionMapping,org.netbeans.api.project.Project,org.netbeans.spi.project.ProjectConfiguration) throws java.io.IOException
meth public static void setUserActionMapping(org.netbeans.modules.maven.execute.model.NetbeansActionMapping,org.netbeans.modules.maven.execute.model.ActionToGoalMapping)
meth public void addConfiguration(org.netbeans.modules.maven.api.customizer.ModelHandle$Configuration)
meth public void markAsModified(java.lang.Object)
meth public void removeConfiguration(org.netbeans.modules.maven.api.customizer.ModelHandle$Configuration)
meth public void setActiveConfiguration(org.netbeans.modules.maven.api.customizer.ModelHandle$Configuration)
meth public void setRawAuxiliaryProperty(java.lang.String,java.lang.String,boolean)
supr java.lang.Object
hfds active,auxiliaryProps,configurations,mappings,modConfig,modMappings,modModel,model,project
hcls AccessorImpl

CLSS public static org.netbeans.modules.maven.api.customizer.ModelHandle$Configuration
 outer org.netbeans.modules.maven.api.customizer.ModelHandle
cons public init()
supr org.netbeans.modules.maven.api.customizer.ModelHandle2$Configuration

CLSS public org.netbeans.modules.maven.api.customizer.ModelHandle2
fld public final static java.lang.String PANEL_BASIC = "BASIC"
fld public final static java.lang.String PANEL_COMPILE = "COMPILE"
fld public final static java.lang.String PANEL_CONFIGURATION = "CONFIGURATION"
fld public final static java.lang.String PANEL_HEADERS = "LICENSE_HEADERS"
fld public final static java.lang.String PANEL_LIBRARIES = "LIBRARIES"
fld public final static java.lang.String PANEL_MAPPING = "MAPPING"
fld public final static java.lang.String PANEL_RUN = "RUN"
fld public final static java.lang.String PANEL_SOURCES = "SOURCES"
innr public static Configuration
meth public java.lang.String getRawAuxiliaryProperty(java.lang.String,boolean)
meth public java.util.List<org.netbeans.modules.maven.api.customizer.ModelHandle2$Configuration> getConfigurations()
meth public java.util.List<org.netbeans.modules.maven.model.ModelOperation<org.netbeans.modules.maven.model.pom.POMModel>> getPOMOperations()
meth public org.apache.maven.project.MavenProject getProject()
meth public org.netbeans.modules.maven.api.customizer.ModelHandle2$Configuration getActiveConfiguration()
meth public org.netbeans.modules.maven.execute.model.ActionToGoalMapping getActionMappings()
meth public org.netbeans.modules.maven.execute.model.ActionToGoalMapping getActionMappings(org.netbeans.modules.maven.api.customizer.ModelHandle2$Configuration)
meth public org.netbeans.modules.maven.model.pom.POMModel getPOMModel()
meth public static org.netbeans.modules.maven.api.customizer.ModelHandle2$Configuration createCustomConfiguration(java.lang.String)
meth public static org.netbeans.modules.maven.api.customizer.ModelHandle2$Configuration createDefaultConfiguration()
meth public static org.netbeans.modules.maven.api.customizer.ModelHandle2$Configuration createProfileConfiguration(java.lang.String)
meth public static org.netbeans.modules.maven.api.customizer.ModelHandle2$Configuration createProvidedConfiguration(java.lang.String)
meth public static org.netbeans.modules.maven.execute.model.NetbeansActionMapping getDefaultMapping(java.lang.String,org.netbeans.api.project.Project)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.maven.execute.model.NetbeansActionMapping getMapping(java.lang.String,org.netbeans.api.project.Project,org.netbeans.spi.project.ProjectConfiguration)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static void putMapping(org.netbeans.modules.maven.execute.model.NetbeansActionMapping,org.netbeans.api.project.Project,org.netbeans.spi.project.ProjectConfiguration) throws java.io.IOException
meth public static void setUserActionMapping(org.netbeans.modules.maven.execute.model.NetbeansActionMapping,org.netbeans.modules.maven.execute.model.ActionToGoalMapping)
meth public void addConfiguration(org.netbeans.modules.maven.api.customizer.ModelHandle2$Configuration)
meth public void addPOMModification(org.netbeans.modules.maven.model.ModelOperation<org.netbeans.modules.maven.model.pom.POMModel>)
meth public void markAsModified(org.netbeans.modules.maven.execute.model.ActionToGoalMapping)
meth public void markConfigurationsAsModified()
meth public void removeConfiguration(org.netbeans.modules.maven.api.customizer.ModelHandle2$Configuration)
meth public void removePOMModification(org.netbeans.modules.maven.model.ModelOperation<org.netbeans.modules.maven.model.pom.POMModel>)
meth public void setActiveConfiguration(org.netbeans.modules.maven.api.customizer.ModelHandle2$Configuration)
meth public void setRawAuxiliaryProperty(java.lang.String,java.lang.String,boolean)
supr java.lang.Object
hfds active,allActions,auxiliaryProps,configurations,mappings,modConfig,model,modifiedMappings,pomOperations,project,transPropsPrivate,transPropsShared
hcls AccessorImpl

CLSS public static org.netbeans.modules.maven.api.customizer.ModelHandle2$Configuration
 outer org.netbeans.modules.maven.api.customizer.ModelHandle2
meth public boolean isDefault()
meth public boolean isProfileBased()
meth public boolean isProvided()
meth public boolean isShared()
meth public java.lang.String getDisplayName()
meth public java.lang.String getFileNameExt()
meth public java.lang.String getId()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getActivatedProfiles()
meth public java.util.Map<java.lang.String,java.lang.String> getProperties()
meth public void setActivatedProfiles(java.util.List<java.lang.String>)
meth public void setDefault(boolean)
meth public void setDisplayName(java.lang.String)
meth public void setProperties(java.util.Map<java.lang.String,java.lang.String>)
meth public void setShared(boolean)
supr java.lang.Object
hfds activatedProfiles,defaul,displayName,id,profileBased,properties,shared

CLSS public abstract org.netbeans.modules.maven.api.customizer.support.CheckBoxUpdater
cons public init(javax.swing.JCheckBox)
intf java.awt.event.ItemListener
intf javax.swing.event.AncestorListener
meth public abstract boolean getDefaultValue()
meth public abstract java.lang.Boolean getValue()
meth public abstract void setValue(java.lang.Boolean)
meth public void ancestorAdded(javax.swing.event.AncestorEvent)
meth public void ancestorMoved(javax.swing.event.AncestorEvent)
meth public void ancestorRemoved(javax.swing.event.AncestorEvent)
meth public void itemStateChanged(java.awt.event.ItemEvent)
supr java.lang.Object
hfds component,inherited

CLSS public abstract org.netbeans.modules.maven.api.customizer.support.ComboBoxUpdater<%0 extends java.lang.Object>
cons public init(javax.swing.JComboBox,javax.swing.JLabel)
intf java.awt.event.ActionListener
intf javax.swing.event.AncestorListener
meth public abstract void setValue({org.netbeans.modules.maven.api.customizer.support.ComboBoxUpdater%0})
meth public abstract {org.netbeans.modules.maven.api.customizer.support.ComboBoxUpdater%0} getDefaultValue()
meth public abstract {org.netbeans.modules.maven.api.customizer.support.ComboBoxUpdater%0} getValue()
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void ancestorAdded(javax.swing.event.AncestorEvent)
meth public void ancestorMoved(javax.swing.event.AncestorEvent)
meth public void ancestorRemoved(javax.swing.event.AncestorEvent)
supr java.lang.Object
hfds component,inherited,label

CLSS public final org.netbeans.modules.maven.api.customizer.support.DelayedDocumentChangeListener
intf javax.swing.event.DocumentListener
meth public static javax.swing.event.DocumentListener create(javax.swing.text.Document,javax.swing.event.ChangeListener,int)
meth public void changedUpdate(javax.swing.event.DocumentEvent)
meth public void insertUpdate(javax.swing.event.DocumentEvent)
meth public void removeUpdate(javax.swing.event.DocumentEvent)
supr java.lang.Object
hfds chEvt,changeTimer,doc

CLSS public final org.netbeans.modules.maven.api.customizer.support.ReflectionTextComponentUpdater
cons public init(java.lang.String,java.lang.Object,java.lang.Object,javax.swing.text.JTextComponent,javax.swing.JLabel,org.netbeans.modules.maven.api.customizer.ModelHandle2,org.netbeans.modules.maven.api.customizer.support.ReflectionTextComponentUpdater$Operation) throws java.lang.NoSuchMethodException
cons public init(java.lang.String,java.lang.String,java.lang.Object,java.lang.Object,javax.swing.text.JTextComponent,javax.swing.JLabel,org.netbeans.modules.maven.api.customizer.ModelHandle) throws java.lang.NoSuchMethodException
innr public abstract static Operation
meth public java.lang.String getDefaultValue()
meth public java.lang.String getValue()
meth public void setValue(java.lang.String)
supr org.netbeans.modules.maven.api.customizer.support.TextComponentUpdater
hfds defaults,defgetter,handle,handle2,initialValue2,model,modelgetter,modelsetter,operation

CLSS public abstract static org.netbeans.modules.maven.api.customizer.support.ReflectionTextComponentUpdater$Operation
 outer org.netbeans.modules.maven.api.customizer.support.ReflectionTextComponentUpdater
cons public init()
intf org.netbeans.modules.maven.model.ModelOperation<org.netbeans.modules.maven.model.pom.POMModel>
meth public final java.lang.String getNewValue()
meth public final void setNewValue(java.lang.String)
supr java.lang.Object
hfds isValueSet,newValue

CLSS public final org.netbeans.modules.maven.api.customizer.support.SelectedItemsTable
cons public init()
cons public init(org.netbeans.modules.maven.api.customizer.support.SelectedItemsTable$SelectedItemsTableModel)
innr public final static SelectedItemsTableModel
supr javax.swing.JTable
hfds CHECKBOX_WIDTH

CLSS public final static org.netbeans.modules.maven.api.customizer.support.SelectedItemsTable$SelectedItemsTableModel
 outer org.netbeans.modules.maven.api.customizer.support.SelectedItemsTable
cons public init(org.netbeans.modules.maven.spi.customizer.SelectedItemsTablePersister)
meth public boolean isChanged()
meth public int getColumnCount()
meth public int getRowCount()
meth public java.lang.Class getColumnClass(int)
meth public java.lang.Object getValueAt(int,int)
meth public void setValueAt(java.lang.Object,int,int)
supr javax.swing.table.AbstractTableModel
hfds originalSelected,persister,pkgNames,selected

CLSS public abstract org.netbeans.modules.maven.api.customizer.support.TextComponentUpdater
cons public init(javax.swing.text.JTextComponent,javax.swing.JLabel)
intf javax.swing.event.AncestorListener
intf javax.swing.event.DocumentListener
meth public abstract java.lang.String getDefaultValue()
meth public abstract java.lang.String getValue()
meth public abstract void setValue(java.lang.String)
meth public void ancestorAdded(javax.swing.event.AncestorEvent)
meth public void ancestorMoved(javax.swing.event.AncestorEvent)
meth public void ancestorRemoved(javax.swing.event.AncestorEvent)
meth public void changedUpdate(javax.swing.event.DocumentEvent)
meth public void insertUpdate(javax.swing.event.DocumentEvent)
meth public void removeUpdate(javax.swing.event.DocumentEvent)
supr java.lang.Object
hfds component,inherited,label

CLSS public abstract interface org.netbeans.modules.maven.api.execute.ActiveJ2SEPlatformProvider
meth public abstract org.netbeans.api.java.platform.JavaPlatform getJavaPlatform()
 anno 0 org.netbeans.api.annotations.common.NonNull()

CLSS public final org.netbeans.modules.maven.api.execute.ExecutionContext
fld public final static int EXECUTION_ABORTED = -10
meth public org.netbeans.api.progress.ProgressHandle getProgressHandle()
meth public org.openide.windows.InputOutput getInputOutput()
supr java.lang.Object
hfds handle,io,res
hcls AccessorImpl

CLSS public abstract interface org.netbeans.modules.maven.api.execute.ExecutionResultChecker
meth public abstract void executionResult(org.netbeans.modules.maven.api.execute.RunConfig,org.netbeans.modules.maven.api.execute.ExecutionContext,int)

CLSS public abstract interface org.netbeans.modules.maven.api.execute.LateBoundPrerequisitesChecker
meth public abstract boolean checkRunConfig(org.netbeans.modules.maven.api.execute.RunConfig,org.netbeans.modules.maven.api.execute.ExecutionContext)

CLSS public abstract interface org.netbeans.modules.maven.api.execute.PrerequisitesChecker
meth public abstract boolean checkRunConfig(org.netbeans.modules.maven.api.execute.RunConfig)

CLSS public abstract interface org.netbeans.modules.maven.api.execute.RunConfig
innr public final static !enum ReactorStyle
meth public abstract boolean isInteractive()
meth public abstract boolean isRecursive()
meth public abstract boolean isShowDebug()
meth public abstract boolean isShowError()
meth public abstract boolean isUpdateSnapshots()
meth public abstract java.io.File getExecutionDirectory()
meth public abstract java.lang.Boolean isOffline()
meth public abstract java.lang.String getActionName()
meth public abstract java.lang.String getExecutionName()
meth public abstract java.lang.String getTaskDisplayName()
meth public abstract java.util.List<java.lang.String> getActivatedProfiles()
meth public abstract java.util.List<java.lang.String> getGoals()
meth public abstract java.util.Map<? extends java.lang.String,? extends java.lang.String> getProperties()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.Map<? extends java.lang.String,?> getInternalProperties()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.apache.maven.project.MavenProject getMavenProject()
meth public abstract org.netbeans.api.project.Project getProject()
meth public abstract org.netbeans.modules.maven.api.execute.RunConfig getPreExecution()
meth public abstract org.netbeans.modules.maven.api.execute.RunConfig$ReactorStyle getReactorStyle()
meth public abstract org.openide.filesystems.FileObject getSelectedFileObject()
meth public abstract void addProperties(java.util.Map<java.lang.String,java.lang.String>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void setActivatedProfiles(java.util.List<java.lang.String>)
meth public abstract void setExecutionDirectory(java.io.File)
meth public abstract void setInternalProperty(java.lang.String,java.lang.Object)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract void setOffline(java.lang.Boolean)
meth public abstract void setPreExecution(org.netbeans.modules.maven.api.execute.RunConfig)
meth public abstract void setProperty(java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public org.openide.util.Lookup getActionContext()

CLSS public final static !enum org.netbeans.modules.maven.api.execute.RunConfig$ReactorStyle
 outer org.netbeans.modules.maven.api.execute.RunConfig
fld public final static org.netbeans.modules.maven.api.execute.RunConfig$ReactorStyle ALSO_MAKE
fld public final static org.netbeans.modules.maven.api.execute.RunConfig$ReactorStyle ALSO_MAKE_DEPENDENTS
fld public final static org.netbeans.modules.maven.api.execute.RunConfig$ReactorStyle NONE
meth public static org.netbeans.modules.maven.api.execute.RunConfig$ReactorStyle valueOf(java.lang.String)
meth public static org.netbeans.modules.maven.api.execute.RunConfig$ReactorStyle[] values()
supr java.lang.Enum<org.netbeans.modules.maven.api.execute.RunConfig$ReactorStyle>

CLSS public final org.netbeans.modules.maven.api.execute.RunUtils
meth public static boolean hasApplicationCompileOnSaveEnabled(org.netbeans.api.project.Project)
 anno 0 java.lang.Deprecated()
meth public static boolean hasApplicationCompileOnSaveEnabled(org.netbeans.modules.maven.api.execute.RunConfig)
 anno 0 java.lang.Deprecated()
meth public static boolean hasTestCompileOnSaveEnabled(org.netbeans.api.project.Project)
 anno 0 java.lang.Deprecated()
meth public static boolean hasTestCompileOnSaveEnabled(org.netbeans.modules.maven.api.execute.RunConfig)
 anno 0 java.lang.Deprecated()
meth public static boolean isCompileOnSaveEnabled(org.netbeans.api.project.Project)
meth public static boolean isCompileOnSaveEnabled(org.netbeans.modules.maven.api.execute.RunConfig)
meth public static org.netbeans.modules.maven.api.execute.RunConfig cloneRunConfig(org.netbeans.modules.maven.api.execute.RunConfig)
meth public static org.netbeans.modules.maven.api.execute.RunConfig createRunConfig(java.io.File,org.netbeans.api.project.Project,java.lang.String,java.util.List<java.lang.String>)
meth public static org.netbeans.modules.maven.api.execute.RunConfig createRunConfig(java.lang.String,org.netbeans.api.project.Project,org.netbeans.spi.project.ProjectConfiguration,org.openide.util.Lookup)
meth public static org.openide.execution.ExecutorTask executeMaven(org.netbeans.modules.maven.api.execute.RunConfig)
meth public static org.openide.execution.ExecutorTask run(org.netbeans.modules.maven.api.execute.RunConfig)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.maven.api.output.ContextOutputProcessorFactory
intf org.netbeans.modules.maven.api.output.OutputProcessorFactory
meth public abstract java.util.Set<? extends org.netbeans.modules.maven.api.output.OutputProcessor> createProcessorsSet(org.netbeans.api.project.Project,org.netbeans.modules.maven.api.execute.RunConfig)

CLSS public abstract interface org.netbeans.modules.maven.api.output.NotifyFinishOutputProcessor
intf org.netbeans.modules.maven.api.output.OutputProcessor
meth public abstract void buildFinished()

CLSS public abstract interface org.netbeans.modules.maven.api.output.OutputProcessor
meth public abstract java.lang.String[] getRegisteredOutputSequences()
meth public abstract void processLine(java.lang.String,org.netbeans.modules.maven.api.output.OutputVisitor)
meth public abstract void sequenceEnd(java.lang.String,org.netbeans.modules.maven.api.output.OutputVisitor)
meth public abstract void sequenceFail(java.lang.String,org.netbeans.modules.maven.api.output.OutputVisitor)
meth public abstract void sequenceStart(java.lang.String,org.netbeans.modules.maven.api.output.OutputVisitor)

CLSS public abstract interface org.netbeans.modules.maven.api.output.OutputProcessorFactory
meth public abstract java.util.Set<? extends org.netbeans.modules.maven.api.output.OutputProcessor> createProcessorsSet(org.netbeans.api.project.Project)

CLSS public final org.netbeans.modules.maven.api.output.OutputUtils
fld public final static java.util.regex.Pattern linePattern
meth public static org.openide.windows.OutputListener matchStackTraceLine(java.lang.String,org.netbeans.api.java.classpath.ClassPath)
 anno 0 java.lang.Deprecated()
meth public static org.openide.windows.OutputListener matchStackTraceLine(java.lang.String,org.netbeans.api.project.Project)
meth public static org.openide.windows.OutputListener matchStackTraceLine(java.lang.String,org.openide.filesystems.FileObject)
supr java.lang.Object
hfds fileStacktraceListeners,projectStacktraceListeners
hcls ClassPathStacktraceOutputListener,FileObjectStacktraceOutputListener,ProjectStacktraceOutputListener,StacktraceAttributes,StacktraceOutputListener

CLSS public final org.netbeans.modules.maven.api.output.OutputVisitor
cons public init()
cons public init(org.netbeans.modules.maven.api.output.OutputVisitor$Context)
fld public final static java.lang.String ACTION_PRIORITY = "Priority"
fld public final static java.lang.String ACTION_QUESTION = "Question"
innr public abstract interface static Context
meth public boolean isImportant()
meth public boolean isLineSkipped()
meth public java.awt.Color getColor()
meth public java.awt.Color getColor(org.openide.windows.InputOutput)
meth public java.lang.String getLine()
meth public javax.swing.Action getSuccessAction()
meth public org.netbeans.modules.maven.api.output.OutputVisitor$Context getContext()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.openide.windows.OutputListener getOutputListener()
meth public void resetVisitor()
meth public void setColor(java.awt.Color)
meth public void setLine(java.lang.String)
meth public void setOutputListener(org.openide.windows.OutputListener)
meth public void setOutputListener(org.openide.windows.OutputListener,boolean)
meth public void setOutputType(org.openide.windows.IOColors$OutputType)
meth public void setSuccessAction(javax.swing.Action)
meth public void skipLine()
supr java.lang.Object
hfds color,context,important,line,outputListener,outputType,skipLine,successAction

CLSS public abstract interface static org.netbeans.modules.maven.api.output.OutputVisitor$Context
 outer org.netbeans.modules.maven.api.output.OutputVisitor
meth public abstract org.netbeans.api.project.Project getCurrentProject()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface org.netbeans.modules.maven.api.output.TestOutputObserver
meth public abstract void processLine(java.lang.String,org.netbeans.api.project.Project)

CLSS public final org.netbeans.modules.maven.api.problem.ProblemReport
 anno 0 java.lang.Deprecated()
cons public init(int,java.lang.String,java.lang.String,javax.swing.Action)
fld public final static int SEVERITY_HIGH = 0
fld public final static int SEVERITY_LOW = 10
fld public final static int SEVERITY_MEDIUM = 5
meth public int getSeverityLevel()
meth public java.lang.String getId()
meth public java.lang.String getLongDescription()
meth public java.lang.String getShortDescription()
meth public javax.swing.Action getCorrectiveAction()
meth public void setId(java.lang.String)
supr java.lang.Object
hfds action,id,level,longDesc,shortDesc

CLSS public abstract interface org.netbeans.modules.maven.api.problem.ProblemReporter
 anno 0 java.lang.Deprecated()
meth public abstract java.util.Collection<org.netbeans.modules.maven.api.problem.ProblemReport> getReports()
meth public abstract void addReport(org.netbeans.modules.maven.api.problem.ProblemReport)
meth public abstract void addReports(org.netbeans.modules.maven.api.problem.ProblemReport[])
meth public abstract void removeReport(org.netbeans.modules.maven.api.problem.ProblemReport)

CLSS public abstract org.netbeans.modules.maven.execute.AbstractMavenExecutor
cons protected init(org.netbeans.modules.maven.api.execute.RunConfig)
fld protected final java.lang.Object SEMAPHORE
fld protected org.netbeans.modules.maven.api.execute.RunConfig config
fld protected org.netbeans.modules.maven.execute.AbstractMavenExecutor$MavenItem item
fld protected org.openide.execution.ExecutorTask task
innr protected MavenItem
innr protected abstract interface static ResumeFromFinder
innr public final static OptionsAction
innr public final static TabContext
intf org.netbeans.modules.maven.execute.MavenExecutor
intf org.openide.util.Cancellable
meth protected final org.netbeans.modules.maven.execute.AbstractMavenExecutor$TabContext createContext()
meth protected final void actionStatesAtFinish(org.netbeans.modules.maven.execute.AbstractMavenExecutor$ResumeFromFinder,org.netbeans.modules.maven.execute.cmd.ExecutionEventObject$Tree)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth protected final void actionStatesAtStart()
meth protected final void processInitialMessage()
meth protected java.lang.Class<org.netbeans.modules.maven.execute.AbstractMavenExecutor$TabContext> tabContextType()
meth protected javax.swing.Action[] createNewTabActions()
meth protected void reassignAdditionalContext(org.netbeans.modules.maven.execute.AbstractMavenExecutor$TabContext)
meth public final void addInitialMessage(java.lang.String,org.openide.windows.OutputListener)
meth public final void setTask(org.openide.execution.ExecutorTask)
supr org.netbeans.modules.maven.execute.OutputTabMaintainer<org.netbeans.modules.maven.execute.AbstractMavenExecutor$TabContext>
hfds listeners,messages,tabContext
hcls ReRunAction,ResumeAction,ShowOverviewAction,StopAction

CLSS protected org.netbeans.modules.maven.execute.AbstractMavenExecutor$MavenItem
 outer org.netbeans.modules.maven.execute.AbstractMavenExecutor
cons protected init(org.netbeans.modules.maven.execute.AbstractMavenExecutor)
intf org.netbeans.spi.project.ui.support.BuildExecutionSupport$ActionItem
meth public boolean equals(java.lang.Object)
meth public boolean isRunning()
meth public int hashCode()
meth public java.lang.String getAction()
meth public java.lang.String getDisplayName()
meth public org.openide.filesystems.FileObject getProjectDirectory()
meth public void repeatExecution()
meth public void stopRunning()
supr java.lang.Object

CLSS public final static org.netbeans.modules.maven.execute.AbstractMavenExecutor$OptionsAction
 outer org.netbeans.modules.maven.execute.AbstractMavenExecutor
cons public init()
meth public void actionPerformed(java.awt.event.ActionEvent)
supr javax.swing.AbstractAction

CLSS protected abstract interface static org.netbeans.modules.maven.execute.AbstractMavenExecutor$ResumeFromFinder
 outer org.netbeans.modules.maven.execute.AbstractMavenExecutor
meth public abstract org.netbeans.modules.maven.api.NbMavenProject find(org.netbeans.api.project.Project)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public final static org.netbeans.modules.maven.execute.AbstractMavenExecutor$TabContext
 outer org.netbeans.modules.maven.execute.AbstractMavenExecutor
cons public init()
meth protected org.netbeans.modules.maven.execute.AbstractMavenExecutor$TabContext clone()
supr java.lang.Object
hfds options,overview,rerun,rerunDebug,resume,stop

CLSS public abstract org.netbeans.modules.maven.execute.AbstractOutputHandler
cons protected init(org.netbeans.api.project.Project,org.netbeans.api.progress.ProgressHandle,org.netbeans.modules.maven.api.execute.RunConfig,org.netbeans.modules.maven.api.output.OutputVisitor)
fld protected final static java.lang.String PRJ_EXECUTE = "project-execute"
fld protected final static java.lang.String SESSION_EXECUTE = "session-execute"
fld protected java.util.HashMap<java.lang.String,java.util.Set<org.netbeans.modules.maven.api.output.OutputProcessor>> processors
fld protected java.util.Set<org.netbeans.modules.maven.api.output.NotifyFinishOutputProcessor> toFinishProcessors
fld protected java.util.Set<org.netbeans.modules.maven.api.output.OutputProcessor> currentProcessors
fld protected org.netbeans.modules.maven.api.output.OutputVisitor visitor
innr public final static !enum Level
meth protected abstract org.openide.windows.InputOutput getIO()
meth protected final java.lang.String getEventId(java.lang.String,java.lang.String)
meth protected final void buildFinished()
meth protected final void initProcessorList(org.netbeans.api.project.Project,org.netbeans.modules.maven.api.execute.RunConfig)
meth protected final void processEnd(java.lang.String,org.openide.windows.OutputWriter)
meth protected final void processFail(java.lang.String,org.openide.windows.OutputWriter)
meth protected final void processLine(java.lang.String,org.openide.windows.OutputWriter,org.netbeans.modules.maven.execute.AbstractOutputHandler$Level)
meth protected final void processMultiLine(java.lang.String,org.openide.windows.OutputWriter,org.netbeans.modules.maven.execute.AbstractOutputHandler$Level)
meth protected final void processStart(java.lang.String,org.openide.windows.OutputWriter)
meth protected final void quitSleepiness()
meth protected void checkSleepiness()
meth public static java.util.List<java.lang.String> splitMultiLine(java.lang.String)
supr java.lang.Object
hfds SLEEP_DELAY,protectedMode,protectedModeLock,sleepTask

CLSS public final static !enum org.netbeans.modules.maven.execute.AbstractOutputHandler$Level
 outer org.netbeans.modules.maven.execute.AbstractOutputHandler
fld public final static org.netbeans.modules.maven.execute.AbstractOutputHandler$Level DEBUG
fld public final static org.netbeans.modules.maven.execute.AbstractOutputHandler$Level ERROR
fld public final static org.netbeans.modules.maven.execute.AbstractOutputHandler$Level FATAL
fld public final static org.netbeans.modules.maven.execute.AbstractOutputHandler$Level INFO
fld public final static org.netbeans.modules.maven.execute.AbstractOutputHandler$Level WARNING
meth public static org.netbeans.modules.maven.execute.AbstractOutputHandler$Level valueOf(java.lang.String)
meth public static org.netbeans.modules.maven.execute.AbstractOutputHandler$Level[] values()
supr java.lang.Enum<org.netbeans.modules.maven.execute.AbstractOutputHandler$Level>

CLSS public abstract interface org.netbeans.modules.maven.execute.ActionNameProvider
meth public abstract java.util.ResourceBundle getTranslations()

CLSS public final org.netbeans.modules.maven.execute.ActionToGoalUtils
fld public static org.netbeans.modules.maven.execute.ActionToGoalUtils$ContextAccessor ACCESSOR
innr public abstract static ContextAccessor
meth public static boolean isActionEnable(java.lang.String,org.netbeans.modules.maven.NbMavenProjectImpl,org.netbeans.spi.project.ProjectConfiguration,org.openide.util.Lookup)
meth public static boolean isActionEnable(java.lang.String,org.netbeans.modules.maven.NbMavenProjectImpl,org.openide.util.Lookup)
meth public static boolean isDisabledMapping(org.netbeans.modules.maven.execute.model.NetbeansActionMapping)
meth public static java.io.File resolveProjectExecutionBasedir(org.netbeans.modules.maven.execute.model.NetbeansActionMapping,org.netbeans.api.project.Project)
meth public static java.util.List<? extends org.netbeans.modules.maven.spi.actions.MavenActionsProvider> actionProviders(org.netbeans.api.project.Project)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.maven.api.execute.RunConfig createRunConfig(java.lang.String,org.netbeans.modules.maven.NbMavenProjectImpl,org.netbeans.spi.project.ProjectConfiguration,org.openide.util.Lookup)
meth public static org.netbeans.modules.maven.api.execute.RunConfig createRunConfig(java.lang.String,org.netbeans.modules.maven.NbMavenProjectImpl,org.openide.util.Lookup)
meth public static org.netbeans.modules.maven.execute.model.ActionToGoalMapping readMappingsFromFileAttributes(org.openide.filesystems.FileObject)
meth public static org.netbeans.modules.maven.execute.model.NetbeansActionMapping getActiveMapping(java.lang.String,org.netbeans.api.project.Project,org.netbeans.modules.maven.configurations.M2Configuration)
meth public static org.netbeans.modules.maven.execute.model.NetbeansActionMapping getDefaultMapping(java.lang.String,org.netbeans.api.project.Project)
meth public static org.netbeans.modules.maven.execute.model.NetbeansActionMapping[] getActiveCustomMappings(org.netbeans.modules.maven.NbMavenProjectImpl)
meth public static org.netbeans.modules.maven.execute.model.NetbeansActionMapping[] getActiveCustomMappingsForFile(org.netbeans.modules.maven.NbMavenProjectImpl)
meth public static void writeMappingsToFileAttributes(org.openide.filesystems.FileObject,org.netbeans.modules.maven.execute.model.ActionToGoalMapping)
supr java.lang.Object
hfds FO_ATTR_CUSTOM_MAPP
hcls PackagingProvider

CLSS public abstract static org.netbeans.modules.maven.execute.ActionToGoalUtils$ContextAccessor
 outer org.netbeans.modules.maven.execute.ActionToGoalUtils
cons public init()
meth public abstract org.netbeans.modules.maven.api.execute.ExecutionContext createContext(org.openide.windows.InputOutput,org.netbeans.api.progress.ProgressHandle)
supr java.lang.Object

CLSS public org.netbeans.modules.maven.execute.BeanRunConfig
cons public init()
cons public init(org.netbeans.modules.maven.api.execute.RunConfig)
intf org.netbeans.modules.maven.api.execute.RunConfig
meth public final boolean isInteractive()
meth public final boolean isRecursive()
meth public final boolean isShowDebug()
meth public final boolean isShowError()
meth public final boolean isUpdateSnapshots()
meth public final java.io.File getExecutionDirectory()
meth public final java.lang.Boolean isOffline()
meth public final java.lang.String getExecutionName()
meth public final java.lang.String getTaskDisplayName()
meth public final java.util.List<java.lang.String> getActivatedProfiles()
meth public final java.util.List<java.lang.String> getGoals()
meth public final java.util.Map<? extends java.lang.String,? extends java.lang.String> getProperties()
meth public final java.util.Map<? extends java.lang.String,?> getInternalProperties()
meth public final org.apache.maven.project.MavenProject getMavenProject()
meth public final org.netbeans.api.project.Project getProject()
meth public final org.netbeans.modules.maven.api.execute.RunConfig$ReactorStyle getReactorStyle()
meth public final void addProperties(java.util.Map<java.lang.String,java.lang.String>)
meth public final void setActivatedProfiles(java.util.List<java.lang.String>)
meth public final void setExecutionDirectory(java.io.File)
meth public final void setExecutionName(java.lang.String)
meth public final void setGoals(java.util.List<java.lang.String>)
meth public final void setInteractive(boolean)
meth public final void setInternalProperty(java.lang.String,java.lang.Object)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public final void setOffline(java.lang.Boolean)
meth public final void setProject(org.netbeans.api.project.Project)
meth public final void setProperty(java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public final void setReactorStyle(org.netbeans.modules.maven.api.execute.RunConfig$ReactorStyle)
meth public final void setRecursive(boolean)
meth public final void setShowDebug(boolean)
meth public final void setShowError(boolean)
meth public final void setTaskDisplayName(java.lang.String)
meth public final void setUpdateSnapshots(boolean)
meth public java.lang.String getActionName()
meth public org.netbeans.modules.maven.api.execute.RunConfig getPreExecution()
meth public org.openide.filesystems.FileObject getSelectedFileObject()
meth public org.openide.util.Lookup getActionContext()
meth public void reassignMavenProjectFromParent()
meth public void setActionContext(org.openide.util.Lookup)
meth public void setActionName(java.lang.String)
meth public void setFileObject(org.openide.filesystems.FileObject)
meth public void setPreExecution(org.netbeans.modules.maven.api.execute.RunConfig)
supr java.lang.Object
hfds actionContext,actionName,activate,executionDirectory,executionName,goals,interactive,internalProperties,mp,offline,parent,preexecution,project,projectDirectory,properties,reactor,recursive,selectedFO,showDebug,showError,taskName,updateSnapshots

CLSS public org.netbeans.modules.maven.execute.CommandLineOutputHandler
cons public init(org.openide.windows.InputOutput,org.netbeans.api.project.Project,org.netbeans.api.progress.ProgressHandle,org.netbeans.modules.maven.api.execute.RunConfig,boolean)
fld public final static java.util.regex.Pattern reactorSummaryLine
fld public final static java.util.regex.Pattern startPatternM2
fld public final static java.util.regex.Pattern startPatternM3
innr public static ContextImpl
meth protected final void checkSleepiness()
meth protected org.openide.windows.InputOutput getIO()
meth public org.netbeans.modules.maven.execute.cmd.ExecutionEventObject$Tree getExecutionTree()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
supr org.netbeans.modules.maven.execute.AbstractOutputHandler
hfds END_TO_START_Mappings,LOG,PROCESSOR,SEC_MOJO_EXEC,addMojoFold,addProjectFold,contextImpl,currentProject,currentTag,currentTreeNode,executionTree,firstFailure,foldsBroken,forkCount,handle,inStackTrace,inp,inputOutput,linePattern,mavenSomethingPlugin,mavencoreurls,outTask,parser,projectCount,reactorFailure,reactorSize,somethingMavenPlugin,stackTraceElement,state,stdOut
hcls FindByEvents,FindByName,Input,Output,ProgressState

CLSS public static org.netbeans.modules.maven.execute.CommandLineOutputHandler$ContextImpl
 outer org.netbeans.modules.maven.execute.CommandLineOutputHandler
intf org.netbeans.modules.maven.api.output.OutputVisitor$Context
meth public org.netbeans.api.project.Project getCurrentProject()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.maven.execute.cmd.ExecutionEventObject$Tree getExecutionTree()
meth public void setCurrentProject(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds currentProject,executionTree

CLSS public org.netbeans.modules.maven.execute.DefaultActionGoalProvider
cons public init()
meth protected java.io.InputStream getActionDefinitionStream()
meth public static org.netbeans.modules.maven.execute.model.io.xpp3.NetbeansBuildActionXpp3Reader createI18nReader(java.util.ResourceBundle)
supr org.netbeans.modules.maven.spi.actions.AbstractMavenActionsProvider
hfds MAPPINGS

CLSS public org.netbeans.modules.maven.execute.DefaultReplaceTokenProvider
cons public init(org.netbeans.api.project.Project)
fld public final static java.lang.String METHOD_NAME = "nb.single.run.methodName"
fld public static java.lang.String[] fileBasedProperties
intf org.netbeans.modules.maven.spi.actions.ActionConvertor
intf org.netbeans.modules.maven.spi.actions.ReplaceTokenProvider
meth public java.lang.String convert(java.lang.String,org.openide.util.Lookup)
meth public java.util.Map<java.lang.String,java.lang.String> createReplacements(java.lang.String,org.openide.util.Lookup)
meth public static java.util.Map<java.lang.String,java.lang.String> readVariables()
supr java.lang.Object
hfds ABSOLUTE_PATH,ARTIFACTID,CLASSNAME,CLASSNAME_EXT,CLASSPATHSCOPE,GROUPID,PACK_CLASSNAME,VARIABLE_PREFIX,project

CLSS public org.netbeans.modules.maven.execute.MavenCommandLineExecutor
cons public init(org.netbeans.modules.maven.api.execute.RunConfig,org.openide.windows.InputOutput,org.netbeans.modules.maven.execute.AbstractMavenExecutor$TabContext)
innr public static ExecuteMaven
meth public boolean cancel()
meth public static org.openide.execution.ExecutorTask executeMaven(org.netbeans.modules.maven.api.execute.RunConfig,org.openide.windows.InputOutput,org.netbeans.modules.maven.execute.AbstractMavenExecutor$TabContext)
meth public void run()
supr org.netbeans.modules.maven.execute.AbstractMavenExecutor
hfds ENV_JAVAHOME,ENV_PREFIX,ICON_MAVEN_PROJECT,INTERNAL_PREFIX,KEY_UUID,LOGGER,NETBEANS_MAVEN_COMMAND_LINE,RP,UPDATE_INDEX_RP,VER18,preProcess,preProcessUUID,process,processUUID
hcls WrapperShellConstructor

CLSS public static org.netbeans.modules.maven.execute.MavenCommandLineExecutor$ExecuteMaven
 outer org.netbeans.modules.maven.execute.MavenCommandLineExecutor
cons public init()
meth public org.openide.execution.ExecutorTask execute(org.netbeans.modules.maven.api.execute.RunConfig,org.openide.windows.InputOutput,org.netbeans.modules.maven.execute.AbstractMavenExecutor$TabContext)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.maven.execute.MavenExecutor
intf java.lang.Runnable
meth public abstract org.openide.windows.InputOutput getInputOutput()
meth public abstract void addInitialMessage(java.lang.String,org.openide.windows.OutputListener)
meth public abstract void setTask(org.openide.execution.ExecutorTask)

CLSS public org.netbeans.modules.maven.execute.MavenProxySupport
cons public init(org.netbeans.api.project.Project)
innr public final static !enum Status
innr public final static ProxyResult
meth public java.util.concurrent.CompletableFuture<org.netbeans.modules.maven.execute.MavenProxySupport$ProxyResult> checkProxySettings()
supr java.lang.Object
hfds FILENAME_BASE_SETTINGS,FILENAME_SETTINGS,FILENAME_SETTINGS_EXT,FILENAME_SUFFIX_OLD,ICON_MAVEN_PROJECT,LOG,PORT_DEFAULT_HTTP,PORT_DEFAULT_HTTPS,PROBE_URI_STRING,PROXY_PROBE_TIMEOUT,SUFFIX_NEW_PROXY,SUFFIX_NONE_PROXY,TAG_ACTIVE_END,TAG_ACTIVE_START,TAG_NAME_ACTIVE,TAG_PROXIES,TAG_PROXY,TAG_SETTINGS,acknowledgedResults
hcls LineAndColumn,Processor,ProxyInfo,TagInfo,TextInfo,XppDelegate

CLSS public final static org.netbeans.modules.maven.execute.MavenProxySupport$ProxyResult
 outer org.netbeans.modules.maven.execute.MavenProxySupport
cons public init(org.netbeans.modules.maven.execute.MavenProxySupport$Status,java.net.Proxy)
cons public init(org.netbeans.modules.maven.execute.MavenProxySupport$Status,java.net.Proxy,java.lang.String,java.lang.String,java.lang.String,int,boolean,org.apache.maven.settings.Settings)
meth public java.io.IOException getException()
meth public java.lang.String getProxySpec()
meth public java.lang.String getToolProxy()
meth public java.net.Proxy getProxy()
meth public org.netbeans.modules.maven.execute.BeanRunConfig configure(org.netbeans.modules.maven.execute.BeanRunConfig) throws java.io.IOException
meth public org.netbeans.modules.maven.execute.MavenProxySupport$Status getStatus()
supr java.lang.Object
hfds FMT_PROXY_HOST,FMT_PROXY_PORT,adoc,exception,fileLookup,mavenSettings,nonDefaultPort,proxy,proxyHost,proxyPort,proxySpec,settingsDir,settingsEditor,settingsFileName,settingsLineDoc,status,textInfo,toolProxy

CLSS public final static !enum org.netbeans.modules.maven.execute.MavenProxySupport$Status
 outer org.netbeans.modules.maven.execute.MavenProxySupport
fld public final static org.netbeans.modules.maven.execute.MavenProxySupport$Status ABORT
fld public final static org.netbeans.modules.maven.execute.MavenProxySupport$Status CONTINUE
fld public final static org.netbeans.modules.maven.execute.MavenProxySupport$Status OVERRIDE
fld public final static org.netbeans.modules.maven.execute.MavenProxySupport$Status RECONFIGURED
fld public final static org.netbeans.modules.maven.execute.MavenProxySupport$Status UNKNOWN
meth public static org.netbeans.modules.maven.execute.MavenProxySupport$Status valueOf(java.lang.String)
meth public static org.netbeans.modules.maven.execute.MavenProxySupport$Status[] values()
supr java.lang.Enum<org.netbeans.modules.maven.execute.MavenProxySupport$Status>

CLSS public final org.netbeans.modules.maven.execute.ModelRunConfig
cons public init(org.netbeans.api.project.Project,org.netbeans.modules.maven.execute.model.NetbeansActionMapping,java.lang.String,org.openide.filesystems.FileObject,org.openide.util.Lookup,boolean)
fld public final static java.lang.String EXEC_MERGED = "exec.args.merged"
meth public boolean isFallback()
supr org.netbeans.modules.maven.execute.BeanRunConfig
hfds CP_PLACEHOLDER,EXEC_ARGS,LOG,fallback,model
hcls ExecPluginConfigBuilder

CLSS public org.netbeans.modules.maven.execute.NbGlobalActionGoalProvider
cons public init()
meth protected boolean reloadStream()
meth public java.io.InputStream getActionDefinitionStream()
meth public org.netbeans.modules.maven.execute.model.NetbeansActionMapping[] getCustomMappings()
supr org.netbeans.modules.maven.spi.actions.AbstractMavenActionsProvider
hfds FILENAME,FILENAME_FOLDER,FILE_NAME_PATH,LOG,listener,resetCache

CLSS public abstract org.netbeans.modules.maven.execute.OutputTabMaintainer<%0 extends java.lang.Object>
cons protected init(java.lang.String)
fld protected org.openide.windows.InputOutput io
meth protected abstract java.lang.Class<{org.netbeans.modules.maven.execute.OutputTabMaintainer%0}> tabContextType()
meth protected abstract void reassignAdditionalContext({org.netbeans.modules.maven.execute.OutputTabMaintainer%0})
meth protected abstract {org.netbeans.modules.maven.execute.OutputTabMaintainer%0} createContext()
meth protected final org.openide.windows.InputOutput createInputOutput()
meth protected final void markFreeTab()
meth protected javax.swing.Action[] createNewTabActions()
meth public final org.openide.windows.InputOutput getInputOutput()
supr java.lang.Object
hfds freeTabs,name
hcls AllContext

CLSS public org.netbeans.modules.maven.execute.PrereqCheckerMerger
cons public init()
intf org.netbeans.spi.project.LookupMerger<org.netbeans.modules.maven.api.execute.PrerequisitesChecker>
meth public java.lang.Class<org.netbeans.modules.maven.api.execute.PrerequisitesChecker> getMergeableClass()
meth public org.netbeans.modules.maven.api.execute.PrerequisitesChecker merge(org.openide.util.Lookup)
supr java.lang.Object
hcls Impl

CLSS public org.netbeans.modules.maven.execute.ProxyNonSelectableInputOutput
cons public init(org.openide.windows.InputOutput)
intf org.openide.windows.InputOutput
meth public boolean isClosed()
meth public boolean isErrSeparated()
meth public boolean isFocusTaken()
meth public java.io.Reader flushReader()
meth public java.io.Reader getIn()
meth public org.openide.windows.OutputWriter getErr()
meth public org.openide.windows.OutputWriter getOut()
meth public void closeInputOutput()
meth public void select()
meth public void setErrSeparated(boolean)
meth public void setErrVisible(boolean)
meth public void setFocusTaken(boolean)
meth public void setInputVisible(boolean)
meth public void setOutputVisible(boolean)
supr java.lang.Object
hfds delegate

CLSS public org.netbeans.modules.maven.execute.ReactorChecker
cons public init()
intf org.netbeans.modules.maven.api.execute.PrerequisitesChecker
meth public boolean checkRunConfig(org.netbeans.modules.maven.api.execute.RunConfig)
meth public static org.netbeans.modules.maven.api.NbMavenProject findReactor(org.netbeans.modules.maven.api.NbMavenProject)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public org.netbeans.modules.maven.execute.model.ActionToGoalMapping
cons public init()
intf java.io.Serializable
meth public java.lang.String getModelEncoding()
meth public java.lang.String getPackaging()
 anno 0 java.lang.Deprecated()
meth public java.util.List<org.netbeans.modules.maven.execute.model.NetbeansActionMapping> getActions()
meth public java.util.List<org.netbeans.modules.maven.execute.model.NetbeansActionProfile> getProfiles()
meth public void addAction(org.netbeans.modules.maven.execute.model.NetbeansActionMapping)
meth public void addProfile(org.netbeans.modules.maven.execute.model.NetbeansActionProfile)
meth public void removeAction(org.netbeans.modules.maven.execute.model.NetbeansActionMapping)
meth public void removeProfile(org.netbeans.modules.maven.execute.model.NetbeansActionProfile)
meth public void setActions(java.util.List<org.netbeans.modules.maven.execute.model.NetbeansActionMapping>)
meth public void setModelEncoding(java.lang.String)
meth public void setPackaging(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void setProfiles(java.util.List<org.netbeans.modules.maven.execute.model.NetbeansActionProfile>)
supr java.lang.Object
hfds actions,modelEncoding,packaging,profiles

CLSS public org.netbeans.modules.maven.execute.model.NetbeansActionMapping
cons public init()
intf java.io.Serializable
meth public boolean isRecursive()
meth public java.lang.String getActionName()
meth public java.lang.String getBasedir()
meth public java.lang.String getDisplayName()
meth public java.lang.String getModelEncoding()
meth public java.lang.String getPreAction()
meth public java.lang.String getReactor()
meth public java.util.List<java.lang.String> getActivatedProfiles()
meth public java.util.List<java.lang.String> getGoals()
meth public java.util.List<java.lang.String> getPackagings()
meth public java.util.Map<java.lang.String,java.lang.String> getProperties()
meth public void addActivatedProfile(java.lang.String)
meth public void addGoal(java.lang.String)
meth public void addPackaging(java.lang.String)
meth public void addProperty(java.lang.String,java.lang.String)
meth public void removeActivatedProfile(java.lang.String)
meth public void removeGoal(java.lang.String)
meth public void removePackaging(java.lang.String)
meth public void setActionName(java.lang.String)
meth public void setActivatedProfiles(java.util.List<java.lang.String>)
meth public void setBasedir(java.lang.String)
meth public void setDisplayName(java.lang.String)
meth public void setGoals(java.util.List<java.lang.String>)
meth public void setModelEncoding(java.lang.String)
meth public void setPackagings(java.util.List<java.lang.String>)
meth public void setPreAction(java.lang.String)
meth public void setProperties(java.util.Map<java.lang.String,java.lang.String>)
meth public void setReactor(java.lang.String)
meth public void setRecursive(boolean)
supr java.lang.Object
hfds actionName,activatedProfiles,basedir,displayName,goals,modelEncoding,packagings,preAction,properties,reactor,recursive

CLSS public org.netbeans.modules.maven.execute.model.NetbeansActionProfile
cons public init()
meth public java.lang.String getDisplayName()
meth public java.lang.String getId()
meth public java.util.List<org.netbeans.modules.maven.execute.model.NetbeansActionMapping> getActions()
meth public void addAction(org.netbeans.modules.maven.execute.model.NetbeansActionMapping)
meth public void removeAction(org.netbeans.modules.maven.execute.model.NetbeansActionMapping)
meth public void setActions(java.util.List<org.netbeans.modules.maven.execute.model.NetbeansActionMapping>)
meth public void setDisplayName(java.lang.String)
meth public void setId(java.lang.String)
supr java.lang.Object
hfds actions,displayName,id

CLSS public abstract org.netbeans.modules.maven.execute.model.NetbeansActionReader
cons public init()
meth protected abstract java.io.Reader performDynamicSubstitutions(java.util.Map<java.lang.String,java.lang.String>,java.lang.String) throws java.io.IOException
meth protected abstract java.lang.String getRawMappingsAsString()
meth public final org.netbeans.modules.maven.execute.model.NetbeansActionMapping getMappingForAction(org.netbeans.modules.maven.execute.model.io.xpp3.NetbeansBuildActionXpp3Reader,java.util.logging.Logger,java.lang.String,boolean[],org.netbeans.api.project.Project,java.lang.String,java.util.Map<java.lang.String,java.lang.String>)
supr java.lang.Object

CLSS public org.netbeans.modules.maven.execute.model.io.jdom.NetbeansBuildActionJDOMWriter
cons public init()
innr public Counter
meth protected org.jdom2.Element findAndReplaceProperties(org.netbeans.modules.maven.execute.model.io.jdom.NetbeansBuildActionJDOMWriter$Counter,org.jdom2.Element,java.lang.String,java.util.Map)
meth protected org.jdom2.Element findAndReplaceSimpleElement(org.netbeans.modules.maven.execute.model.io.jdom.NetbeansBuildActionJDOMWriter$Counter,org.jdom2.Element,java.lang.String,java.lang.String,java.lang.String)
meth protected org.jdom2.Element findAndReplaceSimpleLists(org.netbeans.modules.maven.execute.model.io.jdom.NetbeansBuildActionJDOMWriter$Counter,org.jdom2.Element,java.util.Collection,java.lang.String,java.lang.String)
meth protected org.jdom2.Element findAndReplaceXpp3DOM(org.netbeans.modules.maven.execute.model.io.jdom.NetbeansBuildActionJDOMWriter$Counter,org.jdom2.Element,java.lang.String,org.codehaus.plexus.util.xml.Xpp3Dom)
meth protected org.jdom2.Element updateElement(org.netbeans.modules.maven.execute.model.io.jdom.NetbeansBuildActionJDOMWriter$Counter,org.jdom2.Element,java.lang.String,boolean)
meth protected void insertAtPreferredLocation(org.jdom2.Element,org.jdom2.Element,org.netbeans.modules.maven.execute.model.io.jdom.NetbeansBuildActionJDOMWriter$Counter)
meth protected void iterate2NetbeansActionMapping(org.netbeans.modules.maven.execute.model.io.jdom.NetbeansBuildActionJDOMWriter$Counter,org.jdom2.Element,java.util.Collection,java.lang.String)
meth protected void replaceXpp3DOM(org.jdom2.Element,org.codehaus.plexus.util.xml.Xpp3Dom,org.netbeans.modules.maven.execute.model.io.jdom.NetbeansBuildActionJDOMWriter$Counter)
meth protected void updateActionToGoalMapping(org.netbeans.modules.maven.execute.model.ActionToGoalMapping,java.lang.String,org.netbeans.modules.maven.execute.model.io.jdom.NetbeansBuildActionJDOMWriter$Counter,org.jdom2.Element)
meth protected void updateNetbeansActionMapping(org.netbeans.modules.maven.execute.model.NetbeansActionMapping,java.lang.String,org.netbeans.modules.maven.execute.model.io.jdom.NetbeansBuildActionJDOMWriter$Counter,org.jdom2.Element)
meth public void write(org.netbeans.modules.maven.execute.model.ActionToGoalMapping,org.jdom2.Document,java.io.OutputStreamWriter) throws java.io.IOException
meth public void write(org.netbeans.modules.maven.execute.model.ActionToGoalMapping,org.jdom2.Document,java.io.Writer,org.jdom2.output.Format) throws java.io.IOException
supr java.lang.Object
hfds factory,lineSeparator

CLSS public org.netbeans.modules.maven.execute.model.io.jdom.NetbeansBuildActionJDOMWriter$Counter
 outer org.netbeans.modules.maven.execute.model.io.jdom.NetbeansBuildActionJDOMWriter
cons public init(org.netbeans.modules.maven.execute.model.io.jdom.NetbeansBuildActionJDOMWriter,int)
meth public int getCurrentIndex()
meth public int getDepth()
meth public void increaseCount()
supr java.lang.Object
hfds currentIndex,level

CLSS public org.netbeans.modules.maven.execute.model.io.xpp3.NetbeansBuildActionXpp3Reader
cons public init()
meth public boolean getAddDefaultEntities()
meth public boolean getBooleanValue(java.lang.String,java.lang.String,org.codehaus.plexus.util.xml.pull.XmlPullParser,java.lang.String) throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public char getCharacterValue(java.lang.String,java.lang.String,org.codehaus.plexus.util.xml.pull.XmlPullParser) throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public int getIntegerValue(java.lang.String,java.lang.String,org.codehaus.plexus.util.xml.pull.XmlPullParser,boolean) throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public java.lang.String getTrimmedValue(java.lang.String)
meth public long getLongValue(java.lang.String,java.lang.String,org.codehaus.plexus.util.xml.pull.XmlPullParser,boolean) throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public org.netbeans.modules.maven.execute.model.ActionToGoalMapping read(java.io.InputStream) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public org.netbeans.modules.maven.execute.model.ActionToGoalMapping read(java.io.InputStream,boolean) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public org.netbeans.modules.maven.execute.model.ActionToGoalMapping read(java.io.Reader) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public org.netbeans.modules.maven.execute.model.ActionToGoalMapping read(java.io.Reader,boolean) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public void setAddDefaultEntities(boolean)
supr java.lang.Object
hfds addDefaultEntities

CLSS public org.netbeans.modules.maven.execute.model.io.xpp3.NetbeansBuildActionXpp3Writer
cons public init()
meth public void write(java.io.Writer,org.netbeans.modules.maven.execute.model.ActionToGoalMapping) throws java.io.IOException
supr java.lang.Object
hfds NAMESPACE

CLSS public abstract interface org.netbeans.modules.maven.model.ModelOperation<%0 extends org.netbeans.modules.xml.xam.dom.AbstractDocumentModel<? extends org.netbeans.modules.xml.xam.dom.DocumentComponent<?>>>
meth public abstract void performOperation({org.netbeans.modules.maven.model.ModelOperation%0})

CLSS public final org.netbeans.modules.maven.options.DontShowAgainSettings
meth protected final java.util.prefs.Preferences getPreferences()
meth public boolean showWarningAboutApplicationCoS()
meth public boolean showWarningAboutBuildWithDependencies()
meth public static org.netbeans.modules.maven.options.DontShowAgainSettings getDefault()
meth public void dontShowWarningAboutBuildWithDependenciesAnymore()
meth public void dontshowWarningAboutApplicationCoSAnymore()
supr java.lang.Object
hfds INSTANCE

CLSS public org.netbeans.modules.maven.options.GlobalOptionsPanel
cons public init()
supr javax.swing.JPanel
hfds descMap,jList1,jScrollPane1,jScrollPane2,jTextArea1,lblOptions

CLSS public final org.netbeans.modules.maven.options.MavenCommandSettings
fld public final static java.lang.String COMMAND_CREATE_ARCHETYPENG = "createArchetypeNG"
fld public final static java.lang.String COMMAND_INSTALL_FILE = "installFile"
fld public final static java.lang.String COMMAND_SCM_CHECKOUT = "scmCheckout"
meth protected final java.lang.String getProperty(java.lang.String)
meth protected final java.lang.String putProperty(java.lang.String,java.lang.String)
meth protected final java.util.prefs.Preferences getPreferences()
meth public java.lang.String getCommand(java.lang.String)
meth public static org.netbeans.modules.maven.options.MavenCommandSettings getDefault()
supr java.lang.Object
hfds INSTANCE

CLSS public org.netbeans.modules.maven.options.MavenGroupCategory
cons public init()
intf org.netbeans.spi.project.ui.support.ProjectCustomizer$CompositeCategoryProvider
meth public javax.swing.JComponent createComponent(org.netbeans.spi.project.ui.support.ProjectCustomizer$Category,org.openide.util.Lookup)
meth public org.netbeans.spi.project.ui.support.ProjectCustomizer$Category createCategory(org.openide.util.Lookup)
supr java.lang.Object

CLSS public org.netbeans.modules.maven.options.MavenGroupPanel
meth public void applyValues(org.netbeans.api.project.ui.ProjectGroup)
supr javax.swing.JPanel
hfds SEPARATOR,category,comMavenHome,globalMavenValue,lastSelected,lblCommandLine,lblExternalVersion,listItemChangedListener,mavenHomeDataModel,mavenRuntimeHome,predefinedRuntimes,userDefinedMavenRuntimes
hcls ComboBoxRenderer

CLSS public org.netbeans.modules.maven.options.MavenOptionController
cons public init()
fld public final static java.lang.String OPTIONS_SUBPATH = "Maven"
fld public final static java.lang.String TEMPLATE = "<?xml version=\u00221.0\u0022 encoding=\u0022UTF-8\u0022?><settings xmlns=\u0022http://maven.apache.org/POM/4.0.0\u0022 xmlns:xsi=\u0022http://www.w3.org/2001/XMLSchema-instance\u0022  xsi:schemaLocation=\u0022http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd\u0022></settings>"
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
hfds listeners,panel,setts

CLSS public final org.netbeans.modules.maven.options.MavenSettings
fld public final static java.lang.String PROP_NETWORK_PROXY = "networkProxy"
fld public final static java.lang.String PROP_PROJECTNODE_NAME_PATTERN = "project.displayName"
innr public final static !enum DownloadStrategy
innr public final static !enum OutputTabName
meth public boolean isAlwaysShowOutput()
meth public boolean isCollapseSuccessFolds()
meth public boolean isInteractive()
meth public boolean isOutputTabShowConfig()
meth public boolean isPreferMavenWrapper()
meth public boolean isReuseOutputTabs()
meth public boolean isShowDebug()
meth public boolean isShowErrors()
meth public boolean isShowLoggingLevel()
meth public boolean isSkipTests()
meth public boolean isUpdateSnapshots()
meth public boolean isUseBestMaven()
meth public boolean isUseBestMavenAltLocation()
meth public boolean isVMOptionsWrap()
meth public java.lang.Boolean isOffline()
meth public java.lang.String getBestMavenAltLocation()
meth public java.lang.String getDefaultJdk()
meth public java.lang.String getDefaultOptions()
meth public java.lang.String getLastArchetypeGroupId()
meth public java.lang.String getLastArchetypeVersion()
meth public java.lang.String getProjectNodeNamePattern()
meth public java.util.List<java.lang.String> getUserDefinedMavenRuntimes()
meth public org.netbeans.modules.maven.options.MavenSettings$DownloadStrategy getBinaryDownloadStrategy()
meth public org.netbeans.modules.maven.options.MavenSettings$DownloadStrategy getJavadocDownloadStrategy()
meth public org.netbeans.modules.maven.options.MavenSettings$DownloadStrategy getSourceDownloadStrategy()
meth public org.netbeans.modules.maven.options.MavenSettings$OutputTabName getOutputTabName()
meth public org.netbeans.modules.maven.options.NetworkProxySettings getNetworkProxy()
meth public static boolean isMavenDaemon(java.nio.file.Path)
meth public static java.lang.String getCommandLineMavenVersion()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static java.lang.String getCommandLineMavenVersion(java.io.File)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static java.lang.String getDefaultExternalMavenRuntime()
meth public static org.netbeans.modules.maven.options.MavenSettings getDefault()
meth public void addWeakPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setAlwaysShowOutput(boolean)
meth public void setBestMavenAltLocation(java.lang.String)
meth public void setBinaryDownloadStrategy(org.netbeans.modules.maven.options.MavenSettings$DownloadStrategy)
meth public void setCollapseSuccessFolds(boolean)
meth public void setDefaultJdk(java.lang.String)
meth public void setDefaultOptions(java.lang.String)
meth public void setJavadocDownloadStrategy(org.netbeans.modules.maven.options.MavenSettings$DownloadStrategy)
meth public void setLastArchetypeGroupId(java.lang.String)
meth public void setLastArchetypeVersion(java.lang.String)
meth public void setMavenRuntimes(java.util.List<java.lang.String>)
meth public void setNetworkProxy(org.netbeans.modules.maven.options.NetworkProxySettings)
meth public void setOutputTabName(org.netbeans.modules.maven.options.MavenSettings$OutputTabName)
meth public void setOutputTabShowConfig(boolean)
meth public void setPreferMavenWrapper(boolean)
meth public void setProjectNodeNamePattern(java.lang.String)
meth public void setReuseOutputTabs(boolean)
meth public void setShowLoggingLevel(boolean)
meth public void setSkipTests(boolean)
meth public void setSourceDownloadStrategy(org.netbeans.modules.maven.options.MavenSettings$DownloadStrategy)
meth public void setUseBestMaven(boolean)
meth public void setUseBestMavenAltLocation(boolean)
meth public void setVMOptionsWrap(boolean)
supr java.lang.Object
hfds DEFAULT_PROXY_BEHAVIOUR,INSTANCE,MAVEN_CORE_JAR_PATTERN,PROP_ALWAYS_OUTPUT,PROP_BINARY_DOWNLOAD,PROP_CHECKSUM_POLICY,PROP_COLLAPSE_FOLDS,PROP_DEBUG,PROP_DEFAULT_JDK,PROP_DEFAULT_OPTIONS,PROP_ERRORS,PROP_EXPERIMENTAL_ALTERNATE_LOCATION,PROP_EXPERIMENTAL_USE_ALTERNATE_LOCATION,PROP_EXPERIMENTAL_USE_BEST_MAVEN,PROP_FAILURE_BEHAVIOUR,PROP_JAVADOC_DOWNLOAD,PROP_LAST_ARCHETYPE_GROUPID,PROP_LAST_ARCHETYPE_VERSION,PROP_MAVEN_RUNTIMES,PROP_OUTPUT_TAB_CONFIG,PROP_OUTPUT_TAB_NAME,PROP_PLUGIN_POLICY,PROP_PREFER_WRAPPER,PROP_REUSE_OUTPUT,PROP_SHOW_LOGGING_LEVEL,PROP_SKIP_TESTS,PROP_SOURCE_DOWNLOAD,PROP_USE_REGISTRY,PROP_VM_OPTIONS_WRAP,SYSPROP_DEFAULT_PROXY_BEHAVIOUR,listeners

CLSS public final static !enum org.netbeans.modules.maven.options.MavenSettings$DownloadStrategy
 outer org.netbeans.modules.maven.options.MavenSettings
fld public final static org.netbeans.modules.maven.options.MavenSettings$DownloadStrategy EVERY_OPEN
fld public final static org.netbeans.modules.maven.options.MavenSettings$DownloadStrategy FIRST_OPEN
fld public final static org.netbeans.modules.maven.options.MavenSettings$DownloadStrategy NEVER
meth public static org.netbeans.modules.maven.options.MavenSettings$DownloadStrategy valueOf(java.lang.String)
meth public static org.netbeans.modules.maven.options.MavenSettings$DownloadStrategy[] values()
supr java.lang.Enum<org.netbeans.modules.maven.options.MavenSettings$DownloadStrategy>

CLSS public final static !enum org.netbeans.modules.maven.options.MavenSettings$OutputTabName
 outer org.netbeans.modules.maven.options.MavenSettings
fld public final static org.netbeans.modules.maven.options.MavenSettings$OutputTabName PROJECT_ID
fld public final static org.netbeans.modules.maven.options.MavenSettings$OutputTabName PROJECT_NAME
meth public static org.netbeans.modules.maven.options.MavenSettings$OutputTabName valueOf(java.lang.String)
meth public static org.netbeans.modules.maven.options.MavenSettings$OutputTabName[] values()
supr java.lang.Enum<org.netbeans.modules.maven.options.MavenSettings$OutputTabName>

CLSS public final org.netbeans.modules.maven.options.MavenVersionSettings
fld public final static java.lang.String VERSION_COMPILER = "maven-compiler-plugin"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String VERSION_RESOURCES = "maven-resources-plugin"
 anno 0 java.lang.Deprecated()
meth public java.lang.String getNBVersion()
meth public java.lang.String getVersion(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public java.lang.String getVersion(java.lang.String,java.lang.String)
meth public static org.netbeans.modules.maven.options.MavenVersionSettings getDefault()
supr java.lang.Object
hfds INSTANCE,fallback

CLSS public final !enum org.netbeans.modules.maven.options.NetworkProxySettings
fld public final static org.netbeans.modules.maven.options.NetworkProxySettings ASK
fld public final static org.netbeans.modules.maven.options.NetworkProxySettings IGNORE
fld public final static org.netbeans.modules.maven.options.NetworkProxySettings NOTICE
fld public final static org.netbeans.modules.maven.options.NetworkProxySettings OVERRIDE
fld public final static org.netbeans.modules.maven.options.NetworkProxySettings UPDATE
meth public java.lang.String toString()
meth public static boolean allowProxyOverride()
meth public static org.netbeans.modules.maven.options.NetworkProxySettings valueOf(java.lang.String)
meth public static org.netbeans.modules.maven.options.NetworkProxySettings[] values()
supr java.lang.Enum<org.netbeans.modules.maven.options.NetworkProxySettings>
hfds BRANDING_API_OVERRIDE_ENABLED,displayName

CLSS public org.netbeans.modules.maven.options.SettingsPanel
fld public final static int RUNTIME_COUNT_LIMIT = 5
fld public final static java.lang.String BUNDLED_RUNTIME_VERSION
meth public void applyValues()
meth public void setValues()
supr javax.swing.JPanel
hfds AVAILABLE_OPTIONS,RP,SEPARATOR,bgIndexFilter,btnDirectory,btnGoals,btnIndex,btnOptions,buttonGroup1,cbAlternateLocation,cbAlwaysShow,cbCollapseSuccessFolds,cbEnableIndexDownload,cbEnableIndexing,cbEnableMultiThreading,cbNetworkProxy,cbOutputTabShowConfig,cbPreferWrapper,cbProjectNodeNameMode,cbReuse,cbShowInfoLevel,cbSkipTests,cbUseBestMaven,changed,comBinaries,comIndex,comJavadoc,comJdkHome,comManageJdks,comMavenHome,comSource,completer,controller,jLabel3,jLabel4,jScrollPane1,jdkHomeDataModel,lastSelected,lbNetworkSettings,lblBinaries,lblCategory,lblCommandLine,lblDirectory,lblExternalVersion,lblHint,lblIndex,lblIndexFilter,lblJavadoc,lblJdkHome,lblOptions,lblOutputTab,lblSource,listItemChangedListener,listener,lstCategory,mavenHomeDataModel,mavenRuntimeHome,permissionsTable,plnExperimental,pnlAppearance,pnlCards,pnlDependencies,pnlExecution,pnlIndex,predefinedRuntimes,rb2Years,rb5Years,rbFullIndex,rbOutputTabId,rbOutputTabName,txtDirectory,txtOptions,txtProjectNodeNameCustomPattern,userDefinedMavenRuntimes,userDefinedMavenRuntimesStored,valid
hcls ActionListenerImpl,ComboBoxRenderer,DocumentListenerImpl,IndexDownloadPermissionTableModel

CLSS public org.netbeans.modules.maven.options.UnsetProxyChecker
cons public init()
intf org.netbeans.modules.maven.api.execute.ExecutionResultChecker
meth public void executionResult(org.netbeans.modules.maven.api.execute.RunConfig,org.netbeans.modules.maven.api.execute.ExecutionContext,int)
supr java.lang.Object
hfds USE_SYSTEM_PROXIES

CLSS public org.netbeans.modules.maven.spi.IconResources
fld public final static java.lang.String ARTIFACT_ICON = "org/netbeans/modules/maven/ArtifactIcon.png"
fld public final static java.lang.String BROKEN_PROJECT_BADGE_ICON = "org/netbeans/modules/maven/brokenProjectBadge.png"
fld public final static java.lang.String DEPENDENCY_ICON = "org/netbeans/modules/maven/DependencyIcon.png"
fld public final static java.lang.String ICON_DEPENDENCY_JAR = "org/netbeans/modules/maven/spi/nodes/DependencyJar.gif"
fld public final static java.lang.String JAVADOC_BADGE_ICON = "org/netbeans/modules/maven/DependencyJavadocIncluded.png"
fld public final static java.lang.String MANAGED_BADGE_ICON = "org/netbeans/modules/maven/DependencyManaged.png"
fld public final static java.lang.String MAVEN_ICON = "org/netbeans/modules/maven/resources/Maven2Icon.gif"
fld public final static java.lang.String MOJO_ICON = "org/netbeans/modules/maven/execute/ui/mojo.png"
fld public final static java.lang.String SOURCE_BADGE_ICON = "org/netbeans/modules/maven/DependencySrcIncluded.png"
fld public final static java.lang.String TRANSITIVE_ARTIFACT_ICON = "org/netbeans/modules/maven/TransitiveArtifactIcon.png"
fld public final static java.lang.String TRANSITIVE_DEPENDENCY_ICON = "org/netbeans/modules/maven/TransitiveDependencyIcon.png"
fld public final static java.lang.String TRANSITIVE_MAVEN_ICON = "org/netbeans/modules/maven/TransitiveMaven2Icon.png"
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.maven.spi.PackagingProvider
meth public abstract java.lang.String packaging(org.netbeans.api.project.Project)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract org.netbeans.modules.maven.spi.actions.AbstractMavenActionsProvider
cons public init()
fld protected org.netbeans.modules.maven.execute.model.ActionToGoalMapping originalMappings
fld protected org.netbeans.modules.maven.execute.model.io.xpp3.NetbeansBuildActionXpp3Reader reader
intf org.netbeans.modules.maven.spi.actions.MavenActionsProvider
meth protected abstract java.io.InputStream getActionDefinitionStream()
meth protected boolean reloadStream()
meth protected java.io.Reader performDynamicSubstitutions(java.util.Map<java.lang.String,java.lang.String>,java.lang.String) throws java.io.IOException
meth protected static org.openide.filesystems.FileObject[] extractFileObjectsfromLookup(org.openide.util.Lookup)
meth public boolean isActionEnable(java.lang.String,org.netbeans.api.project.Project,org.openide.util.Lookup)
meth public final boolean isActionEnable(java.lang.String,java.lang.String)
meth public final org.netbeans.modules.maven.api.execute.RunConfig createConfigForDefaultAction(java.lang.String,org.netbeans.api.project.Project,org.openide.util.Lookup)
meth public java.lang.String getRawMappingsAsString()
meth public java.util.Set<java.lang.String> getSupportedDefaultActions()
meth public org.netbeans.modules.maven.execute.model.ActionToGoalMapping getRawMappings()
meth public org.netbeans.modules.maven.execute.model.NetbeansActionMapping getMappingForAction(java.lang.String,org.netbeans.api.project.Project)
meth public static java.lang.String dynamicSubstitutions(java.util.Map<java.lang.String,java.lang.String>,java.lang.String)
meth public static org.netbeans.modules.maven.spi.actions.MavenActionsProvider fromNbActions(org.netbeans.api.project.Project,java.net.URL)
supr java.lang.Object
hfds LOG,writer
hcls ResourceConfigAwareProvider

CLSS public abstract interface org.netbeans.modules.maven.spi.actions.ActionConvertor
meth public abstract java.lang.String convert(java.lang.String,org.openide.util.Lookup)

CLSS public abstract interface org.netbeans.modules.maven.spi.actions.MavenActionsProvider
meth public abstract boolean isActionEnable(java.lang.String,org.netbeans.api.project.Project,org.openide.util.Lookup)
meth public abstract java.util.Set<java.lang.String> getSupportedDefaultActions()
meth public abstract org.netbeans.modules.maven.api.execute.RunConfig createConfigForDefaultAction(java.lang.String,org.netbeans.api.project.Project,org.openide.util.Lookup)
meth public abstract org.netbeans.modules.maven.execute.model.NetbeansActionMapping getMappingForAction(java.lang.String,org.netbeans.api.project.Project)

CLSS public abstract interface org.netbeans.modules.maven.spi.actions.ReplaceTokenProvider
meth public abstract java.util.Map<java.lang.String,java.lang.String> createReplacements(java.lang.String,org.openide.util.Lookup)

CLSS public abstract interface org.netbeans.modules.maven.spi.cos.AdditionalDestination
meth public abstract void copy(org.openide.filesystems.FileObject,java.lang.String)
meth public abstract void delete(org.openide.filesystems.FileObject,java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.spi.cos.CoSAlternativeExecutorImplementation
meth public abstract boolean execute(org.netbeans.modules.maven.api.execute.RunConfig,org.netbeans.modules.maven.api.execute.ExecutionContext)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.modules.maven.spi.cos.CompileOnSaveSkipper
meth public abstract boolean skip(org.netbeans.modules.maven.api.execute.RunConfig,boolean,long)

CLSS public abstract interface org.netbeans.modules.maven.spi.customizer.SelectedItemsTablePersister
meth public abstract java.util.SortedMap<java.lang.String,java.lang.Boolean> read()
meth public abstract void write(java.util.SortedMap<java.lang.String,java.lang.Boolean>)

CLSS public final org.netbeans.modules.maven.spi.customizer.TextToValueConversions
meth public static java.util.Map<java.lang.String,java.lang.String> convertStringToActionProperties(java.lang.String)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.maven.spi.debug.AdditionalDebuggedProjects
meth public abstract java.util.List<org.netbeans.api.project.Project> getProjects()

CLSS public abstract interface org.netbeans.modules.maven.spi.debug.MavenDebugger
meth public abstract void attachDebugger(org.openide.windows.InputOutput,java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws java.lang.Exception

CLSS public final org.netbeans.modules.maven.spi.grammar.DialogFactory
meth public static java.lang.String[] showDependencyDialog(org.netbeans.api.project.Project,boolean)
meth public static java.util.Map<org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.Artifact>> showDependencyExcludeDialog(org.netbeans.api.project.Project)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.maven.spi.grammar.GoalsProvider
meth public abstract java.util.Set<java.lang.String> getAvailableGoals()

CLSS public org.netbeans.modules.maven.spi.newproject.CreateProjectBuilder
cons public init(java.io.File,java.lang.String,java.lang.String,java.lang.String)
innr public abstract interface static AdditionalChangeHandle
innr public abstract interface static PomOperationsHandle
innr public final static Context
meth public org.netbeans.modules.maven.spi.newproject.CreateProjectBuilder setAdditionalNonPomWork(org.netbeans.modules.maven.spi.newproject.CreateProjectBuilder$AdditionalChangeHandle)
meth public org.netbeans.modules.maven.spi.newproject.CreateProjectBuilder setAdditionalOperations(org.netbeans.modules.maven.spi.newproject.CreateProjectBuilder$PomOperationsHandle)
meth public org.netbeans.modules.maven.spi.newproject.CreateProjectBuilder setPackageName(java.lang.String)
meth public org.netbeans.modules.maven.spi.newproject.CreateProjectBuilder setPackaging(java.lang.String)
meth public org.netbeans.modules.maven.spi.newproject.CreateProjectBuilder setParentProject(org.apache.maven.project.MavenProject)
meth public org.netbeans.modules.maven.spi.newproject.CreateProjectBuilder setProgressHandle(org.netbeans.api.progress.ProgressHandle)
meth public org.netbeans.modules.maven.spi.newproject.CreateProjectBuilder setUpdateParent(boolean)
meth public void create()
supr java.lang.Object
hfds SKELETON,artifactId,groupId,moreWork,operations,packageName,packaging,parentProject,progressHandle,projectDirectory,updateParent,version
hcls AddModuleToParentOperation,BasicPropertiesOperation

CLSS public abstract interface static org.netbeans.modules.maven.spi.newproject.CreateProjectBuilder$AdditionalChangeHandle
 outer org.netbeans.modules.maven.spi.newproject.CreateProjectBuilder
meth public abstract java.lang.Runnable createAdditionalChange(org.netbeans.modules.maven.spi.newproject.CreateProjectBuilder$Context)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public final static org.netbeans.modules.maven.spi.newproject.CreateProjectBuilder$Context
 outer org.netbeans.modules.maven.spi.newproject.CreateProjectBuilder
meth public java.io.File getProjectDirectory()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getArtifactId()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getGroupId()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getPackageName()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getPackaging()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getVersion()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.apache.maven.project.MavenProject getParent()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
supr java.lang.Object
hfds artifactId,groupId,packageName,packaging,parent,projectDirectory,version

CLSS public abstract interface static org.netbeans.modules.maven.spi.newproject.CreateProjectBuilder$PomOperationsHandle
 outer org.netbeans.modules.maven.spi.newproject.CreateProjectBuilder
meth public abstract java.util.List<org.netbeans.modules.maven.model.ModelOperation<org.netbeans.modules.maven.model.pom.POMModel>> createPomOperations(org.netbeans.modules.maven.spi.newproject.CreateProjectBuilder$Context)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract org.netbeans.modules.maven.spi.nodes.AbstractMavenNodeList<%0 extends java.lang.Object>
cons public init()
intf org.netbeans.spi.project.ui.support.NodeList<{org.netbeans.modules.maven.spi.nodes.AbstractMavenNodeList%0}>
meth protected void fireChange()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void addNotify()
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void removeNotify()
supr java.lang.Object
hfds cs

CLSS public abstract interface org.netbeans.modules.maven.spi.nodes.DependencyTypeIconBadge
meth public abstract java.awt.Image getBadgeIcon(org.openide.filesystems.FileObject,org.apache.maven.artifact.Artifact)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()

CLSS public final org.netbeans.modules.maven.spi.nodes.MavenNodeFactory
innr public static ArtifactNode
innr public static VersionNode
meth public static org.netbeans.modules.maven.spi.nodes.MavenNodeFactory$ArtifactNode createArtifactNode(java.lang.String,java.util.List<org.netbeans.modules.maven.indexer.api.NBVersionInfo>)
meth public static org.netbeans.modules.maven.spi.nodes.MavenNodeFactory$VersionNode createVersionNode(org.netbeans.modules.maven.indexer.api.NBVersionInfo,boolean)
supr java.lang.Object
hfds ARTIFACT_BADGE,DELIMITER

CLSS public static org.netbeans.modules.maven.spi.nodes.MavenNodeFactory$ArtifactNode
 outer org.netbeans.modules.maven.spi.nodes.MavenNodeFactory
cons public init(java.lang.String,java.util.List<org.netbeans.modules.maven.indexer.api.NBVersionInfo>)
meth public java.awt.Image getIcon(int)
meth public java.awt.Image getOpenedIcon(int)
meth public java.util.List<org.netbeans.modules.maven.indexer.api.NBVersionInfo> getVersionInfos()
meth public void setVersionInfos(java.util.List<org.netbeans.modules.maven.indexer.api.NBVersionInfo>)
supr org.openide.nodes.AbstractNode
hfds myChildren,versionInfos
hcls ArtifactNodeChildren

CLSS public static org.netbeans.modules.maven.spi.nodes.MavenNodeFactory$VersionNode
 outer org.netbeans.modules.maven.spi.nodes.MavenNodeFactory
cons public init(org.netbeans.modules.maven.indexer.api.NBVersionInfo,boolean)
meth public java.lang.String getShortDescription()
meth public org.netbeans.modules.maven.indexer.api.NBVersionInfo getNBVersionInfo()
supr org.openide.nodes.AbstractNode
hfds fromDepMng,nbvi

CLSS public org.netbeans.modules.maven.spi.nodes.NodeUtils
fld public final static java.lang.String ICON_DEPENDENCY_JAR = "org/netbeans/modules/maven/spi/nodes/DependencyJar.gif"
 anno 0 java.lang.Deprecated()
meth public static java.awt.Image getTreeFolderIcon(boolean)
meth public static org.openide.filesystems.FileObject readOnlyLocalRepositoryFile(org.openide.filesystems.FileObject)
meth public static void openPomFile(org.openide.filesystems.FileObject)
supr java.lang.Object
hfds ICON_KEY_UIMANAGER,ICON_KEY_UIMANAGER_NB,ICON_PATH,LOG,OPENED_ICON_KEY_UIMANAGER,OPENED_ICON_KEY_UIMANAGER_NB,OPENED_ICON_PATH,repoFS

CLSS public abstract interface org.netbeans.modules.maven.spi.nodes.OtherSourcesExclude
meth public abstract java.util.Set<java.nio.file.Path> excludedFolders()

CLSS public abstract interface org.netbeans.modules.maven.spi.nodes.SpecialIcon
meth public abstract javax.swing.Icon getIcon()
 anno 0 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.modules.maven.spi.queries.ForeignClassBundler
meth public abstract boolean preferSources()
meth public abstract void resetCachedValue()

CLSS public abstract interface org.netbeans.modules.maven.spi.queries.JavaLikeRootProvider
meth public abstract java.lang.String kind()

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

CLSS public abstract interface org.netbeans.spi.project.LookupMerger<%0 extends java.lang.Object>
innr public abstract interface static !annotation Registration
meth public abstract java.lang.Class<{org.netbeans.spi.project.LookupMerger%0}> getMergeableClass()
meth public abstract {org.netbeans.spi.project.LookupMerger%0} merge(org.openide.util.Lookup)

CLSS public abstract interface org.netbeans.spi.project.ProjectConfiguration
meth public abstract java.lang.String getDisplayName()

CLSS public final org.netbeans.spi.project.ui.support.BuildExecutionSupport
innr public abstract interface static ActionItem
innr public abstract interface static Item
meth public static org.netbeans.spi.project.ui.support.BuildExecutionSupport$Item getLastFinishedItem()
meth public static void addChangeListener(javax.swing.event.ChangeListener)
meth public static void registerFinishedItem(org.netbeans.spi.project.ui.support.BuildExecutionSupport$Item)
meth public static void registerRunningItem(org.netbeans.spi.project.ui.support.BuildExecutionSupport$Item)
meth public static void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object

CLSS public abstract interface static org.netbeans.spi.project.ui.support.BuildExecutionSupport$ActionItem
 outer org.netbeans.spi.project.ui.support.BuildExecutionSupport
intf org.netbeans.spi.project.ui.support.BuildExecutionSupport$Item
meth public abstract java.lang.String getAction()
meth public abstract org.openide.filesystems.FileObject getProjectDirectory()

CLSS public abstract interface static org.netbeans.spi.project.ui.support.BuildExecutionSupport$Item
 outer org.netbeans.spi.project.ui.support.BuildExecutionSupport
meth public abstract boolean isRunning()
meth public abstract java.lang.String getDisplayName()
meth public abstract void repeatExecution()
meth public abstract void stopRunning()

CLSS public abstract interface org.netbeans.spi.project.ui.support.NodeList<%0 extends java.lang.Object>
meth public abstract java.util.List<{org.netbeans.spi.project.ui.support.NodeList%0}> keys()
meth public abstract org.openide.nodes.Node node({org.netbeans.spi.project.ui.support.NodeList%0})
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void addNotify()
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeNotify()

CLSS public final org.netbeans.spi.project.ui.support.ProjectCustomizer
innr public abstract interface static CategoryComponentProvider
innr public abstract interface static CompositeCategoryProvider
innr public final static Category
meth public static java.awt.Dialog createCustomizerDialog(java.lang.String,org.openide.util.Lookup,java.lang.String,java.awt.event.ActionListener,java.awt.event.ActionListener,org.openide.util.HelpCtx)
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
meth public static java.awt.Dialog createCustomizerDialog(java.lang.String,org.openide.util.Lookup,java.lang.String,java.awt.event.ActionListener,org.openide.util.HelpCtx)
 anno 4 org.netbeans.api.annotations.common.NonNull()
meth public static java.awt.Dialog createCustomizerDialog(org.netbeans.spi.project.ui.support.ProjectCustomizer$Category[],org.netbeans.spi.project.ui.support.ProjectCustomizer$CategoryComponentProvider,java.lang.String,java.awt.event.ActionListener,java.awt.event.ActionListener,org.openide.util.HelpCtx)
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
meth public static java.awt.Dialog createCustomizerDialog(org.netbeans.spi.project.ui.support.ProjectCustomizer$Category[],org.netbeans.spi.project.ui.support.ProjectCustomizer$CategoryComponentProvider,java.lang.String,java.awt.event.ActionListener,org.openide.util.HelpCtx)
 anno 4 org.netbeans.api.annotations.common.NonNull()
meth public static javax.swing.ComboBoxModel encodingModel(java.lang.String)
meth public static javax.swing.ListCellRenderer encodingRenderer()
supr java.lang.Object
hfds LOG
hcls DelegateCategoryProvider,EncodingModel,EncodingRenderer

CLSS public abstract interface static org.netbeans.spi.project.ui.support.ProjectCustomizer$CompositeCategoryProvider
 outer org.netbeans.spi.project.ui.support.ProjectCustomizer
innr public abstract interface static !annotation Registration
innr public abstract interface static !annotation Registrations
meth public abstract javax.swing.JComponent createComponent(org.netbeans.spi.project.ui.support.ProjectCustomizer$Category,org.openide.util.Lookup)
meth public abstract org.netbeans.spi.project.ui.support.ProjectCustomizer$Category createCategory(org.openide.util.Lookup)

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

CLSS public abstract interface org.openide.util.Cancellable
meth public abstract boolean cancel()

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

CLSS public abstract interface org.openide.windows.InputOutput
fld public final static java.io.Reader nullReader
 anno 0 java.lang.Deprecated()
fld public final static org.openide.windows.InputOutput NULL
fld public final static org.openide.windows.OutputWriter nullWriter
 anno 0 java.lang.Deprecated()
meth public abstract boolean isClosed()
meth public abstract boolean isErrSeparated()
meth public abstract boolean isFocusTaken()
meth public abstract java.io.Reader flushReader()
 anno 0 java.lang.Deprecated()
meth public abstract java.io.Reader getIn()
meth public abstract org.openide.windows.OutputWriter getErr()
meth public abstract org.openide.windows.OutputWriter getOut()
meth public abstract void closeInputOutput()
meth public abstract void select()
meth public abstract void setErrSeparated(boolean)
meth public abstract void setErrVisible(boolean)
meth public abstract void setFocusTaken(boolean)
meth public abstract void setInputVisible(boolean)
meth public abstract void setOutputVisible(boolean)

