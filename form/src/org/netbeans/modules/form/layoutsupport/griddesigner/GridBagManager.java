/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.form.layoutsupport.griddesigner;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.FormUtils;
import org.netbeans.modules.form.FormLoaderSettings;
import org.netbeans.modules.form.MetaComponentCreator;
import org.netbeans.modules.form.RADVisualComponent;
import org.netbeans.modules.form.RADVisualContainer;
import org.netbeans.modules.form.VisualReplicator;
import org.netbeans.modules.form.layoutsupport.LayoutSupportManager;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.AddAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.DeleteColumnAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.DeleteColumnContentAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.DeleteComponentAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.DeleteRowAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.DeleteRowContentAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.EncloseInContainerAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.GridAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.GridActionPerformer;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.InsertColumnAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.InsertRowAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.SplitColumnAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.SplitRowAction;
import org.netbeans.modules.form.project.ClassSource;
import org.openide.nodes.Node;

/**
 * {@code GridManager} for {@code GrigBagLayout} layout manager.
 *
 * @author Jan Stola
 * @author Petr Somol
 */
public class GridBagManager implements GridManager {
    private Container container;
    private GridBagInfoProvider info;
    private GridCustomizer customizer;
    private VisualReplicator replicator;
    private Map<Component,RADVisualComponent> componentMap = new IdentityHashMap<Component,RADVisualComponent>();
    private LayoutSupportManager layoutManager;

    public GridBagManager(VisualReplicator replicator) {
        this.replicator = replicator;
        RADVisualContainer metacont = (RADVisualContainer)replicator.getTopMetaComponent();
        Object bean = replicator.getClonedComponent(metacont);
        this.container = metacont.getContainerDelegate(bean);
        if (!(container.getLayout() instanceof GridBagLayout)) {
            throw new IllegalArgumentException();
        }
        layoutManager =  metacont.getLayoutSupport();
        info = new GridBagInfoProvider(container, layoutManager);
        updateComponentMap();
    }

    private void updateComponentMap() {
        componentMap.clear();
        RADVisualContainer metacont = (RADVisualContainer)replicator.getTopMetaComponent();
        for (RADVisualComponent metacomp : metacont.getSubComponents()) {
            componentMap.put((Component)replicator.getClonedComponent(metacomp), metacomp);
        }
    }
    
    private RADVisualComponent getMetaComponent(Component component) {
        RADVisualComponent metacomp = componentMap.get(component);
        if (metacomp == null) {
            updateComponentMap();
            metacomp = componentMap.get(component);
        }
        return metacomp;
    }

    private Node.Property getProperty(Component component, String propertyName) {
        RADVisualComponent metacomp = getMetaComponent(component);
        for (Node.Property property : metacomp.getConstraintsProperties()) {
            String name = property.getName();
            if (name.endsWith(propertyName)) {
                return property;
            }
        }
        throw new IllegalArgumentException();
    }

    @Override
    public GridBagInfoProvider getGridInfo() {
        return info;
    }

    private void setProperty(Component component, String propertyName, Object value) {
        Node.Property property = getProperty(component, propertyName);
        try {
            property.setValue(value);
        } catch (IllegalAccessException iaex) {
            FormUtils.LOGGER.log(Level.WARNING, iaex.getMessage(), iaex);
        } catch (InvocationTargetException itex) {
            FormUtils.LOGGER.log(Level.WARNING, itex.getMessage(), itex);
        }
    }
    
    private void setLayoutProperty(String propertyName, Object value) {
        Node.Property property = layoutManager.getLayoutProperty(propertyName);
        try {
            property.setValue(value);
        } catch (IllegalAccessException iaex) {
            FormUtils.LOGGER.log(Level.WARNING, iaex.getMessage(), iaex);
        } catch (InvocationTargetException itex) {
            FormUtils.LOGGER.log(Level.WARNING, itex.getMessage(), itex);
        }
    }

    @Override
    public void setGridX(Component component, int gridX) {
        setProperty(component, "gridx", gridX); // NOI18N
    }

    @Override
    public void setGridY(Component component, int gridY) {
        setProperty(component, "gridy", gridY); // NOI18N
    }

