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
package org.netbeans.api.visual.action;

import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This interface is defining an action that is usually assigned to a widget. The action is notified about all Swing events
 * related to the widget where the action is assigned.
 * <p>
 * Events: mouse, mouse-motion, mouse-wheel, drag'n'drop, focus, keyboard.
 * <p>
 * Each event handler has to return a state of the event processing (represented by WidgetAction.State).
 * <p>
 * Each Swing event is processed by all actions of all widgets in a scene. The processing can be stopped by consuming the event.
 * When processing event for a widget, then all children widget (from the last) of widget are asked for processing first.
 * Then finally the event is processed by all actions (from the first to the last) assigned to the widget.
 *
 * @author David Kaspar
 */
public interface WidgetAction {

    /**
     * Represents a state of event processing.
     */
    public abstract static class State {

        /**
         * The state that means: The event is not processed by the action and has to be processed by other actions too.
         */
        public static final State REJECTED = new State() {
            public boolean isLockedInChain() { return false; }
            public boolean isConsumed() { return false; }
            public Widget getLockedWidget() { return null; }
            public WidgetAction getLockedAction() { return null; }
        };

        /**
         * The state that means: The event is processed by the action and the processing has to stopped immediately (no other action should processed it).
         */
        public static final State CONSUMED = new State() {
            public boolean isLockedInChain() { return false; }
            public boolean isConsumed() { return true; }
            public Widget getLockedWidget() { return null; }
            public WidgetAction getLockedAction() { return null; }
        };

        /**
         * The state that means: The event is processed by the action and only actions that are in the same chain can process the event too.
         */
        public static final State CHAIN_ONLY = new State() {
            public boolean isLockedInChain() { return true; }
            public boolean isConsumed() { return false; }
            public Widget getLockedWidget() { return null; }
            public WidgetAction getLockedAction() { return null; }
        };

        /**
         * Creates a state that means: The event is processed and the processing has to stopped immediately (no other action should processed it).
         * Next event will be processed by the lockedAction on lockedAction first.
         * Only if the lockedAction reject the event, then the event will be processed regularly.
         * It is used for locking the event processing for a long-term operation like moving, resizing, rectangular selection, ...
         * (usually they are using mouse motion events).
         */
        public static State createLocked(final Widget lockedWidget, final WidgetAction lockedAction) {
            assert lockedWidget != null;
            assert lockedAction != null;
            return new State() {
                public boolean isLockedInChain() { return false; }
                public boolean isConsumed() { return true; }
                public Widget getLockedWidget() { return lockedWidget; }
                public WidgetAction getLockedAction() { return lockedAction; }
            };
        }
        
        private State() {
        }

        /**
         * Returns whether the event processing has to be stopped after the processing by the chain where the actions is added.
         * @return true if locked in chain
         */
        public abstract boolean isLockedInChain();

        /**
         * Returns whether the event is consumed
         * @return true if the event is consumed
         */
        public abstract boolean isConsumed();

        /**
         * Returns whether (and by which widget) the next event has to be processed prior to regular processing.
         * @return the locked widget; if null, then there is no prior widget
         */
        public abstract Widget getLockedWidget();

        /**
         * Returns whether (and by which action) the next event has to be processed prior to regular processing.
         * @return the locked action; if null, then there is no prior action
         */
        public abstract WidgetAction getLockedAction();
        
    }

    /**
     * Called for handling a mouseClicked event.
     * @param widget the widget where the action is assigned
     * @param event the mouse event
     * @return the event state
     */
    public State mouseClicked(Widget widget, WidgetMouseEvent event);

    /**
     * Called for handling a mousePressed event.
     * @param widget the widget where the action is assigned
     * @param event the mouse event
     * @return the event state
     */
    public State mousePressed(Widget widget, WidgetMouseEvent event);

    /**
     * Called for handling a mouseReleased event.
     * @param widget the widget where the action is assigned
     * @param event the mouse event
     * @return the event state
     */
    public State mouseReleased(Widget widget, WidgetMouseEvent event);

    /**
     * Called for handling a mouseEntered event.
     * @param widget the widget where the action is assigned
     * @param event the mouse event
     * @return the event state
     */
    public State mouseEntered(Widget widget, WidgetMouseEvent event);

    /**
     * Called for handling a mouseExited event.
     * @param widget the widget where the action is assigned
     * @param event the mouse event
     * @return the event state
     */
    public State mouseExited(Widget widget, WidgetMouseEvent event);

    /**
     * Called for handling a mouseDragged event.
     * @param widget the widget where the action is assigned
     * @param event the mouse event
     * @return the event state
     */
    public State mouseDragged(Widget widget, WidgetMouseEvent event);

    /**
     * Called for handling a mouseMoved event.
     * @param widget the widget where the action is assigned
     * @param event the mouse event
     * @return the event state
     */
    public State mouseMoved(Widget widget, WidgetMouseEvent event);

    /**
     * Called for handling a mouseWheelMoved event.
     * @param widget the widget where the action is assigned
     * @param event the mouse wheel event
     * @return the event state
     */
    public State mouseWheelMoved(Widget widget, WidgetMouseWheelEvent event);

    /**
     * Called for handling a keyTyped event.
     * @param widget the widget where the action is assigned
     * @param event the key event
     * @return the event state
     */
    public State keyTyped(Widget widget, WidgetKeyEvent event);

    /**
     * Called for handling a keyPressed event.
     * @param widget the widget where the action is assigned
     * @param event the key event
     * @return the event state
     */
    public State keyPressed(Widget widget, WidgetKeyEvent event);

