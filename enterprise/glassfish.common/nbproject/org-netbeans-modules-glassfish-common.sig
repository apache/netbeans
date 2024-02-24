#Signature file v4.1
#Version 1.97

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
meth protected boolean requestFocus(boolean,java.awt.event.FocusEvent$Cause)
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
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public boolean areFocusTraversalKeysSet(int)
meth public boolean contains(int,int)
meth public boolean contains(java.awt.Point)
meth public boolean getFocusTraversalKeysEnabled()
meth public boolean getIgnoreRepaint()
meth public boolean gotFocus(java.awt.Event,java.lang.Object)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public boolean handleEvent(java.awt.Event)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public boolean hasFocus()
meth public boolean imageUpdate(java.awt.Image,int,int,int,int,int)
meth public boolean inside(int,int)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public boolean isBackgroundSet()
meth public boolean isCursorSet()
meth public boolean isDisplayable()
meth public boolean isDoubleBuffered()
meth public boolean isEnabled()
meth public boolean isFocusCycleRoot(java.awt.Container)
meth public boolean isFocusOwner()
meth public boolean isFocusTraversable()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
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
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public boolean keyUp(java.awt.Event,int)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public boolean lostFocus(java.awt.Event,java.lang.Object)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public boolean mouseDown(java.awt.Event,int,int)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public boolean mouseDrag(java.awt.Event,int,int)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public boolean mouseEnter(java.awt.Event,int,int)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public boolean mouseExit(java.awt.Event,int,int)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public boolean mouseMove(java.awt.Event,int,int)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public boolean mouseUp(java.awt.Event,int,int)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public boolean postEvent(java.awt.Event)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public boolean prepareImage(java.awt.Image,int,int,java.awt.image.ImageObserver)
meth public boolean prepareImage(java.awt.Image,java.awt.image.ImageObserver)
meth public boolean requestFocusInWindow()
meth public boolean requestFocusInWindow(java.awt.event.FocusEvent$Cause)
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
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
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
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public java.awt.Dimension preferredSize()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public java.awt.Dimension size()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
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
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public java.awt.Rectangle bounds()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
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
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public void disable()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public void doLayout()
meth public void enable()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public void enable(boolean)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public void enableInputMethods(boolean)
meth public void firePropertyChange(java.lang.String,byte,byte)
meth public void firePropertyChange(java.lang.String,char,char)
meth public void firePropertyChange(java.lang.String,double,double)
meth public void firePropertyChange(java.lang.String,float,float)
meth public void firePropertyChange(java.lang.String,long,long)
meth public void firePropertyChange(java.lang.String,short,short)
meth public void hide()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public void invalidate()
meth public void layout()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public void list()
meth public void list(java.io.PrintStream)
meth public void list(java.io.PrintStream,int)
meth public void list(java.io.PrintWriter)
meth public void list(java.io.PrintWriter,int)
meth public void move(int,int)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public void nextFocus()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
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
meth public void requestFocus(java.awt.event.FocusEvent$Cause)
meth public void reshape(int,int,int,int)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public void resize(int,int)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public void resize(java.awt.Dimension)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
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
meth public void setMixingCutoutShape(java.awt.Shape)
meth public void setName(java.lang.String)
meth public void setPreferredSize(java.awt.Dimension)
meth public void setSize(int,int)
meth public void setSize(java.awt.Dimension)
meth public void setVisible(boolean)
meth public void show()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public void show(boolean)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
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
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
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
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public java.awt.Component[] getComponents()
meth public java.awt.Dimension getMaximumSize()
meth public java.awt.Dimension getMinimumSize()
meth public java.awt.Dimension getPreferredSize()
meth public java.awt.Dimension minimumSize()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public java.awt.Dimension preferredSize()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public java.awt.FocusTraversalPolicy getFocusTraversalPolicy()
meth public java.awt.Insets getInsets()
meth public java.awt.Insets insets()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
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
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public void doLayout()
meth public void invalidate()
meth public void layout()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
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
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
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

CLSS public abstract interface !annotation java.beans.JavaBean
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String defaultEventSet()
meth public abstract !hasdefault java.lang.String defaultProperty()
meth public abstract !hasdefault java.lang.String description()

CLSS public abstract interface java.io.Externalizable
intf java.io.Serializable
meth public abstract void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public abstract void writeExternal(java.io.ObjectOutput) throws java.io.IOException

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

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable

CLSS public abstract interface !annotation java.lang.FunctionalInterface
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public java.lang.Object
cons public init()
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void finalize() throws java.lang.Throwable
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="9")
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

CLSS public java.lang.Thread
cons public init()
cons public init(java.lang.Runnable)
cons public init(java.lang.Runnable,java.lang.String)
cons public init(java.lang.String)
cons public init(java.lang.ThreadGroup,java.lang.Runnable)
cons public init(java.lang.ThreadGroup,java.lang.Runnable,java.lang.String)
cons public init(java.lang.ThreadGroup,java.lang.Runnable,java.lang.String,long)
cons public init(java.lang.ThreadGroup,java.lang.Runnable,java.lang.String,long,boolean)
cons public init(java.lang.ThreadGroup,java.lang.String)
fld public final static int MAX_PRIORITY = 10
fld public final static int MIN_PRIORITY = 1
fld public final static int NORM_PRIORITY = 5
innr public abstract interface static UncaughtExceptionHandler
innr public final static !enum State
intf java.lang.Runnable
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public boolean isInterrupted()
meth public final boolean isAlive()
meth public final boolean isDaemon()
meth public final int getPriority()
meth public final java.lang.String getName()
meth public final java.lang.ThreadGroup getThreadGroup()
meth public final void checkAccess()
meth public final void join() throws java.lang.InterruptedException
meth public final void join(long) throws java.lang.InterruptedException
meth public final void join(long,int) throws java.lang.InterruptedException
meth public final void resume()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="1.2")
meth public final void setDaemon(boolean)
meth public final void setName(java.lang.String)
meth public final void setPriority(int)
meth public final void stop()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="1.2")
meth public final void suspend()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="1.2")
meth public int countStackFrames()
 anno 0 java.lang.Deprecated(boolean forRemoval=true, java.lang.String since="1.2")
meth public java.lang.ClassLoader getContextClassLoader()
meth public java.lang.StackTraceElement[] getStackTrace()
meth public java.lang.String toString()
meth public java.lang.Thread$State getState()
meth public java.lang.Thread$UncaughtExceptionHandler getUncaughtExceptionHandler()
meth public long getId()
meth public static boolean holdsLock(java.lang.Object)
meth public static boolean interrupted()
meth public static int activeCount()
meth public static int enumerate(java.lang.Thread[])
meth public static java.lang.Thread currentThread()
meth public static java.lang.Thread$UncaughtExceptionHandler getDefaultUncaughtExceptionHandler()
meth public static java.util.Map<java.lang.Thread,java.lang.StackTraceElement[]> getAllStackTraces()
meth public static void dumpStack()
meth public static void onSpinWait()
meth public static void setDefaultUncaughtExceptionHandler(java.lang.Thread$UncaughtExceptionHandler)
meth public static void sleep(long) throws java.lang.InterruptedException
meth public static void sleep(long,int) throws java.lang.InterruptedException
meth public static void yield()
meth public void interrupt()
meth public void run()
meth public void setContextClassLoader(java.lang.ClassLoader)
meth public void setUncaughtExceptionHandler(java.lang.Thread$UncaughtExceptionHandler)
meth public void start()
supr java.lang.Object

CLSS public java.lang.Throwable
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
intf java.io.Serializable
meth public final java.lang.Throwable[] getSuppressed()
meth public final void addSuppressed(java.lang.Throwable)
meth public java.lang.StackTraceElement[] getStackTrace()
meth public java.lang.String getLocalizedMessage()
meth public java.lang.String getMessage()
meth public java.lang.String toString()
meth public java.lang.Throwable fillInStackTrace()
meth public java.lang.Throwable getCause()
meth public java.lang.Throwable initCause(java.lang.Throwable)
meth public void printStackTrace()
meth public void printStackTrace(java.io.PrintStream)
meth public void printStackTrace(java.io.PrintWriter)
meth public void setStackTrace(java.lang.StackTraceElement[])
supr java.lang.Object

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

CLSS public abstract java.net.Authenticator
cons public init()
innr public final static !enum RequestorType
meth protected final int getRequestingPort()
meth protected final java.lang.String getRequestingHost()
meth protected final java.lang.String getRequestingPrompt()
meth protected final java.lang.String getRequestingProtocol()
meth protected final java.lang.String getRequestingScheme()
meth protected final java.net.InetAddress getRequestingSite()
meth protected java.net.Authenticator$RequestorType getRequestorType()
meth protected java.net.PasswordAuthentication getPasswordAuthentication()
meth protected java.net.URL getRequestingURL()
meth public java.net.PasswordAuthentication requestPasswordAuthenticationInstance(java.lang.String,java.net.InetAddress,int,java.lang.String,java.lang.String,java.lang.String,java.net.URL,java.net.Authenticator$RequestorType)
meth public static java.net.Authenticator getDefault()
meth public static java.net.PasswordAuthentication requestPasswordAuthentication(java.lang.String,java.net.InetAddress,int,java.lang.String,java.lang.String,java.lang.String)
meth public static java.net.PasswordAuthentication requestPasswordAuthentication(java.lang.String,java.net.InetAddress,int,java.lang.String,java.lang.String,java.lang.String,java.net.URL,java.net.Authenticator$RequestorType)
meth public static java.net.PasswordAuthentication requestPasswordAuthentication(java.net.Authenticator,java.lang.String,java.net.InetAddress,int,java.lang.String,java.lang.String,java.lang.String,java.net.URL,java.net.Authenticator$RequestorType)
meth public static java.net.PasswordAuthentication requestPasswordAuthentication(java.net.InetAddress,int,java.lang.String,java.lang.String,java.lang.String)
meth public static void setDefault(java.net.Authenticator)
supr java.lang.Object

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