    public void updateGridX(Component component, int gridXDiff) {
        int oldGridX = info.getGridX(component);
        if(oldGridX + gridXDiff > 0) {
            setProperty(component, "gridx", oldGridX + gridXDiff); // NOI18N
        } else {
            setProperty(component, "gridx", 0); // NOI18N
        }
    }
    
    public void updateGridY(Component component, int gridYDiff) {
        int oldGridY = info.getGridY(component);
        if(oldGridY + gridYDiff > 0) {
            setProperty(component, "gridy", oldGridY + gridYDiff); // NOI18N
        } else {
            setProperty(component, "gridy", 0); // NOI18N
        }
    }

    @Override
    public void setGridWidth(Component component, int gridWidth) {
        setProperty(component, "gridwidth", gridWidth); // NOI18N
    }

    @Override
    public void setGridHeight(Component component, int gridHeight) {
        setProperty(component, "gridheight", gridHeight); // NOI18N
    }

    public void updateGridWidth(Component component, int gridWDiff) {
        int oldGridW = info.getGridWidth(component);
        if(oldGridW + gridWDiff > 1) {
            setProperty(component, "gridwidth", oldGridW + gridWDiff); // NOI18N
        } else {
            setProperty(component, "gridwidth", 1); // NOI18N
        }
    }
    
    public void updateGridHeight(Component component, int gridHDiff) {
        int oldGridH = info.getGridHeight(component);
        if(oldGridH + gridHDiff > 1) {
            setProperty(component, "gridheight", oldGridH + gridHDiff); // NOI18N
        } else {
            setProperty(component, "gridheight", 1); // NOI18N
        }
    }

    @Override
    public void setGridPosition(Component component, int gridX, int gridY, int gridWidth, int gridHeight) {
        boolean widthAbsolute = !info.getGridWidthRemainder(component) && !info.getGridWidthRelative(component);
        boolean heightAbsolute = !info.getGridHeightRemainder(component) && !info.getGridHeightRelative(component);
        int oldGridX = info.getGridX(component);
        int oldGridY = info.getGridY(component);
        int oldGridWidth = info.getGridWidth(component);
        int oldGridHeight = info.getGridHeight(component);
        int oldXEnd = oldGridX + oldGridWidth;
        int oldYEnd = oldGridY + oldGridHeight;
        // Keeping REMAINDER/RELATIVE size where it seems to be appropriate
        setGridX(component, gridX);
        setGridY(component, gridY);
        if ((widthAbsolute && oldGridWidth != gridWidth) || (oldXEnd != gridX + gridWidth)) {
            setGridWidth(component, gridWidth);
        }
        if ((heightAbsolute && oldGridHeight != gridHeight) || (oldYEnd != gridY + gridHeight)) {
            setGridHeight(component, gridHeight);
        }
    }

    @Override
    public void addComponent(Component component, int gridX, int gridY, int gridWidth, int gridHeight) {
        if (!GridUtils.isPaddingComponent(component)) {
            throw new IllegalArgumentException();
        }
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = gridX;
        c.gridy = gridY;
        c.gridwidth = gridWidth;
        c.gridheight = gridHeight;
        container.add(component, c);
        container.doLayout();
    }

    @Override
    public List<GridAction> designerActions(GridAction.Context context) {
        List<GridAction> actions = new ArrayList<GridAction>();
        if (context == GridAction.Context.COLUMN) {
            GridAction action = new InsertColumnAction(false);
            actions.add(action);
            action = new InsertColumnAction(true);
            actions.add(action);
            action = new DeleteColumnAction();
            actions.add(action);
            action = new DeleteColumnContentAction();
            actions.add(action);
            action = new SplitColumnAction();
            actions.add(action);
        } else if (context == GridAction.Context.ROW) {
            GridAction action = new InsertRowAction(false);
            actions.add(action);
            action = new InsertRowAction(true);
            actions.add(action);
            action = new DeleteRowAction();
            actions.add(action);
            action = new DeleteRowContentAction();
            actions.add(action);
            action = new SplitRowAction();
            actions.add(action);
        } else if (context == GridAction.Context.COMPONENT) {
            GridAction action = new DeleteComponentAction();
            actions.add(action);
            action = new EncloseInContainerAction();
            actions.add(action);
        } else if (context == GridAction.Context.CELL) {
            GridAction action = new AddAction(replicator);
            actions.add(action);
        }
        return actions;
    }