    /**
     * Called for handling a keyReleased event.
     * @param widget the widget where the action is assigned
     * @param event the key event
     * @return the event state
     */
    public State keyReleased(Widget widget, WidgetKeyEvent event);

    /**
     * Called for handling a focusGained event.
     * @param widget the widget where the action is assigned
     * @param event the focus event
     * @return the event state
     */
    public State focusGained(Widget widget, WidgetFocusEvent event);

    /**
     * Called for handling a focusLost event.
     * @param widget the widget where the action is assigned
     * @param event the focus event
     * @return the event state
     */
    public State focusLost(Widget widget, WidgetFocusEvent event);

    /**
     * Called for handling a dragEnter event.
     * @param widget the widget where the action is assigned
     * @param event the drop target drag event
     * @return the event state
     */
    public State dragEnter (Widget widget, WidgetDropTargetDragEvent event);

    /**
     * Called for handling a dragOver event.
     * @param widget the widget where the action is assigned
     * @param event the drop target drag event
     * @return the event state
     */
    public State dragOver (Widget widget, WidgetDropTargetDragEvent event);

    /**
     * Called for handling a dropActionChanged event.
     * @param widget the widget where the action is assigned
     * @param event the drop target drag event
     * @return the event state
     */
    public State dropActionChanged (Widget widget, WidgetDropTargetDragEvent event);

    /**
     * Called for handling a dragExit event.
     * @param widget the widget where the action is assigned
     * @param event the drop target event
     * @return the event state
     */
    public State dragExit (Widget widget, WidgetDropTargetEvent event);

    /**
     * Called for handling a drop event.
     * @param widget the widget where the action is assigned
     * @param event the drop target drop event
     * @return the event state
     */
    public State drop (Widget widget, WidgetDropTargetDropEvent event);

    /**
     * An adapter of the widget action. All methods return rejected event state.
     */
    public static class Adapter implements WidgetAction {

        /**
         * Called for handling a mouseClicked event.
         * @param widget the widget where the action is assigned
         * @param event  the mouse event
         * @return the event state
         */
        public State mouseClicked (Widget widget, WidgetMouseEvent event) {
            return State.REJECTED;
        }

        /**
         * Called for handling a mousePressed event.
         * @param widget the widget where the action is assigned
         * @param event  the mouse event
         * @return the event state
         */
        public State mousePressed (Widget widget, WidgetMouseEvent event) {
            return State.REJECTED;
        }

        /**
         * Called for handling a mouseReleased event.
         * @param widget the widget where the action is assigned
         * @param event  the mouse event
         * @return the event state
         */
        public State mouseReleased (Widget widget, WidgetMouseEvent event) {
            return State.REJECTED;
        }

        /**
         * Called for handling a mouseEntered event.
         * @param widget the widget where the action is assigned
         * @param event  the mouse event
         * @return the event state
         */
        public State mouseEntered (Widget widget, WidgetMouseEvent event) {
            return State.REJECTED;
        }

        /**
         * Called for handling a mouseExited event.
         * @param widget the widget where the action is assigned
         * @param event  the mouse event
         * @return the event state
         */
        public State mouseExited (Widget widget, WidgetMouseEvent event) {
            return State.REJECTED;
        }

        /**
         * Called for handling a mouseDragged event.
         * @param widget the widget where the action is assigned
         * @param event  the mouse event
         * @return the event state
         */
        public State mouseDragged (Widget widget, WidgetMouseEvent event) {
            return State.REJECTED;
        }

        /**
         * Called for handling a mouseMoved event.
         * @param widget the widget where the action is assigned
         * @param event  the mouse event
         * @return the event state
         */
        public State mouseMoved (Widget widget, WidgetMouseEvent event) {
            return State.REJECTED;
        }

        /**
         * Called for handling a mouseWheelMoved event.
         * @param widget the widget where the action is assigned
         * @param event  the mouse wheel event
         * @return the event state
         */
        public State mouseWheelMoved (Widget widget, WidgetMouseWheelEvent event) {
            return State.REJECTED;
        }

        /**
         * Called for handling a keyTyped event.
         * @param widget the widget where the action is assigned
         * @param event  the key event
         * @return the event state
         */
        public State keyTyped (Widget widget, WidgetKeyEvent event) {
            return State.REJECTED;
        }

        /**
         * Called for handling a keyPressed event.
         * @param widget the widget where the action is assigned
         * @param event  the key event
         * @return the event state
         */
        public State keyPressed (Widget widget, WidgetKeyEvent event) {
            return State.REJECTED;
        }

        /**
         * Called for handling a keyReleased event.
         * @param widget the widget where the action is assigned
         * @param event  the key event
         * @return the event state
         */
        public State keyReleased (Widget widget, WidgetKeyEvent event) {
            return State.REJECTED;
        }

        /**
         * Called for handling a focusGained event.
         * @param widget the widget where the action is assigned
         * @param event  the focus event
         * @return the event state
         */
        public State focusGained (Widget widget, WidgetFocusEvent event) {
            return State.REJECTED;
        }

        /**
         * Called for handling a focusLost event.
         * @param widget the widget where the action is assigned
         * @param event  the focus event
         * @return the event state
         */
        public State focusLost (Widget widget, WidgetFocusEvent event) {
            return State.REJECTED;
        }

        /**
         * Called for handling a dragEnter event.
         * @param widget the widget where the action is assigned
         * @param event  the drop target drag event
         * @return the event state
         */
        public State dragEnter (Widget widget, WidgetDropTargetDragEvent event) {
            return State.REJECTED;
        }

