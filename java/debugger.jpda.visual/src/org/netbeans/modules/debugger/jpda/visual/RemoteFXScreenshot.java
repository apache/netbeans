/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.debugger.jpda.visual;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.DoubleValue;
import com.sun.jdi.Field;
import com.sun.jdi.FloatValue;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.UnsupportedOperationExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.visual.RemoteServices.ServiceType;
import org.netbeans.modules.debugger.jpda.visual.actions.ComponentBreakpointActionProvider;
import org.netbeans.modules.debugger.jpda.visual.actions.GoToAddIntoHierarchyAction;
import org.netbeans.modules.debugger.jpda.visual.actions.GoToFieldDeclarationAction;
import org.netbeans.modules.debugger.jpda.visual.actions.GoToSourceAction;
import org.netbeans.modules.debugger.jpda.visual.actions.ToggleComponentBreakpointAction;
import org.netbeans.modules.debugger.jpda.visual.breakpoints.ComponentBreakpoint;
import org.netbeans.modules.debugger.jpda.visual.models.ComponentBreakpointsActionsProvider;
import org.netbeans.modules.debugger.jpda.visual.spi.ComponentInfo;
import org.netbeans.modules.debugger.jpda.visual.spi.RemoteScreenshot;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Takes screenshot of a remote application.
 * 
 * @author Martin Entlicher
 */
public class RemoteFXScreenshot {
    
    private static final Logger logger = Logger.getLogger(RemoteFXScreenshot.class.getName());
    
    private static final String FXAppThreadName = "JavaFX Application Thread"; // NOI18N
    
    private static final RemoteScreenshot[] NO_SCREENSHOTS = new RemoteScreenshot[] {};

    
    private RemoteFXScreenshot() {
    }
    
