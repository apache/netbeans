#Signature file v4.0
#Version 2.11.1

CLSS public abstract interface com.sun.java.help.impl.ParserListener
intf java.util.EventListener
meth public abstract void commentFound(com.sun.java.help.impl.ParserEvent)
meth public abstract void doctypeFound(com.sun.java.help.impl.ParserEvent)
meth public abstract void errorFound(com.sun.java.help.impl.ParserEvent)
meth public abstract void piFound(com.sun.java.help.impl.ParserEvent)
meth public abstract void tagFound(com.sun.java.help.impl.ParserEvent)
meth public abstract void textFound(com.sun.java.help.impl.ParserEvent)

CLSS public abstract java.awt.Component
cons protected Component()
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
meth public java.awt.peer.ComponentPeer getPeer()
 anno 0 java.lang.Deprecated()
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
hfds FOCUS_TRAVERSABLE_DEFAULT,FOCUS_TRAVERSABLE_SET,FOCUS_TRAVERSABLE_UNKNOWN,LOCK,accessibleContext,actionListenerK,adjustmentListenerK,appContext,background,boundsOp,bufferStrategy,changeSupport,coalesceEventsParams,coalesceMap,coalescingEnabled,componentListener,componentListenerK,componentOrientation,componentSerializedDataVersion,containerListenerK,cursor,dbg,dropTarget,enabled,eventCache,eventMask,focusListener,focusListenerK,focusLog,focusTraversalKeyPropertyNames,focusTraversalKeys,focusTraversalKeysEnabled,focusable,font,foreground,graphicsConfig,height,hierarchyBoundsListener,hierarchyBoundsListenerK,hierarchyListener,hierarchyListenerK,ignoreRepaint,incRate,inputMethodListener,inputMethodListenerK,isFocusTraversableOverridden,isInc,isPacked,itemListenerK,keyListener,keyListenerK,locale,log,maxSize,maxSizeSet,minSize,minSizeSet,mouseListener,mouseListenerK,mouseMotionListener,mouseMotionListenerK,mouseWheelListener,mouseWheelListenerK,name,nameExplicitlySet,nativeInLightFixer,newEventsOnly,ownedWindowK,parent,peer,peerFont,popups,prefSize,prefSizeSet,privateKey,requestFocusController,serialVersionUID,textListenerK,valid,visible,width,windowClosingException,windowFocusListenerK,windowListenerK,windowStateListenerK,x,y
hcls AWTTreeLock,BltSubRegionBufferStrategy,DummyRequestFocusController,FlipSubRegionBufferStrategy,NativeInLightFixer,SingleBufferStrategy

