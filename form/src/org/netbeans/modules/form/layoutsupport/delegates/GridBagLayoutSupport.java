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

package org.netbeans.modules.form.layoutsupport.delegates;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.*;
import java.util.*;
import java.util.List;
import java.lang.ref.*;
import java.lang.reflect.*;

import org.netbeans.modules.form.FormAwareEditor;
import org.netbeans.modules.form.FormEditor;
import org.netbeans.modules.form.FormModel;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.codestructure.*;
import org.netbeans.modules.form.FormProperty;
import org.netbeans.modules.form.FormPropertyContext;
import org.netbeans.modules.form.RADVisualContainer;
import org.netbeans.modules.form.editors.PrimitiveTypeArrayEditor;
import org.netbeans.modules.form.layoutsupport.griddesigner.GridDesigner;
import org.netbeans.modules.form.project.ClassPathUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;

/**
 * Support class for GridBagLayout. This is an example of support for layout
 * managers with complex layout constraints for which rather special code
 * structure must be managed - GridBagConstraints require to be set up
 * field by field.
 *
 * @author Tran Duc Trung, Tomas Pavek
 */

public class GridBagLayoutSupport extends AbstractLayoutSupport {

    private GridBagLayout initialLayout;

    private Rectangle customizerBounds;
    private static Reference<GridBagCustomizer.Window> customizerRef;
    private FormProperty[] layoutProperties;

    /** Gets the supported layout manager class - GridBagLayout.
     * @return the class supported by this delegate
     */
    @Override
    public Class getSupportedClass() {
        return GridBagLayout.class;
    }

    private boolean isGridDesignerEnabled() {
        return !Boolean.getBoolean("netbeans.modules.form.oldgriddesigner"); // NOI18N
    }
    
    /** Returns a class of customizer for GridBagLayout.
     * @return layout customizer class
     */
    @Override
    public Class getCustomizerClass() {
        return isGridDesignerEnabled() ? GridDesigner.class : GridBagCustomizer.Window.class;
    }

