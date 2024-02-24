#Signature file v4.1
#Version 2.55.0

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

CLSS public abstract interface java.awt.datatransfer.ClipboardOwner
meth public abstract void lostOwnership(java.awt.datatransfer.Clipboard,java.awt.datatransfer.Transferable)

CLSS public abstract interface java.awt.dnd.DragGestureListener
intf java.util.EventListener
meth public abstract void dragGestureRecognized(java.awt.dnd.DragGestureEvent)

CLSS public abstract interface java.awt.dnd.DragSourceListener
intf java.util.EventListener
meth public abstract void dragDropEnd(java.awt.dnd.DragSourceDropEvent)
meth public abstract void dragEnter(java.awt.dnd.DragSourceDragEvent)
meth public abstract void dragExit(java.awt.dnd.DragSourceEvent)
meth public abstract void dragOver(java.awt.dnd.DragSourceDragEvent)
meth public abstract void dropActionChanged(java.awt.dnd.DragSourceDragEvent)

CLSS public abstract interface java.awt.dnd.DropTargetListener
intf java.util.EventListener
meth public abstract void dragEnter(java.awt.dnd.DropTargetDragEvent)
meth public abstract void dragExit(java.awt.dnd.DropTargetEvent)
meth public abstract void dragOver(java.awt.dnd.DropTargetDragEvent)
meth public abstract void drop(java.awt.dnd.DropTargetDropEvent)
meth public abstract void dropActionChanged(java.awt.dnd.DropTargetDragEvent)

CLSS public abstract interface java.awt.event.ActionListener
intf java.util.EventListener
meth public abstract void actionPerformed(java.awt.event.ActionEvent)

CLSS public abstract interface java.awt.event.ItemListener
intf java.util.EventListener
meth public abstract void itemStateChanged(java.awt.event.ItemEvent)

CLSS public abstract interface java.awt.event.KeyListener
intf java.util.EventListener
meth public abstract void keyPressed(java.awt.event.KeyEvent)
meth public abstract void keyReleased(java.awt.event.KeyEvent)
meth public abstract void keyTyped(java.awt.event.KeyEvent)

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

CLSS public abstract interface java.io.Externalizable
intf java.io.Serializable
meth public abstract void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public abstract void writeExternal(java.io.ObjectOutput) throws java.io.IOException

CLSS public abstract interface java.io.Serializable

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

CLSS public javax.swing.JInternalFrame
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,boolean)
cons public init(java.lang.String,boolean,boolean)
cons public init(java.lang.String,boolean,boolean,boolean)
cons public init(java.lang.String,boolean,boolean,boolean,boolean)
fld protected boolean closable
fld protected boolean iconable
fld protected boolean isClosed
fld protected boolean isIcon
fld protected boolean isMaximum
fld protected boolean isSelected
fld protected boolean maximizable
fld protected boolean resizable
fld protected boolean rootPaneCheckingEnabled
fld protected java.lang.String title
fld protected javax.swing.Icon frameIcon
fld protected javax.swing.JInternalFrame$JDesktopIcon desktopIcon
fld protected javax.swing.JRootPane rootPane
fld public final static java.lang.String CONTENT_PANE_PROPERTY = "contentPane"
fld public final static java.lang.String FRAME_ICON_PROPERTY = "frameIcon"
fld public final static java.lang.String GLASS_PANE_PROPERTY = "glassPane"
fld public final static java.lang.String IS_CLOSED_PROPERTY = "closed"
fld public final static java.lang.String IS_ICON_PROPERTY = "icon"
fld public final static java.lang.String IS_MAXIMUM_PROPERTY = "maximum"
fld public final static java.lang.String IS_SELECTED_PROPERTY = "selected"
fld public final static java.lang.String LAYERED_PANE_PROPERTY = "layeredPane"
fld public final static java.lang.String MENU_BAR_PROPERTY = "JMenuBar"
fld public final static java.lang.String ROOT_PANE_PROPERTY = "rootPane"
fld public final static java.lang.String TITLE_PROPERTY = "title"
innr protected AccessibleJInternalFrame
innr public static JDesktopIcon
intf javax.accessibility.Accessible
intf javax.swing.RootPaneContainer
intf javax.swing.WindowConstants
meth protected boolean isRootPaneCheckingEnabled()
meth protected java.lang.String paramString()
meth protected javax.swing.JRootPane createRootPane()
meth protected void addImpl(java.awt.Component,java.lang.Object,int)
meth protected void fireInternalFrameEvent(int)
meth protected void paintComponent(java.awt.Graphics)
meth protected void setRootPane(javax.swing.JRootPane)
meth protected void setRootPaneCheckingEnabled(boolean)
meth public boolean isClosable()
meth public boolean isClosed()
meth public boolean isIcon()
meth public boolean isIconifiable()
meth public boolean isMaximizable()
meth public boolean isMaximum()
meth public boolean isResizable()
meth public boolean isSelected()
meth public final boolean isFocusCycleRoot()
meth public final java.awt.Container getFocusCycleRootAncestor()
meth public final java.lang.String getWarningString()
meth public final void setFocusCycleRoot(boolean)
meth public int getDefaultCloseOperation()
meth public int getLayer()
meth public java.awt.Component getFocusOwner()
meth public java.awt.Component getGlassPane()
meth public java.awt.Component getMostRecentFocusOwner()
meth public java.awt.Container getContentPane()
meth public java.awt.Cursor getLastCursor()
meth public java.awt.Rectangle getNormalBounds()
meth public java.lang.String getTitle()
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.Icon getFrameIcon()
meth public javax.swing.JDesktopPane getDesktopPane()
meth public javax.swing.JInternalFrame$JDesktopIcon getDesktopIcon()
meth public javax.swing.JLayeredPane getLayeredPane()
meth public javax.swing.JMenuBar getJMenuBar()
meth public javax.swing.JMenuBar getMenuBar()
 anno 0 java.lang.Deprecated()
meth public javax.swing.JRootPane getRootPane()
meth public javax.swing.event.InternalFrameListener[] getInternalFrameListeners()
meth public javax.swing.plaf.InternalFrameUI getUI()
meth public void addInternalFrameListener(javax.swing.event.InternalFrameListener)
meth public void dispose()
meth public void doDefaultCloseAction()
meth public void hide()
meth public void moveToBack()
meth public void moveToFront()
meth public void pack()
meth public void remove(java.awt.Component)
meth public void removeInternalFrameListener(javax.swing.event.InternalFrameListener)
meth public void reshape(int,int,int,int)
meth public void restoreSubcomponentFocus()
meth public void setClosable(boolean)
meth public void setClosed(boolean) throws java.beans.PropertyVetoException
meth public void setContentPane(java.awt.Container)
meth public void setCursor(java.awt.Cursor)
meth public void setDefaultCloseOperation(int)
meth public void setDesktopIcon(javax.swing.JInternalFrame$JDesktopIcon)
meth public void setFrameIcon(javax.swing.Icon)
meth public void setGlassPane(java.awt.Component)
meth public void setIcon(boolean) throws java.beans.PropertyVetoException
meth public void setIconifiable(boolean)
meth public void setJMenuBar(javax.swing.JMenuBar)
meth public void setLayer(int)
meth public void setLayer(java.lang.Integer)
meth public void setLayeredPane(javax.swing.JLayeredPane)
meth public void setLayout(java.awt.LayoutManager)
meth public void setMaximizable(boolean)
meth public void setMaximum(boolean) throws java.beans.PropertyVetoException
meth public void setMenuBar(javax.swing.JMenuBar)
 anno 0 java.lang.Deprecated()
meth public void setNormalBounds(java.awt.Rectangle)
meth public void setResizable(boolean)
meth public void setSelected(boolean) throws java.beans.PropertyVetoException
meth public void setTitle(java.lang.String)
meth public void setUI(javax.swing.plaf.InternalFrameUI)
meth public void show()
meth public void toBack()
meth public void toFront()
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

