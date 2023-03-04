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

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import javax.swing.Action;
import javax.swing.GrayFilter;
import org.netbeans.api.java.source.SourceUtils;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
* This class represents BeanInfo root node.
*
* @author   Petr Hrebejk
*/
public final class BiNode extends AbstractNode {


    // static variables ...........................................................................

    /** generated Serialized Version UID */
    //static final long                      serialVersionUID = -6346315017458451778L;

    private static String ICON_BASE = "org/netbeans/modules/beans/resources/beanInfo.gif"; // NOI18N
    private static String ICON_BASE_PATTERNS = "org/netbeans/modules/beans/resources/patternGroup.gif"; // NOI18N
    private static String WAIT_ICON_BASE = "org/netbeans/modules/beans/resources/wait.gif"; // NOI18N
    private static String WARNING_ICON_BASE = "org/netbeans/modules/beans/resources/warning.gif"; // NOI18N

    private static String PROP_NULL_DESCRIPTOR = "nullDescriptor"; // NOI18N
    private static String PROP_NULL_PROPERTIES = "nullProperties"; // NOI18N
    private static String PROP_NULL_EVENTS = "nullEvents"; // NOI18N
    private static String PROP_NULL_METHODS = "nullMethods"; // NOI18N
    private static String PROP_LAZY_DESCRIPTOR = "lazyDescriptor"; // NOI18N
    private static String PROP_LAZY_PROPERTIES = "lazyProperties"; // NOI18N
    private static String PROP_LAZY_EVENTS = "lazyEvents"; // NOI18N
    private static String PROP_LAZY_METHODS = "lazyMethods"; // NOI18N
    private static String PROP_BI_ICON_C16 = "iconColor16x16"; // NOI18N
    private static String PROP_BI_ICON_M16 = "iconMono16x16"; // NOI18N
    private static String PROP_BI_ICON_C32 = "iconColor32x32"; // NOI18N
    private static String PROP_BI_ICON_M32 = "iconMono32x32"; // NOI18N
    private static String PROP_BI_DEFAULT_PROPERTY = "defaultPropertyIndex"; // NOI18N
    private static String PROP_BI_DEFAULT_EVENT = "defaultEventIndex"; // NOI18N
    private static String PROP_USE_SUPERCLASS   = "useSuperclass"; // NOI18N
    
    static javax.swing.GrayFilter grayFilter = null;
    
    static{
        grayFilter = new GrayFilter(true, 5);
    }

    // variables ....................................................................................

    private BiAnalyser biAnalyser;
    
