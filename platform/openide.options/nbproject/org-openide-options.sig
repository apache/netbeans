#Signature file v4.1
#Version 6.60

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

CLSS public abstract interface java.beans.PropertyEditor
meth public abstract boolean isPaintable()
meth public abstract boolean supportsCustomEditor()
meth public abstract java.awt.Component getCustomEditor()
meth public abstract java.lang.Object getValue()
meth public abstract java.lang.String getAsText()
meth public abstract java.lang.String getJavaInitializationString()
meth public abstract java.lang.String[] getTags()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void paintValue(java.awt.Graphics,java.awt.Rectangle)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setAsText(java.lang.String)
meth public abstract void setValue(java.lang.Object)

CLSS public java.beans.PropertyEditorSupport
cons public init()
cons public init(java.lang.Object)
intf java.beans.PropertyEditor
meth public boolean isPaintable()
meth public boolean supportsCustomEditor()
meth public java.awt.Component getCustomEditor()
meth public java.lang.Object getSource()
meth public java.lang.Object getValue()
meth public java.lang.String getAsText()
meth public java.lang.String getJavaInitializationString()
meth public java.lang.String[] getTags()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void firePropertyChange()
meth public void paintValue(java.awt.Graphics,java.awt.Rectangle)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setAsText(java.lang.String)
meth public void setSource(java.lang.Object)
meth public void setValue(java.lang.Object)
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

CLSS public abstract interface java.beans.beancontext.BeanContextProxy
meth public abstract java.beans.beancontext.BeanContextChild getBeanContextProxy()

CLSS public abstract interface java.io.Externalizable
intf java.io.Serializable
meth public abstract void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public abstract void writeExternal(java.io.ObjectOutput) throws java.io.IOException

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

CLSS public abstract interface java.text.AttributedCharacterIterator
innr public static Attribute
intf java.text.CharacterIterator
meth public abstract int getRunLimit()
meth public abstract int getRunLimit(java.text.AttributedCharacterIterator$Attribute)
meth public abstract int getRunLimit(java.util.Set<? extends java.text.AttributedCharacterIterator$Attribute>)
meth public abstract int getRunStart()
meth public abstract int getRunStart(java.text.AttributedCharacterIterator$Attribute)
meth public abstract int getRunStart(java.util.Set<? extends java.text.AttributedCharacterIterator$Attribute>)
meth public abstract java.lang.Object getAttribute(java.text.AttributedCharacterIterator$Attribute)
meth public abstract java.util.Map<java.text.AttributedCharacterIterator$Attribute,java.lang.Object> getAttributes()
meth public abstract java.util.Set<java.text.AttributedCharacterIterator$Attribute> getAllAttributeKeys()

CLSS public abstract interface java.text.CharacterIterator
fld public final static char DONE = '\uffff'
intf java.lang.Cloneable
meth public abstract char current()
meth public abstract char first()
meth public abstract char last()
meth public abstract char next()
meth public abstract char previous()
meth public abstract char setIndex(int)
meth public abstract int getBeginIndex()
meth public abstract int getEndIndex()
meth public abstract int getIndex()
meth public abstract java.lang.Object clone()

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

CLSS public abstract interface javax.swing.text.Position
innr public final static Bias
meth public abstract int getOffset()

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

CLSS public abstract interface javax.swing.undo.UndoableEdit
meth public abstract boolean addEdit(javax.swing.undo.UndoableEdit)
meth public abstract boolean canRedo()
meth public abstract boolean canUndo()
meth public abstract boolean isSignificant()
meth public abstract boolean replaceEdit(javax.swing.undo.UndoableEdit)
meth public abstract java.lang.String getPresentationName()
meth public abstract java.lang.String getRedoPresentationName()
meth public abstract java.lang.String getUndoPresentationName()
meth public abstract void die()
meth public abstract void redo()
meth public abstract void undo()

CLSS public abstract org.openide.ServiceType
 anno 0 java.lang.Deprecated()