    @Override
    public Container getContainer() {
        return container;
    }

    @Override
    public void removeComponent(Component component) {
        if (!GridUtils.isPaddingComponent(component)) {
            // Padding components are not in the form model
            RADVisualComponent metacomp = getMetaComponent(component);
            metacomp.getFormModel().removeComponent(metacomp, true);
        }
        container.remove(component);
    }

    @Override
    public void insertColumn(int newColumnIndex) {
        boolean gapSupport = info.hasGaps();
        if(gapSupport) {
            int gapWidth = FormLoaderSettings.getInstance().getGapWidth();
            int gapHeight = FormLoaderSettings.getInstance().getGapHeight();
            int gappedColumns = (info.getGapXArrayLength() + 1) / 2;
            int gappedRows = (info.getGapYArrayLength() + 1) / 2;
            setGapArrays(gapWidth, gapHeight, gappedColumns+1, gappedRows);
        }
        for (Component component : getContainer().getComponents()) {
            int x = info.getGridX(component);
            int width = info.getGridWidth(component);
            int newX = x;
            int newWidth = width;
            if (x >= newColumnIndex) {
                newX += gapSupport ? 2 : 1;
            } else if (x+width > newColumnIndex) {
                newWidth += gapSupport ? 2 : 1;
            }
            if ((x != newX) || (width != newWidth)) {
                int y = info.getGridY(component);
                int height = info.getGridHeight(component);
                boolean widthRemainder = info.getGridWidthRemainder(component);
                boolean widthRelative = info.getGridWidthRelative(component);
                setGridPosition(component, newX, y, newWidth, height);
                // Insertion of a column shouldn't override REMAINDER/RELATIVE width
                if (widthRemainder) {
                    setGridWidth(component, GridBagConstraints.REMAINDER);
                }
                if (widthRelative) {
                    setGridWidth(component, GridBagConstraints.RELATIVE);
                }
            }
        }
    }

    @Override
    public void deleteColumn(int columnIndex) {
        boolean gapSupport = info.hasGaps();
        if(gapSupport) {
            int gapWidth = FormLoaderSettings.getInstance().getGapWidth();
            int gapHeight = FormLoaderSettings.getInstance().getGapHeight();
            int gappedColumns = (info.getGapXArrayLength() + 1) / 2;
            int gappedRows = (info.getGapYArrayLength() + 1) / 2;
            setGapArrays(gapWidth, gapHeight, gappedColumns-1, gappedRows);
        }
        for (Component component : getContainer().getComponents()) {
            int x = info.getGridX(component);
            int width = info.getGridWidth(component);
            if (x==columnIndex && width==1) {
                removeComponent(component);
            } else {
                int newX = x;
                int newWidth = width;
                if (x > columnIndex) {
                    newX -= gapSupport ? 2 : 1;
                } else if (x+width > columnIndex) {
                    newWidth -= gapSupport ? 2 : 1;
                }
                if ((x != newX) || (width != newWidth)) {
                    int y = info.getGridY(component);
                    int height = info.getGridHeight(component);
                    boolean widthRemainder = info.getGridWidthRemainder(component);
                    boolean widthRelative = info.getGridWidthRelative(component);
                    setGridPosition(component, newX, y, newWidth, height);
                    // Deletion of a column shouldn't override REMAINDER/RELATIVE width
                    if (widthRemainder) {
                        setGridWidth(component, GridBagConstraints.REMAINDER);
                    }
                    if (widthRelative) {
                        setGridWidth(component, GridBagConstraints.RELATIVE);
                    }
                }
            } 
        }
    }

