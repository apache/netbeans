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

package org.netbeans.modules.form;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import javax.swing.*;
import javax.swing.border.Border;
import org.openide.ErrorManager;

import org.netbeans.modules.form.fakepeer.FakePeerSupport;

/**
 * Factory class for creating objects, providing java creation code,
 * registering CreationDescriptor classes, and related utility methods.
 *
 * @author Tomas Pavek
 */

public class CreationFactory {

    private static Map<String,CreationDescriptor> registry;

    private static boolean defaultDescriptorsCreated = false;

    interface PropertyParameters {
     
        public String getPropertyName();
        
        public String getJavaParametersString(FormProperty prop);

        public Object[] getPropertyParametersValues(FormProperty prop);
        
        public Class[] getPropertyParametersTypes();
    }
    
    static class Property2ParametersMapper {              
        
        private final String propertyName;        
        private final Class[] propertyType = new Class[1]; 
        private PropertyParameters parameters; 
        
        Property2ParametersMapper(Class propertyClass, String propertyName) {
            this.propertyType[0] = propertyClass;
            this.propertyName = propertyName;                  
        }                
        
        public String getPropertyName() {
            return  propertyName;
        }        

        public Class[] getPropertyTypes() {
            if(parameters!=null){
                return parameters.getPropertyParametersTypes();
            }            
            return propertyType;
        }        
        
        public String getJavaParametersString(FormProperty prop) {            
            if(parameters!=null){
                return parameters.getJavaParametersString(prop);
            }
            return prop.getJavaInitializationString();
        }

        public Object[] getPropertyParametersValues(FormProperty prop) {              
            if(parameters!=null){
                return parameters.getPropertyParametersValues(prop);
            }    
            try{
                return new Object[] { prop.getRealValue() };   
            } catch(InvocationTargetException ite) {
                ErrorManager.getDefault().notify(ite);
            } catch(IllegalAccessException iae){
                ErrorManager.getDefault().notify(iae);
            }                
            return new Object[] {};   
        }
        
        public void setPropertyParameters(PropertyParameters parameters) {
            this.parameters = parameters;
        }                        
    }    
    
    private CreationFactory() {}

    // -----------
    // registry methods

    public static CreationDescriptor getDescriptor(Class cls) {
        CreationDescriptor cd = getRegistry().get(cls.getName());
        if (cd == null && !defaultDescriptorsCreated
                && (cls.getName().startsWith("javax.swing.") // NOI18N
                    || cls.getName().startsWith("java.awt."))) { // NOI18N
            createDefaultDescriptors();
            cd = getRegistry().get(cls.getName());
        }
        if (cd != null) {
            cd.setDescribedClass(cls);
        }
        return cd;
    }

    public static void registerDescriptor(CreationDescriptor desc) {
        getRegistry().put(desc.getDescribedClassName(), desc);
    }
    
    public static void unregisterDescriptor(CreationDescriptor desc) {
        if (registry != null)
            registry.remove(desc.getDescribedClassName());
    }

    // -----------
    // creation methods

    public static Object createDefaultInstance(final Class cls)
        throws Exception
    {
        CreationDescriptor cd = getDescriptor(cls);
        Object cl = UIManager.get("ClassLoader"); // NOI18N
        Object cl2 = UIManager.getLookAndFeelDefaults().get("ClassLoader"); // NOI18N
        ClassLoader uiCl = (cl instanceof ClassLoader) ? (ClassLoader)cl : null;
        ClassLoader systemCl = org.openide.util.Lookup.getDefault().lookup(ClassLoader.class);
        ClassLoader beanCl = cls.getClassLoader();
        java.util.List<ClassLoader> loaders = new ArrayList<ClassLoader>();
        if (beanCl != null) {
            loaders.add(beanCl);
        }
        loaders.add(systemCl);
        if (uiCl != null) {
            loaders.add(uiCl);
        }
        ClassLoader newCl = new MultiClassLoader(loaders.toArray(new ClassLoader[0]));
        UIManager.put("ClassLoader", newCl); // NOI18N
        Object instance = cd != null ?
                              cd.createDefaultInstance() :
                cls.getDeclaredConstructor().newInstance();
        if (cl == cl2) {
            // The original classloader (i.e., cl) was in look and feel defaults.
            // It remains there, we just have to remove the value that
            // we set in user defaults (see the structure of MultiUIDefaults).
            UIManager.getDefaults().remove("ClassLoader"); // NOI18N
        } else {
            // The original classloader was in user defaults => return it back
            UIManager.put("ClassLoader", cl); // NOI18N
        }
        initAfterCreation(instance);
        return instance;
    }

