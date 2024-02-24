#Signature file v4.1
#Version 1.47

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

CLSS public abstract interface java.awt.event.AWTEventListener
intf java.util.EventListener
meth public abstract void eventDispatched(java.awt.AWTEvent)

CLSS public abstract interface java.awt.event.ActionListener
intf java.util.EventListener
meth public abstract void actionPerformed(java.awt.event.ActionEvent)

CLSS public abstract interface java.awt.event.HierarchyListener
intf java.util.EventListener
meth public abstract void hierarchyChanged(java.awt.event.HierarchyEvent)

CLSS public abstract interface java.awt.event.KeyListener
intf java.util.EventListener
meth public abstract void keyPressed(java.awt.event.KeyEvent)
meth public abstract void keyReleased(java.awt.event.KeyEvent)
meth public abstract void keyTyped(java.awt.event.KeyEvent)

CLSS public abstract interface java.awt.event.MouseListener
intf java.util.EventListener
meth public abstract void mouseClicked(java.awt.event.MouseEvent)
meth public abstract void mouseEntered(java.awt.event.MouseEvent)
meth public abstract void mouseExited(java.awt.event.MouseEvent)
meth public abstract void mousePressed(java.awt.event.MouseEvent)
meth public abstract void mouseReleased(java.awt.event.MouseEvent)

CLSS public abstract interface java.awt.event.MouseMotionListener
intf java.util.EventListener
meth public abstract void mouseDragged(java.awt.event.MouseEvent)
meth public abstract void mouseMoved(java.awt.event.MouseEvent)

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

CLSS public javax.swing.JEditorPane
cons public init()
cons public init(java.lang.String) throws java.io.IOException
cons public init(java.lang.String,java.lang.String)
cons public init(java.net.URL) throws java.io.IOException
fld public final static java.lang.String HONOR_DISPLAY_PROPERTIES = "JEditorPane.honorDisplayProperties"
fld public final static java.lang.String W3C_LENGTH_UNITS = "JEditorPane.w3cLengthUnits"
innr protected AccessibleJEditorPane
innr protected AccessibleJEditorPaneHTML
innr protected JEditorPaneAccessibleHypertextSupport
meth protected java.io.InputStream getStream(java.net.URL) throws java.io.IOException
meth protected java.lang.String paramString()
meth protected javax.swing.text.EditorKit createDefaultEditorKit()
meth public boolean getScrollableTracksViewportHeight()
meth public boolean getScrollableTracksViewportWidth()
meth public final java.lang.String getContentType()
meth public final void setContentType(java.lang.String)
meth public java.awt.Dimension getPreferredSize()
meth public java.lang.String getText()
meth public java.lang.String getUIClassID()
meth public java.net.URL getPage()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.event.HyperlinkListener[] getHyperlinkListeners()
meth public javax.swing.text.EditorKit getEditorKit()
meth public javax.swing.text.EditorKit getEditorKitForContentType(java.lang.String)
meth public static java.lang.String getEditorKitClassNameForContentType(java.lang.String)
meth public static javax.swing.text.EditorKit createEditorKitForContentType(java.lang.String)
meth public static void registerEditorKitForContentType(java.lang.String,java.lang.String)
meth public static void registerEditorKitForContentType(java.lang.String,java.lang.String,java.lang.ClassLoader)
meth public void addHyperlinkListener(javax.swing.event.HyperlinkListener)
meth public void fireHyperlinkUpdate(javax.swing.event.HyperlinkEvent)
meth public void read(java.io.InputStream,java.lang.Object) throws java.io.IOException
meth public void removeHyperlinkListener(javax.swing.event.HyperlinkListener)
meth public void replaceSelection(java.lang.String)
meth public void scrollToReference(java.lang.String)
meth public void setEditorKit(javax.swing.text.EditorKit)
meth public void setEditorKitForContentType(java.lang.String,javax.swing.text.EditorKit)
meth public void setPage(java.lang.String) throws java.io.IOException
meth public void setPage(java.net.URL) throws java.io.IOException
meth public void setText(java.lang.String)
supr javax.swing.text.JTextComponent

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

CLSS public abstract interface javax.swing.Scrollable
meth public abstract boolean getScrollableTracksViewportHeight()
meth public abstract boolean getScrollableTracksViewportWidth()
meth public abstract int getScrollableBlockIncrement(java.awt.Rectangle,int,int)
meth public abstract int getScrollableUnitIncrement(java.awt.Rectangle,int,int)
meth public abstract java.awt.Dimension getPreferredScrollableViewportSize()

CLSS public abstract interface javax.swing.event.ChangeListener
intf java.util.EventListener
meth public abstract void stateChanged(javax.swing.event.ChangeEvent)

CLSS public abstract interface javax.swing.event.DocumentListener
intf java.util.EventListener
meth public abstract void changedUpdate(javax.swing.event.DocumentEvent)
meth public abstract void insertUpdate(javax.swing.event.DocumentEvent)
meth public abstract void removeUpdate(javax.swing.event.DocumentEvent)

CLSS public abstract interface javax.swing.event.HyperlinkListener
intf java.util.EventListener
meth public abstract void hyperlinkUpdate(javax.swing.event.HyperlinkEvent)

CLSS public abstract interface javax.swing.event.ListSelectionListener
intf java.util.EventListener
meth public abstract void valueChanged(javax.swing.event.ListSelectionEvent)

CLSS public abstract interface javax.swing.table.TableCellRenderer
meth public abstract java.awt.Component getTableCellRendererComponent(javax.swing.JTable,java.lang.Object,boolean,boolean,int,int)

CLSS public abstract javax.swing.text.JTextComponent
cons public init()
fld public final static java.lang.String DEFAULT_KEYMAP = "default"
fld public final static java.lang.String FOCUS_ACCELERATOR_KEY = "focusAcceleratorKey"
innr public AccessibleJTextComponent
innr public final static DropLocation
innr public static KeyBinding
intf javax.accessibility.Accessible
intf javax.swing.Scrollable
meth protected boolean saveComposedText(int)
meth protected java.lang.String paramString()
meth protected void fireCaretUpdate(javax.swing.event.CaretEvent)
meth protected void processInputMethodEvent(java.awt.event.InputMethodEvent)
meth protected void restoreComposedText()
meth public boolean getDragEnabled()
meth public boolean getScrollableTracksViewportHeight()
meth public boolean getScrollableTracksViewportWidth()
meth public boolean isEditable()
meth public boolean print() throws java.awt.print.PrinterException
meth public boolean print(java.text.MessageFormat,java.text.MessageFormat) throws java.awt.print.PrinterException
meth public boolean print(java.text.MessageFormat,java.text.MessageFormat,boolean,javax.print.PrintService,javax.print.attribute.PrintRequestAttributeSet,boolean) throws java.awt.print.PrinterException
meth public char getFocusAccelerator()
meth public final javax.swing.DropMode getDropMode()
meth public final javax.swing.text.JTextComponent$DropLocation getDropLocation()
meth public final void setDropMode(javax.swing.DropMode)
meth public int getCaretPosition()
meth public int getScrollableBlockIncrement(java.awt.Rectangle,int,int)
meth public int getScrollableUnitIncrement(java.awt.Rectangle,int,int)
meth public int getSelectionEnd()
meth public int getSelectionStart()
meth public int viewToModel(java.awt.Point)
meth public java.awt.Color getCaretColor()
meth public java.awt.Color getDisabledTextColor()
meth public java.awt.Color getSelectedTextColor()
meth public java.awt.Color getSelectionColor()
meth public java.awt.Dimension getPreferredScrollableViewportSize()
meth public java.awt.Insets getMargin()
meth public java.awt.Rectangle modelToView(int) throws javax.swing.text.BadLocationException
meth public java.awt.im.InputMethodRequests getInputMethodRequests()
meth public java.awt.print.Printable getPrintable(java.text.MessageFormat,java.text.MessageFormat)
meth public java.lang.String getSelectedText()
meth public java.lang.String getText()
meth public java.lang.String getText(int,int) throws javax.swing.text.BadLocationException
meth public java.lang.String getToolTipText(java.awt.event.MouseEvent)
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.Action[] getActions()
meth public javax.swing.event.CaretListener[] getCaretListeners()
meth public javax.swing.plaf.TextUI getUI()
meth public javax.swing.text.Caret getCaret()
meth public javax.swing.text.Document getDocument()
meth public javax.swing.text.Highlighter getHighlighter()
meth public javax.swing.text.Keymap getKeymap()
meth public javax.swing.text.NavigationFilter getNavigationFilter()
meth public static javax.swing.text.Keymap addKeymap(java.lang.String,javax.swing.text.Keymap)
meth public static javax.swing.text.Keymap getKeymap(java.lang.String)
meth public static javax.swing.text.Keymap removeKeymap(java.lang.String)
meth public static void loadKeymap(javax.swing.text.Keymap,javax.swing.text.JTextComponent$KeyBinding[],javax.swing.Action[])
meth public void addCaretListener(javax.swing.event.CaretListener)
meth public void addInputMethodListener(java.awt.event.InputMethodListener)
meth public void copy()
meth public void cut()
meth public void moveCaretPosition(int)
meth public void paste()
meth public void read(java.io.Reader,java.lang.Object) throws java.io.IOException
meth public void removeCaretListener(javax.swing.event.CaretListener)
meth public void removeNotify()
meth public void replaceSelection(java.lang.String)
meth public void select(int,int)
meth public void selectAll()
meth public void setCaret(javax.swing.text.Caret)
meth public void setCaretColor(java.awt.Color)
meth public void setCaretPosition(int)
meth public void setComponentOrientation(java.awt.ComponentOrientation)
meth public void setDisabledTextColor(java.awt.Color)
meth public void setDocument(javax.swing.text.Document)
meth public void setDragEnabled(boolean)
meth public void setEditable(boolean)
meth public void setFocusAccelerator(char)
meth public void setHighlighter(javax.swing.text.Highlighter)
meth public void setKeymap(javax.swing.text.Keymap)
meth public void setMargin(java.awt.Insets)
meth public void setNavigationFilter(javax.swing.text.NavigationFilter)
meth public void setSelectedTextColor(java.awt.Color)
meth public void setSelectionColor(java.awt.Color)
meth public void setSelectionEnd(int)
meth public void setSelectionStart(int)
meth public void setText(java.lang.String)
meth public void setUI(javax.swing.plaf.TextUI)
meth public void updateUI()
meth public void write(java.io.Writer) throws java.io.IOException
supr javax.swing.JComponent