    @Override
    public void insertRow(int newRowIndex) {
        boolean gapSupport = info.hasGaps();
        if(gapSupport) {
            int gapWidth = FormLoaderSettings.getInstance().getGapWidth();
            int gapHeight = FormLoaderSettings.getInstance().getGapHeight();
            int gappedColumns = (info.getGapXArrayLength() + 1) / 2;
            int gappedRows = (info.getGapYArrayLength() + 1) / 2;
            setGapArrays(gapWidth, gapHeight, gappedColumns, gappedRows+1);
        }
        for (Component component : getContainer().getComponents()) {
            int y = info.getGridY(component);
            int height = info.getGridHeight(component);
            int newY = y;
            int newHeight = height;
            if (y >= newRowIndex) {
                newY += gapSupport ? 2 : 1;
            } else if (y+height > newRowIndex) {
                newHeight += gapSupport ? 2 : 1;
            }
            if ((y != newY) || (height != newHeight)) {
                int x = info.getGridX(component);
                int width = info.getGridWidth(component);
                boolean heightRemainder = info.getGridHeightRemainder(component);
                boolean heightRelative = info.getGridHeightRelative(component);
                setGridPosition(component, x, newY, width, newHeight);
                // Insertion of a row shouldn't override REMAINDER/RELATIVE height
                if (heightRemainder) {
                    setGridHeight(component, GridBagConstraints.REMAINDER);
                }
                if (heightRelative) {
                    setGridHeight(component, GridBagConstraints.RELATIVE);
                }
            }
        }
    }

    @Override
    public void deleteRow(int rowIndex) {
        boolean gapSupport = info.hasGaps();
        if(gapSupport) {
            int gapWidth = FormLoaderSettings.getInstance().getGapWidth();
            int gapHeight = FormLoaderSettings.getInstance().getGapHeight();
            int gappedColumns = (info.getGapXArrayLength() + 1) / 2;
            int gappedRows = (info.getGapYArrayLength() + 1) / 2;
            setGapArrays(gapWidth, gapHeight, gappedColumns, gappedRows-1);
        }
        for (Component component : getContainer().getComponents()) {
            int y = info.getGridY(component);
            int height = info.getGridHeight(component);
            if (y==rowIndex && height==1) {
                removeComponent(component);
            } else {
                int newY = y;
                int newHeight = height;
                if (y > rowIndex) {
                    newY -= gapSupport ? 2 : 1;
                } else if (y+height > rowIndex) {
                    newHeight -= gapSupport ? 2 : 1;
                }
                if ((y != newY) || (height != newHeight)) {
                    int x = info.getGridX(component);
                    int width = info.getGridWidth(component);
                    boolean heightRemainder = info.getGridHeightRemainder(component);
                    boolean heightRelative = info.getGridHeightRelative(component);
                    setGridPosition(component, x, newY, width, newHeight);
                    // Deletion of a row shouldn't override REMAINDER/RELATIVE height
                    if (heightRemainder) {
                        setGridHeight(component, GridBagConstraints.REMAINDER);
                    }
                    if (heightRelative) {
                        setGridHeight(component, GridBagConstraints.RELATIVE);
                    }
                }
            }
        }
    }

    @Override
    public void updateLayout(boolean includingSubcontainers) {
        RADVisualContainer metacont = (RADVisualContainer)replicator.getTopMetaComponent();
        if (includingSubcontainers) {
            for (RADVisualComponent metacomp : metacont.getSubComponents()) {
                if (metacomp instanceof RADVisualContainer) {
                    replicator.updateContainerLayout((RADVisualContainer)metacomp);
                }
            }
        }
        replicator.updateContainerLayout(metacont);
    }

    @Override
    public GridCustomizer getCustomizer(GridActionPerformer performer) {
        if (customizer == null) {
            customizer = createCustomizer(performer);
        }
        return customizer;
    }

    private GridCustomizer createCustomizer(GridActionPerformer performer) {
        return new GridBagCustomizer(this, performer);
    }

    public void setAnchor(Component component, int anchor) {
        setProperty(component, "anchor", anchor); // NOI18N
    }

    public void setFill(Component component, int fill) {
        setProperty(component, "fill", fill); // NOI18N
    }
    
    public void setHorizontalFill(Component component, boolean fill) {
        int oldFill = info.getFill(component);
        if(fill) switch(oldFill) {
            case GridBagConstraints.NONE: oldFill = GridBagConstraints.HORIZONTAL; break;
            case GridBagConstraints.VERTICAL: oldFill = GridBagConstraints.BOTH; break;
        } else switch(oldFill) {
            case GridBagConstraints.HORIZONTAL: oldFill = GridBagConstraints.NONE; break;
            case GridBagConstraints.BOTH: oldFill = GridBagConstraints.VERTICAL; break;
        }
        setProperty(component, "fill", oldFill); // NOI18N
    }
    
