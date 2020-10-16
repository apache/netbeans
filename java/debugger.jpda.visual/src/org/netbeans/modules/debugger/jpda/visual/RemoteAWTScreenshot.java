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
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerValue;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.EvaluatorVisitor;
import org.netbeans.modules.debugger.jpda.expr.InvocationExceptionTranslated;
import org.netbeans.modules.debugger.jpda.jdi.ArrayReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.StringReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.visual.RemoteServices.ServiceType;
import org.netbeans.modules.debugger.jpda.visual.actions.ComponentBreakpointActionProvider;
import org.netbeans.modules.debugger.jpda.visual.actions.CreateFixedWatchAction;
import org.netbeans.modules.debugger.jpda.visual.actions.GoToAddIntoHierarchyAction;
import org.netbeans.modules.debugger.jpda.visual.actions.GoToFieldDeclarationAction;
import org.netbeans.modules.debugger.jpda.visual.actions.GoToSourceAction;
import org.netbeans.modules.debugger.jpda.visual.actions.ShowListenersAction;
import org.netbeans.modules.debugger.jpda.visual.actions.ToggleComponentBreakpointAction;
import org.netbeans.modules.debugger.jpda.visual.breakpoints.ComponentBreakpoint;
import org.netbeans.modules.debugger.jpda.visual.models.ComponentBreakpointsActionsProvider;
import org.netbeans.modules.debugger.jpda.visual.spi.ComponentInfo;
import org.netbeans.modules.debugger.jpda.visual.spi.RemoteScreenshot;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Takes screenshot of a remote application.
 * 
 * @author Martin Entlicher
 */
public class RemoteAWTScreenshot {
    
    private static final Logger logger = Logger.getLogger(RemoteAWTScreenshot.class.getName());
    
    static final String AWTThreadName = "AWT-EventQueue-";  // NOI18N
    
    private static final RemoteScreenshot[] NO_SCREENSHOTS = new RemoteScreenshot[] {};
    
    static final boolean FAST_SNAPSHOT_RETRIEVAL = getBooleanProperty("visualDebugger.fastSnapshot", true);   // NOI18N
    static final boolean FAST_FIELDS_SEARCH = getBooleanProperty("visualDebugger.fastFieldsSearch", true);   // NOI18N
    private static final char STRING_DELIMITER = (char) 3;   // ETX (end of text)
    
    public static boolean getBooleanProperty(String name, boolean defaultValue) {
        boolean result = defaultValue;
        String val = System.getProperty(name);
        if (val != null) {
            result = Boolean.parseBoolean(val);
        }
        return result;
    }
    

    private RemoteAWTScreenshot() {}
    
    private static RemoteScreenshot createRemoteAWTScreenshot(DebuggerEngine engine, String title, int width, int height, int[] dataArray, AWTComponentInfo componentInfo) {
        final BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        WritableRaster raster = bi.getRaster();
        raster.setDataElements(0, 0, width, height, dataArray);
        if (FAST_FIELDS_SEARCH) {
            ComponentsFieldFinder.findFieldsForComponents(componentInfo);
        }
        return new RemoteScreenshot(engine, title, width, height, bi, componentInfo);
    }
    
    public static RemoteScreenshot[] takeCurrent() throws RetrievalException {
        DebuggerEngine engine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (engine != null) {
            JPDADebugger debugger = engine.lookupFirst(null, JPDADebugger.class);
            logger.fine("Debugger = "+debugger);
            if (debugger != null) {
                return takeCurrent(debugger, engine);
            }
        }
        return NO_SCREENSHOTS;
    }

    public static RemoteScreenshot[] takeCurrent(JPDADebugger debugger) throws RetrievalException {
        DebuggerEngine engine = ((JPDADebuggerImpl) debugger).getSession().getCurrentEngine();
        return takeCurrent(debugger, engine);
    }
    