    public static Object createInstance(Class cls)
        throws Exception
    {
        Object instance;

        CreationDescriptor cd = CreationFactory.getDescriptor(cls);
        instance = cd != null ? cd.createDefaultInstance() :
                cls.getDeclaredConstructor().newInstance();

        initAfterCreation(instance);
        return instance;
    }

    public static Object createInstance(Class cls,
                                        FormProperty[] props,
                                        int style)
        throws Exception
    {
        CreationDescriptor cd = getDescriptor(cls);
        if (cd == null)
            return null;

        CreationDescriptor.Creator creator = cd.findBestCreator(props, style);
        if (creator == null)
            return null;

        Object instance = creator.createInstance(props);
        initAfterCreation(instance);
        return instance;
    }

//    public static String getJavaCreationCode(Class cls,
//                                             FormProperty[] props,
//                                             int style) {
//        CreationDescriptor cd = getDescriptor(cls);
//        if (cd != null) {
//            CreationDescriptor.Creator creator = cd.findBestCreator(props, style);
//            if (creator != null) {
//               creator.getJavaCreationCode(props, null, null, null, null);
//            }
//        }
//        return null;
//    }

    // ------------
    // utility methods
    
    public static FormProperty[] getPropertiesForCreator(
                                           CreationDescriptor.Creator creator,
                                           FormProperty[] properties) {

        String[] propNames = creator.getPropertyNames();
        FormProperty[] crProps = new FormProperty[propNames.length];

        for (int i=0; i < propNames.length; i++) {
            String propName = propNames[i];
            for (int j=0; j < properties.length; j++)
                if (propName.equals(properties[j].getName())) {
                    crProps[i] = properties[j];
                    break;
                }
            if (crProps[i] == null)
                return null; // missing property, should not happen
        }

        return crProps;
    }

    public static FormProperty[] getRemainingProperties(
                                           CreationDescriptor.Creator creator,
                                           FormProperty[] properties) {

        String[] propNames = creator.getPropertyNames();
        FormProperty[] remProps = new FormProperty[properties.length - propNames.length];
        if (remProps.length == 0) return remProps;

        int ii = 0;
        for (int i=0; i < properties.length; i++) {
            String propName = properties[i].getName();
            for (int j=0; j < propNames.length; j++) {
                if (propName.equals(propNames[j])) break;
                if (j+1 == propNames.length) {
                    if (ii > remProps.length)
                        return null; // should not happen
                    remProps[ii++] = properties[i];
                }
            }
        }

        return remProps;
    }

    public static boolean containsProperty(CreationDescriptor desc,
                                           String propName)
    {
        CreationDescriptor.Creator[] creators = desc.getCreators();
        if (creators == null)
            return false;

        for (int i=0; i < creators.length; i++) {
            String[] propNames = creators[i].getPropertyNames();
            for (int j=0; j < propNames.length; j++)
                if (propNames[j].equals(propName))
                    return true;
        }
        return false;
    }

    public static boolean containsProperty(CreationDescriptor.Creator creator,
                                           String propName)
    {
        String[] propNames = creator.getPropertyNames();
        for (int j=0; j < propNames.length; j++)
            if (propNames[j].equals(propName))
                return true;
        return false;
    }

    public static FormProperty findProperty(String propName,
                                            FormProperty[] properties) {
        for (int i=0; i < properties.length; i++)
            if (properties[i].getName().equals(propName))
                return properties[i];
        return null;
    }

    public static CreationDescriptor.Creator findCreator(
                                                 CreationDescriptor desc,
                                                 Class[] paramTypes)
    {
        CreationDescriptor.Creator[] creators = desc.getCreators();
        for (int i=0; i < creators.length; i++) {
            CreationDescriptor.Creator cr = creators[i];
            if (cr.getParameterCount() == paramTypes.length) {
                Class[] types = cr.getParameterTypes();
                boolean match = true;
                for (int j=0; j < types.length; j++)
                    if (!types[j].isAssignableFrom(paramTypes[j])) {
                        match = false;
                        break;
                    }
                if (match)
                    return cr;
            }
        }
        return null;
    }