CLSS public abstract org.netbeans.lib.profiler.client.ProfilingPointsProcessor
cons public init()
meth public abstract org.netbeans.lib.profiler.client.RuntimeProfilingPoint[] getSupportedProfilingPoints()
meth public abstract void init(java.lang.Object)
meth public abstract void profilingPointHit(org.netbeans.lib.profiler.client.RuntimeProfilingPoint$HitEvent)
meth public abstract void timeAdjust(int,long,long)
supr java.lang.Object

CLSS public abstract interface org.netbeans.lib.profiler.results.DataManagerListener
meth public abstract void dataChanged()
meth public abstract void dataReset()

CLSS public org.netbeans.lib.profiler.ui.components.HTMLLabel
cons public init()
cons public init(java.lang.String)
intf javax.swing.event.HyperlinkListener
meth protected void showURL(java.net.URL)
meth public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent)
meth public void setBackground(java.awt.Color)
meth public void setForeground(java.awt.Color)
meth public void setHorizontalAlignment(int)
meth public void setOpaque(boolean)
meth public void setText(java.lang.String)
supr javax.swing.JEditorPane
hfds halign,txt

CLSS public org.netbeans.modules.profiler.ProfilerTopComponent
cons public init()
fld public final static java.lang.String RECENT_FILE_KEY = "nb.recent.file.path"
meth protected java.awt.Component defaultFocusOwner()
meth protected void componentActivated()
meth protected void componentDeactivated()
meth public java.awt.Dimension getMinimumSize()
meth public void paintComponent(java.awt.Graphics)
supr org.openide.windows.TopComponent
hfds focusListener,lastFocusOwner

CLSS public abstract org.netbeans.modules.profiler.actions.ProfilingAwareAction
cons protected init()
meth protected abstract int[] enabledStates()
meth protected boolean requiresInstrumentation()
meth protected boolean shouldBeEnabled(org.netbeans.lib.profiler.common.Profiler)
meth protected final boolean asynchronous()
meth protected void updateAction()
meth public final boolean isEnabled()
meth public final void setEnabled(boolean)
supr org.openide.util.actions.CallableSystemAction
hfds enabledSet

CLSS public final org.netbeans.modules.profiler.api.icons.Icons
cons public init()
innr public abstract interface static Keys
meth public static java.awt.Image getImage(java.lang.String)
meth public static java.lang.String getResource(java.lang.String)
meth public static javax.swing.Icon getIcon(java.lang.String)
meth public static javax.swing.ImageIcon getImageIcon(java.lang.String)
supr java.lang.Object

CLSS public abstract interface static org.netbeans.modules.profiler.api.icons.Icons$Keys
 outer org.netbeans.modules.profiler.api.icons.Icons

CLSS public abstract org.netbeans.modules.profiler.ppoints.CodeProfilingPoint
innr public abstract static Annotation
innr public abstract static Paired
innr public abstract static Single
innr public final static Location
meth public abstract org.netbeans.lib.profiler.client.RuntimeProfilingPoint[] createRuntimeProfilingPoints()
meth public abstract org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Annotation[] getAnnotations()
meth public org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location getLocation()
supr org.netbeans.modules.profiler.ppoints.ProfilingPoint
hfds PROPERTY_ANNOTATION,PROPERTY_LOCATION

CLSS public abstract static org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Annotation
 outer org.netbeans.modules.profiler.ppoints.CodeProfilingPoint
cons public init()
meth public abstract org.netbeans.modules.profiler.ppoints.CodeProfilingPoint profilingPoint()
supr org.openide.text.Annotation

CLSS public final static org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location
 outer org.netbeans.modules.profiler.ppoints.CodeProfilingPoint
cons public init(java.lang.String,int,int)
fld public final static int OFFSET_END = 2147483647
fld public final static int OFFSET_START = -2147483648
fld public final static java.lang.String PROPERTY_LOCATION_FILE = "p_location_file"
fld public final static java.lang.String PROPERTY_LOCATION_LINE = "p_location_line"
fld public final static java.lang.String PROPERTY_LOCATION_OFFSET = "p_location_offset"
fld public final static org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location EMPTY
meth public boolean equals(java.lang.Object)
meth public boolean isLineEnd()
meth public boolean isLineStart()
meth public int getLine()
meth public int getOffset()
meth public int hashCode()
meth public java.lang.String getFile()
meth public java.lang.String toString()
meth public static org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location load(org.openide.util.Lookup$Provider,int,java.lang.String,java.util.Properties)
meth public static org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location load(org.openide.util.Lookup$Provider,int,java.util.Properties)
meth public void store(org.openide.util.Lookup$Provider,int,java.lang.String,java.util.Properties)
meth public void store(org.openide.util.Lookup$Provider,int,java.util.Properties)
supr java.lang.Object
hfds file,line,offset

CLSS public abstract static org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Paired
 outer org.netbeans.modules.profiler.ppoints.CodeProfilingPoint
meth protected abstract boolean usesEndLocation()
meth protected abstract org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Annotation getEndAnnotation()
meth protected abstract org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Annotation getStartAnnotation()
meth protected void timeAdjust(int,long,long)
meth public boolean isValid()
meth public org.netbeans.lib.profiler.client.RuntimeProfilingPoint[] createRuntimeProfilingPoints()
meth public org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Annotation[] getAnnotations()
meth public org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location getEndLocation()
meth public org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location getStartLocation()
meth public void setEnabled(boolean)
meth public void setEndLocation(org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location)
meth public void setLocation(org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Annotation,org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location)
meth public void setStartLocation(org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location)
supr org.netbeans.modules.profiler.ppoints.CodeProfilingPoint
hfds endLocation,startLocation

CLSS public abstract static org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Single
 outer org.netbeans.modules.profiler.ppoints.CodeProfilingPoint
meth protected abstract org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Annotation getAnnotation()
meth public boolean isValid()
meth public org.netbeans.lib.profiler.client.RuntimeProfilingPoint[] createRuntimeProfilingPoints()
meth public org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Annotation[] getAnnotations()
meth public org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location getLocation()
meth public void setEnabled(boolean)
meth public void setLocation(org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Annotation,org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location)
meth public void setLocation(org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location)
supr org.netbeans.modules.profiler.ppoints.CodeProfilingPoint
hfds location

CLSS public abstract org.netbeans.modules.profiler.ppoints.CodeProfilingPointFactory
cons public init()
meth protected abstract java.lang.String getServerHandlerClassName()
supr org.netbeans.modules.profiler.ppoints.ProfilingPointFactory

CLSS public abstract org.netbeans.modules.profiler.ppoints.GlobalProfilingPoint
meth public boolean supportsProfilingSettings(org.netbeans.lib.profiler.common.ProfilingSettings)
supr org.netbeans.modules.profiler.ppoints.ProfilingPoint

CLSS public org.netbeans.modules.profiler.ppoints.GlobalProfilingPointsProcessor
cons public init()
intf org.netbeans.lib.profiler.results.DataManagerListener
meth public void dataChanged()
meth public void dataReset()
supr java.lang.Object
hfds currentCpuTime,currentGcTime,currentHeapSize,currentHeapUsage,currentLoadedClasses,currentSurvGen,currentThreads,currentTime,defaultInstance,gpp,isRunning,profiledProject,profilingSettings,scheduledTimedPPs,scheduledTriggeredPPs

CLSS public org.netbeans.modules.profiler.ppoints.Installer
cons public init()
intf java.lang.Runnable
meth public void run()
supr java.lang.Object

CLSS public org.netbeans.modules.profiler.ppoints.LoadGenProfilingPoint
cons public init(java.lang.String,org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location,org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location,org.openide.util.Lookup$Provider,org.netbeans.modules.profiler.ppoints.ProfilingPointFactory)
fld public final static java.lang.String PROPERTY_SCRIPTNAME = "p_ScriptName"
intf java.beans.PropertyChangeListener
meth protected boolean usesEndLocation()
meth protected java.lang.String getResultsText()
meth protected org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Annotation getEndAnnotation()
meth protected org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Annotation getStartAnnotation()
meth protected void updateCustomizer(org.netbeans.modules.profiler.ppoints.ui.ValidityAwarePanel)
meth public boolean hasResults()
meth public boolean isEnabled()
meth public java.lang.String getScriptFileName()
meth public void hideResults()
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void setEnabled(boolean)
meth public void setSriptFileName(java.lang.String)
meth public void showResults(java.net.URL)
supr org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Paired
hfds ANNOTATION_DISABLED,ANNOTATION_ENABLED,LOGGER,endAnnotation,reportReference,results,resultsSync,scriptFileName,startAnnotation
hcls Annotation,Report,Result

CLSS public org.netbeans.modules.profiler.ppoints.LoadGenProfilingPointFactory
cons public init()
meth protected java.lang.Class getProfilingPointsClass()
meth protected java.lang.String getServerHandlerClassName()
meth protected org.netbeans.modules.profiler.ppoints.ProfilingPoint loadProfilingPoint(org.openide.util.Lookup$Provider,java.util.Properties,int)
meth protected org.netbeans.modules.profiler.ppoints.ui.ValidityAwarePanel createCustomizer()
meth protected void storeProfilingPoint(org.netbeans.modules.profiler.ppoints.ProfilingPoint,int,java.util.Properties)
meth public boolean isAvailable()
meth public boolean supportsCPU()
meth public boolean supportsMemory()
meth public boolean supportsMonitor()
meth public int getScope()
meth public java.lang.String getDescription()
meth public java.lang.String getType()
meth public javax.swing.Icon getDisabledIcon()
meth public javax.swing.Icon getIcon()
meth public org.netbeans.modules.profiler.ppoints.ProfilingPoint create(org.openide.util.Lookup$Provider)
supr org.netbeans.modules.profiler.ppoints.CodeProfilingPointFactory
hfds END_LOCATION_PREFIX,START_LOCATION_PREFIX,available,defaultInstance,loadGenResult,lookupListener