    private static RemoteScreenshot createRemoteFXScreenshot(DebuggerEngine engine, VirtualMachine vm, ThreadReference tr, String title, ObjectReference window, SGComponentInfo componentInfo) throws InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException, InvocationException {
        final ClassType bufImageClass = getClass(vm, tr, "java.awt.image.BufferedImage");
        final ClassType imageClass = getClass(vm, tr, "javafx.scene.image.Image");
        final ClassType sceneClass = getClass(vm, tr, "javafx.scene.Scene");
        final ClassType windowClass = getClass(vm, tr, "javafx.stage.Window");
        final ClassType utilsClass = getClass(vm, tr, "javafx.embed.swing.SwingFXUtils");

        final Method getScene = windowClass.concreteMethodByName("getScene", "()Ljavafx/scene/Scene;");
        //pre-FX 2.2 API (removed in FX 8)
        final Method fromPlatformImage = imageClass.concreteMethodByName("impl_fromPlatformImage", "(Ljava/lang/Object;)Ljavafx/scene/image/Image;"); 
        final Method convertImage = imageClass.concreteMethodByName("impl_toExternalImage", "(Ljava/lang/Object;)Ljava/lang/Object;");
        final Method renderImage = sceneClass.concreteMethodByName("renderToImage", "(Ljava/lang/Object;FZ)Ljava/lang/Object;");
        // FX 2.2 API (works in FX 8)
        final Method fromFXImage = (utilsClass != null) ? utilsClass.concreteMethodByName("fromFXImage", "(Ljavafx/scene/image/Image;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;") : null;
        final Method snapshot = sceneClass.concreteMethodByName("snapshot", "(Ljavafx/scene/image/WritableImage;)Ljavafx/scene/image/WritableImage;");

        ObjectReference scene = (ObjectReference) window.invokeMethod(tr, getScene, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);

        FloatValue factor = vm.mirrorOf(1.0f);
        BooleanValue syncNeeded = vm.mirrorOf(false);

        // first try FX2.2 API, then try fallback to pre-2.2 API
        ObjectReference image = null;
        if (snapshot != null) {
            image = (ObjectReference)scene.invokeMethod(tr, snapshot, Arrays.asList(new Value[]{null}), ObjectReference.INVOKE_SINGLE_THREADED);
        } else if (renderImage != null) {
            ObjectReference pImage = (ObjectReference)scene.invokeMethod(tr, renderImage, Arrays.asList(null, factor, syncNeeded), ObjectReference.INVOKE_SINGLE_THREADED);
            image = (ObjectReference)imageClass.invokeMethod(tr, fromPlatformImage, Arrays.asList(pImage), ObjectReference.INVOKE_SINGLE_THREADED);             
        }

        // first try FX2.2 API, then try fallback to pre-2.2 API
        ObjectReference bufImage = null;
        if (fromFXImage != null) {
            bufImage = (ObjectReference)utilsClass.invokeMethod(tr, fromFXImage, Arrays.asList(image, null), ObjectReference.INVOKE_SINGLE_THREADED);
        } else if (convertImage != null) {
            bufImage = (ObjectReference)image.invokeMethod(tr, convertImage, Arrays.asList(bufImageClass.classObject()), ObjectReference.INVOKE_SINGLE_THREADED);
        }

        Method getData = ((ClassType)bufImage.referenceType()).concreteMethodByName("getData", "()Ljava/awt/image/Raster;");
        ObjectReference rasterRef = (ObjectReference) bufImage.invokeMethod(tr, getData, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);

        ClassType rasterType = (ClassType)rasterRef.referenceType();
        Method getWidth = rasterType.concreteMethodByName("getWidth", "()I");
        Method getHeight = rasterType.concreteMethodByName("getHeight", "()I");
        Method getDataElements = rasterType.concreteMethodByName("getDataElements", "(IIIILjava/lang/Object;)Ljava/lang/Object;");
        IntegerValue zero = vm.mirrorOf(0);
        IntegerValue width = (IntegerValue)rasterRef.invokeMethod(tr, getWidth, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
        IntegerValue height = (IntegerValue)rasterRef.invokeMethod(tr, getHeight, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
        ArrayReference data = (ArrayReference) rasterRef.invokeMethod(tr, getDataElements, Arrays.asList(zero, zero, width, height, null), ObjectReference.INVOKE_SINGLE_THREADED);

        logger.log(Level.FINE, "Image data length = {0}", data.length());

        List<Value> dataValues = data.getValues();
        int[] dataArray = new int[data.length()];
        int i = 0;
        for (Value v : dataValues) {
            dataArray[i++] = ((IntegerValue) v).value();
        }
        final BufferedImage bi = new BufferedImage(width.value(), height.value(), BufferedImage.TYPE_INT_ARGB);
        WritableRaster raster = bi.getRaster();
        raster.setDataElements(0, 0, width.intValue(), height.intValue(), dataArray);
        if (RemoteAWTScreenshot.FAST_FIELDS_SEARCH) {
            ComponentsFieldFinder.findFieldsForComponents(componentInfo);
        }
        return new RemoteScreenshot(engine, title, width.intValue(), height.intValue(), bi, componentInfo);
    }
    
    public static RemoteScreenshot[] takeCurrent() throws RetrievalException {
        DebuggerEngine engine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (engine != null) {
            JPDADebugger debugger = engine.lookupFirst(null, JPDADebugger.class);
            takeCurrent(debugger);
        }
        return NO_SCREENSHOTS;
    }
    
    public static RemoteScreenshot[] takeCurrent(JPDADebugger debugger) throws RetrievalException {
        logger.log(Level.FINE, "Debugger = {0}", debugger);
        if (debugger != null) {
            DebuggerEngine engine = ((JPDADebuggerImpl) debugger).getSession().getCurrentEngine();
            List<JPDAThread> allThreads = debugger.getThreadsCollector().getAllThreads();
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "Threads = {0}", allThreads);
            }
            for (JPDAThread t : allThreads) {
                if (t.getName().startsWith(FXAppThreadName)) {
                    return take(t, engine, (JPDADebuggerImpl)debugger);
                }
            }
        }

        return NO_SCREENSHOTS;
    }
    