CLSS public abstract interface java.util.Map<%0 extends java.lang.Object, %1 extends java.lang.Object>
innr public abstract interface static Entry
meth public !varargs static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> ofEntries(java.util.Map$Entry<? extends {%%0},? extends {%%1}>[])
 anno 0 java.lang.SafeVarargs()
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
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map$Entry<{%%0},{%%1}> entry({%%0},{%%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> copyOf(java.util.Map<? extends {%%0},? extends {%%1}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> of()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> of({%%0},{%%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> of({%%0},{%%1},{%%0},{%%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> of({%%0},{%%1},{%%0},{%%1},{%%0},{%%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> of({%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> of({%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> of({%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> of({%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> of({%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> of({%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> of({%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1})
meth public void forEach(java.util.function.BiConsumer<? super {java.util.Map%0},? super {java.util.Map%1}>)
meth public void replaceAll(java.util.function.BiFunction<? super {java.util.Map%0},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} compute({java.util.Map%0},java.util.function.BiFunction<? super {java.util.Map%0},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} computeIfAbsent({java.util.Map%0},java.util.function.Function<? super {java.util.Map%0},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} computeIfPresent({java.util.Map%0},java.util.function.BiFunction<? super {java.util.Map%0},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} getOrDefault(java.lang.Object,{java.util.Map%1})
meth public {java.util.Map%1} merge({java.util.Map%0},{java.util.Map%1},java.util.function.BiFunction<? super {java.util.Map%1},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} putIfAbsent({java.util.Map%0},{java.util.Map%1})
meth public {java.util.Map%1} replace({java.util.Map%0},{java.util.Map%1})

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
meth public boolean accept(java.lang.Object)

CLSS public javax.swing.JComboBox<%0 extends java.lang.Object>
 anno 0 java.beans.JavaBean(java.lang.String defaultEventSet="", java.lang.String defaultProperty="UI", java.lang.String description="A combination of a text field and a drop-down list.")
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
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="", java.lang.String[] enumerationValues=[])
meth public int getMaximumRowCount()
meth public int getSelectedIndex()
meth public java.awt.event.ActionListener[] getActionListeners()
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="", java.lang.String[] enumerationValues=[])
meth public java.awt.event.ItemListener[] getItemListeners()
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="", java.lang.String[] enumerationValues=[])
meth public java.lang.Object getSelectedItem()
meth public java.lang.Object[] getSelectedObjects()
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="", java.lang.String[] enumerationValues=[])
meth public java.lang.String getActionCommand()
meth public java.lang.String getUIClassID()
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="", java.lang.String[] enumerationValues=[])
meth public javax.accessibility.AccessibleContext getAccessibleContext()
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="", java.lang.String[] enumerationValues=[])
meth public javax.swing.Action getAction()
meth public javax.swing.ComboBoxEditor getEditor()
meth public javax.swing.ComboBoxModel<{javax.swing.JComboBox%0}> getModel()
meth public javax.swing.JComboBox$KeySelectionManager getKeySelectionManager()
meth public javax.swing.ListCellRenderer<? super {javax.swing.JComboBox%0}> getRenderer()
meth public javax.swing.event.PopupMenuListener[] getPopupMenuListeners()
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="", java.lang.String[] enumerationValues=[])
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
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=true, java.lang.String description="the Action instance connected with this ActionEvent source", java.lang.String[] enumerationValues=[])
meth public void setActionCommand(java.lang.String)
meth public void setEditable(boolean)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=false, boolean preferred=true, boolean required=false, boolean visualUpdate=false, java.lang.String description="If true, the user can type a new value in the combo box.", java.lang.String[] enumerationValues=[])
meth public void setEditor(javax.swing.ComboBoxEditor)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=true, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="The editor that combo box uses to edit the current value", java.lang.String[] enumerationValues=[])
meth public void setEnabled(boolean)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=false, boolean preferred=true, boolean required=false, boolean visualUpdate=false, java.lang.String description="The enabled state of the component.", java.lang.String[] enumerationValues=[])
meth public void setKeySelectionManager(javax.swing.JComboBox$KeySelectionManager)
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=true, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="The objects that changes the selection when a key is pressed.", java.lang.String[] enumerationValues=[])
meth public void setLightWeightPopupEnabled(boolean)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=true, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="Set to <code>false</code> to require heavyweight popups.", java.lang.String[] enumerationValues=[])
meth public void setMaximumRowCount(int)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=false, boolean preferred=true, boolean required=false, boolean visualUpdate=false, java.lang.String description="The maximum number of rows the popup should have", java.lang.String[] enumerationValues=[])
meth public void setModel(javax.swing.ComboBoxModel<{javax.swing.JComboBox%0}>)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="Model that the combo box uses to get data to display.", java.lang.String[] enumerationValues=[])
meth public void setPopupVisible(boolean)
meth public void setPrototypeDisplayValue({javax.swing.JComboBox%0})
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=true, java.lang.String description="The display prototype value, used to compute display width and height.", java.lang.String[] enumerationValues=[])
meth public void setRenderer(javax.swing.ListCellRenderer<? super {javax.swing.JComboBox%0}>)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=true, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="The renderer that paints the item selected in the list.", java.lang.String[] enumerationValues=[])
meth public void setSelectedIndex(int)
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=true, boolean required=false, boolean visualUpdate=false, java.lang.String description="The item at index is selected.", java.lang.String[] enumerationValues=[])
meth public void setSelectedItem(java.lang.Object)
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=true, boolean required=false, boolean visualUpdate=false, java.lang.String description="Sets the selected item in the JComboBox.", java.lang.String[] enumerationValues=[])
meth public void setUI(javax.swing.plaf.ComboBoxUI)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=true, boolean preferred=false, boolean required=false, boolean visualUpdate=true, java.lang.String description="The UI object that implements the Component's LookAndFeel.", java.lang.String[] enumerationValues=[])
meth public void showPopup()
meth public void updateUI()
meth public {javax.swing.JComboBox%0} getItemAt(int)
meth public {javax.swing.JComboBox%0} getPrototypeDisplayValue()
supr javax.swing.JComponent

CLSS public abstract javax.swing.JComponent
 anno 0 java.beans.JavaBean(java.lang.String defaultEventSet="", java.lang.String defaultProperty="UIClassID", java.lang.String description="")
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
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=true, boolean preferred=false, boolean required=false, boolean visualUpdate=true, java.lang.String description="The component's look and feel delegate.", java.lang.String[] enumerationValues=[])
meth public <%0 extends java.util.EventListener> {%%0}[] getListeners(java.lang.Class<{%%0}>)
meth public boolean contains(int,int)
meth public boolean getAutoscrolls()
meth public boolean getInheritsPopupMenu()
meth public boolean getVerifyInputWhenFocusTarget()
meth public boolean isDoubleBuffered()
meth public boolean isManagingFocus()
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="", java.lang.String[] enumerationValues=[])
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public boolean isOpaque()
meth public boolean isOptimizedDrawingEnabled()
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="", java.lang.String[] enumerationValues=[])
meth public boolean isPaintingTile()
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="", java.lang.String[] enumerationValues=[])
meth public boolean isRequestFocusEnabled()
meth public boolean isValidateRoot()
meth public boolean requestDefaultFocus()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public boolean requestFocus(boolean)
meth public boolean requestFocusInWindow()
meth public final boolean isPaintingForPrint()
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="", java.lang.String[] enumerationValues=[])
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
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="", java.lang.String[] enumerationValues=[])
meth public int getWidth()
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="", java.lang.String[] enumerationValues=[])
meth public int getX()
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="", java.lang.String[] enumerationValues=[])
meth public int getY()
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="", java.lang.String[] enumerationValues=[])
meth public java.awt.Component getNextFocusableComponent()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public java.awt.Component$BaselineResizeBehavior getBaselineResizeBehavior()
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="", java.lang.String[] enumerationValues=[])
meth public java.awt.Container getTopLevelAncestor()
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="", java.lang.String[] enumerationValues=[])
meth public java.awt.Dimension getMaximumSize()
meth public java.awt.Dimension getMinimumSize()
meth public java.awt.Dimension getPreferredSize()
meth public java.awt.Dimension getSize(java.awt.Dimension)
meth public java.awt.FontMetrics getFontMetrics(java.awt.Font)
meth public java.awt.Graphics getGraphics()
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="", java.lang.String[] enumerationValues=[])
meth public java.awt.Insets getInsets()
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=true, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="", java.lang.String[] enumerationValues=[])
meth public java.awt.Insets getInsets(java.awt.Insets)
meth public java.awt.Point getLocation(java.awt.Point)
meth public java.awt.Point getPopupLocation(java.awt.event.MouseEvent)
meth public java.awt.Point getToolTipLocation(java.awt.event.MouseEvent)
meth public java.awt.Rectangle getBounds(java.awt.Rectangle)
meth public java.awt.Rectangle getVisibleRect()
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="", java.lang.String[] enumerationValues=[])
meth public java.awt.event.ActionListener getActionForKeyStroke(javax.swing.KeyStroke)
meth public java.beans.VetoableChangeListener[] getVetoableChangeListeners()
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="", java.lang.String[] enumerationValues=[])
meth public java.lang.String getToolTipText()
meth public java.lang.String getToolTipText(java.awt.event.MouseEvent)
meth public java.lang.String getUIClassID()
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=true, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="UIClassID", java.lang.String[] enumerationValues=[])
meth public javax.swing.InputVerifier getInputVerifier()
meth public javax.swing.JPopupMenu getComponentPopupMenu()
meth public javax.swing.JRootPane getRootPane()
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="", java.lang.String[] enumerationValues=[])
meth public javax.swing.JToolTip createToolTip()
meth public javax.swing.KeyStroke[] getRegisteredKeyStrokes()
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="", java.lang.String[] enumerationValues=[])
meth public javax.swing.TransferHandler getTransferHandler()
meth public javax.swing.border.Border getBorder()
meth public javax.swing.event.AncestorListener[] getAncestorListeners()
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="", java.lang.String[] enumerationValues=[])
meth public javax.swing.plaf.ComponentUI getUI()
meth public static boolean isLightweightComponent(java.awt.Component)
meth public static java.util.Locale getDefaultLocale()
meth public static void setDefaultLocale(java.util.Locale)
meth public void addAncestorListener(javax.swing.event.AncestorListener)
meth public void addNotify()
meth public void addVetoableChangeListener(java.beans.VetoableChangeListener)
meth public void computeVisibleRect(java.awt.Rectangle)
meth public void disable()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public void enable()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public void firePropertyChange(java.lang.String,boolean,boolean)
meth public void firePropertyChange(java.lang.String,char,char)
meth public void firePropertyChange(java.lang.String,int,int)
meth public void grabFocus()
meth public void hide()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
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
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public void revalidate()
meth public void scrollRectToVisible(java.awt.Rectangle)
meth public void setAlignmentX(float)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="The preferred horizontal alignment of the component.", java.lang.String[] enumerationValues=[])
meth public void setAlignmentY(float)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="The preferred vertical alignment of the component.", java.lang.String[] enumerationValues=[])
meth public void setAutoscrolls(boolean)
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=true, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="Determines if this component automatically scrolls its contents when dragged.", java.lang.String[] enumerationValues=[])
meth public void setBackground(java.awt.Color)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=false, boolean preferred=true, boolean required=false, boolean visualUpdate=true, java.lang.String description="The background color of the component.", java.lang.String[] enumerationValues=[])
meth public void setBorder(javax.swing.border.Border)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=false, boolean preferred=true, boolean required=false, boolean visualUpdate=true, java.lang.String description="The component's border.", java.lang.String[] enumerationValues=[])
meth public void setComponentPopupMenu(javax.swing.JPopupMenu)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=false, boolean preferred=true, boolean required=false, boolean visualUpdate=false, java.lang.String description="Popup to show", java.lang.String[] enumerationValues=[])
meth public void setDebugGraphicsOptions(int)
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=true, boolean required=false, boolean visualUpdate=false, java.lang.String description="Diagnostic options for graphics operations.", java.lang.String[] enumerationValues=["DebugGraphics.NONE_OPTION", "DebugGraphics.LOG_OPTION", "DebugGraphics.FLASH_OPTION", "DebugGraphics.BUFFERED_OPTION"])
meth public void setDoubleBuffered(boolean)
meth public void setEnabled(boolean)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=true, boolean hidden=false, boolean preferred=true, boolean required=false, boolean visualUpdate=true, java.lang.String description="The enabled state of the component.", java.lang.String[] enumerationValues=[])
meth public void setFocusTraversalKeys(int,java.util.Set<? extends java.awt.AWTKeyStroke>)
meth public void setFont(java.awt.Font)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=false, boolean preferred=true, boolean required=false, boolean visualUpdate=true, java.lang.String description="The font for the component.", java.lang.String[] enumerationValues=[])
meth public void setForeground(java.awt.Color)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=false, boolean preferred=true, boolean required=false, boolean visualUpdate=true, java.lang.String description="The foreground color of the component.", java.lang.String[] enumerationValues=[])
meth public void setInheritsPopupMenu(boolean)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="Whether or not the JPopupMenu is inherited", java.lang.String[] enumerationValues=[])
meth public void setInputVerifier(javax.swing.InputVerifier)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="The component's input verifier.", java.lang.String[] enumerationValues=[])
meth public void setMaximumSize(java.awt.Dimension)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="The maximum size of the component.", java.lang.String[] enumerationValues=[])
meth public void setMinimumSize(java.awt.Dimension)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="The minimum size of the component.", java.lang.String[] enumerationValues=[])
meth public void setNextFocusableComponent(java.awt.Component)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public void setOpaque(boolean)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=true, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="The component's opacity", java.lang.String[] enumerationValues=[])
meth public void setPreferredSize(java.awt.Dimension)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=false, boolean preferred=true, boolean required=false, boolean visualUpdate=false, java.lang.String description="The preferred size of the component.", java.lang.String[] enumerationValues=[])
meth public void setRequestFocusEnabled(boolean)
meth public void setToolTipText(java.lang.String)
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=true, boolean required=false, boolean visualUpdate=false, java.lang.String description="The text to display in a tool tip.", java.lang.String[] enumerationValues=[])
meth public void setTransferHandler(javax.swing.TransferHandler)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=true, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="Mechanism for transfer of data to and from the component", java.lang.String[] enumerationValues=[])
meth public void setVerifyInputWhenFocusTarget(boolean)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="Whether the Component verifies input before accepting focus.", java.lang.String[] enumerationValues=[])
meth public void setVisible(boolean)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=true, boolean preferred=false, boolean required=false, boolean visualUpdate=true, java.lang.String description="", java.lang.String[] enumerationValues=[])
meth public void unregisterKeyboardAction(javax.swing.KeyStroke)
meth public void update(java.awt.Graphics)
meth public void updateUI()
supr java.awt.Container

CLSS public javax.swing.JPanel
 anno 0 java.beans.JavaBean(java.lang.String defaultEventSet="", java.lang.String defaultProperty="UI", java.lang.String description="A generic lightweight container.")
cons public init()
cons public init(boolean)
cons public init(java.awt.LayoutManager)
cons public init(java.awt.LayoutManager,boolean)
innr protected AccessibleJPanel
intf javax.accessibility.Accessible
meth protected java.lang.String paramString()
meth public java.lang.String getUIClassID()
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=true, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="A string that specifies the name of the L&F class.", java.lang.String[] enumerationValues=[])
meth public javax.accessibility.AccessibleContext getAccessibleContext()
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="", java.lang.String[] enumerationValues=[])
meth public javax.swing.plaf.PanelUI getUI()
meth public void setUI(javax.swing.plaf.PanelUI)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=true, boolean preferred=false, boolean required=false, boolean visualUpdate=true, java.lang.String description="The UI object that implements the Component's LookAndFeel.", java.lang.String[] enumerationValues=[])
meth public void updateUI()
supr javax.swing.JComponent

CLSS public javax.swing.JTabbedPane
 anno 0 java.beans.JavaBean(java.lang.String defaultEventSet="", java.lang.String defaultProperty="UI", java.lang.String description="A component which provides a tab folder metaphor for displaying one component from a set of components.")
cons public init()
cons public init(int)
cons public init(int,int)
fld protected int tabPlacement
fld protected javax.swing.SingleSelectionModel model
fld protected javax.swing.event.ChangeEvent changeEvent
fld protected javax.swing.event.ChangeListener changeListener
fld public final static int SCROLL_TAB_LAYOUT = 1
fld public final static int WRAP_TAB_LAYOUT = 0
innr protected AccessibleJTabbedPane
innr protected ModelListener
intf java.io.Serializable
intf javax.accessibility.Accessible
intf javax.swing.SwingConstants
meth protected java.lang.String paramString()
meth protected javax.swing.event.ChangeListener createChangeListener()
meth protected void fireStateChanged()
meth public boolean isEnabledAt(int)
meth public int getDisplayedMnemonicIndexAt(int)
meth public int getMnemonicAt(int)
meth public int getSelectedIndex()
meth public int getTabCount()
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="", java.lang.String[] enumerationValues=[])
meth public int getTabLayoutPolicy()
meth public int getTabPlacement()
meth public int getTabRunCount()
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="", java.lang.String[] enumerationValues=[])
meth public int indexAtLocation(int,int)
meth public int indexOfComponent(java.awt.Component)
meth public int indexOfTab(java.lang.String)
meth public int indexOfTab(javax.swing.Icon)
meth public int indexOfTabComponent(java.awt.Component)
meth public java.awt.Color getBackgroundAt(int)
meth public java.awt.Color getForegroundAt(int)
meth public java.awt.Component add(java.awt.Component)
meth public java.awt.Component add(java.awt.Component,int)
meth public java.awt.Component add(java.lang.String,java.awt.Component)
meth public java.awt.Component getComponentAt(int)
meth public java.awt.Component getSelectedComponent()
meth public java.awt.Component getTabComponentAt(int)
meth public java.awt.Rectangle getBoundsAt(int)
meth public java.lang.String getTitleAt(int)
meth public java.lang.String getToolTipText(java.awt.event.MouseEvent)
meth public java.lang.String getToolTipTextAt(int)
meth public java.lang.String getUIClassID()
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="", java.lang.String[] enumerationValues=[])
meth public javax.accessibility.AccessibleContext getAccessibleContext()
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="", java.lang.String[] enumerationValues=[])
meth public javax.swing.Icon getDisabledIconAt(int)
meth public javax.swing.Icon getIconAt(int)
meth public javax.swing.SingleSelectionModel getModel()
meth public javax.swing.event.ChangeListener[] getChangeListeners()
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="", java.lang.String[] enumerationValues=[])
meth public javax.swing.plaf.TabbedPaneUI getUI()
meth public void add(java.awt.Component,java.lang.Object)
meth public void add(java.awt.Component,java.lang.Object,int)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void addTab(java.lang.String,java.awt.Component)
meth public void addTab(java.lang.String,javax.swing.Icon,java.awt.Component)
meth public void addTab(java.lang.String,javax.swing.Icon,java.awt.Component,java.lang.String)
meth public void insertTab(java.lang.String,javax.swing.Icon,java.awt.Component,java.lang.String,int)
meth public void remove(int)
meth public void remove(java.awt.Component)
meth public void removeAll()
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void removeTabAt(int)
meth public void setBackgroundAt(int,java.awt.Color)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=false, boolean preferred=true, boolean required=false, boolean visualUpdate=true, java.lang.String description="The background color at the specified tab index.", java.lang.String[] enumerationValues=[])
meth public void setComponentAt(int,java.awt.Component)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=true, java.lang.String description="The component at the specified tab index.", java.lang.String[] enumerationValues=[])
meth public void setDisabledIconAt(int,javax.swing.Icon)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=false, boolean preferred=true, boolean required=false, boolean visualUpdate=true, java.lang.String description="The disabled icon at the specified tab index.", java.lang.String[] enumerationValues=[])
meth public void setDisplayedMnemonicIndexAt(int,int)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=true, java.lang.String description="the index into the String to draw the keyboard character mnemonic at", java.lang.String[] enumerationValues=[])
meth public void setEnabledAt(int,boolean)
meth public void setForegroundAt(int,java.awt.Color)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=false, boolean preferred=true, boolean required=false, boolean visualUpdate=true, java.lang.String description="The foreground color at the specified tab index.", java.lang.String[] enumerationValues=[])
meth public void setIconAt(int,javax.swing.Icon)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=false, boolean preferred=true, boolean required=false, boolean visualUpdate=true, java.lang.String description="The icon at the specified tab index.", java.lang.String[] enumerationValues=[])
meth public void setMnemonicAt(int,int)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=true, java.lang.String description="The keyboard mnenmonic, as a KeyEvent VK constant, for the specified tab", java.lang.String[] enumerationValues=[])
meth public void setModel(javax.swing.SingleSelectionModel)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=false, boolean preferred=false, boolean required=false, boolean visualUpdate=false, java.lang.String description="The tabbedpane's SingleSelectionModel.", java.lang.String[] enumerationValues=[])
meth public void setSelectedComponent(java.awt.Component)
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=true, boolean required=false, boolean visualUpdate=false, java.lang.String description="The tabbedpane's selected component.", java.lang.String[] enumerationValues=[])
meth public void setSelectedIndex(int)
 anno 0 java.beans.BeanProperty(boolean bound=false, boolean expert=false, boolean hidden=false, boolean preferred=true, boolean required=false, boolean visualUpdate=false, java.lang.String description="The tabbedpane's selected tab index.", java.lang.String[] enumerationValues=[])