    public void setVerticalFill(Component component, boolean fill) {
        int oldFill = info.getFill(component);
        if(fill) switch(oldFill) {
            case GridBagConstraints.NONE: oldFill = GridBagConstraints.VERTICAL; break;
            case GridBagConstraints.HORIZONTAL: oldFill = GridBagConstraints.BOTH; break;
        } else switch(oldFill) {
            case GridBagConstraints.VERTICAL: oldFill = GridBagConstraints.NONE; break;
            case GridBagConstraints.BOTH: oldFill = GridBagConstraints.HORIZONTAL; break;
        }
        setProperty(component, "fill", oldFill); // NOI18N
    }
    
    public void updateIPadX(Component component, int iPadXDiff) {
        int oldIPadX = info.getIPadX(component);
        if(oldIPadX + iPadXDiff > 0) {
            setProperty(component, "ipadx", oldIPadX + iPadXDiff); // NOI18N
        } else {
            setProperty(component, "ipadx", 0); // NOI18N
        }
    }
    
    public void updateIPadY(Component component, int iPadYDiff) {
        int oldIPadY = info.getIPadY(component);
        if(oldIPadY + iPadYDiff > 0) {
            setProperty(component, "ipady", oldIPadY+iPadYDiff); // NOI18N
        } else {
            setProperty(component, "ipady", 0); // NOI18N
        }
    }

    public void setIPadX(Component component, int iPadX) {
        setProperty(component, "ipadx", iPadX); // NOI18N
    }

    public void setIPadY(Component component, int iPadY) {
        setProperty(component, "ipady", iPadY); // NOI18N
    }
    
    public void updateInsets(Component component, Insets diff) {
        Insets insets = info.getInsets(component);
        insets.top = insets.top + diff.top >= 0 ? insets.top + diff.top : 0;
        insets.left = insets.left + diff.left >= 0 ? insets.left + diff.left : 0;
        insets.bottom = insets.bottom + diff.bottom >= 0 ? insets.bottom + diff.bottom : 0;
        insets.right = insets.right + diff.right >= 0 ? insets.right + diff.right : 0;
        setProperty(component, "insets", insets); // NOI18N
    }

    public void resetInsets(Component component, boolean top, boolean left, boolean bottom, boolean right) {
        Insets insets = info.getInsets(component);
        if (top) insets.top = 0;
        if (left) insets.left = 0;
        if (bottom) insets.bottom = 0;
        if (right) insets.right = 0;
        setProperty(component, "insets", insets); // NOI18N
    }
    
    public void setWeightX(Component component, double weight) {
        setProperty(component, "weightx", weight); // NOI18N
    }

    public void setWeightY(Component component, double weight) {
        setProperty(component, "weighty", weight); // NOI18N
    }

    public void updateWeightX(Component component, double diff) {
        double oldWeight = info.getWeightX(component);
        if(oldWeight + diff > 0.0d) {
            setProperty(component, "weightx", oldWeight + diff); // NOI18N
        } else {
            setProperty(component, "weightx", 0.0d); // NOI18N
        }
    }

    public void updateWeightY(Component component, double diff) {
        double oldWeight = info.getWeightY(component);
        if(oldWeight + diff > 0.0d) {
            setProperty(component, "weighty", oldWeight + diff); // NOI18N
        } else {
            setProperty(component, "weighty", 0.0d); // NOI18N
        }
    }