CLSS public abstract org.netbeans.modules.profiler.ppoints.ProfilingPoint
innr public static ResultsRenderer
meth protected abstract java.lang.String getResultsText()
meth protected abstract void updateCustomizer(org.netbeans.modules.profiler.ppoints.ui.ValidityAwarePanel)
meth protected java.beans.PropertyChangeSupport getChangeSupport()
meth public abstract boolean hasResults()
meth public abstract void hideResults()
meth public abstract void showResults(java.net.URL)
meth public boolean isEnabled()
meth public boolean isValid()
meth public boolean supportsProfilingSettings(org.netbeans.lib.profiler.common.ProfilingSettings)
meth public final org.netbeans.modules.profiler.ppoints.ProfilingPointFactory getFactory()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public org.netbeans.modules.profiler.ppoints.ProfilingPoint$ResultsRenderer getResultsRenderer()
meth public org.openide.util.Lookup$Provider getProject()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void customize(boolean,boolean)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void setEnabled(boolean)
meth public void setName(java.lang.String)
supr java.lang.Object
hfds PROPERTY_CUSTOM,PROPERTY_ENABLED,PROPERTY_NAME,PROPERTY_PROJECT,PROPERTY_RESULTS,enabled,factory,name,project,propertyChangeSupport,resultsRenderer

CLSS public static org.netbeans.modules.profiler.ppoints.ProfilingPoint$ResultsRenderer
 outer org.netbeans.modules.profiler.ppoints.ProfilingPoint
cons public init()
intf javax.swing.table.TableCellRenderer
meth protected void showURL(java.net.URL)
meth public java.awt.Component getTableCellRendererComponent(javax.swing.JTable,java.lang.Object,boolean,boolean,int,int)
meth public void dispatchMouseEvent(java.awt.event.MouseEvent,java.awt.Rectangle)
meth public void moveCaretPosition(int)
meth public void setCaretPosition(int)
meth public void setCursor(java.awt.Cursor)
supr org.netbeans.lib.profiler.ui.components.HTMLLabel
hfds lastProfilingPoint,lastTable

CLSS public abstract org.netbeans.modules.profiler.ppoints.ProfilingPointAnnotator
cons public init()
innr public static Basic
meth public abstract void annotate(org.netbeans.modules.profiler.ppoints.CodeProfilingPoint)
meth public abstract void annotationChanged(java.beans.PropertyChangeEvent)
meth public abstract void appearanceChanged(java.beans.PropertyChangeEvent)
meth public abstract void deannotate(org.netbeans.modules.profiler.ppoints.CodeProfilingPoint)
meth public abstract void locationChanged(java.beans.PropertyChangeEvent)
supr java.lang.Object
hfds INSTANCE

CLSS public static org.netbeans.modules.profiler.ppoints.ProfilingPointAnnotator$Basic
 outer org.netbeans.modules.profiler.ppoints.ProfilingPointAnnotator
cons public init()
meth public void annotate(org.netbeans.modules.profiler.ppoints.CodeProfilingPoint)
meth public void annotationChanged(java.beans.PropertyChangeEvent)
meth public void appearanceChanged(java.beans.PropertyChangeEvent)
meth public void deannotate(org.netbeans.modules.profiler.ppoints.CodeProfilingPoint)
meth public void locationChanged(java.beans.PropertyChangeEvent)
supr org.netbeans.modules.profiler.ppoints.ProfilingPointAnnotator

CLSS public abstract org.netbeans.modules.profiler.ppoints.ProfilingPointFactory
cons public init()
fld public final static int SCOPE_CODE = 1
fld public final static int SCOPE_GLOBAL = 2
fld public final static java.lang.String AVAILABILITY_PROPERTY
fld public final static javax.swing.Icon SCOPE_CODE_ICON
fld public final static javax.swing.Icon SCOPE_GLOBAL_ICON
meth protected abstract java.lang.Class getProfilingPointsClass()
meth protected abstract org.netbeans.modules.profiler.ppoints.ProfilingPoint loadProfilingPoint(org.openide.util.Lookup$Provider,java.util.Properties,int)
meth protected abstract org.netbeans.modules.profiler.ppoints.ui.ValidityAwarePanel createCustomizer()
meth protected abstract void storeProfilingPoint(org.netbeans.modules.profiler.ppoints.ProfilingPoint,int,java.util.Properties)
meth protected void firePropertyChange(java.beans.PropertyChangeEvent)
meth public abstract boolean supportsCPU()
meth public abstract boolean supportsMemory()
meth public abstract boolean supportsMonitor()
meth public abstract int getScope()
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getType()
meth public abstract javax.swing.Icon getDisabledIcon()
meth public abstract javax.swing.Icon getIcon()
meth public abstract org.netbeans.modules.profiler.ppoints.ProfilingPoint create(org.openide.util.Lookup$Provider)
meth public boolean isAvailable()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public java.lang.String getHint()
meth public javax.swing.Icon getScopeIcon()
meth public org.netbeans.modules.profiler.ppoints.ProfilingPoint create()
supr java.lang.Object
hfds PROFILING_POINT_STORAGE_EXT,customizer,pcs

CLSS public abstract org.netbeans.modules.profiler.ppoints.ProfilingPointScopeProvider
cons public init()
meth public abstract boolean isDefaultScope()
meth public abstract boolean matchesScope(org.openide.util.Lookup$Provider,java.util.Set<org.openide.util.Lookup$Provider>)
meth public abstract org.openide.util.Lookup$Provider getScope()
supr java.lang.Object

CLSS public org.netbeans.modules.profiler.ppoints.ProfilingPointWizard
intf org.openide.WizardDescriptor$Iterator
meth public boolean hasNext()
meth public boolean hasPrevious()
meth public java.lang.String name()
meth public org.netbeans.modules.profiler.ppoints.ProfilingPoint finish(boolean)
meth public org.openide.WizardDescriptor getWizardDescriptor()
meth public org.openide.WizardDescriptor getWizardDescriptor(org.openide.util.Lookup$Provider)
meth public org.openide.WizardDescriptor$Panel current()
meth public static org.netbeans.modules.profiler.ppoints.ProfilingPointWizard getDefault()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void nextPanel()
meth public void previousPanel()
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
hfds DEFAULT_PREFERRED_PANEL_SIZE,currentPanel,defaultInstance,ppFactories,preferredPanelSize,profilingPoint,selectedPPFactoryIndex,selectedProject,settingsChanged,wizardDescriptor,wizardPanels,wizardSteps
hcls Singleton,WizardPanel,WizardPanel1,WizardPanel2

CLSS public org.netbeans.modules.profiler.ppoints.ProfilingPointsAnnotationProvider
cons public init()
intf org.openide.text.AnnotationProvider
meth public void annotate(org.openide.text.Line$Set,org.openide.util.Lookup)
supr java.lang.Object

CLSS public final org.netbeans.modules.profiler.ppoints.ProfilingPointsManager
cons public init()
fld public final static int SORT_BY_NAME = 3
fld public final static int SORT_BY_PROJECT = 1
fld public final static int SORT_BY_RESULTS = 4
fld public final static int SORT_BY_SCOPE = 2
fld public final static java.lang.String PROPERTY_PROFILING_POINTS_CHANGED = "p_profiling_points_changed"
fld public final static java.lang.String PROPERTY_PROJECTS_CHANGED = "p_projects_changed"
intf java.beans.PropertyChangeListener
intf javax.swing.event.ChangeListener
meth public <%0 extends org.netbeans.modules.profiler.ppoints.ProfilingPoint> java.util.List<{%%0}> getProfilingPoints(java.lang.Class<{%%0}>,org.openide.util.Lookup$Provider,boolean)
meth public <%0 extends org.netbeans.modules.profiler.ppoints.ProfilingPoint> java.util.List<{%%0}> getProfilingPoints(java.lang.Class<{%%0}>,org.openide.util.Lookup$Provider,boolean,boolean)
meth public boolean belowMaxHits(int)
meth public boolean isDefaultScope(org.openide.util.Lookup$Provider)
meth public boolean isProfilingInProgress()
meth public boolean isProfilingSessionInProgress()
meth public int createUniqueRuntimeProfilingPointIdentificator()
meth public java.lang.String getTruncatedResultsText()
meth public java.util.List<org.netbeans.modules.profiler.ppoints.ProfilingPoint> getCompatibleProfilingPoints(org.openide.util.Lookup$Provider,org.netbeans.lib.profiler.common.ProfilingSettings,boolean)
meth public java.util.List<org.netbeans.modules.profiler.ppoints.ProfilingPoint> getProfilingPoints(org.openide.util.Lookup$Provider,boolean,boolean)
meth public java.util.List<org.netbeans.modules.profiler.ppoints.ProfilingPoint> getSortedProfilingPoints(org.openide.util.Lookup$Provider,int,boolean)
meth public java.util.List<org.openide.util.Lookup$Provider> getProvidedScopes()
meth public org.netbeans.lib.profiler.client.RuntimeProfilingPoint[] createCodeProfilingConfiguration(org.openide.util.Lookup$Provider,org.netbeans.lib.profiler.common.ProfilingSettings)
meth public org.netbeans.lib.profiler.client.RuntimeProfilingPoint[] getSupportedProfilingPoints()
meth public org.netbeans.modules.profiler.ppoints.GlobalProfilingPoint[] createGlobalProfilingConfiguration(org.openide.util.Lookup$Provider,org.netbeans.lib.profiler.common.ProfilingSettings)
meth public org.netbeans.modules.profiler.ppoints.ProfilingPointFactory[] getProfilingPointFactories()
meth public static org.netbeans.modules.profiler.ppoints.ProfilingPointsManager getDefault()
meth public void addProfilingPoint(org.netbeans.modules.profiler.ppoints.ProfilingPoint)
meth public void addProfilingPoints(org.netbeans.modules.profiler.ppoints.ProfilingPoint[])
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void firePropertyChanged(java.lang.String)
meth public void ideClosing()
meth public void init(java.lang.Object)
meth public void profilingPointHit(org.netbeans.lib.profiler.client.RuntimeProfilingPoint$HitEvent)
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void removeProfilingPoint(org.netbeans.modules.profiler.ppoints.ProfilingPoint)
meth public void removeProfilingPoints(org.netbeans.modules.profiler.ppoints.ProfilingPoint[])
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void reset()
meth public void stateChanged(javax.swing.event.ChangeEvent)
meth public void timeAdjust(int,long,long)
meth public void updateLocation(org.netbeans.modules.profiler.ppoints.CodeProfilingPoint,int,int)
supr org.netbeans.lib.profiler.client.ProfilingPointsProcessor
hfds MAX_HITS,_processorLock,_processorRef,activeCodeProfilingPoints,activeGlobalProfilingPoints,customizerButton,customizers,dirtyProfilingPoints,hasInstance,ignoreStoreProfilingPoints,nextUniqueRPPIdentificator,openedProjects,pcl,points,pointsLock,processesProfilingPoints,profilingInProgress,profilingPointFactories,profilingPoints,profilingPointsFiles,profilingSessionInProgress,propertyChangeSupport,providedScopes,scopeProviders
hcls CustomizerButton,CustomizerListener,FileWatch,LocationFileListener,ProfilingPointsComparator,RuntimeProfilingPointMapper