        /**
         * Called for handling a dragOver event.
         * @param widget the widget where the action is assigned
         * @param event  the drop target drag event
         * @return the event state
         */
        public State dragOver (Widget widget, WidgetDropTargetDragEvent event) {
            return State.REJECTED;
        }

        /**
         * Called for handling a dropActionChanged event.
         * @param widget the widget where the action is assigned
         * @param event  the drop target drag event
         * @return the event state
         */
        public State dropActionChanged (Widget widget, WidgetDropTargetDragEvent event) {
            return State.REJECTED;
        }

        /**
         * Called for handling a dragExit event.
         * @param widget the widget where the action is assigned
         * @param event  the drop target event
         * @return the event state
         */
        public State dragExit (Widget widget, WidgetDropTargetEvent event) {
            return State.REJECTED;
        }

        /**
         * Called for handling a drop event.
         * @param widget the widget where the action is assigned
         * @param event  the drop target drop event
         * @return the event state
         */
        public State drop (Widget widget, WidgetDropTargetDropEvent event) {
            return State.REJECTED;
        }

    }

    /**
     * An adapter of the widget action. All methods return locked or rejected event state based on a result of isLocked method.
     * This is often used for long-term actions like MoveAction.
     */
    public abstract static class LockedAdapter implements WidgetAction {

        /**
         * Called by event handlers to resolve whether they should return locked or rejected event state.
         * @return if true, then locked event state is used; if false, then rejected event state is used.
         */
        protected abstract boolean isLocked ();

        /**
         * Called for handling a mouseClicked event.
         * @param widget the widget where the action is assigned
         * @param event  the mouse event
         * @return the event state
         */
        public State mouseClicked (Widget widget, WidgetMouseEvent event) {
            return isLocked () ? State.createLocked (widget, this) : State.REJECTED;
        }

        /**
         * Called for handling a mousePressed event.
         * @param widget the widget where the action is assigned
         * @param event  the mouse event
         * @return the event state
         */
        public State mousePressed (Widget widget, WidgetMouseEvent event) {
            return isLocked () ? State.createLocked (widget, this) : State.REJECTED;
        }

        /**
         * Called for handling a mouseReleased event.
         * @param widget the widget where the action is assigned
         * @param event  the mouse event
         * @return the event state
         */
        public State mouseReleased (Widget widget, WidgetMouseEvent event) {
            return isLocked () ? State.createLocked (widget, this) : State.REJECTED;
        }

        /**
         * Called for handling a mouseEntered event.
         * @param widget the widget where the action is assigned
         * @param event  the mouse event
         * @return the event state
         */
        public State mouseEntered (Widget widget, WidgetMouseEvent event) {
            return isLocked () ? State.createLocked (widget, this) : State.REJECTED;
        }

        /**
         * Called for handling a mouseExited event.
         * @param widget the widget where the action is assigned
         * @param event  the mouse event
         * @return the event state
         */
        public State mouseExited (Widget widget, WidgetMouseEvent event) {
            return isLocked () ? State.createLocked (widget, this) : State.REJECTED;
        }

        /**
         * Called for handling a mouseDragged event.
         * @param widget the widget where the action is assigned
         * @param event  the mouse event
         * @return the event state
         */
        public State mouseDragged (Widget widget, WidgetMouseEvent event) {
            return isLocked () ? State.createLocked (widget, this) : State.REJECTED;
        }

        /**
         * Called for handling a mouseMoved event.
         * @param widget the widget where the action is assigned
         * @param event  the mouse event
         * @return the event state
         */
        public State mouseMoved (Widget widget, WidgetMouseEvent event) {
            return isLocked () ? State.createLocked (widget, this) : State.REJECTED;
        }

        /**
         * Called for handling a mouseWheelMoved event.
         * @param widget the widget where the action is assigned
         * @param event  the mouse wheel event
         * @return the event state
         */
        public State mouseWheelMoved (Widget widget, WidgetMouseWheelEvent event) {
            return isLocked () ? State.createLocked (widget, this) : State.REJECTED;
        }

        /**
         * Called for handling a keyTyped event.
         * @param widget the widget where the action is assigned
         * @param event  the key event
         * @return the event state
         */
        public State keyTyped (Widget widget, WidgetKeyEvent event) {
            return isLocked () ? State.createLocked (widget, this) : State.REJECTED;
        }

        /**
         * Called for handling a keyPressed event.
         * @param widget the widget where the action is assigned
         * @param event  the key event
         * @return the event state
         */
        public State keyPressed (Widget widget, WidgetKeyEvent event) {
            return isLocked () ? State.createLocked (widget, this) : State.REJECTED;
        }

        /**
         * Called for handling a keyReleased event.
         * @param widget the widget where the action is assigned
         * @param event  the key event
         * @return the event state
         */
        public State keyReleased (Widget widget, WidgetKeyEvent event) {
            return isLocked () ? State.createLocked (widget, this) : State.REJECTED;
        }

        /**
         * Called for handling a focusGained event.
         * @param widget the widget where the action is assigned
         * @param event  the focus event
         * @return the event state
         */
        public State focusGained (Widget widget, WidgetFocusEvent event) {
            return isLocked () ? State.createLocked (widget, this) : State.REJECTED;
        }

        /**
         * Called for handling a focusLost event.
         * @param widget the widget where the action is assigned
         * @param event  the focus event
         * @return the event state
         */
        public State focusLost (Widget widget, WidgetFocusEvent event) {
            return isLocked () ? State.createLocked (widget, this) : State.REJECTED;
        }

        /**
         * Called for handling a dragEnter event.
         * @param widget the widget where the action is assigned
         * @param event  the drop target drag event
         * @return the event state
         */
        public State dragEnter (Widget widget, WidgetDropTargetDragEvent event) {
            return isLocked () ? State.createLocked (widget, this) : State.REJECTED;
        }