    @Override
    public void addGaps(int gapWidth, int gapHeight) 
    {
        if(info.hasGaps()) {
            return;
        }
        FormLoaderSettings.getInstance().setGapWidth(gapWidth);
        FormLoaderSettings.getInstance().setGapHeight(gapHeight);
        int columnCount = info.getColumnCount();
        int rowCount = info.getRowCount();
        setGapArrays(gapWidth, gapHeight, columnCount, rowCount);
        
        for (Component component : getContainer().getComponents()) {
            int x = info.getGridX(component);
            int y = info.getGridY(component);
            int width = info.getGridWidth(component);
            int height = info.getGridHeight(component);
            int newX = 2 * x;
            int newY = 2 * y;
            int newWidth = width * 2 - 1;
            int newHeight = height * 2 - 1;
            boolean widthRemainder = info.getGridWidthRemainder(component);
            boolean heightRemainder = info.getGridHeightRemainder(component);
            
            setGridPosition(component, newX, newY, newWidth, newHeight);
            // adding gaps shouldn't override REMAINDER/RELATIVE if possible
            if (widthRemainder) {
                setGridWidth(component, GridBagConstraints.REMAINDER);
            }
            if (heightRemainder) {
                setGridHeight(component, GridBagConstraints.REMAINDER);
            }
        }
    }

    @Override
    public void removeGaps() 
    {
        if(!info.hasGaps()) {
            return;
        }
        int gapXArrayLength = info.getGapXArrayLength();
        int gapYArrayLength = info.getGapYArrayLength();
        setLayoutProperty("columnWidths",null);
        setLayoutProperty("rowHeights",null);
        
        int maxX = -1;
        int maxY = -1;
        for (Component component : getContainer().getComponents()) {
            int x = info.getGridX(component);
            int y = info.getGridY(component);
            int width = info.getGridWidth(component);
            int height = info.getGridHeight(component);
            int newX = x / 2;
            int newY = y / 2;
            int newWidth = (width - 1)/2 + 1;
            int newHeight = (height - 1)/2 + 1;
            boolean widthRemainder = info.getGridWidthRemainder(component);
            boolean heightRemainder = info.getGridHeightRemainder(component);
            
            setGridPosition(component, newX, newY, newWidth, newHeight);
            // adding gaps shouldn't override REMAINDER/RELATIVE if possible
            if (widthRemainder) {
                setGridWidth(component, GridBagConstraints.REMAINDER);
            }
            if (heightRemainder) {
                setGridHeight(component, GridBagConstraints.REMAINDER);
            }
            if(maxX == -1 || newX + newWidth - 1 > maxX) {
                maxX = newX + newWidth - 1;
            }
            if(maxY == -1 || newY + newHeight - 1 > maxY) {
                maxY = newY + newHeight - 1;
            }
        }
    }

    void setGapArrays(int gapWidth, int gapHeight, int columnCount, int rowCount) 
    {
        int gappedColumnCount = Math.max( 2 * columnCount - 1, 1 );
        int gappedRowCount = Math.max( 2 * rowCount - 1, 1 );
        
        int columnWidths[] = new int[gappedColumnCount];
        for(int i = 1; i < columnCount; i++) {
            columnWidths[i * 2 - 1] = gapWidth;
        }
        int rowHeights[] = new int[gappedRowCount];
        for(int j = 1; j < rowCount; j++) {
            rowHeights[j * 2 - 1] = gapHeight;
        }        
        setLayoutProperty("columnWidths",columnWidths);
        setLayoutProperty("rowHeights",rowHeights);
    }
    
