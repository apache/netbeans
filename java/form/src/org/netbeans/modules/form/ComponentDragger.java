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

package org.netbeans.modules.form;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.undo.*;
import java.util.*;
import java.util.List;

import org.netbeans.modules.form.layoutsupport.*;

/**
 *
 * @author Tran Duc Trung
 */

class ComponentDragger
{
    private FormDesigner formDesigner;
    private HandleLayer handleLayer;
    private RADVisualComponent[] selectedComponents;
    private Rectangle[] originalBounds; // in HandleLayer coordinates
    private Point hotspot; // in HandleLayer coordinates
    private Point mousePosition;
    private int resizeType;

    private RADVisualContainer targetMetaContainer;

    private Container targetContainer;
    private Container targetContainerDel;

    static Stroke dashedStroke1 = new BasicStroke((float) 2.0,
                                      BasicStroke.CAP_SQUARE,
                                      BasicStroke.JOIN_MITER,
                                      (float) 10.0,
                                      new float[] { (float) 1.0, (float) 4.0 },
                                      0);

    static Stroke dashedStroke2 = new BasicStroke(
                                      (float) 2.0,
                                      BasicStroke.CAP_SQUARE,
                                      BasicStroke.JOIN_MITER,
                                      (float) 10.0,
                                      new float[] { (float) 2.0, (float) 8.0 },
                                      0);

    /** The FormLoaderSettings instance */
    // constructor for dragging
    ComponentDragger(FormDesigner formDesigner,
                     HandleLayer handleLayer,
                     RADVisualComponent[] selectedComponents,
                     Rectangle[] originalBounds,
                     Point hotspot,
                     RADVisualContainer fixedTargetMetaContainer)
    {
        this.formDesigner = formDesigner;
        this.handleLayer = handleLayer;
        this.selectedComponents = selectedComponents;
        this.originalBounds = originalBounds;
        this.hotspot = hotspot;
        this.mousePosition = hotspot;
        this.resizeType = 0;

        if (fixedTargetMetaContainer != null) {
            targetMetaContainer = fixedTargetMetaContainer;
        }
    }

    // constructor for resizing
    ComponentDragger(FormDesigner formDesigner,
                     HandleLayer handleLayer,
                     RADVisualComponent[] selectedComponents,
                     Rectangle[] originalBounds,
                     Point hotspot,
                     int resizeType)
    {
        this.formDesigner = formDesigner;
        this.handleLayer = handleLayer;
        this.selectedComponents = selectedComponents;
        this.originalBounds = originalBounds;
        this.hotspot = hotspot;
        this.mousePosition = hotspot;
        this.resizeType = resizeType;
    }

    void drag(Point p, RADVisualContainer target) {
        targetMetaContainer = target;
        mousePosition = p;
    }

    void paintDragFeedback(Graphics2D g) {
        Stroke oldStroke = g.getStroke();
        g.setStroke(dashedStroke1);

        Color oldColor = g.getColor();
        g.setColor(FormLoaderSettings.getInstance().getSelectionBorderColor());

        List<LayoutConstraints> constraints = new ArrayList<LayoutConstraints>(selectedComponents.length);
        List<Integer> indices = new ArrayList<Integer>(selectedComponents.length);

        boolean constraintsOK = computeConstraints(mousePosition,
                                                   constraints, indices);

        Point contPos = null;
        LayoutSupportManager layoutSupport = null;
        if (constraintsOK) {
            contPos = SwingUtilities.convertPoint(targetContainerDel, 0, 0, handleLayer);
            layoutSupport = targetMetaContainer.getLayoutSupport();
            if (resizeType == 0)
                paintTargetContainerFeedback(g, targetContainerDel);
        }

        for (int i = 0; i < selectedComponents.length; i++) {
            RADVisualComponent metacomp = selectedComponents[i];
            boolean drawn = false;

            if (constraintsOK) {
                Component comp = (Component) formDesigner.getComponent(metacomp);
                LayoutConstraints constr = constraints.get(i);
                int index = indices.get(i);

                if (constr != null || index >= 0) {
                    g.translate(contPos.x, contPos.y);
                    drawn = layoutSupport.paintDragFeedback(
                                targetContainer, targetContainerDel,
                                comp, constr, index, g);
                    g.translate(- contPos.x, - contPos.y);
                }
//                else continue;
            }

            if (!drawn)
                paintDragFeedback(g, metacomp);
        }

        g.setColor(oldColor);
        g.setStroke(oldStroke);
    }