        /**
         * Called for handling a dragOver event.
         * @param widget the widget where the action is assigned
         * @param event  the drop target drag event
         * @return the event state
         */
        public State dragOver (Widget widget, WidgetDropTargetDragEvent event) {
            return isLocked () ? State.createLocked (widget, this) : State.REJECTED;
        }

        /**
         * Called for handling a dropActionChanged event.
         * @param widget the widget where the action is assigned
         * @param event  the drop target drag event
         * @return the event state
         */
        public State dropActionChanged (Widget widget, WidgetDropTargetDragEvent event) {
            return isLocked () ? State.createLocked (widget, this) : State.REJECTED;
        }

        /**
         * Called for handling a dragExit event.
         * @param widget the widget where the action is assigned
         * @param event  the drop target event
         * @return the event state
         */
        public State dragExit (Widget widget, WidgetDropTargetEvent event) {
            return isLocked () ? State.createLocked (widget, this) : State.REJECTED;
        }

        /**
         * Called for handling a drop event.
         * @param widget the widget where the action is assigned
         * @param event  the drop target drop event
         * @return the event state
         */
        public State drop (Widget widget, WidgetDropTargetDropEvent event) {
            return isLocked () ? State.createLocked (widget, this) : State.REJECTED;
        }

    }

    /**
     * Represents a chain of widget actions.
     */
    public static final class Chain implements WidgetAction {
        
        private List<WidgetAction> actions;
        private List<WidgetAction> actionsUm;

        /**
         * Creates a chain.
         */
        public Chain() {
            actions = new ArrayList<WidgetAction> ();
            actionsUm = Collections.unmodifiableList(actions);
        }

        /**
         * Returns a list of actions in the chain.
         * @return the lst of actions
         */
        public List<WidgetAction> getActions() {
            return actionsUm;
        }

        /**
         * Adds an action.
         * @param action the action to be added
         */
        public void addAction(WidgetAction action) {
            assert action != null;
            actions.add(action);
        }

        /**
         * Adds an action at a specific index
         * @param index the index; the action will be added before the action at the index position
         * @param action the action to be added
         */
        public void addAction(int index, WidgetAction action) {
            assert action != null;
            actions.add(index, action);
        }

        /**
         * Removes an action.
         * @param action the action
         */
        public void removeAction(WidgetAction action) {
            actions.remove(action);
        }

        /**
         * Removes an action at specified index.
         * @param index the index
         */
        public void removeAction(int index) {
            actions.remove(index);
        }

        /**
         * Called for handling a mouseClicked event.
         * @param widget the widget where the action is assigned
         * @param event  the mouse event
         * @return the event state
         */
        public State mouseClicked(Widget widget, WidgetMouseEvent event) {
            WidgetAction[] actionsArray = actions.toArray(new WidgetAction[0]);
            State chainState = State.REJECTED;
            for (WidgetAction action : actionsArray) {
                State state = action.mouseClicked(widget, event);
                if (state.isConsumed())
                    return state;
                if (state.isLockedInChain())
                    chainState = State.CONSUMED;
            }
            return chainState;
        }

        /**
         * Called for handling a mousePressed event.
         * @param widget the widget where the action is assigned
         * @param event  the mouse event
         * @return the event state
         */
        public State mousePressed(Widget widget, WidgetMouseEvent event) {
            WidgetAction[] actionsArray = actions.toArray(new WidgetAction[0]);
            State chainState = State.REJECTED;
            for (WidgetAction action : actionsArray) {
                State state = action.mousePressed(widget, event);
                if (state.isConsumed())
                    return state;
                if (state.isLockedInChain())
                    chainState = State.CONSUMED;
            }
            return chainState;
        }

        /**
         * Called for handling a mouseReleased event.
         * @param widget the widget where the action is assigned
         * @param event  the mouse event
         * @return the event state
         */
        public State mouseReleased(Widget widget, WidgetMouseEvent event) {
            WidgetAction[] actionsArray = actions.toArray(new WidgetAction[0]);
            State chainState = State.REJECTED;
            for (WidgetAction action : actionsArray) {
                State state = action.mouseReleased(widget, event);
                if (state.isConsumed())
                    return state;
                if (state.isLockedInChain())
                    chainState = State.CONSUMED;
            }
            return chainState;
        }

        /**
         * Called for handling a mouseEntered event.
         * @param widget the widget where the action is assigned
         * @param event  the mouse event
         * @return the event state
         */
        public State mouseEntered(Widget widget, WidgetMouseEvent event) {
            WidgetAction[] actionsArray = actions.toArray(new WidgetAction[0]);
            State chainState = State.REJECTED;
            for (WidgetAction action : actionsArray) {
                State state = action.mouseEntered(widget, event);
                if (state.isConsumed())
                    return state;
                if (state.isLockedInChain())
                    chainState = State.CONSUMED;
            }
            return chainState;
        }

        /**
         * Called for handling a mouseExited event.
         * @param widget the widget where the action is assigned
         * @param event  the mouse event
         * @return the event state
         */
        public State mouseExited(Widget widget, WidgetMouseEvent event) {
            WidgetAction[] actionsArray = actions.toArray(new WidgetAction[0]);
            State chainState = State.REJECTED;
            for (WidgetAction action : actionsArray) {
                State state = action.mouseExited(widget, event);
                if (state.isConsumed())
                    return state;
                if (state.isLockedInChain())
                    chainState = State.CONSUMED;
            }
            return chainState;
        }

