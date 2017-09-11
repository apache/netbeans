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
package org.netbeans.jellytools.properties;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import org.netbeans.jellytools.JellyVersion;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JTableOperator;
import org.openide.ErrorManager;
import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;
import org.openide.nodes.Node;

/**
 * Handles properties in IDE property sheets. Properties are grouped in
 * property sheet. Their are identified by their display names. Once you
 * have created a Property instance you can get value, set a new text value,
 * set a new value by index of possible options or open custom editor.
 * <p>
 * Usage:<br>
 * <pre>
        PropertySheetOperator pso = new PropertySheetOperator("Properties of MyClass");
        Property p = new Property(pso, "Name");
        System.out.println("\nProperty name="+p.getName());
        System.out.println("\nProperty value="+p.getValue());
        p.setValue("ANewValue");
        // set a new value by index where it is applicable
        //p.setValue(2);
        // open custom editor where it is applicable
        //p.openEditor();
 * </pre>
 *
 * @author Jiri.Skrivanek@sun.com
 * @see PropertySheetOperator
 */
public class Property {

    /** Class name of string renderer. */
    public static final String STRING_RENDERER = "org.openide.explorer.propertysheet.RendererFactory$StringRenderer";  // NOI18N
    /** Class name of check box renderer. */
    public static final String CHECKBOX_RENDERER = "org.openide.explorer.propertysheet.RendererFactory$CheckboxRenderer";  // NOI18N
    /** Class name of combo box renderer. */
    public static final String COMBOBOX_RENDERER = "org.openide.explorer.propertysheet.RendererFactory$ComboboxRenderer";  // NOI18N
    /** Class name of radio button renderer. */
    public static final String RADIOBUTTON_RENDERER = "org.openide.explorer.propertysheet.RendererFactory$RadioButtonRenderer";  // NOI18N
    /** Class name of set renderer. */
    public static final String SET_RENDERER = "org.openide.explorer.propertysheet.RendererFactory$SetRenderer";  // NOI18N
    
    /** Instance of Node.Property. */
    protected Node.Property property;
    /** Property sheet where this property resides. */
    protected PropertySheetOperator propertySheetOper;
    
    static {
        // Checks if you run on correct jemmy version. Writes message to jemmy log if not.
        JellyVersion.checkJemmyVersion();
    }
    
    /** Waits for property with given name in specified property sheet.
     * @param propertySheetOper PropertySheetOperator where to find property.
     * @param name property display name
     */
    public Property(PropertySheetOperator propertySheetOper, String name) {
        this.propertySheetOper = propertySheetOper;
        this.property = waitProperty(propertySheetOper, name);
    }
    
    /** Waits for index-th property in specified property sheet.
     * @param propertySheetOper PropertySheetOperator where to find property.
     * @param index index (row number) of property inside property sheet
     *              (starts at 0). If there categories shown in property sheet,
     *              rows occupied by their names must by added to index.
     */
    public Property(PropertySheetOperator propertySheetOper, int index) {
        this.propertySheetOper = propertySheetOper;
        this.property = waitProperty(propertySheetOper, index);
    }
    
    /** Waits for property with given name in specified property sheet.
     * @param propSheetOper PropertySheetOperator where to find property.
     * @param name property display name
     */
    private Node.Property waitProperty(final PropertySheetOperator propSheetOper, final String name) {
        try {
            Waiter waiter = new Waiter(new Waitable() {
                public Object actionProduced(Object param) {
                    Node.Property property = null;
                    JTableOperator table = propSheetOper.tblSheet();
                    for(int row=0;row<table.getRowCount();row++) {
                        if(table.getValueAt(row, 1) instanceof Node.Property) {
                            property = (Node.Property)table.getValueAt(row, 1);
                            if(propSheetOper.getComparator().equals(property.getDisplayName(), name)) {
                                return property;
                            }
                        }
                    }
                    return null;
                }
                public String getDescription() {
                    return("Wait property "+name);
                }
            });
            return (Node.Property)waiter.waitAction(null);
        } catch (InterruptedException e) {
            throw new JemmyException("Interrupted.", e);
        }
    }

    /** Waits for index-th property in specified property sheet.
     * @param propSheetOper PropertySheetOperator where to find property.
     * @param index index (row number) of property inside property sheet
     *              (starts at 0). If there are categories shown in property sheet,
     *              rows occupied by their names must by added to index.
     */
    private Node.Property waitProperty(final PropertySheetOperator propSheetOper, final int index) {
        try {
            Waiter waiter = new Waiter(new Waitable() {
                public Object actionProduced(Object param) {
                    JTableOperator table = propSheetOper.tblSheet();
                    if(table.getRowCount() <= index) {
                        // If table is empty or index out of bounds, 
                        // it returns null to wait until table is populated by values
                        return null;
                    }
                    Object property = table.getValueAt(index, 1);
                    if(property instanceof Node.Property) {
                        return (Node.Property)property;
                    } else {
                        throw new JemmyException("On row "+index+" in table there is no property");
                    }
                }
                public String getDescription() {
                    return("Wait property on row "+index+" in property sheet.");
                }
            });
            //waiter.setOutput(TestOut.getNullOutput());
            return (Node.Property)waiter.waitAction(null);
        } catch (InterruptedException e) {
            throw new JemmyException("Interrupted.", e);
        }
    }
    