cons public init()
fld public final static java.lang.String PROP_NAME = "name"
innr public abstract static Registry
innr public final static Handle
intf java.io.Serializable
intf org.openide.util.HelpCtx$Provider
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
 anno 0 java.lang.Deprecated()
meth protected java.lang.String displayName()
meth public abstract org.openide.util.HelpCtx getHelpCtx()
meth public final org.openide.ServiceType createClone()
 anno 0 java.lang.Deprecated()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public java.lang.String getName()
meth public void setName(java.lang.String)
supr java.lang.Object
hfds err,name,serialVersionUID,supp

CLSS public org.openide.explorer.propertysheet.PropertySheetSettings
 anno 0 java.lang.Deprecated()
cons public init()
meth public boolean getDisplayWritableOnly()
 anno 0 java.lang.Deprecated()
meth public boolean getPlastic()
 anno 0 java.lang.Deprecated()
meth public int getPropertyPaintingStyle()
 anno 0 java.lang.Deprecated()
meth public int getSortingMode()
meth public java.awt.Color getDisabledPropertyColor()
 anno 0 java.lang.Deprecated()
meth public java.awt.Color getValueColor()
meth public java.lang.String displayName()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void setDisabledPropertyColor(java.awt.Color)
 anno 0 java.lang.Deprecated()
meth public void setDisplayWritableOnly(boolean)
meth public void setPlastic(boolean)
 anno 0 java.lang.Deprecated()
meth public void setPropertyPaintingStyle(int)
 anno 0 java.lang.Deprecated()
meth public void setSortingMode(int)
meth public void setValueColor(java.awt.Color)
 anno 0 java.lang.Deprecated()
supr org.openide.options.SystemOption
hfds disabledColor,displayWritableOnly,plastic,propertyPaintingStyle,propertySheetSettings,serialVersionUID,sortingMode,valueColor

CLSS public abstract org.openide.options.ContextSystemOption
cons public init()
fld protected java.beans.beancontext.BeanContext beanContext
 anno 0 java.lang.Deprecated()
intf java.beans.beancontext.BeanContextProxy
meth protected void initialize()
meth public final java.beans.beancontext.BeanContextChild getBeanContextProxy()
meth public final org.openide.options.SystemOption[] getOptions()
meth public final void addOption(org.openide.options.SystemOption)
meth public final void removeOption(org.openide.options.SystemOption)
meth public void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr org.openide.options.SystemOption
hfds ctxt,serialVersionUID
hcls ClassComparator,OptionBeanContext

CLSS public org.openide.options.ContextSystemOptionBeanInfo
cons public init()
meth public java.beans.BeanInfo[] getAdditionalBeanInfo()
meth public java.beans.PropertyDescriptor[] getPropertyDescriptors()
supr java.beans.SimpleBeanInfo

CLSS public abstract org.openide.options.SystemOption
 anno 0 java.lang.Deprecated()
cons public init()
intf org.openide.util.HelpCtx$Provider
meth protected boolean clearSharedData()
meth protected final boolean isReadExternal()
meth protected final boolean isWriteExternal()
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void reset()
meth public abstract java.lang.String displayName()
meth public final java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr org.openide.util.SharedClassObject
hfds NULL,PROP_LOADING,PROP_ORIGINAL_VALUES,PROP_STORING,serialVersionUID
hcls Box

CLSS public org.openide.options.SystemOptionBeanInfo
cons public init()
meth public java.beans.PropertyDescriptor[] getPropertyDescriptors()
supr java.beans.SimpleBeanInfo

CLSS public abstract org.openide.options.VetoSystemOption
cons public init()
meth public final void addVetoableChangeListener(java.beans.VetoableChangeListener)
meth public final void fireVetoableChange(java.lang.String,java.lang.Object,java.lang.Object) throws java.beans.PropertyVetoException
meth public final void removeVetoableChangeListener(java.beans.VetoableChangeListener)
supr org.openide.options.SystemOption
hfds PROP_VETO_SUPPORT,serialVersionUID

