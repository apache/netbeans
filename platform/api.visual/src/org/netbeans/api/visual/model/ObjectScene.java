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
package org.netbeans.api.visual.model;

import org.netbeans.api.visual.action.*;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import java.util.*;
import java.util.List;
import java.awt.*;

/**
 * This class manages mapping between model-objects and widgets on a scene. Object mapping is added/removed using addObject and removeObject methods.
 * You can query the mapping using the findWidget(Object) and the findObject(Widget) methods.
 * <p>
 * It also manages object-oriented states and creates a object-specific action that could be assigned to widgets to provide
 * functionality like object-based selection, object-based hovering, ...
 *
 * @author David Kaspar
 */
public class ObjectScene extends Scene {

    private static final ObjectSceneListener[] EMPTY_LISTENERS = new ObjectSceneListener[0];
    private static final Set<Object> EMPTY_SET = Collections.unmodifiableSet (Collections.emptySet ());
    private static final Widget[] EMPTY_WIDGETS_ARRAY = new Widget[0];
    private static final List<Widget> EMPTY_WIDGETS_LIST = Collections.emptyList ();

    private HashMap<Object, Object> objects = new HashMap<Object, Object> ();
    private Set<Object> objectsUm = Collections.unmodifiableSet (objects.keySet ());

    private HashMap<Object, Widget> object2widget = new HashMap<Object, Widget> ();
    private HashMap<Object, List<Widget>> object2widgets = new HashMap<Object, List<Widget>> ();
    private HashMap<Widget, Object> widget2object = new HashMap<Widget, Object> ();

    private HashMap<Object, ObjectState> objectStates = new HashMap<Object, ObjectState> ();

    private HashSet<Object> selectedObjects = new HashSet<Object> ();
    private Set<Object> selectedObjectsUm = Collections.unmodifiableSet (selectedObjects);

    private HashSet<Object> highlightedObjects = new HashSet<Object> ();
    private Set<Object> highlightedObjectsUm = Collections.unmodifiableSet (highlightedObjects);

    private Object focusedObject = null;
    private Object hoveredObject = null;

    private WidgetAction selectAction = ActionFactory.createSelectAction (new ObjectSelectProvider (), true);
    private WidgetAction objectHoverAction;

    private Map<ObjectSceneEventType, List<ObjectSceneListener>> listeners = new java.util.EnumMap<ObjectSceneEventType, List<ObjectSceneListener>> (ObjectSceneEventType.class);
    private ObjectSceneEvent event = new ObjectSceneEvent (this);

    /**
     * Adds a mapping between an object and a widget.
     * Note that it does not add the widget into the scene automatically - it has to be done manually before this method is called.
     * @param object the model object; the object must not be a Widget
     * @param widgets the scene widgets; if it is empty or it is a single null value then the object is non-visual and does not have any widget assigned;
     *     otherwise the widgets cannot contain null values
     */
    public final void addObject (Object object, Widget... widgets) {
        assert object != null  &&  ! (object instanceof Widget)  &&  ! objects.containsKey (object);
        Widget mainWidget = widgets.length > 0 ? widgets[0] : null;
        if (mainWidget == null)
            widgets = EMPTY_WIDGETS_ARRAY;
        for (Widget widget : widgets) {
            assert widget != null;
            assert ! widget2object.containsKey (widget) && widget.getScene () == this;
        }

        objects.put (object, object);
        object2widget.put (object, mainWidget);
        object2widgets.put (object, mainWidget != null ? Arrays.asList (widgets) : EMPTY_WIDGETS_LIST);
        ObjectState state = objectStates.computeIfAbsent(object, (o) -> ObjectState.createNormal());
        for (Widget widget : widgets) {
            widget2object.put (widget, object);
            widget.setState (state);
        }

        for (ObjectSceneListener listener : getListeners (ObjectSceneEventType.OBJECT_ADDED))
            listener.objectAdded (event, object);
    }
    
    /**
     * Removes mapping for an object. The caller is responsible for removing
     * widgets from the scene before this call. If the object is already removed or did not exist, the method
     * does nothing. The method <b>will not</b> clear object's state. 
     * <p>
     * As {@link #addObject} allows to add object-widget mapping as the user works with the scene,
     * this method allows to remove such mapping. Selection, highlight and other flags remain unchanged.
     * 
     * @param object object, whose mapping should be removed
     * @since 2.49
     */
    public final void removeObjectMapping(Object object) {
        boolean removed = objects.remove(object) != null;
        object2widget.remove (object);
        List<Widget> widgets = object2widgets.remove (object);
        for (Widget widget : widgets) {
            widget2object.remove (widget);
        }
        if (removed) {
            for (ObjectSceneListener listener : getListeners (ObjectSceneEventType.OBJECT_REMOVED))
                listener.objectRemoved (event, object);
        }
    }