    /** Creates an instance of customizer for GridBagLayout.
     * @return layout customizer class
     */
    @Override
    public Component getSupportCustomizer() {
        if (isGridDesignerEnabled()) {
            final GridDesigner designer = new GridDesigner();
            RADVisualContainer container = ((LayoutSupportManager)getLayoutContext()).getMetaContainer();
            designer.setDesignedContainer(container);
            DialogDescriptor dd = new DialogDescriptor(designer, NbBundle.getMessage(GridDesigner.class, "GridDesignerWindow.title")); // NOI18N
            dd.setOptions(new Object[] { DialogDescriptor.CLOSED_OPTION });
            Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    customizerBounds = e.getWindow().getBounds();
                    designer.cleanup();
                }
            });
            if (customizerBounds != null) { // set same bounds as last time
                dialog.setBounds(customizerBounds);
                dialog.setPreferredSize(new Dimension(customizerBounds.width, customizerBounds.height)); // so pack() does not change size
            }
            return dialog;
        } else { // the old GridBag customizer
            GridBagCustomizer.Window customizer = null;
            if (customizerRef != null)
                customizer = customizerRef.get();
            if (customizer == null) {
                customizer = new GridBagCustomizer.Window();
                customizerRef = new WeakReference<GridBagCustomizer.Window>(customizer);
            }
            customizer.setObject(this);
            return customizer;   
        }
    }

    @Override
    protected FormProperty[] getProperties() {
        if (layoutProperties == null) {
            FormPropertyContext context = new FormPropertyContext.Component(metaLayout);
            ResourceBundle bundle = getBundle();
            layoutProperties = new FormProperty[] {
                new GBLProperty(context, "columnWidths", int[].class, // NOI18N
                        bundle.getString("PROP_columnWidths"), // NOI18N
                        bundle.getString("HINT_columnWidths")), // NOI18N
                new GBLProperty(context, "rowHeights", int[].class, // NOI18N
                        bundle.getString("PROP_rowHeights"), // NOI18N
                        bundle.getString("HINT_rowHeights")), // NOI18N
                new GBLProperty(context, "columnWeights", double[].class, // NOI18N
                        bundle.getString("PROP_columnWeights"), // NOI18N
                        bundle.getString("HINT_columnWeights")), // NOI18N
                new GBLProperty(context, "rowWeights", double[].class, // NOI18N
                        bundle.getString("PROP_rowWeights"), // NOI18N
                        bundle.getString("HINT_rowWeights")) // NOI18N
            };
        }
        return layoutProperties;
    }

    @Override
    protected Node.Property getProperty(String propName) {
        FormProperty result = null;
        for (FormProperty property : getProperties()) {
            if (property.getName().equals(propName)) {
                result = property;
                break;
            }
        }
        return result;
    }

    @Override
    protected LayoutManager cloneLayoutInstance(Container container, Container containerDelegate) throws Exception {
        GridBagLayout layout = new GridBagLayout();
        for (FormProperty property : getProperties()) {
            ((GBLProperty)property).update(layout);
        }
        return layout;
    }

    @Override
    protected void initializeInstance(LayoutManager initialInstance, boolean initializeProperties)
            throws Exception {
        super.initializeInstance(initialInstance, initializeProperties);
        // the four property we may set are not bean properties, so not handled in AbstractLayoutSupport
        if (initialInstance instanceof GridBagLayout) {
            initialLayout = (GridBagLayout) initialInstance;
            if (initializeProperties) {
                for (FormProperty prop : getProperties()) {
                    if (prop instanceof GBLProperty) {
                        ((GBLProperty)prop).setFromInitial();
                    }
                }
            }
        }
    }

    @Override
    protected boolean isPropertyChangedFromInitial(FormProperty prop) {
        if (prop instanceof GBLProperty) {
            return ((GBLProperty)prop).isChangedFromInitial();
        }
        return prop.isChanged();
    }

    private CodeGroup layoutCode;
    private CodeExpression layoutExpression;
    @Override
    protected CodeExpression createInitLayoutCode(CodeGroup layoutCode) {
        this.layoutCode = layoutCode;
        layoutExpression = getCodeStructure().createExpression(
                getLayoutConstructor(),
                CodeStructure.EMPTY_PARAMS);
        updateLayoutExpression();
        return layoutExpression;
    }

    @Override
    protected void readInitLayoutCode(CodeExpression layoutExp,
                                      CodeGroup layoutCode) {
        this.layoutCode = layoutCode;
        this.layoutExpression = layoutExp;
        Iterator iter = CodeStructure.getDefinedStatementsIterator(layoutExp);
        while (iter.hasNext()) {
            CodeStatement statement = (CodeStatement)iter.next();
            for (FormProperty property : getProperties()) {
                GBLProperty prop = (GBLProperty)property;
                Field propField = prop.getField();
                if (propField.equals(statement.getMetaObject())) {
                    CodeExpression propExp = statement.getStatementParameters()[0];
                    FormCodeSupport.readPropertyExpression(propExp, prop, false);
                }
            }
        }
        updateLayoutExpression();
    }

    void updateLayoutExpression() {
        boolean anyPropertyModified = false;
        layoutCode.removeAll();
        FormProperty[] properties = getProperties();
        for (int i=0; i<properties.length; i++) {
            if (properties[i].isChanged()) {
                anyPropertyModified = true;
                layoutCode.addStatement(getPropertyStatement(i));
            }
        }
        updateLayoutExpressionVariable(anyPropertyModified);
    }

    private void updateLayoutExpressionVariable(boolean variableNeeded) {
        if (variableNeeded) {
            CodeVariable var = layoutExpression.getVariable();
            if (var == null) { // no variable used currently
                var = getCodeStructure().createVariableForExpression(
                        layoutExpression, CodeVariable.LOCAL, defaultLayoutVariableName());
            }
            // add variable assignment code
            layoutCode.addStatement(0, var.getAssignment(layoutExpression));
        } else {
            getCodeStructure().removeExpressionFromVariable(layoutExpression);
        }
    }

    private String defaultLayoutVariableName() {
        LayoutSupportManager manager = (LayoutSupportManager)getLayoutContext();
        RADVisualContainer metaCont = manager.getMetaContainer();
        String name;
        if (metaCont.getFormModel().getTopRADComponent() == metaCont) {
            name = "layout"; // NOI18N
        } else {
            name = metaCont.getName() + "Layout"; // NOI18N
        }
        return name;
    }

    private Constructor getLayoutConstructor() {
        Constructor result = null;
        try {
            result = GridBagLayout.class.getConstructor();
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        }
        return result;
    }

    private CodeStatement[] propertyStatements;
    private CodeStatement getPropertyStatement(int index) {
        FormProperty[] properties = getProperties();
        if (propertyStatements == null)
            propertyStatements = new CodeStatement[properties.length];

        CodeStatement propStatement = propertyStatements[index];
        if (propStatement == null) {
            CodeExpression propExp = getCodeStructure().createExpression(
                    FormCodeSupport.createOrigin(properties[index]));
            Field field = ((GBLProperty)properties[index]).getField();
            propStatement = CodeStructure.createStatement(
                layoutExpression, field, propExp);
            propertyStatements[index] = propStatement;
        }
        return propStatement;
    }

    /** This method is called when switching layout - giving an opportunity to
     * convert the previous constrainst of components to constraints of the new
     * layout (this layout). Conversion from AbsoluteConstraints to
     * GridBagConstraints is implemented here.
     * @param previousConstraints [input] layout constraints of components in
     *                                    the previous layout
     * @param currentConstraints [output] array of converted constraints for
     *                                    the new layout - to be filled
     * @param components [input] real components in a real container having the
     *                           previous layout
     */
    @Override
    public void convertConstraints(LayoutConstraints[] previousConstraints,
                                   LayoutConstraints[] currentConstraints,
                                   Component[] components)
    {
        if (currentConstraints == null || components == null
                || components.length > currentConstraints.length
                || components.length == 0
                || ((previousConstraints != null) && !(previousConstraints[0]
                     instanceof AbsoluteLayoutSupport.AbsoluteLayoutConstraints)))
            return;
        
        List<Integer> xlines = new ArrayList<Integer>();
        List<Integer> ylines = new ArrayList<Integer>();
        
        Rectangle parentbound;
        Container con = components[0].getParent();
        if (con == null) {
            parentbound = components[0].getBounds();
        } else {
            parentbound = con.getBounds();
        }
        
        // Determine the size of the grid
        insertLines(0, xlines);
        insertLines(0, ylines);
        
        for (int i=0; i < components.length; i++) {
            Rectangle ibounds = components[i].getBounds();
            
            if (ibounds.width > 0) {
                insertLines(ibounds.x + ibounds.width, xlines);
            } else {
                insertLines(ibounds.x + 1, xlines);
            }
            
            if (ibounds.height > 0) {
                insertLines(ibounds.y + ibounds.height, ylines);
            } else {
                insertLines(ibounds.y + 1, ylines);
            }
        }
        
        // Determine grid width of components.
        LayoutInfo[] layouts = new LayoutInfo[components.length];
        for (int i=0; i < layouts.length; i++)
            layouts[i] = new LayoutInfo();
        
        for (int i=0; i < xlines.size() - 1; i++) {
            int x1 = xlines.get(i);
            int x2 = xlines.get(i+1);
            
            for (int j=0; j < components.length; j++) {
                Rectangle jbounds = components[j].getBounds();
                if (jbounds.width <= 0) {
                    jbounds.width = 1;
                }
                if (isOverlapped(x1, x2, jbounds.x, jbounds.x + jbounds.width - 1))
                    layouts[j].incGridWidth(i);
            }
        }
        
        // Determine grid height of components.
        for (int i=0; i < ylines.size() - 1; i++) {
            int y1 = ylines.get(i);
            int y2 = ylines.get(i+1);
            
            for (int j=0; j < components.length; j++) {
                Rectangle jbounds = components[j].getBounds();
                if (jbounds.height <= 0) {
                    jbounds.height = 1;
                }
                if (isOverlapped(y1, y2, jbounds.y, jbounds.y + jbounds.height - 1))
                    layouts[j].incGridHeight(i);
            }
        }
        
        // Calculate insets of the components.
        for (int i=0; i < components.length; i++) {
            Rectangle curbounds = components[i].getBounds();
            int lastleft = 0;
            int lasttop = 0;
            
            for (int j = 0; j < components.length; j++) {
                Rectangle jbounds = components[j].getBounds();
                int width = jbounds.width;
                if(width < 0) width = 0;
                if(jbounds.x + width - 1 < curbounds.x){
                    if(jbounds.x + width > lastleft){
                        lastleft = jbounds.x + width;
                    }
                }
                int height = jbounds.height;
                if (height < 0) height = 0;
                if (jbounds.y + height - 1 < curbounds.y) {
                    if(jbounds.y + height > lasttop){
                        lasttop = jbounds.y + height;
                    }
                }
            }
            
            layouts[i].setLeft(curbounds.x - lastleft);
            layouts[i].setTop(curbounds.y - lasttop);
            
            int width = (curbounds.width < 0) ? 0 : curbounds.width;
            int height = (curbounds.height < 0) ? 0 : curbounds.height;

            if (layouts[i].getLastGridX() == xlines.size() - 2) {
                layouts[i].setRight(parentbound.width - curbounds.x - width);
            }
            if (layouts[i].getLastGridY() == ylines.size() - 2) {
                layouts[i].setBottom(parentbound.height - curbounds.y - height);
            }
        }
        
        // GridBagLayout puts the remaining width of the component into the last
        // grid column/row (if the component has weight 0). This would not be
        // a problem for us if it would take components sorted according to
        // their increasing x/y grid coordinate. Unfortunately it takes
        // components sorted according to their increasing grid width/height.
        // This can result in a layout that is much wider/higher then the previous
        // absolute layout. The following code forces the right order by
        // introduction of new (otherwise redundant) grid lines.
        
        LayoutInfoComparator comp = new LayoutInfoComparator(LayoutInfoComparator.XAXIS);
        LayoutInfo [] layoutsX = layouts.clone();
        LayoutInfo [] layoutsY = layouts.clone();
        Arrays.sort(layoutsX, comp);
        comp.cord = LayoutInfoComparator.YAXIS;
        Arrays.sort(layoutsY, comp);
        
        for (int i = 0; i < components.length; i++) {
            int expand = 0;
            int lastgrid = layoutsX[i].getLastGridX();
            for (int j = i + 1; j < components.length; j++) {
                if (layoutsX[j].containsGridX(lastgrid) && (layoutsX[j].getLastGridX() > lastgrid) 
                    && (layoutsX[i].gridwidth >= layoutsX[j].gridwidth)
                    && (expand < layoutsX[i].gridwidth - layoutsX[j].gridwidth + 1)) {
                    expand = layoutsX[i].gridwidth - layoutsX[j].gridwidth + 1;
                }
            }
            if (expand > 0) {
                for (int j = i + 1; j < components.length; j++) {
                    if (layoutsX[j].containsGridX(lastgrid) && layoutsX[j].getLastGridX() > lastgrid) {
                        layoutsX[j].expandGridWidth(expand);
                    } else if (layoutsX[j].gridx > lastgrid) {
                        layoutsX[j].moveGridX(expand);
                    }
                }
            }
            
            expand = 0;
            lastgrid = layoutsY[i].getLastGridY();
            for (int j = i + 1; j < components.length; j++) {
                if (layoutsY[j].containsGridY(lastgrid) && (layoutsY[j].getLastGridY() > lastgrid)
                    && (layoutsY[i].gridheight >= layoutsY[j].gridheight)
                    && (expand < layoutsY[i].gridheight - layoutsY[j].gridheight + 1)) {
                    expand = layoutsY[i].gridheight - layoutsY[j].gridheight + 1;
                }
            }
            if (expand > 0) {
                for (int j = i + 1; j < components.length; j++) {
                    if (layoutsY[j].containsGridY(lastgrid) && layoutsY[j].getLastGridY() > lastgrid) {
                        layoutsY[j].expandGridHeight(expand);
                    } else if(layoutsY[j].gridy > lastgrid) {
                        layoutsY[j].moveGridY(expand);
                    }
                }
            }
        }
	
        // Generate constraints
        for (int i=0; i < components.length; i++) {
            if (Math.max(layouts[i].gridx + layouts[i].gridwidth - 1,
                layouts[i].gridy + layouts[i].gridheight - 1) >= 512) {
                for (int j=0; j<i; j++) {
                    currentConstraints[j] = null; // Reset partially converted constraints
                }
                org.openide.DialogDisplayer.getDefault().notify(
                    new org.openide.NotifyDescriptor.Message(
                        NbBundle.getMessage(AbstractLayoutSupport.class, "MSG_ERR_MoreThan512"))); // NOI18N
                return;
            }
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = layouts[i].gridx;
            gbc.gridy = layouts[i].gridy;
            gbc.gridwidth = layouts[i].gridwidth;
            gbc.gridheight = layouts[i].gridheight;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            
            gbc.insets = new java.awt.Insets(layouts[i].top , layouts[i].left,
            layouts[i].bottom, layouts[i].right);
            
            if (components[i].getClass().getName().equals("javax.swing.JScrollPane")) { // NOI18N
                gbc.weightx = 1.0;
                gbc.weighty = 1.0;
                gbc.fill = java.awt.GridBagConstraints.BOTH;
            }
            
            Rectangle bounds = components[i].getBounds();
            Dimension minsize = components[i].getMinimumSize();
            Dimension prefsize = components[i].getPreferredSize();
            
            if (bounds.width > minsize.width)
                gbc.ipadx = bounds.width - minsize.width;
            else if (bounds.width < prefsize.width)
                gbc.ipadx = bounds.width - prefsize.width;
            if (bounds.height > minsize.height)
                gbc.ipady = bounds.height - minsize.height;
            else if (bounds.height < prefsize.height)
                gbc.ipady = bounds.height - prefsize.height;
            
            currentConstraints[i] = new GridBagLayoutConstraints(gbc);
        }
    }
    
    private static boolean isOverlapped(int border1, int border2,
                                        int compPos1, int compPos2)
    {
        return compPos2 >= border1 && compPos1 < border2;
    }
    
    private static void insertLines(int line, java.util.List<Integer> lines) {
        if (line < 0)
            line = 0;
        for (int i=0; i < lines.size(); i++) {
            int ival = lines.get(i);
            if (line < ival) {
                lines.add(i, new Integer(line));
                return;
            }
            else if (line == ival)
                return;
        }
        lines.add(new Integer(line));
    }
    
    /**
     * Comparator of <code>LayoutInfo</code> objects.
     */
    private static class LayoutInfoComparator implements java.util.Comparator<LayoutInfo>{
        final static int XAXIS = 0;
        final static int YAXIS = 1;
        int cord;
        
        public LayoutInfoComparator(int cord){
            this.cord = cord;
        }
        
        @Override
        public int compare(LayoutInfo layoutleft, LayoutInfo layoutright) {
            if ((layoutleft == null) || (layoutright == null)) return 0;
            if (cord == XAXIS) {
                return layoutleft.getLastGridX() - layoutright.getLastGridX();
            } else {
                return layoutleft.getLastGridY() - layoutright.getLastGridY();
            }
        }
        
    }
 
    /**
     * Layout information for one component.
     */
    private static class LayoutInfo {
        /** Grid coordinates. */
        int gridx, gridy;
        /** Grid width. */
        int gridwidth;
        /** Grid height. */
        int gridheight;
        /** Insets. */
        int top = 0, left = 0, bottom = 0, right = 0;
        
        void setLeft(int left){
            if(left < 0) left = 0;
            this.left = left;
        }
        
        void setTop(int top){
            if(top < 0) top = 0;
            this.top = top;
        }
        
        void setBottom(int bottom){
            if(bottom < 0) bottom = 0;
            this.bottom = bottom;
        }
        
        void setRight(int right){
            if(right < 0) right = 0;
            this.right = right;
        }
        
        void moveGridX(int diff) {
            gridx += diff;
        }
        
        void moveGridY(int diff) {
            gridy += diff;
        }
        
        void expandGridWidth(int diff) {
            gridwidth += diff;
        }
        
        void expandGridHeight(int diff) {
            gridheight += diff;
        }
        
        void incGridWidth(int gridx) {
            if (gridwidth == 0)
                this.gridx = gridx;
            gridwidth++;
        }
        
        void incGridHeight(int gridy) {
            if (gridheight == 0)
                this.gridy = gridy;
            gridheight++;
        }
        
        boolean containsGridX(int grid){
            return ((grid >= gridx) && (grid < gridx + gridwidth));
        }
        
        boolean containsGridY(int grid){
            return ((grid >= gridy) && (grid < gridy + gridheight));
        }
	
        int getLastGridX(){
            return gridx + gridwidth - 1;
        }
        
        int getLastGridY(){
            return gridy + gridheight - 1;
        }
        
    }

    // --------

    /** This method is called from readComponentCode method to read layout
     * constraints of a component from code (GridBagConstraints in this case).
     * @param constrExp CodeExpression object of the constraints (taken from
     *        add method in the code)
     * @param constrCode CodeGroup to be filled with the relevant constraints
     *        initialization code
     * @param compExp CodeExpression of the component for which the constraints
     *        are read (not needed here)
     * @return LayoutConstraints based on information read form code
     */
    @Override
    protected LayoutConstraints readConstraintsCode(CodeExpression constrExp,
                                                    CodeGroup constrCode,
                                                    CodeExpression compExp)
    {
        GridBagLayoutConstraints constr = new GridBagLayoutConstraints();
        // reading is done in GridBagLayoutConstraints
        constr.readCodeExpression(constrExp, constrCode);
        return constr;
    }

    /** Called from createComponentCode method, creates code for a component
     * layout constraints (opposite to readConstraintsCode).
     * @param constrCode CodeGroup to be filled with constraints code
     * @param constr layout constraints metaobject representing the constraints
     * @param compExp CodeExpression object representing the component; not
     *        needed here
     * @return created CodeExpression representing the layout constraints
     */
    @Override
    protected CodeExpression createConstraintsCode(CodeGroup constrCode,
                                                   LayoutConstraints constr,
                                                   CodeExpression compExp,
                                                   int index)
    {
        if (!(constr instanceof GridBagLayoutConstraints))
            return null;

        // the code creation is done in GridBagLayoutConstraints
        return ((GridBagLayoutConstraints)constr).createCodeExpression(
                                            getCodeStructure(), constrCode);
    }

    /** This method is called to get a default component layout constraints
     * metaobject in case it is not provided (e.g. in addComponents method).
     * @return the default LayoutConstraints object for the supported layout;
     *         null if no component constraints are used
     */
    @Override
    protected LayoutConstraints createDefaultConstraints() {
        return new GridBagLayoutConstraints();
    }

    // -----------------

    /** LayoutConstraints implementation class for GridBagConstraints.
     * GridBagConstraints class is special in that it requires more code
     * statements for initialization (setting up the individual fields).
     *
     * There are two possible code variants: simple and complex.
     * In the simple situation, no parameter of GridBagConstraints is set, so
     * the code looks like:
     *   container.add(component, new GridBagConstraints());
     *
     * In the complex situation, there are some parameters set - this requires
     * additional code statement for each parameter, and also a variable to
     * be used for the constraints object. Then the code looks like:
     *   GridBagConstraints gridBagConstraints;
     *   ...
     *   gridBagConstraints = new GridBagConstraints();
     *   gridBagConstraints.gridx = 1;
     *   gridBagConstraints.gridy = 2;
     *   container.add(component, gridBagConstraints);
     */
    public static class GridBagLayoutConstraints implements LayoutConstraints {
        private GridBagConstraints constraints;

        private GridBagConstraints defaultConstraints = new GridBagConstraints();

        private Property[] properties;

        private CodeExpression constraintsExpression;
        private CodeGroup constraintsCode; // set of all relevant statements
        private CodeStatement[] propertyStatements; // statements for properties

        private static Constructor constrConstructor;

        private static final int variableType = CodeVariable.LOCAL
                                         | CodeVariable.EXPLICIT_DECLARATION;
        private static final int variableMask = CodeVariable.SCOPE_MASK
                                         | CodeVariable.DECLARATION_MASK;
        private static final String defaultVariableName = "gridBagConstraints"; // NOI18N

        public GridBagLayoutConstraints() {
            constraints = new GridBagConstraints();
        }

        public GridBagLayoutConstraints(GridBagConstraints constraints) {
            this.constraints = constraints;
        }

        @Override
        public Node.Property[] getProperties() {
            if (properties == null) {
                createProperties();
                reinstateProperties();
            }
            return properties;
        }

        @Override
        public Object getConstraintsObject() {
            return constraints;
        }

        @Override
        public LayoutConstraints cloneConstraints() {
            return new GridBagLayoutConstraints((GridBagConstraints)
                                                constraints.clone());
        }

        // -------

        /** This method creates code expression for the constraints. It's
         * called from the delegate's createConstraintsCode method.
         * @param codeStructure CodeStructure in which the expression will be
         *        created
         * @param constrCode CodeGroup to be filled with all the initialization
         *        statements
         * @return CodeExpression representing the constraints
         */
        private CodeExpression createCodeExpression(CodeStructure codeStructure,
                                                    CodeGroup constrCode)
        {
            this.constraintsCode = constrCode;
            propertyStatements = null;

            // GridBagConstraints is created by a simple constructor...
            constraintsExpression = codeStructure.createExpression(
                                        getConstraintsConstructor(),
                                        CodeStructure.EMPTY_PARAMS);
            // ...but the additionlly it requires to create the initialization
            // code statements
            updateCodeExpression();

            return constraintsExpression;
        }

        /** This method reads CodeExpression object representing the
         * constraints and also all its initialization statements which are
         * mapped to the constraints properties. It's called from the
         * delegate's readConstraintsCode method.
         * @param constrExp CodeExpression of the constraints
         * @param constrCode CodeGroup to be filled with recognize
         *        initialization statements
         */
        private void readCodeExpression(CodeExpression constrExp,
                                        CodeGroup constrCode)
        {
            constraintsExpression = constrExp;
            constraintsCode = constrCode;
            propertyStatements = null;

//            constrExp.setOrigin(CodeStructure.createOrigin(
//                                        getConstraintsConstructor(),
//                                        CodeStructure.EMPTY_PARAMS));

            getProperties(); // ensure properties are created

            boolean isAnyChanged = false;

            Iterator it = CodeStructure.getDefinedStatementsIterator(constrExp);
            List<CodeStatement> redundantStatements = new ArrayList<CodeStatement>(15);
            while (it.hasNext()) {
                // go through all the statements of constraints code expression
                CodeStatement statement = (CodeStatement) it.next();
                for (int j=0; j < properties.length; j++) {
                    Property prop = properties[j];
                    if (prop.field.equals(statement.getMetaObject())) {
                        // this statement represents a GridBagConstraints field
                        // assignment, we map the corresponding property to it
                        FormCodeSupport.readPropertyStatement(
                                            statement, prop, false);
                        setPropertyStatement(j, statement);
                        if (prop.isChanged()) { // this is a non-default value
                            constrCode.addStatement(statement);
                            isAnyChanged = true;
                        } else { // remove statement for default value
                            redundantStatements.add(statement);
                        }
                        break;
                    }
                }
            }
            for (CodeStatement statement : redundantStatements) {
                CodeStructure.removeStatement(statement);
            }

            setupVariable(isAnyChanged);
        }

        /** This method updates the constraints code according to the
         * properties. This is called at the beginning - when the constraints
         * code expression is created - and then after each change of the
         * constraints properties. This keeps the code consistent with the
         * properties.
         */
        private void updateCodeExpression() {
            if (constraintsCode == null || constraintsExpression == null)
                return;

            constraintsCode.removeAll();

            getProperties(); // ensure properties are created

            boolean isAnyChanged = false;
            for (int i=0; i < properties.length; i++)
                // for each changed property, add the corresponding statement
                // to the code (constraintsCode - instance of CodeGroup)
                if (properties[i].isChanged()) {
                    constraintsCode.addStatement(getPropertyStatement(i));
                    isAnyChanged = true;
                }

            setupVariable(isAnyChanged);
        }

        /** This method returns the code statement corresponding to property
         * of given index. The statement is created if it does not exist yet.
         * @param index index of required statement
         */
        private CodeStatement getPropertyStatement(int index) {
            if (propertyStatements == null)
                propertyStatements = new CodeStatement[properties.length];

            CodeStatement propStatement = propertyStatements[index];
            if (propStatement == null) {
                CodeExpression propExp =
                    constraintsExpression.getCodeStructure().createExpression(
                        FormCodeSupport.createOrigin(properties[index]));

                // statement is field assignment; the property code expression
                // represents the assigned value
                propStatement = CodeStructure.createStatement(
                                    constraintsExpression,
                                    properties[index].field,
                                    propExp);

                propertyStatements[index] = propStatement;
            }
            return propStatement;
        }

        /** Sets the code statement read form code for given property index.
         * @param index index of the corresponding property
         * @param propStatement CodeStatement to be set
         */
        private void setPropertyStatement(int index,
                                          CodeStatement propStatement)
        {
            if (propertyStatements == null)
                propertyStatements = new CodeStatement[properties.length];
            propertyStatements[index] = propStatement;
        }

        /** This method sets up the variable for constraints code expression.
         * The variable is needed only if there's some property changed (i.e.
         * there's some statement in which the variable is used). One variable
         * is used for all GridBagConstraints in the form.
         */
        private void setupVariable(boolean anyChangedProperty) {
            CodeStructure codeStructure =
                constraintsExpression.getCodeStructure();
            CodeVariable var = constraintsExpression.getVariable();

            if (anyChangedProperty) { // there should be a variable
                if (var == null) { // no variable currently used
                    var = findVariable(); // find and reuse variable
                    if (var == null) { // create a new variable
                        var = codeStructure.createVariableForExpression(
                                                constraintsExpression,
                                                variableType,
                                                defaultVariableName);
                    }
                    else { // attach the constraints expression to the variable
                        codeStructure.attachExpressionToVariable(
                                          constraintsExpression, var);
                    }
                }
                // add variable assignment code
                constraintsCode.addStatement(
                                  0, var.getAssignment(constraintsExpression));
            }
            else { // no variable needed
                codeStructure.removeExpressionFromVariable(
                                  constraintsExpression);
            }
        }

        private CodeVariable findVariable() {
            CodeStructure codeStructure =
                constraintsExpression.getCodeStructure();

            // first try "gridBagConstraints" name - this succeeds in most
            // cases (unless the name is used elsewhere or not created yet)
            CodeVariable var = codeStructure.getVariable(defaultVariableName);
            if (var != null
                    && (var.getType() & variableMask) == variableType
                    && GridBagConstraints.class.equals(var.getDeclaredType()))
                return var;

            // try to find variable of corresponding type (time expensive)
            Iterator it = codeStructure.getVariablesIterator(
                                            variableType,
                                            variableMask,
                                            GridBagConstraints.class);
            while (it.hasNext()) {
                var = (CodeVariable) it.next();
                if (var.getName().startsWith(defaultVariableName))
                    return var;
            }

            return null;
        }

        private void createProperties() {
            properties = new Property[] {
                new Property("gridx", // NOI18N
                             Integer.TYPE,
                             getBundle().getString("PROP_gridx"), // NOI18N
                             getBundle().getString("HINT_gridx"), // NOI18N
                             GridPosEditor.class),

                new Property("gridy", // NOI18N
                             Integer.TYPE,
                             getBundle().getString("PROP_gridy"), // NOI18N
                             getBundle().getString("HINT_gridy"), // NOI18N
                             GridPosEditor.class),

                new Property("gridwidth", // NOI18N
                             Integer.TYPE,
                             getBundle().getString("PROP_gridwidth"), // NOI18N
                             getBundle().getString("HINT_gridwidth"), // NOI18N
                             GridSizeEditor.class),

                new Property("gridheight", // NOI18N
                             Integer.TYPE,
                             getBundle().getString("PROP_gridheight"), // NOI18N
                             getBundle().getString("HINT_gridheight"), // NOI18N
                             GridSizeEditor.class),

                new Property("fill", // NOI18N
                             Integer.TYPE,
                             getBundle().getString("PROP_fill"), // NOI18N
                             getBundle().getString("HINT_fill"), // NOI18N
                             FillEditor.class),

                new Property("ipadx", // NOI18N
                             Integer.TYPE,
                             getBundle().getString("PROP_ipadx"), // NOI18N
                             getBundle().getString("HINT_ipadx"), // NOI18N
                             null),

                new Property("ipady", // NOI18N
                              Integer.TYPE,
                              getBundle().getString("PROP_ipady"), // NOI18N
                              getBundle().getString("HINT_ipady"), // NOI18N
                              null),

                new Property("anchor", // NOI18N
                             Integer.TYPE,
                             getBundle().getString("PROP_anchor"), // NOI18N
                             getBundle().getString("HINT_anchor"), // NOI18N
                             AnchorEditor.class),

                new Property("weightx", // NOI18N
                             Double.TYPE,
                             getBundle().getString("PROP_weightx"), // NOI18N
                             getBundle().getString("HINT_weightx"), // NOI18N
                             null),

                new Property("weighty", // NOI18N
                             Double.TYPE,
                             getBundle().getString("PROP_weighty"), // NOI18N
                             getBundle().getString("HINT_weighty"), // NOI18N
                             null),

                new Property("insets", // NOI18N
                             Insets.class,
                             getBundle().getString("PROP_insets"), // NOI18N
                             getBundle().getString("HINT_insets"), // NOI18N
                             null) {
                                @Override
                                public void setTargetValue(Object value) {
                                    // Issue 195715: insets cannot be null
                                    if (value == null) {
                                        String msg = getBundle().getString("MSG_null_insets"); // NOI18N
                                        IllegalArgumentException ex = new IllegalArgumentException(msg);
                                        // Workaround for a screwed code in the property sheet:
                                        // simple 'throw new IAE(msg) results in an empty message
                                        // shown in the displayed dialog.
                                        Exceptions.attachLocalizedMessage(ex, msg);
                                        throw ex;
                                    }
                                    super.setTargetValue(value);
                                }
                             }
            };

            // properties with editable combo box
            properties[0].setValue("canEditAsText", Boolean.TRUE); // NOI18N
            properties[1].setValue("canEditAsText", Boolean.TRUE); // NOI18N
            properties[2].setValue("canEditAsText", Boolean.TRUE); // NOI18N
            properties[3].setValue("canEditAsText", Boolean.TRUE); // NOI18N
        }

        private void reinstateProperties() {
            try {
                for (int i=0; i < properties.length; i++) {
                    FormProperty prop = (FormProperty) properties[i];
                    prop.reinstateProperty();
                }
            }
            catch(IllegalAccessException e1) {} // should not happen
            catch(InvocationTargetException e2) {} // should not happen
        }

        private static Constructor getConstraintsConstructor() {
            if (constrConstructor == null) {
                try {
                    constrConstructor =
                        GridBagConstraints.class.getConstructor(new Class[0]);
                }
                catch (NoSuchMethodException ex) { // should not happen
                    ex.printStackTrace();
                }
            }
            return constrConstructor;
        }

        // ---------

        static class GridBagPropertyContext implements FormPropertyContext {
            private FormPropertyContext delegate;

            GridBagPropertyContext(FormPropertyContext delegate) {
                this.delegate = delegate;
            }

            @Override
            public boolean useMultipleEditors() {
                return false; // Issue 20940
            }

            @Override
            public void initPropertyEditor(PropertyEditor prEd, FormProperty property) {
                delegate.initPropertyEditor(prEd, property);
            }

            @Override
            public FormModel getFormModel() {
                return delegate.getFormModel();
            }

            @Override
            public Object getOwner() {
                return delegate.getOwner();
            }

        }

        /** Property implementation for GridBagLayoutConstraints. Each property
         * is tied to one field of GridBagConstraints. After a change in
         * property, updateCodeExpression is called to reflect the change in
         * the code.
         */
        private class Property extends FormProperty {
            private Field field;
            private Class<? extends PropertyEditor> propertyEditorClass;

            Property(String name, Class type,
                     String displayName, String shortDescription,
                     Class<? extends PropertyEditor> propertyEditorClass)
            {
                super("GridBagLayoutConstraints "+name, type, // NOI18N
                      displayName, shortDescription);
                this.propertyEditorClass = propertyEditorClass;
                try {
                    field = GridBagConstraints.class.getField(name);
                }
                catch (NoSuchFieldException ex) { // should not happen
                    ex.printStackTrace();
                }
            }

            @Override
            public Object getTargetValue() {
                try {
                    return field.get(constraints);
                }
                catch (Exception ex) { // should not happen
                    ex.printStackTrace();
                    return null;
                }
            }

            @Override
            public void setTargetValue(Object value) {
                try {
                    field.set(constraints, value);
                }
                catch (Exception ex) { // should not happen
                    ex.printStackTrace();
                }
            }

            @Override
            public boolean supportsDefaultValue () {
                return true;
            }

            @Override
            public Object getDefaultValue() {
                try {
                    return field.get(defaultConstraints);
                }
                catch (Exception ex) { // should not happen
                    ex.printStackTrace();
                    return null;
                }
            }

            @Override
            public PropertyEditor getExpliciteEditor() {
                if (propertyEditorClass == null)
                    return null;
                try {
                    return propertyEditorClass.newInstance();
                }
                catch (Exception ex) { //should not happen
                    ex.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void propertyValueChanged(Object old, Object current) {
                // #36932 - GridBagLayout allows max. 512 grid size
                if (current instanceof Integer) {
                    String name = getName();
                    if (((name.endsWith("gridx") || name.endsWith("gridwidth")) // NOI18N
                         && constraints.gridx + constraints.gridwidth > 512)
                     || ((name.endsWith("gridy") || name.endsWith("gridheight")) // NOI18N
                         && constraints.gridy + constraints.gridheight > 512))
                    {
                        boolean fire = isChangeFiring();
                        setChangeFiring(false);
                        try {
                            setValue(old);
                        }
                        catch (Exception ex) {} // should not happen
                        setChangeFiring(fire);
                        return;
                    }
                }

                if (isChangeFiring())
                    updateCodeExpression();
                super.propertyValueChanged(old, current);
            }

            @Override
            public void setPropertyContext(
                org.netbeans.modules.form.FormPropertyContext ctx) {
                // Fix of issue 20940 disabled property context for these properties;
                // I am allowing it again as a part of fix of issue 168201, but
                // keeping support for multiple editors disabled.
                super.setPropertyContext(new GridBagPropertyContext(ctx));
            }
            }
        }

    // ------------
    // property editors for properties of GridBagLayoutConstraints

    private abstract static class GridBagConstrEditor extends PropertyEditorSupport {
        String[] tags;
        Integer[] values;
        String[] javaInitStrings;
        boolean otherValuesAllowed;

        @Override
        public String[] getTags() {
            return tags;
        }

        @Override
        public String getAsText() {
            Object value = getValue();
            for (int i=0; i < values.length; i++)
                if (values[i].equals(value))
                    return tags[i];

            return otherValuesAllowed && value != null ?
                       value.toString() : null;
        }

        @Override
        public void setAsText(String str) {
            for (int i=0; i < tags.length; i++)
                if (tags[i].equals(str)) {
                    setValue(values[i]);
                    return;
                }

            if (otherValuesAllowed)
                try {
                    setValue(new Integer(Integer.parseInt(str)));
                } 
                catch (NumberFormatException e) {} // ignore
        }

        @Override
        public String getJavaInitializationString() {
            Object value = getValue();
            for (int i=0; i < values.length; i++)
                if (values[i].equals(value))
                    return javaInitStrings[i];

            if (!otherValuesAllowed)
                return javaInitStrings[0];
            return value != null ? value.toString() : null;
        }
    }

    static final class GridPosEditor extends GridBagConstrEditor {

        public GridPosEditor() {
            tags = new String[] {
                getBundle().getString("VALUE_relative") // NOI18N
            };
            values = new Integer[] {
                new Integer(GridBagConstraints.RELATIVE)
            };
            javaInitStrings = new String[] {
                "java.awt.GridBagConstraints.RELATIVE" // NOI18N
            };
            otherValuesAllowed = true;
        }
    }

    static final class GridSizeEditor extends GridBagConstrEditor {

        public GridSizeEditor() {
            tags = new String[] {
                getBundle().getString("VALUE_relative"), // NOI18N
                getBundle().getString("VALUE_remainder") // NOI18N
            };
            values = new Integer[] {
                new Integer(GridBagConstraints.RELATIVE),
                new Integer(GridBagConstraints.REMAINDER)
            };
            javaInitStrings = new String[] {
                "java.awt.GridBagConstraints.RELATIVE", // NOI18N
                "java.awt.GridBagConstraints.REMAINDER" // NOI18N
            };
            otherValuesAllowed = true;
        }
    }

    static final class FillEditor extends GridBagConstrEditor {
        public FillEditor() {
            tags = new String[] {
                getBundle().getString("VALUE_fill_none"), // NOI18N
                getBundle().getString("VALUE_fill_horizontal"), // NOI18N
                getBundle().getString("VALUE_fill_vertical"), // NOI18N
                getBundle().getString("VALUE_fill_both") // NOI18N
            };
            values = new Integer[] {
                new Integer(GridBagConstraints.NONE),
                new Integer(GridBagConstraints.HORIZONTAL),
                new Integer(GridBagConstraints.VERTICAL),
                new Integer(GridBagConstraints.BOTH)
            };
            javaInitStrings = new String[] {
                "java.awt.GridBagConstraints.NONE", // NOI18N
                "java.awt.GridBagConstraints.HORIZONTAL", // NOI18N
                "java.awt.GridBagConstraints.VERTICAL", // NOI18N
                "java.awt.GridBagConstraints.BOTH" // NOI18N
            };
            otherValuesAllowed = false;
        }
    }

    static final class AnchorEditor extends GridBagConstrEditor implements FormAwareEditor {

        @Override
        public void setContext(FormModel formModel, FormProperty property) {
            boolean isJDK6Compatible = true;
            if (formModel != null) {
                FileObject formFile = FormEditor.getFormDataObject(formModel).getPrimaryFile();
                isJDK6Compatible = ClassPathUtils.isJava6ProjectPlatform(formFile);
            }

            // Tags
            List<String> tagList = new LinkedList<String>();
            ResourceBundle bundle = getBundle();
            tagList.add(bundle.getString("VALUE_anchor_center")); // NOI18N
            tagList.add(bundle.getString("VALUE_anchor_north")); // NOI18N
            tagList.add(bundle.getString("VALUE_anchor_northeast")); // NOI18N
            tagList.add(bundle.getString("VALUE_anchor_east")); // NOI18N
            tagList.add(bundle.getString("VALUE_anchor_southeast")); // NOI18N
            tagList.add(bundle.getString("VALUE_anchor_south")); // NOI18N
            tagList.add(bundle.getString("VALUE_anchor_southwest")); // NOI18N
            tagList.add(bundle.getString("VALUE_anchor_west")); // NOI18N
            tagList.add(bundle.getString("VALUE_anchor_northwest")); // NOI18N
            tagList.add(bundle.getString("VALUE_anchor_pagestart")); // NOI18N
            tagList.add(bundle.getString("VALUE_anchor_pageend")); // NOI18N
            tagList.add(bundle.getString("VALUE_anchor_linestart")); // NOI18N
            tagList.add(bundle.getString("VALUE_anchor_lineend")); // NOI18N
            tagList.add(bundle.getString("VALUE_anchor_firstlinestart")); // NOI18N
            tagList.add(bundle.getString("VALUE_anchor_firstlineend")); // NOI18N
            tagList.add(bundle.getString("VALUE_anchor_lastlinestart")); // NOI18N
            tagList.add(bundle.getString("VALUE_anchor_lastlineend")); // NOI18N
            if (isJDK6Compatible) {
                tagList.add(bundle.getString("VALUE_anchor_above_baseline")); // NOI18N
                tagList.add(bundle.getString("VALUE_anchor_above_baseline_leading")); // NOI18N
                tagList.add(bundle.getString("VALUE_anchor_above_baseline_trailing")); // NOI18N
                tagList.add(bundle.getString("VALUE_anchor_baseline")); // NOI18N
                tagList.add(bundle.getString("VALUE_anchor_baseline_leading")); // NOI18N
                tagList.add(bundle.getString("VALUE_anchor_baseline_trailing")); // NOI18N
                tagList.add(bundle.getString("VALUE_anchor_below_baseline")); // NOI18N
                tagList.add(bundle.getString("VALUE_anchor_below_baseline_leading")); // NOI18N
                tagList.add(bundle.getString("VALUE_anchor_below_baseline_trailing")); // NOI18N
            }
            tags = tagList.toArray(new String[tagList.size()]);

            // Values
            List<Integer> valueList = new LinkedList<Integer>();
            valueList.add(GridBagConstraints.CENTER);
            valueList.add(GridBagConstraints.NORTH);
            valueList.add(GridBagConstraints.NORTHEAST);
            valueList.add(GridBagConstraints.EAST);
            valueList.add(GridBagConstraints.SOUTHEAST);
            valueList.add(GridBagConstraints.SOUTH);
            valueList.add(GridBagConstraints.SOUTHWEST);
            valueList.add(GridBagConstraints.WEST);
            valueList.add(GridBagConstraints.NORTHWEST);
            valueList.add(GridBagConstraints.PAGE_START);
            valueList.add(GridBagConstraints.PAGE_END);
            valueList.add(GridBagConstraints.LINE_START);
            valueList.add(GridBagConstraints.LINE_END);
            valueList.add(GridBagConstraints.FIRST_LINE_START);
            valueList.add(GridBagConstraints.FIRST_LINE_END);
            valueList.add(GridBagConstraints.LAST_LINE_START);
            valueList.add(GridBagConstraints.LAST_LINE_END);
            if (isJDK6Compatible) {
                // Using hardcoded values to be able to compile on JDK 5
                valueList.add(1024); // GridBagConstraints.ABOVE_BASELINE
                valueList.add(1280); // GridBagConstraints.ABOVE_BASELINE_LEADING
                valueList.add(1536); // GridBagConstraints.ABOVE_BASELINE_TRAILING
                valueList.add(256); // GridBagConstraints.BASELINE
                valueList.add(512); // GridBagConstraints.BASELINE_LEADING
                valueList.add(768); // GridBagConstraints.BASELINE_TRAILING
                valueList.add(1792); // GridBagConstraints.BELOW_BASELINE
                valueList.add(2048); // GridBagConstraints.BELOW_BASELINE_LEADING
                valueList.add(2304); // GridBagConstraints.BELOW_BASELINE_TRAILING
            }
            values = valueList.toArray(new Integer[valueList.size()]);

            // Java initialization strings
            List<String> initStringList = new LinkedList<String>();
            initStringList.add("java.awt.GridBagConstraints.CENTER"); // NOI18N
            initStringList.add("java.awt.GridBagConstraints.NORTH"); // NOI18N
            initStringList.add("java.awt.GridBagConstraints.NORTHEAST"); // NOI18N
            initStringList.add("java.awt.GridBagConstraints.EAST"); // NOI18N
            initStringList.add("java.awt.GridBagConstraints.SOUTHEAST"); // NOI18N
            initStringList.add("java.awt.GridBagConstraints.SOUTH"); // NOI18N
            initStringList.add("java.awt.GridBagConstraints.SOUTHWEST"); // NOI18N
            initStringList.add("java.awt.GridBagConstraints.WEST"); // NOI18N
            initStringList.add("java.awt.GridBagConstraints.NORTHWEST"); // NOI18N
            initStringList.add("java.awt.GridBagConstraints.PAGE_START"); // NOI18N
            initStringList.add("java.awt.GridBagConstraints.PAGE_END"); // NOI18N
            initStringList.add("java.awt.GridBagConstraints.LINE_START"); // NOI18N
            initStringList.add("java.awt.GridBagConstraints.LINE_END"); // NOI18N
            initStringList.add("java.awt.GridBagConstraints.FIRST_LINE_START"); // NOI18N
            initStringList.add("java.awt.GridBagConstraints.FIRST_LINE_END"); // NOI18N
            initStringList.add("java.awt.GridBagConstraints.LAST_LINE_START"); // NOI18N
            initStringList.add("java.awt.GridBagConstraints.LAST_LINE_END"); // NOI18N
            if (isJDK6Compatible) {
                initStringList.add("java.awt.GridBagConstraints.ABOVE_BASELINE"); // NOI18N
                initStringList.add("java.awt.GridBagConstraints.ABOVE_BASELINE_LEADING"); // NOI18N
                initStringList.add("java.awt.GridBagConstraints.ABOVE_BASELINE_TRAILING"); // NOI18N
                initStringList.add("java.awt.GridBagConstraints.BASELINE"); // NOI18N
                initStringList.add("java.awt.GridBagConstraints.BASELINE_LEADING"); // NOI18N
                initStringList.add("java.awt.GridBagConstraints.BASELINE_TRAILING"); // NOI18N
                initStringList.add("java.awt.GridBagConstraints.BELOW_BASELINE"); // NOI18N
                initStringList.add("java.awt.GridBagConstraints.BELOW_BASELINE_LEADING"); // NOI18N
                initStringList.add("java.awt.GridBagConstraints.BELOW_BASELINE_TRAILING"); // NOI18N
            }
            javaInitStrings = initStringList.toArray(new String[initStringList.size()]);

            otherValuesAllowed = false;
        }

        @Override
        public void updateFormVersionLevel() {
        }
    }

    class GBLProperty extends FormProperty {
        private Object value;

        GBLProperty(FormPropertyContext context, String name, Class type, String displayName, String shortDescription) {
            super(context, name, type, displayName, shortDescription);
        }

        @Override
        public Object getTargetValue() {
            return value;
        }

        @Override
        public void setTargetValue(Object value) {
            this.value = value;
        }

        @Override
        public boolean supportsDefaultValue () {
            return true;
        }

        @Override
        public PropertyEditor getExpliciteEditor() {
            Class type = getValueType();
            PropertyEditor propEd = null;
            if (type.equals(int[].class)) {
                propEd = new IntArrayPropertyEditor();
            } else if (type.equals(double[].class)) {
                propEd = new DoubleArrayPropertyEditor();
            } else {
                assert false;
            }
            return propEd;
        }

        @Override
        protected void propertyValueChanged(Object old, Object current) {
            super.propertyValueChanged(old, current);
            if (isChangeFiring()) {
                updateLayoutExpression();
            }
        }

        void update(GridBagLayout instance) {
            try {
                getField().set(instance, value);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        void setFromInitial() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            if (initialLayout != null) {
                Object initValue = getField().get(initialLayout);
                if (initValue != null) {
                    boolean firing = isChangeFiring();
                    try {
                        setChangeFiring(false);
                        setValue(initValue);
                    } finally {
                        setChangeFiring(firing);
                    }
                }
            }
        }

        boolean isChangedFromInitial() {
            if (initialLayout != null) {
                try {
                    Object initValue = getField().get(initialLayout);
                    return !Utilities.compareObjects(value, initValue);
                } catch (IllegalArgumentException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IllegalAccessException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return isChanged();
        }

        Field getField() {
            Field field = null;
            try {
                field = GridBagLayout.class.getField(getName());
            } catch (NoSuchFieldException ex) {
                Exceptions.printStackTrace(ex);
            }
            return field;
        }

    }

    public static class IntArrayPropertyEditor extends PrimitiveTypeArrayEditor {
        public IntArrayPropertyEditor() {
            setValueType(int[].class);
        }
    }

    public static class DoubleArrayPropertyEditor extends PrimitiveTypeArrayEditor {
        public DoubleArrayPropertyEditor() {
            setValueType(double[].class);
        }
    }

    // ------
    // temporary hacks for GridBagCustomizer and GridBagControlCenter

    static ResourceBundle getBundleHack() {
        return getBundle(); // from AbstractLayoutSupport
    }

    LayoutSupportContext getLayoutSupportHack() {
        return super.getLayoutContext();
    }
}
