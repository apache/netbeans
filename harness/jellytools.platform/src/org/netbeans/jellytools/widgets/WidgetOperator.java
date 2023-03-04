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
package org.netbeans.jellytools.widgets;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.ClassReference;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.Operator;

/**
 * Handle org.netbeans.api.visual.widget.Widget object which represents some
 * graphical content.
 * <p>
 * Usage:<br>
 * <pre>
 * TopComponentOperator tco = new TopComponentOperator("My scene");
 * LabelWidgetOperator lwo0 = new LabelWidgetOperator(tco, "Label 0");
 * lwo0.performPopupAction("An action");
 * LabelWidgetOperator lwo1 = new LabelWidgetOperator(tco, "Label 1");
 * // drag from one widget to another
 * lwo0.dragNDrop(lwo1);
 * </pre>
 *
 * @author Jiri Skrivanek
 */
public class WidgetOperator extends Operator {

    protected Widget widget;

    static {
        Timeouts.initDefault("WidgetOperator.WaitWidgetTimeout", 30000);
    }

    /**
     * Creates operator for given Widget.
     *
     * @param widget Widget to create operator for
     */
    public WidgetOperator(Widget widget) {
        this.widget = widget;
    }

    /**
     * Waits for Widget specified by WidgetChooser implementation under given
     * parent.
     *
     * @param parentWidgetOper parent WidgetOperator
     * @param widgetChooser implementation of WidgetChooser
     */
    public WidgetOperator(WidgetOperator parentWidgetOper, WidgetChooser widgetChooser) {
        this(parentWidgetOper, widgetChooser, 0);
    }

    /**
     * Waits for index-th Widget under given parent.
     *
     * @param parentWidgetOper parent WidgetOperator
     * @param index index of widget to be found
     */
    public WidgetOperator(WidgetOperator parentWidgetOper, int index) {
        this(parentWidgetOper, null, index);
    }

    /**
     * Waits for index-th Widget specified by WidgetChooser implementation under
     * given parent.
     *
     * @param parentWidgetOper parent WidgetOperator
     * @param widgetChooser implementation of WidgetChooser
     * @param index index of widget to be found
     */
    public WidgetOperator(WidgetOperator parentWidgetOper, WidgetChooser widgetChooser, int index) {
        this(waitWidget(parentWidgetOper.getWidget(), widgetChooser, index));
        copyEnvironment(parentWidgetOper);
    }

    /**
     * Waits for Widget specified by WidgetChooser implementation in given
     * TopComponent.
     *
     * @param tco TopComponentOperator to find widgets in
     * @param widgetChooser implementation of WidgetChooser
     */
    public WidgetOperator(TopComponentOperator tco, WidgetChooser widgetChooser) {
        this(tco, widgetChooser, 0);
    }

    /**
     * Waits for index-th Widget in given TopComponent.
     *
     * @param tco TopComponentOperator to find widgets in
     * @param index index of widget to be found
     */
    public WidgetOperator(TopComponentOperator tco, int index) {
        this(tco, null, index);
    }

    /**
     * Waits for index-th Widget specified by WidgetChooser implementation in
     * given TopComponent.
     *
     * @param tco TopComponentOperator to find widgets in
     * @param widgetChooser implementation of WidgetChooser
     * @param index index of widget to be found
     */
    public WidgetOperator(TopComponentOperator tco, WidgetChooser widgetChooser, int index) {
        this(waitWidget(waitScene(tco), widgetChooser, index));
        copyEnvironment(tco);
    }