        /**
         * Called for handling a mouseMoved event.
         * @param widget the widget where the action is assigned
         * @param event  the mouse event
         * @return the event state
         */
        public State mouseDragged(Widget widget, WidgetMouseEvent event) {
            WidgetAction[] actionsArray = actions.toArray(new WidgetAction[0]);
            State chainState = State.REJECTED;
            for (WidgetAction action : actionsArray) {
                State state = action.mouseDragged(widget, event);
                if (state.isConsumed())
                    return state;
                if (state.isLockedInChain())
                    chainState = State.CONSUMED;
            }
            return chainState;
        }

        /**
         * Called for handling a mouseWheelMoved event.
         * @param widget the widget where the action is assigned
         * @param event  the mouse wheel event
         * @return the event state
         */
        public State mouseMoved(Widget widget, WidgetMouseEvent event) {
            WidgetAction[] actionsArray = actions.toArray(new WidgetAction[0]);
            State chainState = State.REJECTED;
            for (WidgetAction action : actionsArray) {
                State state = action.mouseMoved(widget, event);
                if (state.isConsumed())
                    return state;
                if (state.isLockedInChain())
                    chainState = State.CONSUMED;
            }
            return chainState;
        }

        /**
         * Called for handling a keyTyped event.
         * @param widget the widget where the action is assigned
         * @param event  the key event
         * @return the event state
         */
        public State mouseWheelMoved(Widget widget, WidgetMouseWheelEvent event) {
            WidgetAction[] actionsArray = actions.toArray(new WidgetAction[0]);
            State chainState = State.REJECTED;
            for (WidgetAction action : actionsArray) {
                State state = action.mouseWheelMoved(widget, event);
                if (state.isConsumed())
                    return state;
                if (state.isLockedInChain())
                    chainState = State.CONSUMED;
            }
            return chainState;
        }

        /**
         * Called for handling a keyTyped event.
         * @param widget the widget where the action is assigned
         * @param event  the key event
         * @return the event state
         */
        public State keyTyped(Widget widget, WidgetKeyEvent event) {
            WidgetAction[] actionsArray = actions.toArray(new WidgetAction[0]);
            State chainState = State.REJECTED;
            for (WidgetAction action : actionsArray) {
                State state = action.keyTyped(widget, event);
                if (state.isConsumed())
                    return state;
                if (state.isLockedInChain())
                    chainState = State.CONSUMED;
            }
            return chainState;
        }

        /**
         * Called for handling a keyPressed event.
         * @param widget the widget where the action is assigned
         * @param event  the key event
         * @return the event state
         */
        public State keyPressed(Widget widget, WidgetKeyEvent event) {
            WidgetAction[] actionsArray = actions.toArray(new WidgetAction[0]);
            State chainState = State.REJECTED;
            for (WidgetAction action : actionsArray) {
                State state = action.keyPressed(widget, event);
                if (state.isConsumed())
                    return state;
                if (state.isLockedInChain())
                    chainState = State.CONSUMED;
            }
            return chainState;
        }

        /**
         * Called for handling a keyReleased event.
         * @param widget the widget where the action is assigned
         * @param event  the key event
         * @return the event state
         */
        public State keyReleased(Widget widget, WidgetKeyEvent event) {
            WidgetAction[] actionsArray = actions.toArray(new WidgetAction[0]);
            State chainState = State.REJECTED;
            for (WidgetAction action : actionsArray) {
                State state = action.keyReleased(widget, event);
                if (state.isConsumed())
                    return state;
                if (state.isLockedInChain())
                    chainState = State.CONSUMED;
            }
            return chainState;
        }

        /**
         * Called for handling a focusGained event.
         * @param widget the widget where the action is assigned
         * @param event  the focus event
         * @return the event state
         */
        public State focusGained(Widget widget, WidgetAction.WidgetFocusEvent event) {
            WidgetAction[] actionsArray = actions.toArray(new WidgetAction[0]);
            State chainState = State.REJECTED;
            for (WidgetAction action : actionsArray) {
                State state = action.focusGained(widget, event);
                if (state.isConsumed())
                    return state;
                if (state.isLockedInChain())
                    chainState = State.CONSUMED;
            }
            return chainState;
        }

        /**
         * Called for handling a focusLost event.
         * @param widget the widget where the action is assigned
         * @param event  the focus event
         * @return the event state
         */
        public State focusLost(Widget widget, WidgetAction.WidgetFocusEvent event) {
            WidgetAction[] actionsArray = actions.toArray(new WidgetAction[0]);
            State chainState = State.REJECTED;
            for (WidgetAction action : actionsArray) {
                State state = action.focusLost(widget, event);
                if (state.isConsumed())
                    return state;
                if (state.isLockedInChain())
                    chainState = State.CONSUMED;
            }
            return chainState;
        }

        /**
         * Called for handling a dragEnter event.
         * @param widget the widget where the action is assigned
         * @param event  the drop target drag event
         * @return the event state
         */
        public State dragEnter (Widget widget, WidgetDropTargetDragEvent event) {
            WidgetAction[] actionsArray = actions.toArray (new WidgetAction[0]);
            State chainState = State.REJECTED;
            for (WidgetAction action : actionsArray) {
                State state = action.dragEnter (widget, event);
                if (state.isConsumed ())
                    return state;
                if (state.isLockedInChain ())
                    chainState = State.CONSUMED;
            }
            return chainState;
        }

