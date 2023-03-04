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
package org.netbeans.jellytools.actions;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import javax.swing.JSplitPane;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/** Used to attach a window to a new position by IDE API.
 * It also defines constants used for attaching.
 * <p>
 * Usage:<br>
 * <pre>
TopComponentOperator tco = new TopComponentOperator("Runtime");
// attach Runtime top component right to Execution View
new AttachWindowAction("Execution View", AttachWindowAction.RIGHT).perform(tco);
Thread.sleep(2000);
// attach Runtime top component back (next to Filesystems as the last tab)
new AttachWindowAction("Filesystems", AttachWindowAction.AS_LAST_TAB).perform(tco);
 * </pre>
 * @see Action
 * @see org.netbeans.jellytools.TopComponentOperator
 * @author Jiri.Skrivanek@sun.com
 */
public class AttachWindowAction extends Action {

    public static final String TOP = JSplitPane.TOP;
    public static final String BOTTOM = JSplitPane.BOTTOM;
    public static final String LEFT = JSplitPane.LEFT;
    public static final String RIGHT = JSplitPane.RIGHT;
    public static final String AS_LAST_TAB = "As a Last Tab";
    /** "Window" main menu item. */
    private static final String windowItem = Bundle.getStringTrimmed("org.netbeans.core.windows.resources.Bundle",
            "Menu/Window");
    /** Parameter used in API operations. */
    private String sideConstant;
    // at least one of variables is defined in constructor
    private String targetTopComponentName;
    private TopComponentOperator targetTopComponentOperator;

    /** Create new AttachWindowAction instance.
     * @param targetTopComponentName name of top component defining a position
     * where to attach top component
     * @param side side where to attach top component ({@link #LEFT}, 
     * {@link #RIGHT}, {@link #TOP}, {@link #BOTTOM}, {@link #AS_LAST_TAB})
     */
    public AttachWindowAction(String targetTopComponentName, String side) {
        super(null, null);
        if (targetTopComponentName == null) {
            throw new IllegalArgumentException("targetTopComponentName cannot be null.");
        }
        this.targetTopComponentName = targetTopComponentName;
        mapSide(side);
    }

    /** Create new AttachWindowAction instance.
     * @param targetTopComponentOperator operator of top component defining a position
     * where to attach top component
     * @param side side where to attach top component ({@link #LEFT}, 
     * {@link #RIGHT}, {@link #TOP}, {@link #BOTTOM}, {@link #AS_LAST_TAB})
     */
    public AttachWindowAction(TopComponentOperator targetTopComponentOperator, String side) {
        super(null, null);
        if (targetTopComponentOperator == null) {
            throw new IllegalArgumentException("targetTopComponentOperator cannot be null.");
        }
        this.targetTopComponentOperator = targetTopComponentOperator;
        mapSide(side);
    }

    private TopComponentOperator getTargetTopComponentOperator() {
        if (targetTopComponentOperator == null) {
            // it is guaranteed targetTopComponentName != null
            targetTopComponentOperator = new TopComponentOperator(targetTopComponentName);
        }
        return targetTopComponentOperator;
    }

    /** Set sideItem and sideConstant from given parameter.
     */
    private void mapSide(String side) {
        if (side == null || side.equals(AS_LAST_TAB)) {
            sideConstant = AS_LAST_TAB;
        } else if (side.equals(RIGHT)) {
            sideConstant = RIGHT;
        } else if (side.equals(LEFT)) {
            sideConstant = LEFT;
        } else if (side.equals(TOP)) {
            sideConstant = TOP;
        } else if (side.equals(BOTTOM)) {
            sideConstant = BOTTOM;
        } else {
            throw new JemmyException("Cannot attach to position \"" + side + "\".");
        }
    }

    /** Attach given TopComponentOperator to position specified in constructor
     * of action. It also waits until given TopComponent is showing in the 
     * new position.
     * @param compOperator TopComponentOperator which should be attached to desired
     * position
     */
    @Override
    public void performAPI(ComponentOperator compOperator) {
        if (compOperator instanceof TopComponentOperator) {
            performAPI((TopComponentOperator) compOperator);
        } else {
            throw new UnsupportedOperationException(
                    "AttachWindowAction can only be called on TopComponentOperator.");
        }
    }

