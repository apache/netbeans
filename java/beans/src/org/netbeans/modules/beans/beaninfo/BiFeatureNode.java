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

package org.netbeans.modules.beans.beaninfo;

import java.io.IOException;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;

import javax.swing.Action;
import javax.swing.GrayFilter;
import org.openide.util.actions.SystemAction;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;

/** The basic node for representing features included in BanInfo. It recognizes
* the type of the BiFeature and creates properties according to it.
* @author Petr Hrebejk
*/
final class BiFeatureNode extends AbstractNode implements Node.Cookie {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -8680621542479107034L;

    // static variables ...........................................................................

    /** Property display name constant */
    public static final String PROP_DISPLAY_NAME = "displayName"; // NOI18N
    /** Property expert constant */
    public static final String PROP_EXPERT = "expert"; // NOI18N
    /** Property hidden constant
 */
    public static final String PROP_HIDDEN = "hidden"; // NOI18N
    /** Property name constant */
    public static final String PROP_NAME = "name"; // NOI18N
    /** Property preffered constant */
    public static final String PROP_PREFERRED = "preferred"; // NOI18N
    /** Property short description constant */
    public static final String PROP_SHORT_DESCRIPTION = "shortDescription"; // NOI18N
    /** Property include constant */
    public static final String PROP_INCLUDED = "included"; // NOI18N
    /** Property include constant */
    public static final String PROP_CUSTOMIZER = "customizer"; // NOI18N

    /** Property bound constant */
    public static final String PROP_BOUND = "bound"; // NOI18N
    /** Property constrained constant */
    public static final String PROP_CONSTRAINED = "constrained"; // NOI18N
    /** Property mode constant */
    public static final String PROP_MODE = "mode"; // NOI18N
    /** Property editor class constant */
    public static final String PROP_EDITOR_CLASS = "propertyEditorClass"; // NOI18N
    /** Property non-indexed getter constant */
    public static final String PROP_NI_GETTER = "niGetter"; // NOI18N
    /** Property non-indexed setter constant */
    public static final String PROP_NI_SETTER = "niSetter"; // NOI18N

    /** Property unicast constant */
    public static final String PROP_UNICAST = "unicast"; // NOI18N
    /** Property in default event set constant */
    public static final String PROP_IN_DEFAULT_EVENTSET = "inDefaultEventSet"; // NOI18N


    // variables ..........................................................................

    //private static SystemAction [] staticActions;

    // feature mode asociated to this node
    protected BiFeature  biFeature = null;
    
    //analyser
    protected BiAnalyser  biAnalyser = null;

    static javax.swing.GrayFilter grayFilter = null;
    
    static{
        grayFilter = new GrayFilter(true, 5);
    }
    
    // constructors .......................................................................

    /**
    * Creates empty BreakpointContext.
    */
    public BiFeatureNode (final BiFeature biFeature, BiAnalyser biAnalyser) {
        super (Children.LEAF);
        this.biFeature = biFeature;
        this.biAnalyser = biAnalyser;
        setDisplayName (getName ());
        setShortDescription(biFeature.getToolTip());
        setIconBaseWithExtension( biFeature.getIconBase(false) + ".gif" );
        init ();
    }

    @Override
    public java.awt.Image getIcon( int type ){        
        if( biFeature instanceof BiFeature.Descriptor  && biAnalyser.isNullDescriptor() ) {
            //setIconBase( biFeature.getIconBase(true));
            return GrayFilter.createDisabledImage(super.getIcon(type));
        }
        if( ( biFeature instanceof BiFeature.Property || biFeature instanceof BiFeature.IdxProperty ) && biAnalyser.isNullProperties() ) {
            //setIconBase( biFeature.getIconBase(true));
            return GrayFilter.createDisabledImage(super.getIcon(type));
        }
        if( biFeature instanceof BiFeature.EventSet && biAnalyser.isNullEventSets() ) {
            //setIconBase( biFeature.getIconBase(true));
            return GrayFilter.createDisabledImage(super.getIcon(type));
        }
        if( biFeature instanceof BiFeature.Method && biAnalyser.isNullMethods() ) {
            //setIconBase( biFeature.getIconBase(true));
            return GrayFilter.createDisabledImage(super.getIcon(type));
        }
        //setIconBase( biFeature.getIconBase(false));
        return super.getIcon(type);
    }

    private void init () {
        createProperties ();
        getCookieSet().add ( this );
        /*
        breakpoint.addPropertyChangeListener (new PropertyChangeListener () {
          public void propertyChange (PropertyChangeEvent e) {
            String s = e.getPropertyName ();
            parameterChanged (e);
          }
    });
        */
    }

    @Override
    public <T extends Node.Cookie> T getCookie(Class<T> type) {
        if ( type == BiFeatureNode.class )
            return type.cast(this);

        return getCookieSet().getCookie( type );
    }