meth public void setTabComponentAt(int,java.awt.Component)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=false, boolean preferred=true, boolean required=false, boolean visualUpdate=true, java.lang.String description="The tab component at the specified tab index.", java.lang.String[] enumerationValues=[])
meth public void setTabLayoutPolicy(int)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=false, boolean preferred=true, boolean required=false, boolean visualUpdate=true, java.lang.String description="The tabbedpane's policy for laying out the tabs", java.lang.String[] enumerationValues=["JTabbedPane.WRAP_TAB_LAYOUT", "JTabbedPane.SCROLL_TAB_LAYOUT"])
meth public void setTabPlacement(int)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=false, boolean preferred=true, boolean required=false, boolean visualUpdate=true, java.lang.String description="The tabbedpane's tab placement.", java.lang.String[] enumerationValues=["JTabbedPane.TOP", "JTabbedPane.LEFT", "JTabbedPane.BOTTOM", "JTabbedPane.RIGHT"])
meth public void setTitleAt(int,java.lang.String)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=false, boolean preferred=true, boolean required=false, boolean visualUpdate=true, java.lang.String description="The title at the specified tab index.", java.lang.String[] enumerationValues=[])
meth public void setToolTipTextAt(int,java.lang.String)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=false, boolean preferred=true, boolean required=false, boolean visualUpdate=false, java.lang.String description="The tooltip text at the specified tab index.", java.lang.String[] enumerationValues=[])
meth public void setUI(javax.swing.plaf.TabbedPaneUI)
 anno 0 java.beans.BeanProperty(boolean bound=true, boolean expert=false, boolean hidden=true, boolean preferred=false, boolean required=false, boolean visualUpdate=true, java.lang.String description="The UI object that implements the tabbedpane's LookAndFeel", java.lang.String[] enumerationValues=[])
meth public void updateUI()
supr javax.swing.JComponent

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

CLSS public abstract interface !annotation javax.swing.SwingContainer
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean value()
meth public abstract !hasdefault java.lang.String delegate()

CLSS public abstract interface javax.swing.event.ListDataListener
intf java.util.EventListener
meth public abstract void contentsChanged(javax.swing.event.ListDataEvent)
meth public abstract void intervalAdded(javax.swing.event.ListDataEvent)
meth public abstract void intervalRemoved(javax.swing.event.ListDataEvent)