    /** Attaches given TopComponentOperator to position specified in constructor
     * of action. It also waits until given TopComponent is showing in the 
     * new position.
     * @param tco TopComponentOperator which should be attached to desired
     * position
     */
    public void performAPI(TopComponentOperator tco) {
        final TopComponent sourceTc = (TopComponent) tco.getSource();
        final TopComponent targetTc = (TopComponent) getTargetTopComponentOperator().getSource();
        // run in dispatch thread
        tco.getQueueTool().invokeSmoothly(new Runnable() {

            @Override
            public void run() {
                Mode mode = WindowManager.getDefault().findMode(targetTc);
                if (sideConstant.equals(AS_LAST_TAB)) {
                    mode.dockInto(sourceTc);
                    sourceTc.open();
                    sourceTc.requestActive();
                } else {
                    attachTopComponent(sourceTc, mode, sideConstant);
                }
            }
        });

        // wait until TopComponent is in new location, i.e. is showing
        try {
            new Waiter(new Waitable() {

                @Override
                public Object actionProduced(Object tc) {
                    return ((TopComponent) tc).isShowing() ? Boolean.TRUE : null;
                }

                @Override
                public String getDescription() {
                    return ("TopComponent is showing."); // NOI18N
                }
            }).waitAction(sourceTc);
        } catch (InterruptedException e) {
            throw new JemmyException("Interrupted.", e); // NOI18N
        }
    }

    static Object callWindowManager(String method, Object... args) {
        return callWindowManager(WindowManager.getDefault().getClass(), method, args);
    }

    private static Object callWindowManager(Class<?> clazz, String method, Object... args) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (!method.equals(m.getName())) {
                continue;
            }
            if (args == null && m.getParameterTypes().length > 0
                    || args != null && m.getParameterTypes().length != args.length) {
                continue;
            }
            try {
                return m.invoke(WindowManager.getDefault(), args);
            } catch (Exception ex) {
                throw new IllegalStateException("Cannot execute " + method, ex);
            }
        }
        return callWindowManager(clazz.getSuperclass(), method, args);
    }

    private static Class<?> classForName(String className) throws ClassNotFoundException {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return Class.forName(className, true, Thread.currentThread().getContextClassLoader());
        }
    }

    /** Uses Central.userDroppedTopComponents(ModeImpl mode, TopComponentDraggable draggable, String side)
     * to attach source TopComponent to target Mode according to sideConstant
     * value.
     * @param sourceTc source TopComponent
     * @param mode target mode
     * @param sideConstant side constant
     */
    private static void attachTopComponent(TopComponent sourceTc, Mode mode, String sideConstant) {
        try {
            Class<?> centralClass = classForName("org.netbeans.core.windows.Central");
            Class<?> tcdClass = classForName("org.netbeans.core.windows.view.dnd.TopComponentDraggable");
            Class<?> modeImplClass = classForName("org.netbeans.core.windows.ModeImpl");
            Method attachMethod = centralClass.getMethod("userDroppedTopComponents", modeImplClass, tcdClass, String.class);
            Method getCentralMethod = WindowManager.getDefault().getClass().getDeclaredMethod("getCentral", (Class<?>[]) null);
            getCentralMethod.setAccessible(true);
            Object centralInstance = getCentralMethod.invoke(WindowManager.getDefault(), (Object[]) null);
            Constructor<?> tcdConstructor = tcdClass.getDeclaredConstructor(TopComponent.class);
            tcdConstructor.setAccessible(true);
            Object tcdInstance = tcdConstructor.newInstance(sourceTc);
            attachMethod.setAccessible(true);
            attachMethod.invoke(centralInstance, mode, tcdInstance, sideConstant);
        } catch (Exception e) {
            throw new JemmyException("Cannot attach TopComponent.", e);
        }
    }

    /** Throws UnsupportedOperationException because AttachWindowAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    @Override
    public void performAPI(Node[] nodes) {
        throw new UnsupportedOperationException(
                "AttachWindowAction doesn't have popup representation on nodes.");
    }

    /** Throws UnsupportedOperationException because AttachWindowAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    @Override
    public void performMenu(Node[] nodes) {
        throw new UnsupportedOperationException(
                "AttachWindowAction doesn't have popup representation on nodes.");
    }

    /** Throws UnsupportedOperationException because AttachWindowAction doesn't have
     * popup representation on nodes.
     * @param nodes array of nodes
     */
    @Override
    public void performPopup(Node[] nodes) {
        throw new UnsupportedOperationException(
                "AttachWindowAction doesn't have popup representation on nodes.");
    }

    /** Throws UnsupportedOperationException because AttachWindowAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    @Override
    public void performShortcut(Node[] nodes) {
        throw new UnsupportedOperationException(
                "AttachWindowAction doesn't have popup representation on nodes.");
    }
}