CLSS public final org.netbeans.modules.profiler.ppoints.ResetResultsProfilingPoint
cons public init(java.lang.String,org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location,org.openide.util.Lookup$Provider,org.netbeans.modules.profiler.ppoints.ProfilingPointFactory)
intf java.beans.PropertyChangeListener
meth protected java.lang.String getResultsText()
meth protected org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Annotation getAnnotation()
meth protected void updateCustomizer(org.netbeans.modules.profiler.ppoints.ui.ValidityAwarePanel)
meth public boolean hasResults()
meth public void hideResults()
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void showResults(java.net.URL)
supr org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Single
hfds ANNOTATION_DISABLED,ANNOTATION_ENABLED,annotation,reportReference,results,resultsSync
hcls Annotation,Report,Result

CLSS public org.netbeans.modules.profiler.ppoints.ResetResultsProfilingPointFactory
cons public init()
meth protected java.lang.Class getProfilingPointsClass()
meth protected java.lang.String getServerHandlerClassName()
meth protected org.netbeans.modules.profiler.ppoints.ProfilingPoint loadProfilingPoint(org.openide.util.Lookup$Provider,java.util.Properties,int)
meth protected org.netbeans.modules.profiler.ppoints.ui.ResetResultsCustomizer createCustomizer()
meth protected void storeProfilingPoint(org.netbeans.modules.profiler.ppoints.ProfilingPoint,int,java.util.Properties)
meth public boolean supportsCPU()
meth public boolean supportsMemory()
meth public boolean supportsMonitor()
meth public int getScope()
meth public java.lang.String getDescription()
meth public java.lang.String getHint()
meth public java.lang.String getType()
meth public javax.swing.Icon getDisabledIcon()
meth public javax.swing.Icon getIcon()
meth public org.netbeans.modules.profiler.ppoints.ResetResultsProfilingPoint create(org.openide.util.Lookup$Provider)
supr org.netbeans.modules.profiler.ppoints.CodeProfilingPointFactory

CLSS public final org.netbeans.modules.profiler.ppoints.StopwatchProfilingPoint
cons public init(java.lang.String,org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location,org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location,org.openide.util.Lookup$Provider,org.netbeans.modules.profiler.ppoints.ProfilingPointFactory)
intf java.beans.PropertyChangeListener
meth protected boolean usesEndLocation()
meth protected java.lang.String getResultsText()
meth protected org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Annotation getEndAnnotation()
meth protected org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Annotation getStartAnnotation()
meth protected void timeAdjust(int,long,long)
meth protected void updateCustomizer(org.netbeans.modules.profiler.ppoints.ui.ValidityAwarePanel)
meth public boolean hasResults()
meth public void hideResults()
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void showResults(java.net.URL)
supr org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Paired
hfds ANNOTATION_DISABLED,ANNOTATION_ENABLED,endAnnotation,reportReference,results,resultsSync,startAnnotation
hcls Annotation,Report,Result

CLSS public org.netbeans.modules.profiler.ppoints.StopwatchProfilingPointFactory
cons public init()
meth protected java.lang.Class getProfilingPointsClass()
meth protected java.lang.String getServerHandlerClassName()
meth protected org.netbeans.modules.profiler.ppoints.ProfilingPoint loadProfilingPoint(org.openide.util.Lookup$Provider,java.util.Properties,int)
meth protected org.netbeans.modules.profiler.ppoints.ui.StopwatchCustomizer createCustomizer()
meth protected void storeProfilingPoint(org.netbeans.modules.profiler.ppoints.ProfilingPoint,int,java.util.Properties)
meth public boolean supportsCPU()
meth public boolean supportsMemory()
meth public boolean supportsMonitor()
meth public int getScope()
meth public java.lang.String getDescription()
meth public java.lang.String getHint()
meth public java.lang.String getType()
meth public javax.swing.Icon getDisabledIcon()
meth public javax.swing.Icon getIcon()
meth public org.netbeans.modules.profiler.ppoints.ProfilingPoint create(org.openide.util.Lookup$Provider)
supr org.netbeans.modules.profiler.ppoints.CodeProfilingPointFactory
hfds END_LOCATION_PREFIX,START_LOCATION_PREFIX

CLSS public final org.netbeans.modules.profiler.ppoints.TakeSnapshotProfilingPoint
cons public init(java.lang.String,org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location,org.openide.util.Lookup$Provider,org.netbeans.modules.profiler.ppoints.ProfilingPointFactory)
fld public final static java.lang.String TARGET_CUSTOM_KEY = "custom"
fld public final static java.lang.String TARGET_PROJECT_KEY = "project"
fld public final static java.lang.String TYPE_HEAPDUMP_KEY = "heapdump"
fld public final static java.lang.String TYPE_PROFDATA_KEY = "profdata"
intf java.beans.PropertyChangeListener
meth protected java.lang.String getResultsText()
meth protected org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Annotation getAnnotation()
meth protected void updateCustomizer(org.netbeans.modules.profiler.ppoints.ui.ValidityAwarePanel)
meth public boolean getResetResults()
meth public boolean hasResults()
meth public java.lang.String getSnapshotFile()
meth public java.lang.String getSnapshotTarget()
meth public java.lang.String getSnapshotType()
meth public void hideResults()
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void setResetResults(boolean)
meth public void setSnapshotFile(java.lang.String)
meth public void setSnapshotTarget(java.lang.String)
meth public void setSnapshotType(java.lang.String)
meth public void showResults(java.net.URL)
supr org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Single
hfds ANNOTATION_DISABLED,ANNOTATION_ENABLED,PROPERTY_CUSTOM_FILE,PROPERTY_RESET_RESULTS,PROPERTY_TARGET,PROPERTY_TYPE,SNAPSHOT_LOCATION_URLMASK,annotation,reportReference,resetResults,results,resultsSync,snapshotFile,snapshotTarget,snapshotType
hcls Annotation,Report,Result

CLSS public org.netbeans.modules.profiler.ppoints.TakeSnapshotProfilingPointFactory
cons public init()
meth protected java.lang.Class getProfilingPointsClass()
meth protected java.lang.String getServerHandlerClassName()
meth protected org.netbeans.modules.profiler.ppoints.ProfilingPoint loadProfilingPoint(org.openide.util.Lookup$Provider,java.util.Properties,int)
meth protected org.netbeans.modules.profiler.ppoints.ui.TakeSnapshotCustomizer createCustomizer()
meth protected void storeProfilingPoint(org.netbeans.modules.profiler.ppoints.ProfilingPoint,int,java.util.Properties)
meth public boolean supportsCPU()
meth public boolean supportsMemory()
meth public boolean supportsMonitor()
meth public int getScope()
meth public java.lang.String getDescription()
meth public java.lang.String getHint()
meth public java.lang.String getType()
meth public javax.swing.Icon getDisabledIcon()
meth public javax.swing.Icon getIcon()
meth public org.netbeans.modules.profiler.ppoints.TakeSnapshotProfilingPoint create(org.openide.util.Lookup$Provider)
supr org.netbeans.modules.profiler.ppoints.CodeProfilingPointFactory

CLSS public abstract org.netbeans.modules.profiler.ppoints.TimedGlobalProfilingPoint
innr public static TimeCondition
meth public org.netbeans.modules.profiler.ppoints.TimedGlobalProfilingPoint$TimeCondition getCondition()
meth public void setCondition(org.netbeans.modules.profiler.ppoints.TimedGlobalProfilingPoint$TimeCondition)
supr org.netbeans.modules.profiler.ppoints.GlobalProfilingPoint
hfds PROPERTY_TIME,condition

CLSS public static org.netbeans.modules.profiler.ppoints.TimedGlobalProfilingPoint$TimeCondition
 outer org.netbeans.modules.profiler.ppoints.TimedGlobalProfilingPoint