        /**
         * Called for handling a dragOver event.
         * @param widget the widget where the action is assigned
         * @param event  the drop target drag event
         * @return the event state
         */
        public State dragOver (Widget widget, WidgetDropTargetDragEvent event) {
            WidgetAction[] actionsArray = actions.toArray (new WidgetAction[0]);
            State chainState = State.REJECTED;
            for (WidgetAction action : actionsArray) {
                State state = action.dragOver (widget, event);
                if (state.isConsumed ())
                    return state;
                if (state.isLockedInChain ())
                    chainState = State.CONSUMED;
            }
            return chainState;
        }

        /**
         * Called for handling a dropActionChanged event.
         * @param widget the widget where the action is assigned
         * @param event  the drop target drag event
         * @return the event state
         */
        public State dropActionChanged (Widget widget, WidgetDropTargetDragEvent event) {
            WidgetAction[] actionsArray = actions.toArray (new WidgetAction[0]);
            State chainState = State.REJECTED;
            for (WidgetAction action : actionsArray) {
                State state = action.dropActionChanged (widget, event);
                if (state.isConsumed ())
                    return state;
                if (state.isLockedInChain ())
                    chainState = State.CONSUMED;
            }
            return chainState;
        }

        /**
         * Called for handling a dragExit event.
         * @param widget the widget where the action is assigned
         * @param event  the drop target event
         * @return the event state
         */
        public State dragExit (Widget widget, WidgetDropTargetEvent event) {
            WidgetAction[] actionsArray = actions.toArray (new WidgetAction[0]);
            State chainState = State.REJECTED;
            for (WidgetAction action : actionsArray) {
                State state = action.dragExit (widget, event);
                if (state.isConsumed ())
                    return state;
                if (state.isLockedInChain ())
                    chainState = State.CONSUMED;
            }
            return chainState;
        }

        /**
         * Called for handling a drop event.
         * @param widget the widget where the action is assigned
         * @param event  the drop target drop event
         * @return the event state
         */
        public State drop (Widget widget, WidgetDropTargetDropEvent event) {
            WidgetAction[] actionsArray = actions.toArray (new WidgetAction[0]);
            State chainState = State.REJECTED;
            for (WidgetAction action : actionsArray) {
                State state = action.drop (widget, event);
                if (state.isConsumed ())
                    return state;
                if (state.isLockedInChain ())
                    chainState = State.CONSUMED;
            }
            return chainState;
        }

    }

    /**
     * Represents an widget event.
     */
    public static interface WidgetEvent {

        /**
         * Returns an event id.
         * @return the event id
         */
        public long getEventID ();

    }

    /**
     * Represents an location event used for controlling mouse location.
     */
    public static interface WidgetLocationEvent extends WidgetEvent {

        /**
         * Returns stored location.
         * @return the location
         */
        public Point getPoint ();

        /**
         * Sets a new location. This is called by a event provider only.
         * Do not call this method unless you know what it does exactly.
         * @param point the new location
         */
        public void setPoint (Point point);

        /**
         * Translates the stored location.
         * Do not call this method unless you know what it does exactly.
         * @param x the x-axis addition
         * @param y the y-axis addition
         */
        public void translatePoint (int x, int y);

    }

    /**
     * Represents a mouse event.
     */
    public static final class WidgetMouseEvent implements WidgetLocationEvent {
        
        private long id;
        private MouseEvent event;
        private int x, y;

        /**
         * Creates a mouse event.
         * @param id the event id
         * @param event the Swing event
         */
        public WidgetMouseEvent(long id, MouseEvent event) {
            this.id = id;
            this.event = event;
            x = event.getX();
            y = event.getY();
        }

        /**
         * Returns an event id.
         * @return the event id
         */
        public long getEventID() {
            return id;
        }

        /**
         * Returns stored location.
         * @return the location
         */
        public Point getPoint() {
            return new Point(x, y);
        }

        /**
         * Sets a new location. This is called by a event provider only.
         * Do not call this method unless you know what it does exactly.
         * @param point the new location
         */
        public void setPoint(Point point) {
            x = point.x;
            y = point.y;
        }

        /**
         * Translates the stored location.
         * Do not call this method unless you know what it does exactly.
         * @param x the x-axis addition
         * @param y the y-axis addition
         */
        public void translatePoint(int x, int y) {
            this.x += x;
            this.y += y;
        }

        /**
         * @see MouseEvent
         */
        public int getClickCount() {
            return event.getClickCount();
        }

        /**
         * @see MouseEvent
         */
        public int getButton() {
            return event.getButton();
        }

        /**
         * @see MouseEvent
         */
        public boolean isPopupTrigger() {
            return event.isPopupTrigger();
        }

        /**
         * @see MouseEvent
         */
        public boolean isShiftDown() {
            return event.isShiftDown();
        }

        /**
         * @see MouseEvent
         */
        public boolean isControlDown() {
            return event.isControlDown();
        }

        /**
         * @see MouseEvent
         */
        public boolean isMetaDown() {
            return event.isMetaDown();
        }

        /**
         * @see MouseEvent
         */
        public boolean isAltDown() {
            return event.isAltDown();
        }

        /**
         * @see MouseEvent
         */
        public boolean isAltGraphDown() {
            return event.isAltGraphDown();
        }

        /**
         * @see MouseEvent
         */
        public long getWhen() {
            return event.getWhen();
        }

        /**
         * @see MouseEvent
         */
        public int getModifiers() {
            return event.getModifiers();
        }

        /**
         * @see MouseEvent
         */
        public int getModifiersEx() {
            return event.getModifiersEx();
        }
        
    }
    
    public static final class WidgetMouseWheelEvent implements WidgetLocationEvent {

        private long id;
        private MouseWheelEvent event;
        private int x, y;


        /**
         * Creates a mouse wheel event.
         * @param id the event id
         * @param event the Swing event
         */
        public WidgetMouseWheelEvent(long id, MouseWheelEvent event) {
            this.id = id;
            this.event = event;
            x = event.getX ();
            y = event.getY ();
        }