CLSS public javax.swing.text.DocumentFilter
cons public init()
innr public abstract static FilterBypass
meth public void insertString(javax.swing.text.DocumentFilter$FilterBypass,int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth public void remove(javax.swing.text.DocumentFilter$FilterBypass,int,int) throws javax.swing.text.BadLocationException
meth public void replace(javax.swing.text.DocumentFilter$FilterBypass,int,int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
supr java.lang.Object

CLSS public org.netbeans.modules.glassfish.common.AdminAuthenticator
cons public init()
meth protected java.net.PasswordAuthentication getPasswordAuthentication()
supr java.net.Authenticator
hfds TIMEOUT,displayed,lastTry
hcls PasswordPanel

CLSS public abstract org.netbeans.modules.glassfish.common.BasicTask<%0 extends java.lang.Object>
cons protected !varargs init(org.netbeans.modules.glassfish.common.GlassfishInstance,org.netbeans.modules.glassfish.tooling.TaskStateListener[])
fld protected java.lang.String instanceName
fld protected org.netbeans.modules.glassfish.tooling.TaskStateListener[] stateListener
fld protected volatile java.lang.Thread taskThread
fld public final static int DELAY = 250
fld public final static int PORT_CHECK_IDLE = 500
fld public final static int RESTART_DELAY = 5000
fld public final static int START_ADMIN_PORT_TIMEOUT = 120000
fld public final static int START_TIMEOUT = 300000
fld public final static int STOP_TIMEOUT = 180000
fld public final static java.util.concurrent.TimeUnit TIMEUNIT
innr protected static ShutdownStateListener
innr protected static StartStateListener
innr protected static StateChange
intf java.util.concurrent.Callable<{org.netbeans.modules.glassfish.common.BasicTask%0}>
meth protected !varargs final org.netbeans.modules.glassfish.tooling.TaskState fireOperationStateChanged(org.netbeans.modules.glassfish.tooling.TaskState,org.netbeans.modules.glassfish.tooling.TaskEvent,java.lang.String,java.lang.String[])
meth protected org.netbeans.modules.glassfish.common.BasicTask$ShutdownStateListener prepareShutdownMonitoring()
meth protected org.netbeans.modules.glassfish.common.BasicTask$StartStateListener forceStartMonitoring(boolean)
meth protected org.netbeans.modules.glassfish.common.BasicTask$StartStateListener prepareStartMonitoring(boolean)
meth protected org.netbeans.modules.glassfish.common.BasicTask$StateChange waitShutDown()
meth protected org.netbeans.modules.glassfish.common.BasicTask$StateChange waitStartUp(boolean,boolean)
meth protected void clearTaskThread()
meth protected void setTaskThread()
meth public abstract {org.netbeans.modules.glassfish.common.BasicTask%0} call()
supr java.lang.Object
hfds LOGGER,instance

CLSS protected static org.netbeans.modules.glassfish.common.BasicTask$ShutdownStateListener
 outer org.netbeans.modules.glassfish.common.BasicTask
cons protected init()
meth public void currentState(org.netbeans.modules.glassfish.tooling.data.GlassFishServer,org.netbeans.modules.glassfish.tooling.GlassFishStatus,org.netbeans.modules.glassfish.tooling.data.GlassFishStatusTask)
supr org.netbeans.modules.glassfish.common.status.WakeUpStateListener

CLSS protected static org.netbeans.modules.glassfish.common.BasicTask$StartStateListener
 outer org.netbeans.modules.glassfish.common.BasicTask
cons protected init(boolean)
meth public void currentState(org.netbeans.modules.glassfish.tooling.data.GlassFishServer,org.netbeans.modules.glassfish.tooling.GlassFishStatus,org.netbeans.modules.glassfish.tooling.data.GlassFishStatusTask)
supr org.netbeans.modules.glassfish.common.status.WakeUpStateListener
hfds process,profile

CLSS protected static org.netbeans.modules.glassfish.common.BasicTask$StateChange
 outer org.netbeans.modules.glassfish.common.BasicTask
cons protected !varargs init(org.netbeans.modules.glassfish.common.BasicTask<?>,org.netbeans.modules.glassfish.tooling.TaskState,org.netbeans.modules.glassfish.tooling.TaskEvent,java.lang.String,java.lang.String[])
cons protected init(org.netbeans.modules.glassfish.common.BasicTask<?>,org.netbeans.modules.glassfish.tooling.TaskState,org.netbeans.modules.glassfish.tooling.TaskEvent,java.lang.String)
meth protected org.netbeans.modules.glassfish.tooling.TaskState fireOperationStateChanged()
supr java.lang.Object
hfds event,msgArgs,msgKey,result,task

CLSS public org.netbeans.modules.glassfish.common.CommonServerSupport
intf org.netbeans.modules.glassfish.common.nodes.actions.RefreshModulesCookie
intf org.netbeans.modules.glassfish.spi.GlassfishModule3
meth public boolean isRemote()
meth public boolean isRestfulLogAccessSupported()
meth public boolean isWritable()
meth public boolean supportsRestartInDebug()
meth public final org.openide.util.RequestProcessor$Task refresh()
meth public int getAdminPortNumber()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public int getHttpPortNumber()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public java.lang.String getAdminPort()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public java.lang.String getDeployerUri()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public java.lang.String getDisplayName()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public java.lang.String getDomainName()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public java.lang.String getDomainsRoot()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public java.lang.String getGlassfishRoot()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public java.lang.String getHostName()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public java.lang.String getHttpPort()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public java.lang.String getInstallRoot()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public java.lang.String getPassword()
meth public java.lang.String getResourcesXmlName()
meth public java.lang.String getUserName()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public java.lang.String setEnvironmentProperty(java.lang.String,java.lang.String,boolean)
meth public java.util.Map<java.lang.String,java.lang.String> getInstanceProperties()
meth public java.util.Map<java.lang.String,java.util.List<org.netbeans.modules.glassfish.spi.AppDesc>> getApplications(java.lang.String)
meth public java.util.Map<java.lang.String,org.netbeans.modules.glassfish.spi.ResourceDesc> getResourcesMap(java.lang.String)
meth public java.util.concurrent.Future<org.netbeans.modules.glassfish.tooling.TaskState> killServer(org.netbeans.modules.glassfish.tooling.TaskStateListener)
meth public java.util.concurrent.Future<org.netbeans.modules.glassfish.tooling.TaskState> restartServer(org.netbeans.modules.glassfish.tooling.TaskStateListener)
meth public java.util.concurrent.Future<org.netbeans.modules.glassfish.tooling.TaskState> startServer(org.netbeans.modules.glassfish.tooling.TaskStateListener,org.netbeans.modules.glassfish.spi.GlassfishModule$ServerState)
meth public java.util.concurrent.Future<org.netbeans.modules.glassfish.tooling.TaskState> stopServer(org.netbeans.modules.glassfish.tooling.TaskStateListener)
meth public java.util.concurrent.Future<org.netbeans.modules.glassfish.tooling.admin.ResultString> deploy(org.netbeans.modules.glassfish.tooling.TaskStateListener,java.io.File,java.lang.String)
meth public java.util.concurrent.Future<org.netbeans.modules.glassfish.tooling.admin.ResultString> deploy(org.netbeans.modules.glassfish.tooling.TaskStateListener,java.io.File,java.lang.String,java.lang.String)
meth public java.util.concurrent.Future<org.netbeans.modules.glassfish.tooling.admin.ResultString> deploy(org.netbeans.modules.glassfish.tooling.TaskStateListener,java.io.File,java.lang.String,java.lang.String,java.util.Map<java.lang.String,java.lang.String>)
meth public java.util.concurrent.Future<org.netbeans.modules.glassfish.tooling.admin.ResultString> deploy(org.netbeans.modules.glassfish.tooling.TaskStateListener,java.io.File,java.lang.String,java.lang.String,java.util.Map<java.lang.String,java.lang.String>,java.io.File[])
meth public java.util.concurrent.Future<org.netbeans.modules.glassfish.tooling.admin.ResultString> disable(org.netbeans.modules.glassfish.tooling.TaskStateListener,java.lang.String)
meth public java.util.concurrent.Future<org.netbeans.modules.glassfish.tooling.admin.ResultString> enable(org.netbeans.modules.glassfish.tooling.TaskStateListener,java.lang.String)
meth public java.util.concurrent.Future<org.netbeans.modules.glassfish.tooling.admin.ResultString> redeploy(org.netbeans.modules.glassfish.tooling.TaskStateListener,java.lang.String,boolean)
meth public java.util.concurrent.Future<org.netbeans.modules.glassfish.tooling.admin.ResultString> redeploy(org.netbeans.modules.glassfish.tooling.TaskStateListener,java.lang.String,java.lang.String,boolean)
meth public java.util.concurrent.Future<org.netbeans.modules.glassfish.tooling.admin.ResultString> redeploy(org.netbeans.modules.glassfish.tooling.TaskStateListener,java.lang.String,java.lang.String,java.io.File[],boolean)
meth public java.util.concurrent.Future<org.netbeans.modules.glassfish.tooling.admin.ResultString> restartServer(int,boolean,org.netbeans.modules.glassfish.tooling.TaskStateListener[])
meth public java.util.concurrent.Future<org.netbeans.modules.glassfish.tooling.admin.ResultString> undeploy(org.netbeans.modules.glassfish.tooling.TaskStateListener,java.lang.String)
meth public org.netbeans.modules.glassfish.common.GlassfishInstance getInstance()
meth public org.netbeans.modules.glassfish.common.GlassfishInstanceProvider getInstanceProvider()
meth public org.netbeans.modules.glassfish.spi.AppDesc[] getModuleList(java.lang.String)
meth public org.netbeans.modules.glassfish.spi.CommandFactory getCommandFactory()
meth public org.netbeans.modules.glassfish.spi.GlassfishModule$ServerState getServerState()
meth public org.openide.util.RequestProcessor$Task refresh(java.lang.String,java.lang.String)
meth public static boolean isRunning(java.lang.String,int,java.lang.String)
meth public static boolean isRunning(java.lang.String,int,java.lang.String,int)
meth public static void displayPopUpMessage(org.netbeans.modules.glassfish.common.CommonServerSupport,java.lang.String)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void setServerState(org.netbeans.modules.glassfish.spi.GlassfishModule$ServerState)
supr java.lang.Object
hfds FAILED_HTTP_HOST,LOCALHOST,LOGGER,RP,WAIT_TASK_TO_DIE_MAX,WAIT_TASK_TO_DIE_SLEEP,changeSupport,instance,instanceFO,isRemote,latestWarningDisplayTime,localStartProcess,refreshRunning,serverState,startTask,startedByIde,stateMonitor,stopDisabled
hcls KillOperationStateListener,LocationsTaskStateListener,StartOperationStateListener,StopOperationStateListener

CLSS public org.netbeans.modules.glassfish.common.CreateDomain
cons public init(java.lang.String,java.lang.String,java.io.File,java.util.Map<java.lang.String,java.lang.String>,org.netbeans.modules.glassfish.common.GlassfishInstanceProvider,boolean,boolean,java.lang.String)
meth public int getAdminPort()
meth public int getHttpPort()
meth public void run()
supr java.lang.Thread
hfds PORTBASE,gip,installRootKey,ip,map,platformLocation,pword,register,uname
hcls PDCancel

CLSS public org.netbeans.modules.glassfish.common.EnableComet
cons public init(org.netbeans.modules.glassfish.common.GlassfishInstance)
intf java.lang.Runnable
meth public void run()
supr java.lang.Object
hfds LOGGER,instance

CLSS public org.netbeans.modules.glassfish.common.GlassFishExecutors
cons public init()
meth public static java.util.concurrent.ExecutorService fetchLogExecutor()
supr java.lang.Object
hfds FETCH_LOG_EXECUTOR_POOL_KEEPALIVE_TIME,FETCH_LOG_EXECUTOR_POOL_MAX_SIZE,FETCH_LOG_EXECUTOR_POOL_MIN_SIZE,THREAD_GROUP_NAME_LOG,THREAD_GROUP_NAME_STAT,THREAD_GROUP_NAME_TOP,fetchLogExecutor,tgLog,tgStat,tgTop
hcls FetchLogThreadFactory,StatusThreadFactory

CLSS public final !enum org.netbeans.modules.glassfish.common.GlassFishJvmMode
fld public final static int length
fld public final static org.netbeans.modules.glassfish.common.GlassFishJvmMode DEBUG
fld public final static org.netbeans.modules.glassfish.common.GlassFishJvmMode NORMAL
fld public final static org.netbeans.modules.glassfish.common.GlassFishJvmMode PROFILE
meth public java.lang.String toString()
meth public static org.netbeans.modules.glassfish.common.GlassFishJvmMode toValue(java.lang.String)
meth public static org.netbeans.modules.glassfish.common.GlassFishJvmMode valueOf(java.lang.String)
meth public static org.netbeans.modules.glassfish.common.GlassFishJvmMode[] values()
supr java.lang.Enum<org.netbeans.modules.glassfish.common.GlassFishJvmMode>
hfds DEBUG_STR,LOGGER,NORMAL_STR,PROFILE_STR,stringValuesMap

CLSS public org.netbeans.modules.glassfish.common.GlassFishLogger
cons public init()
meth public static java.util.logging.Logger get(java.lang.Class)
supr java.lang.Object

CLSS public org.netbeans.modules.glassfish.common.GlassFishSettings
cons public init()
meth public static boolean getGf312WarningShowAgain()
meth public static boolean getGfKillWarningShowAgain()
meth public static boolean getGfShowPasswordInPropertiesForm()
meth public static boolean showWindowSystem()
meth public static void setGf312WarningShowAgain(boolean)
meth public static void setGfKillWarningShowAgain(boolean)
meth public static void setGfShowPasswordInPropertiesForm(boolean)
supr java.lang.Object
hfds LBL_GF312_WARNING_SHOW_AGAIN,LBL_GF_KILL_SHOW_AGAIN,LBL_GF_SHOW_PASSWORD_IN_PROPERTIES_FORM,NB_PREFERENCES_NODE

CLSS public org.netbeans.modules.glassfish.common.GlassFishState
cons public init()
innr public final static !enum Mode
meth public static boolean canStart(org.netbeans.modules.glassfish.tooling.data.GlassFishServer)
meth public static boolean isOffline(org.netbeans.modules.glassfish.tooling.data.GlassFishServer)
meth public static boolean isOnline(org.netbeans.modules.glassfish.tooling.data.GlassFishServer)
meth public static boolean monitor(org.netbeans.modules.glassfish.tooling.data.GlassFishServer)
meth public static org.netbeans.modules.glassfish.tooling.data.GlassFishServerStatus getStatus(org.netbeans.modules.glassfish.tooling.data.GlassFishServer)
meth public static org.netbeans.modules.glassfish.tooling.data.GlassFishServerStatus getStatus(org.netbeans.modules.glassfish.tooling.data.GlassFishServer,long)
supr java.lang.Object
hfds INIT_MONITORING_TIMEOUT,LOGGER

CLSS public final static !enum org.netbeans.modules.glassfish.common.GlassFishState$Mode
 outer org.netbeans.modules.glassfish.common.GlassFishState
fld public final static org.netbeans.modules.glassfish.common.GlassFishState$Mode DEFAULT
fld public final static org.netbeans.modules.glassfish.common.GlassFishState$Mode REFRESH
fld public final static org.netbeans.modules.glassfish.common.GlassFishState$Mode STARTUP
meth public java.lang.String toString()
meth public static org.netbeans.modules.glassfish.common.GlassFishState$Mode valueOf(java.lang.String)
meth public static org.netbeans.modules.glassfish.common.GlassFishState$Mode[] values()
supr java.lang.Enum<org.netbeans.modules.glassfish.common.GlassFishState$Mode>

CLSS public org.netbeans.modules.glassfish.common.GlassfishInstance
fld public final static int DEFAULT_ADMIN_PORT = 4848
fld public final static int DEFAULT_DEBUG_PORT = 9009
fld public final static int DEFAULT_HTTPS_PORT = 8181
fld public final static int DEFAULT_HTTP_PORT = 8080
fld public final static java.lang.String DEFAULT_ADMIN_NAME = "admin"
fld public final static java.lang.String DEFAULT_ADMIN_PASSWORD = ""
fld public final static java.lang.String DEFAULT_DOMAINS_FOLDER = "domains"
fld public final static java.lang.String DEFAULT_DOMAIN_NAME = "domain1"
fld public final static java.lang.String DEFAULT_HOST_NAME = "localhost"
fld public final static java.lang.String OLD_DEFAULT_ADMIN_PASSWORD = "adminadmin"
innr public Props
intf org.netbeans.modules.glassfish.tooling.data.GlassFishServer
intf org.netbeans.spi.server.ServerInstanceImplementation
intf org.openide.util.Lookup$Provider
intf org.openide.util.LookupListener
meth public boolean equals(java.lang.Object)
meth public boolean isProcessRunning()
meth public boolean isRemote()
meth public boolean isRemovable()
meth public final org.netbeans.modules.glassfish.common.CommonServerSupport getCommonSupport()
meth public final org.netbeans.modules.glassfish.spi.GlassfishModule$ServerState getServerState()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public int getAdminPort()
meth public int getDebugPort()
meth public int getPort()
meth public int hashCode()
meth public java.lang.Process getProcess()
meth public java.lang.String getAdminPassword()
meth public java.lang.String getAdminUser()
meth public java.lang.String getDeployerUri()
meth public java.lang.String getDisplayName()
meth public java.lang.String getDomainName()
meth public java.lang.String getDomainsFolder()
meth public java.lang.String getDomainsRoot()
meth public java.lang.String getGlassfishRoot()
meth public java.lang.String getHost()
meth public java.lang.String getHttpAdminPort()
meth public java.lang.String getHttpPort()
meth public java.lang.String getInstallRoot()
meth public java.lang.String getJavaHome()
meth public java.lang.String getJvmModeAsString()
meth public java.lang.String getName()
meth public java.lang.String getPassword()
meth public java.lang.String getProperty(java.lang.String)
meth public java.lang.String getServerDisplayName()
meth public java.lang.String getServerHome()
meth public java.lang.String getServerRoot()
meth public java.lang.String getTarget()
meth public java.lang.String getUrl()
meth public java.lang.String getUserName()
meth public java.lang.String putProperty(java.lang.String,java.lang.String)
meth public java.lang.String removeProperty(java.lang.String)
meth public java.lang.String setDomainsFolder(java.lang.String)
meth public java.lang.String setTarget(java.lang.String)
meth public java.util.Map<java.lang.String,java.lang.String> getProperties()
meth public javax.swing.JComponent getCustomizer()
meth public org.netbeans.api.java.platform.JavaPlatform getJavaPlatform()
meth public org.netbeans.api.server.ServerInstance getCommonInstance()
meth public org.netbeans.modules.glassfish.common.GlassFishJvmMode getJvmMode()
meth public org.netbeans.modules.glassfish.common.GlassfishInstanceProvider getInstanceProvider()
meth public org.netbeans.modules.glassfish.common.parser.DomainXMLChangeListener getDomainXMLChangeListener()
meth public org.netbeans.modules.glassfish.tooling.data.GlassFishAdminInterface getAdminInterface()
meth public org.netbeans.modules.glassfish.tooling.data.GlassFishVersion getVersion()
meth public org.openide.nodes.Node getBasicNode()
meth public org.openide.nodes.Node getFullNode()
meth public org.openide.util.Lookup getLookup()
meth public static java.lang.String getPasswordFromKeyring(java.lang.String,java.lang.String)
meth public static java.lang.String passwordKey(java.lang.String,java.lang.String)
meth public static org.netbeans.modules.glassfish.common.GlassfishInstance create(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,int,int,java.lang.String,java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.glassfish.common.GlassfishInstanceProvider)
meth public static org.netbeans.modules.glassfish.common.GlassfishInstance create(java.util.Map<java.lang.String,java.lang.String>,org.netbeans.modules.glassfish.common.GlassfishInstanceProvider)
meth public static org.netbeans.modules.glassfish.common.GlassfishInstance create(java.util.Map<java.lang.String,java.lang.String>,org.netbeans.modules.glassfish.common.GlassfishInstanceProvider,boolean)
meth public static org.netbeans.modules.glassfish.common.GlassfishInstance readInstanceFromFile(org.openide.filesystems.FileObject,boolean) throws java.io.IOException
meth public static void writeInstanceToFile(org.netbeans.modules.glassfish.common.GlassfishInstance) throws java.io.IOException
meth public void remove()
meth public void resetProcess()
meth public void resultChanged(org.openide.util.LookupEvent)
meth public void setAdminPassword(java.lang.String)
meth public void setAdminPort(int)
meth public void setAdminPort(java.lang.String)
meth public void setAdminUser(java.lang.String)
meth public void setHost(java.lang.String)
meth public void setHttpPort(int)
meth public void setHttpPort(java.lang.String)
meth public void setJavaHome(java.lang.String)
meth public void setProcess(java.lang.Process)
meth public void setProperties(org.netbeans.modules.glassfish.common.GlassfishInstance$Props)
supr java.lang.Object
hfds INSTANCE_FO_ATTR,KEYRING_IDENT_SEPARATOR,KEYRING_NAME_SEPARATOR,KEYRING_NAME_SPACE,LOGGER,LOWEST_USER_PORT,commonInstance,commonSupport,currentFactories,domainXMLListener,full,fullNode,ic,instanceProvider,localLookup,lookupResult,process,properties,removable,version

CLSS public org.netbeans.modules.glassfish.common.GlassfishInstance$Props
 outer org.netbeans.modules.glassfish.common.GlassfishInstance
cons public init(java.util.Map<java.lang.String,java.lang.String>)
intf java.util.Map<java.lang.String,java.lang.String>
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public int hashCode()
meth public int size()
meth public java.lang.String get(java.lang.Object)
meth public java.lang.String put(java.lang.String,java.lang.String)
meth public java.lang.String remove(java.lang.Object)
meth public java.util.Collection<java.lang.String> values()
meth public java.util.Set<java.lang.String> keySet()
meth public java.util.Set<java.util.Map$Entry<java.lang.String,java.lang.String>> entrySet()
meth public void clear()
meth public void putAll(java.util.Map<? extends java.lang.String,? extends java.lang.String>)
supr java.lang.Object
hfds delegate

CLSS public final org.netbeans.modules.glassfish.common.GlassfishInstanceProvider
fld public final static java.lang.String EE6WC_DEPLOYER_FRAGMENT = "deployer:gfv3ee6wc"
fld public final static java.lang.String EE6_DEPLOYER_FRAGMENT = "deployer:gfv3ee6"
fld public final static java.lang.String EE7_DEPLOYER_FRAGMENT = "deployer:gfv4ee7"
fld public final static java.lang.String EE8_DEPLOYER_FRAGMENT = "deployer:gfv5ee8"
fld public final static java.lang.String GLASSFISH_AUTOREGISTERED_INSTANCE = "glassfish_autoregistered_instance"
fld public final static java.lang.String JAKARTAEE10_DEPLOYER_FRAGMENT = "deployer:gfv700ee10"
fld public final static java.lang.String JAKARTAEE8_DEPLOYER_FRAGMENT = "deployer:gfv510ee8"
fld public final static java.lang.String JAKARTAEE91_DEPLOYER_FRAGMENT = "deployer:gfv610ee9"
fld public final static java.lang.String JAKARTAEE9_DEPLOYER_FRAGMENT = "deployer:gfv6ee9"
fld public final static java.lang.String PRELUDE_DEPLOYER_FRAGMENT = "deployer:gfv3"
fld public final static java.util.Set<java.lang.String> activeRegistrationSet
fld public static java.lang.String EE6WC_DEFAULT_NAME
fld public static java.lang.String PRELUDE_DEFAULT_NAME
intf org.netbeans.spi.server.ServerInstanceProvider
intf org.openide.util.LookupListener
meth public <%0 extends java.lang.Object> java.util.List<{%%0}> getInstancesByCapability(java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} getInstanceByCapability(java.lang.String,java.lang.Class<{%%0}>)
meth public boolean hasServer(java.lang.String)
meth public boolean removeServerInstance(org.netbeans.modules.glassfish.common.GlassfishInstance)
meth public java.util.List<org.netbeans.api.server.ServerInstance> getInstances()
meth public org.netbeans.api.server.ServerInstance getInstance(java.lang.String)
meth public org.netbeans.modules.glassfish.common.GlassfishInstance getGlassfishInstance(java.lang.String)
meth public org.netbeans.modules.glassfish.spi.CommandFactory getCommandFactory()
meth public org.netbeans.spi.server.ServerInstanceImplementation getInternalInstance(java.lang.String)
meth public org.openide.util.Lookup getLookupFor(org.netbeans.api.server.ServerInstance)
meth public static boolean initialized()
meth public static org.netbeans.api.server.ServerInstance getInstanceByUri(java.lang.String)
meth public static org.netbeans.modules.glassfish.common.GlassfishInstance getGlassFishInstanceByUri(java.lang.String)
meth public static org.netbeans.modules.glassfish.common.GlassfishInstanceProvider getProvider()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void addServerInstance(org.netbeans.modules.glassfish.common.GlassfishInstance)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void resultChanged(org.openide.util.LookupEvent)
supr java.lang.Object
hfds AUTOINSTANCECOPIED,EE6WC_INSTANCES_PATH,EE6_INSTANCES_PATH,EE7_INSTANCES_PATH,EE8_INSTANCES_PATH,JAKARTAEE10_INSTANCES_PATH,JAKARTAEE8_INSTANCES_PATH,JAKARTAEE91_INSTANCES_PATH,JAKARTAEE9_INSTANCES_PATH,LOGGER,activeDisplayNames,cf,displayName,glassFishProvider,instanceMap,instancesDirNames,lookupResult,needsJdk6,noPasswordOptions,support,uriFragments

CLSS public org.netbeans.modules.glassfish.common.Installer
cons public init()
meth public void close()
supr org.openide.modules.ModuleInstall

CLSS public org.netbeans.modules.glassfish.common.KillTask
cons public !varargs init(org.netbeans.modules.glassfish.common.GlassfishInstance,org.netbeans.modules.glassfish.tooling.TaskStateListener[])
meth public org.netbeans.modules.glassfish.tooling.TaskState call()
supr org.netbeans.modules.glassfish.common.BasicTask<org.netbeans.modules.glassfish.tooling.TaskState>
hfds LOGGER

CLSS public org.netbeans.modules.glassfish.common.LogViewMgr
meth public !varargs void readInputStreams(java.util.List<org.netbeans.modules.glassfish.spi.Recognizer>,boolean,org.netbeans.modules.glassfish.common.GlassfishInstance,org.netbeans.modules.glassfish.tooling.server.FetchLog[])
meth public static org.netbeans.modules.glassfish.common.LogViewMgr getInstance(java.lang.String)
meth public static org.openide.windows.InputOutput getServerIO(java.lang.String)
meth public static void displayOutput(org.netbeans.modules.glassfish.common.GlassfishInstance,org.openide.util.Lookup)
meth public static void removeLog(org.netbeans.modules.glassfish.common.GlassfishInstance)
meth public void ensureActiveReader(java.util.List<org.netbeans.modules.glassfish.spi.Recognizer>,org.netbeans.modules.glassfish.tooling.server.FetchLog,org.netbeans.modules.glassfish.common.GlassfishInstance)
meth public void selectIO(boolean)
meth public void stopReaders()
meth public void write(java.lang.String,boolean)
meth public void write(java.lang.String,org.openide.windows.OutputListener,boolean,boolean)
supr java.lang.Object
hfds DELAY,LOGGER,LOG_BLUE,LOG_CYAN,LOG_GREEN,LOG_MAGENTA,LOG_RED,LOG_YELLOW,OUTPUT_WINDOW_TCID,VISIBILITY_CHECK_DELAY,colorPattern,colorTable,instances,io,ioWeakMap,lastVisibleCheck,localizedLevels,localizedSevere,localizedWarning,logBundleName,logLocale,outputTCRef,readers,serverInputStreams,setClosedMethod,strictFilter,uri,visibleCheck
hcls Filter,LogFileFilter,LogStateListener,LoggerRunnable,Message,StateFilter,StreamFilter

CLSS public org.netbeans.modules.glassfish.common.PartialCompletionException
cons public init(java.lang.String)
meth public java.lang.String getMessage()
supr java.lang.Exception
hfds failedUpdates

CLSS public org.netbeans.modules.glassfish.common.PortCollection
cons public init()
meth public int getAdminPort()
meth public int getHttpPort()
meth public int getHttpsPort()
meth public void setAdminPort(int)
meth public void setHttpPort(int)
meth public void setHttpsPort(int)
supr java.lang.Object
hfds adminPort,httpPort,httpsPort

CLSS public org.netbeans.modules.glassfish.common.ProcessCreationException
meth public java.lang.String getLocalizedMessage()
supr java.lang.Exception
hfds args,messageName

CLSS public org.netbeans.modules.glassfish.common.RegisteredDerbyServerImpl
cons public init()
intf org.netbeans.modules.glassfish.spi.RegisteredDerbyServer
meth public void initialize(java.lang.String)
meth public void start()
supr java.lang.Object

CLSS public org.netbeans.modules.glassfish.common.RestartTask
cons public !varargs init(org.netbeans.modules.glassfish.common.CommonServerSupport,org.netbeans.modules.glassfish.tooling.TaskStateListener[])
meth public org.netbeans.modules.glassfish.tooling.TaskState call()
meth public org.netbeans.modules.glassfish.tooling.TaskState call2()
supr org.netbeans.modules.glassfish.common.BasicTask<org.netbeans.modules.glassfish.tooling.TaskState>
hfds LOGGER,RESTART_DELAY,support

CLSS public final !enum org.netbeans.modules.glassfish.common.ServerDetails
fld public final static org.netbeans.modules.glassfish.common.ServerDetails GLASSFISH_SERVER_3
fld public final static org.netbeans.modules.glassfish.common.ServerDetails GLASSFISH_SERVER_3_0_1
fld public final static org.netbeans.modules.glassfish.common.ServerDetails GLASSFISH_SERVER_3_1
fld public final static org.netbeans.modules.glassfish.common.ServerDetails GLASSFISH_SERVER_3_1_1
fld public final static org.netbeans.modules.glassfish.common.ServerDetails GLASSFISH_SERVER_3_1_2
fld public final static org.netbeans.modules.glassfish.common.ServerDetails GLASSFISH_SERVER_3_1_2_2
fld public final static org.netbeans.modules.glassfish.common.ServerDetails GLASSFISH_SERVER_4_0
fld public final static org.netbeans.modules.glassfish.common.ServerDetails GLASSFISH_SERVER_4_0_1
fld public final static org.netbeans.modules.glassfish.common.ServerDetails GLASSFISH_SERVER_4_1
fld public final static org.netbeans.modules.glassfish.common.ServerDetails GLASSFISH_SERVER_4_1_1
fld public final static org.netbeans.modules.glassfish.common.ServerDetails GLASSFISH_SERVER_4_1_2
fld public final static org.netbeans.modules.glassfish.common.ServerDetails GLASSFISH_SERVER_5_0
fld public final static org.netbeans.modules.glassfish.common.ServerDetails GLASSFISH_SERVER_5_0_1
fld public final static org.netbeans.modules.glassfish.common.ServerDetails GLASSFISH_SERVER_5_1_0
fld public final static org.netbeans.modules.glassfish.common.ServerDetails GLASSFISH_SERVER_6
fld public final static org.netbeans.modules.glassfish.common.ServerDetails GLASSFISH_SERVER_6_1_0
fld public final static org.netbeans.modules.glassfish.common.ServerDetails GLASSFISH_SERVER_6_2_0
fld public final static org.netbeans.modules.glassfish.common.ServerDetails GLASSFISH_SERVER_6_2_1
fld public final static org.netbeans.modules.glassfish.common.ServerDetails GLASSFISH_SERVER_6_2_2
fld public final static org.netbeans.modules.glassfish.common.ServerDetails GLASSFISH_SERVER_6_2_3
fld public final static org.netbeans.modules.glassfish.common.ServerDetails GLASSFISH_SERVER_6_2_4
fld public final static org.netbeans.modules.glassfish.common.ServerDetails GLASSFISH_SERVER_6_2_5
fld public final static org.netbeans.modules.glassfish.common.ServerDetails GLASSFISH_SERVER_7_0_0
fld public final static org.netbeans.modules.glassfish.common.ServerDetails GLASSFISH_SERVER_7_0_1
fld public final static org.netbeans.modules.glassfish.common.ServerDetails GLASSFISH_SERVER_7_0_10
fld public final static org.netbeans.modules.glassfish.common.ServerDetails GLASSFISH_SERVER_7_0_11
fld public final static org.netbeans.modules.glassfish.common.ServerDetails GLASSFISH_SERVER_7_0_2
fld public final static org.netbeans.modules.glassfish.common.ServerDetails GLASSFISH_SERVER_7_0_3
fld public final static org.netbeans.modules.glassfish.common.ServerDetails GLASSFISH_SERVER_7_0_4
fld public final static org.netbeans.modules.glassfish.common.ServerDetails GLASSFISH_SERVER_7_0_5
fld public final static org.netbeans.modules.glassfish.common.ServerDetails GLASSFISH_SERVER_7_0_6
fld public final static org.netbeans.modules.glassfish.common.ServerDetails GLASSFISH_SERVER_7_0_7
fld public final static org.netbeans.modules.glassfish.common.ServerDetails GLASSFISH_SERVER_7_0_8
fld public final static org.netbeans.modules.glassfish.common.ServerDetails GLASSFISH_SERVER_7_0_9
meth public boolean isInstalledInDirectory(java.io.File)
meth public int getVersion()
meth public java.lang.String getDirectUrl()
meth public java.lang.String getIndirectUrl()
meth public java.lang.String getLicenseUrl()
meth public java.lang.String getUriFragment()
meth public java.lang.String toString()
meth public static int getVersionFromDomainXml(java.io.File)
meth public static int getVersionFromInstallDirectory(java.io.File)
meth public static org.netbeans.modules.glassfish.common.ServerDetails valueOf(java.lang.String)
meth public static org.netbeans.modules.glassfish.common.ServerDetails[] values()
meth public static org.openide.WizardDescriptor$InstantiatingIterator getInstantiatingIterator()
supr java.lang.Enum<org.netbeans.modules.glassfish.common.ServerDetails>
hfds directUrl,displayName,glassFishVersion,indirectUrl,licenseUrl,serverDetails,uriFragment
hcls DomainParser

CLSS public org.netbeans.modules.glassfish.common.SimpleIO
cons public init(java.lang.String,java.lang.Process)
innr public CancelAction
meth public !varargs void readInputStreams(java.io.InputStream[])
meth public void closeIO()
meth public void selectIO()
meth public void write(java.lang.String)
supr java.lang.Object
hfds DELAY,cancelAction,io,name,process
hcls IOReader

CLSS public org.netbeans.modules.glassfish.common.SimpleIO$CancelAction
 outer org.netbeans.modules.glassfish.common.SimpleIO
cons public init(org.netbeans.modules.glassfish.common.SimpleIO)
meth public boolean isEnabled()
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void updateEnabled()
supr javax.swing.AbstractAction
hfds ICON,PROP_ENABLED

CLSS public org.netbeans.modules.glassfish.common.StartTask
cons public !varargs init(org.netbeans.modules.glassfish.common.CommonServerSupport,java.util.List<org.netbeans.modules.glassfish.spi.Recognizer>,org.netbeans.modules.glassfish.spi.VMIntrospector,java.lang.String[],org.netbeans.modules.glassfish.tooling.TaskStateListener[])
cons public !varargs init(org.netbeans.modules.glassfish.common.CommonServerSupport,java.util.List<org.netbeans.modules.glassfish.spi.Recognizer>,org.netbeans.modules.glassfish.spi.VMIntrospector,org.netbeans.modules.glassfish.tooling.TaskStateListener[])
meth public org.netbeans.modules.glassfish.tooling.TaskState call()
supr org.netbeans.modules.glassfish.common.BasicTask<org.netbeans.modules.glassfish.tooling.TaskState>
hfds LOGGER,NODE_REFRESHER,jdkHome,jvmArgs,recognizers,support,vmi

CLSS public org.netbeans.modules.glassfish.common.StopProfilingTask
cons public init(org.netbeans.modules.glassfish.common.CommonServerSupport,org.netbeans.modules.glassfish.tooling.TaskStateListener)
meth public org.netbeans.modules.glassfish.tooling.TaskState call()
supr org.netbeans.modules.glassfish.common.BasicTask<org.netbeans.modules.glassfish.tooling.TaskState>
hfds support

CLSS public org.netbeans.modules.glassfish.common.StopTask
cons public !varargs init(org.netbeans.modules.glassfish.common.CommonServerSupport,org.netbeans.modules.glassfish.tooling.TaskStateListener[])
meth public org.netbeans.modules.glassfish.tooling.TaskState call()
supr org.netbeans.modules.glassfish.common.BasicTask<org.netbeans.modules.glassfish.tooling.TaskState>
hfds support

CLSS public abstract interface org.netbeans.modules.glassfish.common.nodes.actions.RefreshModulesCookie
intf org.openide.nodes.Node$Cookie
meth public abstract org.openide.util.RequestProcessor$Task refresh()
meth public abstract org.openide.util.RequestProcessor$Task refresh(java.lang.String,java.lang.String)

CLSS public org.netbeans.modules.glassfish.common.parser.DomainXMLChangeListener
cons public init(org.netbeans.modules.glassfish.common.GlassfishInstance,java.lang.String)
intf org.openide.filesystems.FileChangeListener
meth public static void registerListener(org.netbeans.modules.glassfish.common.GlassfishInstance)
meth public static void unregisterListener(org.netbeans.modules.glassfish.common.GlassfishInstance)
meth public void fileAttributeChanged(org.openide.filesystems.FileAttributeEvent)
meth public void fileChanged(org.openide.filesystems.FileEvent)
meth public void fileDataCreated(org.openide.filesystems.FileEvent)
meth public void fileDeleted(org.openide.filesystems.FileEvent)
meth public void fileFolderCreated(org.openide.filesystems.FileEvent)
meth public void fileRenamed(org.openide.filesystems.FileRenameEvent)
supr java.lang.Object
hfds LOGGER,instance,path

CLSS public org.netbeans.modules.glassfish.common.parser.JvmConfigReader
cons public init(java.util.List<java.lang.String>,java.util.Map<java.lang.String,java.lang.String>,java.util.Map<java.lang.String,java.lang.String>,java.util.Map<java.lang.String,java.lang.String>)
meth public org.netbeans.modules.glassfish.common.parser.TreeParser$NodeReader getConfigFinder()
meth public org.netbeans.modules.glassfish.common.parser.TreeParser$NodeReader getMonitoringFinder(java.io.File)
meth public org.netbeans.modules.glassfish.common.parser.TreeParser$NodeReader getServerFinder()
meth public void readAttributes(java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public void readCData(java.lang.String,char[],int,int) throws org.xml.sax.SAXException
supr org.netbeans.modules.glassfish.common.parser.TreeParser$NodeReader
hfds SERVER_NAME,argMap,optList,pattern,propMap,readJvmConfig,serverConfigName,varMap

CLSS public final org.netbeans.modules.glassfish.common.parser.TreeParser
innr public abstract static NodeReader
innr public static Path
meth public org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public static boolean readXml(java.io.File,java.util.List<org.netbeans.modules.glassfish.common.parser.TreeParser$Path>)
meth public void characters(char[],int,int) throws org.xml.sax.SAXException
meth public void endDocument() throws org.xml.sax.SAXException
meth public void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void startDocument() throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
supr org.xml.sax.helpers.DefaultHandler
hfds LOGGER,childNodeReader,depth,isFinerLoggable,isFinestLoggable,root,rover,skipping
hcls Node

CLSS public abstract static org.netbeans.modules.glassfish.common.parser.TreeParser$NodeReader
 outer org.netbeans.modules.glassfish.common.parser.TreeParser
cons public init()
meth public void endNode(java.lang.String) throws org.xml.sax.SAXException
meth public void readAttributes(java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public void readCData(java.lang.String,char[],int,int) throws org.xml.sax.SAXException
meth public void readChildren(java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
supr java.lang.Object

CLSS public static org.netbeans.modules.glassfish.common.parser.TreeParser$Path
 outer org.netbeans.modules.glassfish.common.parser.TreeParser
cons public init(java.lang.String)
cons public init(java.lang.String,org.netbeans.modules.glassfish.common.parser.TreeParser$NodeReader)
meth public java.lang.String getPath()
meth public java.lang.String toString()
meth public org.netbeans.modules.glassfish.common.parser.TreeParser$NodeReader getReader()
supr java.lang.Object
hfds path,reader

CLSS public abstract org.netbeans.modules.glassfish.common.status.BasicStateListener
cons public init()
intf org.netbeans.modules.glassfish.tooling.GlassFishStatusListener
meth public boolean isActive()
meth public void added()
meth public void removed()
supr java.lang.Object
hfds active

CLSS public abstract org.netbeans.modules.glassfish.common.status.WakeUpStateListener
cons public init()
meth protected void wakeUp()
meth public boolean isWakeUp()
meth public void error(org.netbeans.modules.glassfish.tooling.data.GlassFishServer,org.netbeans.modules.glassfish.tooling.data.GlassFishStatusTask)
meth public void newState(org.netbeans.modules.glassfish.tooling.data.GlassFishServer,org.netbeans.modules.glassfish.tooling.GlassFishStatus,org.netbeans.modules.glassfish.tooling.data.GlassFishStatusTask)
supr org.netbeans.modules.glassfish.common.status.BasicStateListener
hfds wakeUp

CLSS public org.netbeans.modules.glassfish.common.ui.AdminObjectCustomizer
cons public init()
meth protected java.lang.String getPrefix()
meth protected java.util.List<java.awt.Component> getDataComponents()
supr org.netbeans.modules.glassfish.common.ui.BasePanel
hfds resourceEnabledCB,resourceTypeField,resourceTypeLabel

CLSS public abstract org.netbeans.modules.glassfish.common.ui.BasePanel
cons public init()
innr public static Error
meth protected abstract java.lang.String getPrefix()
meth protected abstract java.util.List<java.awt.Component> getDataComponents()
meth public final java.util.Map<java.lang.String,java.lang.String> getData()
meth public final void initializeUI()
meth public void initializeData(java.lang.String,java.util.Map<java.lang.String,java.lang.String>)
supr javax.swing.JPanel
hcls AttributedPropertyTableModel,ButtonSetter,ComboBoxSetter,DataTableModel,NameValueTableModel,TableSetter,TextFieldSetter

CLSS public static org.netbeans.modules.glassfish.common.ui.BasePanel$Error
 outer org.netbeans.modules.glassfish.common.ui.BasePanel
cons public init()
meth protected java.lang.String getPrefix()
meth protected java.util.List<java.awt.Component> getDataComponents()
meth public void initializeData(java.lang.String,java.util.Map<java.lang.String,java.lang.String>)
supr org.netbeans.modules.glassfish.common.ui.BasePanel

CLSS public org.netbeans.modules.glassfish.common.ui.ConnectionPoolCustomizer
cons public init()
meth protected java.lang.String getPrefix()
meth protected java.util.List<java.awt.Component> getDataComponents()
supr org.netbeans.modules.glassfish.common.ui.BasePanel
hfds connectionPropertiesTable,jScrollPane1

CLSS public org.netbeans.modules.glassfish.common.ui.ConnectorConnectionPoolCustomizer
cons public init()
meth protected java.lang.String getPrefix()
meth protected java.util.List<java.awt.Component> getDataComponents()
supr org.netbeans.modules.glassfish.common.ui.BasePanel
hfds connectionPropertiesTable,jScrollPane1

CLSS public org.netbeans.modules.glassfish.common.ui.ConnectorCustomizer
cons public init()
meth protected java.lang.String getPrefix()
meth protected java.util.List<java.awt.Component> getDataComponents()
supr org.netbeans.modules.glassfish.common.ui.BasePanel
hfds poolNameCombo,poolNameLabel

CLSS public org.netbeans.modules.glassfish.common.ui.Filter
cons public init()
innr public static PortNumber
supr java.lang.Object

CLSS public static org.netbeans.modules.glassfish.common.ui.Filter$PortNumber
 outer org.netbeans.modules.glassfish.common.ui.Filter
cons public init()
meth public void insertString(javax.swing.text.DocumentFilter$FilterBypass,int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth public void replace(javax.swing.text.DocumentFilter$FilterBypass,int,int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
supr javax.swing.text.DocumentFilter

CLSS public org.netbeans.modules.glassfish.common.ui.GlassFishCredentials
cons public init(org.openide.NotifyDescriptor,org.netbeans.modules.glassfish.common.GlassfishInstance,java.lang.String)
meth public static boolean setCredentials(org.netbeans.modules.glassfish.common.GlassfishInstance)
meth public static boolean setCredentials(org.netbeans.modules.glassfish.common.GlassfishInstance,java.lang.String)
meth public void clear()
supr javax.swing.JPanel
hfds LOGGER,messageLabel,password,passwordLabel,userLabel,userText

CLSS public org.netbeans.modules.glassfish.common.ui.GlassFishPassword
cons public init(org.openide.NotifyDescriptor,org.netbeans.modules.glassfish.common.GlassfishInstance,java.lang.String)
meth public static java.lang.String setPassword(org.netbeans.modules.glassfish.common.GlassfishInstance)
meth public void clear()
supr javax.swing.JPanel
hfds LOGGER,messageLabel,password,passwordLabel,passwordVerify,passwordVerifyLabel,passwordVerifyLabelText,userLabel,userText

CLSS public org.netbeans.modules.glassfish.common.ui.GlassFishPropertiesCustomizer
cons public init(org.netbeans.modules.glassfish.common.GlassfishInstance,org.openide.util.Lookup)
supr javax.swing.JTabbedPane
hfds LOGGER,customizerListener
hcls CustomizerListener

CLSS public org.netbeans.modules.glassfish.common.ui.InstanceLocalPanel
cons public init(org.netbeans.modules.glassfish.common.GlassfishInstance)
meth protected java.lang.String getHost()
meth protected void enableFields()
meth protected void initHost()
meth protected void initPorts()
supr org.netbeans.modules.glassfish.common.ui.InstancePanel
hfds LOGGER

CLSS public abstract org.netbeans.modules.glassfish.common.ui.InstancePanel
cons protected init(org.netbeans.modules.glassfish.common.GlassfishInstance)
fld protected boolean cometSupportFlag
fld protected boolean configFileParsed
fld protected boolean httpMonitorFlag
fld protected boolean jdbcDriverDeploymentFlag
fld protected boolean loopbackFlag
fld protected boolean preserverSessionsFlag
fld protected boolean showPasswordFlag
fld protected boolean startDerbyFlag
fld protected final org.netbeans.modules.glassfish.common.GlassfishInstance instance
fld protected java.util.Set<? extends java.net.InetAddress> ips
fld protected javax.swing.JCheckBox commetSupport
fld protected javax.swing.JCheckBox httpMonitor
fld protected javax.swing.JCheckBox jdbcDriverDeployment
fld protected javax.swing.JCheckBox localIpCB
fld protected javax.swing.JCheckBox preserveSessions
fld protected javax.swing.JCheckBox showPassword
fld protected javax.swing.JCheckBox startDerby
fld protected javax.swing.JComboBox hostLocalField
fld protected javax.swing.JLabel dasPortLabel
fld protected javax.swing.JLabel domainLabel
fld protected javax.swing.JLabel domainsFolderLabel
fld protected javax.swing.JLabel hostLocalLabel
fld protected javax.swing.JLabel hostRemoteLabel
fld protected javax.swing.JLabel httpPortLabel
fld protected javax.swing.JLabel installationLocationLabel
fld protected javax.swing.JLabel passwordLabel
fld protected javax.swing.JLabel targetLabel
fld protected javax.swing.JLabel userNameLabel
fld protected javax.swing.JPasswordField passwordField
fld protected javax.swing.JTextField dasPortField
fld protected javax.swing.JTextField domainField
fld protected javax.swing.JTextField domainsFolderField
fld protected javax.swing.JTextField hostRemoteField
fld protected javax.swing.JTextField httpPortField
fld protected javax.swing.JTextField installationLocationField
fld protected javax.swing.JTextField targetField
fld protected javax.swing.JTextField userNameField
innr protected static CheckBoxProperties
meth protected abstract java.lang.String getHost()
meth protected abstract void initHost()
meth protected abstract void initPorts()
meth protected void disableAllFields()
meth protected void enableFields()
meth protected void initCheckBoxes()
meth protected void initCredentials()
meth protected void initDirectoriesFields()
meth protected void initDomainAndTarget()
meth protected void initFlagsFromProperties(org.netbeans.modules.glassfish.common.ui.InstancePanel$CheckBoxProperties)
meth protected void initFormFields()
meth protected void storeCheckBoxes()
meth protected void storeCredentials()
meth protected void storeFormFields()
meth protected void storeHost()
meth protected void storePorts()
meth protected void storeTarget()
meth protected void updatePasswordVisibility()
meth public void addNotify()
meth public void removeNotify()
supr javax.swing.JPanel
hfds LOGGER,MAX_PORT_VALUE

CLSS protected static org.netbeans.modules.glassfish.common.ui.InstancePanel$CheckBoxProperties
 outer org.netbeans.modules.glassfish.common.ui.InstancePanel
cons protected init(org.netbeans.modules.glassfish.common.GlassfishInstance)
meth protected boolean getCommetSupportProperty()
meth protected boolean getHttpMonitorProperty()
meth protected boolean getJdbcDriverDeploymentProperty()
meth protected boolean getLoopbackProperty()
meth protected boolean getPreserveSessionsProperty()
meth protected boolean getStartDerbyProperty()
meth protected void store(boolean,boolean,boolean,boolean,boolean,boolean,org.netbeans.modules.glassfish.common.GlassfishInstance)
meth protected void storeBooleanProperty(java.lang.String,boolean,org.netbeans.modules.glassfish.common.GlassfishInstance)
supr java.lang.Object
hfds cometSupportProperty,httpMonitorProperty,jdbcDriverDeploymentProperty,loopbackProperty,preserveSessionsProperty,startDerbyProperty

CLSS public org.netbeans.modules.glassfish.common.ui.InstanceRemotePanel
cons public init(org.netbeans.modules.glassfish.common.GlassfishInstance)
meth protected java.lang.String getHost()
meth protected void enableFields()
meth protected void initHost()
meth protected void initPorts()
supr org.netbeans.modules.glassfish.common.ui.InstancePanel
hfds LOGGER

CLSS public org.netbeans.modules.glassfish.common.ui.IpComboBox
cons public init(boolean)
cons public init(java.lang.Object[])
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(java.util.Set<? extends java.net.InetAddress>,boolean)
cons public init(java.util.Vector<?>)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(javax.swing.ComboBoxModel)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static java.lang.String IP_4_127_0_0_1_NAME = "localhost"
innr public static InetAddr
innr public static InetAddrComparator
meth public java.lang.Object getSelectedItem()
meth public java.net.InetAddress getSelectedIp()
meth public static boolean isLocalhost(java.net.InetAddress)
meth public void setSelectedIp(java.net.InetAddress)
meth public void setSelectedItem(java.lang.Object)
meth public void updateModel(boolean)
meth public void updateModel(java.util.Set<? extends java.net.InetAddress>,boolean)
supr javax.swing.JComboBox<org.netbeans.modules.glassfish.common.ui.IpComboBox$InetAddr>
hfds CONSTRUCTOR_EXCEPTION_MSG,IP_4_127_0_0_1,ipComparator

CLSS public static org.netbeans.modules.glassfish.common.ui.IpComboBox$InetAddr
 outer org.netbeans.modules.glassfish.common.ui.IpComboBox
meth public boolean isDefault()
meth public java.lang.String toString()
meth public java.net.InetAddress getIp()
supr java.lang.Object
hfds def,ip

CLSS public static org.netbeans.modules.glassfish.common.ui.IpComboBox$InetAddrComparator
 outer org.netbeans.modules.glassfish.common.ui.IpComboBox
cons public init()
intf java.util.Comparator<org.netbeans.modules.glassfish.common.ui.IpComboBox$InetAddr>
meth public int compare(org.netbeans.modules.glassfish.common.ui.IpComboBox$InetAddr,org.netbeans.modules.glassfish.common.ui.IpComboBox$InetAddr)
supr java.lang.Object
hfds INET_ADDRESS_COMPARATOR

CLSS public org.netbeans.modules.glassfish.common.ui.JavaMailCustomizer
cons public init()
meth protected java.lang.String getPrefix()
meth protected java.util.List<java.awt.Component> getDataComponents()
supr org.netbeans.modules.glassfish.common.ui.BasePanel
hfds mailHostField,mailHostLabel,resourceEnabledCB,returnField,returnLabel,userField,userLabel

CLSS public org.netbeans.modules.glassfish.common.ui.JavaPlatformsComboBox
cons public init()
cons public init(java.lang.Object[])
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(java.util.Vector<?>)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(javax.swing.ComboBoxModel)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(org.netbeans.api.java.platform.JavaPlatform[])
fld public final static java.lang.String EMPTY_DISPLAY_NAME
innr public static Platform
innr public static PlatformComparator
meth public void setSelectedItem(java.lang.Object)
meth public void updateModel()
meth public void updateModel(org.netbeans.api.java.platform.JavaPlatform[])
supr javax.swing.JComboBox<org.netbeans.modules.glassfish.common.ui.JavaPlatformsComboBox$Platform>
hfds CONSTRUCTOR_EXCEPTION_MSG,platformComparator

CLSS public static org.netbeans.modules.glassfish.common.ui.JavaPlatformsComboBox$Platform
 outer org.netbeans.modules.glassfish.common.ui.JavaPlatformsComboBox
meth public boolean isDefault()
meth public java.lang.String toString()
meth public org.netbeans.api.java.platform.JavaPlatform getPlatform()
supr java.lang.Object
hfds platform

CLSS public static org.netbeans.modules.glassfish.common.ui.JavaPlatformsComboBox$PlatformComparator
 outer org.netbeans.modules.glassfish.common.ui.JavaPlatformsComboBox
cons public init()
intf java.util.Comparator<org.netbeans.modules.glassfish.common.ui.JavaPlatformsComboBox$Platform>
meth public int compare(org.netbeans.modules.glassfish.common.ui.JavaPlatformsComboBox$Platform,org.netbeans.modules.glassfish.common.ui.JavaPlatformsComboBox$Platform)
supr java.lang.Object

CLSS public org.netbeans.modules.glassfish.common.ui.JavaSEPlatformPanel
cons public init(org.openide.NotifyDescriptor,org.netbeans.modules.glassfish.common.GlassfishInstance,java.lang.String)
meth public static org.openide.filesystems.FileObject selectServerSEPlatform(org.netbeans.modules.glassfish.common.GlassfishInstance,java.io.File)
supr javax.swing.JPanel
hfds LOGGER,descriptor,instance,javaComboBox,javaLabel,javaLabelText,javaPlatforms,message,messageLabel,platformButton,platformButtonAction,platformButtonText,propertiesCheckBox,propertiesLabel,propertiesLabelText
hcls PlatformAction

CLSS public org.netbeans.modules.glassfish.common.ui.JdbcResourceCustomizer
cons public init()
meth protected java.lang.String getPrefix()
meth protected java.util.List<java.awt.Component> getDataComponents()
supr org.netbeans.modules.glassfish.common.ui.BasePanel
hfds poolNameCombo,poolNameLabel,resourceEnabledCB

CLSS public org.netbeans.modules.glassfish.common.ui.VmCustomizer
cons public init(org.netbeans.modules.glassfish.common.GlassfishInstance)
meth public void addNotify()
meth public void removeNotify()
supr javax.swing.JPanel
hfds PORT_MAX,PORT_MIN,addressValue,buttonGroup1,debugSettingsPanel,instance,jLabel1,javaComboBox,javaInstallLabel,javaPlatforms,pickerPanel,platformButton,platformButtonAction,platformButtonText,useIDEProxyInfo,useSharedMemRB,useSocketRB,useUserDefinedAddress
hcls PlatformAction

CLSS public org.netbeans.modules.glassfish.common.ui.WarnPanel
cons public init(java.lang.String,boolean)
meth public boolean showAgain()
meth public static boolean gfKillWarning(java.lang.String)
meth public static void gf312WSWarning(java.lang.String)
meth public static void gfUnknownVersionWarning(java.lang.String,java.lang.String)
supr javax.swing.JPanel
hfds showAgain,warning,warningLabel

CLSS public org.netbeans.modules.glassfish.common.utils.AdminKeyFile
cons public init(org.netbeans.modules.glassfish.tooling.data.GlassFishServer)
fld public final static char HASH_ALGORITHM_PREFIX = '{'
fld public final static char HASH_ALGORITHM_SUFFIX = '}'
fld public final static char SEPARATOR = ';'
fld public final static int RANDOM_PASSWORD_LENGTH = 12
fld public final static java.lang.String ADMIN_KEYFILE_NAME = "admin-keyfile"
fld public final static java.lang.String DEFAULT_TOOL = "asadmin"
fld public final static java.lang.String DEFAULT_USER = "admin"
fld public final static java.lang.String EOL
fld public final static java.lang.String HASH_ALGORITHM = "SHA-1"
fld public final static java.lang.String HASH_ALGORITHM_GALSSFISH = "SSHA"
fld public final static java.lang.String PASSWORD_ENCODING = "UTF-8"
fld public final static java.lang.String PASSWORD_RESET = "RESET"
meth public boolean isReset()
meth public boolean read()
meth public boolean setPassword(java.lang.String)
meth public boolean write()
meth public static java.lang.String buildAdminKeyFilePath(org.netbeans.modules.glassfish.tooling.data.GlassFishServer)
meth public static java.lang.String randomPassword(int)
supr java.lang.Object
hfds CHARS_PW,CHARS_PW0,CHARS_PWL,LOGGER,MIN_PW_SIZE,adminKeyFile,passwordChars,passwordHash,reset,tool,user
hcls Parser

CLSS public org.netbeans.modules.glassfish.common.utils.JavaUtils
cons public init()
fld public final static java.lang.String JAVA_SE_SPECIFICATION_NAME = "j2se"
meth public static boolean checkAndRegisterJavaPlatform(java.lang.String)
meth public static boolean isJavaPlatformSupported(org.netbeans.modules.glassfish.common.GlassfishInstance,java.io.File)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static java.lang.String getDefaultJavaHome()
meth public static java.lang.String getJavaHome(org.netbeans.api.java.platform.JavaPlatform)
meth public static org.netbeans.api.java.platform.JavaPlatform findInstalledPlatform(java.io.File)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.java.platform.JavaPlatform findPlatformByJavaHome(org.netbeans.api.java.platform.JavaPlatform[],java.io.File)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.java.platform.JavaPlatform[] findSupportedPlatforms(org.netbeans.modules.glassfish.common.GlassfishInstance)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.java.platform.JavaPlatform[] getInstalledJavaSEPlatforms()
supr java.lang.Object
hfds GF_PLATFORM_DISPLAY_NAME_PREFIX,GF_PLATFORM_DISPLAY_NAME_SUFFIX,LOGGER

CLSS public org.netbeans.modules.glassfish.common.utils.ServerUtils
cons public init()
meth public static boolean isProcessRunning(java.lang.Process)
meth public static boolean isValidFolder(java.lang.String)
meth public static java.lang.String getDomainsFolder(org.netbeans.modules.glassfish.common.GlassfishInstance)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static java.lang.String getStringAttribute(org.openide.filesystems.FileObject,java.lang.String)
meth public static java.lang.String getStringAttribute(org.openide.filesystems.FileObject,java.lang.String,java.lang.String)
meth public static org.openide.filesystems.FileObject getRepositoryDir(java.lang.String,boolean)
meth public static void setStringAttribute(org.openide.filesystems.FileObject,java.lang.String,java.lang.String)
supr java.lang.Object
hfds DOMAINS_FOLDER_PREFIX,LOGGER

CLSS public final org.netbeans.modules.glassfish.common.utils.Util
fld public final static java.lang.String GF_LOOKUP_PATH = "Servers/GlassFish"
meth public final java.util.List<java.io.File> classPathToFileList(java.lang.String,java.io.File)
meth public static boolean appearsToBeJdk6OrBetter(java.io.File)
meth public static boolean isDefaultOrServerTarget(java.util.Map<java.lang.String,java.lang.String>)
meth public static boolean readServerConfiguration(java.io.File,org.netbeans.modules.glassfish.common.PortCollection)
meth public static java.lang.String computeTarget(java.util.Map<java.lang.String,java.lang.String>)
meth public static java.lang.String escapePath(java.lang.String)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public static java.lang.String quote(java.lang.String)
supr java.lang.Object
hfds DOMAIN_XML_PATH,INDICATOR,JDK6_DETECTION_FILTER
hcls HttpData

CLSS public org.netbeans.modules.glassfish.spi.AppDesc
cons public init(java.lang.String,java.lang.String,java.lang.String,boolean)
meth public boolean getEnabled()
meth public java.lang.String getContextRoot()
meth public java.lang.String getName()
meth public java.lang.String getPath()
supr java.lang.Object
hfds contextRoot,enabled,name,path

CLSS public abstract interface org.netbeans.modules.glassfish.spi.CommandFactory
meth public abstract org.netbeans.modules.glassfish.tooling.admin.CommandSetProperty getSetPropertyCommand(java.lang.String,java.lang.String)

CLSS public abstract interface org.netbeans.modules.glassfish.spi.CustomizerCookie
meth public abstract java.util.Collection<javax.swing.JPanel> getCustomizerPages()

CLSS public abstract org.netbeans.modules.glassfish.spi.Decorator
cons public init()
fld public final static java.awt.Image DISABLED_BADGE
fld public final static java.lang.String DISABLED = "disabled "
meth public boolean canCopy()
meth public boolean canDeployTo()
meth public boolean canDisable()
meth public boolean canEditDetails()
meth public boolean canEnable()
meth public boolean canShowBrowser()
meth public boolean canTest()
meth public boolean canUndeploy()
meth public boolean canUnregister()
meth public boolean isRefreshable()
meth public java.awt.Image getIcon(int)
meth public java.awt.Image getIconBadge()
meth public java.awt.Image getOpenedIcon(int)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.glassfish.spi.DecoratorFactory
meth public abstract boolean isTypeSupported(java.lang.String)
meth public abstract java.util.Map<java.lang.String,org.netbeans.modules.glassfish.spi.Decorator> getAllDecorators()
meth public abstract org.netbeans.modules.glassfish.spi.Decorator getDecorator(java.lang.String)

CLSS public org.netbeans.modules.glassfish.spi.ExecSupport
cons public init()
innr public static OutputCopier
meth public void displayProcessOutputs(java.lang.Process,java.lang.String,java.lang.String) throws java.io.IOException,java.lang.InterruptedException
supr java.lang.Object

CLSS public static org.netbeans.modules.glassfish.spi.ExecSupport$OutputCopier
 outer org.netbeans.modules.glassfish.spi.ExecSupport
cons public init(java.io.Reader,java.io.Writer,boolean)
meth public void interrupt()
meth public void run()
supr java.lang.Thread
hfds autoflush,done,is,os

CLSS public abstract interface org.netbeans.modules.glassfish.spi.GlassfishModule
fld public final static int PROPERTIES_FETCH_TIMEOUT = 10000
fld public final static java.lang.String ADMINOBJECT_RESOURCE = "admin-object"
fld public final static java.lang.String ADMINPORT_ATTR = "adminPort"
fld public final static java.lang.String APPCLIENT_CONTAINER = "appclient"
fld public final static java.lang.String COMET_FLAG = "v3.grizzly.cometSupport"
fld public final static java.lang.String CONNECTORS = "CONNECTORS"
fld public final static java.lang.String CONNECTOR_CONTAINER = "connector"
fld public final static java.lang.String CONN_CONNECTION_POOL = "connector-connection-pool"
fld public final static java.lang.String CONN_RESOURCE = "connector-resource"
fld public final static java.lang.String DEBUG_MEM = "debugMem"
fld public final static java.lang.String DEBUG_MODE
fld public final static java.lang.String DEBUG_PORT = "debugPort"
fld public final static java.lang.String DISPLAY_NAME_ATTR = "displayName"
fld public final static java.lang.String DOMAINS_FOLDER_ATTR = "domainsfolder"
fld public final static java.lang.String DOMAIN_NAME_ATTR = "domainname"
fld public final static java.lang.String DRIVER_DEPLOY_FLAG = "driverDeployOn"
fld public final static java.lang.String EAR_CONTAINER = "ear"
fld public final static java.lang.String EJB_CONTAINER = "ejb"
fld public final static java.lang.String GEM_HOME = "GEM_HOME"
fld public final static java.lang.String GEM_PATH = "GEM_PATH"
fld public final static java.lang.String GLASSFISH_FOLDER_ATTR = "homefolder"
fld public final static java.lang.String HOSTNAME_ATTR = "host"
fld public final static java.lang.String HTTPHOST_ATTR = "httphostname"
fld public final static java.lang.String HTTPPORT_ATTR = "httpportnumber"
fld public final static java.lang.String HTTP_MONITOR_FLAG = "httpMonitorOn"
fld public final static java.lang.String INSTALL_FOLDER_ATTR = "installfolder"
fld public final static java.lang.String JAVAMAIL = "JAVAMAIL"
fld public final static java.lang.String JAVAMAIL_RESOURCE = "javamail-resource"
fld public final static java.lang.String JAVA_PLATFORM_ATTR = "java.platform"
fld public final static java.lang.String JDBC = "JDBC"
fld public final static java.lang.String JDBC_CONNECTION_POOL = "jdbc-connection-pool"
fld public final static java.lang.String JDBC_RESOURCE = "jdbc-resource"
fld public final static java.lang.String JRUBY_CONTAINER = "jruby"
fld public final static java.lang.String JRUBY_HOME = "jruby.home"
fld public final static java.lang.String JVM_MODE = "jvmMode"
fld public final static java.lang.String LOOPBACK_FLAG = "loopbackOn"
fld public final static java.lang.String NB73_IMPORT_FIXED = "nb73ImportFixed"
fld public final static java.lang.String NORMAL_MODE
fld public final static java.lang.String PASSWORD_ATTR = "password"
fld public final static java.lang.String PASSWORD_CONVERTED_FLAG = "this really long string is used to identify a password that has been stored in the Keyring"
fld public final static java.lang.String PROFILE_MODE
fld public final static java.lang.String SESSION_PRESERVATION_FLAG = "preserveSessionsOn"
fld public final static java.lang.String START_DERBY_FLAG = "derbyStartOn"
fld public final static java.lang.String TARGET_ATTR = "target"
fld public final static java.lang.String URL_ATTR = "url"
fld public final static java.lang.String USERNAME_ATTR = "username"
fld public final static java.lang.String USE_IDE_PROXY_FLAG = "useIDEProxyOn"
fld public final static java.lang.String USE_SHARED_MEM_ATTR = "use.shared.mem"
fld public final static java.lang.String WEB_CONTAINER = "web"
innr public final static !enum ServerState
meth public abstract boolean isRemote()
meth public abstract boolean isRestfulLogAccessSupported()
meth public abstract boolean isWritable()
meth public abstract boolean supportsRestartInDebug()
meth public abstract java.lang.String getPassword()
meth public abstract java.lang.String getResourcesXmlName()
meth public abstract java.lang.String setEnvironmentProperty(java.lang.String,java.lang.String,boolean)
meth public abstract java.util.Map<java.lang.String,java.lang.String> getInstanceProperties()
meth public abstract java.util.Map<java.lang.String,org.netbeans.modules.glassfish.spi.ResourceDesc> getResourcesMap(java.lang.String)
meth public abstract java.util.concurrent.Future<org.netbeans.modules.glassfish.tooling.TaskState> killServer(org.netbeans.modules.glassfish.tooling.TaskStateListener)
meth public abstract java.util.concurrent.Future<org.netbeans.modules.glassfish.tooling.TaskState> restartServer(org.netbeans.modules.glassfish.tooling.TaskStateListener)
meth public abstract java.util.concurrent.Future<org.netbeans.modules.glassfish.tooling.TaskState> startServer(org.netbeans.modules.glassfish.tooling.TaskStateListener,org.netbeans.modules.glassfish.spi.GlassfishModule$ServerState)
meth public abstract java.util.concurrent.Future<org.netbeans.modules.glassfish.tooling.TaskState> stopServer(org.netbeans.modules.glassfish.tooling.TaskStateListener)
meth public abstract java.util.concurrent.Future<org.netbeans.modules.glassfish.tooling.admin.ResultString> deploy(org.netbeans.modules.glassfish.tooling.TaskStateListener,java.io.File,java.lang.String)
meth public abstract java.util.concurrent.Future<org.netbeans.modules.glassfish.tooling.admin.ResultString> deploy(org.netbeans.modules.glassfish.tooling.TaskStateListener,java.io.File,java.lang.String,java.lang.String)
meth public abstract java.util.concurrent.Future<org.netbeans.modules.glassfish.tooling.admin.ResultString> deploy(org.netbeans.modules.glassfish.tooling.TaskStateListener,java.io.File,java.lang.String,java.lang.String,java.util.Map<java.lang.String,java.lang.String>)
meth public abstract java.util.concurrent.Future<org.netbeans.modules.glassfish.tooling.admin.ResultString> disable(org.netbeans.modules.glassfish.tooling.TaskStateListener,java.lang.String)
meth public abstract java.util.concurrent.Future<org.netbeans.modules.glassfish.tooling.admin.ResultString> enable(org.netbeans.modules.glassfish.tooling.TaskStateListener,java.lang.String)
meth public abstract java.util.concurrent.Future<org.netbeans.modules.glassfish.tooling.admin.ResultString> redeploy(org.netbeans.modules.glassfish.tooling.TaskStateListener,java.lang.String,boolean)
meth public abstract java.util.concurrent.Future<org.netbeans.modules.glassfish.tooling.admin.ResultString> redeploy(org.netbeans.modules.glassfish.tooling.TaskStateListener,java.lang.String,java.lang.String,boolean)
meth public abstract java.util.concurrent.Future<org.netbeans.modules.glassfish.tooling.admin.ResultString> undeploy(org.netbeans.modules.glassfish.tooling.TaskStateListener,java.lang.String)
meth public abstract org.netbeans.modules.glassfish.common.GlassfishInstanceProvider getInstanceProvider()
meth public abstract org.netbeans.modules.glassfish.spi.AppDesc[] getModuleList(java.lang.String)
meth public abstract org.netbeans.modules.glassfish.spi.CommandFactory getCommandFactory()
meth public abstract org.netbeans.modules.glassfish.spi.GlassfishModule$ServerState getServerState()
meth public abstract org.netbeans.modules.glassfish.tooling.data.GlassFishServer getInstance()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public final static !enum org.netbeans.modules.glassfish.spi.GlassfishModule$ServerState
 outer org.netbeans.modules.glassfish.spi.GlassfishModule
fld public final static org.netbeans.modules.glassfish.spi.GlassfishModule$ServerState RUNNING
fld public final static org.netbeans.modules.glassfish.spi.GlassfishModule$ServerState RUNNING_JVM_DEBUG
fld public final static org.netbeans.modules.glassfish.spi.GlassfishModule$ServerState RUNNING_JVM_PROFILER
fld public final static org.netbeans.modules.glassfish.spi.GlassfishModule$ServerState STARTING
fld public final static org.netbeans.modules.glassfish.spi.GlassfishModule$ServerState STOPPED
fld public final static org.netbeans.modules.glassfish.spi.GlassfishModule$ServerState STOPPED_JVM_BP
fld public final static org.netbeans.modules.glassfish.spi.GlassfishModule$ServerState STOPPED_JVM_PROFILER
fld public final static org.netbeans.modules.glassfish.spi.GlassfishModule$ServerState STOPPING
fld public final static org.netbeans.modules.glassfish.spi.GlassfishModule$ServerState UNKNOWN
meth public static org.netbeans.modules.glassfish.spi.GlassfishModule$ServerState valueOf(java.lang.String)
meth public static org.netbeans.modules.glassfish.spi.GlassfishModule$ServerState[] values()
supr java.lang.Enum<org.netbeans.modules.glassfish.spi.GlassfishModule$ServerState>

CLSS public abstract interface org.netbeans.modules.glassfish.spi.GlassfishModule2
intf org.netbeans.modules.glassfish.spi.GlassfishModule
meth public abstract java.util.concurrent.Future<org.netbeans.modules.glassfish.tooling.admin.ResultString> deploy(org.netbeans.modules.glassfish.tooling.TaskStateListener,java.io.File,java.lang.String,java.lang.String,java.util.Map<java.lang.String,java.lang.String>,java.io.File[])
meth public abstract java.util.concurrent.Future<org.netbeans.modules.glassfish.tooling.admin.ResultString> redeploy(org.netbeans.modules.glassfish.tooling.TaskStateListener,java.lang.String,java.lang.String,java.io.File[],boolean)

CLSS public abstract interface org.netbeans.modules.glassfish.spi.GlassfishModule3
intf org.netbeans.modules.glassfish.spi.GlassfishModule2
meth public abstract org.openide.util.RequestProcessor$Task refresh()

CLSS public abstract interface org.netbeans.modules.glassfish.spi.GlassfishModuleFactory
meth public abstract boolean isModuleSupported(java.lang.String,java.util.Properties)
meth public abstract java.lang.Object createModule(org.openide.util.Lookup)

CLSS public abstract interface org.netbeans.modules.glassfish.spi.JrePicker
meth public abstract javax.swing.JPanel component(org.netbeans.modules.glassfish.spi.GlassfishModule)

CLSS public abstract interface org.netbeans.modules.glassfish.spi.PluggableNodeProvider
meth public abstract org.openide.nodes.Node getPluggableNode(java.util.Map<java.lang.String,java.lang.String>)

CLSS public abstract interface org.netbeans.modules.glassfish.spi.Recognizer
meth public abstract org.openide.windows.OutputListener processLine(java.lang.String)

CLSS public abstract interface org.netbeans.modules.glassfish.spi.RecognizerCookie
meth public abstract java.util.Collection<? extends org.netbeans.modules.glassfish.spi.Recognizer> getRecognizers()

CLSS public org.netbeans.modules.glassfish.spi.RegisterDatabase
cons public init()
meth public static org.netbeans.modules.glassfish.spi.RegisterDatabase getDefault()
meth public void configureDatabase()
meth public void setupDerby(java.lang.String)
supr java.lang.Object
hfds reg
hcls DerbyLibraryRegistrar

CLSS public abstract interface org.netbeans.modules.glassfish.spi.RegisteredDDCatalog
meth public abstract void refreshRunTimeDDCatalog(org.netbeans.spi.server.ServerInstanceProvider,java.lang.String)
meth public abstract void registerEE6RunTimeDDCatalog(org.netbeans.spi.server.ServerInstanceProvider)

CLSS public abstract interface org.netbeans.modules.glassfish.spi.RegisteredDerbyServer
meth public abstract void initialize(java.lang.String)
meth public abstract void start()

CLSS public abstract interface org.netbeans.modules.glassfish.spi.RemoveCookie
meth public abstract void removeInstance(java.lang.String)

CLSS public abstract org.netbeans.modules.glassfish.spi.ResourceDecorator
cons public init()
meth public abstract java.lang.String getCmdPropertyName()
meth public boolean canEditDetails()
meth public boolean isCascadeDelete()
supr org.netbeans.modules.glassfish.spi.Decorator

CLSS public org.netbeans.modules.glassfish.spi.ResourceDesc
cons public init(java.lang.String,java.lang.String)
intf java.lang.Comparable<org.netbeans.modules.glassfish.spi.ResourceDesc>
meth public int compareTo(org.netbeans.modules.glassfish.spi.ResourceDesc)
meth public java.lang.String getCommandType()
meth public java.lang.String getName()
meth public static java.util.List<org.netbeans.modules.glassfish.spi.ResourceDesc> getResources(org.netbeans.modules.glassfish.common.GlassfishInstance,java.lang.String)
supr java.lang.Object
hfds LOGGER,cmdType,name

CLSS public final org.netbeans.modules.glassfish.spi.ServerUtilities
fld public final static int ACTION_TIMEOUT = 15000
fld public final static java.lang.String GFV3_JAR_MATCHER = "glassfish(?:-[0-9bSNAPHOT]+(?:\u005c.[0-9]+(?:_[0-9]+|)|).*|).jar"
fld public final static java.lang.String GFV3_LIB_DIR_NAME = "lib"
fld public final static java.lang.String GFV3_MODULES_DIR_NAME = "modules"
fld public final static java.lang.String GFV3_VERSION_MATCHER = "(?:-[0-9bSNAPHOT]+(?:\u005c.[0-9]+(?:_[0-9]+|)|).*|).jar"
fld public final static java.lang.String PROP_FIRST_RUN = "first_run"
fld public final static java.util.concurrent.TimeUnit ACTION_TIMEOUT_UNIT
meth public <%0 extends java.lang.Object> java.util.List<{%%0}> getInstancesByCapability(java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} getInstanceByCapability(java.lang.String,java.lang.Class<{%%0}>)
meth public boolean isRegisteredUri(java.lang.String)
meth public final static java.lang.String quote(java.lang.String)
meth public org.netbeans.api.server.ServerInstance getServerInstance(java.lang.String)
meth public org.netbeans.spi.server.ServerInstanceProvider getServerProvider()
meth public org.openide.util.Lookup getLookupFor(org.netbeans.api.server.ServerInstance)
meth public static boolean isTP2(java.lang.String)
meth public static java.io.File getJarName(java.lang.String,java.lang.String)
meth public static java.io.File getJarName(java.lang.String,java.lang.String,java.lang.String)
meth public static java.io.File getWsJarName(java.lang.String,java.lang.String)
meth public static java.net.URL fileToUrl(java.io.File) throws java.net.MalformedURLException
meth public static java.util.List<java.lang.String> filterByManifest(java.util.List<java.lang.String>,org.openide.filesystems.FileObject,int,boolean)
meth public static org.netbeans.modules.glassfish.spi.ServerUtilities getEe6Utilities()
meth public static org.netbeans.modules.glassfish.spi.ServerUtilities getEe7Utilities()
meth public static org.netbeans.modules.glassfish.spi.ServerUtilities getEe8Utilities()
meth public static org.netbeans.modules.glassfish.spi.ServerUtilities getJakartaEe10Utilities()
meth public static org.netbeans.modules.glassfish.spi.ServerUtilities getJakartaEe8Utilities()
meth public static org.netbeans.modules.glassfish.spi.ServerUtilities getJakartaEe91Utilities()
meth public static org.netbeans.modules.glassfish.spi.ServerUtilities getJakartaEe9Utilities()
meth public static org.openide.WizardDescriptor$InstantiatingIterator getInstantiatingIterator()
supr java.lang.Object
hfds gip,gwp

CLSS public org.netbeans.modules.glassfish.spi.Utils
cons public init()
fld public final static java.lang.String VERSIONED_JAR_SUFFIX_MATCHER = "(?:-[0-9]+(?:\u005c.[0-9]+(?:_[0-9]+|)|).*|).jar"
meth public final static java.lang.String escapePath(java.lang.String)
meth public static boolean canWrite(java.io.File)
meth public static boolean isLocalPortOccupied(int)
meth public static boolean isSecurePort(java.lang.String,int) throws java.io.IOException
meth public static boolean useGlassfishPrefix(java.lang.String)
meth public static java.io.File getFileFromPattern(java.lang.String,java.io.File)
meth public static java.lang.String getHttpListenerProtocol(java.lang.String,int)
meth public static java.lang.String getHttpListenerProtocol(java.lang.String,java.lang.String)
meth public static java.lang.String sanitizeName(java.lang.String)
meth public static void doCopy(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject) throws java.io.IOException
supr java.lang.Object
hfds PORT_CHECK_TIMEOUT
hcls VersionFilter

CLSS public abstract interface org.netbeans.modules.glassfish.spi.VMIntrospector
meth public abstract boolean isSuspended(java.lang.String,java.lang.String)

CLSS public org.netbeans.modules.glassfish.spi.WSDesc
cons public init(java.lang.String)
meth public java.lang.String getName()
meth public java.lang.String getTestURL()
meth public java.lang.String getWsdlUrl()
meth public static java.util.List<org.netbeans.modules.glassfish.spi.WSDesc> getWebServices(org.netbeans.modules.glassfish.common.GlassfishInstance)
supr java.lang.Object
hfds LOGGER,TEST_URL_EXTENSION,WSDL_URL_EXTENSION,name,testUrl,wsdlUrl

CLSS public abstract interface org.netbeans.modules.glassfish.tooling.GlassFishStatusListener
meth public abstract void added()
meth public abstract void currentState(org.netbeans.modules.glassfish.tooling.data.GlassFishServer,org.netbeans.modules.glassfish.tooling.GlassFishStatus,org.netbeans.modules.glassfish.tooling.data.GlassFishStatusTask)
meth public abstract void error(org.netbeans.modules.glassfish.tooling.data.GlassFishServer,org.netbeans.modules.glassfish.tooling.data.GlassFishStatusTask)
meth public abstract void newState(org.netbeans.modules.glassfish.tooling.data.GlassFishServer,org.netbeans.modules.glassfish.tooling.GlassFishStatus,org.netbeans.modules.glassfish.tooling.data.GlassFishStatusTask)
meth public abstract void removed()

CLSS public abstract interface org.netbeans.modules.glassfish.tooling.data.GlassFishServer
meth public abstract boolean isRemote()
meth public abstract int getAdminPort()
meth public abstract int getPort()
meth public abstract java.lang.String getAdminPassword()
meth public abstract java.lang.String getAdminUser()
meth public abstract java.lang.String getDomainName()
meth public abstract java.lang.String getDomainsFolder()
meth public abstract java.lang.String getHost()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getServerHome()
meth public abstract java.lang.String getServerRoot()
meth public abstract java.lang.String getUrl()
meth public abstract org.netbeans.modules.glassfish.tooling.data.GlassFishAdminInterface getAdminInterface()
meth public abstract org.netbeans.modules.glassfish.tooling.data.GlassFishVersion getVersion()

CLSS public abstract interface org.netbeans.spi.server.ServerInstanceImplementation
meth public abstract boolean isRemovable()
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getServerDisplayName()
meth public abstract javax.swing.JComponent getCustomizer()
meth public abstract org.openide.nodes.Node getBasicNode()
meth public abstract org.openide.nodes.Node getFullNode()
meth public abstract void remove()

CLSS public abstract interface org.netbeans.spi.server.ServerInstanceProvider
meth public abstract java.util.List<org.netbeans.api.server.ServerInstance> getInstances()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public abstract interface org.openide.filesystems.FileChangeListener
intf java.util.EventListener
meth public abstract void fileAttributeChanged(org.openide.filesystems.FileAttributeEvent)
meth public abstract void fileChanged(org.openide.filesystems.FileEvent)
meth public abstract void fileDataCreated(org.openide.filesystems.FileEvent)
meth public abstract void fileDeleted(org.openide.filesystems.FileEvent)
meth public abstract void fileFolderCreated(org.openide.filesystems.FileEvent)
meth public abstract void fileRenamed(org.openide.filesystems.FileRenameEvent)

CLSS public org.openide.modules.ModuleInstall
cons public init()
meth protected boolean clearSharedData()
meth public boolean closing()
meth public void close()
meth public void installed()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public void restored()
meth public void uninstalled()
meth public void updated(int,java.lang.String)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public void validate()
supr org.openide.util.SharedClassObject
hfds serialVersionUID

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
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public org.openide.util.actions.SystemAction[] getActions()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public org.openide.util.actions.SystemAction[] getContextActions()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public void destroy() throws java.io.IOException
meth public void setDisplayName(java.lang.String)
meth public void setHidden(boolean)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public void setName(java.lang.String)
meth public void setShortDescription(java.lang.String)
supr java.beans.FeatureDescriptor
hfds BLOCK_EVENTS,INIT_LOCK,LOCK,TEMPL_COOKIE,err,hierarchy,listeners,lookups,parent,warnedBadProperties
hcls LookupEventList,PropertyEditorRef

CLSS public abstract interface static org.openide.nodes.Node$Cookie
 outer org.openide.nodes.Node

CLSS public final org.openide.util.HelpCtx
cons public init(java.lang.Class<?>)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(java.lang.String)
cons public init(java.net.URL)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
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