    void dropComponents(Point point, RADVisualContainer target) {
        List<LayoutConstraints> constraints = null; // constraints of the dragged components
        List<Integer> indices = null; // indices of the dragged components

        targetMetaContainer = target;
        if (targetMetaContainer != null) {
            constraints = new ArrayList<LayoutConstraints>(selectedComponents.length);
            indices = new ArrayList<Integer>(selectedComponents.length);
            computeConstraints(point, constraints, indices);
        }
        if (targetMetaContainer == null) {
//            if (handleLayer.mouseOnVisual(point)) {
//                return;
//            }
            constraints = null;
            indices = null;
        }

        FormModel formModel = formDesigner.getFormModel();

        // LayoutSupportManager of the target container
        LayoutSupportManager layoutSupport = null;
        // original components in the target container
        RADVisualComponent[] originalComponents = null;
        LayoutConstraints[] originalConstraints = null;
        // components moved from other containers
        List<Integer> movedFromOutside = null;
        // components moved within the target container
        List<RADVisualComponent> movedWithinTarget = null;

        if (targetMetaContainer != null) { // target is a visual container
            layoutSupport = targetMetaContainer.getLayoutSupport();
            originalComponents = targetMetaContainer.getSubComponents();
            originalConstraints =
                new LayoutConstraints[originalComponents.length];

            // adjust indices considering that some of dragged components
            // might be already in target container
            adjustIndices(indices);

            // collect components being dragged from other containers
            for (int i=0; i < selectedComponents.length; i++) {
                if (selectedComponents[i].getParentContainer()
                        != targetMetaContainer)
                {
                    if (movedFromOutside == null)
                        movedFromOutside = new ArrayList<Integer>(selectedComponents.length);
                    movedFromOutside.add(i); // remember index
                }
                else {
                    if (movedWithinTarget == null)
                        movedWithinTarget = new ArrayList<RADVisualComponent>(selectedComponents.length);
                    movedWithinTarget.add(selectedComponents[i]);
                }
            }

            // test whether target container accepts new components (dragged
            // from other containers)
            if (movedFromOutside != null && movedFromOutside.size() > 0) {
                int count = movedFromOutside.size();
                RADVisualComponent[] newComps = new RADVisualComponent[count];
                LayoutConstraints[] newConstr = new LayoutConstraints[count];

                for (int i=0; i < count; i++) {
                    int index = movedFromOutside.get(i);
                    newComps[i] = selectedComponents[index];
                    newConstr[i] = constraints.get(index);
                }

                // j - index to selectedComponents for the first new component
                int j = movedFromOutside.get(0);
                // jj - insertion point of the first component in target container
                int jj = indices.get(j);

                try {
                    layoutSupport.acceptNewComponents(newComps, newConstr, jj);
                }
                catch (RuntimeException ex) {
                    // layout support does not accept components
                    org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
                    return;
                }

                // layout support might adjust the constraints
                for (int i=0; i < count; i++) {
                    int index = movedFromOutside.get(i);
                    constraints.set(index, newConstr[i]);
                }

                movedFromOutside.clear(); // for later use
            }
        }

        // create list of components and constraints for target container
        int n = selectedComponents.length
                + (originalComponents != null ? originalComponents.length : 0);
        List<RADVisualComponent> newComponents = new ArrayList<RADVisualComponent>(n);
        List<LayoutConstraints> newConstraints = new ArrayList<LayoutConstraints>(n);
        int newI = 0;

        // fill enough empty space
        for (int i=0; i < n; i++) {
            newComponents.add(null);
            newConstraints.add(null);
        }

        if (targetMetaContainer != null) {
            // add dragged components requiring exact position (index)
            for (int i=0; i < selectedComponents.length; i++) {
                int index = indices.get(i);
                if (index >= 0 && index < n && checkTarget(selectedComponents[i])) {
                    while (newComponents.get(index) != null) // ensure free index
                        if (++index == n)
                            index = 0;

                    newComponents.set(index, selectedComponents[i]);
                }
            }

            // add all components being already in the target container
            for (int i=0; i < originalComponents.length; i++) {
                RADVisualComponent metacomp = originalComponents[i];
                originalConstraints[i] = layoutSupport.getConstraints(metacomp);

                int index = newComponents.indexOf(metacomp);
                if (index < 0) { // not yet in newComponents
                    while (newComponents.get(newI) != null) // ensure free index
                        newI++;

                    newComponents.set(newI, metacomp);
                    newConstraints.set(newI, originalConstraints[i]);
                }
            }
        }

        // add the rest of dragged components (driven by constraints typically)
        for (int i=0; i < selectedComponents.length; i++) {
            RADVisualComponent metacomp = selectedComponents[i];
            int index = newComponents.indexOf(metacomp);
            if (index >= 0) { // already in newComponents
                newConstraints.set(index, constraints != null ?
                                              constraints.get(i) : null);
            }
            else if (checkTarget(metacomp)) { // not yet in newComponents
                while (newComponents.get(newI) != null) // ensure free index
                    newI++;

                newComponents.set(newI, metacomp);
                newConstraints.set(newI, constraints != null ?
                                             constraints.get(i) : null);
            }
        }
        // now we have lists of components and constraints in right order

        List<RADVisualComponent> movedFromOutside2 = null;
        // remove components from source containers
        for (int i=0; i < n; i++) {
            RADVisualComponent metacomp = newComponents.get(i);
            if (metacomp != null) {
                RADVisualContainer parentCont = metacomp.getParentContainer();
                if (parentCont != targetMetaContainer) {
                    if (movedFromOutside2 == null)
                        movedFromOutside2 = new ArrayList<RADVisualComponent>(selectedComponents.length);
                    movedFromOutside2.add(metacomp);

                    formModel.removeComponent(metacomp, false);
                }
            }
            else { // remove empty space
                newComponents.remove(i);
                newConstraints.remove(i);
                i--;
                n--;
            }
        }

        if (n == 0)
            return; // dragging not allowed

        // turn off undo/redo monitoring in FormModel as we provide our own
        // undoable edit for the rest (adding part) of the operation
        boolean undoRedoOn = formModel.isUndoRedoRecording();
        if (undoRedoOn)
            formModel.setUndoRedoRecording(false);

        // prepare arrays of components and constraints (from lists)
        RADVisualComponent[] newCompsArray = new RADVisualComponent[n];
        LayoutConstraints[] newConstrArray = new LayoutConstraints[n];
        
        if (targetMetaContainer != null) { // target is a visual container
            for (int i=0; i < n; i++) {
                newCompsArray[i] = newComponents.get(i);
                newConstrArray[i] = newConstraints.get(i);
            }

            // clear the target layout
            layoutSupport.removeAll();

            // add all components to the target container
            targetMetaContainer.initSubComponents(newCompsArray);
            layoutSupport.addComponents(newCompsArray, newConstrArray, 0);
        }
        else { // no visual target, add the components to Other Components
            ComponentContainer othersMetaCont = formModel.getModelContainer();
            for (int i=0; i < n; i++) {
                newCompsArray[i] = newComponents.get(i);
                othersMetaCont.add(newCompsArray[i]);
                newCompsArray[i].resetConstraintsProperties();
            }
        }

        // fire changes - adding new components
        RADVisualComponent[] compsMovedFromOutside;
        if (movedFromOutside2 != null) {
            n = movedFromOutside2.size();
            compsMovedFromOutside = new RADVisualComponent[n];
            for (int i=0; i < n; i++) {
                compsMovedFromOutside[i] = movedFromOutside2.get(i);
                formModel.fireComponentAdded(compsMovedFromOutside[i], false);
            }
        }
        else {
            compsMovedFromOutside = new RADVisualComponent[0];
            formModel.fireComponentsReordered(targetMetaContainer, new int[0]);
        }

        // fire changes - changing components layout
        RADVisualComponent[] compsMovedWithinTarget;
        if (movedWithinTarget != null) {
            n = movedWithinTarget.size();
            compsMovedWithinTarget = new RADVisualComponent[n];
            for (int i=0; i < n; i++) {
                compsMovedWithinTarget[i] = movedWithinTarget.get(i);
                formModel.fireComponentLayoutChanged(compsMovedWithinTarget[i],
                                                     null, null, null);
            }
        }
        else compsMovedWithinTarget = new RADVisualComponent[0];

        // setup undoable edit
        if (undoRedoOn) {
            DropUndoableEdit dropUndo = new DropUndoableEdit();
            dropUndo.formModel = formModel;
            dropUndo.targetContainer = targetMetaContainer;
            dropUndo.targetComponentsBeforeMove = originalComponents;
            dropUndo.targetConstraintsBeforeMove = originalConstraints;
            dropUndo.targetComponentsAfterMove = newCompsArray;
            dropUndo.targetConstraintsAfterMove = newConstrArray;
            dropUndo.componentsMovedFromOutside = compsMovedFromOutside;
            dropUndo.componentsMovedWithinTarget = compsMovedWithinTarget;

            formModel.addUndoableEdit(dropUndo);
        }

        // finish undoable edit
        if (undoRedoOn) // turn on undo/redo monitoring again
            formModel.setUndoRedoRecording(true);

//        formDesigner.clearSelection(); // Issue 64342
        // select dropped components in designer (after everything updates)
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                formDesigner.setSelectedComponents(selectedComponents);
            }
        });
    }

    // ------------

    private boolean computeConstraints(Point p, List<LayoutConstraints> constraints, List<Integer> indices) {
        if (selectedComponents == null || selectedComponents.length == 0)
            return false;

        if (targetMetaContainer == null)
            return false; // unknown meta-container

        RADVisualContainer fixTargetContainer = null;
        do {
            if (fixTargetContainer != null) {
                targetMetaContainer = fixTargetContainer;
                fixTargetContainer = null;
            }

            LayoutSupportManager layoutSupport = targetMetaContainer.getLayoutSupport();
//            if (layoutSupport == null)
//                return false; // no LayoutSupport (should not happen)

            targetContainer = (Container) formDesigner.getComponent(targetMetaContainer);
            if (targetContainer == null)
                return false; // container not in designer (should not happen)

            targetContainerDel = targetMetaContainer.getContainerDelegate(targetContainer);
            if (targetContainerDel == null)
                return false; // no container delegate (should not happen)

            Point posInCont = SwingUtilities.convertPoint(handleLayer, p, targetContainerDel);

            for (int i = 0; i < selectedComponents.length; i++) {
                LayoutConstraints constr = null;
                int index = -1;

                RADVisualComponent metacomp = selectedComponents[i];
                Component comp = (Component) formDesigner.getComponent(metacomp);

                if (comp != null) {
                    if (!checkTarget(metacomp)) {
                        fixTargetContainer = metacomp.getParentContainer();
                        constraints.clear();
                        indices.clear();
                        if (fixTargetContainer == null)
                            return false; // should not happen
                        break;
                    }

                    if (resizeType == 0) { // dragging
                        Point posInComp = new Point(hotspot.x - originalBounds[i].x,
                                                    hotspot.y - originalBounds[i].y);
                        index = layoutSupport.getNewIndex(
                                    targetContainer, targetContainerDel,
                                    comp, metacomp.getComponentIndex(),
                                    posInCont, posInComp);
                        constr = layoutSupport.getNewConstraints(
                                    targetContainer, targetContainerDel,
                                    comp, metacomp.getComponentIndex(),
                                    posInCont, posInComp);

                        if (constr == null && index >= 0) {
                            // keep old constraints (if compatible)
                            LayoutSupportManager.storeConstraints(metacomp);
                            constr = layoutSupport.getStoredConstraints(metacomp);
                        }
                    }
                    else { // resizing
                        int up = 0, down = 0, left = 0, right = 0;

                        if ((resizeType & LayoutSupportManager.RESIZE_DOWN) != 0)
                            down = p.y - hotspot.y;
                        else if ((resizeType & LayoutSupportManager.RESIZE_UP) != 0)
                            up = hotspot.y - p.y;
                        if ((resizeType & LayoutSupportManager.RESIZE_RIGHT) != 0)
                            right = p.x - hotspot.x;
                        else if ((resizeType & LayoutSupportManager.RESIZE_LEFT) != 0)
                            left = hotspot.x - p.x;

                        Insets sizeChanges = new Insets(up, left, down, right);
                        constr = layoutSupport.getResizedConstraints(
                                     targetContainer, targetContainerDel,
                                     comp, metacomp.getComponentIndex(),
                                     handleLayer.convertRectangleToComponent(new Rectangle(originalBounds[i]), targetContainerDel),
                                     sizeChanges, posInCont);
                    }
                }

                constraints.add(constr);
                indices.add(Integer.valueOf(index));
            }
        }
        while (fixTargetContainer != null);

        return true;
    }

    /** Checks whether metacomp is not a parent of target container (or the
     * target container itself). Used to avoid dragging to a sub-tree.
     * @return true if metacomp is OK
     */
    private boolean checkTarget(RADVisualComponent metacomp) {
        if (!(metacomp instanceof RADVisualContainer))
            return true;

        RADComponent targetCont = targetMetaContainer;
        while (targetCont != null) {
            if (targetCont == metacomp)
                return false;
            targetCont = targetCont.getParentComponent();
        }

        return true;
    }

    /** Modifies suggested indices of dragged components considering the fact
     * that some of the components might be in the target container.
     */
    private void adjustIndices(List<Integer> indices) {
        int index;
        int correction;
        int prevIndex = -1;
        int prevCorrection = 0;

        for (int i=0; i < indices.size(); i++) {
            index = indices.get(i);
            if (index >= 0) {
                if (index == prevIndex) {
                    correction = prevCorrection;
                }
                else {
                    correction = 0;
                    RADVisualComponent[] targetComps =
                                         targetMetaContainer.getSubComponents();
                    for (int j=0; j < index; j++) {
                        RADVisualComponent tComp = targetComps[j];
                        boolean isSelected = false;
                        for (int k=0; k < selectedComponents.length; k++)
                            if (tComp == selectedComponents[k]) {
                                isSelected = true;
                                break;
                            }

                        if (isSelected)
                            correction++;
                    }
                    prevIndex = index;
                    prevCorrection = correction;
                }

                if (correction != 0) {
                    index -= correction;
                    indices.set(i, Integer.valueOf(index));
                }
            }
        }
    }

    private void paintDragFeedback(Graphics2D g, RADVisualComponent metacomp) {
        Object comp = formDesigner.getComponent(metacomp);
        if (!(comp instanceof Component) || !((Component)comp).isShowing())
            return;

        Component component = (Component) comp;

        Rectangle rect = component.getBounds();
        rect = SwingUtilities.convertRectangle(component.getParent(),
                                               rect,
                                               handleLayer);

        rect.translate(mousePosition.x - hotspot.x,
                       mousePosition.y - hotspot.y);
        
        g.draw(new Rectangle2D.Double(rect.x, rect.y, rect.width, rect.height));

        if (metacomp instanceof RADVisualContainer) {
            RADVisualComponent[] children =
                ((RADVisualContainer)metacomp).getSubComponents();
            for (int i = 0; i < children.length; i++) {
                paintDragFeedback(g, children[i]);
            }
        }
    }

    private void paintTargetContainerFeedback(Graphics2D g, Container cont) {
        Stroke oldStroke = g.getStroke();
        g.setStroke(dashedStroke2);

        Color oldColor = g.getColor();
        g.setColor(FormLoaderSettings.getInstance().getDragBorderColor());
        
        Rectangle rect = new Rectangle(new Point(0,0), cont.getSize());
        rect = SwingUtilities.convertRectangle(cont,
                                               rect,
                                               handleLayer);
        g.draw(new Rectangle2D.Double(rect.x, rect.y, rect.width, rect.height));
        g.setColor(oldColor);
        g.setStroke(oldStroke);
    }

    // ---------

    private static class DropUndoableEdit extends AbstractUndoableEdit {

        FormModel formModel;
        RADVisualContainer targetContainer;
        RADVisualComponent[] targetComponentsBeforeMove;
        LayoutConstraints[] targetConstraintsBeforeMove;
        RADVisualComponent[] targetComponentsAfterMove;
        LayoutConstraints[] targetConstraintsAfterMove;
        RADVisualComponent[] componentsMovedFromOutside;
        RADVisualComponent[] componentsMovedWithinTarget;

        @Override
        public void undo() throws CannotUndoException {
            super.undo();

            // turn off undo/redo monitoring in FormModel while undoing!
            boolean undoRedoOn = formModel.isUndoRedoRecording();
            if (undoRedoOn)
                formModel.setUndoRedoRecording(false);

            for (int i=0; i < componentsMovedFromOutside.length; i++)
                formModel.removeComponentImpl(
                                         componentsMovedFromOutside[i], false);

            if (targetContainer != null) {
                LayoutSupportManager layoutSupport =
                    targetContainer.getLayoutSupport();
                layoutSupport.removeAll();
                targetContainer.initSubComponents(targetComponentsBeforeMove);
                layoutSupport.addComponents(targetComponentsBeforeMove,
                                            targetConstraintsBeforeMove,
                                            0);

                for (int i=0; i < componentsMovedWithinTarget.length; i++)
                    formModel.fireComponentLayoutChanged(
                        componentsMovedWithinTarget[i], null, null, null);
            }

            if (undoRedoOn) // turn on undo/redo monitoring again
                formModel.setUndoRedoRecording(true);
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();

            // turn off undo/redo monitoring in FormModel while redoing!
            boolean undoRedoOn = formModel.isUndoRedoRecording();
            if (undoRedoOn)
                formModel.setUndoRedoRecording(false);

            if (targetContainer != null) {
                LayoutSupportManager layoutSupport =
                    targetContainer.getLayoutSupport();
                layoutSupport.removeAll();
                targetContainer.initSubComponents(targetComponentsAfterMove);
                layoutSupport.addComponents(targetComponentsAfterMove,
                                            targetConstraintsAfterMove,
                                            0);
            }
            else {
                ComponentContainer othersMetaCont = formModel.getModelContainer();
                for (int i=0; i < targetComponentsAfterMove.length; i++) {
                    othersMetaCont.add(targetComponentsAfterMove[i]);
                    targetComponentsAfterMove[i].resetConstraintsProperties();
                }
            }

            for (int i=0; i < componentsMovedFromOutside.length; i++)
                formModel.fireComponentAdded(componentsMovedFromOutside[i],
                                             false);

            for (int i=0; i < componentsMovedWithinTarget.length; i++)
                formModel.fireComponentLayoutChanged(
                    componentsMovedWithinTarget[i], null, null, null);

            if (undoRedoOn) // turn on undo/redo monitoring again
                formModel.setUndoRedoRecording(true);
        }

        @Override
        public String getUndoPresentationName() {
            return ""; // NOI18N
        }
        @Override
        public String getRedoPresentationName() {
            return ""; // NOI18N
        }
    }
}