cons public init()
cons public init(long)
cons public init(long,boolean,int,int)
fld public final static int UNITS_HOURS = 2
fld public final static int UNITS_MINUTES = 1
fld public final static java.lang.String PROPERTY_TIMECOND_PERIODTIME = "p_timecond_periodtime"
fld public final static java.lang.String PROPERTY_TIMECOND_PERIODUNITS = "p_timecond_periodunits"
fld public final static java.lang.String PROPERTY_TIMECOND_REPEATS = "p_timecond_repeats"
fld public final static java.lang.String PROPERTY_TIMECOND_STARTTIME = "p_timecond_starttime"
meth public boolean equals(java.lang.Object)
meth public boolean getRepeats()
meth public int getPeriodTime()
meth public int getPeriodUnits()
meth public int hashCode()
meth public long getStartTime()
meth public static org.netbeans.modules.profiler.ppoints.TimedGlobalProfilingPoint$TimeCondition load(org.openide.util.Lookup$Provider,int,java.lang.String,java.util.Properties)
meth public static org.netbeans.modules.profiler.ppoints.TimedGlobalProfilingPoint$TimeCondition load(org.openide.util.Lookup$Provider,int,java.util.Properties)
meth public void setPeriodTime(int)
meth public void setPeriodUnits(int)
meth public void setRepeats(boolean)
meth public void setStartTime(long)
meth public void store(org.openide.util.Lookup$Provider,int,java.lang.String,java.util.Properties)
meth public void store(org.openide.util.Lookup$Provider,int,java.util.Properties)
supr java.lang.Object
hfds periodTime,periodUnits,repeats,scheduledTime,startTime

CLSS public final org.netbeans.modules.profiler.ppoints.TimedTakeSnapshotProfilingPoint
cons public init(java.lang.String,org.openide.util.Lookup$Provider,org.netbeans.modules.profiler.ppoints.ProfilingPointFactory)
fld public final static java.lang.String TAKEN_HEAPDUMP_PREFIX = "heapdump-"
fld public final static java.lang.String TARGET_CUSTOM_KEY = "custom"
fld public final static java.lang.String TARGET_PROJECT_KEY = "project"
fld public final static java.lang.String TYPE_HEAPDUMP_KEY = "heapdump"
fld public final static java.lang.String TYPE_PROFDATA_KEY = "profdata"
intf java.beans.PropertyChangeListener
meth protected java.lang.String getResultsText()
meth protected void updateCustomizer(org.netbeans.modules.profiler.ppoints.ui.ValidityAwarePanel)
meth public boolean getResetResults()
meth public boolean hasResults()
meth public java.lang.String getSnapshotFile()
meth public java.lang.String getSnapshotTarget()
meth public java.lang.String getSnapshotType()
meth public void hideResults()
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void setResetResults(boolean)
meth public void setSnapshotFile(java.lang.String)
meth public void setSnapshotTarget(java.lang.String)
meth public void setSnapshotType(java.lang.String)
meth public void showResults(java.net.URL)
supr org.netbeans.modules.profiler.ppoints.TimedGlobalProfilingPoint
hfds NO_DATA_AVAILABLE_MESSAGE,PROPERTY_CUSTOM_FILE,PROPERTY_RESET_RESULTS,PROPERTY_TARGET,PROPERTY_TYPE,SNAPSHOT_LOCATION_URLMASK,reportReference,resetResults,results,resultsSync,snapshotFile,snapshotTarget,snapshotType
hcls Report,Result

CLSS public org.netbeans.modules.profiler.ppoints.TimedTakeSnapshotProfilingPointFactory
cons public init()
meth protected java.lang.Class getProfilingPointsClass()
meth protected java.lang.String getServerHandlerClassName()
meth protected org.netbeans.modules.profiler.ppoints.ProfilingPoint loadProfilingPoint(org.openide.util.Lookup$Provider,java.util.Properties,int)
meth protected org.netbeans.modules.profiler.ppoints.ui.TimedTakeSnapshotCustomizer createCustomizer()
meth protected void storeProfilingPoint(org.netbeans.modules.profiler.ppoints.ProfilingPoint,int,java.util.Properties)
meth public boolean supportsCPU()
meth public boolean supportsMemory()
meth public boolean supportsMonitor()
meth public int getScope()
meth public java.lang.String getDescription()
meth public java.lang.String getHint()
meth public java.lang.String getType()
meth public javax.swing.Icon getDisabledIcon()
meth public javax.swing.Icon getIcon()
meth public org.netbeans.modules.profiler.ppoints.TimedTakeSnapshotProfilingPoint create(org.openide.util.Lookup$Provider)
supr org.netbeans.modules.profiler.ppoints.CodeProfilingPointFactory

CLSS public abstract org.netbeans.modules.profiler.ppoints.TriggeredGlobalProfilingPoint
innr public static TriggerCondition
meth public org.netbeans.modules.profiler.ppoints.TriggeredGlobalProfilingPoint$TriggerCondition getCondition()
meth public void setCondition(org.netbeans.modules.profiler.ppoints.TriggeredGlobalProfilingPoint$TriggerCondition)
supr org.netbeans.modules.profiler.ppoints.GlobalProfilingPoint
hfds PROPERTY_TRIGGER,condition

CLSS public static org.netbeans.modules.profiler.ppoints.TriggeredGlobalProfilingPoint$TriggerCondition
 outer org.netbeans.modules.profiler.ppoints.TriggeredGlobalProfilingPoint
cons public init()
cons public init(int,long)
cons public init(int,long,boolean)
fld public final static int METRIC_CPUUSG = 6
fld public final static int METRIC_GCUSG = 7
fld public final static int METRIC_HEAPSIZ = 2
fld public final static int METRIC_HEAPUSG = 1
fld public final static int METRIC_LDCLASS = 4
fld public final static int METRIC_SURVGEN = 3
fld public final static int METRIC_THREADS = 5
fld public final static java.lang.String PROPERTY_TRIGGCOND_METRIC = "p_triggcond_metric"
fld public final static java.lang.String PROPERTY_TRIGGCOND_ONETIME = "p_triggcond_onetime"
fld public final static java.lang.String PROPERTY_TRIGGCOND_VALUE = "p_triggcond_value"
meth public boolean equals(java.lang.Object)
meth public boolean isOnetime()
meth public int getMetric()
meth public int hashCode()
meth public long getValue()
meth public static org.netbeans.modules.profiler.ppoints.TriggeredGlobalProfilingPoint$TriggerCondition load(org.openide.util.Lookup$Provider,int,java.lang.String,java.util.Properties)
meth public static org.netbeans.modules.profiler.ppoints.TriggeredGlobalProfilingPoint$TriggerCondition load(org.openide.util.Lookup$Provider,int,java.util.Properties)
meth public void setMetric(int)
meth public void setOnetime(boolean)
meth public void setValue(long)
meth public void store(org.openide.util.Lookup$Provider,int,java.lang.String,java.util.Properties)
meth public void store(org.openide.util.Lookup$Provider,int,java.util.Properties)
supr java.lang.Object
hfds metric,onetime,triggered,value

CLSS public final org.netbeans.modules.profiler.ppoints.TriggeredTakeSnapshotProfilingPoint
cons public init(java.lang.String,org.openide.util.Lookup$Provider,org.netbeans.modules.profiler.ppoints.ProfilingPointFactory)
fld public final static java.lang.String TAKEN_HEAPDUMP_PREFIX = "heapdump-"
fld public final static java.lang.String TARGET_CUSTOM_KEY = "custom"
fld public final static java.lang.String TARGET_PROJECT_KEY = "project"
fld public final static java.lang.String TYPE_HEAPDUMP_KEY = "heapdump"
fld public final static java.lang.String TYPE_PROFDATA_KEY = "profdata"
intf java.beans.PropertyChangeListener
meth protected java.lang.String getResultsText()
meth protected void updateCustomizer(org.netbeans.modules.profiler.ppoints.ui.ValidityAwarePanel)
meth public boolean getResetResults()
meth public boolean hasResults()
meth public java.lang.String getSnapshotFile()
meth public java.lang.String getSnapshotTarget()
meth public java.lang.String getSnapshotType()
meth public void hideResults()
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void setResetResults(boolean)
meth public void setSnapshotFile(java.lang.String)
meth public void setSnapshotTarget(java.lang.String)
meth public void setSnapshotType(java.lang.String)
meth public void showResults(java.net.URL)
supr org.netbeans.modules.profiler.ppoints.TriggeredGlobalProfilingPoint
hfds PROPERTY_CUSTOM_FILE,PROPERTY_RESET_RESULTS,PROPERTY_TARGET,PROPERTY_TYPE,SNAPSHOT_LOCATION_URLMASK,reportReference,resetResults,results,resultsSync,snapshotFile,snapshotTarget,snapshotType
hcls Report,Result

CLSS public org.netbeans.modules.profiler.ppoints.TriggeredTakeSnapshotProfilingPointFactory
cons public init()
meth protected java.lang.Class getProfilingPointsClass()
meth protected java.lang.String getServerHandlerClassName()
meth protected org.netbeans.modules.profiler.ppoints.ProfilingPoint loadProfilingPoint(org.openide.util.Lookup$Provider,java.util.Properties,int)
meth protected org.netbeans.modules.profiler.ppoints.ui.TriggeredTakeSnapshotCustomizer createCustomizer()
meth protected void storeProfilingPoint(org.netbeans.modules.profiler.ppoints.ProfilingPoint,int,java.util.Properties)
meth public boolean supportsCPU()
meth public boolean supportsMemory()
meth public boolean supportsMonitor()
meth public int getScope()
meth public java.lang.String getDescription()
meth public java.lang.String getHint()
meth public java.lang.String getType()
meth public javax.swing.Icon getDisabledIcon()
meth public javax.swing.Icon getIcon()
meth public org.netbeans.modules.profiler.ppoints.TriggeredTakeSnapshotProfilingPoint create(org.openide.util.Lookup$Provider)
supr org.netbeans.modules.profiler.ppoints.CodeProfilingPointFactory