    public static RemoteScreenshot[] take(final JPDAThread t, final DebuggerEngine engine, final JPDADebuggerImpl d) throws RetrievalException {//throws ClassNotLoadedException, IncompatibleThreadStateException, InvalidTypeException, InvocationException {
//        return new RemoteScreenshot[0];
        //RemoteScreenshot[] screenshots = NO_SCREENSHOTS;
        final ThreadReference tawt = ((JPDAThreadImpl) t).getThreadReference();
        final VirtualMachine vm = tawt.virtualMachine();
        final ClassType windowClass = getClass(vm, tawt, "javafx.stage.Window");
        
        if (windowClass == null) {
            logger.fine("No Window");
            return NO_SCREENSHOTS;
        }

        //Method getWindows = null;//windowClass.concreteMethodByName("getOwnerlessWindows", "()[Ljava/awt/Window;");
        final Method getWindows = windowClass.concreteMethodByName("impl_getWindows", "()Ljava/util/Iterator;");
        if (getWindows == null) {
            logger.fine("No getWindows() method!");
            String msg = NbBundle.getMessage(RemoteFXScreenshot.class, "MSG_ScreenshotNotTaken_MissingMethod", "java.awt.Window.getWindows()");
            throw new RetrievalException(msg);
        }

        final List<RemoteScreenshot> screenshots = new ArrayList<RemoteScreenshot>();
        final RetrievalException[] retrievalExceptionPtr = new RetrievalException[] { null };
        
        final RetrievalException[] thrown = new RetrievalException[1];
        try {
            RemoteServices.runOnStoppedThread(t, new Runnable() {
                @Override
                public void run() {
                    try {
                        pauseAll(tawt, vm);
                        retrieveScreenshots((JPDAThreadImpl)t, tawt, vm, engine, d, screenshots);
                    } catch (RetrievalException e) {
                        thrown[0] = e;
                    } finally {
                        try {
                            resumeAll(tawt, vm);
                        } catch (RetrievalException e) {
                            thrown[0] = e;
                        }
                    }
                }
            }, RemoteServices.ServiceType.FX);
        } catch (PropertyVetoException pve) {
            throw new RetrievalException(pve.getMessage(), pve);
        }
        if (thrown[0] != null) {
            throw thrown[0];
        }
        
        if (retrievalExceptionPtr[0] != null) {
            throw retrievalExceptionPtr[0];
        }
        return screenshots.toArray(new RemoteScreenshot[] {});
    }
    
