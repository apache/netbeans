#Signature file v4.1
#Version 1.64

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

CLSS public abstract interface java.awt.MenuContainer
meth public abstract boolean postEvent(java.awt.Event)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
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

CLSS public abstract interface !annotation java.beans.JavaBean
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String defaultEventSet()
meth public abstract !hasdefault java.lang.String defaultProperty()
meth public abstract !hasdefault java.lang.String description()

CLSS public abstract interface java.io.Serializable

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

CLSS public abstract interface javax.enterprise.deploy.spi.DeploymentManager
meth public abstract boolean isDConfigBeanVersionSupported(javax.enterprise.deploy.shared.DConfigBeanVersionType)
meth public abstract boolean isLocaleSupported(java.util.Locale)
meth public abstract boolean isRedeploySupported()
meth public abstract java.util.Locale getCurrentLocale()
meth public abstract java.util.Locale getDefaultLocale()
meth public abstract java.util.Locale[] getSupportedLocales()
meth public abstract javax.enterprise.deploy.shared.DConfigBeanVersionType getDConfigBeanVersion()
meth public abstract javax.enterprise.deploy.spi.DeploymentConfiguration createConfiguration(javax.enterprise.deploy.model.DeployableObject) throws javax.enterprise.deploy.spi.exceptions.InvalidModuleException
meth public abstract javax.enterprise.deploy.spi.TargetModuleID[] getAvailableModules(javax.enterprise.deploy.shared.ModuleType,javax.enterprise.deploy.spi.Target[]) throws javax.enterprise.deploy.spi.exceptions.TargetException
meth public abstract javax.enterprise.deploy.spi.TargetModuleID[] getNonRunningModules(javax.enterprise.deploy.shared.ModuleType,javax.enterprise.deploy.spi.Target[]) throws javax.enterprise.deploy.spi.exceptions.TargetException
meth public abstract javax.enterprise.deploy.spi.TargetModuleID[] getRunningModules(javax.enterprise.deploy.shared.ModuleType,javax.enterprise.deploy.spi.Target[]) throws javax.enterprise.deploy.spi.exceptions.TargetException
meth public abstract javax.enterprise.deploy.spi.Target[] getTargets()
meth public abstract javax.enterprise.deploy.spi.status.ProgressObject distribute(javax.enterprise.deploy.spi.Target[],java.io.File,java.io.File)
meth public abstract javax.enterprise.deploy.spi.status.ProgressObject distribute(javax.enterprise.deploy.spi.Target[],java.io.InputStream,java.io.InputStream)
meth public abstract javax.enterprise.deploy.spi.status.ProgressObject distribute(javax.enterprise.deploy.spi.Target[],javax.enterprise.deploy.shared.ModuleType,java.io.InputStream,java.io.InputStream)
meth public abstract javax.enterprise.deploy.spi.status.ProgressObject redeploy(javax.enterprise.deploy.spi.TargetModuleID[],java.io.File,java.io.File)
meth public abstract javax.enterprise.deploy.spi.status.ProgressObject redeploy(javax.enterprise.deploy.spi.TargetModuleID[],java.io.InputStream,java.io.InputStream)
meth public abstract javax.enterprise.deploy.spi.status.ProgressObject start(javax.enterprise.deploy.spi.TargetModuleID[])
meth public abstract javax.enterprise.deploy.spi.status.ProgressObject stop(javax.enterprise.deploy.spi.TargetModuleID[])
meth public abstract javax.enterprise.deploy.spi.status.ProgressObject undeploy(javax.enterprise.deploy.spi.TargetModuleID[])
meth public abstract void release()
meth public abstract void setDConfigBeanVersion(javax.enterprise.deploy.shared.DConfigBeanVersionType) throws javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException
meth public abstract void setLocale(java.util.Locale)

CLSS public abstract interface javax.enterprise.deploy.spi.factories.DeploymentFactory
meth public abstract boolean handlesURI(java.lang.String)
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getProductVersion()
meth public abstract javax.enterprise.deploy.spi.DeploymentManager getDeploymentManager(java.lang.String,java.lang.String,java.lang.String) throws javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException
meth public abstract javax.enterprise.deploy.spi.DeploymentManager getDisconnectedDeploymentManager(java.lang.String) throws javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException

CLSS public abstract interface javax.enterprise.deploy.spi.status.ProgressObject
meth public abstract boolean isCancelSupported()
meth public abstract boolean isStopSupported()
meth public abstract javax.enterprise.deploy.spi.TargetModuleID[] getResultTargetModuleIDs()
meth public abstract javax.enterprise.deploy.spi.status.ClientConfiguration getClientConfiguration(javax.enterprise.deploy.spi.TargetModuleID)
meth public abstract javax.enterprise.deploy.spi.status.DeploymentStatus getDeploymentStatus()
meth public abstract void addProgressListener(javax.enterprise.deploy.spi.status.ProgressListener)
meth public abstract void cancel() throws javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException
meth public abstract void removeProgressListener(javax.enterprise.deploy.spi.status.ProgressListener)
meth public abstract void stop() throws javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException

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

CLSS public abstract org.netbeans.modules.glassfish.eecommon.api.config.GlassfishConfiguration
cons protected init(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule,org.netbeans.modules.glassfish.eecommon.api.config.J2eeModuleHelper,org.netbeans.modules.glassfish.tooling.data.GlassFishVersion) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
cons protected init(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule,org.netbeans.modules.glassfish.tooling.data.GlassFishVersion) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
cons public init()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
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

CLSS public final org.netbeans.modules.glassfish.javaee.ApplicationScopedResourcesUtils
innr public abstract interface static JndiNameResolver
innr public static ResourceFileDescription
meth public static boolean isEarChild(java.io.File)
meth public static boolean isEarChild(org.netbeans.api.project.Project)
meth public static java.lang.String getJndiName(java.lang.String,org.netbeans.modules.glassfish.javaee.ApplicationScopedResourcesUtils$ResourceFileDescription)
meth public static org.netbeans.modules.glassfish.javaee.ApplicationScopedResourcesUtils$ResourceFileDescription checkNamespaces(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule,org.netbeans.modules.glassfish.javaee.ApplicationScopedResourcesUtils$ResourceFileDescription,java.lang.String)
supr java.lang.Object

CLSS public abstract interface static org.netbeans.modules.glassfish.javaee.ApplicationScopedResourcesUtils$JndiNameResolver
 outer org.netbeans.modules.glassfish.javaee.ApplicationScopedResourcesUtils
meth public abstract java.lang.String resolveJndiName(java.lang.String)

CLSS public static org.netbeans.modules.glassfish.javaee.ApplicationScopedResourcesUtils$ResourceFileDescription
 outer org.netbeans.modules.glassfish.javaee.ApplicationScopedResourcesUtils
cons public init(java.io.File,boolean,java.util.Set<java.lang.String>)
meth public boolean isIsApplicationScoped()
meth public java.io.File getFile()
meth public java.util.Set<java.lang.String> getNamespaces()
supr java.lang.Object
hfds file,isApplicationScoped,namespaces

CLSS public org.netbeans.modules.glassfish.javaee.Hk2Configuration
cons public init(javax.enterprise.deploy.model.DeployableObject)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule,org.netbeans.modules.glassfish.eecommon.api.config.J2eeModuleHelper,org.netbeans.modules.glassfish.tooling.data.GlassFishVersion) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
cons public init(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule,org.netbeans.modules.glassfish.tooling.data.GlassFishVersion) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
intf javax.enterprise.deploy.spi.DeploymentConfiguration
meth public boolean supportsCreateDatasource()
meth public boolean supportsCreateMessageDestination()
meth public java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.Datasource> getDatasources() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
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
supr org.netbeans.modules.glassfish.eecommon.api.config.GlassfishConfiguration

CLSS public org.netbeans.modules.glassfish.javaee.Hk2DeploymentFactory
intf javax.enterprise.deploy.spi.factories.DeploymentFactory
meth public boolean handlesURI(java.lang.String)
meth public java.lang.String getDisplayName()
meth public java.lang.String getProductVersion()
meth public javax.enterprise.deploy.spi.DeploymentManager getDeploymentManager(java.lang.String,java.lang.String,java.lang.String) throws javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException
meth public javax.enterprise.deploy.spi.DeploymentManager getDisconnectedDeploymentManager(java.lang.String) throws javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException
meth public static javax.enterprise.deploy.spi.factories.DeploymentFactory createEe6()
meth public static javax.enterprise.deploy.spi.factories.DeploymentFactory createEe7()
meth public static javax.enterprise.deploy.spi.factories.DeploymentFactory createEe8()
meth public static javax.enterprise.deploy.spi.factories.DeploymentFactory createJakartaEe10()
meth public static javax.enterprise.deploy.spi.factories.DeploymentFactory createJakartaEe8()
meth public static javax.enterprise.deploy.spi.factories.DeploymentFactory createJakartaEe9()
meth public static javax.enterprise.deploy.spi.factories.DeploymentFactory createJakartaEe91()
supr java.lang.Object
hfds displayName,ee6Instance,ee7Instance,ee8Instance,jakartaee10Instance,jakartaee8Instance,jakartaee91Instance,jakartaee9Instance,preludeInstance,su,uriFragments,version