CLSS public org.netbeans.modules.profiler.ppoints.Utils
cons public init()
fld public final static javax.swing.ImageIcon EMPTY_ICON
meth public static boolean isValidLocation(org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location)
meth public static double getDurationInMicroSec(long,long)
meth public static int getDocumentOffset(org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location)
meth public static java.awt.Font getTitledBorderFont(javax.swing.border.TitledBorder)
meth public static java.lang.String formatLocalProfilingPointTime(long)
meth public static java.lang.String formatProfilingPointTime(long)
meth public static java.lang.String formatProfilingPointTimeHiRes(long)
meth public static java.lang.String getAbsolutePath(org.openide.util.Lookup$Provider,java.lang.String)
meth public static java.lang.String getClassName(org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location)
meth public static java.lang.String getMethodName(org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location)
meth public static java.lang.String getRelativePath(org.openide.util.Lookup$Provider,java.lang.String)
meth public static java.lang.String getThreadClassName(int)
meth public static java.lang.String getThreadName(int)
meth public static java.lang.String getUniqueName(java.lang.String,java.lang.String,org.openide.util.Lookup$Provider)
meth public static javax.swing.ListCellRenderer getPresenterListRenderer()
meth public static javax.swing.ListCellRenderer getProjectListRenderer()
meth public static long getTimeInMillis(long)
meth public static org.netbeans.lib.profiler.ui.swing.renderer.ProfilerRenderer getPresenterRenderer()
meth public static org.netbeans.lib.profiler.ui.swing.renderer.ProfilerRenderer getProjectRenderer()
meth public static org.netbeans.lib.profiler.ui.swing.renderer.ProfilerRenderer getScopeRenderer()
meth public static org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location getCurrentLocation(int)
meth public static org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location getCurrentSelectionEndLocation(int)
meth public static org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location getCurrentSelectionStartLocation(int)
meth public static org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location[] getCurrentSelectionLocations()
meth public static org.netbeans.modules.profiler.ppoints.CodeProfilingPoint[] getProfilingPointsOnLine(org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location)
meth public static org.openide.text.Line getEditorLine(org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location)
meth public static org.openide.text.Line getEditorLine(org.netbeans.modules.profiler.ppoints.CodeProfilingPoint,org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Annotation)
meth public static org.openide.util.Lookup$Provider getCurrentProject()
meth public static org.openide.util.Lookup$Provider getMostActiveJavaProject()
meth public static void checkLocation(org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Paired)
meth public static void checkLocation(org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Single)
meth public static void openLocation(org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location)
supr java.lang.Object
hfds PROJECT_DIRECTORY_MARK,dayDateFormat,fullDateFormat,presenterListRenderer,presenterRenderer,projectListRenderer,projectRenderer,scopeRenderer,todayDateFormat,todayDateFormatHiRes
hcls ProfilingPointPresenterListRenderer,ProfilingPointPresenterRenderer,ProfilingPointScopeRenderer,ProjectPresenterListRenderer,ProjectPresenterRenderer

CLSS public org.netbeans.modules.profiler.ppoints.ui.CustomizeProfilingPointAction
cons public init()
intf org.openide.util.ContextAwareAction
meth public java.lang.String getName()
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void actionPerformed(java.awt.event.ActionEvent)
supr org.openide.util.actions.SystemAction

CLSS public org.netbeans.modules.profiler.ppoints.ui.DeleteProfilingPointAction
cons public init()
intf org.openide.util.ContextAwareAction
meth public java.lang.String getName()
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void actionPerformed(java.awt.event.ActionEvent)
supr org.openide.util.actions.SystemAction

CLSS public org.netbeans.modules.profiler.ppoints.ui.EnableDisableProfilingPointAction
cons public init()
intf org.openide.util.ContextAwareAction
meth public java.lang.String getName()
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void actionPerformed(java.awt.event.ActionEvent)
supr org.openide.util.actions.SystemAction
hfds action
hcls ContextAwareAction

CLSS public org.netbeans.modules.profiler.ppoints.ui.InsertProfilingPointAction
cons public init()
meth protected boolean asynchronous()
meth protected boolean enable(org.openide.nodes.Node[])
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void performAction(org.openide.util.Lookup$Provider)
supr org.openide.util.actions.NodeAction

CLSS public org.netbeans.modules.profiler.ppoints.ui.InsertProfilingPointMenuAction
cons public init()
meth protected int[] enabledStates()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static org.netbeans.modules.profiler.ppoints.ui.InsertProfilingPointMenuAction getInstance()
meth public void performAction()
supr org.netbeans.modules.profiler.actions.ProfilingAwareAction
hfds ENABLED_STATES
hcls Singleton

CLSS public org.netbeans.modules.profiler.ppoints.ui.LoadGeneratorCustomizer
cons public init(java.lang.String,javax.swing.Icon)
intf java.awt.event.ActionListener
intf javax.swing.event.ChangeListener
intf javax.swing.event.DocumentListener
intf org.netbeans.modules.profiler.ppoints.ui.ValidityListener
intf org.openide.util.HelpCtx$Provider
meth public int getPreferredCaptionAreaWidth()
meth public java.awt.Component getInitialFocusTarget()
meth public java.lang.String getPPName()
meth public java.lang.String getScriptFile()
meth public org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location getPPEndLocation()
meth public org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location getPPStartLocation()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static void main(java.lang.String[])
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void changedUpdate(javax.swing.event.DocumentEvent)
meth public void initComponents(java.lang.String,javax.swing.Icon)
meth public void insertUpdate(javax.swing.event.DocumentEvent)
meth public void normalizeCaptionAreaWidth()
meth public void removeUpdate(javax.swing.event.DocumentEvent)
meth public void setPPEndLocation(org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location)
meth public void setPPName(java.lang.String)
meth public void setPPStartLocation(org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location)
meth public void setProject(org.openide.util.Lookup$Provider)
meth public void setScriptFile(java.lang.String)
meth public void stateChanged(javax.swing.event.ChangeEvent)
meth public void validityChanged(boolean)
supr org.netbeans.modules.profiler.ppoints.ui.ValidityAwarePanel
hfds HELP_CTX,HELP_CTX_KEY,captionLabel,captionPanel,defaultTextComponentHeight,fileChooser,firstLineCaptionSpacer,locationBeginCustomizer,locationBeginHeaderLabel,locationBeginHeaderSeparator,locationEndCustomizer,locationEndHeaderLabel,locationEndHeaderSeparator,nameLabel,nameTextField,project,scriptButton,scriptLabel,scriptTextField,secondLineCaptionSpacer,settingsHeaderLabel,settingsHeaderSeparator,stopDefineRadio,stopLabel,stopOnStopRadio,thirdLineCaptionSpacer

CLSS public org.netbeans.modules.profiler.ppoints.ui.LocationCustomizer
cons public init()
intf java.awt.event.ActionListener
intf java.awt.event.HierarchyListener
intf javax.swing.event.ChangeListener
intf javax.swing.event.DocumentListener
meth public int getPreferredCaptionAreaWidth()
meth public org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location getPPLocation()
meth public static void main(java.lang.String[])
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void changedUpdate(javax.swing.event.DocumentEvent)
meth public void hierarchyChanged(java.awt.event.HierarchyEvent)
meth public void insertUpdate(javax.swing.event.DocumentEvent)
meth public void normalizeCaptionAreaWidth(int)
meth public void removeUpdate(javax.swing.event.DocumentEvent)
meth public void resetMnemonic()
meth public void setEnabled(boolean)
meth public void setPPLocation(org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location)
meth public void stateChanged(javax.swing.event.ChangeEvent)
supr org.netbeans.modules.profiler.ppoints.ui.ValidityAwarePanel
hfds defaultTextComponentHeight,fileButton,fileChooser,fileLabel,fileTextField,firstLineCaptionSpacer,fromEditorButton,fromEditorSeparator,lineBeginRadio,lineEndRadio,lineLabel,lineNumberSpinner,lineOffsetRadio,lineOffsetSpinner,secondLineCaptionSpacer
hcls HTMLButton

CLSS public org.netbeans.modules.profiler.ppoints.ui.OpenProfilingPointsWindowAction
cons public init()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void actionPerformed(java.awt.event.ActionEvent)
supr org.openide.util.actions.SystemAction

CLSS public abstract org.netbeans.modules.profiler.ppoints.ui.ProfilingPointReport
cons public init()
meth protected abstract void refresh()
meth protected final java.lang.String preferredID()
meth protected void componentClosed()
meth protected void componentOpened()
meth public final int getPersistenceType()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static java.lang.String getNoDataHint(org.netbeans.modules.profiler.ppoints.ProfilingPoint)
meth public static void refreshOpenReports()
supr org.netbeans.modules.profiler.ProfilerTopComponent
hfds HELP_CTX,HELP_CTX_KEY,REF_CPU_INSTR,currentSettings,openReports,profilingRunning

CLSS public org.netbeans.modules.profiler.ppoints.ui.ProfilingPointsDisplayer
intf org.openide.util.HelpCtx$Provider
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static void displayProfilingPoints(org.openide.util.Lookup$Provider,org.netbeans.lib.profiler.common.ProfilingSettings)
supr javax.swing.JPanel
hfds HELP_CTX,HELP_CTX_KEY,defaultInstance,list,listModel

CLSS public abstract interface org.netbeans.modules.profiler.ppoints.ui.ProfilingPointsIcons
fld public final static java.lang.String ADD = "ProfilingPointsIcons.Add"
fld public final static java.lang.String CODE = "ProfilingPointsIcons.Code"
fld public final static java.lang.String EDIT = "ProfilingPointsIcons.Edit"
fld public final static java.lang.String ENABLE_DISABLE = "ProfilingPointsIcons.EnableDisable"
fld public final static java.lang.String GLOBAL = "ProfilingPointsIcons.Global"
fld public final static java.lang.String LOAD_GENERATOR = "ProfilingPointsIcons.LoadGenerator"
fld public final static java.lang.String LOAD_GENERATOR_DISABLED = "ProfilingPointsIcons.LoadGeneratorDisabled"
fld public final static java.lang.String PPOINT = "ProfilingPointsIcons.PPoint"
fld public final static java.lang.String REMOVE = "ProfilingPointsIcons.Remove"
fld public final static java.lang.String RESET_RESULTS = "ProfilingPointsIcons.ResetResults"
fld public final static java.lang.String RESET_RESULTS_DISABLED = "ProfilingPointsIcons.ResetResultsDisabled"
fld public final static java.lang.String STOPWATCH = "ProfilingPointsIcons.Stopwatch"
fld public final static java.lang.String STOPWATCH_DISABLED = "ProfilingPointsIcons.StopwatchDisabled"
fld public final static java.lang.String TAKE_SNAPSHOT = "ProfilingPointsIcons.TakeSnapshot"
fld public final static java.lang.String TAKE_SNAPSHOT_DISABLED = "ProfilingPointsIcons.TakeSnapshotDisabled"
fld public final static java.lang.String TAKE_SNAPSHOT_TIMED = "ProfilingPointsIcons.TakeSnapshotTimed"
fld public final static java.lang.String TAKE_SNAPSHOT_TRIGGERED = "ProfilingPointsIcons.TakeSnapshotTriggered"
intf org.netbeans.modules.profiler.api.icons.Icons$Keys