    /** Evaluates creators for array of properties.
     * (Useful for CreationDescriptor.findBestCreator(...) implementation.)
     * 
     * @param creators creators to consider
     * @param properties properties to consider
     * @param changedOnly determines whether to consider changed properties only
     * @return array of int - for each creator a count of placed properties
     */
    public static int[] evaluateCreators(CreationDescriptor.Creator[] creators,
                                         FormProperty[] properties,
                                         boolean changedOnly) {

        if (creators == null || creators.length == 0) return null;

        int[] placed = new int[creators.length];
        for (int i=0; i < properties.length; i++) {
            if (!changedOnly || properties[i].isChanged()) {
                String name = properties[i].getName();

                for (int j=0; j < creators.length; j++) {
                    String[] crNames = creators[j].getPropertyNames();
                    for (int k=0; k < crNames.length; k++)
                        if (name.equals(crNames[k]))
                            placed[j]++;
                }
            }
        }
        return placed;
    }

    /** Finds the best creator upon given evaluation.
     * (Useful for CreationDescriptor.findBestCreator(...) implementation.)
     * 
     * @param creators creators to consider.
     * @param properties properties to consider.
     * @param placed for each creator a count of placed properties
     * @param placeAllProps determines whether all properties should be placed
     * @return index of most suitable creator
     */
    public static int getBestCreator(CreationDescriptor.Creator[] creators,
                                     FormProperty[] properties,
                                     int[] placed,
                                     boolean placeAllProps)
    {
        if (creators == null || creators.length == 0)
            return -1;

        int best = 0;
        int[] sizes = new int[creators.length];
        sizes[0] = creators[0].getParameterCount();

        if (placeAllProps) {
            // find shortest creator with all properties placed
            for (int i=1; i < placed.length; i++) {
                sizes[i] = creators[i].getParameterCount();
                if (placed[i] > placed[best]
                    || (placed[i] == placed[best]
                        && (sizes[i] < sizes[best]
                            || (sizes[i] == sizes[best]
                                && compareCreatorsAmbiguity(
                                     creators[i], creators[best], properties)
                                   == 1))))
                    best = i;
            }
        }
        else { // find longest creator with all parameters provided by properties
            for (int i=1; i < placed.length; i++) {
                sizes[i] = creators[i].getParameterCount();
                int iDiff = sizes[i] - placed[i];
                int bestDiff = sizes[best] - placed[best];
                if (iDiff < bestDiff
                    || (iDiff == bestDiff
                        && (sizes[i] > sizes[best]
                            || (sizes[i] == sizes[best]
                                && compareCreatorsAmbiguity(
                                     creators[i], creators[best], properties)
                                   == 1))))
                    best = i;
            }
        }

        return best;
    }

    // -----------
    // non-public methods

    /** Compares two creators with equal number of placed properties and equal
     * number of all properties. To distinguish which one is better, their
     * properties are checked for null values which could cause ambiguity in
     * generated code.
     * @return 1 if creator1 is better, 2 if creator2 is better, 0 if they
     *          are equal
     */
    static int compareCreatorsAmbiguity(CreationDescriptor.Creator cr1,
                                        CreationDescriptor.Creator cr2,
                                        FormProperty[] properties)
    {
        int nullValues1 = 0;
        int nullValues2 = 0;

        for (int i=0, n=cr1.getParameterCount(); i < n; i++) {
            String name1 = cr1.getPropertyNames()[i];
            String name2 = cr2.getPropertyNames()[i];
            if (!name1.equals(name2)) {
                FormProperty prop1 = null;
                FormProperty prop2 = null;
                for (int j=0; j < properties.length; j++)
                    if (prop1 == null && name1.equals(properties[j].getName())) {
                        prop1 = properties[j];
                        if (prop2 != null)
                            break;
                    }
                    else if (prop2 == null && name2.equals(properties[j].getName())) {
                        prop2 = properties[j];
                        if (prop1 != null)
                            break;
                    }

                if (prop1 != null && !prop1.getValueType().isPrimitive()) {
                    try {
                        if (prop1.getRealValue() == null)
                            nullValues1++;
                    }
                    catch (Exception ex) {} // ignore
                }
                if (prop2 != null && !prop2.getValueType().isPrimitive()) {
                    try {
                        if (prop2.getRealValue() == null)
                            nullValues2++;
                    }
                    catch (Exception ex) {} // ignore
                }
            }
        }

        if (nullValues1 == nullValues2)
            return 0;
        return nullValues1 < nullValues2 ? 1 : 2;
    }