        /**
         * Returns an event id.
         * @return the event id
         */
        public long getEventID () {
            return id;
        }

        /**
         * Returns stored location.
         * @return the location
         */
        public Point getPoint () {
            return new Point (x, y);
        }

        /**
         * Sets a new location. This is called by a event provider only.
         * Do not call this method unless you know what it does exactly.
         * @param point the new location
         */
        public void setPoint (Point point) {
            x = point.x;
            y = point.y;
        }

        /**
         * Translates the stored location.
         * Do not call this method unless you know what it does exactly.
         * @param x the x-axis addition
         * @param y the y-axis addition
         */
        public void translatePoint (int x, int y) {
            this.x += x;
            this.y += y;
        }

        /**
         * @see MouseEvent
         */
        public int getClickCount () {
            return event.getClickCount ();
        }

        /**
         * @see MouseEvent
         */
        public int getButton () {
            return event.getButton ();
        }

        /**
         * @see MouseEvent
         */
        public boolean isPopupTrigger () {
            return event.isPopupTrigger ();
        }

        /**
         * @see MouseEvent
         */
        public boolean isShiftDown () {
            return event.isShiftDown ();
        }

        /**
         * @see MouseEvent
         */
        public boolean isControlDown () {
            return event.isControlDown ();
        }

        /**
         * @see MouseEvent
         */
        public boolean isMetaDown () {
            return event.isMetaDown ();
        }

        /**
         * @see MouseEvent
         */
        public boolean isAltDown () {
            return event.isAltDown ();
        }

        /**
         * @see MouseEvent
         */
        public boolean isAltGraphDown () {
            return event.isAltGraphDown ();
        }

        /**
         * @see MouseEvent
         */
        public long getWhen () {
            return event.getWhen ();
        }

        /**
         * @see MouseEvent
         */
        public int getModifiers () {
            return event.getModifiers ();
        }

        /**
         * @see MouseEvent
         */
        public int getModifiersEx () {
            return event.getModifiersEx ();
        }

        /**
         * @see MouseWheelEvent
         */
        public int getScrollType() {
            return event.getScrollType();
        }

        /**
         * @see MouseWheelEvent
         */
        public int getScrollAmount() {
            return event.getScrollAmount();
        }

        /**
         * @see MouseWheelEvent
         */
        public int getWheelRotation() {
            return event.getWheelRotation();
        }

        /**
         * @see MouseWheelEvent
         */
        public int getUnitsToScroll() {
            return event.getUnitsToScroll();
        }
        
    }

    /**
     * Represents a key event.
     */
    public static final class WidgetKeyEvent implements WidgetEvent {
        
        private long id;
        private KeyEvent event;

        /**
         * Creates a key event.
         * @param id the event id
         * @param event the Swing event
         */
        public WidgetKeyEvent(long id, KeyEvent event) {
            this.id = id;
            this.event = event;
        }

        /**
         * Returns an event id.
         * @return the event id
         */
        public long getEventID() {
            return id;
        }

        /**
         * @see KeyEvent
         */
        public int getKeyCode() {
            return event.getKeyCode();
        }

        /**
         * @see KeyEvent
         */
        public char getKeyChar() {
            return event.getKeyChar();
        }

        /**
         * @see KeyEvent
         */
        public int getKeyLocation() {
            return event.getKeyLocation();
        }

        /**
         * @see KeyEvent
         */
        public boolean isActionKey() {
            return event.isActionKey();
        }

        /**
         * @see KeyEvent
         */
        public boolean isShiftDown() {
            return event.isShiftDown();
        }

        /**
         * @see KeyEvent
         */
        public boolean isControlDown() {
            return event.isControlDown();
        }

        /**
         * @see KeyEvent
         */
        public boolean isMetaDown() {
            return event.isMetaDown();
        }

        /**
         * @see KeyEvent
         */
        public boolean isAltDown() {
            return event.isAltDown();
        }

        /**
         * @see KeyEvent
         */
        public boolean isAltGraphDown() {
            return event.isAltGraphDown();
        }

        /**
         * @see KeyEvent
         */
        public long getWhen() {
            return event.getWhen();
        }

        /**
         * @see KeyEvent
         */
        public int getModifiers() {
            return event.getModifiers();
        }

        /**
         * @see KeyEvent
         */
        public int getModifiersEx() {
            return event.getModifiersEx();
        }
        
    }

    /**
     * Represents a focus event of a scene view.
     */
    public static final class WidgetFocusEvent implements WidgetEvent {
        
        private long id;
        private FocusEvent event;

        /**
         * Creates a focus event.
         * @param id the event id
         * @param event the Swing event
         */
        public WidgetFocusEvent(long id, FocusEvent event) {
            this.id = id;
            this.event = event;
        }

        /**
         * Returns an event id.
         * @return the event id
         */
        public long getEventID() {
            return id;
        }

        /**
         * @see FocusEvent
         */
        // TODO
        public Object getOppositeComponent() { 
            return event.getOppositeComponent();
        }

        /**
         * @see FocusEvent
         */
        public String paramString() {
            return event.paramString();
        }

        /**
         * @see FocusEvent
         */
        public boolean isTemporary() {
            return event.isTemporary();
        }
    }

    /**
     * Represents a drop target drag event.
     */
    public static final class WidgetDropTargetDragEvent implements WidgetLocationEvent {

        private long id;
        private DropTargetDragEvent event;
        private int x, y;