CLSS public abstract org.netbeans.modules.profiler.ppoints.ui.ProfilingPointsUIHelper
cons public init()
innr public static Basic
meth public abstract boolean displaySubprojectsOption()
meth public abstract java.lang.String getAllProjectsString()
supr java.lang.Object
hfds INSTANCE

CLSS public static org.netbeans.modules.profiler.ppoints.ui.ProfilingPointsUIHelper$Basic
 outer org.netbeans.modules.profiler.ppoints.ui.ProfilingPointsUIHelper
cons public init()
meth public boolean displaySubprojectsOption()
meth public java.lang.String getAllProjectsString()
supr org.netbeans.modules.profiler.ppoints.ui.ProfilingPointsUIHelper

CLSS public org.netbeans.modules.profiler.ppoints.ui.ProfilingPointsWindow
cons public init()
meth protected java.lang.String preferredID()
meth public boolean needsDocking()
meth public int getPersistenceType()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static boolean hasDefault()
meth public static org.netbeans.modules.profiler.ppoints.ui.ProfilingPointsWindow getDefault()
meth public static void closeIfOpened()
meth public void notifyProfilingStateChanged()
meth public void open()
supr org.netbeans.modules.profiler.ProfilerTopComponent
hfds HELP_CTX,HELP_CTX_KEY,ID,defaultInstance,serialVersionUID,windowUI

CLSS public org.netbeans.modules.profiler.ppoints.ui.ProfilingPointsWindowUI
cons public init()
intf java.awt.event.ActionListener
intf java.awt.event.KeyListener
intf java.awt.event.MouseListener
intf java.awt.event.MouseMotionListener
intf java.beans.PropertyChangeListener
intf javax.swing.event.ListSelectionListener
meth public org.openide.util.Lookup$Provider getSelectedProject()
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void keyPressed(java.awt.event.KeyEvent)
meth public void keyReleased(java.awt.event.KeyEvent)
meth public void keyTyped(java.awt.event.KeyEvent)
meth public void mouseClicked(java.awt.event.MouseEvent)
meth public void mouseDragged(java.awt.event.MouseEvent)
meth public void mouseEntered(java.awt.event.MouseEvent)
meth public void mouseExited(java.awt.event.MouseEvent)
meth public void mouseMoved(java.awt.event.MouseEvent)
meth public void mousePressed(java.awt.event.MouseEvent)
meth public void mouseReleased(java.awt.event.MouseEvent)
meth public void notifyProfilingStateChanged()
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void valueChanged(javax.swing.event.ListSelectionEvent)
supr javax.swing.JPanel
hfds PPOINT_ADD_ICON,PPOINT_EDIT_ICON,PPOINT_ENABLE_DISABLE_ICON,PPOINT_REMOVE_ICON,addButton,dependenciesCheckbox,disableButton,disableItem,editButton,editItem,enableDisableItem,enableItem,ppointProjectSelector,profilingInProgress,profilingPoints,profilingPointsPopup,profilingPointsTable,profilingPointsTableModel,projectLabel,removeButton,removeItem,showEndInSourceItem,showInSourceItem,showReportItem,showStartInSourceItem

CLSS public org.netbeans.modules.profiler.ppoints.ui.ResetResultsCustomizer
cons public init(java.lang.String,javax.swing.Icon)
intf javax.swing.event.DocumentListener
intf org.netbeans.modules.profiler.ppoints.ui.ValidityListener
intf org.openide.util.HelpCtx$Provider
meth public java.awt.Component getInitialFocusTarget()
meth public java.lang.String getPPName()
meth public org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location getPPLocation()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static void main(java.lang.String[])
meth public void changedUpdate(javax.swing.event.DocumentEvent)
meth public void insertUpdate(javax.swing.event.DocumentEvent)
meth public void removeUpdate(javax.swing.event.DocumentEvent)
meth public void setPPLocation(org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location)
meth public void setPPName(java.lang.String)
meth public void validityChanged(boolean)
supr org.netbeans.modules.profiler.ppoints.ui.ValidityAwarePanel
hfds HELP_CTX,HELP_CTX_KEY,captionLabel,captionPanel,defaultTextComponentHeight,firstLineCaptionSpacer,locationCustomizer,locationHeaderLabel,locationHeaderSeparator,nameLabel,nameTextField

CLSS public org.netbeans.modules.profiler.ppoints.ui.ShowOppositeProfilingPointAction
cons public init()
intf org.openide.util.ContextAwareAction
meth public java.lang.String getName()
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void actionPerformed(java.awt.event.ActionEvent)
supr org.openide.util.actions.SystemAction
hcls InvocationLocationDescriptor

CLSS public org.netbeans.modules.profiler.ppoints.ui.SnapshotCustomizer
cons public init()
intf java.awt.event.ActionListener
intf javax.swing.event.ChangeListener
intf javax.swing.event.DocumentListener
meth public int getPreferredCaptionAreaWidth()
meth public static void main(java.lang.String[])
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void changedUpdate(javax.swing.event.DocumentEvent)
meth public void insertUpdate(javax.swing.event.DocumentEvent)
meth public void normalizeCaptionAreaWidth(int)
meth public void removeUpdate(javax.swing.event.DocumentEvent)
meth public void stateChanged(javax.swing.event.ChangeEvent)
supr org.netbeans.modules.profiler.ppoints.ui.ValidityAwarePanel
hfds defaultTextComponentHeight,fileChooser,firstLineCaptionSpacer,resetResultsCheckbox,saveLabel,saveToFileButton,saveToFileField,saveToFileRadio,saveToProjectRadio,secondLineCaptionSpacer,takeHeapdumpRadio,takeLabel,takeSnapshotRadio,thirdLineCaptionSpacer

CLSS public org.netbeans.modules.profiler.ppoints.ui.StopwatchCustomizer
cons public init(java.lang.String,javax.swing.Icon)
intf javax.swing.event.ChangeListener
intf javax.swing.event.DocumentListener
intf org.netbeans.modules.profiler.ppoints.ui.ValidityListener
intf org.openide.util.HelpCtx$Provider
meth public int getPreferredCaptionAreaWidth()
meth public java.awt.Component getInitialFocusTarget()
meth public java.lang.String getPPName()
meth public org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location getPPEndLocation()
meth public org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location getPPStartLocation()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static void main(java.lang.String[])
meth public void changedUpdate(javax.swing.event.DocumentEvent)
meth public void initComponents(java.lang.String,javax.swing.Icon)
meth public void insertUpdate(javax.swing.event.DocumentEvent)
meth public void normalizeCaptionAreaWidth()
meth public void removeUpdate(javax.swing.event.DocumentEvent)
meth public void setPPEndLocation(org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location)
meth public void setPPName(java.lang.String)
meth public void setPPStartLocation(org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location)
meth public void stateChanged(javax.swing.event.ChangeEvent)
meth public void validityChanged(boolean)
supr org.netbeans.modules.profiler.ppoints.ui.ValidityAwarePanel
hfds HELP_CTX,HELP_CTX_KEY,captionLabel,captionPanel,defaultTextComponentHeight,firstLineCaptionSpacer,locationBeginCustomizer,locationBeginHeaderLabel,locationBeginHeaderSeparator,locationEndCustomizer,locationEndHeaderLabel,locationEndHeaderSeparator,measureLabel,measureTimestampDurationRadio,measureTimestampRadio,nameLabel,nameTextField,settingsHeaderLabel,settingsHeaderSeparator

CLSS public org.netbeans.modules.profiler.ppoints.ui.TakeSnapshotCustomizer
cons public init(java.lang.String,javax.swing.Icon)
intf javax.swing.event.DocumentListener
intf org.netbeans.modules.profiler.ppoints.ui.ValidityListener
intf org.openide.util.HelpCtx$Provider
meth public boolean getPPResetResults()
meth public boolean getPPTarget()
meth public boolean getPPType()
meth public int getPreferredCaptionAreaWidth()
meth public java.awt.Component getInitialFocusTarget()
meth public java.lang.String getPPFile()
meth public java.lang.String getPPName()
meth public org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location getPPLocation()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static void main(java.lang.String[])
meth public void changedUpdate(javax.swing.event.DocumentEvent)
meth public void initComponents(java.lang.String,javax.swing.Icon)
meth public void insertUpdate(javax.swing.event.DocumentEvent)
meth public void normalizeCaptionAreaWidth()
meth public void removeUpdate(javax.swing.event.DocumentEvent)
meth public void setPPFile(java.lang.String)
meth public void setPPLocation(org.netbeans.modules.profiler.ppoints.CodeProfilingPoint$Location)
meth public void setPPName(java.lang.String)
meth public void setPPResetResults(boolean)
meth public void setPPTarget(boolean)
meth public void setPPType(boolean)
meth public void validityChanged(boolean)
supr org.netbeans.modules.profiler.ppoints.ui.ValidityAwarePanel
hfds HELP_CTX,HELP_CTX_KEY,captionLabel,captionPanel,defaultTextComponentHeight,firstLineCaptionSpacer,locationCustomizer,locationHeaderLabel,locationHeaderSeparator,nameLabel,nameTextField,settingsHeaderLabel,settingsHeaderSeparator,snapshotCustomizer