    /**
     * Removes the object's state, removes the object from selection, hover etc. Does not deregister
     * the object or remove its widgets.
     * @param object to clear
     * @since 2.49
     */
    public void clearObjectState(Object object) {
        if (selectedObjects.contains (object)) {
            HashSet<Object> temp = new HashSet<Object> (selectedObjects);
            temp.remove (object);
            setSelectedObjects (temp);
        }
        if (highlightedObjects.contains (object)) {
            HashSet<Object> temp = new HashSet<Object> (highlightedObjects);
            temp.remove (object);
            setHighlightedObjects (temp);
        }
        if (object.equals (hoveredObject)) {
            setHoveredObject (null);
        }
        if (object.equals (focusedObject)) {
            setFocusedObject (null);
        }
        objectStates.remove (object);
    }

    /**
     * Removes a mapping for an object.
     * Note that it does not remove the widget from the scene automatically - it has to be done manually after this method is called.
     * @param object the object for which the mapping is removed
     */
    public final void removeObject (Object object) {
        assert object != null  &&   objects.containsKey (object);
        if (selectedObjects.contains (object)) {
            HashSet<Object> temp = new HashSet<Object> (selectedObjects);
            temp.remove (object);
            setSelectedObjects (temp);
        }
        if (highlightedObjects.contains (object)) {
            HashSet<Object> temp = new HashSet<Object> (highlightedObjects);
            temp.remove (object);
            setHighlightedObjects (temp);
        }
        if (object.equals (hoveredObject)) {
            setHoveredObject (null);
        }
        if (object.equals (focusedObject)) {
            setFocusedObject (null);
        }
        objectStates.remove (object);
        object2widget.remove (object);
        List<Widget> widgets = object2widgets.remove (object);
        for (Widget widget : widgets)
            widget2object.remove (widget);
        objects.remove (object);
        for (ObjectSceneListener listener : getListeners (ObjectSceneEventType.OBJECT_REMOVED))
            listener.objectRemoved (event, object);
    }

    /**
     * Returns a set of objects with registered mapping.
     * @return the set of register objects
     */
    public final Set<?> getObjects () {
        return objectsUm;
    }

    /**
     * Returns whether a specified object is registered.
     * @param object the object to be checked
     * @return true if the object is register; false if the object is not registered
     */
    public final boolean isObject (Object object) {
        return objects.containsKey (object);
    }

    /**
     * Returns a set of selected objects.
     * @return the set of selected objects
     */
    public final Set<?> getSelectedObjects () {
        return selectedObjectsUm;
    }

    /**
     * Sets a set of selected objects.
     * @param selectedObjects the set of selected objects
     */
    public final void setSelectedObjects (Set<?> selectedObjects) {
        ObjectSceneListener[] listeners = getListeners (ObjectSceneEventType.OBJECT_STATE_CHANGED);
        ObjectSceneListener[] selectionListeners = getListeners (ObjectSceneEventType.OBJECT_SELECTION_CHANGED);
        Set<Object> previouslySelectedObject = selectionListeners.length != 0 ? Collections.unmodifiableSet (new HashSet<Object> (this.selectedObjects)) : EMPTY_SET;

        for (Iterator<Object> iterator = this.selectedObjects.iterator (); iterator.hasNext ();) {
            Object object = iterator.next ();
            if (! selectedObjects.contains (object)) {
                iterator.remove ();
                ObjectState previousState = objectStates.get (object);
                ObjectState newState = previousState.deriveSelected (false);
                objectStates.put (object, newState);
                List<Widget> lst = object2widgets.get (object);
                if (lst != null) for (Widget widget : lst)
                    widget.setState (widget.getState ().deriveSelected (false));
                for (ObjectSceneListener listener : listeners)
                    listener.objectStateChanged (event, object, previousState, newState);
            }
        }

        for (Object object : selectedObjects) {
            if (! this.selectedObjects.contains (object)) {
                this.selectedObjects.add (object);
                ObjectState previousState = findObjectState (object);
                ObjectState newState = previousState.deriveSelected (true);
                objectStates.put (object, newState);
                List<Widget> lst = object2widgets.get (object);
                if (lst != null) for (Widget widget : lst)
                    widget.setState (widget.getState ().deriveSelected (true));
                for (ObjectSceneListener listener : listeners)
                    listener.objectStateChanged (event, object, previousState, newState);
            }
        }

        for (ObjectSceneListener listener : selectionListeners)
            listener.selectionChanged (event, previouslySelectedObject, this.selectedObjectsUm);
    }