    @Override
    public void updateGaps(boolean updateComponents) 
    {
        if(!info.hasGaps()) {
            return;
        }
        int gapWidth = FormLoaderSettings.getInstance().getGapWidth();
        int gapHeight = FormLoaderSettings.getInstance().getGapHeight();
        int columnCount = 0;
        int rowCount = 0;
        int lastGapColumnIndex = Math.max( info.getLastGapColumn() - 1, -1 );
        int lastGapRowIndex = Math.max( info.getLastGapRow() - 1, -1 );
        if(updateComponents) {
            // this is to compensate possible breaches of GridBagLayout gap support
            // consistency caused by edits outside the customizer. This may 
            // hapen if the user closes the customizer with gap support on, then
            // adds/removes/changes components in free form editor. Components with
            // RELATIVE position or size may get introduced. Components with positions
            // exceeding the size gap definition arrays (columnWidths, rowHeights)
            // may appear. The correction consists in adjusting component positions
            // so that the new ones are placed correctly, j.e., in non-gap columns/rows.
            // Note that the correction can not be done perfectly because there
            // may not be enough information available, e.g., newly added
            // components with asolute position fitting into gap columns/rows
            // are left so because they can not be distinguished from components
            // placed into gap columns/rows manually on purpose.
            GridUtils.removePaddingComponents(this);
            
            // 1. accomodate new positions outside current gap vectors,
            // absolutize RELATIVE positions that fit into non-gap columns/rows,
            // and record RELATIVE positions pointing into gaps
            SortedSet<Integer> wrongPositionX = new TreeSet<Integer>();
            SortedSet<Integer> wrongPositionY = new TreeSet<Integer>();
            for (Component component : getContainer().getComponents()) {
                boolean relativeX = info.getGridXRelative(component);
                boolean relativeY = info.getGridYRelative(component);
                int x = info.getGridX(component);
                int y = info.getGridY(component);
                if(x <= lastGapColumnIndex && relativeX && info.isGapColumn(x)) {
                    wrongPositionX.add(x);
                }
                if(y <= lastGapRowIndex && relativeY && info.isGapRow(y)) {
                    wrongPositionY.add(y);
                }
                columnCount = Math.max(columnCount, x + 1);
                rowCount = Math.max(rowCount, y + 1);
            }
            // 2. insert new gap columns/rows around the remaining
            // RELATIVE positioned components
            if(columnCount > 0 && rowCount > 0) {
                int moveRightBy[] = new int[columnCount];
                int moveDownBy[] = new int[rowCount];
                for(Iterator iterX = wrongPositionX.iterator(); iterX.hasNext();) {
                    int wrongX = (Integer)iterX.next();
                    for(int i = wrongX; i < columnCount; i++) {
                        if(i == wrongX) {
                            moveRightBy[i]++;
                        } else {
                            moveRightBy[i]+=2;
                        }
                    }
                }
                for(Iterator iterY = wrongPositionY.iterator(); iterY.hasNext();) {
                    int wrongY = (Integer)iterY.next();
                    for(int j = wrongY; j < rowCount; j++) {
                        if(j == wrongY) {
                            moveDownBy[j]++;
                        } else {
                            moveDownBy[j]+=2;
                        }
                    }
                }
                for (Component component : getContainer().getComponents()) {
                    boolean remainderWidth = info.getGridWidthRemainder(component);
                    boolean remainderHeight = info.getGridHeightRemainder(component);
                    int x = info.getGridX(component);
                    int y = info.getGridY(component);
                    int width = info.getGridWidth(component);
                    int height = info.getGridHeight(component);
                    int newX = x + moveRightBy[x];
                    int newY = y + moveDownBy[y];
                    int newWidth = width;
                    int newHeight = height;
                    if(x > lastGapColumnIndex + 1) {
                        newX += x - (lastGapColumnIndex + 1);
                        newWidth = width * 2 - 1;
                    }
                    if(y > lastGapRowIndex + 1) {
                        newY += y - (lastGapRowIndex + 1);
                        newHeight = height * 2 - 1;
                    }
                    setGridPosition(component, newX, newY, newWidth, newHeight);
                    if(remainderWidth) {
                        setGridWidth(component, GridBagConstraints.REMAINDER);
                    }
                    if(remainderHeight) {
                        setGridHeight(component, GridBagConstraints.REMAINDER);
                    }
                    columnCount = Math.max(columnCount, newX + newWidth);
                    rowCount = Math.max(rowCount, newY + newHeight);
                }
                columnCount = Math.max(columnCount, lastGapColumnIndex + 1);
                rowCount = Math.max(rowCount, lastGapRowIndex + 1);
                int gapFreeColumnCount = ( columnCount + 1 + (1 - columnCount % 2)) / 2;
                int gapFreeRowCount = (rowCount + 1 + (1 - rowCount % 2)) / 2;
                setGapArrays(gapWidth, gapHeight, gapFreeColumnCount, gapFreeRowCount);
                GridUtils.addPaddingComponents(this, columnCount, rowCount);
                GridUtils.revalidateGrid(this);
            }
        } else { // updateComponents == false
            columnCount = Math.max(info.getColumnCount(), lastGapColumnIndex + 1);
            rowCount = Math.max(info.getRowCount(), lastGapRowIndex + 1);
            int gapFreeColumnCount = ( columnCount + 1 + (1 - columnCount % 2)) / 2;
            int gapFreeRowCount = (rowCount + 1 + (1 - rowCount % 2)) / 2;
            setGapArrays(gapWidth, gapHeight, gapFreeColumnCount, gapFreeRowCount);
        }
    }