    private static void retrieveScreenshots(JPDAThreadImpl t, final ThreadReference tr, VirtualMachine vm, DebuggerEngine engine, JPDADebuggerImpl d, final List<RemoteScreenshot> screenshots) throws RetrievalException {
        try {
            final ClassType windowClass = getClass(vm, tr, "javafx.stage.Window");
            
            Method getWindows = windowClass.concreteMethodByName("impl_getWindows", "()Ljava/util/Iterator;");
            Method windowName = windowClass.concreteMethodByName("impl_getMXWindowType", "()Ljava/lang/String;");

            ObjectReference iterator = (ObjectReference)windowClass.invokeMethod(tr, getWindows, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
            ClassType iteratorClass = (ClassType)iterator.referenceType();
            Method hasNext = iteratorClass.concreteMethodByName("hasNext", "()Z");
            Method next = iteratorClass.concreteMethodByName("next", "()Ljava/lang/Object;");
            
            boolean nextFlag = false;
            do {
                BooleanValue bv = (BooleanValue)iterator.invokeMethod(tr, hasNext, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                nextFlag = bv.booleanValue();
                if (nextFlag) {
                    ObjectReference window = (ObjectReference)iterator.invokeMethod(tr, next, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                    StringReference name = (StringReference)window.invokeMethod(tr, windowName, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                    SGComponentInfo windowInfo = new SGComponentInfo(t, window);
                    
                    screenshots.add(createRemoteFXScreenshot(engine, vm, tr, name.value(), window, windowInfo));
                }
            } while (nextFlag);
        } catch (Exception e) {
            throw new RetrievalException(e.getMessage(), e);
        }
    }
    
    private static boolean pauseAll(ThreadReference tr, VirtualMachine vm) throws RetrievalException {
        final ClassType toolkitClass = getClass(vm, tr, "com.sun.javafx.tk.Toolkit");
        
        if (toolkitClass == null) {
            logger.fine("No Toolkiit");
            return false;
        }
                
        final Method getDefaultTk = toolkitClass.concreteMethodByName("getToolkit", "()Lcom/sun/javafx/tk/Toolkit;");
        if (getDefaultTk == null) {
            logger.fine("No getToolkit() method!");
            String msg = NbBundle.getMessage(RemoteFXScreenshot.class, "MSG_ScreenshotNotTaken_MissingMethod", "com.sun.javafx.tk.Toolkit.getToolkit()");
            throw new RetrievalException(msg);
        }

        final Method pauseScenes = toolkitClass.concreteMethodByName("pauseScenes", "()V");
        if (pauseScenes == null) {
            logger.fine("No pauseScenes() method!");
            String msg = NbBundle.getMessage(RemoteFXScreenshot.class, "MSG_ScreenshotNotTaken_MissingMethod", "com.sun.javafx.tk.Toolkit.pauseScenes()");
            throw new RetrievalException(msg);
        }
        
        try {
            ObjectReference tk = (ObjectReference)toolkitClass.invokeMethod(tr, getDefaultTk, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
            tk.invokeMethod(tr, pauseScenes, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
            
            pauseMedia(tr, vm);
            return true;
        } catch (InvalidTypeException e) {
            throw new RetrievalException(e.getMessage(), e);
        } catch (ClassNotLoadedException e) {
            throw new RetrievalException(e.getMessage(), e);
        } catch (IncompatibleThreadStateException e) {
            throw new RetrievalException(e.getMessage(), e);
        } catch (InvocationException e) {
            throw new RetrievalException(e.getMessage(), e);
        } catch (ClassNotPreparedException e) {
            throw new RetrievalException(e.getMessage(), e);
        }
    }
    
    private static boolean resumeAll(ThreadReference tr, VirtualMachine vm) throws RetrievalException {
        final ClassType toolkitClass = getClass(vm, tr, "com.sun.javafx.tk.Toolkit");
        
        if (toolkitClass == null) {
            logger.fine("No Toolkiit");
            return false;
        }
                
        final Method getDefaultTk = toolkitClass.concreteMethodByName("getToolkit", "()Lcom/sun/javafx/tk/Toolkit;");
        if (getDefaultTk == null) {
            logger.fine("No getToolkit() method!");
            String msg = NbBundle.getMessage(RemoteFXScreenshot.class, "MSG_ScreenshotNotTaken_MissingMethod", "com.sun.javafx.tk.Toolkit.getToolkit()");
            throw new RetrievalException(msg);
        }
        
        final Method resumeScenes = toolkitClass.concreteMethodByName("resumeScenes", "()V");
        if (resumeScenes == null) {
            logger.fine("No pauseScenes() method!");
            String msg = NbBundle.getMessage(RemoteFXScreenshot.class, "MSG_ScreenshotNotTaken_MissingMethod", "com.sun.javafx.tk.Toolkit.resumeScenes()");
            throw new RetrievalException(msg);
        }
        
        try {
            ObjectReference tk = (ObjectReference)toolkitClass.invokeMethod(tr, getDefaultTk, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
            tk.invokeMethod(tr, resumeScenes, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
            
            resumeMedia(tr, vm);
            return true;
        } catch (InvalidTypeException e) {
            throw new RetrievalException(e.getMessage(), e);
        } catch (ClassNotLoadedException e) {
            throw new RetrievalException(e.getMessage(), e);
        } catch (IncompatibleThreadStateException e) {
            throw new RetrievalException(e.getMessage(), e);
        } catch (InvocationException e) {
            throw new RetrievalException(e.getMessage(), e);
        }
    }
    
    private static final Collection<ObjectReference> pausedPlayers = new ArrayList<ObjectReference>();
    
    private static void pauseMedia(ThreadReference tr, VirtualMachine vm) throws InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException, InvocationException {
        final ClassType audioClipClass = getClass(vm, tr, "com.sun.media.jfxmedia.AudioClip");
        final ClassType mediaManagerClass = getClass(vm, tr, "com.sun.media.jfxmedia.MediaManager");
        final InterfaceType mediaPlayerClass = getInterface(vm, tr, "com.sun.media.jfxmedia.MediaPlayer");
        final ClassType playerStateEnum = getClass(vm, tr, "com.sun.media.jfxmedia.events.PlayerStateEvent$PlayerState");
        
        if (audioClipClass != null) {
            Method stopAllClips = audioClipClass.concreteMethodByName("stopAllClips", "()V");
            audioClipClass.invokeMethod(tr, stopAllClips, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
        }
        
        if (mediaManagerClass != null && mediaPlayerClass != null && playerStateEnum != null) {
            Method getAllPlayers = mediaManagerClass.concreteMethodByName("getAllMediaPlayers", "()Ljava/util/List;");

            ObjectReference plList = (ObjectReference)mediaManagerClass.invokeMethod(tr, getAllPlayers, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);

            if (plList != null) {
                ClassType listType = (ClassType)plList.referenceType();
                Method iterator = listType.concreteMethodByName("iterator", "()Ljava/util/Iterator;");
                ObjectReference plIter = (ObjectReference)plList.invokeMethod(tr, iterator, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);

                ClassType iterType = (ClassType)plIter.referenceType();
                Method hasNext = iterType.concreteMethodByName("hasNext", "()Z");
                Method next = iterType.concreteMethodByName("next", "()Ljava/lang/Object;");


                Field playingState = playerStateEnum.fieldByName("PLAYING");

                Method getState = mediaPlayerClass.methodsByName("getState", "()Lcom/sun/media/jfxmedia/events/PlayerStateEvent$PlayerState;").get(0);
                Method pausePlayer = mediaPlayerClass.methodsByName("pause", "()V").get(0);
                boolean hasNextFlag = false;
                do {
                    BooleanValue v = (BooleanValue)plIter.invokeMethod(tr, hasNext, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                    hasNextFlag = v.booleanValue();
                    if (hasNextFlag) {
                        ObjectReference player = (ObjectReference)plIter.invokeMethod(tr, next, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                        ObjectReference curState = (ObjectReference)player.invokeMethod(tr, getState, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                        if (playingState.equals(curState)) {
                            player.invokeMethod(tr, pausePlayer, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                            pausedPlayers.add(player);
                        }
                    }
                } while (hasNextFlag);
            }
        }
    }
    
    private static void resumeMedia(ThreadReference tr, VirtualMachine vm) throws InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException, InvocationException {
        if (!pausedPlayers.isEmpty()) {
            final InterfaceType mediaPlayerClass = getInterface(vm, tr, "com.sun.media.jfxmedia.MediaPlayer");
            List<Method> play = mediaPlayerClass.methodsByName("play", "()V");
            if (play.isEmpty()) {
                return;
            }
            Method p = play.iterator().next();
            for(ObjectReference pR : pausedPlayers) {
                pR.invokeMethod(tr, p, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
            }
        }
    }
    
    private static ClassType getClass(VirtualMachine vm, ThreadReference tr, String name) {
        ReferenceType t = getType(vm, tr, name);
        if (t instanceof ClassType) {
            return (ClassType)t;
        }
        logger.log(Level.WARNING, "{0} is not a class but {1}", new Object[]{name, t}); // NOI18N
        return null;
    } 
    
    private static InterfaceType getInterface(VirtualMachine vm, ThreadReference tr, String name) {
        ReferenceType t = getType(vm, tr, name);
        if (t instanceof InterfaceType) {
            return (InterfaceType)t;
        }
        logger.log(Level.WARNING, "{0} is not an interface but {1}", new Object[]{name, t}); // NOI18N
        return null;
    } 

    
    private static ReferenceType getType(VirtualMachine vm, ThreadReference tr, String name) {
        List<ReferenceType> classList = VirtualMachineWrapper.classesByName0(vm, name);
        if (!classList.isEmpty()) {
            return classList.iterator().next();
        }
        List<ReferenceType> classClassList = VirtualMachineWrapper.classesByName0(vm, "java.lang.Class"); // NOI18N
        if (classClassList.isEmpty()) {
            throw new IllegalStateException("Cannot load class Class"); // NOI18N
        }

        ClassType cls = (ClassType) classClassList.iterator().next();
        try {
            Method m = ClassTypeWrapper.concreteMethodByName(cls, "forName", "(Ljava/lang/String;)Ljava/lang/Class;"); // NOI18N
            StringReference mirrorOfName = VirtualMachineWrapper.mirrorOf(vm, name);
            ClassTypeWrapper.invokeMethod(cls, tr, m, Collections.singletonList(mirrorOfName), ObjectReference.INVOKE_SINGLE_THREADED);
            List<ReferenceType> classList2 = VirtualMachineWrapper.classesByName0(vm, name);
            if (!classList2.isEmpty()) {
                return classList2.iterator().next();
            }
        } catch (ClassNotLoadedException | ClassNotPreparedExceptionWrapper |
                 IncompatibleThreadStateException | InvalidTypeException |
                 InvocationException | InternalExceptionWrapper |
                 ObjectCollectedExceptionWrapper | UnsupportedOperationExceptionWrapper |
                 VMDisconnectedExceptionWrapper ex) {
            logger.log(Level.FINE, "Cannot load class " + name, ex); // NOI18N
        }
        
        return null;
    }
    
    public static class SGComponentInfo extends JavaComponentInfo {
        public SGComponentInfo(JPDAThreadImpl t, ObjectReference component) throws RetrievalException {
            super(t, component, ServiceType.FX);
            init();
        }

        @Override
        protected String getFieldName() {
            String fName = super.getFieldName();
            if (fName.isEmpty()) {
                return getName();
            }
            return fName;
        }
        
        @Override
        protected void retrieve() throws RetrievalException {
            VirtualMachine vm = getThread().getDebugger().getVirtualMachine();
            if (vm == null) {
                throw RetrievalException.disconnected();
            }
            ThreadReference tr = getThread().getThreadReference();
            ClassType compClass = (ClassType)getComponent().referenceType();
            try {
                if (compClass.name().equals("javafx.stage.Window") ||
                    compClass.name().equals("javafx.stage.Stage")) {
                    Method getTitle = compClass.concreteMethodByName("getTitle", "()Ljava/lang/String;");
                    if (getTitle != null) {
                        StringReference nameR = (StringReference)getComponent().invokeMethod(tr, getTitle, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                        setName(nameR != null ? nameR.value() : "");
                    }
                    Method getScene = compClass.concreteMethodByName("getScene", "()Ljavafx/scene/Scene;");
                    ObjectReference scene = (ObjectReference)getComponent().invokeMethod(tr, getScene, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                    ClassType sceneClass = (ClassType)scene.referenceType();
                    Method getX = sceneClass.concreteMethodByName("getX", "()D");
                    Method getY = sceneClass.concreteMethodByName("getY", "()D");
                    Method getWidth = sceneClass.concreteMethodByName("getWidth", "()D");
                    Method getHeight = sceneClass.concreteMethodByName("getHeight", "()D");
                    DoubleValue x = (DoubleValue) scene.invokeMethod(tr, getX, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                    DoubleValue y = (DoubleValue) scene.invokeMethod(tr, getY, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                    DoubleValue width = (DoubleValue) scene.invokeMethod(tr, getWidth, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                    DoubleValue height = (DoubleValue) scene.invokeMethod(tr, getHeight, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                    Rectangle b = new Rectangle(x.intValue(), y.intValue(), width.intValue(), height.intValue());
                    b.x = 0;
                    b.y = 0;
                    setWindowBounds(b);
                    setBounds(b);
                    
                    Method getRoot = sceneClass.concreteMethodByName("getRoot", "()Ljavafx/scene/Parent;");
                    ObjectReference root = (ObjectReference)scene.invokeMethod(tr, getRoot, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                    SGComponentInfo rootInfo = new SGComponentInfo(getThread(), root);
                    
                    setSubComponents(new JavaComponentInfo[]{
                        rootInfo
                    });
                } else {
                    Method getId = compClass.concreteMethodByName("getId", "()Ljava/lang/String;");
                    if (getId != null) {
                        StringReference id = (StringReference)getComponent().invokeMethod(tr, getId, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                        setName(id != null ? id.value() : ""); // NOI18N
                    }
//                    Method getRelBounds = compClass.concreteMethodByName("getBoundsInParent", "()Ljavafx/geometry/Bounds;");
                    Method getLocalBounds = compClass.concreteMethodByName("getBoundsInLocal", "()Ljavafx/geometry/Bounds;");
                    Method local2scene = compClass.concreteMethodByName("localToScene", "(Ljavafx/geometry/Bounds;)Ljavafx/geometry/Bounds;");
                    Method local2parent = compClass.concreteMethodByName("localToParent", "(Ljavafx/geometry/Bounds;)Ljavafx/geometry/Bounds;");
//                    ObjectReference relBounds = (ObjectReference)getComponent().invokeMethod(tr, getRelBounds, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                    ObjectReference locBounds = (ObjectReference)getComponent().invokeMethod(tr, getLocalBounds, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                    ObjectReference relBounds = (ObjectReference)getComponent().invokeMethod(tr, local2parent, Arrays.asList(locBounds), ObjectReference.INVOKE_SINGLE_THREADED);
                    ObjectReference absBounds = (ObjectReference)getComponent().invokeMethod(tr, local2scene, Arrays.asList(locBounds), ObjectReference.INVOKE_SINGLE_THREADED);
                    
                    setBounds(convertBounds(relBounds));
                    setWindowBounds(convertBounds(absBounds));
                    
                    Field children = compClass.fieldByName("children");
                    if (children != null) {
                        ObjectReference childrenList = (ObjectReference)getComponent().getValue(children);
                        ClassType listClass = (ClassType)childrenList.referenceType();
                        Method size = listClass.concreteMethodByName("size", "()I");
                        Method get = listClass.concreteMethodByName("get", "(I)Ljava/lang/Object;");
                        int cnt = ((IntegerValue)childrenList.invokeMethod(tr, size, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED)).intValue();
                        JavaComponentInfo[] cs = new JavaComponentInfo[cnt];
                        for(int i=0;i<cnt;i++) {
                            ObjectReference sub = (ObjectReference)childrenList.invokeMethod(tr, get, Arrays.asList(vm.mirrorOf(i)), ObjectReference.INVOKE_SINGLE_THREADED);
                            cs[i] = new SGComponentInfo(getThread(), sub);
                        }
                        setSubComponents(cs);
                    }
                }
            } catch (InvalidTypeException e) {
            } catch (ClassNotLoadedException e) {
            } catch (IncompatibleThreadStateException e) {
            } catch (InvocationException e) {
            }
        }
        
        private static Rectangle convertBounds(ObjectReference bounds) {
            ClassType boundsClass = (ClassType)bounds.referenceType();
            Field minX = boundsClass.fieldByName("minX");
            Field minY = boundsClass.fieldByName("minY");
            Field width = boundsClass.fieldByName("width");
            Field height = boundsClass.fieldByName("height");

            return new Rectangle(((DoubleValue)bounds.getValue(minX)).intValue(), 
               ((DoubleValue)bounds.getValue(minY)).intValue(), 
               ((DoubleValue)bounds.getValue(width)).intValue(), 
               ((DoubleValue)bounds.getValue(height)).intValue()
            );
        }
                
        @Override
        public Action[] getActions(boolean context) {
            FieldInfo fieldInfo = getField();
            ObjectReference component = getComponent();
            ComponentBreakpoint b = ComponentBreakpointActionProvider.findBreakpoint(component);
            
            List<Action> actions = new ArrayList<Action>();
            if (fieldInfo != null) {
                actions.add(GoToFieldDeclarationAction.get(GoToFieldDeclarationAction.class));
            }
            actions.add(GoToSourceAction.get(GoToSourceAction.class));
            if (getAddCallStack() != null) {
                actions.add(GoToAddIntoHierarchyAction.get(GoToAddIntoHierarchyAction.class));
            }
//            actions.add(null);
//            actions.add(ShowListenersAction.get(ShowListenersAction.class));
            actions.add(null);
            actions.add(ToggleComponentBreakpointAction.get(ToggleComponentBreakpointAction.class));
            if (b != null) {
                actions.add(CBP_CUSTOMIZE_ACTION);
            }
            return actions.toArray(new Action[] {});
        }
        
        @Override
        public ComponentInfo findAt(int x, int y) {
            Rectangle bounds = getWindowBounds();
            if (!bounds.contains(x, y)) {
                return null;
            }

            ComponentInfo[] subComponents = getSubComponents();
            if (subComponents != null) {
                Rectangle tempRect = null;
                ComponentInfo tempRslt = null;
                for (int i = 0; i < subComponents.length; i++) {
                    Rectangle sb = subComponents[i].getWindowBounds();
                    if (sb.contains(x, y)) {
                        tempRect = sb;
                        tempRslt = subComponents[i];
                        ComponentInfo tci = subComponents[i].findAt(x, y);
                        if (tci != null) {
                            Rectangle tbounds = tci.getWindowBounds();
                            if (tempRect.intersects(tbounds)) {
                                tempRect = tbounds;
                                tempRslt = tci;
                            }
                        }
                    }
                }
                return tempRslt;
            }
            return this;
        }
        
        private static final Action CBP_CUSTOMIZE_ACTION = new NodeAction() {

            @Override
            public String getName() {
                return NbBundle.getBundle(RemoteFXScreenshot.class).getString("CTL_Component_Breakpoint_Customize_Label");
            }

            @Override
            protected boolean enable(Node[] activatedNodes) {
                return true;
            }

            @Override
            protected boolean asynchronous() {
                return false;
            }
            
            @Override
            protected void performAction(Node[] activatedNodes) {
                for (Node n : activatedNodes) {
                    JavaComponentInfo ci = n.getLookup().lookup(JavaComponentInfo.class);
                    if (ci != null) {
                        ObjectReference component = ci.getComponent();
                        ComponentBreakpoint b = ComponentBreakpointActionProvider.findBreakpoint(component);
                        ComponentBreakpointsActionsProvider.customize(b);
                    }
                }
            }

            @Override
            public HelpCtx getHelpCtx() {
                return new HelpCtx("ComponentBreakpoint_Customize");
            }
            
        };
    }
}