CLSS public javax.swing.JSplitPane
cons public init()
cons public init(int)
 anno 0 java.beans.ConstructorProperties(java.lang.String[] value=["orientation"])
cons public init(int,boolean)
cons public init(int,boolean,java.awt.Component,java.awt.Component)
cons public init(int,java.awt.Component,java.awt.Component)
fld protected boolean continuousLayout
fld protected boolean oneTouchExpandable
fld protected int dividerSize
fld protected int lastDividerLocation
fld protected int orientation
fld protected java.awt.Component leftComponent
fld protected java.awt.Component rightComponent
fld public final static int HORIZONTAL_SPLIT = 1
fld public final static int VERTICAL_SPLIT = 0
fld public final static java.lang.String BOTTOM = "bottom"
fld public final static java.lang.String CONTINUOUS_LAYOUT_PROPERTY = "continuousLayout"
fld public final static java.lang.String DIVIDER = "divider"
fld public final static java.lang.String DIVIDER_LOCATION_PROPERTY = "dividerLocation"
fld public final static java.lang.String DIVIDER_SIZE_PROPERTY = "dividerSize"
fld public final static java.lang.String LAST_DIVIDER_LOCATION_PROPERTY = "lastDividerLocation"
fld public final static java.lang.String LEFT = "left"
fld public final static java.lang.String ONE_TOUCH_EXPANDABLE_PROPERTY = "oneTouchExpandable"
fld public final static java.lang.String ORIENTATION_PROPERTY = "orientation"
fld public final static java.lang.String RESIZE_WEIGHT_PROPERTY = "resizeWeight"
fld public final static java.lang.String RIGHT = "right"
fld public final static java.lang.String TOP = "top"
innr protected AccessibleJSplitPane
intf javax.accessibility.Accessible
meth protected java.lang.String paramString()
meth protected void addImpl(java.awt.Component,java.lang.Object,int)
meth protected void paintChildren(java.awt.Graphics)
meth public boolean isContinuousLayout()
meth public boolean isOneTouchExpandable()
meth public boolean isValidateRoot()
meth public double getResizeWeight()
meth public int getDividerLocation()
meth public int getDividerSize()
meth public int getLastDividerLocation()
meth public int getMaximumDividerLocation()
meth public int getMinimumDividerLocation()
meth public int getOrientation()
meth public java.awt.Component getBottomComponent()
meth public java.awt.Component getLeftComponent()
meth public java.awt.Component getRightComponent()
meth public java.awt.Component getTopComponent()
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.plaf.SplitPaneUI getUI()
meth public void remove(int)
meth public void remove(java.awt.Component)
meth public void removeAll()
meth public void resetToPreferredSizes()
meth public void setBottomComponent(java.awt.Component)
meth public void setContinuousLayout(boolean)
meth public void setDividerLocation(double)
meth public void setDividerLocation(int)
meth public void setDividerSize(int)
meth public void setLastDividerLocation(int)
meth public void setLeftComponent(java.awt.Component)
meth public void setOneTouchExpandable(boolean)
meth public void setOrientation(int)
meth public void setResizeWeight(double)
meth public void setRightComponent(java.awt.Component)
meth public void setTopComponent(java.awt.Component)
meth public void setUI(javax.swing.plaf.SplitPaneUI)
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

CLSS public abstract interface javax.swing.RootPaneContainer
meth public abstract java.awt.Component getGlassPane()
meth public abstract java.awt.Container getContentPane()
meth public abstract javax.swing.JLayeredPane getLayeredPane()
meth public abstract javax.swing.JRootPane getRootPane()
meth public abstract void setContentPane(java.awt.Container)
meth public abstract void setGlassPane(java.awt.Component)
meth public abstract void setLayeredPane(javax.swing.JLayeredPane)

CLSS public abstract interface javax.swing.Scrollable
meth public abstract boolean getScrollableTracksViewportHeight()
meth public abstract boolean getScrollableTracksViewportWidth()
meth public abstract int getScrollableBlockIncrement(java.awt.Rectangle,int,int)
meth public abstract int getScrollableUnitIncrement(java.awt.Rectangle,int,int)
meth public abstract java.awt.Dimension getPreferredScrollableViewportSize()

CLSS public abstract interface javax.swing.WindowConstants
fld public final static int DISPOSE_ON_CLOSE = 2
fld public final static int DO_NOTHING_ON_CLOSE = 0
fld public final static int EXIT_ON_CLOSE = 3
fld public final static int HIDE_ON_CLOSE = 1

CLSS public abstract interface javax.swing.event.CellEditorListener
intf java.util.EventListener
meth public abstract void editingCanceled(javax.swing.event.ChangeEvent)
meth public abstract void editingStopped(javax.swing.event.ChangeEvent)

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

CLSS public javax.swing.table.DefaultTableModel
cons public init()
cons public init(int,int)
cons public init(java.lang.Object[],int)
cons public init(java.lang.Object[][],java.lang.Object[])
cons public init(java.util.Vector,int)
cons public init(java.util.Vector,java.util.Vector)
fld protected java.util.Vector columnIdentifiers
fld protected java.util.Vector dataVector
intf java.io.Serializable
meth protected static java.util.Vector convertToVector(java.lang.Object[])
meth protected static java.util.Vector convertToVector(java.lang.Object[][])
meth public boolean isCellEditable(int,int)
meth public int getColumnCount()
meth public int getRowCount()
meth public java.lang.Object getValueAt(int,int)
meth public java.lang.String getColumnName(int)
meth public java.util.Vector getDataVector()
meth public void addColumn(java.lang.Object)
meth public void addColumn(java.lang.Object,java.lang.Object[])
meth public void addColumn(java.lang.Object,java.util.Vector)
meth public void addRow(java.lang.Object[])
meth public void addRow(java.util.Vector)
meth public void insertRow(int,java.lang.Object[])
meth public void insertRow(int,java.util.Vector)
meth public void moveRow(int,int,int)
meth public void newDataAvailable(javax.swing.event.TableModelEvent)
meth public void newRowsAdded(javax.swing.event.TableModelEvent)
meth public void removeRow(int)
meth public void rowsRemoved(javax.swing.event.TableModelEvent)
meth public void setColumnCount(int)
meth public void setColumnIdentifiers(java.lang.Object[])
meth public void setColumnIdentifiers(java.util.Vector)
meth public void setDataVector(java.lang.Object[][],java.lang.Object[])
meth public void setDataVector(java.util.Vector,java.util.Vector)
meth public void setNumRows(int)
meth public void setRowCount(int)
meth public void setValueAt(java.lang.Object,int,int)
supr javax.swing.table.AbstractTableModel

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