        /**
         * Creates a drop target drag event.
         * @param id the event id
         * @param event the Swing event
         */
        public WidgetDropTargetDragEvent (long id, DropTargetDragEvent event) {
            this.id = id;
            this.event = event;
            Point location = event.getLocation ();
            x = location.x;
            y = location.y;
        }

        /**
         * Returns an event id.
         * @return the event id
         */
        public long getEventID () {
            return id;
        }

        /**
         * Returns stored location.
         * @return the location
         */
        public Point getPoint () {
            return new Point (x, y);
        }

        /**
         * Sets a new location. This is called by a event provider only.
         * Do not call this method unless you know what it does exactly.
         * @param point the new location
         */
        public void setPoint (Point point) {
            x = point.x;
            y = point.y;
        }

        /**
         * Translates the stored location.
         * Do not call this method unless you know what it does exactly.
         * @param x the x-axis addition
         * @param y the y-axis addition
         */
        public void translatePoint (int x, int y) {
            this.x += x;
            this.y += y;
        }

        /**
         * @see DropTargetDragEvent
         */
        public DataFlavor[] getCurrentDataFlavors () {
            return event.getCurrentDataFlavors ();
        }

        /**
         * @see DropTargetDragEvent
         */
        public List<DataFlavor> getCurrentDataFlavorsAsList () {
            return event.getCurrentDataFlavorsAsList ();
        }

        /**
         * @see DropTargetDragEvent
         */
        public boolean isDataFlavorSupported (DataFlavor df) {
            return event.isDataFlavorSupported (df);
        }

        /**
         * @see DropTargetDragEvent
         */
        public int getSourceActions () {
            return event.getSourceActions ();
        }

        /**
         * @see DropTargetDragEvent
         */
        public int getDropAction () {
            return event.getDropAction ();
        }

        /**
         * @see DropTargetDragEvent
         */
        public Transferable getTransferable () {
            return event.getTransferable ();
        }

        /**
         * @see DropTargetDragEvent
         */
        public DropTargetContext getDropTargetContext () {
            return event.getDropTargetContext ();
        }

        /**
         * @see DropTargetDragEvent
         */
        public void acceptDrag (int dragOperation) {
            event.acceptDrag (dragOperation);
        }

        /**
         * @see DropTargetDragEvent
         */
        public void rejectDrag () {
            event.rejectDrag ();
        }

    }

    /**
     * Represents a drop target drop event.
     */
    public static final class WidgetDropTargetDropEvent implements WidgetLocationEvent {

        private long id;
        private DropTargetDropEvent event;
        private int x, y;

        /**
         * Creates a drop target drop event.
         * @param id the event id
         * @param event the Swing event
         */
        public WidgetDropTargetDropEvent (long id, DropTargetDropEvent event) {
            this.id = id;
            this.event = event;
            Point location = event.getLocation ();
            x = location.x;
            y = location.y;
        }

        /**
         * Returns an event id.
         * @return the event id
         */
        public long getEventID () {
            return id;
        }

        /**
         * Returns stored location.
         * @return the location
         */
        public Point getPoint () {
            return new Point (x, y);
        }

        /**
         * Sets a new location. This is called by a event provider only.
         * Do not call this method unless you know what it does exactly.
         * @param point the new location
         */
        public void setPoint (Point point) {
            x = point.x;
            y = point.y;
        }

        /**
         * Translates the stored location.
         * Do not call this method unless you know what it does exactly.
         * @param x the x-axis addition
         * @param y the y-axis addition
         */
        public void translatePoint (int x, int y) {
            this.x += x;
            this.y += y;
        }

        /**
         * @see DropTargetDropEvent
         */
        public DataFlavor[] getCurrentDataFlavors () {
            return event.getCurrentDataFlavors ();
        }

        /**
         * @see DropTargetDropEvent
         */
        public List<DataFlavor> getCurrentDataFlavorsAsList () {
            return event.getCurrentDataFlavorsAsList ();
        }

        /**
         * @see DropTargetDropEvent
         */
        public boolean isDataFlavorSupported (DataFlavor df) {
            return event.isDataFlavorSupported (df);
        }

        /**
         * @see DropTargetDropEvent
         */
        public int getSourceActions () {
            return event.getSourceActions ();
        }

        /**
         * @see DropTargetDropEvent
         */
        public int getDropAction () {
            return event.getDropAction ();
        }

        /**
         * @see DropTargetDropEvent
         */
        public Transferable getTransferable () {
            return event.getTransferable ();
        }

        /**
         * @see DropTargetDropEvent
         */
        public boolean isLocalTransfer () {
            return event.isLocalTransfer ();
        }

        /**
         * @see DropTargetDropEvent
         */
        public DropTargetContext getDropTargetContext () {
            return event.getDropTargetContext ();
        }

        /**
         * @see DropTargetDropEvent
         */
        public void acceptDrop (int dragOperation) {
            event.acceptDrop (dragOperation);
        }

        /**
         * @see DropTargetDropEvent
         */
        public void rejectDrop () {
            event.rejectDrop ();
        }

    }

    /**
     * Represents a drop target event.
     */
    public static final class WidgetDropTargetEvent implements WidgetEvent {

        private long id;
        private DropTargetEvent event;

        /**
         * Creates a drop target event.
         * @param id the event id
         * @param event the Swing event
         */
        public WidgetDropTargetEvent (long id, DropTargetEvent event) {
            this.id = id;
            this.event = event;
        }

        /**
         * Returns an event id.
         * @return the event id
         */
        public long getEventID () {
            return id;
        }

        /**
         * @see DropTargetEvent
         */
        public DropTargetContext getDropTargetContext () {
            return event.getDropTargetContext ();
        }

    }

}