    /** Gets display name of this property.
     * It can differ from name given in constructor when only
     * substring of property name is used there.
     * @return display name of property
     */
    public String getName() {
        return property.getDisplayName();
    }
    
    /** Gets string representation of property value.
     * @return value of property
     */
    public String getValue() {
        return getPropertyEditor().getAsText();
    }
    
    /** Sets value of this property to specified text. If a new value is
     * not accepted, an information or error dialog is displayed by IDE.
     * If property is not writable JemmyException is thrown.
     * @param textValue text to be set in property (e.g. "a new value",
     * "a new item from list", "false", "TRUE")
     */
    @SuppressWarnings("unchecked")
    public void setValue(final String textValue) {
        propertySheetOper.getOutput().printTrace("Setting value \""+textValue+
                                                 "\" of property \""+getName()+"\".");
        if(!isEnabled()) {
            throw new JemmyException("Property \""+getName()+"\" is read only.");
        }
        final PropertyEditor pe = getPropertyEditor();
        // run in dispatch thread
        new QueueTool().invokeSmoothly(new Runnable() {
            public void run() {
                try {
                    pe.setAsText(textValue);
                    property.setValue(pe.getValue());
                } catch (IllegalAccessException iae) {
                    ErrorManager.getDefault().notify(ErrorManager.USER, iae);
                } catch (IllegalArgumentException iare) {
                    ErrorManager.getDefault().notify(ErrorManager.USER, iare);
                } catch (InvocationTargetException ite) {
                    ErrorManager.getDefault().notify(ErrorManager.USER, ite);
                } catch (Exception e) {
                    throw new JemmyException("Exception while setting value of property.", e);
                }
            }
        });
    }
    
    /** Sets value of this property by given index.
     * It is applicable for properties which can be changed by combo box.
     * If property doesn't support changing value by index JemmyException
     * is thrown.
     * @param index index of item to be selected from possible options
     */
    public void setValue(int index) {
        String[] tags = getPropertyEditor().getTags();
        if(tags != null) {
            setValue(tags[index]);
        } else {
            throw new JemmyException("Property doesn't support changing value by index.");
        }
    }
    
    /** Opens custom property editor for the property by click on "..." button.
     * It checks whether this property supports custom editor by method
     * {@link #supportsCustomEditor}.
     */
    public void openEditor() {
        final JTableOperator table = propertySheetOper.tblSheet();
        // Need to request focus before selection because invokeCustomEditor action works
        // only when table is focused
        table.makeComponentVisible();
        table.requestFocus();
        table.waitHasFocus();
        // run action in a separate thread in AWT (no block)
        new Thread(new Runnable() {

            @Override
            public void run() {
                new QueueTool().invokeSmoothly(new Runnable() {

                    @Override
                    public void run() {
                        // need to select property first
                        ((javax.swing.JTable) table.getSource()).changeSelection(getRow(), 0, false, false);
                        if (supportsCustomEditor()) {
                            // find action
                            Action customEditorAction = ((JComponent) table.getSource()).getActionMap().get("invokeCustomEditor");  // NOI18N
                            customEditorAction.actionPerformed(new ActionEvent(table.getSource(), 0, null));
                        }
                    }
                });
            }
        }, "Thread to open custom editor no block").start(); // NOI18N
    }
    
    /** Checks whether this property supports custom editor.
     * @return true is property supports custom editor, false otherwise
     */
    public boolean  supportsCustomEditor() {
        return getPropertyEditor().supportsCustomEditor();
    }
    
    /** Sets default value for this property. If default value is not available,
     * it does nothing.
     */
    public void setDefaultValue() {
        try {
            property.restoreDefaultValue();
        } catch (Exception e) {
            throw new JemmyException("Exception while restoring default value.", e);
        }
        /*
        nameButtonOperator().clickForPopup();
        String menuItem = Bundle.getString("org.openide.explorer.propertysheet.Bundle",
        "SetDefaultValue");
        new JPopupMenuOperator().pushMenu(menuItem, "|");
        // need to wait until value button is changed
        new EventTool().waitNoEvent(100);
         */
    }

    /** Returns true if this property is enabled in property sheet, that means
     * it is possible to change its value by inplace editor.
     * @return true if this property is enabled, false otherwise
     */
    public boolean isEnabled() {
        return property.canWrite();
    }
    