CLSS public org.netbeans.modules.profiler.ppoints.ui.TimeCustomizer
cons public init()
intf java.awt.event.ActionListener
intf javax.swing.event.ChangeListener
intf javax.swing.event.DocumentListener
meth public int getPreferredCaptionAreaWidth()
meth public org.netbeans.modules.profiler.ppoints.TimedGlobalProfilingPoint$TimeCondition getTimeCondition()
meth public static void main(java.lang.String[])
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void changedUpdate(javax.swing.event.DocumentEvent)
meth public void insertUpdate(javax.swing.event.DocumentEvent)
meth public void normalizeCaptionAreaWidth()
meth public void removeUpdate(javax.swing.event.DocumentEvent)
meth public void setTimeCondition(org.netbeans.modules.profiler.ppoints.TimedGlobalProfilingPoint$TimeCondition)
meth public void stateChanged(javax.swing.event.ChangeEvent)
supr org.netbeans.modules.profiler.ppoints.ui.ValidityAwarePanel
hfds DATE_FORMAT,TEXT_FOREGROUND,TIME_FORMAT,defaultTextComponentHeight,firstLineCaptionSpacer,secondLineCaptionSpacer,timeFrequencyCombo,timeFrequencyRadio,timeFrequencySpinner,timeOnceRadio,timeTakeAtButton,timeTakeAtDateButton,timeTakeAtDateField,timeTakeAtField,timeTakeAtLabel

CLSS public org.netbeans.modules.profiler.ppoints.ui.TimedTakeSnapshotCustomizer
cons public init(java.lang.String,javax.swing.Icon)
intf javax.swing.event.DocumentListener
intf org.netbeans.modules.profiler.ppoints.ui.ValidityListener
intf org.openide.util.HelpCtx$Provider
meth public boolean getPPResetResults()
meth public boolean getPPTarget()
meth public boolean getPPType()
meth public int getPreferredCaptionAreaWidth()
meth public java.awt.Component getInitialFocusTarget()
meth public java.lang.String getPPFile()
meth public java.lang.String getPPName()
meth public org.netbeans.modules.profiler.ppoints.TimedGlobalProfilingPoint$TimeCondition getTimeCondition()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static void main(java.lang.String[])
meth public void changedUpdate(javax.swing.event.DocumentEvent)
meth public void initComponents(java.lang.String,javax.swing.Icon)
meth public void insertUpdate(javax.swing.event.DocumentEvent)
meth public void normalizeCaptionAreaWidth()
meth public void removeUpdate(javax.swing.event.DocumentEvent)
meth public void setPPFile(java.lang.String)
meth public void setPPName(java.lang.String)
meth public void setPPResetResults(boolean)
meth public void setPPTarget(boolean)
meth public void setPPType(boolean)
meth public void setTimeCondition(org.netbeans.modules.profiler.ppoints.TimedGlobalProfilingPoint$TimeCondition)
meth public void validityChanged(boolean)
supr org.netbeans.modules.profiler.ppoints.ui.ValidityAwarePanel
hfds HELP_CTX,HELP_CTX_KEY,captionLabel,captionPanel,defaultTextComponentHeight,nameLabel,nameTextField,settingsHeaderLabel,settingsHeaderSeparator,snapshotCustomizer,timeCustomizer,timeHeaderLabel,timeHeaderSeparator

CLSS public org.netbeans.modules.profiler.ppoints.ui.ToggleProfilingPointAction
cons public init()
intf java.awt.event.AWTEventListener
meth public boolean isEnabled()
meth public static org.netbeans.modules.profiler.ppoints.ui.ToggleProfilingPointAction getInstance()
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void eventDispatched(java.awt.AWTEvent)
supr javax.swing.AbstractAction
hfds acceleratorKeyStroke,currentFactory,instance,ppFactories,ppSwitcher,warningDialogOpened
hcls ProfilingPointsSwitcher

CLSS public org.netbeans.modules.profiler.ppoints.ui.TriggerCustomizer
cons public init()
intf java.awt.event.ActionListener
meth public int getPreferredCaptionAreaWidth()
meth public org.netbeans.modules.profiler.ppoints.TriggeredGlobalProfilingPoint$TriggerCondition getTriggerCondition()
meth public static void main(java.lang.String[])
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void normalizeCaptionAreaWidth()
meth public void setTriggerCondition(org.netbeans.modules.profiler.ppoints.TriggeredGlobalProfilingPoint$TriggerCondition)
supr org.netbeans.modules.profiler.ppoints.ui.ValidityAwarePanel
hfds defaultTextComponentHeight,percentsModel,triggerAlwaysRadio,triggerExceedsLabel,triggerGenerationsLabel,triggerOnceRadio,triggerValueSpinner,triggerWhenCombo,triggerWhenLabel,unitsModel

CLSS public org.netbeans.modules.profiler.ppoints.ui.TriggeredTakeSnapshotCustomizer
cons public init(java.lang.String,javax.swing.Icon)
intf javax.swing.event.DocumentListener
intf org.netbeans.modules.profiler.ppoints.ui.ValidityListener
intf org.openide.util.HelpCtx$Provider
meth public boolean getPPResetResults()
meth public boolean getPPTarget()
meth public boolean getPPType()
meth public int getPreferredCaptionAreaWidth()
meth public java.awt.Component getInitialFocusTarget()
meth public java.lang.String getPPFile()
meth public java.lang.String getPPName()
meth public org.netbeans.modules.profiler.ppoints.TriggeredGlobalProfilingPoint$TriggerCondition getTriggerCondition()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static void main(java.lang.String[])
meth public void changedUpdate(javax.swing.event.DocumentEvent)
meth public void initComponents(java.lang.String,javax.swing.Icon)
meth public void insertUpdate(javax.swing.event.DocumentEvent)
meth public void normalizeCaptionAreaWidth()
meth public void removeUpdate(javax.swing.event.DocumentEvent)
meth public void setPPFile(java.lang.String)
meth public void setPPName(java.lang.String)
meth public void setPPResetResults(boolean)
meth public void setPPTarget(boolean)
meth public void setPPType(boolean)
meth public void setTriggerCondition(org.netbeans.modules.profiler.ppoints.TriggeredGlobalProfilingPoint$TriggerCondition)
meth public void validityChanged(boolean)
supr org.netbeans.modules.profiler.ppoints.ui.ValidityAwarePanel
hfds HELP_CTX,HELP_CTX_KEY,captionLabel,captionPanel,defaultTextComponentHeight,nameLabel,nameTextField,settingsHeaderLabel,settingsHeaderSeparator,snapshotCustomizer,triggerAlwaysRadio,triggerCustomizer,triggerExceedsLabel,triggerGenerationsLabel,triggerHeaderLabel,triggerHeaderSeparator,triggerOnceRadio,triggerWhenCombo,triggerWhenLabel

CLSS public abstract org.netbeans.modules.profiler.ppoints.ui.ValidityAwarePanel
cons public init()
intf javax.swing.Scrollable
meth protected void fireValidityChanged(boolean)
meth public boolean areSettingsValid()
meth public boolean getScrollableTracksViewportHeight()
meth public boolean getScrollableTracksViewportWidth()
meth public int getScrollableBlockIncrement(java.awt.Rectangle,int,int)
meth public int getScrollableUnitIncrement(java.awt.Rectangle,int,int)
meth public java.awt.Component getInitialFocusTarget()
meth public java.awt.Dimension getPreferredScrollableViewportSize()
meth public java.awt.Dimension getPreferredSize()
meth public void addValidityListener(org.netbeans.modules.profiler.ppoints.ui.ValidityListener)
meth public void removeValidityListener(org.netbeans.modules.profiler.ppoints.ui.ValidityListener)
supr javax.swing.JPanel
hfds isValid,listeners

CLSS public abstract interface org.netbeans.modules.profiler.ppoints.ui.ValidityListener
meth public abstract void validityChanged(boolean)

CLSS public org.netbeans.modules.profiler.ppoints.ui.WizardPanel1UI
cons public init()
intf org.openide.util.HelpCtx$Provider
meth public boolean hasDefaultScope()
meth public int getSelectedIndex()
meth public java.awt.Dimension getMinSize()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public org.openide.util.Lookup$Provider getSelectedProject()
meth public void init(org.netbeans.modules.profiler.ppoints.ProfilingPointFactory[])
meth public void setSelectedIndex(int)
meth public void setSelectedProject(org.openide.util.Lookup$Provider)
supr org.netbeans.modules.profiler.ppoints.ui.ValidityAwarePanel
hfds CPU_ICON,HELP_CTX,HELP_CTX_KEY,MEMORY_ICON,MONITOR_ICON,hasDefaultScope,initialMinSize,ppFactories,ppointDescriptionArea,ppointDescriptionCaptionLabel,ppointEffectiveCPULabel,ppointEffectiveCaptionLabel,ppointEffectiveMemoryLabel,ppointEffectiveMonitorLabel,ppointProjectLabel,ppointProjectSelector,ppointTypeCaptionLabel,ppointTypeTable,ppointTypeTableModel
hcls PPointTypeTableModel

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

CLSS public abstract interface static org.openide.WizardDescriptor$Iterator<%0 extends java.lang.Object>
 outer org.openide.WizardDescriptor
meth public abstract boolean hasNext()
meth public abstract boolean hasPrevious()
meth public abstract java.lang.String name()
meth public abstract org.openide.WizardDescriptor$Panel<{org.openide.WizardDescriptor$Iterator%0}> current()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void nextPanel()
meth public abstract void previousPanel()
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public abstract org.openide.text.Annotation
cons public init()
fld public final static java.lang.String PROP_ANNOTATION_TYPE = "annotationType"
fld public final static java.lang.String PROP_MOVE_TO_FRONT = "moveToFront"
fld public final static java.lang.String PROP_SHORT_DESCRIPTION = "shortDescription"
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void notifyAttached(org.openide.text.Annotatable)
meth protected void notifyDetached(org.openide.text.Annotatable)
meth public abstract java.lang.String getAnnotationType()
meth public abstract java.lang.String getShortDescription()
meth public final org.openide.text.Annotatable getAttachedAnnotatable()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void attach(org.openide.text.Annotatable)
meth public final void detach()
meth public final void moveToFront()
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds attached,inDocument,support

CLSS public abstract interface org.openide.text.AnnotationProvider
meth public abstract void annotate(org.openide.text.Line$Set,org.openide.util.Lookup)

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