CLSS public abstract interface org.openide.text.ActiveEditorDrop
fld public final static java.awt.datatransfer.DataFlavor FLAVOR
meth public abstract boolean handleTransfer(javax.swing.text.JTextComponent)

CLSS public abstract org.openide.text.Annotatable
cons public init()
fld public final static java.lang.String PROP_ANNOTATION_COUNT = "annotationCount"
fld public final static java.lang.String PROP_DELETED = "deleted"
fld public final static java.lang.String PROP_TEXT = "text"
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void addAnnotation(org.openide.text.Annotation)
meth protected void removeAnnotation(org.openide.text.Annotation)
meth public abstract java.lang.String getText()
meth public final boolean isDeleted()
meth public final int getAnnotationCount()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds attachedAnnotations,deleted,propertyChangeSupport

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

CLSS public org.openide.text.AttributedCharacters
cons public init()
fld protected char[] chars
fld protected int current
fld protected int[] runLimit
fld protected int[] runStart
fld protected java.awt.Color[] colors
fld protected java.awt.Font[] fonts
innr public static AttributedCharacterIteratorImpl
meth public java.text.AttributedCharacterIterator iterator()
meth public void append(char,java.awt.Font,java.awt.Color)
meth public void append(char[],java.awt.Font,java.awt.Color)
supr java.lang.Object

CLSS public static org.openide.text.AttributedCharacters$AttributedCharacterIteratorImpl
 outer org.openide.text.AttributedCharacters
cons public init(char[],java.awt.Font[],java.awt.Color[],int[],int[])
fld protected char[] chars
fld protected int current
fld protected int[] runLimit
fld protected int[] runStart
fld protected java.awt.Color[] colors
fld protected java.awt.Font[] fonts
fld protected java.util.Set<java.text.AttributedCharacterIterator$Attribute> singleton
intf java.text.AttributedCharacterIterator
meth public char current()
meth public char first()
meth public char last()
meth public char next()
meth public char previous()
meth public char setIndex(int)
meth public int getBeginIndex()
meth public int getEndIndex()
meth public int getIndex()
meth public int getRunLimit()
meth public int getRunLimit(java.text.AttributedCharacterIterator$Attribute)
meth public int getRunLimit(java.util.Set<? extends java.text.AttributedCharacterIterator$Attribute>)
meth public int getRunStart()
meth public int getRunStart(java.text.AttributedCharacterIterator$Attribute)
meth public int getRunStart(java.util.Set<? extends java.text.AttributedCharacterIterator$Attribute>)
meth public java.lang.Object clone()
meth public java.lang.Object getAttribute(java.text.AttributedCharacterIterator$Attribute)
meth public java.util.Map<java.text.AttributedCharacterIterator$Attribute,java.lang.Object> getAttributes()
meth public java.util.Set<java.text.AttributedCharacterIterator$Attribute> getAllAttributeKeys()
supr java.lang.Object

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

CLSS public abstract interface static org.openide.text.CloneableEditorSupport$Env
 outer org.openide.text.CloneableEditorSupport
fld public final static java.lang.String PROP_TIME = "time"
intf org.openide.windows.CloneableOpenSupport$Env
meth public abstract java.io.InputStream inputStream() throws java.io.IOException
meth public abstract java.io.OutputStream outputStream() throws java.io.IOException
meth public abstract java.lang.String getMimeType()
meth public abstract java.util.Date getTime()

CLSS public abstract interface static org.openide.text.CloneableEditorSupport$Pane
 outer org.openide.text.CloneableEditorSupport
meth public abstract javax.swing.JEditorPane getEditorPane()
meth public abstract org.openide.windows.CloneableTopComponent getComponent()
meth public abstract void ensureVisible()
meth public abstract void updateName()

CLSS public abstract org.openide.text.CloneableEditorSupportRedirector
cons public init()
meth protected abstract org.openide.text.CloneableEditorSupport redirect(org.openide.util.Lookup)
supr java.lang.Object
hfds CHECKED