CLSS public abstract javax.swing.text.AbstractDocument
cons protected init(javax.swing.text.AbstractDocument$Content)
cons protected init(javax.swing.text.AbstractDocument$Content,javax.swing.text.AbstractDocument$AttributeContext)
fld protected final static java.lang.String BAD_LOCATION = "document location failure"
fld protected javax.swing.event.EventListenerList listenerList
fld public final static java.lang.String BidiElementName = "bidi level"
fld public final static java.lang.String ContentElementName = "content"
fld public final static java.lang.String ElementNameAttribute = "$ename"
fld public final static java.lang.String ParagraphElementName = "paragraph"
fld public final static java.lang.String SectionElementName = "section"
innr public BranchElement
innr public DefaultDocumentEvent
innr public LeafElement
innr public abstract AbstractElement
innr public abstract interface static AttributeContext
innr public abstract interface static Content
innr public static ElementEdit
intf java.io.Serializable
intf javax.swing.text.Document
meth protected final java.lang.Thread getCurrentWriter()
meth protected final javax.swing.text.AbstractDocument$AttributeContext getAttributeContext()
meth protected final javax.swing.text.AbstractDocument$Content getContent()
meth protected final void writeLock()
meth protected final void writeUnlock()
meth protected javax.swing.text.Element createBranchElement(javax.swing.text.Element,javax.swing.text.AttributeSet)
meth protected javax.swing.text.Element createLeafElement(javax.swing.text.Element,javax.swing.text.AttributeSet,int,int)
meth protected void fireChangedUpdate(javax.swing.event.DocumentEvent)
meth protected void fireInsertUpdate(javax.swing.event.DocumentEvent)
meth protected void fireRemoveUpdate(javax.swing.event.DocumentEvent)
meth protected void fireUndoableEditUpdate(javax.swing.event.UndoableEditEvent)
meth protected void insertUpdate(javax.swing.text.AbstractDocument$DefaultDocumentEvent,javax.swing.text.AttributeSet)
meth protected void postRemoveUpdate(javax.swing.text.AbstractDocument$DefaultDocumentEvent)
meth protected void removeUpdate(javax.swing.text.AbstractDocument$DefaultDocumentEvent)
meth public <%0 extends java.util.EventListener> {%%0}[] getListeners(java.lang.Class<{%%0}>)
meth public abstract javax.swing.text.Element getDefaultRootElement()
meth public abstract javax.swing.text.Element getParagraphElement(int)
meth public final java.lang.Object getProperty(java.lang.Object)
meth public final javax.swing.text.Position getEndPosition()
meth public final javax.swing.text.Position getStartPosition()
meth public final void putProperty(java.lang.Object,java.lang.Object)
meth public final void readLock()
meth public final void readUnlock()
meth public int getAsynchronousLoadPriority()
meth public int getLength()
meth public java.lang.String getText(int,int) throws javax.swing.text.BadLocationException
meth public java.util.Dictionary<java.lang.Object,java.lang.Object> getDocumentProperties()
meth public javax.swing.event.DocumentListener[] getDocumentListeners()
meth public javax.swing.event.UndoableEditListener[] getUndoableEditListeners()
meth public javax.swing.text.DocumentFilter getDocumentFilter()
meth public javax.swing.text.Element getBidiRootElement()
meth public javax.swing.text.Element[] getRootElements()
meth public javax.swing.text.Position createPosition(int) throws javax.swing.text.BadLocationException
meth public void addDocumentListener(javax.swing.event.DocumentListener)
meth public void addUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public void dump(java.io.PrintStream)
meth public void getText(int,int,javax.swing.text.Segment) throws javax.swing.text.BadLocationException
meth public void insertString(int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth public void remove(int,int) throws javax.swing.text.BadLocationException
meth public void removeDocumentListener(javax.swing.event.DocumentListener)
meth public void removeUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public void render(java.lang.Runnable)
meth public void replace(int,int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth public void setAsynchronousLoadPriority(int)
meth public void setDocumentFilter(javax.swing.text.DocumentFilter)
meth public void setDocumentProperties(java.util.Dictionary<java.lang.Object,java.lang.Object>)
supr java.lang.Object

CLSS public javax.swing.text.DefaultStyledDocument
cons public init()
cons public init(javax.swing.text.AbstractDocument$Content,javax.swing.text.StyleContext)
cons public init(javax.swing.text.StyleContext)
fld protected javax.swing.text.DefaultStyledDocument$ElementBuffer buffer
fld public final static int BUFFER_SIZE_DEFAULT = 4096
innr protected SectionElement
innr public ElementBuffer
innr public static AttributeUndoableEdit
innr public static ElementSpec
intf javax.swing.text.StyledDocument
meth protected javax.swing.text.AbstractDocument$AbstractElement createDefaultRoot()
meth protected void create(javax.swing.text.DefaultStyledDocument$ElementSpec[])
meth protected void insert(int,javax.swing.text.DefaultStyledDocument$ElementSpec[]) throws javax.swing.text.BadLocationException
meth protected void insertUpdate(javax.swing.text.AbstractDocument$DefaultDocumentEvent,javax.swing.text.AttributeSet)
meth protected void removeUpdate(javax.swing.text.AbstractDocument$DefaultDocumentEvent)
meth protected void styleChanged(javax.swing.text.Style)
meth public java.awt.Color getBackground(javax.swing.text.AttributeSet)
meth public java.awt.Color getForeground(javax.swing.text.AttributeSet)
meth public java.awt.Font getFont(javax.swing.text.AttributeSet)
meth public java.util.Enumeration<?> getStyleNames()
meth public javax.swing.text.Element getCharacterElement(int)
meth public javax.swing.text.Element getDefaultRootElement()
meth public javax.swing.text.Element getParagraphElement(int)
meth public javax.swing.text.Style addStyle(java.lang.String,javax.swing.text.Style)
meth public javax.swing.text.Style getLogicalStyle(int)
meth public javax.swing.text.Style getStyle(java.lang.String)
meth public void addDocumentListener(javax.swing.event.DocumentListener)
meth public void removeDocumentListener(javax.swing.event.DocumentListener)
meth public void removeElement(javax.swing.text.Element)
meth public void removeStyle(java.lang.String)
meth public void setCharacterAttributes(int,int,javax.swing.text.AttributeSet,boolean)
meth public void setLogicalStyle(int,javax.swing.text.Style)
meth public void setParagraphAttributes(int,int,javax.swing.text.AttributeSet,boolean)
supr javax.swing.text.AbstractDocument

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

CLSS public abstract interface org.netbeans.api.visual.action.PopupMenuProvider
meth public abstract javax.swing.JPopupMenu getPopupMenu(org.netbeans.api.visual.widget.Widget,java.awt.Point)

CLSS public abstract org.netbeans.api.visual.graph.GraphScene<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
innr public abstract static StringGraph
meth protected abstract org.netbeans.api.visual.widget.Widget attachEdgeWidget({org.netbeans.api.visual.graph.GraphScene%1})
meth protected abstract org.netbeans.api.visual.widget.Widget attachNodeWidget({org.netbeans.api.visual.graph.GraphScene%0})
meth protected abstract void attachEdgeSourceAnchor({org.netbeans.api.visual.graph.GraphScene%1},{org.netbeans.api.visual.graph.GraphScene%0},{org.netbeans.api.visual.graph.GraphScene%0})
meth protected abstract void attachEdgeTargetAnchor({org.netbeans.api.visual.graph.GraphScene%1},{org.netbeans.api.visual.graph.GraphScene%0},{org.netbeans.api.visual.graph.GraphScene%0})
meth protected void detachEdgeWidget({org.netbeans.api.visual.graph.GraphScene%1},org.netbeans.api.visual.widget.Widget)
meth protected void detachNodeWidget({org.netbeans.api.visual.graph.GraphScene%0},org.netbeans.api.visual.widget.Widget)
meth protected void notifyEdgeAdded({org.netbeans.api.visual.graph.GraphScene%1},org.netbeans.api.visual.widget.Widget)
meth protected void notifyNodeAdded({org.netbeans.api.visual.graph.GraphScene%0},org.netbeans.api.visual.widget.Widget)
meth public boolean isEdge(java.lang.Object)
meth public boolean isNode(java.lang.Object)
meth public final java.util.Collection<{org.netbeans.api.visual.graph.GraphScene%0}> getNodes()
meth public final java.util.Collection<{org.netbeans.api.visual.graph.GraphScene%1}> findEdgesBetween({org.netbeans.api.visual.graph.GraphScene%0},{org.netbeans.api.visual.graph.GraphScene%0})
meth public final java.util.Collection<{org.netbeans.api.visual.graph.GraphScene%1}> findNodeEdges({org.netbeans.api.visual.graph.GraphScene%0},boolean,boolean)
meth public final java.util.Collection<{org.netbeans.api.visual.graph.GraphScene%1}> getEdges()
meth public final org.netbeans.api.visual.widget.Widget addEdge({org.netbeans.api.visual.graph.GraphScene%1})
meth public final org.netbeans.api.visual.widget.Widget addNode({org.netbeans.api.visual.graph.GraphScene%0})
meth public final void removeEdge({org.netbeans.api.visual.graph.GraphScene%1})
meth public final void removeNode({org.netbeans.api.visual.graph.GraphScene%0})
meth public final void removeNodeWithEdges({org.netbeans.api.visual.graph.GraphScene%0})
meth public final void setEdgeSource({org.netbeans.api.visual.graph.GraphScene%1},{org.netbeans.api.visual.graph.GraphScene%0})
meth public final void setEdgeTarget({org.netbeans.api.visual.graph.GraphScene%1},{org.netbeans.api.visual.graph.GraphScene%0})
meth public final {org.netbeans.api.visual.graph.GraphScene%0} getEdgeSource({org.netbeans.api.visual.graph.GraphScene%1})
meth public final {org.netbeans.api.visual.graph.GraphScene%0} getEdgeTarget({org.netbeans.api.visual.graph.GraphScene%1})
supr org.netbeans.api.visual.model.ObjectScene
hfds edgeSourceNodes,edgeTargetNodes,edges,edgesUm,nodeInputEdges,nodeOutputEdges,nodes,nodesUm

CLSS public org.netbeans.api.visual.model.ObjectScene
cons public init()
meth protected org.netbeans.api.visual.model.ObjectState findObjectState(java.lang.Object)
meth public !varargs final void addObject(java.lang.Object,org.netbeans.api.visual.widget.Widget[])
meth public !varargs final void addObjectSceneListener(org.netbeans.api.visual.model.ObjectSceneListener,org.netbeans.api.visual.model.ObjectSceneEventType[])
meth public !varargs final void removeObjectSceneListener(org.netbeans.api.visual.model.ObjectSceneListener,org.netbeans.api.visual.model.ObjectSceneEventType[])
meth public final boolean isObject(java.lang.Object)
meth public final java.lang.Object findObject(org.netbeans.api.visual.widget.Widget)
meth public final java.lang.Object findStoredObject(java.lang.Object)
meth public final java.lang.Object getFocusedObject()
meth public final java.lang.Object getHoveredObject()
meth public final java.util.List<org.netbeans.api.visual.widget.Widget> findWidgets(java.lang.Object)
meth public final java.util.Set<?> getHighlightedObjects()
meth public final java.util.Set<?> getObjects()
meth public final java.util.Set<?> getSelectedObjects()
meth public final org.netbeans.api.visual.action.WidgetAction createObjectHoverAction()
meth public final org.netbeans.api.visual.action.WidgetAction createSelectAction()
meth public final org.netbeans.api.visual.model.ObjectState getObjectState(java.lang.Object)
meth public final org.netbeans.api.visual.widget.Widget findWidget(java.lang.Object)
meth public final void removeObject(java.lang.Object)
meth public final void removeObjectMapping(java.lang.Object)
meth public final void setFocusedObject(java.lang.Object)
meth public final void setHighlightedObjects(java.util.Set<?>)
meth public final void setHoveredObject(java.lang.Object)
meth public final void setSelectedObjects(java.util.Set<?>)
meth public java.lang.Comparable getIdentityCode(java.lang.Object)
meth public void clearObjectState(java.lang.Object)
meth public void userSelectionSuggested(java.util.Set<?>,boolean)
supr org.netbeans.api.visual.widget.Scene
hfds EMPTY_LISTENERS,EMPTY_SET,EMPTY_WIDGETS_ARRAY,EMPTY_WIDGETS_LIST,event,focusedObject,highlightedObjects,highlightedObjectsUm,hoveredObject,listeners,object2widget,object2widgets,objectHoverAction,objectStates,objects,objectsUm,selectAction,selectedObjects,selectedObjectsUm,widget2object
hcls ObjectHoverProvider,ObjectSelectProvider

CLSS public org.netbeans.api.visual.widget.Scene
cons public init()
innr public abstract interface static SceneListener
meth protected boolean isRepaintRequiredForRevalidating()
meth public boolean isValidated()
meth public final double getZoomFactor()
meth public final java.awt.Graphics2D getGraphics()
meth public final java.awt.Point convertSceneToView(java.awt.Point)
meth public final java.awt.Rectangle convertSceneToView(java.awt.Rectangle)
meth public final java.awt.Rectangle getMaximumBounds()
meth public final java.lang.String getActiveTool()
meth public final org.netbeans.api.visual.action.WidgetAction$Chain getPriorActions()
meth public final org.netbeans.api.visual.animator.SceneAnimator getSceneAnimator()
meth public final org.netbeans.api.visual.laf.InputBindings getInputBindings()
meth public final org.netbeans.api.visual.laf.LookFeel getLookFeel()
meth public final org.netbeans.api.visual.widget.EventProcessingType getKeyEventProcessingType()
meth public final org.netbeans.api.visual.widget.Widget getFocusedWidget()
meth public final void addSceneListener(org.netbeans.api.visual.widget.Scene$SceneListener)
meth public final void paint(java.awt.Graphics2D)
meth public final void removeSceneListener(org.netbeans.api.visual.widget.Scene$SceneListener)
meth public final void setFocusedWidget(org.netbeans.api.visual.widget.Widget)
meth public final void setKeyEventProcessingType(org.netbeans.api.visual.widget.EventProcessingType)
meth public final void setLookFeel(org.netbeans.api.visual.laf.LookFeel)
meth public final void setMaximumBounds(java.awt.Rectangle)
meth public final void setZoomFactor(double)
meth public final void validate()
meth public final void validate(java.awt.Graphics2D)
meth public java.awt.Font getDefaultFont()
meth public java.awt.Point convertViewToScene(java.awt.Point)
meth public java.awt.Rectangle convertViewToScene(java.awt.Rectangle)
meth public javax.swing.JComponent createSatelliteView()
meth public javax.swing.JComponent createView()
meth public javax.swing.JComponent getView()
meth public org.netbeans.api.visual.action.WidgetAction createWidgetHoverAction()
meth public org.netbeans.api.visual.widget.BirdViewController createBirdView()
meth public org.netbeans.api.visual.widget.ResourceTable getResourceTable()
meth public void setActiveTool(java.lang.String)
meth public void setResourceTable(org.netbeans.api.visual.widget.ResourceTable)
supr org.netbeans.api.visual.widget.Widget
hfds activeTool,component,defaultFont,extendSceneOnly,focusedWidget,graphics,inputBindings,keyEventProcessingType,lookFeel,maximumBounds,paintEverything,priorActions,repaintRegion,repaintWidgets,resourceTable,sceneAnimator,sceneListeners,viewShowing,widgetHoverAction,zoomFactor
hcls WidgetHoverAction

CLSS public org.netbeans.api.visual.widget.Widget
cons public init(org.netbeans.api.visual.widget.Scene)
innr public abstract interface static Dependency
intf javax.accessibility.Accessible
intf org.openide.util.Lookup$Provider
meth protected boolean isRepaintRequiredForRevalidating()
meth protected final void updateResources(org.netbeans.api.visual.widget.Widget,boolean)
meth protected java.awt.Cursor getCursorAt(java.awt.Point)
meth protected java.awt.Graphics2D getGraphics()
meth protected java.awt.Rectangle calculateClientArea()
meth protected void notifyAdded()
meth protected void notifyBackgroundChanged(java.awt.Paint)
meth protected void notifyFontChanged(java.awt.Font)
meth protected void notifyForegroundChanged(java.awt.Color)
meth protected void notifyRemoved()
meth protected void notifyStateChanged(org.netbeans.api.visual.model.ObjectState,org.netbeans.api.visual.model.ObjectState)
meth protected void paintBackground()
meth protected void paintBorder()
meth protected void paintChildren()
meth protected void paintWidget()
meth public boolean isHitAt(java.awt.Point)
meth public boolean isValidated()
meth public final boolean equals(java.lang.Object)
meth public final boolean isCheckClipping()
meth public final boolean isEnabled()
meth public final boolean isOpaque()
meth public final boolean isPreferredBoundsSet()
meth public final boolean isVisible()
meth public final int hashCode()
meth public final java.awt.Color getForeground()
meth public final java.awt.Cursor getCursor()
meth public final java.awt.Dimension getMaximumSize()
meth public final java.awt.Dimension getMinimumSize()
meth public final java.awt.Dimension getPreferredSize()
meth public final java.awt.Font getFont()
meth public final java.awt.Paint getBackground()
meth public final java.awt.Point convertLocalToScene(java.awt.Point)
meth public final java.awt.Point convertSceneToLocal(java.awt.Point)
meth public final java.awt.Point getLocation()
meth public final java.awt.Point getPreferredLocation()
meth public final java.awt.Rectangle convertLocalToScene(java.awt.Rectangle)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public final java.awt.Rectangle convertSceneToLocal(java.awt.Rectangle)
meth public final java.awt.Rectangle getBounds()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final java.awt.Rectangle getClientArea()
meth public final java.awt.Rectangle getPreferredBounds()
meth public final java.lang.Object getChildConstraint(org.netbeans.api.visual.widget.Widget)
meth public final java.lang.String getToolTipText()
meth public final java.util.Collection<org.netbeans.api.visual.widget.Widget$Dependency> getDependencies()
meth public final java.util.List<org.netbeans.api.visual.widget.Widget> getChildren()
meth public final javax.accessibility.AccessibleContext getAccessibleContext()
meth public final org.netbeans.api.visual.action.WidgetAction$Chain createActions(java.lang.String)
meth public final org.netbeans.api.visual.action.WidgetAction$Chain getActions()
meth public final org.netbeans.api.visual.action.WidgetAction$Chain getActions(java.lang.String)
meth public final org.netbeans.api.visual.border.Border getBorder()
meth public final org.netbeans.api.visual.layout.Layout getLayout()
meth public final org.netbeans.api.visual.model.ObjectState getState()
meth public final org.netbeans.api.visual.widget.Scene getScene()
meth public final org.netbeans.api.visual.widget.Widget getParentWidget()
meth public final void addChild(int,org.netbeans.api.visual.widget.Widget)
meth public final void addChild(int,org.netbeans.api.visual.widget.Widget,java.lang.Object)
meth public final void addChild(org.netbeans.api.visual.widget.Widget)
meth public final void addChild(org.netbeans.api.visual.widget.Widget,java.lang.Object)
meth public final void addChildren(java.util.List<? extends org.netbeans.api.visual.widget.Widget>)
meth public final void addDependency(org.netbeans.api.visual.widget.Widget$Dependency)
meth public final void bringToBack()
meth public final void bringToFront()
meth public final void paint()
meth public final void removeChild(org.netbeans.api.visual.widget.Widget)
meth public final void removeChildren()
meth public final void removeChildren(java.util.List<org.netbeans.api.visual.widget.Widget>)
meth public final void removeDependency(org.netbeans.api.visual.widget.Widget$Dependency)
meth public final void removeFromParent()
meth public final void repaint()
meth public final void resolveBounds(java.awt.Point,java.awt.Rectangle)
meth public final void revalidate()
meth public final void revalidate(boolean)
meth public final void setAccessibleContext(javax.accessibility.AccessibleContext)
meth public final void setBackground(java.awt.Paint)
meth public final void setBackgroundFromResource(java.lang.String)
meth public final void setBorder(javax.swing.border.Border)
meth public final void setBorder(org.netbeans.api.visual.border.Border)
meth public final void setCheckClipping(boolean)
meth public final void setChildConstraint(org.netbeans.api.visual.widget.Widget,java.lang.Object)
meth public final void setCursor(java.awt.Cursor)
meth public final void setEnabled(boolean)
meth public final void setFont(java.awt.Font)
meth public final void setFontFromResource(java.lang.String)
meth public final void setForeground(java.awt.Color)
meth public final void setForegroundFromResource(java.lang.String)
meth public final void setLayout(org.netbeans.api.visual.layout.Layout)
meth public final void setMaximumSize(java.awt.Dimension)
meth public final void setMinimumSize(java.awt.Dimension)
meth public final void setOpaque(boolean)
meth public final void setPreferredBounds(java.awt.Rectangle)
meth public final void setPreferredLocation(java.awt.Point)
meth public final void setPreferredSize(java.awt.Dimension)
meth public final void setState(org.netbeans.api.visual.model.ObjectState)
meth public final void setToolTipText(java.lang.String)
meth public final void setVisible(boolean)
meth public org.netbeans.api.visual.widget.ResourceTable getResourceTable()
meth public org.openide.util.Lookup getLookup()
meth public void setResourceTable(org.netbeans.api.visual.widget.ResourceTable)
supr java.lang.Object
hfds EMPTY_HASH_MAP,MESSAGE_NULL_BOUNDS,accessibleContext,actionsChain,background,backgroundListener,backgroundProperty,border,bounds,calculatedPreferredBounds,checkClipping,children,childrenUm,constraints,cursor,dependencies,enabled,font,fontListener,fontProperties,foreground,foregroundListener,foregroundProperty,layout,location,maximumSize,minimumSize,opaque,parentWidget,preferredBounds,preferredLocation,preferredSize,requiresFullJustification,requiresFullValidation,requiresPartJustification,requiresPartValidation,resourceTable,scene,state,toolTipText,toolsActions,visible

CLSS public abstract interface org.netbeans.modules.db.spi.sql.visualeditor.VisualSQLEditorProvider
meth public abstract void openVisualSQLEditor(org.netbeans.api.db.explorer.DatabaseConnection,java.lang.String)

CLSS public final org.netbeans.modules.db.sql.visualeditor.api.VisualSQLEditor
fld public final static java.lang.String PROP_STATEMENT = "STATEMENT"
meth public java.awt.Component open()
meth public java.lang.String getStatement()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setStatement(java.lang.String)
supr java.lang.Object
hfds changeSupport,dbconn,metadata,queryBuilder,statement

CLSS public final org.netbeans.modules.db.sql.visualeditor.api.VisualSQLEditorFactory
meth public static org.netbeans.modules.db.sql.visualeditor.api.VisualSQLEditor createVisualSQLEditor(org.netbeans.api.db.explorer.DatabaseConnection,java.lang.String,org.netbeans.modules.db.sql.visualeditor.api.VisualSQLEditorMetaData)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.db.sql.visualeditor.api.VisualSQLEditorMetaData
meth public abstract java.lang.String getIdentifierQuoteString() throws java.sql.SQLException
meth public abstract java.util.List<java.lang.String> getColumns(java.lang.String,java.lang.String) throws java.sql.SQLException
meth public abstract java.util.List<java.lang.String> getPrimaryKeys(java.lang.String,java.lang.String) throws java.sql.SQLException
meth public abstract java.util.List<java.lang.String> getSchemas()
meth public abstract java.util.List<java.util.List<java.lang.String>> getExportedKeys(java.lang.String,java.lang.String) throws java.sql.SQLException
meth public abstract java.util.List<java.util.List<java.lang.String>> getImportedKeys(java.lang.String,java.lang.String) throws java.sql.SQLException
meth public abstract java.util.List<java.util.List<java.lang.String>> getTables() throws java.sql.SQLException

CLSS public org.netbeans.modules.db.sql.visualeditor.querybuilder.AddQueryParameterDlg
cons public init()
cons public init(boolean,java.lang.String)
fld public final static int RET_CANCEL = 0
fld public final static int RET_OK = 1
meth public int getReturnStatus()
meth public java.lang.String getCriteria()
meth public void setColumnName(java.lang.String)
supr javax.swing.JPanel
hfds HEIGHT,WIDTH,_columnName,buttonGroup1,columnNameLbl,comparisonComboBox,comparisonHintLbl,comparisonLbl,dialog,dispColumnNameLbl,fillerLbl,instructions,mainPanel,parmRadioBtn,parmTxtField,parmTxtFieldLbl,radioButtonPanel,returnStatus,valueRadioBtn,valueTxtField,valueTxtFieldLbl

CLSS public org.netbeans.modules.db.sql.visualeditor.querybuilder.AddTableDlg
cons public init()
cons public init(java.lang.String[],boolean)
fld public final static int RET_CANCEL = 0
fld public final static int RET_OK = 1
meth public int getReturnStatus()
meth public java.lang.Object[] getSelectedValues()
supr javax.swing.JPanel
hfds _mainPanel,_tableJList,_tableList,_tableScrollPane,dialog,returnStatus,tableListLabel

CLSS public org.netbeans.modules.db.sql.visualeditor.querybuilder.CondNode
innr public ColumnPropertyEditor1
innr public ColumnPropertyEditor2
innr public JoinTypePropertyEditor
meth protected org.openide.nodes.Sheet createSheet()
meth public java.lang.String getColumn1()
meth public java.lang.String getColumn2()
meth public java.lang.String getTable1()
meth public java.lang.String getTable2()
meth public java.lang.String toString()
meth public void setColumn1(java.lang.String)
meth public void setColumn2(java.lang.String)
supr org.openide.nodes.AbstractNode
hfds _column1,_column2,_queryBuilder,_table1,_table2

CLSS public org.netbeans.modules.db.sql.visualeditor.querybuilder.CondNode$ColumnPropertyEditor1
 outer org.netbeans.modules.db.sql.visualeditor.querybuilder.CondNode
cons public init(org.netbeans.modules.db.sql.visualeditor.querybuilder.CondNode)
meth public java.lang.String[] getTags()
supr java.beans.PropertyEditorSupport

CLSS public org.netbeans.modules.db.sql.visualeditor.querybuilder.CondNode$ColumnPropertyEditor2
 outer org.netbeans.modules.db.sql.visualeditor.querybuilder.CondNode
cons public init(org.netbeans.modules.db.sql.visualeditor.querybuilder.CondNode)
meth public java.lang.String[] getTags()
supr java.beans.PropertyEditorSupport

CLSS public org.netbeans.modules.db.sql.visualeditor.querybuilder.CondNode$JoinTypePropertyEditor
 outer org.netbeans.modules.db.sql.visualeditor.querybuilder.CondNode
cons public init(org.netbeans.modules.db.sql.visualeditor.querybuilder.CondNode)
meth public java.lang.String[] getTags()
supr java.beans.PropertyEditorSupport
hfds tags

CLSS public org.netbeans.modules.db.sql.visualeditor.querybuilder.InternalVSEMetaDataImpl
cons public init(org.netbeans.api.db.explorer.DatabaseConnection)
intf org.netbeans.modules.db.sql.visualeditor.api.VisualSQLEditorMetaData
meth public java.lang.String getIdentifierQuoteString() throws java.sql.SQLException
meth public java.util.List<java.lang.String> getColumns(java.lang.String,java.lang.String) throws java.sql.SQLException
meth public java.util.List<java.lang.String> getPrimaryKeys(java.lang.String,java.lang.String) throws java.sql.SQLException
meth public java.util.List<java.lang.String> getSchemas()
meth public java.util.List<java.util.List<java.lang.String>> getExportedKeys(java.lang.String,java.lang.String) throws java.sql.SQLException
meth public java.util.List<java.util.List<java.lang.String>> getImportedKeys(java.lang.String,java.lang.String) throws java.sql.SQLException
meth public java.util.List<java.util.List<java.lang.String>> getTables() throws java.sql.SQLException
supr java.lang.Object
hfds allColumnsTable,allTables,columnNameTable,databaseMetaData,dbconn,fkExportedTable,fkImportedTable,hashSizeForTables,identifierQuoteString,pkTable,schemas

CLSS public org.netbeans.modules.db.sql.visualeditor.querybuilder.JoinNode
innr public ColumnPropertyEditor1
innr public ColumnPropertyEditor2
innr public JoinTypePropertyEditor
meth protected org.openide.nodes.Sheet createSheet()
meth public java.lang.String getColumn1()
meth public java.lang.String getColumn2()
meth public java.lang.String getTable1()
meth public java.lang.String getTable2()
meth public java.lang.String getType()
meth public java.lang.String toString()
meth public void setColumn1(java.lang.String)
meth public void setColumn2(java.lang.String)
meth public void setType(java.lang.String)
supr org.openide.nodes.AbstractNode
hfds _column1,_column2,_queryBuilder,_table1,_table2,_type

CLSS public org.netbeans.modules.db.sql.visualeditor.querybuilder.JoinNode$ColumnPropertyEditor1
 outer org.netbeans.modules.db.sql.visualeditor.querybuilder.JoinNode
cons public init(org.netbeans.modules.db.sql.visualeditor.querybuilder.JoinNode)
meth public java.lang.String[] getTags()
supr java.beans.PropertyEditorSupport

CLSS public org.netbeans.modules.db.sql.visualeditor.querybuilder.JoinNode$ColumnPropertyEditor2
 outer org.netbeans.modules.db.sql.visualeditor.querybuilder.JoinNode
cons public init(org.netbeans.modules.db.sql.visualeditor.querybuilder.JoinNode)
meth public java.lang.String[] getTags()
supr java.beans.PropertyEditorSupport

CLSS public org.netbeans.modules.db.sql.visualeditor.querybuilder.JoinNode$JoinTypePropertyEditor
 outer org.netbeans.modules.db.sql.visualeditor.querybuilder.JoinNode
cons public init(org.netbeans.modules.db.sql.visualeditor.querybuilder.JoinNode)
meth public java.lang.String[] getTags()
supr java.beans.PropertyEditorSupport
hfds tags

CLSS public org.netbeans.modules.db.sql.visualeditor.querybuilder.ParameterizedQueryDialog
cons public init()
cons public init(java.lang.String[],boolean)
fld public final static int PARAMETER_COLUMN = 0
fld public final static int RETURNED_CANCEL = 0
fld public final static int RETURNED_OK = 1
fld public final static int VALUE_COLUMN = 1
meth public int getReturnStatus()
meth public java.lang.String[] getParameterValues()
meth public java.lang.String[] getParameters()
meth public void setParameterValues(java.lang.String[])
meth public void setParameters(java.lang.String[])
supr javax.swing.JPanel
hfds _pTableModel,dialog,dlg,mainPanel,messageArea,messageAreaPanel,messageAreaTablePanel,parameterValueTable,parameterValueTablePanel,parameterValueTableScrollPane,returnStatus
hcls FocusCellEditor,ParameterizedTable,ParameterizedTableModel

CLSS public org.netbeans.modules.db.sql.visualeditor.querybuilder.QBGraphScene
cons public init(org.netbeans.modules.db.sql.visualeditor.querybuilder.QueryBuilderGraphFrame)
meth protected org.netbeans.api.visual.widget.Widget attachEdgeWidget(java.lang.Object)
meth protected org.netbeans.api.visual.widget.Widget attachNodeWidget(java.lang.Object)
meth protected void attachEdgeSourceAnchor(java.lang.Object,java.lang.Object,java.lang.Object)
meth protected void attachEdgeTargetAnchor(java.lang.Object,java.lang.Object,java.lang.Object)
meth public org.netbeans.api.visual.widget.LayerWidget getConnectionLayer()
meth public org.netbeans.api.visual.widget.LayerWidget getMainLayer()
meth public org.netbeans.api.visual.widget.Widget addNode(java.lang.String,org.netbeans.modules.db.sql.visualeditor.querybuilder.QueryBuilderTableModel)
supr org.netbeans.api.visual.graph.GraphScene
hfds connectionLayer,mainLayer,mouseHoverAction,moveAction,pos,router
hcls MyHoverProvider

CLSS public org.netbeans.modules.db.sql.visualeditor.querybuilder.QBNodeComponent
cons public init(java.lang.String,org.netbeans.modules.db.sql.visualeditor.querybuilder.QueryBuilderTableModel)
supr javax.swing.JPanel
hfds _nodeName,_qbTable,_queryBuilder,_queryBuilderTableModel

CLSS public org.netbeans.modules.db.sql.visualeditor.querybuilder.QueryBuilder
intf java.awt.datatransfer.ClipboardOwner
intf java.awt.event.KeyListener
intf org.netbeans.modules.db.sql.visualeditor.querymodel.ColumnProvider
meth protected void componentClosed()
meth protected void componentHidden()
meth protected void componentOpened()
meth protected void componentShowing()
meth public boolean isColumnName(java.lang.String)
meth public boolean isSchemaName(java.lang.String)
meth public boolean isTableName(java.lang.String)
meth public int getPersistenceType()
meth public java.lang.String preferredID()
meth public org.netbeans.modules.db.sql.visualeditor.api.VisualSQLEditor getVisualSQLEditor()
meth public org.netbeans.modules.db.sql.visualeditor.querybuilder.QueryBuilderMetaData getMetaData()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static java.awt.Component open(org.netbeans.api.db.explorer.DatabaseConnection,java.lang.String,org.netbeans.modules.db.sql.visualeditor.api.VisualSQLEditorMetaData,org.netbeans.modules.db.sql.visualeditor.api.VisualSQLEditor)
meth public void activateActions()
meth public void deactivateActions()
meth public void getColumnNames(java.lang.String,java.util.List)
meth public void handleKeyPress(java.awt.event.KeyEvent)
meth public void keyPressed(java.awt.event.KeyEvent)
meth public void keyReleased(java.awt.event.KeyEvent)
meth public void keyTyped(java.awt.event.KeyEvent)
meth public void lostOwnership(java.awt.datatransfer.Clipboard,java.awt.datatransfer.Transferable)
supr org.openide.windows.TopComponent
hfds DEBUG,_graphicsEnabled,_parseErrorMessage,_queryBuilderPane,_queryModel,_updateModel,_updateText,copyActionPerformer,cutActionPerformer,dbconn,deleteActionPerformer,firstTimeGenerateText,lastException,lastQuery,qbMetaData,quoter,statement,vse
hcls CopyCutActionPerformer,DeleteActionPerformer

CLSS public org.netbeans.modules.db.sql.visualeditor.querybuilder.QueryBuilderGraphFrame
cons public init(org.netbeans.modules.db.sql.visualeditor.querybuilder.QueryBuilder,org.netbeans.modules.db.sql.visualeditor.querybuilder.QueryBuilderInputTable,javax.swing.JEditorPane,javax.swing.table.DefaultTableModel)
innr public PerfTimer
intf java.awt.dnd.DropTargetListener
intf java.awt.event.ActionListener
intf java.awt.event.ItemListener
intf java.awt.event.KeyListener
intf javax.swing.event.TableModelListener
intf org.netbeans.api.visual.action.PopupMenuProvider
meth protected java.lang.String getClassName(java.lang.Object)
meth public boolean checkTableColumnValidity()
meth public javax.swing.JPopupMenu getPopupMenu(org.netbeans.api.visual.widget.Widget,java.awt.Point)
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void addTable()
meth public void dragEnter(java.awt.dnd.DropTargetDragEvent)
meth public void dragExit(java.awt.dnd.DropTargetEvent)
meth public void dragOver(java.awt.dnd.DragSourceDragEvent)
meth public void dragOver(java.awt.dnd.DropTargetDragEvent)
meth public void drop(java.awt.dnd.DropTargetDropEvent)
meth public void dropActionChanged(java.awt.dnd.DragSourceDragEvent)
meth public void dropActionChanged(java.awt.dnd.DropTargetDragEvent)
meth public void itemStateChanged(java.awt.event.ItemEvent)
meth public void keyPressed(java.awt.event.KeyEvent)
meth public void keyReleased(java.awt.event.KeyEvent)
meth public void keyTyped(java.awt.event.KeyEvent)
meth public void removeNode(org.netbeans.modules.db.sql.visualeditor.querybuilder.CondNode)
meth public void removeNode(org.netbeans.modules.db.sql.visualeditor.querybuilder.JoinNode)
meth public void removeNode(org.netbeans.modules.db.sql.visualeditor.querybuilder.TableNode)
meth public void removeTable()
meth public void setCurrentSelectedFrameTitle(java.lang.String)
meth public void setGroupBy(boolean)
meth public void setTableColumnValidity(boolean)
meth public void tableChanged(javax.swing.event.TableModelEvent)
supr javax.swing.JPanel
hfds DEBUG,MAX_TABLES_IN_A_ROW,_addTableDlg,_backgroundPopup,_canvas,_checkTableColumnValidity,_cl,_desktopPane,_desktopScrollPane,_disableQBGF,_dropTarget,_firstTableInserted,_fsl,_gLocation,_inputTableAddCriteria,_inputTableModel,_queryBuilder,_queryBuilderInputTable,_resultTableModel,_scene,_selectedNode,_sqlTextArea,_tableTitlePopup,groupByMenuItem,initX,initY,offsetX,offsetY,randomVal,runQueryMenuItem,url_foreign_key,url_primary_key,viewport
hcls CompListener,FrameSelectionListener,ObjectSelectProvider,QBGFJPanel,TableTitlePopupListener

CLSS public org.netbeans.modules.db.sql.visualeditor.querybuilder.QueryBuilderGraphFrame$PerfTimer
 outer org.netbeans.modules.db.sql.visualeditor.querybuilder.QueryBuilderGraphFrame
cons public init(org.netbeans.modules.db.sql.visualeditor.querybuilder.QueryBuilderGraphFrame)
meth public long elapsedTime()
meth public void print(java.lang.String)
meth public void resetTimer()
supr java.lang.Object
hfds _time

CLSS public org.netbeans.modules.db.sql.visualeditor.querybuilder.QueryBuilderInputTable
cons public init(org.netbeans.modules.db.sql.visualeditor.querybuilder.QueryBuilder)
fld public final static int Alias_COLUMN = 1
fld public final static int Alias_COLUMN_WIDTH = 70
fld public final static int Column_COLUMN = 0
fld public final static int Column_COLUMN_WIDTH = 140
fld public final static int CriteriaOrder_COLUMN = 7
fld public final static int CriteriaOrder_COLUMN_WIDTH = 40
fld public final static int Criteria_COLUMN = 6
fld public final static int Criteria_COLUMN_WIDTH = 100
fld public final static int Output_COLUMN = 3
fld public final static int SortOrder_COLUMN = 5
fld public final static int SortOrder_COLUMN_WIDTH = 80
fld public final static int SortType_COLUMN = 4
fld public final static int SortType_COLUMN_WIDTH = 80
fld public final static int Table_COLUMN = 2
fld public final static int Table_COLUMN_WIDTH = 180
fld public final static java.lang.String CriteriaOrder_Uneditable_String = "*"
fld public final static java.lang.String Criteria_Uneditable_String = "*****"
intf java.awt.event.ActionListener
intf java.awt.event.ItemListener
intf java.awt.event.KeyListener
meth protected java.lang.String getClassName(java.lang.Object)
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void itemStateChanged(java.awt.event.ItemEvent)
meth public void keyPressed(java.awt.event.KeyEvent)
meth public void keyReleased(java.awt.event.KeyEvent)
meth public void keyTyped(java.awt.event.KeyEvent)
supr javax.swing.JTable
hfds DEBUG,_addQueryParameterDlg,_criteriaOrderComboBox,_inputTablePopup,_inputTablePopupColumn,_inputTablePopupRow,_queryBuilder,_sortOrderComboBox
hcls FocusCellEditor,InputTablePopupListener

CLSS public org.netbeans.modules.db.sql.visualeditor.querybuilder.QueryBuilderInternalFrame
cons public init(org.netbeans.modules.db.sql.visualeditor.querybuilder.QueryBuilderTableModel,org.netbeans.modules.db.sql.visualeditor.querybuilder.QueryBuilder)
intf java.awt.dnd.DragGestureListener
intf java.awt.dnd.DragSourceListener
intf java.awt.dnd.DropTargetListener
intf java.awt.event.ActionListener
intf java.awt.event.KeyListener
meth protected java.lang.String getClassName(java.lang.Object)
meth public java.lang.String toString()
meth public org.netbeans.modules.db.sql.visualeditor.querybuilder.QueryBuilderTableModel getQueryBuilderTableModel()
meth public org.netbeans.modules.db.sql.visualeditor.querybuilder.TableNode getNode()
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void create()
meth public void dragDropEnd(java.awt.dnd.DragSourceDropEvent)
meth public void dragEnter(java.awt.dnd.DragSourceDragEvent)
meth public void dragEnter(java.awt.dnd.DropTargetDragEvent)
meth public void dragExit(java.awt.dnd.DragSourceEvent)
meth public void dragExit(java.awt.dnd.DropTargetEvent)
meth public void dragGestureRecognized(java.awt.dnd.DragGestureEvent)
meth public void dragOver(java.awt.dnd.DragSourceDragEvent)
meth public void dragOver(java.awt.dnd.DropTargetDragEvent)
meth public void drop(java.awt.dnd.DropTargetDropEvent)
meth public void dropActionChanged(java.awt.dnd.DragSourceDragEvent)
meth public void dropActionChanged(java.awt.dnd.DropTargetDragEvent)
meth public void keyPressed(java.awt.event.KeyEvent)
meth public void keyReleased(java.awt.event.KeyEvent)
meth public void keyTyped(java.awt.event.KeyEvent)
meth public void setLocation(int,int)
supr javax.swing.JInternalFrame
hfds DEBUG,_dragObject,_dropTarget,_lastX,_lastY,_node,_qbTable,_queryBuilder,_queryBuilderTableModel,_tableColumnPopup
hcls TableColumnPopupListener

CLSS public org.netbeans.modules.db.sql.visualeditor.querybuilder.QueryBuilderMetaData
meth public java.lang.String getIdentifierQuoteString()
meth public java.util.List<java.lang.String> getColumnNames(java.lang.String) throws java.sql.SQLException
meth public void getColumnNames(java.lang.String,java.util.List<java.lang.String>)
supr java.lang.Object
hfds DEBUG,allColumnNames,importKcTable,metadata,queryBuilder

CLSS public org.netbeans.modules.db.sql.visualeditor.querybuilder.QueryBuilderPane
cons public init(org.netbeans.modules.db.sql.visualeditor.querybuilder.QueryBuilder)
meth public void setEnableContainer(java.awt.Container,boolean)
meth public void setQueryBuilderInputTableEnabled(boolean)
supr javax.swing.JSplitPane
hfds DEBUG,DIVIDER_SIZE,_keyTyped,_queryBuilder,_queryBuilderGraphFrame,_queryBuilderInputTable,_queryBuilderResultTable,_queryBuilderSqlTextArea,_sceneView,bottomPanel,buttonPanel,graphScrollPane,graphScrollPaneEnabled,qbInputTableSP,qbInputTableSPEnabled,qbgfLabel,qbitLabel,qbstaLabel

CLSS public org.netbeans.modules.db.sql.visualeditor.querybuilder.QueryBuilderResultTable
cons public init()
cons public init(org.netbeans.modules.db.sql.visualeditor.querybuilder.QueryBuilder)
intf java.awt.event.ActionListener
intf java.awt.event.KeyListener
meth public boolean displayResultSet(java.sql.ResultSet,int,boolean)
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void displayResultSet(java.sql.ResultSet)
meth public void keyPressed(java.awt.event.KeyEvent)
meth public void keyReleased(java.awt.event.KeyEvent)
meth public void keyTyped(java.awt.event.KeyEvent)
supr javax.swing.JTable
hfds _queryBuilder,resultTableModel,resultTablePopup
hcls ResultTablePopupListener

CLSS public org.netbeans.modules.db.sql.visualeditor.querybuilder.QueryBuilderSqlCompletion
cons public init(javax.swing.text.JTextComponent,java.lang.String[])
meth public java.lang.String completeText(java.lang.String)
meth public void addDictionaryEntry(java.lang.String)
meth public void insertString(int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
supr javax.swing.text.DefaultStyledDocument
hfds charCount,comp,dictionary,lastOffset

CLSS public org.netbeans.modules.db.sql.visualeditor.querybuilder.QueryBuilderSqlTextArea
cons public init(org.netbeans.modules.db.sql.visualeditor.querybuilder.QueryBuilder)
fld public final static boolean SYNTAX_HIGHLIGHT = true
intf java.awt.event.ActionListener
intf java.awt.event.KeyListener
meth public boolean queryChanged()
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void keyPressed(java.awt.event.KeyEvent)
meth public void keyReleased(java.awt.event.KeyEvent)
meth public void keyTyped(java.awt.event.KeyEvent)
meth public void restoreLastGoodQuery()
meth public void saveLastGoodQuery()
meth public void setParseQueryMenuEnabled(boolean)
meth public void setRunQueryMenuEnabled(boolean)
meth public void setText(java.lang.String)
supr javax.swing.JEditorPane
hfds DEBUG,_lastGoodQuery,_queryBuilder,column,keyword,keywords,parseQueryMenuItem,runQueryMenuItem,schema,sqlReservedWords
hcls sqlTextListener

CLSS public org.netbeans.modules.db.sql.visualeditor.querybuilder.QueryBuilderTable
cons public init(org.netbeans.modules.db.sql.visualeditor.querybuilder.QueryBuilderTableModel)
supr javax.swing.JTable
hfds DEBUG

CLSS public org.netbeans.modules.db.sql.visualeditor.querybuilder.QueryBuilderTableModel
cons public init(java.lang.String,java.lang.String,java.lang.String[],java.lang.Object[][])
meth public boolean isCellEditable(int,int)
meth public java.lang.Class getColumnClass(int)
meth public java.lang.String getCorrName()
meth public java.lang.String getFullTableName()
meth public java.lang.String getTableName()
meth public java.lang.String getTableSpec()
supr javax.swing.table.DefaultTableModel
hfds DEBUG,_corrName,_schemaName,_tableName

CLSS public abstract interface org.netbeans.modules.db.sql.visualeditor.querybuilder.SqlStatement
fld public final static java.lang.String CLOSING = "closing"
fld public final static java.lang.String COMMAND = "command"
fld public final static java.lang.String CONNECTION_INFO = "connectionInfo"
fld public final static java.lang.String TITLE = "title"
meth public abstract java.lang.String getCommand()
meth public abstract java.lang.String getConnectionInfo()
meth public abstract java.lang.String getTitle()
meth public abstract java.sql.Connection getConnection() throws java.sql.SQLException
meth public abstract java.sql.Connection getReadOnlyConnection() throws java.sql.SQLException
meth public abstract org.netbeans.modules.db.sql.visualeditor.api.VisualSQLEditorMetaData getMetaDataCache() throws java.sql.SQLException
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public abstract void close()
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public abstract void setCommand(java.lang.String)
meth public abstract void validateConnection(java.sql.Connection) throws java.sql.SQLException

CLSS public org.netbeans.modules.db.sql.visualeditor.querybuilder.TableNode
meth protected org.openide.nodes.Sheet createSheet()
meth public boolean isAliasValid(java.lang.String)
meth public java.lang.String getCorrName()
meth public java.lang.String getTableName()
meth public void setCorrName(java.lang.String)
supr org.openide.nodes.AbstractNode
hfds DEBUG,SQL_IDENTIFIER_LENGTH,_corrName,_fullTableName,_queryBuilder

CLSS public org.netbeans.modules.db.sql.visualeditor.querybuilder.VisualSQLEditorProviderImpl
cons public init()
intf org.netbeans.modules.db.spi.sql.visualeditor.VisualSQLEditorProvider
meth public void openVisualSQLEditor(org.netbeans.api.db.explorer.DatabaseConnection,java.lang.String)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.db.sql.visualeditor.querymodel.ColumnProvider
meth public abstract void getColumnNames(java.lang.String,java.util.List)

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