    /** Creates properties for this node */
    private void createProperties () {

        // default sheet with "properties" property set // NOI18N
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = sheet.get(Sheet.PROPERTIES);

        ps.put(new PropertySupport.ReadOnly<String> (
                   PROP_NAME,
                   String.class,
                   GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_NAME ),
                   GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_NAME )
               ) {
                   public String getValue () {
                       return (!(biFeature instanceof BiFeature.Descriptor )) ? biFeature.getName () : ((BiFeature.Descriptor)biFeature).getBeanName();
                   }
                   @Override
                   public void setValue (String val) throws IllegalAccessException {
                       throw new IllegalAccessException(GenerateBeanInfoAction.getString("MSG_Cannot_Write"));
                   }
               });

        ps.put( BiNode.createProperty ( biFeature, Boolean.TYPE,
                                        PROP_EXPERT, 
                                        GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_EXPERT ),
                                        GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_EXPERT ),
                                        "isExpert", "setExpert" )); // NOI18N
        ps.put( BiNode.createProperty ( biFeature, Boolean.TYPE,
                                        PROP_HIDDEN, 
                                        GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_HIDDEN ),
                                        GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_HIDDEN ),
                                        "isHidden", "setHidden" )); // NOI18N
        ps.put( BiNode.createProperty ( biFeature, Boolean.TYPE,
                                        PROP_PREFERRED, 
                                        GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_PREFERRED ),
                                        GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_PREFERRED ),
                                        "isPreferred", "setPreferred" )); // NOI18N

        ps.put(new CodePropertySupportRW<String>(
                   PROP_DISPLAY_NAME,
                   String.class,
                   GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_DISPLAY_NAME ),
                   GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_DISPLAY_NAME )
               ) {
                   public String getValue () {
                       return biFeature.getDisplayName() != null ? biFeature.getDisplayName() : "null"; // NOI18N
                   }
                   public void setValue (String val) throws
                       IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                       try {
                           if( "null".equals(val) ) // NOI18N
                                val = null;
                           biFeature.setDisplayName ( val );
                       } catch (ClassCastException e) {
                           throw new IllegalArgumentException ();
                       }
                   }
               });

        ps.put(new CodePropertySupportRW<String>(//PropertySupport.ReadWrite (
                   PROP_SHORT_DESCRIPTION,
                   String.class,
                   GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_SHORT_DESCRIPTION ),
                   GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_SHORT_DESCRIPTION )
               ) {
                   public String getValue () {
                       String toRet = biFeature.getShortDescription () != null ? biFeature.getShortDescription () : "null"; // NOI18N
                       return toRet;
                   }
                   public void setValue (String val) throws
                       IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                       try {
                           if( "null".equals(val) ) // NOI18N
                                val = null;
                           biFeature.setShortDescription ( val );
                       } catch (ClassCastException e) {
                           throw new IllegalArgumentException ();
                       }
                   }
                });
        // Add special properties according to type of feature
        if( ! (biFeature instanceof BiFeature.Descriptor) )
            addIncludedProperty( ps );
        else
            addCustomizerProperty( sheet );

        if ( biFeature instanceof BiFeature.Property || biFeature instanceof BiFeature.IdxProperty )
            addExpertProperty( sheet );
        else if ( biFeature instanceof BiFeature.EventSet )
            addExpertEventSet( sheet );

        // and set new sheet
        setSheet(sheet);
    }

    protected void addIncludedProperty( Sheet.Set ps ) {    
        ps.put(new PropertySupport.ReadWrite<Boolean> (
                   PROP_INCLUDED,
                   Boolean.TYPE,
                   GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_INCLUDED ),
                   GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_INCLUDED )
               ) {
                   public Boolean getValue () {
                       return biFeature.isIncluded ();
                   }
                   public void setValue (Boolean val) throws
                       IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                       try {
                           biFeature.setIncluded ( val );
                           setIconBaseWithExtension( biFeature.getIconBase(false) + ".gif" );
                       } catch (ClassCastException e) {
                           throw new IllegalArgumentException ();
                       }
                   }
               });
    }
    
    protected void addCustomizerProperty( Sheet sheet ) {
        Sheet.Set ps = Sheet.createExpertSet();
    
          ps.put(new CodePropertySupportRW<String>(//PropertySupport.ReadWrite (
          //ps.put(new PropertySupport.ReadWrite (
               PROP_DISPLAY_NAME,
                   String.class,
                   GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_CUSTOMIZER ),
                   GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_CUSTOMIZER )
               ) {
                   public String getValue () {
                       String toRet = ((BiFeature.Descriptor)biFeature).getCustomizer() != null ? ((BiFeature.Descriptor)biFeature).getCustomizer() : "null"; // NOI18N
                       return toRet;
                   }
                   public void setValue (String val) throws
                       IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                       try {
                           if( "null".equals(val) ) // NOI18N
                                val = null;
                           ((BiFeature.Descriptor)biFeature).setCustomizer( val );
                       } catch (ClassCastException e) {
                           throw new IllegalArgumentException ();
                       }
                   }
               });
        sheet.put( ps );
    }

    protected void addExpertProperty( Sheet sheet ) {
        Sheet.Set ps = Sheet.createExpertSet();

        ps.put( BiNode.createProperty ( (BiFeature.Property)biFeature, Boolean.TYPE,
                                        PROP_BOUND, 
                                        GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_BOUND ),
                                        GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_BOUND ),
                                        "isBound", "setBound" )); // NOI18N
        ps.put( BiNode.createProperty ( (BiFeature.Property)biFeature, Boolean.TYPE,
                                        PROP_CONSTRAINED, 
                                        GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_CONSTRAINED ),
                                        GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_CONSTRAINED ),
                                        "isConstrained", "setConstrained" )); // NOI18N
        ps.put(new PropertySupport<Integer> (
                   PROP_MODE,
                   Integer.TYPE,    //int.class !!!!????,
                   GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_MODE ),
                   GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_MODE ),
                   true,
                   ((BiFeature.Property)biFeature).modeChangeable()
               ) {
                   public Integer getValue () {
                       return ((BiFeature.Property)biFeature).getMode();
                   }
                   public void setValue (Integer val) throws
                       IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                       try {
                           ((BiFeature.Property)biFeature).setMode ( val );
                           setIconBaseWithExtension( biFeature.getIconBase(false) + ".gif" );
                       } catch (ClassCastException e) {
                           throw new IllegalArgumentException ();
                       }
                   }
                   @Override
                   public PropertyEditor getPropertyEditor () {
                       return new org.netbeans.modules.beans.ModePropertyEditor();
                   }
               });
        ps.put( BiNode.createProperty ( (BiFeature.Property)biFeature, String.class,
                                        PROP_EDITOR_CLASS, 
                                        GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_EDITOR_CLASS ),
                                        GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_EDITOR_CLASS ),
                                        "getPropertyEditorClass", "setPropertyEditorClass" ));

        if ( biFeature instanceof BiFeature.IdxProperty ) {
            ps.put(new PropertySupport<Boolean> (
                       PROP_NI_GETTER,
                       Boolean.TYPE,
                       GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_NI_GETTER ),
                       GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_NI_GETTER ),
                       true,
                       ((BiFeature.IdxProperty)biFeature).hasNiGetter()
                   ) {
                       public Boolean getValue () {
                           return ((BiFeature.IdxProperty)biFeature).isNiGetter ();
                       }
                       public void setValue (Boolean val) throws
                           IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                           if ( !((BiFeature.IdxProperty)biFeature).hasNiGetter() )
                               throw new IllegalAccessException ();
                           try {
                               ((BiFeature.IdxProperty)biFeature).setNiGetter ( val );
                           } catch (ClassCastException e) {
                               throw new IllegalArgumentException ();
                           }
                       }
                   });
            ps.put(new PropertySupport<Boolean> (
                       PROP_NI_SETTER,
                       Boolean.TYPE,
                       GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_NI_SETTER ),
                       GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_NI_SETTER ),
                       true,
                       ((BiFeature.IdxProperty)biFeature).hasNiSetter()
                   ) {
                       public Boolean getValue () {
                           return ((BiFeature.IdxProperty)biFeature).isNiSetter ();
                       }
                       public void setValue (Boolean val) throws
                           IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                           if ( !((BiFeature.IdxProperty)biFeature).hasNiSetter() )
                               throw new IllegalAccessException ();
                           try {
                               ((BiFeature.IdxProperty)biFeature).setNiSetter ( val );
                           } catch (ClassCastException e) {
                               throw new IllegalArgumentException ();
                           }
                       }
                   });
        }

        sheet.put( ps );
    }

    void addExpertEventSet ( Sheet sheet ) {
        Sheet.Set ps = Sheet.createExpertSet();

        ps.put( BiNode.createProperty ( (BiFeature.EventSet)biFeature, Boolean.TYPE,
                                        PROP_UNICAST, 
                                        GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_UNICAST ),
                                        GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_UNICAST ),
                                        "isUnicast", null )); // NOI18N

        ps.put( BiNode.createProperty ( (BiFeature.EventSet)biFeature, Boolean.TYPE,
                                        PROP_IN_DEFAULT_EVENTSET, 
                                        GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_IN_DEFAULT_EVENTSET ),
                                        GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_IN_DEFAULT_EVENTSET ),
                                        "isInDefaultEventSet", "setInDefaultEventSet" )); // NOI18N

        sheet.put( ps );

    }
    // implementation of Node ..........................................................

    /** Getter for set of actions that should be present in the
    * popup menu of this node. This set is used in construction of
    * menu returned from getContextMenu and specially when a menu for
    * more nodes is constructed.
    *
    * @return array of system actions that should be in popup menu
    */
    @Override
    public Action[] getActions(boolean context) {
        Action[] staticActions = null;
        //if (staticActions == null) {
            if( ! (biFeature instanceof BiFeature.Descriptor) ){
                staticActions = new Action[] {
                                    SystemAction.get (BiToggleAction.class),
                                    null
                                    /*
                                    SystemAction.get (DeleteAction.class),
                                    null,
                                    SystemAction.get (ToolsAction.class),
                                    SystemAction.get (PropertiesAction.class),
                                    */
                                };
            }
            else {                
                staticActions = new Action[0];
            }
        //}
        return staticActions;
    }

    /**
    * the feature cannot be removed it can only be disabled from BeanInfo
    *
    * @return <CODE>true</CODE>
    */
    @Override
    public boolean canDestroy () {
        return false;
    }

    /**
    * Deletes breakpoint and removes the node too.
    * Ovverrides destroy() from abstract node.
    */
    @Override
    public void destroy () throws IOException {
        // remove node
        // super.destroy ();
    }

    /** It has default action - it is the toggle of value for include to bean info
    * @return <CODE>true</CODE>
    */
    public boolean hasDefaultAction () {
        return true;
    }

    /** Toggles the selection of bi feature */
    public void toggleSelection() {
        biFeature.setIncluded ( !biFeature.isIncluded() );
        firePropertyChange( PROP_INCLUDED, !biFeature.isIncluded(), biFeature.isIncluded() );
        setIconBaseWithExtension( biFeature.getIconBase(false) + ".gif" );
    }

    /** Includes/excludes the pattern from bean info */
    public void include( boolean value ) {
        if (( value && biFeature.isIncluded() ) ||
                ( !value && !biFeature.isIncluded() ) )
            return;

        biFeature.setIncluded ( value );
        firePropertyChange( PROP_INCLUDED, !biFeature.isIncluded(), biFeature.isIncluded() );
        setIconBaseWithExtension( biFeature.getIconBase(false) + ".gif" );
    }


    /** Executes default action.
    */
    public void invokeDefaultAction() {
        //System.out.println ( "Default action"); // NOI18N
        toggleSelection();
    }

    // private methods .....................................................................

    /**
    * Returns the associated BiFature
    */
    BiFeature getBiFeature () {
        return biFeature;
    }

    BiAnalyser getBiAnalyser () {
        return biAnalyser;
    }

    /**
    * Sets display name and fires property change.
    */
    /*
    void parameterChanged (PropertyChangeEvent e) {
      setDisplayName (getName ());
      firePropertyChange (e.getPropertyName (), e.getOldValue (), e.getNewValue ());
}
    */

    /**
    * Returns human presentable name of this breakpoint containing
    * informations about lineNumber, className e.t.c.
    *
    * @return human presentable name of this breakpoint.
    */
    @Override
    public String getName () {
        return (!(biFeature instanceof BiFeature.Descriptor )) ? biFeature.getName () : ((BiFeature.Descriptor)biFeature).getBeanName();
        //return biFeature.getName();
    }
    
    public void iconChanged(){
        if( biFeature instanceof BiFeature.Descriptor  && biAnalyser.isNullDescriptor() ) {
            setIconBaseWithExtension( biFeature.getIconBase(true) + ".gif");            
        }
        else if( ( biFeature instanceof BiFeature.Property || biFeature instanceof BiFeature.IdxProperty ) && biAnalyser.isNullProperties() ) {
            setIconBaseWithExtension( biFeature.getIconBase(true) + ".gif");            
        }
        else if( biFeature instanceof BiFeature.EventSet && biAnalyser.isNullEventSets() ) {
            setIconBaseWithExtension( biFeature.getIconBase(true) + ".gif");            
        }
        else if( biFeature instanceof BiFeature.Method && biAnalyser.isNullMethods() ) {
            setIconBaseWithExtension( biFeature.getIconBase(true) + ".gif");            
        }
        else setIconBaseWithExtension( biFeature.getIconBase(false) + ".gif"); 
        
        fireIconChange();
    }
    
    abstract class CodePropertySupportRW<T> extends PropertySupport.ReadWrite<T>
    {
        CodePropertySupportRW(String name, Class<T> type,
                              String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return new PropertyEditorSupport() {
                @Override
                public java.awt.Component getCustomEditor() {
                    return new CustomCodeEditor(CodePropertySupportRW.this);
                }

                @Override
                public boolean supportsCustomEditor() {
                    return true;
                }
            };            
        }
    }
}

