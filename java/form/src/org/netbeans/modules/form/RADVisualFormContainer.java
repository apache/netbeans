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
import java.beans.*;

import org.openide.nodes.*;

/**
 * RADVisualFormContainer represents the top-level container of the form and
 * the form itself during design time.
 *
 * @author Ian Formanek
 */

public class RADVisualFormContainer extends RADVisualContainer implements FormContainer
{
    public static final String PROP_FORM_SIZE_POLICY = "formSizePolicy"; // NOI18N
    public static final String PROP_FORM_SIZE = "formSize"; // NOI18N
    public static final String PROP_FORM_POSITION = "formPosition"; // NOI18N
    public static final String PROP_GENERATE_POSITION = "generatePosition"; // NOI18N
    public static final String PROP_GENERATE_SIZE = "generateSize"; // NOI18N
    public static final String PROP_GENERATE_CENTER = "generateCenter"; // NOI18N

    public static final int GEN_BOUNDS = 0;
    public static final int GEN_PACK = 1;
    public static final int GEN_NOTHING = 2;

    // Synthetic properties of form
    private Dimension designerSize;
    private Dimension formSize;// = new Dimension(FormEditor.DEFAULT_FORM_WIDTH, FormEditor.DEFAULT_FORM_HEIGHT);
    private Point formPosition;
    private boolean generatePosition = true;
    private boolean generateSize = true;
    private boolean generateCenter = false;
    private int formSizePolicy = GEN_NOTHING;

    // ------------------------------------------------------------------------------
    // Form synthetic properties

    /**
     * Getter for the Name property of the component - overriden to provide
     * non-null value, as the top-level component does not have a variable
     * @return current value of the Name property
     */
    @Override
    public String getName() {
        return FormUtils.getBundleString("CTL_FormTopContainerName"); // NOI18N
    }

    /**
     * Setter for the Name property of the component - usually maps to
     * variable declaration for holding the instance of the component
     * @param value new value of the Name property
     */
    @Override
    public void setName(String value) {
        // noop in forms
    }

    public Point getFormPosition() {
        if (formPosition == null) {
            formPosition = new Point(0,0);//topContainer.getLocation();
        }
        return formPosition;
    }

    public void setFormPosition(Point value) {
        Object old = formPosition;
        formPosition = value;
        getFormModel().fireSyntheticPropertyChanged(this, PROP_FORM_POSITION,
                                                    old, value);
    }

    public Dimension getFormSize() {
        return formSize;
    }

    public void setFormSize(Dimension value) {
        Dimension old = setFormSizeImpl(value);

        // this is called when the property is enabled for writing (i.e. policy
        // is GEN_BOUNDS), but also when loading form (when policy might be not
        // set yet) - so always propagate to designer size
        Dimension designerSize;
        if (getBeanInstance() instanceof Dialog
            || getBeanInstance() instanceof Frame)
        {
            Dimension diffDim = getWindowContentDimensionDiff();
            designerSize = new Dimension(value.width - diffDim.width,
                                         value.height - diffDim.height);
        }
        else designerSize = value;
        setDesignerSizeImpl(designerSize, false);

        getFormModel().fireSyntheticPropertyChanged(this, PROP_FORM_SIZE, old, value);
        getFormModel().fireSyntheticPropertyChanged(this, FormDesigner.PROP_DESIGNER_SIZE, null, null);
    }

    private Dimension setFormSizeImpl(Dimension value) {
        Dimension old = formSize;
        formSize = value;
        if (getNodeReference() != null) { // propagate the change to node
            getNodeReference().firePropertyChangeHelper(PROP_FORM_SIZE, old, value);
        }
        return old;
    }

    public Dimension getDesignerSize() {
        return designerSize;
    }

    public void setDesignerSize(Dimension value) {
        Dimension old = setDesignerSizeImpl(value);

        if (getFormSizePolicy() == GEN_BOUNDS) { // propagate to form size
            Dimension formSize;
            if (getBeanInstance() instanceof Dialog
                || getBeanInstance() instanceof Frame)
            {
                Dimension diffDim = getWindowContentDimensionDiff();
                formSize = new Dimension(value.width + diffDim.width,
                                      value.height + diffDim.height);
            }
            else formSize = value;
            setFormSizeImpl(formSize);
        }

        getFormModel().fireSyntheticPropertyChanged(this, FormDesigner.PROP_DESIGNER_SIZE, old, value);
    }

    private boolean shouldPersistDesignerSize() {
        // don't persist designer size if form size is defined (persisted)
        // and neither for free design forms
        return !hasExplicitSize() && (getLayoutSupport() != null);
    }