CLSS protected abstract java.awt.Component$AccessibleAWTComponent
cons protected AccessibleAWTComponent(java.awt.Component)
fld protected java.awt.event.ComponentListener accessibleAWTComponentHandler
fld protected java.awt.event.FocusListener accessibleAWTFocusHandler
innr protected AccessibleAWTComponentHandler
innr protected AccessibleAWTFocusHandler
intf java.io.Serializable
intf javax.accessibility.AccessibleComponent
meth public boolean contains(java.awt.Point)
meth public boolean isEnabled()
meth public boolean isFocusTraversable()
meth public boolean isShowing()
meth public boolean isVisible()
meth public int getAccessibleChildrenCount()
meth public int getAccessibleIndexInParent()
meth public java.awt.Color getBackground()
meth public java.awt.Color getForeground()
meth public java.awt.Cursor getCursor()
meth public java.awt.Dimension getSize()
meth public java.awt.Font getFont()
meth public java.awt.FontMetrics getFontMetrics(java.awt.Font)
meth public java.awt.Point getLocation()
meth public java.awt.Point getLocationOnScreen()
meth public java.awt.Rectangle getBounds()
meth public java.lang.String getAccessibleDescription()
meth public java.lang.String getAccessibleName()
meth public java.util.Locale getLocale()
meth public javax.accessibility.Accessible getAccessibleAt(java.awt.Point)
meth public javax.accessibility.Accessible getAccessibleChild(int)
meth public javax.accessibility.Accessible getAccessibleParent()
meth public javax.accessibility.AccessibleComponent getAccessibleComponent()
meth public javax.accessibility.AccessibleRole getAccessibleRole()
meth public javax.accessibility.AccessibleStateSet getAccessibleStateSet()
meth public void addFocusListener(java.awt.event.FocusListener)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removeFocusListener(java.awt.event.FocusListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void requestFocus()
meth public void setBackground(java.awt.Color)
meth public void setBounds(java.awt.Rectangle)
meth public void setCursor(java.awt.Cursor)
meth public void setEnabled(boolean)
meth public void setFont(java.awt.Font)
meth public void setForeground(java.awt.Color)
meth public void setLocation(java.awt.Point)
meth public void setSize(java.awt.Dimension)
meth public void setVisible(boolean)
supr javax.accessibility.AccessibleContext
hfds serialVersionUID

CLSS public java.awt.Container
cons public Container()
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
meth public void transferFocusBackward()
meth public void transferFocusDownCycle()
meth public void update(java.awt.Graphics)
meth public void validate()
supr java.awt.Component
hfds INCLUDE_SELF,SEARCH_HEAVYWEIGHTS,component,containerListener,containerSerializedDataVersion,dbg,descendantsCount,dispatcher,focusCycleRoot,focusTraversalPolicy,focusTraversalPolicyProvider,layoutMgr,listeningBoundsChildren,listeningChildren,modalAppContext,modalComp,ncomponents,printing,printingThreads,serialPersistentFields,serialVersionUID
hcls DropTargetEventTargetFilter,EventTargetFilter,MouseEventTargetFilter,WakingRunnable

CLSS protected java.awt.Container$AccessibleAWTContainer
cons protected AccessibleAWTContainer(java.awt.Container)
fld protected java.awt.event.ContainerListener accessibleContainerHandler
innr protected AccessibleContainerHandler
meth public int getAccessibleChildrenCount()
meth public javax.accessibility.Accessible getAccessibleAt(java.awt.Point)
meth public javax.accessibility.Accessible getAccessibleChild(int)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
supr java.awt.Component$AccessibleAWTComponent
hfds serialVersionUID

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

CLSS public abstract interface java.lang.Cloneable

CLSS public java.lang.Exception
cons public Exception()
cons public Exception(java.lang.String)
cons public Exception(java.lang.String,java.lang.Throwable)
cons public Exception(java.lang.Throwable)
supr java.lang.Throwable
hfds serialVersionUID

CLSS public java.lang.IllegalArgumentException
cons public IllegalArgumentException()
cons public IllegalArgumentException(java.lang.String)
cons public IllegalArgumentException(java.lang.String,java.lang.Throwable)
cons public IllegalArgumentException(java.lang.Throwable)
supr java.lang.RuntimeException
hfds serialVersionUID

CLSS public java.lang.Object
cons public Object()
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

CLSS public java.lang.RuntimeException
cons public RuntimeException()
cons public RuntimeException(java.lang.String)
cons public RuntimeException(java.lang.String,java.lang.Throwable)
cons public RuntimeException(java.lang.Throwable)
supr java.lang.Exception
hfds serialVersionUID

CLSS public java.lang.Throwable
cons public Throwable()
cons public Throwable(java.lang.String)
cons public Throwable(java.lang.String,java.lang.Throwable)
cons public Throwable(java.lang.Throwable)
intf java.io.Serializable
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
hfds backtrace,cause,detailMessage,serialVersionUID,stackTrace

CLSS public abstract interface java.util.EventListener

CLSS public abstract java.util.ResourceBundle
cons public ResourceBundle()
fld protected java.util.ResourceBundle parent
innr public static Control
meth protected abstract java.lang.Object handleGetObject(java.lang.String)
meth protected java.util.Set<java.lang.String> handleKeySet()
meth protected void setParent(java.util.ResourceBundle)
meth public abstract java.util.Enumeration<java.lang.String> getKeys()
meth public boolean containsKey(java.lang.String)
meth public final java.lang.Object getObject(java.lang.String)
meth public final java.lang.String getString(java.lang.String)
meth public final java.lang.String[] getStringArray(java.lang.String)
meth public final static java.util.ResourceBundle getBundle(java.lang.String)
meth public final static java.util.ResourceBundle getBundle(java.lang.String,java.util.Locale)
meth public final static java.util.ResourceBundle getBundle(java.lang.String,java.util.Locale,java.util.ResourceBundle$Control)
meth public final static java.util.ResourceBundle getBundle(java.lang.String,java.util.ResourceBundle$Control)
meth public final static void clearCache()
meth public final static void clearCache(java.lang.ClassLoader)
meth public java.util.Locale getLocale()
meth public java.util.Set<java.lang.String> keySet()
meth public static java.util.ResourceBundle getBundle(java.lang.String,java.util.Locale,java.lang.ClassLoader)
meth public static java.util.ResourceBundle getBundle(java.lang.String,java.util.Locale,java.lang.ClassLoader,java.util.ResourceBundle$Control)
supr java.lang.Object
hfds INITIAL_CACHE_SIZE,NONEXISTENT_BUNDLE,cacheKey,cacheList,expired,keySet,locale,name,referenceQueue,underConstruction
hcls BundleReference,CacheKey,CacheKeyReference,LoaderReference,NoFallbackControl,RBClassLoader,SingleFormatControl

CLSS public abstract interface javax.accessibility.Accessible
meth public abstract javax.accessibility.AccessibleContext getAccessibleContext()

CLSS public abstract interface javax.accessibility.AccessibleComponent
meth public abstract boolean contains(java.awt.Point)
meth public abstract boolean isEnabled()
meth public abstract boolean isFocusTraversable()
meth public abstract boolean isShowing()
meth public abstract boolean isVisible()
meth public abstract java.awt.Color getBackground()
meth public abstract java.awt.Color getForeground()
meth public abstract java.awt.Cursor getCursor()
meth public abstract java.awt.Dimension getSize()
meth public abstract java.awt.Font getFont()
meth public abstract java.awt.FontMetrics getFontMetrics(java.awt.Font)
meth public abstract java.awt.Point getLocation()
meth public abstract java.awt.Point getLocationOnScreen()
meth public abstract java.awt.Rectangle getBounds()
meth public abstract javax.accessibility.Accessible getAccessibleAt(java.awt.Point)
meth public abstract void addFocusListener(java.awt.event.FocusListener)
meth public abstract void removeFocusListener(java.awt.event.FocusListener)
meth public abstract void requestFocus()
meth public abstract void setBackground(java.awt.Color)
meth public abstract void setBounds(java.awt.Rectangle)
meth public abstract void setCursor(java.awt.Cursor)
meth public abstract void setEnabled(boolean)
meth public abstract void setFont(java.awt.Font)
meth public abstract void setForeground(java.awt.Color)
meth public abstract void setLocation(java.awt.Point)
meth public abstract void setSize(java.awt.Dimension)
meth public abstract void setVisible(boolean)

CLSS public abstract javax.accessibility.AccessibleContext
cons public AccessibleContext()
fld protected java.lang.String accessibleDescription
fld protected java.lang.String accessibleName
fld protected javax.accessibility.Accessible accessibleParent
fld public final static java.lang.String ACCESSIBLE_ACTION_PROPERTY = "accessibleActionProperty"
fld public final static java.lang.String ACCESSIBLE_ACTIVE_DESCENDANT_PROPERTY = "AccessibleActiveDescendant"
fld public final static java.lang.String ACCESSIBLE_CARET_PROPERTY = "AccessibleCaret"
fld public final static java.lang.String ACCESSIBLE_CHILD_PROPERTY = "AccessibleChild"
fld public final static java.lang.String ACCESSIBLE_COMPONENT_BOUNDS_CHANGED = "accessibleComponentBoundsChanged"
fld public final static java.lang.String ACCESSIBLE_DESCRIPTION_PROPERTY = "AccessibleDescription"
fld public final static java.lang.String ACCESSIBLE_HYPERTEXT_OFFSET = "AccessibleHypertextOffset"
fld public final static java.lang.String ACCESSIBLE_INVALIDATE_CHILDREN = "accessibleInvalidateChildren"
fld public final static java.lang.String ACCESSIBLE_NAME_PROPERTY = "AccessibleName"
fld public final static java.lang.String ACCESSIBLE_SELECTION_PROPERTY = "AccessibleSelection"
fld public final static java.lang.String ACCESSIBLE_STATE_PROPERTY = "AccessibleState"
fld public final static java.lang.String ACCESSIBLE_TABLE_CAPTION_CHANGED = "accessibleTableCaptionChanged"
fld public final static java.lang.String ACCESSIBLE_TABLE_COLUMN_DESCRIPTION_CHANGED = "accessibleTableColumnDescriptionChanged"
fld public final static java.lang.String ACCESSIBLE_TABLE_COLUMN_HEADER_CHANGED = "accessibleTableColumnHeaderChanged"
fld public final static java.lang.String ACCESSIBLE_TABLE_MODEL_CHANGED = "accessibleTableModelChanged"
fld public final static java.lang.String ACCESSIBLE_TABLE_ROW_DESCRIPTION_CHANGED = "accessibleTableRowDescriptionChanged"
fld public final static java.lang.String ACCESSIBLE_TABLE_ROW_HEADER_CHANGED = "accessibleTableRowHeaderChanged"
fld public final static java.lang.String ACCESSIBLE_TABLE_SUMMARY_CHANGED = "accessibleTableSummaryChanged"
fld public final static java.lang.String ACCESSIBLE_TEXT_ATTRIBUTES_CHANGED = "accessibleTextAttributesChanged"
fld public final static java.lang.String ACCESSIBLE_TEXT_PROPERTY = "AccessibleText"
fld public final static java.lang.String ACCESSIBLE_VALUE_PROPERTY = "AccessibleValue"
fld public final static java.lang.String ACCESSIBLE_VISIBLE_DATA_PROPERTY = "AccessibleVisibleData"
meth public abstract int getAccessibleChildrenCount()
meth public abstract int getAccessibleIndexInParent()
meth public abstract java.util.Locale getLocale()
meth public abstract javax.accessibility.Accessible getAccessibleChild(int)
meth public abstract javax.accessibility.AccessibleRole getAccessibleRole()
meth public abstract javax.accessibility.AccessibleStateSet getAccessibleStateSet()
meth public java.lang.String getAccessibleDescription()
meth public java.lang.String getAccessibleName()
meth public javax.accessibility.Accessible getAccessibleParent()
meth public javax.accessibility.AccessibleAction getAccessibleAction()
meth public javax.accessibility.AccessibleComponent getAccessibleComponent()
meth public javax.accessibility.AccessibleEditableText getAccessibleEditableText()
meth public javax.accessibility.AccessibleIcon[] getAccessibleIcon()
meth public javax.accessibility.AccessibleRelationSet getAccessibleRelationSet()
meth public javax.accessibility.AccessibleSelection getAccessibleSelection()
meth public javax.accessibility.AccessibleTable getAccessibleTable()
meth public javax.accessibility.AccessibleText getAccessibleText()
meth public javax.accessibility.AccessibleValue getAccessibleValue()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setAccessibleDescription(java.lang.String)
meth public void setAccessibleName(java.lang.String)
meth public void setAccessibleParent(javax.accessibility.Accessible)
supr java.lang.Object
hfds accessibleChangeSupport,relationSet

CLSS public abstract interface javax.accessibility.AccessibleExtendedComponent
intf javax.accessibility.AccessibleComponent
meth public abstract java.lang.String getTitledBorderText()
meth public abstract java.lang.String getToolTipText()
meth public abstract javax.accessibility.AccessibleKeyBinding getAccessibleKeyBinding()

CLSS public abstract javax.help.AbstractHelpAction
intf javax.help.HelpAction
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public boolean isEnabled()
meth public java.lang.Object getControl()
meth public java.lang.Object getValue(java.lang.String)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void putValue(java.lang.String,java.lang.Object)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setEnabled(boolean)
supr java.lang.Object
hfds control,enabled,propertyChangeSupport,table

CLSS public javax.help.AppendMerge
cons public AppendMerge(javax.help.NavigatorView,javax.help.NavigatorView)
meth public javax.swing.tree.TreeNode processMerge(javax.swing.tree.TreeNode)
meth public static void mergeNodeChildren(javax.swing.tree.TreeNode)
meth public static void mergeNodes(javax.swing.tree.TreeNode,javax.swing.tree.TreeNode)
supr javax.help.Merge
hfds debug

CLSS public javax.help.BackAction
cons public BackAction(java.lang.Object)
intf java.awt.event.MouseListener
intf javax.help.event.HelpHistoryModelListener
meth public void historyChanged(javax.help.event.HelpHistoryModelEvent)
meth public void mouseClicked(java.awt.event.MouseEvent)
meth public void mouseEntered(java.awt.event.MouseEvent)
meth public void mouseExited(java.awt.event.MouseEvent)
meth public void mousePressed(java.awt.event.MouseEvent)
meth public void mouseReleased(java.awt.event.MouseEvent)
supr javax.help.AbstractHelpAction
hfds DELAY,NAME,historyModel,timer
hcls HistoryActionListener,TimeListener

CLSS public javax.help.BadIDException
cons public BadIDException(java.lang.String,javax.help.Map,java.lang.String,javax.help.HelpSet)
meth public java.lang.String getID()
meth public javax.help.HelpSet getHelpSet()
meth public javax.help.Map getMap()
supr java.lang.IllegalArgumentException
hfds hs,id,map

CLSS public javax.help.CSH
cons public CSH()
innr public abstract interface static Manager
innr public static DisplayHelpAfterTracking
innr public static DisplayHelpFromFocus
innr public static DisplayHelpFromSource
meth public static boolean removeManager(javax.help.CSH$Manager)
meth public static int getManagerCount()
meth public static java.lang.Object trackCSEvents()
meth public static java.lang.String getHelpIDString(java.awt.Component)
meth public static java.lang.String getHelpIDString(java.awt.MenuItem)
meth public static java.lang.String getHelpIDString(java.lang.Object,java.awt.AWTEvent)
meth public static javax.help.CSH$Manager getManager(int)
meth public static javax.help.CSH$Manager[] getManagers()
meth public static javax.help.HelpSet getHelpSet(java.awt.Component)
meth public static javax.help.HelpSet getHelpSet(java.awt.MenuItem)
meth public static javax.help.HelpSet getHelpSet(java.lang.Object,java.awt.AWTEvent)
meth public static void addManager(int,javax.help.CSH$Manager)
meth public static void addManager(javax.help.CSH$Manager)
meth public static void removeAllManagers()
meth public static void removeManager(int)
meth public static void setHelpIDString(java.awt.Component,java.lang.String)
meth public static void setHelpIDString(java.awt.MenuItem,java.lang.String)
meth public static void setHelpSet(java.awt.Component,javax.help.HelpSet)
meth public static void setHelpSet(java.awt.MenuItem,javax.help.HelpSet)
supr java.lang.Object
hfds class$java$lang$String,class$javax$help$HelpSet,comps,debug,managers,parents

CLSS public static javax.help.CSH$DisplayHelpAfterTracking
cons public DisplayHelpAfterTracking(javax.help.HelpBroker)
cons public DisplayHelpAfterTracking(javax.help.HelpSet,java.lang.String,java.lang.String)
intf java.awt.event.ActionListener
meth public void actionPerformed(java.awt.event.ActionEvent)
supr java.lang.Object
hfds cursors,hb,hs,presentation,presentationName

CLSS public static javax.help.CSH$DisplayHelpFromFocus
cons public DisplayHelpFromFocus(javax.help.HelpBroker)
cons public DisplayHelpFromFocus(javax.help.HelpSet,java.lang.String,java.lang.String)
intf java.awt.event.ActionListener
meth public void actionPerformed(java.awt.event.ActionEvent)
supr java.lang.Object
hfds hb,hs,presentation,presentationName

CLSS public static javax.help.CSH$DisplayHelpFromSource
cons public DisplayHelpFromSource(javax.help.HelpBroker)
cons public DisplayHelpFromSource(javax.help.HelpSet,java.lang.String,java.lang.String)
intf java.awt.event.ActionListener
meth public void actionPerformed(java.awt.event.ActionEvent)
supr java.lang.Object
hfds hb,hs,presentation,presentationName

CLSS public abstract interface static javax.help.CSH$Manager
meth public abstract java.lang.String getHelpIDString(java.lang.Object,java.awt.AWTEvent)
meth public abstract javax.help.HelpSet getHelpSet(java.lang.Object,java.awt.AWTEvent)

CLSS public javax.help.DefaultHelpBroker
cons public DefaultHelpBroker()
cons public DefaultHelpBroker(javax.help.HelpSet)
fld protected java.awt.event.ActionListener displayHelpFromFocus
fld protected java.awt.event.ActionListener displayHelpFromSource
intf java.awt.event.KeyListener
intf javax.help.HelpBroker
meth protected java.awt.event.ActionListener getDisplayHelpFromFocus()
meth protected java.awt.event.ActionListener getDisplayHelpFromSource()
meth public boolean isDisplayed()
meth public boolean isViewDisplayed()
meth public int getScreen()
meth public java.awt.Dimension getSize()
meth public java.awt.Font getFont()
meth public java.awt.Point getLocation()
meth public java.lang.String getCurrentView()
meth public java.net.URL getCurrentURL()
meth public java.util.Locale getLocale()
meth public javax.help.HelpSet getHelpSet()
meth public javax.help.Map$ID getCurrentID()
meth public javax.help.WindowPresentation getWindowPresentation()
meth public void enableHelp(java.awt.Component,java.lang.String,javax.help.HelpSet)
meth public void enableHelp(java.awt.MenuItem,java.lang.String,javax.help.HelpSet)
meth public void enableHelpKey(java.awt.Component,java.lang.String,javax.help.HelpSet)
meth public void enableHelpKey(java.awt.Component,java.lang.String,javax.help.HelpSet,java.lang.String,java.lang.String)
meth public void enableHelpOnButton(java.awt.Component,java.lang.String,javax.help.HelpSet)
meth public void enableHelpOnButton(java.awt.MenuItem,java.lang.String,javax.help.HelpSet)
meth public void enableHelpOnButton(java.lang.Object,java.lang.String,javax.help.HelpSet,java.lang.String,java.lang.String)
meth public void initPresentation()
meth public void keyPressed(java.awt.event.KeyEvent)
meth public void keyReleased(java.awt.event.KeyEvent)
meth public void keyTyped(java.awt.event.KeyEvent)
meth public void setActivationObject(java.lang.Object)
meth public void setActivationWindow(java.awt.Window)
meth public void setCurrentID(java.lang.String)
meth public void setCurrentID(javax.help.Map$ID) throws javax.help.InvalidHelpSetContextException
meth public void setCurrentURL(java.net.URL)
meth public void setCurrentView(java.lang.String)
meth public void setDisplayed(boolean)
meth public void setFont(java.awt.Font)
meth public void setHelpSet(javax.help.HelpSet)
meth public void setHelpSetPresentation(javax.help.HelpSet$Presentation)
meth public void setLocale(java.util.Locale)
meth public void setLocation(java.awt.Point)
meth public void setScreen(int)
meth public void setSize(java.awt.Dimension)
meth public void setViewDisplayed(boolean)
meth public void showID(java.lang.String,java.lang.String,java.lang.String)
meth public void showID(javax.help.Map$ID,java.lang.String,java.lang.String) throws javax.help.InvalidHelpSetContextException
supr java.lang.Object
hfds class$java$lang$String,class$javax$help$HelpSet,debug,helpKeyHS,helpKeyPresentation,helpKeyPresentationName,mw

CLSS public javax.help.DefaultHelpHistoryModel
cons public DefaultHelpHistoryModel(javax.help.JHelp)
fld protected int historyIndex
fld protected java.util.Vector history
fld protected javax.help.HelpModel helpModel
fld protected javax.help.JHelp help
fld protected javax.help.event.EventListenerList listenerList
intf javax.help.HelpHistoryModel
meth protected javax.help.HelpModel getModel()
meth protected void fireHistoryChanged(java.lang.Object,boolean,boolean)
meth public int getIndex()
meth public java.util.Vector getBackwardHistory()
meth public java.util.Vector getForwardHistory()
meth public java.util.Vector getHistory()
meth public void addHelpHistoryModelListener(javax.help.event.HelpHistoryModelListener)
meth public void discard()
meth public void goBack()
meth public void goForward()
meth public void idChanged(javax.help.event.HelpModelEvent)
meth public void removeHelpHistoryModelListener(javax.help.event.HelpHistoryModelListener)
meth public void removeHelpSet(javax.help.HelpSet)
meth public void removeLastEntry()
meth public void setHelpModel(javax.help.HelpModel)
meth public void setHistoryEntry(int)
supr java.lang.Object
hfds class$javax$help$event$HelpHistoryModelListener,debug

CLSS public javax.help.DefaultHelpModel
cons public DefaultHelpModel(javax.help.HelpSet)
fld protected java.beans.PropertyChangeSupport changes
fld protected javax.help.event.EventListenerList listenerList
fld protected javax.help.event.EventListenerList textListenerList
innr public static DefaultHighlight
intf java.io.Serializable
intf javax.help.TextHelpModel
meth protected void fireHighlightsChanged(java.lang.Object)
meth protected void fireIDChanged(java.lang.Object,javax.help.Map$ID,java.net.URL)
meth protected void fireIDChanged(java.lang.Object,javax.help.Map$ID,java.net.URL,java.lang.String,javax.help.JHelpNavigator)
meth public java.lang.String getDocumentTitle()
meth public java.net.URL getCurrentURL()
meth public javax.help.HelpSet getHelpSet()
meth public javax.help.Map$ID getCurrentID()
meth public javax.help.TextHelpModel$Highlight[] getHighlights()
meth public void addHelpModelListener(javax.help.event.HelpModelListener)
meth public void addHighlight(int,int)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addTextHelpModelListener(javax.help.event.TextHelpModelListener)
meth public void removeAllHighlights()
meth public void removeHelpModelListener(javax.help.event.HelpModelListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removeTextHelpModelListener(javax.help.event.TextHelpModelListener)
meth public void setCurrentID(javax.help.Map$ID) throws javax.help.InvalidHelpSetContextException
meth public void setCurrentID(javax.help.Map$ID,java.lang.String,javax.help.JHelpNavigator) throws javax.help.InvalidHelpSetContextException
meth public void setCurrentURL(java.net.URL)
meth public void setCurrentURL(java.net.URL,java.lang.String,javax.help.JHelpNavigator)
meth public void setDocumentTitle(java.lang.String)
meth public void setHelpSet(javax.help.HelpSet)
meth public void setHighlights(javax.help.TextHelpModel$Highlight[])
supr java.lang.Object
hfds class$javax$help$event$HelpModelListener,class$javax$help$event$TextHelpModelListener,currentID,currentURL,debug,helpset,highlights,navID,title

CLSS public static javax.help.DefaultHelpModel$DefaultHighlight
cons public DefaultHighlight(int,int)
fld public int end
fld public int start
intf javax.help.TextHelpModel$Highlight
meth public int getEndOffset()
meth public int getStartOffset()
supr java.lang.Object

CLSS public javax.help.FavoritesAction
cons public FavoritesAction(java.lang.Object)
intf java.awt.event.ActionListener
meth public void actionPerformed(java.awt.event.ActionEvent)
supr javax.help.AbstractHelpAction
hfds NAME,favorites,favoritesActionListener

CLSS public javax.help.FavoritesItem
cons public FavoritesItem()
cons public FavoritesItem(java.lang.String)
cons public FavoritesItem(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.util.Locale)
fld public final static java.awt.datatransfer.DataFlavor FAVORITES_FLAVOR
intf java.awt.datatransfer.Transferable
intf java.io.Serializable
meth public boolean allowsChildren()
meth public boolean emptyInitState()
meth public boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
meth public boolean isFolder()
meth public boolean isLeaf()
meth public boolean isVisible()
meth public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors()
meth public java.lang.Object clone()
meth public java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
meth public java.lang.String getHelpSetTitle()
meth public java.lang.String getTarget()
meth public java.lang.String getURLSpec()
meth public java.lang.String toString()
meth public java.net.URL getURL()
meth public java.util.Vector getChildren()
meth public javax.help.FavoritesItem getParent()
meth public void add(javax.help.FavoritesItem)
meth public void remove(javax.help.FavoritesItem)
meth public void setAsFolder()
meth public void setParent(javax.help.FavoritesItem)
meth public void setVisible(boolean)
supr javax.help.TreeItem
hfds children,class$javax$help$FavoritesItem,emptyInitState,flavors,isFolder,parent,target,title,url,visible

CLSS public javax.help.FavoritesNode
cons public FavoritesNode(javax.help.FavoritesItem)
fld public final static java.lang.String ELEMENT = "favoriteitem"
fld public final static java.lang.String FOOTER = "</favorites>"
fld public final static java.lang.String HEADER = "<?xml version=\u00221.0\u0022 encoding=\u0022UTF-8\u0022?>\n<!DOCTYPE favorites\n PUBLIC \u0022-//Sun Microsystems Inc.//DTD JavaHelp Favorites Version 2.0//EN\u0022\n        \u0022http://java.sun.com/products/javahelp/favorites_2_0.dtd\u0022>\n\n<favorites version=\u00222.0\u0022>\n"
meth public boolean getAllowsChildren()
meth public boolean isVisible()
meth public int getVisibleChildCount()
meth public java.io.OutputStreamWriter exportHeader(java.io.OutputStream) throws java.io.IOException
meth public java.lang.String getOffset()
meth public java.lang.String getXMLElement()
meth public java.lang.String getXMLHeader()
meth public javax.help.FavoritesNode getDeepCopy()
meth public void add(javax.swing.tree.DefaultMutableTreeNode)
meth public void export(java.io.OutputStream) throws java.io.IOException
meth public void exportNode(java.io.OutputStreamWriter) throws java.io.IOException
meth public void remove(javax.swing.tree.DefaultMutableTreeNode)
meth public void setVisible(boolean)
supr javax.swing.tree.DefaultMutableTreeNode
hfds debug,item

CLSS public javax.help.FavoritesView
cons public FavoritesView(javax.help.HelpSet,java.lang.String,java.lang.String,java.util.Hashtable)
cons public FavoritesView(javax.help.HelpSet,java.lang.String,java.lang.String,java.util.Locale,java.util.Hashtable)
fld public final static java.lang.String publicIDString = "-//Sun Microsystems Inc.//DTD JavaHelp Favorites Version 2.0//EN"
innr public static DefaultFavoritesFactory
meth public java.awt.Component createNavigator(javax.help.HelpModel)
meth public java.lang.String getMergeType()
meth public javax.help.FavoritesNode getDataAsTree()
meth public javax.help.FavoritesNode parse(javax.help.HelpSet,java.util.Locale,javax.help.TreeItemFactory)
meth public void saveFavorites(javax.help.FavoritesNode)
supr javax.help.NavigatorView
hfds debug,enabledSave,hs,warningOfFailures
hcls FavoritesParser

CLSS public static javax.help.FavoritesView$DefaultFavoritesFactory
cons public DefaultFavoritesFactory()
intf javax.help.TreeItemFactory
meth public java.util.Enumeration listMessages()
meth public javax.help.TreeItem createItem()
meth public javax.help.TreeItem createItem(java.lang.String,java.util.Hashtable,javax.help.HelpSet,java.util.Locale)
meth public javax.swing.tree.DefaultMutableTreeNode parsingEnded(javax.swing.tree.DefaultMutableTreeNode)
meth public void parsingStarted(java.net.URL)
meth public void processDOCTYPE(java.lang.String,java.lang.String,java.lang.String)
meth public void processPI(javax.help.HelpSet,java.lang.String,java.lang.String)
meth public void reportMessage(java.lang.String,boolean)
supr java.lang.Object
hfds messages,source,validParse

CLSS public javax.help.FlatMap
cons public FlatMap(java.net.URL,javax.help.HelpSet) throws java.io.IOException
fld public final static java.lang.String publicIDString = "-//Sun Microsystems Inc.//DTD JavaHelp Map Version 1.0//EN"
fld public final static java.lang.String publicIDString_V2 = "-//Sun Microsystems Inc.//DTD JavaHelp Map Version 2.0//EN"
innr protected FlatMapResourceBundle
intf java.io.Serializable
intf javax.help.Map
meth public boolean isID(java.net.URL)
meth public boolean isValidID(java.lang.String,javax.help.HelpSet)
meth public java.net.URL getURLFromID(javax.help.Map$ID) throws java.net.MalformedURLException
meth public java.util.Enumeration getAllIDs()
meth public java.util.Enumeration getIDs(java.net.URL)
meth public javax.help.HelpSet getHelpSet()
meth public javax.help.Map$ID getClosestID(java.net.URL)
meth public javax.help.Map$ID getIDFromURL(java.net.URL)
supr java.lang.Object
hfds base,debug,helpset,resource
hcls FlatEnumeration

CLSS protected javax.help.FlatMap$FlatMapResourceBundle
cons public FlatMapResourceBundle(javax.help.FlatMap,java.net.URL)
intf com.sun.java.help.impl.ParserListener
intf java.io.Serializable
meth public final java.lang.Object handleGetObject(java.lang.String)
meth public java.util.Enumeration getKeys()
meth public java.util.Enumeration listMessages()
meth public void commentFound(com.sun.java.help.impl.ParserEvent)
meth public void doctypeFound(com.sun.java.help.impl.ParserEvent)
meth public void errorFound(com.sun.java.help.impl.ParserEvent)
meth public void piFound(com.sun.java.help.impl.ParserEvent)
meth public void reportMessage(java.lang.String,boolean)
meth public void tagFound(com.sun.java.help.impl.ParserEvent)
meth public void textFound(com.sun.java.help.impl.ParserEvent)
supr java.util.ResourceBundle
hfds lookup,messages,source,startedmap,this$0,validParse

CLSS public javax.help.ForwardAction
cons public ForwardAction(java.lang.Object)
intf java.awt.event.MouseListener
intf javax.help.event.HelpHistoryModelListener
meth public void historyChanged(javax.help.event.HelpHistoryModelEvent)
meth public void mouseClicked(java.awt.event.MouseEvent)
meth public void mouseEntered(java.awt.event.MouseEvent)
meth public void mouseExited(java.awt.event.MouseEvent)
meth public void mousePressed(java.awt.event.MouseEvent)
meth public void mouseReleased(java.awt.event.MouseEvent)
supr javax.help.AbstractHelpAction
hfds DELAY,NAME,historyModel,timer
hcls HistoryActionListener,TimeListener

CLSS public javax.help.GlossaryView
cons public GlossaryView(javax.help.HelpSet,java.lang.String,java.lang.String,java.util.Hashtable)
cons public GlossaryView(javax.help.HelpSet,java.lang.String,java.lang.String,java.util.Locale,java.util.Hashtable)
meth public java.awt.Component createNavigator(javax.help.HelpModel)
supr javax.help.IndexView

CLSS public abstract interface javax.help.HelpAction
meth public abstract boolean isEnabled()
meth public abstract java.lang.Object getControl()
meth public abstract java.lang.Object getValue(java.lang.String)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void putValue(java.lang.String,java.lang.Object)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setEnabled(boolean)

CLSS public abstract interface javax.help.HelpBroker
meth public abstract boolean isDisplayed()
meth public abstract boolean isViewDisplayed()
meth public abstract int getScreen()
meth public abstract java.awt.Dimension getSize()
meth public abstract java.awt.Font getFont()
meth public abstract java.awt.Point getLocation()
meth public abstract java.lang.String getCurrentView()
meth public abstract java.net.URL getCurrentURL()
meth public abstract java.util.Locale getLocale()
meth public abstract javax.help.HelpSet getHelpSet()
meth public abstract javax.help.Map$ID getCurrentID()
meth public abstract void enableHelp(java.awt.Component,java.lang.String,javax.help.HelpSet)
meth public abstract void enableHelp(java.awt.MenuItem,java.lang.String,javax.help.HelpSet)
meth public abstract void enableHelpKey(java.awt.Component,java.lang.String,javax.help.HelpSet)
meth public abstract void enableHelpKey(java.awt.Component,java.lang.String,javax.help.HelpSet,java.lang.String,java.lang.String)
meth public abstract void enableHelpOnButton(java.awt.Component,java.lang.String,javax.help.HelpSet)
meth public abstract void enableHelpOnButton(java.awt.MenuItem,java.lang.String,javax.help.HelpSet)
meth public abstract void enableHelpOnButton(java.lang.Object,java.lang.String,javax.help.HelpSet,java.lang.String,java.lang.String)
meth public abstract void initPresentation()
meth public abstract void setCurrentID(java.lang.String)
meth public abstract void setCurrentID(javax.help.Map$ID) throws javax.help.InvalidHelpSetContextException
meth public abstract void setCurrentURL(java.net.URL)
meth public abstract void setCurrentView(java.lang.String)
meth public abstract void setDisplayed(boolean)
meth public abstract void setFont(java.awt.Font)
meth public abstract void setHelpSet(javax.help.HelpSet)
meth public abstract void setHelpSetPresentation(javax.help.HelpSet$Presentation)
meth public abstract void setLocale(java.util.Locale)
meth public abstract void setLocation(java.awt.Point)
meth public abstract void setScreen(int)
meth public abstract void setSize(java.awt.Dimension)
meth public abstract void setViewDisplayed(boolean)
meth public abstract void showID(java.lang.String,java.lang.String,java.lang.String)
meth public abstract void showID(javax.help.Map$ID,java.lang.String,java.lang.String) throws javax.help.InvalidHelpSetContextException

CLSS public abstract interface javax.help.HelpHistoryModel
intf java.io.Serializable
intf javax.help.event.HelpModelListener
meth public abstract int getIndex()
meth public abstract java.util.Vector getBackwardHistory()
meth public abstract java.util.Vector getForwardHistory()
meth public abstract java.util.Vector getHistory()
meth public abstract void addHelpHistoryModelListener(javax.help.event.HelpHistoryModelListener)
meth public abstract void discard()
meth public abstract void goBack()
meth public abstract void goForward()
meth public abstract void removeHelpHistoryModelListener(javax.help.event.HelpHistoryModelListener)
meth public abstract void removeHelpSet(javax.help.HelpSet)
meth public abstract void setHelpModel(javax.help.HelpModel)
meth public abstract void setHistoryEntry(int)

CLSS public abstract interface javax.help.HelpModel
meth public abstract java.net.URL getCurrentURL()
meth public abstract javax.help.HelpSet getHelpSet()
meth public abstract javax.help.Map$ID getCurrentID()
meth public abstract void addHelpModelListener(javax.help.event.HelpModelListener)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removeHelpModelListener(javax.help.event.HelpModelListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setCurrentID(javax.help.Map$ID) throws javax.help.InvalidHelpSetContextException
meth public abstract void setCurrentID(javax.help.Map$ID,java.lang.String,javax.help.JHelpNavigator) throws javax.help.InvalidHelpSetContextException
meth public abstract void setCurrentURL(java.net.URL)
meth public abstract void setCurrentURL(java.net.URL,java.lang.String,javax.help.JHelpNavigator)
meth public abstract void setHelpSet(javax.help.HelpSet)

CLSS public javax.help.HelpSet
cons public HelpSet()
cons public HelpSet(java.lang.ClassLoader)
cons public HelpSet(java.lang.ClassLoader,java.net.URL) throws javax.help.HelpSetException
fld protected javax.help.event.EventListenerList listenerList
fld public final static java.lang.Object implRegistry
fld public final static java.lang.Object kitLoaderRegistry
fld public final static java.lang.Object kitTypeRegistry
fld public final static java.lang.String helpBrokerClass = "helpBroker/class"
fld public final static java.lang.String helpBrokerLoader = "helpBroker/loader"
fld public final static java.lang.String publicIDString = "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 1.0//EN"
fld public final static java.lang.String publicIDString_V2 = "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 2.0//EN"
innr public static DefaultHelpSetFactory
innr public static Presentation
intf java.io.Serializable
meth protected void addPresentation(javax.help.HelpSet$Presentation,boolean)
meth protected void addSubHelpSet(javax.help.HelpSet)
meth protected void addView(javax.help.NavigatorView)
meth protected void fireHelpSetAdded(java.lang.Object,javax.help.HelpSet)
meth protected void fireHelpSetRemoved(java.lang.Object,javax.help.HelpSet)
meth public boolean contains(javax.help.HelpSet)
meth public boolean remove(javax.help.HelpSet)
meth public java.lang.ClassLoader getLoader()
meth public java.lang.Object getKeyData(java.lang.Object,java.lang.String)
meth public java.lang.String getTitle()
meth public java.lang.String toString()
meth public java.net.URL getHelpSetURL()
meth public java.util.Enumeration getHelpSets()
meth public java.util.Locale getLocale()
meth public javax.help.HelpBroker createHelpBroker()
meth public javax.help.HelpBroker createHelpBroker(java.lang.String)
meth public javax.help.HelpSet$Presentation getDefaultPresentation()
meth public javax.help.HelpSet$Presentation getPresentation(java.lang.String)
meth public javax.help.HelpSet$Presentation[] getPresentations()
meth public javax.help.Map getCombinedMap()
meth public javax.help.Map getLocalMap()
meth public javax.help.Map$ID getHomeID()
meth public javax.help.NavigatorView getNavigatorView(java.lang.String)
meth public javax.help.NavigatorView[] getNavigatorViews()
meth public static java.net.URL findHelpSet(java.lang.ClassLoader,java.lang.String)
meth public static java.net.URL findHelpSet(java.lang.ClassLoader,java.lang.String,java.lang.String,java.util.Locale)
meth public static java.net.URL findHelpSet(java.lang.ClassLoader,java.lang.String,java.util.Locale)
meth public static javax.help.HelpSet parse(java.net.URL,java.lang.ClassLoader,javax.help.HelpSetFactory)
meth public void add(javax.help.HelpSet)
meth public void addHelpSetListener(javax.help.event.HelpSetListener)
meth public void parseInto(java.net.URL,javax.help.HelpSetFactory)
meth public void removeHelpSetListener(javax.help.event.HelpSetListener)
meth public void setHomeID(java.lang.String)
meth public void setKeyData(java.lang.Object,java.lang.String,java.lang.Object)
meth public void setLocalMap(javax.help.Map)
meth public void setTitle(java.lang.String)
supr java.lang.Object
hfds changes,class$java$lang$Object,class$javax$help$HelpSet,class$javax$help$event$HelpSetListener,combinedMap,debug,defaultHelpBroker,defaultKeys,defaultPresentation,errorMsg,helpset,helpsets,homeID,loader,localKeys,locale,map,presentations,subHelpSets,title,views
hcls HelpSetParser

CLSS public static javax.help.HelpSet$DefaultHelpSetFactory
cons public DefaultHelpSetFactory()
innr public static HelpAction
meth public java.util.Enumeration listMessages()
meth public javax.help.HelpSet parsingEnded(javax.help.HelpSet)
meth public void parsingStarted(java.net.URL)
meth public void processDOCTYPE(java.lang.String,java.lang.String,java.lang.String)
meth public void processHomeID(javax.help.HelpSet,java.lang.String)
meth public void processMapRef(javax.help.HelpSet,java.util.Hashtable)
meth public void processPI(javax.help.HelpSet,java.lang.String,java.lang.String)
meth public void processPresentation(javax.help.HelpSet,java.lang.String,boolean,boolean,boolean,java.awt.Dimension,java.awt.Point,java.lang.String,java.lang.String,boolean,java.util.Vector)
meth public void processSubHelpSet(javax.help.HelpSet,java.util.Hashtable)
meth public void processTitle(javax.help.HelpSet,java.lang.String)
meth public void processView(javax.help.HelpSet,java.lang.String,java.lang.String,java.lang.String,java.util.Hashtable,java.lang.String,java.util.Hashtable,java.util.Locale)
meth public void reportMessage(java.lang.String,boolean)
supr java.lang.Object
hfds messages,source,validParse

CLSS public static javax.help.HelpSet$Presentation
cons public Presentation(java.lang.String,boolean,boolean,java.awt.Dimension,java.awt.Point,java.lang.String,javax.help.Map$ID,boolean,java.util.Vector)
meth public boolean isToolbar()
meth public boolean isViewDisplayed()
meth public boolean isViewImagesDisplayed()
meth public java.awt.Dimension getSize()
meth public java.awt.Point getLocation()
meth public java.lang.String getName()
meth public java.lang.String getTitle()
meth public java.util.Enumeration getHelpActions(javax.help.HelpSet,java.lang.Object)
meth public javax.help.Map$ID getImageID()
supr java.lang.Object
hfds displayViewImages,displayViews,helpActions,imageID,location,name,size,title,toolbar

CLSS public javax.help.HelpSetException
cons public HelpSetException(java.lang.String)
supr java.lang.Exception

CLSS public javax.help.HelpUtilities
cons public HelpUtilities()
meth public static boolean isStringInString(java.text.RuleBasedCollator,java.lang.String,java.lang.String)
meth public static java.lang.String getDefaultQueryEngine()
meth public static java.lang.String getHelpSetNameFromBean(java.lang.Class)
meth public static java.lang.String getIDStringFromBean(java.lang.Class)
meth public static java.lang.String getString(java.lang.String)
meth public static java.lang.String getString(java.util.Locale,java.lang.String)
meth public static java.lang.String getText(java.lang.String)
meth public static java.lang.String getText(java.lang.String,java.lang.String)
meth public static java.lang.String getText(java.lang.String,java.lang.String,java.lang.String)
meth public static java.lang.String getText(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public static java.lang.String getText(java.util.Locale,java.lang.String)
meth public static java.lang.String getText(java.util.Locale,java.lang.String,java.lang.String)
meth public static java.lang.String getText(java.util.Locale,java.lang.String,java.lang.String,java.lang.String)
meth public static java.lang.String getText(java.util.Locale,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public static java.lang.String[] getStringArray(java.util.Locale,java.lang.String)
meth public static java.net.URL getLocalizedResource(java.lang.ClassLoader,java.lang.String,java.lang.String,java.util.Locale)
meth public static java.net.URL getLocalizedResource(java.lang.ClassLoader,java.lang.String,java.lang.String,java.util.Locale,boolean)
meth public static java.util.Enumeration getCandidates(java.util.Locale)
meth public static java.util.Locale getLocale(java.awt.Component)
meth public static java.util.Locale localeFromLang(java.lang.String)
supr java.lang.Object
hfds bundles,debug,lastBundle,lastLocale,tailsPerLocales
hcls LocalePair

CLSS public javax.help.HomeAction
cons public HomeAction(java.lang.Object)
intf java.awt.event.ActionListener
meth public void actionPerformed(java.awt.event.ActionEvent)
supr javax.help.AbstractHelpAction
hfds NAME

CLSS public javax.help.IndexItem
cons public IndexItem()
cons public IndexItem(javax.help.Map$ID,java.util.Locale)
cons public IndexItem(javax.help.Map$ID,javax.help.HelpSet,java.util.Locale)
supr javax.help.TreeItem

CLSS public javax.help.IndexView
cons public IndexView(javax.help.HelpSet,java.lang.String,java.lang.String,java.util.Hashtable)
cons public IndexView(javax.help.HelpSet,java.lang.String,java.lang.String,java.util.Locale,java.util.Hashtable)
fld public final static java.lang.String publicIDString = "-//Sun Microsystems Inc.//DTD JavaHelp Index Version 1.0//EN"
fld public final static java.lang.String publicIDString_V2 = "-//Sun Microsystems Inc.//DTD JavaHelp Index Version 2.0//EN"
innr public static DefaultIndexFactory
meth public java.awt.Component createNavigator(javax.help.HelpModel)
meth public java.lang.String getMergeType()
meth public javax.swing.tree.DefaultMutableTreeNode getDataAsTree()
meth public static javax.swing.tree.DefaultMutableTreeNode parse(java.net.URL,javax.help.HelpSet,java.util.Locale,javax.help.TreeItemFactory)
supr javax.help.NavigatorView
hfds debug,warningOfFailures
hcls IndexParser

CLSS public static javax.help.IndexView$DefaultIndexFactory
cons public DefaultIndexFactory()
intf javax.help.TreeItemFactory
meth public java.util.Enumeration listMessages()
meth public javax.help.TreeItem createItem()
meth public javax.help.TreeItem createItem(java.lang.String,java.util.Hashtable,javax.help.HelpSet,java.util.Locale)
meth public javax.swing.tree.DefaultMutableTreeNode parsingEnded(javax.swing.tree.DefaultMutableTreeNode)
meth public void parsingStarted(java.net.URL)
meth public void processDOCTYPE(java.lang.String,java.lang.String,java.lang.String)
meth public void processPI(javax.help.HelpSet,java.lang.String,java.lang.String)
meth public void reportMessage(java.lang.String,boolean)
supr java.lang.Object
hfds messages,source,validParse

CLSS public javax.help.InvalidHelpSetContextException
cons public InvalidHelpSetContextException(java.lang.String,javax.help.HelpSet,javax.help.HelpSet)
meth public javax.help.HelpSet getContext()
meth public javax.help.HelpSet getHelpSet()
supr java.lang.Exception
hfds context,hs

CLSS public javax.help.InvalidNavigatorViewException
cons public InvalidNavigatorViewException(java.lang.String,javax.help.HelpSet,java.lang.String,java.lang.String,java.util.Locale,java.lang.String,java.util.Hashtable)
meth public java.lang.String getClassName()
meth public java.lang.String getLabel()
meth public java.lang.String getName()
meth public java.util.Hashtable getParams()
meth public java.util.Locale getLocale()
meth public javax.help.HelpSet getHelpSet()
supr java.lang.Exception
hfds className,hs,label,locale,name,params

CLSS public javax.help.JHelp
cons public JHelp()
cons public JHelp(javax.help.HelpSet)
cons public JHelp(javax.help.TextHelpModel)
cons public JHelp(javax.help.TextHelpModel,javax.help.HelpHistoryModel,javax.help.HelpSet$Presentation)
fld protected boolean navDisplayed
fld protected boolean toolbarDisplayed
fld protected java.util.Vector navigators
fld protected javax.help.HelpHistoryModel historyModel
fld protected javax.help.HelpSet$Presentation hsPres
fld protected javax.help.JHelpContentViewer contentViewer
fld protected javax.help.TextHelpModel helpModel
innr protected AccessibleJHelp
intf javax.accessibility.Accessible
intf javax.help.event.HelpSetListener
meth protected void setupNavigators()
meth public boolean isNavigatorDisplayed()
meth public boolean isToolbarDisplayed()
meth public java.lang.String getUIClassID()
meth public java.net.URL getHelpSetURL()
meth public java.util.Enumeration getHelpNavigators()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.help.HelpHistoryModel getHistoryModel()
meth public javax.help.HelpSet$Presentation getHelpSetPresentation()
meth public javax.help.JHelpContentViewer getContentViewer()
meth public javax.help.JHelpNavigator getCurrentNavigator()
meth public javax.help.TextHelpModel getModel()
meth public javax.help.TreeItem[] getSelectedItems()
meth public javax.help.plaf.HelpUI getUI()
meth public void addHelpNavigator(javax.help.JHelpNavigator)
meth public void helpSetAdded(javax.help.event.HelpSetEvent)
meth public void helpSetRemoved(javax.help.event.HelpSetEvent)
meth public void removeHelpNavigator(javax.help.JHelpNavigator)
meth public void setCurrentID(java.lang.String)
meth public void setCurrentID(javax.help.Map$ID) throws javax.help.InvalidHelpSetContextException
meth public void setCurrentID(javax.help.Map$ID,java.lang.String,javax.help.JHelpNavigator) throws javax.help.InvalidHelpSetContextException
meth public void setCurrentNavigator(javax.help.JHelpNavigator)
meth public void setCurrentURL(java.net.URL)
meth public void setCurrentURL(java.net.URL,java.lang.String,javax.help.JHelpNavigator)
meth public void setHelpSetPresentation(javax.help.HelpSet$Presentation)
meth public void setHelpSetSpec(java.lang.String)
meth public void setModel(javax.help.TextHelpModel)
meth public void setNavigatorDisplayed(boolean)
meth public void setToolbarDisplayed(boolean)
meth public void setUI(javax.help.plaf.HelpUI)
meth public void updateUI()
supr javax.swing.JComponent
hfds debug

CLSS protected javax.help.JHelp$AccessibleJHelp
cons protected AccessibleJHelp(javax.help.JHelp)
meth public javax.accessibility.AccessibleRole getAccessibleRole()
supr javax.swing.JComponent$AccessibleJComponent
hfds this$0

CLSS public javax.help.JHelpContentViewer
cons public JHelpContentViewer()
cons public JHelpContentViewer(javax.help.HelpSet)
cons public JHelpContentViewer(javax.help.TextHelpModel)
fld protected javax.help.TextHelpModel model
innr protected AccessibleJHelpContentViewer
intf javax.accessibility.Accessible
meth public boolean getSynch()
meth public java.lang.String getDocumentTitle()
meth public java.lang.String getUIClassID()
meth public java.net.URL getCurrentURL()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.help.TextHelpModel getModel()
meth public javax.help.plaf.HelpContentViewerUI getUI()
meth public javax.swing.text.EditorKit createEditorKitForContentType(java.lang.String)
meth public void addHelpModelListener(javax.help.event.HelpModelListener)
meth public void addHighlight(int,int)
meth public void addTextHelpModelListener(javax.help.event.TextHelpModelListener)
meth public void clear()
meth public void reload()
meth public void removeAllHighlights()
meth public void removeHelpModelListener(javax.help.event.HelpModelListener)
meth public void removeHelpModelListener(javax.help.event.TextHelpModelListener)
meth public void setCurrentID(java.lang.String)
meth public void setCurrentID(javax.help.Map$ID) throws javax.help.InvalidHelpSetContextException
meth public void setCurrentURL(java.net.URL)
meth public void setModel(javax.help.TextHelpModel)
meth public void setSynch(boolean)
meth public void setUI(javax.help.plaf.HelpContentViewerUI)
meth public void updateUI()
supr javax.swing.JComponent
hfds debug,kitRegistry,synch

CLSS protected javax.help.JHelpContentViewer$AccessibleJHelpContentViewer
cons protected AccessibleJHelpContentViewer(javax.help.JHelpContentViewer)
meth public javax.accessibility.AccessibleRole getAccessibleRole()
supr javax.swing.JComponent$AccessibleJComponent
hfds this$0

CLSS public javax.help.JHelpFavoritesNavigator
cons public JHelpFavoritesNavigator(javax.help.HelpSet,java.lang.String,java.lang.String,java.net.URL) throws javax.help.InvalidNavigatorViewException
cons public JHelpFavoritesNavigator(javax.help.NavigatorView)
cons public JHelpFavoritesNavigator(javax.help.NavigatorView,javax.help.HelpModel)
meth public boolean canMerge(javax.help.NavigatorView)
meth public java.lang.String getUIClassID()
meth public javax.swing.Action getAddAction()
meth public void collapseID(java.lang.String)
meth public void expandID(java.lang.String)
supr javax.help.JHelpNavigator
hfds debug

CLSS public javax.help.JHelpGlossaryNavigator
cons public JHelpGlossaryNavigator(javax.help.HelpSet,java.lang.String,java.lang.String) throws javax.help.InvalidNavigatorViewException
cons public JHelpGlossaryNavigator(javax.help.NavigatorView)
cons public JHelpGlossaryNavigator(javax.help.NavigatorView,javax.help.HelpModel)
meth public java.lang.String getUIClassID()
supr javax.help.JHelpNavigator
hfds debug

CLSS public javax.help.JHelpIndexNavigator
cons public JHelpIndexNavigator(javax.help.HelpSet,java.lang.String,java.lang.String,java.net.URL) throws javax.help.InvalidNavigatorViewException
cons public JHelpIndexNavigator(javax.help.NavigatorView)
cons public JHelpIndexNavigator(javax.help.NavigatorView,javax.help.HelpModel)
meth public boolean canMerge(javax.help.NavigatorView)
meth public java.lang.String getUIClassID()
meth public void collapseID(java.lang.String)
meth public void expandID(java.lang.String)
meth public void merge(javax.help.NavigatorView)
meth public void remove(javax.help.NavigatorView)
supr javax.help.JHelpNavigator
hfds debug

CLSS public javax.help.JHelpNavigator
cons public JHelpNavigator(javax.help.NavigatorView)
cons public JHelpNavigator(javax.help.NavigatorView,javax.help.HelpModel)
fld protected java.lang.String type
fld protected javax.help.HelpModel helpModel
innr protected AccessibleJHelpNavigator
intf javax.accessibility.Accessible
meth protected static java.util.Hashtable createParams(java.net.URL)
meth public boolean canMerge(javax.help.NavigatorView)
meth public java.lang.String getNavigatorLabel()
meth public java.lang.String getNavigatorLabel(java.util.Locale)
meth public java.lang.String getNavigatorName()
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.help.HelpModel getModel()
meth public javax.help.NavigatorView getNavigatorView()
meth public javax.help.TreeItem[] getSelectedItems()
meth public javax.help.plaf.HelpNavigatorUI getUI()
meth public javax.swing.Icon getIcon()
meth public void addHelpModelListener(javax.help.event.HelpModelListener)
meth public void merge(javax.help.NavigatorView)
meth public void remove(javax.help.NavigatorView)
meth public void removeHelpModelListener(javax.help.event.HelpModelListener)
meth public void setModel(javax.help.HelpModel)
meth public void setSelectedItems(javax.help.TreeItem[])
meth public void setUI(javax.help.plaf.HelpNavigatorUI)
meth public void updateUI()
supr javax.swing.JComponent
hfds debug,jhPackageName,selectedItems,view

CLSS protected javax.help.JHelpNavigator$AccessibleJHelpNavigator
cons protected AccessibleJHelpNavigator(javax.help.JHelpNavigator)
meth public javax.accessibility.AccessibleRole getAccessibleRole()
supr javax.swing.JComponent$AccessibleJComponent
hfds this$0

CLSS public javax.help.JHelpSearchNavigator
cons public JHelpSearchNavigator(javax.help.HelpSet,java.lang.String,java.lang.String,java.net.URL) throws javax.help.InvalidNavigatorViewException
cons public JHelpSearchNavigator(javax.help.NavigatorView)
cons public JHelpSearchNavigator(javax.help.NavigatorView,javax.help.HelpModel)
meth protected java.lang.String getDefaultQueryEngine()
meth public boolean canMerge(javax.help.NavigatorView)
meth public java.lang.String getUIClassID()
meth public javax.help.search.SearchEngine getSearchEngine()
meth public void merge(javax.help.NavigatorView)
meth public void remove(javax.help.NavigatorView)
meth public void setSearchEngine(javax.help.search.SearchEngine)
supr javax.help.JHelpNavigator
hfds debug,search

CLSS public javax.help.JHelpTOCNavigator
cons public JHelpTOCNavigator(javax.help.HelpSet,java.lang.String,java.lang.String,java.net.URL) throws javax.help.InvalidNavigatorViewException
cons public JHelpTOCNavigator(javax.help.NavigatorView)
cons public JHelpTOCNavigator(javax.help.NavigatorView,javax.help.HelpModel)
meth public boolean canMerge(javax.help.NavigatorView)
meth public java.lang.String getUIClassID()
meth public void collapseID(java.lang.String)
meth public void expandID(java.lang.String)
meth public void merge(javax.help.NavigatorView)
meth public void remove(javax.help.NavigatorView)
supr javax.help.JHelpNavigator
hfds debug

CLSS public javax.help.MainWindow
meth public static javax.help.Presentation getPresentation(javax.help.HelpSet,java.lang.String)
supr javax.help.WindowPresentation
hfds debug

CLSS public abstract interface javax.help.Map
innr public final static ID
meth public abstract boolean isID(java.net.URL)
meth public abstract boolean isValidID(java.lang.String,javax.help.HelpSet)
meth public abstract java.net.URL getURLFromID(javax.help.Map$ID) throws java.net.MalformedURLException
meth public abstract java.util.Enumeration getAllIDs()
meth public abstract java.util.Enumeration getIDs(java.net.URL)
meth public abstract javax.help.Map$ID getClosestID(java.net.URL)
meth public abstract javax.help.Map$ID getIDFromURL(java.net.URL)

CLSS public final static javax.help.Map$ID
fld public java.lang.String id
fld public javax.help.HelpSet hs
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public java.lang.String getIDString()
meth public java.lang.String toString()
meth public java.net.URL getURL() throws java.net.MalformedURLException
meth public javax.help.HelpSet getHelpSet()
meth public static javax.help.Map$ID create(java.lang.String,javax.help.HelpSet)
supr java.lang.Object

CLSS public abstract javax.help.Merge
cons protected Merge(javax.help.NavigatorView,javax.help.NavigatorView)
fld protected java.util.Locale locale
fld protected javax.swing.tree.DefaultMutableTreeNode slaveTopNode
innr public static DefaultMergeFactory
meth public abstract javax.swing.tree.TreeNode processMerge(javax.swing.tree.TreeNode)
meth public static void mergeNodeChildren(javax.swing.tree.TreeNode)
meth public static void mergeNodes(javax.swing.tree.TreeNode,javax.swing.tree.TreeNode)
supr java.lang.Object
hfds class$javax$help$NavigatorView

CLSS public static javax.help.Merge$DefaultMergeFactory
cons public DefaultMergeFactory()
meth public static javax.help.Merge getMerge(javax.help.NavigatorView,javax.help.NavigatorView)
supr java.lang.Object

CLSS public javax.help.MergeHelpUtilities
cons public MergeHelpUtilities()
meth public static boolean haveEqualID(javax.swing.tree.DefaultMutableTreeNode,javax.swing.tree.DefaultMutableTreeNode)
meth public static int compareNames(javax.swing.tree.DefaultMutableTreeNode,javax.swing.tree.DefaultMutableTreeNode)
meth public static java.lang.String getNodeName(javax.swing.tree.DefaultMutableTreeNode)
meth public static java.util.Locale getLocale(javax.swing.tree.DefaultMutableTreeNode)
meth public static javax.swing.tree.DefaultMutableTreeNode getChildWithName(javax.swing.tree.DefaultMutableTreeNode,java.lang.String)
meth public static void markNodes(javax.swing.tree.DefaultMutableTreeNode,javax.swing.tree.DefaultMutableTreeNode)
meth public static void mergeNodeChildren(java.lang.String,javax.swing.tree.DefaultMutableTreeNode)
meth public static void mergeNodes(java.lang.String,javax.swing.tree.DefaultMutableTreeNode,javax.swing.tree.DefaultMutableTreeNode)
supr java.lang.Object
hfds class$javax$swing$tree$TreeNode,debug

CLSS public abstract javax.help.NavigatorView
cons protected NavigatorView(javax.help.HelpSet,java.lang.String,java.lang.String,java.util.Locale,java.util.Hashtable)
intf java.io.Serializable
meth public abstract java.awt.Component createNavigator(javax.help.HelpModel)
meth public java.lang.String getLabel()
meth public java.lang.String getLabel(java.util.Locale)
meth public java.lang.String getMergeType()
meth public java.lang.String getName()
meth public java.util.Hashtable getParameters()
meth public java.util.Locale getLocale()
meth public javax.help.HelpSet getHelpSet()
meth public javax.help.Map$ID getImageID()
meth public static javax.help.NavigatorView create(javax.help.HelpSet,java.lang.String,java.lang.String,java.util.Locale,java.lang.String,java.util.Hashtable) throws javax.help.InvalidNavigatorViewException
supr java.lang.Object
hfds class$java$lang$String,class$java$util$Hashtable,class$java$util$Locale,class$javax$help$HelpSet,hs,imageID,label,locale,mergeType,name,params

CLSS public javax.help.NoMerge
cons public NoMerge(javax.help.NavigatorView,javax.help.NavigatorView)
meth public javax.swing.tree.TreeNode processMerge(javax.swing.tree.TreeNode)
meth public static void mergeNodeChildren(javax.swing.tree.TreeNode)
meth public static void mergeNodes(javax.swing.tree.TreeNode,javax.swing.tree.TreeNode)
supr javax.help.Merge
hfds debug

CLSS public javax.help.Popup
intf java.awt.event.ActionListener
meth public boolean isDisplayed()
meth public java.awt.Component getInvoker()
meth public java.awt.Rectangle getInvokerInternalBounds()
meth public static javax.help.Presentation getPresentation(javax.help.HelpSet,java.lang.String)
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void setDisplayed(boolean)
meth public void setInvoker(java.awt.Component)
meth public void setInvokerInternalBounds(java.awt.Rectangle)
supr javax.help.Presentation
hfds CANCEL,SHOW,class$javax$swing$JWindow,currentPopup,debug,grabbed,internalBounds,invoker,jheditor,on1dot4,pca,pka,pma,pmwl,pwa,window
hcls 1,PopupComponentAdapter,PopupKeyAdapter,PopupMouseAdapter,PopupMouseWheelListener,PopupWindowAdapter

CLSS public abstract javax.help.Presentation
cons public Presentation()
meth protected javax.help.TextHelpModel getHelpModel()
meth public abstract boolean isDisplayed()
meth public abstract void setDisplayed(boolean)
meth public java.awt.Dimension getSize()
meth public java.awt.Font getFont()
meth public java.net.URL getCurrentURL()
meth public java.util.Locale getLocale()
meth public javax.help.HelpSet getHelpSet()
meth public javax.help.Map$ID getCurrentID()
meth public static javax.help.Presentation getPresentation(javax.help.HelpSet,java.lang.String)
meth public void setCurrentID(java.lang.String)
meth public void setCurrentID(javax.help.Map$ID) throws javax.help.InvalidHelpSetContextException
meth public void setCurrentURL(java.net.URL)
meth public void setFont(java.awt.Font)
meth public void setHelpSet(javax.help.HelpSet)
meth public void setHelpSetPresentation(javax.help.HelpSet$Presentation)
meth public void setLocale(java.util.Locale)
meth public void setSize(java.awt.Dimension)
supr java.lang.Object
hfds debug,font,height,helpset,locale,model,width

CLSS public javax.help.PrintAction
cons public PrintAction(java.lang.Object)
intf java.awt.event.ActionListener
intf java.beans.PropertyChangeListener
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void propertyChange(java.beans.PropertyChangeEvent)
supr javax.help.AbstractHelpAction
hfds NAME,handler

CLSS public javax.help.PrintSetupAction
cons public PrintSetupAction(java.lang.Object)
intf java.awt.event.ActionListener
intf java.beans.PropertyChangeListener
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void propertyChange(java.beans.PropertyChangeEvent)
supr javax.help.AbstractHelpAction
hfds NAME,handler

CLSS public javax.help.ReloadAction
cons public ReloadAction(java.lang.Object)
intf java.awt.event.ActionListener
meth public void actionPerformed(java.awt.event.ActionEvent)
supr javax.help.AbstractHelpAction
hfds NAME

CLSS public javax.help.SearchHit
cons public SearchHit(double,int,int)
meth public double getConfidence()
meth public int getBegin()
meth public int getEnd()
supr java.lang.Object
hfds begin,confidence,end

CLSS public javax.help.SearchTOCItem
cons public SearchTOCItem(javax.help.Map$ID,javax.help.Map$ID,javax.help.HelpSet,java.util.Locale)
cons public SearchTOCItem(javax.help.search.SearchItem)
meth public boolean inTOC()
meth public double getConfidence()
meth public int hitCount()
meth public java.net.URL getURL()
meth public java.util.Enumeration getConfidences()
meth public java.util.Enumeration getSearchHits()
meth public void addSearchHit(javax.help.SearchHit)
supr javax.help.TOCItem
hfds confidence,inTOC,sivec,url

CLSS public javax.help.SearchView
cons public SearchView(javax.help.HelpSet,java.lang.String,java.lang.String,java.util.Hashtable)
cons public SearchView(javax.help.HelpSet,java.lang.String,java.lang.String,java.util.Locale,java.util.Hashtable)
meth public java.awt.Component createNavigator(javax.help.HelpModel)
supr javax.help.NavigatorView

CLSS public javax.help.SecondaryWindow
meth public static javax.help.Presentation getPresentation(javax.help.HelpSet,java.lang.String)
meth public static javax.help.SecondaryWindow getPresentation(java.lang.String)
meth public void destroy()
supr javax.help.WindowPresentation
hfds debug,name,windows

CLSS public javax.help.SeparatorAction
cons public SeparatorAction(java.lang.Object)
supr javax.help.AbstractHelpAction

CLSS public javax.help.ServletHelpBroker
cons public ServletHelpBroker()
fld protected boolean viewDisplayed
fld protected java.awt.Font font
fld protected java.util.Locale locale
fld protected javax.help.DefaultHelpModel model
fld protected javax.help.HelpSet helpset
fld protected javax.help.NavigatorView curNav
intf javax.help.HelpBroker
meth public boolean isDisplayed()
meth public boolean isViewDisplayed()
meth public int getScreen()
meth public java.awt.Dimension getSize()
meth public java.awt.Font getFont()
meth public java.awt.Point getLocation()
meth public java.lang.String getCurrentView()
meth public java.net.URL getCurrentURL()
meth public java.util.Locale getLocale()
meth public javax.help.HelpSet getHelpSet()
meth public javax.help.Map$ID getCurrentID()
meth public javax.help.NavigatorView getCurrentNavigatorView()
meth public void enableHelp(java.awt.Component,java.lang.String,javax.help.HelpSet)
meth public void enableHelp(java.awt.MenuItem,java.lang.String,javax.help.HelpSet)
meth public void enableHelpKey(java.awt.Component,java.lang.String,javax.help.HelpSet)
meth public void enableHelpKey(java.awt.Component,java.lang.String,javax.help.HelpSet,java.lang.String,java.lang.String)
meth public void enableHelpOnButton(java.awt.Component,java.lang.String,javax.help.HelpSet)
meth public void enableHelpOnButton(java.awt.MenuItem,java.lang.String,javax.help.HelpSet)
meth public void enableHelpOnButton(java.lang.Object,java.lang.String,javax.help.HelpSet,java.lang.String,java.lang.String)
meth public void initPresentation()
meth public void setCurrentID(java.lang.String)
meth public void setCurrentID(javax.help.Map$ID) throws javax.help.InvalidHelpSetContextException
meth public void setCurrentURL(java.net.URL)
meth public void setCurrentView(java.lang.String)
meth public void setDisplayed(boolean)
meth public void setFont(java.awt.Font)
meth public void setHelpSet(javax.help.HelpSet)
meth public void setHelpSetPresentation(javax.help.HelpSet$Presentation)
meth public void setLocale(java.util.Locale)
meth public void setLocation(java.awt.Point)
meth public void setScreen(int)
meth public void setSize(java.awt.Dimension)
meth public void setViewDisplayed(boolean)
meth public void showID(java.lang.String,java.lang.String,java.lang.String)
meth public void showID(javax.help.Map$ID,java.lang.String,java.lang.String) throws javax.help.InvalidHelpSetContextException
supr java.lang.Object
hfds debug

CLSS public javax.help.SortMerge
cons public SortMerge(javax.help.NavigatorView,javax.help.NavigatorView)
meth public javax.swing.tree.TreeNode processMerge(javax.swing.tree.TreeNode)
meth public static void mergeNodeChildren(javax.swing.tree.TreeNode)
meth public static void mergeNodes(javax.swing.tree.TreeNode,javax.swing.tree.TreeNode)
meth public static void sortNode(javax.swing.tree.DefaultMutableTreeNode,java.util.Locale)
supr javax.help.Merge
hfds debug

CLSS public javax.help.SwingHelpUtilities
cons public SwingHelpUtilities()
intf java.beans.PropertyChangeListener
meth public static javax.swing.ImageIcon getImageIcon(java.lang.Class,java.lang.String)
meth public static void setContentViewerUI(java.lang.String)
meth public void propertyChange(java.beans.PropertyChangeEvent)
supr java.lang.Object
hfds basicDnDCursor,basicOnItemCursor,class$java$beans$PropertyChangeListener,class$javax$help$HelpUtilities,class$javax$help$plaf$basic$BasicHelpUI,class$javax$help$plaf$gtk$GTKCursorFactory,contentViewerUI,debug,gtkDnDCursor,gtkOnItemCursor,myLAFListener,uiDefaults

CLSS public javax.help.TOCItem
cons public TOCItem()
cons public TOCItem(javax.help.Map$ID,javax.help.Map$ID,java.util.Locale)
cons public TOCItem(javax.help.Map$ID,javax.help.Map$ID,javax.help.HelpSet,java.util.Locale)
meth public javax.help.Map$ID getImageID()
supr javax.help.TreeItem
hfds imageID

CLSS public javax.help.TOCView
cons public TOCView(javax.help.HelpSet,java.lang.String,java.lang.String,java.util.Hashtable)
cons public TOCView(javax.help.HelpSet,java.lang.String,java.lang.String,java.util.Locale,java.util.Hashtable)
fld public final static java.lang.String publicIDString = "-//Sun Microsystems Inc.//DTD JavaHelp TOC Version 1.0//EN"
fld public final static java.lang.String publicIDString_V2 = "-//Sun Microsystems Inc.//DTD JavaHelp TOC Version 2.0//EN"
innr public static DefaultTOCFactory
meth public java.awt.Component createNavigator(javax.help.HelpModel)
meth public java.lang.String getMergeType()
meth public javax.help.Map$ID getCategoryClosedImageID()
meth public javax.help.Map$ID getCategoryOpenImageID()
meth public javax.help.Map$ID getTopicImageID()
meth public javax.swing.tree.DefaultMutableTreeNode getDataAsTree()
meth public static javax.swing.tree.DefaultMutableTreeNode parse(java.net.URL,javax.help.HelpSet,java.util.Locale,javax.help.TreeItemFactory)
meth public static javax.swing.tree.DefaultMutableTreeNode parse(java.net.URL,javax.help.HelpSet,java.util.Locale,javax.help.TreeItemFactory,javax.help.TOCView)
meth public void setCategoryClosedImageID(java.lang.String)
meth public void setCategoryOpenImageID(java.lang.String)
meth public void setTopicImageID(java.lang.String)
supr javax.help.NavigatorView
hfds categoryClosedImageID,categoryOpenImageID,debug,topicImageID,warningOfFailures
hcls TOCParser

CLSS public static javax.help.TOCView$DefaultTOCFactory
cons public DefaultTOCFactory()
intf javax.help.TreeItemFactory
meth public java.util.Enumeration listMessages()
meth public javax.help.TreeItem createItem()
meth public javax.help.TreeItem createItem(java.lang.String,java.util.Hashtable,javax.help.HelpSet,java.util.Locale)
meth public javax.swing.tree.DefaultMutableTreeNode parsingEnded(javax.swing.tree.DefaultMutableTreeNode)
meth public void parsingStarted(java.net.URL)
meth public void processDOCTYPE(java.lang.String,java.lang.String,java.lang.String)
meth public void processPI(javax.help.HelpSet,java.lang.String,java.lang.String)
meth public void reportMessage(java.lang.String,boolean)
supr java.lang.Object
hfds messages,source,validParse

CLSS public abstract interface javax.help.TextHelpModel
innr public abstract interface static Highlight
intf javax.help.HelpModel
meth public abstract java.lang.String getDocumentTitle()
meth public abstract javax.help.TextHelpModel$Highlight[] getHighlights()
meth public abstract void addHighlight(int,int)
meth public abstract void addTextHelpModelListener(javax.help.event.TextHelpModelListener)
meth public abstract void removeAllHighlights()
meth public abstract void removeTextHelpModelListener(javax.help.event.TextHelpModelListener)
meth public abstract void setDocumentTitle(java.lang.String)
meth public abstract void setHighlights(javax.help.TextHelpModel$Highlight[])

CLSS public abstract interface static javax.help.TextHelpModel$Highlight
meth public abstract int getEndOffset()
meth public abstract int getStartOffset()

CLSS public javax.help.TreeItem
cons public TreeItem()
cons public TreeItem(java.lang.String)
cons public TreeItem(javax.help.Map$ID,java.util.Locale)
cons public TreeItem(javax.help.Map$ID,javax.help.HelpSet,java.util.Locale)
fld protected java.util.Locale locale
fld public final static int COLLAPSE = 0
fld public final static int DEFAULT_EXPANSION = -1
fld public final static int EXPAND = 1
intf java.io.Serializable
meth public int getExpansionType()
meth public java.lang.String getMergeType()
meth public java.lang.String getName()
meth public java.lang.String getPresentation()
meth public java.lang.String getPresentationName()
meth public java.lang.String toString()
meth public java.net.URL getURL()
meth public java.util.Locale getLocale()
meth public javax.help.HelpSet getHelpSet()
meth public javax.help.Map$ID getID()
meth public void setExpansionType(int)
meth public void setHelpSet(javax.help.HelpSet)
meth public void setID(javax.help.Map$ID)
meth public void setMergeType(java.lang.String)
meth public void setName(java.lang.String)
meth public void setPresentation(java.lang.String)
meth public void setPresentationName(java.lang.String)
supr java.lang.Object
hfds expand,hs,id,mergeType,name,presentation,presentationName

CLSS public abstract interface javax.help.TreeItemFactory
meth public abstract java.util.Enumeration listMessages()
meth public abstract javax.help.TreeItem createItem()
meth public abstract javax.help.TreeItem createItem(java.lang.String,java.util.Hashtable,javax.help.HelpSet,java.util.Locale)
meth public abstract javax.swing.tree.DefaultMutableTreeNode parsingEnded(javax.swing.tree.DefaultMutableTreeNode)
meth public abstract void parsingStarted(java.net.URL)
meth public abstract void processDOCTYPE(java.lang.String,java.lang.String,java.lang.String)
meth public abstract void processPI(javax.help.HelpSet,java.lang.String,java.lang.String)
meth public abstract void reportMessage(java.lang.String,boolean)

CLSS public javax.help.TryMap
cons public TryMap()
intf java.io.Serializable
intf javax.help.Map
meth public boolean isID(java.net.URL)
meth public boolean isValidID(java.lang.String,javax.help.HelpSet)
meth public boolean remove(javax.help.Map)
meth public java.net.URL getURLFromID(javax.help.Map$ID) throws java.net.MalformedURLException
meth public java.util.Enumeration getAllIDs()
meth public java.util.Enumeration getIDs(java.net.URL)
meth public java.util.Enumeration getMaps()
meth public javax.help.Map$ID getClosestID(java.net.URL)
meth public javax.help.Map$ID getIDFromURL(java.net.URL)
meth public void add(javax.help.Map)
supr java.lang.Object
hfds debug,maps
hcls TryEnumeration

CLSS public javax.help.UniteAppendMerge
cons public UniteAppendMerge(javax.help.NavigatorView,javax.help.NavigatorView)
meth public javax.swing.tree.TreeNode processMerge(javax.swing.tree.TreeNode)
meth public static void mergeNodeChildren(javax.swing.tree.TreeNode)
meth public static void mergeNodes(javax.swing.tree.TreeNode,javax.swing.tree.TreeNode)
supr javax.help.Merge
hfds debug

CLSS public javax.help.UnsupportedOperationException
cons public UnsupportedOperationException()
cons public UnsupportedOperationException(java.lang.String)
supr java.lang.RuntimeException

CLSS public abstract javax.help.WindowPresentation
cons public WindowPresentation(javax.help.HelpSet)
meth public boolean isDestroyedOnExit()
meth public boolean isDisplayed()
meth public boolean isTitleSetFromDocument()
meth public boolean isToolbarDisplayed()
meth public boolean isViewDisplayed()
meth public int getScreen()
meth public java.awt.Dimension getSize()
meth public java.awt.Font getFont()
meth public java.awt.Point getLocation()
meth public java.awt.Window getActivationWindow()
meth public java.awt.Window getHelpWindow()
meth public java.lang.String getCurrentView()
meth public java.lang.String getTitle()
meth public javax.help.HelpSet$Presentation getHelpSetPresentation()
meth public void createHelpWindow()
meth public void destroy()
meth public void setActivationObject(java.lang.Object)
meth public void setActivationWindow(java.awt.Window)
meth public void setCurrentView(java.lang.String)
meth public void setDestroyOnExit(boolean)
meth public void setDisplayed(boolean)
meth public void setFont(java.awt.Font)
meth public void setHelpSet(javax.help.HelpSet)
meth public void setHelpSetPresentation(javax.help.HelpSet$Presentation)
meth public void setLocale(java.util.Locale)
meth public void setLocation(java.awt.Point)
meth public void setScreen(int)
meth public void setSize(java.awt.Dimension)
meth public void setTitle(java.lang.String)
meth public void setTitleFromDocument(boolean)
meth public void setToolbarDisplayed(boolean)
meth public void setViewDisplayed(boolean)
supr javax.help.Presentation
hfds class$java$awt$Frame,class$java$awt$Window,currentView,debug,destroyOnExit,dialog,dl,frame,hsPres,image,jhelp,location,modalDeactivated,modallyActivated,ownerWindow,propertyChangeListener,screen,title,titleFromDocument,toolbarDisplayed,viewDisplayed
hcls WindowPropertyChangeListener

CLSS public abstract interface javax.help.event.HelpHistoryModelListener
intf java.util.EventListener
meth public abstract void historyChanged(javax.help.event.HelpHistoryModelEvent)

CLSS public abstract interface javax.help.event.HelpModelListener
intf java.util.EventListener
meth public abstract void idChanged(javax.help.event.HelpModelEvent)

CLSS public abstract interface javax.help.event.HelpSetListener
intf java.util.EventListener
meth public abstract void helpSetAdded(javax.help.event.HelpSetEvent)
meth public abstract void helpSetRemoved(javax.help.event.HelpSetEvent)

CLSS public abstract javax.swing.JComponent
cons public JComponent()
fld protected javax.accessibility.AccessibleContext accessibleContext
fld protected javax.swing.event.EventListenerList listenerList
fld protected javax.swing.plaf.ComponentUI ui
fld public final static int UNDEFINED_CONDITION = -1
fld public final static int WHEN_ANCESTOR_OF_FOCUSED_COMPONENT = 1
fld public final static int WHEN_FOCUSED = 0
fld public final static int WHEN_IN_FOCUSED_WINDOW = 2
fld public final static java.lang.String TOOL_TIP_TEXT_KEY = "ToolTipText"
innr public abstract AccessibleJComponent
intf java.io.Serializable
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
meth public javax.accessibility.AccessibleContext getAccessibleContext()
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
hfds ACTIONMAP_CREATED,ANCESTOR_INPUTMAP_CREATED,ANCESTOR_NOTIFIER_KEY,ANCESTOR_USING_BUFFER,AUTOSCROLLS_SET,COMPLETELY_OBSCURED,CREATED_DOUBLE_BUFFER,DEBUG_GRAPHICS_LOADED,FOCUS_INPUTMAP_CREATED,FOCUS_TRAVERSAL_KEYS_BACKWARD_SET,FOCUS_TRAVERSAL_KEYS_FORWARD_SET,INHERITS_POPUP_MENU,INPUT_VERIFIER_KEY,INPUT_VERIFIER_SOURCE_KEY,IS_DOUBLE_BUFFERED,IS_OPAQUE,IS_PAINTING_TILE,IS_PRINTING,IS_PRINTING_ALL,IS_REPAINTING,KEYBOARD_BINDINGS_KEY,KEY_EVENTS_ENABLED,NEXT_FOCUS,NOT_OBSCURED,OPAQUE_SET,PARTIALLY_OBSCURED,REQUEST_FOCUS_DISABLED,RESERVED_1,RESERVED_2,RESERVED_3,RESERVED_4,RESERVED_5,RESERVED_6,TRANSFER_HANDLER_KEY,WHEN_IN_FOCUSED_WINDOW_BINDINGS,WIF_INPUTMAP_CREATED,WRITE_OBJ_COUNTER_FIRST,WRITE_OBJ_COUNTER_LAST,aaTextInfo,actionMap,alignmentX,alignmentY,ancestorInputMap,autoscrolls,border,clientProperties,componentObtainingGraphicsFrom,componentObtainingGraphicsFromLock,defaultLocale,flags,focusController,focusInputMap,inputVerifier,isAlignmentXSet,isAlignmentYSet,managingFocusBackwardTraversalKeys,managingFocusForwardTraversalKeys,paintingChild,popupMenu,readObjectCallbacks,tempRectangles,uiClassID,verifyInputWhenFocusTarget,vetoableChangeSupport,windowInputMap
hcls ActionStandin,IntVector,KeyboardState,ReadObjectCallback

CLSS public abstract javax.swing.JComponent$AccessibleJComponent
cons protected AccessibleJComponent(javax.swing.JComponent)
fld protected java.awt.event.ContainerListener accessibleContainerHandler
fld protected java.awt.event.FocusListener accessibleFocusHandler
innr protected AccessibleContainerHandler
innr protected AccessibleFocusHandler
intf javax.accessibility.AccessibleExtendedComponent
meth protected java.lang.String getBorderTitle(javax.swing.border.Border)
meth public int getAccessibleChildrenCount()
meth public java.lang.String getAccessibleDescription()
meth public java.lang.String getAccessibleName()
meth public java.lang.String getTitledBorderText()
meth public java.lang.String getToolTipText()
meth public javax.accessibility.Accessible getAccessibleChild(int)
meth public javax.accessibility.AccessibleKeyBinding getAccessibleKeyBinding()
meth public javax.accessibility.AccessibleRole getAccessibleRole()
meth public javax.accessibility.AccessibleStateSet getAccessibleStateSet()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.awt.Container$AccessibleAWTContainer

CLSS public javax.swing.tree.DefaultMutableTreeNode
cons public DefaultMutableTreeNode()
cons public DefaultMutableTreeNode(java.lang.Object)
cons public DefaultMutableTreeNode(java.lang.Object,boolean)
fld protected boolean allowsChildren
fld protected java.lang.Object userObject
fld protected java.util.Vector children
fld protected javax.swing.tree.MutableTreeNode parent
fld public final static java.util.Enumeration<javax.swing.tree.TreeNode> EMPTY_ENUMERATION
intf java.io.Serializable
intf java.lang.Cloneable
intf javax.swing.tree.MutableTreeNode
meth protected javax.swing.tree.TreeNode[] getPathToRoot(javax.swing.tree.TreeNode,int)
meth public boolean getAllowsChildren()
meth public boolean isLeaf()
meth public boolean isNodeAncestor(javax.swing.tree.TreeNode)
meth public boolean isNodeChild(javax.swing.tree.TreeNode)
meth public boolean isNodeDescendant(javax.swing.tree.DefaultMutableTreeNode)
meth public boolean isNodeRelated(javax.swing.tree.DefaultMutableTreeNode)
meth public boolean isNodeSibling(javax.swing.tree.TreeNode)
meth public boolean isRoot()
meth public int getChildCount()
meth public int getDepth()
meth public int getIndex(javax.swing.tree.TreeNode)
meth public int getLeafCount()
meth public int getLevel()
meth public int getSiblingCount()
meth public java.lang.Object clone()
meth public java.lang.Object getUserObject()
meth public java.lang.Object[] getUserObjectPath()
meth public java.lang.String toString()
meth public java.util.Enumeration breadthFirstEnumeration()
meth public java.util.Enumeration children()
meth public java.util.Enumeration depthFirstEnumeration()
meth public java.util.Enumeration pathFromAncestorEnumeration(javax.swing.tree.TreeNode)
meth public java.util.Enumeration postorderEnumeration()
meth public java.util.Enumeration preorderEnumeration()
meth public javax.swing.tree.DefaultMutableTreeNode getFirstLeaf()
meth public javax.swing.tree.DefaultMutableTreeNode getLastLeaf()
meth public javax.swing.tree.DefaultMutableTreeNode getNextLeaf()
meth public javax.swing.tree.DefaultMutableTreeNode getNextNode()
meth public javax.swing.tree.DefaultMutableTreeNode getNextSibling()
meth public javax.swing.tree.DefaultMutableTreeNode getPreviousLeaf()
meth public javax.swing.tree.DefaultMutableTreeNode getPreviousNode()
meth public javax.swing.tree.DefaultMutableTreeNode getPreviousSibling()
meth public javax.swing.tree.TreeNode getChildAfter(javax.swing.tree.TreeNode)
meth public javax.swing.tree.TreeNode getChildAt(int)
meth public javax.swing.tree.TreeNode getChildBefore(javax.swing.tree.TreeNode)
meth public javax.swing.tree.TreeNode getFirstChild()
meth public javax.swing.tree.TreeNode getLastChild()
meth public javax.swing.tree.TreeNode getParent()
meth public javax.swing.tree.TreeNode getRoot()
meth public javax.swing.tree.TreeNode getSharedAncestor(javax.swing.tree.DefaultMutableTreeNode)
meth public javax.swing.tree.TreeNode[] getPath()
meth public void add(javax.swing.tree.MutableTreeNode)
meth public void insert(javax.swing.tree.MutableTreeNode,int)
meth public void remove(int)
meth public void remove(javax.swing.tree.MutableTreeNode)
meth public void removeAllChildren()
meth public void removeFromParent()
meth public void setAllowsChildren(boolean)
meth public void setParent(javax.swing.tree.MutableTreeNode)
meth public void setUserObject(java.lang.Object)
supr java.lang.Object
hcls BreadthFirstEnumeration,PathBetweenNodesEnumeration,PostorderEnumeration,PreorderEnumeration

CLSS public abstract interface javax.swing.tree.MutableTreeNode
intf javax.swing.tree.TreeNode
meth public abstract void insert(javax.swing.tree.MutableTreeNode,int)
meth public abstract void remove(int)
meth public abstract void remove(javax.swing.tree.MutableTreeNode)
meth public abstract void removeFromParent()
meth public abstract void setParent(javax.swing.tree.MutableTreeNode)
meth public abstract void setUserObject(java.lang.Object)

CLSS public abstract interface javax.swing.tree.TreeNode
meth public abstract boolean getAllowsChildren()
meth public abstract boolean isLeaf()
meth public abstract int getChildCount()
meth public abstract int getIndex(javax.swing.tree.TreeNode)
meth public abstract java.util.Enumeration children()
meth public abstract javax.swing.tree.TreeNode getChildAt(int)
meth public abstract javax.swing.tree.TreeNode getParent()

CLSS public abstract org.netbeans.api.javahelp.Help
cons protected Help()
meth public abstract java.lang.Boolean isValidID(java.lang.String,boolean)
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public abstract void showHelp(org.openide.util.HelpCtx,boolean)
meth public void showHelp(org.openide.util.HelpCtx)
supr java.lang.Object