    static Map<String,CreationDescriptor> getRegistry() {
        if (registry == null)
            registry = new HashMap<String,CreationDescriptor>(40);
        return registry;
    }

    // additional initializations for some components - in fact hacks required
    // by using fake peers and remapping L&F...
    private static void initAfterCreation(Object instance) {
        if (instance instanceof javax.swing.border.TitledBorder)
            ((javax.swing.border.TitledBorder)instance)
                .setTitleFont(UIManager.getFont("TitledBorder.createDefaultInstancefont")); // NOI18N
        else if (instance instanceof java.awt.Component
                 && !(instance instanceof javax.swing.JComponent)
                 && !(instance instanceof javax.swing.RootPaneContainer))
        {
            ((Component)instance).setName(null);
            ((Component)instance).setFont(FakePeerSupport.getDefaultAWTFont());
        }
        else if (instance instanceof MenuComponent) {
            ((MenuComponent)instance).setName(null);
            ((MenuComponent)instance).setFont(FakePeerSupport.getDefaultAWTFont());
        }
    }

    // ---------------------------------------------------
    // constructors descriptors for some "special" classes...  
    private static void createDefaultDescriptors() {
        Class[][] constrParamTypes;
        String[][] constrPropertyNames;
        Object[] defaultConstrParams;
        String methodName;        
        CreationDescriptor cd;
        InsetsPropertyParameters[] insetsPropertyParameters = 
                new InsetsPropertyParameters[] { new InsetsPropertyParameters("borderInsets") };        
                
        try {
        // borders ------------

        // LineBorder                                       
        constrParamTypes = new Class[][] {
            { Color.class, Integer.TYPE , Boolean.TYPE }

        };
        constrPropertyNames = new String[][] {            
            { "lineColor", "thickness" , "roundedCorners" }
        };
                 
        defaultConstrParams = new Object[] { java.awt.Color.black };
        cd = new CreationDescriptor();
        cd.addConstructorCreators(  
                javax.swing.border.LineBorder.class, 
                constrParamTypes, constrPropertyNames, defaultConstrParams);
                
        constrParamTypes = new Class[][] {
            { Color.class },
            { Color.class, Integer.TYPE }
        };
        constrPropertyNames = new String[][] {
            { "lineColor" },
            { "lineColor", "thickness" }  
            
        };
        methodName = "createLineBorder";
        
        cd.addMethodCreators(
                javax.swing.BorderFactory.class, javax.swing.border.LineBorder.class, methodName,
                constrParamTypes, constrPropertyNames, null, defaultConstrParams);
        registerDescriptor(cd);
                
        
        // EtchedBorder
        defaultConstrParams = new Object[] { };        
        constrParamTypes = new Class[][] {
            { },
            { Color.class, Color.class },
            { Integer.TYPE },
            { Integer.TYPE, Color.class, Color.class }
        };
        constrPropertyNames = new String[][] {
            { },
            { "highlightColor", "shadowColor" },
            { "etchType" },
            { "etchType", "highlightColor", "shadowColor" }
        };
        
        methodName = "createEtchedBorder";    
        registerDescriptor(new CreationDescriptor(
                javax.swing.BorderFactory.class, javax.swing.border.EtchedBorder.class, methodName,
                constrParamTypes, constrPropertyNames, null, defaultConstrParams));
        
        // EmptyBorder     
        constrParamTypes = new Class[][] {
            { Insets.class }
        };
        constrPropertyNames = new String[][] {
            { "borderInsets" }
        };

        Integer one = Integer.valueOf(1);
        defaultConstrParams = new Object[] { one, one, one, one };
        methodName = "createEmptyBorder";
        
        registerDescriptor(new CreationDescriptor(
                                   javax.swing.BorderFactory.class, javax.swing.border.EmptyBorder.class, 
                                   methodName, constrParamTypes, constrPropertyNames, insetsPropertyParameters, defaultConstrParams));

        // TitledBorder              
        constrParamTypes = new Class[][] {
            { String.class },
            { Border.class, String.class },
            { Border.class, String.class, Integer.TYPE, Integer.TYPE },
            { Border.class, String.class, Integer.TYPE, Integer.TYPE, Font.class },
            { Border.class, String.class, Integer.TYPE, Integer.TYPE, Font.class, Color.class },
            { Border.class }
        };
        constrPropertyNames = new String[][] {
            { "title" },
            { "border", "title" },
            { "border", "title", "titleJustification", "titlePosition" },
            { "border", "title", "titleJustification", "titlePosition", "titleFont" },
            { "border", "title", "titleJustification", "titlePosition", "titleFont", "titleColor" },
            { "border" }
        };
        
        defaultConstrParams = new Object[] { null, "", Integer.valueOf(0), Integer.valueOf(0) };
        methodName = "createTitledBorder";                      
        registerDescriptor(new CreationDescriptor(
                javax.swing.BorderFactory.class, javax.swing.border.TitledBorder.class, methodName,
                constrParamTypes, constrPropertyNames, null, defaultConstrParams));

        // CompoundBorder          
        constrParamTypes = new Class[][] {
            { },
            { Border.class, Border.class }
        };
        constrPropertyNames = new String[][] {
            { },
            { "outsideBorder", "insideBorder" }
        };           
        
        defaultConstrParams = new Object[0];
        methodName = "createCompoundBorder";             
        registerDescriptor(new CreationDescriptor(
                javax.swing.BorderFactory.class, javax.swing.border.CompoundBorder.class, methodName,
                constrParamTypes, constrPropertyNames, null, defaultConstrParams));

        // BevelBorder
        constrParamTypes = new Class[][] {
            { Integer.TYPE },
            { Integer.TYPE, Color.class, Color.class },
            { Integer.TYPE, Color.class, Color.class, Color.class, Color.class }
        };
        constrPropertyNames = new String[][] {
            { "bevelType" },
            { "bevelType", "highlightOuterColor", "shadowOuterColor" },
            { "bevelType", "highlightOuterColor", "highlightInnerColor",
                           "shadowOuterColor", "shadowInnerColor" }
        };
                      
        defaultConstrParams = new Object[] { Integer.valueOf(javax.swing.border.BevelBorder.RAISED) };
        methodName = "createBevelBorder";                     
        registerDescriptor(new CreationDescriptor(
                javax.swing.BorderFactory.class, javax.swing.border.BevelBorder.class, methodName, 
                constrParamTypes, constrPropertyNames, null, defaultConstrParams));
                         
        // SoftBevelBorder
        constrParamTypes = new Class[][] {
            { Integer.TYPE },
            { Integer.TYPE, Color.class, Color.class },
            { Integer.TYPE, Color.class, Color.class, Color.class, Color.class }
        };
        constrPropertyNames = new String[][] {
            { "bevelType" },
            { "bevelType", "highlightOuterColor", "shadowOuterColor" },
            { "bevelType", "highlightOuterColor", "highlightInnerColor",
                           "shadowOuterColor", "shadowInnerColor" }
        };        
        registerDescriptor(new CreationDescriptor(
                javax.swing.border.SoftBevelBorder.class, 
                constrParamTypes, constrPropertyNames, defaultConstrParams));

        // MatteBorder              
        cd = new CreationDescriptor();

        constrParamTypes = new Class[][] {
            { Icon.class }            
        };
        constrPropertyNames = new String[][] {
            { "tileIcon" }        
        };         
        cd.addConstructorCreators( javax.swing.border.MatteBorder.class, 
                                   constrParamTypes, constrPropertyNames, defaultConstrParams);
        
        constrParamTypes = new Class[][] {
            { Insets.class, Icon.class },
            { Insets.class, Color.class }
        };
        constrPropertyNames = new String[][] {
            { "borderInsets", "tileIcon" },
            { "borderInsets", "matteColor" }
        };         
        defaultConstrParams = new Object[] { 
            one, one, one, one,
            java.awt.Color.black
        };        
        methodName = "createMatteBorder";                                
        cd.addMethodCreators(javax.swing.BorderFactory.class, javax.swing.border.MatteBorder.class, methodName,
                                constrParamTypes, constrPropertyNames, insetsPropertyParameters, defaultConstrParams);        
        registerDescriptor(cd);

        // layouts --------------

        // BorderLayout
        constrParamTypes = new Class[][] {
            { },
            { Integer.TYPE, Integer.TYPE }
        };
        constrPropertyNames = new String[][] {
            { },
            { "hgap", "vgap" }
        };
        defaultConstrParams = new Object[0];
        registerDescriptor(new CreationDescriptor(
                java.awt.BorderLayout.class,
                constrParamTypes, constrPropertyNames, defaultConstrParams));

        // FlowLayout
        constrParamTypes = new Class[][] {
            { },
            { Integer.TYPE },
            { Integer.TYPE, Integer.TYPE, Integer.TYPE }
        };
        constrPropertyNames = new String[][] {
            { },
            { "alignment" },
            { "alignment", "hgap", "vgap" },
        };
        registerDescriptor(new CreationDescriptor(
                java.awt.FlowLayout.class,
                constrParamTypes, constrPropertyNames, defaultConstrParams));

        // GridBagLayout
        constrParamTypes = new Class[][] {
            { }
        };
        constrPropertyNames = new String[][] {
            { }
        };
        registerDescriptor(new CreationDescriptor(
                java.awt.GridBagLayout.class,
                constrParamTypes, constrPropertyNames, defaultConstrParams));

        // GridLayout
        constrParamTypes = new Class[][] {
            { },
            { Integer.TYPE, Integer.TYPE },
            { Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE }
        };
        constrPropertyNames = new String[][] {
            { },
            { "rows", "columns" },
            { "rows", "columns", "hgap", "vgap" }
        };
        registerDescriptor(new CreationDescriptor(
                java.awt.GridLayout.class,
                constrParamTypes, constrPropertyNames, defaultConstrParams));

        // CardLayout
        constrParamTypes = new Class[][] {
            { },
            { Integer.TYPE, Integer.TYPE }
        };
        constrPropertyNames = new String[][] {
            { },
            { "hgap", "vgap" }
        };
        registerDescriptor(new CreationDescriptor(
                java.awt.CardLayout.class,
                constrParamTypes, constrPropertyNames, defaultConstrParams));

        // AWT --------

        // Dialog
        constrParamTypes = new Class[][] {
            { java.awt.Frame.class },
        };
        constrPropertyNames = new String[][] {
            { "owner" },
        };
        defaultConstrParams = new Object[] { new java.awt.Frame() };
        registerDescriptor(new CreationDescriptor(
            java.awt.Dialog.class,
            constrParamTypes, constrPropertyNames, defaultConstrParams));

        // other -------

        // JPanel on JDK 1.3 uses one instance of FlowLayout for all instances
        // created by default constructor - this causes problems
        registerDescriptor(
            new CreationDescriptor(javax.swing.JPanel.class) {
                @Override
                public Object createDefaultInstance() {
                    return new javax.swing.JPanel(new java.awt.FlowLayout());
                }
            }
        );

        // Box.Filler
        constrParamTypes = new Class[][] {
            { Dimension.class, Dimension.class, Dimension.class }
        };
        constrPropertyNames = new String[][] {
            { "minimumSize", "preferredSize", "maximumSize" }
        };
        defaultConstrParams = new Object[] {new Dimension(), new Dimension(), new Dimension()};
        registerDescriptor(new CreationDescriptor(
                javax.swing.Box.Filler.class,
                constrParamTypes, constrPropertyNames, defaultConstrParams));

        // ----------

        defaultDescriptorsCreated = true;

        }
        catch (NoSuchMethodException ex) { // should not happen
            ex.printStackTrace();
        }
    }
    