    /**
     * Returns a set of highlighted objects.
     * @return the set of highlighted objects
     */
    public final Set<?> getHighlightedObjects () {
        return highlightedObjectsUm;
    }

    /**
     * Sets a set of highlighted objects.
     * @param highlightedObjects the set of highlighted objects
     */
    public final void setHighlightedObjects (Set<?> highlightedObjects) {
        ObjectSceneListener[] listeners = getListeners (ObjectSceneEventType.OBJECT_STATE_CHANGED);
        ObjectSceneListener[] highlightingListeners = getListeners (ObjectSceneEventType.OBJECT_HIGHLIGHTING_CHANGED);
        Set<Object> previouslyHighlightedObject = highlightingListeners.length != 0 ? Collections.unmodifiableSet (new HashSet<Object> (this.highlightedObjects)) : EMPTY_SET;

        for (Iterator<Object> iterator = this.highlightedObjects.iterator (); iterator.hasNext ();) {
            Object object = iterator.next ();
            if (! highlightedObjects.contains (object)) {
                iterator.remove ();
                ObjectState previousState = objectStates.get (object);
                ObjectState newState = previousState.deriveHighlighted (false);
                objectStates.put (object, newState);
                List<Widget> lst = object2widgets.get (object);
                if (lst != null) for (Widget widget : lst)
                    widget.setState (widget.getState ().deriveHighlighted (false));
                for (ObjectSceneListener listener : listeners)
                    listener.objectStateChanged (event, object, previousState, newState);
            }
        }

        for (Object object : highlightedObjects) {
            if (! this.highlightedObjects.contains (object)) {
                this.highlightedObjects.add (object);
                ObjectState previousState = findObjectState(object);
                ObjectState newState = previousState.deriveHighlighted (true);
                objectStates.put (object, newState);
                List<Widget> lst = object2widgets.get (object);
                if (lst != null) for (Widget widget : object2widgets.get (object))
                    widget.setState (widget.getState ().deriveHighlighted (true));
                for (ObjectSceneListener listener : listeners)
                    listener.objectStateChanged (event, object, previousState, newState);
            }
        }

        for (ObjectSceneListener listener : highlightingListeners)
            listener.highlightingChanged (event, previouslyHighlightedObject, this.highlightedObjectsUm);
    }

    /**
     * Returns a hovered object. There could be only one hovered object at maximum at the same time.
     * @return the hovered object; null if no object is hovered
     */
    public final Object getHoveredObject () {
        return hoveredObject;
    }

    /**
     * Sets a hovered object.
     * @param hoveredObject the hovered object; if null, then the scene does not have hovered object
     */
    public final void setHoveredObject (Object hoveredObject) {
        if (hoveredObject != null) {
            if (hoveredObject.equals (this.hoveredObject))
                return;
        } else {
            if (this.hoveredObject == null)
                return;
        }

        ObjectSceneListener[] listeners = getListeners (ObjectSceneEventType.OBJECT_STATE_CHANGED);
        ObjectSceneListener[] hoverListeners = getListeners (ObjectSceneEventType.OBJECT_HOVER_CHANGED);
        Object previouslyHoveredObject = this.hoveredObject;

        if (this.hoveredObject != null) {
            ObjectState previousState = objectStates.get (this.hoveredObject);
            ObjectState newState = previousState.deriveObjectHovered (false);
            objectStates.put (this.hoveredObject, newState);
            List<Widget> lst = object2widgets.get (this.hoveredObject);
            if (lst != null) for (Widget widget : lst)
                widget.setState (widget.getState ().deriveObjectHovered (false));
            for (ObjectSceneListener listener : listeners)
                listener.objectStateChanged (event, this.hoveredObject, previousState, newState);
        }

        this.hoveredObject = hoveredObject;

        if (this.hoveredObject != null) {
            ObjectState previousState = findObjectState (this.hoveredObject);
            ObjectState newState = previousState.deriveObjectHovered (true);
            objectStates.put (this.hoveredObject, newState);
            List<Widget> lst = object2widgets.get (this.hoveredObject);
            if (lst != null) for (Widget widget : lst)
                widget.setState (widget.getState ().deriveObjectHovered (true));
            for (ObjectSceneListener listener : listeners)
                listener.objectStateChanged (event, this.hoveredObject, previousState, newState);
        }

        for (ObjectSceneListener listener : hoverListeners)
            listener.hoverChanged (event, previouslyHoveredObject, this.hoveredObject);
    }

