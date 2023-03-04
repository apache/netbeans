/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.debugger.jpda.ui.focus;

import com.sun.jdi.BooleanValue;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.LongValue;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.beans.PropertyVetoException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.io.InputOutput;
import org.netbeans.api.io.OutputWriter;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.IllegalThreadStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MirrorWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ThreadReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.UnsupportedOperationExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;

/**
 * Checks the presence of AWT grab status from
 * sun.awt.X11.XAwtState.grabWindowRef and release the grab.
 * This prevents from not responding X server due to paused application which holds the grab.
 * Warning: this class contains really hacked code accessing X11 functionality.
 * 
 * This class was extended with focus ungrab for JavaFX.
 * 
 * @author Martin Entlicher
 */
// See https://netbeans.org/bugzilla/show_bug.cgi?id=93076
class AWTGrabHandler {
    
    private static final Logger logger = Logger.getLogger(AWTGrabHandler.class.getName());
    
    private JPDADebuggerImpl debugger;
    private Boolean doGrabCheck = null; // Not decided at the beginning
    
    private static enum TOOLKIT {
        AWT,
        JAVAFX;
        
        static TOOLKIT get(String threadName) {
            if (threadName.startsWith("AWT-EventQueue")) {                      // NOI18N
                return AWT;
            }
            if (threadName.startsWith("JavaFX Application Thread")) {           // NOI18N
                return JAVAFX;
            }
            return null;
        }
    }
    
    AWTGrabHandler(JPDADebuggerImpl debugger) {
        this.debugger = debugger;
    }
    
    public boolean solveGrabbing(VirtualMachine vm) {
        if (vm == null) return true;
        if (Boolean.FALSE.equals(doGrabCheck)) {
            return true;
        }
        if (GraphicsEnvironment.isHeadless()) {
            doGrabCheck = Boolean.FALSE;
            return true;
        }
        // Check if AWT-EventQueue thread is suspended and a window holds a grab
        List<ThreadReference> allThreads = VirtualMachineWrapper.allThreads0(vm);
        for (ThreadReference t : allThreads) {
            if (!t.isSuspended()) continue;
            boolean success = solveGrabbing(t);
            if (!success) {
                return false;
            }
        }
        return true;
    }
    
