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
package org.netbeans.modules.debugger.jpda.visual.remote;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingUtilities;

/**
 * This class provides the main access to remote services.
 * 
 * @author Martin Entlicher
 */
public class RemoteAWTService {
    
    private static final String AWTAccessThreadName = "org.netbeans.modules.debugger.jpda.visual AWT Access Loop";   // NOI18N
    private static volatile boolean awtAccess = false;
    private static volatile boolean awtAccessLoop = false;
    private static volatile AWTAccessLoop awtAccessLoopRunnable;
    private static volatile RemoteAWTHierarchyListener hierarchyListener;
    
    //private static final Map eventData = new HashMap();
    private static final List eventData = new ArrayList();
    
    public RemoteAWTService() {
    }
    
    static boolean startAccessLoop() {
        if (!awtAccessLoop) {
            Thread loop;
            AWTAccessLoop accessLoop;
            try {
                accessLoop = new AWTAccessLoop();
                loop = new Thread(accessLoop, AWTAccessThreadName);
                loop.setDaemon(true);
                loop.setPriority(Thread.MIN_PRIORITY);
            } catch (SecurityException se) {
                return false;
            }
            awtAccessLoopRunnable = accessLoop;
            awtAccessLoop = true;
            loop.start();
        }
        return true;
    }
    
    static void stopAccessLoop() {
        awtAccessLoop = false;
        awtAccessLoopRunnable = null;
        lastGUISnapshots = null;
        preferredEventThread = null;
    }
    
    static String startHierarchyListener() {
        if (hierarchyListener == null) {
            hierarchyListener = new RemoteAWTHierarchyListener();
            try {
                Toolkit.getDefaultToolkit().addAWTEventListener(hierarchyListener, AWTEvent.HIERARCHY_EVENT_MASK);
            } catch (SecurityException se) {
                hierarchyListener = null;
                return "Toolkit.addAWTEventListener() threw "+se.toString();
            }
        }
        return null;
    }
    
    static void stopHierarchyListener() {
        if (hierarchyListener != null) {
            Toolkit.getDefaultToolkit().removeAWTEventListener(hierarchyListener);
            hierarchyListener = null;
        }
    }
    
    static String getHierarchyChangeStackFor(Component c) {
        if (hierarchyListener == null) {
            return null;
        } else {
            return hierarchyListener.getStackFromComponent(c);
        }
    }
    
    static void calledInAWT() {
        // A breakpoint is submitted on this method.
        // When awtAccess field is set to true, this breakpoint is hit in AWT thread
        // and methods can be executed via debugger.
    }
    
    static Object addLoggingListener(Component c, Class listener) {
        return RemoteAWTServiceListener.add(c, listener);
    }
    
    static boolean removeLoggingListener(Component c, Class listenerClass, Object listener) {
        return RemoteAWTServiceListener.remove(c, listenerClass, listener);
    }
    
    static void pushEventData(Component c, Class listenerClass, String[] data, String[] stack) {
        synchronized (eventData) {
            List ld = null;
            if (!eventData.isEmpty()) {
                ListenerEvent le = (ListenerEvent) eventData.get(eventData.size() - 1);
                if (le.c == c && le.listenerClass == listenerClass) {
                    ld = le.data;
                }
            }
            if (ld == null) {
                ListenerEvent le = new ListenerEvent(c, listenerClass);
                eventData.add(le);
                ld = le.data;
            }
            ld.add(data);
            ld.add(stack);
        }
    }
    
    static void calledWithEventsData(Component c, Class listenerClass, String[] data) {
        // A breakpoint is submitted on this method.
        // When breakpoint is hit, data can be retrieved
    }
    
    static Snapshot[] getGUISnapshots() {
        List snapshots = new ArrayList();   //System.err.println("gGUI: thread = "+Thread.currentThread());
        Window[] windows = Window.getWindows(); //System.err.println("gGUI: windows = "+windows.length);
        for (int wi = 0; wi < windows.length; wi++) {
            Window w = windows[wi]; //System.err.println("gGUI: w["+wi+"] = "+w+", is visible = "+w.isVisible());
            if (!w.isVisible()) {
                continue;
            }
            Dimension d = w.getSize();  //System.err.println("gGUI:  size = "+d);
            if (d.width == 0 || d.height == 0) {
                continue;
            }
            BufferedImage bi = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = bi.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
            w.paint(g);
            Raster raster = bi.getData();
            Object data = raster.getDataElements(0, 0, d.width, d.height, null);
            int[] dataArr;  //System.err.println("gGUI: data = "+data);
            if (data instanceof int[]) {
                dataArr = (int[]) data;
            } else {
                continue;
            }
            String title = null;
            if (w instanceof Frame) {
                title = ((Frame) w).getTitle();
            } else if (w instanceof Dialog) {
                title = ((Dialog) w).getTitle();
            }   //System.err.println("gGUI: title = "+title);
            snapshots.add(new Snapshot(w, title, d.width, d.height, dataArr));
        }
        Snapshot[] snapshotArr = (Snapshot[]) snapshots.toArray(new Snapshot[] {});
        lastGUISnapshots = snapshotArr;
        return snapshotArr;
    }
    