CLSS public org.netbeans.modules.glassfish.javaee.Hk2DeploymentManager
cons public init(java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.glassfish.spi.ServerUtilities)
intf org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentManager2
meth public boolean isDConfigBeanVersionSupported(javax.enterprise.deploy.shared.DConfigBeanVersionType)
meth public boolean isLocal()
meth public boolean isLocaleSupported(java.util.Locale)
meth public boolean isRedeploySupported()
meth public final java.lang.String getUri()
meth public java.util.Locale getCurrentLocale()
meth public java.util.Locale getDefaultLocale()
meth public java.util.Locale[] getSupportedLocales()
meth public javax.enterprise.deploy.shared.DConfigBeanVersionType getDConfigBeanVersion()
meth public javax.enterprise.deploy.spi.DeploymentConfiguration createConfiguration(javax.enterprise.deploy.model.DeployableObject) throws javax.enterprise.deploy.spi.exceptions.InvalidModuleException
meth public javax.enterprise.deploy.spi.TargetModuleID[] getAvailableModules(javax.enterprise.deploy.shared.ModuleType,javax.enterprise.deploy.spi.Target[]) throws javax.enterprise.deploy.spi.exceptions.TargetException
meth public javax.enterprise.deploy.spi.TargetModuleID[] getNonRunningModules(javax.enterprise.deploy.shared.ModuleType,javax.enterprise.deploy.spi.Target[]) throws javax.enterprise.deploy.spi.exceptions.TargetException
meth public javax.enterprise.deploy.spi.TargetModuleID[] getRunningModules(javax.enterprise.deploy.shared.ModuleType,javax.enterprise.deploy.spi.Target[]) throws javax.enterprise.deploy.spi.exceptions.TargetException
meth public javax.enterprise.deploy.spi.Target[] getTargets()
meth public javax.enterprise.deploy.spi.status.ProgressObject distribute(javax.enterprise.deploy.spi.Target[],java.io.File,java.io.File)
meth public javax.enterprise.deploy.spi.status.ProgressObject distribute(javax.enterprise.deploy.spi.Target[],java.io.InputStream,java.io.InputStream)
meth public javax.enterprise.deploy.spi.status.ProgressObject distribute(javax.enterprise.deploy.spi.Target[],javax.enterprise.deploy.shared.ModuleType,java.io.InputStream,java.io.InputStream)
meth public javax.enterprise.deploy.spi.status.ProgressObject distribute(javax.enterprise.deploy.spi.Target[],org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentContext)
meth public javax.enterprise.deploy.spi.status.ProgressObject redeploy(javax.enterprise.deploy.spi.TargetModuleID[],java.io.File,java.io.File)
meth public javax.enterprise.deploy.spi.status.ProgressObject redeploy(javax.enterprise.deploy.spi.TargetModuleID[],java.io.InputStream,java.io.InputStream)
meth public javax.enterprise.deploy.spi.status.ProgressObject redeploy(javax.enterprise.deploy.spi.TargetModuleID[],org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentContext)
meth public javax.enterprise.deploy.spi.status.ProgressObject start(javax.enterprise.deploy.spi.TargetModuleID[])
meth public javax.enterprise.deploy.spi.status.ProgressObject stop(javax.enterprise.deploy.spi.TargetModuleID[])
meth public javax.enterprise.deploy.spi.status.ProgressObject undeploy(javax.enterprise.deploy.spi.TargetModuleID[])
meth public org.netbeans.api.server.ServerInstance getServerInstance()
meth public org.netbeans.modules.glassfish.javaee.ide.Hk2PluginProperties getProperties()
meth public org.netbeans.modules.glassfish.spi.GlassfishModule getCommonServerSupport()
meth public org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties getInstanceProperties()
meth public static java.lang.String getTargetFromUri(java.lang.String)
meth public void release()
meth public void setDConfigBeanVersion(javax.enterprise.deploy.shared.DConfigBeanVersionType) throws javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException
meth public void setLocale(java.util.Locale)
supr java.lang.Object
hfds instanceProperties,pluginProperties,serverInstance,su,uri

CLSS public org.netbeans.modules.glassfish.javaee.Hk2JavaEEPlatformFactory
cons public init()
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl getJ2eePlatformImpl(javax.enterprise.deploy.spi.DeploymentManager)
meth public static org.netbeans.modules.glassfish.javaee.Hk2JavaEEPlatformFactory getFactory()
supr org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformFactory
hfds V3_LOOKUP_KEY,V4_LOOKUP_KEY,V510_LOOKUP_KEY,V5_LOOKUP_KEY,V610_LOOKUP_KEY,V6_LOOKUP_KEY,V7_LOOKUP_KEY,instance

CLSS public org.netbeans.modules.glassfish.javaee.Hk2JavaEEPlatformImpl
cons public init(org.netbeans.modules.glassfish.javaee.Hk2DeploymentManager,java.lang.String[],org.netbeans.api.j2ee.core.Profile[],org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type[],java.lang.String,java.lang.String,java.lang.String)
meth public boolean isToolSupported(java.lang.String)
meth public java.awt.Image getIcon()
meth public java.io.File getDomainHome()
meth public java.io.File getMiddlewareHome()
meth public java.io.File getServerHome()
meth public java.io.File[] getPlatformRoots()
meth public java.io.File[] getToolClasspathEntries(java.lang.String)
meth public java.lang.String getDisplayName()
meth public java.lang.String getToolProperty(java.lang.String,java.lang.String)
meth public java.util.Set getSupportedJavaPlatformVersions()
meth public java.util.Set<org.netbeans.api.j2ee.core.Profile> getSupportedProfiles()
meth public java.util.Set<org.netbeans.api.j2ee.core.Profile> getSupportedProfiles(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type)
meth public java.util.Set<org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type> getSupportedTypes()
meth public org.netbeans.api.java.platform.JavaPlatform getJavaPlatform()
meth public org.netbeans.modules.glassfish.javaee.Hk2LibraryProvider getLibraryProvider()
meth public org.netbeans.spi.project.libraries.LibraryImplementation[] getLibraries()
meth public org.openide.util.Lookup getLookup()
meth public static java.lang.String[] nbJavaSEProfiles(java.util.Set<org.netbeans.modules.glassfish.tooling.server.config.JavaSEPlatform>)
meth public static org.netbeans.api.j2ee.core.Profile[] nbJavaEEProfiles(java.util.Set<org.netbeans.modules.glassfish.tooling.server.config.JavaEEProfile>)
meth public static org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type[] nbModuleTypes(java.util.Set<org.netbeans.modules.glassfish.tooling.server.config.ModuleType>)
meth public void notifyLibrariesChanged()
supr org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl2
hfds EMBEDDED_EJB_CONTAINER_PATH,GFv3_ACC_XML,GFv4_ACC_XML,GFv5_ACC_XML,KEYSTORE_LOCATION,PERSISTENCE_PROV_ECLIPSELINK,TOOL_APPCLIENTRUNTIME,TOOL_JAXWSTESTER,TOOL_JSR109,TOOL_KEYSTORE,TOOL_KEYSTORECLIENT,TOOL_TRUSTSTORE,TOOL_TRUSTSTORECLIENT,TOOL_WSCOMPILE,TOOL_WSGEN,TOOL_WSIMPORT,TOOL_WSIT,TRUSTSTORE_LOCATION,displayName,dm,fcl,lib,libInitThread,libraries,libraryName,libraryProvider,lkp,lookupKey,platforms,profiles,types
hcls JaxRsStackSupportImpl,RegistrationHandler

CLSS public org.netbeans.modules.glassfish.javaee.Hk2JaxRpcStack
cons public init(java.lang.String)
innr protected JaxRpcTool
intf org.netbeans.modules.websvc.wsstack.spi.WSStackImplementation<org.netbeans.modules.javaee.specs.support.api.JaxRpc>
meth protected boolean isMetroInstalled()
meth protected java.io.File getJarName(java.lang.String,java.lang.String)
meth public boolean isFeatureSupported(org.netbeans.modules.websvc.wsstack.api.WSStack$Feature)
meth public org.netbeans.modules.javaee.specs.support.api.JaxRpc get()
meth public org.netbeans.modules.websvc.wsstack.api.WSStackVersion getVersion()
meth public org.netbeans.modules.websvc.wsstack.api.WSTool getWSTool(org.netbeans.modules.websvc.wsstack.api.WSStack$Tool)
supr java.lang.Object
hfds GFV3_MODULES_DIR_NAME,METRO_LIBRARIES,gfRootStr,jaxRpc
hcls VersionFilter

CLSS protected org.netbeans.modules.glassfish.javaee.Hk2JaxRpcStack$JaxRpcTool
 outer org.netbeans.modules.glassfish.javaee.Hk2JaxRpcStack
intf org.netbeans.modules.websvc.wsstack.spi.WSToolImplementation
meth public java.lang.String getName()
meth public java.net.URL[] getLibraries()
supr java.lang.Object
hfds tool

CLSS public org.netbeans.modules.glassfish.javaee.Hk2JaxWsStack
cons public init(java.lang.String,org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl)
innr protected JaxWsTool
intf org.netbeans.modules.websvc.wsstack.spi.WSStackImplementation<org.netbeans.modules.javaee.specs.support.api.JaxWs>
meth protected boolean isMetroInstalled()
meth public boolean isFeatureSupported(org.netbeans.modules.websvc.wsstack.api.WSStack$Feature)
meth public org.netbeans.modules.javaee.specs.support.api.JaxWs get()
meth public org.netbeans.modules.websvc.wsstack.api.WSStackVersion getVersion()
meth public org.netbeans.modules.websvc.wsstack.api.WSTool getWSTool(org.netbeans.modules.websvc.wsstack.api.WSStack$Tool)
meth public static java.io.File getWsJarName(java.lang.String,java.lang.String)
supr java.lang.Object
hfds GFV3_MODULES_DIR_NAME,METRO_LIBRARIES,gfRootStr,jaxWs,platform
hcls VersionFilter

CLSS protected org.netbeans.modules.glassfish.javaee.Hk2JaxWsStack$JaxWsTool
 outer org.netbeans.modules.glassfish.javaee.Hk2JaxWsStack
intf org.netbeans.modules.websvc.wsstack.spi.WSToolImplementation
meth public java.lang.String getName()
meth public java.net.URL[] getLibraries()
supr java.lang.Object
hfds tool