CLSS public abstract org.openide.text.DocumentLine
cons public init(org.openide.util.Lookup,org.openide.text.PositionRef)
fld protected org.openide.text.PositionRef pos
innr public abstract static Set
meth protected void addAnnotation(org.openide.text.Annotation)
meth protected void removeAnnotation(org.openide.text.Annotation)
meth public abstract void show(int,int)
 anno 0 java.lang.Deprecated()
meth public boolean equals(java.lang.Object)
meth public boolean isBreakpoint()
 anno 0 java.lang.Deprecated()
meth public int getLineNumber()
meth public int hashCode()
meth public java.lang.String getText()
meth public void markCurrentLine()
 anno 0 java.lang.Deprecated()
meth public void markError()
 anno 0 java.lang.Deprecated()
meth public void setBreakpoint(boolean)
 anno 0 java.lang.Deprecated()
meth public void unmarkCurrentLine()
 anno 0 java.lang.Deprecated()
meth public void unmarkError()
 anno 0 java.lang.Deprecated()
supr org.openide.text.Line
hfds assigned,breakpoint,current,dlEqualsCounter,docL,error,lineParts,listener,serialVersionUID
hcls FindAnnotationPosition,LR,Part

CLSS public abstract static org.openide.text.DocumentLine$Set
 outer org.openide.text.DocumentLine
cons public init(javax.swing.text.StyledDocument)
meth protected abstract org.openide.text.Line createLine(int)
meth public int getOriginalLineNumber(org.openide.text.Line)
meth public java.util.List<? extends org.openide.text.Line> getLines()
meth public org.openide.text.Line getCurrent(int)
meth public org.openide.text.Line getOriginal(int)
supr org.openide.text.Line$Set
hfds list,listener
hcls OffsetLineCreator

