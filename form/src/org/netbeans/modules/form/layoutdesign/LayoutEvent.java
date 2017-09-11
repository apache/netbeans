/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.form.layoutdesign;

import java.util.EventObject;
import java.util.List;

/**
 * Holds information about a change in the layout model. Is able to undo/redo
 * the change.
 */
abstract class LayoutEvent extends EventObject {

    static final int COMPONENT_ADDED = 1;
    static final int COMPONENT_REMOVED = 2;
    static final int LAYOUT_ROOTS_ADDED = 14;
    static final int LAYOUT_ROOTS_REMOVED = 15;
    static final int LAYOUT_ROOTS_CHANGED = 16;
    static final int INTERVAL_ADDED = 3;
    static final int INTERVAL_REMOVED = 4;
    static final int INTERVAL_ALIGNMENT_CHANGED = 5;
    static final int GROUP_ALIGNMENT_CHANGED = 6;
    static final int INTERVAL_SIZE_CHANGED = 7;
    static final int INTERVAL_PADDING_TYPE_CHANGED = 13;
    static final int INTERVAL_ATTRIBUTES_CHANGED = 8;
    static final int INTERVAL_LINKSIZE_CHANGED = 9;
    static final int CONTAINER_ATTR_CHANGED = 10;
    static final int COMPONENT_REGISTERED = 11;
    static final int COMPONENT_UNREGISTERED = 12;

    private int changeType;

    LayoutEvent(LayoutModel source, int changeType) {
        super(source);
        this.changeType = changeType;
    }

    LayoutModel getModel() {
        return (LayoutModel) source;
    }

    int getType() {
        return changeType;
    }

    abstract void undo();
    abstract void redo();

    // -----

    /**
     * Change event related to an interval.
     */
    static class Interval extends LayoutEvent {
        private LayoutInterval interval;
        private LayoutInterval parentInt;
        private int index;
        private int oldAlignment;
        private int newAlignment;
        private int oldAttributes;
        private int newAttributes;
        private int[] oldSizes;
        private int[] newSizes;
        private LayoutConstants.PaddingType oldPaddingType;
        private LayoutConstants.PaddingType newPaddingType;

        Interval(LayoutModel source, int changeType) {
            super(source, changeType);
        }

        void setInterval(LayoutInterval interval, LayoutInterval parent, int index) {
            this.interval = interval;
            this.parentInt = parent;
            this.index = index;
        }

        void setAlignment(LayoutInterval interval, int oldAlign, int newAlign) {
            this.interval = interval;
            this.oldAlignment = oldAlign;
            this.newAlignment = newAlign;
        }

        void setAttributes(LayoutInterval interval, int oldAttributes, int newAttributes) {
            this.interval = interval;
            this.oldAttributes = oldAttributes;
            this.newAttributes = newAttributes;
        }

        void setSize(LayoutInterval interval,
                     int oldMin, int oldPref, int oldMax,
                     int newMin, int newPref, int newMax)
        {
            this.interval = interval;
            this.oldSizes = new int[] { oldMin, oldPref, oldMax };
            this.newSizes = new int[] { newMin, newPref, newMax };
        }

        void setPaddingType(LayoutInterval interval,
                            LayoutConstants.PaddingType oldPadding,
                            LayoutConstants.PaddingType newPadding)
        {
            this.interval = interval;
            this.oldPaddingType = oldPadding;
            this.newPaddingType = newPadding;
        }

        LayoutInterval getInterval() {
            return interval;
        }

        LayoutInterval getParentInterval() {
            return parentInt;
        }

        int getIndex() {
            return index;
        }

        int getOldPreferredSize() {
            return oldSizes[1];
        }

        int getNewPreferredSize() {
            return newSizes[1];
        }

        @Override
        void undo() {
            switch (getType()) {
                case INTERVAL_ADDED:
                    undoIntervalAddition();
                    break;
                case INTERVAL_REMOVED:
                    undoIntervalRemoval();
                    break;
                case INTERVAL_ALIGNMENT_CHANGED:
                    // getModel().setIntervalAlignment(interval, oldAlignment);
                    interval.setAlignment(oldAlignment);
                    break;
                case GROUP_ALIGNMENT_CHANGED:
                    // getModel().setGroupAlignment(interval, oldAlignment);
                    interval.setGroupAlignment(oldAlignment);
                    break;
                case INTERVAL_SIZE_CHANGED:
                    // getModel().setIntervalSize(interval, oldSizes[0], oldSizes[1], oldSizes[2]);
                    interval.setSizes(oldSizes[0], oldSizes[1], oldSizes[2]);
                    break;
                case INTERVAL_PADDING_TYPE_CHANGED:
                    interval.setPaddingType(oldPaddingType);
                    break;
                case INTERVAL_ATTRIBUTES_CHANGED:
                    interval.setAttributes(oldAttributes);
                    break;
            }
        }