    // This static field is used to prevent the result from GC until it's read by debugger.
    // Debugger should clear this field explicitly after it reads the result.
    private static Snapshot[] lastGUISnapshots;
    
    private static Thread preferredEventThread; // The preferred AWT thread
    
    private static class AWTAccessLoop implements Runnable {
        
        public AWTAccessLoop() {}

        public void run() {
            if (SwingUtilities.isEventDispatchThread()) {
                calledInAWT();
                return ;
            }
            while (awtAccessLoop) {
                if (awtAccess) {
                    awtAccess = false;
                    ThreadGroup preferredThreadGroup = getPreferredAWTThreadGroup(preferredEventThread);
                    if (preferredThreadGroup != null) {
                        new TG_AWT_Invocator(preferredThreadGroup).start();
                    } else {
                        SwingUtilities.invokeLater(this);
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    return ;
                }
                List eventDataCopy = null;
                synchronized (eventData) {
                    if (eventData.size() > 0) {
                        eventDataCopy = new ArrayList(eventData);
                        eventData.clear();
                    }
                }
                if (eventDataCopy != null) {
                    for (Iterator ile = eventDataCopy.iterator(); ile.hasNext(); ) {
                        ListenerEvent le = (ListenerEvent) ile.next();
                        Component c = le.c;
                        Class listenerClass = le.listenerClass;
                        List dataList = le.data;
                        int totalLength = 0;
                        int l = dataList.size();
                        for (int i = 0; i < l; i++) {
                            totalLength += 1 + ((String[]) dataList.get(i)).length;
                        }
                        String[] allData = new String[totalLength];
                        int ii = 0;
                        for (int i = 0; i < l; i++) {
                            String[] data = (String[]) dataList.get(i);
                            allData[ii++] = Integer.toString(data.length);
                            for (int j = 0; j < data.length; j++) {
                                allData[ii++] = data[j];
                            }
                        }
                        calledWithEventsData(c, listenerClass, allData);
                    }
                }
            }
            // Stopped
            stopHierarchyListener();
        }
    }
    
    private static ThreadGroup getPreferredAWTThreadGroup(Thread t) {
        if (t == null) {
            return null;
        }
        ThreadGroup tg = t.getThreadGroup();
        if (tg == Thread.currentThread().getThreadGroup()) {
            return null;
        }
        return tg;
    }
    
    private static class TG_AWT_Invocator extends Thread {
        
        public TG_AWT_Invocator(ThreadGroup tg) {
            super(tg, TG_AWT_Invocator.class.getName());
        }

        public void run() {
            Runnable accessLoop = awtAccessLoopRunnable;
            if (accessLoop != null) {
                SwingUtilities.invokeLater(accessLoop);
            }
        }
        
    }
    
    private static class Snapshot {
        
        private Window w;
        private String title;
        private int width;
        private int height;
        private int[] dataArr;
        private String allIntDataString;
        private String allNamesString;
        private Component[] allComponentsArray;
        private String componentsAddAt;
        private ComponentInfo component;
        private final Rectangle rectangle = new Rectangle();
        private static final char STRING_DELIMITER = (char) 3;   // ETX (end of text)
        
        Snapshot(Window w, String title, int width, int height, int[] dataArr) {
            this.w = w;
            this.title = title;
            this.width = width;
            this.height = height;
            this.dataArr = dataArr;
            component = retrieveComponentInfo(w, Integer.MIN_VALUE, Integer.MIN_VALUE);
            int componentCount = component.getComponentsCount();
            int[] allIntDataArray = createAllIntDataArray(componentCount);
            allIntDataString = intArraytoString(allIntDataArray);
            allNamesString = createAllNamesString();
            allComponentsArray = createAllComponentsArray(componentCount);
            componentsAddAt = createComponentsAddAt(allComponentsArray);
        }
        