    @Override
    public Container encloseInContainer(Set<Component> components) {
        GridBagLayout layout = (GridBagLayout)container.getLayout();
        RADVisualContainer parent = null;
        FormModel formModel = null;
        int minx = Integer.MAX_VALUE;
        int miny = Integer.MAX_VALUE;
        int maxx = 0;
        int maxy = 0;
        boolean horizontalFill = false;
        boolean verticalFill = false;
        boolean weightx = false;
        boolean weighty = false;
        for (Component comp : components) {
            RADVisualComponent metaComp = componentMap.get(comp);
            parent = metaComp.getParentContainer();
            formModel = metaComp.getFormModel();
            int gridx = info.getGridX(comp);
            int gridy = info.getGridY(comp);
            int gridwidth = info.getGridWidth(comp);
            int gridheight = info.getGridHeight(comp);            
            minx = Math.min(minx, gridx);
            miny = Math.min(miny, gridy);
            maxx = Math.max(maxx, gridx+gridwidth);
            maxy = Math.max(maxy, gridy+gridheight);
            int fill = info.getFill(comp);
            if (fill == GridBagConstraints.BOTH) {
                horizontalFill = true;
                verticalFill = true;
            } else if (fill == GridBagConstraints.HORIZONTAL) {
                horizontalFill = true;
            } else if (fill == GridBagConstraints.VERTICAL) {
                verticalFill = true;
            }
            double wx = info.getWeightX(comp);
            if (wx != 0) {
                weightx = true;
            }
            double wy = info.getWeightY(comp);
            if (wy != 0) {
                weighty = true;
            }
        }
        double[][] weights = layout.getLayoutWeights();
        MetaComponentCreator creator = formModel.getComponentCreator();
        RADVisualContainer panel = (RADVisualContainer)creator.createComponent(
                new ClassSource("javax.swing.JPanel"), parent, null); // NOI18N
        // Set the layout of the panel
        boolean recording = formModel.isUndoRedoRecording();
        try {
            // Issue 190882. No need to undo this layout change.
            // The panel is newly created. Hence, switching undo off makes no harm.
            formModel.setUndoRedoRecording(false);
            creator.createComponent(new ClassSource("java.awt.GridBagLayout"), panel, null); // NOI18N
        } finally {
            formModel.setUndoRedoRecording(recording);
        }
        for (Component comp : components) {
            RADVisualComponent metaComp = componentMap.get(comp);
            creator.moveComponent(metaComp, panel);
        }
        if (minx != 0) {
            for (Component comp : components) {
                int gridx = info.getGridX(comp);
                setGridX(comp, gridx-minx);
            }
        }
        if (miny != 0) {
            for (Component comp : components) {
                int gridy = info.getGridY(comp);
                setGridY(comp, gridy-miny);
            }
        }
        Container clone = (Container)replicator.getClonedComponent(panel);
        if (clone == null) {
            clone = (Container)replicator.createClone(panel);
        }
        componentMap.put(clone, panel);
        setGridX(clone, minx);
        setGridY(clone, miny);
        setGridWidth(clone, maxx-minx);
        setGridHeight(clone, maxy-miny);
        if (horizontalFill && weightx) {
            double totalWeightX = 0;
            for (int i=minx; i<maxx; i++) {
                totalWeightX += weights[0][i];
            }
            setWeightX(clone, totalWeightX);
        }
        if (verticalFill && weighty) {
            double totalWeightY = 0;
            for (int i=miny; i<maxy; i++) {
                totalWeightY += weights[1][i];
            }
            setWeightY(clone, totalWeightY);
        }
        int fill = (horizontalFill
                ? (verticalFill ? GridBagConstraints.BOTH : GridBagConstraints.HORIZONTAL)
                : (verticalFill ? GridBagConstraints.VERTICAL : GridBagConstraints.NONE));
        setFill(clone, fill);
        return clone;
    }

}