    private PropertySupport[] descSubnodeDescriptor =  new PropertySupport[] {
                new PropertySupport.ReadWrite<Boolean> (
                    PROP_NULL_DESCRIPTOR,
                    Boolean.TYPE,
                    GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_NULL_DESCRIPTOR ),
                    GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_NULL_DESCRIPTOR )
                ) {
                    public Boolean getValue () {
                        return biAnalyser.isNullDescriptor ();
                    }
                    public void setValue (Boolean val) throws
                        IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                        try {                            
                            biAnalyser.setNullDescriptor ( val );                            
                        } catch (ClassCastException e) {
                            throw new IllegalArgumentException ();
                        }
                        iconChange();
                    }
                }
            };

            
    private PropertySupport[] propSubnodeProperties =  new PropertySupport[] {
                new PropertySupport.ReadWrite<Boolean> (
                    PROP_NULL_PROPERTIES,
                    Boolean.TYPE,
                    GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_NULL_PROPERTIES ),
                    GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_NULL_PROPERTIES )
                ) {
                    public Boolean getValue () {
                        return biAnalyser.isNullProperties ();
                    }
                    public void setValue (Boolean val) throws
                        IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                        try {
                            biAnalyser.setNullProperties ( val );
                        } catch (ClassCastException e) {
                            throw new IllegalArgumentException ();
                        }
                        iconChange();
                    }
                }
            };

    private PropertySupport[] eventSubnodeProperties =  new PropertySupport[] {
                new PropertySupport.ReadWrite<Boolean> (
                    PROP_NULL_EVENTS,
                    Boolean.TYPE,
                    GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_NULL_EVENTS ),
                    GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_NULL_EVENTS )
                ) {
                    public Boolean getValue () {
                        return biAnalyser.isNullEventSets ();
                    }
                    public void setValue (Boolean val) throws
                        IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                        try {
                            biAnalyser.setNullEventSets ( val );
                        } catch (ClassCastException e) {
                            throw new IllegalArgumentException ();
                        }
                        iconChange();
                    }
                }
            };

    private PropertySupport[] methodSubnodeProperties =  new PropertySupport[] {
                new PropertySupport.ReadWrite<Boolean> (
                    PROP_NULL_PROPERTIES,
                    Boolean.TYPE,
                    GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_NULL_METHODS ),
                    GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_NULL_METHODS )
                ) {
                    public Boolean getValue () {
                        return biAnalyser.isNullMethods ();
                    }
                    public void setValue (Boolean val) throws
                        IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                        try {
                            biAnalyser.setNullMethods ( val );
                        } catch (ClassCastException e) {
                            throw new IllegalArgumentException ();
                        }
                        iconChange();
                    }
                }
            };

    // constructors ..................................................................................

    /**
    * Creates tree for BeanInfo Analyser.
    */
    BiNode ( final BiAnalyser biAnalyser ) {
        /*
        super ( new BiChildren ( biAnalyser, 
          new Class[] {
            BiFeature.Property.class, 
            BiFeature.IdxProperty.class,
            BiFeature.EventSet.class } ) );
        */
        super (new Children.Array() );
        this.biAnalyser = biAnalyser;
        setDisplayName (NbBundle.getBundle(BiNode.class).
                        getString ("CTL_NODE_BeanInfo"));
        setIconBaseWithExtension (ICON_BASE);

        Node[] subnodes = (biAnalyser.isOlderVersion() ? 
            new Node[] {
                    new SubNode( biAnalyser,
                               new Class[] { BiFeature.Property.class, BiFeature.IdxProperty.class },
                               "CTL_NODE_Properties", // NOI18N
                               ICON_BASE_PATTERNS,
                               propSubnodeProperties, 
                               null ),

                    new SubNode( biAnalyser,
                               new Class[] { BiFeature.EventSet.class },
                               "CTL_NODE_EventSets", // NOI18N
                               ICON_BASE_PATTERNS,
                               eventSubnodeProperties, 
                               null )
            } : new Node[] {
                    new SubNode( biAnalyser,
                               new Class[] { BiFeature.Descriptor.class },
                               "CTL_NODE_Descriptor", // NOI18N
                               ICON_BASE_PATTERNS,
                               descSubnodeDescriptor ,
                               new Node.Property[] {
                                    createProperty (biAnalyser, Boolean.TYPE,
                                    PROP_LAZY_DESCRIPTOR, 
                                    GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_LAZY_DESCRIPTOR ),
                                    GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_LAZY_DESCRIPTOR ),
                                    "isLazyDescriptor", "setLazyDescriptor" )} // NOI18N
                               ),
                               
                    new SubNode( biAnalyser,
                               new Class[] { BiFeature.Property.class, BiFeature.IdxProperty.class },
                               "CTL_NODE_Properties", // NOI18N
                               ICON_BASE_PATTERNS,
                               propSubnodeProperties,
                               new Node.Property[] {
                                    createProperty (biAnalyser, Boolean.TYPE,
                                    PROP_LAZY_DESCRIPTOR, 
                                    GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_LAZY_PROPERTIES ),
                                    GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_LAZY_PROPERTIES ),
                                    "isLazyProperties", "setLazyProperties" )} // NOI18N
                               ),

                    new SubNode( biAnalyser,
                               new Class[] { BiFeature.EventSet.class },
                               "CTL_NODE_EventSets", // NOI18N
                               ICON_BASE_PATTERNS,
                               eventSubnodeProperties, 
                               new Node.Property[] {
                                    createProperty (biAnalyser, Boolean.TYPE,
                                    PROP_LAZY_EVENTS, 
                                    GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_LAZY_EVENTS ),
                                    GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_LAZY_EVENTS ),
                                    "isLazyEventSets", "setLazyEventSets" )} // NOI18N
                               ),

                    new SubNode( biAnalyser, 
                           new Class[] { BiFeature.Method.class },
                           "CTL_NODE_Methods", // NOI18N
                           ICON_BASE_PATTERNS,
                           methodSubnodeProperties, 
                           new Node.Property[] {
                                createProperty (biAnalyser, Boolean.TYPE,
                                PROP_LAZY_METHODS, 
                                GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_LAZY_METHODS ),
                                GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_LAZY_METHODS ),
                                "isLazyMethods", "setLazyMethods" )} // NOI18N
                           )
            });
        
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = sheet.get(Sheet.PROPERTIES);

        ps.put( new ImagePropertySupportRW (
                    PROP_BI_ICON_C16,
                    GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_BI_ICON_C16 ),
                    GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_BI_ICON_C16 )
                ) {
                    public BiIconEditor.BiImageIcon getValue () throws
                            IllegalAccessException, InvocationTargetException {
                        return biAnalyser.getIconC16() != null
                                ? ie.iconFromText(biAnalyser.getIconC16())
                                : null;
                    }
                    
                    public void setValue (BiIconEditor.BiImageIcon value) throws
                            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                        biAnalyser.setIconC16(ie.textFromIcon(value));
                    }                                
                }
              );
        ps.put( new ImagePropertySupportRW (
                    PROP_BI_ICON_M16,
                    GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_BI_ICON_M16 ),
                    GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_BI_ICON_M16 )
                ) {
                    public BiIconEditor.BiImageIcon getValue () throws
                            IllegalAccessException, InvocationTargetException {
                        return biAnalyser.getIconM16() != null
                                ? ie.iconFromText(biAnalyser.getIconM16())
                                : null;
                    }
                    
                    public void setValue (BiIconEditor.BiImageIcon value) throws
                            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                        
                        biAnalyser.setIconM16(ie.textFromIcon(value));
                    }
                }
              );
        ps.put( new ImagePropertySupportRW (
                    PROP_BI_ICON_C32,
                    GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_BI_ICON_C32 ),
                    GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_BI_ICON_C32 )
                ) {
                    public BiIconEditor.BiImageIcon getValue () throws
                            IllegalAccessException, InvocationTargetException {
                        return biAnalyser.getIconC32() != null
                                ? ie.iconFromText(biAnalyser.getIconC32())
                                : null;
                    }
                    
                    public void setValue (BiIconEditor.BiImageIcon value) throws
                            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                        
                        biAnalyser.setIconC32(ie.textFromIcon(value));
                    }                    
                }
              );
        ps.put( new ImagePropertySupportRW (
                    PROP_BI_ICON_M32,
                    GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_BI_ICON_M32 ),
                    GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_BI_ICON_M32 )
                ) {
                    public BiIconEditor.BiImageIcon getValue () throws
                            IllegalAccessException, InvocationTargetException {
                        return biAnalyser.getIconM32() != null
                                ? ie.iconFromText(biAnalyser.getIconM32())
                                : null;
                    }
                    
                    public void setValue (BiIconEditor.BiImageIcon value) throws
                            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                        biAnalyser.setIconM32(ie.textFromIcon(value));
                    }                    
                }
              );
        ps.put( createProperty (biAnalyser, Integer.TYPE,
                                PROP_BI_DEFAULT_PROPERTY, 
                                GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_BI_DEFAULT_PROPERTY ),
                                GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_BI_DEFAULT_PROPERTY ),
                                "getDefaultPropertyIndex", "setDefaultPropertyIndex" ) ); // NOI18N
        
        ps.put( createProperty (biAnalyser, Integer.TYPE,
                                PROP_BI_DEFAULT_EVENT, 
                                GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_BI_DEFAULT_EVENT ),
                                GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_BI_DEFAULT_EVENT ),
                                "getDefaultEventIndex", "setDefaultEventIndex" ) ); // NOI18N

        //only if it is super class version (since 3.3)      
        if(biAnalyser.isSuperclassVersion()){      
            ps.put( createProperty (biAnalyser, Boolean.TYPE,
                                PROP_USE_SUPERCLASS, 
                                GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_USE_SUPERCLASS ),
                                GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_USE_SUPERCLASS ),
                                "isUseSuperClass", "setUseSuperClass" ) ); // NOI18N
        }              
        setSheet(sheet);

        ((Children.Array)getChildren()).add( subnodes );

    }
   
    /** refresh icons after get from introspection change */
    public void iconChange(){
        Node[] nodes = ((Children.Array)getChildren()).getNodes();
        for( int i = 0; i < nodes.length; i++ ){
            ((SubNode)nodes[i]).iconChanged();
        }
    }
    
    static final class SubNode extends AbstractNode implements Node.Cookie {

        //private static SystemAction[] staticActions;
        private BiAnalyser biAnalyser;
        private Class<?> key; 
        
        SubNode ( BiAnalyser biAnalyser, Class<?>[] keys, String titleKey, String iconBase,
                  Node.Property[] properties, Node.Property[] expert ) {
            super ( new BiChildren (  biAnalyser, keys ) );
            setDisplayName (NbBundle.getBundle(BiNode.class).
                            getString (titleKey));
            setIconBaseWithExtension ( iconBase );
                
            this.biAnalyser = biAnalyser;
            this.key = keys[0];
            
            Sheet sheet = Sheet.createDefault();
            Sheet.Set ps = sheet.get(Sheet.PROPERTIES);

            for ( int i = 0; i < properties.length; i++ ) {
                ps.put( properties[i] );
            }
            
            if( expert != null ){                
                Sheet.Set eps = Sheet.createExpertSet();

                for ( int i = 0; i < expert.length; i++ ) {
                    eps.put( expert[i] );
                }
                sheet.put(eps);
            }
            
            setSheet(sheet);

            getCookieSet().add ( this );
        }
        
        @Override
        public java.awt.Image getIcon( int type ){
            if( key == BiFeature.Descriptor.class && biAnalyser.isNullDescriptor() )
                return GrayFilter.createDisabledImage(super.getIcon(type));
            if( key == BiFeature.Property.class && biAnalyser.isNullProperties() )
                return GrayFilter.createDisabledImage(super.getIcon(type));
            if( key == BiFeature.EventSet.class && biAnalyser.isNullEventSets() )
                return GrayFilter.createDisabledImage(super.getIcon(type));
            if( key == BiFeature.Method.class && biAnalyser.isNullMethods() )
                return GrayFilter.createDisabledImage(super.getIcon(type));

            return super.getIcon(type);
        }

        @Override
        public java.awt.Image getOpenedIcon( int type ){
            if( key == BiFeature.Descriptor.class && biAnalyser.isNullDescriptor() )
                return GrayFilter.createDisabledImage(super.getIcon(type));
            if( key == BiFeature.Property.class && biAnalyser.isNullProperties() )
                return GrayFilter.createDisabledImage(super.getIcon(type));
            if( key == BiFeature.EventSet.class && biAnalyser.isNullEventSets() )
                return GrayFilter.createDisabledImage(super.getIcon(type));
            if( key == BiFeature.Method.class && biAnalyser.isNullMethods() )
                return GrayFilter.createDisabledImage(super.getIcon(type));

            return super.getOpenedIcon(type);
        }

        /** Getter for set of actions that should be present in the
        * popup menu of this node. This set is used in construction of
        * menu returned from getContextMenu and specially when a menu for
        * more nodes is constructed.
        *
        * @return array of system actions that should be in popup menu
        */
        @Override
        public Action[] getActions ( boolean context ) {
            if ( context ) {
                return super.getActions( true );
            }
            else {
                Children ch = getChildren();
                Node[] nodes = ch.getNodes();
                if ( nodes == null )
                    return new SystemAction[0];

                if( nodes.length == 0 || ( nodes[0] != null && ((BiFeatureNode)nodes[0]).getBiFeature() instanceof BiFeature.Descriptor) )
                    return new SystemAction[0];

                return new SystemAction[] {
                                        SystemAction.get (BiIncludeAllAction.class),
                                        SystemAction.get (BiExcludeAllAction.class),
                                        null
                                    };
            }                          
        }

        void includeAll( boolean value) {
            Children ch = getChildren();

            Node[] nodes = ch.getNodes();

            if ( nodes == null )
                return;

            for( int i = 0; i < nodes.length; i++ ) {
                ((BiFeatureNode)nodes[i]).include( value );
            }

        }

        /** refresh icons after get from introspection change */
        public void iconChanged(){
            fireIconChange();
            fireOpenedIconChange();
            
            Children ch = getChildren();
            Node[] nodes = ch.getNodes();
            if ( nodes == null )
                return;

            for( int i = 0; i < nodes.length; i++ ) {
                ((BiFeatureNode)nodes[i]).iconChanged();
            }
        }
    }

    // Inner Class ---------------------------------------------------------------

    static final class Wait extends AbstractNode {

        Wait () {

            super( Children.LEAF );
            setDisplayName( SourceUtils.isScanInProgress()? NbBundle.getBundle( BiNode.class ).getString( "CTL_NODE_WaitScan" ) : NbBundle.getBundle( BiNode.class ).getString( "CTL_NODE_Wait" ) );
            setIconBaseWithExtension( WAIT_ICON_BASE );

        }
    }

    private static final class Error extends AbstractNode {

        Error (String name) {

            super( Children.LEAF );
            setDisplayName(name);
            setIconBaseWithExtension( WARNING_ICON_BASE );

        }
    }
    
    public static Node createNoSourceNode(FileObject bi) {
        String name = bi.getName();
        name = name.substring(0, name.length() - "BeanInfo".length()); // NOI18N
        String ext = bi.getExt();
        if (ext.length() > 0) {
            name += '.' + ext;
        }
        String msg = NbBundle.getMessage(BiNode.class, "CTL_NODE_MissingBeanFile", name);
        return new Error(msg);
    }
    
    public static Node createBiNode(BiAnalyser bia, NotifyDescriptor error) {
        if (bia == null) {
            Object msg = error == null
                    ? NbBundle.getMessage(BiNode.class, "CTL_NODE_UnknownBeanInfoState")
                    : error.getMessage();
            return new Error(msg.toString());
        } else if (bia.isBeanBroken()) {
            String msg = NbBundle.getMessage(
                    BiNode.class, "MSG_BrokenBean",
                    bia.bis.getSourceDataObject().getPrimaryFile().getNameExt());
            return new Error(msg);
        } else if (!bia.bis.isNbBeanInfo()) {
            String msg = NbBundle.getMessage(BiNode.class, "CTL_NODE_UnknownBeanInfoFormat");
            return new Error(msg);
        } else if (bia.bis.getSourceDataObject() == null) {
            return createNoSourceNode(bia.bis.getDataObject().getPrimaryFile());
        } else {
            return new BiNode(bia);
        }
    }
    
    abstract class ImagePropertySupportRW extends PropertySupport.ReadWrite<BiIconEditor.BiImageIcon> {
        BiIconEditor ie = null;
        
        ImagePropertySupportRW(String name, String displayName, String shortDescription) {
            super(name, BiIconEditor.BiImageIcon.class, displayName, shortDescription);
            ie = new BiIconEditor( biAnalyser.bis.getSourceDataObject().getPrimaryFile() );            
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return ie;
        }
    }    

    public static <T> Node.Property<T> createProperty (Object inst, Class<T> type,
                                                String name, String dispName,
                                                String shortDesc,
                                                String getter, String setter ) {
        Node.Property<T> prop;

        try {
            prop = new PropertySupport.Reflection<T> (inst, type, getter, setter);
        } catch (NoSuchMethodException e) {            
            throw new IllegalStateException (e.getMessage() + " " + getter); // NOI18N
        }
        
        prop.setName (name);
        prop.setDisplayName (dispName);
        prop.setShortDescription (shortDesc);
        return prop;
    }
}