CLSS public org.netbeans.modules.glassfish.javaee.Hk2JpaSupportImpl
intf org.netbeans.modules.javaee.specs.support.spi.JpaSupportImplementation
meth public java.util.Set<org.netbeans.modules.javaee.specs.support.api.JpaProvider> getProviders()
meth public org.netbeans.modules.javaee.specs.support.api.JpaProvider getDefaultProvider()
supr java.lang.Object
hfds JPA_PROVIDER,defaultProvider,instance,jpaSupport,providers
hcls JpaSupportVector

CLSS public org.netbeans.modules.glassfish.javaee.Hk2LibraryProvider
meth public java.lang.String getJavaEEName()
meth public java.lang.String getJaxRsName()
meth public java.lang.String getJerseyName()
meth public java.util.List<java.net.URL> getJavaEEClassPathURLs()
meth public java.util.List<java.net.URL> getJaxRsClassPathURLs()
meth public java.util.List<java.net.URL> getJerseyClassPathURLs()
meth public org.netbeans.api.project.libraries.Library getJavaEELibrary()
meth public org.netbeans.api.project.libraries.Library getJaxRsLibrary()
meth public org.netbeans.api.project.libraries.Library getJerseyLibrary()
meth public static org.netbeans.modules.glassfish.javaee.Hk2LibraryProvider getProvider(org.netbeans.modules.glassfish.tooling.data.GlassFishServer)
meth public void setJavaEELibraryImplementation(org.netbeans.spi.project.libraries.LibraryImplementation,java.lang.String)
meth public void setJaxRsLibraryImplementation(org.netbeans.spi.project.libraries.LibraryImplementation,java.lang.String)
meth public void setJerseyImplementation(org.netbeans.spi.project.libraries.LibraryImplementation,java.lang.String)
supr java.lang.Object
hfds JAVAEE_DOC_CODE_BASE,JAVAEE_NAME_SUFFIX,JAVAEE_PATTERN,JAXRS_NAME_SUFFIX,JAXRS_PATTERN,JERSEY_NAME_SUFFIX,JERSEY_PATTERN,PROVIDER_TYPE,builder,javaEEName,jaxRsName,jerseyName,providers,server,serverHome,serverName