    /**
     * Returns Scene widget which is parent of all other widgets. It throws
     * JemmyException when Scene is not found.
     *
     * @param tco TopComponentOperator where to find Scene
     * @return Scene instance
     */
    private static Scene waitScene(TopComponentOperator tco) {
        Component sceneComp = tco.waitSubComponent(new ComponentChooser() {

            @Override
            public boolean checkComponent(Component comp) {
                return comp.getClass().getName().endsWith("SceneComponent");
            }

            @Override
            public String getDescription() {
                return "SceneComponent";
            }
        });
        try {
            Field[] fields = sceneComp.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                Class<?> type = field.getType();
                if (Scene.class.isAssignableFrom(type)) {
                    field.setAccessible(true);
                    return (Scene) field.get(sceneComp);
                }
            }
        } catch (Exception ex) {
            throw new JemmyException("Exception while getting Scene field from " + sceneComp, ex);
        }
        throw new JemmyException("Scene field not found in " + sceneComp);
    }

    /**
     * Waits for index-th widget specified by WidgetChooser under given parent
     * widget.
     *
     * @param parentWidget parent Widget
     * @param widgetChooser WidgetChooser implementation
     * @param index index to be found
     * @return Widget instance if found or throws JemmyException if not found.
     */
    private static Widget waitWidget(final Widget parentWidget, final WidgetChooser widgetChooser, final int index) {
        try {
            Waiter waiter = new Waiter(new Waitable() {

                @Override
                public Object actionProduced(Object obj) {
                    return findWidget(parentWidget, widgetChooser, index);
                }

                @Override
                public String getDescription() {
                    return (index > 0 ? index + "-th " : "")
                            + (widgetChooser == null ? "Widget " : widgetChooser.getDescription())
                            + " displayed";
                }
            });
            Timeouts timeouts = JemmyProperties.getCurrentTimeouts().cloneThis();
            timeouts.setTimeout("Waiter.WaitingTime", timeouts.getTimeout("WidgetOperator.WaitWidgetTimeout"));
            waiter.setTimeouts(timeouts);
            waiter.setOutput(JemmyProperties.getCurrentOutput());
            return (Widget) waiter.waitAction(null);
        } catch (InterruptedException ex) {
            throw new JemmyException("Interrupted.", ex);
        }
    }

    /**
     * You can implement this interface if you want to find some specific widget
     * and then you can use it in WidgetOperator constructor.
     */
    public interface WidgetChooser {

        /**
         * Returns true if given Widget matches criteria
         *
         * @param widget Widget to be checked
         * @return true if given Widget matches criteria, false otherwise
         */
        public boolean checkWidget(Widget widget);

        /**
         * Returns description of matching criteria used in output messages.
         *
         * @return description of matching criteria used in output messages
         */
        public String getDescription();
    }

    /**
     * Conter used in findWidget method to search index-th matching Widget.
     */
    private static final class Counter {

        int counter;

        public Counter() {
            counter = 0;
        }

        public void increase() {
            counter++;
        }

        public int getValue() {
            return counter;
        }
    }

    /**
     * Finds index-th widget specified by WidgetChooser under given parent
     * widget.
     *
     * @param parentWidget parent Widget
     * @param widgetChooser WidgetChooser implementation
     * @param index index to be found
     * @return Widget instance or null if not found
     */
    public static Widget findWidget(final Widget parentWidget, final WidgetChooser widgetChooser, final int index) {
        return (Widget) new QueueTool().invokeSmoothly(new QueueTool.QueueAction("findWidget") {    // NOI18N

            @Override
            public Object launch() {
                return findWidget(parentWidget, widgetChooser, index, new Counter());
            }
        });
    }

    /**
     * Finds index-th widget specified by WidgetChooser under given parent
     * widget. Firstly siblings are inspected, then their children.
     *
     * @param parentWidget parent Widget
     * @param widgetChooser WidgetChooser implementation
     * @param index index to be found
     * @param conter shared counter for indexes
     * @return Widget instance or null if not found
     */
    private static Widget findWidget(Widget parentWidget, WidgetChooser widgetChooser, int index, Counter counter) {
        List<Widget> children = parentWidget.getChildren();
        for (Iterator<Widget> it = children.iterator(); it.hasNext();) {
            Widget child = it.next();
            // test this child
            if (widgetChooser == null || widgetChooser.checkWidget(child)) {
                if (index == counter.getValue()) {
                    // found index-th match
                    return child;
                } else {
                    // increase counter and contnue searching
                    counter.increase();
                }
            }
        }
        for (Iterator<Widget> it = children.iterator(); it.hasNext();) {
            Widget found = findWidget(it.next(), widgetChooser, index, counter);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * Returns Scene widget which is parent of all other widgets.
     *
     * @return Sceen instance
     */
    private Scene getScene() {
        return (Scene) runMapping(new MapAction("widget.getScene()") {

            @Override
            public Object map() {
                return widget.getScene();
            }
        });
    }

    /**
     * Returns operator for Scene widget which is parent of all other widgets.
     *
     * @return WidgetOperator instance representing Scene widget
     */
    public WidgetOperator getSceneOperator() {
        return new WidgetOperator(getScene());
    }

    /**
     * Returns List of WidgetOperators representing children of this Widget.
     *
     * @return List&lt;WidgetOperator&gt; with children of this Widget
     */
    public List<WidgetOperator> getChildren() {
        @SuppressWarnings("unchecked")
        List<Widget> children = (List<Widget>) runMapping(new MapAction("widget.getChildren") {

            @Override
            public Object map() {
                return widget.getChildren();
            }
        });
        List<WidgetOperator> operators = new ArrayList<WidgetOperator>();
        for (Iterator<Widget> it = children.iterator(); it.hasNext();) {
            operators.add(new WidgetOperator(it.next()));
        }
        return operators;
    }

    /**
     * Returns WidgetOperator for parent Widget of this operator or null if
     * parent doesn't exist.
     *
     * @return WidgetOperator instance of parent Widget or null if parent
     * doesn't exist
     */
    public WidgetOperator getParent() {
        Widget parent = (Widget) runMapping(new MapAction("widget.getParentWidget") {

            @Override
            public Object map() {
                return widget.getParentWidget();
            }
        });
        if (parent == null) {
            return null;
        } else {
            return new WidgetOperator(parent);
        }
    }

    /**
     * Returns underlying component which hosts this widget.
     *
     * @return underlying Component instance
     */
    @Override
    public Component getSource() {
        return getViewOperator().getSource();
    }

    /**
     * Returns operator of underlying component which hosts this widget.
     *
     * @return JComponentOperator instance of underlying component
     */
    public JComponentOperator getViewOperator() {
        return new JComponentOperator((JComponent) runMapping(new MapAction("widget.getScene().getView()") {

            public Object map() {
                return widget.getScene().getView();
            }
        }));
    }

    /**
     * Returns Widget represented by this operator.
     *
     * @return Widget instance represented by this operator
     */
    public Widget getWidget() {
        return widget;
    }

    /**
     * Returns center of this widget in underlying view coordinates. It can be
     * used for mouse click.
     *
     * @return Point representing center of this widget
     */
    public Point getCenter() {
        return (Point) runMapping(new MapAction("getCenter") {

            @Override
            public Object map() {
                Rectangle inSceneLocation = widget.convertLocalToScene(widget.getBounds());
                Rectangle inViewLocation = widget.getScene().convertSceneToView(inSceneLocation);
                return new Point(inViewLocation.x + inViewLocation.width / 2, inViewLocation.y + inViewLocation.height / 2);
            }
        });
    }

    /**
     * Returns relative location of this widget to its parent.
     *
     * @return Point representing relative location to its parent
     */
    public Point getLocation() {
        return (Point) runMapping(new MapAction("widget.getLocation()") {

            @Override
            public Object map() {
                return widget.getLocation();
            }
        });
    }

    /**
     * Returns bounds occupied by widget.
     *
     * @return bounds occupied by widget
     */
    public Rectangle getBounds() {
        return (Rectangle) runMapping(new MapAction("widget.getBounds()") {

            @Override
            public Object map() {
                return widget.getBounds();
            }
        });
    }

    /**
     * Returns class name of this widget, its location and bounds.
     *
     * @return class name of this widget, its location and bounds
     */
    @Override
    public String toString() {
        return widget.getClass().getName() + "[" + getLocation() + "," + getBounds() + "]";
    }

    /**
     * Prints information about this widget and its children.
     */
    @Override
    public void printDump() {
        printDump("");
    }

    /**
     * Prints information about this widget and its children.
     *
     * @param indent indentation of output
     */
    private void printDump(String indent) {
        getOutput().printLine(indent + this);
        indent += "    ";
        List<Widget> children = widget.getChildren();
        for (Iterator<Widget> it = children.iterator(); it.hasNext();) {
            Widget child = it.next();
            WidgetOperator childOper = createOperator(child);
            // need to copy environment to be able to potentionally redirect output
            childOper.copyEnvironment(this);
            childOper.printDump(indent);
        }
    }

    /**
     * Creates WidgetOperator for given widget.
     *
     * @param widget Widget to create operator for
     * @return WidgetOperator instance for given Widget
     */
    public static WidgetOperator createOperator(Widget widget) {
        Class<?> widgetClass = widget.getClass();
        while (Widget.class.isAssignableFrom(widgetClass)) {
            String fullClassName = widgetClass.getName();
            String className = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
            try {
                return (WidgetOperator) new ClassReference("org.netbeans.jellytools.widgets." + className + "Operator").newInstance(new Object[]{widget}, new Class<?>[]{widgetClass});
            } catch (ClassNotFoundException cnfe) {
                try {
                    Class<?> clazz = Class.forName("org.netbeans.jellytools.widgets." + className + "Operator", true, Thread.currentThread().getContextClassLoader());
                    return (WidgetOperator) clazz.getConstructor(widgetClass).newInstance(widget);
                } catch (Exception e) {
                    // operator for given widget not found => try superclass
                    widgetClass = widgetClass.getSuperclass();
                }
            } catch (Exception e) {
                throw new JemmyException("Exception while creating operator.", e);
            }
        }
        // fallback for not existing operator
        return new WidgetOperator(widget);
    }

    /**
     * Performs popup action on this widget.
     *
     * @param popupPath path of popup menu item (e.g. 'Go|Next')
     */
    public void performPopupAction(String popupPath) {
        Point center = getCenter();
        getViewOperator().clickForPopup(center.x, center.y);
        JPopupMenuOperator popupOper = new JPopupMenuOperator();
        popupOper.setComparator(getComparator());
        popupOper.pushMenu(popupPath, "|", getComparator());
    }

    /**
     * Performs popup action on this widget and no block further execution.
     *
     * @param popupPath path of popup menu item (e.g. 'Go|Next')
     */
    public void performPopupActionNoBlock(String popupPath) {
        Point center = getCenter();
        getViewOperator().clickForPopup(center.x, center.y);
        JPopupMenuOperator popupOper = new JPopupMenuOperator();
        popupOper.setComparator(getComparator());
        popupOper.pushMenuNoBlock(popupPath, "|", getComparator());
    }

    /**
     * Clicks mouse in the center of widget.
     *
     * @param clickCount number of clicks
     */
    public void clickMouse(int clickCount) {
        Point center = getCenter();
        getViewOperator().clickMouse(center.x, center.y, clickCount);
    }

    /**
     * Drag from the center of this widget and drop at new position.
     *
     * @param relativeX relative distance of movement along x axis
     * @param relativeY relative distance of movement along y axis
     */
    public void dragNDrop(int relativeX, int relativeY) {
        Point center = getCenter();
        dragNDrop(center.x, center.y, center.x + relativeX, center.y + relativeY);
    }

    /**
     * Drag from the center widget and drop at the center of given widget.
     *
     * @param widgetOperator target widget where to drop
     */
    public void dragNDrop(WidgetOperator widgetOperator) {
        Point centerStart = getCenter();
        Point centerEnd = widgetOperator.getCenter();
        dragNDrop(centerStart.x, centerStart.y, centerEnd.x, centerEnd.y);
    }

    /**
     * Drag from start coordinates and drop at end coordinates.
     *
     * @param x1 start x position
     * @param y1 start y position
     * @param x2 end x position
     * @param y2 end y position
     */
    public void dragNDrop(int x1, int y1, int x2, int y2) {
        getViewOperator().dragNDrop(x1, y1, x2, y2);
    }
}