    static class InsetsPropertyParameters implements PropertyParameters {              
           
        private static Class[] parameterTypes = new Class[] {Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE};
        private final String propertyName;
        
        public InsetsPropertyParameters(String propertyName) {
            this.propertyName = propertyName;
        }
        
        @Override
        public String getPropertyName() {
            return propertyName;
        }
        
        @Override
        public String getJavaParametersString(FormProperty prop) {     
            Insets insets = (Insets) getRealValue (prop);                    
            if(insets != null) {
                return insets.top + ", " + insets.left + ", " + insets.bottom + ", " + insets.right;
            } else {
                return "";
            }                                                
        }        
        @Override
        public Object[] getPropertyParametersValues(FormProperty prop) {                        
            Insets insets = (Insets) getRealValue(prop);                            
            if(insets != null) {
                return new Object[] {
                    Integer.valueOf(insets.top),
                    Integer.valueOf(insets.left),
                    Integer.valueOf(insets.bottom),
                    Integer.valueOf(insets.right)
                };
            } else {
                return new Object[] { };                
            }                    
        }    
        
        @Override
        public Class[] getPropertyParametersTypes() {
            return parameterTypes;
        }
        
        private static Object getRealValue(FormProperty prop){
            try {
                return prop.getRealValue();
            } catch(InvocationTargetException ite) {
                ErrorManager.getDefault().notify(ite);
            } catch(IllegalAccessException iae){
                ErrorManager.getDefault().notify(iae);
            }                             
            return null;
        }           
    }
    
    
}