    /**
     * Returns a focused object. There could be only one focused object at maximum at the same time.
     * @return the focused object; null if no object is focused
     */
    public final Object getFocusedObject () {
        return focusedObject;
    }

    /**
     * Sets a focused object.
     * @param focusedObject the focused object; if null, then the scene does not have focused object
     */
    public final void setFocusedObject (Object focusedObject) {
        if (focusedObject != null) {
            if (focusedObject.equals (this.focusedObject))
                return;
        } else {
            if (this.focusedObject == null)
                return;
        }

        ObjectSceneListener[] listeners = getListeners (ObjectSceneEventType.OBJECT_STATE_CHANGED);
        ObjectSceneListener[] focusListeners = getListeners (ObjectSceneEventType.OBJECT_FOCUS_CHANGED);
        Object previouslyFocusedObject = this.focusedObject;

        if (this.focusedObject != null) {
            ObjectState previousState = objectStates.get (this.focusedObject);
            ObjectState newState = previousState.deriveObjectFocused (false);
            objectStates.put (this.focusedObject, newState);
            List<Widget> lst = object2widgets.get (this.focusedObject);
            if (lst != null) for (Widget widget : lst)
                widget.setState (widget.getState ().deriveObjectFocused (false));
            for (ObjectSceneListener listener : listeners)
                listener.objectStateChanged (event, this.focusedObject, previousState, newState);
        }

        this.focusedObject = focusedObject;

        if (this.focusedObject != null) {
            ObjectState previousState = findObjectState (this.focusedObject);
            ObjectState newState = previousState.deriveObjectFocused (true);
            objectStates.put (this.focusedObject, newState);
            List<Widget> lst = object2widgets.get (this.focusedObject);
            if (lst != null) for (Widget widget : lst)
                widget.setState (widget.getState ().deriveObjectFocused (true));
            for (ObjectSceneListener listener : listeners)
                listener.objectStateChanged (event, this.focusedObject, previousState, newState);
            setFocusedWidget (object2widget.get (this.focusedObject));
        } else
            setFocusedWidget (null);

        for (ObjectSceneListener listener : focusListeners)
            listener.focusChanged (event, previouslyFocusedObject, this.focusedObject);

    }

    /**
     * Creates a object-oriented select action.
     * @return the object-oriented select action
     */
    public final WidgetAction createSelectAction () {
        return selectAction;
    }

    /**
     * Returns a object-oriented hover action.
     * @return the object-oriented hover action
     */
    public final WidgetAction createObjectHoverAction () {
        if (objectHoverAction == null) {
            objectHoverAction = ActionFactory.createHoverAction (new ObjectHoverProvider ());
            getActions ().addAction (objectHoverAction);
        }
        return objectHoverAction;
    }

    /**
     * Returns the widget that is mapped to a specified object.
     * @param object the object; must not be a Widget
     * @return the widget from the registered mapping; null if the object is non-visual or no mapping is registered
     */
    public final Widget findWidget (Object object) {
        assert ! (object instanceof Widget) : "Use findObject method for getting an object assigned to a specific Widget"; // NOI18N
        return object2widget.get (object);
    }

    /**
     * Returns a list of all widgets that are mapped to a specified object.
     * @param object the object; must not be a Widget
     * @return the list of all widgets from the registered mapping; empty list if the object is non-visual; null if no mapping is registered
     */
    public final List<Widget> findWidgets (Object object) {
        assert ! (object instanceof Widget) : "Use findObject method for getting an object assigned to a specific Widget"; // NOI18N
        return object2widgets.get (object);
    }

    /**
     * Returns an object which is assigned to a widget.
     * If the widget is not mapped to any object then the method recursively searches for an object of the parent widget.
     * @param widget the widget
     * @return the mapped object; null if no object is assigned to a widget or any of its parent widgets
     */
    public final Object findObject (Widget widget) {
        while (widget != null) {
            Object o = widget2object.get (widget);
            if (o != null)
                return o;
            widget = widget.getParentWidget ();
        }
        return null;
    }

    /**
     * Returns an instance of stored object.
     * It searches for an instance of an object stored internally in the class using "equals" method on an object.
     * @param object the object that is equals (observed by calling the "equals" method on the instances stored in the class);
     *           the object must not be a Widget
     * @return the stored instance of the object
     */
    public final Object findStoredObject (Object object) {
        assert ! (object instanceof Widget) : "Use findObject method for getting an object assigned to a specific Widget"; // NOI18N
        return objects.get (object);
    }