CLSS public org.openide.text.FilterDocument
cons public init(javax.swing.text.Document)
fld protected javax.swing.text.Document original
intf javax.swing.text.StyledDocument
meth public int getLength()
meth public java.awt.Color getBackground(javax.swing.text.AttributeSet)
meth public java.awt.Color getForeground(javax.swing.text.AttributeSet)
meth public java.awt.Font getFont(javax.swing.text.AttributeSet)
meth public java.lang.Object getProperty(java.lang.Object)
meth public java.lang.String getText(int,int) throws javax.swing.text.BadLocationException
meth public javax.swing.text.Element getCharacterElement(int)
meth public javax.swing.text.Element getDefaultRootElement()
meth public javax.swing.text.Element getParagraphElement(int)
meth public javax.swing.text.Element[] getRootElements()
meth public javax.swing.text.Position createPosition(int) throws javax.swing.text.BadLocationException
meth public javax.swing.text.Position getEndPosition()
meth public javax.swing.text.Position getStartPosition()
meth public javax.swing.text.Style addStyle(java.lang.String,javax.swing.text.Style)
meth public javax.swing.text.Style getLogicalStyle(int)
meth public javax.swing.text.Style getStyle(java.lang.String)
meth public void addDocumentListener(javax.swing.event.DocumentListener)
meth public void addUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public void getText(int,int,javax.swing.text.Segment) throws javax.swing.text.BadLocationException
meth public void insertString(int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth public void putProperty(java.lang.Object,java.lang.Object)
meth public void remove(int,int) throws javax.swing.text.BadLocationException
meth public void removeDocumentListener(javax.swing.event.DocumentListener)
meth public void removeStyle(java.lang.String)
meth public void removeUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public void render(java.lang.Runnable)
meth public void setCharacterAttributes(int,int,javax.swing.text.AttributeSet,boolean)
meth public void setLogicalStyle(int,javax.swing.text.Style)
meth public void setParagraphAttributes(int,int,javax.swing.text.AttributeSet,boolean)
supr java.lang.Object
hfds leaf

CLSS public org.openide.text.FilterStyledDocument
cons public init(javax.swing.text.StyledDocument)
meth public java.awt.Color getBackground(javax.swing.text.AttributeSet)
meth public java.awt.Color getForeground(javax.swing.text.AttributeSet)
meth public java.awt.Font getFont(javax.swing.text.AttributeSet)
meth public javax.swing.text.Element getCharacterElement(int)
meth public javax.swing.text.Element getParagraphElement(int)
meth public javax.swing.text.Style addStyle(java.lang.String,javax.swing.text.Style)
meth public javax.swing.text.Style getLogicalStyle(int)
meth public javax.swing.text.Style getStyle(java.lang.String)
meth public void removeStyle(java.lang.String)
meth public void setCharacterAttributes(int,int,javax.swing.text.AttributeSet,boolean)
meth public void setLogicalStyle(int,javax.swing.text.Style)
meth public void setParagraphAttributes(int,int,javax.swing.text.AttributeSet,boolean)
supr org.openide.text.FilterDocument

CLSS public abstract org.openide.text.IndentEngine
 anno 0 java.lang.Deprecated()
cons public init()
meth protected boolean acceptMimeType(java.lang.String)
meth public abstract int indentLine(javax.swing.text.Document,int)
meth public abstract int indentNewLine(javax.swing.text.Document,int)
meth public abstract java.io.Writer createWriter(javax.swing.text.Document,int,java.io.Writer)
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static java.util.Enumeration<? extends org.openide.text.IndentEngine> indentEngines()
meth public static org.openide.text.IndentEngine find(java.lang.String)
meth public static org.openide.text.IndentEngine find(javax.swing.text.Document)
meth public static org.openide.text.IndentEngine getDefault()
meth public static void register(java.lang.String,org.openide.text.IndentEngine)
 anno 0 java.lang.Deprecated()
supr org.openide.ServiceType
hfds INSTANCE,map,serialVersionUID
hcls Default

CLSS public abstract org.openide.text.Line
cons public init(java.lang.Object)
cons public init(org.openide.util.Lookup)
fld public final static int SHOW_GOTO = 2
 anno 0 java.lang.Deprecated()
fld public final static int SHOW_REUSE = 4
 anno 0 java.lang.Deprecated()
fld public final static int SHOW_REUSE_NEW = 5
 anno 0 java.lang.Deprecated()
fld public final static int SHOW_SHOW = 1
 anno 0 java.lang.Deprecated()
fld public final static int SHOW_TOFRONT = 3
 anno 0 java.lang.Deprecated()
fld public final static int SHOW_TRY_SHOW = 0
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String PROP_LINE_NUMBER = "lineNumber"
innr public abstract static Part
innr public abstract static Set
innr public final static !enum ShowOpenType
innr public final static !enum ShowVisibilityType
intf java.io.Serializable
meth public abstract boolean isBreakpoint()
 anno 0 java.lang.Deprecated()
meth public abstract int getLineNumber()
meth public abstract void markCurrentLine()
 anno 0 java.lang.Deprecated()
meth public abstract void markError()
 anno 0 java.lang.Deprecated()
meth public abstract void setBreakpoint(boolean)
 anno 0 java.lang.Deprecated()
meth public abstract void show(int,int)
 anno 0 java.lang.Deprecated()
meth public abstract void unmarkCurrentLine()
 anno 0 java.lang.Deprecated()
meth public abstract void unmarkError()
 anno 0 java.lang.Deprecated()
meth public boolean canBeMarkedCurrent(int,org.openide.text.Line)
 anno 0 java.lang.Deprecated()
meth public final org.openide.util.Lookup getLookup()
meth public java.lang.String getDisplayName()
meth public java.lang.String getText()
meth public org.openide.text.Line$Part createPart(int,int)
meth public void show(int)
 anno 0 java.lang.Deprecated()
meth public void show(org.openide.text.Line$ShowOpenType,org.openide.text.Line$ShowVisibilityType)
meth public void show(org.openide.text.Line$ShowOpenType,org.openide.text.Line$ShowVisibilityType,int)
supr org.openide.text.Annotatable
hfds LOG,dataObject,nullPart,serialVersionUID
hcls NullPart

CLSS public abstract static org.openide.text.Line$Part
 outer org.openide.text.Line
cons public init()
fld public final static java.lang.String PROP_COLUMN = "column"
fld public final static java.lang.String PROP_LENGTH = "length"
fld public final static java.lang.String PROP_LINE = "line"
meth public abstract int getColumn()
meth public abstract int getLength()
meth public abstract org.openide.text.Line getLine()
supr org.openide.text.Annotatable

CLSS public abstract static org.openide.text.Line$Set
 outer org.openide.text.Line
cons public init()
meth public abstract java.util.List<? extends org.openide.text.Line> getLines()
meth public abstract org.openide.text.Line getCurrent(int)
meth public abstract org.openide.text.Line getOriginal(int)
meth public final java.util.Date getDate()
meth public int getOriginalLineNumber(org.openide.text.Line)
supr java.lang.Object
hfds date,lineVector

CLSS public final static !enum org.openide.text.Line$ShowOpenType
 outer org.openide.text.Line
fld public final static org.openide.text.Line$ShowOpenType NONE
fld public final static org.openide.text.Line$ShowOpenType OPEN
fld public final static org.openide.text.Line$ShowOpenType REUSE
fld public final static org.openide.text.Line$ShowOpenType REUSE_NEW
meth public static org.openide.text.Line$ShowOpenType valueOf(java.lang.String)
meth public static org.openide.text.Line$ShowOpenType[] values()
supr java.lang.Enum<org.openide.text.Line$ShowOpenType>

CLSS public final static !enum org.openide.text.Line$ShowVisibilityType
 outer org.openide.text.Line
fld public final static org.openide.text.Line$ShowVisibilityType FOCUS
fld public final static org.openide.text.Line$ShowVisibilityType FRONT
fld public final static org.openide.text.Line$ShowVisibilityType NONE
meth public static org.openide.text.Line$ShowVisibilityType valueOf(java.lang.String)
meth public static org.openide.text.Line$ShowVisibilityType[] values()
supr java.lang.Enum<org.openide.text.Line$ShowVisibilityType>

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

CLSS public final org.openide.text.NbDocument$Colors
 anno 0 java.lang.Deprecated()
cons public init()
fld public final static java.lang.String PROP_BREAKPOINT = "NbBreakpointStyle"
fld public final static java.lang.String PROP_CURRENT = "NbCurrentStyle"
fld public final static java.lang.String PROP_ERROR = "NbErrorStyle"
meth public java.awt.Color getBreakpoint()
meth public java.awt.Color getCurrent()
meth public java.awt.Color getError()
meth public java.lang.String displayName()
meth public void setBreakpoint(java.awt.Color)
meth public void setCurrent(java.awt.Color)
meth public void setError(java.awt.Color)
supr org.openide.options.SystemOption
hfds serialVersionUID

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

CLSS public final org.openide.text.PositionBounds
cons public init(org.openide.text.PositionRef,org.openide.text.PositionRef)
intf java.io.Serializable
meth public java.lang.String getText() throws java.io.IOException,javax.swing.text.BadLocationException
meth public java.lang.String toString()
meth public org.openide.text.PositionBounds insertAfter(java.lang.String) throws java.io.IOException,javax.swing.text.BadLocationException
meth public org.openide.text.PositionRef getBegin()
meth public org.openide.text.PositionRef getEnd()
meth public void setText(java.lang.String) throws java.io.IOException,javax.swing.text.BadLocationException
supr java.lang.Object
hfds begin,end,serialVersionUID

CLSS public final org.openide.text.PositionRef
intf java.io.Serializable
intf javax.swing.text.Position
meth public int getColumn() throws java.io.IOException
meth public int getLine() throws java.io.IOException
meth public int getOffset()
meth public java.lang.String toString()
meth public javax.swing.text.Position getPosition() throws java.io.IOException
meth public javax.swing.text.Position$Bias getPositionBias()
meth public org.openide.text.CloneableEditorSupport getCloneableEditorSupport()
supr java.lang.Object
hfds LOG,insertAfter,kind,manager,serialVersionUID
hcls Manager

CLSS public final org.openide.text.PrintPreferences
innr public final static !enum Alignment
meth public static boolean getWrap()
meth public static float getLineAscentCorrection()
meth public static java.awt.Font getFooterFont()
meth public static java.awt.Font getHeaderFont()
meth public static java.awt.print.PageFormat getPageFormat(java.awt.print.PrinterJob)
meth public static java.lang.String getFooterFormat()
meth public static java.lang.String getHeaderFormat()
meth public static org.openide.text.PrintPreferences$Alignment getFooterAlignment()
meth public static org.openide.text.PrintPreferences$Alignment getHeaderAlignment()
meth public static void setFooterAlignment(org.openide.text.PrintPreferences$Alignment)
meth public static void setFooterFont(java.awt.Font)
meth public static void setFooterFormat(java.lang.String)
meth public static void setHeaderAlignment(org.openide.text.PrintPreferences$Alignment)
meth public static void setHeaderFont(java.awt.Font)
meth public static void setHeaderFormat(java.lang.String)
meth public static void setLineAscentCorrection(float)
meth public static void setPageFormat(java.awt.print.PageFormat)
meth public static void setWrap(boolean)
supr java.lang.Object
hfds DEFAULT_FONT_NAME,DEFAULT_FONT_SIZE,DEFAULT_FONT_STYLE,INSTANCE,PROP_FOOTER_ALIGNMENT,PROP_FOOTER_FONT_NAME,PROP_FOOTER_FONT_SIZE,PROP_FOOTER_FONT_STYLE,PROP_FOOTER_FORMAT,PROP_HEADER_ALIGNMENT,PROP_HEADER_FONT_NAME,PROP_HEADER_FONT_SIZE,PROP_HEADER_FONT_STYLE,PROP_HEADER_FORMAT,PROP_LINE_ASCENT_CORRECTION,PROP_PAGE_HEIGHT,PROP_PAGE_IMAGEABLEAREA_HEIGHT,PROP_PAGE_IMAGEABLEAREA_WIDTH,PROP_PAGE_IMAGEABLEAREA_X,PROP_PAGE_IMAGEABLEAREA_Y,PROP_PAGE_ORIENTATION,PROP_PAGE_WIDTH,PROP_WRAP

CLSS public final static !enum org.openide.text.PrintPreferences$Alignment
 outer org.openide.text.PrintPreferences
fld public final static org.openide.text.PrintPreferences$Alignment CENTER
fld public final static org.openide.text.PrintPreferences$Alignment LEFT
fld public final static org.openide.text.PrintPreferences$Alignment RIGHT
meth public static org.openide.text.PrintPreferences$Alignment valueOf(java.lang.String)
meth public static org.openide.text.PrintPreferences$Alignment[] values()
supr java.lang.Enum<org.openide.text.PrintPreferences$Alignment>

CLSS public final org.openide.text.PrintSettings
cons public init()
fld public final static int CENTER = 1
fld public final static int LEFT = 0
fld public final static int RIGHT = 2
fld public final static java.lang.String PROP_FOOTER_ALIGNMENT = "footerAlignment"
fld public final static java.lang.String PROP_FOOTER_FONT = "footerFont"
fld public final static java.lang.String PROP_FOOTER_FORMAT = "footerFormat"
fld public final static java.lang.String PROP_HEADER_ALIGNMENT = "headerAlignment"
fld public final static java.lang.String PROP_HEADER_FONT = "headerFont"
fld public final static java.lang.String PROP_HEADER_FORMAT = "headerFormat"
fld public final static java.lang.String PROP_LINE_ASCENT_CORRECTION = "lineAscentCorrection"
fld public final static java.lang.String PROP_PAGE_FORMAT = "pageFormat"
fld public final static java.lang.String PROP_WRAP = "wrap"
innr public static AlignmentEditor
innr public static PageFormatEditor
meth public boolean getWrap()
meth public float getLineAscentCorrection()
meth public int getFooterAlignment()
meth public int getHeaderAlignment()
meth public java.awt.Font getFooterFont()
meth public java.awt.Font getHeaderFont()
meth public java.awt.print.PageFormat getPageFormat()
 anno 0 java.lang.Deprecated()
meth public java.lang.String displayName()
meth public java.lang.String getFooterFormat()
meth public java.lang.String getHeaderFormat()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static java.awt.print.PageFormat getPageFormat(java.awt.print.PrinterJob)
meth public void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void setFooterAlignment(int)
meth public void setFooterFont(java.awt.Font)
meth public void setFooterFormat(java.lang.String)
meth public void setHeaderAlignment(int)
meth public void setHeaderFont(java.awt.Font)
meth public void setHeaderFormat(java.lang.String)
meth public void setLineAscentCorrection(float)
meth public void setPageFormat(java.awt.print.PageFormat)
meth public void setWrap(boolean)
meth public void writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr org.openide.options.ContextSystemOption
hfds HELP_ID,serialVersionUID

CLSS public static org.openide.text.PrintSettings$AlignmentEditor
 outer org.openide.text.PrintSettings
cons public init()
meth public java.lang.String getAsText()
meth public java.lang.String[] getTags()
meth public void setAsText(java.lang.String)
supr java.beans.PropertyEditorSupport
hfds sCENTER,sLEFT,sRIGHT,tags

CLSS public static org.openide.text.PrintSettings$PageFormatEditor
 outer org.openide.text.PrintSettings
cons public init()
meth public boolean supportsCustomEditor()
meth public java.awt.Component getCustomEditor()
meth public java.lang.String getAsText()
supr java.beans.PropertyEditorSupport

CLSS public org.openide.text.PrintSettingsBeanInfo
cons public init()
meth public java.awt.Image getIcon(int)
meth public java.beans.PropertyDescriptor[] getPropertyDescriptors()
supr java.beans.SimpleBeanInfo

CLSS public org.openide.text.StableCompoundEdit
cons public init()
intf javax.swing.undo.UndoableEdit
meth protected javax.swing.undo.UndoableEdit lastEdit()
meth public boolean addEdit(javax.swing.undo.UndoableEdit)
meth public boolean canRedo()
meth public boolean canUndo()
meth public boolean isInProgress()
meth public boolean isSignificant()
meth public boolean replaceEdit(javax.swing.undo.UndoableEdit)
meth public final java.util.List<javax.swing.undo.UndoableEdit> getEdits()
meth public java.lang.String getPresentationName()
meth public java.lang.String getRedoPresentationName()
meth public java.lang.String getUndoPresentationName()
meth public java.lang.String toString()
meth public void die()
meth public void end()
meth public void redo()
meth public void undo()
supr java.lang.Object
hfds ALIVE,HAS_BEEN_DONE,IN_PROGRESS,edits,statusBits

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

CLSS public abstract interface static org.openide.windows.CloneableOpenSupport$Env
 outer org.openide.windows.CloneableOpenSupport
fld public final static java.lang.String PROP_MODIFIED = "modified"
fld public final static java.lang.String PROP_VALID = "valid"
intf java.io.Serializable
meth public abstract boolean isModified()
meth public abstract boolean isValid()
meth public abstract org.openide.windows.CloneableOpenSupport findCloneableOpenSupport()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void addVetoableChangeListener(java.beans.VetoableChangeListener)
meth public abstract void markModified() throws java.io.IOException
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removeVetoableChangeListener(java.beans.VetoableChangeListener)
meth public abstract void unmarkModified()

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