        private ComponentInfo retrieveComponentInfo(Component c, int shiftx, int shifty) {
            String name = c.getName();
            c.getBounds(rectangle);
            int x = rectangle.x;
            int y = rectangle.y;
            if (shiftx == Integer.MIN_VALUE && shifty == Integer.MIN_VALUE) {
                shiftx = shifty = 0; // Do not shift the window as such
                x = y = 0;
            } else {
                shiftx += x;
                shifty += y;
            }
            ComponentInfo ci = new ComponentInfo(c, name, x, y,
                                                 rectangle.width, rectangle.height,
                                                 shiftx, shifty,
                                                 c.isVisible());
            if (c instanceof Container) {
                Component[] subComponents = ((Container) c).getComponents();
                int n = subComponents.length;
                if (n > 0) {
                    ComponentInfo[] cis = new ComponentInfo[n];
                    for (int i = 0; i < cis.length; i++) {
                        cis[i] = retrieveComponentInfo(subComponents[i], shiftx, shifty);
                    }
                    ci.setSubcomponents(cis);
                }
            }
            return ci;
        }
        
        private int[] createAllIntDataArray(int componentCount) {
            int n1 = dataArr.length;
            int n = 3 + n1 + componentCount * ComponentInfo.INT_DATA_LENGTH;
            int[] array = new int[n];
            array[0] = width;
            array[1] = height;
            array[2] = n1;
            System.arraycopy(dataArr, 0, array, 3, n1);
            component.putIntData(array, n1 + 3);
            return array;
        }
        
        private static String intArraytoString(int[] a) {
            int n = a.length;
            if (n == 0)
                return "0[]";

            StringBuffer b = new StringBuffer();
            b.append(n);
            b.append('[');
            b.append(a[0]);
            for (int i = 1; i < n; i++) {
                b.append(",");
                b.append(a[i]);
            }
            b.append(']');
            return b.toString();
        }

        /** Delimit the strings with char 3 */
        private String createAllNamesString() {
            StringBuffer sb = new StringBuffer();
            if (title == null) {
                sb.append((char) 0);
            } else {
                sb.append(title);
            }
            sb.append(STRING_DELIMITER);
            component.putNamesTo(sb);
            return sb.toString();
        }
        
        private Component[] createAllComponentsArray(int componentCount) {
            Component[] components = new Component[componentCount];
            component.putComponentsTo(components, 0);
            return components;
        }
        
        private String createComponentsAddAt(Component[] components) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < components.length; i++) {
                String stack = getHierarchyChangeStackFor(components[i]);
                sb.append(stack);  // The stack or "null"
                sb.append(STRING_DELIMITER);
            }
            return sb.toString();
        }
        
        private static class ComponentInfo {
            
            private static final int INT_DATA_LENGTH = 8;
            private static final ComponentInfo[] NO_SUBCOMPONENTS = new ComponentInfo[] {};
            
            private Component c;
            private String name;
            private int x;
            private int y;
            private int width;
            private int height;
            private int shiftx;
            private int shifty;
            private boolean visible;
            private ComponentInfo[] subComponents = NO_SUBCOMPONENTS;
            
            ComponentInfo(Component c, String name, int x, int y,
                          int width, int height, int shiftx, int shifty,
                          boolean visible) {
                this.c = c;
                this.name = name;
                this.x = x;
                this.y = y;
                this.width = width;
                this.height = height;
                this.shiftx = shiftx;
                this.shifty = shifty;
                this.visible = visible;
            }
            
            void setSubcomponents(ComponentInfo[] subComponents) {
                this.subComponents = subComponents;
            }
            
            int countComponentIntData() {
                return getComponentsCount() * INT_DATA_LENGTH;
            }
            
            int getComponentsCount() {
                int n = 1;
                for (int i = 0; i < subComponents.length; i++) {
                    n += subComponents[i].getComponentsCount();
                }
                return n;
            }
            
            int putIntData(int[] array, int pos) {
                array[pos++] = x;
                array[pos++] = y;
                array[pos++] = width;
                array[pos++] = height;
                array[pos++] = shiftx;
                array[pos++] = shifty;
                array[pos++] = visible ? 1 : 0;
                array[pos++] = subComponents.length;
                for (int i = 0; i < subComponents.length; i++) {
                    pos = subComponents[i].putIntData(array, pos);
                }
                return pos;
            }
            
            int putComponentsTo(Component[] array, int pos) {
                array[pos++] = c;
                for (int i = 0; i < subComponents.length; i++) {
                    pos = subComponents[i].putComponentsTo(array, pos);
                }
                return pos;
            }
            
            void putNamesTo(StringBuffer sb) {
                if (name == null) {
                    sb.append((char) 0);
                } else {
                    sb.append(name);
                }
                sb.append(STRING_DELIMITER);
                for (int i = 0; i < subComponents.length; i++) {
                    subComponents[i].putNamesTo(sb);
                }
            }
        }
    }
    
    private static class ListenerEvent {
        
        Component c;
        Class listenerClass;
        List data;
        
        ListenerEvent(Component c, Class listenerClass) {
            this.c = c;
            this.listenerClass = listenerClass;
            this.data = new ArrayList();
        }
    }
    
}