    /**
     * Returns an object-state of a specified object.
     * @param object the object
     * @return the object-state of the specified object; null if the object is not registered
     */
    public final ObjectState getObjectState (Object object) {
        return objectStates.get (object);
    }

    /**
     * Set by actions for setting selected objects invoked by an user.
     * @param suggestedSelectedObjects the selected objects suggested by an user
     * @param invertSelection the invert selection is specified by an user
     */
    public void userSelectionSuggested (Set<?> suggestedSelectedObjects, boolean invertSelection) {
        if (invertSelection) {
            HashSet<Object> objects = new HashSet<Object> (getSelectedObjects ());
            for (Object o : suggestedSelectedObjects) {
                if (objects.contains (o))
                    objects.remove (o);
                else
                    objects.add (o);
            }
            setSelectedObjects (objects);
        } else {
            setSelectedObjects (suggestedSelectedObjects);
        }
    }

    /**
     * This method returns an identity code. It should be unique for each object in the scene.
     * The identity code is a Comparable and could be used for sorting.
     * The method implementation should be fast.
     * @param object the object
     * @return the identity code of the object; null, if the object is null
     */
    @SuppressWarnings("rawtypes")
    public Comparable getIdentityCode(Object object) {
        return object != null ? System.identityHashCode (object) : null;
    }

    /**
     * Adds object scene listener for specified object scene event types.
     * @param listener the object scene listener
     * @param types the object scene event types
     */
    public final void addObjectSceneListener (ObjectSceneListener listener, ObjectSceneEventType... types) {
        for (ObjectSceneEventType type : types)
            addObjectSceneListenerCore (listener, type);
    }

    private void addObjectSceneListenerCore (ObjectSceneListener listener, ObjectSceneEventType type) {
        List<ObjectSceneListener> list = listeners.get (type);
        if (list == null) {
            list = new ArrayList<ObjectSceneListener> ();
            listeners.put (type, list);
        }
        list.add (listener);
    }

    /**
     * Removes object scene listener for specified object scene event types.
     * @param listener the object scene listener
     * @param types the object scene event types
     */
    public final void removeObjectSceneListener (ObjectSceneListener listener, ObjectSceneEventType... types) {
        for (ObjectSceneEventType type : types)
            removeObjectSceneListenerCore (listener, type);
    }

    private void removeObjectSceneListenerCore (ObjectSceneListener listener, ObjectSceneEventType type) {
        List<ObjectSceneListener> list = listeners.get (type);
        if (list == null)
            return;
        list.remove (listener);
        if (list.isEmpty ())
            listeners.remove (type);
    }

    private ObjectSceneListener[] getListeners (ObjectSceneEventType type) {
        List<ObjectSceneListener> listeners = this.listeners.get (type);
        if (listeners == null)
            return EMPTY_LISTENERS;
        return listeners.toArray (new ObjectSceneListener[0]);
    }

    private class ObjectSelectProvider implements SelectProvider {

        public boolean isAimingAllowed (Widget widget, Point localLocation, boolean invertSelection) {
            return false;
        }

        public boolean isSelectionAllowed (Widget widget, Point localLocation, boolean invertSelection) {
            return findObject (widget) != null;
        }

        public void select (Widget widget, Point localLocation, boolean invertSelection) {
            Object object = findObject (widget);

            setFocusedObject (object);
            if (object != null) {
                if (! invertSelection  &&  getSelectedObjects ().contains (object))
                    return;
                userSelectionSuggested (Collections.singleton (object), invertSelection);
            } else
                userSelectionSuggested (Collections.emptySet (), invertSelection);
        }
    }

    private class ObjectHoverProvider implements HoverProvider {

        public void widgetHovered (Widget widget) {
            if (ObjectScene.this == widget)
                widget = null;
            setHoveredObject (findObject (widget));
        }

    }

    /**
     * Finds the state for the given object. The object must be a valid part of the model, although
     * it may not be registered yet and no widgets are created for it. The method may return {@code null}
     * for instances that are not proper models for the scene. The default method returns {@link ObjectState#createNormal}
     * for all inputs. 
     * <p>
     * Note that even objects, which have currently no widgets can have ObjectState associated.
     * For example objects, for which the widgets were not created yet, or whose widgets were removed.
     * 
     * @param o the object
     * @return the ObjectState or {@code null} for objects that cannot be part of the model.
     * @since 2.49
     */
    protected ObjectState findObjectState(Object o) throws IllegalArgumentException {
        ObjectState s = objectStates.get(o);
        return s != null ? s : ObjectState.createNormal();
    }

}