    public boolean solveGrabbing(ThreadReference t) {
        
        if (Boolean.FALSE.equals(doGrabCheck)) {
            return true;
        }
        
        String name;
        try {
            name = ThreadReferenceWrapper.name(t);
        } catch (InternalExceptionWrapper ex) {
            return true; // Suppose the X grab is not there
        } catch (VMDisconnectedExceptionWrapper ex) {
            return true; // Disconnected - grab is solved
        } catch (ObjectCollectedExceptionWrapper ex) {
            return true;
        } catch (IllegalThreadStateExceptionWrapper ex) {
            return true;
        }
        logger.fine("solveGrabbing("+name+")");
        TOOLKIT tkt = TOOLKIT.get(name);
        if (tkt != null) {
            
            if (doGrabCheck == null) {
                doGrabCheck = checkXServer(t, tkt);
                logger.fine("Doing the AWT grab check = "+doGrabCheck);
            }
            if (Boolean.TRUE.equals(doGrabCheck)) {
                //System.err.println("");
                ObjectReference grabbedWindow;
                if (tkt == TOOLKIT.AWT) {
                    grabbedWindow = getGrabbedWindow(t);
                } else {
                    grabbedWindow = null; // We do not detect grabbed windows in FX
                }
                //System.err.println("Thread "+t+": some window is grabbed: "+isGrabbed+"\n");
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Thread "+t+": some window is grabbed: "+grabbedWindow);
                }
                if (grabbedWindow != null || tkt == TOOLKIT.JAVAFX) {
                    boolean successUngrab = ungrabWindow(t, grabbedWindow, 5000, tkt);
                    logger.fine("Grabbed window was ungrabbed: "+successUngrab);
                    if (!successUngrab) {
                        InputOutput io = debugger.getConsoleIO().getIO();
                        if (io != null) {
                            OutputWriter ow = io.getErr();
                            ow.println(NbBundle.getMessage(AWTGrabHandler.class, "MSG_GrabNotReleasedDbgContinue"));
                            ow.flush();
                            io.show();
                        }
                    }
                    return successUngrab;
                }
            }
        }
        return true;
    }
    
    /*public static boolean isGrabWindowCheck(VirtualMachine vm) {
        if (GraphicsEnvironment.isHeadless()) return false;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (ge.isHeadlessInstance()) return false;
        GraphicsDevice screen = ge.getDefaultScreenDevice();
        String screenID = screen.getIDstring();
        int type = screen.getType();
        System.err.println("Screen ID = \""+screenID+"\", type = "+type);
        return true;
    }*/
    
    private static ObjectReference getGrabbedWindow(ThreadReference t) {
        try {
            VirtualMachine vm = MirrorWrapper.virtualMachine(t);
            List<ReferenceType> classesByName = VirtualMachineWrapper.classesByName0(vm, "sun.awt.X11.XAwtState");  // NOI18N
            if (classesByName.isEmpty()) {
                logger.fine("No XAwtState class found.");
                return null;
            }
            ReferenceType rt = classesByName.get(0);
            Field grabWindowRefField = ReferenceTypeWrapper.fieldByName(rt, "grabWindowRef");
            if (grabWindowRefField == null) {
                logger.info("No grabWindowRef field");
                return null;
            }
            Value grabWindowRef = ReferenceTypeWrapper.getValue(rt, grabWindowRefField);
            if (grabWindowRef == null) {
                logger.fine("grabWindowRef field is null.");
                return null;
            }
            // We can read the grabbed window from the Reference.referent field.
            classesByName = VirtualMachineWrapper.classesByName0(vm, "java.lang.ref.Reference");
            if (classesByName.isEmpty()) {
                logger.info("No Reference class found.");
                return null;
            }
            Field referenceField = ReferenceTypeWrapper.fieldByName(classesByName.get(0), "referent");
            if (referenceField == null) {
                logger.info("No referent field in Reference class");
                return null;
            }
            Value grabWindow = ObjectReferenceWrapper.getValue((ObjectReference) grabWindowRef, referenceField);
            if (grabWindow == null) {
                logger.fine("Grabbed window is null");
                return null;
            }
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Grabbed window is "+grabWindow);
            }
            // Grabbed window is instance of sun.awt.X11.XFramePeer
            // TODO check XBaseWindow.disposed
            return (ObjectReference) grabWindow;
        } catch (InternalExceptionWrapper ex) {
            return null;
        } catch (VMDisconnectedExceptionWrapper ex) {
            return null;
        } catch (ObjectCollectedExceptionWrapper ex) {
            return null;
        } catch (ClassNotPreparedExceptionWrapper ex) {
            return null;
        }
    }
    
    private boolean ungrabWindow(final ThreadReference tr,
                                 final ObjectReference grabbedWindow,
                                 int timeout,
                                 final TOOLKIT tkt) {
        final boolean[] success = new boolean[] { false };
        Task task = debugger.getRequestProcessor().create(new Runnable() {
            @Override
            public void run() {
                switch (tkt) {
                    case AWT:
                        success[0] = ungrabWindowAWT(tr, grabbedWindow);
                        break;
                    case JAVAFX:
                        ungrabWindowFX(tr);
                        success[0] = true;  // Be always successful in FX.
                        break;
                }
                
            }
        });
        JPDAThreadImpl thread = debugger.getThread(tr);
        try {
            thread.notifyMethodInvoking();
            task.schedule(0);
            task.waitFinished(timeout);
        } catch (PropertyVetoException pvex) {
            logger.log(Level.INFO, "Method invoke vetoed", pvex);
            thread = null;
        } catch (InterruptedException ex) {
        } finally {
            if (thread != null) {
                thread.notifyMethodInvokeDone();
            }
        }
        if (!task.isFinished()) {
            // Something went wrong during ungrab. Maybe a deadlock?
            // We can not do anything but kill the debugger session to resolve the problem.
            debugger.finish();
            NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(AWTGrabHandler.class, "MSG_GrabNotReleasedDbgKilled"), NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(nd);
            return true; // "success" by terminating the debugger session
        }
        return success[0];
    }
    
    private boolean ungrabWindowAWT(ThreadReference tr, ObjectReference grabbedWindow) {
        // Call XBaseWindow.ungrabInput()
        try {
            VirtualMachine vm = MirrorWrapper.virtualMachine(grabbedWindow);
            List<ReferenceType> xbaseWindowClassesByName = VirtualMachineWrapper.classesByName(vm, "sun.awt.X11.XBaseWindow");
            if (xbaseWindowClassesByName.isEmpty()) {
                logger.info("Unable to release X grab, no XBaseWindow class in target VM "+VirtualMachineWrapper.description(vm));
                return false;
            }
            ClassType XBaseWindowClass = (ClassType) xbaseWindowClassesByName.get(0);
            Method ungrabInput = XBaseWindowClass.concreteMethodByName("ungrabInput", "()V");
            if (ungrabInput == null) {
                logger.info("Unable to release X grab, method ungrabInput not found in target VM "+VirtualMachineWrapper.description(vm));
                return false;
            }
            XBaseWindowClass.invokeMethod(tr, ungrabInput, Collections.<Value>emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
        } catch (VMDisconnectedExceptionWrapper vmdex) {
            return true; // Disconnected, all is good.
        } catch (Exception ex) {
            logger.log(Level.INFO, "Unable to release X grab.", ex);
            return false;
        }
        return true;
    }
    
    private boolean ungrabWindowFX(ThreadReference tr) {
        // javafx.stage.Window.impl_getWindows() - Iterator<Window>
        // while (iterator.hasNext()) {
        //     Window w = iterator.next();
        //     ungrabWindowFX(w);
        // }
        try {
            VirtualMachine vm = MirrorWrapper.virtualMachine(tr);
            List<ReferenceType> windowClassesByName = VirtualMachineWrapper.classesByName(vm, "javafx.stage.Window");
            if (windowClassesByName.isEmpty()) {
                logger.info("Unable to release FX X grab, no javafx.stage.Window class in target VM "+VirtualMachineWrapper.description(vm));
                return true; // We do not know whether there was any grab
            }
            ClassType WindowClass = (ClassType) windowClassesByName.get(0);
            Method getWindowsMethod = WindowClass.concreteMethodByName("impl_getWindows", "()Ljava/util/Iterator;");
            if (getWindowsMethod == null) {
                logger.info("Unable to release FX X grab, no impl_getWindows() method in "+WindowClass);
                return true; // We do not know whether there was any grab
            }
            ObjectReference windowsIterator = (ObjectReference) WindowClass.invokeMethod(tr, getWindowsMethod, Collections.<Value>emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
            if (windowsIterator == null) {
                return true; // We do not know whether there was any grab
            }
            InterfaceType IteratorClass = (InterfaceType) VirtualMachineWrapper.classesByName(vm, Iterator.class.getName()).get(0);
            Method hasNext = IteratorClass.methodsByName("hasNext", "()Z").get(0);
            Method next = IteratorClass.methodsByName("next", "()Ljava/lang/Object;").get(0);
            while (hasNext(hasNext, tr, windowsIterator)) {
                ObjectReference w = next(next, tr, windowsIterator);
                ungrabWindowFX(WindowClass, w, tr);
            }
        } catch (VMDisconnectedExceptionWrapper vmdex) {
            return true; // Disconnected, all is good.
        } catch (Exception ex) {
            logger.log(Level.INFO, "Unable to release FX X grab (if any).", ex);
            return true; // We do not know whether there was any grab
        }
        return true;
    }
    
    private boolean hasNext(Method hasNext, ThreadReference tr, ObjectReference iterator) throws Exception {
        Value v = iterator.invokeMethod(tr, hasNext, Collections.<Value>emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
        return (v instanceof BooleanValue) && ((BooleanValue) v).booleanValue();
    }
    
    private ObjectReference next(Method next, ThreadReference tr, ObjectReference iterator) throws Exception {
        return (ObjectReference) iterator.invokeMethod(tr, next, Collections.<Value>emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
    }
    
    private void ungrabWindowFX(ClassType WindowClass, ObjectReference w, ThreadReference tr) throws Exception {
        // javafx.stage.Window w
        // w.focusGrabCounter
        // while (focusGrabCounter-- > 0) {
        //     w.impl_getPeer().ungrabFocus(); OR: w.impl_peer.ungrabFocus();
        // }
        Field focusGrabCounterField = WindowClass.fieldByName("focusGrabCounter");
        if (focusGrabCounterField == null) {
            logger.info("Unable to release FX X grab, no focusGrabCounter field in "+w);
            return ;
        }
        Value focusGrabCounterValue = w.getValue(focusGrabCounterField);
        if (!(focusGrabCounterValue instanceof IntegerValue)) {
            logger.info("Unable to release FX X grab, focusGrabCounter does not have an integer value in "+w);
            return ;
        }
        int focusGrabCounter = ((IntegerValue) focusGrabCounterValue).intValue();
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Focus grab counter of "+w+" is: "+focusGrabCounter);
        }
        while (focusGrabCounter-- > 0) {
            //Method impl_getPeerMethod = WindowClass.concreteMethodByName("impl_getPeer", "");
            Field impl_peerField = WindowClass.fieldByName("impl_peer");
            if (impl_peerField == null) {
                logger.info("Unable to release FX X grab, no impl_peer field in "+w);
                return ;
            }
            ObjectReference impl_peer = (ObjectReference) w.getValue(impl_peerField);
            if (impl_peer == null) {
                continue;
            }
            InterfaceType TKStageClass = (InterfaceType) w.virtualMachine().classesByName("com.sun.javafx.tk.TKStage").get(0);
            Method ungrabFocusMethod = TKStageClass.methodsByName("ungrabFocus", "()V").get(0);
            impl_peer.invokeMethod(tr, ungrabFocusMethod, Collections.<Value>emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("FX Window "+w+" was successfully ungrabbed.");
            }
        }
    }
    
    private boolean ungrabWindowFX_OLD(ThreadReference tr) {
        // com.sun.javafx.tk.quantum.WindowStage has:
        // field static Map<Window, WindowStage> platformWindows = new HashMap<>();
        // com.sun.glass.ui.Window.ungrabFocus()
        try {
            VirtualMachine vm = MirrorWrapper.virtualMachine(tr);
            List<ReferenceType> windowStageClassesByName = VirtualMachineWrapper.classesByName(vm, "com.sun.javafx.tk.quantum.WindowStage");
            if (windowStageClassesByName.isEmpty()) {
                logger.info("Unable to release FX X grab, no quantum WindowStage class in target VM "+VirtualMachineWrapper.description(vm));
                return false;
            }
            ClassType WindowStageClass = (ClassType) windowStageClassesByName.get(0);
            Field platformWindowsField = WindowStageClass.fieldByName("platformWindows");
            if (platformWindowsField == null) {
                logger.info("Unable to release FX X grab, no platformWindows field found in WindowStage in target VM "+VirtualMachineWrapper.description(vm));
                return false;
            }
            ObjectReference platformWindows = (ObjectReference) WindowStageClass.getValue(platformWindowsField);
            if (platformWindows == null) {
                logger.info("Unable to release FX X grab, no platformWindows field has null value in WindowStage in target VM "+VirtualMachineWrapper.description(vm));
                return false;
            }
        } catch (VMDisconnectedExceptionWrapper vmdex) {
            return true; // Disconnected, all is good.
        } catch (Exception ex) {
            logger.log(Level.INFO, "Unable to release FX X grab (if any).", ex);
            return true; // We do not know whether there was any grab
        }
        return true;
    }

    private boolean checkXServer(ThreadReference t, TOOLKIT tkt) {
        try {
            return checkXServerExc(t, tkt);
        } catch (Exception ex) {
            logger.log(Level.FINE, "Exception thrown from checkXServer: ", ex);
            return false;
        }
    }
    
    private Boolean checkXServerExc(ThreadReference tr, TOOLKIT tkt) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
        // Check if we're running under X11:
        //if (!(Toolkit.getDefaultToolkit() instanceof sun.awt.X11.XToolkit)) {
        //    return false; // Not an X server
        //}
        Toolkit t = Toolkit.getDefaultToolkit();
        Class XToolkit;
        try {
            XToolkit = Class.forName("sun.awt.X11.XToolkit");                   // NOI18N
        } catch (ClassNotFoundException cnfex) {
            throw cnfex;
        } catch (ExceptionInInitializerError eie) {
            // We're not able to load the X11 toolkit class
            // See http://netbeans.org/bugzilla/show_bug.cgi?id=208751
            return false;
        } catch (ThreadDeath td) {
            throw td;
        } catch (Throwable ccex) {
            logger.log(Level.INFO, "Can not load sun.awt.X11.XToolkit class.", ccex);
            return false;
        }
        if (!XToolkit.isAssignableFrom(t.getClass())) {
            logger.fine("XToolkit not found.");                                 // NOI18N
            return false;
        }
        
        // To verify if the apps are running on the same X server, set a window property
        //long defaultRootWindow = XToolkit.getDefaultRootWindow();
        long defaultRootWindow = (Long) XToolkit.getMethod("getDefaultRootWindow").invoke(t);
        String grabCheckStr = "NB_debugger_AWT_grab_check";
        Class XAtom = Class.forName("sun.awt.X11.XAtom");
        //XAtom xa = new XAtom(grabCheckStr, true);
        Constructor XAtomC = XAtom.getConstructor(String.class, Boolean.TYPE);
        Object xa = XAtomC.newInstance(grabCheckStr, true);
        //xa.setProperty(defaultRootWindow, grabCheckStr);
        // Set it later on when we verify are able to read it from the target VM.
        // Set the property in this VM: xa.setProperty(defaultRootWindow, grabCheckStr);
        XAtom.getMethod("setProperty", Long.TYPE, String.class).invoke(xa, defaultRootWindow, grabCheckStr);
            
        
        JPDAThreadImpl thread = null;
        try {
            // VERIFY WITH DEBUGGER HERE
            // Run in target VM:
            /*
                // TODO: Test the headless mode:
                if (GraphicsEnvironment.isHeadless()) {
                    return false;
                }
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                if (ge.isHeadlessInstance()) {
                    return false;
                }

                if (!(Toolkit.getDefaultToolkit() instanceof XToolkit)) {
                    return ; // Not an X server
                }
                // To verify if the apps are running on the same X server, read a window property
                boolean sunAwtDisableGrab = XToolkit.getSunAwtDisableGrab();
                if (sunAwtDisableGrab) return false;
                long defaultRootWindow = XToolkit.getDefaultRootWindow();
                String grabCheckStr = "NB_debugger_AWT_grab_check";
                XAtom xa = new XAtom(grabCheckStr, true);
                String prop = xa.getProperty(defaultRootWindow);
             */
            VirtualMachine virtualMachine = MirrorWrapper.virtualMachine(tr);
            List<ReferenceType> xtoolkitClassesByName = null;
            switch (tkt) {
                case AWT:
                    xtoolkitClassesByName = VirtualMachineWrapper.classesByName(virtualMachine, "sun.awt.X11.XToolkit");
                    if (xtoolkitClassesByName.isEmpty()) {
                        // not an X Server
                        logger.fine("No sun.awt.X11.XToolkit class found => not an X server");
                        return false;
                    }
                    break;
                case JAVAFX:
                    if (VirtualMachineWrapper.classesByName(virtualMachine, "com.sun.glass.ui.gtk.GtkWindow").isEmpty()) {
                        // not an X Server
                        logger.fine("No com.sun.glass.ui.gtk.GtkWindow class found => not an X server");
                        return false;
                    }
                    break;
                default:
                    throw new IllegalStateException(tkt.name());
            }
            List<ReferenceType> toolkitClassesByName = VirtualMachineWrapper.classesByName(virtualMachine, "java.awt.Toolkit");
            if (toolkitClassesByName.isEmpty()) {
                if (tkt != TOOLKIT.AWT) {
                    logger.fine("Have no AWT toolkit in "+tkt.name()+" therefore doing ungrab automatically.");
                    return true;    // NO AWT toolkit, therefore we must suppose that there can be a grab
                }
                return null; // There is AWT-EventQueue thread and no Toolkit ? Try again, later...
            }
            thread = debugger.getThread(tr);
            thread.notifyMethodInvoking();
            //writeLock = thread.accessLock.writeLock();
            //writeLock.lock();
            ClassType ToolkitClass = (ClassType) toolkitClassesByName.get(0);
            Method getDefaultToolkit = ClassTypeWrapper.concreteMethodByName(ToolkitClass, "getDefaultToolkit", "()Ljava/awt/Toolkit;");
            ObjectReference toolkit = (ObjectReference) ToolkitClass.invokeMethod(tr, getDefaultToolkit, Collections.<Value>emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
            
            ClassType XToolkitClass;
            switch (tkt) {
                case AWT:
                    XToolkitClass = (ClassType) xtoolkitClassesByName.get(0);
                    break;
                case JAVAFX:
                    ReferenceType toolkitType = toolkit.referenceType();
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("Toolkit Type = "+toolkitType.name()+" is X toolkit = "+toolkitType.name().endsWith(".X11.XToolkit"));
                    }
                    if (!toolkitType.name().endsWith(".X11.XToolkit")) {
                        // NO X toolkit
                        return false;
                    }
                    XToolkitClass = (ClassType) toolkitType;
                    /*if (XToolkitClass == null) {
                        // NO X toolkit, therefore we must suppose that there can be a grab
                        return true;
                    }*/
                    break;
                default:
                    throw new IllegalStateException(tkt.name());
            }
            // if (!(Toolkit.getDefaultToolkit() instanceof XToolkit)) {
            if (!isAssignable(XToolkitClass, (ClassType) toolkit.referenceType())) {
                return false; // XToolkit not found.
            }
            
            if (tkt == TOOLKIT.AWT) {
                //boolean sunAwtDisableGrab = XToolkit.getSunAwtDisableGrab();
                Method getSunAwtDisableGrab = ClassTypeWrapper.concreteMethodByName(XToolkitClass, "getSunAwtDisableGrab", "()Z");
                if (getSunAwtDisableGrab == null) {
                    logger.fine("XToolkit.getSunAwtDisableGrab() method not found in target VM "+VirtualMachineWrapper.description(virtualMachine));
                } else {
                    BooleanValue sunAwtDisableGrab = (BooleanValue) XToolkitClass.invokeMethod(tr, getSunAwtDisableGrab, Collections.<Value>emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("sunAwtDisableGrab = "+sunAwtDisableGrab.value());
                    }
                    if (sunAwtDisableGrab.value()) {
                        // AWT grab is disabled, no need to check for grabbed windows.
                        return false;
                    }
                }
            }
            
            //long defaultRootWindow = XToolkit.getDefaultRootWindow();
            Method getDefaultRootWindow = ClassTypeWrapper.concreteMethodByName(XToolkitClass, "getDefaultRootWindow", "()J");
            if (getDefaultRootWindow == null) {
                // No way to find the root window
                logger.fine("XToolkit.getDefaultRootWindow() method does not exist in the target VM "+VirtualMachineWrapper.description(virtualMachine));
                return false;
            }
            LongValue defaultRootWindowValue = (LongValue) XToolkitClass.invokeMethod(tr, getDefaultRootWindow, Collections.<Value>emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
            
            // XAtom xa = new XAtom(grabCheckStr, true);
            List<ReferenceType> xatomClassesByName = VirtualMachineWrapper.classesByName(virtualMachine, "sun.awt.X11.XAtom");
            if (xatomClassesByName.isEmpty()) {
                logger.fine("No XAtom class in target VM "+VirtualMachineWrapper.description(virtualMachine));
                return false;
            }
            ClassType XAtomClass = (ClassType) xatomClassesByName.get(0);
            Method xatomConstructor = XAtomClass.concreteMethodByName("<init>", "(Ljava/lang/String;Z)V");
            if (xatomConstructor == null) {
                logger.fine("No XAtom(String, boolean) constructor in target VM "+VirtualMachineWrapper.description(virtualMachine));
                return false;
            }
            ObjectReference xaInstance = XAtomClass.newInstance(tr, xatomConstructor,
                    Arrays.asList(VirtualMachineWrapper.mirrorOf(virtualMachine, grabCheckStr),
                                  VirtualMachineWrapper.mirrorOf(virtualMachine, true)),
                    ObjectReference.INVOKE_SINGLE_THREADED);
            
            // String prop = xa.getProperty(defaultRootWindow);
            Method getProperty = XAtomClass.concreteMethodByName("getProperty", "(J)Ljava/lang/String;");
            if (getProperty == null) {
                logger.fine("No XAtom.getProperty(long) method in target VM "+VirtualMachineWrapper.description(virtualMachine));
                return false;
            }
            StringReference srProperty = (StringReference) xaInstance.invokeMethod(tr, getProperty, Collections.singletonList(defaultRootWindowValue), ObjectReference.INVOKE_SINGLE_THREADED);
            if (srProperty == null) {
                logger.fine(grabCheckStr+" property not defined.");
                return false; // The property is not defined => we're running on different X servers.
            }
            String prop = srProperty.value();
            logger.fine(grabCheckStr+" property = "+prop);
            return grabCheckStr.equals(prop);
            
        } catch (InternalExceptionWrapper iex) {
            // Something unexpected, give up
            logger.log(Level.FINE, "InternalExceptionWrapper in target VM.", iex);
            return false;
        } catch (VMDisconnectedExceptionWrapper dex) {
            return false;
        } catch (ClassNotPreparedExceptionWrapper cnpex) {
            logger.log(Level.FINE, "ClassNotPreparedExceptionWrapper in target VM.", cnpex);
            return null; // Something is not initialized yet...
        } catch (ClassNotLoadedException cnlex) {
            logger.log(Level.FINE, "ClassNotLoadedException in target VM.", cnlex);
            return null; // Something is not initialized yet...
        } catch (IncompatibleThreadStateException itsex) {
            logger.log(Level.FINE, "IncompatibleThreadStateException in target VM.", itsex);
            return null; // Try next time...
        } catch (InvalidTypeException itex) {
            Exceptions.printStackTrace(itex);
            return false;
        } catch (InvocationException iex) {
            Exceptions.printStackTrace(iex);
            return false;
        } catch (UnsupportedOperationExceptionWrapper uoex) {
            if (logger.isLoggable(Level.FINE)) {
                try {
                    logger.log(Level.FINE, "Unsupported operation in target VM "+VirtualMachineWrapper.description(tr.virtualMachine()), uoex);
                } catch (Exception ex) {
                    logger.log(Level.FINE, "Unsupported operation in target VM.", uoex);
                }
            }
            return false;
        } catch (PropertyVetoException pvex) {
            logger.fine("Method invocation vetoed. "+pvex);
            thread = null;
            return null;
        } finally {
            if (thread != null) {
                thread.notifyMethodInvokeDone();
            }
            //xa.DeleteProperty(defaultRootWindow);
            XAtom.getMethod("DeleteProperty", Long.TYPE).invoke(xa, defaultRootWindow);
        }
    }

    private static boolean isAssignable(ClassType ct1, ClassType ct2) {
        // return ct1.isAssignableFrom(ct2)
        if (ct1.equals(ct2)) {
            return true;
        }
        ClassType cts = ct2.superclass();
        if (cts != null) {
            return isAssignable(ct1, cts);
        } else {
            return false;
        }
    }
    
}