    private static RemoteScreenshot[] takeCurrent(JPDADebugger debugger, DebuggerEngine engine) throws RetrievalException {
        List<JPDAThread> allThreads = debugger.getThreadsCollector().getAllThreads();
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Threads = {0}", allThreads);
        }
        RemoteScreenshot[] rs = NO_SCREENSHOTS;
        for (JPDAThread t : allThreads) {
            if (t.getName().startsWith(AWTThreadName)) {
                long t1 = System.nanoTime();
                try {
                    RemoteScreenshot[] rst = take(t, engine);
                    if (rst.length > 0) {
                        if (rs.length > 0) {
                            RemoteScreenshot[] nrs = new RemoteScreenshot[rs.length + rst.length];
                            System.arraycopy(rs, 0, nrs, 0, rs.length);
                            System.arraycopy(rst, 0, nrs, rs.length, rst.length);
                            rs = nrs;
                        } else {
                            rs = rst;
                        }
                    }
                    /*
                } catch (InvocationException iex) {
                    //ObjectReference exception = iex.exception();
                    Exceptions.printStackTrace(iex);

                    final InvocationExceptionTranslated iextr = new InvocationExceptionTranslated(iex, (JPDADebuggerImpl) debugger);
                    RequestProcessor.getDefault().post(new Runnable() {
                        @Override
                        public void run() {
                            iextr.getMessage();
                            iextr.getLocalizedMessage();
                            iextr.getCause();
                            iextr.getStackTrace();
                            Exceptions.printStackTrace(iextr);
                        }
                    }, 100);

                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
                     */
                //break;
                } finally {
                    long t2 = System.nanoTime();
                    long ns = t2 - t1;
                    long ms = ns/1000000;
                    logger.info("GUI Snaphot taken in "+((ms > 0) ? (ms + " ms "+(ns - ms*1000000)+" ns.") : (ns+" ns.")));
                }
            }
        }
        return rs;
    }
    
    public static RemoteScreenshot[] take(final JPDAThread t, final DebuggerEngine engine) throws RetrievalException {//throws ClassNotLoadedException, IncompatibleThreadStateException, InvalidTypeException, InvocationException {
        //RemoteScreenshot[] screenshots = NO_SCREENSHOTS;
        final ThreadReference tawt = ((JPDAThreadImpl) t).getThreadReference();
        final VirtualMachine vm = tawt.virtualMachine();
        final ClassType windowClass;
        try {
            windowClass = RemoteServices.getClass(vm, "java.awt.Window");
        } catch (InternalExceptionWrapper ex) {
            return NO_SCREENSHOTS;
        } catch (VMDisconnectedExceptionWrapper ex) {
            return NO_SCREENSHOTS;
        } catch (ObjectCollectedExceptionWrapper ex) {
            return NO_SCREENSHOTS;
        }
        if (windowClass == null) {
            logger.fine("No Window");
            return NO_SCREENSHOTS;
        }

        final JPDADebugger debugger = ((JPDAThreadImpl) t).getDebugger();
        ClassObjectReference serviceClassObject = RemoteServices.getServiceClass(debugger, ServiceType.AWT);
        final ClassType serviceClass;
        try {
            serviceClass = (ClassType) ClassObjectReferenceWrapper.reflectedType(serviceClassObject);
        } catch (InternalExceptionWrapper | ObjectCollectedExceptionWrapper | VMDisconnectedExceptionWrapper ex) {
            return NO_SCREENSHOTS;
        }
        final List<RemoteScreenshot> screenshots = new ArrayList<RemoteScreenshot>();
        final RetrievalException[] retrievalExceptionPtr = new RetrievalException[] { null };
        try {
            RemoteServices.runOnStoppedThread(t, new Runnable() {
                @Override
                public void run() {
                    logger.fine("RemoteScreenshot.take("+t+")");
                    /*
                     * Run following code in the target VM:
                       Window[] windows = Window.getWindows();
                       for (Window w : windows) {
                           if (!w.isVisible()) {
                               continue;
                           }
                           Dimension d = w.getSize();
                           BufferedImage bi = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
                           Graphics g = bi.createGraphics();
                           w.paint(g);
                           Raster raster = bi.getData();
                           Object data = raster.getDataElements(0, 0, d.width, d.height, null);
                       }
                     */
                    try {
                        if (FAST_SNAPSHOT_RETRIEVAL) {
                            final Method getGUISnapshots = ClassTypeWrapper.concreteMethodByName(serviceClass, "getGUISnapshots", "()[Lorg/netbeans/modules/debugger/jpda/visual/remote/RemoteAWTService$Snapshot;");
                            ArrayReference snapshotsArray = (ArrayReference) ClassTypeWrapper.invokeMethod(serviceClass, tawt, getGUISnapshots, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                            List<Value> snapshots = ArrayReferenceWrapper.getValues(snapshotsArray);
                            for (Value snapshot : snapshots) {
                                ObjectReference snapshotObj = (ObjectReference) snapshot;
                                ReferenceType rt = ObjectReferenceWrapper.referenceType(snapshotObj);
                                StringReference allIntDataString = (StringReference) ObjectReferenceWrapper.getValue(snapshotObj, ReferenceTypeWrapper.fieldByName(rt, "allIntDataString"));
                                int[] allIntData = createIntArrayFromString(StringReferenceWrapper.value(allIntDataString));
                                StringReference allNamesString = (StringReference) ObjectReferenceWrapper.getValue(snapshotObj, ReferenceTypeWrapper.fieldByName(rt, "allNamesString"));
                                ArrayReference allComponentsArray = (ArrayReference) ObjectReferenceWrapper.getValue(snapshotObj, ReferenceTypeWrapper.fieldByName(rt, "allComponentsArray"));
                                StringReference componentsAddAtString = (StringReference) ObjectReferenceWrapper.getValue(snapshotObj, ReferenceTypeWrapper.fieldByName(rt, "componentsAddAt"));
                                String allNames = StringReferenceWrapper.value(allNamesString);
                                String componentsAddAt = StringReferenceWrapper.value(componentsAddAtString);
                                int ititle = allNames.indexOf(STRING_DELIMITER);
                                String title = new String(allNames.substring(0, ititle)).intern(); // Create a new String - do not hold the whole allNames.
                                if (title.length() == 1 && title.charAt(0) == 0) {
                                    title = null;
                                }
                                int ndata = allIntData[2];
                                int[] dataArray = new int[ndata];
                                System.arraycopy(allIntData, 3, dataArray, 0, ndata);
                                AWTComponentInfo componentInfo = new AWTComponentInfo((JPDAThreadImpl) t,
                                                                                      allIntData, new int[] { ndata + 3 },
                                                                                      allNames, new int[] { ititle + 1 },
                                                                                      allComponentsArray.getValues(), new int[] { 0 },
                                                                                      componentsAddAt, new int[] { 0 });
                                screenshots.add(createRemoteAWTScreenshot(engine,
                                                                          title,
                                                                          allIntData[0], allIntData[1],
                                                                          dataArray, componentInfo));
                            }
                            // Clear the snapshots reference to enable GC.
                            ClassTypeWrapper.setValue(serviceClass, ReferenceTypeWrapper.fieldByName(serviceClass, "lastGUISnapshots"), null);
                            return;
                        }
                        
                        //Method getWindows = null;//windowClass.concreteMethodByName("getOwnerlessWindows", "()[Ljava/awt/Window;");
                        final Method getWindows = windowClass.concreteMethodByName("getWindows", "()[Ljava/awt/Window;");
                        if (getWindows == null) {
                            logger.fine("No getWindows() method!");
                            String msg = NbBundle.getMessage(RemoteAWTScreenshot.class, "MSG_ScreenshotNotTaken_MissingMethod", "java.awt.Window.getWindows()");
                            throw new RetrievalException(msg);
                        }
                        ArrayReference windowsArray = (ArrayReference) ((ClassType) windowClass).invokeMethod(tawt, getWindows, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                        List<Value> windows = windowsArray.getValues();
                        logger.fine("Have "+windows.size()+" window(s).");

                        Method isVisible = windowClass.concreteMethodByName("isVisible", "()Z");
                        if (isVisible == null) {
                            logger.fine("No isVisible() method!");
                            String msg = NbBundle.getMessage(RemoteAWTScreenshot.class, "MSG_ScreenshotNotTaken_MissingMethod", "java.awt.Window.isVisible()");
                            throw new RetrievalException(msg);
                        }
                        Method getOwner = windowClass.concreteMethodByName("getOwner", "()Ljava/awt/Window;");
                        if (getOwner == null) {
                            logger.fine("No getOwner() method!");
                            String msg = NbBundle.getMessage(RemoteAWTScreenshot.class, "MSG_ScreenshotNotTaken_MissingMethod", "java.awt.Window.getOwner()");
                            throw new RetrievalException(msg);
                        }
                        Method getSize = windowClass.concreteMethodByName("getSize", "()Ljava/awt/Dimension;");
                        if (getSize == null) {
                            logger.fine("No getSize() method!");
                            String msg = NbBundle.getMessage(RemoteAWTScreenshot.class, "MSG_ScreenshotNotTaken_MissingMethod", "java.awt.Window.getSize()");
                            throw new RetrievalException(msg);
                        }
                        ClassType dimensionClass = RemoteServices.getClass(vm, "java.awt.Dimension");
                        if (dimensionClass == null) {
                            logger.fine("No Dimension");
                            String msg = NbBundle.getMessage(RemoteAWTScreenshot.class, "MSG_ScreenshotNotTaken_MissingClass", "java.awt.Dimension");
                            throw new RetrievalException(msg);
                        }
                        ClassType bufferedImageClass = RemoteServices.getClass(vm, "java.awt.image.BufferedImage");
                        if (bufferedImageClass == null) {
                            logger.fine("No BufferedImage class.");
                            String msg = NbBundle.getMessage(RemoteAWTScreenshot.class, "MSG_ScreenshotNotTaken_MissingClass", "java.awt.image.BufferedImage");
                            throw new RetrievalException(msg);
                        }
                        Method bufferedImageConstructor = bufferedImageClass.concreteMethodByName("<init>", "(III)V");
                        Method createGraphics = bufferedImageClass.concreteMethodByName("createGraphics", "()Ljava/awt/Graphics2D;");
                        if (createGraphics == null) {
                            logger.fine("createGraphics() method is not found!");
                            String msg = NbBundle.getMessage(RemoteAWTScreenshot.class, "MSG_ScreenshotNotTaken_MissingMethod", "java.awt.image.BufferedImage.createGraphics()");
                            throw new RetrievalException(msg);
                        }

                        ClassType frameClass = RemoteServices.getClass(vm, "java.awt.Frame");
                        Method getFrameTitle = null;
                        if (frameClass != null) {
                            getFrameTitle = frameClass.concreteMethodByName("getTitle", "()Ljava/lang/String;");
                        }
                        ClassType dialogClass = RemoteServices.getClass(vm, "java.awt.Dialog");
                        Method getDialogTitle = null;
                        if (dialogClass != null) {
                            getDialogTitle = dialogClass.concreteMethodByName("getTitle", "()Ljava/lang/String;");
                        }

                        for (Value windowValue : windows) {
                            ObjectReference window = (ObjectReference) windowValue;
                            //dumpHierarchy(window);

                            BooleanValue visible = (BooleanValue) window.invokeMethod(tawt, isVisible, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                            if (!visible.value()) {
                                // Ignore windows that are not visible.
                                // TODO: mark them as not visible.
                                //continue;
                            }
                            Object owner = window.invokeMethod(tawt, getOwner, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                            if (owner != null) {
                                // An owned window
                                //continue;
                            }

                            ObjectReference sizeDimension = (ObjectReference) window.invokeMethod(tawt, getSize, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                            Field field = dimensionClass.fieldByName("width");
                            IntegerValue widthValue = (IntegerValue) sizeDimension.getValue(field);
                            int width = widthValue.value();
                            field = dimensionClass.fieldByName("height");
                            IntegerValue heightValue = (IntegerValue) sizeDimension.getValue(field);
                            int height = heightValue.value();
                            logger.log(Level.FINE, "The size is {0} x {1}", new Object[]{width, height});

                            List<? extends Value> args = Arrays.asList(widthValue, heightValue, vm.mirrorOf(BufferedImage.TYPE_INT_ARGB));
                            ObjectReference bufferedImage = bufferedImageClass.newInstance(tawt, bufferedImageConstructor, args, ObjectReference.INVOKE_SINGLE_THREADED);
                            ObjectReference graphics = (ObjectReference) bufferedImage.invokeMethod(tawt, createGraphics, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);


                            Method paint = windowClass.concreteMethodByName("paint", "(Ljava/awt/Graphics;)V");
                            window.invokeMethod(tawt, paint, Arrays.asList(graphics), ObjectReference.INVOKE_SINGLE_THREADED);

                            /*
                            // getPeer() - java.awt.peer.ComponentPeer, ComponentPeer.paint()
                            Method getPeer = windowClass.concreteMethodByName("getPeer", "()Ljava/awt/peer/ComponentPeer;");
                            ObjectReference peer = (ObjectReference) window.invokeMethod(tawt, getPeer, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                            Method paint = ((ClassType) peer.referenceType()).concreteMethodByName("paint", "(Ljava/awt/Graphics;)V");
                            peer.invokeMethod(tawt, paint, Arrays.asList(graphics), ObjectReference.INVOKE_SINGLE_THREADED);
                            - paints nothing! */

                            Method getData = bufferedImageClass.concreteMethodByName("getData", "()Ljava/awt/image/Raster;");
                            ObjectReference raster = (ObjectReference) bufferedImage.invokeMethod(tawt, getData, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);

                            Method getDataElements = ((ClassType) raster.referenceType()).concreteMethodByName("getDataElements", "(IIIILjava/lang/Object;)Ljava/lang/Object;");
                            IntegerValue zero = vm.mirrorOf(0);
                            ArrayReference data = (ArrayReference) raster.invokeMethod(tawt, getDataElements, Arrays.asList(zero, zero, widthValue, heightValue, null), ObjectReference.INVOKE_SINGLE_THREADED);

                            logger.log(Level.FINE, "Image data length = {0}", data.length());

                            List<Value> dataValues = data.getValues();
                            int[] dataArray = new int[data.length()];
                            int i = 0;
                            for (Value v : dataValues) {
                                dataArray[i++] = ((IntegerValue) v).value();
                            }

                            String title = null;
                            if (frameClass != null && EvaluatorVisitor.instanceOf(window.referenceType(), frameClass)) {
                                Value v = window.invokeMethod(tawt, getFrameTitle, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                                if (v instanceof StringReference) {
                                    StringReference sr = (StringReference) v;
                                    title = sr.value();
                                }
                            }
                            if (dialogClass != null && EvaluatorVisitor.instanceOf(window.referenceType(), dialogClass)) {
                                Value v = window.invokeMethod(tawt, getDialogTitle, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                                if (v instanceof StringReference) {
                                    StringReference sr = (StringReference) v;
                                    title = sr.value();
                                }
                            }

                            AWTComponentInfo componentInfo = new AWTComponentInfo((JPDAThreadImpl) t, window);
                            screenshots.add(createRemoteAWTScreenshot(engine, title, width, height, dataArray, componentInfo));
                        }
                    } catch (RetrievalException rex) {
                        retrievalExceptionPtr[0] = rex;
                    } catch (InvocationException iex) {
                        //Exceptions.printStackTrace(iex);
                        ((JPDAThreadImpl) t).notifyMethodInvokeDone();
                        final InvocationExceptionTranslated iextr = new InvocationExceptionTranslated(iex, (JPDADebuggerImpl) debugger);
                        // Initialize the translated exception:
                        iextr.setPreferredThread((JPDAThreadImpl) t);
                        iextr.getMessage();
                        iextr.getLocalizedMessage();
                        iextr.getCause();
                        iextr.getStackTrace();
                        logger.log(Level.INFO, iex.getMessage(), iextr);
                        retrievalExceptionPtr[0] =  new RetrievalException(iex.getMessage(), iextr);
                    } catch (InvalidTypeException itex) {
                        retrievalExceptionPtr[0] = new RetrievalException(itex.getMessage(), itex);
                    } catch (ClassNotLoadedException cnlex) {
                        return ;//NO_SCREENSHOTS;
                    } catch (IncompatibleThreadStateException itsex) {
                        retrievalExceptionPtr[0] =  new RetrievalException(itsex.getMessage(), itsex);
                    } catch (ClassNotPreparedExceptionWrapper cnpex) {
                        return ;//NO_SCREENSHOTS;
                    } catch (InternalExceptionWrapper iex) {
                        return ;//NO_SCREENSHOTS;
                    } catch (ObjectCollectedExceptionWrapper ocex) {
                        Exceptions.printStackTrace(ocex);
                        return ;
                    } catch (VMDisconnectedExceptionWrapper vmdex) {
                        return ;
                    }
                }

            }, RemoteServices.ServiceType.AWT);
            
        } catch (PropertyVetoException pvex) {
            // Can not invoke methods
            throw new RetrievalException(pvex.getMessage(), pvex);
        }
        if (retrievalExceptionPtr[0] != null) {
            throw retrievalExceptionPtr[0];
        }
        return screenshots.toArray(new RemoteScreenshot[] {});
    }
    
    private static void retrieveComponents(final AWTComponentInfo ci, JPDAThreadImpl t, VirtualMachine vm,
                                           ClassType componentClass, ClassType containerClass, ObjectReference component,
                                           Method getComponents, Method getBounds,
                                           int shiftx, int shifty)
                                           throws InvalidTypeException, ClassNotLoadedException,
                                                  IncompatibleThreadStateException, InvocationException,
                                                  RetrievalException {
        
        ThreadReference tawt = t.getThreadReference();
        ObjectReference rectangle = (ObjectReference) component.invokeMethod(tawt, getBounds, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
        ClassType rectangleClass;
        try {
            rectangleClass = RemoteServices.getClass(vm, "java.awt.Rectangle");
        } catch (InternalExceptionWrapper ex) {
            return ;
        } catch (VMDisconnectedExceptionWrapper ex) {
            return ;
        } catch (ObjectCollectedExceptionWrapper ex) {
            return ;
        }
        Field fx = rectangleClass.fieldByName("x");
        Field fy = rectangleClass.fieldByName("y");
        Field fwidth = rectangleClass.fieldByName("width");
        Field fheight = rectangleClass.fieldByName("height");
        Map<Field, Value> rvalues = rectangle.getValues(Arrays.asList(new Field[] {fx, fy, fwidth, fheight}));
        Rectangle r = new Rectangle();
        r.x = ((IntegerValue) rvalues.get(fx)).value();
        r.y = ((IntegerValue) rvalues.get(fy)).value();
        r.width = ((IntegerValue) rvalues.get(fwidth)).value();
        r.height = ((IntegerValue) rvalues.get(fheight)).value();
        ci.setBounds(r);
        if (shiftx == Integer.MIN_VALUE && shifty == Integer.MIN_VALUE) {
            shiftx = shifty = 0; // Do not shift the window as such
        } else {
            shiftx += r.x;
            shifty += r.y;
            ci.setWindowBounds(new Rectangle(shiftx, shifty, r.width, r.height));
        }
        Method getName = componentClass.concreteMethodByName("getName", "()Ljava/lang/String;");
        StringReference name = (StringReference) component.invokeMethod(tawt, getName, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
        if (name != null) {
            ci.setName(name.value());
        }
        ci.setComponent(component);
        ci.setType(component.referenceType().name());
        logger.log(Level.FINE, "  Component ''{0}'' class=''{1}'' bounds = {2}", new Object[]{ci.getName(), ci.getType(), r});
        
//        ci.addPropertySet(new PropertySet("main", "Main", "The main properties") {
//            @Override
//            public Property<?>[] getProperties() {
//                return new Property[] {
//                    new ReadOnly("name", String.class, "Component Name", "The name of the component") {
//                        @Override
//                        public Object getValue() throws IllegalAccessException, InvocationTargetException {
//                            return ci.getName();
//                        }
//                    },
//                    new ReadOnly("type", String.class, "Component Type", "The type of the component") {
//                        @Override
//                        public Object getValue() throws IllegalAccessException, InvocationTargetException {
//                            return ci.getType();
//                        }
//                    },
//                    new ReadOnly("bounds", String.class, "Component Bounds", "The bounds of the component in the window.") {
//                        @Override
//                        public Object getValue() throws IllegalAccessException, InvocationTargetException {
//                            Rectangle r = ci.getWindowBounds();
//                            return "[x=" + r.x + ",y=" + r.y + ",width=" + r.width + ",height=" + r.height + "]";
//                        }
//                    },
//                };
//            }
//        });
        
        if (isInstanceOfClass((ClassType) component.referenceType(), containerClass)) {
            ArrayReference componentsArray = (ArrayReference) component.invokeMethod(tawt, getComponents, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
            List<Value> components = componentsArray.getValues();
            logger.log(Level.FINE, "Have {0} component(s).", components.size());
            if (components.size() > 0) {
                AWTComponentInfo[] cis = new AWTComponentInfo[components.size()];
                int i = 0;
                for(Value cv : components) {
                    ObjectReference c = (ObjectReference) cv;
                    cis[i] = new AWTComponentInfo(t, c, shiftx, shifty);
                    i++;
                }
                ci.setSubComponents(cis);
            }
        }
    }
    
    /*
    private static AWTComponentInfo[] retrieveComponents(ThreadReference tawt, VirtualMachine vm,
                                                      ClassType containerClass, ObjectReference window,
                                                      Method getBounds)
                                                      throws InvalidTypeException, ClassNotLoadedException,
                                                             IncompatibleThreadStateException, InvocationException,
                                                             RetrievalException {
        Method getComponents = containerClass.concreteMethodByName("getComponents", "()[Ljava/awt/Component;");
        if (getComponents == null) {
            logger.severe("No getComponents() method!");
            return new AWTComponentInfo[] {};
        }
        ArrayReference componentsArray = (ArrayReference) window.invokeMethod(tawt, getComponents, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
        List<Value> components = componentsArray.getValues();
        logger.severe("Have "+components.size()+" component(s).");
        
        AWTComponentInfo[] cis = new AWTComponentInfo[components.size()];
        int i = 0;
        for(Value cv : components) {
            cis[i] = new AWTComponentInfo();
            ObjectReference c = (ObjectReference) cv;
            ObjectReference rectangle = (ObjectReference) c.invokeMethod(tawt, getBounds, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
            ClassType rectangleClass = getClass(vm, "java.awt.Rectangle");
            Field fx = rectangleClass.fieldByName("x");
            Field fy = rectangleClass.fieldByName("y");
            Field fwidth = rectangleClass.fieldByName("width");
            Field fheight = rectangleClass.fieldByName("height");
            Map<Field, Value> rvalues = rectangle.getValues(Arrays.asList(new Field[] {fx, fy, fwidth, fheight}));
            Rectangle r = new Rectangle();
            r.x = ((IntegerValue) rvalues.get(fx)).value();
            r.y = ((IntegerValue) rvalues.get(fy)).value();
            r.width = ((IntegerValue) rvalues.get(fwidth)).value();
            r.height = ((IntegerValue) rvalues.get(fheight)).value();
            cis[i].bounds = r;
            logger.severe("  Component "+i+": bounds = "+r);
            
            if (isInstanceOfClass((ClassType) c.referenceType(), containerClass)) {
                cis[i].subComponents = retrieveComponents(tawt, vm, containerClass, c, getBounds);
            }
            
            i++;
        }
        return cis;
    }
    */
    
    private static int[] createIntArrayFromString(String s) {
        int i1 = 0;
        int i2 = s.indexOf('[');
        int n = Integer.parseInt(s.substring(i1, i2));
        int[] array = new int[n];
        for (int i = 0; i < n; i++) {
            i1 = i2 + 1;
            i2 = s.indexOf(',', i1);
            if (i2 < 0) {
                i2 = s.indexOf(']', i1);
            }
            array[i] = Integer.parseInt(s.substring(i1, i2));
        }
        return array;
    }
    
    private static boolean isInstanceOfClass(ClassType c1, ClassType c2) {
        if (c1.equals(c2)) {
            return true;
        }
        c1 = c1.superclass();
        if (c1 == null) {
            return false;
        }
        return isInstanceOfClass(c1, c2);
    }
    
    public static class AWTComponentInfo extends JavaComponentInfo {
        private int shiftX, shiftY;
        private boolean visible;
        private VirtualMachine vm;
        private ClassType containerClass, componentClass;
        private Method getBounds, getComponents;
        private String addAtStr;
        private Stack addAt;
        
        public AWTComponentInfo(JPDAThreadImpl t, ObjectReference component) throws RetrievalException {
            this(t, component, Integer.MIN_VALUE, Integer.MIN_VALUE);
        }
        
        public AWTComponentInfo(JPDAThreadImpl t, ObjectReference component, int shiftX, int shiftY) throws RetrievalException {
            super(t, component, ServiceType.AWT);
            
            vm = getThread().getDebugger().getVirtualMachine();
            if (vm == null) {
                throw RetrievalException.disconnected();
            }
            try {
                containerClass = RemoteServices.getClass(vm, "java.awt.Container");
                componentClass = RemoteServices.getClass(vm, "java.awt.Component");
            } catch (InternalExceptionWrapper ex) {
                throw new RetrievalException(ex.getLocalizedMessage(), ex);
            } catch (VMDisconnectedExceptionWrapper ex) {
                throw new RetrievalException(ex.getLocalizedMessage(), ex);
            } catch (ObjectCollectedExceptionWrapper ex) {
                throw new RetrievalException(ex.getLocalizedMessage(), ex);
            }
            getBounds = componentClass.concreteMethodByName("getBounds", "()Ljava/awt/Rectangle;");
            getComponents = containerClass.concreteMethodByName("getComponents", "()[Ljava/awt/Component;");
            if (getComponents == null) {
                logger.fine("No getComponents() method!");
                String msg = NbBundle.getMessage(RemoteAWTScreenshot.class, "MSG_ScreenshotNotTaken_MissingMethod", "java.awt.Container.getComponents()");
                throw new RetrievalException(msg);
            }
            
            this.shiftX = shiftX;
            this.shiftY = shiftY;
            this.visible = true;
            init();
        }
        
        public AWTComponentInfo(JPDAThreadImpl t, int[] allDataArray, int[] dposPtr,
                                String allNames, int[] inamePtr,
                                List<Value> allComponentsArray, int[] cposPtr,
                                String componentsAddAt, int[] iaddPtr) throws RetrievalException {
//            this(t, allDataArray, dpos, allNames, allComponentsArray, 0, Integer.MIN_VALUE, Integer.MIN_VALUE);
//        }
//        
//        public AWTComponentInfo(JPDAThreadImpl t, int[] allDataArray, int dpos,
//                                String allNames, List<Value> allComponentsArray, int cpos,
//                                int shiftX, int shiftY) throws RetrievalException {
            super(t, (ObjectReference) allComponentsArray.get(cposPtr[0]++), ServiceType.AWT);
            //this.shiftX = shiftX;
            //this.shiftY = shiftY;
            Rectangle bounds = new Rectangle();
            int dpos = dposPtr[0];
            //if (shiftX == Integer.MIN_VALUE && shiftY == Integer.MIN_VALUE) {
            //    bounds.x = 0; // Move to the origin, we do not care where it's on the screen.
            //    bounds.y = 0;
            //    dpos += 2;
            //} else {
                bounds.x = allDataArray[dpos++];
                bounds.y = allDataArray[dpos++];
                bounds.width = allDataArray[dpos++];
                bounds.height = allDataArray[dpos++];
            //}
            setBounds(bounds);
            this.shiftX = allDataArray[dpos++];
            this.shiftY = allDataArray[dpos++];
            visible = (allDataArray[dpos++] == 0) ? false : true;
            setWindowBounds(new Rectangle(shiftX, shiftY, bounds.width, bounds.height));
            int iname = allNames.indexOf(STRING_DELIMITER, inamePtr[0]);
            String name = allNames.substring(inamePtr[0], iname);
            if (name.length() == 1 && name.charAt(0) == 0) {
                name = null;
            } else {
                name = new String(name).intern(); // Do not hold the whole original String
            }
            setName(name);
            inamePtr[0] = iname + 1;
            
            int iaddAt = componentsAddAt.indexOf(STRING_DELIMITER, iaddPtr[0]);
            String addAt = componentsAddAt.substring(iaddPtr[0], iaddAt);
            if ("null".equals(addAt)) {
                addAt = null;
            }
            setAddAt(addAt);
            iaddPtr[0] = iaddAt + 1;
            
            int nsc = allDataArray[dpos++];
            dposPtr[0] = dpos;
            if (nsc > 0) {
                AWTComponentInfo[] cis = new AWTComponentInfo[nsc];
                for (int i = 0; i < nsc; i++) {
                    cis[i] = new AWTComponentInfo(getThread(), allDataArray, dposPtr,
                                                  allNames, inamePtr,
                                                  allComponentsArray, cposPtr,
                                                  componentsAddAt, iaddPtr);
                }
                setSubComponents(cis);
            }
            init();
        }
        
        private void setAddAt(String addAtStr) {
            this.addAtStr = addAtStr;
        }

        @Override
        public Stack getAddCallStack() {
            if (!FAST_SNAPSHOT_RETRIEVAL) {
                return super.getAddCallStack();
            }
            if (addAtStr == null) {
                return null;
            }
            if (addAt == null) {
                Stack.Frame f = Stack.Frame.parseLine(addAtStr);
                Stack.Frame[] frames = new Stack.Frame[] { f };
                addAt = new Stack(frames);
            }
            return addAt;
        }
        
        public boolean isVisible() {
            return visible;
        }
        
        @Override
        public Action[] getActions(boolean context) {
            //FieldInfo fieldInfo = getField();
            ObjectReference component = getComponent();
            ComponentBreakpoint b = ComponentBreakpointActionProvider.findBreakpoint(component);
            
            List<Action> actions = new ArrayList<Action>();
            actions.add(GoToFieldDeclarationAction.get(GoToFieldDeclarationAction.class));
            actions.add(GoToSourceAction.get(GoToSourceAction.class));
            if (getAddCallStack() != null) {
                actions.add(GoToAddIntoHierarchyAction.get(GoToAddIntoHierarchyAction.class));
            }
            actions.add(null);
            actions.add(ShowListenersAction.get(ShowListenersAction.class));
            actions.add(CreateFixedWatchAction.get(CreateFixedWatchAction.class));
            actions.add(null);
            actions.add(ToggleComponentBreakpointAction.get(ToggleComponentBreakpointAction.class));
            if (b != null) {
                actions.add(CBP_CUSTOMIZE_ACTION);
            }
            actions.add(null);
            actions.add(PropertiesAction.get(PropertiesAction.class));
            return actions.toArray(new Action[] {});
        }

        @Override
        public ComponentInfo findAt(int x, int y) {
            Rectangle bounds = getBounds();
            if (!bounds.contains(x, y)) {
                return null;
            }
            x -= bounds.x;
            y -= bounds.y;
            ComponentInfo[] subComponents = getSubComponents();
            if (subComponents != null) {
                ComponentInfo invisible = null;
                for (int i = 0; i < subComponents.length; i++) {
                    Rectangle sb = subComponents[i].getBounds();
                    if (sb.contains(x, y)) {
                        if (subComponents[i] instanceof AWTComponentInfo &&
                            !((AWTComponentInfo) subComponents[i]).isVisible()) {
                            if (invisible == null) {
                                invisible = subComponents[i];
                            }
                            continue;
                        }
                        ComponentInfo tci = subComponents[i].findAt(x, y);
                        Rectangle tbounds = tci.getBounds();
                        if (tbounds.width < bounds.width || tbounds.height < bounds.height) {
                            return tci;
                        }
                    }
                }
                if (invisible != null) {
                    ComponentInfo tci = invisible.findAt(x, y);
                    Rectangle tbounds = tci.getBounds();
                    if (tbounds.width < bounds.width || tbounds.height < bounds.height) {
                        return tci;
                    }
                }
            }
            return this;
        }

        @Override
        protected void retrieve() throws RetrievalException {
            if (componentClass == null) {
                return ;
            }
            try {
                retrieveComponents(this, getThread(), vm, componentClass, containerClass, getComponent(), getComponents, getBounds,
                        shiftX, shiftY);
                if (shiftX == Integer.MIN_VALUE && shiftY == Integer.MIN_VALUE) {
                    getBounds().x = 0; // Move to the origin, we do not care where it's on the screen.
                    getBounds().y = 0;
                }
            } catch (RetrievalException e) {
                throw e;
            } catch (Exception e) {
                throw new RetrievalException(e.getMessage(), e);
            }
        }
        
        private static final Action CBP_CUSTOMIZE_ACTION = new NodeAction() {

            @Override
            public String getName() {
                return NbBundle.getMessage(RemoteAWTScreenshot.class, "CTL_Component_Breakpoint_Customize_Label");
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
                        if (b != null) {
                            ComponentBreakpointsActionsProvider.customize(b);
                        }
                    }
                }
            }

            @Override
            public HelpCtx getHelpCtx() {
                return new HelpCtx("AWTComponentBreakpoint_Customize");
            }
            
        };
        
    }
}