    /** Returns true if this property can be edited as text by inplace text field.
     * It can be both for string renderer or combo box renderer.
     * @return true if this property can be edited, false otherwise
     */
    @SuppressWarnings("deprecation")
    public boolean canEditAsText() {
        if (property.canRead() && property.canWrite()) {
            Boolean val = (Boolean)property.getValue("canEditAsText");  // NOI18N
            if (val != null) {
                return val.booleanValue();
            }
            PropertyEditor pe = getPropertyEditor();
            if (pe instanceof EnhancedPropertyEditor && pe.getTags() !=  null) {
                return ((EnhancedPropertyEditor)pe).supportsEditingTaggedValues();
            } else {
                return pe.getTags() == null;
            }
        } else {
            return false;
        }
    }
    
    /** Returns class name of renderer used to render this property. It can
     * be used to determine whether correct renderer is used. Possible values
     * are defined in constants {@link #STRING_RENDERER}, {@link #CHECKBOX_RENDERER},
     * {@link #COMBOBOX_RENDERER}, {@link #RADIOBUTTON_RENDERER}, {@link #SET_RENDERER}.
     * @return class name of renderer used to render this property:
     * <UL>
     * <LI>org.openide.explorer.propertysheet.RendererFactory$StringRenderer</LI>
     * <LI>org.openide.explorer.propertysheet.RendererFactory$CheckboxRenderer</LI>
     * <LI>org.openide.explorer.propertysheet.RendererFactory$ComboboxRenderer</LI>
     * <LI>org.openide.explorer.propertysheet.RendererFactory$RadioButtonRenderer</LI>
     * <LI>org.openide.explorer.propertysheet.RendererFactory$SetRenderer</LI>
     * </UL>
     * @see #STRING_RENDERER
     * @see #CHECKBOX_RENDERER
     * @see #COMBOBOX_RENDERER
     * @see #RADIOBUTTON_RENDERER
     * @see #SET_RENDERER
     */
    public String getRendererName() {
        return getRenderer().getClass().getName();
    }
    
    /** Returns component which represents renderer for this property. */
    private Component getRenderer() {
        final JTableOperator table = propertySheetOper.tblSheet();
        int row = getRow();
        // gets component used to render a value
        TableCellRenderer renderer = table.getCellRenderer(row, 1);
        Component comp = renderer.getTableCellRendererComponent(
                                            (JTable)table.getSource(), 
                                            table.getValueAt(row, 1),
                                            false, 
                                            false, 
                                            row, 
                                            1
        );
        // We need to find a real renderer because it can be embedded
        // in ButtonPanel (supplies custom editor button "...")
        // or IconPanel(supplies property marking).
        try {
            Class clazz = Class.forName("org.openide.explorer.propertysheet.RendererPropertyDisplayer");
            Method findInnermostRendererMethod = clazz.getDeclaredMethod("findInnermostRenderer", new Class[] {JComponent.class});
            findInnermostRendererMethod.setAccessible(true);
            comp = (Component)findInnermostRendererMethod.invoke(null, new Object[] {comp});
        } catch (Exception e) {
            throw new JemmyException("RendererPropertyDisplayer.findInnermostRenderer() by reflection failed.", e);
        }
        return comp;
    }
    
    /** Gets short description for this property. Short description is also 
    * used in tooltip.
    * @return short description for this property.
    */
    public String getShortDescription() {
        return this.property.getShortDescription();
    }

    /* 
    * @return row number of property inside property sheet (starts at 0). 
     * If there are categories shown in property sheet, rows occupied by their 
     * names must by taken into account.
     */
    public int getRow() {
        JTableOperator table = this.propertySheetOper.tblSheet();
        for(int row=0;row<table.getRowCount();row++) {
            if(table.getValueAt(row, 1) instanceof Node.Property) {
                if(this.property == (Node.Property)table.getValueAt(row, 1)) {
                    return row;
                }
            }
        }
        throw new JemmyException("Cannot determine row number of property \""+getName()+"\"");
    }
    
    /** Returns property editor obtained by call PropUtils.getPropertyEditor().
     * It should be safe in any circumstancies (e.g. when IDE starts supporting 
     * XML-based editor registration).
     * @return PropertyEditor instance of this property.
     */
    private PropertyEditor getPropertyEditor() {
        final AtomicReference<PropertyEditor> atomicReference = new AtomicReference<PropertyEditor>();
        new QueueTool().invokeSmoothly(new Runnable() {
            @Override
            public void run() {
                try {
                    Class clazz = Class.forName("org.openide.explorer.propertysheet.PropUtils");
                    Method getPropertyEditorMethod = clazz.getDeclaredMethod("getPropertyEditor", new Class[]{Node.Property.class});
                    getPropertyEditorMethod.setAccessible(true);
                    atomicReference.set((PropertyEditor) getPropertyEditorMethod.invoke(null, new Object[]{property}));
                } catch (Exception e) {
                    throw new JemmyException("PropUtils.getPropertyEditor() by reflection failed.", e);
                }
            }
        });
        return atomicReference.get();
    }
}