CLSS public org.netbeans.modules.glassfish.javaee.Hk2MessageDestinationManager
cons public init(org.netbeans.modules.glassfish.javaee.Hk2DeploymentManager)
fld public final static java.lang.String JMS_PREFIX = "jms/"
fld public final static java.lang.String QUEUE = "javax.jms.Queue"
fld public final static java.lang.String QUEUE_CNTN_FACTORY = "javax.jms.QueueConnectionFactory"
fld public final static java.lang.String QUEUE_PROP = "PhysicalQueue"
fld public final static java.lang.String TOPIC = "javax.jms.Topic"
fld public final static java.lang.String TOPIC_CNTN_FACTORY = "javax.jms.TopicConnectionFactory"
fld public final static java.lang.String TOPIC_PROP = "PhysicalTopic"
intf org.netbeans.modules.j2ee.deployment.plugins.spi.MessageDestinationDeployment
meth public java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.MessageDestination> getMessageDestinations() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public static java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.MessageDestination> getMessageDestinations(java.io.File,java.lang.String)
meth public static org.netbeans.modules.j2ee.deployment.common.api.MessageDestination createMessageDestination(java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type,java.io.File,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public static void createAdminObject(java.io.File,java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type) throws java.io.IOException
meth public static void createConnector(java.io.File,java.lang.String,java.lang.String) throws java.io.IOException
meth public static void createConnectorConnectionPool(java.io.File,java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type) throws java.io.IOException
meth public void deployMessageDestinations(java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.MessageDestination>) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
supr java.lang.Object
hfds AO_TAG_1,AO_TAG_2,AO_TAG_3,ATTR_CONN_DEFINITION,ATTR_JNDINAME,ATTR_POOLNAME,ATTR_POOL_NAME,ATTR_RESTYPE,CONNECTOR_POOL_TAG_1,CONNECTOR_POOL_TAG_2,CONNECTOR_TAG_1,CONNECTOR_TAG_2,DOMAIN_XML_PATH,PROP_NAME,dm
hcls AdminObjectReader,AdminObjectResource,ConnectorPool,ConnectorPoolFinder,DuplicateAOFinder,DuplicateConnectorFinder

CLSS public org.netbeans.modules.glassfish.javaee.Hk2ModuleConfigFactory
cons public init()
intf org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfigurationFactory2
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration create(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration create(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public org.netbeans.modules.glassfish.javaee.Hk2OptionalFactory
cons protected init(javax.enterprise.deploy.spi.factories.DeploymentFactory,org.netbeans.modules.glassfish.spi.ServerUtilities,boolean)
meth public boolean isCommonUIRequired()
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.AntDeploymentProvider getAntDeploymentProvider(javax.enterprise.deploy.spi.DeploymentManager)
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.DatasourceManager getDatasourceManager(javax.enterprise.deploy.spi.DeploymentManager)
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet getFindJSPServlet(javax.enterprise.deploy.spi.DeploymentManager)
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment getIncrementalDeployment(javax.enterprise.deploy.spi.DeploymentManager)
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.JDBCDriverDeployer getJDBCDriverDeployer(javax.enterprise.deploy.spi.DeploymentManager)
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.MessageDestinationDeployment getMessageDestinationDeployment(javax.enterprise.deploy.spi.DeploymentManager)
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.ServerInstanceDescriptor getServerInstanceDescriptor(javax.enterprise.deploy.spi.DeploymentManager)
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer getStartServer(javax.enterprise.deploy.spi.DeploymentManager)
meth public org.openide.WizardDescriptor$InstantiatingIterator getAddInstanceIterator()
meth public static org.netbeans.modules.glassfish.javaee.Hk2OptionalFactory createEe6()
meth public static org.netbeans.modules.glassfish.javaee.Hk2OptionalFactory createEe7()
meth public static org.netbeans.modules.glassfish.javaee.Hk2OptionalFactory createEe8()
meth public static org.netbeans.modules.glassfish.javaee.Hk2OptionalFactory createJakartaEe10()
meth public static org.netbeans.modules.glassfish.javaee.Hk2OptionalFactory createJakartaEe8()
meth public static org.netbeans.modules.glassfish.javaee.Hk2OptionalFactory createJakartaEe9()
meth public static org.netbeans.modules.glassfish.javaee.Hk2OptionalFactory createJakartaEe91()
supr org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory
hfds commonUtilities,df,hasWizard
hcls J2eeInstantiatingIterator

CLSS public org.netbeans.modules.glassfish.javaee.Hk2ServerInstanceDescriptor
cons public init(org.netbeans.modules.glassfish.javaee.Hk2DeploymentManager)
intf org.netbeans.modules.j2ee.deployment.plugins.spi.ServerInstanceDescriptor
meth public boolean isLocal()
meth public int getHttpPort()
meth public java.lang.String getHostname()
supr java.lang.Object
hfds commonSupport

CLSS public org.netbeans.modules.glassfish.javaee.Hk2StartServer
cons public init(javax.enterprise.deploy.spi.DeploymentManager)
intf javax.enterprise.deploy.spi.status.ProgressObject
meth public boolean isAlsoTargetServer(javax.enterprise.deploy.spi.Target)
meth public boolean isCancelSupported()
meth public boolean isDebuggable(javax.enterprise.deploy.spi.Target)
meth public boolean isProfiling(javax.enterprise.deploy.spi.Target)
meth public boolean isRunning()
meth public boolean isStopSupported()
meth public boolean needsRestart(javax.enterprise.deploy.spi.Target)
meth public boolean needsStartForAdminConfig()
meth public boolean needsStartForConfigure()
meth public boolean needsStartForTargetList()
meth public boolean supportsStartDebugging(javax.enterprise.deploy.spi.Target)
meth public boolean supportsStartDeploymentManager()
meth public boolean supportsStartProfiling(javax.enterprise.deploy.spi.Target)
meth public javax.enterprise.deploy.spi.TargetModuleID[] getResultTargetModuleIDs()
meth public javax.enterprise.deploy.spi.status.ClientConfiguration getClientConfiguration(javax.enterprise.deploy.spi.TargetModuleID)
meth public javax.enterprise.deploy.spi.status.DeploymentStatus getDeploymentStatus()
meth public javax.enterprise.deploy.spi.status.ProgressObject startDebugging(javax.enterprise.deploy.spi.Target)
meth public javax.enterprise.deploy.spi.status.ProgressObject startDeploymentManager()
meth public javax.enterprise.deploy.spi.status.ProgressObject startProfiling(javax.enterprise.deploy.spi.Target)
meth public javax.enterprise.deploy.spi.status.ProgressObject stopDeploymentManager()
meth public org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties getInstanceProperties()
meth public org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo getDebugInfo(javax.enterprise.deploy.spi.Target)
meth public void addProgressListener(javax.enterprise.deploy.spi.status.ProgressListener)
meth public void cancel() throws javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException
meth public void fireHandleProgressEvent(javax.enterprise.deploy.spi.TargetModuleID,javax.enterprise.deploy.spi.status.DeploymentStatus)
meth public void removeProgressListener(javax.enterprise.deploy.spi.status.ProgressListener)
meth public void stop() throws javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException
supr org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer
hfds deploymentStatus,dm,ip,lastEvent,listeners,serverName

CLSS public org.netbeans.modules.glassfish.javaee.JDBCDriverDeployerImpl
cons public init(org.netbeans.modules.glassfish.javaee.Hk2DeploymentManager,org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory)
intf org.netbeans.modules.j2ee.deployment.plugins.spi.JDBCDriverDeployer
meth public boolean supportsDeployJDBCDrivers(javax.enterprise.deploy.spi.Target)
meth public javax.enterprise.deploy.spi.status.ProgressObject deployJDBCDrivers(javax.enterprise.deploy.spi.Target,java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.Datasource>)
supr java.lang.Object
hfds commonSupport,dm,driverLoc,isLocal

CLSS public org.netbeans.modules.glassfish.javaee.JavaEEDecoratorFactory
fld public final static org.netbeans.modules.glassfish.spi.Decorator ADMINOBJECT_RESOURCE
fld public final static org.netbeans.modules.glassfish.spi.Decorator APPCLIENT
fld public final static org.netbeans.modules.glassfish.spi.Decorator CONNECTION_POOLS
fld public final static org.netbeans.modules.glassfish.spi.Decorator CONNECTOR
fld public final static org.netbeans.modules.glassfish.spi.Decorator CONNECTORS_FOLDER
fld public final static org.netbeans.modules.glassfish.spi.Decorator CONN_CONNECTION_POOL
fld public final static org.netbeans.modules.glassfish.spi.Decorator CONN_RESOURCE
fld public final static org.netbeans.modules.glassfish.spi.Decorator DISABLED_APPCLIENT
fld public final static org.netbeans.modules.glassfish.spi.Decorator DISABLED_CONNECTOR
fld public final static org.netbeans.modules.glassfish.spi.Decorator DISABLED_EJB_JAR
fld public final static org.netbeans.modules.glassfish.spi.Decorator DISABLED_J2EE_APPLICATION
fld public final static org.netbeans.modules.glassfish.spi.Decorator DISABLED_WEB_APPLICATION
fld public final static org.netbeans.modules.glassfish.spi.Decorator EJB_JAR
fld public final static org.netbeans.modules.glassfish.spi.Decorator J2EE_APPLICATION
fld public final static org.netbeans.modules.glassfish.spi.Decorator J2EE_APPLICATION_FOLDER
fld public final static org.netbeans.modules.glassfish.spi.Decorator JAVAMAIL_FOLDER
fld public final static org.netbeans.modules.glassfish.spi.Decorator JAVAMAIL_RESOURCE
fld public final static org.netbeans.modules.glassfish.spi.Decorator JDBC_FOLDER
fld public final static org.netbeans.modules.glassfish.spi.Decorator JDBC_MANAGED_DATASOURCES
fld public final static org.netbeans.modules.glassfish.spi.Decorator JDBC_NATIVE_DATASOURCES
fld public final static org.netbeans.modules.glassfish.spi.Decorator WEB_APPLICATION
intf org.netbeans.modules.glassfish.spi.DecoratorFactory
meth public boolean isTypeSupported(java.lang.String)
meth public java.util.Map<java.lang.String,org.netbeans.modules.glassfish.spi.Decorator> getAllDecorators()
meth public org.netbeans.modules.glassfish.spi.Decorator getDecorator(java.lang.String)
meth public static org.netbeans.modules.glassfish.spi.DecoratorFactory getDefault()
supr java.lang.Object
hfds APPCLIENT_ICON,CONNECTOR_ICON,JAVAMAIL_ICON,JDBC_RESOURCE_ICON,decoratorMap,singleton

CLSS public org.netbeans.modules.glassfish.javaee.JavaEEServerModule
intf org.netbeans.modules.glassfish.spi.RecognizerCookie
intf org.netbeans.modules.glassfish.spi.RemoveCookie
intf org.openide.util.Lookup$Provider
meth public java.util.Collection<? extends org.netbeans.modules.glassfish.spi.Recognizer> getRecognizers()
meth public org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties getInstanceProperties()
meth public org.openide.util.Lookup getLookup()
meth public void removeInstance(java.lang.String)
supr java.lang.Object
hfds instanceProperties,logSupport,lookup

CLSS public org.netbeans.modules.glassfish.javaee.JavaEEServerModuleFactory
intf org.netbeans.modules.glassfish.spi.GlassfishModuleFactory
meth public boolean isModuleSupported(java.lang.String,java.util.Properties)
meth public java.lang.Object createModule(org.openide.util.Lookup)
meth public static org.netbeans.modules.glassfish.spi.GlassfishModuleFactory getDefault()
supr java.lang.Object
hfds CLASSPATH_VOLUME,CLASS_LIBRARY_TYPE,COMET_JAR_2_MATCHER,COMET_JAR_MATCHER,COMET_LIB,COMET_LIB_2,ECLIPSE_LINK_LIB,ECLIPSE_LINK_LIB_2,EL_CORE_JAR_MATCHER,GRIZZLY_OPTIONAL_JAR_MATCHER,JAKARTA_EE_10_JAVADOC,JAKARTA_EE_8_JAVADOC,JAKARTA_EE_9_JAVADOC,JAVADOC_VOLUME,JAVA_EE_5_LIB,JAVA_EE_6_LIB,JAVA_EE_JAVADOC,JAXRS_LIBRARIES,JAXRS_LIBRARIES_31,JERSEY_GF_SERVER,LOG,PERSISTENCE_API_JAR_MATCHER_1,PERSISTENCE_API_JAR_MATCHER_2,PERSISTENCE_JAVADOC,RP,SOURCE_VOLUME,singleton
hcls InitializeLibrary

CLSS public org.netbeans.modules.glassfish.javaee.ModuleConfigurationImpl
intf org.netbeans.modules.j2ee.deployment.plugins.spi.config.ContextRootConfiguration
intf org.netbeans.modules.j2ee.deployment.plugins.spi.config.DatasourceConfiguration
intf org.netbeans.modules.j2ee.deployment.plugins.spi.config.DeploymentPlanConfiguration
intf org.netbeans.modules.j2ee.deployment.plugins.spi.config.EjbResourceConfiguration
intf org.netbeans.modules.j2ee.deployment.plugins.spi.config.MessageDestinationConfiguration
intf org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration
meth public boolean supportsCreateDatasource()
meth public boolean supportsCreateMessageDestination()
meth public java.lang.String findDatasourceJndiName(java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public java.lang.String findDatasourceJndiNameForEjb(java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public java.lang.String findJndiNameForEjb(java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public java.lang.String findMessageDestinationName(java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public java.lang.String getContextRoot() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.Datasource> getDatasources() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.MessageDestination> getMessageDestinations() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public org.netbeans.modules.j2ee.deployment.common.api.Datasource createDatasource(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException,org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException
meth public org.netbeans.modules.j2ee.deployment.common.api.MessageDestination createMessageDestination(java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule getJ2eeModule()
meth public org.openide.util.Lookup getLookup()
meth public static org.netbeans.modules.glassfish.javaee.ModuleConfigurationImpl get(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule)
meth public void bindDatasourceReference(java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public void bindDatasourceReferenceForEjb(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public void bindEjbReference(java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public void bindEjbReferenceForEjb(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public void bindMdbToMessageDestination(java.lang.String,java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public void bindMessageDestinationReference(java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public void bindMessageDestinationReferenceForEjb(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public void dispose()
meth public void save(java.io.OutputStream) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public void setContextRoot(java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
supr java.lang.Object
hfds config,configs,lookup,module

CLSS public org.netbeans.modules.glassfish.javaee.ResourceModifier
cons public init()
meth public static void appendAttr(java.lang.StringBuilder,java.lang.String,java.lang.String,boolean)
meth public static void appendProperty(java.lang.StringBuilder,java.lang.String,java.lang.String,boolean)
meth public static void appendResource(java.io.File,java.lang.String) throws java.io.IOException
supr java.lang.Object
hfds GF_RESOURCES_XML_HEADER,SUN_RESOURCES_XML_FOOTER,SUN_RESOURCES_XML_HEADER

CLSS public org.netbeans.modules.glassfish.javaee.ResourceRegistrationHelper
innr public static ResourceFinder
meth public static java.util.Map<java.lang.String,java.lang.String> getResourceData(java.lang.String,org.netbeans.modules.glassfish.javaee.Hk2DeploymentManager)
meth public static void deployResources(java.io.File,org.netbeans.modules.glassfish.javaee.Hk2DeploymentManager)
meth public static void putResourceData(java.util.Map<java.lang.String,java.lang.String>,org.netbeans.modules.glassfish.javaee.Hk2DeploymentManager)
supr java.lang.Object
hfds TIMEOUT

CLSS public static org.netbeans.modules.glassfish.javaee.ResourceRegistrationHelper$ResourceFinder
 outer org.netbeans.modules.glassfish.javaee.ResourceRegistrationHelper
cons public init(java.lang.String)
meth public java.util.List<java.lang.String> getResourceNames()
meth public java.util.Map<java.lang.String,java.util.Map<java.lang.String,java.lang.String>> getResourceData()
meth public void endNode(java.lang.String) throws org.xml.sax.SAXException
meth public void readAttributes(java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public void readChildren(java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
supr org.netbeans.modules.glassfish.common.parser.TreeParser$NodeReader
hfds nameKey,properties,resourceData

CLSS public org.netbeans.modules.glassfish.javaee.RunTimeDDCatalog
cons public init()
fld public final static java.lang.String APPCLIENT_10_ID = "SCHEMA:https://jakarta.ee/xml/ns/jakartaee/application-client_10.xsd"
fld public final static java.lang.String APPCLIENT_1_4_ID = "SCHEMA:http://java.sun.com/xml/ns/j2ee/application-client_1_4.xsd"
fld public final static java.lang.String APPCLIENT_5_ID = "SCHEMA:http://java.sun.com/xml/ns/javaee/application-client_5.xsd"
fld public final static java.lang.String APPCLIENT_6_ID = "SCHEMA:http://java.sun.com/xml/ns/javaee/application-client_6.xsd"
fld public final static java.lang.String APPCLIENT_7_ID = "SCHEMA:http://xmlns.jcp.org/xml/ns/javaee/application-client_7.xsd"
fld public final static java.lang.String APPCLIENT_8_ID = "SCHEMA:http://xmlns.jcp.org/xml/ns/javaee/application-client_8.xsd"
fld public final static java.lang.String APPCLIENT_9_ID = "SCHEMA:https://jakarta.ee/xml/ns/jakartaee/application-client_9.xsd"
fld public final static java.lang.String APP_10_ID = "SCHEMA:https://jakarta.ee/xml/ns/jakartaee/application_10.xsd"
fld public final static java.lang.String APP_1_4_ID = "SCHEMA:http://java.sun.com/xml/ns/j2ee/application_1_4.xsd"
fld public final static java.lang.String APP_5_ID = "SCHEMA:http://java.sun.com/xml/ns/javaee/application_5.xsd"
fld public final static java.lang.String APP_6_ID = "SCHEMA:http://java.sun.com/xml/ns/javaee/application_6.xsd"
fld public final static java.lang.String APP_7_ID = "SCHEMA:http://xmlns.jcp.org/xml/ns/javaee/application_7.xsd"
fld public final static java.lang.String APP_8_ID = "SCHEMA:http://xmlns.jcp.org/xml/ns/javaee/application_8.xsd"
fld public final static java.lang.String APP_9_ID = "SCHEMA:https://jakarta.ee/xml/ns/jakartaee/application_9.xsd"
fld public final static java.lang.String EJBJAR_2_1_ID = "SCHEMA:http://java.sun.com/xml/ns/j2ee/ejb-jar_2_1.xsd"
fld public final static java.lang.String EJBJAR_3_0_ID = "SCHEMA:http://java.sun.com/xml/ns/javaee/ejb-jar_3_0.xsd"
fld public final static java.lang.String EJBJAR_3_1_ID = "SCHEMA:http://java.sun.com/xml/ns/javaee/ejb-jar_3_1.xsd"
fld public final static java.lang.String EJBJAR_3_2_ID = "SCHEMA:http://xmlns.jcp.org/xml/ns/javaee/ejb-jar_3_2.xsd"
fld public final static java.lang.String EJBJAR_4_0_ID = "SCHEMA:https://jakarta.ee/xml/ns/jakartaee/ejb-jar_4_0.xsd"
fld public final static java.lang.String IBM_J2EE_NS = "http://www.ibm.com/webservices/xsd"
fld public final static java.lang.String J2EE_NS = "http://java.sun.com/xml/ns/j2ee"
fld public final static java.lang.String JAKARTAEE_NS = "https://jakarta.ee/xml/ns/jakartaee"
fld public final static java.lang.String JAKARTA_PERSISTENCEORM_NS = "https://jakarta.ee/xml/ns/persistence/orm"
fld public final static java.lang.String JAKARTA_PERSISTENCE_NS = "https://jakarta.ee/xml/ns/persistence"
fld public final static java.lang.String JAVAEE_NS = "http://java.sun.com/xml/ns/javaee"
fld public final static java.lang.String NEW_JAVAEE_NS = "http://xmlns.jcp.org/xml/ns/javaee"
fld public final static java.lang.String NEW_PERSISTENCEORM_NS = "http://xmlns.jcp.org/xml/ns/persistence/orm"
fld public final static java.lang.String NEW_PERSISTENCE_NS = "http://xmlns.jcp.org/xml/ns/persistence"
fld public final static java.lang.String PERSISTENCEORM_2_0_ID = "SCHEMA:http://java.sun.com/xml/ns/persistence/orm_2_0.xsd"
fld public final static java.lang.String PERSISTENCEORM_2_1_ID = "SCHEMA:http://xmlns.jcp.org/xml/ns/persistence/orm/orm_2_1.xsd"
fld public final static java.lang.String PERSISTENCEORM_2_2_ID = "SCHEMA:http://xmlns.jcp.org/xml/ns/persistence/orm/orm_2_2.xsd"
fld public final static java.lang.String PERSISTENCEORM_3_0_ID = "SCHEMA:https://jakarta.ee/xml/ns/persistence/orm/orm_3_0.xsd"
fld public final static java.lang.String PERSISTENCEORM_3_1_ID = "SCHEMA:https://jakarta.ee/xml/ns/persistence/orm/orm_3_1.xsd"
fld public final static java.lang.String PERSISTENCEORM_ID = "SCHEMA:http://java.sun.com/xml/ns/persistence/orm_1_0.xsd"
fld public final static java.lang.String PERSISTENCEORM_NS = "http://java.sun.com/xml/ns/persistence/orm"
fld public final static java.lang.String PERSISTENCE_2_0_ID = "SCHEMA:http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd"
fld public final static java.lang.String PERSISTENCE_2_1_ID = "SCHEMA:http://xmlns.jcp.org/xml/ns/persistence/persistence_2_1.xsd"
fld public final static java.lang.String PERSISTENCE_2_2_ID = "SCHEMA:http://xmlns.jcp.org/xml/ns/persistence/persistence_2_2.xsd"
fld public final static java.lang.String PERSISTENCE_3_0_ID = "SCHEMA:https://jakarta.ee/xml/ns/persistence/persistence_3_0.xsd"
fld public final static java.lang.String PERSISTENCE_3_1_ID = "SCHEMA:https://jakarta.ee/xml/ns/persistence/persistence_3_0.xsd"
fld public final static java.lang.String PERSISTENCE_ID = "SCHEMA:http://java.sun.com/xml/ns/persistence/persistence_1_0.xsd"
fld public final static java.lang.String PERSISTENCE_NS = "http://java.sun.com/xml/ns/persistence"
fld public final static java.lang.String RI_CONFIG_NS = "http://java.sun.com/xml/ns/jax-rpc/ri/config"
fld public final static java.lang.String WEBAPP_2_5_ID = "SCHEMA:http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
fld public final static java.lang.String WEBAPP_3_0_ID = "SCHEMA:http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
fld public final static java.lang.String WEBAPP_3_1_ID = "SCHEMA:http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
fld public final static java.lang.String WEBAPP_4_0_ID = "SCHEMA:http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
fld public final static java.lang.String WEBAPP_5_0_ID = "SCHEMA:https://jakarta.ee/xml/ns/jakartaee/web-app_5_0.xsd"
fld public final static java.lang.String WEBAPP_6_0_ID = "SCHEMA:https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
fld public final static java.lang.String WEBCOMMON_3_0_ID = "SCHEMA:http://java.sun.com/xml/ns/javaee/web-common_3_0.xsd"
fld public final static java.lang.String WEBCOMMON_3_1_ID = "SCHEMA:http://xmlns.jcp.org/xml/ns/javaee/web-common_3_1.xsd"
fld public final static java.lang.String WEBCOMMON_4_0_ID = "SCHEMA:http://xmlns.jcp.org/xml/ns/javaee/web-common_4_0.xsd"
fld public final static java.lang.String WEBCOMMON_5_0_ID = "SCHEMA:https://jakarta.ee/xml/ns/jakartaee/web-common_5_0.xsd"
fld public final static java.lang.String WEBCOMMON_6_0_ID = "SCHEMA:https://jakarta.ee/xml/ns/jakartaee/web-common_6_0.xsd"
fld public final static java.lang.String WEBFRAGMENT_3_0_ID = "SCHEMA:http://java.sun.com/xml/ns/javaee/web-fragment_3_0.xsd"
fld public final static java.lang.String WEBFRAGMENT_3_1_ID = "SCHEMA:http://xmlns.jcp.org/xml/ns/javaee/web-fragment_3_1.xsd"
fld public final static java.lang.String WEBFRAGMENT_4_0_ID = "SCHEMA:http://xmlns.jcp.org/xml/ns/javaee/web-fragment_4_0.xsd"
fld public final static java.lang.String WEBFRAGMENT_5_0_ID = "SCHEMA:https://jakarta.ee/xml/ns/jakartaee/web-fragment_5_0.xsd"
fld public final static java.lang.String WEBFRAGMENT_6_0_ID = "SCHEMA:https://jakarta.ee/xml/ns/jakartaee/web-fragment_6_0.xsd"
fld public final static java.lang.String WEBSERVICES_1_1_ID = "SCHEMA:http://www.ibm.com/webservices/xsd/j2ee_web_services_1_1.xsd"
fld public final static java.lang.String WEBSERVICES_1_2_ID = "SCHEMA:http://java.sun.com/xml/ns/javaee/javaee_web_services_1_2.xsd"
fld public final static java.lang.String WEBSERVICES_1_3_ID = "SCHEMA:http://java.sun.com/xml/ns/javaee/javaee_web_services_1_3.xsd"
fld public final static java.lang.String WEBSERVICES_1_4_ID = "SCHEMA:http://xmlns.jcp.org/xml/ns/javaee/javaee_web_services_1_4.xsd"
fld public final static java.lang.String WEBSERVICES_2_0_ID = "SCHEMA:https://jakarta.ee/xml/ns/jakartaee/jakartaee_web_services_2_0.xsd"
fld public final static java.lang.String WEBSERVICES_CLIENT_1_1_ID = "SCHEMA:http://java.sun.com/xml/ns/j2ee/j2ee_web_services_client_1_1.xsd"
fld public final static java.lang.String WEBSERVICES_CLIENT_1_2_ID = "SCHEMA:http://java.sun.com/xml/ns/javaee/javaee_web_services_client_1_2.xsd"
fld public final static java.lang.String WEBSERVICES_CLIENT_1_3_ID = "SCHEMA:http://java.sun.com/xml/ns/javaee/javaee_web_services_client_1_3.xsd"
fld public final static java.lang.String WEBSERVICES_CLIENT_1_4_ID = "SCHEMA:http://xmlns.jcp.org/xml/ns/javaee/javaee_web_services_client_1_4.xsd"
fld public final static java.lang.String WEBSERVICES_CLIENT_2_0_ID = "SCHEMA:https://jakarta.ee/xml/ns/jakartaee/jakartaee_web_services_client_2_0.xsd"
intf org.netbeans.modules.xml.catalog.spi.CatalogDescriptor2
intf org.netbeans.modules.xml.catalog.spi.CatalogReader
intf org.xml.sax.EntityResolver
meth public java.beans.FeatureDescriptor getDescriptor()
meth public java.lang.String getDisplayName()
meth public java.lang.String getFullURLFromSystemId(java.lang.String)
meth public java.lang.String getIconResource(int)
meth public java.lang.String getShortDescription()
meth public java.lang.String getSystemID(java.lang.String)
meth public java.lang.String resolvePublic(java.lang.String)
meth public java.lang.String resolveURI(java.lang.String)
meth public java.util.Enumeration enabled(org.netbeans.modules.xml.api.model.GrammarEnvironment)
meth public java.util.Iterator getPublicIDs()
meth public org.netbeans.modules.xml.api.model.GrammarQuery getGrammar(org.netbeans.modules.xml.api.model.GrammarEnvironment)
meth public org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public static org.netbeans.modules.glassfish.javaee.RunTimeDDCatalog getEE6RunTimeDDCatalog()
meth public static org.netbeans.modules.glassfish.javaee.RunTimeDDCatalog getRunTimeDDCatalog(org.netbeans.spi.server.ServerInstanceProvider)
meth public void addCatalogListener(org.netbeans.modules.xml.catalog.spi.CatalogListener)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void fireCatalogListeners()
meth public void refresh()
meth public void refresh(java.io.File)
meth public void removeCatalogListener(org.netbeans.modules.xml.catalog.spi.CatalogListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setInstanceProvider(org.netbeans.spi.server.ServerInstanceProvider)
supr org.netbeans.modules.xml.api.model.GrammarQueryManager
hfds APPCLIENT_10,APPCLIENT_10_XSD,APPCLIENT_1_4,APPCLIENT_1_4_XSD,APPCLIENT_5,APPCLIENT_5_XSD,APPCLIENT_6,APPCLIENT_6_XSD,APPCLIENT_7,APPCLIENT_7_XSD,APPCLIENT_8,APPCLIENT_8_XSD,APPCLIENT_9,APPCLIENT_9_XSD,APPCLIENT_TAG,APP_10,APP_10_XSD,APP_1_4,APP_1_4_XSD,APP_5,APP_5_XSD,APP_6,APP_6_XSD,APP_7,APP_7_XSD,APP_8,APP_8_XSD,APP_9,APP_9_XSD,APP_TAG,EJBJAR_2_1,EJBJAR_2_1_XSD,EJBJAR_3_0,EJBJAR_3_0_XSD,EJBJAR_3_1,EJBJAR_3_1_XSD,EJBJAR_3_2,EJBJAR_3_2_XSD,EJBJAR_4_0,EJBJAR_4_0_XSD,EJB_JAR_TAG,JavaEE6SchemaToURLMap,JavaEE6TypeToURLMap,PERSISTENCE,PERSISTENCEORM,PERSISTENCEORM_2_0,PERSISTENCEORM_2_0_XSD,PERSISTENCEORM_2_1,PERSISTENCEORM_2_1_XSD,PERSISTENCEORM_2_2,PERSISTENCEORM_2_2_XSD,PERSISTENCEORM_3_0,PERSISTENCEORM_3_0_XSD,PERSISTENCEORM_3_1,PERSISTENCEORM_3_1_XSD,PERSISTENCEORM_TAG,PERSISTENCEORM_XSD,PERSISTENCE_2_0,PERSISTENCE_2_0_XSD,PERSISTENCE_2_1,PERSISTENCE_2_1_XSD,PERSISTENCE_2_2,PERSISTENCE_2_2_XSD,PERSISTENCE_3_0,PERSISTENCE_3_0_XSD,PERSISTENCE_3_1,PERSISTENCE_3_1_XSD,PERSISTENCE_TAG,PERSISTENCE_XSD,SCHEMASLOCATION,SchemaToURLMap,TypeToURLMap,WEBAPP_2_5,WEBAPP_2_5_XSD,WEBAPP_3_0,WEBAPP_3_0_XSD,WEBAPP_3_1,WEBAPP_3_1_XSD,WEBAPP_4_0,WEBAPP_4_0_XSD,WEBAPP_5_0,WEBAPP_5_0_XSD,WEBAPP_6_0,WEBAPP_6_0_XSD,WEBAPP_TAG,WEBCOMMON_3_0,WEBCOMMON_3_0_XSD,WEBCOMMON_3_1,WEBCOMMON_3_1_XSD,WEBCOMMON_4_0,WEBCOMMON_4_0_XSD,WEBCOMMON_5_0,WEBCOMMON_5_0_XSD,WEBCOMMON_6_0,WEBCOMMON_6_0_XSD,WEBFRAGMENT_3_0,WEBFRAGMENT_3_0_XSD,WEBFRAGMENT_3_1,WEBFRAGMENT_3_1_XSD,WEBFRAGMENT_4_0,WEBFRAGMENT_4_0_XSD,WEBFRAGMENT_5_0,WEBFRAGMENT_5_0_XSD,WEBFRAGMENT_6_0,WEBFRAGMENT_6_0_XSD,WEBSERVICES_1_1,WEBSERVICES_1_1_XSD,WEBSERVICES_1_2,WEBSERVICES_1_2_XSD,WEBSERVICES_1_3,WEBSERVICES_1_3_XSD,WEBSERVICES_1_4,WEBSERVICES_1_4_XSD,WEBSERVICES_2_0,WEBSERVICES_2_0_XSD,WEBSERVICES_CLIENT_1_1,WEBSERVICES_CLIENT_1_1_XSD,WEBSERVICES_CLIENT_1_2,WEBSERVICES_CLIENT_1_2_XSD,WEBSERVICES_CLIENT_1_3,WEBSERVICES_CLIENT_1_3_XSD,WEBSERVICES_CLIENT_1_4,WEBSERVICES_CLIENT_1_4_XSD,WEBSERVICES_CLIENT_2_0,WEBSERVICES_CLIENT_2_0_XSD,WEBSERVICES_TAG,XMLNS_ATTR,XML_XSD,XML_XSD_DEF,catalogListeners,ddCatalogMap,displayNameKey,hasAdditionalMap,javaEE6DDCatalog,platformRootDir,shortDescriptionKey

CLSS public org.netbeans.modules.glassfish.javaee.SunMessageDestination
cons public init(java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type)
cons public init(java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type,java.io.File)
intf org.netbeans.modules.j2ee.deployment.common.api.MessageDestination
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type getType()
supr java.lang.Object
hfds name,resourceDir,type

CLSS public org.netbeans.modules.glassfish.javaee.Three1Configuration
cons public init(javax.enterprise.deploy.model.DeployableObject)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule,org.netbeans.modules.glassfish.tooling.data.GlassFishVersion) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
intf javax.enterprise.deploy.spi.DeploymentConfiguration
meth protected org.openide.filesystems.FileObject getSunDD(java.io.File,boolean) throws java.io.IOException
meth public boolean supportsCreateDatasource()
meth public boolean supportsCreateMessageDestination()
meth public java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.Datasource> getDatasources() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
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
supr org.netbeans.modules.glassfish.javaee.Hk2Configuration
hfds GLASSFISH_DASH

CLSS public org.netbeans.modules.glassfish.javaee.Three1ModuleConfigFactory
cons public init(org.netbeans.modules.glassfish.javaee.Hk2DeploymentManager)
intf org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfigurationFactory2
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration create(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration create(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public org.netbeans.modules.glassfish.javaee.db.DataSourcesReader
cons public init(org.netbeans.modules.glassfish.tooling.data.GlassFishServer)
fld public final static java.lang.String DATA_SOURCES_PATTERN = "resources.*"
fld public final static java.lang.String PROPERTY_IDENT = "property"
fld public final static java.lang.String PROPERTY_SPLIT_PATTERN = "\u005c."
meth public java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.Datasource> getDataSourcesFromServer()
supr java.lang.Object
hfds DEFAULT_DATA_SOURCE,DEFAULT_DATA_SOURCE_EE7,server
hcls ResourceType

CLSS public org.netbeans.modules.glassfish.javaee.db.DbUtil
cons public init()
meth public final static boolean notEmpty(java.lang.String)
meth public final static boolean strEmpty(java.lang.String)
meth public final static boolean strEquals(java.lang.String,java.lang.String)
meth public final static boolean strEquivalent(java.lang.String,java.lang.String)
meth public final static int strCompareTo(java.lang.String,java.lang.String)
meth public static java.util.Map<java.lang.String,java.lang.String> normalizePoolMap(java.util.Map<java.lang.String,java.lang.String>)
supr java.lang.Object
hfds Reqd_DBName,VendorsDBNameProp,__DatabaseName,__DatabaseVendor,__DerbyConnAttr,__DerbyDatabaseName,__DerbyPortNumber,__DriverClass,__InformixHostName,__InformixServer,__IsCPExisting,__IsXA,__NotApplicable,__Password,__PortNumber,__SID,__ServerName,__Url,__User

CLSS public org.netbeans.modules.glassfish.javaee.db.DriverMaps
meth public final static java.lang.String getDSClassName(java.lang.String)
meth public final static java.lang.String getDriverName(java.lang.String)
meth public final static java.lang.String getUrlPrefix(java.lang.String,java.lang.String)
supr java.lang.Object
hfds INITIAL_MAP_SIZE,cpClassMap,driverMap,dsClassMap

CLSS public org.netbeans.modules.glassfish.javaee.db.Hk2DatasourceManager
cons public init(org.netbeans.modules.glassfish.javaee.Hk2DeploymentManager)
intf org.netbeans.modules.j2ee.deployment.plugins.spi.DatasourceManager
meth public java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.Datasource> getDatasources() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public static java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.Datasource> getDatasources(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule,org.netbeans.modules.glassfish.tooling.data.GlassFishVersion)
meth public static org.netbeans.modules.j2ee.deployment.common.api.Datasource createDataSource(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule,org.netbeans.modules.glassfish.tooling.data.GlassFishVersion) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException,org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException
meth public static void createConnectionPool(java.io.File,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
meth public static void createJdbcResource(java.io.File,java.lang.String,java.lang.String) throws java.io.IOException
meth public void deployDatasources(java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.Datasource>) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException,org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException
supr java.lang.Object
hfds ATTR_DATASOURCE_CLASSNAME,ATTR_JNDINAME,ATTR_POOLNAME,ATTR_POOL_NAME,ATTR_RES_TYPE,CP_TAG_1,CP_TAG_2,CP_TAG_3,CP_TAG_4,CP_TAG_5,DOMAIN_XML_PATH,GF_RESOURCES_XML_HEADER,JDBC_TAG_1,JDBC_TAG_2,PROP_DATABASE_NAME,PROP_DRIVER_CLASS,PROP_PASSWORD,PROP_PORT_NUMBER,PROP_SERVER_NAME,PROP_URL,PROP_USER,RESTYPE_DATASOURCE,SUN_RESOURCES_XML_HEADER,dm
hcls CPool,ConnectionPool,ConnectionPoolFinder,ConnectionPoolReader,DDResolver,DuplicateJdbcResourceFinder,JdbcReader,JdbcResource,JndiNamespaceFinder,ProxyNodeReader,UserResolver

CLSS public org.netbeans.modules.glassfish.javaee.db.JDBCConnectionPool
cons public init()
innr public final static !enum PropertyType
meth public boolean setProperty(org.netbeans.modules.glassfish.javaee.db.JDBCConnectionPool$PropertyType,java.lang.String)
meth public int getPort()
meth public java.lang.String getDatabaseName()
meth public java.lang.String getDriverClass()
meth public java.lang.String getPassword()
meth public java.lang.String getServerName()
meth public java.lang.String getUrl()
meth public java.lang.String getUser()
supr java.lang.Object
hfds databaseName,driverClass,password,port,serverName,url,user

CLSS public final static !enum org.netbeans.modules.glassfish.javaee.db.JDBCConnectionPool$PropertyType
 outer org.netbeans.modules.glassfish.javaee.db.JDBCConnectionPool
fld public final static org.netbeans.modules.glassfish.javaee.db.JDBCConnectionPool$PropertyType DATABASE_NAME
fld public final static org.netbeans.modules.glassfish.javaee.db.JDBCConnectionPool$PropertyType DRIVER_CLASS
fld public final static org.netbeans.modules.glassfish.javaee.db.JDBCConnectionPool$PropertyType PASSWORD
fld public final static org.netbeans.modules.glassfish.javaee.db.JDBCConnectionPool$PropertyType PORT_NUMBER
fld public final static org.netbeans.modules.glassfish.javaee.db.JDBCConnectionPool$PropertyType SERVER_NAME
fld public final static org.netbeans.modules.glassfish.javaee.db.JDBCConnectionPool$PropertyType UNKNOWN
fld public final static org.netbeans.modules.glassfish.javaee.db.JDBCConnectionPool$PropertyType URL
fld public final static org.netbeans.modules.glassfish.javaee.db.JDBCConnectionPool$PropertyType USER
meth public java.lang.String toString()
meth public static org.netbeans.modules.glassfish.javaee.db.JDBCConnectionPool$PropertyType toValue(java.lang.String)
meth public static org.netbeans.modules.glassfish.javaee.db.JDBCConnectionPool$PropertyType valueOf(java.lang.String)
meth public static org.netbeans.modules.glassfish.javaee.db.JDBCConnectionPool$PropertyType[] values()
supr java.lang.Enum<org.netbeans.modules.glassfish.javaee.db.JDBCConnectionPool$PropertyType>
hfds DATABASE_NAME_STR,DRIVER_CLASS_STR,PASSWORD_STR,PORT_NUMBER_STR,SERVER_NAME_STR,UNKNOWN_STR,URL_STR,USER_STR,stringValues

CLSS public org.netbeans.modules.glassfish.javaee.db.JDBCResource
cons public init()
cons public init(java.lang.String,java.lang.String,java.lang.String,boolean,int,org.netbeans.modules.glassfish.javaee.db.JDBCConnectionPool)
innr public final static !enum AttrType
intf org.netbeans.modules.j2ee.deployment.common.api.Datasource
meth public boolean isEnabled()
meth public boolean setProperty(org.netbeans.modules.glassfish.javaee.db.JDBCResource$AttrType,java.lang.String)
meth public int getDeploymentOrder()
meth public java.lang.String getDisplayName()
meth public java.lang.String getDriverClassName()
meth public java.lang.String getJndiName()
meth public java.lang.String getObjectType()
meth public java.lang.String getPassword()
meth public java.lang.String getPoolName()
meth public java.lang.String getUrl()
meth public java.lang.String getUsername()
meth public org.netbeans.modules.glassfish.javaee.db.JDBCConnectionPool getPool()
meth public org.netbeans.modules.glassfish.javaee.db.JDBCResource copy(java.lang.String)
meth public void setPool(org.netbeans.modules.glassfish.javaee.db.JDBCConnectionPool)
supr java.lang.Object
hfds deploymentOrder,enabled,jndiName,objectType,pool,poolName

CLSS public final static !enum org.netbeans.modules.glassfish.javaee.db.JDBCResource$AttrType
 outer org.netbeans.modules.glassfish.javaee.db.JDBCResource
fld public final static org.netbeans.modules.glassfish.javaee.db.JDBCResource$AttrType DEPLOYMENT_ORDER
fld public final static org.netbeans.modules.glassfish.javaee.db.JDBCResource$AttrType ENABLED
fld public final static org.netbeans.modules.glassfish.javaee.db.JDBCResource$AttrType JNDI_NAME
fld public final static org.netbeans.modules.glassfish.javaee.db.JDBCResource$AttrType OBJECT_TYPE
fld public final static org.netbeans.modules.glassfish.javaee.db.JDBCResource$AttrType POOL_NAME
fld public final static org.netbeans.modules.glassfish.javaee.db.JDBCResource$AttrType UNKNOWN
meth public java.lang.String toString()
meth public static org.netbeans.modules.glassfish.javaee.db.JDBCResource$AttrType toValue(java.lang.String)
meth public static org.netbeans.modules.glassfish.javaee.db.JDBCResource$AttrType valueOf(java.lang.String)
meth public static org.netbeans.modules.glassfish.javaee.db.JDBCResource$AttrType[] values()
supr java.lang.Enum<org.netbeans.modules.glassfish.javaee.db.JDBCResource$AttrType>
hfds DEPLOYMENT_ORDER_STR,ENABLED_STR,JNDI_NAME_STR,OBJECT_TYPE_STR,POOL_NAME_STR,UNKNOWN_STR,stringValues

CLSS public abstract interface org.netbeans.modules.glassfish.javaee.db.JndiNameResolver
meth public abstract java.lang.String resolveJndiName(java.lang.String)

CLSS public org.netbeans.modules.glassfish.javaee.db.ResourcesHelper
cons public init()
meth public static void addSampleDatasource(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule,javax.enterprise.deploy.spi.DeploymentManager)
supr java.lang.Object
hfds RP

CLSS public org.netbeans.modules.glassfish.javaee.db.SunDatasource
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,boolean,org.netbeans.modules.glassfish.javaee.ApplicationScopedResourcesUtils$JndiNameResolver)
intf org.netbeans.modules.j2ee.deployment.common.api.Datasource
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getDisplayName()
meth public java.lang.String getDriverClassName()
meth public java.lang.String getJndiName()
meth public java.lang.String getPassword()
meth public java.lang.String getUrl()
meth public java.lang.String getUsername()
meth public java.lang.String toString()
meth public org.netbeans.modules.glassfish.javaee.db.SunDatasource copy(java.lang.String)
supr java.lang.Object
hfds driverClassName,jndiName,password,resolver,url,username

CLSS public org.netbeans.modules.glassfish.javaee.db.VendorNameMgr
cons public init()
meth public static java.lang.String dsClassNameFromVendorName(java.lang.String)
meth public static java.lang.String vendorNameFromDbUrl(java.lang.String)
supr java.lang.Object
hfds BLANK,DOT,ILLEGAL_CHARS_PATTERN,ILLEGAL_FILENAME_CHARS,REPLACEMENT_PATTERN,vendorNameToDscnMap,vendorNameToUrlMap

CLSS public org.netbeans.modules.glassfish.javaee.ui.DebugPortQuery
cons public init()
meth public boolean shouldPersist()
meth public java.awt.Dimension getPreferredSize()
meth public java.lang.String getDebugPort()
meth public void setDebugPort(java.lang.String)
supr javax.swing.JPanel
hfds debugPortLable,debugPortSpinner,noAskCheck

CLSS public abstract interface org.netbeans.modules.glassfish.spi.DecoratorFactory
meth public abstract boolean isTypeSupported(java.lang.String)
meth public abstract java.util.Map<java.lang.String,org.netbeans.modules.glassfish.spi.Decorator> getAllDecorators()
meth public abstract org.netbeans.modules.glassfish.spi.Decorator getDecorator(java.lang.String)

CLSS public abstract interface org.netbeans.modules.glassfish.spi.GlassfishModuleFactory
meth public abstract boolean isModuleSupported(java.lang.String,java.util.Properties)
meth public abstract java.lang.Object createModule(org.openide.util.Lookup)

CLSS public abstract interface org.netbeans.modules.glassfish.spi.RecognizerCookie
meth public abstract java.util.Collection<? extends org.netbeans.modules.glassfish.spi.Recognizer> getRecognizers()

CLSS public abstract interface org.netbeans.modules.glassfish.spi.RemoveCookie
meth public abstract void removeInstance(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.common.api.Datasource
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getDriverClassName()
meth public abstract java.lang.String getJndiName()
meth public abstract java.lang.String getPassword()
meth public abstract java.lang.String getUrl()
meth public abstract java.lang.String getUsername()

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.common.api.MessageDestination
innr public final static !enum Type
meth public abstract java.lang.String getName()
meth public abstract org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type getType()

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.DatasourceManager
meth public abstract java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.Datasource> getDatasources() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void deployDatasources(java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.Datasource>) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException,org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentManager2
intf javax.enterprise.deploy.spi.DeploymentManager
meth public abstract javax.enterprise.deploy.spi.status.ProgressObject distribute(javax.enterprise.deploy.spi.Target[],org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentContext)
meth public abstract javax.enterprise.deploy.spi.status.ProgressObject redeploy(javax.enterprise.deploy.spi.TargetModuleID[],org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentContext)

CLSS public abstract org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformFactory
cons public init()
meth public abstract org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl getJ2eePlatformImpl(javax.enterprise.deploy.spi.DeploymentManager)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl
cons public init()
fld public final static java.lang.String PROP_DISPLAY_NAME = "displayName"
fld public final static java.lang.String PROP_LIBRARIES = "libraries"
fld public final static java.lang.String PROP_SERVER_LIBRARIES = "serverLibraries"
meth public abstract boolean isToolSupported(java.lang.String)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public abstract java.awt.Image getIcon()
meth public abstract java.io.File[] getPlatformRoots()
meth public abstract java.io.File[] getToolClasspathEntries(java.lang.String)
meth public abstract java.lang.String getDisplayName()
meth public abstract java.util.Set getSupportedJavaPlatformVersions()
meth public abstract org.netbeans.api.java.platform.JavaPlatform getJavaPlatform()
meth public abstract org.netbeans.spi.project.libraries.LibraryImplementation[] getLibraries()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public java.lang.String getToolProperty(java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public java.util.Set getSupportedModuleTypes()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public java.util.Set<java.lang.String> getSupportedSpecVersions()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public java.util.Set<java.lang.String> getSupportedSpecVersions(java.lang.Object)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public java.util.Set<org.netbeans.api.j2ee.core.Profile> getSupportedProfiles()
meth public java.util.Set<org.netbeans.api.j2ee.core.Profile> getSupportedProfiles(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type)
meth public java.util.Set<org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type> getSupportedTypes()
meth public org.netbeans.spi.project.libraries.LibraryImplementation[] getLibraries(java.util.Set<org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency>)
meth public org.openide.util.Lookup getLookup()
supr java.lang.Object
hfds supp

CLSS public abstract org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl2
cons public init()
meth public abstract java.io.File getDomainHome()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.io.File getMiddlewareHome()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.io.File getServerHome()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
supr org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.JDBCDriverDeployer
meth public abstract boolean supportsDeployJDBCDrivers(javax.enterprise.deploy.spi.Target)
meth public abstract javax.enterprise.deploy.spi.status.ProgressObject deployJDBCDrivers(javax.enterprise.deploy.spi.Target,java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.Datasource>)

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.MessageDestinationDeployment
meth public abstract java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.MessageDestination> getMessageDestinations() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void deployMessageDestinations(java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.MessageDestination>) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException

CLSS public abstract org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory
cons public init()
meth public abstract org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet getFindJSPServlet(javax.enterprise.deploy.spi.DeploymentManager)
meth public abstract org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment getIncrementalDeployment(javax.enterprise.deploy.spi.DeploymentManager)
meth public abstract org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer getStartServer(javax.enterprise.deploy.spi.DeploymentManager)
meth public boolean isCommonUIRequired()
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.AntDeploymentProvider getAntDeploymentProvider(javax.enterprise.deploy.spi.DeploymentManager)
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.DatasourceManager getDatasourceManager(javax.enterprise.deploy.spi.DeploymentManager)
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.JDBCDriverDeployer getJDBCDriverDeployer(javax.enterprise.deploy.spi.DeploymentManager)
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.MessageDestinationDeployment getMessageDestinationDeployment(javax.enterprise.deploy.spi.DeploymentManager)
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.ServerInstanceDescriptor getServerInstanceDescriptor(javax.enterprise.deploy.spi.DeploymentManager)
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.ServerLibraryManager getServerLibraryManager(javax.enterprise.deploy.spi.DeploymentManager)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.TargetModuleIDResolver getTargetModuleIDResolver(javax.enterprise.deploy.spi.DeploymentManager)
meth public org.openide.WizardDescriptor$InstantiatingIterator getAddInstanceIterator()
meth public void finishServerInitialization() throws org.netbeans.modules.j2ee.deployment.plugins.spi.ServerInitializationException
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.ServerInstanceDescriptor
meth public abstract boolean isLocal()
meth public abstract int getHttpPort()
meth public abstract java.lang.String getHostname()

CLSS public abstract org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer
cons public init()
meth public abstract boolean isAlsoTargetServer(javax.enterprise.deploy.spi.Target)
meth public abstract boolean isDebuggable(javax.enterprise.deploy.spi.Target)
meth public abstract boolean isRunning()
meth public abstract boolean needsStartForAdminConfig()
meth public abstract boolean needsStartForConfigure()
meth public abstract boolean needsStartForTargetList()
meth public abstract boolean supportsStartDeploymentManager()
meth public abstract javax.enterprise.deploy.spi.status.ProgressObject startDebugging(javax.enterprise.deploy.spi.Target)
meth public abstract javax.enterprise.deploy.spi.status.ProgressObject startDeploymentManager()
meth public abstract javax.enterprise.deploy.spi.status.ProgressObject stopDeploymentManager()
meth public abstract org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo getDebugInfo(javax.enterprise.deploy.spi.Target)
meth public boolean canStopDeploymentManagerSilently()
meth public boolean isRunning(javax.enterprise.deploy.spi.Target)
meth public boolean needsRestart(javax.enterprise.deploy.spi.Target)
meth public boolean supportsStartDebugging(javax.enterprise.deploy.spi.Target)
meth public boolean supportsStartProfiling(javax.enterprise.deploy.spi.Target)
meth public boolean supportsStartTarget(javax.enterprise.deploy.spi.Target)
meth public javax.enterprise.deploy.spi.status.ProgressObject startProfiling(javax.enterprise.deploy.spi.Target)
meth public javax.enterprise.deploy.spi.status.ProgressObject startTarget(javax.enterprise.deploy.spi.Target)
meth public javax.enterprise.deploy.spi.status.ProgressObject stopTarget(javax.enterprise.deploy.spi.Target)
meth public void stopDeploymentManagerSilently()
supr java.lang.Object

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

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.config.DeploymentPlanConfiguration
meth public abstract void save(java.io.OutputStream) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException

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

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration
intf org.openide.util.Lookup$Provider
meth public abstract org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule getJ2eeModule()
meth public abstract org.openide.util.Lookup getLookup()
meth public abstract void dispose()

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfigurationFactory
meth public abstract org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration create(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfigurationFactory2
intf org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfigurationFactory
meth public abstract org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration create(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.modules.javaee.specs.support.spi.JpaSupportImplementation
meth public abstract java.util.Set<org.netbeans.modules.javaee.specs.support.api.JpaProvider> getProviders()
meth public abstract org.netbeans.modules.javaee.specs.support.api.JpaProvider getDefaultProvider()

CLSS public abstract interface org.netbeans.modules.websvc.wsstack.spi.WSStackImplementation<%0 extends java.lang.Object>
meth public abstract boolean isFeatureSupported(org.netbeans.modules.websvc.wsstack.api.WSStack$Feature)
meth public abstract org.netbeans.modules.websvc.wsstack.api.WSStackVersion getVersion()
meth public abstract org.netbeans.modules.websvc.wsstack.api.WSTool getWSTool(org.netbeans.modules.websvc.wsstack.api.WSStack$Tool)
meth public abstract {org.netbeans.modules.websvc.wsstack.spi.WSStackImplementation%0} get()

CLSS public abstract interface org.netbeans.modules.websvc.wsstack.spi.WSToolImplementation
meth public abstract java.lang.String getName()
meth public abstract java.net.URL[] getLibraries()

CLSS public abstract org.netbeans.modules.xml.api.model.GrammarQueryManager
cons public init()
meth public abstract java.beans.FeatureDescriptor getDescriptor()
meth public abstract java.util.Enumeration enabled(org.netbeans.modules.xml.api.model.GrammarEnvironment)
meth public abstract org.netbeans.modules.xml.api.model.GrammarQuery getGrammar(org.netbeans.modules.xml.api.model.GrammarEnvironment)
meth public static org.netbeans.modules.xml.api.model.GrammarQueryManager getDefault()
supr java.lang.Object
hfds instance
hcls DefaultQueryManager

CLSS public abstract interface org.netbeans.modules.xml.catalog.spi.CatalogDescriptor2
intf org.netbeans.modules.xml.catalog.spi.CatalogDescriptorBase
meth public abstract java.lang.String getIconResource(int)

CLSS public abstract interface org.netbeans.modules.xml.catalog.spi.CatalogDescriptorBase
fld public final static java.lang.String PROP_CATALOG_DESC = "ca-desc"
fld public final static java.lang.String PROP_CATALOG_ICON = "ca-icon"
fld public final static java.lang.String PROP_CATALOG_NAME = "ca-name"
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getShortDescription()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public abstract interface org.netbeans.modules.xml.catalog.spi.CatalogReader
meth public abstract java.lang.String getSystemID(java.lang.String)
meth public abstract java.lang.String resolvePublic(java.lang.String)
meth public abstract java.lang.String resolveURI(java.lang.String)
meth public abstract java.util.Iterator getPublicIDs()
meth public abstract void addCatalogListener(org.netbeans.modules.xml.catalog.spi.CatalogListener)
meth public abstract void refresh()
meth public abstract void removeCatalogListener(org.netbeans.modules.xml.catalog.spi.CatalogListener)

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