        @Override
        void redo() {
            switch (getType()) {
                case INTERVAL_ADDED:
                    undoIntervalRemoval();
                    break;
                case INTERVAL_REMOVED:
                    undoIntervalAddition();
                    break;
                case INTERVAL_ALIGNMENT_CHANGED:
                    // getModel().setIntervalAlignment(interval, newAlignment);
                    interval.setAlignment(newAlignment);
                    break;
                case GROUP_ALIGNMENT_CHANGED:
                    // getModel().setGroupAlignment(interval, newAlignment);
                    interval.setGroupAlignment(newAlignment);
                    break;
                case INTERVAL_SIZE_CHANGED:
                    // getModel().setIntervalSize(interval, newSizes[0], newSizes[1], newSizes[2]);
                    interval.setSizes(newSizes[0], newSizes[1], newSizes[2]);
                    break;
                case INTERVAL_PADDING_TYPE_CHANGED:
                    interval.setPaddingType(newPaddingType);
                    break;
                case INTERVAL_ATTRIBUTES_CHANGED:
                    interval.setAttributes(newAttributes);
                    break;
            }
        }

        private void undoIntervalAddition() {
            index = getModel().removeInterval(interval);
        }

        private void undoIntervalRemoval() {
            getModel().addInterval(interval, parentInt, index);
        }
    }

    /**
     * Change event related to a component.
     */
    static class Component extends LayoutEvent {
        private LayoutComponent component;
        private LayoutComponent parentComp;
        private int index;
        private List<LayoutInterval[]> oldLayoutRoots;
        private List<LayoutInterval[]> newLayoutRoots;
        private LayoutInterval[] layoutRoots;
        private int oldLinkSizeId;
        private int newLinkSizeId;
        private int dimension;

        Component(LayoutModel source, int changeType) {
            super(source, changeType);
        }

        void setComponent(LayoutComponent comp) {
            this.component = comp;
        }

        void setComponent(LayoutComponent comp, LayoutComponent parent, int index) {
            this.component = comp;
            this.parentComp = parent;
            this.index = index;
        }

        void setContainer(LayoutComponent comp, List<LayoutInterval[]> rootsList) {
            this.component = comp;
            this.newLayoutRoots = rootsList;
        }

        void setLayoutRoots(LayoutComponent container, LayoutInterval[] roots, int index) {
            this.component = container;
            this.layoutRoots = roots;
            this.index = index;
        }

        void setLayoutRoots(LayoutComponent container,
                            List<LayoutInterval[]> oldRoots, List<LayoutInterval[]> newRoots) {
            this.component = container;
            this.oldLayoutRoots = oldRoots;
            this.newLayoutRoots = newRoots;
        }

        void setLinkSizeGroup(LayoutComponent component, int oldLinkSizeId, int newLinkSizeId, int dimension) {
            this.component = component;
            this.oldLinkSizeId = oldLinkSizeId;
            this.newLinkSizeId = newLinkSizeId;
            this.dimension = dimension;
        }

        @Override
        void undo() {
            switch (getType()) {
                case COMPONENT_ADDED:
                    undoComponentAddition();
                    break;
                case COMPONENT_REMOVED:
                    undoComponentRemoval();
                    break;
                case LAYOUT_ROOTS_ADDED:
                    undoRootsAddition();
                    break;
                case LAYOUT_ROOTS_REMOVED:
                    undoRootsRemoval();
                    break;
                case LAYOUT_ROOTS_CHANGED:
                    component.setLayoutRoots(oldLayoutRoots);
                    break;
                case INTERVAL_LINKSIZE_CHANGED:
                    undoLinkSize(oldLinkSizeId);
                    break;
                case CONTAINER_ATTR_CHANGED:
                    changeContainerAttr();
                    break;
                case COMPONENT_REGISTERED:
                    undoComponentRegistration();
                    break;
                case COMPONENT_UNREGISTERED:
                    undoComponentUnregistration();
                    break;
            }
        }

        @Override
        void redo() {
            switch (getType()) {
                case COMPONENT_ADDED:
                    undoComponentRemoval();
                    break;
                case COMPONENT_REMOVED:
                    undoComponentAddition();
                    break;
                case LAYOUT_ROOTS_ADDED:
                    undoRootsRemoval();
                    break;
                case LAYOUT_ROOTS_REMOVED:
                    undoRootsAddition();
                    break;
                case LAYOUT_ROOTS_CHANGED:
                    component.setLayoutRoots(newLayoutRoots);
                    break;
                case INTERVAL_LINKSIZE_CHANGED:
                    undoLinkSize(newLinkSizeId);
                    break;
                case CONTAINER_ATTR_CHANGED:
                    changeContainerAttr();
                    break;
                case COMPONENT_REGISTERED:
                    undoComponentUnregistration();
                    break;
                case COMPONENT_UNREGISTERED:
                    undoComponentRegistration();
                    break;
            }
        }

        private void undoLinkSize(int id) {
            getModel().removeComponentFromLinkSizedGroup(component, dimension);
            if (!(id == LayoutConstants.NOT_EXPLICITLY_DEFINED)) {
                getModel().addComponentToLinkSizedGroupImpl(id, component.getId(), dimension);
            }
        }

        private void undoComponentAddition() {
            getModel().removeComponentImpl(component);
        }

        private void undoComponentRemoval() {
            getModel().addComponentImpl(component, parentComp, index);
        }

        private void undoRootsAddition() {
            component.removeLayoutRoots(layoutRoots);
        }

        private void undoRootsRemoval() {
            component.addLayoutRoots(layoutRoots, index);
        }

        private void changeContainerAttr() {
            boolean toContainer = !component.isLayoutContainer();
            component.setLayoutContainer(toContainer, toContainer ? newLayoutRoots : null);
        }

        private void undoComponentRegistration() {
            getModel().unregisterComponentImpl(component);
        }

        private void undoComponentUnregistration() {
            getModel().registerComponentImpl(component);
        }
    }    
}