    Dimension setDesignerSizeImpl(Dimension value) {
        return setDesignerSizeImpl(value, shouldPersistDesignerSize());
    }

    private Dimension setDesignerSizeImpl(Dimension value, boolean persistent) {
        final Dimension old = designerSize;
        designerSize = value;
        setAuxValue(FormDesigner.PROP_DESIGNER_SIZE, persistent ? value : null);
        if (getNodeReference() != null) { // propagate the change to node
            if (!FormLAF.inLAFBlock()) {
                getNodeReference().firePropertyChangeHelper(FormDesigner.PROP_DESIGNER_SIZE, old, value);
            } else { // firing the change may lead to UI update out of GUI builder, we don't want that in LAF block
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (getNodeReference() != null) {
                            getNodeReference().firePropertyChangeHelper(FormDesigner.PROP_DESIGNER_SIZE, old, designerSize);
                        }
                    }
                });
            }
        }
        return old;
    }

    public boolean getGeneratePosition() {
        return generatePosition;
    }

    public void setGeneratePosition(boolean value) {
        boolean old = generatePosition;
        generatePosition = value;
        getFormModel().fireSyntheticPropertyChanged(this, PROP_GENERATE_POSITION,
                                        old ? Boolean.TRUE : Boolean.FALSE, value ? Boolean.TRUE : Boolean.FALSE);
    }

    public boolean getGenerateSize() {
        return generateSize;
    }

    public void setGenerateSize(boolean value) {
        boolean old = generateSize;
        generateSize = value;
        if (!old && generateSize && getFormSizePolicy() == GEN_BOUNDS
                && getFormSize() == null && getDesignerSize() != null) {
            setDesignerSize(getDesignerSize()); // force recalculation of formSize
        }
        getFormModel().fireSyntheticPropertyChanged(this, PROP_GENERATE_SIZE,
                                        old ? Boolean.TRUE : Boolean.FALSE, value ? Boolean.TRUE : Boolean.FALSE);
    }

    public boolean getGenerateCenter() {
        return generateCenter;
    }

    public void setGenerateCenter(boolean value) {
        boolean old = generateCenter;
        if (isInternalFrame()) {
            // bug 226740 - centering on screen does not make sense for JInternalFrame,
            // but old forms may have this set (we must keep the property)
            value = false;
        }
        generateCenter = value;
        getFormModel().fireSyntheticPropertyChanged(this, PROP_GENERATE_CENTER,
                                        old ? Boolean.TRUE : Boolean.FALSE, value ? Boolean.TRUE : Boolean.FALSE);
    }

    public boolean hasExplicitSize() {
        return getFormSizePolicy() == GEN_BOUNDS && getGenerateSize();
    }

    public int getFormSizePolicy() {
        return java.awt.Window.class.isAssignableFrom(getBeanClass()) || isInternalFrame()
               ? formSizePolicy : GEN_NOTHING;
    }

    public void setFormSizePolicy(int value) {
        int old = formSizePolicy;
        formSizePolicy = value;
        if (value == GEN_BOUNDS) {
            if (designerSize != null) {
                setDesignerSize(getDesignerSize()); // Force recalculation of formSize
                // designer size should not be persistent if form size is defined
                if (getGenerateSize())
                    setAuxValue(FormDesigner.PROP_DESIGNER_SIZE, null);
            }
        }
        else if (!getFormModel().isFreeDesignDefaultLayout()) {
            // designer size should be persistent
            setAuxValue(FormDesigner.PROP_DESIGNER_SIZE, getDesignerSize());
        }
        getFormModel().fireSyntheticPropertyChanged(this, PROP_FORM_SIZE_POLICY,
            Integer.valueOf(old), Integer.valueOf(value));
    }

    // ------------------------------------------------------------------------------
    // End of form synthetic properties

    @Override
    protected Node.Property[] createSyntheticProperties() {
        java.util.ResourceBundle bundle = FormUtils.getBundle();

        Node.Property policyProperty = new PropertySupport.ReadWrite(
            PROP_FORM_SIZE_POLICY,
            Integer.TYPE,
            bundle.getString("MSG_FormSizePolicy"), // NOI18N
            bundle.getString("HINT_FormSizePolicy")) // NOI18N
        {
            @Override
            public Object getValue() throws
                IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                return Integer.valueOf(getFormSizePolicy());
            }

            @Override
            public void setValue(Object val) throws IllegalAccessException,
                                                    IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                if (!(val instanceof Integer)) throw new IllegalArgumentException();
                setFormSizePolicy(((Integer)val).intValue());
                if (getNodeReference() != null)
                    getNodeReference().fireComponentPropertySetsChange();
            }

            @Override
            public boolean canWrite() {
                return !isReadOnly();
            }

            /** Editor for alignment */
            @Override
            public java.beans.PropertyEditor getPropertyEditor() {
                return new SizePolicyEditor();
            }

        };

        Node.Property sizeProperty = new PropertySupport.ReadWrite(
            PROP_FORM_SIZE,
            Dimension.class,
            bundle.getString("MSG_FormSize"), // NOI18N
            bundle.getString("HINT_FormSize")) // NOI18N
        {
            @Override
            public Object getValue() throws
                IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                return getFormSize();
            }

            @Override
            public void setValue(Object val) throws IllegalAccessException,
                                                    IllegalArgumentException, java.lang.reflect.InvocationTargetException {
//                if (!(val instanceof Dimension)) throw new IllegalArgumentException();
                setFormSize((Dimension)val);
            }

            @Override
            public boolean canWrite() {
                return !isReadOnly()
                       && getFormSizePolicy() == GEN_BOUNDS
                       && getGenerateSize();
            }
        };

        Node.Property positionProperty = new PropertySupport.ReadWrite(
            PROP_FORM_POSITION,
            Point.class,
            bundle.getString("MSG_FormPosition"), // NOI18N
            bundle.getString("HINT_FormPosition")) // NOI18N
        {
            @Override
            public Object getValue() throws
                IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                return getFormPosition();
            }

            @Override
            public void setValue(Object val) throws IllegalAccessException,
                                                    IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                if (!(val instanceof Point)) throw new IllegalArgumentException();
                setFormPosition((Point)val);
            }

            @Override
            public boolean canWrite() {
                return !isReadOnly()
                        && getFormSizePolicy() == GEN_BOUNDS
                        && getGeneratePosition()
                        && !getGenerateCenter();
            }
        };

        Node.Property genPositionProperty = new PropertySupport.ReadWrite(
            PROP_GENERATE_POSITION,
            Boolean.TYPE,
            bundle.getString("MSG_GeneratePosition"), // NOI18N
            bundle.getString("HINT_GeneratePosition")) // NOI18N
        {
            @Override
            public Object getValue() throws
                IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                if (getFormSizePolicy() != GEN_BOUNDS || getGenerateCenter()) {
                    return Boolean.FALSE;
                }
                return getGeneratePosition() ? Boolean.TRUE : Boolean.FALSE;
            }

            @Override
            public void setValue(Object val) throws IllegalAccessException,
                                                    IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                if (!(val instanceof Boolean)) throw new IllegalArgumentException();
                setGeneratePosition(((Boolean)val).booleanValue());
                if (getNodeReference() != null)
                    getNodeReference().fireComponentPropertySetsChange();
            }

            @Override
            public boolean canWrite() {
                return !isReadOnly()
                        && getFormSizePolicy() == GEN_BOUNDS
                        && !getGenerateCenter();
            }
        };

        Node.Property genSizeProperty = new PropertySupport.ReadWrite(
            PROP_GENERATE_SIZE,
            Boolean.TYPE,
            bundle.getString("MSG_GenerateSize"), // NOI18N
            bundle.getString("HINT_GenerateSize")) // NOI18N
        {
            @Override
            public Object getValue() throws
                IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                if (getFormSizePolicy() != GEN_BOUNDS) {
                    return Boolean.FALSE;
                }
                return getGenerateSize() ? Boolean.TRUE : Boolean.FALSE;
            }

            @Override
            public void setValue(Object val) throws IllegalAccessException,
                                                    IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                if (!(val instanceof Boolean)) throw new IllegalArgumentException();
                setGenerateSize(((Boolean)val).booleanValue());
                if (getNodeReference() != null)
                    getNodeReference().fireComponentPropertySetsChange();
            }

            @Override
            public boolean canWrite() {
                return !isReadOnly() && getFormSizePolicy() == GEN_BOUNDS;
            }
        };

        Node.Property genCenterProperty = new PropertySupport.ReadWrite(
            PROP_GENERATE_CENTER,
            Boolean.TYPE,
            bundle.getString("MSG_GenerateCenter"), // NOI18N
            bundle.getString("HINT_GenerateCenter")) // NOI18N
        {
            @Override
            public Object getValue() throws
                IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                if (getFormSizePolicy() == GEN_NOTHING) {
                    return Boolean.FALSE;
                }
                return getGenerateCenter() ? Boolean.TRUE : Boolean.FALSE;
            }

            @Override
            public void setValue(Object val) throws IllegalAccessException,
                                                    IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                if (!(val instanceof Boolean)) throw new IllegalArgumentException();
                setGenerateCenter(((Boolean)val).booleanValue());
                if (getNodeReference() != null)
                    getNodeReference().fireComponentPropertySetsChange();
            }

            @Override
            public boolean canWrite() {
                return !isReadOnly() && getFormSizePolicy() != GEN_NOTHING && !isInternalFrame();
            }
        };

        Node.Property<Dimension> designerSizeProperty = new PropertySupport.ReadWrite<Dimension>(
            FormDesigner.PROP_DESIGNER_SIZE,
            Dimension.class,
            bundle.getString("MSG_DesignerSize"), // NOI18N
            bundle.getString("HINT_DesignerSize")) // NOI18N
        {
            @Override
            public Dimension getValue()
                throws IllegalAccessException, IllegalArgumentException,
                       java.lang.reflect.InvocationTargetException
            {
                return getDesignerSize();
            }

            @Override
            public void setValue(Dimension val)
                throws IllegalAccessException, IllegalArgumentException,
                       java.lang.reflect.InvocationTargetException
            {
                setDesignerSize(val);
            }
        };
        // hack: avoid persisting as synthetic value, it's persisted as aux value when needed (#153085)
        designerSizeProperty.setValue("defaultValue", Boolean.TRUE); // NOI18N

        java.util.List<Node.Property> propList = new java.util.ArrayList<Node.Property>();

        propList.add(JavaCodeGenerator.createBeanClassNameProperty(this));

        if (java.awt.Window.class.isAssignableFrom(getBeanClass()) || isInternalFrame()) {            
            propList.add(sizeProperty);
            propList.add(positionProperty);
            propList.add(policyProperty);
            propList.add(genPositionProperty);
            propList.add(genSizeProperty);
            propList.add(genCenterProperty);
        }

        propList.add(designerSizeProperty);

        Node.Property[] props = new Node.Property[propList.size()];
        propList.toArray(props);
        return props;
    }

    // ---------
    // providing the difference of the whole frame/dialog size and the size
    // of the content pane

    private static Dimension windowContentDimensionDiff;

    public Dimension getWindowContentDimensionDiff() {
        boolean undecorated = true;
        Object beanInstance = getBeanInstance();
        if (beanInstance instanceof java.awt.Frame) {
            undecorated = ((java.awt.Frame)beanInstance).isUndecorated();
        } else if (beanInstance instanceof java.awt.Dialog) {
            undecorated = ((java.awt.Dialog)beanInstance).isUndecorated();
        }
        return undecorated ? new Dimension(0, 0) : getDecoratedWindowContentDimensionDiff();
    }

    public static Dimension getDecoratedWindowContentDimensionDiff() {
        if (windowContentDimensionDiff == null) {
            javax.swing.JFrame frame = new javax.swing.JFrame();
            frame.pack();
            Dimension d1 = frame.getSize();
            Dimension d2 = frame.getRootPane().getSize();
            windowContentDimensionDiff =
                new Dimension(d1.width - d2.width, d1.height - d2.height);
        }
        return windowContentDimensionDiff;
    }

    @Override
    void setNodeReference(RADComponentNode node) {
        super.setNodeReference(node);
        if (node != null) {
            Object beanInstance = getBeanInstance();
            if ((beanInstance instanceof java.awt.Frame)
                || (beanInstance instanceof java.awt.Dialog)) {
                // undecorated is not a bound property => it is not possible to
                // listen on the beanInstance => we have to listen on the node
                node.addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if ("undecorated".equals(evt.getPropertyName())) { // NOI18N
                            // Keep current designer size and force update of form size
                            setDesignerSize(getDesignerSize());
                        }
                    }
                });
            }
        }
    }

    private boolean isInternalFrame() {
        return javax.swing.JInternalFrame.class.isAssignableFrom(getBeanClass());
    }

    // ------------------------------------------------------------------------------------------
    // Innerclasses

    public static final class SizePolicyEditor extends java.beans.PropertyEditorSupport {
        /** Display Names for alignment. */
        private static final String[] names = {
            FormUtils.getBundleString("VALUE_sizepolicy_full"), // NOI18N
            FormUtils.getBundleString("VALUE_sizepolicy_pack"), // NOI18N
            FormUtils.getBundleString("VALUE_sizepolicy_none"), // NOI18N
        };

        /** @return names of the possible directions */
        @Override
        public String[] getTags() {
            return names;
        }

        /** @return text for the current value */
        @Override
        public String getAsText() {
            int value =((Integer)getValue()).intValue();
            return names[value];
        }

        /** Setter.
         * @param str string equal to one value from directions array
         */
        @Override
        public void setAsText(String str) {
            if (names[0].equals(str))
                setValue(Integer.valueOf(0));
            else if (names[1].equals(str))
                setValue(Integer.valueOf(1));
            else if (names[2].equals(str))
                setValue(Integer.valueOf(2));
        }
    }
}
